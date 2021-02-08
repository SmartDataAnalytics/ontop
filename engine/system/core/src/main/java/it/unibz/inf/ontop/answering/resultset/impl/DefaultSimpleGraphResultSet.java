package it.unibz.inf.ontop.answering.resultset.impl;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

import it.unibz.inf.ontop.answering.connection.OntopStatement;
import it.unibz.inf.ontop.answering.resultset.*;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.query.algebra.BNodeGenerator;
import org.eclipse.rdf4j.query.algebra.Extension;
import org.eclipse.rdf4j.query.algebra.ExtensionElem;
import org.eclipse.rdf4j.query.algebra.ProjectionElem;
import org.eclipse.rdf4j.query.algebra.ProjectionElemList;
import org.eclipse.rdf4j.query.algebra.ValueConstant;
import org.eclipse.rdf4j.query.algebra.ValueExpr;
import org.eclipse.rdf4j.sail.SailException;

import it.unibz.inf.ontop.com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.answering.reformulation.input.ConstructTemplate;
import it.unibz.inf.ontop.exception.OntopConnectionException;
import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.term.ObjectConstant;
import it.unibz.inf.ontop.model.term.RDFConstant;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.spec.ontology.RDFFact;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

public class DefaultSimpleGraphResultSet implements GraphResultSet {

	private final ResultSetIterator iterator;

	private final int fetchSize;

	public DefaultSimpleGraphResultSet(
			TupleResultSet tupleResultSet,
			ConstructTemplate constructTemplate,
			TermFactory termFactory,
			org.apache.commons.rdf.api.RDF rdfFactory, OntopStatement ontopStatement,
			boolean preloadStatements)
			throws OntopConnectionException {
		this.fetchSize = tupleResultSet.getFetchSize();
		try {
			iterator =
					new ResultSetIterator(tupleResultSet, constructTemplate, termFactory, rdfFactory, ontopStatement,
							preloadStatements);
		} catch (Exception e) {
			throw new SailException(e.getCause());
		}
	}

	@Override
	public boolean hasNext() throws OntopConnectionException, OntopResultConversionException {
		return iterator.hasNext();
	}

	@Override
	public RDFFact next() throws OntopConnectionException {
		return iterator.next();
	}

	@Override
	public OntopCloseableIterator<RDFFact, OntopConnectionException> iterator() {
		return iterator;
	}

	@Override
	public void close() throws OntopConnectionException {
		iterator.close();
	}

	private static class ResultSetIterator extends RDFFactCloseableIterator {
		private final TupleResultSet resultSet;
		private final ConstructTemplate constructTemplate;
		private final TermFactory termFactory;
		private final org.apache.commons.rdf.api.RDF rdfFactory;
		private final Queue<RDFFact> statementBuffer;
		private final OntopStatement ontopStatement;
		private final boolean enablePreloadStatements;
		private ImmutableMap<String, ValueExpr> extMap;

		private ResultSetIterator(
				TupleResultSet resultSet,
				ConstructTemplate constructTemplate,
				TermFactory termFactory,
				org.apache.commons.rdf.api.RDF rdfFactory, OntopStatement ontopStatement,
				boolean enablePreloadStatements)
				throws OntopConnectionException, OntopResultConversionException {
			this.resultSet = resultSet;
			this.constructTemplate = constructTemplate;
			this.termFactory = termFactory;
			this.rdfFactory = rdfFactory;
			this.ontopStatement = ontopStatement;
			intExtMap();
			this.statementBuffer = new LinkedList<>();
			this.enablePreloadStatements = enablePreloadStatements;
			if (enablePreloadStatements) {
				preloadStatements();
			}
		}

		@Override
		public boolean hasNext() throws OntopConnectionException, OntopResultConversionException {
			if (statementBuffer.isEmpty() && resultSetHasNext()) {
				addStatementFromResultSet();
			}
			boolean hasNext = !statementBuffer.isEmpty();
			if (!hasNext && !enablePreloadStatements) {
				handleClose();
			}
			return hasNext;
		}

		@Override
		public RDFFact next() throws OntopConnectionException {
			if (statementBuffer.isEmpty() && !enablePreloadStatements) {
				handleClose();
			}
			return statementBuffer.remove();
		}

		@Override
		public void remove() throws OntopConnectionException {
			next();
		}

		@Override
		public void handleClose() throws OntopConnectionException {
			try {
				if (resultSet.isConnectionAlive()) {
					// closing sql statement, automatically closes the result set as well, but should not close
					// for Describe queries as it is used multiple times
					if (ontopStatement != null && !enablePreloadStatements) {
						ontopStatement.close();
					} else {
						resultSet.close();
					}
				}
			} catch (Exception e) {
				throw new OntopConnectionException(e);
			}
		}

		private void addStatementFromResultSet() {
			try {
				OntopBindingSet bindingSet = resultSet.next();
				for (ProjectionElemList peList : constructTemplate.getProjectionElemList()) {
					int size = peList.getElements().size();
					for (int i = 0; i < size / 3; i++) {
						ObjectConstant subjectConstant =
								(ObjectConstant) getConstant(peList.getElements().get(i * 3), bindingSet);
						IRIConstant propertyConstant =
								(IRIConstant) getConstant(peList.getElements().get(i * 3 + 1), bindingSet);
						RDFConstant objectConstant =
								(RDFConstant) getConstant(peList.getElements().get(i * 3 + 2), bindingSet);
						if (subjectConstant != null && propertyConstant != null && objectConstant != null) {
							statementBuffer.add(
									RDFFact.createTripleFact(subjectConstant, propertyConstant, objectConstant));
						}
					}
				}
			} catch (OntopResultConversionException | OntopConnectionException e) {
				e.printStackTrace();
			}
		}

		private Constant getConstant(ProjectionElem node, OntopBindingSet bindingSet)
				throws OntopResultConversionException {
			Constant constant = null;
			String nodeName = node.getSourceName();
			ValueExpr ve = null;

			if (extMap != null) {
				ve = extMap.get(nodeName);
			}

			if (ve instanceof ValueConstant) {
				ValueConstant vc = (ValueConstant) ve;
				if (vc.getValue() instanceof IRI) {
					constant = termFactory.getConstantIRI(rdfFactory.createIRI(vc.getValue().stringValue()));
				} else if (vc.getValue() instanceof Literal) {
					constant = termFactory.getRDFLiteralConstant(vc.getValue().stringValue(), XSD.STRING);
				} else {
					constant = termFactory.getConstantBNode(vc.getValue().stringValue());
				}
			} else if (ve instanceof BNodeGenerator) {
				// See https://www.w3.org/TR/sparql11-query/#tempatesWithBNodes
				String rowId = bindingSet.getRowUUIDStr();

				String label =
						Optional.ofNullable(((BNodeGenerator) ve).getNodeIdExpr())
								// If defined, we expected the b-node label to be constant (as appearing in the
								// CONSTRUCT block)
								.filter(e -> e instanceof ValueConstant)
								.map(v -> ((ValueConstant) v).getValue().stringValue())
								.map(s -> s + rowId)
								.orElseGet(() -> nodeName + rowId);

				constant = termFactory.getConstantBNode(label);
			} else {
				constant = bindingSet.getConstant(nodeName);
			}
			return constant;
		}

		public void addNewRDFFact(RDFFact statement) {
			statementBuffer.add(statement);
		}

		private void intExtMap() {
			Extension ex = constructTemplate.getExtension();
			if (ex != null) {
				extMap =
						ex.getElements().stream()
								.collect(ImmutableCollectors.toMap(ExtensionElem::getName, ExtensionElem::getExpr));
			} else {
				extMap = null;
			}
		}

		private void preloadStatements() throws OntopConnectionException, OntopResultConversionException {
			while (resultSet.hasNext()) {
				addStatementFromResultSet();
			}
			handleClose();
		}

		private boolean resultSetHasNext() throws OntopConnectionException, OntopResultConversionException {
			if (enablePreloadStatements) {
				return false;
			}
			if (!resultSet.isConnectionAlive()) {
				return false;
			}
			return resultSet.hasNext();
		}
	}
}

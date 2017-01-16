package it.unibz.inf.ontop.rdf4j.repository;

/*
 * #%L
 * ontop-quest-sesame
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.inf.ontop.model.OBDAException;
import it.unibz.inf.ontop.owlrefplatform.core.IQuestDBStatement;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.core.QuestDBConnection;
import it.unibz.inf.ontop.owlrefplatform.core.SIQuestDBStatement;
import it.unibz.inf.ontop.rdf4j.RDF4JRDFIterator;
import it.unibz.inf.ontop.rdf4j.query.OntopBooleanQuery;
import it.unibz.inf.ontop.rdf4j.query.OntopGraphQuery;
import it.unibz.inf.ontop.rdf4j.query.OntopTupleQuery;
import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.OpenRDFUtil;
import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.common.iteration.CloseableIteratorIteration;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.NamespaceImpl;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.ValueFactoryImpl;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.query.parser.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.UnknownTransactionStateException;
import org.eclipse.rdf4j.rio.*;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;

import java.io.*;
import java.net.URL;
import java.util.*;

// TODO(Xiao): separate the implementation into two subclasses for virtual and classic modes
public class OntopRepositoryConnection implements org.eclipse.rdf4j.repository.RepositoryConnection, AutoCloseable {

    private static final String ONTOP_VIRTUAL_REPOSITORY_IS_READ_ONLY = "Ontop virtual repository is read-only.";
    private AbstractOntopRepository repository;
	private QuestDBConnection questConn;
    private boolean isOpen;
    private boolean autoCommit;
    private boolean isActive;
    private RDFParser rdfParser;

	
	public OntopRepositoryConnection(AbstractOntopRepository rep, QuestDBConnection connection) throws OBDAException
	{
		this.repository = rep;
		this.questConn = connection;
		this.isOpen = true;
		this.isActive = false;
		this.autoCommit = connection.getAutoCommit();
		this.rdfParser = Rio.createParser(RDFFormat.RDFXML,
                this.repository.getValueFactory());
	}

	
	@Override
    public void add(Statement st, Resource... contexts) throws RepositoryException {
		// Adds the supplied statement to this repository, optionally to one or
		// more named contexts.
		OpenRDFUtil.verifyContextNotNull(contexts);
		if (contexts != null && contexts.length == 0 && st.getContext() != null) {
			contexts = new Resource[] { st.getContext() };
		}
		try {
			begin();
			List<Statement> l = new ArrayList<>();
			l.add(st);
			Iterator<Statement> iterator = l.iterator();
			addWithoutCommit(iterator, contexts);
		} catch (Exception e) {
			throw new RepositoryException(e);
		} finally {
			autoCommit();
		}
	}

	@Override
    public void add(Iterable<? extends Statement> statements, Resource... contexts)
			throws RepositoryException {
		//Adds the supplied statements to this repository, optionally to one or more named contexts. 
		 OpenRDFUtil.verifyContextNotNull(contexts);

         begin();

         try {
             addWithoutCommit((Iterator<Statement>) statements, contexts);
             
         } catch (RepositoryException e) {
             if (autoCommit) {
                 rollback();
             }
             throw e;
         } catch (RuntimeException e) {
             if (autoCommit) {
                 rollback();
             }
             throw e;
         } catch (Exception e) {
        	 throw new RepositoryException(e);
		} finally {
             autoCommit();
         }

	}


	@Override
    public void add(File file, String baseIRI, RDFFormat dataFormat, Resource... contexts)
			throws IOException, RDFParseException, RepositoryException {
		//Adds RDF data from the specified file to a specific contexts in the repository. 

		if (baseIRI == null) {
			// default baseIRI to file
			baseIRI = file.toURI().toString();
		}

        try (InputStream in = new FileInputStream(file)) {
            add(in, baseIRI, dataFormat, contexts);
        }
	}

	 @Override
     public void add(URL url, String baseIRI, RDFFormat dataFormat,
                     Resource... contexts) throws IOException,
             RDFParseException, RepositoryException {
		// Adds the RDF data that can be found at the specified URL to the
		// repository,
		// optionally to one or more named contexts.
		if (baseIRI == null) {
			baseIRI = url.toExternalForm();
		}

         try (InputStream in = url.openStream()) {
             add(in, baseIRI, dataFormat, contexts);
         }
     }

     @Override
     public void add(InputStream in, String baseIRI,
                     RDFFormat dataFormat, Resource... contexts)
             throws IOException, RDFParseException, RepositoryException {
 		//Adds RDF data from an InputStream to the repository, optionally to one or more named contexts. 
         addInputStreamOrReader(in, baseIRI, dataFormat, contexts);
     }

     @Override
     public void add(Reader reader, String baseIRI,
                     RDFFormat dataFormat, Resource... contexts)
             throws IOException, RDFParseException, RepositoryException {
    	//Adds RDF data from a Reader to the repository, optionally to one or more 
 		//named contexts. Note: using a Reader to upload byte-based data means that 
 		//you have to be careful not to destroy the data's character encoding by 
 		//enforcing a default character encoding upon the bytes. \
 		//If possible, adding such data using an InputStream is to be preferred.
         addInputStreamOrReader(reader, baseIRI, dataFormat, contexts);
     }

	@Override
    public void add(Resource subject, org.eclipse.rdf4j.model.IRI predicate, Value object, Resource... contexts)
			throws RepositoryException {
		//Adds a statement with the specified subject, predicate and object to this repository, 
		//optionally to one or more named contexts. 
		OpenRDFUtil.verifyContextNotNull(contexts);
		ValueFactory vf = SimpleValueFactory.getInstance();
		
		Statement st = vf.createStatement(subject, vf.createIRI(predicate.toString()), object);
		
		add(st, contexts);
	}
	
	 /**
     * Adds the data that can be read from the supplied InputStream or Reader to
     * this repository.
     * 
     * @param inputStreamOrReader
     *        An {@link InputStream} or {@link Reader} containing RDF data that
     *        must be added to the repository.
     * @param baseIRI
     *        The base IRI for the data.
     * @param dataFormat
     *        The file format of the data.
     * @param contexts
     *        The context to which the data should be added in case
     *        <tt>enforceContext</tt> is <tt>true</tt>. The value
     *        <tt>null</tt> indicates the null context.
     */
    protected void addInputStreamOrReader(Object inputStreamOrReader,
            String baseIRI, RDFFormat dataFormat, Resource... contexts)
            throws IOException, RDFParseException, RepositoryException {
    	
    	if (Objects.equals(repository.getType(), QuestConstants.VIRTUAL))
			throw new RepositoryException();
    	
        OpenRDFUtil.verifyContextNotNull(contexts);

        rdfParser = Rio.createParser(dataFormat,
                getRepository().getValueFactory());

        ParserConfig config = rdfParser.getParserConfig();
		// To emulate DatatypeHandling.IGNORE 
		config.addNonFatalError(BasicParserSettings.FAIL_ON_UNKNOWN_DATATYPES);
		config.addNonFatalError(BasicParserSettings.VERIFY_DATATYPE_VALUES);
		config.addNonFatalError(BasicParserSettings.NORMALIZE_DATATYPE_VALUES);
//		config.set(BasicParserSettings.PRESERVE_BNODE_IDS, true);
		
//        rdfParser.setVerifyData(true);
//        rdfParser.setStopAtFirstError(true);
//        rdfParser.setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);

        boolean autoCommit = isAutoCommit();
        begin();        
        
        RDF4JRDFIterator rdfHandler = new RDF4JRDFIterator();
        rdfParser.setRDFHandler(rdfHandler);
        
        
        try {            
            if (inputStreamOrReader instanceof  InputStream) {
            	inputStreamOrReader = (InputStream) inputStreamOrReader;
            } 
            else if (inputStreamOrReader instanceof  Reader) {
            	inputStreamOrReader = (Reader) inputStreamOrReader;
            } 
            else {
                throw new IllegalArgumentException(
                        "inputStreamOrReader must be an InputStream or a Reader, is a: "
                                + inputStreamOrReader.getClass());
            }
            
           // System.out.println("Parsing... ");
			SIQuestDBStatement questStm = questConn.createSIStatement();
    		
            Thread insert = new Thread(new Insert(rdfParser, (InputStream)inputStreamOrReader, baseIRI));
            Thread process = new Thread(new Process(rdfHandler, questStm));
            
            //start threads
            insert.start();
            process.start();
            
            insert.join();
            process.join();
            
            questStm.close();
                     
     
        } catch (RuntimeException | InterruptedException e) {
        	//System.out.println("exception, rolling back!");
            if (autoCommit) {
                rollback();
            }
            throw new RepositoryException(e);
        } finally {
            setAutoCommit(autoCommit);
            autoCommit();
        }
    }


            
          private class Insert implements Runnable{
        	  private RDFParser rdfParser;
        	  private InputStream inputStreamOrReader;
        	  private String baseIRI;
        	  public Insert(RDFParser rdfParser, InputStream inputStreamOrReader, String baseIRI)
        	  {
        		  this.rdfParser = rdfParser;
        		  this.inputStreamOrReader = inputStreamOrReader;
        		  this.baseIRI = baseIRI;
        	  }
        	  @Override
              public void run()
        	  {
        		  try {
					rdfParser.parse((InputStream) inputStreamOrReader, baseIRI);
				} catch (Exception e) {
throw new RuntimeException(e);
				}
        	  }
        	  
          }
          
          private class Process implements Runnable{
        	  private RDF4JRDFIterator iterator;
        	  private IQuestDBStatement questStmt;
        	  public Process(RDF4JRDFIterator iterator, IQuestDBStatement qstm) throws OBDAException
        	  {
        		  this.iterator = iterator;
        		  this.questStmt = qstm;
        	  }
        	  
        	  @Override
              public void run()
        	  {
        		    try {
						questStmt.add(iterator, boolToInt(autoCommit), 5000);
        		    	
					} catch (OBDAException e) {
						throw new RuntimeException(e);
					}
        	  }
          }
                       
      
  
    protected void addWithoutCommit(Iterator< Statement> stmIterator, Resource... contexts)
    throws RepositoryException, OBDAException {
    	
    	if ( repository.getType().equals(QuestConstants.VIRTUAL))
			throw new RepositoryException();
    	
    	
    	if (contexts.length == 0) {
    		contexts = new Resource[]{} ;
    	}
    	boolean currCommit = autoCommit;
    	autoCommit = false;
 
    	RDF4JRDFIterator it = new RDF4JRDFIterator(stmIterator);
    	
    	//insert data   useFile=false, batch=0
		SIQuestDBStatement questStm = null;
    	try {
			// Statement insertion is only supported in the classic A-box mode
    		questStm = questConn.createSIStatement();
			questStm.add(it);
		} catch (OBDAException e) {
			throw new RepositoryException(e);
		}
    	finally{
			if (questStm != null)
    			questStm.close();
    	}
		
		autoCommit = currCommit;
    }

    
   
    protected void autoCommit() throws RepositoryException {
        if (isAutoCommit()) {
            commit();
        }
    }
    
    
    private int boolToInt(boolean b)
    {
    	if(b) return 1;
    	return 0;
    }

    protected void removeWithoutCommit(Statement st,
    		Resource... contexts) throws RepositoryException {
    	if (contexts.length == 0 && st.getContext() != null) {
    		contexts = new Resource[] { st.getContext() };
    	}
    
    	removeWithoutCommit(st.getSubject(), st.getPredicate(), st.getObject(), contexts);
    }

    protected void removeWithoutCommit(Resource subject,
    		org.eclipse.rdf4j.model.IRI predicate, Value object, Resource... contexts)
    	throws RepositoryException{
    	
    	throw new RepositoryException("Removal not supported!");
    }



	@Override
    public void clear(Resource... contexts) throws RepositoryException {
		//Removes all statements from a specific contexts in the repository. 
        remove(null, null, null, contexts);
	}

	@Override
    public void clearNamespaces() throws RepositoryException {
		//Removes all namespace declarations from the repository. 
		remove(null, null, null,(Resource[]) null);
		
	}

	@Override
    public void close() throws RepositoryException {
		//Closes the connection, freeing resources. 
		//If the connection is not in autoCommit mode, 
		//all non-committed operations will be lost. 
		isOpen = false;
			try {
				questConn.close();
			} catch (Exception e) {
				throw new RepositoryException(e);
			}
	} 
	
	@Override
    public void commit() throws RepositoryException {
		// Commits all updates that have been performed as part of this
		// connection sofar.
		if (isActive()) {
			try {
				// System.out.println("QuestConn commit..");
				questConn.commit();
				this.isActive = false;
			} catch (OBDAException e) {
				throw new RepositoryException(e);
			}
		} else {
			throw new RepositoryException(
					"Connection does not have an active transaction.");
		}
	}

	@Override
    public void export(RDFHandler handler, Resource... contexts)
			throws RepositoryException, RDFHandlerException {
		//Exports all explicit statements in the specified contexts to the supplied RDFHandler. 
        exportStatements(null, null, null, false, handler, contexts);
	}

    @Override
    public void exportStatements(Resource subj, IRI pred, Value obj,
                                 boolean includeInferred, RDFHandler handler, Resource... contexts)
            throws RepositoryException, RDFHandlerException {
        //Exports all statements with a specific subject, predicate
        //and/or object from the repository, optionally from the specified contexts.
        RepositoryResult<Statement> stms = getStatements(subj, pred, obj, includeInferred, contexts);

        handler.startRDF();
        // handle
        if (stms != null) {
            while (stms.hasNext()) {
                Statement st = stms.next();
                if (st != null)
                    handler.handleStatement(st);
            }
        }
        handler.endRDF();

    }

	@Override
    public RepositoryResult<Resource> getContextIDs()
			throws RepositoryException {
		//Gets all resources that are used as content identifiers. 
		//Care should be taken that the returned RepositoryResult 
		//is closed to free any resources that it keeps hold of. 
		List<Resource> contexts = new LinkedList<Resource>();
		return new RepositoryResult<Resource>(new CloseableIteratorIteration<Resource, RepositoryException>(contexts.iterator()));
	}

	@Override
    public String getNamespace(String prefix) throws RepositoryException {
		//Gets the namespace that is associated with the specified prefix, if any. 
		return repository.getNamespace(prefix);
	}

	@Override
    public RepositoryResult<Namespace> getNamespaces()
			throws RepositoryException {
		//Gets all declared namespaces as a RepositoryResult of Namespace objects. 
		//Each Namespace object consists of a prefix and a namespace name. 
		Set<Namespace> namespSet = new HashSet<Namespace>();
		Map<String, String> namesp = repository.getNamespaces();
		Set<String> keys = namesp.keySet();
		for (String key : keys)
		{
			//convert into namespace objects
			namespSet.add(new NamespaceImpl(key, namesp.get(key)));
		}
		return new RepositoryResult<Namespace>(new CloseableIteratorIteration<Namespace, RepositoryException>(
                namespSet.iterator()));
	}

	@Override
    public ParserConfig getParserConfig() {
		//Returns the parser configuration this connection uses for Rio-based operations. 
		return rdfParser.getParserConfig();
	}

	@Override
    public Repository getRepository() {
		//Returns the Repository object to which this connection belongs. 
		return this.repository;
	}

	@Override
    public RepositoryResult<Statement> getStatements(Resource subj, org.eclipse.rdf4j.model.IRI pred,
                                                     Value obj, boolean includeInferred, Resource... contexts)
			throws RepositoryException {
		//Gets all statements with a specific subject, 
		//predicate and/or object from the repository.
		//The result is optionally restricted to the specified set of named contexts. 
		
		//construct query for it
		String queryString = "CONSTRUCT {";
		String s="", p="", o="";
		if (subj == null)
			s = "?s ";
		else {		
			s = subj.toString();
			if (subj instanceof IRI) {
				s = "<" + s + ">";
			}
		}
		
		if (pred == null)
			p = " ?p ";
		else 
			p = "<" + pred.stringValue()  + ">";
		if (obj == null)
			o = " ?o ";
		else {
			if (obj instanceof IRI) {
				o = "<" + obj.stringValue() + ">";
			} else {
				o = obj.stringValue();
			}
		}
		queryString+= s+p+o+"} WHERE {"+s+p+o+"}";	
		
		//execute construct query
		try {
			List<Statement> list = new LinkedList<Statement>();
			
			if (contexts.length == 0 || (contexts.length > 0 && contexts[0] == null)) {
					GraphQuery query = prepareGraphQuery(QueryLanguage.SPARQL,
							queryString);
					GraphQueryResult result = query.evaluate();

					// System.out.println("result: "+result.hasNext());
					while (result.hasNext())
						list.add(result.next());
					// result.close();
			}
			CloseableIteration<Statement, RepositoryException> iter = new CloseableIteratorIteration<Statement, RepositoryException>(
					list.iterator());
			RepositoryResult<Statement> repoResult = new RepositoryResult<Statement>(iter);

			return repoResult;
		} catch (MalformedQueryException e) {
			throw new RepositoryException(e);

		} catch (QueryEvaluationException e) {
			throw new RepositoryException(e);

		}
	}

	@Override
    public ValueFactory getValueFactory() {
		//Gets a ValueFactory for this OntopRepositoryConnection.
		return new ValueFactoryImpl();
	}

	@Override
    public boolean hasStatement(Statement st, boolean includeInferred, Resource... contexts)
			throws RepositoryException {
		//Checks whether the repository contains the specified statement,
		//optionally in the specified contexts. 
		return hasStatement(st.getSubject(), st.getPredicate(), st
                .getObject(), includeInferred, contexts);
	}

	@Override
    public boolean hasStatement(Resource subj, org.eclipse.rdf4j.model.IRI pred, Value obj,
                                boolean includeInferred, Resource... contexts) throws RepositoryException {
		//Checks whether the repository contains statements with a specific subject, 
		//predicate and/or object, optionally in the specified contexts. 
		    RepositoryResult<Statement> stIter = getStatements(subj, pred,
                     obj, includeInferred, contexts);
             try {
                 return stIter.hasNext();
             } finally {
                 stIter.close();
             }
         }



	@Override
    public boolean isAutoCommit() throws RepositoryException {
		//Checks whether the connection is in auto-commit mode. 
		return this.autoCommit;
	}

	@Override
    public boolean isEmpty() throws RepositoryException {
		//Returns true if this repository does not contain any (explicit) statements. 
		return size() == 0;
	}

	@Override
    public boolean isOpen() throws RepositoryException {
		//Checks whether this connection is open. 
		//A connection is open from the moment it is created until it is closed. 
		return this.isOpen;
	}

	@Override
    public BooleanQuery prepareBooleanQuery(QueryLanguage ql, String query)
			throws RepositoryException, MalformedQueryException {
		//Prepares true/false queries. In case the query contains 
		//relative IRIs that need to be resolved against an external base IRI,
		//one should use prepareBooleanQuery(QueryLanguage, String, String) instead. 
        return prepareBooleanQuery(ql, query, null);
    }

	@Override
    public BooleanQuery prepareBooleanQuery(QueryLanguage ql, String queryString,
                                            String baseIRI) throws RepositoryException, MalformedQueryException {
		//Prepares true/false queries. 
		if (ql != QueryLanguage.SPARQL)
			throw new MalformedQueryException("SPARQL query expected!");

		return new OntopBooleanQuery(queryString, baseIRI, questConn);
		
	}

	@Override
    public GraphQuery prepareGraphQuery(QueryLanguage ql, String queryString)
			throws RepositoryException, MalformedQueryException {
		//Prepares queries that produce RDF graphs. In case the query 
		//contains relative IRIs that need to be resolved against an
		//external base IRI, one should use prepareGraphQuery(QueryLanguage, String, String) instead.
		return prepareGraphQuery(ql, queryString, null);
	}

	@Override
    public GraphQuery prepareGraphQuery(QueryLanguage ql, String queryString,
                                        String baseIRI) throws RepositoryException, MalformedQueryException {
		//Prepares queries that produce RDF graphs. 
		if (ql != QueryLanguage.SPARQL)
			throw new MalformedQueryException("SPARQL query expected!");

		return new OntopGraphQuery(queryString, baseIRI, questConn);
			
	}

	@Override
    public Query prepareQuery(QueryLanguage ql, String query)
			throws RepositoryException, MalformedQueryException {
		//Prepares a query for evaluation on this repository (optional operation).
		//In case the query contains relative IRIs that need to be resolved against
		//an external base IRI, one should use prepareQuery(QueryLanguage, String, String) instead.
        return prepareQuery(ql, query, null);
    }

	@Override
    public Query prepareQuery(QueryLanguage ql, String queryString, String baseIRI)
			throws RepositoryException, MalformedQueryException {
		if (ql != QueryLanguage.SPARQL)
			throw new MalformedQueryException("SPARQL query expected! ");
		
		ParsedQuery q = QueryParserUtil.parseQuery(QueryLanguage.SPARQL, queryString, baseIRI);
		
		if (q instanceof ParsedTupleQuery)
			return prepareTupleQuery(ql,queryString, baseIRI);
		else if (q instanceof ParsedBooleanQuery)
			return prepareBooleanQuery(ql, queryString, baseIRI);
		else if (q instanceof ParsedGraphQuery)
			return prepareGraphQuery(ql, queryString, baseIRI);
		else 
			throw new MalformedQueryException("Unrecognized query type. " + queryString);
		
	}

	@Override
    public TupleQuery prepareTupleQuery(QueryLanguage ql, String query)
			throws RepositoryException, MalformedQueryException {
		//Prepares a query that produces sets of value tuples. 
		//In case the query contains relative IRIs that need to be
		//resolved against an external base IRI, one should use
		//prepareTupleQuery(QueryLanguage, String, String) instead. 
        return this.prepareTupleQuery(ql, query, "");
    }

	@Override
    public TupleQuery prepareTupleQuery(QueryLanguage ql, String queryString,
                                        String baseIRI) throws RepositoryException, MalformedQueryException {
		//Prepares a query that produces sets of value tuples. 
		if (ql != QueryLanguage.SPARQL)
			throw new MalformedQueryException("SPARQL query expected!");

			return new OntopTupleQuery(queryString, baseIRI, questConn);

	}

	@Override
    public Update prepareUpdate(QueryLanguage arg0, String arg1)
			throws RepositoryException, MalformedQueryException {
		// TODO Auto-generated method stub
		//Prepares an Update operation. 
		return null;
	}

	@Override
    public Update prepareUpdate(QueryLanguage arg0, String arg1, String arg2)
			throws RepositoryException, MalformedQueryException {
		// TODO Auto-generated method stub
		//Prepares an Update operation. 
		return null;
	}

	@Override
    public void remove(Statement st, Resource... contexts)
			throws RepositoryException {
		//Removes the supplied statement from the specified contexts in the repository. 
		   OpenRDFUtil.verifyContextNotNull(contexts);
           removeWithoutCommit(st, contexts);
           autoCommit();

	}

	@Override
    public void remove(Iterable<? extends Statement> statements, Resource... contexts)
			throws RepositoryException {
		//Removes the supplied statements from the specified contexts in this repository.
		 OpenRDFUtil.verifyContextNotNull(contexts);

         begin();

         try {
             for (Statement st : statements) {
                 remove(st, contexts);
             }
         } catch (RuntimeException e) {
             if (autoCommit) {
                 rollback();
             }
             throw e;
         } finally {
             autoCommit();
         }

	}


	@Override
    public void remove(Resource subject, org.eclipse.rdf4j.model.IRI predicate, Value object, Resource... contexts)
			throws RepositoryException {
		//Removes the statement(s) with the specified subject, predicate and object 
		//from the repository, optionally restricted to the specified contexts. 
		  OpenRDFUtil.verifyContextNotNull(contexts);
          removeWithoutCommit(subject, predicate, object, contexts);
          autoCommit();

	}

	@Override
    public void removeNamespace(String key) throws RepositoryException {
		//Removes a namespace declaration by removing the association between a prefix and a namespace name. 
		repository.removeNamespace(key);
	}

	@Override
    public void rollback() throws RepositoryException {
		// Rolls back all updates that have been performed as part of this
		// connection sofar.
		if (isActive()) {
			try {
				this.questConn.rollBack();
				this.isActive = false;
			} catch (OBDAException e) {
				throw new RepositoryException(e);
			}
		} else {
			throw new RepositoryException(
					"Connection does not have an active transaction.");
		}
	}

	@Override
    public void setAutoCommit(boolean autoCommit) throws RepositoryException {
		//Enables or disables auto-commit mode for the connection. 
		//If a connection is in auto-commit mode, then all updates 
		//will be executed and committed as individual transactions. 
		//Otherwise, the updates are grouped into transactions that are 
		// terminated by a call to either commit() or rollback().
		// By default, new connections are in auto-commit mode.
		if (autoCommit == this.autoCommit) {
			return;
		}
		if (isActive()) {
			this.autoCommit = autoCommit;
			try {
				this.questConn.setAutoCommit(autoCommit);
			} catch (OBDAException e) {
				throw new RepositoryException(e);

			}

			// if we are switching from non-autocommit to autocommit mode,
			// commit any
			// pending updates
			if (autoCommit) {
				commit();
			}
		} else if (!autoCommit) {
			// begin a transaction
			begin();
		}

	}

	@Override
    public void setNamespace(String key, String value)
			throws RepositoryException {
		//Sets the prefix for a namespace. 
		repository.setNamespace(key, value);
		
	}

	@Override
    public void setParserConfig(ParserConfig config) {
		//Set the parser configuration this connection should use for RDFParser-based operations. 
		rdfParser.setParserConfig(config);
	}

	@Override
    public long size(Resource... contexts) throws RepositoryException {
		//Returns the number of (explicit) statements that are in the specified contexts in this repository. 
		return 0;
	}


	/**
	 * Call this method to start a transaction. Have to call commit() or
	 * rollback() to mark end of transaction.
	 */
	@Override
	public void begin() throws RepositoryException {
		// TODO Auto-generated method stub
		if (!isOpen()) {
				throw new RepositoryException("Connection was closed.");
		}
		isActive = true;
	}


	/**
	 * A boolean flag signaling when a transaction is active.
	 */
	@Override
	public boolean isActive() throws UnknownTransactionStateException,
			RepositoryException {
		return this.isActive;
	}

	@Override
	public void setIsolationLevel(IsolationLevel level) throws IllegalStateException {
        if(level != IsolationLevels.NONE)
            throw new UnsupportedOperationException();
	}

	@Override
	public IsolationLevel getIsolationLevel() {
		return IsolationLevels.NONE;
	}

	@Override
	public void begin(IsolationLevel level) throws RepositoryException {
        // do nothing
	}


	@Override
	public <E extends Exception> void add(
			org.eclipse.rdf4j.common.iteration.Iteration<? extends Statement, E> statements, Resource... contexts)
			throws RepositoryException, E {
		throw new UnsupportedOperationException(ONTOP_VIRTUAL_REPOSITORY_IS_READ_ONLY);
	}

	@Override
	public <E extends Exception> void remove(
			org.eclipse.rdf4j.common.iteration.Iteration<? extends Statement, E> statements, Resource... contexts)
			throws RepositoryException, E {
        throw new UnsupportedOperationException(ONTOP_VIRTUAL_REPOSITORY_IS_READ_ONLY);
	}

}

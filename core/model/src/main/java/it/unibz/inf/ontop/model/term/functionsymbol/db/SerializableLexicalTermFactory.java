package it.unibz.inf.ontop.model.term.functionsymbol.db;

import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.MultitypedInputUnarySPARQLFunctionSymbolImpl;

import java.io.Serializable;

/**
 * @author Lorenz Buehmann
 */
public interface SerializableLexicalTermFactory extends MultitypedInputUnarySPARQLFunctionSymbolImpl.TriFunction<TermFactory, ImmutableTerm, ImmutableTerm, ImmutableFunctionalTerm>, Serializable {}

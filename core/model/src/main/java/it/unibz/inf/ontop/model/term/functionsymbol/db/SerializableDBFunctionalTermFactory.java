package it.unibz.inf.ontop.model.term.functionsymbol.db;

import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;

import java.io.Serializable;
import java.util.function.BiFunction;

public interface SerializableDBFunctionalTermFactory extends BiFunction<TermFactory, ImmutableTerm, ImmutableFunctionalTerm>, Serializable {}

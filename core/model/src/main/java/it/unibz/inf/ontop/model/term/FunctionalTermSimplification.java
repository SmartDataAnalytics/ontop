package it.unibz.inf.ontop.model.term;

import it.unibz.inf.ontop.com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.impl.FunctionalTermSimplificationImpl;

public interface FunctionalTermSimplification {

    ImmutableTerm getSimplifiedTerm();

    ImmutableSet<Variable> getSimplifiableVariables();

    static FunctionalTermSimplification create(ImmutableTerm simplifiedTerm,
                                               ImmutableSet<Variable> simplifiableVariables) {
        return new FunctionalTermSimplificationImpl(simplifiedTerm, simplifiableVariables);
    }
}

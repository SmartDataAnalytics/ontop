package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.Variable;

import java.util.Optional;

/**
 * TODO: explain
 */
public interface NodeTransformationProposal {

    NodeTransformationProposedState getState();

    Optional<QueryNode> getOptionalNewNodeOrReplacingChild();

    ImmutableSet<Variable> getNullVariables();
}

package it.unibz.inf.ontop.generation.algebra;

import it.unibz.inf.ontop.com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.term.Variable;

public interface SQLUnionExpression extends SQLExpression {

    ImmutableSet<Variable> getProjectedVariables();

    ImmutableList<? extends SQLExpression> getSubExpressions();
}

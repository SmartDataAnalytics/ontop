package it.unibz.inf.ontop.model.term;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.model.term.functionsymbol.BooleanFunctionSymbol;
import it.unibz.inf.ontop.model.type.TermTypeInference;

import java.util.Optional;


public interface ImmutableExpression extends ImmutableFunctionalTerm {

    @Override
    BooleanFunctionSymbol getFunctionSymbol();

    /**
     * Flattens AND expressions.
     */
    ImmutableSet<ImmutableExpression> flattenAND();

    /**
     * Flattens OR expressions.
     */
    ImmutableSet<ImmutableExpression> flattenOR();

    /**
     * Generalization of flattening (AND, OR, etc.).
     *
     * It is the responsibility of the caller to make sure such a flattening makes sense.
     */
    ImmutableSet<ImmutableExpression> flatten(BooleanFunctionSymbol operator);

    boolean isVar2VarEquality();

    default Optional<TermTypeInference> inferTermType(ImmutableList<Optional<TermTypeInference>> actualArgumentTypes) {
        try {
            BooleanFunctionSymbol functionSymbol = getFunctionSymbol();
            return functionSymbol.inferTypeFromArgumentTypes(actualArgumentTypes);
        } catch (FatalTypingException e) {
            throw new FatalTypingException(this, e);
        }
    }
}

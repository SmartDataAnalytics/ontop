package it.unibz.inf.ontop.spec.mapping.transformer;

import it.unibz.inf.ontop.com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;

public interface MappingSameAsInverseRewriter {

    ImmutableList<MappingAssertion> rewrite(ImmutableList<MappingAssertion> mapping);
}

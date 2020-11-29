package it.unibz.inf.ontop.spec.mapping.transformer;

import it.unibz.inf.ontop.com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;

public interface MappingWithProvenanceTransformer {

    ImmutableList<MappingAssertion> transform(ImmutableList<MappingAssertion> mapping) throws MappingException;
}

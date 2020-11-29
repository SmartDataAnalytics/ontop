package it.unibz.inf.ontop.spec.ontology;

import it.unibz.inf.ontop.com.google.common.collect.ImmutableList;

public interface OntologyABox {

    ImmutableList<ClassAssertion> getClassAssertions();

    ImmutableList<ObjectPropertyAssertion> getObjectPropertyAssertions();

    ImmutableList<DataPropertyAssertion> getDataPropertyAssertions();

    ImmutableList<AnnotationAssertion> getAnnotationAssertions();
}

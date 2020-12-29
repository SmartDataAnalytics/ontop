package it.unibz.inf.ontop.spec.ontology;


import it.unibz.inf.ontop.com.google.common.collect.ImmutableSet;

public interface Ontology  {

    ClassifiedTBox tbox();

    Datatype getDatatype(String uri);

    ImmutableSet<RDFFact> abox();

    /**
     * annotation properties
     *
     * @return annotation properties
     */
    OntologyVocabularyCategory<AnnotationProperty> annotationProperties();
}

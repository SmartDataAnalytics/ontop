package it.unibz.inf.ontop.docker.postgres;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;


/**
 * Created by elem on 21/09/15.
 */
public class MetaMappingExpanderTest extends AbstractVirtualModeTest {

        final static String owlFile = "/pgsql/EPNet.owl";
        final static String obdaFile = "/pgsql/EPNet.obda";
        final static String propertyFile = "/pgsql/EPNet.properties";

    private static OntopOWLReasoner REASONER;
    private static OntopOWLConnection CONNECTION;

    @BeforeClass
    public static void before() throws OWLOntologyCreationException {
        REASONER = createReasoner(owlFile, obdaFile, propertyFile);
        CONNECTION = REASONER.getConnection();
    }

    @Override
    protected OntopOWLStatement createStatement() throws OWLException {
        return CONNECTION.createStatement();
    }

    @AfterClass
    public static void after() throws OWLException {
        CONNECTION.close();
        REASONER.dispose();
    }

    @Test
    public void testQuery() throws Exception {

        /*
		 * Get the book information that is stored in the database
		 */
//        String sparqlQuery = "PREFIX : <http://www.semanticweb.org/ontologies/2015/1/EPNet-ONTOP_Ontology#>\n" +
//                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
//                "PREFIX dcterms: <http://purl.org/dc/terms/>\n" +
//                "select *\n" +
//                "where {\n" +
//                "?x rdf:type :Amphora .\n" +
//                "?x :hasProductionPlace ?pl .\n" +
//                "?pl rdf:type :Place .\n" +
//                "?pl dcterms:title \"La Corregidora\" .\n" +
//                "?pl :hasLatitude ?lat .\n" +
//                "?pl :hasLongitude ?long\n" +
//                "}\n" +
//                "limit 50\n";
        String sparqlQuery = "PREFIX : <http://www.semanticweb.org/ontologies/2015/1/EPNet-ONTOP_Ontology#>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX dcterms: <http://purl.org/dc/terms/>\n" +
                "select ?x\n" +
                "where {\n" +
                "?x rdf:type :AmphoraSection2026 .\n" +
                "}\n" +
                "limit 5\n";

        runQuery(sparqlQuery);

        }
}

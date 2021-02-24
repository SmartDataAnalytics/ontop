package it.unibz.inf.ontop.rdf4j.repository;

import it.unibz.inf.ontop.com.google.common.collect.ImmutableList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

public class SQLViewPersonTestAggregation extends AbstractRDF4JTest {
    private static final String OBDA_FILE = "/person/person_sql_views_aggregation_query.obda";
    private static final String SQL_SCRIPT = "/person/person_sql_views_db.sql";
    private static final String VIEW_FILE = "/person/views/sql_views_aggregation_query.json";

    @BeforeClass
    public static void before() throws IOException, SQLException {
        initOBDA(SQL_SCRIPT, OBDA_FILE, null, null, VIEW_FILE);
    }

    @AfterClass
    public static void after() throws SQLException {
        release();
    }

    /**
     * Test correct columns are projected for query with aggregation
     */
    @Test
    public void testPersonSelect() throws Exception {
        String query = "PREFIX : <http://person.example.org/>\n" +
                "PREFIX  xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "SELECT  ?v \n" +
                "WHERE {\n" +
                " ?x a :Person . \n" +
                " ?x :country ?v . \n" +
                "}";
        runQueryAndCompare(query, ImmutableList.of("ie", "mt"));
    }
}


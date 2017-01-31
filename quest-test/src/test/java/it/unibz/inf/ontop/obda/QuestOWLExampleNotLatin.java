package it.unibz.inf.ontop.obda;

/*
 * #%L
 * ontop-quest-owlapi
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.owlrefplatform.core.SQLExecutableQuery;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLObject;


public class QuestOWLExampleNotLatin {
	
	/*
	 * Use the sample database using H2 from
	 * https://github.com/ontop/ontop/wiki/InstallingTutorialDatabases
	 * 
	 * Please use the pre-bundled H2 server from the above link
	 *
	 * Test with not latin Character
	 * 
	 */
	final String owlfile = "src/test/resources/example/exampleBooksNotLatin.owl";
	final String obdafile = "src/test/resources/example/exampleBooksNotLatin.obda";

    @Test
	public void runQuery() throws Exception {

		/*
		 * Create the instance of Quest OWL reasoner.
		 */
        QuestOWLFactory factory = new QuestOWLFactory();
        QuestConfiguration config = QuestConfiguration.defaultBuilder()
				.nativeOntopMappingFile(obdafile)
				.ontologyFile(owlfile)
				.build();
        QuestOWL reasoner = factory.createReasoner(config);

		/*
		 * Prepare the data connection for querying.
		 */
		OntopOWLConnection conn = reasoner.getConnection();
		OntopOWLStatement st = conn.createStatement();

		/*
		 * Get the book information that is stored in the database
		 */
		String sparqlQuery =
				"PREFIX : <http://meraka/moss/exampleBooks.owl#> \n" +
				"SELECT DISTINCT ?x ?title ?author ?y ?genre ?edition \n" +
				"WHERE { ?x a :книга; :title ?title; :النوع ?genre; :writtenBy ?y; :hasÉdition ?z. \n" +
				"		 ?y a :作者; :name ?author. \n" +
				"		 ?z a :Édition; :editionNumber ?edition }";

		try {
            long t1 = System.currentTimeMillis();
			QuestOWLResultSet rs = st.executeTuple(sparqlQuery);
			int columnSize = rs.getColumnCount();
			while (rs.nextRow()) {
				for (int idx = 1; idx <= columnSize; idx++) {
					OWLObject binding = rs.getOWLObject(idx);
					System.out.print(binding.toString() + ", ");
				}
				System.out.print("\n");
			}
			rs.close();
            long t2 = System.currentTimeMillis();

			/*
			 * Print the query summary
			 */
			QuestOWLStatement qst = st;
			String sqlQuery = ((SQLExecutableQuery)qst.getExecutableQuery(sparqlQuery)).getSQL();

			System.out.println();
			System.out.println("The input SPARQL query:");
			System.out.println("=======================");
			System.out.println(sparqlQuery);
			System.out.println();
			
			System.out.println("The output SQL query:");
			System.out.println("=====================");
			System.out.println(sqlQuery);

            System.out.println("Query Execution Time:");
            System.out.println("=====================");
            System.out.println((t2-t1) + "ms");
			
		} finally {
			
			/*
			 * Close connection and resources
			 */
			if (st != null && !st.isClosed()) {
				st.close();
			}
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
			reasoner.dispose();
		}
	}

	/**
	 * Main client program
	 */
	public static void main(String[] args) {
		try {
			QuestOWLExampleNotLatin example = new QuestOWLExampleNotLatin();
			example.runQuery();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;

import static java.util.stream.Collectors.joining;

/**
 * 
 * @author NK510 (caresssd@hermes.cam.ac.uk)
 * 
 *         Perform SPARQL query on book database under inference mode via Book
 *         ontology. This implementation is based on ontop example available at
 *         https://github.com/ontop/ontop-api-examples. This example shows that
 *         query result is consistent. In case of inconsistency, the query
 *         result is empty.
 *
 */
public class SPARQLConsistentInference {

	/**
	 * Book ontology
	 */
	final String owlFile = "./resources/example/books/exampleBooks.owl";
	/**
	 * Mapping between Book ontology and book database
	 */
	final String obdaFile = "./resources/example/books/bk_code_audio_book_inference.obda";
	/**
	 * SPARQL query performed on book database via Book ontology.
	 */
	final String sparqlFile = "./resources/example/books/book_id.rq";

	public static void main(String[] args) {
		try {
			SPARQLConsistentInference sparqlInferenceMode = new SPARQLConsistentInference();
			sparqlInferenceMode.runSPARQLInferenceMode();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * Run SPARQL query under inference mode.
	 * 
	 * @author NK510 (caresssd@hermes.cam.ac.uk)
	 * @throws Exception
	 */
	public void runSPARQLInferenceMode() throws Exception {

		OWLOntology ontology = OWLManager.createOWLOntologyManager()
				.loadOntologyFromOntologyDocument(new File(owlFile));

		OBDAModel obdaModel = new MappingLoader().loadFromOBDAFile(obdaFile);

		QuestOWLFactory factory = new QuestOWLFactory();

		QuestOWLConfiguration config = QuestOWLConfiguration.builder().obdaModel(obdaModel).build();

		String sparqlQuery = Files.lines(Paths.get(sparqlFile)).collect(joining("\n"));

		try (QuestOWL reasoner = factory.createReasoner(ontology, config);
				QuestOWLConnection conn = reasoner.getConnection();
				QuestOWLStatement st = conn.createStatement();
				QuestOWLResultSet rs = st.executeTuple(sparqlQuery)) {
			int columnSize = rs.getColumnCount();
			BufferedWriter bufferOutput = new BufferedWriter(
					new FileWriter("./resources/example/sparql_consistent_inference_result.txt"));
			bufferOutput.write("reasoner.isQuestConsistent() :" + reasoner.isQuestConsistent());
			bufferOutput.write("\n");
			bufferOutput.write("\n");
			bufferOutput.write("Query results: ");
			bufferOutput.write("\n");
			bufferOutput.write("\n");
			while (rs.nextRow()) {

				for (int i = 1; i <= columnSize; i++) {
					OWLObject result = rs.getOWLObject(i);
					bufferOutput.write(result.toString());
					bufferOutput.write("\n");

				}
				System.out.print("\n");
			}

			bufferOutput.close();
			rs.close();
		}
	}
}
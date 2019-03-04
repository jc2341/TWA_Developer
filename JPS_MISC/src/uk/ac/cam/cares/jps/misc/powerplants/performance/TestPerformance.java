package uk.ac.cam.cares.jps.misc.powerplants.performance;

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

/**
 * The data for this performance test were generated as follows: am Excel table with about 3800 power plants was imported to a database within PostgresSQL. 
 * Then an OBDA mapping was created from that database to OWL with the help of the Ontop framework. An option of Ontop was used to materialize the relational
 * data to one big rdf file (around 28 MB) which contains all information about the 3800 power plants. Finally, this rdf file is imported to Fuseki or RDF4J. 
 * 
 * 
 * @author Andreas
 *
 */
public class TestPerformance extends TestCase {

	private static final String IRI_TEST_PLANT = "http://www.theworldavatar.com/kb/powerplants/powerplant_test";
	
	private String datasetUrl = null;

	public static void main(String[] args) throws IOException {
		new TestPerformance().start(args);
	}
	
	private void start(String[] args) throws IOException {
		
		System.out.println("starting with arguments:");
		
		for (String current : args) {
			System.out.println(current);
		}
		
		if (args.length == 0) {
			System.out.println("\nplease call the jar file with arguments: <command number> <url for dataset> (<new emission value>)");
			System.out.println("1 = list all power plants");
			System.out.println("2 = set new emission value to all power plants ");
			System.out.println("3 = sum up emission values for all power plants ");
			System.out.println("4 = sum up and set emission values for all power plants ");
			System.out.println("99 = insert emission triples");
			System.out.println("example url for fuseki = http://localhost:3030/<dataset name>");
			System.out.println("example url for rdf4j  = http://localhost:8080/rdf4j-server/repositories/<dataset name>");
			return;
		}
		
		int command = Integer.valueOf(args[0]);
		datasetUrl = args[1];
		
		switch(command) {
		case 1: 
			testPerformanceGetAllPowerPlants();
			break;
		case 2: 
			double emissionValue = Double.valueOf(args[2]);
			createModel().loopOnPlants(10000, false, true, emissionValue);
			break;
		case 3: 
			createModel().loopOnPlants(10000, true, false, 0.);
			break;
		case 4: 
			emissionValue = Double.valueOf(args[2]);
			createModel().loopOnPlants(10000, true, true, emissionValue);
			break;
		case 99: 
			testPerformanceInsertEmissionTriplesForAllPlants();
			break;
		default:
			System.out.println("unknown command number");
		}
		
		System.out.println("\n\nfinished for arguments:");
		
		for (String current : args) {
			System.out.println(current);
		}
		
	}
	
	private SparqlOverHttpService createSparqlOverHttpService() {
		
//		SparqlStoreType type = SparqlStoreType.FUSEKI;
//		String uriForQuery = "http://localhost:3030/matpp/query";
//		String uriForUpdate = "http://localhost:3030/matpp/update";
//		String uriForQuery = "http://localhost:3030/matppinmemory/query";
//		String uriForUpdate = "http://localhost:3030/matppinmemory/update";
		
//		SparqlStoreType type = SparqlStoreType.RDF4J;
//		String uriForQuery = "http://localhost:8080/rdf4j-server/repositories/matpp";
//		String uriForUpdate = "http://localhost:8080/rdf4j-server/repositories/matpp/statements";
//		String uriForQuery = "http://localhost:8080/rdf4j-server/repositories/matppinmemory";
//		String uriForUpdate = "http://localhost:8080/rdf4j-server/repositories/matppinmemory/statements";
		
//		SparqlOverHttpService service = new SparqlOverHttpService(type, uriForQuery, uriForUpdate);
		
		String testDatasetUrl = null;
//		String testDatasetUrl = "http://localhost:3030/matpp";
//		String testDatasetUrl = "http://localhost:3030/matppinmemory";
//		String testDatasetUrl = "http://localhost:8080/rdf4j-server/repositories/matpp";
//		String testDatasetUrl = "http://localhost:8080/rdf4j-server/repositories/matppinmemory";
		
		if (datasetUrl != null) {
			testDatasetUrl = datasetUrl;
		}
	
		return new SparqlOverHttpService(testDatasetUrl);
	}
	
	private PowerPlantQueries createModel() {
		return new PowerPlantQueries(createSparqlOverHttpService());
	}
	
	public void testPostUpdateWithDATA() {
		
		String messageBody = "PREFIX powerplant:<http://www.theworldavatar.com/ontology/ontoeip/powerplants/PowerPlant.owl#> \r\n" + 
				"PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> \r\n" + 
				"PREFIX j3:<http://www.theworldavatar.com/ontology/ontocape/upper_level/technical_system.owl#> \r\n" + 
				"PREFIX j4:<http://www.theworldavatar.com/ontology/ontoeip/system_aspects/system_realization.owl#> \r\n" + 
				"PREFIX j5:<http://www.theworldavatar.com/ontology/ontoeip/system_aspects/system_performance.owl#> \r\n" + 
				"INSERT DATA { <http://www.theworldavatar.com/kb/powerplants/powerplant_my> a powerplant:PowerPlant.} ";
		
		createSparqlOverHttpService().executePost(messageBody);
	}
	
	public void testPostDeleteWithDATA() {
		
		String messageBody = "PREFIX powerplant:<http://www.theworldavatar.com/ontology/ontoeip/powerplants/PowerPlant.owl#> \r\n" + 
				"PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> \r\n" + 
				"PREFIX j3:<http://www.theworldavatar.com/ontology/ontocape/upper_level/technical_system.owl#> \r\n" + 
				"PREFIX j4:<http://www.theworldavatar.com/ontology/ontoeip/system_aspects/system_realization.owl#> \r\n" + 
				"PREFIX j5:<http://www.theworldavatar.com/ontology/ontoeip/system_aspects/system_performance.owl#> \r\n" + 
				"DELETE DATA { <http://www.theworldavatar.com/kb/powerplants/powerplant_my> a powerplant:PowerPlant.} ";
		
		createSparqlOverHttpService().executePost(messageBody);
	}
	
	public void testPostDeleteWithWHERE() {
		
		String messageBody = "PREFIX powerplant:<http://www.theworldavatar.com/ontology/ontoeip/powerplants/PowerPlant.owl#> \r\n" + 
				"PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> \r\n" + 
				"PREFIX j3:<http://www.theworldavatar.com/ontology/ontocape/upper_level/technical_system.owl#> \r\n" + 
				"PREFIX j4:<http://www.theworldavatar.com/ontology/ontoeip/system_aspects/system_realization.owl#> \r\n" + 
				"PREFIX j5:<http://www.theworldavatar.com/ontology/ontoeip/system_aspects/system_performance.owl#> \r\n" + 
				"DELETE { <http://www.theworldavatar.com/kb/powerplants/powerplant_my> ?pred ?obj } \r\n" +
				"WHERE { <http://www.theworldavatar.com/kb/powerplants/powerplant_my> ?pred ?obj .} \r\n";
		
		createSparqlOverHttpService().executePost(messageBody);
	}

	public void testOnePlantQuery() throws IOException {
			
		//Double estimatedEmission = createModel().queryPowerplantProperty("http://www.theworldavatar.com/kb/powerplants/powerplant_1");
		Double estimatedEmission = createModel().queryEmission("http://www.theworldavatar.com/kb/powerplants/powerplant_1001");
		System.out.println("Estimated Emission = " + estimatedEmission);
	}
	
	public void testPerformanceLoopOnPlantsWithSelectOnly() throws IOException {
		
		createModel().loopOnPlants(10000, true, false, 1.);
	}
	
	public void testPerformanceLoopOnPlantsWithUpdateOnly() throws IOException {
		
		createModel().loopOnPlants(10000, false, true, 2.);
	}
	
	public void testPerformanceLoopOnPlantSelectAndUpdate() {
		
		// https://jena.apache.org/documentation/fuseki2/soh.html#soh-sparql-update
		// https://www.baeldung.com/httpclient-post-http-request
		// https://www.w3.org/TR/sparql11-protocol/#update-bindings-http-examples
		// https://www.w3.org/TR/sparql11-update/#deleteData
		
		createModel().loopOnPlants(10000, true, true, 3.);
	}
	
	public void testOneTestPlantInsertEmissionTriples() {
			
		String query = PowerPlantQueries.SPARQL_PREFIXES
				+ "INSERT DATA { \r\n" 
				+ "<" + IRI_TEST_PLANT + "> a powerplant:PowerPlant .\r\n"
				+ "}";
			
		// create test plant
		createSparqlOverHttpService().executePost(query);
		
		// add emissions
		insertEmissionTriples(IRI_TEST_PLANT);
	}

	/**
	 * Inserting exactly the same triples (with object type relations and same IRIs) does not lead to duplication of already existing triples (what is for datatype relations?)
	 */
	public void testPerformanceInsertEmissionTriplesForAllPlants() {
		PowerPlantQueries model = createModel();
		List<String> plants = model.queryAllPowerplants();
		int i=1;
		for (String current: plants) {
			System.out.println("inserting for " + i + "th plant: " + current);
			insertEmissionTriples(current);
			i++;
		}
		
		String query = PowerPlantQueries.SPARQL_PREFIXES 
				+ "SELECT (COUNT(*) as ?plantcount) WHERE { ?plant technical_system:realizes ?generation . ?generation system_performance:hasEmission ?emission . ?emission system:hasValue ?emissionvalue . ?emissionvalue system:numericalValue ?emissionvaluenum . }";
		
		String result = createSparqlOverHttpService().executeGet(query);
		System.out.println("result count = " + result);
	}
	
	public void insertEmissionTriples(String plantiri) {
		
		String iriGen = plantiri + "_gen";
		String iriGenEm = iriGen + "em";
		String iriGenEmV = iriGenEm + "v";
		
		String query =  PowerPlantQueries.SPARQL_PREFIXES 
				+ "INSERT DATA { \r\n" 
				+ "<" + plantiri + "> technical_system:realizes <" + iriGen + "> .\r\n"
				+ "<" + iriGen + "> system_performance:hasEmission <" + iriGenEm + "> .\r\n"
				+ "<" + iriGenEm + "> system:hasValue <" + iriGenEmV + "> .\r\n"
				+ "<" + iriGenEmV + "> system:numericalValue 0.0 .\r\n"
				+ "}";
		
		//System.out.println(query);
		
		createSparqlOverHttpService().executePost(query);
	}
	
	public void testOneTestPlantUpdateEmission() {
//		String update =  FactorModelPerformance.SPARQL_PREFIXES 
//			+ "DELETE { ?emissionvalue system:numericalValue ?emissionvaluenum .} "
//			+ "INSERT { ?emissionvalue system:numericalValue %f .} "
//			+ "WHERE { <%s> technical_system:realizes ?generation . ?generation system_performance:hasEmission ?emission . ?emission system:hasValue ?emissionvalue . "
//			+ "?emissionvalue system:numericalValue ?emissionvaluenum . }";
//		
//		update = String.format(update, 12.77, IRI_TEST_PLANT);
//		System.out.println(update);
//		
//		String urlupdate = createModel().getSparqlServiceURIforUpdate();
//		SparqlOverHttpService.executePost(urlupdate, update);
		
		createModel().updateEmission(IRI_TEST_PLANT, 14.03);
		
		String query =  PowerPlantQueries.SPARQL_PREFIXES 
				+ "SELECT * WHERE { <%s> technical_system:realizes ?generation . ?generation system_performance:hasEmission ?emission . ?emission system:hasValue ?emissionvalue . ?emissionvalue system:numericalValue ?emissionvaluenum . }";
		query = String.format(query, IRI_TEST_PLANT);
		System.out.println(query);
		
		String result = createSparqlOverHttpService().executeGet(query);
		System.out.println(result);
	}
	
	public void testPerformanceGetAllPowerPlants() {
		
		createModel().queryAllPowerplants();
	}
	
}

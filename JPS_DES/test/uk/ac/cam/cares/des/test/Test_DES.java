package uk.ac.cam.cares.des.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.ResultSet;
import org.json.JSONObject;

import junit.framework.TestCase;
import uk.ac.cam.cares.jps.base.discovery.AgentCaller;
import uk.ac.cam.cares.jps.base.query.JenaHelper;
import uk.ac.cam.cares.jps.base.query.JenaResultSetFormatter;
import uk.ac.cam.cares.jps.base.query.QueryBroker;
import uk.ac.cam.cares.jps.des.WeatherIrradiationRetriever;


public class Test_DES extends TestCase{
	
	private String ENIRI="http://www.theworldavatar.com/kb/sg/singapore/SingaporeElectricalnetwork.owl#SingaporeElectricalnetwork";
	
	
	public void testrunpython2() throws IOException {
//		DistributedEnergySystem a = new DistributedEnergySystem();
//		String dataPath = QueryBroker.getLocalDataPath();
//		String baseUrl = dataPath + "/JPS_DES";
//		a.runOptimization(baseUrl);
		Runtime rt = Runtime.getRuntime();
		int returnValue = -1;
		System.out.println("Working Directory = " + System.getProperty("user.dir"));
		Process pr = rt.exec("python C:\\Users\\LONG01\\JParkSimulator-git\\JPS_DES\\python\\ocrv1.py", null, new File("C:\\Users\\LONG01\\JParkSimulator-git\\JPS_DES\\python"));
		try {
			pr.waitFor();
			returnValue = pr.exitValue();
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println(e);
		}
			System.out.println(returnValue);
		}

	public void testStartDESScenario() throws IOException  {
		

		JSONObject jo = new JSONObject();
	
		jo.put("electricalnetwork", ENIRI);
		
//		String scenarioUrl = BucketHelper.getScenarioUrl("testtest");
//		
//		
//		JPSContext.putScenarioUrl(jo, scenarioUrl);
//		String usecaseUrl = BucketHelper.getUsecaseUrl(scenarioUrl);
//		JPSContext.putUsecaseUrl(jo, usecaseUrl);
//		JPSHttpServlet.enableScenario(scenarioUrl,usecaseUrl);
		//new ScenarioClient().setOptionCopyOnRead(scenarioUrl, true);
		System.out.println(jo.toString());
		String resultStart = AgentCaller.executeGetWithJsonParameter("JPS_DES/DESAgent", jo.toString());
		System.out.println(resultStart);
		System.out.println("finished execute");

	}
	
	public void testIrrasdiationRetreiver() throws Exception {
		String dataPath = QueryBroker.getLocalDataPath();
		String baseUrl = dataPath + "/JPS_DES";
		
		JSONObject jo = new JSONObject();
		
		jo.put("folder", baseUrl);
		jo.put("tempsensor", "http://www.theworldavatar.com/kb/sgp/singapore/SGTemperatureSensor-001.owl#SGTemperatureSensor-001");
		jo.put("speedsensor", "http://www.theworldavatar.com/kb/sgp/singapore/SGWindSpeedSensor-001.owl#SGWindSpeedSensor-001");
		jo.put("irradiationsensor", "http://www.theworldavatar.com/kb/sgp/singapore/SGSolarIrradiationSensor-001.owl#SGSolarIrradiationSensor-001");
		jo.put("jpscontext", "base");
		WeatherIrradiationRetriever a= new WeatherIrradiationRetriever();
		System.out.println(jo.toString());
		a.readWritedatatoOWL(baseUrl,"http://www.theworldavatar.com/kb/sgp/singapore/SGTemperatureSensor-001.owl#SGTemperatureSensor-001","http://www.theworldavatar.com/kb/sgp/singapore/SGSolarIrradiationSensor-001.owl#SGSolarIrradiationSensor-001","http://www.theworldavatar.com/kb/sgp/singapore/SGWindSpeedSensor-001.owl#SGWindSpeedSensor-001");
		String resultStart = AgentCaller.executeGetWithJsonParameter("JPS_DES/GetIrradiationandWeatherData", jo.toString());
		System.out.println(resultStart);
	}
	
	public void testcsvmanipulation () {
		String sensorinfo2 = "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
				+ "PREFIX j4:<http://www.theworldavatar.com/ontology/ontosensor/OntoSensor.owl#> "
				+ "PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_realization/process_control_equipment/measuring_instrument.owl#> "
				+ "PREFIX j6:<http://www.w3.org/2006/time#> " + "SELECT ?entity ?propval ?proptimeval "
				+ "WHERE { ?entity a j5:Q-Sensor ." + "  ?entity j4:observes ?prop ." + " ?prop   j2:hasValue ?vprop ."
				+ " ?vprop   j2:numericalValue ?propval ." + " ?vprop   j6:hasTime ?proptime ."
				+ " ?proptime   j6:inXSDDateTimeStamp ?proptimeval ." + "}" + "ORDER BY ASC(?proptimeval)";
		
		String iriirradiationsensor="http://localhost:8080/kb/sgp/singapore/SGSolarIrradiationSensor-001.owl#SGSolarIrradiationSensor-001";
		String result2 = new QueryBroker().queryFile(iriirradiationsensor, sensorinfo2);
		String[] keys2 = JenaResultSetFormatter.getKeys(result2);
		List<String[]> resultListfromqueryirr = JenaResultSetFormatter.convertToListofStringArrays(result2, keys2);
		System.out.println("sizeofresult="+resultListfromqueryirr.size());
		System.out.println("element= "+resultListfromqueryirr.get(0)[2]);
		String content=resultListfromqueryirr.get(48)[2];
		System.out.println("year= "+content.split("#")[1].split("-")[0]);
		System.out.println("month= "+content.split("#")[1].split("-")[1]);
		System.out.println("date= "+content.split("#")[1].split("-")[2].split("T")[0]);
		System.out.println("time= "+content.split("#")[1].split("-")[2].split("T")[1].split("\\+")[0]);
	}
	
	public static OntModel readModelGreedy(String iriofnetwork) {
		String electricalnodeInfo = "PREFIX j1:<http://www.jparksimulator.com/ontology/ontoland/OntoLand.owl#> "
				+ "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
				+ "SELECT ?component "
				+ "WHERE { " 
				+ "?entity   j2:hasSubsystem ?component ." 
				+ "}";

		QueryBroker broker = new QueryBroker();
		return broker.readModelGreedy(iriofnetwork, electricalnodeInfo);
	}
	
	public static OntModel readModelGreedyForUser(String useriri) {
		String electricalnodeInfo = "PREFIX j1:<http://www.jparksimulator.com/ontology/ontoland/OntoLand.owl#> "
				+ "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
				+ "PREFIX j6:<http://www.theworldavatar.com/ontology/ontopowsys/PowSysRealization.owl#> " 
				+ "SELECT ?component "
				+ "WHERE { " 
				+ "?entity   j2:isConnectedTo ?component ." 
				+ "}";

		QueryBroker broker = new QueryBroker();
		return broker.readModelGreedy(useriri, electricalnodeInfo);
	}
	
	public void testquerygreedymultiple() {
		String iriofnetworkdistrict="http://www.theworldavatar.com/kb/sgp/singapore/District-001.owl#District-001";
		OntModel model = readModelGreedy(iriofnetworkdistrict);	
		String groupInfo = "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
				+ "PREFIX j4:<http://www.theworldavatar.com/ontology/ontopowsys/OntoPowSys.owl#> "
				+ "PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_realization/process_control_equipment/measuring_instrument.owl#> "
				+ "PREFIX j6:<http://www.theworldavatar.com/ontology/ontopowsys/PowSysRealization.owl#> " 
				+ "SELECT DISTINCT ?entity (COUNT(?entity) AS ?group) ?propval ?user "
				+ "WHERE {"
				+ "{ ?entity a j6:Building ."  
				+ "  ?entity j2:hasProperty ?prop ."
				+ " ?prop   j2:hasValue ?vprop ."
				+ " ?vprop   j2:numericalValue ?propval ."
				+ "?entity j4:isComprisedOf ?user ."	
				+ "}"
				+"FILTER regex(STR(?user),\"001\") ."
				+ "}" 
				+ "GROUP BY ?entity ?propval ?user "; 
		
		
		
		String groupInfo2 = "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
				+ "PREFIX j4:<http://www.theworldavatar.com/ontology/ontopowsys/OntoPowSys.owl#> "
				+ "PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_realization/process_control_equipment/measuring_instrument.owl#> "
				+ "PREFIX j6:<http://www.theworldavatar.com/ontology/ontopowsys/PowSysRealization.owl#> " 
				+ "SELECT DISTINCT ?entity (COUNT(?entity) AS ?group) "
				+ "WHERE "
				+ "{ ?entity a j6:Building ."
				+ "?entity j4:isComprisedOf ?user ."	 
			
				+ "}"

 
				+ "GROUP BY ?entity "; 
		
		String equipmentinfo = "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
				+ "PREFIX j4:<http://www.theworldavatar.com/ontology/ontopowsys/OntoPowSys.owl#> "
				+ "PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_realization/process_control_equipment/measuring_instrument.owl#> "
				+ "PREFIX j6:<http://www.theworldavatar.com/ontology/ontopowsys/PowSysRealization.owl#> "
				+ "PREFIX j7:<http://www.w3.org/2006/time#> "
				 + "PREFIX j9:<http://www.theworldavatar.com/ontology/ontopowsys/PowSysBehavior.owl#> "
				+ "SELECT ?entity ?Pmaxval ?Pminval ?unwillval ?Pactval ?hourval ?unwillval "
				+ "WHERE "
				+ "{ ?entity a j6:Electronics ."
				+ "?entity j9:hasActivePowerAbsorbed ?Pmax ."
				+ "?Pmax a j9:MaximumActivePower ."
				+ " ?Pmax   j2:hasValue ?vPmax ."
				+ " ?vPmax   j2:numericalValue ?Pmaxval ."
				
				+ "  ?entity j2:hasProperty ?prop ."
				+ "?prop a j6:IdealityFactor ."
				+ " ?prop   j2:hasValue ?vprop ."
				+ " ?vprop   j2:numericalValue ?unwillval ."
				
				+ "?entity j9:hasActivePowerAbsorbed ?Pmin ."
				+ "?Pmin a j9:MinimumActivePower ."
				+ " ?Pmin   j2:hasValue ?vPmin ."
				+ " ?vPmin   j2:numericalValue ?Pminval ."
				
				+ "?entity j9:hasActivePowerAbsorbed ?Pact ."
				+ "?Pact a j9:AbsorbedActivePower ."
				+ " ?Pact   j2:hasValue ?vPact ."
				+ " ?vPact   j2:numericalValue ?Pactval ."
				+ " ?vPact   j7:hasTime ?proptime ."
				+ "?proptime j7:hour ?hourval ."
			
				+ "}"
				+ "ORDER BY ASC(?hourval)";

		
		 //?user  ?user ?equipment

		
		ResultSet resultSet = JenaHelper.query(model, groupInfo);
		String result = JenaResultSetFormatter.convertToJSONW3CStandard(resultSet);
		String[] keys = JenaResultSetFormatter.getKeys(result);
		List<String[]> resultList = JenaResultSetFormatter.convertToListofStringArrays(result, keys);
		System.out.println("sizeofresult="+resultList.size());
		int size=resultList.size();
		List<String> iriofgroupuser= new ArrayList<String>();
		for(int d=0;d<size;d++) {
			for(int t=0;t<keys.length;t++) {
				System.out.println("element "+t+"= "+resultList.get(d)[t]);
				if(t==3) {
					iriofgroupuser.add(resultList.get(d)[t]);
				}
			}
			
			System.out.println("---------------------------------------");
		}
		
		ResultSet resultSet2 = JenaHelper.query(model, groupInfo2);
		String result2 = JenaResultSetFormatter.convertToJSONW3CStandard(resultSet2);
		String[] keys2 = JenaResultSetFormatter.getKeys(result2);
		List<String[]> resultList2 = JenaResultSetFormatter.convertToListofStringArrays(result2, keys2);
		System.out.println("sizeofresult="+resultList2.size());
		int size2=resultList2.size();
		for(int d=0;d<size2;d++) {
			for(int t=0;t<keys2.length;t++) {
				System.out.println("element "+t+"= "+resultList2.get(d)[t]);
			}
			System.out.println("---------------------------------------");
			
		}

		int sizeofiriuser=iriofgroupuser.size();
		System.out.println("sizeofiriuser="+sizeofiriuser);
		for(int x=0;x<sizeofiriuser;x++) {
			OntModel model2 = readModelGreedyForUser(iriofgroupuser.get(x));
			ResultSet resultSetx = JenaHelper.query(model2, equipmentinfo);
			String resultx = JenaResultSetFormatter.convertToJSONW3CStandard(resultSetx);
			String[] keysx = JenaResultSetFormatter.getKeys(resultx);
			List<String[]> resultListx = JenaResultSetFormatter.convertToListofStringArrays(resultx, keysx);
			System.out.println("sizeofresult="+resultListx.size());
			for(int d=0;d<resultListx.size();d++) {
				for(int t=0;t<keysx.length;t++) {
					System.out.println("element "+t+"= "+resultListx.get(d)[t]);
				}
				System.out.println("---------------------------------------");
				
			}
		}
		
		
	}
	
	
	

	
	
}

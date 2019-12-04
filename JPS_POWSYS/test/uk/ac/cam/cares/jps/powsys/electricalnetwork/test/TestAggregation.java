package uk.ac.cam.cares.jps.powsys.electricalnetwork.test;

import java.util.Map;

import org.json.JSONObject;

import junit.framework.TestCase;
import uk.ac.cam.cares.jps.base.discovery.AgentCaller;
import uk.ac.cam.cares.jps.powsys.electricalnetwork.AggregationEmissionAgent;
import uk.ac.cam.cares.jps.powsys.listener.LocalOntologyModelManager;

public class TestAggregation extends TestCase{

	public void testsumagg() {
		JSONObject x= new AggregationEmissionAgent().sumEmissionResult("http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/JurongIslandPowerNetwork.owl#JurongIsland_PowerNetwork");
		int size=x.getJSONArray("plant").length();
		System.out.println(x.getJSONArray("plant").get(2));
		System.out.println("total actco2 for plant 1= " + x.getJSONArray("emission").getDouble(2));
		if(x.getJSONArray("plant").get(2).toString().contains("http://www.theworldavatar.com/kb/powerplants/PowerSeraya_Pulau_Seraya_CCGT_Cogen_Power_Plant_Singapore.owl#PowerSeraya_Pulau_Seraya_CCGT_Cogen_Power_Plant_Singapore")){
			System.out.println(("goes here!!"));
			assertEquals(131.04400696870363, x.getJSONArray("emission").getDouble(2), 0.1);
			
		}
	}
	
	public void testfullfunction() {
		JSONObject jo = new JSONObject();
		jo.put("electricalnetwork", TestEN.ELECTRICAL_NETWORK);
		String resultStart = AgentCaller.executeGetWithJsonParameter("JPS_POWSYS/AggregationEmissionAgent/aggregateemission", jo.toString());
		System.out.println("result end="+resultStart);
	}
	
	public void testupdatefunction() {
		JSONObject jo = new AggregationEmissionAgent().updateEmission(TestEN.ELECTRICAL_NETWORK);
		System.out.println("result end="+jo.toString());
	}
	
	public void testmap() {
		Map hmap = LocalOntologyModelManager.getSpeciesMap();
		System.out.println("map size= "+hmap.size());
	}
	

}

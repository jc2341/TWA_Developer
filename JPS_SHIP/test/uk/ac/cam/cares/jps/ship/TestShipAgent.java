package uk.ac.cam.cares.jps.ship;

import org.json.JSONException;
import org.json.JSONObject;

import junit.framework.TestCase;
import uk.ac.cam.cares.jps.base.discovery.AgentCaller;

public class TestShipAgent extends TestCase {
	
	public void testShipAgent () throws JSONException {
		JSONObject arguments = new JSONObject();
		arguments.put("reactionmechanism", "https://como.cheng.cam.ac.uk/kb/Reduced_PRF_ERC.owl#ReactionMechanism_4909454516579602");
		arguments.put("ship", "http://172.25.182.41/kb/ships/Ship-1.owl#Ship-1");
		
		String wasteResult = AgentCaller.executeGet("/JPS_SHIP/ShipAgent", 
				"query", arguments.toString());
		System.out.println("waste: " + wasteResult);
	}
}
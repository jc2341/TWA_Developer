package uk.ac.cam.cares.jps.coordination;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cares.jps.base.config.IKeys;
import uk.ac.cam.cares.jps.base.config.KeyValueManager;
import uk.ac.cam.cares.jps.base.discovery.AgentCaller;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;

@WebServlet("/ADMSCoordinationAgentForShipWithoutComposition")
public class ADMSCoordinationAgentForShipWithoutComposition extends HttpServlet {

	private static final long serialVersionUID = -2264681360832342804L;
	Logger logger = LoggerFactory.getLogger(ADMSCoordinationAgentForShipWithoutComposition.class);

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
		String jsonInput = AgentCaller.readJsonParameter(request).toString();		
		JSONObject result = executeWithoutComposition(jsonInput);
		AgentCaller.writeJsonParameter(response, result);
	}
	
	
	public JSONObject executeWithoutComposition(String jsonInput) throws IOException {
		
		try {
			
			JSONObject jo = new JSONObject(jsonInput);
						
			String regionToCityResult = execute("/JPS/RegionToCity", jsonInput);
			String city = new JSONObject(regionToCityResult).getString("city");
			jo.put("city", city);
			logger.info("city FROM COORDINATION AGENT: " + city);
			logger.info("overall json= "+jo.toString());
			
			
			String result = execute("/JPS/GetBuildingListFromRegion", jo.toString());
			JSONArray building = new JSONObject(result).getJSONArray("building");
			jo.put("building", building);
			logger.info("building FROM COORDINATION AGENT: " + building.toString());
			
			
			if(city.toLowerCase().contains("kong")) {
				result = execute("/JPS_SHIP/GetHKUWeatherData", regionToCityResult);
			}
			else {
				result = execute("/JPS_COMPOSITION/CityToWeather", regionToCityResult);	
			}
			JSONObject weatherstate = new JSONObject(result).getJSONObject("weatherstate");
			jo.put("weatherstate", weatherstate);
//			logger.info("weatherstate FROM COORDINATION AGENT: " + weatherstate.toString());
			
			
			logger.info("calling postgres= "+jsonInput);
			String url = KeyValueManager.get(IKeys.URL_POSITIONQUERY);
			url += "/getEntitiesWithinRegion";
			String resultship=AgentCaller.executeGetWithURLAndJSON(url, jsonInput);			
				
			JSONObject jsonShipIRIs = new JSONObject(resultship);
			JSONArray shipIRIs = jsonShipIRIs.getJSONArray("shipIRIs");

//			jo.put("ship", shipIRIs);
			
			
			JSONObject jsonReactionShip = new JSONObject();
			String reactionMechanism = jo.getString("reactionmechanism");
			jsonReactionShip.put("reactionmechanism", reactionMechanism);
						
//			for (int i = 0; i < shipIRIs.length(); i++) {
			for (int i = 0; i < 1; i++) {
				String shipIRI = shipIRIs.getString(i);
				jsonReactionShip.put("ship", shipIRI);
				
				//String wasteResult = AgentCaller.executeGet("/JPS_SHIP/ShipAgent", "query", jsonReactionShip.toString());
				String wasteResult = execute("/JPS_SHIP/ShipAgent", jsonReactionShip.toString());
				String waste = new JSONObject(wasteResult).getString("waste");
				jo.put("waste", waste);
			}
			
			jo.put("ship", shipIRIs);
			// TODO: SC
			// Iterate over list of ship iris and perform query of each ship.
			
			result = execute("/JPS/ADMSAgent", jo.toString());
			String folder = new JSONObject(result).getString("folder");
			jo.put("folder", folder);
			
			return jo;
			
		} catch (JSONException e) {
			logger.error(e.getMessage(), e);
			throw new JPSRuntimeException(e.getMessage(), e);
		}
	}
	
	protected String execute(String path, String jsonInput) {

		logger.info("execute for path=" + path + ", json=" + jsonInput);
		String result = AgentCaller.executeGet(path, "query", jsonInput);
		logger.info("execution result=" + result);
		return result;
	}
}

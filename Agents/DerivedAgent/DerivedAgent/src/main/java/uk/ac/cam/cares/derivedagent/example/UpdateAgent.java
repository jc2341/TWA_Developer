package uk.ac.cam.cares.derivedagent.example;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import uk.ac.cam.cares.jps.base.agent.JPSAgent;
import uk.ac.cam.cares.jps.base.derivation.DerivationClient;
import uk.ac.cam.cares.jps.base.query.RemoteStoreClient;

@WebServlet(urlPatterns = {UpdateAgent.URL_UpdateTimeDuration, UpdateAgent.URL_UpdateMinTimeCalc, UpdateAgent.URL_UpdateMaxTimeCalc})
public class UpdateAgent extends JPSAgent{
	/**
	 * 
	 */
	public static final String URL_UpdateTimeDuration = "/UpdateTimeDuration";
	public static final String URL_UpdateMinTimeCalc = "/UpdateMinTimeCalc";
	public static final String URL_UpdateMaxTimeCalc = "/UpdateMaxTimeCalc";
	private static final long serialVersionUID = 1L;

	@Override
    public JSONObject processRequestParameters(JSONObject requestParams, HttpServletRequest request) {
		ExampleConfig.initProperties();
		RemoteStoreClient storeClient = new RemoteStoreClient(ExampleConfig.kgurl,ExampleConfig.kgurl,ExampleConfig.kguser,ExampleConfig.kgpassword);
		DerivationClient devClient = new DerivationClient(storeClient);
		
		String path = request.getServletPath();
		switch (path) {
    		case URL_UpdateTimeDuration:
    			devClient.updateDerivation(InstancesDatabase.DerivedQuantityTimeDuration);
    			break;
    			
    		case URL_UpdateMinTimeCalc:
    			devClient.updateDerivation(InstancesDatabase.DerivedQuantityMinTimeCalc);
    			break;
    			
    		case URL_UpdateMaxTimeCalc:
    			devClient.updateDerivation(InstancesDatabase.DerivedQuantityMaxTimeCalc);
    			break;
		}
		
		return requestParams;	
	}
}

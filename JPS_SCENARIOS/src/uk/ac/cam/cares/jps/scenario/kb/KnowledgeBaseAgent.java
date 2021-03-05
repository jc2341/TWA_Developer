package uk.ac.cam.cares.jps.scenario.kb;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cares.jps.base.agent.JPSAgent;
import uk.ac.cam.cares.jps.base.config.JPSConstants;
import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;
import uk.ac.cam.cares.jps.base.http.Http;
import uk.ac.cam.cares.jps.base.query.KnowledgeBaseClient;
import uk.ac.cam.cares.jps.base.util.InputValidator;
import uk.ac.cam.cares.jps.base.util.MiscUtil;

@WebServlet(urlPatterns = {"/kb/*", "/data/*", "/dataset/*"})
public class KnowledgeBaseAgent extends JPSAgent {

	private static final long serialVersionUID = -4195274773048314961L;
	private static Logger logger = LoggerFactory.getLogger(KnowledgeBaseAgent.class);
	/** empty Result for now, because getAccept still requires headers. 
	 * 
	 */
	@Override
	public JSONObject processRequestParameters(JSONObject requestParams) {
		JSONObject result = processRequestParameters(requestParams,null);
		return result;
	}
	@Override
    public JSONObject processRequestParameters(JSONObject requestParams, HttpServletRequest request) {
		System.out.println("JSON PARAMS" + requestParams.toString());
		if (!validateInput(requestParams)) {
			throw new JSONException("KnowledgeBaseAgent: Input parameters not found.\n");
		}
		String body = MiscUtil.optNullKey(requestParams, JPSConstants.CONTENT);
		String requestUrl = MiscUtil.optNullKey(requestParams, JPSConstants.REQUESTURL);
		String path = MiscUtil.optNullKey(requestParams, JPSConstants.PATH);
		String contentType = MiscUtil.optNullKey(requestParams, JPSConstants.CONTENTTYPE);
		String paramResourceUrl= MiscUtil.optNullKey(requestParams,JPSConstants.SCENARIO_RESOURCE);
        String sparql = MiscUtil.optNullKey(requestParams, JPSConstants.QUERY_SPARQL_UPDATE);
        String method = MiscUtil.optNullKey(requestParams, JPSConstants.METHOD);
		
        String paramDatasetUrl = MiscUtil.optNullKey(requestParams, JPSConstants.SCENARIO_DATASET);
		String datasetUrl = KnowledgeBaseManager.getDatasetUrl(requestUrl);
		KnowledgeBaseAbstract kb = KnowledgeBaseManager.getKnowledgeBase(datasetUrl);
		String resourceUrl = getResourceUrl(datasetUrl, requestUrl, paramResourceUrl);

		JSONObject result = new JSONObject();
		logInputParams(method, requestUrl, path, paramDatasetUrl, paramResourceUrl, contentType, sparql, false);
		switch (method) {
			case HttpGet.METHOD_NAME:
				sparql = MiscUtil.optNullKey(requestParams, JPSConstants.QUERY_SPARQL_QUERY);
	            String accept = MiscUtil.optNullKey(requestParams, JPSConstants.HEADERS);
				String qres = "";
				if (sparql == null) {
					qres = kb.get(resourceUrl, accept);
				} else {
					qres = kb.query(resourceUrl, sparql);
				}
				result.put("result", qres);
			    break;
		  case HttpPost.METHOD_NAME:
				if (sparql == null) {
					throw new JPSRuntimeException("parameter " + JPSConstants.QUERY_SPARQL_UPDATE + " is missing");
				}
				kb.update(resourceUrl, sparql);
		    break;
		  case HttpPut.METHOD_NAME:
				if (sparql != null) {
    				throw new JPSRuntimeException("parameter " + JPSConstants.QUERY_SPARQL_UPDATE + " is not allowed");
    			}    			
    			kb.put(resourceUrl, body, contentType);
		  
			}		
        return result;
        }
	


	@Override
    public boolean validateInput(JSONObject requestParams) throws BadRequestException {
        if (requestParams.isEmpty()) {
            throw new BadRequestException();
        }
        try {
	        boolean q = InputValidator.checkURLpattern(requestParams.getString(JPSConstants.REQUESTURL));
	        String method = MiscUtil.optNullKey(requestParams,JPSConstants.METHOD);
	        if (method == null) {
	        	return false;
	        }
	        return q;
        }catch (JSONException ex) {
        	ex.printStackTrace();
        	return false;
        }
    }

	
	public String getResourceUrl(String datasetUrl, String requestUrl, String parameterUrl) {

		// Example: datasetUrl = http://www.thw.com/jps/data/test
		
		if (requestUrl.equals(datasetUrl)) {
			
			if ((parameterUrl == null) || parameterUrl.isEmpty()) {
				return null;
			} else {
				// case 2: indirect query
				return KnowledgeBaseClient.cutHashFragment(parameterUrl);
			}
			
		} else {
			if ((parameterUrl == null) || parameterUrl.isEmpty()) {
				// case 3: direct query
				return requestUrl;
			}
		}
		
		String message = "A URL was given by the query parameter " + JPSConstants.SCENARIO_RESOURCE 
				+ ". This is not allowed since the requested URL does not define a dataset URL."
				+ " parameter URL = " + parameterUrl + ", requested URL=" + requestUrl;
		throw new JPSRuntimeException(message);
	}
	
	protected void logInputParams(String httpVerb, String requestUrl, String path, String datasetUrl, String resourceUrl, String contentType, String sparql, boolean hasErrorOccured) {
		StringBuffer b = new StringBuffer(httpVerb);
		b.append(" with requestedUrl=").append(requestUrl);
		b.append(", path=").append(path);
		b.append(", datasetUrl=").append(datasetUrl);
		b.append(", resourceUrl=").append(resourceUrl);
		b.append(", contentType=").append(contentType);
		if (hasErrorOccured) {
			b.append(", sparql=" + sparql);
			logger.error(b.toString());
		} else {
			if (sparql != null) {
				int i = sparql.toLowerCase().indexOf("select");
				if (i > 0) {
					sparql = sparql.substring(i);
				}
				if (sparql.length() > 150) {
					sparql = sparql.substring(0, 150);
				}
			}
			b.append(", sparql (short)=" + sparql);
			logger.info(b.toString());
		}
	}
}

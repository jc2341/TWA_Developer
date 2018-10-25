package uk.ac.ceb.como.molhub.action;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.jena.ontology.OntModel;
import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.dispatcher.SessionMap;

import com.opensymphony.xwork2.ActionSupport;

import uk.ac.cam.ceb.como.compchem.ontology.query.CompChemQuery;
import uk.ac.cam.ceb.como.jaxb.parsing.utils.FileUtility;
import uk.ac.cam.ceb.como.jaxb.parsing.utils.Utility;

import org.apache.commons.io.FileUtils;

import org.apache.log4j.Logger;

/**
 * 
 * @author nk510
 *         <p>
 * 		Class implements method which runs sparql query on generated Abox (owl
 *         files) of Compchem ontology. Runs thermo calculations implemented in
 *         Python, and generates results using json format.
 *         </p>
 */

public class CalculationAction extends ActionSupport implements SessionAware {

	final static Logger logger = Logger.getLogger(CalculationAction.class.getName());

	private static final long serialVersionUID = 1L;

	/**
	 * @author nk510 <p> Session is instance of {@link java.util.Map} that remembers session as pair (uuid, molecule
	 *         name).</p>
	 */
	
	Map<String, Object> session;

	String catalinaFolderPath = System.getProperty("catalina.home");

	/**
	 * @author nk510 <p>SPARQL query used in thermo - calculations. It queries
	 *         generated ontology file (Abox of Compchem ontology) in a given folder
	 *         name (uuid), and stores sparql results in json file in the same folder
	 *         (uuid).</p>
	 */
	
	String sparql = catalinaFolderPath + "/conf/Catalina/sparql_query/query_all.sparql";

	@Override
	public String execute() throws Exception {

		Utility utility = new FileUtility();

		File sparqlFile = new File(sparql);

		/**
		 * @author nk510 <p>If there are no species (uuid, molecule name) appearing in
		 *         search results, then session map is empty.</p>
		 */
		
		if (session.isEmpty()) {

			addFieldError("term.name", "There are no selected species for which calculation will be performed.");

			return ERROR;
		}
		
		/**
		 * 
		 * @author nk510 <p>Iterates over session HashMap and performs (runs) thermo calculations
		 *         on generated json files stored in folder named by "uuid".</p>
		 *         
		 */
		
		for (Map.Entry<String, Object> mp : session.entrySet()) {

			String speciesFolder = catalinaFolderPath + "/webapps/ROOT/" + mp.getKey().toString() + "/";

			List<File> aboxFiles = utility.getArrayFileList(speciesFolder, ".owl");

			for (File af : aboxFiles) {

				OntModel model = CompChemQuery.getOntModel(af.getAbsolutePath());

				String q = FileUtils.readFileToString(sparqlFile, "UTF-8");

				CompChemQuery.performQuery(model, q, af.getName().toString(), speciesFolder);

			}
			
			List<File> jsonFiles = utility.getArrayFileList(speciesFolder, ".json");

			for (int i = 0; i < jsonFiles.size(); i++) {

				logger.info("jsonFile.getAbsolutePath(): " + jsonFiles.get(i).getAbsolutePath());

				/**
				 * @author nk510 <p>Runs Python script for thermodynamic calculations. Python script implemented by {@author danieln@cmclinnovations.com} 
				 */

				String[] cmd = { "python", "C:/Users/nk510/git/c4e-dln22-TDC/Source/thermoDriver.py", "-j",
						jsonFiles.get(i).getAbsolutePath(), };

				Runtime.getRuntime().exec(cmd);
			}
			
		}
		
		/**
		 * @author nk510
		 * <p>Removes all data from session's map after finishing thermo calculations.</p>
		 * 
		 */
		
		for (Map.Entry<String, Object> entry : session.entrySet()) {
		     
		        session.remove(entry.getKey());
		}
		
		addActionMessage("Calculations successfully completed.");

		return SUCCESS;

	}

	public Map<String, Object> getSession() {
		return session;
	}

	@Override
	public void setSession(Map<String, Object> session) {
		
		this.session = (SessionMap<String, Object>) session;
	}
}
package uk.ac.cam.cares.jps.agent.file_management.marshallr.sensana;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.cam.cares.jps.agent.configuration.MoDSSensAnaAgentProperty;
import uk.ac.cam.cares.jps.agent.json.parser.JSonRequestParser;
import uk.ac.cam.cares.jps.agent.mechanism.sensana.MoDSSensAnaAgentException;
import uk.ac.cam.cares.jps.kg.OntoKinKG;

public class SensAnaResultsProcess {
	private Logger logger = LoggerFactory.getLogger(SensAnaResultsProcess.class);
	private String topN = "10";
	private boolean max = false;
	private MoDSSensAnaAgentProperty modsSensAnaAgentProperty;
	
	public String getTopN() {
		return topN;
	}

	public void setTopN(String topN) {
		this.topN = topN;
	}
	
	public boolean getMax() {
		return max;
	}

	public void setMax(boolean max) {
		this.max = max;
	}
	
	public SensAnaResultsProcess(MoDSSensAnaAgentProperty modsSensAnaAgentProperty) {
		this.modsSensAnaAgentProperty = modsSensAnaAgentProperty;
	}
	
	public static void main(String[] args) {
		String jsonString = "{\"json\":{\"mods\":{\"sensAna\":{\"relPerturbation\":\"1e-3\",\"topN\":\"10\", \"maxORavg\":\"avg\"},\"ignDelayOption\":{\"method\":\"1\",\"species\":\"AR\"},\"flameSpeedOption\":{\"tranModel\":\"mix-average\"}},\"ontochemexpIRI\":{\"ignitionDelay\":[\"https://como.ceb.cam.ac.uk/kb/ontochemexp/x00001700.owl#Experiment_404313416274000\",\"https://como.ceb.cam.ac.uk/kb/ontochemexp/x00001701.owl#Experiment_404313804188800\",\"https://como.ceb.cam.ac.uk/kb/ontochemexp/x00001702.owl#Experiment_404313946760600\"],\"flameSpeed\":[\"https://como.ceb.cam.ac.uk/kb/ontochemexp/x00001703.owl#Experiment_2748799135285400\"]},\"ontokinIRI\":{\"mechanism\":\"http://www.theworldavatar.com/kb/ontokin/pode_mechanism_original.owl#ReactionMechanism_73656018231261\"}}}";
		String jobFolderPath = "C:\\Users\\jb2197\\CompletedJobsMoDSSensAnaAgent_4639325665088300\\login-skylake.hpc.cam.ac.uk_6554602362814500\\output";
	}
	
	public List<String> processResults(String jobFolderPath, String jsonString) throws IOException, MoDSSensAnaAgentException {
		String n = JSonRequestParser.getTopNForRxns(jsonString);
		if (n != null && !n.isEmpty()) {
			setTopN(n);
		}
		String mechanismIRI = JSonRequestParser.getOntoKinMechanismIRI(jsonString);
		
		String maxAvg = JSonRequestParser.getMaxOrAvg(jsonString);
		if (maxAvg != null && !maxAvg.isEmpty() && maxAvg.toLowerCase().contains("max")) {
			setMax(true);
		}
		
		// process ign sens results, get a file to record the rxns selected
		List<String> ignRxnIRI = processResponse("subtype_IgnitionDelay", mechanismIRI, jobFolderPath, Integer.parseInt(getTopN()), getMax());
		// process flame sens results, get a file to record the rxns selected
		List<String> flameRxnIRI = processResponse("subtype_LaminarFlameSpeed", mechanismIRI, jobFolderPath, Integer.parseInt(getTopN()), getMax());
		// merge the two files and get one final list
		List<String> supersetRxns = mergeTwoResponses(ignRxnIRI, flameRxnIRI);
		
		logger.info("Sensitivity analysis results have been processed successfully.");
		return supersetRxns;
	}
	
	
	public List<String> processResponse(String response, String mechanismIRI, String jobFolderPath, Integer topN, boolean max) throws IOException, MoDSSensAnaAgentException {
		if (!jobFolderPath.endsWith("\\")) {
			jobFolderPath = jobFolderPath.concat("\\");
		}
		String responseResults = jobFolderPath+"SensitivityAnalysis\\SensitivityAnalysis_"+response+".csv";
		String sensAnaResults = jobFolderPath+"SensitivityAnalysis\\SensitivityAnalysis_Sensitivities.csv";
		String deriResults = jobFolderPath+"SensitivityAnalysis\\SensitivityAnalysis_Derivatives.csv";
		String rxnOutputResults = jobFolderPath+"SensitivityAnalysis_SelectedRxns_"+response+".csv";
		
		// take input, process, then write results to output file
		// determine the cases that to be used for reaction selection
		LinkedHashMap<Integer, List<Double>> successCases = identifyCases(response, new File(responseResults), new File(sensAnaResults));
		// get the list of active parameters tested in the sensitivity analysis
		LinkedHashMap<Integer, String> activeParameters = getSensParameters(new File(deriResults));
		// load sensitivity analysis results into one map <rxnIndex, averaged sensitivity over all successfully simulated cases>
		LinkedHashMap<String, Double> sensAnaForRxns = computeSensForRxns(successCases, activeParameters, max);		
		// get the top N reactions that most sensitive
		LinkedHashMap<String, Double> selectedRxns = getTopNRxns(sensAnaForRxns, topN);
		
		// output this result to file
		List<String> listOfRxnIRI = writeSelectedRxns(mechanismIRI, new File(rxnOutputResults), selectedRxns);
		
		logger.info("Sensitivity analysis for response "+response+" have been processed successfully.");
		return listOfRxnIRI;
	}
	
	
	private LinkedHashMap<Integer, List<Double>> identifyCases(String response, File simFile, File sensFile) throws IOException, MoDSSensAnaAgentException {
		// read the list of cases and original simulation results with original parameters
		List<String> casesList = new ArrayList<>();
		List<String> origResults = new ArrayList<>();
		// preliminary assessment success cases
		List<String> preCases = new ArrayList<>();
		// read the list of all cases for all responses
		List<String> allCasesList = new ArrayList<>();
		// select only the cases that simulated successfully, i.e., result is within the pre-specified range
		LinkedHashMap<Integer, List<Double>> successCases = new LinkedHashMap<Integer, List<Double>>();
		if (simFile.isFile() && sensFile.isFile()) {
			BufferedReader brSim = new BufferedReader(new FileReader(simFile));
			BufferedReader brSens = new BufferedReader(new FileReader(sensFile));
			casesList = Arrays.asList(brSim.readLine().split(","));
			origResults = Arrays.asList(brSim.readLine().split(","));
			allCasesList = new ArrayList<>(Arrays.asList(brSens.readLine().split(",")));
			String simLine = null;
			String sensLine = null;
			int idx = 1;
			// assessment TODO further parametrised this comparison part to better cope with range
			if (response.toLowerCase().contains("ign") || response.toLowerCase().contains("delay")) {
				for (int i = 0; i < casesList.size(); i++) {
					if (Double.valueOf(origResults.get(i)) > 0 && Double.valueOf(origResults.get(i)) < 500) {
						preCases.add(casesList.get(i));
					}
				}
				while ((simLine = brSim.readLine()) != null && (sensLine = brSens.readLine()) != null) {
					List<String> simList = Arrays.asList(simLine.split(","));
					List<String> sensList = Arrays.asList(sensLine.split(","));
					List<Double> sensVal = new ArrayList<>();
					for (String preCase : preCases) {
						int simIdx = casesList.indexOf(preCase);
						int sensIdx = allCasesList.indexOf(preCase);
						if (Double.valueOf(simList.get(simIdx)) > 0 && Double.valueOf(simList.get(simIdx)) < 500) {
							sensVal.add(Math.abs(Double.valueOf(sensList.get(sensIdx)))); // get the absolute value
						}
					}
					successCases.put(idx, sensVal);
					idx++;
				}
			} else if (response.toLowerCase().contains("flame") || response.toLowerCase().contains("speed")) {
				for (int i = 0; i < casesList.size(); i++) {
					if (Double.valueOf(origResults.get(i)) > 0) {
						preCases.add(casesList.get(i));
					}
				}
				while ((simLine = brSim.readLine()) != null && (sensLine = brSens.readLine()) != null) {
					List<String> simList = Arrays.asList(simLine.split(","));
					List<String> sensList = Arrays.asList(sensLine.split(","));
					List<Double> sensVal = new ArrayList<>();
					for (String preCase : preCases) {
						int simIdx = casesList.indexOf(preCase);
						int sensIdx = allCasesList.indexOf(preCase);
						if (Double.valueOf(simList.get(simIdx)) > 0) {
							sensVal.add(Math.abs(Double.valueOf(sensList.get(sensIdx)))); // get the absolute value
						}
					}
					successCases.put(idx, sensVal);
					idx++;
				}
			}
			brSim.close();
			brSens.close();
			return successCases;
		}
		return null;
	}
	
	private LinkedHashMap<Integer, String> getSensParameters(File deriFile) throws IOException, MoDSSensAnaAgentException {
		LinkedHashMap<Integer, String> activeParameters = new LinkedHashMap<Integer, String>();
		if (deriFile.isFile()) {
			BufferedReader br = new BufferedReader(new FileReader(deriFile));
			// pass the first header line
			String line = br.readLine();
			int i = 1; // this indicates the start index of reaction
			while ((line = br.readLine()) != null) {
				// only take the last element of the string
				activeParameters.put(i, line.substring(line.lastIndexOf(",")+1));
				i++; // move to the next reaction
			}
			br.close();
		}
		return activeParameters;
	}
	
	private LinkedHashMap<String, Double> computeSensForRxns(LinkedHashMap<Integer, List<Double>> successCases, LinkedHashMap<Integer, String> activeParameters, boolean max) throws IOException, MoDSSensAnaAgentException {
		LinkedHashMap<String, Double> sensTableForRxns = new LinkedHashMap<String, Double>();
		if (max) {
			for (Integer j : successCases.keySet()) {
				Double maxi = Collections.max(successCases.get(j));
				sensTableForRxns.put(activeParameters.get(j), maxi);
			}
			return sensTableForRxns;
		} else {
			for (Integer j : successCases.keySet()) {
				Double avg = successCases.get(j).stream().collect(Collectors.summingDouble(Double::doubleValue)) / successCases.get(j).size();
				sensTableForRxns.put(activeParameters.get(j), avg);
			}
			return sensTableForRxns;
		}
	}
	
	private LinkedHashMap<String, Double> getTopNRxns(Map<String, Double> allRxns, Integer topN) throws IOException, MoDSSensAnaAgentException {
		// sort all reactions based on its sensitivity values
		LinkedHashMap<String, Double> sortedRxns = sortRxnSens(allRxns);
		
		// select only the top N reactions based on user requirement
		LinkedHashMap<String, Double> topNRxns = new LinkedHashMap<String, Double>();
		List<String> keys = new ArrayList<>(sortedRxns.keySet());
		for (String rxnNo : keys.subList(0, topN)) {
			topNRxns.put(rxnNo, sortedRxns.get(rxnNo));
		}
		
		return topNRxns;
	}
	
	private LinkedHashMap<String, Double> sortRxnSens(Map<String, Double> allRxns) throws IOException, MoDSSensAnaAgentException {
		// get the list of reactions based on the map of key and value
		List<Entry<String, Double>> listOfRxns = new LinkedList<Entry<String, Double>>(allRxns.entrySet());
		
		// sort the list of reactions based on its sensitivity values
		Collections.sort(listOfRxns, new Comparator<Entry<String, Double>>() {
			public int compare(Entry<String, Double> sens1, Entry<String, Double> sens2) {
				return sens2.getValue().compareTo(sens1.getValue());
			}
		});
		
		// put sorted reactions in the format of LinkedHashMap so that guarantee the order
		LinkedHashMap<String, Double> rxnsInOrder = new LinkedHashMap<String, Double>();
		for (Entry<String, Double> rxn : listOfRxns) {
			rxnsInOrder.put(rxn.getKey(), rxn.getValue());
		}
		
		return rxnsInOrder;
	}
	
	private List<String> writeSelectedRxns(String mechanismIRI, File resultsFile, LinkedHashMap<String, Double> selectedRxns) throws IOException, MoDSSensAnaAgentException {
		List<String[]> dataLines = new ArrayList<>();
		dataLines.add(new String[] {"No", "RxnIRI", "RxnEquation", "RxnSensitivity"});
		List<String> listOfRxnIRI = new ArrayList<>();
		for (String rxn : selectedRxns.keySet()) {
			OntoKinKG ontoKinKg = new OntoKinKG(modsSensAnaAgentProperty);
			LinkedHashMap<String, String> rxnIRIandEqu = ontoKinKg.queryReactionBasedOnNo(mechanismIRI, rxn.substring(rxn.lastIndexOf("_")+1));
			String iri = new String();
			String equ = new String();
			for (String rxnIRI : rxnIRIandEqu.keySet()) {
				iri = rxnIRI;
				equ = rxnIRIandEqu.get(rxnIRI);
				listOfRxnIRI.add(iri);
			}
			
			dataLines.add(new String[] {rxn.substring(rxn.lastIndexOf("_")+1), iri, equ, Double.toString(selectedRxns.get(rxn))});
		}
		
		try (PrintWriter pw = new PrintWriter(resultsFile)) {
			dataLines.stream()
			.map(this::convertToCSV)
			.forEach(pw::println);
		}
		
		return listOfRxnIRI;
	}
	
	private List<String> mergeTwoResponses(List<String> response1, List<String> response2) throws IOException, MoDSSensAnaAgentException {
		for (String res : response2) {
			if (!response1.contains(res)) {
				response1.add(res);
			}
		}
		
		return response1;
	}
	
	/**
	 * Convert a string array to a string in the format of CSV file. 
	 * 
	 * @param data
	 * @return
	 */
	private String convertToCSV(String[] data) {
	    return Stream.of(data)
	      .map(this::escapeSpecialCharacters)
	      .collect(Collectors.joining(","));
	}
	
	/**
	 * Escape special characters when converting string array to string in the format of CSV file. 
	 * 
	 * @param data
	 * @return
	 */
	private String escapeSpecialCharacters(String data) {
	    String escapedData = data.replaceAll("\\R", " ");
	    if (data.contains(",") || data.contains("\"") || data.contains("'")) {
	        data = data.replace("\"", "\"\"");
	        escapedData = "\"" + data + "\"";
	    }
	    return escapedData;
	}
	
}

package uk.ac.cam.cares.jps.agent.file_management.marshallr;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import uk.ac.cam.cares.jps.agent.json.parser.JSonRequestParser;
import uk.ac.cam.cares.jps.agent.mechanism.calibration.MoDSAgentException;
import uk.ac.cam.cares.jps.agent.mechanism.calibration.Utils;

public class MoDSFileManagement extends MoDSMarshaller {
//	public static RepositoryManager repoManager = new RepositoryManager();
	Logger logger = Logger.getLogger(MoDSFileManagement.class);
//	public static List<String[]> dataLines;
	public static String jobFolderName;
	public static List<String> activeParameters_1 = new ArrayList<>();
	public static List<String> passiveParameters_1 = new ArrayList<>();
	public static String outputResponse_1 = new String();
	public static List<String> activeParameters_2 = new ArrayList<>();
	public static List<String> passiveParameters_2 = new ArrayList<>();
	public static String outputResponse_2 = new String();
	
	public static void main(String[] args) throws IOException, MoDSAgentException {
		MoDSFileManagement fileMagt = new MoDSFileManagement();

		String jobFolderName = "login-skylake.hpc.cam.ac.uk_"+Utils.getTimeStamp()+"_Optimisation_MaxDtDt";
		
		String resultsFolder = "C:\\Users\\jb2197\\Desktop\\PODE_Project\\Data\\SensAna\\";
		
//		String sens = "login-cpu.hpc.cam.ac.uk_3705638140418000_SensAna_MaxDcDt_OH"+"\\SensitivityAnalysis\\SensitivityAnalysis_SelectedReactions.csv";
//		String sens = "login-cpu.hpc.cam.ac.uk_3705655429858500_SensAna_MaxDcDt_CO"+"\\SensitivityAnalysis\\SensitivityAnalysis_SelectedReactions.csv";
//		String sens = "login-cpu.hpc.cam.ac.uk_3705683489571800_SensAna_Conc_CO"+"\\SensitivityAnalysis\\SensitivityAnalysis_SelectedReactions.csv";
//		String sens = "login-cpu.hpc.cam.ac.uk_3705704050765200_SensAna_Conc_OH"+"\\SensitivityAnalysis\\SensitivityAnalysis_SelectedReactions.csv";
//		String sens = "login-cpu.hpc.cam.ac.uk_3705731441602300_SensAna_Temp_400K"+"\\SensitivityAnalysis\\SensitivityAnalysis_SelectedReactions.csv";
//		String sens = "login-cpu.hpc.cam.ac.uk_3705768273696900_SensAna_MaxDpDt"+"\\SensitivityAnalysis\\SensitivityAnalysis_SelectedReactions.csv";
		String sens = "login-cpu.hpc.cam.ac.uk_3705790466069800_SensAna_MaxDtDt"+"\\SensitivityAnalysis\\SensitivityAnalysis_SelectedReactions.csv";
		
		File rxnFile = new File(resultsFolder+sens);
		
		String jsonString = "{\"json\":{\"ontochemexpIRI\":{\"ignitionDelay\":[\"https://como.ceb.cam.ac.uk/kb/ontochemexp/x00001700.owl#Experiment_404313416274000\",\"https://como.ceb.cam.ac.uk/kb/ontochemexp/x00001701.owl#Experiment_404313804188800\",\"https://como.ceb.cam.ac.uk/kb/ontochemexp/x00001702.owl#Experiment_404313946760600\"],\"flameSpeed\":[\"https://como.ceb.cam.ac.uk/kb/ontochemexp/x00001703.owl#Experiment_2748799135285400\"]},\"ontokinIRI\":{\"reactionList\":["
				+ fileMagt.getRxnIRIList(rxnFile)
				+ "],\"mechanism\":\"http://www.theworldavatar.com/kb/ontokin/pode_mechanism_original.owl#ReactionMechanism_73656018231261\"}}}";
		
//		System.out.println(jsonString);
		fileMagt.createMoDSJob(jsonString, jobFolderName);
	}
	
	
	public String getRxnIRIList(File sensAnaResultsFile) throws IOException, MoDSAgentException {
		String rxnIRIList = new String();
		if (sensAnaResultsFile.isFile()) {
			BufferedReader br = new BufferedReader(new FileReader(sensAnaResultsFile));
			String[] header = br.readLine().split(",");
			int iriLocate = -1;
			for (int i = 0; i < header.length; i++) {
				if (header[i].contains("IRI")) {
					iriLocate = i;
				}
			}
			String line = new String();
			while ((line = br.readLine()) != null) {
				String[] data = line.split(",");
				rxnIRIList = rxnIRIList.concat(",\"").concat(data[iriLocate]).concat("\"");
			}
			br.close();
		}
		return rxnIRIList.substring(1);
	}
	
	public String createMoDSJob(String jsonString, String jobFolderName) throws IOException, MoDSAgentException {
		
		List<String> ignitionDelayExpIRI = JSonRequestParser.getOntoChemExpIgnitionDelayIRI(jsonString);
		List<String> flameSpeedExpIRI = JSonRequestParser.getOntoChemExpFlameSpeedIRI(jsonString);
		String mechanismIRI = JSonRequestParser.getOntoKinMechanismIRI(jsonString);
		List<String> reactionIRIList = JSonRequestParser.getOntoKinReactionsIRI(jsonString);
		
		IMoDSMarshaller iMoDSMarshaller = new MoDSMarshaller();
		iMoDSMarshaller.initialise(jobFolderName);
		iMoDSMarshaller.plugInKinetics(ignitionDelayExpIRI, mechanismIRI, reactionIRIList);
		iMoDSMarshaller.plugInCantera(flameSpeedExpIRI, mechanismIRI, reactionIRIList);
		String jobFolderPath = iMoDSMarshaller.marshall();
		
		logger.info("The requested MoDS job was created.");
//		placeMoDSSlurmScript(jobFolderPath);
		return jobFolderPath;
	}
	
	protected void placeMoDSSlurmScript(String jobFolderPath) throws IOException, MoDSAgentException {
		// TODO
		File sourceSlurmScript = new File(getClass().getClassLoader().getResource(FILE_MODS_SLURM_SCRIPT).getFile());
		File modsSlurmScript = new File(jobFolderPath.concat(FRONTSLASH+FILE_MODS_SLURM_SCRIPT));
		
		// create the BufferedReader and BufferedWriter to read and write files
		BufferedReader br = null;
		BufferedWriter bw = null;
		
		// copy the modsslurm_como.sh script
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(sourceSlurmScript)));
	        bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(modsSlurmScript)));
	        String line = new String();
	        while ((line = br.readLine()) != null) {
	        	bw.write(line.concat("\n"));
	        }
	        bw.close();
	        br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}

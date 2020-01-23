package uk.ac.cam.ceb.como.paper.enthalpy.data.preprocessing;

import org.junit.Test;

import uk.ac.cam.ceb.como.enthalpy.estimation.balanced_reaction.solver.reactiontype.ISDReactionType;
import uk.ac.cam.ceb.como.enthalpy.estimation.balanced_reaction.solver.reactiontype.ISGReactionType;
import uk.ac.cam.ceb.como.paper.enthalpy.utils.FolderUtils;

/**
 * 
 * @author nk510 (caresssd@hermes.cam.ac.uk)
 * 
 * Junit tests which generate chemical reactions and estimate enthalpy of formation for 25 Ti-based species.
 *
 */

public class DataPreProcessingTest {

	/**
	 * @author nk510 (caresssd@hermes.cam.ac.uk) Folder contains Gaussian files for
	 *         Ti-based species that are reference species used in EBR
	 *         pre-processing step of cross validation.
	 */
	
	static String srcCompoundsRef_ti = "test_data/Gaussian/ti/";
	

	/**
	 * @author nk510 (caresssd@hermes.cam.ac.uk) Folder contains Gaussian files for
	 *         HCO-based species that are reference species used in EBR
	 *         pre-processing step of cross validation.
	 */

	static String srcCompoundsRef_hco = "test_data/Gaussian/hco/";

	/**
	 * @author nk510 (caresssd@hermes.cam.ac.uk) The csv file for Ti-based target
	 *         species that are used in EBR pre-processing step of cross validation.
	 */

	static String srcRefPool_ti = "test_data/csv/ref_scaled_kJperMols_v8.csv";
	
	static String srcRefPool_hco = "test_data/csv/ref-enthalpy_scaled_kJperMol.csv";
	
	/**
	 * 
	 * @author nk510 (caresssd@hermes.cam.ac.uk) Destination folders that store
	 *         results generated by pre-processing module (Java code) of cross
	 *         validation Java code. It includes ISG and ISD reaction types
	 *         generated by Java code.
	 *
	 */
	
	static String destRList_ti_isg = "test_data/test_results/ti_isg/";

	static String destRList_ti_isd = "test_data/test_results/ti_isd/";
	
	static String destRList_hco_isg = "test_data/test_results/hco_isg/";
	
	static String destRList_hco_isd = "test_data/test_results/hco_isd/";
	

	/**
	 * 
	 * @author nk510 (caresssd@hermes.cam.ac.uk) Folder that contains files used by
	 *         GLPK solver. Before running this Junit tests, the folder below should
	 *         be created first.
	 * 
	 */
	
	static String tempFolder = "D:/Data-Philip/LeaveOneOutCrossValidation_temp/";
	
	/**
	 * @author NK510 (caresssd@hermes.cam.ac.uk)
	 * 
	 * 
	 */
	static String destRList_valid_test_results_ti_isg = "test_data/test_results/ti_isg/valid-test-results/";
	

	/**
	 * 
	 * @author nk510 (caresssd@hermes.cam.ac.uk) Number of runs.
	 * 
	 */

	static int[] ctrRuns = new int[] { 1 };

	/**
	 * 
	 * @author nk510 (caresssd@hermes.cam.ac.uk) Number of reactions that will be
	 *         generated for each species from target list.
	 * 
	 */

	static int[] ctrRes = new int[] { 1 }; // 1, 5, 15, 25 //25,50 // 1,2,3,4,5,6,7,8,9,10 //5

	/**
	 * 
	 * @author nk510 (caresssd@hermes.cam.ac.uk) Number of radicals.
	 * 
	 */

	static int[] ctrRadicals_0 = new int[] { 0 }; // 0, 1, 2, 3, 4, 5 //100
	static int[] ctrRadicals_1 = new int[] { 1 }; // 0, 1, 2, 3, 4, 5 //100
	static int[] ctrRadicals_5 = new int[] { 5 }; // 0, 1, 2, 3, 4, 5 //100
	
		
	/**
	 * 
	 * @author nk510 (caresssd@hermes.cam.ac.uk) Junit test that generates ISD type
	 *         of EBR reactions for selected Ti-species and estimates enthalpy of
	 *         formation for each reaction. Parameters used in these testings are:
	 *         - Number of runs: 1
	 *         - Number of reactions: 1
	 *         - Number of radicals: 5
	 *
	 * @throws Exception 
	 * 
	 */
	
//	@Test
	public void getDataPreProcessingISDReactionTi115Test() throws Exception {
		
		String folderName = new FolderUtils().generateUniqueFolderName("isd_Ti_115");
		
		DataPreProcessing dataPreProcessingISD = new DataPreProcessing();

		ISDReactionType isdReactionTypePreProcessing = new ISDReactionType();

		dataPreProcessingISD.getPreProcessingErrorBalanceReaction(folderName,srcCompoundsRef_ti, srcRefPool_ti, destRList_ti_isd, tempFolder, ctrRuns, ctrRes, ctrRadicals_5, isdReactionTypePreProcessing);

	}
	
	/**
	 * 
	 * @author nk510 (caresssd@hermes.cam.ac.uk) Junit test that generates ISG type
	 *         of EBR reactions for selected HCO-species and estimates enthalpy of
	 *         formation for each reaction. Parameters used in these testings are: 
	 *         - Number of runs: 1
	 *         - Number of reactions: 1
	 *         - Number of radicals: 0
	 *         
	 * @throws Exception 
	 * 
	 */
	
//	@Test
	public void getDataPreProcessingISGReactionHCO110Test() throws Exception {

		String folderName = new FolderUtils().generateUniqueFolderName("isg_HCO_110");
		
		DataPreProcessing dataPreProcessingISG = new DataPreProcessing();
		
		ISGReactionType isgReactionTypePreProcessing = new ISGReactionType(true);

		dataPreProcessingISG.getPreProcessingErrorBalanceReaction(folderName,srcCompoundsRef_hco, srcRefPool_hco, destRList_hco_isg, tempFolder, ctrRuns, ctrRes, ctrRadicals_0, isgReactionTypePreProcessing);
		
	}

	/**
	 * 
	 * @author nk510 (caresssd@hermes.cam.ac.uk) Junit test that generates ISD type
	 *         of EBR reactions for selected HCO-species and estimates enthalpy of
	 *         formation for each reaction. Parameters used in these testings are:
	 *         - Number of runs: 1
	 *         - Number of reactions: 1
	 *         - Number of radicals: 0
	 *         
	 * @throws Exception 
	 * 
	 */
	
//	@Test
	public void getDataPreProcessingISDReactionHCO110Test() throws Exception {

		String folderName = new FolderUtils().generateUniqueFolderName("isd_HCO_110");
		
		DataPreProcessing dataPreProcessingISD = new DataPreProcessing();
		
		ISDReactionType isdReactionTypePreProcessing = new ISDReactionType();

		dataPreProcessingISD.getPreProcessingErrorBalanceReaction(folderName,srcCompoundsRef_hco, srcRefPool_hco, destRList_hco_isd, tempFolder, ctrRuns, ctrRes, ctrRadicals_0, isdReactionTypePreProcessing);
		
	}
	
	/**
	 * 
	 * @author nk510 (caresssd@hermes.cam.ac.uk) Junit test that generates ISG type
	 *         of EBR reactions for selected HCO-species and estimates enthalpy of
	 *         formation for each reaction. Parameters used in these testings are:
	 *         - Number of runs: 1
	 *         - Number of reactions: 1
	 *         - Number of radicals: 5
	 *
	 * @throws Exception
	 * 
	 */
	
//	@Test
	public void getDataPreProcessingISGReactionHCO115Test() throws Exception {

		String folderName = new FolderUtils().generateUniqueFolderName("isg_HCO_115");
		
		DataPreProcessing dataPreProcessingISG = new DataPreProcessing();
		
		ISGReactionType isgReactionTypePreProcessing = new ISGReactionType(true);

		dataPreProcessingISG.getPreProcessingErrorBalanceReaction(folderName,srcCompoundsRef_hco, srcRefPool_hco, destRList_hco_isg, tempFolder, ctrRuns, ctrRes, ctrRadicals_5, isgReactionTypePreProcessing);
		
		
	}
	
	/**
	 * 
	 * @author nk510 (caresssd@hermes.cam.ac.uk) Junit test that generates ISD type
	 *         of EBR reactions for selected HCO-species and estimates enthalpy of
	 *         formation for each reaction. Parameters used in these testings are: 
	 *         - Number of runs: 1
	 *         - Number of reactions: 1
	 *         - Number of radicals: 5
	 *         
	 * @throws Exception 
	 * 
	 */
	
//	@Test
	public void getDataPreProcessingISDReactionHCO115Test() throws Exception {

		String folderName = new FolderUtils().generateUniqueFolderName("iss_HCO_115");
		
		DataPreProcessing dataPreProcessingISD = new DataPreProcessing();
		
		ISDReactionType isdReactionTypePreProcessing = new ISDReactionType();

		dataPreProcessingISD.getPreProcessingErrorBalanceReaction(folderName,srcCompoundsRef_hco, srcRefPool_hco, destRList_hco_isd, tempFolder, ctrRuns, ctrRes, ctrRadicals_5, isdReactionTypePreProcessing);		
	}
	
	
	/**
	 * 
	 * @author nk510 (caresssd@hermes.cam.ac.uk) Junit test that generates ISG type
	 *         of EBR for selected Ti-species and estimates enthalpy of
	 *         formation for each reaction. On each run, the results are stored in a
	 *         folders created as sub-folders of "ti_isg", "ti_isd", "hco_isg", "hco_isd". 
	 *         
	 *         Parameters used in these testings are: 
	 *         
	 *         - Number of runs: 1
	 *         - Number of reactions: 1
	 *         - Number of radicals: 5
	 * 
	 *         To run these Junit tests please go to
	 *         uk.ac.cam.ceb.como.enthalpy.estimation.balanced_reaction.solver.glpk.TerminalGLPKSolver class,
	 *         and uncomment "map.put("glpsol", System.getProperty("user.dir") + "/glpk/w32/glpsol"); " line in order to allow GLPK solver to work on Windows machine.
	 *
	 * 
	 */

	@Test
	public void getDataPreProcessingISGReactionTi115Test() throws Exception {
		
		String folderName = new FolderUtils().generateUniqueFolderName("isg_Ti_115");
		
		DataPreProcessing dataPreProcessingISG = new DataPreProcessing();
		
		ISGReactionType isgReactionTypePreProcessing = new ISGReactionType(true);

		dataPreProcessingISG.getPreProcessingErrorBalanceReaction(folderName,srcCompoundsRef_ti, srcRefPool_ti, destRList_ti_isg, tempFolder, ctrRuns, ctrRes, ctrRadicals_5, isgReactionTypePreProcessing);
	}
}

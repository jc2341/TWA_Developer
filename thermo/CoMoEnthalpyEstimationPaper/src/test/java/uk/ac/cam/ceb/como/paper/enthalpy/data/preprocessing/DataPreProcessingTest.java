package uk.ac.cam.ceb.como.paper.enthalpy.data.preprocessing;

import org.junit.Test;

import uk.ac.cam.ceb.como.enthalpy.estimation.balanced_reaction.solver.reactiontype.ISDReactionType;
import uk.ac.cam.ceb.como.enthalpy.estimation.balanced_reaction.solver.reactiontype.ISGReactionType;

/**
 * 
 * @author nk510 (caresssd@hermes.cam.ac.uk)
 *
 */

public class DataPreProcessingTest {

	/**
	 * @author nk510 (caresssd@hermes.cam.ac.uk) Folder contains Gaussian files for
	 *         Ti-based species that are reference species used in EBR
	 *         pre-processing step of cross validation.
	 */
	static String srcCompoundsRef = "test_data/Gaussian/g09/";

	/**
	 * @author nk510 (caresssd@hermes.cam.ac.uk) The csv file for Ti-based target
	 *         species that are used in EBR pre-processing step of cross validation.
	 */

	static String srcRefPool = "test_data/csv/ref_scaled_kJperMols_v8.csv";

	/**
	 * @author nk510 (caresssd@hermes.cam.ac.uk) Destination folders that store
	 *         results generated by pre-processing module (Java code) of cross
	 *         validation Java code. It includes ISG and ISD reaction types
	 *         generated by Java code.
	 */
	static String destRList_isg = "test_data/test_results/ti_isg/";

	static String destRList_isd = "test_data/test_results/ti_isd/";

	/**
	 * 
	 * @author nk510 (caresssd@hermes.cam.ac.uk) Folder that contains files used by
	 *         GLPK solver. Before running this Junit tests, the folder below should
	 *         be created first.
	 * 
	 */
	static String tempFolder = "D:/Data-Philip/LeaveOneOutCrossValidation_temp/";

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

	static int[] ctrRadicals = new int[] { 0 }; // 0, 1, 2, 3, 4, 5 //100

	/**
	 * 
	 * @author nk510 (caresssd@hermes.cam.ac.uk) Junit test that generates ISG type
	 *         of EBR reactions for selected species and estimates enthalpy of
	 *         formation for each reaction. On each run, the results are stored in a
	 *         unique folder created as sub-folders of "ti_isg" and "ti_isd".
	 * 
	 *         To run these Junit tests please go to
	 *         uk.ac.cam.ceb.como.enthalpy.estimation.balanced_reaction.solver.glpk.TerminalGLPKSolver class,
	 *         and uncomment "map.put("glpsol", System.getProperty("user.dir") + "/glpk/w32/glpsol"); " line in order to allow GLPK solver to work on Windows machine.
	 */

	@Test
	public void getDataPreProcessingISGReactionTest() throws Exception {

		DataPreProcessing dataPreProcessingISG = new DataPreProcessing();

		ISGReactionType isgReactionTypePreProcessing = new ISGReactionType(true);

		dataPreProcessingISG.getPreProcessingErrorBalanceReaction(srcCompoundsRef, srcRefPool, destRList_isg, tempFolder, ctrRuns, ctrRes, ctrRadicals, isgReactionTypePreProcessing);
	}
	
	/**
	 * 
	 * @author nk510 (caresssd@hermes.cam.ac.uk) Junit test that generates ISD type
	 *         of EBR reactions for selected species and estimates enthalpy of
	 *         formation for each reaction.
	 * @throws Exception 
	 * 
	 */
	@Test
	public void getDataPreProcessingISDReactionTest() throws Exception {
		
		DataPreProcessing dataPreProcessingISD = new DataPreProcessing();

		ISDReactionType isdReactionTypePreProcessing = new ISDReactionType();

		dataPreProcessingISD.getPreProcessingErrorBalanceReaction(srcCompoundsRef, srcRefPool, destRList_isd, tempFolder, ctrRuns, ctrRes, ctrRadicals, isdReactionTypePreProcessing);
		

	}
	

}
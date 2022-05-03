package mellow.ai.simulation.winPercEstimates;

import mellow.Constants;

public class ConstantsWP {

	public static final int MULT = 64;
	public static final int EGDE_NUMBER = Constants.GOAL_SCORE - 1;


	public static final String folderName = "winPercentageEstimateOutputs" + MULT + "\\";
	public static final String testCaseFileNormalRange = folderName + "PositiveEstimates.txt";
	public static final String testCaseFileNegRange = folderName + "NegativeEstimates.txt";
	public static final String testCaseFileEdgeRange = folderName + "EdgeOfWinningEstimates.txt";
	
	//Negative constants:
	
	// (int) (- Math.pow(2, 9))
	public static final int NEG_LOWER_LIMIT = -512;
	public static final int NEG_GRID_DIM_LENGTH = Constants.GOAL_SCORE/ConstantsWP.MULT + 1;
	
	public static final int INDEX_0_POINT = (0 - ConstantsWP.NEG_LOWER_LIMIT) / ConstantsWP.MULT;
}

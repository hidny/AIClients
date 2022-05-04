package mellow.ai.simulation.winPercEstimates;

import mellow.Constants;

public class ConstantsWP {
	
	//Resonable speed (About 1 day to run all of them)
	public static final int MULT = 64;
	
	//Debug:
	//public static final int MULT = 256;
	
	//Detailed but too slow (4 X slower than mult 64)
	//public static final int MULT = 32;
	
	public static final int EGDE_NUMBER = Constants.GOAL_SCORE - 1;


	public static final String folderName = "winPercentageEstimateOutputs" + MULT + "\\";
	public static final String testCaseFileNormalRange = folderName + "PositiveEstimates.txt";
	public static final String testCaseFileNegRange = folderName + "NegativeEstimates.txt";
	public static final String testCaseFileEdgeRange = folderName + "EdgeOfWinningEstimates.txt";
	
	//Negative constants:
	
	// (int) (- Math.pow(2, 9))
	public static final int NUM_TO_IGNORE = 0;
	public static final int NEG_LOWER_LIMIT = -512;
	public static final int NEG_GRID_DIM_LENGTH = ((Constants.GOAL_SCORE - NEG_LOWER_LIMIT)/ConstantsWP.MULT + 1) - NUM_TO_IGNORE;
	
	public static final int POS_GRID_DIM_LENGTH = Constants.GOAL_SCORE/ConstantsWP.MULT + 1;
	
	public static final int INDEX_0_POINT = (0 - ConstantsWP.NEG_LOWER_LIMIT) / ConstantsWP.MULT;

	// 999:
	public static final int MAX_SCORE_WITHOUT_WINNING = Constants.GOAL_SCORE - 1;

	//(Assumes that the edge number 999 is extra)
	public static final int FULL_SIZE_WP_TABLE = (Constants.GOAL_SCORE - ConstantsWP.NEG_LOWER_LIMIT) / ConstantsWP.MULT + 2;
	
}

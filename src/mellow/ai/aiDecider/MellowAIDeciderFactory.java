package mellow.ai.aiDecider;

import mellow.ai.simulation.MonteCarloMain;


public class MellowAIDeciderFactory {

	public static final int MONTE_CARLO_METHOD_AI_THOROUGH_TEST = 3;
	public static final int MONTE_CARLO_METHOD_AI = 2;
	public static final int FOLLOW_HARD_CODED_RULES_AI = 1;
	public static final int USER_INPUT_TESTCASES = 0;
	
	public static MellowAIDeciderInterface getAI(long aiType) {
		
		if(aiType == USER_INPUT_TESTCASES) {
			//Get user to play for you and crate testcases out of their play:
			return new MellowQueryUserForTestcase();

		} else if(aiType == FOLLOW_HARD_CODED_RULES_AI) {
			//Just follow the rules that were hard-coded:
			return new MellowBasicDecider(false);

		} else if(aiType == MONTE_CARLO_METHOD_AI) {
			//Use the monte carlo method to evaluate every possible action, then find the one that seems like the best:
			//(After an action is tried, we simulate the rest of the round of cards with ailevel == 1, then see how high the points got)
			return new MellowBasicDecider(true);

		} else if(aiType >= MONTE_CARLO_METHOD_AI_THOROUGH_TEST) {
			//Use the monte carlo method to evaluate every possible action, then find the one that seems like the best:
			//(After an action is tried, we simulate the rest of the round of cards with ailevel == 1, then see how high the points got)
			return new MellowBasicDecider(true, MonteCarloMain.NUM_SIMULATIONS_THOROUGH_AND_SLOW);

		}   else {

			//Default: (Just follow the rules that were hard-coded:
			return new MellowBasicDecider(false);
		}
		
		
	}
}

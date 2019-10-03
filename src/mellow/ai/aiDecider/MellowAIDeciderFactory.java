package mellow.ai.aiDecider;


public class MellowAIDeciderFactory {

	public static final int MONTE_CARLO_METHOD_AI = 2;
	public static final int FOLLOW_HARD_CODED_RULES_AI = 1;
	public static final int USER_INPUT_TESTCASES = 0;
	
	public static MellowAIDeciderInterface getAI(long aiLevel) {
		
		if(aiLevel == USER_INPUT_TESTCASES) {
			//Get user to play for you and crate testcases out of their play:
			return new MellowQueryUserForTestcase();

		} else if(aiLevel == FOLLOW_HARD_CODED_RULES_AI) {
			//Just follow the rules that were hard-coded:
			return new MellowBasicDecider(false);

		} else if(aiLevel >= MONTE_CARLO_METHOD_AI) {
			//Use the monte carlo method to evaluate every possible action, then find the one that seems like the best:
			//(After an action is tried, we simulate the rest of the round of cards with ailevel == 1, then see how high the points got)
			return new MellowBasicDecider(true);

		}   else {

			//Default: (Just follow the rules that were hard-coded:
			return new MellowBasicDecider(false);
		}
		
		
	}
}

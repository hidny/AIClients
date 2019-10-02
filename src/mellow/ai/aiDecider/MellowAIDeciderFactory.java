package mellow.ai.aiDecider;


public class MellowAIDeciderFactory {

	public static final int USER_INPUT_TESTCASES = 2;
	public static MellowAIDeciderInterface getAI(long aiLevel, boolean isFast) {
		if(aiLevel <= 1) {
			return new MellowBasicDecider(isFast);
		} else if(aiLevel == USER_INPUT_TESTCASES) {
			return new MellowQueryUserForTestcase();
		} else {
			//Default:
			return new MellowBasicDecider(isFast);
		}
		
		
	}
}

package mellow.ai;

public class MellowAIDeciderFactory {

	public static MellowAIDeciderInterface getAI(long aiLevel, boolean isFast) {
		if(aiLevel <= 1) {
			return new MellowBasicDecider(isFast);
		} else if(aiLevel == 2) {
			return new MellowBasicDecider(isFast);
		} else {
			//Default:
			return new MellowBasicDecider(isFast);
		}
		
		
	}
}

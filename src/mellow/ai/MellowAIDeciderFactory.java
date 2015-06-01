package mellow.ai;

public class MellowAIDeciderFactory {

	public static MellowAIDeciderInterface getAI(long aiLevel, boolean isFast) {
		return new MellowBasicDecider(isFast);
	}
}

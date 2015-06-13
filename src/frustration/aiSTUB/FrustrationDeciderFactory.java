package frustration.aiSTUB;

public class FrustrationDeciderFactory {

	public static FrustrationDeciderInterface getAI(long aiLevel, boolean isFast) {
		return new FrustrationDecider();
	}
}

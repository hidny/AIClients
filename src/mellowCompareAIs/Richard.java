package mellowCompareAIs;

import mellow.ai.aiDecider.MellowAIDeciderFactory;
import clientPlayers.ClientJoinerStarter;

public class Richard {
	public static void main(String[] args) {
		String autoArgs[] = {"mellow", "Richard", "mellowpy", MellowAIDeciderFactory.FOLLOW_HARD_CODED_RULES_AI + "", "fast"};
		try {
			ClientJoinerStarter.main(autoArgs);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

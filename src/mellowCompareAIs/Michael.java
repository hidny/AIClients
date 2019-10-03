package mellowCompareAIs;

import clientPlayers.ClientStarter;
import mellow.ai.aiDecider.MellowAIDeciderFactory;

//Will host and wait until there's a full house...

//In fast speed setting: 1 test game takes 70 seconds. (Pretty fast!)
public class Michael {
	public static void main(String[] args) {
		//String autoArgs[] = {"mellow", "Michael", "mellowpy", "" + MellowAIDeciderFactory.USER_INPUT_TESTCASES, "fast", "13", "37", "1"};
		String autoArgs[] = {"mellow", "garbageTestData", "mellowpy", "" + MellowAIDeciderFactory.MONTE_CARLO_METHOD_AI, "fast", "0", "0", "1"};
		try {
			ClientStarter.main(autoArgs);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

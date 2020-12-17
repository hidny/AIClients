package mellowGenTestcaseStarter;

import mellow.ai.aiDecider.MellowAIDeciderFactory;
import clientPlayers.ClientJoinerStarter;
import clientPlayers.ClientStarter;

public class Phil {

	public static void main(String[] args) {
		
		String autoArgs[] = {"mellow", "Phil", "mellowpy", MellowAIDeciderFactory.FOLLOW_HARD_CODED_RULES_AI + "", "fast"};
		//String autoArgs[] = {"mellow", "Phil", "mellowpy", MellowAIDeciderFactory.MONTE_CARLO_METHOD_AI + "", "fast"};
		try {
			ClientJoinerStarter.main(autoArgs);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

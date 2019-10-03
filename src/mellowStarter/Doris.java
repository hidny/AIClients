package mellowStarter;

import mellow.ai.aiDecider.MellowAIDeciderFactory;
import clientPlayers.ClientJoinerStarter;

public class Doris {

	public static void main(String[] args) {
		String autoArgs[] = {"mellow", "Doris", "mellowpy", MellowAIDeciderFactory.MONTE_CARLO_METHOD_AI + "", "fast"};
		try {
			ClientJoinerStarter.main(autoArgs);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

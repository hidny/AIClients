package mellowGenTestcaseStarter;

import mellow.ai.aiDecider.MellowAIDeciderFactory;
import clientPlayers.ClientJoinerStarter;
import clientPlayers.ClientStarter;

public class Monty {

	public static void main(String[] args) {
		
		String autoArgs[] = {"mellow", "Monty1000", "mellowpy", "" + MellowAIDeciderFactory.MONTE_CARLO_METHOD_AI, "fast"};
		
		try {
			ClientJoinerStarter.main(autoArgs);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

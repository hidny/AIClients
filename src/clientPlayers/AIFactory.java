package clientPlayers;

import mellow.ai.MellowAIListener;
import connect4ai.Connect4Listener;
import frustration.aiSTUB.FrustrationListener;

public class AIFactory {
	
	public static GamePlayerInterface getAIPlayer(String gameName, long aiLevel, boolean isFast) {
		if(gameName.toLowerCase().equals("mellow")) {
			if(aiLevel >= 0) {
				//, long aiLevel, String currentPlayerName and isFast
				return new MellowAIListener(aiLevel, isFast);
			}
		} else if(gameName.toLowerCase().startsWith("connect")) {
			return new Connect4Listener(aiLevel, isFast);
			
		} else if(gameName.toLowerCase().startsWith("frustration")) {
			return new FrustrationListener(aiLevel, isFast);
			
		}
		
		System.out.println("ERROR: unknown game name or ai level.");
		return null;
		
	}
}

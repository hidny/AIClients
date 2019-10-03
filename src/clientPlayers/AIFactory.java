package clientPlayers;

import mellow.ai.listener.MellowAIListener;
import connect4ai.Connect4Listener;
import euchre.ai.EuchreAIListener;
import frustration.aiSTUB.FrustrationListener;
import reversiai.ReversiListener;

public class AIFactory {
	
	public static GamePlayerInterface getAIPlayer(String gameName, long aiLevel, boolean isFast) {
		if(gameName.toLowerCase().equals("mellow")) {
			return new MellowAIListener(aiLevel);

		} else if(gameName.toLowerCase().startsWith("connect")) {
			return new Connect4Listener(aiLevel, isFast);
			
		} else if(gameName.toLowerCase().startsWith("frustration")) {
			return new FrustrationListener(aiLevel, isFast);
			
		} else if(gameName.toLowerCase().startsWith("reversi")) {
			return new ReversiListener(aiLevel, isFast);
			
		} else if(gameName.toLowerCase().startsWith("euchre")) {
			return new EuchreAIListener(aiLevel, isFast);
			
		}
		
		System.out.println("ERROR: unknown game name or ai level.");
		return null;
		
	}
}

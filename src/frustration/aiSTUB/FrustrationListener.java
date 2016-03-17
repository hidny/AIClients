package frustration.aiSTUB;

import clientPlayers.GamePlayerInterface;

//import connectfour.Position;
import deck.DeckFunctions;

import java.util.concurrent.locks.*;


public class FrustrationListener implements GamePlayerInterface {//change to final
	
	private FrustrationDeciderInterface gameAIAgent = null;
	
	private String currentPlayerName = null;
	
	public FrustrationListener(long aiLevel, boolean isFast) {
		gameAIAgent = FrustrationDeciderFactory.getAI(aiLevel, isFast);
	}
	
	public void resetName(String name) {
		this.currentPlayerName = name;
	}
	
	//pre: server message is only 1 transmission
	public String getClientResponse(String serverMessage) {
		System.out.println("Frustration ack received: " + serverMessage);
		if(serverMessage.contains("Your choices") || serverMessage.contains("MOVE PROPERLY!")) {
			return "/move " + gameAIAgent.getMove();
		} else {
			return null;
		}
		
		
	}
	
	

	
}

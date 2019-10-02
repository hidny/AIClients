package mellow.ai.aiDecider;


public interface MellowAIDeciderInterface{

	//players[0] is the current AI. players[1] is on the left of players[0] and so on in counter clockwise order.
	public void setNameOfPlayers(String players[]);

	public void resetStateForNewRound();
	
	public void receiveUnParsedMessageFromServer(String msg);
	
	public void setDealer(String playerName);

	
	public void receiveBid(String playerName, int bid);
	
	public void receiveCardPlayed(String playerName, String card);
	
	public void setCardsForNewRound(String cards[]);
	
	public void setNewScores(int teamAScore, int teanBScore);

	//TODO: only have the two below functions and make another interface for the rest of them.
	public String getCardToPlay();

	public String getBidToMake();

	
}

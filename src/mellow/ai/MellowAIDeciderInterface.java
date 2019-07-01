package mellow.ai;

public interface MellowAIDeciderInterface {

	public void receiveUnParsedMessageFromServer(String msg);
	
	public void setDealer(String playerName);

	//TODO:
	
	public void receiveBid(String playerName, int bid);
	
	public void getPlayedCard(String playerName, String card);
	
	public void setupCardsForNewRound(String cards[]);
	
	public void updateScores(int teamAScore, int teanBScore);
	
	public String getCardToPlay();

	public String getBidToMake();
	
	//players[0] is the current AI. players[1] is on the left of players[0] and so on in counter clockwise order.
	public void setNameOfPlayers(String players[]);
	
}

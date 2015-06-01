package mellow.ai;

public interface MellowAIDeciderInterface {

	public void setDealer(String playerName);

	//TODO:
	
	public void receiveBid(String playerName, int bid);
	
	public void getPlayedCard(String playerName, String card);
	
	public void setupCardsForNewRound(String cards[]);
	
	public void updateScores(int teamAScore, int teanBScore);
	
	public String getCardToPlay();

	public String getBidToMake();
	
	public void resetName(String name);
	
	public void setNameOfPlayers(String players[]);
	
}

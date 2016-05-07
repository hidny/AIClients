package euchre.ai;

public interface EuchreAIDeciderInterface {

	public void setDealer(String playerName);

	//TODO:
	
	public void receiveCall(String playerName, int bid);
	
	public void getPlayedCard(String playerName, String card);
	
	public void setupCardsForNewRound(String cards[]);
	
	public void updateScores(int teamAScore, int teanBScore);
	
	public String getCardToPlay();

	public String getCallToMake();
	
	public void setNameOfPlayers(String players[]);
	
}

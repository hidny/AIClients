package mellow.ai.cardDataModels;

public class StatsBetweenRounds {
	public int getAIScore() {
		return AIScore;
	}
	public int getOpponentScore() {
		return OpponentScore;
	}
	public int getDealerIndexAtStartOfRound() {
		return dealerIndexAtStartOfRound;
	}

	public void setDealerIndexAtStartOfRound(int dealerIndexAtStartOfRound) {
		this.dealerIndexAtStartOfRound = dealerIndexAtStartOfRound;
	}
	
	public void setScores(int aIScore, int opponentScore) {
		this.AIScore = aIScore;
		this.OpponentScore = opponentScore;
	}

	//Beginning of Round info: (Maybe Make an object?
	private int AIScore = 0;
	private int OpponentScore = 0;
	private int dealerIndexAtStartOfRound = -1;
	//End Beginning of Round info
}

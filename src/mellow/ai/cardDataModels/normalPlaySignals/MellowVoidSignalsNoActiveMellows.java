package mellow.ai.cardDataModels.normalPlaySignals;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.cardUtils.CardStringFunctions;

public class MellowVoidSignalsNoActiveMellows {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public static DataModel dataModel;
	
	public MellowVoidSignalsNoActiveMellows(DataModel dataModel) {
		this.dataModel = dataModel;
		initSignalVars();
	}
	
	public void resetCardSignalsForNewRound() {
		initSignalVars();
	}

	int hardMaxCardPlayed[][];
	int softMaxCardPlayed[][];
	
	public void initSignalVars() {
		hardMaxCardPlayed = new int[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		softMaxCardPlayed = new int[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		
		for(int i=0; i<hardMaxCardPlayed.length; i++) {
			for(int j=0; j<hardMaxCardPlayed[0].length; j++) {
				hardMaxCardPlayed[i][j] = -1;
				softMaxCardPlayed[i][j] = -1;
			}
		}
	}
	
	//TODO: active this on:
	//receiveCardPlayed
	
	//updateDataModelWithPlayedCard
	
	
	
	public void updateDataModelSignalsWithPlayedCard(String playerName, String card) {

		int playerIndex = dataModel.convertPlayerNameToIndex(playerName);
		int throwerIndex = dataModel.getCardsPlayedThisRound() % 4;
		
		//if(playerIndex == Constants.CURRENT_AGENT_INDEX) {
			//Don't feel like tracking own signals yet...
		//	return;
		//}
			
		
		
		//I'm going to start with the normal no mellow situation for now...
		if(dataModel.someoneBidMellow() == false
				|| dataModel.stillActiveMellow() == false) {
			
			//System.out.println("TEST: updateDataModelSignalsWithPlayedCard: ");
			
			
			if(throwerIndex == 0) {
				
				//Weak attempt to handle meaning of leading a king:
				//It didn't change a single test case... :(
				if(dataModel.getRankIndex(card) == dataModel.KING) {
					softMaxCardPlayed[playerIndex][CardStringFunctions.getIndexOfSuit(card)] = dataModel.getRankIndex(card);

				}
				
			} else if(throwerIndex > 0 ) {
				
				//This is the obvious case:
				if(dataModel.cardAGreaterThanCardBGivenLeadCard
						(dataModel.getCurrentFightWinningCardBeforeAIPlays(), card)
					) {
					//System.out.println("TEST: Signal: " + playerName + " probably doesn't have lower than " + card);
					//System.out.println("Currently winning card: " + dataModel.getCurrentFightWinningCardBeforeAIPlays());
					//System.out.println();
	
					//What if you don't want to go over partner?
					
					hardMaxCardPlayed[playerIndex][CardStringFunctions.getIndexOfSuit(card)] = dataModel.getRankIndex(card);
				
					//TODO: 
				}
				
				
				//what if you go over, but it's not master?? ...
				
			}
			
			//if player index index not 0:
				//for now, don't worrty about your own signals
			
			//else
			
					//updateMaxCardPlayed
					// 
		}
	}
	

	
	//TODO: implement later if it proves useful:
	public boolean playerSoftSignaledNoCardsOfSuit(String playerName, int suitIndex) {
		
		return playerStrongSignaledNoCardsOfSuit(playerName, suitIndex);
	}
	
	public boolean playerSoftSignaledNoCardsOfSuit(int playerIndex, int suitIndex) {	
		return playerStrongSignaledNoCardsOfSuit(playerIndex, suitIndex);
	}

	
	// A lot of functions just to answer a basic question...
	public boolean playerSignaledNoCardsOfSuit(String playerName, int suitIndex) {
		return playerStrongSignaledNoCardsOfSuit(playerName, suitIndex);
	}
	
	public boolean playerSignaledNoCardsOfSuit(int playerIndex, int suitIndex) {
		return playerStrongSignaledNoCardsOfSuit(playerIndex, suitIndex);
	}
	
	
	public boolean playerStrongSignaledNoCardsOfSuit(String playerName, int suitIndex) {
		int playerIndex = dataModel.convertPlayerNameToIndex(playerName);
		return playerStrongSignaledNoCardsOfSuit(playerIndex, suitIndex);
	}
	
	//TODO: try using this later...
	public boolean playerStrongSignaledNoCardsOfSuit(int playerIndex, int suitIndex) {

		int curMinRank = hardMaxCardPlayed[playerIndex][suitIndex] + 1;
		
		if(hardMaxCardPlayed[playerIndex][suitIndex] == -1 
				&& softMaxCardPlayed[playerIndex][suitIndex] != -1) {
			curMinRank = softMaxCardPlayed[playerIndex][suitIndex];
		}
		
		for(int rank=curMinRank; rank <= this.dataModel.ACE; rank++) {
			if(this.dataModel.getCardsCurrentlyHeldByPlayers()[playerIndex][suitIndex][rank] != dataModel.IMPOSSIBLE) {
				return false;
			}
		}
		
		
		return true;
	}
	//End functions.
	
	//TODO: have basic logic to use this...
	
	//For now, just make sure it works by printing what it thinks the signals are.
	

}

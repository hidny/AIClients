package mellow.ai.cardDataModels.normalPlaySignals;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.cardUtils.CardStringFunctions;

//TODO: Am I using this class?
//Are the functions in this class good?

//It seems like only:
// playerSignalledHighCardInSuit is being used here...

//TODO: maybe make signals for when a player let the opponent win...
//    That's a strong signal that can work...

//TODO: combine the MellowLetPartnerWinSignals with the MellowVoid signal and the yet to be made:
// MellowCouldntTakeIt signal to to figure out if other player is probably void.


//This handles the case where the 3rd or 4th thrower might have just let their partner win...

//WARNING: this signal might be broken for cases when there's an active mellow
public class MellowLetPartnerWinSignals {

	public DataModel dataModel;
	
	public MellowLetPartnerWinSignals(DataModel dataModel) {
		this.dataModel = dataModel;
		initSignalVars();
	}
	
	public void resetCardSignalsForNewRound() {
		initSignalVars();
	}
	
	public int softMinHighCardInHand[][];
	
	public void initSignalVars() {
		softMinHighCardInHand = new int[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		
		for(int i=0; i<softMinHighCardInHand.length; i++) {
			for(int j=0; j<softMinHighCardInHand[0].length; j++) {
				softMinHighCardInHand[i][j] = -1;
			}
		}
	}
	
	//For now, assume all suits are the same... (No trumping)
	public void updateDataModelSignalsWithPlayedCard(String playerName, String card) {

		//Never mind when there's a still a mellow that's active:
		if(dataModel.stillActiveMellow()) {
			return;
		}
		
		int playerIndex = dataModel.convertPlayerNameToIndex(playerName);
		int throwerIndex = dataModel.getCardsPlayedThisRound() % 4;

		//If 4th thrower takes it, 3rd thrower didn't really signal anything...
		// 3rd did a weak signal that I don't feel considering
		if(throwerIndex == 3) {
			checkForSignalCancellation(playerIndex, throwerIndex, card);
		}
		
		if(throwerIndex < 2) {
			return;
		}
		
		int suitIndex = CardStringFunctions.getIndexOfSuit(card);

		//TODO: maybe I should handle when both players could trump...
		//but that's more complicated and a weaker signal.
		if(this.dataModel.getSuitOfLeaderThrow() != suitIndex) {
			return;
		}
		
		String curFightWinner = this.dataModel.getCurrentFightWinningCardBeforeAIPlays();
		
		if(CardStringFunctions.getIndexOfSuit(curFightWinner) != suitIndex) {
			return;
		}
		
		if(dataModel.isMasterCard(curFightWinner)) {
			return;
		}
		
		if(dataModel.cardAGreaterThanCardBGivenLeadCard(card, curFightWinner)) {
			return;
		}
		
		//Handle last thrower because it's easier
		if(throwerIndex == 3
				&& curFightWinner.equals(this.dataModel.getCardSecondThrow())) {
			
			//System.out.println("DEBUG1 " + playerIndex + " " + suitIndex + " " + curFightWinner);
			this.softMinHighCardInHand[playerIndex][suitIndex] = dataModel.getRankNotPlayedCardClosestOverCurrentWinnerSameSuit(curFightWinner);
			
		} else if(throwerIndex == 2
				&& curFightWinner.equals(this.dataModel.getCardLeaderThrow())) {
			
			//TODO: this case is confusing...
			//because there could be maybe reasons lead throw wins with Q other than partner having A K
			//Example of confusion Michael->testcase331.txt
			
			
			//System.out.println("DEBUG2 " + playerIndex + " " + suitIndex + " " + curFightWinner);
			this.softMinHighCardInHand[playerIndex][suitIndex] = dataModel.getRankNotPlayedCardClosestOverCurrentWinnerSameSuit(curFightWinner);
			
		}
		
	}
	
	//TODO: The situation where: 
	//lead Q
	//partner goes low
	//4th plays A
	//should still count for something...
	//Handle nuance later...
	
	public void checkForSignalCancellation(int playerIndex, int throwerIndex, String card) {
		if(throwerIndex != 3) {
			return;
		}
		
		int suitIndex = CardStringFunctions.getIndexOfSuit(card);

		//TODO: what if 3rd thrower knew 4th was trumping???
		//That could be a strong signal...
		if(this.dataModel.getSuitOfLeaderThrow() != suitIndex) {
			return;
		}
		
		String curFightWinner = this.dataModel.getCurrentFightWinningCardBeforeAIPlays();
		
		if(dataModel.cardAGreaterThanCardBGivenLeadCard(card, curFightWinner)) {
			
			int leftOfIndex = (playerIndex + 3) % Constants.NUM_PLAYERS;
			if(softMinHighCardInHand[leftOfIndex][suitIndex] != -1) {
				softMinHighCardInHand[leftOfIndex][suitIndex] = -1;
			}
		}
		
	}
	
	//TODO: REPLACE!
	//TODO: I'm confused, isn't this in MellowVoidSignalsNoActiveMellows?
	public boolean playerSignalledHighCardInSuit(int playerIndex, int suitIndex) {
		
		if(softMinHighCardInHand[playerIndex][suitIndex] == -1) {
			return false;
		}
		
		if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(playerIndex, suitIndex)) {
			return false;
		}
		
		for(int rank = softMinHighCardInHand[playerIndex][suitIndex]; rank<=DataModel.ACE; rank++) {
			
			if( ! dataModel.isCardPlayedInRound(DataModel.getCardString(rank, suitIndex)) ) {
				return true;
			}
		}
		
		return false;
		
	}
}

package mellow.ai.cardDataModels.normalPlaySignals;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.cardUtils.CardStringFunctions;


//TODO
public class MellowSignalsBasedOnLackOfTricks {


	//TODO: adjust this estimate based on stolen tricks in the future...
	
	public static boolean playerCouldHaveAorKBasedOnTrickCount(DataModel dataModel, int playerIndex, int suitIndex) {
		
		//player still needs to make tricks.
		if(playerSignalledMoreTricks(dataModel, playerIndex) == false) {
			return false;
		}

		//A or K not player in round
		if(dataModel.isCardPlayedInRound(DataModel.getCardString(DataModel.ACE, suitIndex)) == false
				&& dataModel.isCardPlayedInRound(DataModel.getCardString(DataModel.KING, suitIndex)) == false) {
			return false;
		}
		
		//Player signalled having suit
		if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(playerIndex, suitIndex)) {
			return false;
		}
		return true;
	}
	
	public static boolean playerSignalledMoreTricks(DataModel dataModel, int playerIndex) {
		int numBid = dataModel.getBid(playerIndex);
		
		if(numBid == 0) {
			return false;
		}
		
		int numTricks= dataModel.getNumTricks(playerIndex);
		
		if(numTricks < numBid) {
			return true;
		} else {
			return false;
		}
		
	}
	
}

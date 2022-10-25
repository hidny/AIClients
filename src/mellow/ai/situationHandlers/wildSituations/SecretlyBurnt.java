package mellow.ai.situationHandlers.wildSituations;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.situationHandlers.NoMellowPlaySituation;
import mellow.ai.situationHandlers.SeatedLeftOfOpponentMellow;
import mellow.cardUtils.CardStringFunctions;
import mellow.ai.cardDataModels.handIndicators.NonMellowBidHandIndicators;

public class SecretlyBurnt {

	//If you don't want to change what to do, return null
	public static String handleBeingSecretlyBurnt(DataModel dataModel) {
		System.out.println("(Secretly Burned)");
		
		int throwIndex = dataModel.getCardsPlayedThisRound() % Constants.NUM_PLAYERS;
		
		if(dataModel.getIndexOfCurrentlyWinningPlayerBeforeAIPlays() == Constants.CURRENT_PARTNER_INDEX) {
			return null;
		}
		
		if(throwIndex >= 2) {
			String winnerCard = dataModel.getCurrentFightWinningCardBeforeAIPlays();
			
			//Check if we could take it:
			if(CardStringFunctions.getIndexOfSuit(winnerCard) == dataModel.getSuitOfLeaderThrow()) {
				
				//No Trumping needed:
				if(dataModel.currentAgentHasSuit(dataModel.getSuitOfLeaderThrow())
						&& dataModel.couldPlayCardInHandOverCardInSameSuit(winnerCard)) {
					return dataModel.getCardInHandClosestOverCurrentWinner();
					
				} else if(! dataModel.currentAgentHasSuit(dataModel.getSuitOfLeaderThrow())
						&& dataModel.currentAgentHasSuit(Constants.SPADE)) {
					return dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
				}
				
			} else if(! dataModel.currentAgentHasSuit(dataModel.getSuitOfLeaderThrow())
					&& dataModel.currentAgentHasSuit(Constants.SPADE)
					&& dataModel.couldPlayCardInHandOverCardInSameSuit(winnerCard)) {
				return dataModel.getCardInHandClosestOverCurrentWinner();
			}
			
		} else if(throwIndex == 1) {
			
			//Second thrower logic:
			String winnerCard = dataModel.getCurrentFightWinningCardBeforeAIPlays();
			
			//I separated it because it might be a little different
			
			//No Trumping needed:
			if(dataModel.currentAgentHasSuit(dataModel.getSuitOfLeaderThrow())
					&& dataModel.couldPlayCardInHandOverCardInSameSuit(winnerCard)) {
				return dataModel.getCardInHandClosestOverCurrentWinner();
				
			} else if(! dataModel.currentAgentHasSuit(dataModel.getSuitOfLeaderThrow())
					&& dataModel.currentAgentHasSuit(Constants.SPADE)) {
				return dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
			}
			
			
		}
		
		//Play lowish
		if(dataModel.currentAgentHasSuit(dataModel.getSuitOfLeaderThrow())) {
			return SeatedLeftOfOpponentMellow.getHighestPartOfGroup(
					dataModel, 
					dataModel.getCardCurrentPlayerGetLowestInSuit(dataModel.getSuitOfLeaderThrow()));
		}
		
		return null;
	}

}

package mellow.ai.situationHandlers.wildSituations;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.cardDataModels.handIndicators.NonMellowBidHandIndicators;
import mellow.ai.situationHandlers.NoMellowPlaySituation;
import mellow.ai.situationHandlers.SeatedLeftOfOpponentMellow;

public class SeatedLeftOfMellowFinalRound {

	public static String handleLeftOfMellowFinalRound(DataModel dataModel) {

		
		System.out.println("(Left of Mellow Final Round)");
		
		int minTricksNeeded = NearEndOfGameHelperFunctions.getMinNumTricksToWinAfterBidsNoMellowBurnIfPossible(dataModel);

		
		int throwIndex = dataModel.getCardsPlayedThisRound() % Constants.NUM_PLAYERS;
		
		if(throwIndex == 3) {
			//Just take it
			
			int indexWinner = dataModel.getIndexOfCurrentlyWinningPlayerBeforeAIPlays();
			
			
			System.out.println("(Left of Mellow Final Round Take it!)");
			
			if(minTricksNeeded != -1
					&& dataModel.getNumTricksCurTeam() + dataModel.currentPlayerGetNumMasterSpadeInHand() 
						< minTricksNeeded
				&& 
				 ( indexWinner == Constants.LEFT_PLAYER_INDEX
					|| (indexWinner == Constants.RIGHT_PLAYER_INDEX
					&& dataModel.getNumTricksCurTeam() + dataModel.currentPlayerGetNumMasterSpadeInHand() + 1 == minTricksNeeded
						)
				 )
			   ) {
				
				if(indexWinner == Constants.RIGHT_PLAYER_INDEX) {
					System.out.println("(Save opponent mellow FTW!)");
				}
				
				//Take away the lead player's trick:
				String curWinner = dataModel.getCardLeaderThrow();
			
				if(dataModel.currentAgentHasSuit(dataModel.getSuitOfLeaderThrow())
						&& dataModel.couldPlayCardInHandOverCardInSameSuit(curWinner)) {
					
					return dataModel.getCardInHandClosestOverCurrentWinner();

				} else if(! dataModel.currentAgentHasSuit(dataModel.getSuitOfLeaderThrow())
						&& dataModel.currentAgentHasSuit(Constants.SPADE)
						) {
					return dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);

				}
				
			}
		}
		
		//Play kequiv or qequiv and mess with opponents:
		if(throwIndex == 0) {
			for(int suitIndex=0; suitIndex<Constants.NUM_SUITS; suitIndex++) {
				
				if(suitIndex == Constants.SPADE || !dataModel.currentAgentHasSuit(suitIndex)) {
					continue;
				}

				if(     (NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, suitIndex)
						|| NonMellowBidHandIndicators.hasKQEquivAndNoAEquiv(dataModel,suitIndex)
						)
						&&
						dataModel.getNumCardsPlayedForSuit(suitIndex) >= 3
						&&
						! dataModel.signalHandler.mellowBidderSignalledNoCardOverCardSameSuit(dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex), Constants.RIGHT_PLAYER_INDEX)
						) {
					return dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex);
				}
			}
		}
		
		if(minTricksNeeded >=0) {
			return NoMellowPlaySituation.handleNormalThrow(dataModel);
		} else {
			//Need to burn mellow:
			return SeatedLeftOfOpponentMellow.playMoveSeatedLeftOfOpponentMellow(dataModel);
		}
	}
}

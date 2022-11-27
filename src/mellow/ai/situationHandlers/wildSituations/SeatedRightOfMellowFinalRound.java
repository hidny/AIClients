package mellow.ai.situationHandlers.wildSituations;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.cardDataModels.handIndicators.NonMellowBidHandIndicators;
import mellow.ai.situationHandlers.NoMellowPlaySituation;
import mellow.ai.situationHandlers.SeatedLeftOfOpponentMellow;
import mellow.ai.situationHandlers.SeatedRightOfOpponentMellow;
import mellow.ai.situationHandlers.bidding.BiddingNearEndOfGameFunctions;
import mellow.cardUtils.CardStringFunctions;

public class SeatedRightOfMellowFinalRound {

	public static String handleRightOfMellowFinalRound(DataModel dataModel) {
		System.out.println("(Right of Mellow Final Round)");
		
		//TODO: test
		int minTricksNeeded = NearEndOfGameHelperFunctions.getMinNumTricksToWinAfterBidsNoMellowBurnIfPossible(dataModel);
		
		int throwIndex = dataModel.getCardsPlayedThisRound() % Constants.NUM_PLAYERS;
		
		if(throwIndex == 2) {
			//Just take it
			
			String curWinner = dataModel.getCurrentFightWinningCardBeforeAIPlays();
			int indexWinner = dataModel.getIndexOfCurrentlyWinningPlayerBeforeAIPlays();
			
			
			//TODO: get num tricks needed
			if(minTricksNeeded != -1
					&& dataModel.getNumTricksCurTeam() + dataModel.currentPlayerGetNumMasterSpadeInHand() 
						< minTricksNeeded
				&& indexWinner == Constants.RIGHT_PLAYER_INDEX
				) {
				
				System.out.println("(Right of Mellow Final Round Take it!)");
				
				if(dataModel.currentAgentHasSuit(dataModel.getSuitOfLeaderThrow())
						&& CardStringFunctions.getIndexOfSuit(curWinner) == dataModel.getSuitOfLeaderThrow()
						&& dataModel.couldPlayCardInHandOverCardInSameSuit(curWinner)) {
					
					return dataModel.getCardInHandClosestOverCurrentWinner();
				} else if(! dataModel.currentAgentHasSuit(dataModel.getSuitOfLeaderThrow())
						&& dataModel.currentAgentHasSuit(Constants.SPADE)
						&& (CardStringFunctions.getIndexOfSuit(curWinner) != Constants.SPADE
							|| dataModel.couldPlayCardInHandOverCardInSameSuit(curWinner)
						)
						) {
					
					if(CardStringFunctions.getIndexOfSuit(curWinner) != Constants.SPADE) {
						return dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
					} else {
						return dataModel.getCardInHandClosestOverCurrentWinner();
					}
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
						! dataModel.signalHandler.mellowBidderSignalledNoCardOverCardSameSuit(dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex), Constants.LEFT_PLAYER_INDEX)
						) {
					return dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex);
				}
				//if(dataModel.signalHandler.mellow)
			}
		}
		
		if(minTricksNeeded >=0) {
			return NoMellowPlaySituation.handleNormalThrow(dataModel);
		} else {
			//Need to burn mellow:
			return SeatedRightOfOpponentMellow.playMoveSeatedRightOfOpponentMellow(dataModel);
		}
	}
	
	public static int getNumberOfTricksNeeded(DataModel dataModel) {
		
		return -1;
	}
}

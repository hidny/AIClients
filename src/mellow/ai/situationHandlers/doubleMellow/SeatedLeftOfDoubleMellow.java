package mellow.ai.situationHandlers.doubleMellow;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.situationHandlers.PartnerSaidMellowSituation;
import mellow.cardUtils.CardStringFunctions;
import mellow.cardUtils.DebugFunctions;

public class SeatedLeftOfDoubleMellow {

	public static int OPPONENT_MELLOW_INDEX = Constants.RIGHT_PLAYER_INDEX;
	
	public static String playMoveSeatedLeftOfDoubleMellow(DataModel dataModel) {
		
		System.out.println("Left of double mellow");
		
		int throwIndex = dataModel.getCardsPlayedThisRound() % Constants.NUM_PLAYERS;
		
		if(throwIndex == 0) {
			return AIHandleLeadDoubleMellow(dataModel);
		
		} else if(throwIndex == 3) {
			return AIHandleFourthDoubleMellow(dataModel);
		}
		return PartnerSaidMellowSituation.playMoveToProtectPartnerMellow(dataModel);
	}
	
	
	public static String AIHandleLeadDoubleMellow(DataModel dataModel) {
		
		
		double bestValue = -10000.0;
		String bestCard = null;
		
		
		
		for(int suit=Constants.NUM_SUITS - 1; suit>=0; suit--) {
			
			if(dataModel.getNumCardsOfSuitInCurrentPlayerHand(suit) == 0) {
				continue;
			}
			
			String tempLowest = dataModel.getCardCurrentPlayerGetLowestInSuit(suit);

			double curValue = 0.0;
			String curCard = null;
			
			if(dataModel.signalHandler.mellowBidderSignalledNoCardOverCardSameSuit(tempLowest, OPPONENT_MELLOW_INDEX)) {
				continue;
			}
			
			if(dataModel.signalHandler.mellowBidderSignalledNoCardOverCardSameSuit(tempLowest, Constants.CURRENT_PARTNER_INDEX)) {
				
				curValue += dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suit);
				
				if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, suit)) {
					curValue += 2.0;
				}
				
				
				curCard = tempLowest;
				
			}
			
		
			if(curCard != null  && curValue > bestValue) {
				bestCard = curCard;
				bestValue = curValue;
			}
			
		}
		
		if(bestCard != null) {
			return bestCard;
		} else {
			return PartnerSaidMellowSituation.playMoveToProtectPartnerMellow(dataModel);
		}
	}
	
	
	public static int numSuitsSafeAttack(DataModel dataModel) {

		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "5S JH 7H 4H 8C 2C")) {
			System.out.println("Debug");
		}
		
		int ret = 0;
		for(int suit=Constants.NUM_SUITS - 1; suit>=0; suit--) {
			
			if(dataModel.getNumCardsOfSuitInCurrentPlayerHand(suit) == 0) {
				continue;
			}
			
			String tempLowest = dataModel.getCardCurrentPlayerGetLowestInSuit(suit);

			if(dataModel.signalHandler.mellowBidderSignalledNoCardOverCardSameSuit(tempLowest, OPPONENT_MELLOW_INDEX)) {
				continue;
			}
			
			if(dataModel.signalHandler.mellowBidderSignalledNoCardOverCardSameSuit(tempLowest, Constants.CURRENT_PARTNER_INDEX)) {
				ret++;
			}
		}
		
		return ret;
	}
	
	public static String AIHandleFourthDoubleMellow(DataModel dataModel) {
		
		
		String defaultProtectCard = PartnerSaidMellowSituation.playMoveToProtectPartnerMellow(dataModel);
		
		String curWinnerCard = dataModel.getCurrentFightWinningCardBeforeAIPlays();
		
		int indexWinner = dataModel.getIndexOfCurrentlyWinningPlayerBeforeAIPlays();
		
		int leadSuit = dataModel.getSuitOfLeaderThrow();
		
		if(dataModel.currentAgentHasSuit(leadSuit)) {
		
			if(indexWinner == Constants.LEFT_PLAYER_INDEX) {
				
				if(dataModel.couldPlayCardInHandOverCardInSameSuit(curWinnerCard)
						&& numSuitsSafeAttack(dataModel) > 0) {
					
					return dataModel.getCardInHandClosestOverCurrentWinner();
					
				} else if(dataModel.getSuitOfSecondThrow() == leadSuit
						&& dataModel.getNumCardsInCurrentPlayersHandOverCardSameSuit(dataModel.getCardSecondThrow()) >= 3 ){
					
					//Save low ones for later...
					return dataModel.getCardCurrentPlayerGetSecondHighestInSuit(leadSuit);
					
				} else {
				
					return defaultProtectCard;
				}
			 
			} else if(indexWinner == Constants.CURRENT_PARTNER_INDEX) {
					return defaultProtectCard;
			
			} else if(indexWinner == Constants.RIGHT_PLAYER_INDEX) {
				
				if(dataModel.getSuitOfThirdThrow() == leadSuit) {
					
					if(dataModel.couldPlayCardInHandUnderCardInSameSuit(curWinnerCard)) {
						return dataModel.getCardCurrentPlayerGetLowestInSuit(leadSuit);
					} else {
						return defaultProtectCard;
					}
					
				} else {
					return dataModel.getCardCurrentPlayerGetLowestInSuit(leadSuit);
				}
			}
				
		} else {
			//Current agent doesn't have lead suit:
			
			if(indexWinner == Constants.LEFT_PLAYER_INDEX) {
				
				if(dataModel.currentAgentHasSuit(Constants.SPADE)
						&& numSuitsSafeAttack(dataModel) > 0) {
					
					return dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
					
				} else {
					return defaultProtectCard;
				}
			 
			} else if(indexWinner == Constants.CURRENT_PARTNER_INDEX) {
					return defaultProtectCard;
			
			} else if(indexWinner == Constants.RIGHT_PLAYER_INDEX) {
				
				if( ! dataModel.currentPlayerOnlyHasSpade()) {
					
					if(CardStringFunctions.getIndexOfSuit(PartnerSaidMellowSituation.playMoveToProtectPartnerMellow(dataModel)) != Constants.SPADE) {
						return defaultProtectCard;
					} else {
						return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
					}
					
				} else {
					
					if(CardStringFunctions.getIndexOfSuit(curWinnerCard) == Constants.SPADE
							&& dataModel.couldPlayCardInHandUnderCardInSameSuit(curWinnerCard)) {
						
						return dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
						
					} else {
						return defaultProtectCard;
					}
				}
			}


		}
		
		return PartnerSaidMellowSituation.playMoveToProtectPartnerMellow(dataModel);
	}
	
	
	
	/*TODO: later:
	public static int numSuitsDanger(DataModel dataModel) {
		
		int ret = 0;
		for(int suit=Constants.NUM_SUITS - 1; suit>=0; suit--) {
			
			double curValue = 0.0;
			String curCard = null;
			
			dataModel.signalHandler.getMaxCardRankSignal(OPPONENT_MELLOW_INDEX, suit);
			if(dataModel.signalHandler.mellowBidderSignalledNoCardOverCardSameSuit(tempLowest, OPPONENT_MELLOW_INDEX)) {
				continue;
			}
			
			if(dataModel.signalHandler.mellowBidderSignalledNoCardOverCardSameSuit(tempLowest, Constants.CURRENT_PARTNER_INDEX)) {
				ret++;
			}
		}
		
		return ret;
	}*/
}

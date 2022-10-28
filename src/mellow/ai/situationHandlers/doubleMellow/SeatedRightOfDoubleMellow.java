package mellow.ai.situationHandlers.doubleMellow;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.cardDataModels.handIndicators.NonMellowBidHandIndicators;
import mellow.ai.situationHandlers.PartnerSaidMellowSituation;
import mellow.ai.situationHandlers.SeatedLeftOfOpponentMellow;
import mellow.ai.situationHandlers.SeatedRightOfOpponentMellow;
import mellow.cardUtils.DebugFunctions;

public class SeatedRightOfDoubleMellow {
	
	public static int OPPONENT_MELLOW_INDEX = Constants.LEFT_PLAYER_INDEX;
	
	public static String playMoveSeatedRightOfDoubleMellow(DataModel dataModel) {

		System.out.println("(RIGHT OF DOUBLE MELLOW)");
		
		int throwIndex = dataModel.getCardsPlayedThisRound() % Constants.NUM_PLAYERS;
		
		if(throwIndex == 0) {
			return AIHandleLeadDoubleMellow(dataModel);
		}
		

		int leadSuitIndex = dataModel.getSuitOfLeaderThrow();
		
		//Edge case:
		if(throwIndex == 1 
				&& (DesperadoFunctions.needToBurnOpponentMellowAtAllCosts(dataModel)
					|| DesperadoFunctions.wayBehindJustAttackOtherMellow(dataModel)
					)) {

			//TODO: maybe have custom code, but for now just take from elsewhere
			//TODO: So far, this is UNTESTED, so it's probably wrong!
			return SeatedRightOfOpponentMellow.AISecondThrow(dataModel);

			//Not what the test case asked for:
		}
		
		
		if(throwIndex == 1
				&& ! dataModel.currentAgentHasSuit(leadSuitIndex)
				&& 3 * dataModel.getNumberOfCardsOneSuit(Constants.SPADE) > dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE)
				&& dataModel.signalHandler.getNumCardsMellowSignalledPossibleInSuit(Constants.CURRENT_PARTNER_INDEX, leadSuitIndex)
					<= 1
				&& dataModel.signalHandler.getNumCardsMellowSignalledPossibleInSuit(Constants.CURRENT_PARTNER_INDEX, leadSuitIndex)
					<= dataModel.signalHandler.getNumCardsMellowSignalledPossibleInSuit(Constants.LEFT_PLAYER_INDEX, leadSuitIndex)
				&& ! dataModel.signalHandler.mellowBidderPlayerSignalNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)) {
					
			
			if(dataModel.signalHandler.getNumCardsMellowSignalledPossibleInSuit(Constants.CURRENT_PARTNER_INDEX, leadSuitIndex)
					== dataModel.signalHandler.getNumCardsMellowSignalledPossibleInSuit(Constants.LEFT_PLAYER_INDEX, leadSuitIndex)) {
				System.out.println("(Dubious trump case) (Maybe trump high when behind?)");
			
			} else if(NonMellowBidHandIndicators.getNumAorKorQEquiv(dataModel, Constants.SPADE) >= 2) {
				return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
				
			}
			
		}
			
		
		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "AS KS JS 7S QH JH 7H 6H TC 9C 8C ")) {
			System.out.println("DEBUG 123");
		}
		
		return PartnerSaidMellowSituation.playMoveToProtectPartnerMellow(dataModel);
	}
	
	
	public static String AIHandleLeadDoubleMellow(DataModel dataModel) {
		

		if(DesperadoFunctions.needToBurnOpponentMellowAtAllCosts(dataModel)) {
			//TODO: maybe you should have your own customized function:
			return SeatedRightOfOpponentMellow.AIHandleLead(dataModel);
		}
		
		
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
	
	//TODO: code to handle 2nd throw
	//Implement code once you have test cases for it.
}

package mellow.ai.situationHandlers.doubleMellow;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.situationHandlers.PartnerSaidMellowSituation;
import mellow.ai.situationHandlers.SeatedLeftOfOpponentMellow;
import mellow.ai.situationHandlers.SeatedRightOfOpponentMellow;

public class SeatedRightOfDoubleMellow {
	
	public static int OPPONENT_MELLOW_INDEX = Constants.LEFT_PLAYER_INDEX;
	
	public static String playMoveSeatedRightOfDoubleMellow(DataModel dataModel) {
		
		System.out.println("Right of double mellow");
		
		int throwIndex = dataModel.getCardsPlayedThisRound() % Constants.NUM_PLAYERS;
		
		if(throwIndex == 0) {
			return AIHandleLeadDoubleMellow(dataModel);
		}
		
		//Edge case:
		if(throwIndex == 1 
				&& (DesperadoFunctions.needToBurnOpponentMellowAtAllCosts(dataModel)
					|| DesperadoFunctions.wayBehindJustAttackOtherMellow(dataModel)
					)) {

			//TODO: maybe have custom code, but for now just take from elsewhere
			//TODO: So far, this is UNTESTED, so it's probably wrong!
			return SeatedRightOfOpponentMellow.AISecondThrow(dataModel);

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

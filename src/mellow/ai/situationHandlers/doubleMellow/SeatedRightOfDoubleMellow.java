package mellow.ai.situationHandlers.doubleMellow;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.situationHandlers.PartnerSaidMellowSituation;

public class SeatedRightOfDoubleMellow {
	
	public static int OPPONENT_MELLOW_INDEX = Constants.LEFT_PLAYER_INDEX;
	
	public static String playMoveSeatedRightOfDoubleMellow(DataModel dataModel) {
		
		System.out.println("Right of double mellow");
		
		int throwIndex = dataModel.getCardsPlayedThisRound() % Constants.NUM_PLAYERS;
		
		if(throwIndex == 0) {
			return AIHandleLeadDoubleMellow(dataModel);
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
	
	//TODO: code to handle 2nd throw
	//Implement code once you have test cases for it.
}

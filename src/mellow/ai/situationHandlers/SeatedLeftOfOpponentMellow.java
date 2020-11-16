package mellow.ai.situationHandlers;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.cardUtils.CardStringFunctions;

public class SeatedLeftOfOpponentMellow {

	

	
	//TODO
	public static String playMoveSeatedLeftOfOpponentMellow(DataModel dataModel) {
		
		int throwIndex = dataModel.getCardsPlayedThisRound() % Constants.NUM_PLAYERS;
		
		//Rule number one:
		//TODO: break this up later!
		
		if(throwIndex == 0) {

			//TODO:
			//handle lead
			
			//TODO: insert complicated lead logic here...
			return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
			
			
		//go under mellow if possible! (Maybe put into anther function?
		} else if(throwIndex > 0 && 
				dataModel.isPrevThrowWinningFight() ) {
			
			String mellowWinningCard = dataModel.getCurrentFightWinningCard();
			
			if(CardStringFunctions.getIndexOfSuit(mellowWinningCard) 
					!= dataModel.getSuitOfLeaderThrow()) {
				//Mellow player is trumping:
				
				if(dataModel.throwerMustFollowSuit()) {
					//Mellow player is trumping, but current player is not trumping:
					return dataModel.getCardCurrentPlayerGetLowestInSuit(dataModel.getSuitOfLeaderThrow());
					
				} else {
					if(dataModel.currentPlayerOnlyHasSpade() 
							&& dataModel.couldPlayCardInHandUnderCardInSameSuit(mellowWinningCard)) {
						
						//If you have to trump over, go big! (Over simplified, but whatever)
						return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
					
					} else {
						
						//TODO: this might need work, but I'm lazy
						return dataModel.getHighestOffSuitCardAnySuit();
					}
				}
			} else {
				
				if(dataModel.throwerMustFollowSuit()) {
					//Both follow suit
					
					if(dataModel.couldPlayCardInHandUnderCardInSameSuit(mellowWinningCard)) {
						return dataModel.getCardInHandClosestUnderSameSuit(mellowWinningCard);
					
					} else {
						return dataModel.getCardCurrentPlayerGetHighestInSuit(dataModel.getSuitOfLeaderThrow());
					}
					
				} else {
					
					//If can't follow suit:
					if(dataModel.currentAgentHasSuit(Constants.SPADE)) {
						//play biggest spade if you can... over-simplified
						return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
					} else {
						//play big off suit to mess-up mellow play (Over-simplified, but whatever)
						return dataModel.getHighestOffSuitCardAnySuit();
					}
				
				}
			}

			//End go under mellow if possible logic
			
			
		} else if(throwIndex > 0 && 
				dataModel.isPrevThrowWinningFight() == false) {

			//handle case where mellow is already safe:
			
			if(dataModel.throwerMustFollowSuit()) {
				dataModel.getCardCurrentPlayerGetHighestInSuit(dataModel.getSuitOfLeaderThrow());
				
			} else {
				if(dataModel.currentAgentHasSuit(Constants.SPADE)) {
					dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);

				} else {
					
					//TODO: This might be too much, but whatever...
					return dataModel.getHighestOffSuitCardAnySuit();
				}
			}
			
			//end handle case where mellow is already safe:
			
		}
		
		
		//TODO: don't be lazy in future (i.e. fill this up!)
		return NoMellowBidPlaySituation.handleNormalThrow(dataModel);
	}
	
	
}

package mellow.ai.situationHandlers;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;

public class SeatedRightOfOpponentMellow {


	//TODO: figure out how to play before a mellow (this is a hard position...)
	//Knowing when to trump is complicated...
	
	public static int MELLOW_PLAYER_INDEX = 1;
	
	public static String playMoveSeatedRightOfOpponentMellow(DataModel dataModel) {
		
		int throwIndex = dataModel.getCardsPlayedThisRound() % Constants.NUM_PLAYERS;
		
		
		if(throwIndex == 0) {
			//handle lead
			
			//TODO: insert complicated lead logic here... (for example: make sure mellow player has card in suit)
			return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
		
		} else if(throwIndex == 1) {
			
			int leadSuit = dataModel.getSuitOfLeaderThrow();
			String leaderThrow = dataModel.getCardLeaderThrow();
			
			if(dataModel.throwerMustFollowSuit()) {
			
				//Handle being the second thrower and following suit...
				if(dataModel.couldPlayCardInHandOverCardInSameSuit(leaderThrow)) {
					
					String cardInHandClosestOver = dataModel.getCardInHandClosestOverSameSuit(leaderThrow);
					
					if(dataModel.mellowSignalledNoCardBetweenTwoCards(leaderThrow, cardInHandClosestOver, MELLOW_PLAYER_INDEX)) {
						
						//TODO: We may not want to lead every single time we can...
						//HANDLE this complication LATER!
						return cardInHandClosestOver;
						
					} else {
						
						if(dataModel.couldPlayCardInHandUnderCardInSameSuit(leaderThrow)) {
							return dataModel.getCardInHandClosestUnderSameSuit(leaderThrow);
						} else {
							
							return dataModel.getCardCurrentPlayergetLowestInSuit(leadSuit);
						}
					}
				} else {
					
					//Just throw away the highest card you got under the protector's lead.... it's a safe play
					//TODO: later: make a more nuanced play...
					//Example: If you have 4+ cards, maybe 2nd best is ok...
					return dataModel.getCardCurrentPlayerGetHighestInSuit(leadSuit);
				}
				
			} else if(dataModel.throwerMustFollowSuit() == false 
					&& leadSuit != Constants.SPADE
					&& dataModel.currentAgentHasSuit(Constants.SPADE)) {
				

				//RANDOM TEST for mellowPlayerSignalNoCardsOfSuit
				if(dataModel.isVoid(MELLOW_PLAYER_INDEX, leadSuit) 
						&& dataModel.mellowPlayerSignalNoCardsOfSuit(MELLOW_PLAYER_INDEX, leadSuit) == false) {
					System.out.println("ERROR: mellowPlayerSignalNoCardsOfSuit didn't work!");
					System.exit(1);
				}
				//END RANDOM TEST

				//System.out.println("DEBUG TEST player with mellow on left tempted to trump:");
				
				if(dataModel.mellowPlayerSignalNoCardsOfSuit(MELLOW_PLAYER_INDEX, leadSuit) == false) {
					
					if(dataModel.mellowPlayerMayBeInDangerInSuit(MELLOW_PLAYER_INDEX, leadSuit) == false) {

						System.out.println("TESTING nov 15");
						
						int numCardsInOtherPeoplesHandsForSuit = dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(leadSuit);
						//System.out.println("DEBUG numCardsInOtherPeoplesHandsForSuit: " + numCardsInOtherPeoplesHandsForSuit);
							
						if(       (numCardsInOtherPeoplesHandsForSuit >= 7)
								|| numCardsInOtherPeoplesHandsForSuit >= 3 && dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, leadSuit)
								)
						{
							//Probably safe to trump high:
							return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
							
						} else {

								
							//Mellow could be able to trump under: don't trump!
							
							//TODO: make sure we have offsuit!
							//TODO: maybe think about what player is throwing off a little bit more??

							//(unless there's no choice but to trump)
							if(dataModel.currentPlayerOnlyHasSpade() == false) {
								return dataModel.getHighestOffSuitCardAnySuit();
							} else {
								//??
								return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
							}
						}
							
							
					} else {
						//Mellow could be in danger: don't trump (unless there's no choice)
						if(dataModel.currentPlayerOnlyHasSpade() == false) {
							return dataModel.getHighestOffSuitCardAnySuit();
						} else {
							//??
							return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
						}
					}
					
				} else {

					// Mellow player signaled no cards of suit don't trump!
					
					
					//TODO: make sure we have offsuit! (unless there's no choice but to trump)
					if(dataModel.currentPlayerOnlyHasSpade() == false) {
						return dataModel.getHighestOffSuitCardAnySuit();
					} else {
						//??
						return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
					}
				}
				
			}
			
			
			
		} else if(throwIndex == 2) {
			//TODO
			
		//Burn a mellow lead throw: (Very important to not mess this up!)
		} else if(throwIndex == 3 && 
				dataModel.getCardLeaderThrow().equals(dataModel.getCurrentFightWinningCard()) ) {
				//Mellow lead and losing (Like when grand-papa used to play)

			
			if(dataModel.throwerMustFollowSuit()
					&& dataModel.couldPlayCardInHandUnderCardInSameSuit(dataModel.getCardLeaderThrow())) {
				
				return dataModel.getCardCurrentPlayergetLowestInSuit(dataModel.getSuitOfLeaderThrow());
			
			} else if(dataModel.currentPlayerOnlyHasSpade() == false) {
				
				return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
			}
			
		}
		//End burn a mellow lead throw
		
		
		//TODO: this is wrong, but whatever...
		return NoMellowBidPlaySituation.handleNormalThrow(dataModel);
	}
	
	

	public static String AISecondThrow(DataModel dataModel) {
		String cardToPlay = null;
		//get suit to follow.
		
		System.out.println("2nd throw right of mellow");
		
		//play barely over (no card inbetween for mellow play to play
		//or barely under.
		
		//TODO
		return "";
	}
	
	public static String AIThirdThrow(DataModel dataModel) {
		
		//play same logic as 2nd throw except compare card to play with highest of 2 prev cards.
		
		return "";
	}
	
	public static String AIFourthThrow(DataModel dataModel) {
		
		//Copy/paste burn logic
		//plus play high...
		return "";
	}
}

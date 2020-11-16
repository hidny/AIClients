package mellow.ai.situationHandlers;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.cardUtils.CardStringFunctions;

public class SingleActiveMellowPlayer {

	public static String handleThrowAsSingleActiveMellowBidder(DataModel dataModel) {

		int throwIndex = dataModel.getCardsPlayedThisRound() % Constants.NUM_PLAYERS;
		//leader:
		String cardToPlay = null;
		System.out.println("**Inside get card to play");
		
		if(dataModel.burntMellow(Constants.CURRENT_AGENT_INDEX)) {
			System.err.println("ERROR: BURNT MELLOW, but entering mellow bidder logic");
			System.exit(1);
		}
		
		if(throwIndex == 0) {
			cardToPlay = AIMellowLead(dataModel);
			
		//Handle the common mellow follow case:
		} else {
			cardToPlay = AIMellowFollow(dataModel);
		}
		
		if(cardToPlay != null) {
			System.out.println("AI decided on " + cardToPlay);
		}
		
		return cardToPlay;
	}
	
	private static String AIMellowLead(DataModel dataModel) {
		
		String ret = "";
		if(dataModel.getNumberOfCardsOneSuit(Constants.SPADE) > 0) {
			

			//TODO: if highest Spade is Queen, maybe don't do this?
			//TOO complicated... :(
			ret = dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
		} else {
			ret = dataModel.getLowOffSuitCardToPlayElseLowestSpade();
		}
		
		return ret;
	}
	
	private static String AIMellowFollow(DataModel dataModel) {
		int leaderSuitIndex = dataModel.getSuitOfLeaderThrow();
		
		String cardToPlay = "";
		
		if(dataModel.currentAgentHasSuit(leaderSuitIndex)) {
			//Follow suit:
			
			//if no one has trump:
			String currentFightWinner = dataModel.getCurrentFightWinningCard();
			
			if(CardStringFunctions.getIndexOfSuit(currentFightWinner) == leaderSuitIndex) {
				
				if(dataModel.couldPlayCardInHandUnderCardInSameSuit(currentFightWinner)) {

					//TODO: check for the case where you'd rather play your 5 over the 4 even if you have a 2.

					//Play just below winning card if possible
					cardToPlay = dataModel.getCardInHandClosestUnderSameSuit(currentFightWinner);
				
				} else {
					//Play slightly above winning card and hope you'll get covered
					cardToPlay = dataModel.getCardInHandClosestOverSameSuit(currentFightWinner);
				}
			} else if(CardStringFunctions.getIndexOfSuit(currentFightWinner) == Constants.SPADE && leaderSuitIndex != Constants.SPADE) {
				//Someone trumped, play high
				cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(leaderSuitIndex);
				
			} else {
				System.err.println("ERROR in AIMellowFollow: The currently winning card should be the lead suit or spade.");
				System.exit(1);
			}
			
			
		} else {
			//Can't follow suit:
			
			//TODO: throw off card that gets rid of the most risk... not necessarily the spade.
			
			String currentFightWinner = dataModel.getCurrentFightWinningCard();
			if(CardStringFunctions.getIndexOfSuit(currentFightWinner) == Constants.SPADE) {
				//Play spade under trump (this isn't always a good idea, but...)
				//TODO: maybe consider not throwing off spade?
				
				if(dataModel.currentAgentHasSuit(Constants.SPADE)) {
					if(dataModel.couldPlayCardInHandUnderCardInSameSuit(currentFightWinner)) {
						//Play spade under trump
						return dataModel.getCardInHandClosestUnderSameSuit(currentFightWinner);
						 
					} else if(dataModel.getNumberOfCardsOneSuit(Constants.SPADE) == dataModel.getNumCardsInCurrentPlayerHand()) {
						
						//If mellow player only has spade over trump, play lower spade:
						return dataModel.getCardInHandClosestOverSameSuit(currentFightWinner);
					}
					
				}
			}

			//Algo that tries to throw off least desirable offsuit for mellow player
			//NOTE: this is BARELY, better than dataModel.getHighestOffSuitCardToLead(). LOL
			cardToPlay = getBestOffSuitCardToThrowOffAsMellowPlayer(dataModel);
		}
		
		return cardToPlay;
	
	}

	//Find the suit the mellow player wants to throw-off most:
	public static String getBestOffSuitCardToThrowOffAsMellowPlayer(DataModel dataModel) {
		int NO_SUIT_FOUND = -1;
		int chosenSuit = NO_SUIT_FOUND;
		double bestScore = Double.MIN_VALUE;
		
		for(int i=0; i<Constants.NUM_SUITS; i++) {
			if(i == Constants.SPADE) {
				continue;

			} else if(dataModel.currentAgentHasSuit(i) == false) {
				continue;
			}
			if(chosenSuit == -1) {
				chosenSuit = i;
			}
			
			double tmpScore = getWillingnessToThrowOffSuitAsMellowPlayer(dataModel, i);
			
			if(tmpScore > bestScore) {
				chosenSuit = i;
				bestScore = tmpScore;
			}
			
		}
		
		if(chosenSuit == NO_SUIT_FOUND) {
			//If mellow player has to play spade, go low
			//TODO: add logic about this.
			String cardTemp = dataModel.getLowOffSuitCardToPlayElseLowestSpade();
			
			return cardTemp;
		}

		return dataModel.getCardCurrentPlayerGetHighestInSuit(chosenSuit);
	}
	
	
	//The higher the number return, the more will the AI is to throw off the suit
	//For now, the numbers returned are between 0 and 1
	//pre condition: player has at least 1 card in suit input
	public static double getWillingnessToThrowOffSuitAsMellowPlayer(DataModel dataModel, int suit) {
		int numCardsInPlayNotInHand = Constants.NUM_RANKS 
								- dataModel.getNumCardsPlayedForSuit(suit)
								- dataModel.getNumCardsOfSuitInCurrentPlayerHand(suit);
		
		
		String cardToConsider = "";
		
		if(numCardsInPlayNotInHand > 6 && dataModel.getNumCardsOfSuitInCurrentPlayerHand(suit) >= 3) {
			cardToConsider = dataModel.getCardCurrentPlayergetThirdLowestInSuit(suit);

		} else if(numCardsInPlayNotInHand > 3 && dataModel.getNumCardsOfSuitInCurrentPlayerHand(suit) >= 2) {
			cardToConsider = dataModel.getCardCurrentPlayergetSecondLowestInSuit(suit);
			
		} else {
			cardToConsider = dataModel.getCardCurrentPlayerGetLowestInSuit(suit);

		}
		
		int numOver = dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(cardToConsider);
		int numUnder = dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(cardToConsider);
		
		//Check if suit is "safe":
		if(numUnder == 0) {
			return 0.0;
			
		} else {
			//Suit is not safe:
			double partnerCantCoverFactor = Math.pow(2.0/3.0, numOver);
		
		
			double giveUpOnSuitFactor = Math.pow(1.0/2.0, Math.max(0, dataModel.getNumCardsCurrentUserStartedWithInSuit(suit)));
		
			return partnerCantCoverFactor * giveUpOnSuitFactor;
		}
	}

}

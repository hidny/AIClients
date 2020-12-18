package mellow.ai.situationHandlers;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.cardUtils.CardStringFunctions;
import mellow.cardUtils.DebugFunctions;

public class SeatedRightOfOpponentMellow {


	//TODO: figure out how to play before a mellow (this is a hard position...)
	//Knowing when to trump is complicated...
	
	public static int MELLOW_PLAYER_INDEX = 1;
	public static int PROTECTOR_PLAYER_INDEX = 3;
	
	public static String playMoveSeatedRightOfOpponentMellow(DataModel dataModel) {
		
		int throwIndex = dataModel.getCardsPlayedThisRound() % Constants.NUM_PLAYERS;
		
		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "JS 9S 7S TH 9H 7H 5H TD 8D 3D 2D")) {
			System.out.println("Debug!");
		}
		
		if(throwIndex == 0) {
			//handle lead
			return AIHandleLead(dataModel);
		
		} else if(throwIndex == 1) {
			return AISecondThrow(dataModel);
			
		} else if(throwIndex == 2) {

			return AIThirdThrow(dataModel);
			
		//Burn a mellow lead throw: (Very important to not mess this up!)
		} else if(throwIndex == 3 ) {
			
			return AIFourthThrow(dataModel);
		}
		//End burn a mellow lead throw
		
		
		//TODO: this is wrong, but whatever...
		return NoMellowBidPlaySituation.handleNormalThrow(dataModel);
	}
	

	public static String AIHandleLead(DataModel dataModel) {
		
		int bestSuitIndex = -1;
		int lowestRankScore = Integer.MAX_VALUE;
		
		for(int suit=Constants.NUM_SUITS - 1; suit>=0; suit--) {
			if(dataModel.isVoid(Constants.CURRENT_PLAYER_INDEX, suit) ) {
				continue;
			}
			
			//TODO: should I treat the other off-suits differently than spades?
		
			String tempLowest = dataModel.getCardCurrentPlayerGetLowestInSuit(suit);
	
			if(dataModel.mellowSignalledNoCardOverCardSameSuit(tempLowest, MELLOW_PLAYER_INDEX) == false) {
				
				//TODO: instead of just returning, try grading the options!
				//Also playing always lowest isn't smart. Sometimes playing 2nd or 3rd lowest is smarter
				//(Save 2C for the end)
				
				int curLowestRankSuitScore = dataModel.getRankIndex(tempLowest);
				
				// pretend lowest spades have a higher rank to discourage use of spades:
				if(suit == Constants.SPADE) {
					curLowestRankSuitScore += 3.5;
				}
				
				if(curLowestRankSuitScore < lowestRankScore) {
					bestSuitIndex = suit;
					lowestRankScore = curLowestRankSuitScore;
				}
				
			}
		
		
		}
		
		if(bestSuitIndex != -1) {
			
			return leadLowButAvoidWastingLowestCardInSuit(dataModel, bestSuitIndex);
			
			
		} else {
		
			return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
		}
	}

	public static String AISecondThrow(DataModel dataModel) {

		int leadSuit = dataModel.getSuitOfLeaderThrow();
		String leaderThrow = dataModel.getCardLeaderThrow();
		
		if(dataModel.throwerMustFollowSuit()) {
		
			//Handle being the second thrower and following suit...
			if(dataModel.couldPlayCardInHandOverCardInSameSuit(leaderThrow)) {
				
				if(dataModel.isVoid(MELLOW_PLAYER_INDEX, leadSuit) == false) {
					String cardInHandClosestOver = dataModel.getCardInHandClosestOverSameSuit(leaderThrow);
					
					if(dataModel.mellowSignalledNoCardBetweenTwoCards(leaderThrow, cardInHandClosestOver, MELLOW_PLAYER_INDEX)) {
						
						//TODO: We may not want to lead every single time we can...
						//HANDLE this complication LATER!
						return cardInHandClosestOver;
						
					} else {
						
						if(dataModel.couldPlayCardInHandUnderCardInSameSuit(leaderThrow)) {
							return dataModel.getCardInHandClosestUnderSameSuit(leaderThrow);
						} else {
							
							return dataModel.getCardCurrentPlayerGetLowestInSuit(leadSuit);
						}
					}
				} else {
					//Play over protector if mellow is void:
					return dataModel.getCardClosestOverCurrentWinner();
				}
			} else {
				
				//Just throw away the highest card you got under the protector's lead.... it's a safe play
				//TODO: later: make a more nuanced play...
				//Example: If you have 4+ cards, maybe 2nd best is ok...
				if(dataModel.isVoid(MELLOW_PLAYER_INDEX, leadSuit) == false) {
					return dataModel.getCardCurrentPlayerGetHighestInSuit(leadSuit);
				} else {
					return dataModel.getCardCurrentPlayerGetLowestInSuit(leadSuit);
				}
			}
		
		//Handle being tempted to trump:
		} else if(dataModel.throwerMustFollowSuit() == false 
				&& leadSuit != Constants.SPADE
				&& dataModel.currentAgentHasSuit(Constants.SPADE)) {
			

			//RANDOM TEST for mellowPlayerSignalNoCardsOfSuit
			if(dataModel.isVoid(MELLOW_PLAYER_INDEX, leadSuit) 
					&& dataModel.mellowPlayerSignalNoCardsOfSuit(MELLOW_PLAYER_INDEX, leadSuit) == false) {
				System.err.println("ERROR: mellowPlayerSignalNoCardsOfSuit didn't work!");
				System.exit(1);
			}
			//END RANDOM TEST

			//System.out.println("DEBUG TEST player with mellow on left tempted to trump:");
			
			if(dataModel.mellowPlayerSignalNoCardsOfSuit(MELLOW_PLAYER_INDEX, leadSuit) == false) {
				
				if(dataModel.mellowPlayerMayBeInDangerInSuit(MELLOW_PLAYER_INDEX, leadSuit) == false) {

					
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
							return dataModel.getHighestOffSuitCardAnySuitButSpade();
						} else {
							//??
							return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
						}
					}
						
						
				} else {
					//Mellow could be in danger: don't trump (unless there's no choice)
					if(dataModel.currentPlayerOnlyHasSpade() == false) {
						return dataModel.getHighestOffSuitCardAnySuitButSpade();
					} else {
						//??
						return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
					}
				}
				
			} else {

				// Mellow player signaled no cards of suit don't trump!
				
				
				//TODO: make sure we have offsuit! (unless there's no choice but to trump)
				if(dataModel.currentPlayerOnlyHasSpade() == false) {
					return dataModel.getHighestOffSuitCardAnySuitButSpade();
				} else {
					//??
					return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
				}
			}
			
			
		} else if(dataModel.throwerMustFollowSuit() == false 
				&& dataModel.currentAgentHasSuit(Constants.SPADE) == false) {
		
			//TODO: do we need the tricks.
			//TODO: can we try to think about which suit to throw off?
			return dataModel.getHighestOffSuitCardAnySuitButSpade();
		}
		
		return NoMellowBidPlaySituation.handleNormalThrow(dataModel);
	}
		
	
	//TODO: this confuses me... review it!
	
	//I also copy/pasted AI second throw...
	public static String AIThirdThrow(DataModel dataModel) {

		int leadSuit = dataModel.getSuitOfLeaderThrow();
		String leaderThrow = dataModel.getCardLeaderThrow();
		
		String curStrongestCard = dataModel.getCurrentFightWinningCard();
		
		
		if(dataModel.throwerMustFollowSuit()) {

			//Handle being the third thrower and following suit...

			//see if protector trumped:
			if(CardStringFunctions.getIndexOfSuit(curStrongestCard) != leadSuit) {
				
				if(dataModel.isVoid(MELLOW_PLAYER_INDEX, leadSuit) == false) {

					return dataModel.getCardCurrentPlayerGetHighestInSuit(leadSuit);
				} else {
					return dataModel.getCardCurrentPlayerGetLowestInSuit(leadSuit);
				}
			
			} else if(dataModel.couldPlayCardInHandOverCardInSameSuit(curStrongestCard)) {
				
				if(dataModel.isVoid(MELLOW_PLAYER_INDEX, leadSuit) == false) {
					String cardInHandClosestOver = dataModel.getCardInHandClosestOverSameSuit(curStrongestCard);
					
					if(dataModel.mellowSignalledNoCardBetweenTwoCards(curStrongestCard, cardInHandClosestOver, MELLOW_PLAYER_INDEX)) {
						
						//TODO: We may not want to lead every single time we can...
						//HANDLE this complication LATER!
						return cardInHandClosestOver;
						
					} else {
						
						if(dataModel.couldPlayCardInHandUnderCardInSameSuit(curStrongestCard)) {

							return dataModel.getCardInHandClosestUnderSameSuit(curStrongestCard);
						} else {

							return dataModel.getCardCurrentPlayerGetLowestInSuit(leadSuit);
						}
					}
				} else {
					//Play over protector if mellow is void:

					return dataModel.getCardClosestOverCurrentWinner();
				}
			} else {
				
				//Just throw away the highest card you got under the protector's lead.... it's a safe play
				//TODO: later: make a more nuanced play...
				//Example: If you have 4+ cards, maybe 2nd best is ok...
				if(dataModel.isVoid(MELLOW_PLAYER_INDEX, leadSuit) == false) {

					return dataModel.getCardCurrentPlayerGetHighestInSuit(leadSuit);
				} else {

					return dataModel.getCardCurrentPlayerGetLowestInSuit(leadSuit);
				}
			}
			
		} else if(dataModel.throwerMustFollowSuit() == false 
				&& leadSuit != Constants.SPADE
				&& dataModel.currentAgentHasSuit(Constants.SPADE)) {

			
			if(CardStringFunctions.getIndexOfSuit(curStrongestCard) != leadSuit) {
				
				
				int indexSuitStrongestCard = CardStringFunctions.getIndexOfSuit(curStrongestCard);
				
				if(indexSuitStrongestCard != Constants.SPADE) {
					System.err.println("ERROR: index strongest card should be spade at this point! 234234");
					System.exit(1);
				}
				
				if(dataModel.mellowPlayerSignalNoCardsOfSuit(MELLOW_PLAYER_INDEX, leadSuit) == false) {

					return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
				
				} else {
					
					
					if(dataModel.couldPlayCardInHandOverCardInSameSuit(curStrongestCard)) {
						String cardInHandClosestOver = dataModel.getCardInHandClosestOverSameSuit(curStrongestCard);
						
						if(dataModel.mellowSignalledNoCardBetweenTwoCards(curStrongestCard, cardInHandClosestOver, MELLOW_PLAYER_INDEX)) {
							
							//TODO: We may not want to lead every single time we can...
							//HANDLE this complication LATER!
							
	
							return cardInHandClosestOver;
							
						} else {
							
							if(dataModel.couldPlayCardInHandUnderCardInSameSuit(curStrongestCard)) {
	
								return dataModel.getCardInHandClosestUnderSameSuit(curStrongestCard);
							} else {
								
								//TODO: maybe play low spade if it's the last 2 or 3...?
								//not a copy...
	
								return dataModel.getCardCurrentPlayerGetLowestInSuit(indexSuitStrongestCard);
							}
						}
						
						///END: couldPlayCardInHandOverCardInSameSuit
					} else {
						return dataModel.getCardCurrentPlayerGetHighestInSuit(indexSuitStrongestCard); 
					}
				}
			}

			//RANDOM TEST for mellowPlayerSignalNoCardsOfSuit
			if(dataModel.isVoid(MELLOW_PLAYER_INDEX, leadSuit) 
					&& dataModel.mellowPlayerSignalNoCardsOfSuit(MELLOW_PLAYER_INDEX, leadSuit) == false) {
				System.err.println("ERROR: mellowPlayerSignalNoCardsOfSuit didn't work!");
				System.exit(1);
			}
			//END RANDOM TEST

			//System.out.println("DEBUG TEST player with mellow on left tempted to trump:");
			
			if(dataModel.mellowPlayerSignalNoCardsOfSuit(MELLOW_PLAYER_INDEX, leadSuit) == false) {
				
				if(dataModel.mellowPlayerMayBeInDangerInSuit(MELLOW_PLAYER_INDEX, leadSuit) == false) {

					
					int numCardsInOtherPeoplesHandsForSuit = dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(leadSuit);
					//System.out.println("DEBUG numCardsInOtherPeoplesHandsForSuit: " + numCardsInOtherPeoplesHandsForSuit);
						
					if(       (numCardsInOtherPeoplesHandsForSuit >= 6)
							|| numCardsInOtherPeoplesHandsForSuit >= 2 && dataModel.isVoid(PROTECTOR_PLAYER_INDEX, leadSuit)
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
							return dataModel.getHighestOffSuitCardAnySuitButSpade();
						} else {
							return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
						}
					}
						
						
				} else {
					//Mellow could be in danger: don't trump (unless there's no choice)
					if(dataModel.currentPlayerOnlyHasSpade() == false) {
						return dataModel.getHighestOffSuitCardAnySuitButSpade();
					} else {

						return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
					}
				}
				
			} else {

				// Mellow player signaled no cards of suit don't trump!
				
				//TODO: make sure we have offsuit! (unless there's no choice but to trump)
				if(dataModel.currentPlayerOnlyHasSpade() == false) {

					//TODO: why not play lower trump and dare mellow player to go under?
					return dataModel.getHighestOffSuitCardAnySuitButSpade();
				} else {

					//DEC 18th:
					//TODO: why not play lower trump just in case mellow in danger
					
					if(dataModel.mellowPlayerSignalNoCardsOfSuit(MELLOW_PLAYER_INDEX, Constants.SPADE) == false) {
						
						return dataModel.getHighestOffSuitCardAnySuitButSpade();
						
					} else {
						return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
					}
				}
			}
			
			
		} else if(dataModel.throwerMustFollowSuit() == false 
				&& dataModel.currentAgentHasSuit(Constants.SPADE) == false) {

			//Throw off high off-suit if can't follow suit and can't trump:
			
			//TODO: make sure we don't need the tricks
			//TODO: make sure the throw might possibly help the mellow
			
			//TODO: make it same logic as 2nd thrower?
			return dataModel.getHighestOffSuitCardAnySuitButSpade();
		}

		return NoMellowBidPlaySituation.handleNormalThrow(dataModel);
	}
	
	public static String AIFourthThrow(DataModel dataModel) {
		
		//Burn a mellow lead throw: (Very important to not mess this up!)
		if(	dataModel.getCardLeaderThrow().equals(dataModel.getCurrentFightWinningCard()) ) {
				//Mellow lead and losing (Like when grand-papa used to play)

			
			if(dataModel.throwerMustFollowSuit()
					&& dataModel.couldPlayCardInHandUnderCardInSameSuit(dataModel.getCardLeaderThrow())) {
				
				return dataModel.getCardCurrentPlayerGetLowestInSuit(dataModel.getSuitOfLeaderThrow());
			
			} else if(dataModel.currentPlayerOnlyHasSpade() == false) {
				
				return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
			} else {
				
				//System.err.println("WARNING: this condition means player has 13 spades!!!");
				//System.exit(1);
				return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
			}
			
		} else {
			
			if(dataModel.throwerMustFollowSuit()) {
				
				//TODO: don't always throw highest...
				return dataModel.getCardCurrentPlayerGetHighestInSuit(dataModel.getSuitOfLeaderThrow());
				
				
			} else {
				
				if(dataModel.isVoid(Constants.CURRENT_PLAYER_INDEX, Constants.SPADE) == false) {
					//TODO: what if highest spade is 5S??
					return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
				} else {
					//TODO: what if you have A,K,Q,J C and only QD??
					return dataModel.getHighestOffSuitCardAnySuitButSpade();
				}
			}
			
		}
				
	}
	

	//Try not to waste the killer small cards 1st time out.
	//This is a rough imitation of how I lead as a mellow attacker...
	//Since other family members don't really do this, it's probably not too important.
	
	//TODO: If I'm seating before the protecter, I usually play higher.
	//Myabe make another function to reflect this.
	
	public static String leadLowButAvoidWastingLowestCardInSuit(DataModel dataModel, int suitToPlay) {
		int numCardsCurPlayerHasOfSuit = dataModel.getNumberOfCardsOneSuit(suitToPlay);
		
		String consideredCard = dataModel.getCardCurrentPlayerGetLowestInSuit(suitToPlay);
		
		//TODO: I should Play higher before protect than before mellow...
		if(dataModel.getRankIndex(consideredCard) <= dataModel.RANK_FOUR
				&& dataModel.getNumCardsPlayedForSuit(suitToPlay) <= 2
				&& numCardsCurPlayerHasOfSuit >= 2) {
			
			String consideredCard2 = dataModel.getCardCurrentPlayergetSecondLowestInSuit(suitToPlay);
			
			if(dataModel.getRankIndex(consideredCard2) <= dataModel.RANK_FIVE) {
				
				if( numCardsCurPlayerHasOfSuit >= 3) {

					String consideredCard3 = dataModel.getCardCurrentPlayergetThirdLowestInSuit(suitToPlay);
				
					if(dataModel.getRankIndex(consideredCard3) <= dataModel.RANK_SIX) {
						
						return consideredCard3;
					} else {
						return consideredCard2;
					}
					
				} else {
					return consideredCard2;
				}
				

			} else {
				return consideredCard;
			}
			
		} else {
			return consideredCard;
		}
		
	}
}

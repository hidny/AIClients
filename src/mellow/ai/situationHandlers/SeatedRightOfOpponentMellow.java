package mellow.ai.situationHandlers;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.cardDataModels.handIndicators.NonMellowBidHandIndicators;
import mellow.cardUtils.CardStringFunctions;
import mellow.cardUtils.DebugFunctions;

public class SeatedRightOfOpponentMellow {

	
	public static int MELLOW_PLAYER_INDEX = 1;
	public static int PROTECTOR_PLAYER_INDEX = 3;
	
	public static String playMoveSeatedRightOfOpponentMellow(DataModel dataModel) {
		
		int throwIndex = dataModel.getCardsPlayedThisRound() % Constants.NUM_PLAYERS;
		
		
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
		return NoMellowPlaySituation.handleNormalThrow(dataModel);
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
	
			if(dataModel.signalHandler.mellowBidderSignalledNoCardOverCardSameSuit(tempLowest, MELLOW_PLAYER_INDEX) == false) {
				
				//TODO: instead of just returning, try grading the options!
				//Also playing always lowest isn't smart. Sometimes playing 2nd or 3rd lowest is smarter
				//(Save 2C for the end)
				
				int curLowestRankSuitScore = DataModel.getRankIndex(tempLowest);
				
				// pretend lowest spades have a higher rank to discourage use of spades:
				if(suit == Constants.SPADE) {
					curLowestRankSuitScore += 3.5;
					

					int numOver = dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(dataModel.signalHandler.getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(MELLOW_PLAYER_INDEX, Constants.SPADE));
					
					if(numOver >= 0) {
						//This is over-simplified, but whatever
						curLowestRankSuitScore += 10.0;
					}
					
				}

				long numStartedWithInSuit = dataModel.getNumCardsCurrentUserStartedWithInSuit(suit);
				
				curLowestRankSuitScore += 2.0 * numStartedWithInSuit;
				
				
				if(curLowestRankSuitScore < lowestRankScore) {
					bestSuitIndex = suit;
					lowestRankScore = curLowestRankSuitScore;
				}
				
				
			}
		
		
		}
		
		if(bestSuitIndex != -1) {
			
			//New:
			return getHighestCardYouCouldLeadWithoutSavingMellowInSuit(dataModel, bestSuitIndex);
			
		} else {
		
			return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
		}
	}
	
	//TODO: investigate placing this in seated left of mellow...
	// But: this might be less straight-forward left of mellow because you might be giving protector some info...
		
	public static String getHighestCardYouCouldLeadWithoutSavingMellowInSuit(DataModel dataModel, int suitIndex) {
		
		String ret = dataModel.getCardCurrentPlayerGetLowestInSuit(suitIndex);
		
		//1st make sure that's it's the first time out:
		
		if(dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex) > 6) {
			
			int numCardsInSuit = dataModel.getNumberOfCardsOneSuit(suitIndex);
			
			if(numCardsInSuit < 2) {
				return ret;
			}
			
			String secondSmallest = dataModel.getCardCurrentPlayergetSecondLowestInSuit(suitIndex);
			
			if(dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(secondSmallest) <= 2) {
				ret = secondSmallest;
			} else {
				return ret;
			}
			
			if(numCardsInSuit < 3) {
				return ret;
			}
			
			String thirdSmallest = dataModel.getCardCurrentPlayergetThirdLowestInSuit(suitIndex);
			
			if(dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(thirdSmallest) <= 2) {
				ret = thirdSmallest;
			} else {
				return ret;
			}
			
			if(numCardsInSuit < 4) {
				return ret;
			}
			
			String fourthSmallest = dataModel.getCardCurrentPlayergetFourthLowestInSuit(suitIndex);
			
			if(dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(fourthSmallest) <= 2) {
				return fourthSmallest;
			} else {
				return ret;
			}
			
		}
		
		return ret;
	}

	public static String AISecondThrow(DataModel dataModel) {


		
		int leadSuit = dataModel.getSuitOfLeaderThrow();
		String leaderThrow = dataModel.getCardLeaderThrow();
		
		if(dataModel.throwerMustFollowSuit()) {
		
			//Handle being the second thrower and following suit...
			if(dataModel.couldPlayCardInHandOverCardInSameSuit(leaderThrow)) {
				
				if(dataModel.isVoid(MELLOW_PLAYER_INDEX, leadSuit) == false) {
					String cardInHandClosestOver = dataModel.getCardInHandClosestOverSameSuit(leaderThrow);
					
					if(dataModel.signalHandler.mellowBidderSignalledNoCardBetweenTwoCards(leaderThrow, cardInHandClosestOver, MELLOW_PLAYER_INDEX)) {
						
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
					return dataModel.getCardInHandClosestOverCurrentWinner();
				}
			} else {
				
				
		//COPY
		//At this point, you have to play under the mellow protector:
			//TODO: put in function

				//Example: If you have 4+ cards, maybe 2nd best is ok...
				if(dataModel.isVoid(MELLOW_PLAYER_INDEX, leadSuit) == false
						&& dataModel.getBidTotal() < Constants.NUM_STARTING_CARDS_IN_HAND - 1) {
					
					
					//Find minimum card over highest card mellow signalled:
					
					String highestCardInHand = dataModel.getCardCurrentPlayerGetHighestInSuit(leadSuit);
					String curCard = highestCardInHand;
					
					for(int rank = DataModel.getRankIndex(highestCardInHand) - 1; rank>=DataModel.RANK_TWO; rank--) { 
						
						String tempCard = DataModel.getCardString(rank, leadSuit);
						
						if(dataModel.hasCard(tempCard)) {
							
							
							if(  dataModel.signalHandler.getNumCardsMellowBidderSignalledBetweenTwoCards(tempCard, leaderThrow, MELLOW_PLAYER_INDEX)
								    >= 2) {
									break;
							}

							curCard = tempCard;
						}
					}
					
					if(curCard == null) {
						System.err.println("ERROR: minCard ==null in SeatedRightOfOpponentMellow. This isn't supposed to happen");
						System.exit(1);
					}
					
					String cardToUse = curCard;
					if(dataModel.isMasterCard(cardToUse)) {
						
						//int numOther = dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(leadSuit);
						int numCardsInHand = dataModel.getNumCardsOfSuitInCurrentPlayerHand(leadSuit);
						
						//Rough rule to avoid wasting highest card in hand
						//I don't have enough test cases to know how to improve on this rule
						if(numCardsInHand >= 4 ) {
							cardToUse = dataModel.getCardCurrentPlayergetThirdLowestInSuit(leadSuit);
						}
						
					//Save highest spade if you have 5+ of them... (The rule isn't sophisticated, but at least it covers the obvious case)
					} else if(leadSuit == Constants.SPADE
							&& dataModel.getNumberOfCardsOneSuit(leadSuit) >= 5
						    && dataModel.cardAGreaterThanCardBGivenLeadCard(cardToUse, 
								 dataModel.getCardCurrentPlayergetFourthLowestInSuit(leadSuit))) {
						
						//TODO: Do a similar thing if you have 4 spades and the mellow played a spade?
						
						cardToUse= dataModel.getCardCurrentPlayergetFourthLowestInSuit(leadSuit);
					
					
					//Don't play highest if it might become master:
					} else if(curCard.equals(highestCardInHand) &&
							(
							(dataModel.getNumberOfCardsOneSuit(leadSuit) >= 5 && NonMellowBidHandIndicators.hasJEquiv(dataModel, leadSuit))
							|| (dataModel.getNumberOfCardsOneSuit(leadSuit) >= 4 && NonMellowBidHandIndicators.hasQEquiv(dataModel, leadSuit))
							|| (dataModel.getNumberOfCardsOneSuit(leadSuit) >= 3 && NonMellowBidHandIndicators.hasKEquiv(dataModel, leadSuit))
							|| (dataModel.getNumberOfCardsOneSuit(leadSuit) >= 3 && dataModel.currentPlayerHasMasterInSuit(leadSuit))
							)
						){
						
						//TODO: prioritize throwing away cards in groups like (789 or JQ)
						cardToUse = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(leadSuit);
					}
					
					
					return cardToUse;
			//END COPY
			//END TODO: put in function
					
				} else {
					return dataModel.getCardCurrentPlayerGetLowestInSuit(leadSuit);
				}
			}
		
		//Handle being tempted to trump:
		} else if(dataModel.throwerMustFollowSuit() == false 
				&& leadSuit != Constants.SPADE
				&& dataModel.currentAgentHasSuit(Constants.SPADE)) {
			

			if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "QS 7S 6S ")) {
				System.out.println("Debug");
			}
			
			//RANDOM TEST for mellowPlayerSignalNoCardsOfSuit
			if(dataModel.isVoid(MELLOW_PLAYER_INDEX, leadSuit) 
					&& dataModel.signalHandler.mellowBidderPlayerSignalNoCardsOfSuit(MELLOW_PLAYER_INDEX, leadSuit) == false) {
				System.err.println("ERROR: mellowPlayerSignalNoCardsOfSuit didn't work!");
				System.exit(1);
			}
			//END RANDOM TEST

			//System.out.println("DEBUG TEST player with mellow on left tempted to trump:");
			
			if(dataModel.signalHandler.mellowBidderPlayerSignalNoCardsOfSuit(MELLOW_PLAYER_INDEX, leadSuit) == false) {
				
				if(dataModel.signalHandler.mellowBidderPlayerMayBeInDangerInSuit(MELLOW_PLAYER_INDEX, leadSuit) == false
						|| 
						//Just assume mellow Bid doesn't have master card:
						(dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(leaderThrow) == 1
						   && dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(leadSuit) > 6)
						
						) {

					
					int numCardsInOtherPeoplesHandsForSuit = dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(leadSuit);
					//System.out.println("DEBUG numCardsInOtherPeoplesHandsForSuit: " + numCardsInOtherPeoplesHandsForSuit);
						
					if(      
							(numCardsInOtherPeoplesHandsForSuit >= 4
							&& dataModel.signalHandler.getNumCardsMellowSignalledPossibleInSuit(MELLOW_PLAYER_INDEX, leadSuit) > 1)
							|| (numCardsInOtherPeoplesHandsForSuit >= 3 && dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, leadSuit))
							)
					{
						
						
						//TODO: maybe save your highest trump and play a middle trump under certain conditions?
						
						//Probably safe to trump high:
						return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
						
					} else {

							
						//Mellow could be able to trump under: don't trump?
						
						//TODO: make sure we have offsuit!
						//TODO: maybe think about what player is throwing off a little bit more??

						//(unless there's no choice but to trump)
						if(dataModel.currentPlayerOnlyHasSpade() == false
								&& ! dataModel.isVoid(MELLOW_PLAYER_INDEX, Constants.SPADE)) {

							return SeatedLeftOfOpponentMellow.throwOffHighCardThatMightAccidentallySaveMellowAndTryToAvoidThrowingMasters(dataModel, MELLOW_PLAYER_INDEX);
							
						} else {
							//??
							return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
						}
					}
						
						
				} else {
					//Mellow could be in danger: don't trump (unless there's no choice)
					if(dataModel.currentPlayerOnlyHasSpade() == false) {
						//return dataModel.getHighestOffSuitCardAnySuitButSpade();
						return SeatedLeftOfOpponentMellow.throwOffHighCardThatMightAccidentallySaveMellowAndTryToAvoidThrowingMasters(dataModel, MELLOW_PLAYER_INDEX);
						
					} else {

						//??
						return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
					}
				}
				
			} else {

				// Mellow player signaled no cards of suit don't trump?
				
				//I decided to make it isVoid instead of signal, because I feel like you need to be sure before trump in front of mellow...
				
				//TODO: make sure we have offsuit! (unless there's no choice but to trump)
				if(dataModel.currentPlayerOnlyHasSpade() == false
						&& ! dataModel.isVoid(MELLOW_PLAYER_INDEX, Constants.SPADE)) {
					
					return SeatedLeftOfOpponentMellow.throwOffHighCardThatMightAccidentallySaveMellowAndTryToAvoidThrowingMasters(dataModel, MELLOW_PLAYER_INDEX);
				
				} else if(dataModel.currentPlayerOnlyHasSpade()){
					//Play 2nd lowest if you are stuck with lots of spade.
					if(dataModel.getNumCardsOfSuitInCurrentPlayerHand(Constants.SPADE) > 2) {
						return dataModel.getCardCurrentPlayergetSecondLowestInSuit(Constants.SPADE);
					} else {
						return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
					
					}
				} else if(dataModel.isVoid(MELLOW_PLAYER_INDEX, Constants.SPADE)) {
					
					if(dataModel.getNumCardsInCurrentPlayerHand() == dataModel.getNumCardsOfSuitInCurrentPlayerHand(Constants.SPADE) + 1
							&& dataModel.isMasterCard(leaderThrow)
							&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, leadSuit)
							&& !dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)) {
						//Fix a single test case:
						return dataModel.getJunkiestCardToFollowLead();
					} else {
						return dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
					}
				}
			}
			
			
		} else if(dataModel.throwerMustFollowSuit() == false 
				&& dataModel.currentAgentHasSuit(Constants.SPADE) == false) {
		

			return SeatedLeftOfOpponentMellow.throwOffHighCardThatMightAccidentallySaveMellowAndTryToAvoidThrowingMasters(dataModel, MELLOW_PLAYER_INDEX);
		}

		return NoMellowPlaySituation.handleNormalThrow(dataModel);
	}
		
	
	//TODO: this confuses me... review it!
	
	//I also copy/pasted AI second throw...
	public static String AIThirdThrow(DataModel dataModel) {

		int leadSuit = dataModel.getSuitOfLeaderThrow();
		String leaderThrow = dataModel.getCardLeaderThrow();
		
		String curStrongestCard = dataModel.getCurrentFightWinningCardBeforeAIPlays();
		
		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "AS JS TS 9S 3S 2S KH TH JC 8C 5C QD 5D")) {
			System.out.println("Debug");
		}
		
		if(dataModel.throwerMustFollowSuit()) {

			//Handle being the third thrower and following suit...

			//see if protector trumped:
			if(CardStringFunctions.getIndexOfSuit(curStrongestCard) != leadSuit) {
				
				if(dataModel.isVoid(MELLOW_PLAYER_INDEX, leadSuit) == false) {

					//Because only partner or mellow bidder can compete in this suit, feel free to play high:
					return dataModel.getCardCurrentPlayerGetHighestInSuit(leadSuit);
					
				} else {
					return dataModel.getCardCurrentPlayerGetLowestInSuit(leadSuit);
				}
			
			} else if(dataModel.couldPlayCardInHandOverCardInSameSuit(curStrongestCard)) {
				
				if(dataModel.isVoid(MELLOW_PLAYER_INDEX, leadSuit) == false) {
					String cardInHandClosestOver = dataModel.getCardInHandClosestOverSameSuit(curStrongestCard);
					
					if(dataModel.signalHandler.mellowBidderSignalledNoCardBetweenTwoCards(curStrongestCard, cardInHandClosestOver, MELLOW_PLAYER_INDEX)) {
						
						//TODO: We may not want to lead every single time we can...
						//HANDLE this complication LATER!
						
						//Play high, but not master:
						if(! dataModel.isMasterCard(SeatedLeftOfOpponentMellow.getHighestPartOfGroup(dataModel,
								cardInHandClosestOver))) {
							return SeatedLeftOfOpponentMellow.getHighestPartOfGroup(dataModel,
									cardInHandClosestOver);
						} else {
							return cardInHandClosestOver;
						}
						
					} else {
						
						if(dataModel.couldPlayCardInHandUnderCardInSameSuit(curStrongestCard)) {

							return dataModel.getCardInHandClosestUnderSameSuit(curStrongestCard);
						} else {

							//Play high, but not master:
							if(! dataModel.isMasterCard(SeatedLeftOfOpponentMellow.getHighestPartOfGroup(dataModel,
									cardInHandClosestOver))) {
								return SeatedLeftOfOpponentMellow.getHighestPartOfGroup(dataModel,
										cardInHandClosestOver);
							} else {
								return cardInHandClosestOver;
							}
						}
					}
				} else {
					//mellow is void in current suit, so play over protector if :

					return dataModel.getCardInHandClosestOverCurrentWinner();
				}
			} else {
		
		//TODO: put in function!
				//Just throw away the lowest card you won't need to help burn a mellow with.

				//Example: If you have 4+ cards, maybe 2nd best is ok...
				if(dataModel.isVoid(MELLOW_PLAYER_INDEX, leadSuit) == false) {
						//TODO: maybe?
						//&& dataModel.getBidTotal() < Constants.NUM_STARTING_CARDS_IN_HAND - 1

					//Find minimum card over highest card mellow signalled:
					
					String minCard = dataModel.getCardCurrentPlayerGetHighestInSuit(leadSuit);
					
					String topCardInLeadSuit = null;
					String secondThrow = dataModel.getCardSecondThrow();
					
					if(dataModel.cardAGreaterThanCardBGivenLeadCard(dataModel.getCardSecondThrow(), leaderThrow)) {
						
						//Did 2nd thrower trump?
						if(dataModel.getSuitOfSecondThrow() == Constants.SPADE
								&& dataModel.getSuitOfLeaderThrow() != Constants.SPADE) {
							topCardInLeadSuit = null;
						} else {
							topCardInLeadSuit = secondThrow;
						}
						
					} else {
						topCardInLeadSuit = leaderThrow;
					}
					
					for(int rank = DataModel.ACE; rank>=DataModel.RANK_TWO; rank--) { 
						
						String tempCard = DataModel.getCardString(rank, leadSuit);
						
						if(dataModel.hasCard(tempCard)) {
							
							
							if(topCardInLeadSuit != null) {
								
								if(dataModel.signalHandler.getNumCardsMellowBidderSignalledBetweenTwoCards(tempCard, topCardInLeadSuit, MELLOW_PLAYER_INDEX)
								    >= 2) {
									break;
								}
								
							} else {
							
								if(dataModel.signalHandler.getNumCardsMellowBidderSignalledOverCardSameSuit(tempCard, MELLOW_PLAYER_INDEX)
										>= 2) {
									break;
								}
							
							}
							
							minCard = tempCard;
						}
					}
					
					if(minCard == null) {
						System.err.println("ERROR: minCard ==null in SeatedRightOfOpponentMellow. This isn't supposed to happen");
						System.exit(1);
					}
					
					String cardToUse = minCard;
					if(dataModel.isMasterCard(cardToUse)) {
						
						//int numOther = dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(leadSuit);
						int numCardsInHand = dataModel.getNumCardsOfSuitInCurrentPlayerHand(leadSuit);
						
						//Rough rule to avoid wasting highest card in hand
						//I don't have enough test cases to know how to improve on this rule
						if(numCardsInHand >= 4 ) {
							cardToUse = dataModel.getCardCurrentPlayergetThirdLowestInSuit(leadSuit);
						}
						
					//COPY PASTA
					//Save highest spade if you have 5+ of them... (The rule isn't sophisticated, but at least it covers the obvious case)
					} else if(leadSuit == Constants.SPADE
							&& dataModel.getNumberOfCardsOneSuit(leadSuit) >= 5
						    && dataModel.cardAGreaterThanCardBGivenLeadCard(cardToUse, 
								 dataModel.getCardCurrentPlayergetFourthLowestInSuit(leadSuit))) {
						
						//TODO: Do a similar thing if you have 4 spades and the mellow played a spade?
						
						cardToUse= dataModel.getCardCurrentPlayergetFourthLowestInSuit(leadSuit);
						
					}
					//END COPY PASTA
					
					
					return cardToUse;
						
	//END COPY
	//END TODO: put in function
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
				
				if(dataModel.signalHandler.mellowBidderPlayerSignalNoCardsOfSuit(MELLOW_PLAYER_INDEX, leadSuit) == false) {

					return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
				
				} else {
					
					
					if(dataModel.couldPlayCardInHandOverCardInSameSuit(curStrongestCard)) {
						String cardInHandClosestOver = dataModel.getCardInHandClosestOverSameSuit(curStrongestCard);
						
						if(dataModel.signalHandler.mellowBidderSignalledNoCardBetweenTwoCards(curStrongestCard, cardInHandClosestOver, MELLOW_PLAYER_INDEX)) {
							
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
					&& dataModel.signalHandler.mellowBidderPlayerSignalNoCardsOfSuit(MELLOW_PLAYER_INDEX, leadSuit) == false) {
				System.err.println("ERROR: mellowPlayerSignalNoCardsOfSuit didn't work!");
				System.exit(1);
			}
			//END RANDOM TEST

			//System.out.println("DEBUG TEST player with mellow on left tempted to trump:");
					
			if(dataModel.signalHandler.mellowBidderPlayerSignalNoCardsOfSuit(MELLOW_PLAYER_INDEX, leadSuit) == false) {
				
				if(dataModel.signalHandler.mellowBidderPlayerMayBeInDangerInSuit(MELLOW_PLAYER_INDEX, leadSuit) == false) {

					
					int numCardsInOtherPeoplesHandsForSuit = dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(leadSuit);
					//System.out.println("DEBUG numCardsInOtherPeoplesHandsForSuit: " + numCardsInOtherPeoplesHandsForSuit);
						
					if(       (numCardsInOtherPeoplesHandsForSuit >= 3)
							|| (numCardsInOtherPeoplesHandsForSuit >= 2 && dataModel.isVoid(PROTECTOR_PLAYER_INDEX, leadSuit))
							|| (2 + dataModel.getNumCardsOfSuitInCurrentPlayerHand(Constants.SPADE)
							>= dataModel.getNumCardsInCurrentPlayerHand())
							)
					{
						
						//TODO: maybe save your highest trump and play a middle trump under certain conditions?
						
						//Probably safe to trump high:
						return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
						
						
					} else {

							
						//Mellow could be able to trump under: don't trump?
						
						//TODO: make sure we have offsuit!
						//TODO: maybe think about what player is throwing off a little bit more??

						//(unless there's no choice but to trump)
						if(dataModel.currentPlayerOnlyHasSpade() == false
								&& ! dataModel.isVoid(MELLOW_PLAYER_INDEX, Constants.SPADE)) {
							//return dataModel.getHighestOffSuitCardAnySuitButSpade();
							return SeatedLeftOfOpponentMellow.throwOffHighCardThatMightAccidentallySaveMellowAndTryToAvoidThrowingMasters(dataModel, MELLOW_PLAYER_INDEX);
							
						} else {
							return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
						}
					}
						
						
				} else {
					//Mellow could be in danger: don't trump (unless there's no choice)
					if(dataModel.currentPlayerOnlyHasSpade() == false) {
						return SeatedLeftOfOpponentMellow.throwOffHighCardThatMightAccidentallySaveMellowAndTryToAvoidThrowingMasters(dataModel, MELLOW_PLAYER_INDEX);
					} else {

						return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
					}
				}
				
			} else {

				// Mellow player signaled no cards of suit don't trump!
				
				//TODO: make sure we have offsuit!
				// and might as well trump if mellow player is void in spade...
				if( ! dataModel.currentPlayerOnlyHasSpade()
						//TODO: shouldn't make a diff...
						&& ! dataModel.signalHandler.mellowBidderPlayerSignalNoCardsOfSuit(MELLOW_PLAYER_INDEX, Constants.SPADE)) {

					//TODO: why not play lower trump and dare mellow player to go under?
					//return dataModel.getHighestOffSuitCardAnySuitButSpade();
					return SeatedLeftOfOpponentMellow.throwOffHighCardThatMightAccidentallySaveMellowAndTryToAvoidThrowingMasters(dataModel, MELLOW_PLAYER_INDEX);
					
				} else {

					//DEC 18th:
					//TODO: why not play lower trump just in case mellow in danger
					
					return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
					
				}
			}
			
			
		} else if(dataModel.throwerMustFollowSuit() == false 
				&& dataModel.currentAgentHasSuit(Constants.SPADE) == false) {

			return SeatedLeftOfOpponentMellow.throwOffHighCardThatMightAccidentallySaveMellowAndTryToAvoidThrowingMasters(dataModel, MELLOW_PLAYER_INDEX);
		}

		return NoMellowPlaySituation.handleNormalThrow(dataModel);
	}
	
	public static String AIFourthThrow(DataModel dataModel) {
		
		//Burn a mellow lead throw: (Very important to not mess this up!)
		if(	dataModel.getCardLeaderThrow().equals(dataModel.getCurrentFightWinningCardBeforeAIPlays()) ) {
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
					
					return SeatedLeftOfOpponentMellow.throwOffHighCardThatMightAccidentallySaveMellowAndTryToAvoidThrowingMasters(dataModel, MELLOW_PLAYER_INDEX);
				}
			}
			
		}
				
	}
	

	//Try not to waste the killer small cards 1st time out.
	//This is a rough imitation of how I lead as a mellow attacker...
	//Since other family members don't really do this, it's probably not too important.
	
	//TODO: If I'm seating before the protecter, I usually play higher.
	//Myabe make another function to reflect this.
	
	//TODO: replace with get Highest Card You could Lead function here,
	// and maybe in the seatedLeft file...
	/*
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
		
	}*/
	
}

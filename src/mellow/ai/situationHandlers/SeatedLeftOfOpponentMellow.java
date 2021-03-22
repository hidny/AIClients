package mellow.ai.situationHandlers;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.cardUtils.CardStringFunctions;
import mellow.cardUtils.DebugFunctions;

public class SeatedLeftOfOpponentMellow {

	

	public static int MELLOW_PLAYER_INDEX = 3;
	public static int PROTECTOR_PLAYER_INDEX = 1;
	
	//TODO
	public static String playMoveSeatedLeftOfOpponentMellow(DataModel dataModel) {
		
		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "AS QS JS 9S 3S 4H QD ")) {
			System.out.println("DEBUG");
		}
		
		int throwIndex = dataModel.getCardsPlayedThisRound() % Constants.NUM_PLAYERS;
		
		if(throwIndex == 0) {

			//TODO:
			//handle lead
			
			//TODO: insert complicated lead logic here...
			//return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
			
			return AIHandleLead(dataModel);
			
		//Mellow vulnerable: go under mellow if possible! (Maybe put into anther function?)
		} else if(throwIndex > 0 && 
				dataModel.isPrevThrowWinningFight() ) {
			
			String curWinningCard = dataModel.getCurrentFightWinningCardBeforeAIPlays();
			
			if(CardStringFunctions.getIndexOfSuit(curWinningCard) 
					!= dataModel.getSuitOfLeaderThrow()) {
				//Mellow player winning and is trumping:
				
				if(dataModel.throwerMustFollowSuit()) {
					//Mellow player is trumping, but current player is not trumping:
					return dataModel.getCardCurrentPlayerGetLowestInSuit(dataModel.getSuitOfLeaderThrow());
					
				} else {
					//Mellow player is trumping, and current player can't follow suit:
					if(dataModel.currentPlayerOnlyHasSpade()) {
						
						if(dataModel.couldPlayCardInHandUnderCardInSameSuit(curWinningCard)) {
							return dataModel.getCardInHandClosestUnderSameSuit(curWinningCard);
						
						} else {
							//lazy approx play highest spade if can't burn trumping mellow
							return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
						
						}
					} else {
						return throwOffHighCardThatMightAccidentallySaveMellow(dataModel, MELLOW_PLAYER_INDEX);
					}
				}
			} else {
				//Mellow player winning and is following suit:
				
				if(dataModel.throwerMustFollowSuit()) {
					//Both follow suit
					
					if(dataModel.couldPlayCardInHandUnderCardInSameSuit(curWinningCard)) {
						
						if(throwIndex < 3) {
							return dataModel.getCardInHandClosestUnderSameSuit(curWinningCard);
						} else {
							//If you are the last thrower, and mellow is burning: play low
							return dataModel.getCardCurrentPlayerGetLowestInSuit(dataModel.getSuitOfLeaderThrow());
						}
					
					} else {
						return dataModel.getCardCurrentPlayerGetHighestInSuit(dataModel.getSuitOfLeaderThrow());
					}
					
				} else {
					
					//If can't follow suit:
					if(dataModel.currentPlayerOnlyHasSpade()) {
						//play biggest spade if have no choice:
						//over simplified, but whatever...
						return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
					} else {
						//play big off suit to mess-up mellow play (Over-simplified, but whatever)
						return throwOffHighCardThatMightAccidentallySaveMellow(dataModel, MELLOW_PLAYER_INDEX);
					}
				
				}
			}

			//End go under mellow if possible logic
			
			
		} else if(throwIndex > 0 && 
				dataModel.isPrevThrowWinningFight() == false) {

			//handle case where mellow is already safe:
		
			if(dataModel.throwerMustFollowSuit()) {
				
				if(dataModel.isVoid(MELLOW_PLAYER_INDEX, dataModel.getSuitOfLeaderThrow())) {
					
					//lazy approx:
					return getHighestPartOfGroup(dataModel, NoMellowBidPlaySituation.handleNormalThrow(dataModel));
					
				} else {
					
					if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "TS 4S KH 3H 2H 8C 6C TD ")) {
						System.out.println("DEBUG");
					}
					//Play barely over max card mellow player signaled they have...
					//This strategy might be exploitable, but it takes a lot of imagination on the mellow player's part.
					
					if(dataModel.signalHandler.mellowPlayerSignalNoCardsOfSuit
							(MELLOW_PLAYER_INDEX, dataModel.getSuitOfLeaderThrow())) {
						
						//lazy approx:
						return getHighestPartOfGroup(dataModel, NoMellowBidPlaySituation.handleNormalThrow(dataModel));

					} else {

							String currentFightWinner = dataModel.getCurrentFightWinningCardBeforeAIPlays();
							
							String fourthThrowMinCardToWin = null;
							if(CardStringFunctions.getIndexOfSuit(currentFightWinner) == dataModel.getSuitOfLeaderThrow()
									&& dataModel.couldPlayCardInHandOverCardInSameSuit(currentFightWinner)) {
								
								if(throwIndex >= 3) {
									fourthThrowMinCardToWin = dataModel.getCardInHandClosestOverCurrentWinner();
								} else if(throwIndex >= 1 && throwIndex <= 2) {
									
									//Just try to play over maxMellowCard if it's the 3rd throw
									
									fourthThrowMinCardToWin = null;
								}
							}

							String minCardOverMaxMellowCard = null;
							String maxMellowCard = dataModel.signalHandler.getMaxRankCardMellowPlayerCouldHaveBasedOnSignals
									(MELLOW_PLAYER_INDEX, dataModel.getSuitOfLeaderThrow());
							
					
							if(dataModel.couldPlayCardInHandOverCardInSameSuit(maxMellowCard)) {
								minCardOverMaxMellowCard = dataModel.getCardInHandClosestOverSameSuit(maxMellowCard);
							}
							
							if(fourthThrowMinCardToWin != null && minCardOverMaxMellowCard != null) {

								if(  dataModel.cardAGreaterThanCardBGivenLeadCard(fourthThrowMinCardToWin, minCardOverMaxMellowCard)
									&& ! dataModel.getCardSecondThrow().equals(currentFightWinner)) {
									
									return getHighestPartOfGroup(dataModel, fourthThrowMinCardToWin);
									
								} else {
									String tempCardToRet = getHighestPartOfGroup(dataModel,
											minCardOverMaxMellowCard);
									
									String tempTestCard = getHighestPartOfGroup(dataModel,
											fourthThrowMinCardToWin);
									
									if( ! minCardOverMaxMellowCard.equals(fourthThrowMinCardToWin)
											&& tempCardToRet.equals(tempTestCard)) {
										System.out.println("DEBUG: there's a fork in the road here.");
										System.out.println("DEBUG: Do you take your partner's trick, or let win have it");
									}

									return tempCardToRet;
								}

							} else if(fourthThrowMinCardToWin != null && minCardOverMaxMellowCard == null) { 
								return getHighestPartOfGroup(dataModel, fourthThrowMinCardToWin);
								

							} else if(fourthThrowMinCardToWin == null && minCardOverMaxMellowCard != null) {
								
								String topCardInHand = getHighestPartOfGroup(dataModel, 
										minCardOverMaxMellowCard);
	
								//Think about not wasting soon to be master cards:
								if(dataModel.isMasterCard(topCardInHand)
										&& ! dataModel.cardAGreaterThanCardBGivenLeadCard(topCardInHand, currentFightWinner)) {
									
									//Think about not throwing away master 1
									if(3 * dataModel.getNumberOfCardsOneSuit(dataModel.getSuitOfLeaderThrow()) 
										> 
										dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(dataModel.getSuitOfLeaderThrow())
										&& dataModel.getNumberOfCardsOneSuit(dataModel.getSuitOfLeaderThrow()) >= 2
										) {
										
										String secondTopCardInHand = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(dataModel.getSuitOfLeaderThrow());
										
										if(dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(secondTopCardInHand)
										        ==
												  dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(topCardInHand)) {
											
											return topCardInHand;
										
										} else {
											
											return secondTopCardInHand;
										}
								
									} else {
										return topCardInHand;
									}
									
								} else {
								
									return topCardInHand;
								}
								
							} else {
								return dataModel.getCardCurrentPlayerGetHighestInSuit(dataModel.getSuitOfLeaderThrow());
								
							}
					}
					
					
					
				}
				
			} else if(dataModel.currentAgentHasSuit(Constants.SPADE)) {
				return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);

			} else {
				
				return throwOffHighCardThatMightAccidentallySaveMellow(dataModel, MELLOW_PLAYER_INDEX);
			}
		
			
			//end handle case where mellow is already safe:
			
		} else {
		
		
			//TODO: don't be lazy in future (i.e. fill this up!)
			return NoMellowBidPlaySituation.handleNormalThrow(dataModel);
		}
	}
	
	//TODO: play high if you got your tricks (HARD!)
	//TODO: consider playing non-top card of suit... LATER
	//TODO: put in another class because this handles the logic for both seated left and seated right...
	public static String throwOffHighCardThatMightAccidentallySaveMellow(DataModel dataModel, int mellowPlayerIndex) {
		
		double bestValue = 0.0;
		String bestCard = null;
		
		for(int curSuitIndex=0; curSuitIndex<Constants.NUM_SUITS; curSuitIndex++) {

			if(curSuitIndex == Constants.SPADE
			|| dataModel.isVoid(Constants.CURRENT_AGENT_INDEX, curSuitIndex)) {
				continue;
			}
			
			double curValue = 0.0;
			String curCard = dataModel.getCardCurrentPlayerGetHighestInSuit(curSuitIndex);
			
			
			if(dataModel.isVoid(mellowPlayerIndex, curSuitIndex)) {
				curValue -= 100.0;

			} else if(dataModel.signalHandler.mellowPlayerSignalNoCardsOfSuit(mellowPlayerIndex, curSuitIndex)) {
				curValue -= 50.0;
				
			} else if(dataModel.signalHandler.mellowSignalledNoCardUnderCardSameSuitExceptRank2(curCard, mellowPlayerIndex)) {
				curValue -= 48.0;

			} else if(dataModel.isMasterCard(curCard) && wantTrick(dataModel)) {
				//TODO: revisit...
				/*
				//Try not to part with a master card if you want a trick: (It's rough and untested)
				if(dataModel.getNumberOfCardsOneSuit(curSuitIndex) > 1) {

					String secondCard = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(curSuitIndex);
					
					boolean useSecondCard = false;
					
					//TODO: make an algo for it?
					for(int rank=dataModel.getRankIndex(secondCard) + 1; rank<dataModel.getRankIndex(curCard); rank++) {
						if(dataModel.isCardPlayedInRound(dataModel.getCardString(rank, curSuitIndex))) {
							useSecondCard = true;
							break;
						}
					}
					//END TODO
					
					if(useSecondCard) {
						curCard = secondCard;
					} else {
						//Do nothing...
					}

					//END: Try not to part with a master card if you want a trick:
				} else {
					curValue -= 20.0;
				}
				*/
			}
			
			//Shouldn't like to throw off a high-card
			if(dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(curCard) < 3) {
				curValue = -4 * (1.5) + 1.5 * dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(curCard);
			}
			
			//Lower rank cards are less fun to throw:
			curValue += 0.9 * dataModel.getRankIndex(curCard);
			
			//2s and 3s don't really save a mellow:
			if(dataModel.getRankIndex(curCard) <= DataModel.RANK_THREE) {
				curValue -= 10.0;
			}
			
			
			//Count Amount of cards over top mellow signal...
			//The more cards over, the less inclined you should be about throwing off...
			if(dataModel.signalHandler.mellowPlayerSignalNoCardsOfSuit(mellowPlayerIndex, curSuitIndex) == false) {
				
				String topMellowSignalCard = dataModel.signalHandler.getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(mellowPlayerIndex, curSuitIndex);
				
				int numCardsOver = dataModel.getNumCardsInCurrentPlayersHandOverCardSameSuit(topMellowSignalCard);
				
				if(numCardsOver > 1) {
					curValue -= 5.0 * (numCardsOver - 1);
				}
			}
			//End count amount of cards over top mellow signal
			
			
			if(bestCard == null
					|| curValue > bestValue) {
				bestCard = curCard;
				bestValue = curValue;
			}
			
		}
		//return dataModel.getHighestOffSuitCardAnySuitButSpade();
		
		return bestCard;
	}
	
	//TODO: make it more precise, and then actually use it...
	//Oversimplified...
	public static boolean wantTrick(DataModel dataModel) {
		
		if(dataModel.getNumCardsInCurrentPlayerHand() < 6) {
			//Calculation below is so rough, that I'm only going to allow the logic if there's less than 6 cards in hand... 
			return false;
		}
		int numBid = dataModel.getBid(Constants.CURRENT_AGENT_INDEX);
		int numTricks = dataModel.getTrick(Constants.CURRENT_AGENT_INDEX);
		

		int numBidPartner = dataModel.getBid(Constants.CURRENT_PARTNER_INDEX);
		int numTricksPartner = dataModel.getTrick(Constants.CURRENT_PARTNER_INDEX);
		
		int partnerCover = Math.max(numTricksPartner - 1, 0);
		
		if(numTricks + partnerCover < numBid) {
			
			int numMasterExpectedTrick = 0;
			
			for(int s=0; s<Constants.NUM_SUITS; s++) {
				if(dataModel.currentPlayerHasMasterInSuit(s)) {
					numMasterExpectedTrick++;
				}
			}
			
			//TODO: what about kings, and what about partial trams...
			//This is really rough...
			
			int numSpadesInHand = dataModel.getNumCardsOfSuitInCurrentPlayerHand(Constants.SPADE);
			int numSpadesInOtherHand = dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE);
			
			int otherPlayersHandFactor = 0;
			for(int playerIndex=0; playerIndex<Constants.NUM_PLAYERS; playerIndex++) {
				if(playerIndex == Constants.CURRENT_AGENT_INDEX) {
					continue;
				} else if(dataModel.isVoid(playerIndex, Constants.SPADE)) {
					otherPlayersHandFactor++;
				}
			}
			
			int spadesTricksExpected = numSpadesInHand - otherPlayersHandFactor * numSpadesInOtherHand;
			
			int roughTricksExpected = numMasterExpectedTrick + spadesTricksExpected;
			
			if(numTricks + partnerCover + roughTricksExpected > numBid) {
				return false;
			} else {
				return true;
			}
			
		} else {
			return false;
		}
		
		
	}
	
	
	//TODO: make into dataModel functions?
	//pre: card is in hand
	public static String getHighestPartOfGroup(DataModel dataModel, String card) {
		
		String ret= card;
		int suitIndex = CardStringFunctions.getIndexOfSuit(card);
		int initRank = DataModel.getRankIndex(card);
		
		for(int rank = initRank; rank<=DataModel.ACE; rank++) {
			
			String tmpCard = DataModel.getCardString(rank, suitIndex);
			
			if(dataModel.isCardPlayedInRound(tmpCard)) {
				continue;
			} else if(dataModel.hasCard(tmpCard)) {
				ret = tmpCard;
			} else {
				break;
			}
		}
		
		return ret;
		
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
	
			if(dataModel.signalHandler.mellowSignalledNoCardOverCardSameSuit(tempLowest, MELLOW_PLAYER_INDEX) == false) {
				

				//TODO: FROM DEBUG TESTCASE:
				// IF PARTNER AND PROTECTOR VOID:
				//Figure out if you could get the mellow...
				//If not, figure out if you could trick proctector into trumping
					//If you can't figure out if you could trick protector into trumping and you can't get the mellow:
						//play high and encourage your partner to trump
				
				//AAH!
				//You know what? This is Pandora's box... I'm not going to touch this yet.
				
				
				//TODO: FIRST STEP: Maybe value playing a suit mellow protector is void in and getting the protector to trump on the 2nd throw
				// or lose an easy trick?
				//END TODO....
				
				// AND PROTECTOR VOID IN SPADE:
				//...
				
				
				//TODO: instead of just returning, try grading the options!
				//Also playing always lowest isn't smart. Sometimes playing 2nd or 3rd lowest is smarter
				//(Save 2C for the end)
				
				int curLowestRankSuitScore = DataModel.getRankIndex(tempLowest);
				
				// pretend lowest spades have a higher rank to discourage use of spades:
				if(suit == Constants.SPADE) {
					curLowestRankSuitScore += 9.5;
				}
				
				//Don't want to lead low if you have master and are left of mellow.
				if(dataModel.currentPlayerHasMasterInSuit(suit)
						&& suit != Constants.SPADE) {
					curLowestRankSuitScore += 3.0;
				}
				
				//Encourage mellow protector to trump...
				//TODO: This isn't good if mellow is void in some offsuit and we don't want the protector to lead it
				if(dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, suit)
					 && suit != Constants.SPADE) {
						 curLowestRankSuitScore -= 3.0;
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

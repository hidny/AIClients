package mellow.ai.situationHandlers;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.cardDataModels.handIndicators.NonMellowBidHandIndicators;
import mellow.cardUtils.CardStringFunctions;
import mellow.cardUtils.DebugFunctions;

public class SeatedLeftOfOpponentMellow {

	

	public static int MELLOW_PLAYER_INDEX = 3;
	public static int PROTECTOR_PLAYER_INDEX = 1;
	
	//TODO
	public static String playMoveSeatedLeftOfOpponentMellow(DataModel dataModel) {
		

		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "AC AD KD 2D ")) {
			System.out.println("Debug");
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
				dataModel.isPrevThrowWinningFight()) {
			

			if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "QS TS 9S 2S KH 5H KC JC 9C 3C 2C 9D 8D ")) {
				System.out.println("Debug");
			}
			
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
						return throwOffHighCardThatMightAccidentallySaveMellowAndTryToAvoidThrowingMasters(dataModel, MELLOW_PLAYER_INDEX);
					}
				}
			} else {
				//Mellow player winning and is following suit:
				
				if(dataModel.throwerMustFollowSuit()) {
					//Both follow suit
					
					if(dataModel.couldPlayCardInHandUnderCardInSameSuit(curWinningCard) 
							&& mellowHasOver1PercChanceOfLose(dataModel)) {
						
						if(throwIndex < 3) {
							return dataModel.getCardInHandClosestUnderSameSuit(curWinningCard);
						} else {
							//If you are the last thrower, and mellow is burning: play low
							return dataModel.getCardCurrentPlayerGetLowestInSuit(dataModel.getSuitOfLeaderThrow());
						}
					
					} else {
						
						if(throwIndex == 1 
								//&& CardStringFunctions.getIndexOfSuit(curWinningCard) == Constants.SPADE
								&& dataModel.currentPlayerHasMasterInSuit(dataModel.getSuitOfLeaderThrow()) == false
								&& dataModel.getNumberOfCardsOneSuit(dataModel.getSuitOfLeaderThrow()) >= 3) {
							
							
							//If responding to the mellow lead, don't waste a high one...
							//Maybe I'll have more cases later...
							return dataModel.getCardCurrentPlayerGetSecondHighestInSuit(dataModel.getSuitOfLeaderThrow());
						} else {
							return dataModel.getCardCurrentPlayerGetHighestInSuit(dataModel.getSuitOfLeaderThrow());
						}
					}
					
				} else {
					
					
					//If can't follow suit:
					if(dataModel.currentPlayerOnlyHasSpade()) {
						//play biggest spade if have no choice:
						//over simplified, but whatever...
						return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
					} else {
						//play big off suit to mess-up mellow play (Over-simplified, but whatever)
						return throwOffHighCardThatMightAccidentallySaveMellowAndTryToAvoidThrowingMasters(dataModel, MELLOW_PLAYER_INDEX);
					}
				
				}
			}

			//End go under mellow if possible logic
			
			
		} else if(throwIndex > 0 && 
				dataModel.isPrevThrowWinningFight() == false) {

			//handle case where mellow is already safe:
			

			//TODO: the logic is completely messed up!
		
			if(dataModel.throwerMustFollowSuit()) {
				
				String curWinningCard =  dataModel.getCurrentFightWinningCardBeforeAIPlays();
				int leadSuitIndex = dataModel.getSuitOfLeaderThrow();
				String highestCardOfSuit = dataModel.getCardCurrentPlayerGetHighestInSuit(leadSuitIndex);
				
				if(dataModel.isVoid(MELLOW_PLAYER_INDEX, dataModel.getSuitOfLeaderThrow())
						|| dataModel.signalHandler.mellowBidderPlayerSignalNoCardsOfSuit
									(MELLOW_PLAYER_INDEX, leadSuitIndex)) {
					
					//lazy approx:
					return getHighestPartOfGroup(dataModel, NoMellowPlaySituation.handleNormalThrow(dataModel));
					
				} else if(dataModel.cardAGreaterThanCardBGivenLeadCard
							                 (highestCardOfSuit,
							                		 curWinningCard)
						   && dataModel.currentPlayerHasMasterInSuit(dataModel.getSuitOfLeaderThrow())
						
						 //Don't play over partner...
						   //TODO: maybe have strategies for this case?
						   && (dataModel.getIndexOfCurrentlyWinningPlayerBeforeAIPlays() != Constants.CURRENT_PARTNER_INDEX
								|| dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit(curWinningCard, highestCardOfSuit) > 0)
						   ){
							
							
						return dataModel.getCardCurrentPlayerGetHighestInSuit(dataModel.getSuitOfLeaderThrow());
						
				} else {

					//TODO: PUT INTO FUNCTION -- 3242
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
						
						//This might be null if all cards damage future mellow burning odds...
						String minCardToPlayWithNoDamageToFutureMellowBurnOdds = getMinCardToPlayWithNoDamageToFutureMellowBurnOddsForSuit(dataModel, dataModel.getSuitOfLeaderThrow());
						
						
						
						if(fourthThrowMinCardToWin != null && minCardToPlayWithNoDamageToFutureMellowBurnOdds != null) {

							if(  dataModel.cardAGreaterThanCardBGivenLeadCard(fourthThrowMinCardToWin, minCardToPlayWithNoDamageToFutureMellowBurnOdds)
								&& ! dataModel.getCardSecondThrow().equals(currentFightWinner)) {
								
								return getHighestPartOfGroup(dataModel, fourthThrowMinCardToWin);
								
							} else {
								String tempCardToRet = getHighestPartOfGroup(dataModel,
										minCardToPlayWithNoDamageToFutureMellowBurnOdds);
								
								String tempTestCard = getHighestPartOfGroup(dataModel,
										fourthThrowMinCardToWin);
								
								if( ! minCardToPlayWithNoDamageToFutureMellowBurnOdds.equals(fourthThrowMinCardToWin)
										&& tempCardToRet.equals(tempTestCard)) {
									System.out.println("DEBUG: there's a fork in the road here.");
									System.out.println("DEBUG: Do you take your partner's trick, or let win have it");
								}

								return tempCardToRet;
							}

						} else if(fourthThrowMinCardToWin != null && minCardToPlayWithNoDamageToFutureMellowBurnOdds == null) { 
							return getHighestPartOfGroup(dataModel, fourthThrowMinCardToWin);
							

						} else if(fourthThrowMinCardToWin == null && minCardToPlayWithNoDamageToFutureMellowBurnOdds != null) {
							
							String topCardInHand = getHighestPartOfGroup(dataModel, 
									minCardToPlayWithNoDamageToFutureMellowBurnOdds);

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

						//END TODO: PUT INTO FUNCTION -- 3242
				}
				
			} else if(dataModel.currentAgentHasSuit(Constants.SPADE)) {
				
				String maxMellowSpade =dataModel.signalHandler.getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(MELLOW_PLAYER_INDEX, Constants.SPADE);
				
				if( maxMellowSpade == null) {
					
					return dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
					
				} else if(DataModel.getRankIndex(maxMellowSpade) < DataModel.QUEEN
						&& dataModel.couldPlayCardInHandOverCardInSameSuit(maxMellowSpade)) {
				
					return dataModel.getCardInHandClosestOverSameSuit(maxMellowSpade);
					
				} else {

					return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
					
				}

			} else {
				
				return throwOffHighCardThatMightAccidentallySaveMellowAndTryToAvoidThrowingMasters(dataModel, MELLOW_PLAYER_INDEX);
			}
		
			
			//end handle case where mellow is already safe:
			
		} else {
		
		
			//TODO: don't be lazy in future (i.e. fill this up!)
			return NoMellowPlaySituation.handleNormalThrow(dataModel);
		}
	}
	
	
	//return null if there is no safe card...
	public static String getMinCardToPlayWithNoDamageToFutureMellowBurnOddsForSuit(DataModel dataModel, int suitIndex) {


		//String minCardOverMaxMellowCard = null;
		String maxMellowCard = dataModel.signalHandler.getMaxRankCardMellowPlayerCouldHaveBasedOnSignals
				(MELLOW_PLAYER_INDEX, suitIndex);
		
		if(maxMellowCard == null) {
			return dataModel.getCardCurrentPlayerGetLowestInSuit(suitIndex);
		}
		
		String minCardOverMaxMellowCard = null;
		if(dataModel.couldPlayCardInHandOverCardInSameSuit(maxMellowCard)) {
			minCardOverMaxMellowCard = dataModel.getCardInHandClosestOverSameSuit(maxMellowCard);
		}
		
		String ret = minCardOverMaxMellowCard;
		
		int numCardsAboveCurrentRankCovered = 0;
		
		for(int rank=DataModel.RANK_TWO; rank<=DataModel.getRankIndex(maxMellowCard); rank++) {
			
			//Adjust numbers based on w
			String curCard = DataModel.getCardString(rank, suitIndex);
			if(dataModel.hasCard(curCard)) {
				
				numCardsAboveCurrentRankCovered++;
				
			} else if( ! dataModel.isCardPlayedInRound(curCard)) {
				
				numCardsAboveCurrentRankCovered--;
				
				if(numCardsAboveCurrentRankCovered < 0) {
					numCardsAboveCurrentRankCovered = 0;
				}
			} else {
				//Do nothing
			}
			
			if(rank + numCardsAboveCurrentRankCovered == DataModel.getRankIndex(maxMellowCard)) {

				String tmpCard = dataModel.getCardInHandClosestOverSameSuit(curCard);
				
				if(tmpCard != null) {
					ret = PartnerSaidMellowSituation.getLowestCardOfGroupOfCardsOverAllSameNumCardsInOtherPlayersHandOfSuit(dataModel,
							tmpCard);
				}
				
				break;
			
			} else if(rank + numCardsAboveCurrentRankCovered > DataModel.getRankIndex(maxMellowCard)) {
				
					ret=PartnerSaidMellowSituation.getLowestCardOfGroupOfCardsOverAllSameNumCardsInOtherPlayersHandOfSuit(
								dataModel,
								curCard);
				break;
			}
				
		}
		
		return ret;
	}
	//OLD TODOS:

	//TODO: FUNCTION 4
	//Make it consider if the tricks were made and/or if protector could be burnt...
	//And maybe throw off suit with less than 4 cards
	//END TODO
	
	//TODO: figure out how to play before a mellow (this is a hard position...)
	//Knowing when to trump is complicated...
	//END OLD TODOS
	
	//TODO: play high if you got your tricks (HARD!)
	//TODO: consider playing non-top card of suit... LATER
	//TODO: put in another class because this handles the logic for both seated left and seated right...
	
	//TODO: being seated left of mellow compareed to being seated right of mellow should make you more comfortable
	//about keeping high cards I think...
		// It probably allows you to have a higher ratio of high-cards to low cards
	public static String throwOffHighCardThatMightAccidentallySaveMellowAndTryToAvoidThrowingMasters(DataModel dataModel, int mellowPlayerIndex) {
		
		double bestValue = 0.0;
		String bestCard = null;
		

		
		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "KS 8S 7S 7C 3C AD QD JD 5D 3D ")) {
			System.out.println("Debug");
		}
		
		for(int curSuitIndex=0; curSuitIndex<Constants.NUM_SUITS; curSuitIndex++) {

			if(curSuitIndex == Constants.SPADE
			|| dataModel.isVoid(Constants.CURRENT_AGENT_INDEX, curSuitIndex)) {
				continue;
			}
			
			double curValue = 0.0;
			//String curCard = dataModel.getCardCurrentPlayerGetHighestInSuit(curSuitIndex);
			String curCard = getMinCardToPlayWithNoDamageToFutureMellowBurnOddsForSuit(dataModel, curSuitIndex);
			
			if(curCard == null) {
				curCard = dataModel.getCardCurrentPlayerGetHighestInSuit(curSuitIndex);
			}
			
			if(dataModel.isVoid(mellowPlayerIndex, curSuitIndex)) {
				curValue -= 100.0;

			} else if(dataModel.signalHandler.mellowBidderPlayerSignalNoCardsOfSuit(mellowPlayerIndex, curSuitIndex)) {
				curValue -= 50.0;
				
			} else if(dataModel.signalHandler.mellowBidderSignalledNoCardUnderCardSameSuitExceptRank2(curCard, mellowPlayerIndex)) {
				curValue -= 48.0;

			} else if(dataModel.isMasterCard(curCard)) {
				//TODO: Maybe don't do this if you really don't want tricks...
				if(dataModel.getNumCardsOfSuitInCurrentPlayerHand(curSuitIndex) >= 4) {
					curCard = dataModel.getCardCurrentPlayergetThirdLowestInSuit(curSuitIndex);
					
					if(dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(curCard) ==0) {

						curValue -=25.0;
					}
				} else {
					curValue -=30.0;
				}
				
			} else if(NonMellowBidHandIndicators.hasKEquiv(dataModel, curSuitIndex)
					&& ! NonMellowBidHandIndicators.hasKQEquivAndNoAEquiv(dataModel, curSuitIndex)) {
				
				//Also save your king equiv...
				//TODO: Maybe don't do this if you really don't want tricks...
				if(dataModel.getNumCardsOfSuitInCurrentPlayerHand(curSuitIndex) >= 4) {
					curCard = dataModel.getCardCurrentPlayergetThirdLowestInSuit(curSuitIndex);
				}

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
			if(dataModel.signalHandler.mellowBidderPlayerSignalNoCardsOfSuit(mellowPlayerIndex, curSuitIndex) == false) {
				
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
		//getMinCardToPlayWithNoDamageToFutureMellowBurnOddsForSuit
		
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
		int numTricks = dataModel.getNumTricks(Constants.CURRENT_AGENT_INDEX);
		

		int numBidPartner = dataModel.getBid(Constants.CURRENT_PARTNER_INDEX);
		int numTricksPartner = dataModel.getNumTricks(Constants.CURRENT_PARTNER_INDEX);
		
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
	
			if(dataModel.signalHandler.mellowBidderSignalledNoCardOverCardSameSuit(tempLowest, MELLOW_PLAYER_INDEX) == false) {
				

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
					
					int numOver = dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(dataModel.signalHandler.getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(MELLOW_PLAYER_INDEX, Constants.SPADE));
					
					if(numOver >= 0) {
						//This is over-simplified, but whatever
						curLowestRankSuitScore += 50.0;
					}
					
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
	
	//TODO: look into replacing this with: getHighestCardYouCouldLeadWithoutSavingMellowInSuit()
	//But be careful!
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
	
	
	// Copied logic from SingleActiveMellowPlayer
	// "Special case where mellow plays over lead"
	
	public static boolean mellowHasOver1PercChanceOfLose(DataModel dataModel) {
		int throwIndex = dataModel.getCardsPlayedThisRound() % Constants.NUM_PLAYERS;
		int leaderSuitIndex = dataModel.getSuitOfLeaderThrow();
		
		if(dataModel.isPrevThrowWinningFight() == false) {
			return false;
		}
		
		String currentFightWinner = dataModel.getCurrentFightWinningCardBeforeAIPlays();
		
		//Mellow is leading:
		if(throwIndex == 1
				&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(leaderSuitIndex) >= 6
				&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(
						currentFightWinner)
				     < 2
				
				&& ! dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, leaderSuitIndex)
				) {
			return false;

		//Mellow is second throw:
		} else if(throwIndex == 2
				&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(leaderSuitIndex) >= 5
				&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(
						currentFightWinner)
				     < 1
				&& ! dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, leaderSuitIndex)) {
			return false;

		} else {
			return true;
		}
		
	}
}

package mellow.ai.situationHandlers;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.simulation.MonteCarloMain;
import mellow.cardUtils.CardStringFunctions;
import mellow.cardUtils.DebugFunctions;

public class NoMellowBidPlaySituation {

	public static String handleNormalThrow(DataModel dataModel) {

		int throwIndex = dataModel.getCardsPlayedThisRound() % Constants.NUM_PLAYERS;
		
		//leader:
		String cardToPlay = null;
		System.out.println("**Inside get card to play");
		if(throwIndex == 0) {
			cardToPlay = AILeaderThrow(dataModel);
			
		//second play low
		} else if(throwIndex == 1) {
			cardToPlay = AISecondThrow(dataModel);
		//TODO
		//third plays high.
		} else if(throwIndex == 2) {
			cardToPlay = AIThirdThrow(dataModel);
		//TODO:
		//last barely makes the trick or plays low.
		} else {
			cardToPlay = AIFourthThrow(dataModel);
		}
		
		if(cardToPlay != null) {
			System.out.println("AI decided on " + cardToPlay);
		}
		
		return cardToPlay;
	}
	
	
	public static String AILeaderThrow(DataModel dataModel) {

		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "8C 6D 3D")) {
			System.out.println("DEBUG");
		}

		if(couldTRAM(dataModel)) {
			System.out.println("THE REST ARE MINE! (TRAM)");
			if(dataModel.currentAgentHasSuit(Constants.SPADE)) {
				return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
			} else {
				return dataModel.getMasterCard();
			}
		}
		
		String bestCardToPlay = null;
		double currentBestScore = -1000000.0;
		
		
		for(int suitIndex = 0; suitIndex< Constants.NUM_SUITS; suitIndex++) {
			
			int numCardsOfSuitInHand = dataModel.getNumCardsOfSuitInCurrentPlayerHand(suitIndex);
			
			//Can't play a suit you don't have:
			if(numCardsOfSuitInHand == 0) {
				continue;
			}
			
			int numCardsOfSuitOtherPlayersHave = Constants.NUM_RANKS
					- dataModel.getNumCardsPlayedForSuit(suitIndex) 
					- dataModel.getNumCardsOfSuitInCurrentPlayerHand(suitIndex);
			
			

		
			String cardToPlay = null;
			
			double curScore = 0.0;
			
			boolean partnerSignalledHighCardOfSuit = false;
			

			if(dataModel.currentPlayerHasMasterInSuit(suitIndex)) {
				//Made up a number to say having the master card to lead in partner's void suit is cool:
				//TODO: refine later
				curScore += 10.0;
				
				
				//if(dataModel.getRankIndex(dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex)) == dataModel.KING) {

				//	curScore += 10.0;
				//}
				/* else if (dataModel.getRankIndex(dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex)) == dataModel.QUEEN) {

					curScore += 15.0;
				}*/

				//Treat partner's possible master as your own...
			} else if(dataModel.signalHandler.playerSignalledHighCardInSuit(Constants.CURRENT_PARTNER_INDEX, suitIndex)) {
				curScore += 9.0;
				partnerSignalledHighCardOfSuit = true;
				
				//Don't want to setup our partner for failure:
				if(numCardsOfSuitOtherPlayersHave - 1 == 0 && 
						dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
		                  (Constants.CURRENT_PARTNER_INDEX, suitIndex) == false) {
					curScore -= 100.0;
				}
						
			}
			
			//Leading in a suit where RHS prob has master could be a good idea sometimes...
			//Like when partner is trumping
			//or partner has no spade.
			if(      ! dataModel.currentPlayerHasMasterInSuit(suitIndex)
					&& dataModel.signalHandler.playerSignalledHighCardInSuit(Constants.RIGHT_PLAYER_INDEX, suitIndex)
					&& ! hasKQEquiv(dataModel, suitIndex)) {
				curScore -= 20.0;
			}
			
			if(suitIndex != Constants.SPADE) {

				
				//Check if leading suitIndex helps partner trump:
				if(
						(       (dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, suitIndex)
								|| dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
								                  (Constants.CURRENT_PARTNER_INDEX, suitIndex))
							&& dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE) == false
						)
					&& (dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
						(Constants.RIGHT_PLAYER_INDEX, suitIndex) == false
							|| dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)
						)
						
					&& numCardsOfSuitOtherPlayersHave >= 1) {
				
						if(numCardsOfSuitOtherPlayersHave >= 2
									
								//If player on left is also void, that's great!
								|| (numCardsOfSuitOtherPlayersHave >= 1
									&& dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, suitIndex))
								) {
	
							curScore += 100;
	
						} else {
							
							curScore += 70;

						}
						
						if(dataModel.currentPlayerHasMasterInSuit(suitIndex)) {
							
							cardToPlay = dataModel.getMasterInHandOfSuit(suitIndex);
							
						} else if(hasKQEquiv(dataModel, suitIndex)) {
							//It's slightly controversial to not play lowest when partner is trumping, but I like the agro play...
							cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex);
						
						} else {
							cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(suitIndex);
						}
				} else if(numCardsOfSuitOtherPlayersHave == 0) {
					//Might want to do this if right is out of spades...
					
					//Note this is a master card because other players don't have any cards to challenge it:
					cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex);
					
					if(dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE) == 0) {
						
						//Jackpot!
						curScore += 1000;
					} else if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
							(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)
							&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
							(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)) {
						
						//Pretty good. You're making this trick, but partner might trump
						curScore +=300;
						
					} else if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
							(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)
							
						&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
						(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE) == false) {
						
						curScore += 50;
					} else {
						
						curScore -= 50;
					}
					
						
				} else {
					if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
							(Constants.LEFT_PLAYER_INDEX, suitIndex)

							&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
								(Constants.CURRENT_PARTNER_INDEX, suitIndex) == false

							&& numCardsOfSuitOtherPlayersHave > 3) {
								curScore -= 5.0 * (numCardsOfSuitOtherPlayersHave);
					}
					
					
					
					if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
						(Constants.RIGHT_PLAYER_INDEX, suitIndex) == false
						|| dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)
						) {
						//increased to 26.0 to fix testcase Michael2021 -> 290...
						curScore += 26.0;
						
					} else {
						curScore -= 10.0;
					}
					
					if( dataModel.currentPlayerHasMasterInSuit(suitIndex)) {
						//Don't lower it...

					} else if(partnerSignalledHighCardOfSuit) {
						//Don't lower it again

					} else if(hasKQEquiv(dataModel, suitIndex)) {
						//Only lower it a little...
						curScore -= 5.0;
					
					} else {
						curScore -= 26.0;
						
						
					}
					
					//Play the ace so you back it up with a king:
					if(numCardsOfSuitInHand > 1
						&& dataModel.getRankIndex(dataModel.getCardCurrentPlayerGetSecondHighestInSuit(suitIndex)) == dataModel.KING) {
						curScore += 5.0;
						
					}
					//Don't play suit with King if you don't have the queen
					if(dataModel.getRankIndex(dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex)) == dataModel.KING
							&& numCardsOfSuitInHand > 1
							&& dataModel.getRankIndex(dataModel.getCardCurrentPlayerGetSecondHighestInSuit(suitIndex)) < dataModel.QUEEN
							&& dataModel.isMasterCard(dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex)) == false) {
						
						curScore -= 50.0;
						
					}
					
					//Play the king if it's alone
					if(dataModel.getRankIndex(dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex)) == dataModel.KING
							&& numCardsOfSuitInHand == 1
							&& dataModel.isMasterCard(dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex)) == false) {
						
						curScore += 50.0;
						
					}
					
					//Consider becoming void if you don't have too many spades (TODO: be more subtle)
					if(3.0 * dataModel.getNumCardsOfSuitInCurrentPlayerHand(Constants.SPADE) - numCardsOfSuitOtherPlayersHave  <= 1
							&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
							(Constants.RIGHT_PLAYER_INDEX, suitIndex)) {
						
						//Only consider becoming void if it's feasible:
						int numCardsLessThanAvgOther = numCardsOfSuitOtherPlayersHave - 3 * dataModel.getNumCardsOfSuitInCurrentPlayerHand(suitIndex);
						
						if(numCardsLessThanAvgOther > 0) {
							curScore += Math.min(5.0 * numCardsLessThanAvgOther, 30);
						}
					}
					
					
					if(dataModel.currentPlayerHasMasterInSuit(suitIndex)) {
						
						cardToPlay = dataModel.getMasterInHandOfSuit(suitIndex);
					} else if(hasKQEquiv(dataModel, suitIndex)) {
						cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex);
						
					} else if(hasKEquiv(dataModel, suitIndex)) {
						cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(suitIndex);
					
					//TODO: special consideration for QEquiv??
						
					} else {
						cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex);
					}
					
					
					//I like leading offsuit queens... if no one is void and I don't have a hope of winning with it.
					if(dataModel.getRankIndex(dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex)) == DataModel.QUEEN
							&& 
								! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
							(Constants.LEFT_PLAYER_INDEX, suitIndex)
							&& 
								! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, suitIndex)
						) {
						
						if(dataModel.getNumCardsCurrentUserStartedWithInSuit(suitIndex) < 3
								|| (dataModel.getNumCardsCurrentUserStartedWithInSuit(Constants.SPADE) < 5 
										&& (dataModel.getNumCardsCurrentUserStartedWithInSuit(suitIndex) > 4
												|| dataModel.hasCard(DataModel.getCardString(DataModel.JACK, suitIndex))))
								) {
							curScore += 10.0;
							
							cardToPlay = DataModel.getCardString(DataModel.QUEEN, suitIndex);

							if(dataModel.hasCard( DataModel.getCardString(DataModel.JACK, suitIndex))) {
								curScore += 5.0;
							}
						}
						
					}
					
					if(partnerSignalledHighCardOfSuit) {
						
						//TODO: this might need more thought...
						cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(suitIndex);
					}
				}
			} else {
				//TODO: consider leading spade here...
				
				if(dataModel.currentPlayerHasMasterInSuit(suitIndex)) {
					
					cardToPlay = dataModel.getMasterInHandOfSuit(suitIndex);
					
					if(numCardsOfSuitInHand >= 2) {
						String cardTmp = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(suitIndex);
						
						//If you have the AK equiv, don't be afraid to play A to see what's going on.
						if(dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(cardTmp)
								== dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(cardToPlay)) {
							curScore += 30.0;
						}
							
					}
					
				} else {
					curScore += 5.0;
					cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(suitIndex);
					
				}

				
				
				
				
				/*
				 * int numCardsOfSuitOtherPlayersHave = Constants.NUM_RANKS
					- dataModel.getNumCardsPlayedForSuit(suitIndex) 
					- dataModel.getNumCardsOfSuitInCurrentPlayerHand(suitIndex);
			
			int numCardsOfSuitInHand = dataModel.getNumCardsOfSuitInCurrentPlayerHand(suitIndex);
			
				 */
				

				//TODO: this is a rough rule of thumb...
				//Want to play spade if partner is void, but not when opponents are void:
				if(dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)) {
					curScore += 40.0;
				}
				//Don't want to play spade when opponents are void: 
				if(dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
						&& dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)) {
					curScore -= 40.0;
				
					//If 1/2 opponents void, be less tempted to play spade:
				} else if(dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
						|| dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)) {
					curScore -= 10.0;
				}
				
				if(dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
						&& dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)
						&& ! dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)) {
					//Maybe there's a case for draining your partner's spade when the opponents don't have spade...
					// but I'd like to see it to believe it.
					//It happened in the orig testcases... shoot.
					curScore -=100.0;
				}
				
				
				
				if(numCardsOfSuitOtherPlayersHave == 0) {
					//TODO: this fixes testcase 220 of Michael2021, but
					// maybe it's not always best
					// I do think it's better than leading the offsuit masters though
					if(dataModel.currentPlayerHasMasterInSuit(1)
							|| dataModel.currentPlayerHasMasterInSuit(2)
							|| dataModel.currentPlayerHasMasterInSuit(3)) {
						curScore += 70.0;
					}
				}
				
				//Don't lead small spade if you don't have much...
				if(3 * numCardsOfSuitInHand < numCardsOfSuitOtherPlayersHave) {
					curScore -= 20.0;
					
					
					
				} else {
					double diff = numCardsOfSuitInHand - (1.0 *numCardsOfSuitOtherPlayersHave)/3.0;
					
					//Just play spade if you have them...
					if(diff >= 2.3) {
						curScore += 20.0;
						if(diff >= 3.4) {
							curScore += 10.0;
							
							if(diff > 4.5) {
								curScore += 5.0;
							}
						}
					}
				}
				

				//Don't volunteer to play spade if you 1 less than master in spade
				//and have few spades
				if(hasKEquiv(dataModel, Constants.SPADE)
						&& !hasKQEquiv(dataModel, Constants.SPADE)
						&& 3 * (numCardsOfSuitInHand-1) < numCardsOfSuitOtherPlayersHave) {
					curScore -= 20.0;
				}
				
				//Play spade in the hopes of making your offsuit masters relevant:
				if((dataModel.currentPlayerHasMasterInSuit(1) && dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(1) < 6)
						|| (dataModel.currentPlayerHasMasterInSuit(2) && dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(2) < 6)
						|| (dataModel.currentPlayerHasMasterInSuit(3) && dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(3) < 6)) {
					curScore += 30.0;
				} else {
					
					//Check if you have AK combo because you might want to drain S if you have that.
					for(int suitTemp = 1; suitTemp<Constants.NUM_SUITS; suitTemp++) {
						if(dataModel.hasCard(dataModel.getCardString(DataModel.ACE, suitTemp))
								&& dataModel.hasCard(dataModel.getCardString(DataModel.KING, suitTemp))) {
							curScore += 30.0;
							System.out.println("BOOM");
							break;
						}
					}
				}
				
				
				//Drain the spades... TODO: this is rough... and doesn't always work out...
				if(dataModel.getNumCardsCurrentUserStartedWithInSuit(Constants.SPADE) >= 5
						&& (dataModel.currentPlayerHasMasterInSuit(1)
							|| dataModel.currentPlayerHasMasterInSuit(2)
							|| dataModel.currentPlayerHasMasterInSuit(3))
						&& numCardsOfSuitOtherPlayersHave > 0) {
					curScore += 30.0;
					
					//Check if we have aggressive spade lead options:
					if(hasKQEquiv(dataModel, Constants.SPADE)) {
						
						cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex);
						curScore += 25.0;
						
					}
				}
				
				
				
				
				//Basic awareness of when to play S based on bids:
				if(dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) >= 5
						&& dataModel.getBid(Constants.CURRENT_PARTNER_INDEX)
						- dataModel.getTrick(Constants.CURRENT_PARTNER_INDEX) >= 2
						&& ! dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)) {
					curScore += 20.0;
					curScore += 5.0 * (dataModel.getBid(Constants.CURRENT_PARTNER_INDEX)
							- dataModel.getTrick(Constants.CURRENT_PARTNER_INDEX) - 2);
				}
				
				if(dataModel.getBid(Constants.RIGHT_PLAYER_INDEX) >= 5
						&& dataModel.getBid(Constants.RIGHT_PLAYER_INDEX)
						- dataModel.getTrick(Constants.RIGHT_PLAYER_INDEX) >= 2
					    && ! dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)) {
					curScore -= 40.0;
				}
				

				if(dataModel.getBid(Constants.LEFT_PLAYER_INDEX) >= 5
						&& dataModel.getBid(Constants.LEFT_PLAYER_INDEX)
						- dataModel.getTrick(Constants.LEFT_PLAYER_INDEX) >= 2
					    && ! dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)) {
					curScore -= 40.0;
				}
				
				int bidDiff = (dataModel.getBid(Constants.CURRENT_PLAYER_INDEX) + dataModel.getBid(Constants.CURRENT_PARTNER_INDEX))
				- (dataModel.getBid(Constants.LEFT_PLAYER_INDEX) + dataModel.getBid(Constants.RIGHT_PLAYER_INDEX));
				//Rough way to look at the bids to decide whether or not to play spade
				
				if(bidDiff > 0) {
					//Limit how much of a benefit this is because it's covered elsewheres:
					curScore += 5.0 * Math.min(4, bidDiff);
				} else {
					curScore += 5.0 * bidDiff;
				}
				
				//Check if we're wasting spades we can trump with:
				if(numCardsOfSuitInHand <= 2 && 
						dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE) > 1) {
					for(int suitTemp = 1; suitTemp<Constants.NUM_SUITS; suitTemp++) {
						if(dataModel.isVoid(Constants.CURRENT_AGENT_INDEX, suitTemp)
								&& ! (dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, suitTemp)
										&& dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, Constants.SPADE))) {
							if(dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitTemp) > 3) {
								
								if(numCardsOfSuitInHand == 1) {
									curScore -= 20.0;
								} else if(numCardsOfSuitInHand == 2) {
									curScore -= 10.0;
								}
								
							}
						}
					}
				}
				//END Check if we're wasting spades we can trump with:
				
				//End basic awareness...
				
			}
			
			
			if(curScore > currentBestScore) {
				currentBestScore = curScore;
				bestCardToPlay = cardToPlay;
			}
			
		}
		
		return bestCardToPlay;
	}

	public static String AISecondThrow(DataModel dataModel) {
		String cardToPlay = null;
		//get suit to follow.
		
		System.out.println("2nd throw");
		//START REALLY OLD CODE:
		//SEE NOTES FOR BETTER PLAN
		//TODO: pseudo code for not following suit
		
		
		//TODO: only deal with string (No index)
		int leaderSuitIndex = dataModel.getSuitOfLeaderThrow();
		String leaderCard = dataModel.getCardLeaderThrow();
		
		//TODO currentAgentHasSuit and isVoid does the same thing...?
		if(dataModel.currentAgentHasSuit(leaderSuitIndex)) {
			
			if(dataModel.couldPlayCardInHandOverCardInSameSuit(leaderCard)) {
				
				boolean thirdVoid = dataModel.isVoid(1, leaderSuitIndex);
				boolean fourthVoid = dataModel.isVoid(2, leaderSuitIndex);
			
			
				if(thirdVoid && fourthVoid) {	
					cardToPlay = dataModel.getCardInHandClosestOverSameSuit(dataModel.getCardLeaderThrow());

				} else if(thirdVoid && fourthVoid == false) {
					//Maybe play low? I don't know...
					cardToPlay = dataModel.getCardInHandClosestOverSameSuit(dataModel.getCardLeaderThrow());
				
				} else if(thirdVoid == false && fourthVoid) {
					//TODO This doesn't really work if trump is spade... 
					cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(leaderSuitIndex);
					
				} else if(thirdVoid == false && fourthVoid == false){
					
					if(dataModel.currentPlayerHasMasterInSuit(leaderSuitIndex)) {
						cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(leaderSuitIndex);
					} else {
						
						String curPlayerTopCardInSuit = dataModel.getCardCurrentPlayerGetHighestInSuit(leaderSuitIndex);
						
						if((dataModel.getRankIndex(curPlayerTopCardInSuit) == dataModel.KING
								&& dataModel.getNumCardsOfSuitInCurrentPlayerHand(leaderSuitIndex) >= 2)
							) {
							
							//2nd throw: Play the King's wing-person card if it's higher than the lead card...
							
							cardToPlay = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(leaderSuitIndex);
							
							if(DataModel.getRankIndex(cardToPlay) == DataModel.QUEEN) {
								//If you have the KQ, just play the K...
								//Don't confuse your partner.
								cardToPlay = curPlayerTopCardInSuit;
								
								//TODO: maybe play lower one (I don't know)
							}
							
							if(dataModel.cardAGreaterThanCardBGivenLeadCard(cardToPlay, leaderCard)) {
								
								return cardToPlay;
							} else {
								//Play the King while the Ace is still out!
								return curPlayerTopCardInSuit;
							}
						
											
						} else if(dataModel.throwerHasCardToBeatCurrentWinner()) {
							
							//Consider playing high second if there's no real chance of making the trick,
							// but you want 3rd thrower to prove they can play higher than you:
							if(leaderSuitIndex != Constants.SPADE
								&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(curPlayerTopCardInSuit) >= 3
								&& (dataModel.getNumCardsCurrentUserStartedWithInSuit(Constants.SPADE) < 5
								      ||  leaderSuitIndex == Constants.SPADE
								      || 1 + dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(curPlayerTopCardInSuit)
								                  < dataModel.getNumberOfCardsOneSuit(leaderSuitIndex))
								) {
								
								//Observation based on original test cases:
								//This condition turns test cases from "AI matches expert alternative response! (PASS)"
								// to "AI matches expert response! (PASS)"
								//So basically, it plays more like me. But whether it's a good thing or not is
								//still in the air.
								
								//I also tend to NOT play like this when it comes to spades...
								//but whether it's a good thing or not is
								//still in the air.
								
								return curPlayerTopCardInSuit;
								
							} else if(leaderSuitIndex == Constants.SPADE) {
								String cardToConsider = dataModel.getCardInHandClosestOverCurrentWinner();
								

								//Save K/Q equiv and try to not throw it if you don't have both the K and the Q equiv:
								if(hasKQEquiv(dataModel, Constants.SPADE)) {
									return cardToConsider;
								
								} else if(dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE).equals(cardToConsider)
										&& (hasKEquiv(dataModel, Constants.SPADE) || hasQEquiv(dataModel, Constants.SPADE))) {
									

									//TODO: maybe make sure partner didn't signal that they only have only the master trump
									//because in that case, maybe play low??
									//Actually I'm not sure, because partner signal only high might mean they are void too...

									if((dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)
										&&	dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(cardToConsider) >= 1)) {
										
										return cardToConsider;
										
									} else {
										return dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
									}
									
								} else {
									return cardToConsider;
									
								}
								
							} else {
								
								//Just play slightly over the person who lead:
								return dataModel.getCardInHandClosestOverCurrentWinner();
							}
						
						} else {
							return dataModel.getCardCurrentPlayerGetLowestInSuit(leaderSuitIndex);
						}
						
						
					}
					
				} else {
					System.err.println("ERROR: this condition shouldn't happen in get ai 2nd throw");
					System.exit(1);
				}

			} else {
				cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(leaderSuitIndex);
			}
			
			
			//No following suit:
		} else {
			
			
			//no trumping: play off:
			if(leaderSuitIndex== Constants.SPADE || dataModel.isVoid(0, Constants.SPADE)) {
				cardToPlay = getJunkiestCardToFollowLead(dataModel);
				
				//Must play trump:
			} else if(dataModel.currentPlayerMustTrump()) {
				cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
				
				//Option to trump:
			} else {
				
				
				if(dataModel.isMasterCard(leaderCard)
						&& dataModel.getNumberOfCardsOneSuit(Constants.SPADE) >= 1) {
						
						//&& (dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE) || dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, leaderSuitIndex) == false)) {
					
					if((dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE) == false 
							&& dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, leaderSuitIndex)
							&& 3 * dataModel.getNumberOfCardsOneSuit(Constants.SPADE) > dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE))
						) {
						
						cardToPlay = getJunkiestCardToFollowLead(dataModel);
					} else {
						
						String consideredHighTrump = dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
						
						if(dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(consideredHighTrump)
								>= dataModel.getNumberOfCardsOneSuit(Constants.SPADE)) {
							cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
						
						} else if(dataModel.getNumCardsCurrentUserStartedWithInSuit(Constants.SPADE) >= 4) {
							
							//TODO: something more complicated...
							//if(dataModel.getNumCardsOfSuitInCurrentPlayerHand(Constants.SPADE) >= 3) {
							//	cardToPlay = dataModel.getCardCurrentPlayergetThirdLowestInSuit(Constants.SPADE);
							//	
							//} else {
							//	cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
							//}
							cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
						} else {
							cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
						}
					}

				//I guess we should trump if we don't have much spade?
					
				} else if(
						//Partner probably can't trump
						(dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE) || dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, leaderSuitIndex) == false) 
						&& 
						//Not much spade: 
						((Constants.NUM_RANKS - dataModel.getNumCardsPlayedForSuit(Constants.SPADE))/4 >= dataModel.getNumberOfCardsOneSuit(Constants.SPADE))
						                //OR only 1 non-master spade:
						      || (dataModel.getNumberOfCardsOneSuit(Constants.SPADE) == 1 
						         && dataModel.currentPlayerHasMasterInSuit(Constants.SPADE) == false)
						&& //No K to protect:
						 ! (dataModel.getNumberOfCardsOneSuit(Constants.SPADE) == 2
						           && hasKEquiv(dataModel, Constants.SPADE))
						 && //No Q to protect:
						 ! (dataModel.getNumberOfCardsOneSuit(Constants.SPADE) == 3
				                   && hasQEquiv(dataModel, Constants.SPADE))
						) {		

					cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);

					
					if(dataModel.isEffectivelyMasterCardForPlayer(Constants.CURRENT_AGENT_INDEX, cardToPlay)) {
						cardToPlay = getJunkiestCardToFollowLead(dataModel);
					}
					
				} else {
					cardToPlay = getJunkiestCardToFollowLead(dataModel);
				}
			}
		}
		
	
		return cardToPlay;
	}
	
	public static String AIThirdThrow(DataModel dataModel) {
		String cardToPlay = null;
		int leaderSuitIndex = dataModel.getSuitOfLeaderThrow();
		
		
		//CAN'T FOLLOW SUIT:
		if(dataModel.currentAgentHasSuit(leaderSuitIndex) == false) {

			if(dataModel.currentAgentHasSuit(Constants.SPADE)) {
				
				//2nd thrower trumped:
				if( dataModel.getSuitOfSecondThrow() == Constants.SPADE) {
					
					//if could trump over, just go barely over
					if(dataModel.couldPlayCardInHandOverCardInSameSuit(dataModel.getCardSecondThrow())) {
						
						//TODO: what if 4th thrower is also able to trump? That gets into weird logic
						cardToPlay = dataModel.getCardInHandClosestOverSameSuit(dataModel.getCardSecondThrow());
					
					} else {
						//If can't trump over
						cardToPlay = getJunkiestCardToFollowLead(dataModel);
					}
				} else {
					
					//if your partner played master and 2nd thrower didn't trump over:
					if(dataModel.isPartnerWinningFight() && dataModel.leaderPlayedMaster()) {
						//PLAY OFF because leaderPlayedMaster
						cardToPlay = getJunkiestCardToFollowLead(dataModel);
					} else {
						//TRUMP
						
						//TODO: what if leader(partner) plays a higher card than 2nd throw that isn't master, but 4th could trump too... 
						//... I don't even know. That gets into weird logic
						
						if(dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, leaderSuitIndex)) {
							
							cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
						} else {
							cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
						}
					}
				}
				
				//No Spade, so play off:
			} else {
				cardToPlay = getJunkiestCardToFollowLead(dataModel);
			}
		
		//FOLLOW SUIT:
		} else {
			
			//If leader got TRUMPED by 2nd player:
			if(dataModel.getSuitOfLeaderThrow() != Constants.SPADE && dataModel.getSuitOfSecondThrow() == Constants.SPADE) {
					cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(leaderSuitIndex);
			
			//FIGHT WITHIN SUIT:
			} else {
				
				//If lead is winning
				if(dataModel.cardAGreaterThanCardBGivenLeadCard(dataModel.getCardLeaderThrow(), dataModel.getCardSecondThrow())) {
					
					if(dataModel.hasNonLeadCardInHandThatCanDecreaseChanceof4thThrowerWinning()) {
						cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(dataModel.getSuitOfLeaderThrow());
						
					} else {
						
						
						cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(dataModel.getSuitOfLeaderThrow());
					}
				
				//If 2nd thrower is winning:
				} else {
				
					//If currentAgent could play over 2nd thrower:
					if(dataModel.couldPlayCardInHandOverCardInSameSuit(dataModel.getCardSecondThrow())) {
						
						//Sanity check:
						if(dataModel.getSuitOfSecondThrow() != dataModel.getSuitOfLeaderThrow()) {
							System.err.println("ERROR: At this point, I expected the 2nd thrower to have followed suit.");
							System.exit(1);
						}
						
						//If we know 4th is void:
						if(dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, dataModel.getSuitOfLeaderThrow())) {
							
							//Play highest to force 4th to play even higher... or stop 4th thrower from winning:
							cardToPlay = dataModel.getCardInHandClosestOverSameSuit(dataModel.getCardSecondThrow());
						
						} else {
							//play barely over 2nd thrower to force 4th thrower to trump for the win:
							cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(dataModel.getSuitOfLeaderThrow());
						}
					} else {
						
						cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(dataModel.getSuitOfLeaderThrow());
					}
				}
			}
		}
				
	
		return cardToPlay;
	}
	
	public static String AIFourthThrow(DataModel dataModel) {
		String cardToPlay = null;

		if(dataModel.isPartnerWinningFight()) {
			
			
			//Could we take for a tram?
			if(dataModel.throwerHasCardToBeatCurrentWinner()) {
				
				
				//TODO: don't steal if you have trump left and there's less than 3 cards... 
				//if(dataModel.isVoid)
				
				String tramTrickTakingCard = dataModel.getCardInHandClosestOverCurrentWinner();
				if(couldTRAMAfterPlayingCard(dataModel, tramTrickTakingCard)) {
					
					System.out.println("4th thrower taking from partner to TRAM!");
					return tramTrickTakingCard;
				}
			}

			cardToPlay = getJunkiestCardToFollowLead(dataModel);
			
			return cardToPlay;
			
		} else if(dataModel.throwerHasCardToBeatCurrentWinner()) {
			cardToPlay = dataModel.getCardInHandClosestOverCurrentWinner();
			
		} else {
			cardToPlay = getJunkiestCardToFollowLead(dataModel);
			
		}

		return cardToPlay;
	}
	//END AIS for non-nellow bid games
	
	
	//TRAM logic (not tested that much...)
	public static boolean couldTRAM(DataModel dataModel) {
		if(dataModel.playerCouldSweepSpades(Constants.CURRENT_AGENT_INDEX) == false) {
			return false;
		}
		
		boolean theRestAreMine = true;
		for(int suitIndex = 0; suitIndex < Constants.NUM_SUITS; suitIndex++) {
			if(suitIndex != Constants.SPADE) {
				if(dataModel.playerWillWinWithAllCardsInHandForSuitIfNotTrumped(
								Constants.CURRENT_AGENT_INDEX,
								suitIndex) == false) {
					
					theRestAreMine = false;
					break;
				}
			}
		}
		return theRestAreMine;
	}
	
	public static boolean couldTRAMAfterPlayingCard(DataModel dataModel, String cardPlayedForTrick) {
		
		if(dataModel.playerCouldSweepSpadesMinusCardToTakeTrick(
				Constants.CURRENT_AGENT_INDEX,
				cardPlayedForTrick) 
			== false) {
			
			return false;
		}
		
		boolean theRestAreMine = true;
		for(int suitIndex = 0; suitIndex < Constants.NUM_SUITS; suitIndex++) {

			if(suitIndex != Constants.SPADE) {
				if(dataModel.playerWillWinWithAllCardsInHandForSuitIfNotTrumpedMinusCardToTakeTrick(
						Constants.CURRENT_AGENT_INDEX,
						suitIndex,
						cardPlayedForTrick)
					== false
						) {

					theRestAreMine = false;
					break;
				}
			}
		}
		return theRestAreMine;
	}
	//END OF TRAM LOGIC
	
	public static String getJunkiestCardToFollowLead(DataModel dataModel) {
		
		if(dataModel.throwerMustFollowSuit()) {
			return dataModel.getCardCurrentPlayerGetLowestInSuit(dataModel.getSuitOfLeaderThrow());
		
		} else if(dataModel.currentPlayerOnlyHasSpade()){
			return dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
			
		}/* else if(dataModel.currentAgentHasSuit(Constants.SPADE)){
			
			return getJunkiestOffSuitCardBasedOnMadeupValueSystem(dataModel);

		} else {
			return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
		}*/
		return getJunkiestOffSuitCardBasedOnMadeupValueSystem(dataModel);
		
	}
	
	
	//TODO: maybe throw off low pretending you could trump...
	//That means make signals about whether or not others know you're void in spades...
	//That's hard... but do-able.
	
	//TODO: what if there's strategy around signalling?
	
	public static String getJunkiestOffSuitCardBasedOnMadeupValueSystem(DataModel dataModel) {

		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "AS QS TS JH TH 5H QC 5C 4C JD 6D 4D 2D ")) {
			System.out.println("Debug");
		}
		
		System.out.println("**In getJunkiestOffSuitCardBasedOnMadeupValueSystem");
		
		int bestSuit = -1;
		double valueOfBestSuit = 0;
		
		for(int suitIndex=0; suitIndex<Constants.NUM_SUITS; suitIndex++) {
			if(suitIndex == Constants.SPADE || dataModel.currentAgentHasSuit(suitIndex) == false) {
				continue;
			}
			
			int numberOfCardsInSuit = dataModel.getNumCardsOfSuitInCurrentPlayerHand(suitIndex);
			
			if(numberOfCardsInSuit == 0) {
				continue;
			}

			String curCard = dataModel.getCardCurrentPlayerGetLowestInSuit(suitIndex);
			String bestCardPlayerHas = dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex);
			
			int numUnderLowest = dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(curCard);
			int numOverHighest = dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(bestCardPlayerHas);

			double currentValue = 0.0;
			
			currentValue -= numUnderLowest;
			
			boolean partnerVoid = dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, suitIndex);
			boolean partnerTrumping = dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE) == false;

			//Easy for throwing off:
			if(dataModel.getNumCardsOfSuitInCurrentPlayerHand(Constants.SPADE) == 0
					&& numUnderLowest == 0 
					&& numOverHighest >= 1
					&& numberOfCardsInSuit == 1) {
				
				System.out.println("Nothing lower...");
				
				
				if(numOverHighest == 1) {
					currentValue += 75;
				} else {
					currentValue += 100;
				}
				
				//Logic doesn't really do anything yet.
				if(partnerVoid && partnerTrumping) {
					System.out.println("DEBUG WARNING");
					currentValue -= 50;
				}
			} else if(numberOfCardsInSuit < numOverHighest + 1) {
				
				//Reliable hammer for throwing off that I discovered:
				
				//TODO: what if throwing off a card signals that opponent could TRAM suit?
				//Shouldn't we be careful about that?
				currentValue += 5 * (numOverHighest - numberOfCardsInSuit);
			}

			//End easy for throwing off

			int numberOfCardsOthersHaveInSuit = dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex);
			
			
			
			if(numberOfCardsInSuit == 1 &&  dataModel.currentPlayerHasMasterInSuit(suitIndex)) {
				//Don't throw off master cards unless you really need to...
				currentValue -= 10.0;
				
				//Maybe it's not bad if you're planing on trumping though...
				
			} else if( (numberOfCardsInSuit == 2 || numberOfCardsInSuit == 3)
					&&  hasKEquiv(dataModel, suitIndex)
					&& numberOfCardsOthersHaveInSuit >= 2) {
				
				//Don't throw off option to play low and save your Kequiv card for later
				currentValue -= 3.0;
				//System.out.println("MICHAEL HERE " + suitIndex);
				
				if(numberOfCardsInSuit == 2) {
					currentValue -= 2.0;
				}
	
			} else if(numberOfCardsInSuit == 2 &&  dataModel.currentPlayerHasMasterInSuit(suitIndex)
					&& ! dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, suitIndex)) {
				
				//Don't throw off option to play low and save your master card for later
				currentValue -= 3.0;
				//int numOverHighest = dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(bestCardPlayerHas);
				int numOverSecondHighest = dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(
												dataModel.getCardCurrentPlayerGetSecondHighestInSuit(suitIndex));
				
				//Don't throw off secondary master card...
				if(numOverSecondHighest == numOverHighest) {
					currentValue -= 3.0;
				}
	
			}   else if((numberOfCardsInSuit == 2 ||numberOfCardsInSuit == 3)
					&&  dataModel.currentPlayerHasMasterInSuit(suitIndex)) {
				
				//Don't throw off option to play low twice and save your master card for later
				currentValue -= 2.5;
				
				
			} else if(numberOfCardsInSuit >= 2 
					&& numberOfCardsInSuit <= 3 
					&& numOverHighest == 1) {
				
				currentValue -= 1.2;
				
				
			} else if(numberOfCardsInSuit >= 3 
					&& numberOfCardsInSuit <= 4 
					&& numOverHighest == 2) {
				currentValue -= 1.1;
				
			} else if(dataModel.getNumCardsCurrentUserStartedWithInSuit(suitIndex) == 3 && 
					dataModel.getRankIndex(bestCardPlayerHas) == dataModel.KING) {
				
				currentValue -= 0.8;
			}
			
			
			//boolean rhsVoid = dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, suitIndex);
			boolean rhsTrumping = dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, suitIndex)
					  && ! dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE);
			
			//boolean lhsVoid = dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, suitIndex);
			boolean lhsTrumping = dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, suitIndex)
					  && ! dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, Constants.SPADE);
			
			if(rhsTrumping) {
				currentValue += 2.0;
			}
			
			if(numUnderLowest == 0 && rhsTrumping) {
				if(numberOfCardsOthersHaveInSuit == 0) {
					currentValue -= 5.0;
				} else {
					currentValue -= 8.0;
				}
			}

		
			
			
			if(numberOfCardsOthersHaveInSuit == 0) {
				
				//TODO only good if you could sweep spades and lead it...
				currentValue += 2.0;

			}
			
			
			if(dataModel.currentAgentHasSuit(Constants.SPADE)) {
				if(rhsTrumping) {
					
					double numTimesToFollowSuitIfThrowOff = (1.0 *numberOfCardsInSuit - 1.0);
					
					double approxNumTrumpingRoundIdeal =  (1.0/ 2.0) * numberOfCardsOthersHaveInSuit - numTimesToFollowSuitIfThrowOff;
					
					double BONUS_VALUE = 0.5;
					
					currentValue += approxNumTrumpingRoundIdeal + BONUS_VALUE;
				} else {
					
					double numTimesToFollowSuitIfThrowOff = (1.0 *numberOfCardsInSuit - 1.0);
					
					double approxNumTrumpingRoundIdeal =  (1.0/ 3.0) * numberOfCardsOthersHaveInSuit - numTimesToFollowSuitIfThrowOff;
					currentValue += approxNumTrumpingRoundIdeal;
					
				}
			} else {
				
				currentValue += numberOfCardsInSuit;
			}
			
			
			
			if(bestSuit == -1 || currentValue > valueOfBestSuit) {
				valueOfBestSuit = currentValue;
				bestSuit = suitIndex;
			}
			
		}
		
		return dataModel.getCardCurrentPlayerGetLowestInSuit(bestSuit);
	}
	
	public static boolean hasKQEquiv(DataModel dataModel, int suitIndex) {
		
		if(dataModel.getNumberOfCardsOneSuit(suitIndex) < 2) {
			return false;
		}
		
		String cardA = dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex);
		String cardB = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(suitIndex);
		
		int numOver = 0;
		
		for(int curRank = dataModel.ACE; curRank > DataModel.getRankIndex(cardB); curRank--) {
			if(dataModel.getCardsCurrentlyHeldByPlayers()[Constants.CURRENT_AGENT_INDEX][suitIndex][curRank] == DataModel.CERTAINTY) {
				continue;

			} else if(dataModel.isCardPlayedInRound(
					dataModel.getCardString(curRank, suitIndex))
					) {
				continue;

			} else {
				numOver++;
				if(numOver > 1) {
					return false;
				}
			}
		}
		
		if(numOver == 1) {
			return true;
		} else {
			return false;
		}
	}
	
public static boolean hasKEquiv(DataModel dataModel, int suitIndex) {
		
		if(dataModel.getNumberOfCardsOneSuit(suitIndex) < 1) {
			return false;
		}
		
		String cardA = dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex);
		
		int numOver = 0;
		
		for(int curRank = dataModel.ACE; curRank > DataModel.getRankIndex(cardA); curRank--) {
			if(dataModel.getCardsCurrentlyHeldByPlayers()[Constants.CURRENT_AGENT_INDEX][suitIndex][curRank] == DataModel.CERTAINTY) {
				continue;

			} else if(dataModel.isCardPlayedInRound(
					dataModel.getCardString(curRank, suitIndex))
					) {
				continue;

			} else {
				numOver++;
				if(numOver > 1) {
					return false;
				}
			}
		}
		
		if(numOver == 1) {
			return true;
		} else {
			return false;
		}
	}


//TODO: put into data model??
     public static boolean hasQEquiv(DataModel dataModel, int suitIndex) {
		
		if(dataModel.getNumberOfCardsOneSuit(suitIndex) < 1) {
			return false;
		}
		
		String cardA = dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex);
		
		int numOver = 0;
		
		for(int curRank = dataModel.ACE; curRank > DataModel.getRankIndex(cardA); curRank--) {
			if(dataModel.getCardsCurrentlyHeldByPlayers()[Constants.CURRENT_AGENT_INDEX][suitIndex][curRank] == DataModel.CERTAINTY) {
				continue;

			} else if(dataModel.isCardPlayedInRound(
					dataModel.getCardString(curRank, suitIndex))
					) {
				continue;

			} else {
				numOver++;
				if(numOver > 2) {
					return false;
				}
			}
		}

		if(numOver == 2) {
			return true;
		} else {
			return false;
		}
	}
}

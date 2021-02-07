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
	
	
	//AIs for non-mellow bid games:
	/*
	public static String AILeaderThrow(DataModel dataModel) {
		String cardToPlay = null;
		
		//TODO: this shouldn't be 1st priority
		     // Weight the benefits of playing every card and then choose...
		cardToPlay = leadCardToHelpPartnerTrumpOtherwiseNull(dataModel);

		if(cardToPlay != null) {
			return cardToPlay;
		}
	
		//isVoid(int playerIndex, int suitIndex)
		//if(partnerisVoid)
		
		
		
		//TODO: use signals to not play certain suits...
		
		//TODO: what if we want the opponents to trump???
		
		
		//TODO: check if master card in SAFE SUIT!!!!
		if(dataModel.getMasterCardInSafeSuit() != null) {
			
			if(dataModel.playerCouldSweepSpades(Constants.CURRENT_AGENT_INDEX)) {
				
				if(couldTRAM(dataModel)) {
					System.out.println("THE REST ARE MINE! (TRAM)");
					if(dataModel.currentAgentHasSuit(Constants.SPADE)) {
						return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
					} else {
						return dataModel.getMasterCard();
					}
				}
			}
			
			//TODO: avoid playing certain suits based on signals...
			//play a master card:
			cardToPlay = dataModel.getMasterCardInSafeSuit();
			
			System.out.println("***********");
			System.out.println("Playing master card: " + cardToPlay);
			System.out.println("***********");
		} else {
			System.out.println("***********");
			System.out.println("Leading low:");
			System.out.println("***********");
			
			
			cardToPlay = dataModel.getLowOffSuitCardToLeadInSafeSuit();
			if(cardToPlay == null) {
				cardToPlay = dataModel.getLowOffSuitCardToLead();
			}
			 
		}
		
		return cardToPlay;
	}
*/
	public static String AILeaderThrow(DataModel dataModel) {
		

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

			int numCardsOfSuitOtherPlayersHave = Constants.NUM_RANKS
					- dataModel.getNumCardsPlayedForSuit(suitIndex) 
					- dataModel.getNumCardsOfSuitInCurrentPlayerHand(suitIndex);
			
			int numCardsOfSuitInHand = dataModel.getNumCardsOfSuitInCurrentPlayerHand(suitIndex);
			
			//Can't play a suit you don't have:
			if(numCardsOfSuitInHand == 0) {
				continue;
			}

		
			if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "KS 8S 5S 6H 4H 3H 5C AD TD 8D ")) {
				System.out.println("DEBUG");
			}
			
			//1454
			//if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "8C 6D 3D ")) {
			//	System.out.println("DEBUG2");
			//}
			
			String cardToPlay = null;
			
			double curScore = 0.0;
			

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
				
			}
			
			if(suitIndex != Constants.SPADE) {

				
				
				//Check if leading suitIndex helps partner trump:
				if(dataModel.isVoid(Constants.CURRENT_AGENT_INDEX, suitIndex) == false

					&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
						(Constants.CURRENT_PARTNER_INDEX, suitIndex)
						
					&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
						(Constants.RIGHT_PLAYER_INDEX, suitIndex) == false
						
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
						} else {
							cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(suitIndex);
						}
				} else if(numCardsOfSuitOtherPlayersHave == 0) {
					//Might want to do this if right is out of spades...
					
					cardToPlay = dataModel.getMasterInHandOfSuit(suitIndex);
					
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
						(Constants.RIGHT_PLAYER_INDEX, suitIndex) == false) {
						
						curScore += 25.0;
						
						//TODO: this is a sledge-hammer. Try to be more subtle than this:
						//If you take it away, you get to testcase file #12
						if( ! dataModel.currentPlayerHasMasterInSuit(suitIndex)) {

							curScore -= 25.0;
						}
						
						//END TODO
						
					} else if( ! dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)) {
						curScore -= 10.0;
					}
					
					//Play the ace so you back it up with a king:
					if(numCardsOfSuitInHand > 1
						&& dataModel.getRankIndex(dataModel.getCardCurrentPlayerGetSecondHighestInSuit(suitIndex)) == dataModel.KING) {
						curScore += 5.0;
						
					}

					if(dataModel.getRankIndex(dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex)) == dataModel.KING
							&& numCardsOfSuitInHand > 1
							&& dataModel.getRankIndex(dataModel.getCardCurrentPlayerGetSecondHighestInSuit(suitIndex)) < dataModel.QUEEN
							&& dataModel.isMasterCard(dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex)) == false) {
						
						curScore -= 50.0;
						
					}
					
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
					} else {
						
						if(dataModel.getRankIndex(dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex)) <= DataModel.RANK_TEN) {
							cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex);
						} else {
							cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(suitIndex);
						}
						
						//TODO: terrible hack to fix
						if(dataModel.hasCard(DataModel.getCardString(DataModel.KING, suitIndex))
								&& dataModel.hasCard(DataModel.getCardString(DataModel.QUEEN, suitIndex))) {
							cardToPlay = DataModel.getCardString(DataModel.KING, suitIndex);
						}
						//END TODO
					}
					
					//I like leading offsuit queens... if no one is void and I don't have a hope of winning with it.
					if(dataModel.getRankIndex(dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex)) == DataModel.QUEEN
							&& 
						! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
							(Constants.LEFT_PLAYER_INDEX, suitIndex)
							&& 
						! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
								(Constants.RIGHT_PLAYER_INDEX, suitIndex)) {
						
						if(dataModel.getNumCardsCurrentUserStartedWithInSuit(suitIndex) < 3
								|| (dataModel.getNumCardsCurrentUserStartedWithInSuit(suitIndex) > 4
										&& dataModel.getNumCardsCurrentUserStartedWithInSuit(Constants.SPADE) < 5)
								) {
							curScore += 10.0;
							
							cardToPlay = DataModel.getCardString(DataModel.QUEEN, suitIndex);

							if(dataModel.hasCard( DataModel.getCardString(DataModel.JACK, suitIndex))) {
								curScore += 5.0;
							}
						}
						
						
					}
				}
			} else {
				//TODO: consider leading spade here...
				
				if(dataModel.currentPlayerHasMasterInSuit(suitIndex)) {
					
					//TODO: if you have top 2 spade bonus:
					//For now, just AS, KS
					if(dataModel.hasCard("AS")
							&&  dataModel.hasCard("KS")) {
						curScore += 20.0;
					}
					
					
					for(int suitTemp=1; suitTemp<Constants.NUM_SUITS; suitTemp++) {
						if(dataModel.currentPlayerHasMasterInSuit(suitIndex)
								&& ! dataModel.hasCard(DataModel.getCardString(DataModel.ACE, suitTemp))
								|| (dataModel.hasCard(DataModel.getCardString(DataModel.ACE, suitTemp))
										&& dataModel.hasCard(DataModel.getCardString(DataModel.KING, suitTemp)))) {
							curScore += 10.0;
						}
					}
					
					cardToPlay = dataModel.getMasterInHandOfSuit(suitIndex);
				} else {
					curScore += 5.0;
					cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(suitIndex);
				}
			}
			
			
			
			

			if(curScore > currentBestScore) {
				currentBestScore = curScore;
				bestCardToPlay = cardToPlay;
			}
			
		}
		
		return bestCardToPlay;
	}
/*
	public static String leadCardToHelpPartnerTrumpOtherwiseNull(DataModel dataModel) {
		
		
		String bestCardToPlay = null;
		double currentBestScore = 0.0;
		
		for(int suitIndex = 0; suitIndex< Constants.NUM_SUITS; suitIndex++) {
			
			if(suitIndex != Constants.SPADE) {
				
				//Check if leading suitIndex helps partner trump:
				if(dataModel.isVoid(Constants.CURRENT_AGENT_INDEX, suitIndex) == false

					&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
						(Constants.CURRENT_PARTNER_INDEX, suitIndex)
						
					&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
						(Constants.RIGHT_PLAYER_INDEX, suitIndex) == false) {
				
					int numCardsOfSuitOtherPlayersHave = Constants.NUM_RANKS
							- dataModel.getNumCardsPlayedForSuit(suitIndex) 
							- dataModel.getNumCardsOfSuitInCurrentPlayerHand(suitIndex);
					
					if(numCardsOfSuitOtherPlayersHave >= 2
								
							//If player on left is also void, that's great!
							|| (numCardsOfSuitOtherPlayersHave >= 1
								&& dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, suitIndex))
							) {

						
						double curScore = numCardsOfSuitOtherPlayersHave;
						String cardToPlay = null;
						
						
						if(dataModel.currentPlayerHasMasterInSuit(suitIndex)) {
							cardToPlay = dataModel.getMasterInHandOfSuit(suitIndex);
							
							//Made up a number to say having the master card to lead in partner's void suit is cool:
							//TODO: refine later
							curScore += 5.0;
							
						} else {
							cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(suitIndex);
						}

						if(curScore > currentBestScore) {
							currentBestScore = curScore;
							bestCardToPlay = cardToPlay;
						}
						

					} else {
						continue;
					}
				}
				
				//TODO: Score leading other scenarios here!
				
			}
		}
		
		return bestCardToPlay;
	}
	*/
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
						
						if(dataModel.getRankIndex(curPlayerTopCardInSuit) == dataModel.KING
								&& dataModel.getNumCardsOfSuitInCurrentPlayerHand(leaderSuitIndex) >= 2) {
							
							//2nd throw: Play the King's wing-person card if it's higher than the lead card...
							
							cardToPlay = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(leaderSuitIndex);
							
							if(dataModel.cardAGreaterThanCardBGivenLeadCard(cardToPlay, leaderCard)) {
								
								return cardToPlay;
							} else {
								//Play the King while the Ace is still out!
								return curPlayerTopCardInSuit;
							}
								
											
						} else if(dataModel.throwerHasCardToBeatCurrentWinner()) {
							return dataModel.getCardInHandClosestOverCurrentWinner();
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
					
				} else if((dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE) || dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, leaderSuitIndex) == false) 
						&& 
						//Not much spade:
						((Constants.NUM_RANKS - dataModel.getNumCardsPlayedForSuit(Constants.SPADE))/4 >= dataModel.getNumberOfCardsOneSuit(Constants.SPADE))
						//OR only 1 non-master spade:
						   || (dataModel.getNumberOfCardsOneSuit(Constants.SPADE) == 1 && dataModel.currentPlayerHasMasterInSuit(Constants.SPADE) == false)) {		

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
			
		} else if(dataModel.currentAgentHasSuit(Constants.SPADE)){
			
			return getJunkiestOffSuitCardBasedOnMadeupValueSystem(dataModel);

		} else {
			return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
		}
		
	}
	
	
	public static String getJunkiestOffSuitCardBasedOnMadeupValueSystem(DataModel dataModel) {

		
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

			
			String bestCardPlayerHas = dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex);
			
			double currentValue = 0.0;
			if(numberOfCardsInSuit == 1 && dataModel.currentPlayerHasMasterInSuit(suitIndex)) {
				
				//Don't throw off master cards unless you really need to...
				currentValue -= 5.0;
			} else if(numberOfCardsInSuit == 2 && dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(bestCardPlayerHas) == 1) {
				
				currentValue -= 1.2;
				
				
			} else if(numberOfCardsInSuit == 3 && dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(bestCardPlayerHas) == 2) {
				currentValue -= 1.1;
				
			} else if(dataModel.getNumCardsCurrentUserStartedWithInSuit(suitIndex) == 3 && 
					dataModel.getRankIndex(bestCardPlayerHas) == dataModel.KING) {
				
				currentValue -= 0.8;
			}
			
			
			
			int numberOfCardsOthersHaveInSuit = dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex);
			
			
			//boolean rhsVoid = dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, suitIndex);
			boolean rhsTrumping = dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE);
			
			//boolean lhsVoid = dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, suitIndex);
			boolean lhsTrumping = dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, Constants.SPADE);
			
			if(lhsTrumping) {
				currentValue -= 2.0;
			}

			//boolean partnerVoid = dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, suitIndex);
			//boolean partnerTrumping = dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE);

			
			
			//if(partnerTrumping || lhsTrumping) {
				//currentValue = 0.0;
			//} else 
			if(numberOfCardsOthersHaveInSuit == 0) {
				//only good if you could sweep spades and lead it...
				currentValue += 0.2;
			
			} else if(rhsTrumping) {
				
				double numTimesToFollowSuitIfThrowOff = (1.0 *numberOfCardsInSuit - 1.0);
				
				double approxNumTrumpingRoundIdeal =  (1.0/ 2.0) * numberOfCardsOthersHaveInSuit - numTimesToFollowSuitIfThrowOff;
				
				double BONUS_VALUE = 0.5;
				
				currentValue += approxNumTrumpingRoundIdeal + BONUS_VALUE;
			} else {
				
				double numTimesToFollowSuitIfThrowOff = (1.0 *numberOfCardsInSuit - 1.0);
				
				double approxNumTrumpingRoundIdeal =  (1.0/ 3.0) * numberOfCardsOthersHaveInSuit - numTimesToFollowSuitIfThrowOff;
				currentValue += approxNumTrumpingRoundIdeal;
				
			}
			
			if(bestSuit == -1 || currentValue > valueOfBestSuit) {
				valueOfBestSuit = currentValue;
				bestSuit = suitIndex;
			}
			
		}
		
		return dataModel.getCardCurrentPlayerGetLowestInSuit(bestSuit);
	}
	
}

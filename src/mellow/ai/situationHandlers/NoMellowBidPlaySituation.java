package mellow.ai.situationHandlers;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.simulation.MonteCarloMain;

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
	
	public static String AILeaderThrow(DataModel dataModel) {
		String cardToPlay = null;
		
		cardToPlay = leadCardToHelpPartnerTrumpOtherwiseNull(dataModel);

		if(cardToPlay != null) {
			return cardToPlay;
		}
	
		//isVoid(int playerIndex, int suitIndex)
		//if(partnerisVoid)
		
		if(dataModel.getMasterCard() != null) {
			//play a master card:
			cardToPlay = dataModel.getMasterCard();
			System.out.println("***********");
			System.out.println("Playing master card: " + cardToPlay);
			System.out.println("***********");
		} else {
			System.out.println("***********");
			System.out.println("Leading low:");
			System.out.println("***********");
			cardToPlay = dataModel.getLowOffSuitCardToLead();
		}
		
		return cardToPlay;
	}


	public static String leadCardToHelpPartnerTrumpOtherwiseNull(DataModel dataModel) {
		String cardToPlay = null;
		
		for(int suitIndex = 0; suitIndex< Constants.NUM_SUITS; suitIndex++) {
			
			if(suitIndex != Constants.SPADE) {
				
				
				if(dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, suitIndex)
					&&  dataModel.isVoid(Constants.CURRENT_AGENT_INDEX, suitIndex) == false
					&&  dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, suitIndex) == false) {
				
					int numCardsOfSuitOpponentsHave = Constants.NUM_RANKS -
							- dataModel.getNumCardsPlayedForSuit(suitIndex) 
							- dataModel.getNumCardsOfSuitInCurrentPlayerHand(suitIndex);
					
					if(numCardsOfSuitOpponentsHave >= 2
								
							//If player on left is also void, that's great!
							|| (numCardsOfSuitOpponentsHave >= 1
								&& dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, suitIndex))
							) {

						if(dataModel.currentPlayerHasMasterInSuit(suitIndex)) {
							cardToPlay = dataModel.getMasterInSuit(suitIndex);
						} else {
							cardToPlay = dataModel.getCardCurrentPlayergetLowestInSuit(suitIndex);
						}

						return cardToPlay;

					} else {
						continue;
					}
				}
				
			}
		}
		
		return null;
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
						
						
						if(dataModel.getNumCardsOfSuitInCurrentPlayerHand(leaderSuitIndex) >= 2) {
							System.out.println("Get Second Highest in suit");
							cardToPlay = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(leaderSuitIndex);
											
						} else {
							cardToPlay = dataModel.getCardCurrentPlayergetLowestInSuit(leaderSuitIndex);
							
						}
					}
					
				} else {
					System.err.println("ERROR: this condition shouldn't happen in get ai 2nd throw");
					System.exit(1);
				}

			} else {
				cardToPlay = dataModel.getCardCurrentPlayergetLowestInSuit(leaderSuitIndex);
			}
			
			
			//No following suit:
		} else {
			
			//no trumping: play off:
			if(leaderSuitIndex== Constants.SPADE || dataModel.isVoid(0, Constants.SPADE)) {
				cardToPlay = dataModel.getLowOffSuitCardToPlayElseLowestSpade();
				
				//Must play trump:
			} else if(dataModel.currentPlayerMustTrump()) {
				cardToPlay = dataModel.getCardCurrentPlayergetLowestInSuit(Constants.SPADE);
				
				//Option to trump:
			} else {
				if(dataModel.isMasterCard(leaderCard) && dataModel.getNumCardsPlayedForSuit(Constants.SPADE) < 2 * Constants.NUM_PLAYERS) {
					cardToPlay = dataModel.getCardCurrentPlayergetLowestInSuit(Constants.SPADE);

				} else if(dataModel.isMasterCard(leaderCard) && (dataModel.isVoid(2, Constants.SPADE) || dataModel.isVoid(2, leaderSuitIndex) == false)) {
					cardToPlay = dataModel.getCardCurrentPlayergetLowestInSuit(Constants.SPADE);

				//I guess we should trump if we don't have much spade?
				} else if((dataModel.isVoid(2, Constants.SPADE) || dataModel.isVoid(2, leaderSuitIndex) == false) && (13 - dataModel.getNumCardsPlayedForSuit(Constants.SPADE))/4 >= dataModel.getNumberOfCardsOneSuit(Constants.SPADE)) {
					cardToPlay = dataModel.getCardCurrentPlayergetLowestInSuit(Constants.SPADE);

					
					if(dataModel.isEffectivelyMasterCardForPlayer(Constants.CURRENT_AGENT_INDEX, cardToPlay)) {
						cardToPlay = dataModel.getLowOffSuitCardToPlayElseLowestSpade();
					}
					
				} else {
					cardToPlay = dataModel.getLowOffSuitCardToPlayElseLowestSpade();
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
						cardToPlay = dataModel.getLowOffSuitCardToPlayElseLowestSpade();
						
					}
				} else {
					
					//if your partner played master and 2nd thrower didn't trump over
					if(dataModel.leaderPlayedMaster()) {
						//PLAY OFF because leaderPlayedMaster
						cardToPlay = dataModel.getLowOffSuitCardToPlayElseLowestSpade();
					} else {
						//TRUMP
						
						//TODO: what if leader(partner) plays a higher card than 2nd throw that isn't master, but 4th could trump too... 
						//... I don't even know. That gets into weird logic
						cardToPlay = dataModel.getCardCurrentPlayergetLowestInSuit(Constants.SPADE);
					}
				}
				
				//No Spade, so play off:
			} else {
				cardToPlay = dataModel.getLowOffSuitCardToPlayElseLowestSpade();
			}
		
		//FOLLOW SUIT:
		} else {
			
			//If leader got TRUMPED by 2nd player:
			if(dataModel.getSuitOfLeaderThrow() != Constants.SPADE && dataModel.getSuitOfSecondThrow() == Constants.SPADE) {
					cardToPlay = dataModel.getCardCurrentPlayergetLowestInSuit(leaderSuitIndex);
			
			//FIGHT WITHIN SUIT:
			} else {
				
				//If lead is winning
				if(dataModel.cardAGreaterThanCardBGivenLeadCard(dataModel.getCardLeaderThrow(), dataModel.getCardSecondThrow())) {
					
					if(dataModel.hasNonLeadCardInHandThatCanDecreaseChanceof4thThrowerWinning()) {
						cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(dataModel.getSuitOfLeaderThrow());
						
					} else {
						cardToPlay = dataModel.getCardCurrentPlayergetLowestInSuit(dataModel.getSuitOfLeaderThrow());
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
						if(dataModel.isVoid(1, dataModel.getSuitOfLeaderThrow())) {
							
							//Play highest to force 4th to play even higher... or stop 4th thrower from winning:
							cardToPlay = dataModel.getCardInHandClosestOverSameSuit(dataModel.getCardSecondThrow());
						
						} else {
							//play barely over 2nd thrower to force 4th thrower to trump for the win:
							cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(dataModel.getSuitOfLeaderThrow());
						}
					} else {
						
						cardToPlay = dataModel.getCardCurrentPlayergetLowestInSuit(dataModel.getSuitOfLeaderThrow());
					}
				}
			}
		}
				
	
		return cardToPlay;
	}
	
	public static String AIFourthThrow(DataModel dataModel) {
		String cardToPlay = null;
		
		if(dataModel.isPartnerWinningFight()) {
			cardToPlay = dataModel.getJunkiestCardToFollowLead();

		} else if(dataModel.throwerHasCardToBeatCurrentWinner()) {
			cardToPlay = dataModel.getCardClosestOverCurrentWinner();
			
		} else {
			cardToPlay = dataModel.getJunkiestCardToFollowLead();
			
		}

		return cardToPlay;
	}
	//END AIS for non-nellow bid games
	
}

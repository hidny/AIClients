package mellow.ai.situationHandlers;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.cardDataModels.handIndicators.NonMellowBidHandIndicators;
import mellow.ai.situationHandlers.objects.CardAndValue;
import mellow.ai.situationHandlers.wildSituations.SecretlyBurnt;
import mellow.cardUtils.CardStringFunctions;
import mellow.cardUtils.DebugFunctions;

public class SingleActiveMellowPlayer {

	public static String handleThrowAsSingleActiveMellowBidder(DataModel dataModel) {

		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "2S JD 6D 2D ")) {
			System.out.println("DEBUG");
		}
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
			
			//exception when you know you're toast:
			if((dataModel.currentPlayerHasMasterInSuit(Constants.SPADE)
					&& dataModel.cardAGreaterThanCardBGivenLeadCard(
							dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE),
							dataModel.getCurrentFightWinningCardBeforeAIPlays()))
				||
				dataModel.currentPlayerGetNumMasterSpadeInHand() > 1
			  )
				 {
				//TODO
				cardToPlay = SecretlyBurnt.handleBeingSecretlyBurnt(dataModel);
				if(cardToPlay != null) {
					return cardToPlay;
				}
			}
			
			cardToPlay = AIMellowFollow(dataModel);
		}
		
		if(cardToPlay != null) {
			System.out.println("AI decided on " + cardToPlay);
		}
		
		return cardToPlay;
	}
	
	private static String AIMellowLead(DataModel dataModel) {
		
		
		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "JS 6S 3S 3H 2H QC JC TC 9C 6C JD 6D 2D ")) {
			System.out.println("Debug");
		}
		
		String ret = "";
		int numSpadesInHand = dataModel.getNumberOfCardsOneSuit(Constants.SPADE);
		if(numSpadesInHand > 0
				&&  
			DataModel.getRankIndex(dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE)) < DataModel.KING
				&&
			(numSpadesInHand == 1
				||	DataModel.getRankIndex(dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE)) <
					DataModel.JACK
				|| dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) >= 4)
		) {

				ret = dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
			
		} else {
			//TODO: over simplified, but whatever:
			//ret = dataModel.getLowOffSuitCardToLeadInSafeSuit();
			ret = getOffsuitMellowLead(dataModel);
		}
		
		//Try to lead with a 4 or a 5:
		int suitIndexRet = CardStringFunctions.getIndexOfSuit(ret);
		if(suitIndexRet != Constants.SPADE
				&& DataModel.getRankIndex(ret) <= DataModel.RANK_THREE
				&& dataModel.getNumberOfCardsOneSuit(suitIndexRet) > 1) {
			
			if(dataModel.hasCard(dataModel.getCardString(DataModel.RANK_FIVE, suitIndexRet))) {
				ret = dataModel.getCardString(DataModel.RANK_FIVE, suitIndexRet);
			} else if(dataModel.hasCard(dataModel.getCardString(DataModel.RANK_FOUR, suitIndexRet))) {
				ret = dataModel.getCardString(DataModel.RANK_FOUR, suitIndexRet);
			}
			
		}
		
		return ret;
	}
	
	private static String AIMellowFollow(DataModel dataModel) {

		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "JS TS 5S 2S KH QH 3H KC 7C 6C 5C 4C 9D ")) {
			System.out.println("Debug");
		}
		
		int leaderSuitIndex = dataModel.getSuitOfLeaderThrow();
		
		String cardToPlay = "";
		
		if(dataModel.currentAgentHasSuit(leaderSuitIndex)) {
			//Follow suit:
			
			//if no one has trump:
			String currentFightWinner = dataModel.getCurrentFightWinningCardBeforeAIPlays();
			
			if(CardStringFunctions.getIndexOfSuit(currentFightWinner) == leaderSuitIndex) {
				
				int throwIndex = dataModel.getCardsPlayedThisRound() % Constants.NUM_PLAYERS;
				
				//Just play high spade case:
				if(throwIndex == 1
						&& leaderSuitIndex == Constants.SPADE
						&& dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) >= 3
						&& (
							(dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE)
							+ dataModel.getNumberOfCardsOneSuit(Constants.SPADE)
							> 10
							&& dataModel.getBid(Constants.LEFT_PLAYER_INDEX) < dataModel.getBid(Constants.CURRENT_PARTNER_INDEX)
							)
							||
							(NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, Constants.SPADE)
							&&
							dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) >= 2)
							||
							(
							dataModel.getBid(Constants.RIGHT_PLAYER_INDEX) == 0
							&& dataModel.getNumTricks(Constants.RIGHT_PLAYER_INDEX) == 0
							&& dataModel.getNumCardsInPlayOverCardSameSuit(currentFightWinner) >= 2
							)
						 )
						&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)
						&& dataModel.cardAGreaterThanCardBGivenLeadCard(
								DataModel.getCardString(
										dataModel.signalHandler.getMaxCardRankSignal(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE),
										Constants.SPADE),
								dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE)
							)
						) {
					
					
							
					cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
				
					if(dataModel.getBid(Constants.RIGHT_PLAYER_INDEX) == 0
							&& dataModel.getNumTricks(Constants.RIGHT_PLAYER_INDEX) == 0
							&& dataModel.getNumCardsInPlayOverCardSameSuit(currentFightWinner) >= 2
							&& DataModel.getRankIndex(currentFightWinner) < DataModel.RANK_SEVEN
							&& dataModel.couldPlayCardInHandUnderCardInSameSuit("TS")
							) {
						//Hard-code to fix DOU-49...
						//Will need to make it more general later.
						cardToPlay = SeatedLeftOfOpponentMellow.getHighestPartOfGroup(dataModel, 
								dataModel.getCardInHandClosestUnderSameSuit("TS"));
					}
					
				} else if(dataModel.couldPlayCardInHandUnderCardInSameSuit(currentFightWinner)) {

					
					//Play just below winning card if possible
					cardToPlay = dataModel.getCardInHandClosestUnderSameSuit(currentFightWinner);
					
					//TODO: check for the case where you'd rather play your 5 over the 4 even if you have a 2.
					
					
					if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "3S 7H 7C 6C 3C QD 9D 6D ")) {
						System.out.println("DEBUG");
					}
					
					/*if(throwIndex == 1
							&& dataModel.getCardInHandClosestOverSameSuit(currentFightWinner) != null
							&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(
									dataModel.getCardInHandClosestOverSameSuit(currentFightWinner))
								<= 1) {
						System.out.println("(DECISION TO GO OVER LEAD TIME)");
					}*/
					
					//Special cases where mellow plays over lead
					
					if(throwIndex == 1
							&& dataModel.getCardInHandClosestOverSameSuit(currentFightWinner) != null
							&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(
									dataModel.getCardInHandClosestOverSameSuit(currentFightWinner))
								<= 1
							&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(leaderSuitIndex) >= 4
							&& ! dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, leaderSuitIndex)
							&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, leaderSuitIndex)
							&& dataModel.getNumCardsInHandUnderCardSameSuit(currentFightWinner) <= 2
							&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE) >= 6
							&& dataModel.getNumberOfCardsPlayerPlayedInSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE) <= 2
							) {

						//Case where attack leads 3 and mellow bidder plays 5 instead of 2:
						
						//TODO: maybe if Num Cards Hidden for lead suit is high enough, forgive partner's lack of spade...
						cardToPlay = SeatedLeftOfOpponentMellow.getHighestPartOfGroup(dataModel, 
								dataModel.getCardInHandClosestOverSameSuit(currentFightWinner));
					}
					//
					//END special cases where mellows plays over lead
				
				} else if(dataModel.getCardsPlayedThisRound() % Constants.NUM_PLAYERS == 1
						&& CardStringFunctions.getIndexOfSuit(currentFightWinner) == Constants.SPADE
						&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)
						&& 3 * dataModel.getNumberOfCardsOneSuit(Constants.SPADE) > dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE)) {
					
					//Go for it!
					//Hope partner covers you:
					cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
					
				} else {
					//Play slightly above winning card and hope you'll get covered
					cardToPlay = dataModel.getCardInHandClosestOverSameSuit(currentFightWinner);
					
					//Exception when partner is weak (For: 3-3810)
					if(dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) == 1 
							&& throwIndex == 1
							&& ! dataModel.isVoid(Constants.CURRENT_AGENT_INDEX, leaderSuitIndex)
							&& ! dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, leaderSuitIndex)
							&& (NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, leaderSuitIndex)
							|| NonMellowBidHandIndicators.hasQEquivNoAorK(dataModel, leaderSuitIndex)
							)
							&& dataModel.getCardCurrentPlayerGetHighestInSuit(leaderSuitIndex).equals(
									SeatedLeftOfOpponentMellow.getHighestPartOfGroup(dataModel,cardToPlay)
								)
						)
							{
						cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(leaderSuitIndex);
					
					//Exception: Play 2nd or 3rd highest to hide lower card if it's safe enough:
						//Maybe this is only good to mix into play, but not to do in general? I don't know.
					} else if(throwIndex == 1
							&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(cardToPlay) == 1
							&& ! SeatedLeftOfOpponentMellow.getHighestPartOfGroup(dataModel, cardToPlay).equals(cardToPlay)
							&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(cardToPlay) > 4
							) {
						cardToPlay = SeatedLeftOfOpponentMellow.getHighestPartOfGroup(dataModel, cardToPlay);
					}
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
			
			//TODO: make playing spade just another option for what to play
			//      maybe the offsuit situation is worse than the spade situation 
			String currentFightWinner = dataModel.getCurrentFightWinningCardBeforeAIPlays();
			if(CardStringFunctions.getIndexOfSuit(currentFightWinner) == Constants.SPADE) {
				//Play spade under trump (this isn't always a good idea, but...)
				//TODO: maybe consider not throwing off spade?
				
				if(dataModel.currentPlayerOnlyHasSpade()) {
					if(dataModel.couldPlayCardInHandUnderCardInSameSuit(currentFightWinner)) {
						//Play spade under trump
						return dataModel.getCardInHandClosestUnderSameSuit(currentFightWinner);
					} else {
						return dataModel.getCardInHandClosestOverSameSuit(currentFightWinner);
					}
				} else if(dataModel.couldPlayCardInHandUnderCardInSameSuit(currentFightWinner)){
					
					
					String cardToPlay2 = dataModel.getCardInHandClosestUnderSameSuit(currentFightWinner);
					
					if(3 * dataModel.getNumberOfCardsOneSuit(Constants.SPADE) < 
							dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE)) {

						return cardToPlay2;
					
					} else if(dataModel.couldPlayCardInHandOverCardInSameSuit(currentFightWinner)) {
						
						String nextHighestSpade = dataModel.getCardInHandClosestOverSameSuit(cardToPlay2);
						
						String lowestSpade = dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
						
						if(cardToPlay2.equals(lowestSpade)
								&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(cardToPlay2)
								 - dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(nextHighestSpade)
								 >= 2
							//	&& 
							//	dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(cardToPlay2)
							//	- dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(cardToPlay2)
							//	 <= 2
								  ) {
							//At this point, it's probably more useful to keep the spade...
							
							//
						} else {
							return cardToPlay2;
						}
						
						//if(dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(cardToPlay2))
						
						
					} else {
						return cardToPlay2;
					}
							
					
				}
			}
			//END TODO
			
			
			//Algo that tries to throw off least desirable offsuit for mellow player
			//NOTE: this is BARELY, better than dataModel.getHighestOffSuitCardToLead(). LOL
			cardToPlay = getBestOffSuitCardToThrowOffAsMellowPlayer(dataModel);
		}
		
		return cardToPlay;
	
	}

	//Find the suit the mellow player wants to throw-off most:
	public static String getBestOffSuitCardToThrowOffAsMellowPlayer(DataModel dataModel) {

		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "QH 7H 3H TD 9D 7D 2D ")) {
			System.out.println("Debug");
		}
		
		int otherMellowIndex = -1;
		if(dataModel.getNumStillActiveMellow() > 1) {
			for(int i=0; i<Constants.NUM_PLAYERS; i++) {
				
				if(i != Constants.CURRENT_AGENT_INDEX && dataModel.getBid(i) == 0) {
					otherMellowIndex = i;
				}
			}
		}
		
		int NO_SUIT_FOUND = -1;
		int chosenSuit = NO_SUIT_FOUND;
		double bestScore = -1.0;
		
		for(int i=0; i<Constants.NUM_SUITS; i++) {
			if(i == Constants.SPADE) {
				continue;

			} else if(dataModel.currentAgentHasSuit(i) == false) {
				continue;
			}
			if(chosenSuit == -1) {
				chosenSuit = i;
			}
			
			double tmpScore = getWillingnessToThrowOffSuitAsMellowPlayer3(dataModel, i);
			
			if(dataModel.getNumStillActiveMellow() > 1) {
				
				//Double mellow case where some suits are given the benefit of the doubt:
				if(dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(dataModel.getCardCurrentPlayerGetHighestInSuit(i)) > 3) {
					int rankUnderMax = DataModel.getRankIndex(dataModel.getCardCurrentPlayerGetHighestInSuit(i)) - 1;
					String card = DataModel.getCardString(rankUnderMax, i);
					if(dataModel.signalHandler.mellowBidderSignalledNoCardOverCardSameSuit(card, otherMellowIndex)) {
						
						if(dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(dataModel.getCardCurrentPlayerGetHighestInSuit(i)) < 3) {
							//Adding + 4.0 worked for all test cases, but maybe it was a fluke?
							tmpScore += 4.0;
						}
					}
					
				}
				//End double mellow case.
			}
			//System.out.println("Willingness: " + tmpScore);
			
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
	
	//TODO
	//public static double getWillingnessToThrowOffSpadeSuitAsMellow(DataModel dataModel) {
		
	//}
		
		
		//TODO: clean it up and make it look less like trial and error...
		public static double getWillingnessToThrowOffSuitAsMellowPlayer3(DataModel dataModel, int suit) {
			
			double origNum= getRiskRating3(dataModel, suit, 0) - 0.9 * getRiskRating3(dataModel, suit, 1);
			
			double adjustedNum = origNum;
			
			//A not so important adjustment to get rid of safe Aces...
			// Feel free to get rid of it if it doesn't work.
			if(origNum == 0.0
					&&
					dataModel.getNumberOfCardsOneSuit(suit) >= 4) {
				adjustedNum += 0.001 * dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(
						dataModel.getCardCurrentPlayerGetHighestInSuit(suit)
						);
			}
			
			return adjustedNum;
		}
	
		//TODO: clean it up and make it look less like trial and error...
		// At least separate by value of i...
		
		public static double getRiskRating3(DataModel dataModel, int suit, int numTopCardsToIgnore) {

			if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "4H 3H KC 9C 5C 2C 8D 6D ")) {
				System.out.println("Debug");
			}
			int numOfSuitPlayerHas = dataModel.getNumCardsOfSuitInCurrentPlayerHand(suit);
			
			numOfSuitPlayerHas -= numTopCardsToIgnore;
			
			int numOfSuitOtherPlayersHave = dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suit);
			
			
			if(numOfSuitPlayerHas <= 0) {
				return 0.0;
			}
			
			double ret =0.0;
			
			String cardToConsider = "";
			
			for(int i=0; i<numOfSuitPlayerHas && i<4; i++) {
				
				//Only worry about high cards that can be forced to be played:
				int indexToUse = Math.min(i, numOfSuitOtherPlayersHave - 1);
				
				
				if(indexToUse==0) {
					cardToConsider = dataModel.getCardCurrentPlayerGetLowestInSuit(suit);
				} else if(indexToUse == 1) {
					cardToConsider = dataModel.getCardCurrentPlayergetSecondLowestInSuit(suit);
				} else if(indexToUse == 2) {
					cardToConsider = dataModel.getCardCurrentPlayergetThirdLowestInSuit(suit);
				} else if(indexToUse == 3) {
					cardToConsider = dataModel.getCardCurrentPlayergetFourthLowestInSuit(suit);
					
					
				} else {
					break;
				}
				
				
				int numOver = dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(cardToConsider);
				int numUnder = dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(cardToConsider);
				
				
				//TODO: also consider 2nd lowest later. (I'll wait until there's practical examples)
				if(i == 0) {
					
					//Check if partner can't guard you:
					if( ! dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, suit)
							&& numUnder >= 2
							&& dataModel.signalHandler.getMaxCardRankSignalForMellowProtector(Constants.CURRENT_PARTNER_INDEX, suit)
							 <=  DataModel.getRankIndex(cardToConsider)) {
						
						if(numUnder >= 3
								&& (!dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, suit)
										|| !dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, suit))) {
							
							ret +=10;
							
						} else if(numUnder == 2
								&& (!dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, suit)
										|| !dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, suit))
								&&
									dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suit) <= 5
								)
								{
							
							ret +=10;
							
						}
					}
				}
				//END Check if partner can't guard you:
				
			//Lowest card logic:
				if(i==0 && numUnder >=2) {
					ret += (numUnder - 1.5);
					
				//I made this up!
				} else if(i==0){
					//I made up 7.0
					
					//TODO: AHH!
					//This applies for i=1 to 10
					//And doesn't really make sense...
					ret += 7.0 * getChancesOfBurningInSuit1stRound(dataModel, suit, numTopCardsToIgnore);
				}
	
			//Second Lowest card logic:
				if(i == 1 && numUnder >=3) {
					ret += (1.0/3.0) * (numUnder - 2.5);
					
				}
				
				if(i == 1 && numUnder >=5) {
					ret += (2.0/3.0) * (numUnder - 4.5);
				
				//I made this up!
				}
				if( i == 1) {
					//I made this up too!

					//TODO AHH!
					//THIS
					//And doesn't really make sense...
					ret += 5.0 * getChancesOfBurningInSuitIthRound(dataModel, suit, i, numTopCardsToIgnore);
				}
				
	
			//Third Lowest card logic:
				if(i == 2 && numUnder >=3) {
					ret += (1.0/6.0) * (numUnder - 2.5);
					
				}
				
				if(i == 2 && numUnder >=8) {
					ret += (5.0/6.0) * (numUnder - 7.5);
					
				}
				
				if(i == 2 && numUnder >= 1 && numOver  < 3 ) {
					ret += (3.0 - numOver);
				}
				
				
			//Fourth lowest card logic:
				if(i == 3 && numUnder >=5) {
					ret += (1.0/9.0) * (numUnder - 4.5);
					
				}
				
				//System.out.println("i = " + i + "  and ret = " + ret);
				
				
			}
			
			
			
			
			return ret;
		}
		

		public static double getChancesOfBurningInSuitIthRound(DataModel dataModel, int suit, int roundNum, int numTopCardsToIgnore) {
			
			double oddsLosingIthRound = 0.0;
			String cardToConsider = "";
			
	
			int numOfSuitPlayerHas = dataModel.getNumCardsOfSuitInCurrentPlayerHand(suit);
			
			numOfSuitPlayerHas -= numTopCardsToIgnore;
			
			if(numOfSuitPlayerHas - roundNum <= 0) {
				return 0.0;
			}
			
			cardToConsider = dataModel.getCardCurrentPlayerGetIthLowestInSuit(roundNum, suit);
			
			int numOver = dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(cardToConsider);
			int numUnder = dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(cardToConsider);
			
	
			//I don't know man...
			//All models are wrong, but some are useful...
			
			//This model makes a lot of bad assumptions like the 0.5
			// and the fact that cards are just as likely to be in partner's hands as opponents...
			
			double oddsPartnerHasNoSpade = 0.5;
			if(dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)) {
				oddsPartnerHasNoSpade = 1.0;
			}
			
			if(numUnder <= roundNum) {
				oddsLosingIthRound = 0.0;
			} else if(numUnder == roundNum + 1) {
				
				if(numOver == 0) {
					oddsLosingIthRound = 1.0 * oddsPartnerHasNoSpade;
				} else {
					//rough approx... but whatever
					//The real number should be less, but this is low enough...
					
					//Real:
					//This answers question if partner doesn't have the suit:
					
					//Odds the opponent has all of 1 suit and partner can't trump...
					double oddsNeedingToBeTrumped = 2 * Math.pow(1.0/3.0, numOver + numUnder);
					oddsLosingIthRound = oddsPartnerHasNoSpade * oddsNeedingToBeTrumped;
				}
				
			
			} else if(numUnder == roundNum + 2) {
				
				//case 1: partner and 1 opponent void:
				double calc1 = Math.pow(1.0/3.0, numOver + numUnder);
				
				//case 2: partner void and both opponents not void:
				double calc2 = Math.pow(2.0/3.0, numOver);
				calc2 *= (2.0/3.0) * (1.0/3.0);
				
				//case 3: partner not void one opponent void and and other has rest
				double calc3 = Math.pow(1.0/3.0, numOver);
				calc3 *= (2.0/3.0) * (1.0/3.0);
				
				double total = calc1 + calc2 + calc3;
				
				oddsLosingIthRound = total * oddsPartnerHasNoSpade;
			
			} else if(numUnder >= roundNum + 3) {
	
				//case 1: partner and 1 opponent void:
				double calc1 = Math.pow(1.0/3.0, numOver + numUnder);
	
				//case 2: partner void and both opponents not void:
				double calc2 = Math.pow(2.0/3.0, numOver + numUnder);
				calc2 *= (1.0 - Math.pow(1.0/2.0, numUnder - 1));
				
				//case 3: partner not void one opponent void and and other has rest
				double calc3 = Math.pow(1.0/3.0, numOver);
				calc3 *= Math.pow(2.0/3.0, numUnder) * (1.0 - Math.pow(1.0/2.0, numUnder-1));
				
				//case 4: no one void all could play under:
				double calc4 = Math.pow(2.0/3.0, numOver);
																	//Inclusion-exclusion principal:
				calc4 *= (1.0 - 3 * Math.pow(2.0/3.0, numUnder)  + 3 * Math.pow(1.0/3.0, numUnder));
				
	
				double total = calc1 + calc2 + calc3 + calc4;
				
				oddsLosingIthRound = total * oddsPartnerHasNoSpade;
			
				
			}
			
			
			return oddsLosingIthRound;
		}
		
		
		public static double getChancesOfBurningInSuit1stRound(DataModel dataModel, int suit, int numTopCardsToIgnore) {
			
			double oddsLosingFirstRound = 0.0;
			String cardToConsider = "";
			
	
			int numOfSuitPlayerHas = dataModel.getNumCardsOfSuitInCurrentPlayerHand(suit);
			
			numOfSuitPlayerHas -= numTopCardsToIgnore;
			
			if(numOfSuitPlayerHas <= 0) {
				return 0.0;
			}
			
			cardToConsider = dataModel.getCardCurrentPlayerGetLowestInSuit(suit);
			
			int numOver = dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(cardToConsider);
			int numUnder = dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(cardToConsider);
			
	
			//I don't know man...
			//All models are wrong, but some are useful...
			
			//This model makes a lot of bad assumptions like the 0.5
			// and the fact that cards are just as likely to be in partner's hands as opponents...
			
			double oddsPartnerHasNoSpade = 0.5;
			if(dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)) {
				oddsPartnerHasNoSpade = 1.0;
			}
			
			if(numUnder == 0) {
				oddsLosingFirstRound = 0.0;
			} else if(numUnder == 1) {
				
				if(numOver == 0) {
					oddsLosingFirstRound = 1.0 * oddsPartnerHasNoSpade;
				} else {
					//rough approx... but whatever
					//The real number should be less, but this is low enough...
					
					//Real:
					//This answers question if partner doesn't have the suit:
					
					//Odds the opponent has all of 1 suit and partner can't trump...
					double oddsNeedingToBeTrumped = 2 * Math.pow(1.0/3.0, numOver + numUnder);
					oddsLosingFirstRound = oddsPartnerHasNoSpade * oddsNeedingToBeTrumped;
				}
				
			
			} else if(numUnder == 2) {
				
				//case 1: partner and 1 opponent void:
				double calc1 = Math.pow(1.0/3.0, numOver + numUnder);
				
				//case 2: partner void and both opponents not void:
				double calc2 = Math.pow(2.0/3.0, numOver);
				calc2 *= (2.0/3.0) * (1.0/3.0);
				
				//case 3: partner not void one opponent void and and other has rest
				double calc3 = Math.pow(1.0/3.0, numOver);
				calc3 *= (2.0/3.0) * (1.0/3.0);
				
				double total = calc1 + calc2 + calc3;
				
				oddsLosingFirstRound = total * oddsPartnerHasNoSpade;
			
			} else if(numUnder >= 3) {
	
				//case 1: partner and 1 opponent void:
				double calc1 = Math.pow(1.0/3.0, numOver + numUnder);
	
				//case 2: partner void and both opponents not void:
				double calc2 = Math.pow(2.0/3.0, numOver + numUnder);
				calc2 *= (1.0 - Math.pow(1.0/2.0, numUnder - 1));
				
				//case 3: partner not void one opponent void and and other has rest
				double calc3 = Math.pow(1.0/3.0, numOver);
				calc3 *= Math.pow(2.0/3.0, numUnder) * (1.0 - Math.pow(1.0/2.0, numUnder-1));
				
				//case 4: no one void all could play under:
				double calc4 = Math.pow(2.0/3.0, numOver);
																	//Inclusion-exclusion principal:
				calc4 *= (1.0 - 3 * Math.pow(2.0/3.0, numUnder)  + 3 * Math.pow(1.0/3.0, numUnder));
				
	
				double total = calc1 + calc2 + calc3 + calc4;
				
				oddsLosingFirstRound = total * oddsPartnerHasNoSpade;
			
				
			}
			
			
			return oddsLosingFirstRound;
		}
		
		public static String getOffsuitMellowLead(DataModel dataModel) {
			
			String bestCardToPlay = null;
			double currentBestScore = -1000000.0;
			
			//TODO: play random card if all spade or something...
			
			for(int suitIndex = 0; suitIndex< Constants.NUM_SUITS; suitIndex++) {
				
				if(suitIndex == Constants.SPADE) {
					continue;	
				}
				
				int numCardsOfSuitInHand = dataModel.getNumCardsOfSuitInCurrentPlayerHand(suitIndex);
				
				//Can't play a suit you don't have:
				if(numCardsOfSuitInHand == 0) {
					continue;
				}
				
				CardAndValue cardAndValueRet = GetValueofMellowBidLeadingOffsuit(dataModel, suitIndex);
				
				
				if(cardAndValueRet.getValue() > currentBestScore) {
					
					currentBestScore = cardAndValueRet.getValue();
					bestCardToPlay = cardAndValueRet.getCard();
					
				} else if(cardAndValueRet.getValue() == currentBestScore
						&& (DataModel.getRankIndex(cardAndValueRet.getCard()) > DataModel.getRankIndex(bestCardToPlay)
								|| DataModel.getRankIndex(cardAndValueRet.getCard()) == DataModel.KING
								)
						) {
					
					currentBestScore = cardAndValueRet.getValue();
					bestCardToPlay = cardAndValueRet.getCard();
				}
				
			}
			
			if(bestCardToPlay != null) {
				return bestCardToPlay;
			} else {
				return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
			}
			
		}
		
		public static CardAndValue GetValueofMellowBidLeadingOffsuit(DataModel dataModel, int suitIndex) {
			
			if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "JS 5S TD 8D 6D 4D 2D JC 8C 6C TH 4H 3H")) {
				System.out.println("DEBUG");
			}
			String lowestCard = dataModel.getCardCurrentPlayerGetLowestInSuit(suitIndex);
			int numCards = dataModel.getNumCardsOfSuitInCurrentPlayerHand(suitIndex);
			
			double value = 0.0;
			value -= 10 * Math.min(numCards, 4);
			
			if(numCards > 0 ) {
				if(DataModel.getRankIndex(lowestCard) > DataModel.RANK_FIVE) {
					value -= 30.0 * (DataModel.getRankIndex(lowestCard) - DataModel.RANK_FIVE);
				}
			} else {
				
				System.err.println("WARNING: Checking value of playing card of suit when mellow has no card of suit.");
				return new CardAndValue( dataModel.getLowOffSuitCardToLeadInSafeSuit(), -1000000.0);
			}
			
			if(numCards > 1) {
				
				String secondLowest = dataModel.getCardCurrentPlayergetSecondLowestInSuit(suitIndex);
				
				if(DataModel.getRankIndex(secondLowest) > DataModel.RANK_NINE) {
					
					value -= 20 * (DataModel.getRankIndex(secondLowest) - DataModel.RANK_NINE);
				}
			}
			
			
			if(numCards > 2) {
				
				String thirdLowest = dataModel.getCardCurrentPlayergetThirdLowestInSuit(suitIndex);
				
				if(DataModel.getRankIndex(thirdLowest) > DataModel.RANK_NINE) {
					
					value -= 20 * (DataModel.getRankIndex(thirdLowest) - DataModel.RANK_NINE);
				}
			}
			
			if(numCards > 3) {
				
				String fourthLowest = dataModel.getCardCurrentPlayergetFourthLowestInSuit(suitIndex);
				
				if(DataModel.getRankIndex(fourthLowest) >= DataModel.KING) {
					
					value -= 20 * (DataModel.getRankIndex(fourthLowest) - DataModel.KING);
				}
			}
			
			return new CardAndValue(lowestCard, value);
			
		}
}

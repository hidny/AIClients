package mellow.ai.situationHandlers;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.cardDataModels.handIndicators.NonMellowBidHandIndicators;
import mellow.cardUtils.CardStringFunctions;
import mellow.cardUtils.DebugFunctions;

public class PartnerSaidMellowSituation {

	//Play move to protect partner mellow
	//The optimal play here is really complicated...
	//I'll just approx by writing a simple answer that's easy to read
	
	
	public static int MELLOW_PLAYER_INDEX = 2;
	
	public static String playMoveToProtectPartnerMellow(DataModel dataModel) {
		
		//TODO: test!
		
		//Advise for refining this function:
		//TODO: Instead of making it perfect considering all factors, play test it and see when it gets into trouble
		//EX: don't worry about counting number of cards left in suit and number of spades left and blah blah blah...
		//EX2: if player needs to play unsafe card, don't worry about having no common-sense to deal with it.
		
		//TRAM (The rest are mine) logic:
		int throwIndex = dataModel.getCardsPlayedThisRound() % Constants.NUM_PLAYERS;

		//TODO: put in function
		if( (throwIndex == 0 && NoMellowBidPlaySituation.couldTRAM(dataModel))
			|| (throwIndex > 0 && NoMellowBidPlaySituation.couldPlayMasterSAndTram(dataModel))) {
			
			System.out.println("THE REST ARE MINE! (TRAM)");
			
			if(throwIndex == 0 && ! dataModel.currentAgentHasSuit(Constants.SPADE)) {
				return dataModel.getMasterCard();
			} else {
				return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
				
			}
		}
		//END TODO put in function
		
		//1st prio:
		if(dataModel.currentThrowIsLeading()) {
			
			return AIHandleLead(dataModel);
			
		} else {
		
			
			if(throwIndex == 1) {
			
				return AISecondThrow(dataModel);
			
			} else if(throwIndex == 2) {
				
				//Note: this could only happen on the 1st round where mellow player leads.
				return AIThirdThrow(dataModel);
				
			} else if(throwIndex == 3) {

				return AIFourthThrow(dataModel);
					
			} else {
				System.err.println("ERROR: unexpected branching in playMoveToProtectPartnerMellow");
				System.exit(1);
			}
			
			return NoMellowBidPlaySituation.handleNormalThrow(dataModel);
		}
	}

	public static String AIHandleLead(DataModel dataModel) {
		
		System.out.println("AILEADPROTECT)");
		int bestSuitIndexToPlay = -1;
		double valueOfBestSuitPlay = -Double.MAX_VALUE;
		
		for(int curSuit=0; curSuit<Constants.NUM_SUITS; curSuit++) {
			if(dataModel.currentAgentHasSuit(curSuit)) {
				
				double currentValueOfSuitPlay = 0.0;
				
				if(curSuit == Constants.SPADE) {
					currentValueOfSuitPlay = getValueLeadingSpade(dataModel);
				} else {
					currentValueOfSuitPlay = getValueLeadingOffsuit(dataModel, curSuit);
				}
				
				if(currentValueOfSuitPlay > valueOfBestSuitPlay) {
					
					bestSuitIndexToPlay = curSuit;
					valueOfBestSuitPlay = currentValueOfSuitPlay;
				}
				
			}
			
			
		}
		
		
		String highestCardOfSuit = dataModel.getCardCurrentPlayerGetHighestInSuit(bestSuitIndexToPlay);
		
		String cardToPlay = "";
		if(dataModel.currentPlayerHasMasterInSuit(bestSuitIndexToPlay)
			|| NonMellowBidHandIndicators.hasKEquiv(dataModel, bestSuitIndexToPlay) ) {
			
			if(CardStringFunctions.getIndexOfSuit(highestCardOfSuit) != Constants.SPADE) {
				
				//Try to confuse opponents by not playing your master card when you don't have to:
				cardToPlay = 
						getLowestCardOfGroupOfCardsOverAllSameNumCardsInOtherPlayersHandOfSuit(dataModel, highestCardOfSuit);
			
			} else {
				cardToPlay = highestCardOfSuit;
			}
		} else {
			//cardToPlay = highestCardOfSuit;
			
			if(dataModel.signalHandler.mellowPlayerSignalNoCardsOfSuit(MELLOW_PLAYER_INDEX, bestSuitIndexToPlay)) {
				cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(bestSuitIndexToPlay);
			} else {
				
				String maxRankCardMellow = dataModel.signalHandler.getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(MELLOW_PLAYER_INDEX, bestSuitIndexToPlay);
				
				if(dataModel.couldPlayCardInHandOverCardInSameSuit(maxRankCardMellow)) {
					cardToPlay = dataModel.getCardInHandClosestOverSameSuit(maxRankCardMellow);
				} else {
					cardToPlay = highestCardOfSuit;
				}
			}
		}
		
		return cardToPlay;
	}
	
	public static double getValueLeadingSpade(DataModel dataModel) {
		
		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "9S KH TH 7H 2H")) {
			System.out.println("Debug");
		}
		double ret = 0.0;
		
		//Prefer leading spade by default:
		ret = 15.0;
		
		//Factors:

		//Master
		if(dataModel.currentPlayerHasMasterInSuit(Constants.SPADE)) {
			ret += 100.0;
		}
		
		//Has Kequiv
		if(NonMellowBidHandIndicators.hasKEquiv(dataModel, Constants.SPADE)) {
			ret += 20.0;
		}
		
		//Has Qequiv
		if(NonMellowBidHandIndicators.hasQEquiv(dataModel, Constants.SPADE)) {
			ret += 20.0;
		}
		
		//Has Jequiv
		if(dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(
				dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE)) == 3) {
			ret += 20.0;
		}
		//Has Tequiv
		if(dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(
				dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE)) == 4) {
			ret += 20.0;
		}
		
		
		int numCardsInHandOfSuit = dataModel.getNumberOfCardsOneSuit(Constants.SPADE);
		
		//Check for a good backup card: (2nd highest card)
		if(numCardsInHandOfSuit >= 2) {
			String highest = dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
			
			String secondHighest = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(Constants.SPADE);
			
			int inBetweenCards = dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit(secondHighest, highest);

			//int cardsUnder = dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(secondHighest);
			
			//if(cardsUnder)
			ret += 20.0 - 5.0 * Math.min(4, inBetweenCards);
			
			
		}
		
		
		//Mellow is void or seems void
		if(dataModel.isVoid(MELLOW_PLAYER_INDEX, Constants.SPADE)
			|| dataModel.signalHandler.mellowPlayerSignalNoCardsOfSuit(MELLOW_PLAYER_INDEX, Constants.SPADE)) {
			ret += 50.0;
			
			
		//Mellow has no card over highest spade in hand::
		} else if(dataModel.signalHandler.mellowSignalledNoCardOverCardSameSuit(
				dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE)
				, MELLOW_PLAYER_INDEX)) {
			ret += 40.0;
		}
		
		
		//Don't use up last spade... unless there's no danger.
		if(dataModel.getNumCardsOfSuitInCurrentPlayerHand(Constants.SPADE) == 1) {
			
			
			//Don't play last S if it's very risky next time spades is led...
			if(dataModel.getNumberOfCardsPlayerPlayedInSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE) <= 1) {
				
				String maxRankCardSignalMellowPlayer = dataModel.signalHandler.getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE);
				
				
				if(maxRankCardSignalMellowPlayer != null
						&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(maxRankCardSignalMellowPlayer) >= 3) {
					ret -= 200.0;
				}
				
			}
			
			
			int numOffsuitDangerAndCouldTrumpToSave = 0;
			for(int s=1; s<Constants.NUM_SUITS; s++) {
				//if(dataModel.isVoid(Constants.CURRENT_AGENT_INDEX, s)) {
				//	numOffsuitVoid++;
				//}
				
				if( ! dataModel.signalHandler.mellowPlayerSignalNoCardsOfSuit(MELLOW_PLAYER_INDEX, s)) {
					String maxRankCardMellow = dataModel.signalHandler.getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(MELLOW_PLAYER_INDEX, s);
					
					if( ! dataModel.couldPlayCardInHandOverCardInSameSuit(maxRankCardMellow)
							&& DataModel.getRankIndex(maxRankCardMellow) > DataModel.RANK_THREE) {
						
						if(3 * dataModel.getNumberOfCardsOneSuit(s) < dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(s)) {
							numOffsuitDangerAndCouldTrumpToSave++;
						}
					}
				}
			}
			
			
			//If you have lots of spade, playing it isn't so bad...
			if(3 * dataModel.getNumberOfCardsOneSuit(Constants.SPADE) > dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE)
					&& dataModel.getNumberOfCardsOneSuit(Constants.SPADE) > 1) {

				ret -= 15.0;
			} else {
				if(numOffsuitDangerAndCouldTrumpToSave > 1) {
					ret -= 100.0;
					
				} else if(numOffsuitDangerAndCouldTrumpToSave == 1) {
					ret -= 50.0;
				}
			}
			
		}
		//END Don't use up last spade... unless there's no danger.
		
		return ret;
	}
	
	//public static boolean hasDangerousSuit
	
	public static double getValueLeadingOffsuit(DataModel dataModel, int currentSuitIndex) {
		double ret = 0.0;
		
		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "9S KH TH 7H 2H")) {
			System.out.println("Debug");
		}
		//Factors:
		//TODO
		
		boolean protectorHaslotsOfSpade = 
				3 * (dataModel.getNumberOfCardsOneSuit(Constants.SPADE) - 1) >= dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE);
				
		
		int numCardsInHandOfSuit = dataModel.getNumberOfCardsOneSuit(currentSuitIndex);
		
		//Master:
		if(dataModel.currentPlayerHasMasterInSuit(currentSuitIndex)) {
			ret += 20.0;
			
			if(numCardsInHandOfSuit >= 2) {
				String highest = dataModel.getCardCurrentPlayerGetHighestInSuit(currentSuitIndex);
				
				String secondHighest = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(currentSuitIndex);
				
				int inBetweenCards = dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit(secondHighest, highest);

				//int cardsUnder = dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(secondHighest);
				
				//if(cardsUnder)
				ret += 30.0 - 10.0 * inBetweenCards;
				
				
				
			} else if(! protectorHaslotsOfSpade) {
				String highest = dataModel.getCardCurrentPlayerGetHighestInSuit(currentSuitIndex);
				
				int inBetweenCards = dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(highest);
				
				//I put a -1 because the propect of the mellow player having the lowest card of suit is actually good...
				ret += 30.0 - 10.0 * (inBetweenCards - 1);

			}
			
		}
		
		//Kequiv:
		if(NonMellowBidHandIndicators.hasKEquiv(dataModel, currentSuitIndex)) {
			ret += 15.0;
			

			if(numCardsInHandOfSuit >= 2) {
				String highest = dataModel.getCardCurrentPlayerGetHighestInSuit(currentSuitIndex);
				
				String secondHighest = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(currentSuitIndex);
				
				int inBetweenCards = dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit(secondHighest, highest);
				
				ret += 20.0 - 5.0 * inBetweenCards;
				
			} else if(! protectorHaslotsOfSpade) {
				String highest = dataModel.getCardCurrentPlayerGetHighestInSuit(currentSuitIndex);
				
				int inBetweenCards = dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(highest);

				//I put a -1 because the propect of the mellow player having the lowest card of suit is actually good...
				ret += 30.0 - 10.0 * (inBetweenCards - 1);

			}
		}
		

		//Qequiv:
		if(NonMellowBidHandIndicators.hasQEquiv(dataModel, currentSuitIndex)) {
			ret += 10.0;
			
			if(numCardsInHandOfSuit >= 2) {
				String highest = dataModel.getCardCurrentPlayerGetHighestInSuit(currentSuitIndex);
				
				String secondHighest = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(currentSuitIndex);
				
				int inBetweenCards = dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit(secondHighest, highest);
				
				ret += 20.0 - 5.0 * inBetweenCards;
				
			} else if(! protectorHaslotsOfSpade) {
				String highest = dataModel.getCardCurrentPlayerGetHighestInSuit(currentSuitIndex);
				
				int inBetweenCards = dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(highest);

				//I put a -1 because the propect of the mellow player having the lowest card of suit is actually good...
				ret += 30.0 - 10.0 * (inBetweenCards - 1);

			}
			
		}

		int numOthersWithCard = dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(currentSuitIndex);
		
		//prefer leading when others have plenty of the suit...
		ret += 4.0 * numOthersWithCard;
		
		
		
		//RHS could trump.
		if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, currentSuitIndex)
				&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)
			) {
			
			ret -= 60;
			
		}
		
		boolean LHSCouldTrump = false;
		//LHS could trump. (Only really matters if mellow player can't play off)
		if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, currentSuitIndex)
				&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)
			) {
			
			LHSCouldTrump = true;
		}
		
		//Is mellow player void in suit?
		if(dataModel.isVoid(MELLOW_PLAYER_INDEX, currentSuitIndex)
				|| dataModel.signalHandler.mellowPlayerSignalNoCardsOfSuit(MELLOW_PLAYER_INDEX, currentSuitIndex)) {
				ret += 50.0;
				
		} else if(LHSCouldTrump
				&& numOthersWithCard > 3) {
			ret -= 30.0;
		}
		
		
		//Mellow is void or seems void
		if(dataModel.isVoid(MELLOW_PLAYER_INDEX, currentSuitIndex)
			|| dataModel.signalHandler.mellowPlayerSignalNoCardsOfSuit(MELLOW_PLAYER_INDEX, currentSuitIndex)) {
			ret += 35.0;
			
			
		//Mellow has no card over highest spade in hand::
		} else if(dataModel.signalHandler.mellowSignalledNoCardOverCardSameSuit(
				dataModel.getCardCurrentPlayerGetHighestInSuit(currentSuitIndex)
				, MELLOW_PLAYER_INDEX)) {
			ret += 20.0;
		
		//Mellow potentially has a card in suit:	
		} else {
		
			String cardMellowMax = dataModel.signalHandler.getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(MELLOW_PLAYER_INDEX, currentSuitIndex);
			
			
			int numCardsSuit = dataModel.getNumberOfCardsOneSuit(currentSuitIndex);
			
			//Consider 2nd highest card of suit:
			if(numCardsSuit >= 2
					&& DataModel.getRankIndex(dataModel.getCardCurrentPlayerGetSecondHighestInSuit(currentSuitIndex))
					 >= DataModel.getRankIndex(cardMellowMax) - 1
					) {
				ret += 10.0;
			}
	
			//Consider 3rd highest card of suit:
			if(numCardsSuit >= 3
					&& DataModel.getRankIndex(dataModel.getCardCurrentPlayerGetThirdHighestInSuit(currentSuitIndex))
					 >= DataModel.getRankIndex(cardMellowMax) - 2
					) {
				ret += 5.0;
				
			}
		}
		
		
		
		//Is leading suit a danger because mellow could have higher one:
		if( ! dataModel.signalHandler.mellowPlayerSignalNoCardsOfSuit(MELLOW_PLAYER_INDEX, currentSuitIndex)) {
			String maxRankCardMellow = dataModel.signalHandler.getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(MELLOW_PLAYER_INDEX, currentSuitIndex);
			
			String highestCardInHand = dataModel.getCardCurrentPlayerGetHighestInSuit(currentSuitIndex);
			
			if( DataModel.getRankIndex(maxRankCardMellow) >
			DataModel.getRankIndex(dataModel.getCardCurrentPlayerGetHighestInSuit(currentSuitIndex))) {
				ret -= 30.0;
				
				int numBetween = dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit(highestCardInHand, maxRankCardMellow);
				
				ret -= 20.0*numBetween;
			}
		}
		
		return ret;
	}
	
	public static String AISecondThrow(DataModel dataModel) {

		System.out.println("TEST PROTECTOR 2nd throw");

		int leadSuit = dataModel.getSuitOfLeaderThrow();
		
		String curStrongestCardPlayed = dataModel.getCurrentFightWinningCardBeforeAIPlays();
		
		
		if(leadSuit != Constants.SPADE
				&& dataModel.isVoid(Constants.CURRENT_AGENT_INDEX, leadSuit)) {

			
			//if 2nd thrower:
			//if void and lead suit is off-suit
			
			
			if(dataModel.isVoid(Constants.CURRENT_AGENT_INDEX, Constants.SPADE) == false) {
				
				
				if(dataModel.signalHandler.mellowSignalledNoCardOverCardSameSuit(curStrongestCardPlayed, MELLOW_PLAYER_INDEX) == false) {

					//If mellow player partner seems vulnerable based on signals: trump if possible
					//TODO: make it more sophisticated in future
					
					
					return dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
				} else {
					//no spade, so just throw off something:
					//Later try to pick best garbage card 
					

					//TODO: maybe trump anyways sometimes
					//Ex: if start with 5+S, and/or mellow is safe in S, trump anyways!
					
					return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
				}
				
				
			} else {

				//else: throw out garbage card...
					//Later try to pick best garbage card 
				
				return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
			}
			
			
			
		} else if(leadSuit == Constants.SPADE
				&& dataModel.isVoid(Constants.CURRENT_AGENT_INDEX, leadSuit)) {
			
			//There's no testcases covering this yet, so I don't want to work hard on this...
			
			//if void and lead suit is trump
			//play garbage
			
			//TODO: play garbage that could ruin ruin mellow's day
			//Think about this later...
			
			return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
		
		} else if(dataModel.isVoid(Constants.CURRENT_AGENT_INDEX, leadSuit) == false) {

			//if you need to follow suit
				
				//Get highest (I know this isn't always a good idea, but whatever man!)

			    //TODO: logic should be more sophisticated then just getting highest or lowest, but whatever...
		
				String highestProtector = dataModel.getCardCurrentPlayerGetHighestInSuit(leadSuit);
				
				if(dataModel.cardAGreaterThanCardBGivenLeadCard(highestProtector, curStrongestCardPlayed) == false) {

					return dataModel.getCardCurrentPlayerGetLowestInSuit(leadSuit);
					
				} else {
					
					if(dataModel.signalHandler.mellowSignalledNoCardBetweenTwoCards(curStrongestCardPlayed, highestProtector, MELLOW_PLAYER_INDEX)) {
						
						//TODO still want to take trick sometimes... 
						
						//TODO: dataModel.numCardsInHandGreaterThanCardSameSuit
						
						if(dataModel.signalHandler.mellowSignalledNoCardUnderCardSameSuitExceptRank2(highestProtector, MELLOW_PLAYER_INDEX)) {
							return highestProtector;
						
						//} else if
							//it's fine to play high...
							
						} else if(dataModel.couldPlayCardInHandUnderCardInSameSuit(curStrongestCardPlayed)) {
							
							
							//TODO: needs more complex logic because maybe taking the trick isn't so bad...
							
							//Maybe we can take the trick anyways
							
							
							return dataModel.getCardCurrentPlayerGetLowestInSuit(leadSuit);
							
						} else {

							if(dataModel.currentPlayerHasMasterInSuit(leadSuit)) {
								return getLowestCardOfGroupOfCardsOverAllSameNumCardsInOtherPlayersHandOfSuit(dataModel, dataModel.getCardCurrentPlayerGetHighestInSuit(leadSuit));
							} else {
								return dataModel.getCardInHandClosestOverCurrentWinner();
							}
						}
						
					} else {
						return getLowestCardOfGroupOfCardsOverAllSameNumCardsInOtherPlayersHandOfSuit(dataModel, dataModel.getCardCurrentPlayerGetHighestInSuit(leadSuit));
					}
				}
			
		
			
				//Rule: if mellow player is not vulnerable this round and made more vulnerable next round... then don't!)
				//(Example: lead K and playing A is questionable if you have a 2 or 3 to backup the A)
				
			
			
		} else {
			System.err.println("ERROR: this case in mellow protector shouldn't happen!");
			System.exit(1);
			return "aah";
		}
		
		
	}
	
	
	public static String AIThirdThrow(DataModel dataModel) {

		System.out.println("TESTING PROTECTOR 3rd throw");

		int leadSuit = dataModel.getSuitOfLeaderThrow();
		
		String currentFightWinnerCard = dataModel.getCurrentFightWinningCardBeforeAIPlays();
		
		if(currentFightWinnerCard == dataModel.getCardLeaderThrow()) {

			if(dataModel.throwerHasCardToBeatCurrentWinner()) {
				//if mellow in danger:
				
				//TODO: if 3rd and want to lead again: play master
				
				//TODO: maybe we could get away with playing master??
				//Say Mellow leads 5C and you have AC KC QC 7C... maybe play AC?

				//play just above to protect
				return dataModel.getCardInHandClosestOverCurrentWinner();
				
			} else {
				
				//Mellow might be lost, just play low :(
				if(dataModel.currentAgentHasSuit(leadSuit)) {
					return dataModel.getCardCurrentPlayerGetLowestInSuit(leadSuit);
				} else {
					return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
				}
				
			}
			
			
			
		} else {

			//Just play low: bad logic, but whatever!
			if(dataModel.currentAgentHasSuit(leadSuit)) {
				
				//Say Mellow leads 5C and next plays 6C and you have AC KC QC 7C... maybe play AC?
				return dataModel.getCardCurrentPlayerGetLowestInSuit(leadSuit);
				
			} else {
				
				//TODO: maybe have a value system for this?
				return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
			}
			
			//TODO: you might want to actually take the trick if possible...
			
			//TODO later: complex logic (Maybe start with stealing from 4th throw logic)
			
		}
	}
	
	public static String AIFourthThrow(DataModel dataModel) {

		System.out.println("TESTING PROTECTOR 4th throw");
		int leadSuit = dataModel.getSuitOfLeaderThrow();
		
		String currentFightWinnerCard = dataModel.getCurrentFightWinningCardBeforeAIPlays();
		
		if(currentFightWinnerCard == dataModel.getCardSecondThrow()) {

			//if mellow in danger:
			//play just above to protect
			if(dataModel.throwerHasCardToBeatCurrentWinner()) {
				return dataModel.getCardInHandClosestOverCurrentWinner();
				
			} else {
				
				//Mellow is lost, just play low :(
				if(dataModel.currentAgentHasSuit(leadSuit)) {
					return dataModel.getCardCurrentPlayerGetLowestInSuit(leadSuit);
				} else {
					return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
				}
				
			}
			
		} else {
			
			//Just play low: bad logic, but whatever!
			if(dataModel.currentAgentHasSuit(leadSuit)) {
				
				if(
						dataModel.cardAGreaterThanCardBGivenLeadCard
						(dataModel.getCardCurrentPlayerGetHighestInSuit(leadSuit), currentFightWinnerCard)) {

						//TODO: logic is overly simplistic, but whatever...
						String curCard = dataModel.getCardInHandClosestOverSameSuit(currentFightWinnerCard);
						String lowestCard = dataModel.getCardCurrentPlayerGetLowestInSuit(leadSuit);
						
						if(curCard.equals(lowestCard)
								|| dataModel.signalHandler.mellowSignalledNoCardBetweenTwoCards(lowestCard, curCard, leadSuit)
								) {
							
							return curCard;
						} else {
							return dataModel.getCardCurrentPlayerGetLowestInSuit(leadSuit);
						}
				} else {

					return dataModel.getCardCurrentPlayerGetLowestInSuit(leadSuit);
				}
				
				//else:
				
			} else {
				return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
			}
			
		
			//TODO later: complex logic (This is a complex situation!)
			//play to take if it doesn't endanger mellow in next round of same suit
			//or play to take if could lead nice high value card next round
			//or play to take if you need the trick...
	
			//play low if playing high endangers mellow card in next round of same suit
		}

	}
	
	public static String getLowestCardOfGroupOfCardsOverAllSameNumCardsInOtherPlayersHandOfSuit(DataModel dataModel, String cardStart) {
		
		String cardToConsider= cardStart;
		
		do {
			String nextCardToConsider = "";
			if(dataModel.couldPlayCardInHandUnderCardInSameSuit(cardToConsider)) {
				nextCardToConsider = dataModel.getCardInHandClosestUnderSameSuit(cardToConsider);
			} else {
				break;
			}
			
			
			if(dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(cardToConsider)
			== dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(nextCardToConsider) ) {
				cardToConsider = nextCardToConsider;
			
			} else {
				break;
			}
			
		} while(true);
		
		return cardToConsider;
	}
	
}

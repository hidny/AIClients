package mellow.ai.situationHandlers;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.cardDataModels.handIndicators.NonMellowBidHandIndicators;
import mellow.ai.situationHandlers.objects.CardAndValue;
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
		if( (throwIndex == 0 && NoMellowPlaySituation.couldTRAM(dataModel))
			|| (throwIndex > 0 && NoMellowPlaySituation.couldPlayMasterSAndTram(dataModel))) {
			
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
			
			return NoMellowPlaySituation.handleNormalThrow(dataModel);
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
		
		
		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "6S TH 9H 8H 6H 2H TC 7D 6D")) {
			System.out.println("Debug");
		}
		String highestCardOfSuit = dataModel.getCardCurrentPlayerGetHighestInSuit(bestSuitIndexToPlay);
		
		String cardToPlay = "";
		if(dataModel.currentPlayerHasMasterInSuit(bestSuitIndexToPlay)
			|| (NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, bestSuitIndexToPlay)
					&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(highestCardOfSuit) > 1) ) {
			
			if(CardStringFunctions.getIndexOfSuit(highestCardOfSuit) != Constants.SPADE) {
				
				//Try to confuse opponents by not playing your master card when you don't have to:
				cardToPlay = 
						getLowestCardOfGroupOfCardsOverAllSameNumCardsInOtherPlayersHandOfSuit(dataModel, highestCardOfSuit);
				
				if(dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(cardToPlay) == 0
					&& dataModel.getNumberOfCardsOneSuit(bestSuitIndexToPlay) > 1
					&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit(cardToPlay,
							dataModel.getCardCurrentPlayergetSecondLowestInSuit(bestSuitIndexToPlay)) == 0
					) {
					//But, don't make it obvious that mellow has none of suit.
					cardToPlay = dataModel.getCardCurrentPlayergetSecondLowestInSuit(bestSuitIndexToPlay);
				}
			
			} else {
				cardToPlay = highestCardOfSuit;
			}
		} else {
			//cardToPlay = highestCardOfSuit;
			
			if(dataModel.signalHandler.mellowBidderPlayerSignalNoCardsOfSuit(MELLOW_PLAYER_INDEX, bestSuitIndexToPlay)) {
				cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(bestSuitIndexToPlay);
			} else {
				
				String maxRankCardMellow = dataModel.signalHandler.getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(MELLOW_PLAYER_INDEX, bestSuitIndexToPlay);
				
				if(dataModel.couldPlayCardInHandOverCardInSameSuit(maxRankCardMellow)) {
					
					if(dataModel.getNumCardsInCurrentPlayersHandOverCardSameSuit(maxRankCardMellow) >= 3) {
						//If you have lots of choice, play the second highest...
						//Might not be a great idea for leading spade, but we'll see.
						cardToPlay = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(bestSuitIndexToPlay);
					} else {
						cardToPlay = dataModel.getCardInHandClosestOverSameSuit(maxRankCardMellow);
					}
				} else {
					cardToPlay = highestCardOfSuit;
				}
			}
		}
		
		return cardToPlay;
	}
	
	public static double getValueLeadingSpade(DataModel dataModel) {
		
		
		double ret = 0.0;
		
		//Prefer leading spade by default:
		ret = 15.0;
		
		//Factors:

		//Master
		if(dataModel.currentPlayerHasMasterInSuit(Constants.SPADE)) {
			ret += 100.0;
		}
		
		//Has Kequiv
		if(NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, Constants.SPADE)) {
			ret += 20.0;
		}
		
		//Has Qequiv
		if(NonMellowBidHandIndicators.hasQEquivNoAorK(dataModel, Constants.SPADE)) {
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
		
		/*
		//Check for a good backup card: (2nd highest card)
		if(numCardsInHandOfSuit >= 2) {
			String highest = dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
			
			String secondHighest = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(Constants.SPADE);
			
			int inBetweenCards = dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit(secondHighest, highest);

			//int cardsUnder = dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(secondHighest);
			
			//if(cardsUnder)
			ret += 20.0 - 5.0 * Math.min(4, inBetweenCards);
			
			
		}*/
		
		String cardMellowMax = dataModel.signalHandler.getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(MELLOW_PLAYER_INDEX, Constants.SPADE);
		
		int numCardsSuit = dataModel.getNumberOfCardsOneSuit(Constants.SPADE);
		
		if(cardMellowMax != null) {
			//Consider 2nd highest card of suit:
			if(numCardsSuit >= 2 ) {
				
				String secondHighest = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(Constants.SPADE);
				
				
				if(
						DataModel.getRankIndex(secondHighest)
						 >= DataModel.getRankIndex(cardMellowMax) - 1
						) {
	
					ret += 10.0;
				} else {
					//ret -= 5.0 * 
					//		(dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit(cardMellowMax, secondHighest)
					//				-1);
				}
			}
			
			//Consider 3rd highest card of suit:
			if(numCardsSuit >= 3 ) {
				String thirdHighest = dataModel.getCardCurrentPlayerGetThirdHighestInSuit(Constants.SPADE);
				
				if(DataModel.getRankIndex(thirdHighest)
					 >= DataModel.getRankIndex(cardMellowMax) - 2
					) {
				ret += 5.0;
				
				} else {
				
				
				//ret -= 4.0 * 
				//		(dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit(cardMellowMax, thirdHighest)
				//				-2);
				}
			}
		}
			
		
		//Mellow is void or seems void
		if(dataModel.isVoid(MELLOW_PLAYER_INDEX, Constants.SPADE)
			|| dataModel.signalHandler.mellowBidderPlayerSignalNoCardsOfSuit(MELLOW_PLAYER_INDEX, Constants.SPADE)) {
			ret += 50.0;
			
			
		//Mellow has no card over highest spade in hand::
		} else if(dataModel.signalHandler.mellowBidderSignalledNoCardOverCardSameSuit(
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
				
				if( ! dataModel.signalHandler.mellowBidderPlayerSignalNoCardsOfSuit(MELLOW_PLAYER_INDEX, s)) {
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
		if(NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, currentSuitIndex)) {
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
		if(NonMellowBidHandIndicators.hasQEquivNoAorK(dataModel, currentSuitIndex)) {
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
				|| dataModel.signalHandler.mellowBidderPlayerSignalNoCardsOfSuit(MELLOW_PLAYER_INDEX, currentSuitIndex)) {
				ret += 50.0;
				
		} else if(LHSCouldTrump
				&& numOthersWithCard > 3) {
			ret -= 30.0;
		}
		
		
		//Mellow is void or seems void
		if(dataModel.isVoid(MELLOW_PLAYER_INDEX, currentSuitIndex)
			|| dataModel.signalHandler.mellowBidderPlayerSignalNoCardsOfSuit(MELLOW_PLAYER_INDEX, currentSuitIndex)) {
			ret += 35.0;
			
			
		//Mellow has no card over highest card of suit in hand::
		} else if(dataModel.signalHandler.mellowBidderSignalledNoCardOverCardSameSuit(
				dataModel.getCardCurrentPlayerGetHighestInSuit(currentSuitIndex)
				, MELLOW_PLAYER_INDEX)) {
			ret += 20.0;
		
		}
		
		String cardMellowMax = dataModel.signalHandler.getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(MELLOW_PLAYER_INDEX, currentSuitIndex);
		
		int numCardsSuit = dataModel.getNumberOfCardsOneSuit(currentSuitIndex);
		
		if(cardMellowMax != null) {
			//Consider 2nd highest card of suit:
			if(numCardsSuit >= 2 ) {
				
				String secondHighest = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(currentSuitIndex);
				
				
				if(
						DataModel.getRankIndex(secondHighest)
						 >= DataModel.getRankIndex(cardMellowMax) - 1
						) {
	
					ret += 10.0;
				} else {
					ret -= 5.0 * 
							(dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit(cardMellowMax, secondHighest)
									-1);
				}
			}
			
			//Consider 3rd highest card of suit:
			if(numCardsSuit >= 3 ) {
				String thirdHighest = dataModel.getCardCurrentPlayerGetThirdHighestInSuit(currentSuitIndex);
				
				if(DataModel.getRankIndex(thirdHighest)
					 >= DataModel.getRankIndex(cardMellowMax) - 2
					) {
				ret += 5.0;
				
				} else {
				
				
				ret -= 4.0 * 
						(dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit(cardMellowMax, thirdHighest)
								-2);
				}
			}
		}
		
		
		//Is leading suit a danger because mellow could have higher one:
		if( ! dataModel.signalHandler.mellowBidderPlayerSignalNoCardsOfSuit(MELLOW_PLAYER_INDEX, currentSuitIndex)) {
			String maxRankCardMellow = dataModel.signalHandler.getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(MELLOW_PLAYER_INDEX, currentSuitIndex);
			
			String highestCardInHand = dataModel.getCardCurrentPlayerGetHighestInSuit(currentSuitIndex);
			
			if( DataModel.getRankIndex(maxRankCardMellow) >
			DataModel.getRankIndex(dataModel.getCardCurrentPlayerGetHighestInSuit(currentSuitIndex))) {
				ret -= 30.0;
				
				int numBetween = dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit(highestCardInHand, maxRankCardMellow);
				
				ret -= 20.0*numBetween;
				
				//Check if mellow could theoretically go under:
				int numBelow = dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(highestCardInHand);
				if(numBelow == 0) {
					ret -= 100.0;
				} else if(numBelow == 1) {
					ret -= 90.0;
				}
				
			}
		}
		
		return ret;
	}
	
	public static String AISecondThrow(DataModel dataModel) {

		System.out.println("TEST PROTECTOR 2nd throw");
		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "AS QS 6S KH 4H QC 5C 4C 3C KD 4D 3D")) {
			System.out.println("Debug");
		}

		int leadSuit = dataModel.getSuitOfLeaderThrow();
		
		String curStrongestCardPlayed = dataModel.getCurrentFightWinningCardBeforeAIPlays();
		
		
		if(leadSuit != Constants.SPADE
				&& dataModel.isVoid(Constants.CURRENT_AGENT_INDEX, leadSuit)) {

			
			//if 2nd thrower:
			//if void and lead suit is off-suit
			
			
			if(dataModel.isVoid(Constants.CURRENT_AGENT_INDEX, Constants.SPADE) == false) {
				
				
				if(dataModel.isVoid(MELLOW_PLAYER_INDEX, leadSuit)
						&& ! dataModel.signalHandler.mellowBidderPlayerSignalNoCardsOfSuit(MELLOW_PLAYER_INDEX, Constants.SPADE)
						&& dataModel.getNumCardsInCurrentPlayerHand() + dataModel.getNumberOfCardsPlayerPlayedInSuit(MELLOW_PLAYER_INDEX, Constants.SPADE) <= 4
						&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(
								dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE)) >= 1
						 ) {
					//Edge-case:
					//Trump high if it's near the end of the round and mellow might be stuck with spades:
					
					if(dataModel.couldPlayCardInHandOverCardInSameSuit(
							dataModel.signalHandler.getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(MELLOW_PLAYER_INDEX, Constants.SPADE))) {
						
						return dataModel.getCardInHandClosestOverSameSuit(
								dataModel.signalHandler.getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(MELLOW_PLAYER_INDEX, Constants.SPADE));
					} else {
						return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
					}
				
				} else if(dataModel.signalHandler.mellowBidderSignalledNoCardOverCardSameSuit(curStrongestCardPlayed, MELLOW_PLAYER_INDEX) == false) {

					//If mellow player partner seems vulnerable based on signals: trump if possible
					//TODO: make it more sophisticated in future
					String cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
					
					//Exception: trust mellow player if LHS is void and lead is high:
					if(dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, leadSuit)
							&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(curStrongestCardPlayed) >=3
							&& dataModel.signalHandler.getMinCardRankSignal(MELLOW_PLAYER_INDEX, leadSuit) < DataModel.getRankIndex(curStrongestCardPlayed)
							&& 3 * dataModel.getNumberOfCardsOneSuit(Constants.SPADE) < dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE)
							) {
						//Take a chance and don't trump.
						// Michael2022-3 testcase3840.txt
						return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
						
					}
					
					return cardToPlay;
					
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

			if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "KS 9S KH 5H JC TC 7C 6C 4C ")) {
				System.out.println("Debug");
			}
			//if you need to follow suit
				
				//Get highest (I know this isn't always a good idea, but whatever man!)

			    //TODO: logic should be more sophisticated then just getting highest or lowest, but whatever...
		
				String highestProtector = dataModel.getCardCurrentPlayerGetHighestInSuit(leadSuit);
				
				if(dataModel.cardAGreaterThanCardBGivenLeadCard(highestProtector, curStrongestCardPlayed) == false) {

					return dataModel.getCardCurrentPlayerGetLowestInSuit(leadSuit);
					
				} else {
					
					//Play Lower to potentially mess with dealer's mellow:
					if(dataModel.getBid(Constants.LEFT_PLAYER_INDEX) == 0
							&& dataModel.getNumTricks(Constants.LEFT_PLAYER_INDEX) == 0
							&& dataModel.getDealerIndexAtStartOfRound() == Constants.LEFT_PLAYER_INDEX
							&& dataModel.signalHandler.mellowBidderPlayerMayBeInDangerInSuit(Constants.LEFT_PLAYER_INDEX, leadSuit)
							&&
							(! dataModel.isCardPlayedInRound(dataModel.getCardString(DataModel.KING, leadSuit))
							||
							(! dataModel.isCardPlayedInRound(dataModel.getCardString(DataModel.ACE, leadSuit))
								&& leadSuit != Constants.SPADE
								)
							)
							&&
							dataModel.couldPlayCardInHandUnderCardInSameSuit(dataModel.getCardString(DataModel.KING, leadSuit))
							) {
							
							String tmp = dataModel.getCardInHandClosestUnderSameSuit(dataModel.getCardString(DataModel.KING, leadSuit));
							
							if(! dataModel.signalHandler.mellowBidderSignalledNoCardOverCardSameSuit(tmp, Constants.LEFT_PLAYER_INDEX)
									&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(tmp) > 3
									&& ! dataModel.signalHandler.mellowBidderSignalledNoCardUnderCardSameSuitExceptRank2(tmp, Constants.CURRENT_PARTNER_INDEX)) {
								return tmp;
							}
					}
					//END Play Lower to potentially mess with dealer's mellow:
					
					
					if(dataModel.signalHandler.mellowBidderSignalledNoCardBetweenTwoCards(curStrongestCardPlayed, highestProtector, MELLOW_PLAYER_INDEX)) {
						
						//TODO still want to take trick sometimes... 
						
						//TODO: dataModel.numCardsInHandGreaterThanCardSameSuit
						
						if(dataModel.signalHandler.mellowBidderSignalledNoCardUnderCardSameSuitExceptRank2(highestProtector, MELLOW_PLAYER_INDEX)) {
							return highestProtector;
						
						} else if(dataModel.getNumCardsInCurrentPlayersHandOverCardSameSuit(
									dataModel.signalHandler.getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(MELLOW_PLAYER_INDEX, leadSuit))
								 >= 2) {

							//Just play over if you got the cards:
							return highestProtector;
							
						} else if(
								 dataModel.isMasterCard(
										 dataModel.signalHandler.getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(MELLOW_PLAYER_INDEX, leadSuit)
								 )
								 ) {
							
							//TODO: maybe only do this if you have a good 2nd support card?
							
							//Play over so you can change the subject... (Mellow may have the master card.)
							//This is risky, but LHS probably doesn't know you trying to pull this stunt...
							return highestProtector;
						
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

						
						
						if(NonMellowBidHandIndicators.hasKQEquivAndNoAEquiv(dataModel, leadSuit)
								&& NonMellowBidHandIndicators.hasJEquiv(dataModel, leadSuit)
								&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(leadSuit) > 8) {
							//Exception:
							//Just play high so you can tram the suit later..
							return dataModel.getCardCurrentPlayerGetHighestInSuit(leadSuit);
						
						}
						
						//Play high to protect, but hide how high you can go if possible:
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
				//if mellow in danger (and it's the 1st round):
				
				if(  (dataModel.currentPlayerHasMasterInSuit(leadSuit)
						|| NonMellowBidHandIndicators.hasKQEquivAndNoAEquiv(dataModel, leadSuit))
						&& 
						dataModel.getNumCardsInCurrentPlayersHandOverCardSameSuit(
								dataModel.getCardLeaderThrow())
							>= 2
					) {
					//Maybe we could get away with playing master (or lowest master equiv to confuse opponents)??
					// If 3rd and want to lead again: play master
					// Example: Say Mellow leads 5C and you have AC KC JC 7C... maybe play KC?
					
					if(leadSuit != Constants.SPADE) {
						return getLowestCardOfGroupOfCardsOverAllSameNumCardsInOtherPlayersHandOfSuit(dataModel,
								dataModel.getCardCurrentPlayerGetHighestInSuit(leadSuit));
					} else {
						return dataModel.getCardCurrentPlayerGetHighestInSuit(leadSuit);
					}
					
				} else {
				
					//play just above to protect
					return dataModel.getCardInHandClosestOverCurrentWinner();
				}
				
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
				
				if(dataModel.throwerHasCardToBeatCurrentWinner()
						&& mellowhasDangerousOffsuit(dataModel)
						&& dataModel.currentPlayerGetNumMasterOfSuitInHand(leadSuit) >= 3) {
					
					//Just take it if you're afraid of what the attackers will lead
					// and you have 3 master cards
					return dataModel.getHighestCardOfSuitNotPlayed(leadSuit);
				}
				
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
		
		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "8S TS")) {
			System.out.println("debug");
		}
		
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
								|| dataModel.signalHandler.mellowBidderSignalledNoCardBetweenTwoCards(lowestCard, curCard, leadSuit)
								) {
							
							return curCard;
						} else {
							return dataModel.getCardCurrentPlayerGetLowestInSuit(leadSuit);
						}
				} else {

					return dataModel.getCardCurrentPlayerGetLowestInSuit(leadSuit);
				}
				
				//else:
			} else if(dataModel.currentPlayerOnlyHasSpade()) {
				return dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
			} else {
				
				if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "KS QS JS 8S 5S 9H 8H 5H 2H 6D ")) {
					System.out.println("DEBUG!");
					//TODO: have better logic here. (See 3282)
				}
				
				//return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
				
				return getNonLeadSuitNonSpadeToThrowOff(dataModel);
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
	
	//This function is rough, but it seems to work with the testcases...
	
	public static String getNonLeadSuitNonSpadeToThrowOff(DataModel dataModel) {
		
		
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
			
			CardAndValue cardAndValueRet = getValueOfThrowingOffsuit(dataModel, suitIndex);
			
			
			if(cardAndValueRet.getValue() > currentBestScore
					
					|| (cardAndValueRet.getValue() == currentBestScore
					&& (DataModel.getRankIndex(cardAndValueRet.getCard()) < DataModel.getRankIndex(bestCardToPlay)
					)
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
	
	
	
	public static CardAndValue getValueOfThrowingOffsuit(DataModel dataModel, int suitIndex) {
	
		String lowestCard = dataModel.getCardCurrentPlayerGetLowestInSuit(suitIndex);
		
		String maxMellow = dataModel.signalHandler.getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(MELLOW_PLAYER_INDEX, suitIndex);
					

		double value = 0.0;
		
		int numCards = dataModel.getNumberOfCardsOneSuit(suitIndex);
		
		if(maxMellow == null) {
			return new CardAndValue(lowestCard, 0.0);
		}
		
		//Factor the suit itself:
		//The every card in suit affects the final value.
		//The lower cards affect it much more than the higher cards though.
		
		//There isn't that many test cases to check this code against,
		//so this could be way off!
		
		if(numCards >=1) {
			String highestCard = dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex);
			
			value *= 0.5;
			
			if(maxMellow != null
				&&	DataModel.getRankIndex(maxMellow) > DataModel.getRankIndex(highestCard)) {
				
				int numBetween = dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit
						(maxMellow, highestCard);
			
				value += 1 + numBetween;
			} else {
				value += 0;
			}
	
		}
		
		if(numCards >=2) {
			value *= 0.5;
			
			String secondHighestCard = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(suitIndex);
			
			if(maxMellow != null
				&&	DataModel.getRankIndex(maxMellow) > DataModel.getRankIndex(dataModel.getCardCurrentPlayerGetSecondHighestInSuit(suitIndex))) {
				
				int numBetween = dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit
						(maxMellow, secondHighestCard);
			
				value += (0 + numBetween);
			} else {
				value += 0;
			}
	
		}
		
		if(numCards >=3) {
			value *= 0.5;

			String thirdHighestCard = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(suitIndex);
			
			if(maxMellow != null
				&&	DataModel.getRankIndex(maxMellow) > DataModel.getRankIndex(dataModel.getCardCurrentPlayerGetSecondHighestInSuit(suitIndex))) {
				
				int numBetween = dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit
						(maxMellow, thirdHighestCard);
			
				value += Math.max(0, (-1 + numBetween));
			} else {
				value += 0;
			}
	
		}
		
		if(numCards >=4) {
			value *= 0.5;
			String fourthHighestCard = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(suitIndex);
			
			if(maxMellow != null
				&&	DataModel.getRankIndex(maxMellow) > DataModel.getRankIndex(dataModel.getCardCurrentPlayerGetSecondHighestInSuit(suitIndex))) {
				
				int numBetween = dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit
						(maxMellow, fourthHighestCard);
			
				value += Math.max(0, (-2 + numBetween));
			} else {
				value += 0;
			}
	
		}
		
		//Factor in the trumps:
		boolean couldTrump = dataModel.getNumberOfCardsOneSuit(Constants.SPADE) > 0;
		
		int lotsOfTrumpFactor = 0;
		for(int i=1; i<Constants.NUM_PLAYERS; i++) {
			if( ! dataModel.isVoid(i, Constants.SPADE)) {
				lotsOfTrumpFactor++;
			}
		}
		boolean hasLotsOfTrump = lotsOfTrumpFactor * dataModel.getNumberOfCardsOneSuit(Constants.SPADE) > 
		dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE);

		
		if(hasLotsOfTrump) {
			double bonus = 2.0 * Math.pow(0.5, numCards-1);
			
			value += bonus;
		} else if(couldTrump) {
			double bonus = 1.0 * Math.pow(0.3, numCards-1);
			value += bonus;
		}
					
		return new CardAndValue(lowestCard, value);
	}
	
	//Idea:
	//Danger 1st
	//Danger 2nd
	//Danger 3rd
	//Danger 4th
	
	
	public static boolean mellowhasDangerousOffsuit(DataModel dataModel) {
		
		for(int s=0; s<Constants.NUM_SUITS; s++) {
			if(s == Constants.SPADE) {
				continue;
			}

			if(dataModel.getNumberOfCardsOneSuit(s) == 0) {
				continue;
			}
			
			String maxMellowCard = dataModel.signalHandler.getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(MELLOW_PLAYER_INDEX, s);
			
			if(maxMellowCard != null &&
					DataModel.getRankIndex(maxMellowCard) > DataModel.getRankIndex(dataModel.getCardCurrentPlayerGetHighestInSuit(s))) {
				return true;
			}
			
		}
		
		return false;
	}
}


package mellow.ai.situationHandlers;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.cardDataModels.handIndicators.NonMellowBidHandIndicators;
import mellow.ai.cardDataModels.normalPlaySignals.MellowSignalsBasedOnLackOfTricks;
import mellow.ai.cardDataModels.normalPlaySignals.VoidSignalsNoActiveMellows;
import mellow.ai.situationHandlers.objects.CardAndValue;
import mellow.cardUtils.CardStringFunctions;
import mellow.cardUtils.DebugFunctions;

public class NoMellowPlaySituation {

	public static String handleNormalThrow(DataModel dataModel) {

		int throwIndex = dataModel.getCardsPlayedThisRound() % Constants.NUM_PLAYERS;
		
		//TODO: put in function
		//TRAM (The rest are mine) logic:
		if( (throwIndex == 0 && couldTRAM(dataModel))
			|| (throwIndex > 0 && couldPlayMasterSAndTram(dataModel))) {
			
			System.out.println("THE REST ARE MINE! (TRAM)");
			
			if(throwIndex == 0 && ! dataModel.currentAgentHasSuit(Constants.SPADE)) {
				return dataModel.getMasterCard();
			} else {
				return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
				
			}
			//Just trump near the end of the round:
		}/* else if(throwIndex > 0
				&& ! dataModel.currentAgentHasSuit(dataModel.getLeaderIndex())
				&& dataModel.getNumberOfCardsOneSuit(Constants.SPADE) >= 1
				&& dataModel.getNumCardsInCurrentPlayerHand() - 1 <= dataModel.getNumberOfCardsOneSuit(Constants.SPADE)
				&& (dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) > 0
						|| dataModel.getNumTricks(Constants.CURRENT_PARTNER_INDEX) > 0
						|| (! PartnerSaidMellowSituation.mellowhasDangerousOffsuit(dataModel)
						&&  dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)
						)
					)
				&& dataModel.throwerHasCardToBeatCurrentWinner()) {
			
			return dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
		}*/
		//END TODO put in function
		

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
	
	
	//TODO: This is a mess!
	public static String AILeaderThrow(DataModel dataModel) {


		String bestCardToPlay = null;
		double currentBestScore = -1000000.0;
		
		
		for(int suitIndex = 0; suitIndex< Constants.NUM_SUITS; suitIndex++) {
			
			int numCardsOfSuitInHand = dataModel.getNumCardsOfSuitInCurrentPlayerHand(suitIndex);
			
			//Can't play a suit you don't have:
			if(numCardsOfSuitInHand == 0) {
				continue;
			}
			
			CardAndValue cardAndValueRet;
			
			if(suitIndex == Constants.SPADE) {
				//cardAndValueRet = AILeaderThrowGetSpadeValue(dataModel);
				cardAndValueRet = AILeaderThrowGetSpadeValue(dataModel);
			} else {
				cardAndValueRet = AILeaderThrowGetOffSuitValue(dataModel, suitIndex);
			}
			
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
		
		//For now, play highest of group:
		//EX: if you have the KS and QS, play KS if you were originally intending the QS.
		bestCardToPlay = SeatedLeftOfOpponentMellow.getHighestPartOfGroup(dataModel, bestCardToPlay);
		
		int suitIndex = CardStringFunctions.getIndexOfSuit(bestCardToPlay);
		
		
		if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, suitIndex)
		&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, suitIndex)
		&& !dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
		&& !dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)
		&& dataModel.signalHandler.playerAlwaysFollowedSuit(Constants.LEFT_PLAYER_INDEX, suitIndex)) {
			
			String tmp = PartnerSaidMellowSituation.getLowestCardOfGroup(dataModel, bestCardToPlay);
			if(! tmp.equals(bestCardToPlay)) {
				System.out.println("AHA");
				
			}
		}
		
		return bestCardToPlay;
	}
	
	
	//pre: current player has spade in hand
	public static CardAndValue AILeaderThrowGetSpadeValue(DataModel dataModel) {
		
		int numCardsOfSuitInHand = dataModel.getNumCardsOfSuitInCurrentPlayerHand(Constants.SPADE);
		int numCardsOfSuitOtherPlayersHave =
		dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE);
		

		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "QS JS TS QD TD 6D")) {
			System.out.println("Debug");
		}

		String cardToPlay = null;
		
		double curScore = 0.0;
		
		boolean partnerSignalledHighCardOfSuit = false;

		
		if(dataModel.currentPlayerHasMasterInSuit(Constants.SPADE)) {

			//Made up a number to say having the master card to lead in partner's void suit is cool:
			//TODO: refine later
			curScore += 10.0;
			
			if(dataModel.currentPlayerHasAtLeastTwoMastersInSuit(Constants.SPADE)
					&& ! dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
					&& ! dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)) {
				curScore += 50.0;
			}
			
			boolean opponentsHaveNoSpadeProbably = false;
			if(numCardsOfSuitOtherPlayersHave == 0) {
				opponentsHaveNoSpadeProbably = true;
				
				
			} else if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
					&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)
					&& ! dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE) ) {
				opponentsHaveNoSpadeProbably = true;
				//But partner has S...
				
			}
			
			int numForcingCards = dataModel.getNumForcingCardsCurrentPlayerHasInALLOffSuitAssumingNoTrump();
			
			if(opponentsHaveNoSpadeProbably
					&& 2 * (numCardsOfSuitInHand + numForcingCards) < dataModel.getNumCardsInCurrentPlayerHand()) {
				
				//TODO:
				//public int getNumMasterOrForcingCards() {
					
				//}
				
				//Don't lead spade when opponents don't have spade.
				//Unless you only have 1 non spade, then I don't know.
				curScore -= 300;
			}

		//Treat partner's possible master as your own...
		} else if(dataModel.signalHandler.partnerHasMasterBasedOnSignals(Constants.SPADE)) {
			
			//System.out.println("TEST " + suitIndex);
			curScore += 9.5;
			partnerSignalledHighCardOfSuit = true;
			
			//Treat partner's possible master as your own...
		} else if(dataModel.signalHandler.playerSignalledHighCardInSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)) {
			curScore += 9.0;
			partnerSignalledHighCardOfSuit = true;
			
					
		}
		
		//Leading in a suit where RHS prob has master could be a good idea sometimes...
		//Like when partner is trumping
		//or partner has no spade.
		if(      ! dataModel.currentPlayerHasMasterInSuit(Constants.SPADE)
				&& dataModel.signalHandler.playerSignalledHighCardInSuit(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)
				&& ! NonMellowBidHandIndicators.hasKQEquivAndNoAEquiv(dataModel, Constants.SPADE)) {
			curScore -= 55.0;
		}
		
		int numVoid = 0;
		for(int i=1; i<Constants.NUM_PLAYERS; i++) {
			if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(i, Constants.SPADE)) {
				numVoid++;
			}
		}
		
		if(numVoid == 2 && 
				numCardsOfSuitInHand <= 
				dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(
						dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE))) {
			//Don't lead into spade when someone else completely dominates it:
			curScore -= 100;
		}
		
		
		//Start of custom Logic for considering spade:
		
		if(dataModel.currentPlayerHasMasterInSuit(Constants.SPADE)) {
			
			cardToPlay = dataModel.getMasterInHandOfSuit(Constants.SPADE);
			
			if(numCardsOfSuitInHand >= 2) {
				String cardTmp = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(Constants.SPADE);
				
				//If you have the AK equiv, don't be afraid to play A to see what's going on.
				if(dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(cardTmp)
						== dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(cardToPlay)) {
					curScore += 30.0;
				}
			
			}
			
			if(numCardsOfSuitInHand >= numCardsOfSuitOtherPlayersHave
					&& !dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
					&& !dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)) {
				//Made to fix: 3-4981
				curScore += 20.0;
				
			}
			
		} else if(NonMellowBidHandIndicators.hasKQEquivAndNoAEquiv(dataModel, Constants.SPADE)) {
			
			curScore += 8.0;
			cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
			
			//if(NonMellowBidHandIndicators.getNumCardsInHandForTop5OfSuit(dataModel, Constants.SPADE) >= 3) {
			//		curScore += 51.0;
			//}
		
		} else if(NonMellowBidHandIndicators.getNumCardsInHandForTop5OfSuit(dataModel, Constants.SPADE) 
			            >=3) {
			
			//Change from 8 to 20 to make a test pass (Nov 2022)
			curScore += 20.0;
			cardToPlay = SeatedLeftOfOpponentMellow.getHighestPartOfGroup
					(dataModel, dataModel.getCardCurrentPlayerGetThirdHighestInSuit(Constants.SPADE));
			
		} else {
			curScore += 5.0;
			cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
			
		}
		
		
		//If trumping over RHS opponent, don't volunteer to lead spade:
		if(! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)
				&& numCardsOfSuitInHand < dataModel.getNumCardsInCurrentPlayerHand() - 1
				&& 3 * numCardsOfSuitInHand <= 2 + dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE)) {
					
			for(int offsuit=1; offsuit<Constants.NUM_SUITS; offsuit++) {
					if( dataModel.isVoid(Constants.CURRENT_AGENT_INDEX, offsuit)
					&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, offsuit)
					&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, offsuit)
					&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, offsuit)
					) {
					
					//I don't know how much lower to go.
					curScore -= 30.0;
					//I don't know if we should break or not...
					//break;
				}
					
			}
		}
		
		if(3 * numCardsOfSuitInHand < 8 + numCardsOfSuitOtherPlayersHave ) {
			//If we're trumping or planning to trump, don't lead spade, unless we started with 6+ spades... 
			for(int offsuit=1; offsuit<Constants.NUM_SUITS; offsuit++) {
				
				if( (dataModel.isVoid(Constants.CURRENT_AGENT_INDEX, offsuit)
						&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(offsuit) >= 9
						&& !dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, offsuit)
						&& !dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, offsuit)
						&& !dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, offsuit)
						)
				  || (dataModel.getNumberOfCardsOneSuit(offsuit) == 1
					&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(offsuit) == Constants.NUM_RANKS - 1)) {
					
					
					//Tested -100, and that messes up a lot of test cases.
					curScore -= 40.0;
					
					//I don't know if we should break or not...
					//break;
				}
			}
		}
					

		if(// Partner void:
				dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)
				//1 of opponents void:
				&& (dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
						|| dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)
						|| numCardsOfSuitOtherPlayersHave == 1)
				//No hope of brute-forcing a trick in spade:
				&& numCardsOfSuitInHand <= dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit
					(dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE))) {
			
			//Don't just waste your spade:
			curScore -= 100.0;
			
			
		} else if(dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)
				&& (dataModel.currentPlayerGetNumMasterSpadeInHand() > 1
				|| dataModel.currentPlayerHasMasterInSuit(Constants.SPADE)
				|| (dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
				   && dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)
				   && dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE) > 1
				   )
				)
			){
			//Want to play spade if partner is void, but not when opponents are void:
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
				 //Don't drain your partner's spade when the opponents don't have spade...
				 //See: 0-1571 and 2-3379
				curScore -=100.0;
		}

		if(numCardsOfSuitOtherPlayersHave == 0) {
			//TODO: this fixes testcase 220 of Michael2021, but
			// maybe it's not always best
			// I do think it's better than leading the offsuit masters though
			if(hasAnOffsuitMaster(dataModel)) {
				curScore += 70.0;
				
				//if( numCardsOfSuitInHand == 1 && dataModel.getNumCardsInCurrentPlayerHand() >= 4) {
				//	curScore -=100;
				//}
			} else {
			
				if( numCardsOfSuitInHand == 1 && dataModel.getNumCardsInCurrentPlayerHand() >= 3) {
					curScore -=100;
				} else if(numCardsOfSuitInHand == 2 && dataModel.getNumCardsInCurrentPlayerHand() >= 5) {
					curScore -= 50;
				}
			}
			
		}
		
		//Don't lead small spade if you don't have much...
		if(dataModel.currentPlayerGetNumMasterSpadeInHand() < numCardsOfSuitInHand
				&& 3 * numCardsOfSuitInHand < numCardsOfSuitOtherPlayersHave) {
			curScore -= 20.0;
			
			if(numCardsOfSuitInHand == 1) {
				//Save your last spade
				curScore -= 20.0;
			}
			
			
		} else {
			double diff = numCardsOfSuitInHand - (1.0 *numCardsOfSuitOtherPlayersHave)/3.0;
			
			if( ! dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
					|| ! dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)) {
				//Just play spade if you have them...
				if(diff >= 1.1) {
					curScore += 20.0;
					if(diff >= 2.1) {
						curScore += 10.0;
						
						if(diff > 3.1) {
							curScore += 15.0;
						}
					}
				}
			}
			
			//Look forward to leading S because it might help your offsuit masters:
			double maxValueCashOut = 0.0;
			
			for(int s=1; s<Constants.NUM_SUITS; s++) {
				if(s == Constants.SPADE) {
					continue;
				} else {
					maxValueCashOut = Math.max(maxValueCashOut, cashOutMasterRating(dataModel, s));
				}
				
			}
			curScore += maxValueCashOut;
		}
		

		//Don't volunteer to play spade if you 1 less than master in spade
		//and have few spades
		if(NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, Constants.SPADE)
				&& !NonMellowBidHandIndicators.hasKQEquivAndNoAEquiv(dataModel, Constants.SPADE)
				&& 3 * (numCardsOfSuitInHand-1) < numCardsOfSuitOtherPlayersHave) {
			curScore -= 20.0;
		}
		
		//Play spade in the hopes of making your offsuit masters relevant:
		if((dataModel.currentPlayerHasMasterInSuit(1) && dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(1) < 6)
				|| (dataModel.currentPlayerHasMasterInSuit(2) && dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(2) < 6)
				|| (dataModel.currentPlayerHasMasterInSuit(3) && dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(3) < 6)) {
			
			//Only do this if you think you might win a trick somehow, so you could lead again...
			//TODO: this is a rough estimate that doesn't account for stolen tricks,
			// Tricks given to partner/ taken from partner
			// or unfortunate circumstances...

			if((dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)
					|| dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE))
				&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)) {
				
				if(dataModel.getNumCardsInCurrentPlayerHand() == 2
						&& dataModel.currentPlayerHasMasterInSuit(Constants.SPADE)) {
					//Over-fitting, but whatever!
					curScore += 10.0;
				} else {
					curScore += 0.0;
				}
			} else {
				
				if(dataModel.getNumCardsOfSuitInCurrentPlayerHand(Constants.SPADE) == 1
						&& ! dataModel.currentPlayerHasMasterInSuit(Constants.SPADE)) {
					//Pass:
				} else {
					curScore += 10.0;
				}
				
				if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)) {
					curScore += 1.0;
				}
				
				int numPlayersWithSpade = getNumOtherPlayersTrumpingSpade(dataModel);
				
				if(numPlayersWithSpade * dataModel.getNumberOfCardsOneSuit(Constants.SPADE) > dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE)) {
					curScore += 15.0;
				}
				
			}
			//if(dataModel.getNumTricks(Constants.CURRENT_AGENT_INDEX) + dataModel.getNumTricks(Constants.CURRENT_PARTNER_INDEX)
			//	< dataModel.getBid(Constants.CURRENT_AGENT_INDEX) + dataModel.getBid(Constants.CURRENT_PARTNER_INDEX)
			//	|| dataModel.currentPlayerHasMasterInSuit(Constants.SPADE)) {
				
			//}
			
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
		if((dataModel.getNumCardsCurrentUserStartedWithInSuit(Constants.SPADE) >= 5
			&& dataModel.getNumberOfCardsOneSuit(Constants.SPADE) * getNumOtherPlayersTrumpingSpade(dataModel) > numCardsOfSuitOtherPlayersHave
				
				&& (hasAnOffsuitMaster(dataModel)
					|| hasAnOffsuitKingEqCouldMakeTrick(dataModel)
					|| hasAnOffsuitQueenEqCouldMakeTrick(dataModel)
					|| hasAnOffsuitKingQueenEqCouldMakeTrick(dataModel)
					|| dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)
				)
				&& (numCardsOfSuitOtherPlayersHave > 1
						|| (numCardsOfSuitOtherPlayersHave == 1
						&& dataModel.currentPlayerHasMasterInSuit(Constants.SPADE)
						)
					)
			) ||
			(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE) 
					&& (dataModel.currentPlayerGetNumMasterOfSuitInHand(Constants.SPADE) >= 2
					    || NonMellowBidHandIndicators.hasKQEquivAndNoAEquiv(dataModel, Constants.SPADE)
					    )
			)
		) {
			
			if(partnerIsTrumpingSuitWeCouldLead(dataModel)) {
				//Don't trump if partner is really trumping suit you could lead
				//This is a bit rough, but whatever...
					//For example, why not just add to the value of leading the offsuit?
					//Whatever!
				
			} else {
			
				curScore += 30.0;
			}
			
			//Check if we have aggressive spade lead options:
			if(NonMellowBidHandIndicators.hasKQEquivAndNoAEquiv(dataModel, Constants.SPADE)) {
				
				cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
				curScore += 25.0;
				
			}
		}
		
		
		//TODO put in function:
		//Basic awareness of when to play S based on bids:
		if(dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) >= 5
				&& dataModel.getBid(Constants.CURRENT_PARTNER_INDEX)
				- dataModel.getNumTricks(Constants.CURRENT_PARTNER_INDEX) >= 2
				//&& ! dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE
				) {
		//END TODO put in function
			//curScore += 25.0;
			curScore += 5.0 * (dataModel.getBid(Constants.CURRENT_PARTNER_INDEX)
					- dataModel.getNumTricks(Constants.CURRENT_PARTNER_INDEX) - 2);
			
			
		}
		
		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "TS KH 9C JD ")) {
			System.out.println("Debug");
		}
		
		
		if(dataModel.currentPlayerHasMasterInSuit(Constants.SPADE) 
				&& ((dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE) <= 2
					&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE) >= 0)
					||
					dataModel.playerCouldSweepSpades(Constants.SPADE)
					)
				&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)) {
			
			//If you could pretty much sweep spade:
			curScore += 20.0;
			
			if(dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE) == 1) {

				curScore += 20.0;
			}
			
		} else {

			//Maybe leading spade helps opponents?
			
			int bidDiff = (dataModel.getBid(Constants.CURRENT_PLAYER_INDEX) + dataModel.getBid(Constants.CURRENT_PARTNER_INDEX))
			- (dataModel.getBid(Constants.LEFT_PLAYER_INDEX) + dataModel.getBid(Constants.RIGHT_PLAYER_INDEX));
			//Rough way to look at the bids to decide whether or not to play spade
			
			
			if((dataModel.getBid(Constants.RIGHT_PLAYER_INDEX) >= 5
					&& (dataModel.getBid(Constants.RIGHT_PLAYER_INDEX)
						- dataModel.getNumTricks(Constants.RIGHT_PLAYER_INDEX) >= 2
					    && ! dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)
					    && bidDiff <= -2
				    )
				)
					||	(  ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
							&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)
							&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE))
			
					) {
				
				//Don't feed RHS.
				curScore -= 40.0;
			}
			
	
			if((dataModel.getBid(Constants.LEFT_PLAYER_INDEX) >= 5
					&& (
							(dataModel.getBid(Constants.LEFT_PLAYER_INDEX)
							- dataModel.getNumTricks(Constants.LEFT_PLAYER_INDEX) >= 2
							&& ! dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
							//&& ! dataModel.currentPlayerHasMasterInSuit(Constants.SPADE)
							&& bidDiff <= -2
							)
					)
				)
						||	
							( ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)
									&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)
									&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
						 )
				 ) {
				//Don't feed LHS.
				curScore -= 40.0;
			}
			
			
			if(bidDiff > 0) {
				//Limit how much of a benefit this is because it's covered elsewheres:
				curScore += 5.0 * Math.min(4, bidDiff);
			} else {
				curScore += 5.0 * Math.max(-3, bidDiff);
			}
			
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
		
		//Just encourage partner to play high...
		//if they signalled master S.
		if(partnerSignalledHighCardOfSuit) {
			
			cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
			
			if(3 * numCardsOfSuitInHand > 
					7 + dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE)) {

				//Play high anyways if you have lots of spade...
				
				cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
			
			}
			
		}

		//Play high anyways if you partner has lots of spade:
		if(dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) >= 5
				&& ! NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, Constants.SPADE)
				&& ! (NonMellowBidHandIndicators.hasQEquivNoAorK(dataModel, Constants.SPADE)
						&& dataModel.getNumberOfCardsOneSuit(Constants.SPADE) >= 3)
				&& numCardsOfSuitInHand <= 3
				&& numCardsOfSuitOtherPlayersHave >= 8
			) {
			
			cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
			//Play high anyways if you partner has lots of spade...
		}
		
		
		//Avoid leading spade if partner is trumping:
		
		if(! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)
			) {
			for(int suitIndex = 0; suitIndex<Constants.NUM_SUITS; suitIndex++) {
				if(suitIndex == Constants.SPADE) {
					continue;
				}
				
				if(dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) <= 4
						&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_AGENT_INDEX, suitIndex)
						&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, suitIndex)
						&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, suitIndex)
						&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex) > 5) {
					
					curScore -= 5 * dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex);
				
				} else if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, suitIndex)
						&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex) > 5
						//Only try to drain if you have sufficient spade:
						//TODO: maybe drain without that much spade if there's no hope to trump?
						&& 3 * numCardsOfSuitInHand > numCardsOfSuitOtherPlayersHave) {
					
					curScore += 5 * dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex);
				}
			}
			
		}
		//End avoid leading spade if partner is trumping.

		return new CardAndValue(cardToPlay, curScore);
	}

	public static CardAndValue AILeaderThrowGetOffSuitValue(DataModel dataModel, int suitIndex) {
		
		
		int numCardsOfSuitInHand = dataModel.getNumCardsOfSuitInCurrentPlayerHand(suitIndex);
		int numCardsOfSuitOtherPlayersHave =
		dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex);
		

		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "AS 8S 7S 3S 2S JH TH 9H 5H 7C 8D")
				&& suitIndex == 1) {
			System.out.println("Debug");
		}

		
		String cardToPlay = null;
		
		double curScore = 0.0;
		
		boolean partnerSignalledHighCardOfSuit = false;
		
		
		//Don't want to lead S if you're the only one with it... unless you have a plan...
		if(dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE) == 0
				|| (dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE))
				    && dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)
				) {
			
			if(couldWinAllCardsOfOffsuitInHand(dataModel, suitIndex)) {
				return new CardAndValue(dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex), 10000.0);
			}
			
			if(dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE) == 0 &&
					dataModel.currentAgentHasSuit(Constants.SPADE)) {
				
				//If current play has Kequiv and nothing else, hope that another player will throw off Aequiv later:
				if(   (NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, suitIndex)
						&& numCardsOfSuitInHand == 1)
					||
					  (NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, suitIndex)
						&& ! dataModel.currentPlayerHasMasterInSuit(suitIndex)
						&& numCardsOfSuitInHand >= 2
					    && numCardsOfSuitOtherPlayersHave > 1
					    )
					) {
					//Do nothing
				} else {
					curScore += 50.0;
				}
			}
		}

		if(dataModel.currentPlayerHasMasterInSuit(suitIndex)
				&&	dataModel.signalHandler.hasCurTeamSignalledHighOffsuit(suitIndex)) {
			System.out.println("(Opponents know they don't have my master of " + CardStringFunctions.getSuitString(suitIndex) + ")");
			
			curScore += 35.0;
		}
			
		//End case where current play is only one with spade.

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

		} else if( ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, suitIndex)
				&& dataModel.signalHandler.playerSingalledMasterCardOrVoidAccordingToCurPlayer(Constants.CURRENT_PARTNER_INDEX, suitIndex)
				&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_AGENT_INDEX, Constants.SPADE)
				|| dataModel.signalHandler.partnerHasMasterBasedOnSignals(suitIndex)
			) {
			
			//System.out.println("COND 1");
			//System.out.println("TEST " + suitIndex);
			curScore += 9.5;
			partnerSignalledHighCardOfSuit = true;
			
			if(NonMellowBidHandIndicators.wantPartnerToLead(dataModel)) {
				System.out.println("(DEBUG: WANT PARTNER TO LEAD BY PLAYING SUIT INDEX: " + suitIndex+ ")");
				curScore += 15.0;
			}
			
			//Don't want to setup our partner for failure:
			if(numCardsOfSuitOtherPlayersHave - 1 == 0 && 
					dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
	                  (Constants.CURRENT_PARTNER_INDEX, suitIndex) == false) {
				curScore -= 100.0;
			}
			
			//Treat partner's possible master as your own...
		} else if(
				( ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, suitIndex)
				&& dataModel.signalHandler.playerSingalledMasterCardOrVoidAccordingToCurPlayer(Constants.CURRENT_PARTNER_INDEX, suitIndex)
				&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_AGENT_INDEX, Constants.SPADE)
				)
				|| dataModel.signalHandler.partnerHasMasterBasedOnSignals(suitIndex)
				|| dataModel.signalHandler.getPlayerIndexOfKingSacrificeForSuit(suitIndex) == Constants.CURRENT_PARTNER_INDEX) {
			//System.out.println("COND 2");
			curScore += 9.0;
			partnerSignalledHighCardOfSuit = true;
			
			//Don't want to setup our partner for failure:
			if(numCardsOfSuitOtherPlayersHave - 1 == 0 && 
					dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
	                  (Constants.CURRENT_PARTNER_INDEX, suitIndex) == false) {
				curScore -= 100.0;
			}

		} else if(dataModel.signalHandler.partnerDoesNotHaveMasterBasedOnSignals(suitIndex)
				&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, suitIndex)) {
			curScore -= 20.0;
			
		} else if(MellowSignalsBasedOnLackOfTricks.playerCouldHaveAorKBasedOnTrickCount(dataModel, Constants.CURRENT_PARTNER_INDEX, suitIndex)
				  && ! MellowSignalsBasedOnLackOfTricks.playerCouldHaveAorKBasedOnTrickCount(dataModel, Constants.RIGHT_PLAYER_INDEX, suitIndex)) {
			
			if(! dataModel.isCardPlayedInRound(DataModel.getCardString(DataModel.ACE, suitIndex))
			&& ! dataModel.hasCard(DataModel.getCardString(DataModel.ACE, suitIndex))
			&& ! dataModel.isCardPlayedInRound(DataModel.getCardString(DataModel.KING, suitIndex))
			&& ! dataModel.hasCard(DataModel.getCardString(DataModel.KING, suitIndex))
					) {
				//Don't risk losing partner's K
				curScore -= 2.0;
			} else {
				curScore += 9.5;
				//Don't trust this signal enough to play low...
				//partnerSignalledHighCardOfSuit = true;
				
			}
			
			//Don't mind not messing up your partner's K (Didn't really help...)
		} else if(dataModel.isCardPlayedInRound(dataModel.getCardString(DataModel.ACE, suitIndex))) {
			curScore += 5.0;
			
			//leading KQequiv is about the same as leading Q.
		} else if(NonMellowBidHandIndicators.hasKQEquivAndNoAEquiv(dataModel, suitIndex)) {
			curScore += 5.0;
		}
		
		//Aug 23rd
		//TODO: try not to lead suit you or your partner doesn't have master of.
		//if(dataModel.currentPlayerHasMasterInSuit(suitIndex) == false
		//		&& dataModel.signalHandler.partnerDoesNotHaveMasterBasedOnSignals(suitIndex)) {
		//	curScore -= 20.0;
		//}
		
		//TODO: put into function
		//Bonus: Dare opponents to use up last trump:

		if(numCardsOfSuitOtherPlayersHave == 0
				&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE) == 0) {

			System.out.println("(No brainer lead!)");
			curScore += 1040.0;
			
		} else if(
				numCardsOfSuitOtherPlayersHave == 0
				&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE) <= 1
				&& (dataModel.getNumberOfCardsOneSuit(Constants.SPADE) == 0
					||
					dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE)
					== dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE))
					)
			){
			//TODO: try to give bonus for more conditions as they arrive
		
			System.out.println("(BONUS TIME)");
			curScore += 40.0;
		}
		
		//END BONUS DARE OPPONENT
		
				
		//Leading in a suit where RHS prob has master could be a good idea sometimes...
		//Like when partner is trumping
		//or partner has no spade.
		if(      ! dataModel.currentPlayerHasMasterInSuit(suitIndex)
				&& (dataModel.signalHandler.playerSignalledHighCardInSuit(Constants.RIGHT_PLAYER_INDEX, suitIndex)
						|| dataModel.signalHandler.getPlayerIndexOfKingSacrificeForSuit(suitIndex) == Constants.RIGHT_PLAYER_INDEX)
						
				&& ! NonMellowBidHandIndicators.hasKQEquivAndNoAEquiv(dataModel, suitIndex)) {
			curScore -= 55.0;
		}
		
		//Start of logic that's only for considering playing offsuits:
	
			
		//Check if leading suitIndex helps partner trump:
		if(
				(       (dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, suitIndex)
						||dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
						                  (Constants.CURRENT_PARTNER_INDEX, suitIndex)
						|| dataModel.signalHandler.getPlayerIndexOfKingSacrificeForSuit(suitIndex) == Constants.CURRENT_PARTNER_INDEX
						)
					&& dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE) == false
				)
			&& ((dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
				(Constants.RIGHT_PLAYER_INDEX, suitIndex) == false
				&& dataModel.signalHandler.getPlayerIndexOfKingSacrificeForSuit(suitIndex) != Constants.RIGHT_PLAYER_INDEX)
					|| dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)
				)
				
			&& numCardsOfSuitOtherPlayersHave >= 1) {
		
				if(numCardsOfSuitOtherPlayersHave >= 2
							
						//If player on left is also void, that's great!
						|| (numCardsOfSuitOtherPlayersHave >= 1
							&& dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, suitIndex))
						) {

					curScore += 100;

				} else if(numCardsOfSuitOtherPlayersHave == 1
						&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, suitIndex)
						&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, suitIndex)
						&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, suitIndex)
						) {

					curScore += 200;
					
				} else if(numCardsOfSuitOtherPlayersHave == 1
						&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, suitIndex)
						&& numCardsOfSuitInHand > 1
						&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)	) {
					
					// RHS has a 50:50 chance of trumping, so
					// don't play it if there's a better option.
					//I could go lower than +20, but whatever...
					
					curScore += 20;
					
					
				} else {
					curScore += 70;
				}
				
				
				if(dataModel.currentPlayerHasMasterInSuit(suitIndex)) {
					
					cardToPlay = dataModel.getMasterInHandOfSuit(suitIndex);
				} else if(NonMellowBidHandIndicators.getCardThatWillEventuallyForceOutAllMasters(dataModel, suitIndex) != null) {
					
					//TODO: maybe return the highest card of group?
					cardToPlay = NonMellowBidHandIndicators.getCardThatWillEventuallyForceOutAllMasters(dataModel, suitIndex);
					
				} else {
					cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(suitIndex);
				}
		
		//Easy trick:
		} else if(dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE) == 0
				&& dataModel.getNumCardsOfSuitInCurrentPlayerHand(Constants.SPADE) == 0
				&& dataModel.currentPlayerHasMasterInSuit(suitIndex)) {
			
			cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex);
			
			//The in-between jackpot:
			curScore += 1100;
			
			//Lazy way of preferring to lead masters without creating opponent masters:
			if(dataModel.playerWillWinWithAllCardsInHandForSuitIfNotTrumpedMinusCardToTakeTrick(
						Constants.CURRENT_AGENT_INDEX,
						suitIndex,
						"")
						) {
				curScore += 100;
			}
			
			curScore -= numCardsOfSuitInHand;
			if(numCardsOfSuitInHand > 1) {
				curScore -= numCardsOfSuitOtherPlayersHave;
			}
			
			
		} else if(numCardsOfSuitOtherPlayersHave == 0
				|| ( dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, suitIndex)
				     && dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, suitIndex))
				) {
			//Might want to do this if right is out of spades...
			
			cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex);
			
			if(dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE) == 0) {
				
				//Jackpot 1!
				curScore += 1000;
				
				if(dataModel.currentPlayerHasMasterInSuit(suitIndex)) {
					//TODO: maybe it's good to let partnier win sometimes...
					curScore += 200;
				}
				
				
			} else if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
					(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)
					&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
					(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)) {
				
				//Pretty good. You're making this trick if your partner doesn't take it...
				curScore +=300;
				
			} else if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
					(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)
					
				&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
				(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE) == false) {
				
				curScore += 50;
				
			
			} else {

				//Reduced from -100 to -30 because leading your only spade can be even worse than this.
				if(dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE) > 1) {
					//At this point, you're just feeding the opponent.
					curScore -= 30;
				} else {
					//Dare opponents to use their last spade to trump it:
					curScore += 30;
					//TODO: maybe this bonus is only relevant if you have another master card a cards under.
				}
				
				if(dataModel.currentAgentHasSuit(Constants.SPADE)
						&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE) <= 2
						&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(
								dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE))
							>= 1
							) {
					
					//Try to clear the spade so your low spade could maybe become master:
					curScore += 40;

				} else if(numCardsOfSuitInHand > 1
						&& dataModel.signalHandler.playerAlwaysFollowedSuit
						(Constants.RIGHT_PLAYER_INDEX, suitIndex)
						&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)) {
					//Don't lead this suit if partner might think they can trump without getting trumped over.
					curScore -= 70;
				} 
						
				
			}
			
				
		} else {
			
			//LHS void cases:
			if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
					(Constants.LEFT_PLAYER_INDEX, suitIndex)

					&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
						(Constants.CURRENT_PARTNER_INDEX, suitIndex) == false

					&& numCardsOfSuitOtherPlayersHave > 3) {
				
				if(dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE) > 2) {
						curScore -= 5.0 * (numCardsOfSuitOtherPlayersHave);
				} else {
					//Dare LHS to trump...
					curScore -= Math.min(10, 5.0 * (numCardsOfSuitOtherPlayersHave));
				}
			}
			
			//Don't feed LHS when there's few spade left and partner is probably following suit
			//(This fixes debug-8015)
			if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, suitIndex)
				&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
				&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, suitIndex)
				&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)
				&& numCardsOfSuitOtherPlayersHave > 1
				&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE) <= 3
				&& dataModel.currentAgentHasSuit(Constants.SPADE)) {
				
				if(dataModel.currentPlayerHasMasterInSuit(suitIndex)) {
					curScore -= 20.0;
				} else {
					curScore -= 40.0;
				}
				
				
			}
				
			
			//END LHS void cases
			
			//RHS probably has to follow suit bonus:
			if(numCardsOfSuitOtherPlayersHave > 0
					&& numCardsOfSuitOtherPlayersHave < 4
					&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, suitIndex)
					&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, suitIndex)
					&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, suitIndex)
					&& 
					dataModel.signalHandler.getMinCardRankSignal(Constants.RIGHT_PLAYER_INDEX, suitIndex)
					<
					dataModel.signalHandler.getMinCardRankSignal(Constants.CURRENT_PARTNER_INDEX, suitIndex)
					&& 
					dataModel.signalHandler.getMinCardRankSignal(Constants.RIGHT_PLAYER_INDEX, suitIndex)
					<
					dataModel.signalHandler.getMinCardRankSignal(Constants.LEFT_PLAYER_INDEX, suitIndex)
					) {
				
				//Works:
				curScore += 10.0;
			}
			//END RHS probably has to follow suit bonus
			
			
			if( (dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
					(Constants.RIGHT_PLAYER_INDEX, suitIndex) == false
				  && dataModel.signalHandler.getPlayerIndexOfKingSacrificeForSuit(suitIndex) != Constants.RIGHT_PLAYER_INDEX
				)
				|| dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
					(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)
				) {
				//increased to 26.0 to fix testcase Michael2021 -> 290...
				curScore += 26.0;
				
				if(dataModel.didPlayerIndexLeadMasterAKOffsuit(Constants.RIGHT_PLAYER_INDEX, suitIndex)
						&& ! dataModel.didPlayerIndexLeadMasterAKOffsuit(Constants.LEFT_PLAYER_INDEX, suitIndex)) {
					//Made it more than +10, because I wanted to beat
					//playing spade with bid diff of 1 in our favour.
					
					if(dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex) > 3) {
						curScore += 12.0;
					} else {
						//Want this to be less than 4.5, so player will prefer to play into partner's A or K...
						curScore += 3.0;
					}
				}
			} else if(dataModel.getNumCardsOfSuitInCurrentPlayerHand(Constants.SPADE) > 0
					&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(
							dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE)) <= dataModel.getNumCardsOfSuitInCurrentPlayerHand(Constants.SPADE)
					&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE))
							==0) {
				//Leading master to drain spade isn't a bad idea
				
				curScore += 20.0;
			} else {
				//Make leading master worse than leading last spade
				//even though others have S.
				curScore -= 52.0;
			}
			
			//System.out.println(dataModel.signalHandler.getMinCardRankSignal(Constants.LEFT_PLAYER_INDEX, suitIndex));
			//System.out.println("VS: " + DataModel.getRankIndex(dataModel.getHighestCardOfSuitNotPlayed(suitIndex)));
			
			//Don't lead into suit that is going to be trumped by LHS
			//Unless there's only 2 unknown cards or less
			//Or the partner is void too
			if( (dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
					(Constants.LEFT_PLAYER_INDEX, suitIndex)
				|| 	dataModel.signalHandler.getPlayerIndexOfKingSacrificeForSuit(suitIndex) == Constants.LEFT_PLAYER_INDEX
					)
					&& ! dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
					&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex) >= 3
					&&  (dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
					(Constants.CURRENT_PARTNER_INDEX, suitIndex) == false
					|| dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE))
					) {
				
				if(dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE) > 2) {
					curScore -= 50;
				} else {
					//Dare LHS to trump:
					curScore -= 10;
				}
				
				
	
			} else if(
					! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, suitIndex)
					&& dataModel.signalHandler.playerSingalledMasterCardOrVoidAccordingToCurPlayer(Constants.LEFT_PLAYER_INDEX, suitIndex)			
				) {
				//If LHS signalled high card or void, don't be eager to play suit:
				curScore -= 30;
				
			}
				
				
			
			
			if( dataModel.currentPlayerHasMasterInSuit(suitIndex)) {
				//AH!!
				curScore += cashOutMasterRating(dataModel, suitIndex);
				

			} else if(partnerSignalledHighCardOfSuit) {
				//Don't lower it again

			} else if(NonMellowBidHandIndicators.hasKQEquivAndNoAEquiv(dataModel, suitIndex)) {
				
				//Only lower it a little...
				curScore -= 5.0;
				
				
				if((! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
				|| ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE))
				&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE) > 1
						) {
					//Prefer to lead suits that are less played... if opponents are trumping:
					
					curScore += (dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex) - 6.0)/2.0;
				}
				
				
			} else {
				curScore -= 26.0;
				
				
			}
			
			//Play the ace so you back it up with a king:
			if(numCardsOfSuitInHand > 1
				&& DataModel.getRankIndex(dataModel.getCardCurrentPlayerGetSecondHighestInSuit(suitIndex)) == DataModel.KING) {
				curScore += 5.0;
				
			}

			//Play the Kequiv if it's alone:
			if(NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, suitIndex)
				&& numCardsOfSuitInHand == 1
				&& dataModel.isMasterCard(dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex)) == false
				&& numCardsOfSuitOtherPlayersHave >= 6
				&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, suitIndex)
				&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, suitIndex)
				&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, suitIndex)
				
				//If it's all spades except for the Kequiv, it's no good:
				&& dataModel.getNumberOfCardsOneSuit(Constants.SPADE) + 1
						< dataModel.getNumCardsInCurrentPlayerHand()
			) {
				//Don't put this to +1000 because that interferes with other test cases.
				curScore += 100.0;
			}
				
			//Don't play suit with King if you don't have the queen
			if(dataModel.getRankIndex(dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex)) == dataModel.KING
					&& numCardsOfSuitInHand > 1
					&& dataModel.getRankIndex(dataModel.getCardCurrentPlayerGetSecondHighestInSuit(suitIndex)) < dataModel.QUEEN
					&& dataModel.isMasterCard(dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex)) == false) {
				
				//Changed to -30, because doing this is better than feed RHS a suit RHS is void in.
				curScore -= 30.0;
				
				
			
			//If you don't have a vulnerable king,
			//consider playing suits that others have a lot of so you are less likely to be trumped
			} else if(3.0 * dataModel.getNumCardsOfSuitInCurrentPlayerHand(suitIndex) - numCardsOfSuitOtherPlayersHave  <= 1

					//TODO: think about this in the case where you have no spade!
		//Maybe it doesn't matter after all
//*******************************************
					//&& dataModel.getNumCardsOfSuitInCurrentPlayerHand(Constants.SPADE) > 0
//*******************************************							
					&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
					(Constants.RIGHT_PLAYER_INDEX, suitIndex)
					&& dataModel.signalHandler.getPlayerIndexOfKingSacrificeForSuit(suitIndex) != Constants.RIGHT_PLAYER_INDEX) {
				
				//Only consider becoming void if it's feasible:
				int numCardsLessThanAvgOther = numCardsOfSuitOtherPlayersHave - 3 * dataModel.getNumCardsOfSuitInCurrentPlayerHand(suitIndex);
				
				if(numCardsLessThanAvgOther > 0) {
					curScore += Math.min(5.0 * numCardsLessThanAvgOther, 25);
				}
				
				if(numCardsLessThanAvgOther > 2
						&& dataModel.currentPlayerHasMasterInSuit(suitIndex)) {
					curScore += 5.0;
				}
				
			}
			
			//Consider not messing up your partner's counted king:
			if(numCardsOfSuitInHand + numCardsOfSuitOtherPlayersHave == Constants.NUM_RANKS) {
				
				if(dataModel.currentPlayerHasMasterInSuit(suitIndex) == false
						&& NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, suitIndex) == false
						&& NonMellowBidHandIndicators.hasQEquivNoAorK(dataModel, suitIndex) == false
						&& dataModel.getNumTricks(Constants.CURRENT_PARTNER_INDEX) < dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) ){
					
					curScore -= 30.0;
				}
			}
			
			
			if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "9H 9C 8C QD JD 3D 2D ")) {
				System.out.println("DEBUG");
			}
			
			if(dataModel.currentPlayerHasMasterInSuit(suitIndex)) {
				
				cardToPlay = dataModel.getMasterInHandOfSuit(suitIndex);
				
			} else if(NonMellowBidHandIndicators.hasKQEquivAndNoAEquiv(dataModel, suitIndex)) {
				
				if(dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE) > 1
						|| dataModel.getNumberOfCardsOneSuit(suitIndex) <= 2
						//TODO: or can't win any trick in a realistic way...
						) {
					cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex);
				} else {
					cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(suitIndex);
					
				}
			
			
			} else if(NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, suitIndex)) {
				
				if(numCardsOfSuitInHand > 1) {
					cardToPlay = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(suitIndex);
				} else {
					cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(suitIndex);
				}
				
			} else if(NonMellowBidHandIndicators.hasQEquivNoAorK(dataModel, suitIndex)) {
				
				if(dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
						&& dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)
						&& numCardsOfSuitInHand >= 3) {
					cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(suitIndex);
					
				} else if(numCardsOfSuitInHand >= 3
						/*&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex) >= 9*/
						&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex) >= 3) {
					//Try to make the Queen maybe?
					
					if(NonMellowBidHandIndicators.has3PlusAndQJEquivOrBetter(dataModel, suitIndex)) {
						cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex);
					} else {
						cardToPlay = dataModel.getCardCurrentPlayergetSecondLowestInSuit(suitIndex);
					}
					
				} else {
					cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex);
				}
			} else if(dataModel.hasCard(DataModel.getCardString(DataModel.JACK, suitIndex))
					&& NonMellowBidHandIndicators.hasJEquivNoAKQeq(dataModel, suitIndex)
					&& numCardsOfSuitInHand >= 4) {
				cardToPlay = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(suitIndex);
				
			} else {
				cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex);
			}
			
			//Nov 2022:
			//Monte keeps telling me to play low when Keq and 2+ cards
			//when AI has lots of spade...
			//I don't get it, but fine! Maybe I shouldn't blindly follow monte, but whatever.
			if(NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, suitIndex)
					&& 2.5 * dataModel.getNumCardsOfSuitInCurrentPlayerHand(Constants.SPADE) > 
					dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE)
					&& dataModel.getNumCardsOfSuitInCurrentPlayerHand(Constants.SPADE) > 1
					&& dataModel.getNumCardsOfSuitInCurrentPlayerHand(suitIndex) >= 3
					&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE) < 5
				) {
				curScore += 30.0;
				cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(suitIndex);
			}
			
			
			//I like leading offsuit queens... if no one is void and I don't have a hope of winning with it.
			if(DataModel.getRankIndex(dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex)) == DataModel.QUEEN
					&& ! dataModel.isCardPlayedInRound(DataModel.getCardString(DataModel.ACE, suitIndex))
					&& ! dataModel.isCardPlayedInRound(DataModel.getCardString(DataModel.KING, suitIndex))
					&& 
						! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, suitIndex)
					   && dataModel.signalHandler.getPlayerIndexOfKingSacrificeForSuit(suitIndex) != Constants.LEFT_PLAYER_INDEX
					&& 
						! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, suitIndex)
					   && dataModel.signalHandler.getPlayerIndexOfKingSacrificeForSuit(suitIndex) != Constants.RIGHT_PLAYER_INDEX
				) {
				
				if(dataModel.getNumCardsCurrentUserStartedWithInSuit(suitIndex) < 3
						|| (dataModel.getNumCardsCurrentUserStartedWithInSuit(Constants.SPADE) < 5 
								&& (dataModel.getNumCardsCurrentUserStartedWithInSuit(suitIndex) > 4
										|| dataModel.hasCard(DataModel.getCardString(DataModel.JACK, suitIndex))))
						) {
					
					if(dataModel.getNumCardsCurrentUserStartedWithInSuit(suitIndex) < 3) {
						//Reduce the bonus if AI started with less than 3 cards of suit,
						//because AI already gets a low # card in suit count bonus,
						//and I want AI to decide to play master over playing queen
						curScore += 5.0;
					} else {
						
						curScore += 25.0;
					}

					if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "AS 7S 5S QH 5H 4H 3H 8C TD 9D 6D 3D 2D")) {
						System.out.println("DEBUG");
					}
					
					
					if(dataModel.hasCard( DataModel.getCardString(DataModel.JACK, suitIndex))) {
						curScore += 10.0;
					}

				}
				
				
			}
			
			
			//Don't play Queen if opponents have less spade and you have lots of the suit:
			if( !dataModel.currentPlayerHasMasterInSuit(suitIndex)
				&&	! NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, suitIndex)
				&&	NonMellowBidHandIndicators.hasQEquivNoAorK(dataModel, suitIndex)
				&& dataModel.getNumberOfCardsOneSuit(suitIndex) >= 3
				&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit
						(dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex),
								dataModel.getCardCurrentPlayerGetSecondHighestInSuit(suitIndex))
					> 0
				&& 
				(
					(dataModel.getBid(Constants.CURRENT_PARTNER_INDEX)  + 1 >
				    dataModel.getBid(Constants.LEFT_PLAYER_INDEX) + dataModel.getBid(Constants.RIGHT_PLAYER_INDEX))
				|| (3 * dataModel.getNumberOfCardsOneSuit(Constants.SPADE) > dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE)
					&& !dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)
						)
				|| (2 * dataModel.getNumberOfCardsOneSuit(Constants.SPADE) > dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE))
				)
			) {
				//Don't play highest if you might be able to save it and use it later:
				if(cardToPlay.equals(dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex))
						//You could play Q if you have the J equiv...
						&& ! NonMellowBidHandIndicators.has3PlusAndQJEquivOrBetter(dataModel, suitIndex)) {
					//System.out.println("(DEBUG: DON'T PLAY QUEEN for suitindex = " + suitIndex + "!)");
					cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(suitIndex);
				}
			}
			
			//Like to play low offsuits when you have lots of spade...
			if(3 * dataModel.getNumberOfCardsOneSuit(Constants.SPADE) > 
				4.0 + dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE)
				&& ! NonMellowBidHandIndicators.hasKQEquivAndNoAEquiv(dataModel, suitIndex)
				&& ! dataModel.currentPlayerHasMasterInSuit(suitIndex)
				&& dataModel.getNumberOfCardsOneSuit(suitIndex) >= 3
				&& (NonMellowBidHandIndicators.hasQEquivNoAorK(dataModel, suitIndex)
				|| NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, suitIndex))) {
		
				cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(suitIndex);
				
				//System.out.println("TEST");
				//TEST

				if(NonMellowBidHandIndicators.hasQEquivNoAorK(dataModel, suitIndex)
						&& dataModel.getNumberOfCardsOneSuit(suitIndex) == 3
						&&  dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex) > 6 ){

					//Don't be too excited to play low when you only have Q and 2 others... 
					curScore += 5.0;
				} else {
					curScore += 20.0;
				}
		
			}
	
			
			if(partnerSignalledHighCardOfSuit) {
				
				//TODO: this might need more thought...
				cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(suitIndex);
			}
		}
		

		return new CardAndValue(cardToPlay, curScore);
	}
	
	public static String AISecondThrow(DataModel dataModel) {
		String cardToPlay = null;
		//get suit to follow.

		System.out.println("2nd throw");
		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "QS 9S 8S 7C QD 6D 3D")) {
			System.out.println("Debug");
		}
		
		
		//TODO: only deal with string (No index)
		int leaderSuitIndex = dataModel.getSuitOfLeaderThrow();
		String leaderCard = dataModel.getCardLeaderThrow();
		
		//TODO currentAgentHasSuit and isVoid does the same thing...?
		if(dataModel.currentAgentHasSuit(leaderSuitIndex)) {
			
			if(dataModel.couldPlayCardInHandOverCardInSameSuit(leaderCard)) {
				
				//I'm going to keep it to isVoid, so the AI won't be as suceptible to tricks:
				//Hopefully, this gets more sophisticated in the future.
				// (If I replace third void with just strongSignalled, it sometimes does better and sometimes does worse)
				boolean thirdVoid = dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, leaderSuitIndex)
						//Let's trust the king sac signal though:
						|| dataModel.signalHandler.getPlayerIndexOfKingSacrificeVoidForSuit(leaderSuitIndex) == Constants.LEFT_PLAYER_INDEX;
				
				//boolean thirdVoid = dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, leaderSuitIndex);
				boolean fourthProbVoid = dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, leaderSuitIndex);
				
				if(thirdVoid && fourthProbVoid) {	
					cardToPlay = dataModel.getCardInHandClosestOverSameSuit(dataModel.getCardLeaderThrow());

				} else if(thirdVoid && fourthProbVoid == false) {

					cardToPlay = dataModel.getCardInHandClosestOverSameSuit(dataModel.getCardLeaderThrow());
					
					//Just play low to confuse LHS exception:
					if(		! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
						    && ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)
							&& dataModel.couldPlayCardInHandUnderCardInSameSuit(leaderCard)
							&& dataModel.getNumCardsInPlayBetweenCardSameSuitPossiblyWRONG(cardToPlay, dataModel.getCardInHandClosestUnderSameSuit(leaderCard)) > 1
							&& dataModel.getNumCardsInCurrentPlayersHandOverCardSameSuit(leaderCard) == 1
					) {
						cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(leaderSuitIndex);
					}
				
				} else if(thirdVoid == false && fourthProbVoid) {
					//TODO This doesn't really work if trump is spade... 
					
					
					cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(leaderSuitIndex);
					
					//Exception:
					//Play lower if LHS signalled no spade...
					if(leaderSuitIndex == Constants.SPADE
							&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, leaderSuitIndex)
							&& ! SeatedLeftOfOpponentMellow.getHighestPartOfGroup(dataModel, dataModel.getCardInHandClosestOverCurrentWinner())
							.equals(dataModel.getCardCurrentPlayerGetHighestInSuit(leaderSuitIndex))
							) {
						cardToPlay = dataModel.getCardInHandClosestOverCurrentWinner();
					
					}
					
				} else if(thirdVoid == false && fourthProbVoid == false){
					
					if(dataModel.getSuitOfLeaderThrow() != Constants.SPADE) {
						
						//Logic for dealing with offsuit:
						
						if(dataModel.currentPlayerHasMasterInSuit(leaderSuitIndex)) {
							
							cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(leaderSuitIndex);
							
							//I'm still not ready to do Queen tricks :(
							/*
							if(leaderSuitIndex == Constants.SPADE
									&& dataModel.getNumCardsInCurrentPlayersHandOverCardSameSuit(leaderCard) >= 2
									&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(
											dataModel.getCardCurrentPlayerGetSecondHighestInSuit(leaderSuitIndex)) == 1
									//TODO: implement a "deserved" tricks counter.
									&& dataModel.getBid(Constants.LEFT_PLAYER_INDEX) <= dataModel.getNumTricks(Constants.LEFT_PLAYER_INDEX)
									&& dataModel.signalHandler.getMaxCardRankSignal(Constants.RIGHT_PLAYER_INDEX, leaderSuitIndex)
										> DataModel.getRankIndex(dataModel.getCardCurrentPlayerGetSecondHighestInSuit(leaderSuitIndex))) {
								//Play for tricks:
								//Play Q equiv instead of master in the hopes of making an extra trick:
								cardToPlay = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(leaderSuitIndex);
							}*/
							
						} else {
							
							//Logic for dealing with offsuit and not having master:
							String curPlayerTopCardInSuit = dataModel.getCardCurrentPlayerGetHighestInSuit(leaderSuitIndex);
							
							if((DataModel.getRankIndex(curPlayerTopCardInSuit) == DataModel.KING
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
									
								} else if(dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit(curPlayerTopCardInSuit, leaderCard) >= 1) {
									//If Jack lead, don't lead king.
									return dataModel.getCardCurrentPlayerGetLowestInSuit(leaderSuitIndex);
									
								} else {
									//Play the King while the Ace is still out!
									return curPlayerTopCardInSuit;
								}
							
												
							} else if(dataModel.throwerHasCardToBeatCurrentWinner()) {
								
								//Consider playing high second if there's no real chance of making the trick,
								// but you want 3rd thrower to prove they can play higher than you:
								if( (dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(curPlayerTopCardInSuit) >= 2)
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
									
								} else {

									//Just play slightly over the person who lead:
									cardToPlay = dataModel.getCardInHandClosestOverCurrentWinner();
									

									if(
											(dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(cardToPlay) > 4
											|| dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(leaderSuitIndex) > 5)
											&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit(cardToPlay, dataModel.getCardCurrentPlayerGetLowestInSuit(leaderSuitIndex)) > 0
											&& ! dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, leaderSuitIndex)
											&& ! dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, leaderSuitIndex)
											&& dataModel.getNumCardsInCurrentPlayersHandOverCardSameSuit(dataModel.getCardLeaderThrow()) == 1) {
										//Exception:
										// Just play low if there's 6 cards higher than your card...
										// And there's in between cards in other people's hands...
										// This exception might only apply to spades though
										
										cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(leaderSuitIndex);
									} 
									
									return cardToPlay;
									
								}
							
							} else {
								return dataModel.getCardCurrentPlayerGetLowestInSuit(leaderSuitIndex);
							}
							
							
						}
						
					} else {
						//Lead suit is spade:
						
						if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, leaderSuitIndex)
								&& ! SeatedLeftOfOpponentMellow.getHighestPartOfGroup(dataModel, dataModel.getCardInHandClosestOverCurrentWinner())
								.equals(dataModel.getCardCurrentPlayerGetHighestInSuit(leaderSuitIndex))
								) {
							cardToPlay = dataModel.getCardInHandClosestOverCurrentWinner();
						
						} else if(dataModel.currentPlayerHasMasterInSuit(leaderSuitIndex)) {
							
							
							if(dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(leaderSuitIndex) >= 3) {
								
								//Play low 2nd by default when it comes to spade:
								cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(leaderSuitIndex);
								
								System.out.println(dataModel.getNumCardsInPlayBetweenCardSameSuitPossiblyWRONG(cardToPlay,
										dataModel.getCardCurrentPlayerGetHighestInSuit(leaderSuitIndex)));
								
								if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)) {
									//Play low because lhs is weak
									
								} else if(SeatedLeftOfOpponentMellow.getHighestPartOfGroup(dataModel, cardToPlay)
										.equals(dataModel.getCardCurrentPlayerGetHighestInSuit(leaderSuitIndex))) {
									cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(leaderSuitIndex);
									
									//TODO: between is bugged, and I had to put 2 here. Maybe fix it?
								} else if(dataModel.getNumCardsInPlayBetweenCardSameSuitPossiblyWRONG(cardToPlay,
										dataModel.getCardCurrentPlayerGetHighestInSuit(leaderSuitIndex)) == 2) {
									cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(leaderSuitIndex);
									
									
								} else if(dataModel.getNumCardsInPlayOverCardSameSuit(leaderCard) - dataModel.getNumCardsInCurrentPlayersHandOverCardSameSuit(leaderCard) <= 2
										&& dataModel.getNumCardsInCurrentPlayersHandOverCardSameSuit(leaderCard) >= 2) {
									cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(leaderSuitIndex);
									
								} else if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)) {
									
									//Can't lie if partner signalled no spade:
									cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(leaderSuitIndex);
								
								} else if(dataModel.getNumberOfCardsPlayerPlayedInSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE) > dataModel.getBid(Constants.CURRENT_PARTNER_INDEX)) {

									//Partner prob has weak spade:
									cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(leaderSuitIndex);
								
								} else if(  dataModel.getNumTricks(Constants.LEFT_PLAYER_INDEX) > dataModel.getBid(Constants.LEFT_PLAYER_INDEX)) {

									//LHS prob has weak spade and might have a spade because we already checked for that.
									cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(leaderSuitIndex);
								
								} else if( NonMellowBidHandIndicators.getNumAorKorQorJEquiv(dataModel, Constants.SPADE) >= 3) {
									
									if(NonMellowBidHandIndicators.hasKEquiv(dataModel, Constants.SPADE)) {
										cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(leaderSuitIndex);
									
									} else {
										cardToPlay = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(Constants.SPADE);
									}
									
								//Just play high if 2 spades left and 2nd round of play...
								} else if( dataModel.getNumberOfCardsOneSuit(Constants.SPADE) == 2 
										&& 3 * dataModel.getNumberOfCardsOneSuit(Constants.SPADE) 
										<= dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE)
										&& dataModel.getNumberOfCardsOneSuit(Constants.SPADE) + 
										dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE)
										 <= Constants.NUM_STARTING_CARDS_IN_HAND - 3
										) {
									cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(leaderSuitIndex);
								}
								
								
							} else {
								
								cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(leaderSuitIndex);
							}
							
							
							
							//Check if we could play barely over the lead:
							if(dataModel.cardAGreaterThanCardBGivenLeadCard(leaderCard, cardToPlay)) {

								String cardTmp = dataModel.getCardInHandClosestOverCurrentWinner();
								
								if(dataModel.getNumCardsInPlayOverCardSameSuit(cardTmp) > 3
										&& dataModel.getNumCardsInPlayOverCardSameSuit(cardTmp) < 6) {
									//Just play over, it won't hurt...
									cardToPlay = cardTmp;
								
								} else if(dataModel.getNumCardsOfSuitInCurrentPlayerHand(Constants.SPADE) == 2
										&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(leaderCard) == 1) {
									//Not much use playing under if there's just 1 card under, and you have only 2 spade:
									cardToPlay = cardTmp;
								}
							}
						
						} else {

							if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "JS 2S 8H 7H 6H 3H KC 7C 4C QD 5D 3D")) {
								System.out.println("Debug");
							}
							//Reminder: Lead suit is spade
							String curPlayerTopCardInSuit = dataModel.getCardCurrentPlayerGetHighestInSuit(leaderSuitIndex);
							
							if((DataModel.getRankIndex(curPlayerTopCardInSuit) == DataModel.KING
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
								
								if(leaderSuitIndex == Constants.SPADE
										&& (    NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, Constants.SPADE)
										    || (NonMellowBidHandIndicators.hasQEquivNoAorK(dataModel, Constants.SPADE)
										    		&& dataModel.getNumCardsOfSuitInCurrentPlayerHand(Constants.SPADE) >= 3)
										    || dataModel.getNumCardsOfSuitInCurrentPlayerHand(Constants.SPADE) >= 4)) {
									
									//Play low for and don't challenge if you want to preserve highish spade:
									//There might be some complicated exceptions, but whatever.
									return dataModel.getCardCurrentPlayerGetLowestInSuit(leaderSuitIndex);
	
								} else if(dataModel.cardAGreaterThanCardBGivenLeadCard(cardToPlay, leaderCard)) {
									
									return cardToPlay;
									
								} else if(dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit(curPlayerTopCardInSuit, leaderCard) >= 1) {
									//If Jack lead, don't lead king.
									return dataModel.getCardCurrentPlayerGetLowestInSuit(leaderSuitIndex);
									
								} else {
									//Play the King while the Ace is still out!
									return curPlayerTopCardInSuit;
								}
							
												
							} else if(dataModel.throwerHasCardToBeatCurrentWinner()) {
								
								//Consider playing barely over if 3rd signalled no spades:

								String cardToConsider = dataModel.getCardInHandClosestOverCurrentWinner();
								

								//Save K/Q equiv and try to not throw it if you don't have both the K and the Q equiv:
								if(NonMellowBidHandIndicators.hasKQEquivAndNoAEquiv(dataModel, Constants.SPADE)) {
									return cardToConsider;
								
								} else if(dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE).equals(cardToConsider)
										&& (NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, Constants.SPADE) || NonMellowBidHandIndicators.hasQEquivNoAorK(dataModel, Constants.SPADE))) {
									

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
									
									if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "JS TS 7S 9H 8H 7H TC AD TD 7D 3D")) {
										System.out.println("Debug");
									}
									if(dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(cardToConsider) > 4
											&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit(cardToConsider, dataModel.getCardCurrentPlayerGetLowestInSuit(leaderSuitIndex)) > 0
											&& ! dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
											&& ! dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)) {
									
										// Just play low if there's 6 cards higher than your card...
										// And there's in between cards in other people's hands...
										
										System.out.println(dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit(cardToConsider, dataModel.getCardCurrentPlayerGetLowestInSuit(leaderSuitIndex)));
										cardToConsider = dataModel.getCardCurrentPlayerGetLowestInSuit(leaderSuitIndex);
									
									}
									
									//Figure out if 2nd thrower should play over or under spade lead:
									String lowest = dataModel.getCardCurrentPlayerGetLowestInSuit(leaderSuitIndex);
									String closestOver = dataModel.getCardInHandClosestOverCurrentWinner();
									
									if(closestOver.equals(lowest)) {
										return lowest;
									} else if(dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit(closestOver, lowest) ==0 ) {
										return closestOver;
									} else if(dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit(closestOver, lowest) ==1 
											&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(closestOver) <= 3
											&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(lowest) > 1) {
										return closestOver;
									} else {
										return lowest;
									}
									
									
								}
							
							
							} else {
								return dataModel.getCardCurrentPlayerGetLowestInSuit(leaderSuitIndex);
							}
							
							
						}
						
					}
					
				} else {
					// I covered (3rd void/not void) * (4th void/not void)
					System.err.println("ERROR: this condition shouldn't happen in get ai 2nd throw");
					System.exit(1);
				}

			} else {
				cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(leaderSuitIndex);
			}
			
			
			//No following suit:
		} else {

			if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "JS TS 7S 6S 4S JC ")) {
				System.out.println("Debug");
			}
			
			//no trumping: play off:
			if(leaderSuitIndex== Constants.SPADE || dataModel.isVoid(Constants.CURRENT_AGENT_INDEX, Constants.SPADE)) {
				cardToPlay = getJunkiestCardToFollowLead(dataModel);
				
				//Must play trump:
			} else if(dataModel.currentPlayerMustTrump()) {
				cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
			
				//TODO: if 3rd signalled void in suit, and signalled less than 7S,
				//play 7S or over...
				
				//Option to trump no need to go bid:
			} else {
				

				if(dataModel.isMasterCard(leaderCard)
						&& dataModel.getNumberOfCardsOneSuit(Constants.SPADE) >= 1) {

						
					//Let partner trump for you:
					if(
							//Partner could trump for you
							! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)
							&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, leaderSuitIndex)
							
							//If there's only 1 more spade available... so partner could probably trump:
							// (//TODO: this is a Hack...) If there's 1 more spade available, you should be looking into forcing plays.
							&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE) > 1
									
							
							//If you have more spades than the avg player, let partner trump or you have master S
							&& (  (3 * dataModel.getNumberOfCardsOneSuit(Constants.SPADE) > 
					                 dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE)
					              ||
					              	(
						              NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, Constants.SPADE)
						              && dataModel.getNumberOfCardsOneSuit(Constants.SPADE) > 1
						            )
					              )
								|| (dataModel.currentPlayerHasMasterInSuit(Constants.SPADE))
								)
							
						    //If you just have 1 small spade, just play it and don't let partner trump
							&& (dataModel.getNumberOfCardsOneSuit(Constants.SPADE) > 1 
									    || dataModel.isMasterCard(dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE))
							)
							//Don't let partner trump for you if they bid higher
							&& dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) <
								dataModel.getBid(Constants.CURRENT_AGENT_INDEX) + 2
						) {
						
						cardToPlay = getJunkiestCardToFollowLead(dataModel);
					
					
					} else {
						
						String highSpadeInMind = dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
						
						int maxRankLHS = dataModel.signalHandler.getMaxRankSpadeSignalled(Constants.LEFT_PLAYER_INDEX);

						boolean couldProbPlayOverLHS = false;
						
						if(maxRankLHS == VoidSignalsNoActiveMellows.MAX_UNDER_RANK_2) {
							highSpadeInMind = dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
							
						} else if( dataModel.getCardInHandClosestOverSameSuit(DataModel.getCardString(maxRankLHS, Constants.SPADE)) != null) {
							
							String minSpadeOverLHS =dataModel.getCardInHandClosestOverSameSuit(DataModel.getCardString(maxRankLHS, Constants.SPADE));
							couldProbPlayOverLHS = true;
							
							if(! dataModel.currentPlayerHasMasterInSuit(Constants.SPADE)
									|| (dataModel.currentPlayerHasMasterInSuit(Constants.SPADE)
										    &&  dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit(highSpadeInMind, minSpadeOverLHS)
										    		> 0)) {
								highSpadeInMind = minSpadeOverLHS;
								
							}
						
						}
						


						//Go big or go home:
						if(
						(
						//K to protect:
								 (dataModel.getNumberOfCardsOneSuit(Constants.SPADE) == 2
						           && NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, Constants.SPADE))
						 || //No Q to protect:
						  (dataModel.getNumberOfCardsOneSuit(Constants.SPADE) == 3
				                   && NonMellowBidHandIndicators.hasQEquivNoAorK(dataModel, Constants.SPADE))
						  )
						
						//TODO: LHS out for blood function
						  //LHS out for blood:
						    && dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, leaderSuitIndex)
							&& !dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
							&& !couldProbPlayOverLHS
						 ) {
							
							//Go home...
							cardToPlay = getJunkiestOffSuitCardBasedOnMadeupValueSystem(dataModel);
							
							//Target another suit
						} else if (   
								//We have lots of spade:
								(dataModel.getNumberOfCardsOneSuit(Constants.SPADE) >= 4
									   || 3 * dataModel.getNumberOfCardsOneSuit(Constants.SPADE) - dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE) > 5
									   )
								//LHS is a real threat:
								&& (dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, leaderSuitIndex)
									|| dataModel.getNumberOfCardsPlayerPlayedInSuit(Constants.LEFT_PLAYER_INDEX, leaderSuitIndex) > 1)
								&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
								
									//There's probably a more inclusive way, but this works for the simple case:
								&& dataModel.cardAGreaterThanCardBGivenLeadCard(DataModel.getCardString(dataModel.signalHandler.getMaxRankSpadeSignalled(Constants.LEFT_PLAYER_INDEX), Constants.SPADE),
										dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE))
								
								&& (
								    //We could realistically clear another suit for trumping without helping opponents too much:
									//AND LHS might not trump:
								        (
								        currentPlayerHasOffsuitToThrowOff(dataModel)
								        && !dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, leaderSuitIndex)
								        && !dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
								        
								        //Egde-case: just trump if you trumped last round:
								        //If there's still 5 of the suit out therre, don't be too worried:
								        && dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(leaderSuitIndex) < 5 
								       )
								    
								   //OR: We need to keep spade...
								   ||    (
								         dataModel.getNumberOfCardsOneSuit(Constants.SPADE)
							             <= dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE))
									     )
								   
								   //OR: Partner could do it:
								   ||    (  dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, leaderSuitIndex)
									     && !dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)
									     )
								   
								   //OR: Let LHS take it because losing a spade is like losing a trick:
								   ||    (//Has lots of spade:
										   dataModel.getNumberOfCardsOneSuit(Constants.SPADE) > 2
										   && dataModel.getNumberOfCardsOneSuit(Constants.SPADE) * 3 - dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE) > 3
										   //Don't mess with RHS
										   && shouldNotTrumpBeforeLHSBecauseLHSWillMessYouUp(dataModel, leaderSuitIndex)
										 )
								   )
								) {
							
							
							if (dataModel.getNumberOfCardsPlayerPlayedInSuit(Constants.LEFT_PLAYER_INDEX, leaderSuitIndex) > 1
									//Check if LHS signalled lower than PARTNER:
								&&      (! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, leaderSuitIndex)
								&& dataModel.signalHandler.getMinCardRankSignal(Constants.CURRENT_PARTNER_INDEX, leaderSuitIndex) <=
									 dataModel.signalHandler.getMinCardRankSignal(Constants.LEFT_PLAYER_INDEX, leaderSuitIndex)
								)
								 ) {
								//TODO: trump anyways?
							}
							
							//Desperation exception:
							// I'm unsure about this:
							if(dataModel.isMasterCard(leaderCard)
								   && dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) + dataModel.getBid(Constants.CURRENT_AGENT_INDEX)
								   - dataModel.getNumTricks(Constants.CURRENT_PARTNER_INDEX) - dataModel.getNumTricks(Constants.CURRENT_AGENT_INDEX)
								   + 2 >= dataModel.getNumCardsInCurrentPlayerHand()
                                   && dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(leaderSuitIndex) > 3
                                   && ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, leaderSuitIndex)
                                   && ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, leaderSuitIndex)
                                   ) {
                               
								cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
							 
							//TODO: what if you could just trump just because you have AKS?
							// or you have all but 1 spade... it gets complicated.
							//I'll deal with it when a new test case comes up
							//clear other suit:
							} else if(currentPlayerHasOffsuitToThrowOff(dataModel)) {
								cardToPlay = getOffsuitCardCurrentPlayerCouldThrowToClearSuit(dataModel);
							} else {
								cardToPlay = getJunkiestOffSuitCardBasedOnMadeupValueSystem(dataModel);
							}
						} else {

							
							//dataModel.getCardInHandClosestOverSameSuit(leaderCard)
							
							//TODO: June28th: be more willing to play 'consideredHighTrump' if it's not a master card.
							
							if( dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(highSpadeInMind)
									>= dataModel.getNumberOfCardsOneSuit(Constants.SPADE)) {
								
								if(dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(leaderSuitIndex) >= 5
										&& (     ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, leaderSuitIndex)
												|| dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE))
										) {
									
									//Play low because you don't expect to be trumped over:
									cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
									
								} else {
									//Play high because if you're trumped over, at least they needed to throw a high one
									cardToPlay = highSpadeInMind;
									
								}
								
							} else {
								
								//If 5050, play 2nd highest...
								//Or if LHS signalled no card, play 2nd highest.
								
								
								//TODO: make is LHS dangerous function
								if(  dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, leaderSuitIndex)
										&& !dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
										&& dataModel.getNumCardsOfSuitInCurrentPlayerHand(Constants.SPADE) >= 2) {
									
									  //Play second highest so you at least put up a fight:
										String tmp = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(Constants.SPADE);
										
										if(DataModel.getRankIndex(tmp) > DataModel.getRankIndex(highSpadeInMind)) {
											cardToPlay = highSpadeInMind;
										} else {
											cardToPlay = tmp;
										}
									
								} else {
									//Play lowest because LHS is not dangerous.
									cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
								}
						   }
						
						
						}
						
					}

				//Lead player didn't play master:
				//I guess we should trump if we don't have much spade?
				} else if(
						//Partner probably can't trump
						(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)
						      || ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, leaderSuitIndex))
						&& 
						
						(
								//Not much spade:
								//I adjusted it to 2.5 to make it slightly more willing to trump and take the lead.
								2.5 * dataModel.getNumberOfCardsOneSuit(Constants.SPADE) <=
					               dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE)
						         
					               //OR: only 1 non-master spade:
						      || (dataModel.getNumberOfCardsOneSuit(Constants.SPADE) == 1 
						         && dataModel.currentPlayerHasMasterInSuit(Constants.SPADE) == false)   
						      
						      	//OR: opponent only one that signalled master
						      || (dataModel.signalHandler.leftHandSideHasMasterBasedOnSignals(leaderSuitIndex))
						      
						      	//OR: low throw-away card that doesn't support a King or Queen or Jack equiv:
						      || (dataModel.getNumberOfCardsOneSuit(Constants.SPADE) <= 2
						      	&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE)) >= 3)
						      
						       //OR: partner bid high and there's still tricks to be made
						      || ( dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) >= 5 && 
						    		  dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) - dataModel.getNumTricks(Constants.CURRENT_PARTNER_INDEX) >= 2)
						)

						&& //No single master spade
						 ! (dataModel.getNumberOfCardsOneSuit(Constants.SPADE) == 1
		                   && dataModel.currentPlayerHasMasterInSuit(Constants.SPADE))
						&& //No K to protect:
						 ! ((dataModel.getNumberOfCardsOneSuit(Constants.SPADE) == 2 || dataModel.getNumberOfCardsOneSuit(Constants.SPADE) == 3 )
						           && NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, Constants.SPADE))
						 && //No Q to protect:
						 ! (dataModel.getNumberOfCardsOneSuit(Constants.SPADE) == 3
				                   && NonMellowBidHandIndicators.hasQEquivNoAorK(dataModel, Constants.SPADE))
						) {		

					
					cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);

				} else if(
						//Partner probably can't trump
						(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)
						      || ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, leaderSuitIndex))
						&& 
						//opponent only one that signalled master
					      dataModel.signalHandler.leftHandSideHasMasterBasedOnSignals(leaderSuitIndex)
					 ) {
					
					//Trumping standards should be lowered if it's just a free trick:
					cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
					
				//Partner is useless, so trump
				} else if (dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)
					      && dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, leaderSuitIndex)){ 

					cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);

					
				//Just play spade because why not? You have all spade and masters but 1 card
				//TODO: maybe allow for 2 exceptions later?	
				} else if (3 * dataModel.getNumberOfCardsOneSuit(Constants.SPADE) > dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE)
						 && getNumberOfNonSpadesAndNonMasters(dataModel) <= 1
						 && ! dataModel.signalHandler.partnerHasMasterBasedOnSignals(leaderSuitIndex)
						 && dataModel.getNumberOfCardsOneSuit(Constants.SPADE) > 0
						 ){ 
 
					//Try to play over LHS if possible...
					if((dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, leaderSuitIndex)
							
							)
							&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(leaderSuitIndex, Constants.SPADE)) {
						
						int lhsMaxRank = dataModel.signalHandler.getMaxRankSpadeSignalled(Constants.LEFT_PLAYER_INDEX);
						String tmpCard = DataModel.getCardString(Math.max(lhsMaxRank, DataModel.RANK_TWO), Constants.SPADE);
						
						if(lhsMaxRank >= DataModel.RANK_TWO
								&& dataModel.couldPlayCardInHandOverCardInSameSuit(tmpCard)) {

							//Play above LHS:
							cardToPlay = dataModel.getCardInHandClosestOverSameSuit(tmpCard);
						} else {
							//just trump low:
							
							cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
						}
						
					} else {
						//just trump low:
						cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
					}
					
					//return cardToPlay;
					
				//LHS probably has master and no spade, and partner has to follow suit, so just trump it
				} else if(
						dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(leaderSuitIndex) >= 4
						&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
						&& 	! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, leaderSuitIndex)
							&& (dataModel.signalHandler.playerSingalledMasterCardOrVoidAccordingToCurPlayer(Constants.LEFT_PLAYER_INDEX, leaderSuitIndex)
									||
								dataModel.signalHandler.getPlayerIndexOfKingSacrificeForSuit(leaderSuitIndex) == Constants.LEFT_PLAYER_INDEX
						       )
						&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, leaderSuitIndex)
						&& ! dataModel.signalHandler.playerSingalledMasterCardOrVoidAccordingToCurPlayer(Constants.CURRENT_PARTNER_INDEX, leaderSuitIndex)) {
					
					cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
					
					//Play master S
				} else if(dataModel.currentPlayerHasMasterInSuit(Constants.SPADE)
						 //Check if willing to keep AS for later:
						 && (3 * dataModel.getNumberOfCardsOneSuit(Constants.SPADE) < dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE)
								|| (dataModel.getNumberOfCardsOneSuit(Constants.SPADE) >= 2
								    && dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit(
								    		dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE), 
								    		dataModel.getCardCurrentPlayerGetSecondHighestInSuit(Constants.SPADE))
								       == 0
								    )
							)
						 && dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, leaderSuitIndex)
						 && ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
						 && ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, leaderSuitIndex)
						 && dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(leaderSuitIndex) >= 3
						) {
					cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
				} else {
					cardToPlay = getJunkiestCardToFollowLead(dataModel);
				}
				
				

				if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "JS 8S 6S AH TH AC JC")) {
					System.out.println("Debug");
				}
				//At this point, we might have decided to trump,
				// but we might reconsider given LHS is also trumping...

				
				boolean goBigOrGoHome = false;
				boolean justGoBig = false;
				
				if(dataModel.isMasterCard(dataModel.getCardLeaderThrow())
						&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, leaderSuitIndex)
						&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(leaderSuitIndex) > 1
						//&& dataModel.signalHandler.
						&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, leaderSuitIndex)
						&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
						&& dataModel.getNumTricks(Constants.LEFT_PLAYER_INDEX) < dataModel.getBid(Constants.LEFT_PLAYER_INDEX)
					) {
					int numSpadesInHand = dataModel.getNumCardsOfSuitInCurrentPlayerHand(Constants.SPADE);
					
					if(  (numSpadesInHand >= 2
							&&	! dataModel.currentPlayerHasMasterInSuit(Constants.SPADE)
							&&	 NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, Constants.SPADE)
							&& ! NonMellowBidHandIndicators.hasKQEquivAndNoAEquiv(dataModel, Constants.SPADE)
							)
						|| (numSpadesInHand == 3
								&& ! dataModel.currentPlayerHasMasterInSuit(Constants.SPADE)
								&& ! NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, Constants.SPADE)
								&& NonMellowBidHandIndicators.hasQEquivNoAorK(dataModel, Constants.SPADE)
								&& ! NonMellowBidHandIndicators.hasQJEquivAndNoAORKEquiv(dataModel, Constants.SPADE)
							)
						){
						// Don't go big...
						// you need to safeguard K or Q equiv.
					} else {
						justGoBig = true;
					}
					
				} else if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, leaderSuitIndex)
								&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
								&& dataModel.getNumTricks(Constants.LEFT_PLAYER_INDEX) < dataModel.getBid(Constants.LEFT_PLAYER_INDEX)) {
					goBigOrGoHome = true;
				}
				
				if(justGoBig
						&& CardStringFunctions.getIndexOfSuit(cardToPlay) == Constants.SPADE
						&& ! dataModel.isMasterCard(cardToPlay)) {
					
					cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
					
				//Check if we want to risk playing high spade:
				//If we currently want to play AS, then there's no risk, so don't worry about that case.
				} else if(goBigOrGoHome
						&& CardStringFunctions.getIndexOfSuit(cardToPlay) == Constants.SPADE
						&& ! dataModel.isMasterCard(cardToPlay)) {
					
					int numSpadesInHand = dataModel.getNumberOfCardsOneSuit(Constants.SPADE);
					
					//Nov  7th, 2021 TODO: fix 2-4211
					//This is too strict.
					if((3 * numSpadesInHand >
					dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE)
					      && numSpadesInHand > 1
					      //Check about fighting:
					      && shouldNotTrumpBeforeLHSBecauseLHSWillMessYouUp(dataModel, leaderSuitIndex)
					)
					|| (numSpadesInHand == 2
						&&	!dataModel.currentPlayerHasMasterInSuit(Constants.SPADE)
						&&	NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, Constants.SPADE)
						&& !NonMellowBidHandIndicators.hasKQEquivAndNoAEquiv(dataModel, Constants.SPADE)
						)
					|| (numSpadesInHand == 3
							&& !dataModel.currentPlayerHasMasterInSuit(Constants.SPADE)
							&& !NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, Constants.SPADE)
							&& NonMellowBidHandIndicators.hasQEquivNoAorK(dataModel, Constants.SPADE)
							&& !NonMellowBidHandIndicators.hasQJEquivAndNoAORKEquiv(dataModel, Constants.SPADE)
						)
					) {
						cardToPlay = getJunkiestCardToFollowLead(dataModel);
					} else {
						
						String highcard = dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
						
						if(numSpadesInHand == 1 && NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, Constants.SPADE)) {
							cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
						
						} else if(dataModel.isMasterCard(dataModel.getCurrentFightWinningCardBeforeAIPlays())
								&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(highcard) >= 1) {
							
							if( ! shouldNotTrumpBeforeLHSBecauseLHSWillMessYouUp(dataModel, leaderSuitIndex)
									&& NonMellowBidHandIndicators.getNumAorKorQorJEquiv(dataModel, Constants.SPADE) > 1) {
								
								//Note: For the testcase I fixed this works too: (3-3377)
								//NonMellowBidHandIndicators.getNumAorKorQorJEquiv(dataModel, Constants.SPADE) > 2
								
								//Play second highest if both spades are high and try to challenge LHS:
								cardToPlay = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(Constants.SPADE);
							} else {

								//TODO: maybe handle signals for min spade LHS has...
									//&& ! dataModel.signalHandler.NOTIMPLEMENTED
								cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
							}
							
						} else {
							cardToPlay = getJunkiestCardToFollowLead(dataModel);
						}
						
					}
				} //END of go big or go home code.
			}//END of Option to trump no need to go bid:
			
		}//END of play off code
		
	
		return cardToPlay;
	}
	
	public static String AIThirdThrow(DataModel dataModel) {
		String cardToPlay = null;
		int leaderSuitIndex = dataModel.getSuitOfLeaderThrow();
		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "TH 9H 6C 5C JD TD")) {
			System.out.println("Debug");
		}
		
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
						
						if(dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, leaderSuitIndex)
						&& dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, leaderSuitIndex)
						&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
						&& dataModel.currentAgentHasSuit(Constants.SPADE)){
							//Just trump and let LHS trump over because your partner wants you to trump?
							System.out.println("(PARNTER WANTS ME TO TRUMP??)");
							
							cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
							
							if(dataModel.getNumCardsInPlayOverCardSameSuit(cardToPlay) > 3
									&& dataModel.getNumCardsInPlayUnderCardSameSuit(cardToPlay) <= 2
									) {
								//Just play junky card because you can't really influence anything:
								cardToPlay = getJunkiestCardToFollowLead(dataModel);
							}
						}
						
					} else {
						//TRUMP
						
						//TODO: what if leader(partner) plays a higher card than 2nd throw that isn't master, but 4th could trump too... 
						//... I don't even know. That gets into weird logic
						
						if(dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, leaderSuitIndex)) {
							
							//Try to play spade high enough that LHS can't play over:
							//TODO: put in seperate function
							String consideredHighTrump = dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
							
							int maxRankLHS = dataModel.signalHandler.getMaxRankSpadeSignalled(Constants.LEFT_PLAYER_INDEX);

							if(maxRankLHS == VoidSignalsNoActiveMellows.MAX_UNDER_RANK_2) {
								consideredHighTrump = dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
							
							} else if( dataModel.getCardInHandClosestOverSameSuit(DataModel.getCardString(maxRankLHS, Constants.SPADE)) != null) {
								String minSpadeOverLHS =dataModel.getCardInHandClosestOverSameSuit(DataModel.getCardString(maxRankLHS, Constants.SPADE));
							
								if(! dataModel.currentPlayerHasMasterInSuit(Constants.SPADE)
										|| (dataModel.currentPlayerHasMasterInSuit(Constants.SPADE)
											    &&  dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit(consideredHighTrump, minSpadeOverLHS)
											    		> 0)) {
									consideredHighTrump = minSpadeOverLHS;		        	 
								}
							
							}
							//End try to play spade higher than what LHS has.
							
							cardToPlay = consideredHighTrump;
							
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
				
				if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "QS TS JH 5H 3H AC JC KD JD 9D ")) {
					System.out.println("Debug");
				}
				
				//If lead is winning
				if(dataModel.cardAGreaterThanCardBGivenLeadCard(dataModel.getCardLeaderThrow(), dataModel.getCardSecondThrow())) {
					
					if(dataModel.hasNonLeadCardInHandThatCanDecreaseChanceof4thThrowerWinning()) {
						
						String leadThrow = dataModel.getCardLeaderThrow();
						String highestInHand = dataModel.getCardCurrentPlayerGetHighestInSuit(dataModel.getSuitOfLeaderThrow());
						
						//Don't be eager to play over partner in spade
						if(dataModel.getSuitOfLeaderThrow() == Constants.SPADE
								&& 
							3 * dataModel.getNumCardsOfSuitInCurrentPlayerHand(leaderSuitIndex)
							> dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(leaderSuitIndex)
								&& 
							dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit(leadThrow, highestInHand) <= 2
							    &&
							! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, leaderSuitIndex)
							
							//Just play master if LHS probably has a higher spade:
							&& ( ! dataModel.currentPlayerHasMasterInSuit(Constants.SPADE)
								|| dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(dataModel.getCardLeaderThrow()) >=
										dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(dataModel.getCardLeaderThrow())
								)
							//Just play master if RHS is out of spade, but not LHS:
							&& ! (dataModel.currentPlayerHasMasterInSuit(Constants.SPADE)
									&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, leaderSuitIndex)
									&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, leaderSuitIndex)
								)
								    ) {
							
							cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(dataModel.getSuitOfLeaderThrow());
							
						} else {

							// But feel free to play over partner in off suits.
							cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(dataModel.getSuitOfLeaderThrow());
							
						}
						
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

							//play barely over 2nd thrower to force 4th thrower to trump for the win:
							cardToPlay = dataModel.getCardInHandClosestOverSameSuit(dataModel.getCardSecondThrow());
						
						} else {
							//Play highest to force 4th to play even higher... or stop 4th thrower from winning:
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
				
				int leadSuit = dataModel.getSuitOfLeaderThrow();
				
				String tramTrickTakingCard = dataModel.getCardInHandClosestOverCurrentWinner();
				if(couldTRAMAfterPlayingCard(dataModel, tramTrickTakingCard)) {
					
					System.out.println("4th thrower taking from partner to TRAM!");
					return tramTrickTakingCard;
				
				} else if(dataModel.currentAgentHasSuit(leadSuit)
						&& dataModel.getNumCardsInHandUnderCardSameSuit(dataModel.getCurrentFightWinningCardBeforeAIPlays()) == 0) {
					
					//If you need to play over partner, pretend to play high over partner:
					return SeatedLeftOfOpponentMellow.getHighestPartOfGroup(dataModel, dataModel.getCardCurrentPlayerGetLowestInSuit(leadSuit));
				
				} else if(CardStringFunctions.getIndexOfSuit(dataModel.getCardInHandClosestOverCurrentWinner()) != Constants.SPADE
						|| dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(dataModel.getCardInHandClosestOverCurrentWinner()) >= 3){
					
					String currentWinnerCard = dataModel.getCurrentFightWinningCardBeforeAIPlays();
					
					if(dataModel.currentPlayerOnlyHasSpade()) {
						if(CardStringFunctions.getIndexOfSuit(currentWinnerCard) != Constants.SPADE
								|| ! dataModel.couldPlayCardInHandUnderCardInSameSuit(currentWinnerCard)) {
							return dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
						
						} else {
							//Just take from partner if you only have spade and you have 2 others that are higher.
							return dataModel.getCardInHandClosestOverCurrentWinner();
						}
					}
						
					
					//Check if taking the trick could setup current team for a good time:
					//Only do this if you don't have to take with a high spade.
					for(int offsuit = 1; offsuit <Constants.NUM_SUITS; offsuit++) {
						if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, offsuit)
						 && dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, offsuit)
						 && dataModel.currentPlayerHasMasterInSuit(offsuit)
						 && ( 
								(offsuit == dataModel.getSuitOfLeaderThrow() 
							 	&& !dataModel.getCardInHandClosestOverCurrentWinner().equals(dataModel.getCardCurrentPlayerGetHighestInSuit(offsuit))
						 	) ||
								 offsuit != dataModel.getSuitOfLeaderThrow() 
							)
						 && dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(offsuit) >= 1
						 
						 && (      (dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE) > 1
								 && ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)
							        )
								 || (dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE))
						     )
						 ) {
							return dataModel.getCardInHandClosestOverCurrentWinner();
						}
					}
					
				} else if(dataModel.currentPlayerOnlyHasSpade()
						&& CardStringFunctions.getIndexOfSuit(dataModel.getCardInHandClosestOverCurrentWinner()) == Constants.SPADE
						&& dataModel.couldPlayCardInHandUnderCardInSameSuit(dataModel.getCurrentFightWinningCardBeforeAIPlays())
						&& dataModel.getNumCardsInPlayBetweenCardSameSuitPossiblyWRONG(
								dataModel.getCardInHandClosestOverCurrentWinner(),
								dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE))
						== 0) {
					//Might as well take it...
					//TODO: there might be an exception if RHS still has strong spade.
					//As of this writing (oct 2022), this never happens.
					return dataModel.getCardInHandClosestOverCurrentWinner();
				}
			}

			cardToPlay = getJunkiestCardToFollowLead(dataModel);
			
			return cardToPlay;
			
		} else if(dataModel.throwerHasCardToBeatCurrentWinner()) {
			
			cardToPlay = SeatedLeftOfOpponentMellow.getHighestPartOfGroup(dataModel, dataModel.getCardInHandClosestOverCurrentWinner());
			
		} else {
			cardToPlay = getJunkiestCardToFollowLead(dataModel);
			
		}

		return cardToPlay;
	}
	//END AIS for non-mellow bid games
	
	
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
	
	public static boolean couldPlayMasterSAndTram(DataModel dataModel) {
		int throwIndex = dataModel.getCardsPlayedThisRound() % Constants.NUM_PLAYERS;
		
		if(throwIndex > 0
				&& (dataModel.isVoid(Constants.CURRENT_AGENT_INDEX, dataModel.getSuitOfLeaderThrow())
						|| dataModel.getSuitOfLeaderThrow() == Constants.SPADE)
				&& dataModel.currentPlayerHasMasterInSuit(Constants.SPADE)
				&& dataModel.throwerHasCardToBeatCurrentWinner()
				&& couldTRAMAfterPlayingCard(dataModel, dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE))) {
			
			return true;
		} else {
			
			return false;
		}
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
		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "JS TS QC 8D ")) {
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
			
			//DEBUG
			String suitString = dataModel.getCardString(0, suitIndex).substring(1);
			System.out.println("Could-Make-A-Follow-Trick-Rating for suit of " + suitString + ": " + NonMellowBidHandIndicators.getCouldMakeAFollowTrickRating(dataModel, suitIndex));
			System.out.println("Could-Make-A-Follow-Trick-Rating after throwing low card for suit of " + suitString + ": " + NonMellowBidHandIndicators.getCouldMakeAFollowTrickRatingMinusLowCard(dataModel, suitIndex));
			System.out.println("");
			System.out.println("Could-Trump-Suit-And-Win-Rating for suit of " + suitString + ": " + NonMellowBidHandIndicators.getCouldTrumpSuitAndWinRating(dataModel, suitIndex));
			System.out.println("Could-Trump-Suit-And-Win-Rating after throwing low card for suit of " + suitString + ": " + NonMellowBidHandIndicators.getCouldTrumpSuitAndWinRatingMinusLowOffsuit(dataModel, suitIndex));
			
			System.out.println("Could-Trump-Suit-And-Win-Rating after throwing low spade card for suit of " + suitString + ": " + NonMellowBidHandIndicators.getCouldTrumpSuitAndWinRatingMinusLowSpade(dataModel, suitIndex));
			//System.out.println("------------------");
			//END DEBUG

			String curCard = dataModel.getCardCurrentPlayerGetLowestInSuit(suitIndex);
			String bestCardPlayerHas = dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex);
			
			int numUnderLowest = dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(curCard);
			int numOverHighest = dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(bestCardPlayerHas);

			double currentValue = 0.0;
			
			currentValue -= numUnderLowest;
			
			
			//2-3209 hack that I decided to cancel:
			//TODO: research this!
			//if(numOverHighest == 0
			//		&& numberOfCardsInSuit == 2
			//		&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(curCard) <= 1) {
				
				//Try to save AK equiv or AQ equiv
				//currentValue -= 5.0;
				
			//}
			
			boolean partnerVoid = dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, suitIndex);
			boolean partnerTrumping = dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE) == false;

			//Easy for throwing off:
			if(dataModel.getNumCardsOfSuitInCurrentPlayerHand(Constants.SPADE) == 0
					&& numUnderLowest == 0 
					&& (numOverHighest >= 1 || dataModel.getNumCardsInCurrentPlayerHand() <=2)
					&& (numberOfCardsInSuit == 1
						||
						dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit(bestCardPlayerHas, curCard) == 0
					)
					/*&& !(partnerVoid
							&& partnerTrumping)*/) {
				
				System.out.println("Nothing lower...");
				
				
				if(numOverHighest == 1) {
					currentValue += 75;
				} else {
					currentValue += 100;
				}
				
				//Logic doesn't really do anything yet.
				if(dataModel.getNumCardsInCurrentPlayerHand() > 2
						&& partnerVoid
						&& partnerTrumping) {
					System.out.println("(DEBUG WARNING)");
					currentValue -= 50;
				}
			} else if(numberOfCardsInSuit < numOverHighest) {
				
				//Reliable hammer for throwing off that I discovered:
				
				//TODO: what if throwing off a card signals that opponent could TRAM suit?
				//Shouldn't we be careful about that?
				currentValue += 5 * (numOverHighest - numberOfCardsInSuit);
				
			
			} else if(numberOfCardsInSuit == numOverHighest) {
				
				if(numberOfCardsInSuit >= 3) {
					currentValue += 2;
				}
			}

			//End easy for throwing off

			boolean shouldSave = false;
			
			if(dataModel.currentPlayerHasMasterInSuit(suitIndex)) {
				
				if(numberOfCardsInSuit == 1) {
					shouldSave = true;
				} else if(dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(curCard) <=1 ){

					shouldSave = true;
				}
			} else if(NonMellowBidHandIndicators.hasKQEquivAndNoAEquiv(dataModel, suitIndex)) {
				
				if(numberOfCardsInSuit == 2) {
					shouldSave = true;
				} else if(numberOfCardsInSuit == 3
						&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(curCard) <= 2 ){

					shouldSave = true;
				}
				
			} else if(NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, suitIndex)
					) {
				
				if(numberOfCardsInSuit == 2) {
					shouldSave = true;
				} else if(numberOfCardsInSuit == 3
						&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(curCard) <= 2 ){

					shouldSave = true;
				}
				
			} else if(NonMellowBidHandIndicators.hasQEquivNoAorK(dataModel, suitIndex)) {

				if(numberOfCardsInSuit == 3
						&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex) > 4) {
					shouldSave = true;
				}
			}
			if(numberOfCardsInSuit == 3
					//Num inplay includes the card in your hand...
					&& dataModel.getNumCardsInPlayOverCardSameSuit(
							dataModel.getCardCurrentPlayerGetSecondHighestInSuit(suitIndex)) == 2
				) {
				shouldSave = true;
			}

			if(numberOfCardsInSuit == 4
					&& dataModel.getNumCardsInPlayOverCardSameSuit(
							dataModel.getCardCurrentPlayerGetThirdHighestInSuit(suitIndex)) == 3
				) {
				shouldSave = true;
			}
		
			//Try to trump later:
			if(shouldSave == false
				//&& dataModel.getNumCardsInPlayOverCardSameSuit(dataModel.getCardCurrentPlayerGetLowestInSuit(suitIndex)) > 3
				
				&& dataModel.getNumberOfCardsOneSuit(Constants.SPADE) >= 1
				&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex) >= 3
				&& (! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, suitIndex)
					|| dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE))
				) {
				
				if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, suitIndex)
						&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)) {
					//never mind
				} else {
					currentValue += 40 - 10 * numberOfCardsInSuit;
				}
			}
			//End try to trump later

			
			
			int numberOfCardsOthersHaveInSuit = dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex);
			
			if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, suitIndex)
					&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, suitIndex)
					&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex) >=
						dataModel.getNumberOfCardsOneSuit(suitIndex)
					&& ! dataModel.currentPlayerHasMasterInSuit(suitIndex)) {
				//Let partner deal with suit only you and partner have???
				//This is just a guess to fix 1 test case.
				
				//System.out.println("(TEST 3RD CHANGE)");
				
			} else if(
					! NonMellowBidHandIndicators.currentPlayerMightWinATrickIfAnotherOffsuitThrown(dataModel, suitIndex)
					&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex) == 0) {
				
				if( 3 * dataModel.getNumberOfCardsOneSuit(Constants.SPADE) 
						> dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE)
					) {
					//Try to sweep
					currentValue -= 20.0;
					System.out.println("(Try to sweep)");
				} else {
				//No bonus incentive to keep the suit because no one else has the suit
				// and you probably won't lead anytime soon.
				//In fact, try to throw it out...
					currentValue += 10.0;
				}
				
			} else if(numberOfCardsInSuit == 1 &&  dataModel.currentPlayerHasMasterInSuit(suitIndex)
					&& (dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex) > 0 
							//TODO: implement indicator functions to estimaste odds of making a trick...
					  ||  dataModel.getNumTricks(Constants.CURRENT_AGENT_INDEX) < dataModel.getBid(Constants.CURRENT_AGENT_INDEX)
					  		//Simple check for spades (It fixed the test case!): (TODO: make more complex)
					  || 3 * dataModel.getNumberOfCardsOneSuit(Constants.SPADE) > dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE) )
					) {
				//Don't throw off master cards unless you really need to...
				//Or the idea of leading it is unrealistic.
				currentValue -= 30.0;
				
				//Maybe it's not bad if you're planing on trumping though...
				
			} else if( (numberOfCardsInSuit == 2 || numberOfCardsInSuit == 3)
					&& ! dataModel.currentPlayerHasMasterInSuit(suitIndex)
					&&  NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, suitIndex)
					&& numberOfCardsOthersHaveInSuit >= 2) {
				
				//Don't throw off option to play low and save your Kequiv card for later
				currentValue -= 5.0;
				//System.out.println("MICHAEL HERE " + suitIndex);
				
				if(numberOfCardsInSuit == 2) {
					currentValue -= 10.0;
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
					currentValue -= 10.0;
				} else if(numOverHighest - numOverSecondHighest == 1) {
					currentValue -= 5.0;
				}
	
			}   else if((numberOfCardsInSuit == 2 || numberOfCardsInSuit == 3)
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
			
			boolean partnerSignalledVoid = dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, suitIndex);
			
			boolean partnerSignalledOnlyMasterOrVoid = dataModel.signalHandler.partnerHasOnlyMasterOrIsVoidBasedOnSignals(suitIndex);
			
			
			//TODO: maybe this is useful?
			//boolean partnerSignalledMaster = dataModel.signalHandler.partnerHasMasterBasedOnSignals(suitIndex);
			
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

		
			if(rhsTrumping == false
					&& partnerTrumping
					//TODO: the real question is whether or not you could lead/
					&& numberOfCardsOthersHaveInSuit >= 2
					) {
				
				if(partnerSignalledVoid) {
					currentValue -= 8.0;
				} else if(partnerSignalledOnlyMasterOrVoid
						&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex) > 1) {
					currentValue -=10.0;
				} else if(partnerSignalledOnlyMasterOrVoid) {
					currentValue -= 6.0;
				}
			} else if(rhsTrumping == false) {
				
				if(partnerSignalledOnlyMasterOrVoid
						&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex) > 1) {
					currentValue -=10.0;
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
				
				//Extra condition to throw off suit with more cards:
				if(dataModel.getNumberOfCardsOneSuit(suitIndex) > 3){
					int maxStrength = dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex)
			                   * numberOfCardsInSuit;
					
					
					if(maxStrength == 0) {
						currentValue += 20;
					} else {
						int strength = maxStrength;
						
						
						for(int i=0; i<numberOfCardsInSuit; i++) {
							strength -= dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(
									dataModel.getCardCurrentPlayerGetIthLowestInSuit(numberOfCardsInSuit - i - 1, suitIndex));
							
						}
						
						
						currentValue += (10.0 * (maxStrength - strength)) / maxStrength;
					}
				}
			}
				
				

			
			
			
			if(bestSuit == -1 || currentValue > valueOfBestSuit) {
				valueOfBestSuit = currentValue;
				bestSuit = suitIndex;
			}
			
		}
		
		
		/*
		//Testing obvious measure to just throw really bad cards:
		// Didn't work. Need to consider that some cards are good for making lead tricks...
		// Maybe this naive strat works better if You and Partner need 1 more trick and you're not sure you'll get it.
		for(int suitIndex=0; suitIndex<Constants.NUM_SUITS; suitIndex++) {
			if(suitIndex == Constants.SPADE || dataModel.currentAgentHasSuit(suitIndex) == false) {
				continue;
			}
			
			int numberOfCardsInSuit = dataModel.getNumCardsOfSuitInCurrentPlayerHand(suitIndex);
			
			if(numberOfCardsInSuit == 0) {
				continue;
			}
			
			if(NonMellowBidHandIndicators.getCouldMakeAFollowTrickRating(dataModel, suitIndex) <= 0.1
					&& NonMellowBidHandIndicators.getCouldTrumpSuitAndWinRating(dataModel, suitIndex) <= 0.1) {
				return dataModel.getCardCurrentPlayerGetLowestInSuit(suitIndex);
			}
			
			//DEBUG
			//String suitString = dataModel.getCardString(0, suitIndex).substring(1);
			//System.out.println("Could-Make-A-Follow-Trick-Rating for suit of " + suitString + ": " + NonMellowBidHandIndicators.getCouldMakeAFollowTrickRating(dataModel, suitIndex));
			//System.out.println("Could-Make-A-Follow-Trick-Rating after throwing low card for suit of " + suitString + ": " + NonMellowBidHandIndicators.getCouldMakeAFollowTrickRatingMinusLowCard(dataModel, suitIndex));
			//System.out.println("");
			//System.out.println("Could-Trump-Suit-And-Win-Rating for suit of " + suitString + ": " + NonMellowBidHandIndicators.getCouldTrumpSuitAndWinRating(dataModel, suitIndex));
			//System.out.println("Could-Trump-Suit-And-Win-Rating after throwing low card for suit of " + suitString + ": " + NonMellowBidHandIndicators.getCouldTrumpSuitAndWinRatingMinusLowOffsuit(dataModel, suitIndex));
			
			//System.out.println("Could-Trump-Suit-And-Win-Rating after throwing low spade card for suit of " + suitString + ": " + NonMellowBidHandIndicators.getCouldTrumpSuitAndWinRatingMinusLowSpade(dataModel, suitIndex));
			
		}*/
		
		//Take care of an edge-case:
		if(bestSuit == -1) {
			System.err.println("WARNING: Called get junkiest card while only having spade...");
			try {
				throw new Exception();
			} catch(Exception e) {
				e.printStackTrace();
			}
			return dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
		}
		
		return dataModel.getCardCurrentPlayerGetLowestInSuit(bestSuit);
	}
	
	
	public static int cashOutMasterRating(DataModel dataModel, int suitIndex) {
		
		if(! dataModel.currentPlayerHasMasterInSuit(suitIndex)) {
			return 0;
		}
		
		if(dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) >= 5
				&& dataModel.getBid(Constants.CURRENT_PARTNER_INDEX)
				- dataModel.getNumTricks(Constants.CURRENT_PARTNER_INDEX) >= 2
				&& dataModel.getNumberOfCardsOneSuit(suitIndex) > 1
				/*&& ! dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE*/) {
			return 0;
		}
		
		int numCardsOfSuitInHand = dataModel.getNumberOfCardsOneSuit(suitIndex);
		int numCardsOfSuitInOtherHand = dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex);
		
		int rating = 0;
		if( ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, suitIndex)
				|| dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)
				) {
			
				if((dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, suitIndex)
						&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE))
						&& (! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, suitIndex)
								|| dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE))
						&& numCardsOfSuitInOtherHand > 2) {
					return 0;
				}
				
				//Don't lead into suit when opponent will trump because partner can't do anything...
				//This gives opponents an easy trick...
				// There might be edge cases where you want this to happen... but I don't know.
				if(numCardsOfSuitInOtherHand <= 1
						&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)
						&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE) >= 1) {
					if(dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE) == 1
							&& dataModel.getNumberOfCardsOneSuit(Constants.SPADE) == 0) {
						//don't mind daring opponent to trump with last spade:
						return 20;		
					} else {
						return -10;
					}
				}
				
				rating += 10;

				//backup master bonus:
				if(dataModel.currentPlayerHasAtLeastTwoMastersInSuit(suitIndex)) {
					rating += 20;
				}
				
				if(numCardsOfSuitInOtherHand <= 5) {
					rating += 25;
				}
				
		}
		
		return rating;
	}
	
	public static boolean couldWinAllCardsOfOffsuitInHand(DataModel datamodel, int suitIndex) {
		
		int numCardsOfSuitInHand = datamodel.getNumCardsInCurrentPlayerHand();
		int numCardsInHandFound = 0;
		
		for(int rank=DataModel.ACE; rank>=DataModel.RANK_TWO; rank--) {
			String tmpCard = DataModel.getCardString(rank, suitIndex);
			if(datamodel.hasCard(tmpCard)) {
				numCardsInHandFound++;
				
				if(numCardsInHandFound == numCardsOfSuitInHand) {
					return true;
				}
				
			} else if(datamodel.isCardPlayedInRound(tmpCard)) {
				//Do nothing
			} else {
				int numCardsNotInHandUnder = datamodel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(tmpCard);
				
				if(numCardsNotInHandUnder + 1 > numCardsInHandFound) {
					return false;
				} else {
					return true;
				}
			}
		}
		
		return true;
	}
	
	public static boolean currentPlayerHasOffsuitToThrowOff(DataModel dataModel) {	
		return getOffsuitCardCurrentPlayerCouldThrowToClearSuit(dataModel) != null;
	}
	
	public static String getOffsuitCardCurrentPlayerCouldThrowToClearSuit(DataModel dataModel) {
		
		CardAndValue bestCardAndValue = null;
		
		for(int suitIndex=0; suitIndex<Constants.NUM_SUITS;suitIndex++) {
			if(suitIndex == Constants.SPADE
					|| dataModel.getNumberOfCardsOneSuit(suitIndex) == 0) {
				continue;
			}
			
			
			if(!dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, suitIndex)
				&& 3* dataModel.getNumberOfCardsOneSuit(suitIndex) - dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex) < 0
				) {
				
				//Don't throw off Ace alone
				if(dataModel.currentPlayerHasMasterInSuit(suitIndex)
						&&  dataModel.getNumberOfCardsOneSuit(suitIndex) == 1) {
					continue;
				
				
				//Don't throw off Kequiv with a king...
				} else if(NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, suitIndex)
						&& DataModel.getRankIndex(dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex)) == DataModel.KING
					) {
					continue;
				
				//If no ace or king to bank on, feel free to throw off cards of offsuit:
				} else {
					
					int curValue = dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex) - 3* dataModel.getNumberOfCardsOneSuit(suitIndex);
					
					if(NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, suitIndex)) {
						curValue -= 4;
					}
					
					if(NonMellowBidHandIndicators.hasQEquivNoAorK(dataModel, suitIndex)) {
						curValue -= 3;
					}
					
					if(dataModel.getNumberOfCardsPlayerPlayedInSuit(Constants.LEFT_PLAYER_INDEX, suitIndex)
							> dataModel.getNumberOfCardsPlayerPlayedInSuit(Constants.CURRENT_PARTNER_INDEX, suitIndex) ) {
						
						int diffNumCardsOfSuitPlayed = dataModel.getNumberOfCardsPlayerPlayedInSuit(Constants.LEFT_PLAYER_INDEX, suitIndex)
								                     - dataModel.getNumberOfCardsPlayerPlayedInSuit(Constants.CURRENT_PARTNER_INDEX, suitIndex);
						curValue -= 4 * diffNumCardsOfSuitPlayed;
					}

					String curCard = dataModel.getCardCurrentPlayerGetLowestInSuit(suitIndex);
					if(bestCardAndValue == null
							|| curValue > bestCardAndValue.getValue()) {
						bestCardAndValue = new CardAndValue(curCard, curValue);
					}
					
				}
			}
		}
		
		
		if(bestCardAndValue == null) {
			return null;
		}
		return bestCardAndValue.getCard();
	}
	
	public static int getNumberOfNonSpadesAndNonMasters(DataModel dataModel) {
		
		int ret = dataModel.getNumCardsInCurrentPlayerHand();
		
		ret -= dataModel.getNumberOfCardsOneSuit(Constants.SPADE);
		
		for(int suitIndex=0; suitIndex < Constants.NUM_SUITS; suitIndex++) {
			if(suitIndex == Constants.SPADE) {
				continue;
			}
			
			for(int rank = DataModel.ACE; rank >=DataModel.RANK_TWO; rank--) {
				String tmpCard = DataModel.getCardString(rank, suitIndex);
				if(dataModel.hasCard(tmpCard)) {
					ret--;
				} else if(dataModel.isCardPlayedInRound(tmpCard)) {
					continue;
				} else {
					break;
				}
			}
		}
		
		
		return ret;
	}
	
	
	public static boolean shouldNotTrumpBeforeLHSBecauseLHSWillMessYouUp(DataModel dataModel, int leaderSuitIndex) {
		return (   dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, leaderSuitIndex)
			       && ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
			       && ! dataModel.currentPlayerHasMasterInSuit(Constants.SPADE)
			       && dataModel.signalHandler.getMaxRankSpadeSignalled(Constants.LEFT_PLAYER_INDEX) >
				   		DataModel.getRankIndex(dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE))
				   
				   //Don't fight LHS:
				   && (
						   (
								   dataModel.getNumberOfCardsPlayerPlayedInSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE) < 2
								   && dataModel.getBid(Constants.LEFT_PLAYER_INDEX) <= 4
								   && dataModel.getNumTricks(Constants.LEFT_PLAYER_INDEX) < dataModel.getBid(Constants.LEFT_PLAYER_INDEX)
						   )
						   ||
						   (
								   dataModel.getNumberOfCardsPlayerPlayedInSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE) < 3
								   && dataModel.getBid(Constants.LEFT_PLAYER_INDEX) <= 5
								   && dataModel.getNumTricks(Constants.LEFT_PLAYER_INDEX) < dataModel.getBid(Constants.LEFT_PLAYER_INDEX)
						   )
						    
					)
			     );
	}
	
	
	public static int getNumOtherPlayersTrumpingSpade(DataModel dataModel) {
		int numPlayersWithSpade = 0;
		if(! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)) {
			numPlayersWithSpade++;
		}
		if(! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)) {
			numPlayersWithSpade++;
		}
		if(! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)) {
			numPlayersWithSpade++;
		}
		
		return numPlayersWithSpade;
	}
	
	public static boolean hasAnOffsuitMaster(DataModel dataModel) {
		return (dataModel.currentPlayerHasMasterInSuit(1)
				|| dataModel.currentPlayerHasMasterInSuit(2)
				|| dataModel.currentPlayerHasMasterInSuit(3));
			
	}

	public static boolean hasAnOffsuitKingEqCouldMakeTrick(DataModel dataModel) {
		return (NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, 1)
				&& dataModel.getNumberOfCardsOneSuit(1) > 2
				)
			||
			(NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, 2)
					&& dataModel.getNumberOfCardsOneSuit(2) > 2
					)
			||
			(NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, 3)
					&& dataModel.getNumberOfCardsOneSuit(3) > 2
					);
	}

	public static boolean hasAnOffsuitQueenEqCouldMakeTrick(DataModel dataModel) {
		return (NonMellowBidHandIndicators.hasQEquivNoAorK(dataModel, 1)
				&& dataModel.getNumberOfCardsOneSuit(1) > 3
				)
			||
			(NonMellowBidHandIndicators.hasQEquivNoAorK(dataModel, 2)
					&& dataModel.getNumberOfCardsOneSuit(2) > 3
					)
			||
			(NonMellowBidHandIndicators.hasQEquivNoAorK(dataModel, 3)
					&& dataModel.getNumberOfCardsOneSuit(3) > 3
					);
	}

	public static boolean hasAnOffsuitKingQueenEqCouldMakeTrick(DataModel dataModel) {
		return (NonMellowBidHandIndicators.hasKQEquivAndNoAEquiv(dataModel, 1)
				
			||
			NonMellowBidHandIndicators.hasKQEquivAndNoAEquiv(dataModel, 2)
					
			||
			NonMellowBidHandIndicators.hasKQEquivAndNoAEquiv(dataModel, 3)
					
		);
	}
	
	public static boolean partnerIsTrumpingSuitWeCouldLead(DataModel dataModel) {
		
		if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)) {
			return false;
		}
		
		for(int s=0; s<Constants.NUM_SUITS; s++) {
			if(s==Constants.SPADE || dataModel.currentAgentHasSuit(s) == false) {
				continue;
			}
			
			if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, s)
					&& !dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, s)
					&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(s) >= 1) {
				return true;
			}
		}
		
		return false;
	}
}

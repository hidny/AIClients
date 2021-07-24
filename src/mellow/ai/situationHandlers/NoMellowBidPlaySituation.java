package mellow.ai.situationHandlers;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.cardDataModels.handIndicators.NonMellowBidHandIndicators;
import mellow.ai.cardDataModels.normalPlaySignals.MellowVoidSignalsNoActiveMellows;
import mellow.ai.simulation.MonteCarloMain;
import mellow.ai.situationHandlers.objects.CardAndValue;
import mellow.cardUtils.CardStringFunctions;
import mellow.cardUtils.DebugFunctions;

public class NoMellowBidPlaySituation {

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
		}
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

		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "9S 8S 6C")) {
			System.out.println("DEBUG");
		}

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
		
		return bestCardToPlay;
	}
	
	public static CardAndValue AILeaderThrowGetSpadeValue(DataModel dataModel) {
		
		int numCardsOfSuitInHand = dataModel.getNumCardsOfSuitInCurrentPlayerHand(Constants.SPADE);
		int numCardsOfSuitOtherPlayersHave =
		dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE);
		

		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "9S 8S 6C")) {
			System.out.println("Debug");
		}
	
		String cardToPlay = null;
		
		double curScore = 0.0;
		
		boolean partnerSignalledHighCardOfSuit = false;
		
		
		if(dataModel.currentPlayerHasMasterInSuit(Constants.SPADE)) {
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
				&& ! NonMellowBidHandIndicators.hasKQEquiv(dataModel, Constants.SPADE)) {
			curScore -= 55.0;
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
			
		} else {
			curScore += 5.0;
			cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
			
		}

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
		if(NonMellowBidHandIndicators.hasKEquiv(dataModel, Constants.SPADE)
				&& !NonMellowBidHandIndicators.hasKQEquiv(dataModel, Constants.SPADE)
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
			if(dataModel.getTrick(Constants.CURRENT_AGENT_INDEX)
				< dataModel.getBid(Constants.CURRENT_AGENT_INDEX)
				|| dataModel.currentPlayerHasMasterInSuit(Constants.SPADE)) {
				curScore += 30.0;
			}
			
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
			if(NonMellowBidHandIndicators.hasKQEquiv(dataModel, Constants.SPADE)) {
				
				cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
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
			    && ! dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
			    && ! dataModel.currentPlayerHasMasterInSuit(Constants.SPADE)) {
			curScore -= 40.0;
		}
		
		int bidDiff = (dataModel.getBid(Constants.CURRENT_PLAYER_INDEX) + dataModel.getBid(Constants.CURRENT_PARTNER_INDEX))
		- (dataModel.getBid(Constants.LEFT_PLAYER_INDEX) + dataModel.getBid(Constants.RIGHT_PLAYER_INDEX));
		//Rough way to look at the bids to decide whether or not to play spade
		
		if(bidDiff > 0) {
			//Limit how much of a benefit this is because it's covered elsewheres:
			curScore += 5.0 * Math.min(4, bidDiff);
		} else {
			curScore += 5.0 * Math.max(-3, bidDiff);
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
		}

		return new CardAndValue(cardToPlay, curScore);
	}

	public static CardAndValue AILeaderThrowGetOffSuitValue(DataModel dataModel, int suitIndex) {
		

		
		int numCardsOfSuitInHand = dataModel.getNumCardsOfSuitInCurrentPlayerHand(suitIndex);
		int numCardsOfSuitOtherPlayersHave =
		dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex);
		

		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "9S 8S 6C")) {
			System.out.println("DEBUG");
		}
	
		String cardToPlay = null;
		
		double curScore = 0.0;
		
		boolean partnerSignalledHighCardOfSuit = false;
		
		
		//Don't want to lead S if you're the only one with it... unless you have a plan...
		if(dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE) == 0
				&& dataModel.currentAgentHasSuit(Constants.SPADE)) {
			
			boolean dontLeadSuit = false;
			
			//If current play has Kequiv and nothing else, hope that another player will throw off Aequiv later:
			if(   (NonMellowBidHandIndicators.hasKEquiv(dataModel, suitIndex)
					&& numCardsOfSuitInHand == 1)
				||
				  (numCardsOfSuitInHand >= 2
				    && numCardsOfSuitOtherPlayersHave >= 4
				    && ! dataModel.currentPlayerHasMasterInSuit(suitIndex)
				    && ! NonMellowBidHandIndicators.hasKEquiv(dataModel, suitIndex)
				    )
				) {
				dontLeadSuit = true;
			}
			if(dontLeadSuit == false) {
				curScore += 50.0;
			}
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

		} else if(dataModel.signalHandler.partnerHasMasterBasedOnSignals(suitIndex)) {
			
			//System.out.println("TEST " + suitIndex);
			curScore += 9.5;
			partnerSignalledHighCardOfSuit = true;
			
			//Don't want to setup our partner for failure:
			if(numCardsOfSuitOtherPlayersHave - 1 == 0 && 
					dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
	                  (Constants.CURRENT_PARTNER_INDEX, suitIndex) == false) {
				curScore -= 100.0;
			}
			
			//Treat partner's possible master as your own...
		} else if(dataModel.signalHandler.playerSignalledHighCardInSuit(Constants.CURRENT_PARTNER_INDEX, suitIndex)
				|| dataModel.signalHandler.getPlayerIndexOfKingSacrificeForSuit(suitIndex) == Constants.CURRENT_PARTNER_INDEX) {
			curScore += 9.0;
			partnerSignalledHighCardOfSuit = true;
			
			//Don't want to setup our partner for failure:
			if(numCardsOfSuitOtherPlayersHave - 1 == 0 && 
					dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
	                  (Constants.CURRENT_PARTNER_INDEX, suitIndex) == false) {
				curScore -= 100.0;
			}
		
			//Don't mind not messing up your partner's K (Didn't really help...)
		} else if(dataModel.isCardPlayedInRound(dataModel.getCardString(DataModel.ACE, suitIndex))) {
			curScore += 5.0;
		}
		
		
		//Leading in a suit where RHS prob has master could be a good idea sometimes...
		//Like when partner is trumping
		//or partner has no spade.
		if(      ! dataModel.currentPlayerHasMasterInSuit(suitIndex)
				&& (dataModel.signalHandler.playerSignalledHighCardInSuit(Constants.RIGHT_PLAYER_INDEX, suitIndex)
						|| dataModel.signalHandler.getPlayerIndexOfKingSacrificeForSuit(suitIndex) == Constants.RIGHT_PLAYER_INDEX)
						
				&& ! NonMellowBidHandIndicators.hasKQEquiv(dataModel, suitIndex)) {
			curScore -= 55.0;
		}
		
		//Start of logic that's only for considering playing offsuits:
	
			
		//Check if leading suitIndex helps partner trump:
		if(
				(       (dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, suitIndex)
						|| dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
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

				} else {
					
					curScore += 70;

				}
				
				if(dataModel.currentPlayerHasMasterInSuit(suitIndex)) {
					
					cardToPlay = dataModel.getMasterInHandOfSuit(suitIndex);
					
				} else if(NonMellowBidHandIndicators.hasKQEquiv(dataModel, suitIndex)) {
					//It's slightly controversial to not play lowest when partner is trumping, but I like the agro play...
					cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex);
				
				} else {
					cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(suitIndex);
				}
				
		} else if(dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE) == 0
				&& dataModel.getNumCardsOfSuitInCurrentPlayerHand(Constants.SPADE) == 0
				&& dataModel.currentPlayerHasMasterInSuit(suitIndex)) {
			
			cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex);
			
			//The in-between jackpot:
			curScore += 1100;
				
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
				curScore -= 30;
				
				if(numCardsOfSuitInHand > 1
						&& dataModel.signalHandler.playerAlwaysFollowedSuit
						(Constants.RIGHT_PLAYER_INDEX, suitIndex)) {
					//Don't lead this suit if partner might think they can trump without getting trumped over.
					curScore -= 70;
				}
				
			}
			
				
		} else {
			
			if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
					(Constants.LEFT_PLAYER_INDEX, suitIndex)

					&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
						(Constants.CURRENT_PARTNER_INDEX, suitIndex) == false

					&& numCardsOfSuitOtherPlayersHave > 3) {
						curScore -= 5.0 * (numCardsOfSuitOtherPlayersHave);
			}
			
			
			
			if( (dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
					(Constants.RIGHT_PLAYER_INDEX, suitIndex) == false
				  && dataModel.signalHandler.getPlayerIndexOfKingSacrificeForSuit(suitIndex) != Constants.RIGHT_PLAYER_INDEX
				)
				|| dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)
				) {
				//increased to 26.0 to fix testcase Michael2021 -> 290...
				curScore += 26.0;
				
				if(dataModel.didPlayerIndexLeadMasterAKOffsuit(Constants.RIGHT_PLAYER_INDEX, suitIndex)
						&& ! dataModel.didPlayerIndexLeadMasterAKOffsuit(Constants.LEFT_PLAYER_INDEX, suitIndex)) {
					//Made it more than +10, because I wanted to beat
					//playing spade with bid diff of 1 in our favour.
					curScore += 12.0;
				}
				
			} else {
				curScore -= 50.0;
			}
			
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
				curScore -= 50;
			}
			
			if( dataModel.currentPlayerHasMasterInSuit(suitIndex)) {
				//Don't lower it...

			} else if(partnerSignalledHighCardOfSuit) {
				//Don't lower it again

			} else if(NonMellowBidHandIndicators.hasKQEquiv(dataModel, suitIndex)) {
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
				
				//Changed to -30, because doing this is better than feed RHS a suit RHS is void in.
				curScore -= 30.0;
				
			}
			
			//Play the king if it's alone
			if(dataModel.getRankIndex(dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex)) == dataModel.KING
					&& numCardsOfSuitInHand == 1
					&& dataModel.isMasterCard(dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex)) == false) {
				
				curScore += 50.0;
				
			}
			
			//Consider playing suits that others have a lot of so you are less likely to be trumped
			if(3.0 * dataModel.getNumCardsOfSuitInCurrentPlayerHand(suitIndex) - numCardsOfSuitOtherPlayersHave  <= 1

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
					curScore += Math.min(5.0 * numCardsLessThanAvgOther, 30);
				}
			}
			
			
			if(dataModel.currentPlayerHasMasterInSuit(suitIndex)) {
				
				cardToPlay = dataModel.getMasterInHandOfSuit(suitIndex);
			} else if(NonMellowBidHandIndicators.hasKQEquiv(dataModel, suitIndex)) {
				cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex);
				
			} else if(NonMellowBidHandIndicators.hasKEquiv(dataModel, suitIndex)) {
				
				if(numCardsOfSuitInHand > 1) {
					cardToPlay = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(suitIndex);
				} else {
					cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(suitIndex);
				}
			//TODO: special consideration for QEquiv??
				
			} else {
				cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex);
			}
			
			
			//I like leading offsuit queens... if no one is void and I don't have a hope of winning with it.
			if(dataModel.getRankIndex(dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex)) == DataModel.QUEEN
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
					
					cardToPlay = DataModel.getCardString(DataModel.QUEEN, suitIndex);

					if(dataModel.hasCard( DataModel.getCardString(DataModel.JACK, suitIndex))) {
						curScore += 10.0;
					}
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
		//START REALLY OLD CODE:
		//SEE NOTES FOR BETTER PLAN
		
		//TODO: pseudo code for not following suit
	
		
		//TODO: only deal with string (No index)
		int leaderSuitIndex = dataModel.getSuitOfLeaderThrow();
		String leaderCard = dataModel.getCardLeaderThrow();
		
		//TODO currentAgentHasSuit and isVoid does the same thing...?
		if(dataModel.currentAgentHasSuit(leaderSuitIndex)) {
			
			if(dataModel.couldPlayCardInHandOverCardInSameSuit(leaderCard)) {
				
				boolean thirdVoid = dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, leaderSuitIndex);
				//boolean fourthVoid = dataModel.isVoid(Constants.CURRENT_PARTNER_INDEX, leaderSuitIndex);
				//boolean thirdVoid = dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, leaderSuitIndex);
				boolean fourthProbVoid = dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, leaderSuitIndex);
				
				if(thirdVoid && fourthProbVoid) {	
					cardToPlay = dataModel.getCardInHandClosestOverSameSuit(dataModel.getCardLeaderThrow());

				} else if(thirdVoid && fourthProbVoid == false) {
					//Maybe play low? I don't know...
					cardToPlay = dataModel.getCardInHandClosestOverSameSuit(dataModel.getCardLeaderThrow());
				
				} else if(thirdVoid == false && fourthProbVoid) {
					//TODO This doesn't really work if trump is spade... 
					
					cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(leaderSuitIndex);
					
				} else if(thirdVoid == false && fourthProbVoid == false){
					
					if(dataModel.currentPlayerHasMasterInSuit(leaderSuitIndex)) {
						cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(leaderSuitIndex);
					} else {
						
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
									&& (    NonMellowBidHandIndicators.hasKEquiv(dataModel, Constants.SPADE)
									    || (NonMellowBidHandIndicators.hasQEquiv(dataModel, Constants.SPADE)
									    		&& dataModel.getNumCardsOfSuitInCurrentPlayerHand(Constants.SPADE) >= 3)
									    || dataModel.getNumCardsOfSuitInCurrentPlayerHand(Constants.SPADE) >= 4)) {
								
								//Play low for and don't challenge if you want to preserve highish spade:
								//There might be some complicated exceptions, but whatever.
								return dataModel.getCardCurrentPlayerGetLowestInSuit(leaderSuitIndex);

							} else if(dataModel.cardAGreaterThanCardBGivenLeadCard(cardToPlay, leaderCard)) {
								
								return cardToPlay;
								
							} else {
								//Play the King while the Ace is still out!
								return curPlayerTopCardInSuit;
							}
						
											
						} else if(dataModel.throwerHasCardToBeatCurrentWinner()) {
							
							//Consider playing high second if there's no real chance of making the trick,
							// but you want 3rd thrower to prove they can play higher than you:
							if(leaderSuitIndex != Constants.SPADE
								&& (dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(curPlayerTopCardInSuit) >= 2)
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
								if(NonMellowBidHandIndicators.hasKQEquiv(dataModel, Constants.SPADE)) {
									return cardToConsider;
								
								} else if(dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE).equals(cardToConsider)
										&& (NonMellowBidHandIndicators.hasKEquiv(dataModel, Constants.SPADE) || NonMellowBidHandIndicators.hasQEquiv(dataModel, Constants.SPADE))) {
									

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
									
							
							//If you have more spades than the avg player, let partner trump
							&& 3 * dataModel.getNumberOfCardsOneSuit(Constants.SPADE) > 
					               dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE)
							
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
						
						String consideredHighTrump = dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
						
						int maxRankLHS = dataModel.signalHandler.getMaxRankSpadeSignalled(Constants.LEFT_PLAYER_INDEX);

						if(maxRankLHS == MellowVoidSignalsNoActiveMellows.MAX_UNDER_RANK_2) {
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
						//dataModel.getCardInHandClosestOverSameSuit(leaderCard)
						
						//TODO: June28th: be more willing to play 'consideredHighTrump' if it's not a master card.
						
						if( dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(consideredHighTrump)
								>= dataModel.getNumberOfCardsOneSuit(Constants.SPADE)) {
							
							if(dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(leaderSuitIndex) >= 7
									&& (! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, leaderSuitIndex)
											|| dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, leaderSuitIndex))
									) {
								//Play low because you don't expect to be trumped over:
								cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
								
							} else {
								//Play high because if you're trumped over, at least they needed to throw a high one
								cardToPlay = consideredHighTrump;
								
							}
							
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
						)
						
						&& //No K to protect:
						 ! (dataModel.getNumberOfCardsOneSuit(Constants.SPADE) == 2
						           && NonMellowBidHandIndicators.hasKEquiv(dataModel, Constants.SPADE))
						 && //No Q to protect:
						 ! (dataModel.getNumberOfCardsOneSuit(Constants.SPADE) == 3
				                   && NonMellowBidHandIndicators.hasQEquiv(dataModel, Constants.SPADE))
						) {		

					
					//if(dataModel.)
					cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);

					
					//if(dataModel.isEffectivelyMasterCardForPlayer(Constants.CURRENT_AGENT_INDEX, cardToPlay)) {
					//	cardToPlay = getJunkiestCardToFollowLead(dataModel);
					//}
					
				//Partner is useless, so trump
				} else if (dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)
					      && dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, leaderSuitIndex)){ 

					cardToPlay = dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
				} else {
					cardToPlay = getJunkiestCardToFollowLead(dataModel);
				}
				
				
				
				//At this point, we might have decided to trump,
				// but we might reconsider given LHS is also trumping...

				boolean goBigOrGoHome = false;
				if(dataModel.signalHandler.mellowPlayerSignalNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, leaderSuitIndex)
								&& ! dataModel.signalHandler.mellowPlayerSignalNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
								&& dataModel.getTrick(Constants.LEFT_PLAYER_INDEX) < dataModel.getBid(Constants.LEFT_PLAYER_INDEX)) {
					goBigOrGoHome = true;
				}
				
				if(goBigOrGoHome
						&& CardStringFunctions.getIndexOfSuit(cardToPlay) == Constants.SPADE) {
					
					int numSpadesInHand = dataModel.getNumberOfCardsOneSuit(Constants.SPADE);
					
					if((3 * numSpadesInHand >
					dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE)
					      && numSpadesInHand > 1
					)
					|| (numSpadesInHand == 2
						&&	!dataModel.currentPlayerHasMasterInSuit(Constants.SPADE)
						&&	NonMellowBidHandIndicators.hasKEquiv(dataModel, Constants.SPADE)
						&& !NonMellowBidHandIndicators.hasKQEquiv(dataModel, Constants.SPADE)
						)
					|| (numSpadesInHand == 3
							&& !dataModel.currentPlayerHasMasterInSuit(Constants.SPADE)
							&& !NonMellowBidHandIndicators.hasKEquiv(dataModel, Constants.SPADE)
							&& NonMellowBidHandIndicators.hasQEquiv(dataModel, Constants.SPADE)
						)
					) {
						cardToPlay = getJunkiestCardToFollowLead(dataModel);
					} else {
						
						String highcard = dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
						
						if(dataModel.isMasterCard(dataModel.getCurrentFightWinningCardBeforeAIPlays())
								&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(highcard) >= 1) {
							
							//TODO: maybe handle signals for min spade LHS has...
								//&& ! dataModel.signalHandler.NOTIMPLEMENTED
							cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
							
						} else {
							cardToPlay = getJunkiestCardToFollowLead(dataModel);
						}
						
					}
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
							
							//Try to play spade high enough that LHS can't play over:
							//TODO: put in seperate function
							String consideredHighTrump = dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
							
							int maxRankLHS = dataModel.signalHandler.getMaxRankSpadeSignalled(Constants.LEFT_PLAYER_INDEX);

							if(maxRankLHS == MellowVoidSignalsNoActiveMellows.MAX_UNDER_RANK_2) {
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

		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "JH 7H TD")) {
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
			
			//END DEBUG

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
			
			
			
			if(numberOfCardsInSuit == 1 &&  dataModel.currentPlayerHasMasterInSuit(suitIndex)
					&& (dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex) > 0 
							//TODO: implement indicator functions to estimaste odds of making a trick...
					  ||  dataModel.getTrick(Constants.CURRENT_AGENT_INDEX) < dataModel.getBid(Constants.CURRENT_AGENT_INDEX)
					  		//Simple check for spades (It fixed the test case!): (TODO: make more complex)
					  || 3 * dataModel.getNumberOfCardsOneSuit(Constants.SPADE) > dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE) )
					) {
				//Don't throw off master cards unless you really need to...
				//Or the idea of leading it is unrealistic.
				currentValue -= 20.0;
				
				//Maybe it's not bad if you're planing on trumping though...
				
			} else if( (numberOfCardsInSuit == 2 || numberOfCardsInSuit == 3)
					&&  NonMellowBidHandIndicators.hasKEquiv(dataModel, suitIndex)
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
		
		return dataModel.getCardCurrentPlayerGetLowestInSuit(bestSuit);
	}
}

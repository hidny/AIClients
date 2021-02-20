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
		
		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "TS 4S 3H 8C 6C TD")) {
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
					
					//Play barely over max card mellow player signaled they have...
					//This strategy might be exploitable, but it takes a lot of imagination on the mellow player's part.
					
					if(dataModel.signalHandler.mellowPlayerSignalNoCardsOfSuit
							(MELLOW_PLAYER_INDEX, dataModel.getSuitOfLeaderThrow())) {
						
						//lazy approx:
						return getHighestPartOfGroup(dataModel, NoMellowBidPlaySituation.handleNormalThrow(dataModel));

					} else {

							String currentFightWinner = dataModel.getCurrentFightWinningCardBeforeAIPlays();
							
							String minCardToWin = null;
							if(CardStringFunctions.getIndexOfSuit(currentFightWinner) == dataModel.getSuitOfLeaderThrow()
									&& dataModel.couldPlayCardInHandOverCardInSameSuit(currentFightWinner)) {
								
								if(throwIndex == 3) {
									minCardToWin = dataModel.getCardInHandClosestOverCurrentWinner();
								} else if(throwIndex >= 1 && throwIndex <= 2) {
									
									//Just try to play over maxMellowCard if it's the 3rd throw
									
									minCardToWin = null;
								}
							}

							String minCardOverMaxMellowCard = null;
							String maxMellowCard = dataModel.signalHandler.getMaxRankCardMellowPlayerCouldHaveBasedOnSignals
									(MELLOW_PLAYER_INDEX, dataModel.getSuitOfLeaderThrow());
							
					
							if(dataModel.couldPlayCardInHandOverCardInSameSuit(maxMellowCard)) {
								minCardOverMaxMellowCard = dataModel.getCardInHandClosestOverSameSuit(maxMellowCard);
							}
							
							if(minCardToWin != null && minCardOverMaxMellowCard != null) {

								if( dataModel.cardAGreaterThanCardBGivenLeadCard(minCardToWin, minCardOverMaxMellowCard)) {
									return getHighestPartOfGroup(dataModel, minCardToWin);
								} else {
									return getHighestPartOfGroup(dataModel,
											minCardOverMaxMellowCard);
								}

							} else if(minCardToWin != null && minCardOverMaxMellowCard == null) {
								return getHighestPartOfGroup(dataModel, minCardToWin);

							} else if(minCardToWin == null && minCardOverMaxMellowCard != null) {
								return getHighestPartOfGroup(dataModel, 
										minCardOverMaxMellowCard);

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
	
	
	public static String AIHandleLead(DataModel dataModel) {
		
		//TODO: make it different later:
		return SeatedRightOfOpponentMellow.AIHandleLead(dataModel);
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
}

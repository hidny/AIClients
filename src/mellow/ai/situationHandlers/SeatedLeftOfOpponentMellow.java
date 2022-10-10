package mellow.ai.situationHandlers;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.cardDataModels.handIndicators.NonMellowBidHandIndicators;
import mellow.ai.situationHandlers.bidding.BiddingNearEndOfGameFunctions;
import mellow.cardUtils.CardStringFunctions;
import mellow.cardUtils.DebugFunctions;

public class SeatedLeftOfOpponentMellow {

	

	public static int MELLOW_PLAYER_INDEX = 3;
	public static int PROTECTOR_PLAYER_INDEX = 1;
	
	public static String playMoveSeatedLeftOfOpponentMellow(DataModel dataModel) {

		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "TS 8S QD")) {
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
			

			
			String curWinningCard = dataModel.getCurrentFightWinningCardBeforeAIPlays();
			
			int leadSuit = dataModel.getSuitOfLeaderThrow(); 
			
			if(CardStringFunctions.getIndexOfSuit(curWinningCard) 
					!= leadSuit) {
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
								&& dataModel.currentPlayerHasMasterInSuit(leadSuit) == false
								&& dataModel.getNumberOfCardsOneSuit(leadSuit) >= 3) {
							
							
							//If responding to the mellow lead, don't waste a high one...
							//Maybe I'll have more cases later...
							return dataModel.getCardCurrentPlayerGetSecondHighestInSuit(leadSuit);
						
							/*
						//Speculative code that works for 3-1958, but fails for 0-473
						} else if(throwIndex == 2
								&& dataModel.getNumberOfCardsOneSuit(leadSuit) >= 3
								&& (dataModel.signalHandler.getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(MELLOW_PLAYER_INDEX, leadSuit) != null
										&&
										dataModel.getNumCardsInPlayBetweenCardSameSuit(
										dataModel.getCardCurrentPlayerGetSecondHighestInSuit(leadSuit),
										dataModel.signalHandler.getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(MELLOW_PLAYER_INDEX, leadSuit))
												>= 3)
								&& dataModel.getNumCardsInPlayBetweenCardSameSuit(
										dataModel.getCardCurrentPlayerGetHighestInSuit(leadSuit),
										dataModel.getCardCurrentPlayerGetSecondHighestInSuit(leadSuit))
												> 1
								&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(dataModel.getCardCurrentPlayerGetSecondHighestInSuit(leadSuit))
												>= 3
								) {
							
							//In some cases, dare mellow protector to play over you:
								
							return dataModel.getCardCurrentPlayerGetSecondHighestInSuit(dataModel.getSuitOfLeaderThrow());
									
						*/

						} else {
							return dataModel.getCardCurrentPlayerGetHighestInSuit(dataModel.getSuitOfLeaderThrow());
						}
					}
					
				} else {
					
					
					//If can't follow suit, don't trump unless you only have spade, or mellow is safe.
					if(dataModel.currentPlayerOnlyHasSpade()) {
						//play biggest spade if have no choice:
						//over simplified, but whatever...
						return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
					} else {
						
						//In some cases, trump on mellow anyways:
						if(throwIndex == 2
							&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(curWinningCard) == 0
							&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(curWinningCard) > 3
							&& dataModel.currentAgentHasSuit(Constants.SPADE)
							&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(
									Constants.LEFT_PLAYER_INDEX, 
									dataModel.getSuitOfLeaderThrow())
							) {
							
							return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
						
						//Later: Maybe add more cases later...
						//Something like: protector wasn't forced to play high and 1 under and 6+ over...
						
						} else {
							//Don't trump!
							//play big off suit to mess-up mellow play (Over-simplified, but whatever)
							return throwOffHighCardThatMightAccidentallySaveMellowAndTryToAvoidThrowingMasters(dataModel, MELLOW_PLAYER_INDEX);
						}
						
					}
				
				}
			}

			//End go under mellow if possible logic
			
			
		} else if(throwIndex > 0 && 
				dataModel.isPrevThrowWinningFight() == false) {

			//handle case where mellow is already safe:

			

			if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "7S 4S 2S AH 9D 7D")) {
				System.out.println("Debug");
			}

			//TODO: the logic is completely messed up!
		
			if(dataModel.throwerMustFollowSuit()) {
				
				String curWinningCard =  dataModel.getCurrentFightWinningCardBeforeAIPlays();
				int leadSuitIndex = dataModel.getSuitOfLeaderThrow();
				String highestCardOfSuit = dataModel.getCardCurrentPlayerGetHighestInSuit(leadSuitIndex);
				
				if(dataModel.isVoid(MELLOW_PLAYER_INDEX, dataModel.getSuitOfLeaderThrow())
						|| dataModel.signalHandler.mellowBidderPlayerSignalNoCardsOfSuit
									(MELLOW_PLAYER_INDEX, leadSuitIndex)) {
					
					//lazy approx for when mellow bidder doesn't have suit:
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
					
						String highCard = dataModel.getCardCurrentPlayerGetHighestInSuit(dataModel.getSuitOfLeaderThrow());
						String barelyOver = getHighestPartOfGroup(dataModel, dataModel.getCardInHandClosestOverCurrentWinner());
						//Don't automatically play master card if you're the last one to play:
						if(throwIndex == 3
								&& dataModel.isMasterCard(highCard)
								&& dataModel.getNumberOfCardsOneSuit(leadSuitIndex) >= 4
								&& ! barelyOver.equals(highCard)) {
							
							//Fixes 3-2796 only
							return barelyOver;
							
						} else {
							return highCard;
						}
					
						
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
						
						int numOverMin = dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(
								dataModel.signalHandler.getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(MELLOW_PLAYER_INDEX, leadSuitIndex)
							);
						
						int projectedScores[] = BiddingNearEndOfGameFunctions.getProjectedScoresAssumingTheWorst(dataModel, dataModel.getBid(Constants.CURRENT_AGENT_INDEX));
						
						//Make an exception for spades:
						// (Don't waste the KS just because!)
						if(minCardToPlayWithNoDamageToFutureMellowBurnOdds != null
								&& dataModel.cardAGreaterThanCardBGivenLeadCard(currentFightWinner, minCardToPlayWithNoDamageToFutureMellowBurnOdds)
								
								//Apparently, if the AS is played in the current round, it's still considered 'InPlay':
								&& dataModel.getNumCardsInPlayOverCardSameSuit(minCardToPlayWithNoDamageToFutureMellowBurnOdds) == 1
								&& ((numOverMin
										>= 4
										&& dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) < 5)
									||
									(numOverMin >= 2
									&&
									dataModel.getBid(PROTECTOR_PLAYER_INDEX) >= 5)
								)
								&&
								dataModel.getNumCardsInCurrentPlayersHandUnderCardSameSuit(minCardToPlayWithNoDamageToFutureMellowBurnOdds)
									>= 1
								//Near end-of-game exception: (Make sure the opposing team won't win with the mellow:
								&& (projectedScores[1] < Constants.GOAL_SCORE
										|| projectedScores[0] >= projectedScores[1]
									)
								) {
							//Make the minCard with no Damage not be a future master card if possible.
							//Fixes D-6030 only
							minCardToPlayWithNoDamageToFutureMellowBurnOdds = dataModel.getCardInHandClosestUnderSameSuit(minCardToPlayWithNoDamageToFutureMellowBurnOdds);
						}
						
						
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
							
							
							//fourthThrowMinCardToWin == null && minCardToPlayWithNoDamageToFutureMellowBurnOdds == null:
						} else {
							
							//Consider not wasting a king:
							if(! dataModel.currentPlayerHasMasterInSuit(leadSuitIndex)
									&& dataModel.getNumberOfCardsOneSuit(leadSuitIndex) >= 4
									&& NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, leadSuitIndex)) {
								return dataModel.getCardCurrentPlayerGetSecondHighestInSuit(dataModel.getSuitOfLeaderThrow());
							
							} else {
								
								if(     //Make sure we're allowed to play over winner card:
										(dataModel.getSuitOfLeaderThrow() == CardStringFunctions.getIndexOfSuit(currentFightWinner)
										|| dataModel.isVoid(Constants.CURRENT_AGENT_INDEX, leadSuitIndex))

										&& dataModel.couldPlayCardInHandUnderCardInSameSuit(currentFightWinner)
										&& dataModel.couldPlayCardInHandOverCardInSameSuit(currentFightWinner)
										&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit(dataModel.getCardInHandClosestOverCurrentWinner(),
																						  dataModel.getCardInHandClosestUnderSameSuit(currentFightWinner))
										== 0
										&& PartnerSaidMellowSituation.getLowestCardOfGroupOfCardsOverAllSameNumCardsInOtherPlayersHandOfSuit(dataModel, dataModel.getCardCurrentPlayerGetHighestInSuit(dataModel.getSuitOfLeaderThrow()))
											.equals(
											 PartnerSaidMellowSituation.getLowestCardOfGroupOfCardsOverAllSameNumCardsInOtherPlayersHandOfSuit(dataModel, dataModel.getCardInHandClosestOverCurrentWinner())
											)
										) {
											//Let partner take it if you have the card directly over and under:
											//And highest card is the one over (or in the same group)
											return dataModel.getCardInHandClosestUnderSameSuit(dataModel.getCardInHandClosestOverCurrentWinner());
								}
								//Just play the highest:
								return dataModel.getCardCurrentPlayerGetHighestInSuit(dataModel.getSuitOfLeaderThrow());
							}
							
						}

						//END TODO: PUT INTO FUNCTION -- 3242
				}
				
			} else if(dataModel.currentAgentHasSuit(Constants.SPADE)
					
						//Basic check to see if we could let our partner take the trick because we need tricks:
					&& ! (dataModel.getIndexOfCurrentlyWinningPlayerBeforeAIPlays() == Constants.CURRENT_PARTNER_INDEX
							&& throwIndex == 3
							&& dataModel.getSumBidsCurTeam() > dataModel.getNumTricksCurTeam()
							//TODO: maybe check if there's something to lead?
							)
					) {
				
				if(SeatedRightOfOpponentMellow.gotNothingThreatningMellowToLead(dataModel, MELLOW_PLAYER_INDEX)
						&& dataModel.getNumberOfCardsOneSuit(Constants.SPADE) + 2 < dataModel.getNumCardsInCurrentPlayerHand()
						//Check if protector needs tricks:
						&& dataModel.getNumTricksOtherTeam() + dataModel.getNumCardsInCurrentPlayerHand() - dataModel.currentPlayerGetNumMasterSpadeInHand() -2 
						>= dataModel.getSumBidsOtherTeam()
						
						//Check if we could attack protector:
						&&
						! (dataModel.getNumTricksOtherTeam() < dataModel.getSumBidsOtherTeam()
								&& dataModel.getNumCardsInCurrentPlayerHand() - dataModel.currentPlayerGetNumMasterSpadeInHand() <= 3
								&& (dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(dataModel.getCurrentFightWinningCardBeforeAIPlays()) > 2
										|| (dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(dataModel.getCurrentFightWinningCardBeforeAIPlays()) >= 1
										    && dataModel.isVoid(MELLOW_PLAYER_INDEX, dataModel.getSuitOfLeaderThrow())
										    )
									)	
								)
					) {
					
					//TODO: maybe make sure protector is not motivated to lead a specific offsuit?
					//Don't bother trumping if you've got nothing to lead...
					
					return throwOffHighCardThatMightAccidentallySaveMellowAndTryToAvoidThrowingMasters(dataModel, MELLOW_PLAYER_INDEX);
				}
				
				String maxMellowSpade =dataModel.signalHandler.getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(MELLOW_PLAYER_INDEX, Constants.SPADE);
				
				if( maxMellowSpade == null) {
					//return lowest spade because mellow signalled no spade:
					return dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
				
				} else if(DataModel.getRankIndex(maxMellowSpade) < DataModel.QUEEN
						&& dataModel.couldPlayCardInHandOverCardInSameSuit(maxMellowSpade)
						&& dataModel.getNumCardsInHandUnderCardSameSuit(maxMellowSpade) <= 3) {

					//return closest over max mellow spade:
					return dataModel.getCardInHandClosestOverSameSuit(maxMellowSpade);
					
				} else if(dataModel.getNumberOfCardsOneSuit(Constants.SPADE) >= 5) {

					//return 4th lowest if you have tons of spade:
					return dataModel.getCardCurrentPlayergetFourthLowestInSuit(Constants.SPADE);
					
				} else {
					//return the highest spade so it won't protect mellow:
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
			} else if(dataModel.isMasterCard
						(getHighestPartOfGroup(dataModel, curCard)
					)) {
				curCard = dataModel.getCardCurrentPlayerGetHighestInSuit(curSuitIndex);
			}
			

			//Dealing with master cards:
			
			
			if(dataModel.isMasterCard(curCard)) {
				if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "KS 8S 3S 9H KC")) {
					System.out.println("Debug");
				}
			
				//TODO: Maybe don't do this if you really don't want tricks...
				if(dataModel.getNumCardsOfSuitInCurrentPlayerHand(curSuitIndex) >= 4) {
					
					curCard = dataModel.getCardCurrentPlayergetThirdLowestInSuit(curSuitIndex);
					
					//TODO: Should I really play KD instead of AD? I don't know...
					//Monte sort of says yes for now. (See 1-683)
					
					if(dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(curCard) ==0) {

						curValue -=25.0;
					}
				} else if(dataModel.getNumCardsOfSuitInCurrentPlayerHand(curSuitIndex) == 1){
					//Clear suit...
					
					if(dataModel.getNumTricksOtherTeam() < dataModel.getSumBidsOtherTeam() ) {
						curValue -=30.0;
					}
					
					if(dataModel.getNumTricksCurTeam() 
							+ dataModel.currentPlayerGetNumMasterSpadeInHand()
							< dataModel.getSumBidsCurTeam()) {
						//Don't throw master if you need the tricks...
						curValue -=70.0;
						//TODO: make an exception for when you're not making the tricks
						// or there's no way to make lead
						// or you know you're making the tricks...
					}
				
				} else {
					
					if(dataModel.getNumCardsOfSuitInCurrentPlayerHand(curSuitIndex) > 2
							&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit(dataModel.getCardCurrentPlayerGetHighestInSuit(curSuitIndex),
							dataModel.getCardCurrentPlayerGetThirdHighestInSuit(curSuitIndex)) == 0) {
						curValue -= 5.0;
						
					} else if(dataModel.getNumCardsOfSuitInCurrentPlayerHand(curSuitIndex) > 1
							&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit(dataModel.getCardCurrentPlayerGetHighestInSuit(curSuitIndex),					
									dataModel.getCardCurrentPlayerGetSecondHighestInSuit(curSuitIndex)) == 0) {
						
						curValue -=15.0;
					} else {
						curValue -=20.0;
					}
				}
				
				//Try to avoid throwing master cards that protector could take advantage of:
				if( ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit((mellowPlayerIndex + 2) % Constants.NUM_PLAYERS, curSuitIndex)
						&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(curSuitIndex) > 1
						
						) {
					
					if(dataModel.getNumberOfCardsOneSuit(curSuitIndex) == 1
							||
							dataModel.signalHandler.getMaxCardRankSignal((mellowPlayerIndex + 2) % Constants.NUM_PLAYERS, curSuitIndex)
							> DataModel.getRankIndex(dataModel.getCardCurrentPlayerGetSecondHighestInSuit(curSuitIndex))) {
						//Don't like throwing masters
							curValue -= 15.0;
							
						
						boolean shouldJustThrowMasters =  dataModel.getNumTricksOtherTeam() >= dataModel.getSumBidsOtherTeam()
						//TODO: copy pasted from Bidding Situation. (Make function with it)
								&& ( dataModel.getOpponentScore() > 850
										|| 1.5 * (1000 - dataModel.getOpponentScore()) < (1000 - dataModel.getOurScore())
										);
						//END TODO
						
						if((dataModel.getNumTricksCurTeam() < dataModel.getSumBidsCurTeam()
								|| dataModel.getNumTricksOtherTeam() < dataModel.getSumBidsOtherTeam())
							&&
							! shouldJustThrowMasters
								) {

							if(dataModel.getNumberOfCardsOneSuit(curSuitIndex) >= 2
									&& dataModel.getNumberOfCardsOneSuit(curSuitIndex) <= 4) {
								
								// TODO: Throw 2nd highest if it's under the master group of cards.
								curCard = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(curSuitIndex);
								
								if(dataModel.getNumberOfCardsOneSuit(curSuitIndex) == 4
										&& dataModel.getNumCardsInPlayBetweenCardSameSuitPossiblyWRONG(dataModel.getCardCurrentPlayerGetHighestInSuit(curSuitIndex),
												curCard) == 0) {
									curCard = dataModel.getCardCurrentPlayerGetThirdHighestInSuit(curSuitIndex);
								}
								
							} else if(dataModel.getNumberOfCardsOneSuit(curSuitIndex) >= 5){
								curCard = dataModel.getCardCurrentPlayerGetThirdHighestInSuit(curSuitIndex);
							}

						}
					}

				} else if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit((mellowPlayerIndex + 2) % Constants.NUM_PLAYERS, curSuitIndex)
						&& !dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit((mellowPlayerIndex + 2) % Constants.NUM_PLAYERS, Constants.SPADE)
						&& dataModel.getBid((mellowPlayerIndex + 2) % Constants.NUM_PLAYERS) >= 4
						&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE) >= 2) {

					//Don't waste a master on a suit the protector could easily trump:
					curValue -= 30.0;
				}
				
			}
						
			
			
			//End dealing with master cards
			
			//Dealing with kequiv
			if(! dataModel.currentPlayerHasMasterInSuit(curSuitIndex)
					&& dataModel.getNumCardsInPlayOverCardSameSuit(curCard) == 1
					&& dataModel.getNumberOfCardsOneSuit(curSuitIndex) >= 2 ) {
					
					if(dataModel.getNumCardsOfSuitInCurrentPlayerHand(curSuitIndex) >= 4) {
						curCard = dataModel.getCardCurrentPlayergetThirdLowestInSuit(curSuitIndex);
					
					} else if( ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit((mellowPlayerIndex + 2) % Constants.NUM_PLAYERS, curSuitIndex)
							&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(curSuitIndex) > 2
							
							) {
					
						if(dataModel.getNumberOfCardsOneSuit(curSuitIndex) == 2
								||
								dataModel.signalHandler.getMaxCardRankSignal((mellowPlayerIndex + 2) % Constants.NUM_PLAYERS, curSuitIndex)
								> DataModel.getRankIndex(dataModel.getCardCurrentPlayerGetSecondHighestInSuit(curSuitIndex))) {
							//Don't like throwing kequiv
								
								if(dataModel.getNumberOfCardsOneSuit(curSuitIndex) > 2) {
									
									if(dataModel.getNumCardsInPlayBetweenCardSameSuitPossiblyWRONG(
											dataModel.getCardCurrentPlayerGetHighestInSuit(curSuitIndex),
											dataModel.getCardCurrentPlayerGetSecondHighestInSuit(curSuitIndex))
									> 0 ) {
										curCard = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(curSuitIndex);
									
									} else if(dataModel.getNumberOfCardsOneSuit(curSuitIndex) <= 3) {
										curValue -= 10.0;
									}

								} else {
									//Only 2 cards left:
									curValue -= 15.0;
								}
						}
				
				
				}
			}
			
			
			if(dataModel.isVoid(mellowPlayerIndex, curSuitIndex)) {
				curValue -= 100.0;

			} else if(dataModel.signalHandler.mellowBidderPlayerSignalNoCardsOfSuit(mellowPlayerIndex, curSuitIndex)) {
				curValue -= 50.0;
				
			} else if(dataModel.signalHandler.mellowBidderSignalledNoCardUnderCardSameSuitExceptRank2(curCard, mellowPlayerIndex)) {
				curValue -= 48.0;

			} else if(NonMellowBidHandIndicators.hasKEquivNoAce(dataModel, curSuitIndex)
					&& ! NonMellowBidHandIndicators.hasKQEquivAndNoAEquiv(dataModel, curSuitIndex)) {
				
				//Also save your king equiv...
				//TODO: Maybe don't do this if you really don't want tricks...
				if(dataModel.getNumCardsOfSuitInCurrentPlayerHand(curSuitIndex) >= 4) {
					curCard = dataModel.getCardCurrentPlayergetThirdLowestInSuit(curSuitIndex);
				}

			} else if(
					NonMellowBidHandIndicators.hasQEquivNoAorK(dataModel, curSuitIndex)
					&& ! NonMellowBidHandIndicators.hasQJEquivAndNoAORKEquiv(dataModel, curSuitIndex)
					) {
				//Save your queen equiv so you could have some flexibility:
				//TODO: Maybe don't do this if you really don't want tricks...
				if(dataModel.getNumCardsOfSuitInCurrentPlayerHand(curSuitIndex) >= 4) {
					curCard = dataModel.getCardCurrentPlayergetThirdLowestInSuit(curSuitIndex);
				}
			}
			
			if(dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(curSuitIndex) == 0) {

				//Save last of suit to mess with protector and mellow. 
				if(mellowPlayerIndex == Constants.LEFT_PLAYER_INDEX) {
					//This only really works if mellow bidder is left of current player
					curValue -= 15.0;
					
					//TODO: make functions to do this:
				} else if(dataModel.getNumTricks(Constants.CURRENT_AGENT_INDEX) + dataModel.getNumTricks(Constants.CURRENT_PARTNER_INDEX)
									< dataModel.getBid(Constants.CURRENT_AGENT_INDEX) + dataModel.getBid(Constants.CURRENT_PARTNER_INDEX)
						|| dataModel.getNumTricks(Constants.RIGHT_PLAYER_INDEX) <  dataModel.getBid(Constants.RIGHT_PLAYER_INDEX)) {
					//OR: tricks are still contested.
					//(I didn't test it.)
				
					curValue -= 10.0;
				}
				
			} else if(dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(curSuitIndex) > 0
					&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, curSuitIndex)
					&& dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit((mellowPlayerIndex + 2) % Constants.NUM_PLAYERS, curSuitIndex)) {

				//Keep cards of suit that you only share with mellow player. This could help partner or protector drain spade.
				curValue -= 20.0;
				
			//Shouldn't like to throw off a high-card
			} else if(dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(curCard) < 3) {
				curValue += -4 * (1.5) + 1.5 * dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(curCard);
			}
			
			//Lower rank cards are less fun to throw:
			curValue += 0.9 * dataModel.getRankIndex(curCard);
			
			
			
			//2s and 3s don't really save a mellow:
			if(dataModel.getRankIndex(curCard) <= DataModel.RANK_THREE) {
				curValue -= 10.0;
			}
			
			//Want to trump seated right of mellow case: (trump in front of mellow)
			if(mellowPlayerIndex == 1
				&& dataModel.getNumCardsOfSuitInCurrentPlayerHand(Constants.SPADE) > 0
				) {
				if(dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(curSuitIndex) > 9
						&& dataModel.getNumCardsOfSuitInCurrentPlayerHand(curSuitIndex) <= 2
						&& dataModel.getNumberOfCardsPlayerPlayedInSuit(mellowPlayerIndex, curSuitIndex) < 2) {
					

					curValue += 10.0;
					if(dataModel.getNumberOfCardsPlayerPlayedInSuit(mellowPlayerIndex, curSuitIndex) == 0) {
						curValue += 10.0;
					}
					
					if(dataModel.getNumCardsInCurrentPlayerHand() <= 1) {
						curValue += 10.0;
					}
					
				}
			
			//Want to trump seated left of mellow case: (trump in front of mellow)
			} else if(mellowPlayerIndex == 3
					&& dataModel.getNumCardsOfSuitInCurrentPlayerHand(Constants.SPADE) > 0) {
				//TODO: requirements should be more relaxed...
				//TODO: fill this in when there's a relevant test case.
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
		

		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "JS KC JC TC KD ")) {
			System.out.println("Debug");
		}

		boolean wantToDrainSpade = false;
		boolean haveSomethingDangerousToPlay = false;
		
		for(int suit=Constants.NUM_SUITS - 1; suit>=0; suit--) {
			if(dataModel.isVoid(Constants.CURRENT_PLAYER_INDEX, suit) ) {
				continue;
			}
			
			//TODO: should I treat the other off-suits differently than spades?

			String tempLowest = dataModel.getCardCurrentPlayerGetLowestInSuit(suit);

			if(dataModel.signalHandler.mellowBidderSignalledNoCardOverCardSameSuit(tempLowest, MELLOW_PLAYER_INDEX) == false
					|| wantToDrainSpade ) {

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
				
				haveSomethingDangerousToPlay  = true;
				int curLowestRankSuitScore = DataModel.getRankIndex(tempLowest);

				// pretend lowest spades have a higher rank to discourage use of spades:
				if(suit == Constants.SPADE) {
					curLowestRankSuitScore += 9.0;
					
					if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(MELLOW_PLAYER_INDEX, suit)) {
						curLowestRankSuitScore += 3.0;
					}
				}
				

				if(suit == Constants.SPADE
						&& wantToDrainSpade) {
					curLowestRankSuitScore -= 12;
				}
				/*
				if(dataModel.signalHandler.mellowBidderSignalledNoCardOverCardSameSuit(tempLowest, MELLOW_PLAYER_INDEX) == false
						&& suit == Constants.SPADE) {
					curLowestRankSuitScore += 9.0;
					
					int numOver = dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(dataModel.signalHandler.getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(MELLOW_PLAYER_INDEX, Constants.SPADE));
					
					if(numOver >= 0) {
						//This is over-simplified, but whatever
						//curLowestRankSuitScore += 50.0;
					}
					
				}
				}*/
				
				//Don't want to lead low if you have master and are left of mellow.
				if(dataModel.currentPlayerHasMasterInSuit(suit)
						&& suit != Constants.SPADE) {
					curLowestRankSuitScore += 3.0;
				}
				
				//Encourage mellow protector to trump...
				//TODO: This isn't good if mellow is void in some offsuit and we don't want the protector to lead it
				if(dataModel.isVoid(Constants.LEFT_PLAYER_INDEX, suit)
					 && suit != Constants.SPADE) {
						 if(
								 ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
								&& (dataModel.getBid(Constants.LEFT_PLAYER_INDEX) <= 3
								 || dataModel.getBid(Constants.LEFT_PLAYER_INDEX) == dataModel.getNumTricks(Constants.LEFT_PLAYER_INDEX)
								 )
								&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
								&& dataModel.getBid(Constants.LEFT_PLAYER_INDEX) > 1
								&& dataModel.signalHandler.getMinCardRankSignal(Constants.LEFT_PLAYER_INDEX, Constants.SPADE) < DataModel.RANK_NINE){
							 wantToDrainSpade = true;
						 } else {
							 curLowestRankSuitScore -= 3.0;
						 }
				 }
				
				if(curLowestRankSuitScore < lowestRankScore) {
					bestSuitIndex = suit;
					lowestRankScore = curLowestRankSuitScore;
				}
				
				
			} else if(suit == Constants.SPADE
					&& haveSomethingDangerousToPlay == false
					&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)) {
				
				
				
			}
		
		
		}
		
		if(bestSuitIndex != -1) {
			
			return leadLowButAvoidWastingLowestCardInSuit(dataModel, bestSuitIndex);
			
		} else {

			int bestValue = -1;
			for(int suit=0; suit<Constants.NUM_SUITS; suit++) {
				
				int curValue = 0;

				if(dataModel.isVoid(Constants.CURRENT_PLAYER_INDEX, suit) ) {
					continue;
				}

				if(dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suit) == 0) {
					continue;
				}
				
				if(suit == Constants.SPADE
						&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(PROTECTOR_PLAYER_INDEX, Constants.SPADE)) {
					
					System.out.println("Just drain spade?");
					
					curValue = 10;
					
				} else {
					
					if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(MELLOW_PLAYER_INDEX, suit)) {
						continue;
					}
					
					curValue = dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suit);
					
					if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, suit)) {
						curValue += 2;
					}
					
					if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(PROTECTOR_PLAYER_INDEX, suit)) {
						curValue += 2;
					}

				}
				
				if(curValue > bestValue) {
					bestValue = curValue;
					bestSuitIndex = suit;
				}
				
			} //END LOOP
			
			
			if(bestSuitIndex != -1) {
				if(dataModel.isMasterCard(dataModel.getCardCurrentPlayerGetHighestInSuit(bestSuitIndex))) {
					return dataModel.getCardCurrentPlayerGetHighestInSuit(bestSuitIndex);
				} else {
					return dataModel.getCardCurrentPlayerGetLowestInSuit(bestSuitIndex);
				}
			} else {
				
				if(! dataModel.isVoid(Constants.CURRENT_PLAYER_INDEX, Constants.SPADE)) {
					return dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
				} else {
					return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
				}
			}
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
				&& dataModel.getNumCardsPlayedForSuit(suitToPlay) <= 6
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
	
	

	//TODO: actually use this eventually:
	public static boolean suitLeadCouldGivePartnerChanceToTrump(DataModel dataModel, int suit) {
		if(suit == Constants.SPADE
				|| dataModel.isVoid(Constants.CURRENT_PLAYER_INDEX, suit) ) {
			return false;
		}
		//TODO: put in function
		String maxRankCardMellow = dataModel.signalHandler.getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(MELLOW_PLAYER_INDEX, suit);
		int maxRankUnder = -1;
		
		if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, suit)) {
			boolean upperBound = true;
			boolean forgetIt = false;
			if(maxRankCardMellow == null) {
				forgetIt = true;
			} else if(DataModel.getRankIndex(maxRankCardMellow) == DataModel.ACE) {
				upperBound = false;
			} else {
				maxRankUnder = 1 + DataModel.getRankIndex(maxRankCardMellow);
			}
			
			int numPotentialCards = 0;
			
			if(! forgetIt && upperBound ) {
				numPotentialCards = dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(
						DataModel.getCardString(maxRankUnder, suit));
				
			} else if( !forgetIt && !upperBound) {
				
				numPotentialCards = dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suit);
				
			}
			
			boolean retCard = false;
			if(numPotentialCards > 0 && dataModel.isVoid(PROTECTOR_PLAYER_INDEX, suit)) {
				//curLowestRankSuitScore -= 10;
				//curLowestRankSuitScore -= 100;
				retCard = true;
			} else if(numPotentialCards > 0 && dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(PROTECTOR_PLAYER_INDEX, suit)) {
				//curLowestRankSuitScore -= 10*(numPotentialCards + 1)*numPotentialCards/2;
				//curLowestRankSuitScore -= 100;
				retCard = true;
				
			} else if(numPotentialCards > 1) {
				//curLowestRankSuitScore -= 10*(numPotentialCards + 1)*numPotentialCards/2;
				//curLowestRankSuitScore -= 100;
				retCard = true;
				
			}
			
			if(retCard) {
				String curCard = dataModel.getCardCurrentPlayerGetHighestInSuit(suit);
				String lowestCard = dataModel.getCardCurrentPlayerGetLowestInSuit(suit);
				
				if(maxRankCardMellow != null
						&& DataModel.getRankIndex(lowestCard) < DataModel.getRankIndex(maxRankCardMellow)) {
					//return lowestCard;
					return true;
				} else {
					return true;
					//return dataModel.getCardCurrentPlayerGetHighestInSuit(suit);
				}
			}
			
		}
		
		return false;
		//End TODO put in function
	}
}

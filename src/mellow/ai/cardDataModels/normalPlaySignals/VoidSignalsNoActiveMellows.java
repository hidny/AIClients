package mellow.ai.cardDataModels.normalPlaySignals;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.cardDataModels.handIndicators.NonMellowBidHandIndicators;
import mellow.cardUtils.CardStringFunctions;
import mellow.cardUtils.DebugFunctions;


//TODO
public class VoidSignalsNoActiveMellows {

	//public static void main(String[] args) {
		// TODO Auto-generated method stub

	//}
	
	public DataModel dataModel;
	
	public VoidSignalsNoActiveMellows(DataModel dataModel) {
		this.dataModel = dataModel;
		initSignalVars();
	}
	
	public void resetCardSignalsForNewRound() {
		initSignalVars();
	}

	public int hardMinCardPlayedBecausePlayedUnderCurWinner[][];
	//public int softMaxCardPlayed[][];
	
	

	public int hardMinCardRankBecausePlayedOverPartner[][];
	public int hardMaxCardPlayedBecauseLackOfTrump[][];
	public int hardMaxBecauseSomeoneElseSignalledMasterQueen[][];
	public int hardMaxBecauseSomeoneDidntMakeATrickas4thThrower[][];
	
	public int hardMaxBecauseThirdDidntPlayAboveSecond[][];
	
	//Adding mellow signal because I'm starting to think all signals should be here.
	//soft max = assume protector could have 1 higher??? Nah!
	//OR: assume they might have master if they played 1 below master?
	public int softMaxBecauseMellowProtectorPlayedLowAtSecondThrow[][];
	public int hardMaxBecauseMellowProtectorPlayedLowAtThirdThrow[][];
	
	//TODO: maybe LHS has 1 above...
	//public int softMaxPLUS1BecauseMellowProtectorPlayedLowAtSecondThrow[][];
	//public int softMaxPLUS2BecauseMellowProtectorPlayedLowAtSecondThrow[][];
	
	public int softMinBecauseMellowProtectorTrumpedHigh[][];
	
	
	
	public int hardMaxBecauseSomeoneDidntPlayMaster[][];

	//TODO: maybe extend this concept to other cards just to say that it's probably missing
	// from player's hand?
	public int hardMaxBecauseSomeonePlayedDirectlyUnderQequiv[][];
	
	public int softMaxBecauseSomeoneLeadKequiv[][];
	
	//TODO
	//public int hardMaxBecauseSomeoneSparedAVulnerableKing[][];
	
	public int playerIndexKingSacrificeForSuit[] = new int[Constants.NUM_PLAYERS];
	public int playerIndexKingSacrificeForSuitVoid[] = new int[Constants.NUM_PLAYERS];
	//public int playerIndexQueenForSuit[] = new int[Constants.NUM_PLAYERS];

	//This is not much of a signal, but whatever...
	public boolean didNotFollowSuit[][];
	
	public static int MAX_UNDER_RANK_2 = -2;
	public static int NO_KING_SACRIFICE = -1;
	public static int NO_KING_SAC_VOID = -1;
	public static int DONT_KNOW = -1;
	
	public boolean curTeamSignalledHighOffsuit[];
	

	
	public void initSignalVars() {
		hardMinCardPlayedBecausePlayedUnderCurWinner = new int[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		//softMaxCardPlayed = new int[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		hardMinCardRankBecausePlayedOverPartner = new int[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		hardMaxCardPlayedBecauseLackOfTrump = new int[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		hardMaxBecauseSomeoneElseSignalledMasterQueen = new int[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		
		hardMaxBecauseSomeoneDidntMakeATrickas4thThrower = new int[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		hardMaxBecauseSomeoneDidntPlayMaster = new int[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		hardMaxBecauseSomeonePlayedDirectlyUnderQequiv = new int[Constants.NUM_PLAYERS][Constants.NUM_SUITS];

		hardMaxBecauseThirdDidntPlayAboveSecond = new int[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		
		didNotFollowSuit = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		
		softMaxBecauseMellowProtectorPlayedLowAtSecondThrow = new int[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		hardMaxBecauseMellowProtectorPlayedLowAtThirdThrow  = new int[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		softMinBecauseMellowProtectorTrumpedHigh = new int[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		
		softMaxBecauseSomeoneLeadKequiv = new int[Constants.NUM_PLAYERS][Constants.NUM_SUITS];;
		
		for(int i=0; i<hardMinCardPlayedBecausePlayedUnderCurWinner.length; i++) {
			for(int j=0; j<hardMinCardPlayedBecausePlayedUnderCurWinner[0].length; j++) {
				
				hardMinCardPlayedBecausePlayedUnderCurWinner[i][j] = DONT_KNOW;
				//softMaxCardPlayed[i][j] = DONT_KNOW;
				hardMinCardRankBecausePlayedOverPartner[i][j] = DONT_KNOW;
				hardMaxCardPlayedBecauseLackOfTrump[i][j] = DONT_KNOW;
				hardMaxBecauseSomeoneElseSignalledMasterQueen[i][j] = DONT_KNOW;
				hardMaxBecauseSomeoneDidntMakeATrickas4thThrower[i][j] = DONT_KNOW;
				hardMaxBecauseSomeoneDidntPlayMaster[i][j] = DONT_KNOW;
				hardMaxBecauseSomeonePlayedDirectlyUnderQequiv[i][j] = DONT_KNOW;
				hardMaxBecauseThirdDidntPlayAboveSecond[i][j] = DONT_KNOW;
				softMaxBecauseMellowProtectorPlayedLowAtSecondThrow[i][j] = DONT_KNOW;
				hardMaxBecauseMellowProtectorPlayedLowAtThirdThrow[i][j] = DONT_KNOW;
				softMinBecauseMellowProtectorTrumpedHigh[i][j] = DONT_KNOW;
				softMaxBecauseSomeoneLeadKequiv[i][j] = DONT_KNOW;
				
				didNotFollowSuit[i][j] = false;
			}
		}
		
		for(int i=0; i<playerIndexKingSacrificeForSuit.length; i++) {
			playerIndexKingSacrificeForSuit[i] = NO_KING_SACRIFICE;
		}
		for(int i=0; i<playerIndexKingSacrificeForSuitVoid.length; i++) {
			playerIndexKingSacrificeForSuitVoid[i] = NO_KING_SAC_VOID;
		}
		
		curTeamSignalledHighOffsuit = new boolean[Constants.NUM_SUITS];
		for(int i=0; i<curTeamSignalledHighOffsuit.length; i++) {
			curTeamSignalledHighOffsuit[i] = false;
		}
	}
	
	//TODO: active this on:
	//receiveCardPlayed
	
	//updateDataModelWithPlayedCard
	
	
	
	public void updateDataModelSignalsWithPlayedCard(String playerName, String card) {

		int playerIndex = dataModel.convertPlayerNameToIndex(playerName);
		int throwerIndex = dataModel.getCardsPlayedThisRound() % 4;
		
		int suitIndex = CardStringFunctions.getIndexOfSuit(card);
		
		//I'm going to start with the normal no mellow situation for now...
		if(dataModel.someoneBidMellow() == false
				|| dataModel.stillActiveMellow() == false) {

			if(dataModel.isMasterCard(card)) {
				this.curTeamSignalledHighOffsuit[suitIndex] = false;
			}
			
			if(suitIndex != dataModel.getSuitOfLeaderThrow()) {
				didNotFollowSuit[playerIndex][dataModel.getSuitOfLeaderThrow()] = true;
			}
			
			if(playerIndexKingSacrificeForSuit[suitIndex] == playerIndex) {
				
				playerIndexKingSacrificeForSuit[suitIndex] = NO_KING_SACRIFICE;
				playerIndexKingSacrificeForSuitVoid[suitIndex] = NO_KING_SAC_VOID;
	
				//Strong queen signal:
				//(EX: If player lead KDs and has a 5D later, it's pretty clear that player also has the QD)
				//Unless queen was played...
				
				if( ! dataModel.isCardPlayedInRound("Q" + card.substring(1) )) {
					
					boolean partnerHasAce = false;
					if(	dataModel.isMasterCard("Q" + card.substring(1)) == false) {
						partnerHasAce = true;
					}
					
					for(int pIndexTmp=0; pIndexTmp<Constants.NUM_PLAYERS; pIndexTmp++) {
						if(pIndexTmp != playerIndex
								&& (partnerHasAce == false
								    || pIndexTmp != (playerIndex + 2) % Constants.NUM_PLAYERS)) {
							hardMaxBecauseSomeoneElseSignalledMasterQueen[pIndexTmp][suitIndex] = DataModel.JACK;
						}
					}
					
				} else if(! card.equals("Q" + card.substring(1))
						&& dataModel.getSuitOfLeaderThrow() == suitIndex
						&& throwerIndex > 0
						){
					System.out.println("DEBUG: WEIRD CASE! Why would the player do a king sacrifice??");
				}
			} else if(playerIndexKingSacrificeForSuit[suitIndex] >= 0
					&& DataModel.getRankIndex(card) == DataModel.QUEEN) {
				
				if(dataModel.getNumberOfCardsPlayerPlayedInSuit(playerIndexKingSacrificeForSuit[suitIndex], suitIndex) == 1
						//If you are the player that played the Queen, you already knew what was happening, so don't waste time:
						&& playerIndex != Constants.CURRENT_AGENT_INDEX
						//Redundant check that the player who played Queen is not the one who played king:
						&& playerIndex != playerIndexKingSacrificeForSuit[suitIndex]
						//If the player is already known to be void, don't waste time:
						&& ! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(playerIndexKingSacrificeForSuit[suitIndex], suitIndex)) {
				
					//At this point, we're pretty sure that the player who lead K before ace is void:
					// because now we know they didn't have the KQ before doing that stunt.
					
					if(playerIndexKingSacrificeForSuit[suitIndex] != Constants.CURRENT_AGENT_INDEX) {
						

						playerIndexKingSacrificeForSuitVoid[playerIndexKingSacrificeForSuit[suitIndex]] = playerIndex;
						
						System.out.println("Queen played by someone other than Ksac player. " + dataModel.getPlayers()[playerIndexKingSacrificeForSuit[suitIndex]] + " is probably void in " + CardStringFunctions.getSuitString(suitIndex));
					} else {
						//Maybe add logic for signals you're sending out???
						// A: Only if it's needed in a test case...
					}
					
				}
			}
			
			
			//System.out.println("TEST: updateDataModelSignalsWithPlayedCard: ");
			
			
			if(throwerIndex == 0) {
				
				// Attempt to handle meaning of leading a king while the ace is still out:
				// TODO: It didn't change a single test case... yet :(
				
				if(DataModel.getRankIndex(card) == dataModel.KING
						&& dataModel.isCardPlayedInRound("A" + card.substring(1)) == false
						) {
					
					//Probably a king-sac, but in some conditions, it doesn't count:
					//For convenience I went through the reasons it doesn't count first.
					// Please see print statements for more details:
					
					if(suitIndex != Constants.SPADE
							&& (playerIndex + 1) % Constants.NUM_PLAYERS != Constants.CURRENT_PLAYER_INDEX
							//TODO: perspective taking isn't properly done here, but I don't want to bother: (Maybe a test case or two will change my mind)
							&& playerStrongSignaledNoCardsOfSuit((playerIndex + 1) % Constants.NUM_PLAYERS, suitIndex)) {
						System.out.println("King Sac player knows the player on left of them is void, so it's weird position");
						
					} else if(suitIndex != Constants.SPADE
							&& playerIndex != Constants.CURRENT_AGENT_INDEX
							&& dataModel.currentPlayerPlayedOffSuitEarlier(suitIndex)) {
					
						if(playerIndex == Constants.RIGHT_PLAYER_INDEX) {
							    //I didn't prepare for this possibility, but hopefully I'll do it one day...
								// I'll do it when a relevant test case comes up!
								// i.e: If current player played off for suit, and LHS led K, then who knows!
								// It's not really a k sac...
							 
							System.out.println("King Sac player is RHS and knows current player is void, so it's weird position");
						
						} else if(playerIndex == Constants.CURRENT_PARTNER_INDEX){
							System.out.println("King Sac player is partner and knows current player is void, so it's weird position");
							
						} else if(playerIndex == Constants.LEFT_PLAYER_INDEX){
							System.out.println("King Sac player is LHS and knows current player is void, so it's weird position. (But not scary)");
							
						}
						// and LHS might be messing with us...
						
					} else  {
						System.out.println("KING SACRIFICE!");
						
						playerIndexKingSacrificeForSuit[suitIndex] = playerIndex;
						
						if(playerIndex != Constants.CURRENT_AGENT_INDEX
								&& dataModel.hasCard(DataModel.getCardString(dataModel.QUEEN, suitIndex))) {


							//If you have queen, then the player who led the king is probably alone:
							playerIndexKingSacrificeForSuitVoid[suitIndex] = playerIndex;
							System.out.println("King Sac player doesn't have Queen so they're probably void in " + CardStringFunctions.getSuitString(suitIndex));
						}
						
						
						// S___'s comment:
						// "   bbbbn bnv bg  bv  b bv                              c   nbnnnb jh jh "
						
					}
					
					
				} else if(dataModel.getRankIndex(card) == dataModel.ACE
						&& playerIndexKingSacrificeForSuit[suitIndex] == playerIndex) {

					System.out.println("KING UNSACRIFICE!");
					playerIndexKingSacrificeForSuit[suitIndex] = NO_KING_SACRIFICE;
					playerIndexKingSacrificeForSuitVoid[suitIndex] = NO_KING_SACRIFICE;
				}
				
				if(dataModel.getNumCardsInPlayOverCardSameSuit(card) == 1
						&& CardStringFunctions.getIndexOfSuit(card) != Constants.SPADE) {
					
					// Maybe this could be exploited, but whatever!
					//Sadly, it didn't change the outcome of any test case.
					softMaxBecauseSomeoneLeadKequiv[playerIndex][CardStringFunctions.getIndexOfSuit(card)] = DataModel.getRankIndex(card);	
				}
				
			} else if(throwerIndex > 0 ) {
				
				if(CardStringFunctions.getIndexOfSuit(card) == dataModel.getSuitOfLeaderThrow()) {
					
					//This is the obvious case:
					if(dataModel.cardAGreaterThanCardBGivenLeadCard
							(dataModel.getCurrentFightWinningCardBeforeAIPlays(), card)
						) {
						//System.out.println("TEST: Signal: " + playerName + " probably doesn't have lower than " + card);
						//System.out.println("Currently winning card: " + dataModel.getCurrentFightWinningCardBeforeAIPlays());
						//System.out.println();
		
						//What if you don't want to go over partner?
						//Answer: That doesn't matter!

						if(dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(card)
								+ dataModel.getNumCardsInCurrentPlayersHandOverCardSameSuit(card)
								<= 4
								&& 
							dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(card)
								+ dataModel.getNumCardsInCurrentPlayersHandUnderCardSameSuit(card)
								>= 2) {
							// Only consider the low card lowest if it has a hope of winning:
							// I added this logic, so that monte won't take someone throwing a 5C too seriously
							// I made the margin wide enough that the AI won't dismiss the signals.
							
							//Later: maybe figure out if player wants to hide signal or not...
							//This could be really deep.

							hardMinCardPlayedBecausePlayedUnderCurWinner[playerIndex][CardStringFunctions.getIndexOfSuit(card)] = DataModel.getRankIndex(card);
							
						}// else if(dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex) > 0) {
						//	System.out.println("Never mind " + card + "!");
						//	
						//}
						
						//TODO: 
					//Less obvious: player goes 1 over partner... could be strategic though...
					} else if(throwerIndex >= 2
							&& dataModel.cardAGreaterThanCardBGivenLeadCard
							(card, dataModel.getCurrentFightWinningCardBeforeAIPlays())) {
						
						String curWinnerCard= dataModel.getCurrentFightWinningCardBeforeAIPlays();
						
						int rankStart = dataModel.getRankIndex(dataModel.getCurrentFightWinningCardBeforeAIPlays()) + 1;
						
						int rankEnd = dataModel.getRankIndex(card);
						
						boolean isBarelyOver = false;
						
						int suitOfCard = CardStringFunctions.getIndexOfSuit(card);
						
						for(int i=rankStart; i<=rankEnd; i++) {
							if(i == rankEnd) {
								isBarelyOver = true;
								break;
							} else if(dataModel.isCardPlayedInRound(
											DataModel.getCardString(i, suitOfCard)
											)) {
								continue;
							} else {
								break;
							}
						}
						
						if(isBarelyOver) {
							if(dataModel.getCardLeaderThrow().equals(curWinnerCard) && 
									throwerIndex == 2) {
								System.out.println("DEBUG MAX " + card + " is barely over 1");
								hardMinCardRankBecausePlayedOverPartner[playerIndex][CardStringFunctions.getIndexOfSuit(card)] = dataModel.getRankIndex(card);
								
							} else if(dataModel.getCardSecondThrow().equals(curWinnerCard) && 
									throwerIndex == 3) {
								System.out.println("DEBUG MAX " + card + " is barely over 2");
								hardMinCardRankBecausePlayedOverPartner[playerIndex][CardStringFunctions.getIndexOfSuit(card)] = dataModel.getRankIndex(card);
								
							}
						}
					}
				}
				
				//what if you go over, but it's not master?? ...

				String curWinnerCard= dataModel.getCurrentFightWinningCardBeforeAIPlays();
				
				if(throwerIndex == 2
						&& CardStringFunctions.getIndexOfSuit(card) == dataModel.getSuitOfLeaderThrow()
						&& CardStringFunctions.getIndexOfSuit(card) != Constants.SPADE
						&& CardStringFunctions.getIndexOfSuit(curWinnerCard) == dataModel.getSuitOfLeaderThrow()
						&& ! dataModel.isMasterCard(curWinnerCard)
						&& curWinnerCard.equals(dataModel.getCardSecondThrow())
						&& dataModel.cardAGreaterThanCardBGivenLeadCard(curWinnerCard, card)) {
					
					//System.out.println("DEBUG: SIGNAL!");

					//For now, only consider for non-spades (with spades, the signal is weaker)
					hardMaxBecauseThirdDidntPlayAboveSecond[playerIndex][dataModel.getSuitOfLeaderThrow()] = DataModel.getRankIndex(curWinnerCard);
				}
				//hardMaxBecauseThirdDidntPlayAboveSecond
				
				// When 4th player fails to trump to make an easy trick, that means something:
				if(throwerIndex == 3
						&& CardStringFunctions.getIndexOfSuit(card) != dataModel.getSuitOfLeaderThrow()
						&& CardStringFunctions.getIndexOfSuit(card) != Constants.SPADE) {
					
					
					if( ! dataModel.getCardSecondThrow().equals(curWinnerCard)) {
						
						if(CardStringFunctions.getIndexOfSuit(curWinnerCard) == dataModel.getSuitOfLeaderThrow() ) {
							
							if(CardStringFunctions.getIndexOfSuit(card) != Constants.SPADE) {
								hardMaxCardPlayedBecauseLackOfTrump[playerIndex][Constants.SPADE] = MAX_UNDER_RANK_2;
							}
						} else if(CardStringFunctions.getIndexOfSuit(curWinnerCard) == Constants.SPADE ) {
							
							hardMaxCardPlayedBecauseLackOfTrump[playerIndex][Constants.SPADE] = DataModel.getRankIndex(curWinnerCard);
							
						} else {
							
							System.err.println("ERROR: This signal case should not be possible for this game.");
							System.exit(1);
						}
					
						
					}

			// When 3rd player fails to trump to make an easy trick and 4th doesn't have spade:
			} else if(throwerIndex == 2
					&& CardStringFunctions.getIndexOfSuit(card) != dataModel.getSuitOfLeaderThrow()
					&& CardStringFunctions.getIndexOfSuit(card) != Constants.SPADE
					&& dataModel.isVoid((playerIndex + 1) % Constants.NUM_SUITS, Constants.SPADE)) {
					
				if( ! dataModel.getCardLeaderThrow().equals(curWinnerCard)) {
					
					if(CardStringFunctions.getIndexOfSuit(curWinnerCard) == dataModel.getSuitOfLeaderThrow() ) {
						
						if(CardStringFunctions.getIndexOfSuit(card) != Constants.SPADE) {
							hardMaxCardPlayedBecauseLackOfTrump[playerIndex][Constants.SPADE] = MAX_UNDER_RANK_2;
						}
					} else if(CardStringFunctions.getIndexOfSuit(curWinnerCard) == Constants.SPADE ) {
						
						hardMaxCardPlayedBecauseLackOfTrump[playerIndex][Constants.SPADE] = DataModel.getRankIndex(curWinnerCard);
						
					} else {
						
						System.err.println("ERROR: This signal case should not be possible for this game. 2");
						System.exit(1);
					}
				
					
				}
				
				
				//Hard max because 4th thrower didn't make a trick:
			} else if(throwerIndex == 3
						&& CardStringFunctions.getIndexOfSuit(card) == dataModel.getSuitOfLeaderThrow()
						&& CardStringFunctions.getIndexOfSuit(curWinnerCard) ==  dataModel.getSuitOfLeaderThrow()) {
					
				//TODO: is this check redundant now?
					
					
					
					if( dataModel.cardAGreaterThanCardBGivenLeadCard(curWinnerCard, card)
							&& ! dataModel.getCardSecondThrow().equals(curWinnerCard)) {
						hardMaxBecauseSomeoneDidntMakeATrickas4thThrower[playerIndex][dataModel.getSuitOfLeaderThrow()] = DataModel.getRankIndex(curWinnerCard);
						
						//if(dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(curWinnerCard) > 0) {
						//	System.out.println("SIGNALLED LOW CARD");
						//}
					}
			}
			
				
				//Signal when 3rd thrower doesn't play master spade
				//TODO
				//What if don't play master because 4th is trumping?
				//or saving master for some other reason.
				/*if(throwerIndex == 2
						
						&& CardStringFunctions.getIndexOfSuit(card) == dataModel.getSuitOfLeaderThrow()
						&& ! dataModel.getCardLeaderThrow().equals(curWinnerCard)) {
					
					if( dataModel.cardAGreaterThanCardBGivenLeadCard(curWinnerCard, card)) {
						//TODO
					}	
				}*/
				
				if(  dataModel.isMasterCard(curWinnerCard) == false
						&& CardStringFunctions.getIndexOfSuit(card) == dataModel.getSuitOfLeaderThrow()
						&& CardStringFunctions.getIndexOfSuit(curWinnerCard) ==  dataModel.getSuitOfLeaderThrow()
						&& dataModel.cardAGreaterThanCardBGivenLeadCard(curWinnerCard, card) ) {
					
						boolean letPartnerWin = letPartnerWin(dataModel, throwerIndex, curWinnerCard);
						
						if(dataModel.getSuitOfLeaderThrow() != Constants.SPADE
									&& throwerIndex == 1
									&& (dataModel.signalHandler.mellowBidderPlayerSignalNoCardsOfSuit((playerIndex + 2) % Constants.NUM_PLAYERS, dataModel.getSuitOfLeaderThrow())
											|| dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex) < 2)
									&& ! dataModel.signalHandler.mellowBidderPlayerSignalNoCardsOfSuit((playerIndex + 2) % Constants.NUM_PLAYERS, Constants.SPADE)) {
							letPartnerWin = true;
						}
					
						if( !letPartnerWin ) {
							hardMaxBecauseSomeoneDidntPlayMaster[playerIndex][dataModel.getSuitOfLeaderThrow()] = DataModel.getRankIndex(dataModel.getHighestCardOfSuitNotPlayed(suitIndex)) - 1;
							
							
						}
				
				}
				
				if(throwerIndex == 3
						&& CardStringFunctions.getIndexOfSuit(curWinnerCard) ==  dataModel.getSuitOfLeaderThrow()
						&& dataModel.isMasterCard(curWinnerCard) == false
						&& CardStringFunctions.getIndexOfSuit(card) == dataModel.getSuitOfLeaderThrow()
						&& dataModel.isMasterCard(card) == false) {
					
					int curWinnerIndex = getCurrentWinningIndex(card, playerIndex, throwerIndex);
					
					
					if(curWinnerIndex ==Constants.CURRENT_AGENT_INDEX || curWinnerIndex == Constants.CURRENT_PARTNER_INDEX) {
						this.curTeamSignalledHighOffsuit[suitIndex] = true;
					}
					
					
				}
				
				
				//hardMaxBecauseSomeonePlayedDirectlyUnderQequiv
				//This is a strong void signal that should not be ignored!
				if(  dataModel.isMasterCard(curWinnerCard) == false
						&& CardStringFunctions.getIndexOfSuit(card) == dataModel.getSuitOfLeaderThrow()
						&& CardStringFunctions.getIndexOfSuit(curWinnerCard) ==  dataModel.getSuitOfLeaderThrow()
						&& dataModel.cardAGreaterThanCardBGivenLeadCard(curWinnerCard, card)
						&& dataModel.getNumCardsInPlayBetweenCardSameSuitPossiblyWRONG(curWinnerCard, card) == 0
						&& dataModel.getNumCardsInPlayOverCardSameSuit(curWinnerCard) <= 2
						&& ! letPartnerWin(dataModel, throwerIndex, curWinnerCard)
						) {
					hardMaxBecauseSomeonePlayedDirectlyUnderQequiv[playerIndex][dataModel.getSuitOfLeaderThrow()] = DataModel.getRankIndex(card) - 1;
				}
			}
			
			
			if(throwerIndex == 0 
					&& dataModel.isMasterCard(card) == false
					&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex) > 3) {
				
				hardMaxBecauseSomeoneDidntPlayMaster[playerIndex][dataModel.getSuitOfLeaderThrow()] = DataModel.getRankIndex(dataModel.getHighestCardOfSuitNotPlayed(suitIndex)) - 1;
				
			}
			
		}

		//Mellow protector signal TO BE MOVED... (or something)

		//This covers the case where protector follows suit but doesn't play high enough to cover mellow player
		//PROTECTOR SIGNAL BY PLAYING TOO LOW:
		if(throwerIndex > 0) {
			int partnerIndex = (playerIndex + 2) % Constants.NUM_PLAYERS;
			
			String curWinnerCard= getCurrentStrongestCardInFight(card);
		
			String winningCardBeforePlayerIndex = dataModel.getCurrentFightWinningCardBeforeAIPlays();
			
			if(dataModel.getBid(partnerIndex) == 0 
					&& ! dataModel.burntMellow(partnerIndex)) {
				
				if( CardStringFunctions.getIndexOfSuit(curWinnerCard) == dataModel.getSuitOfLeaderThrow()
					&& CardStringFunctions.getIndexOfSuit(card) == dataModel.getSuitOfLeaderThrow()
					) {
				
					if(throwerIndex == 1) {
						
						if( ! mellowSignalledNoCardOverCardSameSuit(curWinnerCard, partnerIndex)) {
							
	
							//System.out.println("DEBUG SOFT MAX PROTECTOR 2: " + curWinnerCard);
							
							//TODO: maybe this is a softer max...
							//ex: what if protect has 5C 6C 7C and just plays the 5C? I don't know why they would do that though.
							if(playerIndex != Constants.CURRENT_AGENT_INDEX
									&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(curWinnerCard) 
									+ dataModel.getNumCardsInCurrentPlayersHandOverCardSameSuit(curWinnerCard) <= 1) {
								//System.out.println("CANCEL SIGNAL!");
								
							} else if(playerIndex == Constants.CURRENT_AGENT_INDEX
									&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(curWinnerCard) <= 1) {
								//System.out.println("CANCEL! (SELF SIGNAL)");
								
							} else {
								
								softMaxBecauseMellowProtectorPlayedLowAtSecondThrow[playerIndex][CardStringFunctions.getIndexOfSuit(curWinnerCard)] = dataModel.getRankIndex(curWinnerCard);
							}
							
						}
							
						//if(mellowSignalledNoCardOverCardSameSuit(card))
					} else if(throwerIndex == 2 && getCurrentWinningIndex(card, playerIndex, throwerIndex) == partnerIndex ) {
						
						//System.out.println("DEBUG HARD MAX PROTECTOR 3: " + curWinnerCard);
						hardMaxBecauseMellowProtectorPlayedLowAtThirdThrow[playerIndex][CardStringFunctions.getIndexOfSuit(curWinnerCard)] = dataModel.getRankIndex(curWinnerCard);
						
						if(dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(curWinnerCard) == 0) {
							//System.out.println("DEBUG: maybe the protector is being cheeky (or bad)");
						}
					}
					
				//Protector trump signals:
				} else if(CardStringFunctions.getIndexOfSuit(winningCardBeforePlayerIndex) == dataModel.getSuitOfLeaderThrow()
						&& CardStringFunctions.getIndexOfSuit(card) != dataModel.getSuitOfLeaderThrow()
						&& CardStringFunctions.getIndexOfSuit(card) == Constants.SPADE
						&& dataModel.signalHandler.mellowBidderPlayerMayBeInDangerInSuit(partnerIndex, Constants.SPADE)
						&& (throwerIndex == 1
								|| dataModel.getIndexOfCurrentlyWinningPlayerBeforeAIPlays() == partnerIndex)
						) {
							//System.out.println("Protector trump!");
							softMinBecauseMellowProtectorTrumpedHigh[playerIndex][Constants.SPADE] = DataModel.getRankIndex(card) + 1;							

				}
				
			}
			
		}
		//END PROTECTOR SIGNAL BY PLAYING TOO LOW
	}
	

	public int getCurrentWinningIndex(String card, int curPlayerIndex, int throwerIndex) {
		
		String curWinnerCard= dataModel.getCurrentFightWinningCardBeforeAIPlays();
		
		int leadThrowerIndex = (curPlayerIndex - throwerIndex + Constants.NUM_PLAYERS) % Constants.NUM_PLAYERS;
		
		int curWinnerIndex = -1;
		if(throwerIndex > 0 && dataModel.getCardLeaderThrow().equals(curWinnerCard)) {
			curWinnerIndex = leadThrowerIndex;
		} else if(throwerIndex > 1 && dataModel.getCardSecondThrow().equals(curWinnerCard)) {
			curWinnerIndex = (leadThrowerIndex + 1 ) % 4;
		} else if(throwerIndex > 2 && dataModel.getCardThirdThrow().equals(curWinnerCard)) {
			curWinnerIndex = (leadThrowerIndex + 2 ) % 4;
		}
		
		if(dataModel.cardAGreaterThanCardBGivenLeadCard(card, curWinnerCard)) {
			curWinnerIndex = curPlayerIndex;
		}
		
		return curWinnerIndex;
	}
	
	public String getCurrentStrongestCardInFight(String card) {
		String curWinnerCard= dataModel.getCurrentFightWinningCardBeforeAIPlays();
		
		if(dataModel.cardAGreaterThanCardBGivenLeadCard(card, curWinnerCard)) {
			return card;
		} else {
			return curWinnerCard;
		}
	}
	//TODO THIS WAS COPY/PASTED
	public static final int MELLOW_PLAYER_SIGNALED_NO = 3;
	
	public boolean mellowSignalledNoCardOverCardSameSuit(String inputCard, int mellowPlayerIndex) {
		
		boolean cardsOverInputCard[][] = dataModel.getCardsStrictlyMorePowerfulThanCard(inputCard, true);
		
		int suitIndex = CardStringFunctions.getIndexOfSuit(inputCard);
		
		for(int j=0; j<Constants.NUM_RANKS; j++) {
			
			if(cardsOverInputCard[suitIndex][j]) {

				if(dataModel.getCardsCurrentlyHeldByPlayers()[mellowPlayerIndex][suitIndex][j] != dataModel.IMPOSSIBLE
						&& dataModel.getCardsCurrentlyHeldByPlayers()[mellowPlayerIndex][suitIndex][j] != MELLOW_PLAYER_SIGNALED_NO) {
					
					//At this point, the mellow player signalled that they could have a card in between
					//And you should feel nervous about playing over the currently winning card...
					return false;
				}
			}
		}
		
		return true;
		
	}
	//END TODO THIS WAS COPY PASTED
	
	//TODO: implement later if it proves useful:
	public boolean playerSoftSignaledNoCardsOfSuit(String playerName, int suitIndex) {
		
		return playerStrongSignaledNoCardsOfSuit(playerName, suitIndex);
	}
	
	public boolean playerSoftSignaledNoCardsOfSuit(int playerIndex, int suitIndex) {	
		return playerStrongSignaledNoCardsOfSuit(playerIndex, suitIndex);
	}

	
	// A lot of functions just to answer a basic question...
	public boolean playerSignaledNoCardsOfSuit(String playerName, int suitIndex) {
		return playerStrongSignaledNoCardsOfSuit(playerName, suitIndex);
	}
	
	public boolean playerSignaledNoCardsOfSuit(int playerIndex, int suitIndex) {
		return playerStrongSignaledNoCardsOfSuit(playerIndex, suitIndex);
	}
	
	
	public boolean playerStrongSignaledNoCardsOfSuit(String playerName, int suitIndex) {
		int playerIndex = dataModel.convertPlayerNameToIndex(playerName);
		return playerStrongSignaledNoCardsOfSuit(playerIndex, suitIndex);
	}
	
	public int getPlayerIndexOfKingSacrificeForSuit(int indexSuit) {
		return playerIndexKingSacrificeForSuit[indexSuit];
	}

	public int getPlayerIndexOfKingSacrificeVoidForSuit(int indexSuit) {
		return playerIndexKingSacrificeForSuitVoid[indexSuit];
	}
	
	//public int getPlayerIndexSignalledMasterQueen(int indexSuit) {
	//	return playerIndexQueenForSuit[indexSuit];
		
	//}
	
	
	public boolean playerStrongSignaledNoCardsOfSuit(int playerIndex, int suitIndex) {

		int curMinRank = getMinCardRankSignal(playerIndex, suitIndex);
		int curMaxRank = getMaxCardRankSignal(playerIndex, suitIndex, false);
		
		if(this.getPlayerIndexOfKingSacrificeVoidForSuit(suitIndex) == playerIndex) {
			System.out.println("(King SAC VOID SIGNAL WORKED)");
			return true;
		}
		
		for(int rank=curMinRank; rank <= curMaxRank; rank++) {
			if(this.dataModel.getCardsCurrentlyHeldByPlayers()[playerIndex][suitIndex][rank] != dataModel.IMPOSSIBLE) {
				
				//SANITY TEST (TO DELETE)
				if(this.dataModel.isVoid(playerIndex, suitIndex)) {
					System.out.println("AHH! Wrong signal!");
					System.exit(1);
				}
				//END SANITY TEST
				

				//TODO: I put this here just for testing purposes, move it up to before the loop later!
				/*if(this.getPlayerIndexOfKingSacrificeVoidForSuit(suitIndex) == playerIndex) {
					System.out.println("(King SAC VOID SIGNAL WORKED)");
					return true;
				} else {
					return false;
				}*/
				return false;
			}
		}
		
		
		return true;
	}
	

	public int getMaxCardRankSignal(int playerIndex, int suitIndex ) {
		return getMaxCardRankSignal(playerIndex, suitIndex, true );
	}
	
	public int getMaxCardRankSignal(int playerIndex, int suitIndex, boolean printWarn ) {

		int curMaxRank = DataModel.ACE;
		
		if(hardMaxCardPlayedBecauseLackOfTrump[playerIndex][suitIndex] != DONT_KNOW
				&& curMaxRank > hardMaxCardPlayedBecauseLackOfTrump[playerIndex][suitIndex]) {
			curMaxRank = hardMaxCardPlayedBecauseLackOfTrump[playerIndex][suitIndex];
		}
		
		if(hardMaxBecauseSomeoneElseSignalledMasterQueen[playerIndex][suitIndex] != DONT_KNOW
			&& curMaxRank > hardMaxBecauseSomeoneElseSignalledMasterQueen[playerIndex][suitIndex]) {
			curMaxRank = hardMaxBecauseSomeoneElseSignalledMasterQueen[playerIndex][suitIndex];
		}
		
		if(hardMaxBecauseSomeoneDidntMakeATrickas4thThrower[playerIndex][suitIndex] != DONT_KNOW
				&& curMaxRank > hardMaxBecauseSomeoneDidntMakeATrickas4thThrower[playerIndex][suitIndex]) {
			curMaxRank = hardMaxBecauseSomeoneDidntMakeATrickas4thThrower[playerIndex][suitIndex];
			
		}
		
		if(hardMaxBecauseSomeoneDidntPlayMaster[playerIndex][suitIndex] != DONT_KNOW
				&& curMaxRank > hardMaxBecauseSomeoneDidntPlayMaster[playerIndex][suitIndex]) {
			curMaxRank = hardMaxBecauseSomeoneDidntPlayMaster[playerIndex][suitIndex];

		}

		if(softMaxBecauseMellowProtectorPlayedLowAtSecondThrow[playerIndex][suitIndex] != DONT_KNOW
				&& curMaxRank > softMaxBecauseMellowProtectorPlayedLowAtSecondThrow[playerIndex][suitIndex]) {
			
			if(shouldCancelSoftMaxSignal(dataModel, playerIndex, suitIndex, softMaxBecauseMellowProtectorPlayedLowAtSecondThrow[playerIndex][suitIndex])) {
				// Cancel signal:
				// If the protector plays higher later, ignore this signal (This is rough, but it's better than nothing...)
				// This aspect of the game is a complicated game-theory nightmare.
				
			} else {
				curMaxRank = softMaxBecauseMellowProtectorPlayedLowAtSecondThrow[playerIndex][suitIndex];
				
			}
			

		}

		if(hardMaxBecauseMellowProtectorPlayedLowAtThirdThrow[playerIndex][suitIndex] != DONT_KNOW
				&& curMaxRank > hardMaxBecauseMellowProtectorPlayedLowAtThirdThrow[playerIndex][suitIndex]) {
			curMaxRank = hardMaxBecauseMellowProtectorPlayedLowAtThirdThrow[playerIndex][suitIndex];

		}
		
		if(hardMaxBecauseThirdDidntPlayAboveSecond[playerIndex][suitIndex] != DONT_KNOW
				&& curMaxRank > hardMaxBecauseThirdDidntPlayAboveSecond[playerIndex][suitIndex]) {

			//System.out.println("DEBUG: SIGNAL 2!");
			curMaxRank = hardMaxBecauseThirdDidntPlayAboveSecond[playerIndex][suitIndex];

		}

		if(hardMaxBecauseSomeonePlayedDirectlyUnderQequiv[playerIndex][suitIndex] != DONT_KNOW
				&& curMaxRank > hardMaxBecauseSomeonePlayedDirectlyUnderQequiv[playerIndex][suitIndex]) {
			curMaxRank = hardMaxBecauseSomeonePlayedDirectlyUnderQequiv[playerIndex][suitIndex];

		}
		
		if(softMaxBecauseSomeoneLeadKequiv[playerIndex][suitIndex] != DONT_KNOW
				&& curMaxRank > softMaxBecauseSomeoneLeadKequiv[playerIndex][suitIndex]) {
			System.out.println("TEST: softMaxBecauseSomeoneLeadKequiv for " + dataModel.getPlayers()[playerIndex] 
					+ " = " + DataModel.getCardString(softMaxBecauseSomeoneLeadKequiv[playerIndex][suitIndex], suitIndex));
			
			curMaxRank = softMaxBecauseSomeoneLeadKequiv[playerIndex][suitIndex];

		}
		
		
		//TODO: put in another function:
		int ret=curMaxRank;
		for(; ret>=DataModel.RANK_TWO; ret--) {
			if(dataModel.isCardPlayedInRound(DataModel.getCardString(ret, suitIndex))) {
				continue;
			} else if(playerIndex != Constants.CURRENT_PLAYER_INDEX && dataModel.hasCard(DataModel.getCardString(ret, suitIndex))) {
				continue;
			} else {
				break;
			}
		}
		//END TODO
		
		if(ret < DataModel.RANK_TWO) {
			
			if(printWarn) {
				try {
					throw new Exception("Warning: Max rank under two");
				} catch(Exception e) {
					e.printStackTrace();
					System.err.println("Warning: Max rank under two");
				}
			}
			
			return MAX_UNDER_RANK_2;
		}
		
		return ret;
	}
	
	public int getMinCardRankSignal(int playerIndex, int suitIndex ) {
		//TODO: don't be too clever:
		int curMinRank = Math.max
				(hardMinCardPlayedBecausePlayedUnderCurWinner[playerIndex][suitIndex],
				DataModel.RANK_TWO);
		
		if(hardMinCardRankBecausePlayedOverPartner[playerIndex][suitIndex] != DONT_KNOW
				&& curMinRank <  hardMinCardRankBecausePlayedOverPartner[playerIndex][suitIndex]) {
				
			curMinRank = hardMinCardRankBecausePlayedOverPartner[playerIndex][suitIndex];
		}
		
		if(softMinBecauseMellowProtectorTrumpedHigh[playerIndex][suitIndex] != DONT_KNOW
				&& curMinRank <  softMinBecauseMellowProtectorTrumpedHigh[playerIndex][suitIndex]) {

			curMinRank = softMinBecauseMellowProtectorTrumpedHigh[playerIndex][suitIndex];
		}
		
		
		//TODO: put in another function:
		int ret=curMinRank;
		for(; ret<DataModel.ACE; ret++) {
			if(dataModel.isCardPlayedInRound(DataModel.getCardString(ret, suitIndex))) {
				continue;
			} else if(playerIndex != Constants.CURRENT_PLAYER_INDEX && dataModel.hasCard(dataModel.getCardString(ret, suitIndex))) {
				continue;
			} else {
				break;
			}
		}
		if(ret > DataModel.ACE) {
			return DataModel.ACE;
		}
		//END TODO
		
		return ret;
	}
	

	public int getMaxCardRankSignalForMellowProtector(int playerIndex, int suitIndex ) {

		int curMaxRank = DataModel.ACE;
		

		if(softMaxBecauseMellowProtectorPlayedLowAtSecondThrow[playerIndex][suitIndex] != DONT_KNOW
				&& curMaxRank > softMaxBecauseMellowProtectorPlayedLowAtSecondThrow[playerIndex][suitIndex]) {
			
			if(shouldCancelSoftMaxSignal(dataModel, playerIndex, suitIndex, softMaxBecauseMellowProtectorPlayedLowAtSecondThrow[playerIndex][suitIndex])) {
				// Cancel signal:
				// If the protector plays higher later, ignore this signal (This is rough, but it's better than nothing...)
				// This aspect of the game is a complicated game-theory nightmare.
				
			} else {
				curMaxRank = softMaxBecauseMellowProtectorPlayedLowAtSecondThrow[playerIndex][suitIndex];
				
			}
		}

		if(hardMaxBecauseMellowProtectorPlayedLowAtThirdThrow[playerIndex][suitIndex] != DONT_KNOW
				&& curMaxRank > hardMaxBecauseMellowProtectorPlayedLowAtThirdThrow[playerIndex][suitIndex]) {
			curMaxRank = hardMaxBecauseMellowProtectorPlayedLowAtThirdThrow[playerIndex][suitIndex];

		}
		
		
		//TODO: put in another function:
		int ret=curMaxRank;
		for(; ret>=DataModel.RANK_TWO; ret--) {
			if(dataModel.isCardPlayedInRound(DataModel.getCardString(ret, suitIndex))) {
				continue;
			} else {
				break;
			}
		}
		if(ret < DataModel.RANK_TWO) {
			return DataModel.RANK_TWO;
		}
		//END TODO
		
		return ret;
	}
	
	public int getMaxRankSpadeSignalled(int playerIndex) {
		int curMaxRank = DataModel.ACE;
		
		if(hardMaxCardPlayedBecauseLackOfTrump[playerIndex][Constants.SPADE] != DONT_KNOW
				&& curMaxRank > hardMaxCardPlayedBecauseLackOfTrump[playerIndex][Constants.SPADE]) {
			curMaxRank = hardMaxCardPlayedBecauseLackOfTrump[playerIndex][Constants.SPADE];
		}
		
		int retRank = curMaxRank;
		for(; retRank >= DataModel.RANK_TWO; retRank--) {
			if(this.dataModel.getCardsCurrentlyHeldByPlayers()[playerIndex][Constants.SPADE][retRank] != dataModel.IMPOSSIBLE) {
				break;
			}
		}
		
		if(retRank < DataModel.RANK_TWO) {
			return MAX_UNDER_RANK_2;
		} else {
			return retRank;
		}
	}
	//End functions.
	
	//TODO: have basic logic to use this...
	
	//For now, just make sure it works by printing what it thinks the signals are.
	

	public boolean playerHasOnlyMasterOrIsVoidBasedOnSignals(int playerIndex, int suitIndex) {
		String masterCard = dataModel.getHighestCardOfSuitNotPlayed(suitIndex);
		
		if(dataModel.hasCard(masterCard)) {
			return false;
		}
		

		int masterCardRank = DataModel.getRankIndex(masterCard);
		
		for(int rank = getMinCardRankSignal(playerIndex, suitIndex ); rank<masterCardRank; rank++) {
			if(this.dataModel.getCardsCurrentlyHeldByPlayers()[playerIndex][suitIndex][rank] != dataModel.IMPOSSIBLE) {
				return false;
			}
		}
		return true;
	}
	
	public boolean partnerDoesNotHaveMasterBasedOnSignals(int suitIndex) {
		
		return playerDoesNotHaveMasterBasedOnSignals(Constants.CURRENT_PARTNER_INDEX, suitIndex);
	}


	public boolean playerDoesNotHaveMasterBasedOnSignals(int playerIndex, int suitIndex) {
		
		String masterCard = dataModel.getHighestCardOfSuitNotPlayed(suitIndex);
		
		if(dataModel.hasCard(masterCard)) {
			return false;
		}
		
		int masterCardRank = DataModel.getRankIndex(masterCard);
		
		if(getMaxCardRankSignal(Constants.CURRENT_PARTNER_INDEX, suitIndex, false ) < masterCardRank) {
			return true;
		} else {
			return false;
		}
	}
	

	public boolean partnerHasMasterBasedOnSignals(int suitIndex) {
		
		String masterCard = dataModel.getHighestCardOfSuitNotPlayed(suitIndex);
		
		if(dataModel.hasCard(masterCard)) {
			return false;
		}
		
		int masterCardRank = DataModel.getRankIndex(masterCard);
		
		if(getMaxCardRankSignal(Constants.CURRENT_PARTNER_INDEX, suitIndex, false ) >= masterCardRank
			&& getMaxCardRankSignal(Constants.LEFT_PLAYER_INDEX, suitIndex, false) < masterCardRank
			&& getMaxCardRankSignal(Constants.RIGHT_PLAYER_INDEX, suitIndex, false ) < masterCardRank
			&& ! playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, suitIndex )
				) {
			return true;
		} else {
			return false;
		}
	}
	
	
	public boolean leftHandSideHasMasterBasedOnSignals(int suitIndex) {
		
		String masterCard = dataModel.getHighestCardOfSuitNotPlayed(suitIndex);
		
		if(dataModel.hasCard(masterCard)) {
			return false;
		}
		
		int masterCardRank = DataModel.getRankIndex(masterCard);
		
		if(getMaxCardRankSignal(Constants.LEFT_PLAYER_INDEX, suitIndex, false ) >= masterCardRank
			&& getMaxCardRankSignal(Constants.CURRENT_PARTNER_INDEX, suitIndex, false ) < masterCardRank
			&& getMaxCardRankSignal(Constants.RIGHT_PLAYER_INDEX, suitIndex, false ) < masterCardRank
			&& ! playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, suitIndex )
				) {
			return true;
		} else {
			return false;
		}
	}


	public boolean rightHandSideHasMasterBasedOnSignals(int suitIndex) {
		
		String masterCard = dataModel.getHighestCardOfSuitNotPlayed(suitIndex);
		
		if(dataModel.hasCard(masterCard)) {
			return false;
		}
		
		int masterCardRank = DataModel.getRankIndex(masterCard);
		
		if(getMaxCardRankSignal(Constants.RIGHT_PLAYER_INDEX, suitIndex, false ) >= masterCardRank
			&& getMaxCardRankSignal(Constants.CURRENT_PARTNER_INDEX, suitIndex, false ) < masterCardRank
			&& getMaxCardRankSignal(Constants.LEFT_PLAYER_INDEX, suitIndex, false ) < masterCardRank
			&& ! playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, suitIndex )
				) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean playerAlwaysFollowedSuit(int playerIndex, int suitIndex) {
		return ! didNotFollowSuit[playerIndex][suitIndex];
	}
	

	public boolean playerSingalledMasterCardOrVoidAccordingToCurPlayer(int playerIndex, int suitIndex) {
		if(playerSignaledNoCardsOfSuit(playerIndex, suitIndex)) {
			return true;
		}
		
		
		int minRank = getMinCardRankSignal(playerIndex, suitIndex);
	
		int numCardsFound = 0;
		
		for(int r = minRank; r<=dataModel.ACE; r++) {
			String tmpCard = DataModel.getCardString(r, suitIndex);
			
			if(dataModel.hasCard(tmpCard)) {
				continue;
			} else if(dataModel.isCardPlayedInRound(tmpCard)) {
				continue;
			} else {
				numCardsFound++;
			}
		}
		
		if(numCardsFound == 0) {
			return true;
		} else if(numCardsFound == 1
				&& ! dataModel.currentPlayerHasMasterInSuit(suitIndex)) {
			return true;
		} else {
			return false;
		}
	}
	
	
	public boolean hasCurTeamSignalledHighOffsuit(int suitIndex) {
		return curTeamSignalledHighOffsuit[suitIndex];
	}
	
	public static boolean letPartnerWin(DataModel dataModel, int throwerIndex, String curWinnerCard) {
		
		if(throwerIndex == 2
				&& dataModel.getCardLeaderThrow().equals(curWinnerCard)) {
			return true;
		} else if(throwerIndex == 3
				&& dataModel.getCardSecondThrow().equals(curWinnerCard)) {
			return true;
		} else {
			return false;
		}
		
	}

	private static boolean shouldCancelSoftMaxSignal(DataModel dataModel, int playerIndex, int suitIndex, int rankSoftMax) {
		if(dataModel.getNextHighestRankedCardUsedByPlayerForSuit(playerIndex, suitIndex, rankSoftMax)
				!= -1
				&& dataModel.playerPlayedHigherRankCardAfterPlayingLow(playerIndex, DataModel.getCardString(rankSoftMax, suitIndex))) {
					//pass:
					//If the protector plays higher later, ignore this signal (This is rough, but it's better than nothing...)
					// This aspect of the game is a complicated game-theory nightmare.
				return true;
			} else {
				return false;
			}
	}
}

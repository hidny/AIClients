package mellow.ai.cardDataModels.normalPlaySignals;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.cardUtils.CardStringFunctions;


//TODO
public class MellowVoidSignalsNoActiveMellows {

	//public static void main(String[] args) {
		// TODO Auto-generated method stub

	//}
	
	public DataModel dataModel;
	
	public MellowVoidSignalsNoActiveMellows(DataModel dataModel) {
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
	
	//TODO
	//public int hardMaxBecauseSomeoneSparedAVulnerableKing[][];
	
	public int playerIndexKingSacrificeForSuit[] = new int[Constants.NUM_PLAYERS];
	//public int playerIndexQueenForSuit[] = new int[Constants.NUM_PLAYERS];
	
	public static int MAX_UNDER_RANK_2 = -2;
	public static int NO_KING_SACRIFICE = -1;
	public static int DONT_KNOW_OR_PLAYED = -1;
	
	public void initSignalVars() {
		hardMinCardPlayedBecausePlayedUnderCurWinner = new int[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		//softMaxCardPlayed = new int[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		hardMinCardRankBecausePlayedOverPartner = new int[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		hardMaxCardPlayedBecauseLackOfTrump = new int[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		hardMaxBecauseSomeoneElseSignalledMasterQueen = new int[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		
		for(int i=0; i<hardMinCardPlayedBecausePlayedUnderCurWinner.length; i++) {
			for(int j=0; j<hardMinCardPlayedBecausePlayedUnderCurWinner[0].length; j++) {
				
				hardMinCardPlayedBecausePlayedUnderCurWinner[i][j] = -1;
				//softMaxCardPlayed[i][j] = -1;
				hardMinCardRankBecausePlayedOverPartner[i][j] = -1;
				hardMaxCardPlayedBecauseLackOfTrump[i][j] = -1;
				hardMaxBecauseSomeoneElseSignalledMasterQueen[i][j] = -1;
			}
		}
		
		for(int i=0; i<playerIndexKingSacrificeForSuit.length; i++) {
			playerIndexKingSacrificeForSuit[i] = NO_KING_SACRIFICE;
		}
	}
	
	//TODO: active this on:
	//receiveCardPlayed
	
	//updateDataModelWithPlayedCard
	
	
	
	public void updateDataModelSignalsWithPlayedCard(String playerName, String card) {

		int playerIndex = dataModel.convertPlayerNameToIndex(playerName);
		int throwerIndex = dataModel.getCardsPlayedThisRound() % 4;
		
		int suitIndex = CardStringFunctions.getIndexOfSuit(card);
		//if(playerIndex == Constants.CURRENT_AGENT_INDEX) {
			//Don't feel like tracking own signals yet...
		//	return;
		//}
		
		if(playerIndexKingSacrificeForSuit[suitIndex] == playerIndex) {
			playerIndexKingSacrificeForSuit[suitIndex] = NO_KING_SACRIFICE;
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
			//TODO: add strong queen signal here...
			//(EX: If player lead KD and has a 5D later, it's pretty clear that player also has the QD)
			//Unless queen was played...
		}
		
		
		//I'm going to start with the normal no mellow situation for now...
		if(dataModel.someoneBidMellow() == false
				|| dataModel.stillActiveMellow() == false) {
			
			//System.out.println("TEST: updateDataModelSignalsWithPlayedCard: ");
			
			
			if(throwerIndex == 0) {
				
				// Attempt to handle meaning of leading a king while the ace is still out:
				// TODO: It didn't change a single test case... yet :(
				
				if(dataModel.getRankIndex(card) == dataModel.KING
						&& dataModel.isCardPlayedInRound("A" + card.substring(1)) == false
						) {
					System.out.println("KING SACRIFICE!");
					playerIndexKingSacrificeForSuit[suitIndex] = playerIndex;
					
				} else if(dataModel.getRankIndex(card) == dataModel.ACE
						&& playerIndexKingSacrificeForSuit[suitIndex] == playerIndex) {

					System.out.println("KING UNSACRIFICE!");
					playerIndexKingSacrificeForSuit[suitIndex] = NO_KING_SACRIFICE;
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
						
						hardMinCardPlayedBecausePlayedUnderCurWinner[playerIndex][CardStringFunctions.getIndexOfSuit(card)] = dataModel.getRankIndex(card);
					
						
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
				
				
				// When 4th player fails to trump to make an easy trick, that means something:
				//TODO: make another one for the 3rd player...
				if(throwerIndex == 3
						&& CardStringFunctions.getIndexOfSuit(card) != dataModel.getSuitOfLeaderThrow()
						&& CardStringFunctions.getIndexOfSuit(card) != Constants.SPADE) {
					
					String curWinnerCard= dataModel.getCurrentFightWinningCardBeforeAIPlays();
					
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
				}
			}
			
			//if player index not 0:
				//for now, don't worry about your own signals
			
			//else
			
					//updateMaxCardPlayed
					// 
		}
	}
	

	
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
	
	//public int getPlayerIndexSignalledMasterQueen(int indexSuit) {
	//	return playerIndexQueenForSuit[indexSuit];
		
	//}
	
	//TODO: try using this later...
	public boolean playerStrongSignaledNoCardsOfSuit(int playerIndex, int suitIndex) {

		int curMinRank = hardMinCardPlayedBecausePlayedUnderCurWinner[playerIndex][suitIndex] + 1;
		
		if(hardMinCardRankBecausePlayedOverPartner[playerIndex][suitIndex] != -1
				&& curMinRank <  hardMinCardRankBecausePlayedOverPartner[playerIndex][suitIndex]) {
				
			curMinRank = hardMinCardRankBecausePlayedOverPartner[playerIndex][suitIndex];
		}
		
		
		int curMaxRank = DataModel.ACE;
		
		if(hardMaxCardPlayedBecauseLackOfTrump[playerIndex][suitIndex] != -1
				&& curMaxRank > hardMaxCardPlayedBecauseLackOfTrump[playerIndex][suitIndex]) {
			curMaxRank = hardMaxCardPlayedBecauseLackOfTrump[playerIndex][suitIndex];
		}
		
		if(hardMaxBecauseSomeoneElseSignalledMasterQueen[playerIndex][suitIndex] != -1
			&& curMaxRank > hardMaxBecauseSomeoneElseSignalledMasterQueen[playerIndex][suitIndex]) {
			curMaxRank = hardMaxBecauseSomeoneElseSignalledMasterQueen[playerIndex][suitIndex];
			System.out.println("DEBUG: difference was made!");
		}
		
		for(int rank=curMinRank; rank <= curMaxRank; rank++) {
			if(this.dataModel.getCardsCurrentlyHeldByPlayers()[playerIndex][suitIndex][rank] != dataModel.IMPOSSIBLE) {
				
				//SANITY TEST (TO DELETE)
				if(this.dataModel.isVoid(playerIndex, suitIndex)) {
					System.out.println("AHH! Wrong signal!");
					System.exit(1);
				}
				//END SANITY TEST
				
				
				return false;
			}
		}
		
		
		return true;
	}
	
	
	public int getMaxRankSpadeSignalled(int playerIndex) {
		int curMaxRank = DataModel.ACE;
		
		if(hardMaxCardPlayedBecauseLackOfTrump[playerIndex][Constants.SPADE] != -1
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
	

}

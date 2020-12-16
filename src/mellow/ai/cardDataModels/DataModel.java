package mellow.ai.cardDataModels;

import mellow.Constants;
import mellow.ai.simulation.SimulationSetup;
import mellow.ai.simulation.objects.SelectedPartitionAndIndex;
import mellow.cardUtils.*;

//TODO: randomize suit choice with pseudo random # generator of adding up card indexes.... (plus a seed?)
//It's fast and effective compared to using Math.random() and I want fast so I could do simulations!

//PRE: The DataModel expects queries from current player only when it's the current player's turn
//      AND current player has multiple choices.
public class DataModel {

	
	//TODO: MELLOW_PLAYER_SIGNALED_NO is just a rudimentary signal, feel free to develop it.
	static final int MELLOW_PLAYER_SIGNALED_NO = 300;
	
	static final int IMPOSSIBLE =0;
	static final int CERTAINTY = 1000;
	static final int DONTKNOW = -1;
	
	static final int BID_NOT_SET = -1;
	static final int CARD_NOT_PLAYED_YET = -1;
	static final int INDEX_SUCH_THAT_CURRENT_PLAYER_BIDS_FIRST = Constants.NUM_PLAYERS - 1;
	
	//This is dumb: Think about changing it later.
	private static final int ACE = 12;
	public static final int KING = 11;
	private static final int QUEEN = 10;
	private static final int JACK = 9;
	private static final int RANK_TEN = 8;
	private static final int RANK_NINE = 7;
	private static final int RANK_EIGHT = 6;
	private static final int RANK_SEVEN = 5;
	public static final int RANK_SIX = 4;
	public static final int RANK_FIVE = 3;
	public static final int RANK_FOUR = 2;
	private static final int RANK_THREE = 1;
	private static final int RANK_TWO = 0;
	

	public int getOurScore() {
		return AIScore;
	}

	public int getOpponentScore() {
		return OpponentScore;
	}

	public int getDealerIndexAtStartOfRound() {
		return dealerIndexAtStartOfRound;
	}

	public void setDealerIndexAtStartOfRound(int dealerIndexAtStartOfRound) {
		this.dealerIndexAtStartOfRound = dealerIndexAtStartOfRound;
	}

	public void setNewScores(int AIScore, int OpponentScore) {
		this.AIScore = AIScore;
		this.OpponentScore = OpponentScore;
	}

	public void setDealer(String playerName) {
		int index = convertPlayerNameToIndex(playerName);
		if(index == -1) {
			System.err.println("ERROR: unknown dealer in setDealer. (" + playerName + ")");
		}
		dealerIndexAtStartOfRound = convertPlayerNameToIndex(playerName);
	}
	
	//Beginning of Round info:
	private int AIScore;
	private int OpponentScore;
	private int dealerIndexAtStartOfRound;
	//End Beginning of Round info

	private boolean cardsUsed[][] = new boolean[Constants.NUM_SUITS][Constants.NUM_RANKS];
	
	private int cardsCurrentlyHeldByPlayer[][][] = new int[Constants.NUM_PLAYERS][Constants.NUM_SUITS][Constants.NUM_RANKS];
	
	private boolean cardsUsedByPlayer[][][] = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS][Constants.NUM_RANKS];

	//Map:
	//  2  3  4 5 .. A
	//S
	//H
	//D
	//C
	
	
	private String players[] = new String[Constants.NUM_PLAYERS];
	
	public String[] getPlayers() {
		return players;
	}


	private int bidsMadeThisRound = 0;
	private int bids[] = new int[Constants.NUM_PLAYERS];
	private int tricks[] = new int[Constants.NUM_PLAYERS];
	
	private int cardsPlayedThisRound = 0;
	private String cardStringsPlayed[] = new String[Constants.NUM_CARDS];
	private int playerWhoPlayedCard[] = new int[Constants.NUM_CARDS];
	
	private int simulation_level = 0;
	
	public DataModel createHardCopy() {
		DataModel copy = new DataModel();
		
		copy.AIScore = AIScore;
		copy.OpponentScore = OpponentScore;
		copy.dealerIndexAtStartOfRound = dealerIndexAtStartOfRound;
		
		for(int i=0; i<cardsUsed.length; i++) {
			for(int j=0; j<cardsUsed[0].length; j++) {
				copy.cardsUsed[i][j] = cardsUsed[i][j];
			}
		}
		
		for(int i=0; i<cardsCurrentlyHeldByPlayer.length; i++) {
			for(int j=0; j<cardsCurrentlyHeldByPlayer[0].length; j++) {
				for(int k=0; k<cardsCurrentlyHeldByPlayer[0][0].length; k++) {
					copy.cardsCurrentlyHeldByPlayer[i][j][k] = cardsCurrentlyHeldByPlayer[i][j][k];
					copy.cardsUsedByPlayer[i][j][k] = cardsUsedByPlayer[i][j][k];
				}
			}
		}
		
		copy.bidsMadeThisRound = bidsMadeThisRound;
		copy.cardsPlayedThisRound = cardsPlayedThisRound;
		
		for(int i=0; i<players.length; i++) {
			copy.players[i] = players[i];
			copy.bids[i] = bids[i];
			copy.tricks[i] = tricks[i];
		}
		
		for(int i=0; i<Constants.NUM_CARDS; i++) {
			copy.cardStringsPlayed[i] = cardStringsPlayed[i];
			copy.playerWhoPlayedCard[i] = playerWhoPlayedCard[i];
		}
		
		copy.simulation_level = simulation_level;
		return copy;
	}

	//Makes a data model representing a possible view from another player's perspective
	public DataModel getDataModelFromPerspectiveOfPlayerI(int playerIndex, String simulatedUnknownCardDist[][]) {
		
		if(playerIndex == Constants.CURRENT_AGENT_INDEX) {
			return createHardCopy();
		}
		
		DataModel playerDM = new DataModel();

		playerDM.resetStateForNewRound();
		
		if(playerIndex == Constants.CURRENT_PARTNER_INDEX) {
			playerDM.AIScore = AIScore;
			playerDM.OpponentScore = OpponentScore;
		} else {
			playerDM.OpponentScore = OpponentScore;
			playerDM.AIScore = AIScore;
		}
		
		for(int i=0; i<Constants.NUM_PLAYERS; i++) {
			playerDM.players[translateIndexToOtherPlayerPerspective(playerIndex, i)] = players[i];
		}
		
		String cardsHeld[] = getGuessAtOriginalCardsHeld(playerIndex, simulatedUnknownCardDist[playerIndex]);

	//Get player up-to-date with what happened during the round:
		
		//Set Cards-in-hand and reset knowledge of round....
		playerDM.setupCardsInHandForNewRound(cardsHeld);
		
		playerDM.dealerIndexAtStartOfRound = translateIndexToOtherPlayerPerspective(playerIndex, dealerIndexAtStartOfRound);
		
	//Set bids:
		for(int i=0; i<bidsMadeThisRound; i++) {
			int bidIndexDataModel = (dealerIndexAtStartOfRound + 1 + i ) % Constants.NUM_PLAYERS;
			int bidIndexPers = translateIndexToOtherPlayerPerspective(playerIndex, bidIndexDataModel);
			
			playerDM.bids[bidIndexPers] = bids[bidIndexDataModel];
		}
		playerDM.bidsMadeThisRound = bidsMadeThisRound;
		
	//Set cards played:
		for(int i=0; i<cardsPlayedThisRound; i++) {
			playerDM.updateDataModelWithPlayedCard(players[ playerWhoPlayedCard[i] ], cardStringsPlayed[i]);
		}
		
		playerDM.simulation_level = simulation_level;
		
		return playerDM;
	}
	
	public String[] getGuessAtOriginalCardsHeld(int playerIndex, String simulatedUnknownCardsGivenToPlayer[]) {

		String cardsHeld[] = new String[Constants.NUM_STARTING_CARDS_IN_HAND];
		int curNumCardsGiven = 0;
		
		//get player the cards the data model already knows about
		for(int suit=0; suit<Constants.NUM_SUITS; suit++) {
			for(int rank=0; rank<Constants.NUM_RANKS; rank++) {
				if(cardsUsedByPlayer[playerIndex][suit][rank] || cardsCurrentlyHeldByPlayer[playerIndex][suit][rank] == CERTAINTY) {
					cardsHeld[curNumCardsGiven] = getCardString(rank, suit);
					curNumCardsGiven++;
				}
			}
		}

		//Give player the unknown cards that we are guessing it has:
		for(int i=0; curNumCardsGiven < Constants.NUM_STARTING_CARDS_IN_HAND; i++, curNumCardsGiven++) {
			cardsHeld[curNumCardsGiven] = simulatedUnknownCardsGivenToPlayer[i];
		}
		
		if(curNumCardsGiven != Constants.NUM_STARTING_CARDS_IN_HAND) {
			System.err.println("ERROR: something went wrong in getDataModelFromPerspectiveOfPlayerI. Number of cards given: " + curNumCardsGiven + "(Expected: " + Constants.NUM_STARTING_CARDS_IN_HAND + ")");
			System.exit(1);
		}
		
		cardsHeld = CardStringFunctions.sort(cardsHeld);
		
		return cardsHeld;
	}
	
	public static int translateIndexToOtherPlayerPerspective(int playerIndex, int index) {
		return (index  - playerIndex + Constants.NUM_PLAYERS) % Constants.NUM_PLAYERS;
	}
	
	public int getSimulation_level() {
		return simulation_level;
	}
	
	public void incrementSimulationLevel() {
		simulation_level++;
	}

	public int getCardsPlayedThisRound() {
		return cardsPlayedThisRound;
	}
	
	public void setBid(String playerName, int bid) {

		//Sanity check:
		//The player on the left of the playerName is dealer if playerName made the first bid:
		if(bidsMadeThisRound == 0) {
			
			int dealerIndexAtStartOfRoundSanityCheck = (convertPlayerNameToIndex(playerName) + Constants.NUM_PLAYERS - 1) % Constants.NUM_PLAYERS;
			if(dealerIndexAtStartOfRound != dealerIndexAtStartOfRoundSanityCheck) {
				System.err.println("ERROR: (setBid) dealer Index at start of round isn't consistent with player who bid first");
				System.exit(1);
			}
		}
		//END Sanity check

		int playerIndex = convertPlayerNameToIndex(playerName);
		bids[playerIndex] = bid;
		
		//Handle signals
		if(bid == 0) {

			//TODO: One day, consider the Hail Mary mellow with AS play....
			//I think that's viable as a strat to get opponent to also say mellow near the end of game...
			
			//For now, assume that if there's a mellow bid, player has no AS
			setCardMellowSignalNoIfUncertain(playerIndex, Constants.SPADE, ACE);
		}
		//End Handle signals
		
		bidsMadeThisRound++;
	}
	
	public int getBid(String playerName) {
		return bids[convertPlayerNameToIndex(playerName)];
	}
	
	public int getBid(int indexPlayer) {
		return bids[indexPlayer];
	}
	
	public int getTricks(String playerName) {
		return tricks[convertPlayerNameToIndex(playerName)];
	}
	
	public int getTrick(int indexPlayer) {
		return tricks[indexPlayer];
	}
	
	public boolean someoneBidMellow() {
		for(int i=0; i<bids.length; i++) {
			if(bids[i] == 0) {
				return true;
			}
		}
		return false;
	}

	public boolean saidMellow(int playerIndex) {
		return bids[playerIndex] == 0;
	}
	
	public boolean burntMellow(int playerIndex) {
		return bids[playerIndex] == 0 && tricks[playerIndex] > 0;
	}
	
	public boolean isVoid(int playerIndex, int suitIndex) {
		
		for(int i=RANK_TWO; i<=ACE; i++) {
			if(cardsCurrentlyHeldByPlayer[playerIndex][suitIndex][i] != IMPOSSIBLE) {
				return false;
			}
		}
		return true;
	}
	

	//Resets everything except for the scores and the deal indexes.
	public void resetStateForNewRound() {
		
		//dealerIndex will be set properly after the first bid
		//Before the bid, assume dealer is such that current player is first to bid... just in case it is first to bid
		dealerIndexAtStartOfRound = INDEX_SUCH_THAT_CURRENT_PLAYER_BIDS_FIRST;
		
		cardsUsed = new boolean[Constants.NUM_SUITS][Constants.NUM_RANKS];
		cardsCurrentlyHeldByPlayer = new int[Constants.NUM_PLAYERS][Constants.NUM_SUITS][Constants.NUM_RANKS];
		cardsUsedByPlayer = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS][Constants.NUM_RANKS];
		cardsPlayedThisRound =0;
		cardsPlayedThisRound = 0;
		cardStringsPlayed = new String[Constants.NUM_CARDS];
		playerWhoPlayedCard = new int[Constants.NUM_CARDS];
		
		bids = new int[Constants.NUM_PLAYERS];
		tricks = new int[Constants.NUM_PLAYERS];
		
		
		for(int i=0; i<bids.length; i++) {
			bids[i] = BID_NOT_SET;
		 }
		
		 for(int i=0; i<Constants.NUM_SUITS*Constants.NUM_RANKS; i++) {
			 cardStringsPlayed[i] = "";
			 playerWhoPlayedCard[i] = CARD_NOT_PLAYED_YET;
		 }

		resetCardKnowledgeTableForNewRound();
		 
	}
	
	public void setupCardsInHandForNewRound(String cards[]) {
		
		//System.out.println("Printing cards");
		 
		 for(int i=0; i<cards.length; i++) {
			//System.out.println(cards[i]);
			cardsCurrentlyHeldByPlayer[0][getMellowCardIndex(cards[i])/13][getMellowCardIndex(cards[i])%13]  = CERTAINTY;
			cardsCurrentlyHeldByPlayer[1][getMellowCardIndex(cards[i])/13][getMellowCardIndex(cards[i])%13]  = IMPOSSIBLE;
			cardsCurrentlyHeldByPlayer[2][getMellowCardIndex(cards[i])/13][getMellowCardIndex(cards[i])%13]  = IMPOSSIBLE;
			cardsCurrentlyHeldByPlayer[3][getMellowCardIndex(cards[i])/13][getMellowCardIndex(cards[i])%13]  = IMPOSSIBLE;
			
		}
		
	}

	
	
	private void resetCardKnowledgeTableForNewRound() {
		for(int i=0; i<cardsUsed.length; i++) {
			for(int j=0; j<cardsUsed[0].length; j++) {
				cardsUsed[i][j] = false;
				for(int k=0; k< Constants.NUM_PLAYERS; k++) {
					cardsUsedByPlayer[k][i][j] = false;
				}
				for(int k=1; k< Constants.NUM_PLAYERS; k++) {
					cardsCurrentlyHeldByPlayer[k][i][j] = DONTKNOW;
				}
				cardsCurrentlyHeldByPlayer[0][i][j] = IMPOSSIBLE;
			}
		}
		
	}
	
	
	
	public void updateDataModelWithPlayedCard(String playerName, String card) {
		
		int indexPlayer = convertPlayerNameToIndex(playerName);
		int cardNum = getMellowCardIndex(card);

		//Sanity check:
		if(cardsCurrentlyHeldByPlayer[indexPlayer][cardNum/Constants.NUM_RANKS][cardNum%Constants.NUM_RANKS] == IMPOSSIBLE) {
			System.err.println("ERROR: card played is supposedly impossible in updateDataModelWithPlayedCard " + players[Constants.CURRENT_AGENT_INDEX]);
			System.exit(1);
		}
		//End sanity check
		
		//Update tricks (if needed)
		handleTrickIfPlayedCardIs4thThrow(indexPlayer, card);
		
		cardsUsed[cardNum/Constants.NUM_RANKS][cardNum%Constants.NUM_RANKS] = true;
		cardsUsedByPlayer[indexPlayer][cardNum/Constants.NUM_RANKS][cardNum%Constants.NUM_RANKS] = true;
		for(int i=0; i<Constants.NUM_PLAYERS; i++) {
			cardsCurrentlyHeldByPlayer[i][cardNum/Constants.NUM_RANKS][cardNum%Constants.NUM_RANKS] = IMPOSSIBLE;
		}
		
		
		cardStringsPlayed[cardsPlayedThisRound] = card;
		playerWhoPlayedCard[cardsPlayedThisRound] = indexPlayer;
		
		//Check if non-leading player didn't follow suit (and is void in suit)
		if(CardStringFunctions.getIndexOfSuit(card) != getSuitOfLeaderThrow()) {
			for(int i=RANK_TWO; i<=ACE; i++) {
				cardsCurrentlyHeldByPlayer[indexPlayer][getSuitOfLeaderThrow()][i] = IMPOSSIBLE;
			}
		}
		//End check


		if(indexPlayer != Constants.CURRENT_AGENT_INDEX) {
			updateDataModelSignalsWithPlayedCard(playerName, card);
		}
		 
		cardsPlayedThisRound++;
		
		numWaysOthersPlayersCouldHaveCards = NEED_TO_RECALCULATE;
		do {
			logicallyDeduceWhoHasCardsByProcessOfElimination();
		} while(logicallyDeduceEntireOpponentHandFoundSomething());
		
		//TODO:
		
		
	}
	
	//THIS IS JUST A SIMPLE 1st attempt!

	//So far, it only deals with mellow signals
	public void updateDataModelSignalsWithPlayedCard(String playerName, String card) {
		
		int playerIndex = convertPlayerNameToIndex(playerName);


		//Deal with mellow signals
		
		if(bids[playerIndex] == 0 && burntMellow(playerIndex) == false) {
			int throwNumber = cardsPlayedThisRound % Constants.NUM_PLAYERS;
			int cardNum = getMellowCardIndex(card);

			
			if(throwNumber == 0) {
				//If throwNumber 0, mellow player lead first card
				// and now it's the second player's turn
				
				//and the first card lead by a mellow player is usually weird... ignore leading
				//TODO: don't ignore in future!
				
			} else {
				
				int suitLeadIndex = getSuitOfLeaderThrow();
				
				if(CardStringFunctions.getIndexOfSuit(card) == suitLeadIndex) {
					//Mellow following lead
					
					//TODO: Assumes mellow play doesn't know if it could be protected....
					// Did this to keep it simple.

					if(getCurrentFightWinningCard().equals(card)) {
						
						//Mellow follows suit over, probably has nothing under.
						//Mellow player probably doesn't have cards under card mellow player threw.
						
						// TODO: There's an exception if it's 2nd or 3rd thrower and knows last player must play
						// above, but whatever...
						
						for(int rankIndex=getRankIndex(card) - 1 ; rankIndex >= RANK_TWO; rankIndex--) {
							//TODO: if there's another state, we will need to make a complicate state transition table
							//MELLOW IND -> LEAD_SUGGESTION ...
							setCardMellowSignalNoIfUncertain(playerIndex, suitLeadIndex, rankIndex);
						}
						
					} else {
						
						//Mellow follows suit under
						//Mellow player probability doesn't have cards between curFightWinner and card mellow player threw.
						
						String curWinningCard = getCurrentFightWinningCard();
						
						int suitCurrentWinning = CardStringFunctions.getIndexOfSuit(curWinningCard);
						
						int maxRankIndex = getRankIndex(getCurrentFightWinningCard());
						
						if(suitCurrentWinning == Constants.SPADE && suitLeadIndex != Constants.SPADE) {
							maxRankIndex = ACE;
						}
						
						for(int rankIndex=getRankIndex(card) + 1 ; rankIndex <= maxRankIndex; rankIndex++) {

							//TODO: if there's another state, we will need to make a complicate state transition table
							//MELLOW IND -> LEAD_SUGGESTION ...
							setCardMellowSignalNoIfUncertain(playerIndex, suitLeadIndex, rankIndex);
						}
					}
					
				} else {
					///Mellow not following suit (Mellow is throwing off)
					
					int mellowSuitPlayed = CardStringFunctions.getIndexOfSuit(card);
					
					//If mellow player played spade and the partner can't bail them out:
					if(mellowSuitPlayed == Constants.SPADE && throwNumber >= 1) {
						
						if(getCurrentFightWinningCard().equals(card)) {

							//TODO: if it's unrealistic (EX: it would mean mel has 6 spades, then reconsider signal)\
							
							//If their card is winning, they probably don't have a choice:
							for(int cardIndex=0; cardIndex < Constants.NUM_CARDS; cardIndex++) {

								int suitIndex = cardIndex / Constants.NUM_RANKS;
								int rankIndex = cardIndex % Constants.NUM_RANKS;
								
								if(suitIndex == Constants.SPADE) {
									continue;
								} else {
									setCardMellowSignalNoIfUncertain(playerIndex, suitIndex, rankIndex);
								}
								
							}
						} else {
							//Mellow is just trumping under previous trump
							
							//Mellow prob doesn't have spade between Mellow card played,
							//and the fight winner card played
							int rankCurrentFightWinner = getRankIndex(getCurrentFightWinningCard());
							
							for(int rankIndex = getRankIndex(card) + 1; rankIndex < rankCurrentFightWinner; rankIndex++) {
								
								//TODO: if there's another state, we will need to make a complicate state transition table
								//MELLOW IND -> LEAD_SUGGESTION ...
								setCardMellowSignalNoIfUncertain(playerIndex, Constants.SPADE, rankIndex);
							}
						}
						
					} else {
						
						//If Mellow player plays plays off,
						//Mellow player prob doesn't have anything over that offsuit
						for(int rankIndex=getRankIndex(card) + 1 ; rankIndex <= ACE; rankIndex++) {
							setCardMellowSignalNoIfUncertain(playerIndex, mellowSuitPlayed, rankIndex);
						}
					}
					
				}
				
				
			}
		
		}
	}
	
	
	public boolean mellowPlayerSignalNoCardsOfSuit(int mellowPlayerIndex, int suitIndex) {
		if( getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(mellowPlayerIndex, suitIndex) == null ) {
			return true;
		} else {
			return false;
		}
	}
	
	
	public boolean mellowPlayerMayBeInDangerInSuit(int mellowPlayerIndex, int suitIndex) {
		
		String maxCardMellowSignalledCouldHave = getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(mellowPlayerIndex, suitIndex);
		
		if(maxCardMellowSignalledCouldHave == null) {
			return false;
		}
		
		String currentlyWinningCard = this.getCurrentFightWinningCard();
		
		if(CardStringFunctions.getIndexOfSuit(currentlyWinningCard) != suitIndex) {
			
			if(suitIndex == Constants.SPADE) {
				return true;
			} else {
				return false;
			}
		}

		int rankPossibleMellowCard = this.getRankIndex(maxCardMellowSignalledCouldHave);
		int rankCurrentlyWinningCard = this.getRankIndex(currentlyWinningCard);
		
		if(rankPossibleMellowCard > rankCurrentlyWinningCard) {
			return true;
		} else {
			return false;
		}
	}
	
	public String getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(int mellowPlayerIndex, int suitIndex) {
		for(int rank=ACE; rank>=RANK_TWO; rank--) {
			if(cardsCurrentlyHeldByPlayer[mellowPlayerIndex][suitIndex][rank] != IMPOSSIBLE
					&& cardsCurrentlyHeldByPlayer[mellowPlayerIndex][suitIndex][rank] != MELLOW_PLAYER_SIGNALED_NO) {
				return getCardString(rank, suitIndex);
			}
			
		}
		
		return null;
	}
	
public boolean mellowSignalledNoCardOverCardSameSuit(String inputCard, int mellowPlayerIndex) {
		
		boolean cardsOverInputCard[][] = this.getCardsStrictlyMorePowerfulThanCard(inputCard);
		
		int suitIndex = CardStringFunctions.getIndexOfSuit(inputCard);
		
		for(int j=0; j<Constants.NUM_RANKS; j++) {
			
			if(cardsOverInputCard[suitIndex][j]) {

				if(cardsCurrentlyHeldByPlayer[mellowPlayerIndex][suitIndex][j] != IMPOSSIBLE
						&& cardsCurrentlyHeldByPlayer[mellowPlayerIndex][suitIndex][j] != MELLOW_PLAYER_SIGNALED_NO) {
					
					//At this point, the mellow player signalled that they could have a card in between
					//And you should feel nervous about playing over the currently winning card...
					return false;
				}
			}
		}
		
		return true;
		
	}

	public boolean mellowSignalledNoCardBetweenTwoCards(String currentlyWinningCard, String cardInHand, int mellowPlayerIndex) {
		
		boolean cardsOverWinning[][] = this.getCardsStrictlyMorePowerfulThanCard(currentlyWinningCard);
		boolean cardsUnderConsideredCard[][] = this.getCardsStrictlyLessPowerfulThanCard(cardInHand);
		
		for(int i=0; i<Constants.NUM_SUITS; i++) {
			for(int j=0; j<Constants.NUM_RANKS; j++) {
				
				if(cardsOverWinning[i][j] && cardsUnderConsideredCard[i][j]) {

					if(cardsCurrentlyHeldByPlayer[mellowPlayerIndex][i][j] != IMPOSSIBLE
							&& cardsCurrentlyHeldByPlayer[mellowPlayerIndex][i][j] != MELLOW_PLAYER_SIGNALED_NO) {
						
						//At this point, the mellow player signalled that they could have a card in between
						//And you should feel nervous about playing over the currently winning card...
						return false;
					}
				}
			}
		}
		
		return true;
		
	}
	
	//TODO: if mellow has card even though the player signal he/she doesn't: note that down!
	public void setCardMellowSignalNoIfUncertain(int playerIndex, int suitIndex, int rankIndex) {
		if(cardsCurrentlyHeldByPlayer[playerIndex][suitIndex][rankIndex] != CERTAINTY
				&& cardsCurrentlyHeldByPlayer[playerIndex][suitIndex][rankIndex] != IMPOSSIBLE) {
					cardsCurrentlyHeldByPlayer[playerIndex][suitIndex][rankIndex] = MELLOW_PLAYER_SIGNALED_NO;
				}
	}
	
	
	public boolean logicallyDeduceEntireOpponentHandFoundSomething() {

		boolean foundSomething = false;
		
		for(int playerIndex=0; playerIndex<4; playerIndex++) {
			if(playerIndex == Constants.CURRENT_AGENT_INDEX) {
				continue;
			}
			
			int numCardsLeft = Constants.NUM_STARTING_CARDS_IN_HAND;
			
			for(int i=0; i<cardsUsedByPlayer[0].length; i++) {
				for(int j=0; j<cardsUsedByPlayer[0][0].length; j++) {
					if(cardsUsedByPlayer[playerIndex][i][j]) {
						numCardsLeft--;
					}
				}
			}
			//System.out.println("Num cards left: " + numCardsLeft);
			
			int numPossibleCards = 0;
			for(int i=0; i<cardsCurrentlyHeldByPlayer[0].length; i++) {
				for(int j=0; j<cardsCurrentlyHeldByPlayer[0][0].length; j++) {
					if(cardsCurrentlyHeldByPlayer[playerIndex][i][j] != IMPOSSIBLE) {
						numPossibleCards++;
					}
				}
			}

			//System.out.println("Num possible cards: " + numPossibleCards);
			
			if(numPossibleCards == numCardsLeft) {
				for(int i=0; i<cardsCurrentlyHeldByPlayer[0].length; i++) {
					for(int j=0; j<cardsCurrentlyHeldByPlayer[0][0].length; j++) {
						if(cardsCurrentlyHeldByPlayer[playerIndex][i][j] != IMPOSSIBLE) {
							
							if(cardsCurrentlyHeldByPlayer[playerIndex][i][j] != CERTAINTY) {
								
								//if(foundSomething == false) {
								//	System.out.println("TEST: making cards impossible");
								//}
								
								foundSomething = true;
							}
							
							for(int k=0; k<Constants.NUM_PLAYERS; k++) {
								if(k == playerIndex) {
									cardsCurrentlyHeldByPlayer[playerIndex][i][j] = CERTAINTY;
								} else {
									cardsCurrentlyHeldByPlayer[k][i][j] = IMPOSSIBLE;
								}
							}
							
						}
					}
				}
				
				
			} else if(numPossibleCards < numCardsLeft) {
				System.err.println("ERROR: something bad happened in updateDataModelWithPlayedCard");
				System.exit(1);
			}


		}
		
		return foundSomething;
		
	}
	
	//Does what it says, but I haven't really tested it...
	//I guess it works?
	private void logicallyDeduceWhoHasCardsByProcessOfElimination() {

		for(int i=0; i<cardsCurrentlyHeldByPlayer[0].length; i++) {
			for(int j=0; j<cardsCurrentlyHeldByPlayer[0][0].length; j++) {
				if(cardsUsed[i][j] == false && cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][i][j] != CERTAINTY) {

					int numImpossible = 0;
					
					for(int k=0; k<cardsCurrentlyHeldByPlayer.length; k++) {
						if(cardsCurrentlyHeldByPlayer[k][i][j] == IMPOSSIBLE) {
							numImpossible++;
						}
					}
					
					if(numImpossible == 3) {
						int numCertain = 0;
						for(int k=0; k<cardsCurrentlyHeldByPlayer.length; k++) {
							if(cardsCurrentlyHeldByPlayer[k][i][j] != IMPOSSIBLE) {
								
								if(cardsCurrentlyHeldByPlayer[k][i][j] != CERTAINTY) {
									//System.out.println("TEST: FOUND THAT CARD IS CERTAIN!");
								}
								cardsCurrentlyHeldByPlayer[k][i][j] = CERTAINTY;
								numCertain++;
							}
						}
						
						if(numCertain != 1) {
							System.err.println("ERROR: the logic for figuring out who has what card is messed up! Num certain: " + numCertain);
							System.exit(1);
						}
						
					} else if(numImpossible == 4) {
						System.err.println("ERROR: there's a card that isn't accounted for! player: " + players[Constants.CURRENT_AGENT_INDEX]);
						System.exit(1);
					}
				}
			}
		}
	}
	
	
	public static final long NEED_TO_RECALCULATE = -1;
	private long numWaysOthersPlayersCouldHaveCards = NEED_TO_RECALCULATE;

	public long getCurrentNumWaysOtherPlayersCouldHaveCards() {
		if(numWaysOthersPlayersCouldHaveCards != NEED_TO_RECALCULATE) {
			return numWaysOthersPlayersCouldHaveCards;
		}
		
		String unknownCards[] = getUnknownCards();
		int curNumUnknownCardsPerSuit[] = CardStringFunctions.organizeCardsBySuit(unknownCards);
		boolean originalIsVoidList[][] = createVoidArray();
		int numSpacesAvailPerPlayer[] = getNumUnknownSpaceAvailablePerPlayer();

		return SimulationSetup.getNumberOfWaysToSimulate(curNumUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList);
	}
	
	public String[][] getPossibleDistributionOfUnknownCardsBasedOnIndex(long combinationIndex) {
		
		String unknownCards[] = getUnknownCards();
		int curNumUnknownCardsPerSuit[] = CardStringFunctions.organizeCardsBySuit(unknownCards);
		boolean originalIsVoidList[][] = createVoidArray();
		int numSpacesAvailPerPlayer[] = getNumUnknownSpaceAvailablePerPlayer();
		
		SelectedPartitionAndIndex suitPartitionsAndComboNumbers = 
				SimulationSetup.getSelectedPartitionAndIndexBasedOnCombinationIndex(curNumUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, combinationIndex);
		
		return SimulationSetup.serveCarsdsBasedOnPartitionAndIndexInfo(suitPartitionsAndComboNumbers, unknownCards, numSpacesAvailPerPlayer);
		
	}
	
	public int[] getNumUnknownSpaceAvailablePerPlayer() {
		int ret[] = new int[Constants.NUM_PLAYERS];
		
		for(int player=0; player<Constants.NUM_PLAYERS; player++) {
			ret[player] = Constants.NUM_STARTING_CARDS_IN_HAND;
		}
		
		for(int i=0; i<Constants.NUM_CARDS; i++) {
			boolean cardFound = false;
			
			for(int player=0; player<Constants.NUM_PLAYERS && cardFound == false; player++) {
				if(cardsUsedByPlayer[player][i/Constants.NUM_RANKS][i%Constants.NUM_RANKS]) {
					ret[player]--;
					cardFound = true;
				}
			}
		}
		
		for(int i=0; i<Constants.NUM_CARDS; i++) {
			boolean cardFound = false;
			
			for(int player=0; player<Constants.NUM_PLAYERS && cardFound == false; player++) {
				if(cardsCurrentlyHeldByPlayer[player][i/Constants.NUM_RANKS][i%Constants.NUM_RANKS] == CERTAINTY) {
					ret[player]--;
					cardFound = true;
				}
			}
		}
		
		return ret;
	}
	
	private boolean[][] createVoidArray() {
		boolean ret[][] = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		for(int i=0; i<ret.length; i++) {
			for(int j=0; j<ret[0].length; j++) {
				ret[i][j] = isVoid(i, j);
			}
		}
		return ret;
	}
	
	private String[] getUnknownCards() {

		int numUnknownCards = 0;
		
		NEXT_CARD:
		for(int i=0; i<Constants.NUM_CARDS; i++) {
			for(int player=0; player<Constants.NUM_PLAYERS; player++) {
				if(dontKnowIfPlayerHasCard(cardsCurrentlyHeldByPlayer[player][i/Constants.NUM_RANKS][i%Constants.NUM_RANKS])) {
					numUnknownCards++;
					continue NEXT_CARD;
				}
			}
		}
		
		String unknownCards[] = new String[numUnknownCards];
		int currentCardIndex = 0;
		
		NEXT_CARD:
		for(int i=0; i<Constants.NUM_CARDS; i++) {
			for(int player=0; player<Constants.NUM_PLAYERS; player++) {
				if(dontKnowIfPlayerHasCard(cardsCurrentlyHeldByPlayer[player][i/Constants.NUM_RANKS][i%Constants.NUM_RANKS])) {

					unknownCards[currentCardIndex] = getCardString(i);
					currentCardIndex++;
					continue NEXT_CARD;
				}
			}
		}
		
		return unknownCards;
	}
	
	private static boolean dontKnowIfPlayerHasCard(int statusNum) {
		return statusNum != CERTAINTY && statusNum != IMPOSSIBLE;
	}
	
	private void handleTrickIfPlayedCardIs4thThrow(int indexPlayer, String card) {
		int throwNumber = cardsPlayedThisRound % Constants.NUM_PLAYERS;
		
		if(throwNumber == 3) {
			if(cardAGreaterThanCardBGivenLeadCard(card, getCurrentFightWinningCard())) { 
				tricks[indexPlayer]++;
			} else {
				String winningCard = getCurrentFightWinningCard();
				
				if(getCardLeaderThrow().equals(winningCard)) {
					tricks[(indexPlayer + 1)%4]++;
				} else if(getCardSecondThrow().equals(winningCard)) {
					tricks[(indexPlayer + 2)%4]++;
				} else if(getCardThirdThrow().equals(winningCard)) {
					tricks[(indexPlayer + 3)%4]++;
				} else {
					System.err.println("ERROR: unknown fight winner!");
					System.exit(1);
				}
			
			}
		}
	}
	
	//Returns the index of the person who's turn is
	//where the player the data model represents is index 0.
	//This is allowed to be queried whenever
	public int getCurrentActionIndex() {
		int throwNumber = cardsPlayedThisRound % Constants.NUM_PLAYERS;

		if(throwNumber == 0) {
			if(cardsPlayedThisRound == 0) {
				int indexPlayer = (dealerIndexAtStartOfRound + 1) % Constants.NUM_PLAYERS;
				return indexPlayer;

			} else if(cardsPlayedThisRound > 0) {
				//Simulate fight:
				int numCardsPlayedBeforeFight = cardsPlayedThisRound - Constants.NUM_PLAYERS;
				
				int leadPlayerIndex = playerWhoPlayedCard[numCardsPlayedBeforeFight];
	
				String leadCard = cardStringsPlayed[numCardsPlayedBeforeFight];
				
				int leadSuit = CardStringFunctions.getIndexOfSuit(leadCard);
				
				String winningCard = leadCard;
				
				int currentWinningIndex = 0;
				
				for(int fightIndex=1; fightIndex<Constants.NUM_PLAYERS; fightIndex++) {
					
					String currentCard = cardStringsPlayed[numCardsPlayedBeforeFight + fightIndex];
					
					if(getCardPower(currentCard, leadSuit) > getCardPower(winningCard, leadSuit)) {
						winningCard = currentCard;
						currentWinningIndex = fightIndex;
					}
				}
				
				return (leadPlayerIndex + currentWinningIndex) % Constants.NUM_PLAYERS;

			} else {
				System.err.println("ERROR: Negative cards played... What?");
				System.exit(1);
				return -1;
			}
		} else {
			int indexPlayer = (getPrevThrowerIndex() + 1) % Constants.NUM_PLAYERS;
			return indexPlayer;
		}
	}
	
	private int getPrevThrowerIndex() {
		return playerWhoPlayedCard[cardsPlayedThisRound - 1];
	}

	public boolean currentThrowIsLeading() {
		if(cardsPlayedThisRound >= Constants.NUM_CARDS) {
			System.err.println("ERROR: the round is over... why do you care if current throw is leading?");
			System.exit(1);
		}
		return cardsPlayedThisRound % 4 == 0 && cardsPlayedThisRound < Constants.NUM_CARDS;
	}
	
	//pre: leader card thrown
	public String getCardLeaderThrow() {
		return cardStringsPlayed[cardsPlayedThisRound - (cardsPlayedThisRound%4) + 0];
	}
	
	//pre: 2nd card thrown
	public String getCardSecondThrow() {
		return cardStringsPlayed[cardsPlayedThisRound - (cardsPlayedThisRound%4) + 1];
	}
	//pre: 3rd card thrown
	public String getCardThirdThrow() {
		return cardStringsPlayed[cardsPlayedThisRound - (cardsPlayedThisRound%4) + 2];
	}
	
	
	//pre leader card thrown
	public int getSuitOfLeaderThrow() {
		return CardStringFunctions.getIndexOfSuit(cardStringsPlayed[cardsPlayedThisRound - (cardsPlayedThisRound%4) + 0]);
	}
	
	//pre 2nd card thrown
	public int getSuitOfSecondThrow() {
		return CardStringFunctions.getIndexOfSuit(cardStringsPlayed[cardsPlayedThisRound - (cardsPlayedThisRound%4) + 1]);
	}

	//pre 3rd card thrown
	public int getSuitOfThirdThrow() {
		return CardStringFunctions.getIndexOfSuit(cardStringsPlayed[cardsPlayedThisRound - (cardsPlayedThisRound%4) + 2]);
	}
	
	
	//pre: function is called when AI is deciding the 2nd, 3rd or 4th throu
	public String getCurrentFightWinningCard() {
		
		int throwNumber = cardsPlayedThisRound % Constants.NUM_PLAYERS;
		
		if(throwNumber == 0) {
			System.err.println("ERROR: calling get Current Fight Winning Card before 1st throw.");
			System.exit(1);
		}

		String currentWinner = getCardLeaderThrow();
		
		if(throwNumber > 1) {
			if(cardAGreaterThanCardBGivenLeadCard(getCardSecondThrow(), currentWinner)) {
				currentWinner = getCardSecondThrow();
			}
		}
		
		if(throwNumber > 2) {
			if(cardAGreaterThanCardBGivenLeadCard(getCardThirdThrow(), currentWinner)) {
				currentWinner = getCardThirdThrow();
			}
		}
		
		
		return currentWinner;
		
	}
	
	
	public boolean hasNonLeadCardInHandThatCanDecreaseChanceof4thThrowerWinning() {
		return getNonLeadCardInHandThatCanDecreaseChanceof4thThrowerWinning() != null;
	}
	
	//pre: thrower is 2nd or 3rd
	//post: null if can't play a card that might 
	public String getNonLeadCardInHandThatCanDecreaseChanceof4thThrowerWinning() {
		int throwNumber = cardsPlayedThisRound % Constants.NUM_PLAYERS;
		
		//Check preconditions:
		if(throwNumber == 0) {
			System.err.println("ERROR: calling getNonLeadCardInHandThatCanDecreaseChanceof4thThrowerWinning on 1st throw. (Not meant to do that)");
			System.exit(1);
		}
		
		if(throwNumber == 3) {
			System.err.println("ERROR: calling getNonLeadCardInHandThatCanDecreaseChanceof4thThrowerWinning on 4th throw. (Doesn\'t make sense)");
			System.exit(1);
		}
		
		//Get card that's thrown and is currently winning:
		String currentlyWinningCard = this.getCardLeaderThrow();
		
		if(throwNumber > 1) {
			if( this.cardAGreaterThanCardBGivenLeadCard(this.getCardSecondThrow(), this.getCardLeaderThrow())) {
				currentlyWinningCard = this.getCardSecondThrow();
			}	
		}

		boolean[][] cardsOverCurrentlyWinningCard = getCardsStrictlyMorePowerfulThanCard(currentlyWinningCard);
		
		int fourthThrowerIndex = 3 - throwNumber;
		
		//TODO: reduce # of loops by 2 by creating only 1 loop to go thru the cards (This will just make the code look cleaner)
		
		//For every card in current players hand, check if it can possible reduce the number of ways 4th thrower can win:
		for(int i=0; i<Constants.NUM_SUITS; i++) {
			for(int j=0; j<Constants.NUM_RANKS; j++) {
				String tempCard = getCardString(j, i);
				
				if(nonLeadPlayerCouldMaybeThrowCard(Constants.CURRENT_PLAYER_INDEX, tempCard)
						&& cardsCurrentlyHeldByPlayer[Constants.CURRENT_PLAYER_INDEX][i][j] == CERTAINTY
						&& cardsOverCurrentlyWinningCard[i][j]) {
					
					//At this point, we have a potential card that's playable, and stronger than the best card so far...
					//We just need to check if it could reduce the # of ways the 4th thrower can win:
					
					boolean[][] cardsUnderPotentialCard = getCardsStrictlyLessPowerfulThanCard(tempCard);
					
					//Fourth thrower index
					for(int suit=0; suit<Constants.NUM_SUITS; suit++) {
						for(int rank=0; rank<Constants.NUM_RANKS; rank++) {
							
							if(cardsUnderPotentialCard[suit][rank]
									&& cardsOverCurrentlyWinningCard[suit][rank]) {
								String tempCard2 = getCardString(rank, suit);

								if(nonLeadPlayerCouldMaybeThrowCard(fourthThrowerIndex, tempCard2) &&
										cardsCurrentlyHeldByPlayer[fourthThrowerIndex][suit][rank] != IMPOSSIBLE
										) {
									return tempCard;
								}
							}
							
						}
					}
				}
			}
		}
		
		return null;
	}
	
	
	
	public boolean nonLeadPlayerCouldMaybeThrowCard(int playerIndex, String card) {
		
		int dealerSuit = this.getSuitOfLeaderThrow();
		
		int suitIndex = CardStringFunctions.getIndexOfSuit(card);
		int rankIndex = getRankIndex(card);
		
		//Check if player could have card:
		if(cardsCurrentlyHeldByPlayer[playerIndex][suitIndex][rankIndex] != IMPOSSIBLE) {

			//Check if player throwing this card means she's reneging.
			if(isVoid(playerIndex, dealerSuit) == false && suitIndex != dealerSuit) {
				//reneging
				return false;
			} else {
				
				return true;
			}
		}
		
		return false;
	}
	
	public boolean[][] getCardsStrictlyLessPowerfulThanCard(String card) {
		
		int suitIndex = CardStringFunctions.getIndexOfSuit(card);
		int rankIndex = getRankIndex(card);
		
		boolean ret[][] = new boolean[Constants.NUM_SUITS][Constants.NUM_RANKS];

		for(int i=0; i<ret.length; i++) {
			for(int j=0; j<ret[0].length; j++) {
				ret[i][j] = false;
			}
		}
		
		int leadSuit = this.getSuitOfLeaderThrow();
		
		int upperLimitDealerSuit;
		
		//card is off-suit non trump
		if(suitIndex != Constants.SPADE && leadSuit != suitIndex ) {
			return ret;
		
		//Card trumps lead suit
		} else if(suitIndex == Constants.SPADE && leadSuit != Constants.SPADE) {
			upperLimitDealerSuit = ACE;
		
		//Card is lead suit
		} else if(suitIndex == leadSuit){
			upperLimitDealerSuit = rankIndex - 1;

		} else {
			System.err.println("ERROR in getCardsStrictlyLessPowerfulThanCard. Reached case that should be impossible");
			System.exit(1);
			upperLimitDealerSuit = -1;
		}
		
		//Get list of cards under card in leadsuit:
		for(int i=upperLimitDealerSuit; i>=RANK_TWO; i--) {
			ret[leadSuit][i] = true;
		}
		
		//Get list of off-suit non-trump cards
		for(int i=0; i<Constants.NUM_SUITS; i++) {
			if(i != Constants.SPADE && i != leadSuit) {
				for(int j=0; j<Constants.NUM_RANKS; j++) {
					ret[i][j] = true;
				}
			}
		}

		//Get spades under card
		if(suitIndex == Constants.SPADE) {
			int upperLimitSpadeSuit = rankIndex - 1;
			for(int i=upperLimitSpadeSuit; i>=RANK_TWO; i--) {
				ret[Constants.SPADE][i] = true;
			}
		}

		return ret;
	}
	
	public boolean[][] getCardsStrictlyMorePowerfulThanCard(String card) {
		int suitIndex = CardStringFunctions.getIndexOfSuit(card);
		int rankIndex = getRankIndex(card);
		
		boolean ret[][] = new boolean[Constants.NUM_SUITS][Constants.NUM_RANKS];

		for(int i=0; i<ret.length; i++) {
			for(int j=0; j<ret[0].length; j++) {
				ret[i][j] = false;
			}
		}
		
		int leadSuit = -1;
		
		if(this.currentThrowIsLeading() == false) {
			leadSuit = this.getSuitOfLeaderThrow();
		} else {
			leadSuit = CardStringFunctions.getIndexOfSuit(card);
		}
		
		int lowerLimitDealerSuit;
		
		//card is off-suit non trump
		if(suitIndex != Constants.SPADE && leadSuit != suitIndex ) {
			lowerLimitDealerSuit = RANK_TWO;
		
		//Card trumps lead suit
		} else if(suitIndex == Constants.SPADE && leadSuit != Constants.SPADE) {
			lowerLimitDealerSuit = ACE + 1;
		
		//Card is lead suit
		} else if(suitIndex == leadSuit){
			lowerLimitDealerSuit = rankIndex + 1;

		} else {
			System.err.println("ERROR in getCardsStrictlyMorePowerfulThanCard. Reached case that should be impossible");
			System.exit(1);
			lowerLimitDealerSuit = -1;
		}
		
		for(int i=lowerLimitDealerSuit; i<=ACE; i++) {
			ret[leadSuit][i] = true;
		}
		
		int lowerLimitSpadeSuit;
		if(suitIndex != Constants.SPADE) {
			lowerLimitSpadeSuit = RANK_TWO;
		} else {
			lowerLimitSpadeSuit = rankIndex + 1;
		}

		for(int i=lowerLimitSpadeSuit; i<=ACE; i++) {
			ret[Constants.SPADE][i] = true;
		}

		return ret;
	}
	
	public int getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(String card) {

		int rankStart = getRankIndex(card) + 1;
		int suitIndex = CardStringFunctions.getIndexOfSuit(card);
		
		int ret = 0;
		
		for(int i=rankStart; i<Constants.NUM_RANKS; i++) {
			if(cardsUsed[suitIndex][i] ||
				cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][suitIndex][i] == CERTAINTY) {
				//Doesn't count
			} else {
				ret++;
			}
		}
		return ret;
	}

	public int getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(String card) {

		int rankEnd = getRankIndex(card) -1;
		int suitIndex = CardStringFunctions.getIndexOfSuit(card);
		
		int ret = 0;
		
		for(int i=RANK_TWO; i<=rankEnd; i++) {
			if(cardsUsed[suitIndex][i] ||
				cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][suitIndex][i] == CERTAINTY) {
				//Doesn't count
			} else {
				ret++;
			}
		}
		return ret;
	}
	
	public boolean throwerHasCardToBeatCurrentWinner() {
		if(getCardClosestOverCurrentWinner() != null) {
			return true;
		} else {
			return false;
		}
	}
	
	public String getCardClosestOverCurrentWinner() {
		int throwNumber = cardsPlayedThisRound % Constants.NUM_PLAYERS;
		
		if(throwNumber == 0) {
			System.err.println("ERROR: calling couldGoOverCurrentWinner on 1st throw.");
			System.exit(1);
		}
		
		String currentWinnerCard = getCurrentFightWinningCard();
		int winnerSuitIndex = CardStringFunctions.getIndexOfSuit(currentWinnerCard);
		int winnerRankIndex = getRankIndex(currentWinnerCard);

		//Check if offsuit can win
		if(winnerSuitIndex != Constants.SPADE) {
			
			for(int i=winnerRankIndex + 1; i <= ACE; i++) {
				if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][winnerSuitIndex][i] == CERTAINTY) {
					return getCardString(i, winnerSuitIndex);
				}
			}
		}
		
		//Check if spade can win
		if(throwerCouldPlaySpade()) {
			int startRankIndex = RANK_TWO;
			
			if(winnerSuitIndex == Constants.SPADE) {
				startRankIndex = winnerRankIndex + 1;
			}
			
			for(int i=startRankIndex; i <= ACE; i++) {
				if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][Constants.SPADE][i] == CERTAINTY) {
					return getCardString(i, Constants.SPADE);
				}
			}
		}
		
		return null;
	}

	public boolean throwerCouldPlaySpade() {
		int throwNumber = cardsPlayedThisRound % Constants.NUM_PLAYERS;
		
		if(throwNumber == 0) {
			System.err.println("ERROR: calling getCardInHandClosestOverCurrentWinner on 1st throw.");
			System.exit(1);
		}

		if(currentAgentHasSuit(Constants.SPADE) == false) {
			return false;
		}
		
		int leadCardsuitIndex = CardStringFunctions.getIndexOfSuit(getCardLeaderThrow());
		
		if(leadCardsuitIndex == Constants.SPADE || throwerMustFollowSuit() == false) {
			return true;
		} else {
			return false;
		}
		
	}
	
	public boolean throwerMustFollowSuit() {
		int throwNumber = cardsPlayedThisRound % Constants.NUM_PLAYERS;
		
		if(throwNumber == 0) {
			System.err.println("ERROR: calling throwerMustFollowSuit on 1st throw.");
			System.exit(1);
		}
		
		int leadCardsuitIndex = CardStringFunctions.getIndexOfSuit(getCardLeaderThrow());
		
		for(int i=0; i<Constants.NUM_RANKS; i++) {
			if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][leadCardsuitIndex][i] == CERTAINTY) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isPrevThrowWinningFight() {

		int throwNumber = cardsPlayedThisRound % Constants.NUM_PLAYERS;
		if(throwNumber <= 0) {
			System.err.println("ERROR: calling get isPrevThrowWinningFight on 1st throw. (throw index: " + throwNumber + ")");
			System.exit(1);
		}
		
		if(throwNumber == 1) {
			return true;

		} else if(throwNumber == 2) {
			if(getCardSecondThrow().equals(getCurrentFightWinningCard())) {
				return true;
			}

		} else if(throwNumber == 3) {
			if(getCardThirdThrow().equals(getCurrentFightWinningCard())) {
				return true;
			}

		} else {
			System.err.println("ERROR: calling get isPrevThrowWinningFight and got impossible throw number. (throw index: " + throwNumber + ")");
			System.exit(1);
		}
		
		return false;
	}
	
	public boolean isPartnerWinningFight() {

		int throwNumber = cardsPlayedThisRound % Constants.NUM_PLAYERS;
		if(throwNumber <= 1) {
			System.err.println("ERROR: calling get isPartnerWinningFight 1st or 2nd throw. (throw index: " + throwNumber + ")");
			System.exit(1);
		}
		
		if(throwNumber == 2) {
			if(getCardLeaderThrow().equals(getCurrentFightWinningCard())) {
				return true;
			}
		} else if(throwNumber == 3) {
			if(getCardSecondThrow().equals(getCurrentFightWinningCard())) {
				return true;
			}
		} else {
			System.err.println("ERROR: calling get isPartnerWinningFight and got impossible throw number. (throw index: " + throwNumber + ")");
			System.exit(1);
		}
		
		return false;
	}
	
	//TODO: this is only useful for a bad player...
	public String getJunkiestCardToFollowLead() {
		int throwNumber = cardsPlayedThisRound % Constants.NUM_PLAYERS;

		if(throwNumber == 0) {
			System.err.println("ERROR: calling get getJunkiestCardToFollowLead on 1st throw.");
			System.exit(1);
		}
		
		int leadSuitIndex = getSuitOfLeaderThrow();

		//if must follow suit
		if(throwerMustFollowSuit()) {
			
			for(int i=RANK_TWO; i <= ACE; i++) {
				if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][leadSuitIndex][i] == CERTAINTY) {
					return getCardString(i, leadSuitIndex);
				}
			}
			System.err.println("In getJunkiestCardToFollowLead thrower must follow suit but has nothing in suit to throw. (Logically impossible)");
			System.exit(1);
			
		} else {
			
			//TODO: make the logic more sophisticated...
			
			//Play smallest off-suit:
			for(int i=RANK_TWO; i <= ACE; i++) {
				for(int j=1; j < Constants.NUM_SUITS; j++) {
					if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][j][i] == CERTAINTY) {
						return getCardString(i, j);
					}
				}
			}
			
			//Play smallest spade:
			for(int i=RANK_TWO; i <= ACE; i++) {
				if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][Constants.SPADE][i] == CERTAINTY) {
					return getCardString(i, Constants.SPADE);
				}
			}
			

			System.err.println("In getJunkiestCardToFollowLead thrower must not follow suit but has no off-suit to throw. (Logically impossible)");
			System.exit(1);
			
		}
		
		System.err.println("ERROR in getJunkiestCardToFollowLead");
		System.exit(1);
		
		return null;
	}
	
	public boolean throwerCanOnlyPlayOneCard() {
		return getNumCardsThatCouldBeThrown() == 1;
	}
	
	public String getOnlyCardCurrentPlayerCouldPlay() {
		if(throwerCanOnlyPlayOneCard() == false) {
			System.err.println("ERROR: calling getOnlyCardCurrentPlayerCouldPlay when player has more than 1 card to play");
			System.exit(1);
		}
		
		if(getNumCardsInCurrentPlayerHand() == 1) {
			return getLastRemainingCardInHand();
		} else {
			
			if(throwerMustFollowSuit() == false) {
				System.err.println("ERROR: calling getOnlyCardCurrentPlayerCouldPlay and player has more than 1 card,\n doesn\'t need to follow suit and apparrently has to play single card.\nThis doesn\'t happen in Mellow.");
				System.exit(1);
			}
			
			return getJunkiestCardToFollowLead();
		}
	}
	
	public String getLastRemainingCardInHand() {
		if(getNumCardsInCurrentPlayerHand() != 1) {
			System.err.println("ERROR: calling getLastCardInHand when player has more than 1 card to play");
			System.exit(1);
		}
		
		for(int i=0; i<Constants.NUM_SUITS; i++) {
			for(int j=RANK_TWO; j<=ACE; j++) {
				if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_PLAYER_INDEX][i][j] == CERTAINTY) {
					return getCardString(j, i);
				}
			}
		}
		
		System.err.println("ERROR: could not find last card in getLastCardInHand");
		System.exit(1);
		return null;
	}
	
	public int getNumCardsThatCouldBeThrown() {
		int throwerIndex = cardsPlayedThisRound % Constants.NUM_PLAYERS;

		if(throwerIndex == 0 || throwerMustFollowSuit() == false) {
			return getNumCardsInCurrentPlayerHand();
		}
	
		int leadSuit = getSuitOfLeaderThrow();
		int currentNumCards = 0;
		
		for(int i=RANK_TWO; i<=ACE; i++) {
			if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_PLAYER_INDEX][leadSuit][i] == CERTAINTY) {
				currentNumCards++;
			}
		}
		
		
		if(currentNumCards == 0) {
			System.err.println("ERROR: less than 1 card in hand according to getNumCardsThatCouldBeThrown");
			System.exit(1);
		}
		
		return currentNumCards;
		
		
	}
	
	//pre: input is a card
	public boolean isCardLegalToPlay(String card) {
		if(card == null || card.length() != 2) {
			return false;
		}
		
		int throwerIndex = cardsPlayedThisRound % Constants.NUM_PLAYERS;

		int rank = getRankIndex(card);
		int suit = CardStringFunctions.getIndexOfSuit(card);
		
		//Does player have card?
		if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_PLAYER_INDEX][suit][rank] == CERTAINTY) {
			
			//Could player play any card in hand?
			if(throwerIndex == 0 || throwerMustFollowSuit() == false) {
				return true;

			} else {
				//Could player follow suit with card
				int leadSuit = getSuitOfLeaderThrow();
				
				if(suit == leadSuit) {
					return true;
				} else {
					return false;
				}
			}
		} else {
			
			return false;
		}
	}
	
	public String getFirstLegalCardThatCouldBeThrown() {
		int throwerIndex = cardsPlayedThisRound % Constants.NUM_PLAYERS;

		if(throwerIndex == 0 || throwerMustFollowSuit() == false) {
			for(int suit=0; suit<Constants.NUM_SUITS; suit++) {
				for(int rank=RANK_TWO; rank<=ACE; rank++) {
					if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_PLAYER_INDEX][suit][rank] == CERTAINTY) {
						return getCardString(rank, suit);
					}
				}
			}
		}
	
		int leadSuit = getSuitOfLeaderThrow();
		
		for(int rank=RANK_TWO; rank<=ACE; rank++) {
			if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_PLAYER_INDEX][leadSuit][rank] == CERTAINTY) {
				return getCardString(rank, leadSuit);
			}
		}
		
		System.err.println("ERROR: Not supposed to reach this point in getFirstLegalCardThatCouldBeThrown");
		System.exit(1);
	
		return "";
	}
	
	public int getNumCardsInCurrentPlayerHand() {
		int throwerIndex = cardsPlayedThisRound % Constants.NUM_PLAYERS;
		
		int ret = (Constants.NUM_CARDS / Constants.NUM_PLAYERS) - (cardsPlayedThisRound - throwerIndex) / Constants.NUM_PLAYERS;
		
		//Sanity checks:
		if(ret <= 0) {
			System.err.println("ERROR: less than 1 card in hand according to getNumCardsInCurrentPlayerHand");
			System.exit(1);
		} else if(ret > Constants.NUM_RANKS) {
			System.err.println("ERROR: more than " + Constants.NUM_RANKS + " cards in hand according to getNumCardsInCurrentPlayerHand");
			System.exit(1);
		}
		
		return ret;
		
	}
	
	//Deterministic and bad:
	public String getMasterCard() {
		//TODO: order the search for master randomly between the off-suits:
		 for(int i=Constants.NUM_SUITS - 1; i>=0; i--) {
				for(int j=Constants.NUM_RANKS - 1; j>=0; j--) {
					if(cardsUsed[i][j]) {
						continue;
					} else if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][i][j] == CERTAINTY) {
						return getCardString(Constants.NUM_RANKS * i + j);
					} else {
						break;
					}
				}
			}
		 return null;
	}
	
	
	public boolean leaderPlayedMaster() {
		return isMasterCard(getCardLeaderThrow());
	}
	
	public boolean isMasterCard(String card) {
		
		int suitIndex = CardStringFunctions.getIndexOfSuit(card);
		int rank = getRankIndex(card);
		for(int i=rank+1; i <= ACE; i++) {
			for(int j=0; j<Constants.NUM_PLAYERS; j++) {
				if(cardsCurrentlyHeldByPlayer[j][suitIndex][i] != IMPOSSIBLE) {
					return false;
				}
			}
		}
		
		return true;
		
	}
	
	//(Ignore the cards in playerIndex's hand)
	public boolean isEffectivelyMasterCardForPlayer(int playerIndex, String card) {
		
		int suitIndex = CardStringFunctions.getIndexOfSuit(card);
		int rank = getRankIndex(card);
		for(int i=rank+1; i <= ACE; i++) {
			for(int j=0; j<Constants.NUM_PLAYERS; j++) {
				if(j != playerIndex && cardsCurrentlyHeldByPlayer[j][suitIndex][i] != IMPOSSIBLE) {
					return false;
				}
			}
		}
		
		return true;
		
	}
	
	private int getNumSuitsWithMastersInHand() {
		 int numberOfMasters = 0;
		 for(int i=0; i<cardsUsed.length; i++) {
			for(int j=cardsUsed[0].length - 1; j>=0; j--) {
				if(cardsUsed[i][j]) {
					continue;
				} else if(cardsCurrentlyHeldByPlayer[0][i][j] == CERTAINTY) {
					numberOfMasters++;
					break;
				} else {
					break;
				}
			}
		}
		 return numberOfMasters;
	}
	
	public boolean currentPlayerHasMasterInSuit(int suitIndex) {
		return getMasterInSuit(suitIndex) != null;
	}
	
	public String getMasterInSuit(int suitIndex) {
		for(int i=Constants.NUM_RANKS - 1; i>=0; i--) {
			if(cardsUsed[suitIndex][i] == true) {
				continue;
			} else if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][suitIndex][i] == CERTAINTY) {
				return getCardString(i, suitIndex);
			} else {
				return null;
			}
		}
		return null;
	}
	//END of MASTER FUNCTIONS

	public String getHighestOffSuitCardAnySuit() {
		String cardToPlay = "";
		
		FOUNDCARD:
		for(int i=Constants.NUM_RANKS - 1; i>=0; i--) {
			//TODO: have no preference between the off suits... or have a smart preference.
			for(int j=Constants.NUM_SUITS - 1; j>=Constants.SPADE; j--) {
				if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][j][i] == CERTAINTY) {
					cardToPlay = getCardString(13 * j + i);
					break FOUNDCARD;
				}
			}
		}
		return cardToPlay;
	}
	
	
	//LOWEST CARD
	public String getLowOffSuitCardToLead() {
		String cardToPlay = "";
		
		FOUNDCARD:
		for(int i=0; i<Constants.NUM_RANKS; i++) {
			//TODO: have no preference between the off suits... or have a smart preference.
			for(int j=Constants.NUM_SUITS - 1; j>=Constants.SPADE; j--) {
				if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][j][i] == CERTAINTY) {
					cardToPlay = getCardString(13 * j + i);
					break FOUNDCARD;
				}
			}
		}
		return cardToPlay;
	}
	

	public String getLowOffSuitCardToPlayElseLowestSpade() {
		String  cardToPlay = null;
		
		FOUNDCARD:
		for(int i=0; i<Constants.NUM_RANKS; i++) {
			//TODO: have no preference between the off suits... or have a smart preference.
			for(int j=Constants.NUM_SUITS - 1; j>=0; j--) {
				if(j == Constants.SPADE) {
					continue;
				}
				if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][j][i] == CERTAINTY) {
					cardToPlay = getCardString(13 * j + i);
					break FOUNDCARD;
				}
			}
		}
		
		if(cardToPlay == null) {
			for(int i=0; i<Constants.NUM_RANKS; i++) {
				
					if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][Constants.SPADE][i] == CERTAINTY) {
						cardToPlay = getCardString(13 * Constants.SPADE + i);
						break;
					}
				
			}
		}
		
		if(cardToPlay == null) {
			System.err.println("ERROR: didn't expect null in getLowOffSuitCardToPlayElseLowestSpade");
			System.exit(1);
		}
		return cardToPlay;
	}

	//END LOWEST CARD
	
	public boolean couldPlayCardInHandOverCardInSameSuit(String card) {
		return getCardInHandClosestOverSameSuit(card) != null;
	}
	
	public String getCardInHandClosestOverSameSuit(String card) {
		
		int suitIndex = CardStringFunctions.getIndexOfSuit(card);
		int rankIndex = getRankIndex(card);
		
		for(int j=rankIndex+1; j< Constants.NUM_RANKS; j++) {
			if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][suitIndex][j] == CERTAINTY) {
				return getCardString(j, suitIndex);
			}
		}
		return null;
	}
	

	public boolean couldPlayCardInHandUnderCardInSameSuit(String card) {
		return getCardInHandClosestUnderSameSuit(card) != null;
	}
	
	public String getCardInHandClosestUnderSameSuit(String card) {
		
		int suitIndex = CardStringFunctions.getIndexOfSuit(card);
		int rankIndex = getRankIndex(card);
		
		for(int j=rankIndex-1; j >= 0; j--) {
			if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][suitIndex][j] == CERTAINTY) {
				return getCardString(j, suitIndex);
			}
		}
		return null;
	}
	
	
	//Basic numbers:

	public int getNumberOfAces() {
		int ret = 0;
		for(int i=0; i<Constants.NUM_SUITS; i++) {
			if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][i][ACE]  == CERTAINTY) {
				ret++;
			}
		}
		return ret;
	}
	
	public int getNumberOfKings() {
		int ret = 0;
		for(int i=0; i<Constants.NUM_SUITS; i++) {
			if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][i][KING]  == CERTAINTY) {
				ret++;
			}
		}
		return ret;
	}

	public int getNumberOfCardsOneSuit(int suit) {
		int ret = 0;
		for(int i=0; i<13; i++) {
			if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][suit][i]  == CERTAINTY) {
				ret++;
			}
		}
		return ret;
	}
	
	//Opponent card logic:

	//pre: current player has a card in suit Index.
	public String getCardCurrentPlayerGetHighestInSuit(int suitIndex) {
		for(int i=Constants.NUM_RANKS - 1; i>=0; i--) {
			if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][suitIndex][i] == CERTAINTY) {
				return getCardString(13*suitIndex + i);
			}
		}
		System.err.println("AHH! Searching for highest in card in suit when player has no card in that suit. (" + suitIndex +")");
		System.exit(1);
		return "";
	}
	
	//pre: player has 2 cards of suit
	public String getCardCurrentPlayerGetSecondHighestInSuit(int suitIndex) {
		
		String tmpHighestCardInSuit = getCardCurrentPlayerGetHighestInSuit(suitIndex);
		int highestRankCurPlayerHasInSuit = getRankIndex(tmpHighestCardInSuit);
		
		for(int i=highestRankCurPlayerHasInSuit - 1; i>=0; i--) {
			if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][suitIndex][i] == CERTAINTY) {
				return getCardString(13*suitIndex + i);
			}
		}

		System.err.println("AHH! Searching for second highest in card in suit when player doesn't have 2 cards in that suit. (" + suitIndex +")");
		System.exit(1);
		return "";
	}
	
	//pre: current player has a card in suit Index.
	public String getCardCurrentPlayerGetLowestInSuit(int suitIndex) {
		for(int i=0; i<Constants.NUM_RANKS; i++) {
			if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][suitIndex][i] == CERTAINTY) {
				return getCardString(13*suitIndex + i);
			}
		}
		System.err.println("AHH! Searching for lowest in card in suit when player has no card in that suit. (" + suitIndex +")");
		System.exit(1);
		return "";
	}
	
	//pre: player has 2 cards of suit
	public String getCardCurrentPlayergetSecondLowestInSuit(int suitIndex) {
		
		int lowestRankCurPlayerHasInSuit = getRankIndex(getCardCurrentPlayerGetLowestInSuit(suitIndex));
		
		for(int i=lowestRankCurPlayerHasInSuit + 1; i<Constants.NUM_RANKS; i++) {
			if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][suitIndex][i] == CERTAINTY) {
				return getCardString(13*suitIndex + i);
			}
		}
		System.err.println("AHH! Searching for second lowest in card in suit when player doesn't have 2 cards in that suit. (" + suitIndex +")");
		System.exit(1);
		return "";
	}

	//pre: player has 3 cards of suit
	public String getCardCurrentPlayergetThirdLowestInSuit(int suitIndex) {
		
		int secondlowestRankCurPlayerHasInSuit = getRankIndex(getCardCurrentPlayergetSecondLowestInSuit(suitIndex));
		
		for(int i=secondlowestRankCurPlayerHasInSuit + 1; i<Constants.NUM_RANKS; i++) {
			if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][suitIndex][i] == CERTAINTY) {
				return getCardString(13*suitIndex + i);
			}
		}
		System.err.println("AHH! Searching for third lowest in card in suit when player doesn't have 3 cards in that suit. (" + suitIndex +")");
		System.exit(1);
		return "";
	}
	
	public boolean currentAgentHasSuit(int suitIndex) {
		return isVoid(Constants.CURRENT_AGENT_INDEX, suitIndex) == false;
	}
	
	//Card logic:

	public boolean hasCard(String card) {
		int num = getMellowCardIndex(card);
		
		return cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][num/Constants.NUM_RANKS][num%Constants.NUM_RANKS]  == CERTAINTY;
			
	}

	public static int getMellowCardIndex(String cardString) {
		int x = -1;
		int y = -1;
		if(cardString.charAt(0) >= '2' && cardString.charAt(0) <= '9') {
			x = (int)cardString.charAt(0) - (int)('2');
		} else if(cardString.charAt(0) == 'T') {
			x = 8;
		} else if(cardString.charAt(0) == 'J') {
			x = 9;
		} else if(cardString.charAt(0) == 'Q') {
			x = 10;
		} else if(cardString.charAt(0) == 'K') {
			x = 11;
		} else if(cardString.charAt(0) == 'A') {
			x = 12;
		} else {
			System.err.println("Number unknown! Uh oh!");
			System.exit(1);
		}
		
		if(cardString.charAt(1)=='S') {
			y = 0;
		} else if(cardString.charAt(1)=='H') {
			y = 1;
		} else if(cardString.charAt(1)=='C') {
			y = 2;
		} else if(cardString.charAt(1)=='D') {
			y = 3;
		} else {
			System.err.println("Suit unknown! Uh oh!");
			System.exit(1);
		}
		
		return y*13 + x;
	}
	
	public static String getCardString(int rankIndex, int suitIndex) {
		return getCardString(Constants.NUM_RANKS * suitIndex + rankIndex);
	}
	public static String getCardString(int cardIndex) {
		String ret = "";
		int number = cardIndex % 13;
		if(number < 8) {
			ret += (char)(number + '2') + "";
		} else if(number == 8) {
			ret += "T";
		} else if(number == 9) {
			ret += "J";
		} else if(number == 10) {
			ret += "Q";
		} else if(number == 11) {
			ret += "K";
		} else if(number == 12) {
			ret += "A";
		} else {
			System.err.println("Error: could not get card string from mellow card Number." + cardIndex);
			System.exit(1);
		}
		
		int suitIndex = cardIndex/13;
		if(suitIndex == 0) {
			ret += "S";
		} else if(suitIndex ==1) {
			ret += "H";
		} else if(suitIndex ==2) {
			ret += "C";
		} else if(suitIndex ==3) {
			ret += "D";
		} else {
			System.err.println("Error: Unknown suit for card number. " + cardIndex);
			System.exit(1);
		}
		
		return ret;
	}
	

	public boolean cardAGreaterThanCardBGivenLeadCard(String cardA, String cardB) {
		return getCardPower(cardA, getSuitOfLeaderThrow()) > getCardPower(cardB, getSuitOfLeaderThrow());
	}
	
	public int getCardPower(String card, int leadSuit) {

		//Play trump/spade
		if(CardStringFunctions.getIndexOfSuit(card) == Constants.SPADE) {
			return Constants.NUM_RANKS + getRankIndex(card);
		
		//Follow suit
		} else if(CardStringFunctions.getIndexOfSuit(card) == leadSuit) {
			return getRankIndex(card);

		//Play off-suit
		} else {
			return -1;
		}
	}
	
	public int getRankIndex(String card) {
		int x = 0;
		
		if(card.charAt(0) >= '2' && card.charAt(0) <= '9') {
			x = (int)card.charAt(0) - (int)('2');
		} else if(card.charAt(0) == 'T') {
			x = 8;
		} else if(card.charAt(0) == 'J') {
			x = 9;
		} else if(card.charAt(0) == 'Q') {
			x = 10;
		} else if(card.charAt(0) == 'K') {
			x = 11;
		} else if(card.charAt(0) == 'A') {
			x = 12;
		} else {
			System.err.println("Number unknown! Uh oh!");
			System.exit(1);
		}
		return x;
	}
	
	//Forth throw:
	

	//Names

	public int convertPlayerNameToIndex(String playerName) {
		for(int i=0; i<Constants.NUM_PLAYERS; i++) {
			if(this.players[i].equals(playerName)) {
				return i;
			}
		}
		
		return -1;
	}
	
	public void setNameOfPlayers(String players[]) {
		for(int i=0; i<Constants.NUM_PLAYERS; i++) {
			this.players[i] = players[i] + "";
		}
	}
	
	public int getNumCardsCurrentUserStartedWithInSuit(int suitIndex) {
		int ret = 0;
		for(int i=0; i<Constants.NUM_RANKS; i++) {
			if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][suitIndex][i] == CERTAINTY) {
				ret++;

			} else if(cardsUsedByPlayer[0][suitIndex][i]) {
				ret++;
			}
		}
		return ret;
	}
	

	public int getNumCardsOfSuitInCurrentPlayerHand(int suitIndex) { 
		int ret = 0;
		for(int i=RANK_TWO; i<=ACE; i++) {
			if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][suitIndex][i] == CERTAINTY) {
				ret++;
			}
		}
		
		return ret;
	}

	public int getNumCardsPlayedForSuit(int suitIndex) {
		int ret = 0;
		for(int i=0; i<Constants.NUM_RANKS; i++) {
			if(cardsUsed[suitIndex][i]) {
				ret++;
			}
		}
		return ret;
	}
	
	public int getNumCardsHiddenInOtherPlayersHandsForSuit(int suitIndex) {
		int ret = 0;
		for(int i=0; i<Constants.NUM_RANKS; i++) {
			if(cardsUsed[suitIndex][i] == false 
					&& cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][suitIndex][i] == IMPOSSIBLE) {
				ret++;
			}
		}
		return ret;
	}
	
	public boolean currentPlayerOnlyHasSpade() {
		return currentPlayerMustTrump();
	}
	
	public boolean currentPlayerMustTrump() {
		for(int i =0; i<Constants.NUM_SUITS; i++) {
			if(i != Constants.SPADE) {
				if(isVoid(Constants.CURRENT_AGENT_INDEX, i) == false) {
					return false;
				}
			}
		}
		return true;
	}
	
	public String[] getListOfPossibleActions() {
		if(stillInBiddingPhase()) {
			//Return all bids:
			String ret[] = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13"};
			return ret;
		} else {

			String cardsHeld[] = getCurrentPlayerHandRemaining();
			if(currentThrowIsLeading()) {
				return cardsHeld;
			} else {
				int leadSuitIndex = getSuitOfLeaderThrow();
				
				if(currentAgentHasSuit(leadSuitIndex)) {
					//Follow suit:
					
					int curNumChoices = 0;
					for(int rank=0; rank<Constants.NUM_RANKS; rank++) {
						if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][leadSuitIndex][rank] == CERTAINTY) {
							curNumChoices++;
						}
					}
					
					if(curNumChoices == 0) {
						System.err.println("ERROR (In getListOfPossibleActions): apparently current user can\'t follow suit and must follow suit.");
						System.exit(1);
					}
					
					if(curNumChoices == 1) {
						System.err.println("ERROR: (In getListOfPossibleActions): user can only play 1 card... this shouldn\'t be a hard decision...");
						System.exit(1);
					}
					
					String ret[] = new String[curNumChoices];
					
					curNumChoices = 0;
					for(int rank=0; rank<Constants.NUM_RANKS; rank++) {
						if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][leadSuitIndex][rank] == CERTAINTY) {
							ret[curNumChoices] = getCardString(rank, leadSuitIndex);
							curNumChoices++;
						}
					}
					
					return ret;
				} else {

					//Play anything because you don't have to follow suit:
					return cardsHeld;
					
				}
				
			}
		}
	}
	
	public String[] getCurrentPlayerHandRemaining() {

		int curNumCardsInHand = 0;
		for(int i=0; i<Constants.NUM_SUITS; i++) {
			for(int j=0; j<Constants.NUM_RANKS; j++) {
				if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][i][j] == CERTAINTY) {
					curNumCardsInHand++;
				}
			}
		}
		
		String ret[] = new String[curNumCardsInHand];
		
		curNumCardsInHand = 0;
		for(int suit=0; suit<Constants.NUM_SUITS; suit++) {
			for(int rank=0; rank<Constants.NUM_RANKS; rank++) {
				if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][suit][rank] == CERTAINTY) {
					ret[curNumCardsInHand] = getCardString(rank, suit);
					curNumCardsInHand++;
				}
			}
		}
		
		return ret;
	}

	private boolean stillInBiddingPhase() {
		return bidsMadeThisRound < Constants.NUM_PLAYERS;
	}
	
	
	//New functions....
	//TODO: make them work before adding new ones
	
	public boolean playerCouldSweepSpades(int playerIndex) {
		return playerCouldSweepSpadesAfterTrumpingSpade(playerIndex, "");
	}
	
	public boolean currentPlayerCouldSweepSpadesAfterTrumpingHighestSpade() {
		return playerCouldSweepSpadesAfterTrumpingSpade(
				Constants.CURRENT_AGENT_INDEX,
				this.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE)
				);
	}
	
	public boolean currentPlayerCouldSweepSpadesAfterTrumpingLowestSpade() {
		return playerCouldSweepSpadesAfterTrumpingSpade(
				Constants.CURRENT_AGENT_INDEX,
				this.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE)
				);
	}
	
	public boolean playerCouldSweepSpadesAfterTrumpingSpade(int playerIndex, String cardUsed) {
		int playerOwnedCards = 0;
		int otherPlayerOwnedCards = 0;
		
		for(int rank=ACE; rank >= RANK_TWO; rank--) {
			
			if(cardsUsed[Constants.SPADE][rank] == false) {
				if(cardsCurrentlyHeldByPlayer[playerIndex][Constants.SPADE][rank] == CERTAINTY) {
					
					if(getCardString(rank, Constants.SPADE).equals(cardUsed)) {
						//Ignore card because it's used
					} else {
						playerOwnedCards++;
					}
					
				} else {
					otherPlayerOwnedCards++;
				}
				
				if(otherPlayerOwnedCards > playerOwnedCards) {
					return false;
				}
			}
		}
		
		//Whatever!
		//if(playerOwnedCards + otherPlayerOwnedCards == 0) {
		//	System.err.println("Warning: this is getting philosophical in playerCouldSweepSpades...");
		//	System.exit(1);
		//}
		
		return true;
	}
	
	//This is true if player has ACE alone...
	public boolean playerWillWinWithAllCardsInHandForSuitIfNotTrumped(int playerIndex, int suitIndex) {
		
		int playerOwnedCards = 0;
		int otherPlayerOwnedCards = 0;
		
		for(int rank=ACE; rank >= RANK_TWO; rank--) {
			
			if(cardsUsed[suitIndex][rank] == false) {
				if(cardsCurrentlyHeldByPlayer[playerIndex][suitIndex][rank] == CERTAINTY) {
					playerOwnedCards++;

					if(otherPlayerOwnedCards > playerOwnedCards) {
						return false;
					}
				} else {
					otherPlayerOwnedCards++;
				}
				
			}
		}
		
		//Whatever!
		//if(playerOwnedCards == 0) {
		//	System.err.println("ERROR: this is getting philosophical in playerCouldWinWithAllCardsInHandForSuit...");
		//	System.exit(1);
		//}
		
		return true;
	}
	
	//public boolean playerCouldSweepSuit
	
	//TODO: later
	//public int getNumTricksPlayerCouldForceWithoutBeingTrumped(int playerIndex, int suitIndex) {
		
	//	return -1;
	//}
	
	//Too easy
	//public boolean OpponentsVoid(int playerIndex, int suitIndex) {
	//	return isVoid((playerIndex + 1)%Constants.NUM_PLAYERS, suitIndex)
	//			&& isVoid((playerIndex + 3)%Constants.NUM_PLAYERS, suitIndex);
	//}
	
	
}

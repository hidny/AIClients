package mellow.ai.cardDataModels.playerSaidMellowSignals;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.cardUtils.CardStringFunctions;
import mellow.cardUtils.DebugFunctions;

public class PlayerSaidMellowSignals {

	static final int MELLOW_PLAYER_SIGNALED_NO = 3;
	private DataModel dataModel;

	public PlayerSaidMellowSignals(DataModel dataModel) {
		this.dataModel = dataModel;
	}
	
	
	//TODO: make current file be able to answer questions about signals.
	//TODO: make dataModel need to go to MellowSignalHandler if it wants to know about mellow signals.
	
	public void handleSignalsFromActiveMellow(int playerIndex, String card) {
		
		//Not recording the signals the current player is sending out (yet)
		if(playerIndex == Constants.CURRENT_AGENT_INDEX) {
			return;
		}
		
		int throwNumber = dataModel.getCardsPlayedThisRound() % Constants.NUM_PLAYERS;

		
		if(throwNumber == 0) {
			//If throwNumber 0, mellow player lead first card
			// and now it's the second player's turn
			
			//and the first card lead by a mellow player is usually weird... ignore leading
			//TODO: don't ignore in future!
			
		} else {
			
			int suitLeadIndex = dataModel.getSuitOfLeaderThrow();
			
			if(CardStringFunctions.getIndexOfSuit(card) == suitLeadIndex) {
				//Mellow following lead
				
				//TODO: Assumes mellow play doesn't know if it could be protected....
				// Did this to keep it simple.

				if(dataModel.cardAGreaterThanCardBGivenLeadCard
						(card, dataModel.getCurrentFightWinningCardBeforeAIPlays())
						) {
					
					//Mellow follows suit over, probably has nothing under.
					//Mellow player probably doesn't have cards under card mellow player threw.
					
					// TODO: There's an exception if it's 2nd or 3rd thrower and knows last player must play
					// above, but whatever...
					
					for(int rankIndex=dataModel.getRankIndex(card) - 1 ; rankIndex >= dataModel.RANK_TWO; rankIndex--) {
						//TODO: if there's another state, we will need to make a complicate state transition table
						//MELLOW IND -> LEAD_SUGGESTION ...
						setCardMellowSignalNoIfUncertain(playerIndex, suitLeadIndex, rankIndex);
					}
					
				} else {
					
					//Mellow follows suit under
					//Mellow player probably doesn't have cards between curFightWinner and card mellow player threw.
					
					String curWinningCard = dataModel.getCurrentFightWinningCardBeforeAIPlays();
					
					int suitCurrentWinning = CardStringFunctions.getIndexOfSuit(curWinningCard);
					
					int maxRankIndex = dataModel.getRankIndex(dataModel.getCurrentFightWinningCardBeforeAIPlays());
					
					if(suitCurrentWinning == Constants.SPADE && suitLeadIndex != Constants.SPADE) {
						maxRankIndex = dataModel.ACE;
					}
					
					for(int rankIndex=dataModel.getRankIndex(card) + 1 ; rankIndex <= maxRankIndex; rankIndex++) {

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
					
					if(dataModel.cardAGreaterThanCardBGivenLeadCard
							(card, dataModel.getCurrentFightWinningCardBeforeAIPlays())) {
						//TODO: if it's unrealistic (EX: it would mean mel has 6 spades, then reconsider signal...
						//TODO: maybe mellow is 2nd thrower and expects partner to cover the spade?
						
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
						int rankCurrentFightWinner = dataModel.getRankIndex(dataModel.getCurrentFightWinningCardBeforeAIPlays());
						
						for(int rankIndex = dataModel.getRankIndex(card) + 1; rankIndex < rankCurrentFightWinner; rankIndex++) {
							
							//TODO: if there's another state, we will need to make a complicate state transition table
							//MELLOW IND -> LEAD_SUGGESTION ...
							setCardMellowSignalNoIfUncertain(playerIndex, Constants.SPADE, rankIndex);
						}
					}
					
				} else {
					
					//If Mellow player plays plays off,
					//Mellow player prob doesn't have anything over that offsuit
					for(int rankIndex=dataModel.getRankIndex(card) + 1 ; rankIndex <= dataModel.ACE; rankIndex++) {
						setCardMellowSignalNoIfUncertain(playerIndex, mellowSuitPlayed, rankIndex);
					}
				}
				
			}
			
			
		}
	
	}
	
	

	//TODO: if mellow has card even though the player signal he/she doesn't: note that down!
	public void setCardMellowSignalNoIfUncertain(int playerIndex, int suitIndex, int rankIndex) {
		if(dataModel.getCardsCurrentlyHeldByPlayers()[playerIndex][suitIndex][rankIndex] != dataModel.CERTAINTY
				&& dataModel.getCardsCurrentlyHeldByPlayers()[playerIndex][suitIndex][rankIndex] != dataModel.IMPOSSIBLE) {
			
			//System.out.println("MELLOW HAS NO " + dataModel.getCardString(rankIndex, suitIndex) + ".");
			//System.out.println(playerIndex + ", " + suitIndex + ", " + rankIndex);
			dataModel.getCardsCurrentlyHeldByPlayers()[playerIndex][suitIndex][rankIndex] = MELLOW_PLAYER_SIGNALED_NO;
		}
	}
	
	
	
	public String getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(int mellowPlayerIndex, int suitIndex) {
		for(int rank=dataModel.ACE; rank>=DataModel.RANK_TWO; rank--) {
			if(dataModel.getCardsCurrentlyHeldByPlayers()[mellowPlayerIndex][suitIndex][rank] != DataModel.IMPOSSIBLE
					&& dataModel.getCardsCurrentlyHeldByPlayers()[mellowPlayerIndex][suitIndex][rank] != MELLOW_PLAYER_SIGNALED_NO) {
				return DataModel.getCardString(rank, suitIndex);
			}
			
		}
		
		return null;
	}
	
	public String getSecondHighestRankCardMellowPlayerCouldHaveBasedOnSignals(int mellowPlayerIndex, int suitIndex) {
		int numFound = 0;
		for(int rank=DataModel.ACE; rank>=DataModel.RANK_TWO; rank--) {
			if(dataModel.getCardsCurrentlyHeldByPlayers()[mellowPlayerIndex][suitIndex][rank] != DataModel.IMPOSSIBLE
					&& dataModel.getCardsCurrentlyHeldByPlayers()[mellowPlayerIndex][suitIndex][rank] != MELLOW_PLAYER_SIGNALED_NO) {
				numFound++;
				if(numFound == 2) {
					return DataModel.getCardString(rank, suitIndex);
				}
			}
			
		}
		
		return null;
	}
	

	public int getNumCardsMellowSignalledPossibleInSuit(int mellowPlayerIndex, int suitIndex) {
		
		if(dataModel.isVoid(mellowPlayerIndex, suitIndex)
				||mellowPlayerSignalNoCardsOfSuit(mellowPlayerIndex, suitIndex)) {
			return 0;
		}
		
		int ret = 0;
		for(int rank=DataModel.ACE; rank>=DataModel.RANK_TWO; rank--) {
			if(dataModel.getCardsCurrentlyHeldByPlayers()[mellowPlayerIndex][suitIndex][rank] != DataModel.IMPOSSIBLE
					&& dataModel.getCardsCurrentlyHeldByPlayers()[mellowPlayerIndex][suitIndex][rank] != MELLOW_PLAYER_SIGNALED_NO) {
				ret++;
				
			}
		}
	
		return ret;
		
	}
	
	
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

	public boolean mellowSignalledNoCardUnderCardSameSuitExceptRank2(String inputCard, int mellowPlayerIndex) {
		
		
		boolean cardsUnderInputCard[][] = dataModel.getCardsStrictlyLessPowerfulThanCard(inputCard, false);
		
		int suitIndex = CardStringFunctions.getIndexOfSuit(inputCard);
		
		for(int j=dataModel.RANK_THREE; j<Constants.NUM_RANKS; j++) {
			
			if(cardsUnderInputCard[suitIndex][j]) {
				if(dataModel.getCardsCurrentlyHeldByPlayers()[mellowPlayerIndex][suitIndex][j] != dataModel.IMPOSSIBLE
						&& dataModel.getCardsCurrentlyHeldByPlayers()[mellowPlayerIndex][suitIndex][j] != MELLOW_PLAYER_SIGNALED_NO) {
					
					//At this point, the mellow player signalled that they could have a card under inputCard
					//And you should feel justified in throwing it off to help burn mellow...
					return false;
				}
			}
		}
		
		return true;
		
	}

	public boolean mellowSignalledNoCardBetweenTwoCards(String smallerCard, String biggerCard, int mellowPlayerIndex) {
		
		if(dataModel.cardAGreaterThanCardBGivenLeadCard(biggerCard, smallerCard) == false) {
			System.err.println("ERROR in mellowSignalledNoCardBetweenTwoCards: biggerCard should be bigger then smallerCard");
			System.exit(1);
			
		}
		boolean cardsOverSmallerCard[][] = dataModel.getCardsStrictlyMorePowerfulThanCard(smallerCard, true);
		boolean cardsUnderBiggerCard[][] = dataModel.getCardsStrictlyLessPowerfulThanCard(biggerCard, true);
		
		for(int i=0; i<Constants.NUM_SUITS; i++) {
			for(int j=0; j<Constants.NUM_RANKS; j++) {
				
				if(cardsOverSmallerCard[i][j] && cardsUnderBiggerCard[i][j]) {

					if(dataModel.getCardsCurrentlyHeldByPlayers()[mellowPlayerIndex][i][j] != dataModel.IMPOSSIBLE
							&& dataModel.getCardsCurrentlyHeldByPlayers()[mellowPlayerIndex][i][j] != MELLOW_PLAYER_SIGNALED_NO) {
						
						//At this point, the mellow player signaled that they could have a card in between
						//And you should feel nervous about playing over the currently winning card...
						return false;
					}
				}
			}
		}
		
		return true;
		
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
		
		String currentlyWinningCard = dataModel.getCurrentFightWinningCardBeforeAIPlays();
		
		if(CardStringFunctions.getIndexOfSuit(currentlyWinningCard) != suitIndex) {
			
			if(suitIndex == Constants.SPADE) {
				return true;
			} else {
				return false;
			}
		}

		int rankPossibleMellowCard = dataModel.getRankIndex(maxCardMellowSignalledCouldHave);
		int rankCurrentlyWinningCard = dataModel.getRankIndex(currentlyWinningCard);
		
		if(rankPossibleMellowCard > rankCurrentlyWinningCard) {
			return true;
		} else {
			return false;
		}
	}

	public int getNumCardsMellowSignalledOverCardSameSuit(String card, int mellowPlayerIndex) {
		int suitIndex = CardStringFunctions.getIndexOfSuit(card);
		
		//This is a hack solution... that's depends on the rules of mellow:
		if(suitIndex == Constants.SPADE) {
			return getNumCardsMellowSignalledBetweenTwoCards(card, "AS", mellowPlayerIndex);
		} else {
			return getNumCardsMellowSignalledBetweenTwoCards(card, "2S", mellowPlayerIndex);
		}
	}

	public int getNumCardsMellowSignalledBetweenTwoCards(String smallerCard, String biggerCard, int mellowPlayerIndex) {
		
		if(dataModel.cardAGreaterThanCardBGivenLeadCard(biggerCard, smallerCard) == false) {
			System.err.println("ERROR in numCardsMellowSignalledBetweenTwoCards: biggerCard should be bigger then smallerCard");
			System.exit(1);
			
		}
		boolean cardsOverSmallerCard[][] = dataModel.getCardsStrictlyMorePowerfulThanCard(smallerCard, true);
		boolean cardsUnderBiggerCard[][] = dataModel.getCardsStrictlyLessPowerfulThanCard(biggerCard, true);
		
		int ret = 0;
		
		for(int i=0; i<Constants.NUM_SUITS; i++) {
			for(int j=0; j<Constants.NUM_RANKS; j++) {
				
				if(cardsOverSmallerCard[i][j] && cardsUnderBiggerCard[i][j]) {

					if(dataModel.getCardsCurrentlyHeldByPlayers()[mellowPlayerIndex][i][j] != dataModel.IMPOSSIBLE
							&& dataModel.getCardsCurrentlyHeldByPlayers()[mellowPlayerIndex][i][j] != MELLOW_PLAYER_SIGNALED_NO) {
						
						//At this point, the mellow player signaled that they could have a card in between
						//And you should start to feel nervous about playing over the currently winning card...
						ret++;
					}
				}
			}
		}
		
		return ret;
		
	}
	
	//No two cards signals: (For when you're playing before the mellow player)
	
	//TODO: TEST
/*
public boolean mellowSignalledNo2CardsOverCardSameSuit(String inputCard, int mellowPlayerIndex) {
	
		boolean cardsOverInputCard[][] = dataModel.getCardsStrictlyMorePowerfulThanCard(inputCard, true);
		
		int suitIndex = CardStringFunctions.getIndexOfSuit(inputCard);
		
		boolean firstFoundAlready = false;
		
		for(int j=0; j<Constants.NUM_RANKS; j++) {
			
			if(cardsOverInputCard[suitIndex][j]) {

				if(dataModel.getCardsCurrentlyHeldByPlayers()[mellowPlayerIndex][suitIndex][j] != dataModel.IMPOSSIBLE
						&& dataModel.getCardsCurrentlyHeldByPlayers()[mellowPlayerIndex][suitIndex][j] != MELLOW_PLAYER_SIGNALED_NO) {
					
					if(firstFoundAlready) {
						return false;
					}
					firstFoundAlready = true;
				}
			}
		}
		
		return true;
		
	}
*/
	
}

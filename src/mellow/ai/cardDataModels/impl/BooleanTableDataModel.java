package mellow.ai.cardDataModels.impl;

import java.util.ArrayList;

import mellow.Constants;
import mellow.cardUtils.*;

public class BooleanTableDataModel {

	int AIScore;
	int OpponentScore;

	int IMPOSSIBLE =0;
	int CERTAINTY = 1000;
	int DONTKNOW = -1;
	
	int BID_NOT_SET = -1;
	
	//This is dumb: Think about changing it later.
	//Higher number means higher power
	private int ACE = 12;
	private int KING = 11;
	private int QUEEN = 10;
	private int JACK = 9;
	private int TEN = 8;
	private int NINE = 7;
	private int EIGHT = 6;
	private int SEVEN = 5;
	private int SIX = 4;
	private int FIVE = 3;
	private int FOUR = 2;
	private int THREE = 1;
	private int TWO = 0;
	
	private boolean cardsUsed[][] = new boolean[Constants.NUM_SUITS][Constants.NUM_RANKS];
	
	private int cardsCurrentlyHeldByPlayer[][][] = new int[Constants.NUM_PLAYERS][Constants.NUM_SUITS][Constants.NUM_RANKS];
	
	private boolean CardsUsedByPlayer[][][] = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS][Constants.NUM_RANKS];

	//Map:
	//  2  3  4 5 .. A
	//S
	//H
	//D
	//C
	
	
	private String players[] = new String[Constants.NUM_PLAYERS];
	private int bids[];
	private int tricks[];
	
	private int cardsPlayedThisRound = 0;
	
	private String cardStringsPlayed[] = new String[Constants.NUM_CARDS];


	public int getCardsPlayedThisRound() {
		return cardsPlayedThisRound;
	}
	
	public void setBid(String playerName, int bid) {
		bids[convertPlayerNameToIndex(playerName)] = bid;
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
	
	public boolean burntMellow(int playerIndex) {
		return bids[playerIndex] == 0 && tricks[playerIndex] > 0;
	}
	
	public boolean isVoid(int playerIndex, int suitIndex) {
		
		for(int i=TWO; i<=ACE; i++) {
			if(cardsCurrentlyHeldByPlayer[playerIndex][suitIndex][i] != IMPOSSIBLE) {
				return false;
			}
		}
		return true;
	}

	
	public void setupCardsInHandForNewRound(String cards[]) {
		
		resetStateForNewRound();
		resetCardKnowledgeTableForNewRound();
		
		//System.out.println("Printing cards");
		cardsPlayedThisRound = 0;
		
		 
		 for(int i=0; i<cards.length; i++) {
				//System.out.println(cards[i]);
				cardsCurrentlyHeldByPlayer[0][getMellowCardIndex(cards[i])/13][getMellowCardIndex(cards[i])%13]  = CERTAINTY;
				cardsCurrentlyHeldByPlayer[1][getMellowCardIndex(cards[i])/13][getMellowCardIndex(cards[i])%13]  = IMPOSSIBLE;
				cardsCurrentlyHeldByPlayer[2][getMellowCardIndex(cards[i])/13][getMellowCardIndex(cards[i])%13]  = IMPOSSIBLE;
				cardsCurrentlyHeldByPlayer[3][getMellowCardIndex(cards[i])/13][getMellowCardIndex(cards[i])%13]  = IMPOSSIBLE;
				
			}
		
		 for(int i=0; i<Constants.NUM_SUITS*Constants.NUM_RANKS; i++) {
			 cardStringsPlayed[i] = "";
		 }
		 
		 bids = new int[Constants.NUM_PLAYERS];
		 tricks = new int[Constants.NUM_PLAYERS];
		 for(int i=0; i<bids.length; i++) {
			bids[i] = BID_NOT_SET;
		 }
	}

	//TODO
	//Technically not needed to be called because setupCardsInHandForNewRound will call it.
	public void resetStateForNewRound() {
		cardsUsed = new boolean[Constants.NUM_SUITS][Constants.NUM_RANKS];
		cardsCurrentlyHeldByPlayer = new int[Constants.NUM_PLAYERS][Constants.NUM_SUITS][Constants.NUM_RANKS];
		CardsUsedByPlayer = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS][Constants.NUM_RANKS];
		cardsPlayedThisRound =0;
		cardStringsPlayed = new String[Constants.NUM_CARDS];
		
		bids = new int[Constants.NUM_PLAYERS];
		tricks = new int[Constants.NUM_PLAYERS];
		for(int i=0; i<bids.length; i++) {
			bids[i] = BID_NOT_SET;
		 }
	}
	
	
	private void resetCardKnowledgeTableForNewRound() {
		for(int i=0; i<cardsUsed.length; i++) {
			for(int j=0; j<cardsUsed[0].length; j++) {
				cardsUsed[i][j] = false;
				for(int k=0; k< Constants.NUM_PLAYERS; k++) {
					CardsUsedByPlayer[k][i][j] = false;
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
			System.err.println("ERROR: card played is supposedly impossible in updateDataModelWithPlayedCard");
			System.exit(1);
		}
		//End sanity check
		
		//Update tricks (if needed)
		handleTrickIfPlayedCardIs4thThrow(indexPlayer, card);
		
		cardsUsed[cardNum/Constants.NUM_RANKS][cardNum%Constants.NUM_RANKS] = true;
		CardsUsedByPlayer[indexPlayer][cardNum/Constants.NUM_RANKS][cardNum%Constants.NUM_RANKS] = true;
		for(int i=0; i<Constants.NUM_PLAYERS; i++) {
			cardsCurrentlyHeldByPlayer[i][cardNum/Constants.NUM_RANKS][cardNum%Constants.NUM_RANKS] = IMPOSSIBLE;
		}
		
		
		cardStringsPlayed[cardsPlayedThisRound] = card;
		cardsPlayedThisRound++;
		//System.out.println("Cards played this round: " + cardsPlayedThisRound);
		

		//Check if non-leading player didn't follow suit (and is void in suit)
		if(cardsPlayedThisRound % 4 != 0 ) {
			if(CardStringFunctions.getIndexOfSuit(card) != getSuitOfLeaderThrow()) {
				for(int i=TWO; i<=ACE; i++) {
					cardsCurrentlyHeldByPlayer[indexPlayer][getSuitOfLeaderThrow()][i] = IMPOSSIBLE;
				}
			}
		}
		//End check
		
		logicallyDeduceWhoHasCardsByProcessOfElimination();
		
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
						System.err.println("ERROR: there's a card that isn't accounted for!");
						System.exit(1);
					}
				}
			}
		}
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
			System.err.println("ERRO: calling get Current Fight Winning Card on 1st throw.");
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
	
	
	//TODO
	public boolean hasNonLeadCardInHandThatCanDecreaseChanceof4thThrowerWinning() {
		return getNonLeadCardInHandThatCanDecreaseChanceof4thThrowerWinning() != null;
	}
	
	//TODO:
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
		
		//TODO: reduce # of loops by 2 by creating only 1 loop to go thru the cards
		
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
					for(int m=0; m<Constants.NUM_SUITS; m++) {
						for(int n=0; n<Constants.NUM_RANKS; n++) {
							
							if(cardsUnderPotentialCard[m][n]
									&& cardsOverCurrentlyWinningCard[m][n]) {
								String tempCard2 = getCardString(n, m);

								if(nonLeadPlayerCouldMaybeThrowCard(fourthThrowerIndex, tempCard2) &&
										cardsCurrentlyHeldByPlayer[fourthThrowerIndex][m][n] != IMPOSSIBLE
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
		for(int i=upperLimitDealerSuit; i>=TWO; i--) {
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
			for(int i=upperLimitSpadeSuit; i>=TWO; i--) {
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
		
		int leadSuit = this.getSuitOfLeaderThrow();
		
		int lowerLimitDealerSuit;
		
		//card is off-suit non trump
		if(suitIndex != Constants.SPADE && leadSuit != suitIndex ) {
			lowerLimitDealerSuit = TWO;
		
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
			lowerLimitSpadeSuit = TWO;
		} else {
			lowerLimitSpadeSuit = rankIndex + 1;
		}

		for(int i=lowerLimitSpadeSuit; i<=ACE; i++) {
			ret[Constants.SPADE][i] = true;
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
			int startRankIndex = TWO;
			
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
	
	//TODO
	public String getJunkiestCardToFollowLead() {
		int throwNumber = cardsPlayedThisRound % Constants.NUM_PLAYERS;

		if(throwNumber == 0) {
			System.err.println("ERROR: calling get getJunkiestCardToFollowLead on 1st throw.");
			System.exit(1);
		}
		
		int leadSuitIndex = getSuitOfLeaderThrow();

		//if must follow suit
		if(throwerMustFollowSuit()) {
			
			for(int i=TWO; i <= ACE; i++) {
				if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][leadSuitIndex][i] == CERTAINTY) {
					return getCardString(i, leadSuitIndex);
				}
			}
			System.err.println("In getJunkiestCardToFollowLead thrower must follow suit but has nothing in suit to throw. (Logically impossible)");
			System.exit(1);
			
		} else {
			
			//TODO: make the logic more sophisticated...
			
			//Play smallest off-suit:
			for(int i=TWO; i <= ACE; i++) {
				for(int j=1; j < Constants.NUM_SUITS; j++) {
					if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][j][i] == CERTAINTY) {
						return getCardString(i, j);
					}
				}
			}
			
			//Play smallest spade:
			for(int i=TWO; i <= ACE; i++) {
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
			return getLastCardInHand();
		} else {
			
			if(throwerMustFollowSuit() == false) {
				System.err.println("ERROR: calling getOnlyCardCurrentPlayerCouldPlay and player has more than 1 card,\n doesn\'t need to follow suit and apparrently has to play single card.\nThis doesn\'t happen in Mellow.");
				System.exit(1);
			}
			
			return getJunkiestCardToFollowLead();
		}
	}
	
	public String getLastCardInHand() {
		if(getNumCardsInCurrentPlayerHand() != 1) {
			System.err.println("ERROR: calling getLastCardInHand when player has more than 1 card to play");
			System.exit(1);
		}
		
		for(int i=0; i<Constants.NUM_SUITS; i++) {
			for(int j=TWO; j<=ACE; j++) {
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
		
		for(int i=TWO; i<=ACE; i++) {
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
		 for(int i=cardsUsed.length - 1; i>0; i--) {
				for(int j=cardsUsed[0].length - 1; j>=0; j--) {
					if(cardsUsed[i][j]) {
						continue;
					} else if(cardsCurrentlyHeldByPlayer[0][i][j] == CERTAINTY) {
						//TODO: NUM_NUMBERS -> NUM_RANKS
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
	
	public boolean hasMasterInSuit(int suitIndex) {
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

	public String getHighestOffSuitCardToLead() {
		String cardToPlay = "";
		
		FOUNDCARD:
		for(int i=Constants.NUM_RANKS - 1; i>=0; i--) {
			//TODO: have no pref between the off suits... or have a smart preference.
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
			//TODO: have no pref between the off suits... or have a smart preference.
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
			//TODO: have no pref between the off suits... or have a smart preference.
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
	
	//TODO: suit strings please
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
		System.out.println("AHH! Searching for highest in " + suitIndex + " when player has no card in that suit.");
		System.exit(1);
		return "";
	}
	
	//pre: current player has a card in suit Index.
	public String getCardCurrentPlayergetLowestInSuit(int suitIndex) {
		for(int i=0; i<Constants.NUM_RANKS; i++) {
			if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][suitIndex][i] == CERTAINTY) {
				return getCardString(13*suitIndex + i);
			}
		}
		System.out.println("AHH! Searching for lowest in " + suitIndex + " when player has no card in that suit.");
		System.exit(1);
		return "";
	}
	
	
//TODO: suspicious function
	//TODO: Might delete or make private and only accept Strings from other classes 
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
			System.out.println("Number unknown! Uh oh!");
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
			System.out.println("Suit unknown! Uh oh!");
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
			System.out.println("Error: could not get card string from mellow card Number." + cardIndex);
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
			System.out.println("Error: Unknown suit for card number. " + cardIndex);
			System.exit(1);
		}
		
		return ret;
	}
	

	public boolean cardAGreaterThanCardBGivenLeadCard(String cardA, String cardB) {
		return getCardPower(cardA) > getCardPower(cardB);
	}

	public int getCardPower(String card) {

		//Play trump/spade
		if(CardStringFunctions.getIndexOfSuit(card) == Constants.SPADE) {
			return Constants.NUM_RANKS + getRankIndex(card);
		
		//Follow suit
		} else if(CardStringFunctions.getIndexOfSuit(card) == CardStringFunctions.getIndexOfSuit(getCardLeaderThrow())) {
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
			System.out.println("Number unknown! Uh oh!");
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
	
	public int getNumCardsCurrentUserStartedWithInSuit(int suit) {
		int ret = 0;
		for(int i=0; i<Constants.NUM_RANKS; i++) {
			if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][suit][i] == CERTAINTY) {
				ret++;
			}
		}
		return ret;
	}
	
	public int getNumCardsPlayedForSuit(int suit) {
		int ret = 0;
		for(int i=0; i<Constants.NUM_RANKS; i++) {
			if(cardsUsed[suit][i]) {
				ret++;
			}
		}
		return ret;
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
	
}

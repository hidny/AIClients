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
	
	
	private boolean isVoid[][] = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
	
	
	private String players[] = new String[Constants.NUM_PLAYERS];
	
	private int cardsPlayedThisRound = 0;
	
	public int getCardsPlayedThisRound() {
		return cardsPlayedThisRound;
	}

	private String cardStringsPlayed[] = new String[Constants.NUM_CARDS];

	
	
	
	public void resetStateForNewRound() {
		cardsUsed = new boolean[Constants.NUM_SUITS][Constants.NUM_RANKS];
		cardsCurrentlyHeldByPlayer = new int[Constants.NUM_PLAYERS][Constants.NUM_SUITS][Constants.NUM_RANKS];
		CardsUsedByPlayer = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS][Constants.NUM_RANKS];
		isVoid = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		players = new String[Constants.NUM_PLAYERS];
		cardsPlayedThisRound =0;
		cardStringsPlayed = new String[Constants.NUM_CARDS];
		
		resetCardKnowledgeTableForNewRound();
	}
	
	public void resetCardKnowledgeTableForNewRound() {
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
		
		
		for(int i=0; i<isVoid.length; i++) {
			for(int j=0; j<isVoid[0].length; j++) {
				isVoid[i][j] = false;
			}
		}
	}
	
	
	public void setupCardsInHandForNewRound(String cards[]) {
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
				
	}
	
	public void updateDataModelWithPlayedCard(String playerName, String card) {
		int indexPlayer = convertPlayerNameToIndex(playerName);
		int cardNum = getMellowCardIndex(card);
		cardsUsed[cardNum/Constants.NUM_RANKS][cardNum%Constants.NUM_RANKS] = true;
		CardsUsedByPlayer[indexPlayer][cardNum/Constants.NUM_RANKS][cardNum%Constants.NUM_RANKS] = true;
		for(int i=0; i<Constants.NUM_PLAYERS; i++) {
			cardsCurrentlyHeldByPlayer[i][cardNum/Constants.NUM_RANKS][cardNum%Constants.NUM_RANKS] = IMPOSSIBLE;
		}
		
		
		cardStringsPlayed[cardsPlayedThisRound] = card;
		cardsPlayedThisRound++;
		//System.out.println("Cards played this round: " + cardsPlayedThisRound);
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
				if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][winnerSuitIndex][i] == CERTAINTY) {
					return getCardString(i, winnerSuitIndex);
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
				return false;
			}
		}
		
		return true;
	}
	
	public String getCardInHandClosestOverCurrentWinner() {

		int throwNumber = cardsPlayedThisRound % Constants.NUM_PLAYERS;
		
		if(throwNumber == 0) {
			System.err.println("ERROR: calling getCardInHandClosestOverCurrentWinner on 1st throw.");
			System.exit(1);
		}

		return getCardInHandClosestOverSameSuit(getCurrentFightWinningCard());
		
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
		return null;
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

	public boolean didLeaderPlayMasterAndIsWinning() {
		
		int cardIndex = getMellowCardIndex(getCardLeaderThrow());
		
		int suitNumber = cardIndex/Constants.NUM_RANKS;
		
		for(int i=(cardIndex + 1) %13; i%13 != 0; i++) {
			for(int j=0; j<Constants.NUM_PLAYERS; j++) {
				if(cardsCurrentlyHeldByPlayer[j][suitNumber][i] != IMPOSSIBLE) {
					return false;
				}
						
			}
		}
		
		if(cardsPlayedThisRound % 4 > 1 && cardAGreaterThanCardBGivenLeadCard(getCardLeaderThrow(), getCardSecondThrow())) {
			return false;
		} else if(cardsPlayedThisRound % 4 > 2 && cardAGreaterThanCardBGivenLeadCard(getCardLeaderThrow(), getCardThirdThrow())) {
			return false;
		}
		
		return true;
	}
	
	
	public boolean currentPlayerHasMasterInSuit(int suitIndex) {
		for(int i=Constants.NUM_RANKS - 1; i>=0; i--) {
			if(cardsUsed[suitIndex][i] == true) {
				continue;
			} else if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][suitIndex][i] == CERTAINTY) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
	//END of MASTER FUNCTIONS

	//LOWEST CARD
	public String getLowCardToLead() {
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
	

	public String getLowOffSuitCardToLead() {
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
		return cardToPlay;
	}

	//END LOWEST CARD
	
	public String getCardInHandClosestOverSameSuit(String card) {
		int cardsCurrentlyHeldByPlayer[][][] = new int[Constants.NUM_PLAYERS][Constants.NUM_SUITS][Constants.NUM_RANKS];
		
		int suitIndex = CardStringFunctions.getIndexOfSuit(card);
		int rankIndex = getRankIndex(card);
		
		for(int j=rankIndex+1; j< Constants.NUM_RANKS; j++) {
			if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][suitIndex][j] == CERTAINTY) {
				return getCardString(rankIndex, suitIndex);
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
	public String currentPlayerGetHighestInSuit(int suitIndex) {
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
	public String currentPlayergetLowestInSuit(int suitIndex) {
		for(int i=0; i<Constants.NUM_RANKS; i++) {
			if(cardsCurrentlyHeldByPlayer[Constants.CURRENT_AGENT_INDEX][suitIndex][i] == CERTAINTY) {
				return getCardString(13*suitIndex + i);
			}
		}
		System.out.println("AHH! Searching for lowest in " + suitIndex + " when player has no card in that suit.");
		System.exit(1);
		return "";
	}
	
	
	
	public boolean playerMightHaveSuit(int playerIndex, int suitIndex) {
		for(int j=0; j<Constants.NUM_RANKS; j++) {
			if(cardsCurrentlyHeldByPlayer[playerIndex][suitIndex][j] != IMPOSSIBLE) {
				return true;
			}
		}
		return false;
	}
	
//TODO: suspicious function
	//TODO: Might delete or make private and only accept Strings from other classes 
	public boolean currentAgentHasSuit(int suitIndex) {
		return playerMightHaveSuit(Constants.CURRENT_AGENT_INDEX, suitIndex);
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

	
}

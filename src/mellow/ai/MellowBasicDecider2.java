package mellow.ai;

import java.util.ArrayList;

//AI that's given the idea of the "expected number of trick to be made at a given point" should be able to judge which
//decisions to make with some accuracy. (I hope)

/*
 * int bid[];
 * double expectedNumTricks[];
 * int tricksEarned[];
 * 
 * formulas:
 * ----------
 * **naive:
 *** Utility Team A = tricksEarned[0] + expectedNumTricks[0]  + tricksEarned[2] + expectedNumTricks[2]
 * **Utility Team B = tricksEarned[1] + expectedNumTricks[1]  + tricksEarned[3] + expectedNumTricks[3]
 *** 
  
 * **Team A safe if Min Utility Team A = tricksEarned[0] + minExpectedNumTricks[0]  + tricksEarned[2] + minExpectedNumTricks[2] > Total Bid:
 * **Team B safe if Min Utility Team B = tricksEarned[1] + minExpectedNumTricks[1]  + tricksEarned[3] + minExpectedNumTricks[3] > Total Bid:
 *
 *** If both unsafe, play for tricks.
 *** if team A unsafe and team B safe, try to mitigate risks.
 *** If team A safe and team B not safe, take risks to try to burn!
 *** If both safe, play for tricks.
 * 
 *** in other words:
 * **	Think about the benefit/cost of burning!
 * --------
 * better: (For now: just ignore the 1 pointers.) (Keep things
 * Utility Team A = expectedPass[0 & 2] - expectedBurn[0 & 2]
 * Utility Team B = expectedPass[1 & 3] - expectedBurn[1 & 3]
 *
 * 
 * If mellow wants to bid player X:
 *  ow = oddsOfWinningMellow
 *  expected value mellow =  ow * 100 - (1 - ow) *100
 *  					= 2 * ow - 100
 *  --------------
 *  Naive:
 * Utility Team A = 2 * ow - 100 + expectedNumTricks[X+2] + expectedNumTricks[2]
 * ---------------
 * Better:
 * Utility Team A = 2 * ow - 100 + expectedNumTricks[X+2] + numBid[2] * 2ow[2]  - numBid[2]
 * 
 * If player wants to burn mellow:
 * Problem: those that don't bid mellow don't know the true odds off making the mellow declarer burn...
 *			But every attempt at burning should carry a calculation of the utility of the trial
 * 
 * Much Later: mellow bids lead to higher varience games. You'll want that more when you're losing.
 * Idea make an array of the odds of winning that's 1000*1000 big.
 * ex: oddsOfWinning[500][900] should be equals to practical 0
 * oddsOfWinning[850][900] should inspire a totally messed up mellow call. It should be about 25%???
 * 
 * 			TODO: do statistics on game to come up with a good estimate of oddsOfWinning[x][y].
 * Also respect  oddsOfWinning[x1][y] > oddsOfWinning[x2][y] if x1>x2
 * and           oddsOfWinning[x][y1] > oddsOfWinning[x][y2] if y1>y2
 * 
 * Question 1: When partner says mellow, how does AI know not to lead the 5H when he/she has the AH?
 * 2 ways: 1) (easy) give leader a bias not to test his/her partner's mellow.
 * 			2) (harder) 
 * 			Give her the forsight to see that it would be better for the person on her LHS to lead Heart.
 * 
 * Question 2: How does AI know to lead masters in non-mellow games:
 * 		A: simply adjust the variable expectedNumTricks[0] and tricksEarned[0] to bias it lead masters to get the trick.
 * 
 * Question 3: How does AI know not to trump over partner's master card in 3rd pos?
 * 		A: make it do a case-by-case analysis. (No matter what 4th has, it's a bad idea.)
 * 
 * -----------------------------------------------------------------------------------
 * Question 4: How will te AI be able to simulate into the future of a game?
 * 		A: this is where the magic comes in!
 * 		 I honestly don't do a precise job of this and leave it to my imagination...
 * 		Idea 1: I'm thinking that I should make it simulate 10000 possible hands based on what was played
 * 			and what was bid and predict the state of the game at the end of the fight.
 * 		Which leads to:
 * 	Question 5: how do you calc utiltity based on what was played:
 * 			1) First rules of thumb.
 * 			2) Make a program do 10000s of simulations against the rules of thumb
 * 				and display the exceptional positions so I could maybe fix the rules of thumb?
 * 			3) Make it a neural net? (Make a neural net to solve everything! :)
 * 		
 * IDEA:
 * 		Between every fight:
 * 			Utility Team A & Utility B should be calculated with a cool algo.
 * 			
 * 		That way: the AIs only need to deal with the following equation:
 * 			utiltyOfDecision = max(utiltyOfMyDecisionInFight + utiltyGameStateAfterFight)
 * 		-But that's hard :(
 * 
 * ------------------
 * I feel that I'm missing something to be able to make it do well. :(
 * -Maybe: tell it what "standard" play is so it can predict based on that?
 * AAHHH!
 * This is HARD... I feel like I need to teach it to be approximate.
 */

public class MellowBasicDecider2 implements MellowAIDeciderInterface {
	int tempAScore;
	int tempBScore;
	
	ArrayList<Integer> currentCards;
	
	
	//TODO: know where the dealer is for bidding.
	
	//TODO: handle case where there's a mellow and then double mellow...
	// :(
	
	
	public static final int NUM_PLAYERS = 4;
	public static final int NUM_SUITS = 4;
	public static final int NUM_NUMBERS = 13;
	public static final int CURRENT_AGENT_INDEX = 0;
	
	public static final int SPADE = 0;
	public static final int HEART = 1;
	public static final int CLUB = 2;
	public static final int DIAMOND = 3;
	
	
	boolean cardsUsed[][] = new boolean[NUM_SUITS][NUM_NUMBERS];
	
	
	
	//0: myCardsUsed
	//1: west cards
	//2: north cards
	//3: east cards
	int IMPOSSIBLE =0;
	int CERTAINTY = 1000;
	int DONTKNOW = -1;
	
	int cardsCurrentlyHeldByPlayer[][][] = new int[NUM_PLAYERS][NUM_SUITS][NUM_NUMBERS];
	
	boolean CardsUsedByPlayer[][][] = new boolean[NUM_PLAYERS][NUM_SUITS][NUM_NUMBERS];

	//  2  3  4 5 .. A
	//S
	//H
	//D
	//C
	
	boolean isVoid[][] = new boolean[NUM_PLAYERS][NUM_SUITS];
	
	
	private String players[] = new String[NUM_PLAYERS];
	
	
	private int cardsPlayedThisRound;
	private String cardStringsPlayed[] = new String[52];
	
	public MellowBasicDecider2(boolean isFast) {
		
	}
	
	
	@Override
	public void receiveUnParsedMessageFromServer(String msg) {
		// TODO: use if you want...
		
	}
	
	@Override
	public void setupCardsForNewRound(String cards[]) {
		System.out.println("Printing cards");
		cardsPlayedThisRound = 0;
		
		 setupInfoForNewRound();
		 
		 for(int i=0; i<cards.length; i++) {
				System.out.println(cards[i]);
				cardsCurrentlyHeldByPlayer[0][getCardNum(cards[i])/13][getCardNum(cards[i])%13]  = CERTAINTY;
				cardsCurrentlyHeldByPlayer[1][getCardNum(cards[i])/13][getCardNum(cards[i])%13]  = IMPOSSIBLE;
				cardsCurrentlyHeldByPlayer[2][getCardNum(cards[i])/13][getCardNum(cards[i])%13]  = IMPOSSIBLE;
				cardsCurrentlyHeldByPlayer[3][getCardNum(cards[i])/13][getCardNum(cards[i])%13]  = IMPOSSIBLE;
				
			}
		 
		System.out.println("Table:");
		 for(int i=0; i<cardsUsed.length; i++) {
				for(int j=0; j<cardsUsed[0].length; j++) {
					if(cardsCurrentlyHeldByPlayer[0][i][j] == CERTAINTY) {
						System.out.print("1");
					} else {
						System.out.print("0");
					}
				}
				System.out.println();
			}
		 
		
		 for(int i=0; i<NUM_SUITS*NUM_NUMBERS; i++) {
			 cardStringsPlayed[i] = "";
		 }
		
		System.out.println("Done printing cards");
	}
	
	
	
	//add info about cards received.
	//add info about the dealer.
	//know who bid what
	//know how many tricks each player made.
	
	private void setupInfoForNewRound() {
		for(int i=0; i<cardsUsed.length; i++) {
			for(int j=0; j<cardsUsed[0].length; j++) {
				cardsUsed[i][j] = false;
				for(int k=0; k< NUM_PLAYERS; k++) {
					CardsUsedByPlayer[k][i][j] = false;
				}
				for(int k=1; k< NUM_PLAYERS; k++) {
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
	

	@Override
	public void setDealer(String playerName) {
		
	}

	@Override
	public void receiveBid(String playerName, int bid) {
		
	}

	@Override
	public void getPlayedCard(String playerName, String card) {
		
		int indexPlayer = playerNameToIndex(playerName);
		int cardNum = getCardNum(card);
		cardsUsed[cardNum/NUM_NUMBERS][cardNum%NUM_NUMBERS] = true;
		CardsUsedByPlayer[indexPlayer][cardNum/NUM_NUMBERS][cardNum%NUM_NUMBERS] = true;
		for(int i=0; i<NUM_PLAYERS; i++) {
			cardsCurrentlyHeldByPlayer[i][cardNum/NUM_NUMBERS][cardNum%NUM_NUMBERS] = IMPOSSIBLE;
		}
		
		
		cardStringsPlayed[cardsPlayedThisRound] = card;
		cardsPlayedThisRound++;
		System.out.println("Cards played this round: " + cardsPlayedThisRound);
	}

	@Override
	public void updateScores(int teamAScore, int teanBScore) {
		
	}

	@Override
	public String getCardToPlay() {
		//leader:
		String cardToPlay = null;;
		System.out.println("**Inside get card to play");
		if(cardsPlayedThisRound % 4 == 0) {
			cardToPlay = AILeaderThrow();
			
		//second play low
		} else if(cardsPlayedThisRound % 4 == 1) {
			cardToPlay = AISecondThrow();
		
		//third plays high.
		} else if(cardsPlayedThisRound % 4 == 2) {
			cardToPlay = AIThirdThrow();
		
		//last barely makes the trick or plays low.
		} else {
			cardToPlay = AIFourthThrow();
		}
		
		if(cardToPlay != null) {
			System.out.println("AI decided on " + cardToPlay);
		}
		
		return cardToPlay;
	}

	//AIs for non-mellow bid games:
	
	public String AILeaderThrow() {
		return null;
	}
	
	public String AISecondThrow() {
		return null;
	}
	
	public String AIThirdThrow() {
		return null;
	}
	
	public String AIFourthThrow() {
		return null;
	}
	//END AIS for non-nellow bid games
	
	
	@Override
	public String getBidToMake() {
		return "1";
	}
	
	
	@Override
	public void setNameOfPlayers(String players[]) {
		for(int i=0; i<NUM_PLAYERS; i++) {
			this.players[i] = players[i] + "";
		}
	}
	
	private int playerNameToIndex(String playerName) {
		for(int i=0; i<NUM_PLAYERS; i++) {
			if(this.players[i].equals(playerName)) {
				return i;
			}
		}
		
		return -1;
	}
	

	private static String getCardString(int cardNum) {
		String ret = "";
		int number = cardNum % 13;
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
			System.out.println("Error: could not get card string from mellow card Number." + cardNum);
			System.exit(1);
		}
		
		int suitIndex = cardNum/13;
		if(suitIndex == 0) {
			ret += "S";
		} else if(suitIndex ==1) {
			ret += "H";
		} else if(suitIndex ==2) {
			ret += "C";
		} else if(suitIndex ==3) {
			ret += "D";
		} else {
			System.out.println("Error: Unknown suit for card number. " + cardNum);
			System.exit(1);
		}
		
		return ret;
	}
	
	private static int getCardNum(String cardString) {
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
	
	
	private boolean didLeaderPlayMaster() {
		return isCardMaster(getCardNumLeader());
	}
	
	private boolean isCardMaster(int cardNum) {
		int suitNumber = cardNum/NUM_NUMBERS;
		
		
		for(int i=(cardNum + 1) %13; i%13 != 0; i++) {
			for(int j=0; j<NUM_PLAYERS; j++) {
				if(cardsCurrentlyHeldByPlayer[j][suitNumber][i] != IMPOSSIBLE) {
					return false;
				}
						
			}
		}
		
		//check the cards currently on the table:
		int tempCardNum;
		for(int i=cardsPlayedThisRound; i%4 != 0; i--) {
			tempCardNum = getCardNum(cardStringsPlayed[i-1]);
			if(getSuitIndex(tempCardNum) == getSuitIndex(cardNum) && getNonSuitedNumberOfThrow(tempCardNum) > getNonSuitedNumberOfThrow(cardNum)) {
				return false;
			}
			
		}
		return true;
	}
	
	private int getSuitIndex(int cardNum) {
		return cardNum/4;
	}
	
	private int getNonSuitedNumberOfThrow(int cardNum) {
		return cardNum % NUM_NUMBERS;
	}

	//pre: leader card thrown
	private int getCardNumLeader() {
		String leaderCardString = cardStringsPlayed[cardsPlayedThisRound - (cardsPlayedThisRound%4)];
		return getCardNum(leaderCardString);
	}

	//pre: 2nd card thrown
	private int getCardNumSecondThrow() {
		String leaderCardString = cardStringsPlayed[cardsPlayedThisRound - (cardsPlayedThisRound%4) + 1];
		return getCardNum(leaderCardString);
	}
	
	//pre: 3rd card thrown
	private int getCardNumThirdThrow() {
		String leaderCardString = cardStringsPlayed[cardsPlayedThisRound - (cardsPlayedThisRound%4) + 2];
		return getCardNum(leaderCardString);
	}
	
	//pre: leader card thrown
	private int getSuitOfLeaderThrow() {
		String suitString = cardStringsPlayed[cardsPlayedThisRound - (cardsPlayedThisRound%4)].charAt(1) + "";
		return getIndexOfSuitString(suitString);
	}
	
	//pre: 2nd card thrown
	private int getSuitOfSecondThrow() {
		String suitString = cardStringsPlayed[cardsPlayedThisRound - (cardsPlayedThisRound%4) + 1].charAt(1) + "";
		return getIndexOfSuitString(suitString);
	}

	//pre: 3rd card thrown
	private int getSuitOfThirdThrow() {
		String suitString = cardStringsPlayed[cardsPlayedThisRound - (cardsPlayedThisRound%4) + 2].charAt(1) + "";
		return getIndexOfSuitString(suitString);
	}
	
	private static int getIndexOfSuitString(String suitString) {
		int index = -1;
		if(suitString.equals("S")) {
			index = 0;
		} else if(suitString.equals("H")) {
			index = 1;
		} else if(suitString.equals("C")) {
			index = 2;
		} else if(suitString.equals("D")) {
			index = 3;
		} else {
			System.out.println("ERROR: unknown suit.");
			System.exit(1);
		}
		return index;
	}
	
	private String currentPlayergetLowOffSuitCardToPlay() {
		String  cardToPlay = null;
		
		FOUNDCARD:
		for(int i=0; i<NUM_NUMBERS; i++) {
			//TODO: have no pref between the off suits... or have a smart preference.
			for(int j=NUM_SUITS - 1; j>SPADE; j--) {
				if(cardsCurrentlyHeldByPlayer[CURRENT_AGENT_INDEX][j][i] == CERTAINTY) {
					cardToPlay = getCardString(13 * j + i);
					break FOUNDCARD;
				}
			}
		}
		return cardToPlay;
	}
	
	private String currentPlayergetLowCardToPlay() {
		String cardToPlay = "";
		
		FOUNDCARD:
		for(int i=0; i<NUM_NUMBERS; i++) {
			//TODO: have no pref between the off suits... or have a smart preference.
			for(int j=NUM_SUITS - 1; j>=SPADE; j--) {
				if(cardsCurrentlyHeldByPlayer[CURRENT_AGENT_INDEX][j][i] == CERTAINTY) {
					cardToPlay = getCardString(13 * j + i);
					break FOUNDCARD;
				}
			}
		}
		return cardToPlay;
	}
	
	//post: returns card in current players hand that's barely over given card.
	// returns null if there's no such card.
	private String currentPlayerGetCardInHandClosestOver(int cardNum) {
		
		int temp = cardNum + 1;
		
		while(temp % 13 != 0) {
			if(cardsCurrentlyHeldByPlayer[CURRENT_AGENT_INDEX][temp/4][temp%13] == CERTAINTY) {
				return getCardString(temp);
			}
			temp++;
		}
		
		return null;
	}
	
	
	private boolean currentPlayerHasMasterInSuit(int suitIndex) {
		for(int i=NUM_NUMBERS - 1; i>=0; i--) {
			if(cardsUsed[suitIndex][i] == true) {
				continue;
			} else if(cardsCurrentlyHeldByPlayer[CURRENT_AGENT_INDEX][suitIndex][i] == CERTAINTY) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
	
	//pre: current player has a card in suit Index.
	private String currentPlayergetHighestInSuit(int suitIndex) {
		for(int i=NUM_NUMBERS - 1; i>=0; i--) {
			if(cardsCurrentlyHeldByPlayer[CURRENT_AGENT_INDEX][suitIndex][i] == CERTAINTY) {
				return getCardString(13*suitIndex + i);
			}
		}
		System.out.println("AHH! Searching for highest in " + suitIndex + " when player has no card in that suit.");
		System.exit(1);
		return "";
	}
	
	//pre: current player has a card in suit Index.
	private String currentPlayergetLowestInSuit(int suitIndex) {
		for(int i=0; i<NUM_NUMBERS; i++) {
			if(cardsCurrentlyHeldByPlayer[CURRENT_AGENT_INDEX][suitIndex][i] == CERTAINTY) {
				return getCardString(13*suitIndex + i);
			}
		}
		System.out.println("AHH! Searching for lowest in " + suitIndex + " when player has no card in that suit.");
		System.exit(1);
		return "";
	}
	
	
	private boolean currentAgentHasSuit(int suitIndex) {
		return otherPlayerMightHaveSuit(CURRENT_AGENT_INDEX, suitIndex);
	}
	
	private boolean otherPlayerMightHaveSuit(int playerIndex, int suitIndex) {
		
		for(int j=0; j<NUM_NUMBERS; j++) {
			if(cardsCurrentlyHeldByPlayer[playerIndex][suitIndex][j] != IMPOSSIBLE) {
				return true;
			}
		}
		return false;
	}
	
}

package mellow.ai;

import java.util.ArrayList;

//_______________________
//This is a basic AI that handles non-mellow rounds for someone who is leading, 2nd, and 3rd.
//_____________________________
//After some calculations, I realized there are hundreds of unique mellow situations that I'd have to make rules for
//to force it to play like me.


//Because I'm lazy, I'm going to not create hundreds of rules and try to program it where it
//can make up it's own mind.

//Because there are so many rules to go through

//“The fact that we live at the bottom of a deep gravity well, on the surface of a gas covered planet going
//around a nuclear fireball 90 million miles away and think this to be normal is obviously some indication
//of how skewed our perspective tends to be.”
//-Douglas Adams

public class MellowBasicDecider implements MellowAIDeciderInterface {
	int tempAScore;
	int tempBScore;
	
	
	
	//TODO: know where the dealer is for bidding.
	
	//TODO: handle case where there's a mellow and then double mellow...
	// :(
	
	
	public static final int NUM_PLAYERS = 4;
	public static final int NUM_SUITS = 4;
	public static final int NUM_NUMBERS = 13;
	public static final int NUM_CARDS = NUM_SUITS * NUM_NUMBERS;
	public static final int CURRENT_AGENT_INDEX = 0;
	

	int IMPOSSIBLE =0;
	int CERTAINTY = 1000;
	int DONTKNOW = -1;
	
	public static final int SPADE = 0;
	public static final int HEART = 1;
	public static final int CLUB = 2;
	public static final int DIAMOND = 3;
	
	
	boolean cardsUsed[][] = new boolean[NUM_SUITS][NUM_NUMBERS];
	
	
	
	//0: myCardsUsed
	//1: west cards
	//2: north cards
	//3: east cards
	
	int cardsCurrentlyHeldByPlayer[][][] = new int[NUM_PLAYERS][NUM_SUITS][NUM_NUMBERS];
	
	boolean CardsUsedByPlayer[][][] = new boolean[NUM_PLAYERS][NUM_SUITS][NUM_NUMBERS];

	//  2  3  4 5 .. A
	//S
	//H
	//D
	//C
	
	boolean isVoid[][] = new boolean[NUM_PLAYERS][NUM_SUITS];
	
	int numberOfMaster = 0;
	
	private String players[] = new String[NUM_PLAYERS];
	
	private int cardsPlayedThisRound;
	private String cardStringsPlayed[] = new String[NUM_CARDS];
	
	public void resetStateForNewRound() {
		cardsUsed = new boolean[NUM_SUITS][NUM_NUMBERS];
		cardsCurrentlyHeldByPlayer = new int[NUM_PLAYERS][NUM_SUITS][NUM_NUMBERS];
		CardsUsedByPlayer = new boolean[NUM_PLAYERS][NUM_SUITS][NUM_NUMBERS];
		isVoid = new boolean[NUM_PLAYERS][NUM_SUITS];
		numberOfMaster = 0;
		players = new String[NUM_PLAYERS];
		cardsPlayedThisRound =0;
		cardStringsPlayed = new String[NUM_CARDS];
		
	}


	@Override
	public void receiveUnParsedMessageFromServer(String msg) {
		// TODO: use if you want...
		
	}
	
	public MellowBasicDecider(boolean isFast) {
		
	}

	public String toString() {
		return "MellowBasicDeciderAI";
	}
	
	//TODO: don't make this so destructive... OR: have testcase say orig card in hand!!!
	@Override
	public void setCardsForNewRound(String cards[]) {
		//System.out.println("Printing cards");
		cardsPlayedThisRound = 0;
		
		 setupInfoForNewRound();
		 
		 for(int i=0; i<cards.length; i++) {
				//System.out.println(cards[i]);
				cardsCurrentlyHeldByPlayer[0][getMellowCardNumber(cards[i])/13][getMellowCardNumber(cards[i])%13]  = CERTAINTY;
				cardsCurrentlyHeldByPlayer[1][getMellowCardNumber(cards[i])/13][getMellowCardNumber(cards[i])%13]  = IMPOSSIBLE;
				cardsCurrentlyHeldByPlayer[2][getMellowCardNumber(cards[i])/13][getMellowCardNumber(cards[i])%13]  = IMPOSSIBLE;
				cardsCurrentlyHeldByPlayer[3][getMellowCardNumber(cards[i])/13][getMellowCardNumber(cards[i])%13]  = IMPOSSIBLE;
				
			}
		
		 /*
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
		 */
		 updateNumberOfMaster();
		 
		 System.out.println("Number of Master cards: " + numberOfMaster);
		
		 for(int i=0; i<NUM_SUITS*NUM_NUMBERS; i++) {
			 cardStringsPlayed[i] = "";
		 }
		
		//System.out.println("Done printing cards");
	}
	
	private int getNextMaster() {
		numberOfMaster = 0;
		//TODO: order the search for master randomly between the off-suits:
		 for(int i=cardsUsed.length - 1; i>0; i--) {
				for(int j=cardsUsed[0].length - 1; j>=0; j--) {
					if(cardsUsed[i][j]) {
						continue;
					} else if(cardsCurrentlyHeldByPlayer[0][i][j] == CERTAINTY) {
						return NUM_NUMBERS * i + j;
					} else {
						break;
					}
				}
			}
		 return -1;
	}
	
	private void updateNumberOfMaster() {
		numberOfMaster = 0;
		 for(int i=0; i<cardsUsed.length; i++) {
				for(int j=cardsUsed[0].length - 1; j>=0; j--) {
					if(cardsUsed[i][j]) {
						continue;
					} else if(cardsCurrentlyHeldByPlayer[0][i][j] == CERTAINTY) {
						numberOfMaster++;
						break;
					} else {
						break;
					}
				}
			}
		 
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
	
	private static String getCardStringFromMellowCardNumber(int cardNum) {
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
	
	private static int getMellowCardNumber(String cardString) {
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

	@Override
	public void setDealer(String playerName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receiveBid(String playerName, int bid) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receiveCardPlayed(String playerName, String card) {
		int indexPlayer = convertPlayerNameToIndex(playerName);
		int cardNum = getMellowCardNumber(card);
		cardsUsed[cardNum/NUM_NUMBERS][cardNum%NUM_NUMBERS] = true;
		CardsUsedByPlayer[indexPlayer][cardNum/NUM_NUMBERS][cardNum%NUM_NUMBERS] = true;
		for(int i=0; i<NUM_PLAYERS; i++) {
			cardsCurrentlyHeldByPlayer[i][cardNum/NUM_NUMBERS][cardNum%NUM_NUMBERS] = IMPOSSIBLE;
		}
		
		updateNumberOfMaster();
		
		cardStringsPlayed[cardsPlayedThisRound] = card;
		cardsPlayedThisRound++;
		//System.out.println("Cards played this round: " + cardsPlayedThisRound);
	}

	@Override
	public void setNewScores(int teamAScore, int teanBScore) {
		// TODO Auto-generated method stub
		
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
		//TODO
		//third plays high.
		} else if(cardsPlayedThisRound % 4 == 2) {
			cardToPlay = AIThirdThrow();
		//TODO:
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
		String cardToPlay = null;
		if(numberOfMaster >= 1) {
			//play a master card:
			int card = getNextMaster();
			cardToPlay = getCardStringFromMellowCardNumber(card);
			System.out.println("***********");
			System.out.println("Playing master card: " + cardToPlay);
			System.out.println("***********");
		} else {
			System.out.println("***********");
			System.out.println("Leading low:");
			System.out.println("***********");
			cardToPlay = getLowCardToPlay();
		}
		return cardToPlay;
	}
	
	public String AISecondThrow() {
		String cardToPlay = null;
		//get suit to follow.
		int leaderSuitIndex = getSuitOfLeaderThrow();
		int leaderCardNumber = getNonSuitedNumberOfLeaderThrow();
		
		System.out.println("Suit Index Leader: " + getSuitOfLeaderThrow() + "  " + getCardStringFromMellowCardNumber(13*getSuitOfLeaderThrow()).substring(1));
		
		if(currentAgentHasSuit(leaderSuitIndex)) {
			//FOLLOW SUIT.
			if(currentPlayerHasMasterInSuit(leaderSuitIndex)) {
				System.out.println("***********");
				System.out.println("2nd FOLLOW SUIT HIGH");
				cardToPlay = currentPlayergetHighestInSuit(leaderSuitIndex);
				//don't play high if leader played higher.
				if(getMellowCardNumber(cardToPlay) % NUM_NUMBERS < leaderCardNumber % NUM_NUMBERS) {
					System.out.println("2nd NEVER MIND FOLLOW SUIT LOW");
					cardToPlay = currentPlayergetLowestInSuit(leaderSuitIndex);
				}
				System.out.println("***********");
			} else {
				System.out.println("2nd FOLLOW SUIT LOW");
				cardToPlay = currentPlayergetLowestInSuit(leaderSuitIndex);
			}
			
		} else {
			//check to see if we could trump:
			//If we could trump, just trump :)
		
			if(currentAgentHasSuit(SPADE)) {
					System.out.println("***********");
					System.out.println("2nd trump low.");
					cardToPlay = currentPlayergetLowestInSuit(SPADE);
			} else {
				System.out.println("***********");
				System.out.println("2nd play low off.");
					cardToPlay = getLowOffSuitCardToPlay();
			}
				
		}
		
	
		return cardToPlay;
	}
	
	public String AIThirdThrow() {
		String cardToPlay = null;
		int leaderSuitIndex = getSuitOfLeaderThrow();
		int leaderCardNumber = getNonSuitedNumberOfLeaderThrow();
		
			//check to see if we could trump:
			//If we could trump, just trump :)
			
		//CAN'T FOLLOW SUIT:
		if(currentAgentHasSuit(leaderSuitIndex) == false) {
			if(currentAgentHasSuit(SPADE)) {
				if( getSuitOfSecondThrow() == SPADE) {
					cardToPlay = getCardInHandClosestOver(getCardNumSecondThrow());
					if(cardToPlay == null) {
						cardToPlay = getLowOffSuitCardToPlay();
					}
				} else {
					if(didLeaderPlayMaster()) {
						cardToPlay = getLowOffSuitCardToPlay();
					} else {
						cardToPlay = currentPlayergetLowestInSuit(SPADE);
					}
				}
			} else {
				cardToPlay = getLowOffSuitCardToPlay();
			}
		//FOLLOW SUIT:
		} else {
			if(getSuitOfLeaderThrow() != SPADE && getSuitOfSecondThrow() == SPADE) {
				cardToPlay = currentPlayergetLowestInSuit(leaderSuitIndex);
			} else {
				if(getSuitOfSecondThrow() != getSuitOfLeaderThrow()) {
					if(cardPower(currentPlayergetHighestInSuit(leaderSuitIndex)) >  cardPower(leaderCardNumber) + 1) {
						cardToPlay = currentPlayergetHighestInSuit(leaderSuitIndex);
						
					} else {
						cardToPlay = currentPlayergetLowestInSuit(leaderSuitIndex);
					}
					
				} else {
					String testCard = currentPlayergetHighestInSuit(leaderSuitIndex);
					if(cardPower(testCard) >  cardPower(leaderCardNumber) + 1 && cardPower(testCard) >  cardPower(getCardNumSecondThrow())) {
						cardToPlay = currentPlayergetHighestInSuit(leaderSuitIndex);
						
					} else {
						cardToPlay = currentPlayergetLowestInSuit(leaderSuitIndex);
					}
				}
			}
		}
				
	
		return cardToPlay;
	}
	
	//TODO: complete this if you feel like it.
	//TODO: KISS
	public String AIFourthThrow() {
		String cardToPlay = null;
		int leaderSuitIndex = getSuitOfLeaderThrow();
		//TODO: aHH!
		int leaderCardNumber = getNonSuitedNumberOfLeaderThrow();
		int secondCardNumber = getNonSuitedNumberOfLeaderThrow();
		int thirdCardNumber = getNonSuitedNumberOfLeaderThrow();
		if(leaderSuitIndex == SPADE) {
			
		} else {
			//Follow off-suit:
			if(currentAgentHasSuit(leaderSuitIndex) == true) {
				String highestCard = currentPlayergetHighestInSuit(leaderSuitIndex);
				//if(cardPower(highestCard) > cardPower(highestCard) )
				
			//Don't folow suit.
			} else {
				
			}
		}
		
		
		//NEVER MIND!
		return cardToPlay;
	}
	//END AIS for non-nellow bid games
	
	private int CURRENT_PLAYER = 0;
	private int ACE = 12;
	private int KING = 11;
	
	private int getNumberOfAces() {
		int ret = 0;
		for(int i=0; i<NUM_SUITS; i++) {
			if(cardsCurrentlyHeldByPlayer[CURRENT_PLAYER][i][ACE]  == CERTAINTY) {
				ret++;
			}
		}
		return ret;
	}
	
	private int getNumberOfKings() {
		int ret = 0;
		for(int i=0; i<NUM_SUITS; i++) {
			if(cardsCurrentlyHeldByPlayer[CURRENT_PLAYER][i][KING]  == CERTAINTY) {
				ret++;
			}
		}
		return ret;
	}
	
	private int getNumberOfCardsOneSuit(int suit) {
		int ret = 0;
		for(int i=0; i<13; i++) {
			if(cardsCurrentlyHeldByPlayer[CURRENT_PLAYER][suit][i]  == CERTAINTY) {
				ret++;
			}
		}
		return ret;
	}
	
	
	private boolean hasCard(String card) {
		int num = getMellowCardNumber(card);
		
		return cardsCurrentlyHeldByPlayer[CURRENT_PLAYER][num/13][num%13]  == CERTAINTY;
			
	}
	
	
	//TODO: will need to test/improve.
	
	@Override
	public String getBidToMake() {
		//Converted python function from github to java here:
		
		double bid = 0.0;
		
		//#Add number of aces:
		//bid = bid + getNumberOfAces(hand)
		bid += getNumberOfAces();
		
		System.out.println("Number of aces: " + getNumberOfAces());
		
		//if trumping means losing a king or queen of spades, then it doesn't mean much
		boolean trumpingIsSacrifice = false;
		
		
		//Add king of spades if 1 or 2 other spade
		if (hasCard("KS") && getNumberOfCardsOneSuit(0) >= 2) {
		
			bid += 1.0;
			if (getNumberOfCardsOneSuit(0) == 2) {
				bid = bid - 0.2;
				trumpingIsSacrifice = true;
			}
			System.out.println("I have the KS");
		}
			
		//Add queen of spacdes if 2 other spaces
		if (hasCard("QS") && getNumberOfCardsOneSuit(0) >= 3) {
			bid += 1.0;
			trumpingIsSacrifice = true;
			System.out.println("I have the QS");
		}
			
			
		if (hasCard("JS") && getNumberOfCardsOneSuit(0) >= 4 && (hasCard("QS") || hasCard("KS") || hasCard("AS"))) {
			bid += bid + 0.15;
			System.out.println("I have the JS and a higher one... bonus I guess!");
		}
		
		if(getNumberOfCardsOneSuit(0) >= 4 ) {
			trumpingIsSacrifice = true;
		}
			
		
		double trumpResevoir = 0.0;
		
		//Add a bid for every extra spade over 3 you have:
		if (getNumberOfCardsOneSuit(0) >= 3) {
			bid += getNumberOfCardsOneSuit(0) - 3.5;
			
			if (hasCard("JS")) {
				bid +=  0.5;
			} else if (hasCard("TS")) {
				bid += 0.3;
				trumpResevoir = 0.201;
			} else if(getNumberOfCardsOneSuit(1) < 2 ||  getNumberOfCardsOneSuit(2) < 2 ||  getNumberOfCardsOneSuit(3) < 2) {
				bid += 0.2;
				trumpResevoir = 0.301;
			} else {
				trumpResevoir = 0.501;
			}
		}
			//TODO: 5+ spades should give a special bonus depending on the offsuits.
		    // The "take everything" bonus :P
		
		
		int numOffSuitKings = 0;
		if(hasCard("KS")) {
			numOffSuitKings = getNumberOfKings() - 1;
		} else {
			numOffSuitKings = getNumberOfKings();
		}
		
		bid += 0.75 * numOffSuitKings;

		//offsuit king adjust if too many or too little of a single suit:
		//TODO: COPY/PASTE CODE loop through suits:
		if(hasCard("KH") && getNumberOfCardsOneSuit(1) == 1) {
			bid = bid - 0.55;
		} else if(hasCard("KH") && getNumberOfCardsOneSuit(1) > 5) {
			bid = bid - 0.35;
		} else if(hasCard("KH") && (hasCard("QH") || hasCard("AH"))) {
			bid = bid + 0.26;
		}
		
		if(hasCard("KC") && getNumberOfCardsOneSuit(2) == 1) {
			bid = bid - 0.55;
		} else if(hasCard("KC") && getNumberOfCardsOneSuit(2) > 5) {
			bid = bid - 0.35;
		} else if(hasCard("KC") && (hasCard("QC") || hasCard("AC"))) {
			bid = bid + 0.26;
		}
		
		if(hasCard("KD") && getNumberOfCardsOneSuit(3) == 1) {
			bid = bid - 0.55;
		} else if(hasCard("KD") && getNumberOfCardsOneSuit(3) > 5) {
			bid = bid - 0.35;
		} else if(hasCard("KD") && (hasCard("QD") || hasCard("AD"))) {
			bid = bid + 0.26;
		}
		
		//END offsuit king adjustment logic
		//END TODO COPY/PASTE CODE
			
		if(getNumberOfCardsOneSuit(1) < 3 || getNumberOfCardsOneSuit(2) < 3|| getNumberOfCardsOneSuit(3) < 3) {
			if(getNumberOfCardsOneSuit(0) >= 2 && getNumberOfCardsOneSuit(0) < 4 && trumpingIsSacrifice == false ) {

				bid += 0.3;
				
				if( (getNumberOfCardsOneSuit(1) < 3 && getNumberOfCardsOneSuit(2) < 3) ||
						(getNumberOfCardsOneSuit(1) < 3 && getNumberOfCardsOneSuit(3) < 3) ||
						(getNumberOfCardsOneSuit(2) < 3 && getNumberOfCardsOneSuit(3) < 3) ) {
					bid += 0.75; 
				} else if(getNumberOfCardsOneSuit(1) < 2 || getNumberOfCardsOneSuit(2) < 2 || getNumberOfCardsOneSuit(3) < 2) {
					bid += 0.75;
				}

			} else if(getNumberOfCardsOneSuit(0) >= 4 && trumpResevoir > 0) {
				if( (getNumberOfCardsOneSuit(1) < 3 && getNumberOfCardsOneSuit(2) < 3) ||
						(getNumberOfCardsOneSuit(1) < 3 && getNumberOfCardsOneSuit(3) < 3) ||
						(getNumberOfCardsOneSuit(2) < 3 && getNumberOfCardsOneSuit(3) < 3) ) {
					bid += trumpResevoir; 
				} else if(getNumberOfCardsOneSuit(1) < 2 || getNumberOfCardsOneSuit(2) < 2 || getNumberOfCardsOneSuit(3) < 2) {
					bid += trumpResevoir;
				}
			}
		}
		
		//TODO: didn't handle mellow
		//Didn't handle akqjt (a sweep)
		
		if(getNumberOfCardsOneSuit(0) == 0) {
			bid = bid  - 1;
		}
		
		int intBid = (int) Math.floor(bid);
		
		if (intBid < 0) {
			intBid = 0;
		}
	
		System.out.println("Final bid " + intBid);

		
		return intBid + "";
	}

	
	@Override
	public void setNameOfPlayers(String players[]) {
		for(int i=0; i<NUM_PLAYERS; i++) {
			this.players[i] = players[i] + "";
		}
	}
	
	private int convertPlayerNameToIndex(String playerName) {
		for(int i=0; i<NUM_PLAYERS; i++) {
			if(this.players[i].equals(playerName)) {
				return i;
			}
		}
		
		return -1;
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
		return true;
	}
	
	private int getNonSuitedNumberOfLeaderThrow() {
		return getCardNumLeader() % NUM_NUMBERS;
	}

	//pre leader card thrown
	private int getCardNumLeader() {
		String leaderCardString = cardStringsPlayed[cardsPlayedThisRound - (cardsPlayedThisRound%4)];
		return getMellowCardNumber(leaderCardString);
	}

	//pre 2nd card thrown
	private int getCardNumSecondThrow() {
		String leaderCardString = cardStringsPlayed[cardsPlayedThisRound - (cardsPlayedThisRound%4) + 1];
		return getMellowCardNumber(leaderCardString);
	}
	
	//pre 3rd card thrown
	private int getCardNumThirdThrow() {
		String leaderCardString = cardStringsPlayed[cardsPlayedThisRound - (cardsPlayedThisRound%4) + 2];
		return getMellowCardNumber(leaderCardString);
	}
	
	//pre leader card thrown
	private int getSuitOfLeaderThrow() {
		String suitString = cardStringsPlayed[cardsPlayedThisRound - (cardsPlayedThisRound%4)].charAt(1) + "";
		return getIndexOfSuitString(suitString);
	}
	
	//pre 2nd card thrown
	private int getSuitOfSecondThrow() {
		String suitString = cardStringsPlayed[cardsPlayedThisRound - (cardsPlayedThisRound%4) + 1].charAt(1) + "";
		return getIndexOfSuitString(suitString);
	}

	//pre 3rd card thrown
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
	
	private String getLowOffSuitCardToPlay() {
		String  cardToPlay = null;
		
		FOUNDCARD:
		for(int i=0; i<NUM_NUMBERS; i++) {
			//TODO: have no pref between the off suits... or have a smart preference.
			for(int j=NUM_SUITS - 1; j>SPADE; j--) {
				if(cardsCurrentlyHeldByPlayer[CURRENT_AGENT_INDEX][j][i] == CERTAINTY) {
					cardToPlay = getCardStringFromMellowCardNumber(13 * j + i);
					break FOUNDCARD;
				}
			}
		}
		return cardToPlay;
	}
	
	private String getLowCardToPlay() {
		String cardToPlay = "";
		
		FOUNDCARD:
		for(int i=0; i<NUM_NUMBERS; i++) {
			//TODO: have no pref between the off suits... or have a smart preference.
			for(int j=NUM_SUITS - 1; j>=SPADE; j--) {
				if(cardsCurrentlyHeldByPlayer[CURRENT_AGENT_INDEX][j][i] == CERTAINTY) {
					cardToPlay = getCardStringFromMellowCardNumber(13 * j + i);
					break FOUNDCARD;
				}
			}
		}
		return cardToPlay;
	}
	
	//post: returns card in current players hand that's barely over given card.
	// returns nll if there's no such card.
	private String getCardInHandClosestOver(int cardNum) {
		
		int temp = cardNum + 1;
		
		while(temp % 13 != 0) {
			if(cardsCurrentlyHeldByPlayer[CURRENT_AGENT_INDEX][temp/4][temp%13] == CERTAINTY) {
				return getCardStringFromMellowCardNumber(temp);
			}
			temp++;
		}
		
		return null;
	}
	
	private boolean currentAgentHasSuit(int suitIndex) {
		return playerMightHaveSuit(CURRENT_AGENT_INDEX, suitIndex);
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
				return getCardStringFromMellowCardNumber(13*suitIndex + i);
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
				return getCardStringFromMellowCardNumber(13*suitIndex + i);
			}
		}
		System.out.println("AHH! Searching for lowest in " + suitIndex + " when player has no card in that suit.");
		System.exit(1);
		return "";
	}
	
	
	
	private boolean playerMightHaveSuit(int playerIndex, int suitIndex) {
		for(int j=0; j<NUM_NUMBERS; j++) {
			if(cardsCurrentlyHeldByPlayer[playerIndex][suitIndex][j] != IMPOSSIBLE) {
				return true;
			}
		}
		return false;
	}
	
	private int cardPower(String cardString) {
		return getMellowCardNumber(cardString);
	}
	private int cardPower(int cardNum) {
		int ret = cardNum % 13;
		//if it's a spade, add 13:
		if(cardNum < 13) {
			ret+= 13;
		}
		
		return ret;
		
	}


}

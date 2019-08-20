package mellow.ai;

import java.util.ArrayList;

import mellow.ai.cardDataModels.impl.BooleanTableDataModel;

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
	
	//TODO: make this an interface...
	
	BooleanTableDataModel dataModel = new BooleanTableDataModel();
	
	
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
	
	
	//boolean cardsUsed[][] = new boolean[NUM_SUITS][NUM_NUMBERS];
	
	
	
	//0: myCardsUsed
	//1: west cards
	//2: north cards
	//3: east cards
	

	public void resetStateForNewRound() {
		dataModel.resetStateForNewRound();
		
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
		dataModel.setupCardsInHandForNewRound(cards);
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
		dataModel.updateDataModelWithPlayedCard(playerName, card);
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
		if(dataModel.getMasterCard() != null) {
			//play a master card:
			cardToPlay = dataModel.getMasterCard();
			System.out.println("***********");
			System.out.println("Playing master card: " + cardToPlay);
			System.out.println("***********");
		} else {
			System.out.println("***********");
			System.out.println("Leading low:");
			System.out.println("***********");
			cardToPlay = dataModel.getLowCardToPlay();
		}
		return cardToPlay;
	}
	
	public String AISecondThrow() {
		String cardToPlay = null;
		//get suit to follow.
		int leaderSuitIndex = dataModel.getSuitOfLeaderThrow();
		
		System.out.println("Suit Index Leader: " + dataModel.getSuitOfLeaderThrow() + "  " + dataModel.getCardString(13*dataModel.getSuitOfLeaderThrow()).substring(1));
		
		if(currentAgentHasSuit(leaderSuitIndex)) {
			//FOLLOW SUIT.
			if(dataModel.currentPlayerHasMasterInSuit(leaderSuitIndex)) {
				System.out.println("***********");
				System.out.println("2nd FOLLOW SUIT HIGH");
				cardToPlay = currentPlayergetHighestInSuit(leaderSuitIndex);
				//don't play high if leader played higher.
				if( dataModel.cardAGreaterThanCardB(cardToPlay, dataModel.getCardLeaderThrow())) {
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
				cardToPlay = dataModel.getLowOffSuitCardToPlay();
			}
				
		}
		
	
		return cardToPlay;
	}
	
	public String AIThirdThrow() {
		String cardToPlay = null;
		int leaderSuitIndex = dataModel.getSuitOfLeaderThrow();
		
			//check to see if we could trump:
			//If we could trump, just trump :)
			
		//CAN'T FOLLOW SUIT:
		if(currentAgentHasSuit(leaderSuitIndex) == false) {
			if(currentAgentHasSuit(SPADE)) {
				if( dataModel.getSuitOfSecondThrow() == SPADE) {
					cardToPlay = dataModel.getCardInHandClosestOverSameSuit(dataModel.getCardSecondThrow());
					if(cardToPlay == null) {
						cardToPlay = dataModel.getLowOffSuitCardToPlay();
					}
				} else {
					if(dataModel.didLeaderPlayMasterAndIsWinning()) {
						cardToPlay = dataModel.getLowOffSuitCardToPlay();
					} else {
						cardToPlay = currentPlayergetLowestInSuit(SPADE);
					}
				}
			} else {
				cardToPlay = dataModel.getLowOffSuitCardToPlay();
			}
		//FOLLOW SUIT:
		} else {
			if(dataModel.getSuitOfLeaderThrow() != SPADE && dataModel.getSuitOfSecondThrow() == SPADE) {
				cardToPlay = currentPlayergetLowestInSuit(leaderSuitIndex);
			} else {
				if(dataModel.getSuitOfSecondThrow() != dataModel.getSuitOfLeaderThrow()) {
					String testCard = currentPlayergetHighestInSuit(leaderSuitIndex);
					
					if(dataModel.cardAGreaterThanCardB(testCard, dataModel.getCardLeaderThrow())) {
						cardToPlay = testCard;
						
					} else {
						cardToPlay = currentPlayergetLowestInSuit(leaderSuitIndex);
					}
					
				} else {
					String testCard = currentPlayergetHighestInSuit(leaderSuitIndex);
					if(dataModel.cardAGreaterThanCardB(testCard, dataModel.getCardLeaderThrow()) && dataModel.cardAGreaterThanCardB(testCard, dataModel.getCardSecondThrow()) ) {
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
		int leaderSuitIndex = dataModel.getSuitOfLeaderThrow();
		
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
		if (dataModel.hasCard("KS") && getNumberOfCardsOneSuit(0) >= 2) {
		
			bid += 1.0;
			if (getNumberOfCardsOneSuit(0) == 2) {
				bid = bid - 0.2;
				trumpingIsSacrifice = true;
			}
			System.out.println("I have the KS");
		}
			
		//Add queen of spacdes if 2 other spaces
		if (dataModel.hasCard("QS") && getNumberOfCardsOneSuit(0) >= 3) {
			bid += 1.0;
			trumpingIsSacrifice = true;
			System.out.println("I have the QS");
		}
			
			
		if (dataModel.hasCard("JS") && getNumberOfCardsOneSuit(0) >= 4 && (dataModel.hasCard("QS") || dataModel.hasCard("KS") || dataModel.hasCard("AS"))) {
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
			
			if (dataModel.hasCard("JS")) {
				bid +=  0.5;
			} else if (dataModel.hasCard("TS")) {
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
		if(dataModel.hasCard("KS")) {
			numOffSuitKings = getNumberOfKings() - 1;
		} else {
			numOffSuitKings = getNumberOfKings();
		}
		
		bid += 0.75 * numOffSuitKings;

		//offsuit king adjust if too many or too little of a single suit:
		//TODO: COPY/PASTE CODE loop through suits:
		if(dataModel.hasCard("KH") && getNumberOfCardsOneSuit(1) == 1) {
			bid = bid - 0.55;
		} else if(dataModel.hasCard("KH") && getNumberOfCardsOneSuit(1) > 5) {
			bid = bid - 0.35;
		} else if(dataModel.hasCard("KH") && (dataModel.hasCard("QH") || dataModel.hasCard("AH"))) {
			bid = bid + 0.26;
		}
		
		if(dataModel.hasCard("KC") && getNumberOfCardsOneSuit(2) == 1) {
			bid = bid - 0.55;
		} else if(dataModel.hasCard("KC") && getNumberOfCardsOneSuit(2) > 5) {
			bid = bid - 0.35;
		} else if(dataModel.hasCard("KC") && (dataModel.hasCard("QC") || dataModel.hasCard("AC"))) {
			bid = bid + 0.26;
		}
		
		if(dataModel.hasCard("KD") && getNumberOfCardsOneSuit(3) == 1) {
			bid = bid - 0.55;
		} else if(dataModel.hasCard("KD") && getNumberOfCardsOneSuit(3) > 5) {
			bid = bid - 0.35;
		} else if(dataModel.hasCard("KD") && (dataModel.hasCard("QD") || dataModel.hasCard("AD"))) {
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
	
	private boolean currentAgentHasSuit(int suitIndex) {
		return playerMightHaveSuit(CURRENT_AGENT_INDEX, suitIndex);
	}
	
	//pre: current player has a card in suit Index.
	private String currentPlayergetHighestInSuit(int suitIndex) {
		for(int i=NUM_NUMBERS - 1; i>=0; i--) {
			if(cardsCurrentlyHeldByPlayer[CURRENT_AGENT_INDEX][suitIndex][i] == CERTAINTY) {
				return dataModel.getCardString(13*suitIndex + i);
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
				return dataModel.getCardString(13*suitIndex + i);
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
	


}

package mellow.ai;

import java.util.ArrayList;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.situationHandlers.PlaySituation;
import mellow.cardUtils.CardStringFunctions;

//_______________________
//This is a basic AI that handles non-mellow rounds for someone who is leading, 2nd, and 3rd.
//_____________________________
//After some calculations, I realized there are hundreds of unique mellow situations that I'd have to make rules for
//to force it to play like me.


//Because I'm lazy, I'm going to not create hundreds of rules and try to program it where it
//can make up it's own mind.

//Because there are so many rules to go through

public class MellowBasicDecider implements MellowAIDeciderInterface {
	
	//TODO: make this an interface...
	
	DataModel dataModel = new DataModel();
	
	
	//TODO: know where the dealer is for bidding.
	
	//TODO: handle case where there's a mellow and then double mellow...
	// :(
	
	
	public static final int SPADE = 0;
	public static final int HEART = 1;
	public static final int CLUB = 2;
	public static final int DIAMOND = 3;
	
	//index
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
		dataModel.setBid(playerName, bid);
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
		

		for(int i=0; i<4; i++) {
			if(dataModel.burntMellow(i)) {
				System.out.println("TEST BURNT MELLOW index: " + i);
			}
		}
		
		if(dataModel.throwerCanOnlyPlayOneCard()) {
			System.out.println("**Forced to play card");
			return dataModel.getOnlyCardCurrentPlayerCouldPlay();
		}

		int numActiveMellows = 0;
		for(int i=0; i<4; i++) {
			if(dataModel.getBid(i) ==0 && dataModel.burntMellow(i) == false) {
				numActiveMellows++;
			}
		}
		
		if(numActiveMellows == 0) {
			return PlaySituation.handleNormalThrow(dataModel);
			
		} else if(numActiveMellows == 1) {
			if(dataModel.getBid(Constants.CURRENT_AGENT_INDEX) == 0) {
				System.out.println("MELLOW TEST");
				return handleThrowAsSingleActiveMellowBidder();
				
			} else {

				//TODO: not quite right: (Need to protect mellow or attack mellow...)
				return PlaySituation.handleNormalThrow(dataModel);
			}
			
		} else if(numActiveMellows == 2) {
			if(dataModel.getBid(Constants.CURRENT_AGENT_INDEX) == 0) {
				
				
				System.out.println("MELLOW TEST (double mellow)");
				return handleThrowAsSingleActiveMellowBidder();

			} else {

				//TODO: not quite right: (Need to protect mellow or attack mellow...)
				return PlaySituation.handleNormalThrow(dataModel);
				
			}
			
		} else {
			System.err.println("Really???");
			System.exit(1);
			return "";
		}
		
		
		
	}

	
	public String handleThrowAsSingleActiveMellowBidder() {

		int throwIndex = dataModel.getCardsPlayedThisRound() % Constants.NUM_PLAYERS;
		//leader:
		String cardToPlay = null;
		System.out.println("**Inside get card to play");
		
		if(dataModel.burntMellow(Constants.CURRENT_AGENT_INDEX)) {
			System.out.println("BURNT MELLOW");
		}
		
		if(throwIndex == 0) {
			cardToPlay = AIMellowLead();
			
		//Handle the common mellow follow case:
		} else {
			cardToPlay = AIMellowFollow();
		}
		
		if(cardToPlay != null) {
			System.out.println("AI decided on " + cardToPlay);
		}
		
		return cardToPlay;
	}
	
	public String AIMellowLead() {
		
		String highestSpade = dataModel.getCardCurrentPlayerGetHighestInSuit(SPADE);
		
		//TODO: if highest Spade is Queen, maybe don't do this?
		//TOO complicated... :(
		
		return highestSpade;
	}
	
	public String AIMellowFollow() {
		int leaderSuitIndex = dataModel.getSuitOfLeaderThrow();
		
		String cardToPlay = "";
		
		if(dataModel.currentAgentHasSuit(leaderSuitIndex)) {
			//Follow suit:
			
			//if no one has trump:
			String currentFightWinner = dataModel.getCurrentFightWinningCard();
			
			if(CardStringFunctions.getIndexOfSuit(currentFightWinner) == leaderSuitIndex) {
				
				if(dataModel.couldPlayCardInHandUnderCardInSameSuit(currentFightWinner)) {

					//Play just below winning card if possible
					cardToPlay = dataModel.getCardInHandClosestUnderSameSuit(currentFightWinner);
				
				} else {
					//Play slightly above winning card and hope you'll get covered
					cardToPlay = dataModel.getCardInHandClosestOverSameSuit(currentFightWinner);
				}
			} else if(CardStringFunctions.getIndexOfSuit(currentFightWinner) == SPADE && leaderSuitIndex != SPADE) {
				//Someone trumped, play high
				cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(leaderSuitIndex);
				
			} else {
				System.err.println("ERROR in AIMellowFollow: The currently winning card should be the lead suit or spade.");
				System.exit(1);
			}
			
			
		} else {
			//Can't follow suit:
			
			//TODO: throw off card that gets rid of the most risk...
			// Because this is probably the hardest decision to make in mellow, I'm going to simplify it... :(
			
			
			String currentFightWinner = dataModel.getCurrentFightWinningCard();
			if(CardStringFunctions.getIndexOfSuit(currentFightWinner) == SPADE) {
				//Play spade under trump (this isn't always a good idea, but...
				
				if(dataModel.currentAgentHasSuit(SPADE)) {
					if(dataModel.couldPlayCardInHandUnderCardInSameSuit(currentFightWinner)) {
						//Play spade under trump
						return dataModel.getCardInHandClosestUnderSameSuit(currentFightWinner);
						 
					} else if(dataModel.getNumberOfCardsOneSuit(SPADE) == dataModel.getNumCardsInCurrentPlayerHand()) {
						
						//If mellow player only has spade over trump, play lower spade:
						return dataModel.getCardInHandClosestOverSameSuit(currentFightWinner);
					}
					
				}
			}
			
			
			//TODO: this is a terrible approximation... this is not how I play the game.
			cardToPlay = dataModel.getHighestOffSuitCardToLead();
		}
		
		return cardToPlay;
	
	}
	
	
	//TODO: will need to test/improve.
	
	@Override
	public String getBidToMake() {
		//Converted python function from github to java here:
		
		double bid = 0.0;
		
		//#Add number of aces:
		//bid = bid + getNumberOfAces(hand)
		bid += dataModel.getNumberOfAces();
		
		System.out.println("Number of aces: " + dataModel.getNumberOfAces());
		
		//if trumping means losing a king or queen of spades, then it doesn't mean much
		boolean trumpingIsSacrifice = false;
		
		
		//Add king of spades if 1 or 2 other spade
		if (dataModel.hasCard("KS") && dataModel.getNumberOfCardsOneSuit(0) >= 2) {
		
			bid += 1.0;
			if (dataModel.getNumberOfCardsOneSuit(0) == 2) {
				bid = bid - 0.2;
				
				trumpingIsSacrifice = true;
			}
			System.out.println("I have the KS");
		}
			
		//Add queen of spacdes if 2 other spaces
		if (dataModel.hasCard("QS") && dataModel.getNumberOfCardsOneSuit(0) >= 3) {
			bid += 1.0;
			trumpingIsSacrifice = true;
			System.out.println("I have the QS");
		}
			
	
		if(dataModel.getNumberOfCardsOneSuit(0) >= 4 ) {
			trumpingIsSacrifice = true;
		}
		
		double trumpResevoir= 0.0;
		if (dataModel.getNumberOfCardsOneSuit(0) == 3) {
			trumpResevoir = 0.44;
		}
		
		//Add a bid for every extra spade over 3 you have:
		if (dataModel.getNumberOfCardsOneSuit(0) >= 4) {
			bid += dataModel.getNumberOfCardsOneSuit(0) - 4.0;
			
			if (dataModel.hasCard("JS")) {
				bid +=  1.0;
			} else if (dataModel.hasCard("TS")) {
				bid += 0.8;
				trumpResevoir = 0.201;
			} else if(dataModel.getNumberOfCardsOneSuit(1) < 2 ||  dataModel.getNumberOfCardsOneSuit(2) < 2 ||  dataModel.getNumberOfCardsOneSuit(3) < 2) {
				bid += 0.7;
				trumpResevoir = 0.301;
			} else {
				trumpResevoir = 1.001;
			}
			
		}
			//TODO: 5+ spades should give a special bonus depending on the offsuits.
		    // The "take everything" bonus :P
		
		
		int numOffSuitKings = 0;
		if(dataModel.hasCard("KS")) {
			numOffSuitKings = dataModel.getNumberOfKings() - 1;
		} else {
			numOffSuitKings = dataModel.getNumberOfKings();
		}
		
		bid += 0.75 * numOffSuitKings;
		
		//offsuit king adjust if too many or too little of a single suit:
		for(int i=0; i<Constants.OFFSUITS.length; i++) {
			String off = Constants.OFFSUITS[i];
			if(dataModel.hasCard("K" + off) && dataModel.getNumberOfCardsOneSuit(i + 1) == 1) {
				bid = bid - 0.55;
			} else if(dataModel.hasCard("K" + off) && dataModel.getNumberOfCardsOneSuit(i + 1) > 5) {
				bid = bid - 0.65;
			} else if(dataModel.hasCard("K" + off) && dataModel.getNumberOfCardsOneSuit(i + 1) > 4) {
				bid = bid - 0.25;
			} else if(dataModel.hasCard("K" + off) && dataModel.getNumberOfCardsOneSuit(i + 1) > 3) {
				bid = bid - 0.20;
			}
			
			if(dataModel.hasCard("K" + off) && (dataModel.hasCard("Q" + off) || dataModel.hasCard("A" + off))) {
				bid = bid + 0.26;
			}
		}
		
		//END offsuit king adjustment logic
			
		if(dataModel.getNumberOfCardsOneSuit(1) < 3 || dataModel.getNumberOfCardsOneSuit(2) < 3|| dataModel.getNumberOfCardsOneSuit(3) < 3) {
			if(dataModel.getNumberOfCardsOneSuit(0) >= 2 && dataModel.getNumberOfCardsOneSuit(0) < 4 && trumpingIsSacrifice == false ) {

				bid += 0.3;
				
				if( (dataModel.getNumberOfCardsOneSuit(1) < 3 && dataModel.getNumberOfCardsOneSuit(2) < 3) ||
						(dataModel.getNumberOfCardsOneSuit(1) < 3 && dataModel.getNumberOfCardsOneSuit(3) < 3) ||
						(dataModel.getNumberOfCardsOneSuit(2) < 3 && dataModel.getNumberOfCardsOneSuit(3) < 3) ) {
					bid += 0.75; 
				} else if(dataModel.getNumberOfCardsOneSuit(1) < 2 || dataModel.getNumberOfCardsOneSuit(2) < 2 || dataModel.getNumberOfCardsOneSuit(3) < 2) {
					bid += 0.75;
				}

			} else if(dataModel.getNumberOfCardsOneSuit(0) >= 4 && trumpResevoir > 0) {
				if( (dataModel.getNumberOfCardsOneSuit(1) < 3 && dataModel.getNumberOfCardsOneSuit(2) < 3) ||
						(dataModel.getNumberOfCardsOneSuit(1) < 3 && dataModel.getNumberOfCardsOneSuit(3) < 3) ||
						(dataModel.getNumberOfCardsOneSuit(2) < 3 && dataModel.getNumberOfCardsOneSuit(3) < 3) ) {
					bid += trumpResevoir; 
				} else if(dataModel.getNumberOfCardsOneSuit(1) < 2 || dataModel.getNumberOfCardsOneSuit(2) < 2 || dataModel.getNumberOfCardsOneSuit(3) < 2) {
					bid += trumpResevoir;
				}
			}
		}
		
		//TODO: didn't handle mellow
		//Didn't handle akqjt (a sweep)
		
		if(dataModel.getNumberOfCardsOneSuit(0) == 0) {
			bid = bid  - 1;
		}
		
		System.out.println("Bid double: " + bid);
		int intBid = (int) Math.floor(bid);
		
		if (intBid < 0) {
			intBid = 0;
		}
	
		System.out.println("Final bid " + intBid);

		
		return intBid + "";
	}

	
	@Override
	public void setNameOfPlayers(String players[]) {
		dataModel.setNameOfPlayers(players);
	}
	

}

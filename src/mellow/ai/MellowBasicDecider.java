package mellow.ai;

import java.util.ArrayList;

import mellow.Constants;
import mellow.ai.cardDataModels.impl.BooleanTableDataModel;

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
	
	BooleanTableDataModel dataModel = new BooleanTableDataModel();
	
	
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
		
		if(dataModel.throwerCanOnlyPlayOneCard()) {
			System.out.println("**Forced to play card");
			return dataModel.getOnlyCardCurrentPlayerCouldPlay();
		}
		
		int throwIndex = dataModel.getCardsPlayedThisRound() % Constants.NUM_PLAYERS;
		//leader:
		String cardToPlay = null;
		System.out.println("**Inside get card to play");
		if(throwIndex == 0) {
			cardToPlay = AILeaderThrow();
			
		//second play low
		} else if(throwIndex == 1) {
			cardToPlay = AISecondThrow();
		//TODO
		//third plays high.
		} else if(throwIndex == 2) {
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
			cardToPlay = dataModel.getLowCardToLead();
		}
		return cardToPlay;
	}
	
	public String AISecondThrow() {
		String cardToPlay = null;
		//get suit to follow.
		
		//TODO: only deal with string (No index)
		int leaderSuitIndex = dataModel.getSuitOfLeaderThrow();
		
		System.out.println("Suit Index Leader: " + dataModel.getSuitOfLeaderThrow() + "  " + dataModel.getCardString(13*dataModel.getSuitOfLeaderThrow()).substring(1));
		
		if(dataModel.currentAgentHasSuit(leaderSuitIndex)) {
			//FOLLOW SUIT.
			if(dataModel.hasMasterInSuit(leaderSuitIndex)) {
				System.out.println("***********");
				System.out.println("2nd FOLLOW SUIT HIGH");
				cardToPlay = dataModel.currentPlayerGetHighestInSuit(leaderSuitIndex);
				//don't play high if leader played higher.
				if( dataModel.cardAGreaterThanCardBGivenLeadCard(cardToPlay, dataModel.getCardLeaderThrow())) {
					System.out.println("2nd NEVER MIND FOLLOW SUIT LOW");
					cardToPlay = dataModel.currentPlayergetLowestInSuit(leaderSuitIndex);
				}
				System.out.println("***********");
			} else {
				System.out.println("2nd FOLLOW SUIT LOW");
				cardToPlay = dataModel.currentPlayergetLowestInSuit(leaderSuitIndex);
			}
			
		} else {
			//check to see if we could trump:
			//If we could trump, just trump :)
		
			if(dataModel.currentAgentHasSuit(SPADE)) {
					System.out.println("***********");
					System.out.println("2nd trump low.");
					cardToPlay = dataModel.currentPlayergetLowestInSuit(SPADE);
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
		if(dataModel.currentAgentHasSuit(leaderSuitIndex) == false) {
			if(dataModel.currentAgentHasSuit(SPADE)) {
				
				//TODO: simplicfy: just play over 2nd thrower if possible
				if( dataModel.getSuitOfSecondThrow() == SPADE) {
					
					//if could trump over
					if(dataModel.couldPlayCardInHandOverCardInSameSuit(dataModel.getCardSecondThrow())) {
						cardToPlay = dataModel.getCardInHandClosestOverSameSuit(dataModel.getCardSecondThrow());
					
						//else
						if(cardToPlay == null) {
							System.err.println("ERROR: unexpected null in 3rd thrower");
							System.exit(1);
						}
					} else {
						
						cardToPlay = dataModel.getLowOffSuitCardToPlay();
						
					}
				} else {
					if(dataModel.leaderPlayedMaster()) {
						//PLAY OFF
						cardToPlay = dataModel.getLowOffSuitCardToPlay();
					} else {
						//TRUMP
						cardToPlay = dataModel.currentPlayergetLowestInSuit(SPADE);
					}
				}
			} else {
				cardToPlay = dataModel.getLowOffSuitCardToPlay();
			}
		
		//FOLLOW SUIT:
		} else {
			
			//If TRUMPED
			if(dataModel.getSuitOfLeaderThrow() != SPADE && dataModel.getSuitOfSecondThrow() == SPADE) {
					cardToPlay = dataModel.currentPlayergetLowestInSuit(leaderSuitIndex);
			
			//FIGHT WITHIN SUIT:
			} else {
				
				//If lead is winning
				if(dataModel.cardAGreaterThanCardBGivenLeadCard(dataModel.getCardLeaderThrow(), dataModel.getCardSecondThrow())) {
					
					
					
					if(dataModel.getNonLeadCardInHandThatCanDecreaseChanceof4thThrowerWinning() != null) {
						
					}
					//if 3rd thrower could decrease chance of 4th winning
							//play high in suit
					//else
						//play low in suit
					
				
				//If 2nd thrower is winning:
				} else {
				
					
				//if 2nd is winning play
					//if could play over
						//if 4th doesn't have suit
							//play barely over
						//else
							//play highest over
					//else
						//play lower
				}
			}
		}
				
	
		return cardToPlay;
	}
	
	public String AIFourthThrow() {
		String cardToPlay = null;
		
		if(dataModel.isPartnerWinningFight()) {
			cardToPlay = dataModel.getJunkiestCardToFollowLead();

		} else if(dataModel.throwerHasCardToBeatCurrentWinner()) {
			cardToPlay = dataModel.getCardClosestOverCurrentWinner();
			
		} else {
			cardToPlay = dataModel.getJunkiestCardToFollowLead();
			
		}

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
			
			
		if (dataModel.hasCard("JS") && dataModel.getNumberOfCardsOneSuit(0) >= 4 && (dataModel.hasCard("QS") || dataModel.hasCard("KS") || dataModel.hasCard("AS"))) {
			bid += bid + 0.15;
			System.out.println("I have the JS and a higher one... bonus I guess!");
		}
		
		if(dataModel.getNumberOfCardsOneSuit(0) >= 4 ) {
			trumpingIsSacrifice = true;
		}
			
		
		double trumpResevoir = 0.0;
		
		//Add a bid for every extra spade over 3 you have:
		if (dataModel.getNumberOfCardsOneSuit(0) >= 3) {
			bid += dataModel.getNumberOfCardsOneSuit(0) - 3.5;
			
			if (dataModel.hasCard("JS")) {
				bid +=  0.5;
			} else if (dataModel.hasCard("TS")) {
				bid += 0.3;
				trumpResevoir = 0.201;
			} else if(dataModel.getNumberOfCardsOneSuit(1) < 2 ||  dataModel.getNumberOfCardsOneSuit(2) < 2 ||  dataModel.getNumberOfCardsOneSuit(3) < 2) {
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
			numOffSuitKings = dataModel.getNumberOfKings() - 1;
		} else {
			numOffSuitKings = dataModel.getNumberOfKings();
		}
		
		bid += 0.75 * numOffSuitKings;
		
		//offsuit king adjust if too many or too little of a single suit:
		for(int i=0; i<Constants.OFFSUITS.length; i++) {
			String off = Constants.OFFSUITS[i];
			if(dataModel.hasCard("K" + off) && dataModel.getNumberOfCardsOneSuit(1) == 1) {
				bid = bid - 0.55;
			} else if(dataModel.hasCard("K" + off) && dataModel.getNumberOfCardsOneSuit(1) > 5) {
				bid = bid - 0.35;
			} else if(dataModel.hasCard("K" + off) && (dataModel.hasCard("Q" + off) || dataModel.hasCard("A" + off))) {
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

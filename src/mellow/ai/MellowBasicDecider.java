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
		
		
		//START REALLY OLD CODE:
		//SEE NOTES FOR BETTER PLAN
		//TODO: pseudo code for not following suit
		
		//TODO: only deal with string (No index)
		int leaderSuitIndex = dataModel.getSuitOfLeaderThrow();
		String leaderCard = dataModel.getCardLeaderThrow();
		
		//TODO currentAgentHasSuit and isVoid does the same thing...?
		if(dataModel.currentAgentHasSuit(leaderSuitIndex)) {
			
			if(dataModel.couldPlayCardInHandOverCardInSameSuit(leaderCard)) {
				
				boolean thirdVoid = dataModel.isVoid(1, leaderSuitIndex);
				boolean fourthVoid = dataModel.isVoid(2, leaderSuitIndex);
			
			
				if(thirdVoid && fourthVoid) {	
					cardToPlay = dataModel.getCardInHandClosestOverSameSuit(dataModel.getCardLeaderThrow());

				} else if(thirdVoid && fourthVoid == false) {
					//Maybe play low? I don't know...
					cardToPlay = dataModel.getCardInHandClosestOverSameSuit(dataModel.getCardLeaderThrow());
				
				} else if(thirdVoid == false && fourthVoid) {
					//TODO This doesn't really work if trump is spade... 
					cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(leaderSuitIndex);
					
				} else if(thirdVoid == false && fourthVoid == false){
					
					if(dataModel.hasMasterInSuit(leaderSuitIndex)) {
						cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(leaderSuitIndex);
					} else {
						cardToPlay = dataModel.getCardCurrentPlayergetLowestInSuit(leaderSuitIndex);
					}
					
				} else {
					System.err.println("ERROR: this condition shouldn't happen in get ai 2nd throw");
					System.exit(1);
				}

			} else {
				cardToPlay = dataModel.getCardCurrentPlayergetLowestInSuit(leaderSuitIndex);
			}
			
			
			//No following suit:
		} else {
			
			//no trumping: play off
			if(leaderSuitIndex== SPADE || dataModel.isVoid(0, SPADE)) {
				cardToPlay = dataModel.getLowOffSuitCardToPlay();
				
				//Option to trump:
			} else {
				if(dataModel.isMasterCard(leaderCard) && dataModel.getNumCardsPlayedForSuit(SPADE) < 2 * Constants.NUM_PLAYERS) {
					cardToPlay = dataModel.getCardCurrentPlayergetLowestInSuit(SPADE);

				} else if(dataModel.isMasterCard(leaderCard) && (dataModel.isVoid(2, SPADE) || dataModel.isVoid(2, leaderSuitIndex) == false)) {
					cardToPlay = dataModel.getCardCurrentPlayergetLowestInSuit(SPADE);

				//I guess we should trump if we don't have much spade?
				} else if((dataModel.isVoid(2, SPADE) || dataModel.isVoid(2, leaderSuitIndex) == false) && (13 - dataModel.getNumCardsPlayedForSuit(SPADE))/4 >= dataModel.getNumberOfCardsOneSuit(SPADE)) {
					cardToPlay = dataModel.getCardCurrentPlayergetLowestInSuit(SPADE);

				} else {
					cardToPlay = dataModel.getLowOffSuitCardToPlay();
				}
			}
		}
		
	
		return cardToPlay;
	}
	
	public String AIThirdThrow() {
		String cardToPlay = null;
		int leaderSuitIndex = dataModel.getSuitOfLeaderThrow();
		
		
		//CAN'T FOLLOW SUIT:
		if(dataModel.currentAgentHasSuit(leaderSuitIndex) == false) {

			if(dataModel.currentAgentHasSuit(SPADE)) {
				
				//2nd thrower trumped:
				if( dataModel.getSuitOfSecondThrow() == SPADE) {
					
					//if could trump over, just go barely over
					if(dataModel.couldPlayCardInHandOverCardInSameSuit(dataModel.getCardSecondThrow())) {
						
						//TODO: what if 4th thrower is also able to trump? That gets into weird logic
						cardToPlay = dataModel.getCardInHandClosestOverSameSuit(dataModel.getCardSecondThrow());
					
					} else {
						//If can't trump over
						cardToPlay = dataModel.getLowOffSuitCardToPlay();
						
					}
				} else {
					
					//if your partner played master and 2nd thrower didn't trump over
					if(dataModel.leaderPlayedMaster()) {
						//PLAY OFF because leaderPlayedMaster
						cardToPlay = dataModel.getLowOffSuitCardToPlay();
					} else {
						//TRUMP
						
						//TODO: what if leader(partner) plays a higher card than 2nd throw that isn't master, but 4th could trump too... 
						//... I don't even know. That gets into weird logic
						cardToPlay = dataModel.getCardCurrentPlayergetLowestInSuit(SPADE);
					}
				}
				
				//No Spade, so play off:
			} else {
				cardToPlay = dataModel.getLowOffSuitCardToPlay();
			}
		
		//FOLLOW SUIT:
		} else {
			
			//If leader got TRUMPED by 2nd player:
			if(dataModel.getSuitOfLeaderThrow() != SPADE && dataModel.getSuitOfSecondThrow() == SPADE) {
					cardToPlay = dataModel.getCardCurrentPlayergetLowestInSuit(leaderSuitIndex);
			
			//FIGHT WITHIN SUIT:
			} else {
				
				//If lead is winning
				if(dataModel.cardAGreaterThanCardBGivenLeadCard(dataModel.getCardLeaderThrow(), dataModel.getCardSecondThrow())) {
					
					if(dataModel.hasNonLeadCardInHandThatCanDecreaseChanceof4thThrowerWinning()) {
						cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(dataModel.getSuitOfLeaderThrow());
						
					} else {
						cardToPlay = dataModel.getCardCurrentPlayergetLowestInSuit(dataModel.getSuitOfLeaderThrow());
					}
				
				//If 2nd thrower is winning:
				} else {
				
					//If currentAgent could play over 2nd thrower:
					if(dataModel.couldPlayCardInHandOverCardInSameSuit(dataModel.getCardSecondThrow())) {
						
						//Sanity check:
						if(dataModel.getSuitOfSecondThrow() != dataModel.getSuitOfLeaderThrow()) {
							System.err.println("ERROR: At this point, I expected the 2nd thrower to have followed suit.");
							System.exit(1);
						}
						
						//If we know 4th is void:
						if(dataModel.isVoid(1, dataModel.getSuitOfLeaderThrow())) {
							
							//Play highest to force 4th to play even higher... or stop 4th thrower from winning:
							cardToPlay = dataModel.getCardInHandClosestOverSameSuit(dataModel.getCardSecondThrow());
						
						} else {
							//play barely over 2nd thrower to force 4th thrower to trump for the win:
							cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(dataModel.getSuitOfLeaderThrow());
						}
					} else {
						
						cardToPlay = dataModel.getCardCurrentPlayergetLowestInSuit(dataModel.getSuitOfLeaderThrow());
					}
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

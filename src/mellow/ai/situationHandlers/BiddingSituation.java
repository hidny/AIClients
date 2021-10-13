package mellow.ai.situationHandlers;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.situationHandlers.bidding.BasicBidMellowWinProbCalc;
import mellow.ai.situationHandlers.bidding.BiddingNearEndOfGameFunctions;
import mellow.cardUtils.DebugFunctions;

public class BiddingSituation {
	
	public static String getSimpleBidToMake(DataModel dataModel) {
		//Converted python function from github to java here:
		
		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "JS TS 4S 3S 2S 6H AC 7C 6C 3C KD TD 5D ")) {
			System.out.println("Debug");
		}
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
			if (dataModel.getNumberOfCardsOneSuit(0) == 2 && dataModel.hasCard("AS") == false) {
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

			//Lowered to 3.75 from 4.0 and got 13 less failed cases. 
			bid += dataModel.getNumberOfCardsOneSuit(0) - 3.75;
			
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
		
		boolean AStrumpingBonus = false;
		if(dataModel.hasCard("AS") ) {
			
			if(dataModel.getNumberOfCardsOneSuit(0) == 1) {
				//No bonus
			} else if(dataModel.hasCard("KS") && dataModel.getNumberOfCardsOneSuit(0) == 2) {
				//No bonus
			} else if(dataModel.hasCard("QS") && dataModel.getNumberOfCardsOneSuit(0) == 3) {
				//No bonus
			} else {

				if(dataModel.getNumberOfCardsOneSuit(0) <= 4) {
					AStrumpingBonus = true;
					trumpResevoir += 0.2;
				}
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
				} else if(AStrumpingBonus) {
					bid += 0.60;
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
		
		//Don't double mellow:
		if(intBid == 0
				&& (dataModel.playerMadeABidInRound(Constants.CURRENT_PARTNER_INDEX)
				    && dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) == 0)) {
			intBid = 1;
		}
		
		//Don't bid mellow with KS
		if(intBid == 0 && dataModel.hasCard("KS")) {
			intBid = 1;
		
		// Just don't say mellow if 2 cards are over the 9S...
		} else if(intBid == 0 && dataModel.getNumCardsInCurrentPlayersHandOverCardSameSuit("9S") >= 2) {
			intBid = 1;
		}
		System.out.println("Final bid " + intBid);

		
		if(dataModel.getOurScore() > 800
				&& intBid > 0) {
			

			int scoresProjectedWorstCase[] = BiddingNearEndOfGameFunctions.getProjectedScoresAssumingTheWorst(dataModel, intBid);
			int ourScoreWorstCaseNoBurn = scoresProjectedWorstCase[0];
			int theirScoreWorstCaseNoBurn = scoresProjectedWorstCase[1];
			
			
			int temp1 = intBid;
			if(dataModel.isDealer()) {
				temp1 = BiddingNearEndOfGameFunctions.OLDadjustDealerBidBecauseGameIsCloseToOver(dataModel, intBid);
			} else if(dataModel.getDealerIndexAtStartOfRound() == Constants.LEFT_PLAYER_INDEX) {
				temp1 = BiddingNearEndOfGameFunctions.OLDadjust3rdBidBecauseGameIsCloseToOver(dataModel, intBid);
			}
			
			int temp2 = BiddingNearEndOfGameFunctions.getPossiblyLowerBidBecauseItsNearEndOfGameAssumeWorst(dataModel, intBid, scoresProjectedWorstCase);
			
			System.out.println("TEST scores " + dataModel.getOurScore() + " vs " + dataModel.getOpponentScore());
			
			System.out.println("Test near end bid: " + temp1 + " vs " + temp2);
			
			intBid = temp2;
			
			//TODO:
			//int scoresProjectedWorstCaseButOpponentsDontSayMellow[] = getProjectedScoresAssumingTheWorst(dataModel, intBid);
			//boolean opponentsDidntsaymellowyet TODO
			
			
			//Stop thinking if we are projected to win if we win our tricks:
			if(ourScoreWorstCaseNoBurn >= Constants.GOAL_SCORE
					&& ourScoreWorstCaseNoBurn > theirScoreWorstCaseNoBurn) {
				
				return intBid + "";

			}
			
		}
		
		if(dataModel.getOurScore() < 800
				&&  dataModel.getDealerIndexAtStartOfRound() == Constants.CURRENT_AGENT_INDEX) {
			while(dataModel.getBidTotalSoFar() + intBid >= Constants.NUM_STARTING_CARDS_IN_HAND
					&& intBid > 1) {
				System.out.println("(LOWER BID TO BE SAFE)");
				intBid--;
			}
		}
		
		//TODO: put into function (make it 5 lines instead of 15)
		if(dataModel.getOpponentScore() > 800
				&& dataModel.getDealerIndexAtStartOfRound() == Constants.CURRENT_AGENT_INDEX) {
			

			//Condition for bidding higher if you're about to lose:
			//See if you want to bid high...
			int scoresProjectedWorstCase[] = BiddingNearEndOfGameFunctions.getProjectedScoresAssumingTheWorst(dataModel, intBid);
			
			int bonusPoints = BiddingNearEndOfGameFunctions.getNumberOfPointsAvailableWithBidAsDealer(dataModel, intBid);
			
			int oppScore = scoresProjectedWorstCase[1] - bonusPoints;
			int ourScore = scoresProjectedWorstCase[0] + bonusPoints;
			
			if(oppScore > ourScore
					&& oppScore >= Constants.GOAL_SCORE
					&& BiddingNearEndOfGameFunctions.opponentsDidntSayMellow(dataModel)) {
				
				//TODO: If opponents said mellow, say mellow if it means your team may win
				return BiddingNearEndOfGameFunctions.getFinalWildDealerBid(dataModel, intBid);
			}
		}
		
		
		
		if(BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel) > 0.3) {
			//System.out.println("Mellow prob: " + BasicBidMellowWinProbCalc.getMellowSuccessProb1(dataModel));
			//System.out.println("Mellow prob2: " + BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel));
			//System.out.println("int bid: " + intBid);

		}
		
		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "TS 5S JH 7H 5H 3H JC 9C 6C TD 6D 4D 2D ")) {
			System.out.println("Debug");
		}
		
		if(BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel) > 0.5 + intBid * 0.05
				//No double mellow:
				&& ( ! dataModel.playerMadeABidInRound(Constants.CURRENT_PARTNER_INDEX)
					    || dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) != 0
					    //Make an exception:
					    || BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel) > 0.9)) {
			
			//TODO: put into function:
			if(dataModel.getOurScore() > 900) {
				if(intBid >= 1) {
					int scoresProjectedWorstCase[] = BiddingNearEndOfGameFunctions.getProjectedScoresAssumingTheWorst(dataModel, intBid);
				
					if(scoresProjectedWorstCase[0] >= Constants.GOAL_SCORE
						&& scoresProjectedWorstCase[1] < scoresProjectedWorstCase[0]
						&& (BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel) < 0.8
							 || ! dataModel.playerMadeABidInRound(Constants.CURRENT_PARTNER_INDEX))) {
						
						
						return intBid + "";
					}
				} else {
					
					//TODO: maybe don't assume worst later?
					int scoresProjectedWorstCase[] = BiddingNearEndOfGameFunctions.getProjectedScoresAssumingTheWorst(dataModel, 1);
					
					if(scoresProjectedWorstCase[0] >= Constants.GOAL_SCORE
						&& scoresProjectedWorstCase[1] < scoresProjectedWorstCase[0]
						&& (BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel) < 0.6
						    || ! dataModel.playerMadeABidInRound(Constants.CURRENT_PARTNER_INDEX))) {
						
						
						return 1 + "";
					}
				}
			}
			//END TODO
			
			return 0 + "";
			
		} else if(BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel) < 0.4
				&& intBid == 0) {
			return 1 + "";
		} else {
			
			return intBid + "";
		}
	}
	
	
	//TODO: put below functions in their own class:
	
}

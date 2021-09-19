package mellow.ai.situationHandlers;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.situationHandlers.bidding.BasicMellowWinProbCalc;

public class BiddingSituation {
	
	public static String getSimpleBidToMake(DataModel dataModel) {
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
		if(intBid == 0 && dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) == 0) {
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

		
		if(dataModel.getOurScore() > 800 ||
				dataModel.getOpponentScore() > 800) {
				
			if(dataModel.isDealer()) {
				intBid = adjustDealerBidBecauseGameIsCloseToOver(dataModel, intBid);
			}
				
		}
		
		if(BasicMellowWinProbCalc.getMellowSuccessProb1(dataModel) > 0.3) {
			//System.out.println("Mellow prob: " + BasicMellowWinProbCalc.getMellowSuccessProb1(dataModel));
		}
		
		if(BasicMellowWinProbCalc.getMellowSuccessProb1(dataModel) > 0.5 + intBid * 0.05) {
			return 0 + "";
		} else if(BasicMellowWinProbCalc.getMellowSuccessProb1(dataModel) < 0.3
				&& intBid == 0) {
			return 1 + "";
		} else {
			return intBid + "";
		}
	}
	
	
	//TODO: this is way too rough...
	public static int adjustDealerBidBecauseGameIsCloseToOver(DataModel dataModel, int origBid) {
		int ret = origBid;
		
		int curScore = dataModel.getOurScore();

		int prevBid = dataModel.getBid(Constants.CURRENT_PARTNER_INDEX);
		int projectedIncreaseScore = 0;
		if(prevBid == 0) {
			projectedIncreaseScore += 100;
		} else {
			projectedIncreaseScore += 10 * prevBid;
		}
		
		if(origBid == 0) {
			projectedIncreaseScore += 100;
		} else {
			projectedIncreaseScore += 10 * prevBid;
		}
		
		int projectedScore = curScore + projectedIncreaseScore;
		
		
		int theirScore = dataModel.getOpponentScore();
		int firstOppBid = dataModel.getBid(Constants.LEFT_PLAYER_INDEX);
		int secondOppBid = dataModel.getBid(Constants.RIGHT_PLAYER_INDEX);
		
		int projectedIncreaseOppScore = 0;
		
		if(firstOppBid == 0) {
			projectedIncreaseOppScore += 100;
		} else {
			projectedIncreaseOppScore += 10 * firstOppBid;
		}
		
		if(secondOppBid == 0) {
			projectedIncreaseOppScore += 100;
		} else {
			projectedIncreaseOppScore += 10 * secondOppBid;
		}
		
		int projectedOppScore = theirScore + projectedIncreaseOppScore;
		
		int giveEmTheExtraScore = 13 - firstOppBid - secondOppBid - prevBid - origBid;
		projectedOppScore += giveEmTheExtraScore;
		
		if(projectedScore > 1000 && projectedScore > projectedOppScore) {
			
			int decreaseInBid = 1;
			for(decreaseInBid = 1; decreaseInBid<origBid; decreaseInBid++) {
				if(projectedScore - 9 * decreaseInBid > 1000
						&& projectedScore - 9 * decreaseInBid > projectedIncreaseOppScore) {
					//keep going
				} else {
					break;
				}
			}
			
			if(decreaseInBid > 1) {
				System.out.println("(BID LESS TEST)");
			}
			ret = origBid - (decreaseInBid - 1);
			
		}
		
		return ret;
	}
}

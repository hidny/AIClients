package mellow.ai.situationHandlers.bidding;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.cardUtils.DebugFunctions;

public class BiddingNearEndOfGameFunctions {

	

	public static double getOddsOfWinningWithFinalDealerBidHigherToCompete(DataModel dataModel, int origBid, int curHighBid) {
		
		//Made up a really crude formula:
		
		double ret = 0.95;
		
		for(int i=origBid; i<curHighBid; i++) {
			if(i < 4) {
				ret *= (0.5 - 0.1 * i);
			} else {
				ret *= 0.2;
			}
		}
		
		return ret;
		
	}
		
	public static String getHigherBidRequiredToWinInLastRound(DataModel dataModel) {
		
		int currentBid = putTotalBidTo14AsDealer(dataModel);
		
		int scoresProjectedWorstCaseHighBid[] = getProjectedScoresAssumingTheWorst(dataModel, currentBid);
		int oppScoreHighBid = scoresProjectedWorstCaseHighBid[1];
		int ourScoreHighBid = scoresProjectedWorstCaseHighBid[0];
		
		if(ourScoreHighBid <= oppScoreHighBid) {
			
			//If we need to burn opponent, we can't bid any lower:
			return currentBid + "";
			
		} else {
			
			//Try to bid lower because we could afford it:
			
			//TODO 1: PUT INTO FUNCTION TRY TO LOWER BID SLIGHTLY
			//TODO 2: MAKE SIMPLER TO READ VERSION THAT USES THE getProjectedScoresAssumingTheWorst FUNCTION
			//Try to bid 1 lower so the total bid is 13:
			int testLowerBidBy1 = currentBid - 1;
			
			if(testLowerBidBy1 == 0) {
				return currentBid + "";
			}
			
			if(oppScoreHighBid > ourScoreHighBid - 10) {
				return currentBid + "";
			} else {
				currentBid = testLowerBidBy1;
				
				ourScoreHighBid = ourScoreHighBid -10;
			}
			
			int finalBid = currentBid;
			
			//Try to decrease total bid under total of 13:
			//Every time we decrease bid by one, assume opponents get the bonus point.
			
			for(int numDecreaseUnder13TotalBid = 1; currentBid - numDecreaseUnder13TotalBid > 0; numDecreaseUnder13TotalBid++) {
				int tempOurScoreHighBid = ourScoreHighBid - 10 * numDecreaseUnder13TotalBid;
				int tempOppScoreHighBid = oppScoreHighBid + 1 * numDecreaseUnder13TotalBid;
				
				if(tempOppScoreHighBid > tempOurScoreHighBid) {
					break;
				} else if(tempOppScoreHighBid >= tempOurScoreHighBid){
					//Lower the bid by one but maybe you need the extra trick...
					finalBid--;
					break;
				} else {
					finalBid--;
				}
				
				
			}
			
			return finalBid + "";
			

			//END TODO 1: PUT INTO FUNCTION TRY TO LOWER BID SLIGHTLY
		}
		
	}

	public static double getOddsOfWinningWithFinalDealerBidMellow(DataModel dataModel) {
		
		int scoresProjectedWorstCaseMellowPass[] =
				BiddingNearEndOfGameFunctions.getProjectedScoresAssumingTheWorst(dataModel, 0);
		
		int bonusPoints = BiddingNearEndOfGameFunctions.getNumberOfPointsAvailableWithBidAsDealer(dataModel, 0);
		
		int oppScore = scoresProjectedWorstCaseMellowPass[1] - bonusPoints;
		int ourScore = scoresProjectedWorstCaseMellowPass[0] + bonusPoints;
		
		
		if(dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) == 0 ) {
			return 0.0;
			
		} else if(oppScore > ourScore
				&& oppScore >= Constants.GOAL_SCORE) {
			
			return 0.0;
		
		} else if(oppScore > ourScore
				&& oppScore >= Constants.GOAL_SCORE
				&& ourScore > oppScore - 2 * bonusPoints) {
			return 0.5 * BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel);
			
		} else if(oppScore > ourScore
				&& oppScore >= Constants.GOAL_SCORE
				&& ourScore == oppScore - 2 * bonusPoints) {
			return 0.2 * BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel);
			
		} else {
			if(opponentsDidntSayMellow(dataModel)) {
				return BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel);
			} else {
				//I just made up this formula. I hope it works!
				return 0.4 + (1 - 0.4) * BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel);
			}
		}
	}
	
	//TODO: this function could get more complicated...
	public static boolean dontSayMellowBecauseYoureWinning(int scoresProjectedWorstCase[], double oddsOfWinningMellow) {
		
		System.out.println(oddsOfWinningMellow);
		System.out.println("Projected scores: " + scoresProjectedWorstCase[0] + " vs " + scoresProjectedWorstCase[1]);
		
		int diff = scoresProjectedWorstCase[0] - scoresProjectedWorstCase[1];
		
		if(oddsOfWinningMellow > 0.95) {
			//Check if we got the mellow in the bag:
			return false;
		}
		
		if(scoresProjectedWorstCase[0] > 900
				&& diff > 80) {
			return true;
		} else if(scoresProjectedWorstCase[0] > 800
				&& diff > 150) {
			return true;
		} else if(scoresProjectedWorstCase[0] > 700
			&& diff > 300) {
			return true;
		}
		
		return false;
	}

	public static String getFinalWildDealerBidOpponentsDidntSayMellow(DataModel dataModel, int origBid) {
		//TODO: put in function increase bid
		System.out.println("(MAKING WILD DEALER BID)");
		
		boolean sayMellow = false;
		
		if(origBid > 0) {
			sayMellow = isSayingMellowFeasible(dataModel, origBid);
		}
		
		
		if(sayMellow) {
			return 0 + "";

		} else {
			//TODO: more functions!
			
			int currentBid = putTotalBidTo14AsDealer(dataModel);
			
			int scoresProjectedWorstCaseHighBid[] = getProjectedScoresAssumingTheWorst(dataModel, currentBid);
			int oppScoreHighBid = scoresProjectedWorstCaseHighBid[1];
			int ourScoreHighBid = scoresProjectedWorstCaseHighBid[0];
			
			if(ourScoreHighBid <= oppScoreHighBid) {
				
				//If we need to burn opponent, we can't bid any lower:
				return currentBid + "";
				
			} else {
				
				//Try to bid lower because we could afford it:
				
				//TODO 1: PUT INTO FUNCTION TRY TO LOWER BID SLIGHTLY
				//TODO 2: MAKE SIMPLER TO READ VERSION THAT USES THE getProjectedScoresAssumingTheWorst FUNCTION
				//Try to bid 1 lower so the total bid is 13:
				int testLowerBidBy1 = currentBid - 1;
				
				if(testLowerBidBy1 == 0) {
					return currentBid + "";
				}
				
				if(oppScoreHighBid > ourScoreHighBid - 10) {
					return currentBid + "";
				} else {
					currentBid = testLowerBidBy1;
					
					ourScoreHighBid = ourScoreHighBid -10;
				}
				
				int finalBid = currentBid;
				
				//Try to decrease total bid under total of 13:
				//Every time we decrease bid by one, assume opponents get the bonus point.
				
				for(int numDecreaseUnder13TotalBid = 1; currentBid - numDecreaseUnder13TotalBid > 0; numDecreaseUnder13TotalBid++) {
					int tempOurScoreHighBid = ourScoreHighBid - 10 * numDecreaseUnder13TotalBid;
					int tempOppScoreHighBid = oppScoreHighBid + 1 * numDecreaseUnder13TotalBid;
					
					if(tempOppScoreHighBid > tempOurScoreHighBid) {
						break;
					} else if(tempOppScoreHighBid >= tempOurScoreHighBid){
						//Lower the bid by one but maybe you need the extra trick...
						finalBid--;
						break;
					} else {
						finalBid--;
					}
					
					
				}
				
				return finalBid + "";
				

				//END TODO 1: PUT INTO FUNCTION TRY TO LOWER BID SLIGHTLY
			}
		}
	}
	

	
	public static boolean isSayingMellowFeasible(DataModel dataModel, int origBid) {
		
		if(origBid == 0) {
			System.err.println("WARNING: Checking if mellow is a good idea while originally bidding mellow...");
			
		}
		
		boolean sayMellow = false;
		if(BasicBidMellowWinProbCalc.getMellowSuccessProb1(dataModel) > 0.05) {
			int scoresProjectedWorstCaseMellow[] = getProjectedScoresAssumingTheWorst(dataModel, 0);
			
			int bonusPoints2 = getNumberOfPointsAvailableWithBidAsDealer(dataModel, 0);
			
			int newOppScore = scoresProjectedWorstCaseMellow[1] - bonusPoints2;
			int newCurScore = scoresProjectedWorstCaseMellow[0] + bonusPoints2;
			
			if(newOppScore < newCurScore) {
				sayMellow = true;
				
			} else if(newOppScore == newCurScore) {
				//Unsure...
				sayMellow = true;
			}
			
		}
		
		return sayMellow;
	}
	
	
	public static int putTotalBidTo14AsDealer(DataModel dataModel) {
		if(dataModel.getDealerIndexAtStartOfRound() == Constants.CURRENT_AGENT_INDEX) {
			
			return Math.max(1, 
					1 + Constants.NUM_STARTING_CARDS_IN_HAND 
					- dataModel.getBid(Constants.LEFT_PLAYER_INDEX)
					- dataModel.getBid(Constants.RIGHT_PLAYER_INDEX)
					- dataModel.getBid(Constants.CURRENT_PARTNER_INDEX));
		} else {
			System.err.println("WARNING: putBidTo14AsDealer function is meant to be asked as dealer only");
			return 5;
		}
	}
	
	public static boolean opponentsDidntSayMellow(DataModel dataModel) {
		if(dataModel.getDealerIndexAtStartOfRound() == Constants.CURRENT_AGENT_INDEX) {
			
			return dataModel.getBid(Constants.LEFT_PLAYER_INDEX) != 0
					&& dataModel.getBid(Constants.RIGHT_PLAYER_INDEX) != 0;
		} else {
			System.err.println("WARNING: opponentsDidntSayMellow function is meant to be asked as dealer only");
			return false;
		}
	}
	
	//TODO: LATER: make a new function where you assume opponents don't bid mellow
	public static int getPossiblyLowerBidBecauseItsNearEndOfGameAssumeWorst(DataModel dataModel, int origBid, int scoresProjectedWorstCase[]) {
		
		if(origBid == 0) {
			//TODO: consider not bidding mellow here.... LATER
			return origBid;
			
			
		} else if(origBid == 1) {
			
			return origBid;
		}
		
		int projectedScoreUs = scoresProjectedWorstCase[0];
		int projectedScoreThem = scoresProjectedWorstCase[1];

		boolean theirBidsAreFlexible = opponentsBidsAreFlexibleAssumingTheWorst(dataModel);
		
		if(projectedScoreUs > projectedScoreThem
				&& projectedScoreUs >= Constants.GOAL_SCORE) {
			
			int curBidToUse = origBid;
			
			int curDiff = projectedScoreUs - projectedScoreThem;
			int curProjectedScoreForUs = projectedScoreUs;
			
			for(int numBidDecrease = 1; origBid - numBidDecrease > 0; numBidDecrease++) {
				
				curDiff -= 10;
				curProjectedScoreForUs -= 10;
				
				if(theirBidsAreFlexible) {
					curDiff -=10;
				} else {
					curDiff -= 1;
				}
				
				if(curDiff > 0 
				&& curProjectedScoreForUs >= Constants.GOAL_SCORE) {
					curBidToUse = origBid - numBidDecrease;
				
					
				} else if(curDiff > 1
						&& curProjectedScoreForUs + 1 ==  Constants.GOAL_SCORE) {
					
					//Bid 1 less, but have the goal of getting 1 over the amount bid:
					//That way, you will have 999 if you make your bid or 1000 if you make 1 extra), (burn or 1009)
					//I lived through making this decision...
					curBidToUse = origBid - numBidDecrease;
				}
			}
			
			return curBidToUse;
			
			
		} else {
			return origBid;
		}
		
	}
	
	public static boolean opponentsBidsAreFlexibleAssumingTheWorst(DataModel dataModel) {

		int dealerIndex = dataModel.getDealerIndexAtStartOfRound();
		boolean rightHandOppDidNotBidMellow = true;
		
		if(dealerIndex != Constants.RIGHT_PLAYER_INDEX) {
			if(dataModel.getBid(Constants.RIGHT_PLAYER_INDEX) > 0) {
				rightHandOppDidNotBidMellow = true;
			}
		}
		
		boolean theirBidsAreFlexible = true;
		
		if(dealerIndex == Constants.CURRENT_AGENT_INDEX) {
			
			//If you're the dealer, the opponents already bid and they can't change their bid:
			theirBidsAreFlexible = false;
			
		} else if(dealerIndex != Constants.RIGHT_PLAYER_INDEX
				&& rightHandOppDidNotBidMellow) {
			
			//If RHS didn't bid mellow, LHS has to bid mellow to compete,
			//so LHS's bid is inflexible.
			theirBidsAreFlexible = false;
			
		}
		
		return theirBidsAreFlexible;
	}
	
	public static int getNumberOfPointsAvailableWithBidAsDealer(DataModel dataModel, int origBid) {
		
		return Math.max(0, 
				Constants.NUM_STARTING_CARDS_IN_HAND
				- dataModel.getBid(Constants.LEFT_PLAYER_INDEX)
				- dataModel.getBid(Constants.CURRENT_PARTNER_INDEX)
				- dataModel.getBid(Constants.RIGHT_PLAYER_INDEX)
				- origBid);
	}
	
public static int getNumberOfPointsAvailableAsBonusIfEveryoneMakesIt(DataModel dataModel) {
		
		return Math.max(0, 
				Constants.NUM_STARTING_CARDS_IN_HAND
				- dataModel.getBid(Constants.LEFT_PLAYER_INDEX)
				- dataModel.getBid(Constants.CURRENT_PARTNER_INDEX)
				- dataModel.getBid(Constants.RIGHT_PLAYER_INDEX)
				- dataModel.getBid(Constants.CURRENT_AGENT_INDEX));
	}

	public static int[] getProjectedScoresAssumingTheWorst(DataModel dataModel, int origBid) {
		return getProjectedScoresAssumingTheWorst(dataModel, origBid, true);
	}
	

	public static int[] getProjectedScoresAssumingTheWorstAndLHSDoesntGoMellow(DataModel dataModel, int origBid) {
		return getProjectedScoresAssumingTheWorst(dataModel, origBid, false);
	}
	
	private static int[] getProjectedScoresAssumingTheWorst(DataModel dataModel, int origBid, boolean lhsCouldSayMellow) {
		

		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "AS QS JS TS 8S 7S 5S 2S TH 7H 2H 6C 3C")) {
			System.out.println("Debug");
		}
		
		int dealerIndex = dataModel.getDealerIndexAtStartOfRound();

		int partnerBid = -1;
		
		if(dealerIndex == Constants.CURRENT_AGENT_INDEX || dealerIndex == Constants.LEFT_PLAYER_INDEX) {
			partnerBid = dataModel.getBid(Constants.CURRENT_PARTNER_INDEX);
		} else {
			partnerBid = 1;
		}
		
		
		int rightHandOppBid = -1;
		
		if(dealerIndex != Constants.RIGHT_PLAYER_INDEX) {
			rightHandOppBid = dataModel.getBid(Constants.RIGHT_PLAYER_INDEX);
		} else {
			rightHandOppBid = Math.max(Constants.NUM_STARTING_CARDS_IN_HAND - partnerBid - origBid, 1);
		}
		
		
		int leftHandOppBid = -1;
		
		if(dealerIndex == Constants.CURRENT_AGENT_INDEX) {
			leftHandOppBid = dataModel.getBid(Constants.LEFT_PLAYER_INDEX);
		
		} else if(lhsCouldSayMellow) {
			if(rightHandOppBid > 0) {
				leftHandOppBid = 0;
			} else {
				leftHandOppBid = Math.max(Constants.NUM_STARTING_CARDS_IN_HAND - partnerBid - origBid - rightHandOppBid, 1);
			}
		
			
		} else {
			//Assume lhs can't say mellow (maybe they can't...)
			leftHandOppBid = Math.max(Constants.NUM_STARTING_CARDS_IN_HAND - partnerBid - origBid - rightHandOppBid, 1);
		}
		
		boolean OUR_TEAM = true;
		int projectedScoreUs = getProjectedScoreForTeamGivenBids(dataModel, OUR_TEAM, origBid, partnerBid);
		
		int projectedScoreThem = getProjectedScoreForTeamGivenBids(dataModel, ! OUR_TEAM, leftHandOppBid, rightHandOppBid);
		
		//Give them the extra points:
		int extraPoints = Math.max(Constants.NUM_STARTING_CARDS_IN_HAND - origBid - partnerBid - leftHandOppBid- rightHandOppBid, 0);
		projectedScoreThem += extraPoints;
		
		
		return new int[] {projectedScoreUs, projectedScoreThem};
	}
	
	public static int getProjectedScoreForTeamGivenBids(DataModel dataModel, boolean isUs, int bid1, int bid2) {
		
		int curScore = -1;
		if(isUs) {
			curScore = dataModel.getOurScore();
		} else {
			curScore = dataModel.getOpponentScore();
		}
		
		int bids[] = new int[] {bid1, bid2};
		
		for(int i=0; i<bids.length; i++) {
			if(bids[i] > 0) {
				curScore += 10 * bids[i];
			} else if(bids[i] == 0) {
				curScore += 100;
			} else {
				System.err.println("Invalid bid in getProjectedScore");
				curScore += 100;
			}
		}
		
		return curScore;
	}
	
	
}

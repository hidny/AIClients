package mellow.ai.situationHandlers.bidding;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.cardUtils.DebugFunctions;

public class BiddingNearEndOfGameFunctions {

	

	public static String getFinalWildDealerBid(DataModel dataModel, int origBid) {
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
	
	public static int[] getProjectedScoresAssumingTheWorst(DataModel dataModel, int origBid) {
		

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
		} else {
			if(rightHandOppBid > 0) {
				leftHandOppBid = 0;
			} else {
				leftHandOppBid = Math.max(Constants.NUM_STARTING_CARDS_IN_HAND - partnerBid - origBid, 1);
			}
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
	

	public static int OLDadjust3rdBidBecauseGameIsCloseToOver(DataModel dataModel, int origCurrentPlayerBid) {
		int ret = origCurrentPlayerBid;
		
	
		int curScore = dataModel.getOurScore();

		int partnerBid = dataModel.getBid(Constants.CURRENT_PARTNER_INDEX);
		int projectedIncreaseOurScore = 0;
		if(partnerBid == 0) {
			projectedIncreaseOurScore += 100;
		} else {
			projectedIncreaseOurScore += 10 * partnerBid;
		}
		
		if(origCurrentPlayerBid == 0) {
			projectedIncreaseOurScore += 100;
		} else {
			projectedIncreaseOurScore += 10 * origCurrentPlayerBid;
		}
		
		int projectedScoreForUs = curScore + projectedIncreaseOurScore;
		
		
		int theirScore = dataModel.getOpponentScore();
		int firstOppBid = dataModel.getBid(Constants.RIGHT_PLAYER_INDEX);
		
		int secondOppGuessBid = 0;
		if(firstOppBid == 0) {
			secondOppGuessBid = 13 - partnerBid - origCurrentPlayerBid;
		}
		
		int projectedIncreaseOppScore = 0;
		
		if(firstOppBid == 0) {
			projectedIncreaseOppScore += 100;
		} else {
			projectedIncreaseOppScore += 10 * firstOppBid;
		}
		
		if(secondOppGuessBid == 0) {
			projectedIncreaseOppScore += 100;
		} else {
			projectedIncreaseOppScore += 10 * secondOppGuessBid;
		}
		
		int projectedOppScore = theirScore + projectedIncreaseOppScore;
		
		int giveEmTheExtraScore = 13 - firstOppBid - secondOppGuessBid - partnerBid - origCurrentPlayerBid;
		projectedOppScore += giveEmTheExtraScore;
		
		if(projectedScoreForUs > 1000 && projectedScoreForUs > projectedOppScore) {
			
			int decreaseInBid = 1;
			for(decreaseInBid = 1; decreaseInBid<origCurrentPlayerBid; decreaseInBid++) {
				if(projectedScoreForUs - 9 * decreaseInBid > 1000
						&& projectedScoreForUs - 9 * decreaseInBid > projectedIncreaseOppScore + 10 * decreaseInBid) {
					//keep going
				} else {
					break;
				}
			}
			
			if(decreaseInBid > 1) {
				System.out.println("(BID LESS 3rd TEST)");
			}
			ret = origCurrentPlayerBid - (decreaseInBid - 1);
			
		}
		
		return ret;
	}
	
	//TODO: do a version of it as 2nd thrower, and 1st thrower.
	
	//TODO: copy var names from prev function and make helper functions!
	//TODO: this is way too rough...
	public static int OLDadjustDealerBidBecauseGameIsCloseToOver(DataModel dataModel, int origBid) {
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

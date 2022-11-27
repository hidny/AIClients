package mellow.ai.situationHandlers.wildSituations;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;

public class NearEndOfGameHelperFunctions {

	//TODO: maybe this function needs to move into another class.
	public static int getMinNumTricksToWinAfterBidsNoMellowBurnIfPossible(DataModel dataModel) {
		int ret = -1;
		
		int numTricks = dataModel.getNumTricksCurTeam();
		
		for(int trial=numTricks; trial<=Constants.NUM_STARTING_CARDS_IN_HAND - dataModel.getNumTricksOtherTeam(); trial++) {
			
			int ourScore = getProjectedScoreForUsAfterWithNTricksAfterBid(dataModel, trial);
			int theirScore = getProjectedScoreForThemAfterWithNTricksAfterBid(dataModel, trial);
			
			if(ourScore >= Constants.GOAL_SCORE
					&& ourScore > theirScore) {
				return trial;
			}
		}
		
		
		return ret;
	}
	public static int getProjectedScoreForUsAfterWithNTricksAfterBid(DataModel dataModel, int numTricks) {
		
		int curScore = dataModel.getOurScore();
		
		
		int bids[] = new int[] {dataModel.getBid(Constants.CURRENT_AGENT_INDEX), dataModel.getBid(Constants.CURRENT_PARTNER_INDEX)};
		
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
		
		if(numTricks > dataModel.getSumBidsCurTeam()) {
			curScore += numTricks - dataModel.getSumBidsCurTeam();

		} else if(numTricks < dataModel.getSumBidsCurTeam()) {
			curScore -= 2 * 10 * dataModel.getSumBidsCurTeam();
		}
		
		return curScore;
	}

	public static int getProjectedScoreForThemAfterWithNTricksAfterBid(DataModel dataModel, int numTricks) {
		
		int curScore = dataModel.getOpponentScore();
		
		
		int bids[] = new int[] {dataModel.getBid(Constants.LEFT_PLAYER_INDEX), dataModel.getBid(Constants.RIGHT_PLAYER_INDEX)};
		
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
		
		if(numTricks > dataModel.getSumBidsOtherTeam()) {
			curScore += numTricks - dataModel.getSumBidsOtherTeam();
	
		} else if(numTricks < dataModel.getSumBidsOtherTeam()) {
			curScore -= 2 * 10 * dataModel.getSumBidsOtherTeam();
		}
		
		return curScore;
	}
	
}

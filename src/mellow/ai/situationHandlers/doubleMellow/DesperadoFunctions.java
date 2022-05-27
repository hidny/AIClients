package mellow.ai.situationHandlers.doubleMellow;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;

public class DesperadoFunctions {


	private final static boolean US = true;
	private final static boolean THEM = false;
	

	public static boolean needToBurnOpponentMellowAtAllCosts(DataModel dataModel) {
		
		//Opponent projected score:
		int projectedOpponentScore = getProjectedScoreNoBonus(dataModel, THEM);
		
		int numTricksBidOpp = dataModel.getBid(Constants.LEFT_PLAYER_INDEX)
							+ dataModel.getBid(Constants.RIGHT_PLAYER_INDEX);
		
		
		
		//Our projected score:
		int projectedScore = getProjectedScoreNoBonus(dataModel, US);
		int numTricksBidUs = dataModel.getBid(Constants.CURRENT_AGENT_INDEX)
				           + dataModel.getBid(Constants.CURRENT_PARTNER_INDEX);
		
		
		// 14-bid edge case:
		if(numTricksBidOpp + numTricksBidUs > Constants.NUM_STARTING_CARDS_IN_HAND) {
			//Shouldn't return false, but this case is too complicated.
			
			//Check if it works out if opponent burns:
			int projectedOpponentScoreAfterBurn = -20 * numTricksBidOpp;
			
			if(projectedOpponentScoreAfterBurn >= Constants.GOAL_SCORE
					&& projectedScore < projectedOpponentScoreAfterBurn) {
				
				System.out.println("DEBUG: Don't go after tricks because if opponent tricks burn, opponent still wins.");
				System.out.println("DEBUG: This is a strange edge-case");
				//There might be 
				return true;
			} else {
				return false;
			}
			
		}
		//END 14-bid edge case
		
		int wiggle = Constants.NUM_STARTING_CARDS_IN_HAND - (numTricksBidOpp + numTricksBidUs);
		
		if(wiggle == 0) {
			//Consider burning tricks???
			//Maybe later
		} else if(wiggle == 1) {
			//Consider burning tricks???
			//Maybe later
			
		}
		
		if(projectedOpponentScore >= Constants.GOAL_SCORE
				&& projectedScore + wiggle < projectedOpponentScore) {

			return true;
		} else if(projectedOpponentScore  + wiggle >= Constants.GOAL_SCORE
				&& projectedScore < projectedOpponentScore + wiggle) {
			//Edge case
			System.out.println("Complicated edge case! Maybe I should do something with it later.");
			return false;
		} else {
			return false;
		}
	}
	
	
	private static int getProjectedScoreNoBonus(DataModel dataModel, boolean us) {
		
		int projectedScore = 0;
		
		boolean indexesToUse[] = new boolean[Constants.NUM_PLAYERS];
		if(us) {
			indexesToUse[Constants.CURRENT_AGENT_INDEX]  = true;
			indexesToUse[Constants.CURRENT_PARTNER_INDEX]  = true;
			
			projectedScore = dataModel.getOurScore();
			
		} else {
			indexesToUse[Constants.LEFT_PLAYER_INDEX]  = true;
			indexesToUse[Constants.RIGHT_PLAYER_INDEX]  = true;
			
			projectedScore = dataModel.getOpponentScore();
		}
		

		int numTricksBid = 0;
		
		for(int i=0; i<indexesToUse.length; i++) {
			if(indexesToUse[i]) {
				
				int bid = dataModel.getBid(i);
				
				if(bid == 0) {
					projectedScore += 100;
				} else {
					numTricksBid += bid;
				}
			}
		}
		
		projectedScore += 10 * numTricksBid;
		
		return projectedScore;
	}
	
	
	//TODO: test!
	// I don't think this condition ever gets called.
	public static boolean wayBehindJustAttackOtherMellow(DataModel dataModel) {
		
		
		//Opponent projected score:
		int projectedOpponentScore = getProjectedScoreNoBonus(dataModel, THEM);
			
		int projectedScore = getProjectedScoreNoBonus(dataModel, US);
		
		if(projectedOpponentScore >= Constants.GOAL_SCORE
				&& projectedScore < projectedOpponentScore - 10) {
			System.out.println("You're way behind, so go for it! 1");
			return true;

		} else if(projectedScore <= projectedOpponentScore) {
			
			int dist = Constants.GOAL_SCORE - projectedOpponentScore;
			
			int dist2 = projectedOpponentScore - projectedScore;
			
			if( 0.8 * (1.0*dist) < (1.0 * dist2)
					&& dist2 > 100) {
				
				System.out.println("You're way behind, so go for it! 2");
				return true;
			}
			
		}
		
		return false;
	}
	
}

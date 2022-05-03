package mellow.ai.simulation.winPercEstimates;

import java.util.HashMap;

import mellow.Constants;

public class ProbWinGetter {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ProbWinGetter test = new ProbWinGetter();
		
		System.out.println("Test all the outputs for this functions:");
		System.out.println("The idea is to make sure it's increasing from up to down and decreasing from left to right.");
		
		double SMALL_NUMBER = 0.0000001;
		int numProbs = 0;
		
		for(int i=ConstantsWP.NEG_LOWER_LIMIT + 1; i<Constants.GOAL_SCORE + 100; i++) {
			for(int j=ConstantsWP.NEG_LOWER_LIMIT + 1; j<Constants.GOAL_SCORE + 100; j++) {
				
				//System.out.println("" + i + ", " + j + "");
				double check = getPercentageWin(i, j);
				
				double checkLessForOpponent = getPercentageWin(i, j - 1);
				double checkLessForDealer = getPercentageWin(i - 1, j);
				
				if(check - SMALL_NUMBER > checkLessForOpponent) {
					System.out.println("Error check left at (i,j) = (" + i + ", " + j + ")  (vs (i, j-1))");
					System.out.println(check + "vs" + checkLessForOpponent);
					
					numProbs++;
				}
				
				if(check + SMALL_NUMBER < checkLessForDealer ) {
					System.out.println("Error check up at (i,j) = (" + i + ", " + j + ")   (vs (i-1, j))");
					System.out.println(check + "vs" + checkLessForDealer);
					numProbs++;
				}
				
				
			}
		}
		
		System.out.println("Done!");
		System.out.println("Number of problems: " + numProbs);
		
	}

	private static double probWinRoughTable[][] = WinPercentageParser.parseWinPerc();
	
	
	private static HashMap<String, Double> cache = new HashMap<String, Double>();
	
	
	public static double getPercentageWin(int scoreDealerTeam, int scoreNotDealerTeam) {
		
		
		double ret = -1.0;
		String key = scoreDealerTeam + "," + scoreNotDealerTeam;
		if(cache.containsKey(scoreDealerTeam + "," + scoreNotDealerTeam)) {
			return cache.get(key);
			
		//Edge case:
		} else if(scoreDealerTeam >= Constants.GOAL_SCORE
				|| scoreNotDealerTeam >= Constants.GOAL_SCORE) {
			
			if(scoreDealerTeam > scoreNotDealerTeam) {
				ret = 1.0;

			} else if(scoreDealerTeam == scoreNotDealerTeam) {
				ret = 0.5;
				
			} else {
				ret = 0.0;
			}

			cache.put(key, ret);
			return ret;
		}
		
		if(cache.size() > 50000) {
			cache = new HashMap<String, Double>();
		}
		
		int topLeftI = (scoreDealerTeam - ConstantsWP.NEG_LOWER_LIMIT) / ConstantsWP.MULT;
		int topLeftJ = (scoreNotDealerTeam - ConstantsWP.NEG_LOWER_LIMIT) / ConstantsWP.MULT;
		
		int scoreTopLeftI = (topLeftI - ConstantsWP.INDEX_0_POINT) * ConstantsWP.MULT;
		int scoreTopLeftJ = (topLeftJ - ConstantsWP.INDEX_0_POINT) * ConstantsWP.MULT;

		
		int scoreTopLeftIPlus1 = scoreTopLeftI + ConstantsWP.MULT;
		if(scoreTopLeftIPlus1 > Constants.GOAL_SCORE) {
			scoreTopLeftIPlus1= ConstantsWP.MAX_SCORE_WITHOUT_WINNING;
		}
		
		int scoreTopLeftJPlus1 = scoreTopLeftJ + ConstantsWP.MULT;
		if(scoreTopLeftJPlus1 > Constants.GOAL_SCORE) {
			scoreTopLeftJPlus1= ConstantsWP.MAX_SCORE_WITHOUT_WINNING;
		}
		
		

		double finalWeightLHSI = getWeightInBetweenTwoElementOfTable(scoreDealerTeam, scoreTopLeftI, scoreTopLeftIPlus1);
		
		double finalWeightLHSJ = getWeightInBetweenTwoElementOfTable(scoreNotDealerTeam, scoreTopLeftJ, scoreTopLeftJPlus1);
		
		ret = finalWeightLHSI       *      finalWeightLHSJ    * probWinRoughTable[topLeftI][topLeftJ]
			+ finalWeightLHSI       *   (1 - finalWeightLHSJ) * probWinRoughTable[topLeftI][topLeftJ + 1]
			+ (1- finalWeightLHSI)  *         finalWeightLHSJ * probWinRoughTable[topLeftI + 1][topLeftJ]
			+ (1- finalWeightLHSI)  *   (1 - finalWeightLHSJ) * probWinRoughTable[topLeftI + 1][topLeftJ + 1];
				
		
		cache.put(key, ret);
		return ret;
	}
	
	//PRE: scoreOnLeft < origTeamScore < highEndScore
	//post: returns the the weight for the index on the left compared to the index on the right
	// the left + right index will always add to 1 (I made it linear)
	private static double getWeightInBetweenTwoElementOfTable(int origTeamScore, int lowEndScore, int highEndScore) {
		
		double finalWeight = 1.0;
		
		if(origTeamScore == lowEndScore) {
			finalWeight = 1.0;
		} else if(origTeamScore == highEndScore) {
			finalWeight = 0.0;
			
		} else {

			double weightLHS = 1.0;
			double weightRHS = 0.0;
			
			int left = lowEndScore;
			int right = highEndScore;
			
			while(true) {

				int curScore = (left + right) / 2;
				
				if(curScore < origTeamScore) {
					left = curScore;
					weightLHS = (weightLHS + weightRHS) / 2;
					finalWeight = weightLHS;
				
				} else if(curScore > origTeamScore) {
					right = curScore;
					weightRHS = (weightLHS + weightRHS) / 2;
					finalWeight = weightRHS;
					
				} else {
					weightRHS = (weightLHS + weightRHS) / 2;
					finalWeight = weightRHS;
					break;
				}
				
			}
			
			if(left > right) {
				System.out.println("oops left > right!");
				System.exit(1);
			}
			
		}
		
		return finalWeight;
	}
}

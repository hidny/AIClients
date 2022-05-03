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

	public static double probWinRoughTable[][] = WinPercentageParser.parseWinPerc();
	
	
	private static HashMap<String, Double> cache = new HashMap<String, Double>();
	
	
	public static double getPercentageWin(int scoreDealer, int scoreNotDealer) {
		
		
		double ret = -1.0;
		String key = scoreDealer + "," + scoreNotDealer;
		if(cache.containsKey(scoreDealer + "," + scoreNotDealer)) {
			return cache.get(key);
			
		//Edge case:
		} else if(scoreDealer >= Constants.GOAL_SCORE
				|| scoreNotDealer >= Constants.GOAL_SCORE) {
			
			if(scoreDealer > scoreNotDealer) {
				ret = 1.0;

			} else if(scoreDealer == scoreNotDealer) {
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
		
		int topLeftI = (scoreDealer - ConstantsWP.NEG_LOWER_LIMIT) / ConstantsWP.MULT;
		int topLeftJ = (scoreNotDealer - ConstantsWP.NEG_LOWER_LIMIT) / ConstantsWP.MULT;
		
		int scoreTopLeftI = (topLeftI - ConstantsWP.INDEX_0_POINT) * ConstantsWP.MULT;
		int scoreTopLeftJ = (topLeftJ - ConstantsWP.INDEX_0_POINT) * ConstantsWP.MULT;

		
		int scoreTopLeftIPlus1 = scoreTopLeftI + ConstantsWP.MULT;
		if(scoreTopLeftIPlus1 > Constants.GOAL_SCORE) {
			scoreTopLeftIPlus1= Constants.GOAL_SCORE - 1;
		}
		
		int scoreTopLeftJPlus1 = scoreTopLeftJ + ConstantsWP.MULT;
		if(scoreTopLeftJPlus1 > Constants.GOAL_SCORE) {
			scoreTopLeftJPlus1= Constants.GOAL_SCORE - 1;
		}
		
		
		//TODO: put Into Function and repeat for J

		
		double finalWeightLHSI = 1.0;
		
		if(scoreDealer == scoreTopLeftI) {
			finalWeightLHSI = 1.0;
		} else if(scoreDealer == scoreTopLeftIPlus1) {
			finalWeightLHSI = 0.0;
			
		} else {

			double weightLHS = 1.0;
			double weightRHS = 0.0;
			
			int left = scoreTopLeftI;
			int right = scoreTopLeftIPlus1;
			
			while(left != right) {

				int curScore = (left + right) / 2;
				
				if(curScore < scoreDealer) {
					left = curScore;
					weightLHS = (weightLHS + weightRHS) / 2;
					finalWeightLHSI = weightLHS;
					
				} else if(curScore > scoreDealer){
					right = curScore;
					weightRHS = (weightLHS + weightRHS) / 2;
					finalWeightLHSI = weightRHS;
					
				} else {
					weightRHS = (weightLHS + weightRHS) / 2;
					finalWeightLHSI = weightRHS;
					break;
					
				}
				
			}
			
			if(left > right) {
				System.out.println("oops!");
			}
			
		}
		//END TODO: put into function
		

		
		//TODO: put Into Function! (Repeated for J)
		double finalWeightLHSJ = 1.0;
		
		if(scoreNotDealer == scoreTopLeftJ) {
			finalWeightLHSJ = 1.0;
		} else if(scoreNotDealer == scoreTopLeftJPlus1) {
			finalWeightLHSJ = 0.0;
			
		} else {

			double weightLHS = 1.0;
			double weightRHS = 0.0;
			
			int left = scoreTopLeftJ;
			int right = scoreTopLeftJPlus1;
			
			while(true) {

				int curScore = (left + right) / 2;
				
				if(curScore < scoreNotDealer) {
					left = curScore;
					weightLHS = (weightLHS + weightRHS) / 2;
					finalWeightLHSJ = weightLHS;
				
				} else if(curScore > scoreNotDealer) {
					right = curScore;
					weightRHS = (weightLHS + weightRHS) / 2;
					finalWeightLHSJ = weightRHS;
					
				} else {
					weightRHS = (weightLHS + weightRHS) / 2;
					finalWeightLHSJ = weightRHS;
					break;
				}
				
			}
			
			if(left > right) {
				System.out.println("oops 2!");
			}
			
		}
		//END TODO: put into function
		
		if(scoreDealer == -510 && scoreNotDealer == -511) {
			System.out.println("Debug 2");
		}

		if(scoreDealer == -511 && scoreNotDealer == -511) {
			System.out.println("Debug 1");
		}
		
		ret = finalWeightLHSI       *      finalWeightLHSJ    * probWinRoughTable[topLeftI][topLeftJ]
			+ finalWeightLHSI       *   (1 - finalWeightLHSJ) * probWinRoughTable[topLeftI][topLeftJ + 1]
			+ (1- finalWeightLHSI)  *         finalWeightLHSJ * probWinRoughTable[topLeftI + 1][topLeftJ]
			+ (1- finalWeightLHSI)  *   (1 - finalWeightLHSJ) * probWinRoughTable[topLeftI + 1][topLeftJ + 1];
				
		
		cache.put(key, ret);
		return ret;
	}
	
	//private double getPercentageWin(int topLeft, int topRight, int )
}

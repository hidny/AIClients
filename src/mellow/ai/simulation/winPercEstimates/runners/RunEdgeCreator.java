package mellow.ai.simulation.winPercEstimates.runners;

import mellow.Constants;
import mellow.ai.simulation.winPercEstimates.ConstantsWP;
import mellow.ai.simulation.winPercEstimates.ProbWinCalculator;

public class RunEdgeCreator {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		get999Border();
	}

	public static void get999Border() {
		
		int NUM_ITERATIONS = 40000;
		
		double tableDealer999[] = new double[Constants.GOAL_SCORE/ConstantsWP.MULT + 1];
		double tableOpponent999[] = new double[Constants.GOAL_SCORE/ConstantsWP.MULT + 1];
		
		//Maybe start the other score at 512 and make it about twice as fast?
		for(int otherScore = 0; otherScore< Constants.GOAL_SCORE; otherScore +=ConstantsWP.MULT) {
			
			tableDealer999[otherScore/ConstantsWP.MULT] = ProbWinCalculator.getProbDealerWinningAndHalfDrawing(ConstantsWP.EGDE_NUMBER, otherScore, NUM_ITERATIONS);
			tableOpponent999[otherScore/ConstantsWP.MULT] = ProbWinCalculator.getProbDealerWinningAndHalfDrawing(otherScore, ConstantsWP.EGDE_NUMBER, NUM_ITERATIONS);
		}

		double double999 = ProbWinCalculator.getProbDealerWinningAndHalfDrawing(ConstantsWP.EGDE_NUMBER, ConstantsWP.EGDE_NUMBER, NUM_ITERATIONS);
		
		System.out.println("Results: ");
		System.out.println("Dealer has " + ConstantsWP.EGDE_NUMBER + " points:");
		
		for(int i=0; i<tableDealer999.length; i++) {
			System.out.print((ConstantsWP.MULT * i) + "                    ".substring(((ConstantsWP.MULT * i) + "").length()) );
		}
		System.out.println();
		for(int i=0; i<tableDealer999.length; i++) {
			System.out.print(tableDealer999[i] + "                    ".substring((tableDealer999[i] + "").length() ));
			
		}
		System.out.println();
		System.out.println();
		
		System.out.println("Opponent has " + ConstantsWP.EGDE_NUMBER + " points:");
		
		for(int i=0; i<tableOpponent999.length; i++) {
			System.out.print((ConstantsWP.MULT * i) + "                    ".substring(((ConstantsWP.MULT * i) + "").length()) );
		}
		System.out.println();
		for(int i=0; i<tableOpponent999.length; i++) {
			System.out.print(tableOpponent999[i] + "                    ".substring((tableOpponent999[i] + "").length() ));
			
		}
		System.out.println();
		System.out.println();
		System.out.println();

		System.out.println("Both teams have " + ConstantsWP.EGDE_NUMBER + " points:");
		System.out.println(double999);
		
	}
	
	

}

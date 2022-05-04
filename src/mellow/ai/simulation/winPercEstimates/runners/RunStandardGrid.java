package mellow.ai.simulation.winPercEstimates.runners;

import mellow.Constants;
import mellow.ai.simulation.winPercEstimates.ConstantsWP;
import mellow.ai.simulation.winPercEstimates.ProbWinCalculator;

public class RunStandardGrid {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		getStandardGrid();
	}

	//This takes a long time...
	// when MULt is 64 and NUM_IT is 10K, it might take 24 hours
	// when MULT is 32 and NUM_IT is 20K, that will takes 8 days?
	
	private static void getStandardGrid() {
		
		//int NUM_ITERATIONS = 20000;
		//Too slow?
		int NUM_ITERATIONS = 20000;
		
		double table[][] = new double[Constants.GOAL_SCORE/ConstantsWP.MULT + 1][Constants.GOAL_SCORE/ConstantsWP.MULT + 1];
		for(int dealer = 0; dealer< Constants.GOAL_SCORE; dealer +=ConstantsWP.MULT) {
			
			for(int opponentTeam = 0; opponentTeam< Constants.GOAL_SCORE; opponentTeam +=ConstantsWP.MULT) {
				
				table[dealer/ConstantsWP.MULT][opponentTeam/ConstantsWP.MULT] = ProbWinCalculator.getProbDealerWinningAndHalfDrawing(dealer, opponentTeam, NUM_ITERATIONS);
			}

		}
		
		System.out.println("Results: ");
		for(int i=0; i<table.length; i++) {
			System.out.println("Dealer has " + (i* ConstantsWP.MULT) + " points:");
			for(int j=0; j<table[0].length; j++) {
				System.out.print(table[i][j] + "                    ".substring((table[i][j] + "").length() ));
			}
			System.out.println();
		}

		System.out.println("--------");
		System.out.println("Results that are slightly more raw:");
		System.out.println();
		System.out.println();
		System.out.println();
		
		for(int i=0; i<table.length; i++) {
			for(int j=0; j<table[0].length; j++) {
				System.out.print(table[i][j] + "                    ".substring((table[i][j] + "").length() ));
			}
			System.out.println();
		}
	}
}

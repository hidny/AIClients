package mellow.ai.simulation.winPercEstimates.runners;

import mellow.ai.simulation.winPercEstimates.ConstantsWP;
import mellow.ai.simulation.winPercEstimates.ProbWinCalculator;

public class RunNegGrid {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		getNegGrid();
	}


	private static void getNegGrid() {
		
		System.out.println("Get Neg Grid:");
		int NUM_ITERATIONS = 5000;
		//Too slow:
		//int NUM_ITERATIONS = 10000;
		
		double table[][] = new double[ConstantsWP.NEG_GRID_DIM_LENGTH][ConstantsWP.NEG_GRID_DIM_LENGTH];
		for(int dealer = ConstantsWP.NEG_LOWER_LIMIT, i=0; i< table.length; dealer +=ConstantsWP.MULT, i++) {
			
			for(int opponentTeam = ConstantsWP.NEG_LOWER_LIMIT, j=0; j<table[0].length; opponentTeam +=ConstantsWP.MULT, j++) {
				
				if(dealer >=0 && opponentTeam >=0) {
					table[i][j] = -1.0;
				} else {
					table[i][j] = ProbWinCalculator.getProbDealerWinningAndHalfDrawing(dealer, opponentTeam, NUM_ITERATIONS);
				}
			}

		}
		
		System.out.println("Results: ");
		for(int i=0; i<table.length; i++) {
			System.out.println("Dealer has " + (ConstantsWP.NEG_LOWER_LIMIT + i* ConstantsWP.MULT) + " points:");
			for(int j=0; j<table[0].length; j++) {
				System.out.print(table[i][j] + "                    ".substring((table[i][j] + "").length() ));
			}
			System.out.println();
		}

		System.out.println("--------");
		System.out.println("Results that are slightly more raw:");
		System.out.println("Manually copy results into " + ConstantsWP.folderName + "\\" + ConstantsWP.testCaseFileNegRange);
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

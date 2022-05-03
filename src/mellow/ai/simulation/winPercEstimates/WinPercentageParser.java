package mellow.ai.simulation.winPercEstimates;

import java.io.File;
import java.util.Scanner;

import mellow.Constants;

public class WinPercentageParser {

	public static void main(String[] args) {
		
		parseWinPerc();
	}

	public static int POSITIVE_MULT = 64;
	
	//TODO: make helper methods
	public static double[][] parseWinPerc() {
		
		System.out.println("Parsing raw files to create a win percentage table based on the scores between rounds:");
		
		int normalSize = Constants.GOAL_SCORE / 64 + 1;
		double normalTable[][] = new double[normalSize][normalSize];
		
		Scanner NormalRange = null;
		Scanner NegRange = null;
		Scanner EdgeRange = null;
		
		double fullTable[][] = null;
		
		try {
			NormalRange= new Scanner(new File(ConstantsWP.testCaseFileNormalRange));
			NegRange= new Scanner(new File(ConstantsWP.testCaseFileNegRange));
			EdgeRange = new Scanner(new File(ConstantsWP.testCaseFileEdgeRange));
			
			
			int curI = 0;
			while(NormalRange.hasNextLine()) {
				String line = NormalRange.nextLine();
				
				Scanner tmp = new Scanner(line);
				
				int curJ = 0;
				while(tmp.hasNextDouble()) {
					
					normalTable[curI][curJ] = tmp.nextDouble();
					System.out.print(normalTable[curI][curJ] + "\t");
					curJ++;
				}
				System.out.println();
				
				if(curJ != normalTable[0].length) {
					System.out.println("WARNING: lenth of j incorrect");
				}
				
				curI++;
			}
			if(curI != normalTable.length) {
				System.out.println("WARNING: length of i incorrect");
			}
			
			System.out.println();
			System.out.println();
			System.out.println();
			
			//TODO: this is copy/paste code! Fix it!
			double tableNeg[][] = new double[ConstantsWP.NEG_GRID_DIM_LENGTH][ConstantsWP.NEG_GRID_DIM_LENGTH];
			
			curI = 0;
			while(NegRange.hasNextLine()) {
				String line = NegRange.nextLine();
				
				Scanner tmp = new Scanner(line);
				
				int curJ = 0;
				while(tmp.hasNextDouble()) {
					
					tableNeg[curI][curJ] = tmp.nextDouble();
					System.out.println(tableNeg[curI][curJ]);
					curJ++;
				}
				System.out.println();
				
				if(curJ != tableNeg[0].length) {
					System.out.println("WARNING: lenth of j incorrect (neg)");
				}
				
				curI++;
			}
			if(curI != tableNeg.length) {
				System.out.println("WARNING: length of i incorrect (neg)");
			}
			
			//END TODO: this is copy/paste code! Fix it!
			
			//Scanner edge (999 points)
			
			double egdeTable1[] = new double[normalSize];
			
			curI = 0;
			if(EdgeRange.hasNextLine()) {
				String line = EdgeRange.nextLine();
				
				Scanner tmp = new Scanner(line);
				
				int curJ = 0;
				while(tmp.hasNextDouble()) {
					
					egdeTable1[curJ] = tmp.nextDouble();
					System.out.println(egdeTable1[curJ]);
					curJ++;
				}
				System.out.println();
				System.out.println();
				
			}
			
			//TODO: copy/paste code
			double egdeTable2[] = new double[normalSize];
			curI = 0;
			if(EdgeRange.hasNextLine()) {
				String line = EdgeRange.nextLine();
				
				Scanner tmp = new Scanner(line);
				
				int curJ = 0;
				while(tmp.hasNextDouble()) {
					
					egdeTable2[curJ] = tmp.nextDouble();
					System.out.println(egdeTable2[curJ]);
					curJ++;
				}
				System.out.println();
				System.out.println();
				
			}
			
			double corner = -1.0;
			if(EdgeRange.hasNextLine()) {
				String line = EdgeRange.nextLine();
				
				Scanner tmp = new Scanner(line);
				
				if(tmp.hasNextDouble()) {
					corner = tmp.nextDouble();
				}
			}

			//END TODO: copy/paste code
			
			
			//Create the big table:
			
			//Add 2 because:
			// both ends are counted, and a bonus edgeis counted:
			int size = (Constants.GOAL_SCORE - ConstantsWP.NEG_LOWER_LIMIT) / ConstantsWP.MULT + 2;
			fullTable = new double[size][size];
			
			
			for(int i=0; i<tableNeg.length; i++) {
				for(int j=0; j<tableNeg[0].length; j++) {
					
					fullTable[i][j] = tableNeg[i][j];
					
				}
			}

			
			
			for(int i=0; i<normalTable.length; i++) {
				for(int j=0; j<normalTable[0].length; j++) {
					
					
					fullTable[ConstantsWP.INDEX_0_POINT + i][ConstantsWP.INDEX_0_POINT + j] = normalTable[i][j];
				}
			}

			for(int i=0; i<egdeTable1.length; i++) {

				fullTable[fullTable.length - 1][ConstantsWP.INDEX_0_POINT + i] = egdeTable1[i];
			}
			
			for(int i=0; i<egdeTable2.length; i++) {

				fullTable[ConstantsWP.INDEX_0_POINT + i][fullTable[0].length - 1] = egdeTable2[i];
			}
			
			
			fullTable[fullTable.length - 1][fullTable[0].length - 1] = corner;
			
			
			//Fix the corners that I didn't bother to touch
			for(int i=0; i<fullTable.length; i++) {
				for(int j=0; j< fullTable[0].length; j++) {
					if(fullTable[i][j] == 0.0
							&& i > j) {
						fullTable[i][j] = 1.0;
					}
				}
			}
			
			
			System.out.println("Print it:");
			
			for(int i=0; i<fullTable.length; i++) {
				for(int j=0; j< fullTable[0].length; j++) {
					System.out.print(fullTable[i][j] + "\t");
				}
				System.out.println();
			}
			
		} catch (Exception e) {
			System.err.println("Exception thrown!");
			
			e.printStackTrace();
			
		} finally {
			NormalRange.close();
			NegRange.close();
			EdgeRange.close();
			
		}
		return cleanTable(fullTable);
	}
	
	//Hack to make sure that the table is increasing from up to down
	// and decreasing from left to right.
	// The trick is to do a bubble sort. It's pretty dishonorable, but it works.
	public static double[][] cleanTable(double fullTable[][]) {
		

		boolean swapped = false;
		do {
			
			swapped = false;
			System.out.println("Loop");
			for(int i=0; i<fullTable.length; i++) {
				for(int j=0; j<fullTable[0].length; j++) {
					
					if(j < fullTable[0].length - 1 && fullTable[i][j] < fullTable[i][j + 1]) {
						double tmp = fullTable[i][j];
						fullTable[i][j] = fullTable[i][j + 1];
						fullTable[i][j + 1] = tmp;
						swapped = true;
						System.out.println("Warning: Modified table to make it decreasing side to side.");
					}
					
					if(i < fullTable.length - 1 && fullTable[i][j] > fullTable[i+1][j]) {
						double tmp = fullTable[i][j];
						fullTable[i][j] = fullTable[i + 1][j];
						fullTable[i + 1][j] = tmp;
						swapped = true;
						System.out.println("Warning: Modified table to make it increasing up to down.");
					}
				}
				
			}
		} while(swapped);
		
		
		System.out.println("Print it again:");
		
		for(int i=0; i<fullTable.length; i++) {
			for(int j=0; j< fullTable[0].length; j++) {
				System.out.print(fullTable[i][j] + "\t");
			}
			System.out.println();
		}
		
		return fullTable;
	}
	
	
}

package mellow.ai.simulation.winPercEstimates;

import java.io.File;
import java.util.Scanner;

public class WinPercentageParser {

	public static void main(String[] args) {
		
		parseWinPerc();
	}

	
	public static double[][] parseWinPerc() {
		
		System.out.println("Parsing raw files to create a win percentage table based on the scores between rounds:");
		
		
		
		Scanner NormalRange = null;
		Scanner NegRange = null;
		Scanner EdgeRange = null;
		
		double fullTable[][] = null;
		
		try {
			NormalRange= new Scanner(new File(ConstantsWP.testCaseFileNormalRange));
			
			double normalTable[][] = scanFileIntoTable(NormalRange,
					ConstantsWP.POS_GRID_DIM_LENGTH,
					ConstantsWP.POS_GRID_DIM_LENGTH,
					"Normal Grid");
			
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
				

				tmp.close();
			}
			if(curI != normalTable.length) {
				System.out.println("WARNING: length of i incorrect");
			}
			
			System.out.println();
			System.out.println();
			System.out.println();

			NegRange= new Scanner(new File(ConstantsWP.testCaseFileNegRange));
			double tableNeg[][] = scanFileIntoTable(NegRange,
					ConstantsWP.NEG_GRID_DIM_LENGTH,
					ConstantsWP.NEG_GRID_DIM_LENGTH,
					"Neg Grid");
			
			//Scanner edge (cases where one team has 999 points)

			EdgeRange = new Scanner(new File(ConstantsWP.testCaseFileEdgeRange));
			
			double egdeTable1[] = scanLineRepEdgeOfTable(EdgeRange, ConstantsWP.POS_GRID_DIM_LENGTH);
			
			
			double egdeTable2[] = scanLineRepEdgeOfTable(EdgeRange, ConstantsWP.POS_GRID_DIM_LENGTH);
			

			//Get the corner value (when the scores are 999 vs 999)
			double corner = -1.0;
			if(EdgeRange.hasNextLine()) {
				String line = EdgeRange.nextLine();
				
				Scanner tmp = new Scanner(line);
				
				if(tmp.hasNextDouble()) {
					corner = tmp.nextDouble();
				}
				tmp.close();
			}
			
			//Create the big table:
			
			fullTable = new double[ConstantsWP.FULL_SIZE_WP_TABLE][ConstantsWP.FULL_SIZE_WP_TABLE];
			
			//Fill in the neg table values:
			for(int i=0; i<tableNeg.length; i++) {
				for(int j=0; j<tableNeg[0].length; j++) {
					fullTable[i][j] = tableNeg[i][j];
				}
			}

			//Fill in the positive/normal table values:
			for(int i=0; i<normalTable.length; i++) {
				for(int j=0; j<normalTable[0].length; j++) {
					fullTable[ConstantsWP.INDEX_0_POINT + i][ConstantsWP.INDEX_0_POINT + j] = normalTable[i][j];
				}
			}

			//Fill in the edge (999 point values)
			for(int i=0; i<egdeTable1.length; i++) {
				fullTable[fullTable.length - 1][ConstantsWP.INDEX_0_POINT + i] = egdeTable1[i];
			}
			
			for(int i=0; i<egdeTable2.length; i++) {
				fullTable[ConstantsWP.INDEX_0_POINT + i][fullTable[0].length - 1] = egdeTable2[i];
			}
			
			fullTable[fullTable.length - 1][fullTable[0].length - 1] = corner;
			
			
			
			fullTable = fixCornersOfTable(fullTable);
			fullTable = cleanTable(fullTable);
			
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
		return fullTable;
	}
	
	public static double[][] scanFileIntoTable(Scanner in, int numLines, int numCols, String debug) {
		double ret[][] = new double[numLines][numCols];
		
		int curI = 0;
		while(in.hasNextLine()) {
			String line = in.nextLine();
			
			Scanner tmp = new Scanner(line);
			
			int curJ = 0;
			while(tmp.hasNextDouble()) {
				
				ret[curI][curJ] = tmp.nextDouble();
				System.out.println(ret[curI][curJ]);
				curJ++;
			}
			System.out.println();
			
			if(curJ != ret[0].length) {
				System.out.println("WARNING: lenth of j incorrect ( " + debug + " )");
			}
			
			curI++;

			tmp.close();
		}
		if(curI != ret.length) {
			System.out.println("WARNING: length of i incorrect ( " + debug + " )");
		}
		
		return ret;
	}
	
	public static double[] scanLineRepEdgeOfTable(Scanner in, int numElements) {

		double egdeTable2[] = new double[numElements];
		if(in.hasNextLine()) {
			String line = in.nextLine();
			
			Scanner tmp = new Scanner(line);
			
			int curJ = 0;
			while(tmp.hasNextDouble()) {
				
				egdeTable2[curJ] = tmp.nextDouble();
				System.out.println(egdeTable2[curJ]);
				curJ++;
			}
			System.out.println();
			System.out.println();
			
			tmp.close();
			
		}
		
		
		return egdeTable2;
	}
	
	public static double[][] fixCornersOfTable(double fullTable[][]) {
		
		//Fix the top left and top right corners that I didn't bother to touch
		//(They are either all 0.0 or all 1.0)
		//I only modified the bottom left because top right defaults to 0.
		for(int i=0; i<fullTable.length; i++) {
			for(int j=0; j< fullTable[0].length; j++) {
				if(fullTable[i][j] == 0.0
						&& i > j) {
					fullTable[i][j] = 1.0;
				}
			}
		}
		
		return fullTable;
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

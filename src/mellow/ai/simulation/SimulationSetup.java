package mellow.ai.simulation;

import mellow.Constants;
import mellow.ai.cardDataModels.impl.BooleanTableDataModel;

public class SimulationSetup {

	
	public static long getNumberOfWaysToSimulate(int numUnknownCardsPerSuit[], int numSpacesAvailPerPlayer[], boolean originalIsVoidList[][], int playerList[]) {
		return getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, playerList, 0);
	}
	
	public static long getNumberOfWaysToSimulate(int numUnknownCardsPerSuit[], int numSpacesAvailPerPlayer[], boolean originalIsVoidList[][], int playerList[], int depth) {

		boolean voids[][] = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		int numVoids[] = new int[Constants.NUM_PLAYERS];
		
		for(int i=0; i<voids.length; i++) {
			for(int j=0; j<voids[0].length; j++) {
				
				if(originalIsVoidList[i][j] || numUnknownCardsPerSuit[j] == 0) {
					voids[i][j] = true;
					numVoids[i]++;
				}
			}
		}
		
		long ret = 0L;
		
		//84478098072866400
		
		int numTrumpSeperators = Constants.NUM_SUITS - 1 - numVoids[playerList[depth]];
		//player 1 suit combos:
		boolean combo[] = new boolean[numSpacesAvailPerPlayer[playerList[depth]] + numTrumpSeperators];
		for(int i=0; i<combo.length; i++) {
			if(i < numTrumpSeperators) {
				combo[i] = true;
			} else {
				combo[i] = false;
			}
		}
		
		COMBO:
		while(combo != null) {
			int testSuitArray[] = convertComboToArray(combo, Constants.NUM_SUITS - numVoids[playerList[depth]]);
			
			int suitsTakenByPlayer[] = new int[Constants.NUM_SUITS];
			int numCardsPerSuitAfterTake[] = new int[Constants.NUM_SUITS];
			
			for(int i=0, j=0; i<Constants.NUM_SUITS; i++) {
				if(voids[playerList[depth]][i] == false) {
					suitsTakenByPlayer[i] = testSuitArray[j];
					j++;
				} else {
					suitsTakenByPlayer[i] = 0;
				}
			
			}
			
			for(int i=0; i<4; i++) {
				numCardsPerSuitAfterTake[i] = numUnknownCardsPerSuit[i] - suitsTakenByPlayer[i];
			}
			
			//Shortcut to skip impossible cases:
			//(Make sure that:
			// 1) there's 0 or more of each suit left 
			// and 
			// 2) current player didn't take more cards of a suit than was available
			// (Note: the second condition is handled by combination formula, but whatever)
			for(int i=0; i<Constants.NUM_SUITS; i++) {
				if(numCardsPerSuitAfterTake[i] < 0 || suitsTakenByPlayer[i] > numUnknownCardsPerSuit[i]) {
					combo = getNextCombination(combo);
					continue COMBO;
				}
			}
			
			long numWaysToSetupPlayerWithSuitPartition = 1;
			for(int i=0; i<Constants.NUM_SUITS; i++) {
				numWaysToSetupPlayerWithSuitPartition *= getCombination(numUnknownCardsPerSuit[i], suitsTakenByPlayer[i]);
			}
			
			long numWaysToSetupAllPlayersWithSuitPartitions = numWaysToSetupPlayerWithSuitPartition;

			if(depth + 2 < playerList.length && numWaysToSetupAllPlayersWithSuitPartitions > 0) {
				numWaysToSetupAllPlayersWithSuitPartitions *= getNumberOfWaysToSimulate(numCardsPerSuitAfterTake, numSpacesAvailPerPlayer, originalIsVoidList, playerList, depth + 1);
				
			} else if(depth + 2 == playerList.length) {
				//do nothing (Last player has only 1 way to pick up remaining cards)
			}
			ret += numWaysToSetupAllPlayersWithSuitPartitions;
			
			combo = getNextCombination(combo);
		}
		
		return ret;
		
	}
	
	
	
	public static int[] convertComboToArray(boolean combo[], int size) {
		int ret[] = new int[size];
		
		int currentIndex = 0;
		int currentSize = 0;
		for(int i=0; i<combo.length; i++) {
			if(combo[i]) {
				ret[currentIndex] = currentSize;
				currentIndex++;
				currentSize = 0;
			} else {
				currentSize++;
			}
		}
		
		ret[currentIndex] = currentSize;
		
		return ret;
	}
	
	//Copied from the euler project:
	//get the next combonation of true values given current array
	public static boolean[] getNextCombination(boolean current[]) {
		/*Example: series of executions:
		 *  11000
			10100
			10010
			10001
			01100
			01010
			01001
			00110
			00101
			00011
		 */
		//we know that we are going to readjust at least 1 element:
		int numToReadjust = 1;
		
		boolean foundSpaceToFill = false;
		
		int spaceToFill;
		
		//this loops counts the number of elements we have to readjust
		//and finds out if there exist a space to fill.
		for(spaceToFill=current.length - 1; spaceToFill>=0; spaceToFill--) {
			if(current[spaceToFill] == false) {
				foundSpaceToFill = true;
				break;
			} else {
				numToReadjust++;
			}
		}
		
		if(foundSpaceToFill) {
			//Find the rightmost 1 that we will have to move to the right:
			int indexToMove;
			for(indexToMove = spaceToFill-1; indexToMove>=0; indexToMove--) {
				if(current[indexToMove] == true) {
					break;
				}
			}
			
			if(indexToMove>=0) {
				current[indexToMove] = false;
				//goal:
				//got from: 00010111
				//to        00001111
				int startInput1s = indexToMove+1;
				int stopInput1s = startInput1s + numToReadjust;
				for(int i=startInput1s; i<stopInput1s; i++) {
					current[i] = true;
				}
				//input 0s for the rest:
				for(int i=stopInput1s; i<current.length; i++) {
					current[i] = false;
				}
				
			} else {
				//This should only happen is we've gone through all of the combinations
				//or there is no element in current that is true.
				return null;
			}
			
		} else {
			//This should only happen if current[] is filled with only true.
			return null;
		}
		
		return current;
	}

	public static long triangle[][] = null;
	
	public static long getCombination(int m, int n) {
		if(triangle == null) {
			triangle = createPascalTriangle(Constants.NUM_CARDS + 1);
		}
		
		if(m > triangle.length) {
			System.err.println("ERROR: m is too big for pascal's triangle");
			System.exit(1);
		}

		if(m >= n && n>=0 && m < triangle.length) {
			return triangle[m][n];
		} else {
			return 0;
		}
	}


	//From project euler:
	private static long[][] createPascalTriangle(int size) {
		size = size+1;
		long pascalTriangle[][] = new long[size][size];
		
		for(int i=0; i<size; i++) {
			for(int j=0; j<size; j++) {
				pascalTriangle[i][j] = 0;
			}
		}
		
		pascalTriangle[0][0] = 1;
				
		for(int i=1; i<size; i++) {
			for(int j=0; j<size; j++) {
				pascalTriangle[i][j] = pascalTriangle[i-1][j];
				if(j>0) {
					pascalTriangle[i][j] += pascalTriangle[i-1][j-1];
				}
			}
		}
		
		return pascalTriangle;
	}
	
	public static long[][] createPascalTriangle(int size, long modulo) {
		size = size+1;
		long pascalTriangle[][] = new long[size][size];
		
		for(int i=0; i<size; i++) {
			for(int j=0; j<size; j++) {
				pascalTriangle[i][j] = 0;
			}
		}
		
		pascalTriangle[0][0] = 1;
				
		for(int i=1; i<size; i++) {
			for(int j=0; j<size; j++) {
				pascalTriangle[i][j] = pascalTriangle[i-1][j];
				if(j>0) {
					pascalTriangle[i][j] = (pascalTriangle[i][j] + pascalTriangle[i-1][j-1]) % modulo;
				}
			}
		}
		
		return pascalTriangle;
	}


}

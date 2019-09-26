package mellow.ai.simulation;

import java.util.Random;

import mellow.Constants;
import mellow.cardUtils.CardStringFunctions;
import mellow.ai.simulation.SelectedPartitionAndIndex;

public class SimulationSetup {

	//TODO: playerList and numSpacesAvailPerPlayer arrays assume there's an unused player 0... this is ugly... Oh well

	//TODO: If getNumberOfWaysToSimulate < N, then make it deterministic because why not?

	public static Random random = new Random();
	
	public static String[][] distributeUnknownCards(String unknownCards[], int numSpacesAvailPerPlayer[], boolean originalIsVoidList[][], int playerList[]) {
		
		long numWays = getNumberOfWaysToSimulate(getNumUnknownPerSuit(unknownCards), numSpacesAvailPerPlayer, originalIsVoidList, playerList);
		
		long randIndexNumber = getRandNumberFrom0ToN(numWays);
		
		System.out.println("randIndexNumber: " + randIndexNumber);
		
		SelectedPartitionAndIndex suitPartitionsAndComboNumbers = getSelectedPartitionAndIndexBasedOnRandIndex(
								getNumUnknownPerSuit(unknownCards), numSpacesAvailPerPlayer, originalIsVoidList, playerList, randIndexNumber);
		
		String ret[][] = serveCarsdsBasedOnPartitionAndIndexInfo(playerList, suitPartitionsAndComboNumbers, unknownCards, numSpacesAvailPerPlayer);
		
		return ret;
	}

	public static long getRandNumberFrom0ToN(long numWays) {
		//The do while loop is to remove bias.
		//Without it, the bias could be high if the number of ways is high...
		//For example, if we consider simulating everyone's hand at the beginning of the round we have: (39 choose 13) * (26 choose 13) ways.
		//compared to 2^64 possible longs.
		//Because:
		//2^64/((39 choose 13) * (26 choose 13)) = 218.36..
		//This means some numbers come up 219/218 times more than others or about 0.5% more likely.
		//I decided to fix this... by retrying if the random number is too extreme.
		
		//System.out.println("numWays: " + numWays);
		long tmp;
		long randIndexNumber;
		long numLoops = 0;
		
		do {
			tmp = random.nextLong();
			randIndexNumber = tmp % numWays;
			
			if(randIndexNumber <0 ) {
				randIndexNumber += numWays;
				//System.out.println("Test negative random.");
			}
			
			numLoops++;
			if(numLoops == 2) {
				//System.out.println("FOUND EGDE CASE!");
			}
			
		//Condition that hopefully removes bias on the extremes:
		} while((Long.MAX_VALUE - (Long.MAX_VALUE% numWays) ) <= tmp || (Long.MIN_VALUE + numWays  + (Long.MIN_VALUE% numWays) -1 )>= tmp);
		
		return randIndexNumber;
	}
	
	
	public static SelectedPartitionAndIndex getSelectedPartitionAndIndexBasedOnRandIndex(int numUnknownCardsPerSuit[], int numSpacesAvailPerPlayer[], boolean originalIsVoidList[][], int playerList[], long randIndexNumber) {
		return getSelectedPartitionAndIndex(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, playerList, randIndexNumber, 0, new SelectedPartitionAndIndex());
	}
	//Lots of copy/paste code... oh well
	public static SelectedPartitionAndIndex getSelectedPartitionAndIndex(int numUnknownCardsPerSuit[], int numSpacesAvailPerPlayer[], boolean originalIsVoidList[][], int playerList[], long randIndexNumber, int depth, SelectedPartitionAndIndex selectedPartitionAndIndexToFillIn) {

		//TODO: put into function
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
		//END TODO: put into function
		
		long prevNumCombosSkippedThru = 0L;
		long numCombosSkippedThru = 0L;
		
		//84478098072866400
		
		//TODO: put into function
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
		//END TODO: put into function
		
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
			//(This makes sure that:
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
			
			numCombosSkippedThru += numWaysToSetupAllPlayersWithSuitPartitions;
			
			if(numCombosSkippedThru > randIndexNumber) {
				//TODO: maybe break immediately?
				
				selectedPartitionAndIndexToFillIn.setSuitsTakenByPlayers(playerList[depth], suitsTakenByPlayer);
				
				long numWays = getNumberOfWaysToSimulate(numCardsPerSuitAfterTake, numSpacesAvailPerPlayer, originalIsVoidList, playerList, depth + 1);
				long indexFromStartOfCombo = (randIndexNumber - prevNumCombosSkippedThru);
				long currentPlayerComboNumber;
				
				//If depth + 2 == playerList.length, we are just dividing by 1 which does nothing.
				if(depth + 2 < playerList.length) {
					currentPlayerComboNumber = indexFromStartOfCombo / numWays;
					selectedPartitionAndIndexToFillIn.setPlayerComboNumber(playerList[depth], currentPlayerComboNumber);
					
					long nextRandomIndexNumber = indexFromStartOfCombo % numWays;
					return getSelectedPartitionAndIndex(numCardsPerSuitAfterTake, numSpacesAvailPerPlayer, originalIsVoidList, playerList, nextRandomIndexNumber, depth + 1, selectedPartitionAndIndexToFillIn);

				} else {
					currentPlayerComboNumber = indexFromStartOfCombo;
					selectedPartitionAndIndexToFillIn.setPlayerComboNumber(playerList[depth], currentPlayerComboNumber);
					
					selectedPartitionAndIndexToFillIn.giveWhatsLeftToNextPlayer(playerList[depth + 1], numCardsPerSuitAfterTake);
					
					return selectedPartitionAndIndexToFillIn;
				}
				
			}
			
			prevNumCombosSkippedThru = numCombosSkippedThru;
			
			combo = getNextCombination(combo);
		}
		
		if(numCombosSkippedThru <= randIndexNumber) {
			System.err.println("ERROR: something went wrong in getSelectedPartitionAndIndex randIndexNumber is too big");
			System.exit(1);
		}

		return null;
		
		
	}
	
	public static int[] getNumUnknownPerSuit(String unknownCards[]) {
		int numUnknownCardsPerSuit[] = new int[Constants.NUM_SUITS];
		
		for(int i=0; i<numUnknownCardsPerSuit.length; i++) {
			numUnknownCardsPerSuit[i] = 0;
		}
		
		for(int i=0; i<unknownCards.length; i++) {
			numUnknownCardsPerSuit[CardStringFunctions.getIndexOfSuit(unknownCards[i])]++;
		}
		
		return numUnknownCardsPerSuit;
	}
	
	public static long getNumberOfWaysToSimulate(int numUnknownCardsPerSuit[], int numSpacesAvailPerPlayer[], boolean originalIsVoidList[][], int playerList[]) {
		return getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, playerList, 0);
	}
	
	public static long getNumberOfWaysToSimulate(int numUnknownCardsPerSuit[], int numSpacesAvailPerPlayer[], boolean originalIsVoidList[][], int playerList[], int depth) {

		boolean voids[][] = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		int numVoids[] = new int[Constants.NUM_PLAYERS];
		//TODO: put into function
		for(int i=0; i<voids.length; i++) {
			for(int j=0; j<voids[0].length; j++) {
				
				if(originalIsVoidList[i][j] || numUnknownCardsPerSuit[j] == 0) {
					voids[i][j] = true;
					numVoids[i]++;
				}
			}
		}
		//END TODO: put into function

		if(numSpacesAvailPerPlayer[playerList[depth]] == 0) {
			return 1;
		}
		
		long ret = 0L;
		
		//84478098072866400
		
		//TODO: put into function
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
		//END TODO: put into function
		
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
			//(This makes sure that:
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
			//END shortcut
			
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

	private static long triangle[][] = null;
	
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

	
	private static String TAKEN = null;
	
	//TODO: OMG TEST more!
	public static String[][] serveCarsdsBasedOnPartitionAndIndexInfo(int playerList[], SelectedPartitionAndIndex selectedSuitsAndCombos, String unknownCards[], int numSpacesAvailPerPlayer[]) {
		String unknownCardsPerSuit[][] = new String[Constants.NUM_SUITS][];
		
		int curNumUnknownCardsPerSuit[] = SimulationSetup.getNumUnknownPerSuit(unknownCards);
		int currentIndexPerSuit[] = new int[Constants.NUM_SUITS];
		
		for(int i=0; i<Constants.NUM_SUITS; i++) {
			unknownCardsPerSuit[i] = new String[curNumUnknownCardsPerSuit[i]];
			currentIndexPerSuit[i] = 0;
		}
		
		for(int i=0; i<unknownCards.length; i++) {
			int indexSuit = CardStringFunctions.getIndexOfSuit(unknownCards[i]);
			unknownCardsPerSuit[indexSuit][currentIndexPerSuit[indexSuit]] = unknownCards[i];
			currentIndexPerSuit[indexSuit]++;
		}
		
		
		String unknownCardDistPerPlayer[][] = new String[Constants.NUM_PLAYERS][];
		
		for(int playerI=0; playerI<playerList.length; playerI++) {
			
			int curCardsTakenByPlayer=0;
			
			unknownCardDistPerPlayer[playerList[playerI]] = new String[numSpacesAvailPerPlayer[playerList[playerI]]];
			
			long currentComboNum = selectedSuitsAndCombos.comboIndex[playerList[playerI]];
			
			//Work backwards from suit list and give player the cards:
			for(int suit=Constants.NUM_SUITS - 1; suit>=0; suit--) {
				
				int numCardsPlayerWillTake = selectedSuitsAndCombos.suitsTakenByPlayer[playerList[playerI]][suit];
				
				long numWaysToSetupSuitForPlayer = SimulationSetup.getCombination(curNumUnknownCardsPerSuit[suit], numCardsPlayerWillTake);
				int comboNumberForSuit = (int)( currentComboNum % numWaysToSetupSuitForPlayer);
				
				boolean combo[] = convertComboNumberToArray(curNumUnknownCardsPerSuit[suit], numCardsPlayerWillTake, comboNumberForSuit);
				
				for(int i=0, j=0; i<unknownCardsPerSuit[suit].length && j<combo.length; i++) {
					if(unknownCardsPerSuit[suit][i] != TAKEN) {
						if(combo[j]) {
							//System.out.println("Space avail for player " + playerList[playerI] + ": " + numSpacesAvailPerPlayer[playerList[playerI]] +   " curCardsTakenByPlayer: " + curCardsTakenByPlayer);
							unknownCardDistPerPlayer[playerList[playerI]][curCardsTakenByPlayer] =
									unknownCardsPerSuit[suit][i];
							
							curCardsTakenByPlayer++;
							unknownCardsPerSuit[suit][i] = TAKEN;
							curNumUnknownCardsPerSuit[suit]--;
						}
						j++;
					}
				}
				
				currentComboNum /= numWaysToSetupSuitForPlayer;
			}
			
			if(currentComboNum != 0) {
				System.err.println("ERROR: Distributing the cards messed up and didn\'t end up with correct combo num");
				System.exit(1);
			}
			
			if(curCardsTakenByPlayer != unknownCardDistPerPlayer[playerList[playerI]].length) {
				System.out.println("ERROR: Distributing the cards messed up.");
				System.exit(1);
			}
		}
		
		
		return unknownCardDistPerPlayer;
	}
	

	
	//For convert combo num and num remaining cards into a boolean array to indicate which cards to take
	public static boolean[] convertComboNumberToArray(int numUnknownCardsInSuit, int numCardsToTake, int comboNumber) {

		if(numUnknownCardsInSuit < numCardsToTake) {
			System.err.println("ERROR: trying to create impossible combo in convertComboNumberToArray");
			System.exit(1);
		}

		boolean ret[] = new boolean[numUnknownCardsInSuit];
		for(int i=0; i<ret.length; i++) {
			ret[i] = false;
		}
		
		int numCombosPassed = 0;
		
		for(int i=0; numCardsToTake > 0; i++) {
			int numSpacesLeft = numUnknownCardsInSuit - i;
			
			if(numSpacesLeft == numCardsToTake) {
				ret[i] = true;
				numCardsToTake--;
				
			} else if(numCombosPassed + getCombination(numSpacesLeft-1,numCardsToTake-1) <= comboNumber) {
				numCombosPassed += getCombination(numSpacesLeft-1,numCardsToTake-1);
				
			} else {
				ret[i] = true;
				numCardsToTake--;
			}
			
		}
		
		
		return ret;
	}
	
}

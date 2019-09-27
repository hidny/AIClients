package mellow.ai.simulation;

import java.util.Random;

import mellow.Constants;
import mellow.cardUtils.CardStringFunctions;
import mellow.ai.simulation.SelectedPartitionAndIndex;

public class SimulationSetup {

	public static Random random = new Random();
	
	//TODO: rework logic from the top-down considering:
		// 1) This should be generalizable to euchre
		// 2) If getNumberOfWaysToSimulate < N, we will make it deterministic
		// 3) Try to avoid calling root of getNumberOfWaysToSimulate more than once for same config.
	/*
	public static String[][] distributeUnk555nownCards(String unknownCards[], int numSpacesAvailPerPlayer[], boolean originalIsVoidList[][]) {
		
		long numWays = getNumberOfWaysToSimulate(getNumUnknownPerSuit(unknownCards), numSpacesAvailPerPlayer, originalIsVoidList);
		
		long randIndexNumber = getRandNumberFrom0ToN(numWays);
		
		System.out.println("randIndexNumber: " + randIndexNumber);
		
		SelectedPartitionAndIndex suitPartitionsAndComboNumbers = getSelectedPartitionAndIndexBasedOnCombinationIndex(
								getNumUnknownPerSuit(unknownCards), numSpacesAvailPerPlayer, originalIsVoidList, randIndexNumber);
		
		String ret[][] = serveCarsdsBasedOnPartitionAndIndexInfo(suitPartitionsAndComboNumbers, unknownCards, numSpacesAvailPerPlayer);
		
		return ret;
	}*/

	public static long getRandNumberFrom0ToN(long numWays) {
		//The do while loop is to remove bias.
		//Without it, the bias could be high if the numWays is close to 2^63.
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
	
	
	
	//pre: As long as the answer is less than 2^63, it should get the right answer.
	// All calculations in Euchre and Mellow will be less than 2^63, but other cards games with 5 players may overflow the return value.
	// You might get performance improvements by reordering which players get their cards first, but life's too short.
	public static long getNumberOfWaysToSimulate(int numUnknownCardsPerSuit[], int numSpacesAvailPerPlayer[], boolean originalIsVoidList[][]) {
		return getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, 0);
	}

	
	public static long getNumberOfWaysToSimulate(int numUnknownCardsPerSuit[], int numSpacesAvailPerPlayer[], boolean originalIsVoidList[][], int depth) {

		//Setup basic vars:
		boolean voidSuit[] = getVoidSuitArrayForPlayer(numUnknownCardsPerSuit, originalIsVoidList, depth);
		int numVoids = getSumOfElementsInArray(voidSuit);
		int numUnknownCardsLeft = getSumOfElementsInArray(numUnknownCardsPerSuit);
		int numSpaceAvailable = numSpacesAvailPerPlayer[depth];
		
	//Skipping conditions	
		//Skip players with no unknown cards:
		if(numSpaceAvailable == 0) {
			if(depth + 1 < numSpacesAvailPerPlayer.length) {
				return getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, depth + 1);
			} else {
				return 1;
			}
		}
		
		//Check if current player could pick up remaining unknown cards considering void constraints:
		if(playerCouldFillTheirHandWithRemainingCardsConsideringVoidConstraints(numSpaceAvailable, numUnknownCardsPerSuit, voidSuit) == false) {
			return 0;
		}
		
		//Pick up all cards if allowed and no other unknown cards:
		if(numUnknownCardsLeft == numSpacesAvailPerPlayer[depth]) {
			return 1;
		}
	//END Skipping conditions
		
		long ret = 0L;
		
		//Setup suit partition iterator
		int numTrumpSeperators = Constants.NUM_SUITS - 1 - numVoids;
		boolean suitPartitionIter[] = setupComboIterator(numTrumpSeperators, numSpaceAvailable);

		while(suitPartitionIter != null) {
			
			int suitArrayForEachNonVoidSuit[] = convertComboToArray(suitPartitionIter, Constants.NUM_SUITS - numVoids);
			int suitsTakenByPlayer[] = getNumCardsOfEachSuitTakenByPlayer(voidSuit, suitArrayForEachNonVoidSuit);
			int numCardsPerSuitAfterTake[] = getCardsPerSuitRemainingAfterTake(numUnknownCardsPerSuit, suitsTakenByPlayer);
			
			 if( suitPartitionImpossibleToTake(numCardsPerSuitAfterTake, suitsTakenByPlayer, numUnknownCardsPerSuit) ) {
				suitPartitionIter = getNextCombination(suitPartitionIter);
				continue;
			 }
			
			long currentNumWays = 1;
			for(int i=0; i<Constants.NUM_SUITS; i++) {
				currentNumWays *= getCombination(numUnknownCardsPerSuit[i], suitsTakenByPlayer[i]);
			}
			
			//Setup next players recursively if it's not the last player:
			if(depth + 1 < numSpacesAvailPerPlayer.length && currentNumWays > 0) {
				currentNumWays *= getNumberOfWaysToSimulate(numCardsPerSuitAfterTake, numSpacesAvailPerPlayer, originalIsVoidList, depth + 1);
			}
			
			ret += currentNumWays;
			
			suitPartitionIter = getNextCombination(suitPartitionIter);
		}
		
		return ret;
		
	}
	

	//pre: there's always at least 1 way to do it and randIndexNumber < number Of Combinations
	public static SelectedPartitionAndIndex getSelectedPartitionAndIndexBasedOnCombinationIndex(int numUnknownCardsPerSuit[], int numSpacesAvailPerPlayer[], boolean originalIsVoidList[][], long comboIndexNumber) {
		return getSelectedPartitionAndIndexBasedOnCombinationIndex(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, comboIndexNumber, 0, new SelectedPartitionAndIndex());
	}
	
	//pre: there's always at least 1 way to do it and randIndexNumber < number Of Combinations
	public static SelectedPartitionAndIndex getSelectedPartitionAndIndexBasedOnCombinationIndex(int numUnknownCardsPerSuit[], int numSpacesAvailPerPlayer[], boolean originalIsVoidList[][], long comboIndexNumber, int depth, SelectedPartitionAndIndex selectedPartitionAndIndexToFillIn) {
		//Setup basic vars:
		boolean voidSuit[] = getVoidSuitArrayForPlayer(numUnknownCardsPerSuit, originalIsVoidList, depth);
		int numVoids = getSumOfElementsInArray(voidSuit);
		int numUnknownCardsLeft = getSumOfElementsInArray(numUnknownCardsPerSuit);
		int numSpaceAvailable = numSpacesAvailPerPlayer[depth];
	
	//Skipping conditions	
		//Skip players with no unknown cards:
		if(numSpaceAvailable == 0) {

			selectedPartitionAndIndexToFillIn.giveNoCardsToPlayer(depth);

			if(depth + 1 < numSpacesAvailPerPlayer.length) {
				return getSelectedPartitionAndIndexBasedOnCombinationIndex(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, comboIndexNumber, depth+1, selectedPartitionAndIndexToFillIn);
			} else {
				return selectedPartitionAndIndexToFillIn;
			}
		}
		
		//Check if current player could pick up remaining unknown cards considering void constraints:
		if(playerCouldFillTheirHandWithRemainingCardsConsideringVoidConstraints(numSpaceAvailable, numUnknownCardsPerSuit, voidSuit) == false) {
			System.err.println("ERROR: called getSelectedPartitionAndIndex in a case where there\'s no way to give the players the remaining cards");
			System.exit(1);
			return selectedPartitionAndIndexToFillIn;
		}
		
		//Pick up all cards if allowed and no other unknown cards:
		if(numUnknownCardsLeft == numSpacesAvailPerPlayer[depth]) {
			selectedPartitionAndIndexToFillIn.giveWhatsLeftToNextPlayer(depth, numUnknownCardsPerSuit);
			return selectedPartitionAndIndexToFillIn;
		}
	//END Skipping conditions
		
		long prevNumCombosSkippedThru = 0L;
		long numCombosSkippedThru = 0L;
		
		int numTrumpSeperators = Constants.NUM_SUITS - 1 - numVoids;
		boolean suitPartitionIter[] = setupComboIterator(numTrumpSeperators, numSpaceAvailable);
		
		
		while(suitPartitionIter != null) {
			
			int suitArrayForEachNonVoidSuit[] = convertComboToArray(suitPartitionIter, Constants.NUM_SUITS - numVoids);
			int suitsTakenByPlayer[] = getNumCardsOfEachSuitTakenByPlayer(voidSuit, suitArrayForEachNonVoidSuit);
			int numCardsPerSuitAfterTake[] = getCardsPerSuitRemainingAfterTake(numUnknownCardsPerSuit, suitsTakenByPlayer);
			
			if( suitPartitionImpossibleToTake(numCardsPerSuitAfterTake, suitsTakenByPlayer, numUnknownCardsPerSuit) ) {
				suitPartitionIter = getNextCombination(suitPartitionIter);
				continue;
			}
			
			long currentNumWays = 1;
			for(int i=0; i<Constants.NUM_SUITS; i++) {
				currentNumWays *= getCombination(numUnknownCardsPerSuit[i], suitsTakenByPlayer[i]);
			}
			
			if(depth + 1 < numSpacesAvailPerPlayer.length && currentNumWays > 0) {
				currentNumWays *= getNumberOfWaysToSimulate(numCardsPerSuitAfterTake, numSpacesAvailPerPlayer, originalIsVoidList, depth + 1);
				
			}
			
			numCombosSkippedThru += currentNumWays;
			
			//Check if the combination index is contained with the current suitPartitionIteration:
			if(numCombosSkippedThru > comboIndexNumber && currentNumWays > 0) {

				selectedPartitionAndIndexToFillIn.setSuitsTakenByPlayers(depth, suitsTakenByPlayer);
				
				if(depth + 1 < numSpacesAvailPerPlayer.length) {
					
					//Recursively fill in partition info for next player:
					long indexFromStartOfCombo = (comboIndexNumber - prevNumCombosSkippedThru);
					long numWaysToSetupNextPlayers = getNumberOfWaysToSimulate(numCardsPerSuitAfterTake, numSpacesAvailPerPlayer, originalIsVoidList, depth + 1);
					long currentPlayerComboNumber = indexFromStartOfCombo / numWaysToSetupNextPlayers;
					long nextComboIndexNumber = indexFromStartOfCombo % numWaysToSetupNextPlayers;
					
					selectedPartitionAndIndexToFillIn.setPlayerComboNumber(depth, currentPlayerComboNumber);
					
					return getSelectedPartitionAndIndexBasedOnCombinationIndex(numCardsPerSuitAfterTake, numSpacesAvailPerPlayer, originalIsVoidList, nextComboIndexNumber, depth + 1, selectedPartitionAndIndexToFillIn);

				} else {
					
					//Last player to fill-in shouldn't make it to the while loop in this function
					System.err.println("ERROR: something went wrong in getSelectedPartitionAndIndex. Last player wasted time and iterated thru suit partitions.");
					System.exit(1);
					return null;
				}
				
			}
			
			prevNumCombosSkippedThru = numCombosSkippedThru;
			
			suitPartitionIter = getNextCombination(suitPartitionIter);
		}
		
		//At this point, the combination index got too big...
		
		if(numCombosSkippedThru <= comboIndexNumber) {
			System.out.println(numCombosSkippedThru + "  vs " + comboIndexNumber);
			System.err.println("ERROR: something went wrong in getSelectedPartitionAndIndex. comboIndexNumber is too big");
			System.out.println("depth: " + depth);
			System.exit(1);
		}

		System.err.println("ERROR: did not return selectedPartitionAndIndexToFillIn and numCombosSkippedThru is bigger than comboIndexNumber?");
		System.exit(1);
		return null;
		
		
	}
	
	
	public static int getSumOfElementsInArray(int array[]) {
		int ret = 0;
		for(int i=0; i<array.length; i++) {
			ret += array[i];
		}
		return ret;
	}
	
	public static int getSumOfElementsInArray(boolean array[]) {
		int ret = 0;
		for(int i=0; i<array.length; i++) {
			if(array[i]) {
				ret++;
			}
		}
		return ret;
	}
	
	public static boolean allRemainingCardsCouldBePickedUp(boolean voidSuit[], int numUnknownCardsPerSuit[]) {
		for(int i=0; i<Constants.NUM_SUITS; i++) {
			if(voidSuit[i] == true && numUnknownCardsPerSuit[i] > 0) {
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean[] getVoidSuitArrayForPlayer(int numUnknownCardsPerSuit[], boolean originalIsVoidList[][], int depth) {
		boolean voidSuit[] = new boolean[Constants.NUM_SUITS];
		for(int i=0; i<voidSuit.length; i++) {
			if(originalIsVoidList[depth][i] || numUnknownCardsPerSuit[i] == 0) {
				voidSuit[i] = true;
			}
		}
		return voidSuit;
	}
	
	public static boolean playerCouldFillTheirHandWithRemainingCardsConsideringVoidConstraints(int numSpaceAvailable, int numUnknownCardsPerSuit[], boolean voidSuit[]) {
		int numCouldPickUp = 0;
		for(int i=0; i<Constants.NUM_SUITS; i++) {
			if(voidSuit[i] == false) {
				numCouldPickUp += numUnknownCardsPerSuit[i];
			}
		}
		
		return numCouldPickUp >= numSpaceAvailable;
	}
	
	public static boolean[] setupComboIterator(int numTrueValues, int numFalseValue) {
		boolean combo[] = new boolean[numTrueValues + numFalseValue];
		for(int i=0; i<combo.length; i++) {
			if(i < numTrueValues) {
				combo[i] = true;
			} else {
				combo[i] = false;
			}
		}
		return combo;
	}
	
	public static boolean suitPartitionImpossibleToTake(int numCardsPerSuitAfterTake[], int suitsTakenByPlayer[], int numUnknownCardsPerSuit[]) {
		for(int i=0; i<Constants.NUM_SUITS; i++) {
			if(numCardsPerSuitAfterTake[i] < 0 || suitsTakenByPlayer[i] > numUnknownCardsPerSuit[i]) {
				return true;
			}
		}
		return false;
	}
	
	public static int[] getNumCardsOfEachSuitTakenByPlayer(boolean voidSuit[], int suitArrayForEachNonVoidSuit[]) {
		int ret[] = new int[Constants.NUM_SUITS];
		
		for(int i=0, j=0; i<Constants.NUM_SUITS; i++) {
			if(voidSuit[i] == false) {
				ret[i] = suitArrayForEachNonVoidSuit[j];
				j++;
			} else {
				ret[i] = 0;
			}
		}
		
		return ret;
	
	}
	
	public static int[] getCardsPerSuitRemainingAfterTake(int numUnknownCardsPerSuit[], int suitsTakenByPlayer[]) {
		int numCardsPerSuitAfterTake[] = new int[Constants.NUM_SUITS];
		
		for(int i=0; i<Constants.NUM_SUITS; i++) {
			numCardsPerSuitAfterTake[i] = numUnknownCardsPerSuit[i] - suitsTakenByPlayer[i];
		}
		
		return numCardsPerSuitAfterTake;
	}
	
	public static int[] convertComboToArray(boolean combo[], int size) {
		int ret[] = new int[size];
		
		if(size == 0) {
			return ret;
		}
		
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
	
	
	private static String TAKEN = null;
	
	//TODO: OMG TEST more! (Test that all combinations are different)
	//TODO: clean the code up...
	public static String[][] serveCarsdsBasedOnPartitionAndIndexInfo(SelectedPartitionAndIndex selectedSuitsAndCombos, String unknownCards[], int numSpacesAvailPerPlayer[]) {
		
		//TODO: put in functions
		String unknownCardsPerSuit[][] = new String[Constants.NUM_SUITS][];
		
		int curNumUnknownCardsPerSuit[] = CardStringFunctions.organizeCardsBySuitMellow(unknownCards);
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
		//END PUT IN FUNCTIONS
		
		String unknownCardDistPerPlayer[][] = new String[Constants.NUM_PLAYERS][];
		
		for(int playerI=0; playerI< numSpacesAvailPerPlayer.length; playerI++) {
			
			int curCardsTakenByPlayer=0;
			
			unknownCardDistPerPlayer[playerI] = new String[numSpacesAvailPerPlayer[playerI]];
			
			long currentComboNum = selectedSuitsAndCombos.comboIndex[playerI];
			
			//TODO 1: put in function
			//Work backwards from suit list and give player the cards:
			for(int suit=Constants.NUM_SUITS - 1; suit>=0; suit--) {
				
				int numCardsPlayerWillTake = selectedSuitsAndCombos.suitsTakenByPlayer[playerI][suit];
				
				long numWaysToSetupSuitForPlayer = SimulationSetup.getCombination(curNumUnknownCardsPerSuit[suit], numCardsPlayerWillTake);
				int comboNumberForSuit = (int)( currentComboNum % numWaysToSetupSuitForPlayer);
				
				boolean combo[] = convertComboNumberToArray(curNumUnknownCardsPerSuit[suit], numCardsPlayerWillTake, comboNumberForSuit);
				
				//TODO 2: put in function
				for(int i=0, j=0; i<unknownCardsPerSuit[suit].length && j<combo.length; i++) {
					if(unknownCardsPerSuit[suit][i] != TAKEN) {
						if(combo[j]) {
							//System.out.println("Space avail for player " + playerList[playerI] + ": " + numSpacesAvailPerPlayer[playerList[playerI]] +   " curCardsTakenByPlayer: " + curCardsTakenByPlayer);
							unknownCardDistPerPlayer[playerI][curCardsTakenByPlayer] =
									unknownCardsPerSuit[suit][i];
							
							curCardsTakenByPlayer++;
							unknownCardsPerSuit[suit][i] = TAKEN;
							curNumUnknownCardsPerSuit[suit]--;
						}
						j++;
					}
				}
				//END TODO2
				
				currentComboNum /= numWaysToSetupSuitForPlayer;
			}
			//END TODO 1
			
			if(currentComboNum != 0) {
				System.err.println("ERROR: Distributing the cards messed up and didn\'t end up with correct combo num");
				System.exit(1);
			}
			
			if(curCardsTakenByPlayer != unknownCardDistPerPlayer[playerI].length) {
				System.out.println("ERROR: Distributing the cards messed up and player didn't end up with correct # of cards.");
				System.exit(1);
			}
		}
		
		
		return unknownCardDistPerPlayer;
	}
	

	//Converts index number of of combination into the bool array representing the combination.
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

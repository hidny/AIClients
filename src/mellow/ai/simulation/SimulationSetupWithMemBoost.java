package mellow.ai.simulation;

import java.util.Random;

import mellow.Constants;
import mellow.cardUtils.CardStringFunctions;
import mellow.ai.simulation.objects.SelectedPartitionAndIndex;

public class SimulationSetupWithMemBoost {

	
	public static Random random = new Random();
	
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
		return getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, START_INDEX_PLAYER);
	}

	
	//Don't start at index_player_0, because that's the current player, and we know what's in the current player's hand
	public static int START_INDEX_PLAYER = 1;
	public static long curMult = 1;
	public static long curNumWaysDepth1[];
	public static int curIndexDepth1 = 0;
	
	//TODO: later:
	public static long curNumWaysDepth2[][];
	public static int curIndexDepth2 = 0;
	
	
	private static long getNumberOfWaysToSimulate(int numUnknownCardsPerSuit[], int numSpacesAvailPerPlayer[], boolean originalIsVoidList[][], int playerIndex) {

		//Setup basic vars:
		boolean voidSuit[] = getVoidSuitArrayForPlayer(numUnknownCardsPerSuit, originalIsVoidList, playerIndex);
		int numVoids = getSumOfElementsInArray(voidSuit);
		int numUnknownCardsLeft = getSumOfElementsInArray(numUnknownCardsPerSuit);
		int numCardsPlayerNeedsToPickup = numSpacesAvailPerPlayer[playerIndex];
		
	//Skipping conditions	
		//Skip players with no unknown cards:
		if(numCardsPlayerNeedsToPickup == 0) {
			
			//TODO: fix copy paste code
			if(playerIndex == START_INDEX_PLAYER) {
				curNumWaysDepth1 = new long[1];
				curNumWaysDepth2 = new long[1][];
				curNumWaysDepth1[0] = 0;
			//There's a bug with START_INDEX_PLAYER + 1. Will investigate later.
			}/* else if(playerIndex == START_INDEX_PLAYER + 1) {
				curNumWaysDepth2[curIndexDepth1] = new long[1];
				curNumWaysDepth2[curIndexDepth1][0] = 0;
			}*/
			//END TODO
			if(playerIndex + 1 < numSpacesAvailPerPlayer.length) {
				return getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, playerIndex + 1);
			} else {
				return 1;
			}
		}
		
		//Check if current player could pick up remaining unknown cards considering void constraints:
		if(playerCouldFillTheirHandWithRemainingCardsConsideringVoidConstraints(numCardsPlayerNeedsToPickup, numUnknownCardsPerSuit, voidSuit) == false) {
			return 0;
		}
		
		//Pick up all cards if allowed and no other unknown cards:
		if(numUnknownCardsLeft == numSpacesAvailPerPlayer[playerIndex]) {

			//TODO: fix copy paste code
			if(playerIndex == START_INDEX_PLAYER) {
				curNumWaysDepth1 = new long[1];
				curNumWaysDepth2 = new long[1][];
				curNumWaysDepth1[0] = 0;
			}/* else if(playerIndex == START_INDEX_PLAYER + 1) {
				curNumWaysDepth2[curIndexDepth1] = new long[1];
				curNumWaysDepth2[curIndexDepth1][0] = 0;
			}*/
			//END TODO
			
			return 1;
		}
	//END Skipping conditions
		
		long ret = 0L;
		
		//Setup suit partition iterator
		int numTrumpSeperators = Constants.NUM_SUITS - 1 - numVoids;
		boolean suitPartitionIter[] = setupComboIterator(numTrumpSeperators, numCardsPlayerNeedsToPickup);


		//TODO: fix copy paste code
		if(playerIndex == START_INDEX_PLAYER) {
			curNumWaysDepth1 = new long[(int)getCombination(numCardsPlayerNeedsToPickup + numTrumpSeperators, numTrumpSeperators)];
			curNumWaysDepth2 = new long[(int)getCombination(numCardsPlayerNeedsToPickup + numTrumpSeperators, numTrumpSeperators)][];
			curIndexDepth1 = 0;
			curNumWaysDepth1[0] = 0;
			
		}/* else if(playerIndex == START_INDEX_PLAYER + 1) {
			System.err.println("curIndexDepth1: " + curIndexDepth1);
			System.err.println("length: " + curNumWaysDepth2.length);
			curNumWaysDepth2[curIndexDepth1] = new long[(int)getCombination(numCardsPlayerNeedsToPickup + numTrumpSeperators, numTrumpSeperators)];
			curIndexDepth2=0;
			curNumWaysDepth2[curIndexDepth1][0] = 0;
		}*/
		//END TODO
		
		while(suitPartitionIter != null) {
			
			int suitArrayForEachNonVoidSuit[] = convertComboToArray(suitPartitionIter, Constants.NUM_SUITS - numVoids);
			int suitsTakenByPlayer[] = getNumCardsOfEachSuitTakenByPlayer(voidSuit, suitArrayForEachNonVoidSuit);
			int numCardsPerSuitAfterTake[] = getCardsPerSuitRemainingAfterTake(numUnknownCardsPerSuit, suitsTakenByPlayer);
			
			if(playerIndex == START_INDEX_PLAYER) {
				curNumWaysDepth1[curIndexDepth1] = ret;
				curIndexDepth2 = 0;
				
			}/* else if(playerIndex == START_INDEX_PLAYER + 1) {
				//cumNumWaysDepth1[curIndexDepth0][curIndexDepth1] = cumNumWaysDepth0[curIndexDepth0] + ret;
				//TODO: need to logic out the  var: cumNumWaysDepth0[curIndexDepth0]
				
				curNumWaysDepth2[curIndexDepth1][curIndexDepth2] = ret;
			}*/
			

			//TODO: There might be a way to skip bad suit partitions more quickly than this... but whatever
			 if(! suitPartitionImpossibleToTake(numCardsPerSuitAfterTake, suitsTakenByPlayer, numUnknownCardsPerSuit) ) {
				
				long currentNumWays = 1;
				for(int i=0; i<Constants.NUM_SUITS; i++) {
					currentNumWays *= getCombination(numUnknownCardsPerSuit[i], suitsTakenByPlayer[i]);
				}
				
				//Setup next players recursively if it's not the last player:
				if(playerIndex + 1 < numSpacesAvailPerPlayer.length && currentNumWays > 0) {
					
					currentNumWays *= getNumberOfWaysToSimulate(numCardsPerSuitAfterTake, numSpacesAvailPerPlayer, originalIsVoidList, playerIndex + 1);
				}
				
				ret += currentNumWays;
			 }
			 
			 if(playerIndex == START_INDEX_PLAYER) {
				curIndexDepth1++;
				
			}/* else if(playerIndex == START_INDEX_PLAYER + 1) {
				curIndexDepth2++;
			}*/
			suitPartitionIter = getNextCombination(suitPartitionIter);
		}
		
		return ret;
		
	}
	

	//pre: there's always at least 1 way to do it and randIndexNumber < number Of Combinations
	public static SelectedPartitionAndIndex getSelectedPartitionAndIndexBasedOnCombinationIndex(int numUnknownCardsPerSuit[], int numSpacesAvailPerPlayer[], boolean originalIsVoidList[][], long comboIndexNumber) {
		return getSelectedPartitionAndIndexBasedOnCombinationIndex(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, comboIndexNumber, START_INDEX_PLAYER, new SelectedPartitionAndIndex());
	}
	
	
	
	//pre: there's always at least 1 way to do it and randIndexNumber < number Of Combinations
	private static SelectedPartitionAndIndex getSelectedPartitionAndIndexBasedOnCombinationIndex(int numUnknownCardsPerSuit[], int numSpacesAvailPerPlayer[], boolean originalIsVoidList[][], long comboIndexNumber, int playerIndex, SelectedPartitionAndIndex selectedPartitionAndIndexToFillIn) {
		
		//System.err.println("... " + playerIndex);
		
		//Setup basic vars:
		boolean voidSuit[] = getVoidSuitArrayForPlayer(numUnknownCardsPerSuit, originalIsVoidList, playerIndex);
		int numVoids = getSumOfElementsInArray(voidSuit);
		int numUnknownCardsLeft = getSumOfElementsInArray(numUnknownCardsPerSuit);
		int numSpaceAvailable = numSpacesAvailPerPlayer[playerIndex];
	
	//Skipping conditions	
		//Skip players with no unknown cards:
		if(numSpaceAvailable == 0) {

			selectedPartitionAndIndexToFillIn.giveNoCardsToPlayer(playerIndex);

			if(playerIndex + 1 < numSpacesAvailPerPlayer.length) {
				return getSelectedPartitionAndIndexBasedOnCombinationIndex(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, comboIndexNumber, playerIndex+1, selectedPartitionAndIndexToFillIn);
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
		if(numUnknownCardsLeft == numSpacesAvailPerPlayer[playerIndex]) {
			selectedPartitionAndIndexToFillIn.giveWhatsLeftToNextPlayer(playerIndex, numUnknownCardsPerSuit);
			return selectedPartitionAndIndexToFillIn;
		}
	//END Skipping conditions
		
		
		int numTrumpSeperators = Constants.NUM_SUITS - 1 - numVoids;
		
		boolean suitPartitionIterShortcut[] = null;
		
		//TODO: shortcut for player index 1:
		int suitPartitionIterIndexToUse = -1;
		if(playerIndex == START_INDEX_PLAYER) {
			suitPartitionIterIndexToUse = binarySearchForSuitComboForDepth0(comboIndexNumber);
		
			//boolean combo[] = convertComboNumberToArray(curNumCardsRemainingPerSuit[indexSuit], numCardsPlayerWillTakeOfSuit, comboNumberForSuit);
			
			 //convertComboNumberToArray(int numUnknownCardsInSuit, int numCardsToTake, int comboNumber)
			
			suitPartitionIterShortcut = convertComboNumberToArray(numSpaceAvailable + numTrumpSeperators, numTrumpSeperators, suitPartitionIterIndexToUse);
			//TODO: actually use the shortcut
			//Will need to convert suitPartitionIterIndexToUse to bool array, but I've done that before.
			
			int suitArrayForEachNonVoidSuit[] = convertComboToArray(suitPartitionIterShortcut, Constants.NUM_SUITS - numVoids);
			int suitsTakenByPlayer[] = getNumCardsOfEachSuitTakenByPlayer(voidSuit, suitArrayForEachNonVoidSuit);
			int numCardsPerSuitAfterTake[] = getCardsPerSuitRemainingAfterTake(numUnknownCardsPerSuit, suitsTakenByPlayer);
			
			selectedPartitionAndIndexToFillIn.setSuitsTakenByPlayers(playerIndex, suitsTakenByPlayer);
			
			if(playerIndex + 1 < numSpacesAvailPerPlayer.length) {
				
				//Recursively fill in partition info for next player:
				long indexFromStartOfCombo = (comboIndexNumber - curNumWaysDepth1[suitPartitionIterIndexToUse]);
				long numWaysToSetupNextPlayers = getNumberOfWaysToSimulate(numCardsPerSuitAfterTake, numSpacesAvailPerPlayer, originalIsVoidList, playerIndex + 1);
				long currentPlayerComboNumber = indexFromStartOfCombo / numWaysToSetupNextPlayers;
				long nextComboIndexNumber = indexFromStartOfCombo % numWaysToSetupNextPlayers;
				
				selectedPartitionAndIndexToFillIn.setPlayerComboNumber(playerIndex, currentPlayerComboNumber);
				
				return getSelectedPartitionAndIndexBasedOnCombinationIndex(numCardsPerSuitAfterTake, numSpacesAvailPerPlayer, originalIsVoidList, nextComboIndexNumber, playerIndex + 1, selectedPartitionAndIndexToFillIn);

			} else {
				
				//Last player to fill-in shouldn't make it to the while loop in this function
				System.err.println("ERROR: something went wrong in getSelectedPartitionAndIndex. Last player wasted time and iterated thru suit partitions.");
				System.exit(1);
				return null;
			}
			
		}
		
		//END TODO SHORTCUT
		
		long prevNumCombosSkippedThru = 0L;
		long numCombosSkippedThru = 0L;
		
		boolean suitPartitionIter[] = setupComboIterator(numTrumpSeperators, numSpaceAvailable);
		
		//TODO: make shortcut for index 1...
		//END TODO
		
		int curSuitPartitionIterIndex = 0;
		
		while(suitPartitionIter != null) {
			
			int suitArrayForEachNonVoidSuit[] = convertComboToArray(suitPartitionIter, Constants.NUM_SUITS - numVoids);
			int suitsTakenByPlayer[] = getNumCardsOfEachSuitTakenByPlayer(voidSuit, suitArrayForEachNonVoidSuit);
			int numCardsPerSuitAfterTake[] = getCardsPerSuitRemainingAfterTake(numUnknownCardsPerSuit, suitsTakenByPlayer);
			
			if( ! suitPartitionImpossibleToTake(numCardsPerSuitAfterTake, suitsTakenByPlayer, numUnknownCardsPerSuit) ) {
				
				
				long currentNumWays = 1;
				for(int i=0; i<Constants.NUM_SUITS; i++) {
					currentNumWays *= getCombination(numUnknownCardsPerSuit[i], suitsTakenByPlayer[i]);
				}
				
				if(playerIndex + 1 < numSpacesAvailPerPlayer.length && currentNumWays > 0) {
					currentNumWays *= getNumberOfWaysToSimulate(numCardsPerSuitAfterTake, numSpacesAvailPerPlayer, originalIsVoidList, playerIndex + 1);
					
				}
				
				numCombosSkippedThru += currentNumWays;
				
				//Check if the combination index is contained with the current suitPartitionIteration:
				if(numCombosSkippedThru > comboIndexNumber 
						//TODO: is this AND condition even needed:
						&& currentNumWays > 0) {
					
					//SANITY CHECK:
					//System.err.println("hello..." + playerIndex);
					if(playerIndex == START_INDEX_PLAYER) {
						if(suitPartitionIterIndexToUse == curSuitPartitionIterIndex) {
							System.err.println("GOOD!");
							System.err.println("Test in getSelectedPartitionAndIndexBasedOnCombinationIndex passed!");
							
							if(suitPartitionIterShortcut.length != suitPartitionIter.length) {
								System.err.println("ERROR in getSelectedPartitionAndIndexBasedOnCombinationIndex: The length of suitPartitionIterShortcut is incorrect!");

								System.err.println("Debug!");
								System.exit(1);
							}
							
							for(int i=0; i<suitPartitionIterShortcut.length; i++) {
								if(suitPartitionIterShortcut[i] != suitPartitionIter[i]) {
									System.err.println("ERROR in getSelectedPartitionAndIndexBasedOnCombinationIndex: The element at i = " + i + " of suitPartitionIterShortcut is incorrect!");
									System.err.println("Debug!");
									
									System.exit(1);
								}
							}
						} else {
							System.err.println("suitPartitionIterIndexToUse: " + suitPartitionIterIndexToUse);
							System.err.println("curSuitPartitionIterIndex: " + curSuitPartitionIterIndex);
							
							System.err.println("Debug!");
							System.err.println("ERROR in getSelectedPartitionAndIndexBasedOnCombinationIndex: The short cut messed up for playerIndex = 0!");
							System.exit(1);
						}
					}
					//END SANITY CHECK:
					
					selectedPartitionAndIndexToFillIn.setSuitsTakenByPlayers(playerIndex, suitsTakenByPlayer);
					
					if(playerIndex + 1 < numSpacesAvailPerPlayer.length) {
						
						//Recursively fill in partition info for next player:
						long indexFromStartOfCombo = (comboIndexNumber - prevNumCombosSkippedThru);
						long numWaysToSetupNextPlayers = getNumberOfWaysToSimulate(numCardsPerSuitAfterTake, numSpacesAvailPerPlayer, originalIsVoidList, playerIndex + 1);
						long currentPlayerComboNumber = indexFromStartOfCombo / numWaysToSetupNextPlayers;
						long nextComboIndexNumber = indexFromStartOfCombo % numWaysToSetupNextPlayers;
						
						selectedPartitionAndIndexToFillIn.setPlayerComboNumber(playerIndex, currentPlayerComboNumber);
						
						return getSelectedPartitionAndIndexBasedOnCombinationIndex(numCardsPerSuitAfterTake, numSpacesAvailPerPlayer, originalIsVoidList, nextComboIndexNumber, playerIndex + 1, selectedPartitionAndIndexToFillIn);
	
					} else {
						
						//Last player to fill-in shouldn't make it to the while loop in this function
						System.err.println("ERROR: something went wrong in getSelectedPartitionAndIndex. Last player wasted time and iterated thru suit partitions.");
						System.exit(1);
						return null;
					}
					
				}
				
				prevNumCombosSkippedThru = numCombosSkippedThru;
			} //END IF NOT suitPartitionImpossibleToTake
			
			suitPartitionIter = getNextCombination(suitPartitionIter);
			curSuitPartitionIterIndex++;
		} //END COMBO LOOP
		
		//At this point, the combination index got too big...
		
		if(numCombosSkippedThru <= comboIndexNumber) {
			System.err.println(numCombosSkippedThru + "  vs " + comboIndexNumber);
			System.err.println("ERROR: something went wrong in getSelectedPartitionAndIndex. comboIndexNumber is too big");
			System.err.println("depth: " + playerIndex);
			System.exit(1);
		}

		System.err.println("ERROR: did not return selectedPartitionAndIndexToFillIn and numCombosSkippedThru is bigger than comboIndexNumber?");
		System.exit(1);
		return null;
		
		
	}

	//return index of 
	
	//TODO: OMG TEST THIS...
	//TODO 2: repeat but for depth 1.
	public static int binarySearchForSuitComboForDepth0(long comboIndexNumber) {
		return binarySearchForSuitComboForDepth0(comboIndexNumber, 0, curNumWaysDepth1.length);
	}
	
	private static int binarySearchForSuitComboForDepth0(long comboIndexNumber, int min, int max) {
		int mid = (max + min)/2;
		long comboSumAtMid = curNumWaysDepth1[mid];
		
		if(mid == min) {
			return binarySearchForFirstComboWithASolutionForDepth0(mid, max);
		}
		
		if(comboIndexNumber < comboSumAtMid) {
			return binarySearchForSuitComboForDepth0(comboIndexNumber, min, mid);
		
		} else {
		
			return binarySearchForSuitComboForDepth0(comboIndexNumber, mid, max);
		}	
	}
	
	private static int binarySearchForFirstComboWithASolutionForDepth0(int start, int max) {
		long numWays = curNumWaysDepth1[start];
		int mid = (start + max)/2;
		
		if(start == mid) {
			return mid;
		}
		
		if(mid == numWays) {
			return binarySearchForFirstComboWithASolutionForDepth0(mid, max);
		} else {
			return binarySearchForFirstComboWithASolutionForDepth0(start, mid);
		}
		
	}
	//END TODO:
	
	private static int getSumOfElementsInArray(int array[]) {
		int ret = 0;
		for(int i=0; i<array.length; i++) {
			ret += array[i];
		}
		return ret;
	}
	
	private static int getSumOfElementsInArray(boolean array[]) {
		int ret = 0;
		for(int i=0; i<array.length; i++) {
			if(array[i]) {
				ret++;
			}
		}
		return ret;
	}
	
	private static boolean[] getVoidSuitArrayForPlayer(int numUnknownCardsPerSuit[], boolean originalIsVoidList[][], int depth) {
		boolean voidSuit[] = new boolean[Constants.NUM_SUITS];
		for(int i=0; i<voidSuit.length; i++) {
			if(originalIsVoidList[depth][i] || numUnknownCardsPerSuit[i] == 0) {
				voidSuit[i] = true;
			}
		}
		return voidSuit;
	}
	
	private static boolean playerCouldFillTheirHandWithRemainingCardsConsideringVoidConstraints(int numSpaceAvailable, int numUnknownCardsPerSuit[], boolean voidSuit[]) {
		int numCouldPickUp = 0;
		for(int i=0; i<Constants.NUM_SUITS; i++) {
			if(voidSuit[i] == false) {
				numCouldPickUp += numUnknownCardsPerSuit[i];
			}
		}
		
		return numCouldPickUp >= numSpaceAvailable;
	}
	
	private static boolean suitPartitionImpossibleToTake(int numCardsPerSuitAfterTake[], int suitsTakenByPlayer[], int numUnknownCardsPerSuit[]) {
		for(int i=0; i<Constants.NUM_SUITS; i++) {
			if(numCardsPerSuitAfterTake[i] < 0 || suitsTakenByPlayer[i] > numUnknownCardsPerSuit[i]) {
				return true;
			}
		}
		return false;
	}
	
	private static int[] getNumCardsOfEachSuitTakenByPlayer(boolean voidSuit[], int suitArrayForEachNonVoidSuit[]) {
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
	
	private static int[] getCardsPerSuitRemainingAfterTake(int numUnknownCardsPerSuit[], int suitsTakenByPlayer[]) {
		int numCardsPerSuitAfterTake[] = new int[Constants.NUM_SUITS];
		
		for(int i=0; i<Constants.NUM_SUITS; i++) {
			numCardsPerSuitAfterTake[i] = numUnknownCardsPerSuit[i] - suitsTakenByPlayer[i];
		}
		
		return numCardsPerSuitAfterTake;
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
	
	public static String[][] serveCarsdsBasedOnPartitionAndIndexInfo(SelectedPartitionAndIndex selectedSuitsAndCombos, String unknownCards[], int numSpacesAvailPerPlayer[]) {
		
		int curNumUnknownCardsPerSuit[] = CardStringFunctions.organizeCardsBySuit(unknownCards);
		String unknownCardsPerSuit[][] =  getUnknownCardsPerSuit(curNumUnknownCardsPerSuit, unknownCards);
		
		String playerHandsToPopulate[][] = new String[Constants.NUM_PLAYERS][];
		
		for(int playerI=0; playerI< numSpacesAvailPerPlayer.length; playerI++) {
			long cardCombinationNumberForPlayer = selectedSuitsAndCombos.comboIndex[playerI];
			playerHandsToPopulate[playerI] = new String[numSpacesAvailPerPlayer[playerI]];
			
			populatePlayerCardsWithUnknownCardsAccordingToSelectedSuitsAndCombos(unknownCardsPerSuit, curNumUnknownCardsPerSuit, selectedSuitsAndCombos.suitsTakenByPlayer[playerI], cardCombinationNumberForPlayer, playerHandsToPopulate[playerI]);
		}
		
		return playerHandsToPopulate;
	}
	
	public static void populatePlayerCardsWithUnknownCardsAccordingToSelectedSuitsAndCombos(String unknownCardsPerSuit[][], int curNumCardsRemainingPerSuit[], int suitsTakenByPlayer[], long cardCombinationNumberForPlayer, String playerHandToPopulate[]) {
		
		int curCardsTakenByPlayer=0;
		
		long currentComboNumber = cardCombinationNumberForPlayer;
		
		for(int indexSuit=Constants.NUM_SUITS - 1; indexSuit>=0; indexSuit--) {
			
			int numCardsPlayerWillTakeOfSuit = suitsTakenByPlayer[indexSuit];
			
			long numWaysToSetupSuitForPlayer = SimulationSetupWithMemBoost.getCombination(curNumCardsRemainingPerSuit[indexSuit], numCardsPlayerWillTakeOfSuit);
			
			int comboNumberForSuit = (int)( currentComboNumber % numWaysToSetupSuitForPlayer);
			
			boolean combo[] = convertComboNumberToArray(curNumCardsRemainingPerSuit[indexSuit], numCardsPlayerWillTakeOfSuit, comboNumberForSuit);
			
			playerTakeCardsFromSuitAccordingToCombination(unknownCardsPerSuit[indexSuit], combo, playerHandToPopulate, curCardsTakenByPlayer);
			
			curCardsTakenByPlayer += numCardsPlayerWillTakeOfSuit;
			curNumCardsRemainingPerSuit[indexSuit] -= numCardsPlayerWillTakeOfSuit;
			currentComboNumber /= numWaysToSetupSuitForPlayer;
		}
		
		if(currentComboNumber != 0) {
			System.err.println("ERROR: Distributing the cards messed up and didn\'t end up with correct combo num");
			System.exit(1);
		}
		
		if(curCardsTakenByPlayer != playerHandToPopulate.length) {
			System.err.println("ERROR: Distributing the cards messed up and player didn't end up with correct # of cards.");
			System.exit(1);
		}
	}
	

	private static String CARD_TAKEN = null;
	
	private static void playerTakeCardsFromSuitAccordingToCombination(String unknownCardsForSuit[], boolean combo[], String playerHandToPopulate[], int curCardsTakenByPlayer) {
		
		//Note about java array primitives:
		//The arrays are passed by value, so I could only change the values of the elements of the arrays... which I do
		for(int i=0, j=0; i<unknownCardsForSuit.length && j<combo.length; i++) {
			if(unknownCardsForSuit[i] != CARD_TAKEN) {
				if(combo[j]) {
					playerHandToPopulate[curCardsTakenByPlayer] =
							unknownCardsForSuit[i];
					
					unknownCardsForSuit[i] = CARD_TAKEN;
					curCardsTakenByPlayer++;
				}
				j++;
			}
		}
		
	}

	private static String[][] getUnknownCardsPerSuit(int curNumUnknownCardsPerSuit[], String unknownCards[]) {
		
		String unknownCardsPerSuit[][] = new String[Constants.NUM_SUITS][];
		
		int currentArrayIndexPerSuit[] = new int[Constants.NUM_SUITS];
		
		for(int i=0; i<Constants.NUM_SUITS; i++) {
			unknownCardsPerSuit[i] = new String[curNumUnknownCardsPerSuit[i]];
			currentArrayIndexPerSuit[i] = 0;
		}
		
		for(int i=0; i<unknownCards.length; i++) {
			int indexSuit = CardStringFunctions.getIndexOfSuit(unknownCards[i]);
			unknownCardsPerSuit[indexSuit][currentArrayIndexPerSuit[indexSuit]] = unknownCards[i];
			currentArrayIndexPerSuit[indexSuit]++;
		}
		
		return unknownCardsPerSuit;
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

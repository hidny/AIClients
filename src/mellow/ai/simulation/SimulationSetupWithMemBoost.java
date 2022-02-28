package mellow.ai.simulation;


import mellow.Constants;
import mellow.ai.simulation.objects.SelectedPartitionAndIndex;

public class SimulationSetupWithMemBoost {

	
	
	//pre: As long as the answer is less than 2^63, it should get the right answer.
	// All calculations in Euchre and Mellow will be less than 2^63, but other cards games with 5 players may overflow the return value.
	// You might get performance improvements by reordering which players get their cards first, but life's too short.

	public static long getNumberOfWaysToSimulate(int numUnknownCardsPerSuit[], int numSpacesAvailPerPlayer[], boolean originalIsVoidList[][]) {
		return getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, START_INDEX_PLAYER);
	}

	
	//Don't start at index_player_0, because that's the current player, and we know what's in the current player's hand
	public static int START_INDEX_PLAYER = 1;

	public static long curNumWaysDepth1[];
	public static int curIndexDepth1 = 0;
	
	public static long curNumWaysDepth2[][];
	public static int curIndexDepth2 = 0;
	
	public static void initializeCurNumWaysArray(int playerIndex, int numWaysToChooseSuitPartition) {
		
		if(playerIndex == START_INDEX_PLAYER) {
			curIndexDepth1 = 0;
			curIndexDepth2 = 0;
			curNumWaysDepth1 = new long[numWaysToChooseSuitPartition];
			curNumWaysDepth2 = new long[numWaysToChooseSuitPartition][];

		} else if(playerIndex == START_INDEX_PLAYER + 1) {
			curIndexDepth2 = 0;
			curNumWaysDepth2[curIndexDepth1] = new long[numWaysToChooseSuitPartition];
			curNumWaysDepth2[curIndexDepth1][curIndexDepth2] = 0;
		}
		
	}
	
	private static long getNumberOfWaysToSimulate(int numUnknownCardsPerSuit[], int numSpacesAvailPerPlayer[], boolean originalIsVoidList[][], int playerIndex) {

		//Setup basic vars:
		boolean voidSuit[] = SimSetupUtils.getVoidSuitArrayForPlayer(numUnknownCardsPerSuit, originalIsVoidList, playerIndex);
		int numVoids = SimSetupUtils.getSumOfElementsInArray(voidSuit);
		int numUnknownCardsLeft = SimSetupUtils.getSumOfElementsInArray(numUnknownCardsPerSuit);
		int numCardsPlayerNeedsToPickup = numSpacesAvailPerPlayer[playerIndex];
		
	//Skipping conditions	
		//Skip players with no unknown cards:
		if(numCardsPlayerNeedsToPickup == 0) {
			
			initializeCurNumWaysArray(playerIndex, 1);
			
			if(playerIndex + 1 < numSpacesAvailPerPlayer.length) {
				return getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, playerIndex + 1);
			} else {
				return 1;
			}
		}
		
		//Check if current player could pick up remaining unknown cards considering void constraints:
		if(SimSetupUtils.playerCouldFillTheirHandWithRemainingCardsConsideringVoidConstraints(numCardsPlayerNeedsToPickup, numUnknownCardsPerSuit, voidSuit) == false) {
			return 0;
		}
		
		//Pick up all cards if allowed and no other unknown cards:
		if(numUnknownCardsLeft == numSpacesAvailPerPlayer[playerIndex]) {
			initializeCurNumWaysArray(playerIndex, 1);
			
			return 1;
		}
	//END Skipping conditions
		
		long ret = 0L;
		
		//Setup suit partition iterator
		int numTrumpSeperators = Constants.NUM_SUITS - 1 - numVoids;
		boolean suitPartitionIter[] = SimSetupUtils.setupComboIterator(numTrumpSeperators, numCardsPlayerNeedsToPickup);

		initializeCurNumWaysArray(playerIndex, (int)SimSetupUtils.getCombination(numCardsPlayerNeedsToPickup + numTrumpSeperators, numTrumpSeperators));
		
		while(suitPartitionIter != null) {
			
			int suitArrayForEachNonVoidSuit[] = SimSetupUtils.convertComboToArray(suitPartitionIter, Constants.NUM_SUITS - numVoids);
			int suitsTakenByPlayer[] = SimSetupUtils.getNumCardsOfEachSuitTakenByPlayer(voidSuit, suitArrayForEachNonVoidSuit);
			int numCardsPerSuitAfterTake[] = SimSetupUtils.getCardsPerSuitRemainingAfterTake(numUnknownCardsPerSuit, suitsTakenByPlayer);
			
			if(playerIndex == START_INDEX_PLAYER) {
				curNumWaysDepth1[curIndexDepth1] = ret;

			} else if(playerIndex == START_INDEX_PLAYER + 1) {
				curNumWaysDepth2[curIndexDepth1][curIndexDepth2] = ret;

			}
			

			//There might be a way to skip bad suit partitions more quickly than this... but whatever
			//I only call getNumberOfWaysToSimulate once per testcase. 
			 if(! SimSetupUtils.suitPartitionImpossibleToTake(numCardsPerSuitAfterTake, suitsTakenByPlayer, numUnknownCardsPerSuit) ) {
				
				long currentNumWays = 1;
				for(int i=0; i<Constants.NUM_SUITS; i++) {
					currentNumWays *= SimSetupUtils.getCombination(numUnknownCardsPerSuit[i], suitsTakenByPlayer[i]);
				}
				
				//Setup next players recursively if it's not the last player:
				if(playerIndex + 1 < numSpacesAvailPerPlayer.length && currentNumWays > 0) {
					
					currentNumWays *= getNumberOfWaysToSimulate(numCardsPerSuitAfterTake, numSpacesAvailPerPlayer, originalIsVoidList, playerIndex + 1);
				}
				
				ret += currentNumWays;
			 }
			 
			 if(playerIndex == START_INDEX_PLAYER) {
				curIndexDepth1++;
				curIndexDepth2=0;
				
			} else if(playerIndex == START_INDEX_PLAYER + 1) {
				curIndexDepth2++;
			}
			suitPartitionIter = SimSetupUtils.getNextCombination(suitPartitionIter);
		}
		
		return ret;
		
	}
	

	//pre: there's always at least 1 way to do it and randIndexNumber < number Of Combinations
	public static SelectedPartitionAndIndex getSelectedPartitionAndIndexBasedOnCombinationIndex(int numUnknownCardsPerSuit[], int numSpacesAvailPerPlayer[], boolean originalIsVoidList[][], long comboIndexNumber, long numWaysToSimulate) {
		return getSelectedPartitionAndIndexBasedOnCombinationIndex(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, comboIndexNumber, START_INDEX_PLAYER, new SelectedPartitionAndIndex(), numWaysToSimulate);
	}
	
	
	
	//pre: there's always at least 1 way to do it and randIndexNumber < number Of Combinations
	private static SelectedPartitionAndIndex getSelectedPartitionAndIndexBasedOnCombinationIndex(int numUnknownCardsPerSuit[], int numSpacesAvailPerPlayer[], boolean originalIsVoidList[][], long comboIndexNumber, int playerIndex, SelectedPartitionAndIndex selectedPartitionAndIndexToFillIn, long numWaysToSimulate) {
		
		
		//Setup basic vars:
		boolean voidSuit[] = SimSetupUtils.getVoidSuitArrayForPlayer(numUnknownCardsPerSuit, originalIsVoidList, playerIndex);
		int numVoids = SimSetupUtils.getSumOfElementsInArray(voidSuit);
		int numUnknownCardsLeft = SimSetupUtils.getSumOfElementsInArray(numUnknownCardsPerSuit);
		int numSpaceAvailable = numSpacesAvailPerPlayer[playerIndex];
	
	//Skipping conditions	
		//Skip players with no unknown cards:
		if(numSpaceAvailable == 0) {

			selectedPartitionAndIndexToFillIn.giveNoCardsToPlayer(playerIndex);

			if(playerIndex + 1 < numSpacesAvailPerPlayer.length) {
				return getSelectedPartitionAndIndexBasedOnCombinationIndex(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, comboIndexNumber, playerIndex+1, selectedPartitionAndIndexToFillIn, numWaysToSimulate);
			} else {
				return selectedPartitionAndIndexToFillIn;
			}
		}
		
		//Check if current player could pick up remaining unknown cards considering void constraints:
		if(SimSetupUtils.playerCouldFillTheirHandWithRemainingCardsConsideringVoidConstraints(numSpaceAvailable, numUnknownCardsPerSuit, voidSuit) == false) {
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
		
		
		
		if(playerIndex == START_INDEX_PLAYER) {
			int suitPartitionIterIndexToUse = binarySearchForSuitComboForDepthN(curNumWaysDepth1, comboIndexNumber);
		
			suitPartitionIterShortcut = SimSetupUtils.convertComboNumberToArray(numSpaceAvailable + numTrumpSeperators, numTrumpSeperators, suitPartitionIterIndexToUse);
			
			int suitArrayForEachNonVoidSuit[] = SimSetupUtils.convertComboToArray(suitPartitionIterShortcut, Constants.NUM_SUITS - numVoids);
			
			int suitsTakenByPlayer[] = SimSetupUtils.getNumCardsOfEachSuitTakenByPlayer(voidSuit, suitArrayForEachNonVoidSuit);
			
			int numCardsPerSuitAfterTake[] = SimSetupUtils.getCardsPerSuitRemainingAfterTake(numUnknownCardsPerSuit, suitsTakenByPlayer);
			
			selectedPartitionAndIndexToFillIn.setSuitsTakenByPlayers(playerIndex, suitsTakenByPlayer);
			
				
			//Recursively fill in partition info for next player:
			long indexFromStartOfCombo = (comboIndexNumber - curNumWaysDepth1[suitPartitionIterIndexToUse]);

			
			long numWaysToFillInCardsForCurrentPlayer = 1;
			for(int i=0; i<Constants.NUM_SUITS; i++) {
				numWaysToFillInCardsForCurrentPlayer *= SimSetupUtils.getCombination(numUnknownCardsPerSuit[i], suitsTakenByPlayer[i]);
			}
			
			long numWaysToSetupNextPlayerShortCut = -1;
			
			if(suitPartitionIterIndexToUse + 1 < curNumWaysDepth1.length) {
				numWaysToSetupNextPlayerShortCut = (curNumWaysDepth1[suitPartitionIterIndexToUse + 1]
													- curNumWaysDepth1[suitPartitionIterIndexToUse]) 
					                                     / numWaysToFillInCardsForCurrentPlayer;
			} else {
				numWaysToSetupNextPlayerShortCut = (numWaysToSimulate
						- curNumWaysDepth1[suitPartitionIterIndexToUse]) 
                             / numWaysToFillInCardsForCurrentPlayer;
				
				//System.err.println("Reached the end of the line!");
				//System.err.println("This could happen without being rigged! Try running test case: Michael2021-2 -> testcase3874.txt");
				//System.err.println("I also managed to force the issue with debug case testcase7014.txt, and it seems to work!");
				//System.err.println("To reach this code, I made the simulator test debug case testcase7014.txt, while rigging things in my favour.");
				//System.err.println("In other words, I gave current player no spade, made current player the first bidder, and rigged it so it distributes the last combination.");

			}
			
			if(suitPartitionIterIndexToUse + 1 < curNumWaysDepth1.length
					&& (curNumWaysDepth1[suitPartitionIterIndexToUse + 1]
											- curNumWaysDepth1[suitPartitionIterIndexToUse]) % numWaysToFillInCardsForCurrentPlayer != 0) {
				System.err.println("ERROR: numWaysToSetupNextPlayerShortCut is not right (it doesn't divide cleanly)");
				System.exit(1);
			}
			
			
			long currentPlayerComboNumber = indexFromStartOfCombo / numWaysToSetupNextPlayerShortCut;
			long nextComboIndexNumber = indexFromStartOfCombo % numWaysToSetupNextPlayerShortCut;
			
			selectedPartitionAndIndexToFillIn.setPlayerComboNumber(playerIndex, currentPlayerComboNumber);
			
			curIndexDepth1 = suitPartitionIterIndexToUse;
			
			return getSelectedPartitionAndIndexBasedOnCombinationIndex(numCardsPerSuitAfterTake, numSpacesAvailPerPlayer, originalIsVoidList, nextComboIndexNumber, playerIndex + 1, selectedPartitionAndIndexToFillIn, numWaysToSetupNextPlayerShortCut);

			
			
		} else if(playerIndex == START_INDEX_PLAYER + 1) {
			//This is almost copy/paste code compared to previous if case, but I'm too lazy to refactor...
			int suitPartitionIterIndexToUse = binarySearchForSuitComboForDepthN(curNumWaysDepth2[curIndexDepth1], comboIndexNumber);
		
			suitPartitionIterShortcut = SimSetupUtils.convertComboNumberToArray(numSpaceAvailable + numTrumpSeperators, numTrumpSeperators, suitPartitionIterIndexToUse);
			
			int suitArrayForEachNonVoidSuit[] = SimSetupUtils.convertComboToArray(suitPartitionIterShortcut, Constants.NUM_SUITS - numVoids);
			
			int suitsTakenByPlayer[] = SimSetupUtils.getNumCardsOfEachSuitTakenByPlayer(voidSuit, suitArrayForEachNonVoidSuit);
			
			int numCardsPerSuitAfterTake[] = SimSetupUtils.getCardsPerSuitRemainingAfterTake(numUnknownCardsPerSuit, suitsTakenByPlayer);
			
			selectedPartitionAndIndexToFillIn.setSuitsTakenByPlayers(playerIndex, suitsTakenByPlayer);
			
			
			//Recursively fill in partition info for next player:
			long indexFromStartOfCombo = (comboIndexNumber - curNumWaysDepth2[curIndexDepth1][suitPartitionIterIndexToUse]);

			
			long numWaysToFillInCardsForCurrentPlayer = 1;
			for(int i=0; i<Constants.NUM_SUITS; i++) {
				numWaysToFillInCardsForCurrentPlayer *= SimSetupUtils.getCombination(numUnknownCardsPerSuit[i], suitsTakenByPlayer[i]);
				
			}
			
			long numWaysToSetupNextPlayerShortCut = -1;
			
			if(suitPartitionIterIndexToUse + 1 < curNumWaysDepth2[curIndexDepth1].length) {
				numWaysToSetupNextPlayerShortCut = (curNumWaysDepth2[curIndexDepth1][suitPartitionIterIndexToUse + 1]
													- curNumWaysDepth2[curIndexDepth1][suitPartitionIterIndexToUse]) 
					                                     / numWaysToFillInCardsForCurrentPlayer;
			} else {
				numWaysToSetupNextPlayerShortCut = (numWaysToSimulate
						- curNumWaysDepth2[curIndexDepth1][suitPartitionIterIndexToUse]) 
                             / numWaysToFillInCardsForCurrentPlayer;
				
				//System.err.println("Reached the end of the line number 2!");
				//System.err.println("This could happen without being rigged! Try running test case: Michael2021-2 -> testcase3874.txt");
				//System.err.println("I also managed to force the issue with debug case testcase7014.txt, and it seems to work!");
				//System.err.println("To reach this code, I made the simulator test debug case testcase7014.txt, while rigging things in my favour.");
				//System.err.println("In other words, I gave current player no spade, made LHS be void in Spade, made current player the first bidder, and rigged it so it distributes the last combination.");

			}
			
			if(suitPartitionIterIndexToUse + 1 < curNumWaysDepth2[curIndexDepth1].length
					&& (curNumWaysDepth2[curIndexDepth1][suitPartitionIterIndexToUse + 1]
							- curNumWaysDepth2[curIndexDepth1][suitPartitionIterIndexToUse]) % numWaysToFillInCardsForCurrentPlayer != 0) {
				System.err.println("ERROR: numWaysToSetupNextPlayerShortCut is not right (it doesn't divide cleanly)");
				System.exit(1);
			
			}
			//Assume max 4-player game:
			if(numWaysToSetupNextPlayerShortCut != 1) {
				System.err.println(numWaysToSetupNextPlayerShortCut);
				System.err.println(1);
				System.err.println("ERROR: numWaysToSetupNextPlayerShortCut is not right when player index is 2 (assuming a 4-player game)");
				System.exit(1);
			}
			
			
			long currentPlayerComboNumber = indexFromStartOfCombo / numWaysToSetupNextPlayerShortCut;
			long nextComboIndexNumber = indexFromStartOfCombo % numWaysToSetupNextPlayerShortCut;
			
			selectedPartitionAndIndexToFillIn.setPlayerComboNumber(playerIndex, currentPlayerComboNumber);
			
			curIndexDepth2 = suitPartitionIterIndexToUse;
			
			return getSelectedPartitionAndIndexBasedOnCombinationIndex(numCardsPerSuitAfterTake, numSpacesAvailPerPlayer, originalIsVoidList, nextComboIndexNumber, playerIndex + 1, selectedPartitionAndIndexToFillIn, numWaysToSetupNextPlayerShortCut);

			
		} else {
			System.err.println("At this point, you're playing a 5+ player game!");
			System.err.println("And I don't feel like supporting it!");
			System.exit(1);
			
			return null;
		}
		
		
	}

	public static int binarySearchForSuitComboForDepthN(long array[], long comboIndexNumber) {
		return binarySearchForSuitComboForDepthN(array, comboIndexNumber, 0, array.length);
	}
	
	private static int binarySearchForSuitComboForDepthN(long array[], long comboIndexNumber, int min, int max) {
		int mid = (max + min)/2;
		long comboSumAtMid = array[mid];
		
		if(mid == min) {
			return binarySearchForFirstComboWithASolutionForDepth0(array, mid, max);
		}
		
		if(comboIndexNumber < comboSumAtMid) {
			return binarySearchForSuitComboForDepthN(array, comboIndexNumber, min, mid);
		
		} else {
		
			return binarySearchForSuitComboForDepthN(array, comboIndexNumber, mid, max);
		}	
	}
	
	private static int binarySearchForFirstComboWithASolutionForDepth0(long array[], int start, int max) {
		long numWays = array[start];
		int mid = (start + max)/2;
		
		if(start == mid) {
			return mid;
		}
		
		if(mid == numWays) {
			return binarySearchForFirstComboWithASolutionForDepth0(array, mid, max);
		} else {
			return binarySearchForFirstComboWithASolutionForDepth0(array, start, mid);
		}
		
	}
	
	
}

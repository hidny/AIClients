package mellow.ai.simulation;

import java.util.Random;

import mellow.Constants;
import mellow.ai.cardDataModels.impl.BooleanTableDataModel;
import mellow.cardUtils.CardStringFunctions;

public class SimulationTests {

	public static void main(String[] args) {
		System.out.println("start");
		BooleanTableDataModel dataModel = new BooleanTableDataModel();
		setupSimulationTest1(dataModel);
		setupSimulationTest2(dataModel);
		setupSimulationTest3(dataModel);
		setupSimulationTest4(dataModel);
		setupSimulationTest5(dataModel);
		setupSimulationTest7(dataModel);
		setupSimulationTest8();
		setupSimulationTest9();
		setupSimulationTest10();

		System.out.println("Test get Combinations array:");
		testComboIndexToArrayFunctions();
		
		System.out.println("Test get Combinations array edge cases:");
		compareComboNumToArrayFunctionsEdgeCases();
		
		javaNullTest();
		
		System.out.println("Test get random number function hack/fix:");
		testGetRandNumberFrom0ToN(HAS_BIAS);
		testGetRandNumberFrom0ToN(NO_BIAS);
		
		System.out.println("Test serving the cards:");
		testServeCarsdsBasedOnPartitionAndIndexInfo();
		testServeCarsdsBasedOnPartitionAndIndexInfo2();
		testServeCarsdsBasedOnPartitionAndIndexInfo3();
		testServeCarsdsBasedOnPartitionAndIndexInfo4();
		testServeCarsdsBasedOnPartitionAndIndexInfo5();
		testServeCarsdsBasedOnPartitionAndIndexInfo6();
		
		testProbabilityPartnerHasASorKS();
		

		testServeCarsdsBasedOnPartitionAndIndexInfo7();
		
	}
	
	public static void setupSimulationTest1(BooleanTableDataModel dataModel) {
		
		//Goal: return a possible arrangement of the original cards given what the current AI knows.
		
		//step 1: Get unknown cards by suit
		int numUnknownCardsPerSuit[] = new int[Constants.NUM_SUITS];
		numUnknownCardsPerSuit[0] = 1;
		numUnknownCardsPerSuit[1] = 2;
		numUnknownCardsPerSuit[2] = 0;
		numUnknownCardsPerSuit[3] = 0;
		
		//step 2: Get available spaces by player
		int numSpacesAvailPerPlayer[] = new int[Constants.NUM_PLAYERS];
		numSpacesAvailPerPlayer[0] = 0;
		numSpacesAvailPerPlayer[1] = 1;
		numSpacesAvailPerPlayer[2] = 1;
		numSpacesAvailPerPlayer[3] = 1;
		
		if(numSpacesAvailPerPlayer[0] != 0) {
			System.err.println("ERROR: somehow, the current player doesn't know their own cards.");
			System.exit(1);
		}
		
		//step 3: get # of voids per player
		
		boolean originalIsVoidList[][] = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		
		long answer = SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList);
		
		System.out.println("answer (prediction: 6): " + answer);
		
	}
	

	public static void setupSimulationTest2(BooleanTableDataModel dataModel) {
		
		//Goal: return a possible arrangement of the original cards given what the current AI knows.
		
		//step 1: Get unknown cards by suit
		int numUnknownCardsPerSuit[] = new int[Constants.NUM_SUITS];
		numUnknownCardsPerSuit[0] = 9;
		numUnknownCardsPerSuit[1] = 9;
		numUnknownCardsPerSuit[2] = 9;
		numUnknownCardsPerSuit[3] = 12;
		
		//step 2: Get available spaces by player
		int numSpacesAvailPerPlayer[] = new int[Constants.NUM_PLAYERS];
		numSpacesAvailPerPlayer[0] = 0;
		numSpacesAvailPerPlayer[1] = 13;
		numSpacesAvailPerPlayer[2] = 13;
		numSpacesAvailPerPlayer[3] = 13;
		
		if(numSpacesAvailPerPlayer[0] != 0) {
			System.err.println("ERROR: somehow, the current player doesn't know their own cards.");
			System.exit(1);
		}
		
		int sumUnknownCards1 = 0;
		int sumUnknownCards2 = 0;
		for(int i=0; i<Constants.NUM_SUITS; i++) {
			sumUnknownCards1 += numUnknownCardsPerSuit[i];
		}
		for(int i=0; i<Constants.NUM_PLAYERS; i++) {
			sumUnknownCards2 += numSpacesAvailPerPlayer[i];
		}
		if(sumUnknownCards1 != sumUnknownCards2) {
			System.err.println("ERROR: sum unknown cards isn\'t consistent");
			System.exit(1);
		}
		
		//step 3: get # of voids per player
		
		boolean originalIsVoidList[][] = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		
		long answer = SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList);
		
		System.out.println("answer (prediction: (39 choose 26) times (26 choose 13): (84 478 098 072 866 400) " + answer);
	}
	
	public static void setupSimulationTest3(BooleanTableDataModel dataModel) {
		
		//Goal: return a possible arrangement of the original cards given what the current AI knows.
		//step 1: Get unknown cards by suit
		int numUnknownCardsPerSuit[] = new int[Constants.NUM_SUITS];
		numUnknownCardsPerSuit[0] = 7;
		numUnknownCardsPerSuit[1] = 8;
		numUnknownCardsPerSuit[2] = 9;
		numUnknownCardsPerSuit[3] = 6;
		
		//step 2: Get available spaces by player
		int numSpacesAvailPerPlayer[] = new int[Constants.NUM_PLAYERS];
		numSpacesAvailPerPlayer[0] = 0;
		numSpacesAvailPerPlayer[1] = 10;
		numSpacesAvailPerPlayer[2] = 11;
		numSpacesAvailPerPlayer[3] = 9;
		
		if(numSpacesAvailPerPlayer[0] != 0) {
			System.err.println("ERROR: somehow, the current player doesn't know their own cards.");
			System.exit(1);
		}
		
		int sumUnknownCards1 = 0;
		int sumUnknownCards2 = 0;
		for(int i=0; i<Constants.NUM_SUITS; i++) {
			sumUnknownCards1 += numUnknownCardsPerSuit[i];
		}
		for(int i=0; i<Constants.NUM_PLAYERS; i++) {
			sumUnknownCards2 += numSpacesAvailPerPlayer[i];
		}
		if(sumUnknownCards1 != sumUnknownCards2) {
			System.err.println("ERROR: sum unknown cards isn\'t consistent");
			System.exit(1);
		}
		
		//step 3: get # of voids per player
		boolean originalIsVoidList[][] = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		
		long answer = SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList);
		
		System.out.println("answer (prediction: (30 choose 11) times (19 choose 10): (5 046 360 719 400) " + answer);
		
	}
	
	public static void setupSimulationTest4(BooleanTableDataModel dataModel) {
		
		//Goal: return a possible arrangement of the original cards given what the current AI knows.
		
		//step 1: Get unknown cards by suit
		int numUnknownCardsPerSuit[] = new int[Constants.NUM_SUITS];
		numUnknownCardsPerSuit[0] = 1;
		numUnknownCardsPerSuit[1] = 0;
		numUnknownCardsPerSuit[2] = 0;
		numUnknownCardsPerSuit[3] = 2;
		
		//step 2: Get available spaces by player
		int numSpacesAvailPerPlayer[] = new int[Constants.NUM_PLAYERS];
		numSpacesAvailPerPlayer[0] = 0;
		numSpacesAvailPerPlayer[1] = 1;
		numSpacesAvailPerPlayer[2] = 1;
		numSpacesAvailPerPlayer[3] = 1;
		
		if(numSpacesAvailPerPlayer[0] != 0) {
			System.err.println("ERROR: somehow, the current player doesn't know their own cards.");
			System.exit(1);
		}
		
		int sumUnknownCards1 = 0;
		int sumUnknownCards2 = 0;
		for(int i=0; i<Constants.NUM_SUITS; i++) {
			sumUnknownCards1 += numUnknownCardsPerSuit[i];
		}
		for(int i=0; i<Constants.NUM_PLAYERS; i++) {
			sumUnknownCards2 += numSpacesAvailPerPlayer[i];
		}
		if(sumUnknownCards1 != sumUnknownCards2) {
			System.err.println("ERROR: sum unknown cards isn\'t consistent");
			System.exit(1);
		}
		
		//step 3: get # of voids per player
		boolean originalIsVoidList[][] = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		originalIsVoidList[1][0] = true;
		
		long answer = SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList);
		
		System.out.println("answer (prediction: 4) " + answer);
		
	}
	
	public static void setupSimulationTest5(BooleanTableDataModel dataModel) {
		
		//Goal: return a possible arrangement of the original cards given what the current AI knows.
		
		//step 1: Get unknown cards by suit
		int numUnknownCardsPerSuit[] = new int[Constants.NUM_SUITS];
		numUnknownCardsPerSuit[0] = 9;
		numUnknownCardsPerSuit[1] = 13;
		numUnknownCardsPerSuit[2] = 10;
		numUnknownCardsPerSuit[3] = 7;
		
		//step 2: Get available spaces by player
		int numSpacesAvailPerPlayer[] = new int[Constants.NUM_PLAYERS];
		numSpacesAvailPerPlayer[0] = 0;
		numSpacesAvailPerPlayer[1] = 13;
		numSpacesAvailPerPlayer[2] = 13;
		numSpacesAvailPerPlayer[3] = 13;
		
		if(numSpacesAvailPerPlayer[0] != 0) {
			System.err.println("ERROR: somehow, the current player doesn't know their own cards.");
			System.exit(1);
		}
		
		int sumUnknownCards1 = 0;
		int sumUnknownCards2 = 0;
		for(int i=0; i<Constants.NUM_SUITS; i++) {
			sumUnknownCards1 += numUnknownCardsPerSuit[i];
		}
		for(int i=0; i<Constants.NUM_PLAYERS; i++) {
			sumUnknownCards2 += numSpacesAvailPerPlayer[i];
		}
		if(sumUnknownCards1 != sumUnknownCards2) {
			System.err.println("ERROR: sum unknown cards isn\'t consistent");
			System.exit(1);
		}
		
		//step 3: get # of voids per player
		boolean originalIsVoidList[][] = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		originalIsVoidList[2][1] = true;
		
		long answer = SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList);
		
		System.out.println("answer (prediction: (26 choose 13) * (26 choose 13) = 108172480360000) " + answer);
		
	}
	
	
	public static void setupSimulationTest7(BooleanTableDataModel dataModel) {
	
		//Goal: return a possible arrangement of the original cards given what the current AI knows.
		
		//step 1: Get unknown cards by suit
		int numUnknownCardsPerSuit[] = new int[Constants.NUM_SUITS];
		numUnknownCardsPerSuit[0] = 1;
		numUnknownCardsPerSuit[1] = 1;
		numUnknownCardsPerSuit[2] = 1;
		numUnknownCardsPerSuit[3] = 0;
		
		//step 2: Get available spaces by player
		int numSpacesAvailPerPlayer[] = new int[Constants.NUM_PLAYERS];
		numSpacesAvailPerPlayer[0] = 0;
		numSpacesAvailPerPlayer[1] = 1;
		numSpacesAvailPerPlayer[2] = 1;
		numSpacesAvailPerPlayer[3] = 1;
		
		if(numSpacesAvailPerPlayer[0] != 0) {
			System.err.println("ERROR: somehow, the current player doesn't know their own cards.");
			System.exit(1);
		}
		
		//step 3: get # of voids per player
		boolean originalIsVoidList[][] = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		originalIsVoidList[1][0] = true;
		originalIsVoidList[2][1] = true;
		
		long answer = SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList);
		
		System.out.println("answer (prediction: 3): " + answer);
		
	}

	public static void setupSimulationTest8() {
		int ret[] = CardStringFunctions.organizeCardsBySuit(new String[]{"AS", "AH", "KH", "QH", "KS", "2C", "2S", "2H"});
		
		System.out.println("Spade (3) " + ret[0]);
		System.out.println("Hearts (4) " + ret[1]);
		System.out.println("Clubs (1) " + ret[2]);
		System.out.println("Diamonds (0) " + ret[3]);
		
	}
		
	//Test getSelectedPartitionAndIndex by going thru all 6 combos (all 3! permutations):
	public static void setupSimulationTest9() {
		
		String unknownCards[] = new String[]{"AS", "AH", "KH"};
		
		int numUnknownCardsPerSuit[] = CardStringFunctions.organizeCardsBySuit(unknownCards);
		
		//step 2: Get available spaces by player
		int numSpacesAvailPerPlayer[] = new int[Constants.NUM_PLAYERS];
		numSpacesAvailPerPlayer[0] = 0;
		numSpacesAvailPerPlayer[1] = 1;
		numSpacesAvailPerPlayer[2] = 1;
		numSpacesAvailPerPlayer[3] = 1;
		
		boolean originalIsVoidList[][] = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		
		
		for(int i=0; i<SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList); i++) {
			SelectedPartitionAndIndex test = SimulationSetup.getSelectedPartitionAndIndexBasedOnCombinationIndex(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, i);
			
			System.out.println("Suits for combo #" + i);
			for(int j=0; j<test.suitsTakenByPlayer.length; j++) {
				for(int k=0; k<test.suitsTakenByPlayer[0].length; k++) {
					System.out.print(test.suitsTakenByPlayer[j][k] + "  ");
				}
				System.out.println();
			}
			
			
		}
		
		for(int i=0; i<SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList); i++) {
			SelectedPartitionAndIndex test = SimulationSetup.getSelectedPartitionAndIndexBasedOnCombinationIndex(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, i);
			
			System.out.println("Suits for combo #" + i);
			for(int j=0; j<test.suitsTakenByPlayer.length; j++) {
				for(int k=0; k<test.suitsTakenByPlayer[0].length; k++) {
					System.out.print(test.suitsTakenByPlayer[j][k] + "  ");
				}
				System.out.println("(index: " + test.comboIndex[j]+") ");
			}	
		}
		
		System.out.println();
		System.out.println();
	}
		
	//Test getSelectedPartitionAndIndex by going thru all 4 combos (Test with player 2 being void in spades)
	public static void setupSimulationTest10() {
		
		System.out.println("Test Simulation 10 (4 combos)");
		
		String unknownCards[] = new String[]{"AS", "AH", "KH"};
		
		int numUnknownCardsPerSuit[] = CardStringFunctions.organizeCardsBySuit(unknownCards);
		
		//step 2: Get available spaces by player
		int numSpacesAvailPerPlayer[] = new int[Constants.NUM_PLAYERS];
		numSpacesAvailPerPlayer[0] = 0;
		numSpacesAvailPerPlayer[1] = 1;
		numSpacesAvailPerPlayer[2] = 1;
		numSpacesAvailPerPlayer[3] = 1;
		
		boolean originalIsVoidList[][] = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		originalIsVoidList[2][0] = true;
		

		int numWays = (int)(SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList));
		
		System.out.println("Num ways found: " + numWays);
		for(int i=0; i<numWays; i++) {
			SelectedPartitionAndIndex test = SimulationSetup.getSelectedPartitionAndIndexBasedOnCombinationIndex(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, i);
			
			System.out.println("Suits for combo #" + i);
			for(int j=0; j<test.suitsTakenByPlayer.length; j++) {
				for(int k=0; k<test.suitsTakenByPlayer[0].length; k++) {
					System.out.print(test.suitsTakenByPlayer[j][k] + "  ");
				}
				System.out.println("(index: " + test.comboIndex[j]+") ");
			}	
		}
		
		System.out.println();
		System.out.println();
	}


	public static void testComboIndexToArrayFunctions() {
		//just look at 5 choose 2 = 10...
		
		boolean error = false;
		for(int i=0; i<SimulationSetup.getCombination(5, 2); i++) {
			boolean test1[] = convertComboNumberToArrayTestVersion(5, 2, i);
			boolean test2[] = SimulationSetup.convertComboNumberToArray(5, 2, i);
			
			for(int j=0; j<test1.length || j<test2.length; j++) {
				if(test1[j] != test2[j]) {
					error = true;
					if(test2[j]) {
						System.out.print("1");
					} else {
						System.out.print("0");
					}
				}
				if(test1[j]) {
					System.out.print("1 ");
				} else {
					System.out.print("0 ");
				}
			}
			System.out.println();
			
			
		}
		if(error) {
			System.err.println("ComboNumToArrayFunction failed");
			System.exit(1);
		}
		
	}
		
	public static void compareComboNumToArrayFunctionsEdgeCases() {
		//look at 5 choose 0 = 1 ..
		
		int testArray[][] = new int[][] {{ 5,0}, {5,5}, {0,0}, {5,1}};
		
		boolean error = false;
		
		for(int t=0; t<testArray.length; t++) {
			for(int i=0; i<SimulationSetup.getCombination(testArray[t][0], testArray[t][1]); i++) {
				boolean test1[] = convertComboNumberToArrayTestVersion(testArray[t][0], testArray[t][1], i);
				boolean test2[] = SimulationSetup.convertComboNumberToArray(testArray[t][0], testArray[t][1], i);
				
				for(int j=0; j<test1.length || j<test2.length; j++) {
					if(test1[j] != test2[j]) {
						error = true;
						if(test2[j]) {
							System.out.print("1");
						} else {
							System.out.print("0");
						}
					}
					if(test1[j]) {
						System.out.print("1 ");
					} else {
						System.out.print("0 ");
					}
				}
				System.out.println();
				
				
			}
			if(error) {
				System.err.println("ComboNumToArrayFunction failed for test " + t);
				System.exit(1);
			}
		}
		
	}
	
	public static void javaNullTest() {
		System.out.println("Java null test:");
		if(null == null) {
			System.out.println("nulls are equal");
		} else {
			System.out.println("ERROR: nulls are not equal");
		}
	}
	
	//Inefficient and easy to write function created for testing purposes:
	//The combo num to array mapping function has been optimized. This version could check up to a max of 13 choose 6 combos ( around 1800 combos) before finding the right array.
	//pre: comboNumber < # of combos.
	public static boolean[] convertComboNumberToArrayTestVersion(int numUnknownCardsInSuit, int numCardsToTake, int comboNumber) {

		if(numUnknownCardsInSuit < numCardsToTake) {
			System.err.println("ERROR: trying to create impossible combo in convertComboNumberToArray");
			System.exit(1);
		}

		boolean ret[] = new boolean[numUnknownCardsInSuit];
		for(int i=0; i<ret.length; i++) {
			ret[i] = (i < numCardsToTake);
		}
		
		for(int i=0; i<comboNumber; i++) {
			ret = SimulationSetup.getNextCombination(ret);
			
		}
		
		return ret;
	}
	
	private static int HAS_BIAS = 1;
	private static int NO_BIAS = 0;
		
	public static void testGetRandNumberFrom0ToN(int functionType) {
		
		long testNum = 4*(Long.MAX_VALUE/5);
		long lowBias = Long.MAX_VALUE % testNum;
		long highBias = (Long.MIN_VALUE % testNum) +testNum;
		
		//System.out.println("Test number: " + testNum);
		//System.out.println("Low Bias:    " + lowBias);
		//System.out.println("High Bias:   " + highBias);
		
		long numOutliers = (lowBias + 1) + (testNum-highBias);
		
		long numGotOutlier = 0;
		
		
		int NUM_TESTS = 100000;
		for(int i=0; i<NUM_TESTS; i++) {
			long randNum = 0;
			if(functionType == NO_BIAS) {
				randNum = SimulationSetup.getRandNumberFrom0ToN(testNum);
			} else {
				randNum = getRandNumberFrom0ToNTestBad(testNum);
			}
			
			if(randNum <= lowBias || randNum >= highBias) {
				numGotOutlier++;
			}
		}
		
		//Prediction
		double outlierExpectedLikelyhood = (1.0 *numOutliers) / (1.0 * testNum);
		double outlierActualLikelyhood = (1.0 * numGotOutlier)/NUM_TESTS;
		
		if(functionType == NO_BIAS) {
			System.out.println("Testing get random number without bias:");
			
		} else {
			System.out.println("Testing get random number with bias:");
		}
		System.out.println("Expected: " + outlierExpectedLikelyhood);
		System.out.println("Actual: " + outlierActualLikelyhood);
		System.out.println();
		
	}
	
	public static Random random = new Random();
	
	public static long getRandNumberFrom0ToNTestBad(long numWays) {
		//This function should have bias...
		long tmp = random.nextLong();
		long randIndexNumber = tmp % numWays;
		
		if(randIndexNumber <0 ) {
			randIndexNumber += numWays;
			//System.out.println("Test negative random.");
		}
		return randIndexNumber;
		
	}
	
	//Test getSelectedPartitionAndIndex by going thru all 4 combos?
	public static void testServeCarsdsBasedOnPartitionAndIndexInfo() {
		
		String unknownCards[] = new String[]{"AS", "AH", "KH"};
		
		int numUnknownCardsPerSuit[] = CardStringFunctions.organizeCardsBySuit(unknownCards);
		
		//step 2: Get available spaces by player
		int numSpacesAvailPerPlayer[] = new int[Constants.NUM_PLAYERS];
		numSpacesAvailPerPlayer[0] = 0;
		numSpacesAvailPerPlayer[1] = 1;
		numSpacesAvailPerPlayer[2] = 1;
		numSpacesAvailPerPlayer[3] = 1;
		
		boolean originalIsVoidList[][] = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		originalIsVoidList[2][0] = true;
		
		for(int i=0; i<SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList); i++) {
			SelectedPartitionAndIndex test = SimulationSetup.getSelectedPartitionAndIndexBasedOnCombinationIndex(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, i);
			
			System.out.println("Suits for combo #" + i);
			for(int j=0; j<test.suitsTakenByPlayer.length; j++) {
				for(int k=0; k<test.suitsTakenByPlayer[0].length; k++) {
					System.out.print(test.suitsTakenByPlayer[j][k] + "  ");
				}
				System.out.println();
			}
			
			
		}
		
		for(int comboInd=0; comboInd<SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList); comboInd++) {
			SelectedPartitionAndIndex suitPartitionsAndComboNumbers = SimulationSetup.getSelectedPartitionAndIndexBasedOnCombinationIndex(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, comboInd);
			
			String ret[][] = SimulationSetup.serveCarsdsBasedOnPartitionAndIndexInfo(suitPartitionsAndComboNumbers, unknownCards, numSpacesAvailPerPlayer);
			System.out.println("Unknown card distribution for combo #" + comboInd);
			
			for(int i=0; i<ret.length; i++) {
				System.out.print("player " + i + ":  ");
				for(int j=0; j<ret[i].length; j++) {
					System.out.print(ret[i][j] + " ");
				}
				System.out.println("");
			}
			System.out.println("");
		}
		
		System.out.println();
		System.out.println();
	}

	//Test getSelectedPartitionAndIndex by going thru all 4 combinations?
	public static void testServeCarsdsBasedOnPartitionAndIndexInfo2() {
		
		String unknownCards[] = new String[]{"AS", "KS", "QS", "JS", "AH", "KH"};
		
		int numUnknownCardsPerSuit[] = CardStringFunctions.organizeCardsBySuit(unknownCards);
		
		//step 2: Get available spaces by player
		int numSpacesAvailPerPlayer[] = new int[Constants.NUM_PLAYERS];
		numSpacesAvailPerPlayer[0] = 0;
		numSpacesAvailPerPlayer[1] = 2;
		numSpacesAvailPerPlayer[2] = 2;
		numSpacesAvailPerPlayer[3] = 2;
		
		boolean originalIsVoidList[][] = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		originalIsVoidList[2][0] = true;
		
		for(int i=0; i<SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList); i++) {
			SelectedPartitionAndIndex test = SimulationSetup.getSelectedPartitionAndIndexBasedOnCombinationIndex(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, i);
			
			System.out.println("Suits for combo #" + i);
			for(int j=0; j<test.suitsTakenByPlayer.length; j++) {
				for(int k=0; k<test.suitsTakenByPlayer[j].length; k++) {
					System.out.print(test.suitsTakenByPlayer[j][k] + "  ");
				}
				System.out.println(" (combo index: " + test.comboIndex[j] + ")");
			}
			
			
		}
		
		System.out.println("TEST");
		System.out.println("Number of ways to do this (expected: 6): " + SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList));
		System.out.println("Make sure everything found is diffent");
		
		for(int comboInd=0; comboInd<SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList); comboInd++) {
			SelectedPartitionAndIndex suitPartitionsAndComboNumbers = SimulationSetup.getSelectedPartitionAndIndexBasedOnCombinationIndex(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, comboInd);
			
			String ret[][] = SimulationSetup.serveCarsdsBasedOnPartitionAndIndexInfo(suitPartitionsAndComboNumbers, unknownCards, numSpacesAvailPerPlayer);
			System.out.println("Unknown card distribution for combo #" + comboInd);
			
			for(int i=0; i<ret.length; i++) {
				System.out.print("player " + i + ":  ");
				for(int j=0; j<ret[i].length; j++) {
					System.out.print(ret[i][j] + " ");
					
				}
				System.out.println("");
			}
			System.out.println("");
		}
		
		System.out.println();
		System.out.println();
	}
	
	
	public static void testServeCarsdsBasedOnPartitionAndIndexInfo3() {
		
		System.out.println("Test case where there's no unknown cards");
		String unknownCards[] = new String[]{};
		
		int numUnknownCardsPerSuit[] = CardStringFunctions.organizeCardsBySuit(unknownCards);
		
		//step 2: Get available spaces by player
		int numSpacesAvailPerPlayer[] = new int[Constants.NUM_PLAYERS];
		numSpacesAvailPerPlayer[0] = 0;
		numSpacesAvailPerPlayer[1] = 0;
		numSpacesAvailPerPlayer[2] = 0;
		numSpacesAvailPerPlayer[3] = 0;
		
		boolean originalIsVoidList[][] = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		originalIsVoidList[2][0] = true;
		
		System.out.println("Number of ways to do this (expected: 1): " + SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList));
		
		for(int comboInd=0; comboInd<SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList); comboInd++) {
			SelectedPartitionAndIndex suitPartitionsAndComboNumbers = SimulationSetup.getSelectedPartitionAndIndexBasedOnCombinationIndex(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, comboInd);
			
			String ret[][] = SimulationSetup.serveCarsdsBasedOnPartitionAndIndexInfo(suitPartitionsAndComboNumbers, unknownCards, numSpacesAvailPerPlayer);
			System.out.println("Unknown card distribution for combo #" + comboInd);
			
			for(int i=0; i<ret.length; i++) {
				System.out.print("player " + i + ":  ");
				for(int j=0; j<ret[i].length; j++) {
					System.out.print(ret[i][j] + " ");
					
				}
				System.out.println("");
			}
			System.out.println("");
		}
		
		System.out.println();
		System.out.println();
	}
	
	
	public static void testServeCarsdsBasedOnPartitionAndIndexInfo4() {
		
		System.out.println("Test case where everyone gets 2 cards");
		String unknownCards[] = new String[]{"AS", "KS", "AH", "KH", "AD", "KD"};
		
		int numUnknownCardsPerSuit[] = CardStringFunctions.organizeCardsBySuit(unknownCards);
		
		//step 2: Get available spaces by player
		int numSpacesAvailPerPlayer[] = new int[Constants.NUM_PLAYERS];
		numSpacesAvailPerPlayer[0] = 2;
		numSpacesAvailPerPlayer[1] = 0;
		numSpacesAvailPerPlayer[2] = 2;
		numSpacesAvailPerPlayer[3] = 2;
		
		boolean originalIsVoidList[][] = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		originalIsVoidList[2][0] = true;
		originalIsVoidList[3][0] = true;

		originalIsVoidList[0][1] = true;
		originalIsVoidList[3][1] = true;
		
		originalIsVoidList[2][3] = true;
		originalIsVoidList[0][3] = true;
		
		System.out.println("Number of ways to do this (expected: 1): " + SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList));
		
		for(int comboInd=0; comboInd<SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList); comboInd++) {
			SelectedPartitionAndIndex suitPartitionsAndComboNumbers = SimulationSetup.getSelectedPartitionAndIndexBasedOnCombinationIndex(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, comboInd);
			
			String ret[][] = SimulationSetup.serveCarsdsBasedOnPartitionAndIndexInfo(suitPartitionsAndComboNumbers, unknownCards, numSpacesAvailPerPlayer);
			System.out.println("Unknown card distribution for combo #" + comboInd);
			
			for(int i=0; i<ret.length; i++) {
				System.out.print("player " + i + ":  ");
				for(int j=0; j<ret[i].length; j++) {
					System.out.print(ret[i][j] + " ");
					
				}
				System.out.println("");
			}
			System.out.println("");
		}
		
		System.out.println();
		System.out.println();
	}
	
	public static void testServeCarsdsBasedOnPartitionAndIndexInfo5() {
		
		System.out.println("Test case where everyone grabs from 1 suit except for player 2");
		String unknownCards[] = new String[]{"AS", "KS", "QS", "JS", "TS", "9S", "8S", "7S", "6S", "5S", "4S", "3S", "2S", "2C", "3C"};
		
		int numUnknownCardsPerSuit[] = CardStringFunctions.organizeCardsBySuit(unknownCards);
		
		//step 2: Get available spaces by player
		int numSpacesAvailPerPlayer[] = new int[Constants.NUM_PLAYERS];
		numSpacesAvailPerPlayer[0] = 0;
		numSpacesAvailPerPlayer[1] = 5;
		numSpacesAvailPerPlayer[2] = 5;
		numSpacesAvailPerPlayer[3] = 5;
		
		boolean originalIsVoidList[][] = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		originalIsVoidList[1][2] = true;
		originalIsVoidList[3][2] = true;
		
		System.out.println("Number of ways to do this (expected: (13 choose 5)* (8 choose 5) = 72072): " + SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList));
		
		//TODO: Make sure every combination obtained thru the algorithm is different!
		for(int comboInd=0; comboInd<SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList); comboInd++) {
			SelectedPartitionAndIndex suitPartitionsAndComboNumbers = SimulationSetup.getSelectedPartitionAndIndexBasedOnCombinationIndex(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, comboInd);
			
			String ret[][] = SimulationSetup.serveCarsdsBasedOnPartitionAndIndexInfo(suitPartitionsAndComboNumbers, unknownCards, numSpacesAvailPerPlayer);
			//System.out.println("Unknown card distribution for combo #" + comboInd);
			
			for(int i=0; i<ret.length; i++) {
				//System.out.print("player " + i + ":  ");
				for(int j=0; j<ret[i].length; j++) {
					//System.out.print(ret[i][j] + " ");
					
				}
				//System.out.println("");
			}
			//System.out.println("");
		}
		
		System.out.println();
		System.out.println();
	}

	public static void testServeCarsdsBasedOnPartitionAndIndexInfo6() {
		
		//TODO: have a function to pair up next that does getSelectedPartitionAndIndexBasedOnCombinationIndex 
		//and getSelectedPartitionAndIndexBasedOnCombinationIndex and some prep
		
		System.out.println("Test case where 2 people are void that\'s in my notebook:");
		String unknownCards[] = new String[]{"2H", "7C", "9D"};
		
		int numUnknownCardsPerSuit[] = CardStringFunctions.organizeCardsBySuit(unknownCards);
		
		//step 2: Get available spaces by player
		int numSpacesAvailPerPlayer[] = new int[Constants.NUM_PLAYERS];
		numSpacesAvailPerPlayer[0] = 1;
		numSpacesAvailPerPlayer[1] = 0;
		numSpacesAvailPerPlayer[2] = 1;
		numSpacesAvailPerPlayer[3] = 1;
		
		boolean originalIsVoidList[][] = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		originalIsVoidList[0][Constants.HEART] = true;
		originalIsVoidList[2][Constants.CLUB] = true;

		System.out.println("Number of ways to do this (expected: 3): " + SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList));
		
		for(int comboInd=0; comboInd<SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList); comboInd++) {
			
			
			SelectedPartitionAndIndex suitPartitionsAndComboNumbers = SimulationSetup.getSelectedPartitionAndIndexBasedOnCombinationIndex(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, comboInd);
			String ret[][] = SimulationSetup.serveCarsdsBasedOnPartitionAndIndexInfo(suitPartitionsAndComboNumbers, unknownCards, numSpacesAvailPerPlayer);
			System.out.println("Unknown card distribution for combo #" + comboInd);
			
			for(int i=0; i<ret.length; i++) {
				System.out.print("player " + i + ":  ");
				for(int j=0; j<ret[i].length; j++) {
					System.out.print(ret[i][j] + " ");
					
				}
				System.out.println("");
			}
			System.out.println("");
		}
		
		System.out.println();
		System.out.println();
	}
	
	
	//Proof of concept: Could I use these functions to do probabilities?
	//Answer: YES!
	public static void testProbabilityPartnerHasASorKS() {
		
		System.out.println("Test case where I check if these function could figure out probabilities.");
		System.out.println("The probability my partner has the AS or KS is something I think about a lot in mellow, so let\'s test that!");
		
		//pretend current player has QS and a bunch of clubs...
		String unknownCards1[] = new String[]{"AS", "KS", "JS", "TS", "9S", "8S", "7S", "6S", "5S", "4S", "3S", "2S",
											"AH", "KH", "QH", "JH", "TH", "9H", "8H", "7H", "6H", "5H", "4H", "3H", "2H",
											"AD", "KD", "QD", "JD", "TD", "9D", "8D", "7D", "6D", "5D", "4D", "3D", "2D",
											"AC"};
		int numUnknownCardsPerSuit[] = CardStringFunctions.organizeCardsBySuit(unknownCards1);
		
		//step 2: Get available spaces by player
		int numSpacesAvailPerPlayer[] = new int[Constants.NUM_PLAYERS];
		numSpacesAvailPerPlayer[0] = 0;
		numSpacesAvailPerPlayer[1] = 13;
		numSpacesAvailPerPlayer[2] = 13;
		numSpacesAvailPerPlayer[3] = 13;
		
		boolean originalIsVoidList[][] = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		
		long totalNumWays = SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList);
		
		System.out.println("Number of ways to distribute cards (expected: (39 choose 13)* (26 choose 13) = 84 478 098 072 866 400): " + totalNumWays);
		
		//Case where partner has AS:
		//Same as unknownCards1 minus AS
		String unknownCards2[] = new String[]{"KS", "JS", "TS", "9S", "8S", "7S", "6S", "5S", "4S", "3S", "2S",
				"AH", "KH", "QH", "JH", "TH", "9H", "8H", "7H", "6H", "5H", "4H", "3H", "2H",
				"AD", "KD", "QD", "JD", "TD", "9D", "8D", "7D", "6D", "5D", "4D", "3D", "2D",
				"AC"};
		
		numUnknownCardsPerSuit = CardStringFunctions.organizeCardsBySuit(unknownCards2);
		
		//step 2: Get available spaces by player
		numSpacesAvailPerPlayer[2] = 12;
		
		long totalNumWaysAS = SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList);
		
		//Case where partner has AS and KS:
		//Same as unknownCards1 minus AS and KS
		String unknownCards3[] = new String[]{"JS", "TS", "9S", "8S", "7S", "6S", "5S", "4S", "3S", "2S",
				"AH", "KH", "QH", "JH", "TH", "9H", "8H", "7H", "6H", "5H", "4H", "3H", "2H",
				"AD", "KD", "QD", "JD", "TD", "9D", "8D", "7D", "6D", "5D", "4D", "3D", "2D",
				"AC"};
		
		System.out.println("Number of ways to distribute cards when partner has AS (expected: (38 choose 12)* (26 choose 13) = 28 159 366 024 288 800): " + totalNumWaysAS);
		
		numUnknownCardsPerSuit = CardStringFunctions.organizeCardsBySuit(unknownCards3);
		
		//step 2: Get available spaces by player
		numSpacesAvailPerPlayer[2] = 11;
		
		long totalNumWaysASKS = SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList);
		
		System.out.println("Number of ways to distribute cards when partner has AS and KS (expected: (37 choose 11)* (26 choose 13) = 8 892 431 376 091 200): " + totalNumWaysASKS);
		
		//use Principle of Inclusion and Exclusion and the fact that odds of AS is the same as KS.
		long numWaysPartnerASorKS = 2 * totalNumWaysAS - totalNumWaysASKS;
		
		double probPartnerASorKS = (1.0 * numWaysPartnerASorKS) / (1.0 * totalNumWays);
		System.out.println("Probability of partner having AS or KS: ");
		System.out.println("Expected: (approx 1-(2/3)^2 = .55) More exact: = 1-(26/39)*(25/38) = 0.56140...)");
		System.out.println("Actual: " + probPartnerASorKS);
		
		System.out.println();
		System.out.println();
	}

	
	//Here's a cool thing I found:
	public static void testServeCarsdsBasedOnPartitionAndIndexInfo7() {
		
		System.out.println("Test case where 3 players have N cards each of 3 different suits, and each player is void in a suit different than the other 2:");
		System.out.println("The number of ways to do this is equal to the Franel numbers a(n) = Sum_{k = 0..n} binomial(n,k)^3. (A000172)");
		
		int PLAYERS_UNKNOWN = 3;
		//getCombination(2*i, i)
		for(int i=0; i<=Constants.NUM_RANKS; i++) {

			String unknownCards[] = new String[PLAYERS_UNKNOWN*i];
			for(int j=0; j<PLAYERS_UNKNOWN; j++) {
				for(int k=0; k<i; k++) {
					unknownCards[j*i + k] = Constants.FULL_DECK[Constants.NUM_RANKS * j + k];
				}
			}
			
			int numUnknownCardsPerSuit[] = CardStringFunctions.organizeCardsBySuit(unknownCards);
			
			//step 2: Get available spaces by player
			int numSpacesAvailPerPlayer[] = new int[Constants.NUM_PLAYERS];
			numSpacesAvailPerPlayer[0] = 0;
			numSpacesAvailPerPlayer[1] = i;
			numSpacesAvailPerPlayer[2] = i;
			numSpacesAvailPerPlayer[3] = i;
			
			boolean originalIsVoidList[][] = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
			for(int j=0; j<3; j++) {
				originalIsVoidList[j+1][j] = true;
			}
			
			long expected =0L;
			for(int j=0; j<=i; j++) {
				expected += (long)Math.pow(SimulationSetup.getCombination(i, j), 3);
			}
			
			long actual = SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList);
	
			System.out.println("Number of ways to do this (expected: " + expected + "): " + actual);
			
			if(i < 3) {
				for(int comboInd=0; comboInd<SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList); comboInd++) {
					
					
					SelectedPartitionAndIndex suitPartitionsAndComboNumbers = SimulationSetup.getSelectedPartitionAndIndexBasedOnCombinationIndex(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, comboInd);
					String ret[][] = SimulationSetup.serveCarsdsBasedOnPartitionAndIndexInfo(suitPartitionsAndComboNumbers, unknownCards, numSpacesAvailPerPlayer);
					System.out.println("Unknown card distribution for combo #" + comboInd);
					
					for(int i1=0; i1<ret.length; i1++) {
						System.out.print("player " + i + ":  ");
						for(int j=0; j<ret[i1].length; j++) {
							System.out.print(ret[i1][j] + " ");
							
						}
						System.out.println("");
					}
					System.out.println("");
				}
			}

			if(expected != actual) {
				System.out.println("ERROR (in testServeCarsdsBasedOnPartitionAndIndexInfo7): number of ways players could have cards doesn't match what's predicted");
				System.exit(1);
			}
		}
	}
}
		

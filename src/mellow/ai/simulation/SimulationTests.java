package mellow.ai.simulation;

import java.util.Random;

import mellow.Constants;
import mellow.ai.cardDataModels.impl.BooleanTableDataModel;
import mellow.cardUtils.CardStringFunctions;

public class SimulationTests {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
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
		compareComboNumToArrayFunctions();
		
		System.out.println("Test get Combinations array edge cases:");
		compareComboNumToArrayFunctionsExtreme();
		
		javaNullTest();
		
		System.out.println("Test get random number function hack/fix:");
		testGetRandNumberFrom0ToN(HAS_BIAS);
		testGetRandNumberFrom0ToN(NO_BIAS);
		
		System.out.println("Test serving the cards:");
		testServeCarsdsBasedOnPartitionAndIndexInfo();
	}
	
	//TODO: save these tests somewhere
	public static void setupSimulationTest1(BooleanTableDataModel dataModel) {
		
		String cardsDealt[][] = new String[Constants.NUM_PLAYERS][Constants.NUM_STARTING_CARDS_IN_HAND];
		
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
		
		
		
		//TODO: efficient: start with player with 2 void suits...
		//meh: just do it

		int playerList[] = new int[Constants.NUM_PLAYERS - 1];
		playerList[0] = 3;
		playerList[1] = 2;
		playerList[2] = 1;
		
		boolean originalIsVoidList[][] = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		
		long answer = SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, playerList);
		
		System.out.println("answer (prediction: 6): " + answer);
		
	}
	

	//TODO: save these tests somewhere
	public static void setupSimulationTest2(BooleanTableDataModel dataModel) {
		
		String cardsDealt[][] = new String[Constants.NUM_PLAYERS][Constants.NUM_STARTING_CARDS_IN_HAND];
		
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
		
		
		//TODO: efficient: start with player with 2 void suits...
		//meh: just do it

		int playerList[] = new int[Constants.NUM_PLAYERS - 1];
		playerList[0] = 3;
		playerList[1] = 2;
		playerList[2] = 1;
		
		boolean originalIsVoidList[][] = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		
		long answer = SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, playerList);
		
		System.out.println("answer (prediction: (39 choose 26) times (26 choose 13): (84 478 098 072 866 400) " + answer);
		
	}
	
	

	//TODO: save these tests somewhere
	public static void setupSimulationTest3(BooleanTableDataModel dataModel) {
		
		String cardsDealt[][] = new String[Constants.NUM_PLAYERS][Constants.NUM_STARTING_CARDS_IN_HAND];
		
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
		
		
		
		//TODO: efficient: start with player with 2 void suits...
		//meh: just do it

		int playerList[] = new int[Constants.NUM_PLAYERS - 1];
		playerList[0] = 3;
		playerList[1] = 2;
		playerList[2] = 1;
		
		boolean originalIsVoidList[][] = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		
		long answer = SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, playerList);
		
		System.out.println("answer (prediction: (30 choose 11) times (19 choose 10): (5 046 360 719 400) " + answer);
		
	}
	
	//TODO: save these tests somewhere
	public static void setupSimulationTest4(BooleanTableDataModel dataModel) {
		
		String cardsDealt[][] = new String[Constants.NUM_PLAYERS][Constants.NUM_STARTING_CARDS_IN_HAND];
		
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
		
		
		
		//TODO: efficient: start with player with 2 void suits...
		//meh: just do it

		int playerList[] = new int[Constants.NUM_PLAYERS - 1];
		playerList[0] = 3;
		playerList[1] = 2;
		playerList[2] = 1;
		
		boolean originalIsVoidList[][] = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		originalIsVoidList[1][0] = true;
		
		long answer = SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, playerList);
		
		System.out.println("answer (prediction: 4) " + answer);
		
	}
	

		//TODO: save these tests somewhere
	public static void setupSimulationTest5(BooleanTableDataModel dataModel) {
		
		String cardsDealt[][] = new String[Constants.NUM_PLAYERS][Constants.NUM_STARTING_CARDS_IN_HAND];
		
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
		
		
		
		//TODO: efficient: start with player with 2 void suits...
		//meh: just do it

		int playerList[] = new int[Constants.NUM_PLAYERS - 1];
		playerList[0] = 3;
		playerList[1] = 2;
		playerList[2] = 1;
		
		boolean originalIsVoidList[][] = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		originalIsVoidList[2][1] = true;
		
		long answer = SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, playerList);
		
		System.out.println("answer (prediction: (26 choose 13) * (26 choose 13) = 108172480360000) " + answer);
		
	}
	
	
	//TODO: save these tests somewhere
	public static void setupSimulationTest7(BooleanTableDataModel dataModel) {
		
		String cardsDealt[][] = new String[Constants.NUM_PLAYERS][Constants.NUM_STARTING_CARDS_IN_HAND];
		
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
		
		
		
		//TODO: efficient: start with player with 2 void suits...
		//meh: just do it

		int playerList[] = new int[Constants.NUM_PLAYERS - 1];
		playerList[0] = 3;
		playerList[1] = 2;
		playerList[2] = 1;
		
		boolean originalIsVoidList[][] = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		
		originalIsVoidList[1][0] = true;
		originalIsVoidList[2][1] = true;
		long answer = SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, playerList);
		
		System.out.println("answer (prediction: 3): " + answer);
		
	}
	
	
	//TODO: save these tests somewhere
		public static void setupSimulationTest8() {
			int ret[] = SimulationSetup.getNumUnknownPerSuit(new String[]{"AS", "AH", "KH", "QH", "KS", "2C", "2S", "2H"});
			
			System.out.println("Spade (3) " + ret[0]);
			System.out.println("Hearts (4) " + ret[1]);
			System.out.println("Clubs (1) " + ret[2]);
			System.out.println("Diamonds (0) " + ret[3]);
			
			
			
		}
		
		//Test getSelectedPartitionAndIndex by going thru all 6 combos:
		public static void setupSimulationTest9() {
			
			String unknownCards[] = new String[]{"AS", "AH", "KH"};
			
			int numUnknownCardsPerSuit[] = SimulationSetup.getNumUnknownPerSuit(unknownCards);
			
			//step 2: Get available spaces by player
			int numSpacesAvailPerPlayer[] = new int[Constants.NUM_PLAYERS];
			numSpacesAvailPerPlayer[0] = 0;
			numSpacesAvailPerPlayer[1] = 1;
			numSpacesAvailPerPlayer[2] = 1;
			numSpacesAvailPerPlayer[3] = 1;
			
			boolean originalIsVoidList[][] = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
			
			int playerList[] = new int[Constants.NUM_PLAYERS - 1];
			playerList[0] = 1;
			playerList[1] = 2;
			playerList[2] = 3;
			
			
			for(int i=0; i<SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, playerList); i++) {
				SelectedPartitionAndIndex test = SimulationSetup.getSelectedPartitionAndIndexBasedOnRandIndex(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, playerList, i);
				
				System.out.println("Suits for combo #" + i);
				for(int j=0; j<test.suitsTakenByPlayer.length; j++) {
					for(int k=0; k<test.suitsTakenByPlayer[0].length; k++) {
						System.out.print(test.suitsTakenByPlayer[j][k] + "  ");
					}
					System.out.println();
				}
				
				
			}
			
			for(int i=0; i<SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, playerList); i++) {
				SelectedPartitionAndIndex test = SimulationSetup.getSelectedPartitionAndIndexBasedOnRandIndex(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, playerList, i);
				
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
		
		//Test getSelectedPartitionAndIndex by going thru all 4 combos (This has a void suit thing)
		public static void setupSimulationTest10() {
			
			System.out.println("Test Simulation 10 (4 combos)");
			
			String unknownCards[] = new String[]{"AS", "AH", "KH"};
			
			int numUnknownCardsPerSuit[] = SimulationSetup.getNumUnknownPerSuit(unknownCards);
			
			//step 2: Get available spaces by player
			int numSpacesAvailPerPlayer[] = new int[Constants.NUM_PLAYERS];
			numSpacesAvailPerPlayer[0] = 0;
			numSpacesAvailPerPlayer[1] = 1;
			numSpacesAvailPerPlayer[2] = 1;
			numSpacesAvailPerPlayer[3] = 1;
			
			boolean originalIsVoidList[][] = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
			originalIsVoidList[2][0] = true;
			
			int playerList[] = new int[Constants.NUM_PLAYERS - 1];
			playerList[0] = 1;
			playerList[1] = 2;
			playerList[2] = 3;
			

			
			for(int i=0; i<SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, playerList); i++) {
				SelectedPartitionAndIndex test = SimulationSetup.getSelectedPartitionAndIndexBasedOnRandIndex(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, playerList, i);
				
				System.out.println("Suits for combo #" + i);
				for(int j=0; j<test.suitsTakenByPlayer.length; j++) {
					for(int k=0; k<test.suitsTakenByPlayer[0].length; k++) {
						System.out.print(test.suitsTakenByPlayer[j][k] + "  ");
					}
					System.out.println();
				}
			}
			
			for(int i=0; i<SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, playerList); i++) {
				SelectedPartitionAndIndex test = SimulationSetup.getSelectedPartitionAndIndexBasedOnRandIndex(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, playerList, i);
				
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


		public static void compareComboNumToArrayFunctions() {
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
		
		public static void compareComboNumToArrayFunctionsExtreme() {
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
		
		//Bad and easy to write version of function:
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
			
			
			//(Long.MAX_VALUE - (Long.MAX_VALUE% numWays) <= tmp && Long.MIN_VALUE + numWays  + (Long.MIN_VALUE% numWays) -1 >= tmp
			
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
			
			int numUnknownCardsPerSuit[] = SimulationSetup.getNumUnknownPerSuit(unknownCards);
			
			//step 2: Get available spaces by player
			int numSpacesAvailPerPlayer[] = new int[Constants.NUM_PLAYERS];
			numSpacesAvailPerPlayer[0] = 0;
			numSpacesAvailPerPlayer[1] = 1;
			numSpacesAvailPerPlayer[2] = 1;
			numSpacesAvailPerPlayer[3] = 1;
			
			boolean originalIsVoidList[][] = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
			originalIsVoidList[2][0] = true;
			
			int playerList[] = new int[Constants.NUM_PLAYERS];
			playerList[0] = 1;
			playerList[1] = 2;
			playerList[2] = 3;
			playerList[3] = 0;
			
			
			for(int i=0; i<SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, playerList); i++) {
				SelectedPartitionAndIndex test = SimulationSetup.getSelectedPartitionAndIndexBasedOnRandIndex(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, playerList, i);
				
				System.out.println("Suits for combo #" + i);
				for(int j=0; j<test.suitsTakenByPlayer.length; j++) {
					for(int k=0; k<test.suitsTakenByPlayer[0].length; k++) {
						System.out.print(test.suitsTakenByPlayer[j][k] + "  ");
					}
					System.out.println();
				}
				
				
			}
			
			for(int comboInd=0; comboInd<SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, playerList); comboInd++) {
				SelectedPartitionAndIndex suitPartitionsAndComboNumbers = SimulationSetup.getSelectedPartitionAndIndexBasedOnRandIndex(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, playerList, comboInd);
				
				String ret[][] = SimulationSetup.serveCarsdsBasedOnPartitionAndIndexInfo(playerList, suitPartitionsAndComboNumbers, unknownCards, numSpacesAvailPerPlayer);
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
}
		

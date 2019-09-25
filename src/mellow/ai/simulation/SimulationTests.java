package mellow.ai.simulation;

import mellow.Constants;
import mellow.ai.cardDataModels.impl.BooleanTableDataModel;

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
		playerList[0] = 1;
		playerList[1] = 2;
		playerList[2] = 3;
		
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
		playerList[0] = 1;
		playerList[1] = 2;
		playerList[2] = 3;
		
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
		playerList[0] = 1;
		playerList[1] = 2;
		playerList[2] = 3;
		
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
		playerList[0] = 1;
		playerList[1] = 2;
		playerList[2] = 3;
		
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
		playerList[0] = 1;
		playerList[1] = 2;
		playerList[2] = 3;
		
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
		playerList[0] = 1;
		playerList[1] = 2;
		playerList[2] = 3;
		
		boolean originalIsVoidList[][] = new boolean[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		
		originalIsVoidList[1][0] = true;
		originalIsVoidList[2][1] = true;
		long answer = SimulationSetup.getNumberOfWaysToSimulate(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, playerList);
		
		System.out.println("answer (prediction: 3): " + answer);
		
	}
}

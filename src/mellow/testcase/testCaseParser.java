package mellow.testcase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.OutputStream;
import java.util.Scanner;

import mellow.ai.aiDecider.MellowAIDeciderFactory;
import mellow.ai.aiDecider.MellowAIDeciderInterface;

public class testCaseParser {

	public static void main(String[] args) {
		File list[]  = getTestCaseFiles();
		
		MellowAIDeciderInterface decider = MellowAIDeciderFactory.getAI(0, false);
		
		int numPasses = 0;
		int numTrials = list.length;
		
		for(int i=0; i<list.length; i++) {
			System.out.println("Testing: " + list[i].getName());
			
			if(runTestcase(decider, list[i], true) == TESTCASE_PASS) {
				numPasses++;
			}
			
			//Only show failed tests: (Assumes deterministic)
			/*if(runTestcase(decider, list[i], false) == TESTCASE_PASS) {
				numPasses++;
			} else {
				System.out.println("Showing failed test: " + list[i].getName());
				runTestcase(decider, list[i], true);
			}*/

		}
		
		System.out.println("Final report for " + decider + ":");
		
		System.out.println("AI passes " + numPasses + " out of " + numTrials);
		System.out.println("That's a " + String.format("%.2f", ((100.0*numPasses) /(1.0*numTrials))) + "% pass rate.");
		
	}
	
	public static File[] getTestCaseFiles() {
		
		//https://stackoverflow.com/questions/5694385/getting-the-filenames-of-all-files-in-a-folder
		
		//TODO: standardize this and add constant file
		File folder = new File("..\\TestCaseAndReplayData\\testcases\\Michael");
		
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
		  if (listOfFiles[i].isFile()) {
		    System.out.println("File " + listOfFiles[i].getName());
		    
		  } else if (listOfFiles[i].isDirectory()) {
		    System.out.println("Directory " + listOfFiles[i].getName());
		  }
		}
		System.out.println();
		System.out.println();
		
		return listOfFiles;
	}
	
	static final boolean TESTCASE_PASS = true;
	static final boolean TESTCASE_FAIL = false;
	
	static final int NUM_PLAYERS = 4;
	static final String TRUMP = "S";
	
	
	static PrintStream dummyStream = new PrintStream(new OutputStream(){
	    public void write(int b) {
	        // NO-OP
	    }
	});

	public static boolean runTestcase(MellowAIDeciderInterface decider, File testCaseFile) {
		return runTestcase(decider, testCaseFile, true);
	}
	//0 is fail
	//1 is pass
	//2+ is whatever I want it to be
	public static boolean runTestcase(MellowAIDeciderInterface decider, File testCaseFile, boolean verbose) {
		//https://stackoverflow.com/questions/8363493/hiding-system-out-print-calls-of-a-class

		PrintStream originalStream = System.out;
		
		if(verbose == false) {
			System.setOut(dummyStream);
		}
		
		boolean ret = false;
		
		decider.resetStateForNewRound();
		
		try {
			Scanner in = new Scanner(testCaseFile);
			
			String players[] = new String[4];
			for(int i =0; i<players.length; i++) {
				players[i] = "";
			}
			
			
			String cur;
			do {
				cur = in.nextLine();
			} while(cur.startsWith("Your name") == false);
			
			players[0] = cur.split(" ")[cur.split(" ").length - 1];
			System.out.println("Your name: " + players[0]);
			
			decider.setNameOfPlayers(players);
			
			do {
				cur = in.nextLine();
			} while(cur.startsWith("Dealer") == false && cur.contains("You are the dealer") == false);
			
			
			int dealerIndex = 0;
			
			if(cur.contains("You are the dealer")) {
				dealerIndex = 0;
			} else if(cur.contains("Dealer is on your left")) {
				dealerIndex = 1;
			} else if(cur.contains("Dealer is your partner opposite you.")) {
				dealerIndex = 2;
			} else if(cur.contains("Dealer is on your right")) {
				dealerIndex = 3;
			} else {
				System.out.println("ERROR: unknown dealer" + "\n");
				System.exit(1);
			}
			
			//System.out.println("DEBUG dealer index: " + dealerIndex);
			
			do {
				cur = in.nextLine();
			} while(cur.startsWith("Cards dealt:") == false);
			cur = in.nextLine();
			
			System.out.println("Cards dealt: \n" + cur);

			System.out.println("AI look at cards print statements --------");
			decider.setCardsForNewRound(cur.split(" "));
			System.out.println("****************************");
			
			do {
				cur = in.nextLine();
			} while(cur.startsWith("Bid history") == false);
			
			int actionIndex = dealerIndex;
			
			do {
				actionIndex = (actionIndex + 1) % NUM_PLAYERS;
				
				cur = in.nextLine();
				System.out.println(cur);
				
				if(cur.contains(" bid ") && cur.split(" ")[1].equals("bid")) {
					players[actionIndex] = cur.split(" ")[0];
					decider.setNameOfPlayers(players);
					
					int bid = -1;
					if(cur.split(" ")[2].toLowerCase().contains("m")) {
						bid = 0;
					} else {
						bid = Integer.parseInt(cur.split(" ")[2].replace(".", ""));
					}
					decider.receiveBid(players[actionIndex], bid);
					//System.out.println(players[actionIndex] + " " +  bid);
				}
				
			} while (actionIndex != dealerIndex && cur.equals("") == false);
			
			
			String response = "-1";
			
			//TODO: create helper functions:
			if(actionIndex != dealerIndex) {
				//Only handle bid
				System.out.println("AI handle bid print statements --------");
				response = decider.getBidToMake();
				System.out.println("****************************");
				if(response.toLowerCase().contains("m")) {
					response = "0";
				}

			} else {
				//handle actions
				
				do {
					cur = in.nextLine();
				} while(cur.startsWith("Play history") == false && cur.startsWith("Cards in hand:") == false);
				
				
				actionIndex = (dealerIndex + 1) % NUM_PLAYERS;
				
				int curNumCardsinFight = 0;
				String cardsInFight[] = new String[NUM_PLAYERS];
				int cardNumbersInFight[] = new int[NUM_PLAYERS];
				
				while(cur.equals("Cards in hand:") == false) {
					cur = in.nextLine();
				
					if(cur.equals("Cards in hand:") == false) {
						System.out.println(cur);
					}
					
					if(cur.contains(" played ")) {
						//decider.getPlayedCard(players[actionIndex], bid);
						cardsInFight[curNumCardsinFight] = cur.split(" ")[3].replace(".", "");
						cardNumbersInFight[curNumCardsinFight] = getCardNumber(cardsInFight[curNumCardsinFight]);
						
						decider.receiveCardPlayed(cur.split(" ")[0], cardsInFight[curNumCardsinFight]);
						curNumCardsinFight++;
						
						if(curNumCardsinFight >= NUM_PLAYERS) {
							
							actionIndex = fight(actionIndex, cardNumbersInFight);
							
							for(int i=0; i<4; i++) {
								cardsInFight[i]= "";
								cardNumbersInFight[i] = -1;
							}
							curNumCardsinFight = 0;
						}
					}
					
				}
				
				cur = in.nextLine();
				
				System.out.println("Cards left:");
				System.out.println(cur);

				System.out.println("AI handle action print statements --------");
				String play = decider.getCardToPlay();
				System.out.println("****************************");
				
				response = play;
				
			}
			
			if(response == null || response.trim().equals("")) {
				response = "FAIL: (LACK OF RESPONSE)";
			}
			

			
			do {
				cur = in.nextLine();
			} while(cur.startsWith("Expert response:") == false);

			cur = in.nextLine();
			String expertResponse = cur;
			
			do {
				cur = in.nextLine();
			} while(cur.startsWith("Expert alternative response:") == false);

			cur = in.nextLine();
			String expertAltResponse = cur;


			System.out.println("AI response: " + response);
			System.out.println("Expert response: " + expertResponse);
			System.out.println("Acceptable alternative response: " + expertAltResponse);
			
			if(expertResponse.contains(response)) {
				System.out.println("AI matches expert response! (PASS)");
				ret = TESTCASE_PASS;
				
			} else if(expertAltResponse.contains(response)) {
				System.out.println("AI matches expert alternative response! (PASS)");
				ret = TESTCASE_PASS;
				
			} else {
				System.out.println("Fail: AI doesn't do what the expert does (FAIL)");
				ret = TESTCASE_FAIL;
			}
			
			System.out.println("END OF TESTCASE");
			System.out.println();
			System.out.println();
			

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		//Allow for System.out.println just in case that was taken away:
		System.setOut(originalStream);
		return ret;
		
	}
	
	
	//Fight logic copy/pasted from the AIGameServer Mellow code
	
	public static int getCardNumber(String card) {
		int ret=0;
		if(card.length() < 2) {
			return -1;
		}
		
		if(card.charAt(0) == 'A') {
			ret = 1;
		} else if(card.charAt(0) == 'K') {
			ret = 13;
		} else if(card.charAt(0) == 'Q') {
			ret = 12;
		} else if(card.charAt(0) == 'J') {
			ret = 11;
		} else if(card.charAt(0) == 'T') {
			ret = 10;
		} else {
			ret = card.charAt(0) - '0';
		}
		
		if(card.charAt(1) == 'S') {
			
		} else if(card.charAt(1) == 'H') {
			ret += 13;
		} else if(card.charAt(1) == 'C') {
			ret += 26;
		} else if(card.charAt(1) == 'D') {
			ret += 39;
		}
		
		return ret;
	}
	
	public static int fight(int initialActionIndex, int cardsOnTable[]) {
		int currentWinnerIndex = initialActionIndex;
		String currentBestSuit = "" + getSuit(cardsOnTable[initialActionIndex]);
		int currentBestPower = getPowerOfCardNum(getBaseNumber(cardsOnTable[initialActionIndex]));
		
		String nextSuit;
		int nextPower;
		int currentIndex;
		for(int i=0; i<3; i++) {
			currentIndex = (initialActionIndex + 1 + i)%4;
			
			nextSuit = "" + getSuit(cardsOnTable[currentIndex]);
			if(nextSuit.equals(TRUMP) && currentBestSuit.equals(TRUMP) == false) {
				//TRUMP!
				currentBestSuit = TRUMP;
				currentBestPower = getPowerOfCardNum(getBaseNumber(cardsOnTable[currentIndex]));
				currentWinnerIndex = currentIndex;
			} else if(nextSuit.equals(currentBestSuit)) {
				nextPower = getPowerOfCardNum(getBaseNumber(cardsOnTable[currentIndex]));
				
				if(nextPower > currentBestPower) {
					currentBestPower = nextPower;
					currentWinnerIndex = currentIndex;
				}
				
			}
			//cardsOnTable
		}

		//TESING:
		//for(int i=0; i<4; i++) {
		//	System.out.print(deck.DeckFunctions.getCardString(cardsOnTable[i])  +  ", ");
		//}
		//System.out.println("ALL: END FIGHT!");
		//END TESTING
		
		return currentWinnerIndex;
	}
	

	public static char getBaseNumber(int cardNum) {
		int cardBaseNum = (cardNum % 13);
		
		if(cardBaseNum == 0) {
			return 'K';
		} else if(cardBaseNum == 12) {
			return 'Q';
		} else if(cardBaseNum == 11) {
			return 'J';
		} else if(cardBaseNum == 10) {
			return 'T';
		}  else  if(cardBaseNum == 1) {
			return 'A';
		} else {
			return (char) ('0' + cardBaseNum);
		}
	}

	public static char getSuit(int cardNum) {
		if(cardNum <= 13) {
			return 'S';
		} else if(cardNum <= 26) {
			return 'H';
		} else if(cardNum <= 39) {
			return 'C';
		} else {
			return 'D';
		}
	}


	public static int getPowerOfCardNum(char cardNumber) {
		if(cardNumber >='2' && cardNumber<='9') {
			return (int)(cardNumber - '2');
		} else if(cardNumber == 'T') {
			return 10;
		} else if(cardNumber == 'J') {
			return 11;
		} else if(cardNumber == 'Q') {
			return 12;
		} else if(cardNumber == 'K') {
			return 13;
		} else if(cardNumber == 'A') {
			return 14;
		} else {
			System.out.println("ERROR: unknown card number in getPowerOfCardNum! ( " + cardNumber + ")");
			System.exit(1);
			return -1;
		}
	}
	
	//END copy/paste

}

package mellow.testcase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.OutputStream;
import java.util.Scanner;

import javax.sound.sampled.Line;

import mellow.Constants;
import mellow.ai.aiDecider.MellowAIDeciderFactory;
import mellow.ai.aiDecider.MellowAIDeciderInterface;
import mellow.ai.aiDecider.MellowBasicDecider;

public class testCaseParser {

	//folders:
	public static String TEST_FOLDERS[] = {"MichaelDebugMadeUp", "Michael", "Michael2021", "Michael2021-2"};
//
	//public static String TEST_FOLDERS[] = {"Michael"};
	//public static String TEST_FOLDERS[] = {"Michael2021"};
	//public static String TEST_FOLDERS[] = {"Michael2021-2"};
	//public static String TEST_FOLDERS[] = {"MichaelDebugMadeUp"};
	
	//public static String TEST_FOLDERS[] = {"MonteCarloTests"};


	public static int numLeadingPass = 0;
	public static int numLeading = 0;
	

	public static int numSecondPass = 0;
	public static int numSecond = 0;
	

	public static int numThirdPass = 0;
	public static int numThird = 0;
	

	public static int numFourthPass = 0;
	public static int numFourth = 0;

	public static int numBiddingPass = 0;
	public static int numBidding = 0;
	
	public static void main(String[] args) {
		
		File list[]  = getTestCaseFilesMultFolders(TEST_FOLDERS);
		//File list[]  = getTestCaseFiles(TEST_FOLDER);
		
		//Option 1: Follow hard-coded rules AI:
		MellowAIDeciderInterface decider = MellowAIDeciderFactory.getAI(MellowAIDeciderFactory.FOLLOW_HARD_CODED_RULES_AI);
		
		//Option 2: Quick Monte Carlo AI
		 //MellowAIDeciderInterface decider = MellowAIDeciderFactory.getAI(MellowAIDeciderFactory.MONTE_CARLO_METHOD_AI);
		
		//Option 3: Slow and thorougk Monte Carlo AI:
		//MellowAIDeciderInterface decider = MellowAIDeciderFactory.getAI(MellowAIDeciderFactory.MONTE_CARLO_METHOD_AI_THOROUGH_TEST);

		//Test monte carlo if we're using monte carlo folder:
		if(TEST_FOLDERS.length == 1 && TEST_FOLDERS[0].toLowerCase().equals("montecarlotests")) {
			decider = MellowAIDeciderFactory.getAI(MellowAIDeciderFactory.MONTE_CARLO_METHOD_AI_THOROUGH_TEST);
			//decider = MellowAIDeciderFactory.getAI(MellowAIDeciderFactory.MONTE_CARLO_METHOD_AI);
		}
		
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
		
		if(numLeading > 0) {
			System.out.println("Passes while leading: " + numLeadingPass + " out of " + numLeading);
			System.out.println("That's a " + String.format("%.2f", ((100.0*numLeadingPass) /(1.0*numLeading))) + "% pass rate.");
			System.out.println();
			
			System.out.println("Passes while second: " + numSecondPass + " out of " + numSecond);
			System.out.println("That's a " + String.format("%.2f", ((100.0*numSecondPass) /(1.0*numSecond))) + "% pass rate.");
			System.out.println();
			
			System.out.println("Passes while third: " + numThirdPass + " out of " + numThird);
			System.out.println("That's a " + String.format("%.2f", ((100.0*numThirdPass) /(1.0*numThird))) + "% pass rate.");
			System.out.println();
			
			System.out.println("Passes while fourth: " + numFourthPass + " out of " + numFourth);
			System.out.println("That's a " + String.format("%.2f", ((100.0*numFourthPass) /(1.0*numFourth))) + "% pass rate.");
			System.out.println();
			
		}

		if(numBidding > 0) {
			System.out.println("Passes while Bidding: " + numBiddingPass + " out of " + numBidding);
			System.out.println("That's a " + String.format("%.2f", ((100.0*numBiddingPass) /(1.0*numBidding))) + "% pass rate.");
			System.out.println();
		}
		
		
		System.out.println("AI passes " + numPasses + " out of " + numTrials);
		System.out.println("That's a " + String.format("%.2f", ((100.0*numPasses) /(1.0*numTrials))) + "% pass rate.");
		
		System.out.println("AI is consistent " + agreement + " out of " + (num_plays_for_constancy_test));
		System.out.println("That's a " + String.format("%.2f", ((100.0*agreement) /(1.0*(num_plays_for_constancy_test)))) + "% consistent rate.");

		System.out.println("Num failed testcases: " + (numTrials-numPasses) + ".");
		
	}
	
	public static File[] getTestCaseFilesMultFolders(String folders[]) {
		
		//Get num Files:
		int numFiles = 0;
		File folder[] = new File[folders.length];
		
		for(int i=0; i<folders.length; i++) {
			folder[i] = new File("..\\TestCaseAndReplayData\\testcases\\" + folders[i]);
			numFiles += folder[i].listFiles().length;
		}
		
		File ret[] = new File[numFiles];
		
		int numFilesInRet = 0;
		
		for(int i=0; i<folders.length; i++) {
			
			File list[] = getTestCaseFiles(folders[i]);
			
			for(int j=0; j<list.length; j++) {
				ret[numFilesInRet] = list[j];
				numFilesInRet++;
			}
			
		}
		
		
		return ret;
	}
	
	public static File[] getTestCaseFiles(String testFolder) {
		
		//https://stackoverflow.com/questions/5694385/getting-the-filenames-of-all-files-in-a-folder
		
		//TODO: standardize this and add constant file
		//File folder = new File("..\\TestCaseAndReplayData\\testcases\\Michael");
		File folder = new File("..\\TestCaseAndReplayData\\testcases\\" + testFolder);
		
		File[] listOfFiles = folder.listFiles();


		
		FileWithNumberComparable filesToSort[] = new FileWithNumberComparable[listOfFiles.length];
		
		for(int i=0; i<listOfFiles.length; i++) {
			filesToSort[i] = new FileWithNumberComparable(listOfFiles[i], getFileNumber(listOfFiles[i].getName()));
		}
		
		filesToSort = (FileWithNumberComparable[])Sort.quickSort(filesToSort);
		

		for (int i = 0; i < listOfFiles.length; i++) {
		  if (listOfFiles[i].isFile()) {
		    System.out.println("File " + filesToSort[i].getFile().getName());
		    
		  } else if (listOfFiles[i].isDirectory()) {
		    System.out.println("Directory " + filesToSort[i].getFile().getName());
		  }
		}
		
		File[] listOfFilesOut = new File[listOfFiles.length];
		
		for(int i=0; i<listOfFiles.length; i++) {
			listOfFilesOut[i] = filesToSort[i].getFile();
		}
		
		System.out.println();
		System.out.println();
		
		return listOfFilesOut;
	}
	
	public static int getFileNumber(String fileName) {
		
		int res = new Scanner(fileName).useDelimiter("\\D+").nextInt();
		
		return res;
		
	}
	
	static final boolean TESTCASE_PASS = true;
	static final boolean TESTCASE_FAIL = false;
	
	static final int NUM_PLAYERS = 4;
	static final String TRUMP = "S";
	
	
	public static int agreement = 0;
	public static int num_plays_for_constancy_test = 0;
	
	static PrintStream dummyStream = new PrintStream(new OutputStream(){
	    public void write(int b) {
	        // NO-OP
	    }
	});

	public static boolean runTestcase(MellowAIDeciderInterface decider, File testCaseFile) {
		return runTestcase(decider, testCaseFile, true);
	}

	public static boolean runTestcase(MellowAIDeciderInterface decider, File testCaseFile, boolean verbose) {
		//https://stackoverflow.com/questions/8363493/hiding-system-out-print-calls-of-a-class

		PrintStream originalStream = System.out;
		
		if(verbose == false) {
			System.setOut(dummyStream);
		}
		
		boolean ret = false;
		
		decider.resetStateForNewRound();
		
		Scanner in = null;
		
		try {
			in = new Scanner(testCaseFile);
			
			String players[] = new String[4];
			for(int i =0; i<players.length; i++) {
				players[i] = "test" + i;
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
			} while(cur.toLowerCase().contains("dealer") == false && cur.contains("You are the dealer") == false);
			
			
			int dealerIndex = 0;
			
			//Added other possibilities just in case I enter the wrong prompt in manually created tests:
			if(cur.contains("You are the dealer")
					|| cur.toLowerCase().contains("you are the")
					|| cur.toLowerCase().contains("you\'re the")) {
				
				dealerIndex = 0;
			
			} else if(cur.contains("Dealer is on your left") 
					|| cur.toLowerCase().contains("left")) {
				
				dealerIndex = 1;
				
			} else if(cur.contains("Dealer is your partner opposite you.")
					|| cur.toLowerCase().contains("partner")
					|| cur.toLowerCase().contains("opposite")
					|| cur.toLowerCase().contains("accross")) {
				
				dealerIndex = 2;
				
			} else if(cur.contains("Dealer is on your right") 
					|| cur.toLowerCase().contains("right")) {
				
				dealerIndex = 3;
			} else {
				System.err.println("ERROR: unknown dealer" + "\n");
				System.exit(1);
			}
			
			decider.setDealer(players[dealerIndex]);
			
			//Set the score if it exists:
			int ourScore = 0;
			int theirScore = 0;
			
			boolean getScoreNextLines = false;
			do {
				cur = in.nextLine();
				if(cur.toLowerCase().contains("your score")) {
					getScoreNextLines = true;
				} else if(getScoreNextLines) {
					String scores = cur.trim();
					Scanner score = new Scanner(scores);
					if(score.hasNextInt()) {
						ourScore = score.nextInt();
					}
					
					if(score.hasNextInt()) {
						theirScore = score.nextInt();
					}
					//System.out.println("Scores: " + ourScore + " vs " + theirScore);
					decider.setNewScores(ourScore, theirScore);
					
					getScoreNextLines = false;
				}
				
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
			
			int numBidsMade = 0;
			
			do {
				actionIndex = (actionIndex + 1) % NUM_PLAYERS;
				
				cur = in.nextLine();
				System.out.println(cur);
				
				if(cur.contains(" bid ") && cur.split(" ")[1].equals("bid")) {
					players[actionIndex] = cur.split(" ")[0];
					
					//Rename player from testI to the actual name:
					decider.setNameOfPlayers(players);
					
					int bid = -1;
					if(cur.split(" ")[2].toLowerCase().contains("m")) {
						bid = 0;
					} else {
						bid = Integer.parseInt(cur.split(" ")[2].replace(".", ""));
					}
					decider.receiveBid(players[actionIndex], bid);
					numBidsMade++;
				}
				
			} while (numBidsMade < Constants.NUM_PLAYERS && cur.equals("") == false);
			
			
			String response = "-1";
			
			//TODO: create helper functions:
			if(numBidsMade < Constants.NUM_PLAYERS) {
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
				
				//Constancy test: (Monty carlo doesn't always give the same answers...)
				//TODO: do this in a less hacky hack:
				String response2 = decider.getCardToPlay();
				if(response.equals(response2)) {
					agreement++;
				}
				num_plays_for_constancy_test++;
			}
			
			if(response == null || response.trim().equals("")) {
				response = "FAIL: (LACK OF RESPONSE)";
			}
			

			
			do {
				cur = in.nextLine();
			} while(cur.startsWith("Expert response:") == false);

			cur = in.nextLine();
			String expertResponse = cur.toUpperCase();
			
			do {
				cur = in.nextLine();
			} while(cur.startsWith("Expert alternative response:") == false);

			if(in.hasNextLine()) {
				cur = in.nextLine();
			} else {
				cur = "";
			}
			String expertAltResponse = cur.toUpperCase();


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
			
			//Count type of test cases:
			if(decider instanceof MellowBasicDecider) {
				if( ((MellowBasicDecider)decider).getCopyOfDataModel().stillInBiddingPhase() == false) {
					if(((MellowBasicDecider)decider).getCopyOfDataModel().currentThrowIsLeading()) {
	
						numLeading++;
						
						if(ret == TESTCASE_PASS) {
							numLeadingPass++;
						} else {
							System.out.println("(LEAD FAIL)");
						}
						
					} else if(((MellowBasicDecider)decider).getCopyOfDataModel().getCardsPlayedThisRound( ) % 4 == 1) {

						numSecond++;
						
						if(ret == TESTCASE_PASS) {
							numSecondPass++;
						} else {
							System.out.println("(SECOND FAIL)");
						}
					} else if(((MellowBasicDecider)decider).getCopyOfDataModel().getCardsPlayedThisRound( ) % 4 == 2) {

						numThird++;
						
						if(ret == TESTCASE_PASS) {
							numThirdPass++;
						} else {
							System.out.println("(THIRD FAIL)");
						}
					} else if(((MellowBasicDecider)decider).getCopyOfDataModel().getCardsPlayedThisRound( ) % 4 == 3) {

						numFourth++;
						
						if(ret == TESTCASE_PASS) {
							numFourthPass++;
						} else {
							System.out.println("(FOURTH FAIL)");
						}
					}
				}
			}

			if(decider instanceof MellowBasicDecider) {
				if(((MellowBasicDecider)decider).getCopyOfDataModel().stillInBiddingPhase()) {

					numBidding++;
					
					if(ret == TESTCASE_PASS) {
						numBiddingPass++;
					} else {
						System.out.println("(BID FAIL)");
					}
					
				}
			}
			//End count type of test case.
			
			//Add comments at the end of test case for more context:
			boolean foundComments = false;
			while(in.hasNextLine()) {
				String tmp = in.nextLine();
				
				if(tmp.startsWith("#")) {
					if(foundComments == false) {
						System.out.println("#Comments:");
						foundComments = true;
					}
					System.out.println(tmp);
				}
			}
			
			System.out.println("END OF TESTCASE");
			System.out.println();
			System.out.println();
			
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			in.close();
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
			System.err.println("ERROR: unknown card number in getPowerOfCardNum! ( " + cardNumber + ")");
			System.exit(1);
			return -1;
		}
	}
	
	//END copy/paste

}

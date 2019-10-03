package mellow.testcase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import mellow.Constants;
import mellow.ai.aiDecider.MellowAIDeciderFactory;
import mellow.ai.aiDecider.MellowAIDeciderInterface;

public class PyMellowBidDataCollectParser {

	public static void main(String[] args) {
		
		MellowAIDeciderInterface decider = MellowAIDeciderFactory.getAI(0);
		
		File testCaseFile = new File("..\\TestCaseAndReplayData\\outputBidData1st.txt");
		
		runTestcases(decider, testCaseFile, true);
		
	}
	

	static PrintStream dummyStream = new PrintStream(new OutputStream(){
	    public void write(int b) {
	        // NO-OP
	    }
	});
	
	
	public static void runTestcases(MellowAIDeciderInterface decider, File testCaseFile) {
		runTestcases(decider, testCaseFile, true);
	}
	
	public static void runTestcases(MellowAIDeciderInterface decider, File testCaseFile, boolean verbose) {

		PrintStream originalStream = System.out;
		
		if(verbose == false) {
			System.setOut(dummyStream);
		}
		
		boolean ret = false;
		
		decider.resetStateForNewRound();
		
		int numPrecisionPasses = 0;
		int num_pass_or_alt_pass = 0;
		int num_fail = 0;
		int numTrials = 0;
		
		try {
			Scanner in = new Scanner(testCaseFile);
			
			String cur;
			
			while(in.hasNextLine()) {
				cur = in.nextLine();
				if(isStartingHand(cur)) {
					String userResponse = in.nextLine();
					String altUserResponses = in.nextLine();
					
					int result = testFirstBid(decider, cur, userResponse, altUserResponses);
					if(result == PASS) {
						numPrecisionPasses++;
						num_pass_or_alt_pass++;
					} else if(result == ALT_PASS) {
						num_pass_or_alt_pass++;
					} else {
						num_fail++;
					}
					numTrials++;
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		//Allow for System.out.println just in case that was taken away:
		System.setOut(originalStream);
		
		System.out.println("Final report on intial bids testcases for " + decider + ":");
		System.out.println("AI does exactly what testcase maker does " + numPrecisionPasses + " out of " + numTrials);
		System.out.println("That's a " + String.format("%.2f", ((100.0*numPrecisionPasses) /(1.0*numTrials))) + "% precision pass rate.");
		System.out.println();
		System.out.println("AI passes " + num_pass_or_alt_pass + " out of " + numTrials);
		System.out.println("That's a " + String.format("%.2f", ((100.0*num_pass_or_alt_pass) /(1.0*numTrials))) + "% pass rate.");
		System.out.println();
		System.out.println("AI failures:" + num_fail + " out of " + numTrials);
		System.out.println("That's a " + String.format("%.2f", ((100.0*num_fail) /(1.0*numTrials))) + "% fail rate.");
		
		
		
	}

	public static final int PASS = 1;
	public static final int ALT_PASS = 2;
	public static final int FAIL = 0;
	
	public static int testFirstBid(MellowAIDeciderInterface decider, String hand, String userResponse, String altUserResponses) {

		String players[] = new String[Constants.NUM_PLAYERS];
		players[0] = "Hero";
		players[1] = "Villain1";
		players[2] = "Partner";
		players[3] = "Villain2";
		

		decider.resetStateForNewRound();
		decider.setNameOfPlayers(players);
		decider.setCardsForNewRound(hand.split(" "));
		
		

		String response = decider.getBidToMake();
		
		String printStatement = "";

		
		if(response.toLowerCase().contains("m")) {
			response = "0";
		}
		
		int outcome;
		if(userResponse.contains(response.trim())) {
			outcome =  PASS;
		} else if(altUserResponses.contains(response.trim())) {
			outcome = ALT_PASS;
		} else {
			outcome = FAIL;
		}
		

		printStatement += "1st bid test:";
		if(outcome == FAIL) {
			printStatement += " (FAIL)\n";
		} else {
			printStatement += "\n";
		}
		
		printStatement += hand +"\n";
		printStatement += "----" + "\n";
		printStatement += "AI print statements --------" + "\n";
		printStatement += "****************************" + "\n";
		printStatement += "AI response: " + response + "\n";
		printStatement += "user response: " + userResponse + "\n";
		printStatement += "User alt response: " + altUserResponses + "\n";
		printStatement += "\n\n";
		
		System.out.println(printStatement);
		
		return outcome;
	}
	
	public static boolean isStartingHand(String line) {
		String cards[] = line.split(" ");
		
		if(cards.length != Constants.NUM_STARTING_CARDS_IN_HAND) {
			return false;
		}
		
		for(int i=0; i<cards.length; i++) {
			if(isCard(cards[i]) == false) {
				return false;
			}
			
		}
		
		return true;
		
	}
	
	
	public static boolean isCard(String input) {
		
		if(input.length() < 2) {
			return false;
		}
		
		
		if(input.charAt(0) == 'A'
			|| input.charAt(0) == 'K'
			|| input.charAt(0) == 'Q'
			|| input.charAt(0) == 'J'
			|| input.charAt(0) == 'T'
			|| (input.charAt(0) >= '2' && input.charAt(0) <= '9')) {
			
			if(input.charAt(1) == 'S'
					|| input.charAt(1) == 'H'
					|| input.charAt(1) == 'C'
					|| input.charAt(1) == 'D') {
						return true;
					} else {
						return false;
					}
		} else {
			return false;
		}
		
	}
}

package mellow.ai.aiDecider;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.listener.MellowAIListener;
import mellow.cardUtils.CardStringFunctions;
import mellow.cardUtils.handUtilsQueryForTestcase;
import clientPlayers.ServerRequestHandler;

public class MellowQueryUserForTestcase implements MellowAIDeciderInterface {

	/*How to read large text from console output:
	 * https://stackoverflow.com/questions/790720/eclipse-ide-how-to-zoom-in-on-text
	 * "go to Eclipse > Prefences > General > Appearance > Color and Fonts > Basic > Text Font
		Font problem will resolved I guess. Dont need a any plugin for this."
	 */

	DataModel dataModel;
	
	private Scanner in = new Scanner(System.in);
	
	private ArrayList<String> cardList = null;
	
	private String scoreAtStartOfRound;
	private String savedBidHistory = "";
	private String savedPlayHistory = "";
	
	
	public String toString() {
		return "MellowQueryUserForTestcase PLAYER";
	}
	
	public MellowQueryUserForTestcase() {
		
		//Reuse dataModel that AI uses:
		this.dataModel = new DataModel();
		
		//Set default start scores:
		this.setNewScores(0, 0);
		
	}
	
	@Override
	public void receiveUnParsedMessageFromServer(String msg) {
		
	}
	
	@Override
	public void resetStateForNewRound() {
		this.dataModel.resetStateForNewRound();
		
		if(savedPlayHistory.equals("") == false) {
			//Print last hand for user to see
			String printStr = getGamePlayerStateString();
			printStr += "END OF ROUND. Input anything to continue";
			System.out.println(printStr);
			in.nextLine();
		}
		
		savedPlayHistory = "";
		savedBidHistory = "";
		
	}
	
	@Override
	public void setDealer(String playerName) {
		
		this.dataModel.setDealer(playerName);
		
	}

	@Override
	public void receiveBid(String playerName, int bid) {

		this.dataModel.setBid(playerName, bid);

		if(bid > 0) {
			savedBidHistory += playerName + " bid " + bid  + ".\n";
		} else if(bid == 0){
			savedBidHistory += playerName + " bid mellow.\n";
		} else {
			System.err.println("ERROR: bid below 0");
			System.exit(1);
		}
		
	}

	public void receiveCardPlayed(String playerName, String card) {

		
		if(this.dataModel.getCardsPlayedThisRound() % Constants.NUM_PLAYERS == 0) {
			savedPlayHistory += "--new round--\n";
		}
		
		
		//Update the cards the current player has:
		if(playerName.equals(this.dataModel.getPlayers()[Constants.CURRENT_AGENT_INDEX])) {
			cardList.remove(card);
		}
		
		savedPlayHistory += playerName + " played the " + card  + ".\n";
		

		this.dataModel.updateDataModelWithPlayedCard(playerName, card);
		
		//Setup for a new round:
		if(this.dataModel.getCardsPlayedThisRound() >= Constants.NUM_CARDS) {
			
			//Wait N seconds so the final state of the round will printed at the bottom of the console:
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			resetStateForNewRound();
		}
		
	}
	


	private String OrigHand = "";
	
	@Override
	public void setCardsForNewRound(String[] cards) {
		
		this.dataModel.setupCardsInHandForNewRound(cards);
		
		cardList = new ArrayList<String>();
		
		for(int i=0; i<cards.length; i++) {
			cardList.add(cards[i]+ "");
		}
		
		if(cardList.size() == Constants.NUM_CARDS/Constants.NUM_PLAYERS) {
			//Sort the cards
			cardList = CardStringFunctions.sort(cardList);
			
			OrigHand = "";
			for(int i=0; i<cardList.size(); i++) {
				OrigHand += cardList.get(i) + " ";
			}
			OrigHand += "\n";
		}
	}

	@Override
	public void setNewScores(int teamAScore, int teanBScore) {
		this.dataModel.setNewScores(teamAScore, teanBScore);
		
		scoreAtStartOfRound = "Your Score      Their Score\n";
		scoreAtStartOfRound += " " + teamAScore + ("       ").substring( (teamAScore +"").length() ) + "         " + teanBScore + "\n";
	}

	
	@Override
	public String getCardToPlay() {
		

		String printStr = "\n\n" + getGamePlayerStateString() +"\n";
		String play = "";
		
		//If it's the last case to play, you don't have any choices to make:
		if(Constants.NUM_CARDS - this.dataModel.getCardsPlayedThisRound() <= Constants.NUM_PLAYERS) {
			
			
			printStr += "You are playing your last card: " + cardList.get(0).toUpperCase() + "\n";
			printStr += "Input anything to continue";
			System.out.println(printStr);
			
			in.nextLine();
			
			play = cardList.get(0).toUpperCase() + " ";
			
		} else if(isNextCardLeading(this.dataModel.getCardsPlayedThisRound()) == false && handUtilsQueryForTestcase.hasOnlyOneChoice(this.dataModel.getCardLeaderThrow(), cardList)) {
			
			play = handUtilsQueryForTestcase.getOnlyCardToPlay(this.dataModel.getCardLeaderThrow(), cardList);
			printStr += "You are playing the " + play + "\n(It's the only legal card to play)\n";
			printStr += "Input anything to continue";
			System.out.println(printStr);
			
			in.nextLine();
			
			//For now, make the test case because I don't think the ais follow the rules correctly :(
			//(I make the server force them to follow the rules)
			printTestCase(play, "");
			
		} else {
		
			//Actually make a choice 
			printStr += "Please play a card:";
			System.out.println(printStr);
			
			
			play = in.nextLine().toUpperCase();
			
			System.out.println("Can you list alternative plays that aren't that bad?");
			String alternative = in.nextLine();
			
			//Make test case:
			printTestCase(play, alternative);
		}
		
		return play;
		
	}

	@Override
	public String getBidToMake() {
		
		String printStr = "\n\n" + getGamePlayerStateString() +"\n";
		printStr += "What's your bid:";
		System.out.println(printStr);
		
		String bid = in.nextLine();
		
		if(bid.toLowerCase().startsWith("mellow")) { 
			bid = "0";
		}
		
		
		System.out.println("Can you list alternative bids that aren't that bad?");
		
		String alternative = in.nextLine();
		
		printTestCase(bid, alternative);
		
		return bid;
	}

	@Override
	public void setNameOfPlayers(String[] players) {
		
		this.dataModel.setNameOfPlayers(players);
		
		
	}
	
	
	
	private String getGamePlayerStateString() {
		String ret = "";
		
		ret += "Your name: " + this.dataModel.getPlayers()[0] + "\n";
		
		int dealerIndex = this.dataModel.getDealerIndexAtStartOfRound();
		
		if(dealerIndex ==0 ) { 
			ret += "You are the dealer" + "\n";
		} else if(dealerIndex == 1) {
			ret += "Dealer is on your left" + "\n";
		} else if(dealerIndex == 2) {
			ret += "Dealer is your partner opposite you." + "\n";
		} else if(dealerIndex == 3) {
			ret += "Dealer is on your right" + "\n";
		} else {
			System.err.println("ERROR: unknown dealer");
			System.exit(1);
		}

		ret += "Score at start:" + "\n";
		ret += scoreAtStartOfRound + "\n";
		
		ret += "Cards dealt:" + "\n";
		ret += OrigHand + "\n";
		
		ret += "\n";
		ret += "Bid history:" + "\n";
		ret += savedBidHistory + "\n";
		ret += "\n";
		
		if(savedPlayHistory.trim().equals("") == false) {
			ret += "Play history:"  + "\n";
			ret += savedPlayHistory + "\n";
		}

		//Sort the cards
		cardList = CardStringFunctions.sort(cardList);
		
		ret += "Cards in hand:" + "\n";
		for(int i=0; i<cardList.size(); i++) {
			ret += cardList.get(i) + " ";
		}
		ret += "\n";
		
		return ret;
	}
	
	public static boolean isNextCardLeading(int numCardsPlayedInRound) {
		return numCardsPlayedInRound % Constants.NUM_PLAYERS == 0;
	}

	public PrintWriter getTestCaseWriter() {
		int num = getTestCaseNumber();
		return getTestCaseWriter(num);
	}
	
	private synchronized int getTestCaseNumber() {
		int num = 0;
		File f;
		try {
			
			//TODO: look up how to make directories so it could automatically create directories as required
			//f = new File(""..\\TestCaseAndReplayDatatestcases\\" + this.playerNames[0]);
			
			do {
				num++;
				f = new File("..\\TestCaseAndReplayData\\testcases\\" + this.dataModel.getPlayers()[0] + "\\testcase" +  num + ".txt");
			} while(f.exists());
			
			
		} catch( Exception e) {
			num = -1;
			e.printStackTrace();
		}
		
		return num;
		
	}
	
	private synchronized PrintWriter getTestCaseWriter(int num) {

		PrintWriter testCaseFile = null;
		try {
			
			
			testCaseFile = new PrintWriter(new File("..\\TestCaseAndReplayData\\testcases\\" + this.dataModel.getPlayers()[0] + "\\testcase" +  num + ".txt"));
			
		} catch( Exception e) {
			e.printStackTrace();
		}
		return testCaseFile;
		
	}
	
	public void makeDirectoryIfNotExists() {

		File directory = new File("..\\TestCaseAndReplayData\\testcases\\" + this.dataModel.getPlayers()[0]);
	    if (! directory.exists()){
	        directory.mkdir();
	    }
	}

	public void printTestCase(String bid, String alternative) {
		try {
			
			makeDirectoryIfNotExists();
		    
			PrintWriter newTestCase = getTestCaseWriter();
			newTestCase.println(getGamePlayerStateString());
			newTestCase.flush();
			
			newTestCase.println("Expert response:");
			newTestCase.println(bid.toUpperCase());
			newTestCase.flush();
			
			newTestCase.println("Expert alternative response:");
			newTestCase.println(alternative.toUpperCase());
			
			printHelpComments();
			
			newTestCase.flush();
			newTestCase.close();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void printHelpComments() {
		//TODO:
		//record how many tricks everyone got...
		
		// ex: # Richard 5/4
		//TODO: if only choice, mention it.
	}
	
}

package mellow.ai;

import java.util.ArrayList;
import java.util.Scanner;

import clientPlayers.ServerRequestHandler;

public class MellowQueryUserForTestcase implements MellowAIDeciderInterface {

	
	private Scanner in = new Scanner(System.in);
	
	private ArrayList<String> cardList = null;
	
	int INDEX_CURRENT_PLAYER = 0;
	int NUM_PLAYERS = 4;
	int NUM_CARDS = 52;
	
	private String playerNames[] = new String[4];
	
	String scoreAtStartOfRound = "";
	String savedBidHistory = "";
	String savedPlayHistory = "";
	int numCardsPlayedInRound = 0;
	
	int dealerIndex = -1;
	
	@Override
	public void receiveUnParsedMessageFromServer(String msg) {
		
	}

	
	@Override
	public void setDealer(String playerName) {
		for(int i=0; i<NUM_PLAYERS; i++) {
			if(playerName.equals(playerNames[i])) {
				dealerIndex = i;
			}
		}
	}

	@Override
	public void receiveBid(String playerName, int bid) {
		if(bid > 0) {
			savedBidHistory += playerName + " bid " + bid  + ".\n";
		} else if(bid == 0){
			savedBidHistory += playerName + " bid mellow.\n";
		} else {
			System.err.println("ERROR: bid below 0");
		}
	}

	public void getPlayedCard(String playerName, String card) {
		
		if(numCardsPlayedInRound % NUM_PLAYERS == 0) {
			savedPlayHistory += "--new round--\n";
		}
		numCardsPlayedInRound++;
		
		//Update the cards the current player has:
		if(playerName.equals(playerNames[INDEX_CURRENT_PLAYER])) {
			cardList.remove(card);
		}
		
		savedPlayHistory += playerName + " played the " + card  + ".\n";
		
		//Setup for a new round:
		if(numCardsPlayedInRound >= NUM_CARDS) {
			numCardsPlayedInRound = 0;
			savedPlayHistory = "";
			savedBidHistory = "";
			scoreAtStartOfRound = "";
		}
		
	}

	@Override
	public void setupCardsForNewRound(String[] cards) {
		cardList = new ArrayList<String>();
		
		for(int i=0; i<cards.length; i++) {
			cardList.add(cards[i]+ "");
		}
		
		
	}

	@Override
	public void updateScores(int teamAScore, int teanBScore) {
		scoreAtStartOfRound += "Your Score      Their Score\n";
		scoreAtStartOfRound += " " + teamAScore + ("       ").substring( (teamAScore +"").length() ) + "         " + teanBScore + "\n";
	}

	@Override
	public String getCardToPlay() {
		
		System.out.println(getGamePlayerStateString());
		
		System.out.println("Please play a card:");
		String play = in.nextLine().toUpperCase();
		
		System.out.println("Can you list alternative plays that aren't that bad?");
		String alternativeTODO = in.nextLine();
		
		return play;
		
	}

	@Override
	public String getBidToMake() {
		System.out.println(getGamePlayerStateString());
		
		System.out.println("What's your bid:");
		String bid = in.nextLine();
		
		if(bid.toLowerCase().startsWith("mellow")) { 
			bid = "0";
		}
		System.out.println("Can you list alternative bids that aren't that bad?");
		
		//TODO
		String alternativeTODO = in.nextLine();
		
		return bid;
	}

	@Override
	public void setNameOfPlayers(String[] players) {
		for(int i=0; i<players.length; i++) {
			playerNames[i] = players[i] + "";
		}
		
	}
	
	
	
	private String getGamePlayerStateString() {
		String ret = "\n\n\n";
		
		ret += "Your name: " + playerNames[0] + "\n";
		ret += "\n\n";
		ret += "Your name: " + playerNames[0] + "\n";
		
		if(dealerIndex ==0 ) { 
			ret += "You are the dealer" + "\n";
		} else if(dealerIndex == 1) {
			ret += "Dealer is on your left" + "\n";
		} else if(dealerIndex == 2) {
			ret += "Dealer is your partner opposite you." + "\n";
		} else if(dealerIndex == 3) {
			ret += "Dealer is on your right" + "\n";
		} else {
			ret += "ERROR: unknown dealer" + "\n";
			System.exit(1);
		}

		ret += "Score at start:" + "\n";
		ret += scoreAtStartOfRound + "\n";
		
		ret += "\n";
		ret += "Bid history:" + "\n";
		ret += savedBidHistory + "\n";
		ret += "\n";
		
		if(savedPlayHistory.trim().equals("") == false) {
			ret += "Play history:"  + "\n";
			ret += savedPlayHistory + "\n";
		}

		//Sort the cards
		cardList = MellowAIListener.sort(cardList);
		
		ret += "Cards in hand:" + "\n";
		for(int i=0; i<cardList.size(); i++) {
			ret += cardList.get(i) + " ";
		}
		ret += "\n";
		
		return ret;
	}

}

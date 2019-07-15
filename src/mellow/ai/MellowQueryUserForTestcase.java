package mellow.ai;

import java.util.ArrayList;
import java.util.Scanner;

import clientPlayers.ServerRequestHandler;

public class MellowQueryUserForTestcase implements MellowAIDeciderInterface {

	
	private Scanner in = new Scanner(System.in);
	
	//TODO: use
	private ArrayList<String> cardList = null;
	
	int INDEX_CURRENT_PLAYER = 0;
	int NUM_PLAYERS = 4;
	int NUM_CARDS = 52;
	
	private String playerNames[] = new String[4];
	
	String scoreAtStartOfRound = "";
	String savedBidHistory = "";
	String savedPlayHistory = "";
	int numCardsPlayedInRound = 0;
	
	@Override
	public void receiveUnParsedMessageFromServer(String msg) {
		
	}

	@Override
	public void setDealer(String playerName) {
		
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
		scoreAtStartOfRound += " " + teamAScore + ("    ").substring((4-teamAScore +"").length()) + "                " + teanBScore + "\n";
	}

	@Override
	public String getCardToPlay() {

		System.out.println("Score at start:");
		System.out.println(scoreAtStartOfRound);
		System.out.println();
		System.out.println("Bid history:");
		System.out.println(savedBidHistory);
		System.out.println();
		System.out.println("Play history:");
		System.out.println(savedPlayHistory);

		//Sort the cards
		cardList = MellowAIListener.sort(cardList);
		
		System.out.println("Cards in hand:");
		for(int i=0; i<cardList.size(); i++) {
			System.out.print(cardList.get(i) + " ");
		}
		System.out.println();
		
		//TODO: how do I know who I am?
		
		//TODO: Give User enough input to decide what to do
		//and alternatives
		System.out.println("Please play a card:");
		String play = in.nextLine().toUpperCase();
		
		System.out.println("Can you list alternative plays that aren't that bad?");
		String alternativeTODO = in.nextLine();
		
		return play;
		
	}

	@Override
	public String getBidToMake() {
		System.out.println();
		System.out.println();
		System.out.println("Score at start:");
		System.out.println(scoreAtStartOfRound);
		System.out.println("Bid history:");
		System.out.println(savedBidHistory);
		
		//Sort the cards
		cardList = MellowAIListener.sort(cardList);
		
		System.out.println("Cards in hand:");
		for(int i=0; i<cardList.size(); i++) {
			System.out.print(cardList.get(i) + " ");
		}
		System.out.println();
		
		//TODO: Give User enough input to decide what to do
		//and alternatives
		
		///TODO: display the cards in a similar way to getCardToPlay()
		System.out.println("What's your bid:");
		String bid = in.nextLine();
		
		if(bid.toLowerCase().startsWith("mellow")) { 
			bid = "0";
		}
		System.out.println("Can you list alternative bids that aren't that bad?");
		String alternativeTODO = in.nextLine();
		
		return bid;
	}

	@Override
	public void setNameOfPlayers(String[] players) {
		for(int i=0; i<players.length; i++) {
			playerNames[i] = players[i] + "";
		}
		
	}
	
	
	

}

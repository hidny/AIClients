package mellow.ai;

import java.util.ArrayList;
import java.util.Scanner;

public class MellowQueryUserForTestcase implements MellowAIDeciderInterface {

	
	private Scanner in = new Scanner(System.in);
	
	//TODO: use
	private ArrayList<String> cardList = null;
	
	int INDEX_CURRENT_PLAYER = 0;
	private String playerNames[] = new String[4];
	
	String savedPlayHistory = "";
	
	@Override
	public void receiveUnParsedMessageFromServer(String msg) {
		
	}

	@Override
	public void setDealer(String playerName) {
		
	}

	@Override
	public void receiveBid(String playerName, int bid) {
		
	}

	public void getPlayedCard(String playerName, String card) {
		
		//TODO: Add an indicator when fight is over...
		
		//Update the cards the current player has:
		if(playerName.equals(playerNames[INDEX_CURRENT_PLAYER])) {
			cardList.remove(card);
			savedPlayHistory = "";
		} else {
			savedPlayHistory += playerName + " played the " + card  + ".\n";
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
		
	}

	@Override
	public String getCardToPlay() {

		System.out.println("Play history:");
		System.out.println(savedPlayHistory);

		//Sort the cards
		cardList = sort(cardList);
		
		System.out.println("Cards in hand:");
		for(int i=0; i<cardList.size(); i++) {
			System.out.print(cardList.get(i) + " ");
		}
		System.out.println();
		//TODO: how do I know who I am?
		
		//TODO: Give User enough input to decide what to do
		//and alternatives
		return in.nextLine();
	}

	@Override
	public String getBidToMake() {
		//TODO: Give User enough input to decide what to do
		//and alternatives
		
		///TODO: display the cards in a similar way to getCardToPlay()
		
		return in.nextLine();
	}

	@Override
	public void setNameOfPlayers(String[] players) {
		for(int i=0; i<players.length; i++) {
			playerNames[i] = players[i] + "";
		}
		
	}
	
	
	
	//UTIL
	//Lazy O(n^2) sort: (a hand is only 13 cards... so the sorting of it could be inefficient for this purpose)
	public ArrayList<String> sort(ArrayList<String> cardList) {
		String tmp;
		
		for(int i=0; i<cardList.size(); i++) {
			for(int j=i+1; j<cardList.size(); j++) {
				if(getMellowCardNumber(cardList.get(i)) > getMellowCardNumber(cardList.get(j))  ) {
					tmp = cardList.get(i) + "";
					cardList.set(i, cardList.get(j) + "");
					cardList.set(j, tmp + "");
				}
			}
		}
		
		return cardList;
	}
	
	
	private static int getMellowCardNumber(String cardString) {
		int x = -1;
		int y = -1;
		if(cardString.charAt(0) >= '2' && cardString.charAt(0) <= '9') {
			x = (int)cardString.charAt(0) - (int)('2');
		} else if(cardString.charAt(0) == 'T') {
			x = 8;
		} else if(cardString.charAt(0) == 'J') {
			x = 9;
		} else if(cardString.charAt(0) == 'Q') {
			x = 10;
		} else if(cardString.charAt(0) == 'K') {
			x = 11;
		} else if(cardString.charAt(0) == 'A') {
			x = 12;
		} else {
			System.out.println("Number unknown! Uh oh!");
			System.exit(1);
		}
		
		if(cardString.charAt(1)=='S') {
			y = 0;
		} else if(cardString.charAt(1)=='H') {
			y = 1;
		} else if(cardString.charAt(1)=='C') {
			y = 2;
		} else if(cardString.charAt(1)=='D') {
			y = 3;
		} else {
			System.out.println("Suit unknown! Uh oh!");
			System.exit(1);
		}
		
		return y*13 - x;
	}
	

}

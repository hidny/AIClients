package mellow.cardUtils;

import java.util.ArrayList;

import mellow.Constants;

public class CardStringFunctions {

	public static int getIndexOfSuit(String card) {
		String suitString = card.charAt(1) + "";;
		int index = -1;
		if(suitString.equals("S")) {
			index = 0;
		} else if(suitString.equals("H")) {
			index = 1;
		} else if(suitString.equals("C")) {
			index = 2;
		} else if(suitString.equals("D")) {
			index = 3;
		} else {
			System.err.println("ERROR: unknown suit.");
			System.exit(1);
		}
		return index;
	}
	

	public static int[] organizeCardsBySuit(String cards[]) {
		int cardsPerSuit[] = new int[Constants.NUM_SUITS];
		
		for(int i=0; i<cardsPerSuit.length; i++) {
			cardsPerSuit[i] = 0;
		}
		
		for(int i=0; i<cards.length; i++) {
			cardsPerSuit[CardStringFunctions.getIndexOfSuit(cards[i])]++;
		}
		
		return cardsPerSuit;
	}
	
   
	//UTIL
	//Lazy O(n^2) sort: (a hand is only 13 cards... so the sorting of it could be inefficient for this purpose)
	public static String[] sort(String cardList[]) {
		String tmp;
		
		for(int i=0; i<cardList.length; i++) {
			for(int j=i+1; j<cardList.length; j++) {
				if(getMellowCardNumber(cardList[i]) > getMellowCardNumber(cardList[j])  ) {
					tmp = cardList[i] + "";
					cardList[i] = cardList[j] + "";
					cardList[j] = tmp + "";
				}
			}
		}
		
		return cardList;
	}

	//Sort array List instead of string array: (This is for the user... and the can wait for a sort of 13 cards)
	public static ArrayList<String> sort(ArrayList<String> cardList) {
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
			System.err.println("Number unknown! Uh oh!");
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
			System.err.println("Suit unknown! Uh oh!");
			System.exit(1);
		}
		
		return y*13 - x;
	}
}

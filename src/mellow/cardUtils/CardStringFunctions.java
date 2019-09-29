package mellow.cardUtils;

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
			System.out.println("ERROR: unknown suit.");
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
}

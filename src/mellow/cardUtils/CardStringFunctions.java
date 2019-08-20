package mellow.cardUtils;

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
	
}

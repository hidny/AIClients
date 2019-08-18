package mellow.cardUtils;

import java.util.ArrayList;

//Class filled with mellow util functions.
// Will organize as this files gets bigger
public class handUtilsQueryForTestcase {

	//check if player has only one choice of cards to play
	public static boolean hasOnlyOneChoice(String leadingCard, ArrayList<String> cardList) {
		if(cardList.size() <= 1) {
			return true;
		}
		
		int numCardsToFollowSuit = 0;
		
		String suitLeadingCard = getSuitString(leadingCard);
		for(int i=0; i<cardList.size(); i++) {
			String currentSuit = getSuitString(cardList.get(i));
			if(currentSuit.equals(suitLeadingCard)) {
				numCardsToFollowSuit++;
			}
		}
		
		if(numCardsToFollowSuit == 1) {
			return true;
		} else {
			return false;
		}
		
	}
	
	public static String getSuitString(String card) {
		return card.toUpperCase().substring(1, 2);
	}
	
	//pre: there's only 1 card to play:
	//post: returns the card string of the only card to play
	public static String getOnlyCardToPlay(String leadingCard, ArrayList<String> cardList) {
		if(hasOnlyOneChoice(leadingCard, cardList) == false) {
			System.err.println("ERROR: getting only card to play when there's multiple options");
			System.exit(1);
		}
		
		if(cardList.size() == 1) {
			return cardList.get(0);
		}
		
		int numCardsToFollowSuit = 0;
		
		String suitLeadingCard = getSuitString(leadingCard);
		for(int i=0; i<cardList.size(); i++) {
			String currentSuit = getSuitString(cardList.get(i));
			if(currentSuit.equals(suitLeadingCard)) {
				return cardList.get(i);
			}
		}
		
		System.err.println("ERROR: getOnlyCardToPlay function couldn\'t find single card to play");
		System.exit(1);
		return "ERROR";
	}
	
}

package mellow.cardUtils;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;

public class DebugFunctions {

	/*
	 *Example use:
	 *Let's say the hand is:
	  9S 5S QH 8H 7H AC TC 6C 2C
		
		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "9S 5S QH 8H 7H AC TC 6C 2C")) {
			System.out.println("DEBUG! ahh");
		}
		
	 */
	
	public static boolean currentPlayerHoldsHandDebug(DataModel dataModel, String hand) {
		
		String tokens[] = hand.trim().split(" ");
		
		for(int i=0; i<tokens.length; i++) {
		
			if(dataModel.hasCard(tokens[i]) == false) {
				return false;
			}
		}
		
		if(dataModel.getNumCardsInCurrentPlayerHand() != tokens.length) {
			return false;
		}
		
		
		return true;
	}
	
	public static String DebugGetCurrentPlayerHand(DataModel dataModel) {
		String ret ="";
		for(int s=0; s<Constants.NUM_SUITS; s++) {
			for(int r=DataModel.ACE; r>=DataModel.RANK_TWO; r--) {
				if(dataModel.hasCard(DataModel.getCardString(r, s))) {
					ret += DataModel.getCardString(r, s) + " ";
				}
			}
		}
		return ret;
	}
}

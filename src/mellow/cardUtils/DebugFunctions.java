package mellow.cardUtils;

import mellow.ai.cardDataModels.DataModel;

public class DebugFunctions {

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
}

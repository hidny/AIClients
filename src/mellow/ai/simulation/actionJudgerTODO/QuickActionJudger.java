package mellow.ai.simulation.actionJudgerTODO;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.cardDataModels.normalPlaySignals.VoidSignalsNoActiveMellows;
import mellow.cardUtils.CardStringFunctions;
import mellow.cardUtils.DebugFunctions;

public class QuickActionJudger {

	//This will just have rules about what kind of cards the players should have and apply those rules
	// after a distribution of cards is given.
	// ex: player has QD or is void diamond.
	
	
	public int queenOrVoidBecauseOfKLead[] = new int[Constants.NUM_SUITS];
	
	
	public QuickActionJudger(DataModel dataModel) {
		
		initVars();
		
		
		//No mellow case because that's too complicated:
		if(dataModel.someoneBidMellow() == false
				|| dataModel.stillActiveMellow() == false) {
			
			//TODO: reuse this function once completed:
			for(int s=0; s<Constants.NUM_SUITS; s++) {
				
				if(s == Constants.SPADE) {
					continue;
				}
				if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "QS 9S 8S KH JH 7H 7C QD 6D 3D ")) {
					System.out.println("Debug");
				}
				
				int playerIndex = dataModel.signalHandler.getPlayerIndexOfKingSacrificeForSuit(s);
				
				//TODO: put into function:
				
				if(playerIndex != VoidSignalsNoActiveMellows.NO_KING_SACRIFICE) {
					
					//2 possible ways...
					
					if(playerIndex == Constants.CURRENT_AGENT_INDEX) {
						//Never mind your own signals for now:
						continue;
					}
					
					if(dataModel.isVoid(playerIndex, s)) {
						continue;
					}
					
					//If player only played K then void or Q
					//If player played off low after, then Q (but normal signals should capture this)
					
					
					//TODO: this should be a function
					boolean queenUnknown = ! dataModel.isCardPlayedInRound(DataModel.getCardString(DataModel.QUEEN, s))
							&& ! dataModel.hasCard(DataModel.getCardString(DataModel.QUEEN, s));
					
					int playerWhoPlayedQ = dataModel.getWhoPlayedCard(s, DataModel.QUEEN);
					
					if(dataModel.getNumberOfCardsPlayerPlayedInSuit(playerIndex, s) == 1
							&& queenUnknown) {

						System.out.println("---");
						System.out.println("NOTE: Because of the king lead:");
						//Player is void or has queen.
						System.out.println(dataModel.getPlayers()[playerIndex] + " is void in " + CardStringFunctions.getSuitString(s) + " or has " + DataModel.getCardString(DataModel.QUEEN, s));
						
						queenOrVoidBecauseOfKLead[s] = playerIndex;
						
						//The quickActionJudger records this and use this as a pre-filter (hopefully, it won't filter too much)
						// Maybe I should add this logic in the signal handler...
						// I'll only do it if the needs arises.
						
						
					} else if(dataModel.getNumberOfCardsPlayerPlayedInSuit(playerIndex, s) > 1
							&& queenUnknown) {
						
						//This won't happen because as soon as king leader plays 2nd low card,
						// signal handler knows what it means and removes dataModel.signalHandler.getPlayerIndexOfKingSacrificeForSuit(s)
						
						
					} else if(dataModel.getNumberOfCardsPlayerPlayedInSuit(playerIndex, s) == 1
							&& ! queenUnknown
							&& playerWhoPlayedQ != playerIndex
							) {

						//Signal handler should know phil is void in diamonds...
						//I think I tested this?
						System.out.println("---");
						System.out.println("NOTE: Because of the king lead and the fact that queen is in-hand or played by someone other than the King leader:");
						//Player is void or has queen.
						System.out.println(dataModel.getPlayers()[playerIndex] + " is void in " + CardStringFunctions.getSuitString(s));
					}
					
					
				}
			}
			//END TODO: put into function
		}
		
	}
	
	private static final int NO_SIGNAL = -1;
	
	public void initVars() {
		for(int i=0; i<Constants.NUM_SUITS; i++) {
			queenOrVoidBecauseOfKLead[i] = NO_SIGNAL;
		}
	}
	
	public boolean actionFilterAcceptsDistribution(DataModel dataModel, String distCards[][], boolean verbose) {
		
		for(int s=0; s<Constants.NUM_SUITS; s++) {
			
			//Check queen or void:
			if(s != Constants.SPADE
					&& queenOrVoidBecauseOfKLead[s] > Constants.CURRENT_AGENT_INDEX) {
				
				int playerIndex = queenOrVoidBecauseOfKLead[s];
				
				String theQueen = DataModel.getCardString(DataModel.QUEEN, s);
				boolean voidSoFar = true;
				boolean hasTheQueen = false;
				
				for(int j=0; j<distCards[playerIndex].length; j++) {
					if(distCards[playerIndex][j].equals(theQueen)) {
						hasTheQueen = true;
						break;
					} else if(CardStringFunctions.getIndexOfSuit(distCards[playerIndex][j]) == s) {
						voidSoFar = false;
					}
				}
				
				if(hasTheQueen || voidSoFar) {
					//all good!
				} else {
					if(verbose) {
						System.err.println("Quick action Judgers says no because Klead player (" + dataModel.getPlayers()[playerIndex] + ") was not void and didn't have the queen (" + theQueen + ")");

						//testPrintDistCards(dataModel, distCards);
					}
					return false;
				}
				
				if(verbose) {
					System.err.println("Quick action Judgers says yes because Klead player (" + dataModel.getPlayers()[playerIndex] + ") was void or had the queen (" + theQueen + ")");
					testPrintDistCards(dataModel, distCards);
				}
			}
		}
		
		
		//Default to being good:
		return true;
	}
	
	
	public static void testPrintDistCards(DataModel dataModel, String distCards[][]) {
	
		System.err.println("Dist cards:");
		for(int i=0; i<distCards.length; i++) {
			System.err.print(dataModel.getPlayers()[i] + ": ");
			for(int j=0; j<distCards[i].length; j++) {
				System.err.print(distCards[i][j] + " ");
			}
			System.err.println();
		}

		System.err.println("End dist cards");
		System.err.println("---");
	}
}

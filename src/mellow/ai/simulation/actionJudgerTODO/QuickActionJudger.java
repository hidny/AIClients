package mellow.ai.simulation.actionJudgerTODO;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.cardDataModels.normalPlaySignals.VoidSignalsNoActiveMellows;
import mellow.cardUtils.CardStringFunctions;
import mellow.cardUtils.DebugFunctions;

public class QuickActionJudger {

	//This will just have rules about what kind of cards the players should have
	// ex: QD or void diamond.
	
	//TODO: build a constructor that's given a start dataModel and the actions,
	// and outputs an object with rules to follow (and an effective filter)
	
	//TODO: Make this a filter for monteCarloMain if there's some quick rules to use.
	//TODO: test with test cases 2-3 earlier than 3-4835, so there could actually be quick rules
	// to test with.
	
	
	//TODO: Make MonteCarloMain Use this at line 420 (or earlier)
	public QuickActionJudger(DataModel dataModel) {
		
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

						System.err.println("---");
						System.err.println("NOTE: Because of the king lead:");
						//Player is void or has queen.
						System.err.println(dataModel.getPlayers()[playerIndex] + " is void in " + CardStringFunctions.getSuitString(s) + " or has " + DataModel.getCardString(DataModel.QUEEN, s));
						
						//TODO: record this and use this as a pre-filter (hopefully, it won't filter too much)
						
					} else if(dataModel.getNumberOfCardsPlayerPlayedInSuit(playerIndex, s) > 1
							&& queenUnknown) {
						
						//This won't happen because as soon as king leader plays 2nd low card,
						// signal handler knows what it means and removes dataModel.signalHandler.getPlayerIndexOfKingSacrificeForSuit(s)
						// So This is safe to delete!
						
						/*
						System.err.println("---");
						
						System.err.println("TEST: Because of the king lead and the fact that mult cards of suit played:");
						System.err.println("(TEST: this should be known to mellow signals)");
						//Player is void
						System.err.println(dataModel.getPlayers()[playerIndex] + " has " + DataModel.getCardString(DataModel.QUEEN, s) );
						*/
					} else if(dataModel.getNumberOfCardsPlayerPlayedInSuit(playerIndex, s) == 1
							&& ! queenUnknown
							&& playerWhoPlayedQ != playerIndex
							) {

						//TODO: signal handler should know phil is void in diamonds...
						System.err.println("---");
						System.err.println("NOTE: Because of the king lead and the fact that queen is in-hand or played by someone other than the King leader:");
						//Player is void or has queen.
						System.err.println(dataModel.getPlayers()[playerIndex] + " is void in " + CardStringFunctions.getSuitString(s));
					}
					
					//END TODO: put into function
					
				}
			}
		}
		
	}
}

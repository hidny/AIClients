package mellow.ai.situationHandlers;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;

public class PartnerSaidMelllowSituation {

	//Play move to protect partner mellow
	//The optimal play here is really complicated...
	//I'll just approx by writing a simple answer that's easy to read
	
	public static String playMoveToProtectPartnerMellow(DataModel dataModel) {
		
		//TODO: test!
		
		//Advise for refining this function:
		//TODO: Instead of making it perfect considering all factors, play test it and see when it gets into trouble
		//EX: don't worry about counting number of cards left in suit and number of spades left and blah blah blah...
		//EX2: if player needs to play unsafe card, don't worry about having no common-sense to deal with it.
		
		//1st prio:
		if(dataModel.currentThrowIsLeading()) {
			
			int bestSuitIndexToPlay = -1;
			int valueOfBestSuitPlay = -1;

			System.out.println("DEBUG: reached mellow protection test. Test0!");
			
			//1st check that there's an offsuit current player has, but mellow signalled they don't have:
			for(int curSuit=0; curSuit<Constants.NUM_SUITS; curSuit++) {
				if(dataModel.currentAgentHasSuit(curSuit)) {

					if(dataModel.mellowPlayerSignalNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, curSuit)) {
						//if mellow player signal no to suit
						
						//TODO: check if any test cas even reaches this point
						System.out.println("DEBUG: reached mellow protection test. Test1!");
						
						//value of suit: num cards opponents have of suit
						int numCardsOpponentsCurrentlyHaveOfSuit = Constants.NUM_RANKS -
							dataModel.getNumCardsPlayedForSuit(curSuit)
							 - dataModel.getNumCardsCurrentUserStartedWithInSuit(curSuit);
						
						int currentValueOfSuitPlay = numCardsOpponentsCurrentlyHaveOfSuit;
						
						//If current player has master of suit:
						//that's good! (I can't tell how good, but it's pretty.. pretty.. pretty good.)
						if(dataModel.currentPlayerHasMasterInSuit(curSuit)) {
							//Add 2 to value because why not?
							currentValueOfSuitPlay += 2;
						}
						
						//Exceptions:
						//if opponent to the right is void and if opponent can trump: playing suit is not great
						//playing this suit is good but not great... but maybe it's still the best choice...
						if(curSuit != Constants.SPADE && dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, curSuit)
								&& dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE) == false) {
							currentValueOfSuitPlay = 1;
						}

						//if suit is trump, see if there's another suit
						if(curSuit == Constants.SPADE) {
							currentValueOfSuitPlay = 1;
						}
						//TODO: I don't know what I'd choose between leading trump and leading into opponent because it's context dependant
						//Whatever!
						
						if(currentValueOfSuitPlay > valueOfBestSuitPlay) {
							
							bestSuitIndexToPlay = curSuit;
							valueOfBestSuitPlay = currentValueOfSuitPlay;
						}
					}
						
					
				}
			}
			
			if(bestSuitIndexToPlay != -1) {
				if(dataModel.currentPlayerHasMasterInSuit( bestSuitIndexToPlay)) {
					return dataModel.getMasterInSuit( bestSuitIndexToPlay);
				} else {
					return dataModel.getCardCurrentPlayerGetLowestInSuit( bestSuitIndexToPlay);
				}
				
			}
			
			//At this point, there's no obvious lead...
			
			//Try to play to a master (if applicable)
			for(int curSuit=0; curSuit<Constants.NUM_SUITS; curSuit++) {
				if(dataModel.currentPlayerHasMasterInSuit(curSuit)) {
					
					//TODO: this is really dangerous if mellow protector doesn't have a high second card...
					//Ex: A 3 2, and you lead the A leaving the 3 and 2.

					return dataModel.getMasterInSuit(curSuit);
				}
			}
			
			String bestCardToPlay = null;
	
			valueOfBestSuitPlay = -1;

			//Try to play the lowest card above mellow player safe signal in offsuit:
			for(int curSuit=0; curSuit<Constants.NUM_SUITS; curSuit++) {

				System.out.println("DEBUG: reached mellow protection test where we're just trying to lead a nice card over mellow player. Test2!");
				
				//?? I'll need to make up a value judgement...
				int currentValueOfSuitPlay = -1;
				String currentCardToPlay = null;
				
				//int currentValueOfSuitPlay = numCardsOpponentsHaveOfSuit;
				int numCardsOtherPlayersCurrentlyHaveOfSuit = Constants.NUM_RANKS -
						dataModel.getNumCardsPlayedForSuit(curSuit)
						 - dataModel.getNumCardsCurrentUserStartedWithInSuit(curSuit);
				
				String maxRankMellowPartnerCard = dataModel.getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(Constants.CURRENT_PARTNER_INDEX, curSuit);
				
				if(maxRankMellowPartnerCard != null
					&& dataModel.couldPlayCardInHandOverCardInSameSuit(maxRankMellowPartnerCard)) {
					currentCardToPlay = dataModel.getCardInHandClosestOverSameSuit(maxRankMellowPartnerCard);
					
					//TODO: I just made this formula up because I felt like it... 
					currentValueOfSuitPlay = Constants.NUM_RANKS - 
							dataModel.getRankIndex(currentCardToPlay)
							+ (numCardsOtherPlayersCurrentlyHaveOfSuit / 2);
				}
				
				
				//Exception:
				//if opponent to the right is void and if opponent can trump: playing suit is not great
				if(curSuit != Constants.SPADE && dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, curSuit)
						&& dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE) == false) {
					currentValueOfSuitPlay = -2;
				}
				
				if(currentValueOfSuitPlay > valueOfBestSuitPlay) {
					bestCardToPlay = currentCardToPlay;
					valueOfBestSuitPlay = currentValueOfSuitPlay;
				}
				
				
			}
			if(bestCardToPlay != null) {
				return bestCardToPlay;
			}
			
			
			//Play spade?
			if(dataModel.currentAgentHasSuit(Constants.SPADE)) {
				return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
			} else {
				//play random highest card
				return dataModel.getHighestOffSuitCardAnySuit();
			}
			
			
		} else {
			//TODO: do this later:
			//For now, just test mellow lead
			
			//TODO: throw index should be a dataModel functionL
			int throwIndex = dataModel.getCardsPlayedThisRound() % Constants.NUM_PLAYERS;
			
			if(throwIndex == 1) {
			
			//if 2nd thrower:
				//if void and lead suit is off-suit
					//if could trump:
						//If mellow player partner seems vulnerable based on signals: trump
						//else dont trump
				
					//else: throw out garbage card...
						//Later try to pick best garbage card 
				//else if void and lead suit is trump
					//play garbage
				//else if you need to follow suit
					//Get highest (I know this isn't always a good idea, but whatever man!)
					
					//Exception: if mellow player is not vulnerable this round and made more vulnerable next round... maybe don't!)
					//(Example: lead K and playing A is questionable if you have a 2 or 3 to backup the A)
			} else if(throwIndex == 2) {
				
				//Note: this could only happen on the 1st round where mellow player leads.
				System.out.println("TESTING PROTECTOR 3rd throw");
				
				int leadSuit = dataModel.getSuitOfLeaderThrow();
				
				String currentFightWinnerCard = dataModel.getCurrentFightWinningCard();
				
				if(currentFightWinnerCard == dataModel.getCardLeaderThrow()) {
		
					if(dataModel.throwerHasCardToBeatCurrentWinner()) {
						//if mellow in danger:
						
						//TODO: if 3rd and want to lead again: play master
						
						//play just above to protect
						return dataModel.getCardClosestOverCurrentWinner();
						
					} else {
						
						//Mellow might be lost, just play low :(
						if(dataModel.currentAgentHasSuit(leadSuit)) {
							return dataModel.getCardCurrentPlayerGetLowestInSuit(leadSuit);
						} else {
							return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
						}
						
					}
					
					
					
				} else {

					//Just play low: bad logic, but whatever!
					if(dataModel.currentAgentHasSuit(leadSuit)) {
						return dataModel.getCardCurrentPlayerGetLowestInSuit(leadSuit);
					} else {
						return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
					}
					
					//TODO later: complex logic (Maybe start with stealing from 4th throw logic)
					
				}
				
			} else if(throwIndex == 3) {

				System.out.println("TESTING PROTECTOR 4th throw");
			
				int leadSuit = dataModel.getSuitOfLeaderThrow();
				
				String currentFightWinnerCard = dataModel.getCurrentFightWinningCard();
				
				if(currentFightWinnerCard == dataModel.getCardSecondThrow()) {

					//if mellow in danger:
					//play just above to protect
					if(dataModel.throwerHasCardToBeatCurrentWinner()) {
						return dataModel.getCardClosestOverCurrentWinner();
						
					} else {
						
						//Mellow is lost, just play low :(
						if(dataModel.currentAgentHasSuit(leadSuit)) {
							return dataModel.getCardCurrentPlayerGetLowestInSuit(leadSuit);
						} else {
							return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
						}
						
					}
					
				} else {
					
					//Just play low: bad logic, but whatever!
					if(dataModel.currentAgentHasSuit(leadSuit)) {
						return dataModel.getCardCurrentPlayerGetLowestInSuit(leadSuit);
					} else {
						return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
					}
					
				
					//TODO later: complex logic (This is a complex situation!)
					//play to take if it doesn't endanger mellow in next round of same suit
					//or play to take if could lead nice high value card next round
					//or play to take if you need the trick...
			
					//play low if playing high endangers mellow card in next round of same suit
				}

					
					
			} else {
				System.out.println("ERROR: unexpected branching in playMoveToProtectPartnerMellow");
				System.exit(1);
			}
			
			return NoMellowBidPlaySituation.handleNormalThrow(dataModel);
		}
	}
	
}

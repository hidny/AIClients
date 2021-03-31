package mellow.ai.cardDataModels.handIndicators;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;

public class NonMellowBidHandIndicators {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	
	
	//2nd Priority:
	//TODO: Make indicator for trumping a suit:
	// couldMakeATrumpTrick
	//Making a trumping trick is different...
	
	//3rd Priority
	//TODO: Add indicator:
	//Look for the potential to make several of the same suit...
	//Maybe borrow baseball terminology.
	
	//4th Priority
	//TODO: add logic: if you & partner need 1 more trick and partner not leading,
	//then if opponents both void, then suit is useless unless you could burn them with it...
	
	//5th Priority
	//TODO (eventually/low-priority): Add indicator: Save a card just to intimidate
	// This should indicate that if you are throwing away the card of suit s,
	//    your opponents know you're weak in suit s, and that's bad.
	
	
	//1st prio:
	//TODO: test
	
	//TODO: implement function:
	    //getCouldMakeAFollowTrickRatingMinusACard(DataModel dataModel, int suitIndex)
	//AND:
		//See diff in rating after throwing lowest...
	
	public static double getCouldMakeAFollowTrickRating(DataModel dataModel, int suitIndex) {

		int numCardsOfSuitInHand = dataModel.getNumCardsOfSuitInCurrentPlayerHand(suitIndex);
		
		if(numCardsOfSuitInHand ==0) {
			return 0.0;
		}

		int numCardsOfSuitInOtherHands = dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex);
		
		
		if(dataModel.currentPlayerHasMasterInSuit(suitIndex)) {
			
			double otherCardFactor = 0.2 * numCardsOfSuitInOtherHands;
			
			if(numCardsOfSuitInHand > 1) {
				return 10.0 + otherCardFactor;
			} else {
				return 9.0 + otherCardFactor;
			}
		} else if(hasKEquiv(dataModel, suitIndex)) {

			//TODO: make this better than just a linear adjustment...
			double otherCardFactor = 0.2 * (numCardsOfSuitInOtherHands - 1);
			
			//If only the A equiv is out:
			if(numCardsOfSuitInOtherHands <=1) {
				return 0.0;
			}
			
			if(numCardsOfSuitInHand == 1) {
				return 2.0;
			} else if(numCardsOfSuitInHand >= 2 ) {
				
				int numInbetween = 
						  dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(
								dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex))
						- dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(
								dataModel.getCardCurrentPlayerGetSecondHighestInSuit(suitIndex));
				
				double tripleFactor = 0.0;
				if(numCardsOfSuitInHand >= 3) {
					tripleFactor += 0.5;
				}
				
				if(numInbetween == 0) {
					return 8.0 + tripleFactor + otherCardFactor;
				} else if(numInbetween == 1) {
					return 7.0 + tripleFactor + otherCardFactor;
				} else if(numInbetween == 2) {
					return 6.0 + tripleFactor + otherCardFactor;
				} else if(numInbetween >= 3) {
					return 5.0 + tripleFactor + otherCardFactor;
				}
				
			}
			
		} else if(hasQEquiv(dataModel, suitIndex)) {

			//TODO: make this better than just a linear adjustment...
			double otherCardFactor = 0.2 * (numCardsOfSuitInOtherHands - 2);

			//If only the AK equiv is out:
			if(numCardsOfSuitInOtherHands <= 2) {
				return 0.0;
			}
			
			if(numCardsOfSuitInHand < 3) {
				
				if(numCardsOfSuitInHand == 2) {
					return 1.0;
				} else {
					return 0.0;
				}
				
			} else  {
				
				int numInbetween = 
						  dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(
								dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex))
						- dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(
								dataModel.getCardCurrentPlayerGetSecondHighestInSuit(suitIndex));
				
				//int numInbetween2 = 
				//		dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(
				//				dataModel.getCardCurrentPlayerGetSecondHighestInSuit(suitIndex))
					//	- dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(
					//			dataModel.getCardCurrentPlayerGetThirdHighestInSuit(suitIndex));
				
				if(numInbetween == 0) {
					return 7.0 + otherCardFactor;
				} else if(numInbetween == 1) {
					return 6.0 + otherCardFactor;
				} else if(numInbetween == 2) {
					return 5.0 + otherCardFactor;
				} else if(numInbetween >= 3) {
					return 4.0 + otherCardFactor;
				}
			}
			
		} else if(numCardsOfSuitInHand >=4) {

			//TODO: make this better than just a linear adjustment...
			double otherCardFactor = 0.2 * (numCardsOfSuitInOtherHands - 3);
			
			//If only the AKQ equiv is out:
			if(numCardsOfSuitInOtherHands <= 3) {
				
				return 0.0;
			}
			
			int numOver = 
					dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(
							dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex));
			
			int numUnder =
					dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(
							dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex));
			
			if(numOver == 0) {
				return 0.0;
			} else if(numOver - numUnder > 0) {

				return 6.0 + otherCardFactor;
			} else {
				return 6.0 - numOver + numUnder + otherCardFactor;
			}
			
			
		} else {
			
			return 0.0;
		}
		return numCardsOfSuitInHand;
		
	}
	
	public static boolean hasKQEquiv(DataModel dataModel, int suitIndex) {
		
		if(dataModel.getNumberOfCardsOneSuit(suitIndex) < 2) {
			return false;
		}
		
		String cardA = dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex);
		String cardB = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(suitIndex);
		
		int numOver = 0;
		
		for(int curRank = dataModel.ACE; curRank > DataModel.getRankIndex(cardB); curRank--) {
			if(dataModel.getCardsCurrentlyHeldByPlayers()[Constants.CURRENT_AGENT_INDEX][suitIndex][curRank] == DataModel.CERTAINTY) {
				continue;
	
			} else if(dataModel.isCardPlayedInRound(
					dataModel.getCardString(curRank, suitIndex))
					) {
				continue;
	
			} else {
				numOver++;
				if(numOver > 1) {
					return false;
				}
			}
		}
		
		if(numOver == 1) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean hasKEquiv(DataModel dataModel, int suitIndex) {
		
		if(dataModel.getNumberOfCardsOneSuit(suitIndex) < 1) {
			return false;
		}
		
		String cardA = dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex);
		
		int numOver = 0;
		
		for(int curRank = dataModel.ACE; curRank > DataModel.getRankIndex(cardA); curRank--) {
			if(dataModel.getCardsCurrentlyHeldByPlayers()[Constants.CURRENT_AGENT_INDEX][suitIndex][curRank] == DataModel.CERTAINTY) {
				continue;
	
			} else if(dataModel.isCardPlayedInRound(
					dataModel.getCardString(curRank, suitIndex))
					) {
				continue;
	
			} else {
				numOver++;
				if(numOver > 1) {
					return false;
				}
			}
		}
		
		if(numOver == 1) {
			return true;
		} else {
			return false;
		}
	}

	//TODO: put into data model??
	 public static boolean hasQEquiv(DataModel dataModel, int suitIndex) {
		
		if(dataModel.getNumberOfCardsOneSuit(suitIndex) < 1) {
			return false;
		}
		
		String cardA = dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex);
		
		int numOver = 0;
		
		for(int curRank = dataModel.ACE; curRank > DataModel.getRankIndex(cardA); curRank--) {
			if(dataModel.getCardsCurrentlyHeldByPlayers()[Constants.CURRENT_AGENT_INDEX][suitIndex][curRank] == DataModel.CERTAINTY) {
				continue;
	
			} else if(dataModel.isCardPlayedInRound(
					dataModel.getCardString(curRank, suitIndex))
					) {
				continue;
	
			} else {
				numOver++;
				if(numOver > 2) {
					return false;
				}
			}
		}
	
		if(numOver == 2) {
			return true;
		} else {
			return false;
		}
	}
	
	//Just start with functions.

}

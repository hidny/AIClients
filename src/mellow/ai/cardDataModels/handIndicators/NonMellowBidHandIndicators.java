package mellow.ai.cardDataModels.handIndicators;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.cardUtils.DebugFunctions;

public class NonMellowBidHandIndicators {


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
	
	
	

	
	//2nd Priority:
	//Make indicator for trumping a suit:
	// couldMakeATrumpTrick
	//Making a trumping trick is different...
	
	//For convenience, let's say this calculation comes up at the beginning of the round...
	
	// I have extra function so I can compare rating before/after throwing off lowest card of suit...
	

	public static double getCouldTrumpSuitAndWinRatingMinusLowSpade(DataModel dataModel, int suitIndex) {
		return getCouldTrumpSuitAndWinRating( dataModel, suitIndex, true, false);
	}
	
	public static double getCouldTrumpSuitAndWinRatingMinusLowOffsuit(DataModel dataModel, int suitIndex) {
		return getCouldTrumpSuitAndWinRating( dataModel, suitIndex, false, true);
	}
	
	public static double getCouldTrumpSuitAndWinRating(DataModel dataModel, int suitIndex) {
		return getCouldTrumpSuitAndWinRating( dataModel, suitIndex, false, false);
	}
	
	//TODO: LHS or RHS throwing off suit should have a high hit on the rating (-1 instead of -1/3)..
	public static double getCouldTrumpSuitAndWinRating(DataModel dataModel, int suitIndex, boolean lowestSpadeThrown, boolean lowestCardOfSuitThrown) {

		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "TS AH KH 6H 2H 5C ")) {
			System.out.println("Debug");
		}

		int numSpadesInHand = dataModel.getNumCardsOfSuitInCurrentPlayerHand(Constants.SPADE);
		if(lowestSpadeThrown) {
			numSpadesInHand = numSpadesInHand - 1;
			if(numSpadesInHand <=0){
				
				if(numSpadesInHand < 0) {
					System.out.println("ERROR: Calling getCouldTrumpSuitAndWinRatingMinusLowSpade when you already have 0 spades!");
					//Let it slide for debug purposes...
				}
				
				return 0.0;
			}
		}
		
		int numCardsOfSuitInHand = dataModel.getNumCardsOfSuitInCurrentPlayerHand(suitIndex);

		if(lowestCardOfSuitThrown) {
			numCardsOfSuitInHand = numCardsOfSuitInHand - 1;
			if(numCardsOfSuitInHand < 0) {
				System.out.println("ERROR: Calling getCouldTrumpSuitAndWinRatingMinusLowOffsuit when you already have 0 offuits!");
				System.exit(1);
			}
		}

		int numCardsOfSuitInOtherHands = dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex);
		
		if(numSpadesInHand == 0 
				|| suitIndex == Constants.SPADE
				|| numCardsOfSuitInOtherHands == 0) {
			return 0.0;
		}
		
		double spadeVulnerability = 0.0;
		
		//Rough estimate of "if player's spades are vulnerable, that should affect the ratings..."
		if((numSpadesInHand == 1 && dataModel.currentPlayerHasMasterInSuit(Constants.SPADE))) {
			//All good...
		} else if(
			   (numSpadesInHand == 2 && hasKEquiv(dataModel, Constants.SPADE))
			|| (numSpadesInHand == 3 && hasQEquiv(dataModel, Constants.SPADE))) {
			spadeVulnerability = 1.0;
			
		} else if(getCouldMakeAFollowTrickRating(dataModel, 0, lowestSpadeThrown) <= 5.0) {
			//Get num Other playes not void.
			int numOtherPlayersNotVoidSpade = 0;
			if(! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)) {
				numOtherPlayersNotVoidSpade++;
			}
			
			if(! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)) {
				numOtherPlayersNotVoidSpade++;
			}
			
			if(! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, Constants.SPADE)) {
				numOtherPlayersNotVoidSpade++;
			}
			//End get num other players not void
			
			spadeVulnerability = Math.max(0, 1 + dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE)
					- 1.0 * numOtherPlayersNotVoidSpade * numSpadesInHand);
		}
		//End of rough estimate
				
		//TODO: RM isVoid (It should not make a diff)
		boolean isLHSVoid = dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, suitIndex);
		
		boolean isLHSVoidSpade = dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.LEFT_PLAYER_INDEX, Constants.SPADE);
		
		if(isLHSVoidSpade) {
			spadeVulnerability = 0.0;
		} 
		
		if(numCardsOfSuitInHand == 0) {
			
			if(! isLHSVoidSpade
					&& isLHSVoid) {
				return 0.0 - spadeVulnerability;
				
			} else if(numCardsOfSuitInOtherHands == 1) {
				return 0.0 - spadeVulnerability;
				
			} else if(numCardsOfSuitInOtherHands == 2) {
				return 1.0 - spadeVulnerability;
				
			} else {
				return Math.min(9.0, numCardsOfSuitInOtherHands - 1.0 - spadeVulnerability);
			}
		} else {
			
			//Get num Other playes not void.
			int numOtherPlayersNotVoid = 0;
			if(! isLHSVoid) {
				numOtherPlayersNotVoid++;
			}
			
			if(! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.RIGHT_PLAYER_INDEX, suitIndex)) {
				numOtherPlayersNotVoid++;
			}
			
			if(! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, suitIndex)) {
				numOtherPlayersNotVoid++;
			}
			//End get num other players not void
			
			int numCardsOtherHaveInHandWhenVoid = numCardsOfSuitInOtherHands - numCardsOfSuitInHand * numOtherPlayersNotVoid;
			

			if(! isLHSVoidSpade
					&& isLHSVoid) {
				return 0.0 - spadeVulnerability;
				
			} else if(numCardsOtherHaveInHandWhenVoid <= 1) {
				return 0.0 - spadeVulnerability;
				
			} else if(numCardsOtherHaveInHandWhenVoid == 2) {
				return 1.0 - spadeVulnerability;
				
			} else {
				return Math.min(9.0, numCardsOtherHaveInHandWhenVoid - spadeVulnerability);
			}
			
		}
	}
	
	

	//1st Priority
	//Get a rating for the possibility of making a a non-lead and non-trumping trick for each suit
	public static double getCouldMakeAFollowTrickRating(DataModel dataModel, int suitIndex) {
		
		return getCouldMakeAFollowTrickRating(dataModel, suitIndex, false);
	}
	
	//Make a function to see what happen to the followTrick rating after throwing off lowest card of suit:
	public static double getCouldMakeAFollowTrickRatingMinusLowCard(DataModel dataModel, int suitIndex) {
		
		return getCouldMakeAFollowTrickRating(dataModel, suitIndex, true);
	}
	
	private static double getCouldMakeAFollowTrickRating(DataModel dataModel, int suitIndex, boolean lowCardThrownOff) {

		int numCardsOfSuitInHand = dataModel.getNumCardsOfSuitInCurrentPlayerHand(suitIndex);
		
		if(lowCardThrownOff) {
			numCardsOfSuitInHand = numCardsOfSuitInHand - 1;
		}
		
		if(numCardsOfSuitInHand ==0) {
			return 0.0;
		}

		int numCardsOfSuitInOtherHands = dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex);
		
		if(numCardsOfSuitInOtherHands == 0) {
			return 0.0;
		}
		
		if(dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(
								dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex)) == 0) {
			return 0.0;
		}
		
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
			} else {
				
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
				} else {
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
				} else {
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
		
	}
	
	
	public static boolean hasKQEquivAndNoAEquiv(DataModel dataModel, int suitIndex) {
		
		if(dataModel.getNumberOfCardsOneSuit(suitIndex) < 2) {
			return false;
		}
		
		String secondHighestCard = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(suitIndex);
		
		int numOver = 0;
		
		for(int curRank = dataModel.ACE; curRank > DataModel.getRankIndex(secondHighestCard); curRank--) {
			if(dataModel.getCardsCurrentlyHeldByPlayers()[Constants.CURRENT_AGENT_INDEX][suitIndex][curRank] == DataModel.CERTAINTY) {
				if(numOver == 0) {
					//Has AQ equiv...
					return false;
				}
				continue;
	
			} else if(dataModel.isCardPlayedInRound(
					DataModel.getCardString(curRank, suitIndex))
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


	public static boolean hasQJEquivAndNoAORKEquiv(DataModel dataModel, int suitIndex) {
		
		if(dataModel.getNumberOfCardsOneSuit(suitIndex) < 2) {
			return false;
		}
		
		String secondHighestCard = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(suitIndex);
		
		int numOver = 0;
		
		for(int curRank = dataModel.ACE; curRank > DataModel.getRankIndex(secondHighestCard); curRank--) {
			if(dataModel.getCardsCurrentlyHeldByPlayers()[Constants.CURRENT_AGENT_INDEX][suitIndex][curRank] == DataModel.CERTAINTY) {
				if(numOver <= 1) {
					//Has A or K equiv...
					return false;
				}
				continue;
	
			} else if(dataModel.isCardPlayedInRound(
					DataModel.getCardString(curRank, suitIndex))
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
					DataModel.getCardString(curRank, suitIndex))
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
	 
	 
	 public static boolean hasJEquiv(DataModel dataModel, int suitIndex) {
			
			if(dataModel.getNumberOfCardsOneSuit(suitIndex) < 1) {
				return false;
			}
			
			String cardA = dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex);
			
			int numOver = 0;
			
			for(int curRank = dataModel.ACE; curRank > DataModel.getRankIndex(cardA); curRank--) {
				if(dataModel.getCardsCurrentlyHeldByPlayers()[Constants.CURRENT_AGENT_INDEX][suitIndex][curRank] == DataModel.CERTAINTY) {
					continue;
		
				} else if(dataModel.isCardPlayedInRound(
						DataModel.getCardString(curRank, suitIndex))
						) {
					continue;
		
				} else {
					numOver++;
					if(numOver > 3) {
						return false;
					}
				}
			}
		
			if(numOver == 3) {
				return true;
			} else {
				return false;
			}
		}
	
	 public static boolean has3PlusAndQJEquivOrBetter(DataModel dataModel, int suitIndex) {
		 
		 if(dataModel.getNumberOfCardsOneSuit(suitIndex) < 3) {
				return false;
		 }
		 
		 String secondHighest = dataModel.getCardCurrentPlayerGetSecondHighestInSuit(suitIndex);
		 int numOver = 0;
			
		for(int curRank = dataModel.ACE; curRank > DataModel.getRankIndex(secondHighest); curRank--) {
			
			if(dataModel.getCardsCurrentlyHeldByPlayers()[Constants.CURRENT_AGENT_INDEX][suitIndex][curRank] == DataModel.CERTAINTY) {
				continue;
	
			} else if(dataModel.isCardPlayedInRound(
					DataModel.getCardString(curRank, suitIndex))
					) {
				continue;
	
			} else {
				numOver++;
				if(numOver > 2) {
					return false;
				}
			}
		}
	
		if(numOver <= 2) {
			return true;
		} else {
			return false;
		}
		 
	 }
	 
	 
 public static String getCardThatWillEventuallyForceOutAllMasters(DataModel dataModel, int suitIndex) {
		 
		 if(dataModel.getNumberOfCardsOneSuit(suitIndex) < 1) {
				return null;
		 }
		 
	    int numOver = 0;
	    int numCardsInHand=0;
			
		for(int curRank = dataModel.ACE; curRank >= dataModel.RANK_TWO; curRank--) {
			
			if(dataModel.getCardsCurrentlyHeldByPlayers()[Constants.CURRENT_AGENT_INDEX][suitIndex][curRank] == DataModel.CERTAINTY) {
				numCardsInHand++;
				if(numCardsInHand > numOver) {
					return DataModel.getCardString(curRank, suitIndex);
				}
	
			} else if(dataModel.isCardPlayedInRound(
					DataModel.getCardString(curRank, suitIndex))
					) {
				continue;
	
			} else {
				numOver++;
				
			}
		}
		return null;
		 
	 }
	//Just start with functions.

	 
 
 //October 24th, 2021: this only affects 2-4089
 //Make it more complicated as new test cases get added.
 	public static boolean wantPartnerToLead(DataModel dataModel) {
 		
 		
 		if(dataModel.currentAgentHasSuit(Constants.SPADE)
 			&& (dataModel.getNumCardsOfSuitInCurrentPlayerHand(Constants.SPADE) <
 					3 * dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(Constants.SPADE)
 				||
 				dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE)) >= 4
 				)
 			) {

 			for(int suitIndex = 0; suitIndex<Constants.NUM_SUITS; suitIndex++) {
 				if(suitIndex == Constants.SPADE) {
 					continue;
 				}
 				
 				if( ! dataModel.currentAgentHasSuit(suitIndex)
 						&& (! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
 								(Constants.LEFT_PLAYER_INDEX, suitIndex)
 						 ||  dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
 						 		(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
 						)
 						&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex) >= 2
 					) {
 					return true;
 				} 
 			}//End for
 			
 		}//End if
 		

		for(int suitIndex = 0; suitIndex<Constants.NUM_SUITS; suitIndex++) {
			if(suitIndex == Constants.SPADE) {
				continue;
			}
			
			if(dataModel.getNumberOfCardsOneSuit(suitIndex) >= 2
					&& dataModel.currentPlayerHasMasterInSuit(suitIndex)
					&& (! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
							(Constants.LEFT_PLAYER_INDEX, suitIndex)
					 ||  dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
					 		(Constants.LEFT_PLAYER_INDEX, Constants.SPADE)
					 	)
					&& (! dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
							(Constants.RIGHT_PLAYER_INDEX, suitIndex)
					 ||  dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit
					 		(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE)
					 	)
					&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndex) > 4
					&& dataModel.getNumCardsInPlayNotInCurrentPlayersHandBetweenCardSameSuit
					(dataModel.getCardCurrentPlayerGetHighestInSuit(suitIndex),
							dataModel.getCardCurrentPlayerGetSecondHighestInSuit(suitIndex))
					== 1) {
				return true;
			}
		}
 		
 		//TODO: give a bonus if you know that partner knows you're void. (optional)
 		
 		//TODO: make it better...
 		
 		return false;
 	}
}

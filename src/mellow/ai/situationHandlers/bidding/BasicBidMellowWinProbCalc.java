package mellow.ai.situationHandlers.bidding;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.cardUtils.DebugFunctions;

public class BasicBidMellowWinProbCalc {

	
	//Formula from bottom to top is roughly: 100% * (1 - (2/3)^n)
	public static final double rankProbs[] = 
		{
				0.0,    // 2
				0.010,  // 3
				0.015,  // 4
				0.026,  // 5
				0.039,  // 6
				0.058,  // 7
				0.088,  // 8
				0.131,  // 9
				0.20,   // T
				0.30,   // J
				0.55,   // Q
				0.666,  // K
				0.99   // A
				};
	
	
	public static double getMellowSuccessProb1(DataModel dataModel) {
		
		double ret = 1.0;
		
		ret *= getProbNoBurnSpade(dataModel);
		
		for(int suitIndex=0; suitIndex<Constants.NUM_SUITS; suitIndex++) {
			if(suitIndex != Constants.SPADE) {
				ret *= getProbNoBurnOffsuit(dataModel, suitIndex);
			}
		}
		
		return ret;
	}
	
	public static double getProbNoBurnSpade(DataModel dataModel) {
		
		double ret = 1.0;
		
		int numSpades = dataModel.getNumCardsCurrentUserStartedWithInSuit(Constants.SPADE);
		
		for(int i=0; i<dataModel.getNumCardsCurrentUserStartedWithInSuit(Constants.SPADE); i++) {
			String card = dataModel.getCardCurrentPlayerGetIthLowestInSuit(i, Constants.SPADE);
			
			int numSpadesOver = numSpades - i - 1;
			
			//Acknowledge that saying mellow with 2 high spades is questionable:
			int increaseRank = 0;
			if(DataModel.getRankIndex(card) >= DataModel.RANK_TEN) {
				increaseRank = 2* numSpadesOver;
			} else if(DataModel.getRankIndex(card) >= DataModel.RANK_EIGHT) {
				increaseRank = numSpadesOver;
			}
			//End Acknowledgement
			
			int rankToUse = Math.min(DataModel.getRankIndex(card) + increaseRank, DataModel.ACE);
			
			double probLosingRank = rankProbs[rankToUse];

			if(DataModel.getRankIndex(card) == DataModel.ACE) {
				probLosingRank = 1.0;
			}
			
			ret *= (1 - probLosingRank);
			
		}
		
		if(numSpades == 4 
				&& DataModel.getRankIndex(dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE)) > DataModel.RANK_TWO) {
			ret /= 2.0;
		} else if(numSpades > 4) {
			ret /= 10.0;
		}
		
		return ret;
	}
	
	public static double getProbNoBurnOffsuit(DataModel dataModel, int suitIndex) {
		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "6S JH TH 7H 6H 5H KC QC JC 9C 8C 5D 3D")) {
			System.out.println("Debug");
		}
		double ret = 1.0;
		
		int numOffsuits = dataModel.getNumCardsCurrentUserStartedWithInSuit(suitIndex);
		
		if(numOffsuits > 0) {
			String card = dataModel.getCardCurrentPlayerGetLowestInSuit(suitIndex);
			
			double probLosingRank = rankProbs[DataModel.getRankIndex(card)];
			
			if(DataModel.getRankIndex(card) <= DataModel.RANK_FOUR) {
				probLosingRank = 0.0;
			}
			ret *= (1 - probLosingRank);
	
		}
		
		if(numOffsuits > 1) {
			String card = dataModel.getCardCurrentPlayergetSecondLowestInSuit(suitIndex);
			
			double probLosingRank = rankProbs[DataModel.getRankIndex(card)];
			
			if(DataModel.getRankIndex(card) <= DataModel.RANK_SIX) {
				probLosingRank = 0.0;
				
			} else if(DataModel.getRankIndex(card) <= DataModel.RANK_NINE) {
				probLosingRank = rankProbs[DataModel.getRankIndex(card) - 1];
			}
			
			ret *= (1 - 0.95 * probLosingRank);
	
		}
		

		if(numOffsuits > 2) {
			String card = dataModel.getCardCurrentPlayergetThirdLowestInSuit(suitIndex);
			
			double probLosingRank = rankProbs[DataModel.getRankIndex(card) + (numOffsuits - 3)];
			
			if(DataModel.getRankIndex(card) <= DataModel.RANK_SEVEN) {
				probLosingRank = 0.0;
				
			} else if(DataModel.getRankIndex(card) <= DataModel.RANK_TEN) {
				probLosingRank = rankProbs[DataModel.getRankIndex(card) - 1];
			}
			
			//Maybe .8 is too high?
			ret *= (1 - 0.8 * probLosingRank);
	
		}
		
		if(numOffsuits > 3) {
			String card = dataModel.getCardCurrentPlayergetFourthLowestInSuit(suitIndex);
			
			double probLosingRank = rankProbs[DataModel.getRankIndex(card)];
			
			if(DataModel.getRankIndex(card) < DataModel.RANK_SEVEN) {
				probLosingRank = 0.0;
				
			} else if(DataModel.getRankIndex(card) <= DataModel.JACK) {
				probLosingRank = rankProbs[DataModel.getRankIndex(card) - 1];
			}
			
			ret *= (1 - 0.1 * probLosingRank);
	
		}
		
		if(numOffsuits > 4) {
			String card = dataModel.getCardCurrentPlayerGetIthLowestInSuit(4, suitIndex);
			
			double probLosingRank = rankProbs[DataModel.getRankIndex(card)];
			
			ret *= (1 - 0.01 * probLosingRank);
	
		}
		
		return ret;
	}
	
	public static double getMellowSuccessProb2(DataModel dataModel) {
		
		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "AH QH JH TH 9H 3H 2H 9C 8C 7C 3C QD TD ")) {
			System.out.println("Debug");
		}
		
		double ret = 1.0;
		
		ret *= getProbNoBurnSpade(dataModel);
		
		//Consider your partner's weak hand:
		if(getProbNoBurnSpade(dataModel) < 0.7
				&& dataModel.playerMadeABidInRound(Constants.CURRENT_PARTNER_INDEX)
				&& dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) <= 2) {
			
			if(dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) == 2) {
				ret *= 90.0/100.0;
				
			} else if(dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) == 1) {

				ret *= 80.0/100.0;
			}
		}
		//End consider your partner's weak hand...
		
		int numForgiveSpade = 0;
		if(dataModel.getNumberOfCardsOneSuit(Constants.SPADE) < 3) {
			numForgiveSpade += 3 - dataModel.getNumberOfCardsOneSuit(Constants.SPADE);
			
			if(dataModel.getNumberOfCardsOneSuit(Constants.SPADE) == 0) {
				//Added a no-spade bonus:
				numForgiveSpade += 2;
			}
		}
		int numForgiveOffsuit = 0;
		for(int suitIndex=0; suitIndex<Constants.NUM_SUITS; suitIndex++) {
			if(suitIndex != Constants.SPADE
					&& dataModel.getNumberOfCardsOneSuit(suitIndex) < 3) {
				numForgiveOffsuit += 3 - dataModel.getNumberOfCardsOneSuit(suitIndex);
			}
		}
		
		//Let the previous bids bias you a little bit:
		//Maybe the bias could be stronger in anticipation of partner sweeping spade...
		if(Constants.CURRENT_AGENT_INDEX ==dataModel.getDealerIndexAtStartOfRound() ||
				Constants.LEFT_PLAYER_INDEX == dataModel.getDealerIndexAtStartOfRound()) {
			
			if(dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) > 3) {
				//System.out.println("(MORE SPADE FORGIVENESS)");
				numForgiveSpade += Math.max(dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) - 3, 0);
			}
		} else if(Constants.CURRENT_PARTNER_INDEX ==dataModel.getDealerIndexAtStartOfRound()) {
			if(dataModel.getBid(Constants.RIGHT_PLAYER_INDEX) == 1) {
				//System.out.println("(MORE SPADE FORGIVENESS 2)");
				numForgiveSpade += 1;
			}
		}
		//End let the previous bids bias you a little bit.
		
		for(int suitIndex=0; suitIndex<Constants.NUM_SUITS; suitIndex++) {
			if(suitIndex != Constants.SPADE) {
				
				if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "9S 6S KH 6H 4H JC 9C 7C 6C 4C JD 5D 3D ")
						&& suitIndex == 2) {
					System.out.println("Debug");
				}
				double tmpWinOffsuitProb = getProbNoBurnOffsuit(dataModel, suitIndex);
				
				if((tmpWinOffsuitProb < 0.95 && dataModel.getNumberOfCardsOneSuit(suitIndex) > 1)
						|| (tmpWinOffsuitProb < 0.98 && dataModel.getNumberOfCardsOneSuit(suitIndex) == 1)) {
					
					
					if(dataModel.getNumberOfCardsOneSuit(suitIndex) >= 3
							&& numForgiveSpade + numForgiveOffsuit > dataModel.getNumberOfCardsOneSuit(suitIndex) - 3) {
						
						int numTimes = numForgiveSpade + numForgiveOffsuit - (dataModel.getNumberOfCardsOneSuit(suitIndex) - 3);
						
						double adjustedWinProb = 1 - (1 - tmpWinOffsuitProb) * (100.0 - 7.0 * (numTimes))/100.0;
						ret *= adjustedWinProb;
					
					} else if(dataModel.getNumberOfCardsOneSuit(suitIndex) >= 2
							&& numForgiveSpade + numForgiveOffsuit > 0) {
						
						double adjustedWinProb = 1 - (1 - tmpWinOffsuitProb) * (100.0 - 3.0 * (numForgiveSpade + numForgiveOffsuit - 1))/100.0;
						ret *= adjustedWinProb;
					} else {

						ret *= tmpWinOffsuitProb;
					}
					
				} else {
					//System.out.println("Nevermind this suit!");
				}
			}
		}
		
		//Extremely rough because I'm lazy:
		/*if(numForgiveSpade + numForgiveOffsuit > 0) {
			System.out.println("REt before: " + ret);
			System.out.println("Num forgive: " + (numForgiveSpade + numForgiveOffsuit));
			ret = 1 - (1 - ret) * (1 - (5.0 * (numForgiveSpade + numForgiveOffsuit))/100.0);
			System.out.println("REt after: " + ret);
		}*/
		
		return ret;
	}
}

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
				0.99};  // A
	
	
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
			
			double probLosingRank = rankProbs[DataModel.getRankIndex(card)];

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
		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "TS 8H 7H 5H 3H 4C 2C AD QD TD 9D 6D 5D")) {
			System.out.println("Debug");
		}
		double ret = 1.0;
		
		int numOffsuits = dataModel.getNumCardsCurrentUserStartedWithInSuit(suitIndex);
		
		if(numOffsuits > 0) {
			String card = dataModel.getCardCurrentPlayerGetLowestInSuit(suitIndex);
			
			double probLosingRank = rankProbs[DataModel.getRankIndex(card)];
			
			ret *= (1 - probLosingRank);
	
		}
		
		if(numOffsuits > 1) {
			String card = dataModel.getCardCurrentPlayergetSecondLowestInSuit(suitIndex);
			
			double probLosingRank = rankProbs[DataModel.getRankIndex(card)];
			
			if(DataModel.getRankIndex(card) < DataModel.RANK_FIVE) {
				probLosingRank = 0.0;
				
			} else if(DataModel.getRankIndex(card) <= DataModel.RANK_NINE) {
				probLosingRank = rankProbs[DataModel.getRankIndex(card) - 1];
			}
			
			ret *= (1 - 0.95 * probLosingRank);
	
		}
		

		if(numOffsuits > 2) {
			String card = dataModel.getCardCurrentPlayergetThirdLowestInSuit(suitIndex);
			
			double probLosingRank = rankProbs[DataModel.getRankIndex(card) + (numOffsuits - 3)];
			
			if(DataModel.getRankIndex(card) < DataModel.RANK_SIX) {
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
		
		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "TS 8H 7H 5H 3H 4C 2C AD QD TD 9D 6D 5D")) {
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
		}
		int numForgiveOffsuit = 0;
		for(int suitIndex=0; suitIndex<Constants.NUM_SUITS; suitIndex++) {
			if(suitIndex != Constants.SPADE
					&& dataModel.getNumberOfCardsOneSuit(suitIndex) < 3) {
				numForgiveOffsuit += 3 - dataModel.getNumberOfCardsOneSuit(suitIndex);
			}
		}
		
		for(int suitIndex=0; suitIndex<Constants.NUM_SUITS; suitIndex++) {
			if(suitIndex != Constants.SPADE) {
				
				double tmpWinOffsuitProb = getProbNoBurnOffsuit(dataModel, suitIndex);
				
				if((tmpWinOffsuitProb < 0.95 && dataModel.getNumberOfCardsOneSuit(suitIndex) > 1)
						|| (tmpWinOffsuitProb < 0.98 && dataModel.getNumberOfCardsOneSuit(suitIndex) == 1)) {
					
					
					if(dataModel.getNumberOfCardsOneSuit(suitIndex) >= 3
							&& numForgiveSpade + numForgiveOffsuit > 0) {
						
						double adjustedWinProb = 1 - (1 - tmpWinOffsuitProb) * (100.0 - 5.0 * (numForgiveSpade + numForgiveOffsuit))/100.0;
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

package mellow.ai.situationHandlers.bidding;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;

public class BasicMellowWinProbCalc {

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
			
			ret *= (1 - 0.95 * probLosingRank);
	
		}
		

		if(numOffsuits > 2) {
			String card = dataModel.getCardCurrentPlayergetThirdLowestInSuit(suitIndex);
			
			double probLosingRank = rankProbs[DataModel.getRankIndex(card)];
			
			//Maybe .8 is too high?
			ret *= (1 - 0.8 * probLosingRank);
	
		}
		
		if(numOffsuits > 3) {
			String card = dataModel.getCardCurrentPlayergetFourthLowestInSuit(suitIndex);
			
			double probLosingRank = rankProbs[DataModel.getRankIndex(card)];
			
			ret *= (1 - 0.3 * probLosingRank);
	
		}
		
		if(numOffsuits > 4) {
			String card = dataModel.getCardCurrentPlayerGetIthLowestInSuit(4, suitIndex);
			
			double probLosingRank = rankProbs[DataModel.getRankIndex(card)];
			
			ret *= (1 - 0.01 * probLosingRank);
	
		}
		
		return ret;
	}
	
	
}

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
		
		// Saying saying with 4 spades or more is bad mmkay:
		if(numSpades == 4) {
			if(DataModel.getRankIndex(dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE)) == DataModel.RANK_TWO) {

				ret /= 1.3;
			} else {
				ret /= 2.0;
			}
		} else if(numSpades > 4) {
			ret /= 10.0;
		}
		
		return ret;
	}
	
	public static double getProbNoBurnOffsuit(DataModel dataModel, int suitIndex) {
		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "8S 4S AH KH QH JH TH 5H 7C JD 9D 8D 2D ")) {
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
			
			double probLosingRank = -1.0;
			if(DataModel.getRankIndex(card) + (numOffsuits - 2) >= DataModel.JACK) {
				probLosingRank = rankProbs[DataModel.getRankIndex(card) + (numOffsuits - 2)];

				if(numOffsuits >= 3) {
					//Tamed it down, so that AI is more willing to say mellow
					probLosingRank = 0.5 * probLosingRank + 0.5 * rankProbs[DataModel.getRankIndex(card)];
				}
			} else {
				probLosingRank = rankProbs[DataModel.getRankIndex(card)];
			}
			
			
			if(DataModel.getRankIndex(card) <= DataModel.RANK_SIX) {
				probLosingRank = 0.0;
				
			} else if(DataModel.getRankIndex(card) <= DataModel.RANK_NINE && numOffsuits < 4) {
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
			if(numOffsuits == 3) {
				ret *= (1 - 0.8 * probLosingRank);
			} else if(numOffsuits == 4) {
				ret *= (1 - 0.7 * probLosingRank);
			} else if(numOffsuits == 5) {
				ret *= (1 - 0.6 * probLosingRank);
			} else if (numOffsuits > 5) {
				ret *= (1 - 0.4 * probLosingRank);
			}
	
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
		

		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "7S 3S 2S AH 9H 5H QC 9C KD TD 9D 8D 2D ")) {
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
		
		//Let the previous bids bias you a little bit:
		//Maybe the bias could be stronger in anticipation of partner sweeping spade...
		if(Constants.CURRENT_AGENT_INDEX ==dataModel.getDealerIndexAtStartOfRound() ||
				Constants.LEFT_PLAYER_INDEX == dataModel.getDealerIndexAtStartOfRound()) {
			
			if(dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) > 3) {
				//System.out.println("(MORE SPADE FORGIVENESS)");
				
				int partnerCrazyHighBonus = dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) - 8;
				
				if(partnerCrazyHighBonus >= 0) {
					numForgiveSpade += 3 + partnerCrazyHighBonus;
					
				} else if(dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) >=7) {
					numForgiveSpade += 2;
					
				} else if(dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) >= 5) {
					numForgiveSpade += 1;
				}
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
						
						double factorToUse = 20.0;
						int rankLowest = DataModel.getRankIndex(dataModel.getCardCurrentPlayerGetLowestInSuit(suitIndex));
						
						//Don't dismiss suits where lowest is 9 or above:
							//Rank nine might be a bit too simplified...
						if(rankLowest < DataModel.RANK_NINE) {
							factorToUse = 20.0;
						} else {
							factorToUse = 10.0;
						}
						
						double adjustedWinProb = 1 - (1 - tmpWinOffsuitProb) * (100.0 - factorToUse * (numTimes))/100.0;
						ret *= adjustedWinProb;
					
					} else if(dataModel.getNumberOfCardsOneSuit(suitIndex) >= 2
							&& numForgiveSpade + numForgiveOffsuit > 0) {
						
						//There's only a small decrease in burn % from throwing off the danger card
						// if you only have 2 of the suit:
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

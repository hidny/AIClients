package mellow.ai.situationHandlers;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.situationHandlers.bidding.BasicBidMellowWinProbCalc;
import mellow.ai.situationHandlers.bidding.BiddingNearEndOfGameFunctions;
import mellow.cardUtils.DebugFunctions;

public class BiddingSituation {
	
	public static String getSimpleBidToMake(DataModel dataModel) {
		//Converted python function from github to java here:
		
		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "AS KS TS 7S 6S 2S 9H 8H 2H JC 7C 7D 3D ")) {
			System.out.println("Debug");
		}
		
		double bid = 0.0;
		
		//#Add number of aces:
		//bid = bid + getNumberOfAces(hand)
		bid += dataModel.getNumberOfAces();
		
		for(int suitIndex = 0; suitIndex<Constants.NUM_SUITS; suitIndex++) {
			if(suitIndex != Constants.SPADE
					&& dataModel.currentPlayerHasMasterInSuit(suitIndex)
					&& dataModel.getNumberOfCardsOneSuit(suitIndex) >= 8) {
				//Don't count bid if you have 8 of them...
				bid -= 0.5;
			}
		}
		
		System.out.println("Number of aces: " + dataModel.getNumberOfAces());
		
		//if trumping means losing a king or queen of spades, then it doesn't mean much
		boolean trumpingIsSacrifice = false;
		
		//Add king of spades if 1 or 2 other spade
		if (dataModel.hasCard("KS") && dataModel.getNumberOfCardsOneSuit(0) >= 2) {
		
			bid += 1.0;
			if (dataModel.getNumberOfCardsOneSuit(0) == 2 && dataModel.hasCard("AS") == false) {
				bid = bid - 0.2;
				
				trumpingIsSacrifice = true;
			}
			System.out.println("I have the KS");
		}
			
		//Add queen of spacdes if 2 other spaces
		if (dataModel.hasCard("QS") && dataModel.getNumberOfCardsOneSuit(0) >= 3) {
			bid += 1.0;
			trumpingIsSacrifice = true;
			System.out.println("I have the QS");
		}
			
	
		if(dataModel.getNumberOfCardsOneSuit(0) >= 4 ) {
			trumpingIsSacrifice = true;
		}
		
		double trumpResevoir= 0.0;
		if (dataModel.getNumberOfCardsOneSuit(0) == 3) {
			trumpResevoir = 0.44;
		}
		
		//Add a bid for every extra spade over 3 you have:
		if (dataModel.getNumberOfCardsOneSuit(0) >= 4) {

			//Lowered to 3.75 from 4.0 and got 13 less failed cases. 
			bid += dataModel.getNumberOfCardsOneSuit(0) - 3.75;
			
			double bidAdd = 0.0;
		
			if (dataModel.hasCard("JS")) {
				bidAdd =  1.0;
			} else if (dataModel.hasCard("TS")) {
				bidAdd = 0.8;
				trumpResevoir = 0.201;
			} else if(dataModel.getNumberOfCardsOneSuit(1) < 2 ||  dataModel.getNumberOfCardsOneSuit(2) < 2 ||  dataModel.getNumberOfCardsOneSuit(3) < 2) {
				bidAdd = 0.7;
				trumpResevoir = 0.301;
			} else {
				trumpResevoir = 1.001;
			}
			
			if(dataModel.getNumberOfCardsOneSuit(0) == 4) {
				bid += bidAdd;
			} else if(dataModel.getNumberOfCardsOneSuit(0) >= 5) {
				
				//TODO: 0.5 * bidAdd should be part of highSpadeBidBonus
				bid += 0.5 * bidAdd;
				bid += highSpadeBidBonus(dataModel);
			}
			
		}
			//TODO: 5+ spades should give a special bonus depending on the offsuits.
		    // The "take everything" bonus :P
		
		
		int numOffSuitKings = 0;
		if(dataModel.hasCard("KS")) {
			numOffSuitKings = dataModel.getNumberOfKings() - 1;
		} else {
			numOffSuitKings = dataModel.getNumberOfKings();
		}
		
		bid += 0.75 * numOffSuitKings;
		
		//offsuit king adjust if too many or too little of a single suit:
		for(int i=0; i<Constants.OFFSUITS.length; i++) {
			String off = Constants.OFFSUITS[i];
			if(dataModel.hasCard("K" + off) && dataModel.getNumberOfCardsOneSuit(i + 1) == 1) {
				bid = bid - 0.55;
			} else if(dataModel.hasCard("K" + off) && dataModel.getNumberOfCardsOneSuit(i + 1) > 5) {
				bid = bid - 0.65;
			} else if(dataModel.hasCard("K" + off) && dataModel.getNumberOfCardsOneSuit(i + 1) > 4) {
				bid = bid - 0.27;
			} else if(dataModel.hasCard("K" + off) && dataModel.getNumberOfCardsOneSuit(i + 1) > 3) {
				bid = bid - 0.20;
			}
			
			if(dataModel.hasCard("K" + off) && (dataModel.hasCard("Q" + off)) ) {
				bid = bid + 0.27;
			} else if(dataModel.hasCard("K" + off) && dataModel.hasCard("A" + off)) {
				bid = bid + 0.26;
			}
		}
		
		boolean AStrumpingBonus = false;
		if(dataModel.hasCard("AS") ) {
			
			if(dataModel.getNumberOfCardsOneSuit(0) == 1) {
				//No bonus
			} else if(dataModel.hasCard("KS") && dataModel.getNumberOfCardsOneSuit(0) == 2) {
				//No bonus
			} else if(dataModel.hasCard("QS") && dataModel.getNumberOfCardsOneSuit(0) == 3) {
				//No bonus
			} else {

				if(dataModel.getNumberOfCardsOneSuit(0) <= 4) {
					AStrumpingBonus = true;
					trumpResevoir += 0.2;
				}
			}
		}
		
		//END offsuit king adjustment logic
			
		if(dataModel.getNumberOfCardsOneSuit(1) < 3 || dataModel.getNumberOfCardsOneSuit(2) < 3|| dataModel.getNumberOfCardsOneSuit(3) < 3) {
			if(dataModel.getNumberOfCardsOneSuit(0) >= 2 && dataModel.getNumberOfCardsOneSuit(0) < 4 && trumpingIsSacrifice == false ) {

				bid += 0.3;
				
				
				if( (dataModel.getNumberOfCardsOneSuit(1) < 3 && dataModel.getNumberOfCardsOneSuit(2) < 3) ||
						(dataModel.getNumberOfCardsOneSuit(1) < 3 && dataModel.getNumberOfCardsOneSuit(3) < 3) ||
						(dataModel.getNumberOfCardsOneSuit(2) < 3 && dataModel.getNumberOfCardsOneSuit(3) < 3) ) {
					bid += 0.75; 
				} else if(dataModel.getNumberOfCardsOneSuit(1) < 2 || dataModel.getNumberOfCardsOneSuit(2) < 2 || dataModel.getNumberOfCardsOneSuit(3) < 2) {
					bid += 0.75;
				} else if(AStrumpingBonus) {
					bid += 0.60;
				}
			} else if(dataModel.getNumberOfCardsOneSuit(0) >= 4 && trumpResevoir > 0 && dataModel.getNumberOfCardsOneSuit(0) <=5 ) {
				if( (dataModel.getNumberOfCardsOneSuit(1) < 3 && dataModel.getNumberOfCardsOneSuit(2) < 3) ||
						(dataModel.getNumberOfCardsOneSuit(1) < 3 && dataModel.getNumberOfCardsOneSuit(3) < 3) ||
						(dataModel.getNumberOfCardsOneSuit(2) < 3 && dataModel.getNumberOfCardsOneSuit(3) < 3) ) {
					bid += trumpResevoir; 
				} else if(dataModel.getNumberOfCardsOneSuit(1) < 2 || dataModel.getNumberOfCardsOneSuit(2) < 2 || dataModel.getNumberOfCardsOneSuit(3) < 2) {
					bid += trumpResevoir;
				}
			}
		}
		
		//TODO: didn't handle mellow
		//Didn't handle akqjt (a sweep)
		
		if(dataModel.getNumberOfCardsOneSuit(0) == 0) {
			bid = bid  - 1;
		}
		
		//END OF MEASURE OF STRENGTH OF HAND
		
		//Start coasting:
		if(dataModel.getDealerIndexAtStartOfRound() == Constants.CURRENT_AGENT_INDEX
				|| dataModel.getDealerIndexAtStartOfRound() == Constants.LEFT_PLAYER_INDEX) {
			if(dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) == 1) {
				//bid -= 0.25;
				//Didn't make it better :(
				
			} else if(dataModel.getOurScore() > 700
					&& dataModel.getOurScore() - dataModel.getOpponentScore() > 100
					&& dataModel.getOurScore() - dataModel.getOpponentScore() < 300) {
				//bid -= 0.25;
				//Didn't make it better :(
				//Maybe if you don't have strong spade??? I don't know
			}
		}
		
		System.out.println("Bid double: " + bid);
		int intBid = (int) Math.floor(bid);

		/*
//Caused issues:
 * Try: discounting kings if partner said mellow.
		//Bid less because you want to attack the person who bid 0:
		if(dataModel.getDealerIndexAtStartOfRound() == Constants.LEFT_PLAYER_INDEX
				&& dataModel.getBid(Constants.RIGHT_PLAYER_INDEX) == 0
				&& dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) != 0
				&& intBid > 2) {
			intBid = intBid - 1;
			
		} else if(dataModel.getDealerIndexAtStartOfRound() == Constants.CURRENT_AGENT_INDEX
				&& (dataModel.getBid(Constants.RIGHT_PLAYER_INDEX) == 0 || dataModel.getBid(Constants.LEFT_PLAYER_INDEX) == 0)
				&& dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) != 0
				&& intBid > 2) {
					
				while(dataModel.getBidTotalSoFar() + intBid >= Constants.NUM_STARTING_CARDS_IN_HAND - 1
						&& intBid > 1) {
					intBid--;
				}
		}
		*/
		
		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "8S 6S KH QH TH 9H 3H QC TC 4C 8D 4D 3D ")) {
			System.out.println("Debug");
		}
		
		if (intBid < 0) {
			intBid = 0;
		}
		
		//Don't double mellow:
		if(intBid == 0
				&& (dataModel.playerMadeABidInRound(Constants.CURRENT_PARTNER_INDEX)
				    && dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) == 0)) {
			return "1";
		}
		
		//Don't bid mellow with KS
		if(intBid == 0 && dataModel.hasCard("KS")) {
			intBid = 1;
		
		// Just don't say mellow if 2 cards are over the 9S...
		} else if(intBid == 0 && dataModel.getNumCardsInCurrentPlayersHandOverCardSameSuit("9S") >= 2) {
			intBid = 1;
		} else if(intBid == 0 && ! partnerDidntSayMellow(dataModel)) {
			intBid = 1;
		}
		
		

		if(dataModel.isDealer() 
				&& dataModel.getBidTotalSoFar() <= 8
				&& intBid > 0
				&& ! dataModel.someoneBidMellowSoFar()) {
			//At least put bid to 9... (This could be gamed.)
			if(9 - dataModel.getBidTotalSoFar() > intBid) {
				System.out.println("(Increase bid because bids are so low. I hope Monte doesn't game this)");
				intBid = intBid + 1;
			}
		}
		
		if(dataModel.getDealerIndexAtStartOfRound() == Constants.LEFT_PLAYER_INDEX
				&& dataModel.getBidTotalSoFar() + intBid >= 12) {
			
			//This only fixes testcase 2-4651, but whatever
			System.out.println("Decrease bid because bids are so high");
			while(intBid > 1 && dataModel.getBidTotalSoFar() + intBid >= 12) {
				intBid = intBid - 1;
			}
			
		}
		
		System.out.println("Final bid " + intBid);

		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "5S 3S 4H 2H JC 4C 3C 2C AD JD 6D 5D 4D ")) {
			System.out.println("Debug");
		}

		
		if(dataModel.getOurScore() > 800) {
			

			int scoresProjectedWorstCase[] = BiddingNearEndOfGameFunctions.getProjectedScoresAssumingTheWorst(dataModel, intBid);
			int ourScoreWorstCaseNoBurn = scoresProjectedWorstCase[0];
			int theirScoreWorstCaseNoBurn = scoresProjectedWorstCase[1];
			
			
			if(intBid > 0) {
				int temp2 = BiddingNearEndOfGameFunctions.getPossiblyLowerBidBecauseItsNearEndOfGameAssumeWorst(dataModel, intBid, scoresProjectedWorstCase);
			
				System.out.println("TEST scores " + dataModel.getOurScore() + " vs " + dataModel.getOpponentScore());
			
				if(temp2 > 0) {
					intBid = temp2;
				}
				
				//return temp2 + "";
			} else {
				int temp2 = BiddingNearEndOfGameFunctions.getPossiblyLowerBidBecauseItsNearEndOfGameAssumeWorst(dataModel, 2, scoresProjectedWorstCase);
				
				if(temp2 == 1 
						&& dataModel.getOpponentScore() < 800
						&& (        BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel) < 0.80
								|| 
							       (BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel) < 0.60
									&& dataModel.playerMadeABidInRound(Constants.CURRENT_PARTNER_INDEX)
								    && dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) >= 4)
							)
						
					) {
					System.out.println("JAN 9th: " + BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel));
					return temp2 + "";
				}
				
			}
			
			//TODO:
			//int scoresProjectedWorstCaseButOpponentsDontSayMellow[] = getProjectedScoresAssumingTheWorst(dataModel, intBid);
			//boolean opponentsDidntsaymellowyet TODO
			
			
			//Stop thinking if we are projected to win if we win our tricks:
			// (But only if that bid isn't mellow)
			if(intBid > 0
					&& ourScoreWorstCaseNoBurn >= Constants.GOAL_SCORE
					&& ourScoreWorstCaseNoBurn > theirScoreWorstCaseNoBurn) {
				
				return intBid + "";

			}
			
		}
		
		if(dataModel.getOurScore() < 800
				&&  dataModel.getDealerIndexAtStartOfRound() == Constants.CURRENT_AGENT_INDEX) {
			while(dataModel.getBidTotalSoFar() + intBid >= Constants.NUM_STARTING_CARDS_IN_HAND
					&& intBid > 1) {
				System.out.println("(LOWER BID TO BE SAFE)");
				intBid--;
			}
		}
		
		
		//NEAR end of game for opponent logic.
		//TODO: put into function (make it 5 lines instead of 15)
		if(dataModel.getOpponentScore() > 800
				&& dataModel.getDealerIndexAtStartOfRound() == Constants.CURRENT_AGENT_INDEX) {
			

			//Condition for bidding higher if you're about to lose:
			//See if you want to bid high...
			int scoresProjectedWorstCase[] = BiddingNearEndOfGameFunctions.getProjectedScoresAssumingTheWorst(dataModel, intBid);
			
			int bonusPoints = BiddingNearEndOfGameFunctions.getNumberOfPointsAvailableWithBidAsDealer(dataModel, intBid);
			
			int oppScore = scoresProjectedWorstCase[1] - bonusPoints;
			int ourScore = scoresProjectedWorstCase[0] + bonusPoints;
			
			
			if(oppScore > ourScore
					&& oppScore >= Constants.GOAL_SCORE) {
				
				System.out.println("Scores are putting us in a desperate situation: " + dataModel.getOurScore() + " vs " + dataModel.getOpponentScore());
				
				double oddsMellow = BiddingNearEndOfGameFunctions.getOddsOfWinningWithFinalDealerBidMellow(dataModel);
				
				System.out.println("odds mellow: " + oddsMellow);
				
				int getHighBidNeeded = Integer.parseInt(BiddingNearEndOfGameFunctions.getHigherBidRequiredToWinInLastRound(dataModel));

				double oddsHighBid = BiddingNearEndOfGameFunctions.getOddsOfWinningWithFinalDealerBidHigherToCompete(dataModel, intBid, getHighBidNeeded);

				System.out.println("odds HighBid: " + oddsHighBid);
				
				//TODO: if opponent said mellow, bid 1 and hope!
				if(BiddingNearEndOfGameFunctions.opponentsDidntSayMellow(dataModel) == false
						&& oddsMellow < 0.4
						&& oddsHighBid < 0.4) {
					return "1";
				}
				if(oddsMellow > oddsHighBid) {
					return "0";
					
				} else if(getHighBidNeeded > 0){
					return ""+ getHighBidNeeded;
					
				} else {
					System.err.println("ERROR: oddsHighBid broke!");
					return "0";
				}
			} else if(oppScore + 2 * bonusPoints > ourScore
					&& oppScore >= Constants.GOAL_SCORE) {
				System.err.println("(Weird near end of game edge case!)");
				System.err.println("TODO: test!");
				
				return BiddingNearEndOfGameFunctions.getHigherBidRequiredToWinInLastRound(dataModel);
			}
				
			
		} else if(dataModel.getOpponentScore() > 800
				&& dataModel.getDealerIndexAtStartOfRound() == Constants.LEFT_PLAYER_INDEX) {
			
			if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "6S 5S 4S 3S AH QH 6H 3H QC 9C 5C AD 8D ")) {
				System.out.println("Debug");
			}
			if(intBid > 0) {
				int tempBid = intBid;
				
				boolean treatningToWinIfBidsSum13 = false;
				boolean currentlyLosing = false;
				do {
					int scoresProjectedWorstCaseNoMellow[] = BiddingNearEndOfGameFunctions.getProjectedScoresAssumingTheWorstAndLHSDoesntGoMellow(dataModel, tempBid);
					
					int oppScore = scoresProjectedWorstCaseNoMellow[1];
					int ourScore = scoresProjectedWorstCaseNoMellow[0];
					
					if(oppScore > ourScore
							&& oppScore >= Constants.GOAL_SCORE) {
						tempBid++;
						//We're losing if we keep this bid...
						currentlyLosing = true;
						
					} else if(ourScore > oppScore
							&& ourScore >= Constants.GOAL_SCORE) {
						
						treatningToWinIfBidsSum13 = true;
						break;
					} else {
						break;
					}
					//
				} while(tempBid <= Constants.NUM_RANKS - 
						  (dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) + dataModel.getBid(Constants.RIGHT_PLAYER_INDEX) + 1)
						);
				
				//BiddingNearEndOfGameFunctions.opponentsDidntSayMellow(dataModel)
				
				if(treatningToWinIfBidsSum13 || currentlyLosing) {
					// Consider saying mellow:
					
					int scoresProjectedWorstCaseNoMellow[] = BiddingNearEndOfGameFunctions.getProjectedScoresAssumingTheWorstAndLHSDoesntGoMellow(dataModel, 0);
					
					int oppScore = scoresProjectedWorstCaseNoMellow[1];
					int ourScore = scoresProjectedWorstCaseNoMellow[0];

					int oppScoreHope2Less = oppScore - 20;
					int ourScoreHope2Extra = ourScore + 2;
					
					
					if(ourScoreHope2Extra > oppScoreHope2Less
							&& ourScoreHope2Extra >= Constants.GOAL_SCORE) {
						int stretchAmount = tempBid - intBid;
						
						if(stretchAmount >= 0) {
							

							if(dataModel.getBid(Constants.RIGHT_PLAYER_INDEX) == 0
									&& BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel) > 0.3) {
								//TODO: maybe copying RHS isn't always a good idea?
								return "0";
								
							}
							
							
							if(stretchAmount == 0 && BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel) > 0.7) {
								return "0";
							} else if(stretchAmount == 1 && BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel) > 0.5) {
								return "0";
								
							} else if(stretchAmount == 2 && BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel) > 0.3) {
								return "0";
							} else if(stretchAmount == 3 && BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel) > 0.1) {
								return "0";
							} else if(stretchAmount >= 4 && BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel) > 0.01) {
								return "0";
							}
						}
					}
					
					return tempBid + "";
					
				}
			}
		} else if(dataModel.getOpponentScore() > 800
				&& (dataModel.getDealerIndexAtStartOfRound() == Constants.RIGHT_PLAYER_INDEX
						||
					dataModel.getDealerIndexAtStartOfRound() == Constants.CURRENT_PARTNER_INDEX)
			) {
			
			if(dataModel.getOpponentScore() > 940
					&& dataModel.getOurScore() < 830) {
				
				if(intBid == 0) {
					intBid = 1;
				}
				
				return intBid +"";
			}
		}
		//END NEAR end of game for opponent logic.
		//TODO: make high bid as 3rd bidder
		
		
		
		if(BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel) > 0.3) {
			//System.out.println("Mellow prob: " + BasicBidMellowWinProbCalc.getMellowSuccessProb1(dataModel));
			//System.out.println("Mellow prob2: " + BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel));
			//System.out.println("int bid: " + intBid);

		}
		

		if(BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel) > 0.3
				&& dataModel.getOpponentScore() - dataModel.getOurScore() > 100 
				&& dataModel.getOpponentScore() < 940 //don't let opponents win by saying 6.
				&& ( dataModel.getOpponentScore() > 850
						|| 1.5 * (1000 - dataModel.getOpponentScore()) < (1000 - dataModel.getOurScore())
					)
				&& (partnerDidntSayMellow(dataModel)
						//Make an exception:
					    || BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel) > 0.9)
				) {
			
			if(BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel) < 0.55) {
				System.out.println("(WHATEVER MELLOW) (" + dataModel.getOurScore() + " vs " + dataModel.getOpponentScore() + ")");
				System.out.println("(PROB: " + BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel) + ")");
			}
			return 0 + "";
			
		} else	if(
				//Expected value seems good:
				(	(BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel) > 0.5 + intBid * 0.05)
					||
					//This condition never happens :(... I'll leave it in just in case though.
					(intBid == 0 && isMellowWarrentedIfYouDontHave1(dataModel, bid))
				)
				//No double mellow:
				&& ( ! dataModel.playerMadeABidInRound(Constants.CURRENT_PARTNER_INDEX)
					    || dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) != 0
					    //Make an exception:
					    || BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel) > 0.9)) {
			
			//TODO: put into function:
			if(dataModel.getOurScore() > 900) {
				if(intBid >= 1) {
					int scoresProjectedWorstCase[] = BiddingNearEndOfGameFunctions.getProjectedScoresAssumingTheWorst(dataModel, intBid);
				
					if(scoresProjectedWorstCase[0] >= Constants.GOAL_SCORE
						&& scoresProjectedWorstCase[1] < scoresProjectedWorstCase[0]
						&& (BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel) < 0.8
							 || ! dataModel.playerMadeABidInRound(Constants.CURRENT_PARTNER_INDEX))) {
						
						
						return intBid + "";
					
					//TODO: if it's not close
					} else if(BiddingNearEndOfGameFunctions.dontSayMellowBecauseYoureWinning(scoresProjectedWorstCase, BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel))) {
						return intBid + "";
					}

				} else {
					
					//TODO: maybe don't assume worst later?
					int scoresProjectedWorstCase[] = BiddingNearEndOfGameFunctions.getProjectedScoresAssumingTheWorst(dataModel, 1);
					
					if(scoresProjectedWorstCase[0] >= Constants.GOAL_SCORE
						&& scoresProjectedWorstCase[1] < scoresProjectedWorstCase[0]
						&& (BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel) < 0.6
						    || ! dataModel.playerMadeABidInRound(Constants.CURRENT_PARTNER_INDEX))) {
						
						
						return 1 + "";
					} else if(! dataModel.playerMadeABidInRound(Constants.CURRENT_PARTNER_INDEX) ){
						
						int scoresProjectedReasonableCase[] = BiddingNearEndOfGameFunctions.getProjectedScoresAssumingTheWorst(dataModel, 3);
						
						if(scoresProjectedReasonableCase[0] >= Constants.GOAL_SCORE
								&& scoresProjectedReasonableCase[1] < Constants.GOAL_SCORE - 150
								&& BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel) < 0.6) {
								
								
								return 1 + "";
						}
					}
				}
			}
			//END TODO
			
			if(partnerDidntSayMellow(dataModel)) {
				return 0 + "";
			} else {
				return 1 + "";
			}
			
		} else if(BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel) > 0.30
				&& intBid == 0
				&& (dataModel.getOpponentScore() > 750
						&& dataModel.getOpponentScore() < 900
						&& dataModel.getOpponentScore() - dataModel.getOurScore() > 100)
				) {
			//Play risky catchup:
			return 0 + "";
			
		} else if(BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel) < 0.40
				&& intBid == 0) {
			return 1 + "";

		} else if(
				partnerDidntSayMellow(dataModel)
				&& dataModel.playerMadeABidInRound(Constants.RIGHT_PLAYER_INDEX)
				&& dataModel.getBid(Constants.RIGHT_PLAYER_INDEX) == 0
				&& BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel) > 0.10
				&& intBid > 0) {
			//Lower requirements for double mellow:
			System.out.println("Double mellow with prob: " + BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel));
			
			if(playerHasTwoPermBadSuits(dataModel)) {
				return intBid +"";
			} else if(dataModel.getNumberOfCardsOneSuit(Constants.SPADE) >= 4
					&& !dataModel.hasCard("2S")) {
				//Don't tempt fate:
				return intBid +"";
			} else {
				return 0 + "";
			}
			
		} else if(partnerDidntSayMellow(dataModel)
				&& dataModel.playerMadeABidInRound(Constants.LEFT_PLAYER_INDEX)
				&& dataModel.getBid(Constants.LEFT_PLAYER_INDEX) == 0
				&& BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel) > 0.08
				&& intBid > 0) {
			
			System.out.println("Special Position double mellow with prob: " + BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel));
			//Lower requirements for double mellow with special position:
			//TODO: maybe have an extra spade consideration...
			
			if(playerHasTwoPermBadSuits( dataModel)) {
				return intBid +"";
			} else {
				return 0 + "";
			}
			
		} else {
			
			return intBid + "";
		}
	}
	
	public static boolean partnerDidntSayMellow(DataModel dataModel) {
		if(dataModel.playerMadeABidInRound(Constants.CURRENT_PARTNER_INDEX) == false) {
			return true;
		} else if(dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) != 0){
			return true;
		} else {
			return false;
		}
		
	}
	
	public static boolean playerHasTwoPermBadSuits(DataModel dataModel) {
		
		int numBad = 0;
		for(int suit=0; suit<Constants.NUM_SUITS; suit++) {
			
			if(dataModel.getNumberOfCardsOneSuit(suit) >= 3
				&&	dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(
					dataModel.getCardCurrentPlayergetThirdLowestInSuit(suit))
				<= 1
				&& dataModel.getNumberOfCardsOneSuit(suit) <= 7
				) {
				numBad++;
				
			} else if(dataModel.getNumberOfCardsOneSuit(suit) >= 2
					&&	dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(
					dataModel.getCardCurrentPlayergetSecondLowestInSuit(suit))
					== 0
				&& dataModel.getNumberOfCardsOneSuit(suit) <= 10
				) {
				numBad++;
				
			} else if(dataModel.getNumberOfCardsOneSuit(suit) >= 1
					&&	dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(
					dataModel.getCardCurrentPlayerGetLowestInSuit(suit))
					== 0
				) {
				numBad++;
				
			}
			
		}
		
		if(numBad == 2) {
			System.out.println("Rejected!");
			return true;
		}
		
		return false;
	}
	
	//TODO: maybe I should care if the bid is a fraction like 0.7
	//This is a rough estimate that will hopefully be refined later.
	//EX: I made up a few of the numbers.
	
	//This code doesn't fix anything!
	//I'll leave it, but it doesn't do anything :(
	public static boolean isMellowWarrentedIfYouDontHave1(DataModel dataModel, double bidDouble) {
		double probMellowPass = BasicBidMellowWinProbCalc.getMellowSuccessProb2(dataModel);
		
		//TODO: maybe if you're the 3rd bidder, count on the 4th bidder saying mellow?

		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "3S 8H 7H 6H 2H JC TC 8C 3C QD JD TD 5D ")) {
			System.out.println("Debug1");
		}
		//System.out.println("Prob mellow pass: " + probMellowPass);
		int partnerBid = 3;
		double oddsOfWinningOne = 0.85;
		
		double oddsOfPartnerMakingTricksAlone = 0.90;
		
		if(dataModel.isDealer() || dataModel.getDealerIndexAtStartOfRound() == Constants.LEFT_PLAYER_INDEX) {
			partnerBid = dataModel.getBid(Constants.CURRENT_PARTNER_INDEX);
			
			//Partner can't bid lower for you because partner already bid,
			// so lower the chances of making the tricks:
			oddsOfWinningOne = 0.66;
			oddsOfPartnerMakingTricksAlone = 0.85;
		}
		
		if(bidDouble > 0.5) {
			oddsOfWinningOne = 0.80;
		} else if(bidDouble > 0.2) {
			oddsOfWinningOne += 0.05;
		}
		
		double estimatedExpectedValueMellow = 100 *  probMellowPass - 100 * (1 - probMellowPass) + oddsOfPartnerMakingTricksAlone * (10 * partnerBid) - (1 - oddsOfPartnerMakingTricksAlone) * (10 * partnerBid);
		//ex: 100 * x - 100 * (1 - x) + 0.85 * 10 * 2 - 0.15 * 10 * 2
		//80% of winning when you bid 
		double estimatedExpectedValueOne = 10 * (oddsOfWinningOne * (partnerBid + 1) - (1 - oddsOfWinningOne) * (partnerBid + 1));
		
		if(estimatedExpectedValueMellow > estimatedExpectedValueOne) {
			return true;
		} else {
			return false;
		}
	}
	
	//TODO: put below functions in their own class:
	
	public static double highSpadeBidBonus(DataModel dataModel) {
		
		
		String highest = dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
		
		//Assume player might say 2...
		if(opponentSaidMellowOrMightSayMellow(dataModel, 2)) {
			return 0.0;

		} else if(dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(
				dataModel.getCardCurrentPlayerGetIthHighestInSuit(3, Constants.SPADE)) <= 1) {
			//Full bridge
			
			return 0.5;
			
		} else if(opponentProbHighSpader(dataModel)) {
			return 0.0;
			
		} else if(highest.equals("JS")
				|| highest.equals("TS")) {
			return 0.5;
		

		}
		
		//TODO: bridge
		
		return 0.0;
	}
	
	public static boolean opponentProbHighSpader(DataModel dataModel) {
		
		int dealerIndex = dataModel.getDealerIndexAtStartOfRound();
		
		for(int i=dealerIndex + 1; i<Constants.NUM_PLAYERS; i++) {
			if(i == Constants.CURRENT_PARTNER_INDEX) {
				continue;
			}
			if(dataModel.getBid(i) >= 5) {
				return true;
			}
		}
		
		if(dealerIndex == Constants.LEFT_PLAYER_INDEX
				&& dataModel.getBidTotalSoFar() <= 2) {
			return true;
		}
		
		return false;
	}
	
	public static boolean opponentSaidMellowOrMightSayMellow(DataModel dataModel, int intBidEstimate) {
		int dealerIndex = dataModel.getDealerIndexAtStartOfRound();
		
		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "JS 9S 7S 6S 4S 8H 5H JC 8C 4C 3C AD 6D")) {
			System.out.println("Debug");
		}
		
		for(int i=dealerIndex + 1; i<Constants.NUM_PLAYERS; i++) {
			if(i == Constants.CURRENT_PARTNER_INDEX) {
				continue;
			}
			if(dataModel.getBid(i) == 0) {
				return true;
			}
		}
		
		if(dealerIndex == Constants.LEFT_PLAYER_INDEX
				&& intBidEstimate + dataModel.getBidTotalSoFar() >= 10) {
			return true;
		}
		
		return false;
	}
}

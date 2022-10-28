package mellow.ai.aiDecider;

import java.util.ArrayList;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.simulation.MonteCarloMain;
import mellow.ai.situationHandlers.BiddingSituation;
import mellow.ai.situationHandlers.NoMellowPlaySituation;
import mellow.ai.situationHandlers.PartnerSaidMellowSituation;
import mellow.ai.situationHandlers.SeatedLeftOfOpponentMellow;
import mellow.ai.situationHandlers.SeatedRightOfOpponentMellow;
import mellow.ai.situationHandlers.SingleActiveMellowPlayer;
import mellow.ai.situationHandlers.bidding.BiddingNearEndOfGameFunctions;
import mellow.ai.situationHandlers.doubleMellow.SeatedLeftOfDoubleMellow;
import mellow.ai.situationHandlers.doubleMellow.SeatedRightOfDoubleMellow;
import mellow.cardUtils.CardStringFunctions;
import mellow.cardUtils.DebugFunctions;

//_______________________
//This is a basic AI that handles mellow rounds for someone who is leading, 2nd, and 3rd and 4th
//_____________________________
//After some calculations, I realized there are hundreds of unique mellow situations that I'd have to make rules for
//to force it to play like me.

//In complicated cases, I'm going to just make run it a monte-carlo simulation so the AI
//can make up it's own mind.

//Update: i'm just going to start simple and play against it and notice where it lacks.
// At that point, I'll improve it based on need

//Same idea goes for monte-carlo which depends on a good and fast decider and a good filter/weigher to work

//When it's good enough, I'll think about doing a decision tree that learned based on the features
//and/or using deep learning.... but that's not anytime soon.


public class MellowBasicDecider implements MellowAIDeciderInterface {
	
	
	DataModel dataModel;
	private boolean doMonteCarloSimuations = false;
	private int num_simulations_for_monte_carlo = MonteCarloMain.NUM_SIMULATIONS_DEFAULT;
	
	//TODO: Consider where the dealer is when bidding (and consider previous bids)
	
	//TODO: handle case where there's a mellow and then double mellow...
	// :(
	
	
	 
	public MellowBasicDecider() {
		this(false, MonteCarloMain.NUM_SIMULATIONS_DEFAULT);
	}
	
	public MellowBasicDecider(boolean doSimuations) {
		this(doSimuations, MonteCarloMain.NUM_SIMULATIONS_DEFAULT);
	}
	
	public MellowBasicDecider(boolean doSimuations, int num_simulations) {
		this.dataModel = new DataModel();
		this.doMonteCarloSimuations = doSimuations;
		this.num_simulations_for_monte_carlo = num_simulations;
	}
	
	
	//Use datamodel that already exists:
	//(this helps with creating a decider that enters in the middle of the game.)
	public MellowBasicDecider(DataModel dataModelInput) {
		this.dataModel = dataModelInput;
	}
	
	/*public static final int SPADE = 0;
	public static final int HEART = 1;
	public static final int CLUB = 2;
	public static final int DIAMOND = 3;
	
	//index
	//0: myCardsUsed
	//1: west cards
	//2: north cards
	//3: east cards
	*/
	
	public void resetStateForNewRound() {
		dataModel.resetStateForNewRound();
	}

	@Override
	public void receiveUnParsedMessageFromServer(String msg) {
		// TODO: use if you want...
	}
	
	public String toString() {
		return "MellowBasicDeciderAI";
	}
	
	@Override
	public void setNameOfPlayers(String players[]) {
		dataModel.setNameOfPlayers(players);
	}
	
	
	//TODO: don't make this so destructive... OR: have testcase say orig card in hand!!!
	@Override
	public void setCardsForNewRound(String cards[]) {
		dataModel.setupCardsInHandForNewRound(cards);
	}


	@Override
	public void setDealer(String playerName) {
		dataModel.setDealer(playerName);
	}

	@Override
	public void receiveBid(String playerName, int bid) {
		dataModel.setBid(playerName, bid);
	}

	@Override
	public void receiveCardPlayed(String playerName, String card) {
		dataModel.updateDataModelWithPlayedCard(playerName, card);
	}

	@Override
	public void setNewScores(int AIScore, int OpponentScore) {
		dataModel.setNewScores(AIScore, OpponentScore);
		
	}

	@Override
	public String getCardToPlay() {
	
		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "QH 7H 3H TD 9D 7D 2D ")) {
			System.out.println("debug");
		}
		String cardRecommended = getCardToPlay2();
		
		return getLegalCard(cardRecommended);
	}
	
	public String getLegalCard(String cardRecommended) {
		//Make sure card is legal for simulation:
		if(dataModel.isCardLegalToPlay(cardRecommended)) {
			System.out.println("TEST: " + dataModel.getPlayers()[0] + " plays the " + cardRecommended);
			return cardRecommended;
		} else {
			
			System.out.println("WARNING: calling getFirstLegalCardThatCouldBeThrown in basic mellow decider");
			return dataModel.getFirstLegalCardThatCouldBeThrown();
		}
	}
	
	private String getCardToPlay2() {
		
		
		if(dataModel.throwerCanOnlyPlayOneCard()) {
			System.out.println("**Forced to play card");
			return dataModel.getOnlyCardCurrentPlayerCouldPlay();
		}

		for(int i=0; i<4; i++) {
			if(dataModel.burntMellow(i)) {
				System.out.println("TEST BURNT MELLOW index: " + i);
			}
		}
		
		//Run montecarlo simulations if config set to monte carlo 
		//AND decider is not currently in a simulation: (Running a simulation in a simulation is expensive)
		if(this.doMonteCarloSimuations && dataModel.getSimulation_level() == 0) {
			return MonteCarloMain.runMonteCarloMethod(dataModel, this.num_simulations_for_monte_carlo);
		}
		
		int numActiveMellows = 0;
		for(int i=0; i<4; i++) {
			if(dataModel.getBid(i) == 0 && dataModel.burntMellow(i) == false) {
				numActiveMellows++;
			}
		}
		
		if(numActiveMellows == 0) {
			return NoMellowPlaySituation.handleNormalThrow(dataModel);
			
		} else if(numActiveMellows == 1) {
			if(dataModel.getBid(Constants.CURRENT_AGENT_INDEX) == 0 && dataModel.burntMellow(Constants.CURRENT_AGENT_INDEX) == false) {
				System.out.println("MELLOW TEST HERE");
				return SingleActiveMellowPlayer.handleThrowAsSingleActiveMellowBidder(dataModel);
				
			} else {
				
				System.out.println("MELLOW SOMEWHERE");
				if(dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) == 0) {
					//TODO: Working on the october 3rd, 2020:
					return PartnerSaidMellowSituation.playMoveToProtectPartnerMellow(dataModel);

				} else {
					
					if(shouldTryToWinTricksInsteadOfAttackingMellow(dataModel)) {

						System.out.println("[WEIRD] We need every trick except maybe the current one!");
						
						//TODO: put in function:
						int throwIndex = dataModel.getCardsPlayedThisRound() % Constants.NUM_PLAYERS;
						
						//If we bid 11 or more, mellow will want to take the trick, so play normal...
						int numBidUs = dataModel.getBid(Constants.CURRENT_AGENT_INDEX) + dataModel.getBid(Constants.CURRENT_PARTNER_INDEX);
						
						//Condition where we assume mellow player won't take it.
						//This fixes 1 test case...
						if(throwIndex == 2 && numBidUs < 11) {
							
							int indexWinner = dataModel.getIndexOfCurrentlyWinningPlayerBeforeAIPlays();
							
							if(indexWinner == Constants.CURRENT_PARTNER_INDEX
									&& dataModel.getBid(Constants.LEFT_PLAYER_INDEX) == 0) {
								
								// play low because mellow player doesn't want to take it...
								
								int leadSuitIndex = dataModel.getSuitOfLeaderThrow();
								if(dataModel.currentAgentHasSuit(leadSuitIndex)) {
									//Play lowest of suit
									return dataModel.getCardCurrentPlayerGetLowestInSuit(dataModel.getSuitOfLeaderThrow());
								} else {
									if( ! dataModel.currentPlayerOnlyHasSpade()) {
										//Play lowest offsuit
										return NoMellowPlaySituation.getJunkiestOffSuitCardBasedOnMadeupValueSystem(dataModel);
									} else {
										//Added condition to play lowest spade because monte carlo wouldn't stop
										//sending warning messages about this.
										return dataModel.getCardCurrentPlayerGetLowestInSuit(Constants.SPADE);
									}
								}
							}
						}
						
						
						//TODO: this should be a new situation...
						String cardTmp = NoMellowPlaySituation.handleNormalThrow(dataModel);
						String curCardToUse = cardTmp;
						
						//Hack to save low low cards that could burn mellow:
						while(dataModel.getNumCardsInPlayOverCardSameSuit(cardTmp) > 5
								&& dataModel.getNumCardsInCurrentPlayersHandOverCardSameSuit(cardTmp) > 0
								) {
							
							String biggerCard = dataModel.getCardInHandClosestOverSameSuit(cardTmp);
							
							if((
									dataModel.getBid(Constants.LEFT_PLAYER_INDEX) == 0
									&& ! dataModel.signalHandler.mellowSignalledNoCardBetweenTwoCardsSameSuitIgnoreLead(cardTmp, biggerCard, Constants.LEFT_PLAYER_INDEX))
								||
								(dataModel.getBid(Constants.RIGHT_PLAYER_INDEX) == 0
										&& ! dataModel.signalHandler.mellowSignalledNoCardBetweenTwoCardsSameSuitIgnoreLead(cardTmp, biggerCard, Constants.RIGHT_PLAYER_INDEX)
									)
								&&
								dataModel.getNumCardsInPlayOverCardSameSuit(cardTmp) > 5) {
								
								curCardToUse = biggerCard;
								
							} else {
								//Increasing it doesn't make a diff, but still consider increasing it.
							}
							cardTmp = biggerCard;
							
						}
						
						return curCardToUse;
						
						//END TODO: put in function
						
					} else {
						
						//Added this for when you're so ahead, you don't care about the
						// opponent's mellow.
						if(dontCareAboutMellow(dataModel)) {
							//System.err.println("I Literally don't care --Hikaru");
							return NoMellowPlaySituation.handleNormalThrow(dataModel);
						}
						
						if(dataModel.getBid(Constants.RIGHT_PLAYER_INDEX) == 0) {
							
							return SeatedLeftOfOpponentMellow.playMoveSeatedLeftOfOpponentMellow(dataModel);
						
						} else {
							
							return SeatedRightOfOpponentMellow.playMoveSeatedRightOfOpponentMellow(dataModel);
						}
					}
				}
			}
			
		} else if(numActiveMellows == 2) {
			
			if(dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) == 0
					&& ! dataModel.burntMellow(Constants.CURRENT_PARTNER_INDEX)) {
				
				if(dataModel.getBid(Constants.LEFT_PLAYER_INDEX) == 0
						&& ! dataModel.burntMellow(Constants.LEFT_PLAYER_INDEX)) {
					return SeatedRightOfDoubleMellow.playMoveSeatedRightOfDoubleMellow(dataModel);
					
				} else if(dataModel.getBid(Constants.RIGHT_PLAYER_INDEX) == 0
						&& ! dataModel.burntMellow(Constants.RIGHT_PLAYER_INDEX)) {

					return SeatedLeftOfDoubleMellow.playMoveSeatedLeftOfDoubleMellow(dataModel);
				
				} else if(dataModel.getBid(Constants.CURRENT_AGENT_INDEX) == 0
						&& ! dataModel.burntMellow(Constants.CURRENT_AGENT_INDEX)) {

					return SingleActiveMellowPlayer.handleThrowAsSingleActiveMellowBidder(dataModel);
					
				} else {
					return PartnerSaidMellowSituation.playMoveToProtectPartnerMellow(dataModel);
				}
				
			} else if(dataModel.getBid(Constants.CURRENT_AGENT_INDEX) == 0 && dataModel.burntMellow(Constants.CURRENT_AGENT_INDEX) == false) {
				
				
				System.out.println("MELLOW TEST (double mellow)");
				String cardToPlay = SingleActiveMellowPlayer.handleThrowAsSingleActiveMellowBidder(dataModel);
				
				//(TODO: put in function)
				//Exceptional case where we want to hide our masters:
				int suitIndexCard = CardStringFunctions.getIndexOfSuit(cardToPlay);
			
				if(dataModel.isMasterCard(cardToPlay)
						&& dataModel.getSuitOfLeaderThrow() != suitIndexCard
						&& dataModel.getNumCardsHiddenInOtherPlayersHandsForSuit(suitIndexCard) > 0
						&& dataModel.getNumberOfCardsOneSuit(suitIndexCard) > 2
						//TODO: copied from BiddingSituation
						&& ( dataModel.getOpponentScore() > 850
								|| 1.5 * (1000 - dataModel.getOpponentScore()) < (1000 - dataModel.getOurScore())
								)
						
						//END TODO
						) {
					

					
					int bestRank = -1;
					for(int suitI=0; suitI<Constants.NUM_SUITS; suitI++) {
						if(suitI != Constants.SPADE
								&& dataModel.currentAgentHasSuit(suitI)
							&& ! dataModel.isMasterCard(dataModel.getCardCurrentPlayerGetHighestInSuit(suitI))) {
							
							String card =  dataModel.getCardCurrentPlayerGetHighestInSuit(suitI);
							if(DataModel.getRankIndex(card) > bestRank) {
								cardToPlay = card;
								bestRank = DataModel.getRankIndex(card);
							}
							
						}
					}
				}
				//End Exceptional case where we want to hide our masters:
				//(END TODO: put in function)
				
				return cardToPlay;

			} else {
				System.out.println("Both opponents said mellow");
				return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
			}
			
		} else if(numActiveMellows >= 3){
			
			//If there's at least 3 mellows, print a warning. I only want to submit the message when the round starts.
			if(dataModel.getCardsPlayedThisRound() == Constants.NUM_CARDS) {
				
				System.err.println("Really??? There's more than 2 mellow bids.");
				System.err.println("I will need to debug the AIs so they know that 3 mellows in 1 round is insane");
				System.err.println("Num active mellows: " + numActiveMellows);
				for(int i=0; i<Constants.NUM_PLAYERS; i++) {
					if(dataModel.getBid(i) > 0) {
						System.err.println("Player " + i + " didn't bid mellow (" + dataModel.getPlayers()[i] + ")");
					}
				}
				System.err.println();
			}
			
			
			if(dataModel.getBid(Constants.CURRENT_AGENT_INDEX) == 0 && dataModel.burntMellow(Constants.CURRENT_AGENT_INDEX) == false) {
				
				
				System.out.println("MELLOW TEST (double mellow)");
				return SingleActiveMellowPlayer.handleThrowAsSingleActiveMellowBidder(dataModel);

			} else if(dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) == 0){

				//TODO: not quite right:
				//This should be the most complicated logic of the game,
				//but let's start simple!
				return PartnerSaidMellowSituation.playMoveToProtectPartnerMellow(dataModel);
				
			} else {

				return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
			}

		} else {
			System.out.println("WARNING: This else case should not happen!");
			System.exit(1);
			return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
		}
		
	}

		
	//TODO: will need to test/improve get BidToMake.
	
	@Override
	public String getBidToMake() {
	//Run montecarlo simulations if config set to monte carlo 
	//AND decider is not currently in a simulation: (Running a simulation in a simulation is expensive)
		if(this.doMonteCarloSimuations && dataModel.getSimulation_level() == 0) {
			return MonteCarloMain.runMonteCarloMethod(dataModel, this.num_simulations_for_monte_carlo);
		} else {
			return BiddingSituation.getSimpleBidToMake(dataModel);
		}
	}
	
	
	//TODO: ONLY USE FOR FINDING THE TYPE OF TEST
	public DataModel getCopyOfDataModel() {
		return this.dataModel.createHardCopy();
	}

	public static boolean dontCareAboutMellow(DataModel dataModel) {
		int ourScore = BiddingNearEndOfGameFunctions.getProjectedScoreForTeamGivenBids(dataModel, true,
				dataModel.getBid(Constants.CURRENT_AGENT_INDEX),
				dataModel.getBid(Constants.CURRENT_PARTNER_INDEX));
		
		int theirScore = BiddingNearEndOfGameFunctions.getProjectedScoreForTeamGivenBids(dataModel, false,
				dataModel.getBid(Constants.LEFT_PLAYER_INDEX),
				dataModel.getBid(Constants.RIGHT_PLAYER_INDEX));
		
		if(ourScore >= Constants.GOAL_SCORE
				&& ourScore > theirScore + 
						BiddingNearEndOfGameFunctions.getNumberOfPointsAvailableAsBonusIfEveryoneMakesIt(dataModel)) {
			return true;
		}/* else if(Constants.GOAL_SCORE - ourScore <= (Constants.GOAL_SCORE - theirScore) / 2
				&& Constants.GOAL_SCORE - ourScore > 80) {
			return true;
		}*/
		
		
		return false;
	}
	
	public static boolean shouldTryToWinTricksInsteadOfAttackingMellow(DataModel dataModel) {
		
		if(getNumTricksWeCouldAffordToGiveAway(dataModel) == 0) {
			return true;
		} else {
		
			return false;
		}
	}
	
	public static int getNumTricksWeCouldAffordToGiveAway(DataModel dataModel) {
		
		
		if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "JH 8H 7H")) {
			System.out.println("DEBUG");
		}
		int numTricksUs = dataModel.getNumTricks(Constants.CURRENT_AGENT_INDEX) + dataModel.getNumTricks(Constants.CURRENT_PARTNER_INDEX);
		
		int numTricksThem = dataModel.getNumTricks(Constants.LEFT_PLAYER_INDEX) + dataModel.getNumTricks(Constants.RIGHT_PLAYER_INDEX);
		
		int numBidUs = dataModel.getBid(Constants.CURRENT_AGENT_INDEX) + dataModel.getBid(Constants.CURRENT_PARTNER_INDEX);
		
		int numTricksAvailable = Constants.NUM_STARTING_CARDS_IN_HAND - numTricksUs - numTricksThem;
		
		int numTricksNeededBeforeFight = numTricksAvailable - (numBidUs  - numTricksUs);
		
		int throwIndex = dataModel.getCardsPlayedThisRound() % Constants.NUM_PLAYERS;
		
		//TODO: TMP shortcut:
		if(numTricksNeededBeforeFight >= 2) {
			return numTricksNeededBeforeFight;
		}
		
		
		int numTricksNeeded= numTricksNeededBeforeFight;
		if(throwIndex > 1) {
			
			int indexWinner = dataModel.getIndexOfCurrentlyWinningPlayerBeforeAIPlays();
			
			if(indexWinner != Constants.CURRENT_PARTNER_INDEX) {
				
				String hypoCardToPlay = NoMellowPlaySituation.handleNormalThrow(dataModel);
				String curWinner = dataModel.getCurrentFightWinningCardBeforeAIPlays();
				
				if(dataModel.cardAGreaterThanCardBGivenLeadCard(curWinner, hypoCardToPlay )) {
					
					//Opponents get the last trick:
					numTricksNeeded = numTricksNeededBeforeFight -1;
				}
				
			}
		}
		

		//TODO: What if you know you're lost because opponents have S?
		
		if(numTricksNeeded < 0) {
			System.out.println("[WEIRD] WE BURNED!");
		}
		
		return numTricksNeeded;
		//;
	}
	
}

package mellow.ai.simulation;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;

import mellow.Constants;
import mellow.ai.aiDecider.MellowAIDeciderFactory;
import mellow.ai.aiDecider.MellowAIDeciderInterface;
import mellow.ai.aiDecider.MellowBasicDecider;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.cardDataModels.StatsBetweenRounds;
import mellow.ai.cardDataModels.normalPlaySignals.VoidSignalsNoActiveMellows;
import mellow.ai.simulation.actionJudgerTODO.QuickActionJudger;
import mellow.ai.simulation.simulationSetupImpl.SimSetupUtils;
import mellow.ai.simulation.simulationSetupImpl.SimulationSetup;
import mellow.ai.simulation.simulationSetupImpl.SimulationSetupWithMemBoost;
import mellow.ai.simulation.simulationSetupImpl.SimulationSetupWithSignalsAndMemBoost;
import mellow.ai.simulation.winPercEstimates.ProbWinGetter;
import mellow.ai.situationHandlers.bidding.BasicBidMellowWinProbCalc;
import mellow.cardUtils.CardStringFunctions;
import mellow.cardUtils.DebugFunctions;
import mellow.testcase.testCaseParser;

//TODO: maybe compliment this with a decision tree?
//https://softwareengineering.stackexchange.com/questions/157324/decision-trees-vs-neural-networks

//Vote for decision tree over random forests:
//https://stats.stackexchange.com/questions/285834/difference-between-random-forests-and-decision-tree

public class MonteCarloMain {
	
	
	public static void main(String args[]) {
		
		//testCaseParser.TEST_FOLDERS = new String[] {"MonteCarloTests"};
		testCaseParser.TEST_FOLDERS = new String[] {"tmp"};
		//testCaseParser.TEST_FOLDERS = new String[] {"MonteCarloSignals"};
		//testCaseParser.TEST_FOLDERS = new String[] {"tmpRecentFails"};
		//testCaseParser.TEST_FOLDERS = new String[] {"MonteCarloTestsDone"};
		
		//testCaseParser.TEST_FOLDERS = new String[] {"newBidTestcases"};
		//testCaseParser.TEST_FOLDERS = new String[] {"newFollowFails", "newBonusChecks"};
		//testCaseParser.TEST_FOLDERS = new String[] {"newLeadFails"};
		//testCaseParser.TEST_FOLDERS = new String[] {"newLeadFails2"};
		//testCaseParser.TEST_FOLDERS = new String[] {"TestPython"};
		
		//testCaseParser.TEST_FOLDERS = new String[] {"newBonusChecks"};
		String args2[] = new String[1];
		args2[0] = "monte";
		testCaseParser.main(args2);
		
	}
	//Guess at reason number of simulations to try
	
	//TODO: make # of simulation configurable...

	//TODO: (There numbers might be way too high... 10 simulations seems to have honed in on right answer)	
	//Slow setting:
	//public static int LIMIT_THOROUGH_SEARCH = 2000;
	//public static int NUM_SIMULATIONS_SAMPLE = 1000;
	
	//Slow but good (100 limit is too prone to random swings and there's no clear signal)
	public static int LIMIT_THOROUGH_SEARCH = 2000;

	public static int NUM_SIMULATIONS_DEFAULT = 200;
	

	//Deep dive into 1 test case slow:
	//public static int NUM_SIMULATIONS_THOROUGH_AND_SLOW = 2000000;

	//public static int NUM_SIMULATIONS_THOROUGH_AND_SLOW = 120000;
	//Overnight slow
	//public static int NUM_SIMULATIONS_THOROUGH_AND_SLOW = 60000;
	
	//Do dishes and cook slow:
	//public static int NUM_SIMULATIONS_THOROUGH_AND_SLOW = 20000;
	
	//public static int NUM_SIMULATIONS_THOROUGH_AND_SLOW = 10000;

	//Watch TV slow:
	//public static int NUM_SIMULATIONS_THOROUGH_AND_SLOW = 5000;
	
	//Think while it works slow:
	//public static int NUM_SIMULATIONS_THOROUGH_AND_SLOW = 2000;
	//public static int NUM_SIMULATIONS_THOROUGH_AND_SLOW = 1000;
	
	//Quick useless test: (Maybe test the Monte Carlo Main function)
	//public static int NUM_SIMULATIONS_THOROUGH_AND_SLOW = 100;
	//public static int NUM_SIMULATIONS_THOROUGH_AND_SLOW = 1;
	
	public static int NUM_SIMULATIONS_THOROUGH_AND_SLOW = 400;
	
	//Test case stats as of oct 5th, 2019:
	//Consistency between parallel runs:
	//100:    74%
	//1000:   81.4%
	//10000:  89%
	//20000:  91.36%
	//(It's not too consistent, but at least it's not off by much points on avg when it's wrong...)
	
	//Accuracy compared to what I would do:
	//100:   67.4%
	//1000:  70.2%
	//10000: 71.8%
	//20000: 71.43%
	//End test case stats.
	
	//Experience stats:
	//Monte carlo AI 1000 is slow, but it's not that bad...
	
	//TODO: set limits low for testing/debugging:
	//public static int LIMIT_THOROUGH_SEARCH = 20;
	//public static int NUM_SIMULATIONS_SAMPLE = 10;
	//END TODO

	//No more debug print!
	public static int MAX_NUM_SIMULATIONS_WHILE_DEBUG_PRINT = 0;
	
	
	public static double HUNDRED_PERCENT = 100.0;
	
	//For testing:
	//public static Scanner in = new Scanner(System.in);
	
	//pre: it's the current dataModel's Turn...

	//Return number string for bid
	//Return card for action
	public static String runMonteCarloMethod(DataModel dataModel, int num_simulations) {
		
		boolean testWithSignals = true;
		
		//Slow
		//SimulationSetup simulationSetup = new SimulationSetup(dataModel, testWithSignals);
		
		//Faster:
		//SimulationSetupInterface simulationSetup = new SimulationSetupWithMemBoost(dataModel, testWithSignals);
		
		//Better design and doesn't have the signal hack:
		SimulationSetupWithSignalsAndMemBoost simulationSetup = new SimulationSetupWithSignalsAndMemBoost(dataModel);
		
		//TODO: maybe incorporate the idea that players might be running out of a suit somehow?
		
		return runMonteCarloMethod(dataModel, simulationSetup, num_simulations, true, testWithSignals);
	}

	public static String runMonteCarloMethod(DataModel dataModel, SimulationSetupInterface simulationSetup, int num_simulations_orig, boolean skipSimulationsBasedOnBids, boolean processSignals) {
		
		System.out.println("RUN SIMULATION");
		//in.nextLine();
		
		//The number of simulations done could be subject to change
		// but we want to keep the orig #...
		int num_simulations = num_simulations_orig;

		if(processSignals == false
				&& simulationSetup.hasSignalsBakedIn()) {
			System.out.println("ERROR: Trying to not process signals while using a simulation Setup with signals baked in.");
			System.exit(1);
		}

		long numWaysOtherPlayersCouldHaveCards = simulationSetup.initSimulationSetupAndRetNumWaysOtherPlayersCouldHaveCards();
		
		boolean isThorough = false;
		if(numWaysOtherPlayersCouldHaveCards < LIMIT_THOROUGH_SEARCH
				|| (skipSimulationsBasedOnBids && numWaysOtherPlayersCouldHaveCards <= 20 * num_simulations)
				|| ( numWaysOtherPlayersCouldHaveCards <= 3 * num_simulations)) {
			isThorough = true;	
		}
		
		if(isThorough) {
			num_simulations = (int)numWaysOtherPlayersCouldHaveCards;
		}
		
		//DEBUG: print possibilities:
		String unknownCards[] = dataModel.getUnknownCards();
		String sortedUnknownCards[] = CardStringFunctions.sort(unknownCards);
		
		System.out.println("DEBUG: Unknown cards:");
		for(int i=0; i<sortedUnknownCards.length; i++) {
			System.out.print(sortedUnknownCards[i] + " ");
		}
		System.out.println();
		
		dataModel.printVoidArray(processSignals);
		
		System.out.println("DEBUG: Obvious and active cards:");
		String obviousCards[] = dataModel.getActiveCardsWithObviousOwnersInOtherHandsDebug();
		for(int i=0; i<obviousCards.length; i++) {
			System.out.print(obviousCards[i] + " ");
		}
		System.out.println();
		
		System.out.println("DEBUG: obvious and active cards base on void signals:");
		String signalledCard[] = dataModel.getActiveCardsWithSignalledOwnersInOtherHandsDebug();
		for(int i=0; i<signalledCard.length; i++) {
			System.out.print(signalledCard[i] + " ");
		}
		System.out.println();
		//END DEBUG PRINT POSSIBILITIES
		
		System.out.println("DEBUG getCardPossibilities:");
		SimulationPosibilitiesHandler simulationPossibilities = new SimulationPosibilitiesHandler(dataModel);
		System.out.println("END DEBUG getCardPossibilities:");
		
		//Filter out simulations where the actions don't mix well with what the distributions are:
		//Needs work... So far, it deals with K leads.
		QuickActionJudger quickActionJudger = new QuickActionJudger(dataModel);
		
		
		String actionString[];
		int maxBidThatIsRealistic = -1;
		
		if(dataModel.stillInBiddingPhase()) {
			int bid = Integer.parseInt(new MellowBasicDecider(dataModel).getBidToMake());
			maxBidThatIsRealistic = (bid + 3);
			
			//Near end-of-game bid stretch check:
			if(dataModel.getOurScore() > 900
					|| dataModel.getOpponentScore() > 900) {
				maxBidThatIsRealistic = Math.min(Constants.NUM_STARTING_CARDS_IN_HAND, bid+7);
			}
			//End near end-of-game bid stretch check.

			//TODO: Might not need as many simulations for bids...
			//num_simulations /= 2;
			
			
			//Always try to mellow because it's fun!
			actionString = new String[maxBidThatIsRealistic + 1];
			for(int i=0; i<actionString.length; i++) {
				actionString[i] = i + "";
			}
			
		} else {
			actionString = dataModel.getListOfPossibleActions();
			
		}

		double actionUtil[];
		actionUtil = new double[actionString.length];
		for(int i=0; i<actionUtil.length; i++) {
			actionUtil[i] = 0.0;
		}
		
		//Adding an alternate util function that approximates the Win% at the end of the round:
		double actionUtilWP[];
		actionUtilWP = new double[actionString.length];
		for(int i=0; i<actionUtilWP.length; i++) {
			actionUtilWP[i] = 0.0;
		}
		
		
		//Reduce number of print statements called:
		PrintStream originalStream = System.out;
		if(num_simulations > MAX_NUM_SIMULATIONS_WHILE_DEBUG_PRINT) {
			System.setOut(dummyStream);
		}
		
		
		double sum_impact_to_avg = 0.0;
		
		int numSkipped = 0;
		
		//In my experience, skipping more than 100 X num_simulations makes it too slow.
		int maxSkipped = 100 * num_simulations;
		int i=0;
		int lastestPost = -1;
		
		
		for(; i<num_simulations && numSkipped < maxSkipped; i++) {
			
			if( (i % Math.max(num_simulations/20, 100) == 0) 
					&& i > lastestPost) {
				System.err.println(i+ " out of " + num_simulations);
				lastestPost = i;
				
				if(i > 0) {
					if(! isThorough) {
						System.err.println("Skipped " + numSkipped+ " out of " + (numSkipped + i));
						System.err.println("That's a " + String.format("%.2f", (100.0*numSkipped) /(1.0*(numSkipped + i))) + "% skip rate.");
						System.err.println();
					} else {
						System.err.println("Skipped " + numSkipped+ " out of " + i);
						System.err.println("That's a " + String.format("%.2f", (100.0*numSkipped) /(1.0*(i))) + "% skip rate.");
						System.err.println();
					}
				}
				
				//sanityCheckPrintCardSuitFreq();
			}
			//Distribute unknown cards for simulation:
			String distCards[][];
			
			long randomDistributionNumber = -1;
			
			if(isThorough) {
				randomDistributionNumber = i;
			} else {
				randomDistributionNumber = 
					SimSetupUtils.getRandNumberFrom0ToN(numWaysOtherPlayersCouldHaveCards);
			}

			distCards = simulationSetup.getPossibleDistributionOfUnknownCardsBasedOnIndex(randomDistributionNumber);
			
			//sanityCheckUpdateCardSuitFreq(distCards);
			
			//Check if distCards happen to line up with what the signals are saying:
			if(processSignals
				&& ! simulationSetup.hasSignalsBakedIn()) {
				
				//3rd arg is just for debug
				boolean realistic = isCardDistRealistic2(dataModel, distCards, simulationPossibilities, numSkipped < 50);
				
				if( ! realistic) {
					
					if(isThorough == false) {
						i--;
					}
					
					numSkipped++;
					continue;
				}

			}
			
			

			//For better results, check how realistic the distribution of cards is compared to what the original bid was and try
			//      to dampen the effect of unrealistic distributions of cards:
			double decisionImpact = getRelativeImpactOfSimulatedDistCards(dataModel, distCards);
			//double decisionImpact = 1.0;
			
			boolean verbose = i < 10 && numSkipped < 100;
			
			if(decisionImpact < 0.1
					|| ! quickActionJudger.actionFilterAcceptsDistribution(dataModel, distCards, verbose)
				) {
				
				//Make sure bids are made by players that know what they are doing:
				if( ! dataModel.stillInBiddingPhase()
						&& (
								//<= 8 is possible sometimes... Especially when I accidentally bid too low.
						(dataModel.getBidTotal() <= 5
						&& ! dataModel.someoneBidMellow())
						|| dataModel.getBidTotal() >= 15
						)
					){
					
					if(i == 0 && numSkipped < 100) {
						System.err.println("WARNING: Bids don't make sense! Monte will not be skipping any hands because of bids.");
					}
				} else {
					
					if(dataModel.getBidTotal() < 8
							&& verbose) {
						
						System.err.println("WARNING: Bids don't make sense! But Monte will skip bad bids anyways! (iter, numberSkiped) = (" + i + ", " + numSkipped + ")");
					}
					//For now, don't skip if thorough.... I don't know!
					if(skipSimulationsBasedOnBids) {
						if(isThorough == false) {
							i--;
						}
						//System.err.println("SKIP");
						numSkipped++;
						continue;
					}
				}
			}

			
			
			sum_impact_to_avg +=  decisionImpact;
			

			DataModel dataModelTmpForPlayer0;
			MellowBasicDecider playersInSimulation[];
			
			//System.err.println("---------");
			
			for(int a=0; a<actionString.length; a++) {

				if(dataModel.stillInBiddingPhase()
						&& Integer.parseInt(actionString[a]) > maxBidThatIsRealistic) {
					continue;
				}
				if(a == actionString.length - 1) {
					System.out.println("Debug");
				}
				
				System.out.println("Possible action: " + actionString[a]);
				
				//System.err.println();
				//System.err.println();
				//System.err.println("Possible action: " + actionString[a]);
				
				//Create a tmp data model for current player to keep track of everything:
				dataModelTmpForPlayer0 = dataModel.createHardCopy();
				
				//Tell the data model that it's in the next level of a simulation.
				dataModelTmpForPlayer0.incrementSimulationLevel();
				
				if(dataModelTmpForPlayer0.stillInBiddingPhase()) {
					dataModelTmpForPlayer0.setBid(dataModel.getPlayers()[0], Integer.parseInt(actionString[a]));
				} else {
					dataModelTmpForPlayer0.updateDataModelWithPlayedCard(dataModel.getPlayers()[0], actionString[a]);
				}
				
				playersInSimulation = setupAIsForSimulation(dataModelTmpForPlayer0, distCards);

				

				playOutSimulationTilEndOfRound(dataModelTmpForPlayer0, playersInSimulation);
			
				
				//TODO:
				//The cards played leading up to the test case should
				// give some basic info:
				//TODO: this is supposed to be run once per test case, not once per simulation!
				//QuickActionJudger quickActionJudger = new QuickActionJudger(dataModel);
				
				//Make monte carlos sims more readable (for the point diff util):
				StatsBetweenRounds endOfRoundPointDiffStats = getPointDiffEndOfRound(dataModelTmpForPlayer0);
				

				//Get monte carlo end of round stats for the win percentage util:
				StatsBetweenRounds endOfRoundStats = getStatsAfterSimulatedRound(dataModelTmpForPlayer0);

				
				//Debug:
				/*if(endOfRoundStats.getOpponentScore() < Constants.GOAL_SCORE) {
					printOutcomeOfSimulation(dataModelTmpForPlayer0, actionString, a, endOfRoundPointDiffStats, endOfRoundStats);
				}*/
				//END DEBUG
				
				//System.err.println("Score at end with card " + actionString[a] + ": " + endOfRoundPointDiffStats.getAIScore());
				
				//Get Util at the end of the simulated round:
					//Util is a function of scoreA, scoreB, isDealer
					//TODO: this is just getting the point difference after the round and isn't the most useful measure of how well we're doing.
					//A better strat would be an approx measure of the current player's winning chances... which is hard to calculate.
				actionUtil[a] += decisionImpact * getUtilOfStatsAtEndOfRoundSimulationBAD(endOfRoundPointDiffStats);
				
				//New util function that I want to transition to:
				actionUtilWP[a] += decisionImpact * getApproxWinPercForPointsAtEndOfRound(endOfRoundStats);
				
				
				/*System.out.println("Util (point diff) when the " + actionString[a] + ": " + getUtilOfStatsAtEndOfRoundSimulationBAD(endOfRoundStats));
				in.next();*/
			}
			
			//TESTING DISTRIBUTION:
			//testPrintUnknownCardDistribution(in, distCards, i);
		}
		
		
		//Allow print statements now that the simulation is over:
		System.setOut(originalStream);

		int numSimulationsNotSkipped = 0;
		if(isThorough) {
			numSimulationsNotSkipped = num_simulations - numSkipped;
			System.err.println(numSimulationsNotSkipped + " out of " + num_simulations);
			
		} else {
			System.err.println(i+ " out of " + num_simulations);
			numSimulationsNotSkipped = i;
		}
		
		
		testPrintAverageUtilityOfEachMove(actionString, actionUtil, sum_impact_to_avg, numSimulationsNotSkipped, false);

		System.err.println();
		System.err.println("Alternative measurement with approx win%:");
		for(int j=0; j<actionUtilWP.length; j++) {
			actionUtilWP[j] = HUNDRED_PERCENT * actionUtilWP[j];
		}
		testPrintAverageUtilityOfEachMove(actionString, actionUtilWP, sum_impact_to_avg, numSimulationsNotSkipped, true);
		
		int maxPointsUtil = getMaxIndex(actionUtil);
		
		int maxWinPercUtil = getMaxIndex(actionUtilWP);
		
		if(maxPointsUtil != maxWinPercUtil && actionUtil[maxPointsUtil] > actionUtil[maxWinPercUtil]) {
			System.err.println("WARNING: The two different utility calculations disagree with each other!");
		}
		

		System.out.print("END OF SIMULATION  PLAY: ");

		//Hack to avoid NaN answer after simulation:
		if(numSimulationsNotSkipped == 0
				|| numSimulationsNotSkipped * 1000 < num_simulations) {
			
			if(skipSimulationsBasedOnBids) {
				skipSimulationsBasedOnBids = false;
				System.err.println("RETRY without skipping any simulations because of bids:");
				return runMonteCarloMethod(dataModel, simulationSetup, num_simulations_orig, skipSimulationsBasedOnBids, processSignals);
				
			} else if(processSignals 
					&& ! simulationSetup.hasSignalsBakedIn()) {
				processSignals = false;
				System.err.println("RETRY without processing any signals:");
				return runMonteCarloMethod(dataModel, simulationSetup, num_simulations_orig, skipSimulationsBasedOnBids, processSignals);
				
			} else if(simulationSetup.hasSignalsBakedIn()) {
				
				    skipSimulationsBasedOnBids = false;
					processSignals = false;
					System.out.println("RETRY running simulation without signals:");
					SimulationSetupInterface simulationSetupNoSignals = new SimulationSetupWithMemBoost(dataModel, processSignals);
					return runMonteCarloMethod(dataModel, simulationSetupNoSignals, num_simulations_orig, skipSimulationsBasedOnBids, processSignals);
					
			}

		}
		
		//in.next();
		
		return actionString[getMaxIndex(actionUtil)];
	}
	
	static PrintStream dummyStream = new PrintStream(new OutputStream(){
	    public void write(int b) {
	        // NO-OP
	    }
	});
	
	public static double getRelativeImpactOfSimulatedDistCards(DataModel dataModel, String distCards[][]) {
		DataModel dataModelTmpForPlayer0 = dataModel.createHardCopy();
		
		
		
		
		double impact = 1.0;
		for(int playerI=0; playerI<Constants.NUM_PLAYERS; playerI++) {
			if(playerI == Constants.CURRENT_AGENT_INDEX) {
				//We know the current player's bid is what's expected, so skip that check:
				continue;

			} else if(dataModel.stillInBiddingPhase()
					&& dataModel.getDealerIndexAtStartOfRound()
						 >= playerI) {
				//player didn't even bid yet!
				continue;
			}
			
			String hand[] = dataModelTmpForPlayer0.getGuessAtOriginalCardsHeld(playerI, distCards[playerI]);
			
			MellowBasicDecider tempDecider = new MellowBasicDecider();
			tempDecider.resetStateForNewRound();
			
			
			if(playerI % 2 == Constants.CURRENT_AGENT_INDEX) {
				tempDecider.setNewScores(dataModel.getOurScore(), dataModel.getOpponentScore());
			} else {
				tempDecider.setNewScores(dataModel.getOpponentScore(), dataModel.getOurScore());
			}
			
			//Set names of players such that players[0] is the playerIndex:
			String playersRelativeToPlayerI[] = new String[Constants.NUM_PLAYERS];
			
			for(int i=0; i<playersRelativeToPlayerI.length; i++) {
				playersRelativeToPlayerI[i] = dataModel.getPlayers()[(playerI + i) % 4];
			}
			
			tempDecider.setNameOfPlayers(playersRelativeToPlayerI);
			tempDecider.setCardsForNewRound(hand);
			
			
			int dealerIndexForPlayer = dataModel.getDealerIndexAtStartOfRound();
			//Set dealer:
			tempDecider.setDealer(dataModel.getPlayers()[dealerIndexForPlayer]);
			
			
			//System.err.println("Check this player's bid: " + dataModel.getPlayers()[playerI]);
			//Set previous bids:
			for(int curActionIndex=dataModel.getDealerIndexAtStartOfRound() + 1; curActionIndex % Constants.NUM_PLAYERS != playerI; curActionIndex++) {
				
				curActionIndex = curActionIndex % Constants.NUM_PLAYERS;
				//System.err.println("Get Bid from " + dataModel.getPlayers()[curActionIndex]);
				tempDecider.receiveBid(dataModel.getPlayers()[curActionIndex], dataModel.getBid(dataModel.getPlayers()[curActionIndex]));
			}
			
			
			int expectedResponse = dataModelTmpForPlayer0.getBid(playerI);
			int response = 0;
			try {
				response = Integer.parseInt(tempDecider.getBidToMake());
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("ERROR: could not parse bid in monte carlo simulation");
				System.exit(1);
			}
			
			//System.err.println("( Response from " + dataModel.getPlayers()[playerI] + ": " + response + ") ");
			//System.err.println();
			
			if(response > 0 && expectedResponse > 0
					&& playerI == Constants.CURRENT_PARTNER_INDEX) {
				//For now, only tolerate an off-by-one error:
				if(Math.abs(expectedResponse - response) >= 4) {
					impact /= 130;
		
				} else if(Math.abs(expectedResponse - response) >= 3) {
					impact /= 80;
		
				} else if(Math.abs(expectedResponse - response) >= 2) {
					impact /= 50;
	
				} else if(Math.abs(expectedResponse - response) >= 1) {
					impact /= 30;
				}
			} else if(response > 0 && expectedResponse > 0) {
				if(Math.abs(expectedResponse - response) >= 4) {
					impact /= 80;
		
				} else if(Math.abs(expectedResponse - response) >= 3) {
					impact /= 50;
		
				} else if(Math.abs(expectedResponse - response) >= 2) {
					impact /= 30;
	
				} else if(Math.abs(expectedResponse - response) >= 1) {
					impact /= 20;
				}
			} else {
				//TODO I don't know I'm dividing by less than the previous case, but whatever. 
				if(Math.abs(expectedResponse - response) >= 3) {
					impact /= 30;
		
				} else if(Math.abs(expectedResponse - response) >= 2) {
					impact /= 15;
	
				} else if(Math.abs(expectedResponse - response) >= 1) {
					impact /= 12;
				}
			}
		}
		
		return impact;
		
	}
	
	public static int getMaxIndex(double array[]) {

		int bestIndex = 0;
		for(int i=0; i<array.length; i++) {
			if(array[i] > array[bestIndex]) {
				bestIndex = i;
			}
		}
		
		return bestIndex;
	}
	
	public static MellowBasicDecider[] setupAIsForSimulation(DataModel dataModelTmpForPlayer0, String distCards[][]) {
		
		MellowBasicDecider playerInSimulation[] = new MellowBasicDecider[Constants.NUM_PLAYERS];
		
		//For each player: create a HARD coded data model for their perspective (Maybe their own MellowBasicDecider):
		for(int j=0; j<Constants.NUM_PLAYERS; j++) {
			if(j==0) {
				playerInSimulation[j] = new MellowBasicDecider(dataModelTmpForPlayer0);
			} else {
				playerInSimulation[j] = new MellowBasicDecider(dataModelTmpForPlayer0.getDataModelFromPerspectiveOfPlayerI(j, distCards));
			}
		}
		
		return playerInSimulation;
	}
	
	public static DataModel playOutSimulationTilEndOfRound(DataModel dataModelTmpForPlayer0, MellowBasicDecider playerInSimulation[]) {
		
		int numCardsPlayed = dataModelTmpForPlayer0.getCardsPlayedThisRound();
		String players[] = dataModelTmpForPlayer0.getPlayers();

		//System.err.println();
		//System.err.println("TEST: ");
		//Get whose turn it is:
		int actionIndex = dataModelTmpForPlayer0.getCurrentActionIndex();
		
		if(dataModelTmpForPlayer0.stillInBiddingPhase()) {
			
			while(actionIndex <= dataModelTmpForPlayer0.getDealerIndexAtStartOfRound()) {
				
				int bid = Integer.parseInt(playerInSimulation[actionIndex].getBidToMake());
				
				for(int k=0; k<Constants.NUM_PLAYERS; k++) {
					playerInSimulation[k].receiveBid(players[actionIndex], bid);
				}
				
				actionIndex++;
			}
			
		}
		
		
		for(int j=numCardsPlayed; j<Constants.NUM_CARDS; j++) {
			
			//If it's the beginning of a new round:
			if(j % Constants.NUM_PLAYERS == 0) {
				//figure out who leads
				actionIndex = dataModelTmpForPlayer0.getCurrentActionIndex();
				//System.err.println("New round");

			}
			
			
				
			String card = playerInSimulation[actionIndex].getCardToPlay();
			for(int k=0; k<Constants.NUM_PLAYERS; k++) {
				playerInSimulation[k].receiveCardPlayed(players[actionIndex], card);
			}

			//System.err.println(playerInSimulation[actionIndex].getCopyOfDataModel().getPlayers()[0] + ": " + card);

			actionIndex = (actionIndex + 1) % Constants.NUM_PLAYERS;
			
		
		}

		if(dataModelTmpForPlayer0.getCardsPlayedThisRound() != Constants.NUM_CARDS ) {
			System.err.println("ERROR: (in simulation) simulation hasn't reached the end of the round");
			System.exit(1);
		}
		
		return dataModelTmpForPlayer0;
	}
	
	public static StatsBetweenRounds getPointDiffEndOfRound(DataModel dataModelTmpForPlayer0) {
		int curScoreUs = 0;
		int curScoreThem = 0;
		
		int numMellowUS = 0;
		int numMellowThem = 0;
		
		//Handle mellows:
		for(int i=0; i<Constants.NUM_PLAYERS; i++) {
			if(dataModelTmpForPlayer0.saidMellow(i)) {
				int MULTIPLIER = 1;
				
				if(dataModelTmpForPlayer0.burntMellow(i)) {
					MULTIPLIER = -1;
				}
				
				if(i%2 == 0) {
					curScoreUs += MULTIPLIER * 100;
					numMellowUS++;
				} else {
					curScoreThem += MULTIPLIER * 100;
					numMellowThem++;
				}
			}
		}

		if(numMellowUS < 2){
			curScoreUs += handleNormalBidScoreDiff(dataModelTmpForPlayer0, true);
		}
		
		if(numMellowThem < 2) {
			curScoreThem += handleNormalBidScoreDiff(dataModelTmpForPlayer0, false);
		}
		
		StatsBetweenRounds ret = new StatsBetweenRounds();
		ret.setScores(curScoreUs, curScoreThem);
		
		int nextDealerIndex = (dataModelTmpForPlayer0.getDealerIndexAtStartOfRound() + 1) % Constants.NUM_PLAYERS;
		ret.setDealerIndexAtStartOfRound(nextDealerIndex);
		
		return ret;
	}
	//Get stats at the end of the round:
	//(i.e: Get scores and dealer index at the end of the round)
	public static StatsBetweenRounds getStatsAfterSimulatedRound(DataModel dataModelTmpForPlayer0) {
		int scoreUsAtStartOfRound = dataModelTmpForPlayer0.getOurScore();
		int scoreThemAtStartOfRound = dataModelTmpForPlayer0.getOpponentScore();
		
		int curScoreUs = scoreUsAtStartOfRound;
		int curScoreThem = scoreThemAtStartOfRound;
		
		int numMellowUS = 0;
		int numMellowThem = 0;
		
		//Handle mellows:
		for(int i=0; i<Constants.NUM_PLAYERS; i++) {
			if(dataModelTmpForPlayer0.saidMellow(i)) {
				int MULTIPLIER = 1;
				
				if(dataModelTmpForPlayer0.burntMellow(i)) {
					MULTIPLIER = -1;
				}
				
				if(i%2 == 0) {
					curScoreUs += MULTIPLIER * 100;
					numMellowUS++;
				} else {
					curScoreThem += MULTIPLIER * 100;
					numMellowThem++;
				}
			}
		}

		if(numMellowUS < 2){
			curScoreUs += handleNormalBidScoreDiff(dataModelTmpForPlayer0, true);
		}
		
		if(numMellowThem < 2) {
			curScoreThem += handleNormalBidScoreDiff(dataModelTmpForPlayer0, false);
		}
		
		StatsBetweenRounds ret = new StatsBetweenRounds();
		ret.setScores(curScoreUs, curScoreThem);
		
		int nextDealerIndex = (dataModelTmpForPlayer0.getDealerIndexAtStartOfRound() + 1) % Constants.NUM_PLAYERS;
		ret.setDealerIndexAtStartOfRound(nextDealerIndex);
		
		return ret;
	}
	
	public static int handleNormalBidScoreDiff(DataModel dataModelTmpForPlayer0, boolean teamIsUs) {
		int adjustmentIndex = 0;
		
		if(teamIsUs == false) {
			adjustmentIndex = 1;
		}
		
		int scoreAdjustment = 0;
		
		int numBids = dataModelTmpForPlayer0.getBid(0 + adjustmentIndex) + dataModelTmpForPlayer0.getBid(2 + adjustmentIndex);
		int numTricks = dataModelTmpForPlayer0.getNumTricks(0 + adjustmentIndex) + dataModelTmpForPlayer0.getNumTricks(2 + adjustmentIndex);
		
		if(numBids <= numTricks) {
			scoreAdjustment = 10*numBids + 1 * (numTricks - numBids);
		} else {
			scoreAdjustment = -10*numBids;
		}
		
		return scoreAdjustment;
	}
	
	public static double getUtilOfStatsAtEndOfRoundSimulationBAD(StatsBetweenRounds endOfRoundStats) {
		
		//This is a bad approximation, but for now it will work:
		return endOfRoundStats.getAIScore() - endOfRoundStats.getOpponentScore();
	}
	
	public static double getApproxWinPercForPointsAtEndOfRound(StatsBetweenRounds endOfRoundStats) {
		
		if(endOfRoundStats.getDealerIndexAtStartOfRound() % 2 == 0) {
			//return prob winning when our team is the dealer at the start of the next round:
			return ProbWinGetter.getPercentageWin(endOfRoundStats.getAIScore(), endOfRoundStats.getOpponentScore());
		
		} else {
			//return prob winning when other team is the dealer at the start of the next round: (It's reversed)
			return 1.0 - ProbWinGetter.getPercentageWin(endOfRoundStats.getOpponentScore(), endOfRoundStats.getAIScore());
		}
		
	}
	
	public static void testPrintUnknownCardDistribution(Scanner in, String distCards[][], int simulationNumber) {
		System.out.println("Unknown card distribution for simulation #" + simulationNumber);
		
		for(int i=0; i<distCards.length; i++) {
			System.out.print("player " + i + ":  ");
			for(int j=0; j<distCards[i].length; j++) {
				System.out.print(distCards[i][j] + " ");
				
			}
			System.out.println("");
		}
		System.out.println("");
		in.next();
	}
	
	public static void testPrintAverageUtilityOfEachMove(String actionString[], double actionUtil[], double sum_impact_to_avg, int numSimulations, boolean isPercentage) {
		System.out.println("Comparing utility of different cards to play:");
		for(int a=0; a<actionString.length; a++) {
			
			System.out.print("Average util of playing the " + actionString[a] + ": " + actionUtil[a]/(1.0 * sum_impact_to_avg) );
			
			if(isPercentage) {
				System.out.println(" %");
			} else {
				System.out.println();
			}
		}
		System.out.println("Sum of impact of simulation: " + sum_impact_to_avg + " out of a possible " + numSimulations + " (" + String.format("%.2f", (100*sum_impact_to_avg)/(1.0*numSimulations)) + "%)");
	}
	
	
	
	//TODO: add MellowLetPartnerWinSignals to this when need arises
	//TODO: add VoidSignalsNoActiveMellows signals to the when need arises.
	
	//This filters out card distributions that are in conflict with the signals.
	// When there's lots of signals, this function filters out a lot of possible hand distributions.
	
	 public static boolean isCardDistRealistic2(DataModel dataModel, String distCards[][], SimulationPosibilitiesHandler simulationPosibilities, boolean debug) {
		 
		 for(int playerIndex=0; playerIndex<Constants.NUM_PLAYERS; playerIndex++) {
			 if(playerIndex == Constants.CURRENT_AGENT_INDEX) {
				 continue;
			 }
			 
			 for(int j=0; j<distCards[playerIndex].length; j++) {
				 if( ! simulationPosibilities.playerPos[playerIndex].contains(distCards[playerIndex][j])) {
					 
					 if(debug) {
						 System.err.println(dataModel.getPlayers()[playerIndex] + " should not have the " + distCards[playerIndex][j]);
					 }
					 
					 return false;
				 }
			 }
		 }
		 
		 return true;
	 }
	 

	 //Sanity check function to make sure everything is random:
	 public static int debugCount[][] = new int[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
		
	 public static void sanityCheckUpdateCardSuitFreq(String distCards[][]) {

			for(int j=0; j<distCards.length; j++) {
				for(int k=0; k<distCards[j].length; k++) {
					debugCount[j][CardStringFunctions.getIndexOfSuit(distCards[j][k])]++;
				}
			}
	 }
	 
	 public static void sanityCheckPrintCardSuitFreq() {

			System.err.println("Count Cards taken by player:");
			for(int k=0; k<debugCount[0].length; k++) {
				String out = "suit #" + k;
				System.err.print(out + "          ".substring(out.length()));
			}
			System.err.println();
			
			for(int j=0; j<debugCount.length; j++) {
				
				for(int k=0; k<debugCount[j].length; k++) {
					String out = debugCount[j][k] + "";
					System.err.print(out + "          ".substring(out.length()));
				}
				System.err.println();
			}
			System.err.println();
	 }
	 //END Sanity check function to make sure everything is random
	 
	 
	 public static void printOutcomeOfSimulation(DataModel dataModelTmpForPlayer0, String actionString[], int aIndex, StatsBetweenRounds endOfRoundPointDiffStats, StatsBetweenRounds endOfRoundStats) {
		System.err.println("---------");
		System.err.println("Results when player bid/played " + actionString[aIndex]);
		System.err.println(endOfRoundStats.getAIScore() + " (" + endOfRoundPointDiffStats.getAIScore() + ")");
		System.err.println(endOfRoundStats.getOpponentScore() + " (" + endOfRoundPointDiffStats.getOpponentScore() + ")");
		dataModelTmpForPlayer0.printHandsAndBidInStartOfRound();
		dataModelTmpForPlayer0.printCardsPlayedInRound();
		System.err.println("---------");
	 }
}

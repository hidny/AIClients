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
		
		testCaseParser.TEST_FOLDERS = new String[] {"MonteCarloTests"};
		//testCaseParser.TEST_FOLDERS = new String[] {"tmp"};
		//testCaseParser.TEST_FOLDERS = new String[] {"MonteCarloSignals"};
		
		testCaseParser.main(args);
		
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
	

	//Overnight slow
	//public static int NUM_SIMULATIONS_THOROUGH_AND_SLOW = 60000;
	
	//Do dishes and cook slow:
	//public static int NUM_SIMULATIONS_THOROUGH_AND_SLOW = 20000;

	//Watch TV slow:
	public static int NUM_SIMULATIONS_THOROUGH_AND_SLOW = 5000;
	
	//Think while it works slow:
	//public static int NUM_SIMULATIONS_THOROUGH_AND_SLOW = 2000;
	//public static int NUM_SIMULATIONS_THOROUGH_AND_SLOW = 1000;
	
	//Quick useless test: (Maybe test the Monte Carlo Main function)
	//public static int NUM_SIMULATIONS_THOROUGH_AND_SLOW = 100;
	
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
	
	
	//For testing:
	//public static Scanner in = new Scanner(System.in);
	
	//pre: it's the current dataModel's Turn...

	//Return number string for bid
	//Return card for action
	public static String runMonteCarloMethod(DataModel dataModel, int num_simulations) {
		
		boolean testWithSignals = false;
		//if(testCaseParser.TEST_FOLDERS[0].equals("MonteCarloSignals")) {
			System.err.println("Running with a basic signal filter!");
			testWithSignals = true;
		//}
		return runMonteCarloMethod(dataModel, num_simulations, true, testWithSignals);
	}

	public static String runMonteCarloMethod(DataModel dataModel, int num_simulations, boolean skipSimulationsBasedOnBids, boolean processSignals) {
		
		System.out.println("RUN SIMULATION");
		//in.nextLine();
		

		long numWaysOtherPlayersCouldHaveCards = dataModel.getCurrentNumWaysOtherPlayersCouldHaveCardsAndSetupDataModelForSimulations(processSignals);
		
		boolean isThorough = false;
		if(numWaysOtherPlayersCouldHaveCards < LIMIT_THOROUGH_SEARCH
				|| numWaysOtherPlayersCouldHaveCards <= 2 * num_simulations) {
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
		setupCardPossibilities(dataModel);
		System.out.println("END DEBUG getCardPossibilities:");
		
		
		String actionString[];
		int maxBidThatIsRealistic = -1;
		
		if(dataModel.stillInBiddingPhase()) {
			int bid = Integer.parseInt(new MellowBasicDecider(dataModel).getBidToMake());
			maxBidThatIsRealistic = (bid + 3);

			//TODO: Might not need as many simulations for bids...
			num_simulations /= 2;
			
			
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
		
		boolean voidArray[][] = dataModel.createVoidArray(processSignals);
		int curNumUnknownCardsPerSuit[] = CardStringFunctions.organizeCardsBySuit(unknownCards);
		int numSpacesAvailPerPlayer[] = dataModel.getNumUnknownSpaceAvailablePerPlayer();
		
		
		for(; i<num_simulations && numSkipped < maxSkipped; i++) {
			
			if( (i % Math.max(num_simulations/20, 100) == 0) 
					&& i > lastestPost) {
				System.err.println(i+ " out of " + num_simulations);
				lastestPost = i;
				
				if(i > 0) {
					System.err.println("Skipped " + numSkipped+ " out of " + (numSkipped + i));
					System.err.println("That's a " + String.format("%.2f", (100.0*numSkipped) /(1.0*(numSkipped + i))) + "% skip rate.");
					System.err.println();
					
				}
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

			distCards = dataModel.getPossibleDistributionOfUnknownCardsBasedOnIndex(
					randomDistributionNumber,
					numWaysOtherPlayersCouldHaveCards,
					processSignals,
					voidArray,
					unknownCards,
					curNumUnknownCardsPerSuit,
					numSpacesAvailPerPlayer);
			
			//Check if distCards happen to line up with what the signals are saying:
			if(processSignals) {
				
				//3rd arg is just for debug
				boolean realistic = isCardDistRealistic2(dataModel, distCards, numSkipped < 50);
				
				if( ! realistic) {
					
					if(isThorough == false) {
						i--;
					}
					
					numSkipped++;
					continue;
				}
			}
			//END check distribution of cards against signals.
			

			//For better results, check how realistic the distribution of cards is compared to what the original bid was and try
			//      to dampen the effect of unrealistic distributions of cards:
			double decisionImpact = getRelativeImpactOfSimulatedDistCards(dataModel, distCards);
			//double decisionImpact = 1.0;
			
			if(decisionImpact < 0.1) {
				
				//Make sure bids are made by players that know what they are doing:
				if( ! dataModel.stillInBiddingPhase()
						&& (
								//<= 8 is possible sometimes... Especially when I accidentally bid too low.
						(dataModel.getBidTotal() <= 7
						&& ! dataModel.someoneBidMellow())
						|| dataModel.getBidTotal() >= 15
						)
					){
					
					if(i % 1000 == 0) {
						System.err.println("WARNING: Bids don't make sense! Monte will not be skipping any hands because of bids.");
					}
				} else {
					
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
				
				//AHHH!!
				//TODO: For now I'm assuming action is a throw (not a bid) (This should change)
				if(dataModelTmpForPlayer0.stillInBiddingPhase()) {
					
					dataModelTmpForPlayer0.setBid(dataModel.getPlayers()[0], Integer.parseInt(actionString[a]));
				} else {
					dataModelTmpForPlayer0.updateDataModelWithPlayedCard(dataModel.getPlayers()[0], actionString[a]);
				}
				
				playersInSimulation = setupAIsForSimulation(dataModelTmpForPlayer0, distCards);

				playOutSimulationTilEndOfRound(dataModelTmpForPlayer0, playersInSimulation);
			
				
				//StatsBetweenRounds endOfRoundStats = getStatsAfterSimulatedRound(dataModelTmp);
				
				//Make monte carlos sims more readable:
				StatsBetweenRounds endOfRoundPointDiffStats = getPointDiffEndOfRound(dataModelTmpForPlayer0);
				
				//System.err.println("Score at end with card " + actionString[a] + ": " + endOfRoundPointDiffStats.getAIScore());
				
				//Get Util at the end of the simulated round:
					//Util is a function of scoreA, scoreB, isDealer
					//TODO: this is just getting the point difference after the round and isn't the most useful measure of how well we're doing.
					//A better strat would be an approx measure of the current player's winning chances... which is hard to calculate.
				actionUtil[a] += decisionImpact * getUtilOfStatsAtEndOfRoundSimulationBAD(endOfRoundPointDiffStats);
				
				
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
		
		
		testPrintAverageUtilityOfEachMove(actionString, actionUtil, sum_impact_to_avg, numSimulationsNotSkipped);

		System.out.print("END OF SIMULATION  PLAY: ");

		//Hack to avoid NaN answer after simulation:
		if(numSimulationsNotSkipped == 0
				|| numSimulationsNotSkipped * 1000 < num_simulations) {
			
			if(skipSimulationsBasedOnBids) {
				skipSimulationsBasedOnBids = false;
				System.err.println("RETRY without skipping any simulations because of bids:");
				return runMonteCarloMethod(dataModel, num_simulations, skipSimulationsBasedOnBids, processSignals);
				
			} else if(processSignals) {
				processSignals = false;
				System.err.println("RETRY without processing any signals:");
				return runMonteCarloMethod(dataModel, num_simulations, skipSimulationsBasedOnBids, processSignals);
				
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
	public static StatsBetweenRounds getStatsAfterSdimulatedRound(DataModel dataModelTmpForPlayer0) {
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
	
	public static void testPrintAverageUtilityOfEachMove(String actionString[], double actionUtil[], double sum_impact_to_avg, int numSimulations) {
		System.out.println("Comparing utility of different cards to play:");
		for(int a=0; a<actionString.length; a++) {
			System.out.println("Average util (point diff) of playing the " + actionString[a] + ": " + actionUtil[a]/(1.0 * sum_impact_to_avg) );
		}
		System.out.println("Sum of impact of simulation: " + sum_impact_to_avg + " out of a possible " + numSimulations + " (" + String.format("%.2f", (100*sum_impact_to_avg)/(1.0*numSimulations)) + "%)");
	}
	
	
	//TODO: this function needs a lot of work...
	//TODO: add non-mellow bidder signals to this when the need arises:
	
	//TODO: Make another version of monte carlo 
	//that doesn't need to skip 99% of hands it generates...
	//You just need to tweak the design to something less silly.
		//See SimulationSetupWithSignalsAndMemBoost
	 public static boolean isCardDistRealistic2(DataModel dataModel, String distCards[][], boolean debug) {
		 
		 for(int playerIndex=0; playerIndex<Constants.NUM_PLAYERS; playerIndex++) {
			 if(playerIndex == Constants.CURRENT_AGENT_INDEX) {
				 continue;
			 }
			 
			 for(int j=0; j<distCards[playerIndex].length; j++) {
				 if( ! playerPos[playerIndex].contains(distCards[playerIndex][j])) {
					 
					 if(debug) {
						 System.err.println(dataModel.getPlayers()[playerIndex] + " should not have the " + distCards[playerIndex][j]);
					 }
					 
					 return false;
				 }
			 }
		 }
		 
		 return true;
	 }
	 
	 //TODO: make isCardDistRealistic just 
	 // refer to playerPos and streamline this!
	 //For now, I'm just going to run monte and make sure I didn't mess it up.
	 
	 public static HashSet<String> playerPos[] = new HashSet[Constants.NUM_PLAYERS];


	 public static void setupCardPossibilities(DataModel dataModel) {
		 
		 playerPos = new HashSet[Constants.NUM_PLAYERS];
		 
		 for(int playerIndex = 0; playerIndex<Constants.NUM_PLAYERS; playerIndex++) {
			 
			 playerPos[playerIndex] = new HashSet<String>();
					 
			 if(playerIndex == Constants.CURRENT_AGENT_INDEX) {
				 continue;
			 }
			 
			 
			 for(int suitIndex=0; suitIndex<Constants.NUM_SUITS; suitIndex++) {
				 
				 if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(playerIndex, suitIndex)) {
					 continue;
				 }
				 
				 boolean mellowBidderSignalledNo[] = new boolean[Constants.NUM_RANKS];
				 boolean nonMellowBidderSignalledNo[] = new boolean[Constants.NUM_RANKS];
				 
				 //Mellow bidder signals:
				 if(dataModel.playerMadeABidInRound(playerIndex)
						 && dataModel.getBid(playerIndex) == 0) {

					 for(int rankIndex = 0; rankIndex<Constants.NUM_RANKS; rankIndex++) {
						 
						
						 if( ! isSignalledCardGoodForMellowBidder(dataModel, playerIndex, DataModel.getCardString(rankIndex, suitIndex), false)) {
							 mellowBidderSignalledNo[rankIndex] = true;
						 }
							 
					 }
				 }
				 
				 if(dataModel.playerMadeABidInRound(playerIndex)
						 && (dataModel.getBid(playerIndex) > 0
						|| dataModel.burntMellow(playerIndex))) {
					
					int max = dataModel.signalHandler.getMaxCardRankSignal(playerIndex, suitIndex);
				 	int min = dataModel.signalHandler.getMinCardRankSignal(playerIndex, suitIndex);
				 	

				 	for(int rankIndex = DataModel.RANK_TWO; rankIndex< Math.max(min, DataModel.RANK_TWO) - 1; rankIndex++) {
				 		nonMellowBidderSignalledNo[rankIndex] = true;
				 	}
				 	
				 	
				 	for(int rankIndex = Math.min(max, DataModel.ACE) + 1; rankIndex<= DataModel.ACE; rankIndex++) {
				 		nonMellowBidderSignalledNo[rankIndex] = true;
				 	}
				 }
				 
				 for(int rankIndex = 0; rankIndex<Constants.NUM_RANKS; rankIndex++) {

					 String card = DataModel.getCardString(rankIndex, suitIndex);
					 
					 if(mellowBidderSignalledNo[rankIndex] == false
						&& nonMellowBidderSignalledNo[rankIndex] == false
						&& dataModel.isCardPlayedInRound(card) == false
						&& dataModel.hasCard(card) == false) {
						 playerPos[playerIndex].add(card);
					 }
				 }
				 
			 }
		 }
		 
		 for(int i=0; i<Constants.NUM_PLAYERS; i++) {
			 if(i == Constants.CURRENT_AGENT_INDEX) {
				 continue;
			 }
			System.out.println("Possible cards for " + dataModel.getPlayers()[i] + ": ");
			
			Object array[] = playerPos[i].toArray();
			
			String array2[] = new String[array.length];
			
			for(int j=0; j<array2.length; j++) {
				array2[j] = array[j].toString();
			}
			
			String sortedArray[] = CardStringFunctions.sort(array2);
			CardStringFunctions.printCards(sortedArray);
		 }
		 
		 setupPossibilitySets();
	 }
	 
	 public static int NUM_POSSIBILITIES = 3;
	 // 0 = no
	 // 1 = yes
	 // 2 = doesn't matter.
	 
	 //First index is for current player, but current player always knows what's in his hand, so whatever!
	 //Second index is for LHS player
	 //Third index is for PARTNER player
	 //Fourth index is for RHS player
	 //Last index is for an array of actual cards:
	 public static String otherPlayerPosSet[][][][][] = new String[1][NUM_POSSIBILITIES][NUM_POSSIBILITIES][NUM_POSSIBILITIES][];
	 
	 public static int NO_INDEX = 0;
	 public static int YES_INDEX = 1;
	 public static int ANY_INDEX = 2;
	 

	 public static HashSet<String> playerNOTPossibleAndNotCurPlayer[] = new HashSet[Constants.NUM_PLAYERS];
	 
	 //pre: HashSet<String> playerPos is defined
	 //post: 
	 public static void setupPossibilitySets() {
		 
		 //HashSet<String> playerPos[] 
		 
		 HashSet<String> allCardsInPlayNoCurPlayer = new HashSet<String>();
		 
		 for(int i=0; i<playerPos.length; i++) {
			 Iterator<String> it = playerPos[i].iterator();
			 
			 while(it.hasNext()) {
				 
				 String tmpCard = it.next();
				 
				 if( ! allCardsInPlayNoCurPlayer.contains(tmpCard) ) {
					 allCardsInPlayNoCurPlayer.add(tmpCard);
				 }
			 }
			 //TODO
		 }
		 
		 for(int i=0; i<playerNOTPossibleAndNotCurPlayer.length; i++) {
			 if(i == 0) {
				 playerNOTPossibleAndNotCurPlayer[0] = allCardsInPlayNoCurPlayer;
			 }
			 
			 playerNOTPossibleAndNotCurPlayer[i] = new HashSet<String>();
			 
			 Iterator<String> it = allCardsInPlayNoCurPlayer.iterator();
			 
			 while(it.hasNext()) {
				 
				 String tmpCard = it.next();
				 
				 if( ! playerPos[i].contains(tmpCard) ) {
					 playerNOTPossibleAndNotCurPlayer[i].add(tmpCard);
				 }
			 }
		 }
		 
		 
		 for(int j=0; j<otherPlayerPosSet[0].length; j++) {
			 for(int k=0; k<otherPlayerPosSet[0][0].length; k++) {
				 for(int m=0; m<otherPlayerPosSet[0][0][0].length; m++) {
					 
					 HashSet<String> ret = allCardsInPlayNoCurPlayer;
					 
					 if(j == NO_INDEX) {
						 ret = Intersection(ret, playerNOTPossibleAndNotCurPlayer[1]);
						 
					 } else if(j == YES_INDEX) {
						 ret = Intersection(ret, playerPos[1]);
						 
					 }
					 

					 if(k == NO_INDEX) {
						 ret = Intersection(ret, playerNOTPossibleAndNotCurPlayer[2]);
						 
					 } else if(k == YES_INDEX) {
						 ret = Intersection(ret, playerPos[2]);
						 
					 }

					 if(m == NO_INDEX) {
						 ret = Intersection(ret, playerNOTPossibleAndNotCurPlayer[3]);
						 
					 } else if(m == YES_INDEX) {
						 ret = Intersection(ret, playerPos[3]);
						 
					 }
					 
					 //otherPlayerPosSet[0][j][k][m] = ret;
					
					Object array[] = ret.toArray();
					
					otherPlayerPosSet[0][j][k][m] = new String[array.length];
					
					for(int p=0; p<otherPlayerPosSet[0][j][k][m].length; p++) {
						otherPlayerPosSet[0][j][k][m][p] = array[p].toString();
					}
					
					//Might as well sort the cards for readability:
					otherPlayerPosSet[0][j][k][m] = CardStringFunctions.sort(otherPlayerPosSet[0][j][k][m]);

					System.out.println("(0 " + "," + j +", " + k + ", " + m + "):");
					if(otherPlayerPosSet[0][j][k][m].length > 0) {
						CardStringFunctions.printCards(otherPlayerPosSet[0][j][k][m]);
					} else {
						System.out.println("(empty)");
					}
					
				 }
			}
		 }
	 }
	 
	 public static HashSet<String> Intersection(HashSet<String> a, HashSet<String> b) {
		 HashSet<String> ret = new HashSet<String>();
		 
		 Iterator<String> it = a.iterator();
		 
		 while(it.hasNext()) {
			 
			 String tmpCard = it.next();
			 
			 if( b.contains(tmpCard) ) {
				 ret.add(tmpCard);
			 }
		 }
		 
		 
		 return ret;
	 }
	 
	 
	 
	 
	 public static boolean isSignalledCardGoodForMellowBidder(DataModel dataModel, int playerIndex, String card, boolean debug) {
		 if(dataModel.isCardPlayedInRound(card) == false) {
			 
			 int suitIndex = CardStringFunctions.getIndexOfSuit(card);
			 int rankIndex = DataModel.getRankIndex(card);
			 
			 if( dataModel.getCardsCurrentlyHeldByPlayers()[playerIndex]
					 [suitIndex]
					 [rankIndex]
					== VoidSignalsNoActiveMellows.MELLOW_PLAYER_SIGNALED_NO) {
				 
				 if(debug) {
					 System.err.println("NOPE! Mellow bidder (" + dataModel.getPlayers()[playerIndex] + ") doesn't have the " + card + ".");
				 }
				 return false;
			 }
			 
		 }
		 
		 return true;
	 }
	 
	 public static boolean isCardSignalledGoodForNonMellowBidder(DataModel dataModel, String card, int playerIndex, int minRank, int maxRank, boolean debug) {
		 
		 
		 if(DataModel.getRankIndex(card) < minRank) {

			 if(debug) {
				 System.err.println("NOPE! Mellow player (" + dataModel.getPlayers()[playerIndex] + ") should have higher card than " + card + ".");
			 }
			 
			 return false;
		 } else if(DataModel.getRankIndex(card) > maxRank) {
			
			 if(debug) {
				 System.err.println("NOPE! Mellow player (" + dataModel.getPlayers()[playerIndex] + ") should have lower card than " + card + ".");
			 }
			 
			 return false;
		 }
		 

		 return true;
	 }
	 
}

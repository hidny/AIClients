package mellow.ai.simulation;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import mellow.Constants;
import mellow.ai.aiDecider.MellowAIDeciderFactory;
import mellow.ai.aiDecider.MellowAIDeciderInterface;
import mellow.ai.aiDecider.MellowBasicDecider;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.cardDataModels.StatsBetweenRounds;

//TODO: maybe compliment this with a decision tree?
//https://softwareengineering.stackexchange.com/questions/157324/decision-trees-vs-neural-networks

//Vote for decision tree over random forests:
//https://stats.stackexchange.com/questions/285834/difference-between-random-forests-and-decision-tree

public class MonteCarloMain {
	//Guess at reason number of simulations to try
	
	//TODO: make # of simulation configurable...

	//TODO: (There numbers might be way too high... 10 simulations seems to have honed in on right answer)	
	//Slow setting:
	//public static int LIMIT_THOROUGH_SEARCH = 2000;
	//public static int NUM_SIMULATIONS_SAMPLE = 1000;
	
	//Slow but good (100 limit is too prone to random swings and there's no clear signal)
	public static int LIMIT_THOROUGH_SEARCH = 2000;

	public static int NUM_SIMULATIONS_DEFAULT = 200;
	//public static int NUM_SIMULATIONS_THOROUGH_AND_SLOW = 20000;
	public static int NUM_SIMULATIONS_THOROUGH_AND_SLOW = 5000;
	
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
		
		System.out.println("RUN SIMULATION");
		//in.nextLine();
		

		long numWaysOtherPlayersCouldHaveCards = dataModel.getCurrentNumWaysOtherPlayersCouldHaveCards();
		
		boolean isThorough = false;
		if(numWaysOtherPlayersCouldHaveCards < LIMIT_THOROUGH_SEARCH
				|| numWaysOtherPlayersCouldHaveCards <= 2 * num_simulations) {
			isThorough = true;
		}
		
		if(isThorough) {
			num_simulations = (int)numWaysOtherPlayersCouldHaveCards;
		}
		
		//TODO: Make simulation handle bid simulations! (For now, I'm just worried about starting the monte carlo method from after the bids are made...)
		//TODO: When handling bids: please dismiss unreasonable bids quickly!
		
		//TODO: test bid:
		//Create mapping from index to action (card or bid) 
		String actionString[] = dataModel.getListOfPossibleActions();
		double actionUtil[] = new double[actionString.length];
		for(int i=0; i<actionUtil.length; i++) {
			actionUtil[i] = 0.0;
		}
		
		//Reduce number of print statements called:
		PrintStream originalStream = System.out;
		if(num_simulations > MAX_NUM_SIMULATIONS_WHILE_DEBUG_PRINT) {
			System.setOut(dummyStream);
		}
		
		
		double sum_impact_to_avg = 0.0;
		
		for(int i=0; i<num_simulations; i++) {
			if(i % 100 == 0) {
				System.err.println(i+ " out of " + num_simulations);
			}
			//Distribute unknown cards for simulation:
			String distCards[][];
			if(isThorough) {
				distCards = dataModel.getPossibleDistributionOfUnknownCardsBasedOnIndex(i);
			} else {
				distCards = dataModel.getPossibleDistributionOfUnknownCardsBasedOnIndex(
						SimulationSetup.getRandNumberFrom0ToN(numWaysOtherPlayersCouldHaveCards));
			}
			
			//For better results, check how realistic the distribution of cards is compared to what the original bid was and try
			//      to dampen the effect of unrealistic distributions of cards:
			double decisionImpact = getRelativeImpactOfSimulatedDistCards(dataModel, distCards);
			//double decisionImpact = 1.0;
			
			/*if(isThorough == false && decisionImpact < 0.001 && sum_impact_to_avg > 0.099) {
				i--;
				continue;
			}*/

			//TODO: check how realistic the distribution of cards is compared to what has been played
			//If it's unrealistic, make the simulation count for less.
			
			sum_impact_to_avg +=  decisionImpact;
			

			DataModel dataModelTmpForPlayer0;
			MellowBasicDecider playersInSimulation[];
			
			
			for(int a=0; a<actionString.length; a++) {

				System.out.println("Possible action: " + actionString[a]);

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

		
		testPrintAverageUtilityOfEachMove(actionString, actionUtil, sum_impact_to_avg, num_simulations);

		//in.next();
		System.out.print("END OF SIMULATION  PLAY: ");
		
		return actionString[getMaxIndex(actionUtil)];
	}
	
	static PrintStream dummyStream = new PrintStream(new OutputStream(){
	    public void write(int b) {
	        // NO-OP
	    }
	});
	
	public static double getRelativeImpactOfSimulatedDistCards(DataModel dataModel, String distCards[][]) {
		DataModel dataModelTmpForPlayer0 = dataModel.createHardCopy();
		
		String players[] = new String[Constants.NUM_PLAYERS];
		players[0] = "Hero";
		players[1] = "Villain1";
		players[2] = "Partner";
		players[3] = "Villain2";
		
		
		double impact = 1.0;
		for(int playerI=0; playerI<Constants.NUM_PLAYERS; playerI++) {
			if(playerI == Constants.CURRENT_AGENT_INDEX) {
				//We know the current player's bid is what's expected, so skip that check:
				continue;
			}
			String hand[] = dataModelTmpForPlayer0.getGuessAtOriginalCardsHeld(playerI, distCards[playerI]);
			
			MellowBasicDecider tempDecider = new MellowBasicDecider();
			tempDecider.resetStateForNewRound();
			
			tempDecider.setNameOfPlayers(players);
			tempDecider.setCardsForNewRound(hand);
			
			//Set dealer to be the player of the right of the Hero:
			tempDecider.setDealer("Villain2");

			int expectedResponse = dataModelTmpForPlayer0.getBid(playerI);
			int response = 0;
			try {
				response = Integer.parseInt(tempDecider.getBidToMake());
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("ERROR: could not parse bid in monte carlo simulation");
				System.exit(1);
			}
			
			//For now, only tolarate an off-by-one error:
			
			if(Math.abs(expectedResponse - response) >= 2) {
				impact /= 1.5;

			} else if(Math.abs(expectedResponse - response) >= 1) {
				impact /= 1.2;
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
			}
			
			
				
			String card = playerInSimulation[actionIndex].getCardToPlay();
			for(int k=0; k<Constants.NUM_PLAYERS; k++) {
				playerInSimulation[k].receiveCardPlayed(players[actionIndex], card);
			}
				

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
	
}

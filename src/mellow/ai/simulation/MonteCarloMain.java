package mellow.ai.simulation;

import java.util.Scanner;

import mellow.Constants;
import mellow.ai.aiDecider.MellowBasicDecider;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.cardDataModels.StatsBetweenRounds;

public class MonteCarloMain {

	//Guess at reason number of simulations to try
	//TODO: (There numbers might be way too high... 10 simulations seems to have honed in on right answer)
	//public static int LIMIT_THOROUGH_SEARCH = 2000;
	//public static int NUM_SIMULATIONS_SAMPLE = 1000;
	
	//TODO: set limits low for testing/debugging:
	public static int LIMIT_THOROUGH_SEARCH = 20;
	public static int NUM_SIMULATIONS_SAMPLE = 10;
	//END TODO

	//For testing:
	public static Scanner in = new Scanner(System.in);
	
	//pre: it's the current dataModel's Turn...

	//Return number string for bid
	//Return card for action
	public static String runMonteCarloMethod(DataModel dataModel) {
		
		System.out.println("RUN SIMULATION");
		in.nextLine();
		
		long numWaysOtherPlayersCouldHaveCards = dataModel.getCurrentNumWaysOtherPlayersCouldHaveCards();
		
		boolean isThorough = false;
		if(numWaysOtherPlayersCouldHaveCards < LIMIT_THOROUGH_SEARCH) {
			isThorough = true;
		}
		
		int num_simulations = NUM_SIMULATIONS_SAMPLE;
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
		
		for(int i=0; i<num_simulations; i++) {
			
			//Distribute unknown cards for simulation:
			String distCards[][];
			if(isThorough) {
				distCards = dataModel.getPossibleDistributionOfUnknownCardsBasedOnIndex(i);
			} else {
				distCards = dataModel.getPossibleDistributionOfUnknownCardsBasedOnIndex(
						SimulationSetup.getRandNumberFrom0ToN(numWaysOtherPlayersCouldHaveCards));
			}
			
			//TODO: For better results, check how realistic the distribution of cards is compared to what the original bid was and try
			//      to dampen the effect of unrealistic distributions of cards.

			//TODO2: check how realistic the distribution of cards is compared to what has been played
			//If it's unrealistic, make the simulation count for less.
			
			MellowBasicDecider playersInSimulation[];
			DataModel dataModelTmp;
			
			for(int a=0; a<actionString.length; a++) {

				System.out.println("Possible action: " + actionString[a]);

				//Create a tmp data model for current player to keep track of everything:
				dataModelTmp = dataModel.createHardCopy();
				
				//Tell the data model that it's in the next level of a simulation.
				dataModelTmp.incrementSimulationLevel();
				
				//TODO: For now I'm assuming action is a throw (not a bid) (This should change)
				dataModelTmp.updateDataModelWithPlayedCard(dataModel.getPlayers()[0], actionString[a]);
							
				playersInSimulation = setupAIsForSimulation(dataModelTmp, distCards);

				playOutSimulationTilEndOfRound(dataModelTmp, playersInSimulation);
			
				StatsBetweenRounds endOfRoundStats = getStatsAfterSimulatedRound(dataModelTmp);
				
				//Get Util at the end of the simulated round:
					//Util is a function of scoreA, scoreB, isDealer
					//TODO: this is just getting the point difference after the round and isn't the most useful measure of how well we're doing.
					//A better strat would be an approx measure of the current player's winning chances... which is hard to calculate.
				actionUtil[a] += getUtilOfStatsAtEndOfRoundSimulationBAD(endOfRoundStats);
				
				System.out.println("Util (point diff) when the " + actionString[a] + ": " + getUtilOfStatsAtEndOfRoundSimulationBAD(endOfRoundStats));
				in.next();
			}
			
			//TESTING DISTRIBUTION:
			System.out.println("Unknown card distribution for simulation #" + i);
			
			for(int i1=0; i1<distCards.length; i1++) {
				System.out.print("player " + i1 + ":  ");
				for(int j=0; j<distCards[i1].length; j++) {
					System.out.print(distCards[i1][j] + " ");
					
				}
				System.out.println("");
			}
			System.out.println("");
			//in.next();
			//END TESTING DISTRIBUTION:

		}
		
		//TESTING:
		System.out.println("Comparing utility of different cards to play:");
		for(int a=0; a<actionString.length; a++) {
			System.out.println("Average util (point diff) of playing the " + actionString[a] + ": " + actionUtil[a]/(1.0 * num_simulations) );
		}
		in.next();
		System.out.println("END OF SIMULATION");
		//END TESTING
		
		return actionString[getMaxIndex(actionUtil)];
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
	
	public static MellowBasicDecider[] setupAIsForSimulation(DataModel dataModelTmp, String distCards[][]) {
		
		MellowBasicDecider playerInSimulation[] = new MellowBasicDecider[Constants.NUM_PLAYERS];
		
		//For each player: create a HARD coded data model for their perspective (Maybe their own MellowBasicDecider):
		for(int j=0; j<Constants.NUM_PLAYERS; j++) {
			if(j==0) {
				playerInSimulation[j] = new MellowBasicDecider(dataModelTmp);
			} else {
				playerInSimulation[j] = new MellowBasicDecider(dataModelTmp.getDataModelFromPerspectiveOfPlayerI(j, distCards));
			}
		}
		
		return playerInSimulation;
	}
	
	public static DataModel playOutSimulationTilEndOfRound(DataModel dataModelTmp, MellowBasicDecider playerInSimulation[]) {
		
		int numCardsPlayed = dataModelTmp.getCardsPlayedThisRound();
		String players[] = dataModelTmp.getPlayers();
		
		//Get whose turn it is:
		int actionIndex = dataModelTmp.getCurrentActionIndex();
		
		for(int j=numCardsPlayed; j<Constants.NUM_CARDS; j++) {
			
			//If it's the beginning of a new round:
			if(j % Constants.NUM_PLAYERS == 0) {
				//figure out who leads
				actionIndex = dataModelTmp.getCurrentActionIndex();
			}
			
			String card = playerInSimulation[actionIndex].getCardToPlay();
			
			for(int k=0; k<Constants.NUM_PLAYERS; k++) {
				playerInSimulation[k].receiveCardPlayed(players[actionIndex], card);
			}
			actionIndex = (actionIndex + 1) % Constants.NUM_PLAYERS;
		
		}

		if(dataModelTmp.getCardsPlayedThisRound() != Constants.NUM_CARDS ) {
			System.err.println("ERROR: (in simulation) simulation hasn't reached the end of the round");
			System.exit(1);
		}
		
		return dataModelTmp;
	}
	
	//Get stats at the end of the round:
	//(i.e: Get scores and dealer index at the end of the round)
	public static StatsBetweenRounds getStatsAfterSimulatedRound(DataModel dataModelTmp) {
		int scoreUsAtStartOfRound = dataModelTmp.getAIScore();
		int scoreThemAtStartOfRound = dataModelTmp.getOpponentScore();
		
		int curScoreUs = scoreUsAtStartOfRound;
		int curScoreThem = scoreThemAtStartOfRound;
		
		int numBurnsUS = 0;
		int numBurnsThem = 0;
		//Handle mellows:
		for(int i=0; i<Constants.NUM_PLAYERS; i++) {
			if(dataModelTmp.burntMellow(i)) {
				if(i%2 == 0) {
					curScoreUs -= 100;
					numBurnsUS++;
				} else {
					curScoreThem -= 100;
					numBurnsThem++;
				}
			}
		}

		if(numBurnsUS < 2){
			curScoreUs += handleNormalBidScoreDiff(dataModelTmp, true);
		}
		
		if(numBurnsThem < 2) {
			curScoreThem += handleNormalBidScoreDiff(dataModelTmp, false);
		}
		
		StatsBetweenRounds ret = new StatsBetweenRounds();
		ret.setScores(curScoreUs, curScoreThem);
		
		int nextDealerIndex = (dataModelTmp.getDealerIndexAtStartOfRound() + 1) % Constants.NUM_PLAYERS;
		ret.setDealerIndexAtStartOfRound(nextDealerIndex);
		
		return ret;
	}
	
	public static int handleNormalBidScoreDiff(DataModel dataModelTmp, boolean teamIsUs) {
		int adjustmentIndex = 0;
		
		if(teamIsUs == false) {
			adjustmentIndex = 1;
		}
		
		int scoreAdjustment = 0;
		
		int numBids = dataModelTmp.getBid(0 + adjustmentIndex) + dataModelTmp.getBid(2 + adjustmentIndex);
		int numTricks = dataModelTmp.getTrick(0 + adjustmentIndex) + dataModelTmp.getTrick(2 + adjustmentIndex);
		
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
	
}

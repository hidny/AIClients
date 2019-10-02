package mellow.ai.simulation;

import java.util.Scanner;

import mellow.Constants;
import mellow.ai.aiDecider.MellowBasicDecider;
import mellow.ai.cardDataModels.DataModel;

public class MonteCarloMain {

	
	public static int LIMIT_THOROUGH_SEARCH = 2000;
	
	public static int NUM_SIMULATIONS_SAMPLE = 1000;
	
	//For testing:
	public static Scanner in = new Scanner(System.in);
	
	//pre: it's the current dataModel's Turn...

	//Return number string for bid
	//Return card for action
	public static String runMonteCarloMethod(DataModel dataModel) {
		
		System.out.println("RUN SIMULATION");
		//in.nextLine();
		
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
			actionUtil[i] = Double.MIN_VALUE;
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
			
			//TODO: check how realistic the distribution of cards is compared to what the original bid was
			//TODO2: check how realistic the distribution of cards is compared to what has been played
			//If it's unrealistic, make the simulation count for less.
			
			MellowBasicDecider playersInSimulation[];
			DataModel dataModelTmp;
			
			for(int a=0; a<actionString.length; a++) {

				System.out.println("Possible action: " + actionString[a]);

				//Create a tmp data model for current player to keep track of everything:
				dataModelTmp = dataModel.createHardCopy();
				
				//TODO: For now I'm assuming action is a throw (not a bid) (This should change)
				dataModelTmp.updateDataModelWithPlayedCard(dataModel.getPlayers()[0], actionString[a]);
							
				playersInSimulation = setupAIsForSimulation(dataModelTmp, distCards);

				playOutSimulationTilEndOfRound(dataModelTmp, playersInSimulation);
			
				//(5)Util is a function of scoreA, scoreB, isDealer
				//(Maybe start with making it a point diff... then improve on it)
				//Best would be an approx measure of the current player's winning chances.
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
			
			in.next();
		}
		
		
		return actionString[getMaxIndex(actionUtil)];
	}
	
	public static int getMaxIndex(double array[]) {

		int bestIndex = 0;
		for(int i=0; i<array.length; i++) {
			if(array[i] > array[bestIndex]) {
				i = bestIndex;
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
	
}

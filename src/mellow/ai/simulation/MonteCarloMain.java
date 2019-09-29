package mellow.ai.simulation;

import java.util.Scanner;

import mellow.ai.cardDataModels.impl.BooleanTableDataModel;

public class MonteCarloMain {

	
	public static int LIMIT_THOROUGH_SEARCH = 2000;
	
	public static int NUM_SIMULATIONS_SAMPLE = 1000;
	
	
	
	//pre: it's the current dataModel's Turn...

	//Return number string for bid
	//Return card for action
	public static String runSimulation(BooleanTableDataModel dataModel) {
		
		//For testing:
		Scanner in = new Scanner(System.in);
		
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
		
		//TODO: (0)create mapping from index to action (card or bid)
		int numActions= 0;
		
		double actionUtil[] = new double[numActions];
		String actionString[] = new String[numActions];
		
		for(int i=0; i<num_simulations; i++) {
			
			//(1)distribute unknown cards for simulation:
			String distCards[][];
			if(isThorough) {
				distCards = dataModel.getPossibleDistributionOfUnknownCardsBasedOnIndex(i);
			} else {
				distCards = dataModel.getPossibleDistributionOfUnknownCardsBasedOnIndex(
						SimulationSetup.getRandNumberFrom0ToN(numWaysOtherPlayersCouldHaveCards));
			}
			
			//TODO: For each action A player could take:
				//(2)For each player: create a HARD coded data model for their perspective
				//(3)Play Action A for each player perspective
				//(4)Play out the rest of the round and return util.
			
			
				//(5)Util is a function of scoreA, scoreB, isDealer
				//(Maybe start with making it a point diff... then improve on it)
				//Best would be an approx measure of the current player's winning chances.
			//END INNER FOR
			
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
		
		
		
		//return actionString[getMaxIndex(actionUtil)];
		return null;
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
	
	
}

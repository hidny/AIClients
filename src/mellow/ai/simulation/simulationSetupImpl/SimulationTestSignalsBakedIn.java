package mellow.ai.simulation.simulationSetupImpl;

import java.util.HashSet;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.simulation.SimulationPosibilitiesHandler;
import mellow.cardUtils.CardStringFunctions;

public class SimulationTestSignalsBakedIn {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		reproduceFranelNumbersTest();
		
		goThruEveryComboTest();
	}
	
	public static void reproduceFranelNumbersTest() {
		//Franel numbers check:
		//https://oeis.org/
		//A000172
		long expected[] = new long[] {1, 2, 10, 56, 346, 2252, 15184, 104960, 739162, 5280932, 38165260L, 278415920L, 2046924400L, 15148345760L};
		
		
		HashSet<String> playerPos[] = new HashSet[Constants.NUM_PLAYERS];
		
		for(int n=0; n<=Constants.NUM_STARTING_CARDS_IN_HAND; n++) {
			for(int playerIndex=0; playerIndex<Constants.NUM_PLAYERS; playerIndex++) {
				
				playerPos[playerIndex] = new HashSet<String>();
				
				if(playerIndex == Constants.CURRENT_AGENT_INDEX) {
					continue;
				}
				
				for(int suitIndex=0; suitIndex < Constants.NUM_SUITS; suitIndex++) {
					
					if(suitIndex != 0 && suitIndex != playerIndex) {
						
						for(int k=0; k<n; k++) {
							playerPos[playerIndex].add(DataModel.getCardString(k, suitIndex));
						}
						
					}
				}
			}
			SimulationPosibilitiesHandler simPossibilities = new SimulationPosibilitiesHandler(playerPos);
			
			int numSpacesAvailPerPlayer[] = new int[Constants.NUM_PLAYERS];
			
			for(int i=0; i<numSpacesAvailPerPlayer.length; i++) {
				
				if(i == Constants.CURRENT_AGENT_INDEX) {
					continue;
				}
				numSpacesAvailPerPlayer[i] = n;
			}
			
			SimulationSetupWithSignalsAndMemBoost simSetup = new SimulationSetupWithSignalsAndMemBoost(simPossibilities, numSpacesAvailPerPlayer);
			
			long ret = simSetup.initSimulationSetupAndRetNumWaysOtherPlayersCouldHaveCards();
			
			//System.out.println("Ret: " + ret);
			
			if(ret != expected[n]) {
				System.out.println("ERROR: result doesn't match franel number sequence at n = " + n);
			}
			
		}
				
	}
	
	public static void goThruEveryComboTest() {
		
		System.out.println("Test: go through Every Combo and make sure they are all different:");
		HashSet<String> playerPos[]  = setupBalancePlayerPos();
		
		//Create imbalances:
		playerPos[1].add("AB4");
		playerPos[2].add("AB4");

		playerPos[2].add("BC4");
		playerPos[3].add("BC4");
		playerPos[2].add("BC5");
		playerPos[3].add("BC5");
		
		SimulationPosibilitiesHandler simPossibilities = new SimulationPosibilitiesHandler(playerPos);

		int numSpacesAvailPerPlayer[] = new int[Constants.NUM_PLAYERS];
		
		numSpacesAvailPerPlayer[1] = 7;
		numSpacesAvailPerPlayer[2] = 8;
		numSpacesAvailPerPlayer[3] = 9;
		
		
		SimulationSetupWithSignalsAndMemBoost simSetup = new SimulationSetupWithSignalsAndMemBoost(simPossibilities, numSpacesAvailPerPlayer);
		
		long ret = simSetup.initSimulationSetupAndRetNumWaysOtherPlayersCouldHaveCards();
		
		HashSet <String> set = new HashSet<String>();
		
		for(int i=0; i<ret; i++) {
			String dist[][] = simSetup.getPossibleDistributionOfUnknownCardsBasedOnIndex(i);
			
			String tmpDistString = getDistString(dist);
			
			if(set.contains(tmpDistString)) {
				System.out.println("Error: got repeat distribution!");
				System.out.println(tmpDistString);
			}
			set.add(tmpDistString);
			
			System.out.println(tmpDistString);
		}
		
		System.out.println("ALL different!");
	}
	
	public static String getDistString(String dist[][]) {
		
		String ret = "";
		
		for(int i=0; i<dist.length; i++) {
			dist[i] = SimulationPosibilitiesHandler.sortTestHand(dist[i]);
		}
		
		for(int i=0; i<dist.length; i++) {
			for(int j=0; j<dist[i].length; j++) {
				ret += dist[i][j] + " ";
			}
			ret += "| ";
		}
		
		return ret;
	}
	
	
	
	public static HashSet<String>[] setupBalancePlayerPos() {
		HashSet<String> playerPos[] = new HashSet[Constants.NUM_PLAYERS];
		
		for(int i=0; i<playerPos.length; i++) {
			playerPos[i] = new HashSet<String>();
		}
		
		for(int j=1; j<=3; j++) {
		
			playerPos[1].add("A" + j);
			
			playerPos[2].add("B" + j);
	
			playerPos[3].add("C" + j);
			
			playerPos[1].add("AB" + j);
			playerPos[2].add("AB" + j);
			
			playerPos[1].add("AC" + j);
			playerPos[3].add("AC" + j);
	
			playerPos[2].add("BC" + j);
			playerPos[3].add("BC" + j);
			
			playerPos[1].add("ABC" + j);
			playerPos[2].add("ABC" + j);
			playerPos[3].add("ABC" + j);
		}
		
		return playerPos;
	}

}

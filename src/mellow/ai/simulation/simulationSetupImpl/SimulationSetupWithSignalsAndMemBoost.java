package mellow.ai.simulation.simulationSetupImpl;


import java.util.ArrayList;

import mellow.ai.cardDataModels.DataModel;
import mellow.ai.simulation.SimulationPosibilitiesHandler;
import mellow.ai.simulation.SimulationSetupInterface;
import mellow.ai.simulation.objects.PlayerACombinationInfo;

public class SimulationSetupWithSignalsAndMemBoost implements SimulationSetupInterface {

	
	

	private int numSpacesAvailPerPlayer[];
	SimulationPosibilitiesHandler simPossibilities;
	
	
	private PlayerACombinationInfo playerALookup[];
	
	public SimulationSetupWithSignalsAndMemBoost(DataModel dataModel) {
		
		this(new SimulationPosibilitiesHandler(dataModel), 
			dataModel.getNumUnknownSpaceAvailablePerPlayer());
	}
	
	public SimulationSetupWithSignalsAndMemBoost( SimulationPosibilitiesHandler simPossibilities, int numSpacesAvailPerPlayer[]) {

		System.out.println("Preparing SimulationSetupWithSignalsAndMemBoost for monte carlo simulations");
		
		this.simPossibilities = simPossibilities;
		this.numSpacesAvailPerPlayer = numSpacesAvailPerPlayer;
	}
	
	
	private final int INDEX_PLAYER_A = 1;
	private final int INDEX_PLAYER_B = 2;
	private final int INDEX_PLAYER_C = 3;
	
	private static String forcedA[];
	private static String forcedB[];
	private static String forcedC[];
	
	private final int NUM_UNKNOWN_PLAYERS = 3;
	
	@Override
	public long initSimulationSetupAndRetNumWaysOtherPlayersCouldHaveCards() {
		
		forcedA = this.simPossibilities.otherPlayerPosSet[0][1][0][0];
		forcedB = this.simPossibilities.otherPlayerPosSet[0][0][1][0];
		forcedC = this.simPossibilities.otherPlayerPosSet[0][0][0][1];
		
		int numALeftToFill = numSpacesAvailPerPlayer[INDEX_PLAYER_A] - forcedA.length;
		
		
		//TODO: Go through every way player A can choose from 3 groups:
		//1) A AND B AND (NOT C)
		//2) A AND (NOT B) AND C
		//3) A AND B AND C
		//Max: (13 cards + 2 seperators) choose 2 = 15 choose 2 = 105 different combinations.
		
		boolean cardOwnershipPartitionIter[] = SimSetupUtils.setupComboIterator(
				NUM_UNKNOWN_PLAYERS - 1,
				numALeftToFill
				);
		
		//Groups Player A has the option to take from:
		int maxGroupCountA[] = new int[3];
		maxGroupCountA[0] = this.simPossibilities.otherPlayerPosSet[0][1][1][0].length; // A AND B AND (NOT C)
		maxGroupCountA[1] = this.simPossibilities.otherPlayerPosSet[0][1][0][1].length; // A AND (NOT B) AND C
		maxGroupCountA[2] = this.simPossibilities.otherPlayerPosSet[0][1][1][1].length; // A AND B AND C
		
		ArrayList<PlayerACombinationInfo> playerALookupBuilder = new ArrayList<PlayerACombinationInfo>();
		

		long curSumNumWays = 0L;
		
		while(cardOwnershipPartitionIter != null) {

			//TODO:
			int groupCardCountPlayerA[] = getGroupCardSizesFromComboAssume3Groups(cardOwnershipPartitionIter);
			
			boolean worksSoFar = true;
			for(int i=0; i<3; i++) {
				if(maxGroupCountA[i] < groupCardCountPlayerA[i]) {
					worksSoFar = false;
					break;
				}
			}
			
			int numBToFill = numSpacesAvailPerPlayer[INDEX_PLAYER_B];
			
			int maxPlayerBCouldFill = forcedB.length
			+ (this.simPossibilities.otherPlayerPosSet[0][1][1][0].length - groupCardCountPlayerA[0]) // A AND B AND (NOT C)
			+ (this.simPossibilities.otherPlayerPosSet[0][1][1][1].length - groupCardCountPlayerA[2]) // A AND B AND C
			+ (this.simPossibilities.otherPlayerPosSet[0][0][1][1].length); // B AND C
			
			if(maxPlayerBCouldFill < numBToFill) {
				worksSoFar = false;
			}
			
			int numCToFill = numSpacesAvailPerPlayer[INDEX_PLAYER_C];
			
			int maxPlayerCCouldFill = forcedC.length
			+ (this.simPossibilities.otherPlayerPosSet[0][1][0][1].length - groupCardCountPlayerA[1]) // A AND (NOT B) AND C
			+ (this.simPossibilities.otherPlayerPosSet[0][1][1][1].length - groupCardCountPlayerA[2]) // A AND B AND C
			+ (this.simPossibilities.otherPlayerPosSet[0][0][1][1].length); // B AND C
			
			if(maxPlayerCCouldFill < numCToFill) {
				worksSoFar = false;
			}
			
			
			if(worksSoFar) {

				//TODO: 
				//Add to the num ways.
				//Add an element to playerALookupBuilder.
				
				//TODO: Add curSumNumWays to playerALookupBuilder
				
				
				
				long numWays = 1L;
				
				//For i=0 to 3:
				//maxGroupCountA[i] choose groupCardCountPlayerA[i]
				
				for(int i=0; i<3; i++) {
					numWays *= SimSetupUtils.getCombination(maxGroupCountA[i], groupCardCountPlayerA[i]);
				}
				
				
				//TOP = (CARDINALITY(B AND C) - CARDINALITY(takenByAOf(A AND B AND C)))
				
				int m = this.simPossibilities.otherPlayerPosSet[0][2][1][1].length - groupCardCountPlayerA[2];
				
				//Choose:
				//BNUM = NumBToFill - forcedB - CARDINALITY(notTakenByAOf(A AND B)))
				
				int n = numBToFill - forcedB.length 
						- (this.simPossibilities.otherPlayerPosSet[0][1][1][0].length - groupCardCountPlayerA[0]);
				
				//ALT:
				//CNUM = NumCToFill - forcedC - CARDINALITY(notTakenByAOf(A AND C)))
				//NOTE: TOP should be = BNUM + CNUM
				//TODO: sanity check this!
				
				numWays *= SimSetupUtils.getCombination(m, n);
				
				//SANITY
				int n2 = numCToFill - forcedC.length 
						- (this.simPossibilities.otherPlayerPosSet[0][1][0][1].length - groupCardCountPlayerA[1]);
				
				if(n + n2 != m) {
					System.err.println("ERROR: I didn't count the number of combinations properly!");
					System.exit(1);
				}
				
				
				curSumNumWays += numWays;
				
				
			}
			
			cardOwnershipPartitionIter = SimSetupUtils.getNextCombination(cardOwnershipPartitionIter);
			
		}
		
		//TODO: and a last element to the playerALookupBuilder array, so we can knwo the total number
		// of combinations.
		System.out.println("Sum of the number of ways: " + curSumNumWays);
		
		//84478098072866400
		
		//TODO:
		// Turn playerALookupBuilder to playerALookup
		
		// TODO Auto-generated method stub
		return curSumNumWays;
	}
	
	public int[] getGroupCardSizesFromComboAssume3Groups(boolean cardOwnershipPartitionIter[]) {
		
		int ret[] = new int[3];
		int curIndex = 0;
		
		for(int i=0; i<cardOwnershipPartitionIter.length; i++) {
			
			if(cardOwnershipPartitionIter[i]) {
				curIndex++;
				
				if(curIndex==2) {
					//short cut:
					ret[2] = cardOwnershipPartitionIter.length - i - 1;
					break;
				}
				
			} else if( ! cardOwnershipPartitionIter[i]) {
				ret[curIndex]++;
			}
		}
		
		
		
		return ret;
	}

	@Override
	public String[][] getPossibleDistributionOfUnknownCardsBasedOnIndex(long combinationIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasSignalsBakedIn() {
		return true;
	}

	//TODO:
	//Make an improved monte that takes signals into account before it randomly distributes the cards
	// to the players.
	
	//Here's a sample of test cases the current monte doesn't calculate well because it doesn't understand signals:
	// 0-1657 (By pure chance, it still gets this one right...)
	// 2-1045
	// 2-2542
	// 2-3580
	// 1-544
	//They all rely on knowing partner has master.
	//There's other cases where knowing signals are important too:
		//like in a round during a mellow bid
	
	
	
	//INPUT;
	//Player pos and num empty positions per player.
	
	
	
}

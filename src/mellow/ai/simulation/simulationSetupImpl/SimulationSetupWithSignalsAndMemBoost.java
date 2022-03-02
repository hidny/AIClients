package mellow.ai.simulation.simulationSetupImpl;


import java.util.ArrayList;
import java.util.HashSet;

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
		

		long curSumBeforeCurrentComboIndexNumWays = 0L;
		
		int comboIndex = 0;
		
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
				
				

				PlayerACombinationInfo comboInfo = 
						new PlayerACombinationInfo(
								comboIndex,
								curSumBeforeCurrentComboIndexNumWays,
								SimSetupUtils.getCombination(maxGroupCountA[0], groupCardCountPlayerA[0]),
								groupCardCountPlayerA[0],
								SimSetupUtils.getCombination(maxGroupCountA[1], groupCardCountPlayerA[1]),
								groupCardCountPlayerA[1],
								SimSetupUtils.getCombination(maxGroupCountA[2], groupCardCountPlayerA[2]),
								groupCardCountPlayerA[2],
								SimSetupUtils.getCombination(m, n),
								n);
				
				playerALookupBuilder.add(comboInfo);
				

				curSumBeforeCurrentComboIndexNumWays += numWays;
				
				//Sanity check:
				playerALookupBuilder.get(playerALookupBuilder.size() - 1).sanityCheckNumWays(numWays);
			}
			
			cardOwnershipPartitionIter = SimSetupUtils.getNextCombination(cardOwnershipPartitionIter);
			comboIndex++;
			
		}
		
		//ADD a last element to the playerALookupBuilder array, so we can know the total number
		// of combinations.
		PlayerACombinationInfo comboInfo = 
				new PlayerACombinationInfo(
						comboIndex,
						curSumBeforeCurrentComboIndexNumWays,
						-1,-1,-1,-1,-1,-1,-1,-1);
		
		playerALookupBuilder.add(comboInfo);

		// Turn playerALookupBuilder to playerALookup:
		playerALookup = new PlayerACombinationInfo[playerALookupBuilder.size()];

		for(int i=0; i<playerALookupBuilder.size(); i++) {
			playerALookup[i] = playerALookupBuilder.get(i);
		}
		
		System.out.println("Sum of the number of ways: " + curSumBeforeCurrentComboIndexNumWays);
		
		// num ways others could have cards at beginning of the round:
		//84478098072866400
		//Looks good!
		
		
		return curSumBeforeCurrentComboIndexNumWays;
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
		
		// Binary search correct combo index number:
		int indexLookup = getRelevantIndexToLookup(playerALookup, combinationIndex);
		
		//Get Combo index for current playerALookup table:
		combinationIndex -= playerALookup[indexLookup].getCurSumWaysSoFar();
		
		//Sanity Checks:
		if(combinationIndex < playerALookup[indexLookup].getCurSumWaysSoFar()
				|| combinationIndex >= playerALookup[indexLookup + 1].getCurSumWaysSoFar()) {
			System.err.println("ERROR: Failed to get the correct indexLookup! (" + indexLookup + ")");
			System.exit(1);
			
		} else if(indexLookup == playerALookup.length - 1) {

			//if last index, something is wrong!
			System.err.println("ERROR: Found the last index number that's meant to mark the end! (" + indexLookup + ")");
			System.exit(1);
		}
		
		//Initialize output:
		int outputCurIndex[] = new int[4];
		
		String output[][] = new String[4][];
		for(int i=0; i<output.length; i++) {
			output[i] = new String[numSpacesAvailPerPlayer[i]];
		}
		
		//Insert forced cards:
		String forced[][] = new String[4][];
		forced[0] = new String[0];
		forced[1] = this.simPossibilities.otherPlayerPosSet[0][1][0][0];
		forced[2] = this.simPossibilities.otherPlayerPosSet[0][0][1][0];
		forced[3] = this.simPossibilities.otherPlayerPosSet[0][0][0][1];
		
		for(int i=0; i<forced.length; i++) {
			for(int j=0; j<forced[i].length; j++) {
				output[i][j] = forced[i][j];
			}
			outputCurIndex[i] += forced[i].length;
		}
		
		//Insert AB NOT C into A and B
		long indexToUseForCurrentGroup = combinationIndex % playerALookup[indexLookup].getNumWaysAGroupABNotC();
		combinationIndex /= playerALookup[indexLookup].getNumWaysAGroupABNotC();
		
		addCardsIntoTwoPlayer(
				this.simPossibilities.otherPlayerPosSet[0][1][1][0],
				output,
				outputCurIndex,
				INDEX_PLAYER_A,
				playerALookup[indexLookup].getNumCardsAGroupABNotC(),
				INDEX_PLAYER_B,
				indexToUseForCurrentGroup);
		
		//Insert A NOT B AND C into A and C
		indexToUseForCurrentGroup = combinationIndex % playerALookup[indexLookup].getNumWaysAGroupANotBC();
		combinationIndex /= playerALookup[indexLookup].getNumWaysAGroupANotBC();

		addCardsIntoTwoPlayer(
				this.simPossibilities.otherPlayerPosSet[0][1][0][1],
				output,
				outputCurIndex,
				INDEX_PLAYER_A,
				playerALookup[indexLookup].getNumCardsAGroupANotBC(),
				INDEX_PLAYER_C,
				indexToUseForCurrentGroup);
		
		//Insert ABC into A and mark those taken by A with a hashset
		indexToUseForCurrentGroup = combinationIndex % playerALookup[indexLookup].getNumWaysAGroupABC();
		combinationIndex /= playerALookup[indexLookup].getNumWaysAGroupABC();
		
		HashSet<String> taken = 
			addCardsIntoPlayerMarkRestAsTaken(
				this.simPossibilities.otherPlayerPosSet[0][1][1][1],
				output,
				outputCurIndex,
				INDEX_PLAYER_A,
				playerALookup[indexLookup].getNumCardsAGroupABC(),
				indexToUseForCurrentGroup);
				
		//Insert B AND C into B and C while ignoring cards taken by B
		indexToUseForCurrentGroup = combinationIndex % playerALookup[indexLookup].getNumWaysBGroupBC();
		combinationIndex /= playerALookup[indexLookup].getNumWaysBGroupBC();
		
		addCardsIntoTwoPlayerIgnoreTaken(
				taken,
				this.simPossibilities.otherPlayerPosSet[0][2][1][1],
				output,
				outputCurIndex,
				INDEX_PLAYER_B,
				playerALookup[indexLookup].getNumCardsBGroupBC(),
				INDEX_PLAYER_C,
				indexToUseForCurrentGroup);
		
		if(combinationIndex > 0) {
			System.out.println("ERROR: at this point, the combination Index should be 0");
			System.exit(1);
		}
		
		//Sanity check:
		for(int i=0; i<outputCurIndex.length; i++) {
			if(outputCurIndex[i] != output[i].length) {
				System.err.println("ERROR: not the right number of cards for player index " + i);
				System.exit(1);
			}
		}
		
		return output;
	}
	
	public void addCardsIntoTwoPlayer(String groupOfCards[], String output[][], int outputCurIndex[], int playerIndex1, int player1AmountTaken, int playerIndex2, long comboIndex) {
		boolean combo[] = SimSetupUtils.convertComboNumberToArray(groupOfCards.length, player1AmountTaken, comboIndex);
		
		int indexToGive = -1;
		for(int i=0; i<groupOfCards.length; i++) {
			if(combo[i]) {
				indexToGive = playerIndex1;
			} else {
				indexToGive = playerIndex2;
				
			}
			output[indexToGive][outputCurIndex[indexToGive]] = groupOfCards[i];
			outputCurIndex[indexToGive]++;
			
		}
	}

	public HashSet<String> addCardsIntoPlayerMarkRestAsTaken(String groupOfCards[], String output[][], int outputCurIndex[], int playerIndex1, int player1AmountTaken, long comboIndex) {
		boolean combo[] = SimSetupUtils.convertComboNumberToArray(groupOfCards.length, player1AmountTaken, comboIndex);
		
		HashSet<String> taken = new HashSet<String>();
		
		for(int i=0; i<groupOfCards.length; i++) {
			if(combo[i]) {
				output[playerIndex1][outputCurIndex[playerIndex1]] = groupOfCards[i];
				outputCurIndex[playerIndex1]++;
				
				taken.add(groupOfCards[i]);
			}
			
		}
		
		
		return taken;
	}
	
	//pre: Assumes taken cards are all within String groupOfCards[]
	// I'm assuming this to make it ever so slightly faster... but it's a bad assumption in general.
	public void addCardsIntoTwoPlayerIgnoreTaken(HashSet<String> taken, String groupOfCards[], String output[][], int outputCurIndex[], int playerIndex1, int player1AmountTaken, int playerIndex2, long comboIndex) {
		
		//Get num taken:
		//Could be replaced with taken.size(), but lets be extra safe:
		//taken.size()
		int numTaken = 0;
		for(int i=0; i<groupOfCards.length; i++) {
			if(taken.contains(groupOfCards[i])) {
				numTaken++;
			}
		}
		//End get num taken
		
		boolean combo[] = SimSetupUtils.convertComboNumberToArray(groupOfCards.length - numTaken, player1AmountTaken, comboIndex);
		
		
		
		int indexToGive = -1;
		
		int curComboI =0;
		
		for(int i=0; i<groupOfCards.length; i++) {
			if(taken.contains(groupOfCards[i])) {
				continue;
			}
			
			
			if(combo[curComboI]) {
				indexToGive = playerIndex1;
			} else {
				indexToGive = playerIndex2;
				
			}
			output[indexToGive][outputCurIndex[indexToGive]] = groupOfCards[i];
			outputCurIndex[indexToGive]++;

			curComboI++;
		}
	}
	

	@Override
	public boolean hasSignalsBakedIn() {
		return true;
	}

	public static int getRelevantIndexToLookup(PlayerACombinationInfo playerALookup[], long combinationIndex) {
		return getRelevantIndexToLookup(playerALookup, combinationIndex, 0, playerALookup.length);
	}
	
	public static int getRelevantIndexToLookup(PlayerACombinationInfo playerALookup[], long combinationIndex, int min, int max) {
		
		int mid = (min+max)/2;
		
		if(min == mid) {
			return mid;
			
		} else if(combinationIndex < playerALookup[mid].getCurSumWaysSoFar()) {
			return getRelevantIndexToLookup(playerALookup, combinationIndex, min, mid);
			
		} else {
			return getRelevantIndexToLookup(playerALookup, combinationIndex, mid, max);
			
		}
	}
	
	//Old description of this class before it was built:
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

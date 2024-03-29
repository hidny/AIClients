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
	
	private String cardsDataModelIsCertainAbout[];
	
	public SimulationSetupWithSignalsAndMemBoost(DataModel dataModel) {
		
		this(new SimulationPosibilitiesHandler(dataModel), 
			dataModel.getNumUnknownSpaceAvailablePerPlayer(),
			dataModel.getCardsThatDataModelIsCertainAbout());
	}

	public SimulationSetupWithSignalsAndMemBoost( SimulationPosibilitiesHandler simPossibilities, int numSpacesAvailPerPlayer[]) {
		this(simPossibilities, numSpacesAvailPerPlayer, new String[0]);
	}
	
	public SimulationSetupWithSignalsAndMemBoost( SimulationPosibilitiesHandler simPossibilities, int numSpacesAvailPerPlayer[], String cardsDataModelIsCertainAbout[]) {

		System.out.println("Preparing SimulationSetupWithSignalsAndMemBoost for monte carlo simulations");
		
		this.simPossibilities = simPossibilities;
		this.numSpacesAvailPerPlayer = numSpacesAvailPerPlayer;
		this.cardsDataModelIsCertainAbout = cardsDataModelIsCertainAbout;
		
		if(this.numSpacesAvailPerPlayer[0] > 0) {
			System.err.println("ERROR: SimulationSetupWithSignalsAndMemBoost assumes cards in current player hand are known.");
			System.exit(1);
		}
	}
	
	
	private final int INDEX_PLAYER_A = 1;
	private final int INDEX_PLAYER_B = 2;
	private final int INDEX_PLAYER_C = 3;
	
	private static String forcedBySignalsA[];
	private static String forcedBySignalsB[];
	private static String forcedBySignalsC[];
	
	private final int NUM_UNKNOWN_PLAYERS = 3;
	
	@Override
	public long initSimulationSetupAndRetNumWaysOtherPlayersCouldHaveCards() {
		
		//Only consider cards the dataModel isn't 100% sure about:
		//(For some reason, the dataModel is only expecting cards it's not 100% sure about
		// instead of all cards currently in other player's hands)
		forcedBySignalsA = minusSet(this.simPossibilities.otherPlayerPosSet[0][1][0][0], this.cardsDataModelIsCertainAbout);
		forcedBySignalsB = minusSet(this.simPossibilities.otherPlayerPosSet[0][0][1][0], this.cardsDataModelIsCertainAbout);
		forcedBySignalsC = minusSet(this.simPossibilities.otherPlayerPosSet[0][0][0][1], this.cardsDataModelIsCertainAbout);
		
		int numALeftToFill = numSpacesAvailPerPlayer[INDEX_PLAYER_A] - forcedBySignalsA.length;
		
		
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
			
			int maxPlayerBCouldFill = forcedBySignalsB.length
			+ (this.simPossibilities.otherPlayerPosSet[0][1][1][0].length - groupCardCountPlayerA[0]) // A AND B AND (NOT C)
			+ (this.simPossibilities.otherPlayerPosSet[0][1][1][1].length - groupCardCountPlayerA[2]) // A AND B AND C
			+ (this.simPossibilities.otherPlayerPosSet[0][0][1][1].length); // B AND C
			
			if(maxPlayerBCouldFill < numBToFill) {
				worksSoFar = false;
			}
			
			int numCToFill = numSpacesAvailPerPlayer[INDEX_PLAYER_C];
			
			int maxPlayerCCouldFill = forcedBySignalsC.length
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
				
				int n = numBToFill - forcedBySignalsB.length 
						- (this.simPossibilities.otherPlayerPosSet[0][1][1][0].length - groupCardCountPlayerA[0]);
				
				//ALT:
				//CNUM = NumCToFill - forcedC - CARDINALITY(notTakenByAOf(A AND C)))
				//NOTE: TOP should be = BNUM + CNUM
				//TODO: sanity check this!
				
				numWays *= SimSetupUtils.getCombination(m, n);
				
				//SANITY
				int n2 = numCToFill - forcedBySignalsC.length 
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
		
		//Get cur Combo index for current playerALookup table:
		long curCombinationIndex = combinationIndex - playerALookup[indexLookup].getCurSumWaysSoFar();
				
		
		//Initialize output:
		int outputCurIndex[] = new int[4];
		
		String output[][] = new String[4][];
		for(int i=0; i<output.length; i++) {
			output[i] = new String[numSpacesAvailPerPlayer[i]];
		}
		
		//Insert forced cards that are unknown to dataModel:
		String forcedBySignals[][] = new String[4][];
		forcedBySignals[0] = new String[0];
		forcedBySignals[1] = forcedBySignalsA;
		forcedBySignals[2] = forcedBySignalsB;
		forcedBySignals[3] = forcedBySignalsC;
		
		for(int i=0; i<forcedBySignals.length; i++) {
			for(int j=0; j<forcedBySignals[i].length; j++) {
				output[i][j] = forcedBySignals[i][j];
			}
			outputCurIndex[i] += forcedBySignals[i].length;
		}
		
		//Insert AB NOT C into A and B
		long indexToUseForCurrentGroup = curCombinationIndex % playerALookup[indexLookup].getNumWaysAGroupABNotC();
		curCombinationIndex /= playerALookup[indexLookup].getNumWaysAGroupABNotC();
		
		addCardsIntoTwoPlayer(
				this.simPossibilities.otherPlayerPosSet[0][1][1][0],
				output,
				outputCurIndex,
				INDEX_PLAYER_A,
				playerALookup[indexLookup].getNumCardsAGroupABNotC(),
				INDEX_PLAYER_B,
				indexToUseForCurrentGroup);
		
		//Insert A NOT B AND C into A and C
		indexToUseForCurrentGroup = curCombinationIndex % playerALookup[indexLookup].getNumWaysAGroupANotBC();
		curCombinationIndex /= playerALookup[indexLookup].getNumWaysAGroupANotBC();

		addCardsIntoTwoPlayer(
				this.simPossibilities.otherPlayerPosSet[0][1][0][1],
				output,
				outputCurIndex,
				INDEX_PLAYER_A,
				playerALookup[indexLookup].getNumCardsAGroupANotBC(),
				INDEX_PLAYER_C,
				indexToUseForCurrentGroup);
		
		//Insert ABC into A and mark those taken by A with a hashset
		indexToUseForCurrentGroup = curCombinationIndex % playerALookup[indexLookup].getNumWaysAGroupABC();
		curCombinationIndex /= playerALookup[indexLookup].getNumWaysAGroupABC();
		
		HashSet<String> taken = 
			addCardsIntoPlayerMarkRestAsTaken(
				this.simPossibilities.otherPlayerPosSet[0][1][1][1],
				output,
				outputCurIndex,
				INDEX_PLAYER_A,
				playerALookup[indexLookup].getNumCardsAGroupABC(),
				indexToUseForCurrentGroup);
				
		//Insert B AND C into B and C while ignoring cards taken by B
		indexToUseForCurrentGroup = curCombinationIndex % playerALookup[indexLookup].getNumWaysBGroupBC();
		curCombinationIndex /= playerALookup[indexLookup].getNumWaysBGroupBC();
		
		addCardsIntoTwoPlayerIgnoreTaken(
				taken,
				this.simPossibilities.otherPlayerPosSet[0][2][1][1],
				output,
				outputCurIndex,
				INDEX_PLAYER_B,
				playerALookup[indexLookup].getNumCardsBGroupBC(),
				INDEX_PLAYER_C,
				indexToUseForCurrentGroup);
		
		if(curCombinationIndex > 0) {
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
	
	public static String[] minusSet(String a[], String b[]) {
		
		ArrayList<String> cur = new ArrayList<String>();
		
		for(int i=0; i<a.length; i++) {
			boolean skip = false;
			for(int j=0; j<b.length; j++) {
				if(a[i].equals(b[j])) {
					skip = true;
					break;
				}
			}
			
			if(skip == false) {
				cur.add(a[i]);
			}
		}
		
		String ret[] = new String[cur.size()];
		
		for(int i=0; i<ret.length; i++) {
			ret[i] = cur.get(i);
		}
		
		return ret;
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

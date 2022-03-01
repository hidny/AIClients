package mellow.ai.simulation.simulationSetupImpl;


import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.simulation.SimulationSetupInterface;
import mellow.ai.simulation.objects.SelectedPartitionAndIndex;
import mellow.cardUtils.CardStringFunctions;

public class SimulationSetup implements SimulationSetupInterface{

	
	private boolean voidArray[][];
	private int curNumUnknownCardsPerSuit[];
	private int numSpacesAvailPerPlayer[];
	
	private String unknownCards[];
	
	public SimulationSetup(DataModel dataModel, boolean useSignals) {
		
		System.out.println("Preparing SimulationSetup for monte carlo simulations");
		voidArray = dataModel.createVoidArray(useSignals);

		unknownCards = dataModel.getUnknownCards();
		unknownCards = CardStringFunctions.sort(unknownCards);
		
		curNumUnknownCardsPerSuit = CardStringFunctions.organizeCardsBySuit(unknownCards);
		numSpacesAvailPerPlayer = dataModel.getNumUnknownSpaceAvailablePerPlayer();
		
		
	}
	
	public SimulationSetup() {
		
	}
	
	//pre: As long as the answer is less than 2^63, it should get the right answer.
	// All calculations in Euchre and Mellow will be less than 2^63, but other cards games with 5 players may overflow the return value.
	// You might get performance improvements by reordering which players get their cards first, but life's too short.


	
	public long initSimulationSetupAndRetNumWaysOtherPlayersCouldHaveCards() {
		return initSimulationSetupAndGetNumWaysOtherPlayersCouldHaveCards(curNumUnknownCardsPerSuit, numSpacesAvailPerPlayer, voidArray);
	}

	public long initSimulationSetupAndGetNumWaysOtherPlayersCouldHaveCards(int numUnknownCardsPerSuit[], int numSpacesAvailPerPlayer[], boolean originalIsVoidList[][]) {
		return initSimulationSetupAndGetNumWaysOtherPlayersCouldHaveCards(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, 0);
	}

	
	private long initSimulationSetupAndGetNumWaysOtherPlayersCouldHaveCards(int numUnknownCardsPerSuit[], int numSpacesAvailPerPlayer[], boolean originalIsVoidList[][], int depth) {

		//Setup basic vars:
		boolean voidSuit[] = SimSetupUtils.getVoidSuitArrayForPlayer(numUnknownCardsPerSuit, originalIsVoidList, depth);
		int numVoids = SimSetupUtils.getSumOfElementsInArray(voidSuit);
		int numUnknownCardsLeft = SimSetupUtils.getSumOfElementsInArray(numUnknownCardsPerSuit);
		int numSpaceAvailable = numSpacesAvailPerPlayer[depth];
		
	//Skipping conditions	
		//Skip players with no unknown cards:
		if(numSpaceAvailable == 0) {
			if(depth + 1 < numSpacesAvailPerPlayer.length) {
				return initSimulationSetupAndGetNumWaysOtherPlayersCouldHaveCards(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, depth + 1);
			} else {
				return 1;
			}
		}
		
		//Check if current player could pick up remaining unknown cards considering void constraints:
		if(SimSetupUtils.playerCouldFillTheirHandWithRemainingCardsConsideringVoidConstraints(numSpaceAvailable, numUnknownCardsPerSuit, voidSuit) == false) {
			return 0;
		}
		
		//Pick up all cards if allowed and no other unknown cards:
		if(numUnknownCardsLeft == numSpacesAvailPerPlayer[depth]) {
			return 1;
		}
	//END Skipping conditions
		
		long ret = 0L;
		
		//Setup suit partition iterator
		int numTrumpSeperators = Constants.NUM_SUITS - 1 - numVoids;
		boolean suitPartitionIter[] = SimSetupUtils.setupComboIterator(numTrumpSeperators, numSpaceAvailable);

		while(suitPartitionIter != null) {
			
			int suitArrayForEachNonVoidSuit[] = SimSetupUtils.convertComboToArray(suitPartitionIter, Constants.NUM_SUITS - numVoids);
			int suitsTakenByPlayer[] = SimSetupUtils.getNumCardsOfEachSuitTakenByPlayer(voidSuit, suitArrayForEachNonVoidSuit);
			int numCardsPerSuitAfterTake[] = SimSetupUtils.getCardsPerSuitRemainingAfterTake(numUnknownCardsPerSuit, suitsTakenByPlayer);
			

			//TODO: There might be a way to skip bad suit partitions more quickly than this... but whatever
			 if( SimSetupUtils.suitPartitionImpossibleToTake(numCardsPerSuitAfterTake, suitsTakenByPlayer, numUnknownCardsPerSuit) ) {
				suitPartitionIter = SimSetupUtils.getNextCombination(suitPartitionIter);
				continue;
			 }
			
			long currentNumWays = 1;
			for(int i=0; i<Constants.NUM_SUITS; i++) {
				currentNumWays *= SimSetupUtils.getCombination(numUnknownCardsPerSuit[i], suitsTakenByPlayer[i]);
			}
			
			//Setup next players recursively if it's not the last player:
			if(depth + 1 < numSpacesAvailPerPlayer.length && currentNumWays > 0) {
				currentNumWays *= initSimulationSetupAndGetNumWaysOtherPlayersCouldHaveCards(numCardsPerSuitAfterTake, numSpacesAvailPerPlayer, originalIsVoidList, depth + 1);
			}
			
			ret += currentNumWays;
			
			suitPartitionIter = SimSetupUtils.getNextCombination(suitPartitionIter);
		}
		
		
		return ret;
		
	}
	



	public String[][] getPossibleDistributionOfUnknownCardsBasedOnIndex(long combinationIndex) {
		
		if(numSpacesAvailPerPlayer[0] > 0) {
			System.err.println("ERROR: unknown card for currrent player");
			System.exit(1);
		}

		SelectedPartitionAndIndex suitPartitionsAndComboNumbers = 
				getSelectedPartitionAndIndexBasedOnCombinationIndex(curNumUnknownCardsPerSuit, numSpacesAvailPerPlayer, voidArray, combinationIndex);
		
		return SimSetupUtils.serveCarsdsBasedOnPartitionAndIndexInfo(suitPartitionsAndComboNumbers, unknownCards, numSpacesAvailPerPlayer);
		
	}
	
	
	//pre: there's always at least 1 way to do it and randIndexNumber < number Of Combinations
	public SelectedPartitionAndIndex getSelectedPartitionAndIndexBasedOnCombinationIndex(int numUnknownCardsPerSuit[], int numSpacesAvailPerPlayer[], boolean originalIsVoidList[][], long comboIndexNumber) {
		return getSelectedPartitionAndIndexBasedOnCombinationIndex(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, comboIndexNumber, 0, new SelectedPartitionAndIndex());
	}
	
	//pre: there's always at least 1 way to do it and randIndexNumber < number Of Combinations
	private SelectedPartitionAndIndex getSelectedPartitionAndIndexBasedOnCombinationIndex(int numUnknownCardsPerSuit[], int numSpacesAvailPerPlayer[], boolean originalIsVoidList[][], long comboIndexNumber, int depth, SelectedPartitionAndIndex selectedPartitionAndIndexToFillIn) {
		//Setup basic vars:
		boolean voidSuit[] = SimSetupUtils.getVoidSuitArrayForPlayer(numUnknownCardsPerSuit, originalIsVoidList, depth);
		int numVoids = SimSetupUtils.getSumOfElementsInArray(voidSuit);
		int numUnknownCardsLeft = SimSetupUtils.getSumOfElementsInArray(numUnknownCardsPerSuit);
		int numSpaceAvailable = numSpacesAvailPerPlayer[depth];
	
	//Skipping conditions	
		//Skip players with no unknown cards:
		if(numSpaceAvailable == 0) {

			selectedPartitionAndIndexToFillIn.giveNoCardsToPlayer(depth);

			if(depth + 1 < numSpacesAvailPerPlayer.length) {
				return getSelectedPartitionAndIndexBasedOnCombinationIndex(numUnknownCardsPerSuit, numSpacesAvailPerPlayer, originalIsVoidList, comboIndexNumber, depth+1, selectedPartitionAndIndexToFillIn);
			} else {
				return selectedPartitionAndIndexToFillIn;
			}
		}
		
		//Check if current player could pick up remaining unknown cards considering void constraints:
		if(SimSetupUtils.playerCouldFillTheirHandWithRemainingCardsConsideringVoidConstraints(numSpaceAvailable, numUnknownCardsPerSuit, voidSuit) == false) {
			System.err.println("ERROR: called getSelectedPartitionAndIndex in a case where there\'s no way to give the players the remaining cards");
			System.exit(1);
			return selectedPartitionAndIndexToFillIn;
		}
		
		//Pick up all cards if allowed and no other unknown cards:
		if(numUnknownCardsLeft == numSpacesAvailPerPlayer[depth]) {
			selectedPartitionAndIndexToFillIn.giveWhatsLeftToNextPlayer(depth, numUnknownCardsPerSuit);
			return selectedPartitionAndIndexToFillIn;
		}
	//END Skipping conditions
		
		long prevNumCombosSkippedThru = 0L;
		long numCombosSkippedThru = 0L;
		
		int numTrumpSeperators = Constants.NUM_SUITS - 1 - numVoids;
		boolean suitPartitionIter[] = SimSetupUtils.setupComboIterator(numTrumpSeperators, numSpaceAvailable);
		
		
		while(suitPartitionIter != null) {
			
			int suitArrayForEachNonVoidSuit[] = SimSetupUtils.convertComboToArray(suitPartitionIter, Constants.NUM_SUITS - numVoids);
			int suitsTakenByPlayer[] = SimSetupUtils.getNumCardsOfEachSuitTakenByPlayer(voidSuit, suitArrayForEachNonVoidSuit);
			int numCardsPerSuitAfterTake[] = SimSetupUtils.getCardsPerSuitRemainingAfterTake(numUnknownCardsPerSuit, suitsTakenByPlayer);
			
			if( SimSetupUtils.suitPartitionImpossibleToTake(numCardsPerSuitAfterTake, suitsTakenByPlayer, numUnknownCardsPerSuit) ) {
				suitPartitionIter = SimSetupUtils.getNextCombination(suitPartitionIter);
				continue;
			}
			
			long currentNumWays = 1;
			for(int i=0; i<Constants.NUM_SUITS; i++) {
				currentNumWays *= SimSetupUtils.getCombination(numUnknownCardsPerSuit[i], suitsTakenByPlayer[i]);
			}
			
			if(depth + 1 < numSpacesAvailPerPlayer.length && currentNumWays > 0) {
				currentNumWays *= initSimulationSetupAndGetNumWaysOtherPlayersCouldHaveCards(numCardsPerSuitAfterTake, numSpacesAvailPerPlayer, originalIsVoidList, depth + 1);
				
			}
			
			numCombosSkippedThru += currentNumWays;
			
			//Check if the combination index is contained with the current suitPartitionIteration:
			if(numCombosSkippedThru > comboIndexNumber && currentNumWays > 0) {

				selectedPartitionAndIndexToFillIn.setSuitsTakenByPlayers(depth, suitsTakenByPlayer);
				
				if(depth + 1 < numSpacesAvailPerPlayer.length) {
					
					//Recursively fill in partition info for next player:
					long indexFromStartOfCombo = (comboIndexNumber - prevNumCombosSkippedThru);
					long numWaysToSetupNextPlayers = initSimulationSetupAndGetNumWaysOtherPlayersCouldHaveCards(numCardsPerSuitAfterTake, numSpacesAvailPerPlayer, originalIsVoidList, depth + 1);
					long currentPlayerComboNumber = indexFromStartOfCombo / numWaysToSetupNextPlayers;
					long nextComboIndexNumber = indexFromStartOfCombo % numWaysToSetupNextPlayers;
					
					selectedPartitionAndIndexToFillIn.setPlayerComboNumber(depth, currentPlayerComboNumber);
					
					return getSelectedPartitionAndIndexBasedOnCombinationIndex(numCardsPerSuitAfterTake, numSpacesAvailPerPlayer, originalIsVoidList, nextComboIndexNumber, depth + 1, selectedPartitionAndIndexToFillIn);

				} else {
					
					//Last player to fill-in shouldn't make it to the while loop in this function
					System.err.println("ERROR: something went wrong in getSelectedPartitionAndIndex. Last player wasted time and iterated thru suit partitions.");
					System.exit(1);
					return null;
				}
				
			}
			
			prevNumCombosSkippedThru = numCombosSkippedThru;
			
			suitPartitionIter = SimSetupUtils.getNextCombination(suitPartitionIter);
		}
		
		//At this point, the combination index got too big...
		
		if(numCombosSkippedThru <= comboIndexNumber) {
			System.err.println(numCombosSkippedThru + "  vs " + comboIndexNumber);
			System.err.println("ERROR: something went wrong in getSelectedPartitionAndIndex. comboIndexNumber is too big");
			System.err.println("depth: " + depth);
			System.exit(1);
		}

		System.err.println("ERROR: did not return selectedPartitionAndIndexToFillIn and numCombosSkippedThru is bigger than comboIndexNumber?");
		System.exit(1);
		return null;
		
		
	}



	@Override
	public boolean hasSignalsBakedIn() {
		return false;
	}



}

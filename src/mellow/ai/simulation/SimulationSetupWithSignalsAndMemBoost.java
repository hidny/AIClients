package mellow.ai.simulation;

import java.util.Random;

import mellow.Constants;
import mellow.cardUtils.CardStringFunctions;
import mellow.cardUtils.DebugFunctions;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.simulation.objects.SelectedPartitionAndIndex;

public class SimulationSetupWithSignalsAndMemBoost {

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
	
	
	//Improved version of hack to add signals in:
	/*
	 * 
			//TODO: area to hack signals in
			
			if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "QS 5S 2S 9H 5H AD JD TD")) {
				boolean skip = false;
				for(int j=0; j<distCards[1].length; j++) {
					if(DataModel.getRankIndex(distCards[1][j]) > DataModel.RANK_EIGHT
							&& CardStringFunctions.getIndexOfSuit(distCards[1][j]) == Constants.CLUB) {
						//System.err.println("Skipping because " + distCards[1][j]);
						numSkipped++;
						skip = true;
						break;
					}
				}
				
				if(skip) {
					if(isThorough == false && skipSimulations) {
						numSkipped++;
						i--;
					}
					continue;
				}
			}
			//END TODO
			 */

	
	//Old Hack I use to fix the orig monte:
	/*
	 * 
			if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "3S 3C QD 8D ")) {
				boolean skip = false;
				for(int j=0; j<distCards[1].length; j++) {
					if(distCards[1][j].equals("QC")) {
						//System.err.println("Skipping because QC");
						numSkipped++;
						skip = true;
						break;
					}
					if(distCards[1][j].equals("KD")) {
						//System.err.println("Skipping because QC");
						numSkipped++;
						skip = true;
						break;
					}
					if(distCards[1][j].equals("JD")) {
						//System.err.println("Skipping because QC");
						numSkipped++;
						skip = true;
						break;
					}
				}
				if(skip) {
					continue;
				}
			}
	 */
	
	
}

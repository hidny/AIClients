package mellow.ai.simulation;

import mellow.ai.cardDataModels.DataModel;

//TODO: use this!
public interface SimulationSetupInterface {

	
	//Idea these functions should have everything they need to handle:
	// Orig SimSetup
	// SimSetupWithSignals Baked in
	// That means the implementation won't necessarily use every input var.
	
	//TODO: 1st make Simulation Possibility Handler non-static...
	
	
	//public long getCurrentNumWaysOtherPlayersCouldHaveCardsAndSetupDataModelForSimulations(DataModel dataModel, boolean processSignals);
	//Rename to:
	//TODO: add Player pos as dynamic var
	public long initSimulationSetupAndGetNumWaysOtherPlayersCouldHaveCards(DataModel dataModel, boolean processSignals);
	
	//TODO: add Player pos as dynamic var
	public String[][] getPossibleDistributionOfUnknownCardsBasedOnIndex(DataModel dataModel, long combinationIndex, long numWaysToSimulate, boolean useSignals, boolean voidArray[][], String unknownCards[], int curNumUnknownCardsPerSuit[], int numSpacesAvailPerPlayer[]);
	
	public boolean hasSignalsBakedIn();
}

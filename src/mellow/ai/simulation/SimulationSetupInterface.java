package mellow.ai.simulation;

public interface SimulationSetupInterface {

	//This interface is made to be as simple as possible.
	
	//Customization and vars should be inserted in the constructor
	
	//Make the classes implement simple functions below:
	public long initSimulationSetupAndRetNumWaysOtherPlayersCouldHaveCards();
	
	
	public String[][] getPossibleDistributionOfUnknownCardsBasedOnIndex(long combinationIndex);
	
	
	public boolean hasSignalsBakedIn();
}

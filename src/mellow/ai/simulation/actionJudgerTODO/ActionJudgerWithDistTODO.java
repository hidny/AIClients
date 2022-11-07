package mellow.ai.simulation.actionJudgerTODO;

import mellow.ai.cardDataModels.DataModel;

//TODO: Have a version of MellowBasicDecider, so I can use the getCardFunction
// and compare results...
public class ActionJudgerWithDistTODO {

	// I'll start with the Quick Action Judger, and move onto this one later.
	// Even later, I'll try to make a neural net that just figures out the plausibility of
	// a given distribution of cards for a testcase.
	
	//This will judge the plausibility of the actions with a given distribution of cards
	//before the test case takes place.
	
	//I'll implement the player object even though that's overkill.
	//I just need to copy/paste the dataModel after bids,
	// and dist determined, and implement receiveCardPlayed
	
	public ActionJudgerWithDistTODO(DataModel dataModelAtEndOfBids, String distCards[][]) {
		this.dataModel = dataModelAtEndOfBids.createHardCopy();
	}
	
	private double currentPlausibility = 0.0;
	
	private DataModel dataModel;
	
	public void receiveCardPlayed(String playerName, String card) {
		// TODO Auto-generated method stub
		
		dataModel.updateDataModelWithPlayedCard(playerName, card);
		
		//TODO: do some magic
	}

	public double getCurrentPlausibility() {
		// TODO Auto-generated method stub
		
		return 0.0;
		
	}

}

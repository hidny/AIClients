package mellow.ai.aiDecider;

import java.util.ArrayList;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.simulation.MonteCarloMain;
import mellow.ai.situationHandlers.BiddingSituation;
import mellow.ai.situationHandlers.NoMellowBidPlaySituation;
import mellow.ai.situationHandlers.PartnerSaidMelllowSituation;
import mellow.ai.situationHandlers.SeatedLeftOfOpponentMellow;
import mellow.ai.situationHandlers.SeatedRightOfOpponentMellow;
import mellow.ai.situationHandlers.SingleActiveMellowPlayer;
import mellow.cardUtils.CardStringFunctions;

//_______________________
//This is a basic AI that handles mellow rounds for someone who is leading, 2nd, and 3rd and 4th
//_____________________________
//After some calculations, I realized there are hundreds of unique mellow situations that I'd have to make rules for
//to force it to play like me.

//In complicated cases, I'm going to just make run it a monte-carlo simulation so the AI
//can make up it's own mind.

//Update: i'm just going to start simple and play against it and notice where it lacks.
// At that point, I'll improve it based on need

//Same idea goes for monte-carlo which depends on a good and fast decider and a good filter/weigher to work

//When it's good enough, I'll think about doing a decision tree that learned based on the features
//and/or using deep learning.... but that's not anytime soon.


public class MellowBasicDecider implements MellowAIDeciderInterface {
	
	
	DataModel dataModel;
	private boolean doMonteCarloSimuations = false;
	private int num_simulations_for_monte_carlo = MonteCarloMain.NUM_SIMULATIONS_DEFAULT;
	
	//TODO: Consider where the dealer is when bidding (and consider previous bids)
	
	//TODO: handle case where there's a mellow and then double mellow...
	// :(
	
	
	 
	public MellowBasicDecider() {
		this(false, MonteCarloMain.NUM_SIMULATIONS_DEFAULT);
	}
	
	public MellowBasicDecider(boolean doSimuations) {
		this(doSimuations, MonteCarloMain.NUM_SIMULATIONS_DEFAULT);
	}
	
	public MellowBasicDecider(boolean doSimuations, int num_simulations) {
		this.dataModel = new DataModel();
		this.doMonteCarloSimuations = doSimuations;
		this.num_simulations_for_monte_carlo = num_simulations;
	}
	
	
	//Use datamodel that already exists:
	//(this helps with creating a decider that enters in the middle of the game.)
	public MellowBasicDecider(DataModel dataModelInput) {
		this.dataModel = dataModelInput;
	}
	
	public static final int SPADE = 0;
	public static final int HEART = 1;
	public static final int CLUB = 2;
	public static final int DIAMOND = 3;
	
	//index
	//0: myCardsUsed
	//1: west cards
	//2: north cards
	//3: east cards
	
	
	public void resetStateForNewRound() {
		dataModel.resetStateForNewRound();
	}

	@Override
	public void receiveUnParsedMessageFromServer(String msg) {
		// TODO: use if you want...
	}
	
	public String toString() {
		return "MellowBasicDeciderAI";
	}
	
	@Override
	public void setNameOfPlayers(String players[]) {
		dataModel.setNameOfPlayers(players);
	}
	
	
	//TODO: don't make this so destructive... OR: have testcase say orig card in hand!!!
	@Override
	public void setCardsForNewRound(String cards[]) {
		dataModel.setupCardsInHandForNewRound(cards);
	}


	@Override
	public void setDealer(String playerName) {
		dataModel.setDealer(playerName);
	}

	@Override
	public void receiveBid(String playerName, int bid) {
		dataModel.setBid(playerName, bid);
	}

	@Override
	public void receiveCardPlayed(String playerName, String card) {
		dataModel.updateDataModelWithPlayedCard(playerName, card);
	}

	@Override
	public void setNewScores(int AIScore, int OpponentScore) {
		dataModel.setNewScores(AIScore, OpponentScore);
		
	}

	@Override
	public String getCardToPlay() {
	
		String cardRecommended = getCardToPlay2();
		
		return getLegalCard(cardRecommended);
	}
	
	public String getLegalCard(String cardRecommended) {
		//Make sure card is legal for simulation:
		if(dataModel.isCardLegalToPlay(cardRecommended)) {
			System.out.println("TEST: " + dataModel.getPlayers()[0] + " plays the " + cardRecommended);
			return cardRecommended;
		} else {
			
			System.out.println("WARNING: calling getFirstLegalCardThatCouldBeThrown in basic mellow decider");
			return dataModel.getFirstLegalCardThatCouldBeThrown();
		}
	}
	
	private String getCardToPlay2() {
		
		
		if(dataModel.throwerCanOnlyPlayOneCard()) {
			System.out.println("**Forced to play card");
			return dataModel.getOnlyCardCurrentPlayerCouldPlay();
		}

		for(int i=0; i<4; i++) {
			if(dataModel.burntMellow(i)) {
				System.out.println("TEST BURNT MELLOW index: " + i);
			}
		}
		
		//Run montecarlo simulations if config set to monte carlo 
		//AND decider is not currently in a simulation: (Running a simulation in a simulation is expensive)
		if(this.doMonteCarloSimuations && dataModel.getSimulation_level() == 0) {
			return MonteCarloMain.runMonteCarloMethod(dataModel, this.num_simulations_for_monte_carlo);
		}
		
		int numActiveMellows = 0;
		for(int i=0; i<4; i++) {
			if(dataModel.getBid(i) == 0 && dataModel.burntMellow(i) == false) {
				numActiveMellows++;
			}
		}
		
		if(numActiveMellows == 0) {
			return NoMellowBidPlaySituation.handleNormalThrow(dataModel);
			
		} else if(numActiveMellows == 1) {
			if(dataModel.getBid(Constants.CURRENT_AGENT_INDEX) == 0 && dataModel.burntMellow(Constants.CURRENT_AGENT_INDEX) == false) {
				System.out.println("MELLOW TEST HERE");
				return SingleActiveMellowPlayer.handleThrowAsSingleActiveMellowBidder(dataModel);
				
			} else {
				
				System.out.println("MELLOW SOMEWHERE");
				if(dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) == 0) {
					//TODO: Working on the october 3rd, 2020:
					return PartnerSaidMelllowSituation.playMoveToProtectPartnerMellow(dataModel);

				} else {
					
					if(dataModel.getBid(Constants.RIGHT_PLAYER_INDEX) == 0) {
						
						return SeatedLeftOfOpponentMellow.playMoveSeatedLeftOfOpponentMellow(dataModel);
					
					} else {
						
						return SeatedRightOfOpponentMellow.playMoveSeatedRightOfOpponentMellow(dataModel);
					}
				}
			}
			
		} else if(numActiveMellows == 2) {
			if(dataModel.getBid(Constants.CURRENT_AGENT_INDEX) == 0) {
				
				
				System.out.println("MELLOW TEST (double mellow)");
				return SingleActiveMellowPlayer.handleThrowAsSingleActiveMellowBidder(dataModel);

			} else if(dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) == 0){

				//TODO: not quite right:
				//This should be the most complicated logic of the game,
				//but let's start simple!
				return PartnerSaidMelllowSituation.playMoveToProtectPartnerMellow(dataModel);
				
			} else {

				//Opponents double mellow:
				//Just play low if you can....
				return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
			}
			
		} else {
			System.err.println("Really??? There's either more than 2 or less than 0 mellows.");
			System.exit(1);
			return "";
		}
		
	}

		
	//TODO: will need to test/improve get BidToMake.
	
	@Override
	public String getBidToMake() {
		return BiddingSituation.getSimpleBidToMake(dataModel);
	}

	
	
}

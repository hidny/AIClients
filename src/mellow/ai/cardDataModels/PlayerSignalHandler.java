package mellow.ai.cardDataModels;

import mellow.Constants;
import mellow.ai.cardDataModels.normalPlaySignals.MellowLetPartnerWinSignals;
import mellow.ai.cardDataModels.normalPlaySignals.MellowVoidSignalsNoActiveMellows;
import mellow.ai.cardDataModels.playerSaidMellowSignals.PlayerSaidMellowSignals;
import mellow.cardUtils.CardStringFunctions;


//TODO: put PlayerSignalHandler in DataModel Constructor... or:
// build by default.

//TODO: make current file be able to answer questions about signals.
public class PlayerSignalHandler {
	
	//TODO: MELLOW_PLAYER_SIGNALED_NO is just a rudimentary signal, feel free to develop it.
	static final byte MELLOW_PLAYER_SIGNALED_NO = 3;
	
	//TODO: have new tables
	
	//Source of truth:
	//private int cardsCurrentlyHeldByPlayer[][][]
	
	//Signalling suggestion to add one by one:
	//private int cardsMellowBidderSignalNo[][][];
	//private int cardsSignalledNoDuringNormalPlay[][][];
	//....
	
	//TODO: implement signals for cardsSignalledNoDuringNormalPlay !!
	
	
	private DataModel dataModel;
	
	private PlayerSaidMellowSignals playerSaidMellowSignals;
	private MellowVoidSignalsNoActiveMellows mellowVoidSignalsNoActiveMellows;
	private MellowLetPartnerWinSignals mellowLetPartnerWinSignals;
	
	public PlayerSignalHandler(DataModel dataModel) {
		this.dataModel = dataModel;
		
		this.playerSaidMellowSignals = new PlayerSaidMellowSignals(dataModel);
		this.mellowVoidSignalsNoActiveMellows = new MellowVoidSignalsNoActiveMellows(dataModel);
		this.mellowLetPartnerWinSignals = new MellowLetPartnerWinSignals(dataModel);
	}
	
	
	//THIS IS JUST A SIMPLE 1st attempt!

	//TODO: put this in it's own class...
	//So far, it only deals with mellow signals
	public void updateDataModelSignalsWithPlayedCard(String playerName, String card) {
		
		int playerIndex = dataModel.convertPlayerNameToIndex(playerName);


		
		if(dataModel.getBid(playerIndex) == 0) {
			//Deal with mellow signals:
			this.playerSaidMellowSignals.handleSignalsFromActiveMellow(playerIndex, card);
			
			
		}
		
		//Normally work under normal circumstances for Now
		mellowVoidSignalsNoActiveMellows.updateDataModelSignalsWithPlayedCard(playerName, card);

		mellowLetPartnerWinSignals.updateDataModelSignalsWithPlayedCard(playerName, card);
		
	}
	
	
	//Player said mellow signals:
	//TODO: I'm thinking of just passing this.playerSaidMellowSignals to the situation handlers and
	//      not having these functions here...
	
	public void setCardMellowSignalNoIfUncertain(int playerIndex, int suitIndex, int rankIndex) {
		this.playerSaidMellowSignals.setCardMellowSignalNoIfUncertain(playerIndex, suitIndex, rankIndex);
	}
	
	public String getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(int mellowPlayerIndex, int suitIndex) {
		return this.playerSaidMellowSignals.getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(mellowPlayerIndex, suitIndex);
	}
	
	public boolean mellowSignalledNoCardOverCardSameSuit(String inputCard, int mellowPlayerIndex) {
		return this.playerSaidMellowSignals.mellowSignalledNoCardOverCardSameSuit(inputCard, mellowPlayerIndex);
	}
	
	public boolean mellowSignalledNoCardUnderCardSameSuitExceptRank2(String inputCard, int mellowPlayerIndex) {
		return this.playerSaidMellowSignals.mellowSignalledNoCardUnderCardSameSuitExceptRank2(inputCard, mellowPlayerIndex);
	}
	
	public boolean mellowSignalledNoCardBetweenTwoCards(String smallerCard, String biggerCard, int mellowPlayerIndex) {
		return this.playerSaidMellowSignals.mellowSignalledNoCardBetweenTwoCards(smallerCard, biggerCard, mellowPlayerIndex);
	}
	
	public boolean mellowPlayerSignalNoCardsOfSuit(int mellowPlayerIndex, int suitIndex) {
		return this.playerSaidMellowSignals.mellowPlayerSignalNoCardsOfSuit(mellowPlayerIndex, suitIndex);
	}
	
	public boolean mellowPlayerMayBeInDangerInSuit(int mellowPlayerIndex, int suitIndex) {
		return this.playerSaidMellowSignals.mellowPlayerMayBeInDangerInSuit(mellowPlayerIndex, suitIndex);
	}

	//END Player said mellow signals
	
	public boolean playerStrongSignaledNoCardsOfSuit(int playerIndex, int suitIndex) {

		return this.mellowVoidSignalsNoActiveMellows.playerStrongSignaledNoCardsOfSuit(playerIndex, suitIndex);
	}
	
	public boolean playerSignalledHighCardInSuit(int playerIndex, int suitIndex) {
		return this.mellowLetPartnerWinSignals.playerSignalledHighCardInSuit(playerIndex, suitIndex);
	}
	
	public int getPlayerIndexOfKingSacrificeForSuit(int indexSuit) {
		return this.mellowVoidSignalsNoActiveMellows.getPlayerIndexOfKingSacrificeForSuit(indexSuit);
	}

	public int getMaxRankSpadeSignalled(int playerIndex) {
		return this.mellowVoidSignalsNoActiveMellows.getMaxRankSpadeSignalled(playerIndex);
	}
	
	//public int getPlayerIndexSignalledMasterQueen(int indexSuit) {
	//	return this.mellowVoidSignalsNoActiveMellows.getPlayerIndexSignalledMasterQueen(indexSuit);
	//}
	
	//Player who bid Mellow signals 
	public int getNumCardsMellowSignalledOverCardSameSuit(String card, int mellowPlayerIndex) {
		return this.playerSaidMellowSignals.getNumCardsMellowSignalledOverCardSameSuit(card, mellowPlayerIndex);
	}
	
	public int getNumCardsMellowSignalledBetweenTwoCards(String smallerCard, String biggerCard, int mellowPlayerIndex) {
		return this.playerSaidMellowSignals.getNumCardsMellowSignalledBetweenTwoCards(smallerCard, biggerCard, mellowPlayerIndex);
	}


/*
	public boolean mellowSignalledNo2CardsOverCardSameSuit(String inputCard, int mellowPlayerIndex) {
		
		return this.playerSaidMellowSignals.mellowSignalledNo2CardsOverCardSameSuit(inputCard, mellowPlayerIndex);
		
	}
*/
	
}

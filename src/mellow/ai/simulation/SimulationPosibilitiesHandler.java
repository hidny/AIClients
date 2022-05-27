package mellow.ai.simulation;

import java.util.HashSet;
import java.util.Iterator;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.cardDataModels.normalPlaySignals.VoidSignalsNoActiveMellows;
import mellow.cardUtils.CardStringFunctions;
import mellow.cardUtils.DebugFunctions;

public class SimulationPosibilitiesHandler {

	//TODO: make this non-static?
	
	//TODO: Incorporate signal info in:
	// MellowLetPartnerWinSignals
	// and VoidSignalsNoActiveMellows
	
	 public HashSet<String> playerPos[] = new HashSet[Constants.NUM_PLAYERS];
	

	 public SimulationPosibilitiesHandler(DataModel dataModel) {
		 setupCardPossibilities(dataModel);
	}

	 public SimulationPosibilitiesHandler(HashSet<String> playerPos[]) {
		 this.playerPos = playerPos;
		 setupPossibilitySets();
	}

	public HashSet<String>[] getPlayerPos() {
		return playerPos;
	}

	public String[][][][][] getOtherPlayerPosSet() {
		return otherPlayerPosSet;
	}

	public void setupCardPossibilities(DataModel dataModel) {
		 
		 playerPos = new HashSet[Constants.NUM_PLAYERS];
		 
		 for(int playerIndex = 0; playerIndex<Constants.NUM_PLAYERS; playerIndex++) {
			 
			 playerPos[playerIndex] = new HashSet<String>();
					 
			 if(playerIndex == Constants.CURRENT_AGENT_INDEX) {
				 continue;
			 }
			 
			 
			 for(int suitIndex=0; suitIndex<Constants.NUM_SUITS; suitIndex++) {
				 
				 if(dataModel.signalHandler.playerStrongSignaledNoCardsOfSuit(playerIndex, suitIndex)) {
					 continue;
				 }
				 
				 boolean mellowBidderSignalledNo[] = new boolean[Constants.NUM_RANKS];
				 boolean nonMellowBidderSignalledNo[] = new boolean[Constants.NUM_RANKS];
				 
				 //Mellow bidder signals:
				 if(dataModel.playerMadeABidInRound(playerIndex)
						 && dataModel.getBid(playerIndex) == 0) {

					 for(int rankIndex = 0; rankIndex<Constants.NUM_RANKS; rankIndex++) {
						 
						
						 if( ! isSignalledCardGoodForMellowBidder(dataModel, playerIndex, DataModel.getCardString(rankIndex, suitIndex), false)) {
							 mellowBidderSignalledNo[rankIndex] = true;
						 }
							 
					 }
				 }
				 
				 if(dataModel.playerMadeABidInRound(playerIndex)
						 && (dataModel.getBid(playerIndex) > 0
						|| dataModel.burntMellow(playerIndex))) {
					
					int max = dataModel.signalHandler.getMaxCardRankSignal(playerIndex, suitIndex);
				 	int min = dataModel.signalHandler.getMinCardRankSignal(playerIndex, suitIndex);
				 	

				 	for(int rankIndex = DataModel.RANK_TWO; rankIndex< Math.max(min, DataModel.RANK_TWO) - 1; rankIndex++) {
				 		nonMellowBidderSignalledNo[rankIndex] = true;
				 	}
				 	
				 	
				 	for(int rankIndex = Math.min(max, DataModel.ACE) + 1; rankIndex<= DataModel.ACE; rankIndex++) {
				 		nonMellowBidderSignalledNo[rankIndex] = true;
				 	}
				 }
				 
				 for(int rankIndex = 0; rankIndex<Constants.NUM_RANKS; rankIndex++) {

					 String card = DataModel.getCardString(rankIndex, suitIndex);
					 
					 if(mellowBidderSignalledNo[rankIndex] == false
						&& nonMellowBidderSignalledNo[rankIndex] == false
						&& dataModel.isCardPlayedInRound(card) == false
						&& dataModel.hasCard(card) == false) {
						 playerPos[playerIndex].add(card);
					 }
				 }
				 
			 }
			 
			 //Hack to manually remove some possibilities:
			 /*
			 //Once done with hack, incorporate it in the signals handler...
			 if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "KS JS 7S 2S JC TD 8D ")
					 && playerIndex == 2) {
				 
				 String tmpCard = "JH";
				 System.out.println("Debug HACK!");
				 if(playerPos[playerIndex].contains(tmpCard)) {
					 playerPos[playerIndex].remove(tmpCard);
					 System.out.println("Removed " + tmpCard + "!");
				 } else {
					 System.err.println("TmpCard did not need to be removed!");
					 System.exit(1);
				 }
				 
			 }*/
			 //End Hack to manually remove some possibilities
			 
			 //Second hack to manually remove some possibilities:
			 if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "8S 4S KH QH 9H 5C 2C TD 7D 6D ")
					 && playerIndex == 2) {
				 
				 for(int rank=0; rank<Constants.NUM_RANKS; rank++) {
					 String tmpCard = DataModel.getCardString(rank, Constants.CLUB);
					 
					 if(playerPos[playerIndex].contains(tmpCard)) {
						 playerPos[playerIndex].remove(tmpCard);
						 System.out.println("Debug removed " + tmpCard + " from Michael's hand!");
					 }
				 }
				 
			 }
			 //End Hack.
			 
			 if(DebugFunctions.currentPlayerHoldsHandDebug(dataModel, "AS KS JS 7S QH JH 7H 6H TC 9C ")
					 && playerIndex == 1) {
				 
				 for(int rank=0; rank<Constants.NUM_RANKS; rank++) {
					 String tmpCard = DataModel.getCardString(rank, Constants.DIAMOND);
					 
					 if(DataModel.getRankIndex(tmpCard) == DataModel.ACE) {
					 
						 if(playerPos[playerIndex].contains(tmpCard)) {
							 playerPos[playerIndex].remove(tmpCard);
							 System.out.println("Debug removed " + tmpCard + " from Phil's hand!");
						 }
					 }
				 }
				 
			 }
			 
		 }
		 
		 for(int i=0; i<Constants.NUM_PLAYERS; i++) {
			 if(i == Constants.CURRENT_AGENT_INDEX) {
				 continue;
			 }
			System.out.println("Possible cards for " + dataModel.getPlayers()[i] + ": ");
			
			Object array[] = playerPos[i].toArray();
			
			String array2[] = new String[array.length];
			
			for(int j=0; j<array2.length; j++) {
				array2[j] = array[j].toString();
			}
			
			String sortedArray[] = CardStringFunctions.sort(array2);
			CardStringFunctions.printCards(sortedArray);
		 }
		 
		 setupPossibilitySets();
	 }
	
	
	//Constructor for debug purposes:
	//TODO: test
	public void setupCardPossibilities(HashSet<String> playerPos[]) {
		this.playerPos = playerPos;

		 setupPossibilitySets();
	}
	 
	 public static final int NUM_POSSIBILITIES = 3;
	 // 0 = no
	 // 1 = yes
	 // 2 = doesn't matter.
	 
	 //First index is for current player, but current player always knows what's in his hand, so whatever!
	 //Second index is for LHS player
	 //Third index is for PARTNER player
	 //Fourth index is for RHS player
	 //Last index is for an array of actual cards:
	 public String otherPlayerPosSet[][][][][] = new String[1][NUM_POSSIBILITIES][NUM_POSSIBILITIES][NUM_POSSIBILITIES][];
	 
	 public static final int NO_INDEX = 0;
	 public static final int YES_INDEX = 1;
	 public static final int ANY_INDEX = 2;
	 

	 private HashSet<String> playerNOTPossibleAndNotCurPlayer[] = new HashSet[Constants.NUM_PLAYERS];
	 
	 //pre: HashSet<String> playerPos is defined
	 //post: 
	 public void setupPossibilitySets() {
		 
		 //HashSet<String> playerPos[] 
		 
		 HashSet<String> allCardsInPlayNoCurPlayer = new HashSet<String>();
		 
		 for(int i=0; i<playerPos.length; i++) {
			 Iterator<String> it = playerPos[i].iterator();
			 
			 while(it.hasNext()) {
				 
				 String tmpCard = it.next();
				 
				 if( ! allCardsInPlayNoCurPlayer.contains(tmpCard) ) {
					 allCardsInPlayNoCurPlayer.add(tmpCard);
				 }
			 }
		 }
		 
		 for(int i=0; i<playerNOTPossibleAndNotCurPlayer.length; i++) {
			 if(i == 0) {
				 playerNOTPossibleAndNotCurPlayer[0] = allCardsInPlayNoCurPlayer;
			 }
			 
			 playerNOTPossibleAndNotCurPlayer[i] = new HashSet<String>();
			 
			 Iterator<String> it = allCardsInPlayNoCurPlayer.iterator();
			 
			 while(it.hasNext()) {
				 
				 String tmpCard = it.next();
				 
				 if( ! playerPos[i].contains(tmpCard) ) {
					 playerNOTPossibleAndNotCurPlayer[i].add(tmpCard);
				 }
			 }
		 }
		 
		 
		 for(int j=0; j<otherPlayerPosSet[0].length; j++) {
			 for(int k=0; k<otherPlayerPosSet[0][0].length; k++) {
				 for(int m=0; m<otherPlayerPosSet[0][0][0].length; m++) {
					 
					 HashSet<String> ret = allCardsInPlayNoCurPlayer;
					 
					 if(j == NO_INDEX) {
						 ret = Intersection(ret, playerNOTPossibleAndNotCurPlayer[1]);
						 
					 } else if(j == YES_INDEX) {
						 ret = Intersection(ret, playerPos[1]);
						 
					 }
					 

					 if(k == NO_INDEX) {
						 ret = Intersection(ret, playerNOTPossibleAndNotCurPlayer[2]);
						 
					 } else if(k == YES_INDEX) {
						 ret = Intersection(ret, playerPos[2]);
						 
					 }

					 if(m == NO_INDEX) {
						 ret = Intersection(ret, playerNOTPossibleAndNotCurPlayer[3]);
						 
					 } else if(m == YES_INDEX) {
						 ret = Intersection(ret, playerPos[3]);
						 
					 }
					 
					 //otherPlayerPosSet[0][j][k][m] = ret;
					
					Object array[] = ret.toArray();
					
					otherPlayerPosSet[0][j][k][m] = new String[array.length];
					
					for(int p=0; p<otherPlayerPosSet[0][j][k][m].length; p++) {
						otherPlayerPosSet[0][j][k][m][p] = array[p].toString();
					}
					
					//Might as well sort the cards for readability:
					if(! CardStringFunctions.listContainsFakeCards(otherPlayerPosSet[0][j][k][m])) {
						otherPlayerPosSet[0][j][k][m] = CardStringFunctions.sort(otherPlayerPosSet[0][j][k][m]);
					} else {
						
						otherPlayerPosSet[0][j][k][m] = sortTestHand(otherPlayerPosSet[0][j][k][m]);
					}
					
					//These print statements describe what's in the sets for debug purposes:
					//(But they clutter the output, so I commented them out.)
					/*
					System.out.println("(0 " + "," + j +", " + k + ", " + m + "):");
					if(otherPlayerPosSet[0][j][k][m].length > 0) {
						CardStringFunctions.printCards(otherPlayerPosSet[0][j][k][m]);
					} else {
						System.out.println("(empty)");
					}
					*/
					
				 }
			}
		 }
	 }
	 
	 public static HashSet<String> Intersection(HashSet<String> a, HashSet<String> b) {
		 HashSet<String> ret = new HashSet<String>();
		 
		 Iterator<String> it = a.iterator();
		 
		 while(it.hasNext()) {
			 
			 String tmpCard = it.next();
			 
			 if( b.contains(tmpCard) ) {
				 ret.add(tmpCard);
			 }
		 }
		 
		 
		 return ret;
	 }
	 
	 
	 
	 
	 public static boolean isSignalledCardGoodForMellowBidder(DataModel dataModel, int playerIndex, String card, boolean debug) {
		 if(dataModel.isCardPlayedInRound(card) == false) {
			 
			 int suitIndex = CardStringFunctions.getIndexOfSuit(card);
			 int rankIndex = DataModel.getRankIndex(card);
			 
			 if( dataModel.getCardsCurrentlyHeldByPlayers()[playerIndex]
					 [suitIndex]
					 [rankIndex]
					== VoidSignalsNoActiveMellows.MELLOW_PLAYER_SIGNALED_NO) {
				 
				 if(debug) {
					 System.err.println("NOPE! Mellow bidder (" + dataModel.getPlayers()[playerIndex] + ") doesn't have the " + card + ".");
				 }
				 return false;
			 }
			 
		 }
		 
		 return true;
	 }
	 
	 public static boolean isCardSignalledGoodForNonMellowBidder(DataModel dataModel, String card, int playerIndex, int minRank, int maxRank, boolean debug) {
		 
		 
		 if(DataModel.getRankIndex(card) < minRank) {

			 if(debug) {
				 System.err.println("NOPE! Mellow player (" + dataModel.getPlayers()[playerIndex] + ") should have higher card than " + card + ".");
			 }
			 
			 return false;
		 } else if(DataModel.getRankIndex(card) > maxRank) {
			
			 if(debug) {
				 System.err.println("NOPE! Mellow player (" + dataModel.getPlayers()[playerIndex] + ") should have lower card than " + card + ".");
			 }
			 
			 return false;
		 }
		 

		 return true;
	 }
	 
	 public static String[] sortTestHand(String array[]) {
			
		String ret[] = new String[array.length];
		
		for(int i=0; i<ret.length; i++) {
			ret[i] = array[i];
		}
		
		for(int i=0; i<ret.length; i++) {
			
			int tmpBestIndex = i;
			
			for(int j=i+1; j<ret.length; j++) {
				
				if(ret[tmpBestIndex].compareTo(ret[j]) > 0) {
					tmpBestIndex = j;
				}
				
			}
			String tmp = ret[i];
			ret[i] = ret[tmpBestIndex];
			ret[tmpBestIndex] = tmp;
		}
		
		return ret;
	}
}

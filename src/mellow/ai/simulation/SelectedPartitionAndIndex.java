package mellow.ai.simulation;

import mellow.Constants;

public class SelectedPartitionAndIndex {

	public SelectedPartitionAndIndex() {
		for(int i=0; i<Constants.NUM_PLAYERS; i++) {
			comboIndex[i] = 0;
			for(int j=0; j<Constants.NUM_SUITS; j++) {
				suitsTakenByPlayer[i][j] = 0;
			}
		}
	}
	
	public long comboIndex[] = new long[Constants.NUM_PLAYERS];
	
	public int suitsTakenByPlayer[][] = new int[Constants.NUM_PLAYERS][Constants.NUM_SUITS];
	
	
	public void setSuitsTakenByPlayers(int playerIndex, int suitsTaken[]) {
		//TODO: make hard-copy if needed...
		suitsTakenByPlayer[playerIndex] = suitsTaken;
	}
	
	public void setPlayerComboNumber(int playerIndex, long indexCombo) {
		this.comboIndex[playerIndex] = indexCombo; 
	}
	
	public void giveWhatsLeftToNextPlayer(int playerIndex, int suitsTaken[]) {
		setSuitsTakenByPlayers(playerIndex, suitsTaken);
		setPlayerComboNumber(playerIndex, 0);
		
		
	}
	
}

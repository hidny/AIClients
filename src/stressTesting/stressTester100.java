package stressTesting;
import clientPlayers.ClientStarter;
import clientPlayers.ClientJoinerStarter;

public class stressTester100 {

	//TODO: stress test mellow
	public static int NUM_PLAYERS = 100;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String players[][] = new String[NUM_PLAYERS][2];
		
		for(int i=0; i < NUM_PLAYERS; i++) {
			players[i][0] = "Connect4AI" + i;
			players[i][1] = "aigame" + (i/2);
		}
		try {
		
		for(int i=0; i<NUM_PLAYERS/2; i++) {
			ClientStarter.main(players[2*i]);
		}
		Thread.sleep(1000);
		for(int i=0; i<NUM_PLAYERS/2; i++) {
			ClientJoinerStarter.main(players[2*i+1]);
		}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

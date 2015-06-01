package stressTesting;
import clientPlayers.ClientStarter;
import clientPlayers.ClientJoinerStarter;

public class stressTester {
//TODO: stress test mellow
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String player1[] = {"Connect4AI1", "aigame"};
		String player2[] = {"Connect4AI2", "aigame"};
		String player3[] = {"Connect4AI3", "aigame2"};
		String player4[] = {"Connect4AI4", "aigame2"};
		try {
		
		ClientStarter.main(player1);
		ClientStarter.main(player3);
		Thread.sleep(1000);
		ClientJoinerStarter.main(player2);
		ClientJoinerStarter.main(player4);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

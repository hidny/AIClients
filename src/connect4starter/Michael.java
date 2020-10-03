package connect4starter;

import clientPlayers.ClientStarter;

public class Michael {

	public static void main(String[] args) {
		String autoArgs[] = {"connect_four", "Connect4Guy", "connect4py", "0", "fast"};
		try {
			ClientStarter.main(autoArgs);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

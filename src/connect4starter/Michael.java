package connect4starter;

import clientPlayers.ClientStarter;

public class Michael {

	public static void main(String[] args) {
		String autoArgs[] = {"connect_four", "Michael", "connect4", "0", "fast"};
		try {
			ClientStarter.main(autoArgs);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

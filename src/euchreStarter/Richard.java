package euchreStarter;

import clientPlayers.ClientJoinerStarter;

public class Richard {
	public static void main(String[] args) {
		String autoArgs[] = {"euchre", "Richard", "euchrepy", "0", "fast"};
		try {
			ClientJoinerStarter.main(autoArgs);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

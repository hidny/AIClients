package euchreStarter;

import clientPlayers.ClientJoinerStarter;
import clientPlayers.ClientStarter;

public class Phil {

	public static void main(String[] args) {
		String autoArgs[] = {"euchre", "Phil", "euchrepy", "0", "fast"};
		try {
			ClientJoinerStarter.main(autoArgs);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

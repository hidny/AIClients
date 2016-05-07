package mellowStarter;

import clientPlayers.ClientJoinerStarter;
import clientPlayers.ClientStarter;

public class Phil {

	public static void main(String[] args) {
		String autoArgs[] = {"mellow", "Phil", "mellowpy", "0", "fast"};
		try {
			ClientJoinerStarter.main(autoArgs);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

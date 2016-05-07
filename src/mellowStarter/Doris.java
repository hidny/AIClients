package mellowStarter;

import clientPlayers.ClientJoinerStarter;

public class Doris {

	public static void main(String[] args) {
		String autoArgs[] = {"mellow", "Doris", "mellowpy", "0", "fast"};
		try {
			ClientJoinerStarter.main(autoArgs);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

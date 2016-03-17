package reversiStarter;

import clientPlayers.ClientStarter;

public class Michael {

	public static void main(String[] args) {
		String autoArgs[] = {"reversi", "Michael", "reversipy", "0", "fast"};
		try {
			ClientStarter.main(autoArgs);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

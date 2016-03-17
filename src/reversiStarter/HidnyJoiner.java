package reversiStarter;

import clientPlayers.ClientJoinerStarter;

public class HidnyJoiner {

	public static void main(String[] args) {
		String autoArgs[] = {"reversi", "Hidny", "reversipy", "0", "fast"};
		try {
			ClientJoinerStarter.main(autoArgs);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

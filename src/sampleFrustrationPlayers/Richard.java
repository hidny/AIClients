package sampleFrustrationPlayers;

import clientPlayers.ClientJoinerStarter;

public class Richard {
	public static void main(String[] args) {
		String autoArgs[] = {"frustration", "Richard", "frustrationing", "0", "fast"};
		try {
			ClientJoinerStarter.main(autoArgs);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

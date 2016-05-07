package mellowStarter;

import clientPlayers.ClientStarter;

//Will host and wait until there's a full house...

//In fast speed setting: 1 test game takes 70 seconds. (Pretty fast!)
public class Michael {
	public static void main(String[] args) {
		String autoArgs[] = {"mellow", "Michael", "mellowpy", "0", "fast"};
		try {
			ClientStarter.main(autoArgs);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

package clientPlayers;

public class GameServerAck {

	public static int HELLO = 1;
	public static int GAMECREATED = 2;
	public static int GAME_ROOM_UPDATE = 3;
	public static int GAME_STARTED = 4;
	public static int DONTKNOW = -1;
	public static int FULL_HOUSE = 5;
	
	public static int getBasicTypeOfMessage(String resp, String gameName, String roomName) {
		//Hello, Michael. Type /help or /h for help.
		if(resp.startsWith("Hello, ")) {
			return HELLO;
		}
		
		if(resp.startsWith(gameName + ": " + roomName)) {
			String temp = resp.substring(resp.indexOf("(") + 1, resp.indexOf(")"));
			String array[] = temp.split("/");
			
			int num1 = Integer.parseInt(array[0]);
			int num2 = Integer.parseInt(array[1]);
			
			
			//Check if it's a full house:
			if(num1 >= num2) {
				return FULL_HOUSE;
			} else {
				return GAMECREATED;
			}
		}
		
		if(resp.startsWith(gameName + ": " + roomName)) {
			return GAME_ROOM_UPDATE;
		}
		
		if(resp.startsWith("Starting game in")) {
			return GAME_STARTED;
		}
		return DONTKNOW;
		
	}
	
}

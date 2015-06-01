package clientPlayers;

public interface GamePlayerInterface {
	public String getClientResponse(String gameName, String serverMessage);
	public void resetName(String name);
}

package clientPlayers;

import java.io.DataOutputStream;
import java.io.IOException;

public class GameServerCommands {

    public static void createGame(DataOutputStream outToServer, String game, String gameName, String pass) throws IOException  {
    	sendMessage(outToServer, "/create " + game + " " + gameName + " " + pass);
    }
    
    public static void sendInvite(DataOutputStream outToServer, String playerName) throws IOException {
    	sendMessage(outToServer, "/invite " + playerName);
    }
    
    public static void joinGame(DataOutputStream outToServer, String gameName, String password) throws IOException {
    	sendMessage(outToServer, "/join " + gameName + " " + password);
    }
    
    public static void acceptInvite(DataOutputStream outToServer) throws IOException {
    	sendMessage(outToServer, "/yes");
    }
    
    public static void declineInvite(DataOutputStream outToServer) throws IOException {
    	sendMessage(outToServer, "/no");
    }
    

    public static void start(DataOutputStream outToServer) throws IOException {
    	sendMessage(outToServer, "/start");
    }
    
    public static void glhf(DataOutputStream outToServer) throws IOException {
    	sendMessage(outToServer, "glhf");
    }
    
    public static void u2(DataOutputStream outToServer) throws IOException {
    	sendMessage(outToServer, "u2");
    }
    
    
    public static void gg(DataOutputStream outToServer) throws IOException {
    	sendMessage(outToServer, "gg");
    }

   
    
    public static void makeFunOfOpponent(DataOutputStream outToServer, String message) throws IOException {
    	sendMessage(outToServer, "lol");
    }
    
    public static void rage(DataOutputStream outToServer, String message) throws IOException {
    	sendMessage(outToServer, "you suck!");
    }
    
    public static synchronized void sendMessage(DataOutputStream outToServer, String message) throws IOException {
    	outToServer.writeBytes(message + '\n');
    	System.out.println("Sending message: " + message);
    }
}

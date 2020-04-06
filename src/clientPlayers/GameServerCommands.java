package clientPlayers;

import java.io.DataOutputStream;
import java.io.IOException;

public class GameServerCommands {
	
	public static void createGameWithScoreAndDealerAndRigged1stDeck(DataOutputStream outToServer, String game, String gameName, String pass, int team1Score, int team2Score, int dealerIndex, String riggedFirstDeck) throws IOException  {
		String riggedDeckParam = "-fulldeckrig";
		if(isCustomizeHands(riggedFirstDeck)) {
			riggedDeckParam = "-handrig";
		} else {
			riggedDeckParam = "-fulldeckrig";
		}
		sendMessage(outToServer, "/create " + game + " " + gameName + " " + pass + " " + team1Score + " " + team2Score + " " + dealerIndex + " " + riggedDeckParam + " " + riggedFirstDeck);
	}
	
	public static void createGameWithScoreAndDealer(DataOutputStream outToServer, String game, String gameName, String pass, int team1Score, int team2Score, int dealerIndex) throws IOException  {
	    sendMessage(outToServer, "/create " + game + " " + gameName + " " + pass + " " + team1Score + " " + team2Score + " " + dealerIndex);
	}
	    
    public static void createGame(DataOutputStream outToServer, String game, String gameName, String pass) throws IOException  {
    	sendMessage(outToServer, "/create " + game + " " + gameName + " " + pass);
    }
    
    public static void createGameWithScore(DataOutputStream outToServer, String game, String gameName, String pass, int team1Score, int team2Score) throws IOException  {
    	sendMessage(outToServer, "/create " + game + " " + gameName + " " + pass + " " + team1Score + " " + team2Score);
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
    
    


	 public static final int STANDARD_DECK_SIZE = 52;
	 public static final int CARD_ASCII_LENGTH = 2;

    //Check the format of the param
    //Full deck looks like this:
    //3D TD 5D 7C 7S 6S 5C JS 5H JC 8S 2S 8D QH JD KD 8H TS TC TH 7D 2C AC 4C 6D QS QC AS 2H KC AD 9D 6H JH 9C 3S 3C KS QD 5S AH 4H 9S 4D 8C 9H 3H KH 7H 6C 2D 4S 

    //Customized hands look like this:
    //[AS ADKSQSJSKD2C][][3C][2D3D,4D]
    
    //I want to make my deck rigging implied because it's less annoying to change.
    //It's probably more annoying to understand, but ¯\_(ツ)_/¯
	    
    private static boolean isCustomizeHands(String riggedFirstDeck) {
    	System.out.println(riggedFirstDeck.replaceAll("[^\\[]", ""));
    	if(riggedFirstDeck.replaceAll("[^\\[]", "").length() > 2 ) {
    		//At least 2 hands, so it's customized hands...
    		return true;
    	}
    	
    	String cards = riggedFirstDeck.replaceAll("[^0-9a-zA-Z]", "");

		//System.out.println(deckPart);

    	//Check if there's 52 cards or STANDARD_DECK_SIZE:
		if(cards.length() != CARD_ASCII_LENGTH * STANDARD_DECK_SIZE) {
			System.out.println("ERROR: didn't get the right number of cards to rig (got deckPart.length() cards)");
			return true;
		} else {
			return false;
		}
    }
}

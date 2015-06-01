package clientPlayers;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;


public class ServerRequestHandler extends Thread {

	public static final String EOT = "**end of transmission**\n";
	public static final String EOC = "Goodbye.";

	
	public static int INITIALSTATE = 0;
	public static int ERRORSTATE = -1;
	
    private Socket socket = null;
   
    private BufferedReader inFromServer;
    private DataOutputStream outToServer;
    
    private boolean connected = true;
    
    private int clientState;
    
    private String desiredName;
    private String givenName;
    
    private String gameName;
    
    private String roomName;
    private boolean create;
    
    private boolean gameStarted = false;
    
    private int aiLevel = 0;
    private boolean isFast = false;
    
    public GamePlayerInterface gamePlayer;
    
    
    public ServerRequestHandler(Socket socket, String desiredName, String gameName, String roomName, boolean create, int aiLevel, boolean isFast) {
        super("ClientListener");
        this.socket = socket;
        this.clientState = 0;
        this.desiredName = desiredName;
        
        this.gameName = gameName;
        this.roomName = roomName;
        this.create = create;
        
        this.aiLevel = aiLevel;
        this.isFast = isFast;
        
        this.gamePlayer =AIFactory.getAIPlayer(this.gameName, this.aiLevel, this.isFast);
    }
    
    public boolean isConnected() {
    	return connected;
    }
    
    public int getClientState() {
    	return clientState;
    }
    

    public void run() {
    	String currentMsgFromServer;
    	String currentServerLine;
    	
    	try {

    		inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        	outToServer = new DataOutputStream(socket.getOutputStream());
			
    		outToServer.writeBytes(desiredName + '\n');
			
        	while(this.connected) {
				currentMsgFromServer = "";
				while(this.connected) {
					currentServerLine = inFromServer.readLine();
					currentMsgFromServer = currentMsgFromServer + currentServerLine + '\n';
					currentMsgFromServer = dealWithServerInput(currentMsgFromServer);
				}
				
			}
			
			inFromServer.close();
			socket.close();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	
    }
    
   
   // private boolean inGame = false;
    
   
    //This will make the client transition through states.
    public String dealWithServerInput(String currentMsgFromServer) throws IOException {
    	
    	
    	while (currentMsgFromServer.contains(EOT)) {
    		String message = currentMsgFromServer.substring(0, currentMsgFromServer.indexOf(EOT));
    		currentMsgFromServer = currentMsgFromServer.substring(currentMsgFromServer.indexOf(EOT) + EOT.length());
    		
    		//get rid of last \n:
    		if(message.endsWith("\n")) {
    			message = message.substring(0, message.length() - 1);
    		}
    		
    		if(gameStarted == false) {
	    		dealWithGameSetup(message);
	    	} else if(gameStarted) {
	    		makeAIPlayGame(message);
	    	}
    	}
    	return currentMsgFromServer;
    	
    }
    
    public void dealWithGameSetup(String serverResponse) throws IOException {
    	int type = GameServerAck.getBasicTypeOfMessage(serverResponse, this.gameName, this.roomName);

		if(type == GameServerAck.HELLO) {
			//RENAME to the name the server gives you: (Sorry Richard :P)
			this.givenName = serverResponse.split(" ")[1];
			this.givenName = this.givenName.trim();
			//get rid of last the dot at the end of the sentence:
    		if(this.givenName.endsWith(".") ) {
    			this.givenName = this.givenName.substring(0, this.givenName.length() - 1);
    		}
    		
			if(this.gamePlayer != null) {
				gamePlayer.resetName(this.givenName);
			}
			
			while(this.givenName.endsWith("\n") || this.givenName.endsWith(".")) {
				this.givenName = this.givenName.substring(0, this.givenName.length() - 1);
			}
			
    		if(create) {
    			GameServerCommands.createGame(outToServer, this.gameName, roomName, "");
    		} else {
    			GameServerCommands.joinGame(outToServer, roomName, "");
    		}
    	}
    	
    	
    	if(type == GameServerAck.GAME_ROOM_UPDATE) {
    		if(create == true) {
        		GameServerCommands.sendMessage(outToServer, "hi");
    		} else {
    			GameServerCommands.sendMessage(outToServer, "hi host");
    		}
    	}
    	
    	if(gameStarted == false && create == true && type == GameServerAck.FULL_HOUSE) {
    		
    		try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		GameServerCommands.start(outToServer);
    		gameStarted = true;
    	}
    	
    	if(type == GameServerAck.GAME_STARTED) {
    		System.out.println("Game started!");
    		gameStarted = true;
    	}
    }

    
    //pre: server message is only 1 transmission
    public void makeAIPlayGame(String serverMessage)  throws IOException {
    	String resp = gamePlayer.getClientResponse(gameName, serverMessage);
    	
    	//TODO: implement rules for slowdown in mellow aI!! (Not here!): (so human users can see what's going on)
    		//1500 millies to lead
    		//300 millies to follow lead
    	if(resp != null) {
    		if(isFast == false) {
    			try {
    				Thread.sleep(1000);
    			} catch (InterruptedException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}
    		
    		if(resp != null && resp.length() > 0) {
    			GameServerCommands.sendMessage(outToServer, resp);
    		}
    	}
    	
    }
    
    
}

package clientPlayers;

import java.net.*;


public class ClientJoinerStarter  extends Thread {
	
	//TODO: make it specifically for mellow OR for multiple games.
	
	//Chat program:
	//http://www.java-gaming.org/index.php?topic=22911.0
	
	//I read http://stackoverflow.com/questions/6672413/in-what-way-is-java-net-socket-threadsafe
	//to make sure I can read and write on the same socket 
	
	public static final String EOT = "**end of transmission**";
	public static final String EOC = "Goodbye";
	
	public static Thread currentThread = Thread.currentThread();
	
	
    //TODO: accept a number as an args so I can have 100 run at once.
    public static void main(String args[]) throws Exception {
    	String gameName = "mellow";
    	String desiredName = "mellowAI2";
    	String roomName = "aigame";
    	if(args.length >= 0) {
    		gameName = args[0].toLowerCase();
    	}
    	
    	if(args.length >= 3) {
    		desiredName = args[1];
    		roomName = args[2];
    	}
    	int aiLevel = 0;
    	if(args.length >= 4) {
    		aiLevel = Integer.parseInt(args[3]);
    	}
    	boolean isFast = false;
    	if(args.length >= 5) {
    		if(args[4].equals("fast")) {
    			isFast = true;
    		}
    	}
    	
    	ClientJoinerStarter joiner = new ClientJoinerStarter(gameName, desiredName, roomName, aiLevel, isFast);
    	joiner.start();
    	
    }
    
    private String gameName;
    private String desiredName;
    private String roomName;
    private int aiLevel;
    private boolean isFast;
    
    public ClientJoinerStarter(String gameName, String desiredName, String roomName, int aiLevel, boolean isFast) {
    	this.gameName = gameName.toLowerCase();
    	this.desiredName = desiredName;
    	this.roomName = roomName;
    	this.aiLevel = aiLevel;
    	this.isFast = isFast;
    }
    
    public void run() {
    	ServerRequestHandler serverListener = null;
		
		try {
			Socket clientSocket = new Socket("localhost", 6789);
			
			serverListener = new ServerRequestHandler(clientSocket, desiredName, this.gameName, roomName, false, aiLevel, isFast);
			
			serverListener.run();
			
			
			clientSocket.close();
		 } catch (UnknownHostException e) {
                System.err.println("Trying to connect to unknown host: " + e);
		 } catch(java.net.SocketException e) {
			 
			System.err.println("ERROR socket closed unexpectedly: " + e);
			e.printStackTrace();
			System.exit(1);
			 
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
    }
   
}
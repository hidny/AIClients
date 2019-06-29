package clientPlayers;

import java.net.*;


public class ClientStarter  extends Thread {
	
	//Chat program:
	//http://www.java-gaming.org/index.php?topic=22911.0
	
	//I read http://stackoverflow.com/questions/6672413/in-what-way-is-java-net-socket-threadsafe
	//to make sure I can read and write on the same socket 
	
	public static final String EOT = "**end of transmission**";
	public static final String EOC = "Goodbye";
	
	public static Thread currentThread = Thread.currentThread();
	
	
    //TODO: accept a number as an args so I can have 100 run at once.
    public static void main(String args[]) throws Exception {
    	
    	//TODO: handle args the proper way
    	
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
    	
    	int startRed = 0;
    	int startBlue = 0;
    	int dealerIndex = -1;
    	
    	if(args.length >= 6) {
    		startRed = Integer.parseInt(args[5]);
    		startBlue = Integer.parseInt(args[6]);
    	}
    	
    	if(args.length >=7) {
    		dealerIndex = Integer.parseInt(args[7]);
    	}
    	
    	
    	ClientStarter starter = new ClientStarter(gameName, desiredName, roomName, aiLevel, isFast, startRed, startBlue, dealerIndex);
		
    	starter.start();
    }
    
    private String gameName;
    private String desiredName;
    private String roomName;
    private int aiLevel;
    private boolean isFast;
    
    //Mellow
    private int startRed = 0;
    private int startBlue = 0;
    private int dealerIndex = -1;
    
    public ClientStarter(String gameName, String desiredName, String roomName, int aiLevel, boolean isFast, int startRed, int startBlue, int dealerIndex) {
    	this.gameName = gameName.toLowerCase();
    	this.desiredName = desiredName;
    	this.roomName = roomName;
    	this.aiLevel = aiLevel;
    	this.isFast = isFast;
    	this.startRed = startRed;
    	this.startBlue = startBlue;
    	this.dealerIndex = dealerIndex;
    }
    
    public void run() {
        System.out.println("Hello from a thread!");

    	ServerRequestHandler serverListener = null;
		
		try {
			Socket clientSocket = new Socket("localhost", 6789);
			
			//For now, the host is player 1 and the joiner is player 2... and there's no way to change it. :(
			serverListener = new ServerRequestHandler(clientSocket, desiredName, this.gameName, roomName, true, aiLevel, isFast, this.startRed, this.startBlue, this.dealerIndex);
			
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
package connect4ai;


public class Connect4Listener implements clientPlayers.GamePlayerInterface {

	public static int DEPTH = 10;
	private String currentPlayerName = null;
	BoardPosition currentPos = null;
	
	public Connect4Listener(long level, boolean isFast) {
		if(level > 10) {
			DEPTH = (int)level;
		}
	}
	
	public String getClientResponse(String gameName, String serverMessage) {
		//TODO: AHHH
		int playerNumber = -1;
		System.out.println("Testing connect 4: " + serverMessage);
		
		boolean playTurn = false;
		if(serverMessage.contains(this.currentPlayerName + ": which column?")) {
			playTurn = true;
		}
		if(getConnect4PosIfApplicable(serverMessage, gameName) != null) {
			currentPos = getConnect4PosIfApplicable(serverMessage, gameName);
		}
		
		String msgToSend = null;
		if(currentPos != null && playTurn == true ) {
			System.out.println("Time to make a move!");
			
			int move = SimpleGetBestPosition.getBestMove(currentPos, DEPTH);
			
			System.out.println("Moving to column " + move);
			msgToSend = "/move " + move;
		}
		
		return msgToSend;
	}
	
	
	public void resetName(String name) {
		this.currentPlayerName = name;
		//Do nothing... the AI could figure out if it's true some other way.
	}
	
	public static BoardPosition getConnect4PosIfApplicable(String resp, String gameName) {
		BoardPosition pos = null;
		//System.out.println("**Get connect 4 state!");
		//System.out.println("**" + resp);
		
		
		if(resp.startsWith("From connect 4: \n|")) {
			System.out.println("In position!");
			pos = getPosition(resp);
			System.out.println("Printing position:");
			pos.printPos();
		}
		
		
		return pos;
	}
	
	public static BoardPosition getPosition(String resp) {
		short ret[][] = new short[BoardPosition.HEIGHT][BoardPosition.WIDTH];
		
		int numRed = 0;
		int numBlack = 0;
		
		int currentindex = 0;
		for(int i=0; i<BoardPosition.HEIGHT; i++) {
			for(int j=0; j<BoardPosition.WIDTH; j++) {
				currentindex = resp.indexOf('|', currentindex);
				if(resp.charAt(currentindex + 1) == '\n') {
					//Do nothing!
					j--;
				} else if(resp.charAt(currentindex + 1) == 'R') {
					ret[i][j] = BoardPosition.RED;
					numRed++;
				} else if(resp.charAt(currentindex + 1) == 'B') {
					ret[i][j] = BoardPosition.BLACK;
					numBlack++;
				} else if(resp.charAt(currentindex + 1) == ' ') {
					ret[i][j] = BoardPosition.EMPTY;
				}
				currentindex += 2;
			}
		}
		boolean isRedTurn = false;
		if(numRed == numBlack) {
			isRedTurn = true;
		} else if(numRed - 1 == numBlack) {
			isRedTurn = false;
		} else {
			System.out.println("In getPosition(). Can\'t figure out the position!");
			System.exit(1);
		}
		BoardPosition pos = new BoardPosition(ret, isRedTurn);
		
		return pos;
		
	}

	
}

package reversiai;

import reversiai.Position;
import reversiai.reversiAiTorturer.SimpleGetBestPositionTorture;

public class ReversiListener implements clientPlayers.GamePlayerInterface {

	private String currentPlayerName = null;
	Position currentPos = null;
	
	private long aiLevel = 0;
	public static long TORTURE = 1;
	
	public ReversiListener(boolean isFast) {
		this(0, isFast);
	}
	public ReversiListener(long aiLevel, boolean isFast ) {
		this.aiLevel = aiLevel;
	}
	
	public String getClientResponse(String serverMessage) {
		//TODO: AHHH
		int playerNumber = -1;
		System.out.println("Testing reversi: " + serverMessage);
		
		boolean playTurn = false;
		if(serverMessage.contains("From Reversi(private): Your turn")) {
			playTurn = true;
		}
		if(getReversiPosIfApplicable(serverMessage) != null) {
			currentPos = getReversiPosIfApplicable(serverMessage);
		}
		
		long tStart = System.currentTimeMillis();
		long LIMIT_TIME = 1000;
		
		long timeLimit = tStart + LIMIT_TIME;
		String msgToSend = null;

		int depth = 1;
		int maxDepth = Position.NUM_SPACES;
		if(currentPos != null) {
			maxDepth = Position.NUM_SPACES - currentPos.getNumPegsUsed();
		}
		
		while(currentPos != null && playTurn == true && System.currentTimeMillis() < timeLimit && depth<=maxDepth) {
			System.out.println("Deciding a move with depth: " + depth);
			
			String move = "";
			//TODO.
			if(aiLevel == TORTURE) {
				 move = SimpleGetBestPositionTorture.getBestMove(currentPos, depth, timeLimit);
			} else {
				 move = SimpleGetBestPosition.getBestMove(currentPos, depth, timeLimit);
			}
			
			if(System.currentTimeMillis() < timeLimit) {
				System.out.println("Moving to " + move);
				msgToSend = "/move " + move;
			}
			depth++;
		}
		
		return msgToSend;
	}
	
	
	public void resetName(String name) {
		this.currentPlayerName = name;
		//Do nothing... the AI could figure out if it's true some other way.
	}
	
	public static Position getReversiPosIfApplicable(String resp) {
		Position pos = null;
		//System.out.println("**Get connect 4 state!");
		//System.out.println("**" + resp);
		
		
		if(resp.startsWith("From Reversi(public): Dark to move:") || resp.startsWith("From Reversi(public): White to move:")) {
			pos = getPosition(resp);
			
		}
		
		
		return pos;
	}
	
	public static Position getPosition(String resp) {
		int ret[][] = new int[Position.SIZE][Position.SIZE];
		
		int numRed = 0;
		int numBlack = 0;
		
		int currentindex = 0;
		for(int i=0; i<Position.SIZE; i++) {
			for(int j=0; j<Position.SIZE; j++) {
				currentindex = resp.indexOf('|', currentindex);
				if(resp.charAt(currentindex + 1) == '\n') {
					//Do nothing!
					j--;
				} else if(resp.charAt(currentindex + 1) == 'W') {
					ret[i][j] = Position.WHITE;
					numRed++;
				} else if(resp.charAt(currentindex + 1) == 'D') {
					ret[i][j] = Position.BLACK;
					numBlack++;
				} else if(resp.charAt(currentindex + 1) == ' ') {
					ret[i][j] = Position.EMPTY;
				}
				currentindex += 2;
			}
		}
		
		Position pos = new Position(ret);
		
		return pos;
		
	}

	
}

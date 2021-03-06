package connect4ai;

public class BoardPosition {
	
	public static int HEIGHT = 6;
	public static int WIDTH = 7;
	
	private short pos[][] = new short[HEIGHT][WIDTH];
	private boolean isRedTurn = true;
	
	public static short EMPTY = 0;
	public static short RED = 1;
	public static short BLACK = 2;
	
	//if WINNING_UTILITY >= 10000 or <= -10000
	public static int WINNING_UTILITY = 10000;
	
	
	public BoardPosition(short pos[][], boolean isRedTurn) {
		for(int i=0; i<pos.length; i++) {
			for(int j=0; j<pos[0].length; j++) {
				this.pos[i][j] = pos[i][j];
			}
		}
		this.isRedTurn = isRedTurn;
		
	}

	public BoardPosition() {
		for(int i=0; i<HEIGHT; i++) {
			for(int j=0; j<WIDTH; j++) {
				pos[i][j] = 0;
			}
		}
		isRedTurn = true;
	}
	
	public void printPos() {
		for(int i=0; i<HEIGHT; i++) {
			System.out.print("|");
			for(int j=0; j<WIDTH; j++) {
				if(pos[i][j] == EMPTY) {
					System.out.print(" ");
				} else if(pos[i][j] == RED) {
					System.out.print("R");
				} else if(pos[i][j] == BLACK) {
					System.out.print("B");
				}
				System.out.print("|");
			}
			System.out.println();
		}
		System.out.println("-------------------------");
		
		
		if(getRedUtility() >= WINNING_UTILITY) {
			System.out.println("Red wins!");
		} else if(getRedUtility() <= -WINNING_UTILITY) {
			System.out.println("Black wins!");
		} else {
			if(this.isRedTurn) {
				System.out.println("Red to play");
			} else {
				System.out.println("Black to play");
			}
		}
		System.out.println("-------------------------");

	}
	
	public BoardPosition playTurn(int column) {
		if(couldPlayColumn(column)) {
			int heightInsert = 0;
			for(int i=0; i<HEIGHT; i++) {
				if(pos[i][column] == EMPTY) {
					heightInsert = i;
				}
			}
			return getPositionAfterInsert(heightInsert, column);
			
		}
		
		System.out.println("WARNING: you can't play there!");
		return null;
	}
	
	
	public boolean couldPlayColumn(int column) {
		if(column >=0 && column <BoardPosition.WIDTH) {
			
			if(pos[0][column] == EMPTY) {
				return true;
			} else {
				return false;
			}

		} else {
			return false;
		}
	}
	
	private BoardPosition getPositionAfterInsert(int height, int column) {
		BoardPosition next = this.makeHardCopy();
		if(next.isRedTurn == true) {
			next.pos[height][column] = RED;
		} else {
			next.pos[height][column] = BLACK;
		}
		
		//switch the turn:
		next.isRedTurn = !next.isRedTurn;
		
		return next;
	}
	
	private BoardPosition makeHardCopy() {
		BoardPosition hardCopy = new BoardPosition(this.pos, this.isRedTurn);
		
		
		return hardCopy;
	}
	
	
	public int getRedUtility() {
		
		boolean redWins;
		boolean blackWins;
		
		
		for(int i=0; i<this.pos.length - 3; i++) {
			for(int j=0; j<this.pos[0].length; j++) {
				redWins = true;
				blackWins = true;
				for(int k=0; k<4; k++) {
					if(this.pos[i + k][j] != BLACK) {
						blackWins = false;
					}
					
					if(this.pos[i + k][j] != RED) {
						redWins = false;
					}
				}
				
				if(redWins) {
					return WINNING_UTILITY;
				} else if(blackWins) {
					return -WINNING_UTILITY;
				}
			}
		}
		
		
		for(int i=0; i<this.pos.length; i++) {
			for(int j=0; j<this.pos[0].length - 3; j++) {
				redWins = true;
				blackWins = true;
				for(int k=0; k<4; k++) {
					if(this.pos[i][j + k] != BLACK) {
						blackWins = false;
					}
					
					if(this.pos[i][j + k] != RED) {
						redWins = false;
					}
				}
				
				if(redWins) {
					return WINNING_UTILITY;
				} else if(blackWins) {
					return -WINNING_UTILITY;
				}
			}
		}
		
		
		
		for(int i=0; i<this.pos.length - 3; i++) {
			for(int j=0; j<this.pos[0].length - 3; j++) {
				redWins = true;
				blackWins = true;
				for(int k=0; k<4; k++) {
					if(this.pos[i + k][j + k] != BLACK) {
						blackWins = false;
					}
					
					if(this.pos[i + k][j + k] != RED) {
						redWins = false;
					}
				}
				
				if(redWins) {
					return WINNING_UTILITY;
				} else if(blackWins) {
					return -WINNING_UTILITY;
				}
			}
		}
		
		
		
		for(int i=0; i<this.pos.length - 3; i++) {
			for(int j=3; j<this.pos[0].length; j++) {
				redWins = true;
				blackWins = true;
				for(int k=0; k<4; k++) {
					if(this.pos[i + k][j - k] != BLACK) {
						blackWins = false;
					}
					
					if(this.pos[i + k][j - k] != RED) {
						redWins = false;
					}
				}
				
				if(redWins) {
					return WINNING_UTILITY;
				} else if(blackWins) {
					return -WINNING_UTILITY;
				}
			}
		}
		
		
		
		return 0;
	}
	
	public boolean isGameOver() {
		if(this.getRedUtility() <= -WINNING_UTILITY || this.getRedUtility() >= WINNING_UTILITY) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isTie() {
		for(int j=0; j<pos[0].length; j++) {
			if(pos[0][j] == EMPTY) {
				return false;
			}
		}
		
		return true;
	}
	
	public short[][] getHardCopyPos() {
		short ret[][] = new short[pos.length][pos[0].length];
		for(int i=0; i<pos.length; i++) {
			for(int j=0; j<pos[0].length; j++) {
				ret[i][j] = pos[i][j];
			}
		}
		
		return ret;
	}
	
	public boolean isRedTurn() {
		return isRedTurn;
	}
}
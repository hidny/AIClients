package connect4ai;


public class AiPosition {
	public static int HEIGHT = 6;
	public static int WIDTH = 7;
	
	public static int NUM_CELLS = HEIGHT * WIDTH;
	
	public static int NOTAMOVE = 12301003;
	
	
	private short pos[][] = new short[HEIGHT][WIDTH];
	private boolean isRedTurn = true;
	private int numPegsUsed = -1;
	
	
	public static short EMPTY = 0;
	public static short RED = 1;
	public static short BLACK = 2;
	
	//if WINNING_UTILITY >= 10000 or <= -10000
	public static int WINNING_UTILITY = 10000;
	
	public static int  REALLYHIGHNUMBER = 10 * WINNING_UTILITY;
	
	public int getNumPegsUsed() {
		if(this.numPegsUsed == -1) {
			this.numPegsUsed = 0;
			for(int i=0; i<pos.length; i++) {
				for(int j=0; j<pos[0].length; j++) {
					if(pos[i][j] != EMPTY) {
						this.numPegsUsed++;
					}
				}
			}
			
			return this.numPegsUsed;
		} else {
			return this.numPegsUsed;
		}
	}
	
	public AiPosition(BoardPosition orig) {
		pos = orig.getHardCopyPos();
		isRedTurn = orig.isRedTurn();
	}
	
	public AiPosition(short pos[][], boolean isRedTurn) {
		for(int i=0; i<pos.length; i++) {
			for(int j=0; j<pos[0].length; j++) {
				this.pos[i][j] = pos[i][j];
			}
		}
		this.isRedTurn = isRedTurn;
		
	}

	public AiPosition() {
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
		
		if(this.isRedTurn) {
			System.out.println("Red to play");
		} else {
			System.out.println("Black to play");
		}
		
		
		if(getRedUtility() >= WINNING_UTILITY) {
			System.out.println("Red wins!");
		} else if(getRedUtility() <= -WINNING_UTILITY) {
			System.out.println("Black wins!");
		}
		System.out.println("-------------------------");

	}
	
	public AiPosition playTurn(int column) {
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
		if(column >=0 && column < WIDTH) {
			
			if(pos[0][column] == EMPTY) {
				return true;
			} else {
				return false;
			}

		} else {
			return false;
		}
	}
	
	//TODO
	private AiPosition getPositionAfterInsert(int height, int column) {
		AiPosition next = this.makeHardCopy();
		if(next.isRedTurn == true) {
			next.pos[height][column] = RED;
		} else {
			next.pos[height][column] = BLACK;
		}
		
		//switch the turn:
		next.isRedTurn = !next.isRedTurn;
		
		return next;
	}
	
	private AiPosition makeHardCopy() {
		AiPosition hardCopy = new AiPosition(this.pos, this.isRedTurn);
		
		
		return hardCopy;
	}
	
	
	public int getRedUtility() {
		
		boolean redWins;
		boolean blackWins;
		

		//Check  horizontal lines
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
					return getWinningUtil();
				} else if(blackWins) {
					return -getWinningUtil();
				}
			}
		}
		
		//Check  vertical lines
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
					return getWinningUtil();
				} else if(blackWins) {
					return -getWinningUtil();
				}
			}
		}
		
		
		//Check \ diag.
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
					return getWinningUtil();
				} else if(blackWins) {
					return -getWinningUtil();
				}
			}
		}
		
		
		//Check / diag.
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
					return getWinningUtil();
				} else if(blackWins) {
					return -getWinningUtil();
				}
			}
		}
		
		//At this point, there's no win, but we want to know the rule of thumb utility:
		
		return getRuleOfThumbUtilityForRed2();
	}
	
	//the shorter the game, the better the win!
	public int getWinningUtil() {
		return WINNING_UTILITY + (AiPosition.NUM_CELLS) - this.getNumPegsUsed();
	}
		
	//At this point, there's no win, but we want to know the rule of thumb utility:
	//This just counts the number of ways to red could make a line vs the number of ways black could make a line.
	//TODO: it doesn't give preference for 3/4 or 2/4 (make rule of thumb 3?)
	public int getRuleOfThumbUtilityForRed() {

		boolean redWinPossible;
		boolean blackWinPossible;
		
		int redShadow = 0;
		int blackShadow = 0;

		//Check  horizontal lines
		for(int i=0; i<this.pos.length - 3; i++) {
			for(int j=0; j<this.pos[0].length; j++) {
				redWinPossible = true;
				blackWinPossible = true;
				for(int k=0; k<4; k++) {
					if(this.pos[i + k][j] != RED) {
						blackWinPossible = false;
					}
					
					if(this.pos[i + k][j] != BLACK) {
						redWinPossible = false;
					}
				}
				
				if(redWinPossible) {
					redShadow++;
				} else if(blackWinPossible) {
					blackShadow++;
				}
			}
		}
		
		//Check  vertical lines
		for(int i=0; i<this.pos.length; i++) {
			for(int j=0; j<this.pos[0].length - 3; j++) {
				redWinPossible = true;
				blackWinPossible = true;
				for(int k=0; k<4; k++) {
					if(this.pos[i][j + k] != RED) {
						blackWinPossible = false;
					}
					
					if(this.pos[i][j + k] != BLACK) {
						redWinPossible = false;
					}
				}
				
				if(redWinPossible) {
					redShadow++;
				} else if(blackWinPossible) {
					blackShadow++;
				}
			}
		}
		
		
		//Check \ diag.
		for(int i=0; i<this.pos.length - 3; i++) {
			for(int j=0; j<this.pos[0].length - 3; j++) {
				redWinPossible = true;
				blackWinPossible = true;
				for(int k=0; k<4; k++) {
					if(this.pos[i + k][j + k] != RED) {
						blackWinPossible = false;
					}
					
					if(this.pos[i + k][j + k] != BLACK) {
						redWinPossible = false;
					}
				}
				
				if(redWinPossible) {
					redShadow++;
				} else if(blackWinPossible) {
					blackShadow++;
				}
			}
		}
		
		
		//Check / diag.
		for(int i=0; i<this.pos.length - 3; i++) {
			for(int j=3; j<this.pos[0].length; j++) {
				redWinPossible = true;
				blackWinPossible = true;
				for(int k=0; k<4; k++) {
					if(this.pos[i + k][j - k] != RED) {
						blackWinPossible = false;
					}
					
					if(this.pos[i + k][j - k] != BLACK) {
						redWinPossible = false;
					}
				}
				
				if(redWinPossible) {
					redShadow++;
				} else if(blackWinPossible) {
					blackShadow++;
				}
			}
		}
		
		return redShadow - blackShadow;
	}
	
	//At this point, there's no win, but we want to know the rule of thumb utility:
	//This just has a preference for pegs in the middle:
	//I don't know if it's better or worse than ruleofThumbUtility 1.
	public int getRuleOfThumbUtilityForRed2() {
		int util = 0;
		for(int i=0; i<pos.length; i++) {
			for(int j=0; j<pos[0].length; j++) {
				if(pos[i][j] == RED) {
					util += HEIGHT/2 - Math.abs(HEIGHT/2 - i);
					util += WIDTH/2 - Math.abs(WIDTH/2 - j);
					
				} else if(pos[i][j] == BLACK) {
					util -= HEIGHT/2 - Math.abs(HEIGHT/2 - i);
					util -= WIDTH/2 - Math.abs(WIDTH/2 - j);
					
				}
			}
		}
		
		return util;
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
	
	public boolean isRedTurn() {
		return isRedTurn;
	}
}
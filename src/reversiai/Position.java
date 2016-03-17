package reversiai;


public class Position {

	/* Coordinate:
	 * 
	 *      0 1 2 3 4 5 6 7 8
	 *   0                    
	 *   1                    
	 *   2                    
	 *   3                    
	 *   4                     
	 *   5                    
	 *   6                    
	 *   7                    
	 *   8                    
	 *   
	 *   Translate to: (see Wikipedia)
	 *   
	 *   1
	 *   2
	 *   3
	 *   4
	 *   5
	 *   6
	 *   7
	 *   8
	 *      a  b  c  d  e  f  g  h
	 */
	public static String GAME_NAME = "reversi";
	
	public static int OUT_OF_BOUNDS = -1;
	public static int EMPTY = 0;
	public static int BLACK = 1;
	public static int WHITE = 2;
	
	public static int SIZE = 8;
	public static int NUM_SPACES = SIZE * SIZE;
	
	public static boolean DEBUGGINGAUTOPLAY = false;
	protected int position[][] = new int[SIZE][SIZE];
	
	protected boolean possibleMovesCalculated = false;
	protected boolean possibleMove[][] = new boolean[SIZE][SIZE];
	
	//BLACK
	protected int playerTurn = BLACK;

	protected int piecesUsed = -1;

	public static int WINNING_UTILITY = 10000;
	public static int  REALLYHIGHNUMBER = 10 * WINNING_UTILITY;
	
	public Position() {
		for(int i=0; i<SIZE; i++) {
			for(int j=0; j<SIZE; j++) {
				position[i][j] = EMPTY;
			}
		}
		
		position[SIZE/2-1][SIZE/2-1] = WHITE;
		position[SIZE/2][SIZE/2] = WHITE;
		position[SIZE/2-1][SIZE/2] = BLACK;
		position[SIZE/2][SIZE/2-1] = BLACK;
	}
	
	public Position(int array[][]) {
		int currentNumPieces = 0;
		for(int i=0; i<SIZE; i++) {
			for(int j=0; j<SIZE; j++) {
				if( array[i][j] == BLACK || array[i][j] == WHITE) {
					currentNumPieces++;
				}
				position[i][j] = array[i][j];
			}
		}
		
		this.piecesUsed = currentNumPieces;
		if( this.piecesUsed % 2 == 0 ) {
			playerTurn = BLACK;
		} else {
			playerTurn = WHITE;
		}
		
	}
	
	
	
	public void setPosition(char letter, int num, int value) {
		if(letter >= 'A' || letter <='Z') {
			int temp = 'a' - 'A' + letter;
			letter = (char)temp;
		}
		position[num - 1][(int)(letter - 'a')] = value;
	}
	
	public int getPosition(char letter, int num) {
		return position[num - 1][(int)(letter - 'a')];
	}
	
	
	public int[][] getPosition() {
		return position;
	}
	public boolean[][] getPossibleMoves() {
		if(possibleMovesCalculated) {
			return possibleMove;
		}
		
		for(int i=0; i<SIZE; i++) {
			for(int j=0; j<SIZE; j++) {
				possibleMove[i][j] = checkPositionPlayable(i, j);
			}
		}
		
		possibleMovesCalculated = true;
		
		return possibleMove;
		
	}
	
	
	public boolean checkPositionPlayable(int i, int j) {
		if(position[i][j] != EMPTY) {
			return false;
		}
		
		int x;
		int y;
		
		boolean enemiesInBetween;
		int enemyTurn;
		if(playerTurn == BLACK) {
			enemyTurn = WHITE;
		} else {
			enemyTurn = BLACK;
		}
		
		for(int dirx=-1; dirx<=1; dirx++) {
			for(int diry=-1; diry<=1; diry++) {
				if(dirx == 0 && diry==0) {
					continue;
				} else{
					enemiesInBetween = false;
					
					for(int k=1; k<SIZE; k++) {
						if(j+k*dirx >= 0 && j+k*dirx < SIZE && i+k*diry >= 0 && i+k*diry < SIZE) {
							x = j+k*dirx;
							y = i+k*diry;
							
							//you got x and y reversed!
							if(position[y][x] == enemyTurn) {
								enemiesInBetween = true;
							} else if(position[y][x] == playerTurn && enemiesInBetween) {
								return true;
								
							//check if the space is adjacent to the same colour:
							} else if(position[y][x] == playerTurn && enemiesInBetween == false) {
								break;
							} else if(position[y][x] == EMPTY) {
								break;
							} else {
								System.out.println("POSITION UNKNOWN! (In check position playable)");
								System.exit(1);
							}
						}
					}
				}
			}
		}
		return false;
		
	}
	

	public Position move(int i, int j) {
		
		//STEP 0: check if the move is possible:
		Position newPos = null;
		boolean possible[][] = getPossibleMoves();
		if(possible[i][j] == false) {
			return null;
		}
		
		/*
		 * STEP 1: hard copy position
		 * 
		 */
		newPos = this.makeHardCopy();
		
		/* step 2:
		 * flip the pieces on the board appropriately.
		 */
		int x;
		int y;
		
		int xFlip;
		int yFlip;
		
		boolean enemiesInBetween = false;
		int enemyTurn;
		if(newPos.playerTurn == BLACK) {
			enemyTurn = WHITE;
		} else {
			enemyTurn = BLACK;
		}
		
		for(int dirx=-1; dirx<=1; dirx++) {
			for(int diry=-1; diry<=1; diry++) {
				if(dirx == 0 && diry==0) {
					continue;
				} else{
					enemiesInBetween = false;
					
					for(int k=1; k<SIZE; k++) {
						if(j+k*dirx >= 0 && j+k*dirx < SIZE && i+k*diry >= 0 && i+k*diry < SIZE) {
							x = j+k*dirx;
							y = i+k*diry;
							
							//you got x and y reversed!
							if(newPos.position[y][x] == enemyTurn) {
								enemiesInBetween = true;
							} else if(newPos.position[y][x] == newPos.playerTurn && enemiesInBetween) {
								//FLIPPY TIME!
								if(k==1) {
									System.out.println("ERROR: (In Position.move()) I'm not flipping anything wtf?");
									System.exit(1);
								}
								for(int l=1; l<k; l++) {
									xFlip = j+l*dirx;
									yFlip = i+l*diry;
									if(newPos.position[yFlip][xFlip] == BLACK) {
										newPos.position[yFlip][xFlip] = WHITE;
									} else if(newPos.position[yFlip][xFlip] == WHITE) {
										newPos.position[yFlip][xFlip] = BLACK;
									} else {
										System.out.println("ERROR: (In Position.move()) I'm flipping an empty square wtf?");
										System.exit(1);
									}
								}
								break;
								
							//check if the space is adjacent to the same colour:
							} else if(newPos.position[y][x] == newPos.playerTurn && enemiesInBetween == false) {
								break;
							
							} else if(newPos.position[y][x] == EMPTY) {
								break;
							} else {
								System.out.println("POSITION UNKNOWN! (In Position.move() playable)");
								System.exit(1);
							}
						}
					}
				}
			}
		}
		
		if(newPos.position[i][j] != EMPTY) {
			System.out.println("MOVING into a filled space (wtf?) UNKNOWN! (In Position.move())");
			System.exit(1);
		}
		if(newPos.playerTurn == WHITE) {
			newPos.position[i][j] = WHITE;
		} else if(newPos.playerTurn == BLACK) {
			newPos.position[i][j] = BLACK;
		}
		
		//step 3: make the switch
		
		if(this.playerTurn == BLACK) {
			newPos.playerTurn = WHITE;
			
		} else {
			newPos.playerTurn = BLACK;
			
		}
		
		newPos.possibleMovesCalculated = false;
		
		return newPos;
	}
	
	
	public Position makeHardCopy() {
		Position newPos = new Position();
		
		newPos.possibleMovesCalculated = possibleMovesCalculated;
		newPos.playerTurn = playerTurn;
		
		for(int i=0; i<SIZE; i++) {
			for(int j=0; j<SIZE;j++) {
				newPos.position[i][j] = position[i][j];
				newPos.possibleMove[i][j] = possibleMove[i][j];
			}
		}
		
		return newPos;
	}
	
	public int getNumPegsUsed() {
		if(this.piecesUsed == -1) {
			int currentPiecesUsed = 0;
			for(int i=0; i<position.length; i++) {
				for(int j=0; j<position[0].length; j++) {
					if(position[i][j] != EMPTY) {
						currentPiecesUsed++;
					}
				}
			}
			this.piecesUsed = currentPiecesUsed;
			return this.piecesUsed;
		} else {
			return this.piecesUsed;
		}
	}
	
	//Simple utility:
	public int getNumBlackMinusWhiteUsed() {
		int difference = 0;
		
		for(int i=0; i<position.length; i++) {
			for(int j=0; j<position[0].length; j++) {
				if(position[i][j] == BLACK) {
					difference++;
				} else if(position[i][j] == WHITE) {
					difference--;
				}
			}
		}
		
		return difference;
	}

	
	public boolean isBlackTurn() {
		if(this.playerTurn == BLACK) {
			return true;
		} else {
			return false;
		}
	}
	
	public void printPos() {
		for(int i=0; i<position.length; i++) {
			for(int j=0; j<position[0].length; j++) {
				if(position[i][j] == BLACK) {
					System.out.print("B");
				} else if(position[i][j] == WHITE) {
					System.out.print("W");
				} else {
					System.out.print(" ");
				}
			}
			System.out.println();
		}
	}
	
	//TODO: make a get prob of winning.
	
	//really simple utility function:
	public double getBlackUtility() {
		int blackUtil = 0;
		//lazy count:
		for(int i=0; i<SIZE; i++) {
			for(int j=0; j<SIZE;j++) {
				if(position[i][j] == BLACK) {
					blackUtil++;
				} else if(position[i][j] == WHITE){
					blackUtil--;
				}
			}
		}
		return blackUtil;
	}
}
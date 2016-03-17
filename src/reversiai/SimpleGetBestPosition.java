package reversiai;

import reversiai.Position;


public class SimpleGetBestPosition {

	//TODO: take away constant/use from postion class.
	
		public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	
	public static String getBestMove(Position pos, int depth, long timeLimit) {
		System.out.println("TESTING: in new get best move");
		
		double choices[] = SimpleGetBestPosition.getUtilityOfAllMoves(pos, depth, timeLimit);
		
		if(System.currentTimeMillis() > timeLimit) {
			//STOP ANALYSING!
			System.out.println("Times up!");
			return "timesup";
		}
		
		String SPACE = "              ";
		for(int i=0; i<choices.length; i++) {
			if(i%Position.SIZE == 0) {
				System.out.println();
			}
			if(pos.checkPositionPlayable(i/Position.SIZE, i%Position.SIZE)) {
				System.out.print(choices[i] + SPACE.substring((choices[i] + "").length()));
			} else {
				System.out.print("N/A" + SPACE.substring(("N/A").length()));
			}
		}
		System.out.println();
		
		int bestIndex = -1;
		
		
		if(pos.isBlackTurn()) {
			//get max utility choice:
			for(int i=0; i<choices.length; i++) {
				if(pos.checkPositionPlayable(i/Position.SIZE, i%Position.SIZE)) {
					if(bestIndex != -1) {
						if(choices[i] > choices[bestIndex]) {
							bestIndex = i;
						}
					} else {
						bestIndex = i;
					}
				}
			}
		
		} else {
			//get min utility choice:
			for(int i=0; i<choices.length; i++) {
				if(pos.checkPositionPlayable(i/Position.SIZE, i%Position.SIZE)) {
					if(bestIndex != -1) {
						if(choices[i] < choices[bestIndex]) {
							bestIndex = i;
						}
					} else {
						bestIndex = i;
					}
				}
			}
		}
		
		System.out.println("Picking " + bestIndex + ".");
		
		
		//TODO:
		int row = 1 + bestIndex/Position.SIZE;
		char column = (char)(bestIndex%Position.SIZE + 'a');
		
		System.out.println("Converting to: " + column + "" + row);
		
		return column + "" + row;
	}
		
	
	public static double[] getUtilityOfAllMoves(Position pos, int depth, long timeLimit) {
		Position AiOrig = pos.makeHardCopy();
		Position choices[] = new Position[Position.NUM_SPACES];
		
		//make sure depth doesn't go beyond the game:
		if(Position.NUM_SPACES - AiOrig.getNumPegsUsed() < depth) {
			depth = Position.NUM_SPACES - AiOrig.getNumPegsUsed();
		}
		
		for(int i=0; i<Position.NUM_SPACES; i++) {
			if(AiOrig.checkPositionPlayable(i/Position.SIZE, i%Position.SIZE)) {
				choices[i] = AiOrig.move(i/Position.SIZE, i%Position.SIZE);
			} else {
				choices[i] = null;
			}
		}
		
		double ret[] = new double[Position.NUM_SPACES];
		
		for(int i=0; i<Position.NUM_SPACES; i++) {
			if(choices[i] != null) {
				if(System.currentTimeMillis() > timeLimit) {
					//STOP ANALYSING!
					System.out.println("Times up!");
					break;
				}
				System.out.println("Trying slot " + (i/Position.SIZE) + "," + (i%Position.SIZE));
				ret[i] = getMoveUtil(choices[i], depth-1, -Position.REALLYHIGHNUMBER, Position.REALLYHIGHNUMBER);
				System.out.println("Got " +  ret[i]);
			}
			
		}
		
		return ret;
		
	}

	//pre: there will always be a peg to place until depth = 0.
	
	//once depth =0, just return some rule of thumb.
	public static double getMoveUtil(Position pos, int depth, double alpha, double beta) {
		if(depth == 0 || pos.getNumPegsUsed() == Position.NUM_SPACES) {
			return pos.getBlackUtility();
		}
		
		Position choices[] = new Position[Position.NUM_SPACES];
		
		
		for(int i=0; i<Position.NUM_SPACES; i++) {
			if(pos.checkPositionPlayable(i/Position.SIZE, i%Position.SIZE)) {
				choices[i] = pos.move(i/Position.SIZE, i%Position.SIZE);
			} else {
				choices[i] = null;
			}
		}
		
		double ret;
		boolean foundPossibleMove = false;
		
		if(pos.isBlackTurn()) {
			
			//get max util for next turn:
			for(int i=0; i<Position.NUM_SPACES; i++) {
				if(choices[i] != null) {
					foundPossibleMove = true;
					
					alpha = Math.max(getMoveUtil(choices[i], depth-1, alpha, beta), alpha);
					
					if(alpha >= beta) {
						break;
					}
				}
				
			}
			
			if(foundPossibleMove == false) {
				//If there's no move, it means white should have the adv.
				ret = getMoveUtil(pos, 0, 0, 0);
			} else {
				ret = alpha;
			}
		} else {
			
			//get min util for next turn:
			for(int i=0; i<Position.NUM_SPACES; i++) {
				if(choices[i] != null) {
					
					foundPossibleMove = true;
					
					beta = Math.min(getMoveUtil(choices[i], depth-1, alpha, beta), beta);
					
					if(beta <= alpha) {
						break;
					}
				}
				
			}
			if(foundPossibleMove == false) {
				//If there's no move, it means black should have the adv.
				ret = getMoveUtil(pos, 0, 0, 0);
			} else {
				ret = beta;
			}
		}
		
		
		return ret;
	}
}

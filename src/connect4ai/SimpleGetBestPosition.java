package connect4ai;


public class SimpleGetBestPosition {

	//TODO: take away constant/use from postion class.
	
		public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
		
	public static int getBestMove(BoardPosition pos, int depth) {
		
		int choices[] = SimpleGetBestPosition.getUtilityOfAllMoves(pos, depth);
		
		for(int i=0; i<choices.length; i++) {
			System.out.println("slot " + i + ": " + choices[i]);
		}
		
		int bestIndex = -1;
		
		
		if(pos.isRedTurn()) {
			//get max utility choice:
			for(int i=0; i<choices.length; i++) {
				if(choices[i] != AiPosition.NOTAMOVE) {
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
				if(choices[i] != AiPosition.NOTAMOVE) {
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
		
		
		return bestIndex;
	}
		
	
	public static int[] getUtilityOfAllMoves(BoardPosition pos, int depth) {
		AiPosition AiOrig = new AiPosition(pos);
		AiPosition choices[] = new AiPosition[AiPosition.WIDTH];
		
		if(AiOrig.getNumPegsUsed() >= AiPosition.NUM_CELLS) {
			System.out.println("ERROR: too many pegs. Can't make a move!");
			//Tie!
			return null;
		}
		//make sure depth doesn't go beyond the game:
		if(AiPosition.NUM_CELLS - AiOrig.getNumPegsUsed() < depth) {
			depth = AiPosition.NUM_CELLS - AiOrig.getNumPegsUsed();
		}
		
		for(int i=0; i<AiPosition.WIDTH; i++) {
			if(AiOrig.couldPlayColumn(i)) {
				choices[i] = AiOrig.playTurn(i);
			} else {
				choices[i] = null;
			}
		}
		
		int ret[] = new int[AiPosition.WIDTH];
		
		for(int i=0; i<AiPosition.WIDTH; i++) {
			if(choices[i] != null) {
				System.out.println("Trying slot " + i);
				ret[i] = getMoveUtil(choices[i], depth-1, -AiPosition.REALLYHIGHNUMBER, AiPosition.REALLYHIGHNUMBER);
				System.out.println("Got " +  ret[i]);
			} else {
				ret[i] = AiPosition.NOTAMOVE;
			}
		}
		
		return ret;
		
	}

	//pre: there will always be a peg to place until depth = 0.
	
	//once depth =0, just return some rule of thumb.
	public static int getMoveUtil(AiPosition pos, int depth, int alpha, int beta) {
		if(depth == 0 || pos.isGameOver()) {
			return pos.getRedUtility();
		}
		
		AiPosition choices[] = new AiPosition[AiPosition.WIDTH];
		
		
		for(int i=0; i<AiPosition.WIDTH; i++) {
			if(pos.couldPlayColumn(i)) {
				choices[i] = pos.playTurn(i);
			} else {
				choices[i] = null;
			}
		}
		
		int ret;
		
		if(pos.isRedTurn()) {
			
			//get max util for next turn:
			for(int i=0; i<AiPosition.WIDTH; i++) {
				if(choices[i] != null) {
					alpha = Math.max(getMoveUtil(choices[i], depth-1, alpha, beta), alpha);
					
					if(alpha >= beta) {
						break;
					}
				}
				
			}
			
			ret = alpha;
			
		} else {
			
			//get min util for next turn:
			for(int i=0; i<AiPosition.WIDTH; i++) {
				if(choices[i] != null) {
					beta = Math.min(getMoveUtil(choices[i], depth-1, alpha, beta), beta);
					
					if(beta <= alpha) {
						break;
					}
				}
				
			}
			
			ret = beta;
		}
		
		
		return ret;
	}
}

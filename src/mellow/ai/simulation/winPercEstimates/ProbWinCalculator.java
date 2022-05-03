package mellow.ai.simulation.winPercEstimates;

import java.io.OutputStream;
import java.io.PrintStream;

import mellow.Constants;
import mellow.ai.aiDecider.MellowAIDeciderFactory;
import mellow.ai.aiDecider.MellowAIDeciderInterface;
import mellow.ai.aiDecider.MellowBasicDecider;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.cardDataModels.StatsBetweenRounds;
import mellow.ai.simulation.MonteCarloMain;
import mellow.ai.simulation.deckCopiedFromServer.Deck;
import mellow.ai.simulation.deckCopiedFromServer.DeckFunctions;
import mellow.ai.simulation.deckCopiedFromServer.RandomDeck;
import mellow.cardUtils.CardStringFunctions;

public class ProbWinCalculator {


	//Copied from Monte carlo main:

	private static PrintStream originalStream = System.out;
	
	private static PrintStream dummyStream = new PrintStream(new OutputStream(){
	    public void write(int b) {
	        // NO-OP
	    }
	});
	
	public static double getProbDealerWinningAndHalfDrawing(int dealerScore, int opponentScore, int num_iter) {
		System.setOut(dummyStream);
		int numWins = 0;
		int numLosses = 0;
		int numDraws = 0;
		for(int i=0; i<num_iter; i++) {
			if(i %100 == 0) {
				System.err.println("Simulated game iteration: " + i);
			}
			double result = playGameUntilEnd(dealerScore, opponentScore);
			
			if(result == 1.0) {
				numWins++;
			} else if(result == -1.0) {
				numLosses++;
			} else {
				numDraws++;
			}
		}
		
		System.setOut(originalStream);
		
		System.out.println("Starting from (" + dealerScore + ", " + opponentScore + "), there's: ");
		System.out.println(numWins + " wins.");
		System.out.println(numLosses + " losses.");
		System.out.println(numDraws + " draws.");
		
		double prob = (numWins + 0.5 * numDraws) / (numWins + numLosses + numDraws);
		
		System.out.println("Win prob:" + prob);
	
		
		return prob;
	}
	
	
	public static String players[] = new String[]
			{"player1", "player2", "player3", "player4"};
	
	//returns 1.0 if dealer team wins
	//        -1.0 if opponents win
	//        0.0 if draw
	public static double playGameUntilEnd(int scoreDealer, int scoreOpponent) {
		
		
		//System.err.println(scoreDealer + "  " + scoreOpponent);
		
		if(scoreDealer >= Constants.GOAL_SCORE
				|| scoreOpponent >= Constants.GOAL_SCORE) {
			if(scoreDealer > scoreOpponent) {
				return 1.0;
			} else if(scoreDealer < scoreOpponent) {
				return -1.0;
			} else if(scoreDealer == scoreOpponent) {
				return 0.0;
			} else {
				//System.err.println("oops!");
				System.exit(1);
			}
		}
		
		
		int SANITY_CHECK_DM_INDEX = 1;
		int dealerIndex = 0;
		
		DataModel sanityCheckDataModel = new DataModel();
		
		MellowAIDeciderInterface playerInSimulation[] = new MellowBasicDecider[Constants.NUM_PLAYERS];
		
		//For each player: create a HARD coded data model for their perspective (Maybe their own MellowBasicDecider):
		for(int i=0; i<Constants.NUM_PLAYERS; i++) {
			
			if(i == SANITY_CHECK_DM_INDEX) {
				playerInSimulation[i] = new MellowBasicDecider(sanityCheckDataModel);
			} else {
				playerInSimulation[i] = MellowAIDeciderFactory.getAI(MellowAIDeciderFactory.FOLLOW_HARD_CODED_RULES_AI);
				
			}
			//Set names of players such that players[0] is the playerIndex:
			String playersRelativeToPlayerI[] = new String[Constants.NUM_PLAYERS];
			
			for(int j=0; j<players.length; j++) {
				playersRelativeToPlayerI[j] = players[(i + j) % Constants.NUM_PLAYERS];
			}
			
			playerInSimulation[i].setNameOfPlayers(playersRelativeToPlayerI);
			
			playerInSimulation[i].resetStateForNewRound();

			playerInSimulation[i].setDealer(players[dealerIndex]);
			
			if(i % 2 == 0) {
				playerInSimulation[i].setNewScores(scoreDealer, scoreOpponent);
			} else {
				playerInSimulation[i].setNewScores(scoreOpponent, scoreDealer);
			}
			
		}
		
		//Distribute cards:
		Deck deck = new RandomDeck();
		deck.shuffle();
		
		String dist[][] = new String[Constants.NUM_PLAYERS][Constants.NUM_STARTING_CARDS_IN_HAND];
		for(int i=0; i<52; i++) {
			dist[i / Constants.NUM_STARTING_CARDS_IN_HAND][i % Constants.NUM_STARTING_CARDS_IN_HAND] 
					= DeckFunctions.getCardString(deck.getNextCard());
			
		}
		
		for(int i=0; i<Constants.NUM_PLAYERS; i++) {
			playerInSimulation[i].setCardsForNewRound(dist[i]);
			dist[i] = CardStringFunctions.sort(dist[i]);
			
			/*String tmp = "";
			for(int j=0; j<dist[i].length; j++) {
				tmp += dist[i][j] + " ";
			}
			System.err.println(players[i] + " has: " + tmp);
			*/
		}
		
		
		//System.err.println("Bids:");
		//bids:
		int firstBidIndex = dealerIndex + 1;
		
		int bids[] = new int[Constants.NUM_PLAYERS];

		int tricks[] = new int[Constants.NUM_PLAYERS];
		
		for(int i=0; i<Constants.NUM_PLAYERS; i++) {
			
			int curIndex = (firstBidIndex + i) % Constants.NUM_PLAYERS;
			//System.err.println("Cur Index: " + curIndex);
			
			String bid = playerInSimulation[curIndex].getBidToMake();
			
			if(bid.toLowerCase().startsWith("m")) {
				//Mellow is 0:
				bid = "0";
			}
			
			bids[curIndex] = Integer.parseInt(bid);
			tricks[curIndex] = 0;
			
			for(int j=0; j<Constants.NUM_PLAYERS; j++) {
				
				playerInSimulation[j].receiveBid(players[curIndex], bids[curIndex]);
				
			}
			//System.err.println(players[curIndex] + ": " + bids[curIndex]);
		}

		//System.err.println("Round of Cards:");
		//Play a round:
		
		int curLeadIndex = firstBidIndex;
		
		for(int i=0; i<Constants.NUM_STARTING_CARDS_IN_HAND; i++) {
			
			int leadSuit = -1;
			String bestCard = null;
			int curWinner = -1;
			
			for(int j=0; j<Constants.NUM_PLAYERS; j++) {
				
				int curIndex = (curLeadIndex + j) % Constants.NUM_PLAYERS;
				
				String card = playerInSimulation[curIndex].getCardToPlay();
				
				if(j == 0) {
					leadSuit = CardStringFunctions.getIndexOfSuit(card);
					bestCard = card;
					curWinner = curIndex;
					
				} else if(cardAGreaterThanCardBGivenLeadCard(card, bestCard, leadSuit)) {
					bestCard = card;
					curWinner = curIndex;
				}
			
				for(int k=0; k<Constants.NUM_PLAYERS; k++) {
					playerInSimulation[k].receiveCardPlayed(players[curIndex], card);
				}
				
				//System.err.println(players[curIndex] + ": " + card);
			}
			
			tricks[curWinner]++;
			curLeadIndex = curWinner;
			
			//System.err.println(players[curWinner] + " gets the trick");
			
		}
		
		//count # tricks and update the scores...
		
		//Update score:
		int scoreChanges[] = new int[2];
		
		for(int team=0; team<2; team++) {
			
			int scoreUpdate = 0;
			
			int sumBids = 0;
			int sumTricks = 0;
			
			for(int playerInTeam=0; playerInTeam<2; playerInTeam++) {
				
				sumBids += bids[team + 2 * playerInTeam];
				sumTricks += tricks[team +  2 * playerInTeam];
				
				if(bids[team +  2 * playerInTeam] == 0) {
					if(tricks[team +  2 * playerInTeam] == 0) {
						scoreUpdate += 100;
					} else {
						scoreUpdate -= 100;
					}
				}
			}
			
			if(sumBids > 0) {
				
				if(sumTricks < sumBids) {
					scoreUpdate -= 10 * sumBids;
				} else {
					scoreUpdate += 10 * sumBids;
					
					scoreUpdate += 1 * (sumTricks - sumBids);
				}
			}
			
			scoreChanges[team] = scoreUpdate;
			
		}
		//playGameUntilEnd(int scoreDealer, int scoreOpponent)
		
		int newScoreDealerTeam = scoreDealer + scoreChanges[0];
		int newScoreOpponentTeam = scoreOpponent + scoreChanges[1];
		//End update scores:
		
		
		//Sanity check update scores: (Use function in MonteCarloMain)
		//Why didn't I just use the function in MonteCarloMain in the 1st place? Who knows!
		//I'm just going to be lazy and leave the redundant code where it is!
		StatsBetweenRounds endOfRoundPointDiffStats = MonteCarloMain.getPointDiffEndOfRound(sanityCheckDataModel);
		
		if(endOfRoundPointDiffStats.getAIScore() != scoreChanges[1]) {
			System.err.println("oops! Scores don't match 1!");
			System.err.println(endOfRoundPointDiffStats.getAIScore() + " vs " + scoreChanges[1]);
		}
		
		if(endOfRoundPointDiffStats.getOpponentScore() != scoreChanges[0]) {
			
			System.out.println(endOfRoundPointDiffStats.getAIScore() + " vs " + scoreChanges[0]);
			System.err.println("oops! Scores don't match 2!");
		}
		//End Sanity check update scores
		
		//Start a new round by calling this function recursively:	
		return -1.0 * playGameUntilEnd(newScoreOpponentTeam, newScoreDealerTeam);
	}
	
	//COPIED FROM DATAMODEL:
	public static boolean cardAGreaterThanCardBGivenLeadCard(String cardA, String cardB, int leadSuit) {
		return getCardPower(cardA, leadSuit) > getCardPower(cardB, leadSuit);
	}
	
	public static int getCardPower(String card, int leadSuit) {

		//Play trump/spade
		if(CardStringFunctions.getIndexOfSuit(card) == Constants.SPADE) {
			return Constants.NUM_RANKS + getRankIndex(card);
		
		//Follow suit
		} else if(CardStringFunctions.getIndexOfSuit(card) == leadSuit) {
			return getRankIndex(card);

		//Play off-suit
		} else {
			return -1;
		}
	}
	
	public static int getRankIndex(String card) {
		int x = 0;
		
		try {
			if(card.charAt(0) >= '2' && card.charAt(0) <= '9') {
				x = (int)card.charAt(0) - (int)('2');
			} else if(card.charAt(0) == 'T') {
				x = 8;
			} else if(card.charAt(0) == 'J') {
				x = 9;
			} else if(card.charAt(0) == 'Q') {
				x = 10;
			} else if(card.charAt(0) == 'K') {
				x = 11;
			} else if(card.charAt(0) == 'A') {
				x = 12;
			} else {
				throw new Exception("Number unknown! Uh oh!");
				
			}
		
		} catch (Exception e) {
			e.printStackTrace();

			//System.err.println("Number unknown! Uh oh!");
			System.exit(1);
		}
		return x;
	}
	//END COPIED FROM DATAMODEL
	
	
	
	

}

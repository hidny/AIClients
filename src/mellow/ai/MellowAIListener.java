package mellow.ai;

import clientPlayers.GamePlayerInterface;

//import connectfour.Position;
import deck.DeckFunctions;

import java.util.ArrayList;
import java.util.concurrent.locks.*;


public class MellowAIListener implements GamePlayerInterface {//change to final
	public static final String END_OF_TRANSMISSION = "**end of transmission**";

	public static final String HELLO_MSG = "Hello";
	public static final String START_MSG = "Starting Mellow!";
	public static final String START_POINTS_1 = " start with ";
	public static final String START_POINTS_2 = " points";
	
	public static final String YOUR_BID = "What\'s your bid?";
	public static final String YOUR_TURN = "Play a card!";
	public static final String PRIVATE_MSG = "From Game(private):";
	public static final String PUBLIC_MSG = "From Game(public): ";
	
	//first dealer:
	public static final String FIRST_DEALER = "From Game(public): First dealer is ";
	public static final String DEALER_MSG = "dealer is ";
	public static final String FIGHT_SUMMARY_MSG = "Fight Winner:";
	//From Game(private): 7C KS 9C AC AH 2S 4D KC KH 2D 7H 9S 7S 
	
	public static final String TRICKS = "trick(s).";

	public static final int NUM_CARDS = 52;
	public static final int NUM_PLAYERS = 4;

	public static final String PLAYING_CARD = "playing:";
	public static final String WIN = "win!";
	public static final String END_OF_ROUND = "END ROUND!";
	
	public static final int NUMBER_OF_EOR_MESSAGES = 3;

	private int endOfRoundIndex = NUMBER_OF_EOR_MESSAGES;
	
	//State variables:
	private boolean gameStarted;
	private String players[];
	
	//TODO: use locks to defend against spamming.
	private Lock turnLock;
	
	private boolean itsYourBid;
	private boolean itsYourTurn;
	
	private MellowAIDeciderInterface gameAIAgent = null;
	
	private String currentPlayerName = null;

	private boolean currentPlayerInFirstTeam = true;
	private int tempPlayerAStartScore= 0;

	//End state variables
	
	public MellowAIListener(long aiLevel, boolean isFast) {
		gameAIAgent = MellowAIDeciderFactory.getAI(aiLevel, isFast);
	}
	
	public void resetName(String name) {
		this.currentPlayerName = name;
	}
	
	public boolean isGameStarted() {
		return gameStarted;
	}

	public String[] getPlayers() {
		return players;
	}

	public boolean isItsYourBid() {
		return itsYourBid;
	}
	
	public boolean isItsYourTurn() {
		return itsYourTurn;
	}

	
	//pre: server message is only 1 transmission
	public String getClientResponse(String serverMessage) {
		
		System.out.println("Mellow ack received: " + serverMessage);
		
		if( serverMessage.length() > 0) {
			if(serverMessage.startsWith(PUBLIC_MSG)) {
				if(serverMessage.contains(START_MSG)) {
					
					players = new String[NUM_PLAYERS];
					players[0] = serverMessage.split(" ")[4];
					players[1] = serverMessage.split(" ")[8];
					players[2] = serverMessage.split(" ")[6];
					players[3] = serverMessage.split(" ")[10];
					
					//TODO: playerInTeamA logic
					while (players[0].equals(currentPlayerName) == false ) {
						players = shiftArrayByOne(players);
						currentPlayerInFirstTeam = !currentPlayerInFirstTeam;
					}
					
					System.out.println("Players:");
					for(int i=0; i<players.length; i++) {
						System.out.println(players[i]);
					}
					
					if(currentPlayerInFirstTeam) {
						System.out.println("In first team");
					} else {
						System.out.println("In second team");
					}
					
					gameAIAgent.setNameOfPlayers(players);
					
					this.gameStarted = true;
				
				//TODO: should I use a lock?
				} else if(serverMessage.contains(START_POINTS_1) && serverMessage.contains(START_POINTS_2)) {
					
					//Update one teams score:
					String player1 = serverMessage.split(" ")[2];
					String player2 = serverMessage.split(" ")[4];
					System.out.println("player 1:" + player1);
					System.out.println("player 2:" + player2);
					System.out.println("current player" + currentPlayerName);
					System.out.println(currentPlayerInFirstTeam);
					
					boolean currentPlayerInList = (currentPlayerName.equals(player1) || currentPlayerName.equals(player2));
					
					if(currentPlayerInList) {
						System.out.println("In list");
					} else {
						System.out.println("no in list");
					}
					int score = Integer.parseInt(serverMessage.split(" ")[7]);
					
					if((currentPlayerInFirstTeam && currentPlayerInList) || (currentPlayerInFirstTeam == false && currentPlayerInList == false)) {
						tempPlayerAStartScore = score;
					} else {
						
						//Updates scores in such a way that the AI always thinks it's player 0 and part of team A:
						if(currentPlayerInFirstTeam) {
							gameAIAgent.updateScores(tempPlayerAStartScore, score);
						} else {
							gameAIAgent.updateScores(score, tempPlayerAStartScore);
						}
					}
					
				} else if(serverMessage.contains(PLAYING_CARD)) {
					//From Game(public): Michael playing: 9S
					String player = serverMessage.split(" ")[2];
					String card = serverMessage.split(" ")[4];
					System.out.println("player: " + player);
					System.out.println("card: " + card);
					System.out.println("----------");
					
					if (isACard(card) == false) {
						System.out.println("UH OH! Could not find card: " + card);
						System.exit(1);
					}
					
					String direction = getRelativePosPlayer(player);
					System.out.println(direction + "(" + player + ") plays " + card);
					
					gameAIAgent.getPlayedCard(player, card);
					
				} else if(serverMessage.contains(DEALER_MSG)) {
					String dealer = serverMessage.split(" ")[serverMessage.split(" ").length - 1];
					
					dealer = removeLastNewLines(dealer);

					String direction = getRelativePosPlayer(dealer);
					System.out.println(direction + "(" + dealer + ") is dealer.");
					
				} else if(serverMessage.contains(FIGHT_SUMMARY_MSG)) {
					String fightWinner = serverMessage.split(" ")[serverMessage.split(" ").length - 1];
					fightWinner = fightWinner.trim();
					while(fightWinner.endsWith("\n")) {
						fightWinner = fightWinner.substring(0, fightWinner.length() - 1);
					}
					String direction = getRelativePosPlayer(fightWinner);
					System.out.println(direction + "(" + fightWinner + ") won the trick.");
					
					
				} else if(serverMessage.contains(END_OF_ROUND)) {
					endOfRoundIndex = 0;
				
				} else if(endOfRoundIndex < NUMBER_OF_EOR_MESSAGES) {
					String currentScores =serverMessage.substring(serverMessage.indexOf(PUBLIC_MSG) + PUBLIC_MSG.length());
					currentScores = currentScores.trim();
					
					String tokens[] = currentScores.split(" ");
					
					if(tokens.length > 1 && isInteger(tokens[0])  && isInteger(tokens[tokens.length - 1])) {
						endOfRoundIndex++;
						if(endOfRoundIndex == 1) {
							System.out.println("Previous scores:");
						} else if(endOfRoundIndex == 2) {
							System.out.println("Score added:");
						} else {
							System.out.println("Current total:");
						}
						if(currentPlayerInFirstTeam) {
							System.out.println("US(team A): " + tokens[0]);
							System.out.println("THEM(team B): " + tokens[tokens.length - 1]);
						} else {
							System.out.println("THEM(team A) :" + tokens[0]);
							System.out.println("US(team B): " + tokens[tokens.length - 1]);
						}
						
						if(endOfRoundIndex != 1 && endOfRoundIndex != 2) {
							if(currentPlayerInFirstTeam) {
								gameAIAgent.updateScores(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[tokens.length - 1]));
							} else {
								gameAIAgent.updateScores(Integer.parseInt(tokens[tokens.length - 1]), Integer.parseInt(tokens[0]));
							}
						}
					}
				} else if(serverMessage.contains(WIN)) {
					//Tested!
					System.out.println("Yo man, someone won!");
					String finalMsg =serverMessage.substring(serverMessage.indexOf(PUBLIC_MSG) + PUBLIC_MSG.length());
					finalMsg = finalMsg.trim();
					System.out.println("REMINDER: " + finalMsg);
					
				} else {
					if(players != null) {
						for(int i=0; i<NUM_PLAYERS; i++) {
							/*System.out.println("TESTING");
							System.out.println("serverMessage: " + serverMessage);
							System.out.println("split: " + serverMessage.split(" "));
							System.out.println("TESTING");*/
							
							if(players[i] != null && serverMessage.split(" ") != null && serverMessage.split(" ").length >= 3 && serverMessage.contains(players[i]) && isInteger(serverMessage.split(" ")[3])) {
								System.out.println(players[i]  + " bids " + serverMessage.split(" ")[3]);
								gameAIAgent.receiveBid(players[i], Integer.parseInt(serverMessage.split(" ")[3]));
							}
						}
					}
				}

			} else if(serverMessage.startsWith(PRIVATE_MSG)) {
				serverMessage = serverMessage.substring(serverMessage.indexOf(PRIVATE_MSG) + PRIVATE_MSG.length());
				serverMessage = serverMessage.trim();
				String cardsInHandTemp[] = serverMessage.split(" ");
				
				
				if(serverMessage.contains(YOUR_BID)) {
					//serverMessage = serverMessage.substring(serverMessage.indexOf(YOUR_BID) + YOUR_BID.length());
					itsYourBid = true;
				} else if(serverMessage.contains(YOUR_TURN)) {
					itsYourTurn = true;
				} else if(cardsInHandTemp.length > 0 && isACard(cardsInHandTemp[0])) {
					
					System.out.println("Sorted cards:");
					
					printSortedCards(cardsInHandTemp);
					
					if(cardsInHandTemp.length == NUM_CARDS/NUM_PLAYERS) {
						gameAIAgent.setupCardsForNewRound(cardsInHandTemp);
					}
				}
			}
		}
		
		if(itsYourBid) {
			itsYourBid = false;
			itsYourTurn = false;
			
			String bid = gameAIAgent.getBidToMake();
			
			//Make sure bid is an integer:
			int temp = 1;
			try {
				temp = Integer.parseInt(bid);
			} catch (Exception e) {
				temp = 1;
			}
			
			return "/move " + temp;
			
		} else if(itsYourTurn) {
			itsYourBid = false;
			itsYourTurn = false;
			String cardToPlay = gameAIAgent.getCardToPlay();
			if(cardToPlay == null) {
				return "/move 1";
			} else {
				return "/move " + cardToPlay;
			}
		} else {
			return "";
		}
	}
	
	private String[] shiftArrayByOne(String players[]) {
		String newPlayers[] = new String[players.length];
		for(int i=0; i<players.length; i++) {
			newPlayers[(i+1)%newPlayers.length]= players[i]; 
		}
		return newPlayers;
	}
	
	public static boolean isACard(String card) { 
		if(DeckFunctions.getCard(card) > 0 ){
			return true;
		} else {
			return false;
		}
		
	}
	
	 public static boolean isInteger(String s) {
        try { 
            Integer.parseInt(s); 
        } catch(Exception e) { 
            return false; 
        }
        // only got here if we didn't return false
        return true;
    }
	 
	 public String getRelativePosPlayer(String player) {
		 if (players[0].equals(player)) {
				return "south";
			} else if (players[1].equals(player)) {
				return "west";
			} else if( players[2].equals(player)) {
				return "north";
			} else if( players[3].equals(player)) {
				return "east";
			} else {
				System.out.println("ERROR: unknown player plays card!");
				System.exit(1);
				return "";
			}
	 }
	 
	 public static String removeLastNewLines(String input) {
		 while(input.endsWith("\n")) {
			 input = input.substring(0, input.length() - 1);
		}
		 input = input.trim();
		
		 return input;
	 }

	

	    
	    //TODO: UTIL:

		public static void printSortedCards(String[] cards) {
			
			ArrayList<String> tempCardList = new ArrayList<String>();
			
			for(int i=0; i<cards.length; i++) {
				tempCardList.add(cards[i]+ "");
			}
			String printLine = "";
			
			tempCardList = sort(tempCardList);
			
			for(int i=0; i<tempCardList.size(); i++) {
				printLine += tempCardList.get(i) + " ";
			}
			System.out.println(printLine);
			
		}
		//UTIL
		//Lazy O(n^2) sort: (a hand is only 13 cards... so the sorting of it could be inefficient for this purpose)
		public static ArrayList<String> sort(ArrayList<String> cardList) {
			String tmp;
			
			for(int i=0; i<cardList.size(); i++) {
				for(int j=i+1; j<cardList.size(); j++) {
					if(getMellowCardNumber(cardList.get(i)) > getMellowCardNumber(cardList.get(j))  ) {
						tmp = cardList.get(i) + "";
						cardList.set(i, cardList.get(j) + "");
						cardList.set(j, tmp + "");
					}
				}
			}
			
			return cardList;
		}
		

		
		private static int getMellowCardNumber(String cardString) {
			int x = -1;
			int y = -1;
			if(cardString.charAt(0) >= '2' && cardString.charAt(0) <= '9') {
				x = (int)cardString.charAt(0) - (int)('2');
			} else if(cardString.charAt(0) == 'T') {
				x = 8;
			} else if(cardString.charAt(0) == 'J') {
				x = 9;
			} else if(cardString.charAt(0) == 'Q') {
				x = 10;
			} else if(cardString.charAt(0) == 'K') {
				x = 11;
			} else if(cardString.charAt(0) == 'A') {
				x = 12;
			} else {
				System.out.println("Number unknown! Uh oh!");
				System.exit(1);
			}
			
			if(cardString.charAt(1)=='S') {
				y = 0;
			} else if(cardString.charAt(1)=='H') {
				y = 1;
			} else if(cardString.charAt(1)=='C') {
				y = 2;
			} else if(cardString.charAt(1)=='D') {
				y = 3;
			} else {
				System.out.println("Suit unknown! Uh oh!");
				System.exit(1);
			}
			
			return y*13 - x;
		}
		
}

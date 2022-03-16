package mellow.ai.listener;

import clientPlayers.GamePlayerInterface;

//import connectfour.Position;
import deck.DeckFunctions;

import java.util.ArrayList;
import java.util.concurrent.locks.*;

import mellow.ai.aiDecider.MellowAIDeciderFactory;
import mellow.ai.aiDecider.MellowAIDeciderInterface;
import mellow.cardUtils.CardStringFunctions;


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
	
	private boolean itsYourBid;
	private boolean itsYourTurn;
	
	private MellowAIDeciderInterface gameAIAgent = null;
	
	private String currentPlayerName = null;

	private boolean currentPlayerInFirstTeam = true;
	private int tempPlayerAStartScore= 0;

	//End state variables
	
	public MellowAIListener(long aiLevel) {
		gameAIAgent = MellowAIDeciderFactory.getAI(aiLevel);
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

	
	//TODO: what if the caller or this method is synchronized instead?
	//pre: server message is only 1 transmission
	public synchronized String getClientResponse(String serverMessage) {
		
		System.out.println("Mellow ack received: " + serverMessage);
		
		if( serverMessage.length() == 0) {
			return "";
		}
		
		if(serverMessage.startsWith(PUBLIC_MSG)) {
			if(serverMessage.contains(START_MSG)) {
				
				players = new String[NUM_PLAYERS];
				players[0] = serverMessage.split(" ")[4];
				players[1] = serverMessage.split(" ")[8];
				players[2] = serverMessage.split(" ")[6];
				players[3] = serverMessage.split(" ")[10];
				
				//Make current player/AI index 0:
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
				
				gameAIAgent.resetStateForNewRound();
				
				this.gameStarted = true;
			
			} else if(serverMessage.contains(START_POINTS_1) && serverMessage.contains(START_POINTS_2)) {
				
				//Update one teams score:
				String player1 = serverMessage.split(" ")[2].toLowerCase();
				String player2 = serverMessage.split(" ")[4].toLowerCase();
				System.out.println("player 1:" + player1);
				System.out.println("player 2:" + player2);
				System.out.println("current player" + currentPlayerName);
				System.out.println(currentPlayerInFirstTeam);
				
				boolean currentPlayerInList = (currentPlayerName.toLowerCase().equals(player1) || currentPlayerName.toLowerCase().equals(player2));
				
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
						gameAIAgent.setNewScores(tempPlayerAStartScore, score);
					} else {
						gameAIAgent.setNewScores(score, tempPlayerAStartScore);
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
					System.err.println("UH OH! Could not find card: " + card);
					System.exit(1);
				}
				
				String direction = getRelativePosPlayer(player);
				System.out.println(direction + "(" + player + ") plays " + card);
				
				gameAIAgent.receiveCardPlayed(player, card);
				
			} else if(serverMessage.contains(DEALER_MSG)) {
				String dealer = serverMessage.split(" ")[serverMessage.split(" ").length - 1];
				
				dealer = removeLastNewLines(dealer);

				gameAIAgent.setDealer(dealer);

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
							gameAIAgent.setNewScores(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[tokens.length - 1]));
						} else {
							gameAIAgent.setNewScores(Integer.parseInt(tokens[tokens.length - 1]), Integer.parseInt(tokens[0]));
						}
						
						gameAIAgent.resetStateForNewRound();
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
					gameAIAgent.setCardsForNewRound(cardsInHandTemp);
				}
			}
		}
	
		if(itsYourBid) {
			itsYourBid = false;
			itsYourTurn = false;
			
			String bid = gameAIAgent.getBidToMake();
			
			if(bid == null || bid.trim().equals("") || bid.contains("-")) {
				bid = "1";
			}
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
			
			String cardToPlay ="";
			boolean mightBeGoodCard = false;
			do {
				cardToPlay = gameAIAgent.getCardToPlay();
				
				mightBeGoodCard = !(cardToPlay == null || cardToPlay.trim().equals(""));
				
				if(mightBeGoodCard == false) {
					System.out.println("Invalid card! Try again:");
				}
				
			} while(mightBeGoodCard == false);
			
			if(cardToPlay == null || cardToPlay.trim().equals("")) {
				System.out.println("ERROR: DIDNT FIND A CARD!");
				System.out.println("Sending invalid card, so the server will decide what to play.");
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
				System.err.println("ERROR: unknown player plays card!");
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

	
	public static void printSortedCards(String[] cards) {
		String printLine = "";
		
		cards = CardStringFunctions.sort(cards);
		
		for(int i=0; i<cards.length; i++) {
			printLine += cards[i] + " ";
		}
		System.out.println(printLine);
		
	}
		
}

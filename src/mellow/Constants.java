package mellow;

public class Constants {

	public static final int NUM_PLAYERS = 4;
	public static final int NUM_SUITS = 4;
	public static final int NUM_RANKS = 13;
	public static final int NUM_CARDS = NUM_SUITS * NUM_RANKS;
	
	public static final int NUM_STARTING_CARDS_IN_HAND = 13;
	
	public static final int CURRENT_AGENT_INDEX = 0;
	
	public static final int SPADE = 0;
	public static final int HEART = 1;
	public static final int CLUB = 2;
	public static final int DIAMOND = 3;
	
	public static final String OFFSUITS[] = {"H", "C", "D"};

	//Current player index is 0
	//Left guy index is 1
	//Partner is 2
	//Right guy index is 3
	public static final int CURRENT_PLAYER_INDEX = 0;
	public static final int LEFT_PLAYER_INDEX = 0;
	
	public static final int CURRENT_PARTNER_INDEX = 2;
	public static final int RIGHT_PLAYER_INDEX = 3;
	
	
	public static String FULL_DECK[] = new String[]{"2S", "3S", "4S", "5S", "6S", "7S", "8S", "9S", "TS", "JS", "QS", "KS", "AS",
							                        "2H", "3H", "4H", "5H", "6H", "7H", "8H", "9H", "TH", "JH", "QH", "KH", "AH",
							                        "2C", "3C", "4C", "5C", "6C", "7C", "8C", "9C", "TC", "JC", "QC", "KC", "AC",
							                        "2D", "3D", "4D", "5D", "6D", "7D", "8D", "9D", "TD", "JD", "QD", "KD", "AD"};
	
}

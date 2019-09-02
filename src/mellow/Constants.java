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
	
}

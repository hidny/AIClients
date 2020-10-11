package mellow.ai.aiDecider;

import java.util.ArrayList;

import mellow.Constants;
import mellow.ai.cardDataModels.DataModel;
import mellow.ai.simulation.MonteCarloMain;
import mellow.ai.situationHandlers.NoMellowBidPlaySituation;
import mellow.cardUtils.CardStringFunctions;

//_______________________
//This is a basic AI that handles mellow rounds for someone who is leading, 2nd, and 3rd and 4th
//_____________________________
//After some calculations, I realized there are hundreds of unique mellow situations that I'd have to make rules for
//to force it to play like me.

//In complicated cases, I'm going to just make run it a monte-carlo simulation so the AI
//can make up it's own mind.

//Update: i'm just going to start simple and play against it and notice where it lacks.
// At that point, I'll improve it based on need

//Same idea goes for monte-carlo which depends on a good and fast decider and a good filter/weigher to work

//When it's good enough, I'll think about doing a decision tree that learned based on the features
//and/or using deep learning.... but that's not anytime soon.


public class MellowBasicDecider implements MellowAIDeciderInterface {
	
	
	DataModel dataModel;
	private boolean doMonteCarloSimuations = false;
	private int num_simulations_for_monte_carlo = MonteCarloMain.NUM_SIMULATIONS_DEFAULT;
	
	//TODO: Consider where the dealer is when bidding (and consider previous bids)
	
	//TODO: handle case where there's a mellow and then double mellow...
	// :(
	
	
	 
	public MellowBasicDecider() {
		this(false, MonteCarloMain.NUM_SIMULATIONS_DEFAULT);
	}
	
	public MellowBasicDecider(boolean doSimuations) {
		this(doSimuations, MonteCarloMain.NUM_SIMULATIONS_DEFAULT);
	}
	
	public MellowBasicDecider(boolean doSimuations, int num_simulations) {
		this.dataModel = new DataModel();
		this.doMonteCarloSimuations = doSimuations;
		this.num_simulations_for_monte_carlo = num_simulations;
	}
	
	
	//Use datamodel that already exists:
	//(this helps with creating a decider that enters in the middle of the game.)
	public MellowBasicDecider(DataModel dataModelInput) {
		this.dataModel = dataModelInput;
	}
	
	public static final int SPADE = 0;
	public static final int HEART = 1;
	public static final int CLUB = 2;
	public static final int DIAMOND = 3;
	
	//index
	//0: myCardsUsed
	//1: west cards
	//2: north cards
	//3: east cards
	
	
	public void resetStateForNewRound() {
		dataModel.resetStateForNewRound();
	}

	@Override
	public void receiveUnParsedMessageFromServer(String msg) {
		// TODO: use if you want...
	}
	
	public String toString() {
		return "MellowBasicDeciderAI";
	}
	
	@Override
	public void setNameOfPlayers(String players[]) {
		dataModel.setNameOfPlayers(players);
	}
	
	
	//TODO: don't make this so destructive... OR: have testcase say orig card in hand!!!
	@Override
	public void setCardsForNewRound(String cards[]) {
		dataModel.setupCardsInHandForNewRound(cards);
	}


	@Override
	public void setDealer(String playerName) {
		dataModel.setDealer(playerName);
	}

	@Override
	public void receiveBid(String playerName, int bid) {
		dataModel.setBid(playerName, bid);
	}

	@Override
	public void receiveCardPlayed(String playerName, String card) {
		dataModel.updateDataModelWithPlayedCard(playerName, card);
	}

	@Override
	public void setNewScores(int AIScore, int OpponentScore) {
		dataModel.setNewScores(AIScore, OpponentScore);
		
	}

	@Override
	public String getCardToPlay() {
	
		String cardToPlay = getCardToPlay2();
		
		//Make sure card is legal for simulation:
		if(dataModel.isCardLegalToPlay(cardToPlay)) {
			System.out.println("TEST: " + dataModel.getPlayers()[0] + " plays the " + cardToPlay);
			return cardToPlay;
		} else {
			
			System.out.println("WARNING: calling getFirstLegalCardThatCouldBeThrown in basic mellow decider");
			return dataModel.getFirstLegalCardThatCouldBeThrown();
		}
	}
	
	private String getCardToPlay2() {
		
		
		if(dataModel.throwerCanOnlyPlayOneCard()) {
			System.out.println("**Forced to play card");
			return dataModel.getOnlyCardCurrentPlayerCouldPlay();
		}

		for(int i=0; i<4; i++) {
			if(dataModel.burntMellow(i)) {
				System.out.println("TEST BURNT MELLOW index: " + i);
			}
		}
		
		//Run montecarlo simulations if config set to monte carlo 
		//AND decider is not currently in a simulation: (Running a simulation in a simulation is expensive)
		if(this.doMonteCarloSimuations && dataModel.getSimulation_level() == 0) {
			return MonteCarloMain.runMonteCarloMethod(dataModel, this.num_simulations_for_monte_carlo);
		}
		
		int numActiveMellows = 0;
		for(int i=0; i<4; i++) {
			if(dataModel.getBid(i) == 0 && dataModel.burntMellow(i) == false) {
				numActiveMellows++;
			}
		}
		
		if(numActiveMellows == 0) {
			return NoMellowBidPlaySituation.handleNormalThrow(dataModel);
			
		} else if(numActiveMellows == 1) {
			if(dataModel.getBid(Constants.CURRENT_AGENT_INDEX) == 0 && dataModel.burntMellow(Constants.CURRENT_AGENT_INDEX) == false) {
				System.out.println("MELLOW TEST HERE");
				return handleThrowAsSingleActiveMellowBidder();
				
			} else {
				System.out.println("MELLOW SOMEWHERE");
				if(dataModel.getBid(Constants.CURRENT_PARTNER_INDEX) == 0) {
					//TODO: Working on the october 3rd, 2020:
					return playMoveToProtectPartnerMellow(dataModel);

				} else {
					
					if(dataModel.getBid(Constants.RIGHT_PLAYER_INDEX) == 0) {
						//TODO: not quite right: (Need to protect mellow or attack mellow...)
						
						//TODO: find out which testcases got fixed (and which broken after change)
						return playMoveSeatedLeftOfOpponentMellow(dataModel);
						//return NoMellowBidPlaySituation.handleNormalThrow(dataModel);
					} else {
						
						//TODO: figure out how to play before a mellow (this is a hard position...)
						//Knowing when to trump is complicated...
						
						//return NoMellowBidPlaySituation.handleNormalThrow(dataModel);
						return playMoveSeatedRightOfOpponentMellow(dataModel);
					}
				}
			}
			
		} else if(numActiveMellows == 2) {
			if(dataModel.getBid(Constants.CURRENT_AGENT_INDEX) == 0) {
				
				
				System.out.println("MELLOW TEST (double mellow)");
				return handleThrowAsSingleActiveMellowBidder();

			} else {

				//TODO: not quite right:
				//This should be the most complicated logic of the game,
				//but let's start simple!
				return playMoveToProtectPartnerMellow(dataModel);
				
			}
			
		} else {
			System.err.println("Really??? There's either more than 2 or less than 0 mellows.");
			System.exit(1);
			return "";
		}
		
	}

	
	private String handleThrowAsSingleActiveMellowBidder() {

		int throwIndex = dataModel.getCardsPlayedThisRound() % Constants.NUM_PLAYERS;
		//leader:
		String cardToPlay = null;
		System.out.println("**Inside get card to play");
		
		if(dataModel.burntMellow(Constants.CURRENT_AGENT_INDEX)) {
			System.err.println("ERROR: BURNT MELLOW, but entering mellow bidder logic");
			System.exit(1);
		}
		
		if(throwIndex == 0) {
			cardToPlay = AIMellowLead();
			
		//Handle the common mellow follow case:
		} else {
			cardToPlay = AIMellowFollow();
		}
		
		if(cardToPlay != null) {
			System.out.println("AI decided on " + cardToPlay);
		}
		
		return cardToPlay;
	}
	
	private String AIMellowLead() {
		
		String ret = "";
		if(dataModel.getNumberOfCardsOneSuit(SPADE) > 0) {
			

			//TODO: if highest Spade is Queen, maybe don't do this?
			//TOO complicated... :(
			ret = dataModel.getCardCurrentPlayerGetHighestInSuit(SPADE);
		} else {
			ret = dataModel.getLowOffSuitCardToPlayElseLowestSpade();
		}
		
		return ret;
	}
	
	private String AIMellowFollow() {
		int leaderSuitIndex = dataModel.getSuitOfLeaderThrow();
		
		String cardToPlay = "";
		
		if(dataModel.currentAgentHasSuit(leaderSuitIndex)) {
			//Follow suit:
			
			//if no one has trump:
			String currentFightWinner = dataModel.getCurrentFightWinningCard();
			
			if(CardStringFunctions.getIndexOfSuit(currentFightWinner) == leaderSuitIndex) {
				
				if(dataModel.couldPlayCardInHandUnderCardInSameSuit(currentFightWinner)) {

					//TODO: check for the case where you'd rather play your 5 over the 4 even if you have a 2.

					//Play just below winning card if possible
					cardToPlay = dataModel.getCardInHandClosestUnderSameSuit(currentFightWinner);
				
				} else {
					//Play slightly above winning card and hope you'll get covered
					cardToPlay = dataModel.getCardInHandClosestOverSameSuit(currentFightWinner);
				}
			} else if(CardStringFunctions.getIndexOfSuit(currentFightWinner) == SPADE && leaderSuitIndex != SPADE) {
				//Someone trumped, play high
				cardToPlay = dataModel.getCardCurrentPlayerGetHighestInSuit(leaderSuitIndex);
				
			} else {
				System.err.println("ERROR in AIMellowFollow: The currently winning card should be the lead suit or spade.");
				System.exit(1);
			}
			
			
		} else {
			//Can't follow suit:
			
			//TODO: throw off card that gets rid of the most risk... not necessarily the spade.
			
			String currentFightWinner = dataModel.getCurrentFightWinningCard();
			if(CardStringFunctions.getIndexOfSuit(currentFightWinner) == SPADE) {
				//Play spade under trump (this isn't always a good idea, but...)
				//TODO: maybe consider not throwing off spade?
				
				if(dataModel.currentAgentHasSuit(SPADE)) {
					if(dataModel.couldPlayCardInHandUnderCardInSameSuit(currentFightWinner)) {
						//Play spade under trump
						return dataModel.getCardInHandClosestUnderSameSuit(currentFightWinner);
						 
					} else if(dataModel.getNumberOfCardsOneSuit(SPADE) == dataModel.getNumCardsInCurrentPlayerHand()) {
						
						//If mellow player only has spade over trump, play lower spade:
						return dataModel.getCardInHandClosestOverSameSuit(currentFightWinner);
					}
					
				}
			}

			//Algo that tries to throw off least desirable offsuit for mellow player
			//NOTE: this is BARELY, better than dataModel.getHighestOffSuitCardToLead(). LOL
			cardToPlay = getBestOffSuitCardToThrowOffAsMellowPlayer();
		}
		
		return cardToPlay;
	
	}

	//Find the suit the mellow player wants to throw-off most:
	public String getBestOffSuitCardToThrowOffAsMellowPlayer() {
		int NO_SUIT_FOUND = -1;
		int chosenSuit = NO_SUIT_FOUND;
		double bestScore = Double.MIN_VALUE;
		
		for(int i=0; i<Constants.NUM_SUITS; i++) {
			if(i == Constants.SPADE) {
				continue;

			} else if(dataModel.currentAgentHasSuit(i) == false) {
				continue;
			}
			if(chosenSuit == -1) {
				chosenSuit = i;
			}
			
			double tmpScore = getWillingnessToThrowOffSuitAsMellowPlayer(i);
			
			if(tmpScore > bestScore) {
				chosenSuit = i;
				bestScore = tmpScore;
			}
			
		}
		
		if(chosenSuit == NO_SUIT_FOUND) {
			//If mellow player has to play spade, go low
			//TODO: add logic about this.
			String cardTemp = dataModel.getLowOffSuitCardToPlayElseLowestSpade();
			
			return cardTemp;
		}

		return dataModel.getCardCurrentPlayerGetHighestInSuit(chosenSuit);
	}
	
	
	//The higher the number return, the more will the AI is to throw off the suit
	//For now, the numbers returned are between 0 and 1
	//pre condition: player has at least 1 card in suit input
	public double getWillingnessToThrowOffSuitAsMellowPlayer(int suit) {
		int numCardsInPlayNotInHand = Constants.NUM_RANKS 
								- dataModel.getNumCardsPlayedForSuit(suit)
								- dataModel.getNumCardsOfSuitInCurrentPlayerHand(suit);
		
		
		String cardToConsider = "";
		
		if(numCardsInPlayNotInHand > 6 && dataModel.getNumCardsOfSuitInCurrentPlayerHand(suit) >= 3) {
			cardToConsider = dataModel.getCardCurrentPlayergetThirdLowestInSuit(suit);

		} else if(numCardsInPlayNotInHand > 3 && dataModel.getNumCardsOfSuitInCurrentPlayerHand(suit) >= 2) {
			cardToConsider = dataModel.getCardCurrentPlayergetSecondLowestInSuit(suit);
			
		} else {
			cardToConsider = dataModel.getCardCurrentPlayergetLowestInSuit(suit);

		}
		
		int numOver = dataModel.getNumCardsInPlayNotInCurrentPlayersHandOverCardSameSuit(cardToConsider);
		int numUnder = dataModel.getNumCardsInPlayNotInCurrentPlayersHandUnderCardSameSuit(cardToConsider);
		
		//Check if suit is "safe":
		if(numUnder == 0) {
			return 0.0;
			
		} else {
			//Suit is not safe:
			double partnerCantCoverFactor = Math.pow(2.0/3.0, numOver);
		
		
			double giveUpOnSuitFactor = Math.pow(1.0/2.0, Math.max(0, dataModel.getNumCardsCurrentUserStartedWithInSuit(suit)));
		
			return partnerCantCoverFactor * giveUpOnSuitFactor;
		}
	}
	
	//TODO: will need to test/improve get BidToMake.
	
	@Override
	public String getBidToMake() {
		//Converted python function from github to java here:
		
		double bid = 0.0;
		
		//#Add number of aces:
		//bid = bid + getNumberOfAces(hand)
		bid += dataModel.getNumberOfAces();
		
		System.out.println("Number of aces: " + dataModel.getNumberOfAces());
		
		//if trumping means losing a king or queen of spades, then it doesn't mean much
		boolean trumpingIsSacrifice = false;
		
		
		//Add king of spades if 1 or 2 other spade
		if (dataModel.hasCard("KS") && dataModel.getNumberOfCardsOneSuit(0) >= 2) {
		
			bid += 1.0;
			if (dataModel.getNumberOfCardsOneSuit(0) == 2) {
				bid = bid - 0.2;
				
				trumpingIsSacrifice = true;
			}
			System.out.println("I have the KS");
		}
			
		//Add queen of spacdes if 2 other spaces
		if (dataModel.hasCard("QS") && dataModel.getNumberOfCardsOneSuit(0) >= 3) {
			bid += 1.0;
			trumpingIsSacrifice = true;
			System.out.println("I have the QS");
		}
			
	
		if(dataModel.getNumberOfCardsOneSuit(0) >= 4 ) {
			trumpingIsSacrifice = true;
		}
		
		double trumpResevoir= 0.0;
		if (dataModel.getNumberOfCardsOneSuit(0) == 3) {
			trumpResevoir = 0.44;
		}
		
		//Add a bid for every extra spade over 3 you have:
		if (dataModel.getNumberOfCardsOneSuit(0) >= 4) {
			bid += dataModel.getNumberOfCardsOneSuit(0) - 4.0;
			
			if (dataModel.hasCard("JS")) {
				bid +=  1.0;
			} else if (dataModel.hasCard("TS")) {
				bid += 0.8;
				trumpResevoir = 0.201;
			} else if(dataModel.getNumberOfCardsOneSuit(1) < 2 ||  dataModel.getNumberOfCardsOneSuit(2) < 2 ||  dataModel.getNumberOfCardsOneSuit(3) < 2) {
				bid += 0.7;
				trumpResevoir = 0.301;
			} else {
				trumpResevoir = 1.001;
			}
			
		}
			//TODO: 5+ spades should give a special bonus depending on the offsuits.
		    // The "take everything" bonus :P
		
		
		int numOffSuitKings = 0;
		if(dataModel.hasCard("KS")) {
			numOffSuitKings = dataModel.getNumberOfKings() - 1;
		} else {
			numOffSuitKings = dataModel.getNumberOfKings();
		}
		
		bid += 0.75 * numOffSuitKings;
		
		//offsuit king adjust if too many or too little of a single suit:
		for(int i=0; i<Constants.OFFSUITS.length; i++) {
			String off = Constants.OFFSUITS[i];
			if(dataModel.hasCard("K" + off) && dataModel.getNumberOfCardsOneSuit(i + 1) == 1) {
				bid = bid - 0.55;
			} else if(dataModel.hasCard("K" + off) && dataModel.getNumberOfCardsOneSuit(i + 1) > 5) {
				bid = bid - 0.65;
			} else if(dataModel.hasCard("K" + off) && dataModel.getNumberOfCardsOneSuit(i + 1) > 4) {
				bid = bid - 0.25;
			} else if(dataModel.hasCard("K" + off) && dataModel.getNumberOfCardsOneSuit(i + 1) > 3) {
				bid = bid - 0.20;
			}
			
			if(dataModel.hasCard("K" + off) && (dataModel.hasCard("Q" + off) || dataModel.hasCard("A" + off))) {
				bid = bid + 0.26;
			}
		}
		
		//END offsuit king adjustment logic
			
		if(dataModel.getNumberOfCardsOneSuit(1) < 3 || dataModel.getNumberOfCardsOneSuit(2) < 3|| dataModel.getNumberOfCardsOneSuit(3) < 3) {
			if(dataModel.getNumberOfCardsOneSuit(0) >= 2 && dataModel.getNumberOfCardsOneSuit(0) < 4 && trumpingIsSacrifice == false ) {

				bid += 0.3;
				
				if( (dataModel.getNumberOfCardsOneSuit(1) < 3 && dataModel.getNumberOfCardsOneSuit(2) < 3) ||
						(dataModel.getNumberOfCardsOneSuit(1) < 3 && dataModel.getNumberOfCardsOneSuit(3) < 3) ||
						(dataModel.getNumberOfCardsOneSuit(2) < 3 && dataModel.getNumberOfCardsOneSuit(3) < 3) ) {
					bid += 0.75; 
				} else if(dataModel.getNumberOfCardsOneSuit(1) < 2 || dataModel.getNumberOfCardsOneSuit(2) < 2 || dataModel.getNumberOfCardsOneSuit(3) < 2) {
					bid += 0.75;
				}

			} else if(dataModel.getNumberOfCardsOneSuit(0) >= 4 && trumpResevoir > 0) {
				if( (dataModel.getNumberOfCardsOneSuit(1) < 3 && dataModel.getNumberOfCardsOneSuit(2) < 3) ||
						(dataModel.getNumberOfCardsOneSuit(1) < 3 && dataModel.getNumberOfCardsOneSuit(3) < 3) ||
						(dataModel.getNumberOfCardsOneSuit(2) < 3 && dataModel.getNumberOfCardsOneSuit(3) < 3) ) {
					bid += trumpResevoir; 
				} else if(dataModel.getNumberOfCardsOneSuit(1) < 2 || dataModel.getNumberOfCardsOneSuit(2) < 2 || dataModel.getNumberOfCardsOneSuit(3) < 2) {
					bid += trumpResevoir;
				}
			}
		}
		
		//TODO: didn't handle mellow
		//Didn't handle akqjt (a sweep)
		
		if(dataModel.getNumberOfCardsOneSuit(0) == 0) {
			bid = bid  - 1;
		}
		
		System.out.println("Bid double: " + bid);
		int intBid = (int) Math.floor(bid);
		
		if (intBid < 0) {
			intBid = 0;
		}
	
		System.out.println("Final bid " + intBid);

		
		return intBid + "";
	}

	
	//Play move to protect partner mellow
	//The optimal play here is really complicated...
	//I'll just approx by writing a simple answer that's easy to read
	
	public static String playMoveToProtectPartnerMellow(DataModel dataModel) {
		
		//TODO: test!
		
		//Advise for refining this function:
		//TODO: Instead of making it perfect considering all factors, play test it and see when it gets into trouble
		//EX: don't worry about counting number of cards left in suit and number of spades left and blah blah blah...
		//EX2: if player needs to play unsafe card, don't worry about having no common-sense to deal with it.
		
		//1st prio:
		if(dataModel.currentThrowIsLeading()) {
			
			int bestSuitIndexToPlay = -1;
			int valueOfBestSuitPlay = -1;

			System.out.println("DEBUG: reached mellow protection test. Test0!");
			
			//1st check that there's an offsuit current player has, but mellow signalled they don't have:
			for(int curSuit=0; curSuit<Constants.NUM_SUITS; curSuit++) {
				if(dataModel.currentAgentHasSuit(curSuit)) {

					if(dataModel.mellowPlayerSignalNoCardsOfSuit(Constants.CURRENT_PARTNER_INDEX, curSuit)) {
						//if mellow player signal no to suit
						
						//TODO: check if any test cas even reaches this point
						System.out.println("DEBUG: reached mellow protection test. Test1!");
						
						//value of suit: num cards opponents have of suit
						int numCardsOpponentsCurrentlyHaveOfSuit = Constants.NUM_RANKS -
							dataModel.getNumCardsPlayedForSuit(curSuit)
							 - dataModel.getNumCardsCurrentUserStartedWithInSuit(curSuit);
						
						int currentValueOfSuitPlay = numCardsOpponentsCurrentlyHaveOfSuit;
						
						//If current player has master of suit:
						//that's good! (I can't tell how good, but it's pretty.. pretty.. pretty good.)
						if(dataModel.currentPlayerHasMasterInSuit(curSuit)) {
							//Add 2 to value because why not?
							currentValueOfSuitPlay += 2;
						}
						
						//Exceptions:
						//if opponent to the right is void and if opponent can trump: playing suit is not great
						//playing this suit is good but not great... but maybe it's still the best choice...
						if(curSuit != Constants.SPADE && dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, curSuit)
								&& dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE) == false) {
							currentValueOfSuitPlay = 1;
						}

						//if suit is trump, see if there's another suit
						if(curSuit == Constants.SPADE) {
							currentValueOfSuitPlay = 1;
						}
						//TODO: I don't know what I'd choose between leading trump and leading into opponent because it's context dependant
						//Whatever!
						
						if(currentValueOfSuitPlay > valueOfBestSuitPlay) {
							
							bestSuitIndexToPlay = curSuit;
							valueOfBestSuitPlay = currentValueOfSuitPlay;
						}
					}
						
					
				}
			}
			
			if(bestSuitIndexToPlay != -1) {
				if(dataModel.currentPlayerHasMasterInSuit( bestSuitIndexToPlay)) {
					return dataModel.getMasterInSuit( bestSuitIndexToPlay);
				} else {
					return dataModel.getCardCurrentPlayergetLowestInSuit( bestSuitIndexToPlay);
				}
				
			}
			
			//At this point, there's no obvious lead...
			
			//Try to play to a master (if applicable)
			for(int curSuit=0; curSuit<Constants.NUM_SUITS; curSuit++) {
				if(dataModel.currentPlayerHasMasterInSuit(curSuit)) {
					
					//TODO: this is really dangerous if mellow protector doesn't have a high second card...
					//Ex: A 3 2, and you lead the A leaving the 3 and 2.

					return dataModel.getMasterInSuit(curSuit);
				}
			}
			
			String bestCardToPlay = null;
	
			valueOfBestSuitPlay = -1;

			//Try to play the lowest card above mellow player safe signal in offsuit:
			for(int curSuit=0; curSuit<Constants.NUM_SUITS; curSuit++) {

				System.out.println("DEBUG: reached mellow protection test where we're just trying to lead a nice card over mellow player. Test2!");
				
				//?? I'll need to make up a value judgement...
				int currentValueOfSuitPlay = -1;
				String currentCardToPlay = null;
				
				//int currentValueOfSuitPlay = numCardsOpponentsHaveOfSuit;
				int numCardsOtherPlayersCurrentlyHaveOfSuit = Constants.NUM_RANKS -
						dataModel.getNumCardsPlayedForSuit(curSuit)
						 - dataModel.getNumCardsCurrentUserStartedWithInSuit(curSuit);
				
				String maxRankMellowPartnerCard = dataModel.getMaxRankCardMellowPlayerCouldHaveBasedOnSignals(Constants.CURRENT_PARTNER_INDEX, curSuit);
				
				if(maxRankMellowPartnerCard != null
					&& dataModel.couldPlayCardInHandOverCardInSameSuit(maxRankMellowPartnerCard)) {
					currentCardToPlay = dataModel.getCardInHandClosestOverSameSuit(maxRankMellowPartnerCard);
					
					//TODO: I just made this formula up because I felt like it... 
					currentValueOfSuitPlay = Constants.NUM_RANKS - 
							dataModel.getRankIndex(currentCardToPlay)
							+ (numCardsOtherPlayersCurrentlyHaveOfSuit / 2);
				}
				
				
				//Exception:
				//if opponent to the right is void and if opponent can trump: playing suit is not great
				if(curSuit != Constants.SPADE && dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, curSuit)
						&& dataModel.isVoid(Constants.RIGHT_PLAYER_INDEX, Constants.SPADE) == false) {
					currentValueOfSuitPlay = -2;
				}
				
				if(currentValueOfSuitPlay > valueOfBestSuitPlay) {
					bestCardToPlay = currentCardToPlay;
					valueOfBestSuitPlay = currentValueOfSuitPlay;
				}
				
				
			}
			if(bestCardToPlay != null) {
				return bestCardToPlay;
			}
			
			
			//Play spade?
			if(dataModel.currentAgentHasSuit(Constants.SPADE)) {
				return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
			} else {
				//play random highest card
				return dataModel.getHighestOffSuitCardAnySuit();
			}
			
			
		} else {
			//TODO: do this later:
			//For now, just test mellow lead
			
			//TODO: throw index should be a dataModel functionL
			int throwIndex = dataModel.getCardsPlayedThisRound() % Constants.NUM_PLAYERS;
			
			if(throwIndex == 1) {
			
			//if 2nd thrower:
				//if void and lead suit is off-suit
					//if could trump:
						//If mellow player partner seems vulnerable based on signals: trump
						//else dont trump
				
					//else: throw out garbage card...
						//Later try to pick best garbage card 
				//else if void and lead suit is trump
					//play garbage
				//else if you need to follow suit
					//Get highest (I know this isn't always a good idea, but whatever man!)
					
					//Exception: if mellow player is not vulnerable this round and made more vulnerable next round... maybe don't!)
					//(Example: lead K and playing A is questionable if you have a 2 or 3 to backup the A)
			} else if(throwIndex == 2) {
				
				//Note: this could only happen on the 1st round where mellow player leads.
				System.out.println("TESTING PROTECTOR 3rd throw");
				
				int leadSuit = dataModel.getSuitOfLeaderThrow();
				
				String currentFightWinnerCard = dataModel.getCurrentFightWinningCard();
				
				if(currentFightWinnerCard == dataModel.getCardLeaderThrow()) {
		
					if(dataModel.throwerHasCardToBeatCurrentWinner()) {
						//if mellow in danger:
						
						//TODO: if 3rd and want to lead again: play master
						
						//play just above to protect
						return dataModel.getCardClosestOverCurrentWinner();
						
					} else {
						
						//Mellow might be lost, just play low :(
						if(dataModel.currentAgentHasSuit(leadSuit)) {
							return dataModel.getCardCurrentPlayergetLowestInSuit(leadSuit);
						} else {
							return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
						}
						
					}
					
					
					
				} else {

					//Just play low: bad logic, but whatever!
					if(dataModel.currentAgentHasSuit(leadSuit)) {
						return dataModel.getCardCurrentPlayergetLowestInSuit(leadSuit);
					} else {
						return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
					}
					
					//TODO later: complex logic (Maybe start with stealing from 4th throw logic)
					
				}
				
			} else if(throwIndex == 3) {

				System.out.println("TESTING PROTECTOR 4th throw");
			
				int leadSuit = dataModel.getSuitOfLeaderThrow();
				
				String currentFightWinnerCard = dataModel.getCurrentFightWinningCard();
				
				if(currentFightWinnerCard == dataModel.getCardSecondThrow()) {

					//if mellow in danger:
					//play just above to protect
					if(dataModel.throwerHasCardToBeatCurrentWinner()) {
						return dataModel.getCardClosestOverCurrentWinner();
						
					} else {
						
						//Mellow is lost, just play low :(
						if(dataModel.currentAgentHasSuit(leadSuit)) {
							return dataModel.getCardCurrentPlayergetLowestInSuit(leadSuit);
						} else {
							return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
						}
						
					}
					
				} else {
					
					//Just play low: bad logic, but whatever!
					if(dataModel.currentAgentHasSuit(leadSuit)) {
						return dataModel.getCardCurrentPlayergetLowestInSuit(leadSuit);
					} else {
						return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
					}
					
				
					//TODO later: complex logic (This is a complex situation!)
					//play to take if it doesn't endanger mellow in next round of same suit
					//or play to take if could lead nice high value card next round
					//or play to take if you need the trick...
			
					//play low if playing high endangers mellow card in next round of same suit
				}

					
					
			} else {
				System.out.println("ERROR: unexpected branching in playMoveToProtectPartnerMellow");
				System.exit(1);
			}
			
			return NoMellowBidPlaySituation.handleNormalThrow(dataModel);
		}
	}
	
	
	
	//TODO
	public static String playMoveSeatedLeftOfOpponentMellow(DataModel dataModel) {
		
		int throwIndex = dataModel.getCardsPlayedThisRound() % Constants.NUM_PLAYERS;
		
		//Rule number one:
		//TODO: break this up later!
		
		if(throwIndex == 0) {

			//TODO:
			//handle lead
			
			//TODO: insert complicated lead logic here...
			return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
			
			
		//go under mellow if possible! (Maybe put into anther function?
		} else if(throwIndex > 0 && 
				dataModel.isPrevThrowWinningFight() ) {
			
			String mellowWinningCard = dataModel.getCurrentFightWinningCard();
			
			if(CardStringFunctions.getIndexOfSuit(mellowWinningCard) 
					!= dataModel.getSuitOfLeaderThrow()) {
				//Mellow player is trumping:
				
				if(dataModel.throwerMustFollowSuit()) {
					//Mellow player is trumping, but current player is not trumping:
					return dataModel.getCardCurrentPlayergetLowestInSuit(dataModel.getSuitOfLeaderThrow());
					
				} else {
					if(dataModel.currentPlayerOnlyHasSpade() 
							&& dataModel.couldPlayCardInHandUnderCardInSameSuit(mellowWinningCard)) {
						
						//If you have to trump over, go big! (Over simplified, but whatever)
						return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
					
					} else {
						
						//TODO: this might need work, but I'm lazy
						return dataModel.getHighestOffSuitCardAnySuit();
					}
				}
			} else {
				
				if(dataModel.throwerMustFollowSuit()) {
					//Both follow suit
					
					if(dataModel.couldPlayCardInHandUnderCardInSameSuit(mellowWinningCard)) {
						return dataModel.getCardInHandClosestUnderSameSuit(mellowWinningCard);
					
					} else {
						return dataModel.getCardCurrentPlayerGetHighestInSuit(dataModel.getSuitOfLeaderThrow());
					}
					
				} else {
					
					//If can't follow suit:
					if(dataModel.currentAgentHasSuit(Constants.SPADE)) {
						//play biggest spade if you can... over-simplified
						return dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);
					} else {
						//play big off suit to mess-up mellow play (Over-simplified, but whatever)
						return dataModel.getHighestOffSuitCardAnySuit();
					}
				
				}
			}

			//End go under mellow if possible logic
			
			
		} else if(throwIndex > 0 && 
				dataModel.isPrevThrowWinningFight() == false) {

			//handle case where mellow is already safe:
			
			if(dataModel.throwerMustFollowSuit()) {
				dataModel.getCardCurrentPlayerGetHighestInSuit(dataModel.getSuitOfLeaderThrow());
				
			} else {
				if(dataModel.currentAgentHasSuit(Constants.SPADE)) {
					dataModel.getCardCurrentPlayerGetHighestInSuit(Constants.SPADE);

				} else {
					
					//TODO: This might be too much, but whatever...
					return dataModel.getHighestOffSuitCardAnySuit();
				}
			}
			
			//end handle case where mellow is already safe:
			
		}
		
		
		//TODO: don't be lazy in future (i.e. fill this up!)
		return NoMellowBidPlaySituation.handleNormalThrow(dataModel);
	}

	public static String playMoveSeatedRightOfOpponentMellow(DataModel dataModel) {
		
		int throwIndex = dataModel.getCardsPlayedThisRound() % Constants.NUM_PLAYERS;
		
		
		if(throwIndex == 0) {
			//handle lead
			
			//TODO: insert complicated lead logic here... (for example: make sure mellow player has card in suit)
			return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
		
		} else if(throwIndex == 1) {
			
			//TODO
			
		} else if(throwIndex == 2) {
			//TODO
			
		//Burn a mellow lead throw: (Very important to not mess this up!)
		} else if(throwIndex == 3 && 
				dataModel.getCardLeaderThrow().equals(dataModel.getCurrentFightWinningCard()) ) {
				//Mellow lead and losing (Like when grand-papa used to play)

			
			if(dataModel.throwerMustFollowSuit()
					&& dataModel.couldPlayCardInHandUnderCardInSameSuit(dataModel.getCardLeaderThrow())) {
				
				return dataModel.getCardCurrentPlayergetLowestInSuit(dataModel.getSuitOfLeaderThrow());
			
			} else if(dataModel.currentPlayerOnlyHasSpade() == false) {
				
				return dataModel.getLowOffSuitCardToPlayElseLowestSpade();
			}
			
		}
		//End burn a mellow lead throw
		
		
		//TODO: this is wrong, but whatever...
		return NoMellowBidPlaySituation.handleNormalThrow(dataModel);
	}
}

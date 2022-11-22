package mellowGenTestcaseStarter;

import clientPlayers.ClientStarter;
import mellow.ai.aiDecider.MellowAIDeciderFactory;

//Will host and wait until there's a full house...

//In fast speed setting: 1 test game takes 70 seconds. (Pretty fast!)
public class MichaelGenTestcases {
	public static void main(String[] args) {
		
		//Create testcases in Michael folder:
		String autoArgs[] = {"mellow", "Michael2022-3", "mellowpy", "" + MellowAIDeciderFactory.USER_INPUT_TESTCASES, "fast", "0", "0", "-1"};
		
		//Rigged
		//String autoArgs[] = {"mellow", "garbageTestData", "mellowpy", "" + MellowAIDeciderFactory.USER_INPUT_TESTCASES, "fast", "908", "967", "0", "[AS 5H 3C 5S JC 2C AC 2D KS 4S AD 3D TS][4D 6D 6S 8C KH 3H 7D QD 9H 4H QH 8H AH][JH QS TH 7S QC 9C 6C TD 2S JD 5D 8D KD][TC JS KC 5C 7H 4C 8S 2H 9S 7C 6H 3S 9D]"};
		
		//Rigged2 points reversed
		//String autoArgs[] = {"mellow", "garbageTestData", "mellowpy", "" + MellowAIDeciderFactory.USER_INPUT_TESTCASES, "fast", "967", "908", "0", "[AS 5H 3C 5S JC 2C AC 2D KS 4S AD 3D TS][4D 6D 6S 8C KH 3H 7D QD 9H 4H QH 8H AH][JH QS TH 7S QC 9C 6C TD 2S JD 5D 8D KD][TC JS KC 5C 7H 4C 8S 2H 9S 7C 6H 3S 9D]"};
		
		//Rigged3 points reversed
		//String autoArgs[] = {"mellow", "garbageTestData", "mellowpy", "" + MellowAIDeciderFactory.USER_INPUT_TESTCASES, "fast", "0", "0", "0", "[AS 5H 3C 5S JC 2C AC 2D KS 4S AD 3D TS][4D 6D 6S 8C KH 3H 7D QD 9H 4H QH 8H AH][JH QS TH 7S QC 9C 6C TD 2S JD 5D 8D KD][TC JS KC 5C 7H 4C 8S 2H 9S 7C 6H 3S 9D]"};
		//It matches!
		
		//Play with Richard
		//String autoArgs[] = {"mellow", "MichaelPlay", "mellowpy", "" + MellowAIDeciderFactory.USER_INPUT_TESTCASES, "fast", "900", "900", "-1"};
		
		//Another Rigged part 2:
		//String autoArgs[] = {"mellow", "Michael2022-3", "mellowpy", "" + MellowAIDeciderFactory.USER_INPUT_TESTCASES, "fast", "850", "399", "3", "[AS TS 9S 8S 5S AH JH TH 9H 6H 4H 7C 4D][QS 7S 3S 2S 8H 2H KC 6C 4C JD TD 7D 5D][6S KH 3H JC TC 9C 8C 5C 2C AD KD 8D 6D][KS JS 4S QH 7H 5H AC QC 3C QD 9D 3D 2D]"};
		
		//Part 2:
		//String autoArgs[] = {"mellow", "Michael2022-3", "mellowpy", "" + MellowAIDeciderFactory.USER_INPUT_TESTCASES, "fast", "908", "813", "1", "[][][QS JS KH 8H 4H TC 6C 4C 3C AD 7D 4D 3D][]"};
		

		//String autoArgs[] = {"mellow", "Michael2021-2", "mellowpy", "" + MellowAIDeciderFactory.USER_INPUT_TESTCASES, "fast", "0", "0", "-1"};
		
		//Start part 2 of game
		//String autoArgs[] = {"mellow", "Michael2021-2", "mellowpy", "" + MellowAIDeciderFactory.USER_INPUT_TESTCASES, "fast", "70", "242", "3", "[AS QS 9S 7S 6S AC 6C 4C 3C KD QD 7D 6D]"};
		
		
		//Put testcases in garbage folder
		//String autoArgs[] = {"mellow", "garbageTestData", "mellowpy", "" + MellowAIDeciderFactory.USER_INPUT_TESTCASES, "fast", "13", "37", "1"};
		
		//TEST testcases in april 2020:
		//String autoArgs[] = {"mellow", "MichaelApril2020", "mellowpy", "" + MellowAIDeciderFactory.USER_INPUT_TESTCASES, "fast", "0", "0", "3"};
		
		
		//TEST rigged deck
		//String autoArgs[] = {"mellow", "garbageTestData", "mellowpy", "" + MellowAIDeciderFactory.USER_INPUT_TESTCASES, "fast", "13", "37", "1", "[3D TD 5D 7C 7S 6S 5C JS 5H JC 8S 2S 8D QH JD KD 8H TS TC TH 7D 2C AC 4C 6D QS QC AS 2H KC AD 9D 6H JH 9C 3S 3C KS QD 5S AH 4H 9S 4D 8C 9H 3H KH 7H 6C 2D 4S]"};
		
		//TEST rigged hands:
		//String autoArgs[] = {"mellow", "garbageTestData", "mellowpy", "" + MellowAIDeciderFactory.USER_INPUT_TESTCASES, "fast", "13", "37", "1", "[AS KS QS JS] [AD 2D 4D]"};
			
		
		//TODO: Put testcases in double mellow folder (and rig a double mellow!)
		//String autoArgs[] = {"mellow", "doubleMellowTests", "mellowpy", "" + MellowAIDeciderFactory.USER_INPUT_TESTCASES, "fast", "0", "0", "1", "[JS 4S][TS 5S 2S][AS QS 9S 7S 3S AD AH][AC KS 8S 6S]"};
				
		try {
			ClientStarter.main(autoArgs);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

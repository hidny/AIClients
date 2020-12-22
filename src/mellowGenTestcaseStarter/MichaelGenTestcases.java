package mellowGenTestcaseStarter;

import clientPlayers.ClientStarter;
import mellow.ai.aiDecider.MellowAIDeciderFactory;

//Will host and wait until there's a full house...

//In fast speed setting: 1 test game takes 70 seconds. (Pretty fast!)
public class MichaelGenTestcases {
	public static void main(String[] args) {
		
		//Create testcases in Michael folder:
		//String autoArgs[] = {"mellow", "Michael", "mellowpy", "" + MellowAIDeciderFactory.USER_INPUT_TESTCASES, "fast", "0", "0", "3"};
		
		//Put testcases in garbage folder
		String autoArgs[] = {"mellow", "garbageTestData", "mellowpy", "" + MellowAIDeciderFactory.USER_INPUT_TESTCASES, "fast", "13", "37", "1"};
		
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

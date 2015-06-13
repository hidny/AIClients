package frustration.aiSTUB;

import java.util.ArrayList;


public class FrustrationDecider implements FrustrationDeciderInterface {
	
	public FrustrationDecider() {
		
	}
	
	
	public String getMove() {
		int index = (int)(2*Math.random());
		if(index ==0) {
			return "-3";
		} else {
			return "0";
		}
	}

	public void setNameOfPlayers(String players[]) {
		
	}
	
}

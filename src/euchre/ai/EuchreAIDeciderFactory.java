package euchre.ai;

public class EuchreAIDeciderFactory {

	public static EuchreAIDeciderInterface getAI(long aiLevel, boolean isFast) {
		if(aiLevel <= 1) {
			//TODO: this AI is simply able to play through the game.
			return new EuchreBasicDecider(isFast);
		} else if(aiLevel == 2) {
			return new EuchreBasicDecider(isFast);
		} else {
			//Default:
			return new EuchreBasicDecider(isFast);
		}
		
		
	}
}

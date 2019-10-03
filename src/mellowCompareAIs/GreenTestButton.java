package mellowCompareAIs;

public class GreenTestButton {

	//pre: server is up and running
	// and the game room is availabe.
	public static void main(String args[]) {
		try {
			//Michael is host and must go first.
			//phil is next on the left, rich is on left of phil and so on.
			Michael.main(args);
			Thread.sleep(1000);
			Phil.main(args);
			Thread.sleep(100);
			Richard.main(args);
			Thread.sleep(100);
			Doris.main(args);
		
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

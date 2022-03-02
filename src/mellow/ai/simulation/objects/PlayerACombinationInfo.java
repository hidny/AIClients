package mellow.ai.simulation.objects;

public class PlayerACombinationInfo {

	//This is more like a case class...
	
	//TODO:
	//Hold info like:
	//num ways
	// num combos for each type of relevant set. (See notes for details)
	
	
	//combo index number
	private int comboIndexNumber = -1;
	
	//curSumSoFar
	private long curSumWaysSoFar = -1L;
	
	private long numWaysAGroupABNotC = -1L;
	private int numCardsAGroupABNotC = -1;

	private long numWaysAGroupANotBC = -1L;
	private int numCardsAGroupANotBC = -1;

	private long numWaysAGroupABC = -1L;
	private int numCardsAGroupABC = -1;
	

	private long numWaysBGroupBC = -1L;
	private int numCardsBGroupBC = -1;
	
	public PlayerACombinationInfo(
			int comboIndexNumber,
			long curSumWaysSoFar,
			long numWaysAGroupABNotC,
			int numCardsAGroupABNotC,
			long numWaysAGroupANotBC,
			int numCardsAGroupANotBC,
			long numWaysAGroupABC,
			int numCardsAGroupABC,
			long numWaysBGroupBC,
			int numCardsBGroupBC) {

		this.comboIndexNumber = comboIndexNumber;
		this.curSumWaysSoFar = curSumWaysSoFar;
		this.numWaysAGroupABNotC = numWaysAGroupABNotC;
		this.numCardsAGroupABNotC = numCardsAGroupABNotC;
		this.numWaysAGroupANotBC = numWaysAGroupANotBC;
		this.numCardsAGroupANotBC = numCardsAGroupANotBC;
		this.numWaysAGroupABC = numWaysAGroupABC;
		this.numCardsAGroupABC = numCardsAGroupABC;
		this.numWaysBGroupBC = numWaysBGroupBC;
		this.numCardsBGroupBC = numCardsBGroupBC;
	}

	public int getComboIndexNumber() {
		return comboIndexNumber;
	}

	public long getCurSumWaysSoFar() {
		return curSumWaysSoFar;
	}

	public long getNumWaysAGroupABNotC() {
		return numWaysAGroupABNotC;
	}

	public int getNumCardsAGroupABNotC() {
		return numCardsAGroupABNotC;
	}

	public long getNumWaysAGroupANotBC() {
		return numWaysAGroupANotBC;
	}

	public int getNumCardsAGroupANotBC() {
		return numCardsAGroupANotBC;
	}
	
	public long getNumWaysAGroupABC() {
		return numWaysAGroupABC;
	}

	public int getNumCardsAGroupABC() {
		return numCardsAGroupABC;
	}

	public long getNumWaysBGroupBC() {
		return numWaysBGroupBC;
	}

	public int getNumCardsBGroupBC() {
		return numCardsBGroupBC;
	}
	
	
}

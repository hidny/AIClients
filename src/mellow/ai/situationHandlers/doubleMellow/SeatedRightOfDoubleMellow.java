package mellow.ai.situationHandlers.doubleMellow;

import mellow.ai.cardDataModels.DataModel;
import mellow.ai.situationHandlers.PartnerSaidMellowSituation;

public class SeatedRightOfDoubleMellow {
	
	public static String playMoveSeatedRightOfDoubleMellow(DataModel dataModel) {
		
		System.out.println("Right of double mellow");
		return PartnerSaidMellowSituation.playMoveToProtectPartnerMellow(dataModel);
	}
}

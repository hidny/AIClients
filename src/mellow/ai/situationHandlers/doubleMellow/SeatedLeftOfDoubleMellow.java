package mellow.ai.situationHandlers.doubleMellow;

import mellow.ai.cardDataModels.DataModel;
import mellow.ai.situationHandlers.PartnerSaidMellowSituation;

public class SeatedLeftOfDoubleMellow {

	public static String playMoveSeatedLeftOfDoubleMellow(DataModel dataModel) {
		
		System.out.println("Left of double mellow");
		return PartnerSaidMellowSituation.playMoveToProtectPartnerMellow(dataModel);
	}
}

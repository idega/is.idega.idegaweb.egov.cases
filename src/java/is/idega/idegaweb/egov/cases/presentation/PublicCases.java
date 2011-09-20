package is.idega.idegaweb.egov.cases.presentation;

import com.idega.block.process.business.CasesRetrievalManager;

public class PublicCases extends OpenCases {

	@Override
	protected String getBlockID() {
		return getCasesProcessorType();
	}

	@Override
	public String getCasesProcessorType() {
		return CasesRetrievalManager.CASE_LIST_TYPE_PUBLIC;
	}

}
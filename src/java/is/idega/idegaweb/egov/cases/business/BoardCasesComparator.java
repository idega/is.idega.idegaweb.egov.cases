package is.idega.idegaweb.egov.cases.business;

import is.idega.idegaweb.egov.cases.presentation.beans.CaseBoardBean;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;

public class BoardCasesComparator implements Comparator<CaseBoardBean> {

	private Locale locale;
	private List<String> sortingPreferences;
	private Collator theCollator;
	
	public BoardCasesComparator(Locale locale) {
		this.locale = locale;
		
		theCollator = Collator.getInstance(this.locale);
	}
	
	public BoardCasesComparator(Locale locale, List<String> sortingPreferences) {
		this(locale);
		this.sortingPreferences = sortingPreferences;
	}
	
	public int compare(CaseBoardBean bean1, CaseBoardBean bean2) {
		return theCollator.compare(getExpression(bean1), getExpression(bean2));
	}
	
	private String getExpression(CaseBoardBean bean) {
		if (ListUtil.isEmpty(sortingPreferences)) {
			//	Using default sorting: category + applicant name
			String category = StringUtil.isEmpty(bean.getCategory()) ? "z" : bean.getCategory();
			return category + bean.getApplicantName();
		}
		
		return CoreConstants.EMPTY;
	}

}

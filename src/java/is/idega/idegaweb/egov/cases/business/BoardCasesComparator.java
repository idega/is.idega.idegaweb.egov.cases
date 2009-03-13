package is.idega.idegaweb.egov.cases.business;

import is.idega.idegaweb.egov.cases.presentation.beans.CaseBoardBean;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.idega.util.ListUtil;

public class BoardCasesComparator implements Comparator<CaseBoardBean> {

	private Locale locale;
	private List<String> sortingPreferences;
	
	public BoardCasesComparator(Locale locale) {
		this.locale = locale;
	}
	
	public BoardCasesComparator(Locale locale, List<String> sortingPreferences) {
		this(locale);
		
		this.sortingPreferences = sortingPreferences;
	}
	
	public int compare(CaseBoardBean bean1, CaseBoardBean bean2) {
		return Collator.getInstance(locale).compare(getExpression(bean1), getExpression(bean2));
	}
	
	private String getExpression(CaseBoardBean bean) {
		if (ListUtil.isEmpty(sortingPreferences)) {
			//	Using default sorting: category + applicant name	//	TODO: is this OK?
			return bean.getCategory() + bean.getApplicantName();
		}
		
		return "";	//	TODO
	}

}

package is.idega.idegaweb.egov.cases.business;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import com.idega.block.process.business.ProcessConstants;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;

import is.idega.idegaweb.egov.cases.presentation.beans.CaseBoardBean;

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

	@Override
	public int compare(CaseBoardBean bean1, CaseBoardBean bean2) {
		String expr1 = getExpression(bean1);
		String expr2 = getExpression(bean2);
		return theCollator.compare(expr1, expr2);
	}

	private String getExpression(CaseBoardBean bean) {
		if (ListUtil.isEmpty(sortingPreferences)) {
			//	Using default sorting: category + applicant name
			String category = StringUtil.isEmpty(bean.getCategory()) ? "z" : bean.getCategory();
			return category + bean.getApplicantName();
		} else {
			for (String key: sortingPreferences) {
				if (key.equals(ProcessConstants.HANDLER_IDENTIFIER)) {
					return ((bean.getHandler() == null ? "z" : bean.getHandler().getName())).concat(String.valueOf(Integer.MAX_VALUE - Integer.valueOf(bean.getGradingSum())));
				} else {
					Logger.getLogger(getClass().getName()).warning("Do not know how to handle sorting preference " + key);
				}
			}
		}

		return CoreConstants.EMPTY;
	}

}

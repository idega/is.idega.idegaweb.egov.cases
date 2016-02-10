package is.idega.idegaweb.egov.cases.business;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import com.idega.block.process.business.ProcessConstants;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;

import is.idega.idegaweb.egov.cases.presentation.beans.CaseBoardBean;
import is.idega.idegaweb.egov.cases.util.CasesConstants;

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
		String category = StringUtil.isEmpty(bean.getCategory()) ? "z" : bean.getCategory();

		if (ListUtil.isEmpty(sortingPreferences)) {
			//	Using default sorting: category + applicant name
			return category + bean.getApplicantName();
		}

		StringBuilder expression = new StringBuilder();
		boolean categoryFirst = true;
		for (String key: sortingPreferences) {
			if (key.equals(CaseBoardBean.CASE_OWNER_FULL_NAME)) {
				expression.append(bean.getApplicantName());

			} else if (key.equals(CaseBoardBean.CASE_OWNER_GENDER)) {
				expression.append(bean.getValue(CaseBoardBean.CASE_OWNER_GENDER));

			} else if (key.equals("string_ownerKennitala")) {
				expression.append(bean.getPersonalID());

			} else if (key.equals("string_ownerAddress")) {
				expression.append(bean.getAddress());

			} else if (key.equals("string_ownerPostCode")) {
				expression.append(bean.getPostalCode());

			} else if (key.equals("string_ownerMunicipality")) {
				expression.append(bean.getMunicipality());

			} else if (key.equals(ProcessConstants.CASE_IDENTIFIER)) {
				expression.append(bean.getCaseIdentifier());

			} else if (key.equals(ProcessConstants.CASE_DESCRIPTION)) {
				expression.append(bean.getCaseDescription());

			} else if (key.equals(CaseBoardBean.CASE_OWNER_TOTAL_COST)) {
				expression.append(bean.getValue(CaseBoardBean.CASE_OWNER_TOTAL_COST));

			} else if (key.equals(CasesConstants.APPLIED_GRANT_AMOUNT_VARIABLE)) {
				expression.append(bean.getValue(CasesConstants.APPLIED_GRANT_AMOUNT_VARIABLE));

			} else if (key.equals("string_ownerBusinessConcept")) {
				expression.append(bean.getValue("string_ownerBusinessConcept"));

			} else if (key.equals(CaseBoardBean.CASE_SUM_OF_NEGATIVE_GRADES)) {
				expression.append(bean.getNegativeGradingSum());

			} else if (key.equals(CaseBoardBean.CASE_SUM_ALL_GRADES)) {
				categoryFirst = false;

			} else if (key.equals(CaseBoardBean.CASE_CATEGORY)) {
				categoryFirst = true;

			} else if (key.equals(CaseBoardBean.CASE_OWNER_GRADE)) {
				expression.append(bean.getValue(CaseBoardBean.CASE_OWNER_GRADE));

			} else if (key.equals(CaseBoardBean.CASE_OWNER_ANSWER)) {
				expression.append(bean.getValue(CaseBoardBean.CASE_OWNER_ANSWER));

			} else if (key.equals(ProcessConstants.HANDLER_IDENTIFIER)) {
				expression.append(bean.getHandler() == null ? "z" : bean.getHandler().getName());

			} else {
				Logger.getLogger(getClass().getName()).warning("Do not know how to handle sorting preference " + key);
			}
		}

		if (categoryFirst) {
			expression.append(category);
		}

		expression.append(String.valueOf(Integer.MAX_VALUE - Integer.valueOf(bean.getGradingSum())));

		if (!categoryFirst) {
			expression.append(category);
		}

		return expression.toString();
	}

}

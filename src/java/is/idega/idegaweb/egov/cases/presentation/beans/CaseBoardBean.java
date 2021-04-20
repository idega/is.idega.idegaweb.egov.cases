package is.idega.idegaweb.egov.cases.presentation.beans;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.idega.block.process.business.ProcessConstants;
import com.idega.user.data.User;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.idega.util.datastructures.map.MapUtil;

import is.idega.idegaweb.egov.cases.util.CasesConstants;

public class CaseBoardBean {

	public static final String
			CASE_CATEGORY = "string_ownerProjectLead",
			CASE_SUM_OF_NEGATIVE_GRADES = "sum_all_negative_grades",
			CASE_SUM_ALL_GRADES = "sum_all_grades",
			CASE_OWNER_FULL_NAME = "string_ownerFullName",
			CASE_OWNER_PERSONAL_ID = "string_ownerKennitala",
			CASE_OWNER_ADDRESS = "string_ownerAddress",
			CASE_OWNER_POSTAL_CODE = "string_ownerPostCode",
			CASE_OWNER_MUNICIPALITY = "string_ownerMunicipality",
			CASE_OWNER_TOTAL_COST = "string_ownerTotalCost",
			CASE_OWNER_BUSINESS_CONCEPT = "string_ownerBusinessConcept",
			CASE_OWNER_GRADE = "string_ownerGrade",
			CASE_OWNER_ANSWER = "string_ownerAnswer",
			CASE_OWNER_GENDER = "string_ownerGender";

	private String linkToCase;

	private String caseId, grade, negativeGrade;
	private Long processInstanceId;

	private List<Map<String, String>> financingOfTheTasks;

	private Map<String, String> values = new HashMap<>();

	private User handler;

	public CaseBoardBean(String caseId, Long processInstanceId) {
		this.caseId = caseId;
		this.processInstanceId = processInstanceId;
	}

	public String getApplicantName() {
		return getValue(CASE_OWNER_FULL_NAME);
	}

	public void setApplicantName(String applicantName) {
		addValue(CASE_OWNER_FULL_NAME, applicantName);
	}

	public String getPersonalID() {
		return getValue(CASE_OWNER_PERSONAL_ID);
	}

	public void setPersonalID(String personalID) {
		addValue(CASE_OWNER_PERSONAL_ID, personalID);
	}

	public String getPostalCode() {
		return getValue(CASE_OWNER_POSTAL_CODE);
	}

	public void setPostalCode(String postalCode) {
		addValue(CASE_OWNER_POSTAL_CODE, postalCode);
	}

	public String getMunicipality() {
		return getValue(CASE_OWNER_MUNICIPALITY);
	}

	public void setMunicipality(String municipality) {
		addValue(CASE_OWNER_MUNICIPALITY, municipality);
	}

	public String getCaseIdentifier() {
		return getValue(ProcessConstants.CASE_IDENTIFIER);
	}

	public void setCaseIdentifier(String caseIdentifier) {
		addValue(ProcessConstants.CASE_IDENTIFIER, caseIdentifier);
	}

	public String getCaseDescription() {
		return getValue(ProcessConstants.CASE_DESCRIPTION);
	}

	public void setCaseDescription(String caseDescription) {
		addValue(ProcessConstants.CASE_DESCRIPTION, caseDescription);
	}

	public String getNutshell() {
		return getValue(CASE_OWNER_BUSINESS_CONCEPT);
	}

	public void setNutshell(String nutshell) {
		addValue(CASE_OWNER_BUSINESS_CONCEPT, nutshell);
	}

	public String getCategory() {
		return getValue(CASE_CATEGORY);
	}

	public void setCategory(String category) {
		addValue(CASE_CATEGORY, category);
	}

	public String getRestrictions() {
		return getValue(CASE_OWNER_ANSWER);
	}

	public void setRestrictions(String restrictions) {
		addValue(CASE_OWNER_ANSWER, restrictions);
	}

	public String getTotalCost() {
		return getValue(CASE_OWNER_TOTAL_COST);
	}

	public void setTotalCost(String totalCost) {
		addValue(CASE_OWNER_TOTAL_COST, totalCost);
	}

	public String getAppliedAmount() {
		return getValue(CasesConstants.APPLIED_GRANT_AMOUNT_VARIABLE);
	}

	public void setAppliedAmount(String appliedAmount) {
		addValue(CasesConstants.APPLIED_GRANT_AMOUNT_VARIABLE, appliedAmount);
	}

	public String getComment() {
		return getValue(CASE_OWNER_GRADE);
	}

	public void setComment(String comment) {
		addValue(CASE_OWNER_GRADE, comment);
	}

	public BigDecimal getGrantAmountSuggestion() {
		return getNumberValue(ProcessConstants.BOARD_FINANCING_SUGGESTION);
	}

	public void setGrantAmountSuggestion(Long grantAmountSuggestion) {
		addValue(ProcessConstants.BOARD_FINANCING_SUGGESTION, grantAmountSuggestion);
	}

	public BigDecimal getBoardAmount() {
		return getNumberValue(ProcessConstants.BOARD_FINANCING_DECISION);
	}

	public void setBoardAmount(Long boardAmount) {
		addValue(ProcessConstants.BOARD_FINANCING_DECISION, boardAmount);
	}

	public String getCaseId() {
		return caseId;
	}

	public void setCaseId(String caseId) {
		this.caseId = caseId;
	}

	public String getGradingSum() {
		String value = values.get(CASE_SUM_ALL_GRADES);
		if (StringUtil.isEmpty(value)) {
			return getGrade();
		}
		return value;
	}

	public void setGradingSum(String gradingSum) {
		values.put(CASE_SUM_ALL_GRADES, gradingSum);
	}

	public void setNegativeGradingSum(String value){
		values.put(CASE_SUM_OF_NEGATIVE_GRADES, value);
	}

	public Long getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(Long processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public User getHandler() {
		return handler;
	}

	public void setHandler(User handler) {
		this.handler = handler;
	}

	public String getAddress() {
		return getValue(CASE_OWNER_ADDRESS);
	}

	public void setAddress(String address) {
		addValue(CASE_OWNER_ADDRESS, address);
	}

	public void addValue(String name, String value) {
		if (name != null && value != null) {
			values.put(name, value);
		} else {
			Logger.getLogger(getClass().getName()).warning("Name ('" + name + "') or value ('" + value + "') are not provided for " + getCaseIdentifier() + "!");
		}
	}

	public void addValue(String name, BigDecimal value, NumberFormat formatter) {
		if (name != null && value != null) {
			String numberValue = formatter.format(value.doubleValue());
			addValue(name, numberValue);

			if (CASE_SUM_ALL_GRADES.equals(name)) {
				setGrade(numberValue);
			} else if (CASE_SUM_OF_NEGATIVE_GRADES.equals(name)) {
				setNegativeGrade(numberValue);
			}
		} else {
			Logger.getLogger(getClass().getName()).warning("Name ('" + name + "') or value ('" + value + "') are not provided for " + getCaseIdentifier() + "!");
		}
	}

	private void addValue(String variableName, Long value) {
		if (value != null) {
			addValue(variableName, value.toString());
		}
	}

	public void addValues(Map<String, BigDecimal> values, Locale locale) {
		if (!MapUtil.isEmpty(values)) {
			NumberFormat formatter = DecimalFormat.getNumberInstance(locale);
			for (Map.Entry<String, BigDecimal> entry: values.entrySet()) {
				addValue(entry.getKey(), entry.getValue(), formatter);
			}
		} else {
			Logger.getLogger(getClass().getName()).warning("Values are not provided for " + getCaseIdentifier() + "!");
		}
	}

	public String getValue(String name) {
		if (name == null) {
			return null;
		}

		return values.get(name);
	}

	public BigDecimal getNumberValue(String name) {
		String value = getValue(name);
		if (!StringUtil.isEmpty(value)) {
			return BigDecimal.valueOf(Long.valueOf(value));
		}

		return null;
	}

	@Override
	public String toString() {
		String string = "\n".concat(this.getClass().getSimpleName()).concat(": case ID: ").concat(caseId).concat(", process instance ID: ")
				.concat(String.valueOf(processInstanceId)) + ", identifier: " + getCaseIdentifier() + "\n";

		if (!MapUtil.isEmpty(values)) {
			string = string + "----------------------------------------------------------------------\n";
			for (Entry<String, String> variable : values.entrySet()) {
				string = string + variable.getKey() + ": " + variable.getValue() + "\n";
			}
		}

		if (!ListUtil.isEmpty(getFinancingOfTheTasks())) {
			string = string + "--------------------------FINANCING VALUES---------------------------\n";
			for (Map<String, String> task : getFinancingOfTheTasks()) {
				for (Entry<String, String> variable : task.entrySet()) {
					string = string + variable.getKey() + ": " + variable.getValue() + "\n";
				}
			}
		}

		string = string + "--------------------------Grading sums---------------------------\n";
		string += "Total grade: " + getGradingSum() + "\n";
		string += "Negtative grade: " + getNegativeGradingSum() + "\n";

		string = string + "---------------------------------------------------------------------------------";
		return string;
	}

	public String getNegativeGradingSum(){
		String value = getValue(CASE_SUM_OF_NEGATIVE_GRADES);
		if (StringUtil.isEmpty(value)) {
			return getNegativeGrade();
		}
		return value;
	}

	public List<Map<String, String>> getFinancingOfTheTasks() {
		return financingOfTheTasks;
	}

	public void setFinancingOfTheTasks(List<Map<String, String>> financingOfTheTasks) {
		this.financingOfTheTasks = financingOfTheTasks;
	}

	public String getLinkToCase() {
		return linkToCase;
	}

	public void setLinkToCase(String linkToCase) {
		this.linkToCase = linkToCase;
	}

	private String getGrade() {
		return grade;
	}

	private void setGrade(String grade) {
		this.grade = grade;
	}

	private String getNegativeGrade() {
		return negativeGrade;
	}

	private void setNegativeGrade(String negativeGrade) {
		this.negativeGrade = negativeGrade;
	}

}
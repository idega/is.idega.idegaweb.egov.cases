package is.idega.idegaweb.egov.cases.presentation.beans;

import is.idega.idegaweb.egov.cases.presentation.CasesBoardViewer;
import is.idega.idegaweb.egov.cases.util.CasesConstants;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.idega.block.process.business.ProcessConstants;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.StringUtil;
import com.idega.util.datastructures.map.MapUtil;

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

	private String caseId;
	private Long processInstanceId;

	private List<Map<String, String>> financingOfTheTasks;

	private List<String> allValues;

	private Map<String, String> values = null;

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

	public void setAllValues(List<String> allValues) {
		this.allValues = allValues;
	}

	public String getCaseId() {
		return caseId;
	}

	public void setCaseId(String caseId) {
		this.caseId = caseId;
	}

	public List<String> getAllValues() {
		if (allValues == null) {
			allValues = new ArrayList<String>(CasesBoardViewer.CASE_FIELDS.size());
			allValues.addAll(getValues().values());
			allValues.add(CoreConstants.EMPTY);							//	13	should be a table here...
		}
		return allValues;
	}

	public String getGradingSum() {
		return getValues().get(CASE_SUM_ALL_GRADES);
	}

	public void setGradingSum(String gradingSum) {
		getValues().put(CASE_SUM_ALL_GRADES, gradingSum);
	}

	public void setNegativeGradingSum(String value){
		getValues().put(CASE_SUM_OF_NEGATIVE_GRADES, value);
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
			getValues().put(name, value);
		}
	}

	public void addValue(String name, BigDecimal value) {
		if (name != null && value != null) {
			getValues().put(name, value.toPlainString());
		}
	}

	private void addValue(
			String variableName,
			Long value) {
		if (value != null) {
			addValue(variableName, value.toString());
		}
	}

	public void addValues(Map<String, BigDecimal> values) {
		if (!MapUtil.isEmpty(values)) {
			for (Map.Entry<String,BigDecimal> entry : values.entrySet()) {
				addValue(entry.getKey(), entry.getValue());
			}
		}
	}

	public Map<String, String> getValues() {
		if (this.values == null) {
			this.values = new HashMap<String, String>();
		}

		return values;
	}
	
	public String getValue(String name) {
		if (name == null)
			return null;

		return getValues().get(name);
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
				.concat(String.valueOf(processInstanceId)) + ",\n";
		string = string + "----------------------------------VALUES------------------------------------\n";
		for (Entry<String, String> variable : getValues().entrySet()) {
			string = string + variable.getKey() + ": " + variable.getValue() + "\n";
		}

		string = string + "--------------------------FINANCING VALUES---------------------------\n";
		
		for (Map<String, String> task : getFinancingOfTheTasks()) {
			for (Entry<String, String> variable : task.entrySet()) {
				string = string + variable.getKey() + ": " + variable.getValue() + "\n";
			}
		}
		
		string = string + "---------------------------------------------------------------------------------";
		return string;
	}

	public String getNegativeGradingSum(){
		return getValue(CASE_SUM_OF_NEGATIVE_GRADES);
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
}
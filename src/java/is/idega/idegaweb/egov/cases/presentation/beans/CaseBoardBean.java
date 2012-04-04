package is.idega.idegaweb.egov.cases.presentation.beans;

import is.idega.idegaweb.egov.cases.presentation.CasesBoardViewer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.idega.user.data.User;
import com.idega.util.CoreConstants;

public class CaseBoardBean {

	private String caseId;
	private Long processInstanceId;

	private String applicantName;
	private String personalID;
	private String address;
	private String postalCode;
	private String municipality;
	private String caseIdentifier;
	private String caseDescription;

	private String totalCost;
	private String appliedAmount;

	private String nutshell;
	private String gradingSum;
	private String negativeGradingSum = null;

	private String category;
	private String comment;

	private Long grantAmountSuggestion;
	private Long boardAmount;
	private String restrictions;

	private List<Map<String, String>> financingOfTheTasks;

	private List<String> allValues;

	private Map<String, String> values = new HashMap<String, String>();

	private User handler;

	public CaseBoardBean(String caseId, Long processInstanceId) {
		this.caseId = caseId;
		this.processInstanceId = processInstanceId;
	}

	public String getApplicantName() {
		return applicantName;
	}

	public void setApplicantName(String applicantName) {
		this.applicantName = applicantName;
	}

	public String getPersonalID() {
		return personalID;
	}

	public void setPersonalID(String personalID) {
		this.personalID = personalID;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getMunicipality() {
		return municipality;
	}

	public void setMunicipality(String municipality) {
		this.municipality = municipality;
	}

	public String getCaseIdentifier() {
		return caseIdentifier;
	}

	public void setCaseIdentifier(String caseIdentifier) {
		this.caseIdentifier = caseIdentifier;
	}

	public String getCaseDescription() {
		return caseDescription;
	}

	public void setCaseDescription(String caseDescription) {
		this.caseDescription = caseDescription;
	}

	public String getNutshell() {
		return nutshell;
	}

	public void setNutshell(String nutshell) {
		this.nutshell = nutshell;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getRestrictions() {
		return restrictions;
	}

	public void setRestrictions(String restrictions) {
		this.restrictions = restrictions;
	}

	public String getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(String totalCost) {
		this.totalCost = totalCost;
	}

	public String getAppliedAmount() {
		return appliedAmount;
	}

	public void setAppliedAmount(String appliedAmount) {
		this.appliedAmount = appliedAmount;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Long getGrantAmountSuggestion() {
		return grantAmountSuggestion;
	}

	public void setGrantAmountSuggestion(Long grantAmountSuggestion) {
		this.grantAmountSuggestion = grantAmountSuggestion;
	}

	public Long getBoardAmount() {
		return boardAmount;
	}

	public void setBoardAmount(Long boardAmount) {
		this.boardAmount = boardAmount;
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

			allValues.add(getApplicantName());							//	0
			allValues.add(getPersonalID());								//	1
			allValues.add(getAddress());								//	2
			allValues.add(getPostalCode());								//	3
			allValues.add(getMunicipality());							//	4
			allValues.add(getCaseIdentifier());							//	5
			allValues.add(getCaseDescription());						//	6

			allValues.add(String.valueOf(getTotalCost()));				//	7
			allValues.add(String.valueOf(getAppliedAmount()));			//	8

			allValues.add(getNutshell());								//	9

			allValues.add(getNegativeGradingSum());						//	10
			allValues.add(getGradingSum());								//	11

			allValues.add(getCategory());								//	12

			allValues.add(CoreConstants.EMPTY);							//	13	should be a table here...

			allValues.add(getComment());								//	14
			allValues.add(getRestrictions());							//	15
		}
		return allValues;
	}

	public String getGradingSum() {
		return gradingSum;
	}

	public void setGradingSum(String gradingSum) {
		this.gradingSum = gradingSum;
	}

	public void setNegativeGradingSum(String negativeGradingSum){
		this.negativeGradingSum = negativeGradingSum;
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
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void addValue(String name, String value) {
		if (name == null || value == null)
			return;

		values.put(name, value);
	}

	public String getValue(String name) {
		if (name == null)
			return null;

		return values.get(name);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName().concat(": case ID: ").concat(caseId).concat(", process instance ID: ")
				.concat(String.valueOf(processInstanceId)) + ", values: " + getAllValues();
	}

	public String getNegativeGradingSum(){
		return this.negativeGradingSum;
	}

	public List<Map<String, String>> getFinancingOfTheTasks() {
		return financingOfTheTasks;
	}

	public void setFinancingOfTheTasks(List<Map<String, String>> financingOfTheTasks) {
		this.financingOfTheTasks = financingOfTheTasks;
	}

}
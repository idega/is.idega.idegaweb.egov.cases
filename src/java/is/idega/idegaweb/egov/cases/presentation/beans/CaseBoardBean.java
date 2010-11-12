package is.idega.idegaweb.egov.cases.presentation.beans;

import is.idega.idegaweb.egov.cases.presentation.CasesBoardViewer;

import java.util.ArrayList;
import java.util.List;

import com.idega.user.data.User;

public class CaseBoardBean {

	private String caseId;
	private Long processInstanceId;
	
	private String applicantName;
	private String postalCode;
	private String caseIdentifier;
	private String caseDescription;
	
	private String totalCost;
	private String appliedAmount;
	
	private String nutshell;
	private String gradingSum;
	
	private String category;
	private String comment;
	
	private Long grantAmountSuggestion;
	private Long boardAmount;
	private String restrictions;
	
	private List<String> allValues;

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

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
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
			
			allValues.add(getApplicantName());					//	0
			allValues.add(getPostalCode());						//	1
			allValues.add(getCaseIdentifier());					//	2
			allValues.add(getCaseDescription());				//	3
			
			allValues.add(String.valueOf(getTotalCost()));		//	4
			allValues.add(String.valueOf(getAppliedAmount()));	//	5
			
			allValues.add(getNutshell());						//	6
			
			allValues.add(getGradingSum());						//	7
			
			allValues.add(getCategory());						//	8
			allValues.add(getComment());						//	9
			
			allValues.add(String.valueOf(getGrantAmountSuggestion()));	//	10
			allValues.add(String.valueOf(getBoardAmount()));	//	11
			allValues.add(getRestrictions());					//	12
		}
		return allValues;
	}

	public String getGradingSum() {
		return gradingSum;
	}

	public void setGradingSum(String gradingSum) {
		this.gradingSum = gradingSum;
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

	@Override
	public String toString() {
		return this.getClass().getSimpleName().concat(": case ID: ").concat(caseId).concat(", process instance ID: ").concat(String.valueOf(processInstanceId)) +
			", values: " + getAllValues();
	}
}
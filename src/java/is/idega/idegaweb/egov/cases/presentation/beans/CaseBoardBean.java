package is.idega.idegaweb.egov.cases.presentation.beans;

import java.util.ArrayList;
import java.util.List;

public class CaseBoardBean {

	private String caseId;
	
	private String applicantName;
	private String postalCode;
	private String caseIdentifier;
	private String caseDescription;
	
	private String totalCost;
	private String appliedAmount;
	
	private String nutshell;
	
	private String category;
	
	private String comment;
	private Double grantAmountSuggestion;
	private Double boardAmount;
	private String restrictions;
	
	private List<String> allValues;

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

	public String getOwnerGrade() {
		return comment;
	}

	public void setOwnerGrade(String ownerGrade) {
		this.comment = ownerGrade;
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

	public Double getGrantAmountSuggestion() {
		return grantAmountSuggestion;
	}

	public void setGrantAmountSuggestion(Double grantAmountSuggestion) {
		this.grantAmountSuggestion = grantAmountSuggestion;
	}

	public Double getBoardAmount() {
		return boardAmount;
	}

	public void setBoardAmount(Double boardAmount) {
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
			allValues = new ArrayList<String>();
			
			allValues.add(getApplicantName());
			allValues.add(getPostalCode());
			allValues.add(getCaseIdentifier());
			allValues.add(getCaseDescription());
			
			allValues.add(String.valueOf(getTotalCost()));
			allValues.add(String.valueOf(getAppliedAmount()));
			
			allValues.add(getNutshell());
			
			allValues.add(getCategory());
			
			allValues.add(getOwnerGrade());
			allValues.add(String.valueOf(getGrantAmountSuggestion()));
			allValues.add(String.valueOf(getBoardAmount()));
			allValues.add(getRestrictions());
		}
		return allValues;
	}
}

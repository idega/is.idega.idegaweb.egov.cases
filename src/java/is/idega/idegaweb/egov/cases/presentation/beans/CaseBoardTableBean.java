package is.idega.idegaweb.egov.cases.presentation.beans;

import java.util.List;
import java.util.Map;

import com.idega.builder.bean.AdvancedProperty;

public class CaseBoardTableBean {
	
	private Map<Integer, AdvancedProperty> headerLabels;
	private List<CaseBoardTableBodyRowBean> bodyBeans;
	private List<String> footerValues;
	
	private String errorMessage;
	
	private boolean filledWithData;
	
	public Map<Integer, AdvancedProperty> getHeaderLabels() {
		return headerLabels;
	}
	public void setHeaderLabels(Map<Integer, AdvancedProperty> headerLabels) {
		this.headerLabels = headerLabels;
	}
	public List<String> getFooterValues() {
		return footerValues;
	}
	public void setFooterValues(List<String> footerValues) {
		this.footerValues = footerValues;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public boolean isFilledWithData() {
		return filledWithData;
	}
	public void setFilledWithData(boolean filledWithData) {
		this.filledWithData = filledWithData;
	}
	public List<CaseBoardTableBodyRowBean> getBodyBeans() {
		return bodyBeans;
	}
	public void setBodyBeans(List<CaseBoardTableBodyRowBean> bodyBeans) {
		this.bodyBeans = bodyBeans;
	}
	
}

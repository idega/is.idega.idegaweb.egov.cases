package is.idega.idegaweb.egov.cases.presentation.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.idega.builder.bean.AdvancedProperty;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.idega.util.datastructures.map.MapUtil;

public class CaseBoardTableBean {

	private TreeMap<Integer, List<AdvancedProperty>> headerLabels;
	private List<CaseBoardTableBodyRowBean> bodyBeans;
	private List<String> footerValues;

	private String errorMessage;

	private boolean filledWithData;

	public Map<Integer, List<AdvancedProperty>> getHeaderLabels() {
		if (this.headerLabels == null) {
			this.headerLabels = new TreeMap<Integer, List<AdvancedProperty>>();
		}

		return headerLabels;
	}
	public void setHeaderLabels(Map<Integer, List<AdvancedProperty>> headerLabels) {
		if (!MapUtil.isEmpty(headerLabels)) {
			getHeaderLabels().clear();
			getHeaderLabels().putAll(headerLabels);
		}
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
		if (this.bodyBeans == null) {
			this.bodyBeans = new ArrayList<CaseBoardTableBodyRowBean>();
		}
		
		return bodyBeans;
	}
	public void setBodyBeans(List<CaseBoardTableBodyRowBean> bodyBeans) {
		this.bodyBeans = bodyBeans;
	}

	/**
	 * @param variableName is name of jBPM variable, not <code>null</code>;
	 * @return number of column in table or <code>null</code> on failure;
	 */
	public Integer getIndexOfColumn(String variableName) {
		if (!ListUtil.isEmpty(getBodyBeans()) && !StringUtil.isEmpty(variableName)) {
			for (CaseBoardTableBodyRowBean bodyBean : getBodyBeans()) {
				Integer index = bodyBean.getColumnIndex(variableName);
				if (index != null) {
					return index;
				}
			}
		}

		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());
		sb.append(CoreConstants.NEWLINE);
		sb.append("isFilledWithData: ").append(isFilledWithData());
		sb.append(CoreConstants.NEWLINE);
		
		/*
		 * Labels
		 */
		for (Integer columnIndex : getHeaderLabels().keySet()) {
			List<AdvancedProperty> variables = getHeaderLabels().get(columnIndex);
			if (!ListUtil.isEmpty(variables)) {
				for (AdvancedProperty variable : variables) {
					sb.append(variable.getId()).append(CoreConstants.COLON)
					.append(variable.getValue()).append(CoreConstants.NEWLINE);
				}
			}
		}

		/*
		 * Rows
		 */
		for (CaseBoardTableBodyRowBean rowBean : getBodyBeans()) {
			sb.append(rowBean.toString());
			sb.append(CoreConstants.NEWLINE);
		}

		/*
		 * Footer
		 */
//		for (Integer columnIndex : getFooterValues().keySet()) {
//			List<AdvancedProperty> variables = getHeaderLabels().get(columnIndex);
//			if (!ListUtil.isEmpty(variables)) {
//				for (AdvancedProperty variable : variables) {
//					sb.append(variable.getId()).append(CoreConstants.COLON)
//					.append(variable.getValue()).append(CoreConstants.NEWLINE);
//				}
//			}
//		}

		return sb.toString();
	}
}

package is.idega.idegaweb.egov.cases.presentation.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.idega.block.process.business.ProcessConstants;
import com.idega.builder.bean.AdvancedProperty;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.idega.util.datastructures.map.MapUtil;

public class CaseBoardTableBodyRowBean {

	private String id;
	private String caseId;
	private String caseIdentifier;
	private String linkToCase;
	private Long processInstanceId;

	private User handler;

	private Map<Integer, List<AdvancedProperty>> values;

	private List<Map<String, String>> financingInfo;

	public CaseBoardTableBodyRowBean() {
		super();
	}

	public CaseBoardTableBodyRowBean(String caseId, Long processInstanceId) {
		this();

		this.caseId = caseId;
		this.processInstanceId = processInstanceId;
	}

	public Map<Integer, List<AdvancedProperty>> getValues() {
		return values;
	}

	public void setValues(Map<Integer, List<AdvancedProperty>> values) {
		this.values = values;
	}

	public String getCaseId() {
		return caseId;
	}
	public void setCaseId(String caseId) {
		this.caseId = caseId;
	}
	public String getCaseIdentifier() {
		return caseIdentifier;
	}
	public void setCaseIdentifier(String caseIdentifier) {
		this.caseIdentifier = caseIdentifier;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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

	public List<Map<String, String>> getFinancingInfo() {
		return financingInfo;
	}

	public void setFinancingInfo(List<Map<String, String>> financingInfo) {
		this.financingInfo = financingInfo;
	}

	public void setFinancingInfo(String variable, String value) {
		if (this.financingInfo == null) {
			this.financingInfo = new ArrayList<>();
		}

		Map<String, String> map = null;
		if (!ListUtil.isEmpty(financingInfo)) {
			map = this.financingInfo.iterator().next();
		}

		if (map == null) {
			map = new HashMap<>();
			this.financingInfo.add(map);
		}

		map.put(variable, value);
	}

	public String getLinkToCase() {
		return linkToCase;
	}

	public void setLinkToCase(String linkToCase) {
		this.linkToCase = linkToCase;
	}

	public Integer getColumnIndex(String variableName) {
		if (!MapUtil.isEmpty(getValues()) && !StringUtil.isEmpty(variableName)) {
			for (Integer index : getValues().keySet()) {
				List<AdvancedProperty> variablesInfo = getValues().get(index);
				if (!ListUtil.isEmpty(variablesInfo)) {
					for (AdvancedProperty variableInfo : variablesInfo) {
						if (variableName.equals(variableInfo.getId())) {
							return index;
						}

						if (ProcessConstants.FINANCING_OF_THE_TASKS_VARIABLES.contains(variableInfo.getId())) {
							for (Map<String, String> financingInfo : getFinancingInfo()) {
								int i =1;
								for (Entry<String, String> entry : financingInfo.entrySet()) {
									if (entry.getKey().equals(variableName)) {
										return index + i;
									}

									i++;
								}
							}
						}
					}
				}
			}
		}

		return null;
	}

	@Override
	public String toString() {
		StringBuilder sb  = new StringBuilder(super.toString());

		sb.append("getId(): ").append(getId()).append(CoreConstants.NEWLINE);
		sb.append("getProcessInstanceId(): ").append(getProcessInstanceId()).append(CoreConstants.NEWLINE);
		sb.append("getCaseIdentifier(): ").append(getCaseIdentifier()).append(CoreConstants.NEWLINE);
		sb.append("getCaseId(): ").append(getCaseId()).append(CoreConstants.NEWLINE);
		sb.append("getLinkToCase(): ").append(getLinkToCase()).append(CoreConstants.NEWLINE);
		Map<Integer, List<AdvancedProperty>> values = getValues();
		if (!MapUtil.isEmpty(values)) {
			for (Integer columnNumber : values.keySet()) {
				List<AdvancedProperty> variables = values.get(columnNumber);
				if (!ListUtil.isEmpty(variables)) {
					for (AdvancedProperty variable: variables) {
						sb.append(variable.getId()).append(CoreConstants.COLON).append(variable.getValue()).append(CoreConstants.SPACE);
						sb.append(CoreConstants.NEWLINE);
					}
				}
			}
		}

		List<Map<String, String>> finInfo = getFinancingInfo();
		sb.append("Financial: ").append(ListUtil.isEmpty(finInfo) ? "not available" : CoreConstants.NEWLINE);
		if (!ListUtil.isEmpty(finInfo)) {
			for (Map<String, String> wft : finInfo) {
				if (MapUtil.isEmpty(wft)) {
					continue;
				}

				for (String string : wft.keySet()) {
					sb.append(string).append(CoreConstants.COLON).append(wft.get(string)).append(CoreConstants.NEWLINE);
				}
			}
		}

		return sb.toString();
	}
}
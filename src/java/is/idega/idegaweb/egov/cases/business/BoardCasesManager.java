package is.idega.idegaweb.egov.cases.business;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.idega.block.process.data.Case;
import com.idega.builder.bean.AdvancedProperty;
import com.idega.presentation.IWContext;
import com.idega.user.data.User;

import is.idega.idegaweb.egov.cases.presentation.CasesBoardViewer;
import is.idega.idegaweb.egov.cases.presentation.beans.CaseBoardBean;
import is.idega.idegaweb.egov.cases.presentation.beans.CaseBoardTableBean;

public interface BoardCasesManager {

	public static final String BEAN_NAME = "boardCasesManager";

	/**
	 *
	 * @author <a href="mailto:martynas@idega.is">Martynas Stakė</a>
	 * @param dateFrom is floor of {@link Case#getCreated()},
	 * skipped if <code>null</code>;
	 * @param dateTo is ceiling of {@link Case#getCreated()},
	 * skipped if <code>null</code>;
	 */
	public <K extends Serializable> List<CaseBoardBean> getAllSortedCases(
			User currentUser,
			Collection<String> caseStatuses,
			String processName,
			String uuid,
			boolean isSubscribedOnly,
			boolean backPage,
			String taskName,
			Date dateFrom,
			Date dateTo,
			String casesType,
			Class<K> type
	);

	/**
	 *
	 * <p>Construcs data for table to be shown.</p>
	 * @param iwc - application context;
	 * @param caseStatus of cases to add to table, for example:
	 * "BVJD, INPR, FINI, ...";
	 * @param processName is name of ProcessDefinition object;
	 * @param customColumns - variable names, which should be shown as columns,
	 * for example: "string_caseIdentifier,string_caseDescription,..."
	 * @param uuid of {@link CasesBoardViewer} component;
	 * @return data for table to represent or <code>null</code> on failure.
	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
	 */
	public CaseBoardTableBean getTableData(
			User currentUser,
			Collection<String> caseStatus,
			String processName,
			String uuid,
			boolean isSubscribedOnly,
			boolean useBasePage,
			String taskName,
			String casesType
	);

	/**
	 * @param dateFrom is floor of {@link Case#getCreated()},
	 * skipped if <code>null</code>;
	 * @param dateTo is ceiling of {@link Case#getCreated()},
	 * skipped if <code>null</code>;
	 */
	public <K extends Serializable> CaseBoardTableBean getTableData(
			User currentUser,
			Date dateFrom,
			Date dateTo,
			Collection<String> caseStatus,
			String processName,
			String uuid,
			boolean isSubscribedOnly,
			boolean useBasePage,
			String taskName,
			String casesType,
			Class<K> type
	);

	public AdvancedProperty getHandlerInfo(IWContext iwc, User handler);

	public String getLinkToTheTaskRedirector(IWContext iwc, String basePage, String caseId, Long processInstanceId, String backPage, String taskName);

	public List<AdvancedProperty> getAvailableVariables(String processName, String casesType);

	public Map<Integer, List<AdvancedProperty>> getColumns(String uuid, String casesType);

	public List<String> getCustomColumns(String uuid);

	public boolean isEqual(String currentColumn, String columnOfDomain);

	public int getIndexOfColumn(String column, String uuid, String casesType);

	public Long getNumberValue(String value);

	public boolean hasCustomColumns(String uuid);

	public boolean isBoardSuggestionEnabled();

	public boolean isBoardProposalEnabled();

	public boolean isBoardDecisionEnabled();

	public boolean isAutomaticBoardDecisionAndSuggestion();

}
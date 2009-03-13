package is.idega.idegaweb.egov.cases.business;

import is.idega.idegaweb.egov.cases.data.GeneralCase;
import is.idega.idegaweb.egov.cases.presentation.CaseViewer;
import is.idega.idegaweb.egov.cases.presentation.CasesBoardViewer;
import is.idega.idegaweb.egov.cases.presentation.CasesProcessor;
import is.idega.idegaweb.egov.cases.presentation.beans.CaseBoardBean;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.process.business.CaseManager;
import com.idega.block.process.data.Case;
import com.idega.block.process.presentation.UserCases;
import com.idega.builder.bean.AdvancedProperty;
import com.idega.builder.business.BuilderLogicWrapper;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.idega.util.URIUtil;
import com.idega.util.expression.ELUtil;

@Scope("singleton")
@Service(BoardCasesManager.SPRING_BEAN_IDENTIFIER)
public class BoardCasesManagerImpl implements BoardCasesManager {
	
	private static final List<String> GRADING_VARIABLES = Collections.unmodifiableList(Arrays.asList(
			"string_ownerInnovationalValue",
			"string_ownerCompetitionValue",
			"string_ownerEntrepreneursValue",
			"string_ownerPossibleDevelopments",
			"string_ownerNatureStatus",
			"string_ownerApplication",
			"string_ownerOverturn",
			"string_ownerProceeds",
			"string_ownerEconomist",
			"string_ownerEmployees",
			"string_ownerForsvarsmenn",
			"string_ownerConstant",
			"string_ownerNewConstant",
			"string_ownerBusiness",
			"string_ownerProject",
			"string_ownerCostValue",
			"string_ownerProjectedSize",
			"string_ownerEntrepreneurCompany"
	));

	private static final Logger LOGGER = Logger.getLogger(BoardCasesManagerImpl.class.getName());
	
	public static final String BOARD_CASES_LIST_SORTING_PREFERENCES = "boardCasesListSortingPreferencesAttribute";
	
	private CaseManager caseManager;
	
	@Autowired
	private BuilderLogicWrapper builderLogicWrapper;
	
	private List<String> variables;
	
	public List<CaseBoardBean> getAllSortedCases(IWContext iwc, IWResourceBundle iwrb, String caseStatus) {
		Collection<GeneralCase> cases = getCases(iwc, caseStatus);
		if (ListUtil.isEmpty(cases)) {
			return null;
		}
		
		List<CaseBoardBean> boardBeans = new ArrayList<CaseBoardBean>();
		for (GeneralCase theCase: cases) {
			if (isCaseAvailableForBoard(theCase)) {
				CaseBoardBean boardCase = getFilledBoardCaseWithInfo(theCase);
				boardBeans.add(boardCase);
			}
		}
		
		sortBoardCases(iwc, boardBeans);
		
		return boardBeans;
	}
	
	private CaseBoardBean getFilledBoardCaseWithInfo(GeneralCase theCase) {
		CaseManager caseManager = getCaseManager();
		if (caseManager == null) {
			return null;
		}
		
		List<String> values = caseManager.getCaseStringVariablesValuesByVariables(theCase, getVariables());
		if (ListUtil.isEmpty(values)) {
			return null;
		}
		
		CaseBoardBean boardCase = new CaseBoardBean();
		boardCase.setCaseId(theCase.getPrimaryKey().toString());
		
		boardCase.setApplicantName(getStringValue(values.get(0)));
		boardCase.setPostalCode(getStringValue(values.get(1)));
		boardCase.setCaseIdentifier(getStringValue(values.get(2)));
		boardCase.setCaseDescription(getStringValue(values.get(3)));
		
		boardCase.setTotalCost(String.valueOf(getNumberValue(values.get(4))));
		boardCase.setAppliedAmount(String.valueOf(getNumberValue(values.get(5))));
		
		boardCase.setNutshell(getStringValue(values.get(6)));
		//	Grading sums should be 7
		boardCase.setCategory(getStringValue(values.get(8)));
		
		boardCase.setComment(getStringValue(values.get(9)));
		boardCase.setGrantAmountSuggestion(getNumberValue(values.get(10)));
		boardCase.setBoardAmount(getNumberValue(values.get(11)));
		boardCase.setRestrictions(getStringValue(values.get(12)));
	
		return boardCase;
	}
	
	private String getStringValue(String value) {
		if (StringUtil.isEmpty(value) || "no_value".equals(value)) {
			return CoreConstants.EMPTY;
		}
		
		return value;
	}
	
	private Double getNumberValue(String value) {
		if (StringUtil.isEmpty(getStringValue(value))) {
			return Double.valueOf(0);
		}
		
		try {
			return Double.valueOf(value);
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error converting to Double: " + value);
		}
		
		return Double.valueOf(0);
	}
	
	private boolean isCaseAvailableForBoard(GeneralCase theCase) {
		String managerType = theCase.getCaseManagerType();
		if (StringUtil.isEmpty(managerType) || !managerType.equals("CasesBPM")) {
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private void sortBoardCases(IWContext iwc, List<CaseBoardBean> boardCases) {
		if (ListUtil.isEmpty(boardCases)) {
			return;
		}
		
		List<String> sortingPreferences = null;
		Object o = iwc.getSessionAttribute(BOARD_CASES_LIST_SORTING_PREFERENCES);
		if (o instanceof List) {
			sortingPreferences = (List<String>) o;
		}
		
		Collections.sort(boardCases, new BoardCasesComparator(iwc.getLocale(), sortingPreferences));
	}
	
	@SuppressWarnings("unchecked")
	private Collection<GeneralCase> getCases(IWApplicationContext iwac, String caseStatus) {
		if (StringUtil.isEmpty(caseStatus)) {
			LOGGER.warning("Case status is unkown - terminating!");
			return null;
		}
		CasesBusiness casesBusiness = getCasesBusiness(iwac);
		if (casesBusiness == null) {
			return null;
		}
		
		Collection<Case> allCases = null;
		try {
			allCases = casesBusiness.getCasesByCriteria(null, null, null, casesBusiness.getCaseStatus(caseStatus), false);
		} catch (RemoteException e) {
			LOGGER.log(Level.SEVERE, "Error getting cases by cases status: " + caseStatus, e);
		}
		if (ListUtil.isEmpty(allCases)) {
			return null;
		}
		
		Collection<GeneralCase> bpmCases = new ArrayList<GeneralCase>();
		for (Case theCase: allCases) {
			if (theCase instanceof GeneralCase) {
				bpmCases.add((GeneralCase) theCase);
			}
		}
		
		return bpmCases;
	}
	
	private CasesBusiness getCasesBusiness(IWApplicationContext iwac) {
		try {
			return (CasesBusiness) IBOLookup.getServiceInstance(iwac, CasesBusiness.class);
		} catch (IBOLookupException e) {
			LOGGER.log(Level.SEVERE, "Error getting " + CasesBusiness.class, e);
		}
		
		return null;
	}
	
	private CaseManager getCaseManager() {
		if (caseManager == null) {
			try {
				caseManager = ELUtil.getInstance().getBean("casesBPMCaseHandler");
			} catch(Exception e) {
				LOGGER.log(Level.SEVERE, "Error getting Spring bean for: " + CaseManager.class, e);
			}
		}
		return caseManager;
	}

	private List<String> getVariables() {
		if (variables == null) {
			variables = new ArrayList<String>(CasesBoardViewer.CASE_FIELDS.size());
			for (AdvancedProperty variable: CasesBoardViewer.CASE_FIELDS) {
				variables.add(variable.getId());
			}
		}
		return variables;
	}
	
	public boolean setCaseVariableValue(Integer caseId, String variableName, String value) {
		if (caseId == null || StringUtil.isEmpty(variableName) || StringUtil.isEmpty(value)) {
			return false;
		}
		
		return true;	//	TODO
	}

	public String getVariableValueInput(Integer caseId, String variableName, String currentValue) {
		if (caseId == null || StringUtil.isEmpty(variableName)) {
			return null;
		}
		
		return null;	//	TODO
	}

	public String getLinkToTheTask(IWContext iwc, CaseBoardBean boardCase) {
		if (iwc == null || boardCase == null) {
			return null;
		}
		
		String uri = builderLogicWrapper.getBuilderService(iwc).getFullPageUrlByPageType(iwc, "bpm_assets_view", true);
		if (StringUtil.isEmpty(uri)) {
			return iwc.getRequestURI();
		}
		
		String taskId = getInstanceIdForGradingTask(iwc, boardCase);
		if (StringUtil.isEmpty(taskId)) {
			return iwc.getRequestURI();
		}
		
		URIUtil uriUtil = new URIUtil(uri);
		
		uriUtil.setParameter(CasesProcessor.PARAMETER_ACTION, String.valueOf(UserCases.ACTION_CASE_MANAGER_VIEW));
		uriUtil.setParameter(CaseViewer.PARAMETER_CASE_PK, boardCase.getCaseId());
		uriUtil.setParameter("tiId", taskId);
		
		uri = uriUtil.getUri();
		return iwc.getIWMainApplication().getTranslatedURIWithContext(uri);
	}
	
	private String getInstanceIdForGradingTask(IWContext iwc, CaseBoardBean boardCase) {
		Long taskId = null;
		try {
			taskId = getCaseManager().getTaskInstanceIdForTask(getCasesBusiness(iwc).getCase(boardCase.getCaseId()), "Grading");
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error getting task instance for case: " + boardCase.getCaseId());
		}
		if (taskId == null) {
			return null;
		}
		return String.valueOf(taskId.longValue());
	}

	public String getGradingSum(IWContext iwc, CaseBoardBean boardCase) {
		List<String> gradingValues = null;
		try {
			gradingValues = getCaseManager().getCaseStringVariablesValuesByVariables(getCasesBusiness(iwc).getCase(boardCase.getCaseId()), GRADING_VARIABLES);
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error getting grading values for case: " + boardCase.getCaseId());
		}
		if (ListUtil.isEmpty(gradingValues)) {
			return "0";
		}
		
		long sum = 0;
		Long gradeValue = null;
		for (String value: gradingValues) {
			if (StringUtil.isEmpty(getStringValue(value))) {
				continue;
			}
			
			gradeValue = null;
			try {
				gradeValue = Long.valueOf(value);
			} catch(Exception e) {
				LOGGER.warning("Unable to convert '" + value + "' to number!");
			}
			
			if (gradeValue != null) {
				sum += gradeValue.doubleValue();
			}
		}
		
		return String.valueOf(sum);
	}
}

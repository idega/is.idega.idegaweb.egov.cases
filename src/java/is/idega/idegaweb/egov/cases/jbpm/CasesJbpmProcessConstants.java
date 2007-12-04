package is.idega.idegaweb.egov.cases.jbpm;

/**
 * 
 * @author <a href="civilis@idega.com">Vytautas Čivilis</a>
 * @version $Revision: 1.2 $
 *
 * Last modified: $Date: 2007/12/04 14:04:20 $ by $Author: civilis $
 *
 */
public class CasesJbpmProcessConstants {
	
	private CasesJbpmProcessConstants () {}
	
	public static final String actionTakenVariableName = "string:actionTaken";
	public static final String caseIdVariableName = "string:caseId";
	public static final String caseTypeNameVariableName = "string:caseTypeName";
	public static final String caseCategoryNameVariableName = "string:caseCategoryName";
	public static final String caseCreatedDateVariableName = "string:caseCreatedDateString";
	public static final String caseAllocateToVariableName = "string:allocateTo";
	public static final String casePerformerIdVariableName = "string:performerId";
	public static final String caseStatusVariableName = "string:caseStatus";
	
	public static final String processDefinitionIdActionVariableName = "processDefinitionId";
	public static final String processInstanceIdActionVariableName = "processInstanceId";
	public static final String userIdActionVariableName = "userId";
	public static final String caseCategoryIdActionVariableName = "caseCategoryId";
	public static final String caseTypeActionVariableName = "caseType";
	public static final String startProcessActionVariableName = "simpleCaseStartProcess";
	public static final String proceedProcessActionVariableName = "simpleCaseProceedProcess";
}
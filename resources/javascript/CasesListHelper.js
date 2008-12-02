if (CasesListHelper == null) var CasesListHelper = {};

CasesListHelper.processVariables = [];

var CASE_GRID_STRING_CLICK_TO_EDIT = 'Click to edit...';
var CASE_GRID_STRING_ERROR_OCCURRED_CONFIRM_RELOAD_PAGE = 'Oops! Error occurred. Reloading current page might help to avoid it. Do you want to reload current page?';
var CASE_GRID_STRING_LOADING_PLEASE_WAIT = 'Loading, please wait...';

var CASE_GRID_TOGGLERS_FILTER = 'div.casesListGridExpanderStyleClass';

function initializeCasesList(caseToOpenId, localizations, debug) {
	if (localizations != null && localizations.length >= 3) {
		CASE_GRID_STRING_CLICK_TO_EDIT = localizations[0];						//	0
		CASE_GRID_STRING_ERROR_OCCURRED_CONFIRM_RELOAD_PAGE = localizations[1];	//	1
		CASE_GRID_STRING_LOADING_PLEASE_WAIT = localizations[2];				//	2
	}
	
	DWREngine.setErrorHandler(function(message, exception) {
		closeAllLoadingMessages();
		
		var loadingLabels = jQuery('div.loading');
		if (loadingLabels != null && loadingLabels.length > 0) {
			for (var i = 0; i < loadingLabels.length; i++) {
				jQuery(loadingLabels[i]).css('display', 'none');
			}
		}
		
		var text = exception == null ? CASE_GRID_STRING_ERROR_OCCURRED_CONFIRM_RELOAD_PAGE : exception.message + '\n' + CASE_GRID_STRING_ERROR_OCCURRED_CONFIRM_RELOAD_PAGE;
		if (window.confirm(debug ? text : CASE_GRID_STRING_ERROR_OCCURRED_CONFIRM_RELOAD_PAGE)) {
			reloadPage();
			return false;
		}
	});
	
	continueInitializeCasesList(caseToOpenId);
}

function continueInitializeCasesList(caseToOpenId) {
	var togglers = jQuery(CASE_GRID_TOGGLERS_FILTER);
	if (togglers == null || togglers.length == 0) {
		return false;
	}
	
	var caseIdPar = 'caseid';
	var toggler = null;
	var togglerIsRegisteredForActionParamValue = null;
	var togglerIsRegisteredForActionParamName = 'togglerisregisteredforaction';
	for (var i = 0; i < togglers.length; i++) {
		toggler = jQuery(togglers[i]);
		
		togglerIsRegisteredForActionParamValue = toggler.attr(togglerIsRegisteredForActionParamName);
		if (togglerIsRegisteredForActionParamValue == null || togglerIsRegisteredForActionParamValue == '') {
			toggler.attr(togglerIsRegisteredForActionParamName, 'true');
			
			toggler.click(function(event) {
				registerGridExpanderActionsForElement(event, this);
			});
		}
	}
	
	if (caseToOpenId != null && caseToOpenId != '') {
		var foundWhatToOpen = false;
		for (var i = 0; (i < togglers.length && !foundWhatToOpen); i++) {
			toggler = jQuery(togglers[i]);
			if (caseToOpenId == toggler.attr(caseIdPar)) {
				foundWhatToOpen = true;
				toggler.click();
			}
		}
	}
	
	var editableFields = jQuery('div.casesListBodyItemIsEditable');
	if (editableFields != null && editableFields.length > 0) {
		var editableField = null;
		var editableFieldId = 'editableCasesListElementId';
		var editableFieldName = 'editableCasesListElementName';
		var editableFieldParName = 'iseditable';
		var editableFieldParValue = null;
		
		for (var i = 0; i < editableFields.length; i++) {
			editableField = jQuery(editableFields[i]);
			
			editableFieldParValue = editableField.attr(editableFieldParName);
			if (editableFieldParValue == null || editableFieldParValue == '') {
				editableField.editable(function() {
					var editableElement = jQuery(this);
					var inlineElements = jQuery('input[name='+editableFieldName+']', editableElement);
					if (inlineElements == null || inlineElements.length == 0) {
						return;
					}
					
					var newDescription = jQuery(inlineElements[0]).attr('value');
					if (newDescription == null || newDescription == '') {
						return;
					}
					
					CasesEngine.setCaseSubject(editableElement.attr(caseIdPar), newDescription, {
						callback: function(result) {
							if (!result) {
								return;
							}
							
							editableElement.empty().text(newDescription);
						}
					});
				}, {
					id:			editableFieldId,
					name:		editableFieldName,
					tooltip:	CASE_GRID_STRING_CLICK_TO_EDIT,
					onblur:		'submit'
				});
				
				editableField.attr(editableFieldParName, 'true');
			}
		}
	}
}

function registerGridExpanderActionsForElement(event, element) {
	if (element == null) {
		return false;
	}
	
	if (event) {
		if (event.target != element) {
			if (event.stopPropagation) {
				event.stopPropagation();
			}
			event.cancelBubble = true;
			
			return false;
		}
	}
	
	var parentElement = jQuery(element).parent();
	var allTogglersInTheSameContainer = jQuery(CASE_GRID_TOGGLERS_FILTER, parentElement);
	
	var caseIdPar = 'caseid';
	var classExpanded = 'expandedWithNoImage';
	var changeImageClass = 'expanded';
	var classCaseWithInfo = 'caseWithInfo';
	
	var caseToExpand = jQuery(element);
	var show = false;
	
	var toggler = null;
	var changeImageAttribute = null;
	for (var i = 0; i < allTogglersInTheSameContainer.length; i++) {
		toggler = jQuery(allTogglersInTheSameContainer[i]);
		
		if (toggler.hasClass(classExpanded)) {
			toggler.removeClass(classExpanded);
		}
		else {
			toggler.addClass(classExpanded);
			show = true;
		}
	
		changeImageAttribute = toggler.attr('changeimage');
		if (changeImageAttribute != null && changeImageAttribute == 'true') {
			if (toggler.hasClass(changeImageClass)) {
				toggler.removeClass(changeImageClass);
			}
			else {
				toggler.addClass(changeImageClass);
			}
		}
	}
	
	var customerViewId = caseToExpand.attr('customerviewid');
	var customerView = jQuery('#' + customerViewId);
	if (customerView == null || customerView.length == 0) {
		return false;
	}
	if (show) {
		if (customerView.hasClass(classCaseWithInfo)) {
			jQuery(customerView).show('fast');
			return false;
		}
		
		showLoadingMessage(CASE_GRID_STRING_LOADING_PLEASE_WAIT);
		var caseId = caseToExpand.attr(caseIdPar);
		var usePDFDownloadColumn = caseToExpand.attr('usepdfdownloadcolumn') == 'true';
		var allowPDFSigning = caseToExpand.attr('allowpdfsigning') == 'true';
		CasesEngine.getCaseManagerView(new CasesBPMAssetProperties(caseId, CASE_GRID_CASE_PROCESSOR_TYPE, usePDFDownloadColumn, allowPDFSigning), {
			callback: function(component) {
				
				closeAllLoadingMessages();
				
				if (component == null) {
					return false;
				}
				
				IWCORE.insertHtml(component, customerView[0]);
				
				customerView.addClass(classCaseWithInfo);
                jQuery(customerView).show('fast');
			}
		});
	}
	else {
		customerView.hide('fast');
	}
}

function CasesBPMAssetProperties(caseId, processorType, usePDFDownloadColumn, allowPDFSigning) {
	this.caseId = caseId;
	this.processorType = processorType;
	
	this.usePDFDownloadColumn = usePDFDownloadColumn;
	this.allowPDFSigning = allowPDFSigning;
}

function searchForCases(parameters) {
	if (parameters == null || parameters.length < 13) {
		return false;
	}
	
	var caseNumberId = parameters[1];
	var caseDescriptionId = parameters[8];
	var nameId = parameters[2];
	var personalId = parameters[3];
	var processId = parameters[4];
	var statusId = parameters[5];
	var dateRangeId = parameters[6];
	var showStatisticsId = parameters[12];
	
	var caseNumberValue = DWRUtil.getValue(caseNumberId);
	var caseDescriptionValue = DWRUtil.getValue(caseDescriptionId);
	var nameValue = DWRUtil.getValue(nameId);
	var personalIdValue = DWRUtil.getValue(personalId);
	var processValue = DWRUtil.getValue(processId);
	if (processValue == '' || processValue == -1) {
		processValue = null;
	}
	var statusValue = DWRUtil.getValue(statusId);
	if (statusValue == '' || statusValue == -1) {
		statusValue = null;
	}
	var dateRangeValue = DWRUtil.getValue(dateRangeId);
	var caseListType = DWRUtil.getValue(parameters[9]);
	var contact = DWRUtil.getValue(parameters[10]);
	var showStatistics = jQuery('#' + showStatisticsId).attr('checked');
	if (!showStatistics) {
		showStatistics = false;
	}
	
	var usePDFDownloadColumn = true;
	var allowPDFSigning = true;
	var gridOpeners = jQuery('div.' + parameters[11]);
	if (gridOpeners != null && gridOpeners.length > 0) {
		var gridOpener = jQuery(gridOpeners[0]);
		
		usePDFDownloadColumn = gridOpener.attr('usepdfdownloadcolumn') == 'true';
		allowPDFSigning = gridOpener.attr('allowpdfsigning') == 'true';
	}
	
	showLoadingMessage(parameters[7]);
	CasesEngine.getCasesListByUserQuery(new CasesListSearchCriteriaBean(caseNumberValue, caseDescriptionValue, nameValue, personalIdValue, processValue,
																		statusValue, dateRangeValue, caseListType, contact, usePDFDownloadColumn,
																		allowPDFSigning, showStatistics, CasesListHelper.processVariables), {
		callback: function(component) {
			closeAllLoadingMessages();
			
			var lastCaseList = setDisplayPropertyToAllCasesLists(parameters[0], false);
			if (lastCaseList == null) {
				return false;
			}
			
			var container = lastCaseList.parent()[0];
			insertNodesToContainerBefore(component, container, lastCaseList[0]);
			continueInitializeCasesList(null);
		}
	});
}

function setDisplayPropertyToAllCasesLists(className, show) {
	removePreviousSearchResults(className);
	
	var casesLists = jQuery('div.' + className);
	if (casesLists == null || casesLists.length == 0) {
		return null;
	}
			
	var caseList = null;
	for (var i = 0; i < casesLists.length; i++) {
		caseList = jQuery(casesLists[i]);
		
		if (caseList.attr('searchresult') == null) {
			if (show) {
				caseList.show('fast');
			}
			else {
				caseList.hide('fast');
			}
		}
	}
	
	return caseList;
}

function clearSearchForCases(parameters) {
	DWRUtil.setValue(parameters[1], '');
	DWRUtil.setValue(parameters[8], '');
	DWRUtil.setValue(parameters[2], '');
	DWRUtil.setValue(parameters[3], '');
	DWRUtil.setValue(parameters[4], '-1');
	DWRUtil.setValue(parameters[5], '-1');
	DWRUtil.setValue(parameters[6], '');
	DWRUtil.setValue(parameters[10], '');
	jQuery('#' + parameters[12]).attr('checked', false);
	
	CasesListHelper.closeVariablesWindow();
	
	setDisplayPropertyToAllCasesLists(parameters[0], true);
}

function removePreviousSearchResults(className) {
	var casesLists = jQuery('div.' + className);
	if (casesLists == null || casesLists.length == 0) {
		return false;
	}
	
	var caseList = null;
	for (var i = 0; i < casesLists.length; i++) {
		caseList = jQuery(casesLists[i]);
	
		if (caseList.attr('searchresult') == 'true') {
			caseList.remove();
		}
	}
}

function CasesListSearchCriteriaBean(caseNumber, description, name, personalId, processId, statusId, dateRange, caseListType, contact, usePDFDownloadColumn,
										allowPDFSigning, showStatistics, processVariables) {
	this.caseNumber = caseNumber == '' ? null : caseNumber;
	this.description = description == '' ? null : description;
	this.name = name == '' ? null : name;
	this.personalId = personalId == '' ? null : personalId;
	this.processId = processId;
	this.statusId = statusId;
	this.dateRange = dateRange == '' ? null : dateRange;
	this.caseListType = caseListType == '' ? null : caseListType;
	this.contact = contact == '' ? null : contact;
	this.usePDFDownloadColumn = usePDFDownloadColumn;
	this.allowPDFSigning = allowPDFSigning;
	this.showStatistics = showStatistics;
	
	this.processVariables = processVariables;
}

function registerCasesSearcherBoxActions(id, parameters) {
	if (id == null) {
		return false;
	}
	
	var inputs = jQuery('input.textinput', jQuery('#' + id));
	if (inputs == null || inputs.length == 0) {
		return false;
	}
	
	var input = null;
	for (var i = 0; i < inputs.length; i++) {
		input = jQuery(inputs[i]);
		
		input.keyup(function(event) {
			if (isEnterEvent(event)) {
				searchForCases(parameters);
			}
		});
	}
}

CasesListHelper.getProcessDefinitionVariables = function(message, chooserId) {
	CasesListHelper.processVariables = [];
	
	var windowId = '#processDefinitionVariablesWindow';
	var processDefinitionId = DWRUtil.getValue(chooserId);
	
	if (processDefinitionId == null || processDefinitionId == -1 || processDefinitionId == '') {
		jQuery(windowId).hide('fast');
		return false;
	}
	
	showLoadingMessage(message);
	CasesEngine.getVariablesWindow(processDefinitionId, {
		callback: function(component) {
			closeAllLoadingMessages();
			
			var variablesWindow = jQuery(windowId);
			if (variablesWindow == null || variablesWindow.length == 0) {
				jQuery(document.body).append('<div id=\'processDefinitionVariablesWindow\' class=\'processDefinitionVariablesWindowStyle\' />');
				variablesWindow = jQuery(windowId);
			}
			
			var offsets = jQuery('#' + chooserId).offset();
			if (offsets == null) {
				return false;
			}
			
			var xCoord = offsets.left;
			var yCoord = offsets.top;
			variablesWindow.css('top', (yCoord + 20) + 'px');
			variablesWindow.css('left', xCoord + 'px');
			
			IWCORE.insertRenderedComponent(component, {
				container: variablesWindow,
				callback: function() {
					if (component.html == null) {
						CasesListHelper.closeVariablesWindow();
					}
					else {
						variablesWindow.show('fast');
					}
				},
				rewrite: true
			});
		}
	});
}

CasesListHelper.closeVariablesWindow = function() {
	CasesListHelper.processVariables = [];
	
	jQuery('#processDefinitionVariablesWindow').hide('fast');
	jQuery('#processDefinitionVariablesWindow').remove();
}

CasesListHelper.addVariableInput = function() {
	var chooserId = 'availableVariablesForProcess';
	if (chooserId == null || DWRUtil.getValue(chooserId) == -1) {
		return false;
	}
	
	var selectedOption = document.getElementById(chooserId).options[jQuery('#' + chooserId).attr('selectedIndex')];
	var variableLabel = jQuery(selectedOption).text();
	
	var variablesContainer = jQuery('#variableInputsContainer');
	var emptyInputs = jQuery('input.variableValueField[value=\'\']', variablesContainer);
	if (emptyInputs.length > 0) {
		jQuery.each(emptyInputs, function() {
			jQuery(this).parent().hide('fast').remove();
		});
	}
	
	var id = 'id' + new Date().getTime();
	var id2 = id + '2';
	
	variablesContainer.append('<div id=\''+id2+'\' style=\'display: none;\'><label class=\'variableValueLabel\' for=\'' + id + '\'>' + variableLabel +
								':</label></div>');
	
	var optionValue = jQuery(selectedOption).attr('value').split('@');
	
	var isDateField = optionValue[1] == 'D';
	var options = {
		className: isDateField ? 'com.idega.presentation.ui.IWDatePicker' : 'com.idega.presentation.ui.TextInput',
		properties: [{id: 'setId', value: id}, {id: 'setStyleClass', value: 'variableValueField'}, {id: isDateField ? 'setInputName' : 'setName',
					value: DWRUtil.getValue(chooserId)},
					{id: 'setOnKeyUp', value: 'if (isEnterEvent(event)) { CasesListHelper.addVariablesAndSearch(); } return false;'}],
		container: id2,
		callback: function() {
			jQuery('#' + id2).append('<img class=\'variableFieldDeleter\' onclick="jQuery(\'#' + id2 +
									'\').remove();" src=\'/idegaweb/bundles/is.idega.idegaweb.egov.cases/resources/images/delete.png\' />').show('fast');
		},
		append: true
	};
	IWCORE.getRenderedComponentByClassName(options);
}

CasesListHelper.addVariablesAndSearch = function() {
	CasesListHelper.processVariables = [];
	
	CasesListHelper.addVariables();
	jQuery('input.seachForCasesButton[type=\'button\']').click();
}

CasesListHelper.addVariables = function() {
	var allVariables = jQuery('input.variableValueField[value!=\'\']');
	if (allVariables == null || allVariables.length == 0) {
		CasesListHelper.processVariables = [];
		return false;
	}
	
	jQuery.each(allVariables, function() {
		var input = jQuery(this);
		var nameAttr = input.attr('name').split('@');
		CasesListHelper.processVariables.push({name: nameAttr[0], value: input.attr('value'), type: nameAttr[1]});
	});
}
if (CasesListHelper == null) var CasesListHelper = {};

CasesListHelper.processVariables = [];
CasesListHelper.listPages = [];
CasesListHelper.searchCriterias = [];

var CASE_GRID_STRING_CLICK_TO_EDIT = 'Click to edit...';
var CASE_GRID_STRING_ERROR_OCCURRED_CONFIRM_RELOAD_PAGE = 'Oops! Out of cheese error! Please reboot the Universe and try again...or the page.';
var CASE_GRID_STRING_LOADING_PLEASE_WAIT = 'Loading...';

var CASE_GRID_TOGGLERS_FILTER = 'div.casesListGridExpanderStyleClass';

CasesListHelper.PROCESS_ID = null;

function initializeCasesList(caseToOpenId, localizations, debug) {
		
	if (localizations != null && localizations.length >= 3) {
		CASE_GRID_STRING_CLICK_TO_EDIT = localizations[0];						//	0
		CASE_GRID_STRING_ERROR_OCCURRED_CONFIRM_RELOAD_PAGE = localizations[1];	//	1
		CASE_GRID_STRING_LOADING_PLEASE_WAIT = localizations[2];				//	2
	}

	dwr.engine.setErrorHandler(function(message, exception) {
		closeAllLoadingMessages();

		var loadingLabels = jQuery('div.loading');
		if (loadingLabels != null && loadingLabels.length > 0) {
			for (var i = 0; i < loadingLabels.length; i++) {
				jQuery(loadingLabels[i]).css('display', 'none');
			}
		}

		var text = exception == null ? CASE_GRID_STRING_ERROR_OCCURRED_CONFIRM_RELOAD_PAGE : exception.message + '\n' +
																							CASE_GRID_STRING_ERROR_OCCURRED_CONFIRM_RELOAD_PAGE;
		if (window.confirm(debug ? text : CASE_GRID_STRING_ERROR_OCCURRED_CONFIRM_RELOAD_PAGE)) {
			reloadPage();
			return false;
		}
	});

	continueInitializeCasesList(caseToOpenId);
}

CasesListHelper.openCaseView = function(caseToOpenId) {
	if (caseToOpenId == null || caseToOpenId == '') {
		return;
	}
	
	var togglers = jQuery(CASE_GRID_TOGGLERS_FILTER);
	if (togglers == null || togglers.length > 0) {
		return;
	}
	
	var foundWhatToOpen = false;
	for (var i = 0; (i < togglers.length && !foundWhatToOpen); i++) {
		toggler = jQuery(togglers[i]);
		
		if (caseToOpenId == toggler.attr(caseIdPar)) {
			foundWhatToOpen = true;
			toggler.click();
		}
	}
}

function continueInitializeCasesList(caseToOpenId) {
	var caseIdPar = 'caseid';
	var togglers = jQuery(CASE_GRID_TOGGLERS_FILTER);
	if (togglers != null && togglers.length > 0) { 
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
	if (parentElement == null || parentElement.length == 0)
		return false;
	var allTogglersInTheSameContainer = jQuery(CASE_GRID_TOGGLERS_FILTER, parentElement[0]);
	
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
		
		if (caseToExpand.attr('showLoadingMessage') == 'true')
			showLoadingMessage(CASE_GRID_STRING_LOADING_PLEASE_WAIT);
		var caseId = caseToExpand.attr(caseIdPar);
		var usePDFDownloadColumn = caseToExpand.attr('usepdfdownloadcolumn') == 'true';
		var allowPDFSigning = caseToExpand.attr('allowpdfsigning') == 'true';
		var hideEmptySection = caseToExpand.attr('hideemptysection') == 'true';
		var commentsManagerIdentifier = caseToExpand.attr('commentsmanageridentifier');
		var showAttachmentStatistics = caseToExpand.attr('showattachmentstatistics') == 'true';
		var showOnlyCreatorInContacts = caseToExpand.attr('showonlycreatorincontacts') == 'true';
		var showLogExportButton = caseToExpand.attr('showlogexportbutton') == 'true';
		var showComments = caseToExpand.attr('showcomments') == 'true';
		var showContacts = caseToExpand.attr('showcontacts') == 'true';
		var specialBackPage = caseToExpand.attr('specialbackpage');
		var nameFromExternalEntity = caseToExpand.attr('nameFromExternalEntity') == 'true';
		var showUserProfilePicture = caseToExpand.attr('showUserProfilePicture') == 'true';
		var addExportContacts = caseToExpand.attr('addExportContacts') == 'true';
		var showUserCompany = caseToExpand.attr('showUserCompany') == 'true';
		var showLastLoginDate = caseToExpand.attr('showLastLoginDate') == 'true';
		CasesEngine.getCaseManagerView(new CasesBPMAssetProperties(caseId, 
				CASE_GRID_CASE_PROCESSOR_TYPE, usePDFDownloadColumn, 
				allowPDFSigning, hideEmptySection, commentsManagerIdentifier, 
				showAttachmentStatistics, showOnlyCreatorInContacts, 
				showLogExportButton, showComments, showContacts, specialBackPage,
				nameFromExternalEntity, showUserProfilePicture,addExportContacts,
				showUserCompany,showLastLoginDate), {
			callback: function(component) {
				if (component == null) {
					closeAllLoadingMessages();
					return false;
				}
				
				if (caseToExpand.attr('waitForAllCasePartsLoaded') == 'false') {
					closeAllLoadingMessages();
				}
				
				IWCORE.insertHtml(component, customerView[0]);
				
				customerView.addClass(classCaseWithInfo);
                jQuery(customerView).show('fast');
			}
		});
	} else {
		customerView.hide('fast');
	}
}

function CasesBPMAssetProperties(caseId, processorType, usePDFDownloadColumn, 
		allowPDFSigning, hideEmptySection, commentsManagerIdentifier,
		showAttachmentStatistics, showOnlyCreatorInContacts, 
		showLogExportButton, showComments, showContacts, 
		specialBackPage, nameFromExternalEntity, showUserProfilePicture,addExportContacts,
		showUserCompany,showLastLoginDate) {
	this.caseId = caseId;
	this.processorType = processorType;
	
	this.usePDFDownloadColumn = usePDFDownloadColumn;
	this.allowPDFSigning = allowPDFSigning;
	this.hideEmptySection = hideEmptySection;
	this.showAttachmentStatistics = showAttachmentStatistics;
	this.showOnlyCreatorInContacts = showOnlyCreatorInContacts;
	
	this.commentsPersistenceManagerIdentifier = commentsManagerIdentifier;
	
	this.autoShowComments = window.location.href.indexOf('autoShowComments=true') != -1;
	
	this.showLogExportButton = showLogExportButton;
	this.showComments = showComments;
	this.showContacts = showContacts;	
	
	this.specialBackPage = specialBackPage;
	this.nameFromExternalEntity = nameFromExternalEntity;
	this.showUserProfilePicture = showUserProfilePicture;
	this.addExportContacts = addExportContacts;
	this.showUserCompany = showUserCompany;
	this.showLastLoginDate = showLastLoginDate;
}

CasesListHelper.getPager = function(fromPager, page) {
	if (fromPager == null || CasesListHelper.listPages == null) {
		return null;
	}
	
	CasesListHelper.listPages.push(fromPager);
	
	for (var i = 0; i < CasesListHelper.listPages.length; i++) {
		var cachedPager = CasesListHelper.listPages[i];
		if (cachedPager.instance == fromPager.instance && cachedPager.page == page && cachedPager.size == fromPager.size) {
			return cachedPager;
		}
	}
	
	return null;
}

function navigateCasesList(id, instanceId, containerId, newPage, count) {
	showLoadingMessage(CASE_GRID_STRING_LOADING_PLEASE_WAIT);
	
	var criteriasId = null;
	var currentPage = -1;
	var currentSize = -1;
	var searchResults = false;
	if (jQuery('#' + id).hasClass('listNavigatorPager')) {
		var currentPage = jQuery('a.currentPage', jQuery('#' + id).parent().parent()).text();
		if (IWCORE.isNumericValue(currentPage)) {
			currentPage++;
			currentPage--;
		}
		
		currentSize = dwr.util.getValue(jQuery('select.listPagerSize', jQuery('#' + id).parent().parent().parent()).attr('id'));
		criteriasId = jQuery('input.listNavigatorIdentifier', jQuery('#' + id).parent().parent().parent()).attr('value');
		searchResults = jQuery('input.casesListNavigatorForSearchResults', jQuery('#' + id).parent().parent().parent().parent()).attr('value') == 'true';
	} else if (jQuery('#' + id).hasClass('listPagerSize')) {
		currentSize = dwr.util.getValue(id);
		criteriasId = jQuery('input.listNavigatorIdentifier', jQuery('#' + id).parent()).attr('value');
		searchResults = jQuery('input.casesListNavigatorForSearchResults', jQuery('#' + id).parent().parent()).attr('value') == 'true';
	}
	
	var fromPager = currentPage < 0 ? null : {instance: instanceId, container: containerId, page: currentPage, size: currentSize};
	var toPager = CasesListHelper.getPager(fromPager, newPage);
	if (criteriasId != null || searchResults) {
		CasesListHelper.listPages = [];
		toPager = null;
	}
	
	jQuery('#' + containerId).hide('fast', function() {
		CasesListHelper.displayPager(instanceId, containerId, newPage, count, toPager, criteriasId, searchResults);
	});
}

CasesListHelper.getCriterias = function(criteriasId) {
	if (criteriasId == null || CasesListHelper.searchCriterias == null) {
		return null;
	}
	
	for (var i = 0; i < CasesListHelper.searchCriterias.length; i++) {
		if (criteriasId == CasesListHelper.searchCriterias[i].id) {
			return CasesListHelper.searchCriterias[i].criteria;
		}
	}
	
	return null;
}

CasesListHelper.displayPager = function(instanceId, containerId, page, count, toPager, criteriasId, searchResults) {
	if (toPager == null) {
		CasesEngine.setCasesPagerAttributes(page, count, {
			callback: function(result) {
				var criterias = CasesListHelper.getCriterias(criteriasId);
				if (criterias == null) {
					var properties = [{id: 'setPage', value: page}, {id: 'setPageSize', value: count}];
					if (searchResults) {
						properties.push({id: 'setSearchResultsId', value: window.location.pathname});
					}
					
					var parentContainer = jQuery('#' + containerId).parent();
					var parentContainerId = parentContainer.attr('id');
					if (parentContainerId != null && parentContainerId != '')
						parentContainer = parentContainerId;
					jQuery('div.mainCasesListContainerStyleClass[searchresult=\'true\']').each(function() {
						jQuery(this).hide();
					});
					IWCORE.renderComponent(instanceId, parentContainer, function() {
						closeAllLoadingMessages(toPager);
					}, properties, {append: true});
				} else {
					criterias.page = page;
					criterias.pageSize = count;
					criterias.clearResults = false;
					CasesListHelper.getRenderedCasesListByCriterias(criterias, 'mainCasesListContainerStyleClass', function() {
						jQuery('div.mainCasesListContainerStyleClass').each(function() {
							var caseList = jQuery(this);
							if (caseList.attr('searchresult') == null) {
								caseList.css('display', 'none');
								
								jQuery('ul.legend', caseList.parent()).css('display', 'none');
							}
						});
						jQuery('#' + containerId).show('fast');
					});
				}
			}
		});
	} else {
		jQuery('#' + toPager.container).show('fast', function() {
			closeAllLoadingMessages();
		});
	}
}

function gotoCasesListPage(id, page, size, instanceId, containerId) {
	navigateCasesList(id, instanceId, containerId, page, size);
}

function changeCasesListPageSize(id, size, instanceId, containerId) {
	CasesListHelper.listPages = [];
	navigateCasesList(id, instanceId, containerId, 1, size);
}

function searchForCases(parameters) {
	if (parameters == null) {
		alert('Search parameters are not provided!');
		return false;
	}
	 
	if (parameters.action != null)
		return parameters.action(parameters);
	 
	if (parameters.length == null || parameters.length < 14) {
		alert('Not enough parameters are provided!');
		return false;
	}

	// Component id's
	var caseNumberId = parameters[1];
	var caseDescriptionId = parameters[8];
	var nameId = parameters[2];
	var personalId = parameters[3];
	var processId = parameters[4];
	var statusId = parameters[5];
	var dateRangeId = parameters[6];
	var showStatisticsId = parameters[12];
	var showAllCases = parameters[13];
	var addressId = parameters[14];

	// Component values
	var caseNumberValue = dwr.util.getValue(caseNumberId);
	var caseDescriptionValue = dwr.util.getValue(caseDescriptionId);
	var nameValue = dwr.util.getValue(nameId);
	var personalIdValue = dwr.util.getValue(personalId);

	var processValue = null;
	if (isNaN(processId)) {
		processValue = dwr.util.getValue(processId);
	} else {
		processValue = processId;
	}
	 
	if (processValue == '' || processValue == -1) {
		processValue = null;
	}
	var statusValue = dwr.util.getValue(statusId);
	if (statusValue == '' || statusValue == -1) {
		statusValue = null;
	}
	var dateRangeValue = dwr.util.getValue(dateRangeId);
	var caseListType = dwr.util.getValue(parameters[9]);
	var contact = dwr.util.getValue(parameters[10]);
	var showStatistics = jQuery('#' + showStatisticsId).attr('checked') == 'checked';
	if (!showStatistics)
		showStatistics = false;
	
	var addressValue = dwr.util.getValue(addressId);
	
	var groupId = dwr.util.getValue(parameters[15]);
	if (groupId == '' || groupId == -1) {
		groupId = null;
	}
	
	// Additional properties
	var usePDFDownloadColumn = true;
	var allowPDFSigning = true;
	var hideEmptySection = false;
	var instanceId = null;
	var gridOpeners = jQuery('div.' + parameters[11]);
	var onlySubscribedCases = false;
	var casesListCustomizer = null;
	var customColumns = null;
	var showLoadingMessageAttr = true;
	var showAttachmentStatistics = false;
	var showUserProfilePicture = true;
	var addExportContacts = false;
	var showUserCompany = false;
	if (gridOpeners != null && gridOpeners.length > 0) {
		var gridOpener = jQuery(gridOpeners[0]);
		
		usePDFDownloadColumn = gridOpener.attr('usepdfdownloadcolumn') == 'true';
		allowPDFSigning = gridOpener.attr('allowpdfsigning') == 'true';
		hideEmptySection = gridOpener.attr('hideEmptySection') == 'true';
		showLoadingMessageAttr = gridOpener.attr('showLoadingMessage') == 'true';
		
		instanceId = jQuery('input.casesListInstanceIdProperty').attr('value');
		onlySubscribedCases = gridOpener.attr('onlysubscribedcases') == 'true';
		
		casesListCustomizer = gridOpener.attr('caseslistcustomizer');
		
		customColumns = gridOpener.attr('customcolumns');
		
		showAttachmentStatistics = gridOpener.attr('showAttachmentStatistics');
		showUserProfilePicture = gridOpener.attr('showUserProfilePicture');
		addExportContacts = gridOpener.attr('addExportContacts');
		showUserCompany = gridOpener.attr('showUserCompany');
		
		if (customColumns != null)
			customColumns = customColumns.split(',');
	}
	var showCaseNumberColumn = jQuery('div.casesListHeadersContainerItemCaseNumber').length == 0 ? false : true;
	var showCreationTimeInDateColumn = jQuery('div.showOnlyDateValueForCaseInCasesListRow').length == 0 ? false : true;
	
	CasesListHelper.processVariables = [];
	CasesListHelper.addVariables();
	
	showLoadingMessage(parameters[7]);
	
	var criteriasId = 'id' + new Date().getTime() + '' + Math.floor(Math.random() * 10001);
	var criterias = new CasesListSearchCriteriaBean(caseNumberValue, caseDescriptionValue, nameValue, personalIdValue, processValue, statusValue,
		dateRangeValue, caseListType, contact, usePDFDownloadColumn, allowPDFSigning, showStatistics, CasesListHelper.processVariables,
		hideEmptySection, showCaseNumberColumn,	showCreationTimeInDateColumn, instanceId, onlySubscribedCases, 1,
		dwr.util.getValue(jQuery('select.listPagerSize').attr('id')), jQuery('div.mainCasesListContainerStyleClass').parent().parent().attr('id'),
		criteriasId, showAllCases, casesListCustomizer, customColumns, showLoadingMessageAttr, groupId,showAttachmentStatistics,
		showUserProfilePicture,addExportContacts,showUserCompany
	);
	criterias.clearResults = true;
	criterias.address = addressValue;
	
	CasesListHelper.searchCriterias.push({id: criteriasId, criteria: criterias});
	CasesListHelper.getRenderedCasesListByCriterias(criterias, parameters[0], null);
}

CasesListHelper.getRenderedCasesListByCriterias = function(criterias, className, callback) {
	CasesEngine.getCasesListByUserQuery(criterias, {
		callback: function(component) {
			CasesListHelper.insertRenderedCasesList(component, className, callback);
		}
	});
}

CasesListHelper.insertRenderedCasesList = function(component, className, callback) {
	closeAllLoadingMessages();
	
	className = className == null ? 'mainCasesListContainerStyleClass' : className;
	var lastCaseList = setDisplayPropertyToAllCasesLists(className, false);
	if (lastCaseList == null || lastCaseList.length == 0) {
		reloadPage();
		return false;
	}
	
	jQuery('div.mainCasesListContainerStyleClass[searchresult=\'true\']').each(function() {
		jQuery(this).hide();
	});
	
	var container = lastCaseList.parent().parent().parent()[0];
	var insertBefore = lastCaseList.parent().parent()[0];
	insertNodesToContainerBefore(component, container, insertBefore);
	continueInitializeCasesList(null);
	
	if (callback)
		callback();
}

function setDisplayPropertyToAllCasesLists(className, show) {
	removePreviousSearchResults(className);
	
	var casesLists = jQuery('div.' + className);
	if (casesLists == null || casesLists.length == 0) {
		reloadPage();
		return null;
	}
	
	var fullListVisible = false;
	var caseList = null;
	for (var i = 0; i < casesLists.length; i++) {
		caseList = jQuery(casesLists[i]);
		
		if (caseList.attr('searchresult') == null) {
			if (show && !fullListVisible) {
				fullListVisible = true;
				caseList.show('fast');
				caseList.parent().parent().show('fast');
			} else {
				caseList.hide('fast');
			}
		}
	}
	
	return caseList;
}

CasesListHelper.clearSearchResultsCallback = function() {}

function clearSearchForCases(parameters) {
	CasesEngine.clearSearchResults(window.location.pathname, {
		callback: function(result) {
			if (parameters != null) {
				var cssClassName = null;
				try {
					if (parameters != null && parameters.length >= 14) {
						cssClassName = parameters[0];
						dwr.util.setValue(parameters[1], '');
						dwr.util.setValue(parameters[8], '');
						dwr.util.setValue(parameters[2], '');
						dwr.util.setValue(parameters[3], '');
						dwr.util.setValue(parameters[4], '-1');
						dwr.util.setValue(parameters[5], '-1');
						dwr.util.setValue(parameters[6], '');
						dwr.util.setValue(parameters[10], '');
						dwr.util.setValue(parameters[14], '');
						dwr.util.setValue(parameters[15], '-1');
						jQuery('#' + parameters[12]).attr('checked', false);
					}
				} catch (e) {}
				cssClassName = cssClassName == null ? parameters.cssClassName : cssClassName;
	
				if (isNaN(parameters[4])) {
					CasesListHelper.closeVariablesWindow();
				} else {
					jQuery('div', jQuery('#variableInputsContainer')).hide('normal', function() {
						jQuery('div', jQuery('#variableInputsContainer')).empty();
					});
				}
	
				CasesListHelper.closeSortingOptionsWindow();
	
				jQuery('div.mainCasesListContainerStyleClass[searchresult=\'true\']').each(function() {
					jQuery(this).remove();
				});
				setDisplayPropertyToAllCasesLists(cssClassName, true);
			}

			CasesListHelper.searchCriterias = [];
			
			if (CasesListHelper.clearSearchResultsCallback != null)
				CasesListHelper.clearSearchResultsCallback();
		}
	});
}

function removePreviousSearchResults(className) {
	var casesLists = jQuery('div.' + className);
	if (casesLists == null || casesLists.length == 0) {
		reloadPage();
		return false;
	}
	
	var uuid = null;
	var container = null;
	
	var caseList = null;
	for (var i = 0; i < casesLists.length; i++) {
		caseList = jQuery(casesLists[i]);
	
		if (caseList.attr('searchresult') == 'true') {
			container = jQuery('input.casesListInstanceIdProperty').parent().parent().parent().attr('id');
			uuid = jQuery('input.casesListInstanceIdProperty').attr('value');
			caseList.remove();
		}
	}
	
	casesLists = jQuery('div.' + className);
	if (casesLists == null || casesLists.length == 0) {
		if (uuid == null || container == null) {
			reloadPage();
			return false;
		}
		
		showLoadingMessage(CASE_GRID_STRING_LOADING_PLEASE_WAIT);
		IWCORE.renderComponent(uuid, container, function() {
			closeAllLoadingMessages();
			continueInitializeCasesList(null);
		}, null);
	}
}

CasesListHelper.getStatusesToShow = function() {
	var selectedStatusesToShow = jQuery('input.casesListStatusesToShow').attr('value');
	return selectedStatusesToShow == null || selectedStatusesToShow == '' || selectedStatusesToShow == -1 ? null : new String(selectedStatusesToShow);
}
CasesListHelper.getStatusesToHide = function() {
	var selectedStatusesToHide = jQuery('input.casesListStatusesToHide').attr('value');
	return selectedStatusesToHide == null || selectedStatusesToHide == '' || selectedStatusesToHide == -1 ? null : new String(selectedStatusesToHide);
}

function CasesListSearchCriteriaBean(caseNumber, description, name, personalId, processId, statusId, dateRange, caseListType, contact,
	usePDFDownloadColumn, allowPDFSigning, showStatistics, processVariables, hideEmptySection, showCaseNumberColumn, showCreationTimeInDateColumn,
	instanceId, onlySubscribedCases, page, pageSize, componentId, criteriasId, showAllCases, casesListCustomizer, customColumns,
	showLoadingMessageAttr, groupId,showAttachmentStatistics,showUserProfilePicture,addExportContacts,showUserCompany) {
	
	this.subscribersGroupId = groupId;
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
	this.hideEmptySection = hideEmptySection;
	
	this.processVariables = processVariables;
	
	this.showCaseNumberColumn = showCaseNumberColumn;
	this.showCreationTimeInDateColumn = showCreationTimeInDateColumn;
	
	this.id = window.location.pathname;
	this.instanceId = instanceId;
	
	this.sortingOptions = CasesListHelper.getSortedSortingOptions();
	
	this.statusesToShow = CasesListHelper.getStatusesToShow();
	this.statusesToHide = CasesListHelper.getStatusesToHide();
	
	this.caseCodes = jQuery('input.casesListCaseCodes').attr('value');
	this.onlySubscribedCases = onlySubscribedCases;
	
	this.page = page;
	this.pageSize = pageSize;
	
	this.componentId = componentId;
	this.criteriasId = criteriasId;
	
	this.showAllCases = showAllCases;
	
	this.casesListCustomizer = casesListCustomizer;
	this.customColumns = customColumns;
	
	this.showLoadingMessage = showLoadingMessageAttr;
	this.showAttachmentStatistics = showAttachmentStatistics;
	this.showUserProfilePicture = showUserProfilePicture;
	this.addExportContacts = addExportContacts;
	this.showUserCompany = showUserCompany;
}

function registerCasesSearcherBoxActions(id, parameters) {
	try {
		if (jQuery.url.param('tiId') != null) {
			jQuery('.casesSearcherBoxStyleClass').hide('fast', function() {
				jQuery('.casesSearcherBoxStyleClass').remove();
			});
			return;
		}
	} catch (e) {}
	
	if (window.location.href.indexOf('impra') != -1 && jQuery('#sidebar').length > 0) {
		jQuery('.casesSearcherBoxStyleClass').remove().insertAfter('#sidebar').wrap('<div class="box" id="casesFilter"><div class="content"></div></div>');
	}
	
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

CasesListHelper.getProcessDefinitionVariablesByIwID = function(message, chooserId) {
	CasesListHelper.closeVariablesWindow();

	var placeSelector = jQuery('#' + chooserId).parent();
	var processDefinitionId = dwr.util.getValue(chooserId);

	CasesListHelper.getProcessDefinitionVariables(message, processDefinitionId, placeSelector);
}

CasesListHelper.getProcessDefinitionVariablesByProcessID = function(message, processDefinitionId) {
	CasesListHelper.closeVariablesWindow();

	var placeSelector = jQuery('div.variablesSelectorDropdown');
	CasesListHelper.PROCESS_ID = processDefinitionId;

	if (message == null || message == '') {
		message = "";
	}

	CasesListHelper.getProcessDefinitionVariables(message, processDefinitionId, placeSelector);
}

CasesListHelper.getProcessDefinitionVariables = function(message, processDefinitionId, placeSelector) {
	var windowId = '#processDefinitionVariablesWindow';

	if (processDefinitionId == null || processDefinitionId == -1 || processDefinitionId == '' ||
		placeSelector == null || placeSelector == -1 || placeSelector == '') {
		
		jQuery(windowId).hide('fast');
		return false;
	}

	showLoadingMessage(message);

	CasesEngine.getVariablesWindow(processDefinitionId, {
		callback: function(component) {
			closeAllLoadingMessages();

			jQuery(placeSelector).append('<div id=\'processDefinitionVariablesWindow\' class=\'processDefinitionVariablesWindowStyle\' />');
			var variablesWindow = jQuery(windowId);

			IWCORE.insertRenderedComponent(component, {
				container: variablesWindow,
				callback: function() {
					if (component.html == null) {
						CasesListHelper.closeVariablesWindow();
					}
					else {
						variablesWindow.show('fast');
					}
					
					var sortingOptionsDropdownId = jQuery(jQuery('select.casesSearcherResultsSortingOptionsChooserStyle')[0]).attr('id');
					if (sortingOptionsDropdownId != null) {
						jQuery.each(jQuery('div.processVariableSortingOption'), function() {
							CasesListHelper.removeSelectedSearchResultsSortingOption(jQuery(this).attr('id'), sortingOptionsDropdownId, true);
						});
						jQuery.each(jQuery('option.processVariableSortingOption'), function() {
							jQuery(this).remove();
						});
						
						jQuery.each(jQuery('option', variablesWindow), function() {
							var option = jQuery(this);
							
							CasesListHelper.addSortingOption(sortingOptionsDropdownId, option.attr('value'), option.text(), 'processVariableSortingOption');
						});
					}
				},
				append: true
			});
		}
	});
}

CasesListHelper.closeVariablesWindow = function() {
	CasesListHelper.processVariables = [];
	
	jQuery('#processDefinitionVariablesWindow').hide('fast');
	jQuery('#processDefinitionVariablesWindow').remove();
}

/**
 * <p>
 * Procedure that inserts a one section <code>div</code> of:
 * <ul>
 * 	<li>{@link com.idega.presentation.ui.IWDatePicker}</li>
 * 	<li>{@link com.idega.presentation.ui.TextInput}</li>
 * 	<li>{@link com.idega.presentation.ui.DropdownMenu}</li>
 * 	<li>{@link com.idega.presentation.ui.SelectionBox}</li>
 * </ul>
 * into {@link is.idega.idegaweb.egov.bpm.facelets.UIProcessVariables} section.
 * In cases of last two, it fills lists with data found in:
 * {@link MultipleSelectionVariablesResolver#getValues(String, String)} 
 * implementations.
 * </p>
 */
CasesListHelper.addVariableInput = function() {
	var chooserId = 'availableVariablesForProcess';
	if (chooserId == null || dwr.util.getValue(chooserId) == -1)
		return false;

	var variablesList = document.getElementById(chooserId);
	var selectedOption = variablesList.options[variablesList.selectedIndex];
	var variableLabel = jQuery(selectedOption).text();

	var variablesContainer = jQuery('#variableInputsContainer');
	var chosenVariable = dwr.util.getValue(chooserId);
	if (jQuery('.variableValueField[name=\''+chosenVariable+'\']').length > 0) {
		dwr.util.setValue(chooserId, '-1');
		return;
	}
	
	var id = 'id' + new Date().getTime();
	var id2 = id + '2';
	
	variablesContainer.append('<div id=\''+id2+'\' style=\'display: none;\'><label class=\'variableValueLabel\' for=\'' + id + '\'>' + variableLabel +
								':</label></div>');
	
	var optionValue = jQuery(selectedOption).attr('value').split('@');
	var deleteImage = jQuery('input.deleteBPMVariableImagePath').attr('value');
	var loadingMessage = jQuery('input.loadBPMVariableInputField').attr('value');
	
	showLoadingMessage(loadingMessage);
	
	var isDateField = optionValue[1] == 'D';
	var isMultipleObjectsField = optionValue[1] == 'B' || optionValue[0] == 'string_harbourNr';

	var procDefId = null;
	if (CasesListHelper.PROCESS_ID == null) {
		procDefId = dwr.util.getValue(jQuery('select.availableVariablesChooserForProcess').attr('id'));
	} else {
		procDefId = CasesListHelper.PROCESS_ID;
	}

	LazyLoader.loadMultiple(['/dwr/engine.js', '/dwr/interface/WebUtil.js'], function() {
		WebUtil.getBooleanApplicationProperty("advanced_bpm_cases_search", true, {
			callback: function(advancedSearch) {
				var options = {
					className: isDateField ? 'com.idega.presentation.ui.IWDatePicker' : 'com.idega.presentation.ui.TextInput',
					properties: [
									{id: 'setId', value: id},
									{id: 'setStyleClass', value: 'variableValueField'},
									{id: isDateField ? 'setInputName' : 'setName', value: dwr.util.getValue(chooserId)},
									{id: 'setOnKeyUp', value: 'if (isEnterEvent(event)) { CasesListHelper.addVariablesAndSearch(); } return false;'}
								],
					container: id2,
					callback: function() {
						closeAllLoadingMessages();
						dwr.util.setValue(chooserId, '-1');
						jQuery('#' + id2).append('<img class=\'variableFieldDeleter\' onclick="jQuery(\'#' + id2 +
							'\').hide(\'normal\', function() {jQuery(\'#' + id2 + '\').remove();});" src=\''+deleteImage+'\' />').show('fast');
					},
					append: true
				};
				
				var valueToSet = null;
				var beanName = 'bpmVariableValueResolver' + optionValue[0];
				CasesEngine.isResolverExist(beanName, {
					callback: function(resolverClass) {
						if (resolverClass != null && resolverClass != '') {
							var useProvidedClass = true;
							if (isMultipleObjectsField && advancedSearch) {
								options.className = 'com.idega.presentation.ui.SelectionBox';
								useProvidedClass = false;
							} else {
								options.className = 'com.idega.presentation.ui.DropdownMenu';
							}
							if (useProvidedClass && resolverClass != options.className)
								options.className = resolverClass;

							valueToSet = '#{' + beanName + '.getValues(\'' + procDefId + '\', \'' + optionValue[0] + '\')}';
						}

						if (valueToSet != null)
							options.properties.push({id: 'setValue', value: valueToSet});

						IWCORE.getRenderedComponentByClassName(options);
					}
				});				
			}
		});
	}, null);	
}

CasesListHelper.addVariablesAndSearch = function() {
	jQuery('input.seachForCasesButton[type=\'button\']').click();
}

CasesListHelper.addVariables = function() {
	jQuery.each(jQuery('.variableValueField'), function() {
		var input = this;
		
		var variableValue = dwr.util.getValue(input.id);
		if (variableValue != null && variableValue != '') {
			var nameAttr = input.name.split('@');
			
			var multipleValues = false;
			if (typeof variableValue == 'object') {
				var values = '';
				for (var i = 0; i < variableValue.length; i++) {
					values += variableValue[i];
					if (i + 1 < variableValue.length) {
						values += ';';
					}
				}
				variableValue = values;
				multipleValues = true;
			}
			
			var variableObject = {name: nameAttr[0], value: variableValue, type: nameAttr[1], multiple: multipleValues};
			if (!existsElementInArray(CasesListHelper.processVariables, variableObject)) {
				CasesListHelper.processVariables.push(variableObject);
			}
		}
	});
}

CasesListHelper.resetVariablesAndAddNewOne = function() {
	var inputsToRemove = [];
	jQuery.each(jQuery('input.variableValueField'), function() {
		var inputToRemove = jQuery(this);
		
		if (inputToRemove.attr('value') == null || inputToRemove.attr('value') == '') {
			inputsToRemove.push(inputToRemove);
		}
	});
	jQuery.each(inputsToRemove, function() {
		jQuery(this).parent().hide('normal', function() {jQuery(this).remove();});
	});
	jQuery('#availableVariablesForProcess').attr('selectedIndex', 0);
}

CasesListHelper.exportSearchResults = function(message) {
	showLoadingMessage(message);
	var addexportcontacts = jQuery('[addexportcontacts="true"]').length > 0;
	var showusercompany = jQuery('[showusercompany="true"]').length > 0;
	CasesEngine.getExportedSearchResults(window.location.pathname,addexportcontacts,showusercompany, {
		callback: function(result) {
			if (result == null || result.id != 'true') {
				closeAllLoadingMessages();
				
				if (result != null && result.value != null)
					humanMsg.displayMsg(result.value);
				
				return;
			}
			
			window.location.href = result.value;
			closeAllLoadingMessages();
		}
	});
}

CasesListHelper.exportAllCases = function(message, containerId, instanceId,isExportContacts,isShowCompany) {
	showLoadingMessage(message);
	CasesEngine.getExportedCases(instanceId,window.location.pathname,isExportContacts,isShowCompany, {
		callback: function(result) {
			if (result == null || result.id != 'true') {
				closeAllLoadingMessages();
				
				if (result != null && result.value != null)
					humanMsg.displayMsg(result.value);
				
				return;
			}
			
			window.location.href = result.value;
			closeAllLoadingMessages();
		}
	});
}

CasesListHelper.sortingOptions = [];

CasesListHelper.closeSortingOptionsWindow = function() {
	jQuery.each(jQuery('select.casesSearcherResultsSortingOptionsChooserStyle'), function() {
		var dropdownId = jQuery(this).attr('id');
		CasesListHelper.removeAllSearchResultsSortingOptions(dropdownId);
		CasesListHelper.addSearchResultsSortingOption(dropdownId);
	});
	CasesListHelper.sortingOptions = [];
}

CasesListHelper.addSearchResultsSortingOption = function(dropdownId) {
	dwr.util.setValue(dropdownId, '-1');
}

CasesListHelper.addSelectedSearchResultsSortingOption = function(dropdownId) {
	var dropdown = jQuery('#' + dropdownId)[0];
	var selectedOption = dropdown.options[dropdown.selectedIndex];
	if (selectedOption.value == -1) {
		return;
	}
	
	var value = selectedOption.value;
	if (value.indexOf('@') != -1) {
		value = value.split('@')[0];
	}
	
	CasesListHelper.sortingOptions.push({id: value, value: selectedOption.text,
		selected: jQuery(selectedOption).hasClass('defaultCasesSearcherSortingOption')});
	dropdown.remove(dropdown.selectedIndex);
	CasesListHelper.addSearchResultsSortingOption(dropdownId);
	
	var selectedOptionsContainer = CasesListHelper.getSortingOptionsContainer(dropdownId);
	var containerId = 'id' + value;
	selectedOptionsContainer.append('<li id=\''+containerId+'\' style=\'display: none;\' class=\'casesSearchResultsSortingOptionContainer\'>' +
		'<span class=\'casesSearchResultsSortingOptionsRemoveOptionSpan\' title=\''+selectedOption.text+'\'>'+selectedOption.text+'</span>' +
		'<a class=\'casesSearchResultsSortingOptionsRemoveOption\' href=\'javascript:void(0);\' onclick=\''+
		'CasesListHelper.removeSelectedSearchResultsSortingOption("'+containerId+'", "'+dropdownId+'", false);\'>'+
		'<span class=\'casesSearchResultsSortingOptionsRemoveOptionInnerSpan\'></span></a></li>');
	
	selectedOptionsContainer.parent().show('fast', function() {
		jQuery('#' + containerId).addClass(selectedOption.className);
		jQuery('#' + containerId).show('normal');
	});
}

CasesListHelper.removeAllSearchResultsSortingOptions = function(dropdownId) {
	jQuery.each(jQuery('li.casesSearchResultsSortingOptionContainer', jQuery('#' + dropdownId).parent()), function() {
		CasesListHelper.removeSelectedSearchResultsSortingOption(jQuery(this).attr('id'), dropdownId, true);
	});
}

CasesListHelper.removeSelectedSearchResultsSortingOption = function(id, dropdownId, restoreOnlyDefault) {
	var optionContainer = jQuery('#' + id);
	var mainContainer = optionContainer.parent().parent();
	
	var defaultOption = false;
	id = id.replace('id', '');
	var index = 0;
	var found = false;
	for (var i = 0; (i < CasesListHelper.sortingOptions.length && !found); i++) {
		if (id == CasesListHelper.sortingOptions[i].id) {
			index = i;
			defaultOption = CasesListHelper.sortingOptions[i].selected;
			found = true;
		}
	}
	CasesListHelper.sortingOptions.splice(index, 1);
	
	var restore = true;
	if (restoreOnlyDefault) {
		retore = defaultOption;
	}
	
	if (restore) {
		CasesListHelper.addSortingOption(dropdownId, id, optionContainer.text(), defaultOption ?
			'defaultCasesSearcherSortingOption' : 'processVariableSortingOption');
	}
	
	optionContainer.hide('nomal', function() {
		optionContainer.remove();
		
		if (jQuery('li.casesSearchResultsSortingOptionContainer', mainContainer).length == 0) {
			mainContainer.hide('normal', function() {
				mainContainer.remove();
			});	
		}
	});
	
	CasesListHelper.addSearchResultsSortingOption(dropdownId);
}

CasesListHelper.addSortingOption = function(dropdownId, value, text, styleClass) {
	if (value == null || value == -1) {
		return;
	}
	
	var newOption = document.createElement('option');
	newOption.value = value;
	newOption.text = text;
	if (styleClass != null) {
		newOption.className = styleClass;
	}
	if (IE) {
		document.getElementById(dropdownId).add(newOption);
	} else {
		document.getElementById(dropdownId).add(newOption, null);
	}
}

CasesListHelper.getSortingOptionsContainer = function(dropdownId) {
	var dropdown = jQuery('#' + dropdownId);
	var windowStyle = 'casesSearchResultsSortingOptions';
	var sortingOptionsWindow = jQuery('div.' + windowStyle, dropdown.parent());
	if (sortingOptionsWindow.length == 0) {
		dropdown.parent().append('<div class=\''+windowStyle+'\' style=\'display: none;\'><ul class=\'casesSearchResultsSortingOptionsList\'></ul></div>');
		
		sortingOptionsWindow = jQuery('div.' + windowStyle, dropdown.parent());
	}
	
	var sortableList = jQuery('ul.casesSearchResultsSortingOptionsList', sortingOptionsWindow);
	
	var sortableOptionsListClassName = 'casesSearchResultsSortingOptionsListSortableList';
	if (!sortableList.hasClass(sortableOptionsListClassName)) {
		sortableList.sortable({
			placeholder: 'ui-state-highlight'
		});
		sortableList.disableSelection();
		sortableList.addClass(sortableOptionsListClassName);
	}
	
	return sortableList;
}

CasesListHelper.getSortedSortingOptions = function() {
	var dropdowns = jQuery('select.casesSearcherResultsSortingOptionsChooserStyle');
	if (dropdowns.length == 0) {
		return null;
	}
	
	var sortingOptionsList = CasesListHelper.getSortingOptionsContainer(jQuery(dropdowns[0]).attr('id'));
	var sortedOptions = sortingOptionsList.sortable('toArray');
	if (sortedOptions == null || sortedOptions.length == 0) {
		return null;
	}
	
	var options = [];
	for (var i = 0; i < sortedOptions.length; i++) {
		var value = sortedOptions[i];
		value = value.replace('id', '');
		
		options.push({id: value, value: value});
	}
	
	return options;
}
var CASE_GRID_STRING_CONTACT_NAME = 'Name';
var CASE_GRID_STRING_TASK_NAME = 'Task name';
var CASE_GRID_STRING_FORM_NAME = 'Document name';
var CASE_GRID_STRING_SENDER = 'Sender';
var CASE_GRID_STRING_DATE = 'Date';
var CASE_GRID_STRING_TAKEN_BY = 'Taken by';
var CASE_GRID_STRING_EMAIL_ADDRESS = 'E-mail address';
var CASE_GRID_STRING_PHONE_NUMBER = 'Phone number';
var CASE_GRID_STRING_ADDRESS = 'Address';
var CASE_GRID_STRING_SUBJECT = 'Subject';
var CASE_GRID_STRING_FILE_DESCRIPTION = 'Descriptive name';
var CASE_GRID_STRING_FILE_NAME = 'File name';
var CASE_GRID_STRING_CHANGE_ACCESS_RIGHTS = 'Change access rights';
var CASE_GRID_STRING_DOWNLOAD_DOCUMENT_AS_PDF = 'Download document';
var CASE_GRID_STRING_FILE_SIZE = 'File size';
var CASE_GRID_STRING_SUBMITTED_BY = 'Submitted by';
var CASE_GRID_STRING_CLICK_TO_EDIT = 'Click to edit...';

var CASE_ATTACHEMENT_LINK_STYLE_CLASS = 'casesBPMAttachmentDownloader';
var CASE_PDF_DOWNLOADER_LINK_STYLE_CLASS = 'casesBPMPDFGeneratorAndDownloader';

var GRID_WITH_SUBGRID_ID_PREFIX = '_tableForProcessInstanceGrid_';

var CASE_GRID_TOGGLERS_FILTER = 'div.casesListGridExpanderStyleClass';

function initializeCasesList(caseToOpenId) {
	/*
	DWREngine.setErrorHandler(function() {
		closeAllLoadingMessages();
		
		var loadingLabels = jQuery('div.loading');
		if (loadingLabels != null && loadingLabels.length > 0) {
			for (var i = 0; i < loadingLabels.length; i++) {
				jQuery(loadingLabels[i]).css('display', 'none');
			}
		}
		
		//	TODO: make some explanation text for user
	});
	*/
	
	var jQGridInclude = new JQGridInclude();
	jQGridInclude.SUBGRID = true;
	jqGridInclude(jQGridInclude);
	
	CasesEngine.getLocalizedStrings({
		callback: function(data) {
			setCasesListLocalizations(data);
			continueInitializeCasesList(caseToOpenId);
		}
	});
}

function setCasesListLocalizations(data) {
	if (data == null || data.length < 19) {
		return false;
	}
	
	CASE_GRID_STRING_CONTACT_NAME = data[0];
	CASE_GRID_STRING_SENDER = data[1];
	CASE_GRID_STRING_DATE = data[2];
	CASE_GRID_STRING_TAKEN_BY = data[3];
	CASE_GRID_STRING_EMAIL_ADDRESS = data[4];
	CASE_GRID_STRING_PHONE_NUMBER = data[5];
	CASE_GRID_STRING_ADDRESS = data[6];
	CASE_GRID_STRING_SUBJECT = data[7];
	CASE_GRID_STRING_FILE_NAME = data[8];
	CASE_GRID_STRING_CHANGE_ACCESS_RIGHTS = data[9];
	CASE_GRID_STRING_TASK_NAME = data[10];
	CASE_GRID_STRING_FORM_NAME = data[11];
	CASE_GRID_STRING_DOWNLOAD_DOCUMENT_AS_PDF = data[12];
	CASE_GRID_STRING_FILE_SIZE = data[13];
	CASE_GRID_STRING_SUBMITTED_BY = data[14];
	
	//	Other info
	CASE_ATTACHEMENT_LINK_STYLE_CLASS = data[15];
	CASE_PDF_DOWNLOADER_LINK_STYLE_CLASS = data[16];
	
	CASE_GRID_STRING_FILE_DESCRIPTION = data[17];
	CASE_GRID_STRING_CLICK_TO_EDIT = data[18];
}

function continueInitializeCasesList(caseToOpenId) {
	var togglers = jQuery(CASE_GRID_TOGGLERS_FILTER);
	if (togglers == null || togglers.length == 0) {
		return false;
	}
	
	var caseIdPar = 'caseid';
	var toggler = null;
	for (var i = 0; i < togglers.length; i++) {
		toggler = jQuery(togglers[i]);
		toggler.click(function(event) {
			registerGridExpanderActionsForElement(event, this);
		});
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
					tooltip:	CASE_GRID_STRING_CLICK_TO_EDIT
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
			showCustomerViewForCase(customerView, null);
			return false;
		}
		
		showLoadingMessage('');
		var caseId = caseToExpand.attr(caseIdPar);
		CasesEngine.getCaseManagerView(caseId, {
			callback: function(component) {
				
				closeAllLoadingMessages();
				
				if (component == null) {
					return false;
				}
				
				insertNodesToContainer(component, customerView[0]);
				customerView.addClass(classCaseWithInfo);
                showCustomerViewForCase(customerView, null);
				
				//showCustomerViewForCase(customerView, function() {initializeCaseGrids(caseId, customerView);});
			}
		});
	}
	else {
		customerView.hide('fast');
	}
}

/*
function openAllAttachmentsForCase(table) {
	if (table == null) {
		return false;
	}

	var subGridsOpeners = jQuery('td.subGridOpener', table);
	if (subGridsOpeners == null || subGridsOpeners.length == 0) {
		return false;
	}
	
	var subGridOpener = null;
	var parameterValue = null;
	var openOnLoadPar = 'opened_on_load';
	for (var j = 0; j < subGridsOpeners.length; j++) {
		subGridOpener = jQuery(subGridsOpeners[j]);
		
		parameterValue = subGridOpener.attr(openOnLoadPar);
		if (parameterValue == null || parameterValue == '') {
			subGridOpener.attr(openOnLoadPar, 'true');
			subGridOpener.click();
		}
	}
}


function initializeCaseGrids(caseId, customerView) {
	CasesEngine.getProcessInstanceId(caseId, {
		callback: function(piId) {
			if (piId == null) {
				return false;
			}
			
			BPMProcessAssets.hasUserRolesEditorRights(piId, {
				callback: function(hasRightChangeRights) {
					initTasksGrid(caseId, piId, customerView, false);	//	TODO: currently we are not managing access rights for tasks
					initFormsGrid(caseId, piId, customerView, hasRightChangeRights);
					initEmailsGrid(caseId, piId, customerView, hasRightChangeRights);
					initContactsGrid(piId, customerView, hasRightChangeRights);
				}
			});
		}
	});
}


function initContactsGrid(piId, customerView, hasRightChangeRights) {
	var identifier = 'caseContacts';
	
	var populatingFunction = function(params, callback) {
		params.piId = piId;
		
		BPMProcessAssets.getProcessContactsList(params, {
			callback: function(result) {
				callback(result);
				
				setStyleClassesForGridColumns(jQuery('div.' + identifier + 'Part'));
			}
		});
	};
	
	var namesForColumns = new Array();
	namesForColumns.push(CASE_GRID_STRING_CONTACT_NAME);
	namesForColumns.push(CASE_GRID_STRING_EMAIL_ADDRESS);
	namesForColumns.push(CASE_GRID_STRING_PHONE_NUMBER);
	namesForColumns.push(CASE_GRID_STRING_ADDRESS);
	var modelForColumns = new Array();
	modelForColumns.push({name:'name',index:'name'});
	modelForColumns.push({name:'emailAddress',index:'emailAddress'});
	modelForColumns.push({name:'phoneNumber',index:'phoneNumber'});
	modelForColumns.push({name:'address',index:'address'});
	
	var onSelectRowFunction = function(rowId) {
	}
	
	initCaseGrid(piId, customerView, identifier, populatingFunction, null, namesForColumns, modelForColumns, onSelectRowFunction, hasRightChangeRights);
}

function initEmailsGrid(caseId, piId, customerView, hasRightChangeRights) {
	var identifier = 'caseEmails';
	
	var populatingFunction = function(params, callback) {
		params.piId = piId;

		BPMProcessAssets.getProcessEmailsList(params, {
			callback: function(result) {
				callback(result);

				openAllAttachmentsForCase(jQuery('#' + params.identifier + GRID_WITH_SUBGRID_ID_PREFIX + piId));
				
				setStyleClassesForGridColumns(jQuery('div.' + identifier + 'Part'));
			}
		});
	};
	
	var subGridFunction = function(subgridId, rowId) {
		initFilesSubGridForCasesListGrid(subgridId, rowId, hasRightChangeRights, identifier);
	};
	
	var namesForColumns = new Array();
	namesForColumns.push(CASE_GRID_STRING_SUBJECT);
	namesForColumns.push(CASE_GRID_STRING_SENDER);
	namesForColumns.push(CASE_GRID_STRING_DATE);
	if (hasRightChangeRights) {
		namesForColumns.push(CASE_GRID_STRING_CHANGE_ACCESS_RIGHTS);
	}
	var modelForColumns = new Array();
	modelForColumns.push({name:'subject',index:'subject'});
	modelForColumns.push({name:'from',index:'from'});
	modelForColumns.push({name:'submittedDate',index:'submittedDate'});
	if (hasRightChangeRights) {
		modelForColumns.push({name:'rightsForEmailResources',index:'rightsForEmailResources'});
	}
	
	var onSelectRowFunction = function(rowId) {
		setBPMProcessForPreview(caseId, rowId);
	};
	
	initCaseGrid(piId, customerView, identifier, populatingFunction, subGridFunction, namesForColumns, modelForColumns, onSelectRowFunction, hasRightChangeRights);
}

function initFormsGrid(caseId, piId, customerView, hasRightChangeRights) {
	var identifier = 'caseForms';
	
	var populatingFunction = function(params, callback) {
		params.piId = piId;
		params.rightsChanger = hasRightChangeRights;
		BPMProcessAssets.getProcessDocumentsList(params, {
			callback: function(result) {
				callback(result);
				
				openAllAttachmentsForCase(jQuery('#' + params.identifier + GRID_WITH_SUBGRID_ID_PREFIX + piId));
				
				setStyleClassesForGridColumns(jQuery('div.' + identifier + 'Part'));
			}
		});
	};

	var subGridFunction = function(subgridId, rowId) {
		initFilesSubGridForCasesListGrid(subgridId, rowId, hasRightChangeRights, identifier);
	};
	
    var namesForColumns = new Array();
    namesForColumns.push(CASE_GRID_STRING_FORM_NAME);
    namesForColumns.push(CASE_GRID_STRING_SUBMITTED_BY);
	namesForColumns.push(CASE_GRID_STRING_DATE);
	namesForColumns.push(CASE_GRID_STRING_DOWNLOAD_DOCUMENT_AS_PDF);	//	TODO: check if need to download document in PDF
	if (hasRightChangeRights) {
		namesForColumns.push(CASE_GRID_STRING_CHANGE_ACCESS_RIGHTS);
	}
	var modelForColumns = new Array();
	modelForColumns.push({name:'name',index:'name'});
	modelForColumns.push({name:'submittedByName',index:'submittedByName'});
	modelForColumns.push({name:'submittedDate',index:'submittedDate'});
	modelForColumns.push({name:'downloadAsPdf',index:'downloadAsPdf'});
	if (hasRightChangeRights) {
		modelForColumns.push({name:'rightsForDocumentResources',index:'rightsForDocumentResources'});
	}
	
	var onSelectRowFunction = function(rowId) {
		setBPMProcessForPreview(caseId, rowId);
	};
	
	initCaseGrid(piId, customerView, identifier, populatingFunction, subGridFunction, namesForColumns, modelForColumns, onSelectRowFunction, hasRightChangeRights);
}

function initTasksGrid(caseId, piId, customerView, hasRightChangeRights) {
	var identifier = 'caseTasks';
	
	var populatingFunction = function(params, callback) {
		params.piId = piId;
		
		BPMProcessAssets.getProcessTasksList(params, {
			callback: function(result) {
				callback(result);
				
				setStyleClassesForGridColumns(jQuery('div.' + identifier + 'Part'));
			}
		});
	};
	
	var namesForColumns = new Array();
	namesForColumns.push(CASE_GRID_STRING_TASK_NAME);
	namesForColumns.push(CASE_GRID_STRING_DATE);
	namesForColumns.push(CASE_GRID_STRING_TAKEN_BY);
	if (hasRightChangeRights) {
		namesForColumns.push(CASE_GRID_STRING_CHANGE_ACCESS_RIGHTS);
	}
	var modelForColumns = new Array();
	modelForColumns.push({name:'name',index:'name'});
	modelForColumns.push({name:'createdDate',index:'createdDate'});
	modelForColumns.push({name:'takenBy',index:'takenBy'});
	if (hasRightChangeRights) {
		modelForColumns.push({name:'rightsForTaskResources',index:'rightsForTaskResources'});
	}
	
	var onSelectRowFunction = function(rowId) {
		setBPMProcessForPreview(caseId, rowId);
	};
	
	initCaseGrid(piId, customerView, identifier, populatingFunction, null, namesForColumns, modelForColumns, onSelectRowFunction, hasRightChangeRights);
}

function initCaseGrid(piId, customerView, tableClassName, populatingFunction, subGridForThisGrid, namesForColumns, modelForColumns, onSelectRowFunction, rightsChanger) {
	var params = new JQGridParams();
	
	params.identifier = tableClassName;
	params.rightsChanger = rightsChanger;
	
	params.populateFromFunction = populatingFunction;
	
	params.colNames = namesForColumns;
	params.colModel = modelForColumns;
	
	if (onSelectRowFunction != null) {
		params.onSelectRow = onSelectRowFunction;
	}
	
	var tables = getElementsByClassName(customerView[0], 'table', tableClassName);
	if (tables == null || tables.length == 0) {
		return false;
	}
	var table = tables[0];
	
	if (subGridForThisGrid == null) {
		params.subGridRowExpanded = null;
	}
	else {
		jQuery(table).attr('id', params.identifier + GRID_WITH_SUBGRID_ID_PREFIX + piId);
		params.subGrid = true;
		params.subGridRowExpanded = subGridForThisGrid;
	}
	
	var grid = new JQGrid();
	grid.createGrid(table, params);
	
	jQuery(table).addClass('scroll');
	jQuery(table).attr('cellpadding', 0);
	jQuery(table).attr('cellspacing', 0);
}

function initFilesSubGridForCasesListGrid(subgridId, rowId, hasRightChangeRights, identifier) {
	var subgridTableId = subgridId + '_t';
	jQuery('#' + subgridId).html('<table id=\''+subgridTableId+'\' class=\'scroll subGrid\' cellpadding=\'0\' cellspacing=\'0\'></table>');

	var subGridParams = new JQGridParams();
	subGridParams.rightsChanger = hasRightChangeRights;
	subGridParams.identifier = identifier +'Attachments';
	subGridParams.populateFromFunction = function(params, callback) {
		params.taskId = rowId;
		BPMProcessAssets.getTaskAttachments(params, {
			callback: function(result) {
				callback(result);
				
				var subGridTable = jQuery('#' + subgridTableId);
				if (subGridTable == null || subGridTable.length == 0) {
					return false;
				}
				
				setStyleClassesForGridColumns(subGridTable.parent().parent());
				
				if (result != null) {
					return false;
				}
				
				var tagName = 'TR';
				var className = 'subgrid';
				var fileGridRow = null;
				var foundRow = false;
				var parentElement = subGridTable;
				var tempParentElement = null;
				while (parentElement != null && !foundRow) {
					tempParentElement = parentElement.get(0);
					if (tempParentElement.tagName == tagName && jQuery(tempParentElement).hasClass(className)) {
						fileGridRow = tempParentElement;
						foundRow = true;
					}
					parentElement = parentElement.parent();
				}
				if (fileGridRow != null) {
					jQuery(fileGridRow).css('display', 'none');
					
					var mainRow = jQuery('#' + rowId);
					if (mainRow == null || mainRow.length == 0) {
						return false;
					}
					
					var subGridOpener = jQuery('td.subGridOpener', mainRow);
					if (subGridOpener == null || subGridOpener.length == 0) {
						return false;
					}
					subGridOpener.empty();
					subGridOpener.unbind('click');
				}
			}
		});
	};

	var namesForColumns = new Array();
	namesForColumns.push(CASE_GRID_STRING_FILE_DESCRIPTION);
	namesForColumns.push(CASE_GRID_STRING_FILE_NAME);
	namesForColumns.push(CASE_GRID_STRING_FILE_SIZE);
	if (subGridParams.rightsChanger) {
		namesForColumns.push(CASE_GRID_STRING_CHANGE_ACCESS_RIGHTS);
	}
	subGridParams.colNames = namesForColumns;
	
	var modelForColumns = new Array();
	modelForColumns.push({name:'description',index:'description'});
	modelForColumns.push({name:'name',index:'name'});
	modelForColumns.push({name:'fileSize',index:'fileSize'});
	if (subGridParams.rightsChanger) {
		modelForColumns.push({name:'rightsForAttachment',index:'rightsForAttachment'});
	}
	subGridParams.colModel = modelForColumns;
	
	subGridParams.onSelectRow = function(fileRowId) {
		var uri = '&taskInstanceId=' + rowId + '&varHash=' + fileRowId;
		setCurrentWindowToDownloadCaseResource(uri, CASE_ATTACHEMENT_LINK_STYLE_CLASS);
	};

	var subgrid = new JQGrid();
	subgrid.createGrid("#"+subgridTableId, subGridParams);
}

function setStyleClassesForGridColumns(elements) {
	if (elements == null || elements.length == 0) {
		return false;
	}
	
	var attributeName = 'col_with_classes';
	var attribute = null;
	var element = null;
	var grids = null;
	var grid = null;
	var rows = null;
	var row = null;
	for (var i = 0; i < elements.length; i++) {
		element = jQuery(elements[i]);
		attribute = element.attr(attributeName);
		if (attribute == null || attribute == '') {
			element.attr(attributeName, 'true');
			
			grids = jQuery('table', element);
			if (grids != null && grids.length > 0) {
				for (var j = 0; j < grids.length; j++) {
					grid = jQuery(grids[j]);
					
					rows = jQuery('tr', grid);
					if (rows != null && rows.length > 0) {
						for (var k = 0; k < rows.length; k++) {
							row = jQuery(rows[k]);
							
							var headerCells = jQuery('th', row);
							if (headerCells != null && headerCells.length > 0) {
								for (var l = 0; l < headerCells.length; l++) {
									jQuery(headerCells[l]).addClass('casesGridHeaderCell_' + l);
								}
							}
							
							var bodyCells = jQuery('td', row);
							if (bodyCells != null && bodyCells.length > 0) {
								for (var l = 0; l < bodyCells.length; l++) {
									jQuery(bodyCells[l]).addClass('casesGridBodyCell_' + l);
								}
							}
						}
					}
				}
			}
		}
	}
}

function setBPMProcessForPreview(caseId, taskInstanceId) {
	changeWindowLocationHref('prm_case_pk=' + caseId + '&tiId=' + taskInstanceId + '&cp_prm_action=8');
}

function downloadCaseDocument(event, taskId) {
	var uri = '&taskInstanceId=' + taskId;
	setCurrentWindowToDownloadCaseResource(uri, CASE_PDF_DOWNLOADER_LINK_STYLE_CLASS);
	
	if (event) {
		if (event.stopPropagation) {
			event.stopPropagation();
		}
		event.cancelBubble = true;
	}
}

function setCurrentWindowToDownloadCaseResource(uri, styleClass) {
	var links = jQuery('a.' + styleClass);
	if (links == null || links.length == 0) {
		return false;
	}
	
	if (uri == null || uri == '') {
		return false;
	}
	
	var linkHref = jQuery(links[0]).attr('href');
	if (linkHref == null) {
		return false;
	}
	
	linkHref += uri;
	window.location.href = linkHref;
	return true;
}

*/

function showCustomerViewForCase(component, callbackFunction) {
	var displayProperty = 'fast';
	if (callbackFunction == null) {
		jQuery(component).show(displayProperty);
	}
	else {
		jQuery(component).show(displayProperty, function() {
			callbackFunction();
		});
	}
}

function changeAccessRightsForBpmRelatedResource(event, processId, taskId, id, fileHashValue, setSameRightsForAttachments) {	
	var element = jQuery('#' + id);
	if (element == null || event == null) {
		return false;
	}
	
	var offsets = element.offset();
	if (offsets == null) {
		return false;
	}
	
	var xCoord = offsets.left;
	var yCoord = offsets.top;
	
	if (event) {
		if (event.stopPropagation) {
			event.stopPropagation();
		}
		event.cancelBubble = true;
	}
	
	var rightsBoxId = 'caseProcessResourceAccessRightsSetterBox';
	var rightsBox = jQuery('#' + rightsBoxId);
	if (rightsBox == null || rightsBox.length == 0) {
		var htmlForBox = '<div id=\''+rightsBoxId+'\' class=\'caseProcessResourceAccessRightsSetterStyle\' />';
		jQuery(document.body).append(htmlForBox);
		rightsBox = jQuery('#' + rightsBoxId);
	}
	else {
		rightsBox.empty();
	}
	rightsBox.css('top', yCoord + 'px');
	rightsBox.css('left', xCoord + 'px');
	
	BPMProcessAssets.getAccessRightsSetterBox(processId, taskId, fileHashValue, setSameRightsForAttachments, {
		callback: function(component) {
			if (component == null) {
				return false;
			}
			
			insertNodesToContainer(component, rightsBox[0]);
			showCustomerViewForCase(rightsBox, null);
		}
	});
}

function setAccessRightsForBpmRelatedResource(id, processId, taskInstanceId, fileHashValue, sameRightsSetterId) {
	var element = document.getElementById(id);
	if (element == null) {
		return false;
	}
	
	var canAccess = element.checked;
	var setSameRightsForAttachments = false;
	if (sameRightsSetterId != null) {
		var sameRightsSetter = document.getElementById(sameRightsSetterId);
		if (sameRightsSetter != null) {
			setSameRightsForAttachments = sameRightsSetter.checked;
		}
	}
	
	BPMProcessAssets.setAccessRightsForProcessResource(element.name, taskInstanceId, fileHashValue, canAccess, setSameRightsForAttachments);
}

function closeAccessRightsSetterBox() {
	var rightsBoxId = 'caseProcessResourceAccessRightsSetterBox';
	var rightsBox = jQuery('#' + rightsBoxId);
	if (rightsBox == null || rightsBox.length == 0) {
		return false;
	}
	
	rightsBox.hide('fast');
}

function takeCurrentProcessTask(event, taskInstanceId, id, allowReAssign) {
	if (event) {
		if (event.stopPropagation) {
			event.stopPropagation();
		}
		event.cancelBubble = true;
	}
	
	CasesEngine.takeBPMProcessTask(taskInstanceId, allowReAssign, {
		callback: function(takenByValue) {
			if (takenByValue == null) {
				return false;
			}
		
			jQuery('#' + id).parent().empty().text(takenByValue);
		}
	});
}
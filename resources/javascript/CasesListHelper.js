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
var CASE_GRID_STRING_FILE_NAME = 'File name';
var CASE_GRID_STRING_CHANGE_ACCESS_RIGHTS = 'Change access rights';
var CASE_GRID_STRING_DOWNLOAD_DOCUMENT_AS_PDF = 'Download document';
var CASE_GRID_STRING_FILE_SIZE = 'File size';
var CASE_GRID_STRING_SUBMITTED_BY = 'Submitted by';

var CASE_ATTACHEMENT_LINK_STYLE_CLASS = 'casesBPMAttachmentDownloader';
var CASE_PDF_DOWNLOADER_LINK_STYLE_CLASS = 'casesBPMPDFGeneratorAndDownloader';

function initializeCasesList() {
	/*DWREngine.setErrorHandler(function() {	//	TODO: uncomment
		closeAllLoadingMessages();
		//	TODO: close 'Loading...' for grids
	});*/
	
	var jQGridInclude = new JQGridInclude();
	jQGridInclude.SUBGRID = true;
	jqGridInclude(jQGridInclude);
	
	CasesEngine.getLocalizedStrings({
		callback: function(data) {
			setCasesListLocalizations(data);
			continueInitializeCasesList();
		}
	});
}

function setCasesListLocalizations(data) {
	if (data == null || data.length < 17) {
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
}

function continueInitializeCasesList() {
	var togglers = jQuery('div.casesListBodyContainerItemToggler');
	if (togglers == null || togglers.length == 0) {
		return false;
	}
	
	var toggler = null;
	var classExpanded = 'expanded';
	var classCaseWithInfo = 'caseWithInfo';
	for (var i = 0; i < togglers.length; i++) {
		toggler = jQuery(togglers[i]);
		toggler.click(function() {
			var caseToExpand = jQuery(this);
			var show = false;
			if (caseToExpand.hasClass(classExpanded)) {
				caseToExpand.removeClass(classExpanded);
			}
			else {
				caseToExpand.addClass(classExpanded);
				show = true;
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
				var caseId = caseToExpand.attr('caseid');
				CasesEngine.getInfoForCase(caseId, {
					callback: function(component) {
						closeAllLoadingMessages();
						
						if (component == null) {
							return false;
						}
						
						insertNodesToContainer(component, customerView[0]);
						customerView.addClass(classCaseWithInfo);
						
						showCustomerViewForCase(customerView, function() {initializeCaseGrids(caseId, customerView);});
					}
				});
			}
			else {
				customerView.hide('fast');
			}
		});
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
					initTasksGrid(caseId, piId, customerView, hasRightChangeRights);
					initFormsGrid(caseId, piId, customerView, hasRightChangeRights);
					initEmailsGrid(caseId, piId, customerView, hasRightChangeRights);
					initContactsGrid(piId, customerView, hasRightChangeRights);
				}
			});
		}
	});
}

function initContactsGrid(piId, customerView, hasRightChangeRights) {
	var populatingFunction = function(params, callback) {
		params.piId = piId;
		
		BPMProcessAssets.getProcessContactsList(params, {
			callback: function(result) {
				callback(result);
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
	
	initCaseGrid(piId, customerView, 'caseContacts', populatingFunction, null, namesForColumns, modelForColumns, onSelectRowFunction, hasRightChangeRights);
}

function initEmailsGrid(caseId, piId, customerView, hasRightChangeRights) {
	var populatingFunction = function(params, callback) {
		params.piId = piId;

		BPMProcessAssets.getProcessEmailsList(params, {
			callback: function(result) {
				callback(result);
			}
		});
	};
	
	var subGridFunction = function(subgridId, rowId) {
		initFilesSubGridForCasesListGrid(subgridId, rowId, hasRightChangeRights);
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
	
	initCaseGrid(piId, customerView, 'caseEmails', populatingFunction, subGridFunction, namesForColumns, modelForColumns, onSelectRowFunction, hasRightChangeRights);
}

function initFormsGrid(caseId, piId, customerView, hasRightChangeRights) {
	var populatingFunction = function(params, callback) {
		params.piId = piId;
		params.rightsChanger = hasRightChangeRights;
		BPMProcessAssets.getProcessDocumentsList(params, {
			callback: function(result) {
				callback(result);
			}
		});
	};

	var subGridFunction = function(subgridId, rowId) {
		initFilesSubGridForCasesListGrid(subgridId, rowId, hasRightChangeRights);
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
	
	initCaseGrid(piId, customerView, 'caseForms', populatingFunction, subGridFunction, namesForColumns, modelForColumns, onSelectRowFunction, hasRightChangeRights);
}

function initTasksGrid(caseId, piId, customerView, hasRightChangeRights) {
	var populatingFunction = function(params, callback) {
		params.piId = piId;
		
		BPMProcessAssets.getProcessTasksList(params, {
			callback: function(result) {
				callback(result);
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
	
	initCaseGrid(piId, customerView, 'caseTasks', populatingFunction, null, namesForColumns, modelForColumns, onSelectRowFunction, hasRightChangeRights);
}

function setBPMProcessForPreview(caseId, taskInstanceId) {
	changeWindowLocationHref('prm_case_pk=' + caseId + '&taskInstanceId=' + taskInstanceId + '&cp_prm_action=8');
}

function initCaseGrid(piId, customerView, tableClassName, populatingFunction, subGridForThisGrid, namesForColumns, modelForColumns, onSelectRowFunction, rightsChanger) {
	var params = new JQGridParams();
	
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
		jQuery(table).attr('id', piId);
		params.subGrid = true;
		params.subGridRowExpanded = subGridForThisGrid;
	}
	
	var grid = new JQGrid();
	grid.createGrid(table, params);
	
	jQuery(table).addClass('scroll');
	jQuery(table).attr('cellpadding', 0);
	jQuery(table).attr('cellspacing', 0);
}

function initFilesSubGridForCasesListGrid(subgridId, rowId, hasRightChangeRights) {
	var subgridTableId = subgridId + '_t';
	jQuery('#' + subgridId).html('<table id=\''+subgridTableId+'\' class=\'scroll subGrid\' cellpadding=\'0\' cellspacing=\'0\'></table>');

	var subGridParams = new JQGridParams();
	subGridParams.rightsChanger = hasRightChangeRights;
	subGridParams.populateFromFunction = function(params, callback) {
		params.taskId = rowId;
		BPMProcessAssets.getTaskAttachments(params, {
			callback: function(result) {
				callback(result);
			}
		});
       };

	var namesForColumns = new Array();
	namesForColumns.push(CASE_GRID_STRING_FILE_NAME);
	namesForColumns.push(CASE_GRID_STRING_FILE_SIZE);
	if (subGridParams.rightsChanger) {
		namesForColumns.push(CASE_GRID_STRING_CHANGE_ACCESS_RIGHTS);
	}
	subGridParams.colNames = namesForColumns;
	
	var modelForColumns = new Array();
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

function downloadCaseDocument(taskId) {
	var uri = '&taskInstnaceId=' + taskId;
	setCurrentWindowToDownloadCaseResource(uri, CASE_PDF_DOWNLOADER_LINK_STYLE_CLASS);
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

function showCustomerViewForCase(component, callbackFunction) {
	var displayProperty = 'fast';
	if (callbackFunction == null) {
		component.show(displayProperty);
	}
	else {
		component.show(displayProperty, function() {
			callbackFunction();
		});
	}
}

function changeAccessRightsForBpmRelatedResource(event, processId, taskId, id, variableName, setSameRightsForAttachments) {	
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
		alert('stop prop');
		//event.stopPropagation();
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
	
	BPMProcessAssets.getAccessRightsSetterBox(processId, taskId, variableName, setSameRightsForAttachments, {
		callback: function(component) {
			if (component == null) {
				return false;
			}
			
			insertNodesToContainer(component, rightsBox[0]);
			showCustomerViewForCase(rightsBox, null);
		}
	});
}

function setAccessRightsForBpmRelatedResource(id, processId, taskInstanceId, variableName, sameRightsSetterId) {
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
	
	BPMProcessAssets.setAccessRightsForProcessResource(element.name, taskInstanceId, variableName, canAccess, setSameRightsForAttachments);
}

function closeAccessRightsSetterBox() {
	var rightsBoxId = 'caseProcessResourceAccessRightsSetterBox';
	var rightsBox = jQuery('#' + rightsBoxId);
	if (rightsBox == null || rightsBox.length == 0) {
		return false;
	}
	
	rightsBox.hide('fast');
}
/*
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
*/
var CASE_GRID_STRING_CLICK_TO_EDIT = 'Click to edit...';

/*
var CASE_ATTACHEMENT_LINK_STYLE_CLASS = 'casesBPMAttachmentDownloader';
var CASE_PDF_DOWNLOADER_LINK_STYLE_CLASS = 'casesBPMPDFGeneratorAndDownloader';

var GRID_WITH_SUBGRID_ID_PREFIX = '_tableForProcessInstanceGrid_';
*/
var CASE_GRID_TOGGLERS_FILTER = 'div.casesListGridExpanderStyleClass';

function initializeCasesList(caseToOpenId) {
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
	
	var jQGridInclude = new JQGridInclude();
	jQGridInclude.SUBGRID = true;
	jqGridInclude(jQGridInclude);
	
	CasesEngine.getLocalizedStrings({
		callback: function(data) {
			//setCasesListLocalizations(data);
			continueInitializeCasesList(caseToOpenId);
		}
	});
}

/*
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
* */ 

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
			jQuery(customerView).show('fast');
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
                jQuery(customerView).show('fast');
			}
		});
	}
	else {
		customerView.hide('fast');
	}
}
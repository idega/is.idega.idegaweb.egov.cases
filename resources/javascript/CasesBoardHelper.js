if (CasesBoardHelper == null) var CasesBoardHelper = {};

CasesBoardHelper.localizations = {
	savingMessage:	'Saving...',
	remove:			'Remove',
	edit:			'Edit',
	loading:		'Loading...'
};

CasesBoardHelper.linkInAction = null;
CasesBoardHelper.pressedKeyboardButton = null;
CasesBoardHelper.pressedMouseRightClickButton = false;

CasesBoardHelper.initializeBoardCases = function(localizations) {
	CasesBoardHelper.linkInAction = null;
	CasesBoardHelper.localizations = localizations;
	
	jQuery(document).bind('keydown', function(event) {
		CasesBoardHelper.pressedKeyboardButton = event.keyCode;
	});
	jQuery(document).bind('keyup', function(event) {
		CasesBoardHelper.pressedKeyboardButton = null;
	});
	
	jQuery.each(jQuery('a.casesBoardViewerTableLinkToTaskStyle'), function() {
		var link = jQuery(this);
		
		link.click(function(event) {
			if (event && event.button && event.button == 2) {
				CasesBoardHelper.pressedMouseRightClickButton = true;
			}
			
			if (CasesBoardHelper.linkInAction == this.id) {
				return false;
			}
			
			showLoadingMessage(CasesBoardHelper.localizations.loading);
			CasesBoardHelper.linkInAction = this.id;
			if (CasesBoardHelper.isLinkToBeOpenedInNewTab()) {
				var id = window.setTimeout(function() {
					window.clearTimeout(id);
					closeAllLoadingMessages();
				}, 3000);
			}
		});
		link.dblclick(function() {
			return false;
		});
	});
	
	jQuery.each(jQuery('td.casesBoardViewerTableEditableCellselect'), function() {
		CasesBoardHelper.initializeEditableCell(jQuery(this), {
			data:		"{'no_value': '"+CasesBoardHelper.localizations.remove+"', 'A': 'A', 'B': 'B', 'C': 'C'}",
			type:		'select',
			rerender:	false
		}, 'select');
	});
	
	jQuery.each(jQuery('td.casesBoardViewerTableEditableCelltextinput'), function() {
		CasesBoardHelper.initializeEditableCell(jQuery(this), {
			rerender:	false,
			recount:	true
		}, 'textinput');
	});
	
	jQuery.each(jQuery('td.casesBoardViewerTableEditableCelltextarea'), function() {
		CasesBoardHelper.initializeEditableCell(jQuery(this), {
			type:		'textarea',
			rerender:	false
		}, 'textarea');
	});
}

CasesBoardHelper.isLinkToBeOpenedInNewTab = function() {
	if (CasesBoardHelper.pressedKeyboardButton == 17 || CasesBoardHelper.pressedKeyboardButton == 91 || CasesBoardHelper.pressedKeyboardButton == 224) {
		//	Control or Command button is pressed also!
		return true;
	}
	
	if (CasesBoardHelper.pressedMouseRightClickButton) {
		return true;
	}
	
	return false;
}

CasesBoardHelper.initializeEditableCell = function(cell, settings, type) {
	if (cell.hasClass('casesBoardViewerTableEditableCellInitialized')) {
		return;
	}

	var container = cell.parent().parent().parent().parent();
	var rowId = cell.parent().attr('id');
	settings.caseId = rowId.replace('uniqueCaseId', '');
	settings.variable =  CasesBoardHelper.getValueFromHiddenInput('input[type=\'hidden\'][name=\'casesBoardViewerTableEditableCell'+type+'VariableName\']',
		container);
	settings.role =  CasesBoardHelper.getValueFromHiddenInput('input[type=\'hidden\'][name=\'casesBoardViewerTableRoleKey\']', container);
	
	settings.uuid =  CasesBoardHelper.getValueFromHiddenInput('input[type=\'hidden\'][name=\'casesBoardViewerTableUniqueIdKey\']', container);
	settings.container = CasesBoardHelper.getValueFromHiddenInput('input[type=\'hidden\'][name=\'casesBoardViewerTableContainerKey\']', container);
	settings.totalBoardAmountCellId = CasesBoardHelper.getValueFromHiddenInput('input[type=\'hidden\'][name=\'casesBoardViewerTableTotalBoardAmountCellIdKey\']',
		container);
	settings.backPage = CasesBoardHelper.getValueFromHiddenInput('input[type=\'hidden\'][name=\'casesBoardViewerTableSpecialBackPageFromTaskViewer\']',
		container);
	
	settings.placeholder = '';
	settings.tooltip = CasesBoardHelper.localizations.edit;
	settings.onblur	= 'submit';
	settings.previousValue = cell.text();
	
	cell.editable(function(value, settings) {
		var editableElement = jQuery(this);
		
		showLoadingMessage(CasesBoardHelper.localizations.savingMessage);
		BoardCasesManager.setCaseVariableValue(settings.caseId, settings.variable, value, settings.role, settings.backPage, {
			callback: function(result) {
				CasesBoardHelper.closeEditableField(editableElement, result);
				
				if (result != null && settings.rerender) {
					IWCORE.renderComponent(settings.uuid, settings.container, function() {
						closeAllLoadingMessages();
					}, null);
					return;
				}
				
				if (result != null && settings.recount) {
					var previousValue = settings.previousValue;
					previousValue++;
					previousValue--;
					
					var totalSum = jQuery('#' + settings.totalBoardAmountCellId).text();
					totalSum++;
					totalSum--;
					var newValue = result.id;
					newValue++;
					newValue--;
					
					totalSum = totalSum - previousValue + newValue;
					jQuery('#' + settings.totalBoardAmountCellId).text(totalSum);
					settings.previousValue = newValue;
				}
				
				if (result != null) {
					jQuery('a.casesBoardViewerTableLinkToTaskStyle', editableElement.parent()).attr('href', result.value);
				}
				
				closeAllLoadingMessages();
			}, errorHandler: function() {
				closeAllLoadingMessages();
				CasesBoardHelper.closeEditableField(editableElement, result);
			}
		});
	}, settings);
	
	cell.addClass('casesBoardViewerTableEditableCellInitialized');
}

CasesBoardHelper.getValueFromHiddenInput = function(filter, container) {
	var objects = jQuery(filter, container);
	if (objects == null || objects.length == 0) {
		return null;
	}
	
	return jQuery(objects[objects.length -1]).attr('value');
}

CasesBoardHelper.closeEditableField = function(field, value) {
	field.empty().text(value == null ? '': value.id);
}
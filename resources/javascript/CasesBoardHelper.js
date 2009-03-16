if (CasesBoardHelper == null) var CasesBoardHelper = {};

CasesBoardHelper.localizations = {
	savingMessage:	'Saving...',
	remove:			'Remove',
	edit:			'Edit'
};

CasesBoardHelper.initializeBoardCases = function(localizations) {
	CasesBoardHelper.localizations = localizations;
	
	jQuery.each(jQuery('td.casesBoardViewerTableEditableCellselect'), function() {
		CasesBoardHelper.initializeEditableCell(jQuery(this), {
			data:		"{'no_value': '"+CasesBoardHelper.localizations.remove+"', 'A': 'A', 'B': 'B', 'C': 'C'}",
			type:		'select',
			rerender:	true
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

CasesBoardHelper.initializeEditableCell = function(cell, settings, type) {
	if (cell.hasClass('casesBoardViewerTableEditableCellInitialized')) {
		return;
	}

	var container = cell.parent().parent().parent().parent();
	var rowId = cell.parent().attr('id');
	settings.caseId = rowId.replace('uniqueCaseId', '');
	settings.variable =  CasesBoardHelper.getValueFromHiddenInput('input[type=\'hidden\'][name=\'casesBoardViewerTableEditableCell'+type+'VariableName\']', container);
	settings.role =  CasesBoardHelper.getValueFromHiddenInput('input[type=\'hidden\'][name=\'casesBoardViewerTableRoleKey\']', container);
	
	settings.uuid =  CasesBoardHelper.getValueFromHiddenInput('input[type=\'hidden\'][name=\'casesBoardViewerTableUniqueIdKey\']', container);
	settings.container = CasesBoardHelper.getValueFromHiddenInput('input[type=\'hidden\'][name=\'casesBoardViewerTableContainerKey\']', container);
	settings.totalBoardAmountCellId = CasesBoardHelper.getValueFromHiddenInput('input[type=\'hidden\'][name=\'casesBoardViewerTableTotalBoardAmountCellIdKey\']',
		container);
	
	settings.placeholder = '';
	settings.tooltip = CasesBoardHelper.localizations.edit;
	settings.onblur	= 'submit';
	settings.previousValue = cell.text();
	
	cell.editable(function(value, settings) {
		var editableElement = jQuery(this);
		
		showLoadingMessage(CasesBoardHelper.localizations.savingMessage);
		BoardCasesManager.setCaseVariableValue(settings.caseId, settings.variable, value, settings.role, {
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
					result++;
					result--;
					
					totalSum = totalSum - previousValue + result;
					jQuery('#' + settings.totalBoardAmountCellId).text(totalSum);
					settings.previousValue = result;
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
	field.empty().text(value == null ? '': value);
}

CasesBoardHelper.getCaseVariableValueInput = function(cellId) {
}
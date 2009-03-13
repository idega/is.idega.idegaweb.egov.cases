if (CasesBoardHelper == null) var CasesBoardHelper = {};

CasesBoardHelper.localizations = {
	savingMessage: 'Saving...',
	select:			'Select'
};

CasesBoardHelper.initializeBoardCases = function(localizations) {
	CasesBoardHelper.localizations = localizations;
	
	jQuery.each(jQuery('td.casesBoardViewerTableEditableCellselect'), function() {
		CasesBoardHelper.initializeEditableCell(jQuery(this), {
			data:		"{'no_value': '"+CasesBoardHelper.localizations.select+"', 'A': 'A', 'B': 'B', 'C': 'C'}",
			type:		'select',
			caseId:		'',
			variable:	'',
			tooltip:	CasesBoardHelper.localizations.select,
			onblur:	'submit'
		}, 'select');
	});
	
	jQuery.each(jQuery('td.casesBoardViewerTableEditableCelltextinput'), function() {
		CasesBoardHelper.initializeEditableCell(jQuery(this), {
			caseId:		'',
			variable:	'',
			tooltip:	'',
			onblur:		'submit'
		}, 'textinput');
	});
	
	jQuery.each(jQuery('td.casesBoardViewerTableEditableCelltextarea'), function() {
		CasesBoardHelper.initializeEditableCell(jQuery(this), {
			type:		'textarea',
			caseId:		'',
			variable:	'',
			tooltip:	'',
			onblur:		'submit'
		}, 'textarea');
	});
}

CasesBoardHelper.initializeEditableCell = function(cell, settings, type) {
	if (cell.hasClass('casesBoardViewerTableEditableCellInitialized')) {
		return;
	}

	settings.caseId = jQuery('input[type=\'hidden\'][name=\'casesBoardViewerTableEditableCellCaseId\']', cell.parent()).attr('value');
	settings.variable = jQuery('input[type=\'hidden\'][name=\'casesBoardViewerTableEditableCell'+type+'VariableName\']').attr('value');
	
	cell.editable(function(value, settings) {
		var editableElement = jQuery(this);
		
		showLoadingMessage(CasesBoardHelper.localizations.savingMessage);
		BoardCasesManager.setCaseVariableValue(settings.caseId, settings.variable, value, {
			callback: function(result) {
				closeAllLoadingMessages();
				
				editableElement.empty().text(result == null ? '': result);
			}
		});
	}, settings);
	
	cell.addClass('casesBoardViewerTableEditableCellInitialized');
}

CasesBoardHelper.getCaseVariableValueInput = function(cellId) {
}
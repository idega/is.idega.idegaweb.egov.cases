if (CasesBoardHelper == null) var CasesBoardHelper = {};

CasesBoardHelper.localizations = {
	savingMessage: 'Saving...',
	select:			'Select'
};

CasesBoardHelper.initializeBoardCases = function(localizations) {
	CasesBoardHelper.localizations = localizations;
	
	jQuery.each(jQuery('td.casesBoardViewerTableEditableCellselect'), function() {
		CasesBoardHelper.initializeEditableCell(jQuery(this), 'select', "{'no_value': '"+CasesBoardHelper.localizations.select+"', 'A': 'A', 'B': 'B', 'C': 'C'}");
	});
	
}

CasesBoardHelper.initializeEditableCell = function(cell, type, data) {
	if (cell.hasClass('casesBoardViewerTableEditableCellInitialized')) {
		return;
	}

	var caseId = jQuery('input[type=\'hidden\'][name=\'casesBoardViewerTableEditableCellCaseId\']', cell.parent()).attr('value');
	var variableName = jQuery('input[type=\'hidden\'][name=\'casesBoardViewerTableEditableCell'+type+'VariableName\']').attr('value');
	
	cell.editable(function() {
	}, {
		data:		data,
		type:		type,
		caseId:		caseId,
		variable:	variableName,
		onchange:	'submit'
	});
	cell.addClass('casesBoardViewerTableEditableCellInitialized');
}

CasesBoardHelper.getCaseVariableValueInput = function(cellId) {
}
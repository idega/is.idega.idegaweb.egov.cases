var $j = jQuery.noConflict();

$j(document).ready(function() {
	CasesBusiness.useTypes({
		callback: function(useTypes) {
			var typeColumn = useTypes ? 7 : 6;
			$j('#casesFetcher').tablesorter(
					{
						headers: {
							1: { sorter: false },
							typeColumn: { sorter: false }
						},
						sortList: [[0,1]]
					}
			);
		}
	});	
});
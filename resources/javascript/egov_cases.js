var $j = jQuery.noConflict();

$j(document).ready(function() {
	$j('select#casesParentCategory').change(function() {
		var locale = $('input#casesLocale').val();
		var value = $j(this).val();
		if (value.length > 0) {
			CasesBusiness.getAllSubCategories(value, locale, {
				callback: function(results) {
					$j('select#casesSubCategory').each(function(item) {
						dwr.util.removeAllOptions(item);
						dwr.util.addOptions(item, results);
					});
				}
			});
		}
	});
});
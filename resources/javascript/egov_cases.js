function initializeCaseCategoryDropdowns() {
	$('prm_instruments_1').addEvent('change', function(e){
		var season = null;
		if($('prm_season') != null) {
			season = $('prm_season').value;
		}
		MusicSchoolBusiness.filterMusicSchoolsByInstrumentChoice(dwr.util.getValue('prm_instruments_1'), season, {
			callback: function(result) {
				$ES('select.musicSchoolDropdown').each(function(item) {
					dwr.util.removeAllOptions(item);
					dwr.util.addOptions(item, result, 'id', 'value');
				});
			}
		});
	});
	$ES('select.musicSchoolDropdown').each(function(item) {
		item.addEvent('change', function(e){
			if (item.id == null) {
				return false;
			}
			
			var season = null;
			if($('prm_season') != null) {
				season = $('prm_season').value;
			}
			var selectedSchool = dwr.util.getValue(item.id);
			var instrument = $('prm_instruments_1').value;
			MusicSchoolBusiness.filterMusicSchoolsByInstrumentChoiceAndSchool(instrument, selectedSchool, season, {
				callback: function(result) {
					$ES('select.musicSchoolDropdown').each(function(item2) {
						if(item2.getProperty('id') != item.id) {
							dwr.util.removeAllOptions(item2);
							dwr.util.addOptions(item2, result, 'id', 'value');
						}
					});
				}
			});
		});
	});
}
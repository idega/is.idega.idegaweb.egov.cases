(function($) {
	var DATA_KEY = 'multiple-ic-files-uploader';
	var AUC = 'active-uploader';
	var AUS = '.'+AUC;
	var preparerows = function(rows,data){
		   rows.each(function(){
			   var row = $(this);
			   row.find('.remove').click(data,function(e){
				   e.data.main.find('.components-div').find('.uploader-div').append(data.uploader);
				   var tr = $(this).parents("tr").first();
				   tr.remove();
			   });
			   var uploader = row.find('[type="file"]');
			   uploader.fileupload(data);
			   uploader.bind('fileuploadsubmit',{}, function (e) {
				   showLoadingMessage(jQuery.fn.multipleICFilesUploader.locale.loading);
				   return true;
			   });
			   uploader.on('hover',data,function(e){
				   $(AUS).removeClass(AUC)
				   $(this).addClass(AUC);
			   });
		   });
	   }
	var isBlank = function(str) {
	    return (!str || /^\s*$/.test(str));
	}
	var addTableActions = function(main,data){
		var addBtn = main.find('.add-attachments');
		addBtn.click(data,function(e){
			var main = $(e.data.main);
			var rowsContainer = main.find('.uploads-table').children('tbody');
			var row = main.find('.components-div').find('.td-div').find('tr').clone();
			rowsContainer.append(row);
			preparerows(row,e.data);
		});
		if(main.find('.uploads-table').children('tbody').children().length < 1){
			addBtn.click();
		}
	}
	var addDataToRow = function(row,file,data){
		row.find('.name-label').text(file.name);
		row.find('[name="'+ data.paramName +'"]').val(file.id);
		var description = row.find('.file-name');
		if(isBlank(description.val())){
			description.val(file.name);
		}else{
//				TODO: change file name
		}
		row.find('[type="file"]').remove();
	}
	var addExistingFiles = function(main,files,data){
		if(!files){
			return;
		}
		for(var i = 0;i < files.length;i++){
			var file = files[i];
			var rowsContainer = main.find('.uploads-table').children('tbody');
			var row = main.find('.components-div').find('.td-div').find('tr').clone();
			rowsContainer.append(row);
			addDataToRow(row,file,data);
			preparerows(row,data);
		}
	}
	var addTableData = function(main,data){
		addExistingFiles(main,data.icFiles,data);
	}
	$.fn.multipleICFilesUploader = function(options) {
		   if($(this).data(DATA_KEY)){
			   return;
		   }
		   var opts = $.extend($.fn.multipleICFilesUploader.defaults, options);
		   
		   return this.each(function(){
			   var uploader = $(this);
			   opts.main = uploader;
			   opts.paramName = uploader.attr('id');
			   opts.uploader = uploader.find('.file-uploader');
			   addTableData(uploader,opts);
			   addTableActions(uploader,opts);
		   } ); 
	   }
	   $.fn.multipleICFilesUploader.defaults = {
		      	url : '/servlet/ic-file-upload',
		        uploadTemplateId: null,
		        downloadTemplateId: null,
		        done: function (e, data) {
	   				var file = data.result[0];
	   				if (file.error) {
	   					closeAllLoadingMessages();
	                	alert(file.error);
	                    return;
	   				} 
	   				var uploader = $(AUS);
	   				var row = uploader.parents('.repeat-item').first();
	   				addDataToRow(row,file,data);
	   				closeAllLoadingMessages();
	   			} ,
		   	       fail: function(e,data){
			   	    	closeAllLoadingMessages();
			   	    	if(data.jqXHR.status == 413){
			   	    		alert(jQuery.fn.multipleICFilesUploader.locale.fileIsTooLarge);
			   	    		return;
			   	    	}
			   	    	alert(jQuery.fn.multipleICFilesUploader.locale.error);  
		   	       },
		          progressInterval : 2000,
		          maxFilesToUpload : 7,
		          singleFileUploads : true,
		          icFiles : []
		};
	   $.fn.multipleICFilesUploader.locale = {};
})(jQuery);

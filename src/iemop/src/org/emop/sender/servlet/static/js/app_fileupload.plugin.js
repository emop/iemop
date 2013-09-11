/**
结合upyun上传文件的控件。
*/

!function ($){
	$.fn.uploadfile = function(settings) {
		// Settings to configure the jQuery lightBox plugin how you like
		settings = jQuery.extend({
			bucket: 'tdcms',
			callback: function(e){}
		}, settings);
		
		//console.log(settings.callback);

		var jQueryMatchedObj = this;
		
		function _upload_callback(status){
			settings.callback(status);
		};
		
		if(!$.__file_upload_iframe_index){
			$.__file_upload_iframe_index = 100;
		}
	
		jQueryMatchedObj.each(function(){
			if(!$(this).attr("name")){
				$(this).attr("name", "file01");
			}
			$.__file_upload_iframe_index++;
			var _form = $("<form target='upload_frame_" + $.__file_upload_iframe_index + "' action='http://v0.api.upyun.com/" + 
				settings.bucket + "/'" +
				"method='post' enctype='multipart/form-data'>" + 
				"<input class='policy' type='hidden' name='policy' value=''>" + 
				"<input class='signature' type='hidden' name='signature' value=''>" +
				"<div class='help-inline uploading' style='display:none'>uploading...</div>"+
				"</form>");
			var tmp = $(this).replaceWith(_form);
			_form.append(tmp);
			
			var _iframe = $("<iframe class='upload_frame' name='upload_frame_" + $.__file_upload_iframe_index
				+  "' src='' style='display: none'></iframe>");
			_iframe.load(function(){
				var s = $(this).contents().find('body').text();
				var status = $.parseJSON(s);
				//
				var aa = typeof(status);
				//console.log("xx:" + aa);
				if(status){
					_upload_callback(status);
				}				
			});
			
			$("body").append(_iframe);
		});
	
		/*
			修改一个文件内容。
		*/
		function _start(){
			start_upload($(this));
			//$.show_ok("start to upload file.");
		};
		
		function start_upload(file){
			var form = $(file.parents("form")[0]);		
			if(file.val()){
				//file.hide();
				form.find(".uploading").show();
				settings.get_upload_param(file, function(data){
					if(data.status == 'ok'){
						form.find(".policy").val(data.data.policy);
						form.find(".signature").val(data.data.sign);
						form.submit();
					}else {
						$.show_error("上传图片出错。没有得到上传授权码。");
					}
				});
			}
		};
	
		return this.unbind('change').change(_start);
	};
}(window.jQuery);
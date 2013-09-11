!function ($) {
	function Toast(){
		var that = this;
		$("#msgbox").remove();
		//<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
		var box="<div id='msgbox' class='modal succeed hide fade in' style='width: 350px;margin: -60px 0 0 -175px;'>" +
			"<div class='modal-body'>" +		
			"	<button type='button' class='close' data-dismiss='modal' aria-hidden='true'>×</button>" +
			"	<p class='info'>内容</p>" +
			"</div>" +
			"</div>";
		$('body').append($(box));		
		this.model = $("#msgbox");
	};

	Toast.prototype.show = function(msg_type, title, msg, time, callback){
		this.model.removeClass("succeed");
		this.model.removeClass("error");
		this.model.addClass(msg_type);
		
		this.model.find(".info").html(msg);
		this.model.modal({});
		
		if(time > 0){
			var that = this;
			setTimeout(function(){
				that.model.modal('hide');
				if(callback){callback();}
			}, time);
		}
	};
	
	var toast = null; 
	$.show_error = function(msg, time, callback){
		if(!toast){toast = new Toast();}
		return toast.show("error", '', msg, time, callback);
	};
	
	$.show_ok = function(msg, time, callback){
		if(!toast){toast = new Toast();}
		return toast.show("succeed", '', msg, time, callback);
	};
}(window.jQuery);

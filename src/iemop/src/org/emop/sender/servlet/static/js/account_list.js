var APP_PARAM = {};

function load_account_list(param){	
	TC.call("timing_weibo_account_list", param, function(data){
		var templates = $("#account_row").text();
		
		$("#account_list tbody").empty();
		$.each(data.data.data, function(i, node){
			node.nick = node.nick || '未知微博名';
			var n = $("<tr></tr>");
			n.html(render_template(templates, node));
			$("#account_list tbody").append(n);
		});	
		
		if(parent.fixIframeHeight){
			parent.fixIframeHeight();	
		}
		
		create_paging($(".pagination"), data.data.count, param.page_size, param.page_no + 1);			
	});
}

function reg_event(){
	$("#status_filter").delegate("a", "click", function(e){
		var cur_target = $(e.currentTarget);
		
		APP_PARAM.user_status = cur_target.attr('st');
		APP_PARAM.page_no = 0;
		load_account_list(APP_PARAM);
		return false;	
	});
		
}

$(function(){
	APP_PARAM.page_size = APP_PARAM.page_size ? APP_PARAM.page_size : 20;
	APP_PARAM.page_no = APP_PARAM.page_no ? APP_PARAM.page_no : 0;
	
	load_account_list(APP_PARAM);
	Paging($(".pagination"), APP_PARAM, load_account_list);
	reg_event();
});
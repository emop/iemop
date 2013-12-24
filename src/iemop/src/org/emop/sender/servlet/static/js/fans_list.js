var APP_PARAM = {};

function load_account_list(param){	
	TC.call("fans_user_status_list", param, function(data){
		var templates = $("#account_row").text();
		
		$("#account_list tbody").empty();
		$.each(data.data.data, function(i, node){
			node.nick = node.nick || '未知微博名';
			
			node.fans_status_label = node.fans_status == 1 ? '加粉中' : '暂停';
			var n = $("<tr></tr>");
			n.html(render_template(templates, node));
			$("#account_list tbody").append(n);
		});	
		
		if(parent.fixIframeHeight){
			parent.fixIframeHeight();	
		}
		
		create_paging($(".pagination"), data.data.count, param.page_size, param.page_no + 1);			
	});
	
	TC.call("fans_Summary", param, function(data){
		if(data.status =="ok"){
			$("#fans_count").text(data.data.all_fans_data);
		}else{
			alert("遇见了不可预期的错误，请重试");
		}
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

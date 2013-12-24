var APP_PARAM = {};

function load_account_list(param){
	TC.call("timing_weibo_lib_list", param, function(data){
		var templates = $("#lib_row").text();
		
		var source = {}; //1:'冒泡库', 2:'自定义淘客', 3:'自定义图文'};		
		source[1] = '冒泡库';
		source[2] = '自定义淘客';
		source[3] = '自定义图文';
		
		$("#lib_list tbody").empty();
		$.each(data.data.data, function(i, node){
			node.source_label = source[node.source];
			var n = $("<tr></tr>");
			n.html(render_template(templates, node));
			$("#lib_list tbody").append(n);
		});		
		
		if(parent.fixIframeHeight){
			parent.fixIframeHeight();	
			setTimeout(function(){parent.fixIframeHeight();}, 3000)
		}
		create_paging($(".pagination"), data.data.count, param.page_size, param.page_no + 1);		
	});
}

$(function(){
	APP_PARAM.page_size = APP_PARAM.page_size ? APP_PARAM.page_size : 20;
	APP_PARAM.page_no = APP_PARAM.page_no ? APP_PARAM.page_no : 0;
	
	load_account_list(APP_PARAM);
	Paging($(".pagination"), APP_PARAM, load_account_list);
	
	$("#filter").click(function(){
		APP_PARAM.tags = $("#cate_name").val();
		APP_PARAM.page_no = 0;
		load_account_list(APP_PARAM);
		
		return false;
	});

	$("#import").click(function(){
		TC.call("timing_weibo_lib_update", {'emop_cate':$("#cate_name").val()}, function(data){
			if(data.status == 'ok'){
				$.show_ok("导入任务提交成功，等10多秒后刷新页面。");
			}else {
				$.show_ok("导入任务提交失败， 过会儿再试。");
			}
		});
		
		return false;
	});
	
});
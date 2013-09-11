function load_timing_list(param){
	TC.call("timing_auto_send_list", param, function(data){
		var templates = $("#timing_row").text();
		
		var source = {}; 	
		source[0] = '暂停';
		source[1] = '运行中';
		
		$("#timing_list tbody").empty();
		$.each(data.data.data, function(i, node){
			node.source_label = source[node.source];
			var n = $("<tr></tr>");
			n.html(render_template(templates, node));
			$("#timing_list tbody").append(n);
		});
		
		//load_timing_list();
		if(parent.fixIframeHeight){
			parent.fixIframeHeight();	
		}
	});
}

$(function(){
	load_timing_list({});
	
	$("#timing_list tbody").delegate("a.action", "click", function(e){
		e.preventDefault();				
		var cur_target = $(e.currentTarget);
		var aid = cur_target.attr('aid');
		
		TC.call('timing_run_auto_send_config', {'auto_send_id': aid} , function(e){
			if(e.status == 'ok'){
				$.show_ok("任务运行成功");
			}else {
				$.show_ok("任务运行失败, code:" + e.code);
			}
		});	
		
		return false;	
	});	
});
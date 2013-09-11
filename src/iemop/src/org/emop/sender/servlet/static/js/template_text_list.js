function load_data_list(param){
	TC.call("timing_template_text_list", param, function(data){
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
		}		
	});
}

$(function(){
	load_data_list({});
});
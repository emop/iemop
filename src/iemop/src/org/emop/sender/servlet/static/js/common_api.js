function TaodianCMS(proxy_url){
    this.proxy_url = proxy_url;
}

TaodianCMS.prototype.call = function(name, param, cb){
    //var post_param = {api_name: name};    
    param.__api_name = name;
    $.post(this.proxy_url, param, cb, 'json');
}

var TC = new TaodianCMS("/api/api_proxy/");


function render_template(templ, data){
	
	var text = templ.replace(/\$\{(\w+)\}/g,
                function (s, name) {
                    return data[name];
              });
              
    return text;
}

function Paging(container, param, cb){
	container.delegate("a", "click", function(e){
		e.preventDefault();				
		var cur_target = $(e.currentTarget);
		cur_target.parent().addClass("active");
		
		var page = cur_target.attr("data");
		if(page == 'pre'){
			param.page_no--;
		}else if(page == 'next'){
			param.page_no++;
		}else {
			param.page_no = parseInt(page) - 1;
		}
		
		cb(param);
				
		return false;	
	});
}

function create_paging(container, count, page_size, cur){
	var page_count = Math.ceil(count / page_size);
	
	var show_page = 6;
		
	var page_start = cur - show_page / 2;
	var page_end = cur + show_page / 2;
	if(cur < show_page / 2){
		page_end += show_page / 2 - cur;
	}
	if(cur > page_count - show_page / 2){
		page_start -= show_page / 2 - (page_count - cur);
	}
	
	
	page_start = page_start > 1 ? page_start : 1;
	page_end = page_end < page_count ? page_end : page_count;
	
	var c = container.find("ul");
	c.empty();
	
	if(page_count <= 1) return;
	
	if(cur > 1){
		c.append("<li><a href='#' data='pre'>上一页</a></li>");
	}
	
	for(; page_start <= page_end; page_start++){
		var node = $("<li><a href='#' data='" + page_start + "'>" + page_start  + "</a></li>");
		if(page_start == cur){
			node.addClass("active");
		}
		c.append(node);
	}
	
	if(cur < page_end){
		c.append("<li><a href='#' data='next'>下一页</a></li>");
	}
}
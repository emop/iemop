$("#main_menu").delegate("a", "click", function(e){
	var menu = $("#main_menu");
	menu.find("li").removeClass("active");
	e.preventDefault();				
	var cur_target = $(e.currentTarget);
	cur_target.parent().addClass("active");
	
	var link = cur_target.attr("href");
	
	$("#ifm").attr('src', link);
	
	return false;	
});


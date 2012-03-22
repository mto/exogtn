$(function() {
	$("#tree").treeview({
		collapsed : true,
		animated : "fast",
		control : "#sidetreecontrol",
		prerendered : true,
		persist : "location"
	});
	
	$(".jz").bind("lazyload", function() {
		$(".expandable").each(function() {
			var child = $(this).find("ul");
			if(child.length == 1) {
				if(!child[0].childElementCount) {
					$(this).one("click", function() {
						var loadAction = this.SitemapApplication().loadChild();
						var param = {};
						var id = $(this).attr("id");
						param["id"] = id.substr(0,id.indexOf("-"));
						var result;
						var ajaxGet = $.ajax({
							type: "get",
							data: param,
						    url: loadAction,
						    dataType: "html",
						    success: function(data) {
						    	var branches = $(data).appendTo(child[0]);
								$("#tree").treeview({
									add: branches
								});
								$(".jz").trigger("lazyload");
						    }
						});
						
					});
				}
			}
		});
	});
	$(".jz").trigger("lazyload");
})
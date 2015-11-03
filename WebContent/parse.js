/**
 * Determines whether an event is from the "Enter" key or not and prevents
 * "Enter" key events from propagating.
 */
function isEnter(event) {
	if (13 === (event.keyCode ? event.keyCode : event.which ? event.which : event.charCode)) {
		if (event.preventDefault) {
			event.preventDefault();
		}
		return true;
	} else {
		return false;
	}
}

var sentence = {};

sentence.layout = function(){
	var height = 1;
	var width = 1;
	var nodes = [];
	var links = [];
	
	function layout(s){
		var tokens = s.tokens;
		var stanford = s.stanford;
		var segments = s.segments;	

		// Set token positions
		var n = tokens.length;
		var i = -1;
		while(++i < n){
			tokens[i].position = i;
			tokens[i].name+="|"+tokens[i].pos;
		}

		// Connect segments to tokens
		if (segments && (n=segments.length)){
			// Connect segments and tokens
			i = -1;
			while(++i < n){
				var segment = segments[i];
				segment.tokens = [];
				for(var j=segment.from ; j<= segment.to ; j++){
					var token = tokens[j];
					token.segment = segment;
					segment.tokens.push(token);
				}
			}
		}

		// depth includes segments and tokens
		var depth = 2;
		if (stanford){
			// Replace parse tree leaf indicators with tokens
			function replace_leaves(node){
				var n;
				if (node.children && (n=node.children.length)){
					var i=-1;
					while(++i < n){
						var child = node.children[i];
						if (child.children){
							replace_leaves(child); 
						} else {
							node.children[i] = tokens[child.position];
						}
					}
				}
			}
			replace_leaves(stanford);
			
			// Add parse tree to depth
			function tree_depth(node){
				nodes.push(node);
				var n;
				if (node.children && (n = node.children.length)){
					var i = -1;
					var depth = 0;
					while(++i < n){
						var child = node.children[i];
						links.push({source:node, target:child});
						depth = Math.max(depth, tree_depth(child));
					}
					return depth+1;
				} else {
					// Leave room for segment
					return 2;
				}
			}
			depth = tree_depth(stanford);
		}

		var xscale = width/(depth+1);
		var xoff = xscale/2;
		var yscale = height/(tokens.length+1);
		var yoff = yscale/2;

		// Tokens have depth 1
		n = tokens.length;
		i = -1;
		while(++i < n){
			tokens[i].x = xoff+xscale;
			tokens[i].y = yoff+yscale*i;
		}
		
		// Segments go in column 0, vertically centered on token
		if (segments && (n=segments.length)){
			var i = -1;
			while(++i < n){
				var segment = segments[i];
				nodes.push(segment);
				segment.x = xoff;
				segment.y = (segment.tokens[0].y+segment.tokens[segment.tokens.length-1].y)/2;
				var j = -1;
				while(++j < segment.tokens.length){
					var token = segment.tokens[j];
					links.push({target:segment, source:token});
				}
			}
		}
		
		if (stanford){
			function recurse(node){
				var n;
				if (node.children && (n = node.children.length)){
					var d = -1;
					var ymin = height;
					var ymax = 0;
					var i=-1;
					while(++i < n){
						var val = recurse(node.children[i]);
						d = Math.max(val[0], d);
						ymin = Math.min(val[1], ymin);
						ymax = Math.max(val[2], ymax);
					}
					d++;
					node.x = xoff+d*xscale;
					node.y = (ymin+ymax)/2;
					return [d, ymin, ymax];
				} else {
					// token -- already done
					return [1, node.y, node.y];
				}
			}
			recurse(stanford);
		}
		return nodes;		
	}
	
	layout.height = function(x){
		if (!arguments.length) return height;
		height = x;
		return layout;
	};
	
	layout.width = function(x){
		if (!arguments.length) return width;
		width = x;
		return layout;
	};
	
	layout.links = function(){
		return links;
	};
	
	layout.diagonal = function(d, i) {
 	
		var p0x = d.source.x;
		var p0y = d.source.y;
		var p3x = d.target.x;
		var p3y = d.target.y;
   		var m = (p0x + p3x) / 2;
		return "M" + [p0x,p0y] + "C" + [m, p0y] + " " + [m, p3y] + " " + [p3x,p3y];
	};
	
	return layout;
};

var width;
var height;

var svg;
$(document).ready(function(){
	width = innerWidth-80;
	height = innerHeight-80;
	svg = d3.select("body").append("svg")
	.attr("width", width)
	.attr("height", height)
	.append("g")
	.attr("transform", "translate(40,0)");
});


function drawTree(text){
	var url="parse?text="+encodeURIComponent(text);
	d3.json(url, function(error, s){
	    var table = $('#dependencies');
	    $('#dependencies').empty();
		
		// print the dependencies
	    table.append("<div>Dependencies:</div>");
	    for(var i in s.dependencies){
	    	table.append("<div>"+s.dependencies[i]+"</div>");
	    }
	    
		console.log(s);
		var sent = sentence.layout()
		.width(width)
		.height(height);

		//sentence.connect(s);
		svg.selectAll(".link").remove();
		svg.selectAll(".node").remove();
		
		var nodes = sent(s);
		var links = sent.links();
		var link = svg.selectAll(".link")
		.data(links)
		.enter().append("path")
		.attr("class", "link")
		.attr("d", sent.diagonal);

		var node = svg.selectAll(".node")
		.data(nodes)
		.enter().append("g")
		.attr("class", "node")
		.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });

		node.append("circle")
		.attr("r", 4.5);

		node.append("text")
		.attr("dx", function(d) { return d.children ? -8 : 8; })
		.attr("dy", 3)
		.style("text-anchor", function(d) { return d.children ? "end" : "start"; })
		.text(function(d) { return d.name; });
	});
}

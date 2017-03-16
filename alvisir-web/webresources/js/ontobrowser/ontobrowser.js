/*
 *
 *      This file was partly developped in the TriPhase project
 *
 *      Copyright Institut National de la Recherche Agronomique, 2013.
 *
 */
d3.mig = {};

//Derived from http://mbostock.github.io/d3/talk/20111018/tree.html
d3.mig.ontobrowser = function module() {
	//use function property to track instance number
	if (typeof module.instanceNum == 'undefined') {
		module.instanceNum = 0;
	}


	var jsonUrl;
	var _selectConcepts;
	var _expandPath;
	var selectedItems = {};

	var dispatch = d3.dispatch('dataLoaded', 'ready', 'conceptSelectionChanged', 'conceptMouseOver', 'conceptMouseOut');

	function exports(_selection) {

		_selection.each(function(_data) {
			module.instanceNum++;
			prepareGraph("ob" + module.instanceNum, d3.select(this), jsonUrl);
		});
	}

	exports.jsonUrl = function(_x) {
		if (!arguments.length)
			return jsonUrl;
		jsonUrl = _x;
		return this;
	};

	//aConceptIds : array of concept identifiers
	exports.selectedConcepts = function(aConceptIds, status) {
		if (!arguments.length)
			return selectedItems;
		var kconcepts = _.object(aConceptIds, aConceptIds);
		_selectConcepts(undefined, function(conceptNode) {
			if (conceptNode.intid in kconcepts) {
				return status;
			} else {
				return undefined;
			}
		}, false);
		return this;
	};

	//path : array of concept identifiers of the nodes to be openened
	exports.expandPath = function(path) {
		_expandPath(path);
	};

	d3.rebind(exports, dispatch, "on");
	return exports;


	function prepareGraph(instancePrefix, container, jsonurl) {
		var columnWidth = 190;
		var minRadius = 4;
		var maxRadius = 20;
		var fontSize = 11; //must be same value as declared in css

		var descendantScale = d3.scale.linear();
		descendantScale.range([minRadius, maxRadius]);
		var levelScale = d3.scale.linear();
		levelScale.range([minRadius, maxRadius]);
		var labelSizeDisplayed = 23;
		var colorScale = d3.scale.category10();
		var distribBoxMargin = 3;

		var contWidth;
		var contHeight = 800;
		var tree, vis, diagonal;
		var root;
		var i = 0;


		//load data
		d3.json(jsonurl, function(json) {
			root = json;
			/*
			 var nbNnodeByLevel = []
			 getNbNodesByLevel(root, 0, nbNnodeByLevel);
			 contHeight = _.max(nbNnodeByLevel) * fontSize * 0.5;
			 */

			dispatch.dataLoaded(json);
			initAfterDataLoaded();
			dispatch.ready();
		});

		function getNbNodesByLevel(currentNode, currentLevel, result) {
			if (result[currentLevel] == undefined) {
				result[currentLevel] = 0;
			}
			result[currentLevel]++;
			_.each(currentNode.children, function(element, index, list) {
				getNbNodesByLevel(element, currentLevel + 1, result);
			});
		}




		function initAfterDataLoaded() {

			//adjust width depending on tree depthness
			contWidth = (2 + root.sublevelnb) * (columnWidth + maxRadius);


			descendantScale.domain([0, Math.sqrt(root.descendantnb)]);
			levelScale.domain([0, root.sublevelnb]);
			var margin = {top: 20, right: 120, bottom: 20, left: 120};
			var w = contWidth - margin.right - margin.left;
			var h = contHeight - margin.top - margin.bottom;

			tree = d3.layout.tree()
					.size([h, w]);
			tree.sort(function comparator(a, b) {
				return d3.ascending(a.name, b.name);
			});


			diagonal = d3.svg.diagonal()
					.projection(function(d) {
				return [d.y, d.x];
			});

			vis = container
					.attr("width", contWidth)
					.attr("height", contHeight)
					.attr("viewBox", "0 0 " + contWidth + " " + contHeight)

					.append("svg:g")
					.attr("class", "ontobrowser-tree")
					.attr("transform", "translate(" + margin.left + "," + margin.top + ")");

			root.x0 = h / 2;
			root.y0 = 0;

			function toggleAll(d) {
				if (d.children) {
					d.children.forEach(toggleAll);
					toggleExpanded(d);
				}
			}

			// Initialize the display to show a few nodes.
			root.children.forEach(toggleAll);
			update(root);
		}

		_selectConcepts = selectConcepts;
		function selectConcept(d, selected) {
			if (setSelected(d, selected)) {
				d3.select("#" + getNodeId(d) + " rect.selectmarker").classed("selectedmarker", d.selected);
			}

		}
		function selectConcepts(d, predicat, forceSelectChildren, parentSelected) {
			if (undefined == d) {
				d = root;
			}

			var selected = predicat(d);
			if (undefined != selected) {
				selectConcept(d, selected);
			}

			var children = d.children;
			if (undefined == children) {
				//for collapsed node 
				children = d._children;
			}
			if (undefined != children) {
				children.forEach(function(child) {
					selectConcepts(child, predicat, forceSelectChildren, selected);
				});
			}
		}


		//path : array of concept identifiers of the nodes to be openened
		_expandPath = expandPath;
		function expandPath(path) {
			expandSubPath(root, path);
		}

		function expandSubPath(node, subpath) {
			var pathHeadId = _.head(subpath);
			if (node.intid === pathHeadId) {
				var rest = _.tail(subpath);
				if (rest.length > 0) {
					var subpathHeadId = _.head(rest);
					var subPathHead = _.findWhere(getChildren(node), {intid: subpathHeadId});
					if (subPathHead != undefined) {
						setExpanded(node, true);
						update(node);
						expandSubPath(subPathHead, rest);
					}
				} else {
					//select last element in the path
					selectConcept(node, true);
				}
			}

		}

		function toggleAll(d) {
			if (d.children) {
				d.children.forEach(toggleAll);
				toggleExpanded(d);
			}
		}

		function update(source) {
			var duration = d3.event && d3.event.altKey ? 5000 : 500;

			// Compute the new tree layout.
			var nodes = tree.nodes(root).reverse();

			// Normalize for fixed-depth.
			nodes.forEach(function(d) {
				d.y = d.depth * (columnWidth + maxRadius);
			});

			// Update the nodes…

			var node = vis.selectAll("g.node")
					.data(nodes, function(d) {
				return d.id || (d.id = ++i);
			});

			/*
			 * Init nodes
			 */

			// Enter any new nodes at the parent's previous position.
			var nodeEnter = node.enter().append("svg:g")
					.attr("class", "node")
					.attr("id", function(d) {
				return getNodeId(d);
			})
					.attr("transform", function(d) {
				return "translate(" + source.y0 + "," + source.x0 + ")";
			});



			//distribution icon : width depends on nb of descendants; heigth depends on nb of level below
			nodeEnter.append("svg:path")
					.attr("class", "distrib")
					.attr("d", "m " + minRadius + " " + minRadius + " l 0 -" + (2 * minRadius));

			//rectangle element surrounding distribution icon, used as a click target for expand/collapse
			nodeEnter.append("svg:rect")
					.attr("class", "distribbox")
					.attr("width", 1e-6)
					.attr("height", 1e-6)
					.attr("transform", function(d) {
				var offset = getCircleRadius(d) + distribBoxMargin;
				return "translate(-" + distribBoxMargin + ", -" + offset + ")";
			})
					.on("click", doExpandCollapse);

			//group element for label
			var glabels = nodeEnter.append("svg:g")
					//rotation to avoid labels collision
					.attr("transform", function(d, i) {
				return "rotate(" + 0 + ")"
			}
			);

			//rectangle element used has background for concept label
			glabels
					.append('svg:rect')
					.attr("class", "textbackground selectmarker")
					.attr("width", 1e-6)
					.attr("height", 1e-6)
					.classed("selectedmarker", function(d) {
				return d.selected;
			})
					.style("fill", function(d) {
				return hasChildren(d) ? colorScale(d.depth) : "silver";
			})
					.attr("transform", function(d) {
				var offset = columnWidth - maxRadius;
				return "translate(-" + offset + ", -" + fontSize + ")";

			})
					.on("click", function(d) {
				doSelectUnselect(d, d3.select(this))
			});


			//concept label
			glabels
					.append("svg:text")
					.attr("id", function(d) {
				return getLabelId(d);
			})
					.attr("class", "conceptlabel")
					.attr("x", function(d) {
				return -(2 * minRadius);
			})
					.attr("dy", ".35em")
					.attr("text-anchor", "end")
					.text(function(d) {
				return getShortenedLabel(d);
			})
					.attr("title", function(d) {
				return d.extid;
			})
					.style("fill-opacity", 1e-6)

					.on("mouseover", function(d) {
				d3.select(this).text(d.name);
				dispatch.conceptMouseOver(d);

			})

					.on("mouseout", function(d) {
				d3.select(this).text(getShortenedLabel(d));
				dispatch.conceptMouseOut(d);
			})
					.on("click", function(d) {
				doSelectUnselect(d, d3.select(this))
			});


			/*
			 * Update Nodes
			 */

			// Transition nodes to their new position.
			var nodeUpdate = node.transition()
					.duration(duration)
					.attr("transform", function(d) {
				return "translate(" + d.y + "," + d.x + ")";
			});


			nodeUpdate.select("rect.distribbox")
					.attr("width", function(d) {
				return hasChildren(d) ? 2 * maxRadius : 1e-6;
			})
					.attr("height", function(d) {
				return hasChildren(d) ? 2 * (getCircleRadius(d) + distribBoxMargin) : 1e-6;
			}).attr("title", function(d) {
				return (d.descendantnb > 0) ? (d.descendantnb + " node" + ((d.descendantnb > 1) ? "s" : "") + " / " + d.sublevelnb + " level" + ((d.sublevelnb > 1) ? "s" : "")) : "";
			});

			nodeUpdate.select("path.distrib")
					.attr("d", function(d) {
				if (d.descendantnb > 0) {
					var hsize = descendantScale(Math.sqrt(d.descendantnb));
					var vsize = levelScale(d.sublevelnb) / 2;
					var ho = Math.max(0, 2 * (maxRadius - distribBoxMargin - hsize));
					return "m 0 0"
							+ " l " + ho + " 0"
							+ " l " + 2 * hsize + " " + vsize
							+ " l 0 -" + (2 * vsize)
							+ " l -" + 2 * hsize + " " + vsize
							+ " Z";
					//return "m 0 0 l " + 2 * hsize + " " + vsize + " l 0 -" + (2 * vsize) + " Z";
				} else {
					return "m " + minRadius + " " + minRadius + " l 0 -" + (2 * minRadius);
				}

			}).style("fill", function(d) {
				return hasChildren(d) ? colorScale(d.depth + 1) : "#fff";
			});

			nodeUpdate.select("rect.textbackground")
					.attr('width', columnWidth - maxRadius)
					.attr('height', 2 * fontSize)
					.attr("ry", fontSize)
					.style("fill-opacity", 0.2);

			nodeUpdate.select("text.conceptlabel")
					.style("fill-opacity", 1);

			/*
			 * 
			 */

			// Transition exiting nodes to the parent's new position.
			var nodeExit = node.exit().transition()
					.duration(duration)
					.attr("transform", function(d) {
				return "translate(" + source.y + "," + source.x + ")";
			})
					.remove();

			nodeExit.select("path.distrib")
					.attr("d", "m 0 0");

			nodeExit.select("text")
					.style("fill-opacity", 1e-6);

			// Update the links…
			var link = vis.selectAll("path.link")
					.data(tree.links(nodes), function(d) {
				return d.target.id;
			});

			// Enter any new links at the parent's previous position.
			link.enter().insert("svg:path", "g")
					.attr("class", "link")
					.attr("d", function(d) {
				var o = {x: source.x0, y: source.y0};
				return diagonal({source: o, target: o});
			})
					.transition()
					.duration(duration)
					.attr("d", diagonal);

			// Transition links to their new position.
			link.transition()
					.duration(duration)
					.attr("d", diagonal);

			// Transition exiting nodes to the parent's new position.
			link.exit().transition()
					.duration(duration)
					.attr("d", function(d) {
				var o = {x: source.x, y: source.y};
				return diagonal({source: o, target: o});
			})
					.remove();

			// Stash the old positions for transition.
			nodes.forEach(function(d) {
				d.x0 = d.x;
				d.y0 = d.y;
			});
		}

		function doSelectUnselect(d, e) {
			toggleSelected(d);
			if (e.select("rect.selectmarker").empty()) {
				e = d3.select("#" + getNodeId(d) + " rect.selectmarker");
			}
			e.classed("selectedmarker", d.selected);
			update(d);
		}

		function doExpandCollapse(d) {
			var expanded = toggleExpanded(d);
			update(d);
		}


		function getNodeId(d) {
			return  instancePrefix + "-nd-" + d.id;
		}

		function getLabelId(d) {
			return  instancePrefix + "-lbl-" + d.id;
		}

		function getCircleRadius(d) {
			return d.descendantnb > 0 ? descendantScale(Math.sqrt(d.descendantnb)) : minRadius;
		}

		function getShortenedLabel(d) {
			return d.name.length > labelSizeDisplayed ? d.name.substring(0, labelSizeDisplayed) + "..." : d.name;
		}

		function hasChildren(d) {
			return d.children || d._children;
		}

		function nbChildren(d) {
			if (d.children) {
				return d.children.length;
			} else if (d._children) {
				return d._children.length;
			} else {
				return 0;
			}
		}

		function setSelected(d, selected) {
			if ((selected && !d.selected) || (!selected && d.selected)) {
				toggleSelected(d);
				return true;
			} else {
				return false;
			}
		}

		function toggleSelected(d) {
			if (d.selected) {
				d.selected = false;
				delete selectedItems[getLabelId(d)];
			} else {
				d.selected = true;
				selectedItems[getLabelId(d)] = d;
			}

			var ds = [];
			for (var key in selectedItems) {
				ds.push(selectedItems[key]);
			}
			dispatch.conceptSelectionChanged(ds);
		}

		function isExpanded(d) {
			return d["children"] != undefined && d["children"] != null;
		}

		function setExpanded(d, expand) {
			var previousState = isExpanded(d);
			if (!expand && previousState) {
				d._children = d.children;
				d.children = null;
			} else if (expand && !previousState) {
				d.children = d._children;
				d._children = null;
			}
			return previousState;
		}
		// Toggle children.
		function toggleExpanded(d) {
			setExpanded(d, !isExpanded(d));
		}

		function getChildren(d) {
			if (d["children"] != undefined) {
				return d.children;
			} else {
				return d._children;
			}
		}

	}
};


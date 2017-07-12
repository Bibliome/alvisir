//-- ------------------------------------------------------------------------ --
var ONTOURL = "resources/ontologies/";
var ob;
var initialScaleFactor = 1;
var sliderUnitScaleCoef = 200;
var autoscrollingPaddingSize = 18;
var hscrollingAnimLength = 1500;
var vscrollingAnimLength = 1300;
var hscrollbarHeight = 16;
var vscrollbarWidth = 16;

function getOntoBrowser() {
    return ob;
}
//adjust element size after window has be resized
function resetContainer() {
    var wMax = $("#graph-toolbar").css("width");
    var hMax = $("#onto-box").innerHeight() - 2 * 3 - $("#onto-toolbar").height() - 13;

    $("#graph-container").css("max-width", wMax).css("max-height", hMax + 'px');
    $("#graph-container").css("width", wMax).css("height", hMax + 'px');
    $("#svg-container").css("max-width", wMax).css("max-height", hMax + 'px');
    $("#svg-container").css("width", wMax).css("height", hMax + 'px');

    adjustToScaleFactor(initialScaleFactor);

    var graphCont = $("#graph-container");
    var gPos = graphCont.position();

    var outerH = graphCont.outerHeight(true);
    var innerH = graphCont.innerHeight();
    var mbpVert = outerH - innerH;
    var outerW = graphCont.outerWidth(true);
    var innerW = graphCont.innerWidth();
    var mbpHoriz = outerW - innerW;

    var selectedConceptWidth = $("#onto-selconcept").width();

    $("#scroll-left").css("width", autoscrollingPaddingSize)
            .css("top", gPos.top + mbpVert)
            .css("left", gPos.left + mbpHoriz);

    $("#scroll-right").css("width", autoscrollingPaddingSize)
            .css("top", gPos.top + mbpVert)
            .css("left", gPos.left + outerW - mbpHoriz - autoscrollingPaddingSize - vscrollbarWidth + selectedConceptWidth);

    $("#scroll-up").css("height", autoscrollingPaddingSize)
            .css("left", gPos.left + mbpHoriz);

    $("#scroll-down").css("height", autoscrollingPaddingSize)
            .css("left", gPos.left + mbpHoriz);

}

//change zoom factor
function adjustToScaleFactor(scaleFactor) {
    var svg = document.getElementById("svg-root");

    //
    var box = svg.getAttribute('viewBox');
    if (box != undefined) {
        //adjust root SVG element size to simulate zooming (remember viewBox is left unchanged)
        var boxParams = box.split(/\s+|,/);
        var wScaled = (boxParams[2] - boxParams[0]) * scaleFactor;
        var hScaled = (boxParams[3] - boxParams[1]) * scaleFactor;
        svg.setAttribute("width", wScaled);
        svg.setAttribute("height", hScaled);
        //adjust container size to keep it centered (thanks to auto margin)
        $("#centered-container").css("width", wScaled).css("height", hScaled);

        //adjust the scrolling helpers
        var scrollCont = $("#svg-container");
        var hasVScroll = (scrollCont.get(0).scrollHeight > scrollCont.height());
        var hasHScroll = (scrollCont.get(0).scrollWidth > scrollCont.width());

        var graphCont = $("#graph-container");
        var outerH = graphCont.outerHeight(true);
        var innerH = graphCont.innerHeight();
        var mbpVert = outerH - innerH;

        var selectedConceptWidth = $("#onto-selconcept").width();
        var outerW = graphCont.outerWidth(true);
        var innerW = graphCont.innerWidth();
        var mbpHoriz = outerW - innerW - selectedConceptWidth;

        $("#scroll-left")
                .css("display", hasHScroll ? "block" : "none")
                .css("height", innerH - mbpVert - hscrollbarHeight);

        $("#scroll-right")
                .css("display", hasHScroll ? "block" : "none")
                .css("height", innerH - mbpVert - hscrollbarHeight);

        var gPos = graphCont.position();

        $("#scroll-up")
                .css("display", hasVScroll ? "block" : "none")
                .css("width", innerW - mbpHoriz - vscrollbarWidth)
                .css("top", gPos.top + mbpVert);

        $("#scroll-down")
                .css("display", hasVScroll ? "block" : "none")
                .css("width", innerW - mbpHoriz - vscrollbarWidth)
                .css("top", gPos.top + innerH - autoscrollingPaddingSize - (hasHScroll ? hscrollbarHeight : 0));

        var hh = innerH - mbpVert - hscrollbarHeight;
        $("#onto-selconcept").css("height", hh);
        $("#onto-selected-container").css("height", hh - $("#onto-selconcept-bar").outerHeight(true));
    }
}

function loadOnto(ontoName) {
    ob = d3.mig.ontobrowser().jsonUrl(ONTOURL + ontoName);
    ob(d3.select("#svg-root"));

    ob.on('ready', function() {
        $("#centered-container").css("background-color", "");
        $("#scale-slider").slider('setValue', sliderUnitScaleCoef * initialScaleFactor);
        resetContainer();
    });

    ob.on('dataLoaded',
            function(tree) {

                //flatten Ontology
                var flatConceptList = [];
                var alreadySeen = {};

                //flatten Ontology to display it in combo box
                function traverse(o, level, parentPath) {
                    if ("name" in o) {
                        var extId = o["extid"];
                        if (!(extId in alreadySeen)) {
                            var name = o["name"];
                            alreadySeen[extId] = true;
                            var currentPath = parentPath.concat([o['intid']])
                            flatConceptList.push({label: name, value: o['intid'], path: currentPath});
                            if ("children" in o) {
                                for (var i = 0; i < o["children"].length; i++) {
                                    traverse(o["children"][i], level + 1, currentPath);
                                }
                            }
                        }
                    }
                }
                if (tree != undefined) {
                    traverse(tree, 0, []);
                }
                flatConceptList.sort();

                ob.flatConceptList = flatConceptList;

            });


    $("#clear-selected-concept").click(function() {
        var asc = _.map(getOntoBrowser().selectedConcepts(), function(d) {
            return d.intid;
        });
        getOntoBrowser().selectedConcepts(asc, false);
    });

    //update list of selected concepts
    ob.on('conceptSelectionChanged', function(ds) {
        $("#onto-selected-list").empty();

        ds.forEach(function(d) {
            $("#onto-selected-list").append('<div class="selected-concept" title="' + d.intid + '">' + d.name + '</div> ');
        });
        $("#clear-selected-concept").text("clear selection (" + ds.length + ")");

    });
}

function doConceptSearch(value, name) {
    var resultPopup = $("#concept-searchresult-popup");
    resultPopup.css("display", "none").empty();

    var hits = _.filter(getOntoBrowser().flatConceptList,
            function(item) {

                return item.label.indexOf(value) > -1;
            });

    _.each(hits, function(d) {
        var div = $('<div class="search-result-item" title="' + d.value + '">' + d.label + '</div>').appendTo(resultPopup);
        jQuery.data(div[0], "item", d);
    });

    var searchBox = $("#concept-searchbox");
    var searchGroup = $("#concept-searchgroup");
    var searchGroupPos = searchGroup.position();
    var w = (searchGroup.width() + 20) + "px";
    resultPopup
            .css("display", "block")
            .css("top", (searchGroupPos.top + searchBox.height()) + "px")
            .css("left", (searchGroupPos.left) + "px")
            .css("width", w)
            .css("max-width", w)
            .css("min-width", w);

    resultPopup.click(function(event) {
        var item = jQuery.data(event.target, "item");
        hideOntoSearchResultPopup();
        if (item != undefined) {
            getOntoBrowser().expandPath(item.path);
        }

    });
}

var ontoPopupShowing = false;

function showOntoPopup() {
    if (!ontoPopupShowing) {
        $("#onto-box").css("display", "block");
        $("#onto-box").css("top", "2%").css("left", "2%").css("height", "94%").css("width", "96%");
        ontoPopupShowing = true;

        $("#concept-searchbox").searchbox('resize', $("#concept-searchgroup").width());
        $("#concept-searchbox").click(hideOntoSearchResultPopup);
    }
}

function hideOntoPopup() {
    if (ontoPopupShowing) {
        $("#onto-box").css("display", "none");
        ontoPopupShowing = false;
        hideOntoSearchResultPopup();
    }
}

function hideOntoSearchResultPopup() {
    $("#concept-searchresult-popup").css("display", "none").empty();
}

function initOntoBrowser() {
    //whenever the window is resized, graph container size must be adjusted
    $(window).resize(function() {
        resetContainer();
    });


    //slider used for zooming
    $("#scale-slider").slider({
        min: 25,
        max: 500,
        step: 5,
        value: (sliderUnitScaleCoef * initialScaleFactor),
        mode: 'h',
        onChange: function(newValue, oldValue) {
            var scaleFactor = newValue / sliderUnitScaleCoef;
            adjustToScaleFactor(scaleFactor);
        }
    });

    //bind scrolling helpers to mouse events
    var stopScrollingAnim = function() {
        $("#svg-container").stop();
    };

    $('#scroll-up').mouseenter(function() {
        $("#svg-container").animate({scrollTop: 0}, vscrollingAnimLength);
    });
    $('#scroll-up').mouseleave(stopScrollingAnim);

    $('#scroll-down').mouseenter(function() {
        var scrollCont = $("#svg-container");
        scrollCont.animate({scrollTop: scrollCont.get(0).scrollHeight}, vscrollingAnimLength);
    });
    $('#scroll-down').mouseleave(stopScrollingAnim);

    $('#scroll-left').mouseenter(function() {
        $("#svg-container").animate({scrollLeft: 0}, hscrollingAnimLength);
    });
    $('#scroll-left').mouseleave(stopScrollingAnim);

    $('#scroll-right').mouseenter(function() {
        var scrollCont = $("#svg-container");
        scrollCont.animate({scrollLeft: scrollCont.get(0).scrollWidth}, hscrollingAnimLength);
    });
    $('#scroll-right').mouseleave(stopScrollingAnim);


    //bind event to clear button
    $("#clear-btn").click(function() {
        //clear query input box and set focus on it
        $('#query-input').focus().val('');

        //remove any error message
        $('#error-box').hide();
    });

    //bind event to submit query after selecting concepts
    $("#refine-with-onto").click(function() {
        queryWithOnto(true, $("#onto-operator-and").linkbutton('options').selected);
    });
    $("#search-with-onto").click(function() {
        queryWithOnto(false, $("#onto-operator-and").linkbutton('options').selected);
    });


    var lastOntoDisplayed = '';
    //bind event to ontology menus
    $('#ontos-menu').menu({
        onClick: function(item) {
            //display ontology popup
            ontoName = item.text;
            //reset onto browser if user just choosed another ontology to be displayed
            if (ontoName !== lastOntoDisplayed) {
                $("#svg-container").scrollTop(0).scrollLeft(0);
                $("#svg-root").empty();
                $("#onto-selected-list").empty();
                $("#concept-searchresult-popup").css("display", "none").empty();
                $("#centered-container").css("background-color", "silver");
                loadOnto(ontoName);
                lastOntoDisplayed = ontoName;
                $("#onto-name").text(ontoName);
            }
            showOntoPopup();
        }
    });


    //hide ontology popup
    $(document).mouseup(function(e) {
        var ontobox = $("#onto-box");
        if (!ontobox.is(e.target) && ontobox.has(e.target).length === 0) {
            hideOntoPopup();
        }
    });
}

//- -------------------------------------------------------------------------- -
function initHelp() {
//    var cheetSheet = $('<div id="help-content" style="display:none;"> <div id="cheat-sheet" class="cheat-sheet-panel">  <h2 >Cheat sheet</h2>  <table> <tr> <th rowspan="3">term</th> <td> <code>transcription</code> </td> </tr> <tr> <td> <code>lec2</code> </td> </tr> <tr> <td> <code>sigma\(K\)</code> </td> </tr> <tr> <td colspan="2"> <em>expansion looks for each term</em> </td> </tr> <tr> <td colspan="2"> <em>characters that require escaping:</em>  <code>( ) [ ] ~ =</code> </td> </tr> <tr> <td colspan="2"> <em> <strong>any</strong> other character does not require escaping</em> </td> </tr> </table> <table> <tr> <th rowspan="2">phrase</th> <td> <code>"Bacillus subtilis"</code> </td> </tr> <tr> <td> <code>"therapeutical treatment"</code> </td> </tr> <tr> <td colspan="2"> <em>expansion looks for each phrase</em> </td> </tr> <tr> <td colspan="2"> <em>documents must contain all terms one next to another in the specified order</em> </td> </tr> </table> <table> <tr> <th rowspan="2">prefix</th> <td> <code>Bacill*</code> </td> </tr> <tr> <td> <code>pharma*</code> </td> </tr> <tr> <td colspan="2"> <em>expansion does not look for prefixes</em> </td> </tr> </table> <table> <tr> <th rowspan="3">and</th> <td> <code>obesity and diabete</code> </td> </tr> <tr> <td> <code>bacteria AND transcription</code> </td> </tr> <tr> <td> <code>obesity diabete</code> </td> </tr> <tr> <td colspan="2"> <em>implicit operator</em> </td> </tr> </table> <table> <tr> <th rowspan="2">or</th> <td> <code>inhibition or activation</code> </td> </tr> <tr> <td> <code>diabete OR hypertension</code> </td> </tr> </table> <table> <tr> <th rowspan="2">not</th> <td> <code>regulation not inhibition</code> </td> </tr> <tr> <td> <code>diabete NOT obesity</code> </td> </tr> <tr> <td colspan="2"> <em>binary operator (requires left operand)</em> </td> </tr> </table> <table> <tr> <th rowspan="2">field</th> <td> <code>title=oral</code> </td> </tr> <tr> <td> <code>kind=a1</code> </td> </tr> <tr> <td colspan="2"> <em>default field depends on the search engine instance</em> </td> </tr> <tr> <td colspan="2"> <em>some SE instances may define alias fields</em> </td> </tr> </table>  <table> <tr> <th rowspan="2">near</th> <td> <code>food ~4 bacteria</code> </td> </tr> <tr> <td> <code>particle ~5 diameter</code> </td> </tr> <tr> <td colspan="2"> <em>field qualifiers are not allowed inside near operands</em> </td> </tr> </table>  <table> <tr> <th rowspan="4">grouping</th> <td> <code>(diabete not obes*) or (diabete non-obese)</code> </td> </tr> <tr> <td> <code>(inhibition or activation) transcription</code> </td> </tr> <tr> <td> <code>(melanoma or cancer or tumor) ~4 human</code> </td> </tr> <tr> <td> <code>(kind=A1 or kind=B1) diabet*</code> </td> </tr> <tr> <td colspan="2"> <em>operator precedence without parentheses:</em>  <code>or > and > not > ~N/~REL</code> </td> </tr> </table>  <table> <tr> <th rowspan="2">relations</th> <td> <code>bacteria ~loc gut</code> </td> </tr> <tr> <td> <code>"produit fini" ~traite maladie</code> </td> </tr> </table>  <table> <tr> <th rowspan="2">no expansion</th> <td> <code>[bacteria]</code> </td> </tr> <tr> <td> <code>[human or mouse]</code> </td> </tr> <tr> <td colspan="2"> <em>expansion is turned off inside brackets</em> </td> </tr> </table>  <p></p> </div> </div>')
//    .appendTo('body');
    
    $.get('resources/html/cheatsheet.html')
    .done(function(data) {
    	$(data)
    	.attr('id', 'help-content')
    	.css('display', 'none')
    	.appendTo('body');

        var helpDisplayed = false;
        $("#info-btn").click(function() {
            if (helpDisplayed) {
                $('#cheat-sheet').dialog('close');
            }
            else {
                var searchBtn = $('#search');
                var position = searchBtn.position();
                var dialogLeft = position.left + searchBtn.width() + 10;
                var dialogHeight = $(window).height() - 2 * 10;
                var dialogWidth = $(window).width() - dialogLeft - 10;

                $('#cheat-sheet').dialog({
                    title: 'Query syntax elements',
                    top: 10,
                    left: dialogLeft,
                    width: dialogWidth,
                    height: dialogHeight,
                    closed: false,
                    modal: false,
                    resizable: true,
                    iconCls: 'icon-info',
                    onOpen: function() {
                        helpDisplayed = true;
                    },
                    onClose: function() {
                        helpDisplayed = false;
                    }
                });

                $('#cheat-sheet').dialog('dialog').addClass('cheat-sheet-panel');
            }

        });
    })
    .fail(function(data) {
    	console.log(data);
    })
    ;

}

//- -------------------------------------------------------------------------- -
var reSpecialChars = /([\\()\[\]~"])/g;
var reOperators = /(\b)((?:and|or|not)\b)/gi;

function changeQueryAndSubmit(refineCurrentQuery, isAndOperator, newTerms, escapeSpecialChars) {
    var newQry = '';
    var joinTerms = '';
    _.each(newTerms, function(term) {
        if (escapeSpecialChars) {
            //escape special characters or reserved words contained within concept labels
            var escapedLabel = term.replace(reSpecialChars, "\\$1").replace(reOperators, "$1\\$2");
            newQry += joinTerms + '"' + escapedLabel + '"';
        } else {
            newQry += joinTerms + term;
        }
        joinTerms = isAndOperator ? ' ' : ' or ';
    });

    if (newQry.length > 0) {
        if (refineCurrentQuery) {
            var prevQuery = $("#query-input").attr("value");
            if (prevQuery.length > 0) {
                newQry = '(' + prevQuery + ') and (' + newQry + ')';
            }
        }

        $("#query-input").val(newQry);
        $("#query-form").submit();
    }
}

function queryWithOnto(refineCurrentQuery, isAndOperator) {
    hideOntoPopup();

    var concepts = _.map(ob.selectedConcepts(), function(d) {
        return d.name;
    });
    changeQueryAndSubmit(refineCurrentQuery, isAndOperator, concepts, true);

}

function refineQuery(clickedElement) {
    var term = $(clickedElement).children('input').val();
    changeQueryAndSubmit(true, true, [term], false);
}
//-- ------------------------------------------------------------------------ --
function onPageSelected(pageNumber, pageSize) {
    $("#refresh-form-startsnippet").attr("value", pageSize * (pageNumber - 1));
    $("#refresh-form-pageSize").attr("value", pageSize);
    $("#refresh-query-form").submit();
}

function onPageSizeChanged(pageSize) {
}
//-- ------------------------------------------------------------------------ --
function explnNodeFormatter(node) {
    var s = node.text;
    if (node.children) {
        s += '&nbsp;<span style=\'color:lightsteelblue\'>(' + node.children.length + ')</span>';
    }
    return s;
}
//-- ------------------------------------------------------------------------ --

var facetTabsResizer;
function facetRegionResized() {
    //the actual resizer will be plugged once JqueryEasyUI object are created
    if (facetTabsResizer != undefined) {
        facetTabsResizer();
    }
}

function intColumnSorter(a, b) {
    a = parseInt(a);
    b = parseInt(b);
    if (a === b) {
        return 0;
    } else {
        return (a > b ? 1 : -1);
    }
}

function getFacetNbFilter(tableId) {
    return function(data) {
        if (typeof data.length == 'number' && typeof data.splice == 'function') { // is array
            data = {
                total: data.length,
                rows: data
            };
        }
        if (!data.originalRows) {
            data.originalRows = (data.rows);
        }

        var limit = globalFacetTabLimits[tableId];
        if (limit != undefined) {
            data.rows = (data.originalRows.slice(0, limit));
        } else {
            data.rows = data.originalRows;
        }
        data.total = data.rows.length;
        return data;
    };
}

var globalFacetTabLimits = {}
function reloadGlobalFacetTabsData(tableId, initLoadFilter) {
    var dg = $('#' + tableId);
    var dgData = dg.datagrid('getData');
    if (initLoadFilter) {
        var loadFilter = getFacetNbFilter(tableId);
        dg.datagrid({loadFilter: loadFilter}).datagrid('loadData', dgData);
    } else {
        dg.datagrid('loadData', dgData);
    }
}

function initGlobalFacetTabs() {

    function initFacetTab() {
        var tableId = facetTableIds.pop();
        if (tableId != undefined) {
            //when the DataGrid is created from an existing table, it must be explicitely reloaded with it's own data (i.e. after the loadFilter is bound) so the loadFilter will work without errors (jquery.easyui v1.3.4)
            reloadGlobalFacetTabsData(tableId, true);

            //bind display limit buttons to click event
            $(' .nbdisp-btn[data-table-refid="' + tableId + '"]').each(function(i, v) {

                var nbDisplay = parseInt($(v).text());
                var tableSize = $('#' + tableId).datagrid('getData').rows.length;

                if (!isNaN(nbDisplay) && nbDisplay > tableSize) {
                    //disable buttons with limit above actual number of facets
                    $(v).linkbutton('disable');
                } else {
                    $(v).click(
                            function(event) {
                                //store clicked limit in a global variable to inform the load filter
                                globalFacetTabLimits[tableId] = isNaN(nbDisplay) ? undefined : nbDisplay;
                                reloadGlobalFacetTabsData(tableId, false);
                            });

                    if ($(v).linkbutton('options').selected) {
                        $(v).click();
                    }
                }
            });
            setTimeout(initFacetTab, 5);
        }
    }

    //plug the facet-grids resizer
    facetTabsResizer = $.debounce(
            function() {
                $('.facet-tab').each(function(i, v) {
                    $('#' + v.id).datagrid('resize');
                });
            }, 300);


    var facetTableIds = [];
    $('.facet-tab').each(function(i, v) {
        var tableId = $(v).attr('id');
        facetTableIds.unshift(tableId);
    });
    initFacetTab();
}
//- ------------------------------------------------------------------------- --

function initSvgLayer() {
    //create svg and auxiliary elements
    $('<div class="wrappercontainer" ><div class="dualcontainer"><div class="svgcontainer"><svg xmlns:svg="http://www.w3.org/2000/svg" xmlns="http://www.w3.org/2000/svg"></svg></div><div class="overlaycontainer"></div><div class="textcontainer"></div></div></div>')
            .prependTo('.doc-field');


    $('.wrappercontainer').each(function(i, w) {

        var wId = 'wrapper-' + i;
        $(w).attr('id', wId);
        var frag = $('#' + wId).siblings('.doc-field-fragments');

        var target = $('#' + wId + ' > .dualcontainer > .textcontainer');
        var svg = $('#' + wId + ' > .dualcontainer > .svgcontainer > svg');
        svg.attr('id', wId + '-svg');

        frag.attr('style', 'left:0;top:0;');
        $('#' + wId).siblings().appendTo(target);
    });
}
function resetSvgLayer() {
    // clear svg
    $('.wrappercontainer').each(function(i, w) {

        var wId = $(w).attr('id');
        var svgcont = $('#' + wId + ' > .dualcontainer > .svgcontainer');

        var svg = svgcont.children('svg');
        svg.empty();
        $('#' + wId + ' > .dualcontainer > .overlaycontainer').empty();
    });
}

function populateSvgLayer() {

    var lineHeight;
    var interlineSpace;
    function setInterlineSpace(textcontainer) {
        try {
            lineHeight = parseInt(textcontainer.css('line-height').match(/(\d*(\.\d*)?)px/)[1]);
            var fs = parseInt(textcontainer.css('font-size').match(/(\d*(\.\d*)?)px/)[1]);
            interlineSpace = lineHeight - fs;
        } catch (e) {
            lineHeight = 18;
            interlineSpace = 10;
        }
    }

    function appendSVGElement(svg, elementName, attrs) {
        var el = document.createElementNS('http://www.w3.org/2000/svg', elementName);
        for (var key in attrs)
            el.setAttribute(key, attrs[key]);
        svg.get(0).appendChild(el);
        return el;
    }
    function center(rect) {
        return {x: Math.round(rect.x + rect.width / 2), y: Math.round(rect.y + rect.height / 2)};
    }

    function isobarycenter(a, b) {
        return {x: Math.round((a.x + b.x) / 2), y: Math.round((a.y + b.y) / 2)};
    }

    var padding = -2;
    var entityTemplate = {stroke: '#EFEFEF', 'stroke-width': 1, fill: '#FFFFFF'};
    function computeEntityBox(svg, entityElement) {

        //return the coordinates of the first fragment of the specified element
        //(since large inlined elements can be spanned over several lines, then thier raw coordinates are those of the englobing box)
        function getRectifiedTextClips(srcElt) {

            if (srcElt != null) {
                var entity = $(srcElt);
                var position = entity.position();

                var rawCoord = {left: position.left, top: position.top, width: entity.width(), height: entity.height()};
                var firstboxLeft = rawCoord.left;
                var firstboxTop = rawCoord.top;
                var firstboxBottom = rawCoord.top + rawCoord.height;

                if (rawCoord.height > lineHeight) {

                    $('<span id="size-probe"></span>').insertBefore(srcElt);
                    var probe = $('#size-probe');
                    var probePos = probe.position();
                    if (probePos.left != rawCoord.left) {
                        firstboxLeft = probePos.left;
                        firstboxTop = probePos.top;
                        firstboxBottom = probePos.top + probe.height();
                    }
                    probe.remove();

                    var firstWidth = rawCoord.width - firstboxLeft;
                    var firstHeight = firstboxBottom - firstboxTop;

                    return {left: firstboxLeft, top: firstboxTop, width: firstWidth, height: firstHeight};
                } else {
                    return rawCoord;

                }
            } else {
                return null;
            }
        }

        var rectClips = getRectifiedTextClips(entityElement);

        return _.defaults({x: rectClips.left - padding, y: rectClips.top - padding, width: rectClips.width + 2 * padding, height: rectClips.height + 2 * padding}, entityTemplate);
    }


    var interlineOccupancies = [];
    var outboundSegmentByAnnId = {};
    var inboundSegmentByAnnId = {};
    function clearDocRelationData() {
        interlineOccupancies.length = 0;
        outboundSegmentByAnnId = {};
        inboundSegmentByAnnId = {};
    }

    function getInterlineOccupancy(interlineIndex) {
        var occupancy = interlineOccupancies[interlineIndex];
        return occupancy == undefined ? 0 : occupancy;
    }

    function incInterlineOccupancy(interlineIndex) {
        var previousOccupancy = interlineOccupancies[interlineIndex];
        if (previousOccupancy == undefined) {
            previousOccupancy = 0;
        }
        previousOccupancy++;
        interlineOccupancies[interlineIndex] = previousOccupancy;
    }

    function getSegmentIndex(segmentByAnnId, relationId, referencedAnnotationId) {
        var segments = segmentByAnnId[referencedAnnotationId];
        if (segments == undefined) {
            segments = [];
            segmentByAnnId[referencedAnnotationId] = segments;
            segments.push(relationId);
        }
        var idx = segments.indexOf(relationId);
        if (idx === -1) {
            segments.push(relationId);
            idx = segments.indexOf(relationId);
        }
        return idx;
    }

    function getOutboundSegmentIndex(relationId, referencedAnnotationId) {
        return getSegmentIndex(outboundSegmentByAnnId, relationId, referencedAnnotationId);
    }

    function getInboundSegmentIndex(relationId, referencedAnnotationId) {
        return getSegmentIndex(outboundSegmentByAnnId, relationId, referencedAnnotationId);
    }

    //-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
    var offsetIfStacked = 10;
    var thresholdForStacked = 20;
    var coreXSize = 5;
    var coreYSize = 4;
    var vertCoef = [.5, .4, .6, .3, .7, .2, .55, .45, .65, .35, .25];
    var horzCoef = [.05, .25, .45, .65, .85, .15, .35, .55, .75];

    var relTemplate = {stroke: 'rgba(255,0,255,0.7)', 'stroke-width': 1, fill: 'transparent'};
    function addRelation(svg, ovrlay, sourceElement, targetElement, relProps) {

        function togglePathHighlighting(svgId, relationId, highlight) {
            $("svg#" + svgId + " *[data-relation-id='" + relationId + "']").attr("data-highlighted", highlight ? "true" : "");
        }

        var source = computeEntityBox(svg, sourceElement);
        var target = computeEntityBox(svg, targetElement);

        var sourceCenter = center(source);
        var targetCenter = center(target);
        var relationCenter = isobarycenter(sourceCenter, targetCenter);

        var sfloor = source.y + source.height;
        var tfloor = target.y + target.height;
        var floor = Math.max(sfloor, tfloor);

        //adjust relation center

        //vertical position on the interline
        var interlineIndex = Math.round(floor / lineHeight);
        var occupancy = getInterlineOccupancy(interlineIndex);
        var verticalOffset = (2 * interlineSpace * vertCoef[occupancy % vertCoef.length]);
        incInterlineOccupancy(interlineIndex);

        relationCenter.y = Math.round(floor + verticalOffset);

        var stackedEntities = false;
        var sourceStackedAboveTarget = false;

        if (Math.abs(sourceCenter.x - targetCenter.x) < thresholdForStacked) {
            relationCenter.x = Math.min(source.x, target.x) - offsetIfStacked;
            stackedEntities = true;
            sourceStackedAboveTarget = (source.y < target.y);
        }

        // horizontal position of the outbound segments
        var direction = (relationCenter.x < sourceCenter.x) ? -1 : 1;
        var outboundSegmentIndex = getOutboundSegmentIndex(relProps.relationId, relProps.sourceId);

        var outboundHorizontalOffset = (source.width / 2 * horzCoef[outboundSegmentIndex % horzCoef.length]);
        sourceCenter.x += direction * outboundHorizontalOffset;

        // horizontal position of the inbound segments
        var direction = (relationCenter.x < targetCenter.x) ? -1 : 1;
        var inboundSegmentIndex = getInboundSegmentIndex(relProps.relationId, relProps.targetId);

        var inboundHorizontalOffset = (target.width / 2 * horzCoef[inboundSegmentIndex % horzCoef.length]);
        targetCenter.x += direction * inboundHorizontalOffset;

        var linePoints = [
            {x: sourceCenter.x, y: sourceCenter.y},
            stackedEntities && sourceStackedAboveTarget ?
                    {x: relationCenter.x, y: sourceCenter.y} :
                    {x: sourceCenter.x, y: relationCenter.y},
            {x: relationCenter.x, y: relationCenter.y},
            stackedEntities && !sourceStackedAboveTarget ?
                    {x: relationCenter.x, y: targetCenter.y} :
                    {x: targetCenter.x, y: relationCenter.y},
            {x: targetCenter.x, y: targetCenter.y}
        ];

        var path = '';
        _.each(linePoints, function(p, i) {
            path += (i === 0 ? 'M ' : 'L ') + p.x + ',' + p.y;
        });

        var path = appendSVGElement(svg, 'path', _.defaults({d: path}, relTemplate));
        path.setAttribute("class", "relation relation_" + relProps.explanationId);
        var relationId = relProps.relationId;
        path.setAttribute("data-relation-id", relationId);

        var director = (sourceCenter.x < targetCenter.x) ? 1 : -1;
        var corePoints = [
            {x: relationCenter.x - director * coreXSize, y: relationCenter.y - director * coreYSize},
            {x: relationCenter.x + director * coreXSize, y: relationCenter.y},
            {x: relationCenter.x - director * coreXSize, y: relationCenter.y + director * coreYSize}
        ];
        path = '';
        _.each(corePoints, function(p, i) {
            path += (i === 0 ? 'M ' : ' L ') + p.x + ',' + p.y;
        });
        path += ' Z';
        var core = appendSVGElement(svg, 'path', _.defaults({d: path, fill: 'silver'}, relTemplate));
        core.setAttribute("class", "relation relation_" + relProps.explanationId);
        core.setAttribute("data-relation-id", relationId);

        var title = _.escape('"' + sourceElement.text() + '"(' + relProps.sourceRole + ') >[' + relProps.relationType + ']> "' + targetElement.text() + '"(' + relProps.targetRole + ')');

        var coreClip = {left: relationCenter.x - coreXSize, top: relationCenter.y - coreYSize, width: 2 * coreXSize, height: 2 * coreYSize};
        var coreOverlay = $('<div class="core-rel-overlay" title="' + title + '" style="left:' + coreClip.left + 'px;top:' + coreClip.top + 'px;width:' + coreClip.width + 'px;height:' + coreClip.height + 'px;"></span>').appendTo(ovrlay);
        var svgId = svg.attr("id");
        coreOverlay
                .mouseout(function() {
            togglePathHighlighting(svgId, relationId, false);
        })
                .mouseover(function() {
            togglePathHighlighting(svgId, relationId, true);
        });

        var srcBox = appendSVGElement(svg, 'rect', source);
        srcBox.setAttribute("data-relation-id", relationId);
        srcBox.setAttribute("class", "relation relation_" + relProps.explanationId);

        var tgtBox = appendSVGElement(svg, 'rect', target);
        tgtBox.setAttribute("data-relation-id", relationId);
        tgtBox.setAttribute("class", "relation relation_" + relProps.explanationId);
    }

    var wrapperIds = [];
    function loopProcessSnippets() {

        var wId = wrapperIds.pop();
        if (wId != undefined) {
            var svgcont = $('#' + wId + ' > .dualcontainer > .svgcontainer');
            var target = $('#' + wId + ' > .dualcontainer > .textcontainer');

            setInterlineSpace(target);
            clearDocRelationData();

            var svg = svgcont.children('svg');

            //reset size of svg container
            var frag = target.children('.doc-field-fragments');
            //Add extra space for any relation connecting an entity on the last line
            svg.attr('height', frag.height() + interlineSpace * 2);
            svg.attr('width', frag.width());
            svgcont.height(frag.height() + interlineSpace * 2);
            svgcont.width(frag.width());

            //redraw svg
            svg.empty();

            var ovrlaycontainer = $('#' + wId + ' > .dualcontainer > .overlaycontainer');
            ovrlaycontainer.empty();

            //retrieve data to create relations
            target.find('.flattened-relation').each(function(j, r) {

                var sourceId = $(r).attr('data-arg-1-ref');
                var targetId = $(r).attr('data-arg-2-ref');
                if (sourceId != undefined && targetId != undefined) {
                    var relProps = {
                        relationType: $(r).attr('data-relation'),
                        relationId: $(r).attr('data-annotation'),
                        explanationId: $(r).attr('data-explanation'),
                        sourceId: sourceId,
                        targetId: targetId,
                        sourceRole: $(r).attr('data-arg-1-role'),
                        targetRole: $(r).attr('data-arg-2-role')
                    };

                    var s = $(frag).find("span[data-annotation-id='" + sourceId + "']");
                    var t = $(frag).find("span[data-annotation-id='" + targetId + "']");
                    if (s != undefined && s.length != 0 && t != undefined && t.length != 0) {
                        addRelation(svg, ovrlaycontainer, s, t, relProps);
                    }
                }
            });
            setTimeout(loopProcessSnippets, 5);
        }
    }


    //-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- --
    // reset svg
    wrapperIds = [];
    $('.wrappercontainer').each(function(i, w) {
        var wId = $(w).attr('id');
        wrapperIds.unshift(wId);
    });
    wrapperIds.sort().reverse();

    loopProcessSnippets();
}

var svgLayerSetter;

function snippetRegionResized() {
    populateSvgLayer();
}
//-- ------------------------------------------------------------------------ --

$(document).ready(function() {


    //set focus on query field and caret at the end of text
    var querytext = $('#query-input').val();
    $('#query-input').focus().val('').val(querytext);

    //restore default mouse pointer (was set by previous form submit)
    $("body").css("cursor", "default");

    $("#query-form").submit(function(event) {
        $('#error-box').hide();
        $("body").css("cursor", "progress");
    });
    $("#refresh-query-form").submit(function(event) {
        $('#error-box').hide();
        $("body").css("cursor", "progress");
    });

    initHelp();
    initOntoBrowser();

    svgLayerSetter = $.debounce(populateSvgLayer, 300);
    initSvgLayer();

    $(window).resize(function() {
        resetSvgLayer();
        svgLayerSetter();
    });

    //
    initGlobalFacetTabs();

    //button expand/collaps for document facets
    $('.doc-facet-btn').each(function(i, button) {
        var facetContainer = $(button).siblings('.doc-facets-container');
        $(facetContainer).css('display', 'none');

        if ($(facetContainer).html()) {

            $(button).click({button: button, container: facetContainer.get(0)},
            function(event) {
                var facetContainer = event.handleObj.data.container;

                var display = $(facetContainer).css('display');
                if (display == 'none') {
                    $(button).find('.layout-button-right').removeClass("layout-button-right").addClass("layout-button-left");
                    $(facetContainer).css('display', 'block');
                } else {
                    $(button).find('.layout-button-left').removeClass("layout-button-left").addClass("layout-button-right");
                    $(facetContainer).css('display', 'none');
                }
            });
        } else {
            //hide expand button if doc facet is empty
            $(button).css('display', 'none');
        }
    });

    setTimeout(populateSvgLayer, 500);
});
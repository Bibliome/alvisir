<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version = "1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

    <xsl:output method="html" indent="yes" doctype-public="html"/>

    <xsl:key name="explById" match="search-result/query-expansion/explanation" use="@id" />

    <xsl:param name="style-url">../css/alvisir.css</xsl:param>
    <xsl:param name="extrastyle-url"></xsl:param>


    <!-- dynamic parameters -->
    <xsl:param name="search-url"></xsl:param>
    <xsl:param name="tabular-url"></xsl:param>
    <xsl:param name="query-param"></xsl:param>
    <xsl:param name="start-snippet-param"></xsl:param>
    <xsl:param name="snippet-count-param"></xsl:param>

    <xsl:param name="pageSize"></xsl:param>
    <xsl:param name="pageNumber"></xsl:param>
    <xsl:param name="startSnippet"></xsl:param>
    <!--  -->
    <!-- configuration dependant parameters -->
    <xsl:param name="title">AlvisIR Search</xsl:param>
    <xsl:param name="copyright-owner">INRA</xsl:param>
    <xsl:param name="copyright-years">2013</xsl:param>
    <xsl:param name="about-url">../about.jsp</xsl:param>
    <xsl:param name="search-button-text">Search</xsl:param>

    <xsl:param name="onto-names"/>

    <xsl:param name="subpaths-header"/>
    <xsl:param name="synonyms-header"/>

    <xsl:param name="title-field">title</xsl:param>
    <xsl:param name="title-url-field"/>
    <xsl:param name="title-url-prefix"/>

    <xsl:param name="outlink_1-image"/>
    <xsl:param name="outlink_1-hint"/>
    <xsl:param name="outlink_1-url-field"/>
    <xsl:param name="outlink_1-url-prefix"/>

    <xsl:param name="outlink_2-image"/>
    <xsl:param name="outlink_2-hint"/>
    <xsl:param name="outlink_2-url-field"/>
    <xsl:param name="outlink_2-url-prefix"/>

    <xsl:param name="doc-field_1"/>
    <xsl:param name="doc-field_1-header"/>
    <xsl:param name="doc-field_2"/>
    <xsl:param name="doc-field_2-header"/>
    <xsl:param name="doc-field_3"/>
    <xsl:param name="doc-field_3-header"/>
    <xsl:param name="doc-field_4"/>
    <xsl:param name="doc-field_4-header"/>
    <xsl:param name="doc-field_5"/>
    <xsl:param name="doc-field_5-header"/>
    <xsl:param name="doc-score-header"/>
    <!--  -->


    <xsl:param name="footer">
        <span id="copyright">
            <a target="_blank">
                <xsl:attribute name="href">
                    <xsl:copy-of select="$about-url"/>
                </xsl:attribute>
                <xsl:value-of select="concat('Copyright ', $copyright-owner, ', ', $copyright-years)"/>
            </a>
        </span>
    </xsl:param>

    <!-- Main Container -->
    <xsl:template match="/">
        <html>

            <head>
                <title>
                    <xsl:value-of select="$title"/>
                </title>
                
                <meta name="robots" content="index, nofollow" />

                <link rel="favicon" type="image/png" href="../images/AlvisIR_icon.ico" />
                <link rel="icon" type="image/png" href="../images/AlvisIR_icon.png" />
                <script src="http://cdnjs.cloudflare.com/ajax/libs/jquery/2.1.0/jquery.min.js"></script>
                <script type="text/javascript" src="../js/jquery-3.1.1.min.js" />
                <!--
                <script src="http://codeorigin.jquery.com/jquery-2.0.3.min.js"></script>
                -->


                <script type="text/javascript" src="http://code.jquery.com/ui/1.10.3/jquery-ui.js"></script>
                <link rel="stylesheet" href="http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css" />
                <!--
                <script type="text/javascript" src="http://codeorigin.jquery.com/ui/1.10.3/jquery-ui.min.js"></script>
                <link rel="stylesheet" href="http://codeorigin.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css" />
                -->
                <link rel="stylesheet" type="text/css" href="../js/jqueryeasyui/themes/metro/easyui.css"/>
                <link rel="stylesheet" type="text/css" href="../js/jqueryeasyui/themes/icon.css"/>
                <script type="text/javascript" src="../js/jqueryeasyui/jquery.easyui.1.3.5.min.js"></script>

                <link rel="stylesheet" type="text/css">
                    <xsl:attribute name="href">
                        <xsl:value-of select="$style-url"/>
                    </xsl:attribute>
                </link>
                <xsl:if test="$extrastyle-url">
                    <link rel="stylesheet" type="text/css">
                        <xsl:attribute name="href">
                            <xsl:value-of select="$extrastyle-url"/>
                        </xsl:attribute>
                    </link>
                </xsl:if>
                <script type="text/javascript" src="../js/plugins/jquery.debounce-1.0.5.js"></script>
                <script type="text/javascript" src="../js/alvisir.js"></script>
            </head>

            <body class="easyui-layout">

                <script src="http://cdnjs.cloudflare.com/ajax/libs/d3/3.2.2/d3.v3.min.js" charset="utf-8"></script>
                <script src="../js/d3.js" charset="utf-8"></script>
                <script src="http://cdnjs.cloudflare.com/ajax/libs/underscore.js/1.5.1/underscore-min.js"></script>
                <script src="../js/underscore.min.js"></script>
                <script type="text/javascript" src="../js/ontobrowser/ontobrowser.js" ></script>
                <link type="text/css" rel="stylesheet" href="../js/ontobrowser/ontobrowser.css"/>
                
                <div id="onto-box" style="display:none;">
                    <div id="onto-container" style='padding: 5px;'>
                        <div id="onto-toolbar" style="margin-bottom:1px; border-bottom:1px dotted #f0f0f0; padding:3px; height: 35px;">
                            <div style="float:left; max-width:50%; margin-top:5px;">
                                <span id="onto-name" style="background-color:lightsteelblue; padding:5px; margin-right:15px;"></span>
                                <div class="air-btn onto-btn" id="refine-with-onto">Refine</div>
                                <span style="margin-left:2px;"></span>
                                <div class="air-btn onto-btn" id="search-with-onto">Search</div>
                                <span style="margin-left: 15px;">Query operator:</span>
                                <a id="onto-operator-and" href="#" class="easyui-linkbutton" data-options="toggle:true,group:'ontooperator',plain:true,selected:true">And</a>
                                <a href="#" class="easyui-linkbutton" data-options="toggle:true,group:'ontooperator',plain:true">Or</a>
                            </div>
                            <div style="float:right; width:50%; margin-top:5px;">
                                <div id="scale-slider"></div>
                            </div>
                        </div>
                        <div>
                            <div id="onto-selconcept">
                                <div id="onto-selconcept-bar" style="margin-bottom:2px; padding:3px; height: 54px;">
                                    <div id="concept-searchgroup" style="margin-left:0; margin-right:0;">
                                        <input id="concept-searchbox" class="easyui-searchbox" data-options="prompt:'search for a concept',searcher:doConceptSearch" style="width:100%;">
                                        </input>
                                        <div id="concept-searchresult-popup" >
                                        </div>
                                    </div>
                                    <br/>
                                    <div class="air-btn onto-btn" id="clear-selected-concept">clear selection</div>
                                </div>
                                <div id="onto-selected-container">
                                    <div id="onto-selected-list"></div>
                                </div>
                            </div>
                            <div id="graph-container" style="padding:3px;">
                                <div id="scroll-up" class="scroller-helper" style="cursor: N-resize;"></div>
                                <div id="scroll-down" class="scroller-helper" style="cursor: S-resize;"></div>
                                <div id="scroll-left" class="scroller-helper" style="cursor: W-resize;"></div>
                                <div id="scroll-right" class="scroller-helper" style="cursor: E-resize;"></div>
                                <div id="svg-container" style="overflow:auto; ">
                                    <div  id="centered-container" style="margin:auto;">
                                        <svg id="svg-root" width="100" height="100" viewBox="0 0 800 600"  xmlns="http://www.w3.org/2000/svg">
                                        </svg>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <xsl:choose>
                    <xsl:when test="search-result/messages/message">
                        <div data-options="region:'north', border:true, collapsible:false" style="height:180px;">
                            <div class="Banner">
                                <div class="Right"></div>
                                <div class="Left"></div>
                            </div>
                        </div>
                        <div data-options="region:'center', border:true, collapsible:false">

                            <div class="Dialog">
                                <a href="{$search-url}">
                                    <img alt="Logo" src="resources/images/alvis.png" style="width: 329px; height: 156px;"/>
                                </a>
                            </div>

                            <xsl:call-template name="generate-query-form-box">
                                <xsl:with-param name="style">margin-top:10px;</xsl:with-param>
                            </xsl:call-template>

                            <div id="error-box">
                                <div>
                                    <xsl:value-of select="search-result/messages/message/text"/>
                                </div>
                                <div id="error-reason">
                                    <xsl:value-of select="search-result/messages/message/exception"/>
                                </div>
                            </div>
                        </div>
                        <div data-options="region:'south', border:true, collapsible:false" style="height:24px;">
                            <div id="footer">
                                <xsl:copy-of select="$footer"/>
                            </div>
                        </div>
                    </xsl:when>

                    <xsl:when test="search-result/query/query-string != ''">

                        <div data-options="region:'north', border:true, collapsible:false" style="height:110px;">
                            <div class="Banner">
                                <div class="Right"></div>
                                <div class="Left"></div>
                            </div>

                            <div style="width:100%; height:100%; overflow:hidden;">
                                <div style="width:840px; height: 95px; margin-top: 5px; margin-left: auto; margin-right: auto;overflow:visible;">
                                 
                                    <div class="Col">
                                        <a class="Logo" href="{$search-url}">
                                            <img alt="Logo" src="resources/images/alvis.png" style="width: 200px; height: 95px;"/>
                                        </a>
                                    </div>
                                    <div class="Col">
                                        <xsl:call-template name="generate-query-form-box">
                                            <xsl:with-param name="style">margin-top:40px;</xsl:with-param>
                                        </xsl:call-template>
                                        <div style="display:none">
                                            <form id="refresh-query-form" method="get">
                                                <xsl:attribute name="action">
                                                    <xsl:value-of select="$search-url"/>
                                                </xsl:attribute>
                                                <input type="hidden">
                                                    <xsl:attribute name="name">
                                                        <xsl:value-of select="$query-param"/>
                                                    </xsl:attribute>
                                                    <xsl:attribute name="value">
                                                        <xsl:value-of select="normalize-space(search-result/query/query-string)"/>
                                                    </xsl:attribute>
                                                </input>
                                                <input id="refresh-form-pageSize" type="hidden">
                                                    <xsl:attribute name="name">
                                                        <xsl:value-of select="$snippet-count-param"/>
                                                    </xsl:attribute>
                                                    <xsl:attribute name="value">
                                                        <xsl:value-of select="$pageSize"/>
                                                    </xsl:attribute>
                                                </input>
                                                <input id="refresh-form-startsnippet" type="hidden">
                                                    <xsl:attribute name="name">
                                                        <xsl:value-of select="$start-snippet-param"/>
                                                    </xsl:attribute>
                                                    <xsl:attribute name="value">
                                                        <xsl:value-of select="$startSnippet"/>
                                                    </xsl:attribute>
                                                </input>

                                                <input type="submit">
                                                </input>
                                            </form>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div data-options="region:'south', border:true, collapsible:false" style="height:24px;">
                            <div id="footer">
                                <xsl:copy-of select="$footer"/>
                            </div>
                        </div>


                        <div style="width:260px;">
                            <xsl:attribute name="data-options">
                                <xsl:text>region:'west', border:true, collapsible:true, split:true, onResize:function(w,h){facetRegionResized(w,h);}</xsl:text>
                            </xsl:attribute>
                            <xsl:if test="search-result/query/query-string != ''">
                                <div id="global-facets">
                                  
                                    <form id="query-tabular-form" method="get" target="_blank" >
                                        <xsl:attribute name="action">
                                            <xsl:value-of select="$tabular-url"/>
                                        </xsl:attribute>
                
                                        <input type="hidden">
                                            <xsl:attribute name="name">
                                                <xsl:value-of select="$query-param"/>
                                            </xsl:attribute>
                                            <xsl:attribute name="value">
                                                <xsl:value-of select="normalize-space(search-result/query/query-string)"/>
                                            </xsl:attribute>
                                        </input>
                                        <input type="hidden">
                                            <xsl:attribute name="name">
                                                <xsl:value-of select="$snippet-count-param"/>
                                            </xsl:attribute>
                                            <xsl:attribute name="value">
                                                <xsl:value-of select="$pageSize"/>
                                            </xsl:attribute>
                                        </input>
                                        <input  type="hidden">
                                            <xsl:attribute name="name">
                                                <xsl:value-of select="$start-snippet-param"/>
                                            </xsl:attribute>
                                            <xsl:attribute name="value">
                                                <xsl:value-of select="$startSnippet"/>
                                            </xsl:attribute>
                                        </input>         
                                        <input id="facet-export-btn" border="0" src="../images/table-export.png" type="image" value="submit" align="middle" alt="export facets as table">
                                        </input>                                               
                                    </form>                               
                                    
                                    <xsl:apply-templates select="search-result/facets/facet" mode="global"/>
                                </div>
                            </xsl:if>
                        </div>

                        <div data-options="region:'east', border:true, collapsible:true, split:true" style="width:240px;">
                            <xsl:if test="search-result/query/query-string != ''">
                                <div id="query-expansion">
                                    <xsl:apply-templates select="search-result/query-expansion//explanation[@class != 'fr.inra.mig_bibliome.alvisir.core.expand.explanation.CompositeExpansionExplanation']"/>
                                </div>
                            </xsl:if>
                        </div>

                        <div data-options="region:'center', border:false">
                            <div class="easyui-layout" data-options="fit:true">
                                <div data-options="region:'north', border:true, collapsible:false, minHeight:34" style="">
                                    <div class="easyui-pagination" >
                                        <xsl:attribute name="data-options">
                                            <xsl:variable name="displayMsg">displayMsg:"[{from} to {to} of {total}]"</xsl:variable>
                                            <xsl:value-of select="concat('total:', ./search-result/hits/@total, ',pageSize:', $pageSize, ',pageNumber:', $pageNumber, ',', $displayMsg, ', onRefresh:onPageSelected, onSelectPage:onPageSelected, onChangePageSize:onPageSizeChanged')"/>
                                        </xsl:attribute>
                                    </div>
                                </div>
                                <div>
                                    <xsl:attribute name="data-options">
                                        <xsl:text>region:'center', border:false, onResize:function(w,h){snippetRegionResized(w,h);}</xsl:text>
                                    </xsl:attribute>
                                    <div id="search-result">
                                        <div id="snippets">
                                            <xsl:apply-templates select="search-result/snippets/doc"/>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </xsl:when>
                    <xsl:otherwise>

                        <div data-options="region:'north', border:true, collapsible:false" style="height:180px;">
                            <div class="Banner">
                                <div class="Right"></div>
                                <div class="Left"></div>
                            </div>
                        </div>
                        <div data-options="region:'center', border:true, collapsible:false">

                            <div class="Dialog">
                                <a href="{$search-url}">
                                    <img alt="Logo" src="resources/images/alvis.png" style="width: 329px; height: 156px;"/>
                                </a>
                            </div>

                            <xsl:call-template name="generate-query-form-box">
                                <xsl:with-param name="style">margin-top:10px;</xsl:with-param>
                            </xsl:call-template>

                        </div>
                        <div data-options="region:'south', border:true, collapsible:false" style="height:20px;">
                            <div id="footer">
                                <xsl:copy-of select="$footer"/>
                            </div>
                        </div>
                    </xsl:otherwise>
                </xsl:choose>

            </body>
        </html>
    </xsl:template>

    <!-- Global facets -->
    <xsl:template match="facet" mode="global">

        <xsl:variable name="tableId" select="generate-id(@name)"/>
        <xsl:variable name="tableToolbarId" select="concat($tableId, '-toolbar')"/>

        <div class="global-facet" style="padding-right: 3px;">

            <table class="easyui-datagrid facet-tab" title="{@name}" >
                <xsl:attribute name="id">
                    <xsl:value-of select="$tableId"/>
                </xsl:attribute>
                <xsl:attribute name="data-options">
                    <xsl:text>fitColumns:true,singleSelect:true,remoteSort:false,stripped:true,showFooter:false,toolbar:'#</xsl:text>
                    <xsl:value-of select="$tableToolbarId"/>
                    <xsl:text>'</xsl:text>
                </xsl:attribute>

                <thead>
                    <tr>
                        <th data-options="field:'name', width:220, sortable:true">
                            <span class="global-facet-header" title="facet value">facet value</span>
                        </th>
                        <th data-options="field:'freq', align:'right', width:70, resizable:true, sortable:true, sorter:intColumnSorter">
                            <span class="global-facet-header" title="facet frequency" >freq.</span>
                        </th>
                        <th data-options="field:'docfreq', align:'right', width:70, resizable:true, sortable:true, sorter:intColumnSorter">
                            <span class="global-facet-header" title="document frequency">doc.</span>
                        </th>
                    </tr>
                </thead>
                <tbody>
                    <xsl:for-each select="term">
                        <tr>
                            <td>
                                <a class="doc-facet-anchor" href="#" onClick="refineQuery(this);">
                                    <input type="hidden">
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="@sub-query"/>
                                        </xsl:attribute>
                                    </input>
                                    <xsl:value-of select="@label"/>
                                </a>
                            </td>
                            <td>
                                <xsl:value-of select="@count"/>
                            </td>
                            <td>
                                <xsl:value-of select="@docs"/>
                            </td>
                        </tr>
                    </xsl:for-each>
                </tbody>
            </table>
            <div>
                <xsl:attribute name="id">
                    <xsl:value-of select="$tableToolbarId"/>
                </xsl:attribute>
                <div class="facet-tab-nbdisp" >
                    <a href="#" class="easyui-linkbutton nbdisp-btn">
                        <xsl:attribute name="data-table-refid">
                            <xsl:value-of select="$tableId"/>
                        </xsl:attribute>
                        <xsl:attribute name="data-options">
                            <xsl:text>toggle:true,plain:true,group:'</xsl:text>
                            <xsl:value-of select="concat($tableId, '-dispgroup')"/>
                            <xsl:text>'</xsl:text>
                        </xsl:attribute>
                        <xsl:text>All</xsl:text>
                    </a>
                    <a href="#" class="easyui-linkbutton nbdisp-btn" >
                        <xsl:attribute name="data-table-refid">
                            <xsl:value-of select="$tableId"/>
                        </xsl:attribute>
                        <xsl:attribute name="data-options">
                            <xsl:text>toggle:true,plain:true,group:'</xsl:text>
                            <xsl:value-of select="concat($tableId, '-dispgroup')"/>
                            <xsl:text>'</xsl:text>
                        </xsl:attribute>
                        <xsl:text>20</xsl:text>
                    </a>
                    <a href="#" class="easyui-linkbutton nbdisp-btn" >
                        <xsl:attribute name="data-table-refid">
                            <xsl:value-of select="$tableId"/>
                        </xsl:attribute>
                        <xsl:attribute name="data-options">
                            <xsl:text>toggle:true,plain:true,group:'</xsl:text>
                            <xsl:value-of select="concat($tableId, '-dispgroup')"/>
                            <xsl:text>',selected:true</xsl:text>
                        </xsl:attribute>
                        <xsl:text>10</xsl:text>
                    </a>
                </div>
            </div>
        </div>
    </xsl:template>

    <!-- Document snippets -->
    <xsl:template match="doc">
        <div class="doc-snippet">
            <div class="doc-header doc-field">
                <xsl:if test="field[@name = $outlink_2-url-field]/fragment">
                  <div class="outlink">
                        <a target="_blank">
                            <xsl:attribute name="href">
                                <xsl:value-of select="concat($outlink_2-url-prefix, field[@name = $outlink_2-url-field]/fragment)"/>
                            </xsl:attribute>
                            <img>
                                <xsl:attribute name="src">
                                    <xsl:value-of select="$outlink_2-image"/>
                                </xsl:attribute>
                                <xsl:attribute name="title">
                                    <xsl:value-of select="$outlink_2-hint"/>
                                </xsl:attribute>
                            </img>
                        </a>
                    </div>
                </xsl:if>
                <xsl:if test="field[@name = $outlink_1-url-field]/fragment">
                    <div class="outlink">
                        <a target="_blank">
                            <xsl:attribute name="href">
                                <xsl:value-of select="concat($outlink_1-url-prefix, field[@name = $outlink_1-url-field]/fragment)"/>
                            </xsl:attribute>
                            <img>
                                <xsl:attribute name="src">
                                    <xsl:value-of select="$outlink_1-image"/>
                                </xsl:attribute>
                                <xsl:attribute name="title">
                                    <xsl:value-of select="$outlink_1-hint"/>
                                </xsl:attribute>
                            </img>
                        </a>
                    </div>
                </xsl:if>

                <span class="doc-ord">
                    <xsl:value-of select="position() + /search-result/hits/@first"/>
                </span>
                <xsl:choose>
                    <xsl:when test="$title-url-field!=''">
                        <a class="doc-title doc-field-fragments">
                            <xsl:attribute name="href">
                                <xsl:value-of select="concat($title-url-prefix, field[@name = $title-url-field]/fragment)"/>
                            </xsl:attribute>
                            <xsl:apply-templates select="field[@name = $title-field]/fragment"/>
                        </a>
                    </xsl:when>
                    <xsl:otherwise>
                        <span class="doc-title doc-field-fragments">
                            <xsl:apply-templates select="field[@name = $title-field]/fragment"/>
                        </span>
                    </xsl:otherwise>
                </xsl:choose>

		                <div style="display:none;">
                    <ul>
                        <xsl:for-each select="field[@name = $title-field]/fragment//highlight[@ord='0']">
                            <xsl:if test="key('explById',@explanation)[@class = 'fr.inra.mig_bibliome.alvisir.core.expand.explanation.RelationMatchExplanation']">
                                <xsl:variable name="relation">
                                    <xsl:value-of select="key('explById',@explanation)/@relation"/>
                                </xsl:variable>
                                <li class="flattened-relation">
                                    <xsl:attribute name="data-relation">
                                        <xsl:value-of select="$relation"/>
                                    </xsl:attribute>
                                    <xsl:for-each select="./@*[name() = 'annotation' or name() = 'explanation']">
                                        <xsl:variable name="attrName" select="concat('data-', name())"/>
                                        <xsl:attribute name="{$attrName}">
                                            <xsl:value-of select="."/>
                                        </xsl:attribute>
                                    </xsl:for-each>
                                    <xsl:for-each select="arg">
                                        <xsl:variable name="attrPrefix" select="concat('data-arg-', position(), '-')"/>
                                        <xsl:for-each select="./@*[name() = 'role' or name() = 'ref']">
                                            <xsl:variable name="attrName" select="concat($attrPrefix, name())"/>
                                            <xsl:attribute name="{$attrName}">
                                                <xsl:value-of select="."/>
                                            </xsl:attribute>
                                        </xsl:for-each>
                                    </xsl:for-each>
                                    Relation:<xsl:value-of select="$relation"/>
                                </li>
                            </xsl:if>
                        </xsl:for-each>
                    </ul>
                </div>

            </div>

            <div class="doc-score">
                <span class="doc-score-header">
                    <xsl:value-of select="$doc-score-header"/>
                </span>
                <span class="doc-score-value">
                    <xsl:value-of select="@score"/>
                </span>
            </div>

            <xsl:call-template name="doc-field">
                <xsl:with-param name="doc" select="."/>
                <xsl:with-param name="header" select="$doc-field_1-header"/>
                <xsl:with-param name="field-name" select="$doc-field_1"/>
            </xsl:call-template>

            <xsl:call-template name="doc-field">
                <xsl:with-param name="doc" select="."/>
                <xsl:with-param name="header" select="$doc-field_2-header"/>
                <xsl:with-param name="field-name" select="$doc-field_2"/>
            </xsl:call-template>

            <xsl:call-template name="doc-field">
                <xsl:with-param name="doc" select="."/>
                <xsl:with-param name="header" select="$doc-field_3-header"/>
                <xsl:with-param name="field-name" select="$doc-field_3"/>
            </xsl:call-template>

            <xsl:call-template name="doc-field">
                <xsl:with-param name="doc" select="."/>
                <xsl:with-param name="header" select="$doc-field_4-header"/>
                <xsl:with-param name="field-name" select="$doc-field_4"/>
            </xsl:call-template>

            <xsl:call-template name="doc-field">
                <xsl:with-param name="doc" select="."/>
                <xsl:with-param name="header" select="$doc-field_5-header"/>
                <xsl:with-param name="field-name" select="$doc-field_5"/>
            </xsl:call-template>


            <div class="doc-facets">
                <a href="#" class="easyui-linkbutton doc-facet-btn" data-options="plain:true,iconCls:'layout-button-right'"></a>
                <div class="doc-facets-container">
                    <xsl:apply-templates select="facets/facet" mode="doc"/>
                </div>
            </div>
        </div>
    </xsl:template>

    <xsl:template name="doc-field">
        <xsl:param name="doc"/>
        <xsl:param name="header"/>
        <xsl:param name="field-name"/>

        <xsl:if test="($field-name != '') and ($doc/field[@name = $field-name]/fragment)">
            <xsl:variable name="class">
                <xsl:value-of select="concat('doc-field-', $field-name)"/>
            </xsl:variable>
            <div>
                <xsl:attribute name="class">
                    <xsl:value-of select="concat($class, ' doc-field')"/>
                </xsl:attribute>
                <span>
                    <xsl:attribute name="class">
                        <xsl:value-of select="concat($class, '-header', ' doc-field-header')"/>
                    </xsl:attribute>
                    <xsl:value-of select="$header"/>
                </span>
                <span>
                    <xsl:attribute name="class">
                        <xsl:value-of select="concat($class, '-fragments', ' doc-field-fragments')"/>
                    </xsl:attribute>
                    <xsl:apply-templates select="field[@name = $field-name]/fragment"/>
                </span>
              
                <div style="display:none;">
                    <ul>
                        <xsl:for-each select="field[@name = $field-name]/fragment//highlight[@ord='0']">
                            <xsl:if test="key('explById',@explanation)[@class = 'fr.inra.mig_bibliome.alvisir.core.expand.explanation.RelationMatchExplanation']">
                                <xsl:variable name="relation">
                                    <xsl:value-of select="key('explById',@explanation)/@relation"/>
                                </xsl:variable>
                                <li class="flattened-relation">
                                    <xsl:attribute name="data-relation">
                                        <xsl:value-of select="$relation"/>
                                    </xsl:attribute>
                                    <xsl:for-each select="./@*[name() = 'annotation' or name() = 'explanation']">
                                        <xsl:variable name="attrName" select="concat('data-', name())"/>
                                        <xsl:attribute name="{$attrName}">
                                            <xsl:value-of select="."/>
                                        </xsl:attribute>
                                    </xsl:for-each>
                                    <xsl:for-each select="arg">
                                        <xsl:variable name="attrPrefix" select="concat('data-arg-', position(), '-')"/>
                                        <xsl:for-each select="./@*[name() = 'role' or name() = 'ref']">
                                            <xsl:variable name="attrName" select="concat($attrPrefix, name())"/>
                                            <xsl:attribute name="{$attrName}">
                                                <xsl:value-of select="."/>
                                            </xsl:attribute>
                                        </xsl:for-each>
                                    </xsl:for-each>
                                    Relation:<xsl:value-of select="$relation"/>
                                </li>
                            </xsl:if>
                        </xsl:for-each>
                    </ul>
                </div>
            </div>
        </xsl:if>
    </xsl:template>

    <xsl:template match="fragment">
    	<div class="frag">
	        <xsl:apply-templates select="highlight|text()"/>
	    </div>
    </xsl:template>

    <xsl:template match="highlight">
        <xsl:variable name="expl">
            <xsl:value-of select="@explanation"/>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="key('explById',$expl)[@class != 'fr.inra.mig_bibliome.alvisir.core.expand.explanation.RelationMatchExplanation']">
                <span>
                    <xsl:attribute name="class">
                        <xsl:value-of select="concat('highlight_', @explanation)"/>
                    </xsl:attribute>
                    <xsl:attribute name="data-annotation-id">
                        <xsl:value-of select="@annotation"/>
                    </xsl:attribute>
                    <xsl:choose>
                        <xsl:when test="./prop">
                            <span class="easyui-tooltip">
                                <xsl:attribute name="title">
                                    <xsl:value-of select="concat(./prop/@key, ' : ', ./prop/@value)"/>
                                </xsl:attribute>                        
                                <xsl:apply-templates select="highlight|text()"/>
                            </span>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:apply-templates select="highlight|text()"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </span>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="highlight|text()"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="text()">
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="facet" mode="doc">
        <xsl:if test="term">
            <table class="doc-facet">
                <thead class="doc-facet-name" colspan="2">
                    <xsl:value-of select="@name"/>
                </thead>
                <tbody class="doc-facet-terms">
                    <xsl:for-each select="term">
                        <tr class="doc-facet-line">
                            <td class="doc-facet-term">
                                <a class="doc-facet-anchor" href="#" onClick="refineQuery(this);">
                                    <input type="hidden">
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="@sub-query"/>
                                        </xsl:attribute>
                                    </input>
                                    <xsl:value-of select="@label"/>
                                </a>
                            </td>
                            <td class="doc-facet-freq">
                                <xsl:value-of select="@count"/>
                            </td>
                        </tr>
                    </xsl:for-each>
                </tbody>
            </table>
        </xsl:if>
    </xsl:template>


    <!-- Expansion explanations -->
    <xsl:template match="explanation">
        <div class="explanation">
            <xsl:attribute name="id">
                <xsl:value-of select="concat('explanation_', @id)"/>
            </xsl:attribute>

            <xsl:variable name="field">
                <xsl:choose>
                    <xsl:when test="@field = /search-result/query/@default-field">
                        <xsl:text/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="concat(@field, '=')"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>

            <xsl:variable name="label">
                <xsl:choose>
                    <xsl:when test="@label and @type">
                        <xsl:value-of select="concat(@label, ' (', @type, ')')"/>
                    </xsl:when>
                    <xsl:when test="@class = 'fr.inra.mig_bibliome.alvisir.core.expand.explanation.PrefixMatchExplanation'">
                        <xsl:value-of select="concat(@prefix, '*')"/>
                    </xsl:when>
                    <xsl:when test="@class = 'fr.inra.mig_bibliome.alvisir.core.expand.explanation.PhraseMatchExplanation'">
                        <xsl:text>"</xsl:text>
                        <xsl:for-each select="term">
                            <xsl:if test="position() > 1">
                                <xsl:text> </xsl:text>
                            </xsl:if>
                            <xsl:value-of select="."/>
                        </xsl:for-each>
                        <xsl:text>"</xsl:text>
                    </xsl:when>
                    <xsl:when test="@class = 'fr.inra.mig_bibliome.alvisir.core.expand.explanation.TermMatchExplanation'">
                        <xsl:value-of select="@text"/>
                    </xsl:when>
                    <xsl:when test="@class = 'fr.inra.mig_bibliome.alvisir.core.expand.explanation.RelationMatchExplanation'">
                        <xsl:value-of select="@relation"/>
                        <xsl:text> (Relation)</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="@class"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>

            <div class="explanation-header">
                <span>
                    <xsl:attribute name="id">
                        <xsl:choose>
                            <xsl:when test="@id != ''">
                                <xsl:value-of select="concat('explanation-header_', @id)"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="concat('explanation-header_', ../@id)"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:attribute>
                    <xsl:value-of select="concat($field, $label)"/>
                </span>
                <span class="explanation-productivity">
                    <xsl:value-of select="concat(' (', @productivity, ')')"/>
                </span>
            </div>

            <ul class="easyui-tree" data-options="formatter:explnNodeFormatter">
                <xsl:if test="synonym">
                    <li class="explanation-synonyms" data-options="state:'closed', iconCls:'icon-blank'">
                        <span class="synonyms-header">
                            <xsl:value-of select="$synonyms-header"/>
                        </span>
                        <ul class="synonyms">
                            <xsl:for-each select="synonym">
                                <li class="synonym" data-options="iconCls:'icon-blank'">
                                    <xsl:value-of select="."/>
                                </li>
                            </xsl:for-each>
                        </ul>
                    </li>
                </xsl:if>

                <xsl:if test="sub-path">
                    <li class="explanation-subpaths" data-options="state:'closed', iconCls:'icon-blank'">
                        <span class="subpaths-header">
                            <xsl:value-of select="$subpaths-header"/>
                        </span>
                        <ul>
                            <xsl:for-each select="sub-path">
                                <li class="subpath"  data-options="iconCls:'icon-blank'">
                                    <xsl:value-of select="@label"/>
                                </li>
                            </xsl:for-each>
                        </ul>
                    </li>
                </xsl:if>
            </ul>
        </div>
    </xsl:template>

    <xsl:template name="generate-query-form-box">
        <xsl:param name="style"/>

        <div id="query-form-box" style="{$style}">
            <form id="query-form" method="get">
                <xsl:attribute name="action">
                    <xsl:value-of select="$search-url"/>
                </xsl:attribute>
                <span id="info-btn">
                    <img src="../images/information_icon.png" alt="Help!" width="16px" heigth="16px"/>
                </span>
                <div id="query-box">
                    <input id="query-input" type="text">
                        <xsl:attribute name="name">
                            <xsl:value-of select="$query-param"/>
                        </xsl:attribute>
                        <xsl:attribute name="value">
                            <xsl:value-of select="normalize-space(search-result/query/query-string)"/>
                        </xsl:attribute>
                    </input>

                    <span id="clear-btn" title="clear query">
                        <img src="../images/clear_icon.png" alt="clear query" width="16px" heigth="16px"/>
                    </span>
                </div>

                <input class="air-btn" type="submit" id="search">
                    <xsl:attribute name="value">
                        <xsl:value-of select="$search-button-text"/>
                    </xsl:attribute>
                </input>

                <xsl:if test="$onto-names != ''">
                    <a href="#" class="easyui-menubutton" data-options="menu:'#ontos-menu',iconCls:'icon-onto'"></a>
                    <div id="ontos-menu" style="width:150px;">
                        <xsl:call-template name="split-and-wrap">
                            <xsl:with-param name="texte" select="$onto-names"/>
                            <xsl:with-param name="wrapping-tag">div</xsl:with-param>
                        </xsl:call-template>
                    </div>
                </xsl:if>
            </form>
        </div>
    </xsl:template>

    <xsl:template name="split-and-wrap">
        <xsl:param name="texte"/>
        <xsl:param name="wrapping-tag"/>
        <xsl:if test="string-length($texte) > 0">
            <xsl:variable name="tokenvalue" select="substring-before(concat($texte, ','), ',')"/>
            <xsl:element name="{$wrapping-tag}">
                <xsl:value-of select="$tokenvalue"/>
            </xsl:element>
            <xsl:call-template name="split-and-wrap">
                <xsl:with-param name="texte" select="substring-after($texte, ',')"/>
                <xsl:with-param name="wrapping-tag" select="$wrapping-tag"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>

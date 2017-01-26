<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version = "1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

    <xsl:output method="html" indent="yes" doctype-public="html"/>

    <!-- dynamic parameters -->
    <xsl:param name="search-url"></xsl:param>
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


    <xsl:param name="title-field">title</xsl:param>
    <xsl:param name="title-url-field"/>
    <xsl:param name="title-url-prefix"/>


    <!--  -->




    <!-- Main Container -->
    <xsl:template match="/">
        <html>

            <head>
                <title>
                    <xsl:value-of select="$title"/>
                </title>

                <meta name="robots" content="noindex, nofollow" />

                <link rel="favicon" type="image/png" href="../images/AlvisIR_icon.ico" />
                <link rel="icon" type="image/png" href="../images/AlvisIR_icon.png" />
                <style>

                    table {
                    font-family: monospace;
                    border: medium solid silver;
                    width: 80%;
                    padding: 2em;
                    left-margin: 3em;
                    border-collapse: collapse;
                    }
                    td, th {
                    border: thin solid lightsteelblue;
                    padding: 5px;
                    }
                    th {
                    text-align: center;
                    }
                    .numcell {
                    text-align: right;
                    }
                    #footer > span {
                    background-color: #EFEFEF;
                    border: 1px solid lightsteelblue;
                    border-radius: 5px 5px 5px 5px;
                    padding: 2px; 
                    }
                    caption {
                    font-size: 150%;
                    font-style: italic;
                    }
                </style>
            </head>

            <body >
                
                <div>
                    <span>Facets for query:</span>
                    <span style="border: medium solid lightsteelblue; padding: 5px; margin-left:2em; border-radius: 6px;"> 
                        <xsl:value-of select="search-result/query/query-string"/>
                    </span>
                </div>
                <br/>
                <xsl:choose>
                    <xsl:when test="search-result/messages/message">
                        

                        <div id="error-box">
                            <div>
                                <xsl:value-of select="search-result/messages/message/text"/>
                            </div>
                            <div id="error-reason">
                                <xsl:value-of select="search-result/messages/message/exception"/>
                            </div>
                        </div>
                        
                    </xsl:when>

                    <xsl:otherwise>
   
                        <div>
                            <xsl:if test="search-result/query/query-string != ''">
                                <div id="global-facets">
                                    <xsl:apply-templates select="search-result/facets/facet" mode="global"/>
                                </div>
                            </xsl:if>
                        </div>

                    </xsl:otherwise>
                </xsl:choose>
                <br/>
                <div>
                    <span id="copyright">
                        <a target="_blank">
                            <xsl:attribute name="href">
                                <xsl:copy-of select="$about-url"/>
                            </xsl:attribute>
                            <xsl:value-of select="concat('Copyright ', $copyright-owner, ', ', $copyright-years)"/>
                        </a>
                    </span>
                </div>

            </body>
        </html>
    </xsl:template>

    <!--  -->
    <xsl:template match="facet" mode="global">
      
        <table style="padding: 2em;">
            <caption>
                <xsl:value-of select="@name"/>
            </caption>
            <thead>
                <tr>
                    <th>
                        Facet's label 
                    </th>
                    <th>
                        Canonical value
                    </th>                    
                    <th>
                        Total number of occurrences of the facet
                    </th>
                    <th>
                        Number of documents containing the facet
                    </th>
                </tr>
            </thead>
            <tbody>
                <xsl:for-each select="term">
                    <tr>
                        <td>
                            <xsl:value-of select="@label"/>
                        </td>
                        <td>
                            <xsl:value-of select="@canonical"/>
                        </td>                        
                        <td class="numcell">
                            <xsl:value-of select="@count"/>
                        </td>
                        <td class="numcell">
                            <xsl:value-of select="@docs"/>
                        </td>
                    </tr>
                </xsl:for-each>
            </tbody>
        </table>
        <br/>
       
    </xsl:template>

</xsl:stylesheet>

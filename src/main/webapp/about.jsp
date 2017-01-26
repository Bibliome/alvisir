<!DOCTYPE html>
<!--
/*
 *
 *      AlvisIR2 UI
 *
 *      Copyright Institut National de la Recherche Agronomique, 2013.
 *
 */
-->
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>AlvisIR</title>
        <link rel="stylesheet" type="text/css" href="../css/knacss_base.css"/>
        <link rel="favicon" type="image/png" href="../images/AlvisIR_icon.ico" />
        <link rel="icon" type="image/png" href="../images/AlvisIR_icon.png" />
        <script src="http://cdnjs.cloudflare.com/ajax/libs/jquery/2.0.3/jquery.min.js"></script>


        <script type="text/javascript" src="http://code.jquery.com/ui/1.10.3/jquery-ui.js"></script>
        <link rel="stylesheet" href="http://code.jquery.com/ui/1.10.3/themes/smoothness/jquery-ui.css" />

        <link rel="stylesheet" type="text/css" href="js/jqueryeasyui/themes/metro/easyui.css"/>
        <link rel="stylesheet" type="text/css" href="js/jqueryeasyui/themes/icon.css"/>
        <script type="text/javascript" src="js/jqueryeasyui/jquery.easyui.1.3.4.min.js"></script>
        <link rel="stylesheet" type="text/css" href="css/alvisir.css"></link>
    </head>
    <body class="easyui-layout">

        <div data-options="region:'center', border:true, collapsible:false">
            <div data-options="region:'north', border:true, collapsible:false" style="height:120px;">
                <div class="Banner">
                    <div class="Right"></div>
                    <div class="Left"></div>	
                </div>
            </div>
            <div style="margin-top:0px; width: 35em; margin-left: auto; margin-right: auto ">
                <div class="Dialog"><a href="search"><img alt="Logo" src="images/alvis.png" style="width: 329px; height: 156px;"></a></div>
                <h1 style="text-align:center">Alvis Information Retrieval</h1>

                <div style="margin-top: 20px; margin-bottom: 20px;">

                    <div class ="air-btn" style="width: 65px; margin-left: auto; margin-right: auto ">
                        <a href="webapi/search">
                            try AlvisIR!
                        </a>
                    </div>
                </div>
                This UI uses the following libraries and resources:
                <ul>
                    <li>
                        <a href="http://jquery.com/">jQuery & jQuery UI</a>
                    </li> 
                    <li>
                        <a href="http://www.jeasyui.com/">jQuery EasyUI</a>
                    </li> 
                    <li>
                        <a href="http://d3js.org/">D3.js</a>
                    </li>
                    <li>
                        <a href="http://underscorejs.org/">Underscore.js</a>
                    </li>
                    <li>
                        <a href="http://p.yusukekamiyamane.com/">Fugue Icons</a>
                    </li>
                    <li>
                        <a href="http://www.famfamfam.com/lab/icons/silk/">FamFamFam Icons</a>
                    </li>
                </ul>

            </div>
        </div>
        <div data-options="region:'south', border:true, collapsible:false" style="height:120px;">
            <div  style="width:200px;margin-left:auto;margin-right:auto;margin-bottom:5px">
                <a href="http://inra.fr/">
                    <img src="images/Logo-INRA.png" height="80"/>
                </a>
            </div>
        </div>
    </body>
</html>

<!DOCTYPE html>
<html>
<head>
    <!--[if IE]>
    <script src="http://html5shiv.googlecode.com/svn/trunk/html5.js"></script><![endif]-->
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <meta charset="utf-8">
    <meta name="keywords" content="dera,terminal,console,client"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="Cache-Control" content="max-age=0"/>
    <meta http-equiv="Cache-Control" content="no-cache"/>
    <meta http-equiv="Expires" content="0"/>
    <meta http-equiv="Expires" content="Tue, 01 Jan 1980 1:00:00 GMT"/>
    <meta http-equiv="Pragma" content="no-cache"/>
    <title>DERA</title>
    <link href='http://fonts.googleapis.com/css?family=Droid+Sans+Mono' rel='stylesheet' type='text/css'>
    <link href="css/smoothness/jquery-ui-1.10.1.smoothness.css" rel="stylesheet" type="text/css"/>
    <link href="css/jquery.terminal.css" rel="stylesheet" type="text/css"/>
    <link href="css/main.css" rel="stylesheet" type="text/css" media="screen, projection">

</head>
<body>
<div id="header">
    <h1>DERA</h1>
</div>
<div id="container">
    <div id="canvas"></div>
</div>
<div id="deraTerm"></div>
<button id="termButton" title="Console">&nbsp;</button>
<button id="logButton" title="Log">&nbsp;</button>
<script type="text/javascript">
    mxBasePath = 'js/mxgraph';
</script>
<!-- Load scripts at the end -->
<script src="js/mxClient.js"></script>
<script src="js/jquery.js"></script>
<script src="js/jquery-migrate-1.1.1.js"></script>
<script src="js/jquery-ui.js"></script>
<script src="js/jquery.caret.1.02.js"></script>
<script src="js/jquery.cookie.js"></script>
<script src="js/jquery.terminal.js"></script>
<script src="js/deraInit.js"></script>
<script src="js/deraUtil.js"></script>
<script src="js/deraInitTypes.js"></script>
<script src="js/deraDrawing.js"></script>
<script src="js/deraTerm.js"></script>
<script src="js/deraAjax.js"></script>
<script src="js/deraWebSocketMonitor.js"></script>
</body>
</html>
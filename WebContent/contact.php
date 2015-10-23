<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet"	href="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css">
<link
	href="//cdnjs.cloudflare.com/ajax/libs/x-editable/1.5.0/bootstrap3-editable/css/bootstrap-editable.css" rel="stylesheet" />
<link
	href="http://maxcdn.bootstrapcdn.com/font-awesome/4.1.0/css/font-awesome.min.css"
	rel="stylesheet" type="text/css">
<link
	href='http://fonts.googleapis.com/css?family=Lora:400,700,400italic,700italic'
	rel='stylesheet' type='text/css'>
<link
	href='http://fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,800italic,400,300,600,700,800'
	rel='stylesheet' type='text/css'>
<link rel="stylesheet" type="text/css"
	href="//cdn.datatables.net/1.10.7/css/jquery.dataTables.css">
<link href="css/clean-blog.css" rel="stylesheet">
 
<title>Biodiversity Linked Organisms Occurrences Megadatasets</title>
</head>
<body>
	<nav class="navbar navbar-default navbar-custom navbar-fixed-top">
	<div class="container-fluid">
		<!-- Brand and toggle get grouped for better mobile display -->
		<div class="navbar-header page-scroll">
			<button type="button" class="navbar-toggle" data-toggle="collapse"
				data-target="#bs-example-navbar-collapse-1">
				<span class="sr-only">Toggle navigation</span> <span
					class="icon-bar"></span> <span class="icon-bar"></span> <span
					class="icon-bar"></span>
			</button>
		</div>

		<!-- Collect the nav links, forms, and other content for toggling -->
		<div class="collapse navbar-collapse"
			id="bs-example-navbar-collapse-1">
			<ul class="nav navbar-nav navbar-right">
				<li><a href="HomePage.html">Home</a></li>
                <li><a href="LaunchPage.html">Start</a></li>
				<li><a href="AboutPage.html">About</a></li>
				<li><a href="DocumentationPage.html">Documentation</a></li>
				<li><a href="Contact.html">Contact</a></li>
			</ul>
		</div>
		<!-- /.navbar-collapse -->
	</div>
	<!-- /.container --> </nav>
	<header class="intro-header" style="background-image: url('images/IMG_1792.JPG')">
        <div class="container">
            <div class="row">
                <div class="col-lg-12 col-lg-offset-2 col-md-10 col-md-offset-1">
                    <div class="post-heading">
                        <h1>BLOOM</h1>
                        <h2 class="subheading">Workflow for open data cleaning</h2>
                    </div>
                </div>
            </div>
        </div>
	</header>
	<div class="container">
		<div id="divBody" class="row">
            <div class="col-lg-8 col-lg-offset-2 col-md-10 col-md-offset-1">
				<?php
				if (isset ( $_POST ) && isset ( $_POST ['name'] ) && isset ( $_POST ['email'] ) && isset ( $_POST ['message'] )) {
					if (! empty ( $_POST ['name'] ) && ! empty ( $_POST ['email'] ) && ! empty ( $_POST ['message'] )) {
						$destinataire = "mhachet@mnhn.fr";
						$sujet = "Contact request";
						$message = "Name : " . $_POST ['name'] . "\r\n";
						$message = "email : " . $_POST ['email'] . "\r\n";
						$message = "Message : " . $_POST ['message'] . "\r\n";
						$entete = 'From: ' . $_POST ['email'] . "\r\n" . 'Reply-To: ' . $_POST ['email'] . "\r\n" . 'X-Mailer: PHP/' . phpversion ();
						echo "<div class='col-lg-6'>";
						if (mail ( $destinataire, $sujet, $message, $entete )) {
							echo "<p>Thank you for your message. It has been sent to our team.</p>";
						} else {
							echo "<p>Sorry ... An error occured when submitting the form by email.</p>";
						}
						echo "</div>";
					}
				}
				?>
            </div>
        </div>
	<hr></hr>
	<!-- Footer -->
    <footer>
        <div class="container">
            <div class="row">
                <div class="col-lg-8 col-lg-offset-2 col-md-10 col-md-offset-1">
                    <ul class="list-inline text-center">
                        <li>
                            <a href="http://www.cnrs.fr" target="_blank">
                                <img src="images/logo_cnrs.jpg" height="50" width="50" class="img-circle" style="cursor:pointer;"></img>
                            </a>
                        </li>
                        <li>
                            <a href="https://www.mnhn.fr/fr" target="_blank">
                                <img src="images/logo_mnhn.jpg" height="50" width="50" class="img-circle" style="cursor:pointer;"></img>
                            </a>
                        </li>
                        <li>
                            <a href="http://isyeb.mnhn.fr/" target="_blank">
                               <img src="images/logo_ISYEB.png" height="50" width="50" class="img-circle" style="cursor:pointer;"></img>
                            </a>
                        </li>
                        <li>
                            <a href="http://www.upmc.fr/" target="_blank">
                                <img src="images/logo_upmc.png" height="50" width="50" class="img-circle" style="cursor:pointer;"></img>
                            </a>
                        </li>
                        <li>
                            <a href="http://www.ephe.fr/" target="_blank">
                                <img src="images/logo_ephe.png" height="50" width="50" class="img-circle" style="cursor:pointer;"></img>
                            </a>
                        </li>
                    </ul>
                    <p class="copyright text-muted">Copyright &copy; BLOOM 2015</p>
                </div>
            </div>
        </div>
    </footer>

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js"></script>
	<script src="../bootstrap/js/bootstrap.min.js"></script>
	<script src="../js/jquery.watable.js"></script>
    <script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js"></script>
    <script type="text/javascript"></script>
    <script type="text/javascript" src="js/functionsFinal.js"></script>
</body>
</html>              
                
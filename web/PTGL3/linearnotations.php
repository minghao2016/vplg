<?php session_start(); ?>
<!DOCTYPE html>
<?php 
include('./backend/config.php');
include('./backend/get_linearnotations.php');

$title = "List of all Linear notations";
$title = $SITE_TITLE.$TITLE_SPACER.$title;
?>
<html>
<head>
	<meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta name="description" content="">
	<meta name="author" content="">
	<link rel="shortcut icon" href="favicon.ico?v=1.0" type="image/x-icon" />

	<title><?php echo $title; ?></title>

	<!-- Mobile viewport optimized -->
	<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scale=1.0, user-scalable=no"/>

	<!-- Bootstrap CSS -->
	<link rel="stylesheet" type="text/css" href="css/bootstrap.min.css">
	<link rel="stylesheet" type="text/css" href="css/bootstrap-glyphicons.css">

	<!-- Custom CSS -->
	<link rel="stylesheet" type="text/css" href="css/styles.css">
	<link rel="stylesheet" href="css/font-awesome.css"/>

	<script src="//ajax.googleapis.com/ajax/libs/jquery/2.0.3/jquery.min.js"></script>
	<!-- Include Modernizr in the head, before any other JS -->
	<script src="js/modernizr-2.6.2.min.js"></script>

	<!-- Live Search for PDB IDs -->
	<script src="js/livesearch.js" type="text/javascript"></script>
</head>
<body id="customBackground">
	<noscript>
		<META HTTP-EQUIV="Refresh" CONTENT="0;URL=errorJS.php">
	</noscript>
	<div class="wrapper">

	<?php include('navbar.php'); ?>

	<div class="container" id="publications">
		<h2>List of all linear notations of the folding graphs</h2>
		<br>
		
		<div id="PageIntro">
		<div class="container" id="pageintro">
		On this page, you can browse all linear notations of the different folding graph types (a folding graph is a connected component of a protein graph). All notations stored in the
		database are shown. Clicking on a linear notation in the result list takes you to all protein chains which contain
		the respective linear notation.
		</div><!-- end container-->
		</div><!-- end Home -->
		
		<form class="form-inline" action="linearnotations.php" method="get">
			
		<label>Select graph-type: 
		  <select id="multidown" name="graphtype">
			<option class="downloadOption" value="1">Alpha</option>
			<option class="downloadOption" value="2">Beta</option>
			<option class="downloadOption" value="3">Alpha-Beta</option>
			<option class="downloadOption" value="4">Alpha-Ligand</option>
			<option class="downloadOption" value="5">Beta-Ligand</option>
			<option class="downloadOption" value="6">Alpha-Beta-Ligand</option>
		  </select>
		</label>
		
		<label> and notation-type: 
		  <select id="multidown" name="notationtype">
			<option class="downloadOption" value="adj">ADJ</option>
			<option class="downloadOption" value="red">RED</option>
			<option class="downloadOption" value="key">KEY</option>
			<option class="downloadOption" value="seq">SEQ</option>
		  </select>
		</label>
		
		<button type="submit" id="sendit" "class="btn btn-default">Search <span class="glyphicon glyphicon-search"></span></button><br>

		</form>	
		
		

		
		
		<div class="container" id="searchResults">
			
			<?php if($tableString !== NULL){ ?>
				<h3> Search Results </h3>
				<?php echo $tableString; /* The table string is constructed in backend/search.php, which is included by this file. */  ?>
			<?php } else { ?>
				<div id="linnot_info">
					<div>It seems like you did not selected anything. Choose the graph type and the notation type and hit search!</div>
				</div>
			<?php } ?>

		</div><!-- end container and searchResults -->

</div><!-- end container and contentText -->
</div><!-- end wrapper -->



<?php include('footer.php'); ?>
	<!-- All Javascript at the bottom of the page for faster page loading -->
	<!-- also needed for the dropdown menus etc. ... -->

	<!-- First try for the online version of jQuery-->
	<script src="http://code.jquery.com/jquery.js"></script>

	<!-- If no online access, fallback to our hardcoded version of jQuery -->
	<script>window.jQuery || document.write('<script src="js/jquery-1.8.2.min.js"><\/script>')</script>

	<!-- Bootstrap JS -->
	<script src="js/bootstrap.min.js"></script>

	<!-- Custom JS -->
	<script src="js/script.js"></script>
</body>
</html>

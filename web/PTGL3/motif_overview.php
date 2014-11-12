<?php session_start(); ?>
<!DOCTYPE html>
<?php 
include('./backend/config.php');

$title = "Protein structural motif overview";
$title = $SITE_TITLE.$TITLE_SPACER.$title;


function get_motif_data($db, $motiv_abbreviation) {
  $motif_data = array();
  $query = "SELECT count(c2m.chaintomotif_id) as count, m.motif_name, m.motif_abbreviation FROM plcc_nm_chaintomotif c2m INNER JOIN plcc_motif m ON c2m.motif_id=m.motif_id WHERE m.motif_abbreviation = '" . $motiv_abbreviation . "' GROUP BY m.motif_name, m.motif_abbreviation";
  $result = pg_query($db, $query);
  
  $arr = pg_fetch_array($result, NULL, PGSQL_ASSOC);
  $motif_data['count'] = $arr['count'];
  $motif_data['motif_name'] = $arr['motif_name'];
  $motif_data['motif_abbreviation'] = $arr['motif_abbreviation'];
  
  return $motif_data;
}

function get_motif_fullname($db, $motiv_abbreviation) {
  
  $query = "SELECT m.motif_name FROM plcc_motif m WHERE m.motif_abbreviation = '" . $motiv_abbreviation . "'";
  $result = pg_query($db, $query);
  
  $arr = pg_fetch_array($result, NULL, PGSQL_ASSOC);  
  return $arr['motif_name'];
}

function get_all_motif_names() {
  $motif_names = array("4helix", "barrel", "globin", "immuno", "jelly", "plait", "propeller", "rossman", "tim", "ubi");
  return $motif_names;
}

?>
<html>
<head>
	<meta charset="utf-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta name="description" content="PTGL folding graphs">
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
		<h2><?php print "$SITE_TITLE"; ?> Structural motif overview</h2>
		<br>
		
		<div id="PageIntro">
		<div class="container" id="pageintro">
		A motif is a common supersecondary structure. A motif consists of only a few SSEs, and it may occur in protein chains with very different functions. This server implements motif
		detection in protein graphs based on the linear notations of folding graphs.
		
		</div><!-- end container-->
		</div><!-- end Home -->
		
		<table id="tblmotifoverview">
		<tr>
		    <th>Motif</th><th>Motif abbreviation</th><th>Chains with motif in the database</th><th>Short info on motif</th>
		</tr>
		<?php
		  $conn_string = "host=" . $DB_HOST . " port=" . $DB_PORT . " dbname=" . $DB_NAME . " user=" . $DB_USER ." password=" . $DB_PASSWORD;
	          $db = pg_connect($conn_string);
		
	          $motif_names = get_all_motif_names();
	          foreach($motif_names as $motif) {
	            $motif_data = get_motif_data($db, $motif);
	            if(isset($motif_data['motif_name']) && ( ! empty($motif_data['motif_name']))) {
	                print "<tr><td>" . $motif_data['motif_name'] . "</td><td>" . $motif_data['motif_abbreviation'] . "</td><td><a href='search.php?motif=" . $motif . "'>" . $motif_data['count'] . "</a></td><td><a href='#" . $motif_data['motif_abbreviation'] ."'>" . $motif . " info</a></td></tr>\n";
	            } else {	            
	                print "<tr><td>" . get_motif_fullname($db, $motif) . "</td><td>" . $motif . "</td><td>0</td><td><a href='#" . $motif ."'>" . $motif . " info</a></td></tr>\n";
	            }
	          }
	          
	
		?>
		
		
		</table>
		
		
		<br><br><br><br><br><br>
		
		<h2>Motif info</h2>
		
		
		<br><br>
		<div id="4helix">
		    <h3>Four Helix Bundle</h3>
		    <p>A motif consisting only of alpha helices.</p>
		    <p><img class="motifimage" src="./images/4helixbeide_struktur.jpg" width="300" /></p>
		</div>
		
		
		<br><br><br><br>
		<div id="globin">
		    <h3>Globin Fold</h3>
		    <p>A motif consisting only of alpha helices.</p>
		    <p><img class="motifimage" src="./images/globin_struktur.jpg" width="300" /></p>
		</div>
		
		
		<br><br><br><br>
		<div id="barrel">
		    <h3>Up and Down Barrel</h3>
		    <p>A motif consisting only of beta strands.</p>
		    <p><img class="motifimage" src="./images/barrel_struktur.jpg" width="300" /></p>
		</div>
		
		
		<br><br><br><br>
		<div id="immuno">
		    <h3>Immunoglobin Fold</h3>
		    <p>A motif consisting only of beta strands.</p>
		    <p><img class="motifimage" src="./images/immuno_struktur.jpg" width="300" /></p>
		</div>
		
		
		<br><br><br><br>
		<div id="propeller">
		    <h3>Beta Propeller</h3>
		    <p>A motif consisting only of beta strands.</p>
		    <p><img class="motifimage" src="./images/propeller_struktur.jpg" width="300" /></p>
		</div>
		
		
		<br><br><br><br>
		<div id="jelly">
		    <h3>Jelly Roll</h3>
		    <p>A motif consisting only of beta strands.</p>
		    <p><img class="motifimage" src="./images/jelly_struktur.jpg" width="300" /></p>
		</div>
		
		
		<br><br><br><br>
		<div id="ubi">
		    <h3>Ubiquitin Roll</h3>
		    <p>A motif consisting only of both alpha helices and beta strands.</p>
		    <p><img class="motifimage" src="./images/ubibeide_struktur.jpg" width="300" /></p>
		</div>
		
		
		<br><br><br><br>
		<div id="plait">
		    <h3>Alpha Beta Plait</h3>
		    <p>A motif consisting only of both alpha helices and beta strands.</p>
		    <p><img class="motifimage" src="./images/plait_struktur.jpg" width="300" /></p>
		</div>
		
		
		<br><br><br><br>
		<div id="rossman">
		    <h3>Rossman Fold</h3>
		    <p>A motif consisting only of both alpha helices and beta strands.</p>
		    <p><img class="motifimage" src="./images/rossman_struktur.jpg" width="300" /></p>
		</div>
		
		<br><br><br><br>
		<div id="tim">
		    <h3>TIM Barrel</h3>
		    <p>A motif consisting only of both alpha helices and beta strands.</p>
		    <p><img class="motifimage" src="./images/tim_struktur.jpg" width="300" /></p>
		</div>
		
										
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
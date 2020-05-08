 package com.vaggs.Utils;
 
 import static com.vaggs.Utils.OfyService.ofy;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import com.google.appengine.labs.repackaged.com.google.common.collect.Lists;
 import com.vaggs.AirportDiagram.Airport;
 import com.vaggs.Route.Route;
 import com.vaggs.Route.Taxiway;
 import com.vaggs.Route.Transponder;
 import com.vaggs.Route.Waypoint;
 
 public class DebuggingDBObjects {
 
 	public static void createDBObjects () {
 		/* Transponder Code = 1 */
 		Route debugRoute = Route.ParseRouteByTaxiways((new Waypoint(new LatLng(41.7258, -71.4368), false, false)), "");
 		debugRoute.addWaypoints(Arrays.asList(
 				new Waypoint(new LatLng(41.7258, -71.4368), false, false),
 				new Waypoint(new LatLng(41.7087976, -71.44134), false, false),
 				new Waypoint(new LatLng(41.73783, -71.41615), false, false),
 				new Waypoint(new LatLng(41.725, -71.433333), true, false)
 			));
 		//Transponder.Parse(1).setRoute(debugRoute);
 		//Transponder.Parse(2).setRoute(debugRoute);
 		
 		ArrayList<Taxiway> taxiways = Lists.newArrayList();
 		Taxiway taxiway = new Taxiway('B'); 
 		 taxiway.setWaypoints(Arrays.asList(
 			new Waypoint(new LatLng(41.73228367493647, -71.42739772796631), false, true),
 			new Waypoint(new LatLng(41.72972153234874, -71.42662525177002), false, true),
 			new Waypoint(new LatLng(41.72853650684023, -71.42628192901611), false, false),
 			new Waypoint(new LatLng(41.727479573755126, -71.42520904541016), false, true),
 			new Waypoint(new LatLng(41.726678855296136, -71.42430782318115), true, true),
 			new Waypoint(new LatLng(41.724180549563606, -71.42203330993652), false, false),
 			new Waypoint(new LatLng(41.72321963687659, -71.42310619354248), true, false), 
 			new Waypoint(new LatLng(41.722707144234306, -71.42400741577148), false, true)
 			));
 		taxiways.add(taxiway);
 		
 		taxiway = new Taxiway('A'); 
 		 taxiway.setWaypoints(Arrays.asList( 
 		new Waypoint(new LatLng(41.732027465276374, -71.42314910888672), false, false),
 		new Waypoint(new LatLng(41.7312588301645, -71.42203330993652), false, true),
 		new Waypoint(new LatLng(41.73074640164693, -71.42087459564209), true, true)
 			));
 		taxiways.add(taxiway);
 		
 		taxiway = new Taxiway('C'); 
 		 taxiway.setWaypoints(Arrays.asList(
 		 new Waypoint(new LatLng(41.72575000938133, -71.4336633682251), false, false),
 		new Waypoint(new LatLng(41.72575000938133, -71.43280506134033), false, false), 
 		new Waypoint(new LatLng(41.72571797997234, -71.43001556396484), false, false), 
 		new Waypoint(new LatLng(41.72571797997234, -71.42877101898193), false, false), 
 		new Waypoint(new LatLng(41.725205507257044, -71.42696857452393), false, true),
 		new Waypoint(new LatLng(41.72453288061494, -71.42598152160645), true, true), 
 		new Waypoint(new LatLng(41.722707144234306, -71.42400741577148), false, true),
 		new Waypoint(new LatLng(41.71783826026642, -71.41907215118408), false, false), 
 		new Waypoint(new LatLng(41.718414857887055, -71.41825675964355), true, true)
 			));
 		taxiways.add(taxiway);
 		
 		
 		taxiway = new Taxiway('E'); 
 		 taxiway.setWaypoints(Arrays.asList(
 		new Waypoint(new LatLng(41.719536005117995, -71.43555164337158), false, true),
 		new Waypoint(new LatLng(41.71610843636524, -71.43426418304443), false, true)
 				));
 		taxiways.add(taxiway);
 		
 		
 
 		taxiway = new Taxiway('F'); 
 		 taxiway.setWaypoints(Arrays.asList(
 		new Waypoint(new LatLng(41.72869664669985, -71.43306255340576), false, true),
 		new Waypoint(new LatLng(41.73151504289037, -71.43237590789795), false, true)
 		));
 		 taxiways.add(taxiway);
 		 
 		 
 		 taxiway = new Taxiway('M'); 
 		 taxiway.setWaypoints(Arrays.asList(
 		new Waypoint(new LatLng(41.7312588301645, -71.42203330993652), false, true),
 		new Waypoint(new LatLng(41.729849641905204, -71.42327785491943), false, true),
 		new Waypoint(new LatLng(41.727479573755126, -71.42520904541016), false, true),
 		new Waypoint(new LatLng(41.72626247775354, -71.42602443695068), true, true), 
 		new Waypoint(new LatLng(41.725205507257044, -71.42696857452393), false, true)
 		));
 		 taxiways.add(taxiway);
 		 
 		 
 		 taxiway = new Taxiway('N'); 
 		 taxiway.setWaypoints(Arrays.asList(
 		new Waypoint(new LatLng(41.72808811310961, -71.43435001373291), false, false),
 		new Waypoint(new LatLng(41.72869664669985, -71.43306255340576), false, false),
 		new Waypoint(new LatLng(41.729561395043916, -71.43237590789795), false, true),
 		new Waypoint(new LatLng(41.72965747747473, -71.43061637878418), false, true),
 		new Waypoint(new LatLng(41.72972153234874, -71.4296293258667), true, false), 
 		new Waypoint(new LatLng(41.72972153234874, -71.42662525177002), false, true), 
 		new Waypoint(new LatLng(41.729849641905204, -71.42327785491943), false, true),
 		new Waypoint(new LatLng(41.72988166925439, -71.42160415649414), true, true)
 		 ));
 		 taxiways.add(taxiway);
 		
 		 
 		 taxiway = new Taxiway('S'); 
 		 taxiway.setWaypoints(Arrays.asList( 
 		new Waypoint(new LatLng(41.71796639351807, -71.4325475692749), false, true), 
 		new Waypoint(new LatLng(41.71610843636524, -71.43426418304443), false, true), 
 		new Waypoint(new LatLng(41.714314495734975, -71.43537998199463), false, false),
 		new Waypoint(new LatLng(41.713769896707845, -71.43443584442139), true, true)
 		));
 		 taxiways.add(taxiway);
 		 
 		 taxiway = new Taxiway('T'); 
 		 taxiway.setWaypoints(Arrays.asList(
 		new Waypoint(new LatLng(41.72965747747473, -71.43061637878418), false, false),
 		new Waypoint(new LatLng(41.72575000938133, -71.4336633682251), false, false),
 		new Waypoint(new LatLng(41.72094541960078, -71.4376974105835), false, false),
 		new Waypoint(new LatLng(41.72056103689838, -71.43701076507568), false, true),
 		new Waypoint(new LatLng(41.719536005117995, -71.43555164337158), false, true),
 		new Waypoint(new LatLng(41.71796639351807, -71.4325475692749), false, true),
 		new Waypoint(new LatLng(41.71754995951615, -71.43151760101318), true, true)
 		));
 		 taxiways.add(taxiway);
 		 
 		 taxiway = new Taxiway('V'); 
 		 taxiway.setWaypoints(Arrays.asList( 
 		new Waypoint(new LatLng(41.73228367493647, -71.42739772796631), false, true),
 		new Waypoint(new LatLng(41.72972153234874, -71.4296293258667), true, true), 
 		new Waypoint(new LatLng(41.72575000938133, -71.43280506134033), false, true),
 		new Waypoint(new LatLng(41.72056103689838, -71.43701076507568), false, true) 
 		));
 		 taxiways.add(taxiway);
 		 
 		
 		Airport kpvd = new Airport("kpvd", null, null, null, "");
 		kpvd.setTaxiways(taxiways);
 		kpvd.setRouteStartingPoints(Arrays.asList(new Waypoint(new LatLng(41.72575000938133, -71.4336633682251), false, true)));
 		
 		ofy().save().entities(kpvd).now();
 		
 		AtcUser josh = new AtcUser("josh@joshpearl.com");
		AtcUser hawk = new AtcUser("dazerdude");
		AtcUser max = new AtcUser("maxbulian");
 		
 		ofy().save().entity(josh).now();
 		ofy().save().entity(hawk).now();
 		ofy().save().entity(max).now();
 		
 	}
 }

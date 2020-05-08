 package controllers;
 
 import java.util.Date;
 import java.util.List;
 
 import models.Operational;
 
 import play.modules.morphia.Model.MorphiaQuery;
 import play.mvc.Controller;
 
 public class Reports extends Controller {
 	
 	public static void upcoming() {		
		MorphiaQuery q2 = Operational.find().order("vessel.name"); // create a Query
 	    q2.or(q2.criteria("status").contains("Prospect"),
 	    		q2.criteria("status").contains("Berthing"), 
 	    		q2.criteria("status").contains("Booking Approved"), 
 	    		q2.criteria("status").contains("Berthing Rejected"));
 	    List<Operational> berthings = q2.asList();
 	    render(berthings);
 	}
 	
 	public static void monthly(Date start, Date end) {
 		if(start == null && end == null) {
			List<Operational> monthly = Operational.all().order("vessel.name").asList();
 			render(monthly);
 		} else {
 			List<Operational> monthly = Operational.filter("berthing.ata >", start)
										.filter("departure.atd <", end).order("vessel.name")
										.asList();
 			render(monthly, start, end);
 		}
 	}
 
 }

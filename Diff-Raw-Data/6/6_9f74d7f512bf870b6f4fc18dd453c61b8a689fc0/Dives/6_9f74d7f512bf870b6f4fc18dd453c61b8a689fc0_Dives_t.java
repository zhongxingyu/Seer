 package controllers;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import models.Dive;
 import models.Picture;
 import models.Spot;
 import models.User;
 import play.Logger;
 import play.mvc.Before;
 import play.mvc.Controller;
 import play.i18n.Messages;
 import play.data.validation.Validation;
 import play.data.validation.Valid;
 import play.db.jpa.Blob;
 
 public class Dives extends Controller {
 	
 	@Before(unless="show")
 	static void checkAuthentification() {
 	    if(!Security.isConnected()) {
 	    	Application.index();
 	    }
 	}
 	
 	public static void index() {
 		User currentUser = Security.connectedUser();
 		
 		renderArgs.put("dives", currentUser.dives);
 		render();
 	}
 
 	public static void create(Dive dive) {
 		render(dive);
 	}
 
 	public static void show(java.lang.Long id) {
 		Dive dive = Dive.findById(id);
 		render(dive);
 	}
 
 	public static void edit(java.lang.Long id) {
 		Dive dive = Dive.findById(id);
 		render(dive);
 	}
 
 	public static void delete(java.lang.Long id) {
 		Dive dive = Dive.findById(id);
 		dive.delete();
 		index();
 	}
 	
 	public static void save(@Valid Dive dive, @Valid Spot spot, Blob[] images) {
 		if (validation.hasErrors()) {
 			flash.error(Messages.get("scaffold.validation"));
			dive.spot = spot;
			render("@create", dive);
 		}
 		
 		// Check if the given spot already exists:
 		Spot existingSpot = Spot.find("byNameAndCountry", spot.name, spot.country).first();
 		if(existingSpot == null) {
 			spot.save();
 			dive.spot = spot;
 		} else {
 			dive.spot = existingSpot;
 		}
 		
 		dive.save();
 		
 		User currentUser = Security.connectedUser();
 		if(currentUser.dives == null) {
 			currentUser.dives = new ArrayList<Dive>();
 		}
 		currentUser.dives.add(dive);
 		
 		// pictures
 		/*
 		if(images != null) {
 			for(Blob image : images) {
 				Logger.info("got image");
 				Logger.info(image.getFile().getName());
 				
 				Picture picture = new Picture(image, currentUser);
 				
 				picture.dive = dive;
 				picture.save();
 			}
 		}
 		*/
 
 		currentUser.save();
 
 		flash.success(Messages.get("scaffold.created", "Dive"));
 		show(dive.id);
 	}
 
	public static void update(@Valid Dive dive, @Valid Spot spot) {
 		if (validation.hasErrors()) {
 			flash.error(Messages.get("scaffold.validation"));
 			render("@edit", dive);
 		}
 		
       	dive = dive.merge();
       	dive.save();
       	
       	spot = spot.merge();
       	spot.save();
 		
 		flash.success(Messages.get("scaffold.updated", "Dive"));
 		show(dive.id);
 	}
 
 }

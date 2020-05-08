 package controllers;
 
 import play.*;
 import play.data.validation.Required;
 import play.data.validation.Valid;
 import play.mvc.*;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import models.*;
 import java.text.SimpleDateFormat;
 
 public class Application extends Controller {
 
 	private static final SimpleDateFormat SDF = new SimpleDateFormat(
			"dd-MM-yyyy");
 
 	public static void index() {
 
 		List<Client> clients = Client.findAll();
 		Order order = new Order(null, null);
 		render(clients, order);
 	}
 
 	public static void stats() {
 
 	}
 
 	public static void saveClient(String firtsname, String lastname) {
 
 	}
 
 	public static void saveOrder(@Required String clientId,
 			@Required String orderDate, @Required String deliveryDate,
 			@Required Order order) {
 
		if (validation.hasErrors()) {
 
 			order.client = Client.findById(clientId);
 			try {
 				order.date = SDF.parse(orderDate);
 				order.deliberyDate = SDF.parse(deliveryDate);
 			} catch (ParseException e) {
 				flash.error("Fecha Invalida");
 				index();
 			}
 			order.save();
 			flash.success("Grande Rusa!!");
 			index();
 		}
 	}
 
 }

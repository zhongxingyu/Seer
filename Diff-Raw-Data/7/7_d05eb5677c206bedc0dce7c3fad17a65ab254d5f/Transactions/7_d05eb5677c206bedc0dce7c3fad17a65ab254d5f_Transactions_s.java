 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 
 import models.*;
 
 // @With(Secure.class)
 
 /**
 Transactions controller
 */
 
 public class Transactions extends Controller {
 
     /**
      * Takes a line id and returns all the transactions associated with
      * that line.
      *
      * @param lineID The id of the line that we want all the
      * transactions from.
      */
 	public static void index(long lineId) {
		List<Transaction> transactions = Transaction.find("lineId", lineId).asList();
 		renderJSON(transactions);
 	}
 
     /**
      * Takes a transaction ID and returns the JSON representation of
      * the specified object.
      *
      * @param id The ID of the object we want to be returned in JSON
      * format.
      */
 	public static void transaction(long id) {
 		Transaction t = Transaction.find("by_id", id).first();
 		renderJSON(t);
 	}
 
     /**
      * Takes a JSON representation of a transaction and stores it in the
      * database.
      *
      * @param body The JSON representation of the transaction to be
      * stored in the database.
      */
	public static void create(Transaction body) {
 		User user = User.find("byEmail", Security.connected()).first();
 
 		body.user = user;
 		body.save();
 	}
 
     /**
      * Takes a JSON representation of a transaction and updates the
      * specificied transaction record with the new attributes
      *
      * @param body The JSON representation of the transaction to be
      * updated.
      */
 	public static void update(Transaction body) {
 		Transaction t = Transaction.find("by_id", body.getId()).first();
 		Budget b = Budget.find("by_id", body).first();
 		User user = User.find("byEmail", Security.connected()).first();
 
 		t.user = user;
 		//t.budget = something TODO: Fix this
 		t.subline_number = body.subline_number;
 		t.name = body.name;
 		t.subtotal = body.subtotal;
 		t.subline_id = body.subline_id;
 		t.order = body.order;
 
 		t.save();
 		renderJSON(t);
 	}
     
     /**
      * Deletes the object in the database specified by the supplied id
      *
      * @param id The id of the transaction object to be deleted.
      */
 	public static void delete(long id){
 		Transaction t = Transaction.find("by_id", id).first();
 		t.delete();
 	}
 
 }

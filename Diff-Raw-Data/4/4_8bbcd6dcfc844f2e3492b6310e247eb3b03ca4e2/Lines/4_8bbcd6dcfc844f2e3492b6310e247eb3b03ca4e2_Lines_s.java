 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 
 import models.*;
 
 // @With(Secure.class)
 
 /**
 Lines controller
 */
 public class Lines extends Controller {
 
 	public static void index(long budgetId) {
 		List<Line> lines = Line.find("budgetId", budgetId).asList();
 		renderJSON(lines);
 	}
 
 	public static void line(long id) {
 		Line line = Line.find("by_id", id).first();
 		renderJSON(line);
 	}
 
 	public static void create(long budgetId, Line body) {
 		// find the user
     	User user = User.find("byEmail", Security.connected()).first();
     	body.budgetId = budgetId;
 		body.save();
 		renderJSON(body);
 	}
 
 	public static void update(Line body) {
 		Line l = Line.find("by_id", body.getId()).first();
 			// find the user
 	    User user = User.find("byEmail", Security.connected()).first();
 
 			l.user = user.toString();
 			l.line_number = body.line_number;
 			l.name = body.name;
 			l.subtotal = body.subtotal;
 			l.parent_line_id = body.parent_line_id;
 			l.order = body.order;
 
 			l.save();
 			renderJSON(l);
 	}
 
	public static void delete(long id) {
		Line l = Line.find("by_id", id).first();
 		l.delete();
 	}
 
 	public static void sublines(long lineId) {
 		List<Line> sublines = Line.getSublines(lineId);
 		renderJSON(sublines);
 	}
 
 	public static void expenses(long budgetId) {
 		List<Line> expenses = Line.getExpenses(budgetId);
 		renderJSON(expenses);
 	}
 
 	public static void incomes(long budgetId) {
 		List<Line> incomes = Line.getIncomes(budgetId);
 		renderJSON(incomes);
 	}
 
 }

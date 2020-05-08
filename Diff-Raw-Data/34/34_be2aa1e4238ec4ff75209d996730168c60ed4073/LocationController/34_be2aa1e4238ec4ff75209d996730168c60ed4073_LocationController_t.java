 package controllers;
 
 import models.Author;
 import models.Location;
 import play.mvc.Controller;
 import play.mvc.With;
 
 @With(Secure.class)
 public class LocationController extends Controller {
 
 	/**
 	 * TODO FIXME zip code cannot be used yet since treeview only supports one
 	 * changeable value per node.
 	 * 
 	 * @param name
 	 * @param zip
 	 * @param parent
 	 */
 	public static void add(String name, int zip, Location parent) {
 		Author aut = Author.find("byName", session.get("username")).first();
 		Location location = new Location(name, 0, parent, aut);
 		location.save();
 
 		flash.success("Ort wurde gespeichert.");
 	}
 
 	public static void rename(Location location, String newName) {
 		location.name = newName;
 		location.save();
 		updateFullNames(location);
 	}
 
 	public static void move(Location location, long newParentId) {
 		location.parent = Location.findById(newParentId);
 		location.save();
 		updateFullNames(location);
 	}
 
 	public static void delete(Location location) {
 		location.delete();
 	}
 
 	/**
 	 * Updates the full names (names including the names of all parent nodes) of
 	 * the defined element and all children nodes (recursive).
 	 * 
 	 * @param l
 	 *            - The element to start with.
 	 */
 	public static void updateFullNames(Location l) {
 		l.updateFullName();
 		l.save();
		if (!l.children.isEmpty() && l.children != null) {
			for (Location c : l.children) {
				if (c != null)
					updateFullNames(c);
			}
 		}
 	}
 }

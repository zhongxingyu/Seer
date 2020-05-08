 package controllers;
 
 import java.util.List;
 
 import models.Actor;
 import models.Author;
 import play.mvc.Controller;
 import play.mvc.With;
 
 @With(Secure.class)
 public class ActorController extends Controller {
 
 	public static void add(String name, Actor parent) {
 		Author aut = Author.find("byName", session.get("username")).first();
 		Actor newActor = new Actor(name, parent, aut);
 		newActor.save();
 		flash.success("Legislator wurde erfasst");
 	}
 
 	public static void formAddActor() {
 		List<Actor> allActors = Actor.find("order by name asc").fetch();
 		render(allActors);
 	}
 
 	public static void formEditActor(long actorId) {
 		Actor actor = Actor.findById(actorId);
 		render(actor);
 	}
 
 	public static void rename(Actor actor, String newName) {
 		actor.name = newName;
 		actor.save();
 		updateFullNames(actor);
 	}
 
 	/**
 	 * Sets a new node being the actor's parent element.
 	 * 
 	 * @param actor
 	 *            - the actor to be moved.
 	 * @param newParentId
 	 *            - the actor's new parent element.
 	 */
 	public static void move(Actor actor, long newParentId) {
 		Actor parent = Actor.findById(newParentId);
 		actor.parent = parent;
 		actor.save();
 		updateFullNames(actor);
 	}
 
 	public static void delete(Actor actor) {
 		actor.delete();
 	}
 
 	/**
 	 * Updates the full names (names including the names of all parent nodes) of
 	 * the defined element and all children nodes (recursive).
 	 * 
 	 * @param a
 	 *            - The element to start with.
 	 */
 	public static void updateFullNames(Actor a) {
 		a.updateFullName();
 		a.save();
		if (!a.children.isEmpty() && a.children != null) {
			for (Actor c : a.children) {
				if (c != null)
					updateFullNames(c);
			}
 		}
 	}
 
 }

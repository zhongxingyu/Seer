 package vitro.grid;
 
 import vitro.*;
 
 public class MoveAction extends GridAction {
 
 	public final Location start;
 	public final Location end;
 	public final Actor actor;
 
 	public MoveAction(Grid model, Location destination, Actor actor) {
 		super(model);
 		this.start = model.locations.get(actor);
 		this.end   = destination;
 		this.actor = actor;
 	}
 
 	public void apply() {
 		if (!start.equals(model.locations.get(actor))) {
 			throw new Error(String.format("Precondition for MoveAction '%s' not satisfied.", this));
 		}
 		model.locations.put(actor, end);
 	}
 
 	public void undo() {
 		if (!end.equals(model.locations.get(actor))) {
 			throw new Error(String.format("Postcondition for MoveAction '%s' not satisfied.", this));
 		}
 		model.locations.put(actor, start);
 	}
 
 	@Override
 	public int hashCode() {
 		return model.hashCode() ^
 		       start.hashCode() ^
 		         end.hashCode();
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if (!(o instanceof MoveAction)) { return false; }
 		MoveAction other = (MoveAction)o;
 		return (other.model == this.model) &&
 		       (other.start.equals(start)) &&
		       (other.actor.equals(actor)) &&
		       (other.end.equals(end));
 	}
 
 	@Override
 	public String toString() {
 		return String.format("Move actor '%s' from '%s' to '%s'.", actor, start, end);
 	}
}

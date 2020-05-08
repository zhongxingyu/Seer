 package vitro.model.graph;
 
 import vitro.util.*;
 import vitro.model.*;
 import java.util.*;
 
 public class GraphActor extends Actor {
 
 	protected final Graph model;
 
 	public GraphActor(Graph model) {
 		this.model = model;
 	}
 
 	public Node location() {
 		return model.getLocation(this);
 	}
 
 	public MoveAction move(Edge edge, Set<Action> options) {
 		for(Action action : Groups.ofType(MoveAction.class, options)) {
 			MoveAction move = (MoveAction)action;
 			if (move.actor == this && move.edge == edge) { return move; }
 		}
 		return null;
 	}
 
 	public MoveAction move(Node node, Set<Action> options) {
 		for(Action action : Groups.ofType(MoveAction.class, options)) {
 			MoveAction move = (MoveAction)action;
 			if (move.actor == this && move.edge.end == node) { return move; }
 		}
 		return null;
 	}
 
 	public MoveAction move(Position position, Set<Action> options) {
 		return move(model.getNode(position), options);
 	}
 
 	public MoveAction moveToward(Actor actor, Set<Action> options) {
		return move(model.getLocation(this).path(actor).get(0), options);
 	}
 
 	public MoveAction moveToward(Node node, Set<Action> options) {
		return move(model.getLocation(this).path(node).get(0), options);
 	}
 
 	public MoveAction moveToward(Position position, Set<Action> options) {
 		return moveToward(model.getNode(position), options);
 	}
 
 	public CreateAction create(Class type, Set<Action> options) {
 		for(Action action : Groups.ofType(CreateAction.class, options)) {
 			CreateAction create = (CreateAction)action;
 			if (type.equals(create.actor.getClass())) { return create; }
 		}
 		return null;
 	}
 
 	public CreateAction create(Node node, Class type, Set<Action> options) {
 		for(Action action : Groups.ofType(CreateAction.class, options)) {
 			CreateAction create = (CreateAction)action;
 			if (type.equals(create.actor.getClass()) && node == create.node) { return create; }
 		}
 		return null;
 	}
 
 	public CreateAction create(Position position, Class type, Set<Action> options) {
 		return create(model.getNode(position), type, options);
 	}
 
 	public DestroyAction destroy(Set<Actor> actors, Set<Action> options) {
 		for(Action action : Groups.ofType(DestroyAction.class, options)) {
 			DestroyAction destroy = (DestroyAction)action;
 			if (actors.equals(destroy.actors.keySet())) { return destroy; }
 		}
 		return null;
 	}
 
 	public DestroyAction destroy(Actor actor, Set<Action> options) {
 		Set<Actor> set = new HashSet<Actor>();
 		set.add(actor);
 		return destroy(set, options);
 	}
 
 	public DestroyAction destroy(Class type, Set<Action> options) {
 		for(Action action : Groups.ofType(DestroyAction.class, options)) {
 			DestroyAction destroy = (DestroyAction)action;
 			boolean match = true;
 			for(Actor target : destroy.actors.keySet()) {
 				if (!type.equals(target.getClass())) { match = false; break; }
 			}
 			if (match) { return destroy; }
 		}
 		return null;
 	}
 
 }

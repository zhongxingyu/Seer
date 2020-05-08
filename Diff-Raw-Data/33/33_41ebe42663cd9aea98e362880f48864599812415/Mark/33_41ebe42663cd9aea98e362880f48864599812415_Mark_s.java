 package se.liu.ida.geoza435.tddc69.project.game;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import se.liu.ida.geoza435.tddc69.project.Observable;
 import se.liu.ida.geoza435.tddc69.project.Observer;
 
 public class Mark extends Observable implements Serializable {
 	private static final long serialVersionUID = 1L;
 	MarkType type;
 	Position position;
 	ArrayList<Connection> connections;
 	ArrayList<Observer> observers;
 	boolean selected;
 
 	public Mark(MarkType type, Position position) {
 		this.type = type;
 		this.position = position;
 		this.connections = new ArrayList<Connection>();
 	}
 
 	public Position getPosition() {
 		return position;
 	}
 
 	public void setPosition(Position position) {
 		this.position = position;
 		changed();
 	}
 
 	public boolean isSelected() {
 		return selected;
 	}
 
 	public void setSelected(boolean set) {
 		selected = set;
 		changed();
 	}
 
 	public List<Connection> getConnections() {
 		return connections;
 	}
 
 	public void deleteConnection(Connection c) {
 		connections.remove(c);
 	}
 
 	public void addConnection(Connection c) {
 		connections.add(c);
 	}
 
 	public MarkType getType() {
 		return type;
 	}
 
 	public void setType(MarkType markType) {
 		this.type = markType;
 	}
 
	public ArrayList<MarkListContainer> getNextMarks(Integer upTo) {
 		ArrayList<MarkListContainer> retMarks = new ArrayList<>();
 		System.out.println(this);
		return getNextMarks(upTo, retMarks, this, 1);
 	}
 
 	private ArrayList<MarkListContainer> getNextMarks(Integer upTo,
			ArrayList<MarkListContainer> retMarks, Mark from, Integer distance) {
 		if (upTo <= 0) {
 			return retMarks;
 		}
 		for (MarkListContainer m : this.getAdjacentMarks()) {
 			if (m.getMark() != from) {
				retMarks.add(new MarkListContainer(m, distance));
				m.getMark()
						.getNextMarks(upTo - 1, retMarks, this, distance + 1);
 			}
 		}
 		return retMarks;
 	}
 
 	private ArrayList<MarkListContainer> getAdjacentMarks() {
 		ArrayList<MarkListContainer> retMarks = new ArrayList<>();
 		Mark m;
 		for (Connection c : connections) {
 			m = c.getA();
 			if (m != this)
 				retMarks.add(new MarkListContainer(m, c.getType()));
 			m = c.getB();
 			if (m != this)
 				retMarks.add(new MarkListContainer(m, c.getType()));
 		}
 		return retMarks;
 	}
 
 	@Override
 	public String toString() {
 		return "[m:" + type.toString() + "@" + position.toString() + "]";
 	}
 
 }

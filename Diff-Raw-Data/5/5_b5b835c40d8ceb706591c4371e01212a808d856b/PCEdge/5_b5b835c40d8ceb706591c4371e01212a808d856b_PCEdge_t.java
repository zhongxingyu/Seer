 /*
  * Copyright (c) 2011 Dominik Dingel
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in the
  * Software without restriction, including without limitation the rights to use, 
  * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the 
  * Software, and to permit persons to whom the Software is furnished to do so, 
  * subject to the following conditions:
  * The above copyright notice and this permission notice shall be included in all 
  * copies or substantial portions of the Software.
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
  * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A 
  * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION 
  * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
  * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package de.unisiegen.informatik.bs.alvis.graph.datatypes;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Stack;
 
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.GraphicalRepresentation;
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PCInteger;
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PCList;
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PCObject;
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PCBoolean;
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PCString;
 
 /**
  * 
  * @author Dominik Dingel
  * 
  * @description implementing the Edge class
  * 
  */
 
 public class PCEdge extends PCObject implements Comparable<PCEdge> {
 
 	protected static final String TYPENAME = "Edge";
 
 	// the used members
 	private PCVertex startVertex;
 	private PCVertex endVertex;
 	private PCBoolean isDirected;
 	private PCInteger weight;
 	private PCString color;
	
	// unused members to make compiler happy
	@SuppressWarnings("unused")
	final private PCList<PCVertex> vertices = null;
	
 
 	/**
 	 * Default Constructor: not directed and 0 weight
 	 */
 	public PCEdge() {
 		startVertex = new PCVertex();
 		endVertex = new PCVertex();
 		isDirected = new PCBoolean(false);
 		weight = new PCInteger(0);
 		color = new PCString("");
 		commandsforGr = new ArrayList<Stack<Object>>();
 		commandsforGr.add(new Stack<Object>());
 		commandsforGr.add(new Stack<Object>());
 	}
 
 	/**
 	 * Constructor to create new Edge (not directed)
 	 * 
 	 * @param v1
 	 * @param v2
 	 * @param edge
 	 */
 	public PCEdge(PCVertex v1, PCVertex v2, GraphicalRepresentationEdge edge) {
 		this.allGr.add(edge);
 		this.startVertex = v1;
 		this.endVertex = v2;
 		isDirected = new PCBoolean(false);
 		notifyVertices();
 		weight = new PCInteger(edge.getWeight());
 		color = new PCString(edge.getColorText());
 		commandsforGr = new ArrayList<Stack<Object>>();
 		commandsforGr.add(new Stack<Object>());
 		commandsforGr.add(new Stack<Object>());
 
 	}
 
 	/**
 	 * Constructor to create new Edge (not directed)
 	 * 
 	 * @param v1
 	 * @param v2
 	 */
 	public PCEdge(PCVertex v1, PCVertex v2) {
 		this.startVertex = v1;
 		this.endVertex = v2;
 		isDirected = new PCBoolean(false);
 		notifyVertices();
 		weight = new PCInteger(0);
 		color = new PCString("");
 		commandsforGr = new ArrayList<Stack<Object>>();
 		commandsforGr.add(new Stack<Object>());
 		commandsforGr.add(new Stack<Object>());
 
 	}
 
 	/**
 	 * Constructor to create new Edge
 	 * 
 	 * @param v1
 	 * @param v2
 	 * @param isDirected
 	 */
 	public PCEdge(PCVertex v1, PCVertex v2, boolean isDirected) {
 		this.startVertex = v1;
 		this.endVertex = v2;
 		this.isDirected = new PCBoolean(false);
 		notifyVertices();
 		weight = new PCInteger(0);
 		color = new PCString("");
 		commandsforGr = new ArrayList<Stack<Object>>();
 		commandsforGr.add(new Stack<Object>());
 		commandsforGr.add(new Stack<Object>());
 	}
 
 	/**
 	 * private method to inform the vertices about the connection
 	 */
 	private void notifyVertices() {
 		if (!isDirected.getLiteralValue()) {
 			endVertex.addEdge(this, startVertex);
 		}
 		startVertex.addEdge(this, endVertex);
 	}
 
 	@Override
 	public String toString() {
 		String result = startVertex.toString();
 		if (isDirected.getLiteralValue()) {
 			result += " - ";
 			result += weight.toString();
 			result += "> ";
 		} else {
 			result += " <- ";
 			result += weight.toString();
 			result += " -> ";
 		}
 		result += endVertex.toString();
 		return result;
 	}
 
 	@Override
 	public List<String> getMembers() {
 		String[] attributes = { "weight", "vertices", "startVertex",
 				"endVertex", "color" };
 		return Arrays.asList(attributes);
 	}
 
 	@Override
 	public PCObject get(String memberName) {
 		if (memberName.equals("weight")) {
 			return this.getWeight();
 		}
 		
 		if (memberName.equals("vertices")) {
 			return this.getVertices();
 		}
 		
 		if (memberName.equals("startVertex")) {
 			return this.getStartVertex();
 		}
 		
 		if (memberName.equals("endVertex")) {
 			return this.getEndVertex();
 		}
 		
 		if (memberName.equals("color")) {
 			return this.getColor();
 		}
 
 		return null;
 	}
 
 	public PCString getColor() {
 		return this.color;
 	}
 
 	public PCVertex getStartVertex() {
 		// TODO is not necessary v1
 		return this.startVertex;
 	}
 
 	public PCVertex getEndVertex() {
 		// TODO is not necessary v1
 		return this.endVertex;
 	}
 
 	public PCInteger getWeight() {
 		return this.weight;
 	}
 
 	public void setColor(PCString color) {
 		this.color = color;
 		if (!this.isInBatchRun) {
 			for (GraphicalRepresentation gredge : allGr) {
 				((GraphicalRepresentationEdge) gredge).setColor(color
 						.getLiteralValue());
 
 			}
 		} else {
 			commandsforGr.get(1).push(weight);
 		}
 	}
 
 	public void setWeight(PCInteger weight) {
 		this.weight = weight;
 		if (!this.isInBatchRun) {
 			for (GraphicalRepresentation gredge : allGr) {
 				((GraphicalRepresentationEdge) gredge)
 						.setWeight(((PCInteger) weight).getLiteralValue());
 			}
 		} else {
 			commandsforGr.get(0).push(weight);
 		}
 	}
 
 	@Override
 	public PCObject set(String memberName, PCObject value) {
 		if (memberName.equals("weight")) {
 			this.setWeight((PCInteger) value);
 		}
 		return value;
 	}
 
 	@Override
 	public boolean equals(PCObject toCheckAgainst) {
 		if (((PCEdge) toCheckAgainst).startVertex.equals(this.startVertex)
 				&& ((PCEdge) toCheckAgainst).endVertex.equals(this.endVertex)
 				&& ((PCEdge) toCheckAgainst).isDirected.equals(this.isDirected)) {
 			return true;
 		}
 
 		return false;
 	}
 
 	public static String getTypeName() {
 		return PCEdge.TYPENAME;
 	}
 
 	public boolean isGraphical(GraphicalRepresentationEdge edge) {
 		for (GraphicalRepresentation gr : this.allGr) {
 			if (gr == edge) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	protected void runDelayedCommands() {
 		for (GraphicalRepresentation gr : allGr) {
 			if (commandsforGr.get(0) != null && !commandsforGr.get(0).isEmpty()) {
 				((GraphicalRepresentationEdge) gr)
 						.setWeight(((PCInteger) commandsforGr.get(0).pop())
 								.getLiteralValue());
 			}
 			if (commandsforGr.get(1) != null && !commandsforGr.get(1).isEmpty()) {
 				((GraphicalRepresentationEdge) gr)
 						.setColor(((PCString) commandsforGr.get(1).pop())
 								.getLiteralValue());
 			}
 		}
 		commandsforGr.get(0).clear();
 		commandsforGr.get(1).clear();
 
 	}
 
 	public static PCEdge getNull() {
 		return new PCEdge();
 	}
 
 	public PCList<PCVertex> getVertices() {
 		PCList<PCVertex> vert = new PCList<PCVertex>();
 		vert.add(startVertex);
 		vert.add(endVertex);
 		return vert;
 	}
 
 	@Override
 	public int compareTo(PCEdge arg0) {
 		// checking just based on cost
 		return this.weight.getLiteralValue() - arg0.weight.getLiteralValue();
 	}
 }

 /*
     ranx - general purpose reactive language
     Copyright (C) 2012  codec4ke@gmail.com
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, version 3 of the License.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 
 package org.ranx;
 
 import java.util.HashSet;
 
 public class Node {
 	private Value _value;
 	private Expression _expr = null;
 	private boolean _valid = false;
 	
 	private HashSet<Node> _ins = new HashSet<Node>();
 	private HashSet<Node> _outs = new HashSet<Node>();
 
 	/**
 	 * Creates Node with no expression attached to it (value() will throw).
 	 */
 	public Node() {}
 	
 	/** 
 	 * Creates Node with an expression attached to it
 	 * @param expr_ expression
 	 */
 	public Node(Expression expr_) { 
 		_expr = expr_;
 	}
 	
 	/**
 	 * Returns the node expression
 	 * @return expression
 	 */
 	public Expression expression() { return _expr; }
 	
 	/**
 	 * Sets the node expression
 	 * @param expr_ expression
 	 */
 	public void expression(Expression expr_) { 
 		_expr = expr_;
 		invalidate();
 	}
 	
 	/**
 	 * Returns true if the node valid (has a value)
 	 * @return valid flag
 	 */
 	public boolean valid() { return _valid; }
	public void invalidate() {
		_valid = false;
		for(Node n : outs()) {
			n.invalidate();
		}
	}
 
 	/**
 	 * Returns the set of node inputs
 	 * @return set of node inputs
 	 */
 	public Node[] ins() { return _ins.toArray(new Node[0]); }
 	
 	/**
 	 * Returns the set of node outputs
 	 * @return set of node outputs
 	 */
 	public Node[] outs() { return _outs.toArray(new Node[0]); }
 	
 	/**
 	 * Adds a node input
 	 * @param in input to add
 	 */
 	public void addIn(Node in) { _ins.add(in); }
 	
 	/**
 	 * Adds a node output
 	 * @param out output to add
 	 */
 	public void addOut(Node out) { _outs.add(out); }
 	
 	/**
 	 * Connect the node to another one
 	 * @param that node to connect to
 	 */
 	public void connectTo(Node that) { this.addOut(that); that.addIn(this); }
 	
 	/**
 	 * Remove node input
 	 * @param in input to remove
 	 */
 	public void removeIn(Node in) { _ins.remove(in); }
 	
 	/**
 	 * Remove node output
 	 * @param out output to remove
 	 */
 	public void removeOut(Node out) { _outs.remove(out); }
 	
 	/**
 	 * Disconnect from another node. This is mutual, the other node disconnects to (connection is always bi-directional).
 	 * The direction doesn't matter, disconnects both upstream and downstream nodes.
 	 * @param that node to disconnect 
 	 */
 	public void disconnect(Node that) {
 		if(_ins.remove(that)) {
 			that.removeOut(this);
 		} else if(_outs.remove(that)) {
 			that.removeIn(this);
 		}
 	}
 	
 	/**
 	 * Returns true if this node is connected the other one.
 	 * @param that node to test
 	 * @return true if this node is connected
 	 */
 	public boolean isConnectedTo(Node that) { return _outs.contains(that); }
 	
 	/**
 	 * Returns true if this node is connected from the other one.
 	 * @param that node to test
 	 * @return true if this node is connected
 	 */
 	public boolean isConnectedFrom(Node that) { return _ins.contains(that); }
 	
 	/**
 	 * Returns the node value, calculates it if needed
 	 * @return node's value
 	 * @throws InvalidOperation
 	 */
 	public Value value() throws InvalidOperation { 
 		if(_valid) {
 			return _value;
 		}
 		_value = _expr.eval();
 		_valid = true;
 		return _value;
 	}
 	
 	/**
 	 * Invalidates this node and all the downstream nodes
 	 */
 	public void invalidate() {
 		_valid = false;
 		for(Node n : _outs) {
 			n.invalidate();
 		}
 	}
 }

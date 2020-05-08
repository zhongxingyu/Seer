 package netproj.hosts;
 
 import netproj.skeleton.Router;
 import netproj.skeleton.Message;
 
 public class StaticRouter extends Router {
 	private TrieNode routingTrie;
 
 	public StaticRouter(int inputBuffSize, int numGates, int address) {
 		super(inputBuffSize, numGates, address);
 		routingTrie = new TrieNode();
 	}
 	
 	public void addRoutingTableEntry(int dest, int subnet, int gate) {
		routingTrie.addChild(new TrieNode(dest, subnet, gate));
 	}
 	
 	@Override
 	public void processMessage(Message msg) {
 		System.err.println("Message " + msg + " received at router " +
 				           Integer.toString(getAddress(), 16));
 		int gate = routingTrie.findGateByAddress(msg.getDestAddress());
 		if (gate == -1) {
 			System.err.println("No gate set at router " + getAddress() + " for message " + msg);
 			System.exit(1);
 		}
 		sendMessage(msg, gate);
 	}
 	
 	private class TrieNode {
 		// children[0] is the child whose value has prefix 0; likewise for children[1]
 		private TrieNode[] children;
 		// length of the prefix used to reach this node (0 for root)
 		private int length;
 		// prefix used to reach this node (0 for root)
 		private int value;
 		// gate associated with the current prefix (-1 if none is associated, e.g. for intermediate nodes)
 		private int gate;
 		
 		public TrieNode() {
 			length = value = 0;
 			gate = -1;
 			children = new TrieNode[2];
 		}
 		
 		public TrieNode(int value, int length, int gate) {
 			setData(value, length);
 			this.gate = gate;
 			children = new TrieNode[2];
 		}
 		
 		public void addChild(TrieNode n) {
 			/* Six cases to consider:
 			 *  0) n is length 0 (happens at root only)
 			 *     - we're setting the default gate, let gate = n's gate
 			 *  1) current has no children with a common prefix with n
 			 *     - make n the new child
 			 *  2) current has a child matching n
 			 *     - update the matching child's gate to the new gate (n's gate)
 			 *  3) n is a prefix of a child of current
 			 *     - insert n as a child of current
 			 *     - add the previous child of current as a child of n
 			 *  4) current has a child which is a prefix of n
 			 *     - recursively try to add n to the child
 			 *  5) current has a child which shares a common prefix with n
 			 *     - create an intermediate node representing the common prefix
 			 *     - add both n and the child of current as children of the intermediate
 			 *     - make the intermediate node the new child of current
 			 */
 			int first = n.getValue() >>> (n.getLength() - 1);
 			TrieNode c = children[first];
 			if (n.getLength() == 0)
 				// Case 0
 				gate = n.getGate();
 			else if (c == null)
 				// Case 1
 				children[first] = n;
 			else {
 				int lengthDiff = n.getLength() - c.getLength();
 				if (lengthDiff == 0 && (n.getValue() ^ c.getValue()) == 0) {
 					// Case 2
 					c.setGate(n.getGate());
 				} else if (lengthDiff < 0 && (n.getValue() ^ (c.getValue() >>> (-lengthDiff))) == 0) {
 					// Case 3
 					c.setData(c.getValue(), -lengthDiff);
 					n.addChild(c);
 					children[first] = n;
 				} else if (lengthDiff > 0 && ((n.getValue() >>> lengthDiff) ^ c.getValue()) == 0) {
 					// Case 4
 					n.setData(n.getValue(), lengthDiff);
 					c.addChild(n);
 				} else {
 					// Case 5
 					
 					// Shift up such that both values are of equal effective length
 					int nVal = n.getValue() << (lengthDiff < 0 ? -lengthDiff : 0);
 					int cVal = c.getValue() << (lengthDiff > 0 ? lengthDiff : 0);
 					
 					// Compute the length of the common prefix
 					int suff = (Integer.highestOneBit(nVal ^ cVal) << 1) - 1;
 					int prefixLength = Math.max(n.getLength(), c.getLength());
 					while (suff > 0) {
 						suff >>>= 1;
 						prefixLength--;
 					}
 					
 					// Compute the common prefix
 					int prefix;
 					if (n.getLength() > c.getLength())
 						prefix = n.getValue() >>> (n.getLength() - prefixLength);
 					else
 						prefix = c.getValue() >>> (c.getLength() - prefixLength);
 					
 					// Create the new intermediate node from the common prefix
 					TrieNode prefixNode = new TrieNode();
 					prefixNode.setData(prefix, prefixLength);
 					n.setData(n.getValue(), n.getLength() - prefixLength);
 					c.setData(c.getValue(), c.getLength() - prefixLength);
 					prefixNode.addChild(n);
 					prefixNode.addChild(c);
 					children[first] = prefixNode;
 				}
 			}
 		}
 		
 		public void setData(int value, int length) {
 			this.length = length;
 			if (length == 32) {
 				this.value = value;
 			} else {
 				// Truncate the value to the given length
 				this.value = value & ((1 << length) - 1);
 			}
 		}
 		public void setGate(int gate) {
 			this.gate = gate;
 		}
 		
 		public int getValue() {
 			return value;
 		}
 		public int getLength() {
 			return length;
 		}
 		public int getGate() {
 			return gate;
 		}
 		@SuppressWarnings("unused")
 		public TrieNode getChild(int i) {
 			return children[i];
 		}
 		
 		// This is the only function the end-user really has to care about --
 		// it takes an address and matches it with the gate from the longest prefix in the routing table
 		public int findGateByAddress(int addr) {
 			return findGate(addr, 32);
 		}
 		private int findGate(int addr, int len) {
 			if (len == 0) {
 				// This is a complete match, so we're done
 				return gate;
 			}
 			int first = addr >>> (len - 1);
 			if (children[first] == null) {
 				// No relevant child, so we're done
 				return gate;
 			} else {
 				int lengthDiff = len - children[first].getLength();
 				
 				// If the value of the relevant child is not a prefix or equal to the target, we can't go deeper
 				if ((children[first].getValue() ^ (addr >>> lengthDiff)) != 0) {
 					return gate;
 				} else {
 					// But if the relevant child is good, we jump one step down
 					int ret = children[first].findGate(addr & ((1 << lengthDiff) - 1), lengthDiff);
 					return ret == -1 ? gate : ret;
 				}
 			}
 		}
 		
 		// FOR DEBUGGING -- calling toString() will print the tree rooted at the node on which it is called
 		public String toString() {
 			return toString(0);
 		}
 		public String toString(int indent) {
 			String pref = "";
 			for (int i = 0; i < indent; i++)
 				pref += "\t";
 			String ret = pref + "(L = "+length+", V = "+Integer.toString(value,16)+", G = "+gate+")\n";
 			if (children[0] != null)
 				ret += children[0].toString(indent+1);
 			else { for (int i = 0; i <= indent; i++) ret += "\t"; ret += "null\n"; }
 			if (children[1] != null)
 				ret += children[1].toString(indent+1);
 			else { for (int i = 0; i <= indent; i++) ret += "\t"; ret += "null\n"; }
 			return ret;
 		}
 	}
 }

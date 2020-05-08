 package bioGUI;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class trie {
 	
 	public class Edge{
 		String data;	//suffix
 		private Node node;		//child to Edge
 		int start;		//character index of S start,end
 		int end;
 		
 		public Edge(String data, Node node, int start, int end){
 			this.data = data;
 			this.setNode(node);
 			this.start = start;
 			this.end = end;
 		}
 
 		public Node getNode() {
 			return node;
 		}
 
 		public void setNode(Node node) {
 			this.node = node;
 		}
 		
 	}
 	public class Node{
 //		boolean isLeaf;
 		private int leafNum = -1;	//if no children, then Node has (leafNum != -1)
 		private List<Edge> child = null;  
 		private String label;
 		
 		public Node(){
 			this.setChild(new ArrayList<Edge>());
 		}
 		public Node(int leafNum, String label){
 			this.setChild(new ArrayList<Edge>());
 			this.setLeafNum(leafNum);
 			this.setLabel(label);
 		}	
 		public Node(Edge in){
 			this.setChild(new ArrayList<Edge>());
 			this.getChild().add(in);
 		}
 		
 		public void Insert(String in, int leafCount, String label, int startIndex, int endIndex){
 			int targetChild = -1;
 			int x = 0;
 			int lostCharCount = 0;
 			List<Integer> badChild = new ArrayList<Integer>();
 			Edge oldEdge;
 			
 			while (x < in.length()+1){ //for each char(in)
 				
 				for (int i = 0; i < getChild().size(); i++){  //for each child to the currentNode
 					if (!(badChild.contains(i)) &&   //if the child's previous chars matched
 							x < getChild().get(i).data.length()-1 && 
 							x < in.length()-1){
 							if (in.charAt(x) == getChild().get(i).data.charAt(x)){//and this char matches
 								targetChild = i;		//update child we'll add to
 								lostCharCount = x+1;		//increase num of matched chars
 							}
 							else{
 								badChild.add(i);		//if char doesn't match, don't check child again
 							}
 					}
 				}				
 				x++;
 			}
 
 			if (targetChild != -1){
 				oldEdge = getChild().get(targetChild);		//save the targetChild
 				oldEdge.data = oldEdge.data.substring(lostCharCount);   //modify its string
 				oldEdge.start += lostCharCount;		//and its startindex
 				
 				getChild().set(targetChild,  		//set targetChild's Node's child to new Child 
 						new Edge				//with Edge String containing matched char's
 						(in.substring(0, lostCharCount), new Node(oldEdge),  //and it's child Node equal to the modified oldEdge
 								startIndex, lostCharCount));
 				
 				//finally, call Insert() again on the newly created Node
 				(getChild().get(targetChild).getNode()).Insert(in.substring(lostCharCount), leafCount, label, startIndex+lostCharCount, endIndex);
 			}
 			else{
 				//base case - no children have any matching substrings, so create new Edge/Leaf
				getChild().add(new Edge(in, new Node(++leafCount, label), startIndex, endIndex));
 			}
 			
 		}
 		
 		public String PrintTree(){
 			String out = "";
 			
 			for (int i = 0; i < this.getChild().size(); i++){
 				out += this.getChild().get(i).data;
 	//			out += "        ";
 				out += this.getChild().get(i).getNode().PrintTree();
 				out += "  \n";
 				
 			}
 			
 			
 			return out;
 		}
 		
 		
 
 		public List<Edge> getChild() {
 			return child;
 		}
 		public void setChild(List<Edge> child) {
 			this.child = child;
 		}
 		public int getLeafNum() {
 			return leafNum;
 		}
 		public void setLeafNum(int leafNum) {
 			this.leafNum = leafNum;
 		}
 		public String getLabel() {
 			return label;
 		}
 		public void setLabel(String label) {
 			this.label = label;
 		}
 
 	}
 	
 	/* 
 	 * -Has s leaves numbered from 1...s
 	 * -Every internal node has 2 children (except root)
 	 * -Each edge labeled w/ non-empty substring of S
 	 * -No 2 edges starting from same node can have Strings beginning w/ same char
 	 * -String obtained from concatenating all labels from root to leaf i = suffix(S, i...m)
 	 * 		for i = 1...s		*So root to leaf i = string from i to end
 	 */
 	public class Tree{
 		Node root;
 		int leafCount;
 		
 		public Tree(){
 			this.root = new Node();
 			this.leafCount = 0;
 		}
 		
 		//calls addSuffix for i(0,m), String(0+i,m)
 		//be sure to init tree first...
 		public void StringToTree(String in){
 			//make sure end char is there
 			if (in.charAt(in.length()-1) != '$'){
 				in += '$';
 			}				
 			String label = "";
 			for (int i = 0; i < in.length(); i++){
 				if (i > 0){
 					label = in.substring(i-1,i);
 				}
 				this.addSuffix(in.substring(i), label, i, in.length());
 			}
 		}		
 
 		//uses recursive Node.Insert function to add suffix
 		public void addSuffix(String in, String label, int startIndex, int endIndex){
			root.Insert(in, leafCount, label, startIndex, endIndex);
 		}
 		
 		//returns List<Integer> locations
 		public List<Integer> FindString(String in){
 			List<Integer> locations = new ArrayList<Integer>();
 			
 			
 			
 			
 			return locations;
 		}
 		
 		public String GetSubString(int i){
 			return GetSubString(root, i);
 		}
 		
 		//returns substring created by walking from root...leaf[i]
 		public String GetSubString(Node r, int i){
 			String out = "";
 			for(Edge e : r.getChild())
 			{
 				if(e.getNode().getLeafNum() == i){
 					out = e.data;
 				}
 				else if(e.getNode().getLeafNum() == -1){
 					out = e.data+GetSubString(e.getNode(),i);
 				}
 			}	
 			return out;
 		}
 		
 		public String PrintTree(){
 			String out = root.PrintTree();
 			
 			return out;
 		}
 		
 		
 	}	
 }

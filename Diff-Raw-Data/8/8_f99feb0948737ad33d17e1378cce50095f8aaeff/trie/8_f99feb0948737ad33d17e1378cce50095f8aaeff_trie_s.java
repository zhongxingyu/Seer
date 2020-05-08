 package bioGUI;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class trie {
 	
 	public static class StrMgr{
 		public static String rootString = "";
 	}
 	
 	public class Edge{
 		String data;	//suffix
 		private Node node;		//child to Edge
 		int[] start;		//character index of S start,end
 		int[] end;
 		int count;
 		
 		public Edge(String data, Node node, int start, int end){
 			this.data = data;
 			this.setNode(node);
 			this.start = new int[100];
 			this.end = new int[100];
 			this.start[0] = start;
 			this.end[0] = end;
 			this.count = 0;	//COUNT STARTS AT 0
 		}
 
 		public Node getNode() {
 			return node;
 		}
 
 		public void setNode(Node node) {
 			this.node = node;
 		}
 		
 		public void addIndex(int i, int j) {
 			if(count+1 >= this.start.length){
 				int[] newStart = new int[this.start.length*2];
 				for(int x = 0; x < this.start.length; x++){
 					newStart[x] = this.start[x];
 				}
 				
 				int[] newEnd = new int[this.end.length*2];
 				for(int x = 0; x < this.end.length; x++){
 					newEnd[x] = this.end[x];
 				}
 				this.start = newStart;
 				this.end = newEnd;
 			}
 			this.start[++count] = i;
 			this.end[count] = j;
 		}
 		
 	}
 	public class Node extends StrMgr{
 //		boolean isLeaf;
 
 		private int leafNum = -1;	//if no children, then Node has (leafNum != -1)
 		private List<Edge> child = null;  
 		private String label;
 		
 		public Node(){
 			this.child = new ArrayList<Edge>();
 			this.label = "";
 		}
 		public Node(int leafNum, String label){
 			this.child = new ArrayList<Edge>();
 			this.leafNum = leafNum;
 			this.label = label;
 		}	
 		public Node(Edge in){
 			this.child = new ArrayList<Edge>();
 			this.child.add(in);
 			this.label = "";
 		}
 		
 		public void Insert(int leafCount, String label, int startIndex, int endIndex){
 			int targetChild = -1;
 			int x = 0;
 			int lostCharCount = 0;
 			Edge oldEdge;
 			boolean found = false;
			//String in = StrMgr.rootString.substring(startIndex);
 			
 			int i = 0;
 			while (i < child.size() && !found){
 				if (x < child.get(i).data.length() && 
 						x < StrMgr.rootString.substring(startIndex).length() &&
 						StrMgr.rootString.substring(startIndex).charAt(x) == child.get(i).data.charAt(x)){
 					found = true;
 					targetChild = i;
 					while (x < child.get(i).data.length() && 
 							x < StrMgr.rootString.substring(startIndex).length() &&
 							StrMgr.rootString.substring(startIndex).charAt(x) == child.get(i).data.charAt(x)){
 
 						lostCharCount = ++x;		//increase num of matched chars
 					}
 				}
 				i++;
 			}
 			
 			if (targetChild != -1){
 				
//				System.out.println("!!!!!!!");
//				System.out.println("charCount   " + lostCharCount + "    length()    " + child.get(targetChild).data.length());
 				if (lostCharCount+1 < child.get(targetChild).data.length()){
 					oldEdge = child.get(targetChild);		//save the targetChild
 					oldEdge.data = oldEdge.data.substring(lostCharCount);   //modify its string
 					oldEdge.start[oldEdge.count] += lostCharCount;		//and its startindex
 					
 					child.set(targetChild,  		//set targetChild's Node's child to new Child
 							new Edge				//with Edge String containing matched char's
 							(StrMgr.rootString.substring(startIndex).substring(0, lostCharCount), new Node(oldEdge),  //and it's child Node equal to the modified oldEdge
 									startIndex, startIndex+lostCharCount));	
 					
 					//add in the old indices
 					child.get(targetChild).addIndex(oldEdge.start[oldEdge.count]-lostCharCount, oldEdge.end[oldEdge.count]);					
 				}
 				else{
 //					System.out.println("!!!!!!!!!!!!!!!!!");
 //					System.out.println(lostCharCount + "   " + child.get(targetChild).data.length());
 					//add in new indices
 					child.get(targetChild).addIndex(startIndex, startIndex+lostCharCount);
 				}
 				
 				//finally, call Insert() again on the newly created Node
 				(child.get(targetChild).getNode()).Insert(leafCount, label, startIndex+lostCharCount, endIndex);
 			}
 			else{
 				//base case - no children have any matching substrings, so create new Edge/Leaf
 				child.add(new Edge(StrMgr.rootString.substring(startIndex), new Node(leafCount, label), startIndex, endIndex));
 			}
 			
 		}
 		
 		public String PrintTree(){
 			String out = "";
 			
 			for (int i = 0; i < this.child.size(); i++){
 				out += this.child.get(i).data;
 	//			out += "        ";
 				out += this.child.get(i).getNode().PrintTree();
 				out += "  \n";
				
 			}
 			
 			
 			return out;
 		}
 		
 		public String GSS(int i){
 			String currentString = "";
 			Edge currentEdge = null;
 			
 			//if node isn't a leaf
 			if (this.leafNum == -1){
 				for (int j = 0; j < this.child.size(); j++){
 					currentEdge = child.get(j);
 					currentString = currentEdge.data;
 					currentString += currentEdge.node.GSS(i);
 					if (currentString.charAt(currentString.length()-1) == ' '){
 						
 						break;
 					}
 				}					
 				return currentString;
 			}
 			else{
 				//if it is the CORRECT leaf
 				if (this.leafNum == i){
 					return " ";
 				}
 				//if it is the INCORRECT leaf
 				else{
 					return "_";
 				}
 				
 			}
 
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
 	public class Tree extends StrMgr{
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
 			
 			int ln = in.length();
 			StrMgr.rootString = in;
 			
 			for (int i = 0; i < in.length(); i++){
 				System.out.println("suffix i:"+i);
 				if (i > 0){
 					label = in.substring(i-1,i);
 				}
 				this.addSuffix(label, i, ln);
 			}
 		}		
 
 		//uses recursive Node.Insert function to add suffix
 		public void addSuffix(String label, int startIndex, int endIndex){
 			root.Insert(++leafCount, label, startIndex, endIndex);
 		}
 		
 		//returns List<Integer> locations
 		public List<Integer> FindString(String in){
 			List<Integer> locations = new ArrayList<Integer>();
 			
 			
 			
 			
 			return locations;
 		}
 		
 		//returns substring created by walking from root...leaf[i]
 		public String GetSubString(int i){						
 			return root.GSS(i);
 		}
 		
 		public String PrintTree(){
 			String out = root.PrintTree();
 			
 			return out;
 		}
 		
		
 	}	
 }

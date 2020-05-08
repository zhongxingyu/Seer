 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.PriorityQueue;
 
 public class SimpleHuffProcessor implements IHuffProcessor {
     
     private HuffViewer myViewer;
     private TreeNode myRoot; 
     private HashMap<Integer, String> myMap; 
     private Integer mySize; 
     
     public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
     	// write the magic number
     	BitOutputStream bout = new BitOutputStream(out); 
     	bout.writeBits(BITS_PER_INT, MAGIC_NUMBER); 
     	
     	// write info that allows tree to be recreated
     	writeTraversal(myRoot, bout); 
     	
     	// write bits needed to encode each character of input file 
     	BitInputStream binput = new BitInputStream(in);
         int next = 1;
         while(next > 0){
         	next = binput.read(); 
         	String encoding = myMap.get(next);
         	for(int i=0; i<encoding.length(); i++){
         		char c = encoding.charAt(i);
         		if(c=='0'){ bout.writeBits(1, 0);}
         		else if(c=='1'){ bout.writeBits(1, 1); } 
         	}
         }
         bout.writeBits(BITS_PER_INT, PSEUDO_EOF);
         binput.close(); 
     	bout.close(); 
     	
     	return 0; 
     }
 
 
     public int preprocessCompress(InputStream in) throws IOException {
     	// create forest of nodes
     	HashMap<Integer, TreeNode> forest = new HashMap<Integer, TreeNode>(); 
     	BitInputStream binput = new BitInputStream(in);
         int next = 1;
         while(next > 0){
         	next = binput.read(); 
         	if(forest.containsKey(next)){
         		TreeNode node = forest.get(next);
         		node.myWeight++;
         		forest.put(next, node);
         	}
         	else{
         		TreeNode node = new TreeNode(next, 1); 
         		forest.put(next, node);
         	}
         }
         binput.close(); 
         
         // turn forest into a single tree
         PriorityQueue<TreeNode> pq = new PriorityQueue<TreeNode>(forest.values()); 
         myRoot = qShrinker(pq); 
         
         //create map of ints to encodings
         myMap = new HashMap<Integer, String>(); 
         mySize=0; 
         encodePaths(myRoot, ""); 
         
        return myRoot.myWeight*8 - mySize; 
     }
     
     public TreeNode qShrinker(PriorityQueue<TreeNode> q){
     	TreeNode tree; 
     	if(q.size()==1){
     		tree = q.poll(); 
     	}
     	else{
     		TreeNode smallest = q.remove();
     		TreeNode nextSmallest = q.remove();
     		TreeNode newNode = new TreeNode(smallest.myValue, nextSmallest.myWeight+smallest.myWeight, smallest, nextSmallest);
     		q.add(newNode); 
     		tree = qShrinker(q); 
     	}
     	return tree; 
     }
     
     public void encodePaths(TreeNode t, String path){
     	if(t.isLeaf()){
     		myMap.put(t.myValue, path); 
     		mySize += t.myWeight*path.length(); 
     		return; 
     	}
     	else{
     		encodePaths(t.myLeft, path + "0");
     		encodePaths(t.myRight, path + "1");
     	}
     }
     
     public void writeTraversal(TreeNode t, BitOutputStream out){
     	if(t.isLeaf()){
     		out.writeBits(BITS_PER_INT, 1);
     		out.writeBits(BITS_PER_INT, t.myValue);
     	}
     	else{
     		out.writeBits(BITS_PER_INT, 0);
     		writeTraversal(t.myLeft, out);
     		writeTraversal(t.myRight, out); 
     	}
     }
 
     public void setViewer(HuffViewer viewer) {
         myViewer = viewer;
     }
 
     public int uncompress(InputStream in, OutputStream out) throws IOException {
         throw new IOException("uncompress not implemented");
         //return 0;
         
 //        int magic = in.readBits(BITS_PER_INT);
 //        if (magic != MAGIC_NUMBER){
 //            throw new IOException("magic number not right");
 //        }
     }
     
     private void showString(String s){
         myViewer.update(s);
     }
 
 }

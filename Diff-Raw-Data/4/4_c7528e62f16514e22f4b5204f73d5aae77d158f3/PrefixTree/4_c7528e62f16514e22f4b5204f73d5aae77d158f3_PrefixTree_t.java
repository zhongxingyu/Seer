 public class PrefixTree {
     private char character;
 	private PrefixTree left;
 	private PrefixTree right;
 
 	public PrefixTree() {
 		character = CharStdIn.readChar();
 		if (character == '*') {
 			left = new PrefixTree();
 			right = new PrefixTree();
 		} else {
 			
 		}
 	}
 
 	public void preorder(String encoding) {
 		if (character == '*') {
 			left.preorder(encoding + "0");
 			right.preorder(encoding + "1");
 			
 		} else {
            //TODO: max format numbers
    		System.out.printf("%16s%16d%16s\n",character,encoding.length(),encoding);
 		}
 
 	}
 
 	public static void main(String[] args) {
 		PrefixTree tree = new PrefixTree();
 		tree.preorder("");
 	}
 }

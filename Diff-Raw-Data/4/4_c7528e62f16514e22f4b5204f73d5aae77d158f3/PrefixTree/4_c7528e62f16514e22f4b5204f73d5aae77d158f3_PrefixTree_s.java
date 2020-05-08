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
			System.out.print(character + " ");
			System.out.println(encoding);
 		}
 
 	}
 
 	public static void main(String[] args) {
 		PrefixTree tree = new PrefixTree();
 		tree.preorder("");
 	}
 }

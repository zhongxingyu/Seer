 package suite.instructionexecutor;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.Writer;
 
 import suite.node.Atom;
 import suite.node.Int;
 import suite.node.Node;
 import suite.node.Tree;
 import suite.util.FunUtil.Fun;
 
 public class ExpandUtil {
 
 	/**
 	 * Evaluates the whole (lazy) term to a list of numbers, and converts to a
 	 * string.
 	 */
 	public static String expandString(Fun<Node, Node> unwrapper, Node node) {
 		StringWriter writer = new StringWriter();
 
 		try {
 			expandToWriter(unwrapper, node, writer);
 		} catch (IOException ex) {
 			throw new RuntimeException(ex);
 		}
 
 		return writer.toString();
 	}
 
 	/**
 	 * Evaluates the whole (lazy) term to a list of numbers, and write
 	 * corresponding characters into the writer.
 	 */
 	public static void expandToWriter(Fun<Node, Node> unwrapper, Node node, Writer writer) throws IOException {
 		while (true) {
 			node = unwrapper.apply(node);
 			Tree tree = Tree.decompose(node);
 
 			if (tree != null) {
 				int c = ((Int) unwrapper.apply(tree.getLeft())).getNumber();
 				writer.write(c);
 				node = tree.getRight();
 
 				if (c == 10)
 					writer.flush();
 			} else if (node == Atom.NIL)
 				return;
 			else
 				throw new RuntimeException("Not a list, unable to expand");
 		}
 	}
 
 	/**
 	 * Evaluates the whole (lazy) term to actual by invoking all the thunks.
 	 */
 	public static Node expandFully(Fun<Node, Node> unwrapper, Node node) {
		node = unwrapper.apply(node);

 		if (node instanceof Tree) {
 			Tree tree = (Tree) node;
			Node left = expandFully(unwrapper, tree.getLeft());
			Node right = expandFully(unwrapper, tree.getRight());
 			node = Tree.create(tree.getOperator(), left, right);
 		}
 
 		return node;
 	}
 
 }

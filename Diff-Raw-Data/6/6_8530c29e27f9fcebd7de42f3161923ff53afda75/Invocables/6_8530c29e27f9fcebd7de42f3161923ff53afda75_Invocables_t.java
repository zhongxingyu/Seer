 package suite.lp.invocable;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 
 import suite.instructionexecutor.ExpandUtil;
 import suite.instructionexecutor.IndexedReader;
 import suite.node.Atom;
 import suite.node.Data;
 import suite.node.Int;
 import suite.node.Node;
 import suite.node.Str;
 import suite.node.Tree;
 import suite.node.io.Formatter;
 import suite.node.io.TermParser.TermOp;
 import suite.util.FileUtil;
 import suite.util.FunUtil.Fun;
 import suite.util.FunUtil.Sink;
 import suite.util.LogUtil;
 
 public class Invocables {
 
 	private static final Atom ATOM = Atom.create("ATOM");
 	private static final Atom NUMBER = Atom.create("NUMBER");
 	private static final Atom STRING = Atom.create("STRING");
 	private static final Atom TREE = Atom.create("TREE");
 	private static final Atom UNKNOWN = Atom.create("UNKNOWN");
 
 	public static class AtomString implements Invocable {
 		public Node invoke(InvocableBridge bridge, List<Node> inputs) {
 			String name = ((Atom) inputs.get(0)).getName();
 
 			if (!name.isEmpty()) {
 				Node left = bridge.wrapInvocable(new Id(), Int.create(name.charAt(0)));
 				Node right = bridge.wrapInvocable(this, Atom.create(name.substring(1)));
 				return Tree.create(TermOp.OR____, left, right);
 			} else
 				return Atom.NIL;
 		}
 	}
 
 	public static class Fgetc implements Invocable {
 		public Node invoke(InvocableBridge bridge, List<Node> inputs) {
 			Data<?> data = (Data<?>) inputs.get(0);
 			int p = ((Int) inputs.get(1)).getNumber();
 			int c = ((IndexedReader) data.getData()).read(p);
 			return Int.create(c);
 		}
 	}
 
 	public static class Id implements Invocable {
 		public Node invoke(InvocableBridge bridge, List<Node> inputs) {
 			return inputs.get(0);
 		}
 	}
 
 	public static class Log1 implements Invocable {
 		public Node invoke(InvocableBridge bridge, List<Node> inputs) {
 			Fun<Node, Node> unwrapper = bridge.getUnwrapper();
 			Node node = inputs.get(0);
 			LogUtil.info(Formatter.display(ExpandUtil.expandFully(unwrapper, node)));
 			return node;
 		}
 	}
 
 	public static class Log2 implements Invocable {
 		public Node invoke(InvocableBridge bridge, List<Node> inputs) {
 			Fun<Node, Node> unwrapper = bridge.getUnwrapper();
 			LogUtil.info(ExpandUtil.expandString(unwrapper, inputs.get(0)));
 			return inputs.get(1);
 		}
 	}
 
 	public static class Popen implements Invocable {
 		public Node invoke(InvocableBridge bridge, List<Node> inputs) {
 			final Fun<Node, Node> unwrapper = bridge.getUnwrapper();
 			final List<String> list = new ArrayList<>();
 
 			ExpandUtil.expandList(unwrapper, inputs.get(0), new Sink<Node>() {
 				public void sink(Node node) {
 					list.add(ExpandUtil.expandString(unwrapper, node));
 				}
 			});
 
 			final Node in = inputs.get(1);
 
 			try {
 				final Process process = Runtime.getRuntime().exec(list.toArray(new String[0]));
 				InputStreamReader isr = new InputStreamReader(process.getInputStream(), FileUtil.charset);
 				Node result = new Data<IndexedReader>(new IndexedReader(isr));
 
 				// Use a separate thread to write to the process, so that read
 				// and write occur at the same time and would not block up.
 				// The input stream is also closed by this thread.
 				// Have to make sure the executors are thread-safe!
 				new Thread() {
 					public void run() {
						try (InputStream pes = process.getErrorStream(); OutputStream pos = process.getOutputStream()) {
							try (Writer writer = new OutputStreamWriter(pos)) {
 								ExpandUtil.expandToWriter(unwrapper, in, writer);
 							}
 
 							process.waitFor();
 						} catch (Exception ex) {
 							LogUtil.error(ex);
 						}
 					}
 				}.start();
 
 				return result;
 			} catch (IOException ex) {
 				throw new RuntimeException(ex);
 			}
 		}
 	}
 
 	public static class Seq implements Invocable {
 		public Node invoke(InvocableBridge bridge, List<Node> inputs) {
 			unwrapFully(bridge.getUnwrapper(), inputs.get(0));
 			return inputs.get(1);
 		}
 
 		public static void unwrapFully(Fun<Node, Node> unwrapper, Node node) {
 			node = unwrapper.apply(node);
 
 			if (node instanceof Tree) {
 				Tree tree = (Tree) node;
 				unwrapFully(unwrapper, tree.getLeft());
 				unwrapFully(unwrapper, tree.getRight());
 			}
 		}
 	}
 
 	public static class Throw implements Invocable {
 		public Node invoke(InvocableBridge bridge, List<Node> inputs) {
 			throw new RuntimeException("Error termination");
 		}
 	}
 
 	public static class TypeOf implements Invocable {
 		public Node invoke(InvocableBridge bridge, List<Node> inputs) {
 			Node node = inputs.get(0);
 			Atom type;
 
 			if (node instanceof Atom)
 				type = ATOM;
 			else if (node instanceof Int)
 				type = NUMBER;
 			else if (node instanceof Str)
 				type = STRING;
 			else if (node instanceof Tree)
 				type = TREE;
 			else
 				type = UNKNOWN;
 
 			return type;
 		}
 	}
 
 }

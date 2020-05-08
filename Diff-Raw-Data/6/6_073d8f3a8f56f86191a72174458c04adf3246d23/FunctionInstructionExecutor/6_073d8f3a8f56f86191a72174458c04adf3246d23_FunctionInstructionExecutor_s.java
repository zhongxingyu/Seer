 package org.instructionexecutor;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.Reader;
 import java.io.Writer;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.instructionexecutor.InstructionUtil.Activation;
 import org.instructionexecutor.InstructionUtil.Closure;
 import org.instructionexecutor.InstructionUtil.Frame;
 import org.instructionexecutor.InstructionUtil.Instruction;
 import org.instructionexecutor.io.IndexedIo.IndexedInput;
 import org.instructionexecutor.io.IndexedIo.IndexedOutput;
 import org.instructionexecutor.io.IndexedIo.IndexedReader;
 import org.instructionexecutor.io.IndexedIo.IndexedWriter;
 import org.suite.SuiteUtil;
 import org.suite.doer.Comparer;
 import org.suite.doer.Formatter;
 import org.suite.doer.Generalizer;
 import org.suite.doer.Prover;
 import org.suite.doer.TermParser.TermOp;
 import org.suite.node.Atom;
 import org.suite.node.Int;
 import org.suite.node.Node;
 import org.suite.node.Reference;
 import org.suite.node.Tree;
 import org.suite.node.Vector;
 import org.util.IoUtil;
 import org.util.LogUtil;
 
 public class FunctionInstructionExecutor extends InstructionExecutor {
 
 	private static final Atom COMPARE = Atom.create("COMPARE");
 	private static final Atom CONS = Atom.create("CONS");
 	private static final Atom FGETC = Atom.create("FGETC");
 	private static final Atom FPUTC = Atom.create("FPUTC");
 	private static final Atom HEAD = Atom.create("HEAD");
 	private static final Atom ISTREE = Atom.create("IS-TREE");
 	private static final Atom ISVECTOR = Atom.create("IS-VECTOR");
 	private static final Atom LOG = Atom.create("LOG");
 	private static final Atom LOG2 = Atom.create("LOG2");
 	private static final Atom POPEN = Atom.create("POPEN");
 	private static final Atom PROVE = Atom.create("PROVE");
 	private static final Atom SUBST = Atom.create("SUBST");
 	private static final Atom TAIL = Atom.create("TAIL");
 	private static final Atom VCONCAT = Atom.create("VCONCAT");
 	private static final Atom VCONS = Atom.create("VCONS");
 	private static final Atom VELEM = Atom.create("VELEM");
 	private static final Atom VEMPTY = Atom.create("VEMPTY");
 	private static final Atom VHEAD = Atom.create("VHEAD");
 	private static final Atom VRANGE = Atom.create("VRANGE");
 	private static final Atom VTAIL = Atom.create("VTAIL");
 
 	private Comparer comparer = new Comparer();
 	private Prover prover;
 	private Map<Node, IndexedInput> inputs = new HashMap<>();
 	private Map<Node, IndexedOutput> outputs = new HashMap<>();
 
 	public FunctionInstructionExecutor(Node node) {
 		super(node);
 	}
 
 	/**
 	 * Evaluates the whole (lazy) term to actual by invoking all the thunks.
 	 */
 	public Node unwrap(Node node) {
 		node = node.finalNode();
 
 		if (node instanceof Tree) {
 			Tree tree = (Tree) node;
 			Node left = unwrap(tree.getLeft());
 			Node right = unwrap(tree.getRight());
 			node = Tree.create(tree.getOperator(), left, right);
 		} else if (node instanceof Closure) {
 			Closure closure = (Closure) node;
 			node = unwrap(evaluateClosure(closure));
 		}
 
 		return node;
 	}
 
 	@Override
 	public Node execute() {
 		for (IndexedInput input : inputs.values())
 			input.fetch();
 
 		Node node = super.execute();
 
 		for (IndexedOutput output : outputs.values())
 			output.flush();
 
 		return node;
 	}
 
 	@Override
 	protected void execute(Exec exec, Instruction insn) {
 		Activation current = exec.current;
 		Frame frame = current.frame;
 		Object regs[] = frame != null ? frame.registers : null;
 
 		switch (insn.insn) {
 		case SERVICE_______:
 			exec.sp -= insn.op3;
 			regs[insn.op1] = sys(exec, constantPool.get(insn.op2));
 			break;
 		default:
 			super.execute(exec, insn);
 		}
 	}
 
 	private Node sys(Exec exec, Node command) {
 		Object stack[] = exec.stack;
 		int sp = exec.sp;
 		Node result;
 
 		if (command == COMPARE) {
 			Node left = (Node) stack[sp + 1];
 			Node right = (Node) stack[sp];
 			result = Int.create(comparer.compare(left, right));
 		} else if (command == CONS) {
 			Node left = (Node) stack[sp + 1];
 			Node right = (Node) stack[sp];
 			result = Tree.create(TermOp.AND___, left, right);
		} else if (command == FGETC) {
 			Node node = (Node) stack[sp + 1];
 			int p = ((Int) stack[sp]).getNumber();
 			result = Int.create(inputs.get(node).read(p));
 		} else if (command == FPUTC) {
 			Node node = (Node) stack[sp + 3];
 			int p = ((Int) stack[sp + 2]).getNumber();
 			int c = ((Int) stack[sp + 1]).getNumber();
 			outputs.get(node).write(p, (char) c);
 			result = (Node) stack[sp];
 		} else if (command == HEAD)
 			result = Tree.decompose((Node) stack[sp]).getLeft();
 		else if (command == ISTREE)
 			result = a(Tree.decompose((Node) stack[sp]) != null);
 		else if (command == ISVECTOR)
 			result = a(stack[sp] instanceof Vector);
 		else if (command == LOG) {
 			result = (Node) stack[sp];
 			LogUtil.info("LOG", Formatter.display(unwrap(result)));
 		} else if (command == LOG2) {
 			Node ln = unwrap((Node) stack[sp + 1]);
 			LogUtil.info("LOG2", SuiteUtil.stringize(ln));
 			result = (Node) stack[sp];
 		} else if (command == POPEN) {
 			Node n0 = unwrap((Node) stack[sp + 1]);
 			Node n1 = unwrap((Node) stack[sp]);
 			String cmd = SuiteUtil.stringize(n0);
 			byte in[] = SuiteUtil.stringize(n1).getBytes(IoUtil.charset);
 
 			try {
 				Process process = Runtime.getRuntime().exec(cmd);
 				InputStream pis = process.getInputStream();
 				OutputStream pos = process.getOutputStream();
 
 				pos.write(in);
 				pos.close();
 
 				InputStreamReader reader = new InputStreamReader(pis);
 				inputs.put(result = Atom.unique(), new IndexedReader(reader));
 			} catch (IOException ex) {
 				throw new RuntimeException(ex);
 			}
 		} else if (command == PROVE) {
 			if (prover == null)
 				prover = SuiteUtil.getProver(new String[] { "auto.sl" });
 
 			Node node = (Node) stack[sp];
 			Tree tree = Tree.decompose(node, TermOp.JOIN__);
 			if (tree != null)
 				if (prover.prove(tree.getLeft()))
 					result = tree.getRight().finalNode();
 				else
 					throw new RuntimeException("Goal failed");
 			else
 				result = prover.prove(node) ? Atom.TRUE : Atom.FALSE;
 		} else if (command == SUBST) {
 			Generalizer g = new Generalizer();
 			g.setVariablePrefix("_");
 
 			Node var = (Node) stack[sp + 1];
 			Tree tree = (Tree) g.generalize((Node) stack[sp]);
 			((Reference) tree.getRight()).bound(var);
 			result = tree.getLeft();
 		} else if (command == TAIL)
 			result = Tree.decompose((Node) stack[sp]).getRight();
 		else if (command == VCONCAT) {
 			Vector left = (Vector) stack[sp + 1];
 			Vector right = (Vector) stack[sp];
 			result = Vector.concat(left, right);
 		} else if (command == VCONS) {
 			Node head = (Node) stack[sp + 1];
 			Vector tail = (Vector) stack[sp];
 			result = Vector.cons(head, tail);
 		} else if (command == VELEM)
 			result = new Vector((Node) stack[sp]);
 		else if (command == VEMPTY)
 			result = Vector.EMPTY;
 		else if (command == VHEAD)
 			result = ((Vector) stack[sp]).get(0);
 		else if (command == VRANGE) {
 			Vector vector = (Vector) stack[sp + 2];
 			int s = ((Int) stack[sp + 1]).getNumber();
 			int e = ((Int) stack[sp]).getNumber();
 			return vector.subVector(s, e);
 		} else if (command == VTAIL)
 			result = ((Vector) stack[sp]).subVector(1, 0);
 		else
 			throw new RuntimeException("Unknown system call " + command);
 
 		return result;
 	}
 
 	public void setIn(Reader in) {
 		inputs.put(Atom.NIL, new IndexedReader(in));
 	}
 
 	public void setOut(Writer out) {
 		outputs.put(Atom.NIL, new IndexedWriter(out));
 	}
 
 	public void setProver(Prover prover) {
 		this.prover = prover;
 	}
 
 }

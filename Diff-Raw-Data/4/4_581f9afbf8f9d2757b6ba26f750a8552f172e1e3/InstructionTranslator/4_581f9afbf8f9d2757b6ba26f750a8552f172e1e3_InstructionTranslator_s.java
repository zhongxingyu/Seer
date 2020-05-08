 package org.instructionexecutor;
 
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayDeque;
 import java.util.Arrays;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.tools.JavaCompiler;
 import javax.tools.StandardJavaFileManager;
 import javax.tools.ToolProvider;
 
 import org.instructionexecutor.InstructionUtil.Closure;
 import org.instructionexecutor.InstructionUtil.FunComparer;
 import org.instructionexecutor.InstructionUtil.Insn;
 import org.instructionexecutor.InstructionUtil.Instruction;
 import org.instructionexecutor.TranslatedRunUtil.TranslatedRun;
 import org.suite.doer.Formatter;
 import org.suite.doer.TermParser.TermOp;
 import org.suite.node.Atom;
 import org.suite.node.Node;
 import org.suite.node.Tree;
 import org.util.IoUtil;
 import org.util.LogUtil;
 
 import com.google.common.collect.BiMap;
 import com.google.common.collect.HashBiMap;
 
 /**
  * Possible register types: boolean, closure, int, node
  * (atom/number/reference/tree)
  */
 public class InstructionTranslator {
 
 	private static AtomicInteger counter = new AtomicInteger();
 
 	private BiMap<Integer, Node> constantPool = HashBiMap.create();
 
 	private String basePathName;
 	private String packageName;
 	private String filename;
 	private String className;
 
 	private StringBuilder clazzsec = new StringBuilder();
 	private StringBuilder localsec = new StringBuilder();
 	private StringBuilder switchsec = new StringBuilder();
 
 	private int exitPoint;
 	private Map<Integer, Integer> parentFramesByFrame = new HashMap<>();
 	private Map<Integer, Class<?>[]> registerTypesByFrame = new HashMap<>();
 
 	private Deque<Integer> lastEnterIps = new ArrayDeque<>(); // stack of ENTERs
 
 	private String compare = "comparer.compare(#{reg-node}, #{reg-node})";
 
 	public InstructionTranslator(String basePathName) {
 		this.basePathName = basePathName;
 		this.packageName = getClass().getPackage().getName();
 	}
 
 	public TranslatedRun translate(Node node) throws IOException {
 		InstructionExtractor extractor = new InstructionExtractor(constantPool);
 		List<Instruction> instructions = extractor.extractInstructions(node);
 
 		exitPoint = instructions.size();
 		instructions.add(new Instruction(Insn.EXIT__________, 0, 0, 0));
 
 		// Identify frame regions
 		findFrameInformation(instructions);
 
 		// Find out register types in each frame
 		findRegisterInformation(instructions);
 
 		// Translate instruction code into Java
 		translateInstructions(instructions);
 
 		className = "TranslatedRun" + counter.getAndIncrement();
 
 		String java = String.format("" //
 				+ "package %s; \n" //
 				+ "import org.instructionexecutor.io.*; \n" //
 				+ "import org.suite.*; \n" //
 				+ "import org.suite.doer.*; \n" //
 				+ "import org.suite.kb.*; \n" //
 				+ "import org.suite.node.*; \n" //
 				+ "import org.suite.predicates.*; \n" //
 				+ "import org.util.*; \n" //
 				+ "import org.util.FunUtil.*; \n" //
 				+ "import " + Closeable.class.getCanonicalName() + "; \n" //
 				+ "import " + FunComparer.class.getCanonicalName() + "; \n" //
 				+ "import " + TranslatedRun.class.getCanonicalName() + "; \n" //
 				+ "import " + TranslatedRunUtil.class.getCanonicalName() + ".*; \n" //
 				+ "import " + IOException.class.getCanonicalName() + "; \n" //
 				+ "import " + TermOp.class.getCanonicalName() + "; \n" //
 				+ "\n" //
 				+ "public class %s implements TranslatedRun { \n" //
 				+ "private static final int stackSize = 4096; \n" //
 				+ "\n" //
 				+ "private static final Atom FALSE = Atom.FALSE; \n" //
 				+ "private static final Atom TRUE = Atom.TRUE; \n" //
 				+ "\n" //
 				+ "private IndexedIo indexedIo = new IndexedIo(); \n" //
 				+ "\n" //
 				+ "private Closeable closeable; \n" //
 				+ "\n" //
 				+ "public %s(Closeable closeable) { this.closeable = closeable; } \n" //
 				+ "\n" //
 				+ "public void close() throws IOException { closeable.close(); } \n" //
 				+ "\n" //
 				+ "%s" //
 				+ "\n" //
 				+ "public Node exec(TranslatedRunConfig config, Closure closure) { \n" //
 				+ "Frame frame = closure.frame; \n" //
 				+ "int ip = closure.ip; \n" //
 				+ "Node returnValue = null; \n" //
 				+ "int cs[] = new int[stackSize]; \n" //
 				+ "Node ds[] = new Node[stackSize]; \n" //
 				+ "Object fs[] = new Object[stackSize]; \n" //
 				+ "CutPoint cutPoints[] = new CutPoint[stackSize]; \n" //
 				+ "int csp = 0, dsp = 0, cpsp = 0; \n" //
 				+ "int n; \n" //
 				+ "Node node, n0, n1, var; \n" //
 				+ "CutPoint cutPoint; \n" //
 				+ "\n" //
 				+ "Prover prover = new Prover(config.ruleSet); \n" //
 				+ "Journal journal = prover.getJournal(); \n" //
 				+ "SystemPredicates systemPredicates = new SystemPredicates(prover); \n" //
 				+ "Fun<Node, Node> unwrapper = TranslatedRunUtil.getUnwrapper(config, this); \n" //
 				+ "Comparer comparer = new FunComparer(unwrapper); \n" //
 				+ "\n" //
 				+ "%s \n" //
 				+ "\n" //
 				+ "cs[csp++] = " + exitPoint + "; \n" //
 				+ "\n" //
 				+ "while (true) { \n" //
 				// + "System.out.println(ip); \n" //
 				+ "switch(ip) { \n" //
 				+ "%s \n" //
 				+ "default: \n" //
 				+ "} \n" //
 				+ "} \n" //
 				+ "} \n" //
 				+ "} \n" //
 		, packageName, className, className, clazzsec, localsec, switchsec);
 
 		String pathName = basePathName + "/" + packageName.replace('.', '/');
 		filename = pathName + "/" + className + ".java";
 		new File(pathName).mkdirs();
 
 		try (OutputStream os = new FileOutputStream(filename)) {
 			os.write(java.getBytes(IoUtil.charset));
 		}
 
 		// Compile the Java, load the class, return an instantiated object
 		return getTranslatedRun();
 	}
 
 	private void findFrameInformation(List<Instruction> instructions) {
 
 		// Find out the parent of closures.
 		// Assumes every ENTER has a ASSIGN-CLOSURE referencing it.
 		for (int ip = 0; ip < instructions.size(); ip++) {
 			Instruction insn = instructions.get(ip);
 
 			// Recognize frames and their parents.
 			// Assumes ENTER instruction should be after LABEL.
 			if (insn.insn == Insn.ASSIGNCLOSURE_)
 				parentFramesByFrame.put(insn.op1 + 1, lastEnterIps.peek());
 			else if (insn.insn == Insn.ENTER_________)
 				lastEnterIps.push(ip);
 			else if (insn.insn == Insn.LEAVE_________)
 				lastEnterIps.pop();
 		}
 	}
 
 	private void findRegisterInformation(List<Instruction> instructions) {
 		Class<?> registerTypes[] = null;
 		int ip = 0;
 
 		while (ip < instructions.size()) {
 			Instruction insn = instructions.get(ip);
 			int op0 = insn.op0, op1 = insn.op1, op2 = insn.op2;
 
 			ip++;
 
 			switch (insn.insn) {
 			case EVALEQ________:
 			case EVALGE________:
 			case EVALGT________:
 			case EVALLE________:
 			case EVALLT________:
 			case EVALNE________:
 			case ISTREE________:
 				registerTypes[op0] = boolean.class;
 				break;
 			case ASSIGNCLOSURE_:
 				registerTypes[op0] = Closure.class;
 				break;
 			case ASSIGNINT_____:
 			case BINDMARK______:
 			case COMPARE_______:
 			case CUTBEGIN______:
 			case EVALADD_______:
 			case EVALDIV_______:
 			case EVALMOD_______:
 			case EVALMUL_______:
 			case EVALSUB_______:
 				registerTypes[op0] = int.class;
 				break;
 			case ASSIGNCONST___:
 			case CONS__________:
 			case FGETC_________:
 			case HEAD__________:
 			case LOG1__________:
 			case LOG2__________:
 			case NEWNODE_______:
 			case POP___________:
 			case POPEN_________:
 			case PROVE_________:
 			case SETRESULT_____:
 			case SETCLOSURERES_:
 			case SUBST_________:
 			case TAIL__________:
 			case TOP___________:
 				registerTypes[op0] = Node.class;
 				break;
 			case FORMTREE1_____:
 				registerTypes[insn.op1] = Tree.class;
 				break;
 			case ASSIGNFRAMEREG:
 				int f = lastEnterIps.peek();
 				for (int i = op1; i < 0; i++)
 					f = parentFramesByFrame.get(f);
 				Class<?> clazz1 = registerTypesByFrame.get(f)[op2];
 				if (registerTypes[op0] != clazz1) // Merge into Node if clashed
 					registerTypes[op0] = registerTypes[op0] != null ? Node.class : clazz1;
 				break;
 			case DECOMPOSETREE1:
 				registerTypes[op1] = registerTypes[op2] = Node.class;
 			case ENTER_________:
 				lastEnterIps.push(ip - 1);
 				registerTypesByFrame.put(currentFrame(), registerTypes = new Class<?>[op0]);
 				break;
 			case LEAVE_________:
 				lastEnterIps.pop();
 				break;
 			default:
 			}
 		}
 	}
 
 	private void translateInstructions(List<Instruction> instructions) {
 		Node constant;
 		String s;
 		int ip = 0;
 
 		while (ip < instructions.size()) {
 			Instruction insn = instructions.get(ip);
 			int op0 = insn.op0, op1 = insn.op1, op2 = insn.op2;
 			app("case #{num}: // #{str}", ip, insn);
 
 			// LogUtil.info("Translating instruction (IP = " + ip + ") " +
 			// insn);
 			ip++;
 
 			switch (insn.insn) {
 			case ASSIGNCLOSURE_:
 				app("#{reg} = new Closure(#{fr}, #{num})", op0, op1);
 				break;
 			case ASSIGNFRAMEREG:
 				s = "";
 				for (int i = op1; i < 0; i++)
 					s += ".previous";
 				app("#{reg} = TranslatedRunUtil.toNode(#{fr}#{str}.r#{num})", op0, s, op2);
 				break;
 			case ASSIGNCONST___:
 				constant = constantPool.get(op1);
 				app("#{reg} = #{str}", op0, defineConstant(constant));
 				break;
 			case ASSIGNINT_____:
 				app("#{reg} = #{num}", op0, op1);
 				break;
 			case BIND__________:
 				app("if (!Binder.bind(#{reg-node}, #{reg-node}, journal)) #{jump}", op0, op1, op2);
 				break;
 			case BINDMARK______:
 				app("#{reg} = journal.getPointInTime()", op0);
 				break;
 			case BINDUNDO______:
 				app("journal.undoBinds(#{reg-num})", op0);
 				break;
 			case CALL__________:
 				pushCallee(ip);
 				app("ip = #{reg-num}", op0);
 				app("continue");
 				break;
 			case CALLCONST_____:
 				pushCallee(ip);
 				app("#{jump}", op0);
 				break;
 			case CALLCLOSURE___:
 				app("if (#{reg-clos}.result == null) {", op0);
 				pushCallee(ip);
 				app("frame = #{reg-clos}.frame", op0);
 				app("ip = #{reg-clos}.ip", op0);
 				app("continue");
 				app("} else returnValue = #{reg-clos}.result", op0);
 				break;
 			case COMPARE_______:
 				app("n0 = (Node) ds[--dsp]");
 				app("n1 = (Node) ds[--dsp]");
 				app("#{reg} = comparer.compare(n0, n1)", op0);
 				break;
 			case CONS__________:
 				app("n0 = (Node) ds[--dsp]");
 				app("n1 = (Node) ds[--dsp]");
 				app("#{reg} = Tree.create(TermOp.AND___, n0, n1)", op0);
 				break;
 			case CUTBEGIN______:
 				app("#{reg} = cpsp", op0);
 				app("cutPoint = new CutPoint()");
 				app("cutPoint.frame = #{fr}");
 				app("cutPoint.ip = ip");
 				app("cutPoint.csp = csp");
 				app("cutPoint.jp = journal.getPointInTime()");
 				app("cutPoints[cpsp++] = cutPoint");
 				break;
 			case CUTFAIL_______:
 				app("int cpsp1 = #{reg}", op0);
 				app("cutPoint = cutPoints[cpsp1]");
 				app("while (cpsp > cpsp1) cutPoints[--cpsp] = null");
 				app("${fr} = (${fr-class}) cutPoint.frame");
 				app("csp = cutPoint.csp");
 				app("journal.undoBinds(cutPoint.jp)");
 				app("ip = #{reg}", op1);
 				app("#{jump}", op1);
 				break;
 			case DECOMPOSETREE0:
 				app("node = #{reg-node}.finalNode()", op0);
 				insn = instructions.get(ip++);
 				app("if (node instanceof Tree) {");
 				app("Tree tree = (Tree) node;");
 				app("if (tree.getOperator() == TermOp.#{str}) {", insn.op0);
 				app("#{reg} = tree.getLeft()", insn.op1);
 				app("#{reg} = tree.getRight()", insn.op2);
 				app("} else #{jump}", op1);
 				app("} else if (node instanceof Reference) {");
 				app("Tree tree = Tree.create(op, #{reg} = new Reference(), #{reg} = new Reference())", insn.op1, insn.op2);
 				app("journal.addBind((Reference) node, tree)");
 				app("} else #{jump}", op1);
 				break;
 			case ENTER_________:
 				lastEnterIps.push(ip - 1);
 				app("#{fr} = new #{fr-class}((#{prev-fr-class}) frame)");
 				break;
 			case ERROR_________:
 				app("throw new RuntimeException(\"Error termination\")");
 				break;
 			case EVALADD_______:
 				app("#{reg} = #{reg-num} + #{reg-num}", op0, op1, op2);
 				break;
 			case EVALDIV_______:
 				app("#{reg} = #{reg-num} / #{reg-num}", op0, op1, op2);
 				break;
 			case EVALEQ________:
 				app("#{reg} = " + compare + " == 0", op0, op1, op2);
 				break;
 			case EVALGE________:
 				app("#{reg} = " + compare + " >= 0", op0, op1, op2);
 				break;
 			case EVALGT________:
 				app("#{reg} = " + compare + " > 0", op0, op1, op2);
 				break;
 			case EVALLE________:
 				app("#{reg} = " + compare + " <= 0", op0, op1, op2);
 				break;
 			case EVALLT________:
 				app("#{reg} = " + compare + " < 0", op0, op1, op2);
 				break;
 			case EVALNE________:
 				app("#{reg} = " + compare + " != 0", op0, op1, op2);
 				break;
 			case EVALMOD_______:
 				app("#{reg} = #{reg-num} % #{reg-num}", op0, op1, op2);
 				break;
 			case EVALMUL_______:
 				app("#{reg} = #{reg-num} * #{reg-num}", op0, op1, op2);
 				break;
 			case EVALSUB_______:
 				app("#{reg} = #{reg-num} - #{reg-num}", op0, op1, op2);
 				break;
 			case EXIT__________:
 				if (!lastEnterIps.isEmpty())
 					app("return #{reg}", op0);
 				else
 					app("return returnValue"); // Grand exit point
 				break;
 			case FGETC_________:
 				app("{");
 				app("n0 = (Node) ds[--dsp]");
 				app("int p = ((Int) ds[--dsp]).getNumber()");
 				app("#{reg} = Int.create(indexedIo.get(n0).read(p))", op0);
 				app("}");
 				break;
 			case FORMTREE0_____:
 				insn = instructions.get(ip++);
 				app("#{reg} = Tree.create(TermOp.#{str}, #{reg-node}, #{reg-node})", insn.op1,
 						TermOp.find(((Atom) constantPool.get(insn.op0)).getName()), op0, op1);
 				break;
 			case HEAD__________:
 				app("#{reg} = Tree.decompose((Node) ds[--dsp]).getLeft()", op0);
 				break;
 			case IFFALSE_______:
 				app("if (!#{reg-bool}) #{jump}", op1, op0);
 				break;
 			case IFNOTEQUALS___:
 				app("if (#{reg} != #{reg}) #{jump}", op1, op2, op0);
 				break;
 			case ISTREE________:
 				app("#{reg} = Tree.decompose((Node) ds[--dsp]) != null", op0);
 				break;
 			case JUMP__________:
 				app("#{jump}", op0);
 				break;
 			case LABEL_________:
 				break;
 			case LEAVE_________:
 				generateFrame();
 				lastEnterIps.pop();
 				break;
 			case LOG___________:
 				constant = constantPool.get(op0);
 				app("LogUtil.info(#{str}.toString())", defineConstant(constant));
 				break;
 			case LOG1__________:
 				app("n0 = (Node) ds[--dsp]", op0);
 				app("LogUtil.info(n0.toString())");
 				app("#{reg} = n0", op0);
 				break;
 			case LOG2__________:
 				app("LogUtil.info(((Node) ds[--dsp]).toString())");
 				app("#{reg} = (Node) ds[--dsp]", op0);
 				break;
 			case NEWNODE_______:
 				app("#{reg} = new Reference()", op0);
 				break;
 			case POP___________:
 				app("#{reg} = ds[--dsp]", op0);
 				break;
 			case POPEN_________:
 				app("n0 = ds[--dsp]");
 				app("n1 = ds[--dsp]");
 				app("#{reg} = InstructionUtil.execPopen(n0, n1, indexedIo, unwrapper)", op0);
 				break;
 			case PROVE_________:
 				app("#{reg} = InstructionUtil.execProve((Node) ds[--dsp], prover.config())", op0);
 				break;
 			case PROVESYS______:
 				app("if (!systemPredicates.call(#{reg-node})) #{jump}", op0, op1);
 				break;
 			case PUSH__________:
 				app("ds[dsp++] = #{reg-node}", op0);
 				break;
 			case PUSHCONST_____:
 				app("ds[dsp++] = Int.create(#{num})", op0);
 				break;
 			case REMARK________:
 				break;
 			case RETURN________:
 				popCaller();
 				break;
 			case RETURNVALUE___:
 				app("returnValue = #{reg-node}", op0);
 				popCaller();
 				break;
 			case SETRESULT_____:
 				app("#{reg} = returnValue", op0);
 				break;
 			case SETCLOSURERES_:
 				app("#{reg} = returnValue", op0);
 				app("#{reg-clos}.result = #{reg}", op1, op0);
 				break;
 			case SUBST_________:
 				app("n0 = (Node) ds[--dsp]");
 				app("n1 = (Node) ds[--dsp]");
 				app("#{reg} = InstructionUtil.execSubst(n1, n0)", op0);
 				break;
 			case TAIL__________:
 				app("#{reg} = Tree.decompose((Node) ds[--dsp]).getRight()", op0);
 				break;
 			case TOP___________:
 				app("#{reg} = ds[dsp + #{num}]", op0, op1);
 				break;
 			default:
 				throw new RuntimeException("Unknown instruction " + insn);
 			}
 		}
 	}
 
 	private Integer currentFrame() {
 		return lastEnterIps.peek();
 	}
 
 	private void generateFrame() {
 		Integer frameNo = !lastEnterIps.isEmpty() ? currentFrame() : null;
 		Class<?> registerTypes[] = frameNo != null ? registerTypes = registerTypesByFrame.get(frameNo) : null;
 
 		app(clazzsec, "private static class #{fr-class} implements Frame {");
 		app(clazzsec, "private #{prev-fr-class} previous");
 		app(clazzsec, "private #{fr-class}(#{prev-fr-class} previous) { this.previous = previous; }");
 
 		for (int r = 0; r < registerTypes.length; r++) {
 			String typeName = registerTypes[r].getSimpleName();
 			app(clazzsec, "private #{str} r#{num}", typeName, r);
 		}
 
 		app(clazzsec, "}");
 
 		app(localsec, "#{fr-class} #{fr} = null");
 	}
 
 	private void pushCallee(int ip) {
 		app("cs[csp] = " + ip);
 		app("fs[csp] = #{fr}");
 		app("csp++");
 	}
 
 	private void popCaller() {
 		app("--csp");
 		app("ip = cs[csp]");
 		app("#{prev-fr} = (#{prev-fr-class}) fs[csp]");
 		app("continue");
 	}
 
 	private String defineConstant(Node node) {
 		node = node.finalNode();
 		String result = "const" + counter.getAndIncrement();
 		String decl = "private static final Node #{str} = Suite.parse(\"#{str}\")";
 		app(clazzsec, decl, result, Formatter.dump(node));
 		return result;
 	}
 
 	private void app(String fmt, Object... ps) {
 		app(switchsec, fmt, ps);
 	}
 
 	private void app(StringBuilder section, String fmt, Object... ps) {
 		List<Object> list = Arrays.asList(ps);
 		Iterator<Object> iter = list.iterator();
 
 		while (!fmt.isEmpty()) {
 			int pos0 = fmt.indexOf("#{");
 			int pos1 = fmt.indexOf("}", pos0);
 
 			String s0, s1, s2;
 
 			if (pos0 >= 0 && pos1 >= 0) {
 				s0 = fmt.substring(0, pos0);
 				s1 = fmt.substring(pos0 + 2, pos1);
 				s2 = fmt.substring(pos1 + 1);
 			} else {
 				s0 = fmt;
 				s1 = s2 = "";
 			}
 
 			section.append(s0);
 			section.append(decode(s1, iter));
 			fmt = s2;
 		}
 
 		char lastChar = section.charAt(section.length() - 1);
 
 		if (lastChar != ';' && lastChar != '{' && lastChar != '}')
			section.append(";\n");
 	}
 
 	private String decode(String s, Iterator<Object> iter) {
 		int reg;
 		Integer frameNo = !lastEnterIps.isEmpty() ? currentFrame() : null;
 		Integer parentFrameNo = frameNo != null ? parentFramesByFrame.get(frameNo) : null;
 		Class<?> registerTypes[] = frameNo != null ? registerTypesByFrame.get(frameNo) : null;
 
 		switch (s) {
 		case "fr":
 			s = String.format("f%d", frameNo);
 			break;
 		case "fr-class":
 			s = String.format("Frame%d", frameNo);
 			break;
 		case "jump":
 			s = String.format("{ ip = %d; continue; }", iter.next());
 			break;
 		case "num":
 			s = String.format("%d", iter.next());
 			break;
 		case "prev-fr":
 			s = parentFrameNo != null ? String.format("f%d", parentFrameNo) : "frame";
 			break;
 		case "prev-fr-class":
 			s = parentFrameNo != null ? String.format("Frame%d", parentFrameNo) : "Frame";
 			break;
 		case "reg":
 			s = reg((int) iter.next());
 			break;
 		case "reg-bool":
 			reg = (int) iter.next();
 			s = reg(reg);
 			if (Node.class.isAssignableFrom(registerTypes[reg]))
 				s = "(" + s + " == TRUE)";
 			break;
 		case "reg-clos":
 			reg = (int) iter.next();
 			s = reg(reg);
 			if (Node.class.isAssignableFrom(registerTypes[reg]))
 				s = "((Closure) " + s + ")";
 			break;
 		case "reg-node":
 			reg = (int) iter.next();
 			s = reg(reg);
 			Class<?> sourceClazz = registerTypes[reg];
 			if (sourceClazz == boolean.class)
 				s = "(" + s + " ? TRUE : FALSE)";
 			else if (sourceClazz == int.class)
 				s = "Int.create(" + s + ")";
 			break;
 		case "reg-num":
 			reg = (int) iter.next();
 			s = reg(reg);
 			if (Node.class.isAssignableFrom(registerTypes[reg]))
 				s = "((Int) " + s + ").getNumber()";
 			break;
 		case "str":
 			s = String.format("%s", iter.next());
 		}
 
 		return s;
 	}
 
 	private String reg(int reg) {
 		return String.format("f%d.r%d", currentFrame(), reg);
 	}
 
 	private TranslatedRun getTranslatedRun() throws IOException {
 		LogUtil.info("Translating run " + filename);
 
 		String binDir = basePathName;
 		new File(binDir).mkdirs();
 
 		JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
 
 		try (StandardJavaFileManager sjfm = jc.getStandardFileManager(null, null, null)) {
 			File file = new File(filename);
 
 			if (!jc.getTask(null //
 					, null //
 					, null //
 					, Arrays.asList("-d", binDir) //
 					, null //
 					, sjfm.getJavaFileObjects(file)).call())
 				throw new RuntimeException("Java compilation error");
 		}
 
 		LogUtil.info("Loading class " + className);
 
 		URLClassLoader ucl = new URLClassLoader(new URL[] { new URL("file://" + binDir + "/") });
 		TranslatedRun translatedRun;
 
 		try {
 			@SuppressWarnings("unchecked")
 			Class<? extends TranslatedRun> clazz = (Class<? extends TranslatedRun>) ucl.loadClass(packageName + "." + className);
 			translatedRun = clazz.getConstructor(new Class<?>[] { Closeable.class }).newInstance(ucl);
 		} catch (ReflectiveOperationException ex) {
 			ucl.close();
 			throw new RuntimeException(ex);
 		}
 
 		return translatedRun;
 	}
 
 }

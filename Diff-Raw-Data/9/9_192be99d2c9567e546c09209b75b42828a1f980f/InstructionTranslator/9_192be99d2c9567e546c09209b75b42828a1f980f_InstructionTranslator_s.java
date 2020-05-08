 package suite.instructionexecutor;
 
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.tools.JavaCompiler;
 import javax.tools.StandardJavaFileManager;
 import javax.tools.ToolProvider;
 
 import suite.instructionexecutor.InstructionAnalyzer.AnalyzedFrame;
 import suite.instructionexecutor.InstructionAnalyzer.AnalyzedRegister;
 import suite.instructionexecutor.InstructionUtil.FunComparer;
 import suite.instructionexecutor.InstructionUtil.Insn;
 import suite.instructionexecutor.InstructionUtil.Instruction;
 import suite.instructionexecutor.TranslatedRunUtil.TranslatedRun;
 import suite.node.Atom;
 import suite.node.Node;
 import suite.node.io.Formatter;
 import suite.node.io.TermParser.TermOp;
 import suite.parser.Subst;
 import suite.util.FileUtil;
 import suite.util.FunUtil.Fun;
 import suite.util.LogUtil;
 
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
 
 	private Subst subst = new Subst("#{", "}");
 
 	private InstructionAnalyzer analyzer = new InstructionAnalyzer();
 
 	private int currentIp;
 
 	private String compare = "comparer.compare(#{reg-node}, #{reg-node})";
 
 	public InstructionTranslator(String basePathName) {
 		this.basePathName = basePathName;
 		this.packageName = getClass().getPackage().getName();
 	}
 
 	public TranslatedRun translate(Node node) throws IOException {
 		List<Instruction> instructions;
 
 		try (InstructionExtractor extractor = new InstructionExtractor(constantPool)) {
 			instructions = extractor.extractInstructions(node);
 		}
 
 		int exitPoint = instructions.size();
 		instructions.add(new Instruction(Insn.LABEL_________, 0, 0, 0));
 		instructions.add(new Instruction(Insn.EXIT__________, 0, 0, 0));
 
 		analyzer.analyze(instructions);
 		translateInstructions(instructions);
 
 		className = "TranslatedRun" + counter.getAndIncrement();
 
 		String java = String.format("" //
 				+ "package " + packageName + "; \n" //
				+ "import suite.instructionexecutor.io.*; \n" //
 				+ "import suite.lp.*; \n" //
 				+ "import suite.lp.doer.*; \n" //
 				+ "import suite.lp.kb.*; \n" //
				+ "import suite.lp.node.*; \n" //
				+ "import suite.lp.predicates.*; \n" //
 				+ "import suite.util.*; \n" //
 				+ "import suite.util.FunUtil.*; \n" //
 				+ "import " + Closeable.class.getCanonicalName() + "; \n" //
 				+ "import " + FunComparer.class.getCanonicalName() + "; \n" //
 				+ "import " + IOException.class.getCanonicalName() + "; \n" //
 				+ "import " + TermOp.class.getCanonicalName() + "; \n" //
 				+ "import " + TranslatedRun.class.getCanonicalName() + "; \n" //
 				+ "import " + TranslatedRunUtil.class.getCanonicalName() + ".*; \n" //
 				+ "\n" //
 				+ "public class " + className + " implements TranslatedRun { \n" //
 				+ "private static final int stackSize = 4096; \n" //
 				+ "\n" //
 				+ "private static final Atom FALSE = Atom.FALSE; \n" //
 				+ "private static final Atom TRUE = Atom.TRUE; \n" //
 				+ "\n" //
 				+ "private Closeable closeable; \n" //
 				+ "\n" //
 				+ "public " + className + "(Closeable closeable) { this.closeable = closeable; } \n" //
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
 				+ "int csp = 0, dsp = 0, cpsp = 0; \n" //
 				+ "int n; \n" //
 				+ "Node node, n0, n1, var; \n" //
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
 		, clazzsec, localsec, switchsec);
 
 		String pathName = basePathName + "/" + packageName.replace('.', '/');
 		filename = pathName + "/" + className + ".java";
 		new File(pathName).mkdirs();
 
 		try (OutputStream os = new FileOutputStream(filename)) {
 			os.write(java.getBytes(FileUtil.charset));
 		}
 
 		// Compile the Java, load the class, return an instantiated object
 		return getTranslatedRun();
 	}
 
 	private void translateInstructions(List<Instruction> instructions) {
 		Node constant;
 		int ip = 0;
 		boolean isGenerateLabel = true;
 
 		while (ip < instructions.size()) {
 			Instruction insn = instructions.get(currentIp = ip++);
 			int op0 = insn.op0, op1 = insn.op1, op2 = insn.op2;
 
 			app("// (#{num}) #{str}", currentIp, insn);
 
 			if (isGenerateLabel || insn.insn == Insn.LABEL_________) {
 				app("case #{num}:", currentIp);
 				isGenerateLabel = false;
 			}
 
 			switch (insn.insn) {
 			case ASSIGNCLOSURE_:
 				app("#{reg} = new Closure(#{fr}, #{num})", op0, op1);
 				break;
 			case ASSIGNFRAMEREG:
 				if (op1 != 0) {
 					String prevs = "";
 					for (int i = op1; i < 0; i++)
 						prevs += ".previous";
 					app("#{reg} = TranslatedRunUtil.toNode(#{fr}#{str}.r#{num})", op0, prevs, op2);
 				} else
 					app("#{reg} = TranslatedRunUtil.toNode(#{reg})", op0, op2);
 				break;
 			case ASSIGNCONST___:
 				constant = constantPool.get(op1);
 				app("#{reg} = #{str}", op0, defineConstant(constant));
 				break;
 			case ASSIGNINT_____:
 				app("#{reg} = #{num}", op0, op1);
 				break;
 			case BACKUPCSP_____:
 				app("#{reg} = csp", op0);
 				break;
 			case BACKUPDSP_____:
 				app("#{reg} = dsp", op0);
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
 				backupFrame();
 				pushCallee(ip);
 				app("#{jump}", op0);
 				isGenerateLabel = true;
 				break;
 			case CALLCLOSURE___:
 				app("if (#{reg-clos}.result == null) {", op0);
 				backupFrame();
 				pushCallee(ip);
 				app("frame = #{reg-clos}.frame", op0);
 				app("ip = #{reg-clos}.ip", op0);
 				app("continue");
 				app("} else returnValue = #{reg-clos}.result", op0);
 				isGenerateLabel = true;
 				break;
 			case CALLREG_______:
 				backupFrame();
 				pushCallee(ip);
 				app("ip = #{reg-num}", op0);
 				app("continue");
 				isGenerateLabel = true;
 				break;
 			case COMPARE_______:
 				app("n0 = (Node) ds[--dsp]");
 				app("n1 = (Node) ds[--dsp]");
 				app("#{reg} = comparer.compare(n0, n1)", op0);
 				break;
 			case CONSLIST______:
 				app("n0 = (Node) ds[--dsp]");
 				app("n1 = (Node) ds[--dsp]");
 				app("#{reg} = Tree.create(TermOp.OR____, n0, n1)", op0);
 				break;
 			case CONSPAIR______:
 				app("n0 = (Node) ds[--dsp]");
 				app("n1 = (Node) ds[--dsp]");
 				app("#{reg} = Tree.create(TermOp.AND___, n0, n1)", op0);
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
 				boolean isRequireParent = analyzer.getFrame(currentIp).isRequireParent();
 				String previousFrame = isRequireParent ? "(#{prev-fr-class}) frame" : "null";
 				app("#{fr} = new #{fr-class}(" + previousFrame + ")");
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
 				if (currentFrame() != null)
 					app("return #{reg}", op0);
 				else
 					app("return returnValue"); // Grand exit point
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
 			case INVOKEJAVACLS_:
 				app("{");
 				app("Atom atom = (Atom) unwrapper.apply((Node) ds[--dsp])");
 				app("node = unwrapper.apply((Node) ds[--dsp])");
 				app("String clazzName = atom.toString().split(\"!\")[1]");
 				app("result = InstructionUtil.execInvokeJava(this, clazzName, node)");
 				app("}");
 				break;
 			case ISCONS________:
 				app("#{reg} = Tree.decompose((Node) ds[--dsp]) != null", op0);
 				break;
 			case JUMP__________:
 				app("#{jump}", op0);
 				isGenerateLabel = true;
 				break;
 			case JUMPREG_______:
 				app("{ ip = #{reg-num}; continue; }", op0);
 				isGenerateLabel = true;
 				break;
 			case LABEL_________:
 				break;
 			case LEAVE_________:
 				generateFrame();
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
 			case NEWNODE_______:
 				app("#{reg} = new Reference()", op0);
 				break;
 			case POP___________:
 				app("#{reg} = ds[--dsp]", op0);
 				break;
 			case POPANY________:
 				app("--dsp");
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
 			case RESTORECSP____:
 				app("csp = #{reg-num}", op0);
 				break;
 			case RESTOREDSP____:
 				app("dsp = #{reg-num}", op0);
 				break;
 			case RETURN________:
 				popCaller();
 				isGenerateLabel = true;
 				break;
 			case RETURNVALUE___:
 				app("returnValue = #{reg-node}", op0);
 				popCaller();
 				isGenerateLabel = true;
 				break;
 			case SETRESULT_____:
 				restoreFrame();
 				app("#{reg} = returnValue", op0);
 				break;
 			case SETCLOSURERES_:
 				restoreFrame();
 				app("#{reg} = returnValue", op0);
 				app("#{reg-clos}.result = #{reg}", op1, op0);
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
 
 	private void generateFrame() {
 		AnalyzedFrame frame = currentFrame();
 		List<AnalyzedRegister> registers = frame != null ? frame.getRegisters() : null;
 
 		app(localsec, "#{fr-class} #{fr} = null");
 
 		app(clazzsec, "private static class #{fr-class} implements Frame {");
 		app(clazzsec, "private #{prev-fr-class} previous");
 		app(clazzsec, "private #{fr-class}(#{prev-fr-class} previous) { this.previous = previous; }");
 
 		for (int r = 0; r < registers.size(); r++) {
 			AnalyzedRegister register = registers.get(r);
 			Class<?> clazz = register.getClazz();
 			String typeName = clazz.getSimpleName();
 
 			if (register.isUsedExternally())
 				app(clazzsec, "private #{str} r#{num}", typeName, r);
 			else {
 				String init = clazz == boolean.class ? "false" : clazz == int.class ? "0" : "null";
 				app(localsec, "#{str} f#{num}_r#{num} = #{str}", typeName, frame.getId(), r, init);
 			}
 		}
 
 		app(clazzsec, "}");
 	}
 
 	private void pushCallee(int ip) {
 		app("cs[csp] = " + ip);
 		app("csp++");
 	}
 
 	private void popCaller() {
 		app("--csp");
 		app("ip = cs[csp]");
 		app("continue");
 	}
 
 	private void backupFrame() {
 		app("fs[csp] = #{fr}");
 	}
 
 	private void restoreFrame() {
 		app("#{fr} = (#{fr-class}) fs[csp]");
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
 		final Iterator<Object> iter = list.iterator();
 
 		subst.subst(fmt, new Fun<String, String>() {
 			public String apply(String key) {
 				return decode(key, iter);
 			}
 		}, section);
 
 		char lastChar = section.charAt(section.length() - 1);
 
 		if (lastChar != ';' && lastChar != '{' && lastChar != '}')
 			section.append(";");
 
 		section.append("\n");
 	}
 
 	private String decode(String s, Iterator<Object> iter) {
 		int reg;
 		AnalyzedFrame frame = currentFrame();
 		AnalyzedFrame parentFrame = frame != null ? frame.getParent() : null;
 		List<AnalyzedRegister> registers = frame != null ? frame.getRegisters() : null;
 
 		switch (s) {
 		case "fr":
 			s = String.format("f%d", frame.getId());
 			break;
 		case "fr-class":
 			s = String.format("Frame%d", frame.getId());
 			break;
 		case "jump":
 			s = String.format("{ ip = %d; continue; }", iter.next());
 			break;
 		case "num":
 			s = String.format("%d", iter.next());
 			break;
 		case "prev-fr":
 			s = parentFrame != null ? String.format("f%d", parentFrame.getId()) : "frame";
 			break;
 		case "prev-fr-class":
 			s = parentFrame != null ? String.format("Frame%d", parentFrame.getId()) : "Frame";
 			break;
 		case "reg":
 			s = reg((int) iter.next());
 			break;
 		case "reg-bool":
 			reg = (int) iter.next();
 			s = reg(reg);
 			if (Node.class.isAssignableFrom(registers.get(reg).getClazz()))
 				s = "(" + s + " == TRUE)";
 			break;
 		case "reg-clos":
 			reg = (int) iter.next();
 			s = reg(reg);
 			if (Node.class.isAssignableFrom(registers.get(reg).getClazz()))
 				s = "((Closure) " + s + ")";
 			break;
 		case "reg-node":
 			reg = (int) iter.next();
 			s = reg(reg);
 			Class<?> sourceClazz = registers.get(reg).getClazz();
 			if (sourceClazz == boolean.class)
 				s = "(" + s + " ? TRUE : FALSE)";
 			else if (sourceClazz == int.class)
 				s = "Int.create(" + s + ")";
 			break;
 		case "reg-num":
 			reg = (int) iter.next();
 			s = reg(reg);
 			if (Node.class.isAssignableFrom(registers.get(reg).getClazz()))
 				s = "((Int) " + s + ").getNumber()";
 			break;
 		case "str":
 			s = String.format("%s", iter.next());
 		}
 
 		return s;
 	}
 
 	private String reg(int reg) {
 		AnalyzedFrame frame = currentFrame();
 		int frameNo = frame.getId();
 
 		if (frame.getRegisters().get(reg).isUsedExternally())
 			return String.format("f%d.r%d", frameNo, reg);
 		else
 			return String.format("f%d_r%d", frameNo, reg);
 	}
 
 	private AnalyzedFrame currentFrame() {
 		return analyzer.getFrame(currentIp);
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

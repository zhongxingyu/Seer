 /*
  * Copyright (c) 2008-2009, Intel Corporation.
  * Copyright (c) 2006-2007, The Trustees of Stanford University.
  * All rights reserved.
  */
 package chord.instr;
 
 import gnu.trove.TIntObjectHashMap;
 import javassist.*;
 import javassist.expr.*;
 import java.io.FilenameFilter;
 
 import chord.project.Messages;
 import chord.doms.DomE;
 import chord.doms.DomF;
 import chord.doms.DomH;
 import chord.doms.DomI;
 import chord.doms.DomM;
 import chord.doms.DomL;
 import chord.doms.DomR;
 import chord.doms.DomP;
 import chord.doms.DomB;
 import chord.instr.InstrScheme.EventFormat;
 import chord.program.Program;
 import chord.util.ChordRuntimeException;
 import chord.project.Project;
 import chord.project.Properties;
 import chord.project.analyses.ProgramDom;
 import chord.runtime.Runtime;
 import chord.util.IndexMap;
 import chord.util.IndexSet;
 import chord.util.FileUtils;
 
 import joeq.Class.jq_Class;
 import joeq.Class.jq_Array;
 import joeq.Class.jq_Reference;
 import joeq.Class.jq_Method;
 import joeq.Compiler.Quad.BasicBlock;
 import joeq.Compiler.Quad.ControlFlowGraph;
 import joeq.Compiler.Quad.Operator;
 import joeq.Compiler.Quad.Quad;
 import joeq.Util.Templates.ListIterator;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.HashSet;
 
 /**
  * Functionality for offline instrumentation and rewriting of a
  * program's bytecode to generate the specified events during
  * its execution.
  * 
  * @author Mayur Naik (mhn@cs.stanford.edu)
  */
 public class Instrumentor {
 	protected static final String runtimeClassName = Properties.runtimeClassName + ".";
 
 	protected static final String enterMethodEventCall = runtimeClassName + "enterMethodEvent(";
 	protected static final String leaveMethodEventCall = runtimeClassName + "leaveMethodEvent(";
 
 	protected static final String befNewEventCall = runtimeClassName + "befNewEvent(";
 	protected static final String aftNewEventCall = runtimeClassName + "aftNewEvent(";
 	protected static final String newEventCall = runtimeClassName + "newEvent(";
 	protected static final String newArrayEventCall = runtimeClassName + "newArrayEvent(";
 
 	protected static final String getstaticPriEventCall = runtimeClassName + "getstaticPrimitiveEvent(";
 	protected static final String putstaticPriEventCall = runtimeClassName + "putstaticPrimitiveEvent(";
 	protected static final String getstaticRefEcentCall = runtimeClassName + "getstaticReferenceEvent(";
 	protected static final String putstaticRefEventCall = runtimeClassName + "putstaticReferenceEvent(";
 
 	protected static final String getfieldPriEventCall = runtimeClassName + "getfieldPrimitiveEvent(";
 	protected static final String putfieldPriEventCall = runtimeClassName + "putfieldPrimitiveEvent(";
 	protected static final String getfieldReference = runtimeClassName + "getfieldReferenceEvent(";
 	protected static final String putfieldRefEventCall = runtimeClassName + "putfieldReferenceEvent(";
 
 	protected static final String aloadPriEventCall = runtimeClassName + "aloadPrimitiveEvent(";
 	protected static final String aloadRefEventCall = runtimeClassName + "aloadReferenceEvent(";
 	protected static final String astorePriEventCall = runtimeClassName + "astorePrimitiveEvent(";
 	protected static final String astoreRefEventCall = runtimeClassName + "astoreReferenceEvent(";
 
 	protected static final String methodCallBefEventCall = runtimeClassName + "methodCallBefEvent(";
 	protected static final String methodCallAftEventCall = runtimeClassName + "methodCallAftEvent(";
 	protected static final String returnPriEventCall = runtimeClassName + "returnPrimtiveEvent(";
 	protected static final String returnRefEventCall = runtimeClassName + "returnReferenceEvent(";
 	protected static final String explicitThrowEventCall = runtimeClassName + "explicitThrowEvent(";
 	protected static final String implicitThrowEventCall = runtimeClassName + "implicitThrowEvent(";
 	protected static final String quadEventCall = runtimeClassName + "quadEvent(";
 	protected static final String basicBlockEventCall = runtimeClassName + "basicBlockEvent(";
 
 	protected static final String threadStartEventCall = runtimeClassName + "threadStartEvent(";
 	protected static final String threadJoinEventCall = runtimeClassName + "threadJoinEvent(";
 	protected static final String waitEventCall = runtimeClassName + "waitEvent(";
 	protected static final String notifyEventCall = runtimeClassName + "notifyEvent(";
 	protected static final String notifyAllEventCall = runtimeClassName + "notifyAllEvent(";
 	protected static final String acquireLockEventCall = runtimeClassName + "acquireLockEvent(";
 	protected static final String releaseLockEventCall = runtimeClassName + "releaseLockEvent(";
 
 	protected static final String finalizeEventCall = runtimeClassName + "finalizeEvent(";
 
 	protected InstrScheme scheme;
 	protected Program program;
 	private String mainClassPathName;
 	private String userClassPathName;
 	private String[] scopeExcludeAry;
 	private String bootClassesDirName;
 	private String userClassesDirName;
 	private boolean verbose;
 
 	private ClassPool pool;
 	private Set<String> bootClassPathResourceNames;
 	private Set<String> userClassPathResourceNames;
 
 	protected boolean genBasicBlockEvent;
 	protected boolean genQuadEvent;
 	protected boolean genFinalizeEvent;
 	protected EventFormat enterMethodEvent;
 	protected EventFormat leaveMethodEvent;
 	protected EventFormat newAndNewArrayEvent;
 	protected EventFormat getstaticPrimitiveEvent;
 	protected EventFormat getstaticReferenceEvent;
 	protected EventFormat putstaticPrimitiveEvent;
 	protected EventFormat putstaticReferenceEvent;
 	protected EventFormat getfieldPrimitiveEvent;
 	protected EventFormat getfieldReferenceEvent;
 	protected EventFormat putfieldPrimitiveEvent;
 	protected EventFormat putfieldReferenceEvent;
 	protected EventFormat aloadPrimitiveEvent;
 	protected EventFormat aloadReferenceEvent;
 	protected EventFormat astorePrimitiveEvent;
 	protected EventFormat astoreReferenceEvent;
 	protected EventFormat threadStartEvent;
 	protected EventFormat threadJoinEvent;
 	protected EventFormat acquireLockEvent;
 	protected EventFormat releaseLockEvent;
 	protected EventFormat waitEvent;
 	protected EventFormat notifyEvent;
 	protected EventFormat methodCallEvent;
 	protected EventFormat returnPrimitiveEvent;
 	protected EventFormat returnReferenceEvent;
 	protected EventFormat explicitThrowEvent;
 	protected EventFormat implicitThrowEvent;
 
 	protected DomF domF;
 	protected DomM domM;
 	protected DomH domH;
 	protected DomE domE;
 	protected DomI domI;
 	protected DomL domL;
 	protected DomR domR;
 	protected DomP domP;
 	protected DomB domB;
 
 	protected IndexMap<String> Fmap;
 	protected IndexMap<String> Mmap;
 	protected IndexMap<String> Hmap;
 	protected IndexMap<String> Emap;
 	protected IndexMap<String> Imap;
 	protected IndexMap<String> Lmap;
 	protected IndexMap<String> Rmap;
 	protected IndexMap<String> Pmap;
 	protected IndexMap<String> Bmap;
 
 	private CtClass exType;
 	private MyExprEditor exprEditor = new MyExprEditor();
 
 	protected String mStr;
 	protected TIntObjectHashMap<String> bciToInstrMap =
 		new TIntObjectHashMap<String>();
 
 	public InstrScheme getInstrScheme() { return scheme; }
 
 	public DomF getDomF() { return domF; }
 	public DomM getDomM() { return domM; }
 	public DomH getDomH() { return domH; }
 	public DomE getDomE() { return domE; }
 	public DomI getDomI() { return domI; }
 	public DomL getDomL() { return domL; }
 	public DomR getDomR() { return domR; }
 	public DomP getDomP() { return domP; }
 	public DomB getDomB() { return domB; }
 
 	public IndexMap<String> getFmap() { return Fmap; }
 	public IndexMap<String> getMmap() { return Mmap; }
 	public IndexMap<String> getHmap() { return Hmap; }
 	public IndexMap<String> getEmap() { return Emap; }
 	public IndexMap<String> getImap() { return Imap; }
 	public IndexMap<String> getLmap() { return Lmap; }
 	public IndexMap<String> getRmap() { return Rmap; }
 	public IndexMap<String> getPmap() { return Pmap; }
 	public IndexMap<String> getBmap() { return Bmap; }
 
 	/**
 	 * Initializes the instrumentor.
 	 * 
 	 * @param	program	The program to instrument.
 	 * @param	scheme	The scheme specifying the kind and format
 	 * of events to generate during the execution of the
 	 * instrumented program. 
 	 */
 	public Instrumentor(Program program, InstrScheme scheme) {
 		this.program = program;
 		this.scheme = scheme;
 		mainClassPathName = Properties.mainClassPathName;
 		userClassPathName = Properties.classPathName;
 		scopeExcludeAry = Properties.scopeExcludeAry;
 		bootClassesDirName = Properties.bootClassesDirName;
 		userClassesDirName = Properties.userClassesDirName;
  		verbose = Properties.verbose;
 	}
 	private static boolean checkExists(String pathElem) {
 		File file = new File(pathElem);
 		if (!file.exists()) {
 			Messages.log("INSTR.NON_EXISTENT_PATH_ELEM", pathElem);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Runs the instrumentor which reads each .class file of the
 	 * given program and writes a corresponding .class file with
 	 * instrumentation for generating the specified kind and format
 	 * of events during the execution of the instrumented program.
 	 */
 	public void run() throws NotFoundException, CannotCompileException, IOException {
 		pool = new ClassPool();
 
 		String[] mainClassPathElems = mainClassPathName.split(File.pathSeparator);
 		for (String pathElem : mainClassPathElems) {
 			if (checkExists(pathElem)) {
 				pool.appendClassPath(pathElem);
 			}
 		}
 
 		bootClassPathResourceNames = new HashSet<String>();
 
 		String bootClassPathName = System.getProperty("sun.boot.class.path");
 		String[] bootClassPathElems = bootClassPathName.split(File.pathSeparator);
 		for (String pathElem : bootClassPathElems) {
 			if (checkExists(pathElem)) {
 				bootClassPathResourceNames.add(pathElem);
 				pool.appendClassPath(pathElem);
 			}
 		}
 
 		userClassPathResourceNames = new HashSet<String>();
 
         String javaHomeDir = System.getProperty("java.home");
         assert (javaHomeDir != null);
 
 		File libExtDir = new File(javaHomeDir, File.separator + "lib" + File.separator + "ext");
 		if (libExtDir.exists()) {
 			final FilenameFilter filter = new FilenameFilter() {
 				public boolean accept(File dir, String name) {
 					if (name.endsWith(".jar"))
 						return true;
 					return false;
 				}
 			};
 			File[] subFiles = libExtDir.listFiles(filter);
 			for (File file : subFiles) {
 				String fileName = file.getAbsolutePath();
 				userClassPathResourceNames.add(fileName);
 				pool.appendClassPath(fileName);
 			}
 		}
 
 		String[] userClassPathElems = userClassPathName.split(File.pathSeparator);
 		for (String pathElem : userClassPathElems) {
 			if (checkExists(pathElem)) {
 				userClassPathResourceNames.add(pathElem);
 				pool.appendClassPath(pathElem);
 			}
 		}
 
 		genBasicBlockEvent = scheme.hasBasicBlockEvent();
 		genQuadEvent = scheme.hasQuadEvent();
 		genFinalizeEvent = scheme.hasFinalizeEvent();
 		enterMethodEvent = scheme.getEvent(InstrScheme.ENTER_METHOD);
 		leaveMethodEvent = scheme.getEvent(InstrScheme.LEAVE_METHOD);
 		newAndNewArrayEvent = scheme.getEvent(InstrScheme.NEW_AND_NEWARRAY);
 		getstaticPrimitiveEvent = scheme.getEvent(InstrScheme.GETSTATIC_PRIMITIVE);
 		getstaticReferenceEvent = scheme.getEvent(InstrScheme.GETSTATIC_REFERENCE);
 		putstaticPrimitiveEvent = scheme.getEvent(InstrScheme.PUTSTATIC_PRIMITIVE);
 		putstaticReferenceEvent = scheme.getEvent(InstrScheme.PUTSTATIC_REFERENCE);
 		getfieldPrimitiveEvent = scheme.getEvent(InstrScheme.GETFIELD_PRIMITIVE);
 		getfieldReferenceEvent = scheme.getEvent(InstrScheme.GETFIELD_REFERENCE);
 		putfieldPrimitiveEvent = scheme.getEvent(InstrScheme.PUTFIELD_PRIMITIVE);
 		putfieldReferenceEvent = scheme.getEvent(InstrScheme.PUTFIELD_REFERENCE);
 		aloadPrimitiveEvent = scheme.getEvent(InstrScheme.ALOAD_PRIMITIVE);
 		aloadReferenceEvent = scheme.getEvent(InstrScheme.ALOAD_REFERENCE);
 		astorePrimitiveEvent = scheme.getEvent(InstrScheme.ASTORE_PRIMITIVE);
 		astoreReferenceEvent = scheme.getEvent(InstrScheme.ASTORE_REFERENCE);
 		threadStartEvent = scheme.getEvent(InstrScheme.THREAD_START);
 		threadJoinEvent = scheme.getEvent(InstrScheme.THREAD_JOIN);
 		acquireLockEvent = scheme.getEvent(InstrScheme.ACQUIRE_LOCK);
 		releaseLockEvent = scheme.getEvent(InstrScheme.RELEASE_LOCK);
 		waitEvent = scheme.getEvent(InstrScheme.WAIT);
 		notifyEvent = scheme.getEvent(InstrScheme.NOTIFY);
 		methodCallEvent = scheme.getEvent(InstrScheme.METHOD_CALL);
 		
 		if (scheme.needsFmap()) {
 			domF = (DomF) Project.getTrgt("F");
 			Project.runTask(domF);
 			Fmap = getUniqueStringMap(domF);
 		}
 		if (scheme.needsMmap()) {
 			domM = (DomM) Project.getTrgt("M");
 			Project.runTask(domM);
 			Mmap = getUniqueStringMap(domM);
 		}
 		if (scheme.needsHmap()) {
 			domH = (DomH) Project.getTrgt("H");
 			Project.runTask(domH);
 			Hmap = getUniqueStringMap(domH);
 		}
 		if (scheme.needsEmap()) {
 			domE = (DomE) Project.getTrgt("E");
 			Project.runTask(domE);
 			Emap = getUniqueStringMap(domE);
 		}
 		if (scheme.needsImap()) {
 			domI = (DomI) Project.getTrgt("I");
 			Project.runTask(domI);
 			Imap = getUniqueStringMap(domI);
 		}
 		if (scheme.needsLmap()) {
 			domL = (DomL) Project.getTrgt("L");
 			Project.runTask(domL);
 			Lmap = getUniqueStringMap(domL);
 		}
 		if (scheme.needsRmap()) {
 			domR = (DomR) Project.getTrgt("R");
 			Project.runTask(domR);
 			Rmap = getUniqueStringMap(domR);
 		}
 		if (scheme.needsPmap()) {
 			domP = (DomP) Project.getTrgt("P");
 			Project.runTask(domP);
 		}
 		if (scheme.needsBmap()) {
 			domB = (DomB) Project.getTrgt("B");
 			Project.runTask(domB);
 		}
 		if (leaveMethodEvent.present() || releaseLockEvent.present()) {
 			exType = pool.get("java.lang.Throwable");
 			assert (exType != null);
 		}
 		
 		FileUtils.deleteFile(bootClassesDirName);
 		FileUtils.deleteFile(userClassesDirName);
 
 		IndexSet<jq_Reference> classes = program.getClasses();
 		Messages.log("INSTR.STARTING");
 		for (jq_Reference r : classes) {
 			if (r instanceof jq_Array)
 				continue;
 			jq_Class c = (jq_Class) r;
 			instrClass(c);
 		}
 		Messages.log("INSTR.FINISHED");
 	}
 	private void instrClass(jq_Class c) {
 		String cName = c.getName();
 		boolean match = false;
 		for (String s : scopeExcludeAry) {
 			if (cName.startsWith(s)) {
 				match = true;
 	 			break;
 			}
 		}
 		if (match) {
 			if (verbose) Messages.log("INSTR.EXPLICITLY_EXCLUDING_CLASS", cName);
 			return;
 		}
 		if (cName.equals("java.lang.J9VMInternals") || cName.startsWith("java.lang.ref.")) {
 			Messages.log("INSTR.IMPLICITLY_EXCLUDING_CLASS", cName);
 			return;
 		}
 		String outDirName = null;
 		String resourceName = pool.getResource(cName);
 		if (resourceName == null) {
 			Messages.log("INSTR.CLASS_NOT_FOUND", cName);
 			return;
 		} else if (bootClassPathResourceNames.contains(resourceName)) {
 			outDirName = bootClassesDirName;
 		} else if (userClassPathResourceNames.contains(resourceName)) {
 			outDirName = userClassesDirName;
 		} else {
 			Messages.log("INSTR.CLASS_NOT_BOOT_NOR_USER", cName, resourceName);
 			return;
 		}
 		CtClass clazz;
 		try {
 			clazz = pool.get(cName);
 		} catch (NotFoundException ex) {
 			throw new ChordRuntimeException(ex);
 		}
 		List<jq_Method> methods = program.getMethods(c);
 		CtBehavior[] inits = clazz.getDeclaredConstructors();
 		CtBehavior[] meths = clazz.getDeclaredMethods();
 		for (jq_Method m : methods) {
 			CtBehavior method = null;
 			String mName = m.getName().toString();
 			if (mName.equals("<clinit>")) {
 				method = clazz.getClassInitializer();
 			} else if (mName.equals("<init>")) {
 				String mDesc = m.getDesc().toString();
 				for (CtBehavior x : inits) {
 					if (x.getSignature().equals(mDesc)) {
 						method = x;
 						break;
 					}
 				}
 			} else {
 				String mDesc = m.getDesc().toString();
 				for (CtBehavior x : meths) {
 					if (x.getName().equals(mName) &&
 						x.getSignature().equals(mDesc)) {
 						method = x;
 						break;
 					}
 				}
 			}
 			assert (method != null);
 			try {
 				process(method, m);
 			} catch (ChordRuntimeException ex) {
 				Messages.log("INSTR.CANNOT_INSTRUMENT_METHOD", method.getLongName());
 				ex.printStackTrace();
 			}
 		}
 		try {
 			clazz.writeFile(outDirName);
 		} catch (IOException ex) {
 			throw new ChordRuntimeException(ex);
 		} catch (NotFoundException ex) {
 			throw new ChordRuntimeException(ex);
 		} catch (CannotCompileException ex) {
 			Messages.log("INSTR.CANNOT_INSTRUMENT_CLASS", cName);
 			ex.printStackTrace();
 			return;
 		}
 		if (verbose) Messages.log("INSTR.WROTE_INSTRUMENTED_CLASS", cName);
 	}
 
 	protected <T> IndexMap<String> getUniqueStringMap(ProgramDom<T> dom) {
 		IndexMap<String> map = new IndexMap<String>(dom.size());
 		for (int i = 0; i < dom.size(); i++) {
 			String s = dom.toUniqueString(dom.get(i));
 			if (map.contains(s))
 				throw new ChordRuntimeException(Messages.get("INSTR.DUPLICATE_IN_DOMAIN"));
 			map.getOrAdd(s);
 		}
 		return map;
 	}
 	protected int getBCI(BasicBlock b, jq_Method m) {
 		int n = b.size();
 		for (int i = 0; i < n; i++) {
 			Quad q = b.getQuad(i);
 			int bci = m.getBCI(q);
 			if (bci != -1)
 				return bci;
 		}
 		throw new ChordRuntimeException(Messages.get("INSTR.NO_BCI_IN_BASIC_BLOCK", b, m));
 	}
 	// order must be tail -> head -> rest
 	protected void attachInstrToBCIAft(String str, int bci) {
 		String s = bciToInstrMap.get(bci);
 		bciToInstrMap.put(bci, (s == null) ? str : s + str);
 	}
 	protected void attachInstrToBCIBef(String str, int bci) {
 		String s = bciToInstrMap.get(bci);
 		bciToInstrMap.put(bci, (s == null) ? str : str + s);
 	}
 	protected void process(CtBehavior javassistMethod, jq_Method joeqMethod) {
 		int mods = javassistMethod.getModifiers();
 		if (Modifier.isNative(mods) || Modifier.isAbstract(mods))
 			return;
 		int mId = -1;
 		String mName;
 		if (javassistMethod instanceof CtConstructor) {
 			mName = ((CtConstructor) javassistMethod).isClassInitializer() ?  "<clinit>" : "<init>";
 		} else
 			mName = javassistMethod.getName();
 		String mDesc = javassistMethod.getSignature();
 		String cName = javassistMethod.getDeclaringClass().getName();
 		mStr = Program.toString(mName, mDesc, cName);
 		if (Mmap != null) {
 			mId = Mmap.indexOf(mStr);
 			if (mId == -1) {
 				Messages.log("INSTR.METHOD_NOT_FOUND", mStr);
 				return;
 			}
 		}
 		if (genQuadEvent || genBasicBlockEvent) {
 			Map<Quad, Integer> bcMap;
 			try{
 				bcMap = joeqMethod.getBCMap();
 			} catch (RuntimeException ex) {
 				Messages.log("INSTR.CANNOT_INSTRUMENT_METHOD", mStr);
 				ex.printStackTrace();
 				return;
 			}
 			if (bcMap == null) {
 				Messages.log("INSTR.METHOD_BYTECODE_NOT_FOUND", mStr);
 				return;
 			}
 			ControlFlowGraph cfg = joeqMethod.getCFG();
 			bciToInstrMap.clear();
 			if (genQuadEvent || genBasicBlockEvent) {
 				for (ListIterator.BasicBlock it = cfg.reversePostOrderIterator(); it.hasNext();) {
 					BasicBlock bb = it.nextBasicBlock();
 					if (bb.isEntry() || bb.isExit())
 						continue;
 					if (genBasicBlockEvent) {
 						int bId = domB.indexOf(bb);
 						assert (bId != -1);
 						String instr = basicBlockEventCall + bId + ");";
 						int bci = getBCI(bb, joeqMethod);
 						attachInstrToBCIAft(instr, bci);
 					}
 					if (genQuadEvent) {
 						int n = bb.size();
 						for (int i = 0; i < n; i++) {
 							Quad q = bb.getQuad(i);
 							if (isRelevant(q)) {
 								int bci = joeqMethod.getBCI(q);
 								assert (bci != -1);
 								int pId = domP.indexOf(q);
 								assert (pId != -1);
 								String instr = quadEventCall + pId + ");";
 								attachInstrToBCIAft(instr, bci);
 							}
 						}
 					}
 				}
 			}
 		}
 		try {
 			javassistMethod.instrument(exprEditor);
 		} catch (CannotCompileException ex) {
 			throw new ChordRuntimeException(ex);
 		}
 		// NOTE: do not move insertBefore or insertAfter or addCatch
 		// calls to a method to before bytecode instrumentation, else
 		// bytecode instrumentation offsets could get messed up 
 		String enterStr = "";
 		String leaveStr = "";
 		// is this finalize() method of java.lang.Object?
 		if (genFinalizeEvent && mName.equals("finalize") &&
 				mDesc.equals("()V") && cName.equals("java.lang.Object")) {
 			leaveStr += finalizeEventCall + "$0);";
 		}
 		if (Modifier.isSynchronized(mods) &&
 				(acquireLockEvent.present() || releaseLockEvent.present())) {
 			String syncExpr;
 			if (Modifier.isStatic(mods))
 				syncExpr = cName + ".class";
 			else
 				syncExpr = "$0";
 			if (acquireLockEvent.present()) {
 				int lId = acquireLockEvent.hasLoc() ? set(Lmap, -1) : Runtime.MISSING_FIELD_VAL;
 				enterStr += acquireLockEventCall + lId + "," + syncExpr + ");";
 			}
 			if (releaseLockEvent.present()) {
 				int rId = releaseLockEvent.hasLoc() ? set(Rmap, -2) : Runtime.MISSING_FIELD_VAL;
 				leaveStr += releaseLockEventCall + rId + "," + syncExpr + ");";
 			}
 		}
 		if (enterMethodEvent.present()) {
 			int nId = enterMethodEvent.hasLoc() ? mId : Runtime.MISSING_FIELD_VAL;
 			enterStr = enterMethodEventCall + nId + ");" + enterStr;
 		}
 		if (leaveMethodEvent.present()) {
 			int nId = leaveMethodEvent.hasLoc() ? mId : Runtime.MISSING_FIELD_VAL;
 			leaveStr = leaveStr + leaveMethodEventCall + nId + ");";
 		}
 		if (!enterStr.equals("")) {
 			try {
 				javassistMethod.insertBefore("{" + enterStr + "}");
 			} catch (CannotCompileException ex) {
 				throw new ChordRuntimeException(ex);
 			}
 		}
 		if (!leaveStr.equals("")) {
 			try {
 				javassistMethod.insertAfter("{" + leaveStr + "}");
 				String eventCall = "{" + leaveStr + "throw($e);" + "}";
 				javassistMethod.addCatch(eventCall, exType);
 			} catch (CannotCompileException ex) {
 				throw new ChordRuntimeException(ex);
 			}
 		}
 	}
 
 	public static boolean isRelevant(Quad q) {
 		Operator op = q.getOperator();
 		return
 			op instanceof Operator.Getfield ||
 			op instanceof Operator.Invoke ||
 			op instanceof Operator.Putfield ||
 			op instanceof Operator.New ||
 			op instanceof Operator.ALoad ||
 			op instanceof Operator.AStore ||
 			op instanceof Operator.Return ||
 			op instanceof Operator.Getstatic ||
 			op instanceof Operator.Putstatic ||
 			op instanceof Operator.NewArray ||
 			op instanceof Operator.Monitor;
 	}
 
 	protected int set(IndexMap<String> map, Expr e) {
 		return set(map, e.indexOfOriginalBytecode());
 	}
 
 	protected String getDomainName(IndexMap<String> map) {
 		if (map == Fmap) return "F";
 		if (map == Emap) return "E";
 		if (map == Mmap) return "M";
 		if (map == Hmap) return "H";
 		if (map == Imap) return "I";
 		if (map == Lmap) return "L";
 		if (map == Rmap) return "R";
 		if (map == Pmap) return "P";
 		if (map == Bmap) return "B";
 		throw new ChordRuntimeException();
 	}
 	protected int set(IndexMap<String> map, int bci) {
 		String s = bci + "!" + mStr;
 		int id = map.indexOf(s);
 		if (id == -1) {
 			Messages.log("INSTR.NOT_IN_DOMAIN", getDomainName(map), s);
 			id = Runtime.UNKNOWN_FIELD_VAL;
 		}
 		return id;
 	}
 	protected int getFid(CtField field) {
 		String fName = field.getName();
 		String fDesc = field.getSignature();
 		String cName = field.getDeclaringClass().getName();
 		String s = Program.toString(fName, fDesc, cName);
 		int id = Fmap.indexOf(s);
 		if (id == -1) {
 			Messages.log("INSTR.NOT_IN_DOMAIN", "F", s);
 			id = Runtime.UNKNOWN_FIELD_VAL;
 		}
 		return id;
 	}
 
 	class MyExprEditor extends ExprEditor {
 		public String insertBefore(int pos) {
 			String s = bciToInstrMap.get(pos);
 			// s may be null in which case this method won't
 			// add any instrumentation
 			if (s != null)
 				s = "{ " + s + " }";
 			return s;
 		}
 		public void edit(NewExpr e) {
 			if (newAndNewArrayEvent.present()) {
 				String instr1, instr2;
 				if (newAndNewArrayEvent.hasObj()) {
 					// instrument hId regardless of whether client wants it
 					int hId = set(Hmap, e);
 					instr1 = befNewEventCall + hId + ");";
 					instr2 = aftNewEventCall + hId + ",$_);";
 				} else {
  					int hId = newAndNewArrayEvent.hasLoc() ?
 						set(Hmap, e) : Runtime.MISSING_FIELD_VAL;
 					instr1 = newEventCall + hId + ");";
 					instr2 = "";
 				}
 				try {
 					e.replace("{ " + instr1 + " $_ = $proceed($$); " +
 						instr2 + " }");
 				} catch (CannotCompileException ex) {
 					throw new ChordRuntimeException(ex);
 				}
 			}
 		}
 		public void edit(NewArray e) {
 			if (newAndNewArrayEvent.present()) {
 				int hId = newAndNewArrayEvent.hasLoc() ?
 					set(Hmap, e) : Runtime.MISSING_FIELD_VAL;
 				String o = newAndNewArrayEvent.hasObj() ? "$_" : "null";
 				String instr = newArrayEventCall + hId + "," + o + ");";
 				try {
 					e.replace("{ $_ = $proceed($$); " + instr + " }");
 				} catch (CannotCompileException ex) {
 					throw new ChordRuntimeException(ex);
 				}
 			}
 		}
 		public void edit(FieldAccess e) {
 			boolean isStatic = e.isStatic();
 			CtField field;
 			CtClass type;
 			try {
 				field = e.getField();
 				type = field.getType();
 			} catch (NotFoundException ex) {
 				throw new ChordRuntimeException(ex);
 			}
 			boolean isPrim = type.isPrimitive();
 			boolean isWr = e.isWriter();
 			String instr;
 			if (isStatic) {
 				if (!scheme.hasStaticEvent())
 					return;
 				if (isWr) {
 					instr = isPrim ? putstaticPrimitive(e, field) : putstaticReference(e, field);
 				} else {
 					instr = isPrim ? getstaticPrimitive(e, field) : getstaticReference(e, field);
 				}
 			} else {
 				if (!scheme.hasFieldEvent())
 					return;
 				if (isWr) {
 					instr = isPrim ? putfieldPrimitive(e, field) : putfieldReference(e, field);
 				} else {
 					instr = isPrim ? getfieldPrimitive(e, field) : getfieldReference(e, field);
 				}
 			}
 			if (instr != null) {
 				try {
 					e.replace(instr);
 				} catch (CannotCompileException ex) {
 					throw new ChordRuntimeException(ex);
 				}
 			}
 		}
 		public void edit(ArrayAccess e) {
 			if (scheme.hasArrayEvent()) {
 				boolean isWr = e.isWriter();
 				boolean isPrim = e.getElemType().isPrimitive();
 				String instr;
 				if (isWr) {
 					instr = isPrim ? astorePrimitive(e) : astoreReference(e);
 				} else {
 					instr = isPrim ? aloadPrimitive(e) : aloadReference(e);
 				}
 				if (instr != null) {
 					try {
 						e.replace(instr);
 					} catch (CannotCompileException ex) {
 						throw new ChordRuntimeException(ex);
 					}
 				}
 			}
 		}
 		public void edit(MonitorEnter e) {
 			if (acquireLockEvent.present()) {
 				int lId = acquireLockEvent.hasLoc() ? set(Lmap, e) : Runtime.MISSING_FIELD_VAL;
 				String o = acquireLockEvent.hasObj() ? "$0" : "null";
 				String instr = acquireLockEventCall + lId + "," + o + ");";
 				try {
 					e.replace("{ $proceed(); " + instr + " }");
 				} catch (CannotCompileException ex) {
 					throw new ChordRuntimeException(ex);
 				}
 			}
 		}
 		public void edit(MonitorExit e) {
 			if (releaseLockEvent.present()) {
 				int rId = releaseLockEvent.hasLoc() ? set(Rmap, e) : Runtime.MISSING_FIELD_VAL;
 				String o = releaseLockEvent.hasObj() ? "$0" : "null";
 				String instr = releaseLockEventCall + rId + "," + o + ");";
 				try {
 					e.replace("{ " + instr + " $proceed(); }");
 				} catch (CannotCompileException ex) {
 					throw new ChordRuntimeException(ex);
 				}
 			}
 		}
 		public void edit(MethodCall e) {
 			String befInstr = "";
 			String aftInstr = "";
 			// Part 1: add METHOD_CALL event if present
 			if (methodCallEvent.present()) {
 				int iId = methodCallEvent.hasLoc() ? set(Imap, e) : Runtime.MISSING_FIELD_VAL;
 				String o = methodCallEvent.hasObj() ? "$0" : "null";
 				if (methodCallEvent.isBef())
 					befInstr += methodCallBefEventCall + iId + "," + o + ");";
 				if (methodCallEvent.isAft())
 					aftInstr += methodCallAftEventCall + iId + "," + o + ");";
 			}
 			CtMethod m;
 			try {
 				m = e.getMethod();
 			} catch (NotFoundException ex) {
 				throw new ChordRuntimeException(ex);
 			}
 			// Part 2: add BEF_NEW and AFT_NEW, or just NEW, event
 			// if present and applicable
 			if (newAndNewArrayEvent.present()) {
 				String mDesc = m.getSignature();
 				if (mDesc.equals("()Ljava/lang/Object;")) {
 					String mName = m.getName();
 					String cName = m.getDeclaringClass().getName();
 					if ((mName.equals("newInstance") && cName.equals("java.lang.Class")) ||
 						(mName.equals("clone") && cName.equals("java.lang.Object"))) {
 						int hId = newAndNewArrayEvent.hasLoc() ? set(Hmap, e) : Runtime.MISSING_FIELD_VAL;
 						if (newAndNewArrayEvent.hasObj()) {
 							befInstr += befNewEventCall + hId + ");";
 							aftInstr += aftNewEventCall + hId + ",$_);";
 						} else {
 							befInstr += newEventCall + hId + ");";
 						}
 					}
 				}
 			}
 			// Part 3: add THREAD_START, THREAD_JOIN, WAIT, or NOTIFY event
 			// if present and applicable
 			String instr = processThreadRelatedCall(e, m);
 			if (instr != null)
 				befInstr += instr;
 			if (befInstr.equals("") && aftInstr.equals(""))
 				return;
 			// NOTE: the following must be executed only if at least
 			// befInstr or aftInstr is non-null.  Otherwise, all call sites
 			// in the program will be replaced, and this can cause null
 			// pointer exceptions in certain cases (i.e. $_ = $proceed($$)
 			// does not seem to be safe usage for all call sites).
 			try {
 				// Hack: check if the target method declares exceptions it might throw
 				// and don't put try...catch around the call site if it does.
 				// This is because IBM J9 JVM does not like try...catch blocks around
 				// call sites that call methods it wants to inline, and it does not seem
 				// to inline methods that may throw exceptions.
 				// This is a hack because the target method may throw an undeclared
 				// exception like RuntimeException, which will cause aftInstr to be
 				// bypassed.
 				if (!aftInstr.equals("") && m.getExceptionTypes().length != 0) {
 					e.replace("{ " + befInstr + " try { $_ = $proceed($$); } " +
 						"catch (java.lang.Throwable ex) { " + aftInstr + "; throw ex; }; " +
 						aftInstr + " }");
 				} else {
 					e.replace("{ " + befInstr + " $_ = $proceed($$); " +
 						aftInstr + " }");
 				}
 			} catch (CannotCompileException ex) {
 				throw new ChordRuntimeException(ex);
 			} catch (NotFoundException ex) {
 				throw new ChordRuntimeException(ex);
 			}
 		}
 	}
 	protected String getstaticPrimitive(FieldAccess e, CtField f) {
 		if (getstaticPrimitiveEvent.present()) {
 			int eId = getstaticPrimitiveEvent.hasLoc() ? set(Emap, e) : Runtime.MISSING_FIELD_VAL;
 			String b;
 			if (getstaticPrimitiveEvent.hasBaseObj()) {
 				String cName = f.getDeclaringClass().getName();
 				b = cName + ".class";
 			} else
 				b = "null";
 			int fId = getstaticPrimitiveEvent.hasFld() ? getFid(f) : Runtime.MISSING_FIELD_VAL;
 			return "{ $_ = $proceed($$); " + getstaticPriEventCall + eId +
 				"," + b + "," + fId + "); }";
 		}
 		return null;
 	}
 	protected String getstaticReference(FieldAccess e, CtField f) {
 		if (getstaticReferenceEvent.present()) {
 			int eId = getstaticReferenceEvent.hasLoc() ? set(Emap, e) : Runtime.MISSING_FIELD_VAL;
 			String b;
 			if (getstaticReferenceEvent.hasBaseObj()) {
 				String cName = f.getDeclaringClass().getName();
 				b = cName + ".class";
 			} else
 				b = "null";
 			int fId = getstaticReferenceEvent.hasFld() ? getFid(f) : Runtime.MISSING_FIELD_VAL;
 			String o = getstaticReferenceEvent.hasObj() ? "$_" : "null";
 			return "{ $_ = $proceed($$); " + getstaticRefEcentCall + eId +
 				"," + b + "," + fId + "," + o + "); }";
 		}
 		return null;
 	}
 	protected String putstaticPrimitive(FieldAccess e, CtField f) {
 		if (putstaticPrimitiveEvent.present()) {
 			int eId = putstaticPrimitiveEvent.hasLoc() ? set(Emap, e) : Runtime.MISSING_FIELD_VAL;
 			String b;
 			if (putstaticPrimitiveEvent.hasBaseObj()) {
 				String cName = f.getDeclaringClass().getName();
 				b = cName + ".class";
 			} else
 				b = "null";
 			int fId = putstaticPrimitiveEvent.hasFld() ? getFid(f) : Runtime.MISSING_FIELD_VAL;
 			return "{ $proceed($$); " + putstaticPriEventCall + eId +
 				"," + b + "," + fId + "); }";
 		}
 		return null;
 	}
 	protected String putstaticReference(FieldAccess e, CtField f) {
 		if (putstaticReferenceEvent.present()) {
 			int eId = putstaticReferenceEvent.hasLoc() ? set(Emap, e) : Runtime.MISSING_FIELD_VAL;
 			String b;
 			if (putstaticReferenceEvent.hasBaseObj()) {
 				String cName = f.getDeclaringClass().getName();
 				b = cName + ".class";
 			} else
 				b = "null";
 			int fId = putstaticReferenceEvent.hasFld() ? getFid(f) : Runtime.MISSING_FIELD_VAL;
 			String o = putstaticReferenceEvent.hasObj() ? "$1" : "null";
 			return "{ $proceed($$); " + putstaticRefEventCall + eId +
 				"," + b + "," + fId + "," + o + "); }";
 		}
 		return null;
 	}
 	protected String getfieldPrimitive(FieldAccess e, CtField f) {
 		if (getfieldPrimitiveEvent.present()) {
 			int eId = getfieldPrimitiveEvent.hasLoc() ? set(Emap, e) : Runtime.MISSING_FIELD_VAL;
 			String b = getfieldPrimitiveEvent.hasBaseObj() ? "$0" : "null";
 			int fId = getfieldPrimitiveEvent.hasFld() ? getFid(f) : Runtime.MISSING_FIELD_VAL;
 			return "{ $_ = $proceed($$); " + getfieldPriEventCall +
 				eId + "," + b + "," + fId + "); }"; 
 		}
 		return null;
 	}
 	protected String getfieldReference(FieldAccess e, CtField f) {
 		if (getfieldReferenceEvent.present()) {
 			int eId = getfieldReferenceEvent.hasLoc() ? set(Emap, e) : Runtime.MISSING_FIELD_VAL;
 			String b = getfieldReferenceEvent.hasBaseObj() ? "$0" : "null";
 			int fId = getfieldReferenceEvent.hasFld() ? getFid(f) : Runtime.MISSING_FIELD_VAL;
 			String o = getfieldReferenceEvent.hasObj() ? "$_" : "null";
 			return "{ $_ = $proceed($$); " + getfieldReference +
 				eId + "," + b + "," + fId + "," + o + "); }"; 
 		}
 		return null;
 	}
 	protected String putfieldPrimitive(FieldAccess e, CtField f) {
 		if (putfieldPrimitiveEvent.present()) {
 			int eId = putfieldPrimitiveEvent.hasLoc() ? set(Emap, e) : Runtime.MISSING_FIELD_VAL;
 			String b = putfieldPrimitiveEvent.hasBaseObj() ? "$0" : "null";
 			int fId = putfieldPrimitiveEvent.hasFld() ? getFid(f) : Runtime.MISSING_FIELD_VAL;
 			return "{ $proceed($$); " + putfieldPriEventCall + eId +
 				"," + b + "," + fId + "); }"; 
 		}
 		return null;
 	}
 	protected String putfieldReference(FieldAccess e, CtField f) {
 		if (putfieldReferenceEvent.present()) {
 			int eId = putfieldReferenceEvent.hasLoc() ? set(Emap, e) : Runtime.MISSING_FIELD_VAL;
 			String b = putfieldReferenceEvent.hasBaseObj() ? "$0" : "null";
 			int fId = putfieldReferenceEvent.hasFld() ? getFid(f) : Runtime.MISSING_FIELD_VAL;
 			String o = putfieldReferenceEvent.hasObj() ? "$1" : "null";
 			return "{ $proceed($$); " + putfieldRefEventCall +
 				eId + "," + b + "," + fId + "," + o + "); }"; 
 		}
 		return null;
 	}
 	protected String aloadPrimitive(ArrayAccess e) {
 		if (aloadPrimitiveEvent.present()) {
 			int eId = aloadPrimitiveEvent.hasLoc() ? set(Emap, e) : Runtime.MISSING_FIELD_VAL;
 			String b = aloadPrimitiveEvent.hasBaseObj() ? "$0" : "null";
 			String i = aloadPrimitiveEvent.hasIdx() ? "$1" : "-1";
 			return "{ $_ = $proceed($$); " + aloadPriEventCall +
 				eId + "," + b + "," + i + "); }"; 
 		}
 		return null;
 	}
 	protected String aloadReference(ArrayAccess e) {
 		if (aloadReferenceEvent.present()) {
 			int eId = aloadReferenceEvent.hasLoc() ? set(Emap, e) : Runtime.MISSING_FIELD_VAL;
 			String b = aloadReferenceEvent.hasBaseObj() ? "$0" : "null";
 			String i = aloadReferenceEvent.hasIdx() ? "$1" : "-1";
 			String o = aloadReferenceEvent.hasObj() ? "$_" : "null";
 			return "{ $_ = $proceed($$); " + aloadRefEventCall +
 				eId + "," + b + "," + i + "," + o + "); }"; 
 		}
 		return null;
 	}
 	protected String astorePrimitive(ArrayAccess e) {
 		if (astorePrimitiveEvent.present()) {
 			int eId = astorePrimitiveEvent.hasLoc() ? set(Emap, e) : Runtime.MISSING_FIELD_VAL;
 			String b = astorePrimitiveEvent.hasBaseObj() ? "$0" : "null";
 			String i = astorePrimitiveEvent.hasIdx() ? "$1" : "-1";
 			return "{ $proceed($$); " + astorePriEventCall +
 				eId + "," + b + "," + i + "); }"; 
 		}
 		return null;
 	}
 	protected String astoreReference(ArrayAccess e) {
 		if (astoreReferenceEvent.present()) {
 			int eId = astoreReferenceEvent.hasLoc() ? set(Emap, e) : Runtime.MISSING_FIELD_VAL;
 			String b = astoreReferenceEvent.hasBaseObj() ? "$0" : "null";
 			String i = astoreReferenceEvent.hasIdx() ? "$1" : "-1";
 			String o = astoreReferenceEvent.hasObj() ? "$2" : "null";
 			return "{ $proceed($$); " + astoreRefEventCall +
 				eId + "," + b + "," + i + "," + o + "); }"; 
 		}
 		return null;
 	}
 	protected String processThreadRelatedCall(MethodCall e, CtMethod m) {
 		String instr = null;
 		String cName = m.getDeclaringClass().getName();
 		if (cName.equals("java.lang.Object")) {
 			String mName = m.getName();
 			String mDesc = m.getSignature();
 			if (mName.equals("wait") && mDesc.equals("()V")) {
 				if (waitEvent.present()) {
 					int iId = waitEvent.hasLoc() ? set(Imap, e) : Runtime.MISSING_FIELD_VAL;
 					String o = waitEvent.hasObj() ? "$0" : "null";
 					instr = waitEventCall + iId + "," + o + ");";
 				}
 			} else if (mName.equals("notifyAll") && mDesc.equals("()V")) {
 				if (notifyEvent.present()) {
 					int iId = notifyEvent.hasLoc() ? set(Imap, e) : Runtime.MISSING_FIELD_VAL;
 					String o = notifyEvent.hasObj() ? "$0" : "null";
 					instr = notifyAllEventCall + iId + "," + o + ");";
 				}
 			} else if (mName.equals("notify") && mDesc.equals("()V")) {
 				if (notifyEvent.present()) {
 					int iId = notifyEvent.hasLoc() ? set(Imap, e) : Runtime.MISSING_FIELD_VAL;
 					String o = notifyEvent.hasObj() ? "$0" : "null";
 					instr = notifyEventCall + iId + "," + o + ");";
 				}
 			}
 		} else if (cName.equals("java.lang.Thread")) {
 			String mName = m.getName();
 			String mDesc = m.getSignature();
 			if (mName.equals("start") && mDesc.equals("()V")) {
 				if (threadStartEvent.present()) {
 					int iId = threadStartEvent.hasLoc() ? set(Imap, e) : Runtime.MISSING_FIELD_VAL;
 					String o = threadStartEvent.hasObj() ? "$0" : "null";
 					instr = threadStartEventCall + iId + "," + o + ");";
 				}
 			} else if (mName.equals("join") && mDesc.equals("()V")) {
 				if (threadJoinEvent.present()) {
 					int iId = threadJoinEvent.hasLoc() ? set(Imap, e) : Runtime.MISSING_FIELD_VAL;
 					String o = threadJoinEvent.hasObj() ? "$0" : "null";
 					instr = threadJoinEventCall + iId + "," + o + ");";
 				}
 			}
 		} else if (cName.startsWith("java.util.concurrent.locks.") &&
 				cName.endsWith("ConditionObject")) {
 			String mName = m.getName();
 			String mDesc = m.getSignature();
 			if (mName.equals("await") && mDesc.equals("()V")) {
 				if (waitEvent.present()) {
 					int iId = waitEvent.hasLoc() ? set(Imap, e) : Runtime.MISSING_FIELD_VAL;
 					String o = waitEvent.hasObj() ? "$0" : "null";
 					instr = waitEventCall + iId + "," + o + ");";
 				}
 			} else if (mName.equals("signalAll") && mDesc.equals("()V")) {
 				if (notifyEvent.present()) {
 					int iId = notifyEvent.hasLoc() ? set(Imap, e) : Runtime.MISSING_FIELD_VAL;
 					String o = notifyEvent.hasObj() ? "$0" : "null";
 					instr = notifyAllEventCall + iId + "," + o + ");";
 				}
 			} else if (mName.equals("signal") && mDesc.equals("()V")) {
 				if (notifyEvent.present()) {
 					int iId = notifyEvent.hasLoc() ? set(Imap, e) : Runtime.MISSING_FIELD_VAL;
 					String o = notifyEvent.hasObj() ? "$0" : "null";
 					instr = notifyEventCall + iId + "," + o + ");";
 				}
 			}
 		}
 		return instr;
 	}
 }

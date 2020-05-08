 /*
  * Copyright (c) 2008-2010, Intel Corporation.
  * Copyright (c) 2006-2007, The Trustees of Stanford University.
  * All rights reserved.
  */
 package chord.program;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Arrays;
 
 import java.io.PrintWriter;
 import java.io.IOException;
 
 import com.java2html.Java2HTML;
 
 import chord.project.Project;
 import chord.project.OutDirUtils;
 import chord.project.Messages;
 import chord.project.Properties;
 import chord.util.IndexSet;
 import chord.util.FileUtils;
 import chord.util.ChordRuntimeException;
  
 import joeq.Class.jq_Reference.jq_NullType;
 import joeq.Compiler.Quad.BytecodeToQuad.jq_ReturnAddressType;
 import joeq.Util.Templates.ListIterator;
 import joeq.Main.HostedVM;
 import joeq.UTF.Utf8;
 import joeq.Class.jq_Type;
 import joeq.Class.jq_Class;
 import joeq.Class.jq_Array;
 import joeq.Class.jq_Reference;
 import joeq.Class.jq_Field;
 import joeq.Class.jq_Method;
 import joeq.Class.PrimordialClassLoader;
 import joeq.Compiler.Quad.BasicBlock;
 import joeq.Compiler.Quad.ControlFlowGraph;
 import joeq.Compiler.Quad.Inst;
 import joeq.Compiler.Quad.Operator;
 import joeq.Compiler.Quad.Quad;
 import joeq.Compiler.Quad.Operand;
 import joeq.Compiler.Quad.Operand.ParamListOperand;
 import joeq.Compiler.Quad.Operand.RegisterOperand;
 import joeq.Compiler.Quad.Operator.Move;
 import joeq.Compiler.Quad.Operator.CheckCast;
 import joeq.Compiler.Quad.Operator.ALoad;
 import joeq.Compiler.Quad.Operator.AStore;
 import joeq.Compiler.Quad.Operator.Getfield;
 import joeq.Compiler.Quad.Operator.Getstatic;
 import joeq.Compiler.Quad.Operator.Invoke;
 import joeq.Compiler.Quad.Operator.Monitor;
 import joeq.Compiler.Quad.Operator.New;
 import joeq.Compiler.Quad.Operator.NewArray;
 import joeq.Compiler.Quad.Operator.Putfield;
 import joeq.Compiler.Quad.Operator.Putstatic;
 import joeq.Main.Helper;
 
 /**
  * Representation of a Java program.
  * 
  * @author Mayur Naik (mhn@cs.stanford.edu)
  */
 public class Program {
 	private IndexSet<jq_Type> types;
 	private IndexSet<jq_Reference> classes;
 	private IndexSet<jq_Reference> newInstancedClasses;
 	private IndexSet<jq_Method> methods;
 	private Map<String, jq_Type> nameToTypeMap;
 	private Map<String, jq_Reference> nameToClassMap;
 	private Map<jq_Class, List<jq_Method>> classToMethodsMap;
 	private jq_Method mainMethod;
 	private boolean HTMLizedJavaSrcFiles;
 	private final Map<Inst, jq_Method> instToMethodMap = 
 		new HashMap<Inst, jq_Method>();
 	private ClassHierarchy ch;
 	private Program() { }
 	private static Program program;
 	public static Program getProgram() {
 		if (program == null) {
 			if (Properties.verbose)
 				jq_Method.setVerbose();
 			if (Properties.doSSA)
 				jq_Method.doSSA();
 			jq_Method.exclude(Properties.scopeExcludeAry);
 			program = new Program();
 		}
 		return program;
 	}
 
 
 	public IndexSet<jq_Reference> getNewInstancedClasses() {
 		return null;
 /*
 		build();
 		return newInstancedClasses;
 */
 	}
 
 	public IndexSet<jq_Method> getMethods() {
 		if (methods == null)
 			computeReachableMethods();
 		return methods;
 	}
 
     public final IndexSet<jq_Reference> getClasses() {
         if (types == null)
             computeClassesAndTypes();
         return classes;
     }
 
     public final IndexSet<jq_Type> getTypes() {
         if (types == null)
             computeClassesAndTypes();
         return types;
     }
 
 	private void computeReachableMethods() {
 		assert (methods == null);
 		boolean filesExist =
 			(new File(Properties.classesFileName)).exists() &&
 			(new File(Properties.methodsFileName)).exists();
 		IScope scope = null;
 		if (Properties.reuseScope && filesExist) {
 			loadMethods();
 			return;
 		}
 		String scopeKind = Properties.scopeKind;
 		boolean flag = false;
 		if (scopeKind.equals("rta")) {
 			scope = new RTAScope(false);
 		} else if (scopeKind.equals("rta_reflect")) {
 			scope = new RTAScope(true);
 		} else if (scopeKind.equals("dynamic")) {
 			assert (false);
 			// scope = new DynamicScope();
 		} else if (scopeKind.equals("cha")) {
 			scope = new CHAScope();
 		} else if (scopeKind.equals("0cfa")) {
 			scope = new RTAScope(false);
 			flag = true;
 		} else
 			Messages.fatal("SCOPE.INVALID_SCOPE_KIND", scopeKind);
 		methods = scope.getMethods();
 		if (flag) {
 			Project.init();
 			Project.runTask("0cfa-scope-java");
 			Project.resetAll();
		} else
 			write();
 	}
 
     private void computeClassesAndTypes() {
         assert (classes == null);
         assert (types == null);
 		if (methods == null)
 			computeReachableMethods();
         PrimordialClassLoader loader = PrimordialClassLoader.loader;
         jq_Type[] typesAry = loader.getAllTypes();
         int numTypes = loader.getNumTypes();
         Arrays.sort(typesAry, 0, numTypes, comparator);
         types = new IndexSet<jq_Type>(numTypes + 2);
         classes = new IndexSet<jq_Reference>();
         types.add(jq_NullType.NULL_TYPE);
         types.add(jq_ReturnAddressType.INSTANCE);
         for (int i = 0; i < numTypes; i++) {
             jq_Type t = typesAry[i];
             assert (t != null);
             types.add(t);
             if (t instanceof jq_Reference && t.isPrepared()) {
                 jq_Reference r = (jq_Reference) t;
                 classes.add(r);
             }
         }
     }
 
 	private void loadMethods() {
 		assert (methods == null);
 		HostedVM.initialize();
         methods = new IndexSet<jq_Method>();
         String methodsFileName = Properties.methodsFileName;
         List<String> methodSigns = FileUtils.readFileToList(methodsFileName);
 		Map<String, jq_Class> map = new HashMap<String, jq_Class>();
 		Set<String> excludedClasses = new HashSet<String>();
         for (String s : methodSigns) {
             MethodSign sign = MethodSign.parse(s);
             String cName = sign.cName;
 			jq_Class c  = map.get(cName);
 			if (c == null) {
 				if (excludedClasses.contains(cName))
 					continue;
 				if (Properties.verbose)
 					Messages.log("SCOPE.LOADING_CLASS", cName);
 				try {
 					c = (jq_Class) jq_Type.parseType(cName);
 					c.prepare();
 				} catch (Exception ex) {
 					Messages.log("SCOPE.EXCLUDING_CLASS", cName);
 					ex.printStackTrace();
 					excludedClasses.add(cName);
 					continue;
 				}
 				map.put(cName, c);
 			}
             String mName = sign.mName;
             String mDesc = sign.mDesc;
             jq_Method m = (jq_Method) c.getDeclaredMember(mName, mDesc);
             assert (m != null);
 			m.getCFG();
             methods.add(m);
 		}
 	}
 
 	private void write() {
 		assert (methods != null);
 		write(methods);
 	}
 
 	public static void write(IndexSet<jq_Method> mList) {
         try {
             PrintWriter out;
             out = new PrintWriter(Properties.methodsFileName);
             for (jq_Method m : mList)
                 out.println(m);
             out.close();
         } catch (IOException ex) {
             throw new ChordRuntimeException(ex);
         }
 	}
 
 	public ClassHierarchy getClassHierarchy() {
 		if (ch == null) {
 			ch = new ClassHierarchy();
 			ch.build();
 		}
 		return ch;
 	}
 
 	private void buildNameToTypeMap() {
 		assert (nameToTypeMap == null);
 		IndexSet<jq_Type> types = getTypes();
 		nameToTypeMap = new HashMap<String, jq_Type>();
 		for (jq_Type t : types) {
 			nameToTypeMap.put(t.getName(), t);
 		}
 	}
 
 	private void buildNameToClassMap() {
 		assert (nameToClassMap == null);
 		IndexSet<jq_Reference> cList = getClasses();
 		nameToClassMap = new HashMap<String, jq_Reference>();
 		for (jq_Reference c : cList) {
 			nameToClassMap.put(c.getName(), c);
 		}
 	}
 
 	private void buildClassToMethodsMap() {
 		assert (classToMethodsMap == null);
 		classToMethodsMap = new HashMap<jq_Class, List<jq_Method>>();
 		IndexSet<jq_Method> mList = getMethods();
 		for (jq_Method m : mList) {
 			jq_Class c = m.getDeclaringClass();
 			List<jq_Method> mList2 = classToMethodsMap.get(c);
 			if (mList2 == null) {
 				mList2 = new ArrayList<jq_Method>();
 				classToMethodsMap.put(c, mList2);
 			}
 			mList2.add(m);
 		}
 	}
 
 	public jq_Reference getClass(String name) {
 		if (nameToClassMap == null)
 			buildNameToClassMap();
 		return nameToClassMap.get(name);
 	}
 
 	public List<jq_Method> getMethods(jq_Class c) {
 		if (classToMethodsMap == null)
 			buildClassToMethodsMap();
 		List<jq_Method> mList = classToMethodsMap.get(c);
 		if (mList == null)
 			return Collections.emptyList();
 		return mList;
 	}
 
 	public jq_Method getMethod(String mName, String mDesc, String cName) {
 		jq_Reference r = getClass(cName);
 		if (r == null || r instanceof jq_Array)
 			return null;
 		jq_Class c = (jq_Class) r;
 		List<jq_Method> mList = getMethods(c);
 		for (jq_Method m : mList) {
 			if (m.getName().toString().equals(mName) &&
 				m.getDesc().toString().equals(mDesc))
 				return m;
 		}
 		return null;
 	}
 	
 	public jq_Method getMethod(MethodSign sign) {
 		return getMethod(sign.mName, sign.mDesc, sign.cName);
 	}
 
 	public jq_Method getMethod(String sign) {
 		return getMethod(MethodSign.parse(sign));
 	}
 
 	public jq_Method getMainMethod() {
 		if (mainMethod == null) {
 			String mainClassName = Properties.mainClassName;
 			if (mainClassName == null)
 				Messages.fatal("SCOPE.MAIN_CLASS_NOT_DEFINED");
 			mainMethod = getMethod("main", "([Ljava/lang/String;)V", mainClassName);
 			if (mainMethod == null)
 				Messages.fatal("SCOPE.MAIN_METHOD_NOT_FOUND", mainClassName);
 		}
 		return mainMethod;
 	}
 
 	public jq_Method getThreadStartMethod() {
 		return getMethod("start", "()V", "java.lang.Thread");
 	}
 
 	public jq_Type getType(String name) {
 		if (nameToTypeMap == null)
 			buildNameToTypeMap();
 		return nameToTypeMap.get(name);
 	}
 
 	////////
 
 	public void mapInstToMethod(Inst i, jq_Method m) {
 		instToMethodMap.put(i, m);
 	}
 
 	public jq_Method getMethod(Inst i) {
 		jq_Method m = instToMethodMap.get(i);
 		if (m == null) {
 			throw new ChordRuntimeException("Cannot find method containing inst: " + i);
 		}
 		return m;
 	}
 
 	////////
 
 	public static String getSign(jq_Method m) {
 		String d = m.getDesc().toString();
 		return m.getName().toString() + methodDescToStr(d);
 	}
 	
 	// convert the given method descriptor string to a string
 	// denoting the comma-separated list of types of the method's
 	// arguments in human-readable form
 	// e.g.: convert <tt>([Ljava/lang/String;I)V<tt> to
 	// <tt>(java.lang.String[],int)</tt>
 	public static String methodDescToStr(String desc) {
 		String t = desc.substring(1, desc.indexOf(')'));
 		return "(" + typesToStr(t) + ")";
 	}
 	
 	// convert the given bytecode string encoding a (possibly empty)
 	// list of types to a string denoting the comma-separated list
 	// of those types in human-readable form
 	// e.g. convert <tt>[Ljava/lang/String;I</tt> to
 	// <tt>java.lang.String[],int</tt>
 	public static String typesToStr(String typesStr) {
     	String result = "";
     	boolean needsSep = false;
         while (typesStr.length() != 0) {
             boolean isArray = false;
             int numDim = 0;
             String baseType;
             // Handle array case
             while(typesStr.startsWith("[")) {
             	isArray = true;
             	numDim++;
             	typesStr = typesStr.substring(1);
             }
             // Determine base type
             if (typesStr.startsWith("B")) {
             	baseType = "byte";
             	typesStr = typesStr.substring(1);
             } else if (typesStr.startsWith("C")) {
             	baseType = "char";
             	typesStr = typesStr.substring(1);
             } else if (typesStr.startsWith("D")) {
             	baseType = "double";
             	typesStr = typesStr.substring(1);
             } else if (typesStr.startsWith("F")) {
             	baseType = "float";
             	typesStr = typesStr.substring(1);
             } else if (typesStr.startsWith("I")) {
             	baseType = "int";
             	typesStr = typesStr.substring(1);
             } else if (typesStr.startsWith("J")) {
             	baseType = "long";
             	typesStr = typesStr.substring(1);
             } else if (typesStr.startsWith("L")) {
             	int index = typesStr.indexOf(';');
             	if(index == -1)
             		throw new RuntimeException("Class reference has no ending ;");
             	String className = typesStr.substring(1, index);
             	baseType = className.replace('/', '.');
             	typesStr = typesStr.substring(index + 1);
             } else if (typesStr.startsWith("S")) {
             	baseType = "short";
             	typesStr = typesStr.substring(1);
             } else if (typesStr.startsWith("Z")) {
             	baseType = "boolean";
             	typesStr = typesStr.substring(1);
             } else if (typesStr.startsWith("V")) {
             	baseType = "void";
             	typesStr = typesStr.substring(1);
             } else
             	throw new RuntimeException("Unknown field type!");
             if (needsSep)
             	result += ",";
             result += baseType;
             if (isArray) {
             	for (int i = 0; i < numDim; i++)
             		result += "[]";
             }
             needsSep = true;
         }
         return result;
 	}
 
 	public static List<String> getDynamicallyLoadedClasses() {
 		String mainClassName = Properties.mainClassName;
 		if (mainClassName == null)
 			Messages.fatal("SCOPE.MAIN_CLASS_NOT_DEFINED");
 		String classPathName = Properties.classPathName;
 		if (classPathName == null)
 			Messages.fatal("SCOPE.CLASS_PATH_NOT_DEFINED");
         String[] runIDs = Properties.runIDs.split(Properties.LIST_SEPARATOR);
 		assert(runIDs.length > 0);
         final String cmd = "java " + Properties.runtimeJvmargs +
             " -cp " + classPathName +
             " -agentpath:" + Properties.instrAgentFileName +
             "=classes_file_name=" + Properties.classesFileName +
             " " + mainClassName + " ";
 		List<String> classNames = new ArrayList<String>();
 		String fileName = Properties.classesFileName;
         for (String runID : runIDs) {
             String args = System.getProperty("chord.args." + runID, "");
 			OutDirUtils.executeWithFailOnError(cmd + args);
 			try {
 				BufferedReader in = new BufferedReader(new FileReader(fileName));
 				String s;
 				while ((s = in.readLine()) != null) {
 					// convert "Lsun/misc/Signal;" to "sun.misc.Signal"
 					String cName = typesToStr(s);
 					classNames.add(cName);
 				}
 				in.close();
 			} catch (Exception ex) {
 				throw new ChordRuntimeException(ex);
 			}
 		}
 		return classNames;
 	}
 
 	/**
 	 * Dumps this program's Java source files in HTML form.
 	 */
 	public void HTMLizeJavaSrcFiles() {
 		if (!HTMLizedJavaSrcFiles) {
 			String srcPathName = Properties.srcPathName;
 			if (srcPathName == null)
 				Messages.fatal("SCOPE.SRC_PATH_NOT_DEFINED");
 			String[] srcDirNames = srcPathName.split(File.pathSeparator);
 			try {
 				Java2HTML java2HTML = new Java2HTML();
 				java2HTML.setMarginSize(4);
 				java2HTML.setTabSize(4);
 				java2HTML.setJavaDirectorySource(srcDirNames);
 				java2HTML.setDestination(Properties.outDirName);
 				java2HTML.buildJava2HTML();
 			} catch (Exception ex) {
 				throw new ChordRuntimeException(ex);
 			}
 			HTMLizedJavaSrcFiles = true;
 		}
 	}
 	
 	public static int getLineNumber(Quad q, jq_Method m) {
 		int bci = m.getBCI(q);
 		if (bci == -1)
 			return 0;
 		return m.getLineNumber(bci);
 	}
 	
 	public static int getLineNumber(Inst i, jq_Method m) {
 		if (i instanceof Quad)
 			return getLineNumber((Quad) i, m);
 		return 0;
 	}
 	
 	public static String toString(int bci, String mName, String mDesc,
 			String cName) {
 		return bci + "!" + mName + ":" + mDesc + "@" + cName;
 	}
 	
 	public static String toString(String name, String desc, String cName) {
 		return name + ":" + desc + "@" + cName;
 	}
 
 	public static jq_Field getField(Quad e) {
 		Operator op = e.getOperator();
 		if (op instanceof ALoad || op instanceof AStore)
 			return null;
 		if (op instanceof Getfield)
 			return Getfield.getField(e).getField();
 		if (op instanceof Putfield)
 			return Putfield.getField(e).getField();
 		if (op instanceof Getstatic)
 			return Getstatic.getField(e).getField();
 		if (op instanceof Putstatic)
 			return Putstatic.getField(e).getField();
 		throw new RuntimeException();
 	}
 	
 	public static boolean isHeapInst(Operator op) {
 		return op instanceof ALoad || op instanceof AStore ||
 			op instanceof Getfield || op instanceof Putfield ||
 			op instanceof Getstatic || op instanceof Putstatic;
 	}
 	
 	public static boolean isWrHeapInst(Operator op) {
 		return op instanceof Putfield || op instanceof AStore ||
 			op instanceof Putstatic;
 	}
 	
 	public static String getSourceFileName(jq_Class c) {
 		Utf8 f = c.getSourceFile();
 		if (f == null)
 			return null;
 		String t = c.getName();
 		int i = t.lastIndexOf('.') + 1;
 		String s = t.substring(0, i);
 		return s.replace('.', '/') + f;
 	}
 
 	/**************************************************************
 	 * Functions for printing program structures in various formats
 	 **************************************************************/
 
 	public String toVerboseStr(Quad q) {
 		return toBytePosStr(q) + " (" + toJavaPosStr(q) + ") [" + toQuadStr(q) + "]";
 	}
 
 	public String toPosStr(Quad q) {
 		return toBytePosStr(q) + " (" + toJavaPosStr(q) + ")";
 	}
 
 	public String toJavaPosStr(Quad q) {
 		jq_Method m = getMethod(q);
 		jq_Class c = m.getDeclaringClass();
 		String fileName = getSourceFileName(c);
 		int lineNumber = getLineNumber(q, m);
 		return fileName + ":" + lineNumber;
 	}
 
 	public String toBytePosStr(Inst i) {
         if (i == null)
             return "null";
         jq_Method m = getMethod(i);
         int bci;
         if (i instanceof Quad)
             bci = m.getBCI((Quad) i);
         else {
             BasicBlock b = (BasicBlock) i;
             if (b.isEntry())
                 bci = -1;
             else {
                 assert (b.isExit());
                 bci = -2;
             }
         }
         String mName = m.getName().toString();
         String mDesc = m.getDesc().toString();
         String cName = m.getDeclaringClass().getName();
         return toString(bci, mName, mDesc, cName);
 	}
 
 	public static String toBytePosStr(Quad q, jq_Method m) {
         int bci = m.getBCI(q);
         String mName = m.getName().toString();
         String mDesc = m.getDesc().toString();
         String cName = m.getDeclaringClass().getName();
         return toString(bci, mName, mDesc, cName);
 	}
 
 	public String toQuadStr(Quad q) {
 		Operator op = q.getOperator();
 		if (op instanceof Move || op instanceof CheckCast) {
 			return Move.getDest(q) + " = " + Move.getSrc(q);
 		}
 		if (op instanceof Getfield || op instanceof Putfield ||
 				op instanceof ALoad || op instanceof AStore ||
 				op instanceof Getstatic || op instanceof Putstatic) {
 			return toStringHeapInst(q);
 		}
 		if (op instanceof New || op instanceof NewArray)
 			return toStringNewInst(q);
 		if (op instanceof Invoke)
 			return toStringInvokeInst(q);
 		return q.toString();
 	}
 
 	public static String toString(RegisterOperand op) {
 		return "<" + op.getType().getName() + " " + op.getRegister() + ">";
 	}
 
 	public static String toString(Operand op) {
 		if (op instanceof RegisterOperand)
 			return toString((RegisterOperand) op);
 		return op.toString();
 	}
 
 	public String toStringInvokeInst(Quad q) {
 		String s = "";
 		RegisterOperand ro = Invoke.getDest(q);
 		if (ro != null) 
 			s = toString(ro) + " = ";
 		else
 			s = "";
 		jq_Method m = Invoke.getMethod(q).getMethod();
 		s += m.getNameAndDesc().toString() + "(";
 		ParamListOperand po = Invoke.getParamList(q);
 		int n = po.length();
 		for (int i = 0; i < n; i++) {
 			s += toString(po.get(i));
 			if (i < n - 1)
 				s += ",";
 		}
 		return s + ")";
 	}
 	
 	public String toStringNewInst(Quad q) {
 		String t, l;
 		if (q.getOperator() instanceof New) {
 			l = toString(New.getDest(q));
 			t = New.getType(q).getType().getName();
 		} else {
 			l = toString(NewArray.getDest(q));
 			t = NewArray.getType(q).getType().getName();
 		}
 		return l + " = new " + t;
 	}
 	
 	public String toStringHeapInst(Quad q) {
 		Operator op = q.getOperator();
 		String s;
 		if (isWrHeapInst(op)) {
 			String b, f, r;
 			if (op instanceof Putfield) {
 				b = toString(Putfield.getBase(q)) + ".";
 				f = Putfield.getField(q).getField().toString();
 				r = toString(Putfield.getSrc(q));
 			} else if (op instanceof AStore) {
 				b = toString(AStore.getBase(q));
 				f = "[*]";
 				r = toString(AStore.getValue(q));
 			} else {
 				b = "";
 				f = Putstatic.getField(q).getField().toString();
 				r = toString(Putstatic.getSrc(q));
 			}
 			s = b + f + " = " + r;
 		} else {
 			String l, b, f;
 			if (op instanceof Getfield) {
 				l = toString(Getfield.getDest(q));
 				b = toString(Getfield.getBase(q)) + ".";
 				f = Getfield.getField(q).getField().toString();
 			} else if (op instanceof ALoad) {
 				l = toString(ALoad.getDest(q));
 				b = toString(ALoad.getBase(q));
 				f = "[*]";
 			} else {
 				l = toString(Getstatic.getDest(q));
 				b = "";
 				f = Getstatic.getField(q).getField().toString();
 			}
 			s = l + " = " + b + f;
 			
 		}
 		return s;
 	}
 	
 	public String toStringLockInst(Inst q) {
 		String s;
 		if (q instanceof Quad)
 			s = Monitor.getSrc((Quad) q).toString();
 		else
 			s = getMethod(q).toString();
 		return "monitorenter " + s;
 	}
 	
 	public void printMethod(String sign) {
 		jq_Method m = getMethod(sign);
 		if (m == null)
 			Messages.fatal("SCOPE.METHOD_NOT_FOUND", sign);
 		printMethod(m);
 	}
 	public void printClass(String className) {
 		jq_Reference c = getClass(className);
 		if (c == null)
 			Messages.fatal("SCOPE.CLASS_NOT_FOUND", className);
 		printClass(c);
 	}
 	private void printClass(jq_Reference r) {
 		System.out.println("*** Class: " + r);
 		if (r instanceof jq_Array)
 			return;
 		jq_Class c = (jq_Class) r;
 		for (jq_Method m : getMethods(c))
 			printMethod(m);
 	}
 	private void printMethod(jq_Method m) {
 		System.out.println("Method: " + m);
 		if (!m.isAbstract()) {
 			ControlFlowGraph cfg = m.getCFG();
 			for (ListIterator.BasicBlock it = cfg.reversePostOrderIterator();
 					it.hasNext();) {
 				BasicBlock bb = it.nextBasicBlock();
 				for (ListIterator.Quad it2 = bb.iterator(); it2.hasNext();) {
 					Quad q = it2.nextQuad();                        
 					int bci = m.getBCI(q);
 					System.out.println("\t" + bci + "#" + q.getID());
 				}
 			}
 			System.out.println(cfg.fullDump());
 		}
 	}
 	public void printAllClasses() {
 		for (jq_Reference c : getClasses())
 			printClass(c);
 	}
     private static Comparator comparator = new Comparator() {
         public int compare(Object o1, Object o2) {
             jq_Type t1 = (jq_Type) o1;
             jq_Type t2 = (jq_Type) o2;
             String s1 = t1.getName();
             String s2 = t2.getName();
             return s1.compareTo(s2);
         }
     };
 }

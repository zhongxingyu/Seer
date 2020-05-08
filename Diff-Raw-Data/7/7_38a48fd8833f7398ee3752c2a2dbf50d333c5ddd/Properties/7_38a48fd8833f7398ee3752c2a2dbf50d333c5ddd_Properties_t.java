 /*
  * Copyright (c) 2008-2009, Intel Corporation.
  * Copyright (c) 2006-2007, The Trustees of Stanford University.
  * All rights reserved.
  */
 package chord.project;
 
 import java.io.File;
 import java.io.PrintWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import chord.util.FileUtils;
 import chord.util.ChordRuntimeException;
 
 /**
  * System properties recognized by Chord.
  *
  * @author Mayur Naik (mhn@cs.stanford.edu)
  */
 public class Properties {
 	private Properties() { }
 
 	public final static String LIST_SEPARATOR = " |,|:|;";
 
 	// Chord resource properties
 
 	public final static String mainDirName = System.getProperty("chord.main.dir");
 	static {
 		if (mainDirName == null)
 			Messages.fatal("PROPERTIES.CHORD_MAIN_DIR_UNDEFINED");
 	}
 	public static String libDirName = mainRel2AbsPath("chord.lib.dir", "lib");
 	public final static String mainClassPathName = System.getProperty("chord.main.class.path");
 	public final static String bddbddbClassPathName = System.getProperty("chord.bddbddb.class.path");
 	public static String instrAgentFileName = mainRel2AbsPath("chord.instr.agent.file", "lib" + File.separator + "libchord_instr_agent.so");
 	public final static String javadocURL = System.getProperty("chord.javadoc.url", "http://chord.stanford.edu/javadoc_2_0/");
 
 	// Chord boot properties
 
 	public static String workDirName = System.getProperty("chord.work.dir");
 	static {
 		if (workDirName == null) {
 			workDirName = System.getProperty("user.dir");
 			if (workDirName == null)
 				throw new ChordRuntimeException(Messages.get("PROPERTIES.CHORD_WORK_DIR_UNDEFINED"));
 			Messages.log("PROPERTIES.USER_DIR_AS_CHORD_WORK_DIR", workDirName);
 		}
 	}
 	public final static String propsFileName = System.getProperty("chord.props.file");
 	public final static String maxHeap = System.getProperty("chord.max.heap");
 	public final static String maxStack = System.getProperty("chord.max.stack");
 	public final static String jvmargs = System.getProperty("chord.jvmargs");
 	public final static String bddbddbMaxHeap = System.getProperty("chord.bddbddb.max.heap", "1024m");
 
 	// Chord task properties
 
 	public final static boolean buildScope = buildBoolProperty("chord.build.scope", false);
 	public final static String runAnalyses = System.getProperty("chord.run.analyses", "");
 	public final static String printRels = System.getProperty("chord.print.rels", "");
 	public final static boolean publishTargets = buildBoolProperty("chord.publish.targets", false);
 
 	// Basic program properties
 
 	public final static String mainClassName = System.getProperty("chord.main.class");
 	public final static String classPathName = System.getProperty("chord.class.path");
 	public final static String srcPathName = System.getProperty("chord.src.path");
 	public final static String runIDs = System.getProperty("chord.run.ids", "0");
 	public final static String runtimeJvmargs = System.getProperty("chord.runtime.jvmargs", "-ea -Xmx1024m");
 
 	// Program scope properties
 
 	public final static String scopeKind = System.getProperty("chord.scope.kind", "rta");
 	public final static boolean reuseScope = buildBoolProperty("chord.reuse.scope", false);
 	public final static String CHkind = System.getProperty("chord.ch.kind", "static");
 
	public final static String mainClassPathPackages = "chord.,javassist.,joeq.,net.sf.bddbddb.,net.sf.javabdd.";

	public final static String DEFAULT_SCOPE_EXCLUDES = mainClassPathPackages;
 	public final static String DEFAULT_CHECK_EXCLUDES =
		concat(mainClassPathPackages, ',', "java.,javax.,sun.,com.sun.,com.ibm.,org.apache.harmony.");
 
 	public final static String scopeExcludeStdStr = System.getProperty("chord.scope.exclude.std", DEFAULT_SCOPE_EXCLUDES);
 	public final static String scopeExcludeExtStr = System.getProperty("chord.scope.exclude.ext", "");
 	public static String scopeExcludeStr = System.getProperty("chord.scope.exclude",
 		concat(scopeExcludeStdStr, ',', scopeExcludeExtStr));
 	public static String[] scopeExcludeAry = toArray(scopeExcludeStr);
 
 	// Program analysis properties
 
 	public static String javaAnalysisPathName = mainRel2AbsPath("chord.java.analysis.path", "classes");
 	public static String dlogAnalysisPathName = mainRel2AbsPath("chord.dlog.analysis.path", "src" + File.separator + "dlog");
 	public final static boolean reuseRels = buildBoolProperty("chord.reuse.rels", false);
 	public final static boolean publishResults = buildBoolProperty("chord.publish.results", true);
 
 	public final static String checkExcludeStdStr = System.getProperty("chord.check.exclude.std", DEFAULT_CHECK_EXCLUDES);
 	public final static String checkExcludeExtStr = System.getProperty("chord.check.exclude.ext", "");
 	public static String checkExcludeStr = System.getProperty("chord.check.exclude",
 		concat(checkExcludeStdStr, ',', checkExcludeExtStr));
 	public static String[] checkExcludeAry = toArray(checkExcludeStr);
 
     // Program transformation properties
  
 	public final static boolean doSSA = buildBoolProperty("chord.ssa", true);
 
     // Chord debug properites
 
 	public final static boolean verbose = buildBoolProperty("chord.verbose", false);
 	public final static boolean bddbddbVerbose = buildBoolProperty("chord.bddbddb.verbose", false);
 	public final static boolean saveDomMaps = buildBoolProperty("chord.save.maps", true);
 
 	// Chord instrumentation properties
 
 	public final static boolean reuseTrace = buildBoolProperty("chord.reuse.trace", false);
 	public final static boolean doTracePipe = buildBoolProperty("chord.trace.pipe", true);
 	static {
 		if (reuseTrace) {
 			assert(!doTracePipe);
 			assert(reuseScope);
 		}
 	}
 	public final static int traceBlockSize = Integer.getInteger("chord.trace.block.size", 4096);
 	public final static String runtimeClassName =
 		System.getProperty("chord.runtime.class", chord.runtime.BufferedRuntime.class.getName());
 	public final static int maxConstr = Integer.getInteger("chord.max.constr", 50000000);
 
 	// Chord output properties
 
 	public static String outDirName = workRel2AbsPath("chord.out.dir", "chord_output");
 	static {
 		// Automatically find a free subdirectory; this is Percy;s stuff
 		String outPoolPath = System.getProperty("chord.out.pooldir");
 		if (outPoolPath != null) {
 			for (int i = 0; true; i++) {
 				outDirName = outPoolPath+"/"+i+".exec";
 				if (!new File(outDirName).exists())
 					break;
 			}
 		}
 		FileUtils.mkdirs(outDirName);
 	}
 	public final static String outFileName = outRel2AbsPath("chord.out.file", "log.txt");
 	public final static String errFileName = outRel2AbsPath("chord.err.file", "log.txt");
 	public final static String classesFileName = outRel2AbsPath("chord.classes.file", "classes.txt");
 	public final static String methodsFileName = outRel2AbsPath("chord.methods.file", "methods.txt");
 	public static String bddbddbWorkDirName = outRel2AbsPath("chord.bddbddb.work.dir", "bddbddb");
 	// TODO: create this dir on demand
 	static {
 		FileUtils.mkdirs(bddbddbWorkDirName);
 	}
 
 	public final static String bootClassesDirName = outRel2AbsPath("chord.boot.classes.dir", "boot_classes");
 	public final static String userClassesDirName = outRel2AbsPath("chord.user.classes.dir", "user_classes");
 	public final static String instrSchemeFileName = outRel2AbsPath("chord.instr.scheme.file", "scheme.ser");
 	public final static String crudeTraceFileName = outRel2AbsPath("chord.crude.trace.file", "crude_trace.txt");
 	public final static String finalTraceFileName = outRel2AbsPath("chord.final.trace.file", "final_trace.txt");
 
 	public static void print() {
 		System.out.println("*** Chord resource properties:");
 		System.out.println("chord.main.dir: " + mainDirName);
 		System.out.println("chord.lib.dir: " + libDirName);
 		System.out.println("chord.main.class.path: " + mainClassPathName);
 		System.out.println("chord.bddbddb.class.path: " + bddbddbClassPathName);
 		System.out.println("chord.instr.agent.file: " + instrAgentFileName);
 		System.out.println("chord.javadoc.url: " + javadocURL);
 
 		System.out.println("*** Chord boot properties:");
 		System.out.println("chord.work.dir: " + workDirName);
 		System.out.println("chord.props.file: " + propsFileName);
 		System.out.println("chord.max.heap: " + maxHeap);
 		System.out.println("chord.max.stack: " + maxStack);
 		System.out.println("chord.jvmargs: " + jvmargs);
 		System.out.println("chord.bddbddb.max.heap: " + bddbddbMaxHeap);
 
 		System.out.println("*** Chord task properties:");
 		System.out.println("chord.build.scope: " + buildScope);
 		System.out.println("chord.run.analyses: " + runAnalyses);
 		System.out.println("chord.print.rels: " + printRels);
 		System.out.println("chord.publish.targets: " + publishTargets);
 
 		System.out.println("*** Basic program properties:");
 		System.out.println("chord.main.class: " + mainClassName);
 		System.out.println("chord.class.path: " + classPathName);
 		System.out.println("chord.src.path: " + srcPathName);
 		System.out.println("chord.run.ids: " + runIDs);
 		System.out.println("chord.runtime.jvmargs: " + runtimeJvmargs);
 
 		System.out.println("*** Program scope properties:");
 		System.out.println("chord.scope.kind: " + scopeKind);
 		System.out.println("chord.reuse.scope: " + reuseScope);
 		System.out.println("chord.ch.kind: " + CHkind);
 		System.out.println("chord.scope.exclude.std: " + scopeExcludeStdStr);
 		System.out.println("chord.scope.exclude.ext: " + scopeExcludeExtStr);
 		System.out.println("chord.scope.exclude: " + scopeExcludeStr);
 
 		System.out.println("*** Program analysis properties:");
 		System.out.println("chord.java.analysis.path: " + javaAnalysisPathName);
 		System.out.println("chord.dlog.analysis.path: " + dlogAnalysisPathName);
 		System.out.println("chord.reuse.rels: " + reuseRels);
 		System.out.println("chord.publish.results: " + publishResults);
 		System.out.println("chord.check.exclude.std: " + checkExcludeStdStr);
 		System.out.println("chord.check.exclude.ext: " + checkExcludeExtStr);
 		System.out.println("chord.check.exclude: " + checkExcludeStr);
 
 		System.out.println("*** Program transformation properties:");
 		System.out.println("chord.ssa: " + doSSA);
 
 		System.out.println("*** Chord debug properties:");
 		System.out.println("chord.verbose: " + verbose);
 		System.out.println("chord.bddbddb.verbose: " + bddbddbVerbose);
 		System.out.println("chord.save.maps: " + saveDomMaps);
 
 		System.out.println("*** Chord instrumentation properties:");
 		System.out.println("chord.reuse.trace: " + reuseTrace);
 		System.out.println("chord.trace.pipe: " + doTracePipe);
 		System.out.println("chord.trace.block.size: " + traceBlockSize);
 		System.out.println("chord.runtime.class: " + runtimeClassName);
 		System.out.println("chord.max.constr: " + maxConstr);
 
 		System.out.println("*** Chord output properties:");
 		System.out.println("chord.out.dir: " + outDirName);
 		System.out.println("chord.out.file: " + outFileName);
 		System.out.println("chord.err.file: " + errFileName);
 		System.out.println("chord.classes.file: " + classesFileName);
 		System.out.println("chord.methods.file: " + methodsFileName);
 		System.out.println("chord.bddbddb.work.dir: " + bddbddbWorkDirName);
 		System.out.println("chord.boot.classes.dir: " + bootClassesDirName);
 		System.out.println("chord.user.classes.dir: " + userClassesDirName);
 		System.out.println("chord.instr.scheme.file: " + instrSchemeFileName);
 		System.out.println("chord.crude.trace.file: " + crudeTraceFileName);
 		System.out.println("chord.final.trace.file: " + finalTraceFileName);
 	}
 	private static String outRel2AbsPath(String propName, String fileName) {
 		String val = System.getProperty(propName);
 		return (val != null) ? val : FileUtils.getAbsolutePath(outDirName, fileName);
 	}
 	private static String mainRel2AbsPath(String propName, String fileName) {
 		String val = System.getProperty(propName);
 		return (val != null) ? val : FileUtils.getAbsolutePath(mainDirName, fileName);
 	}
 	private static String workRel2AbsPath(String propName, String fileName) {
 		String val = System.getProperty(propName);
 		return (val != null) ? val : FileUtils.getAbsolutePath(workDirName, fileName);
 	}
 	private static boolean buildBoolProperty(String propName, boolean defaultVal) {
 		return System.getProperty(propName, Boolean.toString(defaultVal)).equals("true"); 
 	}
 	public static String[] toArray(String str) {
         return str.equals("") ? new String[0] : str.split(LIST_SEPARATOR);
 	}
 	public static String concat(String s1, char sep, String s2) {
 		if (s1.equals("")) return s2;
 		if (s2.equals("")) return s1;
 		return s1 + sep + s2;
 	}
 }

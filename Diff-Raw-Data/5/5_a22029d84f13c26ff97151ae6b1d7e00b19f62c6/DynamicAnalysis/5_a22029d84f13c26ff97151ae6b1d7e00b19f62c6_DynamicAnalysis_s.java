 /*
  * Copyright (c) 2008-2009, Intel Corporation.
  * Copyright (c) 2006-2007, The Trustees of Stanford University.
  * All rights reserved.
  */
 package chord.project;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.File;
 import java.lang.InterruptedException;
 
 import chord.instr.EventKind;
 import chord.instr.Instrumentor;
 import chord.instr.TracePrinter;
 import chord.instr.InstrScheme;
 import chord.instr.TraceTransformer;
 import chord.instr.InstrScheme.EventFormat;
 import chord.util.ByteBufferedFile;
 import chord.util.ProcessExecutor;
 import chord.util.ReadException;
 import chord.util.Executor;
 import chord.util.IndexMap;
 
 import joeq.Compiler.Quad.BasicBlock;
 
 /**
  * 
  * @author Mayur Naik (mhn@cs.stanford.edu)
  */
 @Chord(
 	name = "dyn-java"
 )
 public class DynamicAnalysis extends JavaAnalysis {
 	public final static boolean DEBUG = false;
 	protected InstrScheme scheme;
 	protected Instrumentor instrumentor;
 	
 	public void initPass() {
 		// signals beginning of parsing of a trace
 		// do nothing by default; subclasses can override
 	}
 	public void donePass() {
 		// signals end of parsing of a trace
 		// do nothing by default; subclasses can override
 	}
 	public void initAllPasses() {
 		// do nothing by default; subclasses can override
 	}
 	public void doneAllPasses() {
 		// do nothing by default; subclasses can override
 	}
 	// subclass must override
 	public InstrScheme getInstrScheme() {
 		throw new ChordRuntimeException();
 	}
 	public void run() {
 		scheme = getInstrScheme();
 		assert(scheme != null);
 		final String instrSchemeFileName = Properties.instrSchemeFileName;
 		scheme.save(instrSchemeFileName);
 		instrumentor = new Instrumentor(Program.v(), scheme);
 		instrumentor.run();
 		final String mainClassName = Properties.mainClassName;
 		assert (mainClassName != null);
 		final String classPathName = Properties.classPathName;
 		assert (classPathName != null);
 		final String bootClassesDirName = Properties.bootClassesDirName;
 		final String userClassesDirName = Properties.userClassesDirName;
 		final IndexMap<String> Mmap = instrumentor.getMmap();
 		final IndexMap<String> Wmap = instrumentor.getWmap();
 		final int numMeths = (Mmap != null) ? Mmap.size() : 0;
 		final String runtimeClassName = Properties.runtimeClassName;
 		String instrProgramCmd = "java -ea -Xbootclasspath/p:" +
 			Properties.mainClassPathName + File.pathSeparator + bootClassesDirName +
 			" " + Properties.runtimeJvmargs +
 			" -Xverify:none" + // " -verbose" + 
 			" -cp " + userClassesDirName + File.pathSeparator + classPathName +
 			" -agentpath:" + Properties.instrAgentFileName +
 			"=num_meths=" + numMeths +
 			"=instr_scheme_file_name=" + instrSchemeFileName +
 			"=calls_bound=" + scheme.getCallsBound() +
 			"=iters_bound=" + scheme.getItersBound() +
 			"=runtime_class_name=" + runtimeClassName.replace('.', '/');
 		final String[] runIDs = Properties.runIDs.split(Properties.LIST_SEPARATOR);
 		final boolean processBuffer =
 			runtimeClassName.equals(BufferedRuntime.class.getName());
 		if (processBuffer) {
 			final String crudeTraceFileName = Properties.crudeTraceFileName;
 			final String finalTraceFileName = Properties.finalTraceFileName;
 			boolean needsTraceTransform = scheme.needsTraceTransform();
 			final String traceFileName = needsTraceTransform ?
 				crudeTraceFileName : finalTraceFileName;
 			instrProgramCmd += 
 				"=trace_block_size=" + Properties.traceBlockSize +
 				"=trace_file_name=" + traceFileName +
 				" " + mainClassName + " ";
 			ProcessExecutor.execute("rm " + crudeTraceFileName);
 			ProcessExecutor.execute("rm " + finalTraceFileName);
 			final boolean doTracePipe = Properties.doTracePipe;
 			if (doTracePipe) {
 				ProcessExecutor.execute("mkfifo " + crudeTraceFileName);
 				ProcessExecutor.execute("mkfifo " + finalTraceFileName);
 			}
 			Runnable traceTransformer = new Runnable() {
 				public void run() {
 					if (DEBUG) {
 						(new TracePrinter(crudeTraceFileName, instrumentor)).run();
 						System.out.println("DONE");
 					}
 					(new TraceTransformer(crudeTraceFileName,
 						finalTraceFileName, scheme)).run();
 				}
 			};
 			Runnable traceProcessor = new Runnable() {
 				public void run() {
 					if (DEBUG) {
 						(new TracePrinter(finalTraceFileName, instrumentor)).run();
 						System.out.println("DONE");
 					}
 					processTrace(finalTraceFileName);
 				}
 			};
 			final boolean serial = doTracePipe ? false : true;
 			final Executor executor = new Executor(serial);
 			initAllPasses();
 			for (String runID : runIDs) {
 				System.out.println("Processing Run ID: " + runID);
 				final String args = System.getProperty("chord.args." + runID, "");
 				final String cmd = instrProgramCmd + args;
 				Runnable instrProgram = new Runnable() {
 					public void run() {
 						ProcessExecutor.execute(cmd);
 					}
 				};
 				executor.execute(instrProgram);
 				if (processBuffer) {
 					if (needsTraceTransform)
 						executor.execute(traceTransformer);
 					executor.execute(traceProcessor);
 					try {
 						executor.waitForCompletion();
 					} catch (InterruptedException ex) {
 						throw new ChordRuntimeException(ex);
 					}
 				}
 			}
 			doneAllPasses();
 		} else {
 			instrProgramCmd += " " + mainClassName + " ";
 			initAllPasses();
 			for (String runID : runIDs) {
 				System.out.println("Processing Run ID: " + runID);
 				final String args = System.getProperty("chord.args." + runID, "");
 				final String cmd = instrProgramCmd + args;
 				initPass();
 				ProcessExecutor.execute(cmd);
 				donePass();
 			}
 			doneAllPasses();
 		}
 	}
 
 	private void processTrace(String fileName) {
 		try {
 			initPass();
 			ByteBufferedFile buffer = new ByteBufferedFile(1024, fileName, true);
 			long count = 0;
 			while (!buffer.isDone()) {
 				byte opcode = buffer.getByte();
 				count++;
 				switch (opcode) {
 				case EventKind.ENTER_METHOD:
 				{
 					int m = buffer.getInt();
 					int t = buffer.getInt();
 					processEnterMethod(m, t);
 					break;
 				}
 				case EventKind.LEAVE_METHOD:
 				{
 					int m = buffer.getInt();
 					int t = buffer.getInt();
 					processLeaveMethod(m, t);
 					break;
 				}
 				case EventKind.ENTER_LOOP:
 				{
 					int w = buffer.getInt();
 					int t = buffer.getInt();
 					processEnterLoop(w, t);
 					break;
 				}
 				case EventKind.LEAVE_LOOP:
 				{
 					int w = buffer.getInt();
 					int t = buffer.getInt();
 					processLeaveLoop(w, t);
 					break;
 				}
 				case EventKind.NEW:
 				case EventKind.NEW_ARRAY:
 				{
 					EventFormat ef = scheme.getEvent(InstrScheme.NEW_AND_NEWARRAY);
 					int h = ef.hasLoc() ? buffer.getInt() : -1;
 					int t = ef.hasThr() ? buffer.getInt() : -1;
 					int o = ef.hasObj() ? buffer.getInt() : -1;
 					processNewOrNewArray(h, t, o);
 					break;
 				}
 				case EventKind.GETSTATIC_PRIMITIVE:
 				{
 					EventFormat ef = scheme.getEvent(InstrScheme.GETSTATIC_PRIMITIVE);
 					int e = ef.hasLoc() ? buffer.getInt() : -1;
 					int t = ef.hasThr() ? buffer.getInt() : -1;
 					int b = ef.hasBaseObj() ? buffer.getInt() : -1;
 					int f = ef.hasFld() ? buffer.getInt() : -1;
 					processGetstaticPrimitive(e, t, b, f);
 					break;
 				}
 				case EventKind.GETSTATIC_REFERENCE:
 				{
 					EventFormat ef = scheme.getEvent(InstrScheme.GETSTATIC_REFERENCE);
 					int e = ef.hasLoc() ? buffer.getInt() : -1;
 					int t = ef.hasThr() ? buffer.getInt() : -1;
 					int b = ef.hasBaseObj() ? buffer.getInt() : -1;
 					int f = ef.hasFld() ? buffer.getInt() : -1;
 					int o = ef.hasObj() ? buffer.getInt() : -1;
 					processGetstaticReference(e, t, b, f, o);
 					break;
 				}
 				case EventKind.PUTSTATIC_PRIMITIVE:
 				{
 					EventFormat ef = scheme.getEvent(InstrScheme.PUTSTATIC_PRIMITIVE);
 					int e = ef.hasLoc() ? buffer.getInt() : -1;
 					int t = ef.hasThr() ? buffer.getInt() : -1;
 					int b = ef.hasBaseObj() ? buffer.getInt() : -1;
 					int f = ef.hasFld() ? buffer.getInt() : -1;
 					processPutstaticPrimitive(e, t, b, f);
 					break;
 				}
 				case EventKind.PUTSTATIC_REFERENCE:
 				{
 					EventFormat ef = scheme.getEvent(InstrScheme.PUTSTATIC_REFERENCE);
 					int e = ef.hasLoc() ? buffer.getInt() : -1;
 					int t = ef.hasThr() ? buffer.getInt() : -1;
 					int b = ef.hasBaseObj() ? buffer.getInt() : -1;
 					int f = ef.hasFld() ? buffer.getInt() : -1;
 					int o = ef.hasObj() ? buffer.getInt() : -1;
 					processPutstaticReference(e, t, b, f, o);
 					break;
 				}
 				case EventKind.GETFIELD_PRIMITIVE:
 				{
 					EventFormat ef = scheme.getEvent(InstrScheme.GETFIELD_PRIMITIVE);
 					int e = ef.hasLoc() ? buffer.getInt() : -1;
 					int t = ef.hasThr() ? buffer.getInt() : -1;
 					int b = ef.hasBaseObj() ? buffer.getInt() : -1;
 					int f = ef.hasFld() ? buffer.getInt() : -1;
 					processGetfieldPrimitive(e, t, b, f);
 					break;
 				}
 				case EventKind.GETFIELD_REFERENCE:
 				{
 					EventFormat ef = scheme.getEvent(InstrScheme.GETFIELD_REFERENCE);
 					int e = ef.hasLoc() ? buffer.getInt() : -1;
 					int t = ef.hasThr() ? buffer.getInt() : -1;
 					int b = ef.hasBaseObj() ? buffer.getInt() : -1;
 					int f = ef.hasFld() ? buffer.getInt() : -1;
 					int o = ef.hasObj() ? buffer.getInt() : -1;
 					processGetfieldReference(e, t, b, f, o);
 					break;
 				}
 				case EventKind.PUTFIELD_PRIMITIVE:
 				{
 					EventFormat ef = scheme.getEvent(InstrScheme.PUTFIELD_PRIMITIVE);
 					int e = ef.hasLoc() ? buffer.getInt() : -1;
 					int t = ef.hasThr() ? buffer.getInt() : -1;
 					int b = ef.hasBaseObj() ? buffer.getInt() : -1;
 					int f = ef.hasFld() ? buffer.getInt() : -1;
 					processPutfieldPrimitive(e, t, b, f);
 					break;
 				}
 				case EventKind.PUTFIELD_REFERENCE:
 				{
 					EventFormat ef = scheme.getEvent(InstrScheme.PUTFIELD_REFERENCE);
 					int e = ef.hasLoc() ? buffer.getInt() : -1;
 					int t = ef.hasThr() ? buffer.getInt() : -1;
 					int b = ef.hasBaseObj() ? buffer.getInt() : -1;
 					int f = ef.hasFld() ? buffer.getInt() : -1;
 					int o = ef.hasObj() ? buffer.getInt() : -1;
 					processPutfieldReference(e, t, b, f, o);
 					break;
 				}
 				case EventKind.ALOAD_PRIMITIVE:
 				{
 					EventFormat ef = scheme.getEvent(InstrScheme.ALOAD_PRIMITIVE);
 					int e = ef.hasLoc() ? buffer.getInt() : -1;
 					int t = ef.hasThr() ? buffer.getInt() : -1;
 					int b = ef.hasBaseObj() ? buffer.getInt() : -1;
 					int i = ef.hasIdx() ? buffer.getInt() : -1;
 					processAloadPrimitive(e, t, b, i);
 					break;
 				}
 				case EventKind.ALOAD_REFERENCE:
 				{
 					EventFormat ef = scheme.getEvent(InstrScheme.ALOAD_REFERENCE);
 					int e = ef.hasLoc() ? buffer.getInt() : -1;
 					int t = ef.hasThr() ? buffer.getInt() : -1;
 					int b = ef.hasBaseObj() ? buffer.getInt() : -1;
 					int i = ef.hasIdx() ? buffer.getInt() : -1;
 					int o = ef.hasObj() ? buffer.getInt() : -1;
 					processAloadReference(e, t, b, i, o);
 					break;
 				}
 				case EventKind.ASTORE_PRIMITIVE:
 				{
 					EventFormat ef = scheme.getEvent(InstrScheme.ASTORE_PRIMITIVE);
 					int e = ef.hasLoc() ? buffer.getInt() : -1;
 					int t = ef.hasThr() ? buffer.getInt() : -1;
 					int b = ef.hasBaseObj() ? buffer.getInt() : -1;
 					int i = ef.hasIdx() ? buffer.getInt() : -1;
 					processAstorePrimitive(e, t, b, i);
 					break;
 				}
 				case EventKind.ASTORE_REFERENCE:
 				{
 					EventFormat ef = scheme.getEvent(InstrScheme.ASTORE_REFERENCE);
 					int e = ef.hasLoc() ? buffer.getInt() : -1;
 					int t = ef.hasThr() ? buffer.getInt() : -1;
 					int b = ef.hasBaseObj() ? buffer.getInt() : -1;
 					int i = ef.hasIdx() ? buffer.getInt() : -1;
 					int o = ef.hasObj() ? buffer.getInt() : -1;
 					processAstoreReference(e, t, b, i, o);
 					break;
 				}
 				case EventKind.THREAD_START:
 				{
 					EventFormat ef = scheme.getEvent(InstrScheme.THREAD_START);
 					int p = ef.hasLoc() ? buffer.getInt() : -1;
 					int t = ef.hasThr() ? buffer.getInt() : -1;
 					int o = ef.hasObj() ? buffer.getInt() : -1;
 					processThreadStart(p, t, o);
 					break;
 				}
 				case EventKind.THREAD_JOIN:
 				{
 					EventFormat ef = scheme.getEvent(InstrScheme.THREAD_JOIN);
 					int p = ef.hasLoc() ? buffer.getInt() : -1;
 					int t = ef.hasThr() ? buffer.getInt() : -1;
 					int o = ef.hasObj() ? buffer.getInt() : -1;
 					processThreadJoin(p, t, o);
 					break;
 				}
 				case EventKind.ACQUIRE_LOCK:
 				{
 					EventFormat ef = scheme.getEvent(InstrScheme.ACQUIRE_LOCK);
 					int p = ef.hasLoc() ? buffer.getInt() : -1;
 					int t = ef.hasThr() ? buffer.getInt() : -1;
 					int l = ef.hasObj() ? buffer.getInt() : -1;
 					processAcquireLock(p, t, l);
 					break;
 				}
 				case EventKind.RELEASE_LOCK:
 				{
 					EventFormat ef = scheme.getEvent(InstrScheme.RELEASE_LOCK);
 					int p = ef.hasLoc() ? buffer.getInt() : -1;
 					int t = ef.hasThr() ? buffer.getInt() : -1;
 					int l = ef.hasObj() ? buffer.getInt() : -1;
 					processReleaseLock(p, t, l);
 					break;
 				}
 				case EventKind.WAIT:
 				{
 					EventFormat ef = scheme.getEvent(InstrScheme.WAIT);
 					int p = ef.hasLoc() ? buffer.getInt() : -1;
 					int t = ef.hasThr() ? buffer.getInt() : -1;
 					int l = ef.hasObj() ? buffer.getInt() : -1;
 					processWait(p, t, l);
 					break;
 				}
 				case EventKind.NOTIFY:
 				{
 					EventFormat ef = scheme.getEvent(InstrScheme.NOTIFY);
 					int p = ef.hasLoc() ? buffer.getInt() : -1;
 					int t = ef.hasThr() ? buffer.getInt() : -1;
 					int l = ef.hasObj() ? buffer.getInt() : -1;
 					processNotify(p, t, l);
 					break;
 				}
 				case EventKind.METHOD_CALL_BEF:
 				{
 					EventFormat ef = scheme.getEvent(InstrScheme.METHOD_CALL);
 					int i = ef.hasLoc() ? buffer.getInt() : -1;
 					int t = ef.hasThr() ? buffer.getInt() : -1;
					int o = ef.hasThr() ? buffer.getInt() : -1;
 					processMethodCallBef(i, t, o);
 					break;
 				}
 				case EventKind.METHOD_CALL_AFT:
 				{
 					EventFormat ef = scheme.getEvent(InstrScheme.METHOD_CALL);
 					int i = ef.hasLoc() ? buffer.getInt() : -1;
 					int t = ef.hasThr() ? buffer.getInt() : -1;
					int o = ef.hasThr() ? buffer.getInt() : -1;
 					processMethodCallAft(i, t, o);
 					break;
 				}
 				case EventKind.RETURN_PRIMITIVE:
 				{
 					EventFormat ef = scheme.getEvent(InstrScheme.RETURN_PRIMITIVE);
 					int p = ef.hasLoc() ? buffer.getInt() : -1;
 					int t = ef.hasThr() ? buffer.getInt() : -1;
 					processReturnPrimitive(p, t);
 					break;
 				}
 				case EventKind.RETURN_REFERENCE:
 				{
 					EventFormat ef = scheme.getEvent(InstrScheme.RETURN_REFERENCE);
 					int p = ef.hasLoc() ? buffer.getInt() : -1;
 					int t = ef.hasThr() ? buffer.getInt() : -1;
 					int o = ef.hasObj() ? buffer.getInt() : -1;
 					processReturnReference(p, t, o);
 					break;
 				}
 				case EventKind.EXPLICIT_THROW:
 				{
 					EventFormat ef = scheme.getEvent(InstrScheme.EXPLICIT_THROW);
 					int p = ef.hasLoc() ? buffer.getInt() : -1;
 					int t = ef.hasThr() ? buffer.getInt() : -1;
 					int o = ef.hasObj() ? buffer.getInt() : -1;
 					processExplicitThrow(p, t, o);
 					break;
 				}
 				case EventKind.IMPLICIT_THROW:
 				{
 					EventFormat ef = scheme.getEvent(InstrScheme.IMPLICIT_THROW);
 					int p = ef.hasLoc() ? buffer.getInt() : -1;
 					int t = ef.hasThr() ? buffer.getInt() : -1;
 					int o = ef.hasObj() ? buffer.getInt() : -1;
 					processImplicitThrow(p, t, o);
 					break;
 				}
 				case EventKind.QUAD:
 				{
 					int q = buffer.getInt();
 					int t = buffer.getInt();
 					processQuad(q, t);
 					break;
 				}
 				case EventKind.BASIC_BLOCK:
 				{
 					int b = buffer.getInt();
 					int t = buffer.getInt();
 					processBasicBlock(b, t);
 					break;
 				}
 				default:
 					throw new RuntimeException("Unknown opcode: " + opcode);
 				}
 			}
 			donePass();
 			System.out.println("PROCESS TRACE: " + count);
 		} catch (IOException ex) {
 			throw new ChordRuntimeException(ex);
 		} catch (ReadException ex) {
 			throw new ChordRuntimeException(ex);
 		}
 	}
 	public void processEnterMethod(int m, int t) { }
 	public void processLeaveMethod(int m, int t) { }
 	public void processEnterLoop(int w, int t) { }
 	public void processLeaveLoop(int w, int t) { }
 	public void processNewOrNewArray(int h, int t, int o) { }
 	public void processGetstaticPrimitive(int e, int t, int b, int f) { }
 	public void processGetstaticReference(int e, int t, int b, int f, int o) { }
 	public void processPutstaticPrimitive(int e, int t, int b, int f) { }
 	public void processPutstaticReference(int e, int t, int b, int f, int o) { }
 	public void processGetfieldPrimitive(int e, int t, int b, int f) { }
 	public void processGetfieldReference(int e, int t, int b, int f, int o) { }
 	public void processPutfieldPrimitive(int e, int t, int b, int f) { }
 	public void processPutfieldReference(int e, int t, int b, int f, int o) { }
 	public void processAloadPrimitive(int e, int t, int b, int i) { }
 	public void processAloadReference(int e, int t, int b, int i, int o) { }
 	public void processAstorePrimitive(int e, int t, int b, int i) { }
 	public void processAstoreReference(int e, int t, int b, int i, int o) { }
 	public void processThreadStart(int i, int t, int o) { }
 	public void processThreadJoin(int i, int t, int o) { }
 	public void processAcquireLock(int l, int t, int o) { }
 	public void processReleaseLock(int r, int t, int o) { }
 	public void processWait(int i, int t, int o) { }
 	public void processNotify(int i, int t, int o) { }
 	public void processMethodCallBef(int i, int t, int o) { }
 	public void processMethodCallAft(int i, int t, int o) { }
 	public void processReturnPrimitive(int p, int t) { }
 	public void processReturnReference(int p, int t, int o) { }
 	public void processExplicitThrow(int p, int t, int o) { }
 	public void processImplicitThrow(int p, int t, int o) { }
 	public void processQuad(int p, int t) { }
 	public void processBasicBlock(int b, int t) { }
 }

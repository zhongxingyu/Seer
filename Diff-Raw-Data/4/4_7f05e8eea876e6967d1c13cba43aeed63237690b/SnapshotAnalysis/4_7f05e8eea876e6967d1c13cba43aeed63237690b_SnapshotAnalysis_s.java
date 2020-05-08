 package chord.analyses.snapshot;
 
 import gnu.trove.TIntHashSet;
 import gnu.trove.TIntIntHashMap;
 import gnu.trove.TIntObjectHashMap;
 import gnu.trove.TIntProcedure;
 
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 import java.util.Stack;
 
 import joeq.Class.jq_Class;
 import joeq.Compiler.Quad.Quad;
 import chord.instr.InstrScheme;
 import chord.program.Program;
 import chord.project.Properties;
 import chord.project.analyses.DynamicAnalysis;
 
 /**
  * Evaluate the precision and complexity of various heap abstractions.
  * Maintains a graph over the concrete heap.
  * Abstractions are functions that operate on snapshots of the concrete heap.
  * Client will override this class and provide queries on the abstractions.
  *
  * @author Percy Liang (pliang@cs.berkeley.edu)
  */
 public abstract class SnapshotAnalysis extends DynamicAnalysis {
   public abstract String propertyName();
 
   static final int ARRAY_FIELD = 99999;
   static final int NULL_OBJECT = 0;
 
   InstrScheme instrScheme;
 
   // Execution management/logging
   Execution X;
 
   // Parameters of the analysis (updates and abstraction)
   int verbose;
   boolean useStrongUpdates;
   Abstraction abstraction;
   double queryFrac, hitFrac, snapshotFrac;
   Random selectQueryRandom;
   Random selectHitRandom;
   Random selectSnapshotRandom;
   int kCFA; // Number of call sites keep in k-CFA
   int kOS; // Number of object allocation sites to keep in k-OS (note: this is not k-object sensitivity)
   ReachabilityAbstraction.Spec reachabilitySpec = new ReachabilityAbstraction.Spec();
   GraphMonitor graphMonitor;
   boolean queryOnlyAtSnapshot; // For efficiency (but incorrect)
 
   // We have a graph over abstract values (determined by updateAbstraction); each node impliciting representing a set of objects
   State state = new State();
   TIntObjectHashMap<ThreadInfo> threadInfos = new TIntObjectHashMap<ThreadInfo>(); // thread t -> ThreadInfo
   Set<jq_Class> excludedClasses = new HashSet<jq_Class>();
 
   HashMap<Query, QueryResult> queryResults = new HashMap<Query, QueryResult>();
   int numQueryHits;
   StatFig snapshotPrecision = new StatFig();
 
   public Abstraction parseAbstraction(String abstractionType) {
     if (abstractionType.equals("none")) return new NoneAbstraction();
     if (abstractionType.equals("alloc")) return new AllocAbstraction(kCFA, kOS);
     if (abstractionType.equals("recency")) return new RecencyAbstraction();
     if (abstractionType.equals("reachability")) return new ReachabilityAbstraction(reachabilitySpec);
     throw new RuntimeException("Unknown abstraction: "+abstractionType+" (possibilities: none|alloc|recency|reachability)");
   }
 
   public String getStringArg(String key, String defaultValue) {
     return System.getProperty("chord.partition."+key, defaultValue);
   }
   public boolean getBooleanArg(String key, boolean defaultValue) {
     String s = getStringArg(key, null);
     return s == null ? defaultValue : s.equals("true");
   }
   public int getIntArg(String key, int defaultValue) {
     String s = getStringArg(key, null);
     return s == null ? defaultValue : Integer.parseInt(s);
   }
   public double getDoubleArg(String key, double defaultValue) {
     String s = getStringArg(key, null);
     return s == null ? defaultValue : Double.parseDouble(s);
   }
 
 	public void run() {
     X = new Execution();
     boolean success = false;
     try {
       // Parse options
       verbose = getIntArg("verbose", 0);
 
       queryFrac = getDoubleArg("queryFrac", 1.0);
       hitFrac = getDoubleArg("hitFrac", 1.0);
       snapshotFrac = getDoubleArg("snapshotFrac", 0.0);
       selectQueryRandom = new Random(getIntArg("selectQueryRandom", 1));
       selectHitRandom = new Random(getIntArg("selectHitRandom", 1));
       selectSnapshotRandom = new Random(getIntArg("selectSnapshotRandom", 1));
 
       kCFA = getIntArg("kCFA", 0);
       kOS = getIntArg("kOS", 0);
       reachabilitySpec.pointedTo = getBooleanArg("pointedTo", false);
       reachabilitySpec.matchRepeatedFields = getBooleanArg("matchRepeatedFields", false);
       reachabilitySpec.matchFirstField = getBooleanArg("matchFirstField", false);
       reachabilitySpec.matchLastField = getBooleanArg("matchLastField", false);
       queryOnlyAtSnapshot = getBooleanArg("queryOnlyAtSnapshot", false);
 
       useStrongUpdates = getBooleanArg("useStrongUpdates", false);
       abstraction = parseAbstraction(getStringArg("abstraction", ""));
 
       graphMonitor = new SerializingGraphMonitor(X.path("graph"), getIntArg("graph.maxCommands", 100000));
 
       // Save options
       HashMap<Object,Object> options = new LinkedHashMap<Object,Object>();
       options.put("program", System.getProperty("chord.work.dir"));
       options.put("property", propertyName());
       options.put("verbose", verbose);
       options.put("useStrongUpdates", useStrongUpdates);
       options.put("abstraction", abstraction);
       options.put("queryFrac", queryFrac);
       options.put("hitFrac", hitFrac);
       options.put("snapshotFrac", snapshotFrac);
       options.put("exclude", getStringArg("exclude", ""));
       options.put("queryOnlyAtSnapshot", queryOnlyAtSnapshot);
       X.writeMap("options.map", options);
       X.output.put("exec.status", "running");
 
       super.run();
 
       // Save output
       X.output.put("exec.status", "done");
       success = true;
     } catch (Throwable t) {
       X.output.put("exec.status", "failed");
       X.errors("%s", t);
       for (StackTraceElement e : t.getStackTrace())
         X.logs("  %s", e);
     }
     X.finish();
     if (!success) System.exit(1);
 	}
 
   public void computedExcludedClasses() {
     String[] checkExcludedPrefixes = Properties.toArray(Properties.checkExcludeStr);
     Program program = Program.v();
     for (jq_Class c : program.getPreparedClasses()) {
       String cName = c.getName();
       for (String prefix : checkExcludedPrefixes) {
         if (cName.startsWith(prefix))
           excludedClasses.add(c);
       }
     }
   }
   public boolean isExcluded(int e) {
     if (e < 0) return true;
     Quad q = (Quad)instrumentor.getDomE().get(e);
     jq_Class c = Program.v().getMethod(q).getDeclaringClass();
     //X.logs("CHECK e = %s, q = %s, class = %s", estr(e), q, c);
     return excludedClasses.contains(c);
   }
 
   public ThreadInfo threadInfo(int t) {
     if (t == -1) return null;
     ThreadInfo info = threadInfos.get(t);
     if (info == null)
       threadInfos.put(t, info = new ThreadInfo());
     return info;
   }
 
   public InstrScheme getInstrScheme() {
     if (instrScheme != null) return instrScheme;
     instrScheme = getBaselineScheme();
 
//    instrScheme.setEnterAndLeaveMethodEvent();
     //instrScheme.setEnterAndLeaveLoopEvent();
 
     instrScheme.setNewAndNewArrayEvent(true, true, true); // h, t, o
 
 //	  instrScheme.setGetstaticPrimitiveEvent(true, true, true, true); // e, t, b, f
 //	  instrScheme.setGetstaticReferenceEvent(true, true, true, true, true); // e, t, b, f, o
 //	  instrScheme.setPutstaticPrimitiveEvent(true, true, true, true); // e, t, b, f
     instrScheme.setPutstaticReferenceEvent(true, true, true, true, true); // e, t, b, f, o
 
     instrScheme.setGetfieldPrimitiveEvent(true, true, true, true); // e, t, b, f
     instrScheme.setPutfieldPrimitiveEvent(true, true, true, true); // e, t, b, f
     instrScheme.setGetfieldReferenceEvent(true, true, true, true, true); // e, t, b, f, o
     instrScheme.setPutfieldReferenceEvent(true, true, true, true, true); // e, t, b, f, o
 
     instrScheme.setAloadPrimitiveEvent(true, true, true, isArrayIndexSensitive()); // e, t, b, i
     instrScheme.setAstorePrimitiveEvent(true, true, true, isArrayIndexSensitive()); // e, t, b, i
     instrScheme.setAloadReferenceEvent(true, true, true, isArrayIndexSensitive(), true); // e, t, b, i, o
     instrScheme.setAstoreReferenceEvent(true, true, true, isArrayIndexSensitive(), true); // e, t, b, i, o
 
 //    instrScheme.setThreadStartEvent(true, true, true); // i, t, o
     //instrScheme.setThreadJoinEvent(true, true, true); // i, t, o
 
 //    instrScheme.setAcquireLockEvent(true, true, true); // l, t, o
     //instrScheme.setReleaseLockEvent(true, true, true); // r, t, o
     //instrScheme.setWaitEvent(true, true, true); // i, t, o
     //instrScheme.setNotifyEvent(true, true, true); // i, t, o
 
     instrScheme.setMethodCallEvent(true, true, true, true, true); // i, t, o, before, after
 
     instrScheme.setFinalizeEvent();
 
     //instrScheme.setReturnPrimitiveEvent(true, true); // i, t
     //instrScheme.setReturnReferenceEvent(true, true, true); // i, t, o
 
     //instrScheme.setExplicitThrowEvent(true, true, true); // p, t, o
     //instrScheme.setImplicitThrowEvent(true, true); // p, t
 
     //instrScheme.setQuadEvent();
     //instrScheme.setBasicBlockEvent();
 
     return instrScheme;
   }
 
   protected boolean isArrayIndexSensitive() {
 	  return false;
   }
 
 protected InstrScheme getBaselineScheme() {
 	return new InstrScheme();
 }
 
 public boolean shouldAnswerQueryHit(Query query) {
     if (selectHitRandom.nextDouble() < hitFrac)
       return queryResult(query).selected;
     return false;
   }
   public void answerQuery(Query query, boolean isTrue) {
     QueryResult result = queryResult(query);
     result.add(isTrue);
     numQueryHits++;
     if (verbose >= 1) X.logs("QUERY %s: result = %s", query, result);
   }
   private QueryResult queryResult(Query q) {
     QueryResult qr = queryResults.get(q);
     if (qr == null) {
       queryResults.put(q, qr = new QueryResult());
       qr.selected = decideIfSelected();
     }
     return qr;
   }
   protected boolean decideIfSelected() {
 	  return selectQueryRandom.nextDouble() < queryFrac; // Choose to answer this query with some probability
   }
 
   public void outputQueries() {
     PrintWriter out = Utils.openOut(X.path("queries.out"));
     StatFig fig = new StatFig();
     for (Query q : queryResults.keySet()) {
       QueryResult qr = queryResults.get(q);
       if (qr.selected) {
         out.println(String.format("%s | %s %s", q, qr.numTrue, qr.numFalse));
         fig.add(qr.numTrue + qr.numFalse);
       }
       else
         out.println(String.format("%s | ?", q));
     }
     out.close();
     X.output.put("query.numHits", fig.mean());
     X.logs("  # hits per query: %s (%s total hits)", fig, fig.n);
   }
 
   public void initAllPasses() {
     int E = instrumentor.getEmap().size();
     int H = instrumentor.getHmap().size();
     int F = instrumentor.getFmap().size();
     computedExcludedClasses();
     X.logs("initAllPasses: |E| = %s, |H| = %s, |F| = %s, excluding %s classes", E, H, F, excludedClasses.size());
     abstraction.X = X;
     abstraction.state = state;
   }
 
   public void doneAllPasses() {
     // Evaluate on queries (real metric)
     int numTrue = 0;
     int numSelected = 0;
     for (QueryResult qr : queryResults.values()) {
       if (!qr.selected) continue;
       if (qr.isTrue()) numTrue++;
       numSelected++;
     }
 
     X.logs("  %d total queries; %d/%d = %.2f queries proposed to have property %s",
       queryResults.size(), numTrue, numSelected, 1.0*numTrue/numSelected, propertyName());
     X.output.put("query.totalNumHits", numQueryHits);
     X.output.put("query.numTrue", numTrue);
     X.output.put("query.numSelected", numSelected);
     X.output.put("query.numTotal", queryResults.size());
     X.output.put("query.fracTrue", 1.0*numTrue/numSelected);
     outputQueries();
 
     X.logs("Aggregated snapshot precision: %s", snapshotPrecision);
     X.output.put("snapshot.avgPrecision", snapshotPrecision.mean());
     X.output.put("snapshot.num", snapshotPrecision.n);
     X.output.put("finalObjects.numTotal", state.o2h.size());
 
     if (graphMonitor != null) graphMonitor.finish();
   }
 
   //////////////////////////////
   // Override these graph construction handlers (remember to call super though)
 
   public void nodeCreated(int t, int o) {
     if (o < 0) return; // Ignore bad nodes
     if (o == NULL_OBJECT) return;
     if (state.o2edges.containsKey(o)) return; // Already exists
     state.o2h.putIfAbsent(o, -1); // Just in case we didn't get an allocation site
     state.o2edges.put(o, new ArrayList<Edge>());
     ThreadInfo info = threadInfo(t);
     abstraction.nodeCreated(info, o);
     if (graphMonitor != null) graphMonitor.addNode(o, null);
   }
   public void nodeDeleted(int o) {
     // TODO
     //abstraction.nodeDeleted(o);
     //state.o2h.remove(o);
     //state.o2edges.remove(o);
   }
 
   public void edgeCreated(int t, int b, int f, int o) {
     nodeCreated(t, b);
     nodeCreated(t, o);
     assert (b > 0);
 
     // Strong update: remove existing field pointer
     List<Edge> edges = state.o2edges.get(b);
     if (useStrongUpdates) {
       for (int i = 0; i < edges.size(); i++) {
         if (edges.get(i).f == f) {
           int old_o = edges.get(i).o;
           abstraction.edgeDeleted(b, f, old_o);
           if (graphMonitor != null) graphMonitor.deleteEdge(b, old_o);
           edges.remove(i);
           break;
         }
       }
     }
 
     if (o > 0) {
       edges.add(new Edge(f, o));
       abstraction.edgeCreated(b, f, o);
       if (graphMonitor != null) graphMonitor.addEdge(o, b, ""+f);
     }
     if (verbose >= 1)
       X.logs("ADDEDGE b=%s f=%s o=%s", ostr(b), fstr(f), ostr(o));
   }
 
   // Typically, this function is the source of queries
   public void fieldAccessed(int e, int t, int b, int f, int o) {
     nodeCreated(t, b);
     nodeCreated(t, o);
     if (selectSnapshotRandom.nextDouble() < snapshotFrac) 
       doSnapshotAnalysis();
   }
 
   private void doSnapshotAnalysis() {
     // Compute abstraction
     abstraction.ensureComputed();
 
     int complexity = abstraction.a2os.size(); // Complexity of this abstraction (number of abstract values)
     PrintWriter out = Utils.openOut(X.path("snapshot-abstractions"));
     for (Object a : abstraction.a2os.keySet())
       out.println(a);
     out.close();
 
     final SnapshotResult result = takeSnapshot();
     if (result != null && result.proposedNumTrue() > 0)
       snapshotPrecision.add(result.precision());
 
     if (result != null && result instanceof NodeBasedSnapshotResult) annotateGraph((NodeBasedSnapshotResult)result);
 
     X.logs("Snapshot %d", snapshotPrecision.n);
     X.logs("  complexity: %d values", complexity);
     X.output.put("complexity", complexity);
     if (result != null) {
       X.logs("  precision: %d/%d = %.2f", result.actualNumTrue(), result.proposedNumTrue(), result.precision());
       X.output.put("snapshot.actualNumTrue", result.actualNumTrue());
       X.output.put("snapshot.proposedNumTrue", result.proposedNumTrue());
       X.output.put("snapshot.precision", result.precision());
     }
   }
 
   private void annotateGraph(final NodeBasedSnapshotResult result) {
     if (graphMonitor == null) return;
     // Color the nodes for visualization
     state.o2h.forEachKey(new TIntProcedure() { public boolean execute(int o) { 
       boolean actualTrue = result.actualTrueNodes.contains(o);
       boolean proposedTrue = result.proposedTrueNodes.contains(o);
       String color = null;
       if (actualTrue && proposedTrue) color = "#00ff00"; // Good
       else if (!actualTrue && proposedTrue) color = "#ff0000"; // Bad (false positive)
       else if (!actualTrue && !proposedTrue) color = "#ffffff"; // Good
       else throw new RuntimeException("Got true negative - shouldn't happen (snapshot analysis is broken)");
       String label = abstraction.getValue(o)+"";
       graphMonitor.setNodeLabel(o, label);
       graphMonitor.setNodeColor(o, color);
       return true;
     } });
   }
 
   // Evaluate the abstraction in time (doesn't have to be the same evalutaion metric as the queries)
   public abstract SnapshotResult takeSnapshot();
 
   abstract class SnapshotResult {
     public abstract int actualNumTrue();
     public abstract int proposedNumTrue();
     public double precision() { return 1.0 * actualNumTrue() / proposedNumTrue(); }
   }
 
   // When precision can be computed as a function of nodes in the graph
   class NodeBasedSnapshotResult extends SnapshotResult {
     // Fill these up
     TIntHashSet actualTrueNodes = new TIntHashSet();
     TIntHashSet proposedTrueNodes = new TIntHashSet();
     @Override public int actualNumTrue() { return actualTrueNodes.size(); }
     @Override public int proposedNumTrue() { return proposedTrueNodes.size(); }
   }
 
   //////////////////////////////
   // Pretty-printing
 
   public String fstr(int f) { // field
     if (f == ARRAY_FIELD) return "[*]";
     return f < 0 ? "-" : instrumentor.getFmap().get(f);
   }
   public String hstr(int h) { return h < 0 ? "-" : instrumentor.getHmap().get(h); } // heap allocation site
   public String estr(int e) {
     if (e < 0) return "-";
     Quad quad = (Quad)instrumentor.getDomE().get(e);
     return Program.v().toJavaPosStr(quad)+" "+Program.v().toQuadStr(quad);
   }
   public String mstr(int m) { return m < 0 ? "-" : instrumentor.getMmap().get(m); } // method
   public String wstr(int w) { return w < 0 ? "-" : instrumentor.getWmap().get(w); } // loop
   public String istr(int i) { return i < 0 ? "-" : instrumentor.getImap().get(i); } // call site
   public String ostr(int o) { return o < 0 ? "-" : (o == NULL_OBJECT ? "null" : "O"+o); } // concrete object
   public String tstr(int t) { return t < 0 ? "-" : "T"+t; } // thread
   public String pstr(int p) { return p < 0 ? "-" : instrumentor.getPmap().get(p); } // simple statement?
 
   ////////////////////////////////////////////////////////////
   // Handlers
 
 	@Override
 	public void processNewOrNewArray(int h, int t, int o) {
 		// new Object
 		state.o2h.put(o, h);
 		nodeCreated(t, o);
 		if (verbose >= 5)
 			X.logs("EVENT new: h=%s, t=%s, o=%s", hstr(h), tstr(t), ostr(o));
 	}
 
 	@Override
 	public void processPutstaticReference(int e, int t, int b, int f, int o) {
 		// b.f = o, where b is static
 		if (verbose >= 5)
 			X.logs("EVENT putStaticReference: e=%s, t=%s, b=%s, f=%s, o=%s",
 					estr(e), tstr(t), ostr(b), fstr(f), ostr(o));
 		edgeCreated(t, b, f, o);
 	}
 
 	@Override
 	public void processGetfieldPrimitive(int e, int t, int b, int f) {
 		if (!isExcluded(e))
 			fieldAccessed(e, t, b, f, -1);
 	}
 
 	@Override
 	public void processGetfieldReference(int e, int t, int b, int f, int o) {
 		// ... = b.f, where b.f = o
 		if (verbose >= 5)
 			X.logs("EVENT getFieldReference: e=%s, t=%s, b=%s, f=%s, o=%s",
 					estr(e), tstr(t), ostr(b), fstr(f), ostr(o));
 		if (!isExcluded(e))
 			fieldAccessed(e, t, b, f, o);
 	}
 
 	@Override
 	public void processPutfieldPrimitive(int e, int t, int b, int f) {
 		if (!isExcluded(e))
 			fieldAccessed(e, t, b, f, -1);
 	}
 
 	@Override
 	public void processPutfieldReference(int e, int t, int b, int f, int o) {
 		// b.f = o
 		if (verbose >= 5)
 			X.logs("EVENT putFieldReference: e=%s, t=%s, b=%s, f=%s, o=%s",
 					estr(e), tstr(t), ostr(b), fstr(f), ostr(o));
 		if (!isExcluded(e))
 			fieldAccessed(e, t, b, f, o);
 		edgeCreated(t, b, f, o);
 	}
 
 	@Override
 	public void processAloadPrimitive(int e, int t, int b, int i) {
 		if (!isExcluded(e))
 			fieldAccessed(e, t, b, ARRAY_FIELD, -1);
 	}
 
 	@Override
 	public void processAloadReference(int e, int t, int b, int i, int o) {
 		if (verbose >= 5)
 			X.logs("EVENT loadReference: e=%s, t=%s, b=%s, i=%s, o=%s",
 					estr(e), tstr(t), ostr(b), i, ostr(o));
 		if (!isExcluded(e))
 			fieldAccessed(e, t, b, ARRAY_FIELD, o);
 	}
 
 	@Override
 	public void processAstorePrimitive(int e, int t, int b, int i) {
 		if (!isExcluded(e))
 			fieldAccessed(e, t, b, ARRAY_FIELD, -1);
 	}
 
 	@Override
 	public void processAstoreReference(int e, int t, int b, int i, int o) {
 		if (verbose >= 5)
 			X.logs("EVENT storeReference: e=%s, t=%s, b=%s, i=%s, o=%s",
 					estr(e), tstr(t), ostr(b), i, ostr(o));
 		if (!isExcluded(e))
 			fieldAccessed(e, t, b, ARRAY_FIELD, o);
 		edgeCreated(t, b, ARRAY_FIELD, o);
 	}
   
 	@Override
 	public void processMethodCallBef(int i, int t, int o) {
 		if (verbose >= 5)
 			X.logs("EVENT methodCallBefore: i=%s, t=%s, o=%s", istr(i),
 					tstr(t), ostr(o));
 		ThreadInfo info = threadInfo(t);
 		info.callSites.push(i);
 		info.callAllocs.push(state.o2h.get(o));
 	}
   
 	@Override
 	public void processMethodCallAft(int i, int t, int o) {
 		ThreadInfo info = threadInfo(t);
 		if (verbose >= 5)
 			X.logs("EVENT methodCallAfter: i=%s, t=%s, o=%s", istr(i), tstr(t),
 					ostr(o));
 		if (info.callSites.size() == 0)
 			X.errors("Tried to pop empty callSites stack");
 		else {
 			int ii = info.callSites.pop();
 			if (ii != i)
 				X.errors("pushed %s but popped %s", istr(i), istr(ii));
 		}
 		if (info.callAllocs.size() == 0)
 			X.errors("Tried to pop empty callAllocs stack");
 		else {
 			int hh = info.callAllocs.pop();
 			int h = state.o2h.get(o);
 			if (hh != h)
 				X.errors("pushed %s but popped %s", hstr(h), hstr(hh));
 		}
 	}
 	  
 	@Override
 	public void processFinalize(int o) {
 		if (verbose >= 7)
 			X.logs("EVENT processFinalize o=%s", ostr(o));
 		nodeDeleted(o);
 	}
 
 	 
 	@Override
 	public void processGetstaticReference(int e, int t, int b, int f, int o) { 
 		// ...=b.f, where b.f=o and b is static
 		if (verbose >= 5)
 			X.logs("EVENT getStaticReference: e=%s, t=%s, b=%s, f=%s, o=%s",
 					estr(e), tstr(t), ostr(b), fstr(f), ostr(o));
 	}
 	
 	@Override
 	public void processThreadStart(int i, int t, int o) {
 		if (verbose >= 4)
 			X.logs("EVENT threadStart: i=%s, t=%s, o=%s", istr(i), tstr(t),
 					ostr(o));
 	}
 
 	@Override
 	public void processThreadJoin(int i, int t, int o) {
 		if (verbose >= 4)
 			X.logs("EVENT threadJoin: i=%s, t=%s, o=%s", istr(i), tstr(t),
 					ostr(o));
 	}
 
 	@Override
 	public void processEnterMethod(int m, int t) {
 		if (verbose >= 6)
 			X.logs("EVENT enterMethod: m=%s, t=%s", mstr(m), tstr(t));
 	}
 
 	@Override
 	public void processLeaveMethod(int m, int t) {
 		if (verbose >= 6)
 			X.logs("EVENT leaveMethod: m=%s, t=%s", mstr(m), tstr(t));
 	}
 
 	@Override
 	public void processEnterLoop(int w, int t) {
 		if (verbose >= 5)
 			X.logs("EVENT enterLoop: w=%s", w);
 	}
 
 	@Override
 	public void processLeaveLoop(int w, int t) {
 		if (verbose >= 5)
 			X.logs("EVENT leaveLoop: w=%s", w);
 	}
 
 	@Override
 	public void processQuad(int p, int t) {
 		if (verbose >= 7)
 			X.logs("EVENT processQuad p=%s, t=%s", pstr(p), tstr(t));
 	}
 
 	@Override
 	public void processPutstaticPrimitive(int e, int t, int b, int f) {
 	}
 
 	@Override
 	public void processGetstaticPrimitive(int e, int t, int b, int f) {
 	}
 	
 	@Override
 	public void processBasicBlock(int b, int t) {
 	}
 	  
 	@Override
 	public void processAcquireLock(int l, int t, int o) {
 	}
 
 	@Override
 	public void processReleaseLock(int r, int t, int o) {
 	}
 
 	@Override
 	public void processWait(int i, int t, int o) {
 	}
 
 	@Override
 	public void processNotify(int i, int t, int o) {
 	}
 
 	@Override
 	public void processReturnPrimitive(int p, int t) {
 	}
 
 	@Override
 	public void processReturnReference(int p, int t, int o) {
 	}
 
 	@Override
 	public void processExplicitThrow(int p, int t, int o) {
 	}
 
 	@Override
 	public void processImplicitThrow(int p, int t, int o) {
 	}
 
   // Query for thread escape: is the object pointed to by the relvant variable thread-escaping at program point e?
   class ProgramPointQuery extends Query {
     public ProgramPointQuery(int e) { this.e = e; }
     public int e; // Program point
 
     @Override public boolean equals(Object _that) {
       if (_that instanceof ProgramPointQuery) {
         ProgramPointQuery that = (ProgramPointQuery)_that;
         return this.e == that.e;
       }
       return false;
     }
     @Override public int hashCode() { return e; }
     @Override public String toString() { return estr(e); }
   }
 }
 
 ////////////////////////////////////////////////////////////
 
 // Pointer via field f to object o
 class Edge {
   public Edge(int f, int o) {
     this.f = f;
     this.o = o;
   }
   public int f;
   public int o;
 }
 
 abstract class Query {
 }
 
 class QueryResult {
   public boolean selected; // Whether we are trying to answer this query or not
   public int numTrue = 0;
   public int numFalse = 0;
 
   public boolean isTrue() { return numTrue > 0; } // Existential property
   public void add(boolean b) {
     if (b) numTrue++;
     else numFalse++;
   }
 
   @Override public String toString() { return numTrue+"|"+numFalse; }
 }
 
 class ThreadInfo {
   public Stack<Integer> callStack = new Stack(); // Elements are methods m (for visualization)
   public Stack<Integer> callSites = new Stack(); // Elements are call sites i (for kCFA)
   public Stack<Integer> callAllocs = new Stack(); // Elements are object allocation sites h (for kOS)
 }
 
 class State {
   TIntIntHashMap v2o = new TIntIntHashMap(); // variable v -> object o
   TIntIntHashMap o2h = new TIntIntHashMap(); // object o -> heap allocation site h
   TIntObjectHashMap<List<Edge>> o2edges = new TIntObjectHashMap<List<Edge>>(); // object o -> list of outgoing edges from o
 }

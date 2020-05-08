 package chord.analyses.escape.dynamic;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.FileWriter;
 import java.io.File;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 import java.util.HashSet;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Random;
 import java.util.Collections;
 
 import joeq.Compiler.Quad.Quad;
 import chord.doms.DomT;
 
 import chord.util.IntArraySet;
 import chord.util.ChordRuntimeException;
 import chord.project.Properties;
 import chord.util.IndexMap;
 import chord.util.IndexHashMap;
 import chord.instr.InstrScheme;
 import chord.project.Chord;
 import chord.project.analyses.DynamicAnalysis;
 import chord.project.analyses.ProgramRel;
 import chord.project.Project;
 import chord.program.Program;
 
 import gnu.trove.TIntHashSet;
 import gnu.trove.TIntIntHashMap;
 import gnu.trove.TLongIntHashMap;
 import gnu.trove.TIntObjectHashMap;
 import gnu.trove.TIntArrayList;
 //import org.ubiety.ubigraph.UbigraphClient;
 
 /**
  * Evaluate the precision and complexity of various heap abstractions.
  *
  * Two types of abstraction:
  *  - Update abstraction (used to do weak updates):
  *    Build the graph over abstract values obtained from this abstraction.
  *    Supports allocation sites.
  *  - Snapshot abstraction (used to do strong updates):
  *    Build the graph over concrete values (note that this enables us to do very strong updates).
  *    We then apply the abstraction at various snapshots of the graph to answer queries (very inefficient).
  *    Supports abstractions above plus reachability.
  *
  * Notation:
  *   a: node in a graph
  *   v: abstract value from update abstraction
  *   w: abstract value from snapshot abstraction
  *
  * TODO: exclude queries that come from DomT
  * TODO: make this all work on the IBM JDK without excluding java.*
  *
  * @author Percy Liang (pliang@cs.berkeley.edu)
  */
 @Chord(name = "partition-java")
 public class PartitionAnalysis extends DynamicAnalysis {
   static final int ARRAY_FIELD = 88888;
   static final int THREAD_FIELD = 99999;
   static final int NULL_OBJECT = 0;
   static final int THREAD_GLOBAL_OBJECT = 100000;
 
   /*static Execution X = new Execution();
   static {
     System.out.println("PartitionAnalysis");
     Properties.outDirName = X.path(null);
     System.setProperty("chord.out.dir", X.path(null));
   }*/
 
   InstrScheme instrScheme;
   DomT domT; // Classes (to figure out which objects to exclude)
 
   // Execution management/logging
   Execution X = new Execution();
 
   // Parameters of the analysis
   PropertyState propertyState;
   int verbose;
   boolean useStrongUpdates;
   LocalAbstraction updateAbstraction;
   GlobalAbstraction snapshotAbstraction;
   double queryFrac, snapshotFrac;
   Random selectQueryRandom;
   Random selectSnapshotRandom;
   int kCFA; // Number of call sites keep in k-CFA
   int kOS; // Number of object allocation sites to keep in k-OS
   ReachabilityGlobalAbstraction.Spec reachabilitySpec = new ReachabilityGlobalAbstraction.Spec();
   GraphMonitor graphMonitor;
 
   // We have a graph over abstract values (determined by updateAbstraction); each node impliciting representing a set of objects
   TIntIntHashMap o2h = new TIntIntHashMap(); // o (object ID) -> heap allocation site h
   TIntObjectHashMap<Object> o2v = new TIntObjectHashMap<Object>(); // o (object ID) -> abstract value v
   IndexMap<Object> a2v = new IndexHashMap<Object>(); // a (node ID) -> abstract value v (along with reverse map)
   List<List<Edge>> a2edges = new ArrayList<List<Edge>>(); // a (node ID) -> list of outgoing edges from a
   int N; // Number of nodes
 
   TIntObjectHashMap<ThreadInfo> threadInfos = new TIntObjectHashMap<ThreadInfo>(); // t (thread) -> ThreadInfo
 
   // For each query, we maintain counts (number of escaping and non-escaping over the run)
   HashMap<Query, QueryResult> queryResults = new HashMap<Query, QueryResult>();
   int numQueryHits;
   StatFig snapshotPrecision = new StatFig();
 
   LocalAbstraction parseLocalAbstraction(String abstractionType) {
     if (abstractionType.equals("none")) return new NoneLocalAbstraction();
     if (abstractionType.equals("alloc")) return new AllocLocalAbstraction(kCFA, kOS, o2h);
     throw new RuntimeException("Unknown: "+abstractionType+" (possibilities: none|alloc)");
   }
   GlobalAbstraction parseGlobalAbstraction(String abstractionType) {
     if (abstractionType.equals("none")) return new NoneGlobalAbstraction();
     if (abstractionType.startsWith("wrap:")) return new WrappedGlobalAbstraction(parseLocalAbstraction(abstractionType.substring(5)));
     if (abstractionType.equals("recency")) return new RecencyGlobalAbstraction();
     if (abstractionType.equals("reachability")) return new ReachabilityGlobalAbstraction(reachabilitySpec);
     throw new RuntimeException("Unknown: "+abstractionType+" (possibilities: wrap:<local abstraction>|recency|reachability)");
   }
   PropertyState parsePropertyState(String s) {
     //if (s.equals("may-alias")) return new MayAliasPropertyState();
     if (s.equals("thread-escape")) return new ThreadEscapePropertyState();
     //if (s.equals("cyclicity")) return new CyclicityPropertyState();
     throw new RuntimeException("Unknown: "+s+" (possibilities: may-alias|thread-escape|cyclicity)");
   }
 
   String getStringArg(String key, String defaultValue) {
     return System.getProperty("chord.partition."+key, defaultValue);
   }
   boolean getBooleanArg(String key, boolean defaultValue) {
     String s = getStringArg(key, null);
     return s == null ? defaultValue : s.equals("true");
   }
   int getIntArg(String key, int defaultValue) {
     String s = getStringArg(key, null);
     return s == null ? defaultValue : Integer.parseInt(s);
   }
   double getDoubleArg(String key, double defaultValue) {
     String s = getStringArg(key, null);
     return s == null ? defaultValue : Double.parseDouble(s);
   }
 
 	public void run() {
     try {
       // Parse options
       verbose = getIntArg("verbose", 0);
       propertyState = parsePropertyState(getStringArg("property", ""));
 
       queryFrac = getDoubleArg("queryFrac", 1.0);
       snapshotFrac = getDoubleArg("snapshotFrac", 0.0);
       selectQueryRandom = new Random(getIntArg("selectQueryRandom", 1));
       selectSnapshotRandom = new Random(getIntArg("selectSnapshotRandom", 1));
 
       kCFA = getIntArg("kCFA", 0);
       kOS = getIntArg("kOS", 0);
       reachabilitySpec.pointedTo = getBooleanArg("pointedTo", false);
       reachabilitySpec.matchRepeatedFields = getBooleanArg("matchRepeatedFields", false);
       reachabilitySpec.matchFirstField = getBooleanArg("matchFirstField", false);
       reachabilitySpec.matchLastField = getBooleanArg("matchLastField", false);
 
       useStrongUpdates = getBooleanArg("useStrongUpdates", false);
       updateAbstraction = parseLocalAbstraction(getStringArg("updateAbstraction", ""));
       snapshotAbstraction = parseGlobalAbstraction(getStringArg("snapshotAbstraction", ""));
 
       if (useStrongUpdates && !(updateAbstraction instanceof NoneLocalAbstraction))
         throw new RuntimeException("Can only use strong updates with no abstract interpretation.  Use snapshots to have strong updates and abstractions.");
       if (!(updateAbstraction instanceof NoneLocalAbstraction) && !(snapshotAbstraction instanceof NoneGlobalAbstraction))
         throw new RuntimeException("At most one of updateAbstraction and snapshotAbstraction can be not none");
 
      if (getBooleanArg("visualize", false)) graphMonitor = new UbiGraphMonitor();
      else graphMonitor = new SerializingGraphMonitor(X.path("graph"), getIntArg("graph.maxCommands", 100000));
 
       // Save options
       HashMap<Object,Object> options = new LinkedHashMap<Object,Object>();
       options.put("program", System.getProperty("chord.work.dir"));
       options.put("property", propertyState.name());
       options.put("verbose", verbose);
       options.put("useStrongUpdates", useStrongUpdates);
       options.put("updateAbstraction", updateAbstraction);
       options.put("snapshotAbstraction", snapshotAbstraction);
       options.put("queryFrac", queryFrac);
       options.put("snapshotFrac", snapshotFrac);
       X.writeMap("options.map", options);
       X.output.put("exec.status", "running");
 
       super.run();
 
       // Save output
       X.output.put("exec.status", "done");
     } catch (Throwable t) {
       X.output.put("exec.status", "failed");
       X.errors("%s", t);
       for (StackTraceElement e : t.getStackTrace())
         X.logs("  %s", e);
     }
     X.finish();
 	}
 
   QueryResult queryResult(int e, int b) {
     Query q = new Query(e);
     QueryResult qr = queryResults.get(q);
     if (qr == null) {
       queryResults.put(q, qr = new QueryResult());
       qr.selected = selectQueryRandom.nextDouble() < queryFrac; // Choose to answer this query with some probability
     }
     return qr;
   }
 
   void outputQueries() {
     PrintWriter out = Utils.openOut(X.path("queries.out"));
     StatFig fig = new StatFig();
     for (Query q : queryResults.keySet()) {
       QueryResult qr = queryResults.get(q);
       if (qr.selected) {
         out.println(String.format("%s | %s %s", estr(q.e), qr.numTrue, qr.numFalse));
         fig.add(qr.numTrue + qr.numFalse);
       }
       else
         out.println(String.format("%s | ?", estr(q.e)));
     }
     out.close();
     X.output.put("query.numHits", fig.mean());
     X.logs("  # hits per query: %s (%s total hits)", fig, fig.n);
   }
 
   ThreadInfo threadInfo(int t) {
     if (t == -1) return null;
     ThreadInfo info = threadInfos.get(t);
     if (info == null)
       threadInfos.put(t, info = new ThreadInfo());
     return info;
   }
 
   // The global abstraction partitions the nodes in the current graph, where each partition has the same value.
   class SnapshotAnalysis {
     GlobalAbstraction abstraction;
     SnapshotAnalysisState state;
 
     public SnapshotAnalysis(GlobalAbstraction abstraction) {
       this.abstraction = abstraction;
       HashMap<Object, List<Integer>> w2as = abstraction.getAbstractionMap();
 
       this.state = propertyState.newSnapshotAnalysisState();
       state.abstraction = abstraction;
       state.w2as = w2as;
     }
 
     int complexity() { return state.w2as.size(); } // Complexity of this abstraction (number of abstract values)
 
     public boolean computeIsTrue(int a) { return state.computeIsTrue(a); } // Expensive
 
     public void runFinal() {
       propertyState.computeAll();
       int actualNumTrue = propertyState.numTrue();
 
       state.computeAll();
       int propNumTrue = state.numTrue();
 
       if (graphMonitor != null) {
         for (int a = 0; a < N; a++) {
           boolean actualTrue = propertyState.isTrue(a);
           boolean propTrue = state.isTrue(a);
           String color = null;
           if (actualTrue && propTrue) color = "#00ff00"; // Good
           else if (!actualTrue && propTrue) color = "#ff0000"; // Bad (false positive)
           else if (!actualTrue && !propTrue) color = "#ffffff"; // Good
           else throw new RuntimeException("Got true negative - shouldn't happen (snapshot analysis is broken)");
           // Get the abstraction value (either local or global)
           String label = (abstraction instanceof NoneGlobalAbstraction) ? a2v.get(a).toString() : state.getAbstraction(a).toString();
           graphMonitor.setNodeLabel(a, label);
           graphMonitor.setNodeColor(a, color);
         }
       }
 
       X.logs("=== Snapshot abstraction %s (at end) ===", abstraction);
       X.logs("  complexity: %d values", complexity());
       X.logs("  precision: %d/%d = %.2f", actualNumTrue, propNumTrue, 1.0*actualNumTrue/propNumTrue);
       X.output.put("finalSnapshot.actualNumTrue", actualNumTrue);
       X.output.put("finalSnapshot.propNumTrue", propNumTrue);
       X.output.put("finalSnapshot.precision", 1.0*actualNumTrue/propNumTrue);
       X.output.put("complexity", complexity());
       if (propNumTrue > 0) snapshotPrecision.add(1.0*actualNumTrue/propNumTrue);
 
       PrintWriter out = Utils.openOut(X.path("snapshot-abstractions"));
       for (Object w : state.w2as.keySet())
         out.println(w);
       out.close();
     }
   }
 
   public InstrScheme getInstrScheme() {
     if (instrScheme != null) return instrScheme;
     instrScheme = new InstrScheme();
 
     instrScheme.setEnterAndLeaveMethodEvent();
     instrScheme.setEnterAndLeaveLoopEvent();
 
     // TODO: turn off instrumentation when not needed
 
     instrScheme.setNewAndNewArrayEvent(true, true, true); // h, t, o
 
 	  instrScheme.setGetstaticPrimitiveEvent(true, true, true, true); // e, t, b, f
 	  instrScheme.setGetstaticReferenceEvent(true, true, true, true, true); // e, t, b, f, o
 	  instrScheme.setPutstaticPrimitiveEvent(true, true, true, true); // e, t, b, f
     instrScheme.setPutstaticReferenceEvent(true, true, true, true, true); // e, t, b, f, o
 
     instrScheme.setGetfieldPrimitiveEvent(true, true, true, true); // e, t, b, f
     instrScheme.setPutfieldPrimitiveEvent(true, true, true, true); // e, t, b, f
     instrScheme.setGetfieldReferenceEvent(true, true, true, true, true); // e, t, b, f, o
     instrScheme.setPutfieldReferenceEvent(true, true, true, true, true); // e, t, b, f, o
 
     instrScheme.setAloadPrimitiveEvent(true, true, true, true); // e, t, b, i
     instrScheme.setAstorePrimitiveEvent(true, true, true, true); // e, t, b, i
     instrScheme.setAloadReferenceEvent(true, true, true, true, true); // e, t, b, i, o
     instrScheme.setAstoreReferenceEvent(true, true, true, true, true); // e, t, b, i, o
 
     instrScheme.setThreadStartEvent(true, true, true); // i, t, o
     instrScheme.setThreadJoinEvent(true, true, true); // i, t, o
 
     instrScheme.setAcquireLockEvent(true, true, true); // l, t, o
     instrScheme.setReleaseLockEvent(true, true, true); // r, t, o
     instrScheme.setWaitEvent(true, true, true); // i, t, o
     instrScheme.setNotifyEvent(true, true, true); // i, t, o
 
     instrScheme.setMethodCallEvent(true, true, true, true, true); // i, t, o, before, after
 
     instrScheme.setReturnPrimitiveEvent(true, true); // i, t
     instrScheme.setReturnReferenceEvent(true, true, true); // i, t, o
 
     instrScheme.setExplicitThrowEvent(true, true, true); // p, t, o
     instrScheme.setImplicitThrowEvent(true, true); // p, t
 
     instrScheme.setQuadEvent();
     instrScheme.setBasicBlockEvent();
 
     return instrScheme;
   }
 
   public void initAllPasses() {
     int E = instrumentor.getEmap().size();
     int H = instrumentor.getHmap().size();
     int F = instrumentor.getFmap().size();
     X.logs("initAllPasses: |E| = %s, |H| = %s, |F| = %s", E, H, F);
 
     domT = (DomT)Project.getTrgt("T");
     Project.runTask(domT);
 
     propertyState.X = X;
     propertyState.a2edges = a2edges;
     propertyState.verbose = verbose;
     snapshotAbstraction.X = X;
     snapshotAbstraction.o2h = o2h;
     snapshotAbstraction.a2v = a2v;
     snapshotAbstraction.a2edges = a2edges;
   }
 
   public void doneAllPasses() {
     X.logs("===== Results using updateAbstraction = %s, useStrongUpdates = %s =====", updateAbstraction, useStrongUpdates);
 
     // Evaluate on queries (real metric)
     int numTrue = 0;
     int numSelected = 0;
     for (QueryResult qr : queryResults.values()) {
       if (!qr.selected) continue;
       if (qr.isTrue()) numTrue++;
       numSelected++;
     }
 
     X.logs("  %d total queries; %d/%d = %.2f queries proposed to have property %s",
       queryResults.size(), numTrue, numSelected, 1.0*numTrue/numSelected, propertyState.name());
     X.output.put("query.numTrue", numTrue);
     X.output.put("query.numSelected", numSelected);
     X.output.put("query.numTotal", queryResults.size());
     X.output.put("query.fracTrue", 1.0*numTrue/numSelected);
     outputQueries();
 
     if (!(updateAbstraction instanceof NoneLocalAbstraction) || !useStrongUpdates)
       X.logs("    (run with updateAbstraction = none and useStrongUpdates = true to get number actually escaping; divide to get precision of %s)", updateAbstraction);
 
     // Evaluate on final nodes: how many nodes have that property
     if (useStrongUpdates) propertyState.computeAll(); // Need to still do this
     numTrue = 0;
     for (int a = 0; a < N; a++)
       if (propertyState.isTrue(a)) numTrue++;
     X.logs("  %d/%d = %.2f nodes proposed to be escaping at end", numTrue, N, 1.0*numTrue/N);
     X.output.put("finalNodes.numTrue", numTrue);
     X.output.put("finalNodes.numTotal", N);
     X.output.put("finalNodes.fracTrue", 1.0*numTrue/N);
     X.output.put("finalObjects.numTotal", o2v.size());
 
     new SnapshotAnalysis(snapshotAbstraction).runFinal();
 
     X.logs("  snapshot precision: %s", snapshotPrecision);
     X.output.put("snapshotPrecision", snapshotPrecision.mean());
     X.output.put("query.totalNumHits", numQueryHits);
 
     if (graphMonitor != null) graphMonitor.finish();
   }
 
   //////////////////////////////
 
   int getNode(int t, int o) {
     //if (o == -1) return -1;
     if (o == NULL_OBJECT) return -1;
     Object v = o2v.get(o);
     ThreadInfo info = threadInfo(t);
     if (v == null) o2v.put(o, v = updateAbstraction.get(info, o));
     int a = a2v.getOrAdd(v);
     if (a == N) {
       if (verbose >= 1) X.logs("NEWNODE o=%s => a=%s, v=%s", ostr(o), a, v);
       snapshotAbstraction.nodeCreated(a, info, o);
       a2edges.add(new ArrayList<Edge>());
       N++;
       if (graphMonitor != null) graphMonitor.addNode(a, null);
     }
     return a;
   }
 
   void addEdge(int t, int oa, int f, int ob) {
     int a = getNode(t, oa);
     int b = getNode(t, ob);
     // Strong update: remove existing field pointer
     List<Edge> edges = a2edges.get(a);
     if (useStrongUpdates) {
       assert (updateAbstraction instanceof NoneLocalAbstraction); // Otherwise we have to materialize...
       for (int i = 0; i < edges.size(); i++) {
         if (edges.get(i).f == f) {
           int old_b = edges.get(i).b;
           snapshotAbstraction.edgeDeleted(a, old_b);
           if (graphMonitor != null) graphMonitor.deleteEdge(a, old_b);
           edges.remove(i);
           break;
         }
       }
     }
     int numNew = -1;
     if (b != -1) {
       edges.add(new Edge(f, b));
       snapshotAbstraction.edgeCreated(a, b);
       if (!useStrongUpdates) numNew = propertyState.propagateAlongEdge(a, b, f);
       if (graphMonitor != null)
         graphMonitor.addEdge(a, b, ""+f);
     }
     if (verbose >= 1)
       X.logs("ADDEDGE b=%s (%s) f=%s o=%s (%s)%s", ostr(oa), astr(a), fstr(f), ostr(ob), astr(b), numNew != -1 ? ", "+numNew+" new" : "");
   }
 
   void makeQuery(int e, int t, int o) {
     numQueryHits++;
     // If we are using strong updates, then escapeNodes is not getting updated, so we can't answer queries (fast).
     QueryResult result = queryResult(e, o);
     if (result.selected) {
       int a = a2v.indexOf(o2v.get(o));
       if (!useStrongUpdates) // Weak updates: we are updating escape all the time, so easy to answer this query
         result.add(propertyState.isTrue(a));
       else {
         SnapshotAnalysis analysis = new SnapshotAnalysis(snapshotAbstraction);
         result.add(analysis.computeIsTrue(a)); // Use the snapshot (this is expensive!)
       }
       if (verbose >= 1) X.logs("QUERY e=%s, t=%s, o=%s: result = %s", estr(e), tstackstr(t), ostr(o), result);
     }
     if (selectSnapshotRandom.nextDouble() < snapshotFrac) {
       new SnapshotAnalysis(snapshotAbstraction).runFinal();
     }
   }
 
   public String nodes_str(TIntHashSet nodes) {
     StringBuilder buf = new StringBuilder();
     for (int a : nodes.toArray()) {
       if (buf.length() > 0) buf.append(' ');
       buf.append(astr(a));
     }
     return buf.toString();
   }
 
   String astr(int a) { return a == -1 ? "(none)" : a+":"+a2v.get(a); } // node
   String fstr(int f) { // field
     if (f == THREAD_FIELD) return "[T]";
     if (f == ARRAY_FIELD) return "[*]";
     return f < 0 ? "-" : instrumentor.getFmap().get(f);
   }
   String hstr(int h) { return h < 0 ? "-" : instrumentor.getHmap().get(h); } // heap allocation site
   String estr(int e) {
     if (e == -1) return "-";
     Quad quad = (Quad)instrumentor.getDomE().get(e);
     return Program.v().toJavaPosStr(quad)+" "+Program.v().toQuadStr(quad);
   }
   String mstr(int m) { return m == -1 ? "-" : instrumentor.getMmap().get(m); } // method
   String wstr(int w) { return w == -1 ? "-" : instrumentor.getWmap().get(w); } // loop
   String istr(int i) { return i == -1 ? "-" : instrumentor.getImap().get(i); } // call site
   String ostr(int o) { return o == -1 ? "-" : (o == NULL_OBJECT ? "null" : "O"+o); } // concrete object
   String tstr(int t) { return t == -1 ? "-" : "T"+t; } // thread
   String pstr(int p) { return p == -1 ? "-" : instrumentor.getPmap().get(p); } // simple statement?
   String stackstr(int t) {
     Stack<Integer> stack = threadInfo(t).callStack;
     return stack.size() == 0 ? "(empty)" : stack.size()+":"+mstr(stack.peek());
   }
   String tstackstr(int t) { return t == -1 ? "-" : tstr(t)+"["+stackstr(t)+"]"; }
 
   ////////////////////////////////////////////////////////////
   // Handlers
 
   @Override public void processEnterMethod(int m, int t) {
     if (verbose >= 6) X.logs("EVENT enterMethod: m=%s, t=%s", mstr(m), tstackstr(t));
     threadInfo(t).callStack.push(m);
   }
   @Override public void processLeaveMethod(int m, int t) {
     if (verbose >= 6) X.logs("EVENT leaveMethod: m=%s, t=%s", mstr(m), tstackstr(t));
     ThreadInfo info = threadInfo(t);
     if (info.callStack.size() == 0)
       X.errors("Tried to pop empty call stack");
     else {
       int mm = info.callStack.pop();
       if (mm != m) X.errors("Pushed %s but popped %s", mstr(m), mstr(mm));
     }
   }
 
   @Override public void processEnterLoop(int w, int t) {
     if (verbose >= 5) X.logs("EVENT enterLoop: w=%s", w);
   }
   @Override public void processLeaveLoop(int w, int t) {
     if (verbose >= 5) X.logs("EVENT leaveLoop: w=%s", w);
   }
 
   @Override public void processNewOrNewArray(int h, int t, int o) { // new Object
     o2h.put(o, h);
     if (verbose >= 5) X.logs("EVENT new: h=%s, t=%s, o=%s", hstr(h), tstackstr(t), ostr(o));
     getNode(t, o); // Force creation of a new node
   }
 
   @Override public void processGetstaticPrimitive(int e, int t, int b, int f) { }
   @Override public void processGetstaticReference(int e, int t, int b, int f, int o) { // ... = b.f, where b.f = o and b is static
     if (verbose >= 5) X.logs("EVENT getStaticReference: e=%s, t=%s, b=%s, f=%s, o=%s", estr(e), tstackstr(t), ostr(b), fstr(f), ostr(o));
   }
   @Override public void processPutstaticPrimitive(int e, int t, int b, int f) { }
   @Override public void processPutstaticReference(int e, int t, int b, int f, int o) { // b.f = o, where b is static
     if (verbose >= 5) X.logs("EVENT putStaticReference: e=%s, t=%s, b=%s, f=%s, o=%s", estr(e), tstackstr(t), ostr(b), fstr(f), ostr(o));
     propertyState.setGlobal(getNode(t, b), useStrongUpdates);
     addEdge(t, b, f, o);
   }
 
   @Override public void processGetfieldPrimitive(int e, int t, int b, int f) {
     makeQuery(e, t, b);
   }
   @Override public void processGetfieldReference(int e, int t, int b, int f, int o) { // ... = b.f, where b.f = o
     if (verbose >= 5) X.logs("EVENT getFieldReference: e=%s, t=%s, b=%s, f=%s, o=%s", estr(e), tstackstr(t), ostr(b), fstr(f), ostr(o));
     makeQuery(e, t, b);
   }
   @Override public void processPutfieldPrimitive(int e, int t, int b, int f) {
     makeQuery(e, t, b);
   }
   @Override public void processPutfieldReference(int e, int t, int b, int f, int o) { // b.f = o
     if (verbose >= 5) X.logs("EVENT putFieldReference: e=%s, t=%s, b=%s, f=%s, o=%s", estr(e), tstackstr(t), ostr(b), fstr(f), ostr(o));
     makeQuery(e, t, b);
     addEdge(t, b, f, o);
   }
 
   @Override public void processAloadPrimitive(int e, int t, int b, int i) {
     makeQuery(e, t, b);
   }
   @Override public void processAloadReference(int e, int t, int b, int i, int o) {
     if (verbose >= 5) X.logs("EVENT loadReference: e=%s, t=%s, b=%s, i=%s, o=%s", estr(e), tstackstr(t), ostr(b), i, ostr(o));
     makeQuery(e, t, b);
   }
   @Override public void processAstorePrimitive(int e, int t, int b, int i) {
     makeQuery(e, t, b);
   }
   @Override public void processAstoreReference(int e, int t, int b, int i, int o) {
     if (verbose >= 5) X.logs("EVENT storeReference: e=%s, t=%s, b=%s, i=%s, o=%s", estr(e), tstackstr(t), ostr(b), i, ostr(o));
     makeQuery(e, t, b);
     addEdge(t, b, ARRAY_FIELD, o);
   }
 
   // In o.start() acts like g_{t,i} = o, where g_{t,i} is like a global variable specific to thread t.
   //int threadToObjId(int i, int t) { return 100000 + 100*i + t; }
 
   @Override public void processThreadStart(int i, int t, int o) {
     if (verbose >= 4) X.logs("EVENT threadStart: i=%s, t=%s, o=%s", istr(i), tstackstr(t), ostr(o));
     //int b = threadToObjId(i, t);
     //setEscape(getNode(t, b));
     // How to get a handle on the thread that was just started?
     int b = THREAD_GLOBAL_OBJECT;
     propertyState.setGlobal(getNode(t, b), useStrongUpdates);
     addEdge(t, b, THREAD_FIELD, o);
   }
   @Override public void processThreadJoin(int i, int t, int o) {
     if (verbose >= 4) X.logs("EVENT threadJoin: i=%s, t=%s, o=%s", istr(i), tstackstr(t), ostr(o));
     // Need to reclaim selectively...
     //int b = threadToObjId(i, t);
     //addEdge(t, b, THREAD_FIELD, NULL_OBJECT);
   }
 
   @Override public void processAcquireLock(int l, int t, int o) { }
   @Override public void processReleaseLock(int r, int t, int o) { }
   @Override public void processWait(int i, int t, int o) { }
   @Override public void processNotify(int i, int t, int o) { }
 
   @Override public void processMethodCallBef(int i, int t, int o) {
     if (verbose >= 5) X.logs("EVENT methodCallBefore: i=%s, t=%s, o=%s", istr(i), tstackstr(t), ostr(o));
     threadInfo(t).callSites.push(i);
     threadInfo(t).callAllocs.push(o2h.get(o));
   }
   @Override public void processMethodCallAft(int i, int t, int o) {
     ThreadInfo info = threadInfo(t);
     if (verbose >= 5) X.logs("EVENT methodCallAfter: i=%s, t=%s, o=%s", istr(i), tstackstr(t), ostr(o));
     if (info.callSites.size() == 0)
       X.errors("Tried to pop empty callSites stack"); // Should only happen for com.ibm.misc.SignalDispatcher
     else {
       int ii = info.callSites.pop();
       if (ii != i) X.logs("pushed %s but popped %s", istr(i), istr(ii));
     }
     if (info.callAllocs.size() == 0)
       X.errors("Tried to pop empty callAllocs stack");
     else {
       int hh = info.callAllocs.pop();
       int h = o2h.get(o);
       if (hh != h) X.logs("pushed %s but popped %s", hstr(h), hstr(hh));
     }
   }
 
   @Override public void processReturnPrimitive(int p, int t) { }
   @Override public void processReturnReference(int p, int t, int o) { }
   @Override public void processExplicitThrow(int p, int t, int o) { }
   @Override public void processImplicitThrow(int p, int t, int o) { }
 
   @Override public void processQuad(int p, int t) {
     if (verbose >= 7) X.logs("EVENT processQuad p=%s, t=%s", pstr(p), tstackstr(t));
   }
   @Override public void processBasicBlock(int b, int t) { }
 }
 
 ////////////////////////////////////////////////////////////
 
 // Pointer via field f to node b
 class Edge {
   public Edge(int f, int b) {
     this.f = f;
     this.b = b;
   }
   int f;
   int b;
 }
 
 // Query for thread escape: is the object pointed to by the relvant variable thread-escaping at program point e?
 class Query {
   Query(int e) { this.e = e; }
   int e; // Program point
   @Override public boolean equals(Object _that) {
     Query that = (Query)_that;
     return this.e == that.e;
   }
   @Override public int hashCode() {
     return e;
   }
 }
 
 class QueryResult {
   boolean selected; // Whether we are trying to answer this query or not
   int numTrue = 0;
   int numFalse = 0;
 
   boolean isTrue() { return numTrue > 0; } // Existential property
   void add(boolean b) {
     if (b) numTrue++;
     else numFalse++;
   }
 
   @Override public String toString() { return numTrue+"|"+numFalse; }
 }
 
 class ThreadInfo {
   Stack<Integer> callStack = new Stack(); // Elements are methods m (for visualization)
   Stack<Integer> callSites = new Stack(); // Elements are call sites i (for kCFA)
   Stack<Integer> callAllocs = new Stack(); // Elements are object allocation sites h (for kOS)
 }
 
 abstract class PropertyState {
   Execution X;
   List<List<Edge>> a2edges;
   int verbose;
 
   public abstract String name();
   public abstract void setGlobal(int a, boolean useStrongUpdates); // Node a corresponds to a global variable (for whatever that's worth to the analysis)
   public abstract int propagateAlongEdge(int a, int b, int f); // Update the property incrementally (only called with weak updates)
   public abstract int numTrue(); // Number of nodes satisfying the property
   public abstract boolean isTrue(int a); // Property holds on node a?
   public abstract void computeAll(); // Compute the property for all nodes (called if strong updates)
   public abstract SnapshotAnalysisState newSnapshotAnalysisState();
 }
 
 abstract class SnapshotAnalysisState {
   GlobalAbstraction abstraction;
   HashMap<Object, List<Integer>> w2as; // snapshot abstract value w -> nodes with that value
   public Object getAbstraction(int a) { return abstraction.get(a); } // Can override
 
   public abstract boolean computeIsTrue(int a); // Does property hold for node a under the snapshot abstraction specified by w2as?
   public abstract int numTrue(); // Number of nodes satisfying the property
   public abstract boolean isTrue(int a); // Property holds on node a?
   public abstract void computeAll(); // Return number of nodes for which the property holds (under the abstraction)
 }
 
 ////////////////////////////////////////////////////////////
 // Specific instances: TODO: move these to another file
 
 // Not natural
 abstract class MayAliasPropertyState extends PropertyState {
   public String name() { return "may-alias"; }
 }
 
 // This property doesn't make sense for heap abstractions
 abstract class CyclicityPropertyState extends PropertyState {
   public String name() { return "cyclicity"; }
 
   TIntHashSet cyclicNodes = new TIntHashSet();
 
   public void computeAll() {
     // Need to do strongly connected components
     int N = a2edges.size();
     //for (int a = 0; a < N; a++)
     // TODO
   }
 
   public int propagateAlongEdge(int a, int b, int f) {
     // TODO
     return 0;
   }
 
   public void setGlobal(int a, boolean useStrongUpdates) { }
 
   public boolean isTrue(int a) { return cyclicNodes.contains(a); }
   public int numTrue() { return cyclicNodes.size(); }
 
   /*class MySnapshotAnalysisState extends SnapshotAnalysisState {
     private boolean findCycle(int a, TIntHashSet visitedNodes) {
       if (visitedNodes.contains(a)) return true; // Found a cycle
       visitedNodes.add(a);
 
       // Try following edges in the graph
       for (Edge e : a2edges.get(a))
         if (findCycle(e.b, visitedNodes)) return true;
       // Try jumping to nodes with the same value
       Object w = getAbstraction(a);
       for (int b : w2as.get(w))
         if (findCycle(b, visitedNodes)) return true;
       return false;
     }
 
     // Can this node reach a cycle under the given global abstraction?
     public boolean computeIsTrue(int a) {
       return findCycle(a, new TIntHashSet());
     }
 
     TIntHashSet abstractionEscapedNodes = new TIntHashSet();
     public void computeAll() {
       // Which nodes are escaping under the abstraction?
       for (int start : staticEscapedNodes.toArray())
         reachable(start, -1, abstractionEscapedNodes);
     }
     public int numTrue() { return abstractionEscapedNodes.size(); }
     public boolean isTrue(int a) { return abstractionEscapedNodes.contains(a); }
   }
   public SnapshotAnalysisState newSnapshotAnalysisState() { return new MySnapshotAnalysisState(); }*/
 }

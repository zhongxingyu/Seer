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
import org.ubiety.ubigraph.UbigraphClient;
 
 /**
  * Two types of abstractions: local abstractions and global abstractions.
  *
  * TODO: use variables as sources for reachability
  *
  * @author Percy Liang (pliang@cs.berkeley.edu)
  */
 
 // Local abstractions only depend on the object (used for abstract interpretation),
 // but can depend on the current thread (get information aobut where the object was allocated).
 abstract class LocalAbstraction {
   public abstract Object get(ThreadInfo info, int o);
 }
 
 class NoneLocalAbstraction extends LocalAbstraction {
   public String toString() { return "none"; }
   public Object get(ThreadInfo info, int o) { return o; }
 }
 
 class AllocLocalAbstraction extends LocalAbstraction {
   int kCFA, kOS;
   TIntIntHashMap o2h;
 
   public AllocLocalAbstraction(int kCFA, int kOS, TIntIntHashMap o2h) {
     this.kCFA = kCFA;
     this.kOS = kOS;
     this.o2h = o2h;
   }
 
   public String toString() {
     return String.format("alloc(kCFA=%d,kOS=%d)", kCFA, kOS);
   }
 
   public Object get(ThreadInfo info, int o) {
     if (kCFA == 0 && kOS == 0) return o2h.get(o); // 0-CFA
 
     StringBuilder buf = new StringBuilder();
     buf.append(o2h.get(o));
 
     if (kCFA > 0) {
       for (int i = 0; i < kCFA; i++) {
         int j = info.callSites.size() - i - 1;
         if (j < 0) break;
         buf.append('_');
         buf.append(info.callSites.get(j));
       }
     }
 
     if (kOS > 0) {
       for (int i = 0; i < kCFA; i++) {
         int j = info.callAllocs.size() - i - 1;
         if (j < 0) break;
         buf.append('_');
         buf.append(info.callAllocs.get(j));
       }
     }
 
     return buf.toString();
   }
 }
 
 ////////////////////////////////////////////////////////////
 
 // Global abstractions depend on the graph (used for snapshots).
 abstract class GlobalAbstraction {
   Execution X;
   TIntIntHashMap o2h;
   IndexMap<Object> a2v;
   List<List<Edge>> a2edges;
 
   // Main thing to comput
   HashMap<Object,List<Integer>> w2as = new HashMap<Object,List<Integer>>(); // abstraction value w -> nodes with that abstraction value
 
   public abstract Object get(int a); // Get abstraction value w of the node
 
   // These methods should update w2as incrementally for efficiency
   public void nodeCreated(int a, ThreadInfo info, int o) { } // Called when a new node is created
   public void edgeCreated(int a, int b) { } // Called when a new edge is created
   public void edgeDeleted(int a, int b) { } // Called when existing edge is deleted
   public HashMap<Object,List<Integer>> getAbstractionMap() { return w2as; }
 
   // Can always fall back on super inefficient computation of everything
   public void compute_w2as() {
     w2as.clear();
     int N = a2v.size();
     for (int a = 0; a < N; a++) {
       Object w = get(a);
       Utils.add(w2as, w, a);
     }
   }
 
   // Except for the trivial global abstraction, we assume that the node corresponds to the object
   int a2o(int a) {
     //assert (updateAbstraction instanceof NoneLocalAbstraction);
     return (Integer)a2v.get(a);
   }
 }
 
 class NoneGlobalAbstraction extends GlobalAbstraction {
   public String toString() { return "none"; }
   public Object get(int a) { return a; }
   @Override public void nodeCreated(int a, ThreadInfo info, int o) {
     Object w = get(a);
     Utils.add(w2as, w, a);
   }
 }
 
 class WrappedGlobalAbstraction extends GlobalAbstraction {
   LocalAbstraction abstraction; // Just use this local abstraction
   List<Object> a2w = new ArrayList<Object>(); // a (node ID) -> abstract value w
 
   public WrappedGlobalAbstraction(LocalAbstraction abstraction) { this.abstraction = abstraction; }
   public String toString() { return abstraction.toString(); }
 
   public Object get(int a) { return a2w.get(a); }
   @Override public void nodeCreated(int a, ThreadInfo info, int o) {
     assert (a == a2w.size());
     Object w = abstraction.get(info, o);
     a2w.add(w);
     Utils.add(w2as, w, a);
   }
 }
 
 class RecencyGlobalAbstraction extends GlobalAbstraction {
   TIntIntHashMap h2count = new TIntIntHashMap(); // heap allocation site h -> number of objects that have been allocated at h
   TIntIntHashMap o2count = new TIntIntHashMap(); // object -> count
 
   public String toString() { return "recency"; }
 
   public Object get(int a) {
     int o = a2o(a);
     int h = o2h.get(o);
     boolean mostRecent = o2count.get(o) == h2count.get(h);
     return mostRecent ? h+"R" : h;
   }
 
   // TODO: do incremental updates
   @Override public HashMap<Object,List<Integer>> getAbstractionMap() {
     compute_w2as();
     return w2as;
   }
 
   @Override public void nodeCreated(int a, ThreadInfo info, int o) {
     int h = o2h.get(o);
     h2count.adjustOrPutValue(h, 1, 1);
     o2count.put(o, h2count.get(h));
     X.logs("nodeCreated: o=%s, h=%s; count = %s", o, h, o2count.get(o));
   }
 }
 
 class ReachabilityGlobalAbstraction extends GlobalAbstraction {
   static class Spec {
     public boolean pointedTo, matchRepeatedFields, matchFirstField, matchLastField;
   }
   Spec spec;
 
   public ReachabilityGlobalAbstraction(Spec spec) {
     this.spec = spec;
   }
 
   @Override public String toString() {
     if (spec.pointedTo) return "reach(point)";
     if (spec.matchRepeatedFields) return "reach(f*)";
     if (spec.matchFirstField) return "reach(first_f)";
     if (spec.matchLastField) return "reach(last_f)";
     return "reach";
   }
 
   List<List<String>> a2pats = new ArrayList(); // a -> list of path patterns that describe a
 
   // TODO: do incremental updates
   @Override public HashMap<Object,List<Integer>> getAbstractionMap() {
     // For each node, get its source
     int N = a2v.size();
     for (int a = 0; a < N; a++) a2pats.get(a).clear();
     for (int a = 0; a < N; a++) {
       int o = a2o(a);
       String source = "H"+o2h.get(o);
       //X.logs("--- source=%s, a=%s", source, astr(a));
       search(source, -1, -1, 0, a);
     }
 
     compute_w2as(); // a2pats -> w2as
     return w2as;
   }
 
   // Source: variable or heap-allocation site.
   // General recipe for predicates is some function of the list of fields from source to the node.
   // Examples:
   //   Reachable-from: path must exist.
   //   Pointed-to-by: length must be one.
   //   Reachable-following-a-single field
   //   Reachable-from-with-first-field
   //   Reachable-from-with-last-field
   @Override public void nodeCreated(int a, ThreadInfo info, int o) {
     assert (a == a2pats.size());
     a2pats.add(new ArrayList<String>());
   }
   @Override public void edgeCreated(int a, int b) {
     // TODO: incrementally update a2pats and w2as
   }
   @Override public void edgeDeleted(int a, int b) {
     // TODO: incrementally update a2pats and w2as
   }
 
   void search(String source, int first_f, int last_f, int len, int a) {
     String v = null;
     if (len > 0) { // Avoid trivial predicates
       if (spec.pointedTo) {
         if (len == 1) v = source;
       }
       else if (spec.matchRepeatedFields) {
         assert (first_f == last_f);
         v = source+"."+first_f+"*";
       } 
       else if (spec.matchFirstField)
         v = source+"."+first_f+".*";
       else if (spec.matchLastField)
         v = source+".*."+last_f;
       else // Plain reachability
         v = source;
     }
 
     if (v != null && a2pats.get(a).indexOf(v) != -1) return; // Already have it
 
     if (v != null) a2pats.get(a).add(v);
     //X.logs("source=%s first_f=%s last_f=%s len=%s a=%s: v=%s", source, fstr(first_f), fstr(last_f), len, astr(a), a2ws[a]);
 
     if (spec.pointedTo && len >= 1) return;
 
     // Recurse
     for (Edge e : a2edges.get(a)) {
       if (spec.matchRepeatedFields && first_f != -1 && first_f != e.f) continue; // Must have same field
       search(source, len == 0 ? e.f : first_f, e.f, len+1, e.b);
     }
   }
 
   public Object get(int a) {
     Collections.sort(a2pats.get(a)); // Canonicalize
     return a2pats.get(a).toString();
   }
 }

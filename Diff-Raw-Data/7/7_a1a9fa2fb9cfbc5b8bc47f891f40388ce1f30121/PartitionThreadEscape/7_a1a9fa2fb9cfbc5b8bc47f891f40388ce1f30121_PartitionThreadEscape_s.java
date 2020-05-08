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
  * Thread-escape in the partition framework.
  *
  * @author Percy Liang (pliang@cs.berkeley.edu)
  */
 class ThreadEscapePropertyState extends PropertyState {
   public String name() { return "thread-escape"; }
 
   TIntHashSet staticEscapedNodes = new TIntHashSet(); // set of a (node ID) [static nodes: monotonically growing (even with strong updates)]
   TIntHashSet escapedNodes = new TIntHashSet(); // set of a (node ID) [updated if using weak updates]
 
   public void computeAll() {
     escapedNodes.clear();
     // Propagate escapes on nodes
     for (int a : staticEscapedNodes.toArray())
       followEscapeNodes(a);
   }
 
   public int propagateAlongEdge(int a, int b, int f) {
     if (escapedNodes.contains(a))
       return followEscapeNodes(b);
     return 0;
   }
 
   // Weak update: contaminate everyone downstream
   // Return number of new nodes marked as escaping
   private int followEscapeNodes(int a) {
     if (escapedNodes.contains(a)) return 0;
     int n = 1;
     escapedNodes.add(a);
     for (Edge e : a2edges.get(a))
       n += followEscapeNodes(e.b);
     return n;
   }
 
   public void setGlobal(int a, boolean useStrongUpdates) {
     //if (verbose >= 1) X.logs("SETESCAPE a=%s", astr(a));
     staticEscapedNodes.add(a);
     if (!useStrongUpdates) escapedNodes.add(a);
   }
 
   public boolean isTrue(int a) { return escapedNodes.contains(a); }
   public int numTrue() { return escapedNodes.size(); }
 
   // Then we want to compute whether a given node is reachable from a some node in initEscapedNodes
   // via 1) edges in the graph or 2) jumps between nodes with the same value.
   class MySnapshotAnalysisState extends SnapshotAnalysisState {
     @Override public Object getAbstraction(int a) {
       return staticEscapedNodes.contains(a) ? "-" : abstraction.get(a); // Always separate out the statics
     }
 
     private boolean reachable(int a, int dest, TIntHashSet visitedNodes) {
       if (a == dest) return true;
       if (visitedNodes.contains(a)) return false;
       visitedNodes.add(a);
 
       // Try following edges in the graph
       for (Edge e : a2edges.get(a))
         if (reachable(e.b, dest, visitedNodes)) return true;
       // Try jumping to nodes with the same value
       Object w = getAbstraction(a);
       List<Integer> as = w2as.get(w);
       if (as != null) {
         for (int b : as)
           if (reachable(b, dest, visitedNodes)) return true;
       }
       return false;
     }
 
     // Does node a escape under the given global abstraction?
     public boolean computeIsTrue(int a) {
       for (int start : staticEscapedNodes.toArray())
         if (reachable(start, a, new TIntHashSet())) return true;
       return false;
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
   public SnapshotAnalysisState newSnapshotAnalysisState() { return new MySnapshotAnalysisState(); }
 }

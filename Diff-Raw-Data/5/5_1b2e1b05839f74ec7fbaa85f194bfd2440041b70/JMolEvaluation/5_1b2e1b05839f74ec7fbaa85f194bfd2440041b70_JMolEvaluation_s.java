 package racefix;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Test;
 
 import com.ibm.wala.ipa.callgraph.CGNode;
 import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
 import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
 import com.ibm.wala.util.collections.IndiscriminateFilter;
 import com.ibm.wala.util.graph.Graph;
 import com.ibm.wala.util.graph.GraphSlicer;
 
 import racefix.AccessTrace;
 import racefix.jmol.util.JmolCallGraphFilter;
 import racefix.jmol.util.JmolHeapGraphFilter;
 import racefix.util.AccessTraceFilter;
 import sabazios.A;
 import sabazios.tests.DataRaceAnalysisTest;
 import sabazios.util.U;
 import sabazios.util.wala.viz.CGNodeDecorator;
 import sabazios.util.wala.viz.ColoredHeapGraphNodeDecorator;
 import sabazios.wala.CS;
 
 @SuppressWarnings("deprecation")
 public class JMolEvaluation extends DataRaceAnalysisTest {
   public JMolEvaluation() {
     super();
    this.addBinaryDependency("../evaluation/Jmol/bin");
     this.addBinaryDependency("../lib/parallelArray.mock");
    this.addJarFolderDependency("../lib/Jmol");
   }
 
   @Test
   public void testJmolFull() throws Exception {
     CS.NCFA = 0;
     Map<String, String> start = new HashMap<String, String>();
     start.put("plotLine\\(", "this");
     start.put("Cylinder3D, render\\(", "this");
     start.put("plotLineClipped\\(I", "zbuf");
 
     String entryClass = "Lorg/openscience/jmol/app/Jmol";
     String mainMethod = MAIN_METHOD;
 
     runJmol(start, entryClass, mainMethod, true, "Jmol");
   }
   
   @Test
   public void testJmolStripped() throws Exception {
     Map<String, String> start = new HashMap<String, String>();
     start.put("plotLine\\(", "this");
     start.put("Cylinder3D, render\\(", "this");
     start.put("plotLineClipped\\(I", "zbuf");
 
     String entryClass = "Lracefix/jmol/Main";
     String entryMethod = "jmol()V";
     runJmol(start, entryClass, entryMethod, true, "Jmol_mock");
   }
 
 
   private void runJmol(Map<String, String> traceStartingPoint, String entryClass, String entryMethod,
       boolean printGraphs, String graphNames) throws Exception {
     setup(entryClass, entryMethod);
     A a = new A(callGraph, pointerAnalysis);
     a.precompute();
 
     int i = 0;
     final AccessTrace[] traces = new AccessTrace[traceStartingPoint.size()];
     for (String methodName : traceStartingPoint.keySet()) {
       List<CGNode> possibleStartNode = a.findNodes(".*" + methodName + ".*");
       CGNode traceStartMethodeNode = possibleStartNode.get(0);
 
       String varName = traceStartingPoint.get(methodName);
       int ssaValue = 0;
       if (varName.equals("this"))
         ssaValue = 1;
       else
         ssaValue = U.getValueForVariableName(traceStartMethodeNode, traceStartingPoint.get(methodName));
 
       final AccessTrace accessTrace = new AccessTrace(a, traceStartMethodeNode, ssaValue,
           new IndiscriminateFilter<CGNode>());
       accessTrace.compute();
       traces[i++] = accessTrace;
     }
 
     if (printGraphs) {
       Graph<Object> prunedHeapGraph = GraphSlicer.prune(a.heapGraph, new JmolHeapGraphFilter(traces));
       Graph<CGNode> prunedCallGraph = GraphSlicer.prune(a.callGraph, new JmolCallGraphFilter());
       ColoredHeapGraphNodeDecorator color = new ColoredHeapGraphNodeDecorator(prunedHeapGraph, new AccessTraceFilter(
           traces));
       a.dotGraph(prunedHeapGraph, graphNames + "_heapGraph", color);
       a.dotGraph(prunedCallGraph, graphNames + "_callGraph", new CGNodeDecorator(a));
     }
   }
 
   @SuppressWarnings("unused")
   private static void printAccessTraces(AccessTrace[] traces) {
     for (AccessTrace trace : traces) {
       System.out.println("instanceKeys:");
       for (InstanceKey instKey : trace.getinstances())
         System.out.println(instKey.toString());
       System.out.println("pointerKeys:");
       for (PointerKey pointKey : trace.getPointers())
         System.out.println(pointKey);
       System.out.println("--------");
     }
   }
 
 }

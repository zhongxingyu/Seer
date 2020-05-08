 package submit;
 
 // some useful things to import. add any additional imports you need.
 import java.util.*;
 import joeq.Compiler.Quad.*;
 import flow.Flow;
 import joeq.Compiler.Quad.Operand.RegisterOperand;
 import joeq.Compiler.Quad.RegisterFactory.Register;
 
 
 /**
  * Skeleton class for implementing a reaching definition analysis
  * using the Flow.Analysis interface.
  */
 
 public class ReachingDefs implements Flow.Analysis {
 
     /**
      * Class for the dataflow objects in the ReachingDefs analysis.
      * You are free to change this class or move it to another file.
      */
     public static class MyDataflowObject implements Flow.DataflowObject {
         /**
          * Methods from the Flow.DataflowObject interface.
          * See Flow.java for the meaning of these methods.
          * These need to be filled in.
          */
 	private static  Map<String, List<Integer>> registersToQuads = new TreeMap<String, List<Integer>>();
 
 	public static void addToMap(Register key, Quad value) {
 	    List<Integer> s = registersToQuads.get(key.toString());
 	    if (s == null) 
 		registersToQuads.put(key.toString(), s = new LinkedList<Integer>());
 	    s.add(value.getID());
 	}
 
 	
 	private Set<Integer> rd_set;
         public static Set<Integer> universalSet;
         public MyDataflowObject() { rd_set = new TreeSet<Integer>(); }
 
 
         public void setToTop() {rd_set = new TreeSet<Integer>();}
 	public void setToBottom() {rd_set = new TreeSet<Integer>(universalSet);} 
         public void meetWith (Flow.DataflowObject o) {
             MyDataflowObject a = (MyDataflowObject)o;
 	    rd_set.addAll(a.rd_set);
 	}
         public void copy (Flow.DataflowObject o) {
 	    MyDataflowObject a = (MyDataflowObject) o;
             rd_set = new TreeSet<Integer>(a.rd_set);
 	}
 
         /**
          * toString() method for the dataflow objects which is used
          * by postprocess() below.  The format of this method must
          * be of the form "[ID0, ID1, ID2, ...]", where each ID is
          * the identifier of a quad defining some register, and the
          * list of IDs must be sorted.  See src/test/test.rd.out
          * for example output of the analysis.  The output format of
          * your reaching definitions analysis must match this exactly.
          */
         @Override
         public String toString() {
 	    return rd_set.toString(); 
 	}
         @Override
         public int hashCode() {
             return rd_set.hashCode();
         }
         @Override
         public boolean equals(Object o) 
         {
             if (o instanceof MyDataflowObject) 
             {
                 MyDataflowObject a = (MyDataflowObject) o;
 		return rd_set.equals(a.rd_set);
             }
             return false;
         }
         public void genDef(Quad q) {
 	    if(!q.getDefinedRegisters().isEmpty())
 		rd_set.add(q.getID());
 	}
         public void killDef(Quad q) {
 	    //get defined registers for q
 	    //get quads for this register
 	    //remove from the rd_set
 	    for (RegisterOperand def : q.getDefinedRegisters()){
 		List<Integer> temp= registersToQuads.get(def.getRegister().toString());
 		for (Iterator<Integer> i = temp.iterator( ); i.hasNext( ); ) {
 		    Integer qid = i.next( );
 		    rd_set.remove(qid);
 		}
 	    }
 	}
     }
 
     /**
      * Dataflow objects for the interior and entry/exit points
      * of the CFG. in[ID] and out[ID] store the entry and exit
      * state for the input and output of the quad with identifier ID.
      *
      * You are free to modify these fields, just make sure to
      * preserve the data printed by postprocess(), which relies on these.
      */
     private MyDataflowObject[] in, out;
     private MyDataflowObject entry, exit;
 
     /**
      * This method initializes the datflow framework.
      *
      * @param cfg  The control flow graph we are going to process.
      */
     public void preprocess(ControlFlowGraph cfg) {
         // this line must come first.
         System.out.println("Method: "+cfg.getMethod().getName().toString());
 
         // get the amount of space we need to allocate for the in/out arrays.
         QuadIterator qit = new QuadIterator(cfg);
 	Set<Integer> univ = new TreeSet<Integer>();
         int max = 0;
         while (qit.hasNext()) {
            int id = qit.next().getID();
             if (id > max) 
                 max = id;
	    univ.add(id);
         }
         max += 1;
 
 	MyDataflowObject.universalSet = univ;
 
         // allocate the in and out arrays.
         in = new MyDataflowObject[max];
         out = new MyDataflowObject[max];
 
         // initialize the contents of in and out.
         qit = new QuadIterator(cfg);
         while (qit.hasNext()) {
             int id = qit.next().getID();
             in[id] = new MyDataflowObject();
             out[id] = new MyDataflowObject();
         }
 
         // initialize the entry and exit points.
         entry = new MyDataflowObject();
         exit = new MyDataflowObject();
 	transferfn.val = new MyDataflowObject();
         /************************************************/
 
 	// populate the registersToQuads map
         MyDataflowObject valt = new MyDataflowObject();
         QuadIterator qitmap = new QuadIterator(cfg);
         while (qitmap.hasNext()) {
 	    Quad q = qitmap.next();
 	    for (RegisterOperand def : q.getDefinedRegisters())
 		valt.addToMap(def.getRegister(), q);
         }
 
     }
 
     /**
      * This method is called after the fixpoint is reached.
      * It must print out the dataflow objects associated with
      * the entry, exit, and all interior points of the CFG.
      * Unless you modify in, out, entry, or exit you shouldn't
      * need to change this method.
      *
      * @param cfg  Unused.
      */
     public void postprocess (ControlFlowGraph cfg) {
         System.out.println("entry: " + entry.toString());
         for (int i=0; i<in.length; i++) {
             if (in[i] != null) {
                 System.out.println(i + " in:  " + in[i].toString());
                 System.out.println(i + " out: " + out[i].toString());
             }
         }
         System.out.println("exit: " + exit.toString());
     }
 
     /**
      * Other methods from the Flow.Analysis interface.
      * See Flow.java for the meaning of these methods.
      * These need to be filled in.
      */
     public boolean isForward () { return true; }
     public Flow.DataflowObject getEntry() { 
         Flow.DataflowObject result = newTempVar();
         result.copy(entry); 
         return result;
     }
     public Flow.DataflowObject getExit() { 
         Flow.DataflowObject result = newTempVar();
         result.copy(exit); 
         return result;
     }
     public void setEntry(Flow.DataflowObject value) {entry.copy(value);}
     public void setExit(Flow.DataflowObject value) {exit.copy(value);}
     public Flow.DataflowObject getIn(Quad q) { 
        Flow.DataflowObject result = newTempVar();
        result.copy(in[q.getID()]); 
        return result;
     }
     public Flow.DataflowObject getOut(Quad q) { 
        Flow.DataflowObject result = newTempVar();
        result.copy(out[q.getID()]); 
        return result;
     }
     public void setIn(Quad q, Flow.DataflowObject value) {in[q.getID()].copy(value);}
     public void setOut(Quad q, Flow.DataflowObject value) {out[q.getID()].copy(value);}
     public Flow.DataflowObject newTempVar() { return new MyDataflowObject(); }
 
 
     private TransferFunction transferfn = new TransferFunction ();
     public void processQuad(Quad q) {
 	transferfn.val.copy(in[q.getID()]);
         transferfn.visitQuad(q);
 	out[q.getID()].copy(transferfn.val);
     }
 
     /* The QuadVisitor that actually does the computation */
     public static class TransferFunction extends QuadVisitor.EmptyVisitor {
         MyDataflowObject val;
         @Override
 	public void visitQuad(Quad q) {
 	    val.killDef(q);
 	    val.genDef(q);
 
 	}
     }
 }

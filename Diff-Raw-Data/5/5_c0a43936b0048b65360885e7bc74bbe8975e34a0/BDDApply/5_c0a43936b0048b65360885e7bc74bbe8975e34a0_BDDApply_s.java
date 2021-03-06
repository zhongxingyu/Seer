 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package djbdd;
 
 import djbdd.timemeasurer.TimeMeasurer;
 
 import java.util.*;
 
 /**
  * Apply operation for BDDs
  * Contains the apply computation.
  * @author diegoj
  */
 public class BDDApply {
     /** First bdd */
     private final BDD bdd1;
     
     /** Second bdd */
     private final BDD bdd2;
     
     /** Resulting bdd */
     BDD bdd;
     
     /** Operation index */
     private final int operation;
     
     /** Cache table **/
     HashMap<String,Vertex> G;
     
     /** AND logic operation key */
     public final static int OP_AND = 1;
     
     /** OR logic operation key */
     public final static int OP_OR = 2;
     
     /** IF logic operation key */
     public final static int OP_IF = 3;
     
     /** IFF logic operation key */
     public final static int OP_IFF = 4;
     
     /** NAND logic operation key */
     public final static int OP_NAND = 5;
     
     /** NOR logic operation key */
     public final static int OP_NOR = 6;
     
     /** Not implication operation key */
     public final static int OP_NOTIF = 7;
     
     /** Is different operation key */
     public final static int OP_ISDIFFERENT = 8;
     
     
     /**
      * Gets the operation from a human-readable string.
      * @param operation String that contains the operation in human-readable form.
      * @return int Integer key of the operation.
      */
     private static int getOperation(String operation)throws Exception{
          if(operation.equals("&&") || operation.equalsIgnoreCase("and") || operation.equalsIgnoreCase(".") || operation.equalsIgnoreCase("·"))
              return OP_AND;
          if(operation.equals("||") || operation.equalsIgnoreCase("or") || operation.equalsIgnoreCase("+"))
              return OP_OR;
          if(operation.equalsIgnoreCase("nor"))
              return OP_NOR;
          if(operation.equalsIgnoreCase("nand"))
              return OP_NAND;
          if(operation.equalsIgnoreCase("<=>") || operation.equalsIgnoreCase("iff") || operation.equalsIgnoreCase("<->"))
              return OP_IFF;
          if(operation.equalsIgnoreCase("!="))
              return OP_ISDIFFERENT;
          if(operation.equalsIgnoreCase("=>") || operation.equalsIgnoreCase("if") || operation.equalsIgnoreCase("->"))
              return OP_IF;
          if(operation.equalsIgnoreCase("!=>") || operation.equalsIgnoreCase("notif") || operation.equalsIgnoreCase("!->"))
              return OP_NOTIF;
          throw new Exception("Operator "+operation+" undefined");
     }
     
     /**
      * Construct the function string for the resulting BDD.
      * @return String Formula for the resulting BDD obtained of operating the other BDDs.
      */
     private String getFunction(){
         String function1 = bdd1.function.trim();
         String function2 = bdd2.function.trim();
         // Get the function based on the operation
         if (operation == OP_AND) {
             return function1 + " && " + function2;
         }
         if (operation == OP_OR) {
             return function1 + " || " + function2;
         }
         if (operation == OP_IFF) {
             return "(" + function1 + " || !(" + function2 + ")) && (!(" + function1 + ") || " + function2 + ")";
         }
         if (operation == OP_IF) {
             return "(!(" + function1 + ") || (" + function2 + "))";
         }
         if (operation == OP_NOR) {
             return "!(" + function1 + " || " + function2 + ")";
         }
         if (operation == OP_NAND) {
             return "!(" + function1 + " &&" + function2 + ")";
         }
         if (operation == OP_NOTIF) {
             return "(("+function1+ ") && !("+function2+"))";
         }
         if (operation == OP_ISDIFFERENT) {
             return "((" + function1 + ") && !(" + function2 + ")) || (!(" + function1 + ") && (" + function2 + "))";
         }
         // I don't want exceptions, only FATAL ERRORS
         System.err.println("BDDApply.getOperation: Operation '" + this.operation + "' not recognized");
         System.err.flush();
         System.exit(-1);
         return "Operator '" + operation + "' undefined in BDDApply.getFunction";
     }
     
     
     /**
      * Constructs an BDDApply object, container of the Andersen design of apply function.
      * @param operation Operation in human-readable form.
      * @param bdd1 First bdd operand.
      * @param bdd2 Second bdd operand.
      */
     public BDDApply(String operation, BDD bdd1, BDD bdd2)throws Exception{
         this.bdd1 = bdd1;
         this.bdd2 = bdd2;
         this.operation = BDDApply.getOperation(operation);
     }
     
     
     /**
      * Computes the operation between two leaf vertices.
      * @param v1 A leaf vertex.
      * @param v2 Another leaf vertex.
      * @return boolean Result of compute the operation between the vertex values.
      */
     private boolean op(Vertex v1, Vertex v2){
         boolean v1Value = v1.value();
         boolean v2Value = v2.value();
         if (this.operation == OP_AND) {
             return v1Value && v2Value;
         }
         if (this.operation == OP_OR) {
             return v1Value || v2Value;
         }
         if (this.operation == OP_IF) {
             return (!v1Value || v2Value);
         }
         if (this.operation == OP_IFF) {
             return (v1Value == v2Value);
         }
         if (this.operation == OP_NOR) {
             return !(v1Value || v2Value);
         }
         if (this.operation == OP_NAND) {
             return !(v1Value && v2Value);
         }
         if (this.operation == OP_NOTIF) {
             return (v1Value && !v2Value);
         }
         if (this.operation == OP_ISDIFFERENT) {
             return (v1Value != v2Value);
         }
         // I don't want exceptions, only FATAL ERRORS
         System.err.println("BDDApply.op: Operation '"+this.operation+"' not recognized");
         System.err.flush();
         System.exit(-1);
         return false;
     }
     
     /**
      * Core of the apply algorithm.
      * Computes recursively a operation between two BDDs.
      * @param v1 Vertex of BDD1.
      * @param v2 Vertex of BDD2.
      * @return Vertex Result of doing a recursive call to app.
      */
     private Vertex app(Vertex v1, Vertex v2){
         // Hash key of the computation of the subtree of these two vertices
         String key = "1-"+v1.index+"+2-"+v2.index;
         
         if( G.containsKey(key) ){
             return G.get(key);
         }
         
         if(v1.isLeaf() && v2.isLeaf())
         {
             if(this.op(v1,v2)){
                 return BDD.T.True;
             }
             return BDD.T.False;
         }
         
         int var = -1;
         Vertex low = null;
         Vertex high = null;
         // v1.index < v2.index
         if (!v1.isLeaf() && (v2.isLeaf() || v1.variable < v2.variable)) {
             var = v1.variable;
             low = this.app(v1.low(), v2);
             high = this.app(v1.high(), v2);
         } else if (v1.isLeaf() || v1.variable > v2.variable) {
             var = v2.variable;
             low = this.app(v1, v2.low());
             high = this.app(v1, v2.high());
         } else {
             var = v1.variable;
             low = this.app(v1.low(), v2.low());
             high = this.app(v1.high(), v2.high());
         }
 
        // Respect the non-redundant propierty:
         // "No vertex shall be one whose low and high indices are the same."
         if(low.index == high.index){
             return low;
         }
         
        // Respect the uniqueness propierty:
         // "No vertex shall be one that contains same variable, low, high indices as other."
         Vertex u = BDD.T.add(var, low, high);
         this.G.put(key, u);
         return u;
     }
     
     /**
      * Public call to execute the apply algorithm.
      * Note: the first BDD1 is MODIFIED.
      * The returned BDD is the same as BDD1, it is keept to don't break anything.
      * @return BDD BDD Tree with the operatian computed for bdd1 and bdd2.
      */
     public BDD run(){
         TimeMeasurer t = new TimeMeasurer("========= apply =========");
         
         // Cache to avoid repeated computations
         this.G = new HashMap<String,Vertex>();
         
         String function = this.getFunction();
         
         // Fill this.T with vertices of bdd1 and bdd2
         Vertex root = this.app(bdd1.root(), bdd2.root());
         
         // We get the variable indices present in both BDDs as fast as we can
         /*
         HashSet<Integer> presentIndicesSet = new HashSet<Integer>(bdd1.variable_ordering.size()+bdd2.variable_ordering.size());
         presentIndicesSet.addAll(bdd1.variable_ordering);
         presentIndicesSet.addAll(bdd2.variable_ordering);
         ArrayList<Integer> presentVariableIndices = new ArrayList<Integer>(presentIndicesSet);
         */
         
         // Construction of new BDD
         this.bdd = new BDD(function, root);
         t.end().show();
         
         // Return the new BDD computed
         return this.bdd;
     }
     
     
 }

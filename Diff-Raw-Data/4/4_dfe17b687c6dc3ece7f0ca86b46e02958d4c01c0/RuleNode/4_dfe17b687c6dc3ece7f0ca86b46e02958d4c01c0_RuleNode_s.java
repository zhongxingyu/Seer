 package uk.co.mtford.jalp.abduction.rules;
 
 import choco.kernel.model.constraints.Constraint;
 import uk.co.mtford.jalp.JALPException;
 import uk.co.mtford.jalp.abduction.AbductiveFramework;
 import uk.co.mtford.jalp.abduction.DefinitionException;
 import uk.co.mtford.jalp.abduction.Store;
 import uk.co.mtford.jalp.abduction.logic.instance.*;
 import uk.co.mtford.jalp.abduction.logic.instance.constraints.ChocoConstraintSolverFacade;
 import uk.co.mtford.jalp.abduction.logic.instance.constraints.IConstraintInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.equalities.IEqualityInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.term.VariableInstance;
 import uk.co.mtford.jalp.abduction.rules.visitor.RuleNodeVisitor;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: mtford
  * Date: 18/05/2012
  * Time: 06:40
  * To change this template use File | Settings | File Templates.
  */
 public abstract class RuleNode {
 
     public static enum NodeMark {
         FAILED,
         SUCCEEDED,
         UNEXPANDED,
         EXPANDED
     };
 
     protected IInferableInstance currentGoal;
     protected List<IInferableInstance> nextGoals; // G - {currentGoal}
     protected Store store; // ST
     protected Map<VariableInstance, IUnifiableAtomInstance> assignments;  // Theta
     protected AbductiveFramework abductiveFramework; // (P,A,IC),Theta
     protected NodeMark nodeMark; // Defines whether or not leaf node or search node.
     protected List<RuleNode> children; // Next states.
     protected ChocoConstraintSolverFacade constraintSolver;
 
     public RuleNode(AbductiveFramework abductiveFramework, IInferableInstance goal, List<IInferableInstance> restOfGoals) {
         children = new LinkedList<RuleNode>();
         assignments = new HashMap<VariableInstance, IUnifiableAtomInstance>();
         nodeMark = nodeMark.UNEXPANDED;
         this.abductiveFramework = abductiveFramework;
         this.currentGoal = goal;
         this.nextGoals = restOfGoals;
         this.constraintSolver=new ChocoConstraintSolverFacade();
         store = new Store();
     }
 
     public RuleNode(AbductiveFramework abductiveFramework, IInferableInstance goal, List<IInferableInstance> restOfGoals,
                     Store store, Map<VariableInstance, IUnifiableAtomInstance> assignments) {
         children = new LinkedList<RuleNode>();
         this.assignments = assignments;
         this.store = store;
         this.abductiveFramework = abductiveFramework;
         this.currentGoal = goal;
         this.nextGoals = restOfGoals;
         this.constraintSolver=new ChocoConstraintSolverFacade();
         this.nodeMark = nodeMark.UNEXPANDED;
 
     }
 
     protected RuleNode() {
         nodeMark = nodeMark.UNEXPANDED;
     } // For use whilst cloning.
 
     public ChocoConstraintSolverFacade getConstraintSolver() {
         return constraintSolver;
     }
 
     public void setConstraintSolver(ChocoConstraintSolverFacade constraintSolver) {
         this.constraintSolver = constraintSolver;
     }
 
     public List<RuleNode> getChildren() {
         return children;
     }
 
     public void setChildren(List<RuleNode> children) {
         this.children = children;
     }
 
     public Map<VariableInstance, IUnifiableAtomInstance> getAssignments() {
         return assignments;
     }
 
     public void setAssignments(Map<VariableInstance, IUnifiableAtomInstance> assignments) {
         this.assignments = assignments;
     }
 
     public NodeMark getNodeMark() {
         return nodeMark;
     }
 
     public void setNodeMark(NodeMark nodeMark) {
         this.nodeMark = nodeMark;
     }
 
     public AbductiveFramework getAbductiveFramework() {
         return abductiveFramework;
     }
 
     public void setAbductiveFramework(AbductiveFramework abductiveFramework) {
         this.abductiveFramework = abductiveFramework;
     }
 
     public Store getStore() {
         return store;
     }
 
     public void setStore(Store store) {
         this.store = store;
     }
 
     public List<IInferableInstance> getNextGoals() {
         return nextGoals;
     }
 
     public void setNextGoals(List<IInferableInstance> nextGoals) {
         this.nextGoals = nextGoals;
     }
 
     public IInferableInstance getCurrentGoal() {
         return currentGoal;
     }
 
     public void setCurrentGoal(IInferableInstance currentGoal) {
         this.currentGoal = currentGoal;
     }
 
     /** Equality solver implementation **/
     public Map<VariableInstance, IUnifiableAtomInstance> equalitySolve()  {
         Map<VariableInstance, IUnifiableAtomInstance> newAssignments
                 = new HashMap<VariableInstance, IUnifiableAtomInstance>(assignments);
 
         List<IEqualityInstance> equalities = new LinkedList<IEqualityInstance>(this.getStore().equalities);
 
         for (IEqualityInstance equality:equalities) {
             if (equality.equalitySolve(newAssignments)) continue;
             return null;  // TODO null... really?
         }
 
         return newAssignments;
     }
 
     public List<Map<VariableInstance, IUnifiableAtomInstance>> constraintSolve() {
         if (store.constraints.isEmpty()) {
             List<Map<VariableInstance,IUnifiableAtomInstance>> possSubst
                     = new LinkedList<Map<VariableInstance, IUnifiableAtomInstance>>();
             possSubst.add(assignments);
             return possSubst;
         }
         LinkedList<IConstraintInstance> constraints = new LinkedList<IConstraintInstance>();
         for (IConstraintInstance d:store.constraints) {
            constraints.add((IConstraintInstance) d.performSubstitutions(assignments));
         }
         List<Map<VariableInstance,IUnifiableAtomInstance>> possSubst
                 = constraintSolver.executeSolver(new HashMap<VariableInstance,IUnifiableAtomInstance>(assignments),constraints);
         return possSubst;
     }
 
     public abstract RuleNode shallowClone();
 
     public abstract void acceptVisitor(RuleNodeVisitor v) throws Exception;
 
     @Override
     public String toString() {
         String message =
                 "currentGoal = " + currentGoal + "\n" +
                 "nextGoals = " + nextGoals + "\n" +
                 "assignments = " + assignments + "\n\n" +
                 "delta = " + store.abducibles + "\n" +
                 "delta* = " + store.denials + "\n" +
                 "epsilon = " + store.equalities + "\n" +
                 "fd = " + store.constraints + "\n\n" +
                 "chocoFd = " + constraintSolver.getChocoConstraints() + "\n\n" +
                 "nodeType = " + this.getClass() + "\n" +
                 "nodeMark = " + this.getNodeMark() + "\n" +
                 "numChildren = " + this.getChildren().size();
         return message;
     }
 
     public String toJSON()  {
         String type[] = this.getClass().toString().split("\\.");
 
         String json="{";
 
         json+="\\\"type\\\":"+"\\\""+type[type.length-1]+"\\\"";
 
         json+=",";
 
         json+="\\\"currentGoal\\\":"+"\\\""+currentGoal+"\\\"";
 
         json+=",";
 
         json+="\\\"nextGoals\\\""+":[ ";
         for (IInferableInstance inferable:nextGoals) {
             json+="\\\""+inferable+"\\\",";
         }
         json=json.substring(0,json.length()-1);
         json+="]";
 
         json+=",";
 
 
         json+="\\\"assignments\\\""+":{ ";
         for (VariableInstance v:assignments.keySet()) {
             json+="\\\""+v+"\\\""+":"+"\\\""+assignments.get(v)+"\\\""+",";
         }
         json=json.substring(0,json.length()-1);
         json+="}";
 
         json+=",";
 
         json+="\\\"abducibles\\\""+":[ ";
         for (PredicateInstance abducible:store.abducibles) {
             json+="\\\""+abducible+"\\\",";
         }
         json=json.substring(0,json.length()-1);
         json+="]";
 
         json+=",";
 
         json+="\\\"denials\\\""+":[ ";
         for (DenialInstance denial:store.denials) {
             json+="\\\""+denial+"\\\",";
         }
         json=json.substring(0,json.length()-1);
         json+="]";
 
         json+=",";
 
         json+="\\\"equalities\\\""+":[ ";
         for (IEqualityInstance equalities:store.equalities) {
             json+="\\\""+equalities+"\\\",";
         }
         json=json.substring(0,json.length()-1);
         json+="]";
 
         json+=",";
 
         json+="\\\"constraints\\\""+":[ ";
         for (IConstraintInstance constraint:store.constraints) {
             json+="\\\""+constraint+"\\\",";
         }
         for (Constraint constraint:constraintSolver.getChocoConstraints()) {
             json+="\\\""+constraint+"\\\",";
         }
         json=json.substring(0,json.length()-1);
         json+="]";
 
         json+=",";
 
         json+="\\\"mark\\\":"+"\\\""+nodeMark+"\\\"";
 
         json+=",";
 
         json+="\\\"children\\\""+":[ ";
         for (RuleNode child:children) {
             json+=child.toJSON()+",";
         }
         json=json.substring(0,json.length()-1);
         json+="]";
 
         json+="}";
 
         return json;
     }
 }

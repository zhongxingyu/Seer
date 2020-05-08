 package uk.co.mtford.jalp.abduction.logic.instance.constraints;
 
 import choco.kernel.model.constraints.Constraint;
 import choco.kernel.model.variables.Variable;
 import uk.co.mtford.jalp.abduction.AbductiveFramework;
 import uk.co.mtford.jalp.abduction.logic.instance.*;
 import uk.co.mtford.jalp.abduction.logic.instance.term.ITermInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.term.VariableInstance;
 import uk.co.mtford.jalp.abduction.rules.RuleNode;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Created with IntelliJ IDEA.
  * User: mtford
  * Date: 24/05/2012
  * Time: 16:37
  * To change this template use File | Settings | File Templates.
  */
 public class NegativeConstraintInstance implements IConstraintInstance {
      private ConstraintInstance constraintInstance;
 
     public NegativeConstraintInstance(ConstraintInstance constraintInstance) {
         this.constraintInstance = constraintInstance;
     }
 
     @Override
     public RuleNode getPositiveRootRuleNode(AbductiveFramework abductiveFramework, List<IInferableInstance> goals) {
         throw new UnsupportedOperationException(); // TODO Again ASystem paper may suggest rules to deal with negatives constraints?
     }
 
     @Override
     public RuleNode getNegativeRootRuleNode(AbductiveFramework abductiveFramework, List<DenialInstance> nestedDenials, List<IInferableInstance> goals) {
         throw new UnsupportedOperationException(); // TODO Again ASystem paper may suggest rules to deal with negatives constraints?
     }
 
     @Override
     public IFirstOrderLogicInstance performSubstitutions(Map<VariableInstance, IUnifiableAtomInstance> substitutions) {
        return new NegativeConstraintInstance((ConstraintInstance)constraintInstance.performSubstitutions(substitutions));
     }
 
     @Override
     public IFirstOrderLogicInstance deepClone(Map<VariableInstance, IUnifiableAtomInstance> substitutions) {
         return new NegativeConstraintInstance((ConstraintInstance)constraintInstance.deepClone(substitutions));
     }
 
     @Override
     public IFirstOrderLogicInstance shallowClone() {
        return new NegativeConstraintInstance(constraintInstance);
     }
 
     @Override
     public Set<VariableInstance> getVariables() {
         return constraintInstance.getVariables();
     }
 
     @Override
     public String toString () {
         return "not " + constraintInstance;
     }
 
     @Override
     public boolean reduceToChoco(List<Map<VariableInstance, IUnifiableAtomInstance>> possSubst, List<Constraint> chocoConstraints, HashMap<ITermInstance, Variable> chocoVariables, HashMap<Constraint,IConstraintInstance> constraintMap) {
         return constraintInstance.reduceToNegativeChoco(possSubst, chocoConstraints,chocoVariables,constraintMap);
     }
 
     @Override
     public boolean reduceToNegativeChoco(List<Map<VariableInstance, IUnifiableAtomInstance>> possSubst, List<Constraint> chocoConstraints, HashMap<ITermInstance, Variable> chocoVariables, HashMap<Constraint,IConstraintInstance> constraintMap) {
         return constraintInstance.reduceToChoco(possSubst, chocoConstraints,chocoVariables,constraintMap);
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof NegativeConstraintInstance)) return false;
 
         NegativeConstraintInstance that = (NegativeConstraintInstance) o;
 
         if (!constraintInstance.equals(that.constraintInstance)) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return constraintInstance.hashCode();
     }
 }

 package uk.co.mtford.jalp.abduction.logic.instance.constraints;
 
 import choco.kernel.model.constraints.Constraint;
 import choco.kernel.model.variables.Variable;
 import choco.kernel.model.variables.integer.IntegerVariable;
 import choco.kernel.model.variables.set.SetVariable;
 import uk.co.mtford.jalp.abduction.logic.instance.IFirstOrderLogicInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.term.ITermInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.IUnifiableAtomInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.term.IntegerConstantListInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.term.VariableInstance;
 
 import java.util.*;
 
 import static choco.Choco.*;
 import static choco.Choco.makeIntVar;
 
 /**
  * Created with IntelliJ IDEA.
  * User: mtford
  * Date: 30/05/2012
  * Time: 12:06
  * To change this template use File | Settings | File Templates.
  */
 public class InIntegerListConstraintInstance extends InListConstraintInstance {
 
     public InIntegerListConstraintInstance(ITermInstance left, IntegerConstantListInstance right) {
         super(left, right);
     }
 
     @Override
     public IFirstOrderLogicInstance performSubstitutions(Map<VariableInstance, IUnifiableAtomInstance> substitutions) {
        return new InIntegerListConstraintInstance((ITermInstance)left.performSubstitutions(substitutions), (IntegerConstantListInstance) right.performSubstitutions(substitutions));
     }
 
     @Override
     public IFirstOrderLogicInstance deepClone(Map<VariableInstance, IUnifiableAtomInstance> substitutions) {
         return new InIntegerListConstraintInstance((ITermInstance)left.deepClone(substitutions), (IntegerConstantListInstance)right.deepClone(substitutions));
     }
 
     @Override
     public IFirstOrderLogicInstance shallowClone() {
        return new InIntegerListConstraintInstance((ITermInstance)left,(IntegerConstantListInstance)right);
     }
 
     @Override
     public boolean reduceToChoco(List<Map<VariableInstance, IUnifiableAtomInstance>> possSubst, List<Constraint> chocoConstraints, HashMap<ITermInstance, Variable> chocoVariables, HashMap<Constraint,IConstraintInstance> constraintMap) {
         left.reduceToChoco(possSubst,chocoVariables);
         IntegerVariable leftVar = (IntegerVariable) chocoVariables.get(left);
         IntegerConstantListInstance rightVar = (IntegerConstantListInstance) right;
         int[] leftDomainArray = leftVar.getValues();
         int[] rightDomainArray = rightVar.getIntArray();
         IntegerVariable newLeftVar;
         if (leftDomainArray==null) {
             newLeftVar = makeIntVar(leftVar.getName(),rightDomainArray);
         }
         else {
             Set<Integer> leftDomainSet = new HashSet<Integer>();
             Set<Integer> rightDomainSet = new HashSet<Integer>();
             for (int i:leftDomainArray) leftDomainSet.add(i);
             for (int i:rightDomainArray) rightDomainSet.add(i);
             leftDomainSet.retainAll(rightDomainSet);
             if (leftDomainSet.isEmpty()) return false;
             List<Integer> newDomain = new LinkedList<Integer>(leftDomainSet);
             newLeftVar = makeIntVar(leftVar.getName(), newDomain);
         }
 
         for (Constraint c:chocoConstraints) {
             c.replaceBy(leftVar,newLeftVar);
         }
 
         chocoVariables.put(left,newLeftVar);
 
         return true;
     }
 
     @Override
     public boolean reduceToNegativeChoco(List<Map<VariableInstance, IUnifiableAtomInstance>> possSubst, List<Constraint> chocoConstraints, HashMap<ITermInstance, Variable> chocoVariables, HashMap<Constraint,IConstraintInstance> constraintMap) { // TODO messy
         left.reduceToChoco(possSubst,chocoVariables);
         IntegerVariable leftVar = (IntegerVariable) chocoVariables.get(left);
         IntegerConstantListInstance rightVar = (IntegerConstantListInstance) right;
         int[] leftDomainArray = leftVar.getValues();
         int[] rightDomainArray = rightVar.getIntArray();
 
         Set<Integer> leftDomainSet = new HashSet<Integer>();
         Set<Integer> rightDomainSet = new HashSet<Integer>();
         for (int i:leftDomainArray) leftDomainSet.add(i);
         for (int i:rightDomainArray) rightDomainSet.add(i);
         leftDomainSet.removeAll(rightDomainSet);
         if (leftDomainSet.isEmpty()) return false;
         List<Integer> newDomain = new LinkedList<Integer>(leftDomainSet);
 
         IntegerVariable newLeftVar = makeIntVar(leftVar.getName(), newDomain);
 
         for (Constraint c:chocoConstraints) {
             c.replaceBy(leftVar,newLeftVar);
         }
 
         chocoVariables.put(left,newLeftVar);
 
 
         return true;
 
 
 
     }
 
 
 
 
 }

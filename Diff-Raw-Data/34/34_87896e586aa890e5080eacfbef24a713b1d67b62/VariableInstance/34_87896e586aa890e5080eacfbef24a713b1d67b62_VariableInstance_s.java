 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package uk.co.mtford.jalp.abduction.logic.instance.term;
 
 import choco.kernel.model.variables.Variable;
 import choco.kernel.model.variables.integer.IntegerVariable;
 import org.apache.log4j.Logger;
 import uk.co.mtford.jalp.abduction.AbductiveFramework;
 import uk.co.mtford.jalp.abduction.logic.instance.*;
 import uk.co.mtford.jalp.abduction.logic.instance.equalities.EqualityInstance;
 import uk.co.mtford.jalp.abduction.rules.F2RuleNode;
 import uk.co.mtford.jalp.abduction.rules.F2bRuleNode;
 import uk.co.mtford.jalp.abduction.rules.RuleNode;
 import uk.co.mtford.jalp.abduction.tools.UniqueIdGenerator;
 
 import java.util.*;
 
 import static choco.Choco.makeIntVar;
 import static choco.Choco.makeSetVar;
 
 /**
  * @author mtford
  */
 public class VariableInstance implements ITermInstance, IUnifiableAtomInstance {
 
     private static final Logger LOGGER = Logger.getLogger(VariableInstance.class);
     private int uniqueId;
 
     String name;
 
     public int getUniqueId() {
         return uniqueId;
     }
 
     public VariableInstance(String name) {
         this.name = name;
         uniqueId = UniqueIdGenerator.getUniqueId(name);
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     @Override
     public String toString() {
         if (uniqueId==1) {
             return name;
         }
         else {
             return name + uniqueId;
         }
 
     }
 
     /**
      * Returns true if variable names at the same. Not concerned with value.
      */
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 89 * hash + (this.name != null ? this.name.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof VariableInstance)) return false;
 
         VariableInstance that = (VariableInstance) o;
 
         if (uniqueId != that.uniqueId) return false;
         if (!name.equals(that.name)) return false;
 
         return true;
     }
 
     @Override
     public IFirstOrderLogicInstance performSubstitutions(Map<VariableInstance, IUnifiableAtomInstance> substitutions) {
         if (substitutions.containsKey(this)) {
             return substitutions.get(this).performSubstitutions(substitutions);
         } else {
             return this;
         }
     }
 
     @Override
     public IFirstOrderLogicInstance deepClone(Map<VariableInstance, IUnifiableAtomInstance> substitutions) {
         if (substitutions.containsKey(this)) {
             if (substitutions.get(this).equals(this)) {
                 return this;
             }
             return substitutions.get(this).performSubstitutions(substitutions);
         } else {
             VariableInstance clonedVariable = new VariableInstance(new String(name));
             substitutions.put(this, clonedVariable);
             return clonedVariable;
         }
     }
 
     @Override
     public IFirstOrderLogicInstance shallowClone() {
        return new VariableInstance(name);
     }
 
     @Override
     public Set<VariableInstance> getVariables() {
         HashSet<VariableInstance> variables = new HashSet<VariableInstance>();
         variables.add(this);
         return variables;
     }
 
     @Override
     public List<EqualityInstance> reduce(VariableInstance other) {
         return new LinkedList<EqualityInstance>();
     }
 
     @Override
     public List<EqualityInstance> reduce(ConstantInstance other) {
         return new LinkedList<EqualityInstance>();
     }
 
     @Override
     public List<EqualityInstance> reduce(PredicateInstance other) {
         return new LinkedList<EqualityInstance>();
     }
 
     @Override
     public List<EqualityInstance> reduce(IUnifiableAtomInstance other) {
         return new LinkedList<EqualityInstance>();
     }
 
     @Override
     public List<EqualityInstance> acceptReduceVisitor(IUnifiableAtomInstance unifiableAtom) {
         return unifiableAtom.reduce(this);
     }
 
     @Override
     public boolean unify(VariableInstance other, Map<VariableInstance, IUnifiableAtomInstance> assignment) {
         List<IUnifiableAtomInstance> keySet = new LinkedList<IUnifiableAtomInstance>(assignment.keySet());
         IUnifiableAtomInstance left = this;
         IUnifiableAtomInstance right = other;
         if (this.equals(other)) return true;
         if (keySet.contains(left)) {
             while (keySet.contains(left)) left = assignment.get(left);
             return left.unify(right,assignment);
         }
         else if (keySet.contains(right)) {
             while (keySet.contains(right)) right = assignment.get(right);
             return left.unify(right,assignment);
         }
         else {
             assignment.put(this,other);
         }
         return true;
     }
 
     @Override
     public boolean unify(ConstantInstance other, Map<VariableInstance, IUnifiableAtomInstance> assignment) {
         List<IUnifiableAtomInstance> keySet = new LinkedList<IUnifiableAtomInstance>(assignment.keySet());
         IUnifiableAtomInstance left = this;
         if (keySet.contains(left)) {
             while (keySet.contains(left)) left = assignment.get(left);
             return left.unify(other,assignment);
         }
         else {
             assignment.put(this,other);
         }
         return true;
     }
 
     @Override
     public boolean unify(PredicateInstance other, Map<VariableInstance, IUnifiableAtomInstance> assignment) {
         List<IUnifiableAtomInstance> keySet = new LinkedList<IUnifiableAtomInstance>(assignment.keySet());
         IUnifiableAtomInstance left = this;
         if (keySet.contains(left)) {
             while (keySet.contains(left)) left = assignment.get(left);
             return left.unify(other,assignment);
         }
         else {
             assignment.put(this,other);
         }
         return true;
     }
 
     @Override
     public boolean unify(IUnifiableAtomInstance other, Map<VariableInstance, IUnifiableAtomInstance> assignment) {
         return other.acceptUnifyVisitor(this,assignment);
     }
 
     @Override
     public boolean acceptUnifyVisitor(IUnifiableAtomInstance unifiableAtom, Map<VariableInstance, IUnifiableAtomInstance> assignment) {
         return unifiableAtom.unify(this,assignment);
     }
 
     @Override
     public boolean reduceToChoco(List<Map<VariableInstance, IUnifiableAtomInstance>> possSubst, HashMap<ITermInstance, Variable> termToVarMap) {
         if (!termToVarMap.containsKey(this)) {
             IntegerVariable var = makeIntVar(name+uniqueId);
             termToVarMap.put(this,var);
         }
         return true;
     }
 
     @Override
     public boolean inList(CharConstantListInstance constantList, List<Map<VariableInstance, IUnifiableAtomInstance>> possSubst) {
         if (possSubst.isEmpty()) {
             Map<VariableInstance, IUnifiableAtomInstance> subst = new HashMap<VariableInstance, IUnifiableAtomInstance>();
             possSubst.add(subst);
         }
         List<Map<VariableInstance, IUnifiableAtomInstance>> newSubstList = new LinkedList<Map<VariableInstance, IUnifiableAtomInstance>>();
         for (Map<VariableInstance,IUnifiableAtomInstance> subst:possSubst) {
             for (CharConstantInstance constant:constantList.getList()) {
                 HashMap<VariableInstance,IUnifiableAtomInstance> newSubst = new HashMap<VariableInstance, IUnifiableAtomInstance>(subst);
                 boolean unificationSuccess = this.unify(constant,newSubst);
                 if (unificationSuccess) newSubstList.add(newSubst);
             }
         }
         if (newSubstList.isEmpty()) return false;
         else {
             while (!possSubst.isEmpty()) possSubst.remove(0);
             possSubst.addAll(newSubstList);
             return true;
         }
     }
 
     @Override
     public RuleNode getNegativeRootRuleNode(IInferableInstance newGoal, AbductiveFramework abductiveFramework, List<DenialInstance> nestedDenials, List<IInferableInstance> goals) {
         if (nestedDenials.get(0).getUniversalVariables().contains(this)) {
             return new F2bRuleNode(abductiveFramework,newGoal,goals,nestedDenials);
         }
         else {
             return new F2RuleNode(abductiveFramework,newGoal,goals,nestedDenials);
         }
     }
 
 }

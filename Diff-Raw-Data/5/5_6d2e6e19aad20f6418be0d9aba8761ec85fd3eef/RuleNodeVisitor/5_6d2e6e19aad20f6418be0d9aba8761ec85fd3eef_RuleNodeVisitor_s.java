 package uk.co.mtford.jalp.abduction.rules.visitor;
 
 import org.apache.log4j.Logger;
 import uk.co.mtford.jalp.abduction.DefinitionException;
 import uk.co.mtford.jalp.abduction.Store;
 import uk.co.mtford.jalp.abduction.logic.instance.*;
 import uk.co.mtford.jalp.abduction.logic.instance.constraints.ConstraintInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.constraints.IConstraintInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.constraints.NegativeConstraintInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.equalities.EqualityInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.equalities.InEqualityInstance;
 import uk.co.mtford.jalp.abduction.rules.*;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: mtford
  * Date: 18/05/2012
  * Time: 06:44
  * To change this template use File | Settings | File Templates.
  */
 public abstract class RuleNodeVisitor {
 
     private static final Logger LOGGER = Logger.getLogger(RuleNodeVisitor.class);
 
     protected RuleNode currentRuleNode;
     protected LinkedList<LeafRuleNode> leafRuleNodes;
 
     public RuleNodeVisitor(RuleNode ruleNode) throws DefinitionException {
         currentRuleNode = ruleNode;
         leafRuleNodes = new LinkedList<LeafRuleNode>();
     }
 
     public LinkedList<LeafRuleNode> getLeafRuleNodes() {
         return leafRuleNodes;
     }
 
     private RuleNode constructPositiveChildNode(IInferableInstance newGoal, List<IInferableInstance> newRestOfGoals,
                                                 RuleNode previousNode) {
         RuleNode newRuleNode;
         if (!(newGoal==null)) {
             newRuleNode = newGoal.getPositiveRootRuleNode(previousNode.getAbductiveFramework(), newRestOfGoals);
             newRuleNode.setStore(previousNode.getStore().shallowClone());
             newRuleNode.setAssignments(new HashMap<VariableInstance, IUnifiableAtomInstance>(previousNode.getAssignments()));
         }
         else {
             Map<VariableInstance, IUnifiableAtomInstance> assignments = new HashMap<VariableInstance, IUnifiableAtomInstance>(previousNode.getAssignments());
             newRuleNode = new LeafRuleNode(previousNode.getAbductiveFramework(),previousNode.getStore().shallowClone(),assignments);
         }
 
         return newRuleNode;
     }
 
     private RuleNode constructNegativeChildNode(IInferableInstance newGoal, List<DenialInstance> nestedDenialList,
                                                 List<IInferableInstance> newRestOfGoals, RuleNode previousNode) {
         newRestOfGoals = new LinkedList<IInferableInstance>(newRestOfGoals);
         RuleNode newRuleNode = newGoal.getNegativeRootRuleNode(previousNode.getAbductiveFramework(), nestedDenialList, newRestOfGoals);
         Map<VariableInstance, IUnifiableAtomInstance> assignments = new HashMap<VariableInstance, IUnifiableAtomInstance>(previousNode.getAssignments());
         newRuleNode.setAssignments(assignments);
         newRuleNode.setStore(previousNode.getStore().shallowClone());
         return newRuleNode;
     }
 
     public void visit(A1RuleNode ruleNode) {
         if (LOGGER.isInfoEnabled()) LOGGER.info("Applying A1 to node.");
         // 1st Branch: Unify with an already collected abducible.
         LinkedList<RuleNode> childNodes = new LinkedList<RuleNode>();
         Store store = ruleNode.getStore();
         PredicateInstance goalAbducible = (PredicateInstance) ruleNode.getCurrentGoal();
         LinkedList<RuleNode> firstBranchChildNodes = getA1FirstBranch(ruleNode, store, goalAbducible);
         childNodes.addAll(0,firstBranchChildNodes);
 
         // Second branch: Add a new abducible. Check satisfies collected nestedDenialsList. Check not possible to unifyLeftRight with any existing.
         RuleNode secondBranchChildNode = getA1SecondBranch(ruleNode, store, goalAbducible);
         childNodes.add(secondBranchChildNode);
         if (LOGGER.isInfoEnabled()) LOGGER.info("A1 generated "+childNodes.size()+" new states.");
         ruleNode.getChildren().addAll(0,childNodes);
         ruleNode.setNodeMark(RuleNode.NodeMark.EXPANDED);
     }
 
     private LinkedList<RuleNode> getA1FirstBranch(A1RuleNode ruleNode, Store store, PredicateInstance goalAbducible) {
         LinkedList<RuleNode> childNodes = new LinkedList<RuleNode>();
         for (PredicateInstance storeAbducible : store.abducibles) {
             if (goalAbducible.isSameFunction(storeAbducible)) {
                 List<IInferableInstance> equalitySolved = new LinkedList<IInferableInstance>(storeAbducible.reduce(goalAbducible));
                 List<IInferableInstance> newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
                 IInferableInstance newGoal = equalitySolved.remove(0);
                 newRestOfGoals.addAll(0,equalitySolved);
                 RuleNode childNode = constructPositiveChildNode(newGoal, newRestOfGoals, ruleNode);
                 childNodes.add(childNode);
             }
         }
         return childNodes;
     }
 
     private RuleNode getA1SecondBranch(A1RuleNode ruleNode, Store store, PredicateInstance goalAbducible) {
         // Set up new child node and it's data structures.
         RuleNode childNode;
         List<IInferableInstance> newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
 
         // Check our new collected abducible doesn't violate any collected constraints.
         for (DenialInstance collectedDenial : store.denials) {
             PredicateInstance collectedDenialHead = (PredicateInstance) collectedDenial.getBody().get(0);
             if (collectedDenialHead.isSameFunction(goalAbducible)) {
                 DenialInstance newDenial = (DenialInstance) collectedDenial.deepClone(new HashMap<VariableInstance, IUnifiableAtomInstance>(ruleNode.getAssignments()));
                 collectedDenialHead = (PredicateInstance) newDenial.getBody().remove(0);
                 newDenial.getBody().addAll(0,goalAbducible.reduce(collectedDenialHead));
                 newRestOfGoals.add(0, newDenial);
             }
         }
 
         // Check our new collected abducible won't unifyLeftRight with any already collected abducibles.
         for (PredicateInstance storeAbducible : store.abducibles) {
             if (goalAbducible.isSameFunction(storeAbducible)) {
                 List<EqualityInstance> equalitySolved = goalAbducible.reduce(storeAbducible);
                 for (EqualityInstance result : equalitySolved) {
                     newRestOfGoals.add(0,new NegationInstance(result));
                 }
             }
         }
 
         IInferableInstance newGoal = null;
         if (!newRestOfGoals.isEmpty()) newGoal = newRestOfGoals.remove(0);
 
         childNode = constructPositiveChildNode(newGoal, newRestOfGoals, ruleNode);
         childNode.getStore().abducibles.add(goalAbducible);
 
         return childNode;
     }
 
     public void visit(A2RuleNode ruleNode) {
        if (LOGGER.isInfoEnabled()) LOGGER.info("Applying A2 to node:\n"+ruleNode);
 
         // Set up new child nodes data structures.
         RuleNode childNode;
         List<IInferableInstance> newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
         List<DenialInstance> newNestedDenialList = new LinkedList<DenialInstance>(ruleNode.getNestedDenialsList());
         DenialInstance newCurrentDenial = newNestedDenialList.remove(0).shallowClone();
 
         PredicateInstance currentGoal = (PredicateInstance) ruleNode.getCurrentGoal();
 
         // Check that the new constraint we are creating isn't instantly violated by an already collected abducible.
         newCurrentDenial.getBody().add(0, currentGoal);
         Store store = ruleNode.getStore();
         for (PredicateInstance storeAbducible : store.abducibles) {
             if (storeAbducible.isSameFunction(currentGoal)) {
                 DenialInstance newDenial = (DenialInstance) newCurrentDenial.deepClone(new HashMap<VariableInstance, IUnifiableAtomInstance>(ruleNode.getAssignments()));
                 PredicateInstance newDenialHead = (PredicateInstance) newDenial.getBody().remove(0);
                 List<EqualityInstance> equalitySolved = newDenialHead.reduce(storeAbducible);
                 newDenial.getBody().addAll(0,equalitySolved);
                 if (!newNestedDenialList.isEmpty()) newNestedDenialList.get(0).getBody().add(0,newDenial);
                 else newRestOfGoals.add(0,newDenial);
             }
         }
 
         if (newNestedDenialList.isEmpty()) {
             IInferableInstance newGoal = null;
             if (!newRestOfGoals.isEmpty()) newGoal = newRestOfGoals.remove(0);
             childNode = constructPositiveChildNode(newGoal,newRestOfGoals,ruleNode);
         }
         else {
             IInferableInstance newGoal = newNestedDenialList.get(0).getBody().remove(0);
             childNode = constructNegativeChildNode(newGoal,newNestedDenialList,newRestOfGoals,ruleNode);
         }
 
         childNode.getStore().denials.add(newCurrentDenial);
 
         ruleNode.getChildren().add(childNode);
         ruleNode.setNodeMark(RuleNode.NodeMark.EXPANDED);
     }
 
     public void visit(D1RuleNode ruleNode) throws DefinitionException {
         if (LOGGER.isInfoEnabled()) LOGGER.info("Applying D1 to node.");
         PredicateInstance definedPredicate = (PredicateInstance) ruleNode.getCurrentGoal();
         List<List<IInferableInstance>> possibleUnfolds = ruleNode.getAbductiveFramework().unfoldDefinitions(definedPredicate);
         LinkedList<RuleNode> childNodes = new LinkedList<RuleNode>();
         for (List<IInferableInstance> possibleUnfold : possibleUnfolds) {
             List<IInferableInstance> restOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
             restOfGoals.addAll(0,possibleUnfold);
             IInferableInstance newGoal = restOfGoals.remove(0);
             RuleNode childNode = constructPositiveChildNode(newGoal, restOfGoals, ruleNode);
             childNodes.add(childNode);
         }
 
         ruleNode.getChildren().addAll(0,childNodes);
         if (LOGGER.isInfoEnabled()) LOGGER.info("D1 generated "+childNodes.size()+" new states.");
         ruleNode.setNodeMark(RuleNode.NodeMark.EXPANDED);
     }
 
     public void visit(D2RuleNode ruleNode) throws DefinitionException {
         if (LOGGER.isInfoEnabled()) LOGGER.info("Applying D2 to node.");
 
         PredicateInstance currentGoal = (PredicateInstance) ruleNode.getCurrentGoal();
 
         List<IInferableInstance> newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
         List<DenialInstance> newNestedDenials = new LinkedList<DenialInstance>(ruleNode.getNestedDenialsList());
         DenialInstance newCurrentDenial = newNestedDenials.remove(0).shallowClone();
         IInferableInstance newGoal = null;
 
         RuleNode childNode;
 
         List<List<IInferableInstance>> possibleUnfolds = ruleNode.getAbductiveFramework().unfoldDefinitions(currentGoal);
 
         List<DenialInstance> newUnfoldedDenials = new LinkedList<DenialInstance>();
         for (List<IInferableInstance> possibleUnfold:possibleUnfolds) {
             DenialInstance newUnfoldedDenial = newCurrentDenial.shallowClone();
             newUnfoldedDenial.getBody().addAll(possibleUnfold);
            // newUnfoldedDenial = (DenialInstance) newUnfoldedDenial.deepClone(new HashMap<VariableInstance, IUnifiableAtomInstance>(ruleNode.getAssignments()));
             newUnfoldedDenials.add(newUnfoldedDenial);
         }
 
         if (newNestedDenials.isEmpty()) {
             newRestOfGoals.addAll(0,newUnfoldedDenials);
             newGoal = null;
             if (!newRestOfGoals.isEmpty()) newGoal = newRestOfGoals.remove(0);
             childNode = constructPositiveChildNode(newGoal,newRestOfGoals,ruleNode);
         }
 
         else {
             newNestedDenials.get(0).getBody().addAll(0,newUnfoldedDenials);
             newGoal = null;
             if (!newNestedDenials.get(0).getBody().isEmpty()) newGoal = newNestedDenials.get(0).getBody().remove(0);
             childNode = constructNegativeChildNode(newGoal,newNestedDenials,newRestOfGoals,ruleNode);
         }
 
         ruleNode.getChildren().add(childNode);
         ruleNode.setNodeMark(RuleNode.NodeMark.EXPANDED);
 
     }
 
     public void visit(E1RuleNode ruleNode) {
         if (LOGGER.isInfoEnabled()) LOGGER.info("Applying E1 to node.");
         RuleNode childNode;
         List<IInferableInstance> newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
         EqualityInstance currentGoal = (EqualityInstance) ruleNode.getCurrentGoal();
         IInferableInstance newGoal = null;
 
         if (!newRestOfGoals.isEmpty()) newGoal = newRestOfGoals.remove(0);
         childNode = constructPositiveChildNode(newGoal,newRestOfGoals,ruleNode);
         childNode.getStore().equalities.add(currentGoal);
         ruleNode.getChildren().add(childNode);
         ruleNode.setNodeMark(RuleNode.NodeMark.EXPANDED);
     }
 
     public void visit(E2RuleNode ruleNode) {
         EqualityInstance currentGoal = (EqualityInstance) ruleNode.getCurrentGoal();
 
         List<IInferableInstance> newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
         List<DenialInstance> newNestedDenials = new LinkedList<DenialInstance>(ruleNode.getNestedDenialsList());
         DenialInstance newCurrentDenial = newNestedDenials.remove(0).shallowClone();
         IInferableInstance newGoal = null;
 
         RuleNode childNode;
         List<RuleNode> newChildNodes = new LinkedList<RuleNode>();
 
         List<EqualityInstance> reductionResult = currentGoal.reduceLeftRight();
 
         if (!reductionResult.isEmpty()) {  // E2
             if (LOGGER.isInfoEnabled()) LOGGER.info("Applying E2 general case to node.");
             newCurrentDenial.getBody().addAll(0,reductionResult);
             newGoal = newCurrentDenial.getBody().remove(0);
             newNestedDenials.add(newCurrentDenial);
             childNode = constructNegativeChildNode(newGoal,newNestedDenials,newRestOfGoals,ruleNode);
             newChildNodes.add(childNode);
         }
 
         else { // One of the base cases.
             if (currentGoal.getRight() instanceof VariableInstance) { //E2c TODO: instanceof sucks..
                 if (LOGGER.isInfoEnabled()) LOGGER.info("Applying E2c to node.");
                 HashMap<VariableInstance,IUnifiableAtomInstance> newAssignments = new HashMap<VariableInstance,IUnifiableAtomInstance>(ruleNode.getAssignments());
                 boolean unificationSuccess = currentGoal.unifyRightLeft(newAssignments); // TODO Need to check success?
                 newCurrentDenial = (DenialInstance)newCurrentDenial.performSubstitutions(newAssignments);
                 if (newCurrentDenial.getBody().isEmpty()) {
                     newGoal = new FalseInstance();
                     if (newNestedDenials.isEmpty()) {
                         childNode = constructPositiveChildNode(newGoal,newRestOfGoals,ruleNode);
                     }
                     else {
                         childNode = constructNegativeChildNode(newGoal,newNestedDenials,newRestOfGoals,ruleNode);
                     }
                 }
                 else {
                     newGoal = newCurrentDenial.getBody().remove(0);
                     newNestedDenials.add(newCurrentDenial);
                     childNode = constructNegativeChildNode(newGoal,newNestedDenials,newRestOfGoals,ruleNode);
                 }
                 childNode.setAssignments(newAssignments);
                 newChildNodes.add(childNode);
             }
             else { //E2b
                 if (LOGGER.isInfoEnabled()) LOGGER.info("Applying E2b to node.");
                 // Branch 1
                 InEqualityInstance inEqualityInstance = new InEqualityInstance(currentGoal);
                 newGoal = new TrueInstance(); // TODO: Is this correct?
                 if (newNestedDenials.isEmpty()) {
                     childNode = constructPositiveChildNode(newGoal,newRestOfGoals,ruleNode);
                 }
                 else {
                     childNode = constructNegativeChildNode(newGoal,newNestedDenials,newRestOfGoals,ruleNode);
                 }
                 childNode.getStore().equalities.add(inEqualityInstance);
                  newChildNodes.add(childNode);
                 // Branch 2
                 newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
                 newNestedDenials = new LinkedList<DenialInstance>(ruleNode.getNestedDenialsList());
                 newCurrentDenial = newNestedDenials.remove(0).shallowClone();
                 HashMap<VariableInstance,IUnifiableAtomInstance> newAssignments = new HashMap<VariableInstance,IUnifiableAtomInstance>(ruleNode.getAssignments());
                 boolean unificationSuccess = currentGoal.unifyLeftRight(newAssignments); // TODO Need to check success?
                 newCurrentDenial = (DenialInstance)newCurrentDenial.performSubstitutions(newAssignments);
                 if (newCurrentDenial.getBody().isEmpty()) {
                     newGoal = new FalseInstance();
                     if (newNestedDenials.isEmpty()) {
                         childNode = constructPositiveChildNode(newGoal,newRestOfGoals,ruleNode);
                     }
                     else {
                         childNode = constructNegativeChildNode(newGoal,newNestedDenials,newRestOfGoals,ruleNode);
                     }
                 }
                 else {
                     newGoal = newCurrentDenial.getBody().remove(0);
                     newNestedDenials.add(newCurrentDenial);
                     childNode = constructNegativeChildNode(newGoal, newNestedDenials,newRestOfGoals,ruleNode);
                 }
                 childNode.setAssignments(newAssignments);
                 newChildNodes.add(childNode);
             }
         }
 
         ruleNode.getChildren().addAll(0,newChildNodes);
         ruleNode.setNodeMark(RuleNode.NodeMark.EXPANDED);
 
     }
 
     public void visit(N1RuleNode ruleNode) {
         if (LOGGER.isInfoEnabled()) LOGGER.info("Applying N1 to node.");
         List<IInferableInstance> newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
         NegationInstance goal = (NegationInstance) ruleNode.getCurrentGoal();
         DenialInstance denial = new DenialInstance(goal.getSubFormula());
         RuleNode childNode = constructPositiveChildNode(denial, newRestOfGoals, ruleNode);
         ruleNode.getChildren().add(childNode);
         ruleNode.setNodeMark(RuleNode.NodeMark.EXPANDED);
     }
 
     public void visit(N2RuleNode ruleNode) {
         if (LOGGER.isInfoEnabled()) LOGGER.info("Applying N2 to node.");
         RuleNode childNode;
         LinkedList<RuleNode> childNodes = new LinkedList<RuleNode>();
         List<IInferableInstance> newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
         NegationInstance goal = (NegationInstance) ruleNode.getCurrentGoal();
         List<DenialInstance> newNestedDenialList = new LinkedList<DenialInstance>(ruleNode.getNestedDenialsList());
         DenialInstance currentDenial = newNestedDenialList.remove(0).shallowClone();
         if (newNestedDenialList.isEmpty()) {
             childNode = constructPositiveChildNode(goal.getSubFormula(), newRestOfGoals, ruleNode);
 
         } else {
             childNode = constructNegativeChildNode(goal.getSubFormula(), newNestedDenialList, newRestOfGoals, ruleNode);
         }
         childNodes.add(childNode);
         // OR
         newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
         if (newNestedDenialList.isEmpty()) {
             newRestOfGoals.add(0, currentDenial);
             childNode = constructPositiveChildNode(goal, newRestOfGoals, ruleNode);
         } else {
             DenialInstance nestedDenial = newNestedDenialList.remove(0).shallowClone();
             nestedDenial.getBody().add(0, currentDenial);
             nestedDenial.getBody().add(0, goal);
             newNestedDenialList.add(0, nestedDenial);
             childNode = constructNegativeChildNode(goal, newNestedDenialList, newRestOfGoals, ruleNode);
         }
         childNodes.add(childNode);
         ruleNode.getChildren().addAll(0,childNodes);
         ruleNode.setNodeMark(RuleNode.NodeMark.EXPANDED);
     }
 
     public void visit(F1RuleNode ruleNode) {
         if (LOGGER.isInfoEnabled()) LOGGER.info("Applying F1 to node.");
         RuleNode childNode;
         List<IInferableInstance> newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
         ConstraintInstance currentGoal = (ConstraintInstance) ruleNode.getCurrentGoal();
         IInferableInstance newGoal = null;
 
         if (!newRestOfGoals.isEmpty()) newGoal = newRestOfGoals.remove(0);
         childNode = constructPositiveChildNode(newGoal,newRestOfGoals,ruleNode);
         childNode.getStore().constraints.add(currentGoal);
         ruleNode.getChildren().add(childNode);
         ruleNode.setNodeMark(RuleNode.NodeMark.EXPANDED);
     }
 
     public void visit(F2RuleNode ruleNode) {
         if (LOGGER.isInfoEnabled()) LOGGER.info("Applying F2 to node.");
         ConstraintInstance currentGoal = (ConstraintInstance) ruleNode.getCurrentGoal();
 
         List<IInferableInstance> newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
         List<DenialInstance> newNestedDenials = new LinkedList<DenialInstance>(ruleNode.getNestedDenialsList());
         DenialInstance newCurrentDenial = newNestedDenials.remove(0).shallowClone();
         IInferableInstance newGoal = null;
 
         RuleNode childNode;
         List<RuleNode> newChildNodes = new LinkedList<RuleNode>();
         // Branch 1
         NegativeConstraintInstance negativeConstraintInstance = new NegativeConstraintInstance(currentGoal);
         newGoal = new TrueInstance(); // TODO: Is this correct?
         if (newNestedDenials.isEmpty()) {
             childNode = constructPositiveChildNode(newGoal,newRestOfGoals,ruleNode);
         }
         else {
             childNode = constructNegativeChildNode(newGoal,newNestedDenials,newRestOfGoals,ruleNode);
         }
         childNode.getStore().constraints.add(negativeConstraintInstance);
         newChildNodes.add(childNode);
         // Branch 2
         newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
         newNestedDenials = new LinkedList<DenialInstance>(ruleNode.getNestedDenialsList());
         newCurrentDenial = newNestedDenials.remove(0).shallowClone();
         if (newCurrentDenial.getBody().isEmpty()) {
             newGoal = new FalseInstance();
             if (newNestedDenials.isEmpty()) {
                 childNode = constructPositiveChildNode(newGoal,newRestOfGoals,ruleNode);
             }
             else {
                 childNode = constructNegativeChildNode(newGoal,newNestedDenials,newRestOfGoals,ruleNode);
             }
         }
         else {
             newGoal = newCurrentDenial.getBody().remove(0);
             newNestedDenials.add(newCurrentDenial);
             childNode = constructNegativeChildNode(newGoal, newNestedDenials,newRestOfGoals,ruleNode);
         }
 
         childNode.getStore().constraints.add(currentGoal);
         newChildNodes.add(childNode);
 
     }
 
 
         /**
         * Produces one child node where the true instance is removed from the goal stack.
         *
         * @param ruleNode
         */
     public void visit(PositiveTrueRuleNode ruleNode)
     {
         if (LOGGER.isInfoEnabled()) LOGGER.info("Applying truth conjunction rule to node.");
         List<IInferableInstance> newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
         IInferableInstance newGoal = null;
         if (!newRestOfGoals.isEmpty()) newGoal = newRestOfGoals.remove(0);
         RuleNode newRuleNode = constructPositiveChildNode(newGoal, newRestOfGoals, ruleNode);
         ruleNode.getChildren().add(newRuleNode);
         ruleNode.setNodeMark(RuleNode.NodeMark.EXPANDED);
     }
 
     /**
      * Produces one child node where the true instance is removed from the denial.
      *
      * @param ruleNode
      */
     public void visit(NegativeTrueRuleNode ruleNode) {
         if (LOGGER.isInfoEnabled()) LOGGER.info("Applying truth denial conjunction rule to node.");
         List<IInferableInstance> newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
         List<DenialInstance> newNestedDenialList = new LinkedList<DenialInstance>(ruleNode.getNestedDenialsList());
         DenialInstance currentDenialInstance = newNestedDenialList.get(0);
         IInferableInstance newGoal;
         if (!currentDenialInstance.getBody().isEmpty()) {
             newGoal = currentDenialInstance.getBody().remove(0);
         } else {
             newNestedDenialList.remove(0);
             newGoal = new FalseInstance();
         }
         RuleNode newChildNode = constructPositiveChildNode(newGoal, newRestOfGoals, ruleNode);
         ruleNode.getChildren().add(newChildNode);
         ruleNode.setNodeMark(RuleNode.NodeMark.EXPANDED);
     }
 
     /**
      * Fails the node.
      *
      * @param ruleNode
      */
     public void visit(PositiveFalseRuleNode ruleNode) {
         if (LOGGER.isInfoEnabled()) LOGGER.info("Applying false conjunction rule to node.");
         ruleNode.setNodeMark(RuleNode.NodeMark.FAILED);
     }
 
     /**
      * Produces one child node whereby the nestedDenialsList succeeds.
      *
      * @param ruleNode
      */
     public void visit(NegativeFalseRuleNode ruleNode) {
         if (LOGGER.isInfoEnabled()) LOGGER.info("Applying false denial conjunction rule to node.");
         List<IInferableInstance> newRestOfGoals = new LinkedList<IInferableInstance>(ruleNode.getNextGoals());
         List<DenialInstance> newNestedDenialList = new LinkedList<DenialInstance>(ruleNode.getNestedDenialsList());
         RuleNode newChildNode;
         newNestedDenialList.remove(0);
         if (newNestedDenialList.isEmpty()) {
             IInferableInstance newGoal = new TrueInstance();
             newChildNode = constructPositiveChildNode(newGoal, newRestOfGoals, ruleNode);
         } else {
             DenialInstance newDenial = newNestedDenialList.remove(0).shallowClone();
             IInferableInstance newGoal = newDenial.getBody().remove(0);
             newChildNode = constructNegativeChildNode(newGoal, newNestedDenialList, newRestOfGoals, ruleNode);
         }
         ruleNode.getChildren().add(newChildNode);
         ruleNode.setNodeMark(RuleNode.NodeMark.EXPANDED);
     }
 
     public void visit(LeafRuleNode ruleNode) {
         if (LOGGER.isInfoEnabled()) LOGGER.info("Executing equality solver on leaf node:\n"+
                 "--------------------------------------\n"+
                 ruleNode+"\n"+
                 "--------------------------------------\n");
         Map<VariableInstance, IUnifiableAtomInstance> equalitySolveSuccess = ruleNode.equalitySolve();
         if (equalitySolveSuccess!=null) {
             if (LOGGER.isInfoEnabled()) LOGGER.info("Equality solver succeeded.");
             ruleNode.setNodeMark(RuleNode.NodeMark.SUCCEEDED);
             leafRuleNodes.add(ruleNode);
         }
         else {
             if (LOGGER.isInfoEnabled()) LOGGER.info("Equality solver failed.");
             ruleNode.setNodeMark(RuleNode.NodeMark.FAILED);
         }
     }
 
     public RuleNode stateRewrite() throws DefinitionException {
         currentRuleNode.acceptVisitor(this);
         if (currentRuleNode == null) {  // Finished.
             return null;
         }
         currentRuleNode = chooseNextNode();
         return currentRuleNode;
     }
 
     public RuleNode getCurrentRuleNode() {
         return currentRuleNode;
     }
 
     protected abstract RuleNode chooseNextNode();
 }

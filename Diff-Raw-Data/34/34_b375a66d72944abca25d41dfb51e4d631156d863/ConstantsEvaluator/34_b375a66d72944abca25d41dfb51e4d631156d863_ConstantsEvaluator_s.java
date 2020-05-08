 package de.b2tla.analysis;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 
 import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
 import de.be4.classicalb.core.parser.node.ACardExpression;
 import de.be4.classicalb.core.parser.node.AConjunctPredicate;
 import de.be4.classicalb.core.parser.node.AConstraintsMachineClause;
 import de.be4.classicalb.core.parser.node.AEqualPredicate;
 import de.be4.classicalb.core.parser.node.AGreaterPredicate;
 import de.be4.classicalb.core.parser.node.AIdentifierExpression;
 import de.be4.classicalb.core.parser.node.AIntegerExpression;
 import de.be4.classicalb.core.parser.node.ALessEqualPredicate;
 import de.be4.classicalb.core.parser.node.APropertiesMachineClause;
 import de.be4.classicalb.core.parser.node.Node;
 import de.be4.classicalb.core.parser.node.PExpression;
 
 /**
  * In the class the order of constants is determined. Constants can depend on
  * other constants: as an example in the expression k = k2 + 1 the value of the
  * constant k depends on k2. Constants are translated as definitions to TLA+ and
  * these definitions must be in order.
  * 
  */
 public class ConstantsEvaluator extends DepthFirstAdapter {
 	private Hashtable<Node, HashSet<Node>> dependsOnIdentifierTable;
 	private MachineContext machineContext;
 	private ValuesOfIdentifierFinder valuesOfConstantsFinder;
 	private HashMap<Node, Integer> integerValueTable;
 
 	private LinkedHashMap<Node, Node> valueOfIdentifier;
 
 	public Node getValueOfConstant(Node con) {
 		return valueOfIdentifier.get(con);
 	}
 
 	public LinkedHashMap<Node, Node> getValueOfIdentifierMap() {
 		return valueOfIdentifier;
 	}
 
 	public Integer getIntValue(Node node) {
 		return integerValueTable.get(node);
 	}
 
 	public ConstantsEvaluator(MachineContext machineContext) {
 		this.dependsOnIdentifierTable = new Hashtable<Node, HashSet<Node>>();
 		this.integerValueTable = new HashMap<Node, Integer>();
 		this.machineContext = machineContext;
 
 		ConstantsInTreeFinder constantInTreeFinder = new ConstantsInTreeFinder();
 
 		APropertiesMachineClause properties = machineContext
 				.getPropertiesMachineClause();
 		if (null != properties) {
 			properties.apply(constantInTreeFinder);
 		}
 
 		AConstraintsMachineClause constraints = machineContext
 				.getConstraintMachineClause();
 		if (null != constraints) {
 			constraints.apply(constantInTreeFinder);
 		}
 
 		this.valuesOfConstantsFinder = new ValuesOfIdentifierFinder();
 
 		this.valueOfIdentifier = new LinkedHashMap<Node, Node>();
 
 		evalIdentifier(machineContext.getConstants().values());
 
 		evalIdentifier(machineContext.getScalarParameter().values());
 	}
 
 	private void evalIdentifier(Collection<Node> ids) {
 		boolean newRun = true;
 		while (newRun) {
 			newRun = false;
 			Iterator<Node> itr = ids.iterator();
 			while (itr.hasNext()) {
 				Node id = itr.next();
 				if (valueOfIdentifier.containsKey(id))
 					continue;
 				HashSet<Node> idValues = valuesOfConstantsFinder.valuesOfIdentifierTable
 						.get(id);
 				for (Node val : idValues) {
 					HashSet<Node> idsInVal = dependsOnIdentifierTable.get(val);
 					if (idsInVal.size() == 0) {
 						valueOfIdentifier.put(id, val);
 						removeIdentifier(ids, id);
 						newRun = true;
 						break;
 					}
 				}
 			}
 		}
 	}
 
 	private void removeIdentifier(Collection<Node> collection,
 			Node identifierToRemove) {
 		Iterator<Node> itr = collection.iterator();
 		while (itr.hasNext()) {
 			Node id = itr.next();
 			HashSet<Node> idValues = valuesOfConstantsFinder.valuesOfIdentifierTable
 					.get(id);
 			for (Node val : idValues) {
 				HashSet<Node> idsInVal = dependsOnIdentifierTable.get(val);
 				idsInVal.remove(identifierToRemove);
 			}
 		}
 	}
 
 	class ConstantsInTreeFinder extends DepthFirstAdapter {
 		@Override
 		public void defaultIn(Node node) {
 			dependsOnIdentifierTable.put(node, new HashSet<Node>());
 		}
 
 		@Override
 		public void defaultOut(Node node) {
 			HashSet<Node> set = dependsOnIdentifierTable.get(node);
 			Node parent = node.parent();
 			HashSet<Node> parentSet = dependsOnIdentifierTable.get(parent);
 			parentSet.addAll(set);
 		}
 
 		@Override
 		public void caseAPropertiesMachineClause(APropertiesMachineClause node) {
 			defaultIn(node);
 			node.getPredicates().apply(this);
 		}
 
 		@Override
 		public void caseAConstraintsMachineClause(AConstraintsMachineClause node) {
 			defaultIn(node);
 			node.getPredicates().apply(this);
 		}
 
 		@Override
 		public void caseAIdentifierExpression(AIdentifierExpression node) {
 			defaultIn(node);
 			Node refNode = machineContext.getReferences().get(node);
 			if (machineContext.getConstants().containsValue(refNode)) {
 				HashSet<Node> set = dependsOnIdentifierTable.get(node);
 				set.add(refNode);
 			}
 			// it must be a bounded variable
 			defaultOut(node);
 		}
 	}
 
 	class ValuesOfIdentifierFinder extends DepthFirstAdapter {
 		private Hashtable<Node, HashSet<Node>> valuesOfIdentifierTable;
 		private HashSet<Node> identifiers;
 
 		public ValuesOfIdentifierFinder() {
 			valuesOfIdentifierTable = new Hashtable<Node, HashSet<Node>>();
 
 			this.identifiers = new HashSet<Node>();
 			identifiers.addAll(machineContext.getConstants().values());
 			identifiers.addAll(machineContext.getScalarParameter().values());
 
 			Iterator<Node> itr = identifiers.iterator();
 			while (itr.hasNext()) {
 				Node id = itr.next();
 				valuesOfIdentifierTable.put(id, new HashSet<Node>());
 			}
 
 			Node constraints = machineContext.getConstraintMachineClause();
 			if (constraints != null) {
 				analysePredicate(((AConstraintsMachineClause) constraints)
 						.getPredicates());
 			}
 			Node properties = machineContext.getPropertiesMachineClause();
 			if (properties != null) {
 				analysePredicate(((APropertiesMachineClause) properties)
 						.getPredicates());
 			}
 
 		}
 
 		private void analysePredicate(Node n) {
 			if (n instanceof AEqualPredicate) {
 				analyseEqualsPredicate((AEqualPredicate) n);
 				return;
 			} else if (n instanceof AGreaterPredicate) {
 				analyseGreaterPredicate((AGreaterPredicate) n);
 			} else if (n instanceof ALessEqualPredicate) {
 				analyseLessEqualPredicate((ALessEqualPredicate) n);
 			} else if (n instanceof AConjunctPredicate) {
 				analysePredicate(((AConjunctPredicate) n).getLeft());
 				analysePredicate(((AConjunctPredicate) n).getRight());
 				return;
 			}
 
 		}
 
 		private void analyseLessEqualPredicate(ALessEqualPredicate node) {
 			PExpression left = node.getLeft();
 			Node left_ref = machineContext.getReferences().get(left);
 			PExpression right = node.getRight();
 			Node right_ref = machineContext.getReferences().get(right);
 			if (identifiers.contains(left_ref)) {
 				AIntegerExpression intExpr = (AIntegerExpression) right;
 				int value = Integer.parseInt(intExpr.getLiteral().getText());
 				integerValueTable.put(left_ref, value);
 			}
 
 			if (identifiers.contains(right_ref)) {
 				AIntegerExpression intExpr = (AIntegerExpression) left;
 				int value = Integer.parseInt(intExpr.getLiteral().getText());
 				integerValueTable.put(right_ref, value);
 			}
 		}
 
 		private void analyseGreaterPredicate(AGreaterPredicate node) {
 			PExpression left = node.getLeft();
 			Node left_ref = machineContext.getReferences().get(left);
 			PExpression right = node.getRight();
 			Node right_ref = machineContext.getReferences().get(right);
 			if (identifiers.contains(left_ref)) {
 				AIntegerExpression intExpr = (AIntegerExpression) right;
 				int value = Integer.parseInt(intExpr.getLiteral().getText());
 				integerValueTable.put(left_ref, value + 1);
 			}
 
 			if (identifiers.contains(right_ref)) {
 				AIntegerExpression intExpr = (AIntegerExpression) left;
 				int value = Integer.parseInt(intExpr.getLiteral().getText());
 				integerValueTable.put(right_ref, value - 1);
 			}
 		}
 
 		private void analyseEqualsPredicate(AEqualPredicate node) {
 			PExpression left = node.getLeft();
 			Node left_ref = machineContext.getReferences().get(left);
 			PExpression right = node.getRight();
 			Node right_ref = machineContext.getReferences().get(right);
 
 			if (left instanceof ACardExpression) {
 				Node ref = machineContext.getReferences().get(
 						((ACardExpression) left).getExpression());
 				try {
 					AIntegerExpression intExpr = (AIntegerExpression) right;
 					int size = Integer.parseInt(intExpr.getLiteral().getText());
 					integerValueTable.put(ref, size);
 				} catch (ClassCastException e) {
 				}
 			}
 
 			if (right instanceof ACardExpression) {
 				Node ref = machineContext.getReferences().get(
 						((ACardExpression) right).getExpression());
 				try {
 					AIntegerExpression intExpr = (AIntegerExpression) left;
 					int size = Integer.parseInt(intExpr.getLiteral().getText());
 					integerValueTable.put(ref, size);
 				} catch (ClassCastException e) {
 				}
 			}
 
 			if (identifiers.contains(left_ref)) {
 				valuesOfIdentifierTable.get(left_ref).add(right);
 			}
 
 			if (identifiers.contains(right_ref)) {
 				valuesOfIdentifierTable.get(right_ref).add(left);
 			}
 			return;
 
 		}
 
 	}
 
 }

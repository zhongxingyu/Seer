 package TreeAutomata;
 
 import java.util.*;
 import java.util.Map.Entry;
 
 public class FiniteTreesTable {
 
 	public HashMap<Integer, HashSet<HashSet<Integer>>> FiniteTreesTable;
 
 	public FiniteTreesTable(HashSet<Transition> tr, HashSet<Integer> ac) {
 
 		FiniteTreesTable = new HashMap<Integer, HashSet<HashSet<Integer>>>();
 
 		HashSet<Transition> transRelWorkCopy = (HashSet<Transition>) tr.clone();
 		HashSet<Transition> transToEliminate = new HashSet<Transition>();
 		boolean transitionAdded;
 
 		do {
 			transitionAdded = false;
 
 			transRelWorkCopy.removeAll(transToEliminate);
 			transToEliminate.clear();
 
 			for (Transition trans : transRelWorkCopy) {
 				HashSet<HashSet<Integer>> tmpSetOfStates;
 				
 				if ((ac.contains(trans.rightChildren) && ac.contains(trans.leftChildren)) 
						|| FiniteTreesTable.containsKey(trans.leftChildren) && (!trans.node.equals(trans.leftChildren) || ac.contains(trans.node)) && ac.contains(trans.rightChildren) 
						|| FiniteTreesTable.containsKey(trans.rightChildren) && (!trans.node.equals(trans.rightChildren) || ac.contains(trans.node)) && ac.contains(trans.leftChildren) 
						|| FiniteTreesTable.containsKey(trans.leftChildren) && (!trans.node.equals(trans.leftChildren) || ac.contains(trans.node)) && FiniteTreesTable.containsKey(trans.rightChildren) && (!trans.node.equals(trans.rightChildren) || ac.contains(trans.node))) {
 					HashSet<Integer> tmpStates = new HashSet<Integer>();
 					tmpStates.add(trans.rightChildren);
 					tmpStates.add(trans.leftChildren);
 					if (FiniteTreesTable.containsKey(trans.node)) {
 						tmpSetOfStates = FiniteTreesTable.get(trans.node);
 					} else {
 						tmpSetOfStates = new HashSet<HashSet<Integer>>();
 					}
 					tmpSetOfStates.add(tmpStates);
 					transToEliminate.add(trans);
 					FiniteTreesTable.put(trans.node, tmpSetOfStates);
 					transitionAdded = true;
 				}
 			}
 		} while (transitionAdded);
 	}
 
 	public boolean containsBadStates() {
 		for (Entry<Integer, HashSet<HashSet<Integer>>> entryOfTable : FiniteTreesTable.entrySet()) {
 			for (HashSet<Integer> tree : entryOfTable.getValue()) {
 				if (!FiniteTreesTable.keySet().containsAll(tree))
 					return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean hasEmptyTreesSet(Integer state) {
 		return !FiniteTreesTable.containsKey(state);
 	}
 
 	public HashSet<Integer> getStates() {
 		return new HashSet(FiniteTreesTable.keySet());
 	}
 
 	public void removeIncoerentTrees() {
 		HashSet<HashSet<Integer>> treeToRemove = new HashSet<HashSet<Integer>>();
 		HashSet<Integer> keyToRemove = new HashSet<Integer>();
 		Set<Integer> goodStates = FiniteTreesTable.keySet();
 		for (Entry<Integer, HashSet<HashSet<Integer>>> entryOfTable : FiniteTreesTable.entrySet()) {
 			for (HashSet<Integer> tree : entryOfTable.getValue()) {
 				if (!goodStates.containsAll(tree)) {
 					treeToRemove.add(tree);
 				}
 			}
 			for(HashSet<Integer> tree : treeToRemove){
 				entryOfTable.getValue().remove(tree);
 			}
 			treeToRemove.clear();
 			if (entryOfTable.getValue().isEmpty()) {
 				keyToRemove.add(entryOfTable.getKey());
 			}
 		}
 		for (Integer key : keyToRemove)
 			FiniteTreesTable.remove(key);
 		keyToRemove.clear();
 	}
 }

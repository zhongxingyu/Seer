 package masg.dd.representations.tables;
 
 import groovy.lang.Closure;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map.Entry;
 
 import masg.dd.context.DDContext;
 import masg.dd.operations.AdditionOperation;
 import masg.dd.operations.BinaryOperation;
 import masg.dd.operations.ConstantMultiplicationOperation;
 import masg.dd.operations.UnaryOperation;
 import masg.dd.representations.dag.ImmutableDDElement;
 import masg.dd.representations.dag.ImmutableDDLeaf;
 import masg.dd.representations.dag.ImmutableDDNode;
 import masg.dd.variables.DDVariable;
 import masg.util.BitMap;
 
 public class TableDD {
 	
 	HashMap<Double, TableDDLeaf> leaves = new HashMap<Double, TableDDLeaf>();
 	HashMap<DDVariable, ArrayList< HashMap<TableDDElement, HashSet<TableDDNode>> > >  nodes = new HashMap<DDVariable, ArrayList< HashMap<TableDDElement, HashSet<TableDDNode>> > >();
 	
 	TableDDElement rootNode = null;
 	HashSet<DDVariable> vars;
 	
 	protected TableDD(ArrayList<DDVariable> vars ) {
 		this.vars = new HashSet<DDVariable>(vars);
 		
 		for(DDVariable var:vars) {
 			ArrayList< HashMap<TableDDElement, HashSet<TableDDNode>> > temp = new ArrayList< HashMap<TableDDElement, HashSet<TableDDNode>> >();
 			
 			for(int i=0;i<var.getValueCount();i++) {
 				temp.add(new HashMap<TableDDElement,HashSet<TableDDNode>>());
 			}
 			
 			nodes.put(var, temp);
 		}
 	}
 	
 	public ImmutableDDElement asDagDD() {
 		return asDagDD(false);
 	}
 	
 	public ImmutableDDElement asDagDD(boolean isMeasure) {
 		if(rootNode instanceof TableDDNode) {
 			return new ImmutableDDNode(vars, (TableDDNode)rootNode, null, isMeasure);
 		}
 		else if(rootNode instanceof TableDDLeaf) {
 			return new ImmutableDDLeaf(vars, (TableDDLeaf)rootNode, null);
 		}
 		
 		return null;
 	}
 	
 	public static TableDD build(ArrayList<DDVariable> vars, Closure<Double>... closures) {
 		TableDD dd = new TableDD(vars);
 		dd.rootNode = dd.makeSubGraph(new HashMap<DDVariable,Integer>(), DDContext.canonicalVariableOrdering, dd.vars, new ProbabilityClosuresFunction(closures));
 		return dd;
 	}
 	
 	public static TableDD build(ArrayList<DDVariable> vars, Closure<Double> c) {
 		TableDD dd = new TableDD(vars);
 		dd.rootNode = dd.makeSubGraph(new HashMap<DDVariable,Integer>(), DDContext.canonicalVariableOrdering, dd.vars, new ClosureFunction(c));
 		return dd;
 	}
 	
 	public static TableDD build(ArrayList<DDVariable> vars, double constVal) {
 		TableDD dd = new TableDD(vars);
 		dd.rootNode = dd.makeSubGraph(new HashMap<DDVariable,Integer>(), DDContext.canonicalVariableOrdering, dd.vars, new ConstantFunction(constVal));
 		return dd;
 	}
 	
 	public static ImmutableDDElement build(ArrayList<DDVariable> vars, ArrayList<ImmutableDDElement> dags, BinaryOperation op) {
 		
 		ArrayList<ImmutableDDElement> dagsNew = new ArrayList<ImmutableDDElement>();
 		for(ImmutableDDElement dd:dags) {
 			if(dd.isMeasure()) {
 				
 				HashSet<DDVariable> dagVars = new HashSet<DDVariable>(dd.getVariables());
 				dagVars.removeAll(vars);
 				
 				if(dagVars.size()>0) {
 					dd = TableDD.eliminate(new ArrayList<DDVariable>(dagVars), dd);
 				}
 			}
 			
 			dagsNew.add(dd);
 		}
 		
 		dags = dagsNew;
 		
 		ImmutableDDElement el1 = dags.get(0); 
 		ImmutableDDElement el2 = null;
 		
 		HashSet<DDVariable> varsNew = new HashSet<DDVariable>(vars);
 		TableDD dd=null;
 		for(int i=1;i<dags.size();i++) {
 			el2 = dags.get(i);
 			dd = new TableDD(new ArrayList<DDVariable>(varsNew));
 			dd.rootNode = dd.applyOperation(el1, el2, op, new HashMap<ImmutableDDElement,HashMap<ImmutableDDElement,TableDDElement>>());
 			el1 = dd.asDagDD(el1.isMeasure() && el2.isMeasure());
 		}
 		
 		return el1;
 	}
 	
 	public static TableDD build(ArrayList<DDVariable> vars, ImmutableDDElement dag, UnaryOperation op) {
 		TableDD dd = new TableDD(vars);
 		dd.rootNode = dd.applyOperation(dag, op, new HashMap<ImmutableDDElement,TableDDElement>());
 		return dd;
 	}
 	
 	public static TableDD restrict(HashMap<DDVariable,Integer> restrictVarValues, ImmutableDDElement dag) {
 		ArrayList<DDVariable> vars = new ArrayList<DDVariable>(dag.getVariables());
 		vars.removeAll(restrictVarValues.keySet());
 		
 		TableDD dd = new TableDD(vars);
 		dd.rootNode = dd.makeSubGraph(new HashMap<DDVariable,Integer>(), DDContext.canonicalVariableOrdering, dd.vars, new DagDDRestrictFunction(dag,restrictVarValues));
 		return dd;
 	}
 	
 	public static ImmutableDDElement eliminate(ArrayList<DDVariable> elimVars, ImmutableDDElement dag) {
 		ArrayList<DDVariable> elimVarsInOrder = new ArrayList<DDVariable>();
 		for(int i=0;i<DDContext.canonicalVariableOrdering.size();i++) {
 			DDVariable currVar = DDContext.canonicalVariableOrdering.get(i);
 			if(elimVars.contains(currVar)) {
 				elimVarsInOrder.add(currVar);
 			}
 		}
 		elimVars = elimVarsInOrder;
 		ArrayList<DDVariable> vars = new ArrayList<DDVariable>(dag.getVariables());
 		elimVars.retainAll(vars);
 		
 		TableDD dd = null;
 		dd = new TableDD(new ArrayList<DDVariable>(vars));
 		dd.rootNode = dd.sumSubtrees(dag, elimVars, new HashMap<ImmutableDDElement,TableDDElement>());
 		dd.vars.removeAll(elimVars);
 		dag = dd.asDagDD(true);
 		return dag;
 	}
 	
 	public static TableDD prime(ImmutableDDElement dag) {
 		HashMap<DDVariable,DDVariable> translation = new HashMap<DDVariable,DDVariable>();
 		
 		ArrayList<DDVariable> vars = new ArrayList<DDVariable>();
 		for(DDVariable var:dag.getVariables()) {
 			DDVariable varPrimed = var.getPrimed();
 			vars.add(varPrimed);
 			translation.put(varPrimed, var);
 		}
 		
 		TableDD dd = new TableDD(vars);
 		dd.rootNode = dd.makeSubGraph(new HashMap<DDVariable,Integer>(), DDContext.canonicalVariableOrdering, dd.vars, new DagDDTranslateFunction(dag,translation));
 		return dd;
 		
 	}
 	
 	public static TableDD unprime(ImmutableDDElement dag) {
 		HashMap<DDVariable,DDVariable> translation = new HashMap<DDVariable,DDVariable>();
 		
 		ArrayList<DDVariable> vars = new ArrayList<DDVariable>();
 		for(DDVariable var:dag.getVariables()) {
 			DDVariable varUnprimed = var.getUnprimed();
 			vars.add(varUnprimed);
 			translation.put(varUnprimed, var);
 		}
 		
 		TableDD dd = new TableDD(vars);
 		dd.rootNode = dd.makeSubGraph(new HashMap<DDVariable,Integer>(), DDContext.canonicalVariableOrdering, dd.vars, new DagDDTranslateFunction(dag,translation));
 		return dd;
 		
 	}
 	
 	public static TableDD approximate(ImmutableDDElement dag, double tolerance) {
 		TableDD dd = new TableDD(new ArrayList<DDVariable>(dag.getVariables()));
 		dd.rootNode = dd.approximateSubgraph(dag, new ArrayList<TableDDLeaf>(), tolerance, new HashMap<ImmutableDDElement,TableDDElement>());
 		return dd;
 	}
 	
 	public static ImmutableDDLeaf findMaxLeaf(ImmutableDDElement dag) {
 		return findLeaf(dag,null,new MaxLeafComparator(), new HashSet<ImmutableDDElement>());
 	}
 	
 	public static TableDDLeaf findMaxLeaf(TableDD tdd) {
 		TableDDLeaf res = null;
 				
 		for(TableDDLeaf l:tdd.leaves.values()) {
 			if(res == null) {
 				res = l;
 			}
 			else if(l.getValue()>res.getValue()) {
 				res = l;
 			}
 		}
 		
 		return res;
 	}
 	
 	public TableDDElement getRootNode() {
 		return rootNode;
 	}
 	
 	protected TableDDLeaf makeLeaf(Double value) {
 		TableDDLeaf l = leaves.get(value);
 		
 		if(l==null) {
 			l = new TableDDLeaf(value);
 			leaves.put(value, l);
 		}
 		
 		return l;
 	}
 	
 	protected TableDDNode makeNode(DDVariable var, TableDDElement[] children) {
 		if(nodes.get(var)==null || nodes.get(var).get(0)==null) {
 			System.out.println("Wha--?");
 		}
 		
 		HashSet<TableDDNode> possDupes = nodes.get(var).get(0).get(children[0]);
 		
 		for(int i=1;i<var.getValueCount() && possDupes!=null && possDupes.size()>0;++i) {
 			HashSet<TableDDNode> temp = nodes.get(var).get(i).get(children[i]);
 			if(temp==null) {
 				possDupes = null;
 				break;
 			}
 			possDupes.retainAll(temp);
 		}
 		
 		
 		TableDDNode n = null;
 		
 		if(possDupes!=null) {
 			for(TableDDNode possParent:possDupes) {
 				
 				n = possParent;
 				
 				for(int i=0;i<var.getValueCount();++i) {
 					if(!possParent.children[i].equals(children[i])) {
 						n = null;
 						break;
 					}
 				}
 				
 				
 			}
 		}
 		
 		if(n==null) {
 			n = new TableDDNode(var, children);
 			
 			for(int i=0;i<var.getValueCount();++i) {
 				if(nodes.get(var).get(i).get(n.children[i])==null) {
 					nodes.get(var).get(i).put(n.children[i], new HashSet<TableDDNode>());
 				}
 				nodes.get(var).get(i).get(n.children[i]).add(n);
 			}
 		}
 
 		return n;
 	}
 	
 	protected static ImmutableDDLeaf findLeaf(ImmutableDDElement el, ImmutableDDLeaf currFoundLeaf, Comparator<ImmutableDDLeaf> compr, HashSet<ImmutableDDElement> findCache) {
 		if(findCache.contains(el)) {
 			return currFoundLeaf;
 		}
 		
 		findCache.add(el);
 		
 		if(el instanceof ImmutableDDLeaf) {
 			ImmutableDDLeaf l = (ImmutableDDLeaf) el;
 			
 			if(currFoundLeaf == null || compr.compare(l, currFoundLeaf)>0) {
 				return l;
 			}
 		}
 		else {
 			ImmutableDDNode n = (ImmutableDDNode) el;
 			
 			for(ImmutableDDElement childEl:n.getChildren()) {
 				currFoundLeaf = findLeaf(childEl,currFoundLeaf,compr,findCache);
 			}
 		}
 		
 		return currFoundLeaf;
 	}
 	
 	protected TableDDElement approximateSubgraph(ImmutableDDElement el, ArrayList<TableDDLeaf> newLeaves, double tolerance, HashMap<ImmutableDDElement, TableDDElement> approxCache) {
 		if(approxCache.containsKey(el)) {
 			return approxCache.get(el);
 		}
 		
 		if(el instanceof ImmutableDDLeaf) {
 			ImmutableDDLeaf l = (ImmutableDDLeaf) el;
 			TableDDLeaf lNew = makeLeaf(l.getValue());
 			
 			int findIx = Collections.binarySearch(newLeaves,lNew);
 			
 			if(findIx>-1) {
 				approxCache.put(el, newLeaves.get(findIx));
 				return newLeaves.get(findIx);
 			}
 			else {
 				int ubIx = -(findIx + 1);
 				if(ubIx == 0){
 					newLeaves.add(lNew);
 					approxCache.put(el, lNew);
 					return lNew;
 				}
 				
 				int lbIx = ubIx-1;
 				
 				if(ubIx == newLeaves.size()) {
 					ubIx = ubIx-1;
 				}
 				
 				double distFromUb = Math.abs(newLeaves.get(ubIx).getValue()-l.getValue());
 				double distFromLb = Math.abs(newLeaves.get(lbIx).getValue()-l.getValue());
 				
 				if(distFromLb>tolerance && distFromUb>tolerance) {
 					newLeaves.add(lNew);
 					Collections.sort(newLeaves);
 					approxCache.put(el, lNew);
 					return lNew;
 				}
 				
 				if(distFromUb<=tolerance){
 					approxCache.put(el, newLeaves.get(ubIx));
 					return newLeaves.get(ubIx);
 				}
 				else {
 					approxCache.put(el, newLeaves.get(lbIx));
 					return newLeaves.get(lbIx);
 				}
 			}
 			
 		}
 		else if (el instanceof ImmutableDDNode) {
 			ImmutableDDNode n = (ImmutableDDNode) el;
 			TableDDElement[] children = new TableDDElement[n.getVariable().getValueCount()];
 			
 			for(int i=0;i<n.getVariable().getValueCount();i++) {
 				children[i] = approximateSubgraph(n.getChild(i),newLeaves,tolerance,approxCache);
 			}
 			
 			boolean allEqual = true;
 			
 			TableDDElement currEl = children[0];
 			for(int i=1;i<n.getVariable().getValueCount() && allEqual;i++) {
 				allEqual = allEqual && currEl.equals(children[i]);
 				currEl = children[i];
 			}
 			
 			if(allEqual) {
 				approxCache.put(el, children[0]);
 				return children[0];
 			}
 			
 			TableDDNode nNew = makeNode(n.getVariable(),children);
 			approxCache.put(el, nNew);
 			return nNew;
 		}
 		
 		return null;
 	}
 	
 	protected TableDDElement makeSubGraph(HashMap<DDVariable,Integer> path, List<DDVariable> varOrder, HashSet<DDVariable> vars, DDBuilderFunction fn) {
 		
 		if(varOrder.size()==0) {
 			return makeLeaf(fn.invoke(path));
 		}
 		
 		DDVariable currVar = varOrder.get(0);
 		List<DDVariable> nextVarOrder = varOrder.subList(1, varOrder.size());
 		if(vars.contains(currVar)) {
 			TableDDElement[] children = new TableDDElement[currVar.getValueCount()];
 			
 			for(int i=0;i<currVar.getValueCount();i++) {
 				path.put(currVar, i);
 				children[i] = makeSubGraph(path, nextVarOrder, vars, fn );
 			}
 			
 			boolean allEqual = true;
 			
 			TableDDElement currEl = children[0];
 			for(int i=1;i<currVar.getValueCount() && allEqual;i++) {
 				allEqual = allEqual && currEl.equals(children[i]);
 				currEl = children[i];
 			}
 			
 			if(allEqual) {
 				return children[0];
 			}
 			
 			return makeNode(currVar, children);
 		}
 		else {
 			return makeSubGraph(path,nextVarOrder, vars, fn);
 		}
 	}
 	
 	protected TableDDElement sumSubtrees(ImmutableDDElement el,List<DDVariable> subtreeElimVars, HashMap<ImmutableDDElement,TableDDElement> applyCache) {
 		
 		if(applyCache.containsKey(el)) {
 			return applyCache.get(el);
 		}
 		
 		int varIx = -1;
 		
 		if(el instanceof ImmutableDDNode) {
 			ImmutableDDNode n = (ImmutableDDNode) el;
 			varIx = DDContext.getVariableIndex(n.getVariable());
 		}
 		
 		DDVariable currVar = subtreeElimVars.get(0);
 		int subtreeRootVarIx = DDContext.getVariableIndex(currVar);
 		
 		
 		if(el instanceof ImmutableDDLeaf || varIx>subtreeRootVarIx) {
 			ArrayList<DDVariable> varsNew = new ArrayList<DDVariable>(el.getVariables());
			
			double multiplier = 1.0d;
			for(DDVariable v:subtreeElimVars) {
				multiplier*=v.getValueCount();
			}
			TableDD dd = build(varsNew,el,new ConstantMultiplicationOperation(multiplier));
 			
 			TableDDElement result = dd.rootNode;
 			
 			if(result instanceof TableDDLeaf) {
 				result = makeLeaf(((TableDDLeaf) result).getValue());
 			}
 			applyCache.put(el, result);
 			return result;
 		}
 		else {
 			ImmutableDDNode n = (ImmutableDDNode) el;
 			
 			if(n.getVariable().equals(currVar)) {
 				ArrayList<ImmutableDDElement> dags = new ArrayList<ImmutableDDElement>(Arrays.asList(n.getChildren()));
 				
 				ImmutableDDElement el1 = dags.get(0); 
 				ArrayList<DDVariable> varsNew = new ArrayList<DDVariable>(el1.getVariables());
 				ImmutableDDElement el2 = null;
 				TableDD dd=null;
 				for(int i=1;i<dags.size();i++) {
 					el2 = dags.get(i);
 					dd = new TableDD(new ArrayList<DDVariable>(varsNew));
 					dd.rootNode = dd.applyOperation(el1, el2, new AdditionOperation(), new HashMap<ImmutableDDElement,HashMap<ImmutableDDElement,TableDDElement>>());
 					el1 = dd.asDagDD(el1.isMeasure() && el2.isMeasure());
 				}
 				
 				TableDDElement result = dd.rootNode;
 				
 				if(subtreeElimVars.size()>1) {
 					result = sumSubtrees(dd.asDagDD(el.isMeasure()),subtreeElimVars.subList(1, subtreeElimVars.size()), applyCache);
 				}
 				
 				if(result instanceof TableDDLeaf) {
 					result = makeLeaf(((TableDDLeaf) result).getValue());
 				}
 				
 				applyCache.put(el, result);
 				return result;
 			}
 			else {
 				TableDDNode nNew;
 				TableDDElement[] children = new TableDDElement[el.getVariable().getValueCount()];
 				
 				for(int i=0;i<el.getVariable().getValueCount();i++) {
 					children[i] = sumSubtrees(n.getChild(i), subtreeElimVars, applyCache);
 				}
 				
 				nNew = makeNode(el.getVariable(),children);
 
 				boolean allEqual = true;
 				
 				TableDDElement currEl = children[0];
 				for(int i=1;i<el.getVariable().getValueCount() && allEqual;i++) {
 					allEqual = allEqual && currEl.equals(children[i]);
 					currEl = children[i];
 				}
 				
 				if(allEqual) {
 					applyCache.put(el, children[0]);
 					return children[0];
 				}
 				
 				applyCache.put(el, nNew);
 				return nNew;
 			}
 			
 			
 		}
 		
 	}
 	
 	protected TableDDElement applyOperation(ImmutableDDElement el, UnaryOperation op, HashMap<ImmutableDDElement,TableDDElement> applyCache) {
 		if(applyCache.containsKey(el)) {
 			return applyCache.get(el);
 		}
 		
 		TableDDElement result = null;
 		if(el instanceof ImmutableDDLeaf) {
 			ImmutableDDLeaf l = (ImmutableDDLeaf) el;
 			
 			result = makeLeaf(op.invoke(l.getValue()));
 		}
 		else if(el instanceof ImmutableDDNode) {
 			ImmutableDDNode n = (ImmutableDDNode) el;
 			
 			TableDDElement[] children = new TableDDElement[n.getVariable().getValueCount()];
 			
 			for(int i=0;i<n.getVariable().getValueCount();i++) {
 				children[i] = applyOperation(n.getChild(i),op, applyCache);
 			}
 			
 			result = makeNode(n.getVariable(), children);
 			
 			boolean allEqual = true;
 			
 			TableDDElement currEl = children[0];
 			for(int i=1;i<n.getVariable().getValueCount() && allEqual;i++) {
 				allEqual = allEqual && currEl.equals(children[i]);
 				currEl = children[i];
 			}
 			
 			if(allEqual) {
 				result = children[0];
 			}
 		}
 		
 		applyCache.put(el, result);
 		return result;
 	}
 	protected TableDDElement applyOperation(ImmutableDDElement el1, ImmutableDDElement el2, BinaryOperation op, HashMap<ImmutableDDElement,HashMap<ImmutableDDElement,TableDDElement>> applyCache) {
 		
 		if(applyCache.containsKey(el1) && applyCache.get(el1).containsKey(el2)) {
 			return applyCache.get(el1).get(el2);
 		}
 		else {
 			applyCache.put(el1, new HashMap<ImmutableDDElement,TableDDElement>());
 		}
 		
 		if(el1 instanceof ImmutableDDLeaf && el2 instanceof ImmutableDDLeaf) {
 			ImmutableDDLeaf l1 = (ImmutableDDLeaf) el1;
 			ImmutableDDLeaf l2 = (ImmutableDDLeaf) el2;
 			
 			TableDDLeaf lNew = makeLeaf(op.invoke(l1.getValue(), l2.getValue()));
 			applyCache.get(el1).put(el2, lNew);
 			return lNew;
 		}
 		
 		TableDDNode nNew = null;
 		DDVariable currVar = null;
 		TableDDElement[] children;
 		
 		if(el1 instanceof ImmutableDDLeaf && el2 instanceof ImmutableDDNode) {
 			ImmutableDDNode n2 = (ImmutableDDNode) el2;
 			currVar = n2.getVariable();
 			children = new TableDDElement[n2.getVariable().getValueCount()];
 			
 			for(int i=0;i<n2.getVariable().getValueCount();i++) {
 				children[i] = applyOperation(el1,n2.getChild(i),op, applyCache);
 			}
 		}
 		else if(el1 instanceof ImmutableDDNode && el2 instanceof ImmutableDDLeaf) {
 			ImmutableDDNode n1 = (ImmutableDDNode) el1;
 			currVar = n1.getVariable();
 			children = new TableDDElement[n1.getVariable().getValueCount()];
 			
 			for(int i=0;i<n1.getVariable().getValueCount();i++) {
 				children[i] = applyOperation(n1.getChild(i),el2,op, applyCache);
 			}
 		}
 		else if(el1 instanceof ImmutableDDNode && el2 instanceof ImmutableDDNode) {
 			ImmutableDDNode n1 = (ImmutableDDNode) el1;
 			ImmutableDDNode n2 = (ImmutableDDNode) el2;
 			
 			int varIx1 = DDContext.getVariableIndex(n1.getVariable());
 			int varIx2 = DDContext.getVariableIndex(n2.getVariable());
 			
 			if(varIx1==varIx2) {
 				currVar = n1.getVariable();
 				children = new TableDDElement[n1.getVariable().getValueCount()];
 				
 				for(int i=0;i<n1.getVariable().getValueCount();i++) {
 					children[i] = applyOperation(n1.getChild(i),n2.getChild(i),op, applyCache);
 				}
 			}
 			else if(varIx1<varIx2) {
 				currVar = n1.getVariable();
 				children = new TableDDElement[n1.getVariable().getValueCount()];
 				
 				for(int i=0;i<n1.getVariable().getValueCount();i++) {
 					children[i] = applyOperation(n1.getChild(i),n2,op,applyCache);
 				}
 			}
 			else {
 				currVar = n2.getVariable();
 				children = new TableDDElement[n2.getVariable().getValueCount()];
 				
 				for(int i=0;i<n2.getVariable().getValueCount();i++) {
 					children[i] = applyOperation(n1,n2.getChild(i),op,applyCache);
 				}
 			}
 			
 		}
 		else {
 			return null;
 		}
 		
 		boolean allEqual = true;
 		
 		TableDDElement currEl = children[0];
 		for(int i=1;i<currVar.getValueCount() && allEqual;i++) {
 			allEqual = allEqual && currEl.equals(children[i]);
 			currEl = children[i];
 		}
 		
 		if(allEqual) {
 			applyCache.get(el1).put(el2, children[0]);
 			return children[0];
 		}
 		
 		
 		nNew = makeNode(currVar,children);
 		applyCache.get(el1).put(el2, nNew);
 
 		return nNew;
 	}
 	
 	
 	public String toString(TableDDElement el, HashSet<Long> processed) {
 		String str = "";
 		
 		if(processed.contains(el.id)) {
 			return str;
 		}
 		processed.add(el.id);
 		
 		
 		
 		if(el instanceof TableDDNode) {
 			TableDDNode n = (TableDDNode) el;
 			
 			String parentLabel = "	\"" + n.getKey() + "\"";
 			
 			HashMap<TableDDElement,HashSet<Integer>> childrenPaths = new HashMap<TableDDElement,HashSet<Integer>>();
 			for(int i=0;i<n.children.length;i++) {
 				
 				TableDDElement child = n.children[i];
 
 				if(childrenPaths.get(child)==null) {
 					childrenPaths.put(child, new HashSet<Integer>());
 				}
 				childrenPaths.get(child).add(i);
 
 			}
 			
 			for(Entry<TableDDElement, HashSet<Integer>> e:childrenPaths.entrySet()) {
 				TableDDElement child = e.getKey();
 				
 				String childLabel = "";
 				
 				if(child instanceof TableDDNode) {
 					TableDDNode nChild = (TableDDNode) child;
 					childLabel = nChild.getKey();
 				}
 				else if (child instanceof TableDDLeaf) {
 					TableDDLeaf lChild = (TableDDLeaf) child;
 					childLabel = lChild.getId() + ":" + lChild.value;
 				}
 				
 				str += parentLabel + " -> \"" + childLabel + "\" [ label = \"" + e.getValue() + "\" ];\n";
 				
 				if(child instanceof TableDDNode) {
 					str += toString(child,processed);
 				}
 			}
 		}
 		else if (el instanceof TableDDLeaf) {
 			TableDDLeaf l = (TableDDLeaf) el;
 			str += "	\"" + l.getValue() + "\"\n";
 		}
 		
 		
 		return str;
 	}
 	
 	public String toString() {
 		String str = "";
 		str += "digraph add {\n";
 		str += "	rankdir=LR;\n";
 		str += "	node [shape = circle];\n";
 
 		str += toString(rootNode, new HashSet<Long>());
 		
 		str += "}\n";
 		
 		return str;
 	}
 }

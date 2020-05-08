 package masg.dd;
 
 import groovy.lang.Closure;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 
 import masg.dd.context.DDContext;
 import masg.dd.operations.MultiplicationOperation;
 import masg.dd.representations.dag.ImmutableDDElement;
 import masg.dd.representations.tables.TableDD;
 import masg.dd.variables.DDVariable;
 
 public class CondProbDD {
 	private ArrayList<AlgebraicDD> indepFns = new ArrayList<AlgebraicDD>();
 	private ArrayList<DDVariable> condVars = new ArrayList<DDVariable>();
 	private ArrayList<DDVariable> uncondVars = new ArrayList<DDVariable>();
 	
 	public CondProbDD(ArrayList<ArrayList<ArrayList<DDVariable>>> varsLst, List<Closure<Double>> closures) {
 		for(int i=0;i<varsLst.size();++i) {
 			ArrayList<DDVariable> conditionalVars = varsLst.get(i).get(0);
 			ArrayList<DDVariable> vars = varsLst.get(i).get(1);
 			vars.removeAll(conditionalVars);
 			
 			
 			HashSet<DDVariable> temp = new HashSet<DDVariable>(conditionalVars);
 			temp.addAll(this.condVars);
 			this.condVars = new ArrayList<DDVariable>();
 			
 			for(DDVariable v: DDContext.canonicalVariableOrdering) {
 				if(temp.contains(v)) {
 					this.condVars.add(v);
 				}
 			}
 			this.uncondVars = new ArrayList<DDVariable>(new HashSet<DDVariable>(uncondVars));
 			
 			temp = new HashSet<DDVariable>(vars);
 			temp.addAll(this.uncondVars);
 			for(DDVariable v: DDContext.canonicalVariableOrdering) {
 				if(temp.contains(v)) {
 					this.uncondVars.add(v);
 				}
 			}
 			
 			//Conditional variables have to come first
 			ArrayList<DDVariable> allVariables = new ArrayList<DDVariable>(vars);
 			allVariables.addAll(conditionalVars);
 			
 			AlgebraicDD dd = new AlgebraicDD(allVariables, closures.get(i), true);
 			indepFns.add(dd);
 			
 			
 		}
 	}
 	
 	public CondProbDD(ArrayList<DDVariable> condVars, ArrayList<DDVariable> uncondVars, ArrayList<AlgebraicDD> indepFns) {
 		this.condVars = new ArrayList<DDVariable>();
 		
 		HashSet<DDVariable> temp = new HashSet<DDVariable>(condVars);
 		for(DDVariable v: DDContext.canonicalVariableOrdering) {
 			if(temp.contains(v)) {
 				this.condVars.add(v);
 			}
 		}
 		this.uncondVars = new ArrayList<DDVariable>();
 		
 		temp = new HashSet<DDVariable>(uncondVars);
 		for(DDVariable v: DDContext.canonicalVariableOrdering) {
 			if(temp.contains(v)) {
 				this.uncondVars.add(v);
 			}
 		}
 		
 		this.indepFns = indepFns;
 	}
 	
 	public Double getValue(HashMap<DDVariable,Integer> varSpacePoint) {
 		double val = 1.0f;
 		for(AlgebraicDD dd:indepFns) {
 			val*=dd.getValue(varSpacePoint);
 		}
 		return val;
 	}
 	
 	public ProbDD toProbabilityFn() {
 		 ArrayList<ImmutableDDElement> dags = new ArrayList<ImmutableDDElement>();
 		 for(AlgebraicDD dd:indepFns) {
 			 dags.add(dd.ruleCollection);
 		 }
 		 
 		 AlgebraicDD dd = new AlgebraicDD(TableDD.build(getVariables(), dags, new MultiplicationOperation()));
 		 
 		 return new ProbDD(dd, getVariables());
 	}
 	
 	public ArrayList<DDVariable> getVariables() {
 		ArrayList<DDVariable> allVars = new ArrayList<DDVariable>(condVars);
 		allVars.addAll(uncondVars);
 		return allVars;
 	}
 	
 	public CondProbDD restrict(HashMap<DDVariable,Integer> varSpacePoint) {
 		ArrayList<AlgebraicDD> restrictedIndepFns = new ArrayList<AlgebraicDD>();
 		for(AlgebraicDD dd:indepFns) {
 			restrictedIndepFns.add(dd.restrict(varSpacePoint));
 		}
 		
 		ArrayList<DDVariable> newCondVars = new ArrayList<DDVariable>(condVars);
 		newCondVars.removeAll(varSpacePoint.keySet());
 		
 		ArrayList<DDVariable> newUncondVars = new ArrayList<DDVariable>(uncondVars);
 		newUncondVars.removeAll(varSpacePoint.keySet());
 		
 		return new CondProbDD(newCondVars,newUncondVars, restrictedIndepFns);
 	}
 	
 	public CondProbDD multiply(ProbDD pdd) {
 		ArrayList<AlgebraicDD> newIndepFns = new ArrayList<AlgebraicDD>();
 		for(AlgebraicDD dd:indepFns) {
 			newIndepFns.add(dd.multiply(pdd));
 		}
 		
 		ArrayList<DDVariable> resolvedCondVars = pdd.getVariables();
 		resolvedCondVars.retainAll(condVars);
 		
 		ArrayList<DDVariable> newCondVars = new ArrayList<DDVariable>(condVars);
 		newCondVars.removeAll(resolvedCondVars);
 		
 		ArrayList<DDVariable> newUncondVars = new ArrayList<DDVariable>(uncondVars);
 		newUncondVars.addAll(resolvedCondVars);
 		return new CondProbDD(newCondVars,newUncondVars,newIndepFns);
 		
 		
 	}
 	
 	public CondProbDD unprime() {
 		ArrayList<AlgebraicDD> newIndepFns = new ArrayList<AlgebraicDD>();
 		for(AlgebraicDD dd:indepFns) {
 			newIndepFns.add(dd.unprime());
 		}
 		
 		ArrayList<DDVariable> newCondVars = new ArrayList<DDVariable>();
 		for(DDVariable v:condVars) {
 			newCondVars.add(v.getUnprimed());
 		}
 		ArrayList<DDVariable> newUncondVars = new ArrayList<DDVariable>();
 		for(DDVariable v:uncondVars) {
 			newUncondVars.add(v.getUnprimed());
 		}
 		return new CondProbDD(newCondVars,newUncondVars,newIndepFns);
 	}
 	
 	public CondProbDD multiply(CondProbDD pdd) {
 		/*if(!pdd.condVars.isEmpty()) {
 			return null;
 		}*/
 		
 		ArrayList<AlgebraicDD> newIndepFns = new ArrayList<AlgebraicDD>();
 		
 		for(AlgebraicDD dd:indepFns) {
 			newIndepFns.add(dd.multiply(pdd));
 		}
 		
 		ArrayList<DDVariable> newCondVars = new ArrayList<DDVariable>(condVars);
 		newCondVars.removeAll(pdd.uncondVars);
 		newCondVars.addAll(pdd.condVars);
 		ArrayList<DDVariable> newUncondVars = new ArrayList<DDVariable>(uncondVars);
 		newUncondVars.addAll(pdd.uncondVars);
 
 		return new CondProbDD(newCondVars,newUncondVars,newIndepFns);
 	}
 	
 	public CondProbDD div(CondProbDD pdd) {
 		/*if(!pdd.condVars.isEmpty()) {
 			return null;
 		}*/
 		
 		ArrayList<AlgebraicDD> newIndepFns = new ArrayList<AlgebraicDD>();
 		
 		for(AlgebraicDD dd:indepFns) {
 			newIndepFns.add(dd.div(pdd));
 		}
 		
 		ArrayList<DDVariable> newCondVars = new ArrayList<DDVariable>(condVars);
 		newCondVars.removeAll(pdd.uncondVars);
 		newCondVars.addAll(pdd.condVars);
 		ArrayList<DDVariable> newUncondVars = new ArrayList<DDVariable>(uncondVars);
 		newUncondVars.addAll(pdd.uncondVars);
 
 		return new CondProbDD(newCondVars,newUncondVars,newIndepFns);
 	}
 	
 	public CondProbDD sumOut(ArrayList<DDVariable> vars) {
 		ArrayList<AlgebraicDD> newIndepFns = new ArrayList<AlgebraicDD>();
 		for(AlgebraicDD dd:indepFns) {
 			newIndepFns.add(dd.sumOut(vars));
 		}
 		
 		ArrayList<DDVariable> newCondVars = new ArrayList<DDVariable>(condVars);
 		newCondVars.removeAll(vars);
 		ArrayList<DDVariable> newUncondVars = new ArrayList<DDVariable>(uncondVars);
 		newUncondVars.removeAll(vars);
 		
 		return new CondProbDD(newCondVars,newUncondVars,newIndepFns);
 	}
 	
 	public AlgebraicDD multiply(AlgebraicDD dd) {
 		return dd.multiply(this);
 	}
 	
 	public CondProbDD plus(double value) {
 		ArrayList<AlgebraicDD> newIndepFns = new ArrayList<AlgebraicDD>();
 		for(AlgebraicDD dd:indepFns) {
 			newIndepFns.add( dd.plus(value) );
 		}
 		
 		ArrayList<DDVariable> newCondVars = new ArrayList<DDVariable>(condVars);
 		ArrayList<DDVariable> newUncondVars = new ArrayList<DDVariable>(uncondVars);
 		
 		return new CondProbDD(newCondVars,newUncondVars,newIndepFns);
 	}
 	
 	public CondProbDD normalize() {
 		ArrayList<AlgebraicDD> newIndepFns = new ArrayList<AlgebraicDD>();
 		for(AlgebraicDD dd:indepFns) {
 			newIndepFns.add( dd.div(dd.sumOut(uncondVars)) );
 		}
 		
 		ArrayList<DDVariable> newCondVars = new ArrayList<DDVariable>(condVars);
 		ArrayList<DDVariable> newUncondVars = new ArrayList<DDVariable>(uncondVars);
 		
 		return new CondProbDD(newCondVars,newUncondVars,newIndepFns);
 	}
 	
 	public final ArrayList<AlgebraicDD> getComponentFunctions() {
 		return new ArrayList<AlgebraicDD>(indepFns);
 	}
 	
 	public String toString() {
 		String str = "";
 		str += "Conditional:" + condVars + "\n";
 		str += "Non-conditonal:" + uncondVars + "\n";
 		for(AlgebraicDD dd:indepFns) {
 			str+=dd.toString() + "\n";
 		}
 		return str;
 	}
 	static public CondProbDD build(ArrayList<ArrayList<ArrayList<DDVariable>>> varsLst, List<Closure<Double>> closures) {
 		return new CondProbDD(varsLst,closures);
 	}
 }

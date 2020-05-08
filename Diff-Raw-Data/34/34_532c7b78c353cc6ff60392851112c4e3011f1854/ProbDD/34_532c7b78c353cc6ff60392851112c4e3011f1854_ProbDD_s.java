 package masg.dd;
 
 import groovy.lang.Closure;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
import masg.dd.representations.tables.TableDD;
 import masg.dd.variables.DDVariable;
 
 public class ProbDD {
 
 	protected AlgebraicDD dd;
 	protected ArrayList<DDVariable> variables;
 	public ProbDD(ArrayList<DDVariable> vars, Closure<Double>... c) {
 		
 		variables = vars;
 		dd = new AlgebraicDD(vars, c);
 	}
 	
 	public ProbDD(AlgebraicDD dd,ArrayList<DDVariable> variables) {
 		this.dd = dd;
 		this.variables = variables;
 	}
 	
 	protected ProbDD() {
 		
 	}
 	
 	public ProbDD multiply(CondProbDD condProbDD) {
 		return new ProbDD(dd.multiply(condProbDD),variables);
 	}
 	
 	public ProbDD multiply(CondProbDD condProbDD, ArrayList<DDVariable> resVariables) {
 		return new ProbDD(dd.multiply(condProbDD),resVariables);
 	}
 	
 	public ProbDD multiply(ProbDD probDD) {
 		return new ProbDD(dd.multiply(probDD.dd),variables);
 	}
 	
 	public ProbDD div(ProbDD probDD) {
 		return new ProbDD(dd.div(probDD.dd),variables);
 	}
 	
 	public ProbDD sumOut(ArrayList<DDVariable> vars) {
 		return new ProbDD(dd.sumOut(vars),variables);
 	}
 	public Double getValue(HashMap<DDVariable,Integer> varSpacePoint) {
 		return getDD().getValue(varSpacePoint);
 	}
 	
 	
 	public final AlgebraicDD getDD() {
 		return dd;
 	}
 	
 	public final ArrayList<DDVariable> getVariables() {
 		return variables;
 	}
 	
 	static public ProbDD buildProbability(ArrayList<DDVariable> vars, Closure<Double>... c) {
 		return new ProbDD(vars,c);
 	}
 	
 	public CondProbDD toConditionalProbabilityFn() {
 		ArrayList<DDVariable> condVars = new ArrayList<DDVariable>(); 
 		ArrayList<DDVariable> uncondVars = new ArrayList<DDVariable>(getVariables());
 		ArrayList<AlgebraicDD> indepFns = new ArrayList<AlgebraicDD>();
 		indepFns.add(dd);		
 		return new CondProbDD(condVars,uncondVars,indepFns);
 	}
 	
 	public String toString() {
 		return dd.toString();
 	}
 
 }

 package sw10.spideybc.analysis;
 
 import com.ibm.wala.ssa.ISSABasicBlock;
 
 import net.sf.javailp.Constraint;
 import net.sf.javailp.Linear;
 import net.sf.javailp.Operator;
 import net.sf.javailp.OptType;
 import net.sf.javailp.Problem;
 import net.sf.javailp.Result;
 import net.sf.javailp.Solver;
 import net.sf.javailp.SolverFactory;
 import net.sf.javailp.SolverFactoryLpSolve;
 
 public class CGNodeLPProblem {
 	private SolverFactory factory;
 	private Problem problem;
 	private Linear objective;
 	
 	public CGNodeLPProblem() {
 		this.factory = new SolverFactoryLpSolve();
 		this.factory.setParameter(Solver.VERBOSE, 0);
 		this.problem = new Problem();
 		this.objective = new Linear();
 	}
 	
 	public void addBasicBlockVariable(ISSABasicBlock basicBlock) {
 		String variable = "bb" + basicBlock.getGraphNodeId();
 		this.objective.add(1, variable);
 		this.problem.setVarType(variable, Integer.class);
 	}
 	
 	public void addEdgeLabelVariable(String edgeLabel) {
 		problem.setVarType(edgeLabel, Integer.class);
 	}
 	
 	public void addConstraint(Linear lhs, Operator operator, Number rhs) {
 		Constraint constraint = new Constraint(lhs, operator, rhs);
 		problem.add(constraint);
 	}
 	
 	public Result getResults() {
 		problem.setObjective(objective, OptType.MAX);
 		Solver solver = factory.get();
		System.out.println(problem);
		System.out.println(solver.solve(problem));
 		return solver.solve(problem);
 	}
 	
 	public Problem getProblem() {
 		return this.problem;
 	}
 }

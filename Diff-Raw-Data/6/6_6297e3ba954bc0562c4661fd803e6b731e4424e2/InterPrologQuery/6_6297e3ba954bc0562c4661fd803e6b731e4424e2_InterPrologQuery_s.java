 package org.jpc.engine.interprolog;
 
 import static java.util.Arrays.asList;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.jpc.Jpc;
 import org.jpc.query.DeterministicPrologQuery;
 import org.jpc.query.QuerySolution;
 import org.jpc.term.Atom;
 import org.jpc.term.Compound;
 import org.jpc.term.ListTerm;
 import org.jpc.term.Term;
 import org.jpc.term.Variable;
 import org.jpc.term.interprolog.InterPrologTermWrapper;
 
 import com.declarativa.interprolog.TermModel;
 import com.declarativa.interprolog.util.VariableNode;
 
 public class InterPrologQuery extends DeterministicPrologQuery {
 
 	private com.declarativa.interprolog.PrologEngine wrappedInterPrologEngine;
 	private TermModel interPrologQuery;
 	
 	
 	//public static final String ALL_VARIABLES = JPC_VAR_PREFIX + "ALL_VARIABLES";
 	
 	public InterPrologQuery(InterPrologEngine prologEngine, Term goal, Jpc context) {
 		super(prologEngine, goal, context);
 		wrappedInterPrologEngine = prologEngine.getWrappedEngine();
 		InterPrologTermWrapper termWrapper = InterPrologBridge.fromJpcToInterProlog(getInstrumentedGoal());
 		interPrologQuery = termWrapper.getTermModel();
 		//interPrologQuery = asInterPrologQuery(termWrapper);
 	}
 
 //	private TermModel asInterPrologQuery(InterPrologTermWrapper interPrologTermWrapper) {
 //		TermModel listUnification = TermModel.makeList(interPrologTermWrapper.getVariablesUnification().toArray(new TermModel[]{}));
 //		TermModel unificationAllVariables = new TermModel("=", new TermModel[]{listUnification, listUnification});
 //		TermModel bindingTerm = new TermModel(",", new TermModel[]{unificationAllVariables, interPrologTermWrapper.getTermModel()});
 //		return bindingTerm;
 //	}
 	
 	@Override
 	protected Term instrumentGoal(Term goal) {
 		ListTerm mapVarsNames = new ListTerm();
 		for(Variable var : goal.getNonAnonymousVariables()) {
 			Compound varNameEntry = new Compound("=", asList(new Atom(var.getName()), var));
 			mapVarsNames.add(varNameEntry);
 		}
 		Term mapVarsNamesTerm = mapVarsNames.asTerm();
 		Compound dummyUnification = new Compound("=", asList(mapVarsNamesTerm, mapVarsNamesTerm));
		Term superInstrumentGoal = super.instrumentGoal(goal);
		return new Compound(",", asList(dummyUnification, superInstrumentGoal));
 	}
 	
 	@Override
 	public synchronized QuerySolution basicOneSolution() {
 //		interPrologQuery = new TermModel("catch", new TermModel[]{
 //			new TermModel("is", new TermModel[]{new TermModel(new VariableNode(0)), new TermModel(new VariableNode(0))}),
 //			new TermModel(new VariableNode(1)),
 //			new TermModel("true")
 //		});
 		
 		TermModel boundTerm = wrappedInterPrologEngine.deterministicGoal(interPrologQuery);
 		if(boundTerm == null)
 			return null;
 		TermModel listUnification = (TermModel) ((TermModel)boundTerm.getChild(0)).getChild(1);
 		TermModel[] unifications = listUnification.flatList();
 		Map<Integer, String> variablesNames = new HashMap<>(); //a map associating to each InterProlog variable a name
 		Map<String, InterPrologTermWrapper> interPrologBindings = new HashMap<>(); //a solution to the query mapping variable names to InterPrologWrapper terms
 		for(TermModel unification : unifications) {
 			String name = (String)((TermModel)unification.getChild(0)).node;
 			TermModel interPrologTerm = (TermModel)((TermModel)unification.getChild(1));
 			if(interPrologTerm.node instanceof VariableNode) {
 				VariableNode variable = (VariableNode)interPrologTerm.node;
 				variablesNames.put(InterPrologTermWrapper.getVariableCode(variable), name);
 			}
 			interPrologBindings.put(name, new InterPrologTermWrapper(interPrologTerm, variablesNames));
 		}
 		Map<String, Term> oneSolution = new HashMap<>();
 		for(Entry<String, InterPrologTermWrapper> interPrologBinding : interPrologBindings.entrySet()) {
 			Term term = InterPrologBridge.fromInterPrologToJpc(interPrologBinding.getValue());
 			oneSolution.put(interPrologBinding.getKey(), term);
 		}
 		return new QuerySolution(oneSolution, getPrologEngine(), getJpcContext());
 	}
 	
 	@Override
 	public boolean isAbortable() {
 		return true;
 	}
 	
 	@Override
 	protected void basicAbort() {
 		((InterPrologEngine)getPrologEngine()).getWrappedEngine().interrupt();
 		super.basicAbort();
 	}
 	
 }

 package uk.ac.ic.doc.gander.analysis;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.python.pydev.parser.jython.SimpleNode;
 import org.python.pydev.parser.jython.ast.Attribute;
 import org.python.pydev.parser.jython.ast.Call;
 import org.python.pydev.parser.jython.ast.FunctionDef;
 import org.python.pydev.parser.jython.ast.Name;
 import org.python.pydev.parser.jython.ast.NameTok;
 import org.python.pydev.parser.jython.ast.VisitorBase;
 import org.python.pydev.parser.jython.ast.exprType;
 
 import uk.ac.ic.doc.gander.analysis.PassedVariableFinder.PassedVar;
 import uk.ac.ic.doc.gander.analysis.dominance.Domination;
 import uk.ac.ic.doc.gander.analysis.dominance.Postdomination;
 import uk.ac.ic.doc.gander.cfg.Model;
 import uk.ac.ic.doc.gander.cfg.model.BasicBlock;
 import uk.ac.ic.doc.gander.cfg.model.Cfg;
 import uk.ac.ic.doc.gander.cfg.model.Function;
 import uk.ac.ic.doc.gander.cfg.model.Module;
 import uk.ac.ic.doc.gander.flowinference.types.Type;
 import uk.ac.ic.doc.gander.flowinference.types.TypeResolutionVisitor;
 
 public class SignatureBuilder {
 
 	/**
 	 * Return the blocks which are control-dependent on the given block.
 	 */
 	private static Set<BasicBlock> controlDependentBlocks(BasicBlock block,
 			Cfg graph) {
 		Set<BasicBlock> controlDependentBlocks = new HashSet<BasicBlock>();
 
 		controlDependentBlocks.add(block);
 
 		Domination dom = new Domination(graph);
 		controlDependentBlocks.addAll(dom.dominators(block));
 
 		Postdomination postdom = new Postdomination(graph);
 		controlDependentBlocks.addAll(postdom.dominators(block));
 
 		return controlDependentBlocks;
 	}
 
 	/**
 	 * Finds calls that target a given SSA subscripted variable in a single
 	 * basic block.
 	 */
 	private static class IntraBlockVariableMatcher extends BasicBlockTraverser {
 
 		private Set<Call> calls = new HashSet<Call>();
 		private Name target;
 		private int subscript;
 		private SSAVariableSubscripts renamer;
 
 		public IntraBlockVariableMatcher(BasicBlock containingBlock,
 				Name target, int subscript, SSAVariableSubscripts renamer)
 				throws Exception {
 			this.target = target;
 			this.subscript = subscript;
 			this.renamer = renamer;
 			for (SimpleNode node : containingBlock) {
 				node.accept(this);
 			}
 		}
 
 		@Override
 		public Object visitCall(Call node) throws Exception {
 			if (node.func instanceof Attribute) {
 				Attribute fieldAccess = (Attribute) node.func;
 				if (fieldAccess.value instanceof Name) {
 					Name candidate = (Name) fieldAccess.value;
 
 					if (isMatch(candidate))
 						calls.add(node);
 				}
 			}
 			return null;
 		}
 
 		private boolean isMatch(Name candidate) {
 			return isNameMatch(candidate) && isSubscriptMatch(candidate);
 		}
 
 		private boolean isNameMatch(Name candidate) {
 			return target.id.equals(candidate.id);
 		}
 
 		private boolean isSubscriptMatch(Name candidate) {
 			return subscript == renamer.subscript(candidate);
 		}
 
 		Set<Call> matchingCalls() {
 			return calls;
 		}
 	}
 
 	/**
 	 * Build a signature for the given variable by looking at the given blocks
 	 * and recursing into any calls they make.
 	 * 
 	 * Signatures are in the form of a set of all calls that must be executed in
 	 * every case on the object held in the variable. This is the set of calls
 	 * that are control-dependent on the given name and operate not only on the
 	 * same name but on the same SSA renaming of the name. This ensures that
 	 * calls which may happen after re-assigning to a variable aren't included.
 	 */
 	public Set<Call> signature(Name variable, BasicBlock containingBlock,
 			Module module, Cfg graph, Model model) throws Exception {
 		return buildSignature(variable, containingBlock, module, graph, model);
 	}
 
 	/**
 	 * Build a signature for the given variable by looking at the given blocks
 	 * and recursing into any calls they make.
 	 */
 	private static Set<Call> buildSignature(Name variable,
 			BasicBlock controlBlock, Module module, Cfg graph, Model model)
 			throws Exception {
 
 		// FIXME: This will loop infinitely with recursive calls
 
 		Set<BasicBlock> blocks = controlDependentBlocks(controlBlock, graph);
 
 		Set<Call> calls = getPartialSignatureFromVariableUseInFunction(
 				variable, blocks, graph);
 
 		calls.addAll(getPartialSignatureFromPassingVariableToCalls(variable.id,
 				blocks, module, model));
 		return calls;
 	}
 
 	/**
 	 * Given a particular use of a variable, return the part of its signature
 	 * that is derived from uses occurring solely within the same function.
 	 */
 	private static Set<Call> getPartialSignatureFromVariableUseInFunction(
 			Name variableAtLocation, Set<BasicBlock> blocksToSearch, Cfg graph)
 			throws Exception {
 		Set<Call> calls = new HashSet<Call>();
 
 		SSAVariableSubscripts ssa = new SSAVariableSubscripts(graph);
 		int permittedSubscript = ssa.subscript(variableAtLocation);
 
 		for (BasicBlock block : blocksToSearch) {
 			IntraBlockVariableMatcher matcher = new IntraBlockVariableMatcher(
 					block, variableAtLocation, permittedSubscript, ssa);
 			calls.addAll(matcher.matchingCalls());
 		}
 
 		return calls;
 	}
 
 	/**
 	 * Given a variable name, return the part of its signature that is derived
 	 * from looking at any other calls its passed to.
 	 */
 	private static Set<Call> getPartialSignatureFromPassingVariableToCalls(
 			String variable, Iterable<BasicBlock> blocksToSearch,
 			Module module, Model model) throws Exception {
 
 		Set<Call> calls = new HashSet<Call>();
 
 		Set<PassedVar> passes = new PassedVariableFinder(variable,
 				blocksToSearch).passes();
 		for (PassedVar pass : passes) {
 			Call call = pass.getCall();
 			Function function = resolveFunction(model, module, call);
 			if (function != null) {
 				FunctionDef functiondef = function.getFunctionDef();
 				calls.addAll(getSignatureForPassedVariable(pass, functiondef,
						(Module)function.getParent(), model));
 			}
 		}
 
 		return calls;
 	}
 
 	/**
 	 * Given a call, attempt to find the function being called.
 	 */
 	private static class FunctionResolver extends VisitorBase {
 
 		// TODO: deal with situation where function is not a simple
 		// variable name. It might be qualified with a module name, for
 		// instance.
 
 		private Function function;
 		private Model model;
 		private Module enclosingModule;
 
 		FunctionResolver(Call call, Module enclosingModule, Model model)
 				throws Exception {
 			this.enclosingModule = enclosingModule;
 			this.model = model;
 			function = (Function) call.func.accept(this);
 		}
 
 		@Override
 		public Object visitAttribute(Attribute node) throws Exception {
 			if (node.value instanceof Name) {
 				TypeResolutionVisitor typer = new TypeResolutionVisitor(
 						enclosingModule.getAst());
 				Name name = ((Name) node.value);
 				// XXX: using string not good enough
 				Type type = typer.typeOf(name.id);
 				if (type instanceof uk.ac.ic.doc.gander.flowinference.types.Module) {
 					Module module = model.getTopLevelPackage().getModules()
 							.get(name.id);
					return module.getFunctions().get(((NameTok) node.attr).id);
 				}
 			}
 			return null;
 		}
 
 		@Override
 		public Object visitName(Name node) throws Exception {
 			// TODO: Handle case where name doesn't correspond to a function
 			// in the local module. This can happen with builtins for
 			// instance.
 			return enclosingModule.getFunctions().get(node.id);
 		}
 
 		@Override
 		public void traverse(SimpleNode node) throws Exception {
 			// Don't traverse by default
 		}
 
 		@Override
 		protected Object unhandled_node(SimpleNode node) throws Exception {
 			return null;
 		}
 
 		public Function getFunction() {
 			return function;
 		}
 	}
 
 	private static Function resolveFunction(Model model, Module module,
 			Call call) throws Exception {
 		Function function = new FunctionResolver(call, module, model)
 				.getFunction();
 
 		if (function == null)
 			System.err.println("Warning: unable to resolve function: "
 					+ call.func);
 
 		return function;
 	}
 
 	/**
 	 * Return signature implied by passing variable to call.
 	 */
 	private static Set<Call> getSignatureForPassedVariable(PassedVar pass,
 			FunctionDef function, Module module, Model model) throws Exception {
 
 		Set<Call> calls = new HashSet<Call>();
 
 		for (Name param : resolveParameterNames(pass, function)) {
 			calls.addAll(getSignatureForParameter(param, function, module,
 					model));
 		}
 
 		return calls;
 	}
 
 	/**
 	 * With respect to a given parameter in a function, return the set of
 	 * methods calls on the parameter that are always executed if the function
 	 * is called.
 	 * 
 	 * This is the set of calls that postdominate the entry node of the
 	 * function.
 	 */
 	private static Set<Call> getSignatureForParameter(Name param,
 			FunctionDef function, Module module, Model model) throws Exception {
 		Cfg graph = new Cfg(function);
 		return buildSignature(param, graph.getStart(), module, graph, model);
 	}
 
 	/**
 	 * Map passing specification to the parameter names they correspond to in
 	 * the function being called.
 	 */
 	private static Set<Name> resolveParameterNames(PassedVar pass,
 			FunctionDef function) {
 		Set<Name> names = new HashSet<Name>();
 
 		for (Integer pos : pass.getPositions()) {
 			names.add((Name) function.args.args[pos]);
 		}
 
 		for (String keyword : pass.getKeywords()) {
 			Name name = findParamNameNode(keyword, function);
 			if (name == null)
 				throw new Error("Function called with incorrect keyword");
 			names.add(name);
 		}
 
 		return names;
 	}
 
 	private static Name findParamNameNode(String name, FunctionDef function) {
 		for (exprType expr : function.args.args) {
 			Name candidate = ((Name) expr);
 			if (candidate.id.equals(name))
 				return candidate;
 		}
 		return null;
 	}
 }

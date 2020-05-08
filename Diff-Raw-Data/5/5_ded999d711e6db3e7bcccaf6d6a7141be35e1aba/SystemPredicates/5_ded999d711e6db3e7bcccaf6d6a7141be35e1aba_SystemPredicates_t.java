 package org.suite.predicates;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Stack;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.parser.Operator;
 import org.suite.Context;
 import org.suite.Singleton;
 import org.suite.doer.Prover;
 import org.suite.doer.Prover.Backtracks;
 import org.suite.doer.TermParser.TermOp;
 import org.suite.node.Atom;
 import org.suite.node.Node;
 import org.suite.node.Station;
 import org.suite.node.Tree;
 
 public class SystemPredicates {
 
 	public interface SystemPredicate {
 		public boolean prove(Prover prover, Node parameter);
 	}
 
 	private Map<Node, SystemPredicate> predicates = new HashMap<Node, SystemPredicate>();
 
 	private Prover prover;
 
 	public SystemPredicates(Prover prover) {
 		this.prover = prover;
 
 		addPredicate("find.all", new FindAll());
 		addPredicate("not", new Not());
 		addPredicate("once", new Once());
 		addPredicate("temporary", new Temporary());
 
 		addPredicate("bound", new EvalPredicates.Bound());
 		addPredicate("eval.js", new EvalPredicates.EvalJs());
 		addPredicate("eval.fun", new EvalPredicates.EvalFunctional());
 		addPredicate(TermOp.LE____, new EvalPredicates.Compare());
 		addPredicate(TermOp.LT____, new EvalPredicates.Compare());
 		addPredicate(TermOp.GE____, new EvalPredicates.Compare());
 		addPredicate(TermOp.GT____, new EvalPredicates.Compare());
 		addPredicate("let", new EvalPredicates.Let());
 		addPredicate("is.atom", new EvalPredicates.IsAtom());
 		addPredicate("is.int", new EvalPredicates.IsInt());
 		addPredicate("is.string", new EvalPredicates.IsString());
 		addPredicate("is.tree", new EvalPredicates.IsTree());
 		addPredicate("map.retrieve", new EvalPredicates.MapRetrieve());
 		addPredicate("map.erase", new EvalPredicates.MapErase());
 		addPredicate("starts.with", new EvalPredicates.StartsWith());
 		addPredicate("tree", new EvalPredicates.TreePredicate());
 
 		addPredicate("dump", new IoPredicates.Dump());
 		addPredicate("exec", new IoPredicates.Exec());
 		addPredicate("nl", new IoPredicates.Nl());
 		addPredicate("write", new IoPredicates.Write());
 
 		addPredicate("rules", new RuleSetPredicates.GetAllRules());
 		addPredicate("list", new RuleSetPredicates.ListPredicates());
 		addPredicate("import", new RuleSetPredicates.Import());
 		addPredicate("assert", new RuleSetPredicates.Assert());
 		addPredicate("retract", new RuleSetPredicates.Retract());
 	}
 
 	public Boolean call(Node query) {
 		SystemPredicate predicate;
 		Node pass = query;
 
 		if ((predicate = predicates.get(query)) != null)
 			pass = Atom.nil;
 		else {
 			Tree tree = Tree.decompose(query);
 			if (tree != null)
 				if (tree.getOperator() != TermOp.SEP___)
 					predicate = predicates.get(new Tree(tree.getOperator()));
 				else {
 					predicate = predicates.get(tree.getLeft());
 					pass = tree.getRight();
 				}
 		}
 
 		return (predicate != null) ? predicate.prove(prover, pass) : null;
 	}
 
 	private void addPredicate(Operator operator, SystemPredicate pred) {
 		predicates.put(new Tree(operator), pred);
 	}
 
 	private void addPredicate(String name, SystemPredicate pred) {
 		predicates.put(Atom.create(name), pred);
 	}
 
 	private class FindAll implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
 			final Stack<Node> stack = new Stack<Node>();
 			final Node params[] = Predicate.getParameters(ps, 3);
 
 			Tree subGoal = new Tree(TermOp.AND___, params[1], new Station() {
 				public boolean run(Backtracks backtracks) {
 					stack.push(params[0].finalNode());
 					return false;
 				}
 			});
 			Prover subProver = new Prover(prover);
 			subProver.prove(subGoal);
 			subProver.undoAllBinds();
 
 			Node result = Atom.nil;
 			while (!stack.isEmpty())
 				result = new Tree(TermOp.AND___, stack.pop(), result);
 
 			return prover.bind(params[2], result);
 		}
 	}
 
 	private class Not implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
			return !new Prover(prover).prove(ps);
 		}
 	}
 
 	private class Once implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
			return new Prover(prover).prove(ps);
 		}
 	}
 
 	private class Temporary implements SystemPredicate {
 		private final AtomicInteger counter = new AtomicInteger();
 
 		public boolean prove(Prover prover, Node ps) {
 			Context hiddenContext = Singleton.get().getHiddenContext();
 			String name = "TEMP" + counter.getAndIncrement();
 			return prover.bind(ps, Atom.create(hiddenContext, name));
 		}
 	}
 
 }

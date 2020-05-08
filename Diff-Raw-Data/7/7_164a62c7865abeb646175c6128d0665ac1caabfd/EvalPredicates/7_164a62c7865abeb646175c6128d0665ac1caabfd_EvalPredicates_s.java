 package org.suite.predicates;
 
 import java.io.IOException;
 import java.util.Map;
 import java.util.TreeMap;
 
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 import javax.script.ScriptException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.fp.Interpreter;
 import org.parser.Operator;
 import org.suite.doer.Comparer;
 import org.suite.doer.Formatter;
 import org.suite.doer.Prover;
 import org.suite.doer.TermParser;
 import org.suite.doer.TermParser.TermOp;
 import org.suite.node.Atom;
 import org.suite.node.Int;
 import org.suite.node.Node;
 import org.suite.node.Reference;
 import org.suite.node.Str;
 import org.suite.node.Tree;
 import org.suite.predicates.SystemPredicates.SystemPredicate;
 import org.util.Util;
 
 public class EvalPredicates {
 
 	private static Comparer comparer = Comparer.comparer;
 
 	public static class Bound implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
 			return !(ps.finalNode() instanceof Reference);
 		}
 	}
 
 	public static class Compare implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
 			Tree tree = (Tree) ps;
 			switch ((TermOp) tree.getOperator()) {
 			case LE____:
 				return comparer.compare(tree.getLeft(), tree.getRight()) <= 0;
 			case LT____:
 				return comparer.compare(tree.getLeft(), tree.getRight()) < 0;
 			case GE____:
 				return comparer.compare(tree.getLeft(), tree.getRight()) >= 0;
 			case GT____:
 				return comparer.compare(tree.getLeft(), tree.getRight()) > 0;
 			default:
 				throw new RuntimeException("Unknown comparison");
 			}
 		}
 	}
 
 	public static class EvalJs implements SystemPredicate {
 		private final ScriptEngine engine = new ScriptEngineManager()
 				.getEngineByExtension("js");
 
 		public boolean prove(Prover prover, Node ps) {
 			String js = Formatter.display(ps);
 			try {
 				engine.eval(js);
 			} catch (ScriptException ex) {
 				log.error(js, ex);
 				return false;
 			}
 			return true;
 		}
 	}
 
 	public static class EvalFunctional implements SystemPredicate {
 		private static final Interpreter interpreter = new Interpreter();
 		static {
 			try {
 				TermParser parser = new TermParser();
 				interpreter.addFunctions(parser.parseClassPathFile("auto.fp"));
 			} catch (IOException ex) {
 				log.error(EvalFunctional.class, ex);
 			}
 		}
 
 		public boolean prove(Prover prover, Node ps) {
 			final Node params[] = Predicate.getParameters(ps, 2);
 			return prover.bind(interpreter.evaluate(params[0]), params[1]);
 		}
 	}
 
 	public static class IsAtom implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
 			return ps.finalNode() instanceof Atom;
 		}
 	}
 
 	public static class IsInt implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
 			return ps.finalNode() instanceof Int;
 		}
 	}
 
 	public static class IsString implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
 			return ps.finalNode() instanceof Str;
 		}
 	}
 
 	public static class IsTree implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
 			return ps.finalNode() instanceof Tree;
 		}
 	}
 
 	private static final Map<Node, Node> store = new TreeMap<Node, Node>();
 
 	public static class MapRetrieve implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
 			final Node params[] = Predicate.getParameters(ps, 2);
 			Node value = store.get(params[0]);
 			if (value == null)
 				store.put(params[0], value = new Reference());
 			return prover.bind(value, params[1]);
 		}
 	}
 
 	public static class MapErase implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
 			return store.remove(ps) != null;
 		}
 	}
 
 	public static class StartsWith implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
 			final Node params[] = Predicate.getParameters(ps, 2);
 			Node p0 = params[0].finalNode(), p1 = params[1].finalNode();
 
 			return p0 instanceof Atom && p1 instanceof Atom
 					&& ((Atom) p0).getName().startsWith(((Atom) p1).getName());
 		}
 	}
 
 	public static class TreePredicate implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
 			final Node params[] = Predicate.getParameters(ps, 4);
 			Node p = params[0].finalNode();
 			Node p2 = params[2].finalNode();
 
 			if (p instanceof Tree) {
 				Tree tree = (Tree) p;
 				Atom oper = Atom.create(tree.getOperator().getName());
 				return prover.bind(tree.getLeft(), params[1])
 						&& prover.bind(oper, p2)
 						&& prover.bind(tree.getRight(), params[3]);
 			} else if (p2 instanceof Atom) {
 				Operator operator = TermOp.find(((Atom) p2).getName());
 				return prover.bind(p, new Tree(operator, params[1], params[3]));
 			} else
 				return false;
 		}
 	}
 
 	public static class Let implements SystemPredicate {
 		public boolean prove(Prover prover, Node ps) {
 			final Node params[] = Predicate.getParameters(ps, 2);
 			int result = evaluate(params[1]);
 			return prover.bind(Int.create(result), params[0]);
 		}
 
 		public int evaluate(Node node) {
 			int result = 0;

 			Tree tree = Tree.decompose(node);
 			if (tree != null) {
 				int a = evaluate(tree.getLeft()), b = evaluate(tree.getRight());
 
 				switch ((TermOp) tree.getOperator()) {
 				case PLUS__:
 					result = a + b;
 					break;
 				case MINUS_:
 					result = a - b;
 					break;
 				case MULT__:
 					result = a * b;
 					break;
 				case DIVIDE:
 					result = a / b;
 				}
 			} else if (node instanceof Int)
 				result = ((Int) node).getNumber();
 
 			return result;
 		}
 	}
 
 	private static Log log = LogFactory.getLog(Util.currentClass());
 
 }

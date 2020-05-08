 package suite.lp.doer;
 
 import java.util.Date;
 import java.util.List;
 import java.util.ListIterator;
 
 import suite.lp.Journal;
 import suite.lp.kb.Prototype;
 import suite.lp.kb.Rule;
 import suite.lp.kb.RuleSet;
 import suite.lp.predicate.SystemPredicates;
 import suite.node.Atom;
 import suite.node.Data;
 import suite.node.Node;
 import suite.node.Tree;
 import suite.node.io.TermParser.TermOp;
 import suite.util.FormatUtil;
 import suite.util.FunUtil.Fun;
 import suite.util.FunUtil.Source;
 import suite.util.LogUtil;
 
 public class Prover {
 
 	private ProverConfig config;
 	private ProveTracer tracer;
 	private SystemPredicates systemPredicates = new SystemPredicates(this);
 
 	private static final Node OK = Atom.NIL;
 	private static final Node FAIL = Atom.create("fail");
 
 	private Node rem, alt; // remaining, alternative
 
 	private Journal journal = new Journal();
 
 	public Prover(Prover prover) {
 		this(prover.config, prover.tracer);
 	}
 
 	public Prover(RuleSet ruleSet) {
 		this(new ProverConfig(ruleSet));
 	}
 
 	public Prover(ProverConfig proverConfig) {
 		this(proverConfig, null);
 	}
 
 	public Prover(ProverConfig proverConfig, ProveTracer tracer) {
 		this.config = proverConfig;
 		this.tracer = tracer;
 	}
 
 	/**
 	 * Try to prove a query clause. Perform bindings on the way.
 	 * 
 	 * @param query
 	 *            Clause to be proved.
 	 * @return true if success.
 	 */
 	public boolean prove(Node query) {
 		if (config.isTrace())
 			try {
 				tracer = new ProveTracer(config);
 				return prove0(query);
 			} finally {
 				String d = FormatUtil.dtFmt.format(new Date());
 				String dump = tracer.getDump();
 				LogUtil.info("-- Prover dump at " + d + " --\n" + dump);
 			}
 		else
 			return prove0(query);
 	}
 
 	public boolean prove0(Node query) {
 		rem = OK;
 		alt = FAIL;
 
 		while (true) {
 			// LogUtil.info(Formatter.dump(query));
 
 			Tree tree = Tree.decompose(query);
 			if (tree != null) {
 				final Node left = tree.getLeft(), right = tree.getRight();
 
 				switch ((TermOp) tree.getOperator()) {
 				case OR____:
 					final int pit = journal.getPointInTime();
 					Node bt = new Data<Source<Boolean>>(new Source<Boolean>() {
 						public Boolean source() {
 							journal.undoBinds(pit);
 							return Boolean.TRUE;
 						}
 					});
 
 					alt = andTree(bt, orTree(andTree(right, rem), alt));
 					query = left;
 					continue;
 				case AND___:
 					rem = andTree(right, rem);
 					query = left;
 					continue;
 				case EQUAL_:
 					query = isSuccess(bind(left, right));
 					break;
 				default:
 				}
 			} else if (query instanceof Data) {
 				Object data = ((Data<?>) query).getData();
 				Object bool = ((Source<?>) data).source();
 				query = isSuccess((Boolean) bool);
 				continue;
 			}
 
 			Boolean result = systemPredicates.call(query);
 			if (result != null)
 				query = isSuccess(result);
 
 			// Not handled above
 			if (query == OK)
 				if (rem != OK) {
 					query = rem;
 					rem = OK;
 				} else
 					return true;
 			else if (query == FAIL)
 				if (alt != FAIL) {
 					query = alt;
 					alt = FAIL;
 					rem = OK;
 				} else
 					return false;
 			else {
 				boolean isTrace = config.isTrace();
 				Prototype prototype = isTrace ? Prototype.get(query) : null;
 				Node head = prototype != null ? prototype.getHead() : null;
 				Atom atom = head instanceof Atom ? (Atom) head : null;
 				String name = atom != null ? atom.getName() : null;
 				isTrace &= !config.getNoTracePredicates().contains(name);
 
 				if (!isTrace)
 					query = expand(query);
 				else
 					query = tracer.expandWithTrace(query, this, new Fun<Node, Node>() {
 						public Node apply(Node node) {
 							return expand(node);
 						}
 					});
 			}
 		}
 	}
 
 	/**
 	 * Expands an user predicate (with many clauses) to a chain of logic.
 	 * 
 	 * @param query
 	 *            The invocation pattern.
 	 * @return The chained node.
 	 */
 	private Node expand(Node query) {
 		final Node alt0 = alt;
 		Node ret = FAIL;
 
 		List<Rule> rules = config.ruleSet().searchRule(query);
 		ListIterator<Rule> iter = rules.listIterator(rules.size());
 
 		while (iter.hasPrevious()) {
 			Rule rule = iter.previous();
 
 			Generalizer generalizer = new Generalizer();
 			generalizer.setCut(new Data<Source<Boolean>>(new Source<Boolean>() {
 				public Boolean source() {
 					alt = alt0;
					return Boolean.TRUE;
 				}
 			}));
 
 			Node head = generalizer.generalize(rule.getHead());
 			Node tail = generalizer.generalize(rule.getTail());
 
 			ret = Tree.create(TermOp.OR____ //
 					, Tree.create(TermOp.AND___ //
 							, Tree.create(TermOp.EQUAL_ //
 									, query //
 									, head //
 							) //
 							, tail //
 					) //
 					, ret //
 					);
 		}
 
 		return ret;
 	}
 
 	/**
 	 * Performs binding of two items.
 	 * 
 	 * @return true if success.
 	 */
 	public boolean bind(Node left, Node right) {
 		return Binder.bind(left, right, journal);
 	}
 
 	/**
 	 * Resets all bind done by this prover.
 	 */
 	public void undoAllBinds() {
 		journal.undoAllBinds();
 	}
 
 	private Node andTree(Node n0, Node n1) {
 		return formOp(n0, n1, OK, FAIL, TermOp.AND___);
 	}
 
 	private Node orTree(Node n0, Node n1) {
 		return formOp(n0, n1, FAIL, OK, TermOp.OR____);
 	}
 
 	private Node formOp(Node n0, Node n1, Node bail, Node done, TermOp op) {
 		if (n0 == bail)
 			return n1;
 		else if (n1 == bail)
 			return n0;
 		else if (n0 == done || n1 == done)
 			return done;
 		else
 			return Tree.create(op, n0, n1);
 	}
 
 	private Node isSuccess(boolean b) {
 		return b ? OK : FAIL;
 	}
 
 	public ProverConfig config() {
 		return config;
 	}
 
 	public RuleSet ruleSet() {
 		return config.ruleSet();
 	}
 
 	public ProveTracer getTracer() {
 		return tracer;
 	}
 
 	/**
 	 * Goals ahead.
 	 */
 	public Node getRemaining() {
 		return rem;
 	}
 
 	public void setRemaining(Node rem) {
 		this.rem = rem;
 	}
 
 	/**
 	 * Alternative path to succeed.
 	 */
 	public Node getAlternative() {
 		return alt;
 	}
 
 	public void setAlternative(Node alt) {
 		this.alt = alt;
 	}
 
 	/**
 	 * The roll-back log of variable binds.
 	 */
 	public Journal getJournal() {
 		return journal;
 	}
 
 }

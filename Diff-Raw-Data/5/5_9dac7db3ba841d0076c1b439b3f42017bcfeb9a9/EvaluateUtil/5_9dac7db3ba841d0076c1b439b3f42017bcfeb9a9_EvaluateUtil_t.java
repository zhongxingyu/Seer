 package suite;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 
 import suite.fp.FunCompilerConfig;
 import suite.instructionexecutor.ExpandUtil;
 import suite.instructionexecutor.FunInstructionExecutor;
 import suite.lp.doer.Cloner;
 import suite.lp.doer.ProverConfig;
 import suite.lp.doer.Specializer;
 import suite.lp.kb.RuleSet;
 import suite.lp.search.CompiledProverBuilder;
 import suite.lp.search.InterpretedProverBuilder;
 import suite.lp.search.ProverBuilder.Builder;
 import suite.lp.search.ProverBuilder.Finder;
 import suite.node.Atom;
 import suite.node.Node;
 import suite.util.CacheUtil;
 import suite.util.FunUtil.Fun;
 import suite.util.FunUtil.Sink;
 import suite.util.FunUtil.Source;
 import suite.util.LogUtil;
 import suite.util.Pair;
 import suite.util.To;
 import suite.util.Util;
 
 public class EvaluateUtil {
 
 	private Fun<Pair<Boolean, Boolean>, Node> fccNodeFun = new CacheUtil().proxy(new Fun<Pair<Boolean, Boolean>, Node>() {
 		public Node apply(Pair<Boolean, Boolean> pair) {
 			Atom mode = Atom.create(pair.t0 ? "LAZY" : "EAGER");
 
 			return new Specializer().specialize(Suite.substitute("" //
 					+ "source .in" //
 					+ ", compile-function .0 .in .out" //
 					+ (pair.t1 ? ", pretty.print .out" : "") //
 					+ ", sink .out", mode));
 		}
 	});
 
 	private Fun<Pair<ProverConfig, Node>, Finder> fccFinderFun = new CacheUtil().proxy(new Fun<Pair<ProverConfig, Node>, Finder>() {
 		public Finder apply(Pair<ProverConfig, Node> pair) {
 			Builder builder = Boolean.TRUE ? new InterpretedProverBuilder(pair.t0) : CompiledProverBuilder.level1(pair.t0, false);
 
 			// Using level 1 CompiledProverBuilder would break the test case
 			// FunRbTreeTest. It would by blow up the stack in
 			// InstructionExecutor
 			return builder.build(Suite.funCompilerRuleSet(), pair.t1);
 		}
 	});
 
 	public boolean proveLogic(Node lp) {
 		Builder builder = CompiledProverBuilder.level1(new ProverConfig(), false);
 		return proveLogic(builder, Suite.createRuleSet(), lp);
 	}
 
 	public boolean proveLogic(RuleSet rs, Node lp) {
 		return proveLogic(new InterpretedProverBuilder(), rs, lp);
 	}
 
 	public boolean proveLogic(Builder builder, RuleSet rs, Node lp) {
 		Node goal = Suite.substitute(".0, sink ()", lp);
 		return !evaluateLogic(builder, rs, goal).isEmpty();
 	}
 
 	public List<Node> evaluateLogic(Builder builder, RuleSet rs, Node lp) {
 		return collect(builder.build(rs, lp), Atom.NIL);
 	}
 
 	public Node evaluateFun(FunCompilerConfig fcc) {
 		try (FunInstructionExecutor executor = configureFunExecutor(fcc)) {
 			Node result = executor.execute();
 			return fcc.isLazy() ? ExpandUtil.expandFully(executor.getUnwrapper(), result) : result;
 		}
 	}
 
 	public void evaluateFunToWriter(FunCompilerConfig fcc, Writer writer) throws IOException {
 		try (FunInstructionExecutor executor = configureFunExecutor(fcc)) {
 			executor.executeToWriter(writer);
 		}
 	}
 
 	private FunInstructionExecutor configureFunExecutor(FunCompilerConfig fcc) {
 		Node node = fccNodeFun.apply(Pair.create(fcc.isLazy(), fcc.isDumpCode()));
 		Node code = doFcc(node, fcc);
 
 		if (code != null)
 			return new FunInstructionExecutor(code);
 		else
			throw new RuntimeException("Function compilation failure");
 	}
 
 	public Node evaluateFunType(FunCompilerConfig fcc) {
 		Node node = Suite.parse("" //
 				+ "source .in" //
 				+ ", fc-parse .in .p" //
 				+ ", infer-type-rule .p ()/()/() .tr/() .t" //
 				+ ", resolve-type-rules .tr" //
 				+ ", fc-parse-type .out .t" //
 				+ ", sink .out");
 
 		Node type = doFcc(node, fcc);
 
 		if (type != null)
 			return type;
 		else
			throw new RuntimeException("Type inference failure");
 	}
 
 	private Node doFcc(Node compileNode, FunCompilerConfig fcc) {
 		long start = System.currentTimeMillis();
 
 		try {
 			ProverConfig pc = fcc.getProverConfig();
 			Finder finder = fccFinderFun.apply(Pair.create(pc, compileNode));
 
 			List<Node> nodes = collect(finder, appendLibraries(fcc));
 			return nodes.size() == 1 ? nodes.get(0).finalNode() : null;
 		} finally {
 			LogUtil.info("Code compiled in " + (System.currentTimeMillis() - start) + "ms");
 		}
 	}
 
 	private Node appendLibraries(FunCompilerConfig fcc) {
 		Node node = fcc.getNode();
 
 		for (String library : fcc.getLibraries())
 			if (!Util.isBlank(library))
 				node = Suite.substitute("using .0 >> .1", Atom.create(library), node);
 
 		return node;
 	}
 
 	private List<Node> collect(Finder finder, Node in) {
 		final List<Node> nodes = new ArrayList<>();
 
 		Source<Node> source = To.source(in);
 		Sink<Node> sink = new Sink<Node>() {
 			public void sink(Node node) {
 				nodes.add(new Cloner().clone(node));
 			}
 		};
 
 		finder.find(source, sink);
 		return nodes;
 	}
 
 }

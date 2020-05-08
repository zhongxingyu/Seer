 package cs4120.der34dlc287lg342.xi.tests;
 
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import cs4120.der34dlc287lg342.xi.XiParser;
 import cs4120.der34dlc287lg342.xi.assembly.Assembler;
 import cs4120.der34dlc287lg342.xi.assembly.Assembly;
 import cs4120.der34dlc287lg342.xi.assembly.RegAlloc;
 import cs4120.der34dlc287lg342.xi.ast.AbstractSyntaxTree;
 import cs4120.der34dlc287lg342.xi.cfg.CFG;
 import cs4120.der34dlc287lg342.xi.cfg.InterferenceGraph;
 import cs4120.der34dlc287lg342.xi.cfg.LivenessWorklist;
 import cs4120.der34dlc287lg342.xi.cfg.Rewrite;
 import cs4120.der34dlc287lg342.xi.ir.Func;
 import cs4120.der34dlc287lg342.xi.ir.Seq;
 import cs4120.der34dlc287lg342.xi.ir.context.IRContextStack;
 import cs4120.der34dlc287lg342.xi.ir.context.InvalidIRContextException;
 import cs4120.der34dlc287lg342.xi.ir.translate.ConstantFolding;
 import cs4120.der34dlc287lg342.xi.ir.translate.IRTranslation;
 import cs4120.der34dlc287lg342.xi.ir.translate.LowerCjump;
 import cs4120.der34dlc287lg342.xi.tiles.SeqTile;
 import cs4120.der34dlc287lg342.xi.typechecker.InvalidXiTypeException;
 import cs4120.der34dlc287lg342.xi.typechecker.XiTypechecker;
 import edu.cornell.cs.cs4120.xi.AbstractSyntaxNode;
 import edu.cornell.cs.cs4120.xi.parser.Parser;
 import junit.framework.TestCase;
 
 public class TestCFG extends TestCase {
 	
 	public Seq gen(String code){
 		Reader reader = new StringReader(code);
 		Parser p = new XiParser(reader);
 		AbstractSyntaxNode ast = p.parse();
 		XiTypechecker tc;
 		try {
 			tc = new XiTypechecker(ast, code);
 		} catch (InvalidXiTypeException e1) {
 			fail(e1.getMessage());
 			return null;
 		}
 		tc.typecheck();
 		((AbstractSyntaxTree)tc.ast).foldConstants();
 		try {
 			IRTranslation tr = ((AbstractSyntaxTree)tc.ast).to_ir(new IRContextStack());
 			return LowerCjump.translate(tr.stmt().lower());
 		} catch (InvalidIRContextException e) {
 			fail(e.getMessage());
 		}
 		return null;
 	}
 	
 	public void testCFG(){
 		Seq stmt = gen("main(a:int){b:int if (a > 3) {a = (1+a)*3 + b} else {while (a > 3) f(a,b)}} f(a:int, b:int){}");
 		stmt = ConstantFolding.foldConstants(stmt);
 		Func func = (Func) stmt.children.get(0);
 		System.out.println(func.prettyPrint());
 		ArrayList<Assembly> instrs = func.munch().att();
 //		System.out.println(func.prettyPrint());
 		CFG cfg = CFG.cfg(instrs);
 		System.out.println(cfg);
 		System.out.println(cfg.dot_edge(new HashSet<CFG>()));
 	}
 	
 //	public void testWorklist() {
 //		Seq stmt = gen("main(a:int){b:int c:int d:int b=a+a c=a+a d=c+b if(d<b) {d=a+1} else {d=c+1}}");
 //		stmt = ConstantFolding.foldConstants(stmt);
 //		Func func = (Func) stmt.children.get(0);
 //		
 //		CFG cfg = func.cfg();
 //
 ////		System.out.println(cfg);
 //		//System.out.println(cfg.dot_edge(new HashSet<CFG>()));
 //		
 //		LivenessWorklist wl = new LivenessWorklist(cfg);
 //		wl.analyze();
 //		InterferenceGraph g = new InterferenceGraph(cfg);
 //		System.out.println(g.adjacent);
 //		//System.out.println(cfg.dot_edge(new HashSet<CFG>()));
 ////		System.out.println(cfg);
 ////		for (CFG node : wl.worklist) {
 ////			System.out.println(node.simpleName(node));
 ////		}
 //	}
 	
 	public void testWorklist(){
		Seq stmt = gen("use io main(args:int[][]){a:int = 3 print((97,98,99,100,a))}");
 		stmt = ConstantFolding.foldConstants(stmt);
		System.out.println(stmt.prettyPrint());
 		Assembler assembler = new Assembler((SeqTile) stmt.munch());
 		System.out.println(assembler.att());
 //		Func func = (Func) stmt.children.get(0);
 //		//System.out.println(func.prettyPrint());
 //		ArrayList<Assembly> instrs = func.munch().att();
 //		CFG cfg = CFG.cfg(instrs);
 //		//System.out.println(cfg);
 //		//System.out.println(cfg.asm());
 //		
 //		LivenessWorklist wl = new LivenessWorklist(cfg);
 //		wl.analyze();
 //		InterferenceGraph g = new InterferenceGraph(cfg);
 //		
 //		int n = 0;
 //		while (!g.spills.isEmpty()){
 //			
 //			Rewrite rewrite = new Rewrite(instrs, g.spills, n);
 //			n += g.spills.size();
 //			instrs = rewrite.rewrite();
 //			cfg = CFG.cfg(instrs);
 //			wl = new LivenessWorklist(cfg);
 //			wl.analyze();
 //			//System.out.println(cfg.dot_edge(new HashSet<CFG>()));
 //			g = new InterferenceGraph(cfg);
 //		}
 //
 //		System.out.println(RegAlloc.allocate(instrs, g.coloring));
 		//System.out.println(cfg.dot_edge(new HashSet<CFG>(), g.coloring));
 		//System.out.println(g.dot_edge());
 		
 		//System.out.println(g.adjacent);
 		//System.out.println(cfg.dot_edge(new HashSet<CFG>()));
 //		System.out.println(cfg);
 //		for (CFG node : wl.worklist) {
 //			System.out.println(node.simpleName(node));
 //		}
 	}
 }

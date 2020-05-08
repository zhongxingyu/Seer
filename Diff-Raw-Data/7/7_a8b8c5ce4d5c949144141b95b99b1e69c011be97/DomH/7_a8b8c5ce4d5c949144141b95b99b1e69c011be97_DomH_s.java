 /*
  * Copyright (c) 2008-2009, Intel Corporation.
  * Copyright (c) 2006-2007, The Trustees of Stanford University.
  * All rights reserved.
  */
 package chord.doms;
 
 import java.util.Set;
 
 import joeq.Class.jq_Class;
 import joeq.Class.jq_Method;
 import joeq.Class.jq_Type;
 import joeq.Compiler.Quad.BasicBlock;
 import joeq.Compiler.Quad.ControlFlowGraph;
 import joeq.Compiler.Quad.Operator;
 import joeq.Compiler.Quad.Quad;
 import joeq.Compiler.Quad.Operator.CheckCast;
 import joeq.Compiler.Quad.Operator.New;
 import joeq.Compiler.Quad.Operator.NewArray;
 import joeq.Util.Templates.ListIterator;
 import chord.project.Chord;
 import chord.project.Project;
 import chord.project.Properties;
 import chord.project.analyses.ProgramDom;
 import chord.program.Program;
 import chord.util.IndexSet;
 
 /**
  * Domain of object allocation statements.
  * <p>		
  * The 0th element of this domain (null) is a distinguished		
  * hypothetical object allocation statement that may be used		
  * for various purposes.
  *
  * @author Mayur Naik (mhn@cs.stanford.edu)
  * @author omertripp (omertrip@post.tau.ac.il)
  */
 @Chord(
 	name = "H",
 	consumedNames = { "M", "T" }
 )
 public class DomH extends ProgramDom<Object> {
 	protected DomM domM;
 	protected DomT domT;
 	protected jq_Method ctnrMethod;
 	protected int lastRealHidx;
 	public int getLastRealHidx() {
 		return lastRealHidx;
 	}
 	public void init() {		
 		domM = (DomM) Project.getTrgt("M");
 		domT = (DomT) Project.getTrgt("T");
 		getOrAdd(null);	
 	}
 	public void fill() {
 		Program program = Program.getProgram();
 		IndexSet<jq_Class> classes = program.getClasses();
 		IndexSet<jq_Class> rfClasses = null;
		Set<Quad> rfCasts = null;
 		boolean handleRf = Properties.scopeKind.equals("rta_reflect");
 		if (handleRf) {
  			rfClasses = new IndexSet<jq_Class>();
 			rfCasts = program.getRfCasts();
 		}
 		int numM = domM.size();
 		for (int mIdx = 0; mIdx < numM; mIdx++) {
 			jq_Method m = domM.get(mIdx);
 			if (m.isAbstract())
 				continue;
 			ControlFlowGraph cfg = m.getCFG();
 			ctnrMethod = m;
 			for (ListIterator.BasicBlock it = cfg.reversePostOrderIterator();
 					it.hasNext();) {
 				BasicBlock bb = it.nextBasicBlock();
 				for (ListIterator.Quad it2 = bb.iterator(); it2.hasNext();) {
 					Quad q = it2.nextQuad();
 					Operator op = q.getOperator();
 					if (op instanceof New || op instanceof NewArray) {
 						getOrAdd(q);
 					} else if (handleRf && op instanceof CheckCast &&
							rfCasts.contains(q)) {
 						jq_Class c = (jq_Class) CheckCast.getType(q).getType();
 						for (jq_Class d : classes) {
 							if (!d.isInterface() && !d.isAbstract() &&
 									d.isSubtypeOf(c)) {
 								rfClasses.add(d);
 							}
 						}
 					}
 				}
 			}
 		}
 		lastRealHidx = size() - 1;
 		if (handleRf) {
 			for (jq_Class c : rfClasses)
 				getOrAdd(c);
 		}
 	}
 	public int getOrAdd(Object o) {
 		if (o instanceof Quad) {
 			assert (ctnrMethod != null);
 			Quad q = (Quad) o;
 			Program.getProgram().mapInstToMethod(q, ctnrMethod);
 		}
 		return super.getOrAdd(o);
 	}
 	public String toUniqueString(Object o) {
 		if (o instanceof Quad) {
 			Quad q = (Quad) o;
 			return Program.getProgram().toBytePosStr(q);
 		}
 		if (o instanceof jq_Class) {
 			jq_Class c = (jq_Class) o;
 			return c.getName();
 		}
 		assert (o == null);
 		return "null";
 	}
 	public String toXMLAttrsString(Object o) {
 		if (o instanceof Quad) {
 			Quad q = (Quad) o;
 			Operator op = q.getOperator();
 			String type = (op instanceof New) ? New.getType(q).getType().getName() :
 				(op instanceof NewArray) ? NewArray.getType(q).getType().getName() : "unknown";
 			jq_Method m = Program.getProgram().getMethod(q);
 			String file = Program.getSourceFileName(m.getDeclaringClass());
 			int line = Program.getLineNumber(q, m);
 			int mIdx = domM.indexOf(m);
 			return "file=\"" + file + "\" " + "line=\"" + line + "\" " +
 				"Mid=\"M" + mIdx + "\"" + " type=\"" + type + "\"";
 		}
 		return "";
 	}
 }

 /*
  * Copyright (c) 2008-2010, Intel Corporation.
  * Copyright (c) 2006-2007, The Trustees of Stanford University.
  * All rights reserved.
  */
 package chord.rels;
 
 import joeq.Class.jq_Method;
 import joeq.Compiler.Quad.Inst;
 import joeq.Compiler.Quad.Quad;
 import chord.doms.DomL;
 import chord.doms.DomM;
 import chord.program.Program;
 import chord.project.Chord;
 import chord.project.analyses.ProgramRel;
 
 /**
 * Relation containing each tuple (l,m) such that method m
 * is synchronized on lock l.
  *
  * @author Mayur Naik (mhn@cs.stanford.edu)
  */
 @Chord(
 	name = "syncLM",
 	sign = "L0,M0:L0_M0"
 )
 public class RelSyncLM extends ProgramRel {
 	public void fill() {
 		DomL domL = (DomL) doms[0];
 		DomM domM = (DomM) doms[1];
 		int numL = domL.size();
 		Program program = Program.getProgram();
 		for (int lIdx = 0; lIdx < numL; lIdx++) {
 			Inst i = domL.get(lIdx);
 			if (!(i instanceof Quad)) {
 				jq_Method m = program.getMethod(i);
 				int mIdx = domM.indexOf(m);
 				assert (mIdx >= 0);
 				add(lIdx, mIdx);
 			}
 		}
 	}
 }

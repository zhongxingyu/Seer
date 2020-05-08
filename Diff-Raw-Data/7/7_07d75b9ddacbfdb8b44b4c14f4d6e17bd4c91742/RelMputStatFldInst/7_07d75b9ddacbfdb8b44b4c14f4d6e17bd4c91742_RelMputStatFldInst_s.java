 package chord.rels;
 
 import chord.project.Chord;
 import chord.project.ProgramRel;
 
 import chord.program.Var;
 import chord.program.CFG;
 import chord.program.Method;
 import chord.program.insts.Inst;
 import chord.program.insts.StatFldRefInst;
 import chord.doms.DomM;
 
 /**
  * Relation containing each tuple (m,f,v) such that method m contains
  * a statement of the form <tt>f = v</tt>.
  */
 @Chord(
 	name = "MputStatFldInst",
 	sign = "M0,F0,V0:F0_M0_V0"
 )
 public class RelMputStatFldInst extends ProgramRel {
     public void fill() {
         DomM domM = (DomM) doms[0];
        //DomV domV = (DomV) doms[1];
        //DomF domF = (DomF) doms[2];
         for (Method meth : domM) {
             CFG cfg = meth.getCFG();
             if (cfg == null)
                 continue;
             for (Inst inst : cfg.getNodes()) {
                 if (inst instanceof StatFldRefInst) {
                     StatFldRefInst asgn = (StatFldRefInst) inst;
                     if (asgn.isWr()) {
                         Var v = asgn.getVar();
                         if (v == null) continue;
                        add(meth, v, asgn.getField());
                     }
                 }
             }
         }
     }
 }

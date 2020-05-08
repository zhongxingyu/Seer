 package net.cscott.sdr.calls.grm;
 
 import java.util.List;
 
 import net.cscott.jutil.MultiMap;
 import net.cscott.sdr.calls.grm.Grm.Mult;
 import net.cscott.sdr.calls.grm.Grm.Nonterminal;
 import net.cscott.sdr.calls.grm.Grm.Terminal;
 
 public class EmitANTLR extends AbstractEmit {
     private NumberParams np=null;
     private EmitANTLR() { }
     public static String emit(String parserName,
                               List<RuleAndAction> l) {
         EmitANTLR ea = new EmitANTLR();
         // collect all the rules with the same LHS
         MultiMap<String,RuleAndAction> mm = collectLHS(l);
         // now emit the rules.
         String NL = System.getProperty("line.separator");
         StringBuilder sb = new StringBuilder();
         for (String lhs : sorted(mm.keySet())) {
            sb.append(lhs+" returns [Apply r=null]"+NL);
             boolean first = true;
             for (RuleAndAction ra : mm.getValues(lhs)) {
                 if (first) { sb.append("\t: "); first = false; }
                 else { sb.append("\t| "); }
                 ea.np = new NumberParams(ra.rule.rhs);
                 sb.append(ra.rule.rhs.accept(ea));
                 sb.append(" { "+ra.action+" }"+NL);
             }
             sb.append("\t;"+NL);
         }
         // substitute the rules & the classname into the skeleton
         String result = ea.subst("antlr.skel", sb.toString(), parserName);
         // done.
         return result;
     }
     
     @Override
     public String visit(Terminal t) {
         // quote literals.
         return "\""+t.literal+"\"";
     }
     @Override
     public String visit(Mult mult) {
         // always parenthesize multiplicity markers.
         return "("+mult.operand.accept(this)+")"+mult.type;
     }
     @Override
     public String visit(Nonterminal nt) {
         StringBuilder sb = new StringBuilder();
         if (nt.param>=0) {
             sb.append((char)('a'+np.paramToOrder.get(nt.param)));
             sb.append('=');
         }
         sb.append(nt.ruleName);
         return sb.toString();
     }
 }

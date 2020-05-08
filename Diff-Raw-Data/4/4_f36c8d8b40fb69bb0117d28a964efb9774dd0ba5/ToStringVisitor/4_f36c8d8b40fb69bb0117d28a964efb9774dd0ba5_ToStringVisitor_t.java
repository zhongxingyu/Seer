 package net.cscott.sdr.calls.grm;
 
 import java.util.Iterator;
 
 import net.cscott.sdr.calls.grm.Grm.Alt;
 import net.cscott.sdr.calls.grm.Grm.Concat;
 import net.cscott.sdr.calls.grm.Grm.Mult;
 import net.cscott.sdr.calls.grm.Grm.Nonterminal;
 import net.cscott.sdr.calls.grm.Grm.Terminal;
 
 class ToStringVisitor extends GrmVisitor<String> {
     protected String paren(Grm g1, Grm g2) {
         if (g1.precedence() >= g2.precedence())
            return "("+g2.accept(this)+")";
        return g2.accept(this);
     }
 
     @Override
     public String visit(Alt alt) {
         StringBuilder sb = new StringBuilder();
         for (Iterator<Grm> it = alt.alternates.iterator();
             it.hasNext(); ) {
             sb.append(paren(alt, it.next()));
             if (it.hasNext()) sb.append('|');
         }
         return sb.toString();
     }
 
     @Override
     public String visit(Concat concat) {
         StringBuilder sb = new StringBuilder();
         for (Iterator<Grm> it = concat.sequence.iterator();
              it.hasNext(); ) {
             sb.append(paren(concat, it.next()));
             if (it.hasNext()) sb.append(' ');
         }
         return sb.toString();
     }
 
     @Override
     public String visit(Mult mult) {
         return paren(mult, mult.operand)+mult.type;
     }
 
     @Override
     public String visit(Nonterminal nt) {
         StringBuilder sb = new StringBuilder("<");
         if (nt.param>=0) { sb.append(nt.param); sb.append('='); }
         sb.append(nt.ruleName);
         sb.append('>');
         return sb.toString();
     }
 
     @Override
     public String visit(Terminal t) {
         return t.literal;
     }
 }

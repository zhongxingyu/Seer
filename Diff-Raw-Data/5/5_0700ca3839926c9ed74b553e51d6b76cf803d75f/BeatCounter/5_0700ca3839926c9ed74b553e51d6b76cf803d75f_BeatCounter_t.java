 // Remove 'In's from a call tree by pushing them down and adjusting Prim timing
 // Step 1: Compute the 'inherent' timing of the subtrees; this will be used
 //         to proportionally allocate the number of beats we will be given.
 package net.cscott.sdr.calls.transform;
 
 import net.cscott.sdr.calls.ast.*;
 import net.cscott.sdr.util.*;
 
 import java.util.*;
 
/** Propagate 'inherent' time bottom-up: where prim and part = 1, and IN resets
  * to its spec, whatever that is. */
 class BeatCounter extends ValueVisitor<Fraction,Void> {
     public BeatCounter(Comp c) {
         c.accept(this,null);
     }
     private final Map<AstNode,Fraction> inherent = new HashMap<AstNode,Fraction>();
     // shorthand for registering the timing of an AST node
     private Fraction r(AstNode ast, Fraction f) { inherent.put(ast, f); return f; }
     // Math.max for Fractions.
     private Fraction max(Fraction a, Fraction b) {
         return (a.compareTo(b) < 0) ? b : a;
     }
     public Fraction getBeats(AstNode ast) { return inherent.get(ast); }
     @Override
     public Fraction visit(Apply apply, Void v) {
         assert false : "Calls shouldn't be present in simplified tree";
         return null;
     }
     @Override
     public Fraction visit(Condition c, Void v) {
         assert false : "Shouldn't traverse conditions";
         return null;
     }
     @Override
     public Fraction visit(If iff, Void v) {
         return r(iff, iff.child.accept(this,v));
     }
     @Override
     public Fraction visit(In in, Void v) {
         in.child.accept(this,v); // note that we ignore child's length
         return r(in, in.count); // and use the length of the 'in' instead.
     }
     @Override
     public Fraction visit(Opt opt, Void v) {
         Fraction oneBranch = null;
         for (OptCall oc : opt.children) {
             Fraction f = oc.accept(this,v);
             if (oneBranch==null)
                 oneBranch = f;
             else assert oneBranch.equals(f);
         }
         return oneBranch;
     }
     @Override
     public Fraction visit(OptCall oc, Void v) {
        return r(oc, oc.child.accept(this,v));
     }
     @Override
     public Fraction visit(Par p, Void v) {
         Fraction f = Fraction.ZERO;
         for (ParCall pc : p.children)
             f = max(f, pc.accept(this,v));
         return r(p, f);
     }
     @Override
     public Fraction visit(ParCall pc, Void v) {
         return r(pc, pc.child.accept(this,v));
     }
     @Override
     public Fraction visit(Part p, Void v) {
         // note that we ignore length of pieces
         return r(p, Fraction.ONE);
     }
     @Override
     public Fraction visit(Prim p, Void v) {
         return r(p, Fraction.ONE);
     }
     @Override
     public Fraction visit(Seq s, Void v) {
         Fraction f = Fraction.ZERO;
         for (SeqCall sc : s.children)
             f = f.add(sc.accept(this,v));
         r(s, f);
         return f;
     }
     @Override
     public Fraction visit(Warped w, Void v) {
         return r(w, w.child.accept(this, v));
     }
 }

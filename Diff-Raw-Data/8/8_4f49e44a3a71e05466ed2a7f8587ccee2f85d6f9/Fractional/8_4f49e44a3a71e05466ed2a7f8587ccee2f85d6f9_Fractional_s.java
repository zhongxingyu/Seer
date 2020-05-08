 package net.cscott.sdr.calls.transform;
 
 import static net.cscott.sdr.calls.transform.CallFileLexer.PART;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import net.cscott.jdoctest.JDoctestRunner;
 import net.cscott.sdr.calls.BadCallException;
 import net.cscott.sdr.calls.ast.Apply;
 import net.cscott.sdr.calls.ast.Comp;
 import net.cscott.sdr.calls.ast.In;
 import net.cscott.sdr.calls.ast.Part;
 import net.cscott.sdr.calls.ast.Prim;
 import net.cscott.sdr.calls.ast.Seq;
 import net.cscott.sdr.calls.ast.SeqCall;
 import net.cscott.sdr.util.Fraction;
 
 import org.junit.runner.RunWith;
 
 /**
  * Transformation implementing
  * {@link net.cscott.sdr.calls.lists.BasicList#_FRACTIONAL}.
  * This only handles the case where the fraction is less than one;
  * {@link net.cscott.sdr.calls.lists.BasicList#_FRACTIONAL} handles
  * the whole-number portions of the fraction.
  * @author C. Scott Ananian
  * @doc.test
  *  Use Fractional class to evaluate TWICE QUARTER RIGHT:
  *  js> importPackage(net.cscott.sdr.calls.ast)
  *  js> importPackage(net.cscott.sdr.util)
  *  js> callname="dosado"
  *  dosado
  *  js> call = net.cscott.sdr.calls.CallDB.INSTANCE.lookup(callname)
  *  dosado[basic]
  *  js> comp = call.apply(Apply.makeApply(callname))
  *  (In 6 (Opt (From [FACING DANCERS] (Seq (Prim -1, 1, none, 1, SASHAY_START) (Prim 1, 1, none, 1, SASHAY_FINISH) (Prim 1, -1, none, 1, SASHAY_START) (Prim -1, -1, none, 1, SASHAY_FINISH)))))
  *  js> comp.accept(new Fractional(), Fraction.ONE_QUARTER)
  *  (In 1 1/2 (Opt (From [FACING DANCERS] (Seq (Prim -1, 1, none, 1, SASHAY_START)))))
  *  js> try {
  *    >   comp.accept(new Fractional(), Fraction.ONE_THIRD)
  *    > } catch (e) {
  *    >   print(e.javaException)
  *    > }
 *  net.cscott.sdr.calls.BadCallException: No formation options left: Primitives cannot be subdivided: 1/3
  */
 @RunWith(value=JDoctestRunner.class)
 public class Fractional extends TransformVisitor<Fraction> {
     @Override
     public In visit(In in, Fraction f) {
         return in.build(in.count.multiply(f), in.child.accept(this, f));
     }
     @Override
     public Prim visit(Prim p, Fraction f) {
         if (Fraction.ONE.equals(f))
             return p;
        throw new BadCallException("Primitives cannot be subdivided: "+f);
     }
     @Override
     public Part visit(Part p, Fraction f) {
         if (Fraction.ONE.equals(f))
             return p;
         if (p.isDivisible)
             return (Part) super.visit(p, f);
        throw new BadCallException("Can't divide indivisible part: "+f);
     }
     @Override
     public SeqCall visit(Apply apply, Fraction f) {
         if (f.compareTo(Fraction.ONE)==0)
             return apply;
         // optimization: 1/2(1/2(x)) = 1/4(x)
         if (apply.callName.equals("_fractional")) {
             // special case; just multiply fractions
             return Apply.makeApply("_fractional",
                     apply.getNumberArg(0).multiply(f),
                     apply.getArg(1));
         }
         // optimization: some concepts are safe to hoist fractionalization thru
         if (safeConcepts.contains(apply.callName)) {
             assert apply.args.size()==1;
             return Apply.makeApply(apply.callName,
                     Apply.makeApply("_fractional", f, apply.getArg(0)));
         }
         // okay, we have to expand the call in order to fractionalize the
         // contents.
         if (apply.evaluator()!=null)
             throw new BadCallException("Can't fractionalize complex concept");
         // okay, this concept can be simply expanded...
         Part result = new Part(true,apply.expand().accept(this, f));
         return result;
     }
     /** A list of concepts which it is safe to hoist fractionalization through.
      That is, "1/2(as couples(swing thru))" == "as couples(1/2(swing thru))". */
     private static Set<String> safeConcepts = new HashSet<String>(Arrays.asList(
             "as couples","tandem"
             // xxx more fractionalization-safe calls?
             ));
     @Override
     public Comp visit(Seq s, Fraction f) {
         assert f.compareTo(Fraction.ONE) <= 0;
         f = f.multiply(Fraction.valueOf(s.children.size()));
         List<SeqCall> l = new ArrayList<SeqCall>(s.children.size());
         for (SeqCall child : s.children) {
             if (f.compareTo(Fraction.ONE) >= 0) {
                 l.add(child);
                 f = f.subtract(Fraction.ONE);
             } else if (f.compareTo(Fraction.ZERO) != 0) {
                 l.add(child.accept(this, f));
                 f = Fraction.ZERO;
             }
         }
         assert f.compareTo(Fraction.ZERO) == 0;
         if (l.isEmpty()) throw new BadCallException("Nothing left in Seq");
         // OPTIMIZATION: SEQ(PART(c)) = c
         if (l.size()==1 && l.get(0).type==PART) {
             Part p = (Part) l.get(0);
             if (p.isDivisible) return p.child;
         }
         return s.build(l);
     }
 }

 package net.cscott.sdr.calls.transform;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import net.cscott.sdr.calls.Call;
 import net.cscott.sdr.calls.Program;
 import net.cscott.sdr.calls.ExactRotation;
 import net.cscott.sdr.calls.Selector;
 import net.cscott.sdr.calls.Warp;
 import net.cscott.sdr.calls.ast.*;
 import net.cscott.sdr.calls.ast.Prim.Direction;
 import net.cscott.sdr.calls.grm.Rule;
 import net.cscott.sdr.util.Fraction;
 /** 
  * The {@link BuilderHelper} class helps with the generation of paramterized
  * calls.  It supports an abstraction which lets us treat ast trees as
  * "ast tree generation functions", while optimizing the case where the
  * function generates a constant.
  * @author C. Scott Ananian
 * @version $Id: BuilderHelper.java,v 1.7 2007-03-07 19:25:05 cananian Exp $
  */
 abstract class BuilderHelper {
     /** An enumeration of directions, as specified in the call file. */
     static enum BDirection {
         ASIS, IN, OUT;
        /** Translate {@link BDirection}s to {@link Prim.Direction}s. */
         Prim.Direction primDir() {
             switch (this) {
             case OUT:
             case IN: return Prim.Direction.IN;
             default: assert false;
             case ASIS: return Prim.Direction.ASIS;
             }
         }
         /** Flip the sign of the given fraction if {@link BDirection} is OUT. */
         Fraction setSign(Fraction f) {
             return (this==OUT) ? f.negate() : f;
         }
         ExactRotation setSign(ExactRotation r) {
             return (this==OUT) ? r.negate() : r;
         }
     }
     /** An enumeration of primitive attributes, as specified in the call file.
      */
     static enum BPrimAttrib { FORCE_ARC, PASS_LEFT; }
     /**
      * 'B' is pronounced as 'Builder'.  So a B<Prim> builds Prim objects.
      */
     static abstract class B<T> {
         public abstract T build(List<Apply> args);
         /** 
          * Returns true if the build operation will succeed given a zero-length
          * argument list.
          */
         public boolean isConstant() { return false; } // always safe
     }
     static <T> B<T> mkConstant(final T t) {
         return new B<T>() {
             @Override
             public T build(List<Apply> args) { return t; }
             @Override
             public boolean isConstant() { return true; }
         };
     }
     static <T> boolean isConstant(List<? extends B<? extends T>> l) {
         for (B<? extends T> b : l)
             if (!b.isConstant()) return false;
         return true;
     }
     static <T> List<T> reduce(List<? extends B<? extends T>> l, List<Apply> args) {
         List<T> ll = new ArrayList<T>(l.size());
         for (B<? extends T> b : l)
             ll.add(b.build(args));
         return ll;
     }
     static <T> B<T> optimize(B<T> b, boolean isConstant) {
         return (isConstant) ? mkConstant(b.build(null)) : b;
     }
     static B<Apply> mkApply(final String callName, final List<B<Apply>> args) {
         return optimize(new B<Apply>() {
             public Apply build(List<Apply> fargs) {
                 return new Apply(callName, reduce(args, fargs));
             }
         }, isConstant(args));
     }
     static B<Condition> mkCondition(final String predicate, final List<B<Condition>> args) {
         return optimize(new B<Condition>() {
             public Condition build(List<Apply> fargs) {
                 return new Condition(predicate, reduce(args, fargs));
             }
         }, isConstant(args));
     }
     static B<If> mkIf(final B<Condition> cond, final B<? extends Comp> child) {
         return optimize(new B<If>() {
             public If build(List<Apply> fargs) {
                 return new If(cond.build(fargs), child.build(fargs));
             }
         }, cond.isConstant() && child.isConstant());
     }
     static B<In> mkIn(final Fraction count, final B<? extends Comp> child) {
         return optimize(new B<In>() {
             public In build(List<Apply> fargs) {
                 return new In(count, child.build(fargs));
             }
         }, child.isConstant());
     }
     static B<Opt> mkOpt(final List<B<OptCall>> children) {
         return optimize(new B<Opt>() {
             public Opt build(List<Apply> fargs) {
                 return new Opt(reduce(children, fargs));
             }
         }, isConstant(children));
     }
     static B<OptCall> mkOptCall(final List<Selector> selectors, final B<? extends Comp> child) {
         return optimize(new B<OptCall>() {
             public OptCall build(List<Apply> fargs) {
                 return new OptCall(selectors, child.build(fargs));
             }
         }, child.isConstant());
     }
     static B<Par> mkPar(final List<B<ParCall>> children) {
         return optimize(new B<Par>() {
             public Par build(List<Apply> fargs) {
                 return new Par(reduce(children, fargs));
             }
         }, isConstant(children));
     }
     static B<ParCall> mkParCall(final List<B<String>> tags, final B<? extends Comp> child) {
         return optimize(new B<ParCall>() {
             public ParCall build(List<Apply> fargs) {
                 return new ParCall(ParCall.parseTags(reduce(tags,fargs)), child.build(fargs));
             }
         }, child.isConstant() && isConstant(tags));
     }
     static B<Part> mkPart(final boolean isDivisible, final B<? extends Comp> child) {
         return optimize(new B<Part>() {
             public Part build(List<Apply> fargs) {
                 return new Part(isDivisible, child.build(fargs));
             }
         }, child.isConstant());
     }
     static B<Prim> mkPrim(final BDirection dirX, final Fraction x,
             final BDirection dirY, final Fraction y,
             final BDirection dirRot, final ExactRotation rot,
             final Set<BPrimAttrib> attribs) {
         return mkConstant(new Prim(dirX.primDir(), dirX.setSign(x),
                                    dirY.primDir(), dirY.setSign(y),
                                    dirRot.primDir(), dirRot.setSign(rot),
                                    Fraction.ONE,
                                    !attribs.contains(BPrimAttrib.PASS_LEFT),
                                    attribs.contains(BPrimAttrib.FORCE_ARC)));
     }
     static B<Seq> mkSeq(final List<B<? extends SeqCall>> children) {
         return optimize(new B<Seq>() {
             public Seq build(List<Apply> fargs) {
                 return new Seq(reduce(children, fargs));
             }
         }, isConstant(children));
     }
     static B<Warped> mkWarped(final Warp warp, final B<? extends Comp> child) {
         return optimize(new B<Warped>() {
             public Warped build(List<Apply> fargs) {
                 return new Warped(warp, child.build(fargs));
             }
         }, child.isConstant());
     }
     //////////////
     static Call makeCall(final String name, final Program program,
             final B<? extends Comp> b, final int minNumberOfArguments,
             final Rule rule) {
         if (b.isConstant())
             return Call.makeSimpleCall(name,program,b.build(null), rule);
         return new Call() {
             @Override
             public String getName() { return name; }
             @Override
             public Program getProgram() { return program; }
             @Override
             public Comp apply(Apply ast) { 
                 assert ast.callName.equals(name);
                 assert ast.args.size() == minNumberOfArguments; // in this case, must be exact match.
                 return b.build(ast.args);
             }
             @Override
             public int getMinNumberOfArguments() {
                 return minNumberOfArguments;
             }
             @Override
             public Rule getRule() { return rule; }
         };
     }
 }

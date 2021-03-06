 package r.nodes.truffle;
 
 
 import java.util.*;
 
 import com.oracle.truffle.api.frame.*;
 import com.oracle.truffle.api.nodes.*;
 
 import r.*;
 import r.data.*;
 import r.data.RAny.Attributes;
 import r.data.RArray.Names;
 import r.data.RComplex.RComplexUtils;
 import r.data.internal.*;
 import r.errors.*;
 import r.nodes.*;
 
 // FIXME: the design may not be good for complex numbers (too much common computation for real, imaginary parts)
 // FIXME: the complex arithmetic differs for scalars/non-scalars (NA semantics - which part is NA), though this should not be visible to the end-user
 public class Arithmetic extends BaseR {
 
     @Child RNode left;
     @Child RNode right;
     final ValueArithmetic arit;
 
     private static final boolean EAGER = false;
     private static final boolean LIMIT_VIEW_DEPTH = true;
     private static final int MAX_VIEW_DEPTH = 5;
 
     private static final boolean DEBUG_AR = false;
     private static final boolean PROFILE_DOUBLE_VIEWS = false;  // only works with EAGER == false and best with LIMIT_VIEW_DEPTH == false
 
     private static final boolean EAGER_COMPLEX = true;
 
     public Arithmetic(ASTNode ast, RNode left, RNode right, ValueArithmetic arit) {
         super(ast);
         this.left = adoptChild(left);
         this.right = adoptChild(right);
         this.arit = arit;
     }
 
     public static boolean returnsDouble(ValueArithmetic arit) {
         return (arit == POW || arit == DIV);
     }
 
     @Override
     public Object execute(Frame frame) {
         RAny lexpr = (RAny) left.execute(frame);
         RAny rexpr = (RAny) right.execute(frame);
         return execute(lexpr, rexpr);
     }
 
     public Object execute(RAny lexpr, RAny rexpr) {
         try {
             throw new UnexpectedResultException(null);
         } catch (UnexpectedResultException e) {
 
             if (left instanceof Constant || right instanceof Constant) {
                 SpecializedConst sc = SpecializedConst.createSpecialized(lexpr, rexpr, ast, left, right, arit);
                 replace(sc, "install Specialized from Uninitialized");
                 if (DEBUG_AR) Utils.debug("Installed " + sc.dbg + " for expressions " + lexpr + "(" + lexpr.pretty() + ") and " + rexpr + "(" + rexpr.pretty() + ")");
                 return sc.execute(lexpr, rexpr);
             } else {
                 Specialized sn = Specialized.createSpecialized(lexpr, rexpr, ast, left, right, arit);
                 replace(sn, "install Specialized from Uninitialized");
                 if (DEBUG_AR) Utils.debug("Installed " + sn.dbg);
                 return sn.execute(lexpr, rexpr);
             }
         }
     }
 
     public enum FailedSpecialization {
         FIXED_TYPE,
         MULTI_TYPE
     }
 
     static class Specialized extends Arithmetic {
         final String dbg;
         final Calculator calc;
 
         public Specialized(ASTNode ast, RNode left, RNode right, ValueArithmetic arit, Calculator calc, String dbg) {
             super(ast, left, right, arit);
             this.dbg = dbg;
             this.calc = calc;
         }
 
         public abstract static class Calculator {
             public abstract Object calc(RAny lexpr, RAny rexpr) throws UnexpectedResultException;
         }
 
         public static Specialized createSpecialized(RAny leftTemplate, RAny rightTemplate, final ASTNode ast, RNode left, RNode right, final ValueArithmetic arit) {
             if (leftTemplate instanceof ScalarComplexImpl && rightTemplate instanceof ScalarComplexImpl) {
                 Calculator c = new Calculator() {
                     @Override
                     public Object calc(RAny lexpr, RAny rexpr) throws UnexpectedResultException {
                         if (!(lexpr instanceof ScalarComplexImpl && rexpr instanceof ScalarComplexImpl)) {
                             throw new UnexpectedResultException(FailedSpecialization.FIXED_TYPE);
                         }
                         ScalarComplexImpl lcomp = (ScalarComplexImpl) lexpr;
                         double lreal = lcomp.getReal();
                         double limag = lcomp.getImag();
                         ScalarComplexImpl rcomp = (ScalarComplexImpl) rexpr;
                         double rreal = rcomp.getReal();
                         double rimag = rcomp.getImag();
                         if (!RComplex.RComplexUtils.eitherIsNA(lreal, limag) && !RComplex.RComplexUtils.eitherIsNA(rreal, rimag)) {
                             return RComplex.RComplexFactory.getScalar(arit.opReal(ast, lreal, limag, rreal, rimag), arit.opImag(ast, lreal, limag, rreal, rimag));
                         } else {
                             return RComplex.BOXED_NA;
                         }
                     }
                 };
                 return new Specialized(ast, left, right, arit, c, "<ScalarComplex, ScalarComplex>");
             }
             if (leftTemplate instanceof ScalarComplexImpl && rightTemplate instanceof ScalarDoubleImpl) {
                 Calculator c = new Calculator() {
                     @Override
                     public Object calc(RAny lexpr, RAny rexpr) throws UnexpectedResultException {
                         if (!(lexpr instanceof ScalarComplexImpl && rexpr instanceof ScalarDoubleImpl)) {
                             throw new UnexpectedResultException(FailedSpecialization.FIXED_TYPE);
                         }
                         ScalarComplexImpl lcomp = (ScalarComplexImpl) lexpr;
                         double lreal = lcomp.getReal();
                         double limag = lcomp.getImag();
                         double rreal = ((ScalarDoubleImpl) rexpr).getDouble();
                         if (!RComplex.RComplexUtils.eitherIsNA(lreal, limag) && !RDouble.RDoubleUtils.isNA(rreal)) {
                             return RComplex.RComplexFactory.getScalar(arit.opReal(ast, lreal, limag, rreal, 0), arit.opImag(ast, lreal, limag, rreal, 0));
                         } else {
                             return RComplex.BOXED_NA;
                         }
                     }
                 };
                 return new Specialized(ast, left, right, arit, c, "<ScalarComplex, ScalarDouble>");
             }
             if (leftTemplate instanceof ScalarDoubleImpl && rightTemplate instanceof ScalarComplexImpl) {
                 Calculator c = new Calculator() {
                     @Override
                     public Object calc(RAny lexpr, RAny rexpr) throws UnexpectedResultException {
                         if (!(lexpr instanceof ScalarDoubleImpl && rexpr instanceof ScalarComplexImpl)) {
                             throw new UnexpectedResultException(FailedSpecialization.FIXED_TYPE);
                         }
                         double lreal = ((ScalarDoubleImpl) lexpr).getDouble();
                         ScalarComplexImpl rcomp = (ScalarComplexImpl) rexpr;
                         double rreal = rcomp.getReal();
                         double rimag = rcomp.getImag();
                         if (!RDouble.RDoubleUtils.isNA(lreal) && !RComplex.RComplexUtils.eitherIsNA(rreal, rimag)) {
                             return RComplex.RComplexFactory.getScalar(arit.opReal(ast, lreal, 0, rreal, rimag), arit.opImag(ast, lreal, 0, rreal, rimag));
                         } else {
                             return RComplex.BOXED_NA;
                         }
                     }
                 };
                 return new Specialized(ast, left, right, arit, c, "<ScalarDouble, ScalarComplex>");
             }
             if (leftTemplate instanceof ScalarDoubleImpl && rightTemplate instanceof ScalarDoubleImpl) {
                 Calculator c = new Calculator() {
                     @Override
                     public Object calc(RAny lexpr, RAny rexpr) throws UnexpectedResultException {
                         if (!(lexpr instanceof ScalarDoubleImpl && rexpr instanceof ScalarDoubleImpl)) {
                             throw new UnexpectedResultException(FailedSpecialization.FIXED_TYPE);
                         }
                         double ldbl = ((ScalarDoubleImpl) lexpr).getDouble();
                         double rdbl = ((ScalarDoubleImpl) rexpr).getDouble();
                         if (RDouble.RDoubleUtils.isNA(ldbl) || RDouble.RDoubleUtils.isNA(rdbl)) {
                             return RDouble.BOXED_NA;
                         }
                         return RDouble.RDoubleFactory.getScalar(arit.op(ast, ldbl, rdbl));
                     }
                 };
                 return new Specialized(ast, left, right, arit, c, "<ScalarDouble, ScalarDouble>");
             }
             if (leftTemplate instanceof ScalarDoubleImpl && rightTemplate instanceof ScalarIntImpl) {
                 Calculator c = new Calculator() {
                     @Override
                     public Object calc(RAny lexpr, RAny rexpr) throws UnexpectedResultException {
                         if (!(lexpr instanceof ScalarDoubleImpl && rexpr instanceof ScalarIntImpl)) {
                             throw new UnexpectedResultException(FailedSpecialization.FIXED_TYPE);
                         }
                         double ldbl = ((ScalarDoubleImpl) lexpr).getDouble();
                         int rint = ((ScalarIntImpl) rexpr).getInt();
                         if (RDouble.RDoubleUtils.isNA(ldbl) || rint == RInt.NA) {
                             return RDouble.BOXED_NA;
                         }
                         return RDouble.RDoubleFactory.getScalar(arit.op(ast, ldbl, rint));
                     }
                 };
                 return new Specialized(ast, left, right, arit, c, "<ScalarDouble, ScalarInt>");
             }
             if (leftTemplate instanceof ScalarIntImpl && rightTemplate instanceof ScalarDoubleImpl) {
                 Calculator c = new Calculator() {
                     @Override
                     public Object calc(RAny lexpr, RAny rexpr) throws UnexpectedResultException {
                         if (!(lexpr instanceof ScalarIntImpl && rexpr instanceof ScalarDoubleImpl)) {
                             throw new UnexpectedResultException(FailedSpecialization.FIXED_TYPE);
                         }
                         int lint = ((ScalarIntImpl) lexpr).getInt();
                         double rdbl = ((ScalarDoubleImpl) rexpr).getDouble();
                         if (lint == RInt.NA || RDouble.RDoubleUtils.isNA(rdbl)) {
                             return RDouble.BOXED_NA;
                         }
                         return RDouble.RDoubleFactory.getScalar(arit.op(ast, lint, rdbl));
                     }
                 };
                 return new Specialized(ast, left, right, arit, c, "<ScalarInt, ScalarDouble>");
             }
             if (leftTemplate instanceof ScalarIntImpl && rightTemplate instanceof ScalarIntImpl) {
                 if (returnsDouble(arit)) {
                     Calculator c = new Calculator() {
                         @Override
                         public Object calc(RAny lexpr, RAny rexpr) throws UnexpectedResultException {
                             if (!(lexpr instanceof ScalarIntImpl && rexpr instanceof ScalarIntImpl)) {
                                 throw new UnexpectedResultException(FailedSpecialization.FIXED_TYPE);
                             }
                             int lint = ((ScalarIntImpl) lexpr).getInt();
                             int rint = ((ScalarIntImpl) rexpr).getInt();
                             if (lint == RInt.NA || rint == RInt.NA) {
                                 return RDouble.BOXED_NA;
                             }
                             return RDouble.RDoubleFactory.getScalar(arit.op(ast, (double) lint, (double) rint));
                         }
                     };
                     return new Specialized(ast, left, right, arit, c, "<ScalarInt, ScalarInt>");
                 } else {
                     Calculator c = new Calculator() {
                         @Override
                         public Object calc(RAny lexpr, RAny rexpr) throws UnexpectedResultException {
                             if (!(lexpr instanceof ScalarIntImpl && rexpr instanceof ScalarIntImpl)) {
                                 throw new UnexpectedResultException(FailedSpecialization.FIXED_TYPE);
                             }
                             int lint = ((ScalarIntImpl) lexpr).getInt();
                             int rint = ((ScalarIntImpl) rexpr).getInt();
                             if (lint == RInt.NA || rint == RInt.NA) {
                                 return RInt.BOXED_NA;
                             }
                             return RInt.RIntFactory.getScalar(arit.op(ast, lint, rint));
                         }
                     };
                     return new Specialized(ast, left, right, arit, c, "<ScalarInt, ScalarInt>");
                 }
             }
             return createGeneric(ast, left, right, arit);
         }
 
         public static Specialized createSpecializedMultiType(RAny leftTemplate, RAny rightTemplate, final ASTNode ast, RNode left, RNode right, final ValueArithmetic arit) {
             if ((leftTemplate instanceof ScalarIntImpl || leftTemplate instanceof ScalarDoubleImpl) &&
                (rightTemplate instanceof ScalarIntImpl || rightTemplate instanceof ScalarDoubleImpl)) {
 
                 final boolean alwaysDouble = returnsDouble(arit);
                 Calculator c = new Calculator() {
                     @Override
                     public Object calc(RAny lexpr, RAny rexpr) throws UnexpectedResultException {
                         if (lexpr instanceof ScalarDoubleImpl) {
                             double ldbl = ((ScalarDoubleImpl) lexpr).getDouble();
                             boolean leftIsNA = RDouble.RDoubleUtils.isNA(ldbl);
                             if (rexpr instanceof ScalarDoubleImpl) {
                                 double rdbl = ((ScalarDoubleImpl) rexpr).getDouble();
                                 if (leftIsNA || RDouble.RDoubleUtils.isNA(rdbl)) {
                                     return RDouble.BOXED_NA;
                                 }
                                 return RDouble.RDoubleFactory.getScalar(arit.op(ast, ldbl, rdbl));
                             }
                             if (rexpr instanceof ScalarIntImpl) {
                                 int rint = ((ScalarIntImpl) rexpr).getInt();
                                 if (leftIsNA || rint == RInt.NA) {
                                     return RDouble.BOXED_NA;
                                 }
                                 return RDouble.RDoubleFactory.getScalar(arit.op(ast, ldbl, rint));
                             }
                         } else if (lexpr instanceof ScalarIntImpl) {
                             int lint = ((ScalarIntImpl) lexpr).getInt();
                             boolean leftIsNA = lint == RInt.NA;
                             if (rexpr instanceof ScalarDoubleImpl) {
                                 double rdbl = ((ScalarDoubleImpl) rexpr).getDouble();
                                 if (leftIsNA || RDouble.RDoubleUtils.isNA(rdbl)) {
                                     return RDouble.BOXED_NA;
                                 }
                                 return RDouble.RDoubleFactory.getScalar(arit.op(ast, lint, rdbl));
                             }
                             if (rexpr instanceof ScalarIntImpl) {
                                 int rint = ((ScalarIntImpl) rexpr).getInt();
                                 boolean isNA = leftIsNA || rint == RInt.NA;
                                 if (alwaysDouble) {
                                     if (isNA) {
                                         return RDouble.BOXED_NA;
                                     }
                                     return RDouble.RDoubleFactory.getScalar(arit.op(ast, (double) lint, (double) rint));
                                 } else {
                                     if (isNA) {
                                         return RInt.BOXED_NA;
                                     }
                                     return RInt.RIntFactory.getScalar(arit.op(ast, lint, rint));
                                 }
                             }
                         }
                         throw new UnexpectedResultException(FailedSpecialization.MULTI_TYPE);
                     }
                 };
                 return new Specialized(ast, left, right, arit, c, "<ScalarDouble|Int, ScalarDouble|Int>");
             }
             return null;
         }
 
         public static Specialized createGeneric(final ASTNode ast, RNode left, RNode right, final ValueArithmetic arit) {
             Calculator c;
             final boolean returnsDouble = returnsDouble(arit);
             c = new Calculator() {
                 @Override
                 public Object calc(RAny lexpr, RAny rexpr) {
                     if (lexpr instanceof RComplex || rexpr instanceof RComplex) {
                         RComplex lcmp = lexpr.asComplex();
                         RComplex rcmp = rexpr.asComplex();
                         return ComplexView.create(lcmp, rcmp, arit, ast);
                     }
                     if (returnsDouble || lexpr instanceof RDouble || rexpr instanceof RDouble) {
                         RDouble ldbl = lexpr.asDouble();
                         RDouble rdbl = rexpr.asDouble();  // if the cast fails, a zero-length array is returned
                         return DoubleView.create(ldbl, rdbl, arit, ast);
                     }
                     if (lexpr instanceof RInt || rexpr instanceof RInt || lexpr instanceof RLogical || rexpr instanceof RLogical) { // FIXME: this check should be simpler
                         RInt lint = lexpr.asInt();
                         RInt rint = rexpr.asInt();
                         return IntView.create(lint, rint, arit, ast);
                     }
                     throw RError.getNonNumericBinary(ast);
                 }
             };
             return new Specialized(ast, left, right, arit, c, "<Generic, Generic>");
         }
 
         @Override
         public final Object execute(RAny lexpr, RAny rexpr) {
             try {
                 return calc.calc(lexpr, rexpr);
             } catch (UnexpectedResultException e) {
                 FailedSpecialization f = (FailedSpecialization) e.getResult();
                 if (f == FailedSpecialization.FIXED_TYPE) {
                     Specialized sn = createSpecializedMultiType(lexpr, rexpr, ast, left, right, arit);
                     if (sn != null) {
                         replace(sn, "install SpecializedMultiType from Specialized");
                         return sn.execute(lexpr, rexpr);
                     }
                 }
                 Specialized gn = createGeneric(ast, left, right, arit);
                 replace(gn, "install Specialized<Generic, Generic> from Specialized");
                 return gn.execute(lexpr, rexpr);
             }
         }
     }
 
     static class SpecializedConst extends Arithmetic {
         final String dbg;
         final Calculator calc;
 
         public SpecializedConst(ASTNode ast, RNode left, RNode right, ValueArithmetic arit, Calculator calc, String dbg) {
             super(ast, left, right, arit);
             this.dbg = dbg;
             this.calc = calc;
         }
 
         public abstract static class Calculator {
             public abstract Object calc(RAny lexpr, RAny rexpr) throws UnexpectedResultException;
         }
 
         public static SpecializedConst createSpecialized(RAny leftTemplate, RAny rightTemplate, final ASTNode ast, RNode left, RNode right, final ValueArithmetic arit) {
             boolean leftConst = left instanceof Constant;
             boolean rightConst = right instanceof Constant;
             // non-const is complex
             if (leftConst && (rightTemplate instanceof ScalarComplexImpl) &&
                (leftTemplate instanceof ScalarComplexImpl || leftTemplate instanceof ScalarDoubleImpl || leftTemplate instanceof ScalarIntImpl || leftTemplate instanceof ScalarLogicalImpl)) {
                 RComplex lcmp = leftTemplate.asComplex();
                 final double lreal = lcmp.getReal(0);
                 final double limag =  lcmp.getImag(0);
                 final boolean isLeftNA = RComplex.RComplexUtils.eitherIsNA(lreal, limag);
                 Calculator c = new Calculator() {
                     @Override
                     public Object calc(RAny lexpr, RAny rexpr) throws UnexpectedResultException {
                         if (!(rexpr instanceof ScalarComplexImpl)) {
                             throw new UnexpectedResultException(null);
                         }
                         ScalarComplexImpl rcmp = (ScalarComplexImpl) rexpr;
                         double rreal = rcmp.getReal();
                         double rimag = rcmp.getImag();
                         if (isLeftNA || RComplex.RComplexUtils.eitherIsNA(rreal, rimag)) {
                             return RComplex.BOXED_NA;
                         }
                         return RComplex.RComplexFactory.getScalar(arit.opReal(ast, lreal, limag, rreal, rimag), arit.opImag(ast, lreal, limag, rreal, rimag));
                     }
                 };
                 return createLeftConst(ast, left, right, arit, c, "<ConstScalarNumber, ScalarComplex>");
             }
             if (rightConst && (leftTemplate instanceof ScalarComplexImpl) &&
                 (rightTemplate instanceof ScalarComplexImpl || rightTemplate instanceof ScalarDoubleImpl || rightTemplate instanceof ScalarIntImpl || rightTemplate instanceof ScalarLogicalImpl)) {
                  RComplex rcmp = rightTemplate.asComplex();
                  final double rreal = rcmp.getReal(0);
                  final double rimag =  rcmp.getImag(0);
                  final boolean isRightNA = RComplex.RComplexUtils.eitherIsNA(rreal, rimag);
                  Calculator c = new Calculator() {
                      @Override
                      public Object calc(RAny lexpr, RAny rexpr) throws UnexpectedResultException {
                          if (!(lexpr instanceof ScalarComplexImpl)) {
                              throw new UnexpectedResultException(null);
                          }
                          ScalarComplexImpl lcmp = (ScalarComplexImpl) lexpr;
                          double lreal = lcmp.getReal();
                          double limag = lcmp.getImag();
                          if (isRightNA || RComplex.RComplexUtils.eitherIsNA(lreal, limag)) {
                              return RComplex.BOXED_NA;
                          }
                          return RComplex.RComplexFactory.getScalar(arit.opReal(ast, lreal, limag, rreal, rimag), arit.opImag(ast, lreal, limag, rreal, rimag));
                      }
                  };
                 return createLeftConst(ast, left, right, arit, c, "<ScalarComplex, ConstScalarNumber>");
             }
             // non-const is double and const is complex
             if (leftConst && (rightTemplate instanceof ScalarDoubleImpl) && (leftTemplate instanceof ScalarComplexImpl)) {
                  ScalarComplexImpl lcmp = (ScalarComplexImpl) leftTemplate;
                  final double lreal = lcmp.getReal(0);
                  final double limag =  lcmp.getImag(0);
                  final boolean isLeftNA = RComplex.RComplexUtils.eitherIsNA(lreal, limag);
                  Calculator c = new Calculator() {
                      @Override
                      public Object calc(RAny lexpr, RAny rexpr) throws UnexpectedResultException {
                          if (!(rexpr instanceof ScalarDoubleImpl)) {
                              throw new UnexpectedResultException(null);
                          }
                          double rreal = ((ScalarDoubleImpl) rexpr).getDouble();
                          if (isLeftNA || RDouble.RDoubleUtils.isNA(rreal)) {
                              return RComplex.BOXED_NA;
                          }
                          return RComplex.RComplexFactory.getScalar(arit.opReal(ast, lreal, limag, rreal, 0), arit.opImag(ast, lreal, limag, rreal, 0));
                      }
                  };
                  return createLeftConst(ast, left, right, arit, c, "<ConstScalarComplex, ScalarDouble>");
             }
             if (rightConst && (leftTemplate instanceof ScalarDoubleImpl) && (rightTemplate instanceof ScalarComplexImpl)) {
                 ScalarComplexImpl rcmp = (ScalarComplexImpl) rightTemplate;
                 final double rreal = rcmp.getReal(0);
                 final double rimag =  rcmp.getImag(0);
                 final boolean isRightNA = RComplex.RComplexUtils.eitherIsNA(rreal, rimag);
                 Calculator c = new Calculator() {
                     @Override
                     public Object calc(RAny lexpr, RAny rexpr) throws UnexpectedResultException {
                         if (!(lexpr instanceof ScalarDoubleImpl)) {
                             throw new UnexpectedResultException(null);
                         }
                         double lreal = ((ScalarDoubleImpl) lexpr).getDouble();
                         if (isRightNA || RDouble.RDoubleUtils.isNA(lreal)) {
                             return RComplex.BOXED_NA;
                         }
                         return RComplex.RComplexFactory.getScalar(arit.opReal(ast, lreal, 0, rreal, rimag), arit.opImag(ast, lreal, 0, rreal, rimag));
                     }
                 };
                return createLeftConst(ast, left, right, arit, c, "<ScalarDouble, ConstScalarComplex>");
            }
             // non-const is double
             if (leftConst && (rightTemplate instanceof ScalarDoubleImpl) && (leftTemplate instanceof ScalarDoubleImpl || leftTemplate instanceof ScalarIntImpl || leftTemplate instanceof ScalarLogicalImpl)) {
                 final double ldbl = (leftTemplate.asDouble()).getDouble(0);
                 final boolean isLeftNA = RDouble.RDoubleUtils.isNA(ldbl);
                 Calculator c = new Calculator() {
                     @Override
                     public Object calc(RAny lexpr, RAny rexpr) throws UnexpectedResultException {
                         if (!(rexpr instanceof ScalarDoubleImpl)) {
                             throw new UnexpectedResultException(null);
                         }
                         double rdbl = ((ScalarDoubleImpl) rexpr).getDouble();
                         if (isLeftNA || RDouble.RDoubleUtils.isNA(rdbl)) {
                             return RDouble.BOXED_NA;
                         }
                         return RDouble.RDoubleFactory.getScalar(arit.op(ast, ldbl, rdbl));
                     }
                 };
                 return createLeftConst(ast, left, right, arit, c, "<ConstScalarNon-Complex, ScalarDouble>");
             }
             if (rightConst && (leftTemplate instanceof ScalarDoubleImpl) && (rightTemplate instanceof ScalarDoubleImpl || rightTemplate instanceof ScalarIntImpl || rightTemplate instanceof ScalarLogicalImpl)) {
                 final double rdbl = (rightTemplate.asDouble()).getDouble(0);
                 final boolean isRightNA = RDouble.RDoubleUtils.isNA(rdbl);
                 Calculator c = new Calculator() {
                     @Override
                     public Object calc(RAny lexpr, RAny rexpr) throws UnexpectedResultException {
                         if (!(lexpr instanceof ScalarDoubleImpl)) {
                             throw new UnexpectedResultException(null);
                         }
                         double ldbl = ((ScalarDoubleImpl) lexpr).getDouble();
                         if (isRightNA || RDouble.RDoubleUtils.isNA(ldbl)) {
                             return RDouble.BOXED_NA;
                         }
                         return RDouble.RDoubleFactory.getScalar(arit.op(ast, ldbl, rdbl));
                     }
                 };
                 return createRightConst(ast, left, right, arit, c, "<ScalarDouble, ConstScalarNon-Complex>");
             }
             // non-const is int and const is double
             // FIXME: handle also logical?
             if (leftConst && (leftTemplate instanceof ScalarDoubleImpl) && (rightTemplate instanceof ScalarIntImpl)) {
                 final double ldbl = (leftTemplate.asDouble()).getDouble(0);
                 final boolean isLeftNA = RDouble.RDoubleUtils.isNA(ldbl);
                 Calculator c = new Calculator() {
                     @Override
                     public Object calc(RAny lexpr, RAny rexpr) throws UnexpectedResultException {
                         if (!(rexpr instanceof ScalarIntImpl)) {
                             throw new UnexpectedResultException(null);
                         }
                         int rint = ((ScalarIntImpl) rexpr).getInt();
                         if (isLeftNA || rint == RInt.NA) {
                             return RDouble.BOXED_NA;
                         }
                         return RDouble.RDoubleFactory.getScalar(arit.op(ast, ldbl, rint));
                     }
                 };
                 return createLeftConst(ast, left, right, arit, c, "<ConstScalarDouble, ScalarInt>");
             }
             if (rightConst && (rightTemplate instanceof ScalarDoubleImpl) && (leftTemplate instanceof ScalarIntImpl)) {
                 final double rdbl = (rightTemplate.asDouble()).getDouble(0);
                 final boolean isRightNA = RDouble.RDoubleUtils.isNA(rdbl);
                 Calculator c = new Calculator() {
                     @Override
                     public Object calc(RAny lexpr, RAny rexpr) throws UnexpectedResultException {
                         if (!(lexpr instanceof ScalarIntImpl)) {
                             throw new UnexpectedResultException(null);
                         }
                         int lint = ((ScalarIntImpl) lexpr).getInt();
                         if (isRightNA || lint == RInt.NA) {
                             return RDouble.BOXED_NA;
                         }
                         return RDouble.RDoubleFactory.getScalar(arit.op(ast, lint, rdbl));
                     }
                 };
                 return createRightConst(ast, left, right, arit, c, "<ScalarInt, ConstScalarDouble>");
             }
             // non-const is int and const is int or logical
             if (leftConst && (leftTemplate instanceof ScalarIntImpl || leftTemplate instanceof ScalarLogicalImpl) && (rightTemplate instanceof ScalarIntImpl)) {
                 final int lint = (leftTemplate.asInt()).getInt(0);
                 final boolean isLeftNA = (lint == RInt.NA);
                 if (returnsDouble(arit)) {
                     final double ldbl = lint;
                     Calculator c = new Calculator() {
                         @Override
                         public Object calc(RAny lexpr, RAny rexpr) throws UnexpectedResultException {
                             if (!(rexpr instanceof ScalarIntImpl)) {
                                 throw new UnexpectedResultException(null);
                             }
                             int rint = ((ScalarIntImpl) rexpr).getInt();
                             if (isLeftNA || rint == RInt.NA) {
                                 return RDouble.BOXED_NA;
                             }
                             return RDouble.RDoubleFactory.getScalar(arit.op(ast, ldbl, (double) rint));
                         }
                     };
                     return createLeftConst(ast, left, right, arit, c, "<ConstScalarInt, ScalarInt>");
                 } else {
                     Calculator c = new Calculator() {
                         @Override
                         public Object calc(RAny lexpr, RAny rexpr) throws UnexpectedResultException {
                             if (!(rexpr instanceof ScalarIntImpl)) {
                                 throw new UnexpectedResultException(null);
                             }
                             int rint = ((ScalarIntImpl) rexpr).getInt();
                             if (isLeftNA || rint == RInt.NA) {
                                 return RInt.BOXED_NA;
                             }
                             return RInt.RIntFactory.getScalar(arit.op(ast, lint, rint));
                         }
                     };
                     return createLeftConst(ast, left, right, arit, c, "<ConstScalarInt, ScalarInt>");
                 }
             }
             if (rightConst && (rightTemplate instanceof ScalarIntImpl || rightTemplate instanceof ScalarLogicalImpl) && (leftTemplate instanceof ScalarIntImpl)) {
                 final int rint = (rightTemplate.asInt()).getInt(0);
                 final boolean isRightNA = (rint == RInt.NA);
                 if (returnsDouble(arit)) {
                     final double rdbl = rint;
                     Calculator c = new Calculator() {
                         @Override
                         public Object calc(RAny lexpr, RAny rexpr) throws UnexpectedResultException {
                             if (!(lexpr instanceof ScalarIntImpl)) {
                                 throw new UnexpectedResultException(null);
                             }
                             int lint = ((ScalarIntImpl) lexpr).getInt();
                             if (isRightNA || lint == RInt.NA) {
                                 return RDouble.BOXED_NA;
                             }
                             return RDouble.RDoubleFactory.getScalar(arit.op(ast, (double) lint, rdbl));
                         }
                     };
                     return createRightConst(ast, left, right, arit, c, "<ScalarInt, ConstScalarInt>");
                 } else {
                     Calculator c = new Calculator() {
                         @Override
                         public Object calc(RAny lexpr, RAny rexpr) throws UnexpectedResultException {
                             if (!(lexpr instanceof ScalarIntImpl)) {
                                 throw new UnexpectedResultException(null);
                             }
                             int lint = ((ScalarIntImpl) lexpr).getInt();
                             if (isRightNA || lint == RInt.NA) {
                                 return RInt.BOXED_NA;
                             }
                             return RInt.RIntFactory.getScalar(arit.op(ast, lint, rint));
                         }
                     };
                     return createRightConst(ast, left, right, arit, c, "<ScalarInt, ConstScalarInt>");
                 }
             }
             return createGeneric(leftTemplate, rightTemplate, ast, left, right, arit);
         }
 
         public static SpecializedConst createGeneric(RAny leftTemplate, RAny rightTemplate, final ASTNode ast, RNode left, RNode right, final ValueArithmetic arit) {
             Calculator c = null;
             boolean leftConst = left instanceof Constant;
             boolean rightConst = right instanceof Constant;
             final boolean returnsDouble = returnsDouble(arit);
 
             if (leftConst) {
                 final boolean leftComplex = leftTemplate instanceof RComplex;
                 final boolean leftDouble = leftTemplate instanceof RDouble;
                 final boolean leftLogicalOrInt = leftTemplate instanceof RLogical || leftTemplate instanceof RInt; // FIXME: does this pre-allocation pay off?
                 final RComplex lcmp = (leftComplex) ? (RComplex) leftTemplate : leftTemplate.asComplex();
                 final RDouble ldbl = (leftDouble) ? (RDouble) leftTemplate : leftTemplate.asDouble();
                 final RInt lint = (leftLogicalOrInt) ? leftTemplate.asInt() : null;
                 c = new Calculator() {
                     @Override
                     public Object calc(RAny lexpr, RAny rexpr) {
                         if (leftComplex || rexpr instanceof RComplex) {
                             RComplex rcmp = rexpr.asComplex();
                             return ComplexView.create(lcmp, rcmp, arit, ast);
                         }
                         if (returnsDouble || leftDouble || rexpr instanceof RDouble) {
                             RDouble rdbl = rexpr.asDouble();  // if the cast fails, a zero-length array is returned
                             return DoubleView.create(ldbl, rdbl, arit, ast);
                         }
                         if (leftLogicalOrInt || rexpr instanceof RInt || rexpr instanceof RLogical) { // FIXME: this check should be simpler
                             RInt rint = rexpr.asInt();
                             return IntView.create(lint, rint, arit, ast);
                         }
                         Utils.nyi("unsupported case for binary arithmetic operation");
                         return null;
                     }
                 };
             }
             if (rightConst) {
                 final boolean rightComplex = rightTemplate instanceof RComplex;
                 final boolean rightDouble = rightTemplate instanceof RDouble;
                 final boolean rightLogicalOrInt = rightTemplate instanceof RLogical || rightTemplate instanceof RInt;
                 final RComplex rcmp = (rightComplex) ? (RComplex) rightTemplate : rightTemplate.asComplex();
                 final RDouble rdbl = (rightDouble) ? (RDouble) rightTemplate : rightTemplate.asDouble();
                 final RInt rint = (rightLogicalOrInt) ? rightTemplate.asInt() : null;
                 c = new Calculator() {
                     @Override
                     public Object calc(RAny lexpr, RAny rexpr) {
                         if (rightComplex || lexpr instanceof RComplex) {
                             RComplex lcmp = lexpr.asComplex();
                             return ComplexView.create(lcmp, rcmp, arit, ast);
                         }
                         if (returnsDouble || rightDouble || lexpr instanceof RDouble) {
                             RDouble ldbl = lexpr.asDouble();  // if the cast fails, a zero-length array is returned
                             return DoubleView.create(ldbl, rdbl, arit, ast);
                         }
                         if (rightLogicalOrInt || lexpr instanceof RInt || lexpr instanceof RLogical) { // FIXME: this check should be simpler
                             RInt lint = lexpr.asInt();
                             return IntView.create(lint, rint, arit, ast);
                         }
                         Utils.nyi("unsupported case for binary arithmetic operation");
                         return null;
                     }
                 };
             }
             if (c == null) {
                 Utils.nyi("unreachable");
                 return null;
             }
             if (rightConst) {
                 return createRightConst(ast, left, right, arit, c, "<Generic, ConstGeneric>");
             } else {
                 return createLeftConst(ast, left, right, arit, c, "<ConstGeneric, Generic>");
             }
         }
 
         public static SpecializedConst createLeftConst(ASTNode ast, RNode left, RNode right, ValueArithmetic arit, Calculator calc, String dbg) {
             return new SpecializedConst(ast, left, right, arit, calc, dbg) {
                 @Override
                 public Object execute(Frame frame) {
                     RAny rexpr = (RAny) right.execute(frame);
                     return execute(null, rexpr);
                 }
             };
         }
 
         public static SpecializedConst createRightConst(ASTNode ast, RNode left, RNode right, ValueArithmetic arit, Calculator calc, String dbg) {
             return new SpecializedConst(ast, left, right, arit, calc, dbg) {
                 @Override
                 public Object execute(Frame frame) {
                     RAny lexpr = (RAny) left.execute(frame);
                     return execute(lexpr, null);
                 }
             };
         }
 
         private static RAny getExpr(RNode node, RAny value) {
             if (value == null) {
                 return (RAny) node.execute(null);
             } else {
                 return value;
             }
         }
 
         @Override
         public Object execute(RAny lexpr, RAny rexpr) {
             try {
                 return calc.calc(lexpr, rexpr);
             } catch (UnexpectedResultException e) {
                 RAny leftTemplate = getExpr(left, lexpr);
                 RAny rightTemplate = getExpr(right, rexpr);
                 SpecializedConst gn = createGeneric(leftTemplate, rightTemplate, ast, left, right, arit);
                 replace(gn, "install SpecializedConst<Generic, Generic> from SpecializedConst");
                 if (DEBUG_AR) Utils.debug("Rewrote Const" + dbg + " to " + gn.dbg);
                 return gn.execute(leftTemplate, rightTemplate);
             }
         }
     }
 
     public abstract static class ValueArithmetic {
         public abstract double opReal(ASTNode ast, double a, double b, double c, double d); // (a + bi)  op  (c + di)
         public abstract double opImag(ASTNode ast, double a, double b, double c, double d);
 
         public abstract double op(ASTNode ast, double a, double b);
         public abstract int op(ASTNode ast, int a, int b);
 
         public double op(ASTNode ast, double a, int b) {
             return op(ast, a, (double) b);
         }
         public double op(ASTNode ast, int a, double b) {
             return op(ast, (double) a, b);
         }
 
         public abstract RComplex op(ASTNode ast, ComplexImpl xcomp, ComplexImpl ycomp, int size, int[] dimensions, Names names, Attributes attributes);
         public abstract RComplex op(ASTNode ast, ComplexImpl xcomp, double c, double d, int size, int[] dimensions, Names names, Attributes attributes);
     }
 
     public static final class Add extends ValueArithmetic {
         @Override
         public double opReal(ASTNode ast, double a, double b, double c, double d) {
             return a + c;
         }
         @Override
         public double opImag(ASTNode ast, double a, double b, double c, double d) {
             return b + d;
         }
         @Override
         public double op(ASTNode ast, double a, double b) {
             return a + b;
         }
         @Override
         public int op(ASTNode ast, int a, int b) {
             int r = a + b;
             boolean bLTr = b < r;
             if (a > 0) {
                 if (bLTr) {
                     return r;
                 }
             } else {
                 if (!bLTr) {
                     return r;
                 }
             }
             return RInt.NA;
         }
         @Override
         public RComplex op(ASTNode ast, ComplexImpl xcomp, ComplexImpl ycomp, int size, int[] dimensions, Names names, Attributes attributes) {
             int rsize = size * 2;
             double[] res = new double[rsize];
             double[] x = xcomp.getContent();
             double[] y = ycomp.getContent();
             int j = 1;
             for (int i = 0; i < rsize; i++, i++, j++, j++) {
                 double a = x[i];
                 double b = x[j];
                 double c = y[i];
                 double d = y[j];
                 if (!RComplexUtils.eitherIsNA(a, b) && !RComplexUtils.eitherIsNA(c, d)) {
                     res[i] = a + c;
                     res[j] = b + d;
                 } else {
                     res[i] = RDouble.NA;
                     res[j] = RDouble.NA;
                 }
             }
             return RComplex.RComplexFactory.getFor(res, dimensions, names, attributes);
         }
         @Override
         public RComplex op(ASTNode ast, ComplexImpl xcomp, double c, double d, int size, int[] dimensions, Names names, Attributes attributes) {
             int rsize = size * 2;
             double[] res = new double[rsize];
             double[] x = xcomp.getContent();
             int j = 1;
             for (int i = 0; i < rsize; i++, i++, j++, j++) {
                 double a = x[i];
                 double b = x[j];
                 if (!RComplexUtils.eitherIsNA(a, b)) {
                     res[i] = a + c;
                     res[j] = b + d;
                 } else {
                     res[i] = RDouble.NA;
                     res[j] = RDouble.NA;
                 }
             }
             return RComplex.RComplexFactory.getFor(res, dimensions, names, attributes);
         }
     }
 
     public static final class Sub extends ValueArithmetic {
         @Override
         public double opReal(ASTNode ast, double a, double b, double c, double d) {
             return a - c;
         }
         @Override
         public double opImag(ASTNode ast, double a, double b, double c, double d) {
             return b - d;
         }
         @Override
         public double op(ASTNode ast, double a, double b) {
             return a - b;
         }
         @Override
         public int op(ASTNode ast, int a, int b) {
             int r = a - b;
             if ((a < 0 == b < 0) || (a < 0 == r < 0)) {
                 return r;
             } else {
                 return RInt.NA;
             }
         }
         @Override
         public RComplex op(ASTNode ast, ComplexImpl xcomp, ComplexImpl ycomp, int size, int[] dimensions, Names names, Attributes attributes) {
             int rsize = size * 2;
             double[] res = new double[rsize];
             double[] x = xcomp.getContent();
             double[] y = ycomp.getContent();
             int j = 1;
             for (int i = 0; i < rsize; i++, i++, j++, j++) {
                 double a = x[i];
                 double b = x[j];
                 double c = y[i];
                 double d = y[j];
                 if (!RComplexUtils.eitherIsNA(a, b) && !RComplexUtils.eitherIsNA(c, d)) {
                     res[i] = a - c;
                     res[j] = b - d;
                 } else {
                     res[i] = RDouble.NA;
                     res[j] = RDouble.NA;
                 }
             }
             return RComplex.RComplexFactory.getFor(res, dimensions, names, attributes);
         }
         @Override
         public RComplex op(ASTNode ast, ComplexImpl xcomp, double c, double d, int size, int[] dimensions, Names names, Attributes attributes) {
             int rsize = size * 2;
             double[] res = new double[rsize];
             double[] x = xcomp.getContent();
             int j = 1;
             for (int i = 0; i < rsize; i++, i++, j++, j++) {
                 double a = x[i];
                 double b = x[j];
                 if (!RComplexUtils.eitherIsNA(a, b)) {
                     res[i] = a - c;
                     res[j] = b - d;
                 } else {
                     res[i] = RDouble.NA;
                     res[j] = RDouble.NA;
                 }
             }
             return RComplex.RComplexFactory.getFor(res, dimensions, names, attributes);
         }
     }
 
     public static final class Mult extends ValueArithmetic { // FIXME: will be slow for complex numbers (same calculations for real and imaginary parts)
         @Override
         public double opReal(ASTNode ast, double a, double b, double c, double d) {
             return a * c - b * d;
         }
         @Override
         public double opImag(ASTNode ast, double a, double b, double c, double d) {
             return b * c + a * d;
         }
         @Override
         public double op(ASTNode ast, double a, double b) {
             return a * b;
         }
         @Override
         public int op(ASTNode ast, int a, int b) {
             long l = (long) a * (long) b;
             if (!(l < Integer.MIN_VALUE || l > Integer.MAX_VALUE)) {
                 return (int) l;
             } else {
                 return RInt.NA;
             }
         }
         @Override
         public RComplex op(ASTNode ast, ComplexImpl xcomp, ComplexImpl ycomp, int size, int[] dimensions, Names names, Attributes attributes) {
             int rsize = size * 2;
             double[] res = new double[rsize];
             double[] x = xcomp.getContent();
             double[] y = ycomp.getContent();
             int j = 1;
             for (int i = 0; i < rsize; i++, i++, j++, j++) {
                 double a = x[i];
                 double b = x[j];
                 double c = y[i];
                 double d = y[j];
                 if (!RComplexUtils.eitherIsNA(a, b) && !RComplexUtils.eitherIsNA(c, d)) {
                     res[i] = a * c - b * d;
                     res[j] = b * c + a * d;
                 } else {
                     res[i] = RDouble.NA;
                     res[j] = RDouble.NA;
                 }
             }
             return RComplex.RComplexFactory.getFor(res, dimensions, names, attributes);
         }
         @Override
         public RComplex op(ASTNode ast, ComplexImpl xcomp, double c, double d, int size, int[] dimensions, Names names, Attributes attributes) {
             int rsize = size * 2;
             double[] res = new double[rsize];
             double[] x = xcomp.getContent();
             int j = 1;
             for (int i = 0; i < rsize; i++, i++, j++, j++) {
                 double a = x[i];
                 double b = x[j];
                 if (!RComplexUtils.eitherIsNA(a, b)) {
                     res[i] = a * c - b * d;
                     res[j] = b * c + a * d;
                 } else {
                     res[i] = RDouble.NA;
                     res[j] = RDouble.NA;
                 }
             }
             return RComplex.RComplexFactory.getFor(res, dimensions, names, attributes);
         }
     }
 
     public static final class Pow extends ValueArithmetic { // FIXME: will be slow for complex numbers (same calculations for real and imaginary parts)
 
         private static double[] _Z = new double[2];
 
         private static void cpow(double xr, double xi, double yr, double yi) {
             cpow(xr, xi, yr, yi, _Z, 0);
         }
 
         private static void cpow(double xr, double xi, double yr, double yi, double[] z, int offset) {
             if (xr == 0) {
                 if (yi == 0) {
                     z[offset] = Math.pow(0, yr);
                     z[offset + 1] = xi;
                 } else {
                     z[offset] = Double.NaN;
                     z[offset + 1] = Double.NaN;
                 }
             } else {
                 double zr = Math.hypot(xr, xi);
                 double zi = Math.atan2(xi, xr);
                 double theta = zi * yr;
                 double rho;
                 if (yi == 0) {
                     rho = Math.pow(zr, yr);
                 } else {
                     zr = Math.log(zr);
                     theta += zr * yi;
                     rho = Math.exp(zr * yr - zi * yi);
                 }
                 z[offset] = rho * Math.cos(theta);
                 z[offset + 1] = rho * Math.sin(theta);
             }
         }
 
 
         @Override
         public double opReal(ASTNode ast, double a, double b, double c, double d) {
             Utils.nyi();
             return -1;
         }
         @Override
         public double opImag(ASTNode ast, double a, double b, double c, double d) {
             Utils.nyi();
             return -1;
         }
         @Override
         public double op(ASTNode ast, double a, double b) {
             return Math.pow(a, b); // FIXME: check that the R rules correspond to Java
         }
         @Override
         public int op(ASTNode ast, int a, int b) {
             Utils.nyi("unreachable");
             return -1;
         }
         @Override
         public RComplex op(ASTNode ast, ComplexImpl xcomp, ComplexImpl ycomp, int size, int[] dimensions, Names names, Attributes attributes) {
             Utils.nyi();
             return null;
         }
 
         @Override
         public RComplex op(ASTNode ast, ComplexImpl xcomp, double yr, double yi, int size, int[] dimensions, Names names, Attributes attributes) {
             double[] x = xcomp.getContent();
             double[] z = new double[x.length];
             for (int i = 0; i < x.length; i += 2) {
                 cpow(x[i], x[i + 1], yr, yi, z, i);
             }
             return RComplex.RComplexFactory.getFor(z, dimensions, names);
         }
     }
 
     public static final class Div extends ValueArithmetic { // FIXME: will be slow for complex numbers (same calculations for real and imaginary parts)
         @Override
         public double opReal(ASTNode ast, double a, double b, double c, double d) {
             return (a * c + b * d) / (c * c + d * d);
         }
         @Override
         public double opImag(ASTNode ast, double a, double b, double c, double d) {
             return (b * c - a * d) / (c * c + d * d);
         }
         @Override
         public double op(ASTNode ast, double a, double b) {
             return a / b; // FIXME: check that the R rules correspond to Java
         }
         @Override
         public int op(ASTNode ast, int a, int b) {
             Utils.nyi("unreachable");
             return -1;
         }
         @Override
         public RComplex op(ASTNode ast, ComplexImpl xcomp, ComplexImpl ycomp, int size, int[] dimensions, Names names, Attributes attributes) {
             int rsize = size * 2;
             double[] res = new double[rsize];
             double[] x = xcomp.getContent();
             double[] y = ycomp.getContent();
             int j = 1;
             for (int i = 0; i < rsize; i++, i++, j++, j++) {
                 double a = x[i];
                 double b = x[j];
                 double c = y[i];
                 double d = y[j];
                 if (!RComplexUtils.eitherIsNA(a, b) && !RComplexUtils.eitherIsNA(c, d)) {
                     double denom = c * c + d * d;
                     res[i] = (a * c + b * d) / denom;
                     res[j] = (b * c - a * d) / denom;
                 } else {
                     res[i] = RDouble.NA;
                     res[j] = RDouble.NA;
                 }
             }
             return RComplex.RComplexFactory.getFor(res, dimensions, names, attributes);
         }
         @Override
         public RComplex op(ASTNode ast, ComplexImpl xcomp, double c, double d, int size, int[] dimensions, Names names, Attributes attributes) {
             int rsize = size * 2;
             double[] res = new double[rsize];
             double[] x = xcomp.getContent();
             double denom = c * c + d * d;
             int j = 1;
             for (int i = 0; i < rsize; i++, i++, j++, j++) {
                 double a = x[i];
                 double b = x[j];
                 if (!RComplexUtils.eitherIsNA(a, b)) {
                     res[i] = (a * c + b * d) / denom;
                     res[j] = (b * c - a * d) / denom;
                 } else {
                     res[i] = RDouble.NA;
                     res[j] = RDouble.NA;
                 }
             }
             return RComplex.RComplexFactory.getFor(res, dimensions, names, attributes);
         }
     }
 
     public static final class IntegerDiv extends ValueArithmetic {
         @Override
         public double opReal(ASTNode ast, double a, double b, double c, double d) {
             throw RError.getUnimplementedComplex(ast);
         }
         @Override
         public double opImag(ASTNode ast, double a, double b, double c, double d) {
             throw RError.getUnimplementedComplex(ast);
         }
         @Override
         public double op(ASTNode ast, double a, double b) {
             double q = a / b;
             if (b != 0) {
                 double qfloor = Math.floor(q);
                 double tmp = a - qfloor * b; // FIXME: this is R implementation, check if we can avoid this in Java
                 return qfloor + Math.floor(tmp / b);
 
             } else {
                 return q;
             }
         }
         @Override
         public int op(ASTNode ast, int a, int b) {
             if (b != 0) {
                 return (int) Math.floor((double) a / (double) b); // FIXME: this is R implementation, can we do faster without floating point?
             } else {
                 return RInt.NA;
             }
         }
         @Override
         public RComplex op(ASTNode ast, ComplexImpl xcomp, ComplexImpl ycomp, int size, int[] dimensions, Names names, Attributes attributes) {
             throw RError.getUnimplementedComplex(ast);
         }
         @Override
         public RComplex op(ASTNode ast, ComplexImpl xcomp, double c, double d, int size, int[] dimensions, Names names, Attributes attributes) {
             throw RError.getUnimplementedComplex(ast);
         }
     }
 
     public static double fmod(ASTNode ast, double a, double b) { // FIXME: this is R implementation, can we do faster in Java?
         double q = a / b;
         if (b != 0) {
             double tmp = a - Math.floor(q) * b;
             if (RDouble.RDoubleUtils.isFinite(q) && Math.abs(q) > 1 / RDouble.EPSILON) {
                 // FIXME: warning: probable complete loss of accuracy in modulus
                 RContext.warning(ast, RError.ACCURACY_MODULUS);
             }
             return tmp - Math.floor(tmp / b) * b;
         } else {
             return RDouble.NaN;
         }
     }
 
     public static final class Mod extends ValueArithmetic {
         @Override
         public double opReal(ASTNode ast, double a, double b, double c, double d) {
             throw RError.getUnimplementedComplex(ast);
         }
         @Override
         public double opImag(ASTNode ast, double a, double b, double c, double d) {
             throw RError.getUnimplementedComplex(ast);
         }
         @Override
         public double op(ASTNode ast, double a, double b) {
             return fmod(ast, a, b);
         }
         @Override
         public int op(ASTNode ast, int a, int b) {
             if (b != 0) {
                 if (a >= 0 && b > 0) {
                     return a % b;
                 } else {
                     return (int) fmod(ast, a, b);
                 }
             } else {
                 return RInt.NA;
             }
         }
         @Override
         public RComplex op(ASTNode ast, ComplexImpl xcomp, ComplexImpl ycomp, int size, int[] dimensions, Names names, Attributes attributes) {
             throw RError.getUnimplementedComplex(ast);
         }
         @Override
         public RComplex op(ASTNode ast, ComplexImpl xcomp, double c, double d, int size, int[] dimensions, Names names, Attributes attributes) {
             throw RError.getUnimplementedComplex(ast);
         }
     }
 
     public static final Add ADD = new Add();
     public static final Sub SUB = new Sub();
     public static final Mult MULT = new Mult();
     public static final Pow POW = new Pow();
     public static final Div DIV = new Div();
     public static final IntegerDiv INTEGER_DIV = new IntegerDiv();
     public static final Mod MOD = new Mod();
 
     static class ComplexView extends View.RComplexView implements RComplex {
         final RComplex a;
         final RComplex b;
         final int na;
         final int nb;
         final int n;
         final int[] dimensions;
         final Names names;
         final Attributes attributes;
         boolean overflown = false;
 
         final ValueArithmetic arit;
         final ASTNode ast;
 
         // limiting view depth
         private int depth;  // total views involved
 
         public static RComplex create(RComplex a, RComplex b, ValueArithmetic arit, ASTNode ast) {
             if (EAGER_COMPLEX) {
                 int asize = a.size();
                 if (asize > 1) {
                     int bsize = b.size();
                     if (asize == bsize) {
                         return arit.op(ast, (ComplexImpl) a.materialize(), (ComplexImpl) b.materialize(), asize, resultDimensions(ast, a, b), resultNames(ast, a, b), resultAttributes(ast, a, b));
                     }
                     if (bsize == 1) {
                         double c = b.getReal(0);
                         double d = b.getImag(0);
                         if (!RComplexUtils.eitherIsNA(c, d)) {
                             return arit.op(ast, (ComplexImpl) a.materialize(), c, d, asize, resultDimensions(ast, a, b), resultNames(ast, a, b), resultAttributes(ast, a, b));
                         }
                         // NOTE: NA case falls back, could be added here
                     }
                 }
             }
             int depth = 0;
             if (LIMIT_VIEW_DEPTH) {
                 int adepth = (a instanceof ComplexView) ? ((ComplexView) a).depth : 0; // FIXME what about chains of double/complex views, etc?
                 int bdepth = (b instanceof ComplexView) ? ((ComplexView) b).depth : 0;
                 depth = adepth + bdepth + 1;
             }
             int[] dim = resultDimensions(ast, a, b);
             Names names = resultNames(ast, a, b);
             Attributes attributes = resultAttributes(ast, a, b);
             ComplexView res = new ComplexView(a, b, dim, names, attributes, depth, arit, ast);
             if (EAGER || (LIMIT_VIEW_DEPTH && (depth > MAX_VIEW_DEPTH)) || (a instanceof ScalarComplexImpl && b instanceof ScalarComplexImpl)) {
                 return RComplexFactory.copy(res);
             }
             return res;
         }
 
         public ComplexView(RComplex a, RComplex b, int[] dimensions, Names names, Attributes attributes, int depth, ValueArithmetic arit, ASTNode ast) {
             this.a = a;
             this.b = b;
             na = a.size();
             nb = b.size();
             this.ast = ast;
             this.arit = arit;
             this.dimensions = dimensions;
             this.names = names;
             this.attributes = attributes;
             this.depth = depth;
 
             if (na > nb) {
                 n = na;
                 if ((n / nb) * nb != n) {
                     RContext.warning(ast, RError.LENGTH_NOT_MULTI);
                 }
             } else {
                 n = nb;
                 if ((n / na) * na != n) {
                     RContext.warning(ast, RError.LENGTH_NOT_MULTI);
                 }
             }
         }
 
         @Override
         public int size() {
             return n;
         }
 
         @Override
         public double getReal(int i) { // FIXME: this is very slow (real and imag getters repeat the same computation)
             int ai;
             int bi;
             if (i >= na) {
                 ai = i % na;
                 bi = i;
             } else if (i >= nb) {
                 bi = i % nb;
                 ai = i;
             } else {
                 ai = i;
                 bi = i;
             }
             double areal = a.getReal(ai);
             double aimag = a.getImag(ai);
             double breal = b.getReal(bi);
             double bimag = b.getImag(bi);
             if (!RComplexUtils.eitherIsNA(areal, aimag) && !RComplexUtils.eitherIsNA(breal, bimag)) {
                 return arit.opReal(ast, areal, aimag, breal, bimag);
             } else {
                 return RDouble.NA;
             }
         }
 
         @Override
         public double getImag(int i) { // FIXME: this is very slow (real and imag getters repeat the same computation)
             int ai;
             int bi;
             if (i >= na) {
                 ai = i % na;
                 bi = i;
             } else if (i >= nb) {
                 bi = i % nb;
                 ai = i;
             } else {
                 ai = i;
                 bi = i;
             }
             double areal = a.getReal(ai);
             double aimag = a.getImag(ai);
             double breal = b.getReal(bi);
             double bimag = b.getImag(bi);
             if (!RComplexUtils.eitherIsNA(areal, aimag) && !RComplexUtils.eitherIsNA(breal, bimag)) {
                 return arit.opImag(ast, areal, aimag, breal, bimag);
             } else {
                 return RDouble.NA;
             }
         }
 
         @Override
         public boolean isSharedReal() {
             return a.isShared() || b.isShared();
         }
 
         @Override
         public void ref() {
             a.ref();
             b.ref();
         }
 
         @Override
         public int[] dimensions() {
             return dimensions;
         }
 
         @Override
         public Names names() {
             return names;
         }
 
         @Override
         public Attributes attributes() {
             return attributes;
         }
     }
 
     static class DoubleView extends View.RDoubleView implements RDouble {
         final RDouble a;
         final RDouble b;
         final int na;
         final int nb;
         final int n;
         final int[] dimensions;
         final Names names;
         final Attributes attributes;
 
         final ValueArithmetic arit;
         final ASTNode ast;
 
         // limiting view depth
         private int depth;  // total views involved
 
         // profiling
         private int profileUsage; // at creation time, what was the total usage of both children  PLUS all local calls to getDouble
         private int profileDepth; // max of child depths at creation time
 
         public static RDouble create(RDouble a, RDouble b, ValueArithmetic arit, ASTNode ast) {
             int depth = 0;
             if (LIMIT_VIEW_DEPTH) {
                 int adepth = (a instanceof DoubleView) ? ((DoubleView) a).depth : 0;
                 int bdepth = (b instanceof DoubleView) ? ((DoubleView) b).depth : 0;
                 depth = adepth + bdepth + 1;
             }
             int[] dim = resultDimensions(ast, a, b);
             Names names = resultNames(ast, a, b);
             Attributes attributes = resultAttributes(ast, a, b);
             DoubleView res = new DoubleView(a, b, dim, names, attributes, depth, arit, ast);
             if (PROFILE_DOUBLE_VIEWS) {
                 int d = 1;
                 int ausa = 0;
                 int busa = 0;
                 if (a instanceof DoubleView) {
                     d = ((DoubleView) a).profileDepth + 1;
                     ausa = ((DoubleView) a).profileUsage;
                 }
                 if (b instanceof DoubleView) {
                     int bd = ((DoubleView) b).profileDepth + 1;
                     if (bd > d) {
                         d = bd;
                     }
                     busa = ((DoubleView) b).profileUsage;
                 }
                 res.profileDepth = d;
                 res.profileUsage = ausa + busa;
                 int asize = a.size();
                 int bsize = b.size();
                 int size = (asize > bsize) ? asize : bsize;
                 Utils.debug("CREATED DOUBLE VIEW DEPTH " + d + " SIZE " + size + " USAGE " + (ausa + busa) + " A-USAGE " + ausa + " B-USAGE " + busa);
 
                 if (size == 1) {
                     throw new RuntimeException("How come the view has only size 1?");
                 }
             }
             if (EAGER || (LIMIT_VIEW_DEPTH && (depth > MAX_VIEW_DEPTH)) ||  (a instanceof ScalarDoubleImpl && b instanceof ScalarDoubleImpl)) {
                 return RDoubleFactory.copy(res);
             }
             return res;
         }
 
         public DoubleView(RDouble a, RDouble b, int[] dimensions, Names names, Attributes attributes, int depth, ValueArithmetic arit, ASTNode ast) {
             this.a = a;
             this.b = b;
             na = a.size();
             nb = b.size();
             this.depth = depth;
 
             this.arit = arit;
             this.ast = ast;
             this.dimensions = dimensions;
             this.names = names;
             this.attributes = attributes;
 
             if (na > nb) {
                 n = na;
                 if ((n / nb) * nb != n) {
                     RContext.warning(ast, RError.LENGTH_NOT_MULTI);
                 }
             } else {
                 n = nb;
                 if ((n / na) * na != n) {
                     RContext.warning(ast, RError.LENGTH_NOT_MULTI);
                 }
             }
         }
 
         @Override
         public int size() {
             return n;
         }
 
         @Override
         public double getDouble(int i) {
 
             if (PROFILE_DOUBLE_VIEWS) {
                 profileUsage++;
             }
 
             int ai;
             int bi;
             if (i >= na) {
                 ai = i % na;
                 bi = i;
             } else if (i >= nb) {
                 bi = i % nb;
                 ai = i;
             } else {
                 ai = i;
                 bi = i;
             }
             double adbl = a.getDouble(ai);
             double bdbl = b.getDouble(bi);
             if (RDouble.RDoubleUtils.isNA(adbl) || RDouble.RDoubleUtils.isNA(bdbl)) {
                 return RDouble.NA;
             } else {
                 return arit.op(ast, adbl, bdbl);
             }
          }
 
         @Override
         public boolean isSharedReal() {
             return a.isShared() || b.isShared();
         }
 
         @Override
         public void ref() {
             a.ref();
             b.ref();
         }
 
         @Override
         public int[] dimensions() {
             return dimensions;
         }
 
         @Override
         public Names names() {
             return names;
         }
 
         @Override
         public Attributes attributes() {
             return attributes;
         }
     }
 
     static class IntView extends View.RIntView implements RInt {
         final RInt a;
         final RInt b;
         final int na;
         final int nb;
         final int n;
         final int[] dimensions;
         final Names names;
         final Attributes attributes;
         boolean overflown = false;
 
         final ValueArithmetic arit;
         final ASTNode ast;
 
         // limiting view depth
         private int depth;  // total views involved
 
         public static RInt create(RInt a, RInt b, ValueArithmetic arit, ASTNode ast) {
             int depth = 0;
             if (LIMIT_VIEW_DEPTH) {
                 int adepth = (a instanceof IntView) ? ((IntView) a).depth : 0;
                 int bdepth = (b instanceof IntView) ? ((IntView) b).depth : 0;
                 depth = adepth + bdepth + 1;
             }
             int[] dim = resultDimensions(ast, a, b);
             Names names = resultNames(ast, a, b);
             Attributes attributes = resultAttributes(ast, a, b);
             IntView res = new IntView(a, b, dim, names, attributes, depth, arit, ast);
             if (EAGER || (LIMIT_VIEW_DEPTH && (depth > MAX_VIEW_DEPTH)) || (a instanceof ScalarIntImpl && b instanceof ScalarIntImpl)) {
                 return RIntFactory.copy(res);
             }
             return res;
         }
 
         public IntView(RInt a, RInt b, int[] dimensions, Names names, Attributes attributes, int depth, ValueArithmetic arit, ASTNode ast) {
             this.a = a;
             this.b = b;
             na = a.size();
             nb = b.size();
             this.ast = ast;
             this.arit = arit;
             this.dimensions = dimensions;
             this.names = names;
             this.attributes = attributes;
             this.depth = depth;
 
             if (na > nb) {
                 n = na;
                 if ((n / nb) * nb != n) {
                     RContext.warning(ast, RError.LENGTH_NOT_MULTI);
                 }
             } else {
                 n = nb;
                 if ((n / na) * na != n) {
                     RContext.warning(ast, RError.LENGTH_NOT_MULTI);
                 }
             }
         }
 
         @Override
         public int size() {
             return n;
         }
 
         @Override
         public int getInt(int i) {
             int ai;
             int bi;
             if (i >= na) {
                 ai = i % na;
                 bi = i;
             } else if (i >= nb) {
                 bi = i % nb;
                 ai = i;
             } else {
                 ai = i;
                 bi = i;
             }
             int aint = a.getInt(ai);
             int bint = b.getInt(bi);
             if (aint == RInt.NA || bint == RInt.NA) {
                 return RInt.NA;
             } else {
                 int res = arit.op(ast, aint, bint);
                 if (res == RInt.NA && !overflown) {
                     overflown = true;
                     RContext.warning(ast, RError.INTEGER_OVERFLOW);
                 }
                 return res;
             }
         }
 
         @Override
         public boolean isSharedReal() {
             return a.isShared() || b.isShared();
         }
 
         @Override
         public void ref() {
             a.ref();
             b.ref();
         }
 
         @Override
         public int[] dimensions() {
             return dimensions;
         }
 
         @Override
         public Names names() {
             return names;
         }
 
         @Override
         public Attributes attributes() {
             return attributes;
         }
     }
 
     public static int[] resultDimensions(ASTNode ast, RArray a, RArray b) {
         int[] dima = a.dimensions();
         int[] dimb = b.dimensions();
         if (dimb == null) {
             return dima;
         }
         if (dima == null) {
             return dimb;
         }
         if (dima == dimb) {
             return dima;
         }
         int alen = dima.length;
         int blen = dimb.length;
 
         if (alen == 2 && blen == 2 && dima[0] == dimb[0] && dima[1] == dimb[1]) {
             return dima;
         }
 
         if (alen == blen) {
             for (int i = 0; i < alen; i++) {
                 if (dima[i] != dimb[i]) {
                     throw RError.getNonConformableArrays(ast);
                 }
             }
             return dima;
         }
         throw RError.getNonConformableArrays(ast);
     }
 
     public static Names resultNames(ASTNode ast, RArray a, RArray b) {
         Names na = a.names();
         Names nb = b.names();
         if (nb == null) {
             return na;
         }
         if (na == null) {
             return nb;
         }
         if (na == nb) {
             return na;
         }
         int asize = a.size();
         int bsize = b.size();
 
         if (bsize > asize) {
             return nb;
         } else {
             return na;
         }
     }
 
     // note: increments reference count on attributes
     public static Attributes resultAttributes(ASTNode ast, RArray a, RArray b) {
         Attributes aa = a.attributes();
         Attributes ba = b.attributes();
 
         if (ba == null && aa == null) {
             return null;
         }
         int asize = a.size();
         int bsize = b.size();
 
         if (asize > bsize) {
             return Attributes.markShared(aa);
         }
         if (bsize > asize) {
             return Attributes.markShared(ba);
         }
         // asize == bsize
         if (ba == null) {
             return Attributes.markShared(aa);
         }
         if (aa == null) {
             return Attributes.markShared(ba);
         }
         // both aa != null and ba != null
 
         Attributes res = ba.copy();
         Map<RSymbol, RAny> amap = aa.map();
         for (Map.Entry<RSymbol, RAny> ae : amap.entrySet()) {
             RAny value = ae.getValue();
             value.ref();
             res.put(ae.getKey(), value);
         }
         return res;
     }
 }
 

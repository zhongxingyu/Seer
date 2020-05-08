 package xi.ast;
 
 import static xi.ast.BuiltIn.B;
 import static xi.ast.BuiltIn.B_STAR;
 import static xi.ast.BuiltIn.C;
 import static xi.ast.BuiltIn.C_PRIME;
 import static xi.ast.BuiltIn.I;
 import static xi.ast.BuiltIn.K;
 import static xi.ast.BuiltIn.S;
 import static xi.ast.BuiltIn.S_PRIME;
 
 import java.util.Deque;
 import java.util.Set;
 
 import xi.util.Logging;
 
 /**
  * SASL function application.
  * 
  * @author Leo
  * @author Joschi
  * 
  */
 public final class App extends Expr {
 
     /** Pattern for matching applications of B. */
     private static final Expr[] B_PAT = { B, null, null };
 
     /** Pattern for matching applications of C. */
     private static final Expr[] C_PAT = { C, null, null };
 
     /**
      * Creates an application-node for the given expressions.
      * 
      * @param e
      *            First expression
      * @param es
      *            further expressions
      * @return The application-node.
      */
     public static Expr create(final Expr e, final Expr... es) {
         Expr res = e;
         for (final Expr x : es) {
             res = new App(res, x);
         }
         return res;
     }
 
     /**
      * Constructor taking a function and an argument.
      * 
      * @param f
      *            function expression
      * @param x
      *            argument expression
      */
     private App(final Expr f, final Expr x) {
         super(f, x);
     }
 
     @Override
     public String toString() {
         return expr[0] + " "
                 + (expr[1] instanceof App ? "(" + expr[1] + ")" : expr[1]);
     }
 
     /**
      * Getter for the function.
      * 
      * @return the function expression
      */
     public Expr getLeft() {
         return expr[0];
     }
 
     /**
      * Getter for the value.
      * 
      * @return the value expression
      */
     public Expr getRight() {
         return expr[1];
     }
 
     @Override
     protected Expr unLambda(final Name n) {
         final Expr left = getLeft(), right = getRight();
 
         if (n == null) {
             final Expr l = left.unLambda(), r = right.unLambda();
 
             // C I x f => f x
             final Expr[] c = C_PAT.clone();
             if (l.match(c) && c[1] == I) {
                 Logging.getLogger(getClass()).fine(
                         "Optimizing: " + this + "  ==>  " + r + " " + c[2]);
                 return App.create(r, c[2]);
             }
 
             return App.create(l, r);
         }
 
         final boolean leftFree = left.hasFree(n);
         final boolean rightFree = right.hasFree(n);
 
         if (DISABLE_BC) {
             // (\x . a b) => K (a b), if x is free in neither a nor b
             if (!leftFree && !rightFree) {
                 return K.app(this);
             }
 
             // (\x . f x) => f, if x isn't free in f
             if (!leftFree && rightFree && right.equals(n)) {
                 return left;
             }
 
             // (\x . a b) => S a b, if x is free in both a and b
             // (\x . a b) => S (K a) b, if x is free in b
             // (\x . a b) => S a (K b), if x is free in a
             // (\x . a b) => S (K a) (K b), if x is free in neither a nor b
             // does not occur => K (a b)
             return S.app(!leftFree ? K.app(left) : left.unLambda(n),
                     !rightFree ? K.app(right) : right.unLambda(n));
         }
 
         if (leftFree) {
             final Expr l = left.unLambda(n);
             if (rightFree) {
 
                 final Expr r = right.unLambda(n);
 
                 final Expr[] b = B_PAT.clone();
                 if (l.match(b)) {
                     return S_PRIME.app(b[1], b[2], r);
                 }
 
                 // (\x . a b) => S a b, if x is free both a and b
                 return S.app(l, r);
             }
 
             final Expr[] b = B_PAT.clone();
             if (l.match(b)) {
                 return C_PRIME.app(b[1], b[2], right);
             }
 
             // (\x . a b) => C a b, if x is free in a, but not in b
             return C.app(l, right);
 
         }
         if (rightFree) {
             if (right.equals(n)) {
                 // (\x . f x) => f, if x isn't free in f
                 return left;
             }
 
             final Expr r = right.unLambda(n);
             final Expr[] b = B_PAT.clone();
             if (r.match(b)) {
                 return B_STAR.app(left, b[1], b[2]);
             }
 
             // (\x . a b) => B a b, if x is free in b, but not in a
             return B.app(left, r);
         }
         // (\x . a b) => K (a b), if x is free in neither a nor b
         return K.app(this);
     }
 
     @Override
     protected void freeVars(final Deque<Name> bound, final Set<Name> free) {
         getLeft().freeVars(bound, free);
         getRight().freeVars(bound, free);
     }
 
     @Override
     public int numOfUses(final Name n) {
         return expr[0].numOfUses(n) + expr[1].numOfUses(n);
     }
 
     @Override
     public Expr inline(final Name name, final Expr val) {
         expr[0] = expr[0].inline(name, val);
         expr[1] = expr[1].inline(name, val);
         return this;
     }
 
     @Override
     public boolean match(final Expr[] pat, final int l, final int r) {
        if (r - l == 1 && pat[l] == null) {
            pat[l] = this;
            return true;
        }
         if (r - l < 2) {
             return false;
         }
        final boolean fst = expr[0].match(pat, l, r - 1);
        return fst && expr[1].match(pat, r - 1, r);
     }
 
     @Override
     public boolean equals(final Object other) {
         if (!(other instanceof App)) {
             return false;
         }
         final App o = (App) other;
         return expr[0].equals(o.expr[0]) && expr[1].equals(o.expr[1]);
     }
 
     @Override
     public int hashCode() {
         return 31 * expr[0].hashCode() + expr[1].hashCode();
     }
 }

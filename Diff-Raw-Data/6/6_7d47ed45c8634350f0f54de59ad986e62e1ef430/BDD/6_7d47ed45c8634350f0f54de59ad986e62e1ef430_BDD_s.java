 package org.sf.javabdd;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.Random;
 import java.util.Set;
 import java.io.PrintStream;
 
 /**
  * <p>Binary Decision Diagrams (BDDs) are used for efficient computation of many
  * common problems. This is done by giving a compact representation and a set of
  * efficient operations on boolean functions f: {0,1}^n --> {0,1}.</p>
  * 
  * <p>Use an implementation of BDDFactory to create BDD objects.</p>
  *
  * <p>Some methods, namely <tt>exist()</tt>, <tt>forall()</tt>, <tt>unique()</tt>, 
  * <tt>relprod()</tt>, <tt>applyAll()</tt>, <tt>applyEx()</tt>, <tt>applyUni()</tt>, 
  * and <tt>satCount()</tt> take a 'set of variables' argument that is also of type BDD.
  * Those BDDs must be a boolean function that represents the all-true minterm
  * of the BDD variables of interest.  They only serve to identify the set of
  * variables of interest, however.  For example, for a given BDDDomain, a BDD var set
  * representing all BDD variables of that domain can be obtained 
  * by calling <tt>BDDDomain.set()</tt>.</p>
  * 
  * @see org.sf.javabdd.BDDFactory
  * @see org.sf.javabdd.BDDDomain#set()
  * 
  * @author John Whaley
 * @version $Id: BDD.java,v 1.35 2004/08/12 21:23:40 joewhaley Exp $
  */
 public abstract class BDD {
 
     /**
      * <p>Returns the factory that created this BDD.</p>
      * 
      * @return factory that created this BDD
      */
     public abstract BDDFactory getFactory();
 
     /**
      * <p>Returns true if this BDD is the zero (false) BDD.</p>
      * 
      * @return true if this BDD is the zero (false) BDD
      */
     public abstract boolean isZero();
     
     /**
      * <p>Returns true if this BDD is the one (true) BDD.</p>
      * 
      * @return true if this BDD is the one (true) BDD
      */
     public abstract boolean isOne();
     
     /**
      * <p>Gets the variable labeling the BDD.</p>
      * 
      * <p>Compare to bdd_var.</p>
      * 
      * @return the index of the variable labeling the BDD
      */
     public abstract int var();
 
     /**
      * <p>Gets the level of this BDD.</p>
      * 
      * <p>Compare to LEVEL() macro.</p>
      * 
      * @return the level of this BDD
      */
     public int level() {
         return getFactory().var2Level(var());
     }
 
     /**
      * <p>Gets the true branch of this BDD.</p>
      * 
      * <p>Compare to bdd_high.</p>
      * 
      * @return true branch of this BDD
      */
     public abstract BDD high();
 
     /**
      * <p>Gets the false branch of this BDD.</p>
      * 
      * <p>Compare to bdd_low.</p>
      * 
      * @return false branch of this BDD
      */
     public abstract BDD low();
 
     /**
      * <p>Identity function.  Returns a copy of this BDD.  Use as the argument to
      * the "xxxWith" style operators when you do not want to have the argument
      * consumed.</p>
      * 
      * <p>Compare to bdd_addref.</p>
      * 
      * @return copy of this BDD
      */
     public abstract BDD id();
 
     /**
      * <p>Negates this BDD by exchanging all references to the zero-terminal with
      * references to the one-terminal and vice-versa.</p>
      * 
      * <p>Compare to bdd_not.</p>
      * 
      * @return the negated BDD
      */
     public abstract BDD not();
 
     /**
      * <p>Returns the logical 'and' of two BDDs.  This is a shortcut for calling
      * "apply" with the "and" operator.</p>
      * 
      * <p>Compare to bdd_and.</p>
      * 
      * @param that BDD to 'and' with
      * @return the logical 'and' of two BDDs
      */
     public BDD and(BDD that) {
         return this.apply(that, BDDFactory.and);
     }
 
     /**
      * <p>Makes this BDD be the logical 'and' of two BDDs.  The "that" BDD is
      * consumed, and can no longer be used.  This is a shortcut for calling
      * "applyWith" with the "and" operator.</p>
      * 
      * <p>Compare to bdd_and and bdd_delref.</p>
      * 
      * @param that the BDD to 'and' with
      */
     public BDD andWith(BDD that) {
         return this.applyWith(that, BDDFactory.and);
     }
 
     /**
      * <p>Returns the logical 'or' of two BDDs.  This is a shortcut for calling
      * "apply" with the "or" operator.</p>
      * 
      * <p>Compare to bdd_or.</p>
      * 
      * @param that the BDD to 'or' with
      * @return the logical 'or' of two BDDs
      */
     public BDD or(BDD that) {
         return this.apply(that, BDDFactory.or);
     }
 
     /**
      * <p>Makes this BDD be the logical 'or' of two BDDs.  The "that" BDD is
      * consumed, and can no longer be used.  This is a shortcut for calling
      * "applyWith" with the "or" operator.</p>
      * 
      * <p>Compare to bdd_or and bdd_delref.</p>
      * 
      * @param that the BDD to 'or' with
      */
     public BDD orWith(BDD that) {
         return this.applyWith(that, BDDFactory.or);
     }
 
     /**
      * <p>Returns the logical 'xor' of two BDDs.  This is a shortcut for calling
      * "apply" with the "xor" operator.</p>
      * 
      * <p>Compare to bdd_xor.</p>
      * 
      * @param that the BDD to 'xor' with
      * @return the logical 'xor' of two BDDs
      */
     public BDD xor(BDD that) {
         return this.apply(that, BDDFactory.xor);
     }
     
     /**
      * <p>Makes this BDD be the logical 'xor' of two BDDs.  The "that" BDD is
      * consumed, and can no longer be used.  This is a shortcut for calling
      * "applyWith" with the "xor" operator.</p>
      * 
      * <p>Compare to bdd_xor and bdd_delref.</p>
      * 
      * @param that the BDD to 'xor' with
      */
     public BDD xorWith(BDD that) {
         return this.applyWith(that, BDDFactory.xor);
     }
 
     /**
      * <p>Returns the logical 'implication' of two BDDs.  This is a shortcut for
      * calling "apply" with the "imp" operator.</p>
      * 
      * <p>Compare to bdd_imp.</p>
      * 
      * @param that the BDD to 'implication' with
      * @return the logical 'implication' of two BDDs
      */
     public BDD imp(BDD that) {
         return this.apply(that, BDDFactory.imp);
     }
     
     /**
      * <p>Makes this BDD be the logical 'implication' of two BDDs.  The "that" BDD
      * is consumed, and can no longer be used.  This is a shortcut for calling
      * "applyWith" with the "imp" operator.</p>
      * 
      * <p>Compare to bdd_imp and bdd_delref.</p>
      * 
      * @param that the BDD to 'implication' with
      */
     public BDD impWith(BDD that) {
         return this.applyWith(that, BDDFactory.imp);
     }
 
     /**
      * <p>Returns the logical 'bi-implication' of two BDDs.  This is a shortcut for
      * calling "apply" with the "biimp" operator.</p>
      * 
      * <p>Compare to bdd_biimp.</p>
      * 
      * @param that the BDD to 'bi-implication' with
      * @return the logical 'bi-implication' of two BDDs
      */
     public BDD biimp(BDD that) {
         return this.apply(that, BDDFactory.biimp);
     }
     
     /**
      * <p>Makes this BDD be the logical 'bi-implication' of two BDDs.  The "that"
      * BDD is consumed, and can no longer be used.  This is a shortcut for
      * calling "applyWith" with the "biimp" operator.</p>
      * 
      * <p>Compare to bdd_biimp and bdd_delref.</p>
      * 
      * @param that the BDD to 'bi-implication' with
      */
     public BDD biimpWith(BDD that) {
         return this.applyWith(that, BDDFactory.biimp);
     }
 
     /**
      * <p>if-then-else operator.</p>
      * 
      * <p>Compare to bdd_ite.</p>
      * 
      * @param thenBDD the 'then' BDD
      * @param elseBDD the 'else' BDD
      * @return the result of the if-then-else operator on the three BDDs
      */
     public abstract BDD ite(BDD thenBDD, BDD elseBDD);
     
     /**
      * <p>Relational product.  Calculates the relational product of the two BDDs as
      * this AND that with the variables in var quantified out afterwards.
      * Identical to applyEx(that, and, var).</p>
      * 
      * <p>Compare to bdd_relprod.</p>
      * 
      * @param that the BDD to 'and' with
      * @param var the BDD to existentially quantify with
      * @return the result of the relational product
      * @see org.sf.javabdd.BDDDomain#set()
      */
     public abstract BDD relprod(BDD that, BDD var);
     
     /**
      * <p>Functional composition.  Substitutes the variable var with the BDD that
      * in this BDD: result = f[g/var].</p>
      * 
      * <p>Compare to bdd_compose.</p>
      * 
      * @param g the function to use to replace
      * @param var the variable number to replace
      * @return the result of the functional composition
      */
     public abstract BDD compose(BDD g, int var);
 
     /**
      * <p>Simultaneous functional composition.  Uses the pairs of variables and
      * BDDs in pair to make the simultaneous substitution: f [g1/V1, ... gn/Vn].
      * In this way one or more BDDs may be substituted in one step. The BDDs in
      * pair may depend on the variables they are substituting.  BDD.compose()
      * may be used instead of BDD.replace() but is not as efficient when gi is a
      * single variable, the same applies to BDD.restrict().  Note that
      * simultaneous substitution is not necessarily the same as repeated
      * substitution.</p>
      * 
      * <p>Compare to bdd_veccompose.</p>
      * 
      * @param pair the pairing of variables to functions
      * @return BDD the result of the simultaneous functional composition
      */
     public abstract BDD veccompose(BDDPairing pair);
 
     /**
      * <p>Generalized cofactor.  Computes the generalized cofactor of this BDD with
      * respect to the given BDD.</p>
      * 
      * <p>Compare to bdd_constrain.</p>
      * 
      * @param that the BDD with which to compute the generalized cofactor
      * @return the result of the generalized cofactor
      */
     public abstract BDD constrain(BDD that);
 
     /**
      * <p>Existential quantification of variables.  Removes all occurrences of this
      * BDD in variables in the set var by existential quantification.</p>
      * 
      * <p>Compare to bdd_exist.</p>
      *
      * @param var BDD containing the variables to be existentially quantified
      * @return the result of the existential quantification
      * @see org.sf.javabdd.BDDDomain#set()
      */
     public abstract BDD exist(BDD var);
 
     /**
      * <p>Universal quantification of variables.  Removes all occurrences of this
      * BDD in variables in the set var by universal quantification.</p>
      * 
      * <p>Compare to bdd_forall.</p>
      * 
      * @param var BDD containing the variables to be universally quantified
      * @return the result of the universal quantification
      * @see org.sf.javabdd.BDDDomain#set()
      */
     public abstract BDD forAll(BDD var);
 
     /**
      * <p>Unique quantification of variables.  This type of quantification uses a
      * XOR operator instead of an OR operator as in the existential
      * quantification.</p>
      * 
      * <p>Compare to bdd_unique.</p>
      * 
      * @param var BDD containing the variables to be uniquely quantified
      * @return the result of the unique quantification
      * @see org.sf.javabdd.BDDDomain#set()
      */
     public abstract BDD unique(BDD var);
     
     /**
      * <p>Restrict a set of variables to constant values.  Restricts the variables
      * in this BDD to constant true if they are included in their positive form
      * in var, and constant false if they are included in their negative form.</p>
      * 
      * <p>Compare to bdd_restrict.</p>
      * 
      * @param var BDD containing the variables to be restricted
      * @return the result of the restrict operation
      * @see org.sf.javabdd.BDDDomain#set()
      */
     public abstract BDD restrict(BDD var);
 
     /**
      * <p>Mutates this BDD to restrict a set of variables to constant values.
      * Restricts the variables in this BDD to constant true if they are included
      * in their positive form in var, and constant false if they are included in
      * their negative form.  The "that" BDD is consumed, and can no longer be used.</p>
      * 
      * <p>Compare to bdd_restrict and bdd_delref.</p>
      * 
      * @param var BDD containing the variables to be restricted
      * @see org.sf.javabdd.BDDDomain#set()
      */
     public abstract BDD restrictWith(BDD var);
 
     /**
      * <p>Coudert and Madre's restrict function.  Tries to simplify the BDD f by
      * restricting it to the domain covered by d.  No checks are done to see if
      * the result is actually smaller than the input.  This can be done by the
      * user with a call to nodeCount().</p>
      * 
      * <p>Compare to bdd_simplify.</p>
      * 
      * @param d BDD containing the variables in the domain
      * @return the result of the simplify operation
      */
     public abstract BDD simplify(BDD d);
 
     /**
      * <p>Returns the variable support of this BDD.  The support is all the
      * variables that this BDD depends on.</p>
      * 
      * <p>Compare to bdd_support.</p>
      * 
      * @return the variable support of this BDD
      */
     public abstract BDD support();
 
     /**
      * <p>Returns the result of applying the binary operator opr to the two BDDs.</p>
      * 
      * <p>Compare to bdd_apply.</p>
      * 
      * @param that the BDD to apply the operator on
      * @param opr the operator to apply
      * @return the result of applying the operator
      */
     public abstract BDD apply(BDD that, BDDFactory.BDDOp opr);
 
     /**
      * <p>Makes this BDD be the result of the binary operator opr of two BDDs.  The
      * "that" BDD is consumed, and can no longer be used.  Attempting to use the
      * passed in BDD again will result in an exception being thrown.</p>
      * 
      * <p>Compare to bdd_apply and bdd_delref.</p>
      * 
      * @param that the BDD to apply the operator on
      * @param opr the operator to apply
      */
     public abstract BDD applyWith(BDD that, BDDFactory.BDDOp opr);
     
     /**
      * <p>Applies the binary operator opr to two BDDs and then performs a universal
      * quantification of the variables from the variable set var.</p>
      * 
      * <p>Compare to bdd_appall.</p>
      * 
      * @param that the BDD to apply the operator on
      * @param opr the operator to apply
      * @param var BDD containing the variables to quantify
      * @return the result
      * @see org.sf.javabdd.BDDDomain#set()
      */
     public abstract BDD applyAll(BDD that, BDDFactory.BDDOp opr, BDD var);
 
     /**
      * <p>Applies the binary operator opr to two BDDs and then performs an
      * existential quantification of the variables from the variable set var.</p>
      * 
      * <p>Compare to bdd_appex.</p>
      * 
      * @param that the BDD to apply the operator on
      * @param opr the operator to apply
      * @param var BDD containing the variables to quantify
      * @return the result
      * @see org.sf.javabdd.BDDDomain#set()
      */
     public abstract BDD applyEx(BDD that, BDDFactory.BDDOp opr, BDD var);
 
     /**
      * <p>Applies the binary operator opr to two BDDs and then performs a unique
      * quantification of the variables from the variable set var.</p>
      * 
      * <p>Compare to bdd_appuni.</p>
      * 
      * @param that the BDD to apply the operator on
      * @param opr the operator to apply
      * @param var BDD containing the variables to quantify
      * @return the result
      * @see org.sf.javabdd.BDDDomain#set()
      */
     public abstract BDD applyUni(BDD that, BDDFactory.BDDOp opr, BDD var);
 
     /**
      * <p>Finds one satisfying variable assignment.  Finds a BDD with at most one
      * variable at each levels.  The new BDD implies this BDD and is not false
      * unless this BDD is false.</p>
      * 
      * <p>Compare to bdd_satone.</p>
      * 
      * @return one satisfying variable assignment
      */
     public abstract BDD satOne();
 
     /**
      * <p>Finds one satisfying variable assignment.  Finds a BDD with exactly one
      * variable at all levels.  The new BDD implies this BDD and is not false
      * unless this BDD is false.</p>
      * 
      * <p>Compare to bdd_fullsatone.</p>
      * 
      * @return one satisfying variable assignment
      */
     public abstract BDD fullSatOne();
 
     /**
      * <p>Finds one satisfying variable assignment.  Finds a minterm in this BDD.
      * The var argument is a set of variables that must be mentioned in the
      * result.  The polarity of these variables in the result -- in case they
      * are undefined in this BDD, are defined by the pol parameter.  If pol is
      * the false BDD then all variables will be in negative form, and otherwise
      * they will be in positive form.</p>
      * 
      * <p>Compare to bdd_satoneset.</p>
      * 
      * @param var BDD containing the set of variables that must be mentioned in the result
      * @param pol the polarity of the result
      * @return one satisfying variable assignment
      */
     public abstract BDD satOne(BDD var, BDD pol);
 
     /**
      * <p>Finds all satisfying variable assignments.</p>
      * 
      * <p>Compare to bdd_allsat.</p>
      * 
      * @return all satisfying variable assignments
      */
     public abstract List allsat();
 
     /**
      * <p>Scans this BDD to find all occurrences of BDD variables and returns an
      * array that contains the indices of the possible found BDD variables.</p>
      * 
      * <p>Compare to bdd_scanset.</p>
      * 
      * @return int[] containing indices of the possible found BDD variables
      */
     public int[] scanSet() {
         if (isOne() || isZero()) {
             return null;
         }
         
         int num = 0;
         BDD n = this.id();
         do {
             num++;
             BDD n2 = n.high();
             n.free(); n = n2;
         } while (!n.isZero() && !n.isOne());
 
         int[] varset = new int[num];
    
         num = 0;
         n = this.id();
         do {
             varset[num++] = n.var();
             BDD n2 = n.high();
             n.free(); n = n2;
         } while (!n.isZero() && !n.isOne());
         
         return varset;
     }
 
     /**
      * <p>Scans this BDD and copies the stored variables into a integer array of
      * variable numbers.  The numbers returned are guaranteed to be in
      * ascending order.</p>
      * 
      * <p>Compare to fdd_scanset.</p>
      * 
      * @return int[]
      */
     public int[] scanSetDomains() {
         int[] fv;
         int[] varset;
         int fn;
         int num, n, m, i;
 
         fv = this.scanSet();
         if (fv == null)
             return null;
         fn = fv.length;
 
         BDDFactory factory = getFactory();
 
         for (n = 0, num = 0; n < factory.numberOfDomains(); n++) {
             BDDDomain dom = factory.getDomain(n);
             int[] ivar = dom.vars();
             boolean found = false;
             for (m = 0; m < dom.varNum() && !found; m++) {
                 for (i = 0; i < fn && !found; i++) {
                     if (ivar[m] == fv[i]) {
                         num++;
                         found = true;
                     }
                 }
             }
         }
 
         varset = new int[num];
 
         for (n = 0, num = 0; n < factory.numberOfDomains(); n++) {
             BDDDomain dom = factory.getDomain(n);
             int[] ivar = dom.vars();
             boolean found = false;
             for (m = 0; m < dom.varNum() && !found; m++) {
                 for (i = 0; i < fn && !found; i++) {
                     if (ivar[m] == fv[i]) {
                         varset[num++] = n;
                         found = true;
                     }
                 }
             }
         }
 
         return varset;
     }
     
     /**
      * <p>Finds one satisfying assignment of the domain d in this BDD and returns
      * that value.</p>
      * 
      * <p>Compare to fdd_scanvar.</p>
      * 
      * @param d domain to scan
      * @return one satisfying assignment for that domain
      */
     public long scanVar(BDDDomain d) {
         if (this.isZero())
            return -1;
         long[] allvar = this.scanAllVar();
         long res = allvar[d.getIndex()];
         return res;
     }
     
     /**
      * <p>Finds one satisfying assignment in this BDD of all the defined FDD
      * variables.  Each value is stored in an array which is returned.  The size
      * of this array is exactly the number of FDD variables defined.</p>
      * 
      * <p>Compare to fdd_scanallvar.</p>
      * 
      * @return int[] containing one satisfying assignment of all the defined domains
      */
     public long[] scanAllVar() {
         int n;
         boolean[] store;
         long[] res;
 
         if (this.isZero())
             return null;
 
         BDDFactory factory = getFactory();
 
         int bddvarnum = factory.varNum();
         store = new boolean[bddvarnum];
 
         BDD p = this.id();
         while (!p.isOne() && !p.isZero()) {
             BDD lo = p.low();
             if (!lo.isZero()) {
                 store[p.var()] = false;
                 BDD p2 = p.low();
                 p.free(); p = p2;
             } else {
                 store[p.var()] = true;
                 BDD p2 = p.high();
                 p.free(); p = p2;
             }
             lo.free();
         }
 
         int fdvarnum = factory.numberOfDomains();
         res = new long[fdvarnum];
 
         for (n = 0; n < fdvarnum; n++) {
             BDDDomain dom = factory.getDomain(n);
             int[] ivar = dom.vars();
 
             long val = 0;
             for (int m = dom.varNum() - 1; m >= 0; m--)
                 if (store[ivar[m]])
                     val = val * 2 + 1;
                 else
                     val = val * 2;
 
             res[n] = val;
         }
 
         return res;
     }
 
     static int[] varset2levels(BDD r) {
         int size = 0;
         BDD p = r.id();
         while (!p.isOne() && !p.isZero()) {
             ++size;
             BDD p2 = p.high();
             p.free();
             p = p2;
         }
         p.free();
         int[] result = new int[size];
         size = -1;
         p = r.id();
         while (!p.isOne() && !p.isZero()) {
             result[++size] = p.level();
             BDD p2 = p.high();
             p.free();
             p = p2;
         }
         p.free();
         return result;
     }
     
     /**
      * <p>Returns an iteration of the satisfying assignments of this BDD.  Returns
      * an iteration of minterms.  The var argument is the set of variables that
      * will be mentioned in the result.</p>
      * 
      * @return an iteration of minterms
      */
     public BDDIterator iterator(final BDD var) {
         return new BDDIterator(this, var);
     }
     
     public static class BDDIterator implements Iterator {
         BDDFactory factory;
         int[] levels;
         boolean[] values;
         BDD[] nodes = null;
         boolean more = false;
         
         public BDDIterator(BDD dis, BDD var){
             factory = dis.getFactory();
             if (!dis.isZero()) {
                 levels = varset2levels(var);
                 values = new boolean[levels.length];
                 nodes = new BDD[levels.length];
                 fillInSatisfyingAssignment(dis.id(), 0);
                 more = true;
             }
         }
         
         void fillInSatisfyingAssignment(BDD node, int i) {
             while (!node.isOne() && !node.isZero()) {
                 int v = node.level();
                 
                 // Mark skipped variables as dont-cares.
                 int j = i;
                 while (j < levels.length && levels[j] != v) {
                     if (nodes[j] != null)
                         throw new InternalError("nodes["+j+"] should be null");
                     ++j;
                 }
                 if (j == levels.length)
                     throw new BDDException("BDD contains variable not in iteration set: "+factory.level2Var(v));
                 i = j;
                 
                 // Put node in table.
                 nodes[i] = node;
                 
                 // Choose zero edge.
                 BDD node2 = node.low();
                 if (node2.isZero()) {
                     // Zero edge is F.  Choose one edge instead.
                     node2.free();
                     values[i] = true;
                     node2 = node.high();
                 }
                 node = node2;
                 ++i;
             }
         }
         
         boolean findNextSatisfyingAssignment() {
             int i = nodes.length - 1;
             for (;;) {
                 if (i < 0) return false;
                 if (nodes[i] != null) {
                     if (!values[i]) {
                         // We already tried zero, try a one here now.
                         BDD hi = nodes[i].high();
                         if (!hi.isZero()) {
                             values[i] = true;
                             fillInSatisfyingAssignment(hi, i+1);
                             return true;
                         } else {
                             // Leads to zero, no satisfying assignments.
                             // Fallthrough.
                         }
                     }
                     // We already tried both zero and one.
                     nodes[i].free();
                     nodes[i] = null;
                     values[i] = false;
                     // Fallthrough: Go to next bit.
                 } else {
                     // This is a dont-care bit, skip it.
                 }
                 --i;
             }
         }
         
         void increment() {
             more = false;
             boolean carry = true;
             for (int i = levels.length - 1; i >= 0; --i) {
                 boolean val = values[i];
                 if (nodes[i] == null) {
                     if (carry) {
                         values[i] = !val;
                         more |= !val;
                         carry = val;
                     }
                 }
             }
         }
         
         BDD buildAndIncrement() {
             more = false;
             BDD b = factory.one();
             boolean carry = true;
             for (int i = levels.length - 1; i >= 0; --i) {
                 int level = levels[i];
                 int var = factory.level2Var(level);
                 boolean val = values[i];
                 if (nodes[i] == null) {
                     if (carry) {
                         values[i] = !val;
                         more |= !val;
                         carry = val;
                     }
                 }
                 BDD v = val ? factory.ithVar(var) : factory.nithVar(var);
                 b.andWith(v);
             }
             return b;
         }
         
         void free() {
             for (int i = levels.length - 1; i >= 0; --i) {
                 if (nodes[i] != null) {
                     nodes[i].free();
                     nodes[i] = null;
                 }
             }
             nodes = null;
         }
         
         public Object next() {
             BDD b;
             if (more) {
                 b = buildAndIncrement();
             } else {
                 throw new NoSuchElementException();
             }
             if (!more) {
                 more = findNextSatisfyingAssignment();
                 if (!more) {
                     free();
                 }
             }
             return b;
         }
         
         public boolean hasNext() {
             return nodes != null;
         }
         
         public void remove() {
             throw new UnsupportedOperationException();
         }
         
         public boolean isDontCare(int var) {
             if (levels == null)
                 throw new BDDException(); 
             int level = factory.var2Level(var);
             int i = Arrays.binarySearch(levels, level);
             if (i < 0)
                 throw new BDDException("var "+var+" not in iteration set"); 
             return nodes[i] == null;
         }
         
         public boolean isDontCare(BDDDomain d) {
             int[] vars = d.vars();
             for (int i = 0; i < vars.length; ++i) {
                 if (!isDontCare(vars[i])) return false;
             }
             return true;
         }
         
         void fastForward0(int var) {
             if (levels == null)
                 throw new BDDException(); 
             int level = factory.var2Level(var);
             int i = Arrays.binarySearch(levels, level);
             if (i < 0)
                 throw new BDDException(); 
             if (nodes[i] != null)
                 throw new BDDException();
             values[i] = true;
         }
         
         public void fastForward(int var) {
             fastForward0(var);
             //increment();
         }
         
         public void skipDontCare(BDDDomain d) {
             int[] vars = d.vars();
             for (int i = 0; i < vars.length; ++i) {
                 fastForward0(vars[i]);
             }
             //increment();
         }
         
     }
     
     /**
      * <p>Returns an iteration of the satisfying assignments of this BDD.  Returns
      * an iteration of minterms.  The var argument is a set of variables that
      * must be mentioned in the result.</p>
      * 
      * @return an iteration of minterms
      */
     public Iterator iterator2(final BDD var) {
         return new Iterator() {
 
             BDD b = null;
             BDD zero;
             BDD myVar;
             BDD last = null;
             {
                 if (!isZero()) {
                     b = id();
                     zero = getFactory().zero();
                     myVar = var.id();
                 }
             }
             
             public void remove() {
                 if (last != null) {
                     applyWith(last.id(), BDDFactory.diff);
                     last = null;
                 } else {
                     throw new IllegalStateException();
                 }
             }
 
             public boolean hasNext() {
                 return b != null;
             }
 
             public Object next() {
                 if (b == null)
                     throw new NoSuchElementException();
                 BDD c = b.satOne(myVar, zero);
                 b.applyWith(c.id(), BDDFactory.diff);
                 if (b.isZero()) {
                     myVar.free(); myVar = null;
                     b.free(); b = null;
                     zero.free(); zero = null;
                 }
                 return last = c;
             }
                 
         };
     }
     
     /**
      * <p>Returns a BDD where all variables are replaced with the variables
      * defined by pair.  Each entry in pair consists of a old and a new variable.
      * Whenever the old variable is found in this BDD then a new node with
      * the new variable is inserted instead.</p>
      * 
      * <p>Compare to bdd_replace.</p>
      * 
      * @param pair pairing of variables to the BDDs that replace those variables
      * @return result of replace
      */
     public abstract BDD replace(BDDPairing pair);
     
     /**
      * <p>Replaces all variables in this BDD with the variables defined by pair.
      * Each entry in pair consists of a old and a new variable.  Whenever the
      * old variable is found in this BDD then a new node with the new variable
      * is inserted instead.  Mutates the current BDD.</p>
      * 
      * <p>Compare to bdd_replace and bdd_delref.</p>
      * 
      * @param pair pairing of variables to the BDDs that replace those variables
      */
     public abstract BDD replaceWith(BDDPairing pair);
 
     /**
      * <p>Prints the set of truth assignments specified by this BDD.</p>
      * 
      * <p>Compare to bdd_printset.</p>
      */
     public void printSet() {
         System.out.println(this.toString());
     }
 
     /**
      * <p>Prints this BDD using a set notation as in printSet() but with the index
      * of the finite domain blocks included instead of the BDD variables.</p>
      * 
      * <p>Compare to fdd_printset.</p>
      */
     public void printSetWithDomains() {
         System.out.println(toStringWithDomains());
     }
     
     /**
      * <p>Prints this BDD in dot graph notation.</p>
      * 
      * <p>Compare to bdd_printdot.</p>
      */
     public void printDot() {
         PrintStream out = System.out;
         out.println("digraph G {");
         out.println("0 [shape=box, label=\"0\", style=filled, shape=box, height=0.3, width=0.3];");
         out.println("1 [shape=box, label=\"1\", style=filled, shape=box, height=0.3, width=0.3];");
 
         boolean[] visited = new boolean[nodeCount()+2];
         visited[0] = true; visited[1] = true;
         HashMap map = new HashMap();
         map.put(getFactory().zero(), new Integer(0));
         map.put(getFactory().one(), new Integer(1));
         printdot_rec(out, 1, visited, map);
         
         for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
             BDD b = (BDD) i.next();
             b.free();
         }
         out.println("}");
     }
 
     int printdot_rec(PrintStream out, int current, boolean[] visited, HashMap map) {
         Integer ri = ((Integer) map.get(this));
         if (ri == null) {
             map.put(this.id(), ri = new Integer(++current));
         }
         int r = ri.intValue();
         if (visited[r])
             return current;
         visited[r] = true;
        
         // TODO: support labelling of vars.
         out.println(r+" [label=\""+this.var()+"\"];");
 
         BDD l = this.low(), h = this.high();
         Integer li = ((Integer) map.get(l));
         if (li == null) {
             map.put(l.id(), li = new Integer(++current));
         }
         int low = li.intValue();
         Integer hi = ((Integer) map.get(h));
         if (hi == null) {
             map.put(h.id(), hi = new Integer(++current));
         }
         int high = hi.intValue();
 
         out.println(r+" -> "+low+" [style=dotted];");
         out.println(r+" -> "+high+" [style=filled];");
 
         current = l.printdot_rec(out, current, visited, map);
         l.free();
         current = h.printdot_rec(out, current, visited, map);
         h.free();
         return current;
     }
 
     /**
      * <p>Counts the number of distinct nodes used for this BDD.</p>
      * 
      * <p>Compare to bdd_nodecount.</p>
      * 
      * @return the number of distinct nodes used for this BDD
      */
     public abstract int nodeCount();
     
     /**
      * <p>Counts the number of paths leading to the true terminal.</p>
      * 
      * <p>Compare to bdd_pathcount.</p>
      * 
      * @return the number of paths leading to the true terminal
      */
     public abstract double pathCount();
     
     /**
      * <p>Calculates the number of satisfying variable assignments.</p>
      * 
      * <p>Compare to bdd_satcount.</p>
      * 
      * @return the number of satisfying variable assignments
      */
     public abstract double satCount();
     
     /**
      * <p>Calculates the number of satisfying variable assignments to the variables
      * in the given varset.  ASSUMES THAT THE BDD DOES NOT HAVE ANY ASSIGNMENTS TO
      * VARIABLES THAT ARE NOT IN VARSET.  You will need to quantify out the other
      * variables first.</p>
      * 
      * <p>Compare to bdd_satcountset.</p>
      * 
      * @return the number of satisfying variable assignments
      */
     public double satCount(BDD varset) {
         BDDFactory factory = getFactory();
         double unused = factory.varNum();
 
         if (varset.isZero() || varset.isOne() || isZero()) /* empty set */
             return 0.;
 
         BDD n = varset.id();
         do {
             BDD n2 = n.high();
             n.free(); n = n2;
             unused--;
         } while (!n.isOne() && !n.isZero());
         n.free();
 
         unused = satCount() / Math.pow(2.0, unused);
 
         return unused >= 1.0 ? unused : 1.0;
     }
     
     /**
      * <p>Calculates the logarithm of the number of satisfying variable assignments.</p>
      * 
      * <p>Compare to bdd_satcount.</p>
      * 
      * @return the logarithm of the number of satisfying variable assignments
      */
     public double logSatCount() {
         return Math.log(satCount());
     }
     
     /**
      * <p>Calculates the logarithm of the number of satisfying variable assignments to the
      * variables in the given varset.</p>
      * 
      * <p>Compare to bdd_satcountset.</p>
      * 
      * @return the logarithm of the number of satisfying variable assignments
      */
     public double logSatCount(BDD varset) {
         return Math.log(satCount(varset));
     }
     
     /**
      * <p>Counts the number of times each variable occurs in this BDD.  The
      * result is stored and returned in an integer array where the i'th
      * position stores the number of times the i'th printing variable
      * occurred in the BDD.</p>
      * 
      * <p>Compare to bdd_varprofile.</p>     */
     public abstract int[] varProfile();
     
     /**
      * <p>Returns true if this BDD equals that BDD, false otherwise.</p>
      * 
      * @param that the BDD to compare with
      * @return true iff the two BDDs are equal
      */
     public abstract boolean equals(BDD that);
     
     /* (non-Javadoc)
      * @see java.lang.Object#equals(java.lang.Object)
      */
     public boolean equals(Object o) {
         if (!(o instanceof BDD)) return false;
         return this.equals((BDD) o);
     }
     
     /* (non-Javadoc)
      * @see java.lang.Object#hashCode()
      */
     public abstract int hashCode();
     
     /* (non-Javadoc)
      * @see java.lang.Object#toString()
      */
     public String toString() {
         BDDFactory f = this.getFactory();
         int[] set = new int[f.varNum()];
         StringBuffer sb = new StringBuffer();
         bdd_printset_rec(f, sb, this, set);
         return sb.toString();
     }
     
     private static void bdd_printset_rec(BDDFactory f, StringBuffer sb, BDD r, int[] set) {
         int n;
         boolean first;
 
         if (r.isZero())
             return;
         else if (r.isOne()) {
             sb.append('<');
             first = true;
 
             for (n = 0; n < set.length; n++) {
                 if (set[n] > 0) {
                     if (!first)
                         sb.append(", ");
                     first = false;
                     sb.append(f.level2Var(n));
                     sb.append(':');
                     sb.append((set[n] == 2 ? 1 : 0));
                 }
             }
             sb.append('>');
         } else {
             set[f.var2Level(r.var())] = 1;
             BDD rl = r.low();
             bdd_printset_rec(f, sb, rl, set);
             rl.free();
 
             set[f.var2Level(r.var())] = 2;
             BDD rh = r.high();
             bdd_printset_rec(f, sb, rh, set);
             rh.free();
 
             set[f.var2Level(r.var())] = 0;
         }
     }
     
     /**
      * <p>Returns a string representation of this BDD using the defined domains.</p>
      * 
      * @return string representation of this BDD using the defined domains
      */
     public String toStringWithDomains() {
         return toStringWithDomains(BDDToString.INSTANCE);
     }
     
     /**
      * <p>Returns a string representation of this BDD on the defined domains,
      * using the given BDDToString converter.</p>
      * 
      * @see org.sf.javabdd.BDD.BDDToString
      * 
      * @return string representation of this BDD using the given BDDToString converter
      */
     public String toStringWithDomains(BDDToString ts) {
         if (this.isZero()) return "F";
         if (this.isOne()) return "T";
         
         BDDFactory bdd = getFactory();
         StringBuffer sb = new StringBuffer();
         int[] set = new int[bdd.varNum()];
         fdd_printset_rec(bdd, sb, ts, this, set);
         return sb.toString();
     }
     
     private static class OutputBuffer {
         BDDToString ts;
         StringBuffer sb;
         int domain;
         long lastLow;
         long lastHigh;
         boolean done;
         
         OutputBuffer(BDDToString ts, StringBuffer sb, int domain) {
             this.ts = ts;
             this.sb = sb;
             this.lastHigh = -2L;
             this.domain = domain;
         }
         
         void append(long low, long high) {
             if (low == lastHigh + 1L) {
                 lastHigh = high;
             } else {
                 finish();
                 lastLow = low; lastHigh = high;
             }
         }
         
         StringBuffer finish() {
             if (lastHigh != -2L) {
                 if (done) sb.append('/');
                 if (lastLow == lastHigh)
                     sb.append(ts.elementName(domain, lastHigh));
                 else
                     sb.append(ts.elementNames(domain, lastLow, lastHigh));
                 lastHigh = -2L;
             }
             done = true;
             return sb;
         }
         
         void append(long low) {
             append(low, low);
         }
     }
     
     static void fdd_printset_helper(OutputBuffer sb,
                                     long value, int i,
                                     int[] set, int[] var,
                                     int maxSkip) {
         if (i == maxSkip) {
             //_assert(set[var[i]] == 0);
             long maxValue = value | ((1L << (i+1)) - 1L);
             sb.append(value, maxValue);
             return;
         }
         int val = set[var[i]];
         if (val == 0) {
             long temp = value | (1L << i);
             fdd_printset_helper(sb, temp, i-1, set, var, maxSkip);
         }
         fdd_printset_helper(sb, value, i-1, set, var, maxSkip);
     }
     
     static void fdd_printset_rec(BDDFactory bdd, StringBuffer sb, BDDToString ts, BDD r, int[] set) {
         int fdvarnum = bdd.numberOfDomains();
         
         int n, m, i;
         boolean used = false;
         int[] var;
         boolean first;
         
         if (r.isZero())
             return;
         else if (r.isOne()) {
             sb.append('<');
             first = true;
             
             for (n=0 ; n<fdvarnum ; n++) {
                 used = false;
                 
                 BDDDomain domain_n = bdd.getDomain(n);
                 
                 int[] domain_n_ivar = domain_n.vars();
                 int domain_n_varnum = domain_n_ivar.length;
                 for (m=0 ; m<domain_n_varnum ; m++)
                     if (set[domain_n_ivar[m]] != 0)
                         used = true;
                 
                 if (used) {
                     if (!first)
                         sb.append(", ");
                     first = false;
                     sb.append(domain_n.getName());
                     sb.append(':');
                     
                     var = domain_n_ivar;
                     
                     long pos = 0L;
                     int maxSkip = -1;
                     boolean hasDontCare = false;
                     for (i=0; i<domain_n_varnum; ++i) {
                         int val = set[var[i]];
                         if (val == 0) {
                             hasDontCare = true;
                             if (maxSkip == i-1)
                                 maxSkip = i;
                         }
                     }
                     for (i=domain_n_varnum-1; i>=0; --i) {
                         pos <<= 1;
                         int val = set[var[i]];
                         if (val == 2) {
                             pos |= 1L;
                             //System.out.print('1');
                         } else if (val == 1) {
                             //System.out.print('0');
                         } else {
                             //System.out.print('x');
                         }
                     }
                     //System.out.println();
                     if (!hasDontCare) {
                         sb.append(ts.elementName(n, pos));
                     } else {
                         OutputBuffer ob = new OutputBuffer(ts, sb, n);
                         fdd_printset_helper(ob, pos, domain_n_varnum-1,
                                             set, var, maxSkip);
                         ob.finish();
                     }
                 }
             }
             
             sb.append('>');
         } else {
             set[r.var()] = 1;
             BDD lo = r.low();
             fdd_printset_rec(bdd, sb, ts, lo, set);
             lo.free();
             
             set[r.var()] = 2;
             BDD hi = r.high();
             fdd_printset_rec(bdd, sb, ts, hi, set);
             hi.free();
             
             set[r.var()] = 0;
         }
     }
     
     static boolean[] fdddec2bin(BDDFactory bdd, int var, long val) {
         boolean[] res;
         int n = 0;
         
         res = new boolean[bdd.getDomain(var).varNum()];
         
         while (val > 0) {
             if ((val & 0x1) != 0)
                 res[n] = true;
             val >>= 1;
             n++;
         }
         
         return res;
     }
     
     /**
      * <p>BDDToString is used to specify the printing behavior of BDDs with domains.
      * Subclass this type and pass it as an argument to toStringWithDomains to
      * have the toStringWithDomains function use your domain names and element names,
      * instead of just numbers.</p>
      */
     public static class BDDToString {
         /**
          * <p>Singleton instance that does the default behavior: domains and
          * elements are printed as their numbers.</p>
          */
         public static final BDDToString INSTANCE = new BDDToString();
         
         /**
          * <p>Protected constructor.</p>
          */
         protected BDDToString() { }
         
         /**
          * <p>Given a domain index and an element index, return the element's name.
          * Called by the toStringWithDomains() function.</p>
          * 
          * @param i the domain number
          * @param j the element number
          * @return the string representation of that element
          */
         public String elementName(int i, long j) { return Long.toString(j); }
         
         /**
          * <p>Given a domain index and an inclusive range of element indices,
          * return the names of the elements in that range.
          * Called by the toStringWithDomains() function.</p>
          * 
          * @param i the domain number
          * @param lo the low range of element numbers, inclusive
          * @param hi the high range of element numbers, inclusive
          * @return the string representation of the elements in the range
          */
         public String elementNames(int i, long lo, long hi) { return lo+"-"+hi; }
     }
     
     /**
      * <p>Increases the reference count on a node.  Reference counting is done on
      * externally-referenced nodes only.</p>
      * 
      * <p>Compare to bdd_addref.</p>
      */
     //protected abstract void addRef();
     
     /**
      * <p>Decreases the reference count on a node.  Reference counting is done on
      * externally-referenced nodes only.</p>
      * 
      * <p>Compare to bdd_delref.</p>
      */
     //protected abstract void delRef();
     
     /**
      * <p>Frees this BDD.  Further use of this BDD will result in an exception being thrown.</p>
      */
     public abstract void free();
     
     /**
      * <p>Protected constructor.</p>
      */
     protected BDD() { }
     
     public static void main(String[] args) {
         BDDFactory bdd = BDDFactory.init(1000, 1000);
         int domainSize = 1024;
         bdd.extDomain(new int[] { domainSize, domainSize });
         BDDDomain d = bdd.getDomain(0);
         BDDDomain d2 = bdd.getDomain(1);
         Random r = new Random();
         int times = 1000;
         int combine = 400;
         for (int i = 0; i < times; ++i) {
             int count = r.nextInt(combine);
             BDD b = bdd.zero();
             for (int j = 0; j < count; ++j) {
                 int varNum = r.nextInt(domainSize);
                 BDD c = d.ithVar(varNum); //.andWith(d2.ithVar(domainSize - varNum - 1));
                 b.orWith(c);
             }
             BDD var = d.set();
             Iterator i1 = b.iterator(var);
             Iterator i2 = b.iterator2(var);
             b.free();
             Set s1 = new HashSet();
             Set s2 = new HashSet();
             while (i1.hasNext()) {
                 BDD b1 = (BDD) i1.next();
                 double sc = b1.satCount(var); 
                 if (sc != 1.) {
                     System.out.println("Error, iterator() returned BDD with satcount "+sc+" : "+b1);
                 }
                 s1.add(b1);
             }
             while (i2.hasNext()) {
                 BDD b2 = (BDD) i2.next();
                 double sc = b2.satCount(var); 
                 if (sc != 1.) {
                     System.out.println("Error, iterator2() returned BDD with satcount "+sc+" : "+b2);
                 }
                 s2.add(b2);
             }
             var.free();
             if (!s1.equals(s2)) {
                 Set s1_minus_s2 = new HashSet(s1);
                 s1_minus_s2.removeAll(s2);
                 Set s2_minus_s1 = new HashSet(s2);
                 s2_minus_s1.removeAll(s1);
                 System.out.println("iterator() contains these extras: "+s1_minus_s2);
                 System.out.println("iterator2() contains these extras: "+s2_minus_s1);
             }
             for (Iterator k = s1.iterator(); k.hasNext(); ) {
                 BDD q = (BDD) k.next();
                 q.free();
             }
             for (Iterator k = s2.iterator(); k.hasNext(); ) {
                 BDD q = (BDD) k.next();
                 q.free();
             }
         }
     }
     
 }

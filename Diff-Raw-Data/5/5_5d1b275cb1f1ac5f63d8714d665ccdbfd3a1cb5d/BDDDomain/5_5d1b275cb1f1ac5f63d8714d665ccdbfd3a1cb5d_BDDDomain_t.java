 package org.sf.javabdd;
 
 /**
  * Represents a domain of BDD variables.  This is useful for finite state
  * machines, among other things.
  * 
  * @author John Whaley
 * @version $Id: BDDDomain.java,v 1.11 2003/10/23 16:18:47 joewhaley Exp $
  */
 public abstract class BDDDomain {
 
     /* The name of this domain. */
     protected String name;
     /* The index of this domain. */
     protected int index;
 
     /* The specified domain (0...N-1) */
     protected long realsize;
     /* Variable indices for the variable set */
     protected int[] ivar;
     /* The BDD variable set.  Actually constructed in extDomain(), etc. */
     protected BDD var;
 
     protected BDDDomain(int index, long range) {
         long calcsize = 2L;
         if (range <= 0L  || range > Long.MAX_VALUE/2)
             throw new BDDException();
         this.name = Integer.toString(index);
         this.index = index;
         this.realsize = range;
         int binsize = 1;
         while (calcsize < range) {
            binsize++;
            calcsize <<= 1;
         }
         this.ivar = new int[binsize];
     }
 
     /**
      * Returns the factory that created this domain.
      */
     public abstract BDDFactory getFactory();
 
     /**
      * Sets the name of this domain.
      */
     public void setName(String name) {
         this.name = name;
     }
     
     /**
      * Gets the name of this domain.
      */
     public String getName() {
         return name;
     }
     
     /**
      * Returns the index of this domain.
      */ 
     public int getIndex() {
         return index;
     }
 
     /**
      * Returns what corresponds to a disjunction of all possible values of this
      * domain. This is more efficient than doing ithVar(0) OR ithVar(1) ...
      * explicitly for all values in the domain.
      * 
      * Compare to fdd_domain.
      */ 
     public BDD domain() {
         BDDFactory factory = getFactory();
         
         /* Encode V<=X-1. V is the variables in 'var' and X is the domain size */
         long val = size() - 1L;
         BDD d = factory.one();
         int[] ivar = vars();
         for (int n = 0; n < this.varNum(); n++) {
             if ((val & 0x1L) != 0L)
                 d.orWith(factory.nithVar(ivar[n]));
             else
                 d.andWith(factory.nithVar(ivar[n]));
             val >>= 1;
         }
         return d;
     }
 
     /**
      * Returns the size of the domain for this finite domain block.
      * 
      * Compare to fdd_domainsize.
      */
     public long size() {
         return this.realsize;
     }
     
     public BDD buildAdd(BDDDomain that, long value) {
         if (this.varNum() != that.varNum())
             throw new BDDException();
         return buildAdd(that, this.varNum(), value);
     }
     public BDD buildAdd(BDDDomain that, int bits, long value) {
         if (bits > this.varNum() ||
             bits > that.varNum())
             throw new BDDException();
         
         BDDFactory bdd = getFactory();
         
         if (value == 0L) {
             BDD result = bdd.one();
             int n;
             for (n = 0; n < bits; n++) {
                 BDD b = bdd.ithVar(this.ivar[n]);
                 b.biimpWith(bdd.ithVar(that.ivar[n]));
                 result.andWith(b);
             }
             for ( ; n < that.varNum(); n++) {
                 BDD b = bdd.nithVar(this.ivar[n]);
                 b.andWith(bdd.nithVar(that.ivar[n]));
                 result.andWith(b);
             }
             return result;
         }
 
         int[] vars = new int[bits];
         System.arraycopy(this.ivar, 0, vars, 0, vars.length);
         BDDBitVector y = bdd.buildVector(vars);
         BDDBitVector v = bdd.constantVector(bits, value);
         BDDBitVector z = y.add(v);
         
         int[] thatvars = new int[bits];
         System.arraycopy(that.ivar, 0, thatvars, 0, thatvars.length);
         BDDBitVector x = bdd.buildVector(thatvars);
         BDD result = bdd.one();
         int n;
         for (n = 0; n < x.size(); n++) {
             BDD b = x.bitvec[n].biimp(z.bitvec[n]);
             result.andWith(b);
         }
         for ( ; n < that.varNum(); n++) {
             BDD b = bdd.nithVar(this.ivar[n]);
             b.andWith(bdd.nithVar(that.ivar[n]));
             result.andWith(b);
         }
         x.free(); y.free(); z.free(); v.free();
         return result;
     }
     
     /**
      * Builds a BDD which is true for all the possible assignments to the
      * variable blocks that makes the blocks equal.
      * 
      * Compare to fdd_equals/fdd_equ.
      * 
      * @param that
      * @return BDD
      */
     public BDD buildEquals(BDDDomain that) {
         if (this.size() != that.size()) {
             throw new BDDException();
         }
 
         BDDFactory factory = getFactory();
         BDD e = factory.one();
 
         int[] this_ivar = this.vars();
         int[] that_ivar = that.vars();
 
         for (int n = 0; n < this.varNum(); n++) {
             BDD a = factory.ithVar(this_ivar[n]);
             BDD b = factory.ithVar(that_ivar[n]);
             a.biimpWith(b);
             e.andWith(a);
         }
 
         return e;
     }
     
     /**
      * Returns the variable set that contains the variables used to define this
      * finite domain block.
      * 
      * Compare to fdd_ithset.
      * 
      * @return BDD
      */
     public BDD set() {
         return var.id();
     }
     
     /**
      * Returns the BDD that defines the given value for this finite domain
      * block.
      * 
      * Compare to fdd_ithvar.
      * 
      * @return BDD
      */
     public BDD ithVar(int val) {
         return ithVar((long) val);
     }
     public BDD ithVar(long val) {
         if (val < 0L || val >= this.size()) {
            throw new BDDException(val+" is out of range");
         }
 
         BDDFactory factory = getFactory();
         BDD v = factory.one();
         int[] ivar = this.vars();
         for (int n = 0; n < ivar.length; n++) {
             if ((val & 0x1L) != 0L)
                 v.andWith(factory.ithVar(ivar[n]));
             else
                 v.andWith(factory.nithVar(ivar[n]));
             val >>= 1;
         }
 
         return v;
     }
     
     /**
      * Returns the BDD that defines the given range of values, inclusive,
      * for this finite domain block.
      * 
      * @return BDD
      */
     public BDD varRange(long lo, long hi) {
         if (lo < 0L || hi >= this.size() || lo > hi) {
             throw new BDDException("range <"+lo+", "+hi+"> is invalid");
         }
 
         BDDFactory factory = getFactory();
         BDD result = factory.zero();
         int[] ivar = this.vars();
         while (lo <= hi) {
             long bitmask = 1L << (ivar.length - 1);
             BDD v = factory.one();
             for (int n = ivar.length - 1; ; n--) {
                 long bit = lo & bitmask;
                 if (bit != 0L) {
                     v.andWith(factory.ithVar(ivar[n]));
                 } else {
                     v.andWith(factory.nithVar(ivar[n]));
                 }
                 long mask = bitmask - 1L;
                 if ((lo & mask) == 0L && (lo | mask) <= hi) {
                     lo = (lo | mask) + 1L;
                     break;
                 }
                 bitmask >>= 1;
             }
             result.orWith(v);
         }
         return result;
     }
     
     /**
      * Returns the number of BDD variables used for this finite domain block.
      * 
      * Compare to fdd_varnum.
      * 
      * @return int
      */
     public int varNum() {
         return this.ivar.length;
     }
     
     /**
      * Returns an integer array containing the indices of the BDD variables used
      * to define this finite domain.
      * 
      * Compare to fdd_vars.
      * 
      * @return int[]
      */
     public int[] vars() {
         return this.ivar;
     }
     
     /* (non-Javadoc)
      * @see java.lang.Object#toString()
      */
     public String toString() {
         return getName();
     }
     
 }

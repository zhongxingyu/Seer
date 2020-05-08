 package org.sf.javabdd;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 /**
  * Interface for the creation and manipulation of BDDs.
  * 
  * @see org.sf.javabdd.BDD
  * 
  * @author John Whaley
 * @version $Id: BDDFactory.java,v 1.26 2004/08/02 20:32:59 joewhaley Exp $
  */
 public abstract class BDDFactory {
 
     /** Initializes a BDD factory with the given initial node table size
      * and operation cache size.  Tries to use the "buddy" native library;
      * if it fails, it falls back to the "java" library.
      * 
      * @param nodenum initial node table size
      * @param cachesize operation cache size
      * @return BDD factory object
      */
     public static BDDFactory init(int nodenum, int cachesize) {
         String bddpackage = System.getProperty("bdd", "buddy");
         return init(bddpackage, nodenum, cachesize);
     }
 
     /** Initializes a BDD factory of the given type with the given initial
      * node table size and operation cache size.  The type is a string that
      * can be "buddy", "cudd", "cal", "j", "java", "jdd", "test", "typed", or
      * a name of a class that has an init() method that returns a BDDFactory.
      * If it fails, it falls back to the "j" factory.
      * 
      * @param bddpackage BDD package string identifier
      * @param nodenum initial node table size
      * @param cachesize operation cache size
      * @return BDD factory object
      */
     public static BDDFactory init(String bddpackage, int nodenum, int cachesize) {
         try {
             if (bddpackage.equals("buddy"))
                 return BuDDyFactory.init(nodenum, cachesize);
             if (bddpackage.equals("cudd"))
                 return CUDDFactory.init(nodenum, cachesize);
             if (bddpackage.equals("cal"))
                 return CALFactory.init(nodenum, cachesize);
             if (bddpackage.equals("j") || bddpackage.equals("java"))
                 return JFactory.init(nodenum, cachesize);
             if (bddpackage.equals("jdd"))
                 return JDDFactory.init(nodenum, cachesize);
             if (bddpackage.equals("test"))
                 return TestBDDFactory.init(nodenum, cachesize);
             if (bddpackage.equals("typed"))
                 return TypedBDDFactory.init(nodenum, cachesize);
         } catch (LinkageError _) {
             System.out.println("Could not load BDD package "+bddpackage);
         }
         try {
             Class c = Class.forName(bddpackage);
             Method m = c.getMethod("init", new Class[] { int.class, int.class });
             return (BDDFactory) m.invoke(null, new Object[] { new Integer(nodenum), new Integer(cachesize) });
         }
         catch (ClassNotFoundException _) {}
         catch (NoSuchMethodException _) {}
         catch (IllegalAccessException _) {}
         catch (InvocationTargetException _) {}
         // falling back to default java implementation.
         return JFactory.init(nodenum, cachesize);
     }
 
     /**
      * Logical 'and'.
      */
     public static final BDDOp and   = new BDDOp(0, "and");
     /**
      * Logical 'xor'.
      */
     public static final BDDOp xor   = new BDDOp(1, "xor");
     /**
      * Logical 'or'.
      */
     public static final BDDOp or    = new BDDOp(2, "or");
     /**
      * Logical 'nand'.
      */
     public static final BDDOp nand  = new BDDOp(3, "nand");
     /**
      * Logical 'nor'.
      */
     public static final BDDOp nor   = new BDDOp(4, "nor");
     /**
      * Logical 'implication'.
      */
     public static final BDDOp imp   = new BDDOp(5, "imp");
     /**
      * Logical 'bi-implication'.
      */
     public static final BDDOp biimp = new BDDOp(6, "biimp");
     /**
      * Set difference.
      */
     public static final BDDOp diff  = new BDDOp(7, "diff");
     /**
      * Less than.
      */
     public static final BDDOp less  = new BDDOp(8, "less");
     /**
      * Inverse implication.
      */
     public static final BDDOp invimp = new BDDOp(9, "invimp");
 
     /**
      * Enumeration class for binary operations on BDDs.  Use the static
      * fields in BDDFactory to access the different binary operations.
      */
     public static class BDDOp {
         final int id; final String name;
         private BDDOp(int id, String name) {
             this.id = id;
             this.name = name;
         }
         public String toString() {
             return name;
         }
     }
     
     protected BDDFactory() {
         String s = this.getClass().toString();
         s = s.substring(s.lastIndexOf('.')+1);
         System.out.println("Using BDD package: "+s);
     }
     
     /**
      * Get the constant false BDD.
      * 
      * Compare to bdd_false.
      */
     public abstract BDD zero();
     
     /**
      * Get the constant true BDD.
      * 
      * Compare to bdd_true.
      */
     public abstract BDD one();
     
     /**
      * Build a cube from an array of variables.
      * 
      * Compare to bdd_buildcube.
      */
     public BDD buildCube(int value, List/*BDD*/ variables) {
         BDD result = one();
         Iterator i = variables.iterator();
         int z=0;
         while (i.hasNext()) {
             BDD var = (BDD) i.next();
             if ((value & 0x1) != 0)
                 var = var.id();
             else
                 var = var.not();
             result.andWith(var);
             ++z;
             value >>= 1;
         }
         return result;
     }
     
     /**
      * Build a cube from an array of variables.
      * 
      * Compare to bdd_ibuildcube.
      */
     public BDD buildCube(int value, int[] variables) {
         BDD result = one();
         for (int z = 0; z < variables.length; z++, value >>= 1) {
             BDD v;
             if ((value & 0x1) != 0)
                 v = ithVar(variables[variables.length - z - 1]);
             else
                 v = nithVar(variables[variables.length - z - 1]);
             result.andWith(v);
         }
         return result;
     }
     
     /**
      * Builds a BDD variable set from an integer array.  The integer array v
      * holds the variable numbers.  The BDD variable set is represented by a
      * conjunction of all the variables in their positive form.
      * 
      * Compare to bdd_makeset.
      */
     public BDD makeSet(int[] varset) {
         BDD res = one();
         int varnum = varset.length;
         for (int v = varnum-1 ; v>=0 ; v--) {
             res.andWith(ithVar(varset[v]));
         }
         return res;
     }
     
 
     
     /**** STARTUP / SHUTDOWN ****/
     
     /**
      * Compare to bdd_init.
      * 
      * @param nodenum the initial number of BDD nodes
      * @param cachesize the size of caches used by the BDD operators
      */
     protected abstract void initialize(int nodenum, int cachesize);
 
     /**
      * Compare to bdd_isrunning.
      * 
      * @return boolean
      */
     public abstract boolean isInitialized();
 
     /**
      * Resets the BDD package.  This function frees all memory used by the BDD
      * package and resets the package to its initial state.
      * 
      * Compare to bdd_done.
      */
     public abstract void done();
 
     
     
     /**** CACHE/TABLE PARAMETERS ****/
     
     /**
      * Set the maximum available number of BDD nodes.
      * 
      * Compare to bdd_setmaxnodenum.
      * @param size
      */
     public abstract int setMaxNodeNum(int size);
 
     /**
      * Set minimum number of nodes to be reclaimed after a garbage collection.
      * The range of x is 0..100.  The default is 20.
      * 
      * Compare to bdd_setminfreenodes.
      * @param x
      */
     public abstract void setMinFreeNodes(int x);
     
     /**
      * Set maximum number of nodes used to increase node table.
      * 
      * Compare to bdd_setmaxincrease.
      * @param x
      */
     public abstract int setMaxIncrease(int x);
     
     /**
      * Sets the cache ratio for the operator caches.
      * 
      * Compare to bdd_setcacheratio.
      * @param x
      */
     public abstract int setCacheRatio(int x);
     
     
     
     /**** VARIABLE NUMBERS ****/
     
     /**
      * Returns the number of defined variables.
      * 
      * Compare to bdd_varnum.
      */
     public abstract int varNum();
     
     /**
      * Set the number of used BDD variables.  It can be called more than one
      * time, but only to increase the number of variables.
      * 
      * Compare to bdd_setvarnum.
      * 
      * @param num
      */
     public abstract int setVarNum(int num);
     
     /**
      * Add extra BDD variables.  Extends the current number of allocated BDD
      * variables with num extra variables.
      * 
      * Compare to bdd_extvarnum.
      * 
      * @param num
      */
     public int extVarNum(int num) {
         int start = varNum();
         if (num < 0 || num > 0x3FFFFFFF)
            throw new BDDException();
         setVarNum(start+num);
         return start;
     }
     
     /**
      * Returns a BDD representing the I'th variable.  (One node with the
      * children true and false).  The requested variable must be in the
      * (zero-indexed) range defined by setVarNum.
      * 
      * Compare to bdd_ithvar.
      * 
      * @return the I'th variable on success, otherwise the constant false BDD
      */
     public abstract BDD ithVar(int var);
     
     /**
      * Returns a BDD representing the negation of the I'th variable.  (One node
      * with the children false and true).  The requested variable must be in the
      * (zero- indexed) range defined by setVarNum.
      * 
      * Compare to bdd_nithvar.
      * 
      * @return the negated I'th variable on success, otherwise the constant
      * false BDD
      */
     public abstract BDD nithVar(int var);
     
     
     
     /**** INPUT / OUTPUT ****/
     
     /**
      * Prints all used entries in the node table.
      * 
      * Compare to bdd_printall.
      */
     public abstract void printAll();
     
     /**
      * Prints the node table entries used by a BDD.
      * 
      * Compare to bdd_printtable.
      */
     public abstract void printTable(BDD b);
     
     /**
      * Loads a BDD from a file.
      * 
      * Compare to bdd_load.
      */
     public BDD load(String filename) throws IOException {
         BufferedReader r = null;
         try {
             r = new BufferedReader(new FileReader(filename));
             BDD result = load(r);
             return result;
         } finally {
             if (r != null) try { r.close(); } catch (IOException _) { }
         }
     }
     // TODO: error code from bdd_load (?)
     
     public BDD load(BufferedReader ifile) throws IOException {
 
         tokenizer = null;
         
         int lh_nodenum = Integer.parseInt(readNext(ifile));
         int vnum = Integer.parseInt(readNext(ifile));
 
         // Check for constant true / false
         if (lh_nodenum == 0 && vnum == 0) {
             int r = Integer.parseInt(readNext(ifile));
             return r == 0 ? zero() : one();
         }
 
         // Not actually used.
         int[] loadvar2level = new int[vnum];
         for (int n = 0; n < vnum; n++) {
             loadvar2level[n] = Integer.parseInt(readNext(ifile));
         }
 
         if (vnum > varNum())
             setVarNum(vnum);
 
         LoadHash[] lh_table = new LoadHash[lh_nodenum];
         for (int n = 0; n < lh_nodenum; n++) {
             lh_table[n] = new LoadHash();
             lh_table[n].first = -1;
             lh_table[n].next = n + 1;
         }
         lh_table[lh_nodenum - 1].next = -1;
         int lh_freepos = 0;
 
         BDD root = null;
         for (int n = 0; n < lh_nodenum; n++) {
             int key = Integer.parseInt(readNext(ifile));
             int var = Integer.parseInt(readNext(ifile));
             int lowi = Integer.parseInt(readNext(ifile));
             int highi = Integer.parseInt(readNext(ifile));
 
             BDD low, high;
             
             low = loadhash_get(lh_table, lh_nodenum, lowi);
             high = loadhash_get(lh_table, lh_nodenum, highi);
 
             if (low == null || high == null || var < 0)
                 throw new BDDException("Incorrect file format");
 
             BDD b = ithVar(var);
             root = b.ite(high, low);
             b.free();
 
             int hash = key % lh_nodenum;
             int pos = lh_freepos;
 
             lh_freepos = lh_table[pos].next;
             lh_table[pos].next = lh_table[hash].first;
             lh_table[hash].first = pos;
 
             lh_table[pos].key = key;
             lh_table[pos].data = root;
         }
         BDD tmproot = root.id();
         
         for (int n = 0; n < lh_nodenum; n++)
             lh_table[n].data.free();
 
         lh_table = null;
         loadvar2level = null;
 
         return tmproot;
     }
     
     StringTokenizer tokenizer;
     
     String readNext(BufferedReader ifile) throws IOException {
         while (tokenizer == null || !tokenizer.hasMoreTokens()) {
             String s = ifile.readLine();
             if (s == null)
                 throw new BDDException("Incorrect file format");
             tokenizer = new StringTokenizer(s);
         }
         return tokenizer.nextToken();
     }
     
     private static class LoadHash {
         int key;
         BDD data;
         int first;
         int next;
     }
     
     BDD loadhash_get(LoadHash[] lh_table, int lh_nodenum, int key) {
         if (key < 0) return null;
         if (key == 0) return zero();
         if (key == 1) return one();
         
         int hash = lh_table[key % lh_nodenum].first;
 
         while (hash != -1 && lh_table[hash].key != key)
             hash = lh_table[hash].next;
 
         if (hash == -1)
             return null;
         return lh_table[hash].data;
     }
     
     /**
      * Saves a BDD to a file.
      * 
      * Compare to bdd_save.
      */
     public void save(String filename, BDD var) throws IOException {
         BufferedWriter is = null;
         try {
             is = new BufferedWriter(new FileWriter(filename));
             save(is, var);
         } finally {
             if (is != null) try { is.close(); } catch (IOException _) { }
         }
     }
     // TODO: error code from bdd_save (?)
     
     public void save(BufferedWriter out, BDD r) throws IOException {
         if (r.isOne() || r.isZero()) {
             out.write("0 0 " + (r.isOne()?1:0) + "\n");
             return;
         }
 
         out.write(r.nodeCount() + " " + varNum() + "\n");
 
         for (int x = 0; x < varNum(); x++)
             out.write(var2Level(x) + " ");
         out.write("\n");
 
         Map visited = new HashMap();
         save_rec(out, visited, r);
         
         for (Iterator it = visited.keySet().iterator(); it.hasNext(); ) {
             BDD b = (BDD) it.next();
            b.free();
         }
     }
 
     protected int save_rec(BufferedWriter out, Map visited, BDD root) throws IOException {
         if (root.isZero()) {
             root.free();
             return 0;
         }
         if (root.isOne()) {
             root.free();
             return 1;
         }
         Integer i = (Integer) visited.get(root);
         if (i != null) {
             root.free();
             return i.intValue();
         }
         int v = visited.size() + 2;
         visited.put(root, new Integer(v));
         
         BDD l = root.low();
         int lo = save_rec(out, visited, l);
         
         BDD h = root.high();
         int hi = save_rec(out, visited, h);
 
         out.write(v + " ");
         out.write(root.var() + " ");
         out.write(lo + " ");
         out.write(hi + "\n");
         
         return v;
     }
     
     // TODO: bdd_strm_hook, bdd_file_hook, bdd_blockfile_hook
     // TODO: bdd_versionnum, bdd_versionstr
     
     
     
     /**
      * Compare to bdd_level2var.
      */
     public abstract int level2Var(int level);
     
     /**
      * Compare to bdd_var2level.
      */
     public abstract int var2Level(int var);
     
     
     /**** REORDERING ****/
     
     /**
      * Compare to bdd_reorder.
      */
     public abstract void reorder(ReorderMethod m);
     
     /**
      * Enables automatic reordering.  If method is REORDER_NONE then automatic
      * reordering is disabled.
      * 
      * Compare to bdd_autoreorder.
      */
     public abstract void autoReorder(ReorderMethod method);
     
     /**
      * Enables automatic reordering with the given (maximum) number of
      * reorderings. If method is REORDER_NONE then automatic reordering is
      * disabled.
      * 
      * Compare to bdd_autoreorder_times.
      */
     public abstract void autoReorder(ReorderMethod method, int max);
 
     /**
      * Returns the current reorder method as defined by autoReorder.
      * 
      * Compare to bdd_getreorder_method.
      * 
      * @return ReorderMethod
      */
     public abstract ReorderMethod getReorderMethod();
     
     /**
      * Returns the number of allowed reorderings left.  This value can be
      * defined by autoReorder.
      * 
      * Compare to bdd_getreorder_times.
      */
     public abstract int getReorderTimes();
     
     /**
      * Disable automatic reordering until enableReorder is called.  Reordering
      * is enabled by default as soon as any variable blocks have been defined.
      * 
      * Compare to bdd_disable_reorder.
      */
     public abstract void disableReorder();
     
     /**
      * Enable automatic reordering after a call to disableReorder.
      * 
      * Compare to bdd_enable_reorder
      */
     public abstract void enableReorder();
 
     /**
      * Enables verbose information about reordering.  A value of zero means no
      * information, one means some information and greater than one means lots
      * of information.
      * 
      * @param v the new verbose level
      * @return the old verbose level
      */
     public abstract int reorderVerbose(int v);
     
     /**
      * This function sets the current variable order to be the one defined by
      * neworder.  The variable parameter neworder is interpreted as a sequence
      * of variable indices and the new variable order is exactly this sequence.
      * The array must contain all the variables defined so far. If, for
      * instance the current number of variables is 3 and neworder contains
      * [1; 0; 2] then the new variable order is v1<v0<v2.
      *      * @param neworder     */
     public abstract void setVarOrder(int[] neworder);
 
 
     
     /**** VARIABLE BLOCKS ****/
     
     /**
      * Adds a new variable block for reordering.
      * 
      * Creates a new variable block with the variables in the variable set var.
      * The variables in var must be contiguous.
      * 
      * The fixed parameter sets the block to be fixed (no reordering of its
      * child blocks is allowed) or free,
      * 
      * Compare to bdd_addvarblock.
      */
     public abstract void addVarBlock(BDD var, boolean fixed);
     // TODO: handle error code for addVarBlock.
     
     /**
      * Adds a new variable block for reordering.
      * 
      * Creates a new variable block with the variables numbered first through
      * last, inclusive.
      * 
      * The fixed parameter sets the block to be fixed (no reordering of its
      * child blocks is allowed) or free,
      * 
      * Compare to bdd_intaddvarblock.
      */
     public abstract void addVarBlock(int first, int last, boolean fixed);
     // TODO: handle error code for addVarBlock.
     // TODO: fdd_intaddvarblock (?)
     
     /**
      * Add a variable block for all variables.
      * 
      * Adds a variable block for all BDD variables declared so far.  Each block
      * contains one variable only.  More variable blocks can be added later with
      * the use of addVarBlock -- in this case the tree of variable blocks will
      * have the blocks of single variables as the leafs.
      * 
      * Compare to bdd_varblockall.
      */
     public abstract void varBlockAll();
 
     /**
      * Clears all the variable blocks that have been defined by calls to
      * addVarBlock.
      * 
      * Compare to bdd_clrvarblocks.
      */
     public abstract void clearVarBlocks();
 
     /**
      * Prints an indented list of the variable blocks.
      * 
      * Compare to bdd_printorder.
      */
     public abstract void printOrder();
 
 
     /**** BDD STATS ****/
     
     /**
      * Counts the number of shared nodes in a collection of BDDs.  Counts all
      * distinct nodes that are used in the BDDs -- if a node is used in more
      * than one BDD then it only counts once.
      * 
      * Compare to bdd_anodecount.
      */
     public abstract int nodeCount(Collection/*BDD*/ r);
 
     /**
      * Get the number of allocated nodes.  This includes both dead and active
      * nodes.
      * 
      * Compare to bdd_getallocnum.
      */
     public abstract int getAllocNum();
 
     /**
      * Get the number of active nodes in use.  Note that dead nodes that have
      * not been reclaimed yet by a garbage collection are counted as active.
      * 
      * Compare to bdd_getnodenum
      */
     public abstract int getNodeNum();
 
     /**
      * Calculate the gain in size after a reordering.  The value returned is
      * (100*(A-B))/A, where A is previous number of used nodes and B is current
      * number of used nodes.
      * 
      * Compare to bdd_reorder_gain.
      */
     public abstract int reorderGain();
 
     /**
      * Print cache statistics.
      * 
      * Compare to bdd_printstat.
      */
     public abstract void printStat();
 
     // TODO: bdd_cachestats, bdd_stats
     
     
     /**
      * Compare to bdd_newpair.
      */
     public abstract BDDPairing makePair();
 
     public BDDPairing makePair(int oldvar, int newvar) {
         BDDPairing p = makePair();
         p.set(oldvar, newvar);
         return p;
     }
 
     public BDDPairing makePair(int oldvar, BDD newvar) {
         BDDPairing p = makePair();
         p.set(oldvar, newvar);
         return p;
     }
     
     public BDDPairing makePair(BDDDomain oldvar, BDDDomain newvar) {
         BDDPairing p = makePair();
         p.set(oldvar, newvar);
         return p;
     }
     
     /**
      * Compare to bdd_swapvar.
      */
     public abstract void swapVar(int v1, int v2);
     
     // TODO: bdd_sizeprobe_hook, bdd_reorder_hook, bdd_resize_hook, bdd_gbc_hook
     // TODO: bdd_reorder_probe
     
     // TODO: bvec functions
 
 
     /**** FINITE DOMAINS ****/
     
     protected BDDDomain[] domain;
     protected int fdvarnum;
     protected int firstbddvar;
     
     /**
      * Implementors must implement this factory method to create BDDDomain
      * objects of the correct type.
      */
     protected abstract BDDDomain createDomain(int a, long b);
     
     /**
      * Extends the set of finite domain blocks with domains of the given sizes.
      * Each entry in domainSizes is the size of a new finite domain which later
      * on can be used for finite state machine traversal and other operations on
      * finite domains.  Each domain allocates log 2 (|domainSizes[i]|) BDD
      * variables to be used later.  The ordering is interleaved for the domains
      * defined in each call to extDomain. This means that assuming domain D0
      * needs 2 BDD variables x1 and x2 , and another domain D1 needs 4 BDD
      * variables y1, y2, y3 and y4, then the order then will be x1, y1, x2, y2,
      * y3, y4.  The new domains are returned in order.  The BDD variables needed
      * to encode the domain are created for the purpose and do not interfere
      * with the BDD variables already in use.
      * 
      * Compare to fdd_extdomain.
      */
     public BDDDomain[] extDomain(int[] dom) {
         long[] a = new long[dom.length];
         for (int i=0; i<a.length; ++i) {
             a[i] = (long) dom[i];
         }
         return extDomain(a);
     }
     public BDDDomain[] extDomain(long[] domainSizes) {
         int offset = fdvarnum;
         int binoffset;
         int extravars = 0;
         int n, bn;
         boolean more;
         int num = domainSizes.length;
 
         /* Build domain table */
         if (domain == null) /* First time */ {
             domain = new BDDDomain[num];
         } else /* Allocated before */ {
             if (fdvarnum + num > domain.length) {
                 int fdvaralloc = domain.length + Math.max(num, domain.length);
                 BDDDomain[] d2 = new BDDDomain[fdvaralloc];
                 System.arraycopy(domain, 0, d2, 0, domain.length);
                 domain = d2;
             }
         }
 
         /* Create bdd variable tables */
         for (n = 0; n < num; n++) {
             domain[n + fdvarnum] = createDomain(n + fdvarnum, domainSizes[n]);
             extravars += domain[n + fdvarnum].varNum();
         }
 
         binoffset = firstbddvar;
         int bddvarnum = varNum();
         if (firstbddvar + extravars > bddvarnum) {
             setVarNum(firstbddvar + extravars);
         }
 
         /* Set correct variable sequence (interleaved) */
         for (bn = 0, more = true; more; bn++) {
             more = false;
 
             for (n = 0; n < num; n++) {
                 if (bn < domain[n + fdvarnum].varNum()) {
                     more = true;
                     domain[n + fdvarnum].ivar[bn] = binoffset++;
                 }
             }
         }
 
         for (n = 0; n < num; n++) {
             domain[n + fdvarnum].var =
                 makeSet(domain[n + fdvarnum].ivar);
         }
 
         fdvarnum += num;
         firstbddvar += extravars;
 
         BDDDomain[] r = new BDDDomain[num];
         System.arraycopy(domain, offset, r, 0, num);
         return r;
     }
     
     /**
      * This function takes two finit blocks and merges them into a new one, such
      * that the new one is encoded using both sets of BDD variables.
      * 
      * Compare to fdd_overlapdomain.
      */
     public BDDDomain overlapDomain(BDDDomain d1, BDDDomain d2) {
         BDDDomain d;
         int n;
 
         int fdvaralloc = domain.length;
         if (fdvarnum + 1 > fdvaralloc) {
             fdvaralloc += fdvaralloc;
             BDDDomain[] domain2 = new BDDDomain[fdvaralloc];
             System.arraycopy(domain, 0, domain2, 0, domain.length);
             domain = domain2;
         }
 
         d = domain[fdvarnum];
         d.realsize = d1.realsize * d2.realsize;
         d.ivar = new int[d1.varNum() + d2.varNum()];
 
         for (n = 0; n < d1.varNum(); n++)
             d.ivar[n] = d1.ivar[n];
         for (n = 0; n < d2.varNum(); n++)
             d.ivar[d1.varNum() + n] = d2.ivar[n];
 
         d.var = makeSet(d.ivar);
         //bdd_addref(d.var);
 
         fdvarnum++;
         return d;
     }
     
     /**
      * Returns a BDD defining all the variable sets used to define the variable
      * blocks in the given array.
      * 
      * Compare to fdd_makeset.
      */
     public BDD makeSet(BDDDomain[] v) {
         BDD res = one();
         int n;
 
         for (n = 0; n < v.length; n++) {
             res.andWith(v[n].set());
         }
 
         return res;
     }
     
     /**
      * Clear all allocated finite domain blocks that were defined by extDomain()
      * or overlapDomain().
      * 
      * Compare to fdd_clearall.
      */
     public void clearAllDomains() {
         domain = null;
         fdvarnum = 0;
         firstbddvar = 0;
     }
     
     /**
      * Returns the number of finite domain blocks defined by calls to
      * extDomain().
      * 
      * Compare to fdd_domainnum.
      */
     public int numberOfDomains() {
         return fdvarnum;
     }
     
     /**
      * Returns the ith finite domain block, as defined by calls to
      * extDomain().
      */
     public BDDDomain getDomain(int i) {
         if (i < 0 || i >= fdvarnum)
             throw new IndexOutOfBoundsException();
         return domain[i];
     }
     
     // TODO: fdd_file_hook, fdd_strm_hook
     
     public int[] makeVarOrdering(boolean reverseLocal, String ordering) {
         
         int varnum = varNum();
         
         int nDomains = numberOfDomains();
         int[][] localOrders = new int[nDomains][];
         for (int i=0; i<localOrders.length; ++i) {
             localOrders[i] = new int[getDomain(i).varNum()];
         }
         
         for (int i=0; i<nDomains; ++i) {
             BDDDomain d = getDomain(i);
             int nVars = d.varNum();
             for (int j=0; j<nVars; ++j) {
                 if (reverseLocal) {
                     localOrders[i][j] = nVars - j - 1;
                 } else {
                     localOrders[i][j] = j;
                 }
             }
         }
         
         BDDDomain[] doms = new BDDDomain[nDomains];
         
         int[] varorder = new int[varnum];
         
         //System.out.println("Ordering: "+ordering);
         StringTokenizer st = new StringTokenizer(ordering, "x_", true);
         int numberOfDomains = 0, bitIndex = 0;
         boolean[] done = new boolean[nDomains];
         for (int i=0; ; ++i) {
             String s = st.nextToken();
             BDDDomain d;
             for (int j=0; ; ++j) {
                 if (j == numberOfDomains())
                     throw new BDDException("bad domain: "+s);
                 d = getDomain(j);
                 if (s.equals(d.getName())) break;
             }
             if (done[d.getIndex()])
                 throw new BDDException("duplicate domain: "+s);
             done[d.getIndex()] = true;
             doms[i] = d;
             if (st.hasMoreTokens()) {
                 s = st.nextToken();
                 if (s.equals("x")) {
                     ++numberOfDomains;
                     continue;
                 }
             }
             bitIndex = fillInVarIndices(doms, i-numberOfDomains, numberOfDomains+1,
                                         localOrders, bitIndex, varorder);
             if (!st.hasMoreTokens()) {
                 break;
             }
             if (s.equals("_"))
                 numberOfDomains = 0;
             else
                 throw new BDDException("bad token: "+s);
         }
         
         for (int i=0; i<doms.length; ++i) {
             if (!done[i]) {
                 throw new BDDException("missing domain #"+i+": "+getDomain(i));
             }
             doms[i] = getDomain(i);
         }
         
         int[] test = new int[varorder.length];
         System.arraycopy(varorder, 0, test, 0, varorder.length);
         Arrays.sort(test);
         for (int i=0; i<test.length; ++i) {
             if (test[i] != i) 
                 throw new BDDException(test[i]+" != "+i);
         }
         
         return varorder;
     }
     
     static int fillInVarIndices(
                          BDDDomain[] doms, int domainIndex, int numDomains,
                          int[][] localOrders, int bitIndex, int[] varorder) {
         // calculate size of largest domain to interleave
         int maxBits = 0;
         for (int i=0; i<numDomains; ++i) {
             BDDDomain d = doms[domainIndex+i];
             maxBits = Math.max(maxBits, d.varNum());
         }
         // interleave the domains
         for (int bitNumber=0; bitNumber<maxBits; ++bitNumber) {
             for (int i=0; i<numDomains; ++i) {
                 BDDDomain d = doms[domainIndex+i];
                 if (bitNumber < d.varNum()) {
                     int di = d.getIndex();
                     int local = localOrders[di][bitNumber];
                     varorder[bitIndex++] = d.vars()[local];
                 }
             }
         }
         return bitIndex;
     }
     
     /**
      * @see java.lang.Object#finalize()
      */
     protected void finalize() throws Throwable {
         super.finalize();
         this.done();
     }
 
     /**
      * No reordering.
      */
     public static final ReorderMethod REORDER_NONE    = new ReorderMethod(0, "NONE");
     /**
      * Reordering using a sliding window of 2.
      */
     public static final ReorderMethod REORDER_WIN2    = new ReorderMethod(1, "WIN2");
     /**
      * Reordering using a sliding window of 2, iterating until no further
      * progress.
      */
     public static final ReorderMethod REORDER_WIN2ITE = new ReorderMethod(2, "WIN2ITE");
     /**
      * Reordering using a sliding window of 3.
      */
     public static final ReorderMethod REORDER_WIN3    = new ReorderMethod(5, "WIN3");
     /**
      * Reordering using a sliding window of 3, iterating until no further
      * progress.
      */
     public static final ReorderMethod REORDER_WIN3ITE = new ReorderMethod(6, "WIN3ITE");
     /**
      * Reordering where each block is moved through all possible positions.  The
      * best of these is then used as the new position.  Potentially a very slow
      * but good method.
      */
     public static final ReorderMethod REORDER_SIFT    = new ReorderMethod(3, "SIFT");
     /**
      * Same as REORDER_SIFT, but the process is repeated until no further
      * progress is done.  Can be extremely slow.
      */
     public static final ReorderMethod REORDER_SIFTITE = new ReorderMethod(4, "SIFTITE");
     /**
      * Selects a random position for each variable.  Mostly used for debugging
      * purposes.
      */
     public static final ReorderMethod REORDER_RANDOM  = new ReorderMethod(7, "RANDOM");
     
     /**
      * Enumeration class for method reordering techniques.  Use the static fields
      * in BDDFactory to access the different reordering techniques.
      */
     public static class ReorderMethod {
         final int id; final String name;
         private ReorderMethod(int id, String name) {
             this.id = id;
             this.name = name;
         }
         public String toString() {
             return name;
         }
     }
     
     protected abstract BDDBitVector createBitVector(int a);
     
     // compare to bvec_true, bvec_false
     public BDDBitVector buildVector(int bitnum, boolean b) {
         BDDBitVector v = createBitVector(bitnum);
         v.initialize(b);
         return v;
     }
     
     // compare to bvec_con
     public BDDBitVector constantVector(int bitnum, int val) {
         BDDBitVector v = createBitVector(bitnum);
         v.initialize(val);
         return v;
     }
     public BDDBitVector constantVector(int bitnum, long val) {
         BDDBitVector v = createBitVector(bitnum);
         v.initialize(val);
         return v;
     }
     
     // compare to bvec_var
     public BDDBitVector buildVector(int bitnum, int offset, int step) {
         BDDBitVector v = createBitVector(bitnum);
         v.initialize(offset, step);
         return v;
     }
     
     // compare to bvec_varfdd
     public BDDBitVector buildVector(BDDDomain d) {
         BDDBitVector v = createBitVector(d.varNum());
         v.initialize(d);
         return v;
     }
     
     // compare to bvec_varvec
     public BDDBitVector buildVector(int[] var) {
         BDDBitVector v = createBitVector(var.length);
         v.initialize(var);
         return v;
     }
     
 }

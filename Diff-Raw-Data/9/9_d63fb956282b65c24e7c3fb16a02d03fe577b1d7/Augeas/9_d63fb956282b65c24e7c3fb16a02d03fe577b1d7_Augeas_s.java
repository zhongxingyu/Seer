 /*
  * Augeas.java
  *
  * Copyright (C) 2009 Red Hat Inc.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
  *
  * Author: Bryan Kearney <bkearney@redhat.com>
  */
 
 package net.augeas;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.augeas.jna.Aug;
 import net.augeas.jna.AugPointer;
 
 import com.sun.jna.Native;
 import com.sun.jna.Pointer;
 import com.sun.jna.StringArray;
 import com.sun.jna.ptr.IntByReference;
 import com.sun.jna.ptr.PointerByReference;
 
 /**
  * Public Augeas API.
  */
 public class Augeas {
 
     /**
      * Flags to use
      */
     public static int NONE = 0;
     /**
      * Keep the original file with a .augsave extension
      */
     public static int SAVE_BACKUP = (1 << 0);
 
     /**
      * Save changes into a file with extension .augnew, and do not overwrite the
      * original file. Takes precedence over AUG_SAVE_BACKUP
      */
     public static int SAVE_NEWFILE = (1 << 1);
 
     /**
      * Typecheck lenses; since it can be very expensive it is not done by
      * default
      */
     public static int TYPE_CHECK = (1 << 2);
 
     /**
      * Do not use the builtin load path for modules
      */
     public static int NO_STDINC = (1 << 3);
 
     /**
      * Make save a no-op process, just record what would have changed
      */
     public static int SAVE_NOOP = (1 << 4);
 
     /**
      * Do not load the tree from AUG_INIT
      */
     public static int NO_LOAD = (1 << 5);
 
     public static int NO_MODL_AUTOLOAD = (1 << 6);
 
     /**
      * Pointert to he active augeas instance.
      */
     protected AugPointer aug;
 
     /**
      * The augeas library handle
      */
     protected Aug AugLib = Aug.INSTANCE;
 
     /**
      * The result of the last augeas call.
      */
     protected int lastReturn;
 
     /**
      * If a invalid augeas call (lastReturn = -1) should result in an exception
      */
     protected boolean raiseExceptions = true;
 
     /**
      * Default constructor that defaults to root and no load path
      */
     public Augeas() {
         this("/", "", 0);
     }
 
     /**
      * New Augeas instance with only the flags.
      * 
      * @param flags
      */
     public Augeas(int flags) {
         this("/", "", flags);
     }
 
     /**
      * Constructs a new Augeas connection
      * 
      * @param root
      *            or all file lookups
      * @param loadpath
      *            path for finding new schemas
      * @param flags
      *            any flags to use
      */
     public Augeas(String root, String loadpath, int flags) {
         aug = AugLib.aug_init(root, loadpath, flags);
     }
 
     protected void check() {
         if (aug == null) {
             throw new AugeasException("Augues connection closed");
         }
     }
 
     /**
      * Clear the PATH, i.e. set the value to null
      * 
      * @return 0 / -1 based on success/fail
      */
     public int clear(String path) {
         return this.set(path, null);
     }
 
     /**
      * Clear all transforms under <tt>/augeas/load</tt>. If load() is called
      * right after this, there will be no files under +/files+
      * 
      * @return 0 / -1 based on success/fail
      */
     public int clearTransforms() {
         return this.rm("/augeas/load/*");
     }
 
     /**
      * Write all pending changes to disk
      * 
      * @return -1 on error, 0 on success
      */
     public int close() {
         if (aug != null) {
             lastReturn = AugLib.aug_close(aug);
             processLastCall("Close failed");
             aug = null;
         } else {
             lastReturn = 0;
         }
         return lastReturn;
     }
 
     /**
      * Define a variable NAME whose value is the result of evaluating EXPR,
      * which must be non-NULL and evaluate to a nodeset.
      * 
      * @return -1 on error; on success, returns the number of nodes in the
      *         nodeset
      */
     public int defineNode(String name, String expr, String value) {
         check();
         IntByReference created = new IntByReference();
         lastReturn = AugLib.aug_defnode(aug, name, expr, value, created);
         processLastCall("defineNode failed");
         return created.getValue();
     }
 
     /**
      * Define a variable NAME whose value is the result of evaluating EXPR.
      * 
      * @return -1 on error; on success, returns 0 if EXPR evaluates to anything
      *         other than a nodeset, and the number of nodes if EXPR evaluates
      *         to a nodeset
      */
     public int defineVariable(String name, String expr) {
         check();
         lastReturn = AugLib.aug_defvar(aug, name, expr);
         processLastCall("defineVariable failed");
         return lastReturn;
     }
 
     /**
      * Returns true if the path exists
      * 
      * @return true if it exists
      */
     public boolean exists(String path) {
         check();
         lastReturn = AugLib.aug_get(aug, path, null);
         processLastCall("exists failed");
         return lastReturn == 1;
     }
 
     /**
      * Lookup the value associated eith PATH
      * 
      * @return the value
      */
     public String get(String path) {
         check();
         String[] items = new String[1];
         StringArray itemArray = new StringArray(items);
         lastReturn = AugLib.aug_get(aug, path, itemArray);
         processLastCall("get failed");
         return items[0];
     }
 
     /**
      * Return the result from the last augeas call.
      * 
      * @return
      */
     public int getLastReturn() {
         return lastReturn;
     }
 
     /**
      * if exceptions should be raised
      */
     public boolean getRaiseExceptions() {
         return raiseExceptions;
     }
 
     /**
      * Create a new sibling LABEL for PATH by inserting into the tree just
      * before PATH if BEFORE == 1 or just after PATH if BEFORE == 0.
      * 
      * @return 0 on success, and -1 if the insertion fails.
      */
     public int insert(String path, String label, boolean before) {
         check();
         int intbefore = before ? 1 : 0;
         lastReturn = AugLib.aug_insert(aug, path, label, intbefore);
         processLastCall("insert failed");
         return lastReturn;
     }
 
     /**
      * Returns the error code from the last method call
      */
     public AugeasErrorCode lastError() {
         check();
         return AugeasErrorCode.forValue(AugLib.aug_error(aug));
     }
 
     /**
      * Returns the error details from the last method call
      */
     public String lastErrorDetails() {
         check();
         return AugLib.aug_error_details(aug);
     }
 
     /**
      * Returns the error message from the last method call
      */
     public String lastErrorMessage() {
         check();
         return AugLib.aug_error_message(aug);
     }
 
     /**
      * Returns the minor error message from the last method call
      */
     public String lastMinorErrorMessage() {
         check();
         return AugLib.aug_error_minor_message(aug);
     }
 
     /**
      * Load files into the tree
      * 
      * @return -1 on error, 0 on success
      */
     public int load() {
         check();
         lastReturn = AugLib.aug_load(aug);
         processLastCall("load failed");
         return lastReturn;
     }
 
     /**
      * Return a list of the nodes which match the path string
      * 
      * @param path
      * @return
      */
     public List<String> match(String path) {
         check();
         PointerByReference ptrByR = new PointerByReference();
         lastReturn = AugLib.aug_match(aug, path, ptrByR);
         processLastCall("match failed");
         Pointer ptr = ptrByR.getValue();
         ArrayList<String> list = new ArrayList<String>();
         for (int x = 0; x < lastReturn; x++) {
             Pointer inner = ptr.getPointer(x * Native.POINTER_SIZE);
             if (inner != null) {
                 String value = inner.getString(0);
                 list.add(value);
             }
         }
         return list;
     }
 
     /**
      * Move the node SRC to DST.
      * 
      * @return 0 on success and -1 on failure
      */
     public int move(String source, String dest) {
         check();
         lastReturn = AugLib.aug_mv(aug, source, dest);
         processLastCall("move failed");
         return lastReturn;
     }
 
     /**
      * If the user has opted to throw exceptions on failure, this method will do
      * so based on a return code of -1
      */
     protected void processLastCall(String message) {
         if (raiseExceptions && lastReturn == -1) {
             throw new AugeasException(message);
         }
     }
 
     /**
      * @see rm
      */
     public int remove(String path) {
         check();
         return this.rm(path);
     }
 
     /**
      * Remove path and all its children.
      * 
      * @return -1 / 0 based on fail/success
      */
     public int rm(String path) {
         check();
         lastReturn = AugLib.aug_rm(aug, path);
         processLastCall("rm failed");
         return lastReturn;
     }
 
     /**
      * Write all pending changes to disk
      * 
      * @return -1 on error, 0 on success
      */
     public int save() {
         check();
         lastReturn = AugLib.aug_save(aug);
         processLastCall("save failed");
         return lastReturn;
     }
 
     /**
      * Set the value associated with PATH to VALUE.
      * 
      * @return 0 on success, -1 on error. It is an error if more than one node
      *         matches PATH.
      */
     public int set(String path, String value) {
         check();
         lastReturn = AugLib.aug_set(aug, path, value);
         processLastCall("set failed");
         return lastReturn;
     }
 
     /**
      * Set the value of multiple nodes in one operation. Find or create a node
      * matching SUB by interpreting SUB as a path expression relative to each
      * node matching BASE. SUB may be NULL, in which case all the nodes
      * matching BASE will be modified.
      *
      * @return number of modified nodes on success, -1 on error
      */
     public int setMany(String base, String sub, String value) {
         check();
         lastReturn = AugLib.aug_setm(aug, base, sub, value);
         processLastCall("setMany failed");
         return lastReturn;
     }
 
     /**
      * sets if exceptions should be raised
      */
     public void setRaiseExceptions(boolean value) {
         raiseExceptions = value;
     }
 
     /**
      * Add a transform under <tt>/augeas/load</tt>
      * 
      * @param lens
      *            the lens to use
      * @param name
      *            a unique name (optional)
      * @param incl
      *            a list of glob patterns to transform
      * @param excl
      *            a list of glob patterns to remove
      * @return
      */
     public int transform(String lens, String name, List<String> incl, List<String> excl) {
         check();
         if (lens == null)
             throw new AugeasException("No Lens specified");
         if (incl == null)
             throw new AugeasException("No files to include ");
         if (name == null)
             name = lens.split("/.")[0].replace("@", "");
 
         String xfm = String.format("/augeas/load/%s/", name);
         this.set(xfm + "lens", lens);
         for (String inc : incl) {
             this.set(xfm + "incl[last()+1]", inc);
         }
         if (excl != null) {
             for (String ex : excl) {
                 this.set(xfm + "excl[last()+1]", ex);
             }
         }
         return lastReturn;
     }
 }

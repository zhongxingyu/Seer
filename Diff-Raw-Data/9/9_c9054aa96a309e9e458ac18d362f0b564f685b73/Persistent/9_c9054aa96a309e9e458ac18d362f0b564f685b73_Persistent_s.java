 /*
  * $Source$
  * $Revision$
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * -------------------------------------
  *  Copyright (C) 2000 William Chesters
  * -------------------------------------
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * A copy of the GPL should be in the file org/melati/COPYING in this tree.
  * Or see http://melati.org/License.html.
  *
  * Contact details for copyright holder:
  *
  *     William Chesters <williamc@paneris.org>
  *     http://paneris.org/~williamc
  *     Obrechtstraat 114, 2517VX Den Haag, The Netherlands
  *
  *
  * ------
  *  Note
  * ------
  *
  * I will assign copyright to PanEris (http://paneris.org) as soon as
  * we have sorted out what sort of legal existence we need to have for
  * that to make sense. 
  * In the meantime, if you want to use Melati on non-GPL terms,
  * contact me!
  */
 
 package org.melati.poem;
 
 import java.util.*;
 import java.io.*;
 import org.melati.util.*;
 
 public class Persistent extends Transactioned implements Cloneable {
   private Table table;
   private Integer troid;        // or null if a floating object
   private AccessToken clearedToken;
   private boolean knownCanRead = false, knownCanWrite = false, knownCanDelete = false;
 
   boolean dirty = false;
 
   private static final int NONEXISTENT = 0, EXISTENT = 1, DELETED = 2;
   private int status = NONEXISTENT;
 
   private Object[] extras = null;
 
   public Persistent(Table table, Integer troid) {
     super(table.getDatabase());
     this.table = table;
     this.troid = troid;
   }
 
   public Persistent(Table table) {
     this.table = table;
   }
 
   public Persistent() {
   }
 
   // 
   // --------
   //  Status
   // --------
   // 
 
   final void setStatusNonexistent() {
     status = NONEXISTENT;
   }
 
   final void setStatusExistent() {
     status = EXISTENT;
   }
 
   public final boolean statusNonexistent() {
     return status == NONEXISTENT;
   }
 
   public final boolean statusExistent() {
     return status == EXISTENT;
   }
 
   // 
   // ***************
   //  Transactioned
   // ***************
   // 
 
   private void assertNormalPersistent() {
     if (troid == null)
       throw new InvalidOperationOnFloatingPersistentPoemException(this);
     if (status == DELETED)
       throw new RowDisappearedPoemException(this);
   }
 
   protected void load(Transaction transaction) {
     assertNormalPersistent();
     table.load((PoemTransaction)transaction, this);
     // table will clear our dirty flag
   }
 
   protected boolean upToDate(Transaction transaction) {
     return true;
   }
 
   protected void writeDown(Transaction transaction) {
     assertNormalPersistent();
     table.writeDown((PoemTransaction)transaction, this);
     // table will clear our dirty flag
   }
 
   protected void writeLock(Transaction transaction) {
     if (troid != null) {
       assertNormalPersistent();
       super.writeLock(transaction);
       dirty = true;
       table.notifyTouched((PoemTransaction)transaction, this);
     }
   }
 
   /**
    * This is just to make this method available to <TT>Table</TT>.
    */
 
   protected void readLock(Transaction transaction) {
     if (troid != null) {
       assertNormalPersistent();
       super.readLock(transaction);
     }
   }
 
   protected void commit(Transaction transaction) {
     assertNormalPersistent();
     super.commit(transaction);
   }
 
   protected void rollback(Transaction transaction) {
     assertNormalPersistent();
     super.rollback(transaction);
   }
 
   // 
   // ************
   //  Persistent
   // ************
   // 
 
   /** a shortcut method to mark this object as persistent */
   public final void makePersistent()
   {
       getTable().create(this);
   }
   
   synchronized Object[] extras() {
     if (extras == null)
       extras = new Object[table.extrasCount()];
     return extras;
   }
 
   /**
    * The table from which the object comes.
    */
 
   public final Table getTable() {
     return table;
   }
 
   synchronized void setTable(Table table, Integer troid) {
     setTransactionPool(table.getDatabase());
     this.table = table;
     this.troid = troid;
   }
 
 
   /**
    * The database from which the object comes.  <I>I.e.</I>
    * <TT>getTable().getDatabase()</TT>.
    */
 
   public final Database getDatabase() {
     return table.getDatabase();
   }
   /**
    * FIXME This shouldn't be public because we don't in principle want people
    * to know even the troid of an object they aren't allowed to read.  However,
    * I think this information may leak out elsewhere.
    */
 
   public final Integer troid() {
     return troid;
   }
 
   /**
    * The object's troid.
    *
    * @return Every record (object) in a POEM database must have a
    *         troid (table row ID, or table-unique non-nullable integer primary
    *         key), often but not necessarily called <TT>id</TT>, so that it can
    *         be conveniently `named' for retrieval.
    *
    * @exception AccessPoemException
    *                if <TT>assertCanRead</TT> fails
    *
    * @see Table#getObject(java.lang.Integer)
    * @see #assertCanRead
    */
 
   public final Integer getTroid() throws AccessPoemException {
     assertCanRead();
     return troid();
   }
 
   // 
   // ----------------
   //  Access control
   // ----------------
   // 
 
   protected void readLock(SessionToken sessionToken)
       throws AccessPoemException {
     assertCanRead(sessionToken.accessToken);
     readLock(sessionToken.transaction);
   }
 
   protected void writeLock(SessionToken sessionToken)
       throws AccessPoemException {
     if (troid != null)
       assertCanWrite(sessionToken.accessToken);
     writeLock(sessionToken.transaction);
   }
 
   protected void deleteLock(SessionToken sessionToken)
       throws AccessPoemException {
     if (troid != null)
       assertCanDelete(sessionToken.accessToken);
     writeLock(sessionToken.transaction);
   }
 
   protected void readLock() throws AccessPoemException {
     readLock(PoemThread.sessionToken());
   }
 
   protected void writeLock() throws AccessPoemException {
     writeLock(PoemThread.sessionToken());
   }
 
   /**
    * The capability required for reading the object's field values.  This is
    * used by <TT>assertCanRead</TT> (unless that's been overridden) to obtain a
    * <TT>Capability</TT> for comparison against the caller's
    * <TT>AccessToken</TT>.
    *
    * @return the capability specified by the record's <TT>canread</TT> field, or
    *         <TT>null</TT> if it doesn't have one or its value is SQL
    *         <TT>NULL</TT>
    *
    * @see #assertCanRead
    */
 
   protected Capability getCanRead() {
     Column crCol = getTable().canReadColumn();
     return
         crCol == null ? null :
             (Capability)crCol.getType().cookedOfRaw(crCol.getRaw_unsafe(this));
   }
 
   /**
    * Check that you have read access to the object.  Which is to say: the
    * <TT>AccessToken</TT> associated with the POEM task executing in the
    * running thread confers the <TT>Capability</TT> required for inspecting the
    * object's fields.  The access token is set when the task is invoked using
    * <TT>Database.inSession</TT>.  The capability is determined by
    * <TT>getCanRead</TT>, which by default means the capability defined in the
    * record's <TT>canread</TT> field in the database.  If that's <TT>null</TT>,
    * the table's default <TT>canread</TT> capability is obtained using
    * <TT>getTable().getDefaultCanRead()</TT>.  If that is <TT>null</TT> as
    * well, access is granted unconditionally.
    *
    * <P>
    *
    * Although this check can in theory be quite time-consuming, in practice
    * this isn't a problem, because the most recent access token for which the
    * check succeeded is cached; repeat accesses from within the same transaction
    * are therefore quick.
    *
    * <P>
    *
    * Application programmers can override this method to implement their own
    * programmatic access policies.  For instance, POEM's own <TT>TableInfo</TT>
    * class overrides it with an empty method in order to disable all read
    * protection on <TT>TableInfo</TT> objects.  More interestingly, you could
    * implement a check that depends on the values of the object's fields:
    * for example, you could allow read access to an invoice record to its
    * issuing and receiving parties.
    *
    * @param token       the access token on the basis of which readability is
    *                    being claimed
    *
    * @exception AccessPoemException if the check fails
    *
    * @see #getCanRead
    * @see Database#inSession
    * @see Table#getDefaultCanRead
    */
 
   public void assertCanRead(AccessToken token)
       throws AccessPoemException {
     // FIXME!!!! this is wrong because token could be stale ...
     if (!(clearedToken == token && knownCanRead) && troid != null) {
       Capability canRead = getCanRead();
       if (canRead == null)
         canRead = getTable().getDefaultCanRead();
       if (canRead != null) {
         if (!token.givesCapability(canRead))
           throw new ReadPersistentAccessPoemException(this, token, canRead);
         if (clearedToken != token) {
           knownCanWrite = false;
           knownCanDelete = false;
         }
         clearedToken = token;
         knownCanRead = true;
       }
     }
   }
 
   public final void assertCanRead() throws AccessPoemException {
     assertCanRead(PoemThread.accessToken());
   }
 
   /**
    * Whether the object is readable by you.
    *
    * @see #assertCanRead
    */
 
   public final boolean getReadable() {
     try {
       assertCanRead();
       return true;
     }
     catch (AccessPoemException e) {
       return false;
     }
   }
 
   /**
    * The capability required for writing the object's field values.  This is
    * used by <TT>assertCanWrite</TT> (unless that's been overridden) to obtain a
    * <TT>Capability</TT> for comparison against the caller's
    * <TT>AccessToken</TT>.
    *
    * @return the capability specified by the record's <TT>canwrite</TT> field,
    *         or <TT>null</TT> if it doesn't have one or its value is SQL
    *         <TT>NULL</TT>
    *
    * @see #assertCanWrite
    */
 
   protected Capability getCanWrite() {
     Column cwCol = getTable().canWriteColumn();
     return
         cwCol == null ? null :
             (Capability)cwCol.getType().cookedOfRaw(cwCol.getRaw_unsafe(this));
   }
 
   /**
    * Check that you have write access to the object.  Which is to say: the
    * <TT>AccessToken</TT> associated with the POEM task executing in the
    * running thread confers the <TT>Capability</TT> required for updating the
    * object's fields.  The remarks made about <TT>assertCanRead</TT> apply
    * (<I>mutatis mutandis</I>) here as well.
    *
    * @see #assertCanRead
    * @see #getCanWrite
    * @see Table#getDefaultCanWrite
    */
 
   public void assertCanWrite(AccessToken token)
       throws AccessPoemException {
     // FIXME!!!! this is wrong because token could be stale ...
     if (!(clearedToken == token && knownCanWrite) && troid != null) {
       Capability canWrite = getCanWrite();
       if (canWrite == null)
         canWrite = getTable().getDefaultCanWrite();
       if (canWrite != null) {
         if (!token.givesCapability(canWrite))
           throw new WritePersistentAccessPoemException(this, token, canWrite);
         if (clearedToken != token)
           knownCanRead = false;
           knownCanDelete = false;
         clearedToken = token;
         knownCanWrite = true;
       }
     }
   }
 
   public final void assertCanWrite() throws AccessPoemException {
     assertCanWrite(PoemThread.accessToken());
   }
 
   /**
    * The capability required for deleting the object.  This is
    * used by <TT>assertCanDelete</TT> (unless that's been overridden) to obtain a
    * <TT>Capability</TT> for comparison against the caller's
    * <TT>AccessToken</TT>.
    *
    * @return the capability specified by the record's <TT>candelete</TT> field,
    *         or <TT>null</TT> if it doesn't have one or its value is SQL
    *         <TT>NULL</TT>
    *
    * @see #assertCanDelete
    */
 
   protected Capability getCanDelete() {
     Column cwCol = getTable().canDeleteColumn();
     return
         cwCol == null ? null :
             (Capability)cwCol.getType().cookedOfRaw(cwCol.getRaw_unsafe(this));
   }
 
   /**
    * Check that you have delete access to the object.  Which is to say: the
    * <TT>AccessToken</TT> associated with the POEM task executing in the
    * running thread confers the <TT>Capability</TT> required for updating the
    * object's fields.  The remarks made about <TT>assertCanRead</TT> apply
    * (<I>mutatis mutandis</I>) here as well.
    *
    * @see #assertCanRead
    * @see #getCanDelete
    * @see Table#getDefaultCanDelete
    */
 
   public void assertCanDelete(AccessToken token)
       throws AccessPoemException {
     // FIXME!!!! this is wrong because token could be stale ...
     if (!(clearedToken == token && knownCanDelete) && troid != null) {
       Capability canDelete = getCanDelete();
       if (canDelete == null)
         canDelete = getTable().getDefaultCanDelete();
       if (canDelete != null) {
         if (!token.givesCapability(canDelete))
           throw new DeletePersistentAccessPoemException(this, token, canDelete);
         if (clearedToken != token)
           knownCanRead = false;
           knownCanWrite = false;
         clearedToken = token;
         knownCanDelete = true;
       }
     }
   }
 
   public final void assertCanDelete() throws AccessPoemException {
     assertCanDelete(PoemThread.accessToken());
   }
 
   /**
    * Check that you have create access to the object.  Which is to say: the
    * <TT>AccessToken</TT> associated with the POEM task executing in the
    * running thread confers the <TT>Capability</TT> required for creating the
    * object. The capability is determined solely by <TT>getCanCreate<TT>
    * from the table. Unlike <TT>assertCanRead</TT> and <TT>assertCanWrite</TT>
    * there is no idea of having a default <TT>Capability</TT> defined in
    * in the table which could be overriden by a <TT>canwrite</TT> field
    * in the persistent (since the persisent has not yet been been written).
    *
    * <P>
    *
    * Application programmers can override this method to implement their own
    * programmatic access policies.
    *
    * @see #assertCanRead
    * @see #assertCanWrite
    * @see Table#getCanCreate
    */
 
   public void assertCanCreate(AccessToken token) {
    // FIXME URGENTLY  - why would we not have a table for this persistent?
    if (getTable() != null) {
      Capability canCreate = getTable().getCanCreate();
      if (canCreate != null && !token.givesCapability(canCreate))
        throw new CreationAccessPoemException(getTable(), token, canCreate);
    }
   }
 
   public final void assertCanCreate() throws AccessPoemException {
     assertCanCreate(PoemThread.accessToken());
   }
 
 
   // 
   // ============================
   //  Reading and writing fields
   // ============================
   // 
 
   // 
   // ------
   //  Raws
   // ------
   // 
 
   /**
    * The `identifying value' of one of the object's fields.  This is the value
    * which is actually stored in the database, given to you as a basic Java
    * type; currently, the only fields for which this differs from the `true
    * value' returned from <TT>getCooked</TT> are reference fields with type
    * <TT>ReferencePoemType</TT>.
    *
    * <P>
    *
    * If the field <TT><I>baz</I></TT> is defined in the DSD as part of a table
    * called <TT><I>foo</I></TT>, then the table's records will be represented
    * by an application-specialised subclass of <TT>Persistent</TT> called
    * <TT><I>Foo</I></TT> which provides a typed <TT>get<I>Baz</I></TT> method.
    * So the easiest way to be sure of your types is to predeclare any fields
    * you use in the DSD, use the typed field-access methods, and let the
    * compiler take the strain.  When working with generic <TT>Persistent</TT>s,
    * you probably want to use <TT>getField</TT>.
    *
    * <P>
    *
    * The value returned is relative to the transaction associated with the
    * calling thread, as set up by <TT>Database.inSession</TT>.  This means that
    * you never see the value of a field change in your transaction because of
    * another transaction's activities, unless you do a
    * <TT>PoemThread.commit()</TT> or a <TT>PoemThread.rollback()</TT>.  If you
    * need to, you can store a <TT>Persistent</TT> in a permanent data structure
    * and access it in different sessions over time---or even from concurrently
    * running sessions, though this may slow down access checking; each
    * transaction will see the value it expects.
    *
    * @param name        the name of the field (<I>i.e.</I> the name of the
    *                    column in the RDBMS and DSD)
    *
    * @return The field's `identifying value'; this will be a <TT>String</TT>,
    *         <TT>Boolean</TT>, <TT>Integer</TT>, <TT>Double</TT> or
    *         <TT>Date</TT> as appropriate.  If the field is a reference field,
    *         the result is an <TT>Integer</TT> giving the troid of the referee.
    *         If you want references to be resolved transparently to
    *         <TT>Persistent</TT>s, use <TT>getCooked</TT>.  If you want a string
    *         representation of the field, use <TT>getRawString</TT> or
    *         <TT>getCookedString</TT>.
    *
    * @exception NoSuchColumnPoemException
    *                if the field named doesn't exist
    * @exception AccessPoemException
    *                if the calling thread doesn't have read access to the
    *                object (see <TT>assertCanRead</TT>)
    *
    * @see #getCooked
    * @see #getRawString
    * @see #getCookedString
    * @see #getField
    * @see Database#inSession
    * @see PoemThread#commit
    * @see PoemThread#rollback
    * @see #assertCanRead
    */
 
   public Object getRaw(String name)
       throws NoSuchColumnPoemException, AccessPoemException {
     return getTable().getColumn(name).getRaw(this);
   }
 
   /**
    * A string representation of the `identifying value' of one of the object's
    * fields.  The value returned is relative to the transaction associated with
    * the calling thread, as set up by <TT>Database.inSession</TT>: see the
    * remarks made about <TT>getRaw</TT>.
    *
    * @param name        the name of the field (<I>i.e.</I> the name of the
    *                    column in the RDBMS and DSD)
    *
    * @return Roughly, the string the underlying RDBMS would display if asked
    *         to show the field's value.  If you want reference fields to be
    *         represented by their referee's <TT>displayString()</TT> (by
    *         default, its primary display field) rather than by its troid, use
    *         <TT>getCookedString</TT>.  If you want the field's value as an
    *         appropriate Java type like <TT>Integer</TT>, use <TT>getRaw</TT>
    *         or <TT>getCooked</TT>---or an equivalent, but type-safe, method
    *         derived from the DSD.
    *
    * @exception NoSuchColumnPoemException
    *                if the field named doesn't exist
    * @exception AccessPoemException
    *                if the calling thread doesn't have read access to the
    *                object (see <TT>assertCanRead</TT>)
    *
    * @see #getCookedString
    * @see #getRaw
    * @see #getCooked
    * @see #assertCanRead
    */
 
   public final String getRawString(String name)
       throws AccessPoemException, NoSuchColumnPoemException {
     Column column = getTable().getColumn(name);
     return column.getType().stringOfRaw(column.getRaw(this));
   }
 
   /**
    * Set the `identifying value' of one of the record's fields.  This is the
    * value which is actually stored in the database, given by you as a basic
    * Java type; currently, the only fields for which this differs from the
    * `true value' expected by <TT>setCooked</TT> are reference fields with type
    * <TT>ReferencePoemType</TT>.
    *
    * <P>
    *
    * If the field <TT><I>baz</I></TT> is defined in the DSD as part of a table
    * called <TT><I>foo</I></TT>, then the table's records will be represented
    * by an application-specialised subclass of <TT>Persistent</TT> called
    * <TT><I>Foo</I></TT> which provides a typed <TT>set<I>Baz</I></TT>
    * method.  So the easiest way to be sure of your types is to predeclare any
    * fields you use in the DSD, use the typed field-access methods, and let the
    * compiler take the strain.  When working with generic <TT>Persistent</TT>s,
    * you probably mean <TT>setRawString</TT> anyway.
    *
    * <P>
    *
    * The change you make to the field's value will only be visible to the
    * calling thread, until it successfully completes the task started by
    * <TT>Database.inSession</TT>, or does an explicit
    * <TT>PoemThread.commit()</TT>.  Up to that point the change can be undone
    * by calling <TT>PoemThread.rollback()</TT>, and will be undone
    * automatically if the task terminates with an uncaught exception.
    *
    * <P>
    *
    * In fact, your changes are not written down to the database, even relative
    * to an uncommitted transaction, until it's actually necessary.  So multiple
    * calls to <TT>setRaw</TT> and relatives will not cause multiple SQL
    * <TT>UPDATE</TT>s to be issued.
    *
    * @param name        the name of the field (<I>i.e.</I> the name of the
    *                    column in the RDBMS and DSD)
    *
    * @param raw       The new value for the field: a <TT>String</TT>,
    *                    <TT>Boolean</TT>, <TT>Integer</TT>, <TT>Double</TT> or
    *                    <TT>Date</TT> as appropriate.  If the field is a
    *                    reference field: an <TT>Integer</TT> giving the troid
    *                    of the referee.  If you want to pass referees as actual
    *                    <TT>Persistent</TT>s, use <TT>setCooked</TT>.  If you
    *                    want to set the field from a string representation
    *                    (<I>e.g.</I> typed in by the user), use
    *                    <TT>setRawString</TT>.
    *
    * @exception NoSuchColumnPoemException
    *                if the field named doesn't exist
    * @exception AccessPoemException
    *                if the calling thread doesn't have write access to the
    *                object (see <TT>assertCanWrite</TT>)
    * @exception TypeMismatchPoemException
    *                if <TT>raw</TT> is of the wrong type; it's easiest to use
    *                DSD-derived typed versions of this method
    * @exception ValidationPoemException
    *                if <TT>raw</TT> is not a valid value for the field
    *                (<I>e.g.</I> a string is too long)
    *
    * @see #setCooked
    * @see #setRawString
    * @see #assertCanWrite
    * @see Database#inSession
    * @see PoemThread#commit
    * @see PoemThread#rollback
    */
 
   public void setRaw(String name, Object raw)
       throws NoSuchColumnPoemException, AccessPoemException,
              ValidationPoemException {
     getTable().getColumn(name).setRaw(this, raw);
   }
 
   /**
    * Set the `identifying value' of one of the record's fields from a string
    * representation.  The remarks about sessions (transactions) and DSD-derived
    * type-safe methods made for <TT>setRaw</TT> apply here too.
    *
    * @param name        the name of the field (<I>i.e.</I> the name of the
    *                    column in the RDBMS and DSD)
    *
    * @param string      A string that will be parsed to obtain the new value
    *                    for the field.  If it's a reference field, this should
    *                    be a decimal representation of the referee's troid.  If
    *                    you want to set fields to values defined by appropriate
    *                    Java types, use <TT>setRaw</TT> or <TT>setCooked</TT>.
    *
    * @exception NoSuchColumnPoemException
    *                if the field named doesn't exist
    * @exception AccessPoemException
    *                if the calling thread doesn't have write access to the
    *                object (see <TT>assertCanWrite</TT>)
    * @exception ParsingPoemException
    *                if <TT>string</TT> doesn't parse as a value of the
    *                appropriate type
    * @exception ValidationPoemException
    *                if <TT>string</TT> parses to an invalid value for the field
    *                (<I>e.g.</I> it's too wide)
    *
    * @see #setRaw
    * @see #setCooked
    * @see #assertCanWrite
    */
 
   public final void setRawString(String name, String string)
       throws NoSuchColumnPoemException, AccessPoemException,
              ParsingPoemException, ValidationPoemException {
     Column column = getTable().getColumn(name);
     column.setRaw(this, column.getType().rawOfString(string));
   }
 
   // 
   // --------
   //  Values
   // --------
   // 
 
   /**
    * The `true value' of one of the object's fields.  This is the
    * fully-interpreted value rather than the one actually stored in the
    * database; currently, the only fields for which this differs from the
    * `identifying value' return from <TT>getRaw</TT> are reference fields
    * with type <TT>ReferencePoemType</TT>.
    *
    * <P>
    *
    * The value returned is relative to the transaction associated with the
    * calling thread, as set up by <TT>Database.inSession</TT>: see the remarks
    * made about <TT>getRaw</TT>.
    *
    * <P>
    *
    * The easiest way to be sure of your types is to predeclare any fields you
    * use in the DSD, or use <TT>getField</TT>.  Again, see the remarks made
    * about <TT>getRaw</TT>.
    *
    * @return The field's `true value'; this will be a <TT>String</TT>,
    *         <TT>Boolean</TT>, <TT>Integer</TT>, <TT>Double</TT>,
    *         <TT>Date</TT>, or, if the field is a reference field, a
    *         <TT>Persistent</TT> representing the referee.  If you just want to
    *         see referees' troids, use <TT>getRaw</TT>.  If you want a string
    *         representation of the field, use <TT>getRawString</TT> or
    *         <TT>getCookedString</TT>.
    *
    * @exception NoSuchColumnPoemException
    *                if the field named doesn't exist
    * @exception AccessPoemException
    *                if the calling thread doesn't have read access to the
    *                object (see <TT>assertCanRead</TT>)
    *
    * @see #getRaw
    * @see #getRawString
    * @see #getCookedString
    * @see #getField
    * @see #assertCanRead
    */
 
   public Object getCooked(String name)
       throws NoSuchColumnPoemException, AccessPoemException {
     return getTable().getColumn(name).getCooked(this);
   }
 
   /**
    * A string representation of the `true value' of one of the object's fields.
    * The value returned is relative to the transaction associated with the
    * calling thread, as set up by <TT>Database.inSession</TT>: see the remarks
    * made about <TT>getRaw</TT>.
    *
    * @param name        the name of the field (<I>i.e.</I> the name of the
    *                    column in the RDBMS and DSD)
    *
    * @return The string the underlying RDBMS would display if asked
    *         to show the field's value, except that reference fields are
    *         represented by their referee's <TT>displayString()</TT> (by
    *         default, its primary display field) rather than by its troid.  If
    *         you want to see troids instead, use <TT>getRawString</TT>.  If
    *         you want the field's value as an appropriate Java type like
    *         <TT>Integer</TT>, use <TT>getRaw</TT> or <TT>getCooked</TT>---or
    *         an equivalent, but type-safe, method derived from the DSD.
    *
    * @exception NoSuchColumnPoemException
    *                if the field named doesn't exist
    * @exception AccessPoemException
    *                if the calling thread doesn't have read access to the
    *                object (see <TT>assertCanRead</TT>)
    *
    * @see #getRawString
    * @see #getRaw
    * @see #getCooked
    * @see #assertCanRead
    * @see #displayString
    */
 
   public final String getCookedString(String name, MelatiLocale locale,
                                      int style)
       throws NoSuchColumnPoemException, AccessPoemException {
     Column column = getTable().getColumn(name);
     return column.getType().stringOfCooked(column.getCooked(this),
                                           locale, style);
   }
 
   /**
    * Set the `true value' of one of the record's fields.  Like
    * <TT>setRaw</TT>, but reference fields expect to see a
    * <TT>Persistent</TT> representing their new referee rather than an
    * <TT>Integer</TT> specifying its troid.  The remarks about sessions
    * (transactions) and DSD-derived type-safe methods made for
    * <TT>setRaw</TT> apply here too.
    *
    * @param name        the name of the field (<I>i.e.</I> the name of the
    *                    column in the RDBMS and DSD)
    *
    * @param raw       The new value for the field: a <TT>String</TT>,
    *                    <TT>Boolean</TT>, <TT>Integer</TT>, <TT>Double</TT>,
    *                    <TT>Date</TT> or, for a reference field, a
    *                    <TT>Persistent</TT>.  If you want to pass referees as
    *                    troids, use <TT>setRaw</TT>.  If you want to set the
    *                    field from a string representation (<I>e.g.</I> typed
    *                    in by the user), use <TT>setRawString</TT>.
    *
    * @exception NoSuchColumnPoemException
    *                if the field named doesn't exist
    * @exception AccessPoemException
    *                if the calling thread doesn't have write access to the
    *                object (see <TT>assertCanWrite</TT>)
    * @exception TypeMismatchPoemException
    *                if <TT>raw</TT> is of the wrong type; it's easiest to use
    *                DSD-derived typed versions of this method
    * @exception ValidationPoemException
    *                if <TT>cooked<TT> is not a valid value for the field
    *                (<I>e.g.</I> a string is too long)
    *
    * @see #setRaw
    * @see #setRawString
    * @see #assertCanWrite
    */
 
   public void setCooked(String name, Object cooked)
       throws NoSuchColumnPoemException, ValidationPoemException,
              AccessPoemException {
     getTable().getColumn(name).setCooked(this, cooked);
   }
 
   // 
   // --------
   //  Fields
   // --------
   // 
 
   /**
    * The value of one of the object's fields, wrapped up with type information
    * sufficient for rendering it.  Basically, value plus name plus type.  This
    * is the form in which Melati's templating facilities expect to receive
    * values for displaying them or creating input boxes.
    *
    * <P>
    *
    * If the field <TT><I>baz</I></TT> is defined in the DSD as part of a table
    * called <TT><I>foo</I></TT>, then the table's records will be represented
    * by an application-specialised subclass of <TT>Persistent</TT> called
    * <TT><I>Foo</I></TT> which provides a <TT>get<I>Baz</I>Field</TT> method.
    *
    * @see org.melati.MarkupLanguage#input(org.melati.poem.Field)
    */
 
   public final Field getField(String name)
       throws NoSuchColumnPoemException, AccessPoemException {
     return getTable().getColumn(name).asField(this);
   }
 
 //   private static class FieldsEnumeration extends MappedEnumeration {
 //     private Persistent persistent;
 
 //     public FieldsEnumeration(Persistent persistent, Enumeration columns) {
 //       super(columns);
 //       this.persistent = persistent;
 //     }
 
 //     public Object mapped(Object column) {
 //       return ((Column)column).asField(persistent);
 //     }
 //   }
   
   private Enumeration fieldsOfColumns(Enumeration columns) {
     // return new FieldsEnumeration(this, columns);
     final Persistent _this = this;
     return
         new MappedEnumeration(columns) {
           public Object mapped(Object column) {
             return ((Column)column).asField(_this);
           }
         };
   }
 
   /**
    * The values of all the object's fields, wrapped up with type information
    * sufficient for rendering them.
    *
    * @return an <TT>Enumeration</TT> of <TT>Field</TT>s
    */
 
   public Enumeration getFields() {
     return fieldsOfColumns(getTable().columns());
   }
 
   /**
    * The values of all the object's fields designated for inclusion in full
    * record displays, wrapped up with type information sufficient for rendering
    * them.
    *
    * @return an <TT>Enumeration</TT> of <TT>Field</TT>s
    */
 
   public Enumeration getRecordDisplayFields() {
     return fieldsOfColumns(getTable().getRecordDisplayColumns());
   }
 
   public Enumeration getDetailDisplayFields() {
     return fieldsOfColumns(getTable().getDetailDisplayColumns());
   }
 
   public Enumeration getSummaryDisplayFields() {
     return fieldsOfColumns(getTable().getSummaryDisplayColumns());
   }
 
   public Enumeration getSearchCriterionFields() {
     return fieldsOfColumns(getTable().getSearchCriterionColumns());
   }
 
   public void delete_unsafe()
       throws AccessPoemException {
     assertNormalPersistent();
     SessionToken sessionToken = PoemThread.sessionToken();
     deleteLock(sessionToken);
     table.delete(troid(), sessionToken.transaction);    
   }
 
   public Field getPrimaryDisplayField() {
     return getTable().displayColumn().asField(this);
   }
 
   // 
   // ==================
   //  Other operations
   // ==================
   // 
 
   /**
    * Delete the object.  Before the record is deleted from the database, POEM
    * checks to see if it is the target of any reference fields, and throws a
    * <TT>DeletionIntegrityPoemException</TT> if it is.  However, if it's safe
    * to delete the record, POEM does so---and then immediately commits your
    * transaction to make the change permanent.  So deletions can't be undone.
    * POEM also supports `soft deletion' through the use of `deleted
    * flags' (FIXME describe)
    *
    * @see PoemThread#commit
    * @see Database#referencesTo
    */
 
   public void deleteAndCommit()
       throws AccessPoemException, DeletionIntegrityPoemException {
 
     assertNormalPersistent();
 
     getDatabase().beginExclusiveLock();
     try {
       SessionToken sessionToken = PoemThread.sessionToken();
       deleteLock(sessionToken);
 
       Enumeration refs = getDatabase().referencesTo(this);
       if (refs.hasMoreElements())
         throw new DeletionIntegrityPoemException(this, refs);
 
       table.delete(troid(), sessionToken.transaction);
       if (sessionToken.transaction != null)
         sessionToken.transaction.commit();
 
       status = DELETED;
     }
     finally {
       getDatabase().endExclusiveLock();
     }
   }
 
   /**
    * Create a new object identical to this one.
    */
 
   public Persistent duplicated() throws AccessPoemException {
     assertNormalPersistent();
 
     Persistent it = (Persistent)clone();
 //
 //  we can't write this object yet as we need to populate it with new info, otherwise
 //  when we write it is likely cause problems with unique indexes.  so you need to call
 //  getTable().create(it) yourself
 //  getTable().create(it);
     return it;
   }
 
   // 
   // ===========
   //  Utilities
   // ===========
   // 
 
   /**
    * A string briefly describing the object for diagnostic purposes.  The name
    * of its table and its troid.
    */
 
   public String toString() {
     return getTable().getName() + "/" + troid();
   }
 
   /**
    * A string describing the object for the purposes of rendering it in lists
    * presented to the user.  Unless overridden, this returns the value picked
    * out by the designated `primary display column' of the table from which the
    * object comes.  If there is no such column, the object's troid is returned
    * (as a decimal string).
    */
 
   public String displayString(MelatiLocale locale, int style)
       throws AccessPoemException {
     Column displayColumn = getTable().displayColumn();
     if (displayColumn == null)
       return String.valueOf(getTroid());
     else
       return
           displayColumn.getType().stringOfCooked(displayColumn.getCooked(this),
 						 locale, style);
   }
 
   // 
   // ===============
   //  Support stuff
   // ===============
   // 
 
   public final int hashCode() {
     if (troid == null)
       throw new InvalidOperationOnFloatingPersistentPoemException(this);
 
     return getTable().hashCode() + troid().intValue();
   }
 
   public final boolean equals(Object object) {
     if (object == null || !(object instanceof Persistent))
       return false;
     else {
       Persistent other = (Persistent)object;
       return troid() != null && other.troid() == troid() &&
              other.getTable() == getTable();
     }
   }
 
   public synchronized void invalidate() {
     assertNormalPersistent();
     super.invalidate();
     extras = null;
   }
 
   protected Object clone() {
 
 //  to clone it you have to be able to read it
     assertCanRead();
     Persistent it;
     try {
       it = (Persistent)super.clone();
     }
     catch (CloneNotSupportedException e) {
       throw new UnexpectedExceptionPoemException(e);
     }
 
     it.extras = (Object[])extras().clone();
     it.reset();
     it.troid = null;
     it.status = NONEXISTENT;
 
     return it;
   }
 
   public void dump(PrintStream p) {
     p.println(getTable().getName() + "/" + troid());
     for (Enumeration f = getRecordDisplayFields(); f.hasMoreElements();) {
       p.print("  ");
       ((Field)f.nextElement()).dump(p);
       p.println();
     }
   }
 
   // these method is called directly following a record being written (inserted or updated)
   // override it to provide functionality sussequent to writes (or inserts), 
   // your persistent will have a Troid at this stage
   // this method is only called once!
   public void postWrite() {
   }
   // called only for an insert
   public void postInsert() {
   }
   // called only for a modification
   public void postModify() {
   }
 }

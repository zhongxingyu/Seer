 package org.melati.poem;
 
 public interface Persistable {
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
   * @see Persistent#getTroid()
    */
   public abstract Integer getTroid() throws AccessPoemException;
}

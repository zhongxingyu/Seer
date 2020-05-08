 package no.feide.moria.directory.index;
 
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * A serializable index implementation, used for offline generation of a new
  * index.
  */
 public class SerializableIndex
 implements Serializable, DirectoryManagerIndex {
 
     /**
      * Internal list of associations; that is, the mapping between logical ID
      * realms (as <code>String</code>s) - following the 'at' character - and
      * search base references (as <code>String</code> s).
      */
     private HashMap associations = new HashMap();
 
     /**
      * Internal list of exceptions to the associations; that is, explicitly
      * indexed logical IDs (as <code>String</code>s) to full external
      * references (as <code>String</code> arrays).
      */
     private HashMap exceptions = new HashMap();
 
 
     /**
      * Checks whether two index instances are equal.
      * @return <code>true</code> if two <code>SerializableIndex</code>
      *         objects are equal, otherwise <code>false</code>. To instances
      *         are equal if and only if their lists of associations and
      *         exceptions are equal. These structures are compared using the
      *         <code>AbstractMap.equals(Object)</code> method.
      * @see java.lang.Object#equals(java.lang.Object)
      * @see java.util.AbstractMap#equals(java.lang.Object)
      */
     public boolean equals(Object obj) {
 
         // Check class.
         if (obj.getClass() != this.getClass())
             return false;
         final SerializableIndex other = (SerializableIndex) obj;
 
         // Verify associations.
         if (!other.associations.equals(associations))
             return false;
 
         // Verify exceptions.
         if (!other.exceptions.equals(exceptions))
             return false;
 
         return true;
 
     }
 
 
     /**
      * Look up an element reference from the index based on its logical ID
      * (typically username). <br>
      * <br>
      * Note that looking up in the association list requires the logical ID to
      * be on the form <code>identificator-at-realm</code>, similar to an
      * email address. This is <em>not</em> a requirement for looking up
      * references in the exception list, and therefore the 'at' character is not
      * required in the logical ID.
      * @param id
      *            The logical identificator to look up.
      * @return One or more references matching the given identificator, or
      *         <code>null</code> if no such reference was found.
      * @see DirectoryManagerIndex#lookup(String)
      */
     public IndexedReference lookup(final String id) {
 
         // Sanity check.
         if (id == null)
             return null;
 
         // Do we have an explicit match? That is, an exception from the
         // association rule?
         if (exceptions.containsKey(id))
             return new IndexedReference(new String[] {(String)exceptions.get(id)}, true);
 
         // Extract the realm, with sanity check.
         int i = id.lastIndexOf('@');
        if (i < 0)
            return null;
        return new IndexedReference((String[])associations.get(id.substring(i)), false);
 
     }
 
 
     /**
      * Add a new realm-to-base association to the index. Any modification of the
      * index will result in any existing association with the same realm to be
      * appended with the new realm. <br>
      * <br>
      * Note that this method does <em>not</em> check for duplicate
      * associations (associations between one realm and two identical bases).
      * @param realm
      *            The realm (typically user realm) related to this base. Cannot
      *            be <code>null</code>.
      * @param base
      *            The association. In practical use this will be an LDAP search
      *            base similar to
      *            <code>ldap://some.ldap.server:636/dc=search,dc=base</code>.
      *            Cannot be <code>null</code>.
      * @throws IllegalArgumentException
      *             If either <code>realm</code> or <code>base</code> is
      *             <code>null</code>.
      */
     public void addAssociation(final String realm, final String base) {
 
         // Sanity checks.
         if (realm == null)
             throw new IllegalArgumentException("Realm cannot be NULL");
         if (base == null)
             throw new IllegalArgumentException("Base cannot be NULL");
 
         // New association or updating an existing?
         if (associations.containsKey(realm)) {
 
             // Update existing association.
             List oldBase = Arrays.asList((String[]) associations.get(realm));
             oldBase.add(base);
             associations.put(realm, oldBase.toArray(new String[] {}));
 
         } else {
 
             // Create new association.
             associations.put(realm, new String[] {base});
 
         }
 
     }
 
 
     /**
      * Add a new search exception (exception to the basic rule of realm-to-base
      * associations) to this index. Any modifications to an already existing
      * exception will result in the old references being replaced.
      * @param id
      *            The identificator for this exception, typically a user ID.
      *            Cannot be <code>null</code>.
      * @param reference
      *            The reference. In practical use this will be an LDAP element
      *            reference similar to
      *            <code>ldap://some.ldap.server:636/uid=id,dc=search,dc=base</code>.
      *            Cannot be <code>null</code>.
      * @throws IllegalArgumentException
      *             If either <code>id</code> or <code>reference</code> is
      *             <code>null</code>.
      */
     public void addException(final String id, final String reference) {
 
         // Sanity checks.
         if (id == null)
             throw new IllegalArgumentException("ID cannot be NULL");
         if (reference == null)
             throw new IllegalArgumentException("Reference cannot be NULL");
 
         exceptions.put(id, reference);
 
     }
 
 
     /**
      * Gives a string representation of the object, for visual debugging.
      * @return The object represented as a <code>String</code>; includes
      *         separate lists of associations and exceptions.
      */
     public String toString() {
 
         String s = "\tAssociations: " + associations.toString().replaceAll("], ", "\n\t               ");
         return s = s + "\tExceptions: " + exceptions.toString().replaceAll(", ", "\n\t             ");
 
     }
 
 }

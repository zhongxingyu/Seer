 /*
  * Copyright (c) 2004 UNINETT FAS
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option)
  * any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place - Suite 330, Boston, MA 02111-1307, USA.
  */
 
 package no.feide.moria.directory.index;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 
 /**
  * A serializable index implementation, used for offline generation of a new
  * index.
  */
 public class SerializableIndex
 implements Serializable, DirectoryManagerIndex {
 
     /**
      * Internal list of associations; that is, the mapping between logical ID
      * realms (as <code>String</code>s) - following the 'at' character - and
      * search base references (as <code>String</code> arrays).
      */
     private HashMap associations = new HashMap();
 
     /**
      * Internal list of exceptions to the associations; that is, explicitly
      * indexed logical IDs (as <code>String</code>s) to full external
      * references (as <code>String</code> s).
      */
     private HashMap exceptions = new HashMap();
 
     /**
      * Internal list of realms for each exception. The list should have the same
      * keys as <code>exceptions</code>, but the elements of this list give
      * the explicit realm (as <code>String</code>s) for each exception.
      */
     private HashMap realms = new HashMap();
 
 
     /**
      * Checks whether two index instances are equal. <br>
      * <br>
      * Note that for convenience (and/or laziness) this method relies on the
      * <code>String</code> representation as given by <code>toString()</code>.
      * @return <code>true</code> if two <code>SerializableIndex</code>
      *         objects are equal, otherwise <code>false</code>. To instances
      *         are equal if and only if their lists of associations, exceptions
      *         and realms are equal.
      * @see java.lang.Object#equals(java.lang.Object)
      * @see #toString()
      */
     public boolean equals(Object obj) {
 
         // Check class.
         if (obj.getClass() != this.getClass())
             return false;
         final SerializableIndex other = (SerializableIndex) obj;
 
         // Check associations. Normal equals(...) doesn't work here.
         if (!associations.keySet().equals(other.associations.keySet()))
             return false;
         Iterator keys = associations.keySet().iterator();
         while (keys.hasNext()) {
             String key = (String) keys.next();
             String[] values = (String[]) associations.get(key);
             ArrayList myValues = new ArrayList(values.length);
             for (int i = 0; i < values.length; i++)
                 myValues.add(values[i]);
             values = (String[]) other.associations.get(key);
             ArrayList otherValues = new ArrayList(values.length);
             for (int i = 0; i < values.length; i++)
                 otherValues.add(values[i]);
             if (!myValues.equals(otherValues))
                 return false;
         }
 
         // Check exceptions and realms; much simple data structure.
         if (!exceptions.equals(other.exceptions))
             return false;
         if (!realms.equals(other.realms))
             return false;
 
         // We're okay.
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
      * @see DirectoryManagerIndex#getReferences(String)
      */
     public IndexedReference[] getReferences(final String id) {
 
         // Sanity check.
         if (id == null)
             return null;
 
         ArrayList newReferences = new ArrayList();
 
         // Do we have an explicit match? That is, an exception from the
         // association rule?
         if (exceptions.containsKey(id))
             newReferences.add(new IndexedReference(new String[] {(String) exceptions.get(id)}, true));
 
         // Extract the realm, with sanity check.
         int i = id.lastIndexOf('@');
         if ((i > 0) && (associations.containsKey(id.substring(i + 1))))
             newReferences.add(new IndexedReference((String[]) associations.get(id.substring(i + 1)), false));
 
         // Did we find any references?
         if (newReferences.size() == 0)
             return null;
         else
             return (IndexedReference[]) newReferences.toArray(new IndexedReference[] {});
 
     }
 
 
     /**
      * Look up which realm a given identificator belongs to.
      * @param id
      *            The logical identificator to get realm for.
      * @return The resolved realm for this identificator, or <code>null</code>
      *         if no such realm could be found.
      * @see DirectoryManagerIndex#getRealm(String)
      */
     public String getRealm(String id) {
 
        // Do we have an exception matching this identificator with an explicit
         // realm?
         if (realms.containsKey(id))
             return (String) realms.get(id);
 
        // Do we have any associations for this realm?
         int i = id.lastIndexOf('@');
         if ((i > 0) && (associations.containsKey(id.substring(i + 1))))
            return id.substring(i + 1);
 
         // No exception/realm and no association.
         return null;
 
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
             ArrayList bases = new ArrayList(Arrays.asList((String[]) associations.get(realm)));
             bases.add(base);
             associations.put(realm, (String[]) bases.toArray(new String[] {}));
 
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
      * @param realm
      *            The actual realm of the reference, which may not be given by
      *            the identificator (on the form <i>user@realm </i>, for
      *            example). Since moving between realms while keeping an
      *            unchanged identificator is possible, this must be accounted
      *            for.
      * @throws IllegalArgumentException
      *             If either <code>id</code> or <code>reference</code> is
      *             <code>null</code>.
      */
     public void addException(final String id, final String reference, final String realm) {
 
         // Sanity checks.
         if (id == null)
             throw new IllegalArgumentException("ID cannot be NULL");
         if (reference == null)
             throw new IllegalArgumentException("Reference cannot be NULL");
 
         exceptions.put(id, reference);
         realms.put(id, realm);
 
     }
 
 
     /**
      * Gives a string representation of the object, for visual debugging.
      * @return The object represented as a <code>String</code>; includes
      *         separate lists of associations and exceptions.
      */
     public String toString() {
 
         // Associations.
         String s = "\tAssociations: {";
         Iterator associationKeys = associations.keySet().iterator();
         while (associationKeys.hasNext()) {
             String[] bases = (String[]) associations.get(associationKeys.next());
             for (int i = 0; i < bases.length; i++) {
                 s = s + bases[i];
                 if (i < bases.length - 1)
                     s = s + "\n\t               ";
                 else
                     s = s + '}';
             }
         }
 
         // Exceptions.
         return s = s + "\n\tExceptions: " + exceptions.toString().replaceAll(", ", "\n\t             ");
 
     }
 
 }

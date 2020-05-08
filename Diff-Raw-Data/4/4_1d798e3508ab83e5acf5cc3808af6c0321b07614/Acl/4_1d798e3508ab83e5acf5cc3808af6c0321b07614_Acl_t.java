 /* **********************************************************************
     Copyright 2006 Rensselaer Polytechnic Institute. All worldwide rights reserved.
 
     Redistribution and use of this distribution in source and binary forms,
     with or without modification, are permitted provided that:
        The above copyright notice and this permission notice appear in all
         copies and supporting documentation;
 
         The name, identifiers, and trademarks of Rensselaer Polytechnic
         Institute are not used in advertising or publicity without the
         express prior written permission of Rensselaer Polytechnic Institute;
 
     DISCLAIMER: The software is distributed" AS IS" without any express or
     implied warranty, including but not limited to, any implied warranties
     of merchantability or fitness for a particular purpose or any warrant)'
     of non-infringement of any current or pending patent rights. The authors
     of the software make no representations about the suitability of this
     software for any particular purpose. The entire risk as to the quality
     and performance of the software is with the user. Should the software
     prove defective, the user assumes the cost of all necessary servicing,
     repair or correction. In particular, neither Rensselaer Polytechnic
     Institute, nor the authors of the software are liable for any indirect,
     special, consequential, or incidental damages related to the software,
     to the maximum extent the law permits.
 */
 package edu.rpi.cmt.access;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.TreeMap;
 
 /** Object to represent an acl for a calendar entity or service. We should
  * have one of these per session - or perhaps thread - and lock it during
  * processing.
  *
  * <p>The objects represented by Privileges will assume transient states
  * during processing.
  *
  * <p>An ACL is a set of ACEs which are stored as an encoded character
  * array. These aces should be sorted to facilitate merging and to
  * allow us to possibly only process as much of the acl as is necessary.
  *
  * <p>For example, owner access should come first, it's first in the test and
  * we can avoid decoding an ace which doesn't include any owner access.
  *
  * <p>The whoTypexxx declarations in Ace define the order of Ace types. In
  * addition, any aces that contain names should be in ascending alphabetic
  * order.
  *
  * <p>In the list of Ace there can only be one entry per AceWho so we can
  * represent the list as a SortedMap. Replacement then becomes easy.
  *
  *  @author Mike Douglass   douglm - rpi.edu
  */
 public class Acl extends EncodedAcl implements PrivilegeDefs {
   boolean debug;
 
   private TreeMap<AceWho, Ace> aces;
 
   /** Used while evaluating access */
 
   /** Constructor
    *
    */
   public Acl() {
     this(false);
   }
 
   /** Constructor
    *
    * @param debug
    */
   public Acl(boolean debug) {
     this.debug = debug;
   }
 
   /** Turn debugging on/off
    *
    * @param val
    */
   public void setDebug(boolean val) {
     debug = val;
   }
 
   /** Remove all ace entries
    *
    */
   public void clear() {
     aces = null;
   }
 
   /** Result of evaluating access to an object for a principal
    */
   public static class CurrentAccess implements Serializable {
     /** The Acl used to evaluate the access. We should not necessarily
      * make this available to the client.
      */
     public Acl acl;
 
     /**  Allowed access for each privilege type
      * @see PrivilegeDefs
      */
     public PrivilegeSet privileges = null;
 
     /** Privileges desired */
     public Privilege[] desiredAccess;
 
     /** Was it succesful */
     public boolean accessAllowed;
 
     public String toString() {
       StringBuffer sb = new StringBuffer("CurrentAccess{");
       sb.append("acl=");
       sb.append(acl);
 
       sb.append("accessAllowed=");
       sb.append(accessAllowed);
       sb.append("}");
 
       return sb.toString();
     }
   }
 
   /** Evaluating an ACL
    *
    * <p>The process of evaluating access is as follows:
    *
    * <p>For an unauthenticated (guest) user we look for an entry with an
    * unauthenticated 'who' field. If none exists access is denied othewise the
    * indicated privileges are used to determine access.
    *
    * <p>If the principal is authenticated there are a number of steps in the process
    * which are executed in the following order:
    *
    * <ol>
    * <li>If the principal is the owner then use the given access or the default.</li>
    *
    * <li>If there are specific ACEs for the user use the merged access. </li>
    *
    * <li>Find all group entries for the given user's groups. If there is more than
    * one combine them with the more permissive taking precedence, e.g
    * write allowed overrides write denied
    * <p>If any group entries were found we're done.</li>
    *
    * <li>if there is an 'other' entry (i.e. not Owner) use that.</li>
    *
    * <li>if there is an authenticated entry use that.</li>
    *
    * <li>Otherwise apply defaults - for the owner full acccess, for any others no
    * access</li>
    *
    * @param who
    * @param owner
    * @param how
    * @param acl
    * @param filter    if not null specifies maximum access
    * @return CurrentAccess   access + allowed/disallowed
    * @throws AccessException
    */
   public CurrentAccess evaluateAccess(AccessPrincipal who, String owner,
                                       Privilege[] how, char[] acl,
                                       PrivilegeSet filter)
           throws AccessException {
     boolean authenticated = !who.getUnauthenticated();
     boolean isOwner = false;
     CurrentAccess ca = new CurrentAccess();
     ca.desiredAccess = how;
     ca.acl = this;
 
     decode(acl);
 
     if (authenticated) {
       isOwner = who.getAccount().equals(owner);
     }
 
     StringBuffer debugsb = null;
 
     if (debug) {
       debugsb = new StringBuffer("Check access for '");
       debugsb.append(new String(acl));
       debugsb.append("' with authenticated = ");
       debugsb.append(authenticated);
       debugsb.append(" isOwner = ");
       debugsb.append(isOwner);
     }
 
     getPrivileges: {
       if (!authenticated) {
         ca.privileges = Ace.findMergedPrivilege(this, null, Ace.whoTypeUnauthenticated);
 
         if (ca.privileges == null) {
           // All might be available
           ca.privileges = Ace.findMergedPrivilege(this, null, Ace.whoTypeAll);
         }
 
         break getPrivileges;
       }
 
       if (isOwner) {
         ca.privileges = Ace.findMergedPrivilege(this, null, Ace.whoTypeOwner);
         if (ca.privileges == null) {
           ca.privileges = PrivilegeSet.makeDefaultOwnerPrivileges();
         }
         if (debug) {
           debugsb.append("... For owner got: " + ca.privileges);
         }
 
         break getPrivileges;
       }
 
       // Not owner - look for user
       ca.privileges = Ace.findMergedPrivilege(this, who.getAccount(), Ace.whoTypeUser);
       if (ca.privileges != null) {
         if (debug) {
           debugsb.append("... For user got: " + ca.privileges);
         }
 
         break getPrivileges;
       }
 
       // No specific user access - look for group access
 
       if (who.getGroupNames() != null) {
         for (String group: who.getGroupNames()) {
           if (debug) {
             debugsb.append("...Try access for group " + group);
           }
           PrivilegeSet privs = Ace.findMergedPrivilege(this, group, Ace.whoTypeGroup);
           if (privs != null) {
             ca.privileges = PrivilegeSet.mergePrivileges(ca.privileges, privs, false);
           }
         }
       }
 
       if (ca.privileges != null) {
         if (debug) {
           debugsb.append("...For groups got: " + ca.privileges);
         }
 
         break getPrivileges;
       }
 
       // "other" access set?
       ca.privileges = Ace.findMergedPrivilege(this, null, Ace.whoTypeOther);
 
       if (ca.privileges == null) {
         // All might be available
         ca.privileges = Ace.findMergedPrivilege(this, null, Ace.whoTypeAll);
       }
 
       if (ca.privileges != null) {
         if (debug) {
           debugsb.append("...For other got: " + ca.privileges);
         }
 
         break getPrivileges;
       }
     } // getPrivileges
 
     if (ca.privileges == null) {
       if (debug) {
         debugMsg(debugsb.toString() + "...Check access denied (noprivs)");
       }
       return ca;
     }
 
     ca.privileges.setUnspecified(isOwner);
 
     if (filter != null) {
       ca.privileges.filterPrivileges(filter);
     }
 
     if (how.length == 0) {
       // Means any access will do
 
      if (debug) {
        debugMsg(debugsb.toString() + "...Check access allowed (any requested)");
      }
       ca.accessAllowed = ca.privileges.getAnyAllowed();
       return ca;
     }
 
     for (int i = 0; i < how.length; i++) {
       char priv = ca.privileges.getPrivilege(how[i].getIndex());
 
       if ((priv != allowed) && (priv != allowedInherited)) {
         if (debug) {
           debugsb.append("...Check access denied (!allowed) ");
           debugsb.append(ca.privileges);
           debugMsg(debugsb.toString());
         }
         return ca;
       }
     }
 
     if (debug) {
       debugMsg(debugsb.toString() + "...Check access allowed");
     }
 
     ca.accessAllowed = true;
     return ca;
   }
 
   /** Return the ace collection for previously decoded access
    *
    * @return Collection ace collection for previously decoded access
    * @throws AccessException
    */
   public Collection<Ace> getAces() throws AccessException {
     return aces.values();
   }
 
   /** Add an entry to the Acl
    *
    * @param val Ace to add
    */
   public void addAce(Ace val) {
     if (aces == null) {
       aces = new TreeMap<AceWho, Ace>();
     }
 
     aces.put(val.getWho(), val);
   }
 
   /** Set to default access
    *
    */
   public void defaultAccess() {
     aces = null; // reset
 
     addAce(new Ace(null, false, Ace.whoTypeOwner,
                    Privileges.makePriv(Privileges.privAll)));
 
     addAce(new Ace(null, false, Ace.whoTypeOther,
                    Privileges.makePriv(Privileges.privNone)));
   }
 
   /** Remove access for a given 'who' entry
    *
    * @param who
    * @return boolean true if removed
    */
   public boolean removeWho(AceWho who) {
     if (aces == null) {
       return false;
     }
 
     return aces.remove(who) != null;
   }
 
   /* ====================================================================
    *                 Decoding methods
    * ==================================================================== */
 
 
   /** Given an encoded acl convert to an ordered sequence of fully expanded
    * ace objects.
    *
    * @param val String val to decode
    * @throws AccessException
    */
   public void decode(String val) throws AccessException {
     decode(val.toCharArray());
   }
 
   /** Given an encoded acl convert to an ordered sequence of fully expanded
    * ace objects.
    *
    * @param val char[] val to decode
    * @throws AccessException
    */
   public void decode(char[] val) throws AccessException {
     setEncoded(val);
 
     if (empty()) {
       defaultAccess();
     } else {
       aces = new TreeMap<AceWho, Ace>();
 
       while (hasMore()) {
         Ace ace = new Ace();
 
         ace.decode(this, true);
 
         aces.put(ace.getWho(), ace);
       }
     }
   }
 
   /** Given an encoded acl merge it into this objects ace list. This process
    * should be carried out moving up from the end of the path to the root as
    * entries will only be added to the merged list if the notWho + whoType + who
    * do not match.
    *
    * <p>The inherited flag will be set on all merged Ace objects.
    *
    * <p>For example, if we have the path structure
    * <pre>
    *     /user                 owner=sys,access=write-content owner
    *        /jeb               owner=jeb,access=write-content owner
    *           /calendar       owner=jeb    no special access
    *           /rocalendar     owner=jeb    read owner
    * </pre>
    * then, while evaluating the access for rocalendar we start at rocalendar
    * and move up the tree. The "read owner" access on rocalendar overrides any
    * access we find further up the tree, e.g. "write-content owner"
    *
    * <p>While evaluating the access for calendar we start at calendar
    * and move up the tree. There is no overriding access so the final access is
    * "write-content owner" inherited from /user/jeb
    *
    * <p>Also note the encoded value will not reflect the eventual Acl.
    *
    * <p>And what did that mean? I think I meant that we can derive the acl for
    * an entity from the merged result.
    *
    * @param val char[] val to decode and merge
    * @param path   path of current entity
    * @throws AccessException
    */
   public void merge(char[] val, String path) throws AccessException {
     EncodedAcl ea = new EncodedAcl();
     ea.setEncoded(val);
 
     if (ea.empty()) {
       return;
     }
 
     while (ea.hasMore()) {
       Ace ace = new Ace();
 
       ace.decode(ea, true);
       if (!ace.getInherited()) {
         ace.setInherited(true);
         ace.setInheritedFrom(path);
       }
 
       if (aces == null) {
         aces = new TreeMap<AceWho, Ace>();
       }
 
       /* If we don't have this who yet then add it to the result. Otherwise the
        * who from lower down takes precedence.
        */
       if (aces.get(ace.getWho()) == null) {
         aces.put(ace.getWho(), ace);
       }
     }
   }
 
   /* * Given a decoded acl merge it into this objects ace list. This process
    * should be carried out moving up from the end of the path to the root as
    * entries will only be added to the merged list if the notWho + whoType + who
    * do not match.
    *
    * <p>The inherited flag will be set on all merged Ace objects.
    * <p>XXX Note that reuse of Acls for merges invalidates the inherited flag.
    * I think it's only used for display and acl modification purposes so
    * shouldn't affect normal access control checks.
    *
    * <p>Also note the encoded value will not reflect the eventual Acl.
    *
    * @param val Acl to merge
    * @throws AccessException
    * /
   public void merge(Acl val) throws AccessException {
     Collection<Ace> valAces = val.getAces();
 
     if (valAces == null) {
       return;
     }
 
     for (Ace ace: valAces) {
       ace.setInherited(true);
 
       if (!aces.contains(ace)) {
         aces.add(ace);
       }
     }
   }*/
 
   /* ====================================================================
    *                 Encoding methods
    * ==================================================================== */
 
   /** Encode this object after manipulation or creation. Inherited entries
    * will be skipped.
    *
    * @return char[] encoded value
    * @throws AccessException
    */
   public char[] encode() throws AccessException {
     startEncoding();
 
     if (aces == null) {
       return null;
     }
 
     for (Ace ace: aces.values()) {
       if (!ace.getInherited()) {
         ace.encode(this);
       }
     }
 
     return getEncoding();
   }
 
   /** Encode this object after manipulation or creation. Inherited entries
    * will be skipped. Returns null for no aces
    *
    * @return String encoded value or null
    * @throws AccessException
    */
   public String encodeStr() throws AccessException {
     startEncoding();
 
     char[] encoded = encode();
     if (encoded == null) {
        return null;
     }
 
     return new String(encoded);
   }
 
   /** Encode this object after manipulation or creation. Inherited entries
    * will NOT be skipped.
    *
    * @return char[] encoded value
    * @throws AccessException
    */
   public char[] encodeAll() throws AccessException {
     startEncoding();
 
     if (aces == null) {
       return null;
     }
 
     for (Ace ace: aces.values()) {
       ace.encode(this);
     }
 
     return getEncoding();
   }
 
   /* ====================================================================
    *                   Object methods
    * ==================================================================== */
 
   /** Provide a string representation for user display - this should
    * use a localized resource and be part of a display level.
    *
    * @return String representation
    */
   public String toUserString() {
     StringBuffer sb = new StringBuffer();
 
     try {
       decode(getEncoded());
 
       for (Ace ace: aces.values()) {
         sb.append(ace.toString());
         sb.append(" ");
       }
     } catch (Throwable t) {
       error(t);
       sb.append("Decode exception " + t.getMessage());
     }
 
     return sb.toString();
   }
 
   public String toString() {
     StringBuffer sb = new StringBuffer();
 
     sb.append("Acl{");
     if (!empty()) {
       sb.append("encoded=[");
 
       rewind();
       while (hasMore()) {
         sb.append(getChar());
       }
       sb.append("] ");
 
       rewind();
 
       try {
         if (aces == null) {
           decode(getEncoded());
         }
 
         for (Ace ace: aces.values()) {
           sb.append("\n");
           sb.append(ace.toString());
         }
       } catch (Throwable t) {
         error(t);
         sb.append("Decode exception " + t.getMessage());
       }
     }
     sb.append("}");
 
     return sb.toString();
   }
 
   /** For testing
    *
    * @param args
    */
   public static void main(String[] args) {
     try {
       Acl acl = new Acl();
       acl.decode(args[0]);
 
       System.out.println(acl.toString());
     } catch (Throwable t) {
       t.printStackTrace();
     }
   }
 }
 

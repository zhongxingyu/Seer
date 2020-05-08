 /*
  Copyright (c) 2000-2005 University of Washington.  All rights reserved.
 
  Redistribution and use of this distribution in source and binary forms,
  with or without modification, are permitted provided that:
 
    The above copyright notice and this permission notice appear in
    all copies and supporting documentation;
 
    The name, identifiers, and trademarks of the University of Washington
    are not used in advertising or publicity without the express prior
    written permission of the University of Washington;
 
    Recipients acknowledge that this distribution is made available as a
    research courtesy, "as is", potentially with defects, without
    any obligation on the part of the University of Washington to
    provide support, services, or repair;
 
    THE UNIVERSITY OF WASHINGTON DISCLAIMS ALL WARRANTIES, EXPRESS OR
    IMPLIED, WITH REGARD TO THIS SOFTWARE, INCLUDING WITHOUT LIMITATION
    ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
    PARTICULAR PURPOSE, AND IN NO EVENT SHALL THE UNIVERSITY OF
    WASHINGTON BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL
    DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR
    PROFITS, WHETHER IN AN ACTION OF CONTRACT, TORT (INCLUDING
    NEGLIGENCE) OR STRICT LIABILITY, ARISING OUT OF OR IN CONNECTION WITH
    THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 /* **********************************************************************
     Copyright 2005 Rensselaer Polytechnic Institute. All worldwide rights reserved.
 
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
 package edu.rpi.cct.uwcal.access;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.TreeSet;
 
 import org.apache.log4j.Logger;
 
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
  *  @author Mike Douglass   douglm @ rpi.edu
  */
 public class Acl extends EncodedAcl implements PrivilegeDefs {
   boolean debug;
 
   private TreeSet aces;
 
   private transient Logger log;
 
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
 
         break getPrivileges;
       }
 
       if (isOwner) {
         ca.privileges = Ace.findMergedPrivilege(this, null, Ace.whoTypeOwner);
         if (ca.privileges == null) {
           ca.privileges = PrivilegeSet.makeDefaultOwnerPrivileges();
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
         Iterator it = who.getGroupNames().iterator();
 
         while (it.hasNext()) {
           String group = (String)it.next();
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
 
       ca.accessAllowed = ca.privileges.getAnyAllowed();
       return ca;
     }
 
     for (int i = 0; i < how.length; i++) {
       char priv = ca.privileges.getPrivilege(how[i].getIndex());
 
       if ((priv != allowed) && (priv != allowedInherited)) {
         if (debug) {
           debugMsg(debugsb.toString() + "...Check access denied (!allowed)");
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
 
   /**
    * @param acl
    * @return Collection of Ace's representing the acls
    * @throws AccessException
    */
   public Collection getAccess(char[] acl) throws AccessException {
     decode(acl);
 
     return aces;
   }
 
   /** Set the ace collection for this acl
    *
    * @param val    Collection of aces
    * @throws AccessException
    */
   public void setAces(Collection val) throws AccessException {
     aces = (TreeSet)val;
   }
 
   /** Return the ace collection for previously decoded access
    *
    * @return Collection ace collection for previously decoded access
    * @throws AccessException
    */
   public Collection getAces() throws AccessException {
     return aces;
   }
 
   /** Add an entry to the Acl
    *
    * @param val Ace to add
    */
   public void addAce(Ace val) {
     if (aces == null) {
       aces = new TreeSet();
     }
 
     aces.add(val);
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
 
   /* * Remove access for a given 'who' entry
    *
    * @param who
    * @param notWho
    * @param whoType
    * @return boolean true if removed
    * /
   public boolean removeWho(String who, boolean notWho, int whoType) {
     if (aces == null) {
       return false;
     }
 
     return aces.remove(new Ace(who, notWho, whoType, (PrivilegeSet)null));
   }*/
 
   /** Remove access for a given 'who' entry
    *
    * @param whoDef
    * @return boolean true if removed
    */
   public boolean removeWho(Ace whoDef) {
     if (aces == null) {
       return false;
     }
 
     /* We're called to remove any 'who' entries before adding. Usually there will
      * be nothing to remove (I assume) so check first.
      *
      * We can't remove as we check or we get concurrent mod exception.
      */
     boolean remove = false;
     Iterator it = aces.iterator();
     while (it.hasNext()) {
       Ace ace = (Ace)it.next();
 
       if (ace.compareWho(whoDef) == 0) {
         remove = true;
         break;
       }
     }
 
     if (!remove) {
       return false;
     }
 
     TreeSet newAces = new TreeSet();
     it = aces.iterator();
     while (it.hasNext()) {
       Ace ace = (Ace)it.next();
 
       if (ace.compareWho(whoDef) != 0) {
         newAces.add(ace);
       }
     }
 
     aces = newAces;
     return remove;
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
     TreeSet ts = new TreeSet();
 
     setEncoded(val);
 
     if (empty()) {
       defaultAccess();
     } else {
       while (hasMore()) {
         Ace ace = new Ace();
 
         ace.decode(this, true);
 
         ts.add(ace);
       }
 
       aces = ts;
     }
   }
 
   /** Given an encoded acl merge it into this objects ace list. This process
    * should be carried out moving up from the end of the path to the root as
    * entries will only be added to the merged list if the notWho + whoType + who
    * do not match.
    *
    * <p>The inherited flag will be set on all merged Ace objects.
    *
    * <p>Also note the encoded value will not reflect the eventual Acl.
    *
    * @param val char[] val to decode and merge
    * @throws AccessException
    */
   public void merge(char[] val) throws AccessException {
    setEncoded(val);
 
    if (empty()) {
       return;
     }
 
    while (hasMore()) {
       Ace ace = new Ace();
 
      ace.decode(this, true);
       ace.setInherited(true);
 
       if (aces == null) {
         aces = new TreeSet();
         aces.add(ace);
       } else if (!aces.contains(ace)) {
         aces.add(ace);
       }
     }
   }
 
   /** Given a decoded acl merge it into this objects ace list. This process
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
    */
   public void merge(Acl val) throws AccessException {
     TreeSet valAces = (TreeSet)val.getAces();
 
     if (valAces == null) {
       return;
     }
 
     Iterator it = valAces.iterator();
     while (it.hasNext()) {
       Ace ace = (Ace)it.next();
 
       ace.setInherited(true);
 
       if (!aces.contains(ace)) {
         aces.add(ace);
       }
     }
   }
 
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
 
     if (aces != null) {
       Iterator it = aces.iterator();
       while (it.hasNext()) {
         Ace ace = (Ace)it.next();
 
         if (!ace.getInherited()) {
           ace.encode(this);
         }
       }
     }
 
     return getEncoding();
   }
 
   /** Encode this object after manipulation or creation. Inherited entries
    * will NOT be skipped.
    *
    * @return char[] encoded value
    * @throws AccessException
    */
   public char[] encodeAll() throws AccessException {
     startEncoding();
 
     if (aces != null) {
       Iterator it = aces.iterator();
       while (it.hasNext()) {
         Ace ace = (Ace)it.next();
 
         ace.encode(this);
       }
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
       Collection aces = getAccess(getEncoded());
 
       Iterator it = aces.iterator();
       while (it.hasNext()) {
         Ace ace = (Ace)it.next();
 
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
 
     sb.append("AclVO{");
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
 
         Iterator it = aces.iterator();
         while (it.hasNext()) {
           Ace ace = (Ace)it.next();
 
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
 
   private Logger getLog() {
     if (log == null) {
       log = Logger.getLogger(this.getClass());
     }
 
     return log;
   }
 
   private void debugMsg(String msg) {
     getLog().debug(msg);
   }
 
   private void error(Throwable t) {
     getLog().error(this, t);
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
 

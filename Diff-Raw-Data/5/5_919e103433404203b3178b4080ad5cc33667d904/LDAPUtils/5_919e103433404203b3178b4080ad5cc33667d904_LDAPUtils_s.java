 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: LDAPUtils.java,v 1.2 2007-04-20 07:14:56 rarcot Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.common;
 
 import java.io.DataInputStream;
 import java.io.IOException;
 import netscape.ldap.LDAPAttribute;
 import netscape.ldap.LDAPAttributeSet;
 import netscape.ldap.LDAPConnection;
 import netscape.ldap.LDAPEntry;
 import netscape.ldap.LDAPException;
 import netscape.ldap.LDAPModification;
 import netscape.ldap.LDAPModificationSet;
 import netscape.ldap.util.LDIF;
 import netscape.ldap.util.LDIFAddContent;
 import netscape.ldap.util.LDIFAttributeContent;
 import netscape.ldap.util.LDIFContent;
 import netscape.ldap.util.LDIFModifyContent;
 import netscape.ldap.util.LDIFRecord;
 
 public class LDAPUtils {
     
     private LDAPUtils() {
     }
     
     /**
      * Creates LDAP schema from LDIF file.
      *
      * @param file file containing LDIF entries.
      * @param ld LDAP Connection.
      */
     public static void createSchemaFromLDIF(
         String file, 
         LDAPConnection ld
     ) throws IOException, LDAPException {
         createSchemaFromLDIF(new LDIF(file), ld);
     }
 
 
     /**
      * Creates LDAP schema from LDIF file.
      *
      * @param stream Data input stream containing LDIF entries.
      * @param ld LDAP Connection.
      */
     public static void createSchemaFromLDIF(
         DataInputStream stream, 
         LDAPConnection ld
     ) throws IOException, LDAPException {
         createSchemaFromLDIF(new LDIF(stream), ld);
     }
     
 
     /**
      * Creates LDAP schema from LDIF file.
      *
      * @param ldif LDIF object.
      * @param ld LDAP Connection.
      */
     public static void createSchemaFromLDIF(
         LDIF ldif,
         LDAPConnection ld
     ) throws IOException, LDAPException {
         for(LDIFRecord rec = ldif.nextRecord(); rec != null; 
             rec = ldif.nextRecord()
         ) {
             LDIFContent content = null;
             String DN = null;
             
             try {
                 content = rec.getContent();
                 DN = rec.getDN();
 
                 if (content instanceof LDIFModifyContent) {
                     ld.modify(DN, 
                         ((LDIFModifyContent)content).getModifications());
                     
                 } else if ((content instanceof LDIFAttributeContent) ||
                     (content instanceof LDIFAddContent)
                     ) {
                     LDAPAttributeSet attrSet = null;
                     LDAPAttribute[] attrs =
                         (content instanceof LDIFAttributeContent) ?
                             ((LDIFAttributeContent)content).getAttributes():
                             ((LDIFAddContent)content).getAttributes();
                     LDAPEntry amEntry = new LDAPEntry(DN,
                         new LDAPAttributeSet(attrs));
                     ld.add(amEntry);
                 }
                 
                 
             } catch (LDAPException e) {
                 switch (e.getLDAPResultCode()) {
                     case LDAPException.ATTRIBUTE_OR_VALUE_EXISTS:
                        throw e;
                     case LDAPException.NO_SUCH_ATTRIBUTE:
 		        // Ignore some attributes need to be deleted if present
                         break; 
                     case LDAPException.ENTRY_ALREADY_EXISTS:
                         LDAPModificationSet modSet =
                             new LDAPModificationSet();
                         LDAPAttribute[] attrs =
                             (content instanceof LDIFAttributeContent) ?
                                 ((LDIFAttributeContent)content).getAttributes():
                                 ((LDIFAddContent)content).getAttributes();
                         
                         for (int i = 0; i < attrs.length; i++) {
                             modSet.add(LDAPModification.ADD, attrs[i]);
                         }
                         try {
                             ld.modify(DN, modSet);
                         } catch (LDAPException ex) {
                             //Ignore the exception
                         }
                         break;
                     default:
                         // let it thru
                 }
             }
         }
     }
 }

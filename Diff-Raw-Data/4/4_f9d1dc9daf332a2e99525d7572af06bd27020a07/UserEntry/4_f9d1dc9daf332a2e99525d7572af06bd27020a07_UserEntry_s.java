 /*
  Created as part of the StratusLab project (http://stratuslab.eu),
  co-funded by the European Commission under the Grant Agreement
  INFSO-RI-261552.
 
  Copyright (c) 2011, Centre National de la Recherche Scientifique (CNRS)
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  */
 package eu.stratuslab.registration.data;
 
 import static eu.stratuslab.registration.data.UserAttribute.PASSWORD;
 import static eu.stratuslab.registration.data.UserAttribute.UID;
 
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import javax.naming.AuthenticationException;
 import javax.naming.Context;
 import javax.naming.NameAlreadyBoundException;
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.naming.directory.Attribute;
 import javax.naming.directory.Attributes;
 import javax.naming.directory.BasicAttributes;
 import javax.naming.directory.DirContext;
 import javax.naming.directory.InitialDirContext;
 import javax.naming.directory.InvalidAttributesException;
 import javax.naming.directory.SearchResult;
 
 import org.restlet.data.Form;
 import org.restlet.data.Parameter;
 import org.restlet.data.Status;
 import org.restlet.resource.ResourceException;
 
 import eu.stratuslab.registration.utils.FormUtils;
 import eu.stratuslab.registration.utils.LdapConfig;
 
 public final class UserEntry {
 
     private static final String DATABASE_CONNECT_ERROR = "error contacting database";
 
     private static final Logger LOGGER = Logger.getLogger("org.restlet");
 
     private UserEntry() {
 
     }
 
     public static String createUser(Form form, LdapConfig ldapEnv) {
 
         checkUserCreateFormCorrect(form);
 
         String uid = form.getFirstValue(UID.key);
         String dn = UID.key + "=" + uid;
 
         // Get a connection from the pool.
         DirContext ctx = null;
 
         try {
 
             ctx = new InitialDirContext(ldapEnv);
 
             // Copy all of the attributes.
             Attributes attrs = new BasicAttributes(true);
             for (Parameter parameter : form) {
                 attrs.put(parameter.getName(), parameter.getValue());
             }
 
             ctx.createSubcontext(dn, attrs);
 
         } catch (NameAlreadyBoundException e) {
 
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                     "username (" + uid + ") already exists");
 
         } catch (InvalidAttributesException e) {
 
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     "incomplete user entry");
 
         } catch (AuthenticationException e) {
 
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     DATABASE_CONNECT_ERROR);
 
         } catch (NamingException e) {
 
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     DATABASE_CONNECT_ERROR);
 
         } finally {
             freeContext(ctx);
         }
 
         return uid;
     }
 
     public static void checkUserCreateFormCorrect(Form form) {
 
         FormUtils.allCreateAttributesExist(form);
 
         FormUtils.canonicalizeCertificateDN(form);
 
         String newPassword = FormUtils.checkNewPasswords(form);
 
         FormUtils.setNewPasswordInForm(null, newPassword, form);
 
         FormUtils.stripNonLdapAttributes(form);
     }
 
     public static void updateUser(Form form, LdapConfig ldapEnv) {
 
         String uid = form.getFirstValue(UID.key);
 
         Form[] forms = validateUserUpdateForm(form, ldapEnv);
 
         rawUpdateUser(uid, DirContext.ADD_ATTRIBUTE, forms[0], ldapEnv);
         rawUpdateUser(uid, DirContext.REMOVE_ATTRIBUTE, forms[1], ldapEnv);
         rawUpdateUser(uid, DirContext.REPLACE_ATTRIBUTE, forms[2], ldapEnv);
 
     }
 
     public static void rawUpdateUser(String uid, int ldapAction, Form form,
             LdapConfig ldapEnv) {
 
         String dn = UID.key + "=" + uid;
 
         // Get a connection from the pool.
         DirContext ctx = null;
 
         try {
 
             ctx = new InitialDirContext(ldapEnv);
 
             // Copy all of the attributes.
             Attributes attrs = new BasicAttributes(true);
             for (Parameter parameter : form) {
                 attrs.put(parameter.getName(), parameter.getValue());
             }
 
             if (attrs.size() > 0) {
                 ctx.modifyAttributes(dn, ldapAction, attrs);
             }
 
         } catch (InvalidAttributesException e) {
 
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     "incomplete user entry");
 
         } catch (AuthenticationException e) {
 
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     DATABASE_CONNECT_ERROR);
 
         } catch (NamingException e) {
 
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     DATABASE_CONNECT_ERROR);
 
         } finally {
             freeContext(ctx);
         }
 
     }
 
     public static Form[] validateUserUpdateForm(Form updateForm,
             LdapConfig ldapEnv) {
 
         FormUtils.allUpdateAttributesExist(updateForm);
 
         FormUtils.canonicalizeCertificateDN(updateForm);
 
         String uid = updateForm.getFirstValue(UID.key);
         Form currentForm = getUserAttributesInForm(uid, ldapEnv);
 
         // Special processing for current and new passwords.
         String currentPassword = FormUtils.checkCurrentPassword(currentForm,
                 updateForm);
         String newPassword = FormUtils.checkNewPasswords(updateForm);
         FormUtils
                 .setNewPasswordInForm(currentPassword, newPassword, updateForm);
 
         updateForm = FormUtils.sanitizeForm(updateForm);
         currentForm = FormUtils.sanitizeForm(currentForm);
 
         FormUtils.copyNameAttributes(currentForm, updateForm);
 
         FormUtils.addDerivedAttributes(updateForm);
         FormUtils.addDerivedAttributes(currentForm);
 
         FormUtils.removeUnmodifiableAttributes(updateForm);
         FormUtils.removeUnmodifiableAttributes(currentForm);
 
         FormUtils.stripNonLdapAttributes(updateForm);
         FormUtils.stripNonLdapAttributes(currentForm);
 
         FormUtils.removeIdenticalAttributes(currentForm, updateForm);
 
         Form addedAttrs = FormUtils.removeAllNamedParameters(updateForm,
                 currentForm);
         Form deletedAttrs = FormUtils.removeAllNamedParameters(currentForm,
                 updateForm);
         Form modifiedAttrs = FormUtils.retainAllNamedParameters(updateForm,
                 currentForm);
 
         return new Form[] { addedAttrs, deletedAttrs, modifiedAttrs };
     }
 
     public static String extractPassword(Attributes attrs) {
 
         String ldapPassword = "";
 
         try {
 
             Attribute attr = attrs.get(PASSWORD.key);
             if (attr != null) {
                 byte[] bytes;
                 bytes = (byte[]) attr.get();
                 ldapPassword = new String(bytes);
             }
 
         } catch (NamingException consumed) {
             // Do nothing; return empty password.
         }
 
         return ldapPassword;
     }
 
     public static Properties getUserProperties(String uid, LdapConfig ldapEnv) {
 
         Properties userProperties = new Properties();
 
         Attributes attrs = getUserAttributes(uid, ldapEnv);
 
         try {
 
             NamingEnumeration<String> ids = attrs.getIDs();
             while (ids.hasMore()) {
                 String id = ids.next();
                 Attribute attr = attrs.get(id);
                 Object value = attr.get();
 
                 // Ensure that password is not sent back through browser.
                 if (PASSWORD.key.equals(id)) {
                     value = "";
                 }
                 userProperties.put(id, value);
             }
 
         } catch (NamingException e) {
 
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     DATABASE_CONNECT_ERROR);
 
         }
 
         return userProperties;
     }
 
     public static Form getUserAttributesInForm(String uid, LdapConfig ldapEnv) {
 
         Form form = new Form();
 
         Attributes attrs = getUserAttributes(uid, ldapEnv);
 
         try {
 
             NamingEnumeration<? extends Attribute> enumeration = attrs.getAll();
             while (enumeration.hasMore()) {
                 Attribute attr = enumeration.next();
                 String key = attr.getID();
                 Object value = attr.get();
                 if (value != null) {
                     if (value instanceof byte[]) {
                         form.add(key, new String((byte[]) value));
                     } else {
                         form.add(key, value.toString());
                     }
                 }
             }
 
             form = FormUtils.sanitizeForm(form);
 
         } catch (NamingException e) {
 
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     DATABASE_CONNECT_ERROR);
 
         }
 
         return form;
     }
 
     public static Attributes getUserAttributes(String uid, LdapConfig ldapEnv) {
 
         Attributes attrs = null;
 
         // Get a connection from the pool.
         DirContext ctx = null;
 
         try {
 
             ctx = new InitialDirContext(ldapEnv);
 
             Attributes matchingAttrs = new BasicAttributes(true);
             matchingAttrs.put(UID.key, uid);
 
             NamingEnumeration<SearchResult> results = ctx.search("",
                     matchingAttrs, null);
 
             if (results.hasMore()) {
                 SearchResult result = results.next();
                 attrs = result.getAttributes();
             } else {
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                        "user record not found for " + uid);
             }
 
             if (results.hasMore()) {
                 throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                         "multiple records found for " + uid);
             }
 
         } catch (InvalidAttributesException e) {
 
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     "incomplete user entry");
 
         } catch (AuthenticationException e) {
 
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     DATABASE_CONNECT_ERROR);
 
         } catch (NamingException e) {
 
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     DATABASE_CONNECT_ERROR);
 
         } finally {
             freeContext(ctx);
         }
 
         return attrs;
     }
 
     public static String getUserDn(String uid, LdapConfig ldapEnv) {
 
         String userDn = null;
 
         DirContext ctx = null;
 
         try {
 
             ctx = new InitialDirContext(ldapEnv);
 
             Attributes matchingAttrs = new BasicAttributes(true);
             matchingAttrs.put(UID.key, uid);
 
             NamingEnumeration<SearchResult> results = ctx.search("",
                     matchingAttrs, null);
 
             if (results.hasMore()) {
                 SearchResult result = results.next();
                 userDn = result.getNameInNamespace();
             } else {
                 throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                         "user record not found for " + uid);
             }
 
             if (results.hasMore()) {
                 throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                         "multiple records found for " + uid);
             }
 
         } catch (InvalidAttributesException e) {
 
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     "incomplete user entry");
 
         } catch (AuthenticationException e) {
 
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     DATABASE_CONNECT_ERROR);
 
         } catch (NamingException e) {
 
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     DATABASE_CONNECT_ERROR);
 
         } finally {
             freeContext(ctx);
         }
 
         return userDn;
     }
 
     public static String getEmailAddress(String uid, LdapConfig ldapEnv) {
 
         String userEmail = null;
         Attributes attrs = UserEntry.getUserAttributes(uid, ldapEnv);
         Attribute attr = attrs.get(UserAttribute.EMAIL.key);
         if (attr != null) {
             try {
                 userEmail = (String) attr.get();
             } catch (NamingException consumed) {
                 throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                         "missing email address in record");
             }
         } else {
             throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                     "missing email address in record");
         }
 
         return userEmail;
     }
 
     private static void freeContext(Context ctx) {
         if (ctx != null) {
             try {
                 ctx.close();
             } catch (NamingException e) {
                 LOGGER.warning("cannot free directory context: "
                         + e.getMessage());
             }
         }
     }
 
 }

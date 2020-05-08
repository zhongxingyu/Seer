 /**
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright (c) 2009 Sun Microsystems Inc. All Rights Reserved
  *
  * The contents of this file are subject to the terms
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
 * $Id: ApplicationManager.java,v 1.4 2009-11-12 01:11:06 veiming Exp $
  */
 package com.sun.identity.entitlement;
 
 import com.sun.identity.entitlement.interfaces.ResourceName;
 import com.sun.identity.entitlement.util.SearchFilter;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.security.Principal;
import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 import javax.security.auth.Subject;
 
 /**
  * Application Manager handles addition, deletion and listing of applications
  * for each realm.
  */
 public final class ApplicationManager {
     private final static Object lock = new Object();
     private static Map<String, Set<Application>> applications =
         new HashMap<String, Set<Application>>();
     private static final ReentrantReadWriteLock readWriteLock =
         new ReentrantReadWriteLock();
 
     private ApplicationManager() {
     }
 
 
     /**
      * Returns the application names in a realm.
      *
      * @param adminSubject Admin Subject who has the rights to access
      *        configuration datastore.
      * @param realm Realm name.
      * @param filters Search Filters
      * @return application names in a realm.
      */
     public static Set<String> search(
         Subject adminSubject,
         String realm,
         Set<SearchFilter> filters
     ) throws EntitlementException {
         if (adminSubject == PrivilegeManager.superAdminSubject) {
             EntitlementConfiguration ec = EntitlementConfiguration.getInstance(
                 adminSubject, realm);
             return ec.searchApplicationNames(adminSubject, filters);
         }
 
         ApplicationPrivilegeManager apm =
             ApplicationPrivilegeManager.getInstance(realm,
             adminSubject);
         return apm.getApplications(ApplicationPrivilege.Action.READ);
     }
 
     /**
      * Returns the application names in a realm.
      *
      * @param adminSubject Admin Subject who has the rights to access
      *        configuration datastore.
      * @param realm Realm name.
      * @return application names in a realm.
      */
     public static Set<String> getApplicationNames(
         Subject adminSubject,
         String realm
     ) {
         Set<Application> appls = getApplications(adminSubject, realm);
         Set<String> results = new HashSet<String>();
         for (Application appl : appls) {
             results.add(appl.getName());
         }
         return results;
     }
 
     private static Set<Application> getAllApplication(String realm) {
         readWriteLock.readLock().lock();
         try {
             Set<Application> appls = applications.get(realm);
             if (appls != null) {
                 return appls;
             }
         } finally {
             readWriteLock.readLock().unlock();
         }
 
         readWriteLock.writeLock().lock();
         try {
             Set<Application> appls = applications.get(realm);
             if (appls == null) {
                 EntitlementConfiguration ec =
                     EntitlementConfiguration.getInstance(
                     PrivilegeManager.superAdminSubject, realm);
                 appls = ec.getApplications();
                 applications.put(realm, appls);
             }
             return appls;
         } finally {
             readWriteLock.writeLock().unlock();
         }
     }
 
     private static Set<Application> getApplications(Subject adminSubject,
         String realm) {
         Set<Application> appls = getAllApplication(realm);
 
         if (adminSubject == PrivilegeManager.superAdminSubject) {
             return appls;
         }
 
         Set<Application> accessible = new HashSet<Application>();
         ApplicationPrivilegeManager apm =
             ApplicationPrivilegeManager.getInstance(realm, adminSubject);
         Set<String> accessibleApplicationNames =
             apm.getApplications(ApplicationPrivilege.Action.READ);
 
         for (Application app : appls) {
             String applicationName = app.getName();
             Application cloned = app.clone();
 
             if (accessibleApplicationNames.contains(applicationName)) {
                 cloned.setResources(apm.getResources(applicationName,
                     ApplicationPrivilege.Action.READ));
                 accessible.add(cloned);
             }
         }
 
         return accessible;
     }
 
     /**
      * Returns application.
      *
      * @param adminSubject Admin Subject who has the rights to access
      *        configuration datastore.
      * @param realm Realm name.
      * @param name Name of Application.
      * @return application.
      */
     public static Application getApplicationForEvaluation(
         String realm,
         String name
     ) {
         return getApplication(PrivilegeManager.superAdminSubject, realm,
             name);
     }
 
     /**
      * Returns application.
      *
      * @param adminSubject Admin Subject who has the rights to access
      *        configuration datastore.
      * @param realm Realm name.
      * @param name Name of Application.
      * @return application.
      */
     public static Application getApplication(
         Subject adminSubject,
         String realm,
         String name
     ) {
         if ((name == null) || (name.length() == 0)) {
             name = ApplicationTypeManager.URL_APPLICATION_TYPE_NAME;
         }
 
         Set<Application> appls = getApplications(adminSubject, realm);
         for (Application appl : appls) {
             if (appl.getName().equals(name)) {
                 return appl;
             }
         }
 
         // try again, to get application for sub realm.
         clearCache(realm);
 
         appls = getApplications(adminSubject, realm);
         for (Application appl : appls) {
             if (appl.getName().equals(name)) {
                 return appl;
             }
         }
 
         return null;
     }
 
     /**
      * Removes application.
      *
      * @param adminSubject Admin Subject who has the rights to access
      *        configuration datastore.
      * @param realm Realm Name.
      * @param name Application Name.
      * @throws EntitlementException
      */
     public static void deleteApplication(
         Subject adminSubject,
         String realm,
         String name
     ) throws EntitlementException {
         boolean allowed = (adminSubject == PrivilegeManager.superAdminSubject);
         if (!allowed) {
             allowed = hasAccessToApplication(realm, adminSubject, name,
                 ApplicationPrivilege.Action.MODIFY);
         }
 
         if (!allowed) {
             throw new EntitlementException(326);
         }
 
         EntitlementConfiguration ec = EntitlementConfiguration.getInstance(
             adminSubject, realm);
         ec.removeApplication(name);
         clearCache(realm);
     }
 
     /**
      * Saves application data.
      *
      * @param adminSubject Admin Subject who has the rights to access
      *        configuration datastore.
      * @param realm Realm Name.
      * @param application Application object.
      */
     public static void saveApplication(
         Subject adminSubject,
         String realm,
         Application application
     ) throws EntitlementException {
         boolean allow = (adminSubject == PrivilegeManager.superAdminSubject);
         
         if (!allow) {
             ApplicationPrivilegeManager apm = 
                 ApplicationPrivilegeManager.getInstance(realm, adminSubject);
             if (isNewApplication(realm, application)) {
                 allow = apm.canCreateApplication(realm);
             } else {
                 allow = hasAccessToApplication(apm, application,
                     ApplicationPrivilege.Action.MODIFY);
             }
         }
 
         if (!allow) {
             throw new EntitlementException(326);
         }
 
         validateApplication(adminSubject, realm, application);
         Date date = new Date();
         Set<Principal> principals = adminSubject.getPrincipals();
         String principalName = ((principals != null) && !principals.isEmpty()) ?
             principals.iterator().next().getName() : null;
 
         if (application.getCreationDate() == -1) {
             application.setCreationDate(date.getTime());
             if (principalName != null) {
                 application.setCreatedBy(principalName);
             }
         }
         application.setLastModifiedDate(date.getTime());
         if (principalName != null) {
             application.setLastModifiedBy(principalName);
         }
 
         EntitlementConfiguration ec = EntitlementConfiguration.getInstance(
             adminSubject, realm);
         ec.storeApplication(application);
         clearCache(realm);
     }
 
     private static boolean hasAccessToApplication(
         String realm,
         Subject adminSubject,
         String applicationName,
         ApplicationPrivilege.Action action) {
         ApplicationPrivilegeManager apm =
             ApplicationPrivilegeManager.getInstance(realm,
             adminSubject);
         Set<String> applicationNames = apm.getApplications(action);
         return applicationNames.contains(applicationName);
     }
 
     private static boolean hasAccessToApplication(
         ApplicationPrivilegeManager apm,
         Application application,
         ApplicationPrivilege.Action action) {
         Set<String> applNames = apm.getApplications(action);
         return applNames.contains(application.getName());
     }
 
     private static boolean isNewApplication(
         String realm,
         Application application
     ) {
         Set<Application> existingAppls = getAllApplication(realm);
         String applName = application.getName();
 
         for (Application app : existingAppls) {
             if (app.getName().equals(applName)) {
                 return false;
             }
         }
         return true;
     }
 
     private static void validateApplication(
         Subject adminSubject,
         String realm,
         Application application
     ) throws EntitlementException {
         if (!realm.equals("/")) {
             String applTypeName = application.getApplicationType().getName();
             ResourceName comp = application.getResourceComparator();
             Set<String> referredRes = getReferredResources(
                 adminSubject, realm, applTypeName);
             for (String r : application.getResources()) {
                 validateApplication(application, comp, r, referredRes);
             }
         }
     }
 
     private static void validateApplication(
         Application application,
         ResourceName comp,
         String res,
         Set<String> referredRes) throws EntitlementException {
         for (String r : referredRes) {
             ResourceMatch match = comp.compare(res, r, true);
             if (match.equals(ResourceMatch.EXACT_MATCH) ||
                 match.equals(ResourceMatch.WILDCARD_MATCH) ||
                 match.equals(ResourceMatch.SUB_RESOURCE_MATCH)) {
                 return;
             }
         }
         Object[] param = {application.getName()};
         throw new EntitlementException(247, param);
     }
     
     /**
      * Clears the cached applications. Must be called when notifications are
      * received for changes to applications.
      */
     public static void clearCache(String realm) {
         readWriteLock.writeLock().lock();
         try {
             applications.remove(realm);
         } finally {
             readWriteLock.writeLock().unlock();
         }
     }
 
     /**
      * Refers resources to another realm.
      *
      * @param adminSubject Admin Subject who has the rights to access
      *        configuration datastore.
      * @param parentRealm Parent realm name.
      * @param referRealm Referred realm name.
      * @param applicationName Application name.
      * @param resources Referred resources.
      * @throws EntitlementException if resources cannot be referred.
      */
     public static void referApplication(
         Subject adminSubject,
         String parentRealm,
         String referRealm,
         String applicationName,
         Set<String> resources
     ) throws EntitlementException {
         boolean allowed = (adminSubject == PrivilegeManager.superAdminSubject);
         if (!allowed) {
             allowed = hasAccessToApplication(parentRealm, adminSubject,
                 applicationName, ApplicationPrivilege.Action.MODIFY);
         }
 
         if (!allowed) {
             throw new EntitlementException(326);
         }
 
         Application appl = getApplication(PrivilegeManager.superAdminSubject,
             parentRealm, applicationName);
         if (appl == null) {
             Object[] params = {parentRealm, referRealm, applicationName};
             throw new EntitlementException(280, params);
         }
 
         Application clone = appl.refers(referRealm, resources);
         EntitlementConfiguration ec = EntitlementConfiguration.getInstance(
             adminSubject, referRealm);
         ec.storeApplication(clone);
         clearCache(referRealm);
     }
 
     /**
      * Derefers resources from a realm.
      *
      * @param adminSubject Admin Subject who has the rights to access
      *        configuration datastore.
      * @param referRealm Referred realm name,
      * @param applicationName Application name.
      * @param resources Resources to be dereferred.
      * @throws EntitlementException if resources cannot be dereferred.
      */
     public static void dereferApplication(
         Subject adminSubject,
         String referRealm,
         String applicationName,
         Set<String> resources
     ) throws EntitlementException {
         boolean allowed = (adminSubject == PrivilegeManager.superAdminSubject);
         if (!allowed) {
             allowed = hasAccessToApplication(referRealm, adminSubject,
                 applicationName, ApplicationPrivilege.Action.MODIFY);
         }
 
         if (!allowed) {
             throw new EntitlementException(326);
         }
 
         EntitlementConfiguration ec = EntitlementConfiguration.getInstance(
             adminSubject, referRealm);
         ec.removeApplication(applicationName, resources);
     }
 
     /**
      * Returns referred resources for a realm.
      *
      * @param adminSubject Admin Subject who has the rights to access
      *        configuration datastore.
      * @param realm Realm name
      * @param applicationTypeName Application Type Name.
      * @return referred resources for a realm.
      * @throws EntitlementException if referred resources cannot be returned.
      */
     public static Set<String> getReferredResources(
         Subject adminSubject,
         String realm,
         String applicationTypeName
     ) throws EntitlementException {
         boolean allowed = (adminSubject == PrivilegeManager.superAdminSubject);
         if (!allowed) {
             allowed = hasAccessToApplication(realm, adminSubject,
                 applicationTypeName, ApplicationPrivilege.Action.READ);
         }
 
         if (!allowed) {
            return Collections.EMPTY_SET;
         }
 
         PrivilegeIndexStore pis = PrivilegeIndexStore.getInstance(
             adminSubject, realm);
         return pis.getReferredResources(applicationTypeName);
     }
 
     /**
      * Creates an application.
      *
      * @param realm Realm name.
      * @param name Name of application.
      * @param applicationType application type.
      * @throws EntitlementException if application class is not found.
      */
     public static Application newApplication(
         String realm,
         String name,
         ApplicationType applicationType
     ) throws EntitlementException {
         Class clazz = applicationType.getApplicationClass();
         Class[] parameterTypes = {String.class, String.class,
             ApplicationType.class};
         Constructor constructor;
         try {
             constructor = clazz.getConstructor(parameterTypes);
             Object[] parameters = {realm, name, applicationType};
             return (Application) constructor.newInstance(parameters);
         } catch (NoSuchMethodException ex) {
             throw new EntitlementException(6, ex);
         } catch (SecurityException ex) {
             throw new EntitlementException(6, ex);
         } catch (InstantiationException ex) {
             throw new EntitlementException(6, ex);
         } catch (IllegalAccessException ex) {
             throw new EntitlementException(6, ex);
         } catch (IllegalArgumentException ex) {
             throw new EntitlementException(6, ex);
         } catch (InvocationTargetException ex) {
             throw new EntitlementException(6, ex);
         }
     }
 }

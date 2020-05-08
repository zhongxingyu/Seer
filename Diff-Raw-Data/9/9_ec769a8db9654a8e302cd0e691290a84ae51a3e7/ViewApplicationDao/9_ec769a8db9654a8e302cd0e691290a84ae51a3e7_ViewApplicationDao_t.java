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
 * $Id: ViewApplicationDao.java,v 1.11 2009-07-02 12:27:39 veiming Exp $
  */
 
 package com.sun.identity.admin.dao;
 
 import com.sun.identity.admin.ManagedBeanResolver;
 import com.sun.identity.admin.Token;
 import com.sun.identity.admin.model.RealmBean;
 import com.sun.identity.admin.model.RealmsBean;
 import com.sun.identity.admin.model.ViewApplication;
 import com.sun.identity.admin.model.ViewApplicationType;
 import com.sun.identity.entitlement.Application;
 import com.sun.identity.entitlement.ApplicationManager;
 import com.sun.identity.entitlement.EntitlementException;
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 import javax.security.auth.Subject;
 
 public class ViewApplicationDao implements Serializable {
 
     private ViewApplicationTypeDao viewApplicationTypeDao;
 
     public void setViewApplicationTypeDao(ViewApplicationTypeDao viewApplicationTypeDao) {
         this.viewApplicationTypeDao = viewApplicationTypeDao;
     }
 
     public Map<String, ViewApplication> getViewApplications() {
         Map<String, ViewApplication> viewApplications = new HashMap<String, ViewApplication>();
 
         ManagedBeanResolver mbr = new ManagedBeanResolver();
         Map<String, ViewApplicationType> entitlementApplicationTypeToViewApplicationTypeMap = (Map<String, ViewApplicationType>) mbr.resolve("entitlementApplicationTypeToViewApplicationTypeMap");
 
         Token token = new Token();
         Subject adminSubject = token.getAdminSubject();
 
         RealmBean realmBean = RealmsBean.getInstance().getRealmBean();
 
         for (String name : ApplicationManager.getApplicationNames(adminSubject, realmBean.getName())) {
             Application a = ApplicationManager.getApplication(adminSubject, realmBean.getName(), name);
             if (a.getResources() == null || a.getResources().size() == 0) {
                 // TODO: log
                 continue;
             }
 
             // application type
             ViewApplicationType vat = entitlementApplicationTypeToViewApplicationTypeMap.get(a.getApplicationType().getName());
             if (vat == null) {
                 // TODO: log
                 continue;
             }
 
             ViewApplication va = new ViewApplication(a);
             viewApplications.put(va.getName(), va);
         }
 
         return viewApplications;
     }
 
     public void setViewApplication(ViewApplication va) {
         try {
             Application a = va.toApplication();
             RealmBean realmBean = RealmsBean.getInstance().getRealmBean();
             Subject adminSubject = new Token().getAdminSubject();
             ApplicationManager.saveApplication(adminSubject, realmBean.getName(), a);
         } catch (EntitlementException ee) {
             throw new RuntimeException(ee);
         }
     }
 
     public Application getApplication(ViewApplication va) {
         String name = va.getName();
         Token token = new Token();
         Subject adminSubject = token.getAdminSubject();
         RealmBean realmBean = RealmsBean.getInstance().getRealmBean();
         Application a = ApplicationManager.getApplication(adminSubject, realmBean.getName(), name);
         return a;
     }
 }

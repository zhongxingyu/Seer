 /**
  * ******************************************************************************************
  * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations (FAO).
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice,this list
  *       of conditions and the following disclaimer.
  *    2. Redistributions in binary form must reproduce the above copyright notice,this list
  *       of conditions and the following disclaimer in the documentation and/or other
  *       materials provided with the distribution.
  *    3. Neither the name of FAO nor the names of its contributors may be used to endorse or
  *       promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT
  * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
  * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 package org.sola.services.boundary.wsclients;
 
 import java.util.List;
 import javax.xml.namespace.QName;
 import org.sola.services.boundary.wsclients.exception.WebServiceClientException;
 import org.sola.services.boundary.wsclients.exception.WebServiceClientExceptionType;
 import org.sola.webservices.admin.BrTO;
 import org.sola.webservices.admin.LanguageTO;
 import org.sola.webservices.transferobjects.security.GroupSummaryTO;
 import org.sola.webservices.transferobjects.security.GroupTO;
 import org.sola.webservices.transferobjects.security.UserTO;
 import org.sola.webservices.admin.UnhandledFault;
 import org.sola.webservices.admin.SOLAFault;
 import org.sola.webservices.admin.Admin;
 import org.sola.webservices.admin.AdminService;
 import org.sola.webservices.transferobjects.security.RoleTO;
 
 /**
  * Implementation class for the {@linkplain AdminClient} interface. 
  */
 public class AdminClientImpl extends AbstractWSClientImpl implements AdminClient {
 
     private static final String NAMESPACE_URI = "http://webservices.sola.org/admin";
     private static final String LOCAL_PART = "admin-service";
     private static final String SERVICE_NAME = "Admin.";
 
     public AdminClientImpl(String url) {
         super(url, new QName(NAMESPACE_URI, LOCAL_PART));
     }
 
     private Admin getPort() {
         return getPort(Admin.class, AdminService.class);
     }
 
     @Override
     public boolean checkConnection() throws WebServiceClientException {
         final String inputService = SERVICE_NAME + "checkConnection";
         try {
             boolean result = getPort().checkConnection();
             return result;
         } catch (Throwable e) {
            handleExceptionsMethod(inputService,e);
           return  true;
         }
     }
 
     @Override
     public UserTO getCurrentUser() throws WebServiceClientException {
         final String inputService = SERVICE_NAME + "getCurrentUser";
         try {
             UserTO result = getPort().getCurrentUser();
             return result;
         } catch (Throwable e) {
            handleExceptionsMethod(inputService,e);
            return null;
         }
     }
 
     @Override
     public List<GroupTO> getGroups() throws WebServiceClientException {
         final String inputService = SERVICE_NAME + "GetGroups";
         try {
             List<GroupTO> result = getPort().getGroups();
             return result;
        } catch (Throwable e) {
            handleExceptionsMethod(inputService,e);
            return null;
        }
     }
 
     @Override
     public GroupTO getGroup(String groupId) throws WebServiceClientException {
         final String inputService = SERVICE_NAME + "GetGroup";
         try {
             GroupTO result = getPort().getGroup(groupId);
             return result;
          } catch (Throwable e) {
            handleExceptionsMethod(inputService,e);
            return null;
        }
     }
 
     @Override
     public UserTO getUser(String userName) throws WebServiceClientException {
         final String inputService = SERVICE_NAME + "GetUser";
         try {
             UserTO result = getPort().getUser(userName);
             return result;
         } catch (Throwable e) {
            handleExceptionsMethod(inputService,e);
            return null;
        }
     }
 
     @Override
     public UserTO saveUser(UserTO userTO) throws WebServiceClientException {
         final String inputService = SERVICE_NAME + "SaveUser";
         try {
             UserTO result = getPort().saveUser(userTO);
             return result;
          } catch (Throwable e) {
            handleExceptionsMethod(inputService,e);
            return null;
        }
     }
 
     @Override
     public GroupTO saveGroup(GroupTO groupTO) throws WebServiceClientException {
         final String inputService = SERVICE_NAME + "SaveGroup";
         try {
             GroupTO result = getPort().saveGroup(groupTO);
             return result;
          } catch (Throwable e) {
            handleExceptionsMethod(inputService,e);
            return null;
        }
     }
     
     @Override
     public List<RoleTO> getRoles() throws WebServiceClientException {
         final String inputService = SERVICE_NAME + "getRoles";
         try {
             List<RoleTO> result = getPort().getRoles();
             return result;
         } catch (Throwable e) {
            handleExceptionsMethod(inputService,e);
            return null;
        }
     }
     
     @Override
     public RoleTO saveRole(RoleTO roleTO) throws WebServiceClientException {
         final String inputService = SERVICE_NAME + "SaveRole";
         try {
             RoleTO result = getPort().saveRole(roleTO);
             return result;
          } catch (Throwable e) {
            handleExceptionsMethod(inputService,e);
            return null;
        }
     }
 
     @Override
     public List<GroupSummaryTO> getGroupsSummary() throws WebServiceClientException {
         final String inputService = SERVICE_NAME + "GetGroupsSummary";
         try {
             List<GroupSummaryTO> result = getPort().getGroupsSummary();
             return result;
         } catch (Throwable e) {
            handleExceptionsMethod(inputService,e);
            return null;
        }
     }
 
     @Override
     public boolean changePassword(String userName, String password) throws WebServiceClientException {
         final String inputService = SERVICE_NAME + "changePasswords";
         try {
             return getPort().changePassword(userName, password);
         } catch (Throwable e) {
            handleExceptionsMethod(inputService,e);
           return  true;
        }
     }
 
     @Override
     public List<RoleTO> getCurrentUserRoles() throws WebServiceClientException {
         final String inputService = SERVICE_NAME + "getCurrentUserRoles";
         try {
             List<RoleTO> result = getPort().getCurrentUserRoles();
             return result;
         } catch (Throwable e) {
            handleExceptionsMethod(inputService,e);
            return null;
        }
     }
 
     @Override
     public boolean isUserAdmin() throws WebServiceClientException {
         final String inputService = SERVICE_NAME + "isUserAdmin";
         try {
             return getPort().isUserAdmin();
         } catch (Throwable e) {
            handleExceptionsMethod(inputService,e);
            return false;
        }
     }
 
     @Override
     public List<LanguageTO> getLanguages() throws WebServiceClientException {
         return getLanguages(getLanguageCode());
     }
 
     @Override
     public List<LanguageTO> getLanguages(String lang) throws WebServiceClientException {
         final String inputService = SERVICE_NAME + "getLanguages";
         try {
             return getPort().getLanguages(lang);
         } catch (Throwable e) {
            handleExceptionsMethod(inputService,e);
            return null;
        }
     }
 
     @Override
     public BrTO getBr(String id, String lang) throws WebServiceClientException {
         final String inputService = SERVICE_NAME + "getBr";
         try {
             return getPort().getBr(id, lang);
        } catch (Throwable e) {
            handleExceptionsMethod(inputService,e);
            return null;
        }
     }
 
     @Override
     public BrTO getBr(String id) throws WebServiceClientException {
         return getBr(id, getLanguageCode());
     }
     
     @Override
     public BrTO saveBr(BrTO brTO) throws WebServiceClientException {
         final String inputService = SERVICE_NAME + "saveBr";
         try {
             return getPort().saveBr(brTO);
          } catch (Throwable e) {
            handleExceptionsMethod(inputService,e);
            return null;
        }
     }
 }

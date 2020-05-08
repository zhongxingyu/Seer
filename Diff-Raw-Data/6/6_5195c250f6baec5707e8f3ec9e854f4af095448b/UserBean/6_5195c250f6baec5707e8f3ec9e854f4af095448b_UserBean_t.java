 /* MyMAM - Open Source Digital Media Asset Management.
  * http://www.mymam.net
  *
  * Copyright 2013, MyMAM contributors as indicated by the @author tag.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.mymam.controller;
 
 import net.mymam.ejb.UserMgmtEJB;
 import net.mymam.entity.User;
 
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
import javax.faces.bean.NoneScoped;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 
 /**
  * @author fstab
  */
 @ManagedBean
@NoneScoped // UserBean is stateless and may be used as @ManagedBean in any scope.
 public class UserBean {
 
     @EJB
     private UserMgmtEJB userMgmt;
 
 
     private String getRemoteUser() {
         FacesContext context = FacesContext.getCurrentInstance();
         ExternalContext externalContext = context.getExternalContext();
         return externalContext.getRemoteUser();
     }
 
     public boolean isLoggedOn() {
         return getRemoteUser() != null;
     }
 
     public User getLoggedOnUser() {
         String userName = getRemoteUser();
         return userMgmt.findUserByName(userName);
     }
 }

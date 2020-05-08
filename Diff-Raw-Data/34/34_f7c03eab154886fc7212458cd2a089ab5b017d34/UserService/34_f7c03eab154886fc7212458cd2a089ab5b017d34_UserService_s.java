 /*
  * Copyright (c) 2008, Muhd Kamil Mohd Baki. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.qi4j.chronos.service;
 
 import org.qi4j.chronos.model.Account;
 import org.qi4j.chronos.model.User;
 import org.qi4j.chronos.service.impl.UserServiceMixin;
import org.qi4j.composite.Constraints;
 import org.qi4j.composite.Mixins;
 import org.qi4j.composite.Optional;
import org.qi4j.library.constraints.NotNullConstraint;
import org.qi4j.library.constraints.annotation.NotNull;
 import org.qi4j.library.validation.ValidationException;
 import org.qi4j.service.ServiceComposite;
 
 @Mixins( UserServiceMixin.class )
 public interface UserService extends ServiceComposite
 {
     /**
      * Authenticate user by account, username and password. Note: the account may be null if the user attempts
      * to login as super admin.
      *
      * @param anAccount account to login, null if user attempts to login as super user.
      * @param aUserName user name.
      * @param apassword user password.
      * @return the logged in user.
      * @throws UserAuthenticationFailException
      *          thrown if user can't be authenticated.
      */
     User authenticate( @Optional Account anAccount, String aUserName, String apassword )
         throws UserAuthenticationFailException;
 
     /**
      * Change user's password.
      *
      * @param user        User to change password.
      * @param oldPassword users old password.
      * @param password    users new password.
      * @throws ValidationException thrown if the user's password doesn't match with the oldPassword
      */
     void changePassword( User user, String oldPassword, String password )
         throws ValidationException;
 }

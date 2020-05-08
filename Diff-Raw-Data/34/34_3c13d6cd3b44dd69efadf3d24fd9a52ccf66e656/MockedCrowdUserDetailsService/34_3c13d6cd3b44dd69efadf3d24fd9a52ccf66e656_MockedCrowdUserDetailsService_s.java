 /*
  * Copyright (C) 2011-2013 eXo Platform SAS.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 3 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.exoplatform.acceptance.security;
 
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UserDetailsService;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 
 public class MockedCrowdUserDetailsService implements UserDetailsService {
   private CrowdUserMock administrator;
 
   private CrowdUserMock user;
 
  public MockedCrowdUserDetailsService(CrowdUserMock administrator, CrowdUserMock user) {
     this.administrator = administrator;
     this.user = user;
   }
 
   @Override
   public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
     if (user.getUsername().equals(username))
       return user;
     else if (administrator.getUsername().equals(username))
       return administrator;
     else
       throw new UsernameNotFoundException("Unknown user : " + username);
   }
 }

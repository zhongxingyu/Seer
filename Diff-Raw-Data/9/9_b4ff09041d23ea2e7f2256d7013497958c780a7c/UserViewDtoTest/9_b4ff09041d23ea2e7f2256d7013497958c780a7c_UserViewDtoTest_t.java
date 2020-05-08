 /**
  * Copyright (C) 2011  jtalks.org Team
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  * Also add information on how to contact you by electronic and paper mail.
  * Creation date: Apr 12, 2011 / 8:05:19 PM
  * The jtalks.org Project
  */
 package org.jtalks.common.web.dto;
 
 import org.joda.time.DateTime;
 import org.jtalks.common.model.entity.User;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 import static org.testng.Assert.*;
 
 /**
  * <p>This class contains tests for {@link UserViewDto} class.</p>
  * Date: 15.09.11
  * Time: 21:41
  *
  * @author Dmitriy Butakov
  */
 public class UserViewDtoTest {
     User user;
     UserViewDto userViewDto;
 
     @BeforeMethod
     public void setUp() {
         user = mock(User.class);
     }
 
     @Test
     public void testGetUserFirstName() {
         String firstName = "John";
         when(user.getFirstName()).thenReturn(firstName);
 
         userViewDto = new UserViewDto(user);
         
         assertEquals(userViewDto.getFirstName(), firstName);
     }
 
     @Test
     public void testGetUserLastName() {
         String lastName = "Doe";
         when(user.getLastName()).thenReturn(lastName);
 
         userViewDto = new UserViewDto(user);
 
         assertEquals(userViewDto.getLastName(), lastName);
     }
 
     @Test
     public void testGetUserUsername() {
         String username = "John_Doe";
         when(user.getUsername()).thenReturn(username);
 
         userViewDto = new UserViewDto(user);
 
         assertEquals(userViewDto.getUsername(), username);
     }
 
     @Test
     public void testGetUserEncodedUsername() {
         String encodedUsername = "encoded_John_Doe";
         when(user.getEncodedUsername()).thenReturn(encodedUsername);
 
         userViewDto = new UserViewDto(user);
 
         assertEquals(userViewDto.getEncodedUsername(), encodedUsername);
     }
 
     @Test
     public void testGetUserEmail() {
         String email = "john_doe@example.com";
         when(user.getEmail()).thenReturn(email);
 
         userViewDto = new UserViewDto(user);
 
         assertEquals(userViewDto.getEmail(), email);
     }
 
     @Test
     public void testGetUserLastLoginDate() {
         DateTime lastLoginDate = new DateTime(1983, 6, 14, 0, 0, 0, 0); // 14.06.1983 00:00:00:0
         when(user.getLastLogin()).thenReturn(lastLoginDate);
 
         userViewDto = new UserViewDto(user);
 
        assertEquals(userViewDto.getLastLogin(), lastLoginDate);
     }
 }

 /***************************************************************
  *  This file is part of the [fleXive](R) backend application.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) backend application is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/licenses/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.tests.embedded.jsf.bean;
 
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.exceptions.FxAccountInUseException;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxLoginFailedException;
 import com.flexive.shared.exceptions.FxLogoutFailedException;
 import com.flexive.shared.security.UserGroup;
 import static com.flexive.tests.embedded.FxTestUtils.login;
 import static com.flexive.tests.embedded.FxTestUtils.logout;
 import com.flexive.tests.embedded.TestUsers;
 import com.flexive.war.beans.admin.main.UserGroupBean;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 import org.testng.Assert;
 
 import java.util.List;
 
 /**
  * UserGroupBean tests
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 @Test(groups = {"jsf"})
 public class UserGroupBeanTest {
     private UserGroupBean userGroupBean = null;
 
     @BeforeClass
     public void beforeClass() throws FxLoginFailedException, FxAccountInUseException {
         userGroupBean = new UserGroupBean();
         login(TestUsers.SUPERVISOR);
     }
 
     @AfterClass
     public void afterClass() throws FxLoginFailedException, FxAccountInUseException, FxLogoutFailedException {
         logout();
     }
 
     @Test
     public void testGetList() {
         List<UserGroup> groups = userGroupBean.getList();
         Assert.assertTrue(groups.size() > 0, "No user groups defined");
     }
 
     @Test
     public void testCreate() throws FxApplicationException {
         try {
             userGroupBean.setId(-1);
             userGroupBean.setColor(null);
             userGroupBean.setName("TESTNG_TEST_GROUP");
            userGroupBean.setMandator(CacheAdmin.getEnvironment().getMandator(FxContext.getUserTicket().getMandatorId()));
             String result = userGroupBean.create();
             Assert.assertTrue("userGroupOverview".equals(result), "Invalid outcome: " + result);
         } finally {
             deleteUserGroup();
         }
 
     }
 
     /**
      * Delete the user group created in the usergroupbean, if any.
      *
      * @throws FxApplicationException
      */
     private void deleteUserGroup() throws FxApplicationException {
         if (userGroupBean.getId() != -1) {
             EJBLookup.getUserGroupEngine().remove(userGroupBean.getId());
         }
         if (userGroupBean.getCreatedGroupId() != -1) {
             EJBLookup.getUserGroupEngine().remove(userGroupBean.getCreatedGroupId());
         }
 	}
 }

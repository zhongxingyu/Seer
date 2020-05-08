 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
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
 package com.flexive.tests.embedded;
 
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.exceptions.FxAccountInUseException;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxLoginFailedException;
 import com.flexive.shared.exceptions.FxLogoutFailedException;
 import com.flexive.shared.security.ACL;
 import com.flexive.shared.security.Mandator;
 import com.flexive.shared.security.ACLCategory;
 import com.flexive.shared.structure.*;
 import com.flexive.shared.value.FxString;
 import static com.flexive.tests.embedded.FxTestUtils.login;
 import static com.flexive.tests.embedded.FxTestUtils.logout;
 import static org.testng.Assert.assertEquals;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 /**
  * Tests for the filtered environment
  * 
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class FilteredEnvironmentTest extends EnvironmentTest {
     private static final String MANDATOR_NAME = "EnvironmentTestMandator";
     private static final String TYPE_NAME = "EnvironmentTestType";
     private Mandator foreignMandator;
     private static final String PROPERTY_NAME = "EnvironmentTestProperty";
 
     @BeforeClass(groups = {"ejb", "environment"})
     public void init() throws FxApplicationException {
         try {
             FxContext.get().runAsSystem();
             // create a new (unreferenced) mandator
             final long foreignMandatorId = EJBLookup.getMandatorEngine().create(MANDATOR_NAME, true);
             foreignMandator = CacheAdmin.getEnvironment().getMandator(foreignMandatorId);
         } finally {
             FxContext.get().stopRunAsSystem();
         }
     }
 
     @AfterClass(groups = {"ejb", "environment"})
     public void destroy() throws FxApplicationException {
         try {
             FxContext.get().runAsSystem();
             // remove our mandator
             EJBLookup.getMandatorEngine().remove(foreignMandator.getId());
         } finally {
             FxContext.get().stopRunAsSystem();
         }
     }
 
     @Override
     protected FxEnvironment getEnvironment() {
         return CacheAdmin.getFilteredEnvironment();
     }
 
     @Test(groups = {"ejb", "environment"})
     public void mandatorTypeFilterTest() throws FxLoginFailedException, FxAccountInUseException, FxLogoutFailedException, FxApplicationException {
         long typeId = -1;
         long foreignTypeId = -1;
         try {
             // check original environment type count
             login(TestUsers.GUEST);
             final long originalEnvironmentSize = getEnvironment().getTypes(true, true, true, true).size();
             logout();
 
             // create a new type, property, and property assignment with the test mandator
             login(TestUsers.SUPERVISOR);
             final ACL acl = getEnvironment().getDefaultACL(ACLCategory.STRUCTURE);
             typeId = EJBLookup.getTypeEngine().save(FxTypeEdit.createNew(TYPE_NAME, new FxString("test"), acl));
             final long originalPropertiesSize = getEnvironment().getProperties(true, true).size();
             final int originalAssignmentsSize = getEnvironment().getPropertyAssignments().size();
             EJBLookup.getAssignmentEngine().createProperty(typeId, FxPropertyEdit.createNew(PROPERTY_NAME,
                     new FxString(""), new FxString(""), FxMultiplicity.MULT_0_1,
                     getEnvironment().getDefaultACL(ACLCategory.STRUCTURE),
                     FxDataType.String1024), "/");
             logout();
 
             // check the guest user environment
             login(TestUsers.GUEST);
             assertEquals(getEnvironment().getTypes(true, true, true, true).size(), originalEnvironmentSize + 1, "New type not visible in environment");
             assertEquals(getEnvironment().getProperties(true, true).size(), originalPropertiesSize + 1, "New property not visible in environment");
             assertEquals(getEnvironment().getPropertyAssignments().size(), originalAssignmentsSize + 1, "New property assignments not visible in environment");
 
             // check the guest user environment - should be unchanged from the previous call
             login(TestUsers.GUEST);
             assertEquals(getEnvironment().getTypes(true, true, true, true).size(), originalEnvironmentSize + 1,
                     "Foreign type must not be visible in the environment, env=" + getEnvironment().getTypes(true, true, true, true));
         } finally {
             // remove test type
             FxContext.get().runAsSystem();
             try {
                 if (typeId != -1) {
                     EJBLookup.getTypeEngine().remove(typeId);
                 }
                 if (foreignTypeId != -1) {
                     EJBLookup.getTypeEngine().remove(foreignTypeId);
                 }
             } finally {
                 FxContext.get().stopRunAsSystem();
             }
             logout();
         }
     }
 
 }

 /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  Copyright (C) 2008 Felipe Gaúcho
  
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.
  
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.
  
  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  
  This file is part of the CEJUG-CLASSIFIEDS Project - an  open source classifieds system
  originally used by CEJUG - Ceará Java Users Group.
  The project is hosted https://cejug-classifieds.dev.java.net/
  
  You can contact us through the mail dev@cejug-classifieds.dev.java.net
  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
 package net.java.dev.cejug.classifieds.test.functional.admin;
 
 import java.util.List;
 import java.util.TimeZone;
 
 import net.java.dev.cejug_classifieds.admin.CejugClassifiedsAdmin;
 import net.java.dev.cejug_classifieds.admin.CejugClassifiedsServiceAdmin;
 import net.java.dev.cejug_classifieds.metadata.admin.CreateDomainParam;
 import net.java.dev.cejug_classifieds.metadata.admin.DeleteDomainParam;
 import net.java.dev.cejug_classifieds.metadata.admin.ReadCategoryBundleParam;
 import net.java.dev.cejug_classifieds.metadata.admin.UpdateDomainParam;
 import net.java.dev.cejug_classifieds.metadata.common.AdvertisementCategory;
 import net.java.dev.cejug_classifieds.metadata.common.CategoryCollection;
 import net.java.dev.cejug_classifieds.metadata.common.Domain;
 import net.java.dev.cejug_classifieds.metadata.common.ServiceStatus;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Test the domain maintenance CRUD operations through the following steps:
  * <ul>
  * <li><strong>@Before</strong> the tests, we store the number of already
  * available categories. After all tests we check this number again, to be sure
  * our tests didn't changed the state of the database.
  * <ol>
  * <li>CREATE a new Domain, named
  * <em>FunctionalTest + System.currentTimeMillis()</em></li>
  * <li>READ the bundle of available domains and check if our newly created
  * domain is there. At this moment, we read the ID of the test domain.</li>
  * <li>UPDATE the test domain, modifying its name or other attribute.</li>
  * <li>READ the domain by ID and check if the updated data is correct.</li>
  * <li>DELETE the test created domain.</li>
  * </ol>
  * </li>
  * <li><strong>@After</strong> the tests, we and check the number of remained
  * domains in the server side to be sure the state of the database haven't
  * changed.</li>
  * </ul>
  * 
  * @author $Author: felipegaucho $
  * @version $Rev: 249 $ ($Date: 2008-06-08 13:29:07 +0200 (Sun, 08 Jun 2008) $)
  */
 public class DomainMaintenanceFunctionalTest {
 	private CejugClassifiedsAdmin admin = null;
 	private Domain domain = null;
 	private int availableDomainsBeforeTests = -1;
 
 	/**
 	 * We first store the number of already available categories. After all
 	 * tests, we check this number again, to be sure our tests didn't changed
 	 * the state of the database.
 	 * 
 	 * @throws Exception
 	 *             Generic exception, thrown by connection failure or read
 	 *             bundle categories errors.
 	 */
 	@Before
 	public void setUp() throws Exception {
 		admin = new CejugClassifiedsServiceAdmin().getCejugClassifiedsAdmin();
 		availableDomainsBeforeTests = countAvailableDomainsOnDatabase();
 	}
 
 	/**
 	 * Shared count categories method, to be sure the same counting mechanism is
 	 * used before and after the tests. It loads from the server a list of
 	 * available categories and returns its size.
 	 * 
 	 * @return the number of categories stored in the database.
 	 */
 	private int countAvailableDomainsOnDatabase() {
 		List<Domain> domains = admin.readDomainBundleOperation().getDomain();
 		return domains.size();
 
 	}
 
 	/**
 	 * Check if the number of available categories remains the same after the
 	 * tests. A successful test shouldn't modify the original state of the
 	 * database, otherwise we never know what to expect in the next test ;) The
 	 * server database is supposed to be reseted before a complete test run.
 	 * 
 	 * @throws Exception
 	 *             Generic exception, thrown by connection failure or read
 	 *             bundle categories errors.
 	 */
 	@After
 	public void tearDown() throws Exception {
		// Assert.assertEquals(availableDomainsBeforeTests,
		// countAvailableDomainsOnDatabase());
 	}
 
 	@Test
 	public void crudDomain() {
 		// CREATE
 		domain = new Domain();
 		domain.setDomain("functional.test." + System.currentTimeMillis());
 		domain.setBrand("Functional Domain");
 		domain.setSharedQuota(false);
 		domain.setTimezone(TimeZone.getDefault().getDisplayName());
 		CreateDomainParam createParam = new CreateDomainParam();
 		createParam.setDomain(domain);
 		ServiceStatus status = admin.createDomainOperation(createParam);
 		Assert.assertEquals(status.getStatusCode(), 200);
 
 		// READ
 		List<Domain> domains = admin.readDomainBundleOperation().getDomain();
 		Assert.assertFalse(domains.isEmpty());
 
 		boolean createdOk = false;
 		for (Domain readDomain : domains) {
 			if (readDomain.getDomain().equals(domain.getDomain())) {
 				// The just created domain has no ID, so we need to lookup for
 				// its domain name in the received list in order to know its ID.
 				domain = readDomain;
 				createdOk = true;
 				break;
 			}
 		}
 		Assert.assertTrue(createdOk);
 
 		// UPDATE
 		// If we have categories available, let's add one to our new domain.
 		List<AdvertisementCategory> categories = admin
 				.readCategoryBundleOperation(new ReadCategoryBundleParam())
 				.getAdvertisementCategory();
 
 		if (!categories.isEmpty()) {
 			CategoryCollection collection = new CategoryCollection();
 			collection.getAdvertisementCategory().add(categories.get(0));
 			domain.setCategoryCollection(collection);
 		}
 		domain.setBrand("New Brand on the block " + System.currentTimeMillis());
 
 		UpdateDomainParam updateParam = new UpdateDomainParam();
 		updateParam.setDomain(domain);
 		ServiceStatus updateStatus = admin.updateDomainOperation(updateParam);
 		Assert.assertEquals(updateStatus.getStatusCode(), 200);
 
 		List<Domain> updatedDomains = admin.readDomainBundleOperation()
 				.getDomain();
 
 		boolean updateOk = false;
 		for (Domain updatedDomain : updatedDomains) {
 			if (updatedDomain.getId().equals(domain.getId())) {
 				// Check if the received domain has the newly create name.
 				Assert
 						.assertEquals(updatedDomain.getBrand(), domain
 								.getBrand());
 				Assert.assertEquals(updatedDomain.getCategoryCollection()
 						.getAdvertisementCategory().size(), domain
 						.getCategoryCollection().getAdvertisementCategory()
 						.size());
 
 				updateOk = true;
 				break;
 			}
 		}
 		Assert.assertTrue(updateOk);
 
 		// DELETE
 		// remove or inactive the test advertisement
 		DeleteDomainParam deleteParam = new DeleteDomainParam();
 		deleteParam.setPrimaryKey(domain.getId());
 		ServiceStatus deleteStatus = admin.deleteDomainOperation(deleteParam);
 		Assert.assertEquals(deleteStatus.getStatusCode(), 200);
 	}
 }

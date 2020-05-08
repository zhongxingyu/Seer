 /**
  * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.md file.
  */
 
 package org.mule.module.facebook.automation.testcases;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.util.Collection;
 import java.util.HashMap;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 
 import com.restfb.types.Album;
 
 public class GetUserAlbumsTestCases extends FacebookTestParent {
 
 	@SuppressWarnings("unchecked")
 	@Before
 	public void setUp() {
 		try {
 			testObjects = (HashMap<String, Object>) context.getBean("getUserAlbumsTestData");
 			
 			String profileId = getProfileId();
 			String albumId = publishAlbum((String) testObjects.get("albumName"), (String) testObjects.get("msg"), profileId);
 
 			testObjects.put("profileId", profileId);
 			testObjects.put("albumId", albumId);
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	@Category({ RegressionTests.class })
 	@Test
 	public void testGetUserAlbums() {
 		try {
 			String profileId = (String) testObjects.get("profileId");
 			String since = (String) testObjects.get("since");
 			String until = (String) testObjects.get("until");
 			String limit = (String) testObjects.get("limit");
 			String offset = (String) testObjects.get("offset");
 			
 			Collection<Album> albums = requestUserAlbums(profileId,	since, until, limit, offset);

			Album firstAlbum = (Album) albums.toArray()[0];
			assertEquals(testObjects.get("albumName"), firstAlbum.getName());

 		} catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	@After
 	public void tearDown() {
 		try {
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 }

 /**
  *     Copyright SocialSite (C) 2009
  *
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.socialsite.dao.hibernate;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
 
 import java.util.Date;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.springframework.orm.hibernate3.SessionFactoryUtils;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.socialsite.dao.AbstractDaoTestHelper;
 import com.socialsite.persistence.Profile;
 import com.socialsite.persistence.Student;
 import com.socialsite.persistence.User;
 import com.socialsite.profile.Access;
 import com.socialsite.util.PrivacyModel;
 
 /**
  * 
  * @author Ananth
  * 
  */
 public class ProfileDaoTest extends AbstractDaoTestHelper
 {
 
 	@Test
 	@Transactional
 	public void testCreate()
 	{
 		final User ananth = new Student("ananth", "pass");
 		final Profile ananthProfile = new Profile(ananth);
 		ananthProfile.setEmail("ananth@gmail.com");
 		ananthProfile.setFirstName("ananth");
 		ananthProfile.setLastName("Kumaran");
 		profileDao.save(ananthProfile);
 
 		// flush the session so we can get the record using JDBC template
 		SessionFactoryUtils.getSession(sessionFactory, false).flush();
 
 		final int userResult = simpleJdbcTemplate.queryForInt("select count(*) from user");
 		assertEquals("user table should contain one entry ", userResult, 1);
 
 		final int profileResult = simpleJdbcTemplate.queryForInt("select count(*) from profile");
 		assertEquals("profile table should contain one entry ", profileResult, 1);
 
 		final Long user_id = simpleJdbcTemplate
 				.queryForLong("select id from user where username='ananth' ");
 		final Long profile_user_id = simpleJdbcTemplate
 				.queryForLong("select user_id from profile where firstname='ananth' ");
 
 		assertEquals("Both the user_id and profile_user_id should be equal", user_id,
 				profile_user_id);
 
 	}
 
 	@Test
 	@Transactional
 	public void testGetLastModifiedTime()
 	{
 		final Date date = new Date();
 		final User ananth = new Student("ananth", "pass");
 		final Profile ananthProfile = new Profile(ananth);
 		ananthProfile.getUser().setLastModified(date);
 		profileDao.save(ananthProfile);
 		Assert.assertEquals(date.getTime() / 1000, profileDao.getLastModifiedTime(ananth.getId())
 				.getTime() / 1000);
 
 	}
 
 	@Test
 	@Transactional
 	public void testGetUserImage()
 	{
 		final User ananth = new Student("ananth", "pass");
 		final Profile ananthProfile = new Profile(ananth);
 		// set some dummy data for image
 		ananthProfile.changeImage("dummy data".getBytes());
 		profileDao.save(ananthProfile);
 
 		final byte[] image = profileDao.getImage(ananth.getId());
 		assertNotNull("should return the data", image);
 		assertEquals("image data should be equal to  'dummy data' ", new String(image),
 				"dummy data");
 
 	}
 
 	@Test
 	@Transactional
 	public void testGetUserThumb()
 	{
 		final User ananth = new Student("ananth", "pass");
 		final Profile ananthProfile = new Profile(ananth);
 		// set some dummy data for image
 		ananthProfile.changeThumb("dummy data".getBytes());
 		profileDao.save(ananthProfile);
 
 		final byte[] image = profileDao.getThumb(ananth.getId());
 		assertNotNull("should return the data", image);
 		assertEquals("image data should be equal to  'dummy data' ", new String(image),
 				"dummy data");
 
 	}
 
 	@Test
 	@Transactional
 	public void testPrivacyField()
 	{
 
 		final User ananth = new Student("ananth", "pass");
 		final Profile ananthProfile = new Profile(ananth);
		ananthProfile.setHomeTown(new PrivacyModel("testValue", Access.EVERYONE));
 		profileDao.save(ananthProfile);
 		// flush the session so we can get the record using JDBC template
 		SessionFactoryUtils.getSession(sessionFactory, false).flush();
 
		PrivacyModel test = ananthProfile.getHomeTown();
 		assertEquals("testValue", test.getValue());
 		assertEquals(Access.EVERYONE, test.getPrivacy());
 	}
 }

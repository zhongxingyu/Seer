 /*
  *  Weblounge: Web Content Management System
  *  Copyright (c) 2003 - 2011 The Weblounge Team
  *  http://entwinemedia.com/weblounge
  *
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public License
  *  as published by the Free Software Foundation; either version 2
  *  of the License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public License
  *  along with this program; if not, write to the Free Software Foundation
  *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
  */
 
 package ch.entwine.weblounge.security;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import ch.entwine.weblounge.common.impl.security.PasswordImpl;
 import ch.entwine.weblounge.common.impl.security.RoleImpl;
 import ch.entwine.weblounge.common.impl.security.UserImpl;
 import ch.entwine.weblounge.common.security.DigestType;
 import ch.entwine.weblounge.common.security.Password;
 import ch.entwine.weblounge.common.security.Role;
 import ch.entwine.weblounge.common.security.SecurityService;
 import ch.entwine.weblounge.common.security.SiteDirectory;
 import ch.entwine.weblounge.common.security.User;
 import ch.entwine.weblounge.common.site.Site;
 
 import org.easymock.EasyMock;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.Set;
 
 /**
  * Tests the combined user and role directory service.
  */
 public class DirectoryServiceImplTest {
 
   /** The user and role directory */
   protected DirectoryServiceImpl directory = null;
 
   /** An organization */
   protected Site site = null;
 
   /** Login name */
   protected String login = "john";
 
   /** A user */
   protected User john = null;
 
   /** Another user */
   protected User johnAlterEgo = null;
 
   /** A first role */
   protected Role roleA = new RoleImpl("test:role_a");
 
   /** A second role */
   protected Role roleB = new RoleImpl("test:role_b");
 
   /** A third role */
   protected Role roleC = new RoleImpl("test:role_c");
 
   /** the secret password */
   protected Password password = new PasswordImpl("secret", DigestType.plain);
 
   @Before
   public void setUp() {
     site = EasyMock.createNiceMock(Site.class);
     EasyMock.expect(site.getIdentifier()).andReturn("testsite").anyTimes();
     EasyMock.replay(site);
 
     User john = new UserImpl(login);
     john.addPublicCredentials(roleA);
     john.addPublicCredentials(roleB);
 
     User johnAlterEgo = new UserImpl(login);
     johnAlterEgo.addPrivateCredentials(password);
     johnAlterEgo.addPublicCredentials(roleB);
     johnAlterEgo.addPublicCredentials(roleC);
 
     SiteDirectory directoryA = EasyMock.createNiceMock(SiteDirectory.class);
     EasyMock.expect(directoryA.getIdentifier()).andReturn(site.getIdentifier()).anyTimes();
    EasyMock.expect(directoryA.loadUser((String) EasyMock.anyObject(), null)).andReturn(john).anyTimes();
     EasyMock.replay(directoryA);
 
     SiteDirectory directoryB = EasyMock.createNiceMock(SiteDirectory.class);
     EasyMock.expect(directoryB.getIdentifier()).andReturn(site.getIdentifier()).anyTimes();
    EasyMock.expect(directoryB.loadUser((String) EasyMock.anyObject(), null)).andReturn(johnAlterEgo).anyTimes();
     EasyMock.replay(directoryB);
 
     SecurityService securityService = EasyMock.createNiceMock(SecurityService.class);
     EasyMock.expect(securityService.getSite()).andReturn(site).anyTimes();
     EasyMock.replay(securityService);
 
     directory = new DirectoryServiceImpl();
     directory.setSecurityService(securityService);
     directory.addDirectoryProvider(directoryA);
     directory.addDirectoryProvider(directoryB);
   }
 
   /**
    * Test for {@link ch.entwine.weblounge.kernel.security.}
    * 
    * @throws Exception
    */
   @Test
   public void testUserMerge() throws Exception {
    User mergedUser = directory.loadUser(login, null);
 
     Set<Object> roles = mergedUser.getPublicCredentials(Role.class);
     assertTrue(roles.contains(roleA));
     assertTrue(roles.contains(roleB));
     assertTrue(roles.contains(roleC));
 
     Set<Object> passwords = mergedUser.getPrivateCredentials(Password.class);
     assertEquals(1, passwords.size());
     assertTrue(passwords.iterator().next() instanceof Password);
     Password pw = (Password)passwords.iterator().next();
     assertEquals(password.getPassword(), pw.getPassword());
     assertEquals(login, mergedUser.getLogin());
   }
 
 }

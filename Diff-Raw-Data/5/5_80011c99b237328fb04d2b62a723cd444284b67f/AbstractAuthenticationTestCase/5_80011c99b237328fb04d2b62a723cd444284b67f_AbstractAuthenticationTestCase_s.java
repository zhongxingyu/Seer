 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2012, Red Hat, Inc., and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
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
 
 package org.picketbox.cdi.test.authentication;
 
 import static junit.framework.Assert.assertTrue;
 
 import javax.enterprise.event.Observes;
 import javax.inject.Inject;
 
 import org.junit.After;
 import org.junit.Before;
 import org.picketbox.cdi.test.arquillian.AbstractArquillianTestCase;
 import org.picketbox.core.authentication.credential.UsernamePasswordCredential;
import org.picketbox.core.authentication.event.UserAuthenticatedEvent;
 import org.picketlink.Identity;
 import org.picketlink.credential.LoginCredentials;
 import org.picketlink.idm.credential.PasswordCredential;
 
 /**
  * <p>Base class for test cases that needs to have an authenticated user during the tests.</p>
  *
  * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
  *
  */
 public abstract class AbstractAuthenticationTestCase extends AbstractArquillianTestCase {
 
     protected static final String USER_NAME = "admin";
     protected static final String USER_PASSWORD = "admin";
 
     @Inject
     protected Identity identity;
 
     @Inject
     protected LoginCredentials credential;
 
     /**
      * <p>Authenticates the user</p>
      *
      * @throws Exception
      */
     @Before
     public void onSetup() throws Exception {
         populateUserCredential();
         this.identity.login();
         assertTrue(this.identity.isLoggedIn());
 
     }
 
     /**
      * <p>Logouts the user after a test execution.</p>
      */
     @After
     public void onFinish() {
         this.identity.logout();
     }
 
     /**
      * <p>
      * Populates the {@link LoginCredential} with the username and password.
      * </p>
      */
     private void populateUserCredential() {
         this.credential.setUserId(USER_NAME);
         this.credential.setCredential(new UsernamePasswordCredential(USER_NAME, USER_PASSWORD));
     }
 
    public void onSuccessfullAuthentication(@Observes UserAuthenticatedEvent event) {
 
     }
 }

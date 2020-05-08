 package com.smash.revolance.ui.explorer.secured;
 
 /*
  * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  * Revolance-UI-Explorer
  * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  * Copyright (C) 2012 - 2013 RevoLance
  * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as
  * published by the Free Software Foundation, either version 3 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/gpl-3.0.html>.
  * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  */
 
 import com.smash.revolance.ui.explorer.UserExplorer;
 import com.smash.revolance.ui.materials.SecuredApplication;
 import com.smash.revolance.ui.model.application.Application;
 import com.smash.revolance.ui.model.element.ElementNotFound;
 import com.smash.revolance.ui.model.page.api.Page;
 import com.smash.revolance.ui.model.user.User;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
import org.junit.Ignore;
 import org.junit.Test;
 
 import java.io.File;
 
 import static junit.framework.Assert.assertNotNull;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.core.Is.is;
 
 /**
  * User: wsmash
  * Date: 26/01/13
  * Time: 17:50
  */
@Ignore
 public class SecuredApplicationTest
 {
     private static String      target;
     private static Application app;
 
     private static final String domain;
 
     private static final String securedWebsite;
 
     private static final String HOME;
 
     private static final String login;
 
     private static final String changePasswd;
 
     static
     {
         securedWebsite = new File( new File( "" ).getAbsoluteFile(), "src/test/resources/secured-website" ).getAbsolutePath();
 
         target = new File( new File( "" ).getAbsoluteFile(), "target" ).getAbsolutePath();
 
         domain = "file://" + securedWebsite;
         HOME = domain + "/home.html";
         login = domain + "/login.html";
         changePasswd = domain + "/change-passwd.html";
 
     }
 
     private static User        administrator;
     private static User        user_A;
     private static User        user_B;
 
     @BeforeClass
     public static void setUp() throws Exception
     {
         administrator = createUser("administrator");
         administrator.setApplication(new SecuredApplication());
         checkUser( administrator );
 
         user_A = createUser("user_A");
         user_A.setApplication(new SecuredApplication());
         checkUser(user_A);
 
         user_B = createUser("user_B");
         user_B.setApplication(new SecuredApplication());
         checkUser(user_B);
     }
 
     private static User createUser(String userName)
     {
         User user = new User(userName);
 
         user.setBrowserWidth(800);
         user.setBrowserHeight(600);
         user.setFollowButtons(true);
         user.setFollowLinks(true);
         user.setExploreVariantsEnabled(false);
         user.enablePageScreenshot(true);
         user.enablePageElementScreenshot(false);
 
        user.setBrowserType( "PhantomJS" );
         user.setDomain(domain);
         user.setHome(HOME);
 
         return user;
     }
 
     private static void checkUser(User user)
     {
         assertNotNull( user );
         assertThat( user.getBrowserHeight(), is( 600 ) );
         assertThat( user.getBrowserWidth(), is( 800 ) );
         assertThat( user.isPageScreenshotEnabled(), is( true ) );
         assertThat( user.isPageElementScreenshotEnabled(), is( false ) );
         assertThat( user.wantsToFollowLinks(), is( true ) );
         assertThat( user.wantsToFollowButtons(), is( true ) );
         assertThat( user.wantsToExploreVariants(), is( false ) );
     }
 
     @AfterClass
     public static void tearDown() throws Exception
     {
         administrator.stopBot();
         user_A.stopBot();
         user_B.stopBot();
     }
 
     @Test
     public void testerShouldDetectEnforcementOnASecuredWebsite() throws Exception, ElementNotFound
     {
         File tmp = File.createTempFile("exploration", ".log");
 
         new UserExplorer( administrator, tmp, 360 ).explore( );
 
         Page adminPage = administrator.getSiteMap().findPageByUrl(HOME).getInstance();
 
         assertThat( "Wrong number of buttons for administrator", adminPage.getButtons().size(), is( 2 ) );
         assertThat( "Wrong number of links for administrator", adminPage.getLinks().size(), is( 2 ) );
         assertThat( "Missing link 'link-1' for administrator", adminPage.getLink( "link-1" ), notNullValue() );
         assertThat( "Missing link 'link-2' for administrator", adminPage.getLink( "link-2" ), notNullValue() );
         assertThat( "Missing link 'button-1' for administrator", adminPage.getButton( "button-1" ), notNullValue() );
         assertThat( "Missing link 'button-2' for administrator", adminPage.getButton( "button-2" ), notNullValue() );
 
         new UserExplorer( user_A, tmp, 360 ).explore( );
 
         Page userAPage = user_A.getSiteMap().findPageByUrl( HOME ).getInstance();
 
         assertThat( "Wrong number of links for user_A", userAPage.getLinks().size(), is( 2 ) );
         assertThat( "Wrong number of buttons for user_A", userAPage.getButtons().size(), is( 0 ) );
         assertThat( "Missing link 'link-1' for user_A", userAPage.getLink( "link-1" ), notNullValue() );
         assertThat( "Missing link 'link-2' for user_A", userAPage.getLink( "link-2" ), notNullValue() );
 
         new UserExplorer( user_B, tmp, 360 ).explore();
 
         Page userBPage = user_B.getSiteMap().findPageByUrl( HOME ).getInstance();
 
         assertThat( "Wrong number of links for user_B", userBPage.getLinks().size(), is( 0 ) );
         assertThat( "Wrong number of buttons for user_B", userBPage.getButtons().size(), is( 2 ) );
         assertThat( "Missing link 'button-1' for user_A", userBPage.getButton( "button-1" ), notNullValue() );
         assertThat( "Missing link 'button-2' for user_A", userBPage.getButton( "button-2" ), notNullValue() );
 
     }
 
 }

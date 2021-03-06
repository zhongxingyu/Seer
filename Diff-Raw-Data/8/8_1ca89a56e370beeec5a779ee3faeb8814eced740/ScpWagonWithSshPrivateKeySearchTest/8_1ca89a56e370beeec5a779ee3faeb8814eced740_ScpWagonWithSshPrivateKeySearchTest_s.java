 package org.apache.maven.wagon.providers.ssh;
 
 /*
  * Copyright 2001-2004 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import org.apache.maven.wagon.authentication.AuthenticationInfo;
 
 /**
  * @author <a href="michal.maczka@dimatics.com">Michal Maczka</a>
  * @version $Id$
  */
 public class ScpWagonWithSshPrivateKeySearchTest
     extends WagonTestCase
 {
     public ScpWagonWithSshPrivateKeySearchTest( String testName )
     {
         super( testName );
        
     }
 
     protected String getProtocol()
     {
         return "scp";
     }
 
     protected String getTestRepositoryUrl()
     {
         return "scp://beaver.codehaus.org//home/users/" + getUserName() + "/public_html";
     }
 
     protected AuthenticationInfo getAuthInfo()
     {
         AuthenticationInfo authInfo = new AuthenticationInfo();
 
         String userName = getUserName();
 
         authInfo.setUserName( userName );
 
         authInfo.setPassphrase( "" );
 
         authInfo.setGroup( getUserName() );
 
         return authInfo;
     }
 
     private String getUserName()
     {
 
         String retValue = System.getProperty( "testuser.name" );
 
         if ( retValue == null )
         {
             retValue = System.getProperty( "user.name" );
         }
 
         return retValue;
     }
 }

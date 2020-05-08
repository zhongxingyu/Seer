 /*
  * Copyright 2008-${YEAR} Zuse Institute Berlin (ZIB)
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package de.zib.gndms.kit.security;
 
 import de.zib.gndms.common.rest.MyProxyToken;
 import de.zib.gndms.kit.access.MyProxyFactory;
 import org.ietf.jgss.GSSCredential;
 
 /**
  * @author Maik Jorra
  * @email jorra@zib.de
  * @date 22.11.11  17:21
  * @brief Obtains credentials from a myproxy server.
  */
 public class MyProxyCredentialProvider extends GSSCredentialProvider {
 
     private String user;
     private String passwd;
     private final MyProxyFactory factory;
     private CredentialInstaller credentialInstaller;
     private MyProxyToken.FetchMethod fetchMethod;
 
 
     public MyProxyCredentialProvider( MyProxyFactory factory ) {
         super();
         this.factory = factory;
     }
 
 
     public MyProxyCredentialProvider( MyProxyFactory factory, String user, String passwd ) {
 
         this.user = user;
         this.passwd = passwd;
         this.factory = factory;
     }
 
 
     public MyProxyCredentialProvider( MyProxyFactory factory, final MyProxyToken token ) {
         this.factory = factory;
         this.user = token.getLogin();
         this.passwd = token.getPassword();
         this.fetchMethod = token.getFetchMethod();
     }
 
 
     @Override
     public GSSCredential getCredential() {
         GSSCredential cred = super.getCredential();
         if ( cred == null ) {
             try {
                switch ( fetchMethod ) {
                    case GET:
                        cred = factory.getMyProxyClient().fetch( user, passwd );
                    case RETRIEVE:
                        cred = factory.getMyProxyClient().retrieve( user, passwd );
                }
                 setCredential( cred );
             } catch ( Exception e ) {
                 throw new RuntimeException( e );
             }
         }
         return cred;
     }
 
 
     @Override
     public void setInstaller( final CredentialInstaller credentialInstaller ) {
         this.credentialInstaller = credentialInstaller;
     }
 
 
     public String getUser() {
         return user;
     }
 
 
     public void setUser( String user ) {
         this.user = user;
     }
 
 
     public void installCredentials( Object o ) {
         credentialInstaller.installCredentials(
                 credentialInstaller.getReceiverClass().cast( o )
                 , getCredential() );
     }
 }

 /*
  *  Licensed to the Apache Software Foundation (ASF) under one
  *  or more contributor license agreements.  See the NOTICE file
  *  distributed with this work for additional information
  *  regarding copyright ownership.  The ASF licenses this file
  *  to you under the Apache License, Version 2.0 (the
  *  "License"); you may not use this file except in compliance
  *  with the License.  You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing,
  *  software distributed under the License is distributed on an
  *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  *  KIND, either express or implied.  See the License for the
  *  specific language governing permissions and limitations
  *  under the License.
  *
  */
 package org.apache.directory.server.core.operations.bind;
 
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.Hashtable;
 
 import javax.naming.Context;
 import javax.naming.InvalidNameException;
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.naming.OperationNotSupportedException;
 import javax.naming.directory.DirContext;
 import javax.naming.directory.InitialDirContext;
 import javax.naming.directory.SearchControls;
 import javax.naming.directory.SearchResult;
 
 import org.apache.directory.server.core.DirectoryService;
 import org.apache.directory.server.core.integ.AbstractLdapTestUnit;
 import org.apache.directory.server.core.integ.FrameworkRunner;
 import org.apache.directory.server.core.jndi.CoreContextFactory;
import org.apache.directory.shared.i18n.I18n;
 import org.apache.directory.shared.ldap.constants.JndiPropertyConstants;
 import org.apache.directory.shared.ldap.exception.LdapAuthenticationException;
 import org.apache.directory.shared.ldap.exception.LdapNameNotFoundException;
 import org.apache.directory.shared.ldap.message.AliasDerefMode;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 
 /**
  * Test the Simple BindRequest
  *
  * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
  * @version $Rev$
  */
 @RunWith ( FrameworkRunner.class )
 public class SimpleBindIT extends AbstractLdapTestUnit
 {
 
     /**
      * A method to do a search
      */
     private NamingEnumeration<SearchResult> search( DirContext ctx, String baseDn, String filter, int scope ) throws NamingException
     {
         SearchControls controls = new SearchControls();
         controls.setSearchScope( scope );
         controls.setDerefLinkFlag( false );
         controls.setReturningAttributes( new String[]{ "*", "+"} );
         ctx.addToEnvironment( JndiPropertyConstants.JNDI_LDAP_DAP_DEREF_ALIASES,
                 AliasDerefMode.NEVER_DEREF_ALIASES.getJndiValue() );
 
         NamingEnumeration<SearchResult> list = ctx.search( baseDn, filter, controls );
         return list;
     }
     
     
     /**
      * try to connect using a known user/password and read an entry.
      * 
      * @throws Exception on error
      */
     @Test
     public void testSimpleBindUserPassword()
     {
         // We will bind using JNDI
         // Set up the environment for creating the initial context
         Hashtable<String, Object> env = new Hashtable<String, Object>();
         env.put( DirectoryService.JNDI_KEY, service );
         env.put( Context.INITIAL_CONTEXT_FACTORY, CoreContextFactory.class.getName() );
         env.put( Context.PROVIDER_URL, "ou=system" );
 
         // Authenticate as admin and password "secret"
         env.put(Context.SECURITY_AUTHENTICATION, "simple");
         env.put(Context.SECURITY_PRINCIPAL, "uid=admin,ou=system");
         env.put(Context.SECURITY_CREDENTIALS, "secret");
 
         DirContext ctx = null;
         
         // Create the initial context
         try
         {
             ctx = new InitialDirContext(env);
         }
         catch ( NamingException ne )
         {
             fail();
         }
         
         try
         {
             ctx.close();
         }
         catch ( NamingException ne )
         {
             fail();
         }
     }
     
     
     /**
      * try to connect using a known user but with a bad password: we should get a invalidCredentials error.
      * 
      * @throws Exception on error
      */
     @Test
     public void testSimpleBindUserBadPassword()
     {
         // We will bind using JNDI
         // Set up the environment for creating the initial context
         Hashtable<String, Object> env = new Hashtable<String, Object>();
         env.put( DirectoryService.JNDI_KEY, service );
         env.put( Context.INITIAL_CONTEXT_FACTORY, CoreContextFactory.class.getName() );
         env.put( Context.PROVIDER_URL, "ou=system" );
 
         // Authenticate as admin and password "badsecret"
         env.put(Context.SECURITY_AUTHENTICATION, "simple");
         env.put(Context.SECURITY_PRINCIPAL, "uid=admin,ou=system");
         env.put(Context.SECURITY_CREDENTIALS, "badsecret");
 
         // Create the initial context
         try
         {
             new InitialDirContext(env);
 
             // We should not be connected
             fail();
         }
         catch ( LdapAuthenticationException lae )
         {
             assertTrue( true );
         }
         catch ( NamingException ne )
         {
             fail();
         }
     }
     
     
     /**
      * try to connect using a user with an invalid DN: we should get a invalidDNSyntax error.
      * 
      * @throws Exception on error
      */
     @Test
     public void testSimpleBindBadUserPassword()
     {
         // We will bind using JNDI
         // Set up the environment for creating the initial context
         Hashtable<String, Object> env = new Hashtable<String, Object>();
         env.put( DirectoryService.JNDI_KEY, service );
         env.put( Context.INITIAL_CONTEXT_FACTORY, CoreContextFactory.class.getName() );
         env.put( Context.PROVIDER_URL, "ou=system" );
 
         // Authenticate as admin and password "secret"
         env.put(Context.SECURITY_AUTHENTICATION, "simple");
         env.put(Context.SECURITY_PRINCIPAL, "admin");
         env.put(Context.SECURITY_CREDENTIALS, "secret");
 
         // Create the initial context
         try
         {
             new InitialDirContext(env);
 
             // We should not be connected
             fail();
         }
         catch ( InvalidNameException ine )
         {
        	assertTrue(I18n.err(I18n.ERR_04201), ine.getMessage().startsWith(I18n.ERR_04201));
         }
         catch ( NamingException ne )
         {
             fail();
         }
     }
     
 
     /**
      * try to connect using a unknown user: we should get a invalidCredentials error.
      * 
      * @throws Exception on error
      */
     @Test
     public void testSimpleBindUnknowUserPassword()
     {
         // We will bind using JNDI
         // Set up the environment for creating the initial context
         Hashtable<String, Object> env = new Hashtable<String, Object>();
         env.put( DirectoryService.JNDI_KEY, service );
         env.put( Context.INITIAL_CONTEXT_FACTORY, CoreContextFactory.class.getName() );
         env.put( Context.PROVIDER_URL, "ou=system" );
 
         // Authenticate as uid=unknown and password "secret"
         env.put(Context.SECURITY_AUTHENTICATION, "simple");
         env.put(Context.SECURITY_PRINCIPAL, "uid=unknown,ou=system");
         env.put(Context.SECURITY_CREDENTIALS, "secret");
 
         // Create the initial context
         try
         {
             new InitialDirContext(env);
 
             // We should not be connected
             fail();
         }
         catch ( LdapAuthenticationException lae )
         {
             lae.printStackTrace();
        	assertTrue(org.apache.directory.server.i18n.I18n.err(org.apache.directory.server.i18n.I18n.ERR_229), 
        			lae.getMessage().startsWith(org.apache.directory.server.i18n.I18n.ERR_229));
         }
         catch ( NamingException ne )
         {
             fail();
         }
     }
     
     
     /**
      * covers the anonymous authentication : we should be able to read the rootDSE, but that's it
      * 
      * @throws Exception on error
      */
     @Test
     public void testSimpleBindNoUserNoPassword()
     {
         // We will bind using JNDI
         // Set up the environment for creating the initial context
         Hashtable<String, Object> env = new Hashtable<String, Object>();
         env.put( DirectoryService.JNDI_KEY, service );
         env.put( Context.INITIAL_CONTEXT_FACTORY, CoreContextFactory.class.getName() );
         
         // Bind on the rootDSE
         env.put( Context.PROVIDER_URL, "" );
 
         // Authenticate as admin and password "secret"
         env.put(Context.SECURITY_AUTHENTICATION, "simple");
         env.put(Context.SECURITY_PRINCIPAL, "");
         env.put(Context.SECURITY_CREDENTIALS, "");
 
         DirContext ctx = null;
         
         // Create the initial context
         try
         {
             ctx = new InitialDirContext(env);
         }
         catch ( NamingException ne )
         {
             fail();
         }
         
         // We should be anonymous here. 
         // Check that we can read the rootDSE
         try
         {
             NamingEnumeration<SearchResult> list = search( ctx, "", "(ObjectClass=*)", SearchControls.OBJECT_SCOPE );
             
             assertNotNull( list );
             
             while ( list.hasMore() )
             {
                 SearchResult result = list.next();
                 assertNotNull( result );
             }
         }
         catch ( NamingException ne )
         {
             fail();
         }
 
         // Check that we cannot read another entry being anonymous
         try
         {
             NamingEnumeration<SearchResult> list = search( ctx, "uid=admin, ou=system", "(ObjectClass=*)", SearchControls.OBJECT_SCOPE );
             
             assertNotNull( list );
             assertFalse( list.hasMore() );
         }
         catch ( NamingException ne )
         {
             fail();
         }
 
         try
         {
             ctx.close();
         }
         catch ( NamingException ne )
         {
             fail();
         }
     }
     
     
     /**
      * covers the Unauthenticated case : we should get a UnwillingToPerform error.
      * 
      * @throws Exception on error
      */
     @Test
     public void testSimpleBindUserNoPassword()
     {
         // We will bind using JNDI
         // Set up the environment for creating the initial context
         Hashtable<String, Object> env = new Hashtable<String, Object>();
         env.put( DirectoryService.JNDI_KEY, service );
         env.put( Context.INITIAL_CONTEXT_FACTORY, CoreContextFactory.class.getName() );
         
         // Bind on the rootDSE
         env.put( Context.PROVIDER_URL, "" );
 
         // Authenticate as admin and password "secret"
         env.put(Context.SECURITY_AUTHENTICATION, "simple");
         env.put(Context.SECURITY_PRINCIPAL, "uid=admin,ou=system");
         env.put(Context.SECURITY_CREDENTIALS, "");
 
         // Create the initial context
         try
         {
             new InitialDirContext(env);
         }
         catch ( OperationNotSupportedException onse )
         {
             assertEquals( "Cannot Bind for DN uid=admin,ou=system", onse.getMessage() );
         }
         catch ( NamingException ne )
         {
             fail();
         }
     }
     
     
     /**
      * not allowed by the server. We should get a invalidCredentials error.
      * 
      * @throws Exception on error
      */
     @Test
     public void testSimpleBindNoUserPassword() throws Exception
     {
         // We will bind using JNDI
         // Set up the environment for creating the initial context
         Hashtable<String, Object> env = new Hashtable<String, Object>();
         env.put( DirectoryService.JNDI_KEY, service );
         env.put( Context.INITIAL_CONTEXT_FACTORY, CoreContextFactory.class.getName() );
         
         // Bind on the rootDSE
         env.put( Context.PROVIDER_URL, "" );
 
         // Authenticate as admin and password "secret"
         env.put(Context.SECURITY_AUTHENTICATION, "simple");
         env.put(Context.SECURITY_PRINCIPAL, "");
         env.put(Context.SECURITY_CREDENTIALS, "secret");
 
         // Create the initial context
         try
         {
             new InitialDirContext(env);
         }
         catch ( LdapNameNotFoundException lnnfe )
         {
             assertTrue( true );
         }
         catch ( NamingException ne )
         {
             fail();
         }
     }
 }

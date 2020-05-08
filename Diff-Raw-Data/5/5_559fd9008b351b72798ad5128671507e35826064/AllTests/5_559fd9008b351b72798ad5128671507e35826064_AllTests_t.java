 /*
  * Jamm
  * Copyright (C) 2002 Dave Dribin and Keith Garner
  *  
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package jamm;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 import junit.extensions.TestSetup;
 
 import java.util.Hashtable;
 
 import javax.naming.Context;
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.naming.directory.InitialDirContext;
 import javax.naming.directory.DirContext;
 import javax.naming.directory.SearchResult;
 import javax.naming.directory.SearchControls;
 import javax.naming.directory.BasicAttribute;
 import javax.naming.directory.BasicAttributes;
 
 import jamm.ldap.LdapPassword;
 
 /**
  * A top level test suite for all the backend classes.  It connects to
  * the server and creates a known set of data into the directory prior
  * to running the tests.
  */
 public class AllTests
 {
     /**
      * Returns a test suite for all backend tests.
      *
      * @return A top-level test suite
      */
     public static Test suite()
     {
         TestSuite suite;
         TestSetup wrapper;
 
         suite = new TestSuite();
         suite.addTest(jamm.util.AllTests.suite());
         suite.addTest(jamm.ldap.AllTests.suite());
         suite.addTest(jamm.backend.AllTests.suite());
         
                 // This wraps all lower test suites around global setup and
         // tear down routines.
         wrapper = new TestSetup(suite)
             {
                 public void setUp()
                 {
                     try
                     {
                         oneTimeSetUp();
                     }
                     catch (Exception e)
                     {
                         e.printStackTrace();
                         System.exit(1);
                     }
                 }
 
                 public void tearDown()
                 {
                     oneTimeTearDown();
                 }
             };
         
         return wrapper;
     }
 
     /**
      * Setup that needs to be performed only once.
      */
     private static void oneTimeSetUp()
         throws Exception
     {
         setupLdapData();
         setupLdapPassword();
     }
 
     /**
      * Clears out LDAP data and adds initial dataset.
      *
      * Add this to the end of your <tt>slapd.conf</tt> to create a
      * separate database for Jamm testing. <b>Note</b>: Long lines
      * cannot be continued with a backslash in
      * <code>slapd.conf</code>.  It is only done here for readability.
      *
      * <pre>
      * database      ldbm
      * suffix        "dc=jamm,dc=test"
      * rootdn        "cn=Manager,dc=jamm,dc=test"
      * rootpw        jammtest
      * directory     /var/lib/ldap/jamm
      * access to dn=".*jvd=([^,]+),o=hosting,dc=jamm,dc=test"
      *     by self write
      *     by group/organizationalRole/roleOccupant="cn=postmaster,jvd=$1,\
      *        o=hosting,dc=jamm,dc=test" write
      *     by * read
      *
      * access to *
      *     by self write
      *     by * read
      * </pre>
      */
 
     private static void setupLdapData()
         throws Exception
     {
         Hashtable   env;
         DirContext  context;
         DirContext  element;
         BasicAttributes attributes;
         BasicAttribute objectClass;
 
         // Bind as manager
         env = new Hashtable();
         env.put(Context.INITIAL_CONTEXT_FACTORY, 
                 "com.sun.jndi.ldap.LdapCtxFactory");
         env.put(Context.PROVIDER_URL,
                 "ldap://" + LdapConstants.HOST + ":" + LdapConstants.PORT +
                 "/");
         env.put(Context.SECURITY_AUTHENTICATION, "simple");
         env.put(Context.SECURITY_PRINCIPAL, LdapConstants.MGR_DN);
         env.put(Context.SECURITY_CREDENTIALS, LdapConstants.MGR_PW);
         context = new InitialDirContext(env);
 
         // Destroy all elements
         destroySubtree(context, "dc=jamm, dc=test");
 
         // Add root
         attributes = new BasicAttributes();
         objectClass = new BasicAttribute("objectClass");
         objectClass.add("top");
        objectClass.add("dcObject");
         attributes.put(objectClass);
        attributes.put("dc", "jamm");
        element = context.createSubcontext("dc=jamm,dc=test",
                                            attributes);
         element.close();
 
         // Add manager
         attributes = new BasicAttributes();
         objectClass = new BasicAttribute("objectClass");
         objectClass.add("top");
         objectClass.add("organizationalRole");
         attributes.put(objectClass);
         attributes.put("cn", "Manager");
         element = context.createSubcontext("cn=Manager,dc=jamm, dc=test",
                                            attributes);
         element.close();
 
         // Add hosting
         attributes = new BasicAttributes();
         objectClass = new BasicAttribute("objectClass");
         objectClass.add("top");
         objectClass.add("organization");
         attributes.put(objectClass);
         attributes.put("o", "hosting");
         element = context.createSubcontext("o=hosting, dc=jamm, dc=test",
                                            attributes);
         element.close();
 
         // Add domain1.test
         attributes = new BasicAttributes();
         objectClass = new BasicAttribute("objectClass");
         objectClass.add("top");
         objectClass.add("JammVirtualDomain");
         attributes.put(objectClass);
         attributes.put("jvd", "domain1.test");
         attributes.put("postfixTransport", "virtual:");
         attributes.put("editAliases", "TRUE");
         attributes.put("editAccounts", "TRUE");
         attributes.put("editPostmasters", "FALSE");
         attributes.put("editCatchalls", "FALSE");
         element = context.createSubcontext(
             "jvd=domain1.test, o=hosting, dc=jamm, dc=test", attributes);
         element.close();
 
         // Add acct1@domain1.test
         attributes = new BasicAttributes();
         objectClass = new BasicAttribute("objectClass");
         objectClass.add("top");
         objectClass.add("jammMailAccount");
         attributes.put(objectClass);
         attributes.put("mail", "acct1@domain1.test");
         attributes.put("homeDirectory", "/home/vmail/domains");
         attributes.put("mailbox", "domain1.test/acct1");
         // This password is "acct1pw
         attributes.put("userPassword",
                        "{SSHA}tk3w4vV6xghX4r7P0F1EAeA55jo53sSO");
         element = context.createSubcontext(
             "mail=acct1@domain1.test, jvd=domain1.test, o=hosting, dc=jamm, " +
             "dc=test", attributes);
         element.close();
 
         // Add acct2@domain1.test
         attributes = new BasicAttributes();
         objectClass = new BasicAttribute("objectClass");
         objectClass.add("top");
         objectClass.add("jammMailAccount");
         attributes.put(objectClass);
         attributes.put("mail", "acct2@domain1.test");
         attributes.put("homeDirectory", "/home/vmail/domains");
         attributes.put("mailbox", "domain1.test/acct2");
         // This password is "acct2pw
         attributes.put("userPassword",
                        "{SSHA}z0pxwHQV6nvrFLMW07ZgOqjoFRPWzoPk");
         element = context.createSubcontext(
             "mail=acct2@domain1.test, jvd=domain1.test, o=hosting, dc=jamm, " +
             "dc=test", attributes);
         element.close();
 
         context.close();
     }
 
     /**
      * Recursively destroys an entire subtree and all its elements.
      *
      * @param context Context to destroy from
      * @param dn DN to destroy
      */
     private static void destroySubtree(DirContext context, String dn)
     {
         SearchControls controls;
         NamingEnumeration results;
         SearchResult element;
         String rdn;
         
         try
         {
             controls = new SearchControls();
             controls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
             results = context.search(dn, "objectClass=*", controls);
 
             while (results.hasMore())
             {
                 element = (SearchResult) results.next();
                 rdn = element.getName();
                 if (rdn.equals(""))
                 {
                     rdn = dn;
                 }
                 else
                 {
                     rdn = rdn + "," + dn;
                 }
 
                 destroySubtree(context, rdn);
             }
             results.close();
 
             context.destroySubcontext(dn);
         }
         catch (NamingException e)
         {
             // An error may be ok if, for example, there is no
             // existing entries.  Just spit out a warning and continue.
             System.err.println("Warning: could not delete tree: " + e);
         }
     }
     
     /**
      * Sets up the LDAP password stuff to use java.util.Random.
      */
     private static void setupLdapPassword()
     {
         // Use normal random as SecureRandom takes too long to
         // initialize for testing.
         LdapPassword.setRandomClass("java.util.Random");
     }
     
     /**
      * Does nothing.
      */
     private static void oneTimeTearDown()
     {
     }
 }

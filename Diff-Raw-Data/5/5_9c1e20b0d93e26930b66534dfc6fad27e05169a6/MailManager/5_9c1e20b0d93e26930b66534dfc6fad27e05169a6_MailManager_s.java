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
 
 package jamm.backend;
 
 import java.util.Map;
 import java.util.Set;
 import java.util.List;
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Collection;
 
 import javax.naming.NamingException;
 import javax.naming.AuthenticationException;
 
 import jamm.ldap.LdapFacade;
 import jamm.ldap.LdapPassword;
 import jamm.ldap.PasswordScheme;
 
 /**
  * Manages the backend mail LDAP subtree.  All mail data is stored in
  * a subtree of an LDAP directory.  This manipulates that data,
  * creating, deleting, and modifying LDAP entries, but not exposing
  * any of the LDAP complexities to the user of this object.  A
  * connection is not kept around, but created as needed.
  */
 public class MailManager
 {
     /**
      * Create a mail manager.  The host and port number of the LDAP
      * server is supplied.  If the bind DN is an empty string (""),
      * then an anonymous bind is done, otherwise a a simple bind is
      * done using the given element DN and password.
      *
      * @param host Host name of LDAP server
      * @param port Port number of LDAP server
      * @param base The base DN of the mail subtree
      * @param bindDn The DN of the element to bind as, or the empty
      * string for an anonymous bind.
      * @param bindPassword The password of the bind element
      */
     public MailManager(String host, int port, String base, String bindDn,
                        String bindPassword)
     {
         mHost = host;
         mPort = port;
         mBase = base;
         mBindDn = bindDn;
         mBindPassword = bindPassword;
     }
 
     /**
      * Create a mail manager using the default LDAP port. See {@link
      * #MailManager(String, int, String, String, String) the full
      * constructor} for details.
      *
      * @param host Host name of LDAP server
      * @param base The base DN of the mail subtree
      * @param bindDn The DN of the element to bind as
      * @param bindPassword The password of the bind element
      */
     public MailManager(String host, String base, String bindDn,
                        String bindPassword)
     {
         this(host, 389, base, bindDn, bindPassword);
     }
 
     /**
      * Create a mail manager using the default LDAP port using an
      * anonymous bind. See {@link #MailManager(String, int, String,
      * String, String) the full constructor} for details.
      *
      * @param host Host name of LDAP server
      * @param base The base DN of the mail subtree
      */
     public MailManager(String host, String base)
     {
         this(host, 389, base, "", "");
     }
 
     /**
      * Create a mail manager using an anonymous bind. See {@link
      * #MailManager(String, int, String, String, String) the full
      * constructor} for details.
      *
      * @param host Host name of LDAP server
      * @param port The port number of LDAP server
      * @param base The base DN of the mail subtree
      */
     public MailManager(String host, int port, String base)
     {
         this(host, port, base, "", "");
     }
 
     /**
      * Change the entry to bind as.  To perform an anonymous bind, set
      * the bind DN to be the empty string ("").
      *
      * @param bindDn The DN of the element to bind as, or the empty
      * string for an anonymous bind.
      * @param bindPassword The password of the bind element.
      */
     public void setBindEntry(String bindDn, String bindPassword)
     {
         mBindDn = bindDn;
         mBindPassword = bindPassword;
     }
 
     /**
      * Create a new LDAP facade using the specified LDAP host and
      * port.  It creates a facade with an anonymous bind if the bind
      * DN is the empty string.
      *
      * @return A new LDAP facade
      * @throws NamingException If an error occured
      */
     private LdapFacade getLdap()
         throws NamingException
     {
         LdapFacade ldap = new LdapFacade(mHost, mPort);
         if (mBindDn.equals("") && mBindPassword.equals(""))
         {
             ldap.anonymousBind();
         }
         else
         {
             ldap.simpleBind(mBindDn, mBindPassword);
         }
         return ldap;
     }
     
     /**
      * Closes an LDAP facade if it is not null.  This is handy in
      * finally blocks.
      *
      * @param ldap LDAP facade to close.
      */
     private void closeLdap(LdapFacade ldap)
     {
         if (ldap != null)
         {
             ldap.close();
         }
     }
 
     /**
      * Changes the password for an email account or alias.
      *
      * @param mail Email address to change password for
      * @param newPassword New password for this account or alias.
      * @throws MailManagerException If an error occured.
      */
     public void changePassword(String mail, String newPassword)
         throws MailManagerException
     {
         LdapFacade ldap = null;
 
         try
         {
             ldap = getLdap();
             searchForMail(ldap, mail);
 
             String foundDn = ldap.getResultName();
             String hashedPassword =
                 LdapPassword.hash(PasswordScheme.SSHA_SCHEME, newPassword);
             ldap.modifyElementAttribute(foundDn, "userPassword",
                                         hashedPassword);
 
             if (foundDn.equals(mBindDn))
             {
                 mBindPassword = newPassword;
             }
         }
         catch (NamingException e)
         {
             throw new MailManagerException(e);
         }
         finally
         {
             closeLdap(ldap);
         }
     }
 
     /**
      * Checks to see if we can bind as the specified element.
      *
      * @return True if bind as the element.
      * @throws MailManagerException If an error occured
      */
     public boolean authenticate()
         throws MailManagerException
     {
         LdapFacade ldap = null;
         boolean authenticated = false;
         
         try
         {
             ldap = getLdap();
             authenticated = true;
         }
         catch (AuthenticationException e)
         {
             authenticated = false;
         }
         catch (NamingException e)
         {
             throw new MailManagerException("Could not bind", e);
         }
         finally
         {
             closeLdap(ldap);
         }
 
         return authenticated;
     }
 
     /**
      * Checks if the specified email address is a postmater.
      *
      * @param mail Email address to check
      * @return True if this is a postmaster
      * @throws MailManagerException If an error occured
      */
     public boolean isPostmaster(String mail)
         throws MailManagerException
     {
         LdapFacade ldap = null;
         boolean isPostmaster = false;
         try
         {
             ldap = getLdap();
 
             // Get all users in this domain who have postamster
             // privileges.  The results are full DNs.
             String domain = MailAddress.hostFromAddress(mail);
             String postmasterMail = "postmaster@" + domain;
             searchForMail(ldap, postmasterMail);
             Set postmasters =
                 ldap.getAllResultAttributeValues("roleOccupant");
 
             // Get DN for user in question and see if they are a
             // postmaster.
             searchForMail(ldap, mail);
             String mailDn = ldap.getResultName();
             isPostmaster = postmasters.contains(mailDn);
         }
         catch (NamingException e)
         {
             throw new MailManagerException(e);
         }
         finally
         {
             closeLdap(ldap);
         }
 
         return isPostmaster;
     }
 
     /**
      * Returns a list of all domains that are present in this
      * directory.
      *
      * @return A list of domains, as strings
      * @throws MailManagerException If an error occured
      */
     public List getDomains()
         throws MailManagerException
     {
         LdapFacade ldap = null;
         List  domains = new ArrayList();
         try
         {
             ldap = getLdap();
 
             ldap.searchOneLevel(mBase, "jvd=*");
             while (ldap.nextResult())
             {
                 domains.add(createDomainInfo(ldap));
             }
         }
         catch (NamingException e)
         {
             throw new MailManagerException(e);
         }
         finally
         {
             closeLdap(ldap);
         }
 
         Collections.sort(domains, new DomainNameComparator());
         return domains;
     }
 
     /**
      *
      * @param domainName a <code>String</code> value
      * @return a <code>DomainInfo</code> value
      * @exception MailManagerException if an error occurs
      */
     public DomainInfo getDomain(String domainName)
         throws MailManagerException
     {
         LdapFacade ldap = null;
         DomainInfo domainInfo = null;
         try
         {
             ldap = getLdap();
 
             ldap.searchOneLevel(mBase, "jvd=" + domainName);
             ldap.nextResult();
 
             domainInfo = createDomainInfo(ldap);
         }
         catch (NamingException e)
         {
             throw new MailManagerException(e);
         }
         finally
         {
             closeLdap(ldap);
         }
         
         return domainInfo;
     }
 
     /**
      * Creates a DomainInfo object assuming the ldapfacade points at
      * an appropriate object.
      *
      * @param ldap a <code>LdapFacade</code> value
      * @return a <code>DomainInfo</code> value
      * @exception NamingException if an error occurs
      */
     private DomainInfo createDomainInfo(LdapFacade ldap)
         throws NamingException
     {
         String name = ldap.getResultAttribute("jvd");
         boolean canEditAccounts = stringToBoolean(
             ldap.getResultAttribute("editAccounts"));
         boolean canEditPostmasters = stringToBoolean(
             ldap.getResultAttribute("editPostmasters"));
         /*
           boolean active = stringToBoolean(
           ldap.getResultAttribute("active"));
         */
         return new DomainInfo(name, canEditAccounts, canEditPostmasters, true);
     }
 
     /**
      * Returns true if string passed in is "true", ignoring case.
      * False otherwise.
      *
      * @param string a <code>String</code> value
      * @return a <code>boolean</code> value
      */
     private boolean stringToBoolean(String string)
     {
         return Boolean.valueOf(string).booleanValue();
     }
 
     /**
      * Returns a string in uppercase of TRUE or FALSE
      *
      * @param bool a <code>boolean</code> value
      * @return a <code>String</code> value
      */
     private String booleanToString(boolean bool)
     {
         return String.valueOf(bool).toUpperCase();
     }
 
     /**
      * Modifies the domain's capabilities out of the DomainInfo object.
      *
      * @param domain a <code>DomainInfo</code> value
      * @exception MailManagerException if an error occurs
      */
     public void modifyDomain(DomainInfo domain)
         throws MailManagerException
     {
         LdapFacade ldap = null;
         String domaindn = domainDn(domain.getName());
         try
         {
             ldap = getLdap();
             ldap.modifyElementAttribute(domaindn, "editAccounts",
                                         booleanToString(
                                             domain.getCanEditAccounts()));
             ldap.modifyElementAttribute(domaindn, "editPostmasters",
                                         booleanToString(
                                             domain.getCanEditPostmasters()));
         }
         catch (NamingException e)
         {
             throw new MailManagerException(domain.getName(), e);
         }
         finally
         {
             closeLdap(ldap);
         }
     }
 
     /**
      * Gets the LDAP DN of the specified email address.  Returns
      * <code>null</code> if the address was not found.
      *
      * @param mail Email address to lookup
      * @return The LDAP DN of the address, or <code>null</code>, if
      * not found.
      * @throws MailManagerException If an error occured.
      */
     public String getDnFromMail(String mail)
         throws MailManagerException
     {
         LdapFacade ldap = null;
         String foundDn = null;
         try
         {
             ldap = getLdap();
             searchForMail(ldap, mail);
             foundDn = ldap.getResultName();
         }
         catch (NamingException e)
         {
             throw new MailManagerException(e);
         }
         catch (MailNotFoundException e)
         {
             // This is not an exceptional case for this method
             foundDn = null;
         }
         finally
         {
             closeLdap(ldap);
         }
         return foundDn;
     }
 
     /**
      * Searchs the subtree for an entry matching the specified email
      * address.  If the address is found, the caller can get the
      * results from the LDAP facade without calling
      * <code>nextResult()</code>.  If the address is not found, an
      * exception is thrown.
      *
      * @param ldap The LDAP facade to use for the search
      * @param mail Email address to search for
      * @throws MailNotFoundException If the address is not found
      * @throws NamingException If a JNDI error occured
      * @see jamm.ldap.LdapFacade#nextResult
      */
     private void searchForMail(LdapFacade ldap, String mail)
         throws NamingException, MailNotFoundException
     {
         ldap.searchSubtree(mBase, "mail=" + mail);
 
         if (!ldap.nextResult())
         {
             throw new MailNotFoundException(mail);
         }
     }
 
     /**
      * Creates a new domain with the specified name.  It creates a new
      * subtree for the domain as well as the postmater and abuse
      * aliases, both with no password.
      *
      * @param domain Name of the new domain
      * @throws MailManagerException If an error occured
      */
     public void createDomain(String domain)
         throws MailManagerException
     {
         LdapFacade ldap = null;
         try
         {
             ldap = getLdap();
 
             // Create the domain
             Map attributes = new HashMap();
             attributes.put("objectClass",
                            new String[] {"top", "JammVirtualDomain"});
             attributes.put("jvd", domain);
             attributes.put("postfixTransport", "virtual:");
             attributes.put("editAliases", "TRUE");
             attributes.put("editAccounts", "TRUE");
             attributes.put("editPostmasters", "TRUE");
             attributes.put("editCatchalls", "TRUE");
             String domainDn = domainDn(domain);
             ldap.addElement(domainDn, attributes);
 
             // Create the postmaster
             attributes.clear();
             attributes.put("objectClass",
                            new String[] {"top", "organizationalRole",
                                          ALIAS_OBJECT_CLASS});
             attributes.put("cn", "postmaster");
             attributes.put("mail",
                            MailAddress.addressFromParts("postmaster", domain));
             attributes.put("maildrop", "postmaster");
             String dn = "cn=postmaster," + domainDn;
             attributes.put("roleOccupant", dn);
             ldap.addElement(dn, attributes);
 
             // Create the abuse account
             attributes.clear();
             attributes.put("objectClass",
                            new String[] {"top", ALIAS_OBJECT_CLASS});
             String mail = MailAddress.addressFromParts("abuse", domain);
             attributes.put("mail", mail);
             attributes.put("maildrop", "postmaster");
             ldap.addElement(mailDn(mail), attributes);
         }
         catch (NamingException e)
         {
             throw new MailManagerException("Count not create domain: " +
                                            domain, e);
         }
         finally
         {
             closeLdap(ldap);
         }
     }
 
     /**
      * Create a new alias on an existing domain.  See {@link
      * #createAlias(String, String, String[])} for details.
      *
      * @param domain Domain name
      * @param alias Alias name
      * @param destinations A collection of String objects
      * @throws MailManagerException If an error occured
      */
     public void createAlias(String domain, String alias,
                             Collection destinations)
         throws MailManagerException
     {
         createAlias(domain, alias,
                     (String []) destinations.toArray(new String[0]));
     }
 
     /**
      * Create a new alias on an existing domain.  All previous
      * destinations are replaced with this list of destinations.
      *
      * @param domain Doman mame
      * @param alias Alias name
      * @param destinations An array of destinations
      * @throws MailManagerException If an error occured
      */
     public void createAlias(String domain, String alias, String[] destinations)
         throws MailManagerException
     {
         LdapFacade ldap = null;
         String mail = MailAddress.addressFromParts(alias, domain);
         
         try
         {
             ldap = getLdap();
 
             Map attributes = new HashMap();
             attributes.put("objectClass",
                            new String[] {"top", ALIAS_OBJECT_CLASS});
             attributes.put("mail", mail);
             attributes.put("maildrop", destinations);
             attributes.put("accountActive", booleanToString(true));
             ldap.addElement(mailDn(mail), attributes);
         }
         catch (NamingException e)
         {
             throw new MailManagerException("Count not create alias: " + mail,
                                            e);
         }
         finally
         {
             closeLdap(ldap);
         }
     }
 
     /**
      * Modify an existing alias replacing existing data.
      *
      * @param alias New alias data
      * @throws MailManagerException If an error occured
      */
     public void modifyAlias(AliasInfo alias)
         throws MailManagerException
     {
         LdapFacade ldap = null;
         String mail = alias.getName();
         String dn = mailDn(mail);
         String domain = MailAddress.hostFromAddress(mail);
 
         try
         {
             ldap = getLdap();
             ldap.modifyElementAttribute(dn, "maildrop",
                                         alias.getDestinations());
             ldap.modifyElementAttribute(dn, "accountActive",
                                         booleanToString(alias.isActive()));
             if (isPostmaster(domain, mail))
             {
                 if (!alias.isAdministrator())
                 {
                     removePostmaster(domain, mail);
                 }
             }
             else
             {
                 if (alias.isAdministrator())
                 {
                     addPostmaster(domain, mail);
                 }
             }
         }
         catch (NamingException e)
         {
             throw new MailManagerException(alias.getName(), e);
         }
         finally
         {
             closeLdap(ldap);
         }
     }
 
     /**
      * Checks if this email address is an alias or an account.
      *
      * @param mail Email address
      * @return True if it is an alias, false if it is an account
      * @throws MailManagerException If an error occured
      */
     public boolean isAlias(String mail)
         throws MailManagerException
     {
         LdapFacade ldap = null;
         boolean isAlias = false;
         
         try
         {
             ldap = getLdap();
             searchForMail(ldap, mail);
             Set objectClasses =
                 ldap.getAllResultAttributeValues("objectClass");
            // FIX ME: Need to find a real way to do full case
            // insensitive matching.
            isAlias = (objectClasses.contains(ALIAS_OBJECT_CLASS) |
                       objectClasses.contains("jammMailAlias"));
         }
         catch (NamingException e)
         {
             throw new MailManagerException(mail, e);
         }
         finally
         {
             closeLdap(ldap);
         }
 
         return isAlias;
     }
 
     /**
      * Returns all data for the specified alias.
      *
      * @param mail Email address of an alias
      * @return The alias information bean
      * @throws MailManagerException If an error occured
      */
     public AliasInfo getAlias(String mail)
         throws MailManagerException
     {
         LdapFacade ldap = null;
         AliasInfo alias = null;
         try
         {
             ldap = getLdap();
             searchForMail(ldap, mail);
             alias = createAliasInfo(ldap);
         }
         catch (MailNotFoundException e)
         {
             alias = null;
         }
         catch (NamingException e)
         {
             throw new MailManagerException(e);
         }
         finally
         {
             closeLdap(ldap);
         }
         return alias;
     }
 
     /**
      * Returns all aliases for the specified domain as list of {@link
      * AliasInfo} objects.
      *
      * @param domain Domain name
      * @return List of {@link AliasInfo} objects
      * @throws MailManagerException If an error occured
      */
     public List getAliases(String domain)
         throws MailManagerException
     {
         LdapFacade ldap = null;
         List aliases = new ArrayList();
 
         try
         {
             ldap = getLdap();
             String domainDn = domainDn(domain);
             ldap.searchOneLevel(domainDn, "objectClass=" + ALIAS_OBJECT_CLASS);
 
             while (ldap.nextResult())
             {
                 String name = ldap.getResultAttribute("mail");
                 // Skip "special" accounts
                 if (name.startsWith("postmaster@") ||
                     name.startsWith("abuse@") ||
                     name.startsWith("@"))
                 {
                     continue;
                 }
 
                 AliasInfo alias = createAliasInfo(ldap);
                 aliases.add(alias);
             }
         }
         catch (NamingException e)
         {
             throw new MailManagerException(e);
         }
         finally
         {
             closeLdap(ldap);
         }
 
         Collections.sort(aliases, new AliasNameComparator());
         return aliases;
     }
 
     /**
      * Creates a new <code>AliasInfo</code> object from the current
      * result in the LDAP facade.  This assumes a search has been
      * previously done and the facade has been advanced to point to an
      * alias element.
      *
      * @param ldap LDAP facade where result exists
      * @return A new alias information bean
      * @throws NamingException If the object could not be created
      * @throws MailManagerException If their is a problem looking up postmaster
      */
     private AliasInfo createAliasInfo(LdapFacade ldap)
         throws NamingException, MailManagerException
     {
         String name = ldap.getResultAttribute("mail");
 
         List destinations =
             new ArrayList(ldap.getAllResultAttributeValues("maildrop"));
         Collections.sort(destinations, String.CASE_INSENSITIVE_ORDER);
         boolean isActive = stringToBoolean(
             ldap.getResultAttribute("accountActive"));
         boolean isAdmin = isPostmaster(name);
         return new AliasInfo(name, destinations, isActive, isAdmin);
     }
 
     /**
      * Delete the specified alias.
      *
      * @param mail Email address of alias to delete
      * @throws MailManagerException If the alias could not be deleted
      */
     public void deleteAlias(String mail)
         throws MailManagerException
     {
         LdapFacade ldap = null;
         try
         {
             ldap = getLdap();
             searchForMail(ldap, mail);
             ldap.deleteElement(ldap.getResultName());
         }
         catch (NamingException e)
         {
             throw new MailManagerException(mail, e);
         }
         finally
         {
             closeLdap(ldap);
         }
     }
 
     /**
      * Create a new account on an existing domain.
      *
      * @param domain Domain name
      * @param account New account name
      * @param password Password of new account
      * @throws MailManagerException If the account could not be created
      */
     public void createAccount(String domain, String account, String password)
         throws MailManagerException
     {
         LdapFacade ldap = null;
         String mail = MailAddress.addressFromParts(account, domain);
 
         try
         {
             ldap = getLdap();
             Map attributes = new HashMap();
             attributes.put("objectClass",
                            new String[] { "top", ACCOUNT_OBJECT_CLASS});
             attributes.put("homeDirectory", "/home/vmail/domains");
             attributes.put("mail", mail);
             attributes.put("mailbox", domain + "/" + account + "/");
             String hashedPassword =
                 LdapPassword.hash(PasswordScheme.SSHA_SCHEME, password);
             attributes.put("userPassword", hashedPassword);
             attributes.put("accountActive", booleanToString(true));
             ldap.addElement(mailDn(mail), attributes);
         }
         catch (NamingException e)
         {
             throw new MailManagerException("Could not create account: " + mail,
                                            e);
         }
         finally
         {
             closeLdap(ldap);
         }
     }
 
     /**
      * Modifies an account entry based on the AccountInfo passed on.
      *
      * @param account AccountInfo with current settings.
      * @exception MailManagerException if an error occurs
      */
     public void modifyAccount(AccountInfo account)
         throws MailManagerException
     {
         LdapFacade ldap = null;
 
         String mail = account.getName();
         String dn = mailDn(account.getName());
         String domain = MailAddress.hostFromAddress(mail);
         
         try
         {
             ldap = getLdap();
             ldap.modifyElementAttribute(dn, "accountActive",
                                         booleanToString(account.isActive()));
             if (isPostmaster(domain, mail))
             {
                 if (!account.isAdministrator())
                 {
                     removePostmaster(domain, mail);
                 }
             }
             else
             {
                 if (account.isAdministrator())
                 {
                     addPostmaster(domain, mail);
                 }
             }
         }
         catch (NamingException e)
         {
             throw new MailManagerException(mail, e);
         }
         finally
         {
             closeLdap(ldap);
         }
     }
     
     /**
      * Returns all accounts for the specified domain as a list of
      * {@link AccountInfo} objects.
      *
      * @param domain Domain name
      * @return List of {@link AccountInfo} objects
      * @throws MailManagerException If an error occured
      */
     public List getAccounts(String domain)
         throws MailManagerException
     {
         LdapFacade ldap = null;
         List accounts = new ArrayList();
 
         try
         {
             ldap = getLdap();
             String domainDn = domainDn(domain);
             ldap.searchOneLevel(domainDn, "objectClass=" +
                                 ACCOUNT_OBJECT_CLASS);
 
             while (ldap.nextResult())
             {
                 AccountInfo account = createAccountInfo(ldap);
                 accounts.add(account);
             }
 
         }
         catch (NamingException e)
         {
             throw new MailManagerException(e);
         }
         finally
         {
             closeLdap(ldap);
         }
 
         Collections.sort(accounts, new AccountNameComparator());
         return accounts;
     }
 
     /**
      * Returns a single AccountInfo for a given e-mail address.
      *
      * @param mail an e-mail address
      * @return an AccountInfo object or null
      * @exception MailManagerException if an error occurs
      */
     public AccountInfo getAccount(String mail)
         throws MailManagerException
     {
         LdapFacade ldap = null;
         AccountInfo account = null;
         try
         {
             ldap = getLdap();
             searchForMail(ldap, mail);
             account = createAccountInfo(ldap);
         }
         catch (MailNotFoundException e)
         {
             account = null;
         }
         catch (NamingException e)
         {
             throw new MailManagerException(e);
         }
         finally
         {
             closeLdap(ldap);
         }
         return account;
     }
 
     /**
      * Creates a new <code>AccountInfo</code> object from the current
      * result in the LDAP facade.  This assumes a search has been
      * previously done and the facade has been advanced to point to an
      * alias element.
      *
      * @param ldap LDAP facade where result exists
      * @return A new account information bean
      * @throws NamingException If the object could not be created
      * @throws MailManagerException If there is a problem looking up postmaster
      */
     private AccountInfo createAccountInfo(LdapFacade ldap)
         throws NamingException, MailManagerException
     {
         String name = ldap.getResultAttribute("mail");
         boolean isActive =
             stringToBoolean(ldap.getResultAttribute("accountActive"));
         boolean isAdmin = isPostmaster(name);
         return new AccountInfo(name, isActive, isAdmin);
     }
 
     /**
      * Adds a catch all alias for the specified domain.
      *
      * @param domain Domain name
      * @param destination Destination alias
      * @throws MailManagerException If the catch all could not be added
      */
     public void addCatchall(String domain, String destination)
         throws MailManagerException
     {
         LdapFacade ldap = null;
         String catchAll = "@" + domain;
 
         try
         {
             ldap = getLdap();
             Map attributes = new HashMap();
             attributes.put("objectClass",
                            new String[] { "top", ALIAS_OBJECT_CLASS});
             attributes.put("mail", catchAll);
             attributes.put("maildrop", destination);
             ldap.addElement(mailDn(catchAll), attributes);
         }
         catch (NamingException e)
         {
             throw new MailManagerException("Could not create catchall @" +
                                            domain, e);
         }
         finally
         {
             closeLdap(ldap);
         }
     }
 
     /**
      * Returns true if mail is a postmaster for domain.  Intended for
      * internal use only, however, could be useful so made public.
      *
      * @param domain The domain to check in.
      * @param mail The mail address to check for
      * @return true if a postmaster, false if not.
      * @exception MailManagerException if an error occurs
      */
     public boolean isPostmaster(String domain, String mail)
         throws MailManagerException
     {
         LdapFacade ldap = null;
         String pmFilter = "mail=postmaster@" + domain;
         boolean result = false;
         try
         {
             ldap = getLdap();
             ldap.searchOneLevel(domainDn(domain), pmFilter);
             if (ldap.nextResult())
             {
                 Set roleOccupants =
                     ldap.getAllResultAttributeValues("roleOccupant");
                 result = roleOccupants.contains(mailDn(mail));
             }
         }
         catch (NamingException e)
         {
             throw new MailManagerException("lookup up postmaster " + mail, e);
         }
 
         return result;
     }
     
     /**
      * Adds user given by mail postmaster powers over domain.
      *
      * @param domain The domain to grant powers in.
      * @param mail The person to give power to.
      * @throws MailManagerException When a postmaster can't be added.
      */
     public void addPostmaster(String domain, String mail)
         throws MailManagerException
     {
         LdapFacade ldap = null;
         String pmMail = "postmaster@" + domain;
         try
         {
             ldap = getLdap();
             ldap.searchOneLevel(domainDn(domain), "mail=" + pmMail);
 
             if (ldap.nextResult())
             {
                 Set roleOccupants =
                     ldap.getAllResultAttributeValues("roleOccupant");
                 roleOccupants.add(mailDn(mail));
                 ldap.modifyElementAttribute(ldap.getResultName(),
                                             "roleOccupant", roleOccupants);
             }
         }
         catch (NamingException e)
         {
             throw new MailManagerException("Adding " + mail + " as postmaster",
                                            e);
         }
     }
 
     /**
      * Removes postmaster power from mail in domain.
      *
      * @param domain The domain to look in.
      * @param mail The mail address to remove power from.
      * @exception MailManagerException if an error occurs
      */
     public void removePostmaster(String domain, String mail)
         throws MailManagerException
     {
         LdapFacade ldap = null;
         String pmMail = "postmaster@" + domain;
         try
         {
             ldap = getLdap();
             ldap.searchOneLevel(domainDn(domain), "mail=" + pmMail);
 
             if (ldap.nextResult())
             {
                 Set roleOccupants =
                     ldap.getAllResultAttributeValues("roleOccupant");
                 roleOccupants.remove(mailDn(mail));
                 ldap.modifyElementAttribute(ldap.getResultName(),
                                             "roleOccupant", roleOccupants);
             }
         }
         catch (NamingException e)
         {
             throw new MailManagerException("Removing " + mail +
                                            " as postmaster", e);
         }
     }
             
     /**
      * Constructs the DN of a domain.
      *
      * @param domain Domain name
      * @return DN for this domain
      */
     private final String domainDn(String domain)
     {
         StringBuffer domainDn = new StringBuffer();
         domainDn.append("jvd=").append(domain).append(",").append(mBase);
         return domainDn.toString();
     }
 
     /**
      * Constructs the DN of an email address.
      *
      * @param mail Email address
      * @return DN for this address
      */
     private final String mailDn(String mail)
     {
         String domain = MailAddress.hostFromAddress(mail);
         StringBuffer mailDn = new StringBuffer();
         mailDn.append("mail=").append(mail).append(",");
         mailDn.append(domainDn(domain));
         return mailDn.toString();
     }
 
     /** Name of the object class used for accounts. */
     private static final String ACCOUNT_OBJECT_CLASS = "JammMailAccount";
     /** Name of the object class used for aliases. */
     private static final String ALIAS_OBJECT_CLASS = "JammMailAlias";
 
     /** Host name of LDAP server. */
     private String mHost;
     /** Port number of LDAP server */
     private int mPort;
     /** The base of the LDAP subtree for mail data */
     private String mBase;
     /** The DN of the element to bind as */
     private String mBindDn;
     /** The password of the bind element */
     private String mBindPassword;
 }

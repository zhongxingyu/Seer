 package com.jach.jachtoolkit.ldap;
 
 import java.util.*;
 import javax.naming.*;
 import javax.naming.directory.Attributes;
 import javax.naming.directory.SearchControls;
 import javax.naming.directory.SearchResult;
 import javax.naming.ldap.*;
 
 /**
  * Referencia http://blogs.artinsoft.net/mrojas/archive/2007/05/14/1429.aspx
  * @author acruzh
  */
 public class LdapTools {
 
     static String ATTRIBUTE_FOR_USER = "sAMAccountName";
     private LdapContext ctxGC = null;
     private String userName;
     private String dn;
 
     /**
      * Create a conection with LDAP.
      * @param userName The user Name or sAMAccountName.
      * @param password The user password.
      * @param _domain The Domain Name. It's like DOMAIN.COM.MX
      * @param hostAndPort The Host Name or IP Address plus the port. The default port is 389. For example HOSTNAME:389
      * @param dn Description Name of the domain. It's like DC=DOMAIN,DC=COM,DC=MX
      */
     public LdapTools(String userName, String password, String _domain, String hostAndPort, String dn) {
        //---|| Validación de campos llenos.
        //---|| Si se pasa un password vacío, el programa se ejecuta sin marcar 
        //---|| error ya que el objeto ctxGC es inicializado (aunque erroneamente).
        if (userName.equals("") || password.equals("") || _domain.equals("") 
                || hostAndPort.equals("") || dn.equals("")) {
            return;
        }
        
         this.userName = userName;
         this.dn = dn;
         
         Hashtable environment = new Hashtable();
         environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
         //Using starndard Port, check your instalation
 
         environment.put(Context.PROVIDER_URL, "ldap://" + hostAndPort);
         environment.put(Context.SECURITY_AUTHENTICATION, "simple");
 
         environment.put(Context.SECURITY_PRINCIPAL, userName + "@" + _domain);
         environment.put(Context.SECURITY_CREDENTIALS, password);
         try {
             ctxGC = new InitialLdapContext(environment, null);
         } catch (NamingException e) {
             //TODO Manejar la excepción. Agregar un logger.
             //e.printStackTrace();
         }
     }
     
     /**
      * Verify if the credentials are valid.
      * @return True if is a valid user.
      */
     public boolean isValidUser() {
         return(ctxGC == null ? false : true);
     }
     
     /**
      * Obtain the defined Attributes by searching LDAP.
      * @param returnedAtts Array of attributes requered.
      * @return Attributes object with the required information.
      */
     public Attributes getUserAttributes(String[] returnedAtts) {
             //String returnedAtts[] = {"sn", "givenName", "mail", "displayName"};
             String searchFilter = "(&(objectClass=user)(" + ATTRIBUTE_FOR_USER + "=" + userName + "))";
 
             SearchControls searchCtls = new SearchControls();
             searchCtls.setReturningAttributes(returnedAtts);
             searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
             String searchBase = dn;
 
             try {                
                 //    Search for objects in the GC using the filter
                 NamingEnumeration answer = ctxGC.search(searchBase, searchFilter, searchCtls);
                 while (answer.hasMoreElements()) {
                     SearchResult sr = (SearchResult) answer.next();
                     Attributes attrs = sr.getAttributes();
                     if (attrs != null) {
                         return attrs;
                     }
                 }
             } catch (NamingException e) {
                 //TODO Manejar la excepción. Agregar un logger.
                 //e.printStackTrace();
             }
             return null;
     }
 
     public static void main(String[] args) {
         LdapTools ldap = new LdapTools("acruzh",
                 "H3nryC0w",
                 "BMVCORP.COM.MX",
                 "10.100.160.158:389",
                 "DC=BMVCORP,DC=COM,DC=MX");
         
         if (ldap.isValidUser()) {
             System.out.println("EXITO!!!");
             
             System.out.println("Tratando de obtener atributos.");
             String arrayAtts[] = {"sn", "givenName", "mail", "displayName"};
             Attributes att = ldap.getUserAttributes(arrayAtts);
             
             if (att == null) {
                 System.out.println("Los atributos solicitados no fueron encontrados.");
             } else {
                 String s = att.get("givenName").toString();
                 System.out.println("GIVEN NAME=" + s);
                 
                 s = att.get("sn").toString();
                 System.out.println("SECOND NAME=" + s);
                 
                 s = att.get("displayName").toString();
                 System.out.println("DISPLAY NAME=" + s);
                 
                 s = att.get("mail").toString();
                 System.out.println("EMAIL=" + s);
             }
         } else {
             System.out.println("FALOOOO");
         }        
     }
 }

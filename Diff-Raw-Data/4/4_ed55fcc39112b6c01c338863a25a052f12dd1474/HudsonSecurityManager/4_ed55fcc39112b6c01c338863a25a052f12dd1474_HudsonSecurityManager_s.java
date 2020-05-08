 /**
  * *****************************************************************************
  *
  * Copyright (c) 2012 Oracle Corporation.
  *
  * All rights reserved. This program and the accompanying materials are made
  * available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *
  *   Winston Prakash
  *
  ******************************************************************************
  */
 package org.eclipse.hudson.security;
 
 import com.thoughtworks.xstream.XStream;
 import hudson.BulkChange;
 import hudson.Util;
 import hudson.XmlFile;
 import hudson.markup.MarkupFormatter;
 import hudson.markup.RawHtmlMarkupFormatter;
 import hudson.model.Descriptor.FormException;
 import hudson.model.Saveable;
 import hudson.model.listeners.SaveableListener;
 import hudson.security.*;
 import hudson.util.TextFile;
 import hudson.util.XStream2;
 import java.io.File;
 import java.io.IOException;
 import java.security.SecureRandom;
 import javax.crypto.SecretKey;
 import javax.servlet.ServletException;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.*;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import net.sf.json.JSONObject;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.security.Authentication;
 import org.springframework.security.GrantedAuthority;
 import org.springframework.security.GrantedAuthorityImpl;
 import org.springframework.security.SpringSecurityException;
 import org.springframework.security.context.SecurityContextHolder;
 import org.springframework.security.providers.anonymous.AnonymousAuthenticationToken;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  * Manager that manages Hudson Security. The configuration is written to the
  * file hudson-security.xml
  *
  * @author Winston Prakash
  * @since 3.0.0
  */
 public class HudsonSecurityManager implements Saveable {
     
     private final String securityConfigFileName = "hudson-security.xml";
 
     /**
      * Used to load/save Security configuration.
      */
     private static final XStream XSTREAM = new XStream2();
     private transient Logger logger = LoggerFactory.getLogger(HudsonSecurityManager.class);
     /**
      * {@link Authentication} object that represents the anonymous user. Because
      * Spring Security creates its own {@link AnonymousAuthenticationToken}
      * instances, the code must not expect the singleton semantics. This is just
      * a convenient instance.
      *
      * @since 1.343
      */
     public static final Authentication ANONYMOUS = new AnonymousAuthenticationToken(
             "anonymous", "anonymous", new GrantedAuthority[]{new GrantedAuthorityImpl("anonymous")});
     /**
      * Controls a part of the <a
      * href="http://en.wikipedia.org/wiki/Authentication">authentication</a>
      * handling in Hudson. <p> Intuitively, this corresponds to the user
      * database.
      *
      * See {@link HudsonFilter} for the concrete authentication protocol.
      *
      * Never null. Always use {@link #setSecurityRealm(SecurityRealm)} to update
      * this field.
      *
      * @see #getSecurity()
      * @see #setSecurityRealm(SecurityRealm)
      */
     private volatile SecurityRealm securityRealm = SecurityRealm.NO_AUTHENTICATION;
     /**
      * Controls how the <a
      * href="http://en.wikipedia.org/wiki/Authorization">authorization</a> is
      * handled in Hudson. <p> This ultimately controls who has access to what.
      *
      * Never null.
      */
     private volatile AuthorizationStrategy authorizationStrategy = AuthorizationStrategy.UNSECURED;
     /**
      * False to enable anyone to do anything. Left as a field so that we can
      * still read old data that uses this flag.
      *
      * @see #authorizationStrategy
      * @see #securityRealm
      */
     private Boolean useSecurity;
     private MarkupFormatter markupFormatter;
     private File hudsonHome;
 
     static {
         XSTREAM.alias("hudsonSecurityManager", HudsonSecurityManager.class);
     }
     /**
      * Secrete key generated once and used for a long time, beyond container
      * start/stop. Persisted outside <tt>config.xml</tt> to avoid accidental
      * exposure.
      */
     private transient final String secretKey;
 
     public HudsonSecurityManager(File hudsonHome) throws IOException {
         this.hudsonHome = hudsonHome;
         // get or create the secret
         TextFile secretFile = new TextFile(new File(hudsonHome, "secret.key"));
 
         if (secretFile.exists()) {
             secretKey = secretFile.readTrim();
         } else {
             SecureRandom sr = new SecureRandom();
             byte[] random = new byte[32];
             sr.nextBytes(random);
             secretKey = Util.toHexString(random);
             secretFile.write(secretKey);
         }
 
         load();
     }
     
     /**
      * Get the directory where hudson stores the User configuration
      * @return 
      */
     public File getHudsonHome() {
         return hudsonHome;
     }
 
 
     /**
      * Gets the markup formatter used in the system.
      *
      * @return never null.
      */
     public MarkupFormatter getMarkupFormatter() {
         return markupFormatter != null ? markupFormatter : RawHtmlMarkupFormatter.INSTANCE;
     }
 
     /**
      * Sets the markup formatter used in the system globally.
      */
     public void setMarkupFormatter(MarkupFormatter markupFormatter) {
         this.markupFormatter = markupFormatter;
     }
 
     /**
      * Returns the {@link ACL} for this object.
      */
     public ACL getACL() {
         return authorizationStrategy.getRootACL();
     }
 
     /**
      * Short for {@code getACL().checkPermission(p)}
      */
     public void checkPermission(Permission p) {
         getACL().checkPermission(p);
     }
 
     /**
      * Short for {@code getACL().hasPermission(p)}
      */
     public boolean hasPermission(Permission p) {
         return getACL().hasPermission(p);
     }
     
     /**
      * Returns a secret key that survives across container start/stop.
      * <p>
      * This value is useful for implementing some of the security features.
      */
     public String getSecretKey() {
         return secretKey;
     }
 
     /**
      * Gets {@linkplain #getSecretKey() the secret key} as a key for AES-128.
      * @since 1.308
      */
     public SecretKey getSecretKeyAsAES128() {
         return Util.toAes128Key(secretKey);
     }
 
     /**
      * A convenience method to check if there's some security restrictions in
      * place.
      */
     public boolean isUseSecurity() {
         return securityRealm != SecurityRealm.NO_AUTHENTICATION || authorizationStrategy != AuthorizationStrategy.UNSECURED;
     }
 
     /**
      * Returns the constant that captures the three basic security modes in
      * Hudson.
      */
     public SecurityMode getSecurity() {
         // fix the variable so that this code works under concurrent modification to securityRealm.
         SecurityRealm realm = securityRealm;
 
         if (realm == SecurityRealm.NO_AUTHENTICATION) {
             return SecurityMode.UNSECURED;
         }
         if (realm instanceof LegacySecurityRealm) {
             return SecurityMode.LEGACY;
         }
         return SecurityMode.SECURED;
     }
 
     /**
      * Get the configured Security Realm
      *
      * @return never null.
      */
     public SecurityRealm getSecurityRealm() {
         return securityRealm;
     }
 
     /**
      * Set a Security Realm to the Manager
      *
      * @param securityRealm
      */
     public void setSecurityRealm(SecurityRealm securityRealm) {
         if (securityRealm == null) {
             securityRealm = SecurityRealm.NO_AUTHENTICATION;
         }
         this.securityRealm = securityRealm;
         // reset the filters and proxies for the new SecurityRealm
         try {
             HudsonFilter filter = HudsonSecurityEntitiesHolder.getHudsonSecurityFilter();
             if (filter == null) {
                 // Fix for #3069: This filter is not necessarily initialized before the servlets.
                 // when HudsonFilter does come back, it'll initialize itself.
                 logger.debug("HudsonFilter has not yet been initialized: Can't perform security setup for now");
             } else {
                 logger.debug("HudsonFilter has been previously initialized: Setting security up");
                 filter.reset(securityRealm);
                 logger.debug("Security is now fully set up");
             }
         } catch (ServletException e) {
             // for binary compatibility, this method cannot throw a checked exception
             throw new SpringSecurityException("Failed to configure filter", e) {
             };
         }
     }
 
     /**
      * Get the configured Authorization Strategy
      *
      * @return never null.
      */
     public AuthorizationStrategy getAuthorizationStrategy() {
         return authorizationStrategy;
     }
 
     /**
      * Set the Authorization Strategy to the Manager
      *
      * @param a
      */
     public void setAuthorizationStrategy(AuthorizationStrategy authStrategy) {
         if (authStrategy == null) {
             authStrategy = AuthorizationStrategy.UNSECURED;
         }
         authorizationStrategy = authStrategy;
     }
 
     /**
      * Accepts submission from the configuration page.
      */
     public synchronized void doConfigSubmit(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, FormException {
         BulkChange bc = new BulkChange(this);
         try {
             checkPermission(Permission.HUDSON_ADMINISTER);
 
             JSONObject json = req.getSubmittedForm();
 
             // keep using 'useSecurity' field as the main configuration setting
             // until we get the new security implementation working
             // useSecurity = null;
             if (json.has("use_security")) {
                 useSecurity = true;
                 JSONObject security = json.getJSONObject("use_security");
                 setSecurityRealm(SecurityRealm.all().newInstanceFromRadioList(security, "realm"));
                 setAuthorizationStrategy(AuthorizationStrategy.all().newInstanceFromRadioList(security, "authorization"));
 
                 if (security.has("markupFormatter")) {
                     markupFormatter = req.bindJSON(MarkupFormatter.class, security.getJSONObject("markupFormatter"));
                 } else {
                     markupFormatter = null;
                 }
             } else {
                 useSecurity = null;
                 setSecurityRealm(SecurityRealm.NO_AUTHENTICATION);
                 authorizationStrategy = AuthorizationStrategy.UNSECURED;
                 markupFormatter = null;
             }
 
             rsp.sendRedirect(req.getContextPath() + '/');  // go to the top page
 
         } finally {
             bc.commit();
         }
     }
 
     /**
      * Perform the logout action for the current user.
      *
      * @param req
      * @param rsp
      * @throws IOException
      * @throws ServletException
      */
     public void doLogout(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
         securityRealm.doLogout(req, rsp);
     }
 
     /**
      * The file where the Security settings are saved.
      */
     protected final XmlFile getConfigFile() {
         return new XmlFile(XSTREAM, new File(hudsonHome, securityConfigFileName));
     }
 
     /**
      * Save the settings to the configuration file.
      */
     public synchronized void save() throws IOException {
         if (BulkChange.contains(this)) {
             return;
         }
         getConfigFile().write(this);
         SaveableListener.fireOnChange(this, getConfigFile());
     }
 
     /**
      * Load the settings from the configuration file
      */
     private void load() {
 
         XmlFile config = getConfigFile();
         try {
             if (config.exists()) {
                 config.unmarshal(this);
             }else{
                 // Compatibility. Hudson 2.x stores Security config in the Global Config file.
                 if (extractSecurityConfig()){
                     config.unmarshal(this);
                 }
             }
         } catch (IOException e) {
             logger.error("Failed to load " + config, e);
         }
 
         // read in old data that doesn't have the security field set
         if (authorizationStrategy == null) {
             if (useSecurity == null || !useSecurity) {
                 authorizationStrategy = AuthorizationStrategy.UNSECURED;
             } else {
                 authorizationStrategy = new FullControlOnceLoggedInAuthorizationStrategy();
             }
         }
         if (securityRealm == null) {
             if (useSecurity == null || !useSecurity) {
                 setSecurityRealm(SecurityRealm.NO_AUTHENTICATION);
             } else {
                 setSecurityRealm(new LegacySecurityRealm());
             }
         } else {
             // force the set to proxy
             setSecurityRealm(securityRealm);
         }
 
         if (useSecurity != null && !useSecurity) {
             // forced reset to the unsecure mode.
             // this works as an escape hatch for people who locked themselves out.
             authorizationStrategy = AuthorizationStrategy.UNSECURED;
             setSecurityRealm(SecurityRealm.NO_AUTHENTICATION);
         }
     }
 
     /**
      * Convenient static method to provide full control
      */
     public static void grantFullControl() {
         SecurityContextHolder.getContext().setAuthentication(ACL.SYSTEM);
     }
 
     public static void resetFullControl() {
         SecurityContextHolder.clearContext();
     }
 
     /**
      * Gets the {@link Authentication} object that represents the user
      * associated with the current request.
      */
     public static Authentication getAuthentication() {
         Authentication a = SecurityContextHolder.getContext().getAuthentication();
         // on Tomcat while serving the login page, this is null despite the fact
         // that we have filters. Looking at the stack trace, Tomcat doesn't seem to
         // run the request through filters when this is the login request.
         // see http://www.nabble.com/Matrix-authorization-problem-tp14602081p14886312.html
         if (a == null) {
             a = ANONYMOUS;
         }
         return a;
     }
     
     private boolean extractSecurityConfig() {
         try {
 
             File globalConfigFile = new File(hudsonHome, "config.xml");
 
             Document globalConfigDoc = parseXmlFile(globalConfigFile);
 
             if (isSecuritySet(globalConfigDoc)) {
                 DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                 Document securityConfigDoc = builder.newDocument();
 
                 Element root = securityConfigDoc.createElement("hudsonSecurityManager");
                 securityConfigDoc.appendChild(root);
                 moveElement(globalConfigDoc, securityConfigDoc, root, "useSecurity");
                 moveElement(globalConfigDoc, securityConfigDoc, root, "authorizationStrategy");
                 moveElement(globalConfigDoc, securityConfigDoc, root, "securityRealm");
 
                 File securityConfigFile = new File(hudsonHome, securityConfigFileName);
                 securityConfigFile.createNewFile();
                 writeXmlFile(securityConfigDoc, securityConfigFile);
 
                 writeXmlFile(globalConfigDoc, globalConfigFile);
                 return true;
             } else {
                 return false;
             }
 
         } catch (Exception exc) {
             exc.printStackTrace();
             return false;
         }
     }
 
     private void moveElement(Document fromDoc, Document toDoc, Element root, String elementName) {
         NodeList list = fromDoc.getElementsByTagName(elementName);
         if ((list != null) && (list.getLength() > 0)) {
             Element element = (Element) list.item(0);
             Node node = toDoc.importNode(element, true);
             root.appendChild(node);
             element.getParentNode().removeChild(element);
         }
     }
 
     private Document parseXmlFile(File xmlFile) throws ParserConfigurationException, SAXException, IOException {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
 
         Document doc = factory.newDocumentBuilder().parse(xmlFile);
         return doc;
     }
 
     private void writeXmlFile(Document doc, File file) throws TransformerConfigurationException, TransformerException {
         Source source = new DOMSource(doc);
         Result result = new StreamResult(file);
 
         Transformer transformer = TransformerFactory.newInstance().newTransformer();
         transformer.setOutputProperty(OutputKeys.INDENT, "yes");
         transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
         transformer.transform(source, result);
     }
 
     private boolean isSecuritySet(Document globalConfigDoc) {
         NodeList list = globalConfigDoc.getElementsByTagName("useSecurity");
         return (list != null) && (list.getLength() > 0);
     }
 }

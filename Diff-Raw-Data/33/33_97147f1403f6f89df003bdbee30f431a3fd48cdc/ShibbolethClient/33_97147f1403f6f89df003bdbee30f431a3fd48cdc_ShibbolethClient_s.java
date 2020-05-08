 /*
 * $Id: ShibbolethClient.java,v 1.6 2007/10/01 11:00:13 vtschopp Exp $
  * 
  * Created on Jul 5, 2006 by tschopp
  *
  * Copyright (c) Members of the EGEE Collaboration. 2004.
  * See http://eu-egee.org/partners/ for details on the copyright holders.
  * For license conditions see the license file or http://eu-egee.org/license.html
  */
 package org.glite.slcs.shibclient;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.httpclient.Cookie;
 import org.apache.commons.httpclient.Header;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.URI;
 import org.apache.commons.httpclient.URIException;
 import org.apache.commons.httpclient.auth.AuthScheme;
 import org.apache.commons.httpclient.auth.AuthScope;
 import org.apache.commons.httpclient.auth.AuthState;
 import org.apache.commons.httpclient.cookie.CookieSpecBase;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.glite.slcs.AuthException;
 import org.glite.slcs.RemoteException;
 import org.glite.slcs.SLCSConfigurationException;
 import org.glite.slcs.ServiceException;
 import org.glite.slcs.UnknownResourceException;
 import org.glite.slcs.shibclient.metadata.IdentityProvider;
 import org.glite.slcs.shibclient.metadata.ShibbolethClientMetadata;
 
 import au.id.jericho.lib.html.Element;
 import au.id.jericho.lib.html.FormControl;
 import au.id.jericho.lib.html.FormControlType;
 import au.id.jericho.lib.html.Source;
 import au.id.jericho.lib.html.Tag;
 
 /**
  * ShibbolethClient is a SSO login parser and a Shibboleth Browser/POST profile
  * handler.
  * 
  * @author Valery Tschopp <tschopp@switch.ch>
 * @version $Revision: 1.6 $
  */
 public class ShibbolethClient {
 
     /** Logging */
     private static final Log LOG = LogFactory.getLog(ShibbolethClient.class);
 
     /** The HttpClient delegate */
     private HttpClient httpClient_;
 
     /** Shibboleth client metadata */
     private ShibbolethClientMetadata metadata_;
 
     /** Shibboleth client credentials */
     private ShibbolethCredentials credentials_;
 
     /** authenticated or not */
     private boolean isAuthenticated_ = false;
 
     /**
      * Creates a Shibboleth client wrapping a {@link HttpClient}. Requires the
      * Shibboleth client metadata and the Shibboleth credentials.
      * 
      * @param client
      *            The HttpClient engine.
      * @param metadata
      *            The Shibboleth client metadata
      * @param credentials
      *            The Shibboleth credentials (username and password)
      * @throws SLCSConfigurationException
      */
     public ShibbolethClient(HttpClient client,
             ShibbolethClientMetadata metadata, ShibbolethCredentials credentials)
             throws SLCSConfigurationException {
         // check consistancy of creds and metadata
         String id = credentials.getIdentityProviderID();
         IdentityProvider idp = metadata.getIdentityProvider(id);
         if (idp == null) {
             String filename = metadata.getFileConfiguration().getFile().getAbsolutePath();
             LOG.error("Unknown IdP " + id);
             throw new SLCSConfigurationException("IdP id=" + id
                     + " is not defined in metadata file: " + filename);
         }
 
         this.httpClient_ = client;
         this.metadata_ = metadata;
         this.credentials_ = credentials;
 
     }
 
     /**
      * Authenticates the user with his IdP for the default SLCS SP.
      * 
      * @return <code>true</code> iff the user have been authenticated by his
      *         Identity Provider.
      * @throws ServiceException
      * @throws RemoteException
      * @throws AuthException
      * @throws UnknownResourceException
      */
     public boolean authenticate() throws AuthException, RemoteException,
             ServiceException, UnknownResourceException {
         String slcsEntryURL = metadata_.getSLCS().getUrl();
         return authenticate(slcsEntryURL);
     }
 
     /**
      * Authenticates the user on his IdP for the given SP.
      * 
      * @param spEntryURL
      *            The Service Provider entry point URL
      * @return <code>true</code> iff the user have been authenticated by his
      *         Identity Provider.
      * @throws AuthException
      * @throws RemoteException
      * @throws ServiceException
      * @throws UnknownResourceException
      */
     public boolean authenticate(String spEntryURL) throws AuthException,
             RemoteException, ServiceException, UnknownResourceException {
 
         String idpProviderID = credentials_.getIdentityProviderID();
         IdentityProvider idp = metadata_.getIdentityProvider(idpProviderID);
         if (idp == null) {
             throw new UnknownResourceException("IdP " + idpProviderID
                     + " not found in Metadata");
         }
         LOG.info(spEntryURL + " IdP=" + idp.getUrl() + " AuthType="
                 + idp.getAuthTypeName());
         String idpURL = idp.getUrl();
         LOG.debug("idpURL=" + idpURL);
         // LOG.debug("wayfURL=" + wayfURL);
         try {
             URI idpSSOResponseURI = null;
 
             // 1. get the first redirection or already authenticated
             URI spLoginResponseURI = processSPEntry(spEntryURL);
 
             // either wayf or idp or same (already authenticated)
             LOG.debug("spLoginResponseURI=" + spLoginResponseURI);
             String spLoginResponseURL = spLoginResponseURI.getEscapedURI();
             // check if already authenticated
             if (spLoginResponseURL.startsWith(spEntryURL)) {
                 LOG.info("Already authenticated? " + isAuthenticated_ + ": "
                         + spLoginResponseURL);
                 return this.isAuthenticated_;
             }
             // checks if URL contains the shire= parameter (WAYF of IdP)
             if (spLoginResponseURL.indexOf("shire=") == -1) {
                 LOG.error("Unexpected spLoginResponseURL=" + spLoginResponseURL);
                 throw new ServiceException(
                         "SP response URL doesn't contain the 'shire=' parameter: "
                                 + spLoginResponseURL);
             }
             else {
                 LOG.debug("IdP SSO: " + idpURL);
                 // 2. process the IdP SSO login
                 idpSSOResponseURI = processIdPSSO(idp, spLoginResponseURI);
             }
 
             // 3. process the Browser/POST profile
             URI browserPostResponseURI = processIdPBrowserPOST(idp,
                     idpSSOResponseURI);
             String url = browserPostResponseURI.getURI();
             if (url.equals(spEntryURL)) {
                 this.isAuthenticated_ = true;
                 LOG.info("Sucessful authentication");
             }
 
         } catch (URIException e) {
             LOG.error("URIException: " + e);
             e.printStackTrace();
         } catch (HttpException e) {
             LOG.error("HttpException: " + e);
             e.printStackTrace();
         } catch (IOException e) {
             LOG.error("IOException: " + e);
             e.printStackTrace();
             throw new RemoteException(e);
         }
         return this.isAuthenticated_;
     }
 
     /**
      * @param idp
      * @param idpSSOResponseURI
      * @return
      * @throws RemoteException
      */
     private URI processIdPBrowserPOST(IdentityProvider idp,
             URI idpSSOResponseURI) throws RemoteException {
         // return value
         URI browserPostResponseURI = null;
         RemoteException remoteException = null;
 
         try {
             // get the SAMLResponse
             String idpSSOResponseURL = idpSSOResponseURI.getURI();
             GetMethod GETIdPBrowserPOSTMethod = new GetMethod(idpSSOResponseURL);
 
             // set Pubcookie cookie request header: Cookie: <pubcookie_pre_s>;
             // <pubcookie_g>
             if (idp.getAuthType() == IdentityProvider.SSO_AUTHTYPE_PUBCOOKIE) {
                 Cookie pubcookie_pre_s = getCookie("pubcookie_pre_s");
                 Cookie pubcookie_g = getCookie("pubcookie_g");
                 String pubcookies = pubcookie_pre_s.toString() + "; "
                         + pubcookie_g.toString();
                 LOG.debug("setting PubCookie request Cookie: " + pubcookies);
                 GETIdPBrowserPOSTMethod.addRequestHeader("Cookie", pubcookies);
             }
 
             LOG.info("GETIdPBrowserPOSTMethod: " + idpSSOResponseURL);
             int idpBrowserPOSTResponseStatus = executeMethod(GETIdPBrowserPOSTMethod);
             LOG.debug(GETIdPBrowserPOSTMethod.getStatusLine());
             // TODO: check status
 
             // handle Broswer/POST enclosed in FROM
             InputStream samlResponse = GETIdPBrowserPOSTMethod.getResponseBodyAsStream();
             Source source = new Source(samlResponse);
             List forms = source.findAllElements(Tag.FORM);
             if (!forms.isEmpty()) {
                 // check if form contains a valid SAML Browser/POST
                 for (Iterator i = forms.iterator(); i.hasNext();) {
                     Element form = (Element) i.next();
                     String spSAMLURL = form.getAttributeValue("ACTION");
 
                     LOG.debug("SAML Browser/POST URL=" + spSAMLURL);
                     if (spSAMLURL == null) {
                         // no SAML post URL found
                         LOG.error("No SAML Browser/POST URL found: "
                                 + GETIdPBrowserPOSTMethod.getURI());
                         remoteException = new RemoteException(
                                 "No SAML Browser/POST URL found: "
                                         + GETIdPBrowserPOSTMethod.getURI());
                         break; // exit loop
                     }
 
                     // create POST method
                     PostMethod POSTSPSAMLMethod = new PostMethod(spSAMLURL);
                     // add all HIDDEN fields to POST
                     List formControls = form.findFormControls();
                     Iterator controlsIterator = formControls.iterator();
                     while (controlsIterator.hasNext()) {
                         FormControl control = (FormControl) controlsIterator.next();
                         FormControlType type = control.getFormControlType();
                         if (type.equals(FormControlType.HIDDEN)) {
                             String name = control.getName();
                             Collection values = control.getValues();
                             Iterator valuesIterator = values.iterator();
                             while (valuesIterator.hasNext()) {
                                 String value = (String) valuesIterator.next();
                                 LOG.debug("HIDDEN " + name + "=" + value);
                                 // add all hidden fields
                                 POSTSPSAMLMethod.addParameter(name, value);
                             }
                         }
                     }
 
                     // execute the SAML post
                     LOG.info("POSTSPSAMLMethod: " + POSTSPSAMLMethod.getURI());
                     int spSAMLResponseStatus = executeMethod(POSTSPSAMLMethod);
                     LOG.debug(POSTSPSAMLMethod.getStatusLine());
 
                     // status must be 302 and redirect Location
                     Header location = POSTSPSAMLMethod.getResponseHeader("Location");
                     if (spSAMLResponseStatus == 302 && location != null) {
                         String url = location.getValue();
                         browserPostResponseURI = new URI(url, false);
                         LOG.debug("Redirect: " + browserPostResponseURI);
 
                     }
                     else {
                         LOG.error("Unexpected SP response: Status="
                                 + spSAMLResponseStatus + " Location="
                                 + location);
                         remoteException = new RemoteException(
                                 "Unexpected SP response: Status="
                                         + spSAMLResponseStatus + " Location="
                                         + location);
                     }
 
                     LOG.trace("POSTSPSAMLMethod.releaseConnection()");
                     POSTSPSAMLMethod.releaseConnection();
 
                 } // forms loop
             }
             else {
                 // no SAML post found
                 LOG.error("No SAML Browser/POST profile found: "
                         + GETIdPBrowserPOSTMethod.getURI());
                 remoteException = new RemoteException(
                         "No SAML Browser/POST profile found: "
                                 + GETIdPBrowserPOSTMethod.getURI());
             }
 
             LOG.trace("GETIdPBrowserPOSTMethod.releaseConnection()");
             GETIdPBrowserPOSTMethod.releaseConnection();
 
         } catch (URIException e) {
             e.printStackTrace();
             remoteException = new RemoteException(e.getMessage(), e);
         } catch (IOException e) {
             e.printStackTrace();
             remoteException = new RemoteException(e.getMessage(), e);
         }
 
         if (browserPostResponseURI == null) {
             if (remoteException != null) {
                 throw remoteException;
             }
         }
 
         return browserPostResponseURI;
     }
 
     /**
      * Gets the Shibboleth session needed afterward.
      * 
      * @param entryURL
      * @return
      * @throws URIException
      * @throws HttpException
      * @throws IOException
      * @throws RemoteException
      */
     private URI processSPEntry(String entryURL) throws URIException,
             HttpException, IOException, RemoteException {
         GetMethod GETSPEntryMethod = new GetMethod(entryURL);
         LOG.info("GETSPEntryMethod: " + GETSPEntryMethod.getURI());
         int spEntryStatus = executeMethod(GETSPEntryMethod);
         LOG.debug(GETSPEntryMethod.getStatusLine());
 
         URI loginResponseURI = GETSPEntryMethod.getURI();
 
         LOG.trace("GETSPEntryMethod.releaseConnection()");
         GETSPEntryMethod.releaseConnection();
 
         // check response status
         if (spEntryStatus != 200) {
             throw new RemoteException("Unexpected SP response status:"
                     + spEntryStatus + " " + entryURL);
         }
         return loginResponseURI;
     }
 
     /**
      * @param idp
      * @param query
      * @throws URIException
      * @throws HttpException
      * @throws IOException
      * @throws AuthException
      * @throws RemoteException
      * @throws ServiceException
      */
     private URI processIdPSSO(IdentityProvider idp, URI spResponseURI)
             throws URIException, HttpException, IOException, AuthException,
             RemoteException, ServiceException {
         String idpSSOURL = idp.getUrl();
         String query = spResponseURI.getQuery();
         if (query != null) {
             idpSSOURL += "?" + query;
         }
         // create HttpMethod
         GetMethod GETIdpSSOMethod = new GetMethod(idpSSOURL);
 
         // set credential for basic or ntlm
         int authType = idp.getAuthType();
         LOG.debug("IdP authType=" + idp.getAuthTypeName());
         if (authType == IdentityProvider.SSO_AUTHTYPE_BASIC
                 || authType == IdentityProvider.SSO_AUTHTYPE_NTLM) {
             // enable BASIC or NTLM authN
             LOG.info("Enable " + idp.getAuthTypeName() + " authentication: "
                     + credentials_);
             URI idpSSOURI = GETIdpSSOMethod.getURI();
             AuthScope scope = new AuthScope(idpSSOURI.getHost(), 443,
                     idp.getAuthRealm());
             LOG.debug("AuthScope=" + scope);
             httpClient_.getState().setCredentials(scope, credentials_);
             GETIdpSSOMethod.setDoAuthentication(true);
         }
 
         // execute the method
         LOG.info("GETIdpSSOMethod: " + GETIdpSSOMethod.getURI());
         int idpSSOResponseStatus = executeMethod(GETIdpSSOMethod);
         LOG.debug(GETIdpSSOMethod.getStatusLine());
 
         URI idpSSOResponseURI = GETIdpSSOMethod.getURI();
         String idpSSOResponseQuery = idpSSOResponseURI.getEscapedQuery();
         LOG.debug("idpSSOResponseURI=" + idpSSOResponseURI);
         LOG.debug("idpSSOResponseQuery=" + idpSSOResponseQuery);
 
         // XXX
         dumpHttpClientCookies();
 
         LOG.debug("idpSSOResponseStatus=" + idpSSOResponseStatus);
         // if BASIC or NTLM failed
         if (idpSSOResponseStatus == 401) {
             LOG.error("BASIC or NTLM authentication was required for "
                     + idpSSOURL);
 
             LOG.trace("GETIdpSSOMethod.releaseConnection()");
             GETIdpSSOMethod.releaseConnection();
 
             if (LOG.isDebugEnabled()) {
                 AuthState authState = GETIdpSSOMethod.getHostAuthState();
                 String realm = authState.getRealm();
                 AuthScheme scheme = authState.getAuthScheme();
                 String schemeName = scheme.getSchemeName();
                 LOG.debug("Auth realm=" + realm + " scheme=" + schemeName);
             }
             throw new AuthException(idp.getAuthTypeName()
                     + " Authentication failed: " + this.credentials_
                     + " Status: " + idpSSOResponseStatus);
         }
         // CAS sends 200 + Cookies and the login form directly
         else if (idpSSOResponseStatus == 200
                 && idp.getAuthType() == IdentityProvider.SSO_AUTHTYPE_CAS) {
             LOG.debug("Process CAS login form...");
             // process CAS login form
             InputStream idpLoginForm = GETIdpSSOMethod.getResponseBodyAsStream();
             idpSSOResponseURI = processIdPLoginForm(idp, idpSSOResponseURI,
                     idpLoginForm);
             LOG.debug("CAS idpSSOResponseURI=" + idpSSOResponseURI);
         }
         // PUBCOOKIE sends 200 + Cookies and a HTML page #!@! 
         // <meta http-equiv="Refresh"
         // content="0;URL=https://aai1.unil.ch/"> back
         else if (idpSSOResponseStatus == 200
                 && idp.getAuthType() == IdentityProvider.SSO_AUTHTYPE_PUBCOOKIE) {
 
             LOG.trace("GETIdpSSOMethod.releaseConnection()");
             GETIdpSSOMethod.releaseConnection();
 
             LOG.debug("Get Pubcookie login form...");
 
             GetMethod GETPubcookieLoginFormMethod = new GetMethod(
                     idp.getAuthUrl());
             // set Cookie 'pubcookie_g_req'
             Cookie pubcookie_r_req = getCookie("pubcookie_g_req");
             GETPubcookieLoginFormMethod.setRequestHeader("Cookie",
                     pubcookie_r_req.toString());
             LOG.info("GETPubcookieLoginFormMethod: "
                     + GETPubcookieLoginFormMethod.getURI());
             int pubcookieLoginFormStatus = executeMethod(GETPubcookieLoginFormMethod);
             LOG.debug(GETPubcookieLoginFormMethod.getStatusLine());
             // TODO check status
 
             // XXX
             dumpHttpClientCookies();
 
             // process pubcookie login form
             InputStream loginFormStream = GETPubcookieLoginFormMethod.getResponseBodyAsStream();
             idpSSOResponseURI = processIdPLoginForm(idp, idpSSOResponseURI,
                     loginFormStream);
             LOG.debug("Pubcookie idpSSOResponseURI=" + idpSSOResponseURI);
 
             LOG.debug("GETPubcookieLoginFormMethod.releaseConnection()");
             GETPubcookieLoginFormMethod.releaseConnection();
 
         }
 
         else {
             // TODO: error handling
             LOG.error("Unexpected Status: " + idpSSOResponseStatus
                     + " AuthType: " + idp.getAuthTypeName());
         }
 
         LOG.debug("GETIdpSSOMethod.releaseConnection()");
         GETIdpSSOMethod.releaseConnection();
 
         return idpSSOResponseURI;
 
     }
 
     /**
      * Prases and process Pubcookie or CAS login form.
      * 
      * @param idp
      * @param htmlForm
      * @throws IOException
      * @throws RemoteException
      * @throws ServiceException
      * @throws AuthException
      */
     private URI processIdPLoginForm(IdentityProvider idp, URI ssoLoginURI,
             InputStream htmlForm) throws IOException, RemoteException,
             ServiceException, AuthException {
         LOG.info("Parse and process " + idp.getAuthTypeName()
                 + " login form...");
 
         boolean formFound = false;
         URI idpLoginFormResponseURI = null;
 
         // Parse the FORM with Jericho HTML Parser
         Source source = new Source(htmlForm);
         List forms = source.findAllElements(Tag.FORM);
         for (Iterator i = forms.iterator(); i.hasNext();) {
             Element form = (Element) i.next();
             String formName = form.getAttributeValue("NAME");
 
             // BUG FIX: UniL use a CAS login form with NO NAME defined.
             // first try with the form ID as NAME, otherwise use an empty name.
             // the metadata should also define an empty name for this particular
             // form.
             LOG.debug("FORM NAME: " + formName);
             if (formName == null) {
                 LOG.warn("form have no NAME, try form ID...");
                 String formId = form.getAttributeValue("ID");
                 if (formId == null) {
                     LOG.warn("form have no NAME and no ID, using empty name...");
                     formName = "";
                 }
                 else {
                     formName = formId;
                 }
             }
 
             if (formName.equals(idp.getAuthFormName())) {
                 formFound = true;
                 String formLocation = form.getAttributeValue("ACTION");
                 if (formLocation == null || formLocation.equals("")) {
                     // no form location to POST
                     formLocation = ssoLoginURI.getEscapedURI();
                     LOG.info("form ACTION URL=" + formLocation);
                 }
                 else {
                     // BUG FIX: if CAS3 doesn't use cookie, then it adds a path
                     // component ;jsessionid=... between the URL and the query
                     // parameters (?) i.e.:
                     // action="login;jsessionid=52E28683EF109486FAB652E3D76EEDDE?param1..."
                     int jsessionIdPos = formLocation.indexOf(";jsessionid=");
                     int questionMarkPos = formLocation.indexOf('?');
                     if (jsessionIdPos != -1 && jsessionIdPos < questionMarkPos) {
                         LOG.warn("form ACTION URL contains ';jsessionid=...': "
                                 + formLocation);
                         // extract the ;jsessionid= from the form action url
                         String jsessionId = formLocation.substring(
                                 jsessionIdPos, questionMarkPos);
                         LOG.debug("jsessionId=" + jsessionId);
                         // and add it to the existing path
                         String path = ssoLoginURI.getPath();
                         ssoLoginURI.setPath(path + jsessionId);
                         formLocation = ssoLoginURI.getEscapedURI();
                         LOG.info("corrected form ACTION URL=" + formLocation);
                     }
                 }
 
                 String formMethod = form.getAttributeValue("METHOD");
                 LOG.debug("FORM name=" + formName + " location=" + formLocation
                         + " method=" + formMethod);
 
                 if (!formLocation.equals("")
                         && formMethod.equalsIgnoreCase("POST")) {
 
                     PostMethod POSTLoginFormMethod = new PostMethod(
                             formLocation);
 
                     // add all HIDDEN fields to POST
                     List formControls = form.findFormControls();
                     Iterator controlsIterator = formControls.iterator();
                     while (controlsIterator.hasNext()) {
                         FormControl control = (FormControl) controlsIterator.next();
                         FormControlType type = control.getFormControlType();
                         if (type.equals(FormControlType.HIDDEN)) {
                             String name = control.getName();
                             Collection values = control.getValues();
                             Iterator valuesIterator = values.iterator();
                             while (valuesIterator.hasNext()) {
                                 String value = (String) valuesIterator.next();
                                 LOG.debug("HIDDEN " + name + "=" + value);
                                 // add all hidden fields
                                 POSTLoginFormMethod.addParameter(name, value);
                             }
                         }
                     }
                     // add username field
                     POSTLoginFormMethod.addParameter(idp.getAuthFormUsername(),
                             this.credentials_.getUserName());
                     // add the PASSWORD field
                     POSTLoginFormMethod.addParameter(idp.getAuthFormPassword(),
                             this.credentials_.getPassword());
 
                     // execute the login POST
                     LOG.info("POSTLoginFormMethod: "
                             + POSTLoginFormMethod.getURI());
                     // XXX
                     dumpHttpClientCookies();
                     if (LOG.isTraceEnabled()) {
                         LOG.trace("Host:"
                                 + POSTLoginFormMethod.getURI().getHost());
                         LOG.trace("Path:"
                                 + POSTLoginFormMethod.getURI().getPath());
                        LOG.trace("Port:"
                                + POSTLoginFormMethod.getURI().getPort());
                     }
                     // BUG FIX: set the CAS JSESSIONID cookie by hand.
                     // default CookiePolicy is RFC2109 and doesn't handle
                     // correctly FQDN domain.
                     String host = POSTLoginFormMethod.getURI().getHost();
                     String path = POSTLoginFormMethod.getURI().getPath();
                     Cookie cookies[] = getMatchingCookies("JSESSIONID", host,
                             path);
                     for (int j = 0; j < cookies.length; j++) {
                         if (LOG.isDebugEnabled()) {
                             LOG.debug("setting CAS request Cookie: "
                                     + cookies[j]);
                         }
                         POSTLoginFormMethod.setRequestHeader("Cookie",
                                 cookies[j].toString());
                     }
 
                     int formLoginResponseStatus = executeMethod(POSTLoginFormMethod);
                     LOG.debug(POSTLoginFormMethod.getStatusLine());
 
                     // XXX
                     // dumpHttpClientCookies();
 
                     // CAS send a 302 + Location header back
                     if (formLoginResponseStatus == 302
                             && idp.getAuthType() == IdentityProvider.SSO_AUTHTYPE_CAS) {
                         LOG.debug("Process CAS response...");
                         Header location = POSTLoginFormMethod.getResponseHeader("Location");
                         if (location != null) {
                            LOG.debug("CAS Redirect: " + location);
                             String locationURL = location.getValue();
                            // XXX: if location is the same as before, wrong
                            // login
                            String idpSSOUrl = idp.getUrl();
                            if (!locationURL.startsWith(idpSSOUrl)) {
                                 LOG.error("CAS response is not the SSO url ("
                                        + idpSSOUrl + "): " + locationURL);
                                 throw new AuthException(idp.getAuthTypeName()
                                         + " Authentication failed: "
                                         + this.credentials_);
                             }
                             idpLoginFormResponseURI = new URI(locationURL,
                                     false);
                         }
                         else {
                            LOG.error("CAS send status 302 but no redirect Location");
                             throw new AuthException(idp.getAuthTypeName()
                                     + " Authentication failed: "
                                     + this.credentials_);
                         }
                     }
                     // Pubcookie send 200 + fucking HTML meta-data refresh!!!
                     // <meta http-equiv="Refresh"
                     // content="0;URL=https://cipher.ethz.ch/login/index.cgi">
                     else if (formLoginResponseStatus == 200
                             && idp.getAuthType() == IdentityProvider.SSO_AUTHTYPE_PUBCOOKIE) {
                         LOG.debug("Process Pubcookie response...");
                         String pubcookieRefreshURL = null;
                         InputStream pubcookieResponse = POSTLoginFormMethod.getResponseBodyAsStream();
                         Source pubcookieSource = new Source(pubcookieResponse);
                         List metas = pubcookieSource.findAllElements(Tag.META);
                         Iterator metasIterator = metas.iterator();
                         while (metasIterator.hasNext()) {
                             Element meta = (Element) metasIterator.next();
                             LOG.debug("Pubcookie META: " + meta);
                             String metaRefresh = meta.getAttributeValue("HTTP-EQUIV");
                             if (metaRefresh != null
                                     && metaRefresh.equalsIgnoreCase("Refresh")) {
                                 String metaContent = meta.getAttributeValue("CONTENT");
                                 // format: <n>;URL=<location> where <n> in
                                 // second
                                 int pos = metaContent.indexOf(";URL=");
                                 if (pos > 0) {
                                     pubcookieRefreshURL = metaContent.substring(pos + 5);
                                 }
                             }
                         }
 
                         if (pubcookieRefreshURL != null) {
                             LOG.debug("Pubcookie refresh: "
                                     + pubcookieRefreshURL);
                             idpLoginFormResponseURI = new URI(
                                     pubcookieRefreshURL, false);
                         }
                         else {
                             LOG.error("Pubcookie META Refresh not found");
                             throw new AuthException(idp.getAuthTypeName()
                                     + " Authentication failed: "
                                     + this.credentials_);
                         }
 
                         // XXX
                         dumpHttpClientCookies();
 
                     }
                     else {
                         LOG.error("Unexpected response status: "
                                 + formLoginResponseStatus + " AuthType:"
                                 + idp.getAuthTypeName());
                         throw new AuthException(idp.getAuthTypeName()
                                 + " Authentication failed: "
                                 + this.credentials_);
                     }
 
                     LOG.debug("POSTLoginFormMethod.releaseConnection()");
                     POSTLoginFormMethod.releaseConnection();
                 }
             }
         } // end while
 
         if (!formFound) {
             LOG.error("FORM name=" + idp.getAuthFormName() + " not found");
             throw new ServiceException("FORM name=" + idp.getAuthFormName()
                     + " not found");
         }
 
         return idpLoginFormResponseURI;
 
     }
 
     /**
      * @return isAuthenticated
      */
     public boolean isAuthenticated() {
         return this.isAuthenticated_;
     }
 
     /**
      * Delegates execution of the HttpMethod to the underlying HttpClient.
      * 
      * @param method
      *            The HttpMethod to execute.
      * @return The method's response code.
      * @throws HttpException
      *             If an I/O (transport) error occurs.
      * @throws IOException
      *             If a protocol exception occurs.
      */
     public int executeMethod(HttpMethod method) throws HttpException,
             IOException {
         // use delegate
         return this.httpClient_.executeMethod(method);
     }
 
     private void dumpHttpClientCookies() {
         if (LOG.isDebugEnabled()) {
             Cookie[] cookies = this.httpClient_.getState().getCookies();
             StringBuffer sb = new StringBuffer();
             sb.append("\n");
             sb.append("CookiePolicy=").append(
                     httpClient_.getParams().getCookiePolicy()).append("\n");
             sb.append("---HTTPCLIENT COOKIES BEGIN---").append("\n");
             for (int i = 0; i < cookies.length; i++) {
                 String path = cookies[i].getPath();
                 String domain = cookies[i].getDomain();
                 boolean secure = cookies[i].getSecure();
                 // sb.append(name).append('=').append(value).append("\n");
                 sb.append(i).append(": ").append(cookies[i]);
                 sb.append(" domain:").append(domain);
                 sb.append(" path:").append(path);
                 sb.append(" secure:").append(secure);
                 sb.append("\n");
             }
             sb.append("---HTTPCLIENT COOKIES END---");
             LOG.debug(sb.toString());
         }
     }
 
     /**
      * Returns the <b>first</b> Cookie or <code>null</code> if not exists.
      * 
      * @param name
      *            The Cookie name
      * @return The cookie or <code>null</code>
      */
     private Cookie getCookie(String name) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("Cookie name=" + name);
         }
         Cookie[] cookies = this.httpClient_.getState().getCookies();
         for (int i = 0; i < cookies.length; i++) {
             String cookieName = cookies[i].getName();
             if (name.equals(cookieName)) {
                 return cookies[i];
             }
         }
         return null;
     }
 
     private Cookie[] getMatchingCookies(String name, String host, String path) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("search Cookie matching name:" + name + " host:" + host
                     + " path:" + path);
         }
         List matchingCookies = new ArrayList();
         Cookie[] cookies = this.httpClient_.getState().getCookies();
         CookieSpecBase cookieSpecBase = new CookieSpecBase();
         for (int i = 0; i < cookies.length; i++) {
             Cookie cookie = cookies[i];
             if (cookieSpecBase.match(host, 443, path, true, cookie)) {
                 if (cookie.getName().equals(name)) {
                     LOG.debug("Cookie " + cookie + " matched");
                     matchingCookies.add(cookie);
                 }
             }
         }
         return (Cookie[]) matchingCookies.toArray(new Cookie[matchingCookies.size()]);
     }
 
 }

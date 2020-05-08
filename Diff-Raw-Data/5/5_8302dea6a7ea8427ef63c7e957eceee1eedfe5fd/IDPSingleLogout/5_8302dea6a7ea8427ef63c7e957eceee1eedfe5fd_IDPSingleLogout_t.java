 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: IDPSingleLogout.java,v 1.8 2007-10-05 23:00:00 weisun2 Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 
 package com.sun.identity.saml2.profile;
 
 import com.sun.identity.saml2.common.NameIDInfoKey;
 import com.sun.identity.saml2.assertion.NameID;
 import com.sun.identity.multiprotocol.MultiProtocolUtils;
 import com.sun.identity.multiprotocol.SingleLogoutManager;
 import com.sun.net.ssl.internal.ssl.Provider;
 import com.sun.org.apache.bcel.internal.generic.SIPUSH;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.logging.Level;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.sun.identity.shared.Constants;
 import com.sun.identity.shared.debug.Debug;
 import com.sun.identity.plugin.session.SessionManager;
 import com.sun.identity.plugin.session.SessionProvider;
 import com.sun.identity.plugin.session.SessionException;
 import com.sun.identity.saml2.assertion.Issuer;
 import com.sun.identity.saml2.common.SAML2Constants;
 import com.sun.identity.saml2.common.SAML2Exception;
 import com.sun.identity.saml2.common.SAML2Utils;
 import com.sun.identity.saml2.jaxb.entityconfig.SPSSOConfigElement;
 import com.sun.identity.saml2.jaxb.metadata.SPSSODescriptorElement;
 import com.sun.identity.saml2.jaxb.metadata.IDPSSODescriptorElement;
 import com.sun.identity.saml2.logging.LogUtil;
 import com.sun.identity.saml2.meta.SAML2MetaException;
 import com.sun.identity.saml2.meta.SAML2MetaManager;
 import com.sun.identity.saml2.meta.SAML2MetaUtils;
 import com.sun.identity.saml2.protocol.LogoutRequest;
 import com.sun.identity.saml2.protocol.LogoutResponse;
 import com.sun.identity.saml2.protocol.ProtocolFactory;
 import com.sun.identity.saml2.protocol.Status;
 import com.sun.identity.saml2.protocol.StatusCode;
 import java.util.HashSet;
 import java.util.Set;
 import com.sun.identity.saml2.common.AccountUtils;
 
 
 /**
  * This class reads the required data from HttpServletRequest and
  * initiates the <code>LogoutRequest</code> from IDP to SP.
  */
 
 public class IDPSingleLogout {
 
     static SAML2MetaManager sm = null;
     static Debug debug = SAML2Utils.debug;
     static SessionProvider sessionProvider = null;
     static {
         try {
             sm = new SAML2MetaManager();
         } catch (SAML2MetaException sme) {
             debug.error("Error retreiving metadata",sme);
         }
         try {
             sessionProvider = SessionManager.getProvider();
         } catch (SessionException se) {
             debug.error("Error retreiving session provider.", se);
         }
         
     }
 
     /**
      * Parses the request parameters and initiates the Logout
      * Request to be sent to the SP.
      *
      * @param request the HttpServletRequest.
      * @param response the HttpServletResponse.
      * @param binding binding used for this request.
      * @param paramsMap Map of all other parameters.
      *       Following parameters names with their respective
      *       String values are allowed in this paramsMap.
      *       "RelayState" - the target URL on successful Single Logout
      *       "Destination" - A URI Reference indicating the address to
      *                       which the request has been sent.
      *       "Consent" - Specifies a URI a SAML defined identifier
      *                   known as Consent Identifiers.
      *       "Extension" - Specifies a list of Extensions as list of 
      *                   String objects.
      * @throws SAML2Exception if error initiating request to SP.
      */
     public static void initiateLogoutRequest(HttpServletRequest request,
         HttpServletResponse response,
         String binding,
         Map paramsMap)
     throws SAML2Exception {
 
         if (debug.messageEnabled()) {
             debug.message("in initiateLogoutRequest");
             debug.message("binding : " + binding);
             debug.message("logoutAll : " + 
                             (String) paramsMap.get(SAML2Constants.LOGOUT_ALL));
             debug.message("paramsMap : " + paramsMap);
         }
         
         boolean logoutall = false;
         String logoutAllValue = 
                    (String)paramsMap.get(SAML2Constants.LOGOUT_ALL);
         if ((logoutAllValue != null) && 
                         logoutAllValue.equalsIgnoreCase("true")) {
             logoutall = true;
         }
         
         String metaAlias = (String)paramsMap.get(SAML2Constants.IDP_META_ALIAS);
         try {
             Object session = sessionProvider.getSession(request);
             String sessUser = sessionProvider.getPrincipalName(session);
             if (session == null) {
                 throw new SAML2Exception(
                     SAML2Utils.bundle.getString("nullSSOToken"));
             }
             if (metaAlias == null) {
                 String[] values = sessionProvider.
                     getProperty(session, SAML2Constants.IDP_META_ALIAS);
                 if (values != null && values.length != 0) {            
                     metaAlias = values[0];
                 }
             }
             if (metaAlias == null) {
                     throw new SAML2Exception(
                         SAML2Utils.bundle.getString("nullIDPMetaAlias"));
             }
             paramsMap.put(SAML2Constants.METAALIAS, metaAlias);
             
             String realm = SAML2Utils.
                     getRealm(SAML2MetaUtils.getRealmByMetaAlias(metaAlias));
 
             String idpEntityID = sm.getEntityByMetaAlias(metaAlias);
             if (idpEntityID == null) {
                 debug.error("Identity Provider ID is missing");
                 String[] data = {idpEntityID};
                 LogUtil.error(
                     Level.INFO,LogUtil.INVALID_IDP,data,null);
                 throw new SAML2Exception(
                     SAML2Utils.bundle.getString("nullIDPEntityID"));
             }
             // clean up session index
             String tokenID = sessionProvider.getSessionID(session);
             Enumeration keys = IDPCache.idpSessionsByIndices.keys();
             String idpSessionIndex = null;
             IDPSession idpSession = null;
             Object idpToken = null;
             while (keys.hasMoreElements()) {
                 idpSessionIndex = (String)keys.nextElement();   
                 idpSession = (IDPSession)IDPCache.
                     idpSessionsByIndices.get(idpSessionIndex);
                 if (idpSession != null) {
                     idpToken = idpSession.getSession();
                     if ((idpToken != null) && 
                         (tokenID.equals(sessionProvider.getSessionID(idpToken)))) {
                         break;
                     }
                 } else {
                     IDPCache.idpSessionsByIndices.remove(idpSessionIndex);
                     IDPCache.authnContextCache.remove(idpSessionIndex);
                 }       
                 idpSessionIndex = null;
             }
 
             if (idpSessionIndex == null) {
                 if (debug.messageEnabled()) {
                     debug.message("No SP session participant(s)");
                 }
                 MultiProtocolUtils.invalidateSession(session, request, 
                     response, SingleLogoutManager.SAML2);
                 return;
             }
             
             if (debug.messageEnabled()) {
                 debug.message("idpSessionIndex=" + idpSessionIndex);
             }
 
             List list = idpSession.getNameIDandSPpairs();
             int n = list.size();
             if (debug.messageEnabled()) {
                 debug.message("IDPSingleLogout.initiateLogoutReq:" +
                     " NameIDandSPpairs=" + list + ", size=" + n);
             }
 
             if (n == 0) {
                 if (debug.messageEnabled()) {
                     debug.message("No SP session participant(s)");
                 }
                 IDPCache.idpSessionsByIndices.remove(idpSessionIndex);
                 IDPCache.authnContextCache.remove(idpSessionIndex);
                 MultiProtocolUtils.invalidateSession(session, request,
                     response, SingleLogoutManager.SAML2);
                 return;
             }
 
             String relayState = 
                 (String)paramsMap.get(SAML2Constants.RELAY_STATE);
             int soapFailCount = 0;
             for (int i = 0; i < n; i++) {
                 NameIDandSPpair pair = null;
                 if (binding.equals(SAML2Constants.HTTP_REDIRECT)) {
                     pair = (NameIDandSPpair) list.remove(0);
                 } else if (binding.equals(SAML2Constants.SOAP)) {
                     pair = (NameIDandSPpair) list.get(i);
                 } else {
                     // not supported
                     debug.error("IDPSingleLogout. unsuported binding"
                         + binding);
                     throw new SAML2Exception(
                         SAML2Utils.bundle.getString("unsupportedSloBinding"));
                 }
                 String spEntityID = pair.getSPEntityID();
                 if (debug.messageEnabled()) {
                     debug.message("IDPSingleLogout.initLogoutReq:" +
                         " processing spEntityID " + spEntityID);
                 }
 
                 // get SPSSODescriptor
                 SPSSODescriptorElement spsso =
                     sm.getSPSSODescriptor(realm,spEntityID);
 
                 if (spsso == null) {
                     String[] data = { spEntityID};
                     LogUtil.error(Level.INFO,LogUtil.SP_METADATA_ERROR,data,
                         null);
                     throw new SAML2Exception(
                         SAML2Utils.bundle.getString("metaDataError"));
                 }
                 List extensionsList = LogoutUtil.getExtensionsList(paramsMap);
 
                 List slosList = spsso.getSingleLogoutService();
                 if (slosList == null) {
                     String[] data = { idpEntityID};
                     LogUtil.error(Level.INFO,LogUtil.SLO_NOT_FOUND,data,
                         null);
                     throw new SAML2Exception(
                         SAML2Utils.bundle.getString("sloServiceListNotfound"));
                 }
 
                  // get IDP entity config in case of SOAP, for basic auth info
                 SPSSOConfigElement spConfig = null;
                 if (binding.equals(SAML2Constants.SOAP)) {
                      spConfig = sm.getSPSSOConfig(
                          realm,
                          spEntityID
                      );
                      // destroy the session
                      if (i==0) {
                          if (logoutall == true) {
                              String userID = sessionProvider.
                                  getPrincipalName(idpSession.getSession());
                              destroyAllTokenForUser(
                                  userID,request,response);
                          } else {
                              MultiProtocolUtils.invalidateSession(
                                  idpSession.getSession(), request, response,
                                  SingleLogoutManager.SAML2);
                              IDPCache.idpSessionsByIndices.
                                  remove(idpSessionIndex);
                              IDPCache.authnContextCache.remove(idpSessionIndex);
                          }
                      }
                 }
 
                 if (logoutall == true) {
                     idpSessionIndex = null;
                 }
                 
                 StringBuffer requestID = null;
                 try {
                     requestID = LogoutUtil.doLogout(
                         metaAlias,
                         spEntityID,
                         slosList,
                         extensionsList,
                         binding,
                         relayState,
                         idpSessionIndex,
                         pair.getNameID(),
                         response,
                         paramsMap,
                         spConfig);
                 } catch (SAML2Exception ex) {
                     if (binding.equals(SAML2Constants.SOAP)) {
                         debug.error(
                             "IDPSingleLogout.initiateLogoutRequest:" , ex);
                         soapFailCount++;
                         continue;
                     } else {
                         throw ex;
                     }
                 }
 
                 String requestIDStr = requestID.toString();
                 if (debug.messageEnabled()) {
                     debug.message(
                         "\nIDPSLO.requestIDStr = " + requestIDStr +
                         "\nbinding = " + binding);
                 }
 
                 if (requestIDStr != null &&
                     requestIDStr.length() != 0 &&
                     binding.equals(SAML2Constants.HTTP_REDIRECT)) {
                     idpSession.setPendingLogoutRequestID(requestIDStr);
                     idpSession.setLogoutAll(logoutall);
                     break;
                 }
             }
 
             if (binding.equals(SAML2Constants.SOAP)) {
                 int logoutStatus = SingleLogoutManager.LOGOUT_SUCCEEDED_STATUS;
                 boolean isMultiProtocol = 
                     MultiProtocolUtils.isMultipleProtocolSession(request,
                     SingleLogoutManager.SAML2);
                 if (soapFailCount == n) {
                     if (isMultiProtocol) {
                         logoutStatus = SingleLogoutManager.LOGOUT_FAILED_STATUS;
                     } else {
                         throw new SAML2Exception(
                             SAML2Utils.bundle.getString("sloFailed"));
                     }
                 } else if (soapFailCount > 0) {
                     if (isMultiProtocol) {
                         logoutStatus = 
                             SingleLogoutManager.LOGOUT_PARTIAL_STATUS;
                     } else {
                         throw new SAML2Exception(
                             SAML2Utils.bundle.getString("partialLogout"));
                     }
                 }
                 // processing multi-federation protocol session
                 if (isMultiProtocol) {
                     Set set = new HashSet();
                     set.add(session);
                     boolean isSOAPInitiated = 
                         binding.equals(SAML2Constants.SOAP) ? true : false;
                     int retStat = SingleLogoutManager.LOGOUT_SUCCEEDED_STATUS;
                     try {
                         debug.message("IDPSingleLogout.initLogReq: MP");
                         retStat = SingleLogoutManager.getInstance().
                             doIDPSingleLogout(set, sessUser, request, response,
                             isSOAPInitiated, true, SingleLogoutManager.SAML2,
                             realm, idpEntityID, null, relayState, null, null,
                             logoutStatus);
                     } catch (Exception ex) {
                         debug.warning("IDPSingleLogout.initiateLoogutReq: MP", 
                             ex);
                         throw new SAML2Exception(ex.getMessage());
                     }
                     if (debug.messageEnabled()) {
                         debug.message("IDPSingleLogout.initLogoutRequest: "
                             + "SLOManager return status = " + retStat);
                     }
                     switch (retStat) {
                         case SingleLogoutManager.LOGOUT_FAILED_STATUS:
                             throw new SAML2Exception(
                                     SAML2Utils.bundle.getString("sloFailed"));
                         case SingleLogoutManager.LOGOUT_PARTIAL_STATUS:
                             throw new SAML2Exception(
                                     SAML2Utils.bundle.getString("partialLogout"));
                         default:
                             break;
                     }
                 }
             }
         } catch (SAML2MetaException sme) {
             debug.error("Error retreiving metadata",sme);
             throw new SAML2Exception(
                 SAML2Utils.bundle.getString("metaDataError"));
         } catch (SessionException ssoe) {
             debug.error("SessionException: ",ssoe);
             throw new SAML2Exception(
                 SAML2Utils.bundle.getString("metaDataError"));
         }
     }
 
     /**
      * Gets and processes the Single <code>LogoutRequest</code> from SP.
      *
      * @param request the HttpServletRequest.
      * @param response the HttpServletResponse.
      * @param samlRequest <code>LogoutRequest</code> in the
      *          XML string format.
      * @param relayState the target URL on successful
      * <code>LogoutRequest</code>.
      * @throws SAML2Exception if error processing
      *          <code>LogoutRequest</code>.
      * @throws SessionException if error processing
      *          <code>LogoutRequest</code>.
      */
     public static void processLogoutRequest(
         HttpServletRequest request,
         HttpServletResponse response,
         String samlRequest,
         String relayState) throws SAML2Exception, SessionException {
         String method = "processLogoutRequest : ";
         if (debug.messageEnabled()) {
             debug.message(method + "IDPSingleLogout:processLogoutRequest");
             debug.message(method + "samlRequest : " + samlRequest);
             debug.message(method + "relayState : " + relayState);
         }
         String decodedStr =
             SAML2Utils.decodeFromRedirect(samlRequest);
         if (decodedStr == null) {
             throw new SAML2Exception(
                 SAML2Utils.bundle.getString(
                 "nullDecodedStrFromSamlRequest"));
         }
         
         LogoutRequest logoutReq = 
             ProtocolFactory.getInstance().createLogoutRequest(decodedStr);
         String metaAlias =
                 SAML2MetaUtils.getMetaAliasByUri(request.getRequestURI()) ;
         String realm = SAML2Utils.
                 getRealm(SAML2MetaUtils.getRealmByMetaAlias(metaAlias));
         String idpEntityID = sm.getEntityByMetaAlias(metaAlias);
         String spEntityID = logoutReq.getIssuer().getValue();
              
         boolean needToVerify = 
             SAML2Utils.getWantLogoutRequestSigned(realm, idpEntityID, 
                             SAML2Constants.IDP_ROLE);
         if (debug.messageEnabled()) {
             debug.message(method + "metaAlias : " + metaAlias);
             debug.message(method + "realm : " + realm);
             debug.message(method + "idpEntityID : " + idpEntityID);
             debug.message(method + "spEntityID : " + spEntityID);
         }
         
         if (needToVerify == true) {
             String queryString = request.getQueryString();
             boolean valid = 
                         SAML2Utils.verifyQueryString(queryString, realm,
                                     SAML2Constants.IDP_ROLE, spEntityID);
             if (valid == false) {
                     debug.error("Invalid signature in SLO Request.");
                     throw new SAML2Exception(
                         SAML2Utils.bundle.getString("invalidSignInRequest"));
             }
             IDPSSODescriptorElement idpsso =
                 sm.getIDPSSODescriptor(realm, idpEntityID);
             String loc = null; 
             if (idpsso != null) {
                 List sloList = idpsso.getSingleLogoutService();
                 if ((sloList != null) && (!sloList.isEmpty())) {
                     loc = LogoutUtil.getSLOResponseServiceLocation(
                           sloList, SAML2Constants.HTTP_REDIRECT);
                     if ((loc == null) || (loc.length() == 0)) {
                         loc = LogoutUtil.getSLOServiceLocation(
                              sloList, SAML2Constants.HTTP_REDIRECT);
                     }
                 }                   
             }
             if (!SAML2Utils.verifyDestination(logoutReq.getDestination(),
                 loc)) {
                 throw new SAML2Exception(
                     SAML2Utils.bundle.getString("invalidDestination"));
             }   
         }
 
         LogoutResponse logoutRes = processLogoutRequest(
             logoutReq,
             request,
             response,
             SAML2Constants.HTTP_REDIRECT,
             relayState,
             idpEntityID,
             realm
             );
         if (logoutRes == null) {
             // this is the case where there is more SP session participant
             // and processLogoutRequest() sends LogoutRequest to one of them
             // already
             // through HTTP_Redirect, nothing to do here
             return;
         }
         // this is the case where there is no more SP session
         // participant
        
         String location = getSingleLogoutLocation(spEntityID, realm); 
         logoutRes.setDestination(location); 
         
         // call multi-federation protocol processing
         // this is SP initiated HTTP based single logout
         boolean isMultiProtocolSession = false;
         int retStatus = SingleLogoutManager.LOGOUT_SUCCEEDED_STATUS;
         Object session=null;
         SessionProvider provider = null;
         try {
             provider = SessionManager.getProvider();
             session = provider.getSession(request);
             if ((session != null) && (provider.isValid(session))
                 && MultiProtocolUtils.isMultipleProtocolSession(session,
                     SingleLogoutManager.SAML2)) {
                 isMultiProtocolSession = true;
                 // call Multi-Federation protocol SingleLogoutManager
                 SingleLogoutManager sloManager = 
                     SingleLogoutManager.getInstance();
                 Set set = new HashSet();
                 set.add(session);
                 String uid =  provider.getPrincipalName(session);
                 debug.message("IDPSingleLogout.processLogReq: MP/SPinit/Http");
                 retStatus = sloManager.doIDPSingleLogout(set, uid, request, 
                     response, false, false, SingleLogoutManager.SAML2, realm, 
                     idpEntityID, spEntityID, relayState, logoutReq.toString(),
                     logoutRes.toXMLString(), getLogoutStatus(logoutRes));
             }
         } catch (SessionException e) {
             // ignore as session might not be valid
             debug.message("IDPSingleLogout.processLogoutRequest: session",e);
         } catch (Exception e) {
             debug.message("IDPSingleLogout.processLogoutRequest: MP2",e);
             retStatus = SingleLogoutManager.LOGOUT_FAILED_STATUS;
         }
 
         if (!isMultiProtocolSession || 
             (retStatus != SingleLogoutManager.LOGOUT_REDIRECTED_STATUS)) {
             logoutRes = updateLogoutResponse(logoutRes, retStatus);
             List partners = IDPProxyUtil.getSessionPartners(request);
             if (partners != null &&  !partners.isEmpty()) {
                 IDPProxyUtil.sendProxyLogoutRequest(request, response,
                    samlRequest, partners);
             } else {
                 LogoutUtil.sendSLOResponse(response, logoutRes, location, 
                     relayState, realm, idpEntityID,
                     SAML2Constants.IDP_ROLE, spEntityID);
             }    
         }
     }
 
 
     /**
      * Returns single logout location for the service provider.
      */
     public static String getSingleLogoutLocation(String spEntityID,
         String realm) throws SAML2Exception {
         SPSSODescriptorElement spsso = sm.getSPSSODescriptor(realm,spEntityID);
 
         if (spsso == null) {
             String[] data = { spEntityID};
             LogUtil.error(Level.INFO,LogUtil.SP_METADATA_ERROR,data,
                 null);
             throw new SAML2Exception(
                 SAML2Utils.bundle.getString("metaDataError"));
         }
         List slosList = spsso.getSingleLogoutService();
 
         String location = 
             LogoutUtil.getSLOResponseServiceLocation(slosList, 
                             SAML2Constants.HTTP_REDIRECT);
 
         if (location == null || location.length() == 0) {
             location = LogoutUtil.getSLOServiceLocation(
                 slosList,
                 SAML2Constants.HTTP_REDIRECT);
             if (location == null || location.length() == 0) {
                 debug.error(
                     "Unable to find the IDP's single logout "+
                     "response service with the HTTP-Redirect binding");
                 throw new SAML2Exception(
                     SAML2Utils.bundle.getString(
                     "sloResponseServiceLocationNotfound"));
             } else {
                 if (debug.messageEnabled()) {
                     debug.message(
                         "SP's single logout response service location = "+
                         location);
                 }
             }
         } else {
             if (debug.messageEnabled()) {
                 debug.message(
                     "IDP's single logout response service location = "+
                     location);
             }
         }
         if (debug.messageEnabled()) {
             debug.message("IDPSingleLogout.getSLOLocation: loc=" + location);
         }
         return location;
     }
 
     private static int getLogoutStatus(LogoutResponse logoutRes) {
         StatusCode statusCode = logoutRes.getStatus().getStatusCode();
         String code = statusCode.getValue();
         if (code.equals(SAML2Constants.STATUS_SUCCESS)) {
             return SingleLogoutManager.LOGOUT_SUCCEEDED_STATUS;
         } else {
             return SingleLogoutManager.LOGOUT_FAILED_STATUS;
         }
     }
 
     /**
      * Gets and processes the Single <code>LogoutResponse</code> from SP,
      * destroys the local session, checks response's issuer
      * and inResponseTo.
      *
      * @param request the HttpServletRequest.
      * @param response the HttpServletResponse.
      * @param samlResponse <code>LogoutResponse</code> in the
      *          XML string format.
      * @param relayState the target URL on successful
      * <code>LogoutResponse</code>.
      * @return true if jsp has sendRedirect for relayState, false otherwise
      * @throws SAML2Exception if error processing
      *          <code>LogoutResponse</code>.
      * @throws SessionException if error processing
      *          <code>LogoutResponse</code>.
      */
     public static boolean processLogoutResponse(
         HttpServletRequest request,
         HttpServletResponse response,
         String samlResponse,
         String relayState) throws SAML2Exception, SessionException {
         String method = "processLogoutResponse : ";
         if (debug.messageEnabled()) {
             debug.message(method + "samlResponse : " + samlResponse);
             debug.message(method + "relayState : " + relayState);
         }
         String decodedStr =
             SAML2Utils.decodeFromRedirect(samlResponse);
         if (decodedStr == null) {
             throw new SAML2Exception(
                 SAML2Utils.bundle.getString(
                 "nullDecodedStrFromSamlResponse"));
         }
 
         LogoutResponse logoutRes = 
                 ProtocolFactory.getInstance().createLogoutResponse(decodedStr);
         String metaAlias =
                 SAML2MetaUtils.getMetaAliasByUri(request.getRequestURI()) ;
         String realm = SAML2Utils.
                 getRealm(SAML2MetaUtils.getRealmByMetaAlias(metaAlias));
         String idpEntityID = sm.getEntityByMetaAlias(metaAlias);
         String spEntityID = logoutRes.getIssuer().getValue();
         Issuer resIssuer = logoutRes.getIssuer();
             String requestId = logoutRes.getInResponseTo();
         SAML2Utils.verifyResponseIssuer(
                             realm, idpEntityID, resIssuer, requestId);
         
         boolean needToVerify = 
              SAML2Utils.getWantLogoutResponseSigned(realm, idpEntityID, 
                              SAML2Constants.IDP_ROLE);
         if (debug.messageEnabled()) {
             debug.message(method + "metaAlias : " + metaAlias);
             debug.message(method + "realm : " + realm);
             debug.message(method + "idpEntityID : " + idpEntityID);
             debug.message(method + "spEntityID : " + spEntityID);
         }
         
         if (needToVerify == true) {
             String queryString = request.getQueryString();
             boolean valid = SAML2Utils.verifyQueryString(queryString, realm,
                             SAML2Constants.IDP_ROLE, spEntityID);
             if (valid == false) {
                 debug.error("Invalid signature in SLO Response.");
                     throw new SAML2Exception(
                         SAML2Utils.bundle.getString("invalidSignInResponse"));
             }
             IDPSSODescriptorElement idpsso =
                 sm.getIDPSSODescriptor(realm, idpEntityID);
             String loc = null; 
             if (idpsso != null) {
                 List sloList = idpsso.getSingleLogoutService();
                 if (sloList != null && !sloList.isEmpty()) {
                      loc = LogoutUtil.getSLOResponseServiceLocation(
                           sloList, SAML2Constants.HTTP_REDIRECT);
                     if (loc == null || (loc.length() == 0)) {
                         loc = LogoutUtil.getSLOServiceLocation(
                              sloList, SAML2Constants.HTTP_REDIRECT);
                     }
                 }
             }
             if (!SAML2Utils.verifyDestination(logoutRes.getDestination(),
                 loc)) {
                 throw new SAML2Exception(
                     SAML2Utils.bundle.getString("invalidDestination"));
             }  
         }
 
         // use the cache to figure out which session index is in question
         // and then use the cache to see if any more SPs to send logout request
         // if yes, send one
         // if no, do local logout and send response back to original requesting
         // SP (this SP name should be remembered in cache)
 
         Object session = sessionProvider.getSession(request);
         String tokenID = sessionProvider.getSessionID(session);
 
         Enumeration keys = IDPCache.idpSessionsByIndices.keys();
         String idpSessionIndex = null;
         IDPSession idpSession = null;
         Object idpToken = null;
         while (keys.hasMoreElements()) {
             idpSessionIndex = (String)keys.nextElement();   
             idpSession = (IDPSession)IDPCache.
                 idpSessionsByIndices.get(idpSessionIndex);
             if (idpSession != null) {
                 idpToken = idpSession.getSession();
                 if ((idpToken != null) && 
                     (tokenID.equals(sessionProvider.getSessionID(idpToken)))) {
                     break;
                 }
             } else {
                 IDPCache.idpSessionsByIndices.remove(idpSessionIndex);
                 IDPCache.authnContextCache.remove(idpSessionIndex);
             }       
             idpSessionIndex = null;
         }
 
         if (idpSessionIndex == null) {
             if (debug.messageEnabled()) {
                 debug.message("No SP session participant(s)");
             }
             MultiProtocolUtils.invalidateSession(session, request,
                 response, SingleLogoutManager.SAML2);
             return false;
         }
         
         if (debug.messageEnabled()) {
             debug.message("idpSessionIndex=" + idpSessionIndex);
         }
 
         List list = idpSession.getNameIDandSPpairs();
         debug.message("idpSession.getNameIDandSPpairs()=" + list);
 
         if (list.size() == 0) {
             // this is the last response
             // correlate inResponseTo to request ID
             String requestID = idpSession.getPendingLogoutRequestID();
             String inResponseTo = logoutRes.getInResponseTo();
             if (inResponseTo != null && requestID != null &&
                 inResponseTo.equals(requestID)) {
                 if (debug.messageEnabled()) {
                     debug.message(
                         "LogoutRespone's inResponseTo matches "+
                         "the previous LogoutRequest's ID.");
                 }
             }
             String originatingRequestID = 
                 idpSession.getOriginatingLogoutRequestID();
             if (originatingRequestID == null) {
                 // this is IDP initiated SLO
                 if (idpSession.getLogoutAll() == true) {
                     String userID = sessionProvider.
                         getPrincipalName(idpSession.getSession());
                     destroyAllTokenForUser(
                         userID, request, response);
                 } else {
                     IDPCache.idpSessionsByIndices.remove(idpSessionIndex);
                     IDPCache.authnContextCache.remove(idpSessionIndex);
                     if (!MultiProtocolUtils.isMultipleProtocolSession(
                         idpSession.getSession(), SingleLogoutManager.SAML2)) {
                         sessionProvider.invalidateSession(
                             idpSession.getSession(), request, response);
                     } else {
                         MultiProtocolUtils.removeFederationProtocol(
                             idpSession.getSession(), SingleLogoutManager.SAML2);
                         // call Multi-Federation protocol SingleLogoutManager
                         SingleLogoutManager sloManager =
                             SingleLogoutManager.getInstance();
                         Set set = new HashSet();
                         set.add(session);
                         SessionProvider provider = 
                             SessionManager.getProvider();
                         String uid = provider.getPrincipalName(session);
                         debug.message("IDPSingleLogout.processLogRes: MP/Http");
                         int retStatus = 
                             SingleLogoutManager.LOGOUT_SUCCEEDED_STATUS;
                         try {
                             retStatus = sloManager.doIDPSingleLogout(set, uid, 
                                 request, response, false, true, 
                                 SingleLogoutManager.SAML2, realm, idpEntityID, 
                                 spEntityID, relayState, null, null, 
                                 getLogoutStatus(logoutRes));
                         } catch (SAML2Exception ex) {
                             throw ex;
                         } catch (Exception ex) {
                             debug.error("IDPSIngleLogout.processLogoutResponse:"
                                  + " MP/IDP initiated HTTP", ex);
                             throw new SAML2Exception(ex.getMessage());
                         }
                         if (retStatus == 
                             SingleLogoutManager.LOGOUT_REDIRECTED_STATUS) {
                             return true;
                         }
                     }
                 }
                 debug.message("IDP initiated SLO Success");
                 return false;
             }
             String originatingLogoutSPEntityID = 
                 idpSession.getOriginatingLogoutSPEntityID();
 
             SPSSODescriptorElement spsso = null;
             // get SPSSODescriptor
             spsso =
                 sm.getSPSSODescriptor(realm,originatingLogoutSPEntityID);
 
             if (spsso == null) {
                 String[] data = { originatingLogoutSPEntityID};
                 LogUtil.error(Level.INFO,LogUtil.SP_METADATA_ERROR,data,
                     null);
                 throw new SAML2Exception(
                     SAML2Utils.bundle.getString("metaDataError"));
             }
             List slosList = spsso.getSingleLogoutService();
 
             String location = LogoutUtil.
                 getSLOResponseServiceLocation(slosList,
                 SAML2Constants.HTTP_REDIRECT);
 
             if (location == null || location.length() == 0) {
                 location = LogoutUtil.getSLOServiceLocation(
                     slosList,
                     SAML2Constants.HTTP_REDIRECT);
                 if (location == null || location.length() == 0) {
                     debug.error(
                         "Unable to find the IDP's single logout "+
                         "response service with the HTTP-Redirect binding");
                     throw new SAML2Exception(
                         SAML2Utils.bundle.getString(
                         "sloResponseServiceLocationNotfound"));
                 } else {
                     if (debug.messageEnabled()) {
                         debug.message(
                             "SP's single logout response service location = "+
                             location);
                     }
                 }
             } else {
                 if (debug.messageEnabled()) {
                     debug.message(
                         "IDP's single logout response service location = "+
                         location);
                 }
             }
 
             Status status = destroyTokenAndGenerateStatus(
                 idpSessionIndex, idpSession.getSession(),
                 request, response, true);
             
             logoutRes = LogoutUtil.generateResponse(status, 
                 originatingRequestID, SAML2Utils.createIssuer(idpEntityID),
                 realm, SAML2Constants.IDP_ROLE, 
                 logoutRes.getIssuer().getValue());
             
             if (location != null && logoutRes != null) {
                 logoutRes.setDestination(location); 
                 IDPCache.idpSessionsByIndices.remove(idpSessionIndex);
                 IDPCache.authnContextCache.remove(idpSessionIndex);
                 
                 // call multi-federation protocol processing
                 // this is the SP initiated HTTP binding case
                 boolean isMultiProtocolSession = false;
                 int retStatus = SingleLogoutManager.LOGOUT_SUCCEEDED_STATUS;
                 try {
                     SessionProvider provider = SessionManager.getProvider();
                     session = idpSession.getSession();
                     if ((session != null) && (provider.isValid(session)) &&
                         MultiProtocolUtils.isMultipleProtocolSession(session,
                            SingleLogoutManager.SAML2)) {
                         isMultiProtocolSession = true;
                         // call Multi-Federation protocol SingleLogoutManager
                         SingleLogoutManager sloManager =
                             SingleLogoutManager.getInstance();
                         Set set = new HashSet();
                         set.add(session);
                         String uid = provider.getPrincipalName(session);
                         debug.message("IDPSingleLogout.processLogRes: MP/Http");
                         retStatus = sloManager.doIDPSingleLogout(set, uid, 
                             request, response, false, true, 
                             SingleLogoutManager.SAML2, realm, idpEntityID, 
                             spEntityID, relayState, null, 
                             logoutRes.toXMLString(), 
                             getLogoutStatus(logoutRes));
                     }
                 } catch (SessionException e) {
                     // ignore as session might not be valid
                     debug.message("IDPSingleLogout.processLogoutRequest: session",e);
                 } catch (Exception e) {
                     debug.message("IDPSingleLogout.processLogoutRequest: MP2",e);
                     retStatus = SingleLogoutManager.LOGOUT_FAILED_STATUS;
                 }
 
                 if (!isMultiProtocolSession || (retStatus != 
                     SingleLogoutManager.LOGOUT_REDIRECTED_STATUS)) {
                     logoutRes = updateLogoutResponse(logoutRes, retStatus);
                     LogoutUtil.sendSLOResponse(response, logoutRes, location,
                         relayState, realm, idpEntityID, 
                         SAML2Constants.IDP_ROLE, spEntityID);
                     return true;
                 } else {
                     return false;
                 }
             }
             
             IDPCache.idpSessionsByIndices.remove(idpSessionIndex);
             IDPCache.authnContextCache.remove(idpSessionIndex);
             return false;
         } else {
             // send Next Request
             NameIDandSPpair pair = (NameIDandSPpair)list.remove(0);
             spEntityID = pair.getSPEntityID();
 
             SPSSODescriptorElement spsso = null;
             // get SPSSODescriptor
             spsso =
                 sm.getSPSSODescriptor(realm,spEntityID);
 
             if (spsso == null) {
                 String[] data = {spEntityID};
                 LogUtil.error(Level.INFO,LogUtil.SP_METADATA_ERROR,data,
                     null);
                 throw new SAML2Exception(
                     SAML2Utils.bundle.getString("metaDataError"));
             }
             List slosList = spsso.getSingleLogoutService(); 
             List extensionsList = 
                 LogoutUtil.getExtensionsList(request.getParameterMap());
 
             HashMap paramsMap = new HashMap(request.getParameterMap());
             paramsMap.put(SAML2Constants.ROLE, SAML2Constants.IDP_ROLE);
             StringBuffer requestID = LogoutUtil.doLogout(
                 metaAlias,
                 spEntityID,
                 slosList,
                 extensionsList,
                 SAML2Constants.HTTP_REDIRECT,
                 relayState,
                 idpSessionIndex,
                 pair.getNameID(),
                 response,
                 paramsMap,
                 null);
 
             String requestIDStr = requestID.toString();
             if (debug.messageEnabled()) {
                 debug.message(
                     "\requestIDStr = " + requestIDStr +
                     "\nbinding = " + SAML2Constants.HTTP_REDIRECT);
             }
 
             if (requestIDStr != null &&
                 requestIDStr.length() != 0) {
                 idpSession.setPendingLogoutRequestID(requestIDStr);
             }
             
             return true;
         }
     }
     
     /**
      * Gets and processes the Single <code>LogoutRequest</code> from SP
      * and return <code>LogoutResponse</code>.
      *
      * @param logoutReq <code>LogoutRequest</code> from SP
      * @param request the HttpServletRequest.
      * @param response the HttpServletResponse.
      * @param binding name of binding will be used for request processing.
      * @param relayState the relay state.
      * @param idpEntityID name of host entity ID.
      * @param realm name of host entity.
      * @return LogoutResponse the target URL on successful
      * <code>LogoutRequest</code>.
      * @throws SAML2Exception if error processing
      *          <code>LogoutRequest</code>.
      */
     public static LogoutResponse processLogoutRequest(
         LogoutRequest logoutReq,
         HttpServletRequest request,
         HttpServletResponse response,
         String binding,
         String relayState,
         String idpEntityID,
         String realm) throws SAML2Exception {
       
         Status status = null;
         String spEntity = logoutReq.getIssuer().getValue();
         Object session = null;
         try {
             do {
                 String requestId = logoutReq.getID();
                 SAML2Utils.verifyRequestIssuer(
                          realm, idpEntityID, logoutReq.getIssuer(), requestId);
                     
                 List siList = logoutReq.getSessionIndex();
                 // TODO : handle list of session index
                 Iterator siIter = siList.iterator();
                 String sessionIndex = null;
                 if (siIter.hasNext()) {
                     sessionIndex = (String)siIter.next();
                 }
 
                 if (debug.messageEnabled()) {
                     debug.message("IDPLogoutUtil.processLogoutRequest: " +
                         "idpEntityID=" + idpEntityID + ", sessionIndex=" 
                         + sessionIndex);
                 }
 
                 if (sessionIndex == null) {
                     // this case won't happen
                     // according to the spec: SP has to send at least
                     // one sessionIndex, could be multiple (TODO: need
                     // to handle that above; but when IDP sends out 
                     // logout request, it could omit sessionIndex list,
                     // which means all sessions on SP side, so SP side
                     // needs to care about this case
                     debug.error("IDPLogoutUtil.processLogoutRequest: " +
                         "No session index in logout request");      
 
                     status = 
                         SAML2Utils.generateStatus(SAML2Constants.REQUESTER, "");
                     break;
                 }
 
                 IDPSession idpSession = (IDPSession)
                     IDPCache.idpSessionsByIndices.get(sessionIndex);
                 if (idpSession == null) {
                     debug.error("IDPLogoutUtil.processLogoutRequest: " +
                         "IDP no longer has this session index "+ sessionIndex);
                     status = SAML2Utils.generateStatus(
                             SAML2Constants.RESPONDER_ERROR, 
                             SAML2Utils.bundle.getString("invalidSessionIndex"));
                     break;
                 }
                 session = idpSession.getSession();
                 List list = (List)idpSession.getNameIDandSPpairs();
                 int n = list.size();
                 if (debug.messageEnabled()) {
                     debug.message("IDPLogoutUtil.processLogoutRequest: " +
                         "NameIDandSPpair for " + sessionIndex + " is " + list +
                         ", size=" + n);
                 }
 
                 
                 NameIDandSPpair pair = null;
                 // remove sending SP from the list
                 String spIssuer = logoutReq.getIssuer().getValue();
                 for (int i=0; i<n; i++) {
                     pair = (NameIDandSPpair) list.get(i);
                     if (pair.getSPEntityID().equals(spIssuer)) {
                         list.remove(i);
                         break;
                     }
                 }
                 List partners = idpSession.getSessionPartners();
                 boolean cleanUp = true; 
                 if (partners != null && !partners.isEmpty()) {
                     cleanUp = false; 
                 }    
 
                 n = list.size();
                 if (n == 0) {
                     // this is the case where there is no other
                     // session participant
                     status = destroyTokenAndGenerateStatus(
                         sessionIndex, idpSession.getSession(),
                         request, response, cleanUp);
                     if (cleanUp) {    
                        IDPCache.idpSessionsByIndices.remove(sessionIndex);
                        IDPCache.authnContextCache.remove(sessionIndex);
                     }   
                     break;
                 }
 
                 // there are other SPs to be logged out
                 if (binding.equals(SAML2Constants.HTTP_REDIRECT)) {
                     idpSession.setOriginatingLogoutRequestID(logoutReq.getID());
                     idpSession.setOriginatingLogoutSPEntityID(
                                           logoutReq.getIssuer().getValue());
                 }
 
                 int soapFailCount = 0;
                 for (int i = 0; i < n; i++) {
                     if (binding.equals(SAML2Constants.HTTP_REDIRECT)) {
                         pair = (NameIDandSPpair)list.remove(0);
                     } else {
                         // for SOAP binding
                         pair = (NameIDandSPpair) list.get(i);
                     }
             
                     String spEntityID = pair.getSPEntityID();
                     if (debug.messageEnabled()) {
                         debug.message("IDPLogoutUtil.processLogoutRequest: "
                              + "SP for " + sessionIndex + " is " + spEntityID);
                     }
                     SPSSODescriptorElement sp = null;
                     sp = SAML2Utils.getSAML2MetaManager().
                                      getSPSSODescriptor(realm, spEntityID);
                     List slosList = sp.getSingleLogoutService();
                     
                     // get IDP entity config in case of SOAP,for basic auth info
                     SPSSOConfigElement spConfig = null;
                     if (binding.equals(SAML2Constants.SOAP)) {
                         spConfig = SAML2Utils.
                             getSAML2MetaManager().getSPSSOConfig(
                                 realm, spEntityID);
                     }
                     String uri = request.getRequestURI();
                     String metaAlias = SAML2MetaUtils.getMetaAliasByUri(uri);
                     HashMap paramsMap = new HashMap();
                     paramsMap.put(SAML2Constants.ROLE, SAML2Constants.IDP_ROLE);
                     StringBuffer requestID = null; 
                     try {
                         requestID = LogoutUtil.doLogout(metaAlias,
                             spEntityID, slosList, null, binding, relayState,
                             sessionIndex, pair.getNameID(), response,
                             paramsMap, spConfig);
                     } catch (SAML2Exception ex) {
                         if (binding.equals(SAML2Constants.SOAP)) {
                             debug.error(
                                 "IDPSingleLogout.initiateLogoutRequest:" , ex);
                             soapFailCount++;
                             continue;
                         } else {
                             throw ex;
                         }
                     }
 
                     if (binding.equals(SAML2Constants.HTTP_REDIRECT)) {
                         String requestIDStr = requestID.toString();
                         if (requestIDStr != null 
                                         && requestIDStr.length() != 0) {
                             idpSession.setPendingLogoutRequestID(requestIDStr);
                         }
                         return null;
                     }
 
                 }
 
                 if (soapFailCount == n) {
                     throw new SAML2Exception(
                         SAML2Utils.bundle.getString("sloFailed"));
                 } else if (soapFailCount > 0) {
                     throw new SAML2Exception(
                         SAML2Utils.bundle.getString("partialLogout"));
                 }
                 // binding is SOAP, generate logout response 
                 // and send to initiating SP
                 status = destroyTokenAndGenerateStatus(
                     sessionIndex, idpSession.getSession(),
                     request, response, true);
                 if (cleanUp) {    
                     IDPCache.idpSessionsByIndices.remove(sessionIndex);
                     IDPCache.authnContextCache.remove(sessionIndex);
                 }    
             } while (false);
             
         } catch (SessionException ssoe) {
             debug.error("IDPLogoutUtil : unable to get meta for ", ssoe);
             status = SAML2Utils.generateStatus(idpEntityID, ssoe.toString()); 
         }
         // process multi-federation protocol
         boolean isMultiProtocol = false;
         try {
             SessionProvider provider = SessionManager.getProvider(); 
             if ((session != null) && (provider.isValid(session)) &&
                 MultiProtocolUtils.isMultipleProtocolSession(session,
                 SingleLogoutManager.SAML2)) {
                 isMultiProtocol = true;
             }
         } catch (SessionException ex) {
             //ignore
         }
         LogoutResponse logRes = LogoutUtil.generateResponse(status, 
             logoutReq.getID(), SAML2Utils.createIssuer(idpEntityID), realm, 
             SAML2Constants.IDP_ROLE, spEntity);
         if (!isMultiProtocol) {
             return logRes;
         } else {
             try {
                 Set set = new HashSet();
                 set.add(session);
                 String sessUser = 
                     SessionManager.getProvider().getPrincipalName(session);
                 boolean isSOAPInitiated = binding.equals(SAML2Constants.SOAP) 
                     ? true : false;
                 logRes.setDestination(getSingleLogoutLocation(spEntity, realm));
                 debug.message("IDPSingleLogout.processLogReq : call MP");
                 int retStat = SingleLogoutManager.getInstance().
                     doIDPSingleLogout(set, sessUser, request, response, 
                     isSOAPInitiated, false, SingleLogoutManager.SAML2, 
                     realm, idpEntityID, spEntity, relayState, 
                     logoutReq.toXMLString(true, true),
                     logRes.toXMLString(true, true),
                     SingleLogoutManager.LOGOUT_SUCCEEDED_STATUS);
                 if (retStat != SingleLogoutManager.LOGOUT_REDIRECTED_STATUS) {
                     logRes = updateLogoutResponse(logRes, retStat);
                     return logRes;
                 } else {
                     return null;
                 }
             } catch (SessionException ex) {
                 debug.error("IDPSingleLogout.ProcessLogoutRequest: SP " +
                     "initiated SOAP logout", ex);
                 throw new SAML2Exception(ex.getMessage());
             } catch (Exception ex) {
                 debug.error("IDPSingleLogout.ProcessLogoutRequest: SP " +
                     "initiated SOAP logout (MP)", ex);
                 throw new SAML2Exception(ex.getMessage());
             }
         }
     }
     
     private static LogoutResponse updateLogoutResponse(LogoutResponse logRes,
         int retStat) throws SAML2Exception {
         if (debug.messageEnabled()) {
             debug.message("IDPSingleLogout.updateLogoutResponse: response=" +
                 logRes.toXMLString() + "\nstatus = " + retStat);
         }
         if (retStat == SingleLogoutManager.LOGOUT_SUCCEEDED_STATUS) {
             return logRes;
         } else {
             StatusCode code = logRes.getStatus().getStatusCode();
             if (code.getValue().equals(SAML2Constants.SUCCESS)) {
                 code.setValue(SAML2Constants.RESPONDER);
             }
             return logRes;
         }
     }
 
     /**
      * Destroys the Single SignOn token and generates 
      * the <code>Status</code>.
      *
      * @param sessionIndex IDP's session index.
      * @param session the Single Sign On session.
      *  
      * @return <code>Status</code>.
      * @throws SAML2Exception if error generating
      *          <code>Status</code>.
      */
     private static Status destroyTokenAndGenerateStatus(
         String sessionIndex,
         Object session,
         HttpServletRequest request,
         HttpServletResponse response, 
         boolean cleanUp) throws SAML2Exception {
 
         Status status = null;
         if (session != null) {
             try {
                 if (cleanUp) {
                     MultiProtocolUtils.invalidateSession(session, request,
                         response, SingleLogoutManager.SAML2);
                 }
                         
                 if (debug.messageEnabled()) {
                     debug.message("IDPLogoutUtil.destroyTAGR: "
                         + "Local session destroyed.");
                 }
                 status = SAML2Utils.generateStatus(SAML2Constants.SUCCESS, "");
             } catch (Exception e) {
                 debug.error("IDPLogoutUtil.destroyTAGR: ", e);
                 status = SAML2Utils.generateStatus(SAML2Constants.RESPONDER,"");
             }
         } else {
             if (debug.messageEnabled()) {
                 debug.message("IDPLogoutUtil.destroyTAGR: " + 
                     "No such session with index " + sessionIndex + " exists.");
             }
             // TODO : should this be success?
             status = SAML2Utils.generateStatus(SAML2Constants.SUCCESS, "");
         }
         return status;
     }       
 
     private static void destroyAllTokenForUser(
         String  userToLogout, HttpServletRequest request,
         HttpServletResponse response) {
         
         Enumeration keys = IDPCache.idpSessionsByIndices.keys();
         String idpSessionIndex = null;
         IDPSession idpSession = null;
         Object idpToken = null;
         if (debug.messageEnabled()) {
                 debug.message("IDPLogoutUtil.destroyAllTokenForUser: " + 
                     "User to logoutAll : " + userToLogout);
         }
         
         while (keys.hasMoreElements()) {
             idpSessionIndex = (String)keys.nextElement();   
             idpSession = (IDPSession)IDPCache.
                 idpSessionsByIndices.get(idpSessionIndex);
             if (idpSession != null) {
                 idpToken = idpSession.getSession();
                 if (idpToken != null) {
                     try {
                         String userID = sessionProvider.getPrincipalName(idpToken);
                         if (userToLogout.equalsIgnoreCase(userID)) {
                             MultiProtocolUtils.invalidateSession(idpToken, 
                                 request, response, SingleLogoutManager.SAML2);
                             IDPCache.
                                 idpSessionsByIndices.remove(idpSessionIndex);
                             IDPCache.authnContextCache.remove(idpSessionIndex);
                         }
                     } catch (SessionException e) {
                         debug.error(
                             SAML2Utils.bundle.getString("invalidSSOToken"), e);
                         continue;
                     }
                 }
             } else {
                 IDPCache.idpSessionsByIndices.remove(idpSessionIndex);
                 IDPCache.authnContextCache.remove(idpSessionIndex);
             }       
         }
     }
 }

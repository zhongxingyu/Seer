 package org.jboss.seam.xwidgets.action;
 
 import java.util.StringTokenizer;
 
 import javax.enterprise.context.RequestScoped;
 import javax.enterprise.event.Event;
 import javax.enterprise.inject.Instance;
 import javax.inject.Inject;
 
 import org.jboss.seam.remoting.annotations.WebRemote;
 import org.jboss.seam.security.Credentials;
 import org.jboss.seam.security.Identity;
 import org.jboss.seam.security.events.DeferredAuthenticationEvent;
 import org.jboss.seam.security.external.api.ResponseHolder;
 import org.jboss.seam.security.external.dialogues.DialogueFilter;
 import org.jboss.seam.security.external.dialogues.api.DialogueManager;
 import org.jboss.seam.security.external.openid.OpenIdRpAuthenticationService;
 import org.jboss.seam.security.external.openid.api.OpenIdPrincipal;
 import org.jboss.seam.security.external.openid.providers.CustomOpenIdProvider;
 import org.jboss.seam.security.external.spi.OpenIdRelyingPartySpi;
 import org.jboss.seam.transaction.Transactional;
 import org.jboss.seam.xwidgets.dto.AuthResult;
 import org.jboss.seam.xwidgets.service.OpenIdAjaxAuthenticator;
 import org.openid4java.message.MessageException;
 import org.openid4java.message.Parameter;
 import org.openid4java.message.ParameterList;
 import org.picketlink.idm.impl.api.PasswordCredential;
 
 /**
  * 
  * @author Shane Bryzak
  *
  */
 public @RequestScoped class IdentityAction implements OpenIdRelyingPartySpi {
 
     @Inject Credentials credentials;
     @Inject Identity identity;
     
     @Inject Instance<OpenIdAjaxAuthenticator> openIdAuthenticator;
     @Inject Instance<CustomOpenIdProvider> customProvider;
     @Inject Instance<OpenIdRpAuthenticationService> authService;
     
     @Inject Instance<AuthResult> authResult;
     
     @Inject
     Event<DeferredAuthenticationEvent> deferredAuthentication;    
     
     @Inject
     private DialogueManager manager;
     
     /**
      * Returns a URL that the Identity widget can use to perform OpenID authentication
      * 
      * @param provider
      * @param openIdUrl
      * @return
      */
     @WebRemote
     public String openIdLogin(String provider, String openIdUrl) {
         OpenIdAjaxAuthenticator authenticator = openIdAuthenticator.get();
         
         if (provider != null) {
             authenticator.setProviderCode(provider);
         }
         
         if (openIdUrl != null) {
             CustomOpenIdProvider custom = customProvider.get();
             custom.setUrl(openIdUrl);
             authenticator.setProviderCode(custom.getCode());
         }
         
         identity.setAuthenticatorClass(OpenIdAjaxAuthenticator.class);
         identity.login();
         
         return authenticator.getRedirectUrl();        
     }
     
     @WebRemote @Transactional
     public AuthResult processOpenIdResponse(String params) throws MessageException {
         
         AuthResult result = authResult.get();        
         
         ParameterList paramList = new ParameterList();
 
         StringTokenizer tokenizer = new StringTokenizer(params, "&");
         while (tokenizer.hasMoreTokens())
         {
             String param = tokenizer.nextToken();
             int posEqual = param.indexOf('=');
 
             if (posEqual == -1)
                 throw new MessageException("Invalid query parameter, = missing: " + param);
 
             String key   = param.substring(0, posEqual);
             String value = param.substring(posEqual + 1);
 
             paramList.set(new Parameter(key, value));
         }        
         
         String dialogueId = paramList.getParameterValue(DialogueFilter.DIALOGUE_ID_PARAM);
         if (dialogueId != null) {
             if (!manager.isExistingDialogue(dialogueId)) {
                 // TODO return an error code here
                 
                // Something weird happened, detach the current dialogue
                if (manager.isAttached()) {
                    manager.detachDialogue();
                }
                 
                 //((HttpServletResponse) response).sendError(HttpServletResponse.SC_BAD_REQUEST, "dialogue " + dialogueId + " does not exist");
                 result.setSuccess(false);
                 return result;
             }
             manager.attachDialogue(dialogueId);
         }        
         
         authService.get().processIncomingMessage(paramList, params, null);
         
         if (manager.isAttached()) {
             manager.detachDialogue();
         }
         
         return result;
     }
     
     public void loginSucceeded(OpenIdPrincipal principal, ResponseHolder responseHolder) {
         openIdAuthenticator.get().success(principal);
         deferredAuthentication.fire(new DeferredAuthenticationEvent(true));
         authResult.get().setSuccess(true);
     }
 
     public void loginFailed(String message, ResponseHolder responseHolder) {
         deferredAuthentication.fire(new DeferredAuthenticationEvent(false));
            // responseHolder.getResponse().sendRedirect(servletContext.getContextPath() + "/AuthenticationFailed.jsf");
         authResult.get().setSuccess(false);
     }    
            
     @WebRemote
     public AuthResult login(String username, String password) {
         identity.setAuthenticatorClass(null);
         credentials.setUsername(username);
         credentials.setCredential(new PasswordCredential(password));
         
         AuthResult result = authResult.get();
         
         result.setSuccess(Identity.RESPONSE_LOGIN_SUCCESS.equals(identity.login()));
         
         return result;
     }
     
     @WebRemote
     public void logout() {
         identity.logout();        
     }
 }

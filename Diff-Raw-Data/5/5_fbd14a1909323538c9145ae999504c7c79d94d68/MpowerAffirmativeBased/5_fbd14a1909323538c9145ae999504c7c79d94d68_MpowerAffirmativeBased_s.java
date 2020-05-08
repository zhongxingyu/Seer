 package com.mpower.security;
 
 import java.util.Map;
 
 import org.springframework.security.AccessDeniedException;
 import org.springframework.security.Authentication;
 import org.springframework.security.ConfigAttributeDefinition;
 import org.springframework.security.intercept.web.FilterInvocation;
 import org.springframework.security.vote.AffirmativeBased;
 
 import com.mpower.type.AccessType;
 
 /**
  * Simple concrete implementation of {@link org.springframework.security.AccessDecisionManager} that grants access if any <code>AccessDecisionVoter</code> returns an affirmative response.
  */
 public class MpowerAffirmativeBased extends AffirmativeBased {
     /*
      * (non-Javadoc)
      * @see org.springframework.security.vote.AffirmativeBased#decide(org.springframework .security.Authentication, java.lang.Object, org.springframework.security.ConfigAttributeDefinition)
      */
     @Override
     public void decide(Authentication authentication, Object object, ConfigAttributeDefinition config) throws AccessDeniedException {
         int deny = 0;
 
         if (authentication instanceof MpowerAuthenticationToken) {
             if (object instanceof FilterInvocation) {
                 String requestUrl = ((FilterInvocation) object).getRequestUrl();
                 Map<String, AccessType> pageAccess = ((MpowerAuthenticationToken) authentication).getPageAccess();
                AccessType accessType = pageAccess.get(requestUrl);
                 if (accessType != null && AccessType.DENIED.equals(accessType)) {
                     deny++;
                 }
             }
         }
 
         if (deny > 0) {
             throw new AccessDeniedException(messages.getMessage("AbstractAccessDecisionManager.accessDenied", "Access is denied"));
         }
 
         super.decide(authentication, object, config);
     }
 }

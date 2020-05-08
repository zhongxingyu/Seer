 package net.link.safeonline.auth.ws.json;
 
 import java.io.Serializable;
 import java.util.*;
 import net.link.safeonline.auth.ws.soap.AuthenticationStep;
 import org.jetbrains.annotations.Nullable;
 
 
 /**
  * <h2>{@link WSAuthentication}<br> <sub>[in short] (TODO).</sub></h2>
  * <p/>
  * <p> <i>12 01, 2010</i> </p>
  *
  * @author lhunath
  */
 public interface WSAuthentication extends Serializable {
 
     String register(String applicationName, String deviceName, Map<String, String> deviceCredentials, Locale language)
             throws AuthenticationOperationFailedException;
 
     String authenticate(String applicationName, String deviceName, Map<String, String> deviceCredentials, Locale language)
             throws AuthenticationOperationFailedException;
 
     @Nullable
     String requestGlobalUsageAgreement(Locale language)
             throws AuthenticationOperationFailedException;
 
     void confirmGlobalUsageAgreement()
             throws AuthenticationOperationFailedException;
 
     @Nullable
     String requestApplicationUsageAgreement(Locale language)
             throws AuthenticationOperationFailedException;
 
     void confirmApplicationUsageAgreement()
             throws AuthenticationOperationFailedException;
 
    Map<String, List<AttributeType>> requestIdentity(Locale language)
             throws AuthenticationOperationFailedException;
 
     void confirmAllIdentity(Map<String, List<List<String>>> attributes)
             throws AuthenticationOperationFailedException;
 
     void confirmIdentity(Set<String> confirmedAttributeNames, Set<String> rejectedAttributeNames,
                          Map<String, List<List<String>>> attributes)
             throws AuthenticationOperationFailedException;
 
     List<AuthenticationStep> getNextSteps()
             throws AuthenticationOperationFailedException;
 
     byte[] commit()
             throws AuthenticationOperationFailedException;
 }

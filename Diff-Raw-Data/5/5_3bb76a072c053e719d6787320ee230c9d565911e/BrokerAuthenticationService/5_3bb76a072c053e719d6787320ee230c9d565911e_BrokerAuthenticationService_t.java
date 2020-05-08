 package pt.com.broker.security.authentication;
 
 import java.util.concurrent.TimeUnit;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.core.BrokerExecutor;
 import pt.com.common.security.ClientAuthInfo;
 import pt.com.common.security.authentication.AuthenticationCredentialsProvider;
 import pt.com.common.security.authentication.AuthenticationCredentialsProviderFactory;
 import pt.com.common.security.authentication.SapoSTSAuthenticationParamsProvider;
 import pt.com.common.security.authentication.SapoSTSAuthenticationParamsProvider.Parameters;
 import pt.com.gcs.conf.GcsInfo;
 
 public class BrokerAuthenticationService
 {
 	private static final Logger log = LoggerFactory.getLogger(BrokerAuthenticationService.class);
 	
 	private static ClientAuthInfo agentAuthenticationInfo; 
 	
 	public static void start()
 	{
 		String stsLocation = GcsInfo.getSTSLocation();
 		String stsUsername = GcsInfo.getSTSUsername();
 		String stsPassword = GcsInfo.getSTSPassword();		
 		
 		Parameters parameters = new SapoSTSAuthenticationParamsProvider.Parameters(stsLocation, stsUsername, stsPassword);
 		SapoSTSAuthenticationParamsProvider.setSTSParameters(parameters);
 		
 		ClientAuthInfo agentAuthInfo = new ClientAuthInfo(stsUsername, stsPassword);
 		
 		AuthenticationCredentialsProvider authProvider = AuthenticationCredentialsProviderFactory.getProvider("SapoSTS");
 		try
 		{
 			agentAuthenticationInfo = authProvider.getCredentials(agentAuthInfo);
 			if(agentAuthenticationInfo == null)
 			{
				log.warn("Failed to get credentials");
 				return;
 			}
 		}
 		catch (Exception e)
 		{
			log.warn("Failed to get credentials for Service BUS. Reason: '{}'", e.getMessage());
 			return;
 		}
 		
 		Runnable renew = new Runnable() {
 			public void run()
 			{
 				renewCredentials();
 			}
 		};
 		
 		log.info("STS Credentials obtained");
 		BrokerExecutor.scheduleAtFixedRate(renew, 110, 110, TimeUnit.MINUTES);
 	}
 
 	public static ClientAuthInfo getAgentAuthenticationInfo()
 	{
 		synchronized (BrokerAuthenticationService.class)
 		{
 			return agentAuthenticationInfo;
 		}
 	}
 
 	private static void renewCredentials()
 	{
 		ClientAuthInfo newAgentAuthenticationInfo = null;
 		try
 		{
 			newAgentAuthenticationInfo = AuthenticationCredentialsProviderFactory.getProvider("SapoSTS").getCredentials(agentAuthenticationInfo);
 			if(agentAuthenticationInfo == null)
 			{
 				log.warn("BrokerAuthenticationService - failed to renew credentials");
 				return;
 			}
 		}
 		catch (Exception e)
 		{
 			log.warn("BrokerAuthenticationService - failed to renew credentials", e);
 			return;
 		}
 		
 		synchronized (BrokerAuthenticationService.class)
 		{
 			agentAuthenticationInfo = newAgentAuthenticationInfo;
 		}
 		log.info("STS Credentials renewed");
 	}
 	
 }

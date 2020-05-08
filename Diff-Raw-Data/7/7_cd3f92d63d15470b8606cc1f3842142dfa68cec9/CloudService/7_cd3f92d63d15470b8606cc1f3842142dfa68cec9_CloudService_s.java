 package test.cli.cloudify.cloud;
 
 import java.io.IOException;
 
 /**
  * Every supported cloud must have a service that implements this interface in order
  * to be included in the test cycle.
  * @author elip
  *
  */
 public interface CloudService {
 	
 	/**
 	 * performs a bootstrap to a specific cloud.
 	 * see {@link AbstractCloudService} for generic implementation.
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	public void bootstrapCloud() throws IOException, InterruptedException;
 	
 
 	/**
 	 * tears down the specific cloud of all machines.
 	 * see {@link AbstractCloudService} for generic implementation.
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
	public void teardownCloud();
 	
 	/**
 	 * @return the rest url cloudify shell can connect to.
 	 */
 	public String getRestUrl();
 	
 	/**
 	 * 
 	 * @return the webui url browsers can connect to.
 	 */
 	public String getWebuiUrl();
 	
 	/**
 	 * @return the cloud provider name as specified in the jclouds documentation.
 	 */
 	public String getCloudName();
 	
 	/**
 	 * @return the user to be used in the cloud.groovy file.
 	 */
 	public String getUser();
 	
 	/**
 	 * @return the api key to be used in the cloud.groovy file.
 	 */
 	public String getApiKey();
 	
 	
 	/**
 	 * replaces the cloud dsl file with SGTest specific details.
 	 */
 	public void injectAuthenticationDetails() throws IOException;
 	
 }

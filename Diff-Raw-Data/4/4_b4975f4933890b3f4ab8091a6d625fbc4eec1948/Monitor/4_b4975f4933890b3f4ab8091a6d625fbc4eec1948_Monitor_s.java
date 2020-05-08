 package nl.tudelft.cloud_computing_project;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.amazonaws.AmazonServiceException;
 import com.amazonaws.auth.AWSCredentialsProvider;
 import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
 import com.amazonaws.services.ec2.AmazonEC2;
 import com.amazonaws.services.ec2.AmazonEC2Client;
 import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
 import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
 import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
 import com.amazonaws.services.ec2.model.DescribeInstancesResult;
 import com.amazonaws.services.ec2.model.Instance;
 import com.amazonaws.services.ec2.model.InstanceStatus;
 import com.amazonaws.services.ec2.model.Reservation;
 import com.amazonaws.services.ec2.model.Tag;
 
 
 public class Monitor{
 
 	private static Monitor instance;
 	private static Logger LOG = LoggerFactory.getLogger(Monitor.class);
 	private AmazonEC2 ec2 = AmazonEC2Initializer.getInstance();
 	private FaultManager faultManager;
 	private String masterId; 
 
 
 	private Monitor(){
 		faultManager = FaultManager.getInstance();
 		masterId = getInstanceID();
 		initEC2Instance();
 	}
 
 	public static Monitor getInstance(){
 		if(instance == null) { instance = new Monitor(); }
 		return instance;
 	}
 
 	public int getNumAvailableInstances() {
 
 		return getAvailableInstancesId().size();
 
 	}
 
 	public int getNumRunningNormalInstances() {
 
 		int normalInstanceNum = 0;
 
 		try {
 
 			Set<String> availableInstancesId = getAvailableInstancesId();
 			
 			DescribeInstancesResult describeInstancesRequest = ec2.describeInstances(new DescribeInstancesRequest().withInstanceIds(availableInstancesId));
             List<Reservation> reservations = describeInstancesRequest.getReservations();
 			
             for (Reservation reservation : reservations) {
 				for (Instance instance : reservation.getInstances()) {
 					for (Tag tag : instance.getTags()){
 						if(tag.getKey().equals("cloudocr") && tag.getValue().equals("worker")){
 							normalInstanceNum++;
 							break;
 						}	
 					}
 	
 				}
             }
 
 		} catch (AmazonServiceException ase) {
 			LOG.error("Caught Exception: " + ase.getMessage());
 			LOG.error("Reponse Status Code: " + ase.getStatusCode());
 			LOG.error("Error Code: " + ase.getErrorCode());
 			LOG.error("Request ID: " + ase.getRequestId());
 		}
 
 		return normalInstanceNum;
 
 	}
 
 	public Set<String> getAvailableInstancesId() {
 		Set<String> availableInstancesId = new TreeSet<String>();
 
 		try {
 			//Retrieve instances status
 			DescribeInstanceStatusResult describeInstanceResult = ec2.describeInstanceStatus(new DescribeInstanceStatusRequest());
 			List<InstanceStatus> state = describeInstanceResult.getInstanceStatuses();
 
 			for (InstanceStatus instanceStatusInfo : state){
 				//Retrieve machine state (running, stopped, booting)
 				String machineState = instanceStatusInfo.getInstanceState().getName();
 
 				if(machineState.equalsIgnoreCase("running")) {
 					//Retrieve status info
 					String instanceStatus = instanceStatusInfo.getInstanceStatus().getStatus();
 					String systemStatus = instanceStatusInfo.getSystemStatus().getStatus();
 
 					if(instanceStatus.equalsIgnoreCase("ok") && systemStatus.equalsIgnoreCase("ok")) {
						String instanceId = instanceStatusInfo.getInstanceId();
						if (!(instanceId.equals(this.masterId)))
							availableInstancesId.add(instanceStatusInfo.getInstanceId());
 					}	
 				}
 			}
 			
 			DescribeInstancesResult describeInstancesRequest = ec2.describeInstances(new DescribeInstancesRequest().withInstanceIds(availableInstancesId));
             List<Reservation> reservations = describeInstancesRequest.getReservations();
 			
             for (Reservation reservation : reservations) {
 				for (Instance instance : reservation.getInstances()) {
 					for (Tag tag : instance.getTags()){
 						if(tag.getKey().equals("cloudocr") && !(tag.getValue().equals("worker") || tag.getValue().equals("spotinstance"))) {
 							availableInstancesId.remove(instance.getInstanceId());
 							break;
 						}
 					}
 	
 				}
             }
 			
 		} catch (AmazonServiceException ase) {
 			LOG.error("Caught Exception: " + ase.getMessage());
 			LOG.error("Reponse Status Code: " + ase.getStatusCode());
 			LOG.error("Error Code: " + ase.getErrorCode());
 			LOG.error("Request ID: " + ase.getRequestId());
 		}
 
 		return availableInstancesId;
 
 	}
 
 	public void monitorSystem(){
 
 		try {
 
 			//Retrieve instances status
 			DescribeInstanceStatusResult describeInstanceResult = ec2.describeInstanceStatus(new DescribeInstanceStatusRequest());
 			List<InstanceStatus> state = describeInstanceResult.getInstanceStatuses();
 
 			for (InstanceStatus instanceStatusInfo : state){
 				//Retrieve machine state (running, stopped, booting)
 				String machineState = instanceStatusInfo.getInstanceState().getName();
 
 				if(machineState.equalsIgnoreCase("running")) {
 					//Retrieve status info
 					String instanceStatus = instanceStatusInfo.getInstanceStatus().getStatus();
 					String systemStatus = instanceStatusInfo.getSystemStatus().getStatus();
 
 					//Call Fault Manager to handle failure
 					if(!instanceStatus.equalsIgnoreCase("ok") || !systemStatus.equalsIgnoreCase("ok")) {
 
 						//Run Thread that deals with the failed machine
 						FaultManagerThread faultManagerThread = new FaultManagerThread(faultManager, instanceStatusInfo.getInstanceId());
 						faultManagerThread.run();
 					}	
 				}
 			}
 		} catch (AmazonServiceException ase) {
 			LOG.error("Caught Exception: " + ase.getMessage());
 			LOG.error("Reponse Status Code: " + ase.getStatusCode());
 			LOG.error("Error Code: " + ase.getErrorCode());
 			LOG.error("Request ID: " + ase.getRequestId());
 		}
 
 
 	}
 
 	/**
 	 * This method initializes the ec2 instance with the correct parameters.
 	 */
 	private void initEC2Instance() {
 		try {
 			//Init ec2 instance
 			AWSCredentialsProvider credentialsProvider = new ClasspathPropertiesFileCredentialsProvider();
 			ec2 = new AmazonEC2Client(credentialsProvider);
 			ec2.setEndpoint("ec2.eu-west-1.amazonaws.com");
 		}
 		catch (Exception e) {
 			LOG.error(e.getMessage());
 		}
 	}
 
 	private String getInstanceID() {
 		String url = "http://169.254.169.254/latest/meta-data/instance-id";
 		StringBuffer response = new StringBuffer();
 		URL obj;
 
 		try {
 			obj = new URL(url);
 
 			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 			con.setRequestMethod("GET");
 
 			BufferedReader in = new BufferedReader(
 					new InputStreamReader(con.getInputStream()));
 			String inputLine;
 
 
 			while ((inputLine = in.readLine()) != null) {
 				response.append(inputLine);
 			}
 			in.close();
 
 		} catch (Exception e) {
 			LOG.error("Error instantiating ProvvisioningPolicy class:\n" + e.getMessage());
 		}
 
 		return response.toString();
 	}
 
 	/**
 	 * This class is used to thread upon the discovery of a failed machine.
 	 */
 	private class FaultManagerThread extends Thread {
 		private FaultManager fm;
 		private String instanceId;
 
 		public FaultManagerThread (FaultManager fm, String instanceId) {
 			this.fm = fm;
 			this.instanceId = instanceId;
 		}
 
 		public void run(){
 			fm.WorkerFailure(instanceId);
 		}
 	}
 
 }

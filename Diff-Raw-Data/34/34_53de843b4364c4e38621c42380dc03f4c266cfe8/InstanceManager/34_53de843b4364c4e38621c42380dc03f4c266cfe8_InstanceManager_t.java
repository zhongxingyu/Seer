 package assignment1;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 
 import com.amazonaws.AmazonServiceException;
 import com.amazonaws.auth.AWSCredentials;
 
 public class InstanceManager {
   private final ArrayList<Employee> employees;
   private final Ec2OpWrapper ec2OpWrapper;
   private final HashMap<String, ArrayList<Integer>> policy;
   
   public InstanceManager(ArrayList<Employee> employees,
       HashMap<String, ArrayList<Integer>> policy,
       AWSCredentials credentials) {
     this.employees = employees;
     ec2OpWrapper = new Ec2OpWrapper(credentials);
     this.policy = policy;
   }
 
   // Initial creation of all instances on Day1 morning.
   public void initialize() {
     try {
       for(Employee employee: employees) {
         ec2OpWrapper.createKeyPair(employee.getKeyPairName());
     	  ec2OpWrapper.createSecurityGroup(employee.getGroup(),
           employee.getGroup(), policy);
    String elasticIp = ec2OpWrapper.createElasticIp();
        employee.setIp(elasticIp);
         String volumeId = ec2OpWrapper.createVolume();
         employee.addVolumeId(volumeId);
         
         String createdInstanceId = createInstance(employee.getAmiId(),
             employee.getKeyPairName(), employee.getGroup(), employee.getIp(),
             employee.getVolumeIds(), employee.getUsername());
         employee.setInstanceId(createdInstanceId);
       ec2OpWrapper.createS3Bucket(employee.getBucketName());
         employee.setActiveStat(true);
       }
       // Waiting...
       System.out.println("Wait another 2 mins before proceeding...");
       long start = System.currentTimeMillis();
       while ((System.currentTimeMillis() - start) < 3 * 60 * 1000) {
         ;
       }
     } catch (AmazonServiceException ase) {
       System.err.println("Caught Exception1: " + ase.getMessage());
       System.err.println("Reponse Status Code: " + ase.getStatusCode());
       System.err.println("Error Code: " + ase.getErrorCode());
       System.err.println("Request ID: " + ase.getRequestId());
     }
   }
 
   public void createMorningInstances() {
     try {
       for(Employee employee: employees) {
           String createdInstanceId = createInstance(employee.getAmiId(),
               employee.getKeyPairName(), employee.getGroup(), employee.getIp(),
               employee.getVolumeIds(), employee.getUsername());
           employee.setInstanceId(createdInstanceId);
           employee.setActiveStat(true);
           String newVolumeId ="";
           System.out.println("Restoring the volume from the snaphot stored in S3");
           for (String volumeId: employee.getVolumeIds()) {
        	 newVolumeId =  ec2OpWrapper.createVolumeFromSnapshot(employee.getSnapshotId(volumeId),"us-east-1a");
         	 System.out.println("Got new volume: "+newVolumeId+"for snapshot "+employee.getSnapshotId(volumeId));
           }
 
           
       }
     } catch (AmazonServiceException ase) {
       System.err.println("Caught Exception: " + ase.getMessage());
       System.err.println("Reponse Status Code: " + ase.getStatusCode());
       System.err.println("Error Code: " + ase.getErrorCode());
       System.err.println("Request ID: " + ase.getRequestId());
     }
   }
 
   public void afterWorkCleanup() {
     try {
       for(Employee employee: employees) {
         if (employee.isActive()) {
           System.out.println("Creating snapshot of volumes");
           this.snapshotVolumeOfEmployee(employee);
           String amiName = employee.getUsername() + "-" + new Random().nextInt();
           String amiId = snapshotAndTermInst(employee.getInstanceId(),
               employee.getVolumeIds(), employee.getIp(), amiName);
           employee.setAmiId(amiId);
           employee.setActiveStat(false);
         }
       }
     } catch (AmazonServiceException ase) {
       System.err.println("Caught Exception: " + ase.getMessage());
       System.err.println("Reponse Status Code: " + ase.getStatusCode());
       System.err.println("Error Code: " + ase.getErrorCode());
       System.err.println("Request ID: " + ase.getRequestId());
     }
   }
 
   public void snapshotVolumeOfEmployee(Employee employee){
 	  String snapshotId= "";
 	  for(String volumeId: employee.getVolumeIds()){
 		  snapshotId = ec2OpWrapper.createVolumeSnapShot(volumeId);
 		  employee.setSnapshotId(volumeId,snapshotId);
 		  System.out.println("Got snapshot: "+snapshotId+" for volume: "+ volumeId);
 	  }
   }
   public void examAndTermIdleInsts() {
     for(Employee employee: employees) {
       if (employee.isActive()) {
         if ((ec2OpWrapper.getCpuUsage(employee.getInstanceId(), 10)) < 10) {
         	
           System.out.println("Terminating idle instance " + 
               employee.getInstanceId() + " of " + employee.getUsername());
           System.out.println("Creating snapshot of volumes");
           this.snapshotVolumeOfEmployee(employee);
           String amiName = employee.getUsername() + "-" + new Random().nextInt();
           String amiId = snapshotAndTermInst(employee.getInstanceId(),
               employee.getVolumeIds(), employee.getIp(), amiName);
           employee.setAmiId(amiId);
           employee.setActiveStat(false);
         }
       }
     }
   }
   
 
   public String createInstance(String amiId, String keyPairName,
       String groupName, String ip, ArrayList<String> volumeIds, String tag) 
       throws AmazonServiceException {
     String createdInstanceId = ec2OpWrapper.createInstance(
         amiId, keyPairName, groupName);
     ec2OpWrapper.createInstanceTag(createdInstanceId, tag);
     ec2OpWrapper.associateElasticIp(createdInstanceId, ip);
     ec2OpWrapper.attachVolumes(createdInstanceId, volumeIds);
     return createdInstanceId;
   }
 
   public String snapshotAndTermInst(String instanceId,
       ArrayList<String> volumeIds, String ip, String amiName) 
       throws AmazonServiceException {
     ec2OpWrapper.detachVolumes(instanceId, volumeIds);
     ec2OpWrapper.dissociateElasticIp(ip);
     String amiId = ec2OpWrapper.snapshot(instanceId, amiName);
     ec2OpWrapper.terminateInstance(instanceId);
     return amiId;
   }
 }

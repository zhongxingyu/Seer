 package assignment1;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.PrintStream;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import java.util.TimeZone;
 
 import com.amazonaws.AmazonServiceException;
 import com.amazonaws.auth.AWSCredentials;
 import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
 import com.amazonaws.services.cloudwatch.model.Datapoint;
 import com.amazonaws.services.cloudwatch.model.Dimension;
 import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
 import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
 import com.amazonaws.services.ec2.AmazonEC2;
 import com.amazonaws.services.ec2.AmazonEC2Client;
 import com.amazonaws.services.ec2.model.AllocateAddressResult;
 import com.amazonaws.services.ec2.model.AssociateAddressRequest;
 import com.amazonaws.services.ec2.model.AttachVolumeRequest;
 import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
 import com.amazonaws.services.ec2.model.CreateImageRequest;
 import com.amazonaws.services.ec2.model.CreateImageResult;
 import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
 import com.amazonaws.services.ec2.model.CreateKeyPairResult;
 import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
 import com.amazonaws.services.ec2.model.CreateSnapshotRequest;
 import com.amazonaws.services.ec2.model.CreateSnapshotResult;
 import com.amazonaws.services.ec2.model.CreateTagsRequest;
 import com.amazonaws.services.ec2.model.CreateVolumeRequest;
 import com.amazonaws.services.ec2.model.CreateVolumeResult;
 import com.amazonaws.services.ec2.model.DescribeImagesRequest;
 import com.amazonaws.services.ec2.model.DescribeImagesResult;
 import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
 import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
 import com.amazonaws.services.ec2.model.DescribeSnapshotsRequest;
 import com.amazonaws.services.ec2.model.DescribeSnapshotsResult;
 import com.amazonaws.services.ec2.model.DetachVolumeRequest;
 import com.amazonaws.services.ec2.model.DisassociateAddressRequest;
 import com.amazonaws.services.ec2.model.Image;
 import com.amazonaws.services.ec2.model.Instance;
 import com.amazonaws.services.ec2.model.InstanceStatus;
 import com.amazonaws.services.ec2.model.IpPermission;
 import com.amazonaws.services.ec2.model.Placement;
 import com.amazonaws.services.ec2.model.RunInstancesRequest;
 import com.amazonaws.services.ec2.model.RunInstancesResult;
 import com.amazonaws.services.ec2.model.Snapshot;
 import com.amazonaws.services.ec2.model.StopInstancesRequest;
 import com.amazonaws.services.ec2.model.Tag;
 import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
 import com.amazonaws.services.s3.AmazonS3Client;
 import com.amazonaws.services.s3.model.PutObjectRequest;
 
 public class Ec2OpWrapper {
   private final AmazonEC2 ec2;
   private final AmazonCloudWatchClient cloudWatch;
   private final AmazonS3Client s3;
   
   public Ec2OpWrapper(AWSCredentials credentials) {
     ec2 = new AmazonEC2Client(credentials);
     cloudWatch = new AmazonCloudWatchClient(credentials);
     //cloudWatch.setEndpoint("https://monitoring.us-east-1.amazonaws.com");
     s3 = new AmazonS3Client(credentials);
   }
 
   public void createKeyPair(String keyPairName)
       throws AmazonServiceException {
     System.out.println("Creating key pair...");
     CreateKeyPairRequest kpRequest = new CreateKeyPairRequest(keyPairName);
     CreateKeyPairResult kpResult = ec2.createKeyPair(kpRequest);
     try {
       FileOutputStream out = new FileOutputStream(keyPairName + ".pem");
       PrintStream ps = new PrintStream(out);
       ps.print(kpResult.getKeyPair().getKeyMaterial());
       ps.close();
     } catch (IOException e) {
       System.err.println(e);
     }
   }
 
   public void createSecurityGroup(String groupName, String description,
       HashMap<String, ArrayList<Integer>> policy)
       throws AmazonServiceException {
     // Create the group.
     CreateSecurityGroupRequest sgRequest = 
         new CreateSecurityGroupRequest(groupName, "Custom security group");
     ec2.createSecurityGroup(sgRequest);
     // Setup firewall policy for the created group.
     String ipAddr = "0.0.0.0/0";
     ArrayList<String> ipRanges = new ArrayList<String>();
     ipRanges.add(ipAddr);
     ArrayList<IpPermission> ipPermissions = new ArrayList<IpPermission>();
     if (policy.containsKey("tcp")) {
       for (int port: policy.get("tcp")) {
         IpPermission ipPermission = new IpPermission();
         ipPermission.setIpProtocol("tcp");
         ipPermission.setFromPort(port);
         ipPermission.setToPort(port);
         ipPermission.setIpRanges(ipRanges);
         ipPermissions.add(ipPermission);
       }
     }
     if (policy.containsKey("udp")) {
       for (int port: policy.get("udp")) {
         IpPermission ipPermission = new IpPermission();
         ipPermission.setIpProtocol("udp");
         ipPermission.setFromPort(port);
         ipPermission.setToPort(port);
         ipPermission.setIpRanges(ipRanges);
         ipPermissions.add(ipPermission);
       }
     }
     AuthorizeSecurityGroupIngressRequest ingressRequest =
         new AuthorizeSecurityGroupIngressRequest(groupName, ipPermissions);
     ec2.authorizeSecurityGroupIngress(ingressRequest);
   }
 
   public String createInstance(String amiId, String keyPairName,
       String groupName) throws AmazonServiceException {
     int minInstanceCount = 1; // create 1 instance.
     int maxInstanceCount = 1;
     RunInstancesRequest rir = new RunInstancesRequest(amiId, minInstanceCount, maxInstanceCount);
     rir.setKeyName(keyPairName);
     ArrayList<String> securityGroups = new ArrayList<String>();
     securityGroups.add(groupName);
     rir.setSecurityGroups(securityGroups);
   Placement placement = new Placement("us-east-1a");
    rir.setPlacement(placement);
     RunInstancesResult result = ec2.runInstances(rir);
 
     // Get instanceId from the result.
     List<Instance> resultInstance = result.getReservation().getInstances();
     String createdInstanceId = null;
 
     for (Instance ins : resultInstance){
      createdInstanceId = ins.getInstanceId();
      System.out.println("New instance has been created: " + createdInstanceId);
      System.out.println("Instance is associated with key: " + ins.getKeyName());
     }
     
     // Wait for the created instance to be fully provisioned.
     System.out.println(
         "Waiting for the instance we just created to be fully " +
         "provisioned...");
     // Waiting...
     long start = System.currentTimeMillis();
     while ((System.currentTimeMillis() - start) < 3 * 60 * 1000) {
       ;
     }
     DescribeInstanceStatusRequest describeInstanceRequest =
         new DescribeInstanceStatusRequest().withInstanceIds(createdInstanceId);
     DescribeInstanceStatusResult describeInstanceResult =
         ec2.describeInstanceStatus(describeInstanceRequest);
     List<InstanceStatus> state = describeInstanceResult.getInstanceStatuses();
     while (state.size() < 1 ||
         !state.get(0).getInstanceState().getName().equals("running")) {
       try {
         Thread.sleep(5000);
       } catch (InterruptedException e) {
         e.printStackTrace();
       }
       describeInstanceResult = ec2.describeInstanceStatus(describeInstanceRequest);
       state = describeInstanceResult.getInstanceStatuses();
     }
     System.out.println("Instance has been fully provisoined");
     
     return createdInstanceId;
   }
   
   public void createInstanceTag(String createdInstanceId, String tag)
       throws AmazonServiceException {
     List<String> resources = new LinkedList<String>();
     List<Tag> tags = new LinkedList<Tag>();
     Tag nameTag = new Tag("Name", tag);
    
     resources.add(createdInstanceId);
     tags.add(nameTag);
    
     CreateTagsRequest ctr = new CreateTagsRequest(resources, tags);
     ec2.createTags(ctr);
   }
 
   public String createVolume() throws AmazonServiceException {
     // Create a volume.
     CreateVolumeRequest cvr = new CreateVolumeRequest();
   cvr.setAvailabilityZone("us-east-1a");
     cvr.setSize(1); // size = 1 gigabytes
     CreateVolumeResult volumeResult = ec2.createVolume(cvr);
     String createdVolumeId = volumeResult.getVolume().getVolumeId();
     return createdVolumeId;
   }
 
   public void attachVolumes(String instanceId, ArrayList<String> volumeIds)
       throws AmazonServiceException {
     // Attached device's naming starts from /dev/sdf.
     char c = 'f';
     for(String volumeId: volumeIds) {
       AttachVolumeRequest avr = new AttachVolumeRequest();
       avr.setVolumeId(volumeId);
       avr.setInstanceId(instanceId);
       avr.setDevice("/dev/sd" + c);
       ec2.attachVolume(avr);
       System.out.println("****Attached volume " + volumeId + " to " + instanceId + "****");
       c++;
     }
   }
 
   public void detachVolumes(String instanceId, ArrayList<String> volumeIds) {
     DetachVolumeRequest dvr = new DetachVolumeRequest();
     dvr.setInstanceId(instanceId);
     for(String volumeId: volumeIds) {
       dvr.setVolumeId(volumeId);
       ec2.detachVolume(dvr);
       System.out.println("Detach volume " + volumeId);
     }
   }
 
   public String createAmi(String instanceId, String username) {
     CreateImageRequest cir = new CreateImageRequest();
     cir.setInstanceId(instanceId);
     cir.setName(username + "-" + new Random().nextInt());
     CreateImageResult createImageResult = ec2.createImage(cir);
     String createdImageId = createImageResult.getImageId();
     //System.out.println("Ami created with id " + createdImageId);
     return createdImageId;
   }
 
   public String createElasticIp() {
     AllocateAddressResult elasticResult = ec2.allocateAddress();
     String elasticIp = elasticResult.getPublicIp();
     return elasticIp;
   }
 
   public void associateElasticIp(String instanceId, String elasticIp) {
     AssociateAddressRequest aar = new AssociateAddressRequest();
     aar.setInstanceId(instanceId);
     aar.setPublicIp(elasticIp);
     ec2.associateAddress(aar);
   }
 
   public void dissociateElasticIp(String elasticIp) {
     System.out.println("Disassociate IP " + elasticIp);
     DisassociateAddressRequest dar = new DisassociateAddressRequest();
     dar.setPublicIp(elasticIp);
     ec2.disassociateAddress(dar);
   }
 
   public String snapshot(String instanceId, String amiName) {
     System.out.println("Stop the Instance " + instanceId + " for snapshot...");
     List<String> instanceIds = new LinkedList<String>();
     instanceIds.add(instanceId);
     // Stop instance.
     StopInstancesRequest stopIR = new StopInstancesRequest(instanceIds);
     ec2.stopInstances(stopIR);
     // Wait for the instance to be fully stopped.
     DescribeInstanceStatusRequest describeInstanceRequest =
         new DescribeInstanceStatusRequest().withInstanceIds(instanceId);
     DescribeInstanceStatusResult describeInstanceResult =
         ec2.describeInstanceStatus(describeInstanceRequest);
     List<InstanceStatus> state = describeInstanceResult.getInstanceStatuses();
     
     // Waiting...
     long start = System.currentTimeMillis();
     while ((System.currentTimeMillis() - start) < 5 * 60 * 1000) {
       ;
     }
     
     /*
     while (state.size() < 1 ||
         !state.get(0).getInstanceState().getName().equals("stopped")) {
       
       try {
         Thread.sleep(10000);
       } catch (InterruptedException e) {
         e.printStackTrace();
       }
       describeInstanceResult = ec2.describeInstanceStatus(describeInstanceRequest);
       state = describeInstanceResult.getInstanceStatuses();
     }*/
     
     CreateImageRequest cir = new CreateImageRequest();
     cir.setInstanceId(instanceId);
     cir.setName(amiName);
     CreateImageResult createImageResult = ec2.createImage(cir);
     String createdImageId = createImageResult.getImageId();
     
     System.out.println("Waiting for snapshot to be fully created...");
     // Waiting...
     start = System.currentTimeMillis();
     while ((System.currentTimeMillis() - start) < 5 * 60 * 1000) {
       ;
     }
     System.out.println("Snapshot has been fully created!");
     return createdImageId;
   }
 
   public void terminateInstance(String instanceId) {
     List<String> instanceIds = new LinkedList<String>();
     instanceIds.add(instanceId);
     // Terminate instance.
     System.out.println("Terminate the instance " + instanceId + " ...");
     TerminateInstancesRequest tir = new TerminateInstancesRequest(instanceIds);
     ec2.terminateInstances(tir);
   }
   
   public double getCpuUsage(String instanceId, int intervalInMinute) {
     //create request message
     GetMetricStatisticsRequest statRequest = new GetMetricStatisticsRequest();
     
     //set up request message
     statRequest.setNamespace("AWS/EC2"); //namespace
     statRequest.setPeriod(60); //period of data
     ArrayList<String> stats = new ArrayList<String>();
     
     //Use one of these strings: Average, Maximum, Minimum, SampleCount, Sum 
     stats.add("Average");
     stats.add("Sum");
     statRequest.setStatistics(stats);
     
     //Use one of these strings: CPUUtilization, NetworkIn, NetworkOut, DiskReadBytes, DiskWriteBytes, DiskReadOperations  
     statRequest.setMetricName("CPUUtilization"); 
     
     // set time
     GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
     calendar.add(GregorianCalendar.SECOND, -1 * calendar.get(GregorianCalendar.SECOND)); // 1 second ago
     Date endTime = calendar.getTime();
     calendar.add(GregorianCalendar.MINUTE, -intervalInMinute); // 10 minutes ago
     Date startTime = calendar.getTime();
     statRequest.setStartTime(startTime);
     statRequest.setEndTime(endTime);
     
     //specify an instance
     ArrayList<Dimension> dimensions = new ArrayList<Dimension>();
     dimensions.add(new Dimension().withName("InstanceId").withValue(instanceId));
     statRequest.setDimensions(dimensions);
     
     //get statistics
     GetMetricStatisticsResult statResult = cloudWatch.getMetricStatistics(statRequest);
     
     while(statResult.getDatapoints().size() == 0) {
       System.out.println("Still waiting for CPU utilization metrics ... " +
       		"Please be patient...");
       System.out.println(statResult);
       try {
         Thread.sleep(30000);
       } catch (InterruptedException e) {
         e.printStackTrace();
       }
       statResult = cloudWatch.getMetricStatistics(statRequest);
     }
 
     List<Datapoint> dataList = statResult.getDatapoints();
     return dataList.get(0).getAverage();
   }
 
   public void createS3Bucket(String bucketName) {    
     s3.createBucket(bucketName);
   }
 
   public void createS3Files(ArrayList<String> fileNames, String bucketName) {
     for (String fileName: fileNames) {
       //set key
       String key = fileName;
       
       //set value
       String[] nameAndExtension = fileName.split(".");
       File file;
       try {
         file = File.createTempFile(nameAndExtension[0], "." + nameAndExtension[1]);
         file.deleteOnExit();
         Writer writer = new OutputStreamWriter(new FileOutputStream(file));
         writer.write("This is a sample sentence.\r\nYes!");
         writer.close();
         //put object - bucket, key, value(file)
         s3.putObject(new PutObjectRequest(bucketName, key, file));
       } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
       }
     }
   }
   
   public String createVolumeSnapShot(String volumeId){
 	  	CreateSnapshotResult snapRes
 	  	= ec2.createSnapshot(new CreateSnapshotRequest(volumeId, "Snapshot"));
 	  	Snapshot snap = snapRes.getSnapshot();
 	  	 System.out.println("Snapshot request sent.");
 	     System.out.println("Waiting for snapshot to be created");
 	 
 	     String snapState = snap.getState();
 	        	
 	     System.out.println("snapState is " + snapState);
 	     // Wait for the snapshot to be created
 	     
	     /*
 	     DescribeSnapshotsRequest describeSnapshotsRequest =
 	    	        new DescribeSnapshotsRequest().withSnapshotIds(snap.getSnapshotId());
 	     DescribeSnapshotsResult  describeSnapshotsResult =
 	    	        ec2.describeSnapshots(describeSnapshotsRequest);
 	    	    String state = describeSnapshotsResult.getSnapshots().get(0).getState();
 	    	    while (state.equals("pending"))
 	    	     {
 	    	        try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 	    	        System.out.print(".");
 	    	        state = describeSnapshotsResult.getSnapshots().get(0).getState();
 	    	     }
	    	      */
	      // Waiting...
	     /*
	      long start = System.currentTimeMillis();
	      while ((System.currentTimeMillis() - start) < 3 * 60 * 1000) {
	        ;
	      }*/
 	    	    
 	    	    System.out.println("Done.");
 	  	return snap.getSnapshotId();
 	 
   }
   
   public String createVolumeFromSnapshot(String snapshotId, String zone){
 	  CreateVolumeResult volRes
       = ec2.createVolume(new CreateVolumeRequest(snapshotId,zone));
 	  String newVolumeId = volRes.getVolume().getVolumeId();
 	  return newVolumeId;
   }
 }

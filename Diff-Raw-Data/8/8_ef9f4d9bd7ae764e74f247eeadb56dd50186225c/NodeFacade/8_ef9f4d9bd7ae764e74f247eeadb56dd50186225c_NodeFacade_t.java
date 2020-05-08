 package org.ourgrid.node;
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.concurrent.Executor;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.log4j.Logger;
 import org.hyperic.sigar.SigarException;
 import org.ourgrid.node.idleness.IdlenessChecker;
 import org.ourgrid.node.idleness.IdlenessListener;
 import org.ourgrid.node.idleness.LinuxDevInputIdlenessDetector;
 import org.ourgrid.node.model.InstanceRepository;
 import org.ourgrid.node.model.Resources;
 import org.ourgrid.node.model.VBR;
 import org.ourgrid.node.model.sensor.SensorCache;
 import org.ourgrid.node.model.sensor.SensorResource;
 import org.ourgrid.node.model.volume.Volume;
 import org.ourgrid.node.util.NetUtils;
 import org.ourgrid.node.util.NodeProperties;
 import org.ourgrid.node.util.OurVirtUtils;
 import org.ourgrid.node.util.ResourcesInfoGatherer;
 import org.ourgrid.node.util.Sensor;
 import org.ourgrid.node.util.VBRUtils;
 import org.ourgrid.node.util.VolumeUtils;
 
 import edu.ucsb.eucalyptus.InstanceType;
 import edu.ucsb.eucalyptus.NcAssignAddress;
 import edu.ucsb.eucalyptus.NcAssignAddressResponse;
 import edu.ucsb.eucalyptus.NcAssignAddressResponseType;
 import edu.ucsb.eucalyptus.NcAssignAddressType;
 import edu.ucsb.eucalyptus.NcAttachVolume;
 import edu.ucsb.eucalyptus.NcAttachVolumeResponse;
 import edu.ucsb.eucalyptus.NcAttachVolumeType;
 import edu.ucsb.eucalyptus.NcBundleInstance;
 import edu.ucsb.eucalyptus.NcBundleInstanceResponse;
 import edu.ucsb.eucalyptus.NcBundleInstanceResponseType;
 import edu.ucsb.eucalyptus.NcBundleInstanceType;
 import edu.ucsb.eucalyptus.NcDescribeInstances;
 import edu.ucsb.eucalyptus.NcDescribeInstancesResponse;
 import edu.ucsb.eucalyptus.NcDescribeInstancesResponseType;
 import edu.ucsb.eucalyptus.NcDescribeInstancesType;
 import edu.ucsb.eucalyptus.NcDescribeResource;
 import edu.ucsb.eucalyptus.NcDescribeResourceResponse;
 import edu.ucsb.eucalyptus.NcDescribeResourceResponseType;
 import edu.ucsb.eucalyptus.NcDescribeResourceType;
 import edu.ucsb.eucalyptus.NcDescribeSensors;
 import edu.ucsb.eucalyptus.NcDescribeSensorsResponse;
 import edu.ucsb.eucalyptus.NcDescribeSensorsResponseType;
 import edu.ucsb.eucalyptus.NcDescribeSensorsType;
 import edu.ucsb.eucalyptus.NcDetachVolume;
 import edu.ucsb.eucalyptus.NcDetachVolumeResponse;
 import edu.ucsb.eucalyptus.NcDetachVolumeType;
 import edu.ucsb.eucalyptus.NcPowerDown;
 import edu.ucsb.eucalyptus.NcPowerDownResponse;
 import edu.ucsb.eucalyptus.NcPowerDownResponseType;
 import edu.ucsb.eucalyptus.NcPowerDownType;
 import edu.ucsb.eucalyptus.NcRebootInstance;
 import edu.ucsb.eucalyptus.NcRebootInstanceResponse;
 import edu.ucsb.eucalyptus.NcRebootInstanceResponseType;
 import edu.ucsb.eucalyptus.NcRebootInstanceType;
 import edu.ucsb.eucalyptus.NcRunInstance;
 import edu.ucsb.eucalyptus.NcRunInstanceResponse;
 import edu.ucsb.eucalyptus.NcRunInstanceResponseType;
 import edu.ucsb.eucalyptus.NcRunInstanceType;
 import edu.ucsb.eucalyptus.NcStartNetwork;
 import edu.ucsb.eucalyptus.NcStartNetworkResponse;
 import edu.ucsb.eucalyptus.NcStartNetworkResponseType;
 import edu.ucsb.eucalyptus.NcStartNetworkType;
 import edu.ucsb.eucalyptus.NcTerminateInstance;
 import edu.ucsb.eucalyptus.NcTerminateInstanceResponse;
 import edu.ucsb.eucalyptus.NcTerminateInstanceResponseType;
 import edu.ucsb.eucalyptus.NcTerminateInstanceType;
 import edu.ucsb.eucalyptus.NetConfigType;
 import edu.ucsb.eucalyptus.SensorsResourceType;
 
 public class NodeFacade implements IdlenessListener {
 
 	private static final String SUCCESS_STATE = "0";
 	private static final String UNSUCCESS_STATE = "2";
 	private final static Logger LOGGER = Logger.getLogger(NodeFacade.class);
 	private static NodeFacade instance = null;
 	
 	private InstanceRepository instanceRepository = new InstanceRepository();
 	private Map<String, Volume> volumes = new HashMap<String, Volume>(); 
 	private ResourcesInfoGatherer resourcesGatherer;
 	private Properties properties;
 	private IdlenessChecker idlenessChecker;
 	private Executor threadPool = Executors.newFixedThreadPool(20);
 	private ScheduledExecutorService sensorExecutor = Executors.newScheduledThreadPool(1);
 	private Sensor sensor;
 
 	public NodeFacade(Properties properties, 
 			IdlenessChecker iChecker,
 			ResourcesInfoGatherer resIG,
 			InstanceRepository iRep) throws Exception {
 		
 		this(properties);
 		
 		if (resIG != null) { 
 			this.resourcesGatherer = resIG;
 		}
 		
 		if (iRep != null) {
 			this.instanceRepository = iRep;
 		}
 		
 		this.idlenessChecker = iChecker;
 	}
 	
 	public NodeFacade(Properties properties) {
 		try {
 			this.properties = properties;
 			this.resourcesGatherer = new ResourcesInfoGatherer(properties);
 			OurVirtUtils.setHypervisorEnvVars(properties);
 		} catch (SigarException e) {
 			LOGGER.error("Error while retrieving machine resources info.", e);
 			throw new RuntimeException(e);
 		}
 
 		LinuxDevInputIdlenessDetector idlenessDecector = 
 				new LinuxDevInputIdlenessDetector(properties);
 		idlenessDecector.addListener(this);
 		idlenessDecector.init();
 		this.idlenessChecker = idlenessDecector;
 		
 		String pollingIntervalStr = properties.getProperty(
 				NodeProperties.SENSOR_POLLING_INTERVAL);
 		
 		long pollingInterval = Long.valueOf(pollingIntervalStr) * 1000;
 		this.sensor = new Sensor(pollingInterval, instanceRepository);
 		sensorExecutor.schedule(sensor, pollingInterval, TimeUnit.MILLISECONDS);
 	}
 	
 	private NodeFacade() {
 		this(loadProperties());
 	}
 
 	private static Properties loadProperties() {
 		try {
 			Properties properties = new Properties();
 			properties.load(NodeFacade.class.getClassLoader().getResourceAsStream(
 					"../conf/euca.conf"));
 			return properties;
 		} catch (IOException e) {
 			LOGGER.error("Error while loading properties file.", e);
 			throw new RuntimeException(e);
 		}
 	}
 
 	public static NodeFacade getInstance() {
 		if (instance == null) {
 			instance = new NodeFacade();
 		}
 		return instance;
 	}
 	
 	
 	private void checkNodeControllerAvailable() {
 		if (!idlenessChecker.isIdle()) {
 			throw new IllegalStateException("The node controller is not available.");
 		}
 	}
 
 	private InstanceType[] getRunningInstances() {
 		return instanceRepository.getInstances().toArray(new InstanceType[]{});
 	}
 
 	public NcDescribeResourceResponse describeResource(
 			NcDescribeResource ncDescribeResource) {
 		
 		LOGGER.info("Describing Node Controller resources.");
 
 		checkNodeControllerAvailable();
 
 		NcDescribeResourceResponse response = new NcDescribeResourceResponse();
 		NcDescribeResourceResponseType rType = new NcDescribeResourceResponseType();
 		NcDescribeResourceType resourceRequest = ncDescribeResource.getNcDescribeResource();
 
 		Resources available;
 		
 		try {
 			available = resourcesGatherer.describeAvailable(instanceRepository);
 		} catch (Exception e) {
 			LOGGER.error("Error while retrieving machine resources info.", e);
 			throw new RuntimeException(e);
 		}
 
 		//Set standard output fields
 		rType.set_return(true);
 		rType.setCorrelationId(resourceRequest.getCorrelationId());
 		rType.setUserId(resourceRequest.getUserId());
 		
 		//Set operation-specific output fields
 		rType.setMemorySizeMax(resourcesGatherer.getTotalMem());
 		rType.setDiskSizeMax(resourcesGatherer.getTotalDiskSpace());
 		rType.setNumberOfCoresMax(resourcesGatherer.getTotalNumCores());
 
 		rType.setMemorySizeAvailable(available.getMem());
 		rType.setDiskSizeAvailable(available.getDisk());
 		rType.setNumberOfCoresAvailable(available.getCores());
 		
 		rType.setIqn(ResourcesInfoGatherer.ISCSI_IQN);
 		rType.setNodeStatus(ResourcesInfoGatherer.NODE_STATUS_OK);
 		rType.setPublicSubnets(ResourcesInfoGatherer.PUBLIC_SUBNETS);
 
 		response.setNcDescribeResourceResponse(rType);
 		return response;
 	}
 
 
 	public NcDescribeInstancesResponse describeInstances(
 			NcDescribeInstances ncDescribeInstances) {
 		
 		LOGGER.info("Describing Node Controller instances.");
 
 		checkNodeControllerAvailable();
 
 		NcDescribeInstancesResponse response = new NcDescribeInstancesResponse();
 		NcDescribeInstancesType describeInstanceRequest = ncDescribeInstances.getNcDescribeInstances();
 		NcDescribeInstancesResponseType iResponseType = new NcDescribeInstancesResponseType();
 
 		//Set standard output fields
 		iResponseType.set_return(true);
 		iResponseType.setCorrelationId(describeInstanceRequest.getCorrelationId());
 		iResponseType.setUserId(describeInstanceRequest.getUserId());
 		
 		//Set operation-specific output fields
 		iResponseType.setInstances(getRunningInstances());
 		
 		response.setNcDescribeInstancesResponse(iResponseType);
 		return response;
 	}
 
 	private void terminateInstances() {
 		for (InstanceType instance : instanceRepository.getInstances()) {
 			terminateInstance(instance.getInstanceId());
 		}
 	}
 
 	private void terminateInstance(String instanceId) {
 		try {
 			OurVirtUtils.terminateInstance(instanceId, properties);
 			//			instanceRepository.removeInstance(instanceId);
 			InstanceType instance = instanceRepository.getInstance(instanceId);
 			instance.setStateName(InstanceRepository.TEARDOWN_STATE);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public NcTerminateInstanceResponse terminateInstance(
 			NcTerminateInstance ncTerminateInstance) {
 		
 		checkNodeControllerAvailable();
 
 		NcTerminateInstanceResponse terminateInstanceResponse = new NcTerminateInstanceResponse();
 		NcTerminateInstanceResponseType terminateInstance = new NcTerminateInstanceResponseType();
 		NcTerminateInstanceType terminateRequest = ncTerminateInstance.getNcTerminateInstance();
 		
 		LOGGER.info("Terminating Instance [" + terminateRequest.getInstanceId() + "].");
 		
 		checkInstanceExists(terminateRequest.getInstanceId());
 		
 		terminateInstance(terminateRequest.getInstanceId());
 		
 		//Set standard output fields
 		terminateInstance.set_return(true);
 		terminateInstance.setCorrelationId(terminateRequest.getCorrelationId());
 		terminateInstance.setUserId(terminateRequest.getUserId());
 		
 		//Set operation-specific output fields
 		terminateInstance.setInstanceId(terminateRequest.getInstanceId());
 		
 		terminateInstance.setShutdownState(SUCCESS_STATE);
 		terminateInstance.setPreviousState(SUCCESS_STATE);
 
 		terminateInstanceResponse.setNcTerminateInstanceResponse(terminateInstance);
 		return terminateInstanceResponse;
 	}
 
 	private void checkInstanceExists(String instanceId) {
 		if (instanceRepository.getInstance(instanceId) == null) {
 			throw new IllegalArgumentException("Instance " + instanceId + " does not exist.");
 		}
 	}
 
 	private static String getXMLString(String str) {
 		return str == null ? "" : str;
 	}
 
 	public NcRunInstanceResponse runInstance(NcRunInstance ncRunInstance) {
 
 		checkNodeControllerAvailable();
 
 		NcRunInstanceResponse response = new NcRunInstanceResponse();
 		NcRunInstanceResponseType runInstanceResponse =  new NcRunInstanceResponseType();
 		InstanceType instance = new InstanceType();
 		NcRunInstanceType instanceRequest = ncRunInstance.getNcRunInstance();
 		
 		LOGGER.info("Running instance [" + instanceRequest.getInstanceId() + "] of type " + 
 				instanceRequest.getInstanceType().getName()  + " with image " + "[" + 
 				instanceRequest.getImageId() + "].");
 
 		//Build instance object		
 		instance.setUuid(instanceRequest.getUuid());
 		instance.setInstanceId(instanceRequest.getInstanceId());
 		instance.setReservationId(instanceRequest.getReservationId());
 		instance.setUserId(instanceRequest.getUserId());
 		instance.setOwnerId(instanceRequest.getOwnerId());
 		instance.setAccountId(instanceRequest.getAccountId());
 		instance.setKeyName(instanceRequest.getKeyName());
 		instance.setInstanceType(instanceRequest.getInstanceType());
 		instance.setNetParams(instanceRequest.getNetParams());
 		instance.setUserData(instanceRequest.getUserData());
 		instance.setLaunchIndex(instanceRequest.getLaunchIndex());
 		instance.setPlatform(instanceRequest.getPlatform());
 		instance.setGroupNames(instanceRequest.getGroupNames());
 		instance.setExpiryTime(instanceRequest.getExpiryTime());
 
 		instance.setImageId(getXMLString(instanceRequest.getImageId()));
 		instance.setKernelId(getXMLString(instanceRequest.getKernelId()));
 		instance.setRamdiskId(getXMLString(instanceRequest.getRamdiskId()));
 		
 		//TODO
 		instance.setStateName(InstanceRepository.PENDING_STATE);
 		instance.setBundleTaskStateName("none");
 		instance.setCreateImageStateName("");
 		instance.setLaunchTime(Calendar.getInstance());
 		instance.setBlkbytes(0);
 		instance.setNetbytes(0);
 
 		//Set standard output fields
 		runInstanceResponse.set_return(true);
 		runInstanceResponse.setCorrelationId(instanceRequest.getCorrelationId());
 		runInstanceResponse.setUserId(instanceRequest.getUserId());
 
 		//Set operation-specific output fields
 		runInstanceResponse.setInstance(instance);
 		
 		instanceRepository.addInstance(instance);
 		response.setNcRunInstanceResponse(runInstanceResponse);
 
 		threadPool.execute(createStartupRunnable(instanceRequest));
 		
 		return response;
 	}
 	//TODO To check if the VM was started: ping or telnet insted of doing SSH 
 	private Runnable createStartupRunnable(final NcRunInstanceType instanceRequest) {
 		return new Runnable() {
 			public void run() {
 				LOGGER.info("Lauching instance [" + 
 						instanceRequest.getInstanceId() + "] startup thread.");
 				
 				checkInstanceExists(instanceRequest.getInstanceId());
 				
 				InstanceType instance = instanceRepository.getInstance(
 						instanceRequest.getInstanceId());
 				VBR vbr = VBRUtils.syncBootRecords(instanceRequest, properties);
 				
 				try {
 					OurVirtUtils.runInstance(instanceRequest, vbr, properties);
 				} catch (Exception e) {
 					LOGGER.error("Failure in startup thread for instance [" + 
 							instanceRequest.getInstanceId() + "].", e);
 					
 					try {
 						OurVirtUtils.terminateInstance(
 								instanceRequest.getInstanceId(), properties);
 					} catch (Exception e1) {
 						LOGGER.error("Failure to terminate instance[" + 
 								instanceRequest.getInstanceId() + "].", e1);
 					}
 					
 					instance.setStateName(InstanceRepository.TEARDOWN_STATE);
 					throw new RuntimeException(e);
 				}
 				
 				instance.setStateName(InstanceRepository.EXTANT_STATE);
 				NetConfigType params = NetUtils.getParams(instanceRequest, 
 						properties);
 				instance.setNetParams(params);
 			}
 		};
 	}
 
 	public NcAssignAddressResponse assignAddress(NcAssignAddress ncAssignAddress) {
 
 		checkNodeControllerAvailable();
 
 		NcAssignAddressResponseType assignAddressResponse = new NcAssignAddressResponseType();
 		NcAssignAddressResponse response = new NcAssignAddressResponse();
 		NcAssignAddressType assignRequest = ncAssignAddress.getNcAssignAddress();
 				
 		LOGGER.info("Assigning address for instance [" + assignRequest.getInstanceId() + "].");
 		
 		checkInstanceExists(assignRequest.getInstanceId());
 		
 		//Set standard output fields
 		assignAddressResponse.set_return(true);
 		assignAddressResponse.setCorrelationId(assignRequest.getCorrelationId());
 		assignAddressResponse.setUserId(assignRequest.getUserId());
 		
 		//Set operation-specific output fields
 		assignAddressResponse.setStatusMessage(assignRequest.getStatusMessage());
 
 		try {
 			OurVirtUtils.assignAddress(assignRequest.getInstanceId(), 
 					assignRequest.getPublicIp());
 		} catch (Exception e) {
 			LOGGER.error("Failure while assinging address to instance [" + 
 					assignRequest.getInstanceId() + "].", e);
 			throw new RuntimeException(e);
 		}
 
 		response.setNcAssignAddressResponse(assignAddressResponse);
 		return response;
 	}
 
 	public NcStartNetworkResponse startNetwork(NcStartNetwork ncStartNetwork) {
 
 		checkNodeControllerAvailable();
 
 		//TODO(patricia) If we're dealing with MANAGED mode, start a bridge 
 		NcStartNetworkResponseType startNetworkResponse = new NcStartNetworkResponseType();
 		NcStartNetworkResponse response = new NcStartNetworkResponse();
 		NcStartNetworkType startNetRequest =  ncStartNetwork.getNcStartNetwork();
 
 		LOGGER.info("Starting machine network.");
 		
 		startNetworkResponse.set_return(true);
 		startNetworkResponse.setUserId(startNetRequest.getUserId());
 		startNetworkResponse.setCorrelationId(startNetRequest.getCorrelationId());
 		
 		startNetworkResponse.setStatusMessage("0");
 		startNetworkResponse.setNetworkStatus("SUCCESS");
 		//TODO
 		response.setNcStartNetworkResponse(startNetworkResponse);
 		return response;
 	}
 
 	@Override
 	public void changed(boolean isIdle) {
 		if (!isIdle) {
 			terminateInstances();
 		}
 	}
 
 	public NcRebootInstanceResponse rebootInstance(
 			NcRebootInstance ncRebootInstance) {
 		
 		checkNodeControllerAvailable();
 
 		NcRebootInstanceResponse rebootInstanceResponse = new NcRebootInstanceResponse();
 		NcRebootInstanceResponseType rebootInstance = new NcRebootInstanceResponseType();
 		NcRebootInstanceType rebootRequest = ncRebootInstance.getNcRebootInstance();
 		
 		LOGGER.info("Rebooting Instance [" + rebootRequest.getInstanceId() + "].");
 		
 		checkInstanceExists(rebootRequest.getInstanceId());
 		
 		//Set standard output fields
 		rebootInstance.set_return(true); 
 		rebootInstance.setUserId(rebootRequest.getUserId());
 		rebootInstance.setCorrelationId(rebootRequest.getCorrelationId());
 
 		//Set operation-specific output fields
 		rebootInstance.setStatus(true);
 		
 		rebootInstanceResponse.setNcRebootInstanceResponse(rebootInstance);
 
 		threadPool.execute(createRebootRunnable(rebootRequest));
 		
 		return rebootInstanceResponse;
 	}
 
 	private Runnable createRebootRunnable(final NcRebootInstanceType rebootRequest) {
 		return new Runnable() {
 			public void run() {
 				try {
 					InstanceType instance = instanceRepository.
 							getInstance(rebootRequest.getInstanceId());
 					OurVirtUtils.rebootInstance(instance.getInstanceId(), 
 							instance.getImageId(), properties);
 				} catch (Exception e) {
 					throw new RuntimeException(e);
 				}
 			}
 		};
 	}
 
 	public NcBundleInstanceResponse bundleInstance(
 			NcBundleInstance ncBundleInstance) {
 		checkNodeControllerAvailable();
 
 		NcBundleInstanceResponse bundleInstanceResponse = new NcBundleInstanceResponse();
 		NcBundleInstanceResponseType bundleInstance = new NcBundleInstanceResponseType();
 		NcBundleInstanceType bundleRequest = ncBundleInstance.getNcBundleInstance();
 		
 		LOGGER.info("Bundling Instance [" + bundleRequest.getInstanceId() + "].");
 		
 		
 		
 		bundleInstance.setUserId(bundleRequest.getUserId());
 		//TODO
 		bundleInstance.set_return(true);
 		
 		bundleInstanceResponse.setNcBundleInstanceResponse(bundleInstance);
 		
 		return bundleInstanceResponse;
 	}
 
 	public NcPowerDownResponse powerDown(NcPowerDown ncPowerDown) {
 		
 		checkNodeControllerAvailable();
 		
 		NcPowerDownResponse powerDownResponse = new NcPowerDownResponse();
 		NcPowerDownResponseType powerDown = new NcPowerDownResponseType();
 		NcPowerDownType powerDownRequest = ncPowerDown.getNcPowerDown();
 		
 		LOGGER.info("Powering down Node Controller.");
 		
 		if (!resourcesGatherer.getOSType().equals("Linux")) {
 			LOGGER.warn("Power Down operation cannot be executed " +
 					"because OS Type is not Linux");
 			powerDown.setStatusMessage(UNSUCCESS_STATE);
 		} else {
 			try {
 				ProcessBuilder powerDownPB = new ProcessBuilder("sudo", 
 						"/usr/sbin/powernap-now");
 				powerDownPB.start();
 				//Best-effort approach: Does not check for command successful run 
 			} catch(Exception e) {}
 			powerDown.setStatusMessage(SUCCESS_STATE);
 		}
 		
 		//Set standard output fields
 		powerDown.set_return(true);
 		powerDown.setUserId(powerDownRequest.getUserId());
 		powerDown.setCorrelationId(powerDownRequest.getCorrelationId());
 		
 		powerDownResponse.setNcPowerDownResponse(powerDown);
 		
 		return powerDownResponse;
 	}
 
 	public NcDescribeSensorsResponse describeSensors(
 			NcDescribeSensors ncDescribeSensors) {
 		
 		checkNodeControllerAvailable();
 		
 		NcDescribeSensorsResponse describeSensorsResponse = new NcDescribeSensorsResponse();
 		NcDescribeSensorsResponseType describeSensors = new NcDescribeSensorsResponseType();
 		NcDescribeSensorsType describeSensorsRequest = ncDescribeSensors.getNcDescribeSensors();
 		
 		for (String instanceId : describeSensorsRequest.getInstanceIds()) {
 			checkInstanceExists(instanceId);
 		}
 		
 		if (!isArrayEmpty(describeSensorsRequest.getSensorIds())) {
 			throw new IllegalArgumentException("No support for sensorIds[]");
 		}
 		
 		describeSensors.set_return(true);
 		describeSensors.setUserId(describeSensorsRequest.getUserId());
 		describeSensors.setCorrelationId(describeSensorsRequest.getCorrelationId());
 		
 		SensorCache cache = sensor.getCache();
 		List<SensorResource> resources = cache.getSensorResources();
 		SensorsResourceType[] sensorResources = new SensorsResourceType[resources.size()];
 		for (int i = 0; i < sensorResources.length; i++) {
 			sensorResources[i] = resources.get(i);
 		}
 		describeSensors.setSensorsResources(sensorResources);
 		
 		describeSensorsResponse.setNcDescribeSensorsResponse(describeSensors);
 		
 		return describeSensorsResponse;
 	}
 	
 	private boolean isArrayEmpty(String[] array) {
 		for (String string : array) {
 			if (string != null) {
 				return false;
 			}
 		}
 		
 		return true;
 	}
 
 	public NcAttachVolumeResponse attachVolume(NcAttachVolume ncAttachVolume) {
 		NcAttachVolumeType attachVolumeRequest = ncAttachVolume.getNcAttachVolume();
 		String instanceId = attachVolumeRequest.getInstanceId();
 		String volumeId = attachVolumeRequest.getVolumeId();
 		String attachmentToken = attachVolumeRequest.getRemoteDev();
 		
 		Volume volume = new Volume();
 		volume.setId(volumeId);
 		volume.setState(Volume.State.ATTACHING);
 		volumes.put(volumeId, volume);
 		try {
 			VolumeUtils.connectEBSVolume(instanceId, 
 					attachmentToken, properties);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		
 		return null;
 	}
 
 	public NcDetachVolumeResponse detachVolume(NcDetachVolume ncDetachVolume) {
 		NcDetachVolumeType detachVolumeRequest = ncDetachVolume.getNcDetachVolume();
		String instanceId = detachVolumeRequest.getInstanceId();
 		String attachmentToken = detachVolumeRequest.getRemoteDev();
		try {
			VolumeUtils.disconnectEBSVolume(instanceId, 
					attachmentToken, properties);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
 		
 		return null;
 	}
 }

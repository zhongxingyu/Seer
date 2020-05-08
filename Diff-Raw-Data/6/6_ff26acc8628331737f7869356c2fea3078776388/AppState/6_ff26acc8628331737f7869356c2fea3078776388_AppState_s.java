 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.hadoop.hoya.yarn.appmaster.state;
 
 import com.google.common.annotations.VisibleForTesting;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hoya.HoyaExitCodes;
 import org.apache.hadoop.hoya.HoyaKeys;
 import org.apache.hadoop.hoya.api.ClusterDescription;
 import org.apache.hadoop.hoya.api.OptionKeys;
 import org.apache.hadoop.hoya.api.RoleKeys;
 import org.apache.hadoop.hoya.api.StatusKeys;
 import static org.apache.hadoop.hoya.api.RoleKeys.*;
 import org.apache.hadoop.hoya.exceptions.HoyaInternalStateException;
 import org.apache.hadoop.hoya.exceptions.HoyaRuntimeException;
 import org.apache.hadoop.hoya.exceptions.NoSuchNodeException;
 import org.apache.hadoop.hoya.exceptions.TriggerClusterTeardownException;
 import org.apache.hadoop.hoya.providers.ProviderRole;
 import org.apache.hadoop.hoya.tools.ConfigHelper;
 import org.apache.hadoop.hoya.tools.HoyaUtils;
 import org.apache.hadoop.hoya.exceptions.ErrorStrings;
 import org.apache.hadoop.yarn.api.records.Container;
 import org.apache.hadoop.yarn.api.records.ContainerId;
 import org.apache.hadoop.yarn.api.records.ContainerStatus;
 import org.apache.hadoop.yarn.api.records.Resource;
 import org.apache.hadoop.yarn.api.records.impl.pb.ContainerPBImpl;
 import org.apache.hadoop.yarn.client.api.AMRMClient;
 import org.apache.hadoop.yarn.exceptions.YarnRuntimeException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.atomic.AtomicInteger;
 
 
 /**
  * The model of all the ongoing state of a Hoya AM.
  *
  * concurrency rules: any method which begins with <i>build</i>
  * is not synchronized and intended to be used during
  * initialization.
  */
 public class AppState {
   protected static final Logger log =
     LoggerFactory.getLogger(AppState.class);
   
   private final AbstractRecordFactory recordFactory;
   
   public static final String ROLE_UNKNOWN = "unknown";
 
   /**
    The cluster description published to callers
    This is used as a synchronization point on activities that update
    the CD, and also to update some of the structures that
    feed in to the CD
    */
   public ClusterDescription clusterSpec = new ClusterDescription();
   /**
    * This is the status, the live model
    */
   public ClusterDescription clusterDescription = new ClusterDescription();
 
   private final Map<Integer, RoleStatus> roleStatusMap =
     new HashMap<Integer, RoleStatus>();
 
   private final Map<String, ProviderRole> roles =
     new HashMap<String, ProviderRole>();
 
 
   /**
    * The master node.
    */
   private RoleInstance appMasterNode;
 
   /**
    * Hash map of the containers we have. This includes things that have
    * been allocated but are not live; it is a superset of the live list
    */
   private final ConcurrentMap<ContainerId, RoleInstance> activeContainers =
     new ConcurrentHashMap<ContainerId, RoleInstance>();
 
   /**
    * Hash map of the containers we have released, but we
    * are still awaiting acknowledgements on. Any failure of these
    * containers is treated as a successful outcome
    */
   private final ConcurrentMap<ContainerId, Container> containersBeingReleased =
     new ConcurrentHashMap<ContainerId, Container>();
   
   /**
    *  This is the number of containers which we desire for HoyaAM to maintain
    */
   //private int desiredContainerCount = 0;
 
   /**
    * Counter for completed containers ( complete denotes successful or failed )
    */
   private final AtomicInteger completedContainerCount = new AtomicInteger();
 
   /**
    *   Count of failed containers
 
    */
   private final AtomicInteger failedContainerCount = new AtomicInteger();
 
   /**
    * # of started containers
    */
   private final AtomicInteger startedContainers = new AtomicInteger();
 
   /**
    * # of containers that failed to start 
    */
   private final AtomicInteger startFailedContainers = new AtomicInteger();
 
   /**
    * Track the number of surplus containers received and discarded
    */
   private final AtomicInteger surplusContainers = new AtomicInteger();
 
 
   /**
    * Map of requested nodes. This records the command used to start it,
    * resources, etc. When container started callback is received,
    * the node is promoted from here to the containerMap
    */
   private final Map<ContainerId, RoleInstance> startingNodes =
     new ConcurrentHashMap<ContainerId, RoleInstance>();
 
   /**
    * List of completed nodes. This isn't kept in the CD as it gets too
    * big for the RPC responses. Indeed, we should think about how deep to get this
    */
   private final Map<ContainerId, RoleInstance> completedNodes
     = new ConcurrentHashMap<ContainerId, RoleInstance>();
 
   /**
    * Nodes that failed to start.
    * Again, kept out of the CD
    */
   private final Map<ContainerId, RoleInstance> failedNodes =
     new ConcurrentHashMap<ContainerId, RoleInstance>();
 
   /**
    * Nodes that came assigned to a role above that
    * which were asked for -this appears to happen
    */
   private final Set<ContainerId> surplusNodes = new HashSet<ContainerId>();
 
   /**
    * Map of containerID -> cluster nodes, for status reports.
    * Access to this should be synchronized on the clusterDescription
    */
   private final Map<ContainerId, RoleInstance> liveNodes =
     new ConcurrentHashMap<ContainerId, RoleInstance>();
   private final AtomicInteger completionOfNodeNotInLiveListEvent =
     new AtomicInteger();
   private final AtomicInteger completionOfUnknownContainerEvent =
     new AtomicInteger();
 
 
   /**
    * Record of the max no. of cores allowed in this cluster
    */
   private int containerMaxCores;
 
 
   /**
    * limit container memory
    */
   private int containerMaxMemory;
   
   private RoleHistory roleHistory;
   private long startTimeThreshold;
   
   private int failureThreshold = 10;
 
   public AppState(AbstractRecordFactory recordFactory) {
     this.recordFactory = recordFactory;
   }
 
   public int getFailedCountainerCount() {
     return failedContainerCount.get();
   }
 
   /**
    * Increment the count and return the new value
    * @return the latest failed container count
    */
   public int incFailedCountainerCount() {
     return failedContainerCount.incrementAndGet();
   }
 
   public int getStartFailedCountainerCount() {
     return startFailedContainers.get();
   }
 
   /**
    * Increment the count and return the new value
    * @return the latest failed container count
    */
   public int incStartedCountainerCount() {
     return startedContainers.incrementAndGet();
   }
 
   public int getStartedCountainerCount() {
     return startedContainers.get();
   }
 
   /**
    * Increment the count and return the new value
    * @return the latest failed container count
    */
   public int incStartFailedCountainerCount() {
     return startFailedContainers.incrementAndGet();
   }
 
   
   public AtomicInteger getStartFailedContainers() {
     return startFailedContainers;
   }
 
   public AtomicInteger getCompletionOfNodeNotInLiveListEvent() {
     return completionOfNodeNotInLiveListEvent;
   }
 
   public AtomicInteger getCompletionOfUnknownContainerEvent() {
     return completionOfUnknownContainerEvent;
   }
 
   private Map<Integer, RoleStatus> getRoleStatusMap() {
     return roleStatusMap;
   }
 
   private Map<ContainerId, RoleInstance> getStartingNodes() {
     return startingNodes;
   }
 
   private Map<ContainerId, RoleInstance> getCompletedNodes() {
     return completedNodes;
   }
 
   public Map<ContainerId, RoleInstance> getFailedNodes() {
     return failedNodes;
   }
 
   private Map<ContainerId, RoleInstance> getLiveNodes() {
     return liveNodes;
   }
 
   public ClusterDescription getClusterSpec() {
     return clusterSpec;
   }
 
   public ClusterDescription getClusterDescription() {
     return clusterDescription;
   }
 
   public void setClusterDescription(ClusterDescription clusterDesc) {
     this.clusterDescription = clusterDesc;
   }
 
   private void setClusterSpec(ClusterDescription clusterSpec) {
     this.clusterSpec = clusterSpec;
   }
 
 
   /**
    * Get the role history of the application
    * @return the role history
    */
   @VisibleForTesting
   public RoleHistory getRoleHistory() {
     return roleHistory;
   }
 
   /**
    * Get the path used for history files
    * @return the directory used for history files
    */
   @VisibleForTesting
   public Path getHistoryPath() {
     return roleHistory.getHistoryPath();
   }
 
   /**
    * Set the container limits -the max that can be asked for,
    * which are used when the "max" values are requested
    * @param maxMemory maximum memory
    * @param maxCores maximum cores
    */
   public void setContainerLimits(int maxMemory, int maxCores) {
     containerMaxCores = maxCores;
     containerMaxMemory = maxMemory;
   }
   
   /**
    * Build up the application state
    * @param clusterSpec cluster specification
    * @param siteConf site configuration
    * @param providerRoles roles offered by a provider
    * @param fs filesystem
    * @param historyDir directory containing history files
    */
   public void buildInstance(ClusterDescription clusterSpec,
                             Configuration siteConf,
                             List<ProviderRole> providerRoles,
                             FileSystem fs,
                             Path historyDir) {
 
 
     // set the cluster specification
     setClusterSpec(clusterSpec);
 
 
     //build the role list
     for (ProviderRole providerRole : providerRoles) {
       buildRole(providerRole);
     }
     //then pick up the requirements
     buildRoleRequirementsFromClusterSpec();
 
     //copy into cluster status. 
     ClusterDescription clusterStatus = ClusterDescription.copy(clusterSpec);
     Set<String> confKeys = ConfigHelper.sortedConfigKeys(siteConf);
 
 //     Add the -site configuration properties
     for (String key : confKeys) {
       String val = siteConf.get(key);
       clusterStatus.clientProperties.put(key, val);
     }
 
     //set the livespan
     startTimeThreshold = 1000 * clusterSpec.getOptionInt(
       OptionKeys.CONTAINER_FAILURE_SHORTLIFE,
       OptionKeys.DEFAULT_CONTAINER_FAILURE_SHORTLIFE);
     
     failureThreshold = clusterSpec.getOptionInt(
       OptionKeys.CONTAINER_FAILURE_THRESHOLD,
       OptionKeys.DEFAULT_CONTAINER_FAILURE_THRESHOLD);
     
     clusterStatus.state = ClusterDescription.STATE_CREATED;
     long now = now();
     clusterStatus.setInfoTime(StatusKeys.INFO_LIVE_TIME_HUMAN,
                               StatusKeys.INFO_LIVE_TIME_MILLIS,
                               now);
     if (0 == clusterStatus.createTime) {
       clusterStatus.createTime = now;
       clusterStatus.setInfoTime(StatusKeys.INFO_CREATE_TIME_HUMAN,
                                 StatusKeys.INFO_CREATE_TIME_MILLIS,
                                 now);
     }
     clusterStatus.state = ClusterDescription.STATE_LIVE;
 
     //set the app state to this status
     setClusterDescription(clusterStatus);
     
     // add the roles
     roleHistory = new RoleHistory(providerRoles);
     roleHistory.onStart(fs, historyDir);
   }
 
   /**
    * The cluster specification has been updated
    * @param cd updated cluster specification
    */
   public synchronized void updateClusterSpec(ClusterDescription cd) {
     setClusterSpec(cd);
 
     //propagate info from cluster, specifically the role table
 
     Map<String, Map<String, String>> newroles = getClusterSpec().roles;
     getClusterDescription().roles = HoyaUtils.deepClone(newroles);
     getClusterDescription().updateTime = now();
     buildRoleRequirementsFromClusterSpec();
   }
 
   /**
    * build the role requirements from the cluster specification
    */
   private void buildRoleRequirementsFromClusterSpec() {
     //now update every role's desired count.
     //if there are no instance values, that role count goes to zero
     for (RoleStatus roleStatus : getRoleStatusMap().values()) {
       int currentDesired = roleStatus.getDesired();
       String role = roleStatus.getName();
       int desiredInstanceCount =
         getClusterSpec().getDesiredInstanceCount(role, -1);
       if (currentDesired != desiredInstanceCount) {
         log.info("Role {} flexed from {} to {}", role, currentDesired,
                  desiredInstanceCount);
         roleStatus.setDesired(desiredInstanceCount);
       }
     }
   }
 
   /**
    * Add knowledge of a role.
    * This is a build-time operation that is not synchronized, and
    * should be used while setting up the system state -before servicing
    * requests.
    * @param providerRole role to add
    */
   public void buildRole(ProviderRole providerRole) {
     //build role status map
     roleStatusMap.put(providerRole.id,
                       new RoleStatus(providerRole));
     roles.put(providerRole.name, providerRole);
   }
 
   /**
    * build up the special master node, which lives
    * in the live node set but has a lifecycle bonded to the AM
    * @param containerId the AM master
    */
   public void buildAppMasterNode(ContainerId containerId) {
     Container container = new ContainerPBImpl();
     container.setId(containerId);
     
     RoleInstance am = new RoleInstance(container);
     am.role = HoyaKeys.ROLE_HOYA_AM;
     am.buildUUID();
     appMasterNode = am;
     //it is also added to the set of live nodes
     getLiveNodes().put(containerId, am);
   }
 
   /**
    * Note that the master node has been launched,
    * though it isn't considered live until any forked
    * processes are running. It is NOT registered with
    * the role history -the container is incomplete
    * and it will just cause confusion
    */
   public void noteAMLaunched() {
     getLiveNodes().put(appMasterNode.getContainerId(), appMasterNode);
   }
 
   /**
    * AM declares ourselves live in the cluster description.
    * This is meant to be triggered from the callback
    * indicating the spawned process is up and running.
    */
   public void noteAMLive() {
     appMasterNode.state = ClusterDescription.STATE_LIVE;
   }
 
   public RoleInstance getAppMasterNode() {
     return appMasterNode;
   }
 
   /**
    * Look up a role from its key -or fail 
    *
    * @param key key to resolve
    * @return the status
    * @throws YarnRuntimeException on no match
    */
   public RoleStatus lookupRoleStatus(int key) throws HoyaRuntimeException {
     RoleStatus rs = getRoleStatusMap().get(key);
     if (rs == null) {
       throw new HoyaRuntimeException("Cannot find role for role ID " + key);
     }
     return rs;
   }
 
   /**
    * Look up a role from its key -or fail 
    *
    * @param c container in a role
    * @return the status
    * @throws YarnRuntimeException on no match
    */
   public RoleStatus lookupRoleStatus(Container c) throws YarnRuntimeException {
     return lookupRoleStatus(ContainerPriority.extractRole(c));
   }
 
 
   /**
    * Look up a role from its key -or fail 
    *
    * @param c container in a role
    * @return the status
    * @throws YarnRuntimeException on no match
    */
   public RoleStatus lookupRoleStatus(String name) throws YarnRuntimeException {
     ProviderRole providerRole = roles.get(name);
     if (providerRole == null) {
       throw new YarnRuntimeException("Unknown role " + name);
     }
     return lookupRoleStatus(providerRole.id);
   }
 
   /**
    * Clone a list of active containers
    * @return the active containers at the time
    * the call was made
    */
   public synchronized List<RoleInstance> cloneActiveContainerList() {
     Collection<RoleInstance> values = activeContainers.values();
     return new ArrayList<RoleInstance>(values);
   }
   
   /**
    * Get any active container with the given ID
    * @param id container Id
    * @return the active container or null if it is not found
    */
   public RoleInstance getActiveContainer(ContainerId id) {
     return activeContainers.get(id);
   }
 
   /**
    * Create a clone of the list of live cluster nodes.
    * @return the list of nodes, may be empty
    */
   public synchronized List<RoleInstance> cloneLiveContainerInfoList() {
     List<RoleInstance> allRoleInstances;
     Collection<RoleInstance> values = getLiveNodes().values();
     allRoleInstances = new ArrayList<RoleInstance>(values);
     return allRoleInstances;
   }
 
 
   /**
    * Get the {@link RoleInstance} details on a node
    * @param uuid the UUID
    * @return null if there is no such node
    * @throws NoSuchNodeException if the node cannot be found
    * @throws IOException IO problems
    */
   public synchronized RoleInstance getLiveInstanceByUUID(String uuid)
     throws IOException, NoSuchNodeException {
     Collection<RoleInstance> nodes = getLiveNodes().values();
     for (RoleInstance node : nodes) {
       if (uuid.equals(node.uuid)) {
         return node;
       }
     }
     //at this point: no node
     throw new NoSuchNodeException(uuid);
   }
 
   /**
    * Get the details on a list of instaces referred to by UUID.
    * Unknown nodes are not returned
    * <i>Important: the order of the results are undefined</i>
    * @param uuid the UUIDs
    * @return list of instances
    * @throws IOException IO problems
    */
   public List<RoleInstance> getLiveContainerInfosByUUID(String[] uuids) throws IOException {
     Collection<String> strings = Arrays.asList(uuids);
     return getLiveContainerInfosByUUID(strings);
   }
 
   /**
    * Get the details on a list of instaces referred to by UUID.
    * Unknown nodes are not returned
    * <i>Important: the order of the results are undefined</i>
    * @param uuid the UUIDs
    * @return list of instances
    * @throws IOException IO problems
    */
   public List<RoleInstance> getLiveContainerInfosByUUID(Collection<String> uuids) {
     //first, a hashmap of those uuids is built up
     Set<String> uuidSet = new HashSet<String>(uuids);
     List<RoleInstance> nodes = new ArrayList<RoleInstance>(uuidSet.size());
     Collection<RoleInstance> clusterNodes = getLiveNodes().values();
 
     for (RoleInstance node : clusterNodes) {
       if (uuidSet.contains(node.uuid)) {
         nodes.add(node);
       }
     }
     //at this point: a possibly empty list of nodes
     return nodes;
   }
 
   /**
    * Enum all nodes by role. 
    * @param role role, or "" for all roles
    * @return a list of nodes, may be empty
    */
   public synchronized List<RoleInstance> enumLiveNodesInRole(String role) {
     List<RoleInstance> nodes = new ArrayList<RoleInstance>();
     Collection<RoleInstance> allRoleInstances = getLiveNodes().values();
     for (RoleInstance node : allRoleInstances) {
       if (role.isEmpty() || role.equals(node.role)) {
         nodes.add(node);
       }
     }
     return nodes;
   }
 
 
   /**
    * Build an instance map.
    * @return the map of RoleId to count
    */
   private synchronized Map<String, Integer> createRoleToInstanceMap() {
     Map<String, Integer> map = new HashMap<String, Integer>();
     for (RoleInstance node : getLiveNodes().values()) {
       Integer entry = map.get(node.role);
       int current = entry != null ? entry : 0;
       current++;
       map.put(node.role, current);
     }
     return map;
   }
 
   /**
    * Notification called just before the NM is asked to 
    * start a container
    * @param container container to start
    * @param instance clusterNode structure
    */
   public void containerStartSubmitted(Container container,
                                       RoleInstance instance) {
     instance.state = ClusterDescription.STATE_SUBMITTED;
     instance.container = container;
     instance.createTime = now();
     getStartingNodes().put(container.getId(), instance);
     activeContainers.put(container.getId(), instance);
     roleHistory.onContainerStartSubmitted(container, instance);
   }
 
   /**
    * Note that a container has been submitted for release; update internal state
    * and mark the associated ContainerInfo released field to indicate that
    * while it is still in the active list, it has been queued for release.
    *
    * @param container container
    * @throws HoyaInternalStateException if there is no container of that ID
    * on the active list
    */
   public synchronized void containerReleaseSubmitted(Container container) throws
                                                                      HoyaInternalStateException {
     ContainerId id = container.getId();
     //look up the container
     RoleInstance info = getActiveContainer(id);
     if (info == null) {
       throw new HoyaInternalStateException(
         "No active container with ID " + id.toString());
     }
     //verify that it isn't already released
     if (containersBeingReleased.containsKey(id)) {
       throw new HoyaInternalStateException(
         "Container %s already queued for release", id);
     }
     info.released = true;
     containersBeingReleased.put(id, info.container);
     RoleStatus role = lookupRoleStatus(info.roleId);
     role.incReleasing();
     roleHistory.onContainerReleaseSubmitted(container);
   }
 
 
   /**
    * Set up the resource requirements with all that this role needs, 
    * then create the container request itself.
    * @param role role to ask an instance of
    * @param capability a resource to set up
    * @return
    */
   public AMRMClient.ContainerRequest buildContainerResourceAndRequest(
         RoleStatus role,
         Resource capability) {
     buildResourceRequirements(role, capability);
     //get the role history to select a suitable node, if available
     AMRMClient.ContainerRequest containerRequest =
     createContainerRequest(role, capability);
     return  containerRequest;
   }
 
   /**
    * Create a container request.
    * Update internal state, such as the role request count
    * This is where role history information will be used for placement decisions -
    * @param role role
    * @param resource requirements
    * @return the container request to submit
    */
   public AMRMClient.ContainerRequest createContainerRequest(RoleStatus role,
                                                             Resource resource) {
     
     
     AMRMClient.ContainerRequest request;
     request = roleHistory.requestNode(role.getKey(), resource);
     role.incRequested();
 
     return request;
   }
 
   /**
    * Build up the resource requirements for this role from the
    * cluster specification, including substituing max allowed values
    * if the specification asked for it.
    * @param role role
    * @param capability capability to set up
    */
   public void buildResourceRequirements(RoleStatus role, Resource capability) {
     // Set up resource requirements from role values
     String name = role.getName();
     int cores = getClusterSpec().getRoleResourceRequirement(name,
                                                YARN_CORES,
                                                DEF_YARN_CORES,
                                                containerMaxCores);
     capability.setVirtualCores(cores);
     int ram = getClusterSpec().getRoleResourceRequirement(name,
                                              YARN_MEMORY,
                                              DEF_YARN_MEMORY,
                                              containerMaxMemory);
     capability.setMemory(ram);
   }
 
   /**
    * add a launched container to the node map for status responses
    * @param container id
    * @param node node details
    */
   private void addLaunchedContainer(Container container, RoleInstance node) {
     node.container = container;
     if (node.role == null) {
       log.warn("Unknown role for node {}", node);
       node.role = ROLE_UNKNOWN;
     }
     getLiveNodes().put(node.getContainerId(), node);
     //tell role history
     roleHistory.onContainerStarted(container);
   }
 
   /**
    * container start event
    * @param containerId container that is to be started
    * @return the role instance, or null if there was a problem
    */
   public synchronized RoleInstance onNodeManagerContainerStarted(ContainerId containerId) {
     try {
       return innerOnNodeManagerContainerStarted(containerId);
     } catch (YarnRuntimeException e) {
       log.error("NodeManager callback on started container {} failed",
                 containerId,
                 e);
       return null;
     }
   }
 
    /**
    * container start event handler -throwing an exception on problems
    * @param containerId container that is to be started
    * @return the role instance
    * @throws HoyaRuntimeException null if there was a problem
    */
   @VisibleForTesting
   public RoleInstance innerOnNodeManagerContainerStarted(ContainerId containerId)
       throws HoyaRuntimeException {
     incStartedCountainerCount();
     RoleInstance instance = activeContainers.get(containerId);
     if (instance == null) {
       //serious problem
       throw new HoyaRuntimeException("Container not in active containers start %s",
                 containerId);
     }
     if (instance.role == null) {
       throw new HoyaRuntimeException("Role instance has no role name %s",
                                      instance);
     }
     instance.startTime = now();
     RoleInstance starting = getStartingNodes().remove(containerId);
     if (null == starting) {
       throw new HoyaRuntimeException(
         "Container %s is already started", containerId);
     }
     instance.state = ClusterDescription.STATE_LIVE;
     RoleStatus roleStatus = lookupRoleStatus(instance.roleId);
     roleStatus.incStarted();
     Container container = instance.container;
     addLaunchedContainer(container, instance);
     return instance;
   }
 
   /**
    * update the application state after a failure to start a container.
    * This is perhaps where blacklisting could be most useful: failure
    * to start a container is a sign of a more serious problem
    * than a later exit.
    *
    * -relayed from NMClientAsync.CallbackHandler 
    * @param containerId failing container
    * @param thrown what was thrown
    */
   public synchronized void onNodeManagerContainerStartFailed(ContainerId containerId,
                                                              Throwable thrown) {
     activeContainers.remove(containerId);
     incFailedCountainerCount();
     incStartFailedCountainerCount();
     RoleInstance instance = getStartingNodes().remove(containerId);
     if (null != instance) {
       RoleStatus roleStatus = lookupRoleStatus(instance.roleId);
       if (null != thrown) {
         instance.diagnostics = HoyaUtils.stringify(thrown);
       }
       roleStatus.noteFailed(null);
       roleStatus.incStartFailed(); 
       getFailedNodes().put(containerId, instance);
       roleHistory.onNodeManagerContainerStartFailed(instance.container);
     }
   }
 
   /**
    * Is a role short lived by the threshold set for this application
    * @param instance instance
    * @return true if the instance is considered short live
    */
   @VisibleForTesting
   public boolean isShortLived(RoleInstance instance) {
     long time = now();
     long started = instance.startTime;
     boolean shortlived;
     if (started > 0) {
       long duration = time - started;
       shortlived = duration < startTimeThreshold;
     } else {
       // never even saw a start event
       shortlived = true;
     }
     return shortlived;
   }
 
   /**
    * Current time in milliseconds. Made protected for
    * the option to override it in tests.
    * @return the current time.
    */
   protected long now() {
     return System.currentTimeMillis();
   }
 
   /**
    * This is a very small class to send a triple result back from 
    * the completion operation
    */
   public static class NodeCompletionResult {
     public boolean surplusNode = false;
     public RoleInstance roleInstance;
     public boolean containerFailed;
 
     @Override
     public String toString() {
       final StringBuilder sb =
         new StringBuilder("NodeCompletionResult{");
       sb.append("surplusNode=").append(surplusNode);
       sb.append(", roleInstance=").append(roleInstance);
       sb.append(", containerFailed=").append(containerFailed);
       sb.append('}');
       return sb.toString();
     }
   }
   
   /**
    * handle completed node in the CD -move something from the live
    * server list to the completed server list
    * @param status the node that has just completed
    * @return NodeCompletionResult
    */
   public synchronized NodeCompletionResult onCompletedNode(ContainerStatus status) {
     ContainerId containerId = status.getContainerId();
     NodeCompletionResult result = new NodeCompletionResult();
     RoleInstance roleInstance;
 
     if (containersBeingReleased.containsKey(containerId)) {
       log.info("Container was queued for release");
       Container container = containersBeingReleased.remove(containerId);
       RoleStatus roleStatus = lookupRoleStatus(container);
       log.info("decrementing role count for role {}", roleStatus.getName());
       roleStatus.decReleasing();
       roleStatus.decActual();
       roleStatus.incCompleted();
       roleHistory.onReleaseCompleted(container);
 
     } else if (surplusNodes.remove(containerId)) {
       //its a surplus one being purged
       result.surplusNode = true;
     } else {
       //a container has failed 
       result.containerFailed = true;
       roleInstance = activeContainers.remove(containerId);
       if (roleInstance != null) {
         //it was active, move it to failed 
         incFailedCountainerCount();
         failedNodes.put(containerId, roleInstance);
       } else {
         // the container may have been noted as failed already, so look
         // it up
         roleInstance = failedNodes.get(containerId);
       }
       if (roleInstance != null) {
         int roleId = roleInstance.roleId;
         log.info("Failed container in role {}", roleId);
         try {
           RoleStatus roleStatus = lookupRoleStatus(roleId);
           roleStatus.decActual();
           boolean shortLived = isShortLived(roleInstance);
           String message;
           if (roleInstance.container != null) {
             message = String.format("Failure %s on host %s",
                                            roleInstance.getContainerId(),
                                            roleInstance.container
                                                        .getNodeId()
                                                        .getHost());
           } else {
             message = String.format("Failure %s",
                                     containerId.toString());
           }
           roleStatus.noteFailed(message);
           //have a look to see if it short lived
           if (shortLived) {
             roleStatus.incStartFailed();
           }
           
           roleHistory.onFailedContainer(roleInstance.container, shortLived);
           
         } catch (YarnRuntimeException e1) {
           log.error("Failed container of unknown role {}", roleId);
         }
       } else {
         //this isn't a known container.
         
         log.error("Notified of completed container that is not in the list" +
                   " of active or failed containers");
         completionOfUnknownContainerEvent.incrementAndGet();
       }
     }
     
     if (result.surplusNode) {
       //a surplus node
       return result;
     }
     
     //record the complete node's details; this pulls it from the livenode set 
     //remove the node
     ContainerId id = status.getContainerId();
     RoleInstance node = getLiveNodes().remove(id);
     if (node == null) {
       log.warn("Received notification of completion of unknown node");
       completionOfNodeNotInLiveListEvent.incrementAndGet();
 
     } else {
       node.state = ClusterDescription.STATE_DESTROYED;
       node.exitCode = status.getExitStatus();
       node.diagnostics = status.getDiagnostics();
       getCompletedNodes().put(id, node);
       result.roleInstance = node;
     }
     return result;
   }
 
 
   /**
    * Return the percentage done that Hoya is to have YARN display in its
    * Web UI
    * @return an number from 0 to 100
    */
   public synchronized float getApplicationProgressPercentage() {
     float percentage = 0;
     int desired = 0;
     float actual = 0;
     for (RoleStatus role : getRoleStatusMap().values()) {
       desired += role.getDesired();
       actual += role.getActual();
     }
     if (desired == 0) {
       percentage = 100;
     } else {
       percentage = actual / desired;
     }
     return percentage;
   }
 
   /**
    * Update the cluster description with anything interesting
    * @param providerStatus status from the provider
    */
   public void refreshClusterStatus(Map<String, String> providerStatus) {
     ClusterDescription cd = getClusterDescription();
     long now = now();
     cd.setInfoTime(StatusKeys.INFO_STATUS_TIME_HUMAN,
                    StatusKeys.INFO_STATUS_TIME_MILLIS,
                    now);
    for (Map.Entry<String, String> entry : providerStatus.entrySet()) {
      cd.setInfo(entry.getKey(),entry.getValue());
     }
     // set the RM-defined maximum cluster values
     cd.setInfo(RoleKeys.YARN_CORES, Integer.toString(containerMaxCores));
     cd.setInfo(RoleKeys.YARN_MEMORY, Integer.toString(containerMaxMemory));
     HoyaUtils.addBuildInfo(cd,"status");
     cd.statistics = new HashMap<String, Map<String, Integer>>();
     Map<String, Integer> instanceMap = createRoleToInstanceMap();
     if (log.isDebugEnabled()) {
       for (Map.Entry<String, Integer> entry : instanceMap.entrySet()) {
         log.debug("[{}]: {}", entry.getKey(), entry.getValue());
       }
     }
     cd.instances = instanceMap;
     
     for (RoleStatus role : getRoleStatusMap().values()) {
       String rolename = role.getName();
       Integer count = instanceMap.get(rolename);
       if (count == null) {
         count = 0;
       } 
       int nodeCount = count;
       cd.setDesiredInstanceCount(rolename,role.getDesired());
       cd.setActualInstanceCount(rolename, nodeCount);
       cd.setRoleOpt(rolename, ROLE_REQUESTED_INSTANCES, role.getRequested());
       cd.setRoleOpt(rolename, ROLE_RELEASING_INSTANCES, role.getReleasing());
       cd.setRoleOpt(rolename, ROLE_FAILED_INSTANCES, role.getFailed());
       cd.setRoleOpt(rolename, ROLE_FAILED_STARTING_INSTANCES, role.getStartFailed());
       Map<String, Integer> stats = role.buildStatistics();
       cd.statistics.put(rolename, stats);
     }
 
     Map<String, Integer> hoyastats = new HashMap<String, Integer>();
     hoyastats.put(StatusKeys.STATISTICS_CONTAINERS_COMPLETED, completedContainerCount.get());
     hoyastats.put(StatusKeys.STATISTICS_CONTAINERS_FAILED, failedContainerCount.get());
     hoyastats.put(StatusKeys.STATISTICS_CONTAINERS_LIVE, liveNodes.size());
     hoyastats.put(StatusKeys.STATISTICS_CONTAINERS_STARTED,startedContainers.get());
     hoyastats.put(StatusKeys.STATISTICS_CONTAINERS_START_FAILED, startFailedContainers.get());
     hoyastats.put(StatusKeys.STATISTICS_CONTAINERS_SURPLUS, surplusContainers.get());
     hoyastats.put(StatusKeys.STATISTICS_CONTAINERS_UNKNOWN_COMPLETED,
                   completionOfUnknownContainerEvent.get());
     cd.statistics.put(HoyaKeys.ROLE_HOYA_AM, hoyastats);
     
   }
 
   /**
    * Look at where the current node state is -and whether it should be changed
    */
   public synchronized List<AbstractRMOperation> reviewRequestAndReleaseNodes()
       throws HoyaInternalStateException, TriggerClusterTeardownException {
     log.debug("in reviewRequestAndReleaseNodes()");
     List<AbstractRMOperation> allOperations =
       new ArrayList<AbstractRMOperation>();
     for (RoleStatus roleStatus : getRoleStatusMap().values()) {
       if (!roleStatus.getExcludeFromFlexing()) {
         List<AbstractRMOperation> operations = reviewOneRole(roleStatus);
         allOperations.addAll(operations);
       }
     }
     return allOperations;
   }
   
   public void checkFailureThreshold(RoleStatus role) throws
                                                         TriggerClusterTeardownException {
     int failures = role.getFailed();
 
     if (failures > failureThreshold) {
       throw new TriggerClusterTeardownException(
         HoyaExitCodes.EXIT_CLUSTER_FAILED,
         ErrorStrings.E_UNSTABLE_CLUSTER +
         " - failed with role %s failing %d times (%d in startup); threshold is %d - last failure: %s",
         role.getName(),
         role.getFailed(),
         role.getStartFailed(),
         failureThreshold,
         role.getFailureMessage());
     }
   }
   
   /**
    * Look at the allocation status of one role, and trigger add/release
    * actions if the number of desired role instances doesnt equal 
    * (actual+pending)
    * @param role role
    * @return a list of operations
    * @throws HoyaInternalStateException if the operation reveals that
    * the internal state of the application is inconsistent.
    */
   public List<AbstractRMOperation> reviewOneRole(RoleStatus role)
       throws HoyaInternalStateException, TriggerClusterTeardownException {
     List<AbstractRMOperation> operations = new ArrayList<AbstractRMOperation>();
     int delta;
     String details;
     int expected;
     String name = role.getName();
     synchronized (role) {
       delta = role.getDelta();
       details = role.toString();
       expected = role.getDesired();
     }
 
     log.info(details);
     checkFailureThreshold(role);
     
     if (delta > 0) {
       log.info("{}: Asking for {} more nodes(s) for a total of {} ", name,
                delta, expected);
       //more workers needed than we have -ask for more
       for (int i = 0; i < delta; i++) {
         Resource capability = recordFactory.newResource();
         AMRMClient.ContainerRequest containerAsk =
           buildContainerResourceAndRequest(role, capability);
         log.info("Container ask is {}", containerAsk);
         if (containerAsk.getCapability().getMemory() >
             this.containerMaxMemory) {
           log.warn(
             "Memory requested: " + containerAsk.getCapability().getMemory() +
             " > " +
             this.containerMaxMemory);
         }
         operations.add(new ContainerRequestOperation(containerAsk));
       }
     } else if (delta < 0) {
       log.info("{}: Asking for {} fewer node(s) for a total of {}", name,
                -delta,
                expected);
       //reduce the number expected (i.e. subtract the delta)
 
       //then pick some containers to kill
       int excess = -delta;
 
       // get the nodes to release
       int roleId = role.getKey();
       List<NodeInstance> nodesForRelease =
         roleHistory.findNodesForRelease(roleId, excess);
       
       for (NodeInstance node : nodesForRelease) {
         Container possible = findContainerOnHost(node, roleId);
         if (possible == null) {
           throw new HoyaInternalStateException(
             "Failed to find a container to release on node %s", node.hostname);
         }
         containerReleaseSubmitted(possible);
         operations.add(new ContainerReleaseOperation(possible.getId()));
 
       }
    
     }
 
     return operations;
   }
 
 
   /**
    * Find a container running on a specific host -looking
    * into the node ID to determine this.
    *
    * @param node node
    * @param roleId role the container must be in
    * @return a container or null if there are no containers on this host
    * that can be released.
    */
   private Container findContainerOnHost(NodeInstance node, int roleId) {
     Collection<RoleInstance> targets = cloneActiveContainerList();
     String hostname = node.hostname;
     for (RoleInstance ri : targets) {
       if (hostname.equals(RoleHistoryUtils.hostnameOf(ri.container))
                          && ri.roleId == roleId
         && containersBeingReleased.get(ri.getContainerId()) == null) {
         return ri.container;
       }
     }
     return null;
   }
   
   /**
    * Release all containers.
    * @return a list of operations to execute
    */
   public synchronized List<AbstractRMOperation> releaseAllContainers() {
 
     Collection<RoleInstance> targets = cloneActiveContainerList();
     log.info("Releasing {} containers", targets.size());
     List<AbstractRMOperation> operations =
       new ArrayList<AbstractRMOperation>(targets.size());
     for (RoleInstance instance : targets) {
       Container possible = instance.container;
       ContainerId id = possible.getId();
       if (!instance.released) {
         try {
           containerReleaseSubmitted(possible);
         } catch (HoyaInternalStateException e) {
           log.warn("when releasing container {} :", possible, e);
         }
         operations.add(new ContainerReleaseOperation(id));
       }
     }
     return operations;
   }
 
   /**
    * Event handler for allocated containers: builds up the lists
    * of assignment actions (what to run where), and possibly
    * a list of release operations
    * @param allocatedContainers the containers allocated
    * @param assignments the assignments of roles to containers
    * @param releaseOperations any release operations
    */
   public synchronized void onContainersAllocated(List<Container> allocatedContainers,
                                     List<ContainerAssignment> assignments,
                                     List<AbstractRMOperation> releaseOperations) {
     assignments.clear();
     releaseOperations.clear();
     for (Container container : allocatedContainers) {
       String containerHostInfo = container.getNodeId().getHost()
                                  + ":" +
                                  container.getNodeId().getPort();
       int allocated;
       int desired;
       //get the role
       ContainerId cid = container.getId();
       RoleStatus role = lookupRoleStatus(container);
       
 
       //dec requested count
       role.decRequested();
       //inc allocated count -this may need to be dropped in a moment,
       // but us needed to update the logic below
       allocated = role.incActual();
 
       //look for (race condition) where we get more back than we asked
       desired = role.getDesired();
 
       roleHistory.onContainerAllocated( container, desired, allocated );
 
       if (allocated > desired) {
         log.info("Discarding surplus container {} on {}", cid,
                  containerHostInfo);
         releaseOperations.add(new ContainerReleaseOperation(cid));
         //register as a surplus node
         surplusNodes.add(cid);
         surplusContainers.incrementAndGet();
         //and, as we aren't binding it to role, dec that role's actual count
         role.decActual();
       } else {
 
         String roleName = role.getName();
         log.info("Assiging role {} to container" +
                  " {}," +
                  " on {}:{},",
                  roleName,
                  cid,
                  container.getNodeId().getHost(),
                  container.getNodeId().getPort()
                 );
 
         assignments.add(new ContainerAssignment(container, role));
         //add to the history
         roleHistory.onContainerAssigned(container);
       }
     }
   }
 
   /**
    * Get diagnostics info about containers
    */
   public String getContainerDiagnosticInfo() {
     StringBuilder builder = new StringBuilder();
     for (RoleStatus roleStatus : getRoleStatusMap().values()) {
       builder.append(roleStatus).append('\n');
     }
     return builder.toString();
   }
 
 }

 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.neo.loader;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.Set;
 
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.enums.CallProperties;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.enums.ProbeCallRelationshipType;
import org.amanzi.neo.core.enums.CallProperties.CallResult;
 import org.amanzi.neo.core.enums.CallProperties.CallType;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.index.MultiPropertyIndex;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Transaction;
 
 
 /**
  * <p>
  *Abstract loader of call dataset
  * </p>
  * @author tsinkel_a
  * @since 1.0.0
  */
 public abstract class AbstractCallLoader extends DriveLoader {
     protected static final String TIME_FORMAT = "HH:mm:ss";
     private final static SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);
     protected Node callDataset;
     /*
      * Last call in Real Dataset
      */
     private Node lastCallInDataset;
     /*
      * Network node for Probes data
      */
     protected Node networkNode;
     /** Header Index for Real Dataset. */
     protected static final int REAL_DATASET_HEADER_INDEX = 0;
     /** Header Index for Call Dataset. */
     protected static final int CALL_DATASET_HEADER_INDEX = 1;
 
     /** Header Index for Probe Network Dataset. */
     protected static final int PROBE_NETWORK_HEADER_INDEX = 2;
     /*
      * Timestamp Index for Calls
      */
     private final HashMap<String, MultiPropertyIndex<Long>> callTimestampIndexes = new HashMap<String, MultiPropertyIndex<Long>>();
 
 
     /**
      * Creates a Call node and sets properties
      */
     protected  void saveCall(Call call) {        
         if ((call != null) && (call.getCallType() != null)) {
             CallType callType = call.getCallType();
             Transaction tx = neo.beginTx();
             try {
                 switch (callType) {
                 case INDIVIDUAL:
                 case GROUP:
                 case EMERGENCY:
                 case HELP:
                     storeRealCall(call);
                     break;
                 case SDS:
                 case TSM:
                 case ALARM:
                     storeMessageCall(call);
                     break;
                 case ITSI_ATTACH:
                     storeITSICall(call);
                     break;
                 case ITSI_CC:
                     storeITSICCCall(call);
                 default:
                     NeoCorePlugin.error("Unknown call type "+callType+".", null);
                 }
                 tx.success();
             }
             catch (Exception e) {
                 tx.failure();
                 NeoCorePlugin.error(null, e);
             }
             finally {
                 tx.finish();
             }
         }
     }
 
     private void storeRealCall(Call call) {
         Node probeCallNode = call.getCallerProbe();
         Node callNode = createCallNode(call.getTimestamp(), call.getRelatedNodes(), probeCallNode);
 
         long setupDuration = call.getCallSetupEnd() - call.getCallSetupBegin();
         long terminationDuration = call.getCallTerminationEnd() - call.getCallTerminationBegin();
         long callDuration = call.getCallTerminationEnd() - call.getCallSetupBegin();
 
         LinkedHashMap<String, Header> headers = getHeaderMap(CALL_DATASET_HEADER_INDEX).headers;
 
         setIndexProperty(headers, callNode, CallProperties.SETUP_DURATION.getId(), setupDuration);
         setIndexProperty(headers, callNode, CallProperties.CALL_TYPE.getId(), call.getCallType().toString());
         setIndexProperty(headers, callNode, CallProperties.CALL_RESULT.getId(), call.getCallResult().toString());
         setIndexProperty(headers, callNode, CallProperties.CALL_DURATION.getId(), callDuration);
         setIndexProperty(headers, callNode, CallProperties.TERMINATION_DURATION.getId(), terminationDuration);
         
         callNode.setProperty(CallProperties.LQ.getId(), call.getLq());
         callNode.setProperty(CallProperties.DELAY.getId(), call.getDelay());
         
         callNode.createRelationshipTo(probeCallNode, ProbeCallRelationshipType.CALLER);
         
         for (Node calleeProbe : call.getCalleeProbes()) {
             callNode.createRelationshipTo(calleeProbe, ProbeCallRelationshipType.CALLEE);
         }
         
         probeCallNode.setProperty(call.getCallType().getProperty(), true);
     }
     
     private void storeMessageCall(Call call) {
         Node probeCallNode = call.getCallerProbe();
         Node callNode = createCallNode(call.getTimestamp(), call.getRelatedNodes(), probeCallNode);
 
         //TODO remove fake mechanism after investigation
         long callSetupEnd = call.getCallSetupEnd();
         long callTerminationEnd = call.getCallTerminationEnd();
         long receivedTime = call.getReceivedTime()==null?callTerminationEnd - callSetupEnd:call.getReceivedTime();
         //TODO remove fake mechanism after investigation
         long callTerminationBegin = call.getCallTerminationBegin();
         long acknTime = call.getAcknowlegeTime()==null?callTerminationBegin-callSetupEnd:call.getAcknowlegeTime();
         LinkedHashMap<String, Header> headers = getHeaderMap(CALL_DATASET_HEADER_INDEX).headers;
 
         if (call.getCallType().equals(CallType.ALARM)) {
             setIndexProperty(headers, callNode, CallProperties.ALM_MESSAGE_DELAY.getId(), receivedTime);
             setIndexProperty(headers, callNode, CallProperties.ALM_FIRST_MESS_DELAY.getId(), acknTime);
         } else {
             setIndexProperty(headers, callNode, CallProperties.MESS_RECEIVE_TIME.getId(), receivedTime);
             setIndexProperty(headers, callNode, CallProperties.MESS_ACKNOWLEDGE_TIME.getId(), acknTime);
         }
         setIndexProperty(headers, callNode, CallProperties.CALL_TYPE.getId(), call.getCallType().toString());
        CallResult callResult = call.getCallResult()==null?CallResult.FAILURE:call.getCallResult();
        setIndexProperty(headers, callNode, CallProperties.CALL_RESULT.getId(), callResult.toString());
         
         callNode.createRelationshipTo(probeCallNode, ProbeCallRelationshipType.CALLER);
         
         for (Node calleeProbe : call.getCalleeProbes()) {
             callNode.createRelationshipTo(calleeProbe, ProbeCallRelationshipType.CALLEE);
         }
         
         probeCallNode.setProperty(call.getCallType().getProperty(), true);
     }
     
     private void storeITSICall(Call call) {
         Node probeCallNode = call.getCallerProbe();
         Node callNode = createCallNode(call.getTimestamp(), call.getRelatedNodes(), probeCallNode);
         
         long updateTime = call.getCallTerminationEnd() - call.getCallSetupBegin();
         
         LinkedHashMap<String, Header> headers = getHeaderMap(CALL_DATASET_HEADER_INDEX).headers;
         setIndexProperty(headers, callNode, CallProperties.CALL_DURATION.getId(), updateTime);
         
         setIndexProperty(headers, callNode, CallProperties.CALL_TYPE.getId(), call.getCallType().toString());
         setIndexProperty(headers, callNode, CallProperties.CALL_RESULT.getId(), call.getCallResult().toString());
         
         callNode.createRelationshipTo(probeCallNode, ProbeCallRelationshipType.CALLER);
         
         for (Node calleeProbe : call.getCalleeProbes()) {
             callNode.createRelationshipTo(calleeProbe, ProbeCallRelationshipType.CALLEE);
         }
         
         probeCallNode.setProperty(call.getCallType().getProperty(), true);
     }
     private void storeITSICCCall(Call call) {
         Node probeCallNode = call.getCallerProbe();
         Node callNode = createCallNode(call.getTimestamp(), call.getRelatedNodes(), probeCallNode);
 
         
         LinkedHashMap<String, Header> headers = getHeaderMap(CALL_DATASET_HEADER_INDEX).headers;
 
         setIndexProperty(headers, callNode, CallProperties.CALL_TYPE.getId(), call.getCallType().toString());
         setIndexProperty(headers, callNode, CallProperties.CALL_RESULT.getId(), call.getCallResult().toString());
         setIndexProperty(headers, callNode, CallProperties.CC_HANDOVER_TIME.getId(), call.getHandoverTime());
         setIndexProperty(headers, callNode, CallProperties.CC_RESELECTION_TIME.getId(), call.getReselectionTime());
         
         callNode.createRelationshipTo(probeCallNode, ProbeCallRelationshipType.CALLER);
         
         for (Node calleeProbe : call.getCalleeProbes()) {
             callNode.createRelationshipTo(calleeProbe, ProbeCallRelationshipType.CALLEE);
         }
         
         probeCallNode.setProperty(call.getCallType().getProperty(), true);
     }
 
     /**
      * Creates new Call Node
      *
      * @param timestamp timestamp of Call
      * @param relatedNodes list of M node that creates this call
      * @return created Node
      */
     private Node createCallNode(long timestamp, Set<Node> relatedNodes, Node probeCalls) {
         Transaction transaction = neo.beginTx();
         Node result = null;
         try {
             result = neo.createNode();
             result.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.CALL.getId());
             result.setProperty(INeoConstants.PROPERTY_TIMESTAMP_NAME, timestamp);
             String probeName = NeoUtils.getNodeName(probeCalls,neo);
             result.setProperty(INeoConstants.PROPERTY_NAME_NAME, getCallName(probeName, timestamp));
             updateTimestampMinMax(CALL_DATASET_HEADER_INDEX, timestamp);
             index(result);
             
             //index for Probe Calls
             
             MultiPropertyIndex<Long> callIndex = getProbeCallsIndex(probeName);
             callIndex.add(result);
             
             //create relationship to M node
             for (Node mNode : relatedNodes) {
                 result.createRelationshipTo(mNode, ProbeCallRelationshipType.CALL_M);
             }           
 
             //create relationship to Dataset Calls
             NeoUtils.addChild(callDataset, result, lastCallInDataset, neo);
             if (lastCallInDataset == null) {
                 callDataset.createRelationshipTo(result, GeoNeoRelationshipTypes.CHILD);
             }
             else {
                 lastCallInDataset.createRelationshipTo(result, GeoNeoRelationshipTypes.NEXT);
             }
             lastCallInDataset = result;
             
             transaction.success();
         }
         catch (Exception e) {
             NeoCorePlugin.error(null, e);
         }
         finally {
             transaction.finish();
         }
         
         return result;
     }
     /**
      * Returns an index for Probe Calls
      *
      * @param probeCallsName name of Probe Calls
      * @return timestamp index of Probe Calls
      */
     protected MultiPropertyIndex<Long> getProbeCallsIndex(String probeCallsName) {
         MultiPropertyIndex<Long> result = callTimestampIndexes.get(probeCallsName);
         
         if (result == null) {
             Transaction tx = neo.beginTx();
             try {
                 result = NeoUtils.getTimeIndexProperty(probeCallsName);             
                 result.initialize(neo, null);
                 
                 callTimestampIndexes.put(probeCallsName, result);
                 tx.success();
             } catch (IOException e) {
                 tx.failure();
                 throw (RuntimeException)new RuntimeException().initCause(e);
             }
             finally {
                 tx.finish();
             }
         }
         
         return result;
     }    
     public static  String getCallName(String probeName, long timestamp){
         StringBuffer result = new StringBuffer(probeName.split(" ")[0]).append("_").append(timeFormat.format(new Date(timestamp)));
         return result.toString();
     }
     /**
      * Class that calculates a general parameters of Call
      * 
      * @author Lagutko_N
      * @since 1.0.0
      */
     public static class Call {
         private Long acknowlegeTime;
         private Long resivedTime;
         private Long timestamp = null;
         //for ITSI_CC
         private Long handoverTime;
         private Long reselectionTime;
         /*
          * List of Duration Parameters
          */
         private final ArrayList<Long> callParameters = new ArrayList<Long>();
         
         /*
          * Index of Call Setup Begin timestamp parameter 
          */
         private final int callSetupBegin = 0;
         
         /*
          * Index of Call Setup End timestamp parameter
          */
         private final int callSetupEnd = 1;
         
         /*
          * Index of Call Termination Begin timestamp parameter
          */
         private final int callTerminationBegin = 2;
         
         /*
          * Index of Call Termination End timestamp parameter
          */
         private final int callTerminationEnd = 3;
         
         /*
          * Index of last processed parameter
          */
         private int lastProccessedParameter;
         
         /*
          * Type of Call
          */
         private CallProperties.CallResult callResult;
         
         private CallType callType;
         
         /*
          * List of Nodes that creates this call
          */
         private final Set<Node> relatedNodes = new HashSet<Node>();
         
         private Node callerProbe;
         
         private final ArrayList<Node> calleeProbes = new ArrayList<Node>();
         
         /*
          * Listening quality
          */
         private float[] lq = new float[0];
         
         /*
          * Audio delay
          */
         private float[] delay = new float[0];
         
         /**
          * Default constructor
          * 
          * Sets zeros to timestamps
          */
         public Call() {
             callParameters.add(0l);
             callParameters.add(0l);
             callParameters.add(0l);
             callParameters.add(0l);
         }
 
         /**
          * @return Returns the callBeginTime.
          */
         public long getCallSetupBegin() {
             return callParameters.get(callSetupBegin);
         }
 
         /**
          * @param callSetupBeginTime The callBeginTime to set.
          */
         public void setCallSetupBeginTime(long callSetupBeginTime) {
             callParameters.set(this.callSetupBegin, callSetupBeginTime);
             lastProccessedParameter = this.callSetupBegin;
         }
 
         /**
          * @return Returns the callEndTime.
          */
         public long getCallSetupEnd() {
             return callParameters.get(callSetupEnd);
         }
 
         /**
          * @param callSetupEndTime The callEndTime to set.
          */
         public void setCallSetupEndTime(long callSetupEndTime) {
             callParameters.set(this.callSetupEnd, callSetupEndTime);
             lastProccessedParameter = this.callSetupEnd;
         }
 
         public void addRelatedNode(Node mNode) {
             relatedNodes.add(mNode);
         }
         
         public Set<Node> getRelatedNodes() {
             return relatedNodes;
         }
 
         /**
          * @return Returns the callResult.
          */
         public CallProperties.CallResult getCallResult() {
             return callResult;
         }
 
         /**
          * @param callResult The callResult to set.
          */
         public void setCallResult(CallProperties.CallResult callResult) {
             this.callResult = callResult;
         }
 
         /**
          * @return Returns the callTerminationBegin.
          */
         public long getCallTerminationBegin() {
             return callParameters.get(callTerminationBegin);
         }
 
         /**
          * @param callTerminationBegin The callTerminationBegin to set.
          */
         public void setCallTerminationBegin(long callTerminationBegin) {
             callParameters.set(this.callTerminationBegin, callTerminationBegin);
             lastProccessedParameter = this.callTerminationBegin;
         }
 
         /**
          * @return Returns the callTerminationEnd.
          */
         public long getCallTerminationEnd() {
             return callParameters.get(callTerminationEnd);
         }
 
         /**
          * @param callTerminationEnd The callTerminationEnd to set.
          */
         public void setCallTerminationEnd(long callTerminationEnd) {
             callParameters.set(this.callTerminationEnd, callTerminationEnd);
             
             if ((getCallTerminationBegin() == 0) &&
                 (getCallSetupEnd() == 0)) {
                 setCallSetupEndTime(callTerminationEnd);
             }
             
             lastProccessedParameter = this.callTerminationEnd;
         }
         
         /**
          * Handles error
          *
          * @param timestamp
          */
         public void error(long timestamp) {
             switch (lastProccessedParameter) {
             case 0: 
             case 2:
                 //if an error was on beginning of operation than set an end time as time of error
                 callParameters.set(lastProccessedParameter + 1, timestamp);
                 break;
             }
         }
 
         /**
          * @return Returns the lq.
          */
         public float[] getLq() {
             return lq;
         }
 
         /**
          * @param lq The lq to set.
          */
         public void addLq(float lq) {
             this.lq = addToArray(this.lq, lq);
         }
         
         /**
          * Add new element to Array
          *
          * @param original original Array
          * @param value value to add
          * @return changed array
          */
         private float[] addToArray(float[] original, float value) {
             float[] result = new float[original.length + 1];
             result = Arrays.copyOf(this.lq, result.length);
             result[result.length - 1] = value;
             return result;
         }
 
         /**
          * @return Returns the delay.
          */
         public float[] getDelay() {
             return delay;
         }
 
         /**
          * @param delay The delay to set.
          */
         public void addDelay(float delay) {
             this.delay = addToArray(this.delay, delay);
         }
 
         /**
          * @return Returns the callerProbe.
          */
         public Node getCallerProbe() {
             return callerProbe;
         }
 
         /**
          * @param callerProbe The callerProbe to set.
          */
         public void setCallerProbe(Node callerProbe) {
             this.callerProbe = callerProbe;
         }
 
         /**
          * @return Returns the calleeProbes.
          */
         public ArrayList<Node> getCalleeProbes() {
             return calleeProbes;
         }
         
         public void addCalleeProbe(Node calleeProbe) {
 
             if (!calleeProbe.equals(callerProbe) && !calleeProbes.contains(calleeProbe)) {
                 calleeProbes.add(calleeProbe);
             }
         }
         /**
          * Gets the acknowlege time.
          *
          * @return the acknowlege time
          */
         public Long getAcknowlegeTime() {
             return acknowlegeTime;
         }
 
         /**
          * Sets the acknowlege time.
          *
          * @param acknowlegeTime the new acknowlege time
          */
         public void setAcknowlegeTime(Long acknowlegeTime) {
             this.acknowlegeTime = acknowlegeTime;
         }
         /**
          * @return Returns the callType.
          */
         public CallType getCallType() {
             return callType;
         }
 
         /**
          * @param callType The callType to set.
          */
         public void setCallType(CallType callType) {
             this.callType = callType;
         }
 
         public Long getReceivedTime() {
             return resivedTime;
         }
 
         public void setResivedTime(Long resivedTime) {
             this.resivedTime = resivedTime;
         }
 
         /**
          * @param handoverTime The handoverTime to set.
          */
         public void setHandoverTime(Long handoverTime) {
             this.handoverTime = handoverTime;
         }
 
         /**
          * @return Returns the handoverTime.
          */
         public Long getHandoverTime() {
             return handoverTime;
         }
 
         /**
          * @param reselectionTime The reselectionTime to set.
          */
         public void setReselectionTime(Long reselectionTime) {
             this.reselectionTime = reselectionTime;
         }
 
         /**
          * @return Returns the reselectionTime.
          */
         public Long getReselectionTime() {
             return reselectionTime;
         }
 
         /**
          * @return Returns the timestamp.
          */
         public Long getTimestamp() {
             // TODO remove fake after refactoring
             return timestamp == null ? getCallSetupBegin() : timestamp;
         }
 
         /**
          * @param timestamp The timestamp to set.
          */
         public void setTimestamp(Long timestamp) {
             this.timestamp = timestamp;
         }
         
     }
     
     @Override
     protected void finishUpIndexes() {
         for (MultiPropertyIndex<Long> singleIndex : callTimestampIndexes.values()) {
             singleIndex.finishUp();
         }
         super.finishUpIndexes();
     }
     @Override
     public Node[] getRootNodes() {
         return new Node[]{networkNode,datasetNode};
     }
     /**
     *
     * @return
     */
    public Node getVirtualDataset() {
        return callDataset;
    }
 
 }

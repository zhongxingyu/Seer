 package net.floodlightcontroller.ratelimiter;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import net.floodlightcontroller.core.util.AppCookie;
 import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryService;
 import net.floodlightcontroller.routing.*;
 import org.openflow.protocol.OFFlowMod;
 import org.openflow.protocol.OFMatch;
 import org.openflow.protocol.OFPacketIn;
 import org.openflow.protocol.OFPacketOut;
 import org.openflow.protocol.OFPort;
 import org.openflow.protocol.OFType;
 import org.openflow.protocol.action.OFAction;
 import org.openflow.protocol.action.OFActionEnqueue;
 import org.openflow.protocol.action.OFActionType;
 import org.openflow.util.HexString;
 
 import net.floodlightcontroller.core.FloodlightContext;
 import net.floodlightcontroller.core.IFloodlightProviderService;
 import net.floodlightcontroller.core.IOFSwitch;
 import net.floodlightcontroller.core.module.FloodlightModuleContext;
 import net.floodlightcontroller.core.module.FloodlightModuleException;
 import net.floodlightcontroller.counter.ICounterStoreService;
 import net.floodlightcontroller.devicemanager.IDevice;
 import net.floodlightcontroller.devicemanager.IDeviceService;
 import net.floodlightcontroller.devicemanager.SwitchPort;
 import net.floodlightcontroller.forwarding.Forwarding;
 import net.floodlightcontroller.topology.ITopologyService;
 import net.floodlightcontroller.topology.NodePortTuple;
 import net.floodlightcontroller.benchmarkcontroller.IQueueCreaterService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class RateLimiterController extends Forwarding {
 	private Map<Integer, Policy> policyStorage;
 	private Map<Integer, Flow> flowStorage;
 	private Map<Integer, HashSet<Policy>> subSets;
     private static Logger log = LoggerFactory.getLogger(RateLimiterController.class);
     private ILinkDiscoveryService linkService;
     protected IQueueCreaterService queueCreaterService;
     private Map<Long, IOFSwitch> switches;
 
 	public boolean flowBelongsToRule(OFMatch flow, OFMatch rule){
         log.warn("flow: " + flow.toString() + " rule: " + rule.toString());
 
 		int rulewc = rule.getWildcards();
 		if(!(((rulewc & OFMatch.OFPFW_IN_PORT) == OFMatch.OFPFW_IN_PORT) || 
 				rule.getInputPort() == flow.getInputPort()))
 			return false;
 
 		if(!(((rulewc & OFMatch.OFPFW_DL_VLAN) == OFMatch.OFPFW_DL_VLAN) || 
 				rule.getDataLayerVirtualLan() == flow.getDataLayerVirtualLan()))
 			return false;
 
 		if(!(((rulewc & OFMatch.OFPFW_DL_SRC) == OFMatch.OFPFW_DL_SRC) || 
 				Arrays.equals(rule.getDataLayerSource(), flow.getDataLayerSource())))
 			return false;
 
 		if(!(((rulewc & OFMatch.OFPFW_DL_DST) == OFMatch.OFPFW_DL_DST) || 
 				Arrays.equals(rule.getDataLayerDestination(), flow.getDataLayerDestination())))
 			return false;
 
 		if(!(((rulewc & OFMatch.OFPFW_DL_TYPE) == OFMatch.OFPFW_DL_TYPE) || 
 				rule.getDataLayerType() == flow.getDataLayerType()))
 			return false;
 
 		if(!(((rulewc & OFMatch.OFPFW_NW_PROTO) == OFMatch.OFPFW_NW_PROTO) || 
 				rule.getNetworkProtocol() == flow.getNetworkProtocol()))
 			return false;
 
 		if(!(((rulewc & OFMatch.OFPFW_TP_SRC) == OFMatch.OFPFW_TP_SRC) || 
 				rule.getTransportSource() == flow.getTransportSource()))
 			return false;
 
 		if(!(((rulewc & OFMatch.OFPFW_TP_DST) == OFMatch.OFPFW_TP_DST) || 
 				rule.getTransportDestination() == flow.getTransportDestination()))
 			return false;
 		
 		int ruleSrcMask = rule.getNetworkSourceMaskLen();
 		int matchSrcMask = flow.getNetworkSourceMaskLen();
 		log.warn(String.valueOf(ruleSrcMask));
 		log.warn(String.valueOf(matchSrcMask));
 
 		log.warn(String.valueOf((0xffffffff << (3))));
 		log.warn(String.valueOf((1 << (33))));
 
 		
 		if(!(ruleSrcMask <= matchSrcMask &&
 				(rule.getNetworkSource() & ((ruleSrcMask==0)? 0:0xffffffff << (32-ruleSrcMask))) ==
 				(flow.getNetworkSource() & ((ruleSrcMask==0)? 0:0xffffffff << (32-ruleSrcMask)))))
 			return false;
 		log.warn("match src");
 		int ruleDstMask = rule.getNetworkDestinationMaskLen();
 		int matchDstMask = flow.getNetworkDestinationMaskLen();
 		if(!(ruleDstMask <= matchDstMask &&
 				(rule.getNetworkDestination() & ((ruleDstMask==0)? 0:0xffffffff << (32-ruleDstMask))) ==
 				(flow.getNetworkDestination() & ((ruleDstMask==0)? 0:0xffffffff << (32-ruleDstMask)))))
 			return false;
 		log.warn("match dst");
 
 		return true;
 	}
 
 	private Set<Policy> matchPoliciesFromStorage(OFMatch match){
 		Set<Policy> matchedPolicies= new HashSet<Policy>();
 		Iterator itp = policyStorage.values().iterator();
 		while(itp.hasNext()){
 			Policy policytmp = (Policy) itp.next();
 			if(policytmp.flows.contains(Integer.valueOf(match.hashCode())))
 				continue;
 			Iterator itr = policytmp.rules.iterator();
 			while(itr.hasNext()){
 				if(flowBelongsToRule(match, (OFMatch) itr.next())) {
 					matchedPolicies.add(policytmp);
                 }
 			}
 			
 		}
 		return matchedPolicies;
 	}
 
 	private boolean processPacket(IOFSwitch sw, OFPacketIn pi, FloodlightContext cntx){
         IDevice dstDevice =
                 IDeviceService.fcStore.
                         get(cntx, IDeviceService.CONTEXT_DST_DEVICE);
 
         //We can't handle packets with unknown destination
         if (dstDevice == null) {
             return false;
         }
 
         SwitchPort[] dstDaps = dstDevice.getAttachmentPoints();
         if (dstDaps == null) return false;
         SwitchPort dstsw = dstDaps[0];
 
 		OFMatch match = new OFMatch();
 		match.loadFromPacket(pi.getPacketData(), pi.getInPort());
 
 		Set<Policy> policies = matchPoliciesFromStorage(match);
         if(policies.isEmpty()) {
             return false;
         }
 
         NodePortTuple srctuple = new NodePortTuple(sw.getId(), pi.getInPort());
         NodePortTuple dsttuple = new NodePortTuple(dstsw.getSwitchDPID(), dstsw.getPort());
         Flow flow = new Flow(match, srctuple, dsttuple);
 
         for (Policy p : policies) {
             SwitchPort oldsw = p.getSwport();
             if (findNewSwitch(p, flow)) {
                 for (Flow f : p.getFLows()) {
                     //Delete all routes
                     //Delete enqueue
                     SwitchPort newsw = p.getSwport();
                     p.setSwport(oldsw);
                     installMatchedFLowToSwitch(f.getQmatch(p), p, OFFlowMod.OFPFC_DELETE_STRICT);
                     p.setSwport(newsw);
                     //Delete queue
                     //Add new routes and enqueue
                     addRouteAndEnqueue(sw, pi, cntx, p, f);
                 }
             }
 
             addRouteAndEnqueue(sw, pi, cntx, p, flow);
             p.addFlow(flow);
         }
 		return true;
 	}
 
     private void addRouteAndEnqueue(IOFSwitch sw, OFPacketIn pi, FloodlightContext cntx, Policy p, Flow f) {
         OFMatch match = f.getMatch();
         /* Add the route before the queue */
         Route r1_temp = routingEngine.getRoute(f.getSrc().getNodeId(), f.getSrc().getPortId(), p.getDpid(), p.getPort(), 0);
         Route r1 = new Route(r1_temp.getId(), r1_temp.getPath());
         int r1len = r1.getPath().size();
         r1.getPath().remove(r1len-1);
         short qsinport = r1.getPath().get(r1len-2).getPortId();
         r1.getPath().remove(r1len-2);
         NodePortTuple qs = new NodePortTuple(p.getDpid(), p.getPort());
         long cookie1 = AppCookie.makeCookie(FORWARDING_APP_ID, 0);
         pushRoute(r1, match, match.getWildcards(), pi, sw.getId(), cookie1,
                 cntx, false, true, OFFlowMod.OFPFC_MODIFY);
         f.addRoute(r1);
 
         /* Add the route after the queue (if necessary) */
         Set<Link> links = linkService.getPortLinks().get(qs);
         if (links != null) {
             NodePortTuple nexts = null;
             for (Link l : links) {
                 if (l.getSrc() == qs.getNodeId() && l.getSrcPort() == qs.getPortId()) {
                     nexts = new NodePortTuple(l.getDst(), l.getDstPort());
                 }
             }
             Route r2 = routingEngine.getRoute(nexts.getNodeId(), nexts.getPortId(), f.getDst().getNodeId(), f.getDst().getPortId(), 0);
             long cookie2 = AppCookie.makeCookie(FORWARDING_APP_ID, 0);
             pushRoute(r2, match, match.getWildcards(), pi, sw.getId(), cookie2,
                     cntx, false, true, OFFlowMod.OFPFC_MODIFY);
             f.addRoute(r2);
         }
 
         /* Add the enqueue entry */
         OFMatch s2match = match.clone();
         s2match.setInputPort(qsinport);
         installMatchedFLowToSwitch(s2match, p, OFFlowMod.OFPFC_MODIFY_STRICT);
 
         f.addPolicy(p);
         f.setQmatch(p, s2match);
     }
 	
 	private void deletePolicyFromStorage(Set<Policy> policiesToDelete) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/**
 	 * Divide policies into sets according the switches they are installed
 	 * non-installed policies are in one set
 	 * @param policies
 	 * @param flow
 	 * @return
 	 */
     private List<ArrayList<Policy>> dividePolicyBySwitch(Set<Policy> policies, Flow flow) {
 		// TODO Here we simply order the list by switch ID. A better way is to order the switches
     	// in the sequence that flow could travel through in the shortest distance.
     	boolean inserted = false;
     	int i = 0;
     	List<ArrayList<Policy>> policySet = new LinkedList<ArrayList<Policy>>();
     	Iterator itp = policies.iterator();
     	while(itp.hasNext()){
     		Policy p = (Policy) itp.next();
     		Iterator itl = policySet.iterator();
     		ArrayList<Policy> policySameSwitch;
     		while(itl.hasNext()){
     			policySameSwitch = (ArrayList<Policy>) itl.next();
     			if(policySameSwitch.get(0).getDpid() == p.getDpid()){
     				policySameSwitch.add(p);
     				inserted = true;
     			}
     			if(policySameSwitch.get(0).getDpid() < p.getDpid()){
     				i++;
     				continue;
     			}
     			else{
     				i--;
     				break;
     			}
     		}
     		if(inserted == false){
     			policySet.add(i, new ArrayList<Policy>());
     		}else{
     			inserted = false;
     		}
     	}
 		return policySet;
 	}
 
     /**
      * Process and update the flow and the matching policies.
      * @param flow
      * @param policies
      * @return
      */
 	private Set<Policy> processFlowWithPolicy(Flow flow, Set<Policy> policies) {
 		// TODO Auto-generated method stub
 		Set<Policy> policiesToDelete = new HashSet<Policy>();
 		List<ArrayList<Policy>> policySet = dividePolicyBySwitch(policies, flow);
 
 		//List<Integer> switches = getSwitchByPolicy(policySet);
 		
 		// Add sets of policies to the new flow
 		Iterator it = policySet.iterator();
 		while(it.hasNext()){
 			ArrayList<Policy> policySameSwitch = (ArrayList<Policy>) it.next();
 			if(policySameSwitch.get(0).getSwport() != null){
 				// TODO We could apply some strategies here to decide which policy to stay in the same switch
 				Policy p = policySameSwitch.get(0);
 				updatePolicyWithFlow(flow, p);
 				flow.addPolicy(p);
 				int i = 1;
 				int size = policySameSwitch.size();
 				while(i<size){
 					p = policySameSwitch.get(i);
 					updatePolicyWithFlow(flow, p);
 					if(findNewSwitch(p, flow) == false){
 						policiesToDelete.add(p);
 						deleteFlowFromPolicy(p, flow);
 					} else {
 						flow.addPolicy(p);
 					}
 					i++;
 				}
 			} else {
                 Route[] routes = (Route[]) flow.getRoutes().toArray();
 				Route route = routes[0];
 				int i = 0;
 				int size = policySameSwitch.size();
 				while(i<size){
 					Policy p = policySameSwitch.get(i);
 					if(findNewSwitch(p, route) == false){
 						policiesToDelete.add(p);
 					}else{
 						updatePolicyWithFlow(flow, p);
 						flow.addPolicy(p);
 					}
 					i++;
 				}
 			}
 		}
 		if(!flow.getPolicies().isEmpty()) {
 			flowStorage.put(flow.hashCode(), flow);
 		}
 		// we can also implement a optimization here to determine
 		// whether the switches are affecting the route of flow too much
 		return policiesToDelete;
 	}
 	
 	private void installMatchedFLowToSwitch(OFMatch flow, Policy p, short flowModCommand){
         IOFSwitch sw = switches.get(p.getDpid());
         if(flowModCommand != OFFlowMod.OFPFC_DELETE_STRICT) {
             queueCreaterService.createQueue(sw, p.getPort(), p.queue, p.speed);
         } else {
             queueCreaterService.deleteQueue(sw, p.getPort(), p.queue);
         }
 
         OFFlowMod fm = new OFFlowMod();
         fm.setType(OFType.FLOW_MOD);
 
 
         List<OFAction> actions = new ArrayList<OFAction>();
 
         //add the queuing action
         OFActionEnqueue enqueue = new OFActionEnqueue();
         enqueue.setLength((short)OFActionEnqueue.MINIMUM_LENGTH);
         enqueue.setType(OFActionType.OPAQUE_ENQUEUE); // I think this happens anyway in the constructor
         enqueue.setPort(p.getPort());
         enqueue.setQueueId(p.queue);
         actions.add(enqueue);
 
         fm.setMatch(flow)
             .setCommand(flowModCommand)
             .setActions(actions)
             .setIdleTimeout((short)5)  // infinite
             .setHardTimeout((short) 0)  // infinite
             .setBufferId(OFPacketOut.BUFFER_ID_NONE)
             .setFlags((short) 0)
             .setOutPort(OFPort.OFPP_NONE.getValue())
             .setPriority(p.priority)
             .setLengthU(OFFlowMod.MINIMUM_LENGTH+OFActionEnqueue.MINIMUM_LENGTH);
         try {
             sw.write(fm, null);
             sw.flush();
         } catch (IOException e) {
             log.error("Tried to write OFFlowMod to {} but failed: {}",
                     HexString.toHexString(sw.getId()), e.getMessage());
         }
 
 				
 	}
 
 	private boolean findNewSwitch(Policy p, Route route) {
 		// TODO Auto-generated method stub
 		List<NodePortTuple> path = route.getPath();
 		return false;
 	}
 
 	private void deleteFlowFromPolicy(Policy p, Flow flow) {
 		// TODO Auto-generated method stub
 		
 	}
 
     /* This function checks if the policy needs to change the switch */
 	private boolean findNewSwitch(Policy p, Flow flow) {
         NodePortTuple src, dst, next;
         src = flow.getSrc();
         dst = flow.getDst();
         Route r = routingEngine.getRoute(src.getNodeId(), src.getPortId(), dst.getNodeId(), dst.getPortId(), 0);
         if (r == null) {
             /* TODO There is no route for this flow, should return error */
             return false;
         } else if (p.getFLows().isEmpty()) {
             /* If this is the first flow matched by this policy, we add a queue at the first hop */
             next = r.getPath().get(1);
             p.setSwport(new SwitchPort(next.getNodeId(), next.getPortId()));
             return true;
         } else {
             NodePortTuple qs = new NodePortTuple(p.getDpid(), p.getPort());
             if (r.getPath().contains(qs)) {
                 /* If the new flow's default route contains the queue switch, there is no need to change it */
                 return false;
             } else {
                 Route nextr = routingEngine.getRoute(p.getDpid(), p.getPort(), dst.getNodeId(), dst.getPortId(), 0);
                 int inorout = 0;
                 for (NodePortTuple np : nextr.getPath()) {
                     if (inorout == 0) {
                         inorout = 1;
                         continue;
                     } else {
                         if (r.getPath().contains(np)) {
                             p.setSwport(new SwitchPort(np.getNodeId(), np.getPortId()));
                             return true;
                         }
                         inorout = 0;
                     }
                 }
                 /* TODO If there's no overlapped switch, we need to find a new switch that can satisfy all flows (IMPORTANT!!!) */
             }
             return false;
         }
         /*
         if (flow.match.getNetworkSource() == 167772162) {
             IOFSwitch s1 = switches.get((long) 1);
             NodePortTuple s2tuple = new NodePortTuple(s1.getId(), (short) 2);
             p.setSwport(new SwitchPort(s2tuple.getNodeId(), s2tuple.getPortId()));
             return true;
         } else {
             IOFSwitch s2 = switches.get((long) 2);
             NodePortTuple s2tuple = new NodePortTuple(s2.getId(), (short) 2);
             p.setSwport(new SwitchPort(s2tuple.getNodeId(), s2tuple.getPortId()));
             return false;
         }
         */
 	}
 
 	/**
 	 * Add new flow information to the policy
 	 * @param flow
 	 * @param p
 	 */
 	private void updatePolicyWithFlow(Flow flow, Policy p) {
 		// TODO Auto-generated method stub
 		
 	}
 
     @Override
     protected void doForwardFlow(IOFSwitch sw, OFPacketIn pi, FloodlightContext cntx, boolean requestFlowRemovedNotifn) {
         if (processPacket(sw, pi, cntx)) return;
         else super.doForwardFlow(sw, pi, cntx, requestFlowRemovedNotifn);
     }
     
     public void init(FloodlightModuleContext context) throws FloodlightModuleException {
         super.init();
         this.floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
         this.deviceManager = context.getServiceImpl(IDeviceService.class);
         this.routingEngine = context.getServiceImpl(IRoutingService.class);
         this.topology = context.getServiceImpl(ITopologyService.class);
         this.counterStore = context.getServiceImpl(ICounterStoreService.class);
         this.linkService = context.getServiceImpl(ILinkDiscoveryService.class);
         this.queueCreaterService = context.getServiceImpl(IQueueCreaterService.class);
         this.policyStorage = new HashMap<Integer, Policy>();
     	this.flowStorage = new HashMap<Integer, Flow>();
     	
         switches = floodlightProvider.getSwitches();
     	
         OFMatch temp_match = new OFMatch();
         temp_match.setWildcards(~(OFMatch.OFPFW_NW_DST_MASK));
         temp_match.setNetworkDestination(167772164);
 
         Set<OFMatch> temp_policyset = new HashSet<OFMatch>();
         temp_policyset.add(temp_match);
         Policy temp_policy = new Policy(temp_policyset, (short)1);
         temp_policy.setQueue(1);
         policyStorage.put(Integer.valueOf(temp_policy.hashCode()), temp_policy);
 
         // read our config options
         Map<String, String> configOptions = context.getConfigParams(this);
         try {
             String idleTimeout = configOptions.get("idletimeout");
             if (idleTimeout != null) {
                 FLOWMOD_DEFAULT_IDLE_TIMEOUT = Short.parseShort(idleTimeout);
             }
         } catch (NumberFormatException e) {
             log.warn("Error parsing flow idle timeout, " +
             		 "using default of {} seconds",
                      FLOWMOD_DEFAULT_IDLE_TIMEOUT);
         }
         try {
             String hardTimeout = configOptions.get("hardtimeout");
             if (hardTimeout != null) {
                 FLOWMOD_DEFAULT_HARD_TIMEOUT = Short.parseShort(hardTimeout);
             }
         } catch (NumberFormatException e) {
             log.warn("Error parsing flow hard timeout, " +
             		 "using default of {} seconds",
                      FLOWMOD_DEFAULT_HARD_TIMEOUT);
         }
         log.debug("FlowMod idle timeout set to {} seconds", 
                   FLOWMOD_DEFAULT_IDLE_TIMEOUT);
         log.debug("FlowMod hard timeout set to {} seconds", 
                   FLOWMOD_DEFAULT_HARD_TIMEOUT);
     }
 }

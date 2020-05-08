 package edu.wisc.cs.project.secure;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 
 import net.floodlightcontroller.core.IOFSwitch;
 
 import org.openflow.protocol.OFFlowMod;
 import org.openflow.protocol.action.OFAction;
 import org.openflow.protocol.action.OFActionDataLayerDestination;
 import org.openflow.protocol.action.OFActionDataLayerSource;
 import org.openflow.protocol.action.OFActionEnqueue;
 import org.openflow.protocol.action.OFActionNetworkLayerDestination;
 import org.openflow.protocol.action.OFActionNetworkLayerSource;
 import org.openflow.protocol.action.OFActionNetworkTypeOfService;
 import org.openflow.protocol.action.OFActionOutput;
 import org.openflow.protocol.action.OFActionTransportLayerDestination;
 import org.openflow.protocol.action.OFActionTransportLayerSource;
 import org.openflow.protocol.action.OFActionVendor;
 import org.openflow.protocol.action.OFActionVirtualLanIdentifier;
 import org.openflow.protocol.action.OFActionVirtualLanPriorityCodePoint;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Secure {
 	
 	protected static Logger logger = LoggerFactory.getLogger(Secure.class);
 	
 	private static HashMap<Long, HashSet<Alias>> aliasSet = new HashMap<Long, HashSet<Alias>>();
 	
 	/**
 	 * This function is used in OFSwitchBase to check rules in the
 	 * write functions to make sure the switch should get the rule
 	 * 
 	 * @param rule - the rule to be written to the switch
 	 * @param sw - the switch that is trying to write the rule, this way
 	 * 				a view of the switch's current rules can be constructed
 	 * @return - true or false, if the rule is allowed to be written or not
 	 */
 	
 	public static boolean checkFlowRule(OFFlowMod rule, IOFSwitch sw){
 		// If there are no rules in the flow table, add this one
 		if(aliasSet.get(sw.getId()) == null){
 			HashSet<Alias> aliases = new HashSet<Alias>();
 			aliases.add(new Alias(rule));
 			aliasSet.put(sw.getId(), aliases);
 			return true;
 		}
 		
 		HashSet<Alias> aliases = aliasSet.get(sw.getId());
 		for(Alias alias : aliases){
 			// pairwise comparison of current flow table rules
 			// with the candidate rule
 			
 			if(checkActions(rule.getActions(), alias.getActions()) == true){
 				// Actions are the same so add the rule alias to the set
 				if(aliasSet.get(sw.getId()).add(new Alias(rule)) == true){
 					return true;
 				}
 				else{
 					// alias wasn't able to be added to the set
 					// this means it is already in the flow table
 					// so don't bother writing it out to the switch again
 					return false;
 				}
 			}
 			
 			// TODO actions aren't the same so need to compare the rules
 			
 			// TODO time for some real work!
 			
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Check to see if the lists of actions for the two rules are equal
 	 * 
	 * @param cAction - list of candidate actions
	 * @param fActions - list of actions from rule already in the flow table
	 * @return true or false depending on if the lists are equal to each other
 	 */
 	
 	private static boolean checkActions(List<OFAction> cActions, List<OFAction> fActions){
 		
 		ArrayList<OFAction> currentFlowActions = new ArrayList<OFAction>(fActions);
 		// If they aren't the same size they can't be the same action as a whole
 		if(cActions.size() != fActions.size()){
 			return false;
 		}
 		
 		// check types and actions for each
 		for(OFAction cAction : cActions){
 			boolean foundSameType = false;
 			for(int i = 0; i < currentFlowActions.size(); i++){
 				if(cAction.getType() == currentFlowActions.get(i).getType()){
 					foundSameType = true;
 					// continue checking inside since they have the same type
 					if(checkInnerAction(cAction, currentFlowActions.get(i))){
 						// then the inner actions are the same
 						// get rid of the action in the current rule set
 						currentFlowActions.remove(i);
 						break;
 					}
 					else {
 						// the inner actions are not the same
 						// therefore need to check the whole rule
 						return false;
 					}
 				}
 			}
 			if(!foundSameType){
 				// There were no fActions found that have the same
 				// OFActionType as cAction, therefore these actions are
 				// not the same, short-circuit the search
 				return false;
 			}
 		}
 	
 		// Everything checks out to be the same
 		return true;
 	}
 	
 	/**
 	 * Takes two actions and compares them to see if they are equal or not
 	 * 
 	 * @param cAction - candidate action
 	 * @param fAction - action already in the flow table
 	 * @return - true or false depending on if the actions are equal
 	 */
 	
 	private static boolean checkInnerAction(OFAction cAction, OFAction fAction){
 		
 		// Just a sanity check, these should be the same by the time they
 		// get here
 		if(cAction.getType() != fAction.getType()){
 			return false;
 		}
 		
 		switch(cAction.getType()){
 			case OUTPUT:
 				if(((OFActionOutput)cAction).getPort() == ((OFActionOutput)fAction).getPort()){
 					return true;
 				}
 				else {
 					return false;
 				}
 			case SET_VLAN_ID:
 				if(((OFActionVirtualLanIdentifier)cAction).getVirtualLanIdentifier() == 
 						((OFActionVirtualLanIdentifier)fAction).getVirtualLanIdentifier()){
 					return true;
 				}
 				else {
 					return false;
 				}
 			case SET_VLAN_PCP:
 				if(((OFActionVirtualLanPriorityCodePoint)cAction).getVirtualLanPriorityCodePoint() == 
 						((OFActionVirtualLanPriorityCodePoint)fAction).getVirtualLanPriorityCodePoint()) {
 					return true;
 				}
 				else {
 					return false;
 				}
 			case STRIP_VLAN:
 				// TODO Not sure how to handle this action
 				return true;
 			case SET_DL_SRC:				
 				if(Arrays.equals(((OFActionDataLayerSource)cAction).getDataLayerAddress(),
 						((OFActionDataLayerSource)fAction).getDataLayerAddress())){
 					return true;
 				}
 				else {
 					return false;
 				}
 			case SET_DL_DST:		
 				if(Arrays.equals(((OFActionDataLayerDestination)cAction).getDataLayerAddress(),
 						((OFActionDataLayerDestination)fAction).getDataLayerAddress())){
 					return true;
 				}
 				else {
 					return false;
 				}
 			case SET_NW_SRC:
 				if(((OFActionNetworkLayerSource)cAction).getNetworkAddress() == 
 						((OFActionNetworkLayerSource)fAction).getNetworkAddress()){
 					return true;
 				}
 				else {
 					return false;
 				}
 			case SET_NW_DST:
 				if(((OFActionNetworkLayerDestination)cAction).getNetworkAddress() == 
 						((OFActionNetworkLayerDestination)fAction).getNetworkAddress()){
 					return true;
 				}
 				else {
 					return false;
 				}
 			case SET_NW_TOS:
 				if(((OFActionNetworkTypeOfService)cAction).getNetworkTypeOfService() == 
 						((OFActionNetworkTypeOfService)fAction).getNetworkTypeOfService()){
 					return true;
 				}
 				else {
 					return false;
 				}
 			case SET_TP_SRC:
 				if(((OFActionTransportLayerSource)cAction).getTransportPort() == 
 						((OFActionTransportLayerSource)fAction).getTransportPort()){
 					return true;
 				}
 				else {
 					return false;
 				}
 			case SET_TP_DST:
 				if(((OFActionTransportLayerDestination)cAction).getTransportPort() == 
 						((OFActionTransportLayerDestination)fAction).getTransportPort()){
 					return true;
 				}
 				else {
 					return false;
 				}
 			case OPAQUE_ENQUEUE:
 				if(((OFActionEnqueue)cAction).getPort() == ((OFActionEnqueue)fAction).getPort() &&
 						((OFActionEnqueue)cAction).getQueueId() == ((OFActionEnqueue)fAction).getQueueId()){
 					return true;
 				}
 				else {
 					return false;
 				}
 			case VENDOR:
 				if(((OFActionVendor)cAction).getVendor() == ((OFActionVendor)fAction).getVendor()){
 					return true;
 				}
 				else {
 					return false;
 				}
 		}
 		
 		// This acts as a default
 		// At the moment, if we don't know what it is, then allow it
 		return true;
 	}
 	
 }

 package it.polimi.elet.selflet.nodeState;
 
 import static it.polimi.elet.selflet.message.SelfLetMessageTypeEnum.*;
 import it.polimi.elet.selflet.collectionUtils.CollectionUtil;
 import it.polimi.elet.selflet.configuration.DispatcherConfiguration;
 import it.polimi.elet.selflet.id.ISelfLetID;
 import it.polimi.elet.selflet.message.ISelfletNeighbors;
 import it.polimi.elet.selflet.message.MessageBridge;
 import it.polimi.elet.selflet.message.RedsMessage;
 import it.polimi.elet.selflet.message.SelfLetMsg;
 import it.polimi.elet.selflet.message.SelfletNeighbors;
 import it.polimi.elet.selflet.negotiation.nodeState.INodeState;
 import it.polimi.elet.selflet.threadUtilities.IPeriodicTask;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TimerTask;
 
 import org.apache.log4j.Logger;
 
 import com.google.common.collect.Sets;
 
 /**
  * This class sends a message to every known selflet to update their knowledge
  * about the neighbors
  * 
  * @author Nicola Calcavecchia <calcavecchia@gmail.com>
  * */
 public class NeighborSender extends TimerTask implements IPeriodicTask {
 
 	private static final Logger LOG = Logger.getLogger(NeighborSender.class);
 
 	private static final int MAX_NEIGHBORS_PER_SELFLET = DispatcherConfiguration.maxNeighborsPerSelflet;
 	private static final int SLEEP_TIME = DispatcherConfiguration.neighborsUpdaterSleepPeriodInSec * 1000;
 
 	private final MessageBridge messageBridge = MessageBridge.getInstance();
 	private final INodeStateManager nodeStateManager = NodeStateManager
 			.getInstance();
 	private final ISelfletNeighbors selfletNeighbors = SelfletNeighbors
 			.getInstance();
 	Map<ISelfLetID, Set<ISelfLetID>> neighborsToAddMap;
 	Map<ISelfLetID, Set<ISelfLetID>> neighborsToRemoveMap;
 
 	@Override
 	public void run() {
 		Set<ISelfLetID> knownSelflets = selfletNeighbors.getNeighbors();
 		neighborsToAddMap = new HashMap<ISelfLetID, Set<ISelfLetID>>();
 		neighborsToRemoveMap = new HashMap<ISelfLetID, Set<ISelfLetID>>();
 		LOG.debug("Known selflets: " + knownSelflets.size());
 		if (knownSelflets.size() > 1) {
 			initializeNeighborsMaps(knownSelflets);
 			mapNeighborsToAdd(knownSelflets);
 			// TODO By now the removal of an existing neighbor is disabled to
 			// prevent undesired behavior. The case where all the selflets have a
			// full neighborhood is handled instantiating a new selflet with all
 			// the services. The change in the neighborhood should be studied
 			// carefully.
 			// mapNeighborsToChange(knownSelflets);
 			sendNeighborsMessagesIfNeeded();
 		} else {
 			LOG.debug("Number of known selflets < 2; no need to send neighbors");
 		}
 	}
 
 	private void initializeNeighborsMaps(Set<ISelfLetID> knownSelflets) {
 		for (ISelfLetID selflet : knownSelflets) {
 			Set<ISelfLetID> newNeighborsToAdd = new HashSet<ISelfLetID>();
 			Set<ISelfLetID> newNeighborsToRemove = new HashSet<ISelfLetID>();
 			neighborsToAddMap.put(selflet, newNeighborsToAdd);
 			neighborsToRemoveMap.put(selflet, newNeighborsToRemove);
 		}
 	}
 
 	private void mapNeighborsToAdd(Set<ISelfLetID> knownSelflets) {
 		for (ISelfLetID selflet : knownSelflets) {
 			LOG.debug("selflet: " + selflet);
 			Set<ISelfLetID> neighborsToAdd = getNeighborsOfSelfletToAdd(
 					selflet, knownSelflets);
 			if (!neighborsToAdd.isEmpty()) {
 				updateNeighborsToAddMapping(selflet, neighborsToAdd);
 			}
 		}
 	}
 
 	private void mapNeighborsToChange(Set<ISelfLetID> knownSelflets) {
 		Set<ISelfLetID> lonelySelflets = getLonelySelflets(knownSelflets);
 		for (ISelfLetID lonelySelflet : lonelySelflets) {
 			LOG.debug("lonely selflet: " + lonelySelflet);
 			if (nodeStateManager.haveStateOfSelflet(lonelySelflet)) {
 				ISelfLetID myNewNeighbor = getSmartestSelfletFromSet(knownSelflets);
 				LOG.debug("---smartest selflet: " + myNewNeighbor);
 				INodeState nodestateOfmyNewNeighbor = nodeStateManager
 						.getNodeState(myNewNeighbor);
 				LOG.debug("---neighbors of selflet: "
 						+ nodestateOfmyNewNeighbor.getKnownNeighbors().size());
 				ISelfLetID oldNeighbotToRemove = getSmartestSelfletFromSet(nodestateOfmyNewNeighbor
 						.getKnownNeighbors());
 				LOG.debug("---neighbor to remove: " + oldNeighbotToRemove);
 				updateNeighborsToAddMapping(lonelySelflet, myNewNeighbor);
 				updateNeighborsToRemoveMapping(myNewNeighbor,
 						oldNeighbotToRemove);
 			}
 		}
 	}
 
 	private Set<ISelfLetID> getLonelySelflets(Set<ISelfLetID> selflets) {
 		Set<ISelfLetID> lonelySelflets = Sets.newHashSet();
 		for (ISelfLetID selflet : selflets) {
 			if (isSelfletLonely(selflet)) {
 				lonelySelflets.add(selflet);
 			}
 		}
 		return lonelySelflets;
 	}
 
 	private boolean isSelfletLonely(ISelfLetID selflet) {
 		if (nodeStateManager.haveStateOfSelflet(selflet)) {
 			INodeState nodestate = nodeStateManager.getNodeState(selflet);
 			return nodestate.getKnownNeighbors().isEmpty()
 					&& neighborsToAddMap.get(selflet).isEmpty();
 		} else {
 			return false;
 		}
 	}
 
 	private ISelfLetID getSmartestSelfletFromSet(Set<ISelfLetID> setOfSelflets) {
 		ISelfLetID smartestSelflet = null;
 		int maximumNumberOfKnownServices = 0;
 		INodeState nodestate;
 		for (ISelfLetID selflet : setOfSelflets) {
 			if (nodeStateManager.haveStateOfSelflet(selflet)) {
 				nodestate = nodeStateManager.getNodeState(selflet);
 				int numberOfServicesKnownBySelflet = nodestate
 						.getAvailableServices().size();
 				if (numberOfServicesKnownBySelflet > maximumNumberOfKnownServices) {
 					maximumNumberOfKnownServices = numberOfServicesKnownBySelflet;
 					smartestSelflet = selflet;
 				}
 			}
 		}
 
 		if (smartestSelflet != null) {
 			return smartestSelflet;
 		} else {
 			LOG.error("All SelfLets in the set are dummy. Returning a random SelfLet from set");
 			return getRandomSelfletFromSet(setOfSelflets);
 		}
 	}
 
 	private ISelfLetID getRandomSelfletFromSet(Set<ISelfLetID> setOfSelflets) {
 		List<ISelfLetID> listOfSelflets = new ArrayList<ISelfLetID>(
 				setOfSelflets);
 		Collections.shuffle(listOfSelflets);
 		return listOfSelflets.get(0);
 	}
 
 	private Set<ISelfLetID> getNeighborsOfSelfletToAdd(ISelfLetID selflet,
 			Set<ISelfLetID> allSelflets) {
 		if (nodeStateManager.haveStateOfSelflet(selflet)) {
 			INodeState nodestate = nodeStateManager.getNodeState(selflet);
 			Set<ISelfLetID> neighborsOfSelflet = nodestate.getKnownNeighbors();
 			Set<ISelfLetID> availableNeighbors = Sets.newHashSet(allSelflets);
 			int availableSlotsOfSelflet = getAvailableSlotOfSelflet(selflet);
 			LOG.debug("---available slots: " + availableSlotsOfSelflet);
 
 			if (!neighbohroodIsFull(selflet) && availableSlotsOfSelflet > 0) {
 				Set<ISelfLetID> notGoodNeighbors = Sets.newHashSet();
 				notGoodNeighbors.add(selflet);
 				notGoodNeighbors.addAll(neighborsToAddMap.get(selflet));
 				notGoodNeighbors.addAll(neighborsOfSelflet);
 				notGoodNeighbors
 						.addAll(getFullNeighborsFromSet(availableNeighbors));
 				if (isDummySelflet(selflet)) {
 					notGoodNeighbors
 							.addAll(getDummyNeighborsFromSet(availableNeighbors));
 				}
 				Set<ISelfLetID> possibleNeighborsToAdd = Sets.difference(
 						availableNeighbors, notGoodNeighbors);
 				Set<ISelfLetID> neighborsToAdd = CollectionUtil
 						.extractAtMostNElementsFromSet(possibleNeighborsToAdd,
 								availableSlotsOfSelflet);
 				LOG.debug("---neighbors to add: " + neighborsToAdd.size());
 				return neighborsToAdd;
 			} else {
 				return Sets.newHashSet();
 			}
 		} else {
 			return Sets.newHashSet();
 		}
 	}
 
 	private boolean neighbohroodIsFull(ISelfLetID selflet) {
 		if (nodeStateManager.haveStateOfSelflet(selflet)) {
 			INodeState nodestate = nodeStateManager.getNodeState(selflet);
 			Set<ISelfLetID> neighborsOfSelflet = nodestate.getKnownNeighbors();
 			int numberOfNeighbors = neighborsOfSelflet.size();
 			return numberOfNeighbors >= MAX_NEIGHBORS_PER_SELFLET;
 		} else {
 			return true;
 		}
 	}
 
 	private Set<ISelfLetID> getFullNeighborsFromSet(
 			Set<ISelfLetID> possibleNeighborsOfSelflet) {
 		Set<ISelfLetID> fullNeighbors = Sets.newHashSet();
 		for (ISelfLetID neighbor : possibleNeighborsOfSelflet) {
 			if (neighbohroodIsFull(neighbor)) {
 				fullNeighbors.add(neighbor);
 			}
 		}
 		return fullNeighbors;
 	}
 
 	private Set<ISelfLetID> getDummyNeighborsFromSet(
 			Set<ISelfLetID> possibleNeighborsOfSelflet) {
 		Set<ISelfLetID> dummyNeighbors = Sets.newHashSet();
 		for (ISelfLetID neighbor : possibleNeighborsOfSelflet) {
 			if (isDummySelflet(neighbor)) {
 				dummyNeighbors.add(neighbor);
 			}
 		}
 		return dummyNeighbors;
 	}
 
 	private boolean isDummySelflet(ISelfLetID selflet) {
 		if (nodeStateManager.haveStateOfSelflet(selflet)) {
 			INodeState nodestate = nodeStateManager.getNodeState(selflet);
 			return nodestate.getAvailableServices().isEmpty();
 		} else {
 			return true;
 		}
 	}
 
 	private int getAvailableSlotOfSelflet(ISelfLetID selflet) {
 		return MAX_NEIGHBORS_PER_SELFLET
 				- nodeStateManager.getNodeState(selflet).getKnownNeighbors()
 						.size() - neighborsToAddMap.get(selflet).size();
 	}
 
 	private void updateNeighborsToAddMapping(ISelfLetID selflet,
 			Set<ISelfLetID> neighborsToAdd) {
 		Set<ISelfLetID> updatedSet = neighborsToAddMap.get(selflet);
 		updatedSet.addAll(neighborsToAdd);
 		for (ISelfLetID newNeighbor : neighborsToAdd) {
 			updatedSet = neighborsToAddMap.get(newNeighbor);
 			updatedSet.add(selflet);
 			neighborsToAddMap.put(newNeighbor, updatedSet);
 
 		}
 	}
 
 	private void updateNeighborsToAddMapping(ISelfLetID selflet,
 			ISelfLetID neighborToAdd) {
 		updateNeighborsToAddMapping(selflet, Sets.newHashSet(neighborToAdd));
 	}
 
 	private void updateNeighborsToRemoveMapping(ISelfLetID selflet,
 			Set<ISelfLetID> neighborsToAdd) {
 		Set<ISelfLetID> updatedSet = neighborsToRemoveMap.get(selflet);
 		updatedSet.addAll(neighborsToAdd);
 		for (ISelfLetID newNeighbor : neighborsToAdd) {
 			updatedSet = neighborsToRemoveMap.get(newNeighbor);
 			updatedSet.add(selflet);
 			neighborsToRemoveMap.put(newNeighbor, updatedSet);
 
 		}
 	}
 
 	private void updateNeighborsToRemoveMapping(ISelfLetID selflet,
 			ISelfLetID neighborToAdd) {
 		updateNeighborsToRemoveMapping(selflet, Sets.newHashSet(neighborToAdd));
 	}
 
 	private void sendNeighborsMessagesIfNeeded() {
 		for (ISelfLetID selflet : neighborsToRemoveMap.keySet()) {
 			if (!neighborsToRemoveMap.get(selflet).isEmpty()) {
 				sendNeighborsToRemove(selflet,
 						neighborsToRemoveMap.get(selflet));
 			}
 		}
 
 		for (ISelfLetID selflet : neighborsToAddMap.keySet()) {
 			if (!neighborsToAddMap.get(selflet).isEmpty()) {
 				sendNewNeighbors(selflet, neighborsToAddMap.get(selflet));
 			}
 		}
 
 	}
 
 	private void sendNewNeighbors(ISelfLetID selflet,
 			Set<ISelfLetID> newNeighbors) {
 		LOG.info("sending neighbor to add to selflet: " + selflet);
 		RedsMessage neighborMessage = prepareNeighborMessage(selflet,
 				newNeighbors);
 
 		messageBridge.publish(neighborMessage);
 	}
 
 	private RedsMessage prepareNeighborMessage(ISelfLetID receiver,
 			Set<ISelfLetID> neighbors) {
 
 		SelfLetMsg selfletMsg = new SelfLetMsg(MessageBridge.THIS_SELFLET_ID,
 				receiver, NEIGHBORS, (Serializable) neighbors);
 
 		return new RedsMessage(selfletMsg, receiver.getID().toString());
 	}
 
 	private void sendNeighborsToRemove(ISelfLetID selflet,
 			Set<ISelfLetID> neighborsToRemove) {
 		LOG.info("sending neighbor to remove to selflet: " + selflet);
 		Set<RedsMessage> removeNeighborsMessages = prepareRemoveNeighborMessage(
 				selflet, neighborsToRemove);
 
 		for (RedsMessage removeNeighborsMessage : removeNeighborsMessages) {
 			messageBridge.publish(removeNeighborsMessage);
 		}
 	}
 
 	private Set<RedsMessage> prepareRemoveNeighborMessage(ISelfLetID receiver,
 			Set<ISelfLetID> neighbors) {
 
 		Set<RedsMessage> neighborsToRemoveMessages = Sets.newHashSet();
 		for (ISelfLetID neighbor : neighbors) {
 			SelfLetMsg selfletMsg = new SelfLetMsg(neighbor, receiver,
 					REMOVE_SELFLET, "");
 			neighborsToRemoveMessages.add(new RedsMessage(selfletMsg, receiver
 					.getID().toString()));
 		}
 
 		return neighborsToRemoveMessages;
 	}
 
 	@Override
 	public int periodInMsec() {
 		return SLEEP_TIME;
 	}
 
 }

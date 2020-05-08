 /*
  * Copyright (c) 2012-2013 Open Source Community - <http://www.peerfact.org>
  * Copyright (c) 2011-2012 University of Paderborn - UPB
  * Copyright (c) 2005-2011 KOM - Multimedia Communications Lab
  *
  * This file is part of PeerfactSim.KOM.
  * 
  * PeerfactSim.KOM is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  * 
  * PeerfactSim.KOM is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with PeerfactSim.KOM.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package org.peerfact.impl.overlay.unstructured.zeroaccess.components;
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.peerfact.api.common.ConnectivityEvent;
 import org.peerfact.api.common.Message;
 import org.peerfact.api.common.Operation;
 import org.peerfact.api.common.OperationCallback;
 import org.peerfact.api.network.Bandwidth;
 import org.peerfact.api.network.NetLayer;
 import org.peerfact.api.overlay.NeighborDeterminator;
 import org.peerfact.api.overlay.OverlayContact;
 import org.peerfact.api.overlay.unstructured.HomogeneousOverlayNode;
 import org.peerfact.api.transport.TransInfo;
 import org.peerfact.api.transport.TransLayer;
 import org.peerfact.api.transport.TransMessageCallback;
 import org.peerfact.api.transport.TransMessageListener;
 import org.peerfact.impl.network.gnp.GnpNetLayer;
 import org.peerfact.impl.overlay.AbstractOverlayNode;
 import org.peerfact.impl.overlay.unstructured.zeroaccess.message.BaseMessage;
 import org.peerfact.impl.overlay.unstructured.zeroaccess.message.GetLMessage;
 import org.peerfact.impl.overlay.unstructured.zeroaccess.message.RetLMessage;
 import org.peerfact.impl.overlay.unstructured.zeroaccess.operation.GetLOperation;
 import org.peerfact.impl.overlay.unstructured.zeroaccess.operation.RetLOperation;
 import org.peerfact.impl.overlay.unstructured.zeroaccess.operation.ScheduleGetLOperation;
 import org.peerfact.impl.simengine.Simulator;
 import org.peerfact.impl.transport.TransMsgEvent;
 
 /**
  * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  * This part of the Simulator is not maintained in the current version of
  * PeerfactSim.KOM. There is no intention of the authors to fix this
  * circumstances, since the changes needed are huge compared to overall benefit.
  * 
  * If you want it to work correctly, you are free to make the specific changes
  * and provide it to the community.
  * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! !!!!!!!!!!!!!!!!!!!!!!!!!!!!
  * 
  * 
  * @author <info@peerfact.org>
  * @version 05/06/2011
  * 
  */
 public class ZeroAccessOverlayNode extends
 		AbstractOverlayNode<ZeroAccessOverlayID, ZeroAccessOverlayContact>
 		implements
 		HomogeneousOverlayNode<ZeroAccessOverlayID, ZeroAccessOverlayContact>,
 		TransMessageCallback, TransMessageListener {
 
 	final public Logger log = Logger
 			.getLogger(ZeroAccessOverlayNode.class);
 
 	private TransLayer transLayer;
 
 	private NetLayer netLayer;
 
 	private int getL_round_robin_counter = 0;
 
 	boolean active = false;
 
 	long last_route_update_time = 0;
 
 	boolean reply = true;
 
 	long bot_software_version = 0;
 
 	public ZeroAccessOverlayNode(NetLayer netLayer, TransLayer transLayer,
 			ZeroAccessOverlayID peerId, short port, long downBandwidth,
 			long upBandwidth, String reply_param) {
 		super(peerId, port);
 
 		if (reply_param.equals("false"))
 		{
 			reply = false;
 		}
 
 		active = true;
 		this.transLayer = transLayer;
 		transLayer.addTransMsgListener(this, this.getPort());
 
 		this.netLayer = netLayer;
 		if (this.netLayer instanceof GnpNetLayer)
 		{
 			Bandwidth currentBandwidth = new Bandwidth(downBandwidth,
 					upBandwidth);
 			((GnpNetLayer) this.netLayer).setCurrentBandwidth(currentBandwidth);
 		}
 
 		this.routingTable = new ZeroAccessOverlayRoutingTable(peerId);
 		ZeroAccessBootstrapManager.getInstance().registerPeer(this);
 	}
 
 	@Override
 	public TransLayer getTransLayer() {
 		return transLayer;
 	}
 
 	@Override
 	public void connectivityChanged(ConnectivityEvent ce) {
 		if (ce.isOnline())
 		{
 			this.setPeerStatus(PeerStatus.PRESENT);
 		}
 		else if (ce.isOffline())
 		{
 			this.setPeerStatus(PeerStatus.ABSENT);
 		}
 	}
 
 	@Override
 	public void messageTimeoutOccured(int commId) {
 		//
 	}
 
 	@Override
 	public void receive(Message msg, TransInfo senderInfo, int commId) {
 		//
 	}
 
 	@Override
 	public void messageArrived(TransMsgEvent receivingEvent) {
 		Message message = receivingEvent.getPayload();
 
 		if (isActive()) {
 			// accept messages only if connection to peer exists
 			if (message instanceof BaseMessage) {
 				if (message instanceof GetLMessage) {
 					processGetL(receivingEvent);
 				} else if (message instanceof RetLMessage) {
 					processRetL(receivingEvent);
 				}
 			}
 		}
 	}
 
 	public long getBot_software_version() {
 		return bot_software_version;
 	}
 
 	private void processGetL(TransMsgEvent receivingEvent) {

 		GetLMessage getLMessage = (GetLMessage) receivingEvent
 				.getPayload();
 
 		ZeroAccessOverlayContact source_contact = (ZeroAccessOverlayContact) getLMessage
 				.getContact();
 		if (source_contact.getOverlayID() == this.getOverlayID()) {
 			return;
 		}
 
 		if (this.bot_software_version < getLMessage.getBot_software_version())
 		{
 			this.bot_software_version = getLMessage.getBot_software_version();
 		}
 
 		LinkedList<ZeroAccessOverlayContact> latestContacts = this
 				.getZeroAccessRoutingTable().getLatestContacts(16);
 
 		source_contact.refresh();
 
 		boolean sendGetLRequest = false;
 
 		if (this.getZeroAccessRoutingTable().getContact(
 				source_contact.getOverlayID()) == null) {
 			this.getZeroAccessRoutingTable().addContact(source_contact);
 			sendGetLRequest = true;
 		} else {
 			sendGetLRequest = false;
 		}
 
 		if (getLMessage.isRecheck())
 		{
 			sendGetLRequest = false;
 		}
 
 		RetLOperation retLOperation = new RetLOperation(this,
 				source_contact.getTransInfo(), latestContacts,
 				this.bot_software_version,
 				new OperationCallback<Object>() {
 					@Override
 					public void calledOperationFailed(
 							Operation<Object> op) {
 						//
 					}
 
 					@Override
 					public void calledOperationSucceeded(
 							Operation<Object> op) {
 						//
 					}
 				});
 		retLOperation.scheduleImmediately();
 
 		if (sendGetLRequest)
 		{
 			GetLOperation getLOperation = new GetLOperation(this,
 					source_contact.getTransInfo(),
 					new OperationCallback<Object>() {
 						@Override
 						public void calledOperationFailed(
 								Operation<Object> op) {
 							//
 						}
 
 						@Override
 						public void calledOperationSucceeded(
 								Operation<Object> op) {
 							//
 						}
 					}, true, this.bot_software_version);
 			getLOperation.scheduleWithDelay(2000);
 
 		}
 		if (this.getOverlayID().toString().equals("10"))
 		{
 			String current_time = Simulator.getSimulatedRealtime();
 			// log.warn("Current Time " + current_time);
 
 		}
 	}
 
 	public void setBot_software_version(long bot_software_version) {
 		this.bot_software_version = bot_software_version;
 	}
 
 	private void processRetL(TransMsgEvent receivingEvent) {
 
 		this.setPeerStatus(PeerStatus.PRESENT);
 
 		last_route_update_time = Simulator.getCurrentTime();
 
 		RetLMessage retLMessage = (RetLMessage) receivingEvent
 				.getPayload();
 
 		if (this.bot_software_version < retLMessage.getBot_software_version())
 		{
 			this.bot_software_version = retLMessage.getBot_software_version();
 		}
 
 		LinkedList<ZeroAccessOverlayContact> contact_list = retLMessage
 				.getContacts();
 
 		for (int i = 0; i < contact_list.size(); i++)
 		{
 			((ZeroAccessOverlayRoutingTable) this.routingTable)
 					.addContact(contact_list.get(i));
 		}
 	}
 
 	public void scheduleGetL(
 			ScheduleGetLOperation scheduleOverlayOperation) {
 		TransInfo bootstrapInfo = null;
 		List<TransInfo> bootstrapInfos = null;
 		int count = 1;
 		if (this.getOverlayID().toString().equals("651"))
 		{
 			count += 1;
 			String current_time = Simulator.getSimulatedRealtime();
 			// log.warn("Current Time " + current_time);
 		}
 		if (this.getZeroAccessRoutingTable()
 				.numberOfContacts() == 0) {
 			bootstrapInfos = ZeroAccessBootstrapManager
 					.getInstance().getBootstrapInfo();
 		}
 		else
 		{
 			List<ZeroAccessOverlayContact> contactList = (List<ZeroAccessOverlayContact>) this.routingTable
 					.allContacts();
 			bootstrapInfos = new LinkedList<TransInfo>();
 			for (int i = 0; i < contactList.size(); i++)
 			{
 				ZeroAccessOverlayContact znode = contactList.get(i);
 				bootstrapInfos.add(znode.getTransInfo());
 			}
 		}
 
 		if (getL_round_robin_counter >= bootstrapInfos.size()) {
 			getL_round_robin_counter = 0;
 		}
 
 		bootstrapInfo = bootstrapInfos
 				.get(getL_round_robin_counter);
 
 		if (bootstrapInfo.equals(this.getTransLayer()
 				.getLocalTransInfo(this.getPort()))) {
 			if (bootstrapInfos.size() == 1) {
 				return;
 			}
 			else
 			{
 				getL_round_robin_counter = (getL_round_robin_counter + 1)
 						% bootstrapInfos.size();
 				bootstrapInfo = bootstrapInfos
 						.get(getL_round_robin_counter);
 				log.debug("could not send request to itself, turn to next one in bootstrap list with size: "
 						+ bootstrapInfos.size() + " current index: "
 						+ getL_round_robin_counter);
 			}
 		} else {
 			getL_round_robin_counter = (getL_round_robin_counter + 1)
 					% bootstrapInfos.size();
 		}
 
 		GetLOperation getLOperation = new GetLOperation(this,
 				bootstrapInfo, this.bot_software_version,
 				new OperationCallback<Object>() {
 					@Override
 					public void calledOperationFailed(
 							Operation<Object> op) {
 						//
 					}
 
 					@Override
 					public void calledOperationSucceeded(
 							Operation<Object> op) {
 						//
 					}
 				});
 		getLOperation.scheduleImmediately();
 
 		if (this.getOverlayID().toString().equals("902"))
 		{
 			String current_time = Simulator.getSimulatedRealtime();
 			// log.warn("ScheduleGetL Current Time " + current_time);
 		}
 
 		updateConnectivityToOthers();
 	}
 
 	@Override
 	public String toString() {
 		return this.getOverlayID().toString();
 	}
 
 	public void fail() {
 		active = false;
 		routingTable.clearContacts();
 		ZeroAccessBootstrapManager.getInstance().unregisterNode(this);
 		ZeroAccessBootstrapManager.getInstance().unregisterPeer(this);
 	}
 
 	public void leave() {
 		active = false;
 		routingTable.clearContacts();
 		ZeroAccessBootstrapManager.getInstance().unregisterNode(this);
 		ZeroAccessBootstrapManager.getInstance().unregisterPeer(this);
 	}
 
 	public void updateConnectivityToOthers()
 	{
 		List<ZeroAccessOverlayContact> za_list = (List<ZeroAccessOverlayContact>) this.routingTable
 				.allContacts();
 		int fake_count = 0;
 		for (int i = 0; i < za_list.size(); i++)
 		{
 			ZeroAccessOverlayContact contact = za_list.get(i);
 			if (contact.getOverlayID().getUniqueValue().intValue() > ZeroAccessBootstrapManager
 					.getInstance().getSize())
 			{
 				fake_count += 1;
 			}
 		}
 		if (fake_count == 256)
 		{
 			if (this.getPeerStatus() == PeerStatus.PRESENT)
 			{
 				log.debug(Simulator.getSimulatedRealtime() + " node : "
 						+ this.toString()
 						+ " poisoned with fake route entries size "
 						+ fake_count);
 			}
 			this.setPeerStatus(PeerStatus.ABSENT);
 		}
 		else
 		{
 			if (this.getPeerStatus() == PeerStatus.ABSENT)
 			{
 				log.debug(Simulator.getSimulatedRealtime() + " node : "
 						+ this.toString()
 						+ " recovered with fake count: "
 						+ fake_count);
 			}
 			this.setPeerStatus(PeerStatus.PRESENT);
 		}
 
 		if (this.getPeerStatus() == PeerStatus.PRESENT)
 		{
 			long time_elapsed_since_list_update = Simulator.getCurrentTime()
 					- last_route_update_time;
 			long time_out = 1000 * 1000 * 1800;
 			if (time_elapsed_since_list_update > time_out)
 			{
 				log.debug(Simulator.getSimulatedRealtime() + " node : "
 						+ this.toString()
 						+ " update timeout, deemed absent with fake count --> "
 						+ fake_count);
 				this.setPeerStatus(PeerStatus.ABSENT);
 			}
 			else
 			{
 				this.setPeerStatus(PeerStatus.PRESENT);
 			}
 		}
 	}
 
 	public boolean isActive() {
 		return active;
 	}
 
 	public ZeroAccessOverlayRoutingTable getZeroAccessRoutingTable() {
 		return (ZeroAccessOverlayRoutingTable) routingTable;
 	}
 
 	@Override
 	public NeighborDeterminator<ZeroAccessOverlayContact> getNeighbors() {
 		return new NeighborDeterminator() {
 
 			@Override
 			public List<OverlayContact<ZeroAccessOverlayID>> getNeighbors() {
 				return Collections.unmodifiableList(routingTable.allContacts());
 			}
 		};
 	}
 
 	@Override
 	public int join(OperationCallback<Object> callback) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public int leave(OperationCallback<Object> callback) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 }

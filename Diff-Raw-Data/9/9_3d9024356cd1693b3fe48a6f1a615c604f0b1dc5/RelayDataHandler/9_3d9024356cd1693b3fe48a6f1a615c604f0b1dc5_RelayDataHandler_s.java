 package ch.compass.gonzoproxy.relay.io;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.concurrent.LinkedTransferQueue;
 import java.util.concurrent.TimeUnit;
 
 import ch.compass.gonzoproxy.model.ForwardingType;
 import ch.compass.gonzoproxy.model.Packet;
 import ch.compass.gonzoproxy.model.SessionModel;
 import ch.compass.gonzoproxy.relay.modifier.FieldRule;
 import ch.compass.gonzoproxy.relay.modifier.PacketModifier;
 import ch.compass.gonzoproxy.relay.modifier.PacketRegex;
 import ch.compass.gonzoproxy.relay.modifier.PacketRule;
 import ch.compass.gonzoproxy.relay.parser.ParsingHandler;
 import ch.compass.gonzoproxy.utils.PersistingUtils;
 
 public class RelayDataHandler implements Runnable {
 
 	private LinkedTransferQueue<Packet> receiverQueue = new LinkedTransferQueue<Packet>();
 
 	private LinkedTransferQueue<Packet> commandSenderQueue = new LinkedTransferQueue<Packet>();
 	private LinkedTransferQueue<Packet> responseSenderQueue = new LinkedTransferQueue<Packet>();
 
 	private ParsingHandler parsingHandler = new ParsingHandler();
 	private PacketModifier packetModifier = new PacketModifier();
 
 	private SessionModel sessionModel = new SessionModel();
 
 	@Override
 	public void run() {
 		handleRelayData();
 	}
 
 	public void setPacketModifier(PacketModifier packetModifier) {
 		this.packetModifier = packetModifier;
 	}
 
 	private void handleRelayData() {
 
 		while (!Thread.currentThread().isInterrupted()) {
 			try {
 				Packet receivedPacket = receiverQueue.take();
 
 				Packet sendingPacket = processPacket(receivedPacket);
 				addToSenderQueue(sendingPacket);
 
 			} catch (InterruptedException e) {
 				Thread.currentThread().interrupt();
 			}
 		}
 
 	}
 
 	private Packet processPacket(Packet packet) {
 		Packet sendingPacket = packet.clone();
 		parsingHandler.tryParse(packet);
 		sessionModel.addPacket(packet);
 		tryModifyPacket(sendingPacket);
 		
 		if (sendingPacket.isModified())
 			sessionModel.addPacket(sendingPacket);
 
 		return sendingPacket;
 	}
 
 	private void tryModifyPacket(Packet packetToModify) {
 		packetModifier.modifyByRegex(packetToModify);
 		parsingHandler.tryParse(packetToModify);
 		packetModifier.modifyByRule(packetToModify);
 	}
 
 	private void addToSenderQueue(Packet sendingPacket) {
 		switch (sendingPacket.getType()) {
 		case COMMAND:
 			commandSenderQueue.offer(sendingPacket);
 			break;
 
 		case RESPONSE:
 			responseSenderQueue.offer(sendingPacket);
 			break;
 		}
 
 	}
 
 	public void reparse() {

 		for (Packet packet : sessionModel.getPacketList()) {
 			packet.clearFields();
 			parsingHandler.tryParse(packet);
 		}
 	}
 
 	public void offer(Packet packet) {
 		receiverQueue.offer(packet);
 	}
 
 	public Packet poll(ForwardingType type) throws InterruptedException {
 		Packet sendingPacket = null;
 		switch (type) {
 		case COMMAND:
 			sendingPacket = commandSenderQueue.poll(200, TimeUnit.MILLISECONDS);
 			break;
 		case RESPONSE:
 			sendingPacket = responseSenderQueue
 					.poll(200, TimeUnit.MILLISECONDS);
 			break;
 		}
 		return sendingPacket;
 	}
 
 	public SessionModel getSessionModel() {
 		return sessionModel;
 	}
 
 	@SuppressWarnings("unchecked")
 	public void loadPacketsFromFile(File file) throws ClassNotFoundException, IOException {
 			sessionModel.addList((ArrayList<Packet>) PersistingUtils.loadFile(file));
 	}
 
 	public void persistSessionData(File file) throws IOException {
 			PersistingUtils.saveFile(file, sessionModel.getPacketList());
 	}
 
 	public ArrayList<PacketRule> getPacketRules() {
 		return packetModifier.getPacketRule();
 	}
 
 	public ArrayList<PacketRegex> getPacketRegex() {
 		return packetModifier.getPacketRegex();
 	}
 
 	public void addRule(String packetName, FieldRule fieldRule,
 			Boolean updateLength) {
 		packetModifier.addRule(packetName, fieldRule, updateLength);
 	}
 
 	public void addRegex(PacketRegex packetRegex, boolean isActive) {
 		packetModifier.addRegex(packetRegex, isActive);
 	}
 
 	public void persistRules() throws IOException {
 		packetModifier.persistRules();
 	}
 
 	public void persistRegex() throws IOException {
 		packetModifier.persistRegex();
 	}
 
 	public void clearSessionData() {
 		sessionModel.clearData();
 	}
 }

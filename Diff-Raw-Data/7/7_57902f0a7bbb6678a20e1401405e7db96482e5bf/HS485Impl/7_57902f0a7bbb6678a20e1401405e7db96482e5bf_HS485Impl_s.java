 package ch.eleveneye.hs485.protocol;
 
 import gnu.io.RXTXCommDriver;
 import gnu.io.RXTXPort;
 import gnu.io.SerialPort;
 import gnu.io.UnsupportedCommOperationException;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.eleveneye.hs485.api.HS485;
 import ch.eleveneye.hs485.api.MessageHandler;
 import ch.eleveneye.hs485.api.data.HwVer;
 import ch.eleveneye.hs485.api.data.SwVer;
 import ch.eleveneye.hs485.api.data.TFSValue;
 import ch.eleveneye.hs485.protocol.handler.RawDataHandler;
 
 public class HS485Impl implements HS485 {
 	private class AckRunnable implements Runnable {
 		IMessage	origMessage;
 
 		public AckRunnable(final IMessage origMessage) {
 			this.origMessage = origMessage;
 		}
 
 		@Override
 		public void run() {
 			try {
 				sendAck(origMessage);
 			} catch (final IOException e) {
 				log.warn("Konnte Asynchronen Ack nicht schicken", e);
 			}
 
 		}
 
 	}
 
 	private class DataHandler implements RawDataHandler {
 
 		@Override
 		public void handleClientList(final List<Integer> clients) {
 			synchronized (clientListMutex) {
 				clientList = clients;
 				lastClientListUpdate = System.currentTimeMillis();
 			}
 
 		}
 
 		@Override
 		public void handleLongPacket(final HS485Message packet) {
 			if (packet instanceof IMessage) {
 				final IMessage iMsg = (IMessage) packet;
 				if (iMsg.isSync()) {
 					final byte[] data = iMsg.getData();
 					if (data.length == 4 && data[0] == 'K')
 						executorService.execute(new Runnable() {
 							@Override
 							public void run() {
								final EventIndex handlerIndex = new EventIndex(packet.getTargetAddress(), data[2]);
 								final MessageHandler handler = keyEventHandlers.get(handlerIndex);
 								if (handler == null) {
 									if (packet.getTargetAddress() == ownAddress) {
 										doAck(iMsg);
 										try {
 											unRegisterEventAt(packet.getSourceAddress(), data[1], data[2]);
 										} catch (final IOException e) {
 											log.warn(
 													"Konnte Event nicht deregistrieren: " + Integer.toHexString(packet.getSourceAddress()) + "," + data[1] + "," + data[2], e);
 										}
 									} else if (packet.getTargetAddress() == BROADCAST_ADDRESS)
 										handleBroadcast(iMsg);
 
 								} else {
 									doAck(iMsg);
 									handler.handleMessage(iMsg);
 								}
 							}
 						});
 				} else if (packet.getTargetAddress() == ownAddress) {
 					final AckIndex key = new AckIndex(iMsg.getSourceAddress(), iMsg.getReceiveNumber());
 					final BlockingQueue<IMessage> newQueue = new LinkedBlockingQueue<IMessage>();
 					final BlockingQueue<IMessage> oldQueue = expectedReceiveQueue.putIfAbsent(key, newQueue);
 					if (oldQueue != null)
 						oldQueue.add(iMsg);
 					else
 						newQueue.add(iMsg);
 				} else if (packet.getTargetAddress() == BROADCAST_ADDRESS)
 					handleBroadcast(iMsg);
 
 			} else if (packet instanceof ACKMessage && packet.getTargetAddress() == ownAddress) {
 				final ACKMessage ackPacket = (ACKMessage) packet; // handle
 				// ack-Message
 				final AckIndex index = new AckIndex(ackPacket.getSourceAddress(), ackPacket.getReceiveNumber());
 				receivedAckMessages.putIfAbsent(index, new LinkedBlockingQueue<ACKMessage>());
 				final BlockingQueue<ACKMessage> queue = receivedAckMessages.get(index);
 				queue.add(ackPacket);
 			}
 		}
 
 		private void doAck(final IMessage iMsg) {
 			executorService.execute(new AckRunnable(iMsg));
 		}
 	}
 
 	private static final int																		BROADCAST_ADDRESS			= 0xffffffff;
 
 	private static final int																		INC_REPEAT_COUNT			= 8;
 
 	private static final Logger																	log										= LoggerFactory.getLogger(HS485Impl.class);
 
 	private static final int																		PACKET_REPEAT_COUNT		= 4;
 
 	private static final int																		PACKET_WAIT_TIME			= 200;
 
 	private final Collection<MessageHandler>										broadcastHandlers			= new ConcurrentLinkedQueue<MessageHandler>();
 
 	private List<Integer>																				clientList;
 
 	private final Object																				clientListMutex				= new Object();
 
 	private RXTXPort																						commPort;
 
 	private int																									currentSenderNumber;
 
 	private PacketDecoder																				decoder;
 
 	private PacketEncoder																				encoder;
 
 	private ExecutorService																			executorService				= Executors.newCachedThreadPool();
 
 	private ConcurrentMap<AckIndex, BlockingQueue<IMessage>>		expectedReceiveQueue;
 
 	private Map<EventIndex, MessageHandler>											keyEventHandlers;
 
 	private long																								lastClientListUpdate	= 0;
 
 	private int																									ownAddress;
 	private final Object																				readClientListMutex		= new Object();
 
 	private ConcurrentMap<AckIndex, BlockingQueue<ACKMessage>>	receivedAckMessages;
 
 	public HS485Impl(final String port, final int myAddress) throws UnsupportedCommOperationException, IOException {
 		init(port, myAddress);
 		currentSenderNumber = 0;
 	}
 
 	@Override
 	public void addBroadcastHandler(final MessageHandler handler) {
 		broadcastHandlers.add(handler);
 	}
 
 	@Override
	public void addKeyHandler(final int targetAddress, final byte actorNr, final MessageHandler handler) throws IOException {
		final EventIndex eventIndex = new EventIndex(targetAddress, actorNr);
 		keyEventHandlers.put(eventIndex, handler);
 	}
 
 	public void handleBroadcast(final IMessage iMsg) {
 		executorService.execute(new Runnable() {
 
 			@Override
 			public void run() {
 				for (final MessageHandler handler : broadcastHandlers)
 					handler.handleMessage(iMsg);
 			}
 		});
 	}
 
 	@Override
 	public List<Integer> listClients() throws IOException {
 		synchronized (readClientListMutex) {
 
 			synchronized (clientListMutex) {
 				if (lastClientListUpdate + 60 * 1000 > System.currentTimeMillis())
 					return clientList;
 				else
 					clientList = null;
 			}
 			encoder.initiateDiscovering();
 			while (true) {
 				synchronized (clientListMutex) {
 					if (clientList != null)
 						return clientList;
 				}
 				try {
 					Thread.sleep(100);
 				} catch (final InterruptedException e) {
 					synchronized (clientListMutex) {
 						return new ArrayList<Integer>(clientList);
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public int[] listOwnAddresse() {
 		return new int[] { ownAddress };
 	}
 
 	@Override
 	public byte readActor(final int moduleAddress, final byte actor) throws IOException {
 		final IMessage msg = prepareIMessage(moduleAddress);
 		final byte[] data = new byte[] { 'S', actor };
 		msg.setData(data);
 		final IMessage answer = sendAndWaitForAnswer(msg);
 
 		return answer.getData()[1];
 
 	}
 
 	@Override
 	public HwVer readHwVer(final int address) throws IOException {
 		final IMessage msg = prepareIMessage(address);
 		msg.setData(new byte[] { 'h' });
 		final IMessage answer = sendAndWaitForAnswer(msg);
 
 		final byte[] answerData = answer.getData();
 		return new HwVer(answerData[0], answerData[1]);
 	}
 
 	@Override
 	public int readLux(final int address) throws IOException {
 		final IMessage msg = prepareIMessage(address);
 		msg.setData(new byte[] { 'L' });
 		final IMessage answer = sendAndWaitForAnswer(msg);
 
 		final byte[] answerData = answer.getData();
 		return (answerData[0] & 0xff) << 24 | (answerData[1] & 0xff) << 16 | (answerData[2] & 0xff) << 8 | answerData[3] & 0xff;
 	}
 
 	@Override
 	public byte[] readModuleEEPROM(final int address, final int count) throws IOException {
 		final byte[] ret = new byte[count];
 		int partOffset = 0;
 		while (partOffset < count) {
 			int partSize = count - partOffset;
 			if (partSize > 32)
 				partSize = 32;
 			readEEPROMRaw(address, partOffset, ret, partOffset, partSize);
 			partOffset += partSize;
 		}
 		return ret;
 	}
 
 	@Override
 	public SwVer readSwVer(final int address) throws IOException {
 		final IMessage msg = prepareIMessage(address);
 		msg.setData(new byte[] { 'v' });
 		final IMessage answer = sendAndWaitForAnswer(msg);
 
 		final byte[] answerData = answer.getData();
 		if (answerData.length < 1)
 			return new SwVer((byte) -1, (byte) 0);
 		if (answerData.length < 2)
 			return new SwVer(answerData[0], (byte) 0);
 
 		return new SwVer(answerData[0], answerData[1]);
 	}
 
 	@Override
 	public TFSValue readTemp(final int address) throws IOException {
 		final IMessage msg = prepareIMessage(address);
 		msg.setData(new byte[] { 'F' });
 		final IMessage answer = sendAndWaitForAnswer(msg);
 
 		final byte[] answerData = answer.getData();
 		final TFSValue value = new TFSValue(answerData);
 		if (value.getTemperatur() == 0x8002)
 			throw new HardwareError("Sensor " + Integer.toHexString(address) + " defekt");
 		return value;
 	}
 
 	public void registerEventAt(final int moduleAddress, final byte sensor, final byte actor) throws IOException {
 		final IMessage msg = prepareIMessage(moduleAddress);
 		final byte[] data = new byte[] { 'q', sensor, actor };
 		msg.setData(data);
 		sendAndWaitForAck(msg);
 	}
 
 	@Override
 	public void reloadModule(final int address) throws IOException {
 		final IMessage msg = prepareIMessage(address);
 		msg.setData(new byte[] { 'C' });
 		sendAndWaitForAck(msg);
 	}
 
 	@Override
 	public void resetModule(final int address) throws IOException {
 		final IMessage msg = prepareIMessage(address);
 		msg.setData(new byte[] { '!', '!' });
 		sendAndWaitForAck(msg);
 	}
 
 	public void setExecutorService(final ExecutorService executorService) {
 		this.executorService = executorService;
 	}
 
 	public void unRegisterEventAt(final int moduleAddress, final byte sensor, final byte actor) throws IOException {
 		final IMessage msg = prepareIMessage(moduleAddress);
 		final byte[] data = new byte[] { 'c', sensor, actor };
 		msg.setData(data);
 		sendAndWaitForAck(msg);
 	}
 
 	@Override
 	public void writeActor(final int moduleAddress, final byte actor, final byte action) throws IOException {
 		final IMessage msg = prepareIMessage(moduleAddress);
 		final byte[] data = new byte[] { 's', 0, actor, action };
 		msg.setData(data);
 		sendAndWaitForAck(msg);
 	}
 
 	@Override
 	public void writeModuleEEPROM(final int deviceAddress, final int memOffset, final byte[] data, final int dataOffset, final int length)
 			throws IOException {
 		int partOffset = 0;
 		while (partOffset < length) {
 			int partSize = length - partOffset;
 			if (partSize > 32)
 				partSize = 32;
 			writeModuleEEPROMRaw(deviceAddress, memOffset + partOffset, data, dataOffset + partOffset, partSize);
 			partOffset += partSize;
 		}
 	}
 
 	@Override
 	protected void finalize() throws Throwable {
 		super.finalize();
 		commPort.close();
 	}
 
 	private void init(final String port, final int myAddress) throws UnsupportedCommOperationException, IOException {
 		ownAddress = myAddress;
 		receivedAckMessages = new ConcurrentHashMap<AckIndex, BlockingQueue<ACKMessage>>();
 		expectedReceiveQueue = new ConcurrentHashMap<AckIndex, BlockingQueue<IMessage>>();
 		keyEventHandlers = new ConcurrentHashMap<EventIndex, MessageHandler>();
 
 		final RXTXCommDriver commDriver = new RXTXCommDriver();
 		commDriver.initialize();
 		try {
 			Thread.sleep(100);
 		} catch (final InterruptedException e) {
 			log.info("Interrupted Exception aufgetreten", e);
 		}
 		commPort = (RXTXPort) commDriver.getCommPort(port, 1);
 		if (commPort == null)
 			throw new IOException("Auf Gerät " + port + " kann nicht zugegriffen werden");
 		commPort.setSerialPortParams(19200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_EVEN);
 		commPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
 
 		decoder = new PacketDecoder(commPort.getInputStream());
 		encoder = new PacketEncoder(commPort.getOutputStream(), decoder);
 		decoder.addDataHandler(new DataHandler());
 	}
 
 	private IMessage prepareIMessage(final int moduleAddress) {
 		final IMessage msg = new IMessage();
 		msg.setSourceAddress(ownAddress);
 		msg.setTargetAddress(moduleAddress);
 		msg.setSync(true);
 		msg.setSenderNumber((byte) (currentSenderNumber-- & 0x03));
 		msg.setReceiveNumber((byte) 0);
 		msg.setHasSourceAddr(true);
 		return msg;
 	}
 
 	private void readEEPROMRaw(final int address, final int offset, final byte[] data, final int dataOffset, final int dataCount) throws IOException {
 		final IMessage msg = prepareIMessage(address);
 		msg.setData(new byte[] { 'R', (byte) (offset >> 8 & 0xff), (byte) (offset & 0xff), (byte) (dataCount & 0xff) });
 		final IMessage answer = sendAndWaitForAnswer(msg);
 		assert answer.getData().length == dataCount;
 		System.arraycopy(answer.getData(), 0, data, dataOffset, dataCount);
 	}
 
 	private void sendAck(final IMessage answer) throws IOException {
 		final ACKMessage ack = new ACKMessage();
 		ack.setSourceAddress(ownAddress);
 		ack.setTargetAddress(answer.getSourceAddress());
 		ack.setReceiveNumber(answer.getSenderNumber());
 		ack.setHasSourceAddr(true);
 		encoder.sendPacket(ack);
 	}
 
 	private ACKMessage sendAndWaitForAck(final IMessage msg) throws IOException {
 		final byte origSenderNumber = msg.getSenderNumber();
 		for (int k = 0; k < INC_REPEAT_COUNT; k++) {
 			final byte tempSenderNummer = (byte) (origSenderNumber + k & 0x3);
 			msg.setSenderNumber(tempSenderNummer);
 			final AckIndex ackIndex = new AckIndex(msg.getTargetAddress(), msg.getSenderNumber());
 			receivedAckMessages.putIfAbsent(ackIndex, new LinkedBlockingQueue<ACKMessage>());
 			final BlockingQueue<ACKMessage> queue = receivedAckMessages.get(ackIndex);
 			synchronized (queue) {
 				queue.clear();
 				// wait always once per target and ack-number
 				for (int i = 0; i < PACKET_REPEAT_COUNT; i++)
 					try {
 						encoder.sendPacket(msg);
 						ACKMessage ackMessage;
 						ackMessage = queue.poll(PACKET_WAIT_TIME, TimeUnit.MILLISECONDS);
 						if (ackMessage != null)
 							return ackMessage;
 					} catch (final InterruptedException e) {
 						log.warn("Exception beim Warten auf ACK", e);
 					}
 			}
 		}
 		throw new TimeoutException("Expected Ack not requested");
 	}
 
 	private IMessage sendAndWaitForAnswer(final IMessage msg) throws IOException {
 
 		final byte origSenderNumber = msg.getSenderNumber();
 		for (int k = 0; k < INC_REPEAT_COUNT; k++) {
 			final byte tempSenderNummer = (byte) (origSenderNumber + k & 0x3);
 			msg.setSenderNumber(tempSenderNummer);
 
 			final BlockingQueue<IMessage> newQueue = new LinkedBlockingQueue<IMessage>();
 			final BlockingQueue<IMessage> existingQueue = expectedReceiveQueue.putIfAbsent(new AckIndex(msg.getTargetAddress(), msg.getSenderNumber()),
 					newQueue);
 			final BlockingQueue<IMessage> receiveQueue;
 			if (existingQueue != null)
 				receiveQueue = existingQueue;
 			else
 				receiveQueue = newQueue;
 			synchronized (receiveQueue) {
 				receiveQueue.clear();
 				// do not send more than one request concurrent to one target
 				for (int i = 0; i < PACKET_REPEAT_COUNT; i++) {
 					encoder.sendPacket(msg);
 
 					try {
 						final IMessage answer = receiveQueue.poll(PACKET_WAIT_TIME, TimeUnit.MILLISECONDS);
 						if (answer != null) {
 							sendAck(answer);
 							return answer;
 						}
 					} catch (final InterruptedException e) {
 						log.warn("Exception beim Warten auf Antwort", e);
 					}
 
 				}
 			}
 		}
 		throw new TimeoutException("Expected Answer not requested");
 	}
 
 	private void writeModuleEEPROMRaw(final int deviceAddress, final int offset, final byte[] data, final int dataOffset, final int dataCount)
 			throws IOException {
 		final IMessage msg = prepareIMessage(deviceAddress);
 		final byte[] pckData = new byte[4 + dataCount];
 		pckData[0] = 'W';
 		pckData[1] = (byte) (offset >> 8 & 0xff);
 		pckData[2] = (byte) (offset & 0xff);
 		pckData[3] = (byte) dataCount;
 		System.arraycopy(data, dataOffset, pckData, 4, dataCount);
 		msg.setData(pckData);
 		sendAndWaitForAck(msg);
 	}
 }

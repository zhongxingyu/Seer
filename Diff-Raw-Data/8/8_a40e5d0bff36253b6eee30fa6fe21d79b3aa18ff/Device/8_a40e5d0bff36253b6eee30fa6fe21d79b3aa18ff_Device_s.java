 package ch.eleveneye.hs485.dummy.device;
 
 import java.util.Arrays;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import ch.eleveneye.hs485.api.MessageHandler;
 import ch.eleveneye.hs485.api.data.HwVer;
 import ch.eleveneye.hs485.api.data.KeyMessage;
 import ch.eleveneye.hs485.api.data.SwVer;
 import ch.eleveneye.hs485.api.data.TFSValue;
 
 /**
  * Abstract Dummy-Device for Test
  * 
  */
 public abstract class Device {
 	protected byte[]									actor;
 	protected byte[]									data				= new byte[512];
 	private final int									address;
 	private ScheduledExecutorService	executorService;
 	private final MessageHandler			handler;
 	private final boolean[]						keyPressed	= new boolean[] { false, false };
 
 	public Device(final int actorCount, final int address, final MessageHandler handler) {
 		this.address = address;
 		this.handler = handler;
 		actor = new byte[actorCount];
 		Arrays.fill(data, (byte) 0xff);
 		Arrays.fill(actor, (byte) 0xff);
 	}
 
 	/**
 	 * read current Value of a Actor
 	 * 
 	 * @param actorNr
 	 *          Index of Actor
 	 * @return currentValue of Actor
 	 */
 	public synchronized byte readActor(final byte actorNr) {
 		return actor[actorNr];
 	}
 
 	/**
 	 * Reads HW-Version of current device
 	 * 
 	 * @return
 	 */
 	public abstract HwVer readHwVer();
 
 	/**
 	 * Reads the Brightness from Lux-Sensor
 	 * 
 	 * @return Brightness
 	 */
 	public int readLux() {
 		return 0;
 	}
 
 	/**
 	 * Reads config-Data from Module
 	 * 
 	 * @param count
 	 *          count of Bytes
 	 * @return Data
 	 */
 	public synchronized byte[] readModuleEEPROM(final int count) {
 		return Arrays.copyOf(data, count);
 	}
 
 	/**
 	 * Reads Firmware-Version from dummy-Device
 	 * 
 	 * @return Firmware-Version
 	 */
 
 	public abstract SwVer readSwVer();
 
 	/**
 	 * Reads temperature and humidity from TFS-Sensor
 	 * 
 	 * @return Values
 	 */
 	public TFSValue readTemp() {
 		return null;
 	}
 
 	public void setExecutorService(final ScheduledExecutorService executorService) {
 		this.executorService = executorService;
 		for (int i = 0; i < 2; i++)
 			scheduleNextBroadcast(i);
 	}
 
 	public void setInputJoint(final boolean join) {
 		data[0] = join ? (byte) 1 : (byte) 0;
 	}
 
 	public void setSensorTarget(final int place, final int sensor, final int module, final int actor) {
 		final int startAddress = 0x80 + place * 6;
 		data[startAddress] = (byte) (sensor & 0xff);
 		data[startAddress + 1] = (byte) (module >> 24 & 0xff);
 		data[startAddress + 2] = (byte) (module >> 16 & 0xff);
 		data[startAddress + 3] = (byte) (module >> 8 & 0xff);
 		data[startAddress + 4] = (byte) (module & 0xff);
 		data[startAddress + 5] = (byte) (actor & 0xff);
 	}
 
 	public synchronized void writeActor(final byte actorNr, final byte value) {
 		actor[actorNr] = value;
 	}
 
 	public synchronized void writeEEPROM(final int memOffset, final byte[] data2, final int dataOffset, final int length) {
 		System.arraycopy(data2, dataOffset, data, memOffset, length);
 	}
 
 	private void scheduleNextBroadcast(final int keyNr) {
 		final int time;
 		final TimeUnit waitTimeUnit;
 		if (keyPressed[keyNr]) {
 			time = (int) (Math.random() * 60 + 10);
 			waitTimeUnit = TimeUnit.SECONDS;
 		} else {
 			time = (int) (Math.random() * 100 + 5);
 			waitTimeUnit = TimeUnit.MILLISECONDS;
 		}
 
 		executorService.schedule(new Runnable() {
 
 			@Override
 			public void run() {
 				final KeyMessage keyMessage = new KeyMessage();
 				keyMessage.setSourceAddress(address);
 				keyMessage.setTargetAddress(-1);
 				keyMessage.setSourceSensor(0);
				keyMessage.setTargetActor(keyPressed[keyNr] ? 2 : 0);
 				scheduleNextBroadcast(keyNr);
 				keyPressed[keyNr] = !keyPressed[keyNr];
 				handler.handleMessage(keyMessage);
 			}
 		}, time, waitTimeUnit);
 	}
 }

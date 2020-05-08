 package com.traviswyatt.ioio.max31855;
 
 import ioio.lib.api.DigitalInput;
 import ioio.lib.api.DigitalOutput;
 import ioio.lib.api.IOIO;
 import ioio.lib.api.SpiMaster;
 import ioio.lib.api.SpiMaster.Rate;
 import ioio.lib.api.exception.ConnectionLostException;
 import ioio.lib.util.IOIOLooper;
 
 public class MAX31855 implements IOIOLooper {
 	
 	private static final int READ_BUFFER_SIZE = 4; // bytes
 	
 	public static final int THERMOCOUPLE_SIGN_BIT = 0x80000000; // D31
 	public static final int INTERNAL_SIGN_BIT     = 0x8000;     // D15
 	
 	public static final int FAULT_BIT = 0x10000; // D16
 	
 	public static final byte FAULT_OPEN_CIRCUIT_BIT = (byte) 0x01;
 	public static final byte FAULT_SHORT_TO_GND_BIT = (byte) 0x02;
 	public static final byte FAULT_SHORT_TO_VCC_BIT = (byte) 0x04;
 
 	/**
 	 * 11 of the least most significant bits (big endian) set to 1.
 	 */
 	public static final int LSB_11 = 0x07FF;
 	
 	/**
 	 * 13 of the least most significant bits (big endian) set to 1.
 	 */
 	public static final int LSB_13 = 0x1FFF;
 	
 	public interface MAX31855Listener {
 		public void onData(float internal /* C */, float thermocouple /* C */);
 		public void onFault(byte fault);
 	}
 	
 	private MAX31855Listener listener;
 	
 	private volatile float internal;
 	private volatile float thermocouple;
 	
 	private final DigitalInput.Spec  miso;
 	private final DigitalOutput.Spec mosi;
 	private final DigitalOutput.Spec clk;
 	private final DigitalOutput.Spec[] slaveSelect;
 	private final Rate rate;
 	private SpiMaster spi;
 	
 	private final byte[] EMPTY_BYTE_ARRAY = new byte[0];
 	private byte[] readBuffer  = new byte[READ_BUFFER_SIZE];
 	
 	public MAX31855(int sdoPin, int sdaPin, int sclPin, int csPin, Rate rate) {
 		miso = new DigitalInput.Spec(sdoPin);
 		mosi = new DigitalOutput.Spec(sdaPin);
 		clk  = new DigitalOutput.Spec(sclPin);
 		slaveSelect = new DigitalOutput.Spec[] { new DigitalOutput.Spec(csPin) };
 		this.rate = rate;
 	}
 	
 	public MAX31855 setListener(MAX31855Listener listener) {
 		this.listener = listener;
 		return this;
 	}
 	
 	protected void read(int length, byte[] values) throws ConnectionLostException, InterruptedException {
 		int writeSize = EMPTY_BYTE_ARRAY.length;
 		int readSize = length;
 		int totalSize = writeSize + readSize;
 		spi.writeRead(EMPTY_BYTE_ARRAY, writeSize, totalSize, values, readSize);
 	}
 	
 	/*
 	 * IOIOLooper interface methods.
 	 */
 
 	@Override
 	public void setup(IOIO ioio) throws ConnectionLostException, InterruptedException {
 		boolean invertClk = false;
 		boolean sampleOnTrailing = false;
 		SpiMaster.Config config = new SpiMaster.Config(rate, invertClk, sampleOnTrailing);
 		spi = ioio.openSpiMaster(miso, mosi, clk, slaveSelect, config);
 	}
 
 	@Override
 	public void loop() throws ConnectionLostException, InterruptedException {
 		read(4, readBuffer);
 		int data = ((readBuffer[0] & 0xFF) << 24) |
 				   ((readBuffer[1] & 0xFF) << 16) |
 				   ((readBuffer[2] & 0xFF) <<  8) |
 				   ((readBuffer[3] & 0xFF) <<  0);
 		
 		if ((data & FAULT_BIT) == FAULT_BIT) {
 			if (listener != null) {
 				listener.onFault((byte) (data & 0x07));
 			}
 		}
 		
 		int internal = (int) ((data >> 4) & LSB_11);
 		if ((data & INTERNAL_SIGN_BIT) == INTERNAL_SIGN_BIT) {
			internal = -(~internal & LSB_11);
 		}
 		
 		int thermocouple = (int) ((data >> 18) & LSB_13);
 		if ((data & THERMOCOUPLE_SIGN_BIT) == THERMOCOUPLE_SIGN_BIT) {
			thermocouple = -(~thermocouple & LSB_13);
 		}
 		
 		this.internal = internal * 0.0625f;
 		this.thermocouple = thermocouple * 0.25f;
 		
 		if (listener != null) {
 			listener.onData(this.internal, this.thermocouple);
 		}
 	}
 
 	@Override
 	public void disconnected() {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void incompatible() {
 		// TODO Auto-generated method stub
 	}
 	
 }

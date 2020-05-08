 package org.reprap.devices;
 
 import java.io.IOException;
 
 import org.reprap.Device;
 import org.reprap.Preferences;
 import org.reprap.ReprapException;
 import org.reprap.comms.Address;
 import org.reprap.comms.Communicator;
 import org.reprap.comms.IncomingContext;
 import org.reprap.comms.OutgoingMessage;
 import org.reprap.comms.IncomingMessage;
 import org.reprap.comms.IncomingMessage.InvalidPayloadException;
 import org.reprap.comms.messages.IncomingIntMessage;
 import org.reprap.comms.messages.OutgoingAddressMessage;
 import org.reprap.comms.messages.OutgoingBlankMessage;
 import org.reprap.comms.messages.OutgoingByteMessage;
 import org.reprap.comms.messages.OutgoingIntMessage;
 
 public class GenericStepperMotor extends Device {
 
 	public static final byte MSG_SetForward = 1;		
 	public static final byte MSG_SetReverse = 2;
 	public static final byte MSG_SetPosition = 3;
 	public static final byte MSG_GetPosition = 4;
 	public static final byte MSG_Seek = 5;	
 	public static final byte MSG_SetIdle = 6;		
 	public static final byte MSG_SetNotification = 7;		
 	public static final byte MSG_SetSyncMode = 8;		
 	public static final byte MSG_Calibrate = 9;		
 	public static final byte MSG_GetRange = 10;
 	public static final byte MSG_DDAMaster = 11;
 	public static final byte MSG_SetPower = 14;
 	public static final byte MSG_HomeReset = 16;
 
 	public static final byte SYNC_NONE = 0;
 	public static final byte SYNC_SEEK = 1;
 	public static final byte SYNC_INC = 2;
 	public static final byte SYNC_DEC = 3;
 	
 	private boolean haveInitialised = false;
 	private boolean haveSetNotification = false;
 	private boolean haveCalibrated = false;
 	
 	private int maxTorque; ///< Power output limiting (0-100 percent)
 	
 	public GenericStepperMotor(Communicator communicator, Address address, Preferences prefs, int motorId) {
 		
 		//Address address, int maxTorque
 		super(communicator, address);
 		this.maxTorque = prefs.loadInt("Axis" + motorId + "Torque");
 	}
 
 	private void initialiseIfNeeded() throws IOException {
 		if (!haveInitialised) {
 			haveInitialised = true;
 			setMaxTorque(maxTorque);
 		}
 	}
 	
 	public void dispose() {
 	}
 	
 	/**
 	 * Set the motor speed (or turn it off) 
 	 * @param speed A value between -255 and 255.
 	 * @throws ReprapException
 	 * @throws IOException
 	 */
 	public void setSpeed(int speed) throws IOException {
 		initialiseIfNeeded();
 		lock();
 		try {
 			OutgoingMessage request = new RequestSetSpeed(speed);
 			sendMessage(request);
 		}
 		finally {
 			unlock();
 		}
 	}
 
 	public void setIdle() throws IOException {
 		initialiseIfNeeded();
 		lock();
 		try {
 			OutgoingMessage request = new RequestSetSpeed();
 			sendMessage(request);
 		}
 		finally {
 			unlock();
 		}
 	}
 	
 	public void resetPosition() throws IOException {
 		setPosition(0);
 	}
 	
 	public void setPosition(int position) throws IOException {
 		initialiseIfNeeded();
 		lock();
 		try {
 			sendMessage(new RequestSetPosition(position));
 		}
 		finally {
 			unlock();
 		}
 	}
 	
 	public int getPosition() throws IOException {
 		//System.out.println("get enter");
 		int value;
 		initialiseIfNeeded();
 		lock();
 		try {
 			IncomingContext replyContext = sendMessage(
 					new OutgoingBlankMessage(MSG_GetPosition));
 			
 			IncomingIntMessage reply = new RequestPositionResponse(replyContext);
 			try {
 				value = reply.getValue();
 			}
 			catch (IncomingMessage.InvalidPayloadException ex) {
 				throw new IOException(ex.getMessage());
 			}
 		}
 		finally {
 			unlock();
 		}
 		//System.out.println("get leave");
 		return value;
 	}
 	
 	public void seek(int speed, int position) throws IOException {
 		//System.out.println("seek enter");
 		initialiseIfNeeded()	;
 		lock();
 		try {
 			sendMessage(new RequestSeekPosition(speed, position));
 		}
 		finally {
 			unlock();
 			//System.out.println("seek leave");
 		}
 	}
 
 	public void seekBlocking(int speed, int position) throws IOException {
 		initialiseIfNeeded();
 		lock();
 		try {
 			setNotification();
 			IncomingContext replyContext = sendMessage(new RequestSeekPosition(speed, position));
 			new RequestSeekResponse(replyContext);
 			setNotificationOff();
		}
		finally {
 			unlock();
 		}
 	}
 
 	public Range getRange(int speed) throws IOException, InvalidPayloadException {
 		initialiseIfNeeded()	;
 		lock();
 		try {
 			if (haveCalibrated) {
 				IncomingContext replyContext = sendMessage(
 						new OutgoingBlankMessage(MSG_GetRange));
 				RequestRangeResponse response = new RequestRangeResponse(replyContext);
 				return response.getRange();
 			} else {
 				setNotification();
 				IncomingContext replyContext = sendMessage(
 						new OutgoingByteMessage(MSG_Calibrate, (byte)speed));
 				RequestRangeResponse response = new RequestRangeResponse(replyContext);
 				setNotificationOff();
 				return response.getRange();
 			}
 		}
 		finally {
 			unlock();
 		}
 	}
 	
 	public void homeReset(int speed) throws IOException, InvalidPayloadException {
 		initialiseIfNeeded()	;
 		lock();
 		try {
 			setNotification();
 			RequestHomeResetResponse response = new RequestHomeResetResponse(this, new OutgoingByteMessage(MSG_HomeReset, (byte)speed), 60000);
 			setNotificationOff();
 		} finally {
 			unlock();
 		}
 	}
 	
 	public void setSync(byte syncType) throws IOException {
 		initialiseIfNeeded()	;
 		lock();
 		try {
 			sendMessage(
 					new OutgoingByteMessage(MSG_SetSyncMode, syncType));
 		}
 		finally {
 			unlock();
 		}
 		
 	}
 	
 	public void dda(int speed, int x1, int deltaY) throws IOException {
 		initialiseIfNeeded()	;
 		lock();
 		try {
 			setNotification();
 			
 			new RequestDDAMasterResponse(this, new RequestDDAMaster(speed, x1, deltaY), 60000);
 			
 			setNotificationOff();
 		}
 		finally {
 			unlock();
 		}
 	}
 	
 	private void setNotification() throws IOException {
 		initialiseIfNeeded()	;
 		if (!haveSetNotification) {
 			sendMessage(new OutgoingAddressMessage(MSG_SetNotification,
 					getCommunicator().getAddress()));
 			haveSetNotification = true;
 		}
 	}
 
 	private void setNotificationOff() throws IOException {
 		initialiseIfNeeded()	;
 		if (haveSetNotification) {
 			sendMessage(new OutgoingAddressMessage(MSG_SetNotification, getAddress().getNullAddress()));
 			haveSetNotification = false;
 		}
 	}
 
 	/**
 	 * 
 	 * @param maxTorque An integer value 0 to 100 representing the maximum torque percentage
 	 * @throws IOException
 	 */
 	public void setMaxTorque(int maxTorque) throws IOException {
 		initialiseIfNeeded()	;
 		if (maxTorque > 100) maxTorque = 100;
 		double power = maxTorque * 68.0 / 100.0;
 		byte scaledPower = (byte)power;
 		lock();
 		try {
 			sendMessage(
 					new OutgoingByteMessage(MSG_SetPower, scaledPower));
 		}
 		finally {
 			unlock();
 		}
 		
 	}
 	
 	
 	protected class RequestPositionResponse extends IncomingIntMessage {
 		public RequestPositionResponse(IncomingContext incomingContext) throws IOException {
 			super(incomingContext);
 		}
 		
 		protected boolean isExpectedPacketType(byte packetType) {
 			return packetType == MSG_GetPosition; 
 		}
 	}
 
 	protected class RequestSeekResponse extends IncomingIntMessage {
 		public RequestSeekResponse(IncomingContext incomingContext) throws IOException {
 			super(incomingContext);
 		}
 		
 		protected boolean isExpectedPacketType(byte packetType) {
 			return packetType == MSG_Seek; 
 		}
 	}
 	
 	protected class RequestSetPosition extends OutgoingIntMessage {
 		public RequestSetPosition(int position) {
 			super(MSG_SetPosition, position);
 		}
 	}
 
 	protected class RequestSetSpeed extends OutgoingMessage {
 
 		byte [] message;
 		
 		/**
 		 * The empty constructor will create a message to idle the motor
 		 */
 		RequestSetSpeed() {
 			message = new byte [] { MSG_SetIdle }; 
 		}
 		
 		/**
 		 * Create a message for setting the motor speed.
 		 * @param speed The speed to set the motor to.  Note that specifying
 		 * 0 will stop the motor and hold it at 0 and thus still draws
 		 * high current.  To idle the motor, use the message created by the
 		 * empty constructor.
 		 */
 		RequestSetSpeed(int speed) {
 			byte command;
 			if (speed >= 0) {
 				command = MSG_SetForward;
 			} else {
 				command = MSG_SetReverse;
 				speed = -speed;
 			}
 			if (speed > 255) speed = 255;
 			message = new byte[] { command, (byte)speed };
 				
 		}
 		
 		public byte[] getBinary() {
 			return message;
 		}
 		
 	}
 	
 	protected class RequestSeekPosition extends OutgoingMessage {
 		byte [] message;
 		RequestSeekPosition(int speed, int position) {
 			message = new byte[] { MSG_Seek,
 					(byte)speed,
 					(byte)(position & 0xff),
 					(byte)((position >> 8) & 0xff)};
 		}
 		
 		public byte[] getBinary() {
 			return message;
 		}
 		
 	}
 
 	protected class RequestDDAMaster extends OutgoingMessage {
 		byte [] message;
 		RequestDDAMaster(int speed, int x1, int deltaY) {
 			message = new byte[] { MSG_DDAMaster,
 					(byte)speed,
 					(byte)(x1 & 0xff),
 					(byte)((x1 >> 8) & 0xff),
 					(byte)(deltaY & 0xff),
 					(byte)((deltaY >> 8) & 0xff)
 				};
 		}
 		
 		public byte[] getBinary() {
 			return message;
 		}
 		
 	}
 
 	protected class RequestDDAMasterResponse extends IncomingIntMessage {
 		public RequestDDAMasterResponse(Device device, OutgoingMessage message, long timeout) throws IOException {
 			super(device, message, timeout);
 		}
 		
 		protected boolean isExpectedPacketType(byte packetType) {
 			return packetType == MSG_DDAMaster;
 		}
 	}
 	
 	protected class RequestRangeResponse extends IncomingIntMessage {
 		public RequestRangeResponse(IncomingContext incomingContext) throws IOException {
 			super(incomingContext);
 		}
 		protected boolean isExpectedPacketType(byte packetType) {
 			// We could get this either as an asynchronous notification
 			// from calibration or by explicit request
 			return packetType == MSG_GetRange || packetType == MSG_Calibrate; 
 		}
 		public Range getRange() throws InvalidPayloadException {
 		    byte [] reply = getPayload();
 		    if (reply == null || reply.length != 3)
 		    	throw new InvalidPayloadException("Unexpected payload getting range");
 		    Range r = new Range();
 		    r.minimum = 0;
 		    r.maximum = IncomingIntMessage.ConvertBytesToInt(reply[1], reply[2]);
 		    return r;
 		}
 
 	}
 	
 	protected class RequestHomeResetResponse extends IncomingMessage {
 		public RequestHomeResetResponse(Device device, OutgoingMessage message, long timeout) throws IOException {
 			super(device, message, timeout);
 		}
 		protected boolean isExpectedPacketType(byte packetType) {
 			return packetType == MSG_HomeReset; 
 		}
 	}
 	
 	public class Range {
 		public int minimum;
 		public int maximum;
 	}
 
 }

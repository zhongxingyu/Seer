 /*
 Part of the NETLab Hub, which is part of the NETLab Toolkit project - http://netlabtoolkit.org
 
 Copyright (c) 2006-2013 Ewan Branda
 
 NETLab Hub is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 NETLab Hub is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with NETLab Hub.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package netlab.hub.plugins.xbee;
 
 import java.util.HashMap;
 import java.util.Iterator;
 
 import netlab.hub.core.ServiceException;
 import netlab.hub.serial.SerialPort;
 import netlab.hub.util.Logger;
 import netlab.hub.util.ThreadUtil;
 
 import com.rapplogic.xbee.api.ApiId;
 import com.rapplogic.xbee.api.PacketListener;
 import com.rapplogic.xbee.api.RemoteAtRequest;
 import com.rapplogic.xbee.api.XBee;
 import com.rapplogic.xbee.api.XBeeAddress16;
 import com.rapplogic.xbee.api.XBeeAddress64;
 import com.rapplogic.xbee.api.XBeeException;
 import com.rapplogic.xbee.api.XBeeResponse;
 import com.rapplogic.xbee.api.wpan.IoSample;
 import com.rapplogic.xbee.api.wpan.RxBaseResponse;
 import com.rapplogic.xbee.api.wpan.RxResponseIoSample;
 import com.rapplogic.xbee.api.zigbee.ZNetRxIoSampleResponse;
 import com.rapplogic.xbee.util.DoubleByte;
 
 
 public class XBeeNetwork implements PacketListener {
 	
 	/**
 	 * The default baud rate for the serial port connection.
 	 */
 	public static final int DEFAULT_BAUD_RATE = 9600;
 	
 	protected HashMap<String, RemoteXBee> xbees = new HashMap<String, RemoteXBee>();
 	protected String portName;
 	protected XBee baseStation;
 	protected int series = 2;
 	
 	/**
 	 * 
 	 */
 	public XBeeNetwork() {
 		super();
 	}
 	
 	/**
 	 * @param series
 	 */
 	public XBeeNetwork(int series) {
 		super();
 		this.series = series;
 	}
 	
 	/**
 	 * @param port
 	 * @param baudRate
 	 * @throws ServiceException
 	 */
 	public void connect(String portNamePattern, int baudRate) throws ServiceException {
 		portName = null;
 		try {
 			String[] portNames = SerialPort.list(portNamePattern);
 			if (portNames.length == 0) {
 				throw new ServiceException("Could not find available serial port matching ["+portNamePattern+"]");
 			}
 			portName = portNames[0]; // In case of multiple matching ports, take the first one in the list
 			this.baseStation = new XBee();
 			Logger.info("Opening serial port connection to XBee base station "+portName+" (rate="+baudRate+")...");
 			try {		
 				baseStation.open(portName, baudRate);
 				Logger.info("Serial port connection to XBee base station at port "+portName+" established.");
 			} catch (XBeeException e) {
 				throw new ServiceException("Error connecting to XBee", e);
 			}
 			ThreadUtil.pause(2000);
 			baseStation.addPacketListener(this);
 		} catch (Exception e) {
 			Logger.debug("Error connecting to XBee base station through serial port", e);
 		}
 	}
 	
 	/**
 	 * @return
 	 */
 	public boolean isConnected() {
 		return baseStation != null && baseStation.isConnected();
 	}
 	
 	/**
 	 * @return
 	 */
 	public String getPortName() {
 		return this.portName;
 	}
 	
 	/* A packet has been received from the network so update the sample buffer
 	 * if the packet contains samples. The method of extracting samples from
 	 * the packet differs based on the XBee series number.
 	 * See http://code.google.com/p/xbee-api/wiki/DevelopersGuide
 	 * @see com.rapplogic.xbee.api.PacketListener#processResponse(com.rapplogic.xbee.api.XBeeResponse)
 	 */
 	public synchronized void processResponse(XBeeResponse response) {
 		switch (series) {
 		case 1:
 			if (response.getApiId() == ApiId.RX_16_IO_RESPONSE) {
 				if (response instanceof RxResponseIoSample) {
 					RxResponseIoSample ioSample = (RxResponseIoSample)response;
 					int[] addr = ((RxBaseResponse)response).getSourceAddress().getAddress();
 					int remoteId = new DoubleByte(addr[0], addr[1]).get16BitValue();
 					RemoteXBee xbee = xbees.get(remoteId);
 					if (xbee == null) {
 						xbee = new RemoteXBee(Integer.toString(remoteId, 16));
 						xbees.put(xbee.getId(), xbee);
 					}
 					for (IoSample sample: ioSample.getSamples()) {
 						if (ioSample.containsAnalog()) {
 							for (int pin=0; pin<xbee.getAnalogPinCount(); pin++) {
 								xbee.setAnalog(pin, sample.getAnalog(pin));
 							}
 						}
 						if (ioSample.containsDigital()) {
 							for (int pin=0; pin<xbee.getDigitalPinCount(); pin++) {
 								xbee.setDigital(pin, sample.isDigitalOn(pin) ? 1 : 0);
 							}
 						}
 					}
 				}
 			}
 			break;
 		case 2:
 		default:
 			if (response.getApiId() == ApiId.ZNET_IO_SAMPLE_RESPONSE) {
 				ZNetRxIoSampleResponse sample = (ZNetRxIoSampleResponse) response;
 				int remoteId = sample.getRemoteAddress16().get16BitValue();
 				RemoteXBee xbee = xbees.get(remoteId);
 				if (xbee == null) {
 					xbee = new RemoteXBee(Integer.toString(remoteId, 16));
 					xbees.put(xbee.getId(), xbee);
 				}
 				if (sample.containsAnalog()) {
 					for (int pin=0; pin<xbee.getAnalogPinCount(); pin++) {
 						xbee.setAnalog(pin, sample.getAnalog(pin));
 					}
 				}
 				if (sample.containsDigital()) {
 					for (int pin=0; pin<xbee.getDigitalPinCount(); pin++) {
 						xbee.setDigital(pin, sample.isDigitalOn(pin) ? 1 : 0);
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * @return
 	 */
 	public synchronized String[] getCurrentRemoteIds() {
 		String[] remoteIds = new String[xbees.size()];
 		int i=0;
 		for (Iterator<String> ids=xbees.keySet().iterator(); ids.hasNext();) {
 			remoteIds[i++] = ids.next();
 		}
 		return remoteIds;
 	}
 	
 	/**
 	 * Read the latest analog input value from the sample buffer.
 	 * @param remoteId the id of the target device as a hex string
 	 * @param pin
 	 * @return
 	 */
 	public synchronized int analogRead(String remoteId, int pin) {
 		RemoteXBee xbee = xbees.get(remoteId);
 		if (xbee == null) {
 			Logger.debug("No samples from XBee ["+remoteId+"] have been received yet.");
 			return 0;
 		}
 		return xbee.getAnalog(pin);
 	}
 	
 	/**
 	 * Read the latest digital input value from the sample buffer.
 	 * @param remoteId the id of the target device as a hex string
 	 * @param pin
 	 * @return
 	 */
 	public synchronized int digitalRead(String remoteId, int pin) {
 		RemoteXBee xbee = xbees.get(remoteId);
 		if (xbee == null) {
 			Logger.debug("No samples from XBee ["+remoteId+"] have been received yet.");
 			return 0;
 		}
 		return xbee.getDigital(pin);
 	}
 	
 	/**
 	 * See http://code.google.com/p/xbee-api/wiki/DevelopersGuide
 	 * @param remoteId the id of the target device as a hex string, or as "*" for broadcast to all devices
 	 * @param pin
 	 * @param value
 	 */
 	public void digitalWrite(String remoteId, int pin, boolean value) {
 		boolean broadcast = "*".equals(remoteId);
 		// Use the 64 bit address for broadcast to all devices as a defult
 		XBeeAddress64 addr64 = broadcast ? XBeeAddress64.BROADCAST : new XBeeAddress64(new int[8]);
 		// 5 is Digital Output High, 0 is Low
 		RemoteAtRequest request = new RemoteAtRequest(addr64, "D"+pin, new int[] {(value ? 5 : 0)});
 		// If request targets a specific device then use the 16 bit address of the remote device
 		// which overrides the 64 bit address set above. The remote ID can be found in X-CTU app.
 		if (!broadcast) {
 			DoubleByte addr = new DoubleByte(Integer.parseInt(remoteId, 16));
 			request.setRemoteAddr16(new XBeeAddress16(addr.getMsb(), addr.getLsb()));
 		}
 		try {
 			baseStation.sendAsynchronous(request);
 		} catch (XBeeException e) {
 			Logger.warn("Error writing digital value to XBee: "+e);
 		}
 	}
 	
 	/**
 	 * 
 	 */
 	public void dispose() {
 		if (baseStation != null) {
 			baseStation.close();
 			baseStation = null;
 			ThreadUtil.pause(1000);
 		}
 	}
 }

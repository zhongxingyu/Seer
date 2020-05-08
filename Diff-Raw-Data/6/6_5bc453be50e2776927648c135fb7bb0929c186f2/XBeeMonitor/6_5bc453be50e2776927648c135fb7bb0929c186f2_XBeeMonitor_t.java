 package com.buglabs.xbee;
 
 import java.lang.reflect.Constructor;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.service.log.LogService;
 import org.osgi.util.tracker.ServiceTracker;
 
 import com.buglabs.application.ServiceTrackerHelper.ManagedRunnable;
 import com.buglabs.xbee.protocol.BaseXBeeProtocol;
 import com.buglabs.xbee.protocol.XBeeProtocol;
 import com.rapplogic.xbee.api.ApiId;
 import com.rapplogic.xbee.api.AtCommand;
 import com.rapplogic.xbee.api.AtCommandResponse;
 import com.rapplogic.xbee.api.PacketListener;
 import com.rapplogic.xbee.api.XBee;
 import com.rapplogic.xbee.api.XBeeAddress;
 import com.rapplogic.xbee.api.XBeeAddress16;
 import com.rapplogic.xbee.api.XBeeAddress64;
 import com.rapplogic.xbee.api.XBeeException;
 import com.rapplogic.xbee.api.XBeeResponse;
 import com.rapplogic.xbee.api.XBeeTimeoutException;
 import com.rapplogic.xbee.api.wpan.RxBaseResponse;
 import com.rapplogic.xbee.api.wpan.RxResponse;
 import com.rapplogic.xbee.api.wpan.RxResponseIoSample;
 import com.rapplogic.xbee.util.ByteUtils;
 
 public class XBeeMonitor implements ManagedRunnable, Runnable, PacketListener, XBeeController {
 	final int RECONNECT_DELAY = 5000;
 	final int REQUEST_TIMEOUT = 5000;
 	final int LOCAL_POLL_DELAY = 10000;
 	final ApiId[] VALID_APIID = {ApiId.RX_16_IO_RESPONSE,ApiId.RX_16_RESPONSE,
 			ApiId.RX_64_IO_RESPONSE,ApiId.RX_64_RESPONSE,ApiId.REMOTE_AT_RESPONSE};
 	
 	private Thread t;
 	private LogService ls;
 	private boolean running = true;
 	private String devnode = "/dev/ttyUSB0";
 	private int baud = 9600;
 	private Map<XBeeAddress, XBeeProtocol> protocols;
 	private Set<Class> expectedProtocols;
 	private BundleContext _context;
 	private ServiceTracker tracker;
 	
 	public XBee xb;
 	public boolean connected = false;
 	public int[] PANID = new int[2];
 	public int channel = 0;
 	public int[] address = new int[2];
 	public int[] serial = new int[8];
 	
 	@Override
 	public void processResponse(XBeeResponse res) {
 		Map<String,Object> ret = parseResponse(res);
 		if (ret != null)
 			whiteboardNotify(ret);
 	}
 	
 	private Map<String,Object> predictiveParse(XBeeResponse res){
 		Map<String,Object> ret = null;
 		if (res.getApiId() == ApiId.AT_RESPONSE)
 			return null;
 		boolean found = false;
 		for (int i=0;i<VALID_APIID.length;i++)
 			if (res.getApiId() == VALID_APIID[i])
 				found = true;
 		if (!found){
 			return null;
 		}
 		RxBaseResponse pkt = (RxBaseResponse)res;
 		for (Class proto:expectedProtocols){
 			XBeeProtocol candidate = null;
 			try {
 				candidate = (XBeeProtocol) proto.newInstance();
 			} catch (InstantiationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			if (candidate == null){
 				continue;
 			}
 			if (candidate.parseable(res)){
 				candidate.setAddr(pkt.getSourceAddress());
 				addListener(candidate);
 				return candidate.parse(res);
 			}
 		}
 		return null;
 	}
 	
 	private Map<String,Object> parseResponse(XBeeResponse res){
 		//Shortcut - AT_Responses should only be synchronous
 		//Also, ATMY is used as a ping, so we need to filter it out
 		if (res.getApiId() == ApiId.AT_RESPONSE)
 			return null;
 		//First, check this is a packet that contains a remote address
 		boolean found = false;
 		for (int i=0;i<VALID_APIID.length;i++)
 			if (res.getApiId() == VALID_APIID[i])
 				found = true;
 		//We can't process packets with no known sender... Sorry!
 		//TODO - can we support other packet types?
 		if (!found){
 			dlog("Unsupported pkt type recieved: "+res.getApiId().getValue());
 			return null;
 		}
 		RxBaseResponse pkt = (RxBaseResponse)res;
 		Map<String,Object> ret = null;
 		if (protocols.containsKey(pkt.getSourceAddress())){
 			ret = protocols.get(pkt.getSourceAddress()).parse(res);
 			//ret will be null for unparseable data from a protocol
 			//so we shouldn't report unparseable data.	
 			if (ret != null){
 				ret.put("protocol", protocols.get(pkt.getSourceAddress()));
 				ret.put("class", protocols.get(pkt.getSourceAddress()).getClass());
 				ret.put("address", pkt.getSourceAddress().getAddress());
 			} 	
 			return ret;
 		//If we don't know where the packet came from, we can't parse it!
 		//Send the raw (but still unescaped) packet bytes.
 		} else {
 			if (tracker.size() > 0){
 				//If we have listeners, try to guess the protocol for them.
 				dlog("Attempting to guess the protocol...");
 				ret = predictiveParse(res);
 				if (ret != null){
 					dlog("prediction success: "+protocols.get(pkt.getSourceAddress()).getClass().getName());
 					ret.put("protocol", protocols.get(pkt.getSourceAddress()));
 					ret.put("class", protocols.get(pkt.getSourceAddress()).getClass());
 					ret.put("address", pkt.getSourceAddress().getAddress());
 				}
 				return ret;
 			} else {
 				dlog("data(no consumers):"+ByteUtils.toString(pkt.getProcessedPacketBytes()));
 			}
 			return ret;
 		}
 	}
 	
 	private void whiteboardNotify(Map<String,Object> data){
 		Object[] services = tracker.getServices();
 		if (services == null)
 			return;
 		for (Object s:services){
 			((XBeeCallback) s).dataRecieved(data);
 		}
 	}
 	
 	@Override
 	public void addListener(XBeeProtocol proto) {
 		dlog("adding protocol "+ByteUtils.toBase16(proto.getAddr().getAddress())+":"+proto.getClass().getName());
 		protocols.put(proto.getAddr(), proto);
 	}
 	
 	@Override
 	public boolean removeListener(int[] address) {
 		XBeeAddress addr;
 		if (address.length > 2)
 			addr = new XBeeAddress64(address);
 		else
 			addr = new XBeeAddress16(address);
 		if (!protocols.containsKey(addr))
 			return false;
 		dlog("removing protocol at "+ByteUtils.toBase16(addr.getAddress()));
 		protocols.remove(addr);
 		return true;
 	}
 		
 	public XBeeMonitor(BundleContext context){
 		_context = context;
 	}
 	
 	@Override
 	public Map<String, Object> getResponse() {
 		XBeeResponse res;
 		try {
 			res = xb.getResponse(REQUEST_TIMEOUT);
 		} catch (XBeeTimeoutException e) {
 			return null;
 		} catch (XBeeException e) {
 			return null;
 		}
 		if (res == null)
 			return null;
 		return parseResponse(res);
 	}
 
 	@Override
 	public Map<String, Object> getResponse(int[] addr) {
 		long start = System.currentTimeMillis();
 		Map<String,Object> ret = null;
 		getpacket: while ((System.currentTimeMillis()-start) < REQUEST_TIMEOUT){
 			ret = getResponse();
 			int[] recv_array = (int[])ret.get("address");
 			for (int i=0;i<addr.length;i++){
 				if (addr[i] != recv_array[i]){
 					continue getpacket;
 				}
 			}
 			break getpacket;
 		}
 		return ret;
 	}
 	
 	@Override
 	public void addPredictive(Class proto) {
 		expectedProtocols.add(proto);
 	}
 	
 	@Override
 	public void removeAll(Class proto) {
 		boolean success = expectedProtocols.remove(proto);
		Map<XBeeAddress, XBeeProtocol> copyOfProtocols = new HashMap<XBeeAddress, XBeeProtocol>(protocols);
		for (Map.Entry<XBeeAddress, XBeeProtocol> entry : copyOfProtocols.entrySet()){
 			if (entry.getValue().getClass() == proto){
 				dlog("removing protocol at "+ByteUtils.toBase16(entry.getKey().getAddress()));
 				protocols.remove(entry.getKey());
 			}
 		}
 	}
 	
 	@Override
 	public void run(Map<Object, Object> services) {
 		// TODO Auto-generated method stub
 		ls = (LogService) services.get(LogService.class.getName());
 		protocols = new HashMap<XBeeAddress, XBeeProtocol>();
 		expectedProtocols = new HashSet<Class>();
 		t = new Thread(this, "XBee Monitor");
 		t.start();
 		_context.registerService(XBeeController.class.getName(), this, null);
 		tracker = new ServiceTracker(_context, XBeeCallback.class.getName(),null);
 		tracker.open();
 	}
 
 	@Override
 	public void run() {
 		dlog("Monitor thread start");
 		xb = new XBee();
 		running: while(running){
 			PANID = new int[2];
 			channel = 0;
 			address = new int[2];
 			serial = new int[8];
 			try {
 				//Specify system property to force RXTX to take the devnode
 				System.setProperty("gnu.io.rxtx.SerialPorts", devnode);
 				xb.open(devnode, baud);
 			} catch (XBeeException e) {
 				//If we cannot open the port, retry after RECONNECT_DELAY
 				dlog("Cannot open port");
 				try { t.sleep(RECONNECT_DELAY); }
 				//If interrupted, break out and allow thread to close.
 				catch (InterruptedException e1) { break running;}
 				continue running;
 			}
 			connected = true;
 			//get local data:
 			try {
 				PANID = getAtResult("ID");
 				channel = getAtResult("CH")[0];
 				address = getAtResult("MY");
 				int[] serialtemp = getAtResult("SL");
 				for (int i=0;i<4;i++)
 					serial[i] = serialtemp[i];
 				serialtemp = getAtResult("SH");
 				for (int i=0;i<4;i++)
 					serial[i+4] = serialtemp[i];
 			} catch (XBeeTimeoutException e2) {
 			} catch (XBeeException e2) {}
 			ilog("Connected XBee: PAN="+ByteUtils.toBase16(PANID)
 					+" CHANNEL="+ByteUtils.toBase16(channel)
 					+" address="+ByteUtils.toBase16(address)
 					+" serial="+ByteUtils.toBase16(serial));
 			//allows processResponse to receive responses
 			xb.addPacketListener(this);
 			connected: while (connected){
 				try {
 					//Send a local ATMY command - being used as a local "ping"
 					getAtResult("MY");
 					//If local XBee is gone, reconnect
 				} catch (XBeeTimeoutException e) {
 					dlog("Timeout with local XBee");
 					break connected;
 				} catch (XBeeException e) {
 					dlog("Error with local XBee, reconnecting");
 					break connected;
 				}
 				try { t.sleep(LOCAL_POLL_DELAY); } 
 				catch (InterruptedException e1) { break running;}
 			}
 			//Close port in preparation for reconnection
 			connected = false;
 			xb.close();
 			
 		}
 		//Close port before closing thread
 		dlog("Monitor thread stop");
 		xb.close();
 	}
 	
 	public int[] getAtResult(String command) throws XBeeTimeoutException, XBeeException{
 		AtCommandResponse res = (AtCommandResponse) xb.sendSynchronous(new AtCommand(command), REQUEST_TIMEOUT);
 		if (!res.isOk())
 			throw new XBeeException();
 		return res.getValue();
 	}
 	
 	@Override
 	public void shutdown() {
 		running = false;
 		t.interrupt();
 		tracker.close();
 	}
 		
 	//Wrappers for the log service (to standardize logged messages)
 	void ilog(String message){  ls.log(ls.LOG_INFO, "["+this.getClass().getSimpleName()+"] "+message);	}
 	void dlog(String message){  ls.log(ls.LOG_DEBUG, "["+this.getClass().getSimpleName()+"] "+message);	}
 	void elog(String message){  ls.log(ls.LOG_ERROR, "["+this.getClass().getSimpleName()+"] "+message);	}
 	void wlog(String message){  ls.log(ls.LOG_WARNING, "["+this.getClass().getSimpleName()+"] "+message);	}
 
 	@Override
 	public XBee getXBee() {
 		return xb;
 	}
 
 }

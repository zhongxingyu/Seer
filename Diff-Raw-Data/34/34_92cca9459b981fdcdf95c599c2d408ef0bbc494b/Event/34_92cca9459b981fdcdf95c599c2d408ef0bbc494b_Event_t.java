 package com.cs.fwk.sim.types;
 
 import com.cs.fwk.api.Timestamp;
 import com.cs.fwk.util.Conversion;
 
 /**
  * Event data passed between elements
  * 
  * @author pjf
  *
  */
 public class Event implements Comparable<Event> {
 	private final int rawData;
 	private final int src;		// element ID
 	private final int srcPort;
 	private final int instrument_id;
 	private final int tickref;
 	private final boolean isFloat;
 	private Timestamp timestamp;
 	private int tag;
 	
 	/**
	 * Create event with int data and port 1
 	 * 
 	 * @param timestamp
 	 * @param src
 	 * @param instrument_id
 	 * @param tickref
 	 * @param rawData
 	 */
 	public Event(Timestamp timestamp, int src, int instrument_id, int tickref, int rawData) {
 		this.rawData = rawData;
 		this.src = src;
 		this.srcPort = 0;
 		this.instrument_id = instrument_id;
 		this.tickref = tickref;
 		this.isFloat = false;
 		this.timestamp = timestamp;
 		tag = -1;
 	}
 	
 	/**
 	 * Create event with int data and specified port
 	 * 
 	 * @param timestamp
 	 * @param src
	 * @param port (1..4)
 	 * @param instrument_id
 	 * @param tickref
 	 * @param rawData
 	 */
 	public Event(Timestamp timestamp, int src, int port, int instrument_id, int tickref, int rawData) {
 		this.rawData = rawData;
 		this.src = src;
		this.srcPort = port-1;
 		this.instrument_id = instrument_id;
 		this.tickref = tickref;
 		this.isFloat = false;
 		this.timestamp = timestamp;
 		tag = -1;
 	}
 
 
 	/**
 	 * Create event with float data and port 0
 	 * 
 	 * @param timestamp
 	 * @param src
 	 * @param instrument_id
 	 * @param tickref
 	 * @param data
 	 */
 	public Event(Timestamp timestamp, int src, int instrument_id, int tickref, float data) {
 		this.rawData = Float.floatToIntBits(data);
 		this.src = src;
 		this.srcPort = 0;
 		this.instrument_id = instrument_id;
 		this.tickref = tickref;
 		this.isFloat = true;
 		this.timestamp = timestamp;
 		tag = -1;
 	}
 
 	/**
 	 * Create event with float data and specified output port
 	 * 
 	 * @param timestamp
 	 * @param src
 	 * @param port
 	 * @param instrument_id
 	 * @param tickref
 	 * @param data
 	 */
 	public Event(Timestamp timestamp, int src, int port, int instrument_id, int tickref, float data) {
 		this.rawData = Float.floatToIntBits(data);
 		this.src = src;
 		this.srcPort = port;
 		this.instrument_id = instrument_id;
 		this.tickref = tickref;
 		this.isFloat = true;
 		this.timestamp = timestamp;
 		tag = -1;
 	}
 
 	public void setTag(int tag) {
 		this.tag = tag;
 	}
 	
 	public Timestamp getTimestamp() {
 		return timestamp;
 	}
 	
 	public int getRawData() {
 		return rawData;
 	}
 
 	public float getFloatData() {
 		return Float.intBitsToFloat(rawData);
 	}
 
 	public int getSrc() {
 		return src;
 	}
 	
 	public int getSrcPort() {
 		return srcPort;
 	}
 
 	public int getInstrument_id() {
 		return instrument_id;
 	}
 	
 	public int getTickref() {
 		return tickref;
 	}
 	
 	@Override
 	public String toString() {
 		return "[tag=" + tag +
 		" ts=" + timestamp + 
 		" src" + src + ":" + srcPort +
 		",InstrId=" + instrument_id + 
 		",tickref=" + tickref + 
 		",data=" + ((isFloat) ? getFloatData() : "") + " " + Conversion.toHexIntString(rawData) + "]";
 	}
 
 	@Override
 	public int compareTo(Event o) {
 		return timestamp.compareTo(o.timestamp);
 	}
 
 	public void setTimestamp(Timestamp newTime) {
 		this.timestamp = newTime;		
 	}
 	
 	
 }

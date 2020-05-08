 package com.dropoutdesign.ddf.config;
 
 import java.util.*;
 import java.net.*;
 import java.io.*;
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.xml.DomDriver;
 import com.thoughtworks.xstream.io.StreamException;
 
 /**
  * This is a structure meant to be converted to and from XML using XStream.
  */
 
 public class DanceFloorConfig {
 	
 	public List<ModuleConfig> modules;
 	public int framerate;
 	public List<String> ipwhitelist;
 	
	public static final int DEFAULT_MAXFPS = 30;
 
 	private static XStream xstream;
 	static {
 		xstream = new XStream(new DomDriver());
 		xstream.alias("dancefloor", DanceFloorConfig.class);
 		xstream.alias("module", ModuleConfig.class);
 	}
 	
 	public DanceFloorConfig() {
 		modules = new ArrayList<ModuleConfig>();
		maxfps = DEFAULT_MAXFPS;
 		ipwhitelist = new ArrayList<String>();
 		ipwhitelist.add("127.0.0.1");
 	}
 	
 	public static InetAddress stringToAddress(String address) {
 		try {
 			return InetAddress.getByName(address);
 		}
 		catch (UnknownHostException e) {
 			return null;
 		}
 	}
 	
 	public List<InetAddress> getWhitelistAddresses() {
 		List<InetAddress> L = new ArrayList<InetAddress>();
 		for (Iterator iter = ipwhitelist.iterator(); iter.hasNext(); ) {
 			String ip = (String)iter.next();
 			InetAddress address = stringToAddress(ip);
 			if (address != null) {
 				L.add(address);
 			}
 		}
 		return L;
 	}
 	
 	public static DanceFloorConfig readAll(String fileName) throws IOException {
 		Reader r = new BufferedReader(new FileReader(new File(fileName)));
 		DanceFloorConfig df = readAll(r);
 		r.close();
 		return df;
 	}
 	
 	public static DanceFloorConfig readAll(Reader r) {
 		try {
 			return (DanceFloorConfig)xstream.fromXML(r);
 		} catch (StreamException e) {
 			throw new IllegalArgumentException("Malformed XML: "+ e.getMessage());
 		}
 	}
 
 	public void writeAll(Writer w) {
 		xstream.toXML(this, w);
 	}
 
 	public String writeAll() {
 		return xstream.toXML(this);
 	}
 }

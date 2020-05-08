 package org.tyas.upnp.ssdp;
 
 import org.tyas.http.Http;
 import org.tyas.http.HttpResponse;
 import org.tyas.upnp.UpnpUsn;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URI;
 import java.net.URL;
 import java.net.DatagramPacket;
 
 public class SsdpSearchResponse extends HttpResponse implements Ssdp.SearchResponse
 {
	public static class Const extends HttpResponse implements Ssdp.SearchResponse
 	{
 		private Const(HttpResponse.Const c) {
			super(c, Http.VERSION_1_1, "200", "OK");
 		}
 
 		@Override public URL getDescriptionUrl() {
 			try {
 				return getLocation().toURL();
 			} catch (Exception e) {
 				return null;
 			}
 		}
 
 		@Override public String getSearchTarget() { return getFirst(Ssdp.ST); }
 
 		@Override public UpnpUsn getUniqueServiceName() { return UpnpUsn.getByString(getFirst(Ssdp.USN)); }
 
 		@Override public int getBootId() { return getInt(Ssdp.BOOTID, 0); }
 
 		@Override public int getConfigId() { return getInt(Ssdp.CONFIGID, 0); }
 
 		@Override public int getSearchPort() { return getInt(Ssdp.SEARCHPORT, -1); }
 
 		@Override public DatagramPacket toDatagramPacket() throws IOException {
 			return Ssdp.toDatagramPacket(this);
 		}
 	}
 
 	public SsdpSearchResponse() {
 		super(Http.VERSION_1_1, "200", "OK");
 	}
 
 	@Override public URL getDescriptionUrl() {
 		try {
 			return getLocation().toURL();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	@Override public String getSearchTarget() { return getFirst(Ssdp.ST); }
 
 	@Override public UpnpUsn getUniqueServiceName() { return UpnpUsn.getByString(getFirst(Ssdp.USN)); }
 
 	@Override public int getBootId() { return getInt(Ssdp.BOOTID, 0); }
 
 	@Override public int getConfigId() { return getInt(Ssdp.CONFIGID, 0); }
 
 	@Override public int getSearchPort() { return getInt(Ssdp.SEARCHPORT, -1); }
 
 	@Override public DatagramPacket toDatagramPacket() throws IOException {
 		return Ssdp.toDatagramPacket(this);
 	}
 
 	public SsdpSearchResponse setDescriptionUrl(String url) {
 		setLocation(url); return this;
 	}
 
 	public SsdpSearchResponse setSearchTarget(String target) {
 		putFirst(Ssdp.ST, target); return this;
 	}
 
 	public SsdpSearchResponse setUniqueServiceName(String usn) {
 		putFirst(Ssdp.USN, usn); return this;
 	}
 	
 	public SsdpSearchResponse setBootId(int id) {
 		setInt(Ssdp.BOOTID, id); return this;
 	}
 
 	public SsdpSearchResponse setConfigId(int id) {
 		setInt(Ssdp.CONFIGID, id); return this;
 	}
 
 	public SsdpSearchResponse setSearchPort(int port) {
 		setInt(Ssdp.SEARCHPORT, port); return this;
 	}
 
 	public static SsdpSearchResponse.Const getByHttpResponse(HttpResponse.Const resp) {
 		return new SsdpSearchResponse.Const(resp);
 	}
 }

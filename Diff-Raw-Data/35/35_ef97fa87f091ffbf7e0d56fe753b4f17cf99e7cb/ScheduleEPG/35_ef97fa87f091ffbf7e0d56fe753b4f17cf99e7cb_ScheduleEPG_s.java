 package com.wowza.wms.plugin.broadcast;
 
 import java.util.Date;
 import java.util.LinkedList;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 public class ScheduleEPG {
 	
 	private static int COUNT = 5;
 				
 	private final String epgLink;
 	private final int channelId;
 	private LinkedList<ScheduleProgram> list;
 	private String systemTime = null;
 	private final String multicastGroup;
 	private final int multicastPort;
 	private final int channelPort;
 	private final int ttl;
 		
 	public ScheduleEPG(JSONObject obj) {
 		try {
 			this.epgLink = (String) obj.get("epg");
 			this.channelId = ((Long) obj.get("channelid")).intValue();
 			
 			if (obj.get("MulticastGroup") != null)
 				this.multicastGroup = (String) obj.get("MulticastGroup");
 			else
 				this.multicastGroup = null;
 			
 			if (obj.get("MulticastPort") != null)
 				this.multicastPort = ((Long) obj.get("MulticastPort")).intValue();
 			else
 				this.multicastPort = 0;
 			
 			if (obj.get("ChannelPort") != null)
 				this.channelPort = ((Long) obj.get("ChannelPort")).intValue();
 			else
 				this.channelPort = 0;
 			
 			if (obj.get("ttl") != null)
 				this.ttl = ((Long) obj.get("ttl")).intValue();
 			else
 				this.ttl = 0;
 			
 			this.list = new LinkedList<ScheduleProgram>();
 		} catch (Exception e) {
 			throw new RuntimeException("JSON Error");
 		}
 	}
 	
 	public int getChannelId() {
 		return this.channelId;
 	}
 	
 	public Date getSystemTime() {
		return ScheduleProgram.getTimeStamp(this.systemTime);
 	}
 	
 	public String getMulticastGroup() {
 		return this.multicastGroup;
 	}
 	
 	public int getMultivastPort() {
 		return this.multicastPort;
 	}
 	
 	public int getChannelPort() {
 		return this.channelPort;
 	}
 	
 	public int getTimetoLive() {
 		return this.ttl;
 	}
 	
 	private void doUpdate(String link) {
 		list.clear();
 		JSONObject epgList = ScheduleUtils.getJSONObject(link);
 		if (epgList == null) {
 			String ret;
 			ret = "ScheduleEPG: Connection Failed to epg Server";
 			throw new RuntimeException(ret);
 		}
 		systemTime = (String) epgList.get("SystemDateTime");
 		JSONArray epgPrograms = (JSONArray) epgList.get("Programs");
 		for (Object obj : epgPrograms) {
 			list.add(new ScheduleProgram((JSONObject) obj));
 		}
 	}
 	
 	public void update() {
 		String link = epgLink + "?count=" + COUNT;
 		doUpdate(link);
 	}
 	
 	public void update(Date date) {
 		String time = ScheduleProgram.parser.format(date);
 		String link = epgLink + "?totimestamp=" + time;
 		doUpdate(link);
 	}
 	
 	public void update(String epgId) {
 		String link = epgLink + "?count=" + COUNT + "&epgid=" + epgId;
 		doUpdate(link);
 	}
 	
 	public boolean isEmpty() {
 		return list.isEmpty();
 	}
 	
 	public ScheduleProgram getProgram() {
 		return list.pollFirst();
 	}
 	
 	public String getLastEPGId() {
 		if (!list.isEmpty())
 			return list.getLast().getEPGId();
 		else
 			return null;
 	}
 	
 }

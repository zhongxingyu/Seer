 package com.wowza.wms.plugin.multicastpublish;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.wowza.wms.application.IApplicationInstance;
 import com.wowza.wms.application.WMSProperties;
 import com.wowza.wms.logging.WMSLoggerFactory;
 import com.wowza.wms.plugin.broadcast.ScheduleEPG;
 import com.wowza.wms.plugin.broadcast.ScheduleEPGList;
 import com.wowza.wms.rtp.model.RTPDestination;
 
 public class MulticastPublishAddressMapper
 {
 	public static final int MULTICASTADDRESSINCREMENTMODE_PORT = 1;
 	public static final int MULTICASTADDRESSINCREMENTMODE_ADDRESS = 2;
 	
 	private static final long RECYCLETIME_DESTINATION = 10*60*1000;
 	private static final long RECYCLETIME_ADDRESS = 10*60*1000;
 	
 	class MapEntryDestination
 	{
 		RTPDestination rtpDestination = null;
 		
 		public MapEntryDestination(RTPDestination rtpDestination)
 		{
 			this.rtpDestination = rtpDestination;
 		}
 	}
 
 	class MapEntry
 	{
 		String streamName = null;
 		List<MapEntryDestination> destinations = new ArrayList<MapEntryDestination>();
 		long recycleTime = -1;
 		
 		public MapEntry(String streamName)
 		{
 			this.streamName = streamName;
 			this.recycleTime = System.currentTimeMillis();
 		}
 		
 		public void addDestination(MapEntryDestination destination)
 		{
 			destinations.add(destination);
 		}
 		
 		public List<MulticastPublishSessionDestination> getRTPDestinations()
 		{
 			List<MulticastPublishSessionDestination> ret = new ArrayList<MulticastPublishSessionDestination>();
 			
 			Iterator<MapEntryDestination> iter = destinations.iterator();
 			while(iter.hasNext())
 			{
 				MapEntryDestination destination = iter.next();
 				
 				RTPDestination rtpDestination = destination.rtpDestination;
 				if (rtpDestination == null)
 					continue;
 				
 				MulticastPublishSessionDestination sessionDestination = new MulticastPublishSessionDestination();
 				sessionDestination.setRTPDestination(rtpDestination);
 				
 				ret.add(sessionDestination);
 			}
 			return ret;
 		}
 				
 		public Set<String> getAddressSet()
 		{
 			Set<String> ret = new HashSet<String>();
 			
 			Iterator<MapEntryDestination> iter = destinations.iterator();
 			while(iter.hasNext())
 			{
 				MapEntryDestination destination = iter.next();
 				
 				RTPDestination rtpDestination = destination.rtpDestination;
 				if (rtpDestination == null)
 					continue;
 				
 				if (rtpDestination.isStream())
 				{
 					ret.add(rtpDestination.getHost()+":"+rtpDestination.getStreamPort());
 				}
 				else
 				{
 					if (rtpDestination.getAudioPort() > 0)
 						ret.add(rtpDestination.getAudioHost()+":"+rtpDestination.getAudioPort());
 					if (rtpDestination.getVideoPort() > 0)
 						ret.add(rtpDestination.getVideoHost()+":"+rtpDestination.getVideoPort());
 				}
 			}
 			
 			return ret;
 		}
 	}
 	
 	class MyAddress
 	{
 		String host = null;
 		int port = -1;
 	}
 	
 	class RecycleAddress
 	{
 		String host = null;
 		int port = -1;
 		long recycleTime = 0;
 				
 		public RecycleAddress(String host, int port)
 		{
 			this.host = host;
 			this.port = port;
 			this.recycleTime = System.currentTimeMillis();
 		}
 		
 		public String toString()
 		{
 			return this.host+":"+this.port;
 		}
 	}
 	
 	private IApplicationInstance appInstance = null;
 	
 //	private String multicastMapPath = "${com.wowza.wms.context.VHostConfigHome}/conf/multicastmap.txt";
 	private String multicastStartingAddress = "239.1.1.0";
 	private int multicastStartingPort = 10000;
 	private int multicastAddressIncrementMode = MULTICASTADDRESSINCREMENTMODE_ADDRESS;
 	private int multicastAddressIncrement = 0x01;
 	private String multicastMapNameDelimiter = "=";
 	private boolean MPEGTSOut = false;
 	private boolean RTPWrapped = true;
 	private boolean autoAssignAddresses = true;
 
 	private long currMulticastAddress = -1;
 	
 	private Object lock = new Object();
 	
 	private Map<String, MapEntry> streamNameMap = new HashMap<String, MapEntry>();
 	private Map<String, MapEntry> recycleDestinationMap = new HashMap<String, MapEntry>();
 	private List<RecycleAddress> recyleAddressList = new ArrayList<RecycleAddress>();
 	private Set<String> recyleAddressSet = new HashSet<String>();
 //	private long mapFileLastModDate = -1;
 //	private long mapFileLastSize = -1;
 	
 	public void init(IApplicationInstance appInstance)
 	{
 		this.appInstance = appInstance;
 		
 		WMSProperties props = this.appInstance.getProperties();
 
 		multicastStartingAddress = props.getPropertyStr("multicastPublishMulticastStartingAddress", multicastStartingAddress);
 		multicastStartingPort = props.getPropertyInt("multicastPublishMulticastStartingPort", multicastStartingPort);
 //		multicastMapPath = props.getPropertyStr("multicastPublishMulticastMapPath", multicastMapPath);
 		multicastMapNameDelimiter = props.getPropertyStr("multicastPublishMulticastMapNameDelimiter", multicastMapNameDelimiter);
 		multicastAddressIncrement = props.getPropertyInt("multicastPublishMulticastAddressIncrement", multicastAddressIncrement);
 		MPEGTSOut = props.getPropertyBoolean("multicastPublishMPEGTSOut", MPEGTSOut);
 		RTPWrapped = props.getPropertyBoolean("multicastPublishRTPWrapped", RTPWrapped);
 		autoAssignAddresses = props.getPropertyBoolean("multicastPublishAutoAssignAddresses", autoAssignAddresses);
 		
 		String incrementMode = "address";
 		incrementMode = props.getPropertyStr("multicastPublishMulticastAddressIncrementMode", incrementMode);
 		if (incrementMode.toLowerCase().startsWith("port"))
 			multicastAddressIncrementMode = MULTICASTADDRESSINCREMENTMODE_PORT;
 		else
 			multicastAddressIncrementMode = MULTICASTADDRESSINCREMENTMODE_ADDRESS;
 		
 		Map<String, String> pathMap = new HashMap<String, String>();
 		pathMap.put("com.wowza.wms.context.VHost", appInstance.getVHost().getName());
 		pathMap.put("com.wowza.wms.context.VHostConfigHome", appInstance.getVHost().getHomePath());
 		pathMap.put("com.wowza.wms.context.Application", appInstance.getApplication().getName());
 		pathMap.put("com.wowza.wms.context.ApplicationInstance", appInstance.getName());
 		
 //		multicastMapPath =  SystemUtils.expandEnvironmentVariables(multicastMapPath, pathMap);
 //		File file = new File(multicastMapPath);
 //		if (!file.exists())
 //			WMSLoggerFactory.getLogger(MulticastPublishAddressMapper.class).error("MulticastPublishAddressMapper.init["+appInstance.getContextStr()+"]: Multicast map file is missing: "+ multicastMapPath);
 
 		currMulticastAddress = 0;
 		String[] parts = multicastStartingAddress.trim().split("[.]");
 		for(int i=0;i<parts.length;i++)
 		{
 			currMulticastAddress <<= 8;
 
 			String part = parts[i];
 			int partValue = 0;
 			try
 			{
 				partValue = Integer.parseInt(part);
 			}
 			catch(Exception e)
 			{
 			}
 			
 			currMulticastAddress += partValue;
 		}
 		
 		WMSLoggerFactory.getLogger(MulticastPublishAddressMapper.class).info("MulticastPublishAddressMapper.init["+appInstance.getContextStr()+"]: startingAddress:"+multicastAddressToString(currMulticastAddress)+":"+multicastStartingPort+" incMode:"+incrementMode);		
 
 //		loadMapFile();
 		loadMap();
 	}
 	
 	public void addRecycleAddress(String host, int port)
 	{
 		String addressStr = host + ":" + port;
 		synchronized(lock)
 		{
 			if (!recyleAddressSet.contains(addressStr))
 			{
 				RecycleAddress recycleAddress = new RecycleAddress(host, port);
 				recycleAddress.recycleTime = System.currentTimeMillis();
 				recyleAddressList.add(recycleAddress);
 				recyleAddressSet.add(addressStr);
 			}
 		}
 	}
 	
 	public RecycleAddress getRecycleAddress()
 	{
 		RecycleAddress ret = null;
 		
 		synchronized(lock)
 		{
 			if (recyleAddressList.size() > 0)
 			{
 				ret = recyleAddressList.get(0);
 				if ((System.currentTimeMillis() - ret.recycleTime) > RECYCLETIME_ADDRESS)
 				{
 					recyleAddressList.remove(0);
 					recyleAddressSet.remove(ret.toString());
 				}
 				else
 					ret = null;
 			}
 		}
 		
 		return ret;
 	}
 	
 	public void incMulticastAddress()
 	{
 		synchronized(lock)
 		{
 			currMulticastAddress++;
 		}
 	}
 	
 	public void incMulticastPort()
 	{
 		synchronized(lock)
 		{
 			multicastStartingPort += 2;
 		}
 	}
 	
 	public MyAddress getNewMulticastAddress()
 	{
 		MyAddress ret = new MyAddress();
 		
 		synchronized(lock)
 		{
 			ret.host = multicastAddressToString(currMulticastAddress);
 			ret.port = multicastStartingPort;
 			
 			switch(multicastAddressIncrementMode)
 			{
 			case MULTICASTADDRESSINCREMENTMODE_ADDRESS:
 				incMulticastAddress();
 				break;
 			case MULTICASTADDRESSINCREMENTMODE_PORT:
 				incMulticastPort();
 				break;
 			}
 		}
 		
 		return ret;
 	}
 
 	public String multicastAddressToString(long multicastAddress)
 	{
 		String ret = "";
 		
 		for(int i=0;i<4;i++)
 		{
 			ret = (multicastAddress & 0x0ff)+(i>0?".":"")+ret;
 			multicastAddress >>= 8;
 		}
 		
 		return ret;
 	}
 	
 //	public boolean mapFileChanged()
 //	{
 //		long lastModDate = -1;
 //		long lastSize = -1;
 //
 //		try
 //		{
 //			while(true)
 //			{
 //				File file = new File(multicastMapPath);
 //				if (!file.exists())
 //					break;
 //				
 //				lastModDate = file.lastModified();
 //				lastSize = file.length();
 //				break;
 //			}
 //		}
 //		catch(Exception e)
 //		{
 //			WMSLoggerFactory.getLogger(MulticastPublishAddressMapper.class).error("MulticastPublishAddressMapper.loadMapFile: "+ e.toString());
 //		}
 //
 //		return (lastModDate > 0 && lastModDate != this.mapFileLastModDate) || (lastSize > 0 && lastSize != mapFileLastSize);
 //	}
 	
 	public MyAddress parseAddress(String addressStr)
 	{
 		MyAddress ret = new MyAddress();
 		
 		int cloc = addressStr.indexOf(":");
 		if (cloc >= 0)
 		{
 			ret.host = addressStr.substring(0, cloc);
 			try
 			{
 				ret.port = Integer.parseInt(addressStr.substring(cloc+1));
 			}
 			catch(Exception e)
 			{
 			}
 		}
 		else
 		{
 			if (addressStr.indexOf(".") >= 0)
 				ret.host = addressStr;
 			else
 			{
 				try
 				{
 					ret.port = Integer.parseInt(addressStr);
 				}
 				catch(Exception e)
 				{
 				}
 			}
 		}
 		
 		return ret;
 	}
 	
 	public RTPDestination parseAlias(String aliasStr)
 	{
 		RTPDestination rtpDestination = new RTPDestination();
 		
 		aliasStr = aliasStr.trim();
 		
 		if (aliasStr.startsWith("{"))
 			aliasStr = aliasStr.substring(1);
 		if (aliasStr.endsWith("}"))
 			aliasStr = aliasStr.substring(0, aliasStr.length()-1);
 		
 		//{video:239.1.1.1:10000,audio:239.1.1.2:10000}
 		
 		String[] parts = aliasStr.split(",");
 		for(int i=0;i<parts.length;i++)
 		{
 			String part = parts[i].trim();
 			if (part.length() <= 0)
 				continue;
 			
 			int cloc = part.indexOf(":");
 			if (cloc < 0)
 				continue;
 			
 			String name = part.substring(0, cloc).trim();
 			String value = part.substring(cloc+1).trim();
 			
 			//System.out.println("****** name:"+name+"="+value);
 			
 			if (name.equalsIgnoreCase("video"))
 			{
 				MyAddress addressObj = parseAddress(value);
 				if (addressObj.host != null)
 					rtpDestination.setVideoHost(addressObj.host);
 				if (addressObj.port > 0)
 					rtpDestination.setVideoPort(addressObj.port);
 			}
 			else if (name.equalsIgnoreCase("audio"))
 			{
 				MyAddress addressObj = parseAddress(value);
 				if (addressObj.host != null)
 					rtpDestination.setAudioHost(addressObj.host);
 				if (addressObj.port > 0)
 					rtpDestination.setAudioPort(addressObj.port);
 			}
 			else if (name.equalsIgnoreCase("stream"))
 			{
 				MyAddress addressObj = parseAddress(value);
 				if (addressObj.host != null)
 					rtpDestination.setHost(addressObj.host);
 				if (addressObj.port > 0)
 					rtpDestination.setStreamPort(addressObj.port);
 			}
 			else if (name.equalsIgnoreCase("audioPort"))
 			{
 				int port = -1;
 				try
 				{
 					port = Integer.parseInt(value);
 				}
 				catch(Exception e)
 				{
 				}
 				if (port > 0)
 					rtpDestination.setAudioPort(port);
 			}
 			else if (name.equalsIgnoreCase("videoPort"))
 			{
 				int port = -1;
 				try
 				{
 					port = Integer.parseInt(value);
 				}
 				catch(Exception e)
 				{
 				}
 				if (port > 0)
 					rtpDestination.setVideoPort(port);
 			}
 			else if (name.equalsIgnoreCase("streamPort"))
 			{
 				int port = -1;
 				try
 				{
 					port = Integer.parseInt(value);
 				}
 				catch(Exception e)
 				{
 				}
 				if (port > 0)
 					rtpDestination.setStreamPort(port);
 			}
 			else if (name.equalsIgnoreCase("audioHost"))
 			{
 				rtpDestination.setAudioHost(value);
 			}
 			else if (name.equalsIgnoreCase("videoHost"))
 			{
 				rtpDestination.setVideoHost(value);
 			}
 			else if (name.equalsIgnoreCase("host"))
 			{
 				rtpDestination.setHost(value);
 			}
 			else if (name.equalsIgnoreCase("hostType"))
 			{
 				rtpDestination.setHostType(value);
 			}
 			else if (name.equalsIgnoreCase("name"))
 			{
 				rtpDestination.setName(value);
 			}
 			else if (name.equalsIgnoreCase("ttl"))
 			{
 				int ttl = -1;
 				try
 				{
 					ttl = Integer.parseInt(value);
 				}
 				catch(Exception e)
 				{
 				}
 				
 				//System.out.println("**** setTTL: "+ttl);
 				if (ttl > 0)
 					rtpDestination.setTTL(ttl);
 			}
 			else if (name.equalsIgnoreCase("isRTPWrapped"))
 			{
 				value = value.toLowerCase();
 				boolean isRTPWrapped = value.startsWith("t") || value.startsWith("y");
 				rtpDestination.setRTPWrapped(isRTPWrapped);
 			}
 		}
 		
 		return rtpDestination;
 	}
 	
 //	public void loadMapFile()
 //	{
 //		Map<String, MapEntry> newMap = new HashMap<String, MapEntry>();
 //		
 //		long lastModDate = -1;
 //		long lastSize = -1;
 //		boolean successful = false;
 //		
 //		BufferedReader inf = null;
 //		try
 //		{
 //			while(true)
 //			{
 //				File file = new File(multicastMapPath);
 //				if (!file.exists())
 //					break;
 //				
 //				inf = new BufferedReader(new FileReader(file));
 //				String line;
 //				while ((line = inf.readLine()) != null)
 //				{
 //					line = line.trim();
 //					if (line.startsWith("#"))
 //						continue;
 //					if (line.length() == 0)
 //						continue;
 //					
 //					String streamName = null;
 //					String alias = null;
 //					int pos = line.indexOf(multicastMapNameDelimiter);
 //					if (pos >= 0)
 //					{
 //						streamName = line.substring(0, pos).trim();
 //						alias = line.substring(pos+1).trim();
 //					}
 //					else
 //						continue;
 //					
 //					if (streamName != null && alias != null)
 //					{
 //						MapEntry mapEntry = newMap.get(streamName);
 //						if (mapEntry == null)
 //						{
 //							mapEntry = new MapEntry(streamName);
 //							newMap.put(streamName, mapEntry);
 //						}
 //						
 //						RTPDestination rtpDestination = parseAlias(alias);
 //
 //						mapEntry.addDestination(new MapEntryDestination(rtpDestination));
 //
 //						//WMSLoggerFactory.getLogger(MulticastPublishAddressMapper.class).info(streamName+"="+rtpDestination.toString());
 //					}
 //					
 //				}
 //				inf.close();
 //				inf = null;
 //				
 //				lastModDate = file.lastModified();
 //				lastSize = file.length();
 //				successful = true;
 //				break;
 //			}
 //		}
 //		catch(Exception e)
 //		{
 //			WMSLoggerFactory.getLogger(MulticastPublishAddressMapper.class).error("MulticastPublishAddressMapper.loadMapFile: "+ e.toString());
 //			e.printStackTrace();
 //		}
 //		
 //		try
 //		{
 //			if (inf != null)
 //				inf.close();
 //			inf = null;	
 //		}
 //		catch(Exception e)
 //		{
 //			WMSLoggerFactory.getLogger(MulticastPublishAddressMapper.class).error("MulticastPublishAddressMapper.loadMapFile[close]: "+ e.toString());
 //		}
 //
 //		if (successful)
 //		{
 //			synchronized(lock)
 //			{
 //				streamNameMap.clear();
 //				streamNameMap.putAll(newMap);
 //				
 //				if (lastModDate > 0)
 //				{
 //					this.mapFileLastModDate = lastModDate;
 //					this.mapFileLastSize = lastSize;
 //				}
 //				
 //				WMSLoggerFactory.getLogger(MulticastPublishAddressMapper.class).info("MulticastPublishAddressMapper.loadMapFile: entries:"+ streamNameMap.size());
 //			}
 //		}
 //	}
 	
 	public void loadMap() {
 		Map<String, MapEntry> newMap = new HashMap<String, MapEntry>();
 		boolean successful = false;
 		
 		try {
 			ScheduleEPGList epglist = new ScheduleEPGList("http://api.nsbg.foxconn.com/0/channels/broadcast");
 			epglist.init();
 			Iterator<Integer> iter = epglist.getChannelIdSet().iterator();
 			while (iter.hasNext()) {
 				ScheduleEPG epg = epglist.getScheduleEPG(iter.next());
 				String streamName = "stream" + epg.getChannelId();
 				
 				if (epg.getMulticastGroup() == null || epg.getMultivastPort() == 0)
 					continue;
 				
 				StringBuffer alias = new StringBuffer();
 				alias.append('{');
 				alias.append("name:").append(streamName).append(',');
 				alias.append("video:").append(epg.getMulticastGroup()).append(':').append(epg.getMultivastPort()).append(',');
 				alias.append("audio:").append(epg.getMulticastGroup()).append(':').append(epg.getMultivastPort()+2).append(',');
 				alias.append("isRTPWrapped:").append("false");
 				alias.append('}');
 				WMSLoggerFactory.getLogger(MulticastPublishAddressMapper.class).info("=========================" + alias.toString());
 				MapEntry mapEntry = newMap.get(streamName);
 				if (mapEntry == null) {
 					mapEntry = new MapEntry(streamName);
 					newMap.put(streamName, mapEntry);
 				}
 				RTPDestination rtpDestination = parseAlias(alias.toString());
 				mapEntry.addDestination(new MapEntryDestination(rtpDestination));
 			}
 			successful = true;
 		} catch (Exception e) {
 			WMSLoggerFactory.getLogger(MulticastPublishAddressMapper.class).error("MulticastPublishAddressMapper.loadMap: "+ e.toString());
 			e.printStackTrace();
 		}
 		
 		if (successful)	{
 			synchronized(lock) {
 				streamNameMap.clear();
 				streamNameMap.putAll(newMap);
 				WMSLoggerFactory.getLogger(MulticastPublishAddressMapper.class).info("MulticastPublishAddressMapper.loadMapFile: entries:"+ streamNameMap.size());
 			}
 		}
 	}
 	
 	public void work()
 	{
 //		if (mapFileChanged())
 //			loadMapFile();
 //		loadMap();
 		
 		long currTime = System.currentTimeMillis();
 		
 		List<String> localStreamNames = new ArrayList<String>();
 		synchronized(lock)
 		{
 			localStreamNames.addAll(recycleDestinationMap.keySet());
 		}
 		Iterator<String> iter = localStreamNames.iterator();
 		while(iter.hasNext())
 		{
 			String streamName = iter.next();
 			synchronized(lock)
 			{
 				MapEntry mapEntry = recycleDestinationMap.get(streamName);
 				if (mapEntry != null)
 				{
 					if ((currTime - mapEntry.recycleTime) > RECYCLETIME_DESTINATION)
 					{
 						Set<String> recycleSet = mapEntry.getAddressSet();
 						
 						Iterator<String> iterS = recycleSet.iterator();
 						while(iterS.hasNext())
 						{
 							String addressStr = iterS.next();
 							MyAddress myAddress = parseAddress(addressStr);
 							RecycleAddress recycleAddress = new RecycleAddress(myAddress.host, myAddress.port);
 							recyleAddressList.add(recycleAddress);
 						}
 
 						recycleDestinationMap.remove(streamName);
 					}
 				}
 			}
 		}
 		
 	}
 	
 	private void recycleAddressSet(Set<String> recycleSet)
 	{
 		if (recycleSet == null)
 			return;
 		
 		if (recycleSet.size() <= 0)
 			return;
 		
 		synchronized(lock)
 		{
 			Iterator<String> iterS = recycleSet.iterator();
 			while(iterS.hasNext())
 			{
 				String addressStr = iterS.next();
 				MyAddress myAddress = parseAddress(addressStr);
 				RecycleAddress recycleAddress = new RecycleAddress(myAddress.host, myAddress.port);
 				recyleAddressList.add(recycleAddress);
 			}
 		}
 	}
 	
 	public void getDestinations(MulticastPublishSession publishSession)
 	{
 		synchronized(lock)
 		{
 			String streamName = publishSession.getStreamName();
 			
 			MapEntry recycleEntry = recycleDestinationMap.remove(streamName);
 			MapEntry mapEntry = streamNameMap.get(streamName);
 			
 			Set<String> recycleSet = recycleEntry==null?null:recycleEntry.getAddressSet();
 
 			publishSession.clearRTPPublishDestination();
 
 			if (mapEntry != null)
 			{
 				publishSession.addAllRTPPublishDestination(mapEntry.getRTPDestinations());
 				if (recycleSet != null)
 					recycleSet.removeAll(mapEntry.getAddressSet());
 			}
 			else if (recycleEntry != null)
 			{
 				publishSession.addAllRTPPublishDestination(recycleEntry.getRTPDestinations());
 				recycleSet = null;
 			}
 			else if (autoAssignAddresses)
 			{
 				if (MPEGTSOut)
 				{
 					String host = null;
 					int port = -1;
 
 					RecycleAddress recycleAddress = getRecycleAddress();
 					if (recycleAddress != null)
 					{
 						host = recycleAddress.host;
 						port = recycleAddress.port;
 					}
 					else
 					{
 						MyAddress myAddress = this.getNewMulticastAddress();
 						host = myAddress.host;
 						port = myAddress.port;
 					}
 					
 					MulticastPublishSessionDestination sessionDestination = new MulticastPublishSessionDestination();
 					
 					RTPDestination rtpDestination = new RTPDestination();
 					rtpDestination.setHost(host);
 					rtpDestination.setStreamPort(port);
 					rtpDestination.setRTPWrapped(RTPWrapped);
 					sessionDestination.setRTPDestination(rtpDestination);
 					
 					publishSession.addRTPPublishDestination(sessionDestination);
 				}
 				else
 				{
 					String hostAudio = null;
 					int portAudio = -1;
 
 					RecycleAddress recycleAddressAudio = getRecycleAddress();
 					if (recycleAddressAudio != null)
 					{
 						hostAudio = recycleAddressAudio.host;
 						portAudio = recycleAddressAudio.port;
 					}
 					else
 					{
 						MyAddress myAddress = this.getNewMulticastAddress();
 						hostAudio = myAddress.host;
 						portAudio = myAddress.port;
 					}
 					
 					String hostVideo = null;
 					int portVideo = -1;
 					RecycleAddress recycleAddressVideo = getRecycleAddress();
 					if (recycleAddressVideo != null)
 					{
 						hostVideo = recycleAddressVideo.host;
 						portVideo = recycleAddressVideo.port;
 					}
 					else
 					{
 						MyAddress myAddress = this.getNewMulticastAddress();
 						hostVideo = myAddress.host;
 						portVideo = myAddress.port;
 					}
 
 					MulticastPublishSessionDestination sessionDestination = new MulticastPublishSessionDestination();
 					
 					RTPDestination rtpDestination = new RTPDestination();
 					rtpDestination.setAudioHost(hostAudio);
 					rtpDestination.setAudioPort(portAudio);
 					rtpDestination.setVideoHost(hostVideo);
 					rtpDestination.setVideoPort(portVideo);
 					sessionDestination.setRTPDestination(rtpDestination);
 					
 					publishSession.addRTPPublishDestination(sessionDestination);
 				}
 			}
 			
 			if (recycleSet != null)
 				recycleAddressSet(recycleSet);
 		}
 	}
 	
 	public void recycleDestinations(MulticastPublishSession publishSession)
 	{
 		synchronized(lock)
 		{
 			String streamName = publishSession.getStreamName();
 
 			MapEntry mapEntry = new MapEntry(streamName);
 
 			List<MulticastPublishSessionDestination> rtpDestinations = publishSession.getRTPPublishDestinations();
 			Iterator<MulticastPublishSessionDestination> iterd = rtpDestinations.iterator();
 			while(iterd.hasNext())
 			{
 				MulticastPublishSessionDestination destination = iterd.next();
 				if (destination == null)
 					continue;
 				
 				RTPDestination rtpDestination = destination.getRTPDestination();
 				if (rtpDestination == null)
 					continue;
 				
 				mapEntry.addDestination(new MapEntryDestination(rtpDestination));
 			}
 			
 			MapEntry recycleEntry = recycleDestinationMap.remove(streamName);
 			recycleDestinationMap.put(streamName, mapEntry);
 			
 			if (recycleEntry != null)
 			{
 				Set<String> recycleSet = recycleEntry.getAddressSet();
 				recycleSet.removeAll(mapEntry.getAddressSet());
 				recycleAddressSet(recycleSet);
 			}
 		}
 	}
 }

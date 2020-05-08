 package vdrdataservice;
 
 import java.io.File;
 import java.util.*;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.hampelratte.svdrp.Response;
 import org.hampelratte.svdrp.commands.LSTC;
 import org.hampelratte.svdrp.commands.LSTE;
 import org.hampelratte.svdrp.util.ChannelParser;
 
 import tvdataservice.MutableChannelDayProgram;
 import tvdataservice.MutableProgram;
 import tvdataservice.SettingsPanel;
 import tvdataservice.TvDataUpdateManager;
 import util.exc.TvBrowserException;
 import util.ui.Localizer;
 import devplugin.*;
 import devplugin.Date;
 
 /**
  * @author <a href="hampelratte@users.sf.net>hampelratte@users.sf.net </a>
  * 
  */
 public class VDRDataService extends AbstractTvDataService {
 	
 	private static Logger logger = Logger.getLogger(VDRDataService.class.getName());
 
 	private Localizer localizer = Localizer.getLocalizerFor(VDRDataService.class);
   
 	private VDRDataServiceSettingsPanel settingsPanel;
 
     private Channel[] channels = new Channel[] {};
 
     private Properties props = new Properties();
     
     private ChannelGroup cg = new ChannelGroupImpl("vdr", "VDR", "Channels from VDR", "VDR");
 
     private PluginInfo pluginInfo = new PluginInfo(
     		VDRDataService.class,
     		"VDRDataService",
     		localizer.msg("desc", "Loads the EPG-Data from VDR (by Klaus Schmidinger) into TV-Browser"),
             "Henrik Niehaus (hampelratte@users.sf.net)");
     
    
 
     /*
      * (non-Javadoc)
      * 
      * @see tvdataservice.TvDataService#updateTvData(tvdataservice.TvDataUpdateManager,
      *      devplugin.Channel[], devplugin.Date, int, devplugin.ProgressMonitor)
      */
     public void updateTvData(TvDataUpdateManager database, Channel[] channels,
             devplugin.Date date, int dateCount, ProgressMonitor pm)
             throws TvBrowserException {
         
     	pm.setMaximum(channels.length);
         if(channels.length > 0) {
 	        for (int i = 0; i < channels.length; i++) {
 	            //pm.setMessage(localizer.msg("getting_data","Getting data from VDR for " + channels[i].getName()));
                 pm.setMessage("Getting data from VDR for " + channels[i].getName());
 	            Response res = VDRConnection.send(new LSTE(channels[i].getId(), ""));
 	            if (res != null && res.getCode() == 215) {
 	                String data = res.getMessage();
 	                pm.setMessage(localizer.msg("parsing_data","Parsing data"));
 	                MutableChannelDayProgram[] dayPrograms = parseData(data,
 	                        channels[i], date, dateCount);
 	                pm.setMessage(localizer.msg("updating_database","Updating EPG database"));
 	                for (int j = 0; j < dayPrograms.length; j++) {
 	                	if(dayPrograms[j].getProgramCount() > 0) {
 	                		database.updateDayProgram(dayPrograms[j]);
 	                	}
 	                }
 	            } else {
                     pm.setMessage(channels[i].getName() +" Error "+res.getCode()+": " + res.getMessage());
 	            }
 	            
 	            pm.setValue(i);
 	        }
         }
         pm.setMessage(localizer.msg("success","Successfully retrieved data from VDR"));
     }
 
     private MutableChannelDayProgram[] parseData(String data, Channel channel,
             Date date, int dateCount) {
 
         ArrayList dayProgramList = new ArrayList();
         MutableChannelDayProgram dayProgram = null;
         MutableProgram program = null;
         Calendar start = date.getCalendar();
         Calendar stop = date.getCalendar();
         stop.add(Calendar.DAY_OF_MONTH, dateCount - 1);
         int currentDay = -1;
         boolean dayOK = false;
 
         StringTokenizer st = new StringTokenizer(data, "\n");
         while (st.hasMoreTokens()) {
             String line = st.nextToken();
             if (line.startsWith("E ")) {
                 String[] parts = line.split(" ");
                 int startTime = Integer.parseInt(parts[2]);
                 java.util.Date d = new java.util.Date(startTime * 1000L);
                 Calendar progTime = Calendar.getInstance();
                 progTime.setTimeInMillis(d.getTime());
                 if (currentDay != progTime.get(Calendar.DAY_OF_MONTH)) {
                     if (dayProgram != null) {
                         dayProgramList.add(dayProgram);
                     }
                     dayProgram = new MutableChannelDayProgram(
                             new Date(progTime), channel);
                     currentDay = progTime.get(Calendar.DAY_OF_MONTH);
                 }
 
                 if ((progTime.after(start) && progTime.before(stop))
                         || progTime.get(Calendar.DAY_OF_MONTH) == start
                                 .get(Calendar.DAY_OF_MONTH)) {
                     program = new MutableProgram(channel, new Date(progTime),
                            progTime.get(Calendar.HOUR_OF_DAY), progTime
                                    .get(Calendar.MINUTE));
                     dayOK = true;
                 } else {
                     continue;
                 }
 
             } else if (line.startsWith("D ") && dayOK) {
                 program.setDescription(line.substring(2));
                 program.setShortInfo(line.substring(2));
             } else if (line.startsWith("T ") && dayOK) {
                 program.setTitle(line.substring(2));
             } else if (line.startsWith("e") && dayOK) {
                 dayProgram.addProgram(program);
                 dayOK = false;
             }
         }
 
         MutableChannelDayProgram[] progs = new MutableChannelDayProgram[] {};
         progs = (MutableChannelDayProgram[]) dayProgramList.toArray(progs);
         return progs;
     }
 
 
     public void loadSettings(Properties p) {
         // set defaults
         props.setProperty("vdr.host", "htpc");
         props.setProperty("vdr.port", "2001");
         props.setProperty("max.channel.number", "100");
 
         // overwrite defaults with values from configfile
         ArrayList list = new ArrayList();
         Enumeration en = p.keys();
         while (en.hasMoreElements()) {
             String key = (String) en.nextElement();
             props.setProperty(key, p.getProperty(key));
         }
         
         // create channel[]
         en = props.keys();
         while(en.hasMoreElements()) {
             String key = (String)(en.nextElement());
 	        if(key.startsWith("CHANNELID")) {
 	            String id = getChannelID(key);
 	            if(!id.equals("")) {
 	                String channelID = props.getProperty("CHANNELID"+id);
 	                String name = props.getProperty("CHANNELNAME"+id);
 	                name = name == null ? "Channel("+id+")" : name;
 	                String url = props.getProperty("CHANNELURL"+id);
 	                url = url == null ? "" : url;
 	                String country = props.getProperty("CHANNELCOUNTRY"+id);
 	                country = country == null ? Locale.getDefault().getCountry() : country;
 	                String copy = props.getProperty("CHANNELCOPYRIGHT"+id);
 	                int category = 0;
 	                try {
 						category = Integer.parseInt(props.getProperty("CHANNELCATEGORY"+id));
 					} catch (NumberFormatException e) {}
 	                copy = copy == null ? "" : copy;
 	                Channel chan = new Channel(this, name, channelID, TimeZone.getDefault(), "de", "", "", cg, null, category); 
 	                	//new Channel(this, name, channelID, TimeZone.getDefault(), country, copy, "", cg);
 	                list.add(chan);
 	            }
 	        }
         }
         
         channels = (Channel[])list.toArray(channels);
 
         VDRConnection.host = props.getProperty("vdr.host");
         VDRConnection.port = Integer.parseInt(props.getProperty("vdr.port"));
         
         settingsPanel = new VDRDataServiceSettingsPanel(this);
     }
     
     
     public String getProperty(String key) {
       return props.getProperty(key);
     }
     
     public void setProperty(String key, String value) {
       props.setProperty(key, value);
     }
     
     private String getChannelID(String s) {
         Pattern p = Pattern.compile("^([A-Z]+)(\\d+)$");
         Matcher m = p.matcher(s);
         if(m.matches()) {
             String id = m.group(2);
             return id;
         }
         return "";
     }
 
     public Properties storeSettings() {
     	// delete old channels
     	sweepOffChannelsFromProps();
     	
     	// save the current channels
         for (int i = 0; i < channels.length; i++) {
             if (channels[i].getId() != null) {
                 props.setProperty("CHANNELID" + i, channels[i].getId());
             }
             if (channels[i].getName() != null) {
                 props.setProperty("CHANNELNAME" + i, channels[i].getName());
             }
             if (channels[i].getWebpage() != null) {
                 props.setProperty("CHANNELURL" + i, channels[i].getWebpage());
             }
             if (channels[i].getCountry() != null) {
                 props.setProperty("CHANNELCOUNTRY" + i, channels[i]
                         .getCountry());
             }
             
             props.setProperty("CHANNELCATEGORY" + i, ""+channels[i].getCategories());
             
             if (channels[i].getCopyrightNotice() != null) {
                 props.setProperty("CHANNELCOPYRIGHT" + i, channels[i]
                         .getCopyrightNotice());
             }
         }
         return props;
     }
 
     private void sweepOffChannelsFromProps() {
 		for (Iterator iterator = props.keySet().iterator(); iterator.hasNext();) {
 			String key = (String) iterator.next();
 			if(key.startsWith("CHANNEL")) {
 				iterator.remove();
 			}
 		}
 	}
 
 	public boolean hasSettingsPanel() {
         return true;
     }
     
     public SettingsPanel getSettingsPanel() {
         return settingsPanel;
     }
 
     public boolean supportsDynamicChannelList() {
         return true;
     }
 
     public PluginInfo getInfo() {
     	return pluginInfo;
     }
 
     public ChannelGroup[] getAvailableGroups() {
         ChannelGroup[] cgs = new ChannelGroup[] {cg};
         return cgs;
     }
     
 
     public Channel[] getAvailableChannels(ChannelGroup cg) {
         if(cg.equals(this.cg)) {
         	logger.info("Returning " + channels.length + " channels");
         	return channels;
         } else {
             return new Channel[] {};
         }
     }
 
     public Channel[] checkForAvailableChannels(ChannelGroup cg, ProgressMonitor pm) throws TvBrowserException {
         if(cg.getId().equals(this.cg.getId())) {
         	Channel[] channels = new Channel[] {};
             pm.setMessage(localizer.msg("getting_data","Getting data from VDR..."));
             // load channel list from vdr
             Response res = VDRConnection.send(new LSTC());
             if(res.getCode() == 250) {
             	// parse the channel list
             	List<org.hampelratte.svdrp.responses.highlevel.Channel> vdrChannelList = ChannelParser.parse(res.getMessage());
             	List channelList = new ArrayList();
             	for (Iterator iterator = vdrChannelList.iterator(); iterator.hasNext();) {
 					org.hampelratte.svdrp.responses.highlevel.Channel vdrChan = (org.hampelratte.svdrp.responses.highlevel.Channel) iterator.next();
 					int maxChannel = Integer.parseInt(props.getProperty("max.channel.number"));
 					if(maxChannel == 0 || maxChannel > 0 && vdrChan.getChannelNumber() <= maxChannel) {
 						// distinguish between radio and tv channels / pay-tv
 						int category = getChannelCategory(vdrChan);
 						// create a new tvbrowser channel object
 						Channel chan = new Channel(this, vdrChan.getName(), Integer.toString(vdrChan.getChannelNumber()),
 								TimeZone.getDefault(), "de", "", "", cg, null, category);
 	                    channelList.add(chan);
 					}
 				}
             	
             	// convert channel list to an array
                 channels = (Channel[]) channelList.toArray(channels);
             }
             this.channels = channels;
             return channels;
         } else {
             return new Channel[] {};
         }
     }
     
     private int getChannelCategory(org.hampelratte.svdrp.responses.highlevel.Channel chan) {
     	String vpid = chan.getVPID();
     	if("0".equals(vpid) || "1".equals(vpid)) { // a radio station
     		return Channel.CATEGORY_RADIO;
     	} else { // a tv channel
     		if(!"0".equals(chan.getConditionalAccess())) {
     			return Channel.CATEGORY_PAY_TV;
     		} else {
     			return Channel.CATEGORY_DIGITAL;
     		}
     	}
     }
 
     @Override
     public ChannelGroup[] checkForAvailableChannelGroups(ProgressMonitor arg0) throws TvBrowserException {
         return getAvailableGroups();
     }
 
     public boolean supportsDynamicChannelGroups() {
         return false;
     }
 
 	@Override
 	public void setWorkingDirectory(File dataDir) {}
 
 	public static Version getVersion() {
 		return new Version(0,1);
 	}
 }

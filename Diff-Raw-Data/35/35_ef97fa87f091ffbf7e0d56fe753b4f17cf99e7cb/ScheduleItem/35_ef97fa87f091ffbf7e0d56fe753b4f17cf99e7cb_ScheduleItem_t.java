 package com.wowza.wms.plugin.broadcast;
 
 import java.io.File;
 import java.nio.file.Paths;
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.wowza.wms.application.IApplicationInstance;
 import com.wowza.wms.logging.WMSLogger;
 import com.wowza.wms.logging.WMSLoggerFactory;
 import com.wowza.wms.stream.publish.IStreamActionNotify;
 import com.wowza.wms.stream.publish.Playlist;
 import com.wowza.wms.stream.publish.PlaylistItem;
 import com.wowza.wms.stream.publish.Stream;
 
 public class ScheduleItem implements IStreamActionNotify {
 	
 	private boolean onCache = false;
 	private Timer mTimer;
 	private TimerTask mTask;
 	private IApplicationInstance appInstance;
 	private Stream stream;
 	private Playlist playlist;
 	private ScheduleEPG epg = null;
 	private Date startTime;
 	private static WMSLogger log = WMSLoggerFactory.getLogger(ScheduleItem.class);
 	
 	
 	public ScheduleItem(IApplicationInstance appInstance, ScheduleEPG epg) {
 		this.appInstance = appInstance;
 		this.epg = epg;
 		this.mTimer = new Timer();
 		this.onCache = appInstance.getProperties().getPropertyBoolean("ScheduleOnCache", onCache);
 	}
 	
 	@Override
 	public void onPlaylistItemStart(Stream stream, PlaylistItem playlistItem) {
 		if (playlistItem.getIndex() == (stream.getPlaylist().size() - 1)) {
 			if (playlist.getName() != null)
 				loadScheduleItem(playlist.getName());
 		}
 		
 		try {
 			log.info("Schedule PlayList Item Start: " + playlistItem.getName());
 		} catch(Exception ex) {
 			log.error("Schedule Get Item error: " + ex.getMessage());
 		}
 	}
 
 	@Override
 	public void onPlaylistItemStop(Stream stream, PlaylistItem playlistItem) {
 		log.info("Schedule PlayList Item Stop: " + playlistItem.getName());
 	}
 	
 	public void loadScheduleItem(String epgId) {
 		try {
 			if (epgId == null)
 				epg.update();
 			else
 				epg.update(epgId);
 		} catch (Exception e) {
 			log.error(e.getMessage());
 			return ;
 		}
 		
 		playlist = new Playlist(epg.getLastEPGId());
 		playlist.setRepeat(false);
 		
		ScheduleProgram program = null;
 		boolean isFirstProgram = true;
 		while ((program = epg.getProgram()) != null) {
 			String src = (onCache) ? ScheduleCache.CACHE_DIRECTORY + "/" + program.getFileName() : program.getUri();
 			int filmTime = ((Long) program.getFilmTime()).intValue();
			File srcfile = Paths.get("/").resolve(appInstance.getStreamStorageDir() + "/" + src).toFile();
 			if (!srcfile.exists()) {
				log.error("ScheduleItem ChannelId "+ epg.getChannelId() + ": File is Not Found: " + srcfile.getPath());
 				return ;
 			}
 			StringBuilder sb = new StringBuilder();
 			sb.append(src).insert(src.lastIndexOf('/') + 1, "mp4:");
 			
 			if (isFirstProgram) {
 				startTime = program.getStartTimeStamp();
 				int sub = Long.valueOf((epg.getSystemTime().getTime() - startTime.getTime())/1000).intValue();
 				if (sub > 0 && epgId == null) {
 					playlist.addItem(sb.toString(), sub, filmTime-sub);
 				} else {
 					playlist.addItem(sb.toString(), 0, filmTime);
 				}
 				isFirstProgram = false;
 			} else {
 				playlist.addItem(sb.toString(), 0, filmTime);
 			}
 		}
 		
		if (isFirstProgram) {
			log.warn("ScheduleItem ChannelId "+ epg.getChannelId() + ": No EPG to play");
			return ;
		}
		
 		mTask = new TimerTask() {
 			public void run() {
 				if(playlist.open(stream))
 					log.info("ScheduledItem: '" + stream.getName() + "' is now live");
 				else {
 					log.warn("ScheduledItem: '" + stream.getName() + "' is NOT live");
 					close();
 					open();
 				}
 			}
 		};
 		
 		if (mTimer == null)
 			mTimer = new Timer();
 		mTimer.schedule(mTask, startTime);
 		log.info("ScheduledItem: " + stream.getName() + " for: " + startTime);
 	}
 	
 	public void open() {
 		String streamName = "stream" + epg.getChannelId();
 		if (stream != null)
 			stream.close();
 		stream = Stream.createInstance(appInstance, streamName);
 		Boolean passThruMetaData = appInstance.getProperties().getPropertyBoolean("PassthruMetaData", true);
 		stream.setSendOnMetadata(passThruMetaData);
 		stream.addListener(this);
 		this.loadScheduleItem(null);
 	}
 	
 	public void close() {
 		stream.removeListener(this);
 		if (mTimer != null) {
 			mTimer.cancel();
 			mTimer = null;
 		}
 		stream.close();
 	}
 	
 	public int getChannelId() {
 		return this.epg.getChannelId();
 	}
 	
 }

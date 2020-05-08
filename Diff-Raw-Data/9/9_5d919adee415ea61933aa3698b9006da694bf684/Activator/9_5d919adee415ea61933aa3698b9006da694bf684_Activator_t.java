 package com.buglabs.bug.ircbot.behavior.mpd;
 
 import java.net.UnknownHostException;
 
import java.text.NumberFormat;
import java.text.DecimalFormat;
 
 import org.bff.javampd.MPD;
 import org.bff.javampd.objects.MPDSong;
 import org.bff.javampd.exception.MPDConnectionException;
 
 import org.osgi.service.log.LogService;
 
 import com.buglabs.bug.ircbot.pub.IChannelMessageConsumer;
 import com.buglabs.bug.ircbot.pub.IChannelMessageEvent;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 
 import com.buglabs.util.LogServiceUtil;
 
 /**
  * BundleActivator for Com_buglabs_bug_ircbot_behavior_fun.  The OSGi entry point to the application.
  *
  */
 public class Activator implements BundleActivator {
 	private MPD mpd;
 	private LogService logger = null;
 
 	public MPD getMPD() throws UnknownHostException, MPDConnectionException {
 		if (mpd.isConnected()) {
 			return mpd;
 		}
 		mpd = new MPD("bugmpd.local", 6600);
 		return mpd;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext context) throws Exception {
 		logger = LogServiceUtil.getLogService(context);
 		
 		
 		if (mpd != null) {
 			logger.log(LogService.LOG_DEBUG, "connected to mpd");
 			context.registerService(IChannelMessageConsumer.class.getName(), new IChannelMessageConsumer() {
 				public String onChannelMessage(IChannelMessageEvent e) {
 					logger.log(LogService.LOG_DEBUG, "got message:" + e.getMessage());
 					if (e.getMessage().startsWith(e.getBotName() + ": mpd ")) {
 						String command = e.getMessage().substring((e.getBotName() + ": mpd ").length());
 						
 						if (command.compareTo("song") == 0) {
 							try {
 								MPDSong song = mpd.getMPDPlayer().getCurrentSong();
 								return ("Title: " + song.getTitle() + ", Artist: " + song.getArtist() + ", File: " + song.getFile());
 							} catch (Exception ex) {
 								logger.log(LogService.LOG_DEBUG, "problem getting current song");
 								ex.printStackTrace();
 							}
 						}
 						
 						if (command.compareTo("track length") == 0) {
 							try {
								NumberFormat format = new DecimalFormat("#00");
 								MPDSong song = mpd.getMPDPlayer().getCurrentSong();
 								int hours = (song.getLength() / 3600);
 								int minutes = (song.getLength() / 60) % 60;
 								int seconds = song.getLength() % 60;
								return ("" + format.format(hours) + ":" + format.format(minutes) + ":" + format.format(seconds));
 							} catch (Exception ex) {
 								logger.log(LogService.LOG_DEBUG, "problem getting current song");
 								ex.printStackTrace();
 							}
 						}
 						
 						if (command.compareTo("play") == 0) {
 							try {
 								mpd.getMPDPlayer().play();
 								return "playing";
 							} catch (Exception ex) {
 								logger.log(LogService.LOG_DEBUG, "problem getting current song");
 								ex.printStackTrace();
 							}
 						}
 						
 						if (command.compareTo("pause") == 0) {
 							try {
 								mpd.getMPDPlayer().pause();
 								return "pausing";
 							} catch (Exception ex) {
 								logger.log(LogService.LOG_DEBUG, "problem getting current song");
 								ex.printStackTrace();
 							}
 						}
 						
 						if (command.compareTo("skip") == 0) {
 							try {
 								mpd.getMPDPlayer().playNext();
 								return "skipping";
 							} catch (Exception ex) {
 								logger.log(LogService.LOG_DEBUG, "problem skipping song");
 								ex.printStackTrace();
 							}
 						}
 						
 						if (command.compareTo("update") == 0) {
 							try {
 								new Thread() {
 									public void run() {
 										try {
 											mpd.getMPDAdmin().updateDatabase();
 										} catch (Exception e) {
 											logger.log(LogService.LOG_DEBUG, "error while updating");
 										}
 									}
 								}.start();
 								return "updating";
 							} catch (Exception ex) {
 								logger.log(LogService.LOG_DEBUG, "problem updating dabase");
 								ex.printStackTrace();
 							}
 						}
 					}
 					return null;
 				}
 			}, null);
 		} else {
 			logger.log(LogService.LOG_DEBUG, "problem getting mpd");
 		}
 	}
 
     /*
 	 * (non-Javadoc)
 	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {
 		mpd.close();
 	}
 	
 	
 }

 package no.runsafe.moosic;
 
 import no.runsafe.framework.server.RunsafeLocation;
 import no.runsafe.framework.timer.IScheduler;
 
 import java.io.File;
 import java.util.HashMap;
 
 public class MusicHandler
 {
	public MusicHandler(IScheduler scheduler, Plugin moosic)
 	{
 		this.scheduler = scheduler;
 		this.path = String.format("plugins/%s/songs/", moosic.getName());
 	}
 
 	public File loadSongFile(String fileName)
 	{
 		return new File(this.path + fileName);
 	}
 
 	public int startSong(MusicTrack musicTrack, RunsafeLocation location, float volume)
 	{
 		TrackPlayer trackPlayer = new TrackPlayer(location, musicTrack, volume);
 		this.currentTrackPlayerID++;
 
 		int timer = this.scheduler.startSyncRepeatingTask(new Runnable() {
 			@Override
 			public void run() {
 				progressPlayer(currentTrackPlayerID);
 			}
 		}, 0, (long) musicTrack.getTempo() / 20);
 
 		trackPlayer.setTimerID(timer);
 		this.trackPlayers.put(this.currentTrackPlayerID, trackPlayer);
 
 		return currentTrackPlayerID;
 	}
 
 	public boolean forceStop(int playerID)
 	{
 		if (this.trackPlayers.containsKey(playerID))
 		{
 			TrackPlayer trackPlayer = this.trackPlayers.get(playerID);
 			this.scheduler.cancelTask(trackPlayer.getTimerID());
 			this.trackPlayers.remove(playerID);
 
 			return true;
 		}
 		return false;
 	}
 
 	public void progressPlayer(int playerID)
 	{
 		TrackPlayer trackPlayer = this.trackPlayers.get(playerID);
 		if (!trackPlayer.playNextTick())
 		{
 			this.scheduler.cancelTask(trackPlayer.getTimerID());
 			this.trackPlayers.remove(playerID);
 		}
 	}
 
 	private IScheduler scheduler;
 	private HashMap<Integer, TrackPlayer> trackPlayers = new HashMap<Integer, TrackPlayer>();
 	private int currentTrackPlayerID = 0;
 	private String path;
 }

 package com.example.tvshowcrawler;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.List;
 
 import android.app.IntentService;
 import android.content.Intent;
 import android.net.Uri;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 
 import com.example.tvshowcrawler.TVShow.EnumTVShowStatus;
 
 public class UpdateShowService extends IntentService
 {
 	public UpdateShowService()
 	{
 		super("UpdateShowService");
 	}
 
 	public UpdateShowService(String name)
 	{
 		super(name);
 	}
 
 	public List<TVShow> getTvShows()
 	{
 		return tvShows;
 	}
 
 	public void setTvShows(List<TVShow> tvShows)
 	{
 		this.tvShows = tvShows;
 	}
 
 	private boolean isTransmissionServerOnline()
 	{
 		boolean ret = false;
 		// check remote transmission server
 		Log.i(TAG, "Checking Transmission server at: " + Settings.getInstance().getTransmissionServer());
 		try
 		{
 			URL url = new URL(Settings.getInstance().getTransmissionServer());
 			// check if server answers
 			String html = TVShow.readFromUrl(url);
 			if (html == null || html.isEmpty())
 			{
 				Log.i(TAG, "Transmission server offline.");
 				return ret;
 			}
 			else
 			{
 				ret = true;
 			}
 		} catch (MalformedURLException ex)
 		{
 			Log.e(TAG, "Error while checking Transmission server: " + ex.toString());
 			return ret;
 		}
 		return ret;
 	}
 
 	private void updateShow(TVShow show, boolean serverOnline)
 	{
 		show.setStatus(EnumTVShowStatus.Working);
 
 		show.update();
 		if (show.getStatus() == EnumTVShowStatus.NewEpisodeAvailable)
 		{
 			boolean success = false;
 			TorrentItem torrentItem = show.getDownloadItem();
 			String magnetLink = torrentItem.getMagnetLink();
 			if (magnetLink != null && serverOnline)
 			{
				Log.i(TAG,
						String.format("Fetching torrent for '%s S%02dE%02d'...", show.getName(),
								torrentItem.getSeason(),
								torrentItem.getEpisode()));
 				// start magnet link
 				Intent i = new Intent(Intent.ACTION_VIEW);
 				i.setData(Uri.parse(magnetLink));
 				i.addCategory(Intent.CATEGORY_BROWSABLE);
 				i.addCategory(Intent.CATEGORY_DEFAULT);
 				i.setType("application/x-bittorrent");
 				try
 				{
 					sendBroadcast(i);
 					success = true;
 				} catch (Exception e)
 				{
 					Log.e(TAG, e.toString());
 				}
 			}
 			if (success)
 			{
 				// return to unchecked state
 				show.setStatus(EnumTVShowStatus.NotChecked);
 				// update show season and episode
 				show.setSeason(torrentItem.getSeason());
 				show.setEpisode(torrentItem.getEpisode());
 			}
 			else
 			{
 				show.setStatus(EnumTVShowStatus.Error);
 			}
 		}
 	}
 
 	@Override
 	protected void onHandleIntent(Intent intent)
 	{
 		// get tvShows from intent
 		tvShows = intent.getParcelableArrayListExtra("com.example.tvshowcrawler.tvShows");
 
 		boolean serverOnline = true;
 		// not sure if no transmission mode makes sense?
 		if (Settings.getInstance().getEnableTransmission())
 		{
 			serverOnline = isTransmissionServerOnline();
 		}
 
 		// update all shows
 		for (TVShow show : tvShows)
 		{
 			updateShow(show, serverOnline);
 			Intent localIntent = new Intent(BROADCAST_TVSHOW_UPDATED_ACTION);
 			localIntent.putExtra("tvShow", show);
 			// Broadcasts the Intent to receivers in this app.
 			LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
 		}
 		Intent localIntent = new Intent(BROADCAST_DONE_ACTION);
 		// Broadcasts the Intent to receivers in this app.
 		LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
 	}
 
 	// Defines a custom Intent action
 	public static final String BROADCAST_TVSHOW_UPDATED_ACTION = "com.example.tvshowcrawler.BROADCAST_TVSHOW_UPDATED_ACTION";
 
 	public static final String BROADCAST_DONE_ACTION = "com.example.tvshowcrawler.BROADCAST_DONE_ACTION";
 
 	// Defines the key for the status "extra" in an Intent
 	public static final String EXTENDED_DATA_STATUS = "com.example.tvshowcrawler.STATUS";
 
 	private static final String TAG = "UpdateShowService";
 
 	private List<TVShow> tvShows;
 
 }

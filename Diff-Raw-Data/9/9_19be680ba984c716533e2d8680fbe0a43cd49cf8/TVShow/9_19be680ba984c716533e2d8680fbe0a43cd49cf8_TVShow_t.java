 package com.example.tvshowcrawler;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Locale;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.util.ByteArrayBuffer;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.annotation.SuppressLint;
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.util.Log;
 
 @SuppressLint("DefaultLocale")
 public class TVShow implements JSONable, Parcelable
 {
 	public enum EnumTVShowStatus
 	{
 		NotChecked(1),
 		UpToDate(2),
 		NewEpisodeAvailable(3),
 		Working(4),
 		TorrentNotFound(5), // i.e. there should be a new episode (according to TVRage), but we can't find one
 		Error(6);
 
 		private EnumTVShowStatus(int value)
 		{
 			this.value = value;
 		}
 
 		public int getValue()
 		{
 			return value;
 		}
 
 		private int value;
 
 		public static EnumTVShowStatus valueOf(int value)
 		{
 			EnumTVShowStatus[] enums = EnumTVShowStatus.values();
 			for (EnumTVShowStatus valueEnum : enums)
 			{
 				if (valueEnum.getValue() == value)
 				{
 					return valueEnum;
 				}
 			}
 			return NotChecked;
 		}
 	}
 
 	public TVShow()
 	{
 		super();
 		excludedKeyWords = new ArrayList<String>();
 	};
 
 	public TVShow(String name, int season, int episode)
 	{
 		super();
 		this.name = name;
 		this.season = season;
 		this.episode = episode;
 		excludedKeyWords = new ArrayList<String>();
 	}
 
 	// constructor that takes a Parcel and gives you an object populated with it's values
 	private TVShow(Parcel in)
 	{
 		this();
 		try
 		{
 			fromJSONObject(new JSONObject(in.readString()));
 		} catch (JSONException e)
 		{
 			Log.e(TAG, e.toString());
 		}
 	}
 
 	@Override
 	public int describeContents()
 	{
 		return 0;
 	}
 
 	public boolean equals(Object obj)
 	{
 		if (obj == null)
 			return false;
 		if (obj == this)
 			return true;
 		if (obj.getClass() != getClass())
 			return false;
 
 		TVShow rhs = (TVShow) obj;
 		return name.equals(rhs.getName())
 				&& season == rhs.getSeason()
 				&& episode == rhs.getEpisode()
 				&& equal(excludedKeyWords, rhs.getExcludedKeyWords())
 				&& equal(magnetLink, rhs.getMagnetLink())
 				&& equal(lastEpisode, rhs.getLastEpisode())
 				&& equal(nextEpisode, rhs.getNextEpisode())
 				&& status.equals(rhs.getStatus())
 				&& equal(showStatus, rhs.getShowStatus());
 	}
 
 	@Override
 	public void fromJSONObject(JSONObject src) throws JSONException
 	{
 		name = src.getString("name");
 		season = src.getInt("season");
 		episode = src.getInt("episode");
 		active = src.getBoolean("active");
 		if (src.has("status"))
 			status = EnumTVShowStatus.valueOf(src.getInt("status"));
 		if (src.has("showStatus"))
 			showStatus = src.getString("showStatus");
 		if (src.has("magnetLink"))
 			magnetLink = src.getString("magnetLink");
 		excludedKeyWords = new ArrayList<String>();
 		if (src.has("excludedKeyWords"))
 		{
 			JSONArray jsonArray = (JSONArray) src.getJSONArray("excludedKeyWords");
 			for (int i = 0; i < jsonArray.length(); i++)
 			{
 				excludedKeyWords.add(jsonArray.get(i).toString());
 			}
 		}
 
 		if (src.has("lastEpisode"))
 		{
 			lastEpisode = new EpisodeInfo();
 			lastEpisode.fromJSONObject(src.getJSONObject("lastEpisode"));
 		}
 		if (src.has("nextEpisode"))
 		{
 			nextEpisode = new EpisodeInfo();
 			nextEpisode.fromJSONObject(src.getJSONObject("nextEpisode"));
 		}
 	}
 
 	public Boolean getActive()
 	{
 		return active;
 	}
 
 	public TorrentItem getDownloadItem()
 	{
 		TorrentItem ret = null;
 		if (torrentItems == null || torrentItems.size() == 0)
 		{
 			Log.e(TAG, "Error: TorrentItemList is empty!");
 			return ret;
 		}
 
 		// find best matching TorrentItem
 		ArrayList<TorrentItem> primaryItems = new ArrayList<TorrentItem>();
 		ArrayList<TorrentItem> secondaryItems = new ArrayList<TorrentItem>();
 		for (TorrentItem torrentItem : torrentItems)
 		{
 			if (torrentItem.getMagnetLink() == null || torrentItem.getMagnetLink().isEmpty())
 				continue;
 
 			// store secondary choices in case no primary choice is found
 			// i.e.: if we want 720p, store non 720p and vice versa
 			if (Settings.getInstance().getPrefer720p() == torrentItem.isIs720p()
 					&& Settings.getInstance().getX264() == torrentItem.isIsx264())
 				primaryItems.add(torrentItem);
 			else
 				secondaryItems.add(torrentItem);
 		}
 
 		if (primaryItems.size() > 0)
 		{
 			ret = primaryItems.get(0);
 		}
 		else if (secondaryItems.size() > 0)
 		{
 			ret = secondaryItems.get(0);
 		}
 
 		return ret;
 	}
 
 	public int getEpisode()
 	{
 		return episode;
 	}
 
 	public ArrayList<String> getExcludedKeyWords()
 	{
 		return excludedKeyWords;
 	}
 
 	public EpisodeInfo getLastEpisode()
 	{
 		return lastEpisode;
 	}
 
 	public String getMagnetLink()
 	{
 		return magnetLink;
 	}
 
 	public String getName()
 	{
 		return name;
 	}
 
 	public EpisodeInfo getNextEpisode()
 	{
 		return nextEpisode;
 	}
 
 	public int getSeason()
 	{
 		return season;
 	}
 
 	public String getShowStatus()
 	{
 		return showStatus;
 	}
 
 	public EnumTVShowStatus getStatus()
 	{
 		return status;
 	}
 
 	public ArrayList<TorrentItem> getTorrentItems()
 	{
 		return torrentItems;
 	}
 
 	// retrieve TorrentItems from all supported torrent sites
 	public void queryAllSites(int season, int episode)
 	{
 		if (torrentItems == null)
 		{
 			torrentItems = new ArrayList<TorrentItem>();
 		}
 		else
 		{
 			torrentItems.clear();
 		}
 		queryPirateBay(season, episode);
 //		if (getTorrentItems().size() < 5)
 //			queryKickAssTorrents(season, episode);
 		if (getTorrentItems().size() > 0)
 		{
 			setStatus(EnumTVShowStatus.NewEpisodeAvailable);
 			// update magnet link
 			assert (getDownloadItem() != null);
 			assert (getDownloadItem().getMagnetLink() != null);
 			magnetLink = getDownloadItem().getMagnetLink();
 		}
 		else
 		{
 			Log.i(TAG, "Did not find any torrents.");
			setStatus(EnumTVShowStatus.UpToDate);
 			magnetLink = null;
 		}
 	}
 
 	// / retrieve TorrentItems from kickasstorrents (kat.ph)
 	public void queryKickAssTorrents(int season, int episode)
 	{
 		Log.i(TAG, String.format("Querying kickasstorrents for '%s S%02dE%02d'...", name, season, episode));
 
 		if (torrentItems == null)
 		{
 			torrentItems = new ArrayList<TorrentItem>();
 		}
 
 		// build kat.ph url string a la 'http:/kat.ph/usearch/New%20Girl%20S01E17/'
 		StringBuilder sb = new StringBuilder();
 		sb.append("http://kat.ph/usearch/");
 		sb.append(String.format("%s S%02dE%02d/", name, season, episode).replace(" ", "%20"));
 		String html;
 		try
 		{
 			html = readFromUrl(new URL(sb.toString()));
 		} catch (MalformedURLException e)
 		{
 			Log.e(TAG, e.toString());
 			setStatus(EnumTVShowStatus.Error);
 			return;
 		}
 
 		if (html == null)
 		{
 			Log.e(TAG, "html is null");
 			setStatus(EnumTVShowStatus.Error);
 			return;
 		}
 
 		// all results are stored in <tr class="even" OR "odd" id="torrent_... where id="torrent_30_rock_s05e08
 		String searchString = String
 				.format("<tr class=\"(even|odd)\" id=\"torrent_%s_s%02de%02d", name.replace(" ", "_").toLowerCase(),
 						season, episode);
 
 		Pattern p = Pattern.compile(searchString);
 		Matcher m = p.matcher(html);
 		while (m.find())
 		{
 			int startIndex = m.end();
 			TorrentItem item = new TorrentItem();
 			item.setSeason(season);
 			item.setEpisode(episode);
 
 			// <strong class="red">30 Rock S05E08</strong> HDTV XviD-LOL [eztv]</a>
 			// extract name
 			String nameSearchString = "<strong class=\"red\">";
 			int nameStart = html.indexOf(nameSearchString, startIndex);
 			int nameEnd = html.indexOf("</a>", nameStart);
 			String itemName = html.substring(nameStart + nameSearchString.length(), nameEnd);
 			itemName = itemName.replaceAll("</strong>", "");
 			itemName = itemName.replaceAll("<strong class=\"red\">", "");
 			item.setName(itemName);
 
 			// check for 720p/x264
 			item.setIs720p(itemName.contains("720p"));
 			item.setIsx264(itemName.contains("x264"));
 
 			// extract magnet link
 			int magnetStart = html.indexOf("magnet:?xt=urn:", startIndex);
 			int magnetEnd = html.indexOf("\"", magnetStart + 1);
 			if (magnetStart > 0 && magnetStart < magnetEnd)
 				item.setMagnetLink(html.substring(magnetStart, magnetEnd));
 
 			// check seeds
 			String slSearchString = "<td class=\"green center\">";
 			int seedStart = html.indexOf(slSearchString, startIndex);
 			int seedEnd = html.indexOf("</td>", seedStart + 1);
 			int seeds = Integer.parseInt(html.substring(seedStart + slSearchString.length(), seedEnd));
 
 			if (seeds > 0 && !nameContainsExcludedKeyWords(itemName))
 			{
 				Log.i(TAG, String.format("Got %s (%d seeds).", itemName, seeds));
 				getTorrentItems().add(item);
 			}
 		}
 	}
 
 	// retrieve TorrentItems from pirate bay (thepiratebay.se)
 	public void queryPirateBay(int season, int episode)
 	{
 		Log.i(TAG, String.format("Querying thepiratebay.se for '%s S%02dE%02d'...", name, season, episode));
 
 		if (torrentItems == null)
 		{
 			torrentItems = new ArrayList<TorrentItem>();
 		}
 
 		// query pirateBay for torrents, order by seeds
 		StringBuilder sb = new StringBuilder();
 		sb.append("http://thepiratebay.se/search/");
 		sb.append(String.format("%s S%02dE%02d/0/7/0", name, season, episode).replace(" ", "%20"));
 		String html;
 		try
 		{
 			html = readFromUrl(new URL(sb.toString()));
 		} catch (MalformedURLException e)
 		{
 			Log.e(TAG, e.toString());
 			setStatus(EnumTVShowStatus.Error);
 			return;
 		}
 
 		if (html == null)
 		{
 			Log.e(TAG, "html is null");
 			setStatus(EnumTVShowStatus.Error);
 			return;
 		}
 
 		// all results are stored in <div class="detName"> followed directly by magnet link
 		String searchString = "<div class=\"detName\">";
 
 		Pattern p = Pattern.compile(searchString);
 		Matcher m = p.matcher(html);
 		while (m.find())
 		{
 			int startIndex = m.end();
 			TorrentItem item = new TorrentItem();
 			item.setSeason(season);
 			item.setEpisode(episode);
 
 			// extract name
 			int nameStart = html.indexOf(">", startIndex);
 			int nameEnd = html.indexOf("</a>", nameStart);
 			String itemName = html.substring(nameStart + 1, nameEnd);
 			item.setName(itemName);
 
 			// check for 720p
 			item.setIs720p(itemName.contains("720p"));
 			item.setIsx264(itemName.contains("x264"));
 			// extract magnet link
 			int magnetStart = html.indexOf("magnet:?xt=urn:", startIndex);
 			int magnetEnd = html.indexOf("\"", magnetStart + 1);
 			if (magnetStart > 0 && magnetStart < magnetEnd)
 				item.setMagnetLink(html.substring(magnetStart, magnetEnd));
 			// check seeds
 			String slSearchString = "<td align=\"right\">";
 			int seedStart = html.indexOf(slSearchString, startIndex);
 			int seedEnd = html.indexOf("</td>", seedStart + 1);
 			int seeds = Integer.parseInt(html.substring(seedStart + slSearchString.length(), seedEnd));
 
 			if (seeds > 0 && !nameContainsExcludedKeyWords(itemName))
 			{
 				Log.i(TAG, String.format("Got '%s (%d seeds).", itemName, seeds));
 				torrentItems.add(item);
 			}
 		}
 	}
 
 	public String searchStringForNextEpisode()
 	{
 		return String.format(Locale.US, "%s S%02d E%02d", getName(), getSeason(), getEpisode() + 1).replace(" ", "+");
 	}
 
 	public void setActive(Boolean active)
 	{
 		this.active = active;
 	}
 
 	public void setEpisode(int episode)
 	{
 		this.episode = episode;
 	}
 
 	public void setExcludedKeyWords(ArrayList<String> excludedKeyWords)
 	{
 		this.excludedKeyWords = excludedKeyWords;
 	}
 
 	public void setLastEpisode(EpisodeInfo lastEpisode)
 	{
 		this.lastEpisode = lastEpisode;
 	}
 
 	public void setMagnetLink(String magnetLink)
 	{
 		this.magnetLink = magnetLink;
 	}
 
 	public void setName(String name)
 	{
 		this.name = name;
 	}
 
 	public void setNextEpisode(EpisodeInfo nextEpisode)
 	{
 		this.nextEpisode = nextEpisode;
 	}
 
 	public void setSeason(int season)
 	{
 		this.season = season;
 	}
 
 	public void setShowStatus(String showStatus)
 	{
 		this.showStatus = showStatus;
 	}
 
 	public void setStatus(EnumTVShowStatus status)
 	{
 		this.status = status;
 	}
 
 	@Override
 	public JSONObject toJSONObject() throws JSONException
 	{
 		JSONObject jo = new JSONObject();
 
 		jo.put("status", status.getValue());
 		jo.put("name", name);
 		jo.put("season", season);
 		jo.put("episode", episode);
 		jo.put("active", active);
 		if (showStatus != null)
 			jo.put("showStatus", showStatus);
 		if (magnetLink != null)
 			jo.put("magnetLink", magnetLink);
 		if (excludedKeyWords != null)
 		{
 			jo.put("excludedKeyWords", new JSONArray(excludedKeyWords));
 		}
 		if (lastEpisode != null)
 		{
 			jo.put("lastEpisode", lastEpisode.toJSONObject());
 		}
 		if (nextEpisode != null)
 		{
 			jo.put("nextEpisode", nextEpisode.toJSONObject());
 		}
 		return jo;
 	}
 
 	// check for new episodes
 	public void update()
 	{
 		setStatus(EnumTVShowStatus.Working);
 		// create list to store torrent items
 		if (torrentItems == null)
 		{
 			torrentItems = new ArrayList<TorrentItem>();
 		}
 		else
 		{
 			torrentItems.clear();
 		}
 		updateTVRageInfo();
 
 		int checkSeason = season;
 		int checkEpisode = episode;
 
 		// get episode + 1
 		if (Settings.getInstance().getCatchUp())
 		{
 			checkSeason = season;
 			checkEpisode = episode + 1;
 		}
 		else if (lastEpisode != null
 				&& (lastEpisode.getSeason() > season
 				|| (lastEpisode.getSeason() == season && lastEpisode.getEpisode() > episode)))
 		{
 			// get last episode as listed by TVRage
 			checkSeason = lastEpisode.getSeason();
 			checkEpisode = lastEpisode.getEpisode();
 		}
 		// we have last episode, check if next episode is already available
 		else if (nextEpisode != null
 				&& (nextEpisode.getSeason() > season || nextEpisode.getEpisode() > episode)
 				&& nextEpisode.getAirTime().before(Calendar.getInstance()))
 		{
 			checkSeason = nextEpisode.getSeason();
 			checkEpisode = nextEpisode.getEpisode();
 		}
 		else
 		{
 			// nothing to do, update status and exit
 			setStatus(EnumTVShowStatus.UpToDate);
 			return;
 		}
 
 		queryAllSites(checkSeason, checkEpisode);
 
 	}
 
 	// retrieve show metadata and episode info from TVRage
 	public void updateTVRageInfo()
 	{
 		Log.i(TAG, "Retrieving TVRage show info...");
 		// http://services.tvrage.com/tools/quickinfo.php?show=30_Rock&exact=1
 		String url = String.format("http://services.tvrage.com/tools/quickinfo.php?show=%s&exact=1",
 				name.replace(" ", "%20"));
 		String rawText;
 		try
 		{
 			rawText = readFromUrl(new URL(url));
 		} catch (MalformedURLException e)
 		{
 			Log.e(TAG, e.toString());
 			rawText = null;
 		}
 		if (rawText == null)
 		{
 			Log.i(TAG, "Retrieving TVRage show info failed.");
 			return;
 		}
 
 		// <pre>Show ID@28304
 		// Show Name@New Girl
 		// Show URL@http://www.tvrage.com/The_New_Girl
 		// Premiered@2011
 		// Started@Sep/20/2011
 		// Ended@
 		// Latest Episode@01x19^Secrets^Apr/03/2012
 		// Next Episode@01x20^Normal^Apr/10/2012
 		// RFC3339@2012-04-10T21:00:00-4:00
 		// GMT+0 NODST@1334098800
 		// Country@USA
 		// Status@New Series
 		// Classification@Scripted
 		// Genres@Comedy
 		// Network@FOX
 		// Airtime@Tuesday at 09:00 pm
 		// Runtime@30
 
 		String[] lines = rawText.split("\n");
 
 		for (String line : lines)
 		{
 			if (line.startsWith("Latest Episode"))
 			{
 				String temp = line;
 				temp = temp.substring(temp.indexOf("@") + 1);
 				EpisodeInfo ei = EpisodeInfo.fromString(temp);
 				if (ei != null)
 					setLastEpisode(ei);
 			}
 			else if (line.startsWith("Next Episode"))
 			{
 				String temp = line;
 				temp = temp.substring(temp.indexOf("@") + 1);
 				EpisodeInfo ei = EpisodeInfo.fromString(temp);
 				if (ei != null)
 					setNextEpisode(ei);
 			}
 			else if (line.startsWith("Status"))
 			{
 				String temp = line;
 				temp = line.substring(temp.indexOf("@") + 1);
 				if (showStatus != temp)
 				{
 					setShowStatus(temp);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void writeToParcel(Parcel dest, int flags)
 	{
 		try
 		{
 			dest.writeString(toJSONObject().toString());
 		} catch (JSONException e)
 		{
 			// TODO Auto-generated catch block
 			Log.e(TAG, e.toString());
 		}
 	}
 
 	// / check if name contains keywords marked for exclusion
 	private boolean nameContainsExcludedKeyWords(String name)
 	{
 		if (excludedKeyWords != null)
 		{
 			for (String key : excludedKeyWords)
 			{
 				if (name.toLowerCase().contains(key.toLowerCase()))
 					return true;
 			}
 		}
 		return false;
 	}
 
 	private static final String TAG = "TVShow";
 
 	// name of the show e.g. 'New Girl'
 	private String name;
 
 	// current season number (of last successfully fetched episode)
 	private int season;
 
 	// current episode number (of last successfully fetched episode)
 	private int episode;
 
 	// used to track whether the show is active. i.e. not at the end of season or end of show may be used to remove show
 	// from active list
 	private Boolean active = false;
 
 	// status of the show, see eStatus
 	private EnumTVShowStatus status = EnumTVShowStatus.NotChecked;
 
 	// EpisodeInfo for last broadcasted episode according to TVRage
 	private EpisodeInfo lastEpisode;
 
 	// EpisodeInfo for next episode to be aired according to TVRage
 	private EpisodeInfo nextEpisode;
 
 	// status of the show according to TVRage, e.g.: Returning Series, Final Season, Canceled, etc.
 	private String showStatus;
 
 	// holds list of torrent addresses, etc. see TorrentItem
 	private ArrayList<TorrentItem> torrentItems;
 
 	// holds keywords that should not occur in valid torrent items
 	private ArrayList<String> excludedKeyWords;
 
 	// holds magnet link to current download item (if any)
 	private String magnetLink;
 
 	// this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
 	public static final Parcelable.Creator<TVShow> CREATOR = new Parcelable.Creator<TVShow>()
 	{
 		public TVShow createFromParcel(Parcel in)
 		{
 			return new TVShow(in);
 		}
 
 		public TVShow[] newArray(int size)
 		{
 			return new TVShow[size];
 		}
 	};
 
 	// this should be part of Java...
 	public static boolean equal(Object object1, Object object2)
 	{
 		if (object1 == object2)
 		{
 			return true;
 		}
 		if ((object1 == null) || (object2 == null))
 		{
 			return false;
 		}
 		return object1.equals(object2);
 	}
 
 	// open uri and return answer as String (html)
 	public static String readFromUrl(URL url)
 	{
 		String ret = null;
 		try
 		{
 			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
 			HttpURLConnection.setFollowRedirects(true);
 			huc.setConnectTimeout(5 * 1000);
 			huc.setRequestMethod("GET");
 			huc.setRequestProperty("User-Agent",
 					"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:19.0) Gecko/20100101 Firefox/19.0");
 			huc.connect();
 			InputStream is = huc.getInputStream();
 
 			BufferedInputStream bis = new BufferedInputStream(is);
 			ByteArrayBuffer baf = new ByteArrayBuffer(50);
 			int current = 0;
 			while ((current = bis.read()) != -1)
 			{
 				baf.append((byte) current);
 			}
 			ret = new String(baf.toByteArray());
 		} catch (IOException e)
 		{
 			Log.e(TAG, e.toString());
 			ret = null;
 		}
 		return ret;
 	}
 }

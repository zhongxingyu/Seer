 /*
  * Copyright (c) 2013 Joakim Lindskog
  * 
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
  * 
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
  * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 package com.limewoodMedia.nsapi;
 
 import com.limewoodMedia.nsapi.enums.CauseOfDeath;
 import com.limewoodMedia.nsapi.enums.IArguments;
 import com.limewoodMedia.nsapi.enums.IShards;
 import com.limewoodMedia.nsapi.enums.WACouncil;
 import com.limewoodMedia.nsapi.enums.WAStatus;
 import com.limewoodMedia.nsapi.enums.WAVote;
 import com.limewoodMedia.nsapi.exceptions.RateLimitReachedException;
 import com.limewoodMedia.nsapi.exceptions.UnknownNationException;
 import com.limewoodMedia.nsapi.exceptions.UnknownRegionException;
 import com.limewoodMedia.nsapi.holders.Budget;
 import com.limewoodMedia.nsapi.holders.Embassy;
 import com.limewoodMedia.nsapi.holders.HappeningData;
 import com.limewoodMedia.nsapi.holders.HappeningData.EventHappening;
 import com.limewoodMedia.nsapi.holders.HappeningData.ViewType;
 import com.limewoodMedia.nsapi.holders.NSData;
 import com.limewoodMedia.nsapi.holders.NationData;
 import com.limewoodMedia.nsapi.holders.NationFreedoms;
 import com.limewoodMedia.nsapi.holders.NationHappening;
 import com.limewoodMedia.nsapi.holders.RMBMessage;
 import com.limewoodMedia.nsapi.holders.RegionData;
 import com.limewoodMedia.nsapi.holders.RegionHappening;
 import com.limewoodMedia.nsapi.holders.WAData;
 import com.limewoodMedia.nsapi.holders.WAHappening;
 import com.limewoodMedia.nsapi.holders.WAMemberLogHappening;
 import com.limewoodMedia.nsapi.holders.WAVotes;
 import com.limewoodMedia.nsapi.holders.WorldData;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.params.ClientPNames;
 import org.apache.http.conn.params.ConnRoutePNames;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.CoreProtocolPNames;
 import org.kxml2.io.KXmlParser;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 /**
  * Java API for the NationStates Shards API
  * @author Joakim Lindskog
  *
  */
 public class NationStates {
 	public static final String API = "http://www.nationstates.net/cgi-bin/api.cgi";
 	public static final String API_USER_AGENT = "Java NSAPI library by Laevendell (code.google.com/p/ns-api/); ";
 	public static final int DEFAULT_RATE_LIMIT = 49; // One lower to be on the safe side
 
 	public NationStates() {
 		
 	}
 
 	private final LinkedList<Long> calls = new LinkedList<Long>();
 	private int rateLimit = DEFAULT_RATE_LIMIT;
 	private boolean useRateLimit = true;
 	private String userAgent = null;
 	private int version = -1;
 	private boolean verbose = false;
 	private boolean relaxed = false;
 	private long hardRateLimit = 0;
 	private String proxyIP;
 	private int proxyPort;
 
 	/**
 	 * Sets the rate limit - default is 49 (per 30 seconds)
 	 * CAUTION: make sure your application doesn't exceed the rate limit
 	 * @param rateLimit max number of calls per 30 seconds
 	 */
 	public synchronized void setRateLimit(int rateLimit) {
 		this.rateLimit = rateLimit;
 	}
 
 	/**
 	 * @return the current rate limit (max number of calls per 30 seconds)
 	 */
 	public synchronized int getRateLimit() {
 		return rateLimit;
 	}
 
 	/**
 	 * Enables/disables the rate limit (default is enabled)
 	 * CAUTION: only disable the rate limit if you handle it properly elsewhere
 	 * @param enabled true to enable the rate limit, false to disable it
 	 */
 	public synchronized void setRateLimitEnabled(boolean enabled) {
 		useRateLimit = enabled;
 	}
 
 	/**
 	 * @return whether the rate limit is enabled
 	 */
 	public synchronized boolean isRateLimitEnabled() {
 		return useRateLimit;
 	}
 
 	/**
 	 * Verbose mode does extensive debug logging
 	 * @return verbose
 	 */
 	public synchronized boolean isVerbose() {
 		return verbose;
 	}
 
 	/**
 	 * Sets the verbose mode
 	 * @param verbose
 	 */
 	public synchronized void setVerbose(boolean verbose) {
 		this.verbose = verbose;
 	}
 
 	/**
 	 * Sets the User-Agent header
 	 * Note: this needs to be set to be able to access the API
 	 * @param userAgent the User-Agent string to use
 	 */
 	public synchronized void setUserAgent(String userAgent) {
 		this.userAgent = API_USER_AGENT + userAgent;
 	}
 
 	/**
 	 * @return the User-Agent
 	 */
 	public synchronized String getUserAgent() {
 		return userAgent;
 	}
 
 	/**
 	 * Sets version of the NationStates API to use
 	 * @param version the version of the NS API to use
 	 */
 	public synchronized void setVersion(int version) {
 		this.version = version;
 	}
 	
 	/**
 	 * @return the currently used version
 	 */
 	public synchronized int getVersion() {
 		return version;
 	}
 	
 	public synchronized String getProxyIP() {
 		return proxyIP;
 	}
 	
 	public synchronized void setProxyIP(String ip) {
 		proxyIP = ip;
 	}
 	
 	public synchronized int getProxyPort() {
 		return proxyPort;
 	}
 	
 	public synchronized void setProxyPort(int port) {
 		this.proxyPort = port;
 	}
 
 	/**
 	 * In relaxed mode the XML parsing attempts to correct any invalid xml characters it encounters
 	 * 
 	 * @return relaxed mode
 	 */
 	public synchronized boolean isRelaxed() {
 		return relaxed;
 	}
 
 	/**
 	 * Sets whether XML parsing should be strict (relaxed == false) or relaxed.
 	 * 
 	 * @param relax
 	 */
 	public synchronized void setRelaxed(boolean relax) {
 		this.relaxed = relax;
 	}
 
 	/**
 	 * Makes sure you do not exceed the NS API rate limit
 	 * 
 	 * @return true if it's OK to make a call to the NS API
 	 * @throws IllegalArgumentException if no User-Agent was set
 	 */
 	protected synchronized boolean makeCall() throws IllegalArgumentException {
 		if (this.userAgent == null) {
 			throw new IllegalArgumentException("No User-Agent set! Use setUserAgent(String).");
 		}
 		if (hardRateLimit > System.currentTimeMillis()) {
 			return false;
 		}
 		Iterator<Long> i = this.calls.iterator();
 		while(i.hasNext()) {
 			if (i.next() + 30000L < System.currentTimeMillis()) {
 				i.remove();
 			}
 		}
 		if (this.calls.size() < rateLimit) {
 			this.calls.add(System.currentTimeMillis());
 			return true;
 		}
 		return false;
 	}
 
 	public synchronized int getRateLimitRemaining() {
 		if (hardRateLimit > System.currentTimeMillis()) {
 			return 0;
 		}
 		Iterator<Long> i = this.calls.iterator();
 		while(i.hasNext()) {
 			if (i.next() + 30000L < System.currentTimeMillis()) {
 				i.remove();
 			}
 		}
 		return rateLimit - this.calls.size();
 	}
 
 	/**
 	 * Verifies the nation checksum with nationstates authentication
 	 * 
 	 * @param nation name to verify
 	 * @param checksum of the nation
 	 * @return true if verified
 	 */
 	public boolean verifyNation(String nation, String checksum) {
 		return verifyNation(nation, checksum, null);
 	}
 
 	/**
 	 * Verifies the nation checksum with nationstates authentication
 	 * 
 	 * @param nation name to verify
 	 * @param checksum of the nation
 	 * @param site-specific token (optional, null if none)
 	 * @return true if verified
 	 */
 	public boolean verifyNation(String nation, String checksum, String token) {
 		try {
 			if (!makeCall()) {
 				throw new RateLimitReachedException();
 			}
 			InputStream stream = doRequest(API + "?a=verify&nation=" + nation.toLowerCase().replaceAll(" ", "_") + "&checksum=" + checksum + (token != null ? "&token=" + token : ""));
 			return "1".equals(convertStreamToString(stream).trim());
 		} catch (IOException e) {
 			throw new RuntimeException("IOException parsing authentication result", e);
 		}
 	}
 
 	private static String convertStreamToString(java.io.InputStream is) {
 	    java.util.Scanner s = new java.util.Scanner(is);
 	    s.useDelimiter("\\A");
 	    try {
 	    	return s.hasNext() ? s.next() : "";
 	    } finally {
 	    	s.close();
 	    }
 	}
 
 	/**
 	 * Sets a telegram with the NationStates telegram API
 	 * @param clientKey to use
 	 * @param secretKey of the telegram
 	 * @param tgid of the telegram to send
 	 * @param nation to send the telegram to
 	 * 
 	 * @return telegram api result
 	 */
 	public String sendTelegram(String clientKey, String secretKey, String tgid, String nation) {
 		if (!makeCall()) {
 			throw new RateLimitReachedException();
 		}
 		try {
 			return convertStreamToString(doRequest("http://www.nationstates.net/cgi-bin/api.cgi?a=sendTG&client=" + clientKey + "&tgid=" + tgid + "&key=" + secretKey + "&to=" + nation));
 		}  catch (IOException e) {
 			throw new RuntimeException("IOException sending telegram", e);
 		}
 	}
 	/**
 	 * Gets the happening information from the world
 	 * 
 	 * @param filters to filter the happening information gathered
 	 * @return happening data
 	 */
 	public HappeningData getHappeningInfo(HappeningData.Filter ...filters) {
 		return getHappeningInfo(null, -1, -1, filters);
 	}
 
 	/**
 	 * Gets the happening information from the world
 	 * 
 	 * @param view confine results to a specific nation or region of nations to gather happenings on (optional, may be null)
 	 * @param filters to filter the happening information gathered
 	 * @return happening data
 	 */
 	public HappeningData getHappeningInfo(ViewType view, HappeningData.Filter ...filters) {
 		return getHappeningInfo(view, -1, -1, filters);
 	}
 
 	/**
 	 * Gets the happening information from the world
 	 * 
 	 * @param view confine results to a specific nation or region of nations to gather happenings on (optional, may be null)
 	 * @param sinceId restrict results to happenings since the event id, or -1 for no restriction
 	 * @param filters to filter the happening information gathered
 	 * @return happening data
 	 */
 	public HappeningData getHappeningInfo(ViewType view, int sinceId, HappeningData.Filter ...filters) {
 		return getHappeningInfo(view, -1, sinceId, filters);
 	}
 
 	/**
 	 * Gets the happening information from the world
 	 * 
 	 * @param view confine results to a specific nation or region of nations to gather happenings on (optional, may be null)
 	 * @param limit the number of happening results to be returned, or -1 for no limit
 	 * @param sinceId restrict results to happenings since the event id, or -1 for no restriction
 	 * @param filters to filter the happening information gathered
 	 * @return happening data
 	 */
 	public HappeningData getHappeningInfo(ViewType view, int limit, int sinceId, HappeningData.Filter ...filters) {
 		if (!makeCall()) {
 			throw new RateLimitReachedException();
 		}
 		NSData data = null;
 		try {
 			String viewFragment = (view != null ? view.toString() + ";" : "");
 			String limitFragment = (limit != -1 ? "limit=" + limit + ";" : "");
 			String sinceFragment = (sinceId != -1 ? "sinceid=" + sinceId + ";" : "");
 			data = getInfo(doRequest(API + "?q=happenings;" + viewFragment + limitFragment + sinceFragment + buildShardString(filters)));
 			XmlPullParser xpp = null;
 			xpp = data.xpp;
 			xpp.setFeature("http://xmlpull.org/v1/doc/features.html#relaxed", relaxed);
 			xpp.defineEntityReplacementText("<a href='", "&lt;a href=&quot;");
 			xpp.defineEntityReplacementText("'>", "&quot;&gt;");
 			String tagName = null;
 			HappeningData events = new HappeningData();
 			while (xpp.next() != XmlPullParser.END_DOCUMENT) {
 				switch (xpp.getEventType()) {
 				case XmlPullParser.START_TAG:
 					tagName = xpp.getName().toLowerCase();
 					if (verbose) {
 						System.out.println("Parsing happenings Tag: " + tagName);
 					}
 					if (tagName.equals("happenings")) {
 						events.happenings = parseWorldHappenings(xpp).toArray(new EventHappening[0]);
 					}
 				}
 			}
 			return events;
 		} catch (XmlPullParserException e) {
 			throw new RuntimeException("Failed to parse XML", e);
 		} catch (IOException e) {
 			throw new RuntimeException("IOException parsing XML", e);
 		}
 		finally {
 			closeQuietly(data);
 		}
 	}
 
 	private List<EventHappening> parseWorldHappenings(XmlPullParser xpp)	throws NumberFormatException, XmlPullParserException, IOException {
 		String tagName = null;
 		long ts = -1L;
 		String text = null;
 		int eventId = -1;
 		ArrayList<EventHappening> happenings = new ArrayList<EventHappening>();
 		loop: while (xpp.next() != XmlPullParser.END_DOCUMENT) {
 			switch (xpp.getEventType()) {
 			case XmlPullParser.START_TAG:
 				tagName = xpp.getName().toLowerCase();
 				if (tagName.equals("event")) {
 					//Get the event id
 					eventId = Integer.parseInt(xpp.getAttributeValue(0));
 					// Get timestamp
 					xpp.nextTag();
 					ts = Long.parseLong(xpp.nextText());
 					// Get text
 					xpp.nextTag();
 					text = xpp.nextText();
 
 					happenings.add(new EventHappening(ts, text, eventId));
 				}
 				break;
 			case XmlPullParser.END_TAG:
 				tagName = xpp.getName().toLowerCase();
 				if (tagName.equals("HAPPENINGS")) {
 					break loop;
 				}
 			}
 		}
 		return happenings;
 	}
 
 	private static void closeQuietly(NSData data) {
 		if (data != null) {
 			try { data.stream.close(); }
 			catch (Exception ignore) { }
 		}
 	}
 
 	/**
 	* Fetches information on the world
 	* @param arguments the shards to request
 	* @return a WorldData object with world info
 	* @throws RateLimitReachedException if the rate limit was reached (but not exceeded)
 	*/
 	public WorldData getWorldInfo(WorldData.Shards...shards) {
 		if (!makeCall()) {
 			throw new RateLimitReachedException();
 		}
 		NSData data = null;
 		try {
 			data = getInfo("?", shards);
 			if (verbose) {
 				System.out.println("Parsing World Info");
 			}
 			XmlPullParser xpp = null;
 			xpp = data.xpp;
 			xpp.setFeature("http://xmlpull.org/v1/doc/features.html#relaxed", relaxed);
 			String tagName = null;
 			WorldData world = new WorldData();
 			while (xpp.next() != XmlPullParser.END_DOCUMENT) {
 				switch (xpp.getEventType()) {
 				case XmlPullParser.START_TAG:
 					tagName = xpp.getName().toLowerCase();
 					if (verbose) {
 						System.out.println("Parsing World Tag: " + tagName);
 					}
 					if (tagName.equals(WorldData.Shards.NUM_NATIONS.getTag())) {
 						world.numNations = Integer.parseInt(xpp.nextText());
 					}
 					else if (tagName.equals(WorldData.Shards.NUM_REGIONS.getTag())) {
 						world.numRegions = Integer.parseInt(xpp.nextText());
 					}
 					else if (tagName.equals(WorldData.Shards.CENSUS.getTag())) {
 						world.census = xpp.nextText();
 					}
 					else if (tagName.equals(WorldData.Shards.CENSUS_ID.getTag())) {
 						world.censusId = Integer.parseInt(xpp.nextText());
 					}
 					else if (tagName.equals(WorldData.Shards.CENSUS_SIZE.getTag())) {
 						world.censusSize = Integer.parseInt(xpp.nextText());
 					}
 					else if (tagName.equals(WorldData.Shards.CENSUS_SCALE.getTag())) {
 						world.censusScale = xpp.nextText();
 					}
 					else if (tagName.equals(WorldData.Shards.CENSUS_MEDIAN.getTag())) {
 						world.censusMedian = Integer.parseInt(xpp.nextText());
 					}
 					else if (tagName.equals(WorldData.Shards.FEATURED_REGION.getTag())) {
 						world.featuredRegion = xpp.nextText();
 					}
 					else if (tagName.equals(WorldData.Shards.NEW_NATIONS.getTag())) {
 						world.newNations = xpp.nextText().split(",");
 					}
 					else if (tagName.equals(WorldData.Shards.REGIONS_BY_TAG.getTag())) {
 						world.regionsByTag = xpp.nextText().split(",");
 					}
 					else if (verbose) {
 						System.err.println("Unknown world tag: " + tagName);
 					}
 					break;
 				}
 			}
 			return world;
 		} catch (XmlPullParserException e) {
 			throw new RuntimeException("Failed to parse XML", e);
 		} catch (IOException e) {
 			throw new RuntimeException("IOException parsing XML", e);
 		}
 		finally {
 			closeQuietly(data);
 		}
 	}
 
 	/**
 	 * Fetches information on the World Assembly
 	 * @param council what council to query (not used for some shards)
 	 * @param shards the shards to request
 	 * @return a WAData object with World Assembly info
 	 * @throws RateLimitReachedException if the rate limit was reached (but not exceeded)
 	 */
 	public WAData getWAInfo(WACouncil council, WAData.Shards...shards) {
 		if (!makeCall()) {
 			throw new RateLimitReachedException();
 		}
 		NSData data = null;
 		try {
 			data = getInfo("?wa="+council.getId(), shards);
 			XmlPullParser xpp = null;
 			xpp = data.xpp;
 			xpp.setFeature("http://xmlpull.org/v1/doc/features.html#relaxed", relaxed);
 			String tagName = null;
 			WAData wa = new WAData();
 			while (xpp.next() != XmlPullParser.END_DOCUMENT) {
 				switch (xpp.getEventType()) {
 				case XmlPullParser.START_TAG:
 					tagName = xpp.getName().toLowerCase();
 					if (tagName.equals(WAData.Shards.NUM_NATIONS.getTag())) {
 						wa.numNations = Integer.parseInt(xpp.nextText());
 					}
 					else if (tagName.equals(WAData.Shards.NUM_DELEGATES.getTag())) {
 						wa.numDelegates = Integer.parseInt(xpp.nextText());
 					}
 					else if (tagName.equals(WAData.Shards.DELEGATES.getTag())) {
 						wa.delegates = xpp.nextText().split(",");
 					}
 					else if (tagName.equals(WAData.Shards.MEMBERS.getTag())) {
 						wa.members = xpp.nextText().split(",");
 					}
 					else if (tagName.equals(WAData.Shards.HAPPENINGS.getTag())) {
 						wa.happenings = parseWAHappenings(xpp);
 					}
 					else if (tagName.equals(WAData.Shards.MEMBER_LOG.getTag())) {
 						wa.memberLog = parseWAMemberLog(xpp);
 					}
 					else if (tagName.equals(WAData.Shards.LAST_RESOLUTION.getTag())) {
 						// TODO FIXME This tag can contain invalid xml!
 						wa.lastResolution = xpp.nextText();
 					}
 					else if (verbose) {
 						System.err.println("Unknown WA tag: " + tagName);
 					}
 					break;
 				}
 			}
 			return wa;
 		} catch (XmlPullParserException e) {
 			throw new RuntimeException("Failed to parse XML", e);
 		} catch (IOException e) {
 			throw new RuntimeException("IOException parsing XML", e);
 		}
 		finally {
 			closeQuietly(data);
 		}
 	}
 
 	private List<WAHappening> parseWAHappenings(XmlPullParser xpp)
 		throws NumberFormatException, XmlPullParserException, IOException {
 		String tagName = null;
 		long ts = -1L;
 		String text = null;
 		ArrayList<WAHappening> happenings = new ArrayList<WAHappening>();
 		loop: while (xpp.next() != XmlPullParser.END_DOCUMENT)
 			switch (xpp.getEventType()) {
 			case XmlPullParser.START_TAG:
 				tagName = xpp.getName().toLowerCase();
 				if (tagName.equals(WAData.Shards.SubTags.HAPPENINGS_EVENT.getTag())) {
 					// Get timestamp
 					xpp.nextTag();
 					ts = Long.parseLong(xpp.nextText());
 					// Get text
 					xpp.nextTag();
 					text = xpp.nextText();
 					happenings.add(new WAHappening(ts, text));
 				}
 				break;
 			case XmlPullParser.END_TAG:
 				tagName = xpp.getName().toLowerCase();
 				if (tagName.equals(WAData.Shards.HAPPENINGS.getTag())) {
 					break loop;
 				}
 			}
 		return happenings;
 	}
 
 	private List<WAMemberLogHappening> parseWAMemberLog(XmlPullParser xpp)
 		throws NumberFormatException, XmlPullParserException, IOException {
 		String tagName = null;
 		long ts = -1L;
 		String text = null;
 		ArrayList<WAMemberLogHappening> happenings = new ArrayList<WAMemberLogHappening>();
 		loop: while (xpp.next() != XmlPullParser.END_DOCUMENT)
 			switch (xpp.getEventType()) {
 			case XmlPullParser.START_TAG:
 				tagName = xpp.getName().toLowerCase();
 				if (tagName.equals(WAData.Shards.SubTags.MEMBER_LOG_EVENT.getTag())) {
 					// Get timestamp
 					xpp.nextTag();
 					ts = Long.parseLong(xpp.nextText());
 					// Get text
 					xpp.nextTag();
 					text = xpp.nextText();
 					happenings.add(new WAMemberLogHappening(ts, text));
 				}
 				break;
 			case XmlPullParser.END_TAG:
 				tagName = xpp.getName().toLowerCase();
 				if (tagName.equals(WAData.Shards.MEMBER_LOG.getTag())) {
 					break loop;
 				}
 			}
 		return happenings;
 	}
 
 	/**
 	* Fetches information on a nation
 	* @param name the nation id
 	* @param arguments the shards to request
 	* @return a NationData object with nation info
 	* @throws RateLimitReachedException if the rate limit was reached (but not exceeded)
 	* @throws UnknownNationException if the nation could not be found
 	*/
 	public NationData getNationInfo(String name, NationData.Shards...shards) {
 		if (!makeCall()) {
 			throw new RateLimitReachedException();
 		}
 		NSData data = null;
 		try {
 			data = getInfo("?nation=" + name.replace(' ', '_'), shards);
 			if (verbose) {
 				System.out.println("Parsing Nation Info");
 			}
 			XmlPullParser xpp = null;
 			xpp = data.xpp;
 			xpp.setFeature("http://xmlpull.org/v1/doc/features.html#relaxed", relaxed);
 			String tagName = null;
 			NationData nation = new NationData();
 			while (xpp.next() != XmlPullParser.END_DOCUMENT) {
 				switch (xpp.getEventType()) {
 				case XmlPullParser.TEXT:
 					if (xpp.getText().contains("Unknown nation")) {
 						throw new UnknownNationException(name);
 					}
 					break;
 				case XmlPullParser.START_TAG:
 					if (verbose) {
 						System.out.println("Parsing Nation Tag: " + tagName);
 					}
 					tagName = xpp.getName().toLowerCase();
 					if (tagName.equals(NationData.Shards.CATEGORY.getTag())) {
 						nation.category = xpp.nextText();
 					}
 					else if (tagName.equals(NationData.Shards.FREEDOMS.getTag())) {
 						nation.freedoms = parseFreedoms(xpp, nation.freedoms);
 					}
 					else if (tagName.equals(NationData.Shards.FULL_NAME.getTag())) {
 						nation.fullName = xpp.nextText();
 					}
 					else if (tagName.equals(NationData.Shards.MOTTO.getTag())) {
 						nation.motto = xpp.nextText();
 					}
 					else if (tagName.equals(NationData.Shards.FLAG.getTag())) {
 						nation.flagURL = xpp.nextText();
 					}
 					else if (tagName.equals(NationData.Shards.REGION.getTag())) {
 						nation.region = xpp.nextText();
 					}
 					else if (tagName.equals(NationData.Shards.POPULATION.getTag())) {
 						nation.population = Integer.parseInt(xpp.nextText());
 					}
 					else if (tagName.equals(NationData.Shards.ADMIRABLE.getTag())) {
 						nation.admirable = xpp.nextText();
 					}
 					else if (tagName.equals(NationData.Shards.NOTABLE.getTag())) {
 						nation.notable = xpp.nextText();
 					}
 					else if (tagName.equals(NationData.Shards.SENSIBILITIES.getTag())) {
 						nation.sensibilities = xpp.nextText();
 					}
 					else if (tagName.equals(NationData.Shards.GOVERNMENT_DESCRIPTION.getTag())) {
 						nation.governmentDescription = xpp.nextText();
 					}
 					else if (tagName.equals(NationData.Shards.TAX_RATE.getTag())) {
 						nation.taxRate = Integer.parseInt(xpp.nextText());
 					}
 					else if (tagName.equals(NationData.Shards.INDUSTRY_DESCRIPTION.getTag())) {
 						nation.industryDescription = xpp.nextText();
 					}
 					else if (tagName.equals(NationData.Shards.LEGISLATION.getTag())) {
 						nation.legislation = parseLegislation(xpp);
 					}
 					else if (tagName.equals(NationData.Shards.CRIME.getTag())) {
 						nation.crime = xpp.nextText();
 					}
 					else if (tagName.equals(NationData.Shards.NAME.getTag())) {
 						nation.name = xpp.nextText();
 					}
 					else if (tagName.equals(NationData.Shards.ANIMAL.getTag())) {
 						nation.animal = xpp.nextText();
 					}
 					else if (tagName.equals(NationData.Shards.ANIMAL_TRAIT.getTag())) {
 						nation.animalTrait = xpp.nextText();
 					}
 					else if (tagName.equals(NationData.Shards.CURRENCY.getTag())) {
 						nation.currency = xpp.nextText();
 					}
 					else if (tagName.equals(NationData.Shards.LEADER.getTag())) {
 						String str = xpp.nextText();
 						nation.leader = (str.length() > 0 ? str : null);
 					}
 					else if (tagName.equals(NationData.Shards.RELIGION.getTag())) {
 						String str = xpp.nextText();
 						nation.religion = (str.length() > 0 ? str : null);
 					}
 					else if (tagName.equals(NationData.Shards.HAPPENINGS.getTag())) {
 						nation.happenings = parseNationHappenings(xpp);
 					}
 					else if (tagName.equals(NationData.Shards.TYPE.getTag())) {
 						nation.type = xpp.nextText();
 					}
 					else if (tagName.equals(NationData.Shards.WA_STATUS.getTag())) {
 						nation.worldAssemblyStatus = WAStatus.parse(xpp.nextText());
 					}
 					else if (tagName.equals(NationData.Shards.ENDORSEMENTS.getTag())) {
 						nation.endorsements = xpp.nextText().split(",");
 					}
 					else if (tagName.equals(NationData.Shards.GA_VOTE.getTag())) {
 						nation.generalAssemblyVote = WAVote.parse(xpp.nextText());
 					}
 					else if (tagName.equals(NationData.Shards.SC_VOTE.getTag())) {
 						nation.generalAssemblyVote = WAVote.parse(xpp.nextText());
 					}
 					else if (tagName.equals(NationData.Shards.MAJOR_INDUSTRY.getTag())) {
 						nation.majorIndustry = xpp.nextText();
 					}
 					else if (tagName.equals(NationData.Shards.GOVERNMENT_PRIORITY.getTag())) {
 						nation.governmentPriority = xpp.nextText();
 					}
 					else if (tagName.equals(NationData.Shards.GOVERNMENT_BUDGET.getTag())) {
 						nation.governmentBudget = parseBudget(xpp);
 					}
 					else if (tagName.equals(NationData.Shards.FOUNDED.getTag())) {
 						String str = xpp.nextText();
 						nation.founded = (str.equals("0") ? "In antiquity" : str);
 					}
 					else if (tagName.equals(NationData.Shards.FIRST_LOGIN.getTag())) {
 						nation.firstLogin = Long.parseLong(xpp.nextText());
 					}
 					else if (tagName.equals(NationData.Shards.LAST_ACTIVITY.getTag())) {
 						String str = xpp.nextText();
 						nation.lastActivity = (str.equals("0") ? "In antiquity" : str);
 					}
 					else if (tagName.equals(NationData.Shards.LAST_LOGIN.getTag())) {
 						nation.lastLogin = Long.parseLong(xpp.nextText());
 					}
 					else if (tagName.equals(NationData.Shards.INFLUENCE.getTag())) {
 						nation.influence = xpp.nextText();
 					}
 					else if (tagName.equals(NationData.Shards.FREEDOM_SCORES.getTag())) {
 						nation.freedoms = parseFreedomScores(xpp, nation.freedoms);
 					}
 					else if (tagName.equals(NationData.Shards.PUBLIC_SECTOR.getTag())) {
 						String str = xpp.nextText();
 						nation.publicSector = Integer.parseInt(str.substring(0, str.length() - 1));
 					}
 					else if (tagName.equals(NationData.Shards.DEATHS.getTag())) {
 						nation.deaths = parseDeaths(xpp);
 					}
 					else if (tagName.equals(NationData.Shards.CAPITAL.getTag())) {
 						nation.capital = xpp.nextText();
 					}
 					else if (tagName.equals(NationData.Shards.REGIONAL_CENSUS.getTag())) {
 						nation.regionalCensus = Integer.parseInt(xpp.nextText());
 					}
 					else if (tagName.equals(NationData.Shards.WORLD_CENSUS.getTag())) {
 						nation.worldCensus = Integer.parseInt(xpp.nextText());
 					}
 					else if (tagName.equals(NationData.Shards.CENSUS_SCORE.getTag())) {
 						if(nation.censusScore == null) {
 							nation.censusScore = new HashMap<Integer, Float>();
 						}
 						int id = Integer.parseInt(xpp.getAttributeValue(null,
 								NationData.Shards.Attributes.CENSUS_SCORE_ID.getName()));
 						nation.censusScore.put(id, Float.parseFloat(xpp.nextText()));
 					}
 					else if (verbose) {
 						System.err.println("Unknown nation tag: " + tagName);
 					}
 					break;
 				}
 			}
 			return nation;
 		} catch (XmlPullParserException e) {
 			throw new RuntimeException("Failed to parse XML", e);
 		} catch (IOException e) {
 			throw new RuntimeException("IOException parsing XML", e);
 		}
 		finally {
 			closeQuietly(data);
 		}
 	}
 
 	private NationFreedoms parseFreedoms(XmlPullParser xpp, NationFreedoms freedoms)
 		throws XmlPullParserException, IOException {
 		String tagName = null;
 		if (freedoms == null) {
 			freedoms = new NationFreedoms();
 		}
 		loop: while (xpp.next() != XmlPullParser.END_DOCUMENT) {
 			switch (xpp.getEventType()) {
 			case XmlPullParser.START_TAG:
 				tagName = xpp.getName().toLowerCase();
 				if (tagName.equals(NationData.Shards.SubTags.FREEDOMS_CIVIL_RIGHTS.getTag())) {
 					freedoms.civilRights = xpp.nextText();
 				}
 				else if (tagName.equals(NationData.Shards.SubTags.FREEDOMS_ECONOMY.getTag())) {
 					freedoms.economy = xpp.nextText();
 				}
 				else if (tagName.equals(NationData.Shards.SubTags.FREEDOMS_POLITICAL_FREEDOM.getTag())) {
 					freedoms.politicalFreedoms = xpp.nextText();
 				}
 				else if (verbose) {
 					System.err.println("Unknown freedom tag: " + tagName);
 				}
 				break;
 			case XmlPullParser.END_TAG:
 				tagName = xpp.getName().toLowerCase();
 				if (tagName.equals(NationData.Shards.FREEDOMS.getTag())) {
 					break loop;
 				}
 			}
 		}
 		return freedoms;
 	}
 
 	private NationFreedoms parseFreedomScores(XmlPullParser xpp, NationFreedoms freedoms)
 		throws XmlPullParserException, IOException {
 		String tagName = null;
 		if (freedoms == null)
 			freedoms = new NationFreedoms();
 		loop: while (xpp.next() != XmlPullParser.END_DOCUMENT)
 			switch (xpp.getEventType()) {
 			case XmlPullParser.START_TAG:
 				tagName = xpp.getName().toLowerCase();
 				if (tagName.equals(NationData.Shards.SubTags.FREEDOMS_CIVIL_RIGHTS.getTag())) {
 					freedoms.civilRightsValue = Integer.parseInt(xpp.nextText());
 				}
 				else if (tagName.equals(NationData.Shards.SubTags.FREEDOMS_ECONOMY.getTag())) {
 					freedoms.economyValue = Integer.parseInt(xpp.nextText());
 				}
 				else if (tagName.equals(NationData.Shards.SubTags.FREEDOMS_POLITICAL_FREEDOM.getTag())) {
 					freedoms.politicalFreedomsValue = Integer.parseInt(xpp.nextText());
 				}
 				else if (verbose) {
 					System.err.println("Unknown freedom score tag: " + tagName);
 				}
 				break;
 			case XmlPullParser.END_TAG:
 				tagName = xpp.getName().toLowerCase();
 				if (tagName.equals(NationData.Shards.FREEDOM_SCORES.getTag())) {
 					break loop;
 				}
 			}
 		return freedoms;
 	}
 
 	private String parseLegislation(XmlPullParser xpp)
 		throws XmlPullParserException, IOException {
 		String legislation = null;
 		String tagName = null;
 		loop: while (xpp.next() != XmlPullParser.END_DOCUMENT)
 			switch (xpp.getEventType()) {
 			case XmlPullParser.START_TAG:
 				tagName = xpp.getName().toLowerCase();
 				if (tagName.equals(NationData.Shards.SubTags.LEGISLATION_LAW.getTag())) {
 					if (legislation == null) {
 						legislation = xpp.nextText();
						if (legislation.trim().length() > 0) {
							legislation = legislation.substring(0, 1).toUpperCase() + legislation.substring(1);
						}
 					}
 					else {
 						legislation += "||&&|| " + xpp.nextText();
 					}
 				}
 				break;
 			case XmlPullParser.END_TAG:
 				tagName = xpp.getName().toLowerCase();
 				if (tagName.equals(NationData.Shards.LEGISLATION.getTag())) {
					if(legislation.length() > 0) {
						legislation += ". ";
					}
 					break loop;
 				}
 				break;
 			}
 		int i = legislation.lastIndexOf("||&&||");
 		if(i > -1) {
 			legislation = legislation.substring(0, i).replace("||&&||", ",") +
 					legislation.substring(i).replace("||&&||", " and");
 		}
 		return legislation;
 	}
 
 	private List<NationHappening> parseNationHappenings(XmlPullParser xpp)
 		throws NumberFormatException, XmlPullParserException, IOException {
 		String tagName = null;
 		long ts = -1L;
 		String text = null;
 		ArrayList<NationHappening> happenings = new ArrayList<NationHappening>();
 		loop: while (xpp.next() != XmlPullParser.END_DOCUMENT)
 			switch (xpp.getEventType()) {
 			case XmlPullParser.START_TAG:
 				tagName = xpp.getName().toLowerCase();
 				if (tagName.equals(NationData.Shards.SubTags.HAPPENINGS_EVENT.getTag())) {
 					// Get timestamp
 					xpp.nextTag();
 					ts = Long.parseLong(xpp.nextText());
 					// Get text
 					xpp.nextTag();
 					text = xpp.nextText();
 					happenings.add(new NationHappening(ts, text));
 				}
 				break;
 			case XmlPullParser.END_TAG:
 				tagName = xpp.getName().toLowerCase();
 				if (tagName.equals(NationData.Shards.HAPPENINGS.getTag())) {
 					break loop;
 				}
 			}
 		return happenings;
 	}
 
 	private Budget parseBudget(XmlPullParser xpp)
 		throws XmlPullParserException, IOException {
 		String tagName = null;
 		String str = null;
 		int value = -1;
 		Budget budget = new Budget();
 		loop: while (xpp.next() != XmlPullParser.END_DOCUMENT)
 			switch (xpp.getEventType()) {
 			case XmlPullParser.START_TAG:
 				tagName = xpp.getName().toLowerCase();
 				str = xpp.nextText();
 				// Get value without %-sign
 				value = Integer.parseInt(str.substring(0, str.length() - 1));
 				if (tagName.equals(NationData.Shards.SubTags.BUDGET_ENVIRONMENT.getTag())) {
 					budget.environment = value;
 				}
 				else if (tagName.equals(NationData.Shards.SubTags.BUDGET_SOCIAL_EQUALITY.getTag())) {
 					budget.socialEquality = value;
 				}
 				else if (tagName.equals(NationData.Shards.SubTags.BUDGET_EDUCATION.getTag())) {
 					budget.education = value;
 				}
 				else if (tagName.equals(NationData.Shards.SubTags.BUDGET_LAW_AND_ORDER.getTag())) {
 					budget.lawAndOrder = value;
 				}
 				else if (tagName.equals(NationData.Shards.SubTags.BUDGET_ADMINISTRATION.getTag())) {
 					budget.administration = value;
 				}
 				else if (tagName.equals(NationData.Shards.SubTags.BUDGET_WELFARE.getTag())) {
 					budget.welfare = value;
 				}
 				else if (tagName.equals(NationData.Shards.SubTags.BUDGET_SPIRITUALITY.getTag())) {
 					budget.spirituality = value;
 				}
 				else if (tagName.equals(NationData.Shards.SubTags.BUDGET_DEFENCE.getTag())) {
 					budget.defence = value;
 				}
 				else if (tagName.equals(NationData.Shards.SubTags.BUDGET_PUBLIC_TRANSPORT.getTag())) {
 					budget.publicTransport = value;
 				}
 				else if (tagName.equals(NationData.Shards.SubTags.BUDGET_HEALTHCARE.getTag())) {
 					budget.healthCare = value;
 				}
 				else if (tagName.equals(NationData.Shards.SubTags.BUDGET_COMMERCE.getTag())) {
 					budget.commerce = value;
 				}
 				else if (verbose) {
 					System.err.println("Unknown budget tag: " + tagName);
 				}
 				break;
 			case XmlPullParser.END_TAG:
 				tagName = xpp.getName().toLowerCase();
 				if (tagName.equals(NationData.Shards.GOVERNMENT_BUDGET.getTag())) {
 					break loop;
 				}
 			}
 		return budget;
 	}
 
 	private Map<CauseOfDeath, Integer> parseDeaths(XmlPullParser xpp)
 		throws NumberFormatException, XmlPullParserException, IOException {
 		String tagName = null;
 		String type = null;
 		String str = null;
 		int value = -1;
 		HashMap<CauseOfDeath, Integer> deaths = new HashMap<CauseOfDeath, Integer>();
 		loop: while (xpp.next() != XmlPullParser.END_DOCUMENT)
 			switch (xpp.getEventType()) {
 			case XmlPullParser.START_TAG:
 				tagName = xpp.getName().toLowerCase();
 				if (tagName.equals(NationData.Shards.SubTags.DEATHS_CAUSE.getTag())) {
 					type = xpp.getAttributeValue(null, NationData.Shards.Attributes.DEATHS_CAUSE_TYPE.getName());
 					str = xpp.nextText();
 					// Get value without %-sign
 					value = Integer.parseInt(str.substring(0, str.length() - 1));
 					deaths.put(CauseOfDeath.parse(type), Integer.valueOf(value));
 				}
 				break;
 			case XmlPullParser.END_TAG:
 				tagName = xpp.getName().toLowerCase();
 				if (tagName.equals(NationData.Shards.DEATHS.getTag())) {
 					break loop;
 				}
 			}
 		return deaths;
 	}
 
 	/**
 	 * Fetches information on a region
 	 * @param NSData to use to retrieve region info
 	 * @param name of the region id
 	 * @return a RegionData object with region info
 	 * @throws RateLimitReachedException if the rate limit was reached (but not exceeded)
 	 * @throws UnknownRegionException if the region could not be found
 	 */
 	public RegionData getRegionInfo(NSData data, String name) {
 		try {
 			if (verbose) {
 				System.out.println("Parsing Region Info");
 			}
 			String tagName = null;
 			XmlPullParser xpp = data.xpp;
 			xpp.setFeature("http://xmlpull.org/v1/doc/features.html#relaxed", relaxed);
 			RegionData region = new RegionData();
 			while (xpp.next() != XmlPullParser.END_DOCUMENT)
 				switch (xpp.getEventType()) {
 				case XmlPullParser.TEXT:
 					if (xpp.getText().contains("Unknown region")) {
 						throw new UnknownRegionException(name);
 				}
 					break;
 				case XmlPullParser.START_TAG:
 					tagName = xpp.getName().toLowerCase();
 					if (verbose) {
 						System.out.println("Parsing Region Tag: " + tagName);
 					}
 					if (tagName.equals(RegionData.Shards.FLAG.getTag())) {
 						region.flagURL = xpp.nextText();
 					}
 					else if (tagName.equals(RegionData.Shards.NAME.getTag())) {
 						region.name = xpp.nextText();
 					}
 					else if (tagName.equals(RegionData.Shards.FACTBOOK.getTag())) {
 						region.factbook = xpp.nextText();
 					}
 					else if (tagName.equals(RegionData.Shards.DELEGATE.getTag())) {
 						region.delegate = xpp.nextText();
 					}
 					else if (tagName.equals(RegionData.Shards.FOUNDER.getTag())) {
 						region.founder = xpp.nextText();
 					}
 					else if (tagName.equals(RegionData.Shards.HAPPENINGS.getTag())) {
 						region.happenings = parseRegionHappenings(xpp);
 					}
 					else if (tagName.equals(RegionData.Shards.MESSAGES.getTag())) {
 						region.messages = parseRMBMessages(xpp);
 					}
 					else if (tagName.equals(RegionData.Shards.NUM_NATIONS.getTag())) {
 						region.numNations = Integer.parseInt(xpp.nextText());
 					}
 					else if (tagName.equals(RegionData.Shards.NATIONS.getTag())) {
 						region.nations = xpp.nextText().split(":");
 					}
 					else if (tagName.equals(RegionData.Shards.DELEGATE_VOTES.getTag())) {
 						region.delegateVotes = Integer.parseInt(xpp.nextText());
 					}
 					else if (tagName.equals(RegionData.Shards.GA_VOTES.getTag())) {
 						region.generalAssemblyVotes = parseWAVotes(xpp, RegionData.Shards.GA_VOTES.getTag());
 					}
 					else if (tagName.equals(RegionData.Shards.SC_VOTES.getTag())) {
 						region.securityCouncilVotes = parseWAVotes(xpp, RegionData.Shards.SC_VOTES.getTag());
 					}
 					else if (tagName.equals(RegionData.Shards.POWER.getTag())) {
 						region.power = xpp.nextText();
 					}
 					else if (tagName.equals(RegionData.Shards.EMBASSIES.getTag())) {
 						region.embassies = parseEmbassies(xpp);
 					}
 					else if (tagName.equals(RegionData.Shards.TAGS.getTag())) {
 						region.tags = parseTags(xpp);
 					}
 					else if (verbose) {
 						System.err.println("Unknown region tag: " + tagName);
 					}
 					break;
 				}
 			return region;
 		} catch (XmlPullParserException e) {
 			throw new RuntimeException("Failed to parse XML", e);
 		} catch (IOException e) {
 			throw new RuntimeException("IOException parsing XML", e);
 		}
 		finally {
 			closeQuietly(data);
 		}
 	}
 
 	/**
 	 * Fetches information on a region
 	 * @param name the region id
 	 * @param arguments the shards to request
 	 * @return a RegionData object with region info
 	 * @throws RateLimitReachedException if the rate limit was reached (but not exceeded)
 	 * @throws UnknownRegionException if the region could not be found
 	 */
 	public RegionData getRegionInfo(String name, RegionData.Shards...shards) {
 		if (!makeCall()) {
 			throw new RateLimitReachedException();
 		}
 		try {
 			return getRegionInfo(getInfo(new StringBuilder().append("?region=").append(name.replace(' ', '_')).toString(), shards), name);
 		} catch (XmlPullParserException e) {
 			throw new RuntimeException("Failed to parse XML", e);
 		} catch (IOException e) {
 			throw new RuntimeException("IOException parsing XML", e);
 		}
 	}
 
 	private List<RegionHappening> parseRegionHappenings(XmlPullParser xpp)
 		throws NumberFormatException, XmlPullParserException, IOException {
 		String tagName = null;
 		long ts = -1L;
 		String text = null;
 		ArrayList<RegionHappening> happenings = new ArrayList<RegionHappening>();
 		loop: while (xpp.next() != XmlPullParser.END_DOCUMENT) {
 			switch (xpp.getEventType()) {
 			case XmlPullParser.START_TAG:
 				tagName = xpp.getName().toLowerCase();
 				if (tagName.equals(RegionData.Shards.SubTags.HAPPENINGS_EVENT.getTag())) {
 					// Get timestamp
 					xpp.nextTag();
 					ts = Long.parseLong(xpp.nextText());
 					// Get text
 					xpp.nextTag();
 					text = xpp.nextText();
 					happenings.add(new RegionHappening(ts, text));
 				}
 				break;
 			case XmlPullParser.END_TAG:
 				tagName = xpp.getName().toLowerCase();
 				if (tagName.equals(RegionData.Shards.HAPPENINGS.getTag())) {
 					break loop;
 				}
 			}
 		}
 		return happenings;
 	}
 
 	private List<RMBMessage> parseRMBMessages(XmlPullParser xpp)
 		throws NumberFormatException, XmlPullParserException, IOException {
 		String tagName = null;
 		long ts = -1L;
 		String nation = null;
 		String text = null;
 		ArrayList<RMBMessage> messages = new ArrayList<RMBMessage>();
 		loop: while (xpp.next() != XmlPullParser.END_DOCUMENT) {
 			switch (xpp.getEventType()) {
 			case XmlPullParser.START_TAG:
 				tagName = xpp.getName().toLowerCase();
 				if (tagName.equals(RegionData.Shards.SubTags.MESSAGES_POST.getTag())) {
 					// Get timestamp
 					xpp.nextTag();
 					ts = Long.parseLong(xpp.nextText());
 					// Get nation name
 					xpp.nextTag();
 					nation = xpp.nextText();
 					// Get text
 					xpp.nextTag();
 					text = xpp.nextText();
 					messages.add(new RMBMessage(ts, nation, text));
 				}
 				break;
 			case XmlPullParser.END_TAG:
 				tagName = xpp.getName().toLowerCase();
 				if (tagName.equals(RegionData.Shards.MESSAGES.getTag())) {
 					break loop;
 				}
 			}
 		}
 		return messages;
 	}
 
 	private WAVotes parseWAVotes(XmlPullParser xpp, String vote)
 		throws NumberFormatException, XmlPullParserException, IOException {
 		String tagName = null;
 		WAVotes votes = new WAVotes();
 		loop: while (xpp.next() != XmlPullParser.END_DOCUMENT) {
 			switch (xpp.getEventType()) {
 			case XmlPullParser.START_TAG:
 				tagName = xpp.getName().toLowerCase();
 				if (tagName.equals(RegionData.Shards.SubTags.WA_VOTES_FOR.getTag())) {
 					String str = xpp.nextText();
 					votes.forVotes = str.length() == 0 ? 0 : Integer.parseInt(str);
 				}
 				else if (tagName.equals(RegionData.Shards.SubTags.WA_VOTES_AGAINST.getTag())) {
 					String str = xpp.nextText();
 					votes.againstVotes = str.length() == 0 ? 0 : Integer.parseInt(str);
 				}
 				else {
 					System.err.println("Unknown WA voting tag: " + tagName);
 				}
 				break;
 			case XmlPullParser.END_TAG:
 				tagName = xpp.getName().toLowerCase();
 				if (tagName.equals(vote)) {
 					break loop;
 				}
 			}
 		}
 		return votes;
 	}
 
 	private List<Embassy> parseEmbassies(XmlPullParser xpp)
 		throws NumberFormatException, XmlPullParserException, IOException {
 		String tagName = null;
 		Embassy embassy = null;
 		List<Embassy> embassies = new ArrayList<Embassy>();
 		loop: while (xpp.next() != XmlPullParser.END_DOCUMENT) {
 			switch (xpp.getEventType()) {
 			case XmlPullParser.START_TAG:
 				tagName = xpp.getName().toLowerCase();
 				if (tagName.equals(RegionData.Shards.SubTags.EMBASSIES_EMBASSY.getTag())) {
 					embassy = new Embassy();
 					embassy.status = Embassy.EmbassyStatus.parse(xpp.getAttributeValue(null, "type"));
 					embassy.region = xpp.nextText();
 					embassies.add(embassy);
 				}
 				break;
 			case XmlPullParser.END_TAG:
 				tagName = xpp.getName().toLowerCase();
 				if (tagName.equals(RegionData.Shards.EMBASSIES.getTag())) {
 					break loop;
 				}
 			}
 		}
 		return embassies;
 	}
 
 	private List<String> parseTags(XmlPullParser xpp)
 		throws NumberFormatException, XmlPullParserException, IOException {
 		String tagName = null;
 		List<String> tags = new ArrayList<String>();
 		loop: while (xpp.next() != XmlPullParser.END_DOCUMENT) {
 			switch (xpp.getEventType()) {
 			case XmlPullParser.START_TAG:
 				tagName = xpp.getName().toLowerCase();
 				if (tagName.equals(RegionData.Shards.SubTags.TAGS_TAG.getTag())) {
 					tags.add(xpp.nextText());
 				}
 				break;
 			case XmlPullParser.END_TAG:
 				tagName = xpp.getName().toLowerCase();
 				if (tagName.equals(RegionData.Shards.TAGS.getTag())) {
 					break loop;
 				}
 			}
 		}
 		return tags;
 	}
 
 	/**
 	 * Fetches data from the NationStates Shards API
 	 * @param urlStart the start of the URL
 	 * @param shards what shards to include
 	 * @return an NSData object with the XmlPullParser and InputStream
 	 * @throws XmlPullParserException if there was a problem with parsing the xml
 	 * @throws IOException if there was a network problem
 	 */
 	private NSData getInfo(String urlStart, IShards...shards) throws XmlPullParserException, IOException {
 		String shardsStr = buildShardString(shards);
 		String str = API + urlStart + (this.version > -1 ? "&v=" + this.version : "") +
 				"&q=" + shardsStr;
 
 		return getInfo(doRequest(str));
 	}
 
 	private String buildShardString(IShards...shards) {
 		String shardsStr = null;
 		for (IShards s : shards) {
 			if(shardsStr == null) {
 				shardsStr = s.getName();
 			} else {
 				shardsStr += "+" + s.getName();
 			}
 			if(s.getArguments() != null) {
 				Map<IArguments, String> args = s.getArguments();
 				for(Entry<IArguments, String> a : args.entrySet()) {
 					shardsStr += ";" + a.getKey().getName() + "=" + a.getValue();
 				}
 			}
 		}
 		return shardsStr;
 	}
 
 	private synchronized InputStream doRequest(String url) throws IOException {
 		if (verbose) {
 			System.out.println("Making HTTP request: " + url);
 		}
 
 		HttpClient client = new DefaultHttpClient();
 		if (proxyIP != null) {
 			HttpHost proxy = new HttpHost(proxyIP, proxyPort);
 			client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
 		}
 		client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, this.userAgent);
 		client.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
 		HttpGet get = new HttpGet(url);
 		HttpResponse response = client.execute(get);
 		if (verbose) {
 			System.out.println("Status code for request: " + response.getStatusLine().getStatusCode());
 		}
 		if (response.getStatusLine().getStatusCode() == 429)  {
 			hardRateLimit = System.currentTimeMillis() + 900000L; //15 min
 			throw new RateLimitReachedException();
 		}
 		return response.getEntity().getContent();
 	}
 
 	/**
 	 * Creates data from the inputstream
 	 * @param inputstream to create data with
 	 * @throws XmlPullParserException if there was a problem with parsing the xml
 	 * @throws IOException if there was a network problem
 	 */
 	public NSData getInfo(InputStream stream) throws XmlPullParserException, IOException {
 		KXmlParser xpp = new KXmlParser();
 		xpp.setInput(stream, "ISO-8859-15");
 		return new NSData(xpp, stream);
 	}
 }

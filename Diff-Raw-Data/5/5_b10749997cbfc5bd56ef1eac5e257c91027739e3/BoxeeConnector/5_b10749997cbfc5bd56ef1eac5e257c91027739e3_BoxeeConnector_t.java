 package net.evendanan.android.thumbremote.boxee;
 
 import java.io.IOException;
 import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.client.HttpResponseException;
 
 import net.evendanan.android.thumbremote.MediaStateListener;
 import net.evendanan.android.thumbremote.ServerAddress;
 import net.evendanan.android.thumbremote.ServerConnector;
 import net.evendanan.android.thumbremote.network.HttpRequest;
 import net.evendanan.android.thumbremote.network.Response;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.text.TextUtils;
 import android.util.Log;
 
 
 /*
  * ftp://ohnopublishing.net/distfiles/svn-src/xbmc/trunk/xbmc/lib/libGoAhead/XBMChttp.cpp
  * http://wiki.xbmc.org/index.php?title=Web_Server_HTTP_API
  * http://developer.boxee.tv/Remote_Control_Interface#Remote_Control_Interface
  */
 public class BoxeeConnector implements ServerConnector  {
 	public static final String BOXEE_SERVER_TYPE = "Boxee";
 	public static final String BOXEE_SERVER_VERSION_OLD = "0.9";
 	public static final String BOXEE_SERVER_VERSION_NEW = "1.1";
 	
 	private static final String TAG = "BoxeeConnector";
 	
 	private final Queue<String[]> mUrlsToDo = new LinkedList<String[]>();
 	
 	//various heavily used strings
 	private String mRequestPrefix = null;
 	private String mRequestPausePlay = null;
 	private String mRequestStop = null;
 	private String mSendKeyRequestTemplate = null;
 	private String mSeekPercentageRelativeTemplate = null;
 	private String mSeekPercentageTemplate = null;
 	private String[] mCurrentPlayingUrls = null;
 	private String mRequestUp = null;
 	private String mRequestDown = null;
 	private String mRequestLeft = null;
 	private String mRequestRight = null;
 	private String mRequestBack = null;
 	private String mRequestSelect = null;
 	private String mRequestGetVolume = null;
 	private String mRequestSetVolumeTemplate = null;
 	//private String mRequestGetOnScreenText = null;
 	
 	final static Pattern LIST_ITEM = Pattern.compile("^<li>([A-Za-z ]+):([^\n<]+)", Pattern.MULTILINE);
 	final static Pattern SINGLE_ITEM = Pattern.compile("^<li>([0-9]+)", Pattern.MULTILINE);
 	//private static final String KEYBOARD_TEXT_KEY = "KEYBOARD_TEXT_KEY";
 	private static final String KEYBOARD_ACTIVE_KEY = "KEYBOARD_ACTIVE_KEY";
 
 	private HashMap<String, String> mEntries = new HashMap<String, String>();
 	private boolean mInMoreDataState = false;
 	private Bitmap mThumbnail;
 	
 	private MediaStateListener mUiView;
 
 	private void clear() {
 		synchronized (mEntries) {
 			mEntries.clear();			
 		}
 	}
 	
 	@Override
 	public void setUiView(MediaStateListener uiView) {
 		mUiView = uiView;
 	}
 	
 	@Override
 	public void setServer(ServerAddress server) {
 		if (server == null || server.address() == null || server.port() < 1)
 		{
 			mRequestPrefix = null;
 			mRequestPausePlay = null;
 			mRequestStop = null;
 			mSendKeyRequestTemplate = null;
 			mCurrentPlayingUrls = null;
 			mSeekPercentageRelativeTemplate = null;
 			mSeekPercentageTemplate = null;
 			mRequestUp = null;
 			mRequestDown = null;
 			mRequestLeft = null;
 			mRequestRight = null;
 			mRequestBack = null;
 			mRequestSelect = null;
 			mRequestGetVolume = null;
 			mRequestSetVolumeTemplate = null;
 			//mRequestGetOnScreenText = null;
 		}
 		else
 		{
 			//setting up various heavily used strings
 			String host = server.address().getHostAddress();
 			int port = server.port();
 			mRequestPrefix = String.format("http://%s:%d/xbmcCmds/xbmcHttp?command=", host, port);
 			mSendKeyRequestTemplate = mRequestPrefix+"SendKey(%d)";
 			mRequestUp = String.format(mSendKeyRequestTemplate, 270);
 			mRequestDown = String.format(mSendKeyRequestTemplate, 271);
 			mRequestLeft = String.format(mSendKeyRequestTemplate, 272);
 			mRequestRight = String.format(mSendKeyRequestTemplate, 273);
 			mRequestBack = String.format(mSendKeyRequestTemplate, 275);
 			mRequestSelect = String.format(mSendKeyRequestTemplate, 256);
 			mRequestPausePlay = mRequestPrefix+"Pause";
 			mRequestStop = mRequestPrefix+"Stop";
 			mSeekPercentageRelativeTemplate = mRequestPrefix + "SeekPercentageRelative(%3.5f)";
 			mSeekPercentageTemplate = mRequestPrefix + "SeekPercentage(%3.5f)";
 			mRequestGetVolume = mRequestPrefix+"getVolume()";
 			mRequestSetVolumeTemplate = mRequestPrefix+"setVolume(%d)";
 			//mRequestGetOnScreenText = mRequestPrefix+"getKeyboardText()";
 			
 			mCurrentPlayingUrls = new String[]{
 					mRequestPrefix + "getcurrentlyplaying()",
 					mRequestPrefix + "getguistatus()",
 					mRequestPrefix + "getKeyboardText()"
 			};
 			synchronized (mEntries) {
 				mEntries.clear();
 			}
 		}
 	}
 	
 	@Override
 	public void startOver() {
 		mInMoreDataState = false;
 		mUrlsToDo.clear();
 		if (mCurrentPlayingUrls != null)
 			mUrlsToDo.add(mCurrentPlayingUrls);
 	}
 
 	@Override
 	public boolean requiresServerStateData() {
 		return mUrlsToDo.size() > 0;
 	}
 
 	@Override
 	public String[] getServerStateUrls() {
 		return mUrlsToDo.poll();
 	}
 
 	@Override
 	public void onServerStateResponsesAvailable(String[] responses) {
 		synchronized (mEntries) {
 			if (mInMoreDataState)
 			{
 				mInMoreDataState = false;
 				String bitmapResponse = responses[0];
 				String shorter = bitmapResponse.replaceAll("<html>", "").replaceAll("</html>", "");
 				byte[] thumb;
 				try {
 					thumb = iharder.base64.Base64.decode(shorter.getBytes());
 				} catch (Exception e) {//protect about decode's IOExceptions and other RuntimeExceptions
 					thumb = null;
 					e.printStackTrace();
 				}
 				
 				if (thumb != null)
 				{
 					mThumbnail = BitmapFactory.decodeByteArray(thumb, 0, thumb.length);
 					mUiView.onMediaMetadataChanged(this);
 				}
 				else
 				{
 					mThumbnail = null;
 				}
 			}
 			else
 			{
 				final String filename = getMediaFilename();
 				final boolean isPlaying = isMediaPlaying();
 				final boolean isMediaActive = isMediaActive();
 				final String time = getMediaCurrentTime();
 				final String posterUrl = getEntryValue("Thumb");
 				final boolean keyboardActive = isKeyboardActive();
 				clear();
 				if (responses != null)//it can be null if there are lots of network errors.
 				{
 					for(String response : responses)
 					{
 						if (response.startsWith("<html>") && response.contains("<boxee:keyboard active"))
 						{
 							if (response.contains("<boxee:keyboard active=\"1\""))
 								mEntries.put(KEYBOARD_ACTIVE_KEY, "true");
 							else if (response.contains("<boxee:keyboard active=\"0\""))
 								mEntries.put(KEYBOARD_ACTIVE_KEY, "false");
 							
 							final boolean newKeyboardActive = isKeyboardActive();
 							
 							/*if (newKeyboardActive)
 							{
 								int textStartIndex = response.indexOf("text=\"") + "text=\"".length();
 								int textEndIndex = response.indexOf("\" hidden=\"", textStartIndex);
 								mEntries.put(KEYBOARD_TEXT_KEY, response.substring(textStartIndex, textEndIndex));
 							}
 							else
 							{
 								mEntries.put(KEYBOARD_TEXT_KEY, "");
 							}*/
 							if (keyboardActive != newKeyboardActive)
 							{
 								mUiView.onKeyboardStateChanged(this);
 							}
 						}
 						else
 						{
 							Matcher m = LIST_ITEM.matcher(response);
 							while (m.find()) {
 								//Log.d(TAG, "mEntries put '"+m.group(1)+"' = '"+m.group(2)+"'");
 								mEntries.put(m.group(1), m.group(2));
 							}
 						}
 					}
 				}
 				/*
 				* void onPlayingStateChanged(ServerState serverState);
 				* void onPlayingProgressChanged(ServerState serverState);
 				* void onMetadataChanged(ServerState serverState);
 				 */
 				if (isPlaying != isMediaPlaying() || isMediaActive != isMediaActive())
 					mUiView.onMediaPlayingStateChanged(this);
 				if (!time.equals(getMediaCurrentTime()))
 					mUiView.onMediaPlayingProgressChanged(this);
 				if (filename.equals(getMediaFilename()))
 					mUiView.onMediaMetadataChanged(this);
 				final String newPoster = getEntryValue("Thumb");
 				if (!posterUrl.equals(newPoster))
 				{
 					Log.d(TAG, "New poster at:"+newPoster);
 					if (!TextUtils.isEmpty(newPoster))
 					{
 						mInMoreDataState = true;
 						mUrlsToDo.add(new String[]{mRequestPrefix + String.format("getthumbnail(%s)", URLEncoder.encode(newPoster))});
 					}	
 				}
 			}
 		}
 	}
 	
 	@Override
 	public void onServerStateRetrievalError(int errorCode) {
 		clear();
 		mThumbnail = null;
 		//notifying UI
 		if (mUiView != null)
 		{
 			mUiView.onMediaPlayingStateChanged(this);
 			mUiView.onMediaPlayingProgressChanged(this);
 			mUiView.onMediaMetadataChanged(this);
 			mUiView.onKeyboardStateChanged(this);
 		}
 		
 		startOver();
 	}
 
 	private String getEntryValue(String key) {
 		synchronized (mEntries) {
 			return mEntries.containsKey(key) ? mEntries.get(key) : "";
 		}
 	}
 
 	/*Media information*/
 	
 	@Override
 	public String getMediaFilename() {
 		return getEntryValue("Filename");
 	};
 	
 	@Override
 	public String getMediaTitle() {
 		return getEntryValue("Title");
 	}
 	
 	@Override
 	public String getShowTitle() {
 		return getEntryValue("Show Title");
 	}
 	
 	@Override
 	public String getShowSeason() {
 		return getEntryValue("Season");
 	}
 	
 	@Override
 	public String getShowEpisode() {
 		return getEntryValue("Episode");
 	}
 
 	@Override
 	public String getMediaPlot() {
 		return getEntryValue("Plot");
 	}
 	
 	@Override
 	public String getMediaType() {
 		return getEntryValue("Type");
 	}
 	
 	@Override
 	public String getMediaTotalTime() {
 		return getEntryValue("Duration");
 	}
 
 	@Override
 	public String getMediaCurrentTime() {
 		return getEntryValue("Time");
 	}
 
 	@Override
 	public int getMediaProgressPercent() {
 		String p = getEntryValue("Percentage");
 		if (!TextUtils.isEmpty(p) && TextUtils.isDigitsOnly(p))
 			return Integer.parseInt(p);
 		else
 			return 0;
 	}
 
 	@Override
 	public Bitmap getMediaPoster() {
 		return mThumbnail;
 	}
 
 	@Override
 	public boolean isMediaPlaying() {
 		return getEntryValue("PlayStatus").equals("Playing");
 	}
 	
 	@Override
 	public boolean isMediaActive() {
 		synchronized (mEntries) {
 			return mEntries.containsKey("Time");
 		}
 	}
 	/*
 	@Override
 	public String getKeyboardText() {
 		return getEntryValue(KEYBOARD_TEXT_KEY);
 	}
 	*/
 	@Override
 	public boolean isKeyboardActive() {
 		return getEntryValue(KEYBOARD_ACTIVE_KEY).equals("true");
 	}
 	
 	/*CONTROL*/
 
 	private Response sendHttpCommand(final String request) throws HttpResponseException {
 		Response response = HttpRequest.getHttpResponse(request);
 		if (!response.success())
 		{
 			throw new HttpResponseException(response.responseCode(), response.response());
 		}
 		return response;
 	}
 	
 	private void sendKeyPress(int keycode) throws IOException  {
 		String request = String.format(mSendKeyRequestTemplate, keycode);
 
 		sendHttpCommand(request);
 	}
 	
 	@Override
 	public int getVolume() throws IOException {
 		Response response = sendHttpCommand(mRequestGetVolume);
 		Matcher m = SINGLE_ITEM.matcher(response.response());
 		if (m != null && m.find()) {
 			try
 			{
 				final int currentVolume = Integer.parseInt(m.group(1));
 				return currentVolume;
 			}
 			catch(Exception e)
 			{
 				Log.w(TAG, "Failed to parsse current volume! Got value "+m.group(1));
 				return 50;
 			}
 		}
 		else
 		{
 			throw new IOException("Failed to understand server response!");
 		}
 	}
 	
 	@Override
 	public void setVolume(final int percent) throws IOException {
 		int newVolume = Math.max(0, Math.min(100, percent));
 		Log.d(TAG, "Setting volume to " + newVolume);
 		final String setvolume = String.format(mRequestSetVolumeTemplate, newVolume);
 		sendHttpCommand(setvolume);
 	}
 	
 	@Override
 	public void up() throws IOException {
 		sendHttpCommand(mRequestUp);
 	}
 
 	@Override
 	public void down() throws IOException {
 		sendHttpCommand(mRequestDown);
 	}
 
 	@Override
 	public void left() throws IOException {
 		sendHttpCommand(mRequestLeft);
 	}
 
 	@Override
 	public void right() throws IOException {
 		sendHttpCommand(mRequestRight);
 	}
 
 	@Override
 	public void back() throws IOException {
 		sendHttpCommand(mRequestBack);
 	}
 
 	@Override
 	public void select() throws IOException {
 		sendHttpCommand(mRequestSelect);
 	}
 
 	@Override
 	public void keypress(int unicode) throws IOException {
 		if (unicode == 8)//backspace is a special case
 			sendKeyPress(61704);
 		else
 		{
			/* issue #3: allow lowercase letters anyhow
 			if (Character.isLetter(unicode)) unicode = Character.toUpperCase(unicode);
			//this is a bug I can't find a way to fix: lowercase L does not register in Boxee....
			 */
 			sendKeyPress(unicode + 61696);
 		}
 	}
 
 	@Override
 	public void flipPlayPause() throws IOException {
 		sendHttpCommand(mRequestPausePlay);
 	}
 
 	@Override
 	public void stop() throws IOException {
 		sendHttpCommand(mRequestStop);
 	}
 
 	@Override
 	public void seekRelative(double pct) throws IOException {
 		String request = String.format(mSeekPercentageRelativeTemplate, pct);
 
 		sendHttpCommand(request);
 	}
 
 	@Override
 	public void seekTo(double pct) throws IOException {
 		String request = String.format(mSeekPercentageTemplate, pct);
 
 		sendHttpCommand(request);
 	}
 //	
 //	public String getOnScreenTextboxText() throws IOException
 //	{
 //		Response response = sendHttpCommand(mRequestGetOnScreenText);
 //		Matcher m = SINGLE_ITEM.matcher(response.response());
 //		if (m != null && m.find()) {
 //			return m.group(1);
 //		}
 //		else
 //		{
 //			throw new IOException("Failed to understand server response!");
 //		}
 //	}
 
 }

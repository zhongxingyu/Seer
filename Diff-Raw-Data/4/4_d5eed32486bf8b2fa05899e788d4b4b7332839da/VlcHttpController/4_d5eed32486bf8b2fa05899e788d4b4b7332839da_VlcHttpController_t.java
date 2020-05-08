 package com.guillaumecharmetant.vlchttpcontroller;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URLEncoder;
 
 import org.apache.http.client.HttpClient;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.util.Log;
 
 public class VlcHttpController {
 	private static final String TAG = "VHC";
 	
 	private static final int VOLUME_STEP = 100;
 	
 	private static final String ENCODING = "UTF-8";
 	private static final String URLENCODED_PLUS;
 	private static final String URLENCODED_MINUS;
	@SuppressWarnings("unused")
 	private static final String URLENCODED_PERCENT;
 	static {
 		String urlEncodedPlus;
 		String urlEncodedMinus;
 		String urlEncodedPercent;
 		try {
 			urlEncodedPlus 		= URLEncoder.encode("+", ENCODING);
 			urlEncodedMinus 	= URLEncoder.encode("-", ENCODING);
 			urlEncodedPercent 	= URLEncoder.encode("%", ENCODING);
 		} catch (UnsupportedEncodingException e) {
 			urlEncodedPlus 		= "";
 			urlEncodedMinus		= "";
 			urlEncodedPercent 	= "";
 			Log.e(TAG, "Unsupported encoding: " + ENCODING);
 			e.printStackTrace();
 		}
 
 		URLENCODED_PLUS		= urlEncodedPlus;
 		URLENCODED_MINUS	= urlEncodedMinus;
 		URLENCODED_PERCENT	= urlEncodedPercent;
 	}
 	
 	private static final String VLC_COMMAND_URI = "/requests/status.xml?command=";
 	private static final String VLC_PLAYLIST_URI = "/requests/playlist.xml";
 
 	private static final String CMD_PLAY_PAUSE = "pl_pause";
 	private static final String CMD_STOP = "pl_stop";
 	private static final String CMD_VOLUME = "volume&val=";
 	private static final String CMD_TOGGLE_FULLSCREEN = "fullscreen";
 	
 	protected HttpClient httpClient = new DefaultHttpClient();
 	private String vlcHostIp;
 	private String vlcHttpPort;
 	private String vlcBaseUrl;
 	private String vlcCommandBaseUrl;
 	private String vlcPlaylistUrl;
 	
 	public VlcHttpController(String ip, String port) {
 		this.setHost(ip, port);
 	}
 	
 	public void setHost(String ip, String port) {
 		this.vlcHostIp = ip;
 		this.vlcHttpPort = port;
 		
 		this.vlcBaseUrl = "http://" + this.vlcHostIp + ":" + this.vlcHttpPort;
 		try { // Checking the host & port validity:
 			URI uri = new URI(this.vlcBaseUrl);
 			if (uri.getHost() == null) {
 				throw new URISyntaxException(this.vlcHostIp, "Invalid host");
 			} else if (uri.getPort() == -1) {
 				throw new URISyntaxException(this.vlcHttpPort, "Invalid port");
 			}
 		} catch (URISyntaxException e) {
 			Log.e(TAG, "VLC URI creation failed");
 			e.printStackTrace();
 		}
 		
 		this.vlcCommandBaseUrl = this.vlcBaseUrl + VLC_COMMAND_URI;
 		this.vlcPlaylistUrl = this.vlcBaseUrl + VLC_PLAYLIST_URI;
 	}
 	
 	public String getIp() {
 		return this.vlcHostIp;
 	}
 	public void setIp(String ip) {
 		this.setHost(ip, this.vlcHttpPort);
 	}
 
 	public String getPort() {
 		return this.vlcHttpPort;
 	}
 	public void setPort(String port) {
 		this.setHost(this.vlcHostIp, port);
 	}
 
 	private URI createCommandURI(String name) {
 		try {
 			return new URI(this.createVlcCommandURL(name));
 		} catch (URISyntaxException e) {
 			Log.e(TAG, e.getMessage());
 			return null;
 		}
 	}
 	
 	private VlcHttpCommand createCommand(String name, VlcHttpCommandResponseHandler responseHandler) {
 		return new VlcHttpCommand(this, name, this.createCommandURI(name), responseHandler);
 	}
 	
 	public VlcHttpCommand createPlayPauseCommand(VlcHttpCommandResponseHandler responseHandler) {
 		return this.createCommand(CMD_PLAY_PAUSE, responseHandler);
 	}
 
 	// Nested command since a restart is a stop + start command
 	public VlcHttpCommand createRestartCommand(final VlcHttpCommandResponseHandler responseHandler) {
 		return this.createCommand(CMD_STOP, new VlcHttpCommandResponseHandler() {
 			@Override
 			public void handleResponse(VlcHttpController controller, VlcHttpResponse response) {
 				Log.d(TAG, "[" + String.valueOf(response.getStatusCode()) + "] " + response.getStatusText());
 				if (response.getStatusCode() != 200) {
 					responseHandler.handleResponse(controller, response);
 				} else {
 					controller.createCommand(CMD_PLAY_PAUSE, responseHandler).execute();
 				}
 			}
 		});
 	}
 
 	public VlcHttpCommand createStopCommand(VlcHttpCommandResponseHandler responseHandler) {
 		return this.createCommand(CMD_STOP, responseHandler);
 	}
 	
 	public VlcHttpCommand createIncreaseVolumeCommand(VlcHttpCommandResponseHandler responseHandler) {
 		return this.createCommand(CMD_VOLUME + URLENCODED_PLUS + String.valueOf(VOLUME_STEP), responseHandler);
 	}
 	
 	public VlcHttpCommand createDecreaseVolumeCommand(VlcHttpCommandResponseHandler responseHandler) {
 		return this.createCommand(CMD_VOLUME + URLENCODED_MINUS + String.valueOf(VOLUME_STEP), responseHandler);
 	}
 	
 	public VlcHttpCommand createMuteCommand(VlcHttpCommandResponseHandler responseHandler) {
 		return this.createCommand(CMD_VOLUME + "0", responseHandler);
 	}
 	
 	public VlcHttpCommand createResetVolumeCommand(VlcHttpCommandResponseHandler responseHandler) {
		return this.createCommand(CMD_VOLUME + "256", responseHandler);
 	}
 	
 	public VlcHttpCommand createToggleFullscreenCommand(VlcHttpCommandResponseHandler responseHandler) {
 		return this.createCommand(CMD_TOGGLE_FULLSCREEN, responseHandler);
 	}
 	
 	// TODO: Other commands
 
 	// debug:
 	public void TEST() throws URISyntaxException {
 		new VlcHttpCommand(this, "TEST PLAYLIST", new URI(this.vlcPlaylistUrl), new VlcHttpCommandResponseHandler() {
 			@Override
 			public void handleResponse(VlcHttpController controller, VlcHttpResponse response) {
 				Log.d("TEST", response.getStatusText());
 			}
 		}).execute();
 	}
 
 	public String createVlcCommandURL(String command) {
 		return this.vlcCommandBaseUrl + command + "&id=8";// &id=8 debug purpose
 	}
 }

 package de.zigapeda.flowspring.data;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 
 import de.zigapeda.flowspring.interfaces.TreeRow;
 
 public class YoutubeVideo implements TreeRow {
 	private final static String[] QUALITYORDER = new String[] { "37", "46",
 			"22", "45", // HD
 			"35", "44", "18", "34", "43", // SD
 			"36", "5", "17" // LD
 	};
 
 	public final static int HD = 0;
 	public final static int SD = 4;
 	public final static int LD = 9;
 
	private int quality = 0; // 0 for HD or lower, 4 for SD or lower, 9 for LD
 
 	private String name;
 	private String url;
 	private String videoUrl;
 	private Integer time;
 
 	public YoutubeVideo(String name, String url, String time) {
		this.name = name.replaceAll("&amp;", "&").replaceAll("&#39;", "'").replaceAll("&quot;", "\"").replaceAll("&lt;", "<").replaceAll("&gt;", ">");
 		this.url = url;
 		this.time = 0;
 		int multiply = 1;
 		int pos = 0;
 		while((pos = time.lastIndexOf(":")) > -1) {
 			this.time += Integer.valueOf(time.substring(pos + 1)) * multiply;
 			time = time.substring(0, pos);
 			multiply = multiply * 60;
 		}
 		this.time += Integer.valueOf(time) * multiply;
 		this.videoUrl = null;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getVideoUrl() {
 		if (videoUrl == null) {
 			this.parseUrl();
 		}
 		return videoUrl;
 	}
 
 	public void setQuality(int q) {
 		this.quality = q;
 	}
 
 	private void parseUrl() {
 		if (!this.url.startsWith("http://")) {
 			if (this.url.length() == "ophDZlhbXIo".length()) {
 				this.url = "http://www.youtube.com/watch?v=" + this.url;
 			} else {
 				this.url = "http://" + this.url;
 			}
 		}
 		try {
 			URL u = new URL(this.url);
 			InputStream is = u.openConnection().getInputStream();
 			BufferedReader br = new BufferedReader(new InputStreamReader(is));
 			String line = "";
 			while ((line = br.readLine()) != null) {
 				try {
 					if (line.matches("(.*)\"url_encoded_fmt_stream_map\":(.*)")) {
 
 						HashMap<String, String> videoUrls = new HashMap<String, String>();
 						line = line
 								.replaceFirst(
 										".*\"url_encoded_fmt_stream_map\": \"",
 										"").replaceFirst("\".*", "")
 								.replace("%25", "%").replace("\\u0026", "&")
 								.replace("\\", "");
 						String[] ytUrls = line.split(",");
 						for (String urlString : ytUrls) {
 							String[] ytUrl = urlString.split("&url=");
 							ytUrl[0] = ytUrl[0].substring(5);
 							ytUrl[1] = ytUrl[1].replaceFirst("%3A", ":")
 									.replaceAll("%3F", "?")
 									.replaceAll("%2F", "/")
 									.replaceAll("%3D", "=")
 									.replaceAll("%26", "&")
 									.replaceAll("\\u0026", "&")
 									.replaceAll("%252C", "%2C")
 									.replaceAll("sig=", "signature=");
 							videoUrls.put(ytUrl[0], ytUrl[1]);
 						}
 						for (int i = quality; i < QUALITYORDER.length; i++) {
 							if (videoUrls.containsKey(QUALITYORDER[i])) {
 								this.videoUrl = videoUrls.get(QUALITYORDER[i]);
 								break;
 							}
 						}
 						if (this.videoUrl == null) {
 							this.videoUrl = (String) videoUrls.values()
 									.toArray()[0];
 						}
 					}
 				} catch (NullPointerException npe) {
 				}
 			}
 			is.close();
 		} catch (MalformedURLException e1) {
 		} catch (IOException e1) {
 		}
 	}
 	
 	public int getId() {
 		return 0;
 	}
 	
 	public Integer getInt() {
 		return this.time;
 	}
 
 	public String getArtist() {
 		return null;
 	}
 
 	public String getAlbum() {
 		return null;
 	}
 
 	public String getGenre() {
 		return null;
 	}
 
 	public String getTrack() {
 		return null;
 	}
 
 	public String getYear() {
 		return null;
 	}
 
 	public Integer getDuration() {
 		return this.time;
 	}
 
 	public String getComment() {
 		return null;
 	}
 
 	public String getRating() {
 		return null;
 	}
 
 	public String getPlaycount() {
 		return null;
 	}
 
 	public Integer getType() {
 		return TreeRow.YoutubeVideo;
 	}
 
 }

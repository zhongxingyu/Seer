 import java.io.* ;
 import java.net.* ;
 import java.util.* ;
 import java.math.BigInteger;
 public final class Manager {
 	private static Map<String, Object> status = new HashMap<String, Object>();
 	private static LinkedList<Track> playlist = new LinkedList<Track>();
 	private final static String[] noupdates = { "now", "next", "isPlaying" };
 	private static Thread currentFetcherThread = null;
 	private static Properties settings = new Properties();
 	private static Hosts hosts;
 	public static void main(String argv[]) throws Exception {
 		
		try {
			settings.load(Manager.class.getClassLoader().getResourceAsStream("config.properties"));
		} catch (NullPointerException e) {
			System.err.println("No config file found");
			System.exit(4);
		}
 
 		if (argv.length < 1) {
 			System.err.println("No port specified (must be 1st argument)");
 			System.exit(1);
 		}
 		
 		// Set the port number.
 		int port;
 		try{
 			port = Integer.parseInt(argv[0]);
 		} catch (NumberFormatException e) {
 			System.err.println("Port must be a number");
 			System.exit(2);
 			return;
 		}
 		if (argv.length < 2) {
 			System.err.println("No domain specified for lucos_services instance (must be 2nd argument)");
 			System.exit(3);
 		}
 
 		hosts = new Hosts(argv[1]);
 		
 		status.put("isPlaying", true);
 		status.put("volume", 0.5);
 		status.put("openurl", null);
 		next();
 		
 		// Start a thread to sync with Vlc
 		VlcSync vlcsync = new VlcSync();
 		Thread vlcsyncThread = new Thread(vlcsync);
 		vlcsyncThread.start();
 		
 		
 		// Establish the listen socket.
 		ServerSocket serverSocket = new ServerSocket(port);
 		System.out.println("outgoing data server ready");
     
 		// Process HTTP service requests in an infinite loop.
 		while (true) {
 			// Listen for a TCP connection request.
 
 			Socket clientSocket = serverSocket.accept();
 			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
 			// Construct an object to process the HTTP request message.
 			HttpRequest request = new HttpRequest( clientSocket );
 			// Create a new thread to process the request.
 			Thread thread = new Thread(request);
 			// Start the thread.
 			thread.start();
 
 		}
 	
 	}
 	public static String getSetting(String key) {
 		return settings.getProperty(key);
 	}
 	public static String getSetting(String key, String defaultValue) {
 		return settings.getProperty(key, defaultValue);
 	}
 	public static String getHost(String key) {
 		return hosts.get(key);
 	}
 	public static boolean TogglePlayPause() {
 		setPlaying(!getPlaying());
 		return getPlaying();
 	}
 	public static void setPlaying(boolean isPlaying) {
 		boolean wasPlaying = getPlaying();
 		VlcSync.pausedForVlc = false;
 		status.put("isPlaying", isPlaying);
 		
 		// If unpausing, then update timeset in the current track, so nothing is missed
 		if (isPlaying && !wasPlaying) {
 			((Track)status.get("now")).timeSetNow();
 		}
 	}
 	public static boolean getPlaying() {
 		return (Boolean)status.get("isPlaying");
 	}
 	public static void setVolume(float volume) {
 		if (volume > 1) volume = 1;
 		if (volume < 0) volume = 0;
 		status.put("volume", volume);
 	}
 	public static boolean hasChanged(int oldhashcode) {
 		return (status.hashCode() != oldhashcode);
 	}
 	public static boolean playlistHasChanged(int oldhashcode) {
 		return (playlist.hashCode() != oldhashcode);
 	}
 	public static Map<String, Object> getStatus() {
 		Map<String, Object> output = new HashMap<String, Object>(status);
 		output.put("hashcode", status.hashCode());
 		return output;
 	}
 	public static Map<String, Object> getPlaylist() {
 		Map<String, Object> output = new HashMap<String, Object>();
 		output.put("playlist", playlist);
 		output.put("hashcode", playlist.hashCode());
 		return output;
 	}
 	public static void update(Map<String, String> changes) {
 		
 		// Remove any keys which shouldn't be overwritten
 		for (String key : noupdates) changes.remove(key);
 		status.putAll(changes);
 	}
 	public static void queue(Track track) {
 		playlist.add(track);
 		updateNowNext();
 	}
 	public static void queue(Track track, int index) {
 		if (index == -1) {
 			Track now = (Track)status.get("now");
 			if (!now.equals(new NullTrack())) playlist.addFirst(now);
 			status.put("now", track);
 		} else playlist.add(index, track);
 		updateNowNext();
 	}
 	public static void queuem3u(BufferedReader br) throws IOException {
 		queuem3u(br, "");
 	}
 	public static void queuem3u(BufferedReader br, String prefix) throws IOException {
 		String line;
 		while ((line = br.readLine()) != null) {
 			line = line.trim();
 			if (line.charAt(0) == '#' || line.indexOf('/') == -1) continue;
 			queue(new Track(prefix+line));
 		}
 	}
 	public static void next() {
 		status.put("now", new NullTrack());
 		status.put("currentTime", 0);
 		updateNowNext();
 		if (playlist.size() < 10) fetchTracks();
 	}
 	private static void updateNowNext() {
 		if (status.get("now").equals(new NullTrack()) && playlist.size() > 0) {
 			status.put("now", playlist.removeFirst());
 			((Track)status.get("now")).timeSetNow();
 		}
 		if (playlist.size() > 0) {
 			status.put("next", playlist.getFirst());
 		} else {
 			status.put("next", new NullTrack());
 		}
 	}
 	private static void fetchTracks() {
 		if (currentFetcherThread != null && currentFetcherThread.isAlive()) return;
 		TrackFetcher fetcher = new TrackFetcher();
 		
 		// Create a new thread to process the request.
 		currentFetcherThread = new Thread(fetcher);
 		
 		// Start the thread.
 		currentFetcherThread.start();
 	}
 	private static void openUrl(String type, String url) {
 		Map<String, String> openUrl = new HashMap<String, String>();
 		openUrl.put("type", type);
 		openUrl.put("url", url);
 		status.put("open", openUrl);
 		try {
 			Thread.sleep(5000);
 		} catch (InterruptedException e) {
 			System.err.println("Thread Error:");
 			e.printStackTrace(System.err);
 		}
 		if (status.get("open").hashCode() == openUrl.hashCode()) status.put("open", null);
 	}
 	public static boolean openExtUrl() {
 			Track now = (Track)status.get("now");
 			if (now.equals(new NullTrack())) return false;
 			String exturl = now.getExtUrl();
 			if (exturl == null) return false;
 			openUrl("ext", exturl);
 			return true;
 	}
 	public static boolean openEditUrl() {
 			Track now = (Track)status.get("now");
 			if (now.equals(new NullTrack())) return false;
 			String editurl = now.getEditUrl();
 			if (editurl == null) return false;
 			openUrl("edit", editurl);
 			return true;
 	}
 	public static void finished(Track oldtrack, String trackstatus) {
 		
 		if (!oldtrack.equals(status.get("now"))) {
 			System.err.println("Bad Finish Request: "+oldtrack.getUrl()+", "+((Track)status.get("now")).getUrl());
 			return;
 		}
 		
 		// TODO: save the time finished and status.
 		
 		next();
 	}
 	public static boolean update(Track curtrack, float currentTime, BigInteger currentTimeSet) {
 		if (curtrack.equals(status.get("now"))) {
 			Track now = (Track)status.get("now");
 			now.setTime(currentTime, currentTimeSet);
 			return true;
 		}
 		Iterator iter = playlist.iterator();
 		while(iter.hasNext()) {
 			Track track = (Track)iter.next();
 			if (curtrack.equals(track)) track.setTime(currentTime, currentTimeSet);
 			return true;
 		}
 		return false;
 	}
 	public static boolean isCurrentURL(String url) {
 		return (((Track)status.get("now")).getUrl().equals(url));
 	}
 }

 package com.sandwich.client;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.ConnectException;
 import java.net.HttpURLConnection;
 import java.net.Inet6Address;
 import java.net.Inet4Address;
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Random;
 import java.util.Scanner;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.fasterxml.jackson.core.JsonFactory;
 import com.fasterxml.jackson.core.JsonParser;
 import com.fasterxml.jackson.core.JsonToken;
 import com.sandwich.player.MediaMimeInfo;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.DownloadManager;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteStatement;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Environment;
 
 public class Client {
 	private Context context;
 	PeerSet peers;
 	
 	private ArrayList<Thread> searchThreads;
 	private HashMap<PeerSet.Peer, Thread> indexDownloadThreads;
 	
 	SQLiteDatabase database;
 	private HashMap<PeerSet.Peer, SQLiteDatabase> peerDatabases;
 	
 	private static final String CREATE_PEER_TABLE = "CREATE TABLE IF NOT EXISTS peers (IP TEXT PRIMARY KEY, IndexHash INT, LastSeen TEXT);";
 
 	static final String PEER_TABLE = "peers";
 	static final String PEER_DB = "peers.db";
 	
 	public Client(Context context)
 	{
 		this.context = context;
 		this.searchThreads = new ArrayList<Thread>();
 		this.indexDownloadThreads = new HashMap<PeerSet.Peer, Thread>();
 		this.peerDatabases = new HashMap<PeerSet.Peer, SQLiteDatabase>();
 		this.peers = new PeerSet();
 	}
 	
 	// Holy fucking shit
 	private static int unsign(byte signed)
 	{
 		if (signed >= 0)
 			return signed;
 		else
 			return signed+256;
 	}
 	
 	private boolean isNetworkActive()
 	{
 		ConnectivityManager mgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo active = mgr.getActiveNetworkInfo();
 
 		return (active != null && active.isConnected());
 	}
 	
 	public static int getPortNumberFromIPAddress(InetAddress address) throws NoSuchAlgorithmException
 	{
 		MessageDigest md = MessageDigest.getInstance("MD5");
 		byte[] hash;
 		byte[] addr;
 		int port;
 		
 		// We need an IPv6 address in all cases
 		if (address instanceof Inet4Address)
 		{
 			// If it's an IPv4 address, we need to add 2 octets of 255
 			addr = new byte[address.getAddress().length+12];
 			
 			// Copy the IPv4 portion over at the 12th octet
 			System.arraycopy(address.getAddress(), 0, addr, 12, address.getAddress().length);
 			
 			// Copy the FF octets from the 10th octet
 			addr[10] = (byte) 0xFF;
 			addr[11] = (byte) 0xFF;
 		}
 		else if (address instanceof Inet6Address)
 		{
 			// It's fine the way it is
 			addr = address.getAddress();
 		}
 		else
 		{
 			throw new IllegalArgumentException("Not an IPv4 or IPv6 address");
 		}
 		
 		// Compute the MD5 of the address in byte form
 		hash = md.digest(addr);
 		
 		// Create the port number by bit-shifting the hash
 		port = (unsign(hash[0]) + unsign(hash[3])) << 8;
 		port += unsign(hash[1]) + unsign(hash[2]);
 		port &= 0xFFFF;
 		
 		// Keep the port number of out the sub-1024 range
 		if (port < 1024)
 			port += 1024;
 		
 		return port;
 	}
 	
 	static HttpURLConnection createHttpConnection(URL url, boolean useCaches) throws IOException
 	{
 		HttpURLConnection conn;
 		
 		System.out.println("Connecting to "+url.toExternalForm());
 		conn = (HttpURLConnection) url.openConnection();
 		
 		// Set cache options
 		conn.setUseCaches(useCaches);
 		
 		// Set timeouts of 5 seconds
 		conn.setConnectTimeout(5000);
 		conn.setReadTimeout(5000);
 		
 		return conn;
 	}
 	
 	private SQLiteDatabase getPeerDatabase(PeerSet.Peer peer)
 	{
 		synchronized (peerDatabases) {
 			if (peerDatabases.containsKey(peer))
 				return peerDatabases.get(peer);
 			
 			SQLiteDatabase db = context.openOrCreateDatabase(peer.getIpAddress(), Context.MODE_PRIVATE, null);
 			peerDatabases.put(peer, db);
 			return db;
 		}
 	}
 	
 	private static int sendGetRequest(HttpURLConnection conn) throws IOException
 	{		
 		// Send the get request
 		conn.setRequestMethod("GET");
 
 		// Wait for the response
 		return conn.getResponseCode();
 	}
 	
 	static String createPeerUrlString(String peer, String path, String parameters) throws URISyntaxException, UnknownHostException, NoSuchAlgorithmException
 	{
 		return new URI("http", null, peer, getPortNumberFromIPAddress(Inet6Address.getByName(peer)), path, parameters, null).toString();
 	}
 	
 	static URL createQueryUrl(URL peerUrl, String querySuffix) throws MalformedURLException
 	{
 		String urlString = peerUrl.toExternalForm();
 		
 		// If it doesn't end with a slash, normalize it by adding a slash
 		if (!urlString.endsWith("/"))
 			urlString += "/";
 		
 		// Now append the query suffix
 		urlString += querySuffix;
 		
 		return new URL(urlString);
 	}
 	
 	static InputStream getInputStreamFromConnection(HttpURLConnection conn) throws IOException
 	{
 		int responseCode;
 
 		// Send the GET request and get the response code
 		responseCode = sendGetRequest(conn);
 		if (responseCode != HttpURLConnection.HTTP_OK)
 		{
 			throw new ConnectException("Failed to get peer list from bootstrap peer");
 		}
 		
 		return conn.getInputStream();
 	}
 	
 	private PeerSet getPeerList(URL bootstrapUrl) throws IOException, JSONException
 	{
 		URL queryUrl;
 		Scanner in;
 		PeerSet peerSet;
 		JSONArray jsonPeerList;
 		HttpURLConnection conn;
 		String json;
 		
 		// Build the query peer list URL
 		queryUrl = createQueryUrl(bootstrapUrl, "peerlist");
 		System.out.println("Getting peerlist from "+queryUrl.toExternalForm());
 		
 		// Create a URL connection to the peer
 		conn = Client.createHttpConnection(queryUrl, false);
 		
 		// Get an input stream from the GET request on this URL
 		in = new Scanner(getInputStreamFromConnection(conn));
 		
 		// Read the JSON response
 		json = "";
 		while (in.hasNext())
 		{
 			json += in.next();
 		}
 		
 		// Close the scanner and the connection
 		in.close();
 		conn.disconnect();
 		
 		jsonPeerList = new JSONArray(json);
 		peerSet = new PeerSet();
 		for (int i = 0; i < jsonPeerList.length(); i++)
 		{
 			JSONObject jsonPeer = jsonPeerList.getJSONObject(i);
 
 			peerSet.updatePeer(jsonPeer.getString("IP"), jsonPeer.getString("LastSeen"), jsonPeer.getLong("IndexHash"));
 		}
 		
 		return peerSet;
 	}
 	
 	private Thread startDownloadIndexThreadForPeer(PeerSet.Peer peer)
 	{
 		Thread t = new IndexDownloadThread(this, getPeerDatabase(peer), peer);
 		t.start();
 		return t;
 	}
 	
 	private PeerSet getPeerSetFromDatabase(SQLiteDatabase database) throws SQLiteException
 	{
 		PeerSet peers = new PeerSet();
 		Cursor c = database.query(PEER_TABLE, new String[] {"IP", "IndexHash", "LastSeen"}, null, null, null, null, null, null);
 		
 		c.moveToFirst();
 		while (!c.isAfterLast())
 		{
 			peers.updatePeer(c.getString(0), c.getString(2), c.getLong(1));
 			c.moveToNext();
 		}
 		
 		c.close();
 		
 		return peers;
 	}
 	
 	boolean deletePeerFromDatabase(PeerSet.Peer peer) throws SQLiteException
 	{
 		boolean ret = false;
 		
 		// Delete them from the peer table first
 		try {
 			ret = database.delete(PEER_TABLE, "IP=?", new String[] {peer.getIpAddress()}) > 0;
 		} catch (SQLiteException e) {
 			// It's ok for this to fail
 		}
 		
 		// Delete their index database
 		try {
 			getPeerDatabase(peer).execSQL("DROP TABLE IF EXISTS"+getTableNameForPeer(peer));
 		} catch (SQLiteException e) {
 			// It's ok for this to fail
 		}
 		
 		return ret;
 	}
 	
 	static String getTableNameForPeer(PeerSet.Peer peer)
 	{
 		return '[' + peer.getIpAddress() + ']';
 	}
 	
 	long getOldHashOfPeerIndex(PeerSet.Peer peer)
 	{
 		Cursor c = database.query(PEER_TABLE, new String[] {"IP", "IndexHash"}, null, null, null, null, null, null);
 		
 		c.moveToFirst();
 		while (!c.isAfterLast())
 		{
 			if (c.getString(0).equals(peer.getIpAddress()))
 			{
 				String hash = c.getString(1);
 
 				c.close();
 				return Long.parseLong(hash, 10);
 			}
 
 			c.moveToNext();
 		}
 
 		c.close();
 		return -1;
 	}
 	
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	public void initialize()
 	{
 		// Open or create it
 		database = context.openOrCreateDatabase(PEER_DB, Context.MODE_PRIVATE, null);
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
 		{
 			// Enable write-ahead logging if possible
 			database.enableWriteAheadLogging();
 		}
 		
 		// Create our table
 		database.execSQL(CREATE_PEER_TABLE);
 		
 		// Setup the database to be fast
 		database.execSQL("PRAGMA synchronous=OFF");
 		database.execSQL("PRAGMA count_changes=OFF");
 		database.execSQL("PRAGMA temp_store=MEMORY");
 	}
 	
 	public void release()
 	{
 		// Start killing of search threads
 		endSearch();
 		
 		try {
 			// Stop bootstrapping
 			stopBootstrap();
 			
 			// Wait for the search threads to die
 			for (Thread t : searchThreads)
 			{
 				t.join();
 			}
 		} catch (InterruptedException e) { }
 		
 		// Close all the peer databases
 		synchronized (peerDatabases) {
 			for (SQLiteDatabase db : peerDatabases.values())
 				db.close();
 		}
 		
 		// Close the peer list db
 		if (database != null)
 			database.close();
 	}
 	
 	private boolean readCachedBootstrapData()
 	{
 		boolean success;
 
 		System.out.println("Loading cached bootstrap data");
 		
 		try {
 			// Try to read the peer set from the database
 			peers.updatePeerSet(getPeerSetFromDatabase(database));
 			success = peers.getPeerListLength() != 0;
 		} catch (SQLiteException e) {
 			// Failed to read peer set
 			success = false;
 		}
 
 		return success;
 	}
 	
 	public boolean bootstrapFromCache()
 	{
 		// Just read the bootstrap data in from the database
 		return readCachedBootstrapData();
 	}
 	
 	private void downloadPeerList(String initialPeer) throws NoSuchAlgorithmException, URISyntaxException, IOException, JSONException
 	{
 		URL bootstrapUrl;
 		Iterator<PeerSet.Peer> iterator;
 		Random rand = new Random();
 		
 		System.out.println("Downloading the peer list");
 		
 		// Bootstrap from a random peer
 		rand.setSeed(System.currentTimeMillis());
 		while (peers.getPeerListLength() > 0)
 		{
 			// Generate a random peer to bootstrap from
 			int nextPeer = rand.nextInt(peers.getPeerListLength());
 			System.out.println("Bootstrapping from peer index: "+nextPeer);
 			
 			// Get the peer object for the index
 			iterator = peers.getPeerListIterator();
 			PeerSet.Peer selectedPeer = null;
 			for (int i = 0; iterator.hasNext(); i++)
 			{
 				PeerSet.Peer currentPeer = iterator.next();
 				
 				if (i == nextPeer)
 				{
 					selectedPeer = currentPeer;
 					break;
 				}
 			}
 			
 			// Make sure it worked
 			if (selectedPeer == null)
 			{
 				break;
 			}
 				
 			try {
 				// Resolve address and create a URL
 				bootstrapUrl = new URL(createPeerUrlString(selectedPeer.getIpAddress(), null, null));
 				
 				// Download the peer list
 				peers.updatePeerSet(getPeerList(bootstrapUrl));
 				
 				// If we get here, bootstrapping was successful
 				return;
 			} catch (Exception e) {
 				// Make sure the network is available
 				if (!isNetworkActive())
 				{
 					throw new IllegalStateException("No network connection available");
 				}
 				
 				// Failed to connect to this one, so prune it
 				selectedPeer.remove();
 				
 				// Drop it from the database
 				deletePeerFromDatabase(selectedPeer);
 			}
 		}
 
 		// Try the initial peer if all else fails
 		try {
 			System.out.println("Trying initial peer: "+initialPeer);
 			
 			// Resolve address and create a URL
 			bootstrapUrl = new URL(createPeerUrlString(initialPeer, null, null));
 			
 			// Download the peer list
 			peers.updatePeerSet(getPeerList(bootstrapUrl));
 			
 			// It worked
 			return;
 		} catch (Exception e) {
 			// Failed to connect to the initial peer, so let's try another one
 		}
 		
 		// We're out of people to bootstrap from
 		throw new IllegalStateException("No peers online");
 	}
 	
 	public void stopBootstrap() throws InterruptedException
 	{
 		synchronized (indexDownloadThreads) {
 			// Interrupt threads
 			for (Thread t : indexDownloadThreads.values())
 			{
 				t.interrupt();
 			}
 			
 			// Wait for them to die
 			for (Thread t : indexDownloadThreads.values())
 			{
 				t.join();
 			}
 		}
 	}
 	
 	public String getUriForResult(String peer, ResultListener.Result result) throws UnknownHostException, NoSuchAlgorithmException, URISyntaxException
 	{
 		return createPeerUrlString(peer, "/file", "path="+result.result);
 	}
 	
 	public boolean isResultStreamable(ResultListener.Result result)
 	{
 		String mimeType;
 
 		mimeType = MediaMimeInfo.getMimeTypeForPath(result.result);
 		if (mimeType == null)
 		{
 			// Undetermined MIME type
 			return false;
 		}
 		else if (mimeType.startsWith("audio/"))
 		{
 			// Streamable via the audio player
 			return true;
 		}
 		else if (mimeType.startsWith("video/"))
 		{
 			// Streamable via the video player
 			return true;
 		}
 		else
 		{
 			// No player for this
 			return false;
 		}
 	}
 	
 	public void bootstrapFromNetwork(String initialPeer) throws IOException, JSONException, InterruptedException, NoSuchAlgorithmException, URISyntaxException
 	{
 		Iterator<PeerSet.Peer> iterator;
 
 		// Download the peer list
 		downloadPeerList(initialPeer);
 		
 		// Reap threads that aren't still downloading
 		ArrayList<PeerSet.Peer> reapList = new ArrayList<PeerSet.Peer>();
 		synchronized (indexDownloadThreads) {
 			// Add dead threads to the reap list
 			for (Map.Entry<PeerSet.Peer, Thread> entry : indexDownloadThreads.entrySet())
 			{
 				if (entry.getValue().getState() == Thread.State.TERMINATED)
 					reapList.add(entry.getKey());
 			}
 			
 			// Reap dead threads
 			for (PeerSet.Peer p : reapList)
 			{
 				indexDownloadThreads.remove(p);
 			}
 		}
 		
 		// We have to do this in a synchronized block so our index download threads
 		// can't touch it while we're downloading
 		synchronized (peers) {
 			// Iterate the peer list and download indexes for each
 			iterator = peers.getPeerListIterator();
 			while (iterator.hasNext())
 			{
 				PeerSet.Peer peer = iterator.next();
 				long oldHash;
 				
 				synchronized (indexDownloadThreads) {
 					// Check if an updater is already running for this peer
 					if (!indexDownloadThreads.containsKey(peer))
 					{
 						// Check if the old index hash is valid
 						oldHash = getOldHashOfPeerIndex(peer);
 						if (oldHash == peer.getIndexHash())
 						{
 							System.out.println(peer.getIpAddress()+" index is up to date (hash: "+peer.getIndexHash()+")");
 							
 							// Don't download anything
 							continue;
 						}
 						
 						// No updater running and index is not up to date
 						indexDownloadThreads.put(peer, startDownloadIndexThreadForPeer(peer));
 					}
 				}
 			}
 		}
 	}
 	
 	public void endSearch()
 	{
 		ArrayList<Thread> reapList = new ArrayList<Thread>();
 
 		synchronized (searchThreads) {	
 			// Interrupt existing search threads
 			for (Thread t : searchThreads)
 			{
 				if (t.getState() == Thread.State.TERMINATED)
 					reapList.add(t);
 				else
 					t.interrupt();
 			}
 			
 			// Reap dead threads
 			searchThreads.removeAll(reapList);
 		}
 	}
 	
 	public void beginSearch(String query, ResultListener listener) throws IOException
 	{		
 		if (peers == null)
 		{
 			throw new IllegalStateException("Not bootstrapped");
 		}
 		
 		// Make sure the peer list isn't modified by the search threads while we're iterating
 		synchronized (peers) {
 			// Start search threads for each peer
 			Iterator<PeerSet.Peer> peerIterator = peers.getPeerListIterator();
 			while (peerIterator.hasNext())
 			{
 				PeerSet.Peer peer = peerIterator.next();
 
 				// Start the search thread
 				System.out.println("Spawning thread to search "+peer.getIpAddress());
 				Thread t = new SearchThread(this, getPeerDatabase(peer), peer, listener, query);
 				synchronized (searchThreads) {
 					searchThreads.add(t);
 				}
 				t.start();
 			}
 		}
 	}
 
 	@TargetApi(11)
 	public void startFileDownloadFromPeer(String peer, String file) throws URISyntaxException, UnknownHostException, NoSuchAlgorithmException
 	{
 		DownloadManager downloader = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
 		DownloadManager.Request request;
 		String title;
 		String url;
 		
 		// Make a title from the last component of the file path
 		title = file;
 		if (title.lastIndexOf("/") > 0)
 			title = title.substring(title.lastIndexOf("/")+1);
 
 		url = createPeerUrlString(peer, "/file", "path="+file);
 		
 		System.out.println("Downloading URL: "+url+" ("+title+")");
 
 		request = new DownloadManager.Request(Uri.parse(url));
 		
 		// Download to the external downloads folder
 		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title);
 		
 		// DownloadManager was enhanced with Honeycomb with useful features we want to activate
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
 		{
 			// Allow the media scanner to pick this file up
 			request.allowScanningByMediaScanner();
 		
 			// Continue showing the notification even after the download finishes
 			request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
 		}
 		
 		// Give it our title
 		request.setTitle(title);
 		
 		// Fire the download
 		downloader.enqueue(request);
 	}
 	
 	public boolean startFileStreamFromPeer(Activity activity, String peer, String file) throws NoSuchAlgorithmException, URISyntaxException, IOException
 	{
 		String url;
 		String mimeType;
 
 		mimeType = MediaMimeInfo.getMimeTypeForPath(file);
 		url = createPeerUrlString(peer, "/file", "path="+file);
 		if (mimeType == null)
 		{
 			// Undetermined MIME type
 			return false;
 		}
 		else if (mimeType.startsWith("audio/"))
 		{
 			// Create the audio player activity
 			Intent i = new Intent(activity, com.sandwich.AudioPlayer.class);
 			i.putExtra("URL", url);
 			activity.startActivity(i);
 		}
 		else if (mimeType.startsWith("video/"))
 		{
 			// Create the video player activity
 			Intent i = new Intent(activity, com.sandwich.VideoPlayer.class);
 			i.putExtra("URL", url);
 			activity.startActivity(i);
 		}
 		else
 		{
 			// No player for this
 			return false;
 		}
 		
 		return true;
 	}
 }
 
 class IndexDownloadThread extends Thread {
 	SQLiteDatabase peerindex;
 	PeerSet.Peer peer;
 	Client client;
 	
 	public IndexDownloadThread(Client client, SQLiteDatabase peerindex, PeerSet.Peer peer)
 	{
 		this.client = client;
 		this.peerindex = peerindex;
 		this.peer = peer;
 	}
 
 	@Override
 	public void run() {
 		URL queryUrl;
 		HttpURLConnection conn;
 		InputStream in;
 		long indexHash;
 		String timeStamp;
 		boolean gotIndexHash;
 		
 		conn = null;
 		in = null;
 		timeStamp = null;
 		indexHash = 0;
 		gotIndexHash = false;
 		
 		// Drop the tables and rows for this peer
 		client.deletePeerFromDatabase(peer);
 		try {
 			// Build the query index URL
 			queryUrl = new URL(Client.createPeerUrlString(peer.getIpAddress(), "/indexfor", null));
 			
 			// Connect a URL connection
 			conn = Client.createHttpConnection(queryUrl, false);
 			
 			// Get an input stream from the GET request on this URL
 			in = Client.getInputStreamFromConnection(conn);
 			
 			// Create the required table
 			peerindex.execSQL("CREATE TABLE IF NOT EXISTS "+Client.getTableNameForPeer(peer)+" (FileName TEXT PRIMARY KEY, Size INTEGER, Checksum INTEGER);");
 			
 			// Compile the database insert so we don't have to do it each time
 			SQLiteStatement insertStmt = peerindex.compileStatement("INSERT OR REPLACE INTO "+Client.getTableNameForPeer(peer)+" VALUES (?1, ?2, ?3);");
 
 			// Read from the GET response
 			JsonFactory jfactory = new JsonFactory();
 			JsonParser parser = jfactory.createJsonParser(new BufferedInputStream(in));
 			
 			// Begin immediate transaction
 			peerindex.execSQL("BEGIN IMMEDIATE TRANSACTION");
 			try {
 				// Parse the index object
 				parser.nextToken();
 				while (parser.nextToken() != JsonToken.END_OBJECT)
 				{
 					String ioname = parser.getCurrentName();
 					
 					if (ioname.equals("IndexHash"))
 					{
 						parser.nextToken();
 						indexHash = parser.getLongValue();
 						gotIndexHash = true;
 					}
 					else if (ioname.equals("TimeStamp"))
 					{
 						parser.nextToken();
 						timeStamp = parser.getText();
 					}
 					else if (ioname.equals("List"))
 					{
 						parser.nextToken();
 						while (parser.nextToken() != JsonToken.END_ARRAY)
 						{							
 							while (parser.nextToken() != JsonToken.END_OBJECT)
 							{
 								String foname = parser.getCurrentName();
 								
 								if (foname.equals("FileName"))
 								{
 									parser.nextToken();
 									insertStmt.bindString(1, parser.getText());
 								}
 								else if (foname.equals("CheckSum"))
 								{
 									parser.nextToken();
 									insertStmt.bindLong(3, parser.getLongValue());
 								}
 								else if (foname.equals("Size"))
 								{
 									parser.nextToken();
 									insertStmt.bindLong(2, parser.getLongValue());
 								}
 								else
 								{
 									parser.nextToken();
 								}
 								
 								insertStmt.execute();
 								insertStmt.clearBindings();
 							}
 							
 							if (isInterrupted()) throw new InterruptedException();
 						}
 					}
 					else
 					{
 						parser.nextToken();
 					}
 				}
 			} finally {
 				// Commit transaction
 				peerindex.execSQL("END TRANSACTION");
 				
 				// Delete the precompiled statement
 				insertStmt.close();
 				
 				// Close the JSON parser
 				parser.close();
 			}
 			
 			// Check if we got the header properly
 			if (timeStamp == null || !gotIndexHash)
 				throw new IllegalStateException("No header found in JSON file index");
 			
 			// Create the values to be stored in the SQL database
 			ContentValues vals = new ContentValues();
 			vals.put("IP", peer.getIpAddress());
 			vals.put("IndexHash", indexHash);
 			vals.put("LastSeen", timeStamp);
 
 			// Update the peer entry in the database
 			client.database.insert(Client.PEER_TABLE, null, vals);
 			
 			// Update the peer table in memory
 			client.peers.updatePeer(peer.getIpAddress(), timeStamp, indexHash);
 			
 			System.out.println("Index for "+peer.getIpAddress()+" downloaded (hash: "+peer.getIndexHash()+")");
 		} catch (Exception e) {
 			System.err.println("Failed to download index for peer: "+peer.getIpAddress());
 			
 			// Remove this peer from the peer set
 			peer.remove();
 
 			// Remove them from the database
 			client.deletePeerFromDatabase(peer);
 		} finally {
 			// Cleanup the socket and reader
 			try {
 				if (in != null)
 					in.close();
 			} catch (IOException e) {}
 						
 			if (conn != null)
 				conn.disconnect();
 		}
 	}
 }
 
 class SearchThread extends Thread {
 	PeerSet.Peer peer;
 	SQLiteDatabase peerindex;
 	String query;
 	ResultListener listener;
 	Client client;
 	
 	public SearchThread(Client client, SQLiteDatabase peerindex, PeerSet.Peer peer, ResultListener listener, String query)
 	{
 		this.client = client;
 		this.peerindex = peerindex;
 		this.peer = peer;
 		this.query = query;
 		this.listener = listener;
 	}
 
 	@Override
 	public void run() {
 		try {
 			Cursor c = peerindex.query(Client.getTableNameForPeer(peer), new String[] {"FileName", "CheckSum"}, "FileName LIKE '%"+query+"%'" , null, null, null, null, null);
 			
 			// Iterate the cursor
 			c.moveToFirst();
 			while (!c.isAfterLast())
 			{
 				String file = c.getString(0);
 				int checksum = c.getInt(1);
 				if (!isInterrupted())
 					listener.foundResult(query, new ResultListener.Result(peer.getIpAddress(), file, checksum));
 				else
 				{
 					System.out.println("Search on "+peer.getIpAddress()+" was interrupted");
 					break;
 				}
 				c.moveToNext();
 			}
 			c.close();
 		} catch (SQLiteException e) {
 			System.err.println("Failed to search index for peer: "+peer.getIpAddress());
 			
 			// Remove this peer from the peer set
 			peer.remove();
 			
 			// Drop them from the database
 			client.deletePeerFromDatabase(peer);
 		}
 	}
 }

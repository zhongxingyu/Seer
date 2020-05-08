 package com.chess.genesis;
 
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.SocketException;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 final class SocketClient
 {
 	private static SocketClient instance = null;
 
 	private boolean isLoggedin;
 	private String loginHash;
 	private Socket socket;
 	private DataInputStream input;
 	private OutputStream output;
 
 	private SocketClient()
 	{
 		disconnect();
 	}
 
 	public static synchronized SocketClient getInstance()
 	{
 		if (instance == null)
 			instance = new SocketClient();
 		return instance;
 	}
 
 	public static synchronized SocketClient getInstance(final int id)
 	{
 		return new SocketClient();
 	}
 
 	public synchronized boolean getIsLoggedIn()
 	{
 		return isLoggedin;
 	}
 
 	public synchronized void setIsLoggedIn(final boolean value)
 	{
 		isLoggedin = value;
 	}
 
 	public synchronized String getHash() throws SocketException, IOException
 	{
 		if (loginHash == null)
 			connect();
 		return loginHash;
 	}
 
 	private synchronized void connect() throws SocketException, IOException
 	{
 		if (socket.isConnected())
 			return;
 		socket.connect(new InetSocketAddress("genesischess.com", 8338));
 		input = new DataInputStream(socket.getInputStream());
 		output = socket.getOutputStream();
 		loginHash = input.readLine().trim();
 	}
 
 	public synchronized void disconnect()
 	{
 	try {
 		if (socket != null)
 			socket.close();
 		socket = new Socket();
 		loginHash = null;
 		isLoggedin = false;
 	} catch (IOException e) {
 		e.printStackTrace();
 		throw new RuntimeException();
 	}
 	}
 
 	public synchronized void write(final JSONObject data) throws SocketException, IOException
 	{
 		connect();
 
 		final String str = data.toString() + "\n";
 
 		output.write(str.getBytes());
 	}
 
 	public synchronized JSONObject read() throws SocketException, IOException, JSONException
 	{
 		connect();
 
	try {
 		return (JSONObject) (new JSONTokener(input.readLine())).nextValue();
	} catch (NullPointerException e) {
		return new JSONObject("{\"result\":\"error\",\"reason\":\"connection lost\"}");
	}
 	}
 }

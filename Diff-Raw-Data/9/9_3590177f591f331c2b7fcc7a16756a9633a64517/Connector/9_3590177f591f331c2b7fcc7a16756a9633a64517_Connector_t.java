 package com.Cyberpad.Reroot;
 
 import java.net.InetAddress;
 import java.net.SocketException;
 import java.util.Date;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 import com.illposed.osc.*;
 
 public class Connector {
 	private OSCPortOut mSender;
 	private OSCPortIn mReceiver;
 	private Crypto mCrypto;
 	
	private static final int REROOT_SERVER_PORT = 57110;
	private static final int REROOT_CLIENT_PORT = 57220;
	
 	static private Connector mInstance;
 	
 	public static synchronized Connector getInstance( Context c )
 	{
 		if( mInstance == null )
 		{
 			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences( c );
 			mInstance = new Connector( preferences );
 		}
 		return mInstance;
 	}
 	
 	// Constructor
 	private Connector( SharedPreferences preferences )
 	{	
 		// Upon initialization, we have not authenticated yet
 		mCrypto = new Crypto( preferences );
 		
 		// Create a listener port which will listen for a server handshake
 		try {
			mReceiver = new OSCPortIn( REROOT_CLIENT_PORT );
 		} catch ( SocketException e ) {
 			e.printStackTrace();
 		}
 		
 		// Create a new HandshakeListener
 		HandshakeListener listener = new HandshakeListener( this );
 		mReceiver.addListener( "/handshake_server", listener );
 		mReceiver.startListening();
 	}
 	
 	public void ConnectToServer( InetAddress address, int connectionKey )
 	{
 		// Close the connection to any previous server and open a new server port
 		if( mSender != null ) mSender.close();
 		try {
			mSender = new OSCPortOut( address, REROOT_SERVER_PORT );
 		} catch( SocketException e ) {
 			Log.d( "Connector", "Unable to create outward OSC port. Exception: " + e.getMessage() );
 		}
 		
 		// Send a handshake message to the server
 		// Consider converting these to hex strings
 		Object[] args = new Object[5];
 		args[0] = mCrypto.GetPublicEncryptionKey().getModulus().toString();
 		args[1] = mCrypto.GetPublicEncryptionKey().getPublicExponent().toString();
 		args[2] = mCrypto.GetPublicSigningKey().getModulus().toString();
 		args[3] = mCrypto.GetPublicSigningKey().getPublicExponent().toString();
 		args[4] = connectionKey;
 		OSCMessage msg = new OSCMessage( "/handshake_client", args );
 		SendMessage( msg );
 	}
 	
 	public void SendControlMessage( ControlMessage message )
 	{
 		// Encrypt and sign the control message
 		String cipherText = message.FormatMessage();
 		String signature = mCrypto.Encrypt( cipherText );
 		
 		if( signature != "" )
 		{
 			// Send the control message
 			Object[] args = new Object[2];
 			args[0] = cipherText;
 			args[1] = signature;
 			OSCMessage msg = new OSCMessage( "/control", args );
 			SendMessage( msg );
 		}
 		else
 			Log.d( "Connector", "Failed to send control message." );
 	}
 	
 	private void Authenticate( String publicEncKeyMod, String publicEncKeyExp, String publicSignKeyMod, String publicSignKeyExp )
 	{
 		mCrypto.SetRemoteKeys( publicEncKeyMod, publicEncKeyExp, publicSignKeyMod, publicSignKeyExp );
 	}
 	
 	// Simple handshake listener class which shouldn't be needed outside of connector
 	private class HandshakeListener implements OSCListener
 	{
 		Connector mConnector;
 		HandshakeListener( Connector connector ) { mConnector = connector; }
 		
 		// Possible security risk.  Anyone can send a handshake message with their own public key.
 		// Server would just not interpret these messages and the user would not see input feedback from the server.
 		public void acceptMessage( Date time, OSCMessage message ) 
 		{
 			// Parse message, pull keys, call *authenticate* method
 			Object[] args = message.getArguments();
 			if( args.length == 4 )
 			{
 				String encMod = ( String )args[0];
 				String encExp = ( String )args[1];
 				String signMod = ( String )args[2];
 				String signExp = ( String )args[3];
 				mConnector.Authenticate( encMod, encExp, signMod, signExp );
 				Log.d( "Connector", "Recieved server handshake, keys exchanged." );
 			}
 		}
 	}
 	
 	// Send a message over the outgoing server port if possible.
 	private void SendMessage( OSCMessage msg )
 	{
 		try { 
 			if( mSender != null ) mSender.send( msg ); 
 		}
 		catch( Exception e ) {
 			Log.d( "Connector", "Unable to send message to server. Exception: " + e.getMessage() ); 
 		}
 	}
 }

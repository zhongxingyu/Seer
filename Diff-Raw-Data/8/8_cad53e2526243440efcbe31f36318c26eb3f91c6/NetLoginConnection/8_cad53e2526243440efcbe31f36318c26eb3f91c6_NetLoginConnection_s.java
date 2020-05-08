 package nz.ac.auckland.netlogin;
 
 import nz.ac.auckland.cs.des.C_Block;
 import nz.ac.auckland.cs.des.Key_schedule;
 import nz.ac.auckland.cs.des.desDataInputStream;
 import nz.ac.auckland.cs.des.desDataOutputStream;
 import nz.ac.auckland.netlogin.negotiation.Authenticator;
 import nz.ac.auckland.netlogin.negotiation.AuthenticatorFactory;
 import nz.ac.auckland.netlogin.negotiation.CredentialsCallback;
 import javax.security.auth.login.LoginException;
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.IOException;
 
 public class NetLoginConnection {
 		
 	final int AUTHD_PORT = 312; // The port that we are awaiting authd from.
 	final int PINGD_PORT = 443; // The port that we are awaiting pings from.
 
 	//Packet Types
 	final int AUTH_REQ_PACKET				= 1;
 	final int AUTH_REQ_RESPONSE_PACKET		= 2;
 	final int AUTH_CONFIRM_PACKET			= 3;
 	final int AUTH_CONFIRM_RESPONSE_PACKET	= 4;
 	final int NETGUARDIAN_JAVA_CLIENT		= 12;
 	final int NETGUARDIAN_JAVA_MCLIENT		= 34;
 
 	//client commands
 	final int CMD_NULL							= 0; //do nothing
 	final int CMD_LAST_CMD						= 1; //breaks cmd loop
 	final int CMD_REGISTER						= 2; //register a client
 	final int CMD_GET_USER_BALANCES_NO_BLOCK	= 3;
 	final int CMD_GET_USER_BALANCES_DO_BLOCK	= 4;
 	final int CMD_BAN_USER						= 5;
 
 	//Response ping packet Commands
 	final int ACK		= 1;
 	final int NACK		= 0;  	//will cause a shutdown.
 	final int VERSION	= 0;
 
 	//From bookdefines.h
 	final int UNAMESIZ	= 9;
 	final int PASSWDSIZ	= 17;
 
 	final int BUFSIZ	= 1024;
 	byte InBuffer[]		= new byte[ BUFSIZ ];
 
 	int clientNonce;
 	int serverNonce;
 	int Sequence_Number = 0;
 
 	Key_schedule schedule = null;				//set up encryption key to the users passwd
 	int clientType = NETGUARDIAN_JAVA_CLIENT; 	//We are a multiuser client today
 	int cmdDataLength = 2; 					//Ping Ports size (sizeof( short ))
 	short cmdData = 0; 						//Port we want pings responses on
 	
 	/* client version will control ping response messages:
 	 * when client version <3, quota-based Internet usage without user Internet plan
 	 * when client version >=3, usage-based Internet usage with displaying user plan
 	 */
 	int clientVersion = 3;
 	
 	int clientRelVersion;
 	int clientCommand = CMD_GET_USER_BALANCES_NO_BLOCK;
 
 	SPP_Packet netGuardianStream = null;
 
 	int authRef; 	//Reference to quote when we ping
 	int ipUsage; 	//Our IP Balance
 	int responsePort;  //Our Port. the Server sends Ping responses to it
 
 	PingSender pinger = null;
 	PingRespHandler ping_receiver = null;
 	boolean useStaticPingPort = false;
 	String username;
 
 	int onPlan; 					//True of False. Used in ping packets & Statusd. in net byte order
 	int localUnitCost; 				//c/MBytes of data for NZ traffic
 	int intlOffPeakRate; 			//c/MBytes of data for international traffic
 	int intlOnPeakRate; 			//c/MBytes of data for international traffic
 	int start_Peak; 				//TIME OF DAY IN MINUTES
 	int endPeak; 					//TIME OF DAY IN MINUTES
 	int lastModDate; 				//DATESTAMP FROM THE CHARGES FILE
 
 	private Authenticator authenticator;
 	private PingListener netLogin;
 
 	public NetLoginConnection(PingListener netLogin) {
 		this.netLogin = netLogin;
 	}
 
 	public void login(CredentialsCallback callback) throws IOException, LoginException {
 		String server = NetLoginPreferences.getInstance().getServer();
 		setUseStaticPingPort(NetLoginPreferences.getInstance().getUseStaticPingPort());
 		
 		pinger = new PingSender( server, PINGD_PORT, netLogin );
 		if( useStaticPingPort ){
 			ping_receiver = new PingRespHandler( netLogin, pinger, pinger.getSocket() );
 			responsePort = 0;
 		} else {
 			ping_receiver = new PingRespHandler( netLogin, pinger );
 			responsePort = ping_receiver.getLocalPort();
 		}
 
 		authenticate( server, callback );
 		
 		pinger.prepare( schedule, authRef, serverNonce + 2, Sequence_Number );
 		ping_receiver.prepare( clientNonce + 3, Sequence_Number, schedule );
 		ping_receiver.start();
 		pinger.start();
 
         NetLoginPlan plan = NetLoginPlan.lookupPlanFromFlags(onPlan);
 		netLogin.connected(username, ipUsage, plan);
 	}
 
 	public void setUseStaticPingPort( boolean b ){
 		useStaticPingPort = b;
 	}
 
 	public void logout(){
 		if( pinger != null )
 			pinger.stopPinging();
 		if( ping_receiver != null )
 			ping_receiver.end();
 	}
 
 	private void authenticate(String server, CredentialsCallback callback) throws IOException, LoginException {
 		authenticator = AuthenticatorFactory.getInstance().getSelectedAuthenticator();
 		netGuardianStream = new SPP_Packet(server, AUTHD_PORT);
         try {
 			sendPacket1(callback);
 			readPacket1();
 			sendPacket2();
 			readPacket2();
 		} finally {
             netGuardianStream.close();
 			netGuardianStream = null;
 		}
 	}
 
 	private void sendPacket1(CredentialsCallback callback) throws IOException, LoginException {
 		Authenticator.AuthenticationRequest request = authenticator.startAuthentication(callback);
 		this.username = request.getUsername();
 
 		desDataOutputStream packet = new desDataOutputStream(128);
 		packet.writeInt(clientType);
 		packet.writeInt(clientVersion);
 		packet.writeBytes(request.getUsername(), UNAMESIZ); // truncates or pads so always UNAMESIZ
 		packet.write(request.getPayload());
 
 		netGuardianStream.SendPacket(AUTH_REQ_PACKET, VERSION, packet.toByteArray());
 	}
 
 	private void readPacket1() throws IOException, LoginException {
		desDataInputStream des_in;
 		DataInputStream	unencrypted_data_input_Stream = null;
		int random_returned;
 		ReadResult PacketHeader;
 		int client_URL_Length;
 		byte URL[];
 		String error_msg;
 
 		PacketHeader = netGuardianStream.ReadPacket( AUTH_REQ_RESPONSE_PACKET, VERSION, InBuffer );
 		switch ( PacketHeader.Result ) {
 			case SPP_Packet.RSLT_BAD_PACKET_TYPE:
 				throw new IOException( "Unexpected PacketType " + PacketHeader.Packet_type +
 						" expected " + AUTH_REQ_RESPONSE_PACKET );
 			case SPP_Packet.REMOTE_RSLT_BAD_VERSION:
 				error_msg = "Client Version incorrect";
 				unencrypted_data_input_Stream = new DataInputStream( new ByteArrayInputStream(
 							InBuffer, 0, netGuardianStream.Last_Read_length ) );
 				try {
 					clientRelVersion = unencrypted_data_input_Stream.readInt();
 					if ( clientRelVersion > 0 ) {
 						error_msg += "\rThe latest version is " + clientRelVersion;
 						try {
 							client_URL_Length = unencrypted_data_input_Stream.readInt();
 							if ( client_URL_Length != 0 ) {
 								URL = new byte[ client_URL_Length ];
 								unencrypted_data_input_Stream.read( URL );
 								error_msg += "\rURL " + URL;
 							}
 						} catch ( Exception e ) {} //ignore
 					}
 
 				} catch ( Exception e ) {} //ignore
 
 				throw new IOException( error_msg );
 			case SPP_Packet.REMOTE_RSLT_ACCESS_RESTRICTION:
 				throw new IOException( "Access Denied from this Network or Host" );
 			case SPP_Packet.REMOTE_RSLT_ACCESS_RESTRICTION_BAN:
 				throw new IOException( "User Banned for using this host/network" );
 			case SPP_Packet.RSLT_OK:
 				break;
 			default:
 				throw new IOException( "Protocol Error " + PacketHeader.Result );
 		}
 
 		PacketHeader = null;
 
 		if( netGuardianStream.Last_Read_length < /*C_Block.size() + 4 * 4*/ 20 )
 			throw new IOException("readPacket1: AUTH_REQ_RESPONSE_PACKET too short, " +
 					netGuardianStream.Last_Read_length + " bytes " + (C_Block.size() * 4) );
 
 		unencrypted_data_input_Stream = new DataInputStream( new ByteArrayInputStream( InBuffer, 0, 4 ) );
 		clientRelVersion = unencrypted_data_input_Stream.readInt();
 
 		byte[] payload = new byte[netGuardianStream.Last_Read_length - 4];
 		System.arraycopy(InBuffer, 4, payload, 0, payload.length);
 
 		Authenticator.LoginComplete session = authenticator.validateResponse(payload);
 
 		clientNonce = session.getClientNonce();
 		serverNonce = session.getServerNonce();
 		schedule = new Key_schedule(session.getSessionKey());
 	}
 
 	private void sendPacket2() throws IOException {
 		desDataOutputStream des_out = new desDataOutputStream( 128 );
 		byte EncryptedOutBuffer[];
 
 		cmdData = ( short ) responsePort; 	//Port we want pings responses on
 		des_out.writeInt( serverNonce + 1 );   		//Can throw IOException
 		des_out.writeInt(clientCommand);   	//Can throw IOException
 		des_out.writeInt(cmdDataLength); 	//Can throw IOException
 		des_out.writeShort(cmdData);			//Can throw IOException
 
 		EncryptedOutBuffer = des_out.des_encrypt( schedule );  	//encrypt buffer
 		netGuardianStream.SendPacket( AUTH_CONFIRM_PACKET, VERSION, EncryptedOutBuffer );
 	}
 
 	private void readPacket2() throws IOException {
 		desDataInputStream des_in;
 		int random_returned;
 		int ack;
 		int cmd_result_data_length;
 		DataInputStream	unencrypted_data_input_Stream = null;
 		ReadResult PacketHeader = null;
 
 		// Process final ack packet to ensure all went well
 		PacketHeader = netGuardianStream.ReadPacket( AUTH_CONFIRM_RESPONSE_PACKET, VERSION, InBuffer );
 		switch ( PacketHeader.Result ) {
 			case SPP_Packet.RSLT_BAD_PACKET_TYPE:
 				throw new IOException( "Unexpected PacketType " + PacketHeader.Packet_type +
 						" expected " + AUTH_REQ_RESPONSE_PACKET );
 			case SPP_Packet.RSLT_OK:
 				break;
 			default:
 				throw new IOException( "Protocol Error " + PacketHeader.Result );
 		}
 
 		if ( netGuardianStream.Last_Read_length < ( 10 * 4 ) )
 			throw new IOException( "readPacket2: AUTH_CONFIRM_RESPONSE_PACKET too short" );
 		//too short for serverNonce, ack and string
 
 		unencrypted_data_input_Stream = new DataInputStream( new ByteArrayInputStream( InBuffer, 0, 28 ) );
 		onPlan = unencrypted_data_input_Stream.readInt();
 		localUnitCost = unencrypted_data_input_Stream.readInt();
 		intlOffPeakRate = unencrypted_data_input_Stream.readInt();
 		intlOnPeakRate = unencrypted_data_input_Stream.readInt();
 		start_Peak = unencrypted_data_input_Stream.readInt();
 		endPeak = unencrypted_data_input_Stream.readInt();
 		lastModDate = unencrypted_data_input_Stream.readInt();
 
 
 		des_in = new desDataInputStream( InBuffer , 28, netGuardianStream.Last_Read_length, schedule );
 
 		random_returned = des_in.readInt();
 		if ( clientNonce + 2 != random_returned ) {	// Other end doesn't agree on the current passwd
 			throw new IOException( "Incorrect password" );
 		}
 
 		ack = des_in.readInt();
 		cmd_result_data_length = des_in.readInt();
 
 		if( ack != Errors.CMD_RSLT_OK && ack != Errors.CMD_RSLT_WOULD_BLOCK )  //Error
 			throw new IOException( "got Nack on authentication " + Errors.error_messages[ ack ] );
 
 		if( ack == Errors.CMD_RSLT_WOULD_BLOCK )
 			throw new IOException( "User record is locked. Unable to read IP balance" );
 
 		if( cmd_result_data_length < 2 * 4 )
 			throw new IOException( "Cmd result buffer too small" );
 		else {
 			authRef = des_in.readInt();
 			ipUsage = des_in.readInt();
 		}
 	}
 
	public String getUsername() {
		return username;
	}
	
 }

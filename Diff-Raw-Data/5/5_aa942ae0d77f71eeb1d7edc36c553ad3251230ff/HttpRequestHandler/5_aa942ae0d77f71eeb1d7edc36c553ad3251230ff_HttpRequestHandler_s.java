 /**
  * $$\\ToureNPlaner\\$$
  */
 
 package server;
 
 import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
 import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
 import static org.jboss.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
 import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
 import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
 import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Random;
 
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBufferInputStream;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelFutureListener;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
 import org.jboss.netty.handler.codec.base64.Base64;
 import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
 import org.jboss.netty.handler.codec.http.HttpMethod;
 import org.jboss.netty.handler.codec.http.HttpRequest;
 import org.jboss.netty.handler.codec.http.HttpResponse;
 import org.jboss.netty.handler.codec.http.HttpResponseStatus;
 import org.jboss.netty.handler.codec.http.QueryStringDecoder;
 import org.jboss.netty.util.CharsetUtil;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import computecore.ComputeCore;
 import computecore.ComputeRequest;
 
 import config.ConfigManager;
 import database.DatabaseManager;
 import database.UsersDBRow;
 
 /**
  * This handler handles HTTP Requests on the normal operation socket including *
  * 
  * @author Niklas Schnelle, Peter Vollmer
  * @version 0.1
  * 
  *          Initially based on: http://docs.jboss.org/netty/3.2/xref
  *          /org/jboss/netty/example/http/snoop/package-summary.html
  */
 public class HttpRequestHandler extends SimpleChannelUpstreamHandler {
 
 	/** JSONParser we can reuse **/
 	private final JSONParser parser = new JSONParser();
 
 	/** The ComputeCore managing the threads **/
 	private final ComputeCore computer;
 
 	private final Map<String, Object> serverInfo;
 
 	private boolean isPrivate;
 
 	private DatabaseManager dbm;
 
 	private MessageDigest digester;
 
 	/**
 	 * Constructs a new RequestHandler using the given ComputeCore and
 	 * ServerInfo
 	 * 
 	 * @param cCore
 	 * @param serverInfo
 	 */
 	public HttpRequestHandler(ComputeCore cCore,
 			Map<String, Object> serverInfo, boolean isPrivate) {
 		super();
 		ConfigManager cm = ConfigManager.getInstance();
 		computer = cCore;
 		this.serverInfo = serverInfo;
 		this.isPrivate = isPrivate;
 		if (isPrivate) {
 			try {
 
 				this.dbm = new DatabaseManager(cm.getEntryString("dburi",
 						"jdbc:mysql://localhost:3306/"), cm.getEntryString(
 						"dbname", "tourenplaner"), cm.getEntryString("dbuser",
 						"toureNPlaner"), cm.getEntryString("dbpw",
 						"toureNPlaner"));
 			} catch (SQLException e) {
 				System.err
 						.println("Can't connect to database (switching to public mode) "
 								+ e.getMessage());
 				this.isPrivate = false;
 			}
 		}
 
 		if (isPrivate) {
 			try {
 				digester = MessageDigest.getInstance("SHA-1");
 			} catch (NoSuchAlgorithmException e) {
 				System.err
 						.println("Can't load SHA-1 Digester. Will now switch to public mode");
 				this.isPrivate = false;
 			}
 		}
 	}
 
 	/**
 	 * Called when a message is received
 	 */
 	@Override
 	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
 			throws Exception {
 
 		HttpRequest request = (HttpRequest) e.getMessage();
 		Channel channel = e.getChannel();
 		// System.out.print(request.toString());
 		// Handle preflighted requests so wee need to work with OPTION Requests
 		if (request.getMethod().equals(HttpMethod.OPTIONS)) {
 			handlePreflights(request, channel);
 			return;
 		}
 
 		Responder responder = new Responder(channel, isKeepAlive(request));
 
 		// Get the Requeststring e.g. /info
 		QueryStringDecoder queryStringDecoder = new QueryStringDecoder(
 				request.getUri());
 
 		String path = queryStringDecoder.getPath();
 
 		if (path.equals("/info")) {
 
 			handleInfo(request, responder);
 
 		} else if (isPrivate && path.equals("/registeruser")) {
 
 			handleRegisterUser(request, responder);
 
 		} else if (isPrivate && path.equals("/authuser")) {
 
 			handleAuthUser(request, responder);
 
 		} else if (isPrivate && path.equals("/getuser")) {
 
 			handleGetUser(request, responder);
 
 		} else if (isPrivate && path.equals("/updateuser")) {
 
 			handleUpdateUser(request, responder);
 
 		} else if (isPrivate && path.equals("/listrequests")) {
 
 			handleListRequests(request, responder);
 
 		} else if (isPrivate && path.equals("/listusers")) {
 
 			handleListUsers(request, responder);
 
 		} else if (path.startsWith("/alg")) {
 
 			if (!isPrivate || auth(request, responder) != null) {
 				String algName = queryStringDecoder.getPath().substring(4);
 				handleAlg(request, responder, algName);
 			} else {
 				responder.writeUnauthorizedClose();
 			}
 		}
 
 	}
 
 	private void handleAlg(HttpRequest request, Responder responder,
 			String algName) throws IOException {
 
 		Map<String, Object> objmap = getJSONContent(responder, request);
 		if (objmap != null) {
 			// Create ComputeRequest and commit to workqueue
 			ComputeRequest req = new ComputeRequest(responder, algName, objmap);
 			boolean sucess = computer.submit(req);
 
 			if (!sucess) {
 				responder
 						.writeErrorMessage(
 								"EBUSY",
 								"This server is currently too busy to fullfill the request",
 								null, HttpResponseStatus.SERVICE_UNAVAILABLE);
 			}
 		}
 	}
 
 	/**
 	 * Extracts and parses the JSON encoded content of the given HttpRequest, in
 	 * case of error sends a EBADJSON or HttpStatus.NO_CONTENT answer to the
 	 * client and returns null, the connection will be closed afterwards.
 	 * 
 	 * @param responder
 	 * @param request
 	 * @throws IOException
 	 */
 	@SuppressWarnings("unchecked")
 	private Map<String, Object> getJSONContent(Responder responder,
 			HttpRequest request) throws IOException {
 
 		Map<String, Object> objmap = null;
 		ChannelBuffer content = request.getContent();
 		if (content.readableBytes() != 0) {
 			InputStreamReader inReader = new InputStreamReader(
 					new ChannelBufferInputStream(content));
 			try {
 				objmap = (JSONObject) parser.parse(inReader);
 			} catch (ParseException e) {
 				responder.writeErrorMessage("EBADJSON",
 						"Could not parse supplied JSON", null,
 						HttpResponseStatus.UNAUTHORIZED);
 				objmap = null;
 			}
 
 		} else {
 			// Respond with No Content
 			HttpResponse response = new DefaultHttpResponse(HTTP_1_1,
 					NO_CONTENT);
 			// Write the response.
 			ChannelFuture future = responder.getChannel().write(response);
 			future.addListener(ChannelFutureListener.CLOSE);
 		}
 
 		return objmap;
 	}
 
 	private void handleListUsers(HttpRequest request, Responder responder) {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void handleListRequests(HttpRequest request, Responder responder) {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void handleUpdateUser(HttpRequest request, Responder responder) {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void handleGetUser(HttpRequest request, Responder responder) {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void handleAuthUser(HttpRequest request, Responder responder) {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void handleRegisterUser(HttpRequest request, Responder responder) {
 		try {
 			UsersDBRow user = null;
 			UsersDBRow authUser = null;
 
 			Map<String, Object> objmap = getJSONContent(responder, request);
 			if (objmap == null) {
 				return;
 			}
 
 			// TODO optimize salt-generation
 			Random rand = new Random();
 			StringBuilder saltBuilder = new StringBuilder(100);
 			for (int i = 0; i < 10; i++) {
 				saltBuilder.append(Long.toHexString(rand.nextLong()));
 			}
 
 			String salt = saltBuilder.toString();
 			String pw = (String) objmap.get("password");
 
 			String toHash = generateHash(salt, pw);
 			// TODO cover all cases
 
 			// if no authorization header add not verified user
 			if (request.getHeader("Authorization") == null) {
 
 				user = dbm.addNewUser((String) objmap.get("email"), toHash,
 						salt, (String) objmap.get("firstname"),
 						(String) objmap.get("lastname"),
 						(String) objmap.get("address"),
 						(Boolean) objmap.get("admin"));
 			} else {
 				if ((authUser = auth(request, responder)) != null
 						&& authUser.isAdmin) {
 
 					user = dbm.addNewVerifiedUser((String) objmap.get("email"),
 							toHash, salt, (String) objmap.get("firstname"),
 							(String) objmap.get("lastname"),
 							(String) objmap.get("address"),
 							(Boolean) objmap.get("admin"));
 
 				} else {
 					responder.writeUnauthorizedClose();
 				}
 			}
 
 			// TODO handler if user already existed
 			if (user == null) {
 
 			}
 
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 
 			// TODO JSON Exception Handling
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	protected String generateHash(String salt, String pw) {
 		// Compute SHA1 of PW:SALT
 		String toHash = pw + ":" + salt;
 
 		byte[] bindigest = digester.digest(toHash.getBytes(CharsetUtil.UTF_8));
 		// Convert to Hex String
 		StringBuilder hexbuilder = new StringBuilder(bindigest.length * 2);
 		for (byte b : bindigest) {
 			hexbuilder.append(Integer.toHexString((b >>> 4) & 0x0F));
 			hexbuilder.append(Integer.toHexString(b & 0x0F));
 		}
 		toHash = hexbuilder.toString();
 		return toHash;
 	}
 
 	private void handleInfo(HttpRequest request, Responder responder) {
 		responder.writeJSON(serverInfo, HttpResponseStatus.OK);
 	}
 
 	/**
 	 * Handles preflighted OPTION Headers
 	 * 
 	 * @param request
 	 * @param channel
 	 */
 	private void handlePreflights(HttpRequest request, Channel channel) {
 		boolean keepAlive = isKeepAlive(request);
 		HttpResponse response;
 
 		// We only allow POST and GET methods so only allow request when Method
 		// is Post or Get
 		String methodType = request.getHeader("Access-Control-Request-Method");
 		if (methodType != null
 				&& (methodType.trim().equals("POST") || methodType.trim()
 						.equals("GET"))) {
 			response = new DefaultHttpResponse(HTTP_1_1, OK);
 			response.addHeader("Connection", "Keep-Alive");
 		} else {
 			response = new DefaultHttpResponse(HTTP_1_1, FORBIDDEN);
 			// We don't want to keep the connection now
 			keepAlive = false;
 		}
 
 		ArrayList<String> allowHeaders = new ArrayList<String>(2);
 		allowHeaders.add("Content-Type");
 		allowHeaders.add("Authorization");
 
 		response.setHeader("Access-Control-Allow-Origin", "*");
 		response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
 		response.setHeader(CONTENT_TYPE, "application/json");
 		response.setHeader("Content-Length", "0");
 
 		response.setHeader("Access-Control-Allow-Headers", allowHeaders);
 
 		ChannelFuture future = channel.write(response);
 		if (!keepAlive) {
 			future.addListener(ChannelFutureListener.CLOSE);
 		}
 
 	}
 
 	/**
 	 * Authenticates a Request using HTTP Basic Authentication and returns the
 	 * UsersDBRow object of the authenticated user or null if authentication
 	 * failed. Errors will be sent to the client as error messages see protocol
 	 * specification for details. The connection will get closed after the error
 	 * has been sent
 	 * 
 	 * @param responder
 	 * 
 	 * @param request
 	 * @return the UsersDBRow object of the user or null if auth failed
 	 * @throws SQLException
 	 */
 	private UsersDBRow auth(HttpRequest myReq, Responder responder)
 			throws SQLException {
 		String email, emailandpw, pw;
 		UsersDBRow user = null;
 		int index = 0;
 		// Why between heaven and earth does Java have AES Encryption in
 		// the standard library but not Base64 though it has it internally
 		// several times
 		emailandpw = myReq.getHeader("Authorization");
 		if (emailandpw == null) {
 			return null;
 		}
 
 		ChannelBuffer encodeddata;
 		ChannelBuffer data;
 		// Base64 is always ASCII
 		encodeddata = ChannelBuffers.wrappedBuffer(emailandpw.substring(
 				emailandpw.lastIndexOf(' ')).getBytes(CharsetUtil.US_ASCII));
 
 		data = Base64.decode(encodeddata);
 		// The string itself is utf-8
 		emailandpw = data.toString(CharsetUtil.UTF_8);
 		index = emailandpw.indexOf(':');
 		if (index <= 0) {
 			return null;
 		}
 
 		email = emailandpw.substring(0, index);
 		pw = emailandpw.substring(index + 1);
 		// TODO Database
 
 		user = dbm.getUser(email);
 		if (user == null) {
 			responder.writeErrorMessage("EAUTH", "Wrong username or password",
 					null, HttpResponseStatus.UNAUTHORIZED);
 			return null;
 		}
 
 		// Compute SHA1 of PW:SALT
 		String toHash = generateHash(user.salt, pw);
 
 		System.out.println(pw + ":" + user.salt + " : " + toHash);
 		if (!user.passwordhash.equals(toHash)) {
 			responder.writeErrorMessage("EAUTH", "Wrong username or password",
 					null, HttpResponseStatus.UNAUTHORIZED);
 			return null;
 		}
 
 		return user;
 	}
 
 	/**
 	 * Called when an uncaught exception occurs
 	 */
 	@Override
 	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
 			throws Exception {
 		e.getCause().printStackTrace();
 		e.getChannel().close();
 	}
 }

 package ar.edu.it.itba.pdc.v2.implementations.configurator;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import javax.ws.rs.core.MediaType;
 
 import org.apache.log4j.Logger;
 
 import ar.edu.it.itba.pdc.v2.interfaces.ConfiguratorConnectionDecoderInt;
 
 public class ConfiguratorConnectionDecoder implements
 		ConfiguratorConnectionDecoderInt {
 
 	private boolean logged = false;
 	private boolean closeConnection = false;
 	private boolean applyTransformations = false;
 	private boolean applyRotations = false;
 	private boolean blockAll = false;
 	private Map<String, String> reply;
 	private Set<InetAddress> blockedAddresses;
 	private Set<String> blockedMediaType;
 	private Set<String> blockedURIs;
 	private int maxSize = -1;
 	private Logger decoderLog = Logger.getLogger(this.getClass());
 
 	public ConfiguratorConnectionDecoder() {
 		reply = new HashMap<String, String>();
 		fillReply();
 		blockedAddresses = new TreeSet<InetAddress>();
 		blockedMediaType = new TreeSet<String>();
 		blockedURIs = new TreeSet<String>();
 	}
 
 	public boolean closeConnection() {
 		return closeConnection;
 	}
 
 	public String decode(String s) {
 		s = s.replace("\n", "");
 		if (!logged) {
 			String[] credentials = s.split(":");
 			if (credentials.length == 2 && credentials[0].equals("admin")
 					&& credentials[1].equals("pdc2012")) {
 				decoderLog.info("Admin logged in");
 				logged = true;
 				return reply.get("LOG_IN_OK");
 			} else {
 				decoderLog.info("Log in failed. Closing connection");
 				closeConnection = true;
 				return reply.get("LOGIN_ERROR");
 			}
 		} else {
 			String[] args = s.split(" ");
 			if (args[0].equals("BLOCK")) {
 				return analyzeBlockCommand(args);
 			} else if (args[0].equals("UNBLOCK")) {
 				return analyzeUnblockCommand(args);
 			} else if (args[0].equals("TRANSFORMATIONS")) {
 				if (args.length != 2) {
 					return reply.get("WRONG_COMMAND");
 				}
 				if (args[1].equals("ON")) {
 					decoderLog.info("Transformations turned on");
 					applyTransformations = true;
 					return reply.get("TRANSF_ON");
 				} else if (args[1].equals("OFF")) {
 					decoderLog.info("Transformations turned off");
 					applyTransformations = false;
 					return reply.get("TRANSF_OFF");
 				} else {
 					return reply.get("WRONG_PARAMETERS");
 				}
 			} else if (args[0].equals("ROTATIONS")) {
 				if (args.length != 2) {
 					return reply.get("WRONG_COMMAND");
 				}
 				if (args[1].equals("ON")) {
 					decoderLog.info("Rotations turned on");
 					applyRotations = true;
 					return reply.get("ROT_ON");
 				} else if (args[1].equals("OFF")) {
 					decoderLog.info("Rotations turned off");
 					applyRotations = false;
 					return reply.get("ROT_OFF");
 				} else {
 					return reply.get("WRONG_PARAMETERS");
 				}
 			} else if (args.length == 3 && args[0].equals("GET")
 					&& args[1].equals("CONF")) {
 				if (args.length != 3) {
 					return reply.get("WRONG_COMMAND");
 				}
 				if (args[2].equals("ROTATIONS")) {
 					if (applyRotations) {
 						return reply.get("ROT_ON");
 					} else {
 						return reply.get("ROT_OFF");
 					}
 				} else if (args[2].equals("TRANSFORMATIONS")) {
 					if (applyTransformations) {
 						return reply.get("TRANSF_ON");
 					} else {
 						return reply.get("TRANSF_OFF");
 					}
 				} else if (args[2].equals("BLOCK")) {
 					StringBuffer sb = new StringBuffer();
 					sb.append("200 - Blocked list:\n");
 					sb.append("Media Types:\n");
 					for (String mt : blockedMediaType) {
 						sb.append("\t" + mt.toString() + "\n");
 					}
 					sb.append("URIs:\n");
 					for (String p : blockedURIs) {
 						sb.append("\t" + p + "\n");
 					}
 					sb.append("IP addresses:\n");
 					for (InetAddress addr : blockedAddresses) {
 						sb.append("\t" + addr.toString() + "\n");
 					}
 					return sb.toString();
 				} else {
 					return reply.get("WRONG_PARAMETERS");
 				}
 			} else if (args[0].equals("EXIT")) {
 				decoderLog.info("EXIT command received. Closing connection");
 				closeConnection = true;
 				logged = false;
 				return "Bye bye\n";
 			} else if (args[0].equals("HELP")) {
 				return printHelp();
 			} else {
 				return reply.get("WRONG_COMMAND");
 			}
 		}
 	}
 
 	private String analyzeBlockCommand(String[] line) {
 		if (line.length == 2 && line[1].equals("ALL")) {
 			blockAll = true;
 			return "200 - All access blocked\n";
 		}
 		if (line.length != 3)
 			return reply.get("WRONG_COMMAND");
 		String type = line[1];
 		String arg = line[2];
 		if (type.equals("IP")) {
 			InetAddress addr;
 			try {
 				addr = InetAddress.getByName(arg);
 				decoderLog.info("Blocking " + addr.toString());
 				synchronized (blockedAddresses) {
 					blockedAddresses.add(addr);
 				}
 				return "200 - " + arg + " blocked\n";
 			} catch (Exception e) {
 				return reply.get("WRONG_PARAMETERS");
 			}
 		} else if (type.equals("MTYPE")) {
 			MediaType mt = analyzeMediaType(arg);
 			if (mt == null) {
 				return "400 - Invalid media type\n";
 			}
 			decoderLog.info("Blocking " + mt.toString());
 			synchronized (blockedMediaType) {
 				blockedMediaType.add(mt.toString());
 			}
 			return "200 - " + mt.toString() + " blocked\n";
 		} else if (type.equals("SIZE")) {
 			try {
 				Integer max = Integer.parseInt(arg);
				maxSize = max;
				decoderLog.info("Blocking files bigger than " + max);
 				return "200 - Sizes bigger than " + maxSize
						+ " are now blocked\n";
 			} catch (NumberFormatException e) {
 				return "400 - Invalid size\n";
 			}
 		} else if (type.equals("URI")) {
 			try {
 				Pattern p = Pattern.compile(arg);
 				decoderLog.info("Blocking " + p.toString());
 				synchronized (blockedURIs) {
 					blockedURIs.add(p.pattern());
 				}
 				return "200 - " + arg + " blocked\n";
 			} catch (PatternSyntaxException e) {
 				return "400 - Invalid pattern\n";
 			}
 		} else {
 			return reply.get("WRONG_PARAMETERS");
 		}
 	}
 
 	private String analyzeUnblockCommand(String[] line) {
 		if (line.length == 2 && line[1].equals("ALL")) {
 			blockAll = false;
 			return "200 - All access unblocked\n";
 		}
 		if (line.length != 3)
 			return reply.get("WRONG_COMMAND");
 		String type = line[1];
 		String arg = line[2];
 		if (type.equals("IP")) {
 			InetAddress addr;
 			try {
 				addr = InetAddress.getByName(arg);
 				decoderLog.info("Unblocking " + addr.toString());
 				synchronized (blockedAddresses) {
 					blockedAddresses.remove(addr);
 				}
 				return "200 - " + arg + " blocked\n";
 			} catch (UnknownHostException e) {
 				return reply.get("WRONG_PARAMETERS");
 			}
 		} else if (type.equals("MTYPE")) {
 			MediaType mt = analyzeMediaType(arg);
 			if (mt == null) {
 				return "400 - Invalid media type\n";
 			}
 			decoderLog.info("Unblocking " + mt.toString());
 			synchronized (blockedMediaType) {
 				blockedMediaType.remove(mt.toString());
 			}
 			return "200 - " + mt.getType() + mt.getSubtype() + " unblocked\n";
 		} else if (type.equals("SIZE")) {
 			try {
 				Integer max = Integer.parseInt(arg);
 				maxSize = max;
 				if (max == -1) {
 					return "200 - All sizes are permited\n";
 				}
 				decoderLog.info("Unblocking files bigger than" + max);
 				return "200 - Sizes bigger than " + maxSize
 						+ " are now blocked\n";
 			} catch (NumberFormatException e) {
 				return "400 - Invalid size\n";
 			}
 		} else if (type.equals("URI")) {
 			try {
 				Pattern p = Pattern.compile(arg);
 				synchronized (blockedURIs) {
 					blockedURIs.remove(p.pattern());
 				}
 				decoderLog.info("Unblocking " + p.toString());
 				return "200 - " + p.pattern() + " unblocked\n";
 			} catch (PatternSyntaxException e) {
 				return "400 - Invalid pattern\n";
 			}
 		} else {
 			return reply.get("WRONG_PARAMETERS");
 		}
 	}
 
 	private MediaType analyzeMediaType(String mtype) {
 		MediaType media;
 		try {
 			media = MediaType.valueOf(mtype);
 		} catch (IllegalArgumentException e) {
 			return null;
 		}
 		return media;
 	}
 
 	private void fillReply() {
 		reply.put("LOG_IN_OK", "200 - Welcome\n");
 		reply.put("LOGIN_ERROR", "400 - Wrong username and/or password\n");
 		reply.put("WRONG_COMMAND", "401 - Wrong command\n");
 		reply.put("WRONG_PARAMETERS", "401 - Wrong parameters\n");
 		reply.put("TRANSF_ON", "200 - Transformations are on\n");
 		reply.put("TRANSF_OFF", "200 - Transformations are off\n");
 		reply.put("ROT_ON", "200 - Rotations are on\n");
 		reply.put("ROT_OFF", "200 - Rotations are off\n");
 	}
 
 	private String printHelp() {
 		decoderLog.info("HELP command received");
 		StringBuffer sb = new StringBuffer();
 		sb.append("Available commands: BLOCK - UNBLOCK - TRANSFORMATIONS - ROTATIONS - GET\n");
 		sb.append("BLOCK or UNBLOCK usage: (BLOCK | UNBLOCK)SP(IP|URI|MTYPE|SIZE)SP(VALUE)\n");
 		sb.append("TRANSFORMATIONS or ROTATIONS usage: (TRANSFORMATIONS|ROTATIONS)SP(ON|OFF)\n");
 		sb.append("GET usage: GET SP (ROTATIONS|TRANSFORMATIONS|BLOCK)\n");
 
 		return sb.toString();
 	}
 
 	public Object[] getBlockedAddresses() {
 		synchronized (blockedAddresses) {
 			return blockedAddresses.toArray();
 		}
 	}
 
 	public Object[] getBlockedMediaType() {
 		synchronized (blockedMediaType) {
 			return blockedMediaType.toArray();
 		}
 	}
 
 	public Object[] getBlockedURIs() {
 		synchronized (blockedURIs) {
 			return blockedURIs.toArray();
 		}
 	}
 
 	public int getMaxSize() {
 		return maxSize;
 	}
 
 	public boolean applyRotations() {
 		return applyRotations;
 	}
 
 	public boolean applyTransformations() {
 		return applyTransformations;
 	}
 
 	public void reset() {
 		closeConnection = false;
 	}
 
 	public boolean blockAll() {
 		return blockAll;
 	}
 
 }

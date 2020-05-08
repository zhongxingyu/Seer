 package jipdbs.xmlrpc.handler;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Logger;
 
 import jipdbs.api.Events;
 import jipdbs.api.ServerManager;
 import jipdbs.api.v2.Update;
 import jipdbs.core.JIPDBS;
 import jipdbs.core.model.Penalty;
 import jipdbs.core.model.Server;
 import jipdbs.exception.UnauthorizedUpdateException;
 import jipdbs.info.PenaltyInfo;
 import jipdbs.info.PlayerInfo;
 import jipdbs.xmlrpc.JIPDBSXmlRpc2Servlet;
 
 public class JIPDBSRpc3Handler {
 
 	private static final Logger log = Logger.getLogger(JIPDBSRpc3Handler.class
 			.getName());
 
 	@SuppressWarnings("unused")
 	private final JIPDBS app;
 	private final Update updateApi;
 
 	private static final int maxListSize = 30;
 
 	public JIPDBSRpc3Handler(JIPDBS app) {
 		this.app = app;
 		this.updateApi = new Update();
 	}
 
 	public void updateName(String key, String name, Object[] data) {
 		this.updateApi.updateName(key, name, (String) data[0],
 				(Integer) data[1], JIPDBSXmlRpc2Servlet.getClientIpAddress());
 	}
 
 	public void update(String key, Object[] plist) throws Exception {
 
 		try {
 			Server server = ServerManager.getAuthorizedServer(key,
 					JIPDBSXmlRpc2Servlet.getClientIpAddress());
 
 			List<PlayerInfo> list = new ArrayList<PlayerInfo>();
 			for (Object o : plist) {
 				Object[] values = ((Object[]) o);
 				String event = (String) values[0];
 				PlayerInfo playerInfo = new PlayerInfo(event,
 														(String) values[1],
 														(String) values[2],
 														parseLong(values[3]),
 														(String) values[4],
 														parseLong(values[5]));
 				if (values.length > 6) {
 					Date updated;
 					if (values[6] instanceof Date) {
 						updated = (Date) values[6];
 					} else {
 						try {
 							updated = new Date((Integer) values[6] * 1000L);
 						} catch (Exception e) {
 							log.severe(e.getMessage());
 							updated = new Date();
 						}
 					}
 					playerInfo.setUpdated(updated);
 				}
 				if (Events.BAN.equals(event)) {
 					Object[] data = (Object[]) values[7];
 					PenaltyInfo penalty = new PenaltyInfo();
 					penalty.setType(Penalty.BAN);
 					penalty.setCreated(parseLong(data[1]));
 					penalty.setDuration(parseLong(data[2]));
 					penalty.setReason((String) data[3]);
 					penalty.setAdmin((String) data[4]);
					penalty.setAdminId(Integer.toString((Integer) data[5]));
 					playerInfo.setPenaltyInfo(penalty);
 				} else if (Events.ADDNOTE.equals(event)) {
 					Object[] data = (Object[]) values[7];
 					PenaltyInfo penalty = new PenaltyInfo();
 					penalty.setType(Penalty.NOTICE);
 					penalty.setCreated(parseLong(data[1]));
 					penalty.setReason((String) data[2]);
 					penalty.setAdmin((String) data[3]);
					penalty.setAdminId(Integer.toString((Integer) data[4]));
 					playerInfo.setPenaltyInfo(penalty);			
 				}
 				list.add(playerInfo);
 			}
 			if (list.size() > 0) {
 				if (list.size() > maxListSize) {
 					log.warning("List size is " + Integer.toString(list.size()));
 					// this is too much to process
 					list = list.subList(list.size() - maxListSize, list.size());
 				} else {
 					log.info("List size is " + Integer.toString(list.size()));
 				}
 				updateApi.updatePlayer(server, list);
 			} else {
 				if (server.getOnlinePlayers() > 0) {
 					log.fine("Cleaning server " + server.getName());
 					updateApi.cleanServer(server);
 				}
 			}
 		} catch (UnauthorizedUpdateException e) {
 			log.severe(e.getMessage());
 			StringWriter w = new StringWriter();
 			e.printStackTrace(new PrintWriter(w));
 			log.severe(w.getBuffer().toString());
 		} catch (Exception e) {
 			log.severe(e.getMessage());
 			StringWriter w = new StringWriter();
 			e.printStackTrace(new PrintWriter(w));
 			log.severe(w.getBuffer().toString());
 			throw e;
 		}
 	}
 
 	private Long parseLong(Object s) {
 		try {
 			if (s instanceof String) {
 				return Long.parseLong((String) s);
 			} else if (s instanceof Number) {
 				return ((Number) s).longValue();
 			}
 			return null;
 		} catch (NumberFormatException e) {
 			return null;
 		}
 	}
 }

 package org.witness.informa;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.cometd.bayeux.Message;
 import org.cometd.bayeux.server.BayeuxServer;
 import org.cometd.bayeux.server.ServerChannel;
 import org.cometd.bayeux.server.ServerSession;
 import org.cometd.server.AbstractService;
 import org.witness.informa.utils.Constants.DC.Attempts;
 import org.witness.informa.utils.Constants;
 import org.witness.informa.utils.CouchParser;
 import org.witness.informa.utils.InformaMessage;
 
 public class DesktopService extends AbstractService implements Constants {
 	MediaLoader ml = null;
 	boolean cFlag = false;
 	
 	public DesktopService(BayeuxServer bayeux) {
 		super(bayeux, "desktopConnection");
 		addService("/service/desktopConnection", "desktopResponse");
 		addService("/multicast", "multicastResponse");
 		
 		if(ml == null)
			ml = new MediaLoader();		
 	}
 	
 	public void multicastResponse(ServerSession remote, Message message) {
 		Log("hello multicast");
 	}
 	
 	public void desktopResponse(ServerSession remote, Message message) {
		// TODO: set client session id with remote.getId()
 		
 		InformaMessage msg = new InformaMessage(message);
 		if(msg.in.containsKey(Attempts.TAG)) {
 			long l = (Long) msg.in.get(Attempts.TAG);
 			switch((int) l) {
 			case Attempts.ATTEMPT_LOGIN:
 				if(ml == null)
 					ml = new MediaLoader();
 				
 				msg.put(DC.Keys.COMMAND, DC.Commands.ATTEMPT_LOGIN);
 				msg.put(DC.Keys.METADATA, ml.loginUser(msg.opts));
 				break;
 			case Attempts.LOGOUT:
 				msg.put(DC.Keys.COMMAND, DC.Commands.LOGOUT);
 				msg.put(DC.Keys.METADATA, true);
 				break;
 			case Attempts.VIEW_DERIVATIVES:
 				if(ml == null)
 					ml = new MediaLoader();
 				
 				msg.put(DC.Keys.COMMAND, DC.Commands.VIEW_DERIVATIVES);
 				msg.put(DC.Keys.METADATA, ml.getDerivatives());
 				break;
 			case Attempts.LOAD_MEDIA:
 				try {
 					if(ml == null)
 						ml = new MediaLoader();
 					
 					msg.put(DC.Keys.COMMAND, DC.Commands.LOAD_MEDIA);
 					msg.put(DC.Keys.METADATA, ml.loadMedia((String) msg.opts.get(DC.Options._ID)));
 					
 				} catch (Exception e) {
 					Log(e.toString());
 				}
 				break;
 			case Attempts.SEARCH:
 				if(ml == null)
 					ml = new MediaLoader();
 				
 				msg.put(DC.Keys.COMMAND, DC.Commands.LOAD_SEARCH_RESULTS);
 				msg.put(DC.Keys.METADATA, ml.getSearchResults(msg.opts));
 				break;
 			case Attempts.SAVE_SEARCH:
 				msg.put(DC.Keys.COMMAND, DC.Commands.SAVE_SEARCH);
 				msg.put(DC.Keys.METADATA, ml.saveSearch(msg.opts));
 				break;
 			case Attempts.LOAD_SEARCH:
 				msg.put(DC.Keys.COMMAND, DC.Commands.LOAD_SEARCH);
 				msg.put(DC.Keys.METADATA, ml.getSearchResults(ml.search.loadSearch((String) msg.opts.get(DC.Options._ID))));
 				break;
 			case Attempts.RENAME_MEDIA:
 				msg.put(DC.Keys.COMMAND, DC.Commands.RENAME_MEDIA);
 				msg.put(DC.Keys.METADATA, ml.renameMedia((String) msg.opts.get(DC.Options._ID), (String) msg.opts.get(DC.Options._REV), (String) msg.opts.get(DC.Options.ALIAS)));
 				break;
 			case Attempts.ADD_ANNOTATION:
 				msg.put(DC.Keys.COMMAND, DC.Commands.ADD_ANNOTATION);
 				msg.put(DC.Keys.METADATA, ml.addAnnotation(msg.opts));
 				break;
 			case Attempts.APPEND_TO_ANNOTATION:
 				msg.put(DC.Keys.COMMAND, DC.Commands.APPEND_TO_ANNOTATION);
 				msg.put(DC.Keys.METADATA, ml.appendToAnnotation(msg.opts));
 				break;
 			case Attempts.SEND_MESSAGE:
 				msg.put(DC.Keys.COMMAND, DC.Commands.SEND_MESSAGE);
 				msg.put(DC.Keys.METADATA, ml.sendMessage(msg.opts));
 				break;
 			case Attempts.EDIT_ANNOTATION:
 				msg.put(DC.Keys.COMMAND, DC.Commands.EDIT_ANNOTATION);
 				msg.put(DC.Keys.METADATA, ml.modifyAnnotation(msg.opts));
 				break;
 			}
 		}
 		
 		remote.deliver(getServerSession(), "/desktopConnection", msg.out(), null);
 	}
 	
 	public static void Log(String l) {
 		System.out.println(LOG + ": " + l);
 	}
 }

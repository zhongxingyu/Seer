 /**
  * 
  */
 package uk.ac.horizon.ug.exploding.clientapi;
 
 import java.io.StringWriter;
 import java.util.Enumeration;
 
 import org.apache.log4j.Logger;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.xml.CompactWriter;
 
 import uk.ac.horizon.ug.exploding.db.ClientConversation;
 import uk.ac.horizon.ug.exploding.db.Game;
 import uk.ac.horizon.ug.exploding.db.Member;
 import uk.ac.horizon.ug.exploding.db.MessageToClient;
 import uk.ac.horizon.ug.exploding.db.Player;
 import uk.ac.horizon.ug.exploding.db.Zone;
 
 import equip2.core.DataspaceObjectEvent;
 import equip2.core.DataspaceObjectsEvent;
 import equip2.core.IDataspace;
 import equip2.core.IDataspaceObjectsListener;
 import equip2.core.ISession;
 import equip2.core.QueryTemplate;
 import equip2.spring.db.IDAllocator;
 
 /**
  * @author cmg
  *
  */
 public class ClientSubscriptionManager implements IDataspaceObjectsListener {
 	static Logger logger = Logger.getLogger(ClientSubscriptionManager.class.getName());
 
 	protected IDataspace dataspace;
 
 	public void setDataspace(IDataspace dataspace)
 	{
 		this.dataspace = dataspace;
 		initialiseListeners();
 	}
 
 	private void initialiseListeners() {
 		// TODO Auto-generated method stub
 		logger.info("Intialising ClientSubscriptionManager listener(s)");
 		// unhandled messages
 		QueryTemplate q= new QueryTemplate(uk.ac.horizon.ug.exploding.db.Message.class);
 		q.addConstraintNe("handled", true);
 		// add only! (includes initial listener add because handled will suppress duplicates)
 		dataspace.getEventManagement().addIDataspaceObjectsListener(this, q, DataspaceObjectsEvent.OBJECT_ADDED_MASK | DataspaceObjectsEvent.LISTENER_ADDED_MASK );
 		// Members - don't send initial on listener add because these should
 		// already be queued previously / on converation join
 		q = new QueryTemplate(uk.ac.horizon.ug.exploding.db.Member.class);
 		// add/update/delete
 		dataspace.getEventManagement().addIDataspaceObjectsListener(this, q, DataspaceObjectsEvent.OBJECT_ADDED_MASK | DataspaceObjectsEvent.OBJECT_MODIFIED_MASK | DataspaceObjectsEvent.OBJECT_REMOVED_MASK);
 		// Players 
 		q = new QueryTemplate(uk.ac.horizon.ug.exploding.db.Player.class);
 		// Changes only (sent to own client; sent on intial join)
 		dataspace.getEventManagement().addIDataspaceObjectsListener(this, q, DataspaceObjectsEvent.OBJECT_MODIFIED_MASK);
 
 		// Game Changes only (sent to own client; sent on intial join)
 		// - state changes, current year
 		q = new QueryTemplate(uk.ac.horizon.ug.exploding.db.Game.class);
 		dataspace.getEventManagement().addIDataspaceObjectsListener(this, q, DataspaceObjectsEvent.OBJECT_MODIFIED_MASK);
 	}
 
 	public IDataspace getDataspace()
 	{
 		return this.dataspace;
 	}
 	/** insert a new MessageToClient */
 	static void insertMessageToClient(ClientConversation conversation,
 			int type, boolean clientLifetime, Object oldVal, Object newVal, String updateClass, String updateID, String updateCategory, int priority, ISession session) {
 		try {
 			MessageToClient msg = new MessageToClient();
 			int seqNo = conversation.getNextSeqNo();
 			msg.setID(IDAllocator.getNewID(session, MessageToClient.class, "MC", null));
 			msg.setClientID(conversation.getClientID());
 			msg.setPlayerID(conversation.getPlayerID());
 			msg.setConversationID(conversation.getID());
 			msg.setGameID(conversation.getGameID());
 			msg.setSeqNo(seqNo);
 			msg.setClientLifetime(clientLifetime);
 			msg.setTime(System.currentTimeMillis());
 			msg.setType(type);
 			if (oldVal!=null) {
 				if (oldVal instanceof String)
 					// alredy marshalled
 					msg.setOldVal((String)oldVal);
 				else
 					msg.setOldVal(marshallFact(oldVal));
 			}
 			if (newVal!=null) {
 				if (newVal instanceof String)
 					// alredy marshalled
 					msg.setNewVal((String)newVal);
 				else
 					msg.setNewVal(marshallFact(newVal));
 			}
 			if (updateClass==null) {
 				if (newVal!=null) {
 					updateClass = getUpdateClass(newVal);
 					updateID = getUpdateID(newVal);
 					updateCategory = getUpdateCategory(newVal);
 				}
 				else if (oldVal!=null) {
 					updateClass = getUpdateClass(oldVal);
 					updateID = getUpdateID(oldVal);
 					updateCategory = getUpdateCategory(oldVal);
 				}
 			}
 			if (priority<=0) {
 				priority = getPriority(conversation, type, oldVal, newVal);
 			}
 			msg.setPriority(priority);
 			msg.setUpdateClass(updateClass);
 			msg.setUpdateID(updateID);
 			msg.setUpdateCategory(updateCategory);
 			msg.setAckedByClient(0L);
 			msg.setSentToClient(0L);
 
 			if (messageIsRedundant(conversation, msg, session)) {
 				logger.info("Suppress 'redundant' message for "+conversation.getClientID()+": "+msg);
 			}
 			else {
 				session.add(msg);
 				conversation.setNextSeqNo(seqNo+1);
 				//em.merge(conversation);
 				logger.info("Added message: "+msg);
 			}
 		} catch (Exception e)  {
 			logger.error("Unable to create/insert message to client", e);			
 		}
 	}
 
 	private static String marshallFact(Object oldVal) {
 		XStream xs = ClientController.getXStream();
 		StringWriter sw = new StringWriter();
 		xs.marshal(oldVal, new CompactWriter(sw));
 		return sw.getBuffer().toString();
 	}
 
 	public void handleConversationEnd(ClientConversation cc, ISession session) {
 		// remove old messages (unless lifetime = client)
 		QueryTemplate q = new QueryTemplate(MessageToClient.class);
 		q.addConstraintEq("conversationID", cc.getID());
 		q.addConstraintEq("clientLifetime", false);
 		Object mtcs [] = session.match(q);
 		for (int i=0; i<mtcs.length; i++) {
 			MessageToClient mtc = (MessageToClient)mtcs[i];
 			logger.debug("Remove old MessageToClient "+mtc.getID());
 			session.remove(mtcs[i]);
 		}
 		if (mtcs.length>0)
 			logger.info("Removed "+mtcs.length+" messages from now inactive conversation "+cc.getID());
 	}
 
 	public void handleConversationStart(ClientConversation cc, ISession session) {
     	insertMessageToClient(cc, MessageType.NEW_CONV.ordinal(), false, null, null, null, null, null, 0, session);
     	
     	// adopt any old messages for this clientId and sessionId
 		QueryTemplate q = new QueryTemplate(MessageToClient.class);
 		q.addConstraintEq("gameID", cc.getGameID());
 		q.addConstraintEq("clientID", cc.getClientID());
 		q.addConstraintEq("clientLifetime", true);
 		Object mtcs [] = session.match(q);
 		for (int i=0; i<mtcs.length; i++) {
 			MessageToClient mtc = (MessageToClient)mtcs[i];
 			logger.debug("Adopt old MessageToClient "+mtc.getID());
 			mtc.setConversationID(cc.getID());
 			// will need to resend to next client conversation
 			// TODO back-port
 			mtc.setSentToClient(0L);
 		}
 		if (mtcs.length>0)
 			logger.info("Adopted "+mtcs.length+" messages from inactive conversations into conversation "+cc.getID());
     	
 		handleSubscriptions(cc, session);
 	}
 	private void handleSubscriptions(ClientConversation cc, ISession session) {
 
 		// APPLICATION_SPECIFIC
     	// own Player
     	Player player = (Player)getClientProjection(cc, session.get(Player.class, cc.getPlayerID()));
 		insertMessageToClient(cc, MessageType.FACT_EX.ordinal(), false, null, player, null, null, null, 0, session);
 
     	// replicate Zones...
 		// repliace Game
     	Game game = (Game) getClientProjection(cc, session.get(Game.class, cc.getGameID()));
 		insertMessageToClient(cc, MessageType.FACT_EX.ordinal(), false, null, game, null, null, null, 0, session);
     	
     	//ContentGroup contentgame.getContentGroupID()
     	QueryTemplate q = new QueryTemplate(Zone.class);
     	q.addConstraintEq("contentGroupID", game.getContentGroupID());
     	sendInitialValues(cc, session, q);
     	
     	// initial messages
     	// unhandled only, time ordered
     	q = new QueryTemplate(uk.ac.horizon.ug.exploding.db.Message.class);
     	q.addConstraintEq("playerID", cc.getPlayerID());
     	q.addConstraintNe("handled", true);
     	q.addOrder("createTime", false);
     	// mark as handled
     	long now = System.currentTimeMillis();
     	Object match[] = session.match(q);
     	for (int zi=0; zi<match.length; zi++) {
     		uk.ac.horizon.ug.exploding.db.Message m = (uk.ac.horizon.ug.exploding.db.Message)getClientProjection(cc, match[zi]);
     		m.setHandled(true);
     		m.setHandledTime(now);
     		insertMessageToClient(cc, MessageType.FACT_EX.ordinal(), false, null, m, null, null, null, 0, session);
     	}
     	logger.info("Sent "+match.length+" unhandled Messages on new conversation");
     	//sendInitialValues(cc, session, q);
     	
     	// initial members of game
     	q = new QueryTemplate(Member.class);
     	q.addConstraintEq("gameID", cc.getGameID());
     	sendInitialValues(cc, session, q);
 	}
 
 	private void sendInitialValues(ClientConversation cc, ISession session,
 			QueryTemplate q) {
     	Object match[] = session.match(q);
     	for (int zi=0; zi<match.length; zi++) {
     		Object m = getClientProjection(cc, match[zi]);
     		insertMessageToClient(cc, MessageType.FACT_EX.ordinal(), false, null, m, null, null, null, 0, session);
     	}
     	logger.info("Sent "+match.length+" "+q.getQueryClass().getName()+" on new conversation");
 	}
 
 	public static Object unmarshallFact(String newVal) {
 		XStream xs = ClientController.getXStream();
 		return xs.fromXML(newVal);
 	}
 
 	@Override
 	public void objectsChanged(DataspaceObjectsEvent dose) {
 		Enumeration en = dose.getDataspaceObjectEvents();
 		while (en.hasMoreElements()) {
 			DataspaceObjectEvent doe = (DataspaceObjectEvent)en.nextElement();
 			switch(doe.getApparentChange()) {
 			case DataspaceObjectEvent.ADDED:
 				handleSessionEvent(MessageType.FACT_ADD, null, doe.getNewValue());
 				break;
 			case DataspaceObjectEvent.MODIFIED:
 				handleSessionEvent(MessageType.FACT_UPD, doe.getOldValue(), doe.getNewValue());
 				break;
 			case DataspaceObjectEvent.REMOVED:
 				handleSessionEvent(MessageType.FACT_DEL, doe.getOldValue(), null);
 				break;				
 			}
 		}
 	}
 
 	private void handleSessionEvent(MessageType type, Object oldObject,
 			Object newObject) {
 		// TODO Auto-generated method stub
 		ISession session = dataspace.getSession();
 		session.begin();
 		try {
 			// not sure how enums are mapped...
     		QueryTemplate q = new QueryTemplate(ClientConversation.class);
     		//em.createQuery ("SELECT x FROM ClientConversation x WHERE x.status = :status");
     		q.addConstraintEq("active", true);
     		//q.setParameter("status", ConversationStatus.ACTIVE);
     		Object conversations [] = session.match(q);
 			
 			// each client type...
     		String oldValue = null, newValue = null;
     		String updateClass = null, updateID = null, updateCategory = null;
     		
     		// client independent match first
     		// should only get relevant classes!
     		//if (!matches(pattern, oldObject!=null ? oldObject : newObject, null, droolsSession.getKsession().getKnowledgeBase()))
     		//continue;
 					
     		// each client...
     		nextConversation:
     		for (int ci=0; ci<conversations.length; ci++) {
     			ClientConversation conversation  = (ClientConversation)conversations[ci];
     			// client-dependent match
     			Object matchObject = oldObject!=null ? oldObject : newObject;
 				if (!matches(matchObject, conversation))
 						continue;
 				// chance to filter values on the way to the client
 				boolean clientSpecificProjection = false;
 				Object newProj = getClientProjection(conversation, newObject);
 				Object oldProj = getClientProjection(conversation, oldObject);
 				// exact equals can be left for other clients; otherwise we need to send this value to only this client...
 				if (!(newProj==newObject)) {
 					clientSpecificProjection = true;
 					newValue = null;
 				}
 				if (!(oldProj==oldObject)) {
 					clientSpecificProjection = true;
 					oldValue = null;
 				}
 				if (type==MessageType.FACT_UPD && clientSpecificProjection) {
 					// we might be able to suppress this update if the client doesn't see the difference
 					if (oldProj!=null && newProj!=null && oldProj.equals(newProj)) {
 						logger.debug("Suppress update "+oldObject+" -> "+newObject+" on client projection for "+conversation.getClientID()+" as "+oldProj+" -> "+newProj);
 						continue nextConversation;
 					}
 //					else 
 //						logger.debug("Don't suppress update "+oldObject+" -> "+newObject+" on client projection for "+conversation.getClientID()+" as "+oldProj+" -> "+newProj+" - not equal");
 				}
 				// marshall on demand
 				if (newProj!=null && newValue==null) {
 					if (updateClass==null) {
 						updateClass = getUpdateClass(newObject);
 						updateID = getUpdateID(newObject);
 						updateCategory = getUpdateCategory(newObject);
 					}
 					newValue = marshallFact(newProj);
 				}
 				if (oldProj!=null && oldValue==null) {
 					if (updateClass==null) {
 						updateClass = getUpdateClass(oldObject);
 						updateID = getUpdateID(oldObject);
 						updateCategory = getUpdateCategory(oldObject);
 					}
 					oldValue = marshallFact(oldProj);
 				}
 				// add message
 				boolean clientLifetime = false;
 				// APPLICATION-SPECIFIC
 				if (matchObject instanceof uk.ac.horizon.ug.exploding.db.Message) {
 					uk.ac.horizon.ug.exploding.db.Message message = (uk.ac.horizon.ug.exploding.db.Message)matchObject;
 					// handled
 					message.setHandled(true);
 					message.setHandledTime(System.currentTimeMillis());
 					session.addOrUpdate(message);
 					clientLifetime = true;
 				}
 				// END APPLICATION-SPECIFIC
 				int priority = getPriority(conversation, type.ordinal(), oldObject, newObject);
 				// clobber/merge with existing messages
 				boolean insertMessage = true;
 				// TODO	clobber/merge with existing messages
 				if (type==MessageType.FACT_UPD && updateID!=null && updateClass!=null && oldValue!=null && newValue!=null) {
 					// just updates for now??
 					// MTCs of type FACT_UPD for this Conversation with this objectClass and objectID,
 					// not yet sent to client, sorted by seqNo
 //					MessageToClient mtc;
 //					mtc.getConversationID();
 //					mtc.getType();
 //					mtc.getUpdateID();
 //					mtc.getUpdateClass();
 //					mtc.getSeqNo();
 //					mtc.getSentToClient();
 					QueryTemplate uqt = new QueryTemplate(MessageToClient.class);
 					uqt.addConstraintEq("updateID", updateID);
 					uqt.addConstraintEq("conversationID", conversation.getID());
 					uqt.addConstraintEq("sentToClient", 0L);
 					uqt.addConstraintEq("type", MessageType.FACT_UPD.ordinal());
 					uqt.addConstraintEq("updateClass", updateClass);
 //					uqt.addOrder("seqNo", false);
 					Object mtcs[] = session.match(uqt);
 					if (mtcs.length==1) {
 						MessageToClient mtc = (MessageToClient)mtcs[0];
 						if (oldValue.equals(mtc.getNewVal())) {
 							mtc.setNewVal(newValue);
 							logger.debug("Suppress update "+oldProj+" -> "+newProj+" by rewriting pending update "+mtc);
 							insertMessage = false;
 						} else
 							logger.warn("Suppress update "+oldProj+" -> "+newProj+" had old new value "+mtc.getNewVal()+" - in pending update "+mtc);
 					}
 					else if (mtcs.length>1) {
 						logger.warn("Suppress update for "+newProj+" found "+mtcs.length+" old matching updates");
 					}
 				}
 				// actual message
 				if (insertMessage)
 					insertMessageToClient(conversation, type.ordinal(), clientLifetime, oldValue, newValue, updateClass, updateID, updateCategory, priority, session);
 				// tidy up projections
 				if (clientSpecificProjection) {
 					oldValue = null;
 					newValue = null;
 				}
     		}
     		session.end();
 		}
 		catch (Exception e) {
 			session.abort();
 			logger.warn("Problem handling session event", e);
 		}
 	}
 
 	private boolean matches(Object matchObject, ClientConversation conversation) {
 		// APPLICATION-SPECIFIC
 		// tell own game
 		if (matchObject instanceof Game) {
 			Game game = (Game)matchObject;
 			if (game.getID()!=null && game.getID().equals(conversation.getGameID()))
 				return true;
 			return false;
 		}
 		
 		// tell own (Player's) Messages
 		if (matchObject instanceof uk.ac.horizon.ug.exploding.db.Message) {
 			uk.ac.horizon.ug.exploding.db.Message message = (uk.ac.horizon.ug.exploding.db.Message)matchObject;
 			if (message.getPlayerID()!=null && message.getPlayerID().equals(conversation.getPlayerID()))
 				return true;
 			return false;
 		}
 		// tell game's Members
 		if (matchObject instanceof Member) {
 			Member member = (Member)matchObject;
 			if (member.getGameID()!=null && member.getGameID().equals(conversation.getGameID()))
 				return true;
 			return false;
 		}
 		// tell own Player
 		if (matchObject instanceof Player) {
 			Player player = (Player)matchObject;
 			if (player.getID()!=null && player.getID().equals(conversation.getPlayerID()))
 				return true;
 			return false;
 		}
 		logger.warn("Matches for unknown class "+matchObject.getClass().getName());
 		return false;
 	}
 
 	/** application-specific: return "classname" for update suppression */
 	public static String getUpdateClass(Object value) {
 		if (value==null)
 			return null;
 		return value.getClass().getName();
 	}
 	/** application-specific: return object class subgroup identifier, if relevant, for update suppression */
 	private static String getUpdateCategory(Object value) {
 		if (value==null)
 			return null;
 		if (value instanceof uk.ac.horizon.ug.exploding.db.Message) {
 			uk.ac.horizon.ug.exploding.db.Message m = (uk.ac.horizon.ug.exploding.db.Message)value;
 			if (uk.ac.horizon.ug.exploding.db.Message.MSG_TIMELINE_CONTENT.equals(m.getType()) ||
 					uk.ac.horizon.ug.exploding.db.Message.MSG_TIMELINE_CONTENT_GLOBAL.equals(m.getType()))
 				return null;
 			// "contextual message"
 			return "contextual";						
 		}
 		return null;
 	}
 
 	/** application-specific: return "objectID" for update suppression */
 	public static String getUpdateID(Object o) {
 		if (o==null)
 			return null;
 		if (o instanceof Player) {
 			Player p = (Player)o;
 			return p.getID();
 		}
 		if (o instanceof uk.ac.horizon.ug.exploding.db.Message) {
 			uk.ac.horizon.ug.exploding.db.Message p = (uk.ac.horizon.ug.exploding.db.Message)o;
 			return p.getID();
 		}
 		if (o instanceof Member) {
 			Member p = (Member)o;
 			return p.getID();
 		}
 		if (o instanceof Zone) {
 			Zone p = (Zone)o;
 			return p.getID();
 		}
 		if (o instanceof Game) {
 			Game p = (Game)o;
 			return p.getID();
 		}
 		logger.warn("getUpdateID for unknown class "+o.getClass().getName());
 		return null; //!?!		
 	}
 	/** application-specific: priority levels */
 	static enum AppPriority {
 		OTHERS_MEMBERS,
 		CONTEXT_MESSAGES,
 		TIMELINE_MESSAGES,
 		YOUR_MEMBERS_SYSTEM_CHANGES,
 		MUST_SEND, // the ones after this we REALLY want to send
 		YOUR_MEMBERS_USER_CHANGES, // pick/place, user create
 		GLOBAL_MESSAGES,
 		ZONES, // required at start
 		YOUR_GAME, // year
 		YOUR_PLAYER, // can author/create
 	}
 	/** application-specific: return priority for delivery to client */
 	public static int getPriority(ClientConversation cc, int typeOrdinal, Object oldVal, Object newVal) {
 		Object val = newVal!=null ? newVal : oldVal;
 		if (val instanceof Player)
 			// you only get your own player anyway
 			return AppPriority.YOUR_PLAYER.ordinal();
 		if (val instanceof Game) 
 			// you only get your own game anyway
 			return AppPriority.YOUR_GAME.ordinal();
 		if (val instanceof Zone)
 			return AppPriority.ZONES.ordinal();
 		if (val instanceof uk.ac.horizon.ug.exploding.db.Message) {
 			uk.ac.horizon.ug.exploding.db.Message m = (uk.ac.horizon.ug.exploding.db.Message)val;
 			if (uk.ac.horizon.ug.exploding.db.Message.MSG_TIMELINE_CONTENT.equals(m.getType()))
 				return AppPriority.TIMELINE_MESSAGES.ordinal();
 			if (uk.ac.horizon.ug.exploding.db.Message.MSG_TIMELINE_CONTENT_GLOBAL.equals(m.getType()))
 				return AppPriority.GLOBAL_MESSAGES.ordinal();
 			return AppPriority.CONTEXT_MESSAGES.ordinal();						
 		}
 		if (val instanceof Member) {
 			Member m = (Member)val;
 			if (cc.getPlayerID().equals(m.getPlayerID())) {
 				// can we spot a player-driven change?
 				Member nm = (Member)newVal;
 				Member om = (Member)oldVal;
 				// pick/drop?
 				if (om==null && nm.getCarried()==false)
 					// player member creation
 					return AppPriority.YOUR_MEMBERS_USER_CHANGES.ordinal();
 				if (nm!=null && om!=null && om.getCarried()!=nm.getCarried()) 
 					// pick/drop
 					return AppPriority.YOUR_MEMBERS_USER_CHANGES.ordinal();
 				return AppPriority.YOUR_MEMBERS_SYSTEM_CHANGES.ordinal();
 			}
 			else
 				return AppPriority.OTHERS_MEMBERS.ordinal();
 		}
 		logger.warn("getPriority for unknown class "+val);
 		// default
 		return 1;
 	}
 	/** application-specific: project values for client */
 	private Object getClientProjection(ClientConversation cc, Object value) {
 		if (value instanceof Member) {
 			Member m = (Member)value;
 			if (!cc.getPlayerID().equals(m.getPlayerID())) {
 				// don't give other player's member attributes
 				Member nm = new Member();
 				nm.setID(m.getID());
 				nm.setCarried(m.getCarried());
 				nm.setColourRef(m.getColourRef());
 				nm.setGameID(m.getGameID());
 				nm.setLimbData(m.getLimbData());
 				nm.setName(m.getName());
 				nm.setParentMemberID(m.getParentMemberID());
 				nm.setPlayerID(m.getPlayerID());
 				nm.setPosition(m.getPosition());
 				nm.setZone(m.getZone());
 				logger.debug("getClientProjection for player "+cc.getPlayerID()+": "+m+" -> "+nm);
 				return nm;
 			}
 		}
 		// no change
 		return value;
 	}
 	/** application-specific: is there no point sending this message at all */
 	private static boolean messageIsRedundant(ClientConversation conversation,
 			MessageToClient msg, ISession session) {
 		// client will only want one Contextual message each poll
 		if (MessageType.FACT_ADD.ordinal()==msg.getType() && 
 				uk.ac.horizon.ug.exploding.db.Message.class.getName().equals(msg.getUpdateClass()) &&
 				msg.getUpdateCategory()!=null) {
 			QueryTemplate qt = new QueryTemplate(MessageToClient.class);
 			//MessageToClient mtc; mtc.getConversationID(); mtc.getSentToClient(); 
 			qt.addConstraintEq("conversationID", conversation.getID());
 			qt.addConstraintEq("updateCategory", msg.getUpdateCategory());			
 			qt.addConstraintEq("updateClass", msg.getUpdateClass());
 			int count = session.count(qt);
 			if (count>0) {
 				logger.debug("mesageIdRedundant: found "+count+" MTCs with "+msg.getUpdateClass()+" category "+msg.getUpdateCategory());
 				return true;
 			}
 		}
 		// be careful :-)
 		return false;
 	}
 }

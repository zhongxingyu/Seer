 /**
  * Copyright (c) 2011, SOCIETIES Consortium (WATERFORD INSTITUTE OF TECHNOLOGY (TSSG), HERIOT-WATT UNIVERSITY (HWU), SOLUTA.NET 
  * (SN), GERMAN AEROSPACE CENTRE (Deutsches Zentrum fuer Luft- und Raumfahrt e.V.) (DLR), Zavod za varnostne tehnologije
  * informacijske družbe in elektronsko poslovanje (SETCCE), INSTITUTE OF COMMUNICATION AND COMPUTER SYSTEMS (ICCS), LAKE
  * COMMUNICATIONS (LAKE), INTEL PERFORMANCE LEARNING SOLUTIONS LTD (INTEL), PORTUGAL TELECOM INOVAÇÃO, SA (PTIN), IBM Corp., 
  * INSTITUT TELECOM (ITSUD), AMITEC DIACHYTI EFYIA PLIROFORIKI KAI EPIKINONIES ETERIA PERIORISMENIS EFTHINIS (AMITEC), TELECOM 
  * ITALIA S.p.a.(TI),  TRIALOG (TRIALOG), Stiftelsen SINTEF (SINTEF), NEC EUROPE LTD (NEC))
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
  * conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *    disclaimer in the documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
  * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.societies.enterprise.collabtools.runtime;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.index.Index;
 import org.neo4j.helpers.collection.IterableWrapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.societies.enterprise.collabtools.acquisition.LongTermCtxTypes;
 import org.societies.enterprise.collabtools.acquisition.Person;
 import org.societies.enterprise.collabtools.acquisition.RelTypes;
 import org.societies.enterprise.collabtools.acquisition.ShortTermCtxTypes;
 
 /**
  * Class in charge to manage collaborative sessions
  *
  * @author cviana
  *
  */
 public class SessionRepository implements Observer {
 
 	private final GraphDatabaseService graphDb;
 	private final Index<Node> indexSession;
 	private final Node sessionRefNode;
 	private CollabApps collabApps;
 	private String language = "English";
 	private static final Logger logger = LoggerFactory.getLogger(SessionRepository.class);
 
 	public SessionRepository(GraphDatabaseService graphDb, Index<Node> indexSession, CollabApps collabApps)
 	{
 		this.graphDb = graphDb;
 		this.indexSession = indexSession;
 		this.collabApps = collabApps;
 
 		this.sessionRefNode = getSessionRootNode(graphDb);
 	}
 
 	private Node getSessionRootNode(GraphDatabaseService graphDb)
 	{
 		Relationship rel = graphDb.getReferenceNode().getSingleRelationship(
 				RelTypes.REF_SESSIONS, Direction.OUTGOING);
 		if (rel != null)
 		{
 			return rel.getEndNode();
 		}
 
 		Transaction tx = this.graphDb.beginTx();
 		try
 		{
 			Node refNode = this.graphDb.createNode();
 			this.graphDb.getReferenceNode().createRelationshipTo(refNode, 
 					RelTypes.REF_SESSIONS);
 			tx.success();
 			return refNode;
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 
 	public Session getSessionByName(String sessionName)
 	{
 		Node sessionNode = (Node)this.indexSession.get(Session.SESSION, sessionName).getSingle();
 		if (sessionNode == null)
 		{
 			throw new IllegalArgumentException("Session[" + sessionName + "] not found");
 		}
 		return new Session(sessionNode, this.collabApps);
 	}
 
 	public void update(Observable o, Object arg)
 	{
 		Person person = (Person)arg;
 		//TODO: Fix for "has_session"
 		//		logger.info(person.getLongTermCtx("has_sessions"));
 		String sessionName = person.getLastShortTermUpdate().getShortTermCtx(ShortTermCtxTypes.LOCATION);
 
 		if (isInSession(person, sessionName))
 		{
 			List<String> personList = new ArrayList<String>();
 			Iterator<Person> it = getSessionByName(sessionName).getMembers();
 			while (it.hasNext()) {
 				personList.add(((Person)it.next()).getName());
 			}
 			System.out.println(sessionName+" Session graph before: " + personList.toString());
 
 			getSessionByName(sessionName).removeMember(person);
 
 			it = getSessionByName(sessionName).getMembers();
 			personList.removeAll(personList);
 			while (it.hasNext()) {
 				personList.add(((Person)it.next()).getName());
 			}
 			System.out.println(sessionName+" Session graph after: " + personList.toString());
 		}
 	}
 
 	private synchronized boolean isInSession(Person person, String session)
 	{
 		Iterator<Node> personNode = this.indexSession.get(LongTermCtxTypes.NAME, session).iterator();
 		while (personNode.hasNext()) {
 			Node temp = (Node)personNode.next();
 			if (person.getName().equals(new Person(temp).getName()))
 				return true;
 		}
 		return false;
 	}
 
 	public boolean containSession(String sessionName)
 	{
 		Node temp = (Node)this.indexSession.get(Session.SESSION, sessionName).getSingle();
 		if (temp == null) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Create a collaborative sessions if doesn't exists
 	 * @param Session name
 	 * @return A session object
 	 */
 	public Session createSession(String sessionName)
 	{
 		Transaction tx = this.graphDb.beginTx();
 		try
 		{
 			Node newSessionNode = this.graphDb.createNode();
 			this.sessionRefNode.createRelationshipTo(newSessionNode, RelTypes.A_SESSION);
 
 			Node sessionAlreadyExist = (Node)this.indexSession.get(Session.SESSION, sessionName).getSingle();
 			if (sessionAlreadyExist != null)
 			{
 				tx.failure();
 				try {
 					throw new Exception("Session with this name already exists ");
 				}
 				catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			//Add session
 			newSessionNode.setProperty(Session.SESSION, sessionName);
 			this.indexSession.add(newSessionNode, Session.SESSION, sessionName);
 			tx.success();
 			Session session = new Session(newSessionNode, this.collabApps);
 			
 			//Setting language
 			System.out.println("Setting session language: "+language );
 			session.setLanguage(language);
 
 			logger.info("Session created: " + sessionName);
 			System.out.println("Session created: " + sessionName);
 			return session;
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 
 
 	/**
 	 * Include members in a session previously created
 	 * @param Session name
 	 * @param Members to participate
 	 * @return Members include in the session if necessary
 	 */
 	public synchronized String[] addMembers(String sessionName, HashSet<Person> members)
 	{
 		List<String> membersIncluded = new ArrayList<String>();
 		
 		//Add member to the session
 		List<Person> membersPersonList = new ArrayList<Person>();
 		Iterator<Person> personIterator = members.iterator();
 		while (personIterator.hasNext()) {
 			Person person = (Person)personIterator.next();
 			//Verify if person is already in session
 			if (!isInSession(person, sessionName)) {
 				person.addSession(sessionName);  
 				membersPersonList.add(person);				
 				//Insert names in string name for async app	
 				membersIncluded.add(person.getName());
 			}
 		}
 		//Including in session if not yet present
 		personIterator = membersPersonList.iterator();
 		boolean sessionChanges = false;
 
 
 		while (personIterator.hasNext()) {
 			//TODO: Implement floor control
 			Person person = (Person)personIterator.next();		
 			getSessionByName(sessionName).addMember(person, Session.LISTENER);
 			sessionChanges = true;
             System.out.println("Inviting new members...");
 		}
 		
 		//Insert members in session history if session had changes
 		if (sessionChanges) {
 			Iterator<Person> it = getSessionByName(sessionName).getMembers();
 			List<String> membersList = new ArrayList<String>();
 			while (it.hasNext()) {
 				membersList.add(((Person)it.next()).getName());
 			}
 			HashMap<String, String[]> ctxSessionHistory = new HashMap<String, String[]>();
 			String [] historyPeople = membersList.toArray(new String[0]);
 			System.out.println("members to the session history: " +Arrays.toString(historyPeople));
 			ctxSessionHistory.put(Session.MEMBERS, historyPeople);
 			getSessionByName(sessionName).addSessionHistoryStatus(ctxSessionHistory);
 			//Returning members which were inserted in a existed session
 			return historyPeople;
 		}
 		//Returning members which were inserted in a new session
 		return membersIncluded.toArray(new String[0]);
 	}
 	
     public Iterable<Session> getAllSessions()
     {
         return new IterableWrapper<Session, Relationship>(
         		sessionRefNode.getRelationships(RelTypes.A_SESSION) )
         {
             @Override
             protected Session underlyingObjectToObject(Relationship sessionRel)
             {
                 return new Session(sessionRel.getEndNode(), collabApps);
             }
         };
     }
 
 	/**
 	 * @param string language of sessions
 	 */
 	public void setLanguage(String language) {
		if (language != null){
			this.language = language;
			System.out.println("*Setting language: "+language );
		}
		else {
			System.out.println("*Setting language: "+language );
		}
 	}
 
 }

 /****************************************************************************
  * Copyright (c) 2008 Composent, Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Composent, Inc. - initial API and implementation
  *****************************************************************************/
 
 package org.eclipse.ecf.tests.provider.twitter;
 
 import java.text.ParseException;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.ecf.core.IContainer;
 import org.eclipse.ecf.core.identity.IDFactory;
 import org.eclipse.ecf.core.user.IUser;
 import org.eclipse.ecf.core.util.ECFException;
 import org.eclipse.ecf.presence.IPresenceContainerAdapter;
 import org.eclipse.ecf.presence.roster.IRoster;
 import org.eclipse.ecf.presence.roster.IRosterManager;
 import org.eclipse.ecf.presence.search.ICriteria;
 import org.eclipse.ecf.presence.search.ICriterion;
 import org.eclipse.ecf.presence.search.IRestriction;
 import org.eclipse.ecf.presence.search.IResult;
 import org.eclipse.ecf.presence.search.IResultList;
 import org.eclipse.ecf.presence.search.ISearch;
 import org.eclipse.ecf.presence.search.message.IMessageSearchCompleteEvent;
 import org.eclipse.ecf.presence.search.message.IMessageSearchEvent;
 import org.eclipse.ecf.presence.search.message.IMessageSearchListener;
 import org.eclipse.ecf.presence.search.message.IMessageSearchManager;
 import org.eclipse.ecf.provider.twitter.container.IStatus;
 import org.eclipse.ecf.provider.twitter.container.TwitterContainer;
 import org.eclipse.ecf.provider.twitter.search.ITweetItem;
 import org.eclipse.ecf.tests.ContainerAbstractTestCase;
 
 /**
  *
  */
 public class TwitterContainerTests extends ContainerAbstractTestCase {
 
 	ISearch searchResult = null;
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.ecf.tests.ContainerAbstractTestCase#getClientContainerName()
 	 */
 	protected String getClientContainerName() {
 		return Twitter.CONTAINER_NAME;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ecf.tests.ContainerAbstractTestCase#setUp()
 	 */
 	protected void setUp() throws Exception {
 		super.setUp();
 		clients = createClients();
 		serverID = IDFactory.getDefault().createID(clients[0].getConnectNamespace(), getUsername(0));
 		connectClients();
 	}
 
 	/* (non-Javadoc)
 	 * @see junit.framework.TestCase#tearDown()
 	 */
 	protected void tearDown() throws Exception {
 		disconnectClients();
 		cleanUpClients();
 	}
 
 	public void testTweetSearchAsync() throws ECFException, InterruptedException{
 		final IContainer client = getClient(0);
 		IPresenceContainerAdapter container = (IPresenceContainerAdapter)client.getAdapter(IPresenceContainerAdapter.class);
 		IMessageSearchManager messageManager = container.getChatManager().getMessageSearchManager();
 		
 		assertNotNull(messageManager);
 
 		IRestriction selection = messageManager.createRestriction();
 		
 		assertNotNull(selection);
 
 		//for twitter is not necessary to provide the field
 		ICriterion name = selection.eq("", "eclipse");
 
 		// create a specific criteria
 		final ICriteria criteria = messageManager.createCriteria();
 		assertNotNull(criteria);
 		criteria.add(name);
 		
 		IMessageSearchListener listenerCompleted = new IMessageSearchListener(){
 			public void handleMessageSearchEvent(IMessageSearchEvent event) {
 				if (event instanceof IMessageSearchCompleteEvent){
 					searchResult = ((IMessageSearchCompleteEvent)event).getSearch();
 				}
 			}
 		};
 		
 		// call the non-block search
 		messageManager.search(criteria, listenerCompleted);
 		
 		Thread.sleep(5000);
 		
 		//put the completion result on the search handle
 		assertNotNull(searchResult);
 
 		// check if there is at least one result
 		assertTrue(0 != searchResult.getResultList().getResults().size());
 
 				
 	}
 	
 	public void testTweetSearchSync() throws ECFException{
 		final IContainer client = getClient(0);
 		IPresenceContainerAdapter container = (IPresenceContainerAdapter)client.getAdapter(IPresenceContainerAdapter.class);
 		IMessageSearchManager messageManager = container.getChatManager().getMessageSearchManager();
 
 		//for twitter is not necessary to provide the field
 		ICriterion criterion = messageManager.createRestriction().eq("", "eclipse");
 		ICriteria criteria = messageManager.createCriteria();
 		criteria.add(criterion);
 		ISearch search = messageManager.search(criteria);
 		
 		assertNotNull(search);
 		IResultList results = search.getResultList();
 		assertTrue(results.getResults().size() > 0);
 		Collection c = results.getResults();
 		for (Iterator iterator = c.iterator(); iterator.hasNext();) {
 			IResult object = (IResult) iterator.next();
 			ITweetItem item = (ITweetItem)object;
 			System.out.println(item.getText());
 			
 		}
 	}
 	
 
 	public void testGetAdapter() throws Exception {
 		final IContainer client = getClient(0);
 		final IPresenceContainerAdapter presenceAdapter = (IPresenceContainerAdapter) client.getAdapter(IPresenceContainerAdapter.class);
 		assertNotNull(presenceAdapter);
 		final IRosterManager rosterManager = presenceAdapter.getRosterManager();
 		final IRoster roster = rosterManager.getRoster();
 		assertNotNull(roster);
 	}
 	
 	public void testTimeLine() throws ECFException{
 		final IContainer client = getClient(0);
 		TwitterContainer container = (TwitterContainer)client.getAdapter(TwitterContainer.class);
 		List l = container.getFriendsTimeline();
 	}
 	
 	
 	public void testFollowings() throws ECFException{
 		final IContainer client = getClient(0);
 		TwitterContainer container = (TwitterContainer)client.getAdapter(TwitterContainer.class);
 		List<IUser> l = container.getFollowing();
 		assertTrue(l.size() > 0);
 		
 	}
 	
 	public void testFollowers() throws ECFException{
 		final IContainer client = getClient(0);
 		TwitterContainer container = (TwitterContainer)client.getAdapter(TwitterContainer.class);
 		List<IUser> l = container.getFollowers();
 		Iterator<IUser> it = l.iterator();
 		while (it.hasNext()) {
 			IUser type = it.next();
 			System.out.println(type.getName());
 			
 		}
 		
 	}
 	
 	public void testTimeLineSince() throws ECFException, ParseException{
 		final IContainer client = getClient(0);
 		TwitterContainer container = (TwitterContainer)client.getAdapter(TwitterContainer.class);
 		List l = container.getFriendsTimeline(2);
 		for (Iterator iterator = l.iterator(); iterator.hasNext();) {
 			IStatus object = (IStatus) iterator.next();
 			System.out.println(object.getText());
 			
 		}
 	}
 
 	public void testUserTimeLine() throws ECFException{
 		System.out.println("");
 		final IContainer client = getClient(0);
 		TwitterContainer container = (TwitterContainer)client.getAdapter(TwitterContainer.class);
 		List l = container.getUserTimeline();
 		for (Iterator iterator = l.iterator(); iterator.hasNext();) {
 			IStatus object = (IStatus) iterator.next();
 			System.out.println(object.getText());
 			
 		}
 	}
 	
	public void testUrlShorten() throws ECFException{

			final IContainer client = getClient(0);
			TwitterContainer container = (TwitterContainer)client.getAdapter(TwitterContainer.class);
			String url = "http://wiki.eclipse.org/index.php/Eclipse_Communication_Framework_Project";
			String shortenUrl = container.getUrlShorten(url);
			assertNotSame("Shorten URL are the same", shortenUrl, url);
			
	}
 
 	
 
 
 	
 }

 /*******************************************************************************
  *  Copyright (c) 2010 Weltevree Beheer BV, Nederland
  *
  *  All rights reserved. This program and the accompanying materials
  *  are made available under the terms of the Eclipse Public License v1.0
  *  which accompanies this distribution, and is available at
  *  http://www.eclipse.org/legal/epl-v10.html
  *
  *  Contributors:
  *     Wim Jongman - initial API and implementation
  *
  *
  *******************************************************************************/
 package org.eclipse.ecf.protocol.nntp.store.derby.internal;
 
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.eclipse.ecf.protocol.nntp.core.ArticleFactory;
 import org.eclipse.ecf.protocol.nntp.core.NewsgroupFactory;
 import org.eclipse.ecf.protocol.nntp.core.ServerFactory;
 import org.eclipse.ecf.protocol.nntp.model.IArticle;
 import org.eclipse.ecf.protocol.nntp.model.ICredentials;
 import org.eclipse.ecf.protocol.nntp.model.INewsgroup;
 import org.eclipse.ecf.protocol.nntp.model.ISecureStore;
 import org.eclipse.ecf.protocol.nntp.model.IServer;
 import org.eclipse.ecf.protocol.nntp.model.IStore;
 import org.eclipse.ecf.protocol.nntp.model.IStoreEvent;
 import org.eclipse.ecf.protocol.nntp.model.IStoreEventListener;
import org.eclipse.ecf.protocol.nntp.model.IStoreFactory;
 import org.eclipse.ecf.protocol.nntp.model.NNTPException;
 import org.eclipse.ecf.protocol.nntp.model.NNTPIOException;
 import org.eclipse.ecf.protocol.nntp.model.SALVO;
 import org.eclipse.ecf.protocol.nntp.model.StoreException;
 import org.eclipse.ecf.protocol.nntp.model.UnexpectedResponseException;
 import org.eclipse.ecf.protocol.nntp.store.derby.StoreFactory;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class StoreTest {
 
 	static String[] overviewHeaders = new String[] {
 			"1	=?utf-8?Q?ReportScripting=20in=20BIRT=202.2.1=20:=20Problem=20wit?= =?utf-8?Q?h=20the=20evaluation=20of=20an=20Expression?=	Maghen Calinghee <maghen.calinghee@zenika.com>	Fri, 09 Oct 2009 08:00:06 -0400	<han8k7$9m7$1@build.eclipse.org>		1313	9	Xref: build.eclipse.org eclipse.birt:01",
 			"2	=?utf-8?Q?dynamic=20grouping=20of=20charts=20possible=3F?=	<a.reiners@reply.eu>	Fri, 09 Oct 2009 08:22:23 -0400	<han9u0$obp$1@build.eclipse.org>		1408	14	Xref: build.eclipse.org eclipse.birt:02",
 			"3	Re: How to force page break after group in HTML but not in PDF?	Thilo Bruesshaber <thilo.bruesshaber@gmail.com>	Fri, 09 Oct 2009 14:24:04 +0200	<hana0t$ov4$1@build.eclipse.org>	<ha4at4$dq4$1@build.eclipse.org> <halm91$a9f$1@build.eclipse.org>	2057	39	Xref: build.eclipse.org eclipse.birt:03",
 			"4	Re: Problems with ReportEngine after Upgrading Birt from 2.3 to 2.5.1	\"Gerald Ploner\" <gerald.ploner@innovations.de>	Fri, 9 Oct 2009 16:17:29 +0200	<hangml$5vi$1@build.eclipse.org>	<haldiv$42p$1@build.eclipse.org> <hamiqa$qkd$1@build.eclipse.org>	2870	48	Xref: build.eclipse.org eclipse.birt:04",
 			"5	=?utf-8?Q?Exceptions=20-=20while=20using=20Bar=20Charts=20etc=20i?= =?utf-8?Q?n=20BIRT=20Reports?=	Venkat  <venkatre@gmail.com>	Fri, 09 Oct 2009 10:56:51 -0400	<hanivl$t7i$1@build.eclipse.org>		10675	109	Xref: build.eclipse.org eclipse.birt:05",
 			"6	=?utf-8?Q?List=20Box=20report=20parameters=20-=20access=20to=20Di?= =?utf-8?Q?splay=20Text=3F?=	Craig M <craig.mcdaniel@db.com>	Fri, 09 Oct 2009 11:08:47 -0400	<hanjm1$9nh$1@build.eclipse.org>		1395	5	Xref: build.eclipse.org eclipse.birt:06",
 			"7	=?utf-8?Q?Re:=20Curve=20fitting=20line=20-=20null=20values=20as?= =?utf-8?Q?=20zero=20(2.5.1)?=	Michael  <mwilliams@actuate.com>	Fri, 09 Oct 2009 12:07:09 -0400	<hann3g$7qt$1@build.eclipse.org>	<ha4efk$dii$1@build.eclipse.org>	988	9	Xref: build.eclipse.org eclipse.birt:07",
 			"8	=?utf-8?Q?Re:=20How=20to=20force=20page=20break=20after=20group?= =?utf-8?Q?=20in=20HTML=20but=20not=20in=20PDF=3F?=	Michael  <mwilliams@actuate.com>	Fri, 09 Oct 2009 12:10:54 -0400	<hannah$bnu$1@build.eclipse.org>	<hana0t$ov4$1@build.eclipse.org>	1131	10	Xref: build.eclipse.org eclipse.birt:08",
 			"9	=?utf-8?Q?Re:=20List=20Box=20report=20parameters=20-=20access=20t?= =?utf-8?Q?o=20Display=20Text=3F?=	Michael  <mwilliams@actuate.com>	Fri, 09 Oct 2009 12:14:47 -0400	<hannhr$gha$1@build.eclipse.org>	<hanjm1$9nh$1@build.eclipse.org>	1003	11	Xref: build.eclipse.org eclipse.birt:09",
 			"10	=?utf-8?Q?report=20fiel=20version=20not=20supported?=	<m4_batticaloa@yahoo.com>	Fri, 09 Oct 2009 12:34:45 -0400	<hanon9$7iv$1@build.eclipse.org>		1093	12	Xref: build.eclipse.org eclipse.birt:10",
 			"11	=?utf-8?Q?Re:=20report=20fiel=20version=20not=20supported?=	Michael  <mwilliams@actuate.com>	Fri, 09 Oct 2009 13:01:23 -0400	<hanq97$lp9$1@build.eclipse.org>	<hanon9$7iv$1@build.eclipse.org>	1002	7	Xref: build.eclipse.org eclipse.birt:11",
 			"12	=?utf-8?Q?Re:=202=20fields=20from=202=20scripted=20data=20sets=20?= =?utf-8?Q?to=20be=20combined=20in=20one=20data=20field?=	tero  <ter055@yahoo.de>	Fri, 09 Oct 2009 13:11:46 -0400	<hanqsm$q7f$1@build.eclipse.org>	<hamie8$nc9$1@build.eclipse.org>	971	4	Xref: build.eclipse.org eclipse.birt:12",
 			"13	=?utf-8?Q?how=20to=20improve=20performance?=	tero  <ter055@yahoo.de>	Fri, 09 Oct 2009 13:19:24 -0400	<hanrav$u57$1@build.eclipse.org>		1231	14	Xref: build.eclipse.org eclipse.birt:13",
 			"14	=?utf-8?Q?Re:=20dynamic=20grouping=20of=20charts=20possible=3F?=	Michael  <mwilliams@actuate.com>	Fri, 09 Oct 2009 15:33:16 -0400	<hao362$hd2$1@build.eclipse.org>	<han9u0$obp$1@build.eclipse.org>	1126	13	Xref: build.eclipse.org eclipse.birt:14",
 			"15	Problem with syntax trying to pass parameter to Java Object	Richard Catlin <richard.m.catlin@gmail.com>	Fri, 09 Oct 2009 19:09:09 -0700	<haoqch$a2d$1@build.eclipse.org>		1248	14	Xref: build.eclipse.org eclipse.birt:15",
 			"16	=?utf-8?Q?Re:=20dynamic=20grouping=20of=20charts=20possible=3F?=	<a.reiners@reply.eu>	Sat, 10 Oct 2009 09:33:43 -0400	<haq2fo$gi0$1@build.eclipse.org>	<hao362$hd2$1@build.eclipse.org>	1749	20	Xref: build.eclipse.org eclipse.birt:16",
 			"17	=?utf-8?Q?Re:=20report=20fiel=20version=20not=20supported?=	<m4_batticaloa@yahoo.com>	Mon, 12 Oct 2009 05:51:14 -0400	<hauu6j$eav$1@build.eclipse.org>	<hanon9$7iv$1@build.eclipse.org>	1245	9	Xref: build.eclipse.org eclipse.birt:17",
 			"18	=?utf-8?Q?problem=20with=20BIRT=20charts?=	<m4_batticaloa@yahoo.com>	Mon, 12 Oct 2009 05:56:00 -0400	<hauufh$hi0$1@build.eclipse.org>		1309	9	Xref: build.eclipse.org eclipse.birt:18",
 			"19	Improve drawing speed	Olivier Cailloux <mlsmg@ulb.ac.be>	Mon, 12 Oct 2009 16:06:45 +0200	<havd5m$apa$1@build.eclipse.org>		4266	86	Xref: build.eclipse.org eclipse.birt:19",
 			"20	Re: Table x-axis - Weekday Sorting	Jason Weathersby <jasonweathersby@windstream.net>	Mon, 12 Oct 2009 11:04:07 -0400	<havgd2$u7s$1@build.eclipse.org>	<ha578l$3e4$1@build.eclipse.org> <ha58c1$1bq$1@build.eclipse.org>	1039	14	Xref: build.eclipse.org eclipse.birt:20",
 			"21	Re: Exceptions - while using Bar Charts etc in BIRT Reports	Jason Weathersby <jasonweathersby@windstream.net>	Mon, 12 Oct 2009 11:05:53 -0400	<havggb$u2l$1@build.eclipse.org>	<hanivl$t7i$1@build.eclipse.org>	11328	208	Xref: build.eclipse.org eclipse.birt:21",
 			"22	Re: Microsoft WordML import	Jason Weathersby <jasonweathersby@windstream.net>	Mon, 12 Oct 2009 11:07:42 -0400	<havgjo$u2l$2@build.eclipse.org>	<ha4h28$m91$1@build.eclipse.org>	1248	15	Xref: build.eclipse.org eclipse.birt:22",
 			"23	Re: Problems with ReportEngine after Upgrading Birt from 2.3 to 2.5.1	Jason Weathersby <jasonweathersby@windstream.net>	Mon, 12 Oct 2009 11:13:15 -0400	<havgu5$u2l$3@build.eclipse.org>	<haldiv$42p$1@build.eclipse.org> <hamiqa$qkd$1@build.eclipse.org> <hangml$5vi$1@build.eclipse.org>	3135	55	Xref: build.eclipse.org eclipse.birt:23",
 			"24	Re: problem with BIRT installation on top od zend studio for eclipse on ubuntu	Jason Weathersby <jasonweathersby@windstream.net>	Mon, 12 Oct 2009 11:16:55 -0400	<havh51$u2l$4@build.eclipse.org>	<hah8aa$p1k$1@build.eclipse.org> <han669$7rd$1@build.eclipse.org>	2159	32	Xref: build.eclipse.org eclipse.birt:24" };
 
 	static String[] headers = new String[] { "Subject:", "From:", "Date:",
 			"Message-ID:", "References:", "Bytes:", "Lines:", "Xref:full" };
 
 	private IStore store;
 
 	private IStoreEventListener listener;
 	private static HashMap<IStoreEvent, Integer> listenerCounter;
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 	}
 
 	@Before
 	public void setUp() throws Exception {
 		DatabaseTest.setUpBeforeClass();
		final IStoreFactory sf = new StoreFactory();
		store = sf.createStore(SALVO.SALVO_HOME + SALVO.SEPARATOR
 				+ "StoreTestDerby");
 		store.setSecureStore(new ISecureStore() {
 			HashMap<String, String> mappie = new HashMap<String, String>();
 
 			public void remove(String key) {
 				mappie.remove(key);
 			}
 
 			public void put(String key, String value, boolean encrypt) {
 				mappie.put(key, value);
 			}
 
 			public String get(String key, String def) {
 				return mappie.get(key).equals(null) ? def : mappie.get(key);
 			}
 
 			public void clear() {
 				mappie.clear();
 			}
 		});
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		DatabaseTest.tearDownAfterClass();
 	}
 
 	@Test
 	public void testStore() {
 		assertTrue(store != null);
 	}
 
 	@Test
 	public void testFireEvent() {
 		final ArrayList<String> boolie = new ArrayList<String>();
 		store.addListener(new IStoreEventListener() {
 
 			public void storeEvent(IStoreEvent event) {
 				boolie.clear();
 				boolie.add(event.getEventObject().toString());
 			}
 		}, SALVO.EVENT_ALL_EVENTS);
 
 		store.fireEvent(new IStoreEvent() {
 			public int getEventType() {
 				return SALVO.EVENT_ADD;
 			}
 
 			public Object getEventObject() {
 				return "add";
 			}
 		});
 		assertTrue(boolie.get(0).equals("add"));
 
 		store.fireEvent(new IStoreEvent() {
 			public int getEventType() {
 				return SALVO.EVENT_ADD_GROUP;
 			}
 
 			public Object getEventObject() {
 				return "add_group";
 			}
 		});
 		assertTrue(boolie.get(0).equals("add_group"));
 
 		store.fireEvent(new IStoreEvent() {
 			public int getEventType() {
 				return SALVO.EVENT_ADD_SERVER;
 			}
 
 			public Object getEventObject() {
 				return "add_server";
 			}
 		});
 		assertTrue(boolie.get(0).equals("add_server"));
 
 		store.fireEvent(new IStoreEvent() {
 			public int getEventType() {
 				return SALVO.EVENT_CHANGE;
 			}
 
 			public Object getEventObject() {
 				return "change";
 			}
 		});
 		assertTrue(boolie.get(0).equals("change"));
 
 		store.fireEvent(new IStoreEvent() {
 			public int getEventType() {
 				return SALVO.EVENT_CHANGE_GROUP;
 			}
 
 			public Object getEventObject() {
 				return "change_group";
 			}
 		});
 		assertTrue(boolie.get(0).equals("change_group"));
 
 		store.fireEvent(new IStoreEvent() {
 			public int getEventType() {
 				return SALVO.EVENT_CHANGE_SERVER;
 			}
 
 			public Object getEventObject() {
 				return "change_server";
 			}
 		});
 		assertTrue(boolie.get(0).equals("change_server"));
 
 	}
 
 	@Test
 	public void testAddListener() {
 
 		StoreTest.listenerCounter = new HashMap<IStoreEvent, Integer>();
 
 		listener = new IStoreEventListener() {
 
 			public void storeEvent(IStoreEvent event) {
 				Integer counter = listenerCounter.get(event);
 				if (counter == null) {
 					counter = new Integer(0);
 					listenerCounter.put(event, counter);
 				}
 				counter += 1;
 			}
 		};
 
 		store.addListener(listener, SALVO.EVENT_ALL_EVENTS);
 	}
 
 	@Test
 	public void testSubscribeServer() throws NNTPException {
 		ICredentials credentials = new ICredentials() {
 
 			public String getUser() {
 				return "Wim Jongman";
 			}
 
 			public String getPassword() {
 				return "flinder1f7";
 			}
 
 			public String getOrganization() {
 				return "weltevree beheer";
 			}
 
 			public String getLogin() {
 				return "exquisitus";
 			}
 
 			public String getEmail() {
 				return "wim.jongman@gmail.com";
 			}
 		};
 
 		IServer server = ServerFactory.getCreateServer("news.eclipse.org", 119,
 				credentials, true);
 		store.subscribeServer(server, "flinder1f7");
 
 		assertTrue(store.getSubscribedServers()[0].equals(server));
 
 	}
 
 	@Test
 	public void testSubscribeNewsgroup() throws NNTPException {
 		testSubscribeServer();
 		INewsgroup newsgroup = NewsgroupFactory.createNewsGroup(store
 				.getSubscribedServers()[0], "eclipse.platform.rcp",
 				"eclipse.platform.rcp");
 		store.subscribeNewsgroup(newsgroup);
 		assertTrue(newsgroup.isSubscribed());
 		assertTrue(store
 				.getSubscribedNewsgroups(store.getSubscribedServers()[0]).length == 1);
 	}
 
 	@Test
 	public void testGetSubscribedNewsgroups() throws StoreException,
 			NNTPException {
 		testSubscribeNewsgroup();
 		assertTrue(store
 				.getSubscribedNewsgroups(store.getSubscribedServers()[0]).length == 1);
 	}
 
 	@Test
 	public void testStoreArticles() throws NNTPIOException,
 			UnexpectedResponseException, StoreException, NNTPException {
 
 		testSubscribeNewsgroup();
 		ArrayList<IArticle> result = new ArrayList<IArticle>();
 		for (String string : overviewHeaders) {
 			IArticle article = ArticleFactory.createArticle(headers, string,
 					store
 							.getSubscribedNewsgroups(store
 									.getSubscribedServers()[0])[0]);
 			result.add(article);
 		}
 		store.storeArticles((IArticle[]) result.toArray(new IArticle[0]));
 		assertTrue(store.getArticles(store.getSubscribedNewsgroups(store
 				.getSubscribedServers()[0])[0], 1, 100) == null);
 		assertTrue(store.getArticles(store.getSubscribedNewsgroups(store
 				.getSubscribedServers()[0])[0], 1, 24).length == overviewHeaders.length);
 	}
 
 	@Test
 	public void testStoreArticleBody() throws StoreException, NNTPIOException,
 			UnexpectedResponseException, NNTPException {
 
 		testStoreArticles();
 
 		String[] body = new String[] {
 				"the quick brown fox jumps over the lazy dog 2",
 				"the quick brown fox jumps over the lazy dog 3",
 				"the quick brown fox jumps over the lazy dog 4",
 				"the quick brown fox jumps over the lazy dog 5",
 				"the quick brown fox jumps over the lazy dog 6" };
 
 		for (int i = 0; i < overviewHeaders.length; i++) {
 			store.storeArticleBody(store.getArticle(
 					store
 							.getSubscribedNewsgroups(store
 									.getSubscribedServers()[0])[0], i + 1),
 					body);
 
 		}
 	}
 
 	@Test
 	public void testGetArticleBody() throws NNTPIOException,
 			UnexpectedResponseException, StoreException, NNTPException {
 
 		testStoreArticleBody();
 		for (int i = 0; i < overviewHeaders.length; i++) {
 			String[] body = store.getArticleBody(store.getArticle(
 					store
 							.getSubscribedNewsgroups(store
 									.getSubscribedServers()[0])[0], i + 1));
 			assertTrue(body.length + "", body.length == 5);
 		}
 	}
 
 	@Test
 	public void testUpdateAttributes() throws StoreException, NNTPException {
 		testSubscribeNewsgroup();
 		INewsgroup group = store.getSubscribedNewsgroups(store
 				.getSubscribedServers()[0])[0];
 		group.setSubscribed(false);
 		store.updateAttributes(group);
 		assertTrue(store
 				.getSubscribedNewsgroups(store.getSubscribedServers()[0]).length == 0);
 	}
 
 	@Test
 	public void testGetSubscribedServers() throws NNTPException {
 		testSubscribeServer();
 		assertTrue(store.getSubscribedServers().length == 1);
 	}
 
 	@Test
 	public void testGetArticles() throws NNTPIOException,
 			UnexpectedResponseException, StoreException, NNTPException {
 		testStoreArticles();
 		assertTrue(store.getArticles(store.getSubscribedNewsgroups(store
 				.getSubscribedServers()[0])[0], 1, 24).length == overviewHeaders.length);
 	}
 
 	@Test
 	public void testGetArticleINewsgroupInt() throws NNTPIOException,
 			UnexpectedResponseException, StoreException, NNTPException {
 		testStoreArticles();
 		for (int i = 0; i < overviewHeaders.length; i++) {
 			assertTrue(store.getArticle(store.getSubscribedNewsgroups(store
 					.getSubscribedServers()[0])[0], i + 1) != null);
 
 		}
 	}
 
 	@Test
 	public void testGetFirstArticle() throws StoreException, NNTPException {
 		testStoreArticles();
 		assertTrue(store
 				.getFirstArticle(
 						store.getSubscribedNewsgroups(store
 								.getSubscribedServers()[0])[0])
 				.getArticleNumber() == 1);
 	}
 
 	@Test
 	public void testGetLastArticle() throws StoreException, NNTPException {
 		testStoreArticles();
 		assertTrue(store
 				.getLastArticle(
 						store.getSubscribedNewsgroups(store
 								.getSubscribedServers()[0])[0])
 				.getArticleNumber() == 24);
 	}
 
 	@Test
 	public void testGetFollowUps() throws NNTPIOException,
 			UnexpectedResponseException, StoreException, NNTPException {
 		testStoreArticles();
 
 		for (int i = 0; i < overviewHeaders.length; i++) {
 			IArticle article = store.getArticle(
 					store
 							.getSubscribedNewsgroups(store
 									.getSubscribedServers()[0])[0], i + 1);
 			assertTrue(article != null);
 
 			IArticle[] followUps = store.getFollowUps(article);
 			System.out.println(followUps.length);
 
 		}
 
 	}
 
 	@Test
 	public void testUpdateArticle() throws NNTPIOException,
 			UnexpectedResponseException, StoreException, NNTPException {
 		testStoreArticles();
 		for (int i = 0; i < overviewHeaders.length; i++) {
 			IArticle article = store.getArticle(
 					store
 							.getSubscribedNewsgroups(store
 									.getSubscribedServers()[0])[0], i + 1);
 			assertTrue(article != null);
 
 			article.setMarked(false);
 			article.setRead(false);
 			store.updateArticle(article);
 			article = store.getArticle(store.getSubscribedNewsgroups(store
 					.getSubscribedServers()[0])[0], i + 1);
 			assertTrue(article.isRead() == false);
 			assertTrue(article.isMarked() == false);
 
 			article.setMarked(true);
 			article.setRead(true);
 			store.updateArticle(article);
 			article = store.getArticle(store.getSubscribedNewsgroups(store
 					.getSubscribedServers()[0])[0], i + 1);
 			assertTrue(article.isRead() == true);
 			assertTrue(article.isMarked() == true);
 
 		}
 
 	}
 
 	@Test
 	public void testGetDescription() throws NNTPIOException,
 			UnexpectedResponseException, StoreException, NNTPException {
 		assertTrue(store.getDescription() != null);
 	}
 
 	@Test
 	public void testSetWaterMarks() {
 
 	}
 
 	@Test
 	public void testSetSecureStore() {
 	}
 
 	@Test
 	public void testGetArticleString() throws NNTPIOException,
 			UnexpectedResponseException, NNTPException {
 		testStoreArticles();
 		INewsgroup group = store.getSubscribedNewsgroups(store
 				.getSubscribedServers()[0])[0];
 
 		for (int i = 0; i < overviewHeaders.length; i++) {
 			IArticle article = store.getArticle(group.getURL() + "&article="
 					+ (i + 1));
 			assertTrue(article != null);
 		}
 	}
 
 	@Test
 	public void testUnsubscribeNewsgroup() throws NNTPIOException,
 			UnexpectedResponseException, StoreException, NNTPException {
 		testStoreArticles();
 		INewsgroup group = store.getSubscribedNewsgroups(store
 				.getSubscribedServers()[0])[0];
 		store.unsubscribeNewsgroup(group, false);
 		assertTrue(store.getArticles(group, 1, 24).length == overviewHeaders.length);
 		store.subscribeNewsgroup(group);
 		store.unsubscribeNewsgroup(group, true);
 		assertTrue(store.getArticles(group, 1, 24) == null);
 	}
 
 	@Test
 	public void testUnsubscribeServer() throws StoreException, NNTPException {
 		testSubscribeNewsgroup();
 		IServer server = store.getSubscribedServers()[0];
 		assertTrue(store.getSubscribedServers().length == 1);
 		store.unsubscribeServer(store.getSubscribedServers()[0], false);
 		assertTrue(store.getSubscribedServers().length == 0);
 		assertTrue(store.getSubscribedNewsgroups(server).length == 1);
 		store.unsubscribeServer(server, true);
 		assertTrue(store.getSubscribedNewsgroups(server).length == 0);
 
 	}
 
 	@Test
 	public void testRemoveListener() throws NNTPException {
 		// assertTrue(!listenerCounter.isEmpty());
 		// listenerCounter.clear();
 		// assertTrue(listenerCounter.isEmpty());
 		testUnsubscribeServer();
 		assertTrue(listenerCounter.isEmpty());
 		testAddListener();
 		testUnsubscribeServer();
 		testSubscribeServer();
 		assertTrue(!listenerCounter.isEmpty());
 	}
 }

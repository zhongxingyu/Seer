 /*  Copyright (C) 2012  Nicholas Wright
 	
 	part of 'Aid', an imageboard downloader.
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package board;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.junit.matchers.JUnitMatchers.*;
 import io.TextFileReader;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.eclipse.jetty.server.Request;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.AbstractHandler;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class FourChanStrategyTest {
 	SiteStrategy strategy;
 	static Server server;
 	static String mainPage, boardPage, thread;
 	
 	static URL mainUrl, boardUrl, threadUrl, invalidUrl;
 	
 	@BeforeClass
 	static public void before() throws Exception{
 		server  = new Server(80);
 		server.setHandler(new TestHandler());
 		server.start();
 		
 		TextFileReader tfr = new TextFileReader();
 		
 		
 		mainPage = tfr.read(ClassLoader.getSystemResourceAsStream("HtmlData\\mainPage.html"));
 		boardPage = tfr.read(ClassLoader.getSystemResourceAsStream("HtmlData\\pageTestData"));
 		thread = tfr.read(ClassLoader.getSystemResourceAsStream("HtmlData\\threadData.html"));
 
 		mainUrl = new URL("http://localhost/");
 		boardUrl = new URL("http://localhost/htmlnew");
 		threadUrl = new URL("http://localhost/p/res/57867301");
 		invalidUrl = new URL("http://foobar/");
 	}
 
 	@Before
 	public void setUp() throws Exception {
 		strategy = new FourChanStrategy();
 	}
 	
 	@AfterClass
 	static public void after() throws Exception{
 		server.stop();
 	}
 
 	@Test
 	public void testValidSiteStrategy() throws MalformedURLException {
 		boolean isValid = strategy.validSiteStrategy(new URL("http://www.4chan.org"));
 		assertTrue(isValid);
 	}
 
 	@Test
 	public void testFindBoards() throws Exception {
 		Map<String, URL> foundBoards;
 		foundBoards = strategy.findBoards(mainUrl);
 		
 		assertThat(foundBoards.get("Photography"), is(new URL("http://boards.4chan.org/p/")));
 		assertThat(foundBoards.get("Music"), is(new URL("http://boards.4chan.org/mu/")));
 		assertThat(foundBoards.get("Fashion"), is(new URL("http://boards.4chan.org/fa/")));
 		assertThat(foundBoards.get("Sports"), is(new URL("http://boards.4chan.org/sp/")));
		
		assertThat(foundBoards.size(), is(59));
 	}
 	
 	@Test
 	public void testFindBoardsInvalid() {
 		Map<String, URL> foundBoards;
 		foundBoards = strategy.findBoards(invalidUrl);
 		assertThat(foundBoards.size(),is(0));
 	}
 
 	@Test
 	public void testGetBoardPageCount() {
 		assertThat(strategy.getBoardPageCount(boardUrl), is(2));
 	}
 	
 	@Test
 	public void testGetBoardPageCountInvalid() {
 		assertThat(strategy.getBoardPageCount(invalidUrl), is(0));
 	}
 	
 	@Test
 	public void testParsePage() throws Exception {
 		List<URL> pageUrls = strategy.parsePage(boardUrl);
 		LinkedList<URL> correctUrls = new LinkedList<>();
 		
 		int replyNr[] = {1418, 7897, 7910, 1461, 1454, 1456, 1450, 1449, 1448, 1447};
 		
 		for(int i : replyNr){
 			correctUrls.add(new URL("http://localhost/newhtml/res/" + String.valueOf(i)));
 		}
 		
 		URL correctArray[] = new URL[0];
 		
 		correctUrls.toArray(correctArray);
 		assertThat(pageUrls, hasItems(correctArray));
 	}
 	
 	@Test
 	public void testParsePageInvalid() {
 		List<URL> pageUrls = strategy.parsePage(invalidUrl);
 		assertThat(pageUrls.size(), is(0));
 	}
 
 	@Test
 	public void testParseThread() {
 		List<Post> posts = strategy.parseThread(threadUrl);
 		int images = 0, comments = 0;
 		
 		assertThat(posts.size(), is(13));
 		
 		for(Post p : posts){
 			if(p.hasComment()){
 				comments++;
 			}
 			
 			if(p.hasImage()){
 				images++;
 			}
 		}
 		
 		assertThat(images, is(8));
 		assertThat(comments, is(13));
 	
 	}
 	
 	@Test
 	public void testParseThreadInvalid() {
 		List<Post> posts = strategy.parseThread(invalidUrl);
 		assertThat(posts.size(), is(0));
 	}
 	
 	@Test
 	public void testGetThreadNumber() {
 		int threadNumber = strategy.getThreadNumber(threadUrl);
 		assertThat(threadNumber, is(57867301));
 	}
 	
 	@Test
 	public void testGetThreadNumberInvalid() {
 		int threadNumber = strategy.getThreadNumber(invalidUrl);
 		assertThat(threadNumber, is(0));
 	}
 	
 	@Test
 	public void testGetBoardLetters() {
 		String threadLetters = strategy.getBoardShortcut(threadUrl);
 		assertThat(threadLetters, is("p"));
 	}
 	
 	@Test
 	public void testGetBoardLettersInvalid() {
 		String threadLetters = strategy.getBoardShortcut(invalidUrl);
 		assertThat(threadLetters, is(""));
 	}
 	
 	static class TestHandler extends AbstractHandler{
 		@Override
 		public void handle(String arg0, Request baseRequest, HttpServletRequest request,
 				HttpServletResponse response) throws IOException, ServletException {
 
 			response.setContentType("text/html;charset=utf-8");
 			response.setStatus(HttpServletResponse.SC_OK);
 			baseRequest.setHandled(true);
 			
 			String requestUri = request.getRequestURI();
 			
 			if(requestUri.equals("/")){
 				response.getWriter().println(mainPage);
 			}else if(requestUri.equals("/htmlnew")){
 				response.getWriter().println(boardPage);
 			}else if(requestUri.equals("/p/res/57867301")){
 				response.getWriter().println(thread);
 			}else{
 				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
 			}
 		}
 	}
 
 }

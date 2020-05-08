 /**
  * Copyright 2010 Google Inc.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  *
  */
 
 package org.waveprotocol.wave.examples.fedone.rpc;
 
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.when;
 
 import junit.framework.TestCase;
 
 import org.waveprotocol.wave.common.util.PercentEscaper;
 import org.waveprotocol.wave.examples.fedone.account.HumanAccountData;
 import org.waveprotocol.wave.examples.fedone.account.HumanAccountDataImpl;
 import org.waveprotocol.wave.examples.fedone.authentication.AccountStoreHolder;
 import org.waveprotocol.wave.examples.fedone.authentication.AuthTestUtil;
 import org.waveprotocol.wave.examples.fedone.authentication.PasswordDigest;
 import org.waveprotocol.wave.examples.fedone.authentication.SessionManagerImpl;
 import org.waveprotocol.wave.examples.fedone.persistence.AccountStore;
 import org.waveprotocol.wave.examples.fedone.persistence.memory.MemoryStore;
 import org.waveprotocol.wave.model.wave.ParticipantId;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Reader;
 import java.io.StringReader;
import java.util.Locale;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  * @author josephg@gmail.com (Joseph Gentle)
  */
 public class AuthenticationServletTest extends TestCase {
   private AuthenticationServlet servlet;
 
   @Override
   protected void setUp() throws Exception {
     AccountStore store = new MemoryStore();
     HumanAccountData account = new HumanAccountDataImpl(
         ParticipantId.ofUnsafe("frodo@example.com"), new PasswordDigest("password".toCharArray()));
     store.putAccount(account);
     servlet = new AuthenticationServlet(
         AuthTestUtil.make(), new SessionManagerImpl(store), "example.com");
     AccountStoreHolder.init(store);
   }
 
   @Override
   protected void tearDown() throws Exception {
     AccountStoreHolder.clearAccountStore();
   }
 
   public void testGetReturnsSomething() throws IOException {
     HttpServletRequest req = mock(HttpServletRequest.class);
     when(req.getSession(false)).thenReturn(null);
 
     HttpServletResponse resp = mock(HttpServletResponse.class);
     PrintWriter writer = mock(PrintWriter.class);
     when(resp.getWriter()).thenReturn(writer);
    when(req.getLocale()).thenReturn(Locale.ENGLISH);
    
     servlet.doGet(req, resp);
 
     verify(resp).setStatus(HttpServletResponse.SC_OK);
   }
 
   public void testValidLoginWorks() throws IOException {
     HttpServletRequest req = mock(HttpServletRequest.class);
     HttpSession session = mock(HttpSession.class);
     HttpServletResponse resp = mock(HttpServletResponse.class);
 
     attemptLogin(req, resp, session, "frodo@example.com", "password", null);
 
     verify(resp).setStatus(HttpServletResponse.SC_OK);
     verify(session).setAttribute("user", ParticipantId.ofUnsafe("frodo@example.com"));
   }
 
   public void testLoginRedirects() throws IOException {
     HttpServletRequest req = mock(HttpServletRequest.class);
     HttpSession session = mock(HttpSession.class);
     HttpServletResponse resp = mock(HttpServletResponse.class);
 
     String redirect_location = "/abc123?nested=query&string";
     PercentEscaper escaper =
         new PercentEscaper(PercentEscaper.SAFEQUERYSTRINGCHARS_URLENCODER, false);
     String query_str = "r=" + escaper.escape(redirect_location);
 
     attemptLogin(req, resp, session, "frodo@example.com", "password", query_str);
 
     verify(resp).sendRedirect(redirect_location);
     verify(session).setAttribute("user", ParticipantId.ofUnsafe("frodo@example.com"));
   }
 
   public void testLoginDoesNotRedirectToRemoteSite() throws IOException {
     HttpServletRequest req = mock(HttpServletRequest.class);
     HttpSession session = mock(HttpSession.class);
     HttpServletResponse resp = mock(HttpServletResponse.class);
 
     String redirect_location = "http://example.com/other/site";
     PercentEscaper escaper =
         new PercentEscaper(PercentEscaper.SAFEQUERYSTRINGCHARS_URLENCODER, false);
     String query_str = "r=" + escaper.escape(redirect_location);
 
     attemptLogin(req, resp, session, "frodo@example.com", "password", query_str);
 
     verify(resp, never()).sendRedirect(anyString());
   }
 
   public void testIncorrectPasswordReturns403() throws IOException {
     HttpServletRequest req = mock(HttpServletRequest.class);
     HttpSession session = mock(HttpSession.class);
     HttpServletResponse resp = mock(HttpServletResponse.class);
 
     attemptLogin(req, resp, session, "frodo@example.com", "incorrect", null);
 
     verify(resp).sendError(HttpServletResponse.SC_FORBIDDEN);
     verify(session, never()).setAttribute(eq("user"), anyString());
   }
 
   public void testInvalidUsernameReturns403() throws IOException {
     HttpServletRequest req = mock(HttpServletRequest.class);
     HttpSession session = mock(HttpSession.class);
     HttpServletResponse resp = mock(HttpServletResponse.class);
 
     attemptLogin(req, resp, session, "madeup@example.com", "incorrect", null);
 
     verify(resp).sendError(HttpServletResponse.SC_FORBIDDEN);
     verify(session, never()).setAttribute(eq("address"), anyString());
   }
 
   // *** Utility methods
 
   public void attemptLogin(HttpServletRequest req, HttpServletResponse resp, HttpSession session,
       String address, String password, String queryString) throws IOException {
     // The query string is escaped.
     PercentEscaper escaper = new PercentEscaper(PercentEscaper.SAFECHARS_URLENCODER, true);
     String data =
         "address=" + escaper.escape(address) + "&" + "password=" + escaper.escape(password);
 
     when(req.getSession(false)).thenReturn(null);
     Reader reader = new StringReader(data);
     when(req.getReader()).thenReturn(new BufferedReader(reader));
     when(req.getQueryString()).thenReturn(queryString);
     PrintWriter writer = mock(PrintWriter.class);
     when(resp.getWriter()).thenReturn(writer);
     when(req.getSession(true)).thenReturn(session);
 
     servlet.doPost(req, resp);
   }
 }

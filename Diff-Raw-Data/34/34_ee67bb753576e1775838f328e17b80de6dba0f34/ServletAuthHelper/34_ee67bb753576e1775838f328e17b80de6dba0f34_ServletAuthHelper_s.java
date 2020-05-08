 /*
  * Copyright 2011 Google Inc. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.google.walkaround.wave.server.auth;
 
 import org.waveprotocol.wave.model.wave.ParticipantId;
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.common.net.UriEscapers;
 import com.google.inject.Inject;
 import com.google.walkaround.util.server.RetryHelper.PermanentFailure;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.annotation.Nullable;
 import javax.servlet.FilterChain;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Utility methods for authentication filters.
  *
  * @author ohler@google.com (Christian Ohler)
  */
 public class ServletAuthHelper {
 
   @SuppressWarnings("unused")
   private static final Logger log = Logger.getLogger(ServletAuthHelper.class.getName());
 
   private final AccountStore accountStore;
   private final UserContext userContext;
  private final User user;
 
   @Inject ServletAuthHelper(AccountStore accountStore,
       UserContext userContext,
      User user) {
     this.accountStore = accountStore;
     this.userContext = userContext;
     this.user = user;
   }
 
   private static String queryEncode(String s) {
     return UriEscapers.uriQueryStringEscaper(false).escape(s);
   }
 
   public void redirectToOAuthPage(HttpServletRequest req, HttpServletResponse resp,
       String email) throws IOException {
     String originalRequest = req.getRequestURI()
         + (req.getQueryString() == null ? "" : "?" + req.getQueryString());
     String targetUrl = "/enable?originalRequest=" + queryEncode(originalRequest);
     log.info("Redirecting to OAuth page: " + targetUrl);
     resp.sendRedirect(targetUrl);
   }
 
   private void populateContext(AccountStore.Record record) {
     log.info("Populating " + userContext + " from record " + record);
     userContext.setUserId(record.getUserId());
     userContext.setParticipantId(record.getParticipantId());
     userContext.setOAuthCredentials(record.getOAuthCredentials());
     log.info("User context is now " + userContext);
   }
 
   private void clearOAuthTokenFromContext() {
     log.info("Clearing OAuth token from " + userContext);
     userContext.setOAuthCredentials(null);
     log.info("User context is now " + userContext);
   }
 
   private void writeAccountRecord(AccountStore.Record record) throws IOException {
     try {
       accountStore.put(record);
     } catch (PermanentFailure e) {
       throw new IOException("Failed to write account record: " + record, e);
     }
   }
 
   /**
    * Extracts an account record from {@code userContext} and writes it to the
    * account store unless it matches {@code storedRecord}.
    */
   private void writeBackContextMaybe(@Nullable AccountStore.Record storedRecord)
       throws IOException {
     AccountStore.Record newRecord =
         new AccountStore.Record(userContext.getUserId(), userContext.getParticipantId(),
             userContext.hasOAuthCredentials() ? userContext.getOAuthCredentials() : null);
     if (!newRecord.equals(storedRecord)) {
       log.info("User context dirty, writing back:\nold=" + storedRecord
           + "\nnew=" + newRecord);
       writeAccountRecord(newRecord);
     } else {
       log.info("User context unchanged, not writing back");
     }
   }
 
   /** Extracts user id from the request and looks up account information. */
   public interface AccountLookup {
     /**
      * Extracts the user id from the request and reads and returns the
      * corresponding account store record.
      *
     * A null return value is equivalent to throwing
     * {@link NeedNewOAuthTokenException}.
      */
     @Nullable AccountStore.Record getAccount() throws PermanentFailure, IOException;
   }
 
   /** Tells the client that a new OAuth token is needed. */
   public interface NeedNewOAuthTokenHandler {
     /** Sends a response that indicates that a new token is needed. */
     void sendNeedTokenResponse() throws IOException;
   }
 
   /** Implements {@link AccountLookup} with App Engine's {@link UserService}. */
   public static class UserServiceAccountLookup implements AccountLookup {
     private final AccountStore accountStore;
     private final User user;
 
     @Inject public UserServiceAccountLookup(AccountStore accountStore, User user) {
       this.accountStore = accountStore;
       this.user = user;
     }
 
     @Override @Nullable public AccountStore.Record getAccount()
         throws PermanentFailure, IOException {
       return accountStore.get(new StableUserId(user.getUserId()));
     }
   }
 
   /**
    * Body for {@link #serve()}.
    */
   public interface ServletBody {
     public void run() throws ServletException, IOException;
   }
 
   /**
    * Invokes {@code body} with {@link UserContext} populated from
    * {@code lookup.getAccount()}.
    *
    * Writes a new {@link AccountStore.Record} from {@code UserContext} and/or
    * calls {@link NeedNewOAuthTokenHandler#sendNeedTokenResponse} as needed.
    */
   public void serve(ServletBody body, AccountLookup lookup, NeedNewOAuthTokenHandler handler)
       throws IOException, ServletException {
     @Nullable AccountStore.Record record = null;
     try {
       try {
         record = lookup.getAccount();
       } catch (PermanentFailure e) {
         throw new IOException("PermanentFailure getting account information", e);
       }
       if (record == null) {
        userContext.setUserId(new StableUserId(user.getUserId()));
        userContext.setParticipantId(ParticipantId.ofUnsafe(user.getEmail()));
       } else {
         populateContext(record);
       }
       body.run();
       writeBackContextMaybe(record);
     } catch (NeedNewOAuthTokenException e) {
       log.log(Level.INFO, "Need new token", e);
       if (record != null) {
         // Delete the token so that the next page load will prompt to re-enable.
         // TODO(ohler): Change the client to be able to prompt for a new token
         // from within /wave.
         writeAccountRecord(
             new AccountStore.Record(userContext.getUserId(), userContext.getParticipantId(), null));
       }
       handler.sendNeedTokenResponse();
       return;
     }
   }
 
   /**
    * Like {@link #serve()}, but invokes {@code filterChain}.
    */
   public void filter(final ServletRequest req, final ServletResponse resp,
       final FilterChain filterChain, AccountLookup lookup, NeedNewOAuthTokenHandler handler)
       throws IOException, ServletException {
     serve(new ServletBody() {
       @Override public void run() throws IOException, ServletException {
         filterChain.doFilter(req, resp);
       }
     }, lookup, handler);
   }
 
 }

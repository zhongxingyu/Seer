 /*
  * Copyright 2012 Janrain, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.janrain.backplane.server1;
 
 import com.janrain.backplane.cache.CachedL1;
 import com.janrain.backplane.common.AuthException;
 import com.janrain.backplane.common.BackplaneServerException;
 import com.janrain.backplane.common.HmacHashUtils;
 import com.janrain.backplane.config.Admin;
 import com.janrain.backplane.config.BackplaneConfig;
 import com.janrain.backplane.config.BackplaneSystemProps;
 import com.janrain.backplane.config.BpServerConfig;
 import com.janrain.backplane.dao.ServerDAOs;
 import com.janrain.backplane.server1.dao.BP1DAOs;
 import com.janrain.backplane.server1.dao.BP1MessageDao;
 import com.janrain.backplane.servlet.ServletUtil;
 import com.janrain.commons.message.MessageException;
 import com.yammer.metrics.Metrics;
 import com.yammer.metrics.core.Histogram;
 import com.yammer.metrics.core.MetricName;
 import com.yammer.metrics.core.TimerContext;
 import org.apache.commons.codec.binary.Base64;
 import org.apache.log4j.Logger;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.security.SecureRandom;
 import java.util.*;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Backplane API implementation.
  *
  * @author Johnny Bufu
  */
 @Controller
 @RequestMapping(value="/*")
 @SuppressWarnings({"UnusedDeclaration"})
 public class Backplane1Controller {
 
     // - PUBLIC
 
     @RequestMapping(value = "/", method = { RequestMethod.GET, RequestMethod.HEAD })
     public ModelAndView greetings(HttpServletRequest request, HttpServletResponse response) {
         if (RequestMethod.HEAD.toString().equals(request.getMethod())) {
             response.setContentLength(0);
         }
         return new ModelAndView("welcome");
     }
 
     @RequestMapping(value = "/admin", method = { RequestMethod.GET, RequestMethod.HEAD })
     public ModelAndView admin(HttpServletRequest request, HttpServletResponse response) throws BackplaneServerException {
 
         ServletUtil.checkSecure(request);
         boolean adminUserExists = true;
 
         // check to see if an admin record already exists, if it does, do not allow an update
         Admin admin = ServerDAOs.getAdminDAO().get(BackplaneSystemProps.ADMIN_USER);
         if (admin == null) {
             adminUserExists = false;
         }
 
         if (RequestMethod.HEAD.toString().equals(request.getMethod())) {
             response.setContentLength(0);
         }
 
         BpServerConfig bpServerConfig = ServerDAOs.getConfigDAO().get(BackplaneSystemProps.BPSERVER_CONFIG_KEY);
         if (bpServerConfig == null) {
             bpServerConfig = new BpServerConfig();
         }
         // add it to the L1 cache
         CachedL1.getInstance().setObject(BackplaneSystemProps.BPSERVER_CONFIG_KEY, -1, bpServerConfig);
 
         ModelAndView view = new ModelAndView("admin");
         view.addObject("adminUserExists", adminUserExists);
         view.addObject("configKey", bpServerConfig.getIdValue());
         view.addObject("debugMode", bpConfig.isDebugMode());
         view.addObject("defaultMessagesMax", bpServerConfig.get(BpServerConfig.Field.DEFAULT_MESSAGES_MAX));
 
         return view;
     }
 
     @RequestMapping(value = "/adminupdate", method = { RequestMethod.POST })
     public ModelAndView updateConfiguration(HttpServletRequest request, HttpServletResponse response) throws BackplaneServerException {
 
         ServletUtil.checkSecure(request);
 
         BpServerConfig bpServerConfig =
                 ServerDAOs.getConfigDAO().get(BackplaneSystemProps.BPSERVER_CONFIG_KEY);
         if (bpServerConfig == null) {
             bpServerConfig = new BpServerConfig();
         }
         ModelAndView view = new ModelAndView("adminadd");
         String debugModeString = request.getParameter("debug_mode");
         String defaultMessagesMax = request.getParameter("default_messages_max");
         bpServerConfig.put(BpServerConfig.Field.DEBUG_MODE.getFieldName(), Boolean.valueOf(debugModeString).toString());
         bpServerConfig.put(BpServerConfig.Field.DEFAULT_MESSAGES_MAX.getFieldName(), defaultMessagesMax);
 
         try {
             bpServerConfig.validate();
             ServerDAOs.getConfigDAO().persist(bpServerConfig);
             // add it to the L1 cache
             CachedL1.getInstance().setObject(BackplaneSystemProps.BPSERVER_CONFIG_KEY, -1, bpServerConfig);
             logger.info(bpServerConfig.toString());
         } catch (Exception e) {
             logger.error(e);
             view.addObject("message", "An error has occurred " + e.getMessage());
             return view;
         }
 
         view.addObject("message", "Configuration updated");
         return view;
 
     }
 
 
 
     @RequestMapping(value = "/adminadd", method = { RequestMethod.POST })
     public ModelAndView addAdmin(HttpServletRequest request, HttpServletResponse response) {
 
         try {
             ServletUtil.checkSecure(request);
 
             ModelAndView view = new ModelAndView("adminadd");
             // be sure no record exists
             Admin admin = ServerDAOs.getAdminDAO().get(BackplaneSystemProps.ADMIN_USER);
             if (admin == null) {
                 String name = request.getParameter("username");
                 if (!name.equals(BackplaneSystemProps.ADMIN_USER)) {
                     view.addObject("message", "Admin user name must be " + BackplaneSystemProps.ADMIN_USER);
                     return view;
                 }
                 String password = request.getParameter("password");
                 // hash password
                 password = HmacHashUtils.hmacHash(password);
                 Admin newAdmin = new Admin();
                 newAdmin.setUserNamePassword(name, password);
                 ServerDAOs.getAdminDAO().persist(newAdmin);
                 view.addObject("message", "Admin user " + name + " updated");
             } else {
                 view.addObject("message", "Admin user already exists.  You must delete the entry from the database before submitting a new admin user.");
             }
 
             if (RequestMethod.HEAD.toString().equals(request.getMethod())) {
                 response.setContentLength(0);
             }
 
             return view;
 
         } catch (Exception e) {
             logger.error(e);
             throw new RuntimeException(e);
         }
     }
 
 
 
     @RequestMapping(value = "/{version}/bus/{bus}", method = RequestMethod.GET)
     public @ResponseBody List<HashMap<String,Object>> getBusMessages(
             @PathVariable String version,
             @RequestHeader(value = "Authorization", required = false) String basicAuth,
             @PathVariable String bus,
             @RequestParam(value = "since", defaultValue = "") String since,
             @RequestParam(value = "sticky", required = false) String sticky )
             throws AuthException, MessageException, BackplaneServerException {
 
         final TimerContext context = getBusMessagesTime.time();
 
         try {
 
             checkAuth(basicAuth, bus, BusConfig1.BUS_PERMISSION.GETALL);
 
             List<BackplaneMessage> messages = BP1DAOs.getMessageDao().getMessagesByBus(bus, since, sticky);
 
             List<HashMap<String,Object>> frames = new ArrayList<HashMap<String, Object>>();
             for (BackplaneMessage message : messages) {
                 frames.add(message.asFrame(version));
             }
             return frames;
 
         } finally {
             context.stop();
         }
 
     }
 
     @RequestMapping(value = "/{version}/bus/{bus}/channel/{channel}", method = RequestMethod.GET)
     public ResponseEntity<String> getChannel(
             @PathVariable String version,
             @PathVariable String bus,
             @PathVariable String channel,
             @RequestParam(required = false) String callback,
             @RequestParam(value = "since", required = false) String since,
             @RequestParam(value = "sticky", required = false) String sticky )
             throws MessageException, AuthException, BackplaneServerException {
 
         logger.debug("request started");
 
         try {
 
             return new ResponseEntity<String>(
                     NEW_CHANNEL_LAST_PATH.equals(channel) ? newChannel() : getChannelMessages(bus, channel, since, sticky, version),
                     new HttpHeaders() {{
                         add("Content-Type", "application/json");
                     }},
                     HttpStatus.OK);
 
         } finally {
             logger.debug("request ended");
         }
 
     }
 
     @RequestMapping(value = "/{version}/bus/{bus}/channel/{channel}", method = RequestMethod.POST)
     public @ResponseBody String postToChannel(
             @PathVariable String version,
             @RequestHeader(value = "Authorization", required = false) String basicAuth,
             @RequestBody List<Map<String,Object>> messages,
             @PathVariable String bus,
             @PathVariable String channel) throws AuthException, MessageException, BackplaneServerException {
 
         checkAuth(basicAuth, bus, BusConfig1.BUS_PERMISSION.POST);
 
         final TimerContext context = postMessagesTime.time();
 
         try {
 
             BP1MessageDao backplaneMessageDAO = BP1DAOs.getMessageDao();
 
             //Block post if the caller has exceeded the message post limit
             if (backplaneMessageDAO.getMessageCount(bus, channel) >= bpConfig.getDefaultMaxMessageLimit()) {
                 logger.warn("Channel " + bus + ":" + channel + " has reached the maximum of " +
                         bpConfig.getDefaultMaxMessageLimit() + " messages");
                 throw new BackplaneServerException("Message limit exceeded for this channel");
             }
 
             BusConfig1 busConfig = BP1DAOs.getBusDao().get(bus);
 
             for(Map<String,Object> messageData : messages) {
                 BackplaneMessage message = new BackplaneMessage(bus, channel,
                         busConfig.getRetentionTimeSeconds(),
                         busConfig.getRetentionTimeStickySeconds(),
                         messageData);
                 backplaneMessageDAO.persist(message);
             }
 
             return "";
 
         } finally {
             context.stop();
         }
     }
 
     /**
      * Handle auth errors
      */
     @ExceptionHandler
     @ResponseBody
     public Map<String, String> handle(final AuthException e, HttpServletResponse response) {
         logger.error("Backplane authentication error: " + e.getMessage(), bpConfig.getDebugException(e));
         response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
         return new HashMap<String,String>() {{
             put(ERR_MSG_FIELD, e.getMessage());
         }};
     }
 
     @ExceptionHandler
     @ResponseBody
     public Map<String, String> handle(final BackplaneServerException bse, HttpServletResponse response) {
         logger.error("Backplane server error: " + bse.getMessage(), bpConfig.getDebugException(bse));
         response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
         return new HashMap<String,String>() {{
             put(ERR_MSG_FIELD, bpConfig.isDebugMode() ? bse.getMessage() : "Service unavailable");
         }};
     }
 
     /**
      * Handle all other errors
      */
     @ExceptionHandler
     @ResponseBody
    public Map<String, String> handle(final Exception e, HttpServletResponse response) {
        logger.error("Error handling backplane request: " + e.getMessage(), bpConfig.getDebugException(e));
         response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
         return new HashMap<String,String>() {{
             put(ERR_MSG_FIELD, bpConfig.isDebugMode() ? e.getMessage() : "Error processing request.");
         }};
     }
 
     public static String randomString(int length) {
         byte[] randomBytes = new byte[length];
         random.nextBytes(randomBytes);
         for (int i = 0; i < length; i++) {
             byte b = randomBytes[i];
             int c = Math.abs(b % 16);
             if (c < 10) c += 48; // map (0..9) to '0' .. '9'
             else c += (97 - 10);   // map (10..15) to 'a'..'f'
             randomBytes[i] = (byte) c;
         }
         try {
             return new String(randomBytes, "US-ASCII");
         }
         catch (UnsupportedEncodingException e) {
             logger.error("US-ASCII character encoding not supported", e); // shouldn't happen
             return null;
         }
     }
     
     // - PRIVATE
 
     private static final Logger logger = Logger.getLogger(Backplane1Controller.class);
 
     private static final String NEW_CHANNEL_LAST_PATH = "new";
     private static final String ERR_MSG_FIELD = "ERR_MSG";
     private static final int CHANNEL_NAME_LENGTH = 32;
 
     private final com.yammer.metrics.core.Timer getBusMessagesTime =
             Metrics.newTimer(new MetricName("v1", this.getClass().getName().replace(".","_"), "get_bus_messages_time"), TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
 
     private final com.yammer.metrics.core.Timer getChannelMessagesTime =
             Metrics.newTimer(new MetricName("v1", this.getClass().getName().replace(".","_"), "get_channel_messages_time"), TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
 
     private final com.yammer.metrics.core.Timer getNewChannelTime =
             Metrics.newTimer(new MetricName("v1", this.getClass().getName().replace(".","_"), "get_new_channel_time"), TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
 
     private final com.yammer.metrics.core.Timer postMessagesTime =
             Metrics.newTimer(new MetricName("v1", this.getClass().getName().replace(".","_"), "post_messages_time"), TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
 
     private final Histogram payLoadSizesOnGets = Metrics.newHistogram(new MetricName("v1", this.getClass().getName().replace(".","_"), "payload_sizes_gets"));
 
     @Inject
     private BackplaneConfig bpConfig;
 
     private static final Random random = new SecureRandom();
 
     private void checkAuth(String basicAuth, String bus, BusConfig1.BUS_PERMISSION permission) throws AuthException, BackplaneServerException {
         // authN
         String userPass = null;
         if ( basicAuth == null || ! basicAuth.startsWith("Basic ") || basicAuth.length() < 7) {
             authError("Invalid Authorization header: " + basicAuth);
         } else {
             try {
                 userPass = new String(Base64.decodeBase64(basicAuth.substring(6).getBytes("utf-8")));
             } catch (UnsupportedEncodingException e) {
                 authError("Cannot check authentication, unsupported encoding: utf-8"); // shouldn't happen
             }
         }
 
         @SuppressWarnings({"ConstantConditions"})
         int delim = userPass.indexOf(":");
         if (delim == -1) {
             authError("Invalid Basic auth token: " + userPass);
         }
         String user = userPass.substring(0, delim);
         String pass = userPass.substring(delim + 1);
 
         BP1User userEntry;
 
         userEntry = BP1DAOs.getUserDao().get(user);
 
         if (userEntry == null) {
             authError("User not found: " + user);
         } else if ( ! HmacHashUtils.checkHmacHash(pass, userEntry.get(BP1User.Field.PWDHASH)) ) {
             authError("Incorrect password for user " + user);
         }
 
         // authZ
         BusConfig1 busConfig;
 
         busConfig = BP1DAOs.getBusDao().get(bus);
 
         if (busConfig == null) {
             authError("Bus configuration not found for " + bus);
         } else if (!busConfig.getPermissions(user).contains(permission)) {
             authError("User " + user + " denied " + permission + " to " + bus);
         }
     }
 
     private void authError(String errMsg) throws AuthException {
         logger.error(errMsg);
         try {
             throw new AuthException("Access denied. " + (bpConfig.isDebugMode() ? errMsg : ""));
         } catch (Exception e) {
             throw new AuthException("Access denied.");
         }
     }
 
     private String newChannel() {
     	final TimerContext context = getNewChannelTime.time();
         String newChannel = "\"" + randomString(CHANNEL_NAME_LENGTH) +"\"";
         context.stop();
     	return newChannel;
     }
 
     private String getChannelMessages(final String bus, final String channel, final String since, final String sticky, final String version) throws MessageException, BackplaneServerException {
 
         final TimerContext context = getChannelMessagesTime.time();
 
         try {
             List<BackplaneMessage> messages = BP1DAOs.getMessageDao().getMessagesByChannel(bus, channel, since, sticky);
             List<Map<String,Object>> frames = new ArrayList<Map<String, Object>>();
 
             for (BackplaneMessage message : messages) {
                 frames.add(message.asFrame(version));
             }
 
             ObjectMapper mapper = new ObjectMapper();
             try {
                 String payload = mapper.writeValueAsString(frames);
                 payLoadSizesOnGets.update(payload.length());
                 return payload;
             } catch (IOException e) {
                 String errMsg = "Error converting frames to JSON: " + e.getMessage();
                 logger.error(errMsg, bpConfig.getDebugException(e));
                 throw new BackplaneServerException(errMsg, e);
             }
         } catch (MessageException sdbe) {
             throw sdbe;
         } catch (BackplaneServerException bse) {
             throw bse;
         } catch (Exception e) {
             throw new BackplaneServerException(e.getMessage(), e);
         } finally {
             context.stop();
         }
     }
 }

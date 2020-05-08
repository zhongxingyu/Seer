 /*
  * Sonar, open source software quality management tool.
  * Copyright (C) 2010 SonarSource
  * mailto:contact AT sonarsource DOT com
  *
  * Sonar is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * Sonar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with Sonar; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 
 package org.sonar.plugins.twitter;
 
 import static org.sonar.plugins.twitter.TwitterPlugin.HOST_DEFAULT_VALUE;
 import static org.sonar.plugins.twitter.TwitterPlugin.HOST_PROPERTY;
 import static org.sonar.plugins.twitter.TwitterPlugin.PASSWORD_PROPERTY;
 import static org.sonar.plugins.twitter.TwitterPlugin.TWITTER_TOKEN;
 import static org.sonar.plugins.twitter.TwitterPlugin.TWITTER_TOKEN_ID;
 import static org.sonar.plugins.twitter.TwitterPlugin.TWITTER_TOKEN_SECRET;
 import static org.sonar.plugins.twitter.TwitterPlugin.USERNAME_PROPERTY;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import org.apache.commons.configuration.Configuration;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sonar.api.batch.PostJob;
 import org.sonar.api.batch.SensorContext;
 import org.sonar.api.resources.Project;
 
 import twitter4j.Status;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.http.AccessToken;
 import twitter4j.http.RequestToken;
 
 /**
  * @author Evgeny Mandrikov
  * @author Akram Ben Aissi
  */
 public class TwitterPublisher implements PostJob {
 
   private static final String PROJECT_INDEX_URI = "/project/index/";
  private final static Logger logger = LoggerFactory.getLogger(TwitterPublisher.class);
  private final static TwitterFactory factory = new TwitterFactory();
  private final Twitter twitter = factory.getInstance();
 
   public void executeOn(Project project, SensorContext context) {
     Configuration configuration = project.getConfiguration();
     String hostUrl = configuration.getString(HOST_PROPERTY, HOST_DEFAULT_VALUE);
 
     try {
       AccessToken accessToken = loadAccessToken(configuration);
       if (accessToken.getToken() == null || accessToken.getTokenSecret() == null) {
         authenticate(configuration);
         accessToken = loadAccessToken(configuration);
       }
       StringBuilder url = new StringBuilder(hostUrl).append(PROJECT_INDEX_URI).append(project.getKey());
       String status = String.format("Sonar analysis of %s is available at %s", project.getName(), url);
       updateStatus(status);
     } catch (TwitterException e) {
      logger.warn("Exception updating Twitter status", e);
     } catch (IOException e) {
      logger.warn("Exception updating Twitter status", e);
     }
   }
 
   public void updateStatus(String message) throws TwitterException {
    logger.info("Updating Twitter status to: '{}'", message);
     try {
       Status status = twitter.updateStatus(message);
      logger.info("Successfully updated the status to [" + status.getText() + "].");
     } catch (TwitterException e) {
      logger.warn("Exception updating Twitter status", e);
     }
   }
 
   private void authenticate(Configuration configuration) throws TwitterException, IOException {
 
     String key = configuration.getString(USERNAME_PROPERTY);
     String secret = configuration.getString(PASSWORD_PROPERTY);
     twitter.setOAuthConsumer(key, secret);
 
     RequestToken requestToken = twitter.getOAuthRequestToken();
     AccessToken accessToken = null;
     BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
     while (null == accessToken) {
      logger.info("Open the following URL and grant access to your account:");
      logger.info(requestToken.getAuthorizationURL());
      logger.info("Enter the PIN(if aviailable) or just hit enter.[PIN]:");
       String pin = br.readLine();
       try {
         if (pin.length() > 0) {
           accessToken = twitter.getOAuthAccessToken(requestToken, pin);
         } else {
           accessToken = twitter.getOAuthAccessToken();
         }
       } catch (TwitterException te) {
         if (401 == te.getStatusCode()) {
          logger.error("Unable to get the access token.");
         } else {
          logger.error("Unexpected Twitter error: " + te.getMessage(), te);
         }
       }
     }
     int id = twitter.verifyCredentials().getId();
     // persist to the accessToken for future reference.
     storeAccessToken(configuration, id, accessToken);
 
     // unset clear password on the configuration
     configuration.setProperty(USERNAME_PROPERTY, null);
     configuration.setProperty(PASSWORD_PROPERTY, null);
   }
 
   private void storeAccessToken(Configuration configuration, int id, AccessToken accessToken) {
     configuration.setProperty(TWITTER_TOKEN, accessToken.getToken());
     configuration.setProperty(TWITTER_TOKEN_SECRET, accessToken.getTokenSecret());
     configuration.setProperty(TWITTER_TOKEN_ID, id);
   }
 
   private AccessToken loadAccessToken(Configuration configuration) {
     String token = configuration.getString(TWITTER_TOKEN);
     String tokenSecret = configuration.getString(TWITTER_TOKEN_SECRET);
     return new AccessToken(token, tokenSecret);
   }
 }

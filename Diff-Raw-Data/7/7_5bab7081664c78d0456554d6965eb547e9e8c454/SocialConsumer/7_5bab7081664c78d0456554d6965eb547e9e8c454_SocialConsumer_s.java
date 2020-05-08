 /**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.camel.component.social;
 
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.camel.Exchange;
 import org.apache.camel.Message;
 import org.apache.camel.Processor;
 import org.apache.camel.component.social.path.RateLimitExceededException;
 import org.apache.camel.component.social.path.SocialDataFetchError;
 import org.apache.camel.component.social.path.SocialPathConsumer;
 import org.apache.camel.component.social.path.SocialPathSessionAware;
 import org.apache.camel.component.social.path.SocialPathSessionAwareWrapper;
 import org.apache.camel.impl.ScheduledPollConsumer;
 
 public class SocialConsumer extends ScheduledPollConsumer implements Processor {
 
 	private SocialEndpoint endpoint;
 	private SocialPathConsumer socialPathConsumer;
 	SocialConfiguration config;
 	private Object MASTER_USER = new Object();
 	private Map<Object, Deque<String>> lastIdMap;
 	private boolean autoDelay;
 	private long startedOn;
 
 	public SocialConsumer(SocialEndpoint endpoint, Processor processor)
 			throws Exception {
 		super(endpoint, processor);
 		this.endpoint = endpoint;
 
 		config = endpoint.getConfiguration();
 		socialPathConsumer = endpoint.createSocialPath();
 
 		if (config.hasAuth() && config.hasConsumerAuth()) {
 			SocialPathSessionAware wrapper = SocialPathSessionAwareWrapper
 					.wrapper(socialPathConsumer);
 
 			SocialOAuth userCredentials = new SocialOAuth(
 					config.getOauthToken(), config.getOauthSecret());
 
 			wrapper.initSession(endpoint.getOAuthConsumer(null),
 					userCredentials);
 		}
 
 		lastIdMap = new HashMap<Object, Deque<String>>(config.getMaxKeepId());
 	}
 
 	@Override
 	public void start() throws Exception {
 		startedOn = System.currentTimeMillis();
 		super.start();
 	}
 
 	@Override
 	public void resume() {
 		startedOn = System.currentTimeMillis();
		super.resume();
 	}
 
 	@Override
 	protected void poll() throws Exception {
 		SocialOAuth oAuthUser = endpoint.getOAuthUser(null);
 		Object token = oAuthUser != null ? oAuthUser.getToken() : MASTER_USER;
 		pollSocialPath(socialPathConsumer, getLastId(token),
 				oAuthUser, endpoint.getOAuthConsumer(null), getQueryParams());
 	}
 
 	private void pollSocialPath(SocialPathConsumer socialPathConsumer,
 			String sinceSocialId, SocialOAuth userOAuth, SocialOAuth consumerOAuth, Map<String, Object> specificParams) throws Exception {
 
 		// TODO remover
 		Iterable<SocialData> socialData = null;
 		try {
 			socialData = socialPathConsumer.readData(sinceSocialId, specificParams);
 		} catch (RateLimitExceededException e) {
 			long timeToWait = e.getDelay();
 			if (autoDelay == true) {
 				long timeNow = System.currentTimeMillis();
 				long howLong = timeNow - startedOn;
 				timeToWait = TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS)
 						- howLong;
 			} else {
 				timeToWait = e.getDelay();
 			}
 
 			Timer timer = new Timer();
 			timer.schedule(new TimerTask() {
 				@Override
 				public void run() {
 					try {
 						synchronized (SocialConsumer.this) {
 							SocialConsumer.this.resume();
 						}
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			}, timeToWait);
 
 			synchronized (this) {
 				suspend();
 			}
 
 		} catch (SocialDataFetchError e) {
 			throw e;
 		}
 
 		if (socialData == null) {
 			return;
 		}
 
 		for (SocialData sd : socialData) {
 			if (config.isSkipRead()) {
 				Object token = userOAuth != null ? userOAuth.getToken() : MASTER_USER;
 				Deque<String> idHistory = lastIdMap.get(token);
 				if (idHistory != null && idHistory.contains(sd.getId())) {
 					continue;
 				}
 			}
 
 			consume(socialPathConsumer, sd, userOAuth, consumerOAuth);
 		}
 	}
 
 	private void addToHistory(Object userToken, String id) {
 		if (userToken == null) {
 			userToken = MASTER_USER;
 		}
 		Deque<String> history = lastIdMap.get(userToken);
 		if (history == null) {
 			history = new LinkedList<String>();
 			lastIdMap.put(userToken, history);
 		}
 
 		if (history.size() + 1 == config.getMaxKeepId()) {
 			history.removeFirst();
 		}
 
 		history.add(id);
 	}
 
 	private String getLastId(Object userToken) {
 		if (config.isSkipRead() && lastIdMap.containsKey(userToken)) {
 			return lastIdMap.get(userToken).getLast();
 		}
 
 		return null;
 	}
 
 	@Override
 	public void stop() throws Exception {
 		if (socialPathConsumer instanceof SocialPathSessionAware) {
 			SocialPathSessionAware session = (SocialPathSessionAware) socialPathConsumer;
 
 			if (session.isSessionActive()) {
 				session.endSession();
 			}
 		}
 
 		super.stop();
 	}
 
 	private void consume(SocialPathConsumer socialPC, SocialData socialData,
 			SocialOAuth userOAuth, SocialOAuth consumerOAuth) throws Exception {
 		Exchange e = getEndpoint().createExchange();
 		endpoint.configureMessage(e.getIn(), socialData);
 		e.getIn().setHeader(SocialHeaders.SOCIAL_CONSUMER_OAUTH, consumerOAuth);
 		e.getIn().setHeader(SocialHeaders.SOCIAL_USER_OAUTH, userOAuth);
 		getProcessor().process(e);
 
 		Object token = userOAuth != null ? userOAuth.getToken() : MASTER_USER;
 		addToHistory(token, socialData.getId());
 	}
 
 	public void process(Exchange arg0) throws Exception {
 		Message in = arg0.getIn();
 		SocialOAuth userOAuth = endpoint.getOAuthUser(arg0);
 		SocialOAuth consumerOAuth = endpoint.getOAuthConsumer(arg0);
 
 		if (userOAuth != null && consumerOAuth == null) {
 			log.warn("Message with user OAuth credentials was found. But this social endpoint has no consumer OAuth credentials and no credentials were found on message headers.");
 			return;
 		}
 
 		SocialPathConsumer spc = socialPathConsumer;
 		if (userOAuth != null && consumerOAuth != null) {
 			spc = endpoint.createSocialPath();
 			SocialPathSessionAware wrapper = SocialPathSessionAwareWrapper
 					.wrapper(spc);
 			wrapper.initSession(consumerOAuth, userOAuth);
 		}
 
 		boolean pollPath = in.getHeader(SocialHeaders.SOCIAL_POLL_PATH,
 				Boolean.FALSE, Boolean.class);
 		String sinceSocialId = in.getHeader(SocialHeaders.SOCIAL_SINCE_ID,
 				String.class);
 
 		if (sinceSocialId == null) {
 			sinceSocialId = getLastId(userOAuth != null ? userOAuth.token : config
 					.getOauthToken());
 		}
 
 		if (pollPath) {
 			Map<String, Object> specificParams = in.getHeader(SocialHeaders.SOCIAL_PROVIDER_PARAMS, getQueryParams(), Map.class);
 			pollSocialPath(spc, sinceSocialId, userOAuth, consumerOAuth, specificParams);
 		}
 
 		String body = in.getBody(String.class);
 		boolean bodyIsString = body != null;
 		boolean updatePath = in.getHeader(SocialHeaders.SOCIAL_UPDATE_PATH,
 				bodyIsString, Boolean.class);
 		if (updatePath) {
 			updateSocialPath(spc, in.getBody(),
 					new HashMap<String, Object>(in.getHeaders()), userOAuth, consumerOAuth);
 		}
 
 		if (userOAuth != null) {
 			SocialPathSessionAware wrapper = SocialPathSessionAwareWrapper
 					.wrapper(spc);
 			wrapper.endSession();
 		}
 	}
 
 	private Map<String, Object> getQueryParams() {
 		Map<String, Object> queryParams = new HashMap<String, Object>();
 		queryParams.put("q", config.getQuery());
 		return queryParams;
 	}
 
 	private void updateSocialPath(SocialPathConsumer spc, Object body,
 			HashMap<String, Object> headers, SocialOAuth userOAuth, SocialOAuth consumerOAuth) throws Exception {
 		try {
 			SocialData data = spc.updateData(body, headers);
 			if (data != null) {
 				consume(spc, data, userOAuth, consumerOAuth);
 			}
 		} catch (SocialDataFetchError e) {
 			log.warn("Could not process received status update", e);
 		}
 	}
 
 	public boolean isSocialPathAuthRequired() {
 		SocialPathSessionAware wrapper = SocialPathSessionAwareWrapper
 				.wrapper(socialPathConsumer);
 		return wrapper.isAuthRequired();
 	}
 
 	protected void setAutoDelay(boolean value) {
 		this.autoDelay = value;
 	}
 }

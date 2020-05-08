 /**
  * Copyright (C) 2012 RECIA http://www.recia.fr
  * @Author (C) 2012 Maxime Bossard <mxbossard@gmail.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 /**
  * 
  */
 package org.esco.portlet.changeetab.service.impl;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import javax.portlet.PortletRequest;
 
 import org.esco.portlet.changeetab.service.IUserInfoService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.util.Assert;
 import org.springframework.util.StringUtils;
 
 /**
  * @author GIP RECIA 2013 - Maxime BOSSARD.
  *
  */
 public class BasicUserInfoService implements IUserInfoService, InitializingBean {
 
 	/** Logger. */
 	private static final Logger LOG = LoggerFactory.getLogger(BasicUserInfoService.class);
 
 	private String userIdInfoKey;
 
 	private String etabIdsInfoKey;
 
 	private String currentEtabIdInfoKey;
 
 	private final Map<String, List<String>> basicUserInfoMap = new HashMap<String, List<String>>();
 
 	private final Map<String, List<String>> emptyUserInfoMap = new HashMap<String, List<String>>();
 
 	private final Map<String, List<String>> testUserInfoMap = this.basicUserInfoMap;
 
 	@Override
 	public Collection<String> getChangeableEtabIds(final PortletRequest request) {
 		final Collection<String> etabIds = this.getUserInfo(request, this.etabIdsInfoKey);
 		final Collection<String> etabIdsLowerCase = new HashSet<String>(etabIds.size());
 
 		if (etabIds.isEmpty()) {
 			// Multivalued attribute which should not be empty
 			BasicUserInfoService.LOG.error("Unable to retrieve {} attribute in Portal UserInfo !", this.etabIdsInfoKey);
 		} else {
 			for (final String id : etabIds) {
 				etabIdsLowerCase.add(id.toLowerCase());
 			}
 		}
 
 		return etabIdsLowerCase;
 	}
 
 	@Override
 	public String getCurrentEtabId(final PortletRequest request) {
 		String escoUaiCourant = null;
 
 		final List<String> uaiCourant = this.getUserInfo(request, this.currentEtabIdInfoKey);
 
 		if (uaiCourant.size() == 1) {
 			// Monovalued attribute
 			escoUaiCourant = uaiCourant.iterator().next().toLowerCase();
 		}
 
 		if (!StringUtils.hasText(escoUaiCourant)) {
 			escoUaiCourant = null;
 			BasicUserInfoService.LOG.warn("Unable to retrieve {} attribute in Portal UserInfo !", this.currentEtabIdInfoKey);
 		}
 
 		return escoUaiCourant;
 	}
 
 	@Override
 	public String getUserId(final PortletRequest request) {
 		String userId = null;
 
 		final List<String> id = this.getUserInfo(request, this.userIdInfoKey);
 
 		if (id.size() == 1) {
 			// Monovalued attribute
 			userId = id.iterator().next();
 		}
 
 		if (!StringUtils.hasText(userId)) {
 			userId = null;
 			BasicUserInfoService.LOG.warn("Unable to retrieve {} attribute in Portal UserInfo !", this.userIdInfoKey);
 		}
 
 		return userId;
 	}
 
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		Assert.hasText(this.userIdInfoKey, "No User Id user info key configured !");
 		Assert.hasText(this.etabIdsInfoKey, "No Etab Ids user info key configured !");
 		Assert.hasText(this.currentEtabIdInfoKey, "No Current Etab Id user info key configured !");
 
 		this.basicUserInfoMap.put(this.userIdInfoKey, Arrays.asList(new String[]{"f1000ugr"}));
 		this.basicUserInfoMap.put(this.etabIdsInfoKey, Arrays.asList(new String[]{"0450822x","0333333y","0377777U"}));
 		this.basicUserInfoMap.put(this.currentEtabIdInfoKey, Arrays.asList(new String[]{"0450822X"}));
 
 		this.emptyUserInfoMap.put(this.userIdInfoKey, Arrays.asList(new String[]{"id2"}));
 		this.emptyUserInfoMap.put(this.etabIdsInfoKey, Arrays.asList(new String[]{"1234567b"}));
 		this.emptyUserInfoMap.put(this.currentEtabIdInfoKey, Arrays.asList(new String[]{"1234567B"}));
 	}
 
 	/**
 	 * Retrieve the user info attribute from portlet context, or the Mocked user info
 	 * if the system property testEnv = true.
 	 * 
 	 * @param request the portlet request
 	 * @param atributeName the attribute to retrieve
 	 * @return the user info attribute values
 	 */
 	@SuppressWarnings("unchecked")
 	public List<String> getUserInfo(final PortletRequest request, final String attributeName) {
 		Map<String, List<String>> userInfo =
 				(Map<String, List<String>>) request.getAttribute("org.jasig.portlet.USER_INFO_MULTIVALUED");
 
 		if ((userInfo == null) && "true".equals(System.getProperty("testEnv"))) {
 			userInfo = this.testUserInfoMap;
 		}
 
		List<String> attributeValues = null;
 
 		if (userInfo != null) {
 			attributeValues = userInfo.get(attributeName);
 		} else {
 			BasicUserInfoService.LOG.error("Unable to retrieve Portal UserInfo !");
 			throw new IllegalStateException("Unable to retrieve Portal UserInfo !");
 		}
 
 		return attributeValues;
 	}
 
 	/**
 	 * Getter of userIdInfoKey.
 	 *
 	 * @return the userIdInfoKey
 	 */
 	public String getUserIdInfoKey() {
 		return this.userIdInfoKey;
 	}
 
 	/**
 	 * Setter of userIdInfoKey.
 	 *
 	 * @param userIdInfoKey the userIdInfoKey to set
 	 */
 	public void setUserIdInfoKey(final String userIdInfoKey) {
 		this.userIdInfoKey = userIdInfoKey;
 	}
 
 	/**
 	 * Getter of etabIdsInfoKey.
 	 *
 	 * @return the etabIdsInfoKey
 	 */
 	public String getEtabIdsInfoKey() {
 		return this.etabIdsInfoKey;
 	}
 
 	/**
 	 * Setter of etabIdsInfoKey.
 	 *
 	 * @param etabIdsInfoKey the etabIdsInfoKey to set
 	 */
 	public void setEtabIdsInfoKey(final String etabIdsInfoKey) {
 		this.etabIdsInfoKey = etabIdsInfoKey;
 	}
 
 	/**
 	 * Getter of currentEtabIdInfoKey.
 	 *
 	 * @return the currentEtabIdInfoKey
 	 */
 	public String getCurrentEtabIdInfoKey() {
 		return this.currentEtabIdInfoKey;
 	}
 
 	/**
 	 * Setter of currentEtabIdInfoKey.
 	 *
 	 * @param currentEtabIdInfoKey the currentEtabIdInfoKey to set
 	 */
 	public void setCurrentEtabIdInfoKey(final String currentEtabIdInfoKey) {
 		this.currentEtabIdInfoKey = currentEtabIdInfoKey;
 	}
 
 }

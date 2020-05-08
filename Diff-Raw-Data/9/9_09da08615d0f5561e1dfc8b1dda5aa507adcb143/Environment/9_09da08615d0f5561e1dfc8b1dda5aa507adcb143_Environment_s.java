 /**
  * Copyright (c) 2012, 2013 SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  *     disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod.util;
 
 import javax.annotation.PostConstruct;
 import org.apache.commons.lang.Validate;
 import nl.surfnet.bod.domain.ProtectionType;
 import org.joda.time.DateTimeZone;
 import org.joda.time.Duration;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 
 @Component("bodEnvironment")
 public class Environment {
 
   @Value("${shibboleth.imitate}")
   private boolean imitateShibboleth;
 
   @Value("${shibboleth.imitate.displayName}")
   private String imitateShibbolethDisplayName;
 
   @Value("${shibboleth.imitate.userId}")
   private String imitateShibbolethUserId;
 
   @Value("${shibboleth.logout.url}")
   private String shibbolethLogoutUrl;
 
   @Value("${shibboleth.imitate.email}")
   private String imitateShibbolethEmail;
 
   @Value("${os.url}")
   private String openSocialUrl;
 
   @Value("${os.oauth-key}")
   private String openSocialOAuthKey;
 
   @Value("${os.oauth-secret}")
   private String openSocialOAuthSecret;
 
   @Value("${bod.external.url}")
   private String externalBodUrl;
 
   @Value("${bod.development}")
   private boolean development;
 
   @Value("${bod.version}")
   private String version;
 
   @Value("${bod.env}")
   private String environment;
 
   @Value("${google.analytics.code}")
   private String googleAnalyticsCode;
 
   @Value("${feedbacktool.enabled}")
   private boolean feedbackToolEnabled;
 
   @Value("${os.group.noc}")
   private String nocGroup;
 
   @Value("${oauth.server.url}")
   private String oauthServerUrl;
 
   @Value("${oauth.admin.clientId}")
   private String adminClientId;
 
   @Value("${oauth.admin.secret}")
   private String adminSecret;
 
   @Value("${oauth.client.clientId}")
   private String clientClientId;
 
   @Value("${oauth.client.secret}")
   private String clientSecret;
 
   @Value("${oauth.resource.key}")
   private String resourceKey;
 
   @Value("${oauth.resource.secret}")
   private String resourceSecret;
 
   @Value("${sab.enabled}")
   private boolean sabEnabled;
 
   @Value("${vers.enabled}")
   private boolean versEnabled;
 
   @Value("${institute.cache.max.age.in.hours}")
   private int instituteCacheMaxAgeInHours;
 
   @Value("${nsi.reserve.held.timeout.value.in.seconds}")
   private int nsiReserveHeldTimeoutValueInSeconds;
 
   @Value("${nsi.v2.service.url}")
   private String nsiV2ServiceUrl;
 
   @Value("${nsi.v2.service.type}")
   private String nsiV2ServiceType;
 
   @Value("${nbi.default.protection.type}")
   private ProtectionType defaultProtectionType;
 
   @Value("${nbi.setup.time}")
   private int nbiSetupTime;
 
   @Value("${nbi.teardown.time}")
   private int nbiTeardownTime;
 
   @Value("${healthcheck.timeout.seconds}")
   private int healthcheckTimeoutInSeconds;
 
   public Environment() {
   }
 
   public Environment(boolean imitateShibboleth, String imitateShibbolethUserId, String imitateShibbolethDisplayName,
       String imitateShibbolethEmail, String shibbolethLogoutUrl) {
     this.imitateShibboleth = imitateShibboleth;
     this.imitateShibbolethUserId = imitateShibbolethUserId;
     this.imitateShibbolethDisplayName = imitateShibbolethDisplayName;
     this.imitateShibbolethEmail = imitateShibbolethEmail;
     this.shibbolethLogoutUrl = shibbolethLogoutUrl;
   }
 
   @PostConstruct
   public void validate() {
     Validate.isTrue(nbiSetupTime >= 0, "nbi.setup.time must be non-negative");
     Validate.isTrue(nbiTeardownTime >= 0, "nbi.teardown.time must be non-negative");
   }
 
   public String getOpenSocialUrl() {
     return openSocialUrl;
   }
 
   public String getOpenSocialOAuthKey() {
     return openSocialOAuthKey;
   }
 
   public String getOpenSocialOAuthSecret() {
     return openSocialOAuthSecret;
   }
 
   public boolean getImitateShibboleth() {
     return imitateShibboleth;
   }
 
   public String getImitateShibbolethDisplayName() {
     return imitateShibbolethDisplayName;
   }
 
   public String getImitateShibbolethUserId() {
     return imitateShibbolethUserId;
   }
 
   public String getImitateShibbolethEmail() {
     return imitateShibbolethEmail;
   }
 
   public String getShibbolethLogoutUrl() {
     return shibbolethLogoutUrl;
   }
 
   public String getExternalBodUrl() {
     return externalBodUrl;
   }
 
   public boolean isDevelopment() {
     return development;
   }
 
   public void setDevelopment(boolean development) {
     this.development = development;
   }
 
   public String getGoogleAnalyticsCode() {
     return googleAnalyticsCode;
   }
 
   public void setGoogleAnalyticsCode(String googleAnalyticsCode) {
     this.googleAnalyticsCode = googleAnalyticsCode;
   }
 
   public String getVersion() {
     return version;
   }
 
   public String getNocGroup() {
     return nocGroup;
   }
 
   public String getDefaultTimeZoneId() {
     return DateTimeZone.getDefault().getID();
   }
 
   public String getDefaultTimeZoneOffset() {
     final int offsetInMillis = DateTimeZone.getDefault().getOffset(null);
 
    final int offsetHours = offsetInMillis / (60 * 60 * 1000);
    final int offsetMinutes = offsetInMillis % offsetHours;
 
     return String.format("UTC %0+3d:%02d", offsetHours, offsetMinutes);
   }
 
   public String getAdminClientId() {
     return adminClientId;
   }
 
   public String getAdminSecret() {
     return adminSecret;
   }
 
   public String getClientClientId() {
     return clientClientId;
   }
 
   public String getClientSecret() {
     return clientSecret;
   }
 
   public String getResourceKey() {
     return resourceKey;
   }
 
   public String getResourceSecret() {
     return resourceSecret;
   }
 
   public boolean isSabEnabled() {
     return sabEnabled;
   }
 
   public String getOauthServerUrl() {
     return oauthServerUrl;
   }
 
   public void setOauthServerUrl(String oauthServerUrl) {
     this.oauthServerUrl = oauthServerUrl;
   }
 
   public void setResourceKey(String resourceKey) {
     this.resourceKey = resourceKey;
   }
 
   public void setResourceSecret(String resourceSecret) {
     this.resourceSecret = resourceSecret;
   }
 
   public void setOpenSocialUrl(String openSocialUrl) {
     this.openSocialUrl = openSocialUrl;
   }
 
   public void setOpenSocialOAuthKey(String openSocialOAuthKey) {
     this.openSocialOAuthKey = openSocialOAuthKey;
   }
 
   public void setOpenSocialOAuthSecret(String openSocialOAuthSecret) {
     this.openSocialOAuthSecret = openSocialOAuthSecret;
   }
 
   public boolean isFeedbackToolEnabled() {
     return feedbackToolEnabled;
   }
 
   public String getEnvironment() {
     return environment;
   }
 
   public Duration getInstituteCacheMaxAge() {
     return Duration.standardHours(instituteCacheMaxAgeInHours);
   }
 
   public int getNsiReserveHeldTimeoutValueInSeconds() {
     return nsiReserveHeldTimeoutValueInSeconds;
   }
 
   public String getNsiV2ServiceUrl() {
     return nsiV2ServiceUrl;
   }
 
   public String getNsiV2ServiceType() {
     return nsiV2ServiceType;
   }
 
   public boolean isVersEnabled() {
     return versEnabled;
   }
 
   public ProtectionType getDefaultProtectionType() {
     return defaultProtectionType;
   }
 
   public int getNbiSetupTime() {
     return nbiSetupTime;
   }
 
   public int getNbiTeardownTime() {
     return nbiTeardownTime;
   }
 
   public int getHealthcheckTimeoutInSeconds() {
     return healthcheckTimeoutInSeconds;
   }
 }

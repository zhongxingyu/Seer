 package org.motechproject.ananya.kilkari.reporting.repository;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.motechproject.ananya.kilkari.reporting.profile.ProductionProfile;
 import org.motechproject.ananya.reports.kilkari.contract.request.CallDetailsReportRequest;
 import org.motechproject.ananya.reports.kilkari.contract.request.SubscriberReportRequest;
 import org.motechproject.ananya.reports.kilkari.contract.request.SubscriptionReportRequest;
 import org.motechproject.ananya.reports.kilkari.contract.request.SubscriptionStateChangeRequest;
 import org.motechproject.ananya.reports.kilkari.contract.response.LocationResponse;
 import org.motechproject.ananya.reports.kilkari.contract.response.SubscriberResponse;
 import org.motechproject.http.client.domain.Method;
 import org.motechproject.http.client.service.HttpClientService;
 import org.motechproject.web.context.HttpThreadContext;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Repository;
 import org.springframework.web.client.HttpClientErrorException;
 import org.springframework.web.client.RestTemplate;
 import org.springframework.web.util.UriComponentsBuilder;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import static org.motechproject.ananya.kilkari.reporting.domain.URLPath.*;
 
 @Repository
 @ProductionProfile
 public class ReportingGatewayImpl implements ReportingGateway {
     private RestTemplate restTemplate;
     private HttpClientService httpClientService;
     private Properties kilkariProperties;
 
     @Autowired
     public ReportingGatewayImpl(RestTemplate kilkariRestTemplate, HttpClientService httpClientService, @Qualifier("kilkariProperties") Properties kilkariProperties) {
         this.restTemplate = kilkariRestTemplate;
         this.httpClientService = httpClientService;
         this.kilkariProperties = kilkariProperties;
     }
 
     @Override
     public LocationResponse getLocation(String district, String block, String panchayat) {
         List<NameValuePair> locationParameters = constructParameterMap(district, block, panchayat);
         String url = constructGetLocationUrl(locationParameters);
         try {
             return restTemplate.getForEntity(url, LocationResponse.class).getBody();
         } catch (HttpClientErrorException ex) {
             if (ex.getStatusCode().equals(HttpStatus.NOT_FOUND))
                 return null;
             throw ex;
         }
     }
 
     @Override
     public SubscriberResponse getSubscriber(String subscriptionId) {
         String url = String.format("%s%s/%s", getBaseUrl(), GET_SUBSCRIBER_PATH, subscriptionId);
         return restTemplate.getForEntity(url, SubscriberResponse.class).getBody();
     }
 
     @Override
     public void reportSubscriptionCreation(SubscriptionReportRequest subscriptionReportRequest) {
         String url = String.format("%s%s", getBaseUrl(), CREATE_SUBSCRIPTION_PATH);
         performHttpRequestBasedOnChannel(url, subscriptionReportRequest, Method.POST);
     }
 
     @Override
     public void reportSubscriptionStateChange(SubscriptionStateChangeRequest subscriptionStateChangeRequest) {
         String subscriptionId = subscriptionStateChangeRequest.getSubscriptionId();
         String url = String.format("%s%s/%s", getBaseUrl(), SUBSCRIPTION_STATE_CHANGE_PATH, subscriptionId);
         performHttpRequestBasedOnChannel(url, subscriptionStateChangeRequest, Method.PUT);
     }
 
     @Override
     public void reportCampaignMessageDeliveryStatus(CallDetailsReportRequest callDetailsReportRequest) {
         String url = String.format("%s%s", getBaseUrl(), CALL_DETAILS_PATH);
         performHttpRequestBasedOnChannel(url, callDetailsReportRequest, Method.POST);
     }
 
     @Override
     public void reportSubscriberDetailsChange(String subscriptionId, SubscriberReportRequest subscriberReportRequest) {
         String url = String.format("%s%s/%s", getBaseUrl(), SUBSCRIBER_UPDATE_PATH, subscriptionId);
         performHttpRequestBasedOnChannel(url, subscriberReportRequest, Method.PUT);
     }
 
     @Override
     public void reportChangeMsisdnForSubscriber(String subscriptionId, String msisdn) {
         String url = String.format("%s%s?subscriptionId=%s&msisdn=%s", getBaseUrl(), CHANGE_MSISDN_PATH, subscriptionId, msisdn);
         performHttpRequestBasedOnChannel(url, null, Method.POST);
     }
 
     private boolean isCallCenterCall() {
         String channel = HttpThreadContext.get();
         return "CONTACT_CENTER".equalsIgnoreCase(channel);
     }
 
     private <T> void performHttpRequestBasedOnChannel(String url, T postObject, Method method) {
         if (isCallCenterCall()) {
             try {
                 httpClientService.executeSync(url, postObject, method);
             } catch (Exception e) {
                 httpClientService.execute(url, postObject, method);
             }
         } else
             httpClientService.execute(url, postObject, method);
     }
 
     private String constructGetLocationUrl(List<NameValuePair> params) {
         UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
         try {
             uriComponentsBuilder.uri(new URI(getBaseUrl())).path(GET_LOCATION_PATH);
         } catch (URISyntaxException e) {
             throw new RuntimeException("Error constructing reporting URI", e);
         }
 
         for (NameValuePair nameValuePair : params) {
             uriComponentsBuilder.queryParam(nameValuePair.getName(), nameValuePair.getValue());
         }
 
        return uriComponentsBuilder.build().toString();
     }
 
     private List<NameValuePair> constructParameterMap(String district, String block, String panchayat) {
         List<NameValuePair> locationParameters = new ArrayList<>();
         if (StringUtils.isNotBlank(district)) locationParameters.add(new BasicNameValuePair("district", district));
         if (StringUtils.isNotBlank(block)) locationParameters.add(new BasicNameValuePair("block", block));
         if (StringUtils.isNotBlank(panchayat)) locationParameters.add(new BasicNameValuePair("panchayat", panchayat));
         return locationParameters;
     }
 
     private String getBaseUrl() {
         String baseUrl = kilkariProperties.getProperty("reporting.service.base.url");
         return baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
     }
 }

 package com.pocketsunited.facebook.service;
 
 import com.fasterxml.jackson.core.type.TypeReference;
 import com.pocketsunited.facebook.data.AppRequest;
 import com.pocketsunited.facebook.data.User;
 import com.pocketsunited.facebook.exceptions.GenericFacebookGraphAPIException;
 import com.pocketsunited.facebook.protocol.data.DataResponse;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.util.EntityUtils;
 
 import javax.annotation.PostConstruct;
 import java.util.List;
 
 /**
  * @author Michael Duergner <michael@pocketsunited.com>
  */
 public class FacebookGraphUserService extends AbstractFacebookGraphService implements IFacebookGraphUserService {
 
     @PostConstruct
     public void postConstruct() {
         super.postConstruct();
     }
 
     @Override
     public User me() throws GenericFacebookGraphAPIException {
         return read("me");
     }
 
     @Override
     public User me(String userAccessToken) throws GenericFacebookGraphAPIException {
         return read("me",userAccessToken);
     }
 
     @Override
     public User read(String userId) throws GenericFacebookGraphAPIException {
         return read(userId,null);
     }
 
     @Override
     public User read(String userId, String userAccessToken) throws GenericFacebookGraphAPIException {
         HttpGet request = null;
         try {
             request = new HttpGet(buildRequestUrl(new String[] {userId},userAccessToken));
             HttpResponse response = httpClient.execute(request);
             if (200 == response.getStatusLine().getStatusCode()) {
                 DataResponse<User> user = objectMapper.readValue(response.getEntity().getContent(), new TypeReference<DataResponse<User>>() {});
                 return user.getData();
             }
             else {
                 logger.warn("Got status code {} when trying to read User  with ID {} with status line {} and content {}",new Object[] {response.getStatusLine().getStatusCode(),userId,response.getStatusLine(),EntityUtils.toString(response.getEntity())});
                 throw new GenericFacebookGraphAPIException("Got unexpected response code ("+response.getStatusLine().getStatusCode()+") while reading User for ID "+userId);
             }
         }
         catch (Throwable t) {
             logger.error("Got {} while trying to read user for UserID {} from Facebook",new Object[] {t.getClass().getSimpleName(),userId},t);
             throw new GenericFacebookGraphAPIException("While reading User for ID "+userId,t);
         }
         finally {
             if (null != request && !request.isAborted()) {
                 request.abort();
             }
         }
     }
 
     @Override
     public List<User> friends(String userAccessToken) throws GenericFacebookGraphAPIException {
         return friends("me",userAccessToken);
     }
 
     @Override
     public List<User> friends(String userId, String userAccessToken) throws GenericFacebookGraphAPIException {
         HttpGet request = null;
         try {
             request = new HttpGet(buildRequestUrl(new String[] {userId,"friends"},userAccessToken));
             HttpResponse response = httpClient.execute(request);
            if (200 == response.getStatusLine().getStatusCode()) {
                DataResponse<List<User>> friends = objectMapper.readValue(response.getEntity().getContent(), new TypeReference<DataResponse<List<User>>>() {});
                 return friends.getData();
             }
             else {
                 logger.warn("Got status code {} when trying to read Friends  for user {} with status line {} and content {}",new Object[] {response.getStatusLine().getStatusCode(),userId,response.getStatusLine(),EntityUtils.toString(response.getEntity())});
                 throw new GenericFacebookGraphAPIException("Got unexpected response code ("+response.getStatusLine().getStatusCode()+") while reading Friends for ID "+userId);
             }
         }
         catch (Throwable t) {
             logger.error("Got {} while trying to read friends for UserID {} from Facebook",new Object[] {t.getClass().getSimpleName(),userId},t);
             throw new GenericFacebookGraphAPIException("While reading Friends for ID "+userId,t);
         }
         finally {
             if (null != request && !request.isAborted()) {
                 request.abort();
             }
         }
     }
 
     @Override
     public List<AppRequest> readAppRequests(String userId, String userAccessToken) throws GenericFacebookGraphAPIException {
         HttpGet request = null;
         try {
             request = new HttpGet(buildRequestUrl(new String[] {userId,"apprequests"},userAccessToken));
             HttpResponse response = httpClient.execute(request);
             if (200 == response.getStatusLine().getStatusCode()) {
                 DataResponse<List<AppRequest>> appRequests = objectMapper.readValue(response.getEntity().getContent(),new TypeReference<DataResponse<List<AppRequest>>>() {});
                 logger.debug("Read {} AppRequests from Facebook for UserID {}",new Object[] {appRequests.getData().size(),userId});
                 return appRequests.getData();
             }
             else {
                 logger.warn("Got StatusCode {} with StatusLine {} and Body {} when trying to read AppRequests from Facebook for UserID {}",new Object[] {response.getStatusLine().getStatusCode(),response.getStatusLine(),EntityUtils.toString(response.getEntity()),userId});
                 throw new GenericFacebookGraphAPIException("Got unexpected response code ("+response.getStatusLine().getStatusCode()+") while reading AppRequests for ID "+userId);
             }
         }
         catch (Throwable t) {
             logger.error("Got {} while trying to read AppRequests for UserID {} from Facebook",new Object[] {t.getClass().getSimpleName(),userId},t);
             throw new GenericFacebookGraphAPIException("While reading AppRequests for ID "+userId,t);
         }
         finally {
             if (null != request && !request.isAborted()) {
                 request.abort();
             }
         }
     }
 
     @Override
     public boolean deleteAppRequest(String requestId, String userId, String accessToken) throws GenericFacebookGraphAPIException {
         HttpDelete request = null;
         String fullRequestId = requestId+"_"+userId;
         try {
             request = new HttpDelete(buildRequestUrl(new String[] {fullRequestId},accessToken));
             HttpResponse response = httpClient.execute(request);
             if (200 == response.getStatusLine().getStatusCode()) {
                 logger.debug("Deleted appRequest with id {} for user {}", new Object[]{requestId, userId});
                 return true;
             }
             else {
                 logger.warn("Got status code {} when trying to delete appRequest with id {} for user {} with status line {} and content {}",new Object[] {response.getStatusLine().getStatusCode(),requestId,userId,response.getStatusLine(),EntityUtils.toString(response.getEntity())});
                 throw new GenericFacebookGraphAPIException("Got unexpected response code ("+response.getStatusLine().getStatusCode()+") while deleting AppRequest with ID "+fullRequestId);
             }
         }
         catch (Throwable t) {
             logger.error("Got {} while trying to delete AppRequest {} for UserID {} from Facebook",new Object[] {t.getClass().getSimpleName(),requestId,userId},t);
             throw new GenericFacebookGraphAPIException("While deleting AppRequest with ID "+fullRequestId,t);
         }
         finally {
             if (null != request && !request.isAborted()) {
                 request.abort();
             }
         }
     }
 }

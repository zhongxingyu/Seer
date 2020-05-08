 /*
  * Copyright (C) 2003-2011 eXo Platform SAS.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.exoplatform.social.client.core.service;
 
 import java.io.IOException;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.util.EntityUtils;
 import org.exoplatform.social.client.api.auth.AccessDeniedException;
 import org.exoplatform.social.client.api.common.RealtimeListAccess;
 import org.exoplatform.social.client.api.model.Activity;
 import org.exoplatform.social.client.api.model.Comment;
 import org.exoplatform.social.client.api.model.Identity;
 import org.exoplatform.social.client.api.model.Like;
 import org.exoplatform.social.client.api.net.SocialHttpClient.POLICY;
 import org.exoplatform.social.client.api.service.ActivityService;
 import org.exoplatform.social.client.api.service.ServiceException;
 import org.exoplatform.social.client.core.model.ActivityImpl;
 import org.exoplatform.social.client.core.util.SocialHttpClientSupport;
 import org.exoplatform.social.client.core.util.SocialJSONDecodingSupport;
 import org.json.simple.parser.ParseException;
 
 /**
  * Implementation of {@link ActivityService}.
  *
  * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
  * @since Jun 28, 2011
  */
 public class ActivityServiceImpl extends ServiceBase<Activity, ActivityService<Activity>> implements ActivityService<Activity> {
 
   /**
    * {@inheritDoc}
    */
   @Override
   public Activity create(Activity newInstance) throws AccessDeniedException, ServiceException {
     return null;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public Activity get(String uuid) throws AccessDeniedException, ServiceException {
     final String targetURL = "" + uuid;
     HttpResponse response = SocialHttpClientSupport.executeGet(targetURL, POLICY.BASIC_AUTH);
     try {
       return SocialJSONDecodingSupport.parser(ActivityImpl.class, response);
     } catch (IOException ioex) {
       throw new ServiceException(ActivityServiceImpl.class, "IOException when reads Json Content.", ioex);
       
     } catch (ParseException pex) {
       throw new ServiceException(ActivityServiceImpl.class, "ParseException when reads Json Content.", pex);
     }
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public Activity update(Activity existingInstance) throws AccessDeniedException, ServiceException {
     // TODO Auto-generated method stub
     return null;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void delete(Activity existingInstance) throws AccessDeniedException, ServiceException {
     // TODO Auto-generated method stub
     
   }
 
 
   /**
    * {@inheritDoc}
    */
   @Override
   public RealtimeListAccess<Activity> getActivityStream(Identity identity) throws AccessDeniedException,
                                                                           ServiceException {
     // TODO Auto-generated method stub
     return null;
   }
 
 
   /**
    * {@inheritDoc}
    */
   @Override
   public Comment createComment(Activity existingActivity, Comment newComment) throws AccessDeniedException,
                                                                                      ServiceException {
     // TODO Auto-generated method stub
     return null;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public Comment getComment(String commentId) throws AccessDeniedException, ServiceException {
     // TODO Auto-generated method stub
     return null;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void deleteComment(Comment existingComment) throws AccessDeniedException, ServiceException {
     // TODO Auto-generated method stub
     
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public Like like(Activity existingActivity, Identity existingIdentity) throws AccessDeniedException,
                                                                                 ServiceException {
     // TODO Auto-generated method stub
     return null;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public Like getLike(String likeId) throws AccessDeniedException, ServiceException {
     // TODO Auto-generated method stub
     return null;
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public void unlike(Like existingLike) throws AccessDeniedException, ServiceException {
     // TODO Auto-generated method stub
     
   }
 
 }

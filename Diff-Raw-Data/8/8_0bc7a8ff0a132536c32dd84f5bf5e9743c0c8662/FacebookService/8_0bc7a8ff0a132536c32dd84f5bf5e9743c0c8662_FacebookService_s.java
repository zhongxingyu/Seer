 /**
  * Copyright (c) 2013 HAN University of Applied Sciences
  * Arjan Oortgiese
  * Joëll Portier
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 package com.dare2date.businessservice;
 
 import com.dare2date.applicationservice.IAuthorisationService;
 import com.dare2date.domein.facebook.FacebookData;
 import com.dare2date.externeservice.facebook.IFacebookAPI;
 
 /**
  * Facebook service.
  */
 public class FacebookService {
     private IAuthorisationService authorisationService;
     private IFacebookAPI facebook;
 
     /**
      * Set the authorisation service.
      *
      * @param authorisationService Authorisation service.
      */
     public void setAuthorisationService(IAuthorisationService authorisationService) {
         this.authorisationService = authorisationService;
     }
 
     /**
      * Set the Facebook API implementation to use.
      *
      * @param facebook Facebook API implementation.
      */
     public void setFacebook(IFacebookAPI facebook) {
         this.facebook = facebook;
     }
 
     /**
      * Get Facebook events, work history and education history form the user.
      *
      * @param username Dare2Data username.
      * @return FacebookData object with History, Education and events.
      */
     public FacebookData getFacebookMatch(String username) {
         String accessToken = authorisationService.getAccessToken(username, "facebook");
 
         FacebookData data = new FacebookData();
 
        data.setEvents(facebook.getUsersEvents(accessToken));
        data.setWorkHistory(facebook.getUsersWorkHistory(accessToken));
        data.setEducationHistory(facebook.getUsersEducationHistory(accessToken));
 
         return data;
     }
 }

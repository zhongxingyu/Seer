 /**
  *    Copyright 2012 meltmedia
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package com.meltmedia.cadmium.captcha;
 
 import javax.servlet.http.HttpServletRequest;
 
 import net.tanesha.recaptcha.ReCaptchaImpl;
 
 /**
  * Checks the validity of the incoming Http request with ReCaptcha.
  * 
  * @author John McEntire
  *
  */
 public final class CaptchaValidator {
   private final String CHALLENGE_FIELD_NAME = "recaptcha_challenge_field";
   private final String RESPONSE_FIELD_NAME = "recaptcha_response_field";
   
   private ReCaptchaImpl reCaptcha;
   
   /**
    * Initializes an instance with the given privateKey.
    * @param privateKey
    */
   public CaptchaValidator(String privateKey) {
     reCaptcha = new ReCaptchaImpl();
     reCaptcha.setPrivateKey(privateKey);
   }
   
   /**
    * Validates the captcha response.
    * 
    * @param request
    * @return
    */
   public boolean isValid(HttpServletRequest request) {
     String remoteAddr = request.getRemoteAddr();
     String challenge = request.getParameter(CHALLENGE_FIELD_NAME);
     String response = request.getParameter(RESPONSE_FIELD_NAME);
    if(challenge != null && response != null && challenge.trim().length() > 0) {
      return reCaptcha.checkAnswer(remoteAddr, challenge, response).isValid();
    } else {
      return false;
    }
   }
 }

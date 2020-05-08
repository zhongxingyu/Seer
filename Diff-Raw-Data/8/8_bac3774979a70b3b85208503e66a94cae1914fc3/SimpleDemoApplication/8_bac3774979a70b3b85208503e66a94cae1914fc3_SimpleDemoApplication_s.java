 /*
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  *  Copyright (c) 2013, Janrain, Inc.
  *
  *  All rights reserved.
  *
  *  Redistribution and use in source and binary forms, with or without modification,
  *  are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, this
  *    list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation and/or
  *    other materials provided with the distribution.
  *  * Neither the name of the Janrain, Inc. nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
  *
  *
  *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
  *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  */
 
 package com.janrain.android.simpledemo;
 
 import android.app.Application;
 import com.janrain.android.Jump;
 import com.janrain.android.JumpConfig;
 
 public class SimpleDemoApplication extends Application {
     @Override
     public void onCreate() {
         super.onCreate();
 
         JumpConfig jumpConfig = new JumpConfig();
 
         //capture prod
         jumpConfig.engageAppId = "appcfamhnpkagijaeinl";
         jumpConfig.captureDomain = "mobile-dev.janraincapture.com";
         jumpConfig.captureClientId = "gpy4j6d8bcsepkb2kzm7zp5qkk8wrza6";
         jumpConfig.captureLocale = "en-US";
         jumpConfig.captureTraditionalSignInFormName = "userInformationForm";
         jumpConfig.traditionalSignInType = Jump.TraditionalSignInType.EMAIL;
         jumpConfig.captureAppId = "rz47skmnm48jpcv3bh4eguv4b8";
         jumpConfig.captureFlowName = "standard_flow";
         jumpConfig.captureSocialRegistrationFormName = "socialRegistrationForm";
         jumpConfig.captureTraditionalRegistrationFormName = "registrationForm";
         jumpConfig.captureEditUserProfileFormName = "editProfileForm";
         jumpConfig.captureEnableThinRegistration = false;
        jumpConfig.captureForgotPasswordFormName="recoverPassword"  ;
 
         Jump.init(this, jumpConfig);
     }
 }

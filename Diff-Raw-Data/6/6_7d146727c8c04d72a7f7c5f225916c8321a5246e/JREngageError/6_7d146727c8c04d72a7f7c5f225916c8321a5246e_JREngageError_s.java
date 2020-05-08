 /*
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  *  Copyright (c) 2011, Janrain, Inc.
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
 package com.janrain.android.engage;
 
 /**
  * @brief A representation of an error encountered by the JREngage library
  * @nosubgrouping
  */
 public class JREngageError {
     public static final int CODE_UNKNOWN = 0;
 
     /**
      * @name Public Constants
      * Constant pools for different kinds of errors.
      **/
 /*@{*/
 
     /**
      * @brief String based error categorizations. See JREngageError#mType for more information.
      */
     public static final class ErrorType {
         /**
          * Reserved
          */
         public static final String NO_NETWORK_CONNECTION = "noNetwork";
 
         /**
          * Reserved
          */
         public static final String MINOR = "minor";
 
         /**
          * Reserved
          */
         public static final String MAJOR = "major";
 
         /**
          * The Engage configuration query failed
          */
         public static final String CONFIGURATION_FAILED = "configurationFailed";
 
         /**
          * There are no providers configured on the Engage dashboard for the Engage app
          */
         public static final String CONFIGURATION_INFORMATION_MISSING = "missingInformation";
 
         /**
          * The authentication failed because of:
          * - There was a network error in the WebView used to authenticate the user
          * - There was an unrecognized Engage error during authentication (details found in the
          *   JREngageError message)
          * - There was a network error contacting Engage during authentication (details found in the
          *   JREngageError exception)
          */
         public static final String AUTHENTICATION_FAILED = "authenticationFailed";
 
         /**
          * The social publishing failed because:
          * - There was a response JSON decoding error
          * - There was an unrecognized error response from Engage
          * - The Engage app's Facebook credential has exceeded its feed action request limit
          * - There was an unrecognized Facebook error (found in the JREngageError message)
          */
         public static final String PUBLISH_FAILED = "publishFailed";
 
         /**
          * The social publishing failed because the OAuth publishing token associated with the mobile
          * device token has expired or is otherwise no longer valid.
          */
         public static final String PUBLISH_NEEDS_REAUTHENTICATION = "publishNeedsReauthentication";
 
         /**
          * The social publishing failed because Engage rejected the supplied activity as invalid.
          * - With code SocialPublishingError.LINKEDIN_CHARACTER_EXCEEDED the error was caused by a too long
          *   message for LinkedIn
          * - With code SocialPublishingError.DUPLICATE_TWITTER the error was caused by a duplicate Twitter
          *   status update.
          * - With code SocialPublishingError.FAILED the error cause was unknown, details may be found in the
          *   JREngageError message.
          */
         public static final String PUBLISH_INVALID_ACTIVITY = "publishInvalidActivity";
     }
 
     /**
      * @brief Container for error code constants related to library configuration. See JREngageError#mCode
      * for more information.
      */
     public static final class ConfigurationError {
         private static final int START = 100;
 
         /**
          * Reserved
          */
         public static final int URL_ERROR = START;
 
         /**
          * Reserved
          */
         public static final int DATA_PARSING_ERROR = START + 1;
 
         /**
          * Error parsing JSON from Engage configuration query, possible walled garden?
          */
         public static final int JSON_ERROR = START + 2;
 
         /**
          * The Engage configuration query failed with an exception.
          */
         public static final int CONFIGURATION_INFORMATION_ERROR = START + 3;
 
         /**
          * Reserved
          */
         public static final int SESSION_DATA_FINISH_GET_PROVIDERS_ERROR = START + 4;
 
         /**
          * Reserved
          */
         public static final int DIALOG_SHOWING_ERROR = START + 5;
 
         /**
          * Reserved
          */
         public static final int PROVIDER_NOT_CONFIGURED_ERROR = START +6;
     }
 
     /**
      * @brief Container for error code constants related to authentication. See JREngageError#mCode for
      * more information.
      */
     public static final class AuthenticationError {
         private static final int START = 200;
 
         /**
          * The authentication failed because of:
          * - There was a network error in the WebView used to authenticate the user
          * - There was an unrecognized Engage error during authentication (details found in the
          *   JREngageError message)
          * - There was a network error contacting Engage during authentication (details found in the
          *   JREngageError exception)
          */
         public static final int AUTHENTICATION_FAILED = START;
     }
 
     /**
      * @brief Container for error code constants related to social publishing. See JREngageError#mCode for
      * more information.
      */
     public static final class SocialPublishingError {
         private static final int START = 300;
 
         /**
          * Social publishing failed because of an unknown reason.
          */
         public static final int FAILED = START;
 
 //        JRPublishErrorBadConnection,
 
         /**
          * Reserved
          */
         public static final int ACTIVITY_NIL = START + 1;
 
 //        JRPublishErrorMissingParameter,
         
         /**
          * Social publishing failed because of a missing Engage API key.
          */
         public static final int MISSING_API_KEY = START + 2;
 
         /**
          * Engage missing API key parameter error.
          */
         public static final int INVALID_OAUTH_TOKEN = START + 3;
 
         /**
          * Duplicate Twitter status update error.
          */
         public static final int DUPLICATE_TWITTER = START + 4;
 
         /**
          * LinkedIn character count limit exceeded.
          */
         public static final int LINKEDIN_CHARACTER_EXCEEDED = START + 5;
 
         /**
          * Facebook feed action request over-limit error.
          */
         public static final int FEED_ACTION_REQUEST_LIMIT = START + 6;
 //        JRPublishErrorCharacterLimitExceeded,
 //        JRPublishErrorFacebookGeneric,
 //        JRPublishErrorInvalidFacebookSession,
 //        JRPublishErrorInvalidFacebookMedia,
 //        //JRPublishErrorInvalidFacebookActionLinks/Properties,
 //        JRPublishErrorTwitterGeneric,
 //        JRPublishErrorDuplicateTwitter,
 //        JRPublishErrorLinkedInGeneric,
 //        JRPublishErrorMyspaceGeneric,
 //        JRPublishErrorYahooGeneric,
     }
 /*@}*/
 
 /**
  * @name Private Attributes
  * The properties of the JREngageError that can be accessed and configured through the object's
  * constructor and getters
  **/
 /*@{*/
 
     /**
      * The integer code for the error, one of the constants defined in ConfigurationError,
      * AuthenticationError, or SocialPublishingError
      */
     private int mCode;
 
     /**
      * A String representation of the type of error.
      */
     private String mType;
 
     /**
      * A human readable message describing the error.
      */
     private String mMessage;
 
     /**
      * A Throwable (frequently an IOException subtype) that precipitated the error.
      */
     private Throwable mCause;
 /*@}*/
 
     public JREngageError(String message, int code, String type) {
         mMessage = message;
         mCode = code;
         mType = type;
     }
 
     public JREngageError(String message, int code, String type, Throwable cause) {
         mMessage = message;
         mCause = cause;
         mCode = code;
         mType = type;
     }
 
 
 /**
  * @name Getters
  * Getters for JREngageError's private properties.
  **/
 /*@{*/
     /**
      * Getter for the mCode property.
      * @return The error code
      */
     public int getCode() {
         return mCode;
     }
 
     /**
      * Getter for the mType property.
      * @return The error type
      */
     public String getType() {
         return mType;
     }
 
     /**
      * Getter for the mMessage property.
      * @return The human readable error message
      */
     public String getMessage() {
         return mMessage;
     }
 
     /**
      * Getter for the mCause property.
      * @return The Throwable which precipitated the error
      */
     public Throwable getException() {
         return mCause;
     }
 
     /**
      * @return True if the error was precipitated by an Exception
      */
     public boolean hasException() {
         return mCause != null;
     }
 /*@}*/
 }

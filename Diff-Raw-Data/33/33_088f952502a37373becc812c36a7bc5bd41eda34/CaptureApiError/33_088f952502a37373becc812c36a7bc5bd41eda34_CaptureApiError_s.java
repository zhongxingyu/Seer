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
 
 package com.janrain.android.capture;
 
 import com.janrain.android.Jump;
 import com.janrain.android.utils.JsonUtils;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static com.janrain.android.utils.CollectionUtils.listFromIterator;
 import static com.janrain.android.utils.JsonUtils.jsonArrayToList;
 
 /**
  * http://developers.janrain.com/documentation/capture/restful_api/
  */
 public class CaptureApiError {
     private String engageToken;
     private String conflictingIdentityProvider;
 
     /**
      * Indicates a form field validation failure, as a result of form submission.
      * See also getLocalizedValidationErrorMessages()
      */
     public static final int FORM_VALIDATION_ERROR = 390;
 
     /**
      * Recoverable.
      *
      * Indicates that the email address associated with the social identity is already in use by an existing
      * Capture record. To recover, perform the "merge account flow".
      */
     public static final int EMAIL_ADDRESS_IN_USE = 380;
 
     /**
      * Recoverable.
      *
      * Indicates a record was not found for the social-identifier associated with the Engage auth_info
      * token used to perform a social sign-in. To recover, perform two-step social registration.
      */
     public static final int RECORD_NOT_FOUND = 310;
 
     /**
      * Generic Error
      */
     public static final int GENERIC_ERROR = -1;
 
     /**
      * Indicates an API response that could not be parsed. Has no meaningful field values.
      */
     public static final CaptureApiError INVALID_API_RESPONSE = new CaptureApiError();
 
     /**
      * The Capture error code. See http://developers.janrain.com/documentation/capture/restful_api/
      */
     public final int code;
 
     /**
      * The error string which is 1:1 associate with the code
      */
     public final String error;
 
     /**
      * A description of this instance of the error, which can vary for a single given code.
      */
     public final String error_description;
 
     /**
      * The raw JSON response
      */
     public final JSONObject raw_response;
 
     /**
      * Indicates an API response that could not be parsed. Has no meaningful fields values.
      */
 
     private CaptureApiError() {
         error = "INVALID_API_RESPONSE";
         code = -1;
         error_description = null;
         raw_response = null;
     }
 
     /**
      * Construct an error object from a JSON response
      * @param response the JSON response
      * @param engageToken the Engage auth_info token in the request precipitating this error, if any
      * @param conflictingProvider for merge errors, the identity provider in the request precipitating this
      *                            error.
      */
     /*package*/ CaptureApiError(JSONObject response, String engageToken, String conflictingProvider) {
         code = response.optInt("code");
         error = response.optString("error");
         error_description = response.optString("error_description");
         raw_response = response;
         this.engageToken = engageToken;
         this.conflictingIdentityProvider = conflictingProvider;
     }
 
     /**
      * Construct a generic CaptureApiError
      * @param description description of the error
      */
     /*package*/ CaptureApiError(String description) {
         code = GENERIC_ERROR;
         error = null;
         error_description = description;
         raw_response = null;
     }
 
     public boolean isInvalidApiResponse() {
         return this == INVALID_API_RESPONSE;
     }
 
     /**
      * @return a human readable (but not end-user facing, and not localized) version of this error
      */
     public String toString() {
         return "<CaptureApiError code: " + code + " error: " + error + " description: " + error_description
                 + ">";
     }
 
     /**
      * @return true if this error represents an invalid password error in a password submission form response
      */
     public boolean isInvalidPassword() {
         if (isInvalidApiResponse()) return false;
         if (error.equals("bad username/password combo")) return true; // legacy username/password endpoint
         return false;
     }
 
     /**
      * @return the end-user-facing localized error messages for form field validation errors resulting from
      *         a form submission.
      */
     public Map<String, List<String>> getLocalizedValidationErrorMessages() {
         JSONObject invalid_fields = raw_response.optJSONObject("invalid_fields");
         if (invalid_fields == null) return null;
         Map<String, List<String>> retval = new HashMap<String, List<String>>();
         List<String> keys = listFromIterator(invalid_fields.keys());
         for (String k : keys) {
            JSONArray fieldMessagesJson = invalid_fields.optJSONObject(k).optJSONArray("messages");
             retval.put(k, ((List<String>) ((List) jsonArrayToList(fieldMessagesJson)))); // bleh
         }
 
         return retval;
     }
 
     /**
      * True if this object is an error representing a merge-flow is required to continue signing in.
      * @return whether a merge-account-flow is required.
      */
     public boolean isMergeFlowError() {
         return code == EMAIL_ADDRESS_IN_USE;
     }
 
     public String getMergeToken() {
         return engageToken;
     }
 
     public String getSocialRegistrationToken() {
         return engageToken;
     }
 
     public String getExistingAccountIdentityProvider() {
         return raw_response.optString("existing_provider");
     }
 
     public String getConflictingIdentityProvider() {
         return conflictingIdentityProvider;
     }
 
     public boolean isTwoStepRegFlowError() {
         return code == RECORD_NOT_FOUND;
     }
 
     public boolean isFormValidationError() {
         return code == FORM_VALIDATION_ERROR;
     }
 
     public JSONObject getPreregistrationRecord() {
         JSONObject preregFields = raw_response.optJSONObject("prereg_fields");
         if (preregFields == null) return null;
         return CaptureRecord.captureRecordWithPrefilledFields(JsonUtils.jsonToCollection(preregFields),
                 Jump.getCaptureFlow());
     }
 }

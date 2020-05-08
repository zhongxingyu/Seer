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
 
 import android.content.Context;
 import android.text.TextUtils;
 import android.util.Base64;
 import android.util.Pair;
 import com.janrain.android.Jump;
 import com.janrain.android.utils.ApiConnection;
 import com.janrain.android.utils.JsonUtils;
 import com.janrain.android.utils.LogUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TimeZone;
 
 import static com.janrain.android.capture.Capture.CaptureApiRequestCallback;
 import static com.janrain.android.capture.Capture.InvalidApidChangeException;
 import static com.janrain.android.utils.ApiConnection.FetchJsonCallback;
 import static com.janrain.android.utils.JsonUtils.copyJsonVal;
 import static com.janrain.android.utils.JsonUtils.unsafeJsonObjectToString;
 import static com.janrain.android.utils.LogUtils.throwDebugException;
 
 public class CaptureRecord extends JSONObject {
     private static final SimpleDateFormat CAPTURE_API_SIGNATURE_DATE_FORMAT;
     private static final String JR_CAPTURE_SIGNED_IN_USER_FILENAME = "jr_capture_signed_in_user";
 
     static {
         CAPTURE_API_SIGNATURE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         CAPTURE_API_SIGNATURE_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
     }
 
     private JSONObject original;
 
     /*package*/ String accessToken;
 
     private CaptureRecord(){}
 
     /**
      * Instantiates a new CaptureRecord model from a JSON representation of the record
      * @param jo a JSON representation of a Capture record, e.g. as from the response to oauth/auth_native
      * @param accessToken the access token returned from the sign-on or registration
      */
     /*package*/ CaptureRecord(JSONObject jo, String accessToken) {
         super();
 
         original = (JSONObject) copyJsonVal(jo);
         JsonUtils.deepCopy(original, this);
         this.accessToken = accessToken;
     }
 
     /**
      * @deprecated
      *
      * Instantiates a new CaptureRecord model from a JSON representation of the record
      * @param jo a JSON representation of a Capture record, e.g. as from the response to oauth/auth_native
      */
     /*package*/ CaptureRecord(JSONObject jo, String accessToken, String refreshSecret) {
         this(jo, accessToken);
     }
 
     /**
      * Loads a Capture user from a well-known filename on disk.
      * @param applicationContext the context from which to interact with the disk
      * @return the loaded record, or null
      */
     public static CaptureRecord loadFromDisk(Context applicationContext) {
         String fileContents = null;
         FileInputStream fis = null;
         try {
             fis = applicationContext.openFileInput(JR_CAPTURE_SIGNED_IN_USER_FILENAME);
             fileContents = CaptureStringUtils.readAndClose(fis);
             fis = null;
             return inflateCaptureRecord(fileContents);
         } catch (FileNotFoundException ignore) {
         } catch (JSONException ignore) {
             throwDebugException(new RuntimeException("Bad CaptureRecord file contents:\n" + fileContents,
                     ignore));
         } finally {
             if (fis != null) try {
                 fis.close();
             } catch (IOException e) {
                 throwDebugException(new RuntimeException(e));
             }
         }
         return null;
     }
 
     private static CaptureRecord inflateCaptureRecord(String jsonifiedRecord) throws JSONException {
         JSONObject serializedVersion = new JSONObject(jsonifiedRecord);
         CaptureRecord inflatedRecord = new CaptureRecord();
         inflatedRecord.original = serializedVersion.getJSONObject("original");
         inflatedRecord.accessToken = serializedVersion.getString("accessToken");
         JsonUtils.deepCopy(serializedVersion.getJSONObject("this"), inflatedRecord);
         return inflatedRecord;
     }
 
     /**
      * Saves the Capture record to a well-known private file on disk.
      * @param applicationContext the context to use to write to disk
      */
     public void saveToDisk(Context applicationContext) {
         FileOutputStream fos = null;
         try {
             fos = applicationContext.openFileOutput(JR_CAPTURE_SIGNED_IN_USER_FILENAME, 0);
             fos.write(deflateCaptureRecord());
         } catch (JSONException e) {
             throwDebugException(new RuntimeException("Unexpected", e));
         } catch (UnsupportedEncodingException e) {
             throwDebugException(new RuntimeException("Unexpected", e));
         } catch (IOException e) {
             throwDebugException(new RuntimeException("Unexpected", e));
         } finally {
             if (fos != null) try {
                 fos.close();
             } catch (IOException e) {
                 throwDebugException(new RuntimeException("Unexpected", e));
             }
         }
     }
 
     private byte[] deflateCaptureRecord() throws JSONException, UnsupportedEncodingException {
         JSONObject serializedVersion = new JSONObject();
         serializedVersion.put("original", original);
         serializedVersion.put("accessToken", accessToken);
         serializedVersion.put("this", this);
         return serializedVersion.toString().getBytes("UTF-8");
     }
 
     /**
      * Deletes the record saved to disk
      * @param applicationContext the context with which to delete the saved record
      */
     public static void deleteFromDisk(Context applicationContext) {
         applicationContext.deleteFile(JR_CAPTURE_SIGNED_IN_USER_FILENAME);
     }
 
     private String getRefreshSignature(String date) {
         if (Jump.getRefreshSecret() == null) return null;
 
         String stringToSign = "refresh_access_token\n" + date + "\n" + accessToken + "\n";
 
         byte[] hash;
         try {
             Mac mac = Mac.getInstance("HmacSHA1");
             byte[] refreshSecret = Jump.getRefreshSecret().getBytes("UTF-8");
             SecretKeySpec secret = new SecretKeySpec(refreshSecret, mac.getAlgorithm());
             mac.init(secret);
             hash = mac.doFinal(stringToSign.getBytes("UTF-8"));
         } catch (NoSuchAlgorithmException e) {
             throw new RuntimeException("Unexpected", e);
         } catch (UnsupportedEncodingException e) {
             throw new RuntimeException("Unexpected", e);
         } catch (InvalidKeyException e) {
             throw new RuntimeException("Unexpected", e);
         }
 
         return Base64.encodeToString(hash, Base64.NO_WRAP);
     }
 
     /**
      * Synchronizes the Capture record with the Capture service
      * Note that this sends any local changes to the service, but does not retrieve updates from the service.
      * @param callback your callback handler
      * @throws InvalidApidChangeException
      */
     public void synchronize(final CaptureApiRequestCallback callback) throws InvalidApidChangeException {
         Set<ApidChange> changeSet = getApidChangeSet();
         List<ApidChange> changeList = new ArrayList<ApidChange>();
         changeList.addAll(changeSet);
 
         if (accessToken == null) throwDebugException(new IllegalStateException());
         fireNextChange(changeList, callback);
     }
 
     private void fireNextChange(final List<ApidChange> changeList, final CaptureApiRequestCallback callback) {
         if (changeList.size() == 0) {
             if (callback != null) callback.onSuccess();
             return;
         }
 
         final ApidChange change = changeList.get(0);
         Set<Pair<String, String>> params = new HashSet<Pair<String, String>>(change.getBodyParams());
         params.add(new Pair<String, String>("access_token", accessToken));
 
         FetchJsonCallback jsonCallback = new FetchJsonCallback() {
             public void run(JSONObject content) {
                if (content.opt("stat").equals("ok")) {
                     LogUtils.logd("Capture", change.toString());
                     LogUtils.logd("Capture", unsafeJsonObjectToString(content, 2));
                     fireNextChange(changeList.subList(1, changeList.size()), callback);
                 } else {
                     if (callback != null) callback.onFailure(new CaptureApiError(content, null, null));
                 }
             }
         };
 
         CaptureApiConnection connection = new CaptureApiConnection(change.getUrlFor());
         connection.addAllToParams(params);
         connection.fetchResponseAsJson(jsonCallback);
     }
 
     private static Set<ApidChange> collapseApidChanges(Set<ApidChange> changeSet) {
         HashMap<String, Set<ApidUpdate>> subentityUpdateBuckets = new HashMap<String, Set<ApidUpdate>>();
 
         Set<ApidChange> collapsedChangeSet = new HashSet<ApidChange>();
         for (ApidChange change : changeSet) {
             if (change instanceof ApidUpdate) {
                 String parent = change.findClosestParentSubentity();
                 ApidUpdate rewritten =
                         rewriteUpdateForParent((ApidUpdate) change, parent);
                 Set<ApidUpdate> bucket = subentityUpdateBuckets.get(parent);
                 if (bucket == null) {
                     subentityUpdateBuckets.put(parent, bucket = new HashSet<ApidUpdate>());
                 }
                 bucket.add(rewritten);
             } else if (change instanceof ApidReplace) {
                 collapsedChangeSet.add(change);
             } else if (change instanceof ApidDelete) {
                 collapsedChangeSet.add(change);
             }
         }
 
         collapsedChangeSet.addAll(collapseApidUpdateBuckets(subentityUpdateBuckets));
 
         return collapsedChangeSet;
     }
 
     private static Set<? extends ApidChange> collapseApidUpdateBuckets(
             Map<String, Set<ApidUpdate>> subentityUpdateBuckets) {
         Set<ApidChange> collapsedApidUpdates = new HashSet<ApidChange>();
         for (String subentity : subentityUpdateBuckets.keySet()) {
             ApidUpdate collapsedUpdate = null;
             for (ApidUpdate update : subentityUpdateBuckets.get(subentity)) {
                 if (collapsedUpdate == null) {
                     collapsedUpdate = new ApidUpdate(update.newVal, update.attrPath);
                 } else {
                     collapsedUpdate = collapsedUpdate.collapseWith(update);
                 }
             }
 
             if (collapsedUpdate != null) {
                 collapsedApidUpdates.add(collapsedUpdate);
             } else {
                 throw new RuntimeException("Unexpected null collapsed update");
             }
         }
 
         return collapsedApidUpdates;
     }
 
     private static ApidUpdate rewriteUpdateForParent(ApidUpdate update, String parent) {
         String subObjectPath = update.attrPath.replaceFirst(parent, "");
         String[] flattenedObjectPaths = subObjectPath.split("/");
         Object newVal = update.newVal;
         for (int i = flattenedObjectPaths.length - 1; i >= 0; i--) {
             String s = flattenedObjectPaths[i];
             if (s.equals("")) continue; // ignore bad~ path components from .split
             JSONObject wrapper = new JSONObject();
             try {
                 wrapper.put(s, newVal);
             } catch (JSONException e) {
                 throw new RuntimeException("Unexpected");
             }
             newVal = wrapper;
         }
         return new ApidUpdate(newVal, parent);
     }
 
     private Set<ApidChange> getApidChangeSet() throws InvalidApidChangeException {
         return collapseApidChanges(CaptureJsonUtils.compileChangeSet(original, this));
     }
 
     /*package*/ static JSONObject captureRecordWithPrefilledFields(Map<String, Object> prefilledFields,
                                                                  Map<String, Object> flow) {
         Map<String, Object> preregAttributes = new HashMap<String, Object>();
         for (Map.Entry<String, Object> entry : prefilledFields.entrySet()) {
             if (entry.getValue() == null) continue;
             Map<String, Object> fieldDefinition = CaptureFlowUtils.getFieldDefinition(flow, entry.getKey());
             if (fieldDefinition != null && !TextUtils.isEmpty((String) fieldDefinition.get("schemaId"))) {
                 preregAttributes.put((String) fieldDefinition.get("schemaId"), entry.getValue());
             }
         }
         return JsonUtils.collectionToJson(preregAttributes);
     }
 
     private String getUTCdatetimeAsString()
     {
         final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
         final String utcTime = sdf.format(new Date());
 
         return utcTime;
     }
 
     /**
      * Uses the refresh secret to refresh the access token
      * @param callback your handler, invoked upon completion
      */
     public void refreshAccessToken(final CaptureApiRequestCallback callback) {
         String date = getUTCdatetimeAsString();
         String signature = getRefreshSignature(date);
 
         if (date == null || accessToken == null || signature == null) {
             callback.onFailure(new CaptureApiError("Unable to generate signature"));
             return;
         }
 
         CaptureApiConnection c = new CaptureApiConnection("/oauth/refresh_access_token");
 
         c.addAllToParams(
                 "access_token", accessToken,
                 "signature", signature,
                 "date", date,
                 "client_id", Jump.getCaptureClientId(),
                 "locale", Jump.getCaptureLocale()
         );
         c.fetchResponseAsJson(new FetchJsonCallback() {
             public void run(JSONObject response) {
                 if (response == null) {
                     callback.onFailure(CaptureApiError.INVALID_API_RESPONSE);
                 } else if ("ok".equals(response.opt("stat"))) {
                     accessToken = (String) response.opt("access_token");
                     callback.onSuccess();
                 } else {
                     callback.onFailure(new CaptureApiError(response, accessToken, null));
                 }
             }
         });
     }
 
     /**
      * @internal
      */
     public static abstract class refreshAccessTokenResultHandler implements ApiConnection.FetchJsonCallback {
         private boolean canceled = false;
         private String authenticationToken;
         private String identityProvider;
 
         public void cancel() {
             canceled = true;
         }
 
         public final void run(JSONObject response) {
             if (canceled) return;
             if (response == null) {
                 onFailure(CaptureApiError.INVALID_API_RESPONSE);
             } else if ("ok".equals(response.opt("stat"))) {
                 Object user = response.opt("capture_user");
                 if (user instanceof JSONObject) {
                     String accessToken = response.optString("access_token");
                     String refreshSecret = response.optString("refresh_secret");
                     CaptureRecord record = new CaptureRecord(((JSONObject) user), accessToken);
                     onSuccess(record, response);
                 } else {
                     onFailure(CaptureApiError.INVALID_API_RESPONSE);
                 }
             } else {
                 onFailure(new CaptureApiError(response, authenticationToken, identityProvider));
             }
         }
 
         public abstract void onSuccess(CaptureRecord record, JSONObject response);
 
         public abstract void onFailure(CaptureApiError error);
     }
 
     public String getAccessToken(){
         return accessToken;
     }
 
     public boolean hasPassword() {
         String password = getPasswordSchemaInfoFromFlow(Jump.getCaptureFlow());
         if (password == null || password.isEmpty()) {
             return true;
         } else return false;
     }
 
     private String getPasswordSchemaInfoFromFlow(Map<String, Object> captureFlow) {
         if (captureFlow == null) return null;
         Map form = (Map) captureFlow.get("schema_info");
         Map fieldNames = (Map) form.get("paths");
         String type = (String) fieldNames.get("password");
         String formFieldValue = CaptureJsonUtils.valueForAttrByDotPath(this, type);
         return formFieldValue;
     }
 }

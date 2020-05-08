 // Copyright 2006 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.remote.http;
 
 import org.joe_e.Struct;
 import org.waterken.http.Message;
 import org.waterken.http.Request;
 import org.waterken.http.Response;
 
 /**
  * A queued HTTP request.
  */
 /* package */ abstract class
Operation extends Struct {
     
     /**
      * Is this an idempotent {@link Operation} whose return might be affected by
      * a subsequent {@linkplain #isUpdate update} {@link Operation}?
      */
     protected final boolean isQuery;
     
     /**
      * Could this {@link Operation} cause side-effects?
      */
     protected final boolean isUpdate;
     
     protected
     Operation(final boolean isQuery, final boolean isUpdate) {
         this.isQuery = isQuery;
         this.isUpdate = isUpdate;
     }
     
     /**
      * Render the request.
      * @param sessionKey    message session key
      * @param window        messaging window number
      * @param index         intra-<code>window</code> index
      * @return corresponding request
      * @throws Exception    any problem
      */
     protected abstract Message<Request>
     render(final String sessionKey, long window, int index) throws Exception;
     
     /**
      * Process the corresponding response.
      * @param request   GUID for request message
      * @param response  received HTTP response
      */
     protected abstract void
     fulfill(String request, Message<Response> response);
     
     /**
      * Process the corresponding rejection.
      * @param request   GUID for request message
      * @param reason    reason response will never be provided
      */
     protected abstract void
     reject(String request, Exception reason);
 }

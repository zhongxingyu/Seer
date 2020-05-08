 //: "The contents of this file are subject to the Mozilla Public License
 //: Version 1.1 (the "License"); you may not use this file except in
 //: compliance with the License. You may obtain a copy of the License at
 //: http://www.mozilla.org/MPL/
 //:
 //: Software distributed under the License is distributed on an "AS IS"
 //: basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 //: License for the specific language governing rights and limitations
 //: under the License.
 //:
 //: The Original Code is Guanxi (http://www.guanxi.uhi.ac.uk).
 //:
 //: The Initial Developer of the Original Code is Alistair Young alistair@smo.uhi.ac.uk.
 //: Portions created by SMO WWW Development Group are Copyright (C) 2005 SMO WWW Development Group.
 //: All Rights Reserved.
 //:
 /* CVS Header
    $Id$
    $Log$
   Revision 1.5  2006/11/17 14:55:23  alistairskye
   Updated setRequestParameters() to fix bug when adding extra request parameters for non spring applications

    Revision 1.4  2006/05/18 13:30:51  alistairskye
    Removed original request.
    Now only stores parameters from original request
 
    Revision 1.3  2006/05/18 09:24:23  alistairskye
    Now stores original request
 
    Revision 1.2  2005/08/12 12:48:31  alistairskye
    Added license
 
    Revision 1.1  2005/08/11 14:17:47  alistairskye
    Class for holding session specific information
 
 */
 
 package org.guanxi.common;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * <font size=5><b></b></font>
  *
  * @author Alistair Young alistair@smo.uhi.ac.uk
  */
 public class Pod {
   /** The URL of the original request */
   private String requestURL = null;
   /** The Guard session ID associated with this Pod */
   private String sessionID = null;
   /** The collection of SAML attributes */
   private Bag attributes = null;
   /** The parameters from the original request */
   private HashMap requestParameters = null;
 
   public void setRequestURL(String requestURL) {
     this.requestURL = requestURL;
   }
 
   public void setSessionID(String sessionID) {
     this.sessionID = sessionID;
   }
 
   public void setAttributes(Bag attributes) {
     this.attributes = attributes;
   }
 
   public void setRequestParameters(Map requestParameters) {
    if (this.requestParameters == null)
      this.requestParameters = new HashMap();
     this.requestParameters.putAll(requestParameters);
   }
 
   public String getRequestURL() { return requestURL; }
   public String getSessionID() { return sessionID; }
   public Bag getAttributes() { return attributes; }
   public HashMap getRequestParameters() { return requestParameters; }
 }

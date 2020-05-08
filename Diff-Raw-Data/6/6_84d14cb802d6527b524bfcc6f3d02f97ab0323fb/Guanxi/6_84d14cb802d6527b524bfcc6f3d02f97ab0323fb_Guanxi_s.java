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
    Revision 1.5  2005/07/21 09:10:34  alistairskye
    Added DEFAULT_AUTHENTICATOR_CONFIG_DIR and DEFAULT_AUTHCOOKIEHANDLER_CONFIG_DIR
 
    Revision 1.4  2005/07/21 09:06:42  alistairskye
    Added DEFAULT_ATTRIBUTOR_CONFIG_DIR
    Added javadocs
 
    Revision 1.3  2005/07/19 14:15:18  alistairskye
    Added NS_IDP_NAME_IDENTIFIER
 
    Revision 1.2  2005/07/11 10:51:34  alistairskye
    Package restructure
 
    Revision 1.1  2005/06/28 11:33:45  alistairskye
    Guanxi definitions
 
 */
 
 package org.guanxi.common.definitions;
 
 /**
  * <font size=5><b></b></font>
  *
  * @author Alistair Young alistair@smo.uhi.ac.uk
  */
 public class Guanxi {
   /** Default location where the Authenticator implementations can find their config files */
   public static final String DEFAULT_AUTHENTICATOR_CONFIG_DIR = "/WEB-INF/config/authenticators/";
 
   /** Default location where the Attributor implementations can find their config files */
   public static final String DEFAULT_ATTRIBUTOR_CONFIG_DIR = "/WEB-INF/config/attributors/";
 
   /** Default location where the AuthCookieHandler implementations can find their config files */
  public static final String DEFAULT_AUTHCOOKIEHANDLER_CONFIG_DIR = "/WEB-INF/config/authenticators/";
 
   /** The Guanxi IdP XML namespace */
   public static final String NS_IDP_NAME_IDENTIFIER = "urn:guanxi:idp";
 
   /** The Guanxi SP XML namespace */
   public static final String NS_SP_NAME_IDENTIFIER = "urn:guanxi:sp";
 }

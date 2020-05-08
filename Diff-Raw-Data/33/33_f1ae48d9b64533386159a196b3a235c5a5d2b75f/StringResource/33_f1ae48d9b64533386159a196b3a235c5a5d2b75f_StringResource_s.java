 /*
  * $Id$
  * (c) Copyright 2000 wingS development team.
  *
  * This file is part of wingS (http://wings.mercatis.de).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 
 package org.wings;
 
 import org.wings.externalizer.ExternalizeManager;
 import org.wings.session.SessionManager;
 import org.wings.session.PropertyService;
 import org.wings.io.Device;
 
 import java.io.IOException;
 
 /**
  * TODO: documentation
  *
  * @author <a href="mailto:hengels@mercatis.de">Holger Engels</a>
  * @version $Revision$
  */
 public class StringResource
     extends Resource
 {
     private final String string;
 
    private final String id;
 
     /**
      * Flags that influence the behaviour of the externalize manager
      */
     protected int externalizerFlags;
 
     public StringResource(String string) {
 	this(string, "txt", "text/plain");
     }
 
     public StringResource(String string, String extension, String mimeType) {
 	this(string, extension, mimeType, ExternalizeManager.FINAL);
     }
 
     public StringResource(String string, String extension, String mimeType, 
 			  int externalizerFlags) {
 	super(extension, mimeType);
 
 	this.string = string;
 	this.externalizerFlags = externalizerFlags;

	ExternalizeManager ext = SessionManager.getSession().getExternalizeManager();
	id = ext.getId(ext.externalize(this, externalizerFlags));
     }
 
     /**
      * TODO: documentation
      *
      * @return
      */
     public int getLength() {
 	return string.length();
     }
 
     public String getId() {
	return id;
     }
 
     public SimpleURL getURL() {
        String name = getId();
        if (extension != null)
            name += "." + extension;
 
         // append the sessionid, if not global
         if ((externalizerFlags & ExternalizeManager.GLOBAL) > 0) {
             return new SimpleURL(name);
         }
         else {
 	    RequestURL requestURL = (RequestURL)getPropertyService().getProperty("request.url");
 	    requestURL = (RequestURL) requestURL.clone();
 	    requestURL.setResource(name);
 	    return requestURL;
 	}
     }
 
     public final void write(Device out) throws IOException {
 	out.print(string);
     }
 
     public int getExternalizerFlags() {
         return externalizerFlags;
     }
 
     protected PropertyService getPropertyService() {
	return (PropertyService)SessionManager.getSession();
     }
 
 }
 
 
 
 
 
 

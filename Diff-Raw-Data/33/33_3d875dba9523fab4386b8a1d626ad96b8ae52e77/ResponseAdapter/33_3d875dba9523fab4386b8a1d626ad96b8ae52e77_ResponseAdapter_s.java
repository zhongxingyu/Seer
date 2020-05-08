 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.geoserver.ows.adapters;
 
 import org.geoserver.ows.util.OwsUtils;
 import org.geoserver.platform.Operation;
 import org.geoserver.platform.ServiceException;
 import org.vfny.geoserver.Request;
 import org.vfny.geoserver.Response;
 import org.vfny.geoserver.global.GeoServer;
 import java.io.IOException;
 import java.io.OutputStream;
 
 
 /**
  * Wraps an old style {@link Response} in a new {@link org.geoserver.ows.Response}.
  * <p>
  * The class binding (see {@link #getBinding()} ), is the implementation of
  * {@link Response} which will delegated to.
  * </p>
  * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
  *
  */
 public class ResponseAdapter extends org.geoserver.ows.Response {
     GeoServer gs;
 
     public ResponseAdapter(Class delegateClass, GeoServer gs) {
         super(delegateClass);
 
         this.gs = gs;
     }
 
     public String getMimeType(Object value, Operation operation)
         throws ServiceException {
         //get the delegate
         Response delegate = (Response) value;
 
         //get the requst object from the operation
         Request request = (Request) OwsUtils.parameter(operation.getParameters(), Request.class);
 
         //the old contract specifies that execute must be called before 
         // get content type
         delegate.execute(request);
 
         //return the content type
         return delegate.getContentType(gs);
     }
 
     public void write(Object value, OutputStream output, Operation operation)
         throws IOException, ServiceException {
         //get the delegate
         Response delegate = (Response) value;
 
         //write the response
         delegate.writeTo(output);
     }
 }

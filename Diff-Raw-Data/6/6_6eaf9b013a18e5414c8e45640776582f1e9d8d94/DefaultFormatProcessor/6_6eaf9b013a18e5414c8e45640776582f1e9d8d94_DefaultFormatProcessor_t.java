 package com.redshape.servlet.core.format;
 
 import com.redshape.servlet.core.IHttpRequest;
 import com.redshape.servlet.core.SupportType;
 import com.redshape.servlet.core.controllers.ProcessingException;
 import com.redshape.utils.Commons;
 import org.apache.commons.lang.StringEscapeUtils;
 
 import java.io.IOException;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: cyril
  * Date: 7/10/12
  * Time: 3:48 PM
  * To change this template use File | Settings | File Templates.
  */
 public class DefaultFormatProcessor implements IRequestFormatProcessor {
 
     @Override
     public SupportType check(IHttpRequest request) throws ProcessingException {
         return SupportType.MUST;
     }
 
     @Override
     public void process(IHttpRequest request) throws ProcessingException {
         try {
             if ( request.isPost() ) {
                 this.processParameters( request, Commons.select( request.getBody(), "" ) );
             }
 
             this.processParameters( request, Commons.select( request.getQueryString(), "" ) );
         } catch ( IOException e ) {
             throw new ProcessingException( e.getMessage(), e );
         }
     }
 
     protected void processParameters( IHttpRequest request, String data ) throws IOException {
        if ( data.isEmpty() ) {
            return;
        }

         for (String param : data.split("&")) {
             String[] paramParts = param.split("=");
 
             String value = paramParts.length > 1 ? paramParts[1] : null;
             String name = URLDecoder.decode(paramParts[0]);
             if ( name.endsWith("[]") ) {
                 name = name.replace("[]", "");
                 if ( !request.hasParameter(name) ) {
                     request.setParameter(name, new ArrayList<Object>());
                 }
 
                 request.getListParameter(name).add(value);
             } else {
                 request.setParameter(name, value != null ? StringEscapeUtils.escapeHtml(URLDecoder.decode(value, "UTF-8")) : null);
             }
         }
     }
 
 }

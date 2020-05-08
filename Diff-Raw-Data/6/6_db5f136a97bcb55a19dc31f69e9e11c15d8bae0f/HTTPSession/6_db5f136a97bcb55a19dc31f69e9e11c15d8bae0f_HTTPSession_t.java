 /*
  * Copyright (c) 2008-2011 David Kellum
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package iudex.http;
 
 import java.io.Closeable;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 import java.util.List;
 
 import com.gravitext.util.Streams;
 
 public abstract class HTTPSession implements Closeable
 {
     public static enum Method {
         GET,
         HEAD
     }
 
     /**
      * Pseudo-HTTP status code: as yet unknown.
      */
     public static final int STATUS_UNKNOWN   =   0;
 
     /**
      * Pseudo-HTTP status code: ERROR delivered as exception
      */
     public static final int ERROR            =  -1;
 
     /**
      * Pseudo-HTTP status code: Critical (i.e. Throwable non-Exception)
      * delivered as exception or re-thrown.
      */
     public static final int ERROR_CRITICAL   =  -2;
 
     /**
      * Pseudo-HTTP status code: Host was not resolved (DNS lookup failed).
      */
     public static final int UNRESOLVED       =  -5;
 
     /**
      * Pseudo-HTTP status code: Response body too large by Content-Length
      * header.
      */
     public static final int TOO_LARGE_LENGTH = -10;
 
     /**
      * Pseudo-HTTP status code: Response body found too large upon reading.
      */
     public static final int TOO_LARGE        = -11;
 
     /**
      * Pseudo-HTTP status code: Server improperly returned Non-Accepted
      * Content-Type (despite our Accept header).
      */
     public static final int NOT_ACCEPTED     = -20;
 
     /**
      *  Pseudo-HTTP status code: A redirect was received with an invalid URL
      *  (i.e. with iudex-core, VisitURL.SyntaxException).
      */
     public static final int INVALID_REDIRECT_URL = -30;
 
    public static final int MISSING_REDIRECT_LOCATION = -31;

    public static final int MAX_REDIRECTS_EXCEEDED = -32;

    public static final int REDIRECT_LOOP = -33;

     /**
      * Pseudo-HTTP status code: Timeout (general)
      */
     public static final int TIMEOUT          = -40;
 
     /**
      * Pseudo-HTTP status code: Timeout waiting on connection.
      */
     public static final int TIMEOUT_CONNECT  = -41;
 
     /**
      * Pseudo-HTTP status code: Timeout on socket read.
      */
     public static final int TIMEOUT_SOCKET   = -42;
 
     public String url()
     {
         return _url;
     }
     public void setUrl( String url )
     {
         _url = url;
     }
     public Method method()
     {
         return _method;
     }
     public void setMethod( Method method )
     {
         _method = method;
     }
 
     public abstract void addRequestHeader( Header header );
 
     public void addRequestHeaders( List<Header> headers )
     {
         for( Header h : headers ) {
             addRequestHeader( h );
         }
     }
 
     public abstract List<Header> requestHeaders();
 
     /**
      * Set accepted mime types.
      */
     public void setAcceptedContentTypes( ContentTypeSet types )
     {
         _acceptedContentTypes = types;
     }
 
     public ContentTypeSet acceptedContentTypes()
     {
         return _acceptedContentTypes;
     }
 
     /**
      * Set maximum length of content body to download in bytes.
      */
     public void setMaxContentLength( int maxContentLength )
     {
         _maxContentLength = maxContentLength;
     }
 
     public int maxContentLength()
     {
         return _maxContentLength;
     }
 
     /**
      * Return HTTP 1.1 status code or Psuedo-code as per above constants:
      * zero or negative.
      */
     public abstract int statusCode();
 
     public abstract String statusText();
 
     public Exception error()
     {
         return _error;
     }
 
     protected void setError( Exception e )
     {
         _error = e;
     }
 
     public abstract List<Header> responseHeaders();
 
     public abstract ByteBuffer responseBody();
 
     public InputStream responseStream()
     {
         ByteBuffer body = responseBody();
 
         if ( body != null ) {
             return Streams.inputStream( body );
         }
         return null;
     }
 
     public abstract void close();
 
     public abstract void abort();
 
     private String _url;
     private Method _method = Method.GET;
     private Exception _error;
 
     private ContentTypeSet _acceptedContentTypes = ContentTypeSet.ANY;
     private int _maxContentLength = 1024 * 1024 - 1;
 }

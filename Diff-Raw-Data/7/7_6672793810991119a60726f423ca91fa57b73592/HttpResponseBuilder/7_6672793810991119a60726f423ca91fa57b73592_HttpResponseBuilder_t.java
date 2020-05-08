 package xingu.netty.http;
 
 import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
 import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
 import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.LOCATION;
 
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
 import org.jboss.netty.handler.codec.http.HttpResponse;
 import org.jboss.netty.handler.codec.http.HttpResponseStatus;
 import org.jboss.netty.handler.codec.http.HttpVersion;
 
 import br.com.ibnetwork.xingu.utils.StringUtils;
 
 public class HttpResponseBuilder
 {
     private final HttpResponse response;
 
     private HttpResponseBuilder(HttpResponse response)
     {
         this.response = response;
     }
 
     public static HttpResponseBuilder builder()
     {
         return builder(HttpResponseStatus.OK);
     }
 
     public static HttpResponseBuilder builder(HttpResponseStatus status)
     {
         HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
         return new HttpResponseBuilder(response);
     }
 
     public HttpResponse build()
     {
         return response;
     }
 
     public HttpResponseBuilder withHeader(String name, Object value)
     {
         String old = response.getHeader(name);
         if(StringUtils.isNotEmpty(old))
         {
             response.removeHeader(name);
         }
         response.addHeader(name, value);
         return this;
     }
 
     public HttpResponseBuilder withContentType(String name)
     {
         return withHeader(CONTENT_TYPE, name);
     }
 
     public HttpResponseBuilder withContent(byte[] content)
     {
         ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(content);
         return withContent(buffer);
     }
 
     public HttpResponseBuilder withContent(String content)
     {
         byte[] bytes = content.getBytes();
         return withContent(bytes);
     }
 
     public HttpResponseBuilder withContent(ChannelBuffer buffer)
     {
         response.setContent(buffer);
         int length = buffer.capacity();
         withContentLength(length);
         return this;
     }
 
     public HttpResponseBuilder withContentLength(int length)
     {
         return withHeader(CONTENT_LENGTH, length);
     }
 
     public HttpResponseBuilder withChunks(boolean chunked)
     {
         response.setChunked(chunked);
         return this;
     }
 
     public HttpResponseBuilder withLocation(String location)
     {
    	HttpResponseStatus status = response.getStatus();
    	if(status == null)
    	{
    		status = HttpResponseStatus.TEMPORARY_REDIRECT;
    	}
        return withLocation(location, status);
     }
     
     public HttpResponseBuilder withLocation(String location, HttpResponseStatus status)
     {
     	withStatus(status);
         withContentLength(0);
         return withHeader(LOCATION, location);
     }
 
     public static HttpResponse redirect(String to)
     {
         return redirect(to, HttpResponseStatus.TEMPORARY_REDIRECT);
     }
 
     public static HttpResponse redirect(String to, HttpResponseStatus status)
     {
         return builder().withLocation(to, status).build();
     }
     
     public HttpResponseBuilder withStatus(HttpResponseStatus status)
     {
     	response.setStatus(status);
     	return this;
     }
     
 }

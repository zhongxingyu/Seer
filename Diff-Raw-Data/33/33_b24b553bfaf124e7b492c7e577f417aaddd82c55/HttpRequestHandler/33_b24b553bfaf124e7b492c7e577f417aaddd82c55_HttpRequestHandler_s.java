 /**
  * ToureNPlaner Event Based Prototype
  * 
  * @author Niklas Schnelle
  * 
  * Initially based on: 
  * 	http://docs.jboss.org/netty/3.2/xref/org/jboss/netty/example/http/snoop/package-summary.html
  */
 package tourenplaner.server.prototype.event;
 
 import static org.jboss.netty.handler.codec.http.HttpHeaders.*;
 import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;
 import static org.jboss.netty.handler.codec.http.HttpResponseStatus.*;
 import static org.jboss.netty.handler.codec.http.HttpVersion.*;
 
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBufferInputStream;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelFutureListener;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
 import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
 import org.jboss.netty.handler.codec.http.HttpRequest;
 import org.jboss.netty.handler.codec.http.HttpResponse;
 import org.jboss.netty.handler.codec.http.QueryStringDecoder;
 import org.jboss.netty.util.CharsetUtil;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 
 import algorithms.ComputeRequest;
 
 import computecore.ComputeCore;
 
 /**
  * @author Niklas Schnelle
  * @version 0.1 Prototype
  */
 public class HttpRequestHandler extends SimpleChannelUpstreamHandler {
 
     private HttpRequest request;
     /** Buffer that stores the response content */
     private final StringBuilder buf = new StringBuilder();
     /** JSONParser we can reuse **/
     private final JSONParser parser = new JSONParser();
     /** MessageDigest object used to compute SHA1**/
     private MessageDigest digester;
     
     /** The ComputeCore managing the threads**/
     private ComputeCore computer;
     
     public HttpRequestHandler(ComputeCore cCore){
     	super();
     	computer = cCore;
     	try {
 			digester = MessageDigest.getInstance("SHA1");
 		} catch (NoSuchAlgorithmException e) {
 			System.err.println("Could not load SHA1 algorithm");
 			System.exit(1);
 		}
     }
 
     @Override
     public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
         
         this.request = (HttpRequest) e.getMessage();
 
 
         /*
         buf.setLength(0);
         buf.append("WELCOME TO THE WILD WILD WEB SERVER\r\n");
         buf.append("===================================\r\n");
 
         buf.append("VERSION: " + request.getProtocolVersion() + "\r\n");
         buf.append("HOSTNAME: " + getHost(request, "unknown") + "\r\n");
         buf.append("REQUEST_URI: " + request.getUri() + "\r\n\r\n");
 
         for (Map.Entry<String, String> h: request.getHeaders()) {
             buf.append("HEADER: " + h.getKey() + " = " + h.getValue() + "\r\n");
         }
         buf.append("\r\n");
         */
 
         QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
         Map<String, List<String>> params = queryStringDecoder.getParameters();
        
        
        
     
         ChannelBuffer content = request.getContent();
        //System.out.println(content.toString(CharsetUtil.UTF_8) );
         if(auth(params, content)){
 	        
 	        InputStreamReader inReader = new InputStreamReader(new ChannelBufferInputStream(content));
 	        /*if (content.readable()) {
 	            buf.append("CONTENT: " + content.toString(CharsetUtil.UTF_8) + "\r\n");
 	        }*/
 	        
 	        JSONObject requestJSON = (JSONObject) parser.parse(inReader);
	        System.out.println(requestJSON);
 	        Map<String, Object> objmap = requestJSON;
 	        String algName = queryStringDecoder.getPath().substring(1);
 	        ComputeRequest req = new ComputeRequest(e.getChannel(), isKeepAlive(request),algName,objmap);
 	        computer.submit(req);
         } else {
         	// Respond with Unauthorized Access
             HttpResponse response = new DefaultHttpResponse(HTTP_1_1, UNAUTHORIZED);
             // Write the response.
             ChannelFuture future = e.getChannel().write(response);
             future.addListener(ChannelFutureListener.CLOSE);
         }
     }
     /**
      * Authenticats the request in the ChannelBuffer content with the parameters given in params
      * see: @link https://gerbera.informatik.uni-stuttgart.de/projects/server/wiki/Authentifizierung
      * for a detailed explanation
      * 
      * @param params
      * @param content
      * @return
      */
     private boolean auth(Map<String, List<String>> params, ChannelBuffer content) {
 		boolean authentic = false;
 		if(!params.isEmpty()){
 			List<String> username;
 			List<String> signature;
 			String realSecret;
 			byte[] bodyhash, finalhash;
 			
 			
 			username = params.get("tp-user");
 			signature = params.get("tp-signature");
 			if(username != null && username.size() == 1 && signature != null && signature.size() == 1) {
				System.out.println("User: "+username.get(0));
				System.out.println("Signature: "+signature.get(0));
 				// TODO replace static test with database access
 				if(username.get(0).equals("FooUser")){
 					realSecret = "FooPassword";
 					// Hash the content
 					digester.reset();
					digester.update(content.array());
 					bodyhash = digester.digest();
 					// Contenthash to String and then hash for SHA1(BODYHASH:SECRET)
 					StringBuilder sb = new StringBuilder();
 					/** @link http://www.spiration.co.uk/post/1199/Java-md5-example-with-MessageDigest **/
 					for (byte element : bodyhash) {
 						sb.append(Character.forDigit((element >> 4) & 0xf, 16));
 						sb.append(Character.forDigit(element & 0xf, 16));
 					}
 					// DEBUG
 					System.out.println("Bodyhash: "+sb.toString());
 					sb.append(':');
 					try {
 						sb.append(realSecret);
 						// Now sb is BODYHASH:SECRET do the final hashing and hex conversion
 						digester.reset();
 						digester.update(sb.toString().getBytes("UTF-8"));
 						finalhash = digester.digest();
 						// Reset the string builder so we can reuse it
						System.out.println("Before Hashing: "+sb.toString());
 						sb.delete(0, sb.length());
 						for (byte element : finalhash) {
 							sb.append(Character.forDigit((element >> 4) & 0xf, 16));
 							sb.append(Character.forDigit(element & 0xf, 16));
 						}
						System.out.println("MyHash: "+sb.toString());
 						authentic = (sb.toString().equals(signature.get(0)))? true: false;
 					} catch (UnsupportedEncodingException e) {
 						System.err.println("We can't fcking find UTF-8 Charset hell wtf?");
 					}
 					
 				}
 				
 				
 			}
 			
 		}
 		return authentic; 
 	}
 
 	private void writeResponse(MessageEvent e) {
         // Decide whether to close the connection or not.
         boolean keepAlive = isKeepAlive(request);
 
         // Build the response object.
         HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
         response.setContent(ChannelBuffers.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));
         response.setHeader(CONTENT_TYPE, "application/json; charset=UTF-8");
 
         if (keepAlive) {
             // Add 'Content-Length' header only for a keep-alive connection.
             response.setHeader(CONTENT_LENGTH, response.getContent().readableBytes());
         }
 
         // Write the response.
         ChannelFuture future = e.getChannel().write(response);
 
         // Close the non-keep-alive connection after the write operation is done.
         if (!keepAlive) {
             future.addListener(ChannelFutureListener.CLOSE);
         }
     }
 
 
 
     @Override
     public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
             throws Exception {
         e.getCause().printStackTrace();
         e.getChannel().close();
     }
 }

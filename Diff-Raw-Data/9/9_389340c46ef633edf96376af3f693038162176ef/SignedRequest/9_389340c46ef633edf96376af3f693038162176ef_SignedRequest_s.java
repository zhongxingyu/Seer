 /**
  * Copyright (c) 2011, salesforce.com, inc.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided
  * that the following conditions are met:
  *
  *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
  *    following disclaimer.
  *
  *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
  *    the following disclaimer in the documentation and/or other materials provided with the distribution.
  *
  *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
  *    promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
  * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
  * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 
 package canvas;
 
 import org.apache.commons.codec.binary.Base64;
 //import org.codehaus.jackson.map.ObjectMapper;
 //import org.codehaus.jackson.map.ObjectReader;
 //import org.codehaus.jackson.type.TypeReference;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 
 import javax.crypto.Mac;
 import javax.crypto.SecretKey;
 import javax.crypto.spec.SecretKeySpec;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.util.Arrays;
 import java.util.HashMap;
 
 /**
  *
  * The utility method can be used to validate/verify the signed request. In this case,
  * the signed request is verified that it is from Salesforce and that it has not been tampered with.
  * <p>
  * This utility class has two methods. One verifies and decodes the request as a Java object the
  * other as a JSON String.
  *
  */
 public class SignedRequest {
 
     public static CanvasRequest verifyAndDecode(String input, String secret) throws SecurityException {
 
         String[] split = getParts(input);
 
         String encodedSig = split[0];
         String encodedEnvelope = split[1];
 
         // Deserialize the json body
         String json_envelope = new String(new Base64(true).decode(encodedEnvelope));
         Gson g = new Gson();
         JsonParser jp = new JsonParser();
         JsonElement je = jp.parse(json_envelope);
         JsonObject canvRequst = je.getAsJsonObject();
         JsonObject context = canvRequst.getAsJsonObject("context");
         JsonObject otherContext = context.getAsJsonObject("user");
         
         CanvasRequest canvasRequest = g.fromJson(canvRequst, CanvasRequest.class) ;
         canvasRequest.setContext(g.fromJson(context, CanvasContext.class));
         canvasRequest.getContext().setUserContext(g.fromJson(otherContext, CanvasUserContext.class));
         otherContext = context.getAsJsonObject("links");
         canvasRequest.getContext().setLinkContext(g.fromJson(otherContext, CanvasLinkContext.class));
         otherContext = context.getAsJsonObject("organization");
         canvasRequest.getContext().setOrganizationContext(g.fromJson(otherContext, CanvasOrganizationContext.class));
         otherContext = context.getAsJsonObject("environment");
         canvasRequest.getContext().setEnvironmentContext(g.fromJson(otherContext, CanvasEnvironmentContext.class));
         
         
         String algorithm; 
         algorithm = canvasRequest.getAlgorithm() == null ? "HMACSHA256" : canvasRequest.getAlgorithm();
 
         verify(secret, algorithm, encodedEnvelope, encodedSig);
 
         // If we got this far, then the request was not tampered with.
         // return the request as a Java object
         return canvasRequest;
     }
 
 
     public static String verifyAndDecodeAsJson(String input, String secret) throws SecurityException {
 
         String[] split = getParts(input);
 
         String encodedSig = split[0];
         String encodedEnvelope = split[1];
 
         String json_envelope = new String(new Base64(true).decode(encodedEnvelope));
         Gson g = new Gson();
         String algorithm;
 
         HashMap<String,Object> o = new HashMap<String, Object>(); // = mapper.readValue(json_envelope, typeRef);
         o = g.fromJson(json_envelope, o.getClass());
            
        algorithm = (String)o.get("algorithm");
 
         verify(secret, algorithm, encodedEnvelope, encodedSig);
 
         // If we got this far, then the request was not tampered with.
         // return the request as a JSON string.
         String outp = g.toJson(o).toString();
         return outp;
     }
 
     private static String[] getParts(String input) {
 
         if (input == null || input.indexOf(".") <= 0) {
             throw new SecurityException(String.format("Input [%s] doesn't look like a signed request", input));
         }
 
         String[] split = input.split("[.]", 2);
         return split;
     }
 
     private static void verify(String secret, String algorithm, String encodedEnvelope, String encodedSig )
         throws SecurityException
     {
     	if (secret == null || secret.trim().length() == 0) {
             throw new IllegalArgumentException("secret is null, did you set your environment variable CANVAS_CONSUMER_SECRET?");
         }
 
         SecretKey hmacKey = null;
         try {
             byte[] key = secret.getBytes();
             hmacKey = new SecretKeySpec(key, algorithm);
             Mac mac = Mac.getInstance(algorithm);
             mac.init(hmacKey);
 
             // Check to see if the body was tampered with
             byte[] digest = mac.doFinal(encodedEnvelope.getBytes());
             byte[] decode_sig = new Base64(true).decode(encodedSig);
             if (! Arrays.equals(digest, decode_sig)) {
                 String label = "Warning: Request was tampered with";
                 throw new SecurityException(label);
             }
         } catch (NoSuchAlgorithmException e) {
             throw new SecurityException(String.format("Problem with algorithm [%s] Error [%s]", algorithm, e.getMessage()), e);
         } catch (InvalidKeyException e) {
             throw new SecurityException(String.format("Problem with key [%s] Error [%s]", hmacKey, e.getMessage()), e);
         }
 
         // If we got here and didn't throw a SecurityException then all is good.
     }
 }

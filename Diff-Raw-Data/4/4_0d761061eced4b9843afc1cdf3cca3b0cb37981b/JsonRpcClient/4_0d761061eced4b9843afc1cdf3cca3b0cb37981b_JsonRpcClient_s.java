 package com.googlecode.jsonrpc4j;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.reflect.Type;
 import java.util.Random;
 
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.JsonProcessingException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.type.TypeFactory;
 import org.codehaus.jackson.node.ObjectNode;
 
 /**
  * A JSON-RPC client.
  */
 public class JsonRpcClient {
 
     private static final String JSON_RPC_VERSION = "2.0";
 
 
     private ObjectMapper mapper;
     private Random random;
 
     /**
      * Creates the {@link JsonRpcHttpClient} bound to the given {@code serviceUrl}.
      * The headers provided in the {@code headers} map are added to every request
      * made to the {@code serviceUrl}.
      *
      * @param serviceUrl the URL
      */
     public JsonRpcClient(ObjectMapper mapper) {
         this.mapper = mapper;
         this.random = new Random(System.currentTimeMillis());
     }
 
     /**
      * Creates the {@link JsonRpcHttpClient} bound to the given {@code serviceUrl}.
      * The headers provided in the {@code headers} map are added to every request
      * made to the {@code serviceUrl}.
      *
      * @param serviceUrl the URL
      */
     public JsonRpcClient() {
         this(new ObjectMapper());
     }
 
     /**
      * Invokes the given method with the given arguments and returns
      * an object of the given type, or null if void.
      *
      * @param methodName   the name of the method to invoke
      * @param arguments    the arguments to the method
      * @param returnType   the return type
      * @param extraHeaders extra headers to add to the request
      * @return the return value
      * @throws Exception on error
      */
     public Object invokeAndReadResponse(
         String methodName, Object[] arguments, Type returnType,
         OutputStream ops, InputStream ips)
         throws Exception {
 
         // invoke it
     	invoke(methodName, arguments, ops);
 
         // read it
         return readResponse(returnType, ips);
     }
 
     /**
      * Invokes the given method with the given arguments and returns
      * an object of the given type, or null if void.
      *
      * @param methodName   the name of the method to invoke
      * @param arguments    the arguments to the method
      * @param returnType   the return type
      * @throws Exception on error
      */
     public void invoke(
         String methodName, Object[] arguments, OutputStream ops)
         throws Exception {
         writeRequest(methodName, arguments, ops, random.nextLong()+"");
         ops.flush();
     }
 
     /**
      * Reads the response from the server.
      *
      * @param returnType the expected return type
      * @param ips        the {@link InnputStream} to read the response from
      * @return the object
      * @throws IOException
      * @throws JsonProcessingException
      * @throws Exception
      * @throws JsonParseException
      * @throws JsonMappingException
      */
     public Object readResponse(Type returnType, InputStream ips)
         throws IOException,
         JsonProcessingException,
         Exception,
         JsonParseException,
         JsonMappingException {
 
         // read the response
         JsonNode response = mapper.readTree(ips);
 
         // bail on invalid response
         if (!response.isObject()) {
             throw new Exception("Invalid JSON-RPC response");
         }
         ObjectNode jsonObject = ObjectNode.class.cast(response);
 
         // detect errors
         if (jsonObject.has("error")
         	&& jsonObject.get("error")!=null
         	&& !jsonObject.get("error").isNull()) {
             ObjectNode errorObject = ObjectNode.class.cast(jsonObject.get("error"));
             throw new Exception(
                 "JSON-RPC Error "+errorObject.get("code")+": "+errorObject.get("message"));
         }
 
         // convert it to a return object
        if (jsonObject.has("result")) {
             return mapper.readValue(
             	jsonObject.get("result"), TypeFactory.type(returnType));
         }
 
         // no return type
         return null;
     }
 
     /**
      * Writes a request to the server.
      *
      * @param methodName the method to invoke
      * @param arguments  the method arguments
      * @param ops        the {@link OutputStream}
      * @param id         the id
      * @throws IOException
      * @throws JsonGenerationException
      * @throws JsonMappingException
      */
     public void writeRequest(
         String methodName, Object[] arguments, OutputStream ops, String id)
         throws IOException,
         JsonGenerationException,
         JsonMappingException {
 
         // create the request
         ObjectNode request = mapper.createObjectNode();
         request.put("id", id);
         request.put("jsonrpc", JSON_RPC_VERSION);
         request.put("method", methodName);
         request.put("params", mapper.valueToTree(arguments));
 
         // post the json data;
         mapper.writeValue(ops, request);
     }
 
 
 }

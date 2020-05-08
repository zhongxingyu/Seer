 package com.wixpress.fjarr.json;
 
 import com.fasterxml.jackson.core.JsonParseException;
 import com.fasterxml.jackson.databind.*;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.fasterxml.jackson.databind.node.ValueNode;
 import com.wixpress.fjarr.server.*;
 import com.wixpress.fjarr.server.exceptions.BadRequestException;
 import com.wixpress.fjarr.server.exceptions.HttpMethodNotAllowedException;
 import com.wixpress.fjarr.server.exceptions.MethodNotFoundException;
 import com.wixpress.fjarr.server.exceptions.UnsupportedContentTypeException;
 import com.wixpress.fjarr.util.IOUtils;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.reflect.Method;
 import java.lang.reflect.Type;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * @author alexeyr
  * @author DanielS
  * @since 6/2/11 10:38 AM
  */
 public class JsonRpcProtocol implements RpcProtocol
 {
     public static final String CONTENT_TYPE = "application/json-rpc";
     public static final String CHARSET_UTF8 = "charset=UTF-8";
     public static final String RESPONSE_CONTENT_TYPE = CONTENT_TYPE + "; " + CHARSET_UTF8;
 
     protected static final String JSON_RPC_VERSION = "2.0";
     protected static final String NOTIFICATION = "notification";
     protected static final String ID = "id";
 
     //    ObjectReader objectReader;
     ObjectMapper mapper;
     //    ParameterNameDiscoverer parameterNameDiscoverer = new AnnotationParameterNameDiscoverer();
     public static final List<String> allowedMethods = Arrays.asList("POST");
 
     public JsonRpcProtocol(ObjectMapper mapper)
     {
         //this.objectReader = mapper.reader().without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
         this.mapper = mapper;
 
         //        configureObjectMapper(this.mapper);
 
         //        StdTypeResolverBuilder r = new StdTypeResolverBuilder();
         //        r.init(JsonTypeInfo.Id.MINIMAL_CLASS, null);
         //        r.inclusion(JsonTypeInfo.As.PROPERTY);
         //        mapper.setDefaultTyping(r);
         //        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT, JsonTypeInfo.As.PROPERTY);
 
     }
 
     public JsonRpcProtocol()
     {
         ObjectMapper m = new ObjectMapper();
         m.registerModule(new FjarrJacksonModule());
 //        this.objectReader = m.reader().without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
         this.mapper = m;
 
     }
 
 
     public ParsedRpcRequest parseRequest(RpcRequest request) throws IOException, HttpMethodNotAllowedException, UnsupportedContentTypeException, BadRequestException
     {
         InputStream is = null;
         try
         {
             //application/json-rpc; charset=UTF-8
             if (request.getContentType() == null || !request.getContentType().split(";")[0].equalsIgnoreCase(CONTENT_TYPE))
                 throw new UnsupportedContentTypeException(CONTENT_TYPE);
 
             if (!allowedMethods.contains(request.getHttpMethod()))
                 throw new HttpMethodNotAllowedException(allowedMethods);
 
 
             return parseRequestJson(request, mapper.readValue(request.getRawRequestBody(), JsonNode.class));
         }
         catch (JsonParseException e)
         {
             throw new BadRequestException("JSON-RPC request payload parse error", e);
         }
     }
 
     public void resolveMethod(List<Method> methods, RpcInvocation invocation, ParsedRpcRequest request)
     {
         // TODO - cache resolved method and parameter types
 
         RpcParameters<?> parameters = invocation.getParameters();
         if (parameters instanceof PositionalRpcParameters)
             resolveMethodPositional(methods, invocation, request);
 //        else if (parameters instanceof NamedRpcParameters)
 //            resolveMethodNamed(methods, invocation, request);
         else if (parameters instanceof ObjectRpcParameters)
             resolveMethodObject(methods, invocation, request);
     }
 
     protected void resolveMethodObject(List<Method> methods, RpcInvocation invocation, ParsedRpcRequest request)
     {
         if (((JsonNode) ((ObjectRpcParameters) invocation.getParameters()).getParameters()).isValueNode())
             resolveMethodValueNode(methods, invocation, request);
         else
             resolveMethodObjectNode(methods, invocation, request);
     }
 
     protected void resolveMethodObjectNode(List<Method> methods, RpcInvocation invocation, ParsedRpcRequest request)
     {
         try
         {
             ObjectNode param = (ObjectNode) ((ObjectRpcParameters) invocation.getParameters()).getParameters();
 
             for (Method method : methods)
             {
 
                 Type[] parameterTypes = method.getGenericParameterTypes();
                 if (parameterTypes.length == 1)
                 {
                     try
                     {
                         final ObjectReader reader = getReader();
                         Object value = (param.get("@class") != null) ?
                                 reader.readValue(reader.treeAsTokens(param), Object.class) :
                                 reader.readValue(reader.treeAsTokens(param), mapper.getTypeFactory().constructType(parameterTypes[0]));
 
 
                         invocation.setResolvedMethod(method);
                         invocation.setResolvedParameters(new Object[]{value});
                         return;
                     }
                     catch (Exception e)
                     {
                         // ignore
                     }
                 }
             }
         }
         catch (Exception e)
         {
            invocation.setError(new MethodNotFoundException("Error at resolving of method [%s] \n %s\ncaused by %s", e, invocation.getMethodName(), request.getBaseRequest().getRawRequestBody(), e.getMessage()));
             return;
         }
         invocation.setError(new MethodNotFoundException("Method [%s] was not found \n %s", invocation.getMethodName(), request.getBaseRequest().getRawRequestBody()));
     }
 
     protected void resolveMethodValueNode(List<Method> methods, RpcInvocation invocation, ParsedRpcRequest request)
     {
         try
         {
             ValueNode param = (ValueNode) ((ObjectRpcParameters) invocation.getParameters()).getParameters();
 
             for (Method method : methods)
             {
                 Type[] parameterTypes = method.getGenericParameterTypes();
                 if (parameterTypes.length == 1)
                 {
                     try
                     {
                         final ObjectReader reader = getReader();
                         Object value = reader.readValue(reader.treeAsTokens(param), mapper.getTypeFactory().constructType(parameterTypes[0]));
 
 
                         invocation.setResolvedMethod(method);
                         invocation.setResolvedParameters(new Object[]{value});
                         return;
                     }
                     catch (Exception e)
                     {
                         // ignore
                     }
                 }
             }
         }
         catch (Exception e)
         {
            invocation.setError(new MethodNotFoundException("Error at resolving of method [%s] \n %s\ncaused by %s", e, invocation.getMethodName(), request.getBaseRequest().getRawRequestBody(), e.getMessage()));
             return;
         }
         invocation.setError(new MethodNotFoundException("Method [%s] was not found \n %s", invocation.getMethodName(), request.getBaseRequest().getRawRequestBody()));
     }
 
 
 //    protected void resolveMethodNamed(List<Method> methods, RpcInvocation invocation, ParsedRpcRequest request)
 //    {
 //        try
 //        {
 //            Map<String, Object> params = ((NamedRpcParameters) invocation.getParameters()).getParameters();
 //            for (Method method : methods)
 //            {
 //
 //                List<JsonNode> nonResolvedParams = new ArrayList<JsonNode>(params.size());
 //                for (String paramName : parameterNameDiscoverer.getParameterNames(method))
 //                {
 //                    if (paramName == null)
 //                    {
 //                        nonResolvedParams = null;
 //                        break;
 //                    }
 //
 //                    if (params.containsKey(paramName))
 //                    {
 //                        nonResolvedParams.add((JsonNode) params.get(paramName));
 //                    }
 //                }
 //                // this resolvedMethod is the correct one
 //                if (nonResolvedParams != null)
 //                {
 //                    Object[] convertedParams = new Object[nonResolvedParams.size()];
 //                    Type[] parameterTypes = method.getGenericParameterTypes();
 //
 //                    // convert the parameters
 //                    for (int i = 0; i < parameterTypes.length; i++)
 //                    {
 //                        if (nonResolvedParams.get(i).get("@class") != null)
 //                            convertedParams[i] = objectReader.readValue(objectReader.treeAsTokens(nonResolvedParams.get(i)), Object.class);
 //                        else
 //                            convertedParams[i] = objectReader.readValue(objectReader.treeAsTokens(nonResolvedParams.get(i)), mapper.getTypeFactory().constructType(parameterTypes[i]));
 //                    }
 //                    invocation.setResolvedMethod(method);
 //                    invocation.setResolvedParameters(convertedParams);
 //                    return;
 //                }
 //            }
 //        }
 //        catch (IOException e)
 //        {
 //            invocation.setError(new MethodNotFoundException("Error at resolving of method [%s] \n %s", e, invocation.getMethodName(), request.getBaseRequest().getRawRequestBody()));
 //            return;
 //        }
 //        invocation.setError(new MethodNotFoundException("Method [%s] was not found \n %s", invocation.getMethodName(), request.getBaseRequest().getRawRequestBody()));
 //    }
 
     protected void resolveMethodPositional(List<Method> methods, RpcInvocation invocation, ParsedRpcRequest request)
     {
         JsonNode[] parameters = (JsonNode[]) ((PositionalRpcParameters) invocation.getParameters()).getParameters();
         try
         {
             for (Method method : methods)
             {
                 if (method.getGenericParameterTypes().length == 1 &&
                         ((method.getParameterTypes()[0]).isArray() || (Collection.class.isAssignableFrom(method.getParameterTypes()[0]))))
                 {
 
                     ArrayNode param = mapper.createArrayNode();
                     for (JsonNode node : parameters)
                     {
                         param.add(node);
                     }
                     final ObjectReader reader = getReader();
                     Object value = (param.get("@class") != null) ?
                             reader.readValue(reader.treeAsTokens(param), Object.class) :
                             reader.readValue(reader.treeAsTokens(param), mapper.getTypeFactory().constructType(method.getGenericParameterTypes()[0]));
 
 
                     invocation.setResolvedMethod(method);
                     invocation.setResolvedParameters(new Object[]{value});
                     return;
                 }
 
                 if (parameters.length == method.getParameterTypes().length)
                 {
 
 
                     Object[] convertedParams = new Object[parameters.length];
                     Type[] parameterTypes = method.getGenericParameterTypes();
 
                     // convert the parameters
                     for (int i = 0; i < parameterTypes.length; i++)
                     {
                         // if the client have sent a type info as a @class property - try to use it
                         JsonNode parameter = (JsonNode) parameters[i];
                         final ObjectReader reader = getReader();
                         if (parameter.get("@class") != null)
                             convertedParams[i] = reader.readValue(reader.treeAsTokens(parameter), Object.class);
                         else
                             convertedParams[i] = reader.readValue(reader.treeAsTokens(parameter), mapper.getTypeFactory().constructType(parameterTypes[i]));
                     }
                     invocation.setResolvedMethod(method);
                     invocation.setResolvedParameters(convertedParams);
                     return;
                 }
             }
         }
         catch (IOException e)
         {
            invocation.setError(new MethodNotFoundException("Error at resolving of method [%s] \n %s\ncaused by %s", e, invocation.getMethodName(), request.getBaseRequest().getRawRequestBody(), e.getMessage()));
             return;
         }
         invocation.setError(new MethodNotFoundException("Method [%s] was not found \n %s", invocation.getMethodName(), request.getBaseRequest().getRawRequestBody()));
     }
 
 
     public void writeResponse(RpcResponse response, ParsedRpcRequest request) throws IOException
     {
 
         JsonNode resp = createResponseObject(request);
         response.setContentType(RESPONSE_CONTENT_TYPE);
 
         ObjectWriter writer = request.getBaseRequest().getQueryParameter("prettyPrint") != null
                 ? mapper.writerWithDefaultPrettyPrinter()
                 : mapper.writer();
 
         OutputStream os = null;
         try
         {
             os = response.getOutputStream();
             writer.writeValue(os, resp);
         }
         finally
         {
             IOUtils.close(os);
         }
     }
 
     protected JsonNode createResponseObject(ParsedRpcRequest request)
     {
         JsonNode resp = null;
         if (request.getInvocations().size() > 1)
         {
             resp = mapper.createArrayNode();
             for (RpcInvocation invocation : request.getInvocations())
             {
                 if (invocation.isError() || invocation.getValueFromContext(NOTIFICATION).equals(false))
                     ((ArrayNode) resp).add(createSingleResponse(invocation));
             }
         }
         else if (request.getInvocations().size() == 1)
             resp = createSingleResponse(request.getInvocations().get(0));
 
         return resp;
     }
 
     protected ObjectNode createSingleResponse(RpcInvocation invocation)
     {
         ObjectNode resp = mapper.createObjectNode();
         resp.put("jsonrpc", JSON_RPC_VERSION);
         resp.put(ID, (JsonNode) invocation.getValueFromContext(ID, null));
 
         if (invocation.isError())
         {
             resp.put("error", createErrorResponse(invocation));
         }
         else
         {
             resp.put("result", (invocation.getResolvedMethod().getGenericReturnType() != null) ? mapper.valueToTree(invocation.getResult()) : null);
         }
         return resp;
     }
 
     protected ObjectNode createErrorResponse(RpcInvocation invocation)
     {
         Exception e = invocation.getError();
         ObjectNode error = mapper.createObjectNode();
         error.put("code", calculateErrorCode(invocation));
         error.put("message", getFullErrorMessage(e));
         Class<?>[] checkedExceptionTypes = invocation.getResolvedMethod() != null ? invocation.getResolvedMethod().getExceptionTypes() : null;
 
 
         error.put("data", mapper.valueToTree(e));
 
 //        boolean isCheckedException = false;
 //        if (checkedExceptionTypes != null)
 //        {
 //            for (int i = 0; i < checkedExceptionTypes.length; i++)
 //            {
 //                if (checkedExceptionTypes[i].equals(e.getClass()))
 //                {
 //                    error.put("data", mapper.valueToTree(e));
 //                    isCheckedException = true;
 //                    break;
 //                }
 //            }
 //        }
 //        if (!isCheckedException)
 //        {
 //            ObjectNode data = mapper.createObjectNode();
 //            data.put("@class", e.getClass().getCanonicalName());
 //            data.put("message", getFullErrorMessage(e));
 //            error.put("data", data);
 //        }
 
         return error;
     }
 
     private String getFullErrorMessage(Throwable e)
     {
         StringBuilder sb = new StringBuilder();
         sb.append(e.getMessage());
         while (e.getCause() != null && e.getCause() != e)
         {
             sb.append("\n  caused by ").append(e.getCause().getClass().getName()).append(", with message: ").append(e.getCause().getMessage());
             e = e.getCause();
         }
         return sb.toString();
     }
 
     protected int calculateErrorCode(RpcInvocation invocation)
     {
         if (invocation.getErrorCode() != null && !ErrorCodes.clashesWithJsonError(invocation.getErrorCode()))
             return invocation.getErrorCode();
         Class<? extends Exception> clazz = invocation.getError().getClass();
 
         if (clazz.equals(InvalidJsonRpcRequest.class))
             return ErrorCodes.INVALID_REQUEST;
         if (clazz.equals(MethodNotFoundException.class))
             return ErrorCodes.METHOD_NOT_FOUND;
 
         return ErrorCodes.INTERNAL_ERROR;
     }
 
 
     protected ParsedRpcRequest parseRequestJson(RpcRequest rpcRequest, JsonNode request) throws BadRequestException
     {
         // process single request
         if (request.isObject())
             return ParsedRpcRequest.from(rpcRequest, parseSingleRequest((ObjectNode) request));
         else if (request.isArray())// process batch request
             return ParsedRpcRequest.from(rpcRequest, processBatchRequest((ArrayNode) request));
         else
             throw new BadRequestException("Unsupported request object type " + request.getClass().getName());
     }
 
     protected RpcInvocation[] processBatchRequest(ArrayNode requests)
     {
         if (requests.size() == 0)
             return new RpcInvocation[]{
                     new RpcInvocation(new InvalidJsonRpcRequest("Request is an empty array "))
             };
 
         RpcInvocation invocations[] = new RpcInvocation[requests.size()];
 
         int i = 0;
         for (JsonNode request : requests)
         {
             if (!request.isObject())
                 invocations[i] = new RpcInvocation(new InvalidJsonRpcRequest("Unsupported request object type " + request.getClass().getName()));
             else
                 invocations[i] = parseSingleRequest((ObjectNode) request);
             i++;
         }
         return invocations;
     }
 
     protected RpcInvocation parseSingleRequest(ObjectNode request)
     {
         // validate request
         if (!request.has("jsonrpc"))
             return new RpcInvocation(new InvalidJsonRpcRequest("Missing jsonrpc param"));
         if (!request.has("method"))
             return new RpcInvocation(new InvalidJsonRpcRequest("Missing resolvedMethod param"));
         if (!request.get("method").isTextual())
             return new RpcInvocation(new InvalidJsonRpcRequest("Method name must be a string"));
 
         // parse request
         String version = request.get("jsonrpc").asText();
         String methodName = request.get("method").asText();
         JsonNode id = null;
         boolean isNotification = false;
         if (request.has(ID))
             id = request.get(ID);
         else
             isNotification = true;
         JsonNode params = request.get("params");
 
         // more validations
         if (!version.equals(JSON_RPC_VERSION))
             return new RpcInvocation(new InvalidJsonRpcRequest("Missing resolvedMethod param"));
 
         RpcParameters<?> parameters = buildParameters(params);
         if (parameters == null)
             return new RpcInvocation(new InvalidJsonRpcRequest("Failed parsing resolvedMethod params"));
 
         return new RpcInvocation(methodName, parameters)
                 .withContextValue(ID, id)
                 .withContextValue(NOTIFICATION, isNotification);
     }
 
     protected RpcParameters<?> buildParameters(JsonNode params)
     {
         // handle param arrays, or no params
         if (params.isValueNode() || params.isObject())
         {
             // handle named params
 //            Map<String, Object> ret = new HashMap<String, Object>(params.size());
 //            Iterator<String> fieldNames = params.fieldNames();
 //            while (fieldNames.hasNext())
 //            {
 //                String fieldName = fieldNames.next();
 //                ret.put(fieldName, params.get(fieldName));
 //            }
 //            return new NamedRpcParameters(ret);
 
             //handle ObjectParams
 
             return new ObjectRpcParameters(params);
 
         }
         else if (params.size() == 0 || params.isArray())
         {
             JsonNode[] ret = new JsonNode[params.size()];
             for (int i = 0; i < params.size(); i++)
             {
                 ret[i] = params.get(i);
             }
 
             return new PositionalRpcParameters(ret);
         }
         return null;
     }
 
     static class InvalidJsonRpcRequest extends Exception
     {
         protected InvalidJsonRpcRequest(String validation)
         {
             super(validation);
         }
     }
 
     protected static class ErrorCodes
     {
         public static final int PARSE_ERROR = -32700;      // Parse error Invalid JSON was received by the server.
         // An error occurred on the server while parsing the JSON text.
         public static final int INVALID_REQUEST = -32600;   // The JSON sent is not a valid Request object.
         public static final int METHOD_NOT_FOUND = -32601;  // The resolvedMethod does not exist / is not available.
         public static final int IVALID_PARAMS = -32602;     // Invalid resolvedMethod parameter(s).
         public static final int INTERNAL_ERROR = -32603;    // Internal JSON-RPC error.
 
         public static boolean clashesWithJsonError(int errorCode)
         {
             return errorCode >= 32600 && errorCode <= 32700;
         }
     }
 
     public void setMapper(ObjectMapper mapper)
     {
         this.mapper = mapper;
     }
 
     private ObjectReader getReader()
     {
         return mapper.reader().without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
     }
 }

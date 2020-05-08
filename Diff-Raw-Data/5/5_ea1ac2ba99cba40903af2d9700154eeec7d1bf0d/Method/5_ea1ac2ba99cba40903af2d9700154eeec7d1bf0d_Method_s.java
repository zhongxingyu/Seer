 /*
  */
 package net.linkfluence.jspore;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Strings;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 import com.ning.http.client.RequestBuilder;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.jboss.netty.handler.codec.http.HttpResponseStatus;
 
 /**
  * Describe a route specification.
  * 
  * @author Nicolas Yzet <nyzet@linkfluence.net>
  */
 public class Method {
 
     private final static String VAR_PREFIX = ":";
     private final static Pattern VAR_PATTERN = Pattern.compile("(" + VAR_PREFIX + "\\w+)");
     private final String pathFormat;
     
     public final String name;
     public final String path;
     public final String httpMethod;
     
     public final ImmutableSet<Integer> expectedStatus;
     public final ImmutableSet<String> requiredParams;
     public final ImmutableSet<String> optionalParams;
     public final ImmutableMap<String, String> headers;
     public final ImmutableList<String> pathVarNames;
     public final boolean authentication;
 
     protected Method(String name, String path, String httpMethod,
             Iterable<Integer> expectedStatus,
             Iterable<String> requiredParams,
             Iterable<String> optionalParams,
             Map<String, String> headers,
             boolean authentication) {
         this.authentication = authentication;
         this.name = name;
         this.path = path;
         this.httpMethod = httpMethod.toUpperCase();
         this.expectedStatus = new ImmutableSet.Builder<Integer>()
                 .addAll(expectedStatus)
                 .build();
         this.requiredParams = new ImmutableSet.Builder<String>()
                 .addAll(requiredParams)
                 .build();
         this.optionalParams = new ImmutableSet.Builder<String>()
                 .addAll(optionalParams)
                 .build();
         this.headers = new ImmutableMap.Builder<String, String>()
                 .putAll(headers)
                 .build();
         Matcher variableMatcher = VAR_PATTERN.matcher(path);
         List<String> varNames = new ArrayList<String>();
         int fromIndex = 0;
         String expr = path;
         while (variableMatcher.find()) {
             String varName = variableMatcher.group(1);
             int varIdx = expr.indexOf(varName);
             expr = expr.substring(0, varIdx) + "%s" + expr.substring(varIdx + varName.length());
             varNames.add(varName.substring(1));
         }
         this.pathFormat = expr;
         this.pathVarNames = new ImmutableList.Builder<String>()
                 .addAll(varNames)
                 .build();
     }
 
     private RequestBuilder buildRequestImpl(String baseUrl, Map<String, String> params) throws SporeException {
         RequestBuilder builder = new RequestBuilder();
         builder.setMethod(this.httpMethod);
         Set<String> checkRequired = new HashSet<String>(this.requiredParams);
 
         // fill path parameters
         Object[] pathParams = new String[pathVarNames.size()];
         Arrays.fill(pathParams, "");
         for (int i = 0; i < pathVarNames.size(); i++) {
             String val = params.get(pathVarNames.get(i));
             if (val != null) {
                 pathParams[i] = val;
                 checkRequired.remove(pathVarNames.get(i));
             }
         }
         String url;
         String reqPath = String.format(this.pathFormat, pathParams);
         if (baseUrl.endsWith("/") && reqPath.startsWith("/")) {
             url = baseUrl + reqPath.substring(1);
         } else {
             url = baseUrl + reqPath;
         }
         builder.setUrl(url);
 
         // add query parameter
         for (Entry<String, String> e : params.entrySet()) {
             builder.addParameter(e.getKey(), e.getValue());
             checkRequired.remove(e.getKey());
         }
 
         if (!checkRequired.isEmpty()) {
             Joiner joiner = Joiner.on(',');
             throw new SporeException("Missing required parameters: "
                     + joiner.join(checkRequired));
         }
         return builder;
     }
 
     public RequestBuilder buildRequest(String baseUrl, Map<String, String> params, String payload) throws SporeException {
         RequestBuilder builder = buildRequestImpl(baseUrl, params);
         if (!Strings.isNullOrEmpty(payload)) {
             builder.setBody(payload);
         }
         return builder;
     }
 
     public RequestBuilder buildRequest(String baseUrl, Map<String, String> params, Object payload) throws SporeException {
         RequestBuilder builder = buildRequestImpl(baseUrl, params);
         if (payload != null) {
             if (!Strings.isNullOrEmpty(payload.toString())) {
                 builder.setBody(payload.toString());
             }
         }
         return builder;
     }
 
     public RequestBuilder buildRequest(String baseUrl, Map<String, String> params, byte[] payload) throws SporeException {
         RequestBuilder builder = buildRequestImpl(baseUrl, params);
         if (payload != null) {
             builder.setBody(payload);
         }
         return builder;
     }
     public static class Builder {
 
         public final String name;
         public final String path;
         public final String httpMethod;
         private final Set<Integer> expectedStatus;
         private final Set<String> requiredParams;
         private final Set<String> optionalParams;
         private final Map<String, String> headers;
         
         private boolean authentication = false;
                 
         public Builder(String name, String path, String httpMethod) {
             Preconditions.checkArgument(!Strings.isNullOrEmpty(name));
             Preconditions.checkArgument(!Strings.isNullOrEmpty(path));
             Preconditions.checkArgument(!Strings.isNullOrEmpty(httpMethod));
             this.name = name;
             this.path = path;
             this.httpMethod = httpMethod.toUpperCase();
             this.expectedStatus = new HashSet<Integer>();
             this.optionalParams = new HashSet<String>();
             this.requiredParams = new HashSet<String>();
             this.headers = new HashMap<String, String>();
         }
 
        public Builder addExepectedStatus(int status) {
             HttpResponseStatus s = HttpResponseStatus.valueOf(status);
             expectedStatus.add(s.getCode());
             return this;
         }
 
         public Builder addOptionalParam(String param) {
             optionalParams.add(param);
             return this;
         }
 
         public Builder addRequiredParam(String param) {
             requiredParams.add(param);
             return this;
         }
 
         public Builder addRequiredParams(Collection<String> params) {
             for (String param : params) {
                 addRequiredParam(param);
             }
             return this;
         }
 
         public Builder addOptionalParams(Collection<String> params) {
             for (String param : params) {
                 addOptionalParam(param);
             }
             return this;
         }
 
         public Builder addExpectedStatuses(Collection<Integer> statuses) {
             for (int status : statuses) {
                addExepectedStatus(status);
             }
             return this;
         }
 
         /**
          * if key exists replace with new value
          * @param key
          * @param value 
          */
         public Builder addHeader(String key, String value) {
             this.headers.put(key, value);
             return this;
         }
 
         /**
          * If key already exist, replace with new value.
          * @param headers 
          */
         public Builder addHeaders(Map<String, String> headers) {
             this.headers.putAll(headers);
             return this;
         }
         
         public Builder setAuthentication(boolean auth) {
             this.authentication = auth;
             return this;
         }
         
         public Method build(){
             return new Method(this.name, this.path, this.httpMethod,
                     this.expectedStatus, this.requiredParams, this.optionalParams,
                     this.headers, this.authentication);
         }
     }
 
     
 }

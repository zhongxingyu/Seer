 /*
  *  Copyright 2011 mathieuancelin.
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 package cx.ath.mancel01.webframework.routing;
 
 import cx.ath.mancel01.webframework.http.Request;
 import java.lang.annotation.Annotation;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.QueryParam;
 
 /**
  *
  * @author mathieuancelin
  */
 public class Param {
 
     public enum ParamType {
 
         PATH, QUERY, FORM, BODY
     }
     public static final Pattern PATH_PARAM_DECLARATION = Pattern.compile("\\{[a-zA-Zàáâãäåçèéêëìíîïðòóôõöùúûüýÿ0-9]+\\}");
     private final String name;
     private String prefix;
     private String suffix;
     private final String urlRoute;
     private final ParamType paramType;
     private final Class<?> type;
     private TypeMapper mapper;
 
     public Param(Annotation annotation, String urlRoute, Class<?> type) {
         if (annotation instanceof PathParam) {
             this.name = ((PathParam) annotation).value();
             this.paramType = ParamType.PATH;
         } else if (annotation instanceof QueryParam) {
             this.name = ((QueryParam) annotation).value();
             this.paramType = ParamType.QUERY;
         } else if (annotation instanceof FormParam) {
             this.name = ((FormParam) annotation).value();
             this.paramType = ParamType.FORM;
         } else {
             this.name = ""; // TODO : remove that
             this.paramType = ParamType.QUERY;
         }
         this.urlRoute = urlRoute;
         this.type = type;
     }
 
     public Param(Class<?> type, String urlRoute, TypeMapper mapper) {
         this.name = "param";
         this.urlRoute = urlRoute;
         this.paramType = ParamType.BODY;
         this.type = type;
         this.mapper = mapper;
     }
 
     public void setPathParamName(Matcher matcher) {
         //this.name = matcher.group().replaceAll("\\{|\\}", "");
         prefix = urlRoute.substring(0, matcher.start());
         suffix = urlRoute.substring(matcher.end());
         prefix = replaceParamsWithWildcard(prefix);
         suffix = replaceParamsWithWildcard(suffix);
     }
 
     public Object value(Request req) {
         Object ret = null;
         if (this.paramType.equals(ParamType.PATH)) {
             // TODO : check here simple types only 
             if (matchesRoute(req.getPath(), urlRoute)) {
                 ret = req.getPath().replaceAll(prefix, "").replaceAll(suffix, "");
                 ret = URLDecoder.decode((String) ret);
             } else {
                 throw new RuntimeException("url " + req.getPath() + " doesn't match route url pattern" + urlRoute);
             }
         } else if (this.paramType.equals(ParamType.QUERY)) {
             // TODO : check here simple types only
             ret = getQueryParam(this.name, "?" + req.querystring);
             ret = URLDecoder.decode((String) ret);
         } else if (this.paramType.equals(ParamType.FORM)) {
             // TODO : check here simple types only
             ret = getQueryParam(this.name, "?" + req.body());
             ret = URLDecoder.decode((String) ret);
         } else if (this.paramType.equals(ParamType.BODY)) {
             ret = mapper.map(req.body()); // Map with type according to kind of value (json, xml, txt, bytes)
            return ret;
             // not with mappropertyvalue
         }
         return mapProperlyValue((String) ret, this.type);
     }
 
     private Object mapProperlyValue(String value, Class<?> type) {
         // TODO : map simple types (string, integer, etc ...) correctly
         if (type.equals(Byte.TYPE)) {
             if (value != null) {
                 return Byte.valueOf(value);
             } else {
                 return Byte.MIN_VALUE;
             }
         }
         if (type.equals(Short.TYPE)) {
             if (value != null) {
                 return Short.valueOf(value);
             } else {
                 return Short.MIN_VALUE;
             }
         }
         if (type.equals(Integer.TYPE)) {
             if (value != null) {
                 return Integer.valueOf(value);
             } else {
                 return Integer.MIN_VALUE;
             }
         }
         if (type.equals(Long.TYPE)) {
             if (value != null) {
                 return Long.valueOf(value);
             } else {
                 return Long.MIN_VALUE;
             }
         }
         if (type.equals(Float.TYPE)) {
             if (value != null) {
                 return Float.valueOf(value);
             } else {
                 return Float.MIN_VALUE;
             }
         }
         if (type.equals(Double.TYPE)) {
             if (value != null) {
                 return Double.valueOf(value);
             } else {
                 return Double.MIN_VALUE;
             }
         }
         if (type.equals(Boolean.TYPE)) {
             if (value != null) {
                 return Boolean.valueOf(value);
             } else {
                 return false;
             }
         }
         if (type.equals(Byte.class)) {
             return Byte.valueOf(value);
         }
         if (type.equals(Short.class)) {
             return Short.valueOf(value);
         }
         if (type.equals(Integer.class)) {
             return Integer.valueOf(value);
         }
         if (type.equals(Long.class)) {
             return Long.valueOf(value);
         }
         if (type.equals(Float.class)) {
             return Float.valueOf(value);
         }
         if (type.equals(Double.class)) {
             return Double.valueOf(value);
         }
         if (type.equals(Boolean.class)) {
             return Boolean.valueOf(value);
         }
         return value;
     }
 
     private String getQueryParam(String name, String req) {
         List<String> values = Param.getQueryValues(name, req);
         if (!values.isEmpty()) {
             return values.iterator().next();
         }
         return "";
     }
 
     public String name() {
         return name;
     }
 
     public Class<?> type() {
         return type;
     }
 
     public static List<String> getQueryValues(String name, String url) {
         List<String> values = new ArrayList<String>();
         Pattern pattern = Pattern.compile("[\\?&]" + name + "=([^&#]*)");
         Matcher matcher = pattern.matcher(url);
         while (matcher.find()) {
             values.add(matcher.group().replaceAll("[\\?&]" + name + "=", ""));
         }
         return values;
     }
 
     public static boolean matchesRoute(String url, String route) {
         return url.matches(Param.replaceParamsWithWildcard(route));
     }
 
     public static String replaceParamsWithWildcard(String value) {
         return value.replaceAll("\\{[a-zA-Zàáâãäåçèéêëìíîïðòóôõöùúûüýÿ0-9]+\\}", "[a-zàáâãäåçèéêëìíîïðòóôõöùúûüýÿA-Z0-9]+");
     }
 
     public interface TypeMapper {
         Object map(String value);
     }
 }

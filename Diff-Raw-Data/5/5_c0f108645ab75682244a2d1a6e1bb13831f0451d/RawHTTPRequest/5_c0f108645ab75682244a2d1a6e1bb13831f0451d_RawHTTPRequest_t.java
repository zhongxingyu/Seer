 package prj.httpparser.httpparser;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class RawHTTPRequest
 {
     private RequestType requestType;
     private String resourceAddress;
     private String httpVersion;
     private Map<String, String> headers;
     private Map<String, String> GETParams;
     private String body;
     private static String SP = " ";
     private static String CRLF = "\r\n";
     private static final Object COLON = ":";
 
     public RawHTTPRequest()
     {
         headers = new HashMap<>();
         GETParams = new HashMap<>();
     }
 
     public RequestType getRequestType()
     {
         return requestType;
     }
 
     public void setRequestType(RequestType requestType)
     {
         this.requestType = requestType;
     }
 
     public String getResourceAddress()
     {
         return resourceAddress;
     }
 
     public void setResourceAddress(String resourceAddress)
     {
         this.resourceAddress = resourceAddress;
     }
 
     public String getHttpVersion()
     {
         return httpVersion;
     }
 
     public void setHttpVersion(String httpVersion)
     {
         this.httpVersion = httpVersion;
     }
 
     public Map<String, String> getHeaders()
     {
         return headers;
     }
 
     public void addHeader(String field, String value)
     {
         headers.put(field, value);
     }
 
     public Map<String, String> getGETParams()
     {
         return GETParams;
     }
 
     public void setGETParams(String paramString)
     {
         String[] params = paramString.split("&");
         for (String paramElement : params)
         {
            String[] keyValue = paramElement.split("=");
            if (keyValue.length == 2)
             {
                 GETParams.put(keyValue[0], keyValue[1]);
             }
         }
     }
 
     public String getBody()
     {
         return body;
     }
 
     public void setBody(String body)
     {
         this.body = body;
     }
 
     @Override
     public String toString()
     {
         StringBuffer sb = new StringBuffer();
         sb.append(requestType.toString()).append(SP).append(resourceAddress).append(SP).append(httpVersion).append(CRLF);
         for (String headerField : headers.keySet())
         {
             sb.append(headerField).append(COLON).append(SP).append(headers.get(headerField)).append(CRLF);
         }
         sb.append(CRLF);
         if (body != null)
         {
             sb.append(body);
         }
         return sb.toString();
     }
 }

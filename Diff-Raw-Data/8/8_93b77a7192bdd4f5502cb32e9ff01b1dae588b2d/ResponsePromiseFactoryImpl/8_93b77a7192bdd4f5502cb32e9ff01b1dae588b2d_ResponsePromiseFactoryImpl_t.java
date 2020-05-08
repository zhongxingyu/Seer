 package util;
 
 import controllers.ws.WSWrapper;
 import domain.PageComponent;
 import org.apache.commons.lang3.StringUtils;
 import play.Logger;
 import play.libs.F;
 import play.libs.WS;
 import play.mvc.Http;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class ResponsePromiseFactoryImpl implements ResponsePromiseFactory {
 
     private static final String ORIGINATOR_URI_HEADER_NAME = "Originator-Uri";
     private static final String[] HEADER_NAMES = new String[]{"Content-Type", "Cookie"};
     private static final Pattern QUERY_STRING_BOUNDARY = Pattern.compile("\\?");
     private static final Pattern AMPERSAND_PATTERN = Pattern.compile("&");
     private static final Pattern EQUALS_PATTERN = Pattern.compile("=");
     private static final Pattern PARAMS_PATTERN = Pattern.compile("\\{params\\}");
     private static final String POST = "POST";
     private static final String HTTP_PREFIX = "http://";
 
     private WSWrapper wsWrapper;
 
     @Override
     public F.Promise<WS.Response> getResponsePromise(Http.Request request, PageComponent pageComponent) {
 
         final String rQueryString = findQueryString(request.uri());
 
         final String pageComponentUrl = substituteVars(pageComponent.getUrl(), rQueryString);
 
         Logger.debug("promising content from: " + pageComponentUrl);
 
         // it's irritating that setting the queryString as part of the URI passed to WS.url() doesn't work - the query string is not sent.
         // so have to parse params and then set them individually on WSRequestHolder
         // todo: find a better way
         final String[] urlParts = splitPathAndQuery(pageComponentUrl);
         final WS.WSRequestHolder urlHolder = wsWrapper.url(urlParts[0]).setFollowRedirects(false);
         if (urlParts.length == 2) {
             setQueryParameters(urlParts[1], urlHolder);
         }
 
         this.copyRequestHeaders(urlHolder, request);
         this.addCustomHeaders(urlHolder, request);
 
         return this.copyMethodAndBody(pageComponent, urlHolder, request);
     }
 
     private void setQueryParameters(String queryString, WS.WSRequestHolder urlHolder) {
         final String[] params = AMPERSAND_PATTERN.split(queryString);
         for (String param : params) {
             final String[] nameValue = EQUALS_PATTERN.split(param);
             String value;
             if (nameValue.length == 2) {
                 //decoding is necessary since Play will automatically encode params, resulting in double encoding
                 try {
                     value = URLDecoder.decode(nameValue[1], "UTF-8");
                 } catch (UnsupportedEncodingException e) {
                     throw new RuntimeException(e);
                 }
             } else {
                 value = "";
             }
             urlHolder.setQueryParameter(nameValue[0], value);
         }
     }
 
     private F.Promise<WS.Response> copyMethodAndBody(PageComponent pageComponent, WS.WSRequestHolder urlHolder, Http.Request request) {
         F.Promise<WS.Response> componentPromise;
         if (request.method().equals(POST) && pageComponent.isAcceptPost()) {
             // another peculiarity: if data is formUrlEncoded then RequestBody.asText() doesn't get anything.
             // That's why have to get RequestBody.asFormUrlEncoded() and re-encode as text, since WSRequestHolder.post()
             // doesn't take a map.
            String postData = null;
            Map<String, String[]> body = request.body().asFormUrlEncoded();
            if (!(body == null || body.isEmpty())) {
                postData = getPostData(body);
            } else {
                postData = request.body().asText();
            }
             componentPromise = urlHolder.post(postData);
         } else {
             componentPromise = urlHolder.get();
         }
         return componentPromise;
     }
 
     private void copyRequestHeaders(WS.WSRequestHolder urlHolder, Http.Request request) {
         //todo: copying all headers causes problems, so just copying Content-Type and Cookie for now
         //this is might be to do with request header "Accept-Encoding" causing response to be compressed,
         //which isn't handled, breaking something else.
         Map<String, String[]> headers = request.headers();
         for (String key : HEADER_NAMES) {
             String[] values = headers.get(key);
             if (values != null) {
                 for (String value : values) {
                     urlHolder.setHeader(key, value);
                 }
             }
         }
     }
 
     private void addCustomHeaders(final WS.WSRequestHolder urlHolder, final Http.Request request){
         final String originatorUri = HTTP_PREFIX + request.host() + request.uri();
         urlHolder.setHeader(ORIGINATOR_URI_HEADER_NAME, originatorUri);
     }
 
     private String getPostData(Map<String, String[]> map) {
         final StringBuilder sb = new StringBuilder();
         int i = 0;
         for (String key : map.keySet()) {
             try {
                 sb.append(URLEncoder.encode(key, "UTF-8")).append("=").append(URLEncoder.encode(map.get(key)[0], "UTF-8"));
             } catch (UnsupportedEncodingException e) {
                 throw new RuntimeException(e);
             }
             if (map.size() - 1 > i++) {
                 sb.append("&");
             }
         }
         return sb.toString();
     }
 
     private String findQueryString(String url) {
         if (StringUtils.isNotEmpty(url)) {
             String[] split = splitPathAndQuery(url);
             if (split.length == 2) {
                 return split[1];
             }
         }
         return "";
     }
 
     private String[] splitPathAndQuery(String componentUrl) {
         return QUERY_STRING_BOUNDARY.split(componentUrl);
     }
 
     private String substituteVars(String componentUrl, String realQueryString) {
         final Matcher matcher = PARAMS_PATTERN.matcher(componentUrl);
         String rtnVal = matcher.replaceAll(realQueryString);
         return rtnVal;
     }
 
     public void setWsWrapper(WSWrapper wsWrapper) {
         this.wsWrapper = wsWrapper;
     }
 }

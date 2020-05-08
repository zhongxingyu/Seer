 package daniel.web.http.server.util;
 
 import daniel.data.option.Option;
 import daniel.data.sequence.ImmutableArray;
 import daniel.data.sequence.ImmutableSequence;
 import daniel.logging.Logger;
 import daniel.web.http.HttpRequest;
 import daniel.web.http.HttpResponse;
 import daniel.web.http.RequestMethod;
 import daniel.web.http.server.PartialHandler;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 
 public final class LineSeparatorRemovingHandler implements PartialHandler {
   private static final Logger logger = Logger.forClass(LineSeparatorRemovingHandler.class);
 
   public static final LineSeparatorRemovingHandler singleton = new LineSeparatorRemovingHandler();
 
   private static final ImmutableSequence<RequestMethod> methodToRedirect =
       ImmutableArray.create(RequestMethod.GET, RequestMethod.HEAD);
 
   private LineSeparatorRemovingHandler() {}
 
   @Override
   public Option<HttpResponse> tryHandle(HttpRequest request) {
     if (!methodToRedirect.contains(request.getMethod()))
       return Option.none();
 
    String strippedResource = stripLineSeparatorsFromPercentEncodedResource(request.getResource());
     if (strippedResource.equals(request.getResource()))
       return Option.none();
 
     String location = String.format("http://%s%s", request.getHost(), strippedResource);
     logger.info("Redirecting to %s.", location);
     return Option.some(HttpResponseFactory.permanentRedirect(location));
   }
 
  private static String stripLineSeparatorsFromPercentEncodedResource(String percentEncodedResource) {
     try {
      String decodedResource = URLDecoder.decode(percentEncodedResource, "UTF-8");
      String strippedDecodedResource = stripLineSeparators(decodedResource);
      return URLEncoder.encode(strippedDecodedResource, "UTF-8");
     } catch (UnsupportedEncodingException e) {
       logger.error("Eh? UTF-8 not supported?", e);
      return percentEncodedResource;
     }
   }
 
   private static String stripLineSeparators(String input) {
     StringBuilder sb = new StringBuilder();
     for (int offset = 0; offset < input.length();) {
       int codepoint = input.codePointAt(offset);
       int type = Character.getType(codepoint);
       if (type != Character.LINE_SEPARATOR && type != Character.PARAGRAPH_SEPARATOR)
         sb.append(Character.toChars(codepoint));
       offset += Character.charCount(codepoint);
     }
     return sb.toString();
   }
 }

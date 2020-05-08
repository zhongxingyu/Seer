 package org.apache.maven.shared.model;
 
 import org.kxml2.io.KXmlParser;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.*;
 
 /**
  * Provides methods for marshalling and unmarshalling XML that does not contain attributes.
  */
 public final class ModelMarshaller {
 
     public static List<ModelProperty> marshallXmlToModelProperties(InputStream inputStream, String baseUri,
                                                                    Set<String> collections)
             throws IOException {
         if (inputStream == null) {
             throw new IllegalArgumentException("inputStream: null");
         }
 
         if (baseUri == null) {
             throw new IllegalArgumentException("baseUri: null");
         }
 
         if (collections == null) {
             collections = Collections.EMPTY_SET;
         }
 
         List<ModelProperty> modelProperties = new ArrayList<ModelProperty>();
 
         KXmlParser parser = new KXmlParser();
         try {
             parser.setInput(inputStream, null);
         } catch (XmlPullParserException e) {
             try {
                 inputStream.close();
             } catch (IOException e1) {
 
             }
             throw new IOException(e.toString());
         }
 
         Uri uri = new Uri(baseUri);
         String tagName = baseUri;
         String tagValue = null;
 
         try {
             for (; ; parser.next()) {
                 int type = parser.getEventType();
                 switch (type) {
                     case XmlPullParser.TEXT: {
                         String tmp = parser.getText();
                         if (tmp != null && tmp.trim().length() != 0) {
                             tagValue = tmp;
                         }
                         break;
                     }
                     case XmlPullParser.START_TAG: {
                        if (parser.isEmptyElementTag()) {
                            tagValue = "";
                        }
 
                         if (!tagName.equals(baseUri)) {
                             modelProperties.add(new ModelProperty(tagName, tagValue));
                         }
 
                         tagName = uri.getUriFor(parser.getName(), parser.getDepth());
                         if (collections.contains(tagName + "#collection")) {
                             tagName = tagName + "#collection";
                             uri.addTag(parser.getName() + "#collection");
                         } else {
                             uri.addTag(parser.getName());
                         }
                         tagValue = null;
                         break;
                     }
                     case XmlPullParser.END_DOCUMENT: {
                         modelProperties.add(new ModelProperty(tagName, tagValue));
                         return modelProperties;
                     }
                 }
             }
         } catch (XmlPullParserException e) {
             throw new IOException(":" + e.toString());
         } finally {
             try {
                 inputStream.close();
             } catch (IOException e) {
 
             }
         }
     }
 
 
     public static String unmarshalModelPropertiesToXml(List<ModelProperty> modelProperties, String baseUri) throws IOException {
         if (modelProperties == null || modelProperties.isEmpty()) {
             throw new IllegalArgumentException("modelProperties: null or empty");
         }
 
         if (baseUri == null || baseUri.trim().length() == 0) {
             throw new IllegalArgumentException("baseUri: null or empty");
         }
 
         final int basePosition = baseUri.length();
 
         StringBuffer sb = new StringBuffer();
         List<String> lastUriTags = new ArrayList<String>();
         for (ModelProperty mp : modelProperties) {
             String uri = mp.getUri();
             if (!uri.startsWith(baseUri)) {
                 throw new IllegalArgumentException("Passed in model property that does not match baseUri: Property URI = "
                         + uri + ", Base URI = " + baseUri);
             }
             List<String> tagNames = getTagNamesFromUri(basePosition, uri);
             if (lastUriTags.size() > tagNames.size()) {
                 for (int i = lastUriTags.size() - 1; i >= tagNames.size(); i--) {
                     sb.append(toEndTag(lastUriTags.get(i - 1)));
                 }
             }
             String tag = tagNames.get(tagNames.size() - 1);
             sb.append(toStartTag(tag));
             if (mp.getResolvedValue() != null) {
                 sb.append(mp.getResolvedValue());
                 sb.append(toEndTag(tag));
             }
             lastUriTags = tagNames;
         }
        for (int i = lastUriTags.size() - 1; i >= 1; i--) {
             sb.append(toEndTag(lastUriTags.get(i)));
         }
         return sb.toString();
     }
 
     private static List<String> getTagNamesFromUri(int basePosition, String uri) {
         return Arrays.asList(uri.substring(basePosition).replace("#collection", "").split("/"));
     }
 
     private static String toStartTag(String value) {
         StringBuffer sb = new StringBuffer();
         sb.append("<").append(value).append(">\r\n");
         return sb.toString();
     }
 
     private static String toEndTag(String value) {
         if (value.trim().length() == 0) {
             return "";
         }
         StringBuffer sb = new StringBuffer();
         sb.append("\r\n</").append(value).append(">\r\n");
         return sb.toString();
     }
 
     private static class Uri {
 
         List<String> uris;
 
         Uri(String baseUri) {
             uris = new LinkedList<String>();
             uris.add(baseUri);
         }
 
         String getUriFor(String tag, int depth) {
             setUrisToDepth(depth);
             StringBuffer sb = new StringBuffer();
             for (String tagName : uris) {
                 sb.append(tagName).append("/");
             }
             sb.append(tag);
             return sb.toString();
         }
 
         void addTag(String tag) {
             uris.add(tag);
         }
 
         void setUrisToDepth(int depth) {
             uris = new LinkedList<String>(uris.subList(0, depth));
         }
     }
 }

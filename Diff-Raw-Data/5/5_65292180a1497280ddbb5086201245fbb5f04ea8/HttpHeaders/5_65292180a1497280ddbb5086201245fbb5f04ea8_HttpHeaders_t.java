 package com.ninja_squad.jb.codestory;
 
 import com.google.common.base.Objects;
 import com.google.common.base.Optional;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Splitter;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Maps;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.nio.charset.Charset;
 import java.nio.charset.StandardCharsets;
 import java.util.Iterator;
 import java.util.Map;
 
 /**
  * The headers of a request or response, stored in lower-case
  * @author JB
  */
 public final class HttpHeaders {
     public static final String CONTENT_LENGTH = "content-length";
     public static final String CONTENT_TYPE = "content-type";
 
     /**
      * The headers, including the content type
      */
     private final Map<String, String> map;
 
     /**
      * The content type header
      */
     private final Optional<ContentType> contentType;
 
     private HttpHeaders(Builder builder) {
         this.map = ImmutableMap.copyOf(builder.map);
         this.contentType = Optional.fromNullable(builder.contentType);
     }
 
     public Optional<String> getHeader(String key) {
         return Optional.fromNullable(map.get(key));
     }
 
     public Optional<ContentType> getContentType() {
         return contentType;
     }
 
     @Override
     public String toString() {
         return Objects.toStringHelper(this).add("map", map).add("contentType", contentType).toString();
     }
 
     void writeTo(Writer writer) throws IOException {
         String EOL = "\r\n";
         for (Map.Entry<String, String> header : map.entrySet()) {
             writer.write(header.getKey());
             writer.write(':');
             writer.write(header.getValue());
             writer.write(EOL);
         }
     }
 
     public static class ContentType {
         /**
          * The default HTTP charset
          */
         public static final Charset DEFAULT_CHARSET = StandardCharsets.ISO_8859_1;
 
         private final String name;
         private final Charset charset;
 
         public ContentType(String name, Charset charset) {
             this.name = Preconditions.checkNotNull(name);
             this.charset = Preconditions.checkNotNull(charset);
         }
 
         public static ContentType parse(String line) {
             Iterator<String> parts = Splitter.on(';').trimResults().split(line).iterator();
             String name = parts.next();
             Charset charset = DEFAULT_CHARSET;
             if (parts.hasNext()) {
                 String charsetDefinition = parts.next();
                 Iterator<String> charsetParts = Splitter.on('=').trimResults().split(charsetDefinition).iterator();
                 String key = charsetParts.next();
                 if (key.equals("charset") && charsetParts.hasNext()) {
                     charset = Charset.forName(charsetParts.next());
                 }
             }
             return new ContentType(name, charset);
         }
 
         public String getName() {
             return name;
         }
 
         public Charset getCharset() {
             return charset;
         }
 
         public String toHeaderValue() {
             return name + "; charset=" + charset.name();
         }
 
         @Override
         public String toString() {
             return Objects.toStringHelper(this).add("name", name).add("charset", charset).toString();
         }
     }
 
     public static Builder builder() {
         return new Builder();
     }
 
     public static final class Builder {
         private final Map<String, String> map = Maps.newHashMap();
         private ContentType contentType;
 
         public Builder add(String name, String value) {
             Preconditions.checkNotNull(name);
             Preconditions.checkNotNull(value);
 
             name = name.toLowerCase();
             if (name.equals(CONTENT_TYPE)) {
                 contentType = ContentType.parse(value);
             }
 
             map.put(name, value);
             return this;
         }
 
         public Builder setContentType(String name, Charset charset) {
             contentType = new ContentType(name, charset);
             map.put(CONTENT_TYPE, name + ";charset=" + charset.name());
             return this;
         }
 
         public Optional<ContentType> getContentType() {
             return Optional.fromNullable(contentType);
         }
 
         public HttpHeaders build() {
             return new HttpHeaders(this);
         }
     }
 }

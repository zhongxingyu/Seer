 package http.serialisation;
 
 import http.header.ContentType;
 
 /**
  * Deserialise the body that contains a specified {@code Content-Type} and is contained in a specified input type.
  *
  * The {@code Content-Type} will usually be something like {@code application/json}, {@code application/xml}, or
 * {@code application/x-www-form-urlencoded}. Where as the input type will be some for of {@link java.io.InputStream} or
 * {@link CharSequence}.
  *
  * @param <I>   the type of input the {@code BodyDeserialiser} accepts.
  * @param <C>   the type of {@code Content-Type} the {@code BodyDeserialiser} accepts. This should be set using a typed
  *              {@link http.Header} like {@link http.header.JsonContentType} or {@link http.header.XmlContentType}.
  *
  * @author Karl Bennett
  */
 public abstract class BodyDeserialiser<I, C extends ContentType> {
 
     private final Class<I> inputType;
     private final Class<C> contentType;
 
 
     /**
      * Create a new {@code BodyDeserialiser} that accepts the supplied input and content types.
      *
      * @param inputType the type of input that the {@code BodyDeserialiser} accepts.
      * @param contentType the type of content that should be contained within the input.
      */
     protected BodyDeserialiser(Class<I> inputType, Class<C> contentType) {
 
         this.inputType = inputType;
         this.contentType = contentType;
     }
 
 
     /**
      * Convert the supplied input into the required type.
      *
      * @param type the type of object that will be output after conversion.
      * @param input the input to be converted.
      * @param <T> the output type.
      * @return the converted input.
      */
     public abstract <T> T deserialise(Class<T> type, I input);
 
 
     /**
      * Get the input type that this converter accepts.
      *
      * @return the supported input type.
      */
     public Class<I> getInputType() {
 
         return inputType;
     }
 
     /**
      * Get the content type that this converter accepts.
      *
      * @return the supported content type.
      */
     public Class<C> getContentType() {
 
         return contentType;
     }
 }

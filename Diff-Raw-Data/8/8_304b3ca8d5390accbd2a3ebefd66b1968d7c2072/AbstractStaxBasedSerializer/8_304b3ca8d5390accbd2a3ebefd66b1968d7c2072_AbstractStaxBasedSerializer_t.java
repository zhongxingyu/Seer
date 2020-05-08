 package com.cedarsoft.serialization.stax;
 
 import com.cedarsoft.Version;
 import com.cedarsoft.VersionMismatchException;
 import com.cedarsoft.VersionRange;
 import com.cedarsoft.serialization.AbstractXmlSerializer;
 import org.jetbrains.annotations.NonNls;
 import org.jetbrains.annotations.NotNull;
 
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import java.io.IOException;
 import java.io.InputStream;
 
 /**
  * @param <T> the type
  * @param <S> the object to serialize to
  */
 public abstract class AbstractStaxBasedSerializer<T, S> extends AbstractXmlSerializer<T, S, XMLStreamReader, XMLStreamException> {
   protected AbstractStaxBasedSerializer( @NotNull @NonNls String defaultElementName, @NotNull VersionRange formatVersionRange ) {
     super( defaultElementName, formatVersionRange );
   }
 
   @Override
   @NotNull
   public T deserialize( @NotNull InputStream in ) throws IOException {
     try {
       XMLStreamReader reader = StaxSupport.getXmlInputFactory().createXMLStreamReader( in );
       Version version = Version.parse( getProcessingInstructionData( reader, PI_TARGET_FORMAT ) );
       if ( !getFormatVersionRange().contains( version ) ) {
         throw new VersionMismatchException( getFormatVersion(), version );
       }
 
       reader.nextTag();
       T deserialized = deserialize( reader );
 
       if ( !reader.isEndElement() ) {
         throw new IllegalStateException( "Not consumed everything in <" + getClass().getName() + ">" );
       }
 
       if ( reader.next() != XMLStreamReader.END_DOCUMENT ) {
         throw new IllegalStateException( "Not consumed everything in <" + getClass().getName() + ">" );
       }
 
       return deserialized;
     } catch ( XMLStreamException e ) {
       throw new IOException( "Could not parse stream due to " + e.getMessage(), e );
     }
   }
 
   @NotNull
   @NonNls
   protected String getProcessingInstructionData( @NotNull XMLStreamReader reader, @NotNull @NonNls String piTarget ) throws XMLStreamException {
     int result = reader.next();
     if ( result != XMLStreamReader.PROCESSING_INSTRUCTION ) {
      throw new IllegalArgumentException( "No processing instruction found. Was <" + StaxSupport.getEventName( result ) + ">" );
     }
 
     String foundTarget = reader.getPITarget();
     if ( !foundTarget.equals( piTarget ) ) {
       throw new IllegalArgumentException( "Invalid processing instruction. Expected <" + piTarget + "> but was <" + foundTarget + ">" );
     }
 
     return reader.getPIData();
   }
 
   protected void ensureTag( @NotNull XMLStreamReader deserializeFrom, @NotNull @NonNls String tagName ) {
     String current = deserializeFrom.getName().getLocalPart();
     if ( !current.equals( tagName ) ) {
       throw new IllegalStateException( "Invalid tag. Was <" + current + "> but expected <" + tagName + ">" );
     }
   }
 
   /**
    * Returns the text and closes the tag
    *
    * @param reader the reader
    * @return the text
    *
    * @throws XMLStreamException
    */
   @NotNull
   protected String getText( @NotNull XMLStreamReader reader ) throws XMLStreamException {
     StringBuilder content = new StringBuilder();
 
     int result;
     while ( ( result = reader.next() ) != XMLStreamReader.END_ELEMENT ) {
       if ( result != XMLStreamReader.CHARACTERS ) {
         throw new IllegalStateException( "Invalid state: " + result );
       }
       content.append( reader.getText() );
     }
 
     return content.toString();
   }
 
   @NotNull
   protected String getChildText( @NotNull XMLStreamReader reader, @NotNull @NonNls String tagName ) throws XMLStreamException {
     reader.nextTag();
     ensureTag( reader, tagName );
     return getText( reader );
   }
 
   protected void closeTag( @NotNull XMLStreamReader reader ) throws XMLStreamException {
     int result = reader.nextTag();
     if ( result != XMLStreamReader.END_ELEMENT ) {
      throw new IllegalStateException( "Invalid result. Expected <END_ELEMENT> but was <" + StaxSupport.getEventName( result ) + ">" );
     }
   }
 
   protected void nextTag( @NotNull XMLStreamReader reader, @NotNull @NonNls String tagName ) throws XMLStreamException {
     int result = reader.nextTag();
     if ( result != XMLStreamReader.START_ELEMENT ) {
      throw new IllegalStateException( "Invalid result. Expected <START_ELEMENT> but was <" + StaxSupport.getEventName( result ) + ">" );
     }
     ensureTag( reader, tagName );
   }
 
   /**
    * Attention! The current element will be closed!
    *
    * @param streamReader the stream reader
    * @param callback     the callback
    * @throws XMLStreamException
    * @throws IOException
    */
   protected void visitChildren( @NotNull XMLStreamReader streamReader, @NotNull CB callback ) throws XMLStreamException, IOException {
     while ( streamReader.nextTag() != XMLStreamReader.END_ELEMENT ) {
       String tagName = streamReader.getName().getLocalPart();
       callback.tagEntered( streamReader, tagName );
     }
   }
 
   public interface CB {
     /**
      * Is called for each child.
      * ATTENTION: This method *must* close the tag
      *
      * @param deserializeFrom the reader
      * @param tagName         the tag name
      * @throws XMLStreamException
      * @throws IOException
      */
     void tagEntered( @NotNull XMLStreamReader deserializeFrom, @NotNull @NonNls String tagName ) throws XMLStreamException, IOException;
   }
 }

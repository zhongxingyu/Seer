 package org.codehaus.xfire.aegis.type.basic;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.Collections;
 import java.util.Set;
 import javax.xml.namespace.QName;
 
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.aegis.MessageReader;
 import org.codehaus.xfire.aegis.MessageWriter;
 import org.codehaus.xfire.aegis.type.Type;
 import org.codehaus.xfire.aegis.type.TypeMapping;
 import org.codehaus.xfire.fault.XFireFault;
 import org.codehaus.xfire.soap.SoapConstants;
 import org.codehaus.xfire.util.Base64;
 import org.jdom.Attribute;
 import org.jdom.Element;
 import org.w3c.dom.Document;
 
 /**
  * Type for runtime inspection of types. Looks as the class to be written, and looks to see if there is a type for that
  * class. If there is, it writes out the value and inserts a <em>xsi:type</em> attribute to signal what the type of the
  * value is.
  *
  * Can specify an optional set of dependent <code>Type</code>'s in the constructor, in the case that the type is a
  * custom type that may not have its schema in the WSDL.
  *
  * Can specify whether or not unknown objects should be serialized as a byte stream.
  *
  * @author <a href="mailto:peter.royal@pobox.com">peter royal</a>
  */
 public class ObjectType extends Type
 {
     private static final QName XSI_TYPE = new QName( SoapConstants.XSI_NS, "type" );
     private static final QName XSI_NIL = new QName( SoapConstants.XSI_NS, "nil" );
 
     private Set dependencies;
     private boolean serializedWhenUnknown;
     private boolean readToDocument = false;
     
     public ObjectType()
     {
         this( Collections.EMPTY_SET );
         readToDocument = true;
     }
 
     public ObjectType( Set dependencies )
     {
         this( dependencies, false );
     }
 
     public ObjectType( boolean serializeWhenUnknown )
     {
         this( Collections.EMPTY_SET, serializeWhenUnknown );
     }
 
     public ObjectType( Set dependencies, boolean serializeWhenUnknown )
     {
         this.dependencies = dependencies;
         this.serializedWhenUnknown = serializeWhenUnknown;
     }
 
     public Object readObject( MessageReader reader, MessageContext context ) throws XFireFault
     {
         if( isNil( reader.getAttributeReader( XSI_NIL ) ) )
         {
 			while( reader.hasMoreElementReaders() ) {
                 reader.getNextElementReader();
             }
 
             return null;
         }
 
         MessageReader typeReader = reader.getAttributeReader( XSI_TYPE );
 
         if( null == typeReader && !readToDocument )
         {
             throw new XFireFault( "Missing 'xsi:type' attribute", XFireFault.SENDER );
         }
 
         String typeName = typeReader.getValue();
 
         if( null == typeName  && !readToDocument )
         {
             throw new XFireFault( "Missing 'xsi:type' attribute value", XFireFault.SENDER);
         }
 
         Type type = null;
 		QName typeQName = null;
 		if (typeName != null)
         {
             typeQName = extractQName(reader, typeName);
             type = getTypeMapping().getType( typeQName );
         }
 
 		if (type == null && readToDocument)
 		{
 			type = getTypeMapping().getType(Document.class);
 		}
 
         if( null == type )
         {
             //TODO should check namespace as well..
             if( serializedWhenUnknown && "serializedJavaObject".equals( typeName ) )
             {
                 return reconstituteJavaObject( reader );
             }
 
             throw new XFireFault( "No mapped type for '" + typeName + "' (" + typeQName + ")", XFireFault.SENDER);
         }
 
         return type.readObject( reader, context );
     }
 
     private QName extractQName( MessageReader reader, String typeName )
     {
         int colon = typeName.indexOf( ':' );
 
         if( -1 == colon )
         {
             return new QName( reader.getNamespace(), typeName );
         }
         else
         {
             return new QName( reader.getNamespaceForPrefix( typeName.substring( 0, colon ) ),
                               typeName.substring( colon + 1 ) );
         }
     }
 
     private Object reconstituteJavaObject( MessageReader reader ) throws XFireFault
     {
         ByteArrayInputStream in = new ByteArrayInputStream( Base64.decode( reader.getValue() ) );
 
         try
         {
             return new ObjectInputStream( in ).readObject();
         }
         catch( Exception e )
         {
             throw new XFireFault( "Unable to reconstitute serialized object", e, XFireFault.RECEIVER );
         }
     }
 
     private boolean isNil( MessageReader reader )
     {
         return null != reader && "true".equals( reader.getValue() );
     }
 
     public void writeObject( Object object, MessageWriter writer, MessageContext context ) throws XFireFault
     {
         if( null == object )
         {
             MessageWriter nilWriter = writer.getAttributeWriter( XSI_NIL );
 
             nilWriter.writeValue( "true" );
 
             nilWriter.close();
         }
         else
         {
             Type type = determineType( object.getClass() );
 
             if( null == type )
             {
                 handleNullType( object, writer );
             }
             else
             {
                 String prefix = writer.getPrefixForNamespace( type.getSchemaType().getNamespaceURI() );
 
                 if( null == prefix || prefix.length() == 0 )
                 {
                     addXsiType( writer, type.getSchemaType().getLocalPart() );
                 }
                 else
                 {
                     addXsiType( writer, prefix + ":" + type.getSchemaType().getLocalPart() );
                 }
 
                 type.writeObject( object, writer, context );
             }
         }
     }
 
     private Type determineType( Class clazz )
     {
         TypeMapping mapping = getTypeMapping();
         Type type = mapping.getType( clazz );
 
         if (null != type) 
         {
             return type;
         }
 
         Class[] interfaces = clazz.getInterfaces();
 
         for( int i = 0; i < interfaces.length; i++ )
         {
             Class anInterface = interfaces[i];
 
             type = mapping.getType( anInterface );
 
             if( null != type ) {
                 return type;
             }
         }
 
         Class superclass = clazz.getSuperclass();
 
         if( null == superclass || Object.class.equals( superclass)) {
             return null;
         }
 
         return determineType( superclass );
     }
 
     private void addXsiType( MessageWriter writer, String prefixedType )
     {
         MessageWriter typeWriter = writer.getAttributeWriter( XSI_TYPE );
 
         typeWriter.writeValue( prefixedType );
 
         typeWriter.close();
     }
 
     private void handleNullType( Object object, MessageWriter writer ) throws XFireFault
     {
         if( !serializedWhenUnknown )
         {
            throw new XFireFault( "Unable to write '" + object + "' [" + object.getClass().getName() + "]. Type is unknown.",
                                   XFireFault.RECEIVER );
         }
 
         addXsiType( writer,
                     "serializedJavaObject" ); //TODO not sure what namespace to put here..should match what is put in writeSchema
 
         ByteArrayOutputStream out = new ByteArrayOutputStream( 4096 );
 
         try
         {
             ObjectOutputStream objectOutputStream = new ObjectOutputStream( out );
 
             objectOutputStream.writeObject( object );
             objectOutputStream.close();
         }
         catch( IOException e )
         {
             throw new XFireFault( "Unable to serialize '" + object + "' [" + object.getClass().getName() + "]",
                                   e,
                                   XFireFault.RECEIVER );
         }
 
         writer.writeValue( Base64.encode( out.toByteArray() ) );
     }
 
     public boolean isReadToDocument()
     {
         return readToDocument;
     }
 
     public void setReadToDocument(boolean readToDocument)
     {
         this.readToDocument = readToDocument;
     }
 
     public boolean isSerializedWhenUnknown()
     {
         return serializedWhenUnknown;
     }
 
     public void setSerializedWhenUnknown(boolean serializedWhenUnknown)
     {
         this.serializedWhenUnknown = serializedWhenUnknown;
     }
 
     public void setDependencies(Set dependencies)
     {
         this.dependencies = dependencies;
     }
 
     public Set getDependencies()
     {
         return dependencies;
     }
 
     public boolean isComplex()
     {
         return true;
     }
 
     public boolean isAbstract()
     {
         // TODO Auto-generated method stub
         return super.isAbstract();
     }
 
     public boolean isNillable()
     {
         // TODO Auto-generated method stub
         return super.isNillable();
     }
 
     public boolean isWriteOuter()
     {
         // TODO Auto-generated method stub
         return super.isWriteOuter();
     }
 
     public void setNillable(boolean nillable)
     {
         // TODO Auto-generated method stub
         super.setNillable(nillable);
     }
 
     public void writeSchema( Element root )
     {
         if( serializedWhenUnknown )
         {
             Element simple = new Element( "simpleType", SoapConstants.XSD_PREFIX, SoapConstants.XSD );
             simple.setAttribute( new Attribute( "name", "serializedJavaObject" ) );
             root.addContent( simple );
 
             Element restriction = new Element( "restriction", SoapConstants.XSD_PREFIX, SoapConstants.XSD );
             restriction.setAttribute( new Attribute( "base", SoapConstants.XSD_PREFIX + ":base64Binary" ) );
 
             simple.addContent( restriction );
         }
     }
 }

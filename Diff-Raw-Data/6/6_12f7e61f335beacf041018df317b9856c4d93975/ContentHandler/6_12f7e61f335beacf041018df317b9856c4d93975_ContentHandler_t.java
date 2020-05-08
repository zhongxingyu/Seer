 package com.google.buzz.parser.handler;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import com.google.buzz.model.BuzzContent;
 
 /**
  * Handler for element: <b>Content</b>
  * 
  * @author roberto.estivill
  */
 public class ContentHandler
     extends BaseHandler
 {
     /**
      * XML elements handled by this handler
      */
     private static final String CONTENT = "content";
     private static final String ORIGINAL_CONTENT = "buzz:original-content";
     private static final String TYPE = "type";
 
     /**
      * Object to return
      */
     private BuzzContent content;
 
     /**
      * Constructor method to create a child handler.
      * 
      * @param parentHandler handler
      */
     public ContentHandler( BaseHandler aParent )
     {
         super( aParent );
         content = new BuzzContent();
     }
 
     /**
      * @return the created content object
      */
     public BuzzContent getBuzzContent()
     {
         return content;
     }
 
     /**
      * Method to be called every time an xml element starts
      */
     public void startElement( String uri, String name, String qName, Attributes attributes )
         throws SAXException
     {
         String type = attributes.getValue( TYPE );
         if ( type != null && !type.equals( "" ) )
             content.setType( type );
     }
 
     /**
      * Method to be called every time an xml element ends
      */
     public void endElement( String uri, String name, String qName )
         throws SAXException
     {
         if ( CONTENT.equals( name ) || ORIGINAL_CONTENT.equals( qName ) )
         {
             stopHandlingEvents();
             parentHandler.endElement( uri, name, qName );
         }
     }
 
     /**
      * Method to be called between the beginning and the end of the xml elements.
      */
     public void characters( char[] ch, int start, int length )
         throws SAXException
     {
         String s = new String( ch ).substring( start, start + length );
         String prev = content.getText();
        if (prev==null) {
          content.setText(s);
        } else {
          content.setText( prev + s);
        }
     }
 
 }

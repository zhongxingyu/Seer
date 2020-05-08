 
 /***************************************************************************
  *   Copyright (C) 2010 iCoreTech research labs                            *
  *   Contributed code from:                                                *
  *   - Valerio Chiodino - keytwo at keytwo dot net                         *
  *   - Fabio Tunno      - fat at fatshotty dot net                         *
  *                                                                         *
  *   This program is free software: you can redistribute it and/or modify  *
  *   it under the terms of the GNU General Public License as published by  *
  *   the Free Software Foundation, either version 3 of the License, or     *
  *   (at your option) any later version.                                   *
  *                                                                         *
  *   This program is distributed in the hope that it will be useful,       *
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
  *   GNU General Public License for more details.                          *
  *                                                                         *
  *   You should have received a copy of the GNU General Public License     *
  *   along with this program. If not, see http://www.gnu.org/licenses/     *
  *                                                                         *
  ***************************************************************************/
 
 package fm.audiobox.core.api;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Stack;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.ResponseHandler;
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.DefaultHandler;
 
 import fm.audiobox.core.exceptions.ModelException;
 import fm.audiobox.core.exceptions.ServiceException;
 import fm.audiobox.core.models.AudioBoxClient;
 import fm.audiobox.core.models.AudioBoxClient.AudioBoxConnector;
 import fm.audiobox.core.models.Track;
 import fm.audiobox.core.util.Inflector;
 
 
 /**
  * Model is an abstract class that provides the RESTful API parser for each 
  * XML AudioBox.fm response.
  * 
  * <p>
  * 
  * It basically transforms XML responses into comfortable Java objects.
  * 
  * <p>
  * 
  * It implements a {@link ResponseHandler} called back from the HttpClient object.
  * 
  *
  * @author Valerio Chiodino
  * @author Fabio Tunno
  * @version 0.0.1
  */
 public abstract class Model extends DefaultHandler implements ResponseHandler<String[]> {
 
     /** Constant that defines bytes dimention to be read from responses {@link InputStream} */
     protected static final int CHUNK = 4096;
 
     private static final String ADD_PREFIX = "add";
     private static final String SET_PREFIX = "set";
 
     private static Log log = LogFactory.getLog(Model.class);
 
     private boolean mSkipField = false;
     private Stack<Object> mStack;
     private Inflector mInflector = Inflector.getInstance();
     private StringBuffer mStringBuffer = new StringBuffer();
 
     // Default models variables
     protected String pName;
     protected String pToken;
     protected String pEndPoint;
     protected AudioBoxConnector pConnector;
 
 
     /**
      * <p>Getter for the end point of this model.</p>
      *
      * @return the model specific API end point.
      */
     public final String getEndPoint(){
         return this.pEndPoint;
     }
 
     /**
      * <p>Getter for this model {@link AudioBoxConnector connector}.</p>
      *
      * @return the {@link AudioBoxConnector} object used by this model.
      */
     public AudioBoxConnector getConnector(){
         return this.pConnector;
     }
 
     /**
      * <p>Setter for the {@link AudioBoxConnector connector}.</p>
      *
      * @param connector a {@link AudioBoxConnector} object.
      */
     public void setConnector(AudioBoxConnector connector){
         this.pConnector = connector;
     }
 
     /**
      * <p>Getter for the name of this model, may be vary depending the Model extension.</p>
      *
      * @return the name of the model.
      */
     public String getName() {
         return this.pName;
     }
 
     /**
      * <p>Setter for the model name: used by the parser.</p>
      *
      * @param name the name of the model.
      */
     public void setName(String name) {
         this.pName = name;
     }
 
     /**
      * <p>Getter for the model token.</p>
      *
      * @return the unique token of the model.
      */
     public String getToken() {
         return this.pToken;
     }
 
     /**
      * <p>Setter for the model token: used by the parser.</p>
      *
      * @param token the unique token of the model.
      */
     public final void setToken(String token) {
         this.pToken = token;
     }
 
     /** {@inheritDoc} */
     @Override
     public String[] handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
 
         int responseCode = response.getStatusLine().getStatusCode();
         String responseString = "";
 
         switch( responseCode ) {
 
         // 20*
         case HttpStatus.SC_CREATED:
         case HttpStatus.SC_OK:
             
             responseString = this.parseResponse( response );
             break;
 
         case HttpStatus.SC_NO_CONTENT:
             responseString = "Resource not ready";
             break;
 
             // 30*
         case HttpStatus.SC_SEE_OTHER:
             responseString = response.getFirstHeader("Location").getValue();
             break;
 
             // 40*
         case HttpStatus.SC_UNAUTHORIZED:
             throw new ClientProtocolException(String.valueOf(responseCode));
 
         case HttpStatus.SC_UNPROCESSABLE_ENTITY:
             throw new ServiceException( "Unprocessable entity", responseCode );
 
         case HttpStatus.SC_NOT_FOUND:
             throw new ServiceException( "Resource not found", responseCode );
 
             // 50*
         default:
             throw new ServiceException( "Response code not recognized (" + responseCode + ")", responseCode );
 
         }
 
 
         HttpEntity responseEntity = response.getEntity(); 
         if (responseEntity != null) 
             responseEntity.consumeContent();
 
         return new String[]{ String.valueOf( responseCode ) , responseString };
 
     }
 
     /**
      * This method is used to determine the kind of response and parse the HTTP response content if any.
      * 
      * <p>
      * 
      * Returns an empty String if the content type is XML or binary, the body of the response otherwise.
      *
      * @param response the {@link HttpResponse} object
      * 
      * @return the text of the response content  
      * 
      * @throws IOException if the parse process fails for some reason.
      */
     public final String parseResponse( HttpResponse response ) throws IOException {
 
         Header contentType = response.getEntity().getContentType();
         String rsp = "";
         if ( contentType.getValue().contains(AudioBoxConnector.XML_FORMAT) )
             rsp = this.parseXMLResponse( response.getEntity().getContent() );
 
         else if ( contentType.getValue().contains( AudioBoxConnector.TEXT_CONTENT_TYPE )  )
             rsp = this.parsePlainResponse( response.getEntity().getContent() );
         
         else
         	rsp = this.parseBinaryResponse( response );
 
         HttpEntity ent = response.getEntity();
         if (ent != null)
             ent.consumeContent();
         
         return rsp;
     }
 
     /**
      * This method is used to parse XML responses
      * 
     * @param input the {@link InputStream} of the entity content.
      * 
      * @return an empty String
      */
     protected String parseXMLResponse( InputStream input ) throws IOException {
         String response = "";
         try {
 
             // Instanciate new SaxParser
             SAXParserFactory spf = SAXParserFactory.newInstance();
             SAXParser sp = spf.newSAXParser();
 
             /* Get the XMLReader of the SAXParser we created. */
             XMLReader xr = sp.getXMLReader();
 
             /* Create a new ContentHandler and apply it to the XML-Reader */
             xr.setContentHandler( this );
 
             xr.parse( new InputSource( input ) );
 
             input.close();
 
         } catch (ParserConfigurationException e) {
             e.printStackTrace();
 
         } catch (SAXException e) {
             e.printStackTrace();
 
         }
         return response;
     }
 
     /**
      * This method is used to parse plain text responses.
      * 
     * @param input the {@link InputStream} of the entity content.
      * 
      * @return a String representing the body of the response
      */
     protected String parsePlainResponse(InputStream input) throws IOException {
 
         int read;
         byte[] bytes = new byte[ CHUNK ];
         StringBuffer sb = new StringBuffer();
         while(  ( read = input.read( bytes) ) != -1 )
             sb.append( new String( bytes, 0, read ));
         return sb.toString().trim();
 
     }
     
     /**
      * This method implementation does nothing. It must be overridden by each model that should contains
      * a binary response in the body. Tipically {@link Track} objects.
      * 
      * @param response the {@link HttpResponse} object 
      * 
      * @return an empty String
      */
     protected String parseBinaryResponse( HttpResponse response ) throws IOException {
         return "";
     }
 
     
     
     /* --------- */
     /* Overrides */
     /* --------- */
 
 
     /** {@inheritDoc} */
     @Override
     public String toString() {
         return this.getName();
     }
 
     /** {@inheritDoc} */
     @Override
     public final void startDocument() throws SAXException {
         this.mStack = new Stack<Object>();
         this.mStack.push( this );
         super.startDocument();
     }
 
     /** {@inheritDoc} */
     @Override
     public void endDocument() throws SAXException {
         this.mStack = null;
         super.endDocument();
     }
 
 
     /** {@inheritDoc} */
     @Override
     public final void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
 
         Object peek = this.mStack.peek();
 
         mSkipField = false;
 
         try {
             if (localName.trim().length() == 0)
                 localName = qName;
 
             localName =  mInflector.upperCamelCase( localName, '-' );
 
             String methodPrefix = SET_PREFIX;
             String scm = mInflector.singularize( peek.getClass().getSimpleName() );
             if (!scm.equals( peek.getClass().getSimpleName() ) && localName.equals( scm  ) ) {
                 methodPrefix = ADD_PREFIX;
             }
 
             String methodName =  methodPrefix + localName;
             Method method = null;
             try {
                 method = peek.getClass().getMethod(methodName, String.class);
             } catch (NoSuchMethodException e) {
                 for (Method m : peek.getClass().getMethods()) {
                     if (m.getName().equals( methodName )) {
                         method = m;
                         break;
                     }
                 }
             }
 
 
             if (method == null)
                 mSkipField = true;
 
             if ( !mSkipField ) {
 
                 Class<?> argType = method.getParameterTypes()[0];
 
                 if ( ! argType.isPrimitive() && ! argType.equals(String.class) ){
 
                     Model subClass = null;
                     try {
                         subClass = AudioBoxClient.getModelInstance( mInflector.lowerCamelCase( localName, '-'), this.getConnector() );
                         method.invoke(peek, subClass );
 
                         this.mStack.push( subClass );
                     } catch (ModelException e) {
                         e.printStackTrace();
 
                     }
 
                 } else {
                     this.mStack.push( method );
                 }
             }
 
         } catch (IllegalArgumentException e) {
             log.error("Illegal Argument Exception @" + localName + ": " + e.getMessage());
             e.printStackTrace();
 
         } catch (IllegalAccessException e) {
             log.error("Illegal AccessException @" + localName + ": " + e.getMessage());
             e.printStackTrace();
 
         } catch (SecurityException e) {
             log.error("Security Exception @" + localName + ": " + e.getMessage());
             e.printStackTrace();
 
         } catch (InvocationTargetException e) {
             log.error("Invocation Target Exception @" + localName + ": " + e.getMessage());
             e.printStackTrace();
 
         } 
 
         super.startElement(uri, localName, qName, attributes);
     }
 
     /** {@inheritDoc} */
     @Override
     public void endElement(String uri, String localName, String qName) throws SAXException {
 
         if ( !mSkipField ) {
 
             String _temp = mStringBuffer.toString();
             _temp = _temp.replace("\n","").trim();
             mStringBuffer = new StringBuffer();
 
 
             if (this.mStack.peek() instanceof Method) {
 
                 Method method = ( Method ) this.mStack.peek();
                 Object _dest = this.mStack.get(  this.mStack.size() - 2 );
 
                 try {
                     if (_temp.trim().length() > 0) {
                         method.invoke(_dest, _temp);
                     }
                 } catch (IllegalArgumentException e) {
                     e.printStackTrace();
                 } catch (IllegalAccessException e) {
                     e.printStackTrace();
                 } catch (InvocationTargetException e) {
                     e.printStackTrace();
                 }
 
             }
 
             this.mStack.pop();
 
         }
 
         super.endElement(uri, localName, qName);
     }
 
     /** {@inheritDoc} */
     @Override
     public void characters(char[] ch, int start, int length) throws SAXException {
 
         if ( !mSkipField ) {
             mStringBuffer.append( String.valueOf( ch , start , length ) );
         }
 
         super.characters(ch, start, length);
     }
 
 
 }

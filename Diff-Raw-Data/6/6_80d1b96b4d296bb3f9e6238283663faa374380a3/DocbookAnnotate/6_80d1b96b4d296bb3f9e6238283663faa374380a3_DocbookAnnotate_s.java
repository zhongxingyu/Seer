 /**
  * \file DocbookAnnotate.java
  * \brief The DocbookAnnotate class
  * \package plugins;
  */
 
 package dk.dbc.opensearch.plugins;
 
 /*
    
 This file is part of opensearch.
 Copyright © 2009, Dansk Bibliotekscenter a/s, 
 Tempovej 7-11, DK-2750 Ballerup, Denmark. CVR: 15149043
 
 opensearch is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 opensearch is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with opensearch.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 
 import dk.dbc.opensearch.common.helpers.OpensearchNamespaceContext;
 import dk.dbc.opensearch.common.pluginframework.IAnnotate;
 import dk.dbc.opensearch.common.pluginframework.PluginException;
 import dk.dbc.opensearch.common.pluginframework.PluginType;
 import dk.dbc.opensearch.common.types.CargoContainer;
 import dk.dbc.opensearch.common.types.CargoObject;
 import dk.dbc.opensearch.common.types.DataStreamType;
 import dk.dbc.opensearch.common.types.IndexingAlias;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.lang.Integer;
 import java.lang.StringBuilder;
 import java.net.HttpURLConnection;
 import java.net.URL;
 
 import javax.xml.namespace.NamespaceContext;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.*;
 
 import org.apache.log4j.Logger;
 import org.w3c.dom.Document;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 
 /**
  * Plugin for annotating docbook carcoContainers
  */
 public class DocbookAnnotate implements IAnnotate
 {
     static Logger log = Logger.getLogger( DocbookAnnotate.class );
 
     private PluginType pluginType = PluginType.ANNOTATE;
     private  NamespaceContext nsc;
 
 
     /**
      * Constructor for the DocbookAnnotate plugin.
      */
 
     public DocbookAnnotate()
     {
         log.debug( "Constructor() called" );
         nsc = new OpensearchNamespaceContext();
     }
 
     /**
      * The "main" method of this plugin. Request annotation data from
      * a webservice. If annotationdata is available it added to the
      * cargocontainer in a new stream typed DublinCoreData
      *
      * @param CargoContainer The CargoContainer to annotate
      *
      * @returns An annotated CargoContainer
      * 
      * @throws PluginException thrown if anything goes wrong during annotation.
      */
 
     public CargoContainer getCargoContainer( CargoContainer cargo ) throws PluginException
     {
         log.debug( "getCargoContainer() called" );
 
         // our namespace context for evaluating xpath expressions
         
         log.debug( "Retrive docbook xml from CargoContainer" );
         
         if ( cargo == null )
         {
             log.error( "DocbookAnnotate getCargoContainer cargo is null" );
             throw new PluginException( new NullPointerException( "DocbookAnnotate getCargoContainer throws NullPointerException" ) );
         }
         else 
         {
             log.debug( "DocbookAnnotate getCargoContainer cargo is not null" );
         }
         
         CargoObject co = cargo.getCargoObject( DataStreamType.OriginalData );
         
         if ( co == null )
         {
             String error = "DocbookAnnotate getCargoContainer cargo object null";
             log.error( error );
             throw new PluginException( String.format( error ) );
         }
         
         byte[] b = co.getBytes();
         XPath xpath = XPathFactory.newInstance().newXPath();
         xpath.setNamespaceContext( nsc );
         XPathExpression xPathExpression;
         XPathExpression xPathExpression_numOfRec;
         
         try
         {
             xPathExpression = xpath.compile( "/docbook:article/docbook:title" );
         } 
         catch (XPathExpressionException e) 
         {
             throw new PluginException( String.format( "Could not compile xpath expression '%s'",  "/docbook:article/docbook:title" ), e );
         }
 
         InputSource docbookSource = new InputSource(new ByteArrayInputStream( b ) );
 
         // Find title of the docbook document
         String title;
         try 
         {
             title = xPathExpression.evaluate( docbookSource ).replaceAll( "\\s", "+" );
         } 
         catch (XPathExpressionException xpe) 
         {
             throw new PluginException( "Could not evaluate xpath expression to find title", xpe );
 
         }
 
         // isolate format
         String serverChoice = co.getFormat();
 
         // Querying webservice
         log.debug( String.format( "querying the webservice with title='%s', serverChoice(format)='%s'", title, serverChoice ) );
 
         String xmlString = null;
         String queryURL = null;
 
         try 
         {
             queryURL = formURL( title, serverChoice );
             xmlString = httpGet( queryURL );
         } 
         catch (IOException ioe) 
         {
             throw new PluginException( String.format( "could not get result from webservice = %s", queryURL ), ioe);
         }
         log.debug( String.format( "data: title='%s', serverChoice(format)='%s', queryURL='%s', xml retrieved='%s'", title, serverChoice, queryURL, xmlString ) );
 
 
         // put retrieved answer into inputsource object
         log.debug( "Got answer from the webservice" );
         ByteArrayInputStream bis;
         try 
         {
             bis = new ByteArrayInputStream( xmlString.getBytes( "UTF-8" ) );
         } 
         catch (UnsupportedEncodingException uee) 
         {
             throw new PluginException( "Could not convert string to UTF-8 ByteArrayInputStream", uee );
         }
         InputSource annotateSource = new InputSource( bis );
 
         // Get number of records...
         // create xpath exp
         xpath = XPathFactory.newInstance().newXPath();
         xpath.setNamespaceContext( nsc );
         
         //String xpathString = "/docbook:article/docbook:title";
         // \todo: Remove wildcards in xpath expression (something to do with default namespace-shite)
         String xpathString = "/*/*[2]";
         try 
         {
             xPathExpression_numOfRec = xpath.compile( xpathString );
         } 
         catch (XPathExpressionException e) 
         {
             throw new PluginException( String.format( "Could not compile xpath expression '%s'",  xmlString ), e );
         }
 
         int numOfRec = 0;
         
         try 
         {
             numOfRec = Integer.parseInt( xPathExpression_numOfRec.evaluate( annotateSource ) );
         } 
         catch ( NumberFormatException nfe ) 
         {
             log.fatal( String.format( "Could not format number of records returned by the webservice" ) );
             throw new PluginException( "Could not format number of records returned by the webservice", nfe );
         } 
         catch (XPathExpressionException xpee ) 
         {
             log.fatal( String.format( String.format( "The xpath %s failed with reason %s", xpathString, xpee.getMessage() ) ) );
             throw new PluginException( String.format( "The xpath %s failed with reason %s", xpathString, xpee.getMessage() ), xpee );
         }
 
         log.debug( String.format( "Number of record hits='%s', with format='%s'", numOfRec, serverChoice ) );
 
         if( numOfRec == 0 ){ // no hits. Make another search without serverchoice
             @SuppressWarnings("unused")
 			String xmlStr = null;
             queryURL = null;
             try 
             {
                 queryURL = formURL( title, serverChoice );
                 xmlStr = httpGet( queryURL );
             } 
             catch (IOException ioe) 
             {
                 log.fatal( String.format( "Caugth IOException: Could not get result from webservice = %s.", queryURL ) );
                 throw new PluginException( String.format( "could not get result from webservice = %s", queryURL ), ioe);
             }
             log.debug( String.format( "data: title='%s', serverChose(format)=\"\"\nxml retrieved\n%s", title, xmlString ) );
         }
 
         // put retrieved answer into inputsource object
         try
         {
             bis = new ByteArrayInputStream( xmlString.getBytes( "UTF-8" ) );
         } 
         catch (UnsupportedEncodingException uee) 
         {
             log.fatal( String.format( "Could not convert string to UTF-8 ByteArrayInputStream" ) );
             throw new PluginException( "Could not convert string to UTF-8 ByteArrayInputStream", uee );
         }
         annotateSource = new InputSource( bis );
 
         // Get annotation if one is returned 
            
         String xpath_evaluation = null;
         try
         {
             xpath_evaluation = xPathExpression_numOfRec.evaluate( annotateSource );
             numOfRec = Integer.parseInt( xpath_evaluation );
         } 
         catch (NumberFormatException nfe) 
         {
             log.fatal( String.format( "Caught NumberFormatException: could not convert xpath evaluation '%s' to int", xpath_evaluation ) );
             throw new PluginException( String.format( "Caught NumberFormatException: could not convert xpath evaluation '%s' to int",
                                                       xpath_evaluation ), nfe );
         } 
         catch (XPathExpressionException xpe) 
         {
             log.fatal( String.format( "Could not evaluate xpath expression to find number of returned records" ) );
             throw new PluginException( "Could not evaluate xpath expression to find number of returned records", xpe );
         }
 
         log.debug( String.format( "Number of record hits='%s', with format='%s'", numOfRec, serverChoice ) );
 
         if ( numOfRec == 1 )
         {
             try 
             {
                 log.debug( "Adding annotation to CargoContainer" );
                 String isolatedDCData = isolateDCData( xmlString );   
                 /** \todo: use of deprecated method from CargoContainer */
                 cargo.add( DataStreamType.DublinCoreData, co.getFormat(), co.getSubmitter(), "da", "text/xml", IndexingAlias.DC, isolatedDCData.getBytes() );
             } 
             catch (IOException ioe) 
             {
                 log.fatal( "Could not add DC data to CargoContainer" );
                 throw new PluginException( "Could not add DC data to CargoContainer", ioe );
             }
         }
         return cargo;
     }
 
 
     /**
      * Isolates the Dublin Core data from the data retrieved from the
      * webservice.
      *
      *
      * @param The xml String retrieved from the webservice
      * 
      * @throws PluginException Thrown if something goes wrong during xml parsing
      */
 
     private String isolateDCData( String recordXmlString ) throws PluginException
     {
         log.debug( "isolateDCData( recordXMLString ) called" );
         
         // building document 
         Document annotationDocument = null;
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         try{
             DocumentBuilder builder = factory.newDocumentBuilder();
             annotationDocument = builder.parse( new InputSource( new ByteArrayInputStream(  recordXmlString.getBytes() ) ) );
         }catch( ParserConfigurationException pce ){
             log.fatal( String.format( "Caught error while trying to instanciate documentbuilder '%s'", pce ) );
             throw new PluginException( "Caught error while trying to instanciate documentbuilder", pce );
         }catch( SAXException se){
             log.fatal( String.format( "Could not parse annotation data: '%s'", se ) );
             throw new PluginException( "Could not parse annotation data ", se );
         }catch( IOException ioe ){
             log.fatal( String.format( "Could not cast the bytearrayinputstream to a inputsource: '%s'", ioe ) );
             throw new PluginException( "Could not cast the bytearrayinputstream to a inputsource", ioe );
         }
         
         log.debug( String.format( "Isolate Dublin Core from annotation data." ) );
         XPath xpath = XPathFactory.newInstance().newXPath();
         xpath.setNamespaceContext( nsc );
         XPathExpression xPathExpression_record;
         String recordString = null;
         
         try {
             // \todo: Remove wildcards in xpath expression (something to do with default namespace-shite)
             xPathExpression_record = xpath.compile( "/*/*[3]/*/*[3]" );
             recordString  = xPathExpression_record.evaluate( annotationDocument );
         } catch (XPathExpressionException e) {
             throw new PluginException( String.format( "Could not compile xpath expression '%s'",  "/*/*[3]/*/*[3]" ), e );
         }
         log.debug( String.format( "IsolateDC returns xml: %s", recordString ) );
         return recordString;
     }
 
 
     /**
      * Forms the URL to use for annotate query.
      *
      *
      * @param title the title to query.
      * @param serverChoice This correspond to submitter field (eg. faktalink). Can be empty.
      */
 
     private String formURL( String title, String serverChoice ){
 
         int maxRecords = 1;
 
         String baseURL = "http://koncept.dbc.dk/~fvs/webservice.bibliotek.dk/";
 
         String preTitle = "?version=1.1&operation=searchRetrieve&query=dc.title+%3D+%28%22";
         String postTitle = "%22%29";
 
         String preServerChoice = "+and+cql.serverChoice+%3D+%28";
         String postServerChoice = "%29";
 
         String preRecords = "&startRecord=1&maximumRecords=";
         String postRecords = "&recordSchema=dc&stylesheet=default.xsl&recordPacking=string";
 
         String queryURL;
        if( serverChoice == "" ){
             queryURL = baseURL + preTitle + title + postTitle +
                 preRecords + maxRecords + postRecords;
         }
         else{
             queryURL = baseURL + preTitle + title + postTitle +
                 preServerChoice + serverChoice + postServerChoice + preRecords + maxRecords + postRecords;
         }
         return queryURL;
     }
 
 
     /**
      *  Performs a http call and returns the answer.
      *
      *  @param URLstr The URL to use for hhtp call.
      *
      *  @returns String containing the response.
      *
      *  @throws IOException if we got a connection error.
      */
 
     private String httpGet( String URLstr ) throws IOException
     {
         URL url = new URL( URLstr );
         HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 
         if (conn.getResponseCode() != 200)
             {
                 throw new IOException(conn.getResponseMessage());
             }
 
         // Buffer the result into a string
         BufferedReader rd = new BufferedReader( new InputStreamReader( conn.getInputStream() ) );
         StringBuilder sb = new StringBuilder();
         String line;
         while ( ( line = rd.readLine() ) != null )
             {
                 sb.append( line );
             }
 
         rd.close();
 
         conn.disconnect();
         return sb.toString();
     }
 
     public PluginType getTaskName()
     {
         return pluginType;
     }
 
 }

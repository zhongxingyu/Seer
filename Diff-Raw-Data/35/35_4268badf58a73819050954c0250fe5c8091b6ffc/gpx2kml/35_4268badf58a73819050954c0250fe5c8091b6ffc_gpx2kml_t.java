 /**
  * 
  */
 package de.jjprojects.gpx2kml;
 
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Stack;
 import java.util.logging.Logger;
 
 import org.jdom.*;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.DefaultHandler;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 /**
  This java application takes an XMLTrackFile (GPX Format) and converts it into an KML file.
  
  @author Copyright (C) 2012  JJ-Projects Joerg Juenger <BR>
   
 <pre>
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  </pre>
  */
 public class gpx2kml extends DefaultHandler
 {
   static final String GPXTag        = "gpx";
   static final String TrackTag      = "trk";
   static final String NameTag       = "name";
   static final String TimeTag       = "time";
   static final String TrackpointTag = "trkpt";
   
  static final String KMLFolderTag  = "Folder";
  static final String KMLPlaceTag   = "Placemark";
  static final String KMLPointTag   = "Point";
   static final String KMLCoordsTag  = "coordinates";
   static final String KMLOpenTag    = "open";
  static final String KMLDocTag     = "Document";
   static final String KMLDescTag    = "description";
  static final String KMLStyleTag   = "Style";
  static final String KMLStyleURLTag = "styleUrl";
   
   static final String KMLTag        = "kml";
   
   static final String KMLIconName = "pointIcon";
   static String routeFileName;
   
   static Logger log = Logger.getLogger("JJProjects");
   
   
   public gpx2kml () {
      super();
      trkNodeStack = null;
      kmlRoot = null;
      xmlNS = Namespace.getNamespace("http://www.opengis.net/kml/2.2");
   }
 
   public static void main( String[] argv ) {
      
 
     if( argv.length != 2 )
     {
       System.err.println( "Usage:" );
       System.err.println( "java gpx2kml <XmlTrackFile>  <KmlFile>");
       System.err.println( "Example:" );
       System.err.println( "java gpx2kml mytrack.gpx track.kml" );
       System.exit( 1 );
     }
 
     try {
        XMLReader xr = XMLReaderFactory.createXMLReader();
        gpx2kml handler = new gpx2kml();
        xr.setContentHandler(handler);
        xr.setErrorHandler(handler);
        
        routeFileName = argv[1];
        FileReader r = new FileReader (argv[0]);
        
        xr.parse(new InputSource(r));
 
       
    } catch (SAXException  sxe ) {
       Exception e = ( sxe.getException() != null ) ? sxe.getException() : sxe;
       e.printStackTrace();
    } catch (IOException ioe) {
       ioe.printStackTrace();
    } finally {
    }
 } // End of main function
   
   ////////////////////////////////////////////////////////////////////
   // Event handlers.
   ////////////////////////////////////////////////////////////////////
 
 
   public void startDocument ()
   {
      log.info("Start document");
      
      kmlNodeStack = new Stack<Element>();
      trkNodeStack = new Stack<String> ();
      pointCount = 0;
   }
 
 
   public void endDocument ()
   {
      log.info("End document");
       try {
          Document doc = new Document(kmlRoot);
          // serialize it onto System.out
          XMLOutputter serializer = new XMLOutputter();
 
          Format format = Format.getPrettyFormat();
          // use two space indent
          format.setIndent("  ");
          format.setLineSeparator ("\r\n"); 
          serializer.setFormat (format); 
          
          FileOutputStream outFile = new FileOutputStream (new File (routeFileName));
          serializer.output(doc, outFile);
          
       } catch (IOException e) {
          System.err.println(e);
       }
   }
 
 
   public void startElement (String uri, String name,
                 String qName, Attributes atts)
   {
      String nodeName;
      if ("".equals (uri)) {
         log.info("Start element: " + qName);
         nodeName = qName;
      } else {
         log.info("Start element: {" + uri + "}" + name);
         nodeName = name;
      }
      
      // handle the gpx tag
      if (GPXTag == nodeName) {
         kmlRoot = this.buildRootNode ();
         kmlNodeStack.push(kmlRoot);
         Element kmlDoc = new Element (KMLDocTag, xmlNS);
         
         // add Document Name
         Element docName = docNameElement();
         kmlDoc.addContent (docName);
 
         Element iconStyle = iconStyleElement();
         kmlDoc.addContent (iconStyle);
 
         // add open and description
         Element kmlOpen = new Element (KMLOpenTag, xmlNS);
         kmlOpen.setText("1");
         kmlDoc.addContent (kmlOpen);
         Element kmlDesc = this.createdDescriptionElement ();
         kmlDoc.addContent (kmlDesc);
 
         kmlNodeStack.push(kmlDoc);
 
      }
      
      // handle the track node
      if (TrackTag == nodeName) {
         log.info("Start element: " + atts.getLocalName(0) + ", " + atts.getType(0) + ", " + atts.getValue(0));
         log.info ("Start element: " + atts.getLocalName(1) + ", " + atts.getType(1) + ", " + atts.getValue(1) );
         
         //initiate an new route now; 
         Element rteElement = new Element (KMLFolderTag, xmlNS);
         Element kmlOpen = new Element (KMLOpenTag, xmlNS);
         kmlOpen.setText("1");
         rteElement.addContent (kmlOpen);
         kmlNodeStack.push(rteElement);
      }
      
      // handle track points
      if (TrackpointTag == nodeName) {
         log.info("Start element: " + atts.getLocalName(0) + ", " + atts.getType(0) + ", " + atts.getValue(0));
         log.info ("Start element: " + atts.getLocalName(1) + ", " + atts.getType(1) + ", " + atts.getValue(1) );
         pointCount++;
         
         // add new route waypoint here;
         Element kmlElement = new Element (KMLPlaceTag, xmlNS);
         // add the name to the placemark
         Element ele = new Element(NameTag, xmlNS);
        ele.setText("P" + pointCount);
         kmlElement.addContent (ele);
         // add the styleURL to the placemark
         ele = new Element(KMLStyleURLTag, xmlNS);
         ele.setText("#" + KMLIconName);
         kmlElement.addContent (ele);
 
         Element kmlChild =new Element (KMLCoordsTag, xmlNS);
         Collection<String> coll = new ArrayList<String>();
         coll.add(atts.getValue(1) + ",");
         coll.add(atts.getValue(0) + ",");
         coll.add("0");
         kmlChild.addContent(coll);
         Element kmlPoint = new Element (KMLPointTag, xmlNS);
         kmlPoint.addContent (kmlChild);
         kmlElement.addContent (kmlPoint);
         
         kmlNodeStack.push(kmlElement);
      }
      
      trkNodeStack.push(nodeName);
      log.info("stack: " + trkNodeStack); 
   }
 
   public void endElement (String uri, String name, String qName)
   {
     String nodeName;
      if ("".equals (uri)) {
         log.info("End element: " + qName);
         nodeName = qName;
      } else {
         log.info("End element:   {" + uri + "}" + name);
         nodeName = name;
      }
      
      if (GPXTag == nodeName || TrackTag == nodeName || TrackpointTag == nodeName) {
         Element child = kmlNodeStack.pop ();
         if (! kmlNodeStack.isEmpty ())
            kmlNodeStack.peek().addContent(child);
         else if (child != kmlRoot)
            kmlRoot.addContent (child);
      }
      trkNodeStack.pop();
   }
 
 
   public void characters (char ch[], int start, int length)
   {
       String str = "";
       log.finest("Characters:    \"");
       for (int i = start; i < start + length; i++) {
          switch (ch[i]) {
             case '\\':
                log.finest("\\\\");
                break;
             case '"':
                log.finest("\\\"");
                break;
             case '\n':
                log.finest("\\n");
                break;
             case '\r':
                log.finest("\\r");
                break;
             case '\t':
                log.finest("\\t");
                break;
             default:
                str = str.concat(Character.toString(ch[i]));
                break;
          }
       }
       
       log.finest(str + "\"\n");
       
       log.info("Stack Element: " + trkNodeStack.peek() + " == " + str);
       // set the name in the route element
       if (NameTag == trkNodeStack.peek() && null != kmlNodeStack.peek()) {
          Element nameEle = new Element(NameTag, xmlNS);
          nameEle.setText(str);
          kmlNodeStack.peek().addContent(nameEle);
       }
   }
 
   private Element buildRootNode () {
      Element root =  new Element (KMLTag, xmlNS);
 
      return root;
   }
   
   private Element createdDescriptionElement () {
      Element timeEle = new Element (KMLDescTag, xmlNS);
      Calendar cal = Calendar.getInstance();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd' 'hh:mm:ss' '");
      timeEle.setText("Created at " + sdf.format(cal.getTime()));
 
      return timeEle;
   };
   
   private Element docNameElement () {
      Element timeEle = new Element (NameTag, xmlNS);
      Calendar cal = Calendar.getInstance();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
      timeEle.setText("KML " + sdf.format(cal.getTime()));
 
      return timeEle;
   };
   
   private Element iconStyleElement () {
      /*
      <Style id="randomColorIcon">
      <IconStyle>
              <color>ff00ff00</color>
              <colorMode>random</colorMode>
              <scale>1.1</scale>
              <Icon>
                 <href>http://maps.google.com/mapfiles/kml/pal3/icon21.png</href>
              </Icon>
           </IconStyle>
        </Style>
      */
      Element eleStyle = new Element (KMLStyleTag, xmlNS);
      eleStyle.setAttribute("id", KMLIconName);
      Element iconStyle = new Element ("IconStyle", xmlNS);
      eleStyle.addContent(iconStyle);
      Element ele = new Element ("color", xmlNS);
      ele.setText("ff00ff00");
      iconStyle.addContent(ele);
      ele = new Element("colorMode", xmlNS);
      ele.setText("random");
      iconStyle.addContent(ele);
      ele = new Element("scale", xmlNS);
     ele.setText("0.5");
      iconStyle.addContent(ele);
     ele = new Element("Icon", xmlNS);
      iconStyle.addContent(ele);
      Element hrefEle = new Element("href", xmlNS);
     hrefEle.setText("http://maps.google.com/mapfiles/kml/paddle/ylw-blank-lv.png");
      ele.addContent(hrefEle);
      
      return eleStyle;
      
   };
   
   private Stack<String> trkNodeStack;
   private Stack<Element> kmlNodeStack;
   private Element kmlRoot;
   private Namespace xmlNS;
   private int pointCount;
 }  // End of Class Body
   
 /*
       <LookAt>
         <longitude>11.0510015</longitude>
         <latitude>47.4392637</latitude>
         <altitude>0</altitude>
         <heading>-148.4122922628044</heading>
         <tilt>40.5575073395506</tilt>
         <range>500.6566641072245</range>
       </LookAt>
 */

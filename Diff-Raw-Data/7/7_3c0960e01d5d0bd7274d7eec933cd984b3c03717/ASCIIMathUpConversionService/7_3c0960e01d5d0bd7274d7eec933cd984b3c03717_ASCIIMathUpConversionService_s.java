 /* $Id:FullLaTeXInputDemoServlet.java 158 2008-07-31 10:48:14Z davemckain $
  *
  * Copyright (c) 2011, The University of Edinburgh.
  * All Rights Reserved
  */
 package uk.ac.ed.ph.mathplayground;
 
 import uk.ac.ed.ph.snuggletex.internal.util.IOUtilities;
 import uk.ac.ed.ph.snuggletex.upconversion.MathMLUpConverter;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.json.simple.JSONObject;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
  * FIXME: Document this type!
  * 
  * @author  David McKain
  * @version $Revision:158 $
  */
 public final class ASCIIMathUpConversionService extends BaseServlet {
     
     private static final long serialVersionUID = 2261754980279697343L;
     
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws IOException {
         /* Read MathML created by ASCIIMathML */
         String asciiMathML = IOUtilities.readCharacterStream(request.getReader());
         doService(response, asciiMathML);
     }
     
     @SuppressWarnings("unchecked")
     private void doService(HttpServletResponse response, String asciiMathML) throws IOException {
         /* Do up-conversion */
         MathMLUpConverter upConverter = new MathMLUpConverter(getStylesheetManager());
         Document upConvertedMathDocument = upConverter.upConvertASCIIMathML(asciiMathML, getUpConversionOptions());
         Element mathElement = upConvertedMathDocument.getDocumentElement(); /* NB: Document is <math/> here */
         
         /* Create and send JSON Object encapsulating result */
         JSONObject jsonObject = new JSONObject();
         jsonObject.put("asciiMathML", asciiMathML);
         jsonObject.putAll(unwrapMathMLElement(mathElement));
         sendJSONResponse(response, jsonObject);
      }
 }

 /* $Id:FullLaTeXInputDemoServlet.java 158 2008-07-31 10:48:14Z davemckain $
  *
  * Copyright (c) 2011, The University of Edinburgh.
  * All Rights Reserved
  */
 package uk.ac.ed.ph.mathplayground;
 
 import uk.ac.ed.ph.snuggletex.DOMOutputOptions;
 import uk.ac.ed.ph.snuggletex.InputError;
 import uk.ac.ed.ph.snuggletex.SnuggleSimpleMathRunner;
 import uk.ac.ed.ph.snuggletex.internal.util.IOUtilities;
 import uk.ac.ed.ph.snuggletex.upconversion.UpConvertingPostProcessor;
 import uk.ac.ed.ph.snuggletex.utilities.MessageFormatter;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.w3c.dom.Element;
 
 /**
  * FIXME: Document this type!
  * 
  * @author  David McKain
  * @version $Revision:158 $
  */
 public final class SnuggleTeXUpConversionService extends BaseServlet {
     
     private static final long serialVersionUID = 2261754980279697343L;
     
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws IOException {
         /* Read input LaTeX */
        request.setCharacterEncoding("UTF-8");
         String inputLaTeX = IOUtilities.readCharacterStream(request.getReader());
         doService(response, inputLaTeX);
     }
     
     @SuppressWarnings("unchecked")
     private void doService(HttpServletResponse response, String inputMathModeLaTeX) throws IOException {
         /* Create JSON Object encapsulating result */
         JSONObject jsonObject = new JSONObject();
         jsonObject.put("input", inputMathModeLaTeX);
         
         /* Set up SnuggleTeX output options, including up-conversion */
         DOMOutputOptions domOptions = new DOMOutputOptions();
         domOptions.addDOMPostProcessors(new UpConvertingPostProcessor(getUpConversionOptions()));
         
         /* Run SnuggleTeX on input */
         SnuggleSimpleMathRunner runner = getSnuggleEngine().createSimpleMathRunner();
         Element mathElement = runner.doMathInput(inputMathModeLaTeX, domOptions);
         if (mathElement!=null) {
             /* Unwrap parallel MathML */
             jsonObject.putAll(unwrapMathMLElement(mathElement));
         }
         else {
             /* Parse/building errors */
             JSONArray errorArray = new JSONArray();
             for (InputError error : runner.getLastErrors()) {
                 errorArray.add(MessageFormatter.formatErrorAsString(error));
             }
             jsonObject.put("errors", errorArray);
         }
         sendJSONResponse(response, jsonObject);
     }
 }

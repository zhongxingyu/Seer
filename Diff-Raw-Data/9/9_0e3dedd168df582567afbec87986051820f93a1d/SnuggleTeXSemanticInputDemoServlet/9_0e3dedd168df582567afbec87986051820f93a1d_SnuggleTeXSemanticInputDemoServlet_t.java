 /* $Id:FullLaTeXInputDemoServlet.java 158 2008-07-31 10:48:14Z davemckain $
  *
  * Copyright (c) 2011, The University of Edinburgh.
  * All Rights Reserved
  */
 package uk.ac.ed.ph.mathplayground;
 
 import uk.ac.ed.ph.snuggletex.DOMOutputOptions;
 import uk.ac.ed.ph.snuggletex.InputError;
 import uk.ac.ed.ph.snuggletex.SnuggleSimpleMathRunner;
 import uk.ac.ed.ph.snuggletex.upconversion.UpConvertingPostProcessor;
 import uk.ac.ed.ph.snuggletex.utilities.MessageFormatter;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Element;
 
 /**
  * Version of {@link ASCIIMathSemanticInputDemoServlet} that uses SnuggleTeX input instead.
  * 
  * @author  David McKain
  * @version $Revision:158 $
  */
 public final class SnuggleTeXSemanticInputDemoServlet extends BaseServlet {
     
     private static final long serialVersionUID = 2261754980279697343L;
 
     /** Logger so that we can log what users are trying out to allow us to improve things */
     private static Logger logger = LoggerFactory.getLogger(SnuggleTeXSemanticInputDemoServlet.class);
     
     public static final String DEFAULT_INPUT = "2(x-1)";
     
     /** Generates initial input form with some demo JavaScript. */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
             IOException {
         request.setAttribute("latexMathInput", DEFAULT_INPUT);
         request.getRequestDispatcher("/WEB-INF/jsp/views/snuggletex-semantic-input-demo.jsp").forward(request, response);
     }
     
     /** Handles the posted raw input & PMathML extracted from ASCIIMathML. */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         request.setCharacterEncoding("UTF-8");
         String latexMathInput = request.getParameter("latexMathInput");
         if (latexMathInput==null) {
             logger.warn("No latexInput parameter present");
             response.sendError(HttpServletResponse.SC_BAD_REQUEST);
             return;
         }
         logger.info("LaTeX Math Input: {}", latexMathInput);
         request.setAttribute("latexMathInput", latexMathInput);
         
         /* Set up SnuggleTeX output options, including up-conversion */
         DOMOutputOptions domOptions = new DOMOutputOptions();
         domOptions.addDOMPostProcessors(new UpConvertingPostProcessor(getUpConversionOptions()));
         
         /* Run SnuggleTeX on input */
         SnuggleSimpleMathRunner runner = getSnuggleEngine().createSimpleMathRunner();
         Element mathElement = runner.doMathInput(latexMathInput, domOptions);
         if (mathElement!=null) {
             /* Successful parse, so unwrap MathML */
             LinkedHashMap<String, String> unwrappedMathML = unwrapMathMLElement(mathElement);
             logger.info("Final parallel MathML: {}", unwrappedMathML.get("pmathParallel"));
             request.setAttribute("pmathParallel", unwrappedMathML.get("pmathParallel"));
            request.setAttribute("pmathSemantic", unwrappedMathML.get("pmathSemantic"));
             request.setAttribute("cmath", unwrappedMathML.get("cmath"));
             request.setAttribute("maxima", unwrappedMathML.get("maxima"));
         }
         else {
             /* Failed to parse or build DOM */
             logger.info("Resulting errors: {}", runner.getLastErrors());
             List<String> errors = new ArrayList<String>();
             for (InputError error : runner.getLastErrors()) {
                 errors.add(MessageFormatter.formatErrorAsString(error));
             }
             request.setAttribute("errors", errors);
         }
         request.getRequestDispatcher("/WEB-INF/jsp/views/snuggletex-semantic-input-demo-result.jsp").forward(request, response);
     }
 }

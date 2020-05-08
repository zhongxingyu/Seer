 /*
     XSLTDebugger.java
 
     @author: <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
     Date: March 6, 2001
 
  */
 
 package xslt.debugger.saxon;
 
 
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.Templates;
 import javax.xml.transform.Source;
 
 import java.util.Stack;
 import java.util.ArrayList;
 import java.lang.Runnable;
 import java.io.FileOutputStream;
 import java.io.File;
 
 import xslt.debugger.Manager;
 import xslt.debugger.AbstractXSLTDebugger;
 
 import com.icl.saxon.trace.TraceListener;
 import com.icl.saxon.style.StyleElement;
 import com.icl.saxon.StyleSheet;
 import com.icl.saxon.ParameterSet;
 import com.icl.saxon.FeatureKeys;
 import com.icl.saxon.ExtendedInputSource;
 
 public class XSLTDebugger extends AbstractXSLTDebugger
 {
   public XSLTDebugger() {}
 
   public synchronized void run()
   {
     state = RUNNING;
     notifyAll();
 
     try {
       TransformerFactory tFactory = TransformerFactory.newInstance();
 
       TraceListener traceListener = new SaxonTraceListener(this);
       tFactory.setAttribute(FeatureKeys.TRACE_LISTENER, traceListener);
       tFactory.setAttribute(FeatureKeys.LINE_NUMBERING, Boolean.TRUE);
 
       File inFile = new File(xmlFilename);
       StreamSource in = new StreamSource(xmlFilename);
 
       FileOutputStream outStream = null;
       StreamResult result;
 
       if (outFilename != null) {
         outStream = new FileOutputStream(outFilename);
         result = new StreamResult(outStream);
       }
       else
         result = new StreamResult(System.out);
 
       String media = null, title = null, charset = null;
 
       Source stylesheet
         = tFactory.getAssociatedStylesheet(in, media, title, charset);
       String stylesheetId = stylesheet.getSystemId();
 
       Transformer transformer = tFactory.newTransformer(stylesheet);
       if (transformer != null)
         transformer.transform(in, result);
 
       if (outStream != null)
         outStream.close();
      manager.getObserver().processorFinished();
     }
     catch(Exception e) {
      manager.getObserver().caughtException(e);
     }
 
     state = NOT_RUNNING;
     notifyAll();
   }
 
   /**
    * Return the list of global variables.
    *
    * @return an <code>ArrayList</code> value
    */
   public ArrayList getGlobalVariables()
   {
     // With Saxon we obtain the list of global variables by asking a
     // SaxonStyleFrame object for it. We need to find a StyleElement
     // in the style frames stack to be able to ask for global
     // variables.
     Stack styleFrames = manager.getStyleFrames();
     SaxonStyleFrame styleFrame = (SaxonStyleFrame)styleFrames.get(0);
     if (styleFrame.getElement() instanceof StyleElement)
       return styleFrame.getGlobalVariables();
     else
       return null;
   }
 }

 package com.globant.katari.console.view;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.Validate;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.AbstractController;
 
 import com.globant.katari.console.application.ScriptingEngine;
 
 /** Handles the execution of code sent by the console's client code.
  * @author juan.pereyra@globant.com
  */
 public class ScriptExecutionController extends AbstractController {
 
   /** Request parameter key for the code to be executed. */
  private final static String SCRIPT_CODE_REQ_PARAM = "script";
 
   /** Content type to be used in the response. */
  private final static String JSON_CONTENT_TYPE = "application/json";
 
   /** Scripting engine used to execute the code. */
   private ScriptingEngine scriptingEngine;
 
   /** Builds a new instance of the controller.
    * @param theScriptingEngine Scripting engine used to execute the code. It
    * can't be null.
    */
   public ScriptExecutionController(final ScriptingEngine theScriptingEngine) {
     Validate.notNull(theScriptingEngine,
         "The the scripting engine cannot be null.");
     scriptingEngine = theScriptingEngine;
   }
 
   @Override
   protected ModelAndView handleRequestInternal(final HttpServletRequest
       request, final HttpServletResponse response) throws IOException {
 
     String code = request.getParameter(SCRIPT_CODE_REQ_PARAM);
 
     ByteArrayOutputStream output = new ByteArrayOutputStream();
     ByteArrayOutputStream error = new ByteArrayOutputStream();
 
     scriptingEngine.execute(code, output, error);
 
    String result = "{\"output\":\"" + escapeForJson(output.toString()) +
        "\",\"error\":\"" + escapeForJson(error.toString()) + "\"}";
 
     response.setContentType(JSON_CONTENT_TYPE);
     response.getWriter().print(result);
 
     response.flushBuffer();
 
     return null;
   }
 
  /** Escapes non-JSON characters. Inspired by <a
    * href="http://code.google.com/p/json-simple/">JSON-Simple</a>.
    * @param input The text to be escaped. It can be null or empty, in
    * both cases the result will be an empty string.
    * @return An escaped version of the input string. It can return an empty
    * string if the input is either null or empty.
    */
   private String escapeForJson(final String input) {
     if (null == input || StringUtils.isEmpty(input)) {
       return "";
     }
 
     StringBuffer output = new StringBuffer();
     for (int i = 0; i < input.length(); i++) {
       char ch = input.charAt(i);
       switch(ch) {
       case '"':
         output.append("\\\"");
         break;
       case '\\':
         output.append("\\\\");
         break;
       case '\b':
         output.append("\\b");
         break;
       case '\f':
         output.append("\\f");
         break;
       case '\n':
         output.append("\\n");
         break;
       case '\r':
         output.append("\\r");
         break;
       case '\t':
         output.append("\\t");
         break;
       case '/':
         output.append("\\/");
         break;
       default:
         // Reference: http://www.unicode.org/versions/Unicode5.1.0/
         boolean isUnicodeChar = (ch >= '\u0000' && ch <= '\u001F')
           || (ch >= '\u007F' && ch <= '\u009F')
           || (ch >= '\u2000' && ch <= '\u20FF');
 
         if (isUnicodeChar) {
           String ss = Integer.toHexString(ch);
           output.append("\\u");
           for (int k = 0; k < 4 - ss.length(); k++) {
             output.append('0');
           }
           output.append(ss.toUpperCase());
         } else {
           output.append(ch);
         }
       }
     }
     return output.toString();
   }
 }
 

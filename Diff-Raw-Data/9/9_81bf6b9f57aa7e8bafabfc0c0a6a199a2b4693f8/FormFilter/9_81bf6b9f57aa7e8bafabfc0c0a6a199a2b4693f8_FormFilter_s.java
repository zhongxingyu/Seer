 package com.psddev.dari.util;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 
 import javax.servlet.FilterChain;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpServletResponseWrapper;
 
 /**
  * Filter that processes forms written by {@link FormTag}.
  *
  * @see FormTag
  *
  */
 public class FormFilter extends AbstractFilter {
 
     private static final String ATTRIBUTE_PREFIX = FormFilter.class.getName() + ".";
 
     private static final String IS_FORM_SUCCESSFUL_ATTRIBUTE =          ATTRIBUTE_PREFIX + "isFormSuccessful";
     private static final String FORM_RESULT_ATTRIBUTE =                 ATTRIBUTE_PREFIX + "formResult";
     private static final String FORM_ERROR_ATTRIBUTE =                  ATTRIBUTE_PREFIX + "formError";
     private static final String BUFFERED_RESPONSE_ATTRIBUTE =           ATTRIBUTE_PREFIX + "bufferedResponse";
     private static final String SUBMITTED_FORM_PROCESSOR_ATTRIBUTE =    ATTRIBUTE_PREFIX + "submittedFormProcessor";
 
     @Override
     protected void doRequest(
             final HttpServletRequest request,
             HttpServletResponse response,
             FilterChain chain)
             throws IOException, ServletException {
 
         FormProcessor2 processor = FormTag.Static.getProcessorById(request.getParameter(FormTag.ID_PARAMETER), request);
 
         if (processor != null) {
             request.setAttribute(SUBMITTED_FORM_PROCESSOR_ATTRIBUTE, processor);
             try {
                 BufferedResponse buffered = new BufferedResponse(response);
                 request.setAttribute(BUFFERED_RESPONSE_ATTRIBUTE, buffered);
 
                 Object result = processor.process(request, buffered);
 
                 request.setAttribute(IS_FORM_SUCCESSFUL_ATTRIBUTE, Boolean.TRUE);
                 request.setAttribute(FORM_RESULT_ATTRIBUTE, result);
 
                 if (JspUtils.isFinished(request, JspUtils.getHeaderResponse(request, response))) {
                     return;
                 }
 
             } catch (IOException e) {
                 throw e;
 
             } catch (Throwable throwable) {
                throwable.printStackTrace(); // TODO: Remove debug statement

                 request.setAttribute(IS_FORM_SUCCESSFUL_ATTRIBUTE, Boolean.FALSE);
                 request.setAttribute(FORM_ERROR_ATTRIBUTE, throwable);
             }
         }
 
         chain.doFilter(request, response);
     }
 
     /** Response that buffers the output from the underlying writer for reading
      *  later. */
     private static class BufferedResponse extends HttpServletResponseWrapper {
 
         private PrintWriter printWriter;
         private StringWriter stringWriter;
 
         public BufferedResponse(HttpServletResponse response) {
             super(response);
             stringWriter = new StringWriter();
             printWriter = new PrintWriter(stringWriter);
         }
 
         @Override
         public PrintWriter getWriter() {
             return printWriter;
         }
 
         public String getResponseString() {
             return stringWriter.toString();
         }
     }
 
     public static final class Static {
 
         /**
          * Returns true if the processor argument was used to process the form
          * that was submitted on the request.
          *
          * @param processor
          * @param request
          * @return
          */
         public static boolean isFormSubmitted(FormProcessor2 processor, HttpServletRequest request) {
             return processor != null && processor == request.getAttribute(SUBMITTED_FORM_PROCESSOR_ATTRIBUTE);
         }
 
         /**
          * Returns true if the form submitted on this request was successfully
          * processed, or false if there were errors processing the form. If
          * true, the result can be retrieved via a call to
          * {@linkplain #getFormResult(HttpServletRequest)}. If false, the
          * errors can be retrieved via a call to
          * {@linkplain #getFormError(HttpServletRequest)}.
          * <p>
          * If no form was processed on this request, then null is returned.
          *
          * @param request
          * @return
          */
         public static Boolean isFormSuccess(HttpServletRequest request) {
             return (Boolean) request.getAttribute(IS_FORM_SUCCESSFUL_ATTRIBUTE);
         }
 
         /**
          * Returns the result for the form submitted on the request or null if
          * the form had errors or no form was processed.
          *
          * @param request
          * @return
          */
         public static Object getFormResult(HttpServletRequest request) {
             return request.getAttribute(FORM_RESULT_ATTRIBUTE);
         }
 
         /**
          * Returns the errors for the form submitted on the request or null if
          * the form had no errors or no form was processed.
          *
          * @param request
          * @return
          */
         public static Object getFormError(HttpServletRequest request) {
             return request.getAttribute(FORM_ERROR_ATTRIBUTE);
         }
 
         /**
          * Returns the buffered output for the form with ID {@code formId} or
          * null if none exists.
          *
          * @param formId
          * @param request
          * @return
          */
         protected static String getBufferedOutput(String formId, HttpServletRequest request) {
 
             if (formId.equals(request.getParameter(FormTag.ID_PARAMETER))) {
                 BufferedResponse buffered = (BufferedResponse) request.getAttribute(BUFFERED_RESPONSE_ATTRIBUTE);
 
                 if (buffered != null) {
                     return buffered.getResponseString();
                 }
             }
 
             return null;
         }
     }
 }

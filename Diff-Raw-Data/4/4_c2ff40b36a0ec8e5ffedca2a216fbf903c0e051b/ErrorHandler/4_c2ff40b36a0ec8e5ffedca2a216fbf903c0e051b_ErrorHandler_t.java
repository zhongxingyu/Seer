 package org.iplantc.core.uicommons.client;
 
 import java.util.Date;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.uicommons.client.views.dialogs.ErrorDialog;
 
 import com.extjs.gxt.ui.client.GXT;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.json.client.JSONObject;
 
 /**
  * Provides a uniform manner for posting errors to the user.
  * 
  * @author amuir
  * 
  */
 public class ErrorHandler {
     private static final String NEWLINE = "\n"; //$NON-NLS-1$
 
     /**
      * Post a message box with error styles with the argument error message.
      * 
      * @param error the string message to include in the displayed dialog
      */
     public static void post(String error) {
         post(error, null);
     }
 
     /**
      * Post a message box with error styles with a general error message summary and the given caught
      * with additional error details.
      * 
      * @param caught
      */
     public static void post(Throwable caught) {
         post(I18N.ERROR.error(), caught);
     }
 
     /**
      * Post a message box with error styles with the given error message summary and optional caught with
      * additional error details.
      * 
      * @param errorSummary
      * @param caught
      */
     public static void post(String errorSummary, Throwable caught) {
         String errorDetails = getSystemDescription();
 
         if (caught != null) {
             GWT.log(errorSummary, caught);
 
             errorDetails = parseExceptionJson(caught) + NEWLINE + NEWLINE + errorDetails;
         }
 
         ErrorDialog ed = new ErrorDialog(errorSummary, errorDetails);
         ed.show();
     }
 
     private static String parseExceptionJson(Throwable caught) {
         String exceptionMessage = caught.getMessage();
 
         JSONObject jsonError = null;
         try {
             jsonError = JsonUtil.getObject(exceptionMessage);
         } catch (Exception ignoreParseErrors) {
             // intentionally ignore JSON parse errors
         }
 
         if (jsonError != null) {
             String name = JsonUtil.getString(jsonError, "name"); //$NON-NLS-1$
             String message = JsonUtil.getString(jsonError, "message"); //$NON-NLS-1$
 
             if (!message.isEmpty() || !name.isEmpty()) {
                 exceptionMessage = I18N.ERROR.errorReport(name, message);
             }
         }
 
         return exceptionMessage;
     }
 
     /**
      * Builds a string with details about the GXT user agent and version, and GWT version.
      * 
      * @return A system description string.
      */
     private static String getSystemDescription() {
         String gwtVersion = I18N.DISPLAY.gwtVersion() + " " + GWT.getVersion(); //$NON-NLS-1$
 
         String gxtVersion = I18N.DISPLAY.gxtVersion() + " " + I18N.DISPLAY.majorVersion() //$NON-NLS-1$
                 + ": " + GXT.getVersion().getMajor() + " " + I18N.DISPLAY.minorVersion() + " " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                 + GXT.getVersion().getMajor();
 
         String userAgent = I18N.DISPLAY.userAgent() + " " + GXT.getUserAgent(); //$NON-NLS-1$
 
         String date = I18N.DISPLAY.date() + ": " + new Date().toString(); //$NON-NLS-1$
 
        String host = I18N.DISPLAY.host() + ":" + GWT.getHostPageBaseURL();

        return gwtVersion + NEWLINE + gxtVersion + NEWLINE + userAgent + NEWLINE + date + NEWLINE + host;
     }
 }

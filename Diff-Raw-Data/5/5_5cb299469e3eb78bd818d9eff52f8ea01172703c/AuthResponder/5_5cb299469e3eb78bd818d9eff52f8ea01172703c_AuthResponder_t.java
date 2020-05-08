 package no.knubo.accounting.client.misc;
 
 import java.util.ArrayList;
 
 import no.knubo.accounting.client.AccountingGWT;
 import no.knubo.accounting.client.Constants;
 import no.knubo.accounting.client.I18NAccount;
 import no.knubo.accounting.client.Util;
 
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONParser;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.user.client.Window;
 
 public class AuthResponder implements RequestCallback {
 
     private final Constants constants;
     private final ServerResponse callback;
     private final I18NAccount messages;
 
     private AuthResponder(Constants constants, I18NAccount messages, ServerResponse callback) {
         this.constants = constants;
         this.messages = messages;
         this.callback = callback;
        if(callback == null) {
            throw new RuntimeException("Callback cannot be null");
        }
         AccountingGWT.setLoading();
 
     }
 
     public void onError(Request request, Throwable exception) {
         /* Not needed? */
     }
 
     public void onResponseReceived(Request request, Response response) {
         AccountingGWT.setDoneLoading();
         if (response.getStatusCode() == 510) {
             Util.forward(constants.loginURL());
         } else if (response.getStatusCode() == 511) {
             Window.alert(messages.no_access());
         } else if (response.getStatusCode() == 512) {
             Window.alert("DB error:" + response.getText());
         } else if (response.getStatusCode() == 513) {
             JSONValue parse = JSONParser.parse(response.getText());
 
             ArrayList fields = new ArrayList();
             JSONArray array = parse.isArray();
 
             for (int i = 0; i < array.size(); i++) {
                 fields.add(Util.str(array.get(i)));
             }
 
             if (callback instanceof ServerResponseWithValidation) {
                 ((ServerResponseWithValidation) callback).validationError(fields);
             } else {
                 Window.alert("Validation error:" + fields);
             }
         } else {
             String data = response.getText();
             if (data == null || data.length() == 0) {
                 Window.alert(messages.no_server_response());
                 return;
             }
             data = data.trim();
 
             JSONValue jsonValue = null;
 
             try {
                 jsonValue = JSONParser.parse(data);
             } catch (Exception e) {
                 /* BAD, but cought below */
             }
 
             if (jsonValue == null) {
                 if (callback instanceof ServerResponseWithErrorFeedback) {
                     ((ServerResponseWithErrorFeedback) callback).onError();
                 } else {
                     Window.alert("Bad return data:" + data);
                 }
             } else {
                 callback.serverResponse(jsonValue);
             }
         }
     }
 
     public static void get(Constants constants, I18NAccount messages, ServerResponse callback,
             String url) {
         RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, constants.baseurl() + url);
 
         try {
             builder.sendRequest("", new AuthResponder(constants, messages, callback));
         } catch (RequestException e) {
             Window.alert("Failed to send the request: " + e.getMessage());
         }
     }
 
     public static void post(Constants constants, I18NAccount messages, ServerResponse callback,
             StringBuffer parameters, String url) {
 
         RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, constants.baseurl() + url);
 
         try {
             builder.setHeader("Content-Type", "application/x-www-form-urlencoded");
             builder.sendRequest(parameters.toString(), new AuthResponder(constants, messages,
                     callback));
         } catch (RequestException e) {
             Window.alert("Failed to send the request: " + e.getMessage());
         }
 
     }
 }

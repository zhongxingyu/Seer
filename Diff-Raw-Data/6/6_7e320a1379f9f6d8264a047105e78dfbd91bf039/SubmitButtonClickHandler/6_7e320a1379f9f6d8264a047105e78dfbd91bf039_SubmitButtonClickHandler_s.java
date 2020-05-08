 package com.google.gwt.sample.stockwatcher.modules.stockreader.client.handlers;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.http.client.URL;
 import com.google.gwt.sample.stockwatcher.modules.stockreader.client.callbacks.JSONRequestCallback;
 import com.google.gwt.sample.stockwatcher.modules.stockreader.client.model.StockData;
 import com.google.gwt.sample.stockwatcher.modules.stockreader.client.widgets.CurrenciesTextField;
 import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
 import com.sencha.gxt.widget.core.client.info.Info;
 
 /**
  * Submit button click handler.
  */
 public class SubmitButtonClickHandler implements ClickHandler {
     private static final String JSON_URL = GWT.getHostPageBaseURL() + "stockPricesJSON";
     private CurrenciesTextField currenciesTextField;
 
     public SubmitButtonClickHandler(CurrenciesTextField currenciesTextField) {
         this.currenciesTextField = currenciesTextField;
     }
 
     @Override
     public void onClick(ClickEvent event) {
         StringBuilder urlBuilder = new StringBuilder(JSON_URL);
        if (!currenciesTextField.getValue().isEmpty()) {
            urlBuilder.append("?q=")
                    .append(currenciesTextField.getValue().replace(' ', '+'));
        }
 
         String url = URL.encode(urlBuilder.toString());
 
         // Send request to server and catch any errors.
         RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
 
         try {
             builder.sendRequest(null, new JSONRequestCallback());
         } catch (RequestException e) {
             AlertMessageBox msgBox = new AlertMessageBox("Request Error", e.getMessage());
             msgBox.show();
         }
     }
 }

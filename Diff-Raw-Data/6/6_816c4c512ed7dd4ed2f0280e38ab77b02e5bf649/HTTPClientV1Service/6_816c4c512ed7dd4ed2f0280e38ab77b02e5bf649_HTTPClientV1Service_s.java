 /*
  * The MtGox-Java API is free software: you can redistribute it and/or modify
  * it under the terms of the Lesser GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * The MtGox-Java API is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * Lesser GNU General Public License for more details.
  *
  * You should have received a copy of the Lesser GNU General Public License
  * along with the MtGox-Java API .  If not, see <http://www.gnu.org/licenses/>.
  */
 package to.sparks.mtgox.service;
 
 import java.io.IOException;
 import java.net.URL;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.util.Currency;
 import java.util.HashMap;
 import java.util.logging.Logger;
 import to.sparks.mtgox.model.*;
 import to.sparks.mtgox.net.HTTPAuthenticator;
 import to.sparks.mtgox.net.JSONSource;
 
 /**
  * A simple implementation of a client for the MtGox HTTP API version 1.
  *
  * @author SparksG
  */
 class HTTPClientV1Service extends HTTPAuthenticator {
 
     private JSONSource<Result<AccountInfo>> privateInfoJSON;
     private JSONSource<Result<Order[]>> openOrdersJSON;
     private JSONSource<Result<String>> stringJSON;
     private JSONSource<Result<OrderResult>> orderResultJSON;
     private JSONSource<Result<FullDepth>> fullDepthJSON;
     private JSONSource<Result<Ticker>> tickerJSON;
     private JSONSource<Result<CurrencyInfo>> currencyInfoJSON;
     private JSONSource<Result<SendBitcoinsTransaction>> sendBitcoinsJSON;
     private JSONSource<Result<Lag>> lagJSON;
 
     public HTTPClientV1Service(final Logger logger, String apiKey, String secret) {
         super(logger, apiKey, secret);
         openOrdersJSON = new JSONSource<>();
         stringJSON = new JSONSource<>();
         orderResultJSON = new JSONSource<>();
         fullDepthJSON = new JSONSource<>();
         tickerJSON = new JSONSource<>();
         privateInfoJSON = new JSONSource<>();
         currencyInfoJSON = new JSONSource<>();
         sendBitcoinsJSON = new JSONSource<>();
     }
 
     public FullDepth getFullDepth(Currency currency) throws Exception {
         FullDepth fullDepth = fullDepthJSON.getResultFromStream(new URL(UrlFactory.getUrlForRestCommand(currency, UrlFactory.RestCommand.FullDepth)).openStream(), FullDepth.class).getReturn();
         return fullDepth;
     }
 
     public String placeOrder(Currency currency, HashMap<String, String> params) throws Exception {
         Result<String> result = stringJSON.getResultFromStream(getMtGoxHTTPInputStream(UrlFactory.getUrlForRestCommand(currency, UrlFactory.RestCommand.PrivateOrderAdd), params), String.class);
         if (result.getError() != null) {
             throw new RuntimeException(result.getToken() + ": " + result.getError());
         }
         return result.getReturn();
     }
 
     public OrderResult getPrivateOrderResult(HashMap<String, String> params) throws Exception {
         Result<OrderResult> result = orderResultJSON.getResultFromStream(getMtGoxHTTPInputStream(UrlFactory.getUrlForRestCommand("", UrlFactory.RestCommand.PrivateOrderResult), params), OrderResult.class);
         if (result.getError() != null) {
             throw new RuntimeException(result.getToken() + ": " + result.getError());
         }
         return result.getReturn();
     }
 
     public Order[] getOpenOrders() throws IOException, NoSuchAlgorithmException, InvalidKeyException, Exception {
 
         Result<Order[]> openOrders = openOrdersJSON.getResultFromStream(getMtGoxHTTPInputStream(UrlFactory.getUrlForRestCommand("", UrlFactory.RestCommand.PrivateOrders)), Order[].class);
         return openOrders.getReturn();
     }
 
     public AccountInfo getPrivateInfo() throws IOException, NoSuchAlgorithmException, InvalidKeyException, Exception {
 
         Result<AccountInfo> privateInfo = privateInfoJSON.getResultFromStream(getMtGoxHTTPInputStream(UrlFactory.getUrlForRestCommand("", UrlFactory.RestCommand.PrivateInfo)), AccountInfo.class);
         return privateInfo.getReturn();
     }
 
     public Ticker getTicker(Currency currency) throws IOException, Exception {
         Result<Ticker> tickerUSD = tickerJSON.getResultFromStream(getMtGoxHTTPInputStream(UrlFactory.getUrlForRestCommand(currency, UrlFactory.RestCommand.Ticker)), Ticker.class);
         return tickerUSD.getReturn();
     }
 
     public CurrencyInfo getCurrencyInfo(Currency currency) throws IOException, Exception {
         return getCurrencyInfo(currency.getCurrencyCode());
     }
 
     public CurrencyInfo getCurrencyInfo(String currencyCode) throws IOException, Exception {
         HashMap<String, String> params = new HashMap<>();
         params.put("currency", currencyCode);
         Result<CurrencyInfo> currencyInfo = currencyInfoJSON.getResultFromStream(getMtGoxHTTPInputStream(UrlFactory.getUrlForRestCommand(currencyCode, UrlFactory.RestCommand.CurrencyInfo), params), CurrencyInfo.class);
         if (currencyInfo.getError() != null) {
             throw new RuntimeException(currencyInfo.getToken() + ": " + currencyInfo.getError());
         }
         return currencyInfo.getReturn();
     }
 
     public SendBitcoinsTransaction sendBitcoins(HashMap<String, String> params) throws IOException, Exception {
         Result<SendBitcoinsTransaction> response = sendBitcoinsJSON.getResultFromStream(getMtGoxHTTPInputStream(UrlFactory.getUrlForRestCommand(UrlFactory.RestCommand.SendBitcoins), params), SendBitcoinsTransaction.class);
         if (response.getError() != null) {
             throw new RuntimeException(response.getToken() + ": " + response.getError());
         }
         return response.getReturn();
     }
 
     public Lag getLag() throws IOException, Exception {
        Result<Lag> response = lagJSON.getResultFromStream(getMtGoxHTTPInputStream(UrlFactory.getUrlForRestCommand(UrlFactory.RestCommand.Lag), new HashMap<String, String>()), Lag.class);
         if (response.getError() != null) {
             throw new RuntimeException(response.getToken() + ": " + response.getError());
         }
         return response.getReturn();
     }
 }

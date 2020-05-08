 package es.finnapps.piggybank.bankapi;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.entity.AbstractHttpEntity;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.google.inject.Inject;
 
 import es.finnapps.piggybank.model.Operation;
 import es.finnapps.piggybank.model.Piggy;
 import es.finnapps.piggybank.model.UserInfo;
 import es.finnapps.piggybank.sharedprefs.PiggyBankPreferences;
 
 import android.util.Base64;
 import android.util.Log;
 
 public class BankApi implements BankApiInterface {
     private String TAG = BankApi.class.getName();
 
     private String API_URL = "http://finappsapi.bdigital.org/api/2012/";
     private String API_KEY = "c6ab8d3240";
     private String CREATE_CLIENT_URL = API_URL + API_KEY + "/access/client";
     private String GET_TOKEN_URL = API_URL + API_KEY + "/access/login";
     private String CREATE_ACCOUNT_URL = API_URL + API_KEY + "/" + REPLACE_TOKEN + "/operations/account/@me";
     private String GET_ACCOUNTS_URL = API_URL + API_KEY + "/" + REPLACE_TOKEN + "/operations/account/list";
     private String GET_ACCOUNT_AMOUNT = API_URL + API_KEY + "/" + REPLACE_TOKEN + "/operations/account/"
             + REPLACE_ACCOUNT_ID;
     private String TRANSFER_MONEY_URL = API_URL + API_KEY + "/" + REPLACE_TOKEN + "/operations/account/transfer";
    private String DEPOSIT_URL =  API_URL + API_KEY + "/" + REPLACE_TOKEN + "/operations/account/deposit";
 
     @Inject
     private PiggyBankPreferences prefs;
 
     private static final String REPLACE_TOKEN = "{token}";
     private static final String REPLACE_ACCOUNT_ID = "{id_account}";
     private static final String JSON_TYPE = "application/json";
     private static final String XML_TYPE = "text/xml";
 
     private static final String KEY_USERNAME = "username";
     private static final String KEY_PASSWORD = "password";
     private static final String KEY_FIRSTNAME = "firstName";
     private static final String KEY_LASTNAME = "lastName";
     private static final String KEY_ADDRESS = "address";
     private static final String KEY_STREET = "street";
     private static final String KEY_NUMBER = "number";
     private static final String KEY_CITY = "city";
     private static final String KEY_POSTALCODE = "postalCode";
     private static final String KEY_COUNTRY = "country";
     private static final int BUFFERSIZE = 1024;
     private static final String KEY_AUTHORIZATION = "Authorization";
     private static final String KEY_BASIC = "Basic";
     private static final String KEY_TOKEN = "token";
 
     private static final String OFFICE_KEY = "office";
     private static final String CURRENCY_KEY = "currency";
     private static final String FIX_OFFICE = "508a8989e4b0a7694d240e9b";
     private static final String FIX_CURRENCY = "EURO";
     private static final String KEY_ACCOUNT_NUMBER = "id";
     private static final String KEY_ACCOUNT_LONG_NUMBER = "accountNumber";
     private static final String KEY_DATA = "data";
     private static final String KEY_AMOUNT = "actualBalance";
     private static final String KEY_CONCEPT = "concept";
     private static final String KEY_PAYEE = "payee";
     private static final String KEY_ORIGIN_ACOUNT = "originAccount";
     private static final String KEY_DESTINATION_ACCOUNT = "destinationAccount";
     private static final String KEY_TRANSFER_AMOUNT = "value";
     private static final String KEY_ADITIONAL_DATAS = "additionalData";
     private static final String KEY_DEPOSIT_ACCOUNT = "accountNumber";
 
  

     private String CONTENT = "Content-Type";
     private JSONObject responseJson;
 
     public enum HttpRequestType {
         get, post, put, delete,
     }
 
     protected StringEntity createJSONRequestForRegister(String[] names, Object[] values) {
         try {
             JSONObject json = createJsonFromParams(names, values);
             StringEntity entity = new StringEntity(json.toString());
             entity.setContentType(JSON_TYPE);
             return entity;
         } catch (UnsupportedEncodingException e) {
             Log.d(TAG, "error creating entity from json Encodign", e);
         } catch (JSONException e) {
             Log.d(TAG, "error creating entity from json json", e);
         }
         return null;
     }
 
    protected StringEntity createJSONRequestForTransfer (String[] names, Object[] values) {
         try {
             JSONObject json = createJsonFromParams(names, values);
             StringEntity entity = new StringEntity(json.toString());
             entity.setContentType(JSON_TYPE);
             return entity;
         } catch (UnsupportedEncodingException e) {
             Log.d(TAG, "error creating entity from json Encodign", e);
         } catch (JSONException e) {
             Log.d(TAG, "error creating entity from json json", e);
         }
         return null;
     }
 
     protected StringEntity createNewAccountEntity(String[] names, Object[] values) {
         try {
             JSONObject json = createJsonFromParams(names, values);
             StringEntity entity = new StringEntity(json.toString());
             entity.setContentType(JSON_TYPE);
             return entity;
         } catch (UnsupportedEncodingException e) {
             Log.d(TAG, "error creating entity from json Encodign", e);
         } catch (JSONException e) {
             Log.d(TAG, "error creating entity from json json", e);
         }
         return null;
     }
 
     private JSONObject createJsonFromParams(String[] names, Object[] values) throws JSONException {
         JSONObject json = new JSONObject();
         for (int i = 0; i < values.length; i++) {
             json.put(names[i], values[i]);
         }
         return json;
     }
 
     protected StringEntity createJSONRequest(String[] names, String[] values) {
         try {
             JSONObject json = new JSONObject();
             for (int i = 0; i < values.length; i++) {
                 json.put(names[i], values[i]);
             }
             StringEntity entity = new StringEntity(json.toString());
             entity.setContentType(XML_TYPE);
             return entity;
         } catch (UnsupportedEncodingException e) {
             Log.d(TAG, "error creating entity from json Encodign", e);
         } catch (JSONException e) {
             Log.d(TAG, "error creating entity from json json", e);
         }
         return null;
     }
 
     protected Map<String, String> getBasicHeaders() {
         Map<String, String> headers = new HashMap<String, String>();
         headers.put(CONTENT, JSON_TYPE);
         return headers;
     }
 
     protected Map<String, String> getAuthoritationHeaders() {
         Map<String, String> headers = new HashMap<String, String>();
         headers.put(KEY_AUTHORIZATION, KEY_BASIC + " " + prefs.getUserName() + ":" + prefs.getPassword());
         return headers;
     }
 
     protected HttpResponse callApi(String url, Map<String, String> headers, HttpRequestType type,
             AbstractHttpEntity reqEntity, boolean authorization) {
         Log.d(TAG, "callApi " + url);
 
         HttpClient httpclient = new DefaultHttpClient();
 
         HttpRequestBase request = null;
         if (type == HttpRequestType.post) {
             request = new HttpPost(url);
             if (authorization) {
                 String userPass = prefs.getUserName() + ":" + prefs.getPassword();
                 request.setHeader("Authorization",
                         "Basic " + Base64.encodeToString(userPass.getBytes(), Base64.NO_WRAP));
             }
             if (reqEntity != null) {
                 ((HttpPost) request).setEntity(reqEntity);
             }
         } else if (type == HttpRequestType.get) {
             request = new HttpGet(url);
             if (authorization) {
                 String userPass = prefs.getUserName() + ":" + prefs.getPassword();
                 request.setHeader("Authorization",
                         "Basic " + Base64.encodeToString(userPass.getBytes(), Base64.NO_WRAP));
             }
         } else if (type == HttpRequestType.put) {
             request = new HttpPut(url);
             if (reqEntity != null) {
                 ((HttpPut) request).setEntity(reqEntity);
             }
         } else if (type == HttpRequestType.delete) {
             request = new HttpDelete(url);
         } else {
             assert false;
         }
 
         if (headers != null) {
             for (String key : headers.keySet()) {
                 request.addHeader(key, headers.get(key));
             }
         }
 
         HttpResponse response = null;
 
         try {
             response = httpclient.execute(request);
         } catch (IOException e) {
             Log.d(TAG, "Error requesting url", e);
         }
 
         Log.v(TAG, "Response received " + response.getStatusLine());
 
         int resultado = response.getStatusLine().getStatusCode();
         if (resultado == 400) {
             Log.d(TAG, "error in petition");
         }
         return response;
     }
 
     public JSONObject getResponseInfo(HttpResponse response) {
         HttpEntity entity = response.getEntity();
         try {
             responseJson = getJSONObject(entity);
             Log.i(TAG, responseJson.toString());
         } catch (IOException e) {
             e.printStackTrace();
         } catch (JSONException e) {
             e.printStackTrace();
         }
         return responseJson;
     }
 
     protected JSONObject getJSONObject(HttpEntity entity) throws IOException, JSONException {
         JSONObject json = new JSONObject();
         if (entity != null) {
             InputStream instream = entity.getContent();
             String result = convertStreamToString(instream);
             json = new JSONObject(result);
             System.gc();
             instream.close();
         }
         return json;
     }
 
     private String convertStreamToString(InputStream is) throws IOException {
         Writer writer = new StringWriter();
         char[] buffer = new char[BUFFERSIZE];
         try {
             Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
             int n;
             while ((n = reader.read(buffer)) != -1) {
                 writer.write(buffer, 0, n);
             }
         } finally {
             is.close();
         }
         return writer.toString();
     }
 
     public boolean registerClient(UserInfo userInfo) {
         String[] street = { KEY_STREET, KEY_NUMBER, KEY_CITY, KEY_POSTALCODE, KEY_COUNTRY };
         String[] streetValues = { userInfo.getStreet(), userInfo.getCity(), userInfo.getPostalCode(),
                 userInfo.getCountry() };
         JSONObject streetJson = null;
         try {
             streetJson = createJsonFromParams(street, streetValues);
         } catch (JSONException e) {
             e.printStackTrace();
         }
         String[] names = { KEY_USERNAME, KEY_PASSWORD, KEY_FIRSTNAME, KEY_LASTNAME, KEY_ADDRESS };
         Object[] values = { userInfo.getUserName(), userInfo.getPassword(), userInfo.getFirstName(),
                 userInfo.getLastName(), streetJson };
         StringEntity entity = createJSONRequestForRegister(names, values);
         HttpResponse response = callApi(CREATE_CLIENT_URL, getBasicHeaders(), HttpRequestType.post, entity, false);
 
         if (response.getStatusLine().getStatusCode() == 200) {
             prefs.setUserName(userInfo.getUserName());
             prefs.setPassword(userInfo.getPassword());
            String token = getToken();           
             prefs.setToken(token);
             String firstAccount = createAccount(token);
             depositFunds(token, firstAccount);
             return true;
         } else {
             return false;
         }
     }
 
    public String getAccountNumber (String accountId, String token) {
         String getAccountAmount = GET_ACCOUNT_AMOUNT.replace(REPLACE_ACCOUNT_ID, accountId);
         String getAccountAmountWithToken = getAccountAmount.replace(REPLACE_TOKEN, token);
         HttpResponse response = callApi(getAccountAmountWithToken, null, HttpRequestType.get, null, true);
 
         JSONObject responseJson = null;
         try {
             responseJson = getResponseInfo(response);
         } catch (IllegalStateException e) {
             e.printStackTrace();
         }
 
         try {
             JSONObject data = responseJson.getJSONObject(KEY_DATA);
             String accountNumber = data.getString(KEY_ACCOUNT_LONG_NUMBER);
             return accountNumber;
         } catch (JSONException e) {
             e.printStackTrace();
         }
         return "";
     }
 
     public String createAccount(String token) {
         String createAccountUrl = CREATE_ACCOUNT_URL.replace(REPLACE_TOKEN, token);
         String[] names = { OFFICE_KEY, CURRENCY_KEY };
         String[] values = { FIX_OFFICE, FIX_CURRENCY };
         HttpResponse response = callApi(createAccountUrl, null, HttpRequestType.post,
                 createNewAccountEntity(names, values), false);
 
         JSONObject responseJson = null;
         try {
             responseJson = getResponseInfo(response);
         } catch (IllegalStateException e) {
             e.printStackTrace();
         }
 
         try {
             JSONObject data = responseJson.getJSONObject(KEY_DATA);
             String accountNumber = data.getString(KEY_ACCOUNT_NUMBER);
             return accountNumber;
         } catch (JSONException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         return null;
     }
 
     public List<Operation> getOperations(String account) {
         // TODO Auto-generated method stub
         return null;
     }
 
     public long getAccountAmount(String account, String token) {
         String getAccountAmount = GET_ACCOUNT_AMOUNT.replace(REPLACE_ACCOUNT_ID, account);
         String getAccountAmountWithToken = getAccountAmount.replace(REPLACE_TOKEN, token);
         HttpResponse response = callApi(getAccountAmountWithToken, null, HttpRequestType.get, null, true);
 
         JSONObject responseJson = null;
         try {
             responseJson = getResponseInfo(response);
         } catch (IllegalStateException e) {
             e.printStackTrace();
         }
 
         try {
             JSONObject data = responseJson.getJSONObject(KEY_DATA);
             long accountNumber = data.getLong(KEY_AMOUNT);
             return accountNumber;
         } catch (JSONException e) {
             e.printStackTrace();
         }
         return 0;
     }
 
     public List<Piggy> getAccounts(String token) {
         String getAccounts = GET_ACCOUNTS_URL.replace(REPLACE_TOKEN, token);
         HttpResponse response = callApi(getAccounts, null, HttpRequestType.get, null, false);
 
         JSONObject responseJson = null;
         try {
             responseJson = getResponseInfo(response);
         } catch (IllegalStateException e) {
             e.printStackTrace();
         }
 
         try {
             JSONArray data = responseJson.getJSONArray(KEY_DATA);
             for (int x = 0; x < data.length(); x++) {
                 String currentAccountId = data.getString(x);
                 long currentAmount = getAccountAmount(currentAccountId, token);
                Piggy newPiggy = new Piggy("asdf", currentAccountId, currentAmount, null, null, Piggy.PIGGY_TYPE_SHARED, null);
                 Log.d(TAG, "current account: " + currentAccountId);
                 Log.d(TAG, "current amount: " + newPiggy.getAmount());
             }
         } catch (JSONException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     public String getAccountStatus(String account) {
         // TODO Auto-generated method stub
         return null;
     }
 
    public boolean transferFunds(String fromAccountId, String toAccountId, String token, String concept, String userNumber, float amount) {
         String[] accountNames = { KEY_CONCEPT, KEY_PAYEE };
        String[] accountValues = { concept, userNumber};
         JSONObject adicionalData = null;
         try {
             adicionalData = createJsonFromParams(accountNames, accountValues);
         } catch (JSONException e) {
             e.printStackTrace();
         }
         String[] names = { KEY_ORIGIN_ACOUNT, KEY_DESTINATION_ACCOUNT, KEY_TRANSFER_AMOUNT, KEY_ADITIONAL_DATAS };
        Object[] values = { getAccountNumber(fromAccountId, token), getAccountNumber(toAccountId, token), amount, adicionalData };
         StringEntity entity = createJSONRequestForRegister(names, values);
         String transferUrl = TRANSFER_MONEY_URL.replace(REPLACE_TOKEN, token);
         HttpResponse response = callApi(transferUrl, null, HttpRequestType.post, entity, false);
         if (response.getStatusLine().getStatusCode() == 200) {
             JSONObject responseJson = null;
             try {
                 responseJson = getResponseInfo(response);
                 Log.d(TAG, responseJson.toString());
             } catch (IllegalStateException e) {
                 e.printStackTrace();
             }
             return true;
         } else {
             responseJson = getResponseInfo(response);
             Log.d(TAG, responseJson.toString());
             return false;
         }
     }
 
     public boolean depositFunds(String token, String account) {
         String[] accountNames = { KEY_CONCEPT, KEY_PAYEE };
        String[] accountValues = { "initial commit", "dios del capital"};
         JSONObject adicionalData = null;
         try {
             adicionalData = createJsonFromParams(accountNames, accountValues);
         } catch (JSONException e) {
             e.printStackTrace();
         }
         String[] names = { KEY_DEPOSIT_ACCOUNT, KEY_TRANSFER_AMOUNT, KEY_ADITIONAL_DATAS };
         Object[] values = { getAccountNumber(account, token), 1234.50, adicionalData };
         StringEntity entity = createJSONRequestForRegister(names, values);
         String depositFundsUrl = DEPOSIT_URL.replace(REPLACE_TOKEN, token);
         HttpResponse response = callApi(depositFundsUrl, null, HttpRequestType.post, entity, false);
         if (response.getStatusLine().getStatusCode() == 200) {
             JSONObject responseJson = null;
             try {
                 responseJson = getResponseInfo(response);
                 Log.d(TAG, responseJson.toString());
             } catch (IllegalStateException e) {
                 e.printStackTrace();
             }
             return true;
         } else {
             return false;
         }
     }
 
     public String getToken() {
         HttpResponse response = callApi(GET_TOKEN_URL, null, HttpRequestType.get, null, true);
 
         JSONObject responseJson = null;
         try {
             responseJson = getResponseInfo(response);
         } catch (IllegalStateException e) {
             e.printStackTrace();
         }
 
         try {
             String token = responseJson.getString(KEY_TOKEN);
             return token;
         } catch (JSONException e) {
             e.printStackTrace();
         }
         return null;
     }
 
 }

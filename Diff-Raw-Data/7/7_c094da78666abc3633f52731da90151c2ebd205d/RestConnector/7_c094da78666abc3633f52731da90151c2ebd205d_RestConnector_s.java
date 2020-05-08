 package jp.co.nttcom.eai.webadmin.client;
 
 import java.io.BufferedReader;
 import java.io.Closeable;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Collections;
 import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
 import net.arnx.jsonic.util.Base64;
 
 /**
  * <p>[概 要] RestConnectorクラス。</p>
  * <p>[詳 細] RESTサーバと接続して、JSON形式のレスポンスを取得します。</p>
  * <p>[備 考] </p>
  * <p>[環 境] JavaSE 6.0</p>
  *
  * @author Seiji Sogabe
  */
 public class RestConnector {
 
     /**
      * コネクションタイムアウト(ms)。
      */
     private static final int CONNECTION_TIMEOUT = 8000;
 
     private RestConnector() {
         //
     }
 
     public static String doGet(String url) throws IOException {
         return doGet(url, Collections.EMPTY_MAP);
     }
 
     /**
      * <p>[概 要] GET処理</p>
      * <p>[詳 細] クエリーパラメータを指定してURLにGETメソッドで接続し、レスポンスを返します。 リトライ処理は行いません。
      * </p>
      * <p>
      * ステータスコードがOK(200)以外の場合や、タイムアウトが発生した場合に、 例外をスローします。
      * </p>
      *
      * @param url 接続先URL
      * @param params クエリーパラメータ
      * @return レスポンス
      * @throws IOException 入出力エラー
      */
     public static String doGet(String url, Map<String, String> params) throws IOException {
         URL u = createURL(url, params);
 
         String res;
         HttpURLConnection conn = null;
         try {
             conn = (HttpURLConnection) u.openConnection();
             conn.setDoOutput(true);
             conn.setUseCaches(false);
             conn.setConnectTimeout(CONNECTION_TIMEOUT);
             conn.setReadTimeout(CONNECTION_TIMEOUT);
             conn.setRequestProperty("Authorization", 
                         "Basic " + createBase64text("sogabe:sogabe"));            
             res = getResponse(conn);
         } finally {
             if (conn != null) {
                 conn.disconnect();
             }
         }
 
         return res.toString();
     }
 
     /**
     * <p>[概 要] PUT処理</p>
     * <p>[詳 細] クエリーパラメータを指定してURLにPUTメソッドで接続し、レスポンスを返します。 リトライ処理は行いません。
      * </p>
      * <p>
      * ステータスコードがOK(200)以外の場合や、タイムアウトが発生した場合に、 例外をスローします。
      * </p>
      *
      * @param url 接続先URL
      * @param params クエリーパラメータ
      * @return レスポンス
      * @throws IOException 入出力エラー
      */
     public static void doPost(String url, Map<String, String> params) throws IOException {
         doPost(url, params, Collections.EMPTY_MAP);
     }
 
     /**
      * <p>[概 要] PUT処理</p>
      * <p>[詳 細] クエリーパラメータを指定してURLにPUTメソッドで接続し、レスポンスを返します。 リトライ処理は行いません。
      * </p>
      * <p>
      * ステータスコードがOK(200)以外の場合や、タイムアウトが発生した場合に、 例外をスローします。
      * </p>
      *
      * @param url 接続先URL
      * @param params クエリーパラメータ
      * @param data 送信するデータ
      * @return レスポンス
      * @throws IOException 入出力エラー
      */
     public static void doPost(String url, Map<String, String> params, Map<String, String> data) throws IOException {
         URL u = createURL(url, params);
 
         QueryBuilder sendQuery = new QueryBuilder();
         sendQuery.putAll(data);
 
         String res;
         HttpURLConnection conn = null;
         OutputStreamWriter writer = null;
         try {
             conn = (HttpURLConnection) u.openConnection();
             conn.setDoOutput(true);
             conn.setRequestMethod("POST");
             conn.setUseCaches(false);
             conn.setConnectTimeout(CONNECTION_TIMEOUT);
             conn.setReadTimeout(CONNECTION_TIMEOUT);
             conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
             conn.setRequestProperty("Authorization", 
                         "Basic " + createBase64text("sogabe:sogabe"));            
             writer = new OutputStreamWriter(conn.getOutputStream());
             writer.write(sendQuery.build());
             writer.flush();
             res = getResponse(conn);
         } finally {
             try {
                 close(writer);
             } finally {
                 if (conn != null) {
                     conn.disconnect();
                 }
             }
         }
     }
 
     private static String getResponse(HttpURLConnection conn) throws IOException {
         StringBuilder res = new StringBuilder();
         InputStreamReader isr = null;
         BufferedReader reader = null;
         try {
             isr = new InputStreamReader(conn.getInputStream(), "UTF-8");
             reader = new BufferedReader(isr);
 
             int code = conn.getResponseCode();
             if (isResponseOK(code)) {
                 throw new IOException("接続に失敗しました。(レスポンスコード: " + code + ")");
             }
 
             String line;
             while (null != (line = reader.readLine())) {
                 res.append(line);
             }
         } finally {
             try {
                 close(isr);
             } finally {
                 close(reader);
             }
         }
         return res.toString();
     }
 
     private static boolean isResponseOK(int code) throws IOException {
         return code == HttpURLConnection.HTTP_NOT_FOUND
                 || code == HttpURLConnection.HTTP_BAD_REQUEST
                 || code == HttpURLConnection.HTTP_INTERNAL_ERROR;
     }
 
     private static void close(Closeable obj) throws IOException {
         if (obj != null) {
             obj.close();
         }
     }
     
     private static String createBase64text(String str) {
         String base64text = "";
         try {
             base64text = Base64.encode(str.getBytes("UTF-8"));
         } catch (UnsupportedEncodingException ex) {
             //
         }
         return base64text;
     }
 
     private static URL createURL(String url, Map<String, String> params) throws IOException {
         QueryBuilder query = new QueryBuilder();
         query.putAll(params);
         URL u;
         try {
             u = new URL(url + '?' + query.build());
         } catch (MalformedURLException e) {
             throw new IOException("failed to create url", e);
         }
         return u;
     }
 }

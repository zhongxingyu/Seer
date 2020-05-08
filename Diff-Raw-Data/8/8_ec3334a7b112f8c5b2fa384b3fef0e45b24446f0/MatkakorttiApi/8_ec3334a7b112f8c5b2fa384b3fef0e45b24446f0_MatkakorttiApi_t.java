 package fi.iki.dezgeg.matkakorttiwidget;
 
 import com.gistlabs.mechanize.MechanizeAgent;
 import com.gistlabs.mechanize.document.Document;
 import com.gistlabs.mechanize.document.html.HtmlDocument;
 import com.gistlabs.mechanize.document.html.HtmlElement;
 import com.gistlabs.mechanize.document.html.form.Form;
 import com.gistlabs.mechanize.document.html.form.SubmitButton;
 
 import org.apache.http.HttpVersion;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.impl.client.AbstractHttpClient;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.HTTP;
 import org.json.JSONArray;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.security.KeyStore;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 
 import static com.gistlabs.mechanize.document.html.query.HtmlQueryBuilder.byId;
 import static com.gistlabs.mechanize.document.html.query.HtmlQueryBuilder.byTag;
 
 public class MatkakorttiApi
 {
     private static class NonverifyingSSLSocketFactory extends SSLSocketFactory
     {
         SSLContext sslContext = SSLContext.getInstance("TLS");
 
         public NonverifyingSSLSocketFactory(KeyStore truststore) throws Exception
         {
             super(truststore);
 
             TrustManager tm = new X509TrustManager() {
                 public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
                 {
                 }
 
                 public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
                 {
                 }
 
                 public X509Certificate[] getAcceptedIssuers()
                 {
                     return null;
                 }
 
             };
             sslContext.init(null, new TrustManager[] { tm }, null);
         }
 
         @Override
         public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException,
                 UnknownHostException
         {
             return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
         }
 
         @Override
         public Socket createSocket() throws IOException
         {
             return sslContext.getSocketFactory().createSocket();
         }
     }
 
     private static AbstractHttpClient createNonverifyingHttpClient() throws Exception
     {
 
         KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
         trustStore.load(null, null);
 
         SSLSocketFactory sf = new NonverifyingSSLSocketFactory(trustStore);
         sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
 
         HttpParams params = new BasicHttpParams();
         HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
         HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
 
         SchemeRegistry registry = new SchemeRegistry();
         registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
         registry.register(new Scheme("https", sf, 443));
 
         ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
 
         return new DefaultHttpClient(ccm, params);
     }
 
     public static double getMoney(String username, String password) throws Exception
     {
         AbstractHttpClient httpClient = createNonverifyingHttpClient();
 
         MechanizeAgent agent = new MechanizeAgent(httpClient);
         Document page = agent.get("https://omamatkakortti.hsl.fi/Login.aspx");
 
         Form loginForm = page.forms().get(byId("aspnetForm"));
         String prefix = "Etuile$MainContent$LoginControl$LoginForm$";
         loginForm.get(prefix + "UserName").set(username);
         loginForm.get(prefix + "Password").set(password);
 
         HtmlDocument loginResponse = loginForm.submit((SubmitButton) loginForm.get(prefix + "LoginButton"));
         HtmlElement validationSummary = loginResponse.htmlElements().get(byId("Etuile_mainValidationSummary"));
 
         if (validationSummary != null) {
             List<HtmlElement> errorElements = validationSummary.get(byTag("ul")).getAll(byTag("li"));
             String errors = "";
             // - 1 since the service will always complain about our browser.
             for (int i = 0; i < errorElements.size() - 1; i++)
                 errors += errorElements.get(i).getText() + "\n";
            throw new MatkakorttiException(errors);
         }
 
         // TODO: Follow redirects
         HtmlDocument response = agent.get("https://omamatkakortti.hsl.fi/Basic/Cards.aspx");
         List<HtmlElement> scripts = response.htmlElements().getAll(byTag("script"));
 
         Pattern jsonPattern = Pattern.compile(".*?parseJSON\\('(.*)'\\).*", Pattern.DOTALL);
 
         for (HtmlElement script : scripts) {
             Matcher matcher = jsonPattern.matcher(script.getInnerHtml());
             if (matcher.matches()) {
                 JSONArray cards = new JSONArray(matcher.group(1));
                 if (cards.length() == 0)
                    throw new MatkakorttiException("Tunnuksella ei ole matkakortteja.");
                 return cards.getJSONObject(0).getDouble("RemainingMoney");
             }
         }
        throw new MatkakorttiException("No voi vittu");
     }
 }

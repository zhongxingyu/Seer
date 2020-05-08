 package net.phedny.valuemanager.data.account;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.phedny.valuemanager.data.Account;
 import net.phedny.valuemanager.data.AccountRetriever;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.params.ClientPNames;
 import org.apache.http.client.params.CookiePolicy;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.cookie.BasicClientCookie;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 
 public class MoneYouRetriever implements AccountRetriever {
 
 	private static final Pattern INPUT_LINE = Pattern
 			.compile(".*<INPUT type=\"hidden\" value=\"([^\"]*)\" id=\"[^\"]*\" name=\"([^\"]*)\">.*");
 
 	private static final Pattern LINKTO_LINE = Pattern
 			.compile(".*<TD[^>]*><A HREF=\"javascript:linkTo\\(01,([0-9]*)\\)\"><SPAN[^>]*>Spaarrekening</SPAN></A></TD>.*");
 
 	private static final Pattern ACCOUNT_LINE = Pattern
 			.compile(".*<tr[^>]*><TD[^>]*><INPUT[^>]*></TD><TD[^>]*><A HREF=\"javascript:linkTo\\(([0-9]*),13\\)\">([0-9.]*)</A></TD><TD[^>]*>.*</TD><TD[^>]*>[^<]*</TD><TD[^>]*>[^<]*</TD><TD[^>]*>.*</TD><TD[^>]*>([0-9.,]*) *([A-Z]*)</TD></TR>.*");
 
 	private final String username;
 
 	private final String password;
 
 	private Map<String, Account> accounts;
 
 	public MoneYouRetriever(String username, String password) {
 		this.username = username;
 		this.password = password;
 	}
 
 	@Override
	public Account getAccount(String accontId) {
 		if (accounts == null) {
 			return null;
 		}
		return accounts.get(accontId);
 	}
 
 	@Override
 	public String[] getAccountIds() {
 		if (accounts == null) {
 			return null;
 		}
 		Set<String> keySet = accounts.keySet();
 		return keySet.toArray(new String[keySet.size()]);
 	}
 
 	@Override
 	public void retrieve() {
 		accounts = new HashMap<String, Account>();
 		DefaultHttpClient httpClient = new DefaultHttpClient();
 		httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
 		InputStream contentStream = null;
 		try {
 			HttpContext context = new BasicHttpContext();
 			HttpGet get = new HttpGet("https://sparen.moneyou.nl/exp/jsp/authenticationNL.jsp");
 
 			HttpResponse response = httpClient.execute(get, context);
 			if (response.getStatusLine().getStatusCode() != 200) {
 				get.abort();
 				return;
 			}
 
 			get.abort();
 
 			HttpPost post = new HttpPost("https://sparen.moneyou.nl/exp/jsp/authenticationNL.jsp");
 			List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
 			params.add(new BasicNameValuePair("first_pass_done", "Y"));
 			params.add(new BasicNameValuePair("id", username));
 			params.add(new BasicNameValuePair("password", password));
 			params.add(new BasicNameValuePair("save_abt", "on"));
 			params.add(new BasicNameValuePair("Subm01", "Inloggen >"));
 			params.add(new BasicNameValuePair("mltsoc", "1"));
 			post.setEntity(new UrlEncodedFormEntity(params));
 
 			BasicClientCookie nomabtCookie = new BasicClientCookie("nomabt", username);
 			nomabtCookie.setVersion(0);
 			nomabtCookie.setDomain("sparen.moneyou.nl");
 			nomabtCookie.setPath("/exp/jsp");
 			nomabtCookie.setSecure(true);
 			httpClient.getCookieStore().addCookie(nomabtCookie);
 
 			BasicClientCookie loginabtCookie = new BasicClientCookie("loginabt", username);
 			loginabtCookie.setVersion(0);
 			loginabtCookie.setDomain("sparen.moneyou.nl");
 			loginabtCookie.setPath("/exp/jsp");
 			httpClient.getCookieStore().addCookie(loginabtCookie);
 
 			response = httpClient.execute(post, context);
 			if (response.getStatusLine().getStatusCode() != 200) {
 				post.abort();
 				return;
 			}
 
 			post.abort();
 
 			get = new HttpGet("https://sparen.moneyou.nl/exp/jsp/entrypoint.jsp?state=000001");
 			response = httpClient.execute(get, context);
 			if (response.getStatusLine().getStatusCode() != 200) {
 				get.abort();
 				return;
 			}
 
 			HttpEntity entity = response.getEntity();
 			contentStream = entity.getContent();
 			InputStreamReader isReader = new InputStreamReader(contentStream, "UTF-8");
 			BufferedReader reader = new BufferedReader(isReader);
 
 			String line;
 			String subm99 = null;
 			params = new ArrayList<BasicNameValuePair>();
 			while ((line = reader.readLine()) != null) {
 				Matcher m = INPUT_LINE.matcher(line);
 				if (m.matches()) {
 					params.add(new BasicNameValuePair(m.group(2), m.group(1)));
 				}
 
 				m = LINKTO_LINE.matcher(line);
 				if (m.matches()) {
 					subm99 = m.group(1);
 				}
 			}
 			params.add(new BasicNameValuePair("graph", "09"));
 			params.add(new BasicNameValuePair("Subm99", subm99));
 			contentStream.close();
 			contentStream = null;
 
 			post = new HttpPost("https://sparen.moneyou.nl/exp/jsp/entrypoint.jsp");
 			post.setEntity(new UrlEncodedFormEntity(params));
 			response = httpClient.execute(post, context);
 			if (response.getStatusLine().getStatusCode() != 200) {
 				post.abort();
 				return;
 			}
 
 			entity = response.getEntity();
 			contentStream = entity.getContent();
 			isReader = new InputStreamReader(contentStream, "UTF-8");
 			reader = new BufferedReader(isReader);
 
 			while ((line = reader.readLine()) != null) {
 				Matcher m = ACCOUNT_LINE.matcher(line);
 				if (m.matches()) {
 					Locale dutchLocale = new Locale("nl", "NL");
 					NumberFormat numberParser = NumberFormat.getNumberInstance(dutchLocale);
 					final String accountName = m.group(2).replaceAll("\\.", "");
 					final String accountId = "net.phedny.valuemanager.sepa.NL47ABNA0000000000".substring(0,
 							47 - accountName.length());
 					Account account = new SimpleAccount(accountId + accountName, accountName, m.group(4), numberParser
 							.parse(m.group(3)));
 					accounts.put(accountName, account);
 				}
 			}
 			contentStream.close();
 			contentStream = null;
 
 			get = new HttpGet("https://sparen.moneyou.nl/exp/jsp/entrypoint.jsp?service=BWY2&state=000001");
 			response = httpClient.execute(get, context);
 			get.abort();
 
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ParseException e) {
 			e.printStackTrace();
 		} finally {
 			if (contentStream != null) {
 				try {
 					contentStream.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 }

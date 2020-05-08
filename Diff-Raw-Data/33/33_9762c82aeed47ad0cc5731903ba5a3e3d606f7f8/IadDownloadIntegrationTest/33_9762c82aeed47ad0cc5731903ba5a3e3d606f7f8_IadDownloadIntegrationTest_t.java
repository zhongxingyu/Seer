 package com.apple.aid;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.http.HeaderElement;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.HttpVersion;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.utils.URIBuilder;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.PoolingClientConnectionManager;
 import org.apache.http.impl.conn.SchemeRegistryFactory;
 import org.apache.http.message.BasicHeaderElementIterator;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.CoreProtocolPNames;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.select.Elements;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.apple.iad.api.DownloadIAdReport;
 import com.apple.iad.beans.ReportRequestBean;
 import com.apple.iad.domain.IAdReportByNameStory;
 import com.apple.iad.ingestion.IAdReportInfoDirector;
 import com.apple.iad.ingestion.builder.IAdReportByNameBuilder;
 
 /**
  * 
  * @author Georgi Lambov
  * 
  */
 public class IadDownloadIntegrationTest {
 
 	private static final Properties prop = new Properties();
 	private DefaultHttpClient httpclient;
 	private HttpContext localContext;
 
 	@BeforeClass
 	public static void init() throws IOException {
 		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("iad.properties");
 		prop.load(in);
 		in.close();
 	}
 
 	@Before
 	public void initMethod() {
 		httpclient = new DefaultHttpClient();
 		localContext = new BasicHttpContext();
 	}
 
 	@Test
 	public void propertiesFileLoaded() {
 		Assert.assertNotNull(prop);
 		Assert.assertNotNull(prop.get("iad.username"));
 		Assert.assertNotNull(prop.get("iad.password"));
 	}
 
 	@Test
 	public void simpleGetRequest() throws URISyntaxException {
 		URIBuilder builder = new URIBuilder();
 		builder.setScheme("http").setHost("www.google.com").setPath("/search").setParameter("q", "httpclient").setParameter("btnG", "Google Search")
 				.setParameter("aq", "f").setParameter("oq", "");
 		URI uri = builder.build();
 		HttpGet httpget = new HttpGet(uri);
 
 		Assert.assertEquals("http://www.google.com/search?q=httpclient&btnG=Google+Search&aq=f&oq=", httpget.getURI().toString());
 	}
 
 	@Test
 	public void downoadIadReportRaw() throws ClientProtocolException, IOException, URISyntaxException {
 		URIBuilder cookieBuilder = new URIBuilder();
 
 		PoolingClientConnectionManager cxMgr = new PoolingClientConnectionManager(SchemeRegistryFactory.createDefault());
 		cxMgr.setMaxTotal(3);
 		cxMgr.setDefaultMaxPerRoute(20);
 		HttpClient client = new DefaultHttpClient(cxMgr);
 
 		// first request retrieve post url
 		URIBuilder builder = new URIBuilder();
 		builder.setScheme("https").setHost("itunesconnect.apple.com").setPath("/");
 		URI uri = builder.build();
 
 		HttpGet httpget = new HttpGet(uri);
 		httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
 		httpclient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");
 		httpget.getParams().setParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, Boolean.TRUE);
 		httpclient.getParams().setParameter(CoreProtocolPNames.USER_AGENT,
 				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.7; rv:16.0) Gecko/20100101 Firefox/16.0");
 		httpclient.getParams().setParameter(CoreProtocolPNames.WAIT_FOR_CONTINUE, Boolean.TRUE);
 
 		HttpResponse response = client.execute(httpget, localContext);
 		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
 
 		HttpEntity entity = response.getEntity();
 		InputStream instream = entity.getContent();
 		Document loginPageContent;
 		try {
 			loginPageContent = Jsoup.parse(IOUtils.toString(instream, "UTF-8"));
 		} finally {
 			instream.close();
 		}
 
 		Elements loginForm = loginPageContent.select("div#ds_container > form");
 		String loginFormAction = loginForm.attr("action");
 
 		Assert.assertNotNull(loginFormAction); // some kind of /WebObjects/iTunesConnect.woa/wo/0.1.9.3.5.2.1.1.3.1.1
 
 		// next request - security checks
 		builder.setScheme("http").setHost("metrics.apple.com").setPath("/b/ss/appleitmsna/1/H.24--NS/0");
 		uri = builder.build();
 		httpget.setURI(uri);
 		response = client.execute(httpget, localContext);
 
 		BasicHeaderElementIterator its = new BasicHeaderElementIterator(response.headerIterator("Set-Cookie"));
 		while (its.hasNext()) {
 			HeaderElement elem = its.nextElement();
 			if (elem.getName().equals("s_vi")) {
 				cookieBuilder.setParameter("s_vi", elem.getValue());
 				break;
 			}
 		}
 
 		// next request - login to itunes connect
 		builder.setScheme("https").setHost("itunesconnect.apple.com").setPath(loginFormAction);
 		uri = builder.build();
 
 		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
 		formparams.add(new BasicNameValuePair("theAccountName", prop.getProperty("iad.username")));
 		formparams.add(new BasicNameValuePair("theAccountPW", prop.getProperty("iad.password")));
 		formparams.add(new BasicNameValuePair("1.Continue.x", "0"));
 		formparams.add(new BasicNameValuePair("1.Continue.y", "0"));
 		formparams.add(new BasicNameValuePair("theAuxValue", ""));
 
 		UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
 
 		HttpPost httpPost = new HttpPost(uri);
 		httpPost.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.7; rv:15.0) Gecko/20100101 Firefox/15.0.1'");
 		httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
 
 		httpPost.setEntity(formEntity);
 		response = httpclient.execute(httpPost, localContext);
 
 		BasicHeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator("Set-Cookie"));
 		int neededElements = 2;
 		int findedElements = 0;
 
 		while (it.hasNext()) {
 			if (neededElements == findedElements) {
 				break;
 			}
 
 			HeaderElement elem = it.nextElement();
 
 			if (elem.getName().equals("myacinfo")) {
 				cookieBuilder.setParameter("myacinfo", elem.getValue());
 				findedElements++;
 			}
 
 			if (elem.getName().equals("ds01")) {
 				cookieBuilder.setParameter("ds01", elem.getValue());
 				findedElements++;
 			}
 		}
 
 		Assert.assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, response.getStatusLine().getStatusCode());
 
 		// retrieve iad report
 		builder = new URIBuilder();
 		builder.setScheme("https").setHost("iad.apple.com").setPath("/itcportal/generatecsv").setParameter("pageName", "app_homepage")
 				.setParameter("dashboardType", "publisher").setParameter("publisherId", prop.getProperty("iad.publisher"))
 				.setParameter("dateRange", "fourteenDays").setParameter("searchTerms", "Search%20Apps").setParameter("adminFlag", "false")
 				.setParameter("fromDate", "04/04/13").setParameter("toDate", "").setParameter("dataType", "byName");
 
 		uri = builder.build();
 		httpget = new HttpGet(uri);
 
 		cookieBuilder.setParameter("s_cc", "true");
 		cookieBuilder
 				.setParameter("s_sq",
 						"pplesuperglobal%3D%2526pid%253DiTC%252520Home%2526pidt%253D1%2526oid%253Dhttps%25253A%25252F%25252Fiad.apple.com%25252Fitcportal%2526ot%253DA");
 
 		httpget.setHeader("Cookie", cookieBuilder.toString().replace("&", "; ").replaceFirst("\\?", ""));
 
 		response = client.execute(httpget, localContext);
 		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
 
 		entity = response.getEntity();
 		instream = entity.getContent();
 
 		String content;
 		try {
 			content = IOUtils.toString(instream, "UTF-8");
 		} finally {
 			instream.close();
 		}
 
 		Assert.assertTrue(content.length() != 0);
 	}
 
 	@Test
 	public void downloReprotWithAbstractionAndReturnInputStream() throws ParseException, IOException {
 		SimpleDateFormat format = new SimpleDateFormat("mm/DD/yy");
 		String d = "04/04/13";
 		Date fromDate = format.parse(d);
 
 		ReportRequestBean requestBean = new ReportRequestBean();
 		requestBean.setFromDate(fromDate);
 		requestBean.setDateRange(ReportRequestBean.DATE_RANGE_FOURTEEN_DAYS);
 		requestBean.setProviderId(Long.valueOf(prop.getProperty("iad.publisher")));
 		requestBean.setUsername(prop.getProperty("iad.username"));
 		requestBean.setPassword(prop.getProperty("iad.password"));
 
 		IAdReportInfoDirector director = new IAdReportInfoDirector();
 		director.setReportBuilder(new IAdReportByNameBuilder());
 		director.constructReport(requestBean);
 
 		Assert.assertNotNull(director.getReportContent());
 	}
 	
 	@Test
 	public void downloadReportUsingApiInterface() {
 		ReportRequestBean requestBean = new ReportRequestBean();
 		requestBean.setFromDate(new Date());
 		requestBean.setDaysGoBack(1);
 		requestBean.setProviderId(Long.valueOf(prop.getProperty("iad.publisher")));
 		requestBean.setUsername(prop.getProperty("iad.username"));
 		requestBean.setPassword(prop.getProperty("iad.password"));
 		
 		DownloadIAdReport downloadIAdReport = new DownloadIAdReport();
 		downloadIAdReport.setReportRequest(requestBean);
 		List<IAdReportByNameStory> fetchDailyReportsByName = downloadIAdReport.fetchDailyReportsByName();
 		
 		Assert.assertNotNull(fetchDailyReportsByName);
		Assert.assertNotNull(fetchDailyReportsByName.iterator().next().getReports().iterator().next());
 	}
 
 }

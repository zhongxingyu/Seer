 package emcshop;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 import java.util.TimeZone;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.lang3.exception.ExceptionUtils;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.ContentType;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 
 import emcshop.util.XmlBuilder;
 
 /**
  * Uploads error reports to my website.
  */
 public class ReportSender {
 	private static final Logger logger = Logger.getLogger(ReportSender.class.getName());
 	private static ReportSender instance;
 
 	private final String url;
 	private Integer dbVersion;
 	private final LinkedBlockingQueue<Job> queue = new LinkedBlockingQueue<Job>();
 
 	/**
 	 * Gets the singleton instance of this class.
 	 * @return the singleton object
 	 */
 	public static synchronized ReportSender instance() {
 		if (instance == null) {
 			instance = new ReportSender("http://mikeangstadt.name/emc-shopkeeper/error-report.php");
 		}
 
 		return instance;
 	}
 
 	/**
 	 * Tests this class and the PHP script.
 	 * @param args
 	 * @throws Throwable
 	 */
 	public static void main(String args[]) throws Throwable {
 		ReportSender rs = ReportSender.instance();
 
 		try {
 			Integer.parseInt("test");
 		} catch (Throwable t) {
 			rs.report(t);
 		}
 
 		System.in.read(); //stop the program from terminating
 	}
 
 	/**
 	 * Creates a new report sender.
 	 * @param url the URL to send the error reports to
 	 */
 	ReportSender(String url) {
 		this.url = url;
 
 		SenderThread t = new SenderThread();
 		t.setDaemon(true);
 		t.start();
 	}
 
 	/**
 	 * Sets the version of the database.
 	 * @param version the database version
 	 */
 	public void setDatabaseVersion(Integer version) {
 		dbVersion = version;
 	}
 
 	/**
 	 * Reports an error.
 	 * @param throwable the error to report
 	 */
 	public void report(Throwable throwable) {
 		Job job = new Job(throwable);
 		queue.add(job);
 	}
 
 	private class SenderThread extends Thread {
 		private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss'Z'");
 		{
 			df.setTimeZone(TimeZone.getTimeZone("GMT"));
 		}
 
 		@Override
 		public void run() {
 			while (true) {
 				//get the next error
 				Job job;
 				try {
 					job = queue.take();
 				} catch (InterruptedException e) {
 					break;
 				}
 
 				try {
 					String body = createRequestBody(job);
 					HttpPost request = new HttpPost(url);
 					request.setEntity(new StringEntity(body, ContentType.TEXT_XML));
 
 					DefaultHttpClient client = new DefaultHttpClient();
 					HttpResponse response = client.execute(request);
 
 					int status = response.getStatusLine().getStatusCode();
 					HttpEntity entity = response.getEntity();
 					String responseBody = (entity == null) ? "" : EntityUtils.toString(entity);
 					if (status != 200 || !responseBody.isEmpty()) {
 						logger.log(Level.WARNING, "Error report not accepted (HTTP " + status + "): " + responseBody);
 					}
 				} catch (Throwable t) {
 					logger.log(Level.WARNING, "Problem sending error report.", t);
 				}
 			}
 		}
 
 		private String createRequestBody(Job job) {
 			XmlBuilder xml = new XmlBuilder("Error");
 
 			String timestamp = df.format(job.received);
 			xml.append(xml.root(), "Timestamp", timestamp);
 
 			xml.append(xml.root(), "Version", Main.VERSION);
 
 			String dbVersionStr = (dbVersion == null) ? "null" : dbVersion.toString();
 			xml.append(xml.root(), "DatabaseVersion", dbVersionStr);
 
 			xml.append(xml.root(), "JavaVersion", System.getProperty("java.version"));
 
 			xml.append(xml.root(), "OS", System.getProperty("os.name"));
 
 			xml.append(xml.root(), "Locale", Locale.getDefault().toString());
 
 			String stackTrace = ExceptionUtils.getStackTrace(job.throwable);
 			xml.append(xml.root(), "StackTrace", stackTrace);
 
 			return xml.toString();
 		}
 	}
 
 	private static class Job {
 		private final Throwable throwable;
 		private final Date received = new Date();
 
 		public Job(Throwable throwable) {
 			this.throwable = throwable;
 		}
 	}
 }

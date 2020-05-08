 package bzb.gae;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.URL;
 import java.net.URLConnection;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.SimpleTimeZone;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletRequest;
 
 public abstract class Utility {
 
 	// http://carsharesms.appspot.com/csdemo
 
 	public static final int REFRESH_PERIOD = 20;
 	private static final Logger log = Logger.getLogger(Utility.class.getName());
 
 	public static String makeGetRequest(String request) {
 		String result = null;
 		try {
 			URL url = new URL(request);
 			URLConnection conn = url.openConnection();
 
 			// Get the response
 			BufferedReader rd = new BufferedReader(new InputStreamReader(conn
 					.getInputStream()));
 			StringBuffer sb = new StringBuffer();
 			String line;
 			while ((line = rd.readLine()) != null) {
 				sb.append(line);
 			}
 			rd.close();
 			result = sb.toString();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return result;
 	}
 
 	public static String sendPost(String target, String data) {
 		String result = null;
 		try {
 			URL url = new URL(target);
 			URLConnection conn = url.openConnection();
 			conn.setDoOutput(true);
 			OutputStreamWriter wr = new OutputStreamWriter(conn
 					.getOutputStream());
 			wr.write(data);
 			wr.flush();
 
 			// Get the response
 			BufferedReader rd = new BufferedReader(new InputStreamReader(conn
 					.getInputStream()));
 			StringBuffer sb = new StringBuffer();
 			String line;
 			while ((line = rd.readLine()) != null) {
 				sb.append(line);
 			}
 			rd.close();
 			result = sb.toString();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return result;
 	}
 	
 	public static boolean isValidPhone (String target) {
 		Pattern p = Pattern.compile("^(((44\\s?\\d{4}|\\(?0\\d{4}\\)?)\\s?\\d{3}\\s?\\d{3})|((44\\s?\\d{3}|\\(?0\\d{3}\\)?)\\s?\\d{3}\\s?\\d{4})|((44\\s?\\d{2}|\\(?0\\d{2}\\)?)\\s?\\d{4}\\s?\\d{4}))(\\s?\\#(\\d{4}|\\d{3}))?$"); //http://regexlib.com/REDetails.aspx?regexp_id=593
 		return p.matcher(target).matches();
 	}
 	
 	public static boolean isValidTwitter (String target) {
 		Pattern p = Pattern.compile("^@(.+)");
 		return p.matcher(target).matches();
 	}
 	
 	public static boolean isValidFacebookURL (String target) {
		Pattern p = Pattern.compile("^http://www.facebook.com/profile.php?id=([0-9]+)$");
 		return p.matcher(target).matches();
 	}
 
 	public static String ESENDEX_FORM = "https://www.esendex.com/secure/messenger/formpost/SendSMS.aspx";
 
 	public static String postToEsendex(String data) {
 		return Utility.sendPost(ESENDEX_FORM,
 				"Username=ben@growlingfish.com&Password=spam1234&Account=EX0064088"
 						+ data);
 	}
 
 	public static String sendSMS(String recipient, String body) {
 		if (Utility.isValidPhone(recipient)) {
 			log.warning("Sending SMS to " + recipient + " saying \"" + body + "\"");
 			if (body.length() > 160) {
 				log.warning("Body longer than 160 chars: " + body);
 			}
 			return postToEsendex("&Originator=Horizon&Recipient=" + recipient + "&Body=" + body
 					+ "&PlainText=1");
 		} else {
 			return null;
 		}
 	}
 
 	public static final String DOCTYPE = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">";
 
 	public static String headWithJQueryAndTitle(String title, String js) {
 		String html = DOCTYPE
 				+ "\n"
 				+ "<html>\n"
 				+ "<head><title>"
 				+ title
 				+ "</title>"
 				+ "<link href=\"style.css\" rel=\"stylesheet\" type=\"text/css\" />"
 				+ "<script type=\"text/javascript\" src=\"jquery-1.4.2.min.js\"></script>"
 				+ "<script type=\"text/javascript\">"
 			    + js
 			    + "</script>"
 				+ "</head>";
 
 		return html;
 	}
 	
 	public static String headWithTitle(String title) {
 		String html = DOCTYPE
 				+ "\n"
 				+ "<html>\n"
 				+ "<head><title>"
 				+ title
 				+ "</title>"
 				+ "<link href=\"style.css\" rel=\"stylesheet\" type=\"text/css\" />"
 				+ "</head>";
 
 		return html;
 	}
 
 	public static String headWithRefreshAndTitle(String title) {
 		String html = DOCTYPE
 				+ "\n"
 				+ "<html>\n"
 				+ "<head><title>"
 				+ title
 				+ "</title>"
 				+ "<meta http-equiv=\"refresh\" content=\"" + REFRESH_PERIOD + "\">"
 				+ "<link href=\"style.css\" rel=\"stylesheet\" type=\"text/css\" />"
 				+ "</head>";
 
 		return html;
 	}
 
 	public static String printHeaders(HttpServletRequest request) {
 		String headers = "<ul>";
 		Enumeration e = request.getHeaderNames();
 		while (e.hasMoreElements()) {
 			String headerName = (String) e.nextElement();
 			headers += "<li>" + headerName + "="
 					+ request.getHeader(headerName) + "</li>";
 		}
 		headers += "</ul>";
 		return headers;
 	}
 
 	private static final int ONE_HOUR = 60*60*1000;
 	
 	public static int getMinutesUntil (String arrivalTime) {
 		DateFormat sdf = new SimpleDateFormat("hhmm");
 		try {
 			Date date = sdf.parse(arrivalTime);
 			Calendar arrival = Calendar.getInstance();
 			arrival.setTime(date);
 			
 			SimpleTimeZone britTime = new SimpleTimeZone(0*ONE_HOUR, "Europe/London" /*GMT/BST*/,
 				    Calendar.MARCH,  -1, Calendar.SUNDAY /*DOW_IN_DOM*/, 1*ONE_HOUR,
 				    Calendar.OCTOBER,-1, Calendar.SUNDAY /*DOW_IN_DOM*/, 1*ONE_HOUR,
 				1*ONE_HOUR);
 
 			   // Apply TimeZone to create a Calendar object for UK locale
 			Calendar now = Calendar.getInstance();
 			now.setTimeZone(britTime);
 			return (arrival.get(Calendar.HOUR_OF_DAY) - now.get(Calendar.HOUR_OF_DAY)) * 60 + (arrival.get(Calendar.MINUTE) - now.get(Calendar.MINUTE)); 
 		} catch (ParseException e) {
 			e.printStackTrace();
 			return Integer.MAX_VALUE;
 		}
 	}
 }

 package net.chat.util;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 public class HttpClientTest {
 
 	/**
 	 * @param args
 	 * @throws ParseException
 	 */
 	public static void main(String[] args) throws IOException, ParseException {
 		// yyyy_mm_dd
 		String s = "2013_09_20";
 		SimpleDateFormat sf = new SimpleDateFormat("yyyy_MM_dd");
 		Date d = sf.parse(s);
 		System.out.println(d);
 		// String content = "<xml>";
 		// content = content + " <ToUserName><![CDATA[toUser]]></ToUserName>";
 		// content = content
 		// + " <FromUserName><![CDATA[fromUser]]></FromUserName>";
 		// content = content + "<CreateTime>1348831860</CreateTime>";
 		// content = content + "<MsgType><![CDATA[text]]></MsgType>";
 		// content = content + "<Content><![CDATA[你好]]></Content>";
 		// content = content + "<MsgId>1234567890123456</MsgId>";
 		// content = content + "</xml>";
 		// String content = "<xml>";
 		// content = content + " <ToUserName><![CDATA[toUser]]></ToUserName>";
 		// content = content
 		// + " <FromUserName><![CDATA[fromUser]]></FromUserName>";
 		// content = content + " <CreateTime>1348831860</CreateTime>";
 		// content = content + " <MsgType><![CDATA[image]]></MsgType>";
 		// content = content + " <PicUrl><![CDATA[this is a url]]></PicUrl>";
 		// content = content + " <MsgId>1234567890123456</MsgId>";
 		// content = content + " </xml>";
 
 		// @SuppressWarnings("deprecation")
 		// HttpClient httpclient = new DefaultHttpClient();
 		// HttpPost httppost = new HttpPost(
 		// "http://localhost:8080/WeiChat/API/JzfNgyGmEQKvqnaCBLNt");
 		// StringEntity myEntity = new StringEntity(content, "UTF-8");
 		// httppost.addHeader("Content-Type", "text/xml");
 		// httppost.setEntity(myEntity);
 		// HttpResponse response = httpclient.execute(httppost);
 		// HttpEntity resEntity = response.getEntity();
 		// InputStreamReader reader = new InputStreamReader(
 		// resEntity.getContent(), "UTF-8");
 
 		String content = "<xml>";
 		content = content + " <ToUserName><![CDATA[toUser]]></ToUserName>";
 		content = content
 				+ " <FromUserName><![CDATA[fromUser]]></FromUserName>";
 		content = content + "<CreateTime>1348831860</CreateTime>";
		content = content + "<MsgType><![CDATA[voice]]></MsgType>";
 		content = content + "<MediaId><![CDATA[media_id]]></MediaId>";
		content = content + "<Format><![CDATA[Format]]></Format>";
 		content = content + "<MsgId>1234567890123456</MsgId>";
 		content = content + "<MsgId>1234567890123456</MsgId>";
 		content = content + "</xml>";
 
 		@SuppressWarnings("deprecation")
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpPost httppost = new HttpPost(
 				"http://localhost:8080/WeiChat/API/lLfMiuDuqWiqKLvHXEvF");
 		StringEntity myEntity = new StringEntity(content, "UTF-8");
 		httppost.addHeader("Content-Type", "text/xml");
 		httppost.setEntity(myEntity);
 		HttpResponse response = httpclient.execute(httppost);
 		HttpEntity resEntity = response.getEntity();
 
 		InputStreamReader reader = new InputStreamReader(
 				resEntity.getContent(), "UTF-8");
 		char[] buff = new char[1024];
 		int length = 0;
 		while ((length = reader.read(buff)) != -1) {
 			System.out.println(new String(buff, 0, length));
 		}
 		httpclient.getConnectionManager().shutdown();
 
 	}
 }

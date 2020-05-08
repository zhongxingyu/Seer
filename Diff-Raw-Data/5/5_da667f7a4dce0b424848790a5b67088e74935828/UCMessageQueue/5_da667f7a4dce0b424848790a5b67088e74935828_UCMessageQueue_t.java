 package cn.uc.udac.mqs;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import org.apache.log4j.Logger;
 
 import cn.uc.udac.zjj.bolts.BoltCrawler;
 
 public class UCMessageQueue implements MessageQueue {
 
 	static public Logger LOG = Logger.getLogger(BoltCrawler.class);
 	private String _host;
 	private int _port;
 	private String _qname;
 	
 	public UCMessageQueue(String host, int port, String qname) {
 		_host = host;
 		_port = port;
 		_qname = qname;
 	}
 	
 	private boolean check(String resp) {
 		boolean flag = false;
		String tag = "UCMQ_HTTP_OK";
 		if (resp.indexOf(tag) == 0) {
 			flag = true;
			resp = resp.substring(tag.length()).trim();
 		}
 		return flag;
 	}
 	
 	@Override
 	public String get() throws IOException {
 		String resp="", line="";
 		URL url = new URL(String.format("http://%s:%d/?name=%s&opt=get&ver=2", _host, _port, _qname));
 		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 		conn.connect();
 		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));
 		while ((line = reader.readLine()) != null)
 			resp += line;
 		LOG.info(String.format("resp = %s", resp));
 		reader.close();
         conn.disconnect();
         if (check(resp)) return resp;
         else return "";
 	}
 
 	@Override
 	public boolean put(String msg) throws IOException {
 		String resp=null, line=null;
 		URL url = new URL(String.format("%s:%d/?name=%s&opt=put&ver=2", _host, _port, _qname));
 		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 		conn.setDoOutput(true);
 		conn.setDoInput(true);
 		conn.setRequestMethod("POST");
 		conn.connect();
 		DataOutputStream out = new DataOutputStream(conn.getOutputStream());
 		String content = URLEncoder.encode(msg, "utf-8");
 		out.writeBytes(content); 
 	    out.flush();
 	    out.close();
 	    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));
 		while ((line = reader.readLine()) != null)
 			resp += line;
 		reader.close();
         conn.disconnect();
         return check(resp);
 	}
 
 	@Override
 	public boolean reset() throws IOException {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }

 package com.withparadox2.whut.library.search;
 
 import java.io.IOException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.parser.Parser;
 import org.jsoup.select.Elements;
 
 import android.os.Handler;
 import android.os.Message;
 
 import com.withparadox2.whut.dao.WhutGlobal;
 
 public class HttpSearchThread extends Thread{
 	private HttpGet httpGet;
 	private HttpClient httpClient = new DefaultHttpClient();
 	private HttpResponse response;
 	private String html;
 	private int page;
 	
 	private static List<String> bookRecnoNumList = new ArrayList<String>();
 	private static Map<String, String> bookRecnoNumValueMap = new HashMap<String, String>();//from bookRecnoNumList get value
 	private Message msg;
 	
 	private Handler myHandler;
 	
 	public HttpSearchThread(Handler handler, int page){
 		this.myHandler = handler;
 		this.page = page;
 	}
 	public HttpSearchThread(Handler handler){
 		this.myHandler = handler;
 	}
 	
 	@Override
 	public void run() {
 		// TODO Auto-generated method stub
 		switch(WhutGlobal.WhichAction){
 		case SearchBookActivity.UPDATE_GROUP_THREAD:
 			getGroupData();
 			break;
 		case SearchBookActivity.UPDATE_CHILD_THREAD:
 			getChildData();
 			break;
 		}
 	}
 	
 	private void sendMyMessage(int arg){
 		msg = myHandler.obtainMessage();
 		msg.arg1 = arg;
 		msg.sendToTarget();
 	}
 	
 	private void getGroupData(){
 		try {
 			sendMyMessage(SearchBookActivity.GET_HTML);
 			getSearchBookResultHtml();
 			getBookListData();
 			sendMyMessage(SearchBookActivity.UPDATE_GROUP);
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IndexOutOfBoundsException e) {
 			// TODO: handle exception
 			e.printStackTrace();
 			sendMyMessage(SearchBookActivity.NO_BOOKS);
 		}
 	}
 	
     private void getSearchBookResultHtml() throws ClientProtocolException, IOException{
     	httpGet = new HttpGet("http://202.114.89.11/opac/search?rows=10&searchWay=title&q="+URLEncoder.encode(WhutGlobal.SEARCH_TITLE)+"&page="+page);
 	    HttpResponse response;
 		response = httpClient.execute(httpGet);
         HttpEntity entity = response.getEntity();
         html = EntityUtils.toString(entity, "UTF-8");    	
        // System.out.println( "-----------"+html);
     }
     
     private void getBookListData() throws ClientProtocolException, IndexOutOfBoundsException, IOException{
     	bookRecnoNumValueMap.clear();
     	bookRecnoNumList.clear();
     	 String[] row;
     	 ArrayList<String[]> childArrayList;
 	     Document document = Jsoup.parse(html);
 	     Elements books = document.select(".resultTable .bookmeta");
 	     for(int i=0; i<books.size(); i++){
 	    	 bookRecnoNumList.add(books.get(i).attr("bookrecno"));
 	     }
 	     parseGroupXML();
 	     for(int i=0; i<books.size(); i++){
 	    	 row = new String[5];
 	    	 row[0] = books.get(i).select(".bookmetaTitle a").html().toString();
 	    	 row[1] = books.get(i).select("div a").get(2).html().toString();
 	    	 row[2] = books.get(i).select("div a").get(3).html().toString();
 	    	 row[3] = bookRecnoNumValueMap.get(bookRecnoNumList.get(i));
 	    	 row[4] = bookRecnoNumList.get(i);
 	    	 WhutGlobal.BOOKLIST.add(row);
 	    	 childArrayList = new ArrayList<String[]>();
 	    	 WhutGlobal.CHILDLIST.add(childArrayList);
 	    	 WhutGlobal.CLICK_GROUP_FLAG.add(false);
 	     }
     }
     
     private String getCallnoXml() throws ClientProtocolException, IOException{
     	String url = getCallnoXmlUrl();
     	httpGet = new HttpGet(url);
 	    HttpResponse response;
 		response = httpClient.execute(httpGet);
         HttpEntity entity = response.getEntity();
         return EntityUtils.toString(entity, "UTF-8");    	
     }
 
     private String getCallnoXmlUrl(){
     	StringBuilder url = new StringBuilder("http://202.114.89.11/opac/book/callnos?bookrecnos=");
     	for(int i=0; i<bookRecnoNumList.size()-1; i++){
     		url.append(bookRecnoNumList.get(i)).append("%2C");
     	}
     	return url.append(bookRecnoNumList.get(bookRecnoNumList.size()-1)).toString();
     }
     
     private void parseGroupXML() throws ClientProtocolException, IOException, IndexOutOfBoundsException{
     	
 		Document xmlParse = Jsoup.parse(getCallnoXml(), "", Parser.xmlParser());
 		Elements nodes = xmlParse.select("record");
 		for(int i=0; i<nodes.size(); i++){
 			bookRecnoNumValueMap.put(nodes.get(i).select("bookrecno").text(), nodes.get(i).select("callno").text());
 		}
     }
     
     private String getChildXml() throws ClientProtocolException, IOException{
     	String url = "http://202.114.89.11/opac/book/holdingpreview/" + WhutGlobal.BOOK_CODE;
     	httpGet = new HttpGet(url);
 	    HttpResponse response;
 		response = httpClient.execute(httpGet);
         HttpEntity entity = response.getEntity();
         return EntityUtils.toString(entity, "UTF-8");   
     }
     
     private void getChildData(){
     	Document xmlParse = null;
     	String s[];
 		try {
 			xmlParse = Jsoup.parse(getChildXml(), "", Parser.xmlParser());
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     	Elements callnos = xmlParse.select("callno");
     	Elements locations = xmlParse.select("curlocalName");
     	for(int i=0; i<callnos.size(); i++){
     		s = new String[2];
     		s[0] = callnos.get(i).text();
     		s[1] = locations.get(i).text();
     		WhutGlobal.CHILDLIST.get(WhutGlobal.BOOK_CODE_POS).add(s);
     	}
     	sendMyMessage(SearchBookActivity.UPDATE_CHILD);
     }
 }

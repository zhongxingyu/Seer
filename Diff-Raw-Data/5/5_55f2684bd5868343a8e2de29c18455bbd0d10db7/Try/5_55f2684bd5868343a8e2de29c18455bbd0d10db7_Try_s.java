 package ru.xrm.app;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import ru.xrm.app.httpclient.CachingHttpFetcher;
 
 public class Try {
 
 	public static void main(String[] args){
		
     	CachingHttpFetcher hf=CachingHttpFetcher.getInstance();
 	
     	String content=hf.fetch("http://www.e1.ru/business/job/vacancy.search.php?search=yes&section=23", "windows-1251");
     	Document doc=Jsoup.parse(content);
 		Elements elems=doc.select("table[border=0][cellspacing=0][cellpadding=3]:not([align=left]) span.big a[href~=search=yes]");
 
 		for (Element e:elems){
 			System.out.format("%s \n",e.attr("href"));
		}
 	}
 }

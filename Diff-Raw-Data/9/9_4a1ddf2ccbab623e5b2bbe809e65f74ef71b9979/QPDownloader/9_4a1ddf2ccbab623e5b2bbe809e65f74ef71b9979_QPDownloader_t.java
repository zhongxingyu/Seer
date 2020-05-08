 //File: QPDownloader.java
 /*TODO
 1. Try remove "\n" in strbuf.append() call : Why it does not work?
 */
 import java.io.IOException;
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.InputStreamReader;
 
 import java.net.URL;
 import java.net.URLConnection;
 
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 import java.util.ArrayList;
 import java.util.Iterator;
 class CourseInfo{
 	CourseInfo(String na, String li){
 		name = (na!=null)?na:"";
 		link = (li!=null)?li:"";
 		System.out.print(name + "\n" + link + "\n");
 	}
 	String link;
 	String name;
 }
 class SeasonPage{//Contains info for 1 exam season
 	void setCourseInfo(String input){
 		Pattern pattern1 = Pattern.compile("http.*pdf");
 		Matcher matcher1 = pattern1.matcher(input);
 		Pattern pattern2 = Pattern.compile(">.*<");
 		Matcher matcher2 = pattern2.matcher(input);
 		coursesInfo = new ArrayList<CourseInfo>();
 		if(matcher1.find() && matcher2.find()){
 			int start = matcher1.start();
 			int end = matcher1.end();
 			String course_link = input.substring(start, end);
 
 			start = matcher2.start()+1;
 			end = matcher2.end()-1;
			String course_name = input.substring(start, end).trim();
 			coursesInfo.add(new CourseInfo(course_name, course_link));
 		}
 	}
 	SeasonPage(String na, String li){
 		name = (na!=null)?na:"";
 		link = (li!=null)?li:"";
 		System.out.print(name + "\n" + link + "\n");
 
 		//initialise coursesInfo
 		String seasonPageHTML = (new DownloadHTML(link)).getHTML();
		//http://172.31.19.11/qp/esmay09/BH008.pdf" style="text-decoration: underline;"> BH008</
		//http://cl.thapar.edu/qp/EN0105.pdf">EN105</
		String patternString = "http.+pdf\".*>\\s*\\w+\\s*</";
 		Pattern pattern = Pattern.compile(patternString);
 		
 		Matcher matcher = pattern.matcher(seasonPageHTML);
 		
 		while(matcher.find()){
 			int start = matcher.start();
 			int end = matcher.end();
 			String match = seasonPageHTML.substring(start, end);
 			setCourseInfo(match);
 		}
 		System.out.println("========================================================");
 		System.out.println("========================================================");
 	}
 	String link;
 	String name;
 	ArrayList<CourseInfo> coursesInfo;
 }
 public class QPDownloader{//downloads links from the html select box and saves each in SeasonPage object
 	QPDownloader(){
 		seasonPagesInfo = new ArrayList<SeasonPage>();
 	}
 	void setExamSeasonInfo(String input){
 		Pattern pattern1 = Pattern.compile("http.*html");
 		Matcher matcher1 = pattern1.matcher(input);
 		Pattern pattern2 = Pattern.compile(">.*<");
 		Matcher matcher2 = pattern2.matcher(input);
 		if(matcher1.find() && matcher2.find()){
 			int start = matcher1.start();
 			int end = matcher1.end();
 			String season_link = input.substring(start, end);
 
 			start = matcher2.start()+1;
 			end = matcher2.end()-1;
 			String season_name = input.substring(start, end);
 			seasonPagesInfo.add(new SeasonPage(season_name, season_link));
 		}
 	}
 	void printExamSeasonInfo(){
 		Iterator season=seasonPagesInfo.iterator();
 		while(season.hasNext())
 		{
 			SeasonPage s = (SeasonPage)(season.next());
 			System.out.println(s.name);
 			System.out.println(s.link);
 			System.out.println("");
 		}
 	}
 	public static void main(String[] args){
 		String input = (new DownloadHTML("http://cl.thapar.edu/library_qp.html")).getHTML();
 
 		String patternString = "<option.+option>";
 		Pattern pattern = Pattern.compile(patternString);
 		
 		Matcher matcher = pattern.matcher(input);
 		QPDownloader examSeasons = new QPDownloader();
 		while(matcher.find()){
 			int start = matcher.start()+14;
 			int end = matcher.end()-8;
 			String match = input.substring(start, end);
 			//System.out.println(match);
 			//for each page full of pdf links, read pdf link and its name in anchor tag.
 			examSeasons.setExamSeasonInfo(match);
 		}
 		//examSeasons.printExamSeasonInfo();
 	}
 	ArrayList<SeasonPage> seasonPagesInfo;
 }
 class DownloadHTML{
 	DownloadHTML(String web_link){
 		try{
 			// Here we give the URL for the Crawler
 			URL url = new URL(web_link);
 
 			strbuf = new StringBuffer();
 			System.setProperty("http.proxyHost","");
 			System.setProperty("http.proxyPort", "");
 			URLConnection conn = url.openConnection();
 			DataInputStream in = new DataInputStream ( conn.getInputStream ( ) ) ;
 			BufferedReader d = new BufferedReader(new InputStreamReader(in));
 			while(d.ready())
 			{
 				//System.out.println(d.readLine());
 				strbuf.append(d.readLine()+"\n");
 			}
 		}
 		catch(IOException e)
 		{
 			System.out.println(e);
 		}
 	}
 	String getHTML()
 	{
 		return strbuf.toString();
 	}
 	
 	private StringBuffer strbuf;
 }

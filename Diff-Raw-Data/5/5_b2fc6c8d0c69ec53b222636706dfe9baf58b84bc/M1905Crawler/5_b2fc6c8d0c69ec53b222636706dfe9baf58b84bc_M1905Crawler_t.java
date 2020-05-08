 package crawl;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 
 import model.Movie_Info;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.nodes.Node;
 import org.jsoup.select.Elements;
 
 import util.LogUtil;
 import witer.DBWriter;
 import witer.ImageWriter;
 
 public class M1905Crawler extends BaseCrawler{
 	
 	private final static String ID_NEW_PAGE = "new_page";
 	private final static String ATT_SCR = "src";
 	private final static String ATT_ALT = "alt";
 	private final static String ATT_HREF = "href";
 	private final static String NO_PIC = "nopic.gif";
 	
 	public static void main(String[] args){
 		M1905Crawler mc = new M1905Crawler();
 		mc.begin();
 	}
 	
 	public M1905Crawler(){
 		movie_src = "M1905";
 		CRAWLABLE_URLS.add("http://www.m1905.com/mdb/film/list/year-2013/o0d0p%d.html");
 		CRAWLABLE_URLS.add("http://www.m1905.com/mdb/film/list/year-2012/o0d0p%d.html");
 		CRAWLABLE_URLS.add("http://www.m1905.com/mdb/film/list/year-2011/o0d0p%d.html");
 		CRAWLABLE_URLS.add("http://www.m1905.com/mdb/film/list/year-2010/o0d0p%d.html");
 		CRAWLABLE_URLS.add("http://www.m1905.com/mdb/film/list/year-2009/o0d0p%d.html");
 		CRAWLABLE_URLS.add("http://www.m1905.com/mdb/film/list/year-2008/o0d0p%d.html");
 		CRAWLABLE_URLS.add("http://www.m1905.com/mdb/film/list/year-2007/o0d0p%d.html");
 		CRAWLABLE_URLS.add("http://www.m1905.com/mdb/film/list/year-2006/o0d0p%d.html");
 		CRAWLABLE_URLS.add("http://www.m1905.com/mdb/film/list/year-2005/o0d0p%d.html");
 		CRAWLABLE_URLS.add("http://www.m1905.com/mdb/film/list/year-2004/o0d0p%d.html");
 		CRAWLABLE_URLS.add("http://www.m1905.com/mdb/film/list/year-2003/o0d0p%d.html");
 		CRAWLABLE_URLS.add("http://www.m1905.com/mdb/film/list/year-2002/o0d0p%d.html");
 		CRAWLABLE_URLS.add("http://www.m1905.com/mdb/film/list/year-2001/o0d0p%d.html");
 		CRAWLABLE_URLS.add("http://www.m1905.com/mdb/film/list/year-2000/o0d0p%d.html");
 	}
 	
 	protected void begin(){
 		super.begin();
 	}
 	
 	/**
 	 * 获取电影信息
 	 * @param id : 当前线程ID
 	 * @param sUrl : 网页地址
 	 * @return 当前页获取电影数量
 	 */
 	protected int crawlMovies(int id, String sUrl){
 		Document doc = null;
 		try {
 			doc = Jsoup.connect(sUrl)
 					.userAgent(AGENT).timeout(TIME_OUT).post();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if(doc == null){
 			return 0;
 		}
 		
 		//finding movies
 		try {
 			Elements inqList_childs = doc.getElementById(ID_NEW_PAGE).previousElementSibling().children();
 			doc = null;
 			ArrayList<Movie_Info> movie_list = new ArrayList<Movie_Info>();
 			for(int i= 0; i < inqList_childs.size(); i ++){
 				Element movie_elements =  inqList_childs.get(i).children()
 						.first();
 				String href = movie_elements.attr(ATT_HREF);
 				Element movie_element =  movie_elements.children().first();
 				String src = movie_element.attr(ATT_SCR);
 				String alt = movie_element.attr(ATT_ALT);
 				if(alt != null && src != null){
 					src = src.substring(0, src.lastIndexOf("/") + 1) + 
 							src.substring(src.lastIndexOf("_") + 1, src.length());
 					Movie_Info movie = new Movie_Info(alt, src);
 					//含有nopic.gif表示当前电影没有海报
 					if(!src.endsWith(NO_PIC)){
 						ImageWriter.getInstance().addMovieList(movie);
 					}
 					
 					//获取影片译名
 					try {
 						doc = Jsoup.connect(href)
 								.userAgent(AGENT).timeout(TIME_OUT).post();
 						Node other_name_node = doc.getElementsByClass("laMovName").first().child(1).child(0).childNode(0);
 						if(other_name_node.childNodeSize() != 0){
 							String other_name = other_name_node.childNode(0).toString();
 							if(other_name.length() != 0){
 								movie.addName(other_name);
 							}
 						}
 					}catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
						System.err.println(href);
 					}catch (NullPointerException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
						System.err.println(href);
 					}
 					movie_list.add(movie);
 				}
 			}
 			DBWriter.getInstance().addMovieList(movie_list);
 			return inqList_childs.size();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			LogUtil.getInstance().write(this.getClass().getName() + "	[error] crawling movies at URL : " + sUrl);
 			return 0;
 		}
 	}
 
 	@Override
 	protected boolean getMaxPage() {
 		for(int i = 0; i < CRAWLABLE_URLS.size() ; i ++){
 			Document doc = null;
 			String url = String.format(CRAWLABLE_URLS.get(i), 1);
 			try {
 				doc = Jsoup.connect(url)
 						.userAgent(AGENT).timeout(TIME_OUT * 2).post();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			if(doc == null){
 				return false;
 			}
 			//find last page
 			try {
 				Elements totall_num_elements = doc.getElementsByClass("termsBox");
 				Node totall_num_node = totall_num_elements.first().childNode(5).childNode(0);
 				String totall_num_str = totall_num_node.toString();
 				String num = totall_num_str.substring(2, totall_num_str.length() - 3);
 				int last_page = Integer.parseInt(num) / 30 + 1;
 				CRAWLABLE_MAX_PAGE.add(i, last_page);
 				System.out.println("last page found: " + last_page);
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				LogUtil.getInstance().write(this.getClass().getName() + "	[error] getting max page at URL : " + url);
 			}
 		}
 		return true;
 	}
 }
 

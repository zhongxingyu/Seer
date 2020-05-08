 package com.hannah.http.baidu;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 public class TiebaUtil {
 
 	/**
 	 * 获取百度贴吧网址
 	 * @param tieba 贴吧
 	 * @return
 	 * @throws UnsupportedEncodingException 
 	 */
 	public static String getTiebaUrl(String tieba) throws UnsupportedEncodingException {
 		tieba = URLEncoder.encode(tieba, "utf-8");
 		return "http://tieba.baidu.com/f?ie=utf-8&kw=" + tieba;
 	}
 
 	public static String getSearchUrl(String tieba, String searchWord) throws UnsupportedEncodingException {
 		return getSearchUrl(tieba, searchWord, true);
 	}
 
 	/**
 	 * 获取百度贴吧中搜索网址
 	 * @param tieba 贴吧
 	 * @param searchWord 搜索关键字
 	 * @param onlyThread 只看主题贴
 	 * @return
 	 * @throws UnsupportedEncodingException 
 	 */
 	public static String getSearchUrl(String tieba, String searchWord, boolean onlyThread) throws UnsupportedEncodingException {
 		tieba = URLEncoder.encode(tieba, "utf-8");
 		searchWord = URLEncoder.encode(searchWord, "utf-8");
 		if (onlyThread)
 			return "http://tieba.baidu.com/f/search/res?ie=utf-8&kw=" + tieba + "&qw=" + searchWord + "&only_thread=1";
 		else
 			return "http://tieba.baidu.com/f/search/res?ie=utf-8&kw=" + tieba + "&qw=" + searchWord;
 	}
 
 	public static List<TiebaSearchResult> search(String tieba, String searchWord, boolean onlyThread) throws IOException {
 		return fetchSearchResult(getSearchUrl(tieba, searchWord, onlyThread));
 	}
 
 	/**
 	 * 返回贴吧搜索结果
 	 * @param url
 	 * @return
 	 * @throws IOException
 	 */
 	public static List<TiebaSearchResult> fetchSearchResult(String url) throws IOException {
 		List<TiebaSearchResult> results = new ArrayList<TiebaSearchResult>();
 		Document doc = Jsoup.connect(url).get();
 		Elements posts = doc.select("div.s_post");
 		for(Element post : posts) {
 			TiebaSearchResult result = new TiebaSearchResult();
 			// 标题和链接
 			Elements hrefs = post.select("a[href]");
 			Element title = hrefs.first();
 			result.setTitle(title.text());
 			result.setUrl(title.attr("abs:href"));
 			// 摘要
 			Element brief = post.select("div.p_content").first();
 			result.setBrief(brief.text());
 			// 贴吧
 			Element tieba = hrefs.get(1);
 			result.setTieba(tieba.text());
			// 作者
			Element author = hrefs.get(2);
			result.setAuthor(author.text());
 			// 更新时间
 			Element updateTime = post.select("font.p_green").first();
 			result.setUpdateTime(updateTime.text());
 			results.add(result);
 		}
 		return results;
 	}
 
 	/**
 	 * 获取只看楼主链接
 	 * @param url
 	 * @return
 	 */
 	public static String getSeeLzUrl(String url) {
 		int index = url.indexOf("?");
 		if (index != -1)
 			url = url.substring(0, index);
 		return url + "?see_lz=1";
 	}
 
 	/**
 	 * 获取帖子内容
 	 * @param url
 	 * @return
 	 * @throws IOException
 	 */
 	public static List<String> fetchPostContent(String url) throws IOException {
 		List<String> contentList = new ArrayList<String>();
 		Document doc = Jsoup.connect(url).get();
 		Elements contents = doc.select("div.p_content");
 		for (Element content : contents) {
 			content = content.child(0).child(0);
 			String htmlText = content.html().replaceAll("<br.{0,2}>", "   ").replaceAll("<a [^>]*>|</a>", "");
 			htmlText = htmlText.replaceAll("&nbsp;", " ");
 			contentList.add("    " + htmlText);
 		}
 		return contentList;
 	}
 
 }

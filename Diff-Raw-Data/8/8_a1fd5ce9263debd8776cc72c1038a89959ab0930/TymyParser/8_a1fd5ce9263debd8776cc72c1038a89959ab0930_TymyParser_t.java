 package com.ph.tymyreader;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.text.Html;
 
 import com.ph.tymyreader.model.DsItem;
 
 /** 
  * @author petr Haering
  *
  */
 public class TymyParser {
 
 	//private final String TAG = TymyReader.TAG;
 	private String s = null;
 	private Integer position = 0; 
 
 	public TymyParser(final String inputString) {
 		this.s = inputString;
 	}
 	
 	public TymyParser() {
 		this(null);
 	}
 
 
 	/**
 	 * public Integer findStartTag (final String tagName)
 	 * <br>
 	 * return index of last char before the stop tag ($lt;/tag&gt;)
 	 * 
 	 * @param tagName	Name of tag
 	 * @return index of last character before stop tag ($lt;/tag&gt;) or -1 if not found
 	 */
 	public Integer startTag (final String tagName, final String attr) {
 		boolean hit = false;
 		Integer absPos = 0;
 		Integer relStart = 0;
 		Integer relEnd = 0;
 		String tag = "<" + tagName;
 		while ((relStart = (s.substring(absPos)).indexOf(tag)) != -1) {
 			while ((relEnd = (s.substring(absPos + relStart)).indexOf('>')) != -1) {
 				break;
 			}			
 			String t = s.substring(absPos + relStart, absPos + relStart + relEnd + 1);
 			if (hit = t.matches(".*" + attr + ".*")) {
 				absPos += relStart;
 				break;
 			}
 			absPos += relStart + relEnd;
 		}
 		return (hit ? absPos : -1);
 	}
 
 
 	/**
 	 * public Integer findStartTag (final String tagName)
 	 * <br>
 	 * return index of last char before the stop tag (</tag>)
 	 * 
 	 * @param tagName	Name of tag
 	 * @return index of last character before stop tag ($lt;/tag&gt;) or -1 if not found
 	 */
 	public Integer stopTag (final String tagName, final String cls) {
 		Integer position = -1;
 		String tag = "</" + tagName;
 		if ((position = startTag(tagName, cls)) != -1) {
 			position += s.substring(position).indexOf(tag) + tag.length();
 		}
 		return position;
 	}
 
 	/**
 	 * extract text and the start and stop tag: &lt;tag&gt;..&lt;/tag&gt;
 	 * 
 	 * @param tagName	Name of tag
 	 * @return a string representation of whole tag 
 	 */
 	public String getWholeTag (final String tagName, final String attr) {
 		String out = "";
 		int start, end;
 
 		s = s.substring(position);
 		start = startTag(tagName, attr);
 		end = stopTag(tagName, attr);
 		if (start != -1) {
 			if (end != -1) {
 				out = s.substring(start, end + 1);
 				position = end + 1;
 			}		
 			else {
 				out = s.substring(start);
 				position = s.length();
 			}		
 		}
 		return out;		
 	}
 
 	/**
 	 * extract text between the start and stop tag: &lt;tag&gt;..&lt;/tag&gt;
 	 * 
 	 * @param tagName	Name of tag
 	 * @param attr		attribute of tag
 	 * @return a string representation of content of tag 
 	 */
 	public String getBodyOfTag (final String tagName, final String attr) {
 		String out = "";
 
 		out = getWholeTag(tagName, attr);
 		int x = out.indexOf('>') + 1;
 		int y = out.lastIndexOf('<');
 		return out.substring(x, y);
 	}
 
 	public String getRawText () {
 		s = s.substring(position);
 		return s;
 	}
 
 	@SuppressWarnings("unused")
 	private int getPosition () {
 		return position;
 	}
 
 
 	public DsItem getDsItem () {
 		final String ITEMTAG = "td";
 		final String ITEMTAGATTR = "class=\"dsitem\"";
 		final String CAPTAG = "div";
 		final String CAPTAGATTR = "class=\"dscaption\"";
 		DsItem dsItem = new DsItem();
 		String post;
 
 		if ((post = getWholeTag(ITEMTAG, ITEMTAGATTR)) != "") {
 			TymyParser postParser = new TymyParser(post);
 			dsItem.setDsCaption(Html.fromHtml(postParser.getWholeTag(CAPTAG, CAPTAGATTR)).toString().trim());
 			dsItem.setDsItemText(Html.fromHtml(postParser.getRawText()).toString().trim());
 			return dsItem;
 		} else {
 			return null;
 		}
 	}
 
 	// TODO dodelat automaticke vytazeni jmen diskusi
 	/**
 	 * detDisArray parse page and try to find pairs discussion ID and NAME. Returns
 	 * String Array with String of "ID:NAME"
 	 * 
 	 * @param mainPage tymy main page
 	 * @return String Array of pairs "ID:NAME"
 	 */
 	public ArrayList<String> getDsArray(String mainPage) {
 		ArrayList<String> ds = new ArrayList<String>();
 		
		Pattern MY_PATTERN;
		if (mainPage.indexOf("menu_item") == -1) {
			MY_PATTERN = Pattern.compile("<a .*?page=discussion&amp;id=(.*?)&amp;level=101\">(.*?)</a> <span");			
		} else {
			//Complete menu version
			MY_PATTERN = Pattern.compile("<a href=\"/index.php\\?page=discussion&amp;id=(.*?)&amp;level=101\"><img alt=\"(.*?)\".*?</span>");			
		}
 		Matcher m = MY_PATTERN.matcher(mainPage);
 		while (m.find()) {
 		    ds.add(m.group(1) + ":" + m.group(2));
 		}
 		return ds;
 	}
 	
 	/**
 	 * Parse ajaxPage contains information about new items in discussions. Returns 
 	 * HashMap<String, Integer> where key id dsId and value is number of new items
 	 * e.g. [dsId, newItems]
 	 * @param ajaxPage String
 	 * @return Returns HashMap<String, Integer> where K=dsId and V=newItems.
 	 */
 	public HashMap<String, Integer> getNewItems (String ajaxPage) {
 		HashMap<String, Integer> ds_news = new HashMap<String, Integer>();		
 		Pattern MY_PATTERN = Pattern.compile("ds_new_(.*?)\".*?:(.*?)</b>");
 
 		Matcher m = MY_PATTERN.matcher(ajaxPage);
 		while (m.find()) {
 			ds_news.put(m.group(1), Integer.parseInt(m.group(2).trim()));		
 		}
 		return ds_news;
 	}
 	
 	// TODO for parsing dsItems could be used regular expresion
 	//Pattern MY_PATTERN = Pattern.compile("<td valign=\"top\" class=\"dsitem\"><div class=\"dscaption\"><b>(.*?)</b>&nbsp;-&nbsp;(.*?)&nbsp; &nbsp;</div>(.*?)</td>");
 
 }
 
 

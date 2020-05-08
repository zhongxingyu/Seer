 /**
  * 
  */
 package org.qburst.search.model;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * @author Cyril
  * 
  */
 public class Search {
 	private List<String> highlights = new LinkedList<String>();
 	private String author = "";
 	private String id = "";
 	private String title = "";
 	private String url = "";
 	private String fileName = "";
 
 	public String getAuthor() {
 		return author;
 	}
 
 	public void setAuthor(String author) {
 		this.author = author;
 	}
 
 	public List<String> getHighlights() {
 		return highlights;
 	}
 
 	public void setHighlights(List<String> highlights) {
 		this.highlights = highlights;
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
		if (title.trim().equals("")){
			title = url.replaceAll("([\\s\\S])+/", "");
		}
 		this.title = title;
 	}
 
 	public String getUrl() {
 		return url;
 	}
 
 	public void setUrl(String url) {
 		this.url = url;
 		this.fileName = url.replaceAll("([\\s\\S])+/", "");
 	}
 
 	public String getFileName() {
 		return fileName;
 	}
 }

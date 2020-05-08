 package com.snow.model;
 
public class Article { 
 	private Integer id;
 	private Integer userid;
 	private Integer typeid;
 	private String title;
 	private String content;
 	private String time;
 	public Integer getId() {
 		return id;
 	}
 	public void setId(Integer id) {
 		this.id = id;
 	}
 	public Integer getUserid() {
 		return userid;
 	}
 	public void setUserid(Integer userid) {
 		this.userid = userid;
 	}
 	public Integer getTypeid() {
 		return typeid;
 	}
 	public void setTypeid(Integer typeid) {
 		this.typeid = typeid;
 	}
 	public String getTitle() {
 		return title;
 	}
 	public void setTitle(String title) {
 		this.title = title;
 	}
 	public String getContent() {
 		return content;
 	}
 	public void setContent(String content) {
 		this.content = content;
 	}
 	public String getTime() {
 		return time;
 	}
 	public void setTime(String time) {
 		this.time = time;
 	}
 	@Override
 	public String toString() {
 		return "Article [id=" + id + ", userid=" + userid + ", typeid="
 				+ typeid + ", title=" + title + ", content=" + content
 				+ ", time=" + time + "]";
 	}
 	
 	
 	
 }

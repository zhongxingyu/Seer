 package com.smit.web.content.forms;
 
 
 import java.sql.Timestamp;
 
 import org.apache.struts.action.ActionForm;
 
 import com.smit.vo.Part;
 
 public class ContentForm extends ActionForm{
 
 	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4767023827329542424L;
 	private Integer id;
 	private String title;
 	private String shortTitle;
 	private Integer isCheck;
 	private Part part;
 	private String excerpt;
 	private String typeName;
 	private String tags;
 	private String source;
 	private String author;
 	private String content;
 	private Integer putter;
 	private Integer onclickCount;
 	private Integer langType;
 	private Integer prime;
 	private Integer sortRank;
 	
 	
 	private String subImg;
 	public Integer getId() {
 		return id;
 	}
 	public void setId(Integer id) {
 		this.id = id;
 	}
 	public String getTitle() {
 		return title;
 	}
 	public void setTitle(String title) {
 		this.title = title;
 	}
 	public String getShortTitle() {
 		return shortTitle;
 	}
 	public void setShortTitle(String shortTitle) {
 		this.shortTitle = shortTitle;
 	}
 	public Integer getIsCheck() {
 		return isCheck;
 	}
 	public void setIsCheck(Integer isCheck) {
 		this.isCheck = isCheck;
 	}
 	public Part getPart() {
 		return part;
 	}
 	public void setPart(Part part) {
 		this.part = part;
 	}
 	public String getExcerpt() {
 		return excerpt;
 	}
 	public void setExcerpt(String excerpt) {
 		this.excerpt = excerpt;
 	}
 	public String getTypeName() {
 		return typeName;
 	}
 	public void setTypeName(String typeName) {
 		this.typeName = typeName;
 	}
 	public String getTags() {
 		return tags;
 	}
 	public void setTags(String tags) {
 		this.tags = tags;
 	}
 	public String getSource() {
 		return source;
 	}
 	public void setSource(String source) {
 		this.source = source;
 	}
 	
 	public String getAuthor() {
 		return author;
 	}
 	public void setAuthor(String author) {
 		this.author = author;
 	}
 	public String getContent() {
 		return content;
 	}
 	public void setContent(String content) {
 		this.content = content;
 	}
 	public Integer getPutter() {
 		return putter;
 	}
 	public void setPutter(Integer putter) {
 		this.putter = putter;
 	}
 	public Integer getOnclickCount() {
 		return onclickCount;
 	}
 	public void setOnclickCount(Integer onclickCount) {
 		this.onclickCount = onclickCount;
 	}
 	public Integer getLangType() {
 		return langType;
 	}
 	public void setLangType(Integer langType) {
 		this.langType = langType;
 	}
 	public Integer getPrime() {
 		return prime;
 	}
 	public void setPrime(Integer prime) {
 		this.prime = prime;
 	}
 	public Integer getSortRank() {
 		return sortRank;
 	}
 	public void setSortRank(Integer sortRank) {
 		this.sortRank = sortRank;
 	}
 	
 	public String getSubImg() {
 		return subImg;
 	}
 	public void setSubImg(String subImg) {
 		this.subImg = subImg;
 	}
 
  
 }

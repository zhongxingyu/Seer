 package com.xlthotel.foundation.orm.entity;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 /** 用户投诉 */
 @Entity
 @Table(name = "xlt_usercomplaint")
 public class Complaint extends UUIDEntity {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	// 投诉标题
 	private String title;
 	// 投诉内容
 	private String content;
 	// 创建时间
 	private Date createDate;
 	// 查看数
 	private int viewNum;
 	// 回复数
 	private int replyNum;
 	// 状态 1为审核通过有效 0为审核不通过无效
 	private int status;
	// // 酒店 暂时注销
 	private Hotel hotel;
 	// 人员
 	private UserInfo userInfo;
 	/** 酒店投诉回复 */
 	private Set<ComplaintNotes> complaintNotes = new HashSet<ComplaintNotes>(0);
 
 	@Column(name = "title", length = 100, nullable = true)
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	@Column(name = "content", length = 1024, nullable = true)
 	public String getContent() {
 		return content;
 	}
 
 	public void setContent(String content) {
 		this.content = content;
 	}
 
 	@Column(name = "createDate", nullable = true)
 	@Temporal(TemporalType.TIMESTAMP)
 	public Date getCreateDate() {
 		return createDate;
 	}
 
 	public void setCreateDate(Date createDate) {
 		this.createDate = createDate;
 	}
 
 	@Column(name = "viewNum", length = 7, nullable = true)
 	public int getViewNum() {
 		return viewNum;
 	}
 
 	public void setViewNum(int viewNum) {
 		this.viewNum = viewNum;
 	}
 
 	public int getReplyNum() {
 		return replyNum;
 	}
 
 	@Column(name = "replyNum", length = 7, nullable = true)
 	public void setReplyNum(int replyNum) {
 		this.replyNum = replyNum;
 	}
 
 	@Column(name = "status", length = 1, nullable = true)
 	public int getStatus() {
 		return status;
 	}
 
 	public void setStatus(int status) {
 		this.status = status;
 	}
 
 	 @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
 	 @JoinColumn(name = "hotelId", unique = false, nullable = false,insertable = true, updatable = true)
 	 public Hotel getHotel() {
 	 return hotel;
 	 }
 	
 	 public void setHotel(Hotel hotel) {
 	 this.hotel = hotel;
 	 }
 
 	@ManyToOne(cascade = {}, fetch = FetchType.LAZY)
 	@JoinColumn(name = "userId", unique = false, nullable = false, insertable = true, updatable = true)
 	public UserInfo getUserInfo() {
 		return userInfo;
 	}
 
 	public void setUserInfo(UserInfo userInfo) {
 		this.userInfo = userInfo;
 	}
 
 	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "complaint")
 	public Set<ComplaintNotes> getComplaintNotes() {
 		return complaintNotes;
 	}
 
 	public void setComplaintNotes(Set<ComplaintNotes> complaintNotes) {
 		this.complaintNotes = complaintNotes;
 	}
 
 }

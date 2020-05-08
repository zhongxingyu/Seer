 package com.ag.zhaisujie.model;
 
 import java.io.Serializable;
 
import org.json.JSONObject;

 /**
  *    Order.java
  *     <p>
  *     ʵϢ
  *     Copyright: Copyright(c) 2013 
  *     @author Gavin_Feng
  *     </p>
  * 
  */
 public class Order implements Serializable  {
 	private String taskId = "";
 	private String orderNumber = "";
 	private String content = "";
 	private String refuse = "";
 	private String linkman = "";
 	private String linkmobile = "";
 	private String address = "";
 	private double longitude;
 	private double latitude;
 	private String begin_time = "";
 	private int clean_hours;
 	private double price;
 	private String payStatus = "";
 	private String status = "";
 	
 	
 	public String getTaskId() {
 		return taskId;
 	}
 	public void setTaskId(String taskId) {
 		this.taskId = taskId;
 	}
 	
 	public String getContent() {
 		return content;
 	}
 	public void setContent(String content) {
 		this.content = content;
 	}
 	public String getRefuse() {
 		return refuse;
 	}
 	public void setRefuse(String refuse) {
 		this.refuse = refuse;
 	}
 	public String getLinkman() {
 		return linkman;
 	}
 	public void setLinkman(String linkman) {
 		this.linkman = linkman;
 	}
 	public String getLinkmobile() {
 		return linkmobile;
 	}
 	public void setLinkmobile(String linkmobile) {
 		this.linkmobile = linkmobile;
 	}
 	public String getAddress() {
 		return address;
 	}
 	public void setAddress(String address) {
 		this.address = address;
 	}
 	public double getLongitude() {
 		return longitude;
 	}
 	public void setLongitude(double longitude) {
 		this.longitude = longitude;
 	}
 	public double getLatitude() {
 		return latitude;
 	}
 	public void setLatitude(double latitude) {
 		this.latitude = latitude;
 	}
 	public String getBegin_time() {
 		return begin_time;
 	}
 	public void setBegin_time(String begin_time) {
 		this.begin_time = begin_time;
 	}
 	public int getClean_hours() {
 		return clean_hours;
 	}
 	public void setClean_hours(int clean_hours) {
 		this.clean_hours = clean_hours;
 	}
 	public String getOrderNumber() {
 		return orderNumber;
 	}
 	public void setOrderNumber(String orderNumber) {
 		this.orderNumber = orderNumber;
 	}
 	public double getPrice() {
 		return price;
 	}
 	public void setPrice(double price) {
 		this.price = price;
 	}
 	public String getPayStatus() {
 		return payStatus;
 	}
 	public void setPayStatus(String payStatus) {
 		this.payStatus = payStatus;
 	}
 	public String getStatus() {
 		return status;
 	}
 	public void setStatus(String status) {
 		this.status = status;
 	}
 	
	public String toJson(){
		String jsonStr="";	
		try{
			JSONObject userJob = new JSONObject(); 
			userJob.put("content", getContent());
			userJob.put("refuse", getRefuse());
			userJob.put("linkman", getLinkman());
			userJob.put("linkmobile", getLinkmobile());
			userJob.put("address", getAddress());
			userJob.put("longitude", getLongitude());
			userJob.put("latitude", getLatitude());
			userJob.put("begin_time", getBegin_time());
			userJob.put("clean_hours", getClean_hours());
			
			jsonStr=userJob.toString();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return jsonStr;
	}
 }

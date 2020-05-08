 package com.vaps.bean;
 
 import java.sql.Date;
 
 public class SalesList {
 	private int s_num;
 	private String s_id;
 	private String s_item_name;
 	private int s_buy_code;
 	private int s_buy_cnt;
 	private Date s_buy_date;
 	
 	public int getS_num() {
 		return s_num;
 	}
 	public void setS_num(int s_num) {
 		this.s_num = s_num;
 	}
 	public String getS_id() {
 		return s_id;
 	}
 	public void setS_id(String s_id) {
 		this.s_id = s_id;
 	}
 	public String getS_item_name() {
 		return s_item_name;
 	}
 	public void setS_item_name(String s_item_name) {
 		this.s_item_name = s_item_name;
 	}
 	public int getS_buy_code() {
 		return s_buy_code;
 	}
 	public void setS_buy_code(int s_buy_code) {
 		this.s_buy_code = s_buy_code;
 	}
 	public int getS_buy_cnt() {
 		return s_buy_cnt;
 	}
 	public void setS_buy_cnt(int s_buy_cnt) {
 		this.s_buy_cnt = s_buy_cnt;
 	}
 	public Date getS_buy_date() {
 		return s_buy_date;
 	}
 	public void setS_buy_date(Date s_buy_date) {
 		this.s_buy_date = s_buy_date;
 	}
 
 }

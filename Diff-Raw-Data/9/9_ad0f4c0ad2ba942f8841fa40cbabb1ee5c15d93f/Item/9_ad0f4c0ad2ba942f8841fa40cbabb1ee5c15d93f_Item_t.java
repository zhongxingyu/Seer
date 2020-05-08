 package com.youpony.amuse;
 
 import java.io.Serializable;
 
 public class Item implements Serializable{
 	

 	String name, author, year, description, mostra, url, id, type, bigPic, itemCommento;
	double emo;
	
 	int e_id;
 	Item(){
 	}
 	
 	@Override
 	public String toString(){
 		return name;
 	}
 	
 	public int returnId(){
 		return e_id;
 	}
 	
 	public String toUrl(){
 		return url;
 	}
 
 
 
 }

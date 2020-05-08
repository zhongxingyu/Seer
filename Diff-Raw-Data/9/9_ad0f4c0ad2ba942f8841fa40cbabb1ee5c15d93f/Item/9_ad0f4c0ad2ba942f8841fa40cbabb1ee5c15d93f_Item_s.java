 package com.youpony.amuse;
 
 import java.io.Serializable;
 
 public class Item implements Serializable{
 	
<<<<<<< HEAD
	String name, author, year, description, mostra, url, id, type;
	double emo;
=======
 	String name, author, year, description, mostra, url, id, type, bigPic, itemCommento;
>>>>>>> 0032b59659ff8b715035f6e9ce2f4033b6a4694e
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

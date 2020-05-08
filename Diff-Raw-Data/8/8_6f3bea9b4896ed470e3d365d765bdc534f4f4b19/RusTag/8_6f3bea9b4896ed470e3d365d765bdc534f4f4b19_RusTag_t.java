 package pro.trousev.cleer.android.service;
 
 import java.io.UnsupportedEncodingException;
 import java.nio.ByteBuffer;
 import java.nio.charset.Charset;
 
 public class RusTag {
 	public String change(String s){
 		if (s == null)
 			return null;
		if (s.isEmpty())
			return s;
 		Charset C= Charset.forName("windows-1251");
		ByteBuffer buffer=null;
		int i,x=0;
 		try{
			//TODO insert Akbar
 		}catch(UnsupportedEncodingException e){
 			return s;
 		}
 	}
 }

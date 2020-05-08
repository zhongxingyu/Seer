 package pro.trousev.cleer.android.service;
 
 import java.io.UnsupportedEncodingException;
 import java.nio.ByteBuffer;
 import java.nio.charset.Charset;
 
 public class RusTag {
 	public String change(String s){
 		if (s == null)
 			return null;
 		Charset C= Charset.forName("windows-1251");
 		try{
			return (C.decode(ByteBuffer.wrap(s.getBytes("UTF-16LE")))).toString();
 		}catch(UnsupportedEncodingException e){
 			return s;
 		}
 	}
 }

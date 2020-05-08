 package br.com.fastrequest.model;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class ValidaEmail {
 
 	public static boolean validaEmail(String email){
 		
		//Metodo valida�ao de email
		Pattern p; 
            p = Pattern.compile("^[\\w-]+(\\.[\\w-]+)*@([\\w-]+\\.)+[a-zA-Z]{2,7}$");
 		Matcher m = p.matcher(email);
 		if(m.find())
 			return true;
 		else
 			return false;
 	}
 
 }

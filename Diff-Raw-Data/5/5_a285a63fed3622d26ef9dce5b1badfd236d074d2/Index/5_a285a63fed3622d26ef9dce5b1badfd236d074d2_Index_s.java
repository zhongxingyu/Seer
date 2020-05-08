 package example.controller;
 
 import java.io.IOException;
 import juva.Controller;
 
 
 public class Index extends Controller {
 
 	final static String URL_PATTERN = "/Index";
 
 	public Index(){
 		super(URL_PATTERN);
 	}
 
	public void get() throws IOException{
 		putVar("home", "Shonenada");
 		render("test.html");
 	}
 	
 }

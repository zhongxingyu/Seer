 package example.controller;
 
 import java.io.IOException;
import java.sql.SQLException;

 import juva.Controller;
 
 
 public class Index extends Controller {
 
 	final static String URL_PATTERN = "/Index";
 
 	public Index(){
 		super(URL_PATTERN);
 	}
 
	public void get() throws IOException, SQLException{
 		putVar("home", "Shonenada");
 		render("test.html");
 	}
 	
 }

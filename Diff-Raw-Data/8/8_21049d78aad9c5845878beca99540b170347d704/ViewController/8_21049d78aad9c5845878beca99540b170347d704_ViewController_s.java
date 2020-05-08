 package controllers;
 
 import java.util.List;
 
 import models.Usuario;
 import play.data.validation.Valid;
 import play.i18n.Lang;
 import play.mvc.Controller;
 
 public class ViewController extends Controller{
 
 	public static void index(){
 		
 		Usuario u = new Usuario();
 		List<Usuario> usuarios = Usuario.findAll();
 		u.username = "Carlos";
 		render(u,usuarios);
 	}
 	
	public static void myForm(@Valid Usuario u){
 		
 		renderText("OK");
 	}
 }

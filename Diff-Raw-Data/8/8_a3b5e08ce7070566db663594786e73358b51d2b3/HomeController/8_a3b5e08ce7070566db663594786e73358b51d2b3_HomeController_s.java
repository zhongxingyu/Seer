 package br.com.classeencanto.controller;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import br.com.classeencanto.dao.ProdutoDAO;
 
 @Controller
 public class HomeController {
 
 	@Autowired
 	private ProdutoDAO produtoDao;
	
	@RequestMapping({"/", "/home"})
	public String execute(){
 		return "home";
 	}
	
 }

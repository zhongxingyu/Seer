 package com.country.controllers;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
import com.country.mappers.RecursoMapper;
 import com.country.form.RecursoForm;
 import com.country.hibernate.model.DataTable;
 import com.country.hibernate.model.Recurso;
import com.country.hibernate.model.Telefono;
 import com.country.services.ResourceManager;
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 @RequestMapping(value = "/recurso")
 public class RecursoController {
 
 	@Autowired
 	private ResourceManager recursoManager;
 	
 
 
 	@RequestMapping(value = "/create",method = RequestMethod.GET)
 	public String showForm(ModelMap model) {
 		RecursoForm form = new RecursoForm();
 		model.addAttribute("RECURSO", form);
 
 		return "recurso";
 	}
 
 	@RequestMapping(value = "/create",method = RequestMethod.POST)
 	public String processForm(
 			@ModelAttribute(value = "RECURSO") RecursoForm form,
 			BindingResult result) throws ParseException {
 		
 		if (result.hasErrors()) {
 			return "registration";
 		} else {
 			
 	   recursoManager.save(RecursoMapper.getRecurso(form),null);
 			return "success";
 		}
 			
 	}
 	
 	@RequestMapping(value = "/load/{id}", method = RequestMethod.GET)
 	public String load(ModelMap model,@PathVariable int id) throws ParseException {
 		Recurso recurso =recursoManager.findById(id);
 		
 		RecursoForm form = (RecursoForm) RecursoMapper.getForm(recurso);
 		model.addAttribute("RECURSO", form);
 		
 		return "forms/recursoForm";
 
 	}
 	
 	@RequestMapping(value = "/load/{id}", method = RequestMethod.POST)
 	public String update(@ModelAttribute(value = "RECURSO") RecursoForm form,@PathVariable int id,
 			BindingResult result) throws ParseException {
 		return "success";
 		
 
 	}
 	@RequestMapping(value = "/lista", method = RequestMethod.GET)
 	public  @ResponseBody DataTable getUserInJSON()  {
            
            DataTable dataTable=new DataTable();
    		System.out.println("Entrandoo!!  ");
 
 			for (Recurso recurso : recursoManager.listAll()) {
 				List <String> row =new ArrayList<String>();
 				row.add(String.valueOf(recurso.getId()));
 				row.add(recurso.getNombre());
 				row.add(recurso.getConcepto().getNombre());
 				row.add(recurso.getTipoRecurso().getNombre());
 				
 				dataTable.getAaData().add(row);
 			}
 
            dataTable.setsEcho("1");
            dataTable.setiTotalDisplayRecords("2");
            dataTable.setiTotalRecords("1");
            return dataTable;
 	}
 
 }

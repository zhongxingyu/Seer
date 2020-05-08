 package com.tda.model.utils;
 
 public enum FormType {
 	pediatrician("Pediatra"), socialworker("TrabajadorSocial"), nurse(
			"Enfermero"), dentist("Dentista"), admin("Admin");
 
 	private String description;
 
 	FormType(String description) {
 		this.setDescription(description);
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 }

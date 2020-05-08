 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Modelo;
 
 /**
  *
  * @author Estevo
  */
 public class Cd extends Producto{
     
     
 	private String nombre;
 	private String autor;
 	private String pais;
         private String anho;
 
     public Cd() {
        super();
        this.nombre = "";
        this.autor = "";
        this.pais = "";
        this.anho = "";
     }
 
 
     public Cd(String nombre, String autor, String pais,float precio, int stock, int id,String anho) {
         super(precio, stock, id);
         this.nombre = nombre;
         this.autor = autor;
         this.pais = pais;
         this.anho = anho;
        
     }
 
     public String getAnho() {
         return anho;
     }
 
     public void setAnho(String anho) {
         this.anho = anho;
     }
 
 	
 	
 	public void setNombre(String nombre){
 		this.nombre=nombre;
 	}
 
 	public String getNombre(){
 		return nombre;
 	}
 	
 
 	public void setAutor(String autor){
 		this.autor=autor;
 	}
 
 	public String getAutor(){
 		return autor;
 	}
 
 	public void setPais(String pais){
 		this.pais=pais;
 	}
 
 	public String getPais(){
 		return pais;
 	}
 
 	public void setPrecio(Float precio){
 		this.precio=precio;
 	}
 
 	
     
 }

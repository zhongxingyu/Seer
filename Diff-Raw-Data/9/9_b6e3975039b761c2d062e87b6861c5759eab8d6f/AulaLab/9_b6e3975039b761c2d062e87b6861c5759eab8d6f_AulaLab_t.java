 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Domini;
 
 /**
  *
  * @author Daniel
  */
 public class AulaLab extends Aula{
     
     private boolean material;
     
     /** 
     * Create a AulaLab.
     */
     public AulaLab (String nombre, int capacidad, boolean material) {
         super(nombre, capacidad);
         this.material = material;
     }
     
     /**
      *  
      * @return Returns if the AulaLab has material
      */
     public boolean GetMaterial(){
         return material;
     }
     
     /**
      *  
      * @param mar A material
      */    
    public void SetMaterial(boolean material){
         this.material = material;
     }
 }
 

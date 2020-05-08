 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 package practica2;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 
 /**
  *
  * @author Joan
  */
 class LlistaAssignatures implements AccesAssignatures {
 
     ArrayList<Assignatura> llistaAssignatures;
 
     public LlistaAssignatures() {
         llistaAssignatures = new ArrayList();
     }
 
     @Override
     public void inserirAssignatura(Assignatura a) {
         llistaAssignatures.add(a);
         Collections.sort(llistaAssignatures, new NomAssignaturaComparator());
     }
 
     @Override
     public void baixaAssignatura(Assignatura a) {
         llistaAssignatures.remove(a);
     }
 
     @Override
     public ArrayList<Assignatura> cercarOptatives() {
         ArrayList<Assignatura> aux = new ArrayList();
         Iterator<Assignatura> it = llistaAssignatures.iterator();
         
         while (it.hasNext()) {
             Assignatura a = it.next();
             
             if (a.getObligatorietat()== Assignatura.OBLIGATORIETAT.OPTATIVA) {
                 aux.add(a);
             }
         }
         return aux;
     }
 
     public ArrayList<Assignatura> cercarObligatories() {
         ArrayList<Assignatura> aux = new ArrayList();
         Iterator<Assignatura> it = llistaAssignatures.iterator();
         
         while (it.hasNext()) {
             Assignatura a = it.next();
             
             if (a.getObligatorietat()== Assignatura.OBLIGATORIETAT.OBLIGATORIA) {
                 aux.add(a);
             }
         }
         return aux;
     }
 
     public ArrayList<Assignatura> getLlistaAssignatures() {
         return llistaAssignatures;
     }
 
     @Override
     public boolean existeixAssignatura(int codi) {
         Iterator it= llistaAssignatures.iterator();
         while(it.hasNext()){
             Assignatura a= (Assignatura) it.next();
            if(a.getCodi()== codi){
                 return true;
             }
         }
         return false;
     }
     
     
 
    
 }

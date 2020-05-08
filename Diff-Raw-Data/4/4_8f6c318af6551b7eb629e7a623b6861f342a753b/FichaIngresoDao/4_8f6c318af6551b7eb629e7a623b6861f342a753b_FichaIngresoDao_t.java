 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.cmail.rehabilitacion.dao;
 
 import org.cmail.rehabilitacion.modelo.sira.FichaIngreso;
 import org.hibernate.Transaction;
 
 /**
  * Clase de acceso a datos para menejar las fichas de ingreso.
  * 
  * @author Noralma Vera
  * @author Doris Viñamagua
  * @version 1.0
  */
 public class FichaIngresoDao extends GanericDao<FichaIngreso> {
 
     /**
      * Constructor por defecto
      */
     public FichaIngresoDao() {
         super(FichaIngreso.class);
     }    
     
     /**
      * Guarda una ficha de ingreso
      * 
      * @param fichaIngreso la ficha de ingreso a guardar
      * @return true si se guardó correctamente
      */
     public boolean save(FichaIngreso fichaIngreso) {
         boolean b = false;
         Transaction tx = null;
         
         log.info("Guardando ficha....");
         
         try {
             tx = getSession().beginTransaction();
             
             //merge(instancia);
            if (fichaIngreso.getAdolescente().getMadre() != null){
                log.info("Madre: " + fichaIngreso.getAdolescente().getMadre().getNombres());
            }
             
             getSession().saveOrUpdate(merge(fichaIngreso.getAdolescente()));
             getSession().saveOrUpdate(merge(fichaIngreso));
             
             tx.commit();
             b=true;
         } catch (Exception e) {
             log.error("Error guardar ficha", e);
             if(tx !=null) tx.rollback();
             
             b = false;
         }
         
         return b;
     }
     
 }

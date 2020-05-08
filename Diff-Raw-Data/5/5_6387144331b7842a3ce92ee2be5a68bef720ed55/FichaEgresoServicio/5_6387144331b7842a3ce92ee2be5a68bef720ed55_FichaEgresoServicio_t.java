 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.cmail.rehabilitacion.servicio;
 
 import java.util.List;
 import org.cmail.rehabilitacion.dao.Dao;
 import org.cmail.rehabilitacion.modelo.sira.FichaEgreso;
 import org.cmail.rehabilitacion.modelo.Persona;
 import org.cmail.rehabilitacion.dao.hql.KProperty;
 
 /**
  * Clase de lógica de negocio para manejar fichas de egresos.
  * 
  * @author Noralma Vera
  * @author Doris Viñamagua
  * @version 1.0Usuario
  */
 public class FichaEgresoServicio extends GenericServicio<FichaEgreso> {
 
     /**Capa de acceso a datos*/
     private Dao dao = null;
     
     /**
      * Constructor por defecto
      */
     public FichaEgresoServicio() {
         super(FichaEgreso.class);
         dao = new Dao();
     }
     
     /**
      * Guarda una ficha de egreso en la base de datos
      * @param egreso la ficha de egreso
      * @return true si se guardó correctamente
      */
     public boolean guardar(FichaEgreso egreso){
         boolean b = false;
         try {
             dao.beginTransaction();
            dao.merge(egreso.getAdolescente());
            dao.merge(egreso.getAutorizaEgreso());
            dao.merge(egreso.getCompaneroEgreso());
            dao.merge(egreso.getResponsableEgreso());
            dao.merge(egreso.getFichaIngreso());
                         
             dao.saveOnTx(egreso);
             
             //Fijo el idFichaIngreso
             egreso.getFichaIngreso().setFichaEgreso(egreso);
             
             dao.saveOnTx(egreso.getFichaIngreso());
             
             
             dao.commit();       
             b = true;
         } catch (Exception e) {
             dao.rollback();
         }
         
         return b;
     }   
     
     /**
      * Lista las fichas de egreso donde la la cédula, los nombres o los apellidos contengan las cedenas respectivas.
      * @param cedula la cédula del adolescente
      * @param nombres los nombres del adolescente
      * @param apellidos los apellidos del adolescente
      * @return lista de fichas de egreso
      */
     public List<FichaEgreso> listarFichas(String cedula, String nombres, String apellidos) {
         return super.listarPorPropiedadesValoresLike(
                 new KProperty("adolescente.cedula", cedula),
                 new KProperty("adolescente.nombres", nombres),
                 new KProperty("adolescente.apellidos", apellidos));
     }
 
     /**
      * Crear una ficha de egreso la inicializa con valores por defecto.
      * 
      * @return la ficha de egreso
      */
     public FichaEgreso crearNueva() {
         FichaEgreso fichaEgreso = new FichaEgreso();
         fichaEgreso.setAdolescente(new Persona());
         fichaEgreso.getAdolescente().setPadre(new Persona());
         fichaEgreso.getAdolescente().setMadre(new Persona());
         return fichaEgreso;
     }
 }

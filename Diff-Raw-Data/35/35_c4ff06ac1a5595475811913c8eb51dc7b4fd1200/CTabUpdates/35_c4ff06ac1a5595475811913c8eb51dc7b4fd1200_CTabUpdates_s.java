 package com.stylishdb.qt.tabResultViews.controllers;
 
 import com.trolltech.qt.gui.QWidget;
 import com.stylishdb.db.ExecutorTextSQL;
 import com.stylishdb.db.ExecutorTextSQLException;
 import com.stylishdb.db.NoConnectionException;
 import com.stylishdb.db.domain.ResultUpdates;
 import com.stylishdb.domain.MConnection;
 import com.stylishdb.qt.controllers.Controller;
 import com.stylishdb.qt.modals.ModalAlert;
 import com.stylishdb.qt.tabResultViews.TabUpdates;
 import java.util.List;
 
 /**
  *
  ** @author StylishDB
  */
 public class CTabUpdates extends Controller {
     private ExecutorTextSQL manejadorConsulta;
     private TabUpdates pestanaConsultas;
     
     private MConnection mConexion;
     private List<String> consultasActualizar;
     
     public CTabUpdates(MConnection mConexion,
             List<String> consultasActualizar) throws ExecutorTextSQLException {
         super();
         
         this.mConexion = mConexion;
         this.consultasActualizar = consultasActualizar;
         
         crearControladoresYComponentes();
         ejecutarPasosLanzarQuery();
     }
 
     public QWidget getPestanaResultado() {
         return pestanaConsultas;
     }
     
     private void crearControladoresYComponentes() {
         manejadorConsulta = new ExecutorTextSQL(
                 mConexion,
                 consultasActualizar
         );
         
         pestanaConsultas = new TabUpdates(this);
     }
     
     private void ejecutarPasosLanzarQuery() throws ExecutorTextSQLException {
         if(conectarContraBaseDeDatos()) {
             ejecutarActualizarConsultas();
             pintarRespuestasActualizacion();
         }
     }
     
     private boolean conectarContraBaseDeDatos() {
         try {
             manejadorConsulta.conectarContraBaseDeDatos();
             return true;
         } catch (NoConnectionException ex) {
             mostrarErrorEnPantalla(
                     "Connection exception",
                     ex.getMessage()
             );
         }
         return false;
     }
     
     private void ejecutarActualizarConsultas() throws ExecutorTextSQLException {
         manejadorConsulta.actualizarConsultas();
     }
     
     private void pintarRespuestasActualizacion() {
         ResultUpdates resultadoActualizarConsultas =
                 manejadorConsulta.getResultadoActualizacionConsultas();
         
         pestanaConsultas.pintarResultado(
                 resultadoActualizarConsultas
         );
     }
     
     private void mostrarErrorEnPantalla(String tituloError, String mensajeError) {
         ModalAlert.mostrarErrorEnPantalla(tituloError,
                 mensajeError);
     }
 }

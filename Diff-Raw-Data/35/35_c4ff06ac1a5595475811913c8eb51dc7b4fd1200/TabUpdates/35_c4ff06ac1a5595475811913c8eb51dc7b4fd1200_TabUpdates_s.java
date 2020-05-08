 package com.stylishdb.qt.tabResultViews;
 
 import com.trolltech.qt.gui.QFont;
 import com.trolltech.qt.gui.QPlainTextEdit;
 import com.trolltech.qt.gui.QVBoxLayout;
 import com.trolltech.qt.gui.QWidget;
 import com.stylishdb.db.domain.ResultUpdates;
 import com.stylishdb.qt.tabResultViews.controllers.CTabUpdates;
 
 /**
  *
  ** @author StylishDB
  */
 public class TabUpdates extends QWidget {
     
     private QPlainTextEdit textResultadoActualizaciones;
     
     private CTabUpdates controlador;
     
     public TabUpdates(CTabUpdates controlador) {
         this.controlador = controlador;
         
         crearComponentesInterfaz();
         
         posicionarComponentesInterfaz();
     }
     
     private void crearComponentesInterfaz() {
         textResultadoActualizaciones = new QPlainTextEdit();
         
         textResultadoActualizaciones.setLineWrapMode(QPlainTextEdit.LineWrapMode.NoWrap);
         textResultadoActualizaciones.setReadOnly(true);
         textResultadoActualizaciones.setUndoRedoEnabled(false);
         
         QFont fuenteTextoPlano = new QFont();
         fuenteTextoPlano.setFixedPitch(true);
         fuenteTextoPlano.setFamily("monospacedmonospaced");
         textResultadoActualizaciones.setFont(fuenteTextoPlano);
     }
 
     private void posicionarComponentesInterfaz() {
         QVBoxLayout ventanaLayout = new QVBoxLayout();
         
         ventanaLayout.addWidget(textResultadoActualizaciones);
         
         setLayout(ventanaLayout);
     }
     
     public void pintarResultado(ResultUpdates resultadoActualizarConsultas) {
         StringBuilder strResultado = new StringBuilder();
         
         strResultado.append("Resultado de actualizar la base de datos:\n\n");
         strResultado.append("Tiempo en lanzar las consultas: ").append(
                 resultadoActualizarConsultas.tiempoActualizacionConsultas)
                 .append("msg\n");
         strResultado.append("Tiempo en conectar contra base de datos: ").append(
                 resultadoActualizarConsultas.tiempoParaConectarContraBaseDeDatos).
                 append("msg\n");
         strResultado.append("\n");
         
         int numConsultas = resultadoActualizarConsultas.consultasSQLActualizar.size();
         for(int i=0; i<numConsultas; i++) {
             String consultaSQL = resultadoActualizarConsultas.
                     consultasSQLActualizar.get(i);
             int numFilasAfectadas = resultadoActualizarConsultas.
                     numeroFilasAfectadas.get(i);
             strResultado.append("Filas afectadas; ").append(numFilasAfectadas).
                     append(" (Consulta SQL: ").append(consultaSQL).append(")\n");
         }
         
         textResultadoActualizaciones.setPlainText(strResultado.toString());
     }
     
 }

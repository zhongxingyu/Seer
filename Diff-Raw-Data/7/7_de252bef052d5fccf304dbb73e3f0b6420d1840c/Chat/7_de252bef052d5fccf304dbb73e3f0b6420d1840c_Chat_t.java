 /*
  *  Chat.java
  *  Copyright (C) 2012  Diego Estévez <dgmvecuador@gmail.com>
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.educautecisystems.intefaz;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.image.BufferedImage;
 import java.beans.PropertyVetoException;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.DefaultListModel;
 import javax.swing.JFileChooser;
 import javax.swing.Timer;
 import org.educautecisystems.core.Sistema;
 import org.educautecisystems.core.chat.cliente.ClienteServidorChat;
 import org.educautecisystems.core.chat.elements.FileChat;
 import org.educautecisystems.core.chat.elements.UserChat;
 
 /**
  *
  * @author Shadow2012
  */
 public final class Chat extends javax.swing.JInternalFrame {
     private VentanaPrincipal ventanaPrincipal;
 	private boolean esDocente;
     private final StringBuffer logChat = new StringBuffer();
     private ClienteServidorChat clienteServidorChat;
     private ArrayList <UserChat> usuarios;
 	private ArrayList <FileChat> archivos;
 	DefaultListModel listaArchivosModelo = new DefaultListModel();
     private long actualSize = 0;
 	private long actualSizeListaUsuarios = 0;
 	private long actualSizeListaArchivos = 0;
 	private final PantallaProfesor pantallaProfesor = new PantallaProfesor(this);
 	private BufferedImage pantallaActual = null;
     
     /**
      * Creates new form ChaPrueba
      */
     public Chat( VentanaPrincipal ventanaPrincipal, boolean esDocente ) {
         initComponents();
         this.ventanaPrincipal = ventanaPrincipal;
 		this.esDocente = esDocente;
         clienteServidorChat = new ClienteServidorChat(this);
         activarBotones(false);
         clienteServidorChat.start();
         usuarios = null;
 		
 		if ( !esDocente ) {
 			/* Generar pantalla. */
 			ventanaPrincipal.insertarNuevaVentana(pantallaProfesor);
 			pantallaProfesor.setVisible(true);
 			btnGenerarReporteAsistencia.setVisible(false);
 			
 			try {
 				pantallaProfesor.setMaximum(true);
 			} catch (PropertyVetoException ex) {
 				
 			}
 			
 			Timer actualizarPantallaProfesor = new Timer(1000, new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					actualizarPantallaProfessor();
 				}
 			});
 			actualizarPantallaProfesor.start();
 		}
 		
         Timer actualizadorChat = new Timer(500, new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 actualizarChat();
             }
         });
         actualizadorChat.start();
 		Timer actualizarListaUsuariosTimer = new Timer(2000, new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				actualizarListaUsuarios();
 			}
 		});
 		actualizarListaUsuariosTimer.start();
 		Timer actualizarListaArchivosTimer = new Timer(2000, new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				actualizarListaArchivos();
 			}
 		});
 		actualizarListaArchivosTimer.start();
     }
 	
 	public boolean esChatDocente () {
 		return esDocente;
 	}
 	
 	private void actualizarPantallaProfessor() {
 		synchronized ( pantallaProfesor ) {
 			if ( pantallaActual == null ) {
 				return;
 			}
 			pantallaProfesor.actualizarPantallaProfesor(pantallaActual);
 		}
 	}
     
 	public void nuevaPantallaProfesor ( BufferedImage nuevaPantalla ) {
 		synchronized( pantallaProfesor ) {
 			this.pantallaActual = nuevaPantalla;
 		}
 	}
 	
     public void mostrarError( String txt ) {
         synchronized( logChat ) {
             String mensaje = "<font color=\"red\"><b>Error: </b>" + txt + "</font><br/>";
             logChat.append(mensaje);
         }
     }
     
     public void mostrarInfo( String txt ) {
         synchronized( logChat ) {
             String mensaje = "<font color=\"blue\"><b>Info: </b>" + txt + "</font><br/>";
             logChat.append(mensaje);
         }
     }
     
     public void activarBotones( boolean b ) {
         txtTexto.setEnabled(b);
         btnEnviar.setEnabled(b);
     }
     
     public void recibirMensaje ( String userIdString, String mensaje ) {
         String userName = null;
         synchronized ( this ) {
             int userId = 0;
             try {
                 userId = Integer.parseInt(userIdString);
             } catch( NumberFormatException nfe ) {
                 this.mostrarError("Id de usuario no encontrado.");
                 return;
             }
             
             /* Buascar el nombre del usuario. */
             for ( UserChat userChat:usuarios ) {
                 if ( userChat.getId() == userId ) {
                     userName = userChat.getNickName();
                 }
             }
         }
         
         synchronized ( logChat ) {
             String directorioActual = dameDiretorioActual();
             File imgs = new File(directorioActual, "img");
             File emoticon = new File(imgs, "Emoticon_sorpresa.jpg");
             if (!emoticon.exists()) {
                 System.err.println("No existe imagen.\n\t" + emoticon.getAbsolutePath());
                 return;
             }
             String regex_emoticon = emoticon.getAbsolutePath().
                     replaceAll("\\\\", "\\\\\\\\").replaceAll(":", "|");
 
             String salida = mensaje.
                     replaceAll(":o", "<img src=\"file:///" + regex_emoticon + "\"/>").
                     replaceAll("\\b(www\\.[^ ]+\\.com)\\b", "<a href=\"http://$1\">$1</a>").
                     replaceAll("\\bN[iI]ck\\b", "<b>$0</b>").
 					replace(" ", "&nbsp;");
             logChat.append("<font color=\"black\"><b><i>").append(userName).append(":</i></b>&nbsp;").append(salida).append("</font><br>\n");
         }
     }
     
     private String dameDiretorioActual() {
         return System.getProperty("user.dir");
     }
     
     private void enviarMensaje() {
         synchronized ( logChat ) {
             String texto = txtTexto.getText();
             //recibirMensaje("Nick", texto);
             clienteServidorChat.enviarMensaje(texto);
             txtTexto.setText("");
         }
     }
     
     private void actualizarChat() {
         synchronized( logChat ) {
             if ( actualSize != logChat.length() ) {
                 contenidoChat.setText("<html><body>"+logChat.toString()+"</body></html>");
                 contenidoChat.setCaretPosition(contenidoChat.getDocument().getLength());
                 actualSize = logChat.length();
             }
         }
     }
 	
 	private void actualizarListaUsuarios() {
 		synchronized ( this ) {
 			StringBuilder listaUsuarios = new StringBuilder();
 			for ( UserChat usuarioChat:usuarios ) {
 				listaUsuarios.append("<font color=\"green\"><i><b>"+usuarioChat.getNickName()+"&nbsp;</b></i><font>");
 				
 				/* Exconcer los nombres cuando no son docentes. */
 				if ( esDocente ) {
 					listaUsuarios.append("(<font color=\"blue\">"+usuarioChat.getRealName()+")</font><br/>");
 				} else {
 					listaUsuarios.append("<br/>");
 				}
 				
 			}
 			
 			/* No hacer nada hasta que se actualice el mensaje. */
 			if ( actualSizeListaUsuarios == listaUsuarios.toString().length() ) {
 				return;
 			}
 			
 			txtListaUsuarios.setText(listaUsuarios.toString());
 			actualSizeListaUsuarios = listaUsuarios.toString().length();
 		}
 	}
 	
 	private void actualizarListaArchivos () {
 		synchronized(this) {
 			StringBuilder comprobador = new StringBuilder();
 			
 			for ( FileChat fileChat:archivos ) {
 				comprobador.append(fileChat.toString());
 			}
 			
 			/* No actualizar la lista si no es necesario. */
 			if ( actualSizeListaArchivos == comprobador.toString().length() ) {
 				return;
 			}
 			
 			listaArchivosModelo.clear();
 			
 			
 			for ( FileChat fileChat:archivos ) {
 				listaArchivosModelo.addElement(fileChat);
 			}
 			actualSizeListaArchivos = comprobador.toString().length();
 		}
 	}
     
     public void nuevaLista( ArrayList <UserChat> usuarios ) {
         synchronized( this ) {
             this.usuarios = usuarios;
         }
     }
 	
 	public void nuevaListaArchivos ( ArrayList <FileChat> archivos ) {
 		synchronized ( this ) {
 			this.archivos = archivos;
 		}
 	}
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jLabel1 = new javax.swing.JLabel();
         jScrollPane1 = new javax.swing.JScrollPane();
         contenidoChat = new javax.swing.JEditorPane();
         txtTexto = new javax.swing.JTextField();
         btnEnviar = new javax.swing.JButton();
         jLabel2 = new javax.swing.JLabel();
         jScrollPane3 = new javax.swing.JScrollPane();
         txtListaUsuarios = new javax.swing.JEditorPane();
         btnCerrarSesion = new javax.swing.JButton();
         jLabel3 = new javax.swing.JLabel();
         jScrollPane2 = new javax.swing.JScrollPane();
         listaArchivos = new javax.swing.JList();
         btnDescargar = new javax.swing.JButton();
         btnGenerarReporteAsistencia = new javax.swing.JButton();
 
         setClosable(true);
         setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
         setTitle("Chat");
 
         jLabel1.setText("Charla:");
 
         contenidoChat.setEditable(false);
         contenidoChat.setContentType("text/html"); // NOI18N
         jScrollPane1.setViewportView(contenidoChat);
 
         txtTexto.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 txtTextoKeyPressed(evt);
             }
         });
 
         btnEnviar.setText("Enviar");
         btnEnviar.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnEnviarActionPerformed(evt);
             }
         });
 
         jLabel2.setText("Lista de usuarios:");
 
         txtListaUsuarios.setEditable(false);
         txtListaUsuarios.setContentType("text/html"); // NOI18N
         jScrollPane3.setViewportView(txtListaUsuarios);
 
         btnCerrarSesion.setText("Cerrar Sesión");
         btnCerrarSesion.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnCerrarSesionActionPerformed(evt);
             }
         });
 
         jLabel3.setText("Lista de archivos:");
 
         listaArchivos.setModel(listaArchivosModelo);
         listaArchivos.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         jScrollPane2.setViewportView(listaArchivos);
 
         btnDescargar.setText("Descargar");
         btnDescargar.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnDescargarActionPerformed(evt);
             }
         });
 
         btnGenerarReporteAsistencia.setText("Generar Reporte");
         btnGenerarReporteAsistencia.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnGenerarReporteAsistenciaActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(txtTexto)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(btnEnviar)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(btnCerrarSesion))
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 388, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jScrollPane3)
                             .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                             .addGroup(layout.createSequentialGroup()
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(jLabel2)
                                     .addComponent(jLabel3)
                                     .addGroup(layout.createSequentialGroup()
                                         .addComponent(btnDescargar)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                         .addComponent(btnGenerarReporteAsistencia)))
                                 .addGap(0, 0, Short.MAX_VALUE))))
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jLabel1)
                         .addGap(0, 0, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(jLabel1)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jLabel2)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jLabel3)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(btnDescargar)
                             .addComponent(btnGenerarReporteAsistencia)))
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 401, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(txtTexto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnEnviar)
                     .addComponent(btnCerrarSesion)))
         );
 
         java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
         setBounds((screenSize.width-736)/2, (screenSize.height-482)/2, 736, 482);
     }// </editor-fold>//GEN-END:initComponents
 
     private void btnEnviarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEnviarActionPerformed
         enviarMensaje();
     }//GEN-LAST:event_btnEnviarActionPerformed
 
     private void txtTextoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtTextoKeyPressed
         if ( evt.getKeyCode() == KeyEvent.VK_ENTER ) {
             enviarMensaje();
         }
     }//GEN-LAST:event_txtTextoKeyPressed
 
     private void btnCerrarSesionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCerrarSesionActionPerformed
         clienteServidorChat.cerrarSesion();
		
		/* Cerrar pantalla, si tiene. */
		if ( !esDocente ) {
			pantallaProfesor.setVisible(false);
			pantallaProfesor.dispose();
		}
		
 		this.setVisible(false);
 		this.dispose();
     }//GEN-LAST:event_btnCerrarSesionActionPerformed
 
     private void btnDescargarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDescargarActionPerformed
 		FileChat fileChat = (FileChat) listaArchivos.getSelectedValue();
 		
 		/* Revisar si existe algún elemento seleccionado. */
 		if ( fileChat == null ) {
 			Sistema.mostrarMensajeError("Por favor seleccione un archivo.");
 			return;
 		}
 		
 		JFileChooser fc = new JFileChooser();
 		fc.setDialogTitle("Seleccione donde guardar el archivo.");
 		fc.setSelectedFile(new File(fc.getCurrentDirectory(), fileChat.getName()));
 		int respuesta = fc.showSaveDialog(this);
 		
 		if( respuesta == JFileChooser.APPROVE_OPTION ) {
 			File archivo = fc.getSelectedFile();
 			clienteServidorChat.descargarArchivo(fileChat, archivo);
 		}
     }//GEN-LAST:event_btnDescargarActionPerformed
 
     private void btnGenerarReporteAsistenciaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGenerarReporteAsistenciaActionPerformed
         String defaultReport = "Asistencia.html";
 		
 		JFileChooser fc = new JFileChooser();
 		fc.setDialogTitle("Seleccione donde guardar el archivo.");
 		fc.setSelectedFile(new File(fc.getCurrentDirectory(), defaultReport));
 		int respuesta = fc.showSaveDialog(this);
 		
 		if( respuesta == JFileChooser.APPROVE_OPTION ) {
 			File archivo = fc.getSelectedFile();
 			StringBuilder htmlAsistencia = new StringBuilder();
 			Date fechaActual = new Date();
 			
 			htmlAsistencia.append("<html>");
 			htmlAsistencia.append("<head><meta name=\"tipo_contenido\" content=\"text/html;\" http-equiv=\"content-type\" charset=\"utf-8\"><title>Reporte Asistencia - "+fechaActual+"</title></head>");
 			htmlAsistencia.append("<body>");
 			htmlAsistencia.append("<div align=\"center\">");
 			htmlAsistencia.append("<h1>Reporte de asistencia sistema EducaUteciSystems</h1></br></br>");
 			htmlAsistencia.append("<b>Creado en: </b>"+fechaActual+"</br></br>");
 			htmlAsistencia.append("<table border=\"1\" width=\"500px\">");
 			htmlAsistencia.append("<tr><td><b>Nombre Real</b></td><td><b>Apodo en Chat</b></td></tr>");
 			synchronized( this ) {
 				for ( UserChat usuario:usuarios ) {
 					htmlAsistencia.append("<tr><td>"+usuario.getRealName()+"</td><td>"+usuario.getNickName()+"</td></tr>");
 				}
 			}
 			htmlAsistencia.append("</table>");
 			htmlAsistencia.append("</div>");
 			htmlAsistencia.append("</body>");
 			htmlAsistencia.append("</html>");
 			
 			/* Borrar si se tiene ptoblemas. */
 			if ( archivo.exists() ) {
 				if ( !archivo.delete() ) {
 					mostrarError("No se pudo guardar resporte de asistencia.");
 					return;
 				}
 			}
 			
 			/* Guardar archivo. */
 			try {
 				FileOutputStream fos = new FileOutputStream(archivo);
 				fos.write(htmlAsistencia.toString().getBytes());
 				fos.flush();
 				fos.close();
 			} catch (FileNotFoundException ex) {
 				mostrarError("No se ha encontrado la ruta para guardar.");
 				return;
 			} catch ( IOException ioe ) {
 				mostrarError("Error de estrada/salida al escribir archivo.");
 				return;
 			}
 			Sistema.mostrarMensajeInformativo("Se ha guardado información con éxito.");
 		}
     }//GEN-LAST:event_btnGenerarReporteAsistenciaActionPerformed
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnCerrarSesion;
     private javax.swing.JButton btnDescargar;
     private javax.swing.JButton btnEnviar;
     private javax.swing.JButton btnGenerarReporteAsistencia;
     private javax.swing.JEditorPane contenidoChat;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JList listaArchivos;
     private javax.swing.JEditorPane txtListaUsuarios;
     private javax.swing.JTextField txtTexto;
     // End of variables declaration//GEN-END:variables
 }

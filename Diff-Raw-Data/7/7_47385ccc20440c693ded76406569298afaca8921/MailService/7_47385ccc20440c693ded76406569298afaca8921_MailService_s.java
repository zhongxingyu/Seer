 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package MailService;
 
 import dbhandler.DbHandler;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import messages.msgEmail;
 import piserver.MsgListener;
 import piserver.PIServer;
 import piserver.ServerConstants;
 
 /**
  *
  * @author julianacb
  */
 public class MailService extends Thread implements MsgListener {
 
     public MailService() {
     }
 
     @Override
     public void run() {
         try {
 
             // crear objeto ServerSocket para las conexiones entrantes
             ServerSocket socketServer = new ServerSocket(
                     ServerConstants.SERVER_PORT, 100);
 
             PIServer.addLog("Servicio de correo inicializado en el puerto "
                     + ServerConstants.SERVER_PORT + " ...");
 
             // escuchar constantemente en espera de clientes
             while (true) {
 
                 // aceptar nueva conexi�n de cliente
                 Socket socketClient = socketServer.accept();
 
                 // crear nuevo objeto SubprocesoReceptor para recibir
                 // mensajes del cliente
                 new MailListener(this, socketClient).start();
 
                 // imprimir informaci�n sobre la conexi�n
                 PIServer.addLog("Conexion recibida de: "
                         + socketClient.getInetAddress());
 
             } // fin de instrucci�n while     
 
         } // fin del bloque try
         // manejar posible excepci�n al crear servidor y conectar clientes
         catch (java.net.BindException e){
             PIServer.addError("El servidor ya está inicializado");
         }
         catch (IOException excepcionES) {
             excepcionES.printStackTrace();
         }
     }
 
     @Override
     public void inMsg(Object obj) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void inMsg(Object obj, Socket clientSocket) {
 
        if (obj.getClass() == msgEmail.class) {
             
             if(!DbHandler.incomingMail((msgEmail) obj)){
                 
                new MailSender(this, clientSocket, "Error, el correo del destinatario no existe").start();
                 
             }
         } else {
             System.out.print("Error de objeto");
         }
 
     }
 }

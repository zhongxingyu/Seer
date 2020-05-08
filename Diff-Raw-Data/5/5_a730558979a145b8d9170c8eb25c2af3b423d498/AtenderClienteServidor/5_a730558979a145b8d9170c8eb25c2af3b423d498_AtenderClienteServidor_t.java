 /*
  *  AtenderClienteServidor.java
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
 package org.educautecisystems.core.chat;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.util.ArrayList;
 import org.educautecisystems.core.Sistema;
 import org.educautecisystems.core.chat.elements.ChatConstants;
 import org.educautecisystems.core.chat.elements.MessageHeaderParser;
 import org.educautecisystems.core.chat.elements.UserChat;
 
 /**
  *
  * @author dgmv
  */
 public class AtenderClienteServidor extends Thread {
 	/* Flujos del cliente */
 	private Socket clienteSocket;
 	private InputStream entrada = null;
 	private OutputStream salida = null;
 	private boolean continuar;
 	private UserChat nuevoUsuario = null;
 	
 	/* Elmeentos de comunicación */
 	private LogChatManager logChatManager;
 	private ServidorChat servidorChat;
 	
 	public static int number = 0;
 
 	public AtenderClienteServidor(Socket clienteSocket, LogChatManager logChatManager, ServidorChat servidorChat ) {
 		this.clienteSocket = clienteSocket;
 		continuar = true;
 		this.logChatManager = logChatManager;
 		this.servidorChat = servidorChat;
 	}
 	
 	public boolean esCliente( int idUsuario ) {
 		return true;
 	}
 
 	@Override
 	public void run() {
 		/* Evitar que problemas */
 		if ( logChatManager == null ) {
 			System.err.println("No existe interface para el log.");
 			System.exit(-1);
 			return;
 		}
 		if ( servidorChat == null ) {
 			logChatManager.logError("No existe un servidor con el cual comuinicarse.");
 			detenerCliente();
 			return;
 		}
 		
 		try {
 			if (clienteSocket == null) {
 				return;
 			}
 			
 			entrada = clienteSocket.getInputStream();
 			salida = clienteSocket.getOutputStream();
 			
 			MessageHeaderParser header = MessageHeaderParser.parseMessageHeader(entrada);
 			if (!header.getCommand().equals(ChatConstants.CHAT_HEADER_MAIN_COMMAND)) {
 				logChatManager.logError("Formato incorrecto.");
 				return;
 			}
 			
 			if (!header.getVar(ChatConstants.LABEL_COMMAND).equals(ChatConstants.COMMAND_LOGIN)) {
 				procesarRequerimiento(header);
 				return;
 			}
 			
 			String realName = header.getVar(ChatConstants.LABEL_REAL_NAME);
 			String nickName = header.getVar(ChatConstants.LABEL_NICKNAME);
 			
 			logChatManager.logInfo("Entra nuevo usuario: "+nickName+"("+realName+")");
 			
 			String token = generarToken();
 			int idUsuario = number;
 			
 			String response = "";
 			response += generateHeaderValue(ChatConstants.CHAT_HEADER_RESPONSE_COMMAND,
 					ChatConstants.RESPONSE_OK);
 			response += generateHeaderValue(ChatConstants.LABEL_USER_ID, ""+idUsuario);
 			response += generateHeaderValue(ChatConstants.LABEL_USER_TOKEN, token);
 			
 			salida.write(response.getBytes());
 			salida.flush();
 			
 			nuevoUsuario = new UserChat();
 			nuevoUsuario.setRealName(realName);
 			nuevoUsuario.setNickName(nickName);
 			nuevoUsuario.setToken(token);
 			
 			servidorChat.insertarUsuario(nuevoUsuario);
 			
 			while (continuar) {
 				/* TODO Enviar mensajes de chat. */
 			}
 			
 			/* Quitar usuario de la lista si se pierde la conexión. */
 			servidorChat.quitarUsuario(nuevoUsuario);
 		} catch (Exception e) {
 			logChatManager.logError("Problema en la atención a un cliente: "+e);
 			
 			/* Quitar usuario de la lista si se pierde la conexión. */
 			if ( nuevoUsuario != null ) {
 				servidorChat.quitarUsuario(nuevoUsuario);
 			}
 			
 			detenerCliente();
 		}
 		
 		detenerCliente();
 	}
 	
 	public boolean detenerUsuarioToken ( String token ) {
 		if ( nuevoUsuario != null && nuevoUsuario.getToken().equals(token) ) {
 			detenerCliente();
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean detenerUsuario ( UserChat userChat ) {
 		if ( nuevoUsuario != null && userChat.getId() == nuevoUsuario.getId() ) {
 			detenerCliente();
 			return true;
 		}
 		
 		return false;
 	}
 	
 	private String generarToken() {
 		number++;
 		return Sistema.getMD5("TOKEN-SALT"+number);
 	}
 
 	private void procesarRequerimiento(MessageHeaderParser header) throws Exception {
 		/* List of users */
 		if ( header.getVar(ChatConstants.LABEL_COMMAND).equals(ChatConstants.COMMAND_GET_USERS) ) {
 			String userToken =	header.getVar(ChatConstants.LABEL_USER_TOKEN);
 			String format =		header.getVar(ChatConstants.LABEL_FORMAT);
 			
 			/* Must be a valid token */
 			if ( !ServidorChat.testToken(userToken) ) {
 				logChatManager.logError("Un usuario no registrado a intentado acceder a información del chat.");
				sendResponseError("User not found.");
 				return;
 			}
 			
 			/* Only XML is valid */
 			if ( !format.equals("XML") ) {
 				logChatManager.logError("No se soporta el formato: " + format);
				sendResponseError("Format not supported.");
 				return;
 			}
 			
 			ArrayList<UserChat> users = ServidorChat.getUserList();
 			String xmlUsers = UserChat.generateXMLFromList(users);
 			long size = xmlUsers.getBytes().length;
 			System.out.println(xmlUsers);
 			
 			/* Genering response */
 			String headerResponse = "";
 			headerResponse += 
 					generateHeaderValue(ChatConstants.CHAT_HEADER_RESPONSE_COMMAND,
 					ChatConstants.RESPONSE_OK);
 			headerResponse +=
 					generateHeaderValue(ChatConstants.LABEL_CONTENT_LENGHT, ""+size);
 			headerResponse += ChatConstants.CHAT_END_HEADER;
 			headerResponse += xmlUsers;
 			
 			salida.write(headerResponse.getBytes());
 			salida.flush();
 			
 			detenerCliente();
 			return;
 		}
 		
 		if ( header.getVar(ChatConstants.LABEL_COMMAND).equals(ChatConstants.COMMAND_LOGOUT) ) {
 			String userToken = header.getVar(ChatConstants.LABEL_USER_TOKEN);
 			
 			if ( !ServidorChat.testToken(userToken) ) {
 				logChatManager.logError("Un usuario no registrado a intentado acceder a información del chat.");
 				detenerCliente();
 				return;
 			}
 			
 			if (servidorChat.logoutCliente(userToken)) {
 				logChatManager.logInfo("A salido el usuario con el token: "+userToken);
 				sendResponseOk();
 			} else {
 				logChatManager.logWarning("Se ha intendo cerrar la sesión de un usuario que no existe.");
 				sendResponseError("User not found.");
 			}
 			return;
 		}
 		
 		sendResponseError("Command not valid.");
 		detenerCliente();
 	}
 	
 	private void sendResponseOk () throws Exception {
 		StringBuilder response = new StringBuilder();
 		response.append(generateHeaderValue(ChatConstants.CHAT_HEADER_RESPONSE_COMMAND,
 					ChatConstants.RESPONSE_OK));
 		response.append(ChatConstants.CHAT_END_HEADER);
 		
 		salida.write(response.toString().getBytes());
 		salida.flush();
 		
 		detenerCliente();
 	}
 	
 	private void sendResponseError ( String description ) throws Exception {
 		StringBuilder response = new StringBuilder();
 		response.append(generateHeaderValue(ChatConstants.CHAT_HEADER_RESPONSE_COMMAND,
 					ChatConstants.RESPONSE_ERROR));
 		response.append(generateHeaderValue(ChatConstants.LABEL_DESCRIPTION, description));
 		response.append(ChatConstants.CHAT_END_HEADER);
 		
 		salida.write(response.toString().getBytes());
 		salida.flush();
 		
 		detenerCliente();
 	}
 	
 	private String generateHeaderValue( String name, String val ) {
 		return name + ": " + val + "\r\n";
 	}
 
 	public boolean estaCorriendo() {
 		return continuar;
 	}
 
 	public void detenerCliente() {
 		continuar = false;
 
 		try {
 			if ( entrada != null ) {
 				entrada.close();
 				entrada = null;
 			}
 			if ( salida != null ) {
 				salida.close();
 				salida = null;
 			}
 			
 			if ( clienteSocket != null ) {
 				clienteSocket.close();
 				clienteSocket = null;
 			}
 		} catch (Exception e) {
 		}
 	}
 }

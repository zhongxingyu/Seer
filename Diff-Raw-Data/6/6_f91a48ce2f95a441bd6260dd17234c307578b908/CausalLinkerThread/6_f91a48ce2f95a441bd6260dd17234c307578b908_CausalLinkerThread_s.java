 package panchat.listeners;
 
 import java.io.IOException;
 import java.net.MulticastSocket;
 
 import panchat.Panchat;
 import panchat.data.Canal;
 import panchat.data.ListaCanales;
 import panchat.data.Usuario;
 import panchat.linker.CausalLinker;
 import panchat.messages.CausalMessage;
 import panchat.messages.InscripcionCanal;
 import panchat.messages.MessageChat;
 import panchat.messages.SaludoListaCanales;
 
 public class CausalLinkerThread extends Thread {
 
 	public final static boolean DEBUG = false;
 
 	private MulticastSocket socket;
 
 	private Panchat panchat;
 
 	private CausalLinker causalLinker;
 
 	public CausalLinkerThread(MulticastSocket pSocket, Panchat pPanchat) {
 		this.socket = pSocket;
 		this.panchat = pPanchat;
 		this.causalLinker = panchat.getCausalLinker();
 	}
 
 	public void run() {
 
 		while (!socket.isClosed()) {
 
 			try {
 				// Leyendo objeto
 
 				CausalMessage cm = causalLinker.handleMsg();
 
 				Object objeto = cm.getContent();
 
 				printDebug("objeto leído");
 
 				if (objeto instanceof SaludoListaCanales) {
 
 					printDebug("SaludoListaCanales");
 
 					registrarSaludoCanales((SaludoListaCanales) objeto, cm
 							.getUsuario());
 
 				} else if (objeto instanceof InscripcionCanal) {
 
 					printDebug("InscripcionCanal");
 
 					inscribirCanal((InscripcionCanal) objeto, cm.getUsuario());
 
 				} else if (objeto instanceof MessageChat) {
 
 					printDebug("MessageChat");
 
 					escribirMensajeCanal((MessageChat) objeto);
 
 				} else if (objeto instanceof String) {
 
 					printDebug("Message");
 
 					escribirMensaje((String) objeto, cm.getUsuario());
 				}
 
 			} catch (IOException e1) {
 				// Se ha cerrado el socket
 			}
 		}
 
 		printDebug("Terminado");
 	}
 
 	private void registrarSaludoCanales(SaludoListaCanales saludo,
 			Usuario usuario) {
 
 		printDebug("Saludo canal recibido");
 
 		ListaCanales listaCanales = panchat.getListaCanales();
 
 		for (Canal canal : saludo.lista) {
 
 			Usuario usuarioObtenido = panchat.getListaUsuarios().getUsuario(
 					usuario.uuid);
 
 			Canal canalObtenido = listaCanales.getCanal(canal.getNombreCanal());
 
 			if (canalObtenido == null) {
 
 				canalObtenido = new Canal(canal.getNombreCanal(), panchat
 						.getListaUsuarios());
 
 				listaCanales.añadirCanal(canalObtenido);
 			}
 
 			canalObtenido.anyadirUsuarioConectado(usuarioObtenido);
 
 			printDebug("añadiendo usuario " + usuarioObtenido + " al canal : "
 					+ canalObtenido.toString());
 
 		}
 
 		listaCanales.canalModificado();
 	}
 
 	private void inscribirCanal(InscripcionCanal inscripcion, Usuario usuario) {
 
 		ListaCanales listaCanales = panchat.getListaCanales();
 
 		Usuario usuarioInscripcionObtenido = panchat.getListaUsuarios()
 				.getUsuario(inscripcion.usuario.uuid);
 		Usuario usuarioEnvioObtenido = panchat.getListaUsuarios().getUsuario(
 				usuario.uuid);
 
 		Canal canalObtenido = listaCanales.getCanal(inscripcion.canal
 				.getNombreCanal());
 
 		printDebug("usuarioInscripcionObtenido : " + usuarioInscripcionObtenido);
 		printDebug("usuarioEnvioObtenido : " + usuarioEnvioObtenido);
 		printDebug("canalObtenido : " + canalObtenido);
 
 		// El canal no está registrado
 		if (canalObtenido == null) {
 
 			if (inscripcion.registrar) {
 
 				printDebug("El canal no existía, creamos el canal y lo añadimos");
 
 				// Creamos el canal
 				Canal canal = new Canal(inscripcion.canal.getNombreCanal(),
 						panchat.getListaUsuarios());
 
 				// Añadimos al usuario inscrito al canal
 				canal.anyadirUsuarioConectado(usuarioInscripcionObtenido);
 
 				// Añadimos el canal a la lista de canales
 				panchat.getListaCanales().añadirCanal(canal);
 
 				// Si he sido invitado a la conversacion
 				if (usuarioInscripcionObtenido.equals(panchat.getUsuario())) {
 
 					printDebug("he sido invitado");
 
 					String bienvenido = panchat.getUsuario().nickName
 							+ " he sido invitado a la conversacion por "
 							+ usuario.nickName;
 
 					// Crear la ventana
 					panchat.getListaConversaciones().getVentanaConversacion(
 							canalObtenido).escribirComentario(bienvenido);
 				}
 
 				if (DEBUG) {
 					printDebug("Resultado");
 					printDebug("Usuarios conectados :");
 					for (int i = 0; i < canal.getNumUsuariosConectados(); i++)
 						printDebug(canal.getUsuarioConectado(i).toString());
 
 					printDebug("Usuarios desconectados :");
 					for (int i = 0; i < canal.getNumUsuariosDesconectados(); i++)
 						printDebug(canal.getUsuarioDesconectado(i).toString());
 				}
 			}
 		}
 
 		// Si el canal ya exístia y no ha sido una acción provocada por mi,
 		// entonces :
 		else if (!usuarioEnvioObtenido.equals(panchat.getUsuario())) {
 
 			printDebug("El canal existía");
 
 			if (inscripcion.registrar) {
 
 				if (!canalObtenido.contains(usuarioInscripcionObtenido)) {
 
 					printDebug("El canal no contenia al usuario inscrito, agregandolo a la lista");
 					canalObtenido
 							.anyadirUsuarioConectado(usuarioInscripcionObtenido);
 				}
 
 				// Si he sido invitado a la conversacion
 				if (usuarioInscripcionObtenido.equals(panchat.getUsuario())) {
 
 					printDebug("he sido invitado");
 
					String bienvenido = panchat.getUsuario().nickName
							+ " he sido invitado a la conversacion por "
 							+ usuario.nickName;
 
 					panchat.getListaConversaciones().getVentanaConversacion(
 							canalObtenido).escribirComentario(bienvenido);
 				}
 
 				// Si estando yo conectado alguien más se ha agregado a la
 				// conversacion mostramos saludo
 				else if (canalObtenido.contains(panchat.getUsuario())) {
 
 					printDebug("alguien ha entrado en una conversacion en la que ya estaba");
 
 					String bienvenido = panchat.getUsuario().nickName
							+ " bienvenido a la conversacion";
 
 					panchat.getListaConversaciones().getVentanaConversacion(
 							canalObtenido).escribirComentario(bienvenido);
 				}
 
 				if (DEBUG) {
 					printDebug("Resultado");
 					printDebug("Usuarios conectados :");
 					for (int i = 0; i < canalObtenido
 							.getNumUsuariosConectados(); i++)
 						printDebug(canalObtenido.getUsuarioConectado(i)
 								.toString());
 
 					printDebug("Usuarios desconectados :");
 					for (int i = 0; i < canalObtenido
 							.getNumUsuariosDesconectados(); i++)
 						printDebug(canalObtenido.getUsuarioDesconectado(i)
 								.toString());
 				}
 
 			} else {
 
 				if (canalObtenido.contains(usuarioInscripcionObtenido)) {
 
 					printDebug("El canal contenía al usuario inscrito, desregistrando al usuario del canal");
 
 					canalObtenido
 							.eliminarUsuarioConectado(usuarioInscripcionObtenido);
 
 					if (DEBUG) {
 						printDebug("Resultado");
 						printDebug("Usuarios conectados :");
 						for (int i = 0; i < canalObtenido
 								.getNumUsuariosConectados(); i++)
 							printDebug(canalObtenido.getUsuarioConectado(i)
 									.toString());
 
 						printDebug("Usuarios desconectados :");
 						for (int i = 0; i < canalObtenido
 								.getNumUsuariosDesconectados(); i++)
 							printDebug(canalObtenido.getUsuarioDesconectado(i)
 									.toString());
 					}
 
 					if (canalObtenido.getNumUsuariosConectados() == 0) {
 
 						printDebug("El canal ya no posee más usuarios, desregistrando");
 
 						listaCanales.eliminarCanal(canalObtenido);
 					}
 				}
 			}
 		}
 
 		// Actualizamos la vista de canales
 		panchat.getListaCanales().canalModificado();
 	}
 
 	private void escribirMensajeCanal(MessageChat objeto) {
 
 		String nombreCanal = objeto.canal.getNombreCanal();
 
 		ListaCanales listaCanales = panchat.getListaCanales();
 
 		Canal canalObtenido = listaCanales.getCanal(nombreCanal);
 
 		printDebug("canalObtenido : " + canalObtenido);
 
 		if (canalObtenido != null) {
 
 			if (canalObtenido.contains(panchat.getUsuario()))
 
 				panchat.getListaConversaciones().getVentanaConversacion(
 						canalObtenido).escribirComentario(objeto.mensaje);
 
 		}
 	}
 
 	private void escribirMensaje(String comentario, Usuario usuario) {
 
 		panchat.getListaConversaciones().getVentanaConversacion(usuario)
 				.escribirComentario(comentario);
 
 	}
 
 	private void printDebug(String string) {
 		String msgClase = "CausalLinkerThread.java: ";
 		if (DEBUG)
 			System.out.println(msgClase + string);
 	}
 }

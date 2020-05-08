 package panchat;
 
 import panchat.connector.Connector;
 import panchat.data.Canal;
 import panchat.data.ListaCanales;
 import panchat.data.ListaConversaciones;
 import panchat.data.ListaUsuarios;
 import panchat.data.Usuario;
 import panchat.linker.CausalLinker;
 import panchat.linker.Linker;
 import panchat.messages.InscripcionCanal;
 import panchat.messages.MessageChat;
 import panchat.ui.main.PanchatUI;
 
 public class Panchat {
 
 	// Connector
 	private Connector connector;
 
 	// Datos
 	private Usuario usuario;
 	private ListaUsuarios listaUsuarios;
 	private ListaCanales listaCanales;
 	private ListaConversaciones listaConversaciones;
 
 	// Linkers
 	private Linker linker;
 	private CausalLinker causalLinker;
 
 	/**
 	 * Crea nueva instancia de panchat
 	 * 
 	 * @param nombreUsuario
 	 */
 	public Panchat(String nombreUsuario) {
 		// Apartir de un String obtiene un usuario valido, buscano la IP del
 		// ordenador actual, y buscando un puerto disponible a partir del 5000
 		this(new Usuario(nombreUsuario));
 	}
 
 	/**
 	 * Crea nueva instancia de panchat
 	 * 
 	 * @param pUsuario
 	 */
 	public Panchat(Usuario pUsuario) {
 		this.usuario = pUsuario;
 
 		this.listaCanales = new ListaCanales();
 		this.listaUsuarios = new ListaUsuarios(listaCanales);
 		this.listaConversaciones = new ListaConversaciones(this);
 
 		// Nos añadimos a nuestra propia lista de usuarios
 		this.listaUsuarios.añadirUsuario(usuario);
 
 		this.connector = new Connector(this);
 		this.causalLinker = new CausalLinker(this);
 		this.linker = new Linker(this);
 
 		// Como el hilo MulticastListenerThread depende de causalLinker, lo
 		// arrancamos después para evitar una condicción de carrera al
 		// instanciar las clases.
 		this.connector.arrancarThreads();
 	}
 
 	/*
 	 * Getters
 	 */
 
 	/**
 	 * Devuelve la lista de usuarios del chat
 	 * 
 	 * @return
 	 */
 	public ListaUsuarios getListaUsuarios() {
 		return listaUsuarios;
 	}
 
 	/**
 	 * Devuelve la lista de canales del chat
 	 * 
 	 * @return
 	 */
 	public ListaCanales getListaCanales() {
 		return listaCanales;
 	}
 
 	/**
 	 * Devuelve la lista de conversaciones
 	 * 
 	 * @return
 	 */
 	public ListaConversaciones getListaConversaciones() {
 		return listaConversaciones;
 	}
 
 	/**
 	 * Devuelve la lista de canales del chat
 	 * 
 	 * @return
 	 */
 	public Usuario getUsuario() {
 		return usuario;
 	}
 
 	/**
 	 * Devuelve el causal linker asociado al chat
 	 * 
 	 * @return
 	 */
 	public CausalLinker getCausalLinker() {
 		return causalLinker;
 	}
 
 	/**
 	 * Devuelve el linker asociado al chat
 	 * 
 	 * @return
 	 */
 	public Linker getLinker() {
 		return linker;
 	}
 
 	/**
 	 * Devuelve la clase connector
 	 * 
 	 * @return
 	 */
 	public Connector getConnector() {
 		return connector;
 	}
 
 	/*
 	 * Acciones para el GUI
 	 */
 
 	/**
 	 * Desregistra el cliente e inicia la terminación de la aplicación
 	 */
 	public void accionDesegistrarCliente() {
 		connector.enviarSaludo(false);
 
 		connector.closeSockets();
 	}
 
 	/**
 	 * Inicia una conversación con el usuario
 	 * 
 	 * @param usuario
 	 */
 	public void accionInscribirCanal(String nombre) {
 		Canal canal = new Canal(nombre, listaUsuarios);
 		listaCanales.añadirCanal(canal);
 
 		accionIniciarConversacionCanal(canal);
 	}
 
 	/**
 	 * Inicia una conversación con el usuario
 	 * 
 	 * @param usuario
 	 */
 	public void accionIniciarConversacion(Usuario usuario) {
 		listaConversaciones.getVentanaConversacion(usuario);
 	}
 
 	/**
 	 * Cierra una conversación con el usuario
 	 * 
 	 * @param usuario
 	 */
 	public void accionCerrarConversacion(Usuario usuario) {
 		this.listaConversaciones.eliminarConversacion(usuario);
 	}
 
 	/**
 	 * Inicia la conversación de un canal
 	 * 
 	 * @param usuario
 	 */
 	public void accionIniciarConversacionCanal(Canal canal) {
 		if (!canal.contains(usuario)) {
 			canal.anyadirUsuarioConectado(usuario);
 
 			listaConversaciones.getVentanaConversacion(canal);
 
 			// Notificamos a todo el mundo sobre el nuevo canal
 			InscripcionCanal inscripcion = new InscripcionCanal(canal, usuario,
 					true);
 
 			causalLinker.sendMsg(this.listaUsuarios.getListaUsuarios(),
 					inscripcion);
 		}
 
 	}
 
 	/**
 	 * Cierra la conversación de un canal
 	 * 
 	 * @param usuario
 	 */
 	public void accionCerrarConversacionCanal(Canal canal) {
 		// Lo añadimos a la lista de conversaciones
 		listaConversaciones.eliminarConversacion(canal);
 
 		// Notificamos a todo el mundo sobre el nuevo canal
 		InscripcionCanal inscripcion = new InscripcionCanal(canal, usuario,
 				false);
 
 		causalLinker.sendMsg(listaUsuarios.getListaUsuarios(), inscripcion);
 
 		// Nos borramos del listado de usuarios conectados del canal
 		canal.eliminarUsuarioConectado(usuario);
 
 		if (canal.getNumUsuariosConectados() == 0) {
 
 			listaCanales.eliminarCanal(canal);
 		}
 	}
 
 	/**
 	 * Envia un comentario a un usuario
 	 * 
 	 * @param usuario
 	 * @param pComentario
 	 */
 	public void escribirComentario(Usuario usuario, String pComentario) {
 		causalLinker.sendMsg(usuario, pComentario);
 	}
 
 	/**
 	 * Envia un comentario a un canal
 	 * 
 	 * @param usuario
 	 * @param pComentario
 	 */
 	public void escribirComentarioCanal(Canal pCanal, String pComentario) {
 		MessageChat mensaje = new MessageChat(pCanal, pComentario);
 		causalLinker.sendMsg(pCanal.getListadoUsuarios(), mensaje);
 	}
 
 	/**
 	 * Invitar un usuario un canal
 	 * 
 	 * @param canal
 	 * @param usuario
 	 */
 	public void invitarUsuario(Canal pCanal, Usuario pUsuario) {
 		// Lo añadimos a la lista de conversaciones
 		listaCanales.getCanal(pCanal.getNombreCanal()).anyadirUsuarioConectado(
 				pUsuario);
 
 		listaCanales.canalModificado();
 
 		// Notificamos a todo el mundo sobre el nuevo canal
 		InscripcionCanal inscripcion = new InscripcionCanal(pCanal, pUsuario,
 				true);
 
 		causalLinker
 				.sendMsg(this.listaUsuarios.getListaUsuarios(), inscripcion);
 	}
 
 	public static void main(String[] args) {
 		new PanchatUI();
 	}
 
 }

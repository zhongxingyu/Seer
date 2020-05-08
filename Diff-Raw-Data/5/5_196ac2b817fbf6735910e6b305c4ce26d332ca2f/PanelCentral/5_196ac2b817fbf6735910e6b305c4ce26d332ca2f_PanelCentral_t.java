 package interfaz.paneles;
 
 import interfaz.ventanas.VentanaBase;
 import interfaz.conversaciones.Conversacion;
 import interfaz.elementos.Datos;
 
 import java.awt.Graphics;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.HashMap;
 
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 
 import panchat.Panchat;
 import panchat.connector.Connector;
 import panchat.data.ListaConversaciones;
 import panchat.data.Usuario;
 import panchat.data.models.UsuarioTablaModel;
 
 
 
 
 public class PanelCentral extends MiPanel implements MouseListener {
 
 	private static final long serialVersionUID = 1L;
 
 	// tabla hash con las rutas de los emoticonos
 	static HashMap<String, String> emoticonos = new HashMap<String, String>();
 	static boolean cargada=false;
 	static Object mutex=new Object();
 
 	PanelRecuadro imagen = new PanelRecuadro(camino+"nubes.jpg");
 	MiPanel nick = new MiPanel();
 	MiPanel usuarios = new MiPanel();
 
 	JScrollPane scroll = new JScrollPane(usuarios);
 
 	Datos nickname;
 	Datos estado;
 
 	UsuarioTablaModel modelo;
 	JTable listaUsuarios ;//= new JList(modelo);
 	
 	Panchat panchat;
 	
 	
 	static String  camino ="/interfaz/imagenes/";
 
 	public PanelCentral(Panchat panchat) {
 		super();
 		modelo=new UsuarioTablaModel(panchat.getListaUsuarios());
 		listaUsuarios = new JTable(modelo);
 		
 		this.panchat=panchat;
 		this.setOpaque(false);
 		this.setLayout(new GridBagLayout());
 
 		nickname = new Datos(panchat.getUsuario().nickName);
 		estado = new Datos("conectado");
 
 		nick.setLayout(new GridLayout(2, 1));
 		nick.add(nickname);
 		nick.add(estado);
 
 		/*int i=0;
 		int numUsuarios = panchat.getListaUsuarios().getNumUsuarios();
 		for (; i < numUsuarios; i++) {
 			if(panchat.getListaUsuarios().getUsuario(i).nickName!=panchat.getUsuario().nickName)
 			modelo.add(i,panchat.getListaUsuarios().getUsuario(i).nickName);
 
 		}*/
 
 		listaUsuarios.addMouseListener(this);
 
 		usuarios.add(listaUsuarios);
 
 		GridBagConstraints c = new GridBagConstraints();
 
 		c.insets = new Insets(2, 2, 2, 2);
 		c.weightx = 0.15;
 		c.weighty = 0.6;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.gridwidth = 2;
 		c.gridheight = 2;
 		c.anchor = GridBagConstraints.CENTER;
 		c.fill = GridBagConstraints.BOTH;
 		this.add(imagen, c);
 
 		c.weightx = 0.2;
 		c.weighty = 0.2;
 		c.gridx = 2;
 		c.gridy = 0;
 		c.gridwidth = GridBagConstraints.REMAINDER;
 		c.gridheight = GridBagConstraints.RELATIVE;
 		c.anchor = GridBagConstraints.NORTH;
 		c.fill = GridBagConstraints.NONE;
 		c.insets = new Insets(40, 15, 40, 2);
 		this.add(nickname, c);
 
 		c.weightx = 0.2;
 		c.weighty = 0.2;
 		c.gridy = 1;
 		c.anchor = GridBagConstraints.SOUTH;
 		this.add(estado, c);
 
 		c.insets = new Insets(5, 4, 5, 3);
 		c.weightx = 1;
 		c.weighty = 1;
 		c.gridy = 2;
 		c.gridx = 0;
 		c.gridwidth = GridBagConstraints.REMAINDER;
 		c.gridheight = GridBagConstraints.REMAINDER;
 		c.anchor = GridBagConstraints.EAST;
 		c.fill = GridBagConstraints.BOTH;
 		this.add(scroll, c);
 		
 
 	}
 
 	public static HashMap<String, String> obtenerEmoticonos() {
 		synchronized(mutex){
 		if(!cargada)
 			cargarEmoticonos();
 			cargada=true;
 		}
 		return emoticonos;
 	}
 
 	public static boolean estaEmoticon(String clave) {
 		synchronized(mutex){
 			if(!cargada)
 				cargarEmoticonos();
 				cargada=true;
 			}
 		return emoticonos.containsKey(clave);
 	}
 	
 	private static void cargarEmoticonos(){
 		emoticonos.put("nubes", "nubes.jpg");
 		emoticonos.put("xd", "xd.gif");
 		emoticonos.put("pizarra", "pizarra.png");
 	}
 
 	static String obtenerRuta(String clave) {
 		return emoticonos.get(clave);
 	}
 
 	
 
 	public void paint(Graphics g) {
 
 		super.paint(g);
 
 	}
 
 	public static void main(String[] args) {
 		VentanaBase in = new VentanaBase();
 		Panchat panchat=new Panchat("kk");
 		PanelCentral central = new PanelCentral(panchat);
 		in.add(central);
 		in.setVisible(true);
 
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
		
		Usuario usuario = panchat.getListaUsuarios().getUsuario(listaUsuarios.getSelectedRow());
 		
 		if (arg0.getClickCount() == 2) {
 			System.out.println((usuario));
 			VentanaBase ventana = new VentanaBase();
 			Conversacion conversacion = new Conversacion();
 			
 			ListaConversaciones conversaciones= panchat.getListaConversaciones();
 			
 			
 			
 			Connector conector =panchat.getConnector();
 			ventana.add( conversacion);
 		}
 
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 	}
 
 	@Override
 	public void mousePressed(MouseEvent arg0) {
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 	}
 
 }

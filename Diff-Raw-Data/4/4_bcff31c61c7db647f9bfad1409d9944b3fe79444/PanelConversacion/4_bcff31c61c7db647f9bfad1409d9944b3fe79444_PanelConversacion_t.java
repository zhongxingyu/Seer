 package panchat.ui.chat;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JPanel;
import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 public class PanelConversacion extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 
 	private JTextArea textArea;
 	private JTextField textField;
 	private IVentanaConversacion ventana;
 
 	/**
 	 * 
 	 * @param panchat
 	 */
 	public PanelConversacion(IVentanaConversacion conversacionVentana) {
 
 		// Usamos este método para indicar a nuestra ventana contenedora que se
 		// ha producido un evento
 		this.ventana = conversacionVentana;
 
 		// Construimos los objetos
 		textArea = new JTextArea();
 		textField = new JTextField();
 
 		// Organizar el foco
 		textArea.setFocusable(false);
 		textField.requestFocusInWindow();
 
 		// Cambiamos el borderlayout del panel
 		setLayout(new BorderLayout());
 
 		// Anadimos el textArea al área central del panel
		add(new JScrollPane(textArea), BorderLayout.CENTER);
 
 		// Añadimos el textfield al área inferior del panel
 		add(textField, BorderLayout.SOUTH);
 
 		// Modificamos el TextArea a no editable
 		textArea.setEditable(false);
 
 		textField.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 				ventana.eventoNuevoComentario(textField.getText());
 				textField.setText("");
 			}
 
 		});
 	}
 
 	/**
 	 * Escribir nuevo comentario
 	 * 
 	 * @param comentario
 	 */
 	public void escribirComentario(String comentario) {
 		textArea.append(comentario + "\n");
 	}
 }

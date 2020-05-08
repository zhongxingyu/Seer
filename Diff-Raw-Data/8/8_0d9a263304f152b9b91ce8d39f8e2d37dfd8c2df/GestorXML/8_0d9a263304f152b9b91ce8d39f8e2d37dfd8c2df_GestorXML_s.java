 package IS2011.bibliotecaXML;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.util.*;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.xml.DomDriver;
 
 public class GestorXML {
 
 	//private File rutaBiblioteca = new File("xml/biblioteca.xml");
 	//private ArrayList<Track> biblioteca = null;
 	
 	private File rutaBiblioteca ;
 	private Biblioteca biblioteca;
	private Biblioteca carga;
 	private InputStream is=null;
 	
 	/**
 	 * Crea una biblioteca a partir de el archivo XML por defecto
 	 * @param b
 	 */
 	public GestorXML(Biblioteca b) {
 	//	biblioteca = new ArrayList<Track>();
 	//	carga();
 		biblioteca = b;
 		rutaBiblioteca= new File("xml/biblioteca.xml");
 	}
 	
 	/**
 	 *  Crea una biblioteca
 	 */
 	public GestorXML(){
 		rutaBiblioteca= new File("xml/biblioteca.xml");
 	}
 	
 	/**
 	 * Crea una biblioteca a partir de un archivo XML
 	 * @param f: Archivo XML de entrada
 	 */
 	public GestorXML(File f){
 		rutaBiblioteca = f;
 	//	biblioteca = new ArrayList<Track>();
 	//	carga();
 		
 	}
 
 
 	/**
 	 * Esta funcin simplemente llama a la de biblioteca
 	 * @param trList
 	 */
 	public void addAll(List<Track> trList) {
 		this.biblioteca.addAll(trList);
 	}
 
 	/**
 	 * Esta funcin simplemente llama a la de biblioteca
 	 * @param tr
 	 */
 	public void add(Track tr) {
 		this.biblioteca.add(tr);
 	}
 	
 	/**
 	 * Esta funcin simplemente llama a la de biblioteca
 	 */
 	public ArrayList<Track> getBiblioteca(){
 		return biblioteca.getBiblioteca();
 	}
 	
 	/**
 	 * Carga en la biblioteca el contenido del XML
 	 */
 	public void cargar(){
 		try {
 			 XStream xs=new XStream(new DomDriver());
 			 is = new FileInputStream(rutaBiblioteca);
 			 biblioteca = new Biblioteca();
 			 xs.processAnnotations(Biblioteca.class);
			 biblioteca = (Biblioteca)xs.fromXML(is);
 		} catch (Exception e) {
 			JOptionPane.showMessageDialog(new JFrame(),
 				    "La biblioteca no ha podido ser cargada, el formato es errneo.\n" +
 				    e.getMessage(),
 				    "Error carga de la biblioteca",
 				    JOptionPane.WARNING_MESSAGE);
 			//TODO crear un nuevo biblioteca.xml 
 			e.printStackTrace();
 		}
 		//xs.fromXML
 		//String xml = xs.toXML(is);
 		//String aux=(String)xs.fromXML(xml);
 		//Iterator it=(Iterator)tracks.listIterator();
 		
 		//while(it.hasNext()){
 			//Track t=(Track)it.next();
 		//	System.out.println(t.getName());
 		//}		
 	}
 	
 
 	/**
 	 * Guarda en el XML todo el contenido actual de la biblioteca b
 	 */
 	public void guardar(){
 		//tarea correspondiente a la escritura del XML (Miguel, Jachu)
 		// tener como entrada el ArrayList de la Biblioteca funcionXML(biblioteca)?
 		guardar(rutaBiblioteca);
 	}
 	
 	/**
 	 * Metodo para guardar la biblioteca a un archivo especificado mediante un String
 	 * @param filename
 	 */
 	public void guardar(String filename){
 		guardar(new File(filename));
 	}
 	
 	/**
 	 * Metodo para guardar la biblioteca a un archivo especificado
 	 * @param filename
 	 */
 	public void guardar(File file){
 		if( biblioteca != null){
 			try {
 				XStream xstream = new XStream(new DomDriver());
 				// Esta linea es para que haga caso de las anotaciones del tipo @XStream*
 				xstream.processAnnotations(Biblioteca.class);
 				xstream.toXML(biblioteca, new FileOutputStream(file));
 			} catch (FileNotFoundException e) {
 				JOptionPane.showMessageDialog(new JFrame(),
 					    "La biblioteca no ha podido ser guardada.\n" +
 					    e.getMessage(),
 					    "Error carga de la biblioteca",
 					    JOptionPane.WARNING_MESSAGE);
 				e.printStackTrace();
 			}
 		}
 	}
 
 	
 	
 	
 }

 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package analizador;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Logger;
 import org.w3c.dom.DOMImplementation;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 import org.xml.sax.SAXException;
 
 /**
  *
  * @author juancarlos
  */
 public class Analisis {
 
     private final static Logger log = Logger.getLogger(Analisis.class);
     final String delimitador = "::";
     final String identificadorOracle = "([A-Za-z0-9._\\$#]+)"; // doble \\ por ser caracter especial para java
     final String identificadorAlter = "([A-Za-z0-9._\\$#]+)";
     final String espacioBlanco = "\\\\s*";
     final String parentesisApertura = "\\\\(";
     final String parentesisCierre = "\\\\)";
     final String critico = "critico";
     final String medio = "medio";
     final String bajo = "bajo";
     final String operador = "[\\*\\+\\-\\/]";
     Map<String, List<Integer>> defectos;
     List<PatronDefecto> pd;
 
     public Analisis() {
         BasicConfigurator.configure();
         defectos = new HashMap<String, List<Integer>>();
         pd = obtenerPatrones();
 
     }
 
     public void detectarDefectos(String ubicacionArchivos) {
         Map<String, Integer> mapa = new HashMap<String, Integer>();
         Map<String, Integer> tmp = new HashMap<String, Integer>();
         Grafo grafo = new Grafo();
         File archivo = new File(ubicacionArchivos);
         List<Nodo> archivosFiltrados = grafo.crearGrafo(ubicacionArchivos, archivo.list(), null);
 
         for (int x = 0; x < archivosFiltrados.size(); x++) {
             log.info("Nombre archivo " + archivosFiltrados.get(x).getPrograma());
             tmp = analizarNodo(archivosFiltrados.get(x));
             mapa.put(archivosFiltrados.get(x) + critico, tmp.get(critico));
             mapa.put(archivosFiltrados.get(x) + medio, tmp.get(medio));
             mapa.put(archivosFiltrados.get(x) + bajo, tmp.get(bajo));
 
         }

         generarXML(mapa, archivosFiltrados);
 
     }
 
     public Map<String, Integer> analizarNodo(Nodo nodo) {
         //MAPA clasificiacion del defecto, cantidad
         Map<String, Integer> mapa = new HashMap<String, Integer>();
         File archivo = null;
         FileReader fr = null;
         BufferedReader br = null;
         String linea;
         int numLinea = 0;
         int cantidadCritico = 0;
         int cantidadMedio = 0;
         int cantidadBajo = 0;
         int numLineaInicio = -1;
         int numLineaFinal = -1;
         String inicio = null;
         String cierre = null;
         boolean group;
         boolean comentario = false;
 
         try {
 
             archivo = new File(nodo.getPrograma());

             log.info("Cantidad de patrones " + pd.size());
             for (int x = 0; x < pd.size(); x++) {// para todos los patrones
                 List<Integer> lineas = new ArrayList<Integer>();
                 if (archivo.isFile()) {
                     fr = new FileReader(archivo);
                     br = new BufferedReader(fr);
                     group = false;
                     String patron = pd.get(x).getExpresion();
                     if (patron.contains(" ")) {
                         log.info("Reemplazando espacio en blanco");
                         patron = patron.replaceAll(" ", espacioBlanco);
                     }
                     if (patron.contains("(") && patron.contains(")")) {
                         log.info("Reemplazando ( )");
                         patron = patron.replaceAll("\\(", parentesisApertura);
                         patron = patron.replaceAll("\\)", parentesisCierre);
                     }
 
 
                     if (patron.contains("operador")) {
                         log.info("Patron con operador");
                         patron = patron.replaceAll("operador", operador);
                         log.info(patron);
                     }
                     if (patron.contains("identificadorAlter")) {
                         log.info("Patron con IdentificadorAlternativo");
                         patron = patron.replaceAll("identificadorAlter", identificadorAlter);
                         log.info(patron);
                     }
 
                     if (patron.contains(delimitador)) {
                         log.info("Patron con delimitador");
                         int delimitadorPosicion = patron.indexOf(delimitador);
                         inicio = patron.substring(0, delimitadorPosicion);
                         cierre = patron.substring(patron.indexOf(delimitador) + delimitador.length(), patron.length());
                     }
                     if (inicio != null) {
                         if (inicio.contains("identificador")) {
                             log.info("Patron con identificador");
                             inicio = inicio.replaceAll("identificador", identificadorOracle);
                             log.info(inicio);
                             group = true;
                         }
                     }
                     if (patron.contains("identificador")) {
                         log.info("Patron con identificador");
                         patron = patron.replaceAll("identificador", identificadorOracle);
                         log.info(patron);
                         group = true;
                     }
 
                     numLinea = 0;
 
                     while ((linea = br.readLine()) != null) { // para cada linea del archivo
 
                         numLinea++;
 
 
                         if (linea.contains("--")) { // valida de que la  linea a analizar no sea comentario de solo 1 linea
                             continue;
                         }
                         if (linea.contains("/*") && linea.contains("*/")) {
                             continue;
                         }
                         if (linea.contains("/*")) {  /* validacion comentarios multiples*/
                             comentario = true;
                         }
                         if (linea.contains("*/")) {
                             comentario = false;
                         }
                         if (comentario) {
                             continue;
                         }
                         for (int p = 0; p < nodo.getLlamadas().size(); p++) {
                             String llamada = nodo.getLlamadas().get(p).getPrograma().substring(nodo.getLlamadas().get(p).getPrograma().lastIndexOf("/") + 1, nodo.getLlamadas().get(p).getPrograma().length() - 4);
                             if (linea.contains(llamada)) {
                                 log.info("LLamando al nodo " + nodo.getLlamadas().get(p).getPrograma() + " desde " + archivo.toString());
                                 analizarNodo(nodo.getLlamadas().get(p));
                             }
                         }
 
 
                         if (patron.contains(delimitador)) {
                             log.info("Patron con delimitador");
                             Pattern p = Pattern.compile(inicio, Pattern.CASE_INSENSITIVE);
                             Matcher m = p.matcher(linea);
                             if (m.find() && group) {
                                 String ident = null;
                                 ident = m.group(1);
                                 numLineaInicio = numLinea;
                                 numLineaFinal = analizarCierre(ident, cierre, nodo.getPrograma());
                             } else if (m.find()) {
                                 numLineaInicio = numLinea;
                                 numLineaFinal = analizarCierre(null, cierre, nodo.getPrograma());
                             }
                             //log.info("Linea inicio " + numLineaInicio);
                             //log.info("Linea final " + numLineaFinal);
                             if (numLineaInicio > numLineaFinal) {
                                log.info("Defecto encontrado linea inicio" + numLineaInicio + "Final" + numLineaFinal);
                                 if (pd.get(x).getClasificacion().equals(critico)) {
                                     cantidadCritico++;
                                 } else if (pd.get(x).getClasificacion().equals(medio)) {
                                     cantidadMedio++;
                                 } else {
                                     cantidadBajo++;
                                 }
                                 lineas.add(numLinea);
                                 numLineaInicio = -1;
                                 numLineaFinal = -1;
                             }
 
 
                         } else {
                             log.info("Patron sin delimitador");
                             Pattern p = Pattern.compile(patron, Pattern.CASE_INSENSITIVE);
                             Matcher m = p.matcher(linea);
                             log.info("Patron reemplazado " + patron);
                             if (m.find()) {
                                 if (pd.get(x).getClasificacion().equals(critico)) {
                                     cantidadCritico++;
                                 } else if (pd.get(x).getClasificacion().equals(medio)) {
                                     cantidadMedio++;
                                 } else {
                                     cantidadBajo++;
                                 }
                                 lineas.add(numLinea);
 
                             }
 
                         }
 
                     }
                     defectos.put(nodo.getPrograma() + pd.get(x).getNombre(), lineas);
                 }
             }
 
             mapa.put(critico, cantidadCritico);
             mapa.put(medio, cantidadMedio);
             mapa.put(bajo, cantidadBajo);
 
         } catch (IOException e) {
             log.error(e.getMessage());
 
         } finally {
 
             try {
                 if (null != fr) {
                     fr.close();
                 }
             } catch (Exception e2) {
                 log.error(e2.getMessage());
 
             }
         }
 
         return mapa;
     }
 
     public int analizarCierre(String identificador, String patron, String programa) {
 
         File archivo = null;
         FileReader fr = null;
         BufferedReader br = null;
         int numLinea = -1;
        boolean comentario =false;
         String linea;
         int nLinea = 0;
 
         try {
 
             archivo = new File(programa);
             if (archivo.isFile()) {
                 fr = new FileReader(archivo);
                 br = new BufferedReader(fr);
 
 
                 log.info("analizarCierre");
 
                 if (identificador != null || "".equals(identificador)) {
                     patron = patron.replaceAll("identificador", identificador);
                     log.info("Patron Cierre Identificador " + patron);
                 }
 
                 while ((linea = br.readLine()) != null) { // para cada linea del archivo
 
                     nLinea++;
                    if (linea.contains("--")) { // valida de que la  linea a analizar no sea comentario de solo 1 linea
                        continue;
                    }
                    if (linea.contains("/*") && linea.contains("*/")) {
                        continue;
                    }
                    if (linea.contains("/*")) {  /* validacion comentarios multiples*/
                        comentario = true;
                    }
                    if (linea.contains("*/")) {
                        comentario = false;
                    }
                    if (comentario) {
                        continue;
                    }
 
                     Pattern p = Pattern.compile(patron);
                     Matcher m = p.matcher(linea);
                     log.info("Patron reemplazado Cierre " + patron);
                     if (m.find()) {
                        log.info("Patron Cierre encontrado" + nLinea);
                         numLinea = nLinea;
                     }
                 }
             }
         } catch (IOException e) {
             log.error(e.getMessage());
 
         } finally {
 
             try {
                 if (null != fr) {
                     fr.close();
                 }
             } catch (IOException e2) {
                 log.error(e2.getMessage());
             }
         }
         return numLinea;
 
     }
 
     public boolean generarXML(Map<String, Integer> mapa, List<Nodo> nombreArchivo) {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         try {
             DocumentBuilder builder = factory.newDocumentBuilder();
             DOMImplementation implementation = builder.getDOMImplementation();
             Document document = implementation.createDocument(null, "estadisticas", null);
 
             for (int x = 0; x < nombreArchivo.size(); x++) {
                 Element raiz = document.createElement("estadistica");
 
                 Element fechaElem = document.createElement("fecha");
                 Text textFecha = document.createTextNode(new Date().toString());
 
 
                 Element ubicacionElem = document.createElement("nombreArchivo");
                 Text textUb = document.createTextNode(nombreArchivo.get(x).getPrograma());
 
 
                 Element cantDefBElem = document.createElement("cantidadDefectosBajo");
                 Text textCdb = document.createTextNode(String.valueOf(mapa.get(nombreArchivo.get(x) + bajo)));
 
                 Element cantDefMElem = document.createElement("cantidadDefectosMedio");
                 Text textCdm = document.createTextNode(String.valueOf(mapa.get(nombreArchivo.get(x) + medio)));
 
                 Element cantDefCElem = document.createElement("cantidadDefectosCritico");
                 Text textCdc = document.createTextNode(String.valueOf(mapa.get(nombreArchivo.get(x) + critico)));
 
                 for (int j = 0; j < pd.size(); j++) {
                     Element e = document.createElement(pd.get(j).getNombre());

                    if (defectos.get(nombreArchivo.get(x).getPrograma() + pd.get(j).getNombre()) != null) {
 
                         Text t = document.createTextNode(defectos.get(nombreArchivo.get(x).getPrograma() + pd.get(j).getNombre()).toString());
                         raiz.appendChild(e);
                         e.appendChild(t);
                     } else {
                         continue;
                     }
                 }
 
                 document.getDocumentElement().appendChild(raiz);
 
                 raiz.appendChild(fechaElem);
                 raiz.appendChild(ubicacionElem);
                 raiz.appendChild(cantDefBElem);
                 raiz.appendChild(cantDefMElem);
                 raiz.appendChild(cantDefCElem);
 
                 fechaElem.appendChild(textFecha);
                 ubicacionElem.appendChild(textUb);
                 cantDefBElem.appendChild(textCdb);
                 cantDefMElem.appendChild(textCdm);
                 cantDefCElem.appendChild(textCdc);
 
             }
 
             document.setXmlVersion("1.0");
             Source source = new DOMSource(document);
             Result result = new StreamResult(new java.io.File("defectos.xml"));
             Transformer transformer = TransformerFactory.newInstance().newTransformer();
             transformer.setOutputProperty(OutputKeys.INDENT, "yes");
             transformer.transform(source, result);
             return true;
         } catch (ParserConfigurationException e) {
             log.error(e.getMessage());
             return false;
 
         } catch (TransformerConfigurationException e) {
             log.error(e.getMessage());
             return false;
 
         } catch (TransformerException e) {
             log.error(e.getMessage());
             return false;
 
         }
     }
 
     public List<PatronDefecto> obtenerPatrones() {
 
         List<PatronDefecto> p = new ArrayList<PatronDefecto>();
         try {
 
             DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
             DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
             log.info(new File("patrones.xml").getAbsolutePath());
             log.info(new File("patrones.xml").getCanonicalPath());
             Document doc = docBuilder.parse(new File("patrones.xml"));
 
             doc.getDocumentElement().normalize();
 
             NodeList listPatrones = doc.getElementsByTagName("patron");
 
             for (int s = 0; s < listPatrones.getLength(); s++) {
 
                 Node patron = listPatrones.item(s);
                 if (patron.getNodeType() == Node.ELEMENT_NODE) {
 
                     Element pa = (Element) patron;
                     PatronDefecto patronD = new PatronDefecto();
                     // -------
                     NodeList identificador = pa.getElementsByTagName("identificador");
                     Element identificadorElement = (Element) identificador.item(0);
 
                     NodeList identificadorList = identificadorElement.getChildNodes();
                     if (((Node) identificadorList.item(0)) != null) {
                         patronD.setIdentificador(Integer.parseInt(((Node) identificadorList.item(0)).getNodeValue().trim()));
                     }
 
                     // -------
                     NodeList nombre = pa.getElementsByTagName("nombre");
                     Element nombreElement = (Element) nombre.item(0);
 
                     NodeList nombreList = nombreElement.getChildNodes();
                     if (((Node) nombreList.item(0)) != null) {
                         patronD.setNombre(((Node) nombreList.item(0)).getNodeValue().trim());
 
                     }
                     // -------
                     NodeList expresion = pa.getElementsByTagName("expresion");
                     Element expresionElement = (Element) expresion.item(0);
 
                     NodeList expresionList = expresionElement.getChildNodes();
                     if (((Node) expresionList.item(0)) != null) {
                         patronD.setExpresion(((Node) expresionList.item(0)).getNodeValue().trim());
 
                     }
                     // ----
                     NodeList clasificacion = pa.getElementsByTagName("clasificacion");
                     Element clasificacionElement = (Element) clasificacion.item(0);
 
                     NodeList clasificacionList = clasificacionElement.getChildNodes();
                     if (((Node) clasificacionList.item(0)) != null) {
                         patronD.setClasificacion(((Node) clasificacionList.item(0)).getNodeValue().trim());
                     }
 
                     // ----
                     NodeList descripcion = pa.getElementsByTagName("descripcion");
                     Element descripcionElement = (Element) descripcion.item(0);
 
                     NodeList descripcionList = descripcionElement.getChildNodes();
                     if (((Node) descripcionList.item(0)) != null) {
                         patronD.setDescripcion(((Node) descripcionList.item(0)).getNodeValue().trim());
                     }
                     // ----
                     NodeList correccion = pa.getElementsByTagName("correccion");
                     Element correccionElement = (Element) correccion.item(0);
 
                     NodeList correccionList = correccionElement.getChildNodes();
                     if (((Node) correccionList.item(0)) != null) {
                         patronD.setCorreccion(((Node) correccionList.item(0)).getNodeValue().trim());
                     }
 
                     p.add(patronD);
 
                 }
 
             }
 
         } catch (SAXException e) {
             log.error(e.getMessage());
         } catch (ParserConfigurationException e) {
             log.error(e.getMessage());
         } catch (IOException e) {
             log.error(e.getMessage());
         }
 
         return p;
 
     }
 }

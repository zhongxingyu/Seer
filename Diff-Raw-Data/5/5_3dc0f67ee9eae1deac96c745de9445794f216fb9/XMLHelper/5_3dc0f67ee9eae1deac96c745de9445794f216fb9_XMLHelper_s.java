 /*************************************************************************************
  * Copyright (c) 2006, 2008 The Sakai Foundation
  *
  * Licensed under the Educational Community License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *       http://www.osedu.org/licenses/ECL-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 
  *************************************************************************************/
 
 package uk.ac.lancs.e_science.sakaiproject.api.blogger.util;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.SortedMap;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.Vector;
 
 
 public class XMLHelper {
 	
 	/**
 	 * Elimina la cabecera de un documento XML.
 	 */
 	public static String eliminaCabecera(String documento) {
 		if (documento.indexOf("<?xml version=")==0)
 			return documento.substring(documento.indexOf("?>") + 2); // +2 por el '?>' de la cabecera
 		return documento;
 	}
 	
 	public static String aniadeCabecera(String documento){
 		if (documento.indexOf("<?xml version=\"1.0\"")!=-1) //si tiene cabecera ni lo tocamos
 			return documento;
 		return (new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(documento)).toString();
 	}
 
 	public static String aniadeCabecera(StringBuilder documento){
 		if (documento.indexOf("<?xml version=\"1.0\"")!=-1) //si tiene cabecera ni lo tocamos
 			return documento.toString();
 		StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(documento);
 		return sb.toString();
 	}
 	
 	/**
 	 * @param subdocumento Subdocumento que queremos a&ntilde;adir
 	 * @param nombreEtiquetaPadre Nombre de la etiqueta padre de la cual va a colgar el subdocumento
 	 * @param documento documento con el que vamos a trabajar y al que se le va a a&ntilde;adir el subdocumento indicado 
 	 * @return documento con el subdocumento a&ntilde;adido.
 	 */
 	public static String aniadeSubdocumento(String subdocumento, String nombreEtiquetaPadre, String documento) {
 		StringBuilder stTmp = new StringBuilder();
 		stTmp.append("</").append(nombreEtiquetaPadre).append(">");		
 		StringBuilder st= new StringBuilder(documento);
 		int posicionEtiqueta = documento.indexOf(stTmp.toString());
 		st.insert(posicionEtiqueta, subdocumento);		
 		return st.toString();		
 	}
 	
 	public static String aniadeCabeceraEnElCasoDeQueNoLaTenga(String documento){
 		if (!tieneCabecera(documento))
 			return aniadeCabecera(documento);
 		return documento;
 	}
 	
 	public static boolean tieneCabecera(String documento){
 		if (documento.indexOf("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")==0)
 			return true;
 		if (documento.indexOf("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>")==0)
 			return true;
 		return false;
 	}	
 	/**
 	 * Devuelve el subdocumento xml incluido entre dos etiquetas. el resultado incluye las etiquetas de apertura y cierre correspondientes
 	 * al parametro etiqueta.
 	 * @param etiqueta etiqueta de la que queremos extraer el subdocumento
 	 * @param documento: documento o subdocumento donde esta definida la etiqueta de la cual queremos extraer su texto
 	 * @return String Contenido de la etiqueta. En el caso de que la etiqueta no existiera devuelve la cadena vacía
 	 * TODO: reimplementarlo segun dameSubdocumentosDeLaEtiqueta
 	 */
 	public static String dameSubdocumentoDeLaEtiqueta(String etiqueta, String documento){
 		if (!XMLHelper.tieneCabecera(documento))
 			documento = XMLHelper.aniadeCabecera(documento);
 		int posicionEtiqueta = documento.indexOf(etiqueta);
 		boolean esRealmenteUnCierreDeEtiqueta=false;
 		while (posicionEtiqueta!=-1){ //hay que encontrarl a etiqueta de apertura
 			if (!esUnaEtiquetaDeAperturaYCierre(etiqueta,posicionEtiqueta,documento)&& esRealmenteUnaEtiquetaDeApertura(etiqueta,posicionEtiqueta,documento)){
 				int posCierreAperturaEtiqueta = documento.indexOf('>',posicionEtiqueta);
 				int posAperturaCierreEtiqueta = documento.indexOf("</"+etiqueta,posCierreAperturaEtiqueta); //en este punto ya sabemos que tiene que haber una etiqueta de cierre que no es de apertua
 				while (!esRealmenteUnCierreDeEtiqueta){ //hay que encontrar la etiquieta de cierre.
 					if (documento.charAt(posAperturaCierreEtiqueta-posAperturaCierreEtiqueta)!='\\')
 						esRealmenteUnCierreDeEtiqueta=true;
 					else
 						posAperturaCierreEtiqueta = documento.indexOf(etiqueta,posCierreAperturaEtiqueta);
 				}
 				StringBuilder texto = new StringBuilder();
 				texto.append("<").append(etiqueta).append(">").append(documento.substring(posCierreAperturaEtiqueta+1,posAperturaCierreEtiqueta).trim()).append("</").append(etiqueta).append(">"); //se resta dos para quitar el </
 				return texto.toString();
 			}
 			posicionEtiqueta = documento.indexOf(etiqueta,posicionEtiqueta+1);		
 		}
 		return "";
 	}
 	/**
 	 * Devuelve una lista con cadal subdocumento xml incluido entre dos etiquetas. el resultado incluye las etiquetas de apertura y cierre correspondientes
 	 * al parametro etiqueta.
 	 * @param etiqueta etiqueta de la que queremos extraer el subdocumento
 	 * @param documento: documento o subdocumento donde esta definida la etiqueta de la cual queremos extraer su texto
 	 * @return String Contenido de la etiqueta. En el caso de que la etiqueta no existiera devuelve la cadena vacía
 	 */
 	public static List dameSubdocumentosDeLaEtiqueta(String etiqueta, String documento){
 		List resultado = new ArrayList();
 		StringBuilder etiquetaApertura = new StringBuilder("<");
 		etiquetaApertura.append(etiqueta).append(">");
 		StringBuilder etiquetaCierre = new StringBuilder("</");
 		etiquetaCierre.append(etiqueta).append(">");
 		if (!XMLHelper.tieneCabecera(documento))
 			documento = XMLHelper.aniadeCabecera(documento);
 		int posicionEtiquetaApertura = documento.indexOf(etiquetaApertura.toString());
 		int posicionEtiquetaCierre=0;
 		while (posicionEtiquetaApertura!=-1){ //hay que encontrarl a etiqueta de apertura
 				posicionEtiquetaCierre = documento.indexOf(etiquetaCierre.toString(),posicionEtiquetaApertura);
 				StringBuilder texto = new StringBuilder();
 				texto.append(documento.substring(posicionEtiquetaApertura,posicionEtiquetaCierre).trim()).append(etiquetaCierre); 
 				resultado.add( texto.toString());
 				posicionEtiquetaApertura = documento.indexOf(etiquetaApertura.toString(),posicionEtiquetaCierre);		
 		}
 		return resultado;
 	}
 	
 	/**
 	 * devuelve el texto enmarcado en una etiqueta. Este m&eacute;todo funciona correctamente en el caso de que la etiqueta no tenga
 	 * multiplicidad. En el caso de que tuviera multiplicidad devolver&iacute;a &uacute;nicamente el prime contenido que se encuentre.
 	 * @param etiqueta etiqueta de la que queremos extraer su texto
 	 * @param documento: documento o subdocumento donde esta definida la etiqueta de la cual queremos extraer su texto
 	 * @return String Contenido de la etiqueta. En el caso de que la etiqueta no existiera devuelve <code>null</code>
 	 */
 	public static String dameTextoDeLaEtiqueta(String etiqueta,String documento) {
 			StringBuilder sb = new StringBuilder("<");
 			sb.append(etiqueta).append("/>");
 			if (documento.indexOf(sb.toString())!=-1)
 				return "";
 			sb = new StringBuilder("<");
 			sb.append(etiqueta).append(">");
 			int posEtiquetaApertura = documento.indexOf(sb.toString());
 			if (posEtiquetaApertura==-1)
 				return null;
 			sb = new StringBuilder("</");
 			sb.append(etiqueta).append(">");
 			int posEtiquetaCierre = documento.indexOf(sb.toString());
 			return documento.substring(posEtiquetaApertura+etiqueta.length()+2,posEtiquetaCierre);
 	}
 	/**
 	 * devuelve el texto enmarcado en una etiqueta. Se supone que la etiqueta exite y tiene valor. Este m&eacute;todo funciona correctamente en el caso de que la etiqueta no tenga
 	 * multiplicidad. En el caso de que tuviera multiplicidad devolver&iacute;a &uacute;nicamente el prime contenido que se encuentre.
 	 * @param etiqueta etiqueta de la que queremos extraer su texto
 	 * @param documento: documento o subdocumento donde esta definida la etiqueta de la cual queremos extraer su texto
 	 * @return String Contenido de la etiqueta. En el caso de que la etiqueta no existiera devuelve <code>null</code>
 	 */
 	public static String dameTextoDeLaEtiquetaSinComprobaciones(String etiqueta,String documento){
 		StringBuilder sb = new StringBuilder("<");
 		sb.append(etiqueta).append(">");
 		int posEtiquetaApertura = documento.indexOf(sb.toString());
 		sb = new StringBuilder("</");
 		sb.append(etiqueta).append(">");
 		int posEtiquetaCierre = documento.indexOf(sb.toString());
 		return documento.substring(posEtiquetaApertura+etiqueta.length()+2,posEtiquetaCierre);
 		
 	}
 
 	/**
 	 * Method dameTextosDeLaEtiqueta. devuelve el texto que contienen la etiqueta. Funciona solo cuando la etiqueta no contiene
 	 * ning&uacute;n patron de documento
 	 * @param etiqueta etiqueta de la que queremos extraer su texto
 	 * @param documento: documento o subdocumento donde esta definida la etiqueta de la cual queremos extraer su texto
 	 * @return Lista de objetos <code>String</code> correspondientes con los textos que pudiera tener la etiqueta (caso de que esta
 	 * tuviera multiplicidad). Si la etiqueta no tiene multiplicidad, devuelve una lista con un &uacute;nico &iacute;tem.<br>
 	 * En el caso de que la etiqueta no existiera devuelve una lista de tama&ntilde;o 0
 	 */
 	public static List dameTextosDeLaEtiqueta(String etiqueta, String documento){
 		if (!XMLHelper.tieneCabecera(documento))
 			documento = XMLHelper.aniadeCabecera(documento);
 		ArrayList listaValores = new ArrayList(20);
 		int posicionEtiqueta = documento.indexOf(etiqueta);
 		while (posicionEtiqueta!=-1){
 			if (!esUnaEtiquetaDeAperturaYCierre(etiqueta,posicionEtiqueta,documento)&& esRealmenteUnaEtiquetaDeApertura(etiqueta,posicionEtiqueta,documento)){
 				int posCierreAperturaEtiqueta = documento.indexOf('>',posicionEtiqueta);
 				int posAperturaCierreEtiqueta = documento.indexOf("</",posCierreAperturaEtiqueta);
 				boolean esRealmenteUnCierreDeEtiqueta=false;
 				int i=1;
 				while (!esRealmenteUnCierreDeEtiqueta){
 					if (documento.charAt(posAperturaCierreEtiqueta-i)!='\\')
 						esRealmenteUnCierreDeEtiqueta=true;
 					else
 						i++;
 				}
 				String texto = documento.substring(posCierreAperturaEtiqueta+1,posAperturaCierreEtiqueta).trim();
 				listaValores.add(texto);
 				posicionEtiqueta=posAperturaCierreEtiqueta+etiqueta.length(); //para avanzar mas rapido
 			}
 			posicionEtiqueta = documento.indexOf(etiqueta,posicionEtiqueta+1);		
 			
 		}
 		return listaValores;
 	}
 	
 	/**
 	 * 
 	 * @param etiqueta
 	 * @param posEtiqueta
 	 * @param documento
 	 * @return
 	 */
 	private static boolean esRealmenteUnaEtiquetaDeApertura(String etiqueta, int posEtiqueta, String documento){
 		if (esElCaracterElPrimerPredecesorQueNoSeaBlanco('<',posEtiqueta,documento)&&esElCaracterElPrimerSucesorQueNoSeaBlanco('>',posEtiqueta+etiqueta.length(),documento))
 			return true;
 		return false;
 	}
 	/**
 	 * 
 	 * @param etiqueta
 	 * @param posEtiqueta
 	 * @param documento
 	 * @return
 	 */
 	private static boolean esUnaEtiquetaDeAperturaYCierre(String etiqueta, int posEtiqueta, String documento){
 		if (esElCaracterElPrimerPredecesorQueNoSeaBlanco('<',posEtiqueta,documento)&&esElCaracterElPrimerSucesorQueNoSeaBlanco('/',posEtiqueta+etiqueta.length(),documento))
 			return true;
 		return false;
 	}
 	/**
 	 * 
 	 * @param caracter
 	 * @param pos
 	 * @param cadena
 	 * @return
 	 */
 	private static boolean esElCaracterElPrimerPredecesorQueNoSeaBlanco(char caracter, int pos, String cadena){
 		if (pos==0)
 			return false;
 		
 		while (true){
 			pos = pos-1;
 			char predecesor = cadena.charAt(pos);
 			if ((predecesor==caracter && pos==0)||(predecesor==caracter && cadena.charAt(pos-1)!='\\')) //para saltar los caracteres con \
 				return true;
 			if (!Character.isWhitespace(predecesor)&&predecesor!=caracter)
 				return false;
 			if (pos==0)
 				return false;
 		}
 	}
 	/**
 	 * 
 	 * @param caracter
 	 * @param pos
 	 * @param cadena
 	 * @return
 	 */
 	private static boolean esElCaracterElPrimerSucesorQueNoSeaBlanco(char caracter, int pos, String cadena){
 		if (pos==cadena.length())
 			return false;
 		while (true){
 			char sucesor = cadena.charAt(pos);
 			if (sucesor==caracter && cadena.charAt(pos-1)!='\\') //para saltar los caracteres con \
 				return true;
 			if (!Character.isWhitespace(sucesor)&&sucesor!=caracter)
 				return false;
 			if (pos==cadena.length())
 				return false;
 			pos = pos+1;
 
 		}
 	}
 	
 	public static String ponTextosDeLaEtiqueta(String etiqueta, String nuevoContenido, String documento){
 		int posicionEtiqueta = documento.indexOf(etiqueta);
 		StringBuilder st= new StringBuilder(documento);
 		while (posicionEtiqueta!=-1){
 			if (!esUnaEtiquetaDeAperturaYCierre(etiqueta,posicionEtiqueta,documento)&& esRealmenteUnaEtiquetaDeApertura(etiqueta,posicionEtiqueta,documento)){
 				int posCierreAperturaEtiqueta = documento.indexOf('>',posicionEtiqueta);
 				int posAperturaCierreEtiqueta = documento.indexOf("</",posCierreAperturaEtiqueta);
 				boolean esRealmenteUnCierreDeEtiqueta=false;
 				int i=1;
 				while (!esRealmenteUnCierreDeEtiqueta){
 					if (documento.charAt(posAperturaCierreEtiqueta-i)!='\\')
 						esRealmenteUnCierreDeEtiqueta=true;
 					else
 						i++;
 				}
 				st.replace(posCierreAperturaEtiqueta+1, posAperturaCierreEtiqueta, nuevoContenido);
 				posicionEtiqueta=posAperturaCierreEtiqueta+etiqueta.length(); //para avanzar mas rapido
 			}
 			posicionEtiqueta = documento.indexOf(etiqueta,posicionEtiqueta+1);			
 		}
 		return st.toString();
 	}
 	
 	/**
 	 * Este metodo esta especialmente pensado para cuando hay que poner un valor para una etiqueta y se busca mucha velocidad en detrimento de la seguridad.
 	 * Aqui se supone que existe la etiqueta de apertura y de cierre
 	 * @param etiqueta
 	 * @param nuevoContenido
 	 * @param documento
 	 * @return
 	 */
 	public static String ponTextoDeLaEtiquetaSinComprobaciones(String etiqueta, String nuevoContenido, String documento){
 		try{
 			StringBuilder etiquetaCompletaApertura = new StringBuilder("<");
 			etiquetaCompletaApertura.append(etiqueta).append(">");
 			StringBuilder etiquetaCompletaCierre = new StringBuilder("</");
 			etiquetaCompletaCierre.append(etiqueta).append(">");
 			
 			int posicionAperturaEtiqueta = documento.indexOf(etiquetaCompletaApertura.toString());
 			int posicionCierreEtiqueta = documento.indexOf(etiquetaCompletaCierre.toString());
 			StringBuilder st= new StringBuilder(documento);
 			st.replace(posicionAperturaEtiqueta+etiquetaCompletaApertura.length(), posicionCierreEtiqueta, nuevoContenido);
 			return st.toString();
 		}catch (Exception e){
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Borra la etiqueta con el tag y contenido del documento incluso si es xml
 	 * Se supone que el documento tiene que ser válido.
 	 * 
 	 */   	
 
 	public static String borraEtiqueta(String tag, String contenido, String documento){
 		StringBuilder st = new StringBuilder(documento);
 		int posBusquedaInicio = 0;
 		int tamanioDocumento = st.length();
 		while (posBusquedaInicio<tamanioDocumento) {
 			int posInicio = st.indexOf("<"+tag+">", posBusquedaInicio);
 			if (posInicio!=-1){
 				String t = "</"+tag+">";
 				int posFin = st.indexOf(t, posBusquedaInicio);
 				int longitudTag=t.length();
 				int posContenido = documento.indexOf(contenido);
 				if (posInicio<posContenido && posContenido<posFin) {
 					st.delete(posInicio, posFin+longitudTag);
 					return st.toString();
 				}
 				posBusquedaInicio = posFin+longitudTag;
 
 			} else {
 				return documento;
 			}
 				
 		}
 		return documento;
 	}	
 	   	
         
 	/**
 	 * Method reemplazaTextoDeLaEtiqueta. Dado una etiqueta con un contenido, lo reemplaza en todas las apariciones por uno nuevo en el documento dado
 	 * @param etiqueta Etiqueta a la que queremos reemplazarle el contenido. La etiqueta debe corresponderse con un item
 	 * @param contenidoViejo Contenido que queremos reemplazar
 	 * @param contenidoNuevo Nuevo contenido a poner en la etiqueta indicada
 	 * @param documento Documento con la etiqueta y contenido a reemplazar
 	 * @return String Documento con el contenido reemplazado
 	 */
        public static String reemplazaTextoDeLaEtiqueta(String etiqueta, String contenidoViejo, String contenidoNuevo, String documento) {
             StringBuilder st = new StringBuilder(documento);
             String cadenaVieja = (new StringBuilder("<").append(etiqueta).append(">").append(contenidoViejo).append("</").append(etiqueta).append(">")).toString();
             int posIni = documento.indexOf(cadenaVieja);
             while (posIni!=-1){
                 String cadenaNueva = (new StringBuilder("<").append(etiqueta).append(">").append(contenidoNuevo).append("</").append(etiqueta).append(">")).toString();       		
                 st.replace(posIni, posIni+cadenaVieja.length(), cadenaNueva);
                 posIni = st.indexOf(cadenaVieja);
                 if (cadenaVieja.equals(cadenaNueva))
                 	posIni=-1;
             }
             return st.toString();
        }
 
 
 	/**
 	 * Method ordenaListaDeDocumento. Ordena una lista de documentos dada seg&uacute;n el item que se le pase.
 	 * La ordenaci&oacute;n se realiza exclusivamente ascendentemente
 	 * @param listaAOrdenar Lista que queremos ordenar
 	 * @param item ItemPatron por el que queremos ordenar
 	 * @return List Lista de documentos XML ordenada. Lista vac&iacute;a en el caso de que los documentos a ordenar no
 	 * tuvieran el item establecido.
 	 */
 	public static List ordenaListaDeDocumento(List listaAOrdenar, String item) {
 		SortedMap mapOrdenado = new TreeMap();
 		Iterator it = listaAOrdenar.iterator();
 		while (it.hasNext()) {
 			String documento = (String)it.next();
 			List listaClave = dameTextosDeLaEtiqueta(item, documento);
			if (listaClave!=null || listaClave.isEmpty()) {
 				SortedSet setOrdenadaDeClaves= new TreeSet(listaClave);
 				String primeraClave = (String)setOrdenadaDeClaves.first();
 				mapOrdenado.put(primeraClave, documento);			
 			}
 		}	
 		return new ArrayList(mapOrdenado.values());
 	}
 	/**
 	 * 
 	 * @param nombreNuevo
 	 * @param nombreViejo
 	 * @param documento
 	 * @return
 	 */
 	public static String renombraEtiqueta(String nombreNuevo, String nombreViejo, String documento){
 		
 		documento = documento.replaceAll("<"+nombreViejo+">","<"+nombreNuevo+">");
 		documento = documento.replaceAll("</"+nombreViejo+">","</"+nombreNuevo+">");
 		documento = documento.replaceAll("<"+nombreViejo+"/>","<"+nombreNuevo+">");
 		return documento;
 	}
 	/**
 	 * Concatena un documento xml como si fuera la ultima etiqueta de otro documento xml
 	 * @param xml1: documento sobre el que se va a concatenar
 	 * @param xml2: documento a concatenar
 	 * @return el resultado de concatenar xm12 al final de xml1 (justo antes de la etiqueta de cierre)
 	 */
 	public static String concatenaXML(String xml1,String xml2) throws XMLHelperException{
 		if (xml1 == null) {
 			throw new XMLHelperException("\n{XMLHelper.concatenaXML()}[]. El primer parametro debe ser un documento xml valido y en este caso es null");
 		}
 		if (xml2==null) {
 			xml2="";
 		}
 		xml1 = siTieneCabeceraEliminala(xml1);
 		xml2 = siTieneCabeceraEliminala(xml2);
 		StringBuilder resultado = new StringBuilder(xml1);
 		
 		resultado.insert(xml1.lastIndexOf("<"),xml2);
 	
 		String r =  XMLHelper.aniadeCabecera(resultado.toString());
 		return r;
 	}
 	/**
 	 * Method concatenaListaDeResultados.
 	 * @param listaAConcatenar Lista de documentos XML que queremos concatenar
 	 * @param nombreDeLaLista Nombre que va a llevar la lista de documentos en el documento XML resultante
 	 * @return String
 
 	 */
 	public static String concatenaListaDeResultados(List listaAConcatenar, String nombreDeLaLista) throws XMLHelperException {
 		if (listaAConcatenar == null) {
 			throw new XMLHelperException("\n{XMLHelper.concatenaListaDeResultados}[]: La lista a concatenar es null.");
 		}
 	
 		StringBuilder resultado = new StringBuilder("");
 		if (!listaAConcatenar.isEmpty()) {
 			resultado.append("<").append(nombreDeLaLista).append(">");	
 			Iterator itLista = listaAConcatenar.iterator();
 			String documentoAConcatenar;
 			while (itLista.hasNext()) {
 				documentoAConcatenar = (String)itLista.next();
 				documentoAConcatenar = siTieneCabeceraEliminala(documentoAConcatenar);
 				resultado.append(documentoAConcatenar);
 			}
 			resultado.append("</").append(nombreDeLaLista).append(">");			
 		}
 		String r =  XMLHelper.aniadeCabecera(resultado.toString());
 		return r;
 	}	
 	/**
 	 * 
 	 * @param doc
 	 * @return
 	 */
 	private static String siTieneCabeceraEliminala(String doc){
 		if (doc.indexOf("<?xml")!=-1)
 			doc = XMLHelper.eliminaCabecera(doc);
 		return doc;
 	}	
 	
 	/**
 	 * param  String Documento XML del que queremos obtener la lista de etiquetas
 	 * @return Lista de <code>String</code> con todos los nombre de las etiquetas que tiene el documento. 
 	 * Devuelve una lista vac&iacute en elcaso de que el documento no tuviera etiquetas. Si tuviera etiquetas repetidas, 
 	 * solo devuelve el nombre de la etiqueta una vez.
 	 */
 	public static List dameListaDeEtiquetas(String documento) {
 		List listaEtiquetas=new Vector();
 		int posEtiqueta=documento.indexOf('<',1);
 		while (posEtiqueta!=-1) {
 			int finEtiqueta=documento.indexOf('>', posEtiqueta+1);
 			String etiqueta=documento.substring(posEtiqueta+1, finEtiqueta);
 			if (!listaEtiquetas.contains(etiqueta) && (etiqueta.indexOf('/')==-1) ) 
 				listaEtiquetas.add(etiqueta);
 			posEtiqueta=documento.indexOf('<',finEtiqueta);
 		}
 		return listaEtiquetas;
 	}
 		
 	/**
 	 * Ejemplo de uso: tenemos un documento y queremos eliminar el trozo enmarcado entre las etiquetas et1 y que tengan una etiqueta et2 con un valor "v"
 	 * llamariamos a este metodo así: eliminaSubdocumento(documento,"et1","et2","v");
 	 * Este metodos se diseño para ser utilizado para eliminar subdocumentos sincronizados.
 	 * @param documento
 	 * @param etiquetaSubdocumento
 	 * @param etiquetaDiscriminador
 	 * @param valorDiscriminador
 	 * @return
 	 */
 	public static String eliminaSubdocumento(String documento, String etiquetaSubdocumento, String etiquetaDiscriminador, String valorDiscriminador){
 		StringBuilder documentoCapado = new StringBuilder();
 		String etiquetaApertura = "<"+etiquetaSubdocumento+">";
 		String etiquetaCierre = "</"+etiquetaSubdocumento+">";
 		String tagDiscriminador = "<"+etiquetaDiscriminador+">"+valorDiscriminador+"</"+etiquetaDiscriminador+">";
 		int posicionEtiquetaApertura = documento.indexOf(etiquetaApertura);
 		int ultimaPosicionDeCorte=0;
 		while (posicionEtiquetaApertura!=-1){
 			int posicionEtiquetaCierre = documento.indexOf(etiquetaCierre,posicionEtiquetaApertura);
 			int posicionTagDiscriminador = documento.indexOf(tagDiscriminador,posicionEtiquetaApertura);
 			if (posicionTagDiscriminador<posicionEtiquetaCierre && posicionTagDiscriminador!=-1){ //tenemos que eliminar
 				documentoCapado.append(documento.substring(ultimaPosicionDeCorte,posicionEtiquetaApertura));
 				ultimaPosicionDeCorte = posicionEtiquetaCierre+etiquetaCierre.length();
 			}
 			posicionEtiquetaApertura = documento.indexOf(etiquetaApertura,posicionEtiquetaCierre);
 		}
 		return documentoCapado.append(documento.substring(ultimaPosicionDeCorte)).toString();
 	}
 	
 	/**
 	 * Ejemplo de uso: tenemos un documento y queremos eliminar el trozo enmarcado entre las etiquetas et1 y que tengan una etiqueta et2 con un valor "v"
 	 * llamariamos a este metodo así: eliminaSubdocumento(documento,"et1","et2","v");
 	 * Este metodos se diseño para ser utilizado para eliminar subdocumentos sincronizados.
 	 * @param documento
 	 * @param etiquetaSubdocumento
 	 * @return
 	 */
 	public static String eliminaSubdocumento(String documento, String etiquetaSubdocumento){
 		StringBuilder documentoCapado = new StringBuilder();
 		String etiquetaApertura = "<"+etiquetaSubdocumento+">";
 		String etiquetaCierre = "</"+etiquetaSubdocumento+">";
 		int posicionEtiquetaApertura = documento.indexOf(etiquetaApertura);
 		int ultimaPosicionDeCorte=0;
 		while (posicionEtiquetaApertura!=-1){
 			int posicionEtiquetaCierre = documento.indexOf(etiquetaCierre,posicionEtiquetaApertura);
 			documentoCapado.append(documento.substring(ultimaPosicionDeCorte,posicionEtiquetaApertura));
 			ultimaPosicionDeCorte = posicionEtiquetaCierre+etiquetaCierre.length();
 
 			posicionEtiquetaApertura = documento.indexOf(etiquetaApertura,posicionEtiquetaCierre);
 		}
 		return documentoCapado.append(documento.substring(ultimaPosicionDeCorte)).toString();
 	}
 	/**
 	 * 
 	 * @param documento
 	 * @param etiquetaSubdocumento
 	 * param paresDiscriminador, pares con valores etiqueta, valor (generalmente de etiquetas primarias) que denotan el subdocumento
 	 * @return
 	 */
 	public static String eliminaSubdocumento(String documento, String etiquetaSubdocumento, List paresDiscriminador){
 		StringBuilder documentoCapado = new StringBuilder();
 		String etiquetaApertura = "<"+etiquetaSubdocumento+">";
 		String etiquetaCierre = "</"+etiquetaSubdocumento+">";
 		int posicionEtiquetaApertura = documento.indexOf(etiquetaApertura);
 		int ultimaPosicionDeCorte=0;
 		while (posicionEtiquetaApertura!=-1){
 			int posicionEtiquetaCierre = documento.indexOf(etiquetaCierre,posicionEtiquetaApertura);
 			Iterator itDiscriminadores = paresDiscriminador.iterator();
 			boolean hayQueCapar = true;
 			while (itDiscriminadores.hasNext()){
 				Par par = (Par) itDiscriminadores.next();
 				String etiquetaDiscriminador = (String)par.getObjeto1();
 				String valorDiscriminador = (String)par.getObjeto2();
 				String tagDiscriminador = "<"+etiquetaDiscriminador+">"+valorDiscriminador+"</"+etiquetaDiscriminador+">";
 				int posicionTagDiscriminador = documento.indexOf(tagDiscriminador,posicionEtiquetaApertura);
 				if (posicionTagDiscriminador>posicionEtiquetaCierre || posicionTagDiscriminador==-1){ //tenemos que eliminar
 					hayQueCapar = false;
 				}
 			}
 			if (hayQueCapar){
 				documentoCapado.append(documento.substring(ultimaPosicionDeCorte,posicionEtiquetaApertura));
 				ultimaPosicionDeCorte = posicionEtiquetaCierre+etiquetaCierre.length();
 			}
 			posicionEtiquetaApertura = documento.indexOf(etiquetaApertura,posicionEtiquetaCierre);
 		}
 		return documentoCapado.append(documento.substring(ultimaPosicionDeCorte)).toString();
 	}
  	/**
  	 * aniade una etiqueta a un documento XML situada dentro del documento bajo el padre que le indiquemos. En el caso de que el
  	 * padre de la etiqueta tenga multiplicidad, se a&ntilde;adir&aacute; bajo la primera etiqueta padre que encontremos
 	 * @param nombreEtiqueta Nombre de la etiqueta que vamos a a&ntilde;adir al documento. Solo se le pasara el nombre si los 
 	 * simbolos de mayor y menor
 	 * @param textoEtiqueta Texto que va a llevar la etiqueta que vamos a a&ntilde;adir
 	 * @param nombreEtiquetaPadre Etiqueta que va a ser el padre en el &aacute;rbol XML de la etiqueta que vamos a a&ntilde;adir.
 	 * Solo se le pasara el nombre si los simbolos de mayor y menor.
 	 * @param documento documento sobre el que vamos a realizar la operaci&oacute;
 	 * @return
 	 */
 	public static String ponEtiquetaYTextoDeLaEtiqueta(String nombreEtiqueta,String textoEtiqueta, String nombreEtiquetaPadre, String documento){
 		StringBuilder stTmp = new StringBuilder();
 		stTmp.append("</").append(nombreEtiquetaPadre).append(">");		
 		StringBuilder stAniadir = new StringBuilder();
 		stAniadir.append("<").append(nombreEtiqueta).append(">").append(textoEtiqueta).append("</").append(nombreEtiqueta).append(">");
 		StringBuilder st= new StringBuilder(documento);
 		
 		int posicionEtiqueta = documento.indexOf(stTmp.toString());
 		st.insert(posicionEtiqueta, stAniadir);		
 		return st.toString();
  	}
 	
 	/**
 	 * @param nombreEtiqueta Nombre de la etiqueta que hemos de comprobar si contiene sin los s&iacute;mbolos de aportura y cierre: < >
 	 * @param documento Documento XML con o sin cabecera,  para el cual queremos comprobar si tiene la etiqueta
 	 * @return true si el documento contiene a la etiqueta.
 	 */
 	public static boolean contieneEtiqueta(String nombreEtiqueta, String documento) {
 		if (documento.indexOf("<"+nombreEtiqueta+">")!=-1)
 			return true;
 		return false;
 	}
 }

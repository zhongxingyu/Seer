 package webservice;
 import java.io.IOException;
 import org.apache.http.client.ClientProtocolException;
 import android.util.Log;
  
 public class AplicacionWeb{
 	private Coneccion coneccion;
 	// WebServices URL
 	String serverName = "www.informaticacurico.cl";
 	String urlLogin = "/listas/login.php";
 	
 	public AplicacionWeb(){
 		coneccion = new Coneccion(serverName);
 	}
 	
 	/**
 	 * Autentificar al usuario
 	 * parametros	: usuario, password
 	 * retorno		: boleano indicando el estado de la autentificacion
 	 * @throws AppWebException 
 	 * @throws IOException 
 	 * @throws ClientProtocolException 
 	 */
 
 	public String loginUsuario(String usuario, String password) throws AppWebException {
 		 // PRIMERO: Enviar el POST y obtener el string de retorno
 		String str = "nada...";
 		try {				
 			str = coneccion.GET_generico(urlLogin);
 			
 			//Log.i("INTERNET", "logeando con usuario:"+usuario+" y password:"+password);
			//Log.i("INTERNET", "El servidor responde: "+str );
 		} catch (ClientProtocolException e) {
			Log.i("DEBUG", "CPE "+e.toString() );
 			throw new AppWebException("Problemas al conectar con el servidor");
 			
 		} catch (IOException e) {
			Log.i("DEBUG", "EXC "+e.toString() );
 			throw new AppWebException("Coneccion no disponible");
 		}
 		return str;
 		
 		// SEGUNDO: Convertir el String a JSON
 
 		//JSONObject jso_resumen;
 		//try{
 		//	jso_resumen = new JSONObject( strRespuesta );
 		//}catch( org.json.JSONException e2){
 		//	throw new Exception("JSON mal formateado o desconocido");
 		//}
 		
 		// TERCERO: se ha logeado correctamente?
 	}
 
 	
 	
 	/**
 	 * Obtiene todos los grupos que exisetn en el servidor
 	 * retorno		: un arreglo con todos los grupos del servidor
 	 */
 	public void getGroups(){}
 
 	
 	/**
 	 * Obtiene los ultimos N mensajes de un grupo
 	 * parametros	: el id del grupo, la cantidad de mensajes
 	 * retorno   	: un arreglo con los ultimos 10 mensajes del grupo
 	 */
 	public void getLastMessagesFromGroup(){}
 	
 	/**
 	 * Obtiene los mensajes de todo los grupos en los que esta subscrito
 	 * parametros	: conjunto de ID de los grupos a los que seta subscrito
 	 * retorna		: los ultimos N mensajes de cada uno de los grupos
 	 */
 	public void getLastMessagesFromSubscribedGroups(){}
 }

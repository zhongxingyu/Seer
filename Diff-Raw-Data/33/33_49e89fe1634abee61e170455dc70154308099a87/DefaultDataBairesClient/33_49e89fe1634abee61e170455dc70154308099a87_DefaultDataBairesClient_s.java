 package ar.com.restba;
 
import java.io.FileNotFoundException;
import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
import java.util.Collections;
 import java.util.List;
 
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

 import ar.com.restba.types.ObrasRegistradas;
 import au.com.bytecode.opencsv.CSVReader;
 
import com.restfb.Parameter;
 import com.restfb.json.JsonArray;
 import com.restfb.json.JsonObject;
 
 public class DefaultDataBairesClient implements DataBairesClient {
 
 	private RestFbConnector restFbConnector;
 
 	public DefaultDataBairesClient() {
 		restFbConnector = new RestFbConnector();
 		;
 	}
 
 	@Override
 	public List<ObrasRegistradas> fetchObrasRegistradas() {
 
 		JsonObject fetchObject = restFbConnector.fetchObject(
 				"obras-registradas", JsonObject.class);
 		JsonArray resources = fetchObject.getJsonArray("resources");
 
 		for (int i = 0; i < resources.length(); i++) {
 			JsonObject resource = resources.getJsonObject(i);
 			String url = resource.getString("url")
 					.replaceFirst("https", "http");
 			System.out.println(url);
 			InputStream inputStream = null;
 			URL urlconnect;
 			try {
 				urlconnect = new URL(url);
 				URLConnection uc = urlconnect.openConnection();
 				inputStream = uc.getInputStream();
 			} catch (MalformedURLException eURL) {
 				throw new RuntimeException(eURL);
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			}
 
			List<ObrasRegistradas> obrasRegistradas = new ArrayList<ObrasRegistradas>();
 			CSVReader reader = null;
 			reader = new CSVReader(new InputStreamReader(inputStream));
 			String[] nextLine;
 			try {
 				while ((nextLine = reader.readNext()) != null) {
 					ObrasRegistradas entity = new ObrasRegistradas();
 					entity.setnExpediente(nextLine[0]);
 					entity.setDireccion(nextLine[1]);
 					entity.setSmp(nextLine[2]);
 					entity.setEstadoTramite(nextLine[3]);
 					entity.setFechaEstado(nextLine[4]);
 					entity.setTipoObra(nextLine[5]);
 					entity.setNombreProfesional(nextLine[6]);
 					obrasRegistradas.add(entity);
 				}
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			} finally {
 				try {
 					reader.close();
 				} catch (IOException e) {
 					throw new RuntimeException(e);
 				}
 			}
			obrasRegistradas.remove(0);
			return obrasRegistradas;
 		}

		return Collections.emptyList();
 	}
 
 }

 package ar.com.restba;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.List;
 
 import ar.com.restba.types.ObrasRegistradas;
 import au.com.bytecode.opencsv.CSVReader;
 
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
 
		List<ObrasRegistradas> obrasRegistradas = new ArrayList<ObrasRegistradas>();

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
 
 			CSVReader reader = null;
 			reader = new CSVReader(new InputStreamReader(inputStream));
 			String[] nextLine;
			boolean firstTime = true;
 			try {
 				while ((nextLine = reader.readNext()) != null) {

					// To Remove Header of CSV
					if (firstTime) {
						firstTime = false;
						continue;
					}
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
 		}
		return obrasRegistradas;
 	}
 
 }

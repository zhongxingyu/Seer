 package com.cinemar.phoneticket.films;
 
 import com.cinemar.phoneticket.external.APIClient;
 import com.cinemar.phoneticket.external.RestClient;
 import com.cinemar.phoneticket.model.Film;
 import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
 
 
 public class FilmsClientAPI {
 
 	private RestClient client;
 
 	public FilmsClientAPI() {
 		client = new APIClient();
 	}
 	
 	public void getFilms(JsonHttpResponseHandler responseHandler){			
 		client.get("movies.json", responseHandler);		
 	}
 	
 	public void getFunciones(Film film, JsonHttpResponseHandler responseHandler){
 		client.get("movies/" + film.getId() + ".json", responseHandler);
 	}
 	
 	public void getFuncionesEnComplejo(String filmId, String complejoId, JsonHttpResponseHandler responseHandler){
		RequestParams parameters = new RequestParams();
		parameters.put("theatre_id", complejoId);
		
		client.get("movies/" + filmId + ".json",parameters, responseHandler); //TODO: Usar ruta correcta para filtrar con el complejoId
 	}
 	
 
 }

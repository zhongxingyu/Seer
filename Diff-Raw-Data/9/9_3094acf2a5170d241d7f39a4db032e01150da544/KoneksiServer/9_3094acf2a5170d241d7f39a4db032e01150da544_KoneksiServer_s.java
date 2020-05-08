 package com.muhardin.endy.belajar.spring.android.service;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.springframework.http.HttpEntity;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpMethod;
 import org.springframework.http.MediaType;
 import org.springframework.http.ResponseEntity;
 import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
 import org.springframework.web.client.RestTemplate;
 
 import com.muhardin.endy.belajar.spring.android.domain.Produk;
 
 public class KoneksiServer {
	private String ipServer;
	private String portServer;
	
	final String urlDaftarProduk = "http://"+ipServer+":"+portServer+"/belajar-spring-android-server/master/produk";
 
 	public KoneksiServer(String ipServer, String portServer) {
		this.ipServer = ipServer;
		this.portServer = portServer;
 	}
 	
 	public List<Produk> ambilDataProduk(){
 		// Set the Accept header for "application/json"
 		HttpHeaders requestHeaders = new HttpHeaders();
 		List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();
 		acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
 		requestHeaders.setAccept(acceptableMediaTypes);
 
 		// Populate the headers in an HttpEntity object to use for the request
 		HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);
 
 		// Create a new RestTemplate instance
 		RestTemplate restTemplate = new RestTemplate();
 		restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
 
 		// Perform the HTTP GET request
 		ResponseEntity<Produk[]> responseEntity = restTemplate.exchange(urlDaftarProduk, HttpMethod.GET, requestEntity,
 				Produk[].class);
 
 		// convert the array to a list and return it
 		return Arrays.asList(responseEntity.getBody());
 	}
 }

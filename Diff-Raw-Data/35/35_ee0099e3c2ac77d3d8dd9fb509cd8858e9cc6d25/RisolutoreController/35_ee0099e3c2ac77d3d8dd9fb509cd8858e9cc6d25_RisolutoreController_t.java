 package eu.trentorise.smartcampus.universiadi.controller;
 
 import java.io.IOException;
 import java.util.ArrayList;
import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.params.ConnManagerParams;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import eu.trentorise.smartcampus.universiadi.model.TicketObj;
 import eu.trentorise.smartcampus.universiadi.model.UserObj;
 import eu.trentorise.smartcampus.universiadi.model.containerData.ContainerUtenti;
 import eu.trentorise.smartcampus.universiadi.util.EasyTokenManger;
 
 @Controller("risolutoreController")
 public class RisolutoreController {
 	
 	
 	@Autowired
 	@Value("${clientidsc}")
 	private String client_sc_Id;
 
 	@Autowired
 	@Value("${clientscsecrect}")
 	private String client_sc_secret;
 	
 	@Autowired
 	@Value("${fix.juniper.token}")
 	private String client_juniper_token;	
 
 	@Autowired
 	@Value("${usertoken}")
 	private String userToken;
 
 
 	@Autowired
 	@Value("${profile.address}")
 	private String profileAddress;
 	
 	/** Timeout (in ms) we specify for each http request */
 	public static final int HTTP_REQUEST_TIMEOUT_MS = 30 * 1000;
 	
 	
 	@Autowired
 	@Value("${juniper.address}")
 	private String juniperAddress;
 
 	
 	
 	private EasyTokenManger easyTokenManger;
 
 	
 		
 	@PostConstruct
 	public void init() {
 		easyTokenManger=new EasyTokenManger(client_sc_Id,client_sc_secret,client_juniper_token,userToken,profileAddress);
 	
 	}
 
 	@RequestMapping(method = RequestMethod.GET, value = "/categoria_volontari")
 	public @ResponseBody
 	ArrayList<String> getCategorieVolontari(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session) {
 
 		
 
 		ArrayList<String> mCategorie = new ArrayList<String>();
 		for (UserObj utente : ContainerUtenti.getUtenti())
 			if (!mCategorie.contains(utente.getAmbito()))
 				mCategorie.add(utente.getAmbito());
 
 		return mCategorie;
 	}
 
 	@RequestMapping(method = RequestMethod.GET, value = "/utenti/{ambito}/{ruolo}")
 	public @ResponseBody
 	ArrayList<UserObj> getCategorieVolontari(HttpServletRequest request,
 			@PathVariable("ambito") String ambito,
 			@PathVariable("ruolo") String ruolo, HttpServletResponse response,
 			HttpSession session) {
 
 		
 
 		return ContainerUtenti.getUtentiForAmbitoAndRuolo(ambito, ruolo);
 	}
 
 	@RequestMapping(method = RequestMethod.POST, value = "/send_sms")
 	public @ResponseBody
 	boolean sendSMS(HttpServletRequest request, HttpServletResponse response,
 			HttpSession session, @RequestBody TicketObj ticket,
 			@RequestBody ArrayList<UserObj> receiver) throws 
 			IOException {
 		return true;
 	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/ticket/all")
	//List< HelpDeskTicketObj > getMyTickets()
	public @ResponseBody
	List<TicketObj> getMyTickets(HttpServletRequest request,
			HttpServletResponse response, HttpSession session){
		final HttpResponse resp;	
		
		String url =  juniperAddress + "getMyTickets";
		final HttpGet post = new HttpGet(url);				
		post.setHeader("Authorization", easyTokenManger.getClientJuniperToken());
		

		try {
			resp = getHttpClient().execute(post);
			
			final String responseString = EntityUtils.toString(resp.getEntity());
			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				return new ArrayList();
			}
			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
 
 	@RequestMapping(method = RequestMethod.POST, value = "/send_helpdesk")
 	public @ResponseBody
 	boolean sendHelpdesk(HttpServletRequest request,
 			HttpServletResponse response,@RequestBody TicketObj ticket, HttpSession session) throws  IOException
 			 {
 		//,@RequestBody TicketObj ticket,
 		//boolean sendHelpdesk(TicketObj ticket)
 		//{"GPS": [0,0], "Descrizione": "test", "Ambito": "", "Foto": "", "Indirizzo": "via test","Telefono": "00000"}
 		
 		
 		final HttpResponse resp;
 		
 		//test
 		//TicketObj ticket=new TicketObj(new GeoPoint(), "test",  "test",  "test",  "test",  "test");
 		//test
 		
 		
 		final StringEntity entity = new StringEntity(ticket.toJson(), HTTP.UTF_8);
 		entity.setContentType("application/json");
 		
 		String url =  juniperAddress + "sendHelpdesk";
 		final HttpPost post = new HttpPost(url);
 		post.setEntity(entity);
 		post.setHeader("Accept", "application/json");
 		post.setHeader("Authorization", easyTokenManger.getClientJuniperToken());
 		
 
 		try {
 			resp = getHttpClient().execute(post);
 			
 			final String responseString = EntityUtils.toString(resp.getEntity());
 			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
 				return (responseString.contains("NEW"));
 			}
 			
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return false;
 	}
 
 	
 	
 	
 
 
 	protected static HttpClient getHttpClient() {
 		HttpClient httpClient = new DefaultHttpClient();
 		final HttpParams params = httpClient.getParams();
 		HttpConnectionParams.setConnectionTimeout(params,
 				HTTP_REQUEST_TIMEOUT_MS);
 		HttpConnectionParams.setSoTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
 		ConnManagerParams.setTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
 		return httpClient;
 	}
 
 	
 	
 
 }

 package smartcampus.webtemplate.controllers;
 
 import it.sayservice.platform.smartplanner.data.message.Itinerary;
 import it.sayservice.platform.smartplanner.data.message.Position;
 import it.sayservice.platform.smartplanner.data.message.RType;
 import it.sayservice.platform.smartplanner.data.message.TType;
 import it.sayservice.platform.smartplanner.data.message.journey.SingleJourney;
 import it.sayservice.platform.smartplanner.data.message.otpbeans.Route;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import eu.trentorise.smartcampus.ac.provider.AcService;
 import eu.trentorise.smartcampus.ac.provider.filters.AcProviderFilter;
 import eu.trentorise.smartcampus.ac.provider.model.User;
 import eu.trentorise.smartcampus.communicator.CommunicatorConnector;
 import eu.trentorise.smartcampus.communicator.CommunicatorConnectorException;
 import eu.trentorise.smartcampus.communicator.model.Notification;
 import eu.trentorise.smartcampus.communicator.model.NotificationAuthor;
 import eu.trentorise.smartcampus.controllers.SCController;
 import eu.trentorise.smartcampus.corsi.model.Commento;
 import eu.trentorise.smartcampus.corsi.model.Corso;
 import eu.trentorise.smartcampus.corsi.model.CorsoLite;
 import eu.trentorise.smartcampus.corsi.model.UtenteCorsi;
 import eu.trentorise.smartcampus.corsi.repository.CorsoLiteRepository;
 import eu.trentorise.smartcampus.discovertrento.DiscoverTrentoConnector;
 import eu.trentorise.smartcampus.dt.model.EventObject;
 import eu.trentorise.smartcampus.dt.model.ObjectFilter;
 import eu.trentorise.smartcampus.filestorage.client.Filestorage;
 import eu.trentorise.smartcampus.filestorage.client.FilestorageException;
 import eu.trentorise.smartcampus.filestorage.client.model.AppAccount;
 import eu.trentorise.smartcampus.filestorage.client.model.Metadata;
 import eu.trentorise.smartcampus.filestorage.client.model.UserAccount;
 import eu.trentorise.smartcampus.journeyplanner.JourneyPlannerConnector;
 import eu.trentorise.smartcampus.profileservice.ProfileConnector;
 import eu.trentorise.smartcampus.profileservice.model.BasicProfile;
 import eu.trentorise.smartcampus.socialservice.SocialService;
 import eu.trentorise.smartcampus.socialservice.SocialServiceException;
 import eu.trentorise.smartcampus.socialservice.model.Group;
 
 
 @Controller("corsiController")
 public class CorsiController extends SCController
 {
 	private static final String EVENT_OBJECT = "eu.trentorise.smartcampus.dt.model.EventObject";
 	private static final Logger logger = Logger.getLogger(CorsiController.class);
 	@Autowired
 	private AcService acService;
 
 	/*
 	 * the base url of the service. Configure it in webtemplate.properties
 	 */
 	@Autowired
 	@Value("${services.server}")
 	private String serverAddress;
 
 	/*
 	 * the base appName of the service. Configure it in webtemplate.properties
 	 */
 	@Autowired
 	@Value("${webapp.name}")
 	private String appName;
 	
 	
 	@Autowired
 	private CorsoLiteRepository corsoLiteRepository;
 	
 	
 	/*
 	 *   Ritorna tutti i corsi in versione lite
 	 */
 	@RequestMapping(method = RequestMethod.GET, value = "/corsi/all")
 	public @ResponseBody
 	
 	List<CorsoLite> getCorsiAll(HttpServletRequest request, HttpServletResponse response, HttpSession session)
 	
 	throws IOException
 	{
 		try
 		{
 			String token = request.getHeader(AcProviderFilter.TOKEN_HEADER);
 			ProfileConnector profileConnector = new ProfileConnector(serverAddress);
 		
 			
 			//TEST
 			CorsoLite c = new CorsoLite();
 		
 			c.setNome("Fisica dei materiali");
 			corsoLiteRepository.save(c);
 			
 			c = new CorsoLite();
 		
 			c.setNome("Analisi matematica 2");
 			corsoLiteRepository.save(c);
 			
 			c = new CorsoLite();
 	
 			c.setNome("Lettere 1");
 			corsoLiteRepository.save(c);
 			
 			//TEST
 			
 			return corsoLiteRepository.findAll();
 		}
 		catch (Exception e)
 		{
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 		return null;
 	}
 	
 	
 	/*
 	 *   Ritorna tutti i corsi del corso di laurea in versione lite
 	 */
 	@RequestMapping(method = RequestMethod.GET, value = "/corsi/laurea")
 	public @ResponseBody
 	
 	List<CorsoLite> getCorsiLaurea(HttpServletRequest request, HttpServletResponse response, HttpSession session)
 	
 	throws IOException
 	{
 		try
 		{
 			String token = request.getHeader(AcProviderFilter.TOKEN_HEADER);
 			ProfileConnector profileConnector = new ProfileConnector(serverAddress);
 			BasicProfile profile = profileConnector.getBasicProfile(token);
 						
 			ArrayList<CorsoLite> list = new ArrayList<CorsoLite>();
 			
 			CorsoLite c = new CorsoLite();
 			c.setId(1);
 			c.setNome("Analisi matematica 3");
 			list.add(c);	
 			
 			c = new CorsoLite();
 			c.setId(2);
 			c.setNome("Chimica 2");
 			list.add(c);	
 			
 			c = new CorsoLite();
 			c.setId(3);
 			c.setNome("Fisica 2");
 			list.add(c);	
 			
 			return list;
 		}
 		catch (Exception e)
 		{
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 		return null;
 	}
 	
 	
 	/*
 	 * Ritorna tutti i corsi del dipartimento in versione lite
 	 */
 	@RequestMapping(method = RequestMethod.GET, value = "/corsi/dipartimento")
 	public @ResponseBody
 	
 	List<CorsoLite> getCorsiDipartimento(HttpServletRequest request, HttpServletResponse response, HttpSession session)
 	
 	throws IOException
 	{
 		try
 		{
 			String token = request.getHeader(AcProviderFilter.TOKEN_HEADER);
 			ProfileConnector profileConnector = new ProfileConnector(serverAddress);
 			BasicProfile profile = profileConnector.getBasicProfile(token);
 						
 			ArrayList<CorsoLite> list = new ArrayList<CorsoLite>();
 			
 			CorsoLite c = new CorsoLite();
 			c.setId(1);
 			c.setNome("Analisi matematica 3");
 			list.add(c);	
 			
 			c = new CorsoLite();
 			c.setId(2);
 			c.setNome("Analisi 2");
 			list.add(c);		
 			
 			return list;
 		}
 		catch (Exception e)
 		{
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 		return null;
 	}
 	
 	
 	/*
 	 *   Ritorna tutti i corsi dal libretto dell'utente in versione lite
 	 */
 	@RequestMapping(method = RequestMethod.GET, value = "/corsi/frequentati")
 	public @ResponseBody
 	
 	List<CorsoLite> getCorsiLibrettoUtente(HttpServletRequest request, HttpServletResponse response, HttpSession session)
 	
 	throws IOException
 	{
 		try
 		{
 			String token = request.getHeader(AcProviderFilter.TOKEN_HEADER);
 			ProfileConnector profileConnector = new ProfileConnector(serverAddress);
 			BasicProfile profile = profileConnector.getBasicProfile(token);
 			
 			User us = retrieveUser(request);
 			
 			ArrayList<CorsoLite> list = new ArrayList<CorsoLite>();
 			
 			CorsoLite c = new CorsoLite();
 			c.setId(1);
 			c.setNome("Analisi matematica 1");
 			list.add(c);	
 			
 			c = new CorsoLite();
 			c.setId(2);
 			c.setNome("Matematica Discreta 1");
 			list.add(c);	
 			
 			c = new CorsoLite();
 			c.setId(3);
 			c.setNome("Programmazione 2");
 			list.add(c);	
 			
 			return list;
 		}
 		catch (Exception e)
 		{
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 		return null;
 	}
 	
 	
 	/*
 	 *   Ritorna il corso dato l'id
 	 */
 	@RequestMapping(method = RequestMethod.GET, value = "/corsi/{id}")
 	public @ResponseBody
 	
 	Corso getCorsoByID(HttpServletRequest request, HttpServletResponse response, HttpSession session, @PathVariable("id") String id)
 	
 	throws IOException
 	{
 		try
 		{
 			String token = request.getHeader(AcProviderFilter.TOKEN_HEADER);
 			ProfileConnector profileConnector = new ProfileConnector(serverAddress);
 			BasicProfile profile = profileConnector.getBasicProfile(token);
 					
 			String id_corso = request.getParameter("id");
 
 			Corso corso = new Corso();
 			corso.setId(10);
 			corso.setNome("Analisi 2");
 			corso.setData_inizio(new Date("10/01/2013"));
 			corso.setData_fine(new Date("01/06/2013"));
 			corso.setValutazione_media(7);
 			corso.setDescrizione("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut " +
 					"labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut " +
 					"aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum " +
 					"dolore eu fugiat nulla pariatur.");	
 			
 			
 			ArrayList<Commento> commenti = new ArrayList<Commento>();
 			
 			Commento co = new Commento();
 			UtenteCorsi utente = new UtenteCorsi();
 			utente.setId(50);
 			utente.setNome("Gino Paoli");
 			
			co.setAutore(utente);
 			co.setData(new Date("20/03/2013"));
 			co.setId(44);
 			co.setTesto("Bella materia, bel corso.. peccato che faccia dare i numeri!");
 			co.setValutazione(7);
 			
 			commenti.add(co);
 			
 			corso.setCommenti(commenti);
 
 			
 			return corso;
 		}
 		catch (Exception e)
 		{
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 		return null;
 	}
 }

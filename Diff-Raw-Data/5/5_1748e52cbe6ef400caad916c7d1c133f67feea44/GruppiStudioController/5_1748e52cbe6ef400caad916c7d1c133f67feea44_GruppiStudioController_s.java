 package eu.trentorise.smartcampus.corsi.controller;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.derby.catalog.GetProcedureColumns;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.core.io.Resource;
 import org.springframework.core.io.support.PropertiesLoaderUtils;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.mysql.jdbc.PreparedStatement;
 
 import eu.trentorise.smartcampus.communicator.CommunicatorConnector;
 import eu.trentorise.smartcampus.communicator.model.Notification;
 import eu.trentorise.smartcampus.communicator.model.NotificationAuthor;
 import eu.trentorise.smartcampus.corsi.model.Commento;
 import eu.trentorise.smartcampus.corsi.model.Corso;
 import eu.trentorise.smartcampus.corsi.model.Evento;
 import eu.trentorise.smartcampus.corsi.model.GruppoDiStudio;
 import eu.trentorise.smartcampus.corsi.model.Studente;
 import eu.trentorise.smartcampus.corsi.repository.CorsoRepository;
 import eu.trentorise.smartcampus.corsi.repository.GruppoDiStudioRepository;
 import eu.trentorise.smartcampus.corsi.repository.StudenteRepository;
 import eu.trentorise.smartcampus.profileservice.BasicProfileService;
 import eu.trentorise.smartcampus.profileservice.model.BasicProfile;
 
 @Controller("gruppiStudioController")
 public class GruppiStudioController {
 
 	private static final Logger logger = Logger
 			.getLogger(GruppiStudioController.class);
 	/*
 	 * the base url of the service. Configure it in webtemplate.properties
 	 */
 	@Autowired
 	@Value("${profile.address}")
 	private String profileaddress;
 
 	/*
 	 * the base appName of the service. Configure it in webtemplate.properties
 	 */
 	@Autowired
 	@Value("${webapp.name}")
 	private String appName;
 
 	@Autowired
 	@Value("${communicator.address}")
 	private String communicatoraddress;
 	
 	@Autowired
 	private GruppoDiStudioRepository gruppidistudioRepository;
 
 	@Autowired
 	private StudenteRepository studenteRepository;
 	
 	@Autowired
 	private CorsoRepository corsoRepository;
 
 	/*
 	 * Ritorna tutti i corsi in versione lite
 	 */
 	@RequestMapping(method = RequestMethod.GET, value = "/gruppidistudio/all")
 	public @ResponseBody
 	List<GruppoDiStudio> getgruppidistudioAll(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session)
 
 	throws IOException {
 		try {
 
 			List<GruppoDiStudio> getgruppidistudio = gruppidistudioRepository.findAll();
 
 			return getgruppidistudio;
 
 		} catch (Exception e) {
 			logger.error(e.getMessage());
 			e.printStackTrace();
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 		return null;
 	}
 
 
 	private String getToken(HttpServletRequest request) {
 		return (String) SecurityContextHolder.getContext().getAuthentication()
 				.getPrincipal();
 	}
 	
 	
 	/*
 	 * Ritorna i gruppi di un corso
 	 */
 	@RequestMapping(method = RequestMethod.GET, value = "/gruppidistudio/{id_corso}")
 	public @ResponseBody
 	List<GruppoDiStudio> getgruppidistudioByIDCourse(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session,
 			@PathVariable("id_corso") Long id_corso)
 
 	throws IOException {
 		try {
 			logger.info("/gruppidistudio/{id_corso}");
 
 			if (id_corso == null)
 				return null;
 
 			return  gruppidistudioRepository.findGdsBycourseId(id_corso);
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 		return null;
 	}
 	
 	
 	/*
 	 * Ritorna i gruppi di uno studente
 	 */
 	@RequestMapping(method = RequestMethod.GET, value = "/gruppidistudio/me")
 	public @ResponseBody
 	List<GruppoDiStudio> getgruppidistudioByMe(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session)
 
 	throws IOException {
 		try {
 			logger.info("/gruppidistudio/me");
 			
 			String token = getToken(request);
 			BasicProfileService service = new BasicProfileService(
 					profileaddress);
 			BasicProfile profile = service.getBasicProfile(token);
 			Long userId = Long.valueOf(profile.getUserId());
 			
 			Studente studente = studenteRepository.findOne(userId);
 			if (studente == null) {
 				studente = new Studente();
 				studente.setId(userId);
 				studente.setNome(profile.getName());
 				studente.setCognome(profile.getSurname());
 				studente = studenteRepository.save(studente);
 
 				// studente = studenteRepository.save(studente);
 
 				// TODO caricare corsi da esse3
 				// Creare associazione su frequenze
 
 				// TEST
 				List<Corso> corsiEsse3 = corsoRepository.findAll();
 
 				String supera = null;
 				String interesse = null;
 				int z = 0;
 				supera = new String();
 				interesse = new String();
 
 				for (Corso cors : corsiEsse3) {
 
 					if (z % 2 == 0) {
 						supera = supera.concat(String.valueOf(cors.getId())
 								.concat(","));
 					}
 					
 					if (z % 4 == 0) {
 						interesse = interesse.concat(String.valueOf(cors.getId())
 								.concat(","));
 					}
 					
 					z++;
 				}
 				
 				// Set corso follwed by studente
 				studente.setCorsi(corsiEsse3);
 				studente = studenteRepository.save(studente);
 
 				// Set corsi superati
 				studente.setIdsCorsiSuperati(supera);
 				studente.setIdsCorsiInteresse(interesse);
 				
 				studente = studenteRepository.save(studente);
 			}
 			
 			
 			if (userId == null)
 				return null;
 
 			List<GruppoDiStudio> listaGruppi = gruppidistudioRepository.findAll();
 			
 			List<GruppoDiStudio> listaGruppiStudente = new ArrayList<GruppoDiStudio>();
 			
 			for(GruppoDiStudio gruppoDiStudio : listaGruppi){
 				if(gruppoDiStudio.isContainsStudente(userId)){
 					listaGruppiStudente.add(gruppoDiStudio);
 				}
 			}
 			
 			return listaGruppiStudente;
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 		return null;
 	}
 	
 	
 	/*
 	 * Ritorna i gruppi di uno studente per un determinato corso
 	 */
 	@RequestMapping(method = RequestMethod.GET, value = "/gruppidistudio/{id_corso}/me")
 	public @ResponseBody
 	List<GruppoDiStudio> getgruppidistudioByIDCourseByMe(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session,
 			@PathVariable("id_corso") Long id_corso)
 
 	throws IOException {
 		try {
 			logger.info("/gruppidistudio/{id_corso}/me");
 			
 			String token = getToken(request);
 			BasicProfileService service = new BasicProfileService(
 					profileaddress);
 			BasicProfile profile = service.getBasicProfile(token);
 			Long userId = Long.valueOf(profile.getUserId());
 			
 			Studente studente = studenteRepository.findOne(userId);
 			if (studente == null) {
 				studente = new Studente();
 				studente.setId(userId);
 				studente.setNome(profile.getName());
 				studente.setCognome(profile.getSurname());
 				studente = studenteRepository.save(studente);
 
 				// studente = studenteRepository.save(studente);
 
 				// TODO caricare corsi da esse3
 				// Creare associazione su frequenze
 
 				// TEST
 				List<Corso> corsiEsse3 = corsoRepository.findAll();
 
 				String supera = null;
 				String interesse = null;
 				int z = 0;
 				supera = new String();
 				interesse = new String();
 
 				for (Corso cors : corsiEsse3) {
 
 					if (z % 2 == 0) {
 						supera = supera.concat(String.valueOf(cors.getId())
 								.concat(","));
 					}
 					
 					if (z % 4 == 0) {
 						interesse = interesse.concat(String.valueOf(cors.getId())
 								.concat(","));
 					}
 					
 					z++;
 				}
 			}
 			
 			if (userId == null)
 				return null;
 
 			if (id_corso == null)
 				return null;
 
 			List<GruppoDiStudio> listaGruppiCorso = gruppidistudioRepository.findGdsBycourseId(id_corso);
 			
 			List<GruppoDiStudio> listaGruppiCorsoStudente = new ArrayList<GruppoDiStudio>();
 			
 			for(GruppoDiStudio gruppoDiStudio : listaGruppiCorso){
 				if(gruppoDiStudio.isContainsStudente(userId)){
 					listaGruppiCorsoStudente.add(gruppoDiStudio);
 				}
 			}
 			
 			return listaGruppiCorsoStudente;
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 		return null;
 	}
 
 	
 	/**
 	 * 
 	 * @param request
 	 * @param response
 	 * @param session
 	 * @param gruppodistudio
 	 * @return true se l'operazione va a buon fine, false altrimenti
 	 * @throws IOException
 	 * 
 	 * Aggiunge un nuovo gruppo di studio nel db. L'unico studente appartenente al gruppo è chi ha fatto la POST.
 	 * 
 	 */
 	@RequestMapping(method = RequestMethod.POST, value = "/gruppodistudio/add")
 	public @ResponseBody
 	boolean AddGds(HttpServletRequest request, HttpServletResponse response,
 			HttpSession session, @RequestBody GruppoDiStudio gruppodistudio)
 
 					throws IOException {
 		try {
 			logger.info("/gruppodistudio/add");
 			// TODO control valid field
 			if (gruppodistudio == null)
 				return false;
 
 			
 			String token = getToken(request);
 			BasicProfileService service = new BasicProfileService(
 					profileaddress);
 			BasicProfile profile = service.getBasicProfile(token);
 			Long userId = Long.valueOf(profile.getUserId());
 				
 			
 			//mediationParserImpl.updateKeyWord(token);
 			
 			// controllo se lo studente � presente nel db
 			Studente studente = studenteRepository.findStudenteByUserId(userId);
 			
 			if (studente == null) {
 				studente = new Studente();
 				studente.setId(userId);
 				studente.setNome(profile.getName());
 				studente.setCognome(profile.getSurname());
 				studente = studenteRepository.save(studente);
 
 				// studente = studenteRepository.save(studente);
 
 				// TODO caricare corsi da esse3
 				// Creare associazione su frequenze
 
 				// TEST
 				List<Corso> corsiEsse3 = corsoRepository.findAll();
 
 				String supera = null;
 				String interesse = null;
 				int z = 0;
 				supera = new String();
 				interesse = new String();
 
 				for (Corso cors : corsiEsse3) {
 
 					if (z % 2 == 0) {
 						supera = supera.concat(String.valueOf(cors.getId())
 								.concat(","));
 					}
 					
 					if (z % 4 == 0) {
 						interesse = interesse.concat(String.valueOf(cors.getId())
 								.concat(","));
 					}
 					
 					z++;
 				}
 				
 				// Set corso follwed by studente
 				studente.setCorsi(corsiEsse3);
 				studente = studenteRepository.save(studente);
 
 				// Set corsi superati
 				studente.setIdsCorsiSuperati(supera);
 				studente.setIdsCorsiInteresse(interesse);
 				
 				studente = studenteRepository.save(studente);
 			}
 			
 			
 //			CommunicatorConnector communicatorConnector = new CommunicatorConnector(
 //					communicatoraddress, appName);
 //
 //			List<String> users = new ArrayList<String>();
 //			List<String> idsInvited = gruppodistudio.getListInvited(gruppodistudio, userId);
 //			
 //			for(String id : idsInvited){
 //				users.add(id);
 //			}
 //
 //			Notification n = new Notification();
 //			n.setTitle(gruppodistudio.getNome());
 //			NotificationAuthor nAuthor = new NotificationAuthor();
 //			nAuthor.setAppId(appName);
 //			nAuthor.setUserId(userId.toString());
 //			n.setAuthor(nAuthor);
 //			n.setUser(userId.toString());
 //			n.setTimestamp(System.currentTimeMillis());
 //			n.setDescription("Invito da "+profile+" al gruppo "+gruppodistudio.getNome());
 //			Map<String, Object> mapGruppo = new HashMap<String, Object>();
 			gruppodistudio.initStudenteGruppo(userId); //inizializzo i membri del gruppo
 			gruppodistudio.setVisible(false); // setto a visible = false finchè non ci saranno almeno 2 componenti
 //			mapGruppo.put("GruppoDiStudio", gruppodistudio); //passo come contenuto della notifica l'hashmap con l'attivita
 //			n.setContent(mapGruppo);
 //			
 //			communicatorConnector.sendAppNotification(n, appName, users,
 //					getToken(request));
 			
 			
 			gruppodistudio.setId(-1); // setto l'id a -1 per evitare che il commento venga sovrascritto
 			
 			
 			GruppoDiStudio gruppodistudioAggiornato = gruppidistudioRepository.save(gruppodistudio);
 			
 			
 			// Controllo se il commento è gia presente
 			if (gruppodistudioAggiornato == null) {
 				return false;
 			} else {
 				return true;
 			}
 
 			
 		} catch (Exception e) {
 
 			e.printStackTrace();
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			return false;
 		}
 	
 	}
 	
 	
 	
 	/**
 	 * 
 	 * @param request
 	 * @param response
 	 * @param session
 	 * @param gruppodistudio
 	 * @return true se l'operazione va a buon fine, false altrimenti
 	 * @throws IOException
 	 * 
 	 * Aggiunge un nuovo membro al gruppo di studio passato come input.
 	 * 
 	 */
 	@RequestMapping(method = RequestMethod.POST, value = "/gruppodistudio/accept")
 	public @ResponseBody
 	boolean AcceptGds(HttpServletRequest request, HttpServletResponse response,
 			HttpSession session, @RequestBody GruppoDiStudio gruppodistudio)
 
 					throws IOException {
 		try {
 			logger.info("/gruppodistudio/accept");
 			// TODO control valid field
 			if (gruppodistudio == null)
 				return false;
 
 			
 			String token = getToken(request);
 			BasicProfileService service = new BasicProfileService(
 					profileaddress);
 			BasicProfile profile = service.getBasicProfile(token);
 			Long userId = Long.valueOf(profile.getUserId());
 				
 			
 			//mediationParserImpl.updateKeyWord(token);
 			
 			// controllo se lo studente � presente nel db
 			Studente studente = studenteRepository.findStudenteByUserId(userId);
 			
 			if (studente == null) {
 				studente = new Studente();
 				studente.setId(userId);
 				studente.setNome(profile.getName());
 				studente.setCognome(profile.getSurname());
 				studente = studenteRepository.save(studente);
 
 				// studente = studenteRepository.save(studente);
 
 				// TODO caricare corsi da esse3
 				// Creare associazione su frequenze
 
 				// TEST
 				List<Corso> corsiEsse3 = corsoRepository.findAll();
 
 				String supera = null;
 				String interesse = null;
 				int z = 0;
 				supera = new String();
 				interesse = new String();
 
 				for (Corso cors : corsiEsse3) {
 
 					if (z % 2 == 0) {
 						supera = supera.concat(String.valueOf(cors.getId())
 								.concat(","));
 					}
 					
 					if (z % 4 == 0) {
 						interesse = interesse.concat(String.valueOf(cors.getId())
 								.concat(","));
 					}
 					
 					z++;
 				}
 				
 				// Set corso follwed by studente
 				studente.setCorsi(corsiEsse3);
 				studente = studenteRepository.save(studente);
 
 				// Set corsi superati
 				studente.setIdsCorsiSuperati(supera);
 				studente.setIdsCorsiInteresse(interesse);
 				
 				studente = studenteRepository.save(studente);
 			}
 			
 			GruppoDiStudio gdsFromDB = gruppidistudioRepository.findOne(gruppodistudio.getId());
 				
 			// se gds non è nel db -> false
 			if(gdsFromDB == null)
 				return false;
 			
 			gdsFromDB.addStudenteGruppo(userId); // aggiungo il membro al gruppo
 			gdsFromDB.setIfVisibleFromNumMembers();
 			
 			
 			GruppoDiStudio gruppodistudioAggiornato = gruppidistudioRepository.save(gdsFromDB);
 			
 			if (gruppodistudioAggiornato == null) {
 				return false;
 			} else {
 				// controllo che il gds aggiornato abbia lo stesso id di prima
 				if(gdsFromDB.getId() == gruppodistudioAggiornato.getId())
 					return true;
 				else
 					return false;
 			}
 
 			
 		} catch (Exception e) {
 
 			e.printStackTrace();
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			return false;
 		}
 	
 	}
 	
 	
 	
 	/*
 	 * Cancella lo studente dal gruppo
 	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/gruppidistudio/delete/me")
 	public @ResponseBody
 	boolean deleteMeByGds(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session, @RequestBody GruppoDiStudio gruppodistudio)
 
 	throws IOException {
 		try {
			logger.info("/gruppidistudio/me");
 			
 			String token = getToken(request);
 			BasicProfileService service = new BasicProfileService(
 					profileaddress);
 			BasicProfile profile = service.getBasicProfile(token);
 			Long userId = Long.valueOf(profile.getUserId());
 			
 			Studente studente = studenteRepository.findOne(userId);
 			if (studente == null) {
 				studente = new Studente();
 				studente.setId(userId);
 				studente.setNome(profile.getName());
 				studente.setCognome(profile.getSurname());
 				studente = studenteRepository.save(studente);
 
 				// studente = studenteRepository.save(studente);
 
 				// TODO caricare corsi da esse3
 				// Creare associazione su frequenze
 
 				// TEST
 				List<Corso> corsiEsse3 = corsoRepository.findAll();
 
 				String supera = null;
 				String interesse = null;
 				int z = 0;
 				supera = new String();
 				interesse = new String();
 
 				for (Corso cors : corsiEsse3) {
 
 					if (z % 2 == 0) {
 						supera = supera.concat(String.valueOf(cors.getId())
 								.concat(","));
 					}
 					
 					if (z % 4 == 0) {
 						interesse = interesse.concat(String.valueOf(cors.getId())
 								.concat(","));
 					}
 					
 					z++;
 				}
 				
 				// Set corso follwed by studente
 				studente.setCorsi(corsiEsse3);
 				studente = studenteRepository.save(studente);
 
 				// Set corsi superati
 				studente.setIdsCorsiSuperati(supera);
 				studente.setIdsCorsiInteresse(interesse);
 				
 				studente = studenteRepository.save(studente);
 			}
 			
 			
 			if (userId == null)
 				return false;
 			
 			GruppoDiStudio gdsFromDB = gruppidistudioRepository.findOne(gruppodistudio.getId());
 
 			gdsFromDB.removeStudenteGruppo(userId);
 			gdsFromDB.setIfVisibleFromNumMembers();
 			// se il gruppo ha 0 membri lo elimino dal db
 			if(gdsFromDB.canRemoveGruppoDiStudioIfVoid())
 				gruppidistudioRepository.delete(gdsFromDB);
 			else
 				gruppidistudioRepository.save(gdsFromDB);
 			
 			return true;
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 		return false;
 	}
 
 }
 	

 package eu.trentorise.smartcampus.vas.ifame.controllers;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.annotation.PostConstruct;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import jxl.Workbook;
 import jxl.read.biff.BiffException;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import eu.trentorise.smartcampus.profileservice.BasicProfileService;
 import eu.trentorise.smartcampus.profileservice.model.BasicProfile;
 import eu.trentorise.smartcampus.vas.ifame.model.MenuDelGiorno;
 import eu.trentorise.smartcampus.vas.ifame.model.MenuDelMese;
 import eu.trentorise.smartcampus.vas.ifame.model.MenuDellaSettimana;
 import eu.trentorise.smartcampus.vas.ifame.model.Piatto;
 import eu.trentorise.smartcampus.vas.ifame.repository.MensaRepository;
 import eu.trentorise.smartcampus.vas.ifame.repository.PiattoRepository;
 import eu.trentorise.smartcampus.vas.ifame.utils.NewMenuXlsUtil;
 
 @Controller("MenuController")
 public class MenuController {
 
 	private static final Logger logger = Logger.getLogger(MenuController.class);
 
 	private static Workbook workbook = null;
 
 	@Autowired
 	PiattoRepository piattoRepository;
 
 	@Autowired
 	MensaRepository mensaRepository;
 
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
 
 	/*
 	 * 
 	 * 
 	 * 
 	 * 
 	 * MENU DEL GIORNO CONTROLLER
 	 */
 
 	@Autowired
 	@Value("${profile.address}")
 	private String profileaddress;
 
 	private String getToken(HttpServletRequest request) {
 		return (String) SecurityContextHolder.getContext().getAuthentication()
 				.getPrincipal();
 	}
 
 	@RequestMapping(method = RequestMethod.GET, value = "/getmenudelgiorno")
 	public @ResponseBody
 	MenuDelGiorno getMenuDelGiornoStringList(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session)
 			throws IOException {
 		try {
 			logger.info("/getmenudelgiorno");
 
 			String token = getToken(request);
 			BasicProfileService service = new BasicProfileService(
 					profileaddress);
 			BasicProfile profile = service.getBasicProfile(token);
 			// Long userId = Long.valueOf(profile.getUserId());
 			if (profile != null) {
 
 				Calendar data = Calendar.getInstance();
 				int day = data.get(Calendar.DAY_OF_MONTH);
 
 				return NewMenuXlsUtil.getMenuDelGiorno(day, workbook);
 			}
 		} catch (Exception e) {
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 		return null;
 	}
 
 	/*
 	 * 
 	 * 
 	 * 
 	 * 
 	 * MENU DEL MESE
 	 */
 
 	@RequestMapping(method = RequestMethod.GET, value = "/getmenudelmese")
 	public @ResponseBody
 	MenuDelMese getMenuDelMese(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session)
 			throws IOException {
 		try {
 
 			logger.info("/getmenudelmese");
 			String token = getToken(request);
 			BasicProfileService service = new BasicProfileService(
 					profileaddress);
 			BasicProfile profile = service.getBasicProfile(token);
 			// Long userId = Long.valueOf(profile.getUserId());
 			if (profile != null) {
 
 				MenuDelMese mdm = NewMenuXlsUtil.getMenuDelMese(workbook);
 
 				return mdm;
 
 			}
 		} catch (Exception e) {
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 		return null;
 	}
 
 	/*
 	 * 
 	 * 
 	 * 
 	 * 
 	 * ALTERNATIVE CONTROLLER
 	 * 
 	 * restituisce le alternative al menu del giorno che sono sempre fisse
 	 * leggendo l'excel
 	 */
 
 	@RequestMapping(method = RequestMethod.GET, value = "/getalternative")
 	public @ResponseBody
 	List<Piatto> getAlternative(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session)
 			throws IOException {
 		try {
 
 			logger.info("/getalternative");
 			String token = getToken(request);
 			BasicProfileService service = new BasicProfileService(
 					profileaddress);
 			BasicProfile profile = service.getBasicProfile(token);
 			// Long userId = Long.valueOf(profile.getUserId());
 			if (profile != null) {
 
 				return NewMenuXlsUtil.getAlternative(workbook);
 			}
 		} catch (Exception e) {
 			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 		}
 		return null;
 	}
 
 	/*
 	 * 
 	 * 
 	 * 
 	 * INIZIALIZZO IL WORKBOOK
 	 */
 	@PostConstruct
 	private void initXls() throws BiffException, IOException {
 
 		InputStream stream = getClass().getResourceAsStream("/Dicembre.xls");
 
 		MenuController.workbook = NewMenuXlsUtil.getWorkbook(stream);
 	}
 
 	/*
 	 * 
 	 * 
 	 * 
 	 * 
 	 * INIZIALIZZO TUTTE LE TABELLE
 	 */
 
 	@PostConstruct
 	private void inizializzaDatabase() throws BiffException, IOException {
 
 		logger.info("Called: inizializzaDatabase()");
 		// piattoRepository.deleteAll();
 
 		// ********************************************************************
 		// Only from november to december to uniformate all piatto's name. After
 		// this few lines can be commented beacause the NewMenuXlsUtils class
 		// will take care of formatting the names (the first charachter
 		// uppercase and the rest of the string lowercase). This is to avoid
 		// multiple piatti with the same name with only differnet case letters
 		// --------------------------------------------------------------------
 		// get all the piatti
		List<Piatto> listDbPiatti = piattoRepository.findAll();
		for (Piatto piatto : listDbPiatti) {
			// format the name
			piatto.setPiatto_nome(NewMenuXlsUtil.format(piatto.getPiatto_nome()));
			// update the database
			piattoRepository.saveAndFlush(piatto);
		}
 		// ********************************************************************
 
 		// get the updated piatti and now do what you want
		listDbPiatti = piattoRepository.findAll();
 
 		// get the new set of piatti
 		Workbook excel = NewMenuXlsUtil.getWorkbook(getClass()
 				.getResourceAsStream("/Dicembre.xls"));
 		MenuDelMese menuDelMese = NewMenuXlsUtil.getMenuDelMese(excel);
 
 		Set<Piatto> setPiatti = new HashSet<Piatto>();
 
 		for (MenuDellaSettimana mds : menuDelMese.getMenuDellaSettimana()) {
 			List<MenuDelGiorno> mdglist = mds.getMenuDelGiorno();
 			for (MenuDelGiorno mdg : mdglist) {
 				List<Piatto> piattiDelGiorno = mdg.getPiattiDelGiorno();
 				for (Piatto p : piattiDelGiorno) {
 					setPiatti.add(p);
 				}
 			}
 		}
 
 		int counterNuoviPiatti = 0;
 		// for each new piatto in the new menu check if it's in the db
 		for (Piatto nuovoPiatto : setPiatti) {
 			if (!listDbPiatti.contains(nuovoPiatto)) {
 				// is not in the db so save it
 				piattoRepository.saveAndFlush(nuovoPiatto);
 				logger.info(++counterNuoviPiatti + ". Saved: "
 						+ nuovoPiatto.getPiatto_nome());
 			}
 		}
 
 		// *************************************************************
 		// finche non ci sono aggiornamenti sui link delle webcam o nomi delle
 		// mense lasciare tutto commentato
 		// *************************************************************
 		// /*
 		// *
 		// * Inizializzo le mense
 		// */
 		// final String url_povo_mensa_offline =
 		// "http://www.operauni.tn.it/upload/cms/456_x/mensa-povo2.jpg";
 		// final String url_povo_mensa_veloce_offline =
 		// "http://www.operauni.tn.it/upload/cms/456_x/mensa-povo1.jpg";
 		// final String url_tommaso_gar_offline =
 		// "http://www.operauni.tn.it/upload/cms/456_x/gar-offline.jpg";
 		// final String url_zanella_offline =
 		// "http://www.operauni.tn.it/upload/cms/456_x/mensa-zanella.jpg";
 		// final String url_mesiano_1_offline =
 		// "http://www.operauni.tn.it/upload/cms/456_x/mesiano-offline.jpg";
 		// final String url_mesiano_2_offline =
 		// "http://www.operauni.tn.it/upload/cms/456_x/mesiano-web-2.jpg";
 		//
 		// final String url_povo_mensa_online =
 		// "http://www.operauni.tn.it/upload/Webcam/Povo02.jpg";
 		// final String url_povo_mensa_veloce_online =
 		// "http://www.operauni.tn.it/upload/Webcam/Povo01.jpg";
 		// final String url_tommaso_gar_online =
 		// "http://www.operauni.tn.it/upload/Webcam/MensaUni.jpg";
 		// final String url_zanella_online =
 		// "http://www.operauni.tn.it/upload/Webcam/mensa_zanella.jpg";
 		// final String url_mesiano_1_online =
 		// "http://www.operauni.tn.it/upload/Webcam/MensaMes01.jpg";
 		// final String url_mesiano_2_online =
 		// "http://www.operauni.tn.it/upload/Webcam/MensaMes02.jpg";
 		//
 		// final String name_povo_mensa = "Povo Mensa";
 		// final String name_povo_mensa_veloce = "Povo Mensa Veloce";
 		// final String name_tommaso_gar = "Tommaso Gar";
 		// final String name_zanella = "Zanella";
 		// final String name_mesiano_1 = "Mesiano 1";
 		// final String name_mesiano_2 = "Mesiano 2";
 		//
 		// Mensa povo_mensa = new Mensa(name_povo_mensa, url_povo_mensa_online,
 		// url_povo_mensa_offline);
 		// Mensa povo_mensa_veloce = new Mensa(name_povo_mensa_veloce,
 		// url_povo_mensa_veloce_online, url_povo_mensa_veloce_offline);
 		// Mensa tommaso_gar = new Mensa(name_tommaso_gar,
 		// url_tommaso_gar_online,
 		// url_tommaso_gar_offline);
 		// Mensa zanella = new Mensa(name_zanella, url_zanella_online,
 		// url_zanella_offline);
 		// Mensa mesiano_1 = new Mensa(name_mesiano_1, url_mesiano_1_online,
 		// url_mesiano_1_offline);
 		// Mensa mesiano_2 = new Mensa(name_mesiano_2, url_mesiano_2_online,
 		// url_mesiano_2_offline);
 		//
 		// mensaRepository.save(povo_mensa);
 		// mensaRepository.save(povo_mensa_veloce);
 		// mensaRepository.save(tommaso_gar);
 		// mensaRepository.save(zanella);
 		// mensaRepository.save(mesiano_1);
 		// mensaRepository.save(mesiano_2);
 	}
 }

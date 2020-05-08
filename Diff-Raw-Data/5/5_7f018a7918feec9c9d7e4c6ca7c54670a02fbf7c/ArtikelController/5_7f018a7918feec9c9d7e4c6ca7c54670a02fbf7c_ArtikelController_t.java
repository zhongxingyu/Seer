 package de.shop.artikelverwaltung.controller;
 
 import static javax.ejb.TransactionAttributeType.REQUIRED;
 import static javax.ejb.TransactionAttributeType.SUPPORTS;
 
 import java.io.Serializable;
 import java.lang.invoke.MethodHandles;
 import java.util.Collections;
 import java.util.List;
 import java.util.Locale;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.ejb.Stateful;
 import javax.ejb.TransactionAttribute;
 import javax.enterprise.context.SessionScoped;
 import javax.faces.context.Flash;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.servlet.http.HttpSession;
 
 import org.jboss.logging.Logger;
 
 import de.shop.artikelverwaltung.domain.Artikel;
 import de.shop.artikelverwaltung.domain.Kategorie;
 import de.shop.artikelverwaltung.service.ArtikelService;
 import de.shop.util.Client;
 import de.shop.util.Log;
 import de.shop.util.Transactional;
 
 
 
 /**
  * Dialogsteuerung fuer die ArtikelService
  */
 @Named("ac")
 @SessionScoped
 @Stateful
 @TransactionAttribute(SUPPORTS)
 @Log
 public class ArtikelController implements Serializable {
 	private static final long serialVersionUID = 1564024850446471639L;
 
 	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
 	
 	private static final String JSF_LIST_ARTIKEL = "/artikelverwaltung/listArtikel";
 	private static final String FLASH_ARTIKEL = "artikel";
 	private static final String JSF_LIST_ARTIKEL_BY_KATEGORIE = "/artikelverwaltung/listArtikelByKategorie";
 	//private static final int ANZAHL_LADENHUETER = 5;
 	
 	private static final String JSF_SELECT_ARTIKEL = "/artikelverwaltung/selectArtikel";
 	private static final String SESSION_VERFUEGBARE_ARTIKEL = "verfuegbareArtikel";
 
 	private Long artikel_id;	
 	private List<Artikel> artikel = Collections.emptyList();
 	private String name;
 	private Long id;
 	private List<Artikel> ladenhueter;
 	private Artikel neuerArtikel;
 	private Kategorie neueKategorie;
 	
 
 	private String selectedKatId;
 
 	@Inject
 	private ArtikelService as;
 	
 	@Inject
 	@Client
 	private Locale locale;
 	
 	@Inject
 	private Flash flash;
 	
 	@Inject
 	private transient HttpSession session;
 
 	
 	@PostConstruct
 	private void postConstruct() {
 		LOGGER.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
 	}
 
 	@PreDestroy
 	private void preDestroy() {
 		LOGGER.debugf("CDI-faehiges Bean %s wird geloescht", this);
 	}
 	
 	@Override
 	public String toString() {
 		return "ArtikelController [name=" + name + "]";
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 
 	public List<Artikel> getLadenhueter() {
 		return ladenhueter;
 	}
 	//TODO locale statt null
 	@Transactional
 	public String findArtikelByName() {
 		artikel = as.findArtikelByNamen(name, locale);
 		flash.put(FLASH_ARTIKEL, artikel);
 
 		return JSF_LIST_ARTIKEL;
 	}
 	
 	@TransactionAttribute(REQUIRED)
 	public void createArtikel() {
 		
 			neuerArtikel = (Artikel) as.createArtikel(neuerArtikel, locale);
 		
 	}
 	@Transactional
	public void findArtikelByKategorie() {
 		artikel = as.findArtikelByKategorie(selectedKatId, locale);
 		//flash.put(FLASH_ARTIKEL, artikel);
 
		//return JSF_LIST_ARTIKEL_BY_KATEGORIE;
 	}
 	public void createEmptyArtikel() {
 		if (neuerArtikel != null) {
 			return;
 		}
 		neuerArtikel = new Artikel();
 		neueKategorie = new Kategorie();
 		neuerArtikel.setKategorie(neueKategorie);
 		
 	}
 //TODO 
 //	@Transactional
 //	public void loadLadenhueter() {
 //		ladenhueter = as.ladenhueter(ANZAHL_LADENHUETER);
 //	}
 	
 	@Transactional
 	public String selectArtikel() {
 		if (session.getAttribute(SESSION_VERFUEGBARE_ARTIKEL) != null) {
 			return JSF_SELECT_ARTIKEL;
 		}
 		
 		final List<Artikel> alleArtikel = as.findAllArtikel();
 		session.setAttribute(SESSION_VERFUEGBARE_ARTIKEL, alleArtikel);
 		return JSF_SELECT_ARTIKEL;
 	}
 
 	public Long getArtikel_id() {
 		return artikel_id;
 	}
 
 	public void setArtikel_id(Long artikel_id) {
 		this.artikel_id = artikel_id;
 	}
 
 	public List<Artikel> getArtikel() {
 		return artikel;
 	}
 
 	public void setArtikel(List<Artikel> artikel) {
 		this.artikel = artikel;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public Artikel getNeuerArtikel() {
 		return neuerArtikel;
 	}
 
 	public void setNeuerArtikel(Artikel neuerArtikel) {
 		this.neuerArtikel = neuerArtikel;
 	}
 //	public void getKategorieParam(){
 //		
 //		FacesContext fc = FacesContext.getCurrentInstance();
 //		Map<String,String> params = fc.getExternalContext().getRequestParameterMap();
 //		selectedKatId = Long.valueOf(params.get("id"));
 //	}
 
 	public String getSelectedKatId() {
 		return selectedKatId;
 	}
 
 	public void setSelectedKatId(String selectedKatId) {
 		this.selectedKatId = selectedKatId;
 	}
 }

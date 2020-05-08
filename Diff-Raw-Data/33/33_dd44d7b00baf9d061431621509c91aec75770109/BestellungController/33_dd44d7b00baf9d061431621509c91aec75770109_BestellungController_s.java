 package de.shop.bestellverwaltung.controller;
 
 import static de.shop.util.Constants.JSF_DEFAULT_ERROR;
 import static javax.ejb.TransactionAttributeType.REQUIRED;
 
 import java.io.Serializable;
 import java.lang.invoke.MethodHandles;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Locale;
 
 import javax.ejb.TransactionAttribute;
import javax.enterprise.context.RequestScoped;
 import javax.enterprise.context.SessionScoped;
 import javax.faces.context.Flash;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.jboss.logging.Logger;
 
import de.shop.artikelverwaltung.domain.Artikel;
 import de.shop.auth.controller.AuthController;
 import de.shop.auth.controller.KundeLoggedIn;
 import de.shop.bestellverwaltung.domain.Bestellposition;
 import de.shop.bestellverwaltung.domain.BestellstatusType;
 import de.shop.bestellverwaltung.domain.Bestellung;
 import de.shop.bestellverwaltung.service.BestellungService;
 import de.shop.kundenverwaltung.domain.Kunde;
 import de.shop.kundenverwaltung.service.KundeService;
import de.shop.kundenverwaltung.service.KundeService.FetchType;
 import de.shop.util.Client;
 import de.shop.util.Log;
 import de.shop.util.Transactional;
 import de.shop.bestellverwaltung.service.AbstractBestellungValidationException;
 import de.shop.bestellverwaltung.service.BestellungService.BestellungFetchType;
 
 
 @Named("bc")
 @SessionScoped
 //@RequestScoped
 @Log
 public class BestellungController implements Serializable {
 	private static final long serialVersionUID = 8788102910739438907L;
 
 	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
 
 	private static final String JSF_VIEW_BESTELLUNG = "/bestellverwaltung/viewBestellung";
 	
 	private Long bestellungId;
 	private Bestellung bestellung;
 	
 	private String selectedBestellId;
 
 	private List<Bestellung> bestellungen = Collections.emptyList();
 
 	
 	@Inject
 	private Warenkorb warenkorb;
 	
 	@Inject
 	private BestellungService bs;
 	
 	@Inject
 	private KundeService ks;
 	
 	@Inject
 	private AuthController auth;
 	
 	@Inject
 	@KundeLoggedIn
 	private Kunde kunde;
 	
 	@Inject
 	@Client
 	private Locale locale;
 	
 	@Inject
 	private Flash flash;
 	
 	
 	public Long getBestellungId() {
 		return bestellungId;
 	}
 
 
 	public void setBestellungId(Long bestellungId) {
 		this.bestellungId = bestellungId;
 	}
 	
 	public List<Bestellung> getBestellungen() {
 		return bestellungen;
 	}
 	
 	
 	/**
 	 * Action Methode, um eine Bestellung anhand der ID zu suchen
 	 * @return URL fuer Anzeige der gefundenen Bestellung
 	 * @throws Exception 
 	 */
 	@TransactionAttribute(REQUIRED)
 	public String findBestellungById(Long id) throws Exception {
 		bestellung = bs.findBestellungById(id, BestellungFetchType.MIT_BESTELLPOSITIONEN, locale);
 		
 		if (bestellung == null) {
 			// Keine Bestellung zu gegebener ID gefunden
 			//TODO implement method findBestellungByIdErrorMsg(String);
 			//return findBestellungByIdErrorMsg(bestellungId.toString());
			System.out.println("es wurde keine Bestellung mit BestellId "+id+" gefunden");
 		}
 
		System.out.println("Bestellung mit BestellId "+id+" gefunden");
 		flash.put("bestellung", bestellung);
 		return JSF_VIEW_BESTELLUNG;
 	}
 	
 	@TransactionAttribute(REQUIRED)
 	public void findBestellungBySelectedId() {
		bestellung = bs.findBestellungById(Long.valueOf(selectedBestellId), BestellungFetchType.MIT_BESTELLPOSITIONEN, locale);
 		
 		if (bestellung == null) {
 			// Keine Bestellung zu gegebener ID gefunden
 			//TODO implement method findBestellungByIdErrorMsg(String);
 			//return findBestellungByIdErrorMsg(bestellungId.toString());
			System.out.println("es wurde keine Bestellung mit BestellId "+selectedBestellId+" gefunden");
 		}
 		
		System.out.println("Bestellung mit BestellId "+selectedBestellId+" gefunden");
 	}
 
 
 	@Transactional
 	public String bestellen() {
 		
 		if (!auth.isLoggedIn()) {
 			// Darf nicht passieren, wenn der Button zum Bestellen verfuegbar ist
 			return JSF_DEFAULT_ERROR;
 		}
 		
 		auth.preserveLogin();
 		
 		if (warenkorb == null || warenkorb.getPositionen() == null || warenkorb.getPositionen().isEmpty()) {
 			// Darf nicht passieren, wenn der Button zum Bestellen verfuegbar ist
 			return JSF_DEFAULT_ERROR;
 		}
 		
 		
 		
 		// Den eingeloggten Kunden mit seinen Bestellungen ermitteln, und dann die neue Bestellung zu ergaenzen
 		//TODO FetchType MIT_BESTELLUNGEN in KS implementieren
 		//kunde = ks.findKundeById(kunde.getKundeId(), FetchType.MIT_BESTELLUNGEN, locale);
 		// Aus dem Warenkorb nur Positionen mit Anzahl > 0
 		final List<Bestellposition> positionen = warenkorb.getPositionen();
 		final List<Bestellposition> neuePositionen = new ArrayList<>(positionen.size());
 		for (Bestellposition bp : positionen) {
 			if (bp.getBestellmenge() > 0) {
 				neuePositionen.add(bp);
 			}
 		}
 		
 		// Warenkorb zuruecksetzen
 		warenkorb.endConversation();
 		
 		// Neue Bestellung mit neuen Bestellpositionen erstellen
 		Bestellung bestellung = new Bestellung();
 		bestellung.setStatus(BestellstatusType.OFFEN);
 		bestellung.setBestellpositionen(neuePositionen);
 		LOGGER.tracef("Neue Bestellung: %s\nBestellpositionen: %s", bestellung, bestellung.getBestellpositionen());
 		
 		// Bestellung mit VORHANDENEM Kunden verknuepfen:
 		// dessen Bestellungen muessen geladen sein, weil es eine bidirektionale Beziehung ist
 		try {
 			bestellung = bs.createBestellung(bestellung, kunde, locale);
 		}
 		//ToDo Implement Class
 		catch (AbstractBestellungValidationException e) {
 			// Validierungsfehler KOENNEN NICHT AUFTRETEN, da Attribute durch JSF validiert wurden
 			// und in der Klasse Bestellung keine Validierungs-Methoden vorhanden sind
 			throw new IllegalStateException(e);
 		}
 		
 		// Bestellung im Flash speichern wegen anschliessendem Redirect
 		flash.put("bestellung", bestellung);
 		
 		return JSF_VIEW_BESTELLUNG;
 		
 	}
 
 	// Bestellungen des eingeloggten Kunden ermitteln
 	@Transactional
 	public void findBestellungenByKunde() {
 		if (auth.isLoggedIn()) {
 			bestellungen = bs.findBestellungenByKunde(kunde.getKundeId());
 		}	
 	}
 	
 	@Transactional
 	public List<Bestellung> getAllBestellungen() {
 		return bs.findAllBestellungen(BestellungFetchType.NUR_BESTELLUNG, locale);
 	}
 
 	public int getAnzahlBestellungenByKunde() {
 		return bestellungen.size();
 	}
 
 
 	public String getSelectedBestellId() {
 		return selectedBestellId;
 	}
 
 
 	public void setSelectedBestellId(String selectedBestellId) {
 		this.selectedBestellId = selectedBestellId;
 	}






}	

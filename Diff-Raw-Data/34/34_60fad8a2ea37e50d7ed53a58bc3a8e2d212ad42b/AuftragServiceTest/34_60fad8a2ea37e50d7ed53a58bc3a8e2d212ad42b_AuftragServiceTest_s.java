 package de.shop.Auftragsverwaltung.service;
 
 import static org.hamcrest.CoreMatchers.anyOf;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.junit.Assert.assertThat;
 
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.transaction.HeuristicMixedException;
 import javax.transaction.HeuristicRollbackException;
 import javax.transaction.NotSupportedException;
 import javax.transaction.RollbackException;
 import javax.transaction.SystemException;
 import javax.transaction.UserTransaction;
 
 import org.jboss.arquillian.junit.Arquillian;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import de.shop.Artikelverwaltung.domain.Artikel;
 import de.shop.Artikelverwaltung.service.ArtikelService;
import de.shop.Auftragsverwaltung.domain.Auftragsposition;
 import de.shop.Auftragsverwaltung.domain.Auftrag;
 import de.shop.Kundenverwaltung.domain.Kunde;
 import de.shop.Kundenverwaltung.service.KundeService;
 import de.shop.Kundenverwaltung.service.KundeService.FetchType;
 import de.shop.Util.AbstractTest;
 
 @RunWith(Arquillian.class)
 public class AuftragServiceTest extends AbstractTest {
 	private static final Long KUNDE_ID_VORHANDEN = Long.valueOf(13);
 	private static final Long ARTIKEL_1_ID = Long.valueOf(304);
 	private static final short ARTIKEL_1_ANZAHL = 1;
 	private static final Long ARTIKEL_2_ID = Long.valueOf(309);
 	private static final short ARTIKEL_2_ANZAHL = 2;
 	private static final Long AUFTRAG_ID_VORHANDEN = Long.valueOf(151);
 	private static final Long KUNDE_ID_AUFTRAGVORHANDEN = Long.valueOf(14);
 	private static final short KUNDE_ID_AUFTRAG_ANZ_AUFTRAGVORHANDEN = 2;
 	
 	@Inject
 	private AuftragService auftragService;
 	
 	@Inject
 	private KundeService kundeService;
 	
 	@Inject
 	private ArtikelService artikelService;
 	
 	@Test
 	public void createAuftrag() throws NotSupportedException, 
 										SystemException, 
 										SecurityException, 
 										IllegalStateException, 
 										RollbackException, 
 										HeuristicMixedException, 
 										HeuristicRollbackException {
 		// Given
 		final Long kundeId = KUNDE_ID_VORHANDEN;
 		final Long artikel1Id = ARTIKEL_1_ID;
 		final short artikel1Anzahl = ARTIKEL_1_ANZAHL;
 		final Long artikel2Id = ARTIKEL_2_ID;
 		final short artikel2Anzahl = ARTIKEL_2_ANZAHL; 
 		
 		// When
 		Artikel artikel1 = artikelService
 							.findArtikelByID(artikel1Id, 
 											de.shop.Artikelverwaltung.service.ArtikelService.FetchType.NUR_Artikel,
 											LOCALE);
 		final UserTransaction trans = getUserTransaction();
 		trans.commit();
 		
 		Auftrag neuerAuftrag = new Auftrag();
 		Auftragsposition position1 = new Auftragsposition(artikel1, artikel1Anzahl);
 		neuerAuftrag.addAuftragsposition(position1);
 		
 		trans.begin();
 		Artikel artikel2 = artikelService
 							.findArtikelByID(artikel2Id, 
 											de.shop.Artikelverwaltung.service.ArtikelService.FetchType.NUR_Artikel, 
 											LOCALE);
 		trans.commit();
 		
 		Auftragsposition position2 = new Auftragsposition(artikel2, artikel2Anzahl);
 		neuerAuftrag.addAuftragsposition(position2);
 		
 		trans.begin();
 		Kunde kunde = kundeService.findKundenByKundennummer(kundeId, 
 															FetchType.MIT_BESTELLUNGEN, 
 															LOCALE);
 		trans.commit();
 		
 		trans.begin();
 		neuerAuftrag = auftragService.createAuftrag(neuerAuftrag, kunde, LOCALE);
 		trans.commit();
 		
 		// Then
 		assertThat(neuerAuftrag.getAuftragspositionen().size(), is(2));
 		for (Auftragsposition aPos : neuerAuftrag.getAuftragspositionen()) {
 			assertThat(aPos.getArtikel().getId(), anyOf(is(ARTIKEL_1_ID), is(ARTIKEL_2_ID)));
 		}															
 	}
 	
 	@Test
 	public void findAuftragById() {
 		// Given
 		final Long auftragId = AUFTRAG_ID_VORHANDEN;
 		
 		// Then
 		Auftrag testAuftrag = auftragService.findAuftragById(auftragId);
 		
 		// When
		assertThat(testAuftrag.getKunde().getKundenNr() == AUFTRAG_ID_VORHANDEN, is(true));
		assertThat(testAuftrag.getAuftragspositionen().size(), is(1));
 	}
 	
 	@Test
 	public void findAuftragByKunde() {
 		// Given
 		Long kundeId = KUNDE_ID_AUFTRAGVORHANDEN;
 		short anzahlAuftraege = KUNDE_ID_AUFTRAG_ANZ_AUFTRAGVORHANDEN;
 		
 		// Then
 		List<Auftrag> testAuftrag = auftragService.findAuftragByKundeId(kundeId);
 		
 		// When
 		assertThat(testAuftrag, is(notNullValue()));
 		assertThat(testAuftrag.size() == anzahlAuftraege, is(true));
 	}	
 }

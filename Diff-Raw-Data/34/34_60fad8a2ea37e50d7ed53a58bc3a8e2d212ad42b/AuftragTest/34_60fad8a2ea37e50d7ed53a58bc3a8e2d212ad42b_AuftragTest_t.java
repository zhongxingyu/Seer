 package de.shop.Auftragsverwaltung.domain;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.NoResultException;
 import javax.validation.ConstraintViolation;
 import javax.validation.ConstraintViolationException;
 
 import org.jboss.arquillian.junit.Arquillian;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
import de.shop.Artikelverwaltung.domain.Artikel;
import de.shop.Kundenverwaltung.domain.Kunde;
 import de.shop.Util.AbstractDomainTest;
 
 
 @RunWith(Arquillian.class)
 public class AuftragTest extends AbstractDomainTest {
 	private static final Long ID_VORHANDEN = Long.valueOf(150);
 	private static final Long ID_KUNDE_VORHANDEN = Long.valueOf(13);
 	private static final Long ID_NICHTVORHANDEN = Long.valueOf(50);
 	
 	private static final Long KUNDEID_NEU = Long.valueOf(13);
 	private static final int POSITION1ANZAHL_NEU = 1;
 	private static final Long POSITION1ARTIKELID_NEU = Long.valueOf(303);
 	
 	
 	@Test
 	public void validate() {
 		assertThat(true, is(true));
 	}
 	
 	@Test
 	public void findAuftragByIdVorhanden() {
 		// Given
 		final Long id = ID_VORHANDEN;
 		
 		// When		
 		Auftrag auftrag = getEntityManager().createNamedQuery(Auftrag.FIND_AUFTRAG_BY_ID, Auftrag.class)
 							.setParameter(Auftrag.PARAM_ID, id)
 							.getSingleResult();
 				
 		// Then
 		assertThat(auftrag.getId(), is(id));
 	}
 	
 	@Test
 	public void findAuftragByIdNichtVorhanden() {
 		// Given
 		final Long id = ID_NICHTVORHANDEN;
 		
 		// When / Then
 		thrown.expect(NoResultException.class);
 		getEntityManager().createNamedQuery(Auftrag.FIND_AUFTRAG_BY_ID, Auftrag.class)
 							.setParameter(Auftrag.PARAM_ID, id)
 							.getSingleResult();
 	}
 	
 	@Test
 	public void findAuftragByKundeVorhanden() {
 		// Given
 		final Long kundeId = ID_KUNDE_VORHANDEN;
 		
 		// When		
 		List<Auftrag> kundenAuftraege = getEntityManager()
 							.createNamedQuery(Auftrag.FIND_AUFTRAG_BY_KUNDE, Auftrag.class)
 							.setParameter(Auftrag.PARAM_KUNDEID, kundeId)
 							.getResultList();
 				
 		// Then
 		assertThat(kundenAuftraege.size(), is(1));
 	}
 	
 	@Test
 	public void createAuftragTest() {
 		// Given
 		final Long kundeId = KUNDEID_NEU;
 		final int position1Anz = POSITION1ANZAHL_NEU;
 		final Long position1ArtikelId = POSITION1ARTIKELID_NEU;
 		final int auftragAnzahlVorCreate = getEntityManager()
											.createNamedQuery(Auftrag.FIND_AUFTRAG_ALL, Auftrag.class)
 											.getResultList()
 											.size();
 		
 		Auftrag auftrag = new Auftrag();
 		Kunde kunde = getEntityManager().createNamedQuery(Kunde.KUNDE_BY_KNR, Kunde.class)
 				.setParameter(Kunde.PARAM_KUNDENNUMMER, kundeId)
 				.getSingleResult();
 		auftrag.setKunde(kunde);
 		
 		//Auftragsposition 1 erstellen und dem Auftrag hinzufuegen
 		Artikel artikel = getEntityManager().createNamedQuery(Artikel.FIND_Artikel_BY_Artikel_ID, Artikel.class)
 											.setParameter(Artikel.PARAM_ID, position1ArtikelId)
 											.getSingleResult();
 		Auftragsposition position1 = new Auftragsposition(artikel, position1Anz);
 		auftrag.addAuftragsposition(position1);
 		
 		// When
 		try {
			getEntityManager().persist(auftrag);			
 		}
 		catch (ConstraintViolationException e) {
 			// Es gibt Verletzungen bzgl. Bean Validation: auf der Console ausgeben
 			final Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
 			for (ConstraintViolation<?> v : violations) {
 				System.err.println("!!! FEHLERMELDUNG>>> " + v.getMessage());
 				System.err.println("!!! ATTRIBUT>>> " + v.getPropertyPath());
 				System.err.println("!!! ATTRIBUTWERT>>> " + v.getInvalidValue());
 			}
 			
 			throw new RuntimeException(e);
 		}
 		
 		// Then
 		List<Auftrag> auftraege = getEntityManager().createNamedQuery(Auftrag.FIND_AUFTRAG_ALL, Auftrag.class)
 													.getResultList();
 		assertThat(auftraege.size(), is(auftragAnzahlVorCreate + 1));
 	}
 }

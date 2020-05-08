 package aip2.m.KundenModul;
 
 import static org.junit.Assert.*;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import aip2.m.PersistenzModul.IPersistenzSessionIntern;
 import aip2.m.PersistenzModul.Persistenz;
 import aip2.m.TransaktionModul.ITransaktionIntern;
 import aip2.m.TransaktionModul.Transaktion;
 
 public final class TestKundenKomponente {
 
 	private static KundenFassade kundenFassade;
 	private static KundenLogik kundenLogik;
 	private static KundenVerwalter kundenVerwalter;
 	private static IPersistenzSessionIntern persistenzSession;
 	private static ITransaktionIntern transaktion;
 
 	public TestKundenKomponente() {
 	}
 
 	@BeforeClass
 	/**
 	 * Starte KundenKomponente
 	 */
 	public static void testSetup() {
 		persistenzSession = Persistenz.getPersistenzSessionIntern();
 		transaktion = Transaktion.getTransaktion(persistenzSession);
 
 		kundenVerwalter = new KundenVerwalter(Persistenz.getPersistenz());
 		kundenLogik = new KundenLogik(kundenVerwalter);
 		kundenFassade = new KundenFassade(kundenLogik, kundenVerwalter,
 				transaktion);
 
 	}
 
 	@Before
 	public void beforeEach() {
 		transaktion.startTransaction();
 	}
 
 	@After
 	public void afterEach() {
		transaktion.commitTransaction();
 	}
 
 	@Test
 	public void testIKundenModulExtern() {
 
 		List<String> ortsListe = Arrays.asList(new String[] { "da", "dort",
 				"da" });
 
 		for (String ort : ortsListe) {
 			// Erstelle Kunden
 			IKunde hansda = kundenFassade.erstelleKunde("Hans", ort);
 			// Prüfe ob die DB das gleiche Objekt zurückgibt
 			IKunde hansdaDB = persistenzSession.getById(Kunde.class,
 					hansda.getKundenNr());
 			assertEquals("DB sollte gleiches Objekt zurückgeben", hansda,
 					hansdaDB);
 		}
 
 		// Prüfe ob auch alle Hanse in DB findbar sind
 		List<KundenTyp> gesuchte = kundenFassade.sucheKunden("Hans");
 		assertEquals("Es sollte 3 Hanses geben", ortsListe.size(),
 				gesuchte.size());
 
 		// Prüfe ob es keine KEINGÜLTIGERNAME!?! in DB findbar sind
 		List<KundenTyp> gesuchte2 = kundenFassade
 				.sucheKunden("KEINGÜLTIGERNAME!?!");
 		assertEquals("Es sollte keine KEINGÜLTIGERNAME!?! geben", 0,
 				gesuchte2.size());
 	}
 
 }

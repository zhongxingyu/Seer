 package com.polytech.propps.models;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.sql.Date;
 
 import org.junit.*;
 
 import com.polytech.propps.appli.PROPPS;
 import com.polytech.propps.utilsTest.Comparaison;
 
 public class RecruteurTest {
 	private Recruteur r1;
	private Recruteur r2;
 	private Date date;
 	private Societe nintendo;
 	
 	@BeforeClass
 	public static void setUpClass() throws IOException, ClassNotFoundException {
 		PROPPS.init();
 	}
 	
 	@AfterClass
 	public static void tearDownClass() {
 		
 	}
 	
 	@Before
 	public void setUp() {
 		date = new Date(2013, 02, 19);
 		r1 = new Recruteur("Test", "Mathieu", "mathieu.test@u-psud.fr", "kubor", nintendo);
 		
 	}
 	
 	@After
 	public void tearDown() {
 		r1.delete();
		r2.delete();
 	}
 	
 	@Test
 	public void Constructeur1Test() {
 		
 		int fake_id = 2;
 		Recruteur r = new Recruteur(fake_id);
 		
 		// Test de super(ID)
 		Assert.assertEquals("La valeur ID n'est pas initialisée correctement.", r.getID_Utilisateur(), fake_id);
 	
 		// Test de l'initialisation de societe
		Assert.assertNull("La société n'est pas initialisée à NULL.",r.getSociete());
 	}
 	/*
 	@Test
 	public void Constructeur2Test() {
 		
 		// Test de super(sNom, sPrenom, sEmail, sPassword)
 		Assert.assertEquals("La valeur sNom n'est pas initalisée correctement.", r1.getsNom(), "Test");
 		Assert.assertEquals("La valeur de sPrenom n'est pas initialisée correctement.", r1.getsPrenom(), "Mathieu");
 		Assert.assertEquals("La valeur de sEmail n'est pas initialisée correctement.", r1.getsEmail(), "mathieu.test@u-psud.fr");
 		Assert.assertEquals("La valeur de sPassword n'est pas initialisée correctement.", r1.getsPassword(), "kubor");
 		
 		// Test de l'initialisation de societe
 		Assert.assertEquals("La valeur de societe n'est pas initialisée correctement.", r1.getSociete(), nintendo);
 	}
 	
 	@Test
 	public void fillTest() {
 		Recruteur r4 = new Recruteur(r1.getID_Utilisateur());
 		r4.fill();
 		Comparaison.comparerDeuxRecruteurs(r1, r4);
 		r4.delete();
 	}
 	
 	@Test
 	public void insertOrUpdateTest() {
 		r1.insertOrUpdate();
 		Recruteur r3 = new Recruteur(r1.getID_Utilisateur());
 		Comparaison.comparerDeuxRecruteurs(r1, r3);
 		r3.delete();
 	}
 	
 	@Test
 	public void deleteTest() {
 		// Vérifier qu'il est bien dans la base
 		Integer indiceBase = r1.getID_Utilisateur();
 		Recruteur r6 = new Recruteur(r1.getID_Utilisateur());
 		Comparaison.comparerDeuxRecruteurs(r1, r6);
 		r6.delete();
 		
 		// delete
 		r1.delete();
 		
 		// checker à l'indice de m1 il n'y a plus rien
 		Recruteur r7 = new Recruteur(indiceBase);
 		Comparaison.RecruteurEstVide(r7);
 			
 	}*/
 }

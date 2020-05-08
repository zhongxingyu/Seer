 package jdbc;
 
 import static org.junit.Assert.*;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import fr.eservice.jdbc.Database;
 import fr.eservice.jdbc.Etudiant;
 
 public class TestEtudiant {
 	
 	private static Connection db;
 	
 	@BeforeClass
 	public static void initDB() {
 		db = Database.getInstance();
 	}
 	
 	@Before
 	public void clearEtudiant() throws SQLException {
 		Statement stm = db.createStatement();
 		stm.execute("delete from etudiant");
 	}
 	
 	private int saveEtudiant(String firstName, String lastName, int age) {
 		Etudiant etudiant = new Etudiant();
 		etudiant.setFirstname(firstName);
		etudiant.setLastname(lastName);
 		etudiant.setAge(age);
 		etudiant.save(db);
 		return etudiant.getId();
 	}
 	
 	@Test
 	public void canSave() {
 		Etudiant etudiant = new Etudiant();
 		etudiant.setFirstname("Guillaume");
		etudiant.setLastname("Dufrne");
 		etudiant.setAge(31);
 		
 		boolean saved = etudiant.save(db);
 		
 		assertTrue( "Etudiant n'a pas t sauvegard ?", saved );
 		assertTrue( "Un identifiant doit tre plac", etudiant.getId() > 0 );
 	}
 	
 	@Test
 	public void canLoad() {
 		int id = saveEtudiant("Guillaume", "Dufrne", 31);
 		Etudiant etudiant = Etudiant.load(db, id);
 		
 		assertEquals("Guillaume", etudiant.getFirstname());
 		assertEquals("Dufrne", etudiant.getLastname());
 		assertEquals(31, etudiant.getAge());
 		assertEquals(id, etudiant.getId());
 	}
 	
 	@Test
 	public void loadNotFound() {
 		Etudiant etudiant = Etudiant.load(db, 0xCAFE);
 		assertNull("Doit retourner null lorsqu'il n'existe pas d'tudiant de cet ID", etudiant);
 	}
 	
 	private int[] init_BeforeAfter() {
 		int[] ids = new int[] {
 			saveEtudiant("Guillaume",  "Dufrne", 31),
 			saveEtudiant("Jean",       "Dupond",  28),
 			saveEtudiant("Christophe", "Martin",  32),
 			saveEtudiant("Julien",     "Durand",  27)
 		};
 		return ids;
 	}
 	
 	@Test
 	public void testAfter() {
 		int[] ids = init_BeforeAfter();
 		Etudiant etudiant;
 		
 		etudiant = Etudiant.after( ids[0] );
 		assertEquals( "Dupond", etudiant.getLastname());
 		
 		etudiant = Etudiant.after( ids[1] );
 		assertEquals( "Martin", etudiant.getLastname());
 	}
 	
 	@Test
 	public void testAfterLast() {
 		int[] ids = init_BeforeAfter();
 		Etudiant etudiant;
 		etudiant = Etudiant.after( ids[3] );
 		assertNull( etudiant );
 	}
 	
 	@Test
 	public void testBefore() {
 		int[] ids = init_BeforeAfter();
 		Etudiant etudiant;
 		
 		etudiant = Etudiant.before( ids[3] );
 		assertEquals( "Christophe", etudiant.getFirstname());
 		
 		etudiant = Etudiant.before( ids[1] );
 		assertEquals( "Guillaume", etudiant.getFirstname());
 	}
 
 	@Test
 	public void testBeforeFirst() {
 		int[] ids = init_BeforeAfter();
 		Etudiant etudiant;
 		etudiant = Etudiant.before( ids[0] );
 		assertNull( etudiant );
 	}
 
 }

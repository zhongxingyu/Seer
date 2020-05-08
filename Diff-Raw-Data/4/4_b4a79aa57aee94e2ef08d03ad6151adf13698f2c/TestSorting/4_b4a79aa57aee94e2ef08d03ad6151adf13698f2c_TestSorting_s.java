 /*
  * Example code used in exercises for lecture "Grundlagen des Software-Testens"
  * Created and given by Ina Schieferdecker and Edzard Hoefig
  * Freie Universitaet Berlin, SS 2012
  */
 package exercise5.test;
 
 import java.awt.Component;
 import java.io.IOException;
 
 import javax.swing.JTable;
import javax.swing.plaf.basic.BasicBorders.RadioButtonBorder;
 import javax.swing.table.TableModel;
 
 import junit.extensions.abbot.ComponentTestFixture;
 
 import static org.junit.Assert.*;
 import static org.hamcrest.CoreMatchers.*;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import abbot.finder.ComponentNotFoundException;
 import abbot.finder.MultipleComponentsFoundException;
 import abbot.finder.matchers.NameMatcher;
 import abbot.tester.JButtonTester;
 import abbot.tester.JComponentTester;
 import abbot.tester.JTableLocation;
 import abbot.tester.JTableTester;
 import abbot.tester.JTextComponentTester;
 
 /**
  * Uebung 5 - Black Box Test
  * GUI testing
  * 
  * Bitte Nummer der Gruppe eintragen:
  * 0
  * 
  * Bitte Gruppenmitglieder eintragen:
  * @author ...
  */
 public class TestSorting extends ComponentTestFixture {
 
 	// Tester for (radio) button components
 	private JButtonTester buttonTester;
 
 	// Tester for table components
 	private JTableTester tableTester;
 
 	// Tester for text field components
 	private JTextComponentTester textTester;
 	
 	private TableModel tableModel;
 
 	
 	/**
 	 * Creates test fixture
 	 */
 	@Before
 	public void setUp() throws Exception {
 
 		// Start the application
 		exercise5.addressbook.Manager.main(null);
 		
 		// Setup some test instrumentation
 		this.buttonTester = new JButtonTester();
 		this.tableTester = new JTableTester();
 		this.textTester = new JTextComponentTester();
 		
 		this.tableModel = ((JTable) findByName("viewTable")).getModel();
 		
 		loadData();
 	}
 
 	/**
 	 * Removes test fixture
 	 */
 	@After
 	public void tearDown() throws Exception {
 		// Nothing to do
 	}
 	
 	/**
 	 * Test editing of an entry.
 	 * This test case serves as a small tutorial on using the Abbot GUI testing framework. Please remove it before submitting the exercise.
 	 * @throws ComponentNotFoundException When a GUI component was not available
 	 * @throws MultipleComponentsFoundException When there is an ambiguous resolution of GUI components 
 	 * @throws IOException In case the address book data file could not be read 
 	 */
 	public void testEdit() throws ComponentNotFoundException, MultipleComponentsFoundException, IOException {
 		
 	    // Is the correct data in the table?
 	    TableModel content = ((JTable) getFinder().find(new NameMatcher("viewTable"))).getModel();
 	    assertEquals("Dagobert", content.getValueAt(0, 0));
 	    assertEquals("Duck", content.getValueAt(0, 1));
 	    assertEquals("M", content.getValueAt(0, 2));
 	    assertEquals("dagobert@duck-enterprises.com", content.getValueAt(0, 3));
 	    assertEquals("1.10.1911", content.getValueAt(0, 4));
 	    
 	    // Edit first entry in table
 	    tableTester.actionSelectCell(getFinder().find(new NameMatcher("viewTable")), new JTableLocation(0,0));
 	    buttonTester.actionClick(getFinder().find(new NameMatcher("editButton")));
 	   
 	    // Change entry's values
 	    textTester.actionEnterText(getFinder().find(new NameMatcher("firstNameTextfield")), "Foo");
 	    textTester.actionEnterText(getFinder().find(new NameMatcher("lastNameTextfield")), "Bar");
 	    buttonTester.actionClick(getFinder().find(new NameMatcher("femaleRadiobutton")));
 	    buttonTester.actionClick(getFinder().find(new NameMatcher("phoneRadiobutton")));
 	    textTester.actionEnterText(getFinder().find(new NameMatcher("contactInformationTextfield")), "999999");
 	    textTester.actionEnterText(getFinder().find(new NameMatcher("dateOfBirthTextfield")), "1.1.1111");
 	    
 	    // Release dialog
 	    buttonTester.actionClick(getFinder().find(new NameMatcher("okButton")));
 	    
 	    // Did the data change properly in the table?
 	    content = ((JTable) getFinder().find(new NameMatcher("viewTable"))).getModel();
 	    assertEquals("Foo", content.getValueAt(0, 0));
 	    assertEquals("Bar", content.getValueAt(0, 1));
 	    assertEquals("F", content.getValueAt(0, 2));
 	    assertEquals("999999", content.getValueAt(0, 3));
 	    assertEquals("1.1.1111", content.getValueAt(0, 4));
 	}
 	
 	/*
 	 * Aufgabe 4
 	 * Verwenden Sie JUnit zur Ueberpruefung der korrekten Sortierreihenfolge beim Hinzufuegen von Eintraegen in das Adressbuch.  
 	 * Testen Sie dabei ausschliesslich nach Black-Box Prinzipien und greifen Sie niemals direkt auf Klassen zu
 	 * die in den sub-packages model, view und controller des package exercise5.addressbook definiert sind. 
 	 * Verwenden Sie das Abbot GUI test framework zur Testdurchfuehrung.
 	 * 
 	 * Hinweis:
 	 * Die aktuelle Version von Abbot (1.2.0) hat auf manchen Systemen (z.B. OS X 10.7) Schwierigkeiten die richtige "Keymap" zu 
 	 * erkennen. Als Folge davon werden einige Zeichen nicht richtig in die Textfelder eingetragen (z.B. Sonderzeichen, 
 	 * y und z vertauscht...). Bitte ueberpruefen Sie bei Ihren Testfaellen, ob Abbot die richtigen Testdaten eintraegt und waehlen 
 	 * Sie ggfs. andere.
 	 */
 	
 	public void testSortsCorrect() throws ComponentNotFoundException, MultipleComponentsFoundException {
 
 		// add some on first position
 		addEntry("Alfons", "Anfang", "a.a@entenhausen.de", "4.12.1912");
 		assertFirstAndLastName(0, new String("Alfons"), new String("Anfang"));
 		
 		// add someone in the middle
 		addEntry("Emil", "Erpel", "123451", "1.1.1969");
 		assertFirstAndLastName(8, new String("Emil"), new String("Erpel"));
 		
 		// add someone in the end
 		addEntry("Zacharias", "Zorngiebel", "654321", "5.12.1255");
 		assertFirstAndLastName(tableModel.getRowCount() - 1, new String("Zacharias"), new String("Zorngiebel"));
 		
 	}
 	
 //	- unterschiedlicher Nachname, gleicher Vorname
 	public void testSortingDifferentLastnameSameFirstname() 
 			throws ComponentNotFoundException, MultipleComponentsFoundException {
 		String dagobert = "Dagobert";
 		String biber = "Biber";
 		addEntry(dagobert, biber, "d.b@entenhausen.de", "1.1.1950");
 		assertFirstAndLastName(0, dagobert, biber);
 		
 	}
 	
 //	- gleicher Nachname, unterschiedlicher Vorname
 	public void testSortingSameLastnameDifferentFirstName()
 			throws ComponentNotFoundException, MultipleComponentsFoundException {
 		String alfons = "Alfons";
 		String duck = "Duck";
 		addEntry(alfons, duck, "d.b@entenhausen.de", "1.1.1950");
 		assertFirstAndLastName(0, alfons, duck);
 	}
 	
 //	- Groß- und Kleinschreibung
 	/* Ich bin mir nicht sicher was 
 	 * "Zwischen Gro-und Kleinschreibung wird nicht unterschieden."
 	 * genau bedeuten soll.
 	 * Ich interpretiere es so, dass "dagobert" und "Dagobert" der selbe Name
 	 * ist und somit nicht eingefügt wird.
 	 */
 	public void testSortingSameNameDifferentCase() throws Exception {
 		String dagobert = "dAgObErT";
 		String duck = "DuCk";
 		addEntry(dagobert, duck, "d.b@entenhausen.de", "1.1.1950");
 		
 		// the entry was not added to table
 		// neither in first row nor in second row
 		assertThat(dagobert, 	is( not( tableModel.getValueAt(0, 0))));
 		assertThat(duck, 		is( not( tableModel.getValueAt(0, 1))));
 		assertThat(dagobert, 	is( not( tableModel.getValueAt(1, 0))));
 		assertThat(duck, 		is( not( tableModel.getValueAt(1, 1))));
 	}
 	
 //	- Ignorieren von existierenden Einträgen (Nachname, Vorname)
 	public void testAddingSameName() throws Exception {
 		String dagobert = "Dagobert";
 		String duck = "Duck";
 		addEntry(dagobert, duck, "d.b@entenhausen.de", "1.1.1950");
 		
 		// the entry was not added to table
 		// neither in first row nor in second row
 		assertThat(dagobert, 	is( tableModel.getValueAt(0, 0)));
 		assertThat(dagobert, 	is( not( tableModel.getValueAt(1, 0))));
 	}
 	
 //	- Einträge ohne Vorname
 	public void testAddingNoName() throws Exception {
 		String noName = "";
 		String dagobert = "Dagobert";
 		String duck = "Duck";
 		addEntry(noName, duck, "d.b@entenhausen.de", "1.1.1950");
 		addEntry(dagobert, noName, "d.b@entenhausen.de", "1.1.1950");
 		addEntry(noName, noName, "d.b@entenhausen.de", "1.1.1950");
 		// the entry was not added to table
 		// neither in first row nor in second row
 		for (int i = 0; i < tableModel.getRowCount(); i++){
 			assertThat("failure in row "+i,	(String) tableModel.getValueAt(i, 0), is( not(noName)));
 			assertThat("failure in row "+i, (String) tableModel.getValueAt(0, i), is( not(noName)));	
 		}
 	}
 
 //	- Sortierung von Sonderzeichen
 //	- Umsortierung nach Bearbeitung
 
 	public void testSortingAfterEdit() throws Exception {
 		
 		  // Edit first entry in table
 	    tableTester.actionSelectCell(findByName("viewTable"), new JTableLocation(0,0));
 	    buttonTester.actionClick(findByName("editButton"));
 	 
 	    // Change entry's values
 	    textTester.actionEnterText(findByName("firstNameTextfield"), "Dagobert");
 	    textTester.actionEnterText(findByName("lastNameTextfield"), "Erpel");
 	    buttonTester.actionClick(findByName("femaleRadiobutton"));
 	    buttonTester.actionClick(findByName("phoneRadiobutton"));
 	    textTester.actionEnterText(findByName("contactInformationTextfield"), "999999");
 	    textTester.actionEnterText(findByName("dateOfBirthTextfield"), "1.1.1111");
 	    buttonTester.actionClick(findByName("okButton"));
 		     
 	    assertThat( (String) tableModel.getValueAt(6, 0), is( "Dagobert"));
 	    assertThat( (String) tableModel.getValueAt(6, 1), is( "Erpel"));
 		
 	}
 	
 	private void assertFirstAndLastName(int rowIndex, String string, String string2) {
 		assertEquals(string, tableModel.getValueAt(rowIndex, 0));
 		assertEquals(string2, tableModel.getValueAt(rowIndex, 1));
 	}
 
 	private void loadData() throws ComponentNotFoundException, MultipleComponentsFoundException {
 		buttonTester.actionClick(findByName("loadButton"));
 		
 	}
 
 	public void addEntry(String firstname, String lastname, String contact, String dob) 
 			throws ComponentNotFoundException, MultipleComponentsFoundException {
 		
 		buttonTester.actionClick(findByName("addButton"));
 		
 		textTester.actionEnterText(findByName("firstNameTextfield"),firstname);
 		textTester.actionEnterText(findByName("lastNameTextfield"),lastname);
 		
 		buttonTester.actionClick(findByName("maleRadiobutton"));
 		textTester.actionEnterText(findByName("contactInformationTextfield"), contact);
 	    textTester.actionEnterText(findByName("dateOfBirthTextfield"), dob);
 	    
 	    buttonTester.actionClick(findByName("okButton"));
 	}
 	
 	public Component findByName(String name) throws ComponentNotFoundException, MultipleComponentsFoundException{
		return findByName(name);
 	}
 }

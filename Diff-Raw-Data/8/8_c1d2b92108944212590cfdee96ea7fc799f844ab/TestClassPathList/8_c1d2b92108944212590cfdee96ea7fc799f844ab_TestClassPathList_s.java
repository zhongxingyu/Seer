 package fest.org.kalisen.classpathdoctor.gui;
 
 import java.io.File;
 
 import org.fest.swing.annotation.GUITest;
 import org.fest.swing.fixture.FrameFixture;
 import org.fest.swing.fixture.JListItemFixture;
 import org.kalisen.classpathdoctor.DirectoryPath;
 import org.kalisen.classpathdoctor.gui.ClassPathDoctorGUI;
 import org.testng.Assert;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 @Test
 @GUITest
 public class TestClassPathList {
 
 	private final static DirectoryPath CURRENT_DIR = new DirectoryPath(
 			new File("."));
 
 	private FrameFixture frame = null;
 
 	@BeforeMethod(alwaysRun=true)
 	protected void setUp() {
 		this.frame = new FrameFixture(new ClassPathDoctorGUI());
 		this.frame.show();
 	}
 	
 	@Test(groups="DnD")
 	public void testDroppingOnStartingPointShouldHaveNoEffect() {
 		final String ENTRY1 = "entry1";
 		final String ENTRY2 = "entry2";
 		final String ENTRY3 = "entry3";
 		addEntry(ENTRY1);
 		addEntry(ENTRY2);
 		addEntry(ENTRY3);
 		this.frame.list().drag(2);
 		this.frame.list().drop(2);
 		assertListEntryEquals(this.frame.list().item(0), ENTRY1);
 		assertListEntryEquals(this.frame.list().item(1), ENTRY2);
 		assertListEntryEquals(this.frame.list().item(2), ENTRY3);
 		this.frame.list().drag(2);
 		this.frame.list().drop(0);
 		assertListEntryEquals(this.frame.list().item(0), ENTRY3);
 		assertListEntryEquals(this.frame.list().item(1), ENTRY1);
 		assertListEntryEquals(this.frame.list().item(2), ENTRY2);
		this.frame.list().requireSelection(ENTRY3);
 	}
 
 	@Test(groups="DnD")
 	public void testDraggingObjectToTheTop() {
 		final String ENTRY1 = "entry1";
 		final String ENTRY2 = "entry2";
 		final String ENTRY3 = "entry3";
 		addEntry(ENTRY1);
 		addEntry(ENTRY2);
 		addEntry(ENTRY3);
 		this.frame.list().drag(2);
 		this.frame.list().drop(0);
 		assertListEntryEquals(this.frame.list().item(0), ENTRY3);
 		assertListEntryEquals(this.frame.list().item(1), ENTRY1);
 		assertListEntryEquals(this.frame.list().item(2), ENTRY2);
		this.frame.list().requireSelection(ENTRY3);
 	}
 
 	@Test(groups="DnD")
 	public void testDraggingObjectToTheBottom() {
 		final String ENTRY1 = "entry1";
 		final String ENTRY2 = "entry2";
 		final String ENTRY3 = "entry3";
 		addEntry(ENTRY1);
 		addEntry(ENTRY2);
 		addEntry(ENTRY3);
 		this.frame.list().drag(0);
 		this.frame.list().drop(2);
 		assertListEntryEquals(this.frame.list().item(0), ENTRY2);
 		assertListEntryEquals(this.frame.list().item(1), ENTRY3);
 		assertListEntryEquals(this.frame.list().item(2), ENTRY1);
		this.frame.list().requireSelection(ENTRY1);
 	}
 	
 	protected void addEntry(String path) {
 		File file = new File(path);
 		this.frame.button("ADD_ENTRY").click();
 		this.frame.fileChooser().setCurrentDirectory(CURRENT_DIR.getFile())
 				.selectFile(file).approve();
 	}
 
 	protected void assertListEntryEquals(JListItemFixture listItemFixture,
 			String expectedRelativePath) {
 		Assert.assertEquals(listItemFixture.value(), new File(
 				expectedRelativePath).getAbsolutePath());
 	}
 
 	@AfterMethod(alwaysRun=true)
 	public void tearDown() {
 		this.frame.cleanUp();
 	}
 
 }

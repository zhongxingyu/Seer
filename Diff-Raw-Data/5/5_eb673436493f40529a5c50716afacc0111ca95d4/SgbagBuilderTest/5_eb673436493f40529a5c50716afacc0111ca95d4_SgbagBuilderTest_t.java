 package aeroport.sgbag.xml;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import lombok.extern.log4j.Log4j;
 
 import org.apache.log4j.PropertyConfigurator;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import aeroport.sgbag.utils.CircuitGenerator;
 import aeroport.sgbag.views.VueHall;
 import aeroport.sgbag.views.VueRail;
 
 @Log4j
 public class SgbagBuilderTest {
 
 	@BeforeClass
 	public static void beforeClass() {
 		PropertyConfigurator.configure("log4j.properties");
 	}
 
 	@Test
 	public void testSerialize() throws IOException {
 		SgbagBuilder builder = new SgbagBuilder(
 				"src/test/java/aeroport/sgbag/xml/test1.xml");
 		CircuitGenerator vh = new CircuitGenerator(new VueHall(new Shell(),
 				SWT.NONE));
 
		String xml = builder.serialize(vh.getVueHall());
 
 		log.debug(xml);
 	}
 
 	@Test
 	public void testDeserialize() throws FileNotFoundException {
 		Display display = new Display();
 		Shell shell = new Shell(display);
 		shell.setText("VueTest");
 		shell.setLayout(new FillLayout());
 		shell.setSize(800, 800);
 		VueHall vueHall = new VueHall(shell, SWT.NONE);
 		vueHall.setSize(300, 300);
 		
 		SgbagBuilder builder = null;
 		try {
 			builder = new SgbagBuilder(
 					"src/test/java/aeroport/sgbag/xml/test3.xml");
 		} catch (IOException e) {
 			fail();
 		}
 
 		CircuitGenerator cg = null;
 		try {
 			cg = builder.deserialize();
 		} catch (IOException e) {
 			fail();
 		}
 		assertNotNull(cg);
 		
 		cg.generateAll(vueHall);
 		
 		shell.open();
 		vueHall.updateView();
 		vueHall.draw();
 		
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch())
 				display.sleep();
 		}
 	}
 
 	@Test
 	public void serializeBig() {
 		Shell shell = new Shell();
 		shell.setText("VueTest");
 		shell.setLayout(new FillLayout());
 		shell.setSize(800, 800);
 		VueHall vueHall = new VueHall(shell, SWT.NONE);
 		vueHall.setSize(300, 300);
 
 		CircuitGenerator cg = new CircuitGenerator(vueHall);
 
 		Point p1 = new Point(20, 20);
 		Point p2 = new Point(100, 20);
 		Point p3 = new Point(200, 100);
 		Point p4 = new Point(500, 500);
 		Point p5 = new Point(40, 300);
 		Point p6 = new Point(40, 400);
 
 		cg.createSegment(p1, p2);
 		cg.createSegment(p2, p3);
 		cg.createSegment(p3, p4);
 		cg.createSegment(p4, p5);
 		VueRail vueRail1 = cg.createSegment(p5, p6);
 		cg.createSegment(p3, p5);
 		cg.createExit(p3);
 		cg.createEntry(p5, 100, 10, 5, false);
 		cg.addChariot(vueRail1.getRail(), 40, 20, 200, null, null, null);
 
 		SgbagBuilder builder = null;
 		try {
 			builder = new SgbagBuilder(
 					"src/test/java/aeroport/sgbag/xml/test2.xml");
 		} catch (IOException e) {
 			log.error(e);
 			fail();
 		}
 
 		try {
			builder.serialize(cg.getVueHall());
 		} catch (IOException e) {
 			log.error(e);
 			fail();
 		}
 	}
 }

 import java.io.FileWriter;
 
 public class EricTest {
 
 	/**
 	 * @param args
 	 * @throws Exception 
 	 */
 	public static void main(String[] args) throws Exception {
 		XAALScripter scripter = new XAALScripter();
 		
 		String rect1 = scripter.addRectangle(10, 10, 60 , 60);
 		String rect2 = scripter.addRectangle(10, 110, 60, 160);
 		String text1 = scripter.addText(50, 20, "awesome");
 		
 		scripter.addArrow(rect1, rect2, false);
 		
 		String triangle = scripter.addTriangle(20, 200, 100);
 		
 		scripter.startSlide();
		scripter.addTranslate(56, 300, rect1, triangle);
		scripter.addTranslate(-45, -30, text1);
 		scripter.endSlide();
 		
 		scripter.startSlide();
 		scripter.startPar();
 		
		scripter.addTranslate(4, -300, rect2);
 		scripter.endPar();
 		scripter.endSlide();
 		
 		FileWriter writer = new FileWriter("C:\\Users\\Eric\\Desktop\\test.xaal");
 		
 		writer.write(scripter.toString());
 		
 		writer.close();
 	}
 
 }
 
